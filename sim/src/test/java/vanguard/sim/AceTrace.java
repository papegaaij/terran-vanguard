package vanguard.sim;

import java.util.Locale;

/**
 * Step F6s (round 78, B.1: "the movement of the other crafts is not very natural"): traces each ace of a played duel on
 * the fixtures (the paper's fire spread over the aces on the field, the ship weaving across the bottom) and measures how
 * it flies: the nose's turn a second, its flown motion's (its path's and its avoidance's, before the exact solver) turn
 * per step and speed change per step, the steps it moved sideways or backwards against its nose, the steps it hovered
 * without aiming, the exact solver's shoves after the avoidance, and the leader's steps out of his band on his station.
 * Step F6s4 (F6r's recording: hard stops, a stall into each hold, the pincer's group shove, the leader sliding as he
 * aimed): its flown motion as seen (after the exact solver) too: its speed changes a step over the easing, its slides
 * across or against its nose (aiming or not), and the steps two aces' motion changed alike (a group shove).
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
        /**
         * Step R79b2: the steps its hull was nearer another's than the separation (the rule steers: a goal over the
         * seeds), and the steps it was inside its keep-off from the ship.
         */
        int close;

        int inKeep;
        /** Step R79b2: the steps it held its station's orbit and of them inside its band (20 px slack). */
        int holding;

        int inBand;
        /**
         * Step F6s2: the most px it was outside its band while holding (its orbit, aiming, its way back onto it) or
         * sliding, once inside it since its last other move (the feinter's crossing over the ship leaves it on purpose).
         */
        double bandMost;
        /**
         * Step F6s4: its flown motion's (after the exact solver: what is seen) most speed change in one step, px/s, the
         * steps above {@link #EASED}, and the most speed it slid across or against its nose while moving, px/s, and the
         * steps it slid over {@link #SLIDE}.
         */
        double flownStep;

        int unEased;
        /** Step F6s4: of those, the steps the exact solver shoved it (over half a px). */
        int unEasedShoved;

        double slideMost;
        int slides;
        /** Step F6s4: of those, the steps it aimed (its nose on the ship). */
        int aimSlides;
        /** Step F6s4: of those, the steps the exact solver did not shove it (its own flight's). */
        int aimSlidesOwn;
        /** Step F6s4: when its most speed change and its most slide were, s from the sting. */
        double flownStepAt;

        double slideAt;

        Ace(String sign) {
            this.sign = sign;
        }

        @Override
        public String toString() {
            return String.format(
                    Locale.ROOT,
                    "%-7s %5.1f s: against the nose >45° %4.1f %% (>120° %4.1f %%), parked %4.1f %%, nose ≤ %4.0f°/s,"
                            + " motion turn ≤ %4.1f° a step, speed step ≤ %4.1f px/s, exact shoves %d steps > 1 px"
                            + " (%d > 5 px, ≤ %4.1f px), out of band %4.1f s ≤ %3.0f px, gap ≥ %4.1f, held ≤ %3.0f px out;"
                            + " flown speed step ≤ %5.1f px/s (at %5.1f s, %d steps over the easing, %d shoved), slide ≤ %5.1f px/s"
                            + " (at %5.1f s, %d steps over %2.0f, %d aiming, %d of them unshoved); close %4.1f %%,"
                            + " in its keep-off %4.1f %%, in band %5.1f %%",
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
                    leastGap,
                    bandMost,
                    flownStep,
                    flownStepAt,
                    unEased,
                    unEasedShoved,
                    slideMost,
                    slideAt,
                    slides,
                    SLIDE,
                    aimSlides,
                    aimSlidesOwn,
                    100.0 * close / Math.max(1, steps),
                    100.0 * inKeep / Math.max(1, steps),
                    100.0 * inBand / Math.max(1, holding));
        }
    }

    /**
     * Step F6s4: the most a flown speed changes in a step, px/s: its eased speed's ({@link AceFlight#ACCEL}) and its
     * avoidance's offset's ({@link AceFlight#PUSH_ACCEL}) a step, with a margin for an offset's turn with its path.
     */
    static final double EASED = (AceFlight.ACCEL + AceFlight.PUSH_ACCEL) * SimStep.SECONDS + 5;
    /** Step R79b2: an ace on its orbit this far out of its band, px, still counts as in it. */
    static final double BAND_SLACK = 20;
    /** Step F6s4: the most it may slide across or against its nose, px/s. */
    static final double SLIDE = 30;
    /** Step F6s4: a group shove: two or more aces' flown motion changed alike by more than this in a step, px/s. */
    static final double GROUP = 30;

    /** One duel's aces, its length, s from the sting, and (step F6s4) its group shoves (steps). */
    record Duel(Ace[] aces, double seconds, int groupShoves) {}

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
        double[] lastVx = new double[n];
        double[] lastVy = new double[n];
        double[] dvx = new double[n];
        double[] dvy = new double[n];
        boolean[] flew = new boolean[n];
        int group = 0;
        boolean[] was = new boolean[n];
        boolean[] settled = new boolean[n];
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
                return new Duel(aces, rig.piece.bossSeconds(), group);
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
                dvx[a] = dvy[a] = Double.NaN;
                if (squadron.state(a) != Squadron.State.FLYING) {
                    // Step F6s2: its pushes while it entered are not a shove on the field.
                    lastPushed[a] = flight.pushed[a];
                    was[a] = false;
                    flew[a] = false;
                    settled[a] = false;
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
                if (speed > HOVER) {
                    // Step F6s4: across or against its nose.
                    double nx = -Math.sin(facing);
                    double ny = -Math.cos(facing);
                    double slide = Math.max(Math.abs(vx * ny - vy * nx), -(vx * nx + vy * ny));
                    if (slide > ace.slideMost) {
                        ace.slideMost = slide;
                        ace.slideAt = t;
                    }
                    ace.slides += slide > SLIDE ? 1 : 0;
                    ace.aimSlides += slide > SLIDE && flight.aiming[a] ? 1 : 0;
                    ace.aimSlidesOwn += slide > SLIDE && flight.aiming[a] && !(shove > 0.5) ? 1 : 0;
                }
                if (flew[a]) {
                    double step = Math.abs(speed - Math.hypot(lastVx[a], lastVy[a]));
                    if (step > ace.flownStep) {
                        ace.flownStep = step;
                        ace.flownStepAt = t;
                    }
                    ace.unEased += step > EASED ? 1 : 0;
                    ace.unEasedShoved += step > EASED && shove > 0.5 ? 1 : 0;
                    dvx[a] = vx - lastVx[a];
                    dvy[a] = vy - lastVy[a];
                }
                flew[a] = true;
                lastVx[a] = vx;
                lastVy[a] = vy;
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
                boolean near = false;
                for (int b = 0; b < n; b++) {
                    if (b != a && squadron.state(b) == Squadron.State.FLYING) {
                        ace.leastGap = Math.min(ace.leastGap, squadron.gap(a, b));
                        near |= squadron.gap(a, b) < squadron.spec().separation() - 0.5;
                    }
                }
                ace.close += near ? 1 : 0;
                ace.inKeep += squadron.keepOff(a) > 0
                                && Math.hypot(flight.x[a] - rig.shipX, flight.y[a] - rig.shipY)
                                        < squadron.keepOff(a) - 0.5
                        ? 1
                        : 0;
                SquadronSpec.Station station = squadron.spec().aces().get(a).station();
                boolean crossing = squadron.spec().aces().get(a).role() == SquadronSpec.Role.FEINTER
                        && squadron.leg(a) == AceLeg.APPROACH;
                boolean held = !crossing && (squadron.move(a) == AceMove.HOLD || squadron.move(a) == AceMove.SLIDE);
                double beyond = Math.max(station.yMin() - flight.y[a], flight.y[a] - station.yMax());
                if (!held) {
                    settled[a] = false;
                } else if (beyond <= 0.5) {
                    settled[a] = true;
                } else if (settled[a]) {
                    ace.bandMost = Math.max(ace.bandMost, beyond);
                }
                boolean onStation = squadron.move(a) == AceMove.HOLD && squadron.leg(a) == AceLeg.HOLD;
                if (onStation) {
                    ace.holding++;
                    ace.inBand += beyond <= BAND_SLACK ? 1 : 0;
                }
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
            group += groupShove(dvx, dvy) ? 1 : 0;
        }
        return new Duel(aces, Double.NaN, group);
    }

    /**
     * Step F6s4: whether two aces' flown motion changed alike this step, each by over {@link #GROUP} px/s (their
     * changes within a quarter of each other: shoved together, as the pincer's chain did at Level 40's 126.25 s).
     */
    static boolean groupShove(double[] dvx, double[] dvy) {
        for (int i = 0; i < dvx.length; i++) {
            for (int j = i + 1; j < dvx.length; j++) {
                double di = Math.hypot(dvx[i], dvy[i]);
                double dj = Math.hypot(dvx[j], dvy[j]);
                if (di > GROUP
                        && dj > GROUP
                        && Math.hypot(dvx[i] - dvx[j], dvy[i] - dvy[j]) < 0.25 * Math.max(di, dj)) {
                    return true;
                }
            }
        }
        return false;
    }
}
