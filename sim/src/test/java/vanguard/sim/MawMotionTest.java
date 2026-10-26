package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Round 58 F.2 (step m8i1, design/enemies/bosses/abyssal-maw): the coil's motion on the test Maw of {@link MawSpecs}:
 * no pop-in (it winds in from off the screen, the head first, the body following along the path), rounded corners and
 * headings that follow the line as it lies (a sweep's bight turns its segments), and the dive's way out of the column
 * and back in (every segment off the screen before it winds back in; nothing jumps on the screen).
 */
class MawMotionTest {
    /** A segment's or the head's half size beyond which it is off the screen, px (the head's frame is 200x212). */
    private static final double SEGMENT_OFF = 40;

    private static final double HEAD_OFF = 106;
    /**
     * Neighbouring segments turn at most this much from each other, degrees (a 90 px bend at 56 px apart: 36; a sweep's
     * bight at its fullest: 40).
     */
    private static final double MOST_TURN = 45;
    /** Nothing on the screen moves more than this in a step, px (the wind-in's top speed is under 14). */
    private static final double MOST_STEP = 16;

    private static Sortie sortie(MawSpecs.Fit fit) {
        return MawSpecs.sortie(fit, TestSpecs.RULES.withInvulnerableShip());
    }

    private static boolean offScreen(double x, double y, double half) {
        return x < -half || x > PlayField.WIDTH + half || y < -half || y > PlayField.HEIGHT + half;
    }

    private static boolean onScreen(double x, double y) {
        return x >= 0 && x <= PlayField.WIDTH && y >= 0 && y <= PlayField.HEIGHT;
    }

    private static double turn(double a, double b) {
        return Math.abs(Math.toDegrees(Math.IEEEremainder(a - b, 2 * Math.PI)));
    }

    /** The most two neighbouring segments' headings differ now, degrees. */
    private static double mostNeighbourTurn(CoilArena coil) {
        double most = 0;
        for (int i = 1; i < coil.segmentCount(); i++) {
            most = Math.max(most, turn(coil.segmentAngle(i), coil.segmentAngle(i - 1)));
        }
        return most;
    }

    /** Places of the head and the segments, to compare with the next step's. */
    private static double[] places(CoilArena coil) {
        double[] p = new double[2 * coil.segmentCount() + 2];
        p[0] = coil.headX();
        p[1] = coil.headY();
        for (int i = 0; i < coil.segmentCount(); i++) {
            p[2 * i + 2] = coil.segmentX(i);
            p[2 * i + 3] = coil.segmentY(i);
        }
        return p;
    }

    /** Asserts nothing on the screen in both places jumped further than {@link #MOST_STEP}. */
    private static void noJump(double[] before, double[] after, String when) {
        for (int k = 0; k < before.length; k += 2) {
            if (onScreen(before[k], before[k + 1]) && onScreen(after[k], after[k + 1])) {
                double moved = Math.hypot(after[k] - before[k], after[k + 1] - before[k + 1]);
                assertTrue(moved <= MOST_STEP, when + ": " + (k / 2 - 1) + " moved " + moved);
            }
        }
    }

    @Test
    void itWindsInFromOffTheScreenHeadFirstAndTheBodyFollowsAlongThePath() {
        Sortie sortie = sortie(MawSpecs.Fit.QUIET);
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, MawSpecs.ARRIVE + 1, 0);
        assertTrue(offScreen(coil.headX(), coil.headY(), HEAD_OFF), "no pop-in: the head starts off the screen");
        for (int i = 0; i < coil.segmentCount(); i++) {
            assertTrue(offScreen(coil.segmentX(i), coil.segmentY(i), SEGMENT_OFF), "and every segment: " + i);
        }
        boolean[] entered = new boolean[coil.segmentCount()];
        double headEnteredX = Double.NaN;
        double headEnteredY = Double.NaN;
        int order = -1;
        double[] last = places(coil);
        while (!coil.up()) {
            sortie.step(0);
            double[] now = places(coil);
            noJump(last, now, "winding in");
            last = now;
            assertTrue(mostNeighbourTurn(coil) <= MOST_TURN, "a smooth body winding in: " + mostNeighbourTurn(coil));
            if (Double.isNaN(headEnteredX) && onScreen(coil.headX(), coil.headY())) {
                headEnteredX = coil.headX();
                headEnteredY = coil.headY();
            }
            for (int i = 0; i < coil.segmentCount(); i++) {
                if (!entered[i] && onScreen(coil.segmentX(i), coil.segmentY(i))) {
                    entered[i] = true;
                    assertTrue(i == order + 1, "the body follows the head in, segment by segment: " + i);
                    order = i;
                    assertFalse(Double.isNaN(headEnteredX), "the head first");
                }
            }
        }
        assertEquals(462, headEnteredX, 1e-6, "the head enters down the right edge, where the tail ends");
        assertTrue(headEnteredY > PlayField.HEIGHT - MOST_STEP, "at the top: " + headEnteredY);
        assertEquals(MawSpecs.REST_X, coil.headX(), 1e-9, "at rest as it is up");
        assertEquals(MawSpecs.REST_Y, coil.headY(), 1e-9);
        assertEquals(-Math.PI / 2, coil.headFacing(), 1e-9, "facing down");
        assertEquals(0, coil.depth(), 1e-12, "on the play plane");
    }

    @Test
    void itsCornersAreRoundAndItsSegmentsFaceAlongTheLineABightTurnsThem() {
        Sortie sortie = MawSpecs.sortie(MawSpecs.Fit.QUIET.withSweeps(), TestSpecs.RULES.withInvulnerableShip());
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        double[] rest = new double[coil.segmentCount()];
        for (int i = 0; i < coil.segmentCount(); i++) {
            rest[i] = coil.segmentAngle(i);
        }
        assertTrue(mostNeighbourTurn(coil) <= MOST_TURN, "no square corner: " + mostNeighbourTurn(coil));
        int turned = 0;
        for (int i = 1; i < coil.segmentCount(); i++) {
            double t = turn(rest[i], rest[i - 1]);
            turned += t > 1 && t < 89 ? 1 : 0;
        }
        assertTrue(turned >= 6, "the corners' segments turn partway round each bend: " + turned);
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        MawSpecs.steerTo(sortie, 62, 250);
        MawSpecs.until(sortie, SimEvents.Type.COIL_SWEEP, 8, 0);
        double most = 0;
        while (coil.sweepState() != CoilArena.SweepState.IDLE) {
            sortie.step(0);
            assertTrue(mostNeighbourTurn(coil) <= MOST_TURN, "a smooth bight: " + mostNeighbourTurn(coil));
            for (int i = 0; i < coil.segmentCount(); i++) {
                if (coil.inBight(i)) {
                    most = Math.max(most, turn(coil.segmentAngle(i), rest[i]));
                }
            }
        }
        assertTrue(most > 30, "the bight turns its segments with the bend: " + most);
        for (int i = 0; i < coil.segmentCount(); i++) {
            assertEquals(rest[i], coil.segmentAngle(i), 1e-9, "back at rest: " + i);
        }
    }

    @Test
    void afterTheLungeTheBodyLeavesTheScreenThenWindsBackInWithoutAJump() {
        Sortie sortie = sortie(MawSpecs.Fit.QUIET);
        CoilArena coil = MawSpecs.coil(sortie);
        MawSpecs.untilUp(sortie);
        MawSpecs.headTo(sortie, 0.65);
        MawSpecs.untilPhase(sortie, 1, 0.1);
        MawSpecs.steps(sortie, 1, 0);
        MawSpecs.headTo(sortie, 0.45);
        MawSpecs.until(sortie, SimEvents.Type.MAW_DIVE, 0.1, 0);
        double[] last = places(coil);
        while (coil.diveState() == CoilArena.DiveState.SLIDE) {
            sortie.step(0);
            double[] now = places(coil);
            noJump(last, now, "sliding out");
            last = now;
        }
        for (int i = 0; i < coil.segmentCount(); i++) {
            assertTrue(offScreen(coil.segmentX(i), coil.segmentY(i), SEGMENT_OFF), "slid off the edges: " + i);
        }
        assertEquals(MawSpecs.REST_X, coil.headX(), 1e-9, "the head sinks where it rests (in the anti-sub's reach)");
        assertEquals(MawSpecs.REST_Y, coil.headY(), 1e-9);
        while (coil.diveState() != CoilArena.DiveState.LUNGE) {
            sortie.step(0);
        }
        last = places(coil);
        boolean allOff = false;
        while (coil.diveState() == CoilArena.DiveState.LUNGE || coil.diveState() == CoilArena.DiveState.BODY) {
            sortie.step(0);
            double[] now = places(coil);
            noJump(last, now, "up the column");
            last = now;
            if (coil.diveState() == CoilArena.DiveState.BODY) {
                for (int i = 0; i < coil.segmentCount(); i++) {
                    assertEquals(coil.columnX(), coil.segmentX(i), 1e-9, "the body follows the head up the column");
                }
            }
            boolean off = offScreen(coil.headX(), coil.headY(), HEAD_OFF);
            for (int i = 0; i < coil.segmentCount(); i++) {
                off &= offScreen(coil.segmentX(i), coil.segmentY(i), SEGMENT_OFF);
            }
            allOff |= off && coil.diveState() == CoilArena.DiveState.BODY;
        }
        assertEquals(CoilArena.DiveState.RETURN, coil.diveState());
        assertTrue(allOff, "every segment leaves the screen before it winds back in");
        for (int i = 0; i < coil.segmentCount(); i++) {
            assertTrue(offScreen(coil.segmentX(i), coil.segmentY(i), SEGMENT_OFF), "off as the way back starts: " + i);
        }
        int order = -1;
        boolean[] entered = new boolean[coil.segmentCount()];
        while (coil.diving()) {
            sortie.step(0);
            double[] now = places(coil);
            noJump(last, now, "winding back in");
            last = now;
            for (int i = 0; i < coil.segmentCount(); i++) {
                if (!entered[i] && onScreen(coil.segmentX(i), coil.segmentY(i))) {
                    entered[i] = true;
                    assertEquals(order + 1, i, "the body follows the head back in");
                    order = i;
                }
            }
        }
        assertTrue(coil.walled(), "the walls back");
        assertEquals(MawSpecs.REST_X, coil.headX(), 1e-9);
        assertEquals(MawSpecs.REST_Y, coil.headY(), 1e-9);
    }
}
