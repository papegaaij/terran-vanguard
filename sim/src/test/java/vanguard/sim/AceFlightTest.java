package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M10 part F, step F1a: the aces' flight ({@link AceFlight}; design/enemies/bosses/honour-guard, The aces and The
 * playbook) on the test guard of {@link Act6FSpecs}: over five seeds with a stand-in playbook the hulls stay 80 px
 * apart, the dancer 120 px and the feinter its pull-out from the ship, the stations in their bands; the feint dive
 * pulls out at 110 px (85 once an ace died), never a ram; the loop's time; the slide (step F6s: the nose along its
 * way); the roll's and the ribbon's passes; the pincer's two lanes.
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

    @Test
    void overFiveSeedsTheHullsStayApartAndOffTheShipAndTheStationsInTheirBands() {
        for (long seed = 1; seed <= 5; seed++) {
            Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD, seed);
            Act6FSpecs.Playbook playbook = new Act6FSpecs.Playbook(seed * 31);
            Squadron squadron = rig.squadron;
            AceFlight flight = squadron.flight();
            double least = Double.POSITIVE_INFINITY;
            int together = 0;
            int[] moves = new int[AceMove.values().length];
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
                    moves[squadron.move(a).ordinal()]++;
                    for (int b = a + 1; b < 3; b++) {
                        if (flies(squadron, b)) {
                            least = Math.min(least, squadron.gap(a, b));
                        }
                    }
                    double toShip = Math.hypot(squadron.x(a) - rig.shipX, squadron.y(a) - rig.shipY);
                    if (a != H) {
                        assertTrue(
                                toShip >= squadron.keepOff(a) - SOLVED,
                                "seed " + seed + " t " + t + ": ace " + a + " " + toShip + " from the ship "
                                        + dump(squadron, rig));
                    }
                    SquadronSpec.Station station = squadron.spec().aces().get(a).station();
                    AceLeg leg = squadron.leg(a);
                    boolean onStation = (a != K && leg == AceLeg.HOLD) || (a == K && leg == AceLeg.WAIT);
                    if (squadron.move(a) == AceMove.HOLD && onStation) {
                        assertTrue(
                                flight.pathY[a] >= station.yMin() - EPS && flight.pathY[a] <= station.yMax() + EPS,
                                "seed " + seed + ": ace " + a + " holds in its band: " + flight.pathY[a]);
                    }
                }
                if (flies(squadron, H) && flies(squadron, V) && flies(squadron, K)) {
                    together++;
                }
            }
            assertTrue(least >= 80 - SOLVED, "seed " + seed + ": the hulls 80 px apart, edge to edge: " + least);
            assertTrue(together > SimStep.ticks(40), "seed " + seed + ": the three flew together");
            for (AceMove m : new AceMove[] {
                AceMove.SLIDE, AceMove.LOOP, AceMove.ROLL, AceMove.RIBBON, AceMove.DIVE, AceMove.RAKE, AceMove.OVERHEAD
            }) {
                assertTrue(moves[m.ordinal()] > 0, "seed " + seed + " flew " + m);
            }
        }
    }

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
        for (int i = 0; i < SimStep.ticks(6) && squadron.move(K) == AceMove.DIVE; i++) {
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
        assertEquals(AceFlight.pullOutStart(110), first[1], 6, "the pull-out (its burst) starts out there");
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
        assertTrue(slide * SimStep.SECONDS < 1.5, "at 280 px/s");
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
                        flight.pathY[V] >= AceFlight.RIBBON_Y_MIN && flight.pathY[V] <= AceFlight.RIBBON_Y_MAX,
                        "the pass at y 200–260 from the top");
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
