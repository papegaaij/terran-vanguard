package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1b (design/campaign Level 48, Waves: the Chimera V-wings {@code from: rear} at t=17 and t=58, "up
 * from the bottom edge, BOTTOM warning 3 s"; the rear rule "a rear Chimera wing rises in the lane the ship is not in,
 * its V's tip ≥ 120 px off the ship's x at the warning"): a rear V-wing is the front V mirrored top to bottom (M10 part
 * F's rising frame), in the lane {@code away} ({@link RearLanes}). Its units enter below the bottom edge, the tip first
 * at the drawn lane with the arms trailing below it, keep the V's spacing, and leave by their exit: a swooping V (the
 * Chimera's) down through the bottom edge at its curve's end, having swept toward the field's middle (mirrored about
 * its tip when its planned sweep pointed at the lane's near side edge), a gliding V out through the top edge.
 */
class RearVWingTest {
    /** The Chimera as Level 48 flies it: 220 px/s on the Talon's swoop, 200–260 px deep, 280 px across. */
    static final EnemySpec CHIMERA = EnemySpec.of("chimera", 20, new Hitbox(28, 24), Layer.AIR, 8, true, 14, 220)
            .withGun(Optional.of(EnemyGun.aimed(1.6, 0.25, 1, 160, 4, false)))
            .withSwoopPath(Optional.of(new EnemySpec.SwoopPath(new Range(200, 260), 280, 300)));

    /** A plain glider: no hover, no dive, no swoop (a V of it flies straight through). */
    private static final EnemySpec GLIDER = EnemySpec.of("glider", 10, new Hitbox(24, 24), Layer.AIR, 6, true, 5, 150);

    private static final double T = 5;
    private static final double V_SPACING = 50;
    private static final double V_RANK_STEP = 35;

    /** A V-wing of {@code count} {@code enemy} from the rear at {@code t}, its 3 s warning, in the lane away. */
    static WaveSpec wing(double t, EnemySpec enemy, int count) {
        return WaveSpec.builder(t, WaveSpec.Formation.V_WING, enemy, count, WaveSpec.Entry.REAR, WaveSpec.Edge.NONE)
                .warningSeconds(Optional.of(3.0))
                .lane(WaveSpec.Lane.AWAY)
                .build();
    }

    /** The ship's x and the lane as the warning started, whether it flew mirrored, the tracks, the units left, the hash. */
    private record Flight(
            double shipX, double lane, boolean mirrored, List<List<double[]>> ranks, int left, long hash) {}

    /** Flies {@code count} {@code enemy} with the ship steering by {@code command} until the warning starts. */
    private static Flight fly(EnemySpec enemy, int count, int command) {
        Sortie sortie = Act7ASpecs.sortie(Act7ASpecs.level(40, List.of(wing(T, enemy, count)), Optional.empty()));
        RearLanes lanes = sortie.rearLanes().orElseThrow();
        double shipX = Double.NaN;
        double lane = Double.NaN;
        Map<Integer, List<double[]>> tracks = new TreeMap<>();
        for (int i = 0; i < SimStep.ticks(T + 12); i++) {
            sortie.step(Double.isNaN(lane) ? command : 0);
            if (Double.isNaN(lane) && !Double.isNaN(lanes.laneX(0))) {
                shipX = sortie.ship().x();
                lane = lanes.laneX(0);
            }
            for (int k = 0; k < sortie.enemyCount(); k++) {
                Enemy u = sortie.enemy(k);
                tracks.computeIfAbsent(u.serial(), s -> new ArrayList<>())
                        .add(new double[] {sortie.levelTick, u.x(), u.y()});
            }
        }
        // The units in their order of entry: the tip first, then a rank's two (they enter together).
        List<List<double[]>> ranks = new ArrayList<>(tracks.values());
        ranks.sort((a, b) -> Double.compare(a.getFirst()[0], b.getFirst()[0]));
        return new Flight(shipX, lane, lanes.mirrored(0), ranks, sortie.enemyCount(), sortie.stateHash());
    }

    /** The track's point at level tick {@code tick}; null when it was not on the field then. */
    private static double[] at(List<double[]> track, double tick) {
        for (double[] point : track) {
            if (point[0] == tick) {
                return point;
            }
        }
        return null;
    }

    @Test
    void aSwoopingRearVRisesInTheAwayLaneKeepsItsShapeAndSweepsTowardTheMiddle() {
        boolean[] mirrored = new boolean[2];
        int[] commands = {Command.LEFT.bit(), Command.RIGHT.bit()};
        for (int c = 0; c < 2; c++) {
            Flight flight = fly(CHIMERA, 5, commands[c]);
            List<List<double[]>> units = flight.ranks();
            assertEquals(5, units.size());
            assertEquals(0, flight.left(), "all gone by their exit");
            double tipX = units.getFirst().getFirst()[1];
            // Its first point is one step along its curve.
            assertEquals(flight.lane(), tipX, 0.5, "the tip comes up in the lane");
            assertTrue(Math.abs(tipX - flight.shipX()) >= RearLanes.LEAST_OFF, "≥ 120 px off the ship's x");
            double tipEnd = units.getFirst().getLast()[1];
            assertTrue(
                    (PlayField.WIDTH / 2.0 - tipX) * (tipEnd - tipX) > 0,
                    "it sweeps toward the middle: " + tipX + " -> " + tipEnd);
            for (List<double[]> track : units) {
                assertTrue(track.getFirst()[2] < 0, "enters below the bottom edge: " + track.getFirst()[2]);
                assertTrue(track.getLast()[2] < 0, "leaves down through the bottom edge: " + track.getLast()[2]);
                for (double[] point : track) {
                    assertTrue(
                            point[1] >= -Formations.OUTSIDE && point[1] <= PlayField.WIDTH + Formations.OUTSIDE,
                            "on the field across: " + point[1]);
                }
            }
            // The V: each rank's two at one height, 2 × rank × 50 px apart, trailing below the ones ahead.
            double lastIn = units.getLast().getFirst()[0];
            double previous = Double.POSITIVE_INFINITY;
            for (int rank = 0; rank <= 2; rank++) {
                double[] a = at(units.get(rank == 0 ? 0 : 2 * rank - 1), lastIn);
                double[] b = at(units.get(rank == 0 ? 0 : 2 * rank), lastIn);
                assertEquals(a[2], b[2], 1e-6, "rank " + rank + " level");
                assertEquals(2 * rank * V_SPACING, Math.abs(a[1] - b[1]), 1e-6, "rank " + rank + " spacing");
                assertTrue(a[2] < previous, "rank " + rank + " trails below");
                previous = a[2];
            }
            mirrored[c] = flight.mirrored();
        }
        assertNotEquals(mirrored[0], mirrored[1], "the planned sweep points outward from exactly one of the lanes");
        assertEquals(
                fly(CHIMERA, 5, commands[0]).hash(),
                fly(CHIMERA, 5, commands[0]).hash(),
                "the same every time");
    }

    @Test
    void aGlidingRearVRisesStraightUpTipFirstAndLeavesByTheTop() {
        Flight flight = fly(GLIDER, 5, Command.LEFT.bit());
        List<List<double[]>> units = flight.ranks();
        assertEquals(5, units.size());
        assertEquals(0, flight.left());
        assertFalse(flight.mirrored(), "a straight V only moves");
        assertEquals(360, flight.lane(), 1e-9, "the right half's middle");
        double[] tip = units.getFirst().getFirst();
        assertEquals(flight.lane(), tip[1], 1e-6, "the tip in the lane");
        double lastIn = units.getLast().getFirst()[0];
        double tipY = at(units.getFirst(), lastIn)[2];
        for (int i = 0; i < units.size(); i++) {
            List<double[]> track = units.get(i);
            int rank = (i + 1) / 2;
            assertTrue(track.getFirst()[2] < 0, "enters below the bottom edge");
            assertTrue(track.getLast()[2] > PlayField.HEIGHT, "leaves through the top edge: " + track.getLast()[2]);
            for (double[] point : track) {
                assertEquals(track.getFirst()[1], point[1], 1e-9, "straight up");
            }
            assertEquals(rank * V_SPACING, Math.abs(track.getFirst()[1] - tip[1]), 1e-6, "its place across");
            assertEquals(rank * V_RANK_STEP, tipY - at(track, lastIn)[2], 2, "rank " + rank + " trails below the tip");
        }
    }
}
