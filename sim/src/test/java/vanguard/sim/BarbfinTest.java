package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M8 part D, the Barbfin (design/enemies/naval/barbfin; m8d-gaps.md §1.3): coming down its path it stops 90–200 px
 * above the ship (its own seeded draw, at the moment it stops; never higher than its hover depth's min below the top
 * edge, its path's end at the latest), holds its 9 s drifting 6 px/s sideways with its facing turned toward the ship,
 * fires its needles from the stop and leaves down and out to its side. A plain hover is unchanged. Deterministic.
 */
class BarbfinTest {
    /** 36×28 / 26×20, 24 HP (L24's beam era), 120 px/s, hover 9 s 90–200 px above the ship drifting 6 px/s. */
    static EnemySpec barbfin(EnemyGun gun) {
        return new EnemySpec(
                "barbfin",
                24,
                new Hitbox(26, 20),
                Layer.AIR,
                10,
                true,
                12,
                120,
                Optional.empty(),
                Optional.empty(),
                Optional.of(new EnemySpec.Hover(new Range(9, 9), new Range(60, 460)).aboveShip(new Range(90, 200), 6)),
                Optional.of(new EnemySpec.Orbit(60, 90)),
                Optional.of(gun),
                Optional.empty(),
                Optional.empty(),
                false);
    }

    static final EnemyGun NEEDLE = EnemyGun.aimed(2.4, 0.9, 1, 150, 4, false);
    static final EnemySpec BARBFIN = barbfin(NEEDLE);

    static WaveSpec column(double t, EnemySpec enemy, int count) {
        return TestSpecs.wave(t, WaveSpec.Formation.COLUMN, enemy, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
    }

    static Sortie sortie(List<WaveSpec> waves) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                TestSpecs.level(40, waves),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Per serial, the step it stopped (entered its hold) and its height above the ship then. */
    private record Stops(Map<Integer, Integer> step, Map<Integer, Double> above, Map<Integer, Double> x) {}

    private static Stops fly(Sortie sortie, int steps, int commands) {
        Map<Integer, Integer> step = new HashMap<>();
        Map<Integer, Double> above = new HashMap<>();
        Map<Integer, Double> x = new HashMap<>();
        Map<Integer, Double> lastY = new HashMap<>();
        for (int tick = 0; tick < steps; tick++) {
            sortie.step(commands);
            for (Enemy unit : AbyssRayTest.all(sortie, "barbfin")) {
                Double was = lastY.put(unit.serial(), unit.y());
                if (was != null && was == unit.y() && !step.containsKey(unit.serial())) {
                    // Stopped in the step before.
                    step.put(unit.serial(), tick - 1);
                    above.put(unit.serial(), unit.y() - sortie.ship().y());
                    x.put(unit.serial(), unit.x());
                }
            }
        }
        return new Stops(step, above, x);
    }

    @Test
    void comingDownItStopsItsOwnDrawnHeightAboveTheShip() {
        Sortie sortie = sortie(List.of(column(1, BARBFIN, 5)));
        Stops stops = fly(sortie, SimStep.ticks(9), Command.NONE);
        assertEquals(5, stops.above().size(), "all five stopped");
        Set<Long> heights = new HashSet<>();
        for (double above : stops.above().values()) {
            assertTrue(above >= 90 - 120 * SimStep.SECONDS && above <= 200, "90-200 px above the ship: " + above);
            heights.add(Math.round(above));
        }
        assertTrue(heights.size() >= 4, "each its own draw: " + heights);
    }

    @Test
    void itNeverStopsHigherThanItsDepthsMinNorPastItsPathsEnd() {
        // The ship high up the screen: its height above the ship would be off the top; it stops at the depth's min.
        Sortie high = sortie(List.of(column(2.5, BARBFIN, 1)));
        for (int i = 0; i < SimStep.ticks(2); i++) {
            high.step(Command.UP.bit());
        }
        Stops stops = fly(high, SimStep.ticks(5), Command.NONE);
        assertEquals(1, stops.above().size());
        Enemy unit = AbyssRayTest.find(high, "barbfin");
        assertTrue(
                high.ship().y() + 90 > PlayField.HEIGHT - 60,
                "the ship high: " + high.ship().y());
        assertEquals(PlayField.HEIGHT - 60, unit.y(), 120 * SimStep.SECONDS + 1e-9, "at the depth's min");

        // A plain hover stops at its path's end as before (no height above the ship).
        EnemySpec plain = new EnemySpec(
                "barbfin",
                24,
                new Hitbox(26, 20),
                Layer.AIR,
                10,
                true,
                12,
                120,
                Optional.empty(),
                Optional.empty(),
                Optional.of(new EnemySpec.Hover(new Range(9, 9), new Range(60, 460))),
                Optional.of(new EnemySpec.Orbit(60, 90)),
                Optional.of(NEEDLE),
                Optional.empty(),
                Optional.empty(),
                false);
        Sortie path = sortie(List.of(column(1, plain, 1)));
        Stops end = fly(path, SimStep.ticks(9), Command.NONE);
        assertEquals(1, end.above().size());
        assertEquals(PlayField.HEIGHT - 260, AbyssRayTest.find(path, "barbfin").y(), 1e-9, "its formation's stop");
    }

    @Test
    void holdingItDriftsSixPixelsASecondAndFacesTheShipThenLeavesToItsSide() {
        Sortie sortie = sortie(List.of(column(1, BARBFIN, 1)));
        Stops stops = fly(sortie, SimStep.ticks(6), Command.NONE);
        assertEquals(1, stops.step().size());
        Enemy unit = AbyssRayTest.find(sortie, "barbfin");
        double y = unit.y();
        double before = unit.x();
        int shots = 0;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(i < SimStep.ticks(1) ? Command.LEFT.bit() : Command.NONE);
            assertEquals(y, unit.y(), 1e-9, "it holds its height");
            assertEquals(6 * SimStep.SECONDS, Math.abs(unit.x() - before), 1e-9, "6 px/s sideways");
            before = unit.x();
            shots += sortie.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        assertTrue(shots > 0, "its needles from the stop");
        double toShip =
                Enemy.heading(sortie.ship().x() - unit.x(), sortie.ship().y() - unit.y());
        assertEquals(0, Math.IEEEremainder(unit.facing() - toShip, 2 * Math.PI), 1e-6, "it faces the ship");
        // Its 9 s over, it leaves down and out to its side.
        double side = Math.signum(unit.x() - PlayField.WIDTH / 2.0);
        while (unit.y() == y) {
            sortie.step(Command.NONE);
        }
        double dx = unit.x() - before;
        assertTrue(side == 0 ? dx == 0 : Math.signum(dx) == side, "out to its side: " + dx);
        assertTrue(unit.y() < y, "and down");
    }

    @Test
    void itTurnsBackAtTheSideEdge() {
        HoverHold hold = new HoverHold();
        EnemySpec spec = BARBFIN;
        Enemy unit = new Enemy();
        unit.spawn(
                new Spawn(
                        0,
                        0,
                        spec,
                        FlightPath.through(14, 300, 14, 200),
                        120,
                        9,
                        Optional.empty(),
                        Spawn.Exit.DOWN,
                        false,
                        Optional.empty()),
                0);
        hold.start(spec, 0);
        unit.x = 14;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            hold.hold(unit, 240, 96);
            assertTrue(unit.x >= 13 - 1e-9 && unit.x <= 14 + 6 * 4 + 1e-6, "on the field: " + unit.x);
        }
        assertTrue(unit.x > 14, "it turned back from the left edge");
    }

    @Test
    void aHoverAboveTheShipNeedsAHeightOfZeroOrMoreAndADrift() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Hover(new Range(9, 9), new Range(60, 460))
                .aboveShip(new Range(-1, 200), 6));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Hover(new Range(9, 9), new Range(60, 460))
                .aboveShip(new Range(90, 200), -6));
        assertFalse(new EnemySpec.Hover(new Range(9, 9), new Range(60, 460))
                .aboveShip()
                .isPresent());
    }

    @Test
    void theSameSeedFliesTheSameBarbfins() {
        Sortie a = sortie(List.of(column(1, BARBFIN, 5), column(4, BARBFIN, 3)));
        Sortie b = sortie(List.of(column(1, BARBFIN, 5), column(4, BARBFIN, 3)));
        for (int step = 0; step < SimStep.ticks(16); step++) {
            int command = Command.FIRE.bit() | (step % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit());
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
    }
}
