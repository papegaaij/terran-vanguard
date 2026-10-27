package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.TestSpecs.INFINITE;
import static vanguard.sim.TestSpecs.muzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part C (design/campaign Level 16, What walls stop; M2 = a): shots stop at rock with a spark, after
 * the hit passes so a unit at the rock's foot is hit first (risk 2); enemy bullets stop at it; the beam
 * ends at it; bombs land at the wall's foot; pickups slide off it within 0.3 s; formations are laid out
 * on the flyable span at their entry row (risk 1), and a level without terrain plans as before.
 */
class RockStopsTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double SCROLL_SPEED = 130;

    private static LevelScript withRock(LevelScript level, Terrain terrain) {
        return TestSpecs.withGround(
                level, g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(terrain, 10, 28, 0.12, 0.5))));
    }

    /** A block island over x from–to and rows low–high. */
    private static Terrain block(double from, double to, double low, double high) {
        return new Terrain(
                Optional.empty(),
                Optional.empty(),
                List.of(new Terrain.Island(new double[] {from, to, to, from}, new double[] {low, low, high, high})));
    }

    private static int count(Sortie sortie, SimEvents.Type type, int value) {
        SimEvents events = sortie.events();
        int n = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type && events.value(i) == value) {
                n++;
            }
        }
        return n;
    }

    @Test
    void shotsStopAtRockWithASparkAndNeverPassIt() {
        // The block's foot reaches y 400 after half a second (ground scroll 65).
        double foot = 400 + SCROLL_SPEED / 2 - PlayField.HEIGHT;
        Sortie sortie = TestSpecs.sortie(withRock(TestSpecs.level(10, List.of()), block(200, 280, foot, foot + 2_000)));
        int sparks = 0;
        int fired = 0;
        for (int step = 0; step < 90; step++) {
            sortie.step(FIRE);
            sparks += count(sortie, SimEvents.Type.ROCK_SPARK, 0);
            fired += sortie.events().count(SimEvents.Type.SHOT_FIRED);
            double footY = Terrain.y(sortie.groundScroll(), foot);
            for (int i = 0; i < sortie.shotCount(); i++) {
                assertTrue(sortie.shot(i).y() - 6 < footY, "no shot inside the rock after a step");
            }
        }
        assertTrue(fired > 5);
        assertTrue(sparks >= fired - 2, "every shot that got there sparked: " + sparks + " of " + fired);
    }

    @Test
    void aUnitAtTheRocksFootIsHitBeforeTheRockStopsTheShot() {
        // A flat ground unit (8 px tall: a shot moves 17 px a step against it, so it cannot skip it) whose
        // top touches the block's foot: a shot mostly overlaps both in the step it arrives.
        EnemySpec flat = PartASpecs.dummy(Layer.GROUND, 1e6, new Hitbox(40, 8), false);
        LevelScript level = PartASpecs.units(PartASpecs.unit(0, Ship.START_X, flat));
        // Its row, measured without rock: the ground scroll plus its height, less the field's.
        Sortie probe =
                PartASpecs.sortie(level, new Armament.Mount(Armament.Slot.FRONT, TestSpecs.PULSE, TestSpecs.PULSE_L2));
        for (int step = 0; step < 30; step++) {
            probe.step(Command.NONE);
        }
        Enemy unit = probe.enemy(0);
        double row = Terrain.row(probe.groundScroll(), unit.y());
        probe.step(Command.NONE);
        assertEquals(row, Terrain.row(probe.groundScroll(), probe.enemy(0).y()), 1e-9, "it scrolls with the ground");
        Sortie sortie = PartASpecs.sortie(
                withRock(level, block(200, 280, row + 4, row + 2_000)),
                new Armament.Mount(Armament.Slot.FRONT, TestSpecs.PULSE, TestSpecs.PULSE_L2));
        int hits = 0;
        int sparks = 0;
        int fired = 0;
        for (int step = 0; step < 120; step++) {
            sortie.step(FIRE);
            hits += sortie.events().count(SimEvents.Type.ENEMY_HIT);
            sparks += sortie.events().count(SimEvents.Type.ROCK_SPARK);
            fired += sortie.events().count(SimEvents.Type.SHOT_FIRED);
        }
        assertTrue(fired > 10);
        assertEquals(0, sparks, "the unit takes every shot");
        assertTrue(hits >= fired - 2, hits + " of " + fired);
    }

    @Test
    void enemyBulletsStopAtRockWithASpark() {
        Sortie sortie = TestSpecs.sortie(withRock(TestSpecs.level(10, List.of()), WallContactTest.CANYON));
        sortie.step(Command.NONE);
        sortie.fireBullet(300, 300, 0, 300, 4);
        int sparks = 0;
        for (int step = 0; step < 30 && sortie.bulletCount() > 0; step++) {
            sortie.step(Command.NONE);
            sparks += count(sortie, SimEvents.Type.ROCK_SPARK, -1);
        }
        assertEquals(0, sortie.bulletCount(), "stopped at the right wall (x 330)");
        assertEquals(1, sparks);
    }

    @Test
    void theBeamEndsAtTheFirstRockInItsColumn() {
        double foot = 400 + SCROLL_SPEED / 2 - PlayField.HEIGHT;
        Sortie sortie = PartASpecs.sortie(
                withRock(TestSpecs.level(10, List.of()), block(200, 280, foot, foot + 2_000)), PartASpecs.beamMount(1));
        for (int step = 0; step < 30; step++) {
            sortie.step(FIRE);
        }
        Beam beam = sortie.beam(0);
        assertSame(Beam.Contact.ROCK, beam.contact());
        assertEquals(Terrain.y(sortie.groundScroll(), foot), beam.endY(), 1e-9);
        // Without rock it runs to its reach.
        Sortie open = PartASpecs.sortie(TestSpecs.level(10, List.of()), PartASpecs.beamMount(1));
        for (int step = 0; step < 30; step++) {
            open.step(FIRE);
        }
        assertSame(Beam.Contact.NONE, open.beam(0).contact());
    }

    @Test
    void aBombAimedOverRockLandsAtTheWallsFoot() {
        WeaponSpec bomb = new WeaponSpec(
                "bomb-rack",
                "bomb",
                "bomb",
                WeaponSpec.Delivery.DROPPED,
                true,
                1.2,
                10,
                0,
                new Hitbox(8, 12),
                0,
                INFINITE,
                1,
                20,
                0,
                Math.PI,
                0.5,
                0,
                List.of(muzzle(-80, -3, 0)));
        Terrain wall = new Terrain(
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {200, 200})),
                Optional.empty(),
                List.of());
        Sortie sortie = PartASpecs.sortie(
                withRock(TestSpecs.level(10, List.of()), wall), new Armament.Mount(Armament.Slot.FRONT, bomb, bomb));
        boolean burst = false;
        for (int step = 0; step < 60 && !burst; step++) {
            sortie.step(FIRE);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.BLAST) {
                    assertEquals(200, events.x(i), 1e-9, "at the wall's foot, not 160 in the rock");
                    burst = true;
                }
            }
        }
        assertTrue(burst);
    }

    @Test
    void aPickupOnRockSlidesOffWithinAThirdOfASecond() {
        EnemySpec dropper = EnemySpec.of("dropper", 1, new Hitbox(20, 20), Layer.GROUND, 0, false, 0, 0)
                .withDrop(Optional.of(new EnemySpec.Drop(PickupType.SHIELD_CELL, 1)))
                .withTerrain(true);
        Terrain wall = new Terrain(
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {150, 150})),
                Optional.empty(),
                List.of());
        Sortie sortie = TestSpecs.sortie(withRock(PartASpecs.units(PartASpecs.unit(0, 100, dropper)), wall));
        for (int step = 0; step < 60; step++) {
            sortie.step(Command.NONE);
        }
        sortie.destroyEnemy(0);
        assertEquals(1, sortie.pickupCount());
        assertTrue(sortie.pickup(0).x() < 150, "dropped on the rock");
        for (int step = 0; step < SimStep.ticks(0.3); step++) {
            sortie.step(Command.NONE);
        }
        assertTrue(
                sortie.pickup(0).x() >= 150 + Sortie.PICKUP_ROCK_MARGIN - 1e-9,
                "off it: " + sortie.pickup(0).x());
    }

    @Test
    void formationsAreLaidOutOnTheFlyableSpanAndUnchangedWithoutOne() {
        WaveSpec vee = TestSpecs.wave(
                1, WaveSpec.Formation.V_WING, TestSpecs.SKITTER, 5, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        List<Spawn> plain = new ArrayList<>();
        List<Spawn> none = new ArrayList<>();
        List<Spawn> whole = new ArrayList<>();
        List<Spawn> mapped = new ArrayList<>();
        Formations.plan(vee, 0, new SplitMix64(7), plain);
        Formations.plan(vee, 0, new SplitMix64(7), none, Optional.empty());
        Formations.plan(vee, 0, new SplitMix64(7), whole, Optional.of(new Formations.Span(0, PlayField.WIDTH)));
        Formations.plan(vee, 0, new SplitMix64(7), mapped, Optional.of(new Formations.Span(150, 330)));
        assertSamePlan(plain, none);
        assertSamePlan(plain, whole);
        assertEquals(plain.size(), mapped.size());
        for (int i = 0; i < plain.size(); i++) {
            FlightPath from = plain.get(i).path();
            FlightPath to = mapped.get(i).path();
            assertEquals(150 + from.startX() * 180 / PlayField.WIDTH, to.startX(), 1e-9);
            assertEquals(150 + from.endX() * 180 / PlayField.WIDTH, to.endX(), 1e-9);
            assertEquals(from.startY(), to.startY(), 1e-9);
            if (from.startX() >= 0 && from.startX() <= PlayField.WIDTH) {
                assertTrue(to.startX() >= 150 && to.startX() <= 330);
            }
        }
        assertFalse(
                Formations.Span.maps(new WaveSpec(
                        1,
                        WaveSpec.Formation.V_WING,
                        TestSpecs.SKITTER,
                        5,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE,
                        Optional.empty(),
                        Optional.empty(),
                        1,
                        Optional.empty(),
                        Optional.empty(),
                        List.of(),
                        Optional.of(new WaveSpec.At(100, 400)),
                        List.of(),
                        Optional.empty(),
                        "",
                        Optional.empty(),
                        false)),
                "an authored point is the author's");
    }

    private static void assertSamePlan(List<Spawn> expected, List<Spawn> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Spawn a = expected.get(i);
            Spawn b = actual.get(i);
            assertEquals(a.tick(), b.tick());
            assertEquals(a.path().startX(), b.path().startX());
            assertEquals(a.path().endX(), b.path().endX());
            assertEquals(a.path().length(), b.path().length());
            assertEquals(a.orbit(), b.orbit());
        }
    }

    @Test
    void aSortieLaysAWaveOutOnTheSpanAtItsEntryRow() {
        WaveSpec vee = TestSpecs.wave(
                1, WaveSpec.Formation.V_WING, TestSpecs.SKITTER, 5, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        Sortie sortie = TestSpecs.sortie(withRock(TestSpecs.level(10, List.of(vee)), WallContactTest.CANYON));
        Sortie open = TestSpecs.sortie(TestSpecs.level(10, List.of(vee)));
        for (int step = 0; step < SimStep.ticks(1.5); step++) {
            sortie.step(Command.NONE);
            open.step(Command.NONE);
        }
        assertTrue(sortie.enemyCount() > 0);
        double wideOpen = 0;
        double wideCanyon = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy unit = sortie.enemy(i);
            assertTrue(unit.x() > 150 && unit.x() < 330, "inside the canyon: " + unit.x());
            wideCanyon = Math.max(wideCanyon, Math.abs(unit.x() - 240));
        }
        for (int i = 0; i < open.enemyCount(); i++) {
            wideOpen = Math.max(wideOpen, Math.abs(open.enemy(i).x() - 240));
        }
        assertTrue(wideCanyon < wideOpen, "narrower than on the open field");
    }
}
