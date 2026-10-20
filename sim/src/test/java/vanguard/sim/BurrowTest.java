package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part B, step B1: the Burrower's {@code burrow} (design/enemies/ground/burrower; user decision Q1 =
 * a, the spec's numbers in screen space). A {@code field} wave's unit appears buried and scrolls in with
 * the ground, then swims fixed on the screen to a spot ahead of the ship, bulges for 1 s, is up on
 * {@code ground} for 2 s firing its ring 0.4 s in (hard: a second ring at 1.4 s), dives over 0.4 s
 * (buried from its middle) and after three surfacings swims off the bottom edge: an escape. A wave
 * staggers its bulges 0.8 s apart with at most two up at once; no-surface zones move its spot or skip
 * the surfacing; the radio's {@code first-surfacing} starts on the attempt's first surfacing.
 */
class BurrowTest {
    static final EnemySpec.BurrowRing RING = new EnemySpec.BurrowRing(10, 150, 4, 0.4);
    static final EnemySpec.BurrowRing SECOND_RING = new EnemySpec.BurrowRing(8, 150, 4, 1.4);
    static final EnemySpec.SurfaceSpot SPOT = new EnemySpec.SurfaceSpot(140, 240, 110, 40, 110);

    /** The spec's burrow at medium (hard: {@code second} ring, a 1.8 s window). */
    static EnemySpec.Burrow burrow(double surfaced, Optional<EnemySpec.BurrowRing> second) {
        return new EnemySpec.Burrow(90, SPOT, new Range(1.0, 1.8), 1.0, surfaced, 0.4, 3, RING, second, 0.8, 2, 30);
    }

    /** A Burrower of {@code hp} at medium, its burrow {@code spec}. */
    static EnemySpec burrower(double hp, EnemySpec.Burrow spec) {
        return PartASpecs.dummy(Layer.GROUND, hp, new Hitbox(44, 44), false).withBurrow(Optional.of(spec));
    }

    static EnemySpec burrower() {
        return burrower(44, burrow(2.0, Optional.empty()));
    }

    /** A field wave of {@code count} Burrowers at {@code t}, scattered over 300×200 px at 60 px. */
    static WaveSpec field(double t, EnemySpec enemy, int count) {
        return DriftjellyTest.field(t, enemy, count, new WaveSpec.Field(240, 300, 200, 60, 0));
    }

    /** One section of {@code seconds} at 150 px/s over land, with the waves and the radio cues. */
    static LevelScript level(double seconds, List<RadioCue> radio, WaveSpec... waves) {
        return LevelScript.builder(15, 3)
                .sections(List.of(new Section(seconds, 150)))
                .waves(List.of(waves))
                .radio(radio)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    static Sortie sortie(long seed, LevelScript level) {
        return new Sortie(
                seed, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** What one unit did, step by step: the steps of its events and its layer every step. */
    private record Trace(
            List<Integer> bulges, List<Integer> surfaced, List<Integer> dives, List<Integer> rings, int escaped) {}

    private static Trace trace(Sortie sortie, int steps) {
        List<Integer> bulges = new ArrayList<>();
        List<Integer> surfaced = new ArrayList<>();
        List<Integer> dives = new ArrayList<>();
        List<Integer> rings = new ArrayList<>();
        int escaped = -1;
        for (int i = 1; i <= steps; i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case BURROW_BULGE -> bulges.add(i);
                    case BURROW_SURFACED -> surfaced.add(i);
                    case BURROW_DIVE -> dives.add(i);
                    case ENEMY_FIRED -> rings.add(i);
                    case BURROW_ESCAPED -> escaped = i;
                    default -> {}
                }
            }
        }
        return new Trace(bulges, surfaced, dives, rings, escaped);
    }

    @Test
    void aBurrowIsAGroundUnitsWithoutAGunWhoseRingsFireWhileItIsUp() {
        EnemySpec plain = PartASpecs.dummy(Layer.AIR, 44, new Hitbox(44, 44), false);
        assertThrows(IllegalArgumentException.class, () -> plain.withBurrow(Optional.of(burrow(2, Optional.empty()))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpec.Burrow(
                        90,
                        SPOT,
                        new Range(1, 1.8),
                        1,
                        2,
                        0.4,
                        3,
                        new EnemySpec.BurrowRing(10, 150, 4, 2),
                        Optional.empty(),
                        0.8,
                        2,
                        30));
    }

    @Test
    void itEntersBuriedWithTheGroundThenSwimsFixedOnTheScreenToASpotAheadOfTheShip() {
        Sortie sortie = sortie(3, level(40, List.of(), field(0, burrower(), 1)));
        sortie.step(Command.NONE);
        Enemy unit = sortie.enemy(0);
        assertEquals(Enemy.BurrowPhase.ENTER, unit.burrowPhase());
        assertEquals(Layer.BURIED, unit.layer());
        assertTrue(unit.y() > PlayField.HEIGHT, "it appears above the top edge: " + unit.y());
        double y = unit.y();
        while (unit.burrowPhase() == Enemy.BurrowPhase.ENTER) {
            sortie.step(Command.NONE);
            assertEquals(Layer.BURIED, unit.layer());
            if (unit.burrowPhase() == Enemy.BurrowPhase.ENTER) {
                assertEquals(y - 150 * SimStep.SECONDS, unit.y(), 1e-9, "with the ground");
                y = unit.y();
            }
        }
        assertEquals(Enemy.BurrowPhase.TRAVEL, unit.burrowPhase());
        assertTrue(unit.y() <= Ship.START_Y + 240, "within reach of the spot band: " + unit.y());
        double spotX = unit.burrowSpotX();
        double spotY = unit.burrowSpotY();
        assertTrue(spotY >= Ship.START_Y + 140 && spotY <= Ship.START_Y + 240, "140-240 px ahead: " + spotY);
        assertTrue(Math.abs(spotX - Ship.START_X) <= 110, "within 110 px across: " + spotX);
        assertTrue(Math.hypot(spotX - Ship.START_X, spotY - Ship.START_Y) >= 110);
        double x = unit.x();
        y = unit.y();
        while (unit.burrowPhase() == Enemy.BurrowPhase.TRAVEL) {
            sortie.step(Command.NONE);
            double moved = Math.hypot(unit.x() - x, unit.y() - y);
            assertTrue(moved <= 90 * SimStep.SECONDS + 1e-9, "it swims at 90 px/s on the screen: " + moved);
            x = unit.x();
            y = unit.y();
        }
        assertEquals(spotX, unit.x(), 1e-9, "at its spot");
        assertEquals(spotY, unit.y(), 1e-9);
    }

    @Test
    void itBulgesOneSecondIsUpTwoFiresItsRingAtPointFourAndDivesBuriedFromTheDivesMiddle() {
        Sortie sortie = sortie(5, level(60, List.of(), field(0, burrower(), 1)));
        int bulge = -1;
        int surfaced = -1;
        int dive = -1;
        int ring = -1;
        int bulletsBefore = 0;
        int ringBullets = -1;
        double atX = Double.NaN;
        double atY = Double.NaN;
        for (int i = 1; i <= SimStep.ticks(20) && dive < 0; i++) {
            bulletsBefore = sortie.bulletCount();
            sortie.step(Command.NONE);
            Enemy unit = sortie.enemy(0);
            SimEvents events = sortie.events();
            if (events.count(SimEvents.Type.BURROW_BULGE) > 0) {
                bulge = i;
                atX = unit.x();
                atY = unit.y();
            }
            if (events.count(SimEvents.Type.BURROW_SURFACED) > 0) {
                surfaced = i;
            }
            if (events.count(SimEvents.Type.BURROW_DIVE) > 0) {
                dive = i;
            }
            if (events.count(SimEvents.Type.ENEMY_FIRED) > 0) {
                ring = i;
                ringBullets = sortie.bulletCount() - bulletsBefore;
            }
            if (bulge > 0) {
                assertEquals(atX, unit.x(), 1e-9, "fixed on the screen while it bulges and is up");
                assertEquals(atY, unit.y(), 1e-9);
                boolean up = surfaced > 0;
                assertEquals(up ? Layer.GROUND : Layer.BURIED, unit.layer(), "step " + i);
            }
        }
        assertTrue(bulge > 0);
        assertEquals(SimStep.ticks(1.0), surfaced - bulge, "the 1.0 s tell");
        assertEquals(SimStep.ticks(2.0), dive - surfaced, "2.0 s up");
        assertEquals(SimStep.ticks(0.4), ring - surfaced, "the ring 0.4 s after it surfaced");
        assertEquals(10, ringBullets, "a ring of 10");
        Enemy unit = sortie.enemy(0);
        for (int k = 1; k < SimStep.ticks(0.4) / 2; k++) {
            sortie.step(Command.NONE);
            assertEquals(Layer.GROUND, unit.layer(), "up until the dive's middle");
        }
        sortie.step(Command.NONE);
        assertEquals(Layer.BURIED, unit.layer(), "buried from the dive's middle");
    }

    @Test
    void afterThreeSurfacingsItSwimsOffTheBottomEdgeAsAnEscapeWithoutABounty() {
        Sortie sortie = sortie(7, level(60, List.of(), field(0, burrower(), 1)));
        Trace trace = trace(sortie, SimStep.ticks(40));
        assertEquals(3, trace.surfaced().size());
        assertEquals(3, trace.bulges().size());
        assertTrue(trace.escaped() > trace.dives().get(2), "gone after its third dive");
        assertEquals(0, sortie.enemyCount());
        assertEquals(0, sortie.kills());
        for (int s = 1; s < 3; s++) {
            int cycle = trace.surfaced().get(s) - trace.surfaced().get(s - 1);
            // Bulge 1 + up 2 + dive 0.4 + a swim of 1.0-1.8 s (on until it arrives).
            assertTrue(cycle >= SimStep.ticks(4.4), "a cycle of about 4.5 s: " + cycle);
            assertTrue(cycle <= SimStep.ticks(7), "a cycle of about 4.5 s: " + cycle);
        }
    }

    @Test
    void hardAddsASecondRingAtOnePointFour() {
        EnemySpec hard = burrower(57, burrow(1.8, Optional.of(SECOND_RING)));
        Sortie sortie = sortie(5, level(60, List.of(), field(0, hard, 1)));
        Trace trace = trace(sortie, SimStep.ticks(12));
        int surfaced = trace.surfaced().getFirst();
        assertEquals(surfaced + SimStep.ticks(0.4), trace.rings().get(0));
        assertEquals(surfaced + SimStep.ticks(1.4), trace.rings().get(1));
        assertEquals(surfaced + SimStep.ticks(1.8), trace.dives().getFirst(), "1.8 s up");
    }

    @Test
    void aWaveStaggersItsBulgesPointEightApartWithAtMostTwoUp() {
        Sortie sortie = sortie(9, level(80, List.of(), field(0, burrower(), 6)));
        List<Integer> bulges = new ArrayList<>();
        int mostUp = 0;
        for (int i = 1; i <= SimStep.ticks(60); i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.BURROW_BULGE) > 0) {
                assertEquals(1, sortie.events().count(SimEvents.Type.BURROW_BULGE), "one at a time");
                bulges.add(i);
            }
            int up = 0;
            for (int j = 0; j < sortie.enemyCount(); j++) {
                up += sortie.enemy(j).layer() == Layer.GROUND ? 1 : 0;
            }
            mostUp = Math.max(mostUp, up);
        }
        assertEquals(18, bulges.size(), "six units, three surfacings each");
        for (int b = 1; b < bulges.size(); b++) {
            assertTrue(bulges.get(b) - bulges.get(b - 1) >= SimStep.ticks(0.8), "0.8 s apart");
        }
        assertEquals(2, mostUp, "at most two up at once");
        assertEquals(0, sortie.enemyCount(), "all gone");
    }

    @Test
    void theSameSeedReplaysTheSameCycleAndAnotherSeedPicksOtherSpots() {
        double[] a = spots(11);
        double[] b = spots(11);
        double[] c = spots(12);
        assertEquals(a[0], b[0]);
        assertEquals(a[1], b[1]);
        assertFalse(a[0] == c[0] && a[1] == c[1]);
    }

    private static double[] spots(long seed) {
        Sortie sortie = sortie(seed, level(40, List.of(), field(0, burrower(), 1)));
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.BURROW_BULGE) > 0) {
                return new double[] {sortie.enemy(0).x(), sortie.enemy(0).y()};
            }
        }
        throw new AssertionError("no bulge");
    }

    @Test
    void itIsKilledOnlyWhileItIsUpAndPaysAsAnyGroundKill() {
        Sortie sortie = sortie(5, level(60, List.of(), field(0, burrower(4, burrow(2.0, Optional.empty())), 1)));
        boolean hitBuried = false;
        for (int i = 0; i < SimStep.ticks(30) && sortie.kills() == 0; i++) {
            int commands = Command.FIRE.bit();
            if (sortie.enemyCount() > 0) {
                double dx = sortie.enemy(0).x() - sortie.ship().x();
                commands |= dx > 3 ? Command.RIGHT.bit() : dx < -3 ? Command.LEFT.bit() : 0;
            }
            Layer before = sortie.enemyCount() > 0 ? sortie.enemy(0).layer() : null;
            sortie.step(commands);
            hitBuried |= before == Layer.BURIED && sortie.events().count(SimEvents.Type.ENEMY_HIT) > 0;
        }
        assertEquals(1, sortie.kills(), "shot while up");
        assertFalse(hitBuried, "never hit while buried");
    }

    @Test
    void aSpotInANoSurfaceZoneMovesBesideItAndWithNoRoomTheSurfacingIsSkipped() {
        // The left half of the field is a zone for the whole level: every bulge lies right of it.
        LevelScript half = TestSpecs.withGround(
                level(60, List.of(), field(0, burrower(), 3)),
                g -> g.withNoSurface(List.of(new GroundSpec.NoSurface(0, 80, -100, 240))));
        Sortie sortie = sortie(13, half);
        int bulges = 0;
        for (int i = 0; i < SimStep.ticks(50); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.BURROW_BULGE) {
                    bulges++;
                    assertTrue(events.x(e) > 240, "outside the zone: " + events.x(e));
                }
            }
        }
        assertTrue(bulges > 0);

        // The whole field is a zone: no surfacing at all, each counted; it leaves after three.
        LevelScript all = TestSpecs.withGround(
                level(60, List.of(), field(0, burrower(), 1)),
                g -> g.withNoSurface(List.of(new GroundSpec.NoSurface(0, 80, -100, 600))));
        Trace trace = trace(sortie(13, all), SimStep.ticks(30));
        assertTrue(trace.bulges().isEmpty());
        assertTrue(trace.escaped() > 0, "three skipped surfacings, then away");
    }

    @Test
    void noSurfaceZonesAreNotForLevelsWithAnArena() {
        List<GroundSpec.NoSurface> zones = List.of(new GroundSpec.NoSurface(0, 10, 0, 100));
        LevelScript level = LevelScript.builder(15, 3)
                .sections(List.of(new Section(10, 150), new Section(20, 0, true)))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
        assertThrows(IllegalArgumentException.class, () -> TestSpecs.withGround(level, g -> g.withNoSurface(zones)));
    }

    @Test
    void theRadiosFirstSurfacingStartsOnceOnTheAttemptsFirstSurfacing() {
        LevelScript level =
                level(60, List.of(LevelClockSpecs.cue(CueTrigger.FIRST_SURFACING, 0, 0)), field(0, burrower(), 3));
        Sortie sortie = sortie(5, level);
        int lines = 0;
        int firstSurfacing = -1;
        int radioAt = -1;
        for (int i = 1; i <= SimStep.ticks(40); i++) {
            sortie.step(Command.NONE);
            if (firstSurfacing < 0 && sortie.events().count(SimEvents.Type.BURROW_SURFACED) > 0) {
                firstSurfacing = i;
            }
            if (LevelClockSpecs.radioed(sortie, 0)) {
                lines++;
                radioAt = i;
            }
        }
        assertEquals(1, lines, "once");
        assertEquals(firstSurfacing, radioAt);
    }

    @Test
    void aFieldOfBurrowersDoesNotAllocate() {
        LevelScript level = TestSpecs.withGround(
                level(60, List.of(), field(0, burrower(), 6)),
                g -> g.withNoSurface(List.of(new GroundSpec.NoSurface(2, 6, 0, 200))));
        long allocated = Allocations.least(() -> sortie(5, level), sortie -> {
            for (int i = 0; i < SimStep.ticks(40); i++) {
                sortie.step(Command.FIRE.bit());
            }
        });
        assertEquals(0, allocated);
    }
}
