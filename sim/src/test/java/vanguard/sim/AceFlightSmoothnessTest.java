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
 * prints the measures for the report.
 */
class AceFlightSmoothnessTest {
    /** The flown motion's most turn in one step above 60 px/s, degrees (the joins' and arcs' turn plus the avoidance's). */
    static final double MOST_TURN = 25;
    /** The flown motion's most speed change in one step, px/s (the eased speed plus the avoidance's offset). */
    static final double MOST_SPEED_STEP = 30;
    /** The exact solver's most shove in one step, px, and the most steps (a share of an ace's) it shoves over 5 px. */
    static final double MOST_SHOVE = 24;

    static final double MOST_SHOVING = 0.05;
    /** The most steps (a share of an ace's) it hovers without aiming. */
    static final double MOST_PARKED = 0.01;
    /** The leader on his orbit at most this far out of his band, px (the exact solver's residue at its edge). */
    static final double MOST_OUT_OF_BAND = 5;

    @Test
    void theAcesTurnAndChangeSpeedSmoothlyAndNeverJumpOrPark() {
        StringBuilder out = new StringBuilder("Honour Guard flight (fixture, medium, the paper's fire spread)\n");
        for (long seed = 1; seed <= 5; seed++) {
            AceTrace.Duel duel = AceTrace.fly(Act6FSpecs.PLAYED, seed);
            out.append(String.format("seed %d: %.1f s%n", seed, duel.seconds()));
            for (AceTrace.Ace ace : duel.aces()) {
                out.append("  ").append(ace).append('\n');
                String at = "seed " + seed + ", " + ace.sign + ": ";
                assertTrue(ace.steps > SimStep.ticks(20), at + "flew");
                assertTrue(
                        ace.noseTurn <= Math.toDegrees(AceFlight.NOSE_RATE) + 1e-6,
                        at + "the nose turns at most its rate: " + ace.noseTurn);
                assertTrue(ace.motionTurn <= MOST_TURN, at + "no corner: " + ace.motionTurn);
                assertTrue(ace.speedStep <= MOST_SPEED_STEP, at + "no speed step: " + ace.speedStep);
                assertTrue(ace.mostShove <= MOST_SHOVE, at + "no jump: " + ace.mostShove);
                assertTrue(ace.bigShoves <= MOST_SHOVING * ace.steps, at + "seldom shoved: " + ace.bigShoves);
                assertTrue(ace.parked <= MOST_PARKED * ace.steps, at + "never parked: " + ace.parked);
                assertTrue(ace.outMost <= MOST_OUT_OF_BAND, at + "the leader keeps his band: " + ace.outMost);
                assertTrue(ace.leastGap >= 80 - 0.01, at + "the hulls 80 px apart: " + ace.leastGap);
            }
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
