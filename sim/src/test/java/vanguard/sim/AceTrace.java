package vanguard.sim;

import java.util.Locale;

/**
 * Step F6s (round 78, B.1: "the movement of the other crafts is not very natural"): traces each ace of a played duel on
 * the fixtures (the paper's fire spread over the aces on the field, the ship weaving across the bottom) and measures how
 * it flies: the nose's turn a second, its flown motion's (its path's and its avoidance's, before the exact solver) turn
 * per step and speed change per step, the steps it moved sideways or backwards against its nose, the steps it hovered
 * without aiming, the exact solver's shoves after the avoidance, and the leader's steps out of his band on his station.
 */
final class AceTrace {
    /** The paper's effective fire, a second (as {@link SquadronDuelMeasureTest}). */
    static final double DPS = 0.6 * 160 * 0.7;
    /** A step's motion counts as a move above this speed, px/s (below: hovering). */
    static final double HOVER = 15;
    /** The flown motion's turn is read above this speed, px/s. */
    static final double TURN_SPEED = 60;

    /** One ace's flight over a duel. */
    static final class Ace {
        final String sign;
        int steps;
        /** The steps it moved (above {@link #HOVER}) and those of them against its nose by over 45° and over 120°. */
        int moving;

        int sideways;
        int backwards;
        /** The steps it hovered (below {@link #HOVER}) while not aiming. */
        int parked;
        /** The most nose turn, degrees/s. */
        double noseTurn;
        /** The flown motion's most turn in one step, degrees (above {@link #TURN_SPEED}), and its most speed change. */
        double motionTurn;

        double speedStep;
        /** The exact solver's shoves: steps over 1 px, over 5 px, the most, px. */
        int shoves;

        int bigShoves;
        double mostShove;
        /** The leader on his station: steps outside his band and the most, px. */
        int outOfBand;

        double outMost;
        /** The least gap to another ace's hull, px. */
        double leastGap = Double.POSITIVE_INFINITY;

        Ace(String sign) {
            this.sign = sign;
        }

        @Override
        public String toString() {
            return String.format(
                    Locale.ROOT,
                    "%-7s %5.1f s: against the nose >45° %4.1f %% (>120° %4.1f %%), parked %4.1f %%, nose ≤ %4.0f°/s,"
                            + " motion turn ≤ %4.1f° a step, speed step ≤ %4.1f px/s, exact shoves %d steps > 1 px"
                            + " (%d > 5 px, ≤ %4.1f px), out of band %4.1f s ≤ %3.0f px, gap ≥ %4.1f",
                    sign,
                    steps * SimStep.SECONDS,
                    100.0 * sideways / Math.max(1, moving),
                    100.0 * backwards / Math.max(1, moving),
                    100.0 * parked / Math.max(1, steps),
                    noseTurn,
                    motionTurn,
                    speedStep,
                    shoves,
                    bigShoves,
                    mostShove,
                    outOfBand * SimStep.SECONDS,
                    outMost,
                    leastGap);
        }
    }

    /** One duel's aces and its length, s from the sting. */
    record Duel(Ace[] aces, double seconds) {}

    private AceTrace() {}

    static Duel fly(SetPieceSpec spec, long seed) {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(spec, seed);
        rig.recording = false;
        Squadron squadron = rig.squadron;
        AceFlight flight = squadron.flight();
        int n = squadron.aceCount();
        Ace[] aces = new Ace[n];
        double[] lastMx = new double[n];
        double[] lastMy = new double[n];
        double[] lastFacing = new double[n];
        double[] lastPushed = new double[n];
        boolean[] was = new boolean[n];
        for (int a = 0; a < n; a++) {
            aces[a] = new Ace(squadron.callSign(a));
        }
        double per = DPS * SimStep.SECONDS;
        for (int i = 0; i < SimStep.ticks(240); i++) {
            double t = i * SimStep.SECONDS;
            rig.shipX = 240 + 170 * Math.sin(t * 0.6 + seed);
            rig.shipY = 90 + 50 * Math.sin(t * 1.1);
            rig.step();
            if (squadron.deaths() == n) {
                return new Duel(aces, rig.piece.bossSeconds());
            }
            int on = 0;
            for (int a = 0; a < n; a++) {
                on += squadron.touchable(a) ? 1 : 0;
            }
            for (int a = 0; a < n; a++) {
                if (squadron.touchable(a)) {
                    rig.piece.damagePartFlat(squadron.part(a), per / on);
                }
            }
            for (int a = 0; a < n; a++) {
                if (squadron.state(a) != Squadron.State.FLYING) {
                    was[a] = false;
                    continue;
                }
                Ace ace = aces[a];
                double vx = (flight.x[a] - flight.prevX[a]) * SimStep.PER_SECOND;
                double vy = (flight.y[a] - flight.prevY[a]) * SimStep.PER_SECOND;
                double speed = Math.hypot(vx, vy);
                double facing = flight.facing[a];
                double mx = flight.tanX[a] * flight.spd[a] + flight.ovX[a];
                double my = flight.tanY[a] * flight.spd[a] + flight.ovY[a];
                double shove = flight.pushed[a] - lastPushed[a];
                lastPushed[a] = flight.pushed[a];
                ace.steps++;
                if (speed > HOVER) {
                    ace.moving++;
                    double against = Math.abs(Math.IEEEremainder(Enemy.heading(vx, vy) - facing, 2 * Math.PI));
                    ace.sideways += against > Math.PI / 4 ? 1 : 0;
                    ace.backwards += against > 2 * Math.PI / 3 ? 1 : 0;
                } else if (!flight.aiming[a]) {
                    ace.parked++;
                }
                if (was[a]) {
                    double nose = Math.abs(Math.IEEEremainder(facing - lastFacing[a], 2 * Math.PI));
                    ace.noseTurn = Math.max(ace.noseTurn, Math.toDegrees(nose) * SimStep.PER_SECOND);
                    double m = Math.hypot(mx, my);
                    double lastM = Math.hypot(lastMx[a], lastMy[a]);
                    if (m > TURN_SPEED && lastM > TURN_SPEED) {
                        double turn = Math.abs(Math.IEEEremainder(
                                Enemy.heading(mx, my) - Enemy.heading(lastMx[a], lastMy[a]), 2 * Math.PI));
                        ace.motionTurn = Math.max(ace.motionTurn, Math.toDegrees(turn));
                    }
                    ace.speedStep = Math.max(ace.speedStep, Math.abs(m - lastM));
                }
                ace.shoves += shove > 1 ? 1 : 0;
                ace.bigShoves += shove > 5 ? 1 : 0;
                ace.mostShove = Math.max(ace.mostShove, shove);
                for (int b = 0; b < n; b++) {
                    if (b != a && squadron.state(b) == Squadron.State.FLYING) {
                        ace.leastGap = Math.min(ace.leastGap, squadron.gap(a, b));
                    }
                }
                SquadronSpec.Station station = squadron.spec().aces().get(a).station();
                boolean onStation = squadron.move(a) == AceMove.HOLD && squadron.leg(a) == AceLeg.HOLD;
                if (squadron.spec().aces().get(a).role() == SquadronSpec.Role.SNIPER && onStation) {
                    double out = Math.max(station.yMin() - flight.y[a], flight.y[a] - station.yMax());
                    if (out > 0.5) {
                        ace.outOfBand++;
                        ace.outMost = Math.max(ace.outMost, out);
                    }
                }
                was[a] = true;
                lastMx[a] = mx;
                lastMy[a] = my;
                lastFacing[a] = facing;
            }
        }
        return new Duel(aces, Double.NaN);
    }
}
