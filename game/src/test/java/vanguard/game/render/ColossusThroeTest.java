package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.BreachArena;
import vanguard.sim.BreachArena.Phase;
import vanguard.sim.Command;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WalkerHooks;

/**
 * Round 44 (step GK; briefs/l21-art-names.md "Round 44: spine" and "the death throe"): the Dust Colossus's back rising
 * out of its ridge head to tail and sinking tail to head (step GSR) on the ridge's track and the vents' places, its 4.5 s death throe's timeline (the body, the geysers from the
 * tail to the head, the fissure ring, the plume, the settling, the shake, the flash and the credit shower), the ground
 * standing still under a risen mouth in the crawling arena (K.c), and the bite's circle through the bite's tell only
 * (Q.10).
 */
class ColossusThroeTest {
    private static final double RISE = 1.5;
    private static final double DIVE = 1.5;

    /**
     * Step GSR: one front runs along the back from the collar to the tail's tip over the rise, the plates rising
     * continuously behind it and each segment shown only up to it; the dive runs it back from the tail to the head.
     */
    @Test
    void theBackRisesOutOfTheRidgeHeadToTailAndSinksTailToHead() {
        double far = 1000;
        assertEquals(ColossusSpine.FRONT_FROM, ColossusSpine.front(Phase.RISE, 0, RISE, DIVE), 1e-9);
        assertEquals(ColossusSpine.FRONT_TO, ColossusSpine.front(Phase.RISE, RISE, RISE, DIVE), 1e-9);
        for (int i = 0; i < ColossusSpine.SEGMENTS; i++) {
            assertFalse(ColossusSpine.out(i, ColossusSpine.front(Phase.RISE, 0, RISE, DIVE), far), "nothing out yet");
            for (Phase buried : new Phase[] {Phase.BURIED, Phase.TELL}) {
                assertFalse(ColossusSpine.out(i, ColossusSpine.front(buried, 0.5, RISE, DIVE), far), "buried");
            }
            for (Phase up : new Phase[] {Phase.OPEN, Phase.BITE}) {
                assertEquals(1, ColossusSpine.emergence(i, ColossusSpine.front(up, 0.5, RISE, DIVE)), 1e-9, "up");
            }
            assertEquals(1, ColossusSpine.emergence(i, ColossusSpine.FRONT_TO), 1e-9, "all up at the rise's end");
            assertFalse(ColossusSpine.out(i, ColossusSpine.front(Phase.DIVE, DIVE, RISE, DIVE), far), "under");
        }
        assertEquals(0, ColossusSpine.shown(ColossusSpine.FRONT_FROM, ColossusSpine.HEAD), 1e-9, "the collar hidden");
        assertEquals(1, ColossusSpine.shown(ColossusSpine.FRONT_TO, ColossusSpine.TIP), 1e-9, "the tip shown");

        // The rise: head to tail, continuous (no step over a frame), each segment's head end before its tail end.
        double[] firstOut = new double[ColossusSpine.SEGMENTS];
        double[] firstUp = new double[ColossusSpine.SEGMENTS];
        Arrays.fill(firstOut, -1);
        Arrays.fill(firstUp, -1);
        double frame = 1 / 60.0;
        double last = ColossusSpine.FRONT_FROM;
        double[] lastEmergence = new double[ColossusSpine.SEGMENTS];
        for (double t = 0; t <= RISE + 1e-9; t += frame) {
            double front = ColossusSpine.front(Phase.RISE, t, RISE, DIVE);
            assertTrue(front >= last, "the front only runs on");
            last = front;
            for (int i = 0; i < ColossusSpine.SEGMENTS; i++) {
                double e = ColossusSpine.emergence(i, front);
                assertTrue(e - lastEmergence[i] < 0.06, "segment " + i + " rises smoothly at " + t);
                lastEmergence[i] = e;
                if (firstOut[i] < 0 && ColossusSpine.out(i, front, far)) {
                    firstOut[i] = t;
                }
                if (firstUp[i] < 0 && e >= 1) {
                    firstUp[i] = t;
                }
                if (i > 0) {
                    assertTrue(e <= ColossusSpine.emergence(i - 1, front), "the head ahead of the tail at " + t);
                }
                double start = ColossusSpine.SPAN_START[i];
                double end = ColossusSpine.SPAN_END[i];
                assertTrue(
                        ColossusSpine.shown(front, start) >= ColossusSpine.shown(front, end),
                        "a segment's head end shows before its tail end");
            }
        }
        for (int i = 1; i < ColossusSpine.SEGMENTS; i++) {
            assertTrue(firstOut[i] > firstOut[i - 1], "segment " + i + " breaks the sand after " + (i - 1));
            assertTrue(firstUp[i] > firstUp[i - 1], "segment " + i + " up after " + (i - 1));
        }
        assertTrue(firstOut[0] < 0.1, "the collar's plates at once");
        assertTrue(firstUp[ColossusSpine.SEGMENTS - 1] <= RISE + 1e-6, "all up by the rise's end");
        double half = ColossusSpine.front(Phase.RISE, RISE / 2, RISE, DIVE);
        double mid = (ColossusSpine.SPAN_START[1] + ColossusSpine.SPAN_END[1]) / 2;
        assertTrue(half > ColossusSpine.SPAN_START[1] && half < ColossusSpine.TIP, "half way: in the back");
        assertTrue(ColossusSpine.emergence(1, half) > 0 && ColossusSpine.emergence(3, half) < 1, "breaking through");
        assertTrue(ColossusSpine.shown(half, mid) > ColossusSpine.shown(half, ColossusSpine.TIP), "masked ahead");

        // The dive: the exact reverse over its first share, tail to head, the head last.
        double[] under = new double[ColossusSpine.SEGMENTS];
        double[] sinking = new double[ColossusSpine.SEGMENTS];
        Arrays.fill(under, -1);
        Arrays.fill(sinking, -1);
        double share = DIVE * ColossusSpine.DIVE_SHARE;
        for (double t = 0; t <= DIVE + 1e-9; t += frame) {
            double front = ColossusSpine.front(Phase.DIVE, t, RISE, DIVE);
            assertEquals(ColossusSpine.front(Phase.RISE, RISE * Math.max(0, 1 - t / share), RISE, DIVE), front, 1e-9);
            for (int i = 0; i < ColossusSpine.SEGMENTS; i++) {
                if (sinking[i] < 0 && ColossusSpine.emergence(i, front) < 1) {
                    sinking[i] = t;
                }
                if (under[i] < 0 && !ColossusSpine.out(i, front, far)) {
                    under[i] = t;
                }
            }
        }
        for (int i = 0; i < ColossusSpine.SEGMENTS - 1; i++) {
            assertTrue(sinking[i] > sinking[i + 1], "segment " + i + " sinks after " + (i + 1));
            assertTrue(under[i] > under[i + 1], "segment " + i + " under after " + (i + 1));
        }
        assertTrue(under[0] <= share + 1e-6 && under[0] > share - 0.1, "the head last, with the mouth");
        assertEquals(1, ColossusLooks.diveBody(0, DIVE), 1e-6, "the ridge stays while the back sinks into it");
        assertEquals(1, ColossusLooks.diveBody(share, DIVE), 1e-6);
        assertEquals(0, ColossusLooks.diveBody(DIVE, DIVE), 1e-6, "then fades as the buried ridge goes on");

        // The sand spills where the front passes, in the rise and the dive.
        for (double d = ColossusSpine.HEAD; d <= ColossusSpine.TIP; d += 20) {
            double rose = RISE * (d - ColossusSpine.FRONT_FROM) / (ColossusSpine.FRONT_TO - ColossusSpine.FRONT_FROM);
            assertEquals(d, ColossusSpine.front(Phase.RISE, rose, RISE, DIVE), 1e-6);
            assertEquals(0, ColossusSpine.puffAge(d, rose, false, RISE, DIVE), 1e-9);
            double sank = share * (1 - rose / RISE);
            assertEquals(d, ColossusSpine.front(Phase.DIVE, sank, RISE, DIVE), 1e-6);
            assertEquals(0, ColossusSpine.puffAge(d, sank, true, RISE, DIVE), 1e-9);
        }
        assertFalse(ColossusSpine.out(3, ColossusSpine.FRONT_TO, 300), "nothing past the ridge's reach");
        assertTrue(ColossusSpine.out(2, ColossusSpine.FRONT_TO, 300), "up to it");

        assertTrue(ColossusSpine.shows(BreachArena.Pose.FRONT) && ColossusSpine.shows(BreachArena.Pose.REAR));
        assertFalse(ColossusSpine.shows(BreachArena.Pose.UNDER), "the under pose's body goes straight down");
        assertEquals(0, ColossusSpine.frame(0, 0, 0));
        assertEquals(16 * 3 + 5, ColossusSpine.frame(1, 0, 5));
        assertEquals(191, ColossusSpine.frame(3, 2, 15), "the last of 192");
    }

    /** Step GSR: the back lies on the ridge's own track: the body's points, then straight on past its start. */
    @Test
    void theBackLiesOnTheRidgesTrack() {
        int n = BreachArena.RIDGE_POINTS;
        double[] cx = new double[n];
        double[] cy = new double[n];
        for (int i = 0; i < n; i++) {
            double u = (double) i / (n - 1);
            // A short hairpin: from the rear point up toward the centre and down to the left point.
            cx[i] = (1 - u) * (1 - u) * 240 + 2 * u * (1 - u) * 240 + u * u * 30;
            cy[i] = (1 - u) * (1 - u) * 25 + 2 * u * (1 - u) * 270 + u * u * 120;
        }
        double[] xs = new double[RidgeLooks.MAX_POINTS];
        double[] ys = new double[RidgeLooks.MAX_POINTS];
        int[] headings = new int[RidgeLooks.MAX_POINTS];
        int points = ColossusLooks.layOut(cx, cy, n, 1, 0, xs, ys, headings);
        double[] at = new double[2];
        for (int k = 0; k < points; k++) {
            ColossusSpine.onTrack(cx, cy, n, k * RidgeLooks.SPACING, at);
            assertEquals(xs[k], at[0], 1e-6, "point " + k + " x");
            assertEquals(ys[k], at[1], 1e-6, "point " + k + " y");
        }
        double length = ColossusSpine.length(cx, cy, n);
        ColossusSpine.onTrack(cx, cy, n, length + 50, at);
        double dx = cx[0] - cx[1];
        double dy = cy[0] - cy[1];
        double step = Math.hypot(dx, dy);
        assertEquals(cx[0] + dx / step * 50, at[0], 1e-6, "straight on past the start");
        assertEquals(cy[0] + dy / step * 50, at[1], 1e-6);
    }

    /**
     * Step SPR: a segment's frame turns by the leftover of its 22.5° heading, so the frame then points exactly along the
     * body and the joints of a slanted back line up.
     */
    @Test
    void eachSegmentTurnsByTheLeftoverOfItsHeading() {
        assertEquals(0, ColossusSpine.tilt(0, -1), 1e-4, "straight down: frame 0 as it is");
        assertEquals(0, ColossusSpine.tilt(-1, -1), 1e-4, "down and left: frame 2 as it is");
        for (int a = 0; a < 3600; a++) {
            double turn = Math.toRadians(a / 10.0); // clockwise from down
            double dx = -Math.sin(turn);
            double dy = -Math.cos(turn);
            float tilt = ColossusSpine.tilt(dx, dy);
            assertTrue(Math.abs(tilt) <= 11.25f + 1e-3f, "at most half a step: " + a / 10.0 + "° → " + tilt);
            // The frame's own heading, turned counter-clockwise by the tilt, points along the body.
            double frameTurn = RidgeLooks.heading(dx, dy) * 2 * Math.PI / RidgeLooks.HEADINGS;
            double turned = frameTurn - Math.toRadians(tilt);
            assertEquals(dx, -Math.sin(turned), 1e-5, "x at " + a / 10.0 + "°");
            assertEquals(dy, -Math.cos(turned), 1e-5, "y at " + a / 10.0 + "°");
        }
        assertTrue(
                ColossusSpine.tilt(-Math.sin(0.1), -Math.cos(0.1)) < 0,
                "a little past down toward the left: clockwise");
    }

    /**
     * The segments lie on the simulation's vents (the same distances along the same curve) where the curve does not
     * turn back on itself (the simulation then goes straight on; step GSR keeps the back on the ridge's track).
     */
    @Test
    void theVentSegmentsSitOnTheSimulationsVents() {
        Sortie sortie = ColossusFixture.sortie();
        SetPiece piece = ColossusFixture.colossus(sortie);
        BreachArena breach = piece.breach().orElseThrow();
        double[] cx = new double[BreachArena.RIDGE_POINTS];
        double[] cy = new double[BreachArena.RIDGE_POINTS];
        double[] at = new double[2];
        int checked = 0;
        boolean wasRaised = false;
        boolean hunting = false;
        int compared = 0;
        for (int t = 0; t < SimStep.ticks(130) && checked < 3; t++) {
            sortie.step(Command.NONE);
            if (piece.phase() == 0 && breach.phase() == Phase.OPEN) {
                // Into phase 2 (Hunting, below 70 % of the mouth): its breaches raise the vents.
                WalkerHooks.damage(sortie, 0, ColossusFixture.MOUTH, piece.partHp(ColossusFixture.MOUTH) * 0.1);
                hunting = true;
            }
            boolean raised = breach.ventCount() > 0 && breach.ventRaised(0);
            if (raised && !wasRaised && breach.phase() == Phase.RISE) {
                for (int i = 0; i < cx.length; i++) {
                    cx[i] = breach.ridgeX(i);
                    cy[i] = breach.ridgeY(i);
                }
                for (int v = 0; v < breach.ventCount(); v++) {
                    if (!breach.ventRaised(v)) {
                        continue;
                    }
                    double d = ColossusSpine.DISTANCES[v];
                    if (turnsBack(cx, cy, d) || d > ColossusSpine.length(cx, cy, cx.length)) {
                        continue;
                    }
                    ColossusSpine.onTrack(cx, cy, cx.length, d, at);
                    compared++;
                    assertEquals(breach.ventX(v), at[0], 1, "vent " + v + " x");
                    assertEquals(breach.ventY(v), at[1], 1, "vent " + v + " y");
                }
                checked++;
            }
            wasRaised = raised;
        }
        assertTrue(hunting && piece.phase() > 0, "into phase 2");
        assertTrue(checked > 0 && compared > 0, "a breach raised its vents");
    }

    /** Whether the track turns more than a right angle from its last stretch within {@code d} px of the mouth. */
    private static boolean turnsBack(double[] cx, double[] cy, double d) {
        int last = cx.length - 1;
        double fx = cx[last - 1] - cx[last];
        double fy = cy[last - 1] - cy[last];
        double walked = 0;
        for (int i = last; i > 0 && walked < d; i--) {
            double dx = cx[i - 1] - cx[i];
            double dy = cy[i - 1] - cy[i];
            if (dx * fx + dy * fy <= 0) {
                return true;
            }
            walked += Math.hypot(dx, dy);
        }
        return false;
    }

    @Test
    void theThroeRearsCollapsesAndSinksThenTheFlashAndTheShower() {
        List<Integer> sets = new ArrayList<>();
        for (double a = 0; a < 4; a += 0.01) {
            int frame = ColossusThroe.bodyFrame(a);
            int set = frame < 0 ? -1 : frame / 8;
            if (sets.isEmpty() || sets.getLast() != set) {
                sets.add(set);
            }
        }
        assertEquals(List.of(ColossusThroe.REAR, ColossusThroe.COLLAPSE, ColossusThroe.DEATH, -1), sets);
        assertEquals(ColossusThroe.REAR * 8 + 2, ColossusThroe.bodyFrame(0.9), "the scream");
        assertEquals(ColossusThroe.DEATH * 8 + 5, ColossusThroe.bodyFrame(3.5), "the last of the sinking");
        assertEquals(-1, ColossusThroe.bodyFrame(3.61), "no body once it is under");
        assertEquals(-1, ColossusThroe.bodyFrame(-0.1));
        assertEquals(4.5, ColossusThroe.THROE_SECONDS, 1e-9);
        assertTrue(ColossusThroe.FLASH_SECONDS > 3.6 && ColossusThroe.FLASH_SECONDS < ColossusThroe.SHOWER_SECONDS);
        assertEquals(SimStep.ticks(4.2), LevelRenderer.colossusFlashTicks());
        assertEquals(SimStep.ticks(4.5), LevelRenderer.colossusShowerTicks());
    }

    @Test
    void theGeysersRollFromTheTailToTheHead() {
        int points = 30;
        int count = ColossusThroe.geysers(points);
        assertEquals(9, count);
        assertEquals(points - 1, ColossusThroe.geyserPoint(0, count, points), "the first at the tail");
        assertEquals(0, ColossusThroe.geyserPoint(count - 1, count, points), "the last at the head");
        assertEquals(0.5, ColossusThroe.geyserStart(0, count), 1e-9);
        assertEquals(2.0, ColossusThroe.geyserStart(count - 1, count), 1e-9, "the last as the collapse starts");
        for (int j = 1; j < count; j++) {
            assertTrue(ColossusThroe.geyserStart(j, count) > ColossusThroe.geyserStart(j - 1, count));
            assertTrue(ColossusThroe.geyserPoint(j, count, points) < ColossusThroe.geyserPoint(j - 1, count, points));
        }
        assertEquals(-1, ColossusThroe.geyserFrame(0, count, 0.49));
        assertEquals(0, ColossusThroe.geyserFrame(0, count, 0.5));
        assertEquals(7, ColossusThroe.geyserFrame(0, count, 0.5 + 0.71));
        assertEquals(-1, ColossusThroe.geyserFrame(0, count, 0.5 + 0.73), "played in 0.72 s (step GFX4)");
        assertEquals(ColossusThroe.GEYSER_SCALE, ColossusThroe.geyserScale(0), 1e-6);
        assertTrue(ColossusThroe.geyserScale(7) > ColossusThroe.geyserScale(0), "growing");
        assertTrue(ColossusThroe.geyserBaseAlpha(0) > 0 && ColossusThroe.geyserBaseAlpha(5) == 0, "a short base");
        assertTrue(ColossusThroe.geyserCoreAlpha(0) > 0.5f && ColossusThroe.geyserCoreAlpha(5) == 0, "the core fades");
        assertEquals(1, ColossusThroe.geysers(1));
        assertEquals(0, ColossusThroe.geyserPoint(0, 1, 1));
    }

    @Test
    void theFissuresOpenThePlumeClimbsAndTheCraterSettles() {
        assertEquals(-1, ColossusThroe.fissureFrame(0.89));
        assertEquals(0, ColossusThroe.fissureFrame(0.9));
        assertEquals(2, ColossusThroe.fissureFrame(1.6));
        assertEquals(3, ColossusThroe.fissureFrame(1.9));
        assertEquals(3, ColossusThroe.fissureFrame(60), "the ring stays with the crater");
        assertEquals(-1, ColossusThroe.plumeFrame(1.8));
        assertEquals(0, ColossusThroe.plumeFrame(2.0));
        assertEquals(1, ColossusThroe.plumeFrame(3.0));
        assertEquals(2, ColossusThroe.plumeFrame(4.0));
        assertEquals(2, ColossusThroe.plumeFrame(5.0), "step GFX4: hanging on past the throe");
        assertEquals(-1, ColossusThroe.plumeFrame(6.5), "gone by 6.5 s");
        assertEquals(1, ColossusThroe.plumeScale(1.9), 1e-6);
        assertEquals(2, ColossusThroe.plumeScale(6.5), 1e-6);
        assertEquals(1, ColossusThroe.plumeAlpha(4.5), 1e-6);
        assertEquals(0, ColossusThroe.plumeAlpha(6.5), 1e-6);
        assertEquals(0, ColossusThroe.settled(3.3), 1e-6);
        assertEquals(0.5, ColossusThroe.settled(3.6), 1e-6);
        assertEquals(1, ColossusThroe.settled(3.9), 1e-6);
        for (int k = 0; k < ColossusThroe.ROCKS; k++) {
            double born = ColossusThroe.rockBorn(k);
            assertTrue(born >= 0.7 && born <= 3.0, "rock " + k + " born " + born);
            assertTrue(ColossusThroe.rockX(k) > 0 && ColossusThroe.rockX(k) < 480);
            assertTrue(ColossusThroe.rockY(k) > 0 && ColossusThroe.rockY(k) < 540);
        }
    }

    @Test
    void theShakeIsStrongestAsTheBodyGoesUnderAndStopsAfterTheFlash() {
        double strongest = 0;
        double at = 0;
        for (double a = 0; a < 6; a += 0.01) {
            double s = ColossusThroe.shake(a);
            assertTrue(s >= 0 && s <= 1, a + ": " + s);
            if (s > strongest) {
                strongest = s;
                at = a;
            }
            for (double t = 0; t < 0.2; t += 0.013) {
                assertTrue(Math.abs(ColossusThroe.shakeX(a, t)) <= ColossusThroe.SHAKE_PX + 1e-9);
            }
        }
        assertEquals(1, strongest, 0.02);
        assertEquals(ColossusThroe.SHOCK_AT, at, 0.02, "the plunge under the sand");
        assertTrue(ColossusThroe.shake(0.5) > 0, "the rearing body shudders");
        assertTrue(ColossusThroe.shake(4.3) > 0, "a last kick at the flash");
        assertEquals(0, ColossusThroe.shake(4.7), "still after it");
        assertEquals(0, ColossusThroe.shake(-0.1));
        assertEquals(0, ScreenShake.scaled(ColossusThroe.shakeX(3, 0.01), 0), "none with the shake off");
        assertEquals(0, ColossusThroe.shockRadius(ColossusThroe.SHOCK_AT - 0.01));
        assertTrue(ColossusThroe.shockRadius(ColossusThroe.SHOCK_AT + 0.3) > 100);
        assertEquals(0, ColossusThroe.shockRadius(ColossusThroe.SHOCK_AT + ColossusThroe.SHOCK_SECONDS + 0.01));
    }

    /**
     * K.c: while the Colossus is out of the sand the crawling arena's ground stands still as drawn, between the steps
     * too (so the craters, laid on the ground's rows, stay where the mouth dived); it crawls before the first breach.
     */
    @Test
    void theGroundStandsStillUnderARisenMouth() {
        Sortie sortie = ColossusFixture.sortie();
        SetPiece piece = ColossusFixture.colossus(sortie);
        BreachArena breach = piece.breach().orElseThrow();
        assertTrue(sortie.crawling(), "the fixture's arena crawls");
        int up = 0;
        boolean crawledBefore = false;
        boolean dived = false;
        double held = Double.NaN;
        for (int t = 0; t < SimStep.ticks(60); t++) {
            sortie.step(Command.NONE);
            Phase phase = breach.phase();
            boolean out = phase == Phase.RISE || phase == Phase.OPEN || phase == Phase.BITE || phase == Phase.DIVE;
            if (out && piece.present()) {
                assertEquals(0, sortie.groundSpeed(), 1e-12, "still under the mouth (" + phase + ")");
                double drawn = LevelRenderer.drawnScroll(sortie, 0);
                assertEquals(drawn, LevelRenderer.drawnScroll(sortie, 0.5f), 1e-12, "between the steps too");
                assertEquals(drawn, LevelRenderer.drawnScroll(sortie, 1), 1e-12);
                if (!Double.isNaN(held)) {
                    assertEquals(held, drawn, 1e-9, "not a pixel through the breach");
                }
                held = drawn;
                up++;
                dived |= phase == Phase.DIVE;
            } else {
                held = Double.NaN;
                crawledBefore |= !dived && up == 0 && sortie.groundSpeed() > 0;
            }
        }
        assertTrue(up > SimStep.ticks(3), "a breach flown: " + up);
        assertTrue(crawledBefore, "the ground crawls before the Colossus comes up");
    }

    /** Q.10: the bite's circle shows through the bite's tell and in no other phase. */
    @Test
    void theBiteCircleShowsThroughTheBitesTellOnly() {
        for (Phase phase : Phase.values()) {
            assertEquals(phase == Phase.BITE, ColossusLooks.biteCircle(phase), phase + "");
        }
        Sortie sortie = ColossusFixture.sortie();
        BreachArena breach = ColossusFixture.colossus(sortie).breach().orElseThrow();
        int shown = 0;
        int longest = 0;
        for (int t = 0; t < SimStep.ticks(40); t++) {
            sortie.step(Command.NONE);
            if (ColossusLooks.biteCircle(breach.phase())) {
                shown++;
                longest = Math.max(longest, shown);
            } else {
                shown = 0;
            }
        }
        assertTrue(longest > 0, "a bite's tell flown");
        assertTrue(longest <= SimStep.ticks(1.0), "the tell's 0.8 s, not longer: " + longest);
        assertEquals(70, breach.biteRadius(), 1e-9, "the 70 px circle");
    }
}
