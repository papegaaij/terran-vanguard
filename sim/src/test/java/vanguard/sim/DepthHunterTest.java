package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M8 part G, step G1 (design/enemies/naval/depth-hunter; m8g-gaps.md §1.1, §5 G4): a column of Depth Hunters rises from
 * below the bottom edge (warned there) in one x, each to its own height in the lower band (380–460 px below the top
 * edge), holds its 10 s drifting 12 px/s sideways (the column fanning out) with its facing up the screen, launches a
 * homing torpedo along its facing at its interval (at most its gun's most in flight), and leaves straight down. The
 * torpedoes go for a burning decoy flare and burst on the Bulwark's hull. Deterministic and allocation-free.
 */
class DepthHunterTest {
    /** A torpedo every 3.5 s (the first 0.5 s after the stop), 90 px/s, `heavy` 10, 70°/s, 5 s, 3 HP, 8 at most. */
    static final EnemyGun TORPEDO = EnemyGun.aimed(3.5, 0.5, 1, 90, 10, false)
            .withHoming(Optional.of(new EnemyGun.HomingSpec(Math.toRadians(70), 5, 3, 8, 0)));

    /** 72×40 / 56×28, 120 HP, 80 px/s, hover 10 s at 380–460 px below the top edge drifting 12 px/s. */
    static EnemySpec hunter(EnemyGun gun) {
        return EnemySpec.of("depth-hunter", 120, new Hitbox(56, 28), Layer.AIR, 10, false, 30, 80)
                .withHover(
                        Optional.of(new EnemySpec.Hover(new Range(10, 10), new Range(380, 460), Optional.empty(), 12)))
                .withGun(Optional.of(gun));
    }

    static final EnemySpec HUNTER = hunter(TORPEDO);

    static WaveSpec column(double t, int count) {
        return TestSpecs.wave(t, WaveSpec.Formation.COLUMN, HUNTER, count, WaveSpec.Entry.REAR, WaveSpec.Edge.NONE);
    }

    private static Sortie sortie(List<WaveSpec> waves) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                TestSpecs.level(40, waves),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Per serial, where it stopped (entered its hold): x, y and the step. */
    private record Stop(double x, double y, int tick) {}

    private static Map<Integer, Stop> fly(Sortie sortie, int steps, List<double[]> trail) {
        Map<Integer, Stop> stops = new HashMap<>();
        Map<Integer, Double> lastY = new HashMap<>();
        for (int tick = 0; tick < steps; tick++) {
            sortie.step(Command.NONE);
            for (Enemy unit : AbyssRayTest.all(sortie, "depth-hunter")) {
                Double was = lastY.put(unit.serial(), unit.y());
                if (was != null && was == unit.y() && !stops.containsKey(unit.serial())) {
                    stops.put(unit.serial(), new Stop(unit.x(), unit.y(), tick - 1));
                }
                if (trail != null) {
                    trail.add(new double[] {unit.serial(), unit.x(), unit.y(), unit.facing()});
                }
            }
        }
        return stops;
    }

    @Test
    void aColumnRisesFromBelowInOneXEachToItsOwnHeightInTheLowerBand() {
        Sortie sortie = sortie(List.of(column(1, 4)));
        List<double[]> trail = new ArrayList<>();
        Map<Integer, Stop> stops = fly(sortie, SimStep.ticks(12), trail);
        assertEquals(4, stops.size(), "all four stopped");
        Set<Long> heights = new HashSet<>();
        for (Stop stop : stops.values()) {
            double depth = PlayField.HEIGHT - stop.y();
            assertTrue(depth >= 380 - 1e-9 && depth <= 460 + 1e-9, "380-460 px below the top edge: " + depth);
            heights.add(Math.round(depth));
        }
        assertEquals(4, heights.size(), "each its own height: " + heights);
        // They came up from below the bottom edge, in the column's x.
        for (double[] point : trail) {
            if (point[2] < 0) {
                assertEquals(PlayField.WIDTH / 2.0, point[1], 1e-9, "the column's x below the edge");
            }
        }
        assertTrue(trail.stream().anyMatch(point -> point[2] < 0), "entered from below the bottom edge");
        // Never two of them overlapping on the way up.
        for (Stop a : stops.values()) {
            for (Stop b : stops.values()) {
                if (a != b) {
                    assertTrue(Math.abs(a.y() - b.y()) >= 20, "apart in height: " + a + " " + b);
                }
            }
        }
    }

    @Test
    void aRearColumnIsWarnedAtTheBottomEdge() {
        Sortie sortie = sortie(List.of(column(4, 3)));
        boolean warned = false;
        for (int i = 0; i < SimStep.ticks(3.9); i++) {
            sortie.step(Command.NONE);
            warned |= WarningEdge.BOTTOM.in(sortie.edgeWarnings());
        }
        assertTrue(warned, "BOTTOM warning before the column");
    }

    @Test
    void holdingItDriftsTwelvePixelsASecondFacingUpThenLeavesStraightDown() {
        Sortie sortie = sortie(List.of(column(1, 2)));
        List<double[]> trail = new ArrayList<>();
        Map<Integer, Stop> stops = fly(sortie, SimStep.ticks(9), trail);
        assertEquals(2, stops.size());
        List<Enemy> units = AbyssRayTest.all(sortie, "depth-hunter");
        assertEquals(2, units.size());
        Set<Integer> sides = new HashSet<>();
        for (Enemy unit : units) {
            Stop stop = stops.get(unit.serial());
            double seconds = (SimStep.ticks(9) - 1 - stop.tick()) * SimStep.SECONDS;
            double moved = unit.x() - stop.x();
            assertEquals(12 * seconds, Math.abs(moved), 12 * 2 * SimStep.SECONDS + 1e-6, "12 px/s: " + moved);
            sides.add((int) Math.signum(moved));
            assertEquals(stop.y(), unit.y(), 1e-9, "at its depth");
            assertEquals(Math.PI, Math.abs(unit.facing()), 1e-9, "facing up the screen");
        }
        assertEquals(Set.of(-1, 1), sides, "the column fans out");
        // After its 10 s hold it leaves straight down and off the bottom edge.
        Map<Integer, double[]> last = new HashMap<>();
        int leaving = 0;
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(Command.NONE);
            for (Enemy unit : AbyssRayTest.all(sortie, "depth-hunter")) {
                double[] was = last.put(unit.serial(), new double[] {unit.x(), unit.y()});
                if (was != null && unit.y() < was[1]) {
                    assertEquals(was[0], unit.x(), 1e-9, "straight down");
                    leaving++;
                }
            }
        }
        assertTrue(leaving > 0, "they left down");
        TorpedoTest.run(sortie, SimStep.ticks(3), Command.NONE);
        assertTrue(AbyssRayTest.all(sortie, "depth-hunter").isEmpty(), "gone off the bottom edge");
    }

    @Test
    void itLaunchesItsTorpedoesAlongItsFacingUpTheScreen() {
        Sortie sortie = sortie(List.of(column(1, 1)));
        int launched = 0;
        for (int i = 0; i < SimStep.ticks(9) && launched == 0; i++) {
            int before = sortie.missileCount();
            sortie.step(Command.NONE);
            if (sortie.missileCount() > before) {
                launched++;
                Missile torpedo = sortie.missile(sortie.missileCount() - 1);
                Enemy hunter = AbyssRayTest.find(sortie, "depth-hunter");
                // One step of flight: turned at most its turn rate's step from straight up.
                double off = Math.abs(Math.IEEEremainder(torpedo.heading() - Math.PI, 2 * Math.PI));
                assertTrue(off <= Math.toRadians(70) * SimStep.SECONDS + 1e-9, "along its facing: " + off);
                assertEquals(hunter.x(), torpedo.x(), 2, "from the hunter");
            }
        }
        assertEquals(1, launched);
        // It steers toward the ship below it afterwards and bursts on it (the ship flies on, invulnerable).
        List<Integer> ends = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.MISSILE_DESTROYED) {
                    ends.add(events.value(e));
                }
            }
        }
        assertFalse(ends.isEmpty(), "its torpedo ended: " + ends);
    }

    @Test
    void atMostItsGunsMostTorpedoesFlyAtOnce() {
        EnemyGun rapid = EnemyGun.aimed(0.1, 0, 1, 20, 10, false)
                .withHoming(Optional.of(new EnemyGun.HomingSpec(Math.toRadians(70), 5, 3, 2, 0)));
        Sortie sortie = sortie(List.of(TestSpecs.wave(
                1, WaveSpec.Formation.COLUMN, hunter(rapid), 1, WaveSpec.Entry.REAR, WaveSpec.Edge.NONE)));
        int most = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.NONE);
            most = Math.max(most, sortie.missileCount());
        }
        assertEquals(2, most);
    }

    @Test
    void aTorpedoGoesForABurningDecoyFlare() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withSpecial(Act4SpecialsTest.flares(6)),
                TestSpecs.level(40, List.of(column(1, 1))),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        boolean decoyed = false;
        for (int i = 0; i < SimStep.ticks(9); i++) {
            int before = sortie.missileCount();
            sortie.step(Command.NONE);
            if (sortie.missileCount() > before) {
                // The torpedo is away: fire the flares.
                sortie.step(Command.SPECIAL.bit());
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                decoyed |= events.type(e) == SimEvents.Type.MISSILE_DESTROYED && events.value(e) == Missiles.DECOYED;
            }
        }
        assertTrue(decoyed, "it burst on a flare");
    }

    @Test
    void aTorpedoBurstsOnTheBulwark() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.loadout().withBulwark(BulwarkTest.BULWARK),
                TestSpecs.level(40, List.of()),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        TorpedoTest.run(sortie, SimStep.ticks(1), Command.NONE);
        Bulwark bulwark = sortie.bulwark().orElseThrow();
        double armour = bulwark.armour();
        // A torpedo right below it, heading up into it.
        assertTrue(sortie.missiles().launchAlong(bulwark.x(), bulwark.y() - 14, TORPEDO, Math.PI));
        boolean struck = false;
        for (int i = 0; i < SimStep.ticks(1) && !struck; i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                struck |= events.type(e) == SimEvents.Type.MISSILE_DESTROYED && events.value(e) == Missiles.STRUCK;
            }
        }
        assertTrue(struck, "it burst on the Bulwark");
        assertEquals(armour - 10, bulwark.armour(), 1e-9, "`heavy` 10");
    }

    @Test
    void aColumnFromTheRearNeedsAHoverAtItsDepth() {
        EnemySpec diver = TestSpecs.SKITTER;
        assertThrows(
                IllegalArgumentException.class,
                () -> sortie(List.of(TestSpecs.wave(
                        1, WaveSpec.Formation.COLUMN, diver, 2, WaveSpec.Entry.REAR, WaveSpec.Edge.NONE))));
    }

    @Test
    void theHuntersReplayToTheSameHashAndDoNotAllocate() {
        Supplier<Sortie> make = () -> sortie(List.of(column(1, 4), column(14, 3)));
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(24); i++) {
            int command = i % 90 < 40 ? Command.FIRE.bit() | Command.LEFT.bit() : Command.RIGHT.bit();
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        long allocated = Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(24); i++) {
                sortie.step(i % 90 < 40 ? Command.FIRE.bit() | Command.LEFT.bit() : Command.RIGHT.bit());
            }
        });
        assertEquals(0, allocated);
    }
}
