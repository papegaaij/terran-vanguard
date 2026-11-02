package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M10 part F, step F1a: the aces' flight ({@link AceFlight}; design/enemies/bosses/honour-guard, The aces and The
 * playbook) on the test guard of {@link Act6FSpecs}: over five seeds with a stand-in playbook the hulls stay 80 px
 * apart, the dancer 120 px and the feinter its pull-out from the ship, the stations in their bands; the feint dive
 * pulls out at 110 px (85 once an ace died), never a ram; the loop's time; the slide (step F6s: the nose along its
 * way); the roll's and the ribbon's passes; the pincer's two lanes. Step R79b2: the hulls, the keep-offs and the bands
 * as rates over the seeds (they steer), the floors held every step.
 */
class AceFlightTest {
    private static final double EPS = 1e-6;
    /** The solver's residue when two told moves and the ship's keep-off press on one ace at once, px. */
    private static final double SOLVED = 0.01;

    private static final int H = Act6FSpecs.HARKER;
    private static final int V = Act6FSpecs.VESPER;
    private static final int K = Act6FSpecs.KESTREL;

    private static boolean flies(Squadron squadron, int a) {
        Squadron.State s = squadron.state(a);
        return s == Squadron.State.ENTERING || s == Squadron.State.FLYING;
    }

    /**
     * Step R79b2 (round 79 H.1; the main agent's decisions: the separation and the keep-offs steer, the bands hold where
     * an ace holds): as rates over the five seeds, never a per-step stop: the hulls nearer than 80 px a small share of
     * the time and never nearer than the solver's floor; the dancer and the feinter inside their keep-offs a small share
     * and never inside the ship's ring (never a ram); the stations' orbits in their bands; every move flown.
     */
    @Test
    void overFiveSeedsTheHullsStayApartAndOffTheShipAndTheStationsInTheirBands() {
        int[] moves = new int[AceMove.values().length];
        int steps = 0;
        int close = 0;
        int inKeep = 0;
        int holding = 0;
        int inBand = 0;
        for (long seed = 1; seed <= 5; seed++) {
            Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD, seed);
            Act6FSpecs.Playbook playbook = new Act6FSpecs.Playbook(seed * 31);
            Squadron squadron = rig.squadron;
            AceFlight flight = squadron.flight();
            int together = 0;
            for (int i = 0; i < SimStep.ticks(110); i++) {
                double t = i * SimStep.SECONDS;
                rig.shipX = 240 + 170 * Math.sin(t * 0.6 + seed);
                rig.shipY = 90 + 50 * Math.sin(t * 1.1);
                rig.step();
                playbook.step(squadron, rig.shipX, rig.shipY);
                for (int a = 0; a < 3; a++) {
                    if (!flies(squadron, a)) {
                        continue;
                    }
                    steps++;
                    moves[squadron.move(a).ordinal()]++;
                    boolean near = false;
                    for (int b = 0; b < 3; b++) {
                        if (b != a && flies(squadron, b)) {
                            double gap = squadron.gap(a, b);
                            assertTrue(
                                    gap >= 80 - AceFlight.SOLVED_SLACK - SOLVED,
                                    "seed " + seed + " t " + t + ": the solver's floor " + gap + dump(squadron, rig));
                            near |= gap < 80 - 0.5;
                        }
                    }
                    close += near ? 1 : 0;
                    double toShip = Math.hypot(squadron.x(a) - rig.shipX, squadron.y(a) - rig.shipY);
                    if (a != H && squadron.state(a) == Squadron.State.FLYING) {
                        assertTrue(
                                toShip >= AceFlight.SHIP_RING - SOLVED,
                                "seed " + seed + " t " + t + ": ace " + a + " " + toShip
                                        + " from the ship, never a ram " + dump(squadron, rig));
                        inKeep += toShip < squadron.keepOff(a) - 0.5 ? 1 : 0;
                    }
                    SquadronSpec.Station station = squadron.spec().aces().get(a).station();
                    if (squadron.move(a) == AceMove.HOLD && squadron.leg(a) == AceLeg.HOLD) {
                        holding++;
                        inBand += flight.pathY[a] >= station.yMin() - EPS && flight.pathY[a] <= station.yMax() + EPS
                                ? 1
                                : 0;
                    }
                }
                if (flies(squadron, H) && flies(squadron, V) && flies(squadron, K)) {
                    together++;
                }
            }
            assertTrue(together > SimStep.ticks(40), "seed " + seed + ": the three flew together");
        }
        System.out.printf(
                java.util.Locale.ROOT,
                "Honour Guard flight, five seeds: under 80 px %.1f %%, in a keep-off %.1f %%, orbits in band %.1f %%%n",
                100.0 * close / steps,
                100.0 * inKeep / steps,
                100.0 * inBand / Math.max(1, holding));
        assertTrue((double) close / steps <= AceFlightSmoothnessTest.MOST_CLOSE, "the hulls 80 px apart: " + close);
        assertTrue((double) inKeep / steps <= MOST_IN_KEEP, "off the ship: " + inKeep);
        assertTrue((double) inBand / holding >= LEAST_ORBIT_IN_BAND, "the orbits in their bands");
        for (AceMove m : new AceMove[] {
            AceMove.SLIDE, AceMove.LOOP, AceMove.ROLL, AceMove.RIBBON, AceMove.DIVE, AceMove.RAKE, AceMove.OVERHEAD
        }) {
            assertTrue(moves[m.ordinal()] > 0, "flew " + m);
        }
    }

    /** Step R79b2: the most share of the dancer's and the feinter's steps inside their keep-offs from the ship. */
    static final double MOST_IN_KEEP = 0.10;
    /**
     * Step R79b2: the least share of the orbits' steps whose path lies in its band, no slack (the feinter's orbit lifts
     * over a ship in its lane's half when it cannot cross).
     */
    static final double LEAST_ORBIT_IN_BAND = 0.9;

    private static String dump(Squadron squadron, Act6FSpecs.Rig rig) {
        StringBuilder b = new StringBuilder("ship " + rig.shipX + "," + rig.shipY);
        for (int a = 0; a < 3; a++) {
            b.append(" | ")
                    .append(a)
                    .append(' ')
                    .append(squadron.move(a))
                    .append(' ')
                    .append(squadron.leg(a))
                    .append(' ')
                    .append(squadron.x(a))
                    .append(',')
                    .append(squadron.y(a))
                    .append(" path ")
                    .append(squadron.flight().pathX[a])
                    .append(',')
                    .append(squadron.flight().pathY[a]);
        }
        return b.toString();
    }

    /** The guard with Kestrel on the field (its join's time) and the others where they are. */
    private static Act6FSpecs.Rig withKestrel() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD);
        rig.untilAllFly(50);
        rig.steps(1);
        return rig;
    }

    /**
     * Step F6s: ace {@code a}'s 0.8 s tell as the playbook flies it: its nose turned onto the ship (aiming from its
     * station), then its move starts.
     */
    private static void tell(Act6FSpecs.Rig rig, int a) {
        AceFlight flight = rig.squadron.flight();
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            flight.aim[a] = true;
            rig.step();
        }
        flight.aim[a] = false;
    }

    /**
     * Flies Kestrel's dive from its station at the still ship (its tell first); returns its least distance and where
     * its pull-out began.
     */
    private static double[] dive(Act6FSpecs.Rig rig) {
        Squadron squadron = rig.squadron;
        while (!(squadron.ready(K) && squadron.leg(K) == AceLeg.HOLD)) {
            // Step F6s: on its station's orbit (not still on its way home).
            rig.step();
        }
        tell(rig, K);
        squadron.start(K, AceMove.DIVE, rig.shipX, rig.shipY, rig);
        double least = Double.POSITIVE_INFINITY;
        double pullOut = -1;
        boolean ran = false;
        // (step R79b: 8 s, 6 before: a dive begun too near for a run draws off first in a wide turn)
        for (int i = 0; i < SimStep.ticks(8) && squadron.move(K) == AceMove.DIVE; i++) {
            rig.step();
            double d = Math.hypot(squadron.x(K) - rig.shipX, squadron.y(K) - rig.shipY);
            least = Math.min(least, d);
            ran |= squadron.leg(K) == AceLeg.RUN;
            if (pullOut < 0 && squadron.leg(K) == AceLeg.PULL_OUT) {
                pullOut = d;
            }
        }
        assertTrue(ran, "it ran at the ship");
        assertEquals(AceMove.HOLD, squadron.move(K), "back on its station");
        return new double[] {least, pullOut};
    }

    @Test
    void theFeintDivePullsOutAtItsDistanceAndCommitsCloserAfterADeath() {
        Act6FSpecs.Rig rig = withKestrel();
        double[] first = dive(rig);
        assertEquals(110, first[0], 1.5, "pulls out at 110 px, never closer");
        assertTrue(first[0] >= 110 - EPS);
        // (step R79b: 10, 6 before: its offset's turn eased, it flies a few px off its path at the pull-out's start)
        assertEquals(AceFlight.pullOutStart(110), first[1], 10, "the pull-out (its burst) starts out there");
        Act6FSpecs.kill(rig.piece, H);
        rig.step();
        assertEquals(85, rig.squadron.keepOff(K), EPS, "committed after the first death");
        double[] second = dive(rig);
        assertEquals(85, second[0], 1.5, "commits at 85 px");
        assertTrue(second[0] >= 85 - EPS && second[0] > AceFlight.SHIP_RING, "outside the ring: never a ram");
    }

    @Test
    void theLoopTakesItsTwoAndAHalfSecondsAndTheSlideCrossesWithTheNoseAlongItsWay() {
        Act6FSpecs.Rig rig = withKestrel();
        Squadron squadron = rig.squadron;
        AceFlight flight = squadron.flight();
        while (!squadron.ready(V)) {
            rig.step();
        }
        squadron.start(V, AceMove.LOOP, rig.shipX, rig.shipY, rig);
        double fromX = flight.pathX[V];
        double fromY = flight.pathY[V];
        int ticks = 0;
        while (squadron.move(V) == AceMove.LOOP) {
            rig.step();
            ticks++;
            if (squadron.move(V) == AceMove.LOOP) {
                assertEquals(AceLeg.TURN, squadron.leg(V));
            }
        }
        assertEquals(2.5, ticks * SimStep.SECONDS, 2 * SimStep.SECONDS, "one loop in 2.5 s");
        assertEquals(0, Math.hypot(flight.pathX[V] - fromX, flight.pathY[V] - fromY), 4, "round to where it began");
        while (!squadron.ready(H)) {
            rig.step();
        }
        double centre = flight.centreX[H];
        squadron.start(H, AceMove.SLIDE, rig.shipX, rig.shipY, rig);
        assertEquals(PlayField.WIDTH - centre, flight.centreX[H], EPS, "to the mirrored side");
        int slide = 0;
        while (squadron.move(H) == AceMove.SLIDE) {
            rig.step();
            slide++;
            double vx = squadron.x(H) - flight.prevX[H];
            double vy = squadron.y(H) - flight.prevY[H];
            if (Math.hypot(vx, vy) * SimStep.PER_SECOND > 60) {
                // Step F6s: his nose leads his way (no more strafing sideways with it on the ship).
                double off = Math.IEEEremainder(Enemy.heading(vx, vy) - squadron.facing(H), 2 * Math.PI);
                assertTrue(Math.abs(off) < Math.toRadians(30), "his nose along his way: " + Math.toDegrees(off));
            }
        }
        // Step R79b: its turns at the turn rate, slower than its dash (1.5 s before).
        assertTrue(slide * SimStep.SECONDS < 2.5, "at 280 px/s: " + slide * SimStep.SECONDS);
        assertEquals(PlayField.WIDTH - centre, flight.pathX[H], 41, "on the other side");
    }

    @Test
    void theRollPassesOffTheShipsSideAndTheRibbonCrossesTheField() {
        Act6FSpecs.Rig rig = withKestrel();
        Squadron squadron = rig.squadron;
        while (!squadron.ready(V)) {
            rig.step();
        }
        squadron.start(V, AceMove.ROLL, rig.shipX, rig.shipY, rig);
        double least = Double.POSITIVE_INFINITY;
        boolean middle = false;
        while (squadron.move(V) == AceMove.ROLL) {
            rig.step();
            least = Math.min(least, Math.hypot(squadron.x(V) - rig.shipX, squadron.y(V) - rig.shipY));
            middle |= squadron.leg(V) == AceLeg.RETURN && squadron.legTicks(V) == 0;
        }
        assertTrue(middle, "the roll's middle (its fan) is told by its return leg's start");
        assertTrue(least >= 120 - EPS && least < 160, "past the ship's side, at least 120 off: " + least);
        while (!squadron.ready(V)) {
            rig.step();
        }
        squadron.start(V, AceMove.RIBBON, rig.shipX, rig.shipY, rig);
        double fromX = Double.NaN;
        double toX = Double.NaN;
        while (squadron.move(V) == AceMove.RIBBON) {
            rig.step();
            if (squadron.leg(V) == AceLeg.PASS) {
                AceFlight flight = squadron.flight();
                fromX = Double.isNaN(fromX) ? flight.pathX[V] : fromX;
                toX = flight.pathX[V];
                assertTrue(
                        flight.pathY[V] >= flight.ribbonLow(V)
                                && flight.pathY[V] <= flight.ribbonLow(V) + 2 * AceFlight.RIBBON_HALF,
                        "the pass in the middle of her band");
            }
        }
        assertTrue(Math.abs(toX - fromX) > PlayField.WIDTH - 2 * AceFlight.RIBBON_INSET - 20, "across the field");
    }

    @Test
    void thePincersFlankersCrossFromOppositeSidesInTwoLanesApart() {
        Act6FSpecs.Rig rig = withKestrel();
        Squadron squadron = rig.squadron;
        while (!(squadron.ready(V) && squadron.ready(K))) {
            rig.step();
        }
        squadron.startPincer(V, K, rig.shipX, rig.shipY);
        int vPass = -1;
        int kPass = -1;
        double vFrom = Double.NaN;
        double kFrom = Double.NaN;
        for (int i = 0;
                i < SimStep.ticks(8) && (squadron.move(V) == AceMove.PINCER || squadron.move(K) == AceMove.PINCER);
                i++) {
            rig.step();
            if (vPass < 0 && squadron.leg(V) == AceLeg.PASS) {
                vPass = i;
                vFrom = squadron.x(V);
            }
            if (kPass < 0 && squadron.leg(K) == AceLeg.PASS) {
                kPass = i;
                kFrom = squadron.x(K);
            }
            if (squadron.leg(V) == AceLeg.PASS && squadron.leg(K) == AceLeg.PASS) {
                assertTrue(squadron.gap(V, K) >= 80 - EPS, "two lanes 80 px apart");
                assertTrue(squadron.y(V) > squadron.y(K), "Vesper above");
            }
        }
        assertTrue(vPass >= 0 && Math.abs(vPass - kPass) <= 1, "they start across at once");
        assertTrue((vFrom - PlayField.WIDTH / 2.0) * (kFrom - PlayField.WIDTH / 2.0) < 0, "from opposite sides");
    }
}
