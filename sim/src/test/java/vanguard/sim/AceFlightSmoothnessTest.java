package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Step F6s (round 78, B.1: "the movement of the other crafts is not very natural"; design/enemies/bosses/honour-guard):
 * the aces fly like aircraft ({@link AceFlight}). Over five played medium duels on the fixtures ({@link AceTrace}): the
 * nose never turns faster than {@link AceFlight#NOSE_RATE} (no snap); the flown motion never turns a corner (at most
 * {@link #MOST_TURN}° in a step, where the F1a flight turned up to 180°) nor changes its speed by a step (at most {@link
 * #MOST_SPEED_STEP} px/s a step, where it jumped by thousands); an ace never parks without aiming (its holds are orbits);
 * the exact solver behind the avoidance seldom shoves an ace and never far; the leader on his orbit keeps his band. It
 * prints the measures for the report. Step F6s4: its own flight (the steps the exact solver did not shove it) changes its
 * speed eased and it aims without sliding under its nose; the group shoves (two aces' motion changed alike) are
 * printed. Step R79b2 (round 79 H.1; the main agent's decisions): the separation, the keep-offs and the bands steer, so
 * the aces' parks, shoves, close passes and holds are rates over the seeds (never one seed's hard limit); the hulls
 * never come nearer than the exact solver's floor.
 */
class AceFlightSmoothnessTest {
    /** The flown motion's most turn in one step above 60 px/s, degrees (the joins' and arcs' turn plus the avoidance's). */
    static final double MOST_TURN = 25;
    /** The flown motion's most speed change in one step, px/s (the eased speed plus the avoidance's offset). */
    static final double MOST_SPEED_STEP = 30;
    /** The exact solver's most shove in one step, px, and the most steps (a share of the aces') it shoves over 5 px. */
    static final double MOST_SHOVE = 24;

    static final double MOST_SHOVING = 0.05;
    /** Step R79b2: the share of the leader's steps the solver shoves him over 5 px (the others yield to him). */
    static final double LEADER_SHOVING = 0.02;
    /** The most steps (a share of the aces') they hover without aiming. */
    static final double MOST_PARKED = 0.01;
    /**
     * Step R79b2 (decision 2: the 80 px hull rule steers, rare and brief transits nearer allowed): the most share of
     * the aces' steps their hull is nearer another's than the separation ...
     */
    static final double MOST_CLOSE = 0.15;
    /** ... and never nearer than the exact solver's floor, px. */
    static final double FLOOR = 80 - AceFlight.SOLVED_SLACK - 0.01;
    /**
     * Step R79b2 (decision 1): the least share of their orbits' steps the aces hold inside their bands (20 px slack;
     * the feinter's orbit lifts over a ship in its lane's half when it cannot cross: 3-6 % of the steps).
     */
    static final double LEAST_IN_BAND = 0.9;
    /**
     * Step F6s4: the most steps (a share of the aces') their own flight (the steps the exact solver did not shove it)
     * changes its flown speed by more than the easing allows ({@link AceTrace#EASED}).
     */
    static final double MOST_UNEASED = 0.0025;
    /**
     * Step F6s4: the most steps (a share of the aces') they slide across or against the nose by more than {@link
     * AceTrace#SLIDE} while aiming, unshoved (they aim by turning onto the ship: F6r's Harker slid as he aimed).
     */
    static final double MOST_AIM_SLIDING = 0.01;

    @Test
    void theAcesTurnAndChangeSpeedSmoothlyAndNeverJumpOrPark() {
        StringBuilder out = new StringBuilder("Honour Guard flight (fixture, medium, the paper's fire spread)\n");
        Rates rates = new Rates();
        java.util.List<AceTrace.Duel> duels = new java.util.ArrayList<>();
        for (long seed = 1; seed <= 5; seed++) {
            AceTrace.Duel duel = AceTrace.fly(Act6FSpecs.PLAYED, seed);
            duels.add(duel);
            out.append(String.format("seed %d: %.1f s, group shoves %d%n", seed, duel.seconds(), duel.groupShoves()));
            for (AceTrace.Ace ace : duel.aces()) {
                out.append("  ").append(ace).append('\n');
                rates.add(ace);
            }
        }
        System.out.println(out);
        System.out.println(rates);
        for (int d = 0; d < duels.size(); d++) {
            for (AceTrace.Ace ace : duels.get(d).aces()) {
                String at = "seed " + (d + 1) + ", " + ace.sign + ": ";
                assertTrue(ace.steps > SimStep.ticks(20), at + "flew");
                assertTrue(
                        ace.noseTurn <= Math.toDegrees(AceFlight.NOSE_RATE) + 1e-6,
                        at + "the nose turns at most its rate: " + ace.noseTurn);
                assertTrue(ace.motionTurn <= MOST_TURN, at + "no corner: " + ace.motionTurn);
                assertTrue(ace.speedStep <= MOST_SPEED_STEP, at + "no speed step: " + ace.speedStep);
                assertTrue(ace.mostShove <= MOST_SHOVE, at + "no jump: " + ace.mostShove);
                assertTrue(
                        ace.leastGap >= FLOOR, at + "the hulls never nearer than the solver's floor: " + ace.leastGap);
            }
        }
        rates.check("");
    }

    /**
     * Step R79b2 (round 79 H.1; the main agent's decisions: the separation and the bands steer, measured as rates over
     * the seeds, never a per-step stop): an ace's flight summed over duels.
     */
    static final class Rates {
        int steps;
        int bigShoves;
        int parked;
        int close;
        int unEasedOwn;
        int aimSlidesOwn;
        int holding;
        int inBand;

        void add(AceTrace.Ace ace) {
            steps += ace.steps;
            bigShoves += ace.bigShoves;
            parked += ace.parked;
            close += ace.close;
            unEasedOwn += ace.unEased - ace.unEasedShoved;
            aimSlidesOwn += ace.aimSlidesOwn;
            holding += ace.holding;
            inBand += ace.inBand;
        }

        double share(int n) {
            return (double) n / Math.max(1, steps);
        }

        void check(String at) {
            assertTrue(share(bigShoves) <= MOST_SHOVING, at + "seldom shoved: " + share(bigShoves));
            assertTrue(share(parked) <= MOST_PARKED, at + "never parked: " + share(parked));
            assertTrue(share(close) <= MOST_CLOSE, at + "the hulls 80 px apart but in transit: " + share(close));
            assertTrue(share(unEasedOwn) <= MOST_UNEASED, at + "its own speed changes eased: " + share(unEasedOwn));
            assertTrue(share(aimSlidesOwn) <= MOST_AIM_SLIDING, at + "aims without sliding: " + share(aimSlidesOwn));
            double band = (double) inBand / Math.max(1, holding);
            assertTrue(band >= LEAST_IN_BAND, at + "holding in its band: " + band);
        }

        @Override
        public String toString() {
            return String.format(
                    java.util.Locale.ROOT,
                    "over the seeds: shoved over 5 px %.2f %%, parked %.2f %%, under 80 px %.1f %%, uneased %.2f %%,"
                            + " aim slides %.2f %%, holding in band %.1f %%",
                    100 * share(bigShoves),
                    100 * share(parked),
                    100 * share(close),
                    100 * share(unEasedOwn),
                    100 * share(aimSlidesOwn),
                    100.0 * inBand / Math.max(1, holding));
        }
    }

    /**
     * Step F6s2 (round 78 follow-up: Harker was shoved on his way home, carried ahead of Kestrel's overhead run and its
     * way back down from its rake; on easy Kestrel held 120 px out of its band): in Level 40's bands over the three
     * difficulties ({@link Act6FSpecs#BANDED}), five played duels each: the leader seldom shoved, the aces holding in
     * their bands, the hulls apart but in transit, every flight as smooth as above (step R79b2: as rates over the seeds,
     * the bands and the separation steering; the leader's most shove a step bounded).
     */
    @Test
    void inTheSeparateBandsTheLeaderIsNeverShovedAndNoHoldingAceLeavesItsBand() {
        StringBuilder out =
                new StringBuilder("Honour Guard flight in the separate bands (fixtures, the paper's fire)\n");
        SetPieceSpec[] specs = {Act6FSpecs.BANDED_EASY, Act6FSpecs.BANDED, Act6FSpecs.BANDED_HARD};
        String[] names = {"easy", "medium", "hard"};
        for (int d = 0; d < specs.length; d++) {
            Rates rates = new Rates();
            Rates leader = new Rates();
            for (long seed = 1; seed <= 5; seed++) {
                AceTrace.Duel duel = AceTrace.fly(specs[d], seed);
                out.append(String.format(
                        "%s seed %d: %.1f s, group shoves %d%n", names[d], seed, duel.seconds(), duel.groupShoves()));
                for (AceTrace.Ace ace : duel.aces()) {
                    out.append("  ").append(ace).append('\n');
                    String at = names[d] + " seed " + seed + ", " + ace.sign + ": ";
                    if (ace.sign.equals("HARKER")) {
                        assertTrue(ace.mostShove <= MOST_SHOVE, at + "the leader's most shove: " + ace.mostShove);
                        leader.add(ace);
                    }
                    assertTrue(ace.leastGap >= FLOOR, at + "the solver's floor: " + ace.leastGap);
                    rates.add(ace);
                }
            }
            out.append(names[d]).append(' ').append(rates).append('\n');
            System.out.println(out);
            out.setLength(0);
            rates.check(names[d] + ": ");
            assertTrue(
                    leader.share(leader.bigShoves) <= LEADER_SHOVING,
                    names[d] + ": the leader seldom shoved: " + leader.share(leader.bigShoves));
        }
        System.out.println(out);
    }

    @Test
    void anAceAboutToFireFromItsStationTurnsItsNoseOntoTheShipAndCreeps() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD);
        rig.untilAllFly(50);
        Squadron squadron = rig.squadron;
        AceFlight flight = squadron.flight();
        int h = Act6FSpecs.HARKER;
        while (!squadron.ready(h)) {
            rig.step();
        }
        SquadronSpec.Station station = squadron.spec().aces().get(h).station();
        for (int i = 0; i < SimStep.ticks(2); i++) {
            flight.aim[h] = true;
            rig.step();
            assertTrue(squadron.y(h) >= station.yMin() - 5 && squadron.y(h) <= station.yMax() + 5, "in his band");
        }
        double off = Math.IEEEremainder(
                Enemy.heading(rig.shipX - squadron.x(h), rig.shipY - squadron.y(h)) - squadron.facing(h), 2 * Math.PI);
        assertTrue(Math.abs(off) < 1e-6, "his nose on the ship: " + off);
        assertTrue(flight.spd[h] <= AceFlight.AIM_SPEED + 1e-9, "creeping: " + flight.spd[h]);
        assertTrue(squadron.leg(h) == AceLeg.HOLD && squadron.ready(h), "still on his station");
        flight.aim[h] = false;
        for (int i = 0; i < SimStep.ticks(1); i++) {
            rig.step();
        }
        assertTrue(flight.spd[h] > 2 * AceFlight.AIM_SPEED, "back to his orbit's speed: " + flight.spd[h]);
    }

    @Test
    void theNoseLeadsAndTheHullBanksIntoTheTurns() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.GUARD);
        rig.untilAllFly(50);
        Squadron squadron = rig.squadron;
        double mostBank = 0;
        int along = 0;
        int moving = 0;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            rig.step();
            for (int a = 0; a < squadron.aceCount(); a++) {
                double vx = squadron.x(a) - squadron.flight().prevX[a];
                double vy = squadron.y(a) - squadron.flight().prevY[a];
                mostBank = Math.max(mostBank, Math.abs(squadron.bank(a)));
                assertTrue(Math.abs(squadron.bank(a)) <= AceFlight.MAX_BANK + 1e-9, "the bank's most");
                if (Math.hypot(vx, vy) * SimStep.PER_SECOND > 60) {
                    moving++;
                    double off = Math.IEEEremainder(Enemy.heading(vx, vy) - squadron.facing(a), 2 * Math.PI);
                    along += Math.abs(off) < Math.toRadians(30) ? 1 : 0;
                }
            }
        }
        assertTrue(mostBank > 0.3, "the hulls bank in their orbits' turns: " + mostBank);
        assertTrue(along >= 0.95 * moving, "the nose along the way: " + along + " of " + moving);
    }
}
