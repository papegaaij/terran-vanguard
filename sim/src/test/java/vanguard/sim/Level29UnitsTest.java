package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part B, step B1b (the four gaps step B1 left for Level 29's units): the wavering Stormhawks' peel-off, the Talon's
 * swoop path, the SAM Nest's hold fire within 96 px and its per-nest cap of missiles in flight. All opt-in: a level
 * without them flies as before (the golden flights pin that).
 */
class Level29UnitsTest {
    private static final int FIRE = Command.FIRE.bit();

    /** One unit's track while it was on the list: its first and last place and its lowest point. */
    private static final class Track {
        double firstX = Double.NaN;
        double firstY;
        double lastX;
        double lastY;
        double lowX;
        double lowY = Double.POSITIVE_INFINITY;
        double onFieldSeconds;
        boolean peeled;
        double peelY = Double.NaN;
        boolean peelLevel = true;
        double peelVx;
    }

    private static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /**
     * Flies {@code sortie} for {@code seconds} with {@code command}, tracking every unit of {@code slug} by serial and
     * counting the events of {@code types} into {@code counts}.
     */
    private static Map<Integer, Track> track(
            Sortie sortie, String slug, double seconds, int command, int[] counts, SimEvents.Type... types) {
        Map<Integer, Track> tracks = new HashMap<>();
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(command);
            for (int k = 0; k < types.length; k++) {
                counts[k] += PartCSpecs.count(sortie, types[k]);
            }
            for (int k = 0; k < sortie.enemyCount(); k++) {
                Enemy u = sortie.enemy(k);
                if (!u.spec().slug().equals(slug)) {
                    continue;
                }
                Track t = tracks.computeIfAbsent(u.serial(), s -> new Track());
                if (Double.isNaN(t.firstX)) {
                    t.firstX = u.x();
                    t.firstY = u.y();
                }
                if (t.peeled && u.peeling()) {
                    t.peelLevel &= Math.abs(u.y() - t.peelY) < 1e-9;
                    t.peelVx = (u.x() - t.lastX) / SimStep.SECONDS;
                }
                if (u.peeling() && !t.peeled) {
                    t.peeled = true;
                    t.peelY = u.y();
                }
                t.lastX = u.x();
                t.lastY = u.y();
                if (u.y() < t.lowY) {
                    t.lowY = u.y();
                    t.lowX = u.x();
                }
                if (PlayField.overlaps(u.x(), u.y(), u.spec().hitbox())) {
                    t.onFieldSeconds += SimStep.SECONDS;
                }
            }
        }
        return tracks;
    }

    private static LevelScript waves(WaveSpec... waves) {
        return LevelScript.builder(29, 5)
                .sections(List.of(new Section(40, 100)))
                .waves(List.of(waves))
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    // --- 1. The wavering Stormhawks' peel-off -------------------------------------------------------------------

    @Test
    void theWaveringPairHoldsThenPeelsOffToTheNearerSideEdgeAt260() {
        Sortie sortie = sortie(waves(TestSpecs.wave(
                0.5,
                WaveSpec.Formation.LINE_ABREAST,
                Act5BSpecs.WAVERING,
                2,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE)));
        int[] counts = new int[2];
        Map<Integer, Track> tracks = track(
                sortie, "wavering-stormhawk", 14, 0, counts, SimEvents.Type.PEEL_OFF, SimEvents.Type.NEUTRAL_LEFT);
        int peels = counts[0];
        int left = counts[1];
        assertEquals(2, peels);
        assertEquals(2, left, "both leave the screen: the secondary's count");
        assertEquals(2, tracks.size());
        for (Track t : tracks.values()) {
            assertTrue(t.peeled);
            assertEquals(PlayField.HEIGHT - 150, t.peelY, 1e-6, "it peels off from its hold");
            assertTrue(t.peelLevel, "straight sideways");
            boolean leftHalf = t.firstX < PlayField.WIDTH / 2.0;
            assertEquals(leftHalf ? -260 : 260, t.peelVx, 1e-6, "toward the nearer side edge at 260 px/s");
            assertTrue(leftHalf ? t.lastX < 0 : t.lastX > PlayField.WIDTH, "out through that edge: " + t.lastX);
            assertTrue(t.onFieldSeconds > 8 && t.onFieldSeconds < 9.5, "about 9 s on the screen: " + t.onFieldSeconds);
        }
    }

    @Test
    void aHoverWithoutAPeelLeavesAsBefore() {
        EnemySpec.Hover plain = new EnemySpec.Hover(new Range(6, 6), new Range(150, 150));
        assertEquals(0, plain.peelSpeed());
        assertEquals(plain, new EnemySpec.Hover(new Range(6, 6), new Range(150, 150), Optional.empty(), 0));
        assertEquals(260, plain.withPeel(260).peelSpeed());
        assertEquals(260, plain.withPeel(260).aboveShip(new Range(90, 90), 6).peelSpeed());
        assertThrows(IllegalArgumentException.class, () -> plain.withPeel(-1));
        Sortie sortie = sortie(waves(TestSpecs.wave(
                0.5,
                WaveSpec.Formation.LINE_ABREAST,
                Act5BSpecs.STORMHAWK,
                2,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE)));
        int peels = 0;
        for (int i = 0; i < SimStep.ticks(14); i++) {
            sortie.step(0);
            peels += PartCSpecs.count(sortie, SimEvents.Type.PEEL_OFF);
            for (int k = 0; k < sortie.enemyCount(); k++) {
                assertFalse(sortie.enemy(k).peeling());
            }
        }
        assertEquals(0, peels);
    }

    // --- 2. The Talon's swoop path -----------------------------------------------------------------------------

    @Test
    void aVWingSwoopsAcrossTheUpperHalfOnOneCurveKeepingItsShape() {
        Sortie sortie = sortie(waves(Act5BSpecs.vWing(3, Act5BSpecs.SWOOP_TALON)));
        Map<Integer, Track> tracks = track(sortie, "talon", 10, 0, new int[0]);
        assertEquals(5, tracks.size());
        double lowest = Double.NaN;
        int side = 0;
        List<Double> places = new ArrayList<>();
        for (Track t : tracks.values()) {
            assertEquals(PlayField.HEIGHT + Formations.OUTSIDE, t.firstY, 5, "enters at the top edge");
            assertEquals(PlayField.HEIGHT + Formations.OUTSIDE, t.lastY, 5, "leaves up through the top edge");
            int s = (int) Math.signum(t.lastX - t.firstX);
            assertEquals(200, Math.abs(t.lastX - t.firstX), 5, "200 px across");
            side = side == 0 ? s : side;
            assertEquals(side, s, "the wing sweeps one way");
            double depth = PlayField.HEIGHT - t.lowY;
            assertTrue(depth >= 200 - 1 && depth <= 260 + 1, "lowest point 200-260 px below the top edge: " + depth);
            lowest = Double.isNaN(lowest) ? depth : lowest;
            assertEquals(lowest, depth, 1, "one curve for the wing");
            assertEquals((t.firstX + t.lastX) / 2, t.lowX, 5, "it bottoms out halfway across");
            places.add(t.lowX);
        }
        assertPlaces(places, 140, 190, 240, 290, 340);
        assertEquals(0, sortie.enemyCount(), "gone at its curve's end");
    }

    /** The lowest points' x, sorted, are {@code expected} within 5 px: the units' formation places. */
    private static void assertPlaces(List<Double> places, double... expected) {
        places.sort(Double::compare);
        assertEquals(expected.length, places.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], places.get(i), 5, "place " + i + " of " + places);
        }
    }

    @Test
    void aWingInTheLeftColumnSweepsRight_theRightColumnLeft() {
        for (WaveSpec.Edge edge : new WaveSpec.Edge[] {WaveSpec.Edge.LEFT, WaveSpec.Edge.RIGHT}) {
            Sortie sortie = sortie(waves(TestSpecs.wave(
                    3, WaveSpec.Formation.COLUMN, Act5BSpecs.SWOOP_TALON, 3, WaveSpec.Entry.FRONT, edge)));
            Map<Integer, Track> tracks = track(sortie, "talon", 10, 0, new int[0]);
            assertEquals(3, tracks.size());
            for (Track t : tracks.values()) {
                double place = edge == WaveSpec.Edge.LEFT ? PlayField.WIDTH * 0.3 : PlayField.WIDTH * 0.7;
                assertEquals(place, t.lowX, 5, "it bottoms out in its column");
                assertTrue(edge == WaveSpec.Edge.LEFT ? t.lastX > t.firstX : t.lastX < t.firstX, edge.toString());
            }
        }
    }

    @Test
    void aLineAbreastSwoopsTogether() {
        Sortie sortie = sortie(waves(TestSpecs.wave(
                3,
                WaveSpec.Formation.LINE_ABREAST,
                Act5BSpecs.SWOOP_TALON,
                4,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE)));
        Map<Integer, Track> tracks = track(sortie, "talon", 10, 0, new int[0]);
        assertEquals(4, tracks.size());
        List<Double> places = new ArrayList<>();
        for (Track t : tracks.values()) {
            places.add(t.lowX);
        }
        assertPlaces(places, 96, 192, 288, 384);
    }

    @Test
    void aPincersWingsEnterAtTheirEdgesAndLeaveUpThroughTheTop() {
        Sortie sortie = sortie(waves(TestSpecs.wave(
                3, WaveSpec.Formation.PINCER, Act5BSpecs.SWOOP_TALON, 4, WaveSpec.Entry.SIDES, WaveSpec.Edge.NONE)));
        Map<Integer, Track> tracks = track(sortie, "talon", 10, 0, new int[0]);
        assertEquals(4, tracks.size());
        int fromLeft = 0;
        for (Track t : tracks.values()) {
            boolean left = t.firstX < 0;
            fromLeft += left ? 1 : 0;
            assertTrue(left || t.firstX > PlayField.WIDTH, "enters at a side edge");
            assertEquals(PlayField.HEIGHT - SwoopPaths.SIDE_ENTRY, t.firstY, 5);
            assertEquals(left ? 200 : PlayField.WIDTH - 200, t.lastX, 5, "up through the top on its own half");
            assertEquals(PlayField.HEIGHT + Formations.OUTSIDE, t.lastY, 5);
            double depth = PlayField.HEIGHT - t.lowY;
            assertTrue(depth >= 200 - 1 && depth <= 260 + 1, "lowest point: " + depth);
        }
        assertEquals(2, fromLeft);
    }

    @Test
    void itFiresOnlyInItsBandBelowTheTopEdge() {
        EnemySpec talon = Act5BSpecs.swoopTalon(new EnemySpec.SwoopPath(new Range(240, 240), 200, 120));
        Sortie sortie = sortie(waves(
                TestSpecs.wave(3, WaveSpec.Formation.COLUMN, talon, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)));
        int fired = 0;
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.ENEMY_FIRED) {
                    fired++;
                    assertTrue(events.y(e) >= PlayField.HEIGHT - 120 - 1e-6, "in its band: " + events.y(e));
                }
            }
        }
        assertTrue(fired >= 3, "each fires in its band: " + fired);
    }

    @Test
    void aSwoopingWingScattersWhenItsLeaderFalls() {
        Sortie sortie = sortie(waves(Act5BSpecs.vWing(3, Act5BSpecs.SWOOP_TALON)));
        while (sortie.levelSeconds() < 4.2) {
            sortie.step(0);
        }
        for (int k = 0; k < sortie.enemyCount(); k++) {
            if (sortie.enemy(k).leader()) {
                sortie.destroyEnemy(k);
                break;
            }
        }
        sortie.step(0);
        assertEquals(4, sortie.enemyCount());
        for (int k = 0; k < sortie.enemyCount(); k++) {
            assertTrue(sortie.enemy(k).scattering());
        }
    }

    @Test
    void aSwoopPathIsAPlainAirUnitsOnlyMovement() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5BSpecs.SWOOP_TALON.withHover(
                        Optional.of(new EnemySpec.Hover(new Range(6, 6), new Range(160, 160)))));
        assertThrows(
                IllegalArgumentException.class, () -> Act5BSpecs.CLAMP.withSwoopPath(Optional.of(Act5BSpecs.SWOOP)));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.SwoopPath(new Range(0, 10), 200, 300));
    }

    // --- 3. and 4. The SAM Nest: hold fire within 96 px, at most 8 per nest -------------------------------------

    /** A level of nests of {@code gun} entering at 0.5 s at {@code xs}, the ground scrolling at {@code scroll}. */
    private static LevelScript nests(EnemyGun gun, double scroll, double... xs) {
        List<GroundUnit> ground = new ArrayList<>();
        for (double x : xs) {
            ground.add(new GroundUnit(0.5, x, Act5BSpecs.samNest(gun), -1));
        }
        return LevelScript.builder(29, 5)
                .sections(List.of(new Section(40, scroll)))
                .groundUnits(ground)
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    /** Launches over the nest's pass down the ship's column: [far, within 96 px of the ship]. */
    private static int[] launches(double holdWithin) {
        Sortie sortie = sortie(nests(Act5BSpecs.samGun(1.0, 1, 8, holdWithin, true), 60, Ship.START_X));
        int[] counts = new int[2];
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.MISSILE_LAUNCHED) {
                    double dx = events.x(e) - sortie.ship().x();
                    double dy = events.y(e) - sortie.ship().y();
                    counts[Math.sqrt(dx * dx + dy * dy) < 96 ? 1 : 0]++;
                }
            }
        }
        return counts;
    }

    @Test
    void theNestHoldsFireWhileTheShipIsWithin96Px() {
        int[] held = launches(96);
        int[] open = launches(0);
        assertEquals(0, held[1], "no missile launched on the ship");
        assertTrue(held[0] >= 3, "it fires from farther off: " + held[0]);
        assertTrue(open[1] >= 2, "without the hold it would: " + open[1]);
    }

    /** The most missiles in flight at once over 7 s. */
    private static int mostInFlight(EnemyGun gun, double... xs) {
        Sortie sortie = sortie(nests(gun, 20, xs));
        int most = 0;
        for (int i = 0; i < SimStep.ticks(7); i++) {
            sortie.step(0);
            most = Math.max(most, sortie.missileCount());
            for (int k = 0; k < sortie.enemyCount(); k++) {
                assertTrue(sortie.missiles().inFlightOf(sortie.enemy(k).serial()) <= 8);
            }
        }
        return most;
    }

    @Test
    void theCapCountsEachNestsOwnMissiles() {
        assertEquals(4, mostInFlight(Act5BSpecs.samGun(0.2, 1, 2, 0, true), 120, 360), "2 a nest");
        assertEquals(2, mostInFlight(Act5BSpecs.samGun(0.2, 1, 2, 0, false), 120, 360), "2 in all (Acts 1-4)");
        assertEquals(24, mostInFlight(Act5BSpecs.samGun(0.15, 1, 8, 0, true), 80, 240, 400), "8 a nest, 3 nests");
    }

    @Test
    void theHomingSpecsOlderFormsKeepTheirMeaning() {
        EnemyGun.HomingSpec old = new EnemyGun.HomingSpec(1, 4, 2, 8, 0.4);
        assertEquals(0, old.holdWithin());
        assertFalse(old.perShooter());
        assertEquals(old, new EnemyGun.HomingSpec(1, 4, 2, 8, 0.4, false, 0, false));
        EnemyGun.HomingSpec nest = old.withSeekable(true).withHoldWithin(96).withPerShooter(true);
        assertTrue(nest.seekable() && nest.perShooter());
        assertEquals(96, nest.holdWithin());
        assertEquals(nest, nest.withSeekable(true));
        assertThrows(IllegalArgumentException.class, () -> old.withHoldWithin(-1));
    }

    // --- Together: allocation-free and replayable -----------------------------------------------------------------

    static LevelScript together() {
        List<GroundUnit> ground = List.of(
                new GroundUnit(0.5, 120, Act5BSpecs.samNest(Act5BSpecs.samGun(1.5, 2, 8, 96, true)), -1),
                new GroundUnit(0.5, 360, Act5BSpecs.samNest(Act5BSpecs.samGun(1.5, 2, 8, 96, true)), -1));
        return LevelScript.builder(29, 5)
                .sections(List.of(new Section(20, 40)))
                .waves(List.of(
                        Act5BSpecs.vWing(3, Act5BSpecs.SWOOP_TALON),
                        TestSpecs.wave(
                                3,
                                WaveSpec.Formation.PINCER,
                                Act5BSpecs.SWOOP_TALON,
                                4,
                                WaveSpec.Entry.SIDES,
                                WaveSpec.Edge.NONE),
                        TestSpecs.wave(
                                4,
                                WaveSpec.Formation.LINE_ABREAST,
                                Act5BSpecs.WAVERING,
                                2,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)))
                .groundUnits(ground)
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    static Sortie togetherSortie() {
        return new Sortie(5, TestSpecs.LOADOUT, together(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    static void flyTogether(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(16); i++) {
            sortie.step(i % 120 < 60 ? FIRE : FIRE | Command.LEFT.bit());
        }
    }

    @Test
    void theyStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Level29UnitsTest::togetherSortie, Level29UnitsTest::flyTogether));
    }

    @Test
    void theyReplay() {
        Sortie one = togetherSortie();
        Sortie two = togetherSortie();
        flyTogether(one);
        flyTogether(two);
        assertEquals(one.stateHash(), two.stateHash());
    }
}
