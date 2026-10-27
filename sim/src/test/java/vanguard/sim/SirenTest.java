package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M8 part E, the Siren (design/enemies/naval/siren; design/campaign Level 25, Siren nests; m8e-gaps.md §1.2, §5 E1–E4):
 * its nests stand on the walls' feet and scroll with the ground, a stagger apart and taking turns from the right wall,
 * or opposite at one row with the left one's song a stagger behind; the sac's tell begins 0.4 s after a nest enters
 * and its ring leaves at 1.2 s, then every 4.5 s; the ring (120 px/s to 230 px, band 10, no damage) slows the ship and
 * Rook to 60 % of their speed and acceleration for 2 s, then 3 s of immunity (easy 1.5 s; hard 2 s of immunity); rock
 * on its line stops it, a dash passes it, shooting the sac ends it. Deterministic and allocation-free.
 */
class SirenTest {
    /** The Siren's ring at medium: first at 1.2 s, every 4.5 s, tell 0.8 s, 120 px/s to 230 px, band 10, 60 % for 2 s, 3 s. */
    static final EnemySpec.SlowRing RING = new EnemySpec.SlowRing(1.2, 4.5, 0.8, 120, 230, 10, 0.6, 2, 3);

    static final EnemySpec.SlowRing EASY = new EnemySpec.SlowRing(1.2, 6, 0.8, 120, 230, 10, 0.6, 1.5, 3);
    static final EnemySpec.SlowRing HARD = new EnemySpec.SlowRing(1.2, 3.6, 0.8, 120, 230, 10, 0.6, 2, 2);

    /** 64×56 / 48×40, {@code hp}, fixed on its wall, bounty 28. */
    static EnemySpec siren(double hp, EnemySpec.SlowRing ring) {
        return EnemySpec.of("siren", hp, new Hitbox(48, 40), Layer.AIR, 10, true, 28, 0)
                .withSlowRing(Optional.of(ring));
    }

    static final EnemySpec SIREN = siren(90, RING);

    /** Straight walls at 110 and 370: a 260 px trench. */
    static final Terrain WALLS = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {110, 110})),
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {370, 370})),
            List.of());

    /** The trench split by a rock pillar from x 320 to 340 the whole way. */
    static final Terrain PILLAR = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {110, 110})),
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {370, 370})),
            List.of(new Terrain.Island(new double[] {320, 340, 340, 320}, new double[] {-900, -900, 99_000, 99_000})));

    private static final int RING_STEPS = SimStep.ticks(1.2);
    private static final int TELL_STEPS = SimStep.ticks(0.8);
    private static final int STAGGER_STEPS = SimStep.ticks(WallMounts.STAGGER_SECONDS);

    static WaveSpec nests(double t, EnemySpec siren, int count, WaveSpec.Edge edge, boolean opposite) {
        return TestSpecs.wave(t, WaveSpec.Formation.SINGLE, siren, count, WaveSpec.Entry.FRONT, edge)
                .withWall(opposite);
    }

    static LevelScript trench(Terrain terrain, List<WaveSpec> waves) {
        return CaveEntryTest.trench(terrain, waves);
    }

    private static Sortie sortie(Terrain terrain, List<WaveSpec> waves, Loadout loadout) {
        return new Sortie(
                1, loadout, trench(terrain, waves), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static Sortie sortie(List<WaveSpec> waves) {
        return sortie(WALLS, waves, TestSpecs.LOADOUT);
    }

    /** A nest as first seen: the step, its place, its serial. */
    private record Seen(int step, double x, double y, int serial) {}

    /** Per step the events of {@code type} at their x. */
    private record At(int step, double x, int value) {}

    private static List<At> events(Sortie sortie, SimEvents.Type type, int step, List<At> into) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                into.add(new At(step, events.x(i), events.value(i)));
            }
        }
        return into;
    }

    /** The commands that steer the ship toward (x, y), nothing within 3 px. */
    private static int toward(Sortie sortie, double x, double y) {
        Ship ship = sortie.ship();
        int commands = 0;
        if (ship.x() < x - 3) {
            commands |= Command.RIGHT.bit();
        } else if (ship.x() > x + 3) {
            commands |= Command.LEFT.bit();
        }
        if (ship.y() < y - 3) {
            commands |= Command.UP.bit();
        } else if (ship.y() > y + 3) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }

    @Test
    void staggeredNestsEnterABeatApartOnTheirWallsFeetRightFirstAndSingOnTheirClock() {
        Sortie sortie = sortie(List.of(nests(2, SIREN, 3, WaveSpec.Edge.NONE, false)));
        List<Seen> seen = new ArrayList<>();
        List<At> tells = new ArrayList<>();
        List<At> rings = new ArrayList<>();
        double[] tellAtRing = new double[1];
        for (int step = 0; step < SimStep.ticks(10); step++) {
            sortie.step(Command.NONE);
            for (Enemy unit : AbyssRayTest.all(sortie, "siren")) {
                if (seen.stream().noneMatch(s -> s.serial() == unit.serial())) {
                    seen.add(new Seen(step, unit.x(), unit.y() + sortie.groundScroll(), unit.serial()));
                    assertTrue(unit.wallMounted(), "a wall's nest");
                    assertTrue(unit.y() > 555 && unit.y() < 560, "it enters at the top edge: " + unit.y());
                }
                Seen first = seen.stream()
                        .filter(s -> s.serial() == unit.serial())
                        .findFirst()
                        .orElseThrow();
                assertEquals(first.y(), unit.y() + sortie.groundScroll(), 1e-6, "it scrolls with the ground");
                assertEquals(first.x(), unit.x(), "it stays on its wall's foot");
                if (step == first.step() + RING_STEPS - 2) {
                    tellAtRing[0] = Math.max(tellAtRing[0], unit.ringTell(1));
                }
                if (step < first.step() + RING_STEPS - TELL_STEPS - 1) {
                    assertEquals(0, unit.ringTell(1), "no tell before its time");
                }
            }
            events(sortie, SimEvents.Type.RING_TELL, step, tells);
            events(sortie, SimEvents.Type.RING, step, rings);
        }
        seen.sort(Comparator.comparingInt(Seen::step));
        assertEquals(3, seen.size());
        assertEquals(370 - WallMounts.FOOT_INSET, seen.get(0).x(), "the right wall first");
        assertEquals(110 + WallMounts.FOOT_INSET, seen.get(1).x(), "then the left");
        assertEquals(370 - WallMounts.FOOT_INSET, seen.get(2).x());
        for (int k = 0; k < 3; k++) {
            Seen nest = seen.get(k);
            assertEquals(SimStep.ticks(2) + k * STAGGER_STEPS, nest.step() + 1, 1, "nest " + k + " a stagger apart");
            assertEquals(
                    1,
                    tells.stream()
                            .filter(e -> e.x() == nest.x()
                                    && Math.abs(e.step() - (nest.step() + RING_STEPS - TELL_STEPS - 1)) <= 1)
                            .count(),
                    "nest " + k + "'s tell 0.4 s after it entered");
            assertEquals(
                    1,
                    rings.stream()
                            .filter(e -> e.x() == nest.x() && Math.abs(e.step() - (nest.step() + RING_STEPS - 1)) <= 1)
                            .count(),
                    "nest " + k + "'s ring 1.2 s after it entered");
        }
        assertTrue(tellAtRing[0] > 0.9, "the tell runs up to the ring: " + tellAtRing[0]);
        assertEquals(3, rings.size(), "each sings once on the screen at 130 px/s");
    }

    @Test
    void oppositeNestsShareARowTheLeftOnesSongAStaggerBehind() {
        Sortie sortie = sortie(List.of(nests(2, SIREN, 2, WaveSpec.Edge.NONE, true)));
        List<Seen> seen = new ArrayList<>();
        List<At> rings = new ArrayList<>();
        for (int step = 0; step < SimStep.ticks(7); step++) {
            sortie.step(Command.NONE);
            for (Enemy unit : AbyssRayTest.all(sortie, "siren")) {
                if (seen.stream().noneMatch(s -> s.serial() == unit.serial())) {
                    seen.add(new Seen(step, unit.x(), unit.y(), unit.serial()));
                }
            }
            events(sortie, SimEvents.Type.RING, step, rings);
        }
        assertEquals(2, seen.size());
        assertEquals(seen.get(0).step(), seen.get(1).step(), "one row");
        assertEquals(seen.get(0).y(), seen.get(1).y());
        assertEquals(2, rings.size());
        At right = rings.get(0);
        At left = rings.get(1);
        assertEquals(370 - WallMounts.FOOT_INSET, right.x(), "the right one sings first");
        assertEquals(110 + WallMounts.FOOT_INSET, left.x());
        assertEquals(STAGGER_STEPS, left.step() - right.step(), 1, "the left one a stagger later");
    }

    @Test
    void aNestOnTheScreenLongerSingsEveryInterval() {
        LevelScript slow = trench(WALLS, List.of(nests(1, SIREN, 1, WaveSpec.Edge.LEFT, false)));
        slow = slow.toBuilder().sections(List.of(new Section(40, 40))).build();
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT, slow, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        List<At> rings = new ArrayList<>();
        for (int step = 0; step < SimStep.ticks(14); step++) {
            sortie.step(Command.NONE);
            events(sortie, SimEvents.Type.RING, step, rings);
        }
        assertEquals(3, rings.size(), "at 1.2, 5.7 and 10.2 s after it entered");
        assertEquals(SimStep.ticks(4.5), rings.get(1).step() - rings.get(0).step());
        assertEquals(SimStep.ticks(4.5), rings.get(2).step() - rings.get(1).step());
    }

    /** Flies the ship to (x, y) and holds; returns the step a ring slowed it, -1 for none within {@code steps}. */
    private static int slowedAt(Sortie sortie, double x, double y, int steps, int[] dashAt) {
        for (int step = 0; step < steps; step++) {
            int commands = toward(sortie, x, y);
            if (dashAt != null && step == dashAt[0]) {
                commands = Command.DASH.bit() | Command.RIGHT.bit() | Command.UP.bit();
            }
            sortie.step(commands);
            for (At slowed : events(sortie, SimEvents.Type.SLOWED, step, new ArrayList<>())) {
                if (slowed.value() == 0) {
                    assertEquals(sortie.ship().x(), slowed.x(), "at the ship");
                    return step;
                }
            }
        }
        return -1;
    }

    /** The steps the ship stays slowed, then immune, after a ring of {@code ring} slowed it. */
    private static int[] slowAndImmunity(EnemySpec.SlowRing ring) {
        Sortie sortie = sortie(List.of(nests(1, siren(90, ring), 1, WaveSpec.Edge.RIGHT, false)));
        int at = slowedAt(sortie, 280, 330, SimStep.ticks(5), null);
        assertTrue(at > 0, "slowed");
        assertTrue(sortie.ship().slow().slowed());
        assertEquals(1, sortie.ship().slow().share(), 1e-9, "the whole slow left");
        int slowed = 0;
        int immune = 0;
        double fastest = 0;
        for (int step = 0; step < SimStep.ticks(8); step++) {
            sortie.step(step < SimStep.ticks(0.5) ? Command.LEFT.bit() : Command.NONE);
            if (sortie.ship().slow().slowed()) {
                slowed++;
                fastest = Math.max(fastest, Math.abs(sortie.ship().vx()));
            } else if (sortie.ship().slow().immune()) {
                immune++;
            }
        }
        assertEquals(ring.slow() * TestSpecs.SHIP.speed(), fastest, 1e-9, "slowed to its share of the speed");
        return new int[] {slowed + 1, immune};
    }

    @Test
    void theRingSlowsTheShipTo60PercentFor2SThen3SOfImmunityEasyAndHardByTheirNumbers() {
        int[] medium = slowAndImmunity(RING);
        assertEquals(SimStep.ticks(2), medium[0], "slowed 2 s");
        assertEquals(SimStep.ticks(3), medium[1], "then immune 3 s");
        int[] easy = slowAndImmunity(EASY);
        assertEquals(SimStep.ticks(1.5), easy[0], "easy: slowed 1.5 s");
        int[] hard = slowAndImmunity(HARD);
        assertEquals(SimStep.ticks(2), hard[1], "hard: immune 2 s");
    }

    @Test
    void aSlowedOrImmuneHullTakesNoOtherSlow() {
        ShipSlow slow = new ShipSlow();
        assertTrue(slow.slow(RING));
        assertFalse(slow.slow(RING), "slowed already");
        assertEquals(0.6, slow.factor());
        for (int i = 0; i < SimStep.ticks(2); i++) {
            slow.step();
        }
        assertEquals(1, slow.factor(), "the slow is over");
        assertTrue(slow.immune());
        assertFalse(slow.slow(RING), "immune");
        for (int i = 0; i < SimStep.ticks(3); i++) {
            slow.step();
        }
        assertFalse(slow.immune());
        assertTrue(slow.slow(RING), "slowed again after its immunity");
    }

    @Test
    void rockOnTheRingsLineStopsIt() {
        Sortie sortie = sortie(PILLAR, List.of(nests(1, SIREN, 1, WaveSpec.Edge.RIGHT, false)), TestSpecs.LOADOUT);
        assertEquals(-1, slowedAt(sortie, 280, 330, SimStep.ticks(5), null), "the pillar between the sac and the ship");
        assertFalse(sortie.ship().slow().slowed());
    }

    @Test
    void aDashPassesTheRing() {
        Loadout dashing = TestSpecs.LOADOUT.withThrusters(DashTest.DASH);
        int at = slowedAt(
                sortie(WALLS, List.of(nests(1, SIREN, 1, WaveSpec.Edge.RIGHT, false)), dashing),
                280,
                330,
                SimStep.ticks(5),
                null);
        assertTrue(at > 0);
        Sortie sortie = sortie(WALLS, List.of(nests(1, SIREN, 1, WaveSpec.Edge.RIGHT, false)), dashing);
        assertEquals(-1, slowedAt(sortie, 280, 330, SimStep.ticks(5), new int[] {at - 3}), "dashed through its band");
        assertFalse(sortie.ship().slow().slowed());
    }

    @Test
    void rookIsSlowedTooWithHisOwnImmunity() {
        LevelScript level = trench(WALLS, List.of(nests(1, SIREN, 1, WaveSpec.Edge.RIGHT, false)));
        Sortie sortie = WingmanTest.sortie(level, WingmanSpec.Side.RIGHT);
        int rook = -1;
        boolean ship = false;
        for (int step = 0; step < SimStep.ticks(9) && (rook < 0 || step <= rook + SimStep.ticks(2)); step++) {
            sortie.step(toward(sortie, 250, 330));
            for (At slowed : events(sortie, SimEvents.Type.SLOWED, step, new ArrayList<>())) {
                if (slowed.value() == 1) {
                    rook = step;
                    assertTrue(sortie.wingman().orElseThrow().slow().slowed());
                    assertEquals(sortie.wingman().orElseThrow().x(), slowed.x(), "at Rook");
                } else {
                    ship = true;
                }
            }
            if (rook >= 0 && step > rook && step < rook + SimStep.ticks(2)) {
                assertTrue(sortie.wingman().orElseThrow().slow().slowed(), "slowed 2 s");
            }
        }
        assertTrue(rook >= 0, "Rook slowed");
        assertTrue(ship, "the ship slowed");
        assertFalse(sortie.wingman().orElseThrow().slow().slowed(), "his slow is over");
        assertTrue(sortie.wingman().orElseThrow().slow().immune(), "his immunity runs");
    }

    @Test
    void shootingTheSacEndsItsSong() {
        // A 4 HP nest on the left wall: shot down once its ring is out, the ring ends with it.
        Sortie sortie = sortie(List.of(nests(1, siren(4, RING), 1, WaveSpec.Edge.LEFT, false)));
        boolean rang = false;
        boolean killed = false;
        for (int step = 0; step < SimStep.ticks(5) && !killed; step++) {
            int commands = toward(sortie, 140, 96);
            sortie.step(rang ? commands | Command.FIRE.bit() : commands);
            rang |= sortie.events().count(SimEvents.Type.RING) > 0;
            if (rang && !killed) {
                killed = sortie.events().count(SimEvents.Type.ENEMY_DESTROYED) > 0;
                assertEquals(killed ? 0 : 1, sortie.sonarRings().count(), "its ring in flight until it dies");
            }
        }
        assertTrue(killed, "shot down");
        // Shot before its first ring: no ring at all.
        Sortie early = sortie(List.of(nests(1, siren(4, RING), 1, WaveSpec.Edge.LEFT, false)));
        int rings = 0;
        for (int step = 0; step < SimStep.ticks(5); step++) {
            early.step(toward(early, 140, 96) | Command.FIRE.bit());
            rings += early.events().count(SimEvents.Type.RING);
        }
        assertEquals(0, rings, "shot during its tell");
        assertTrue(AbyssRayTest.all(early, "siren").isEmpty(), "shot down");
    }

    @Test
    void nestsNeedWallsAndComeInAtTheFront() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Sortie(
                        1,
                        TestSpecs.LOADOUT,
                        TestSpecs.level(30, List.of(nests(1, SIREN, 1, WaveSpec.Edge.RIGHT, false))),
                        TestSpecs.RULES,
                        TestSpecs.FULL_ARMOUR));
        assertThrows(IllegalArgumentException.class, () -> TestSpecs.wave(
                        1, WaveSpec.Formation.SINGLE, SIREN, 1, WaveSpec.Entry.SIDES, WaveSpec.Edge.RIGHT)
                .withWall(false));
        assertThrows(
                IllegalArgumentException.class, () -> nests(1, SIREN, 2, WaveSpec.Edge.RIGHT, true), "opposite: both");
        assertThrows(
                IllegalArgumentException.class, () -> new EnemySpec.SlowRing(1.2, 4.5, 0.8, 120, 230, 10, 1.5, 2, 3));
    }

    @Test
    void aLevelWithoutARingUnitHasNoRings() {
        assertNull(new Sortie(1, TestSpecs.LOADOUT, trench(WALLS, List.of()), TestSpecs.RULES, TestSpecs.FULL_ARMOUR)
                .sonarRings());
        assertNotNull(
                sortie(List.of(nests(1, SIREN, 1, WaveSpec.Edge.RIGHT, false))).sonarRings());
    }

    @Test
    void theSameSeedSingsTheSameWithoutAllocating() {
        java.util.function.Supplier<Sortie> make = () -> {
            LevelScript level = trench(WALLS, List.of(nests(1, SIREN, 3, WaveSpec.Edge.NONE, false)));
            return WingmanTest.sortie(level, WingmanSpec.Side.RIGHT);
        };
        Sortie a = make.get();
        Sortie b = make.get();
        for (int step = 0; step < SimStep.ticks(8); step++) {
            a.step(toward(a, 250, 330));
            b.step(toward(b, 250, 330));
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int step = 0; step < SimStep.ticks(8); step++) {
                sortie.step(toward(sortie, 250, 330));
            }
        }));
    }
}
