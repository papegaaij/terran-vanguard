package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.AceLeg;
import vanguard.sim.AceMove;
import vanguard.sim.SetPiece;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;
import vanguard.sim.SquadronSpec;

/**
 * Step R79b2 (round 79 H.1, on the duel's recording: "they take way too many sharp turns. The turns should be smoother
 * and wider. This will make the pilot seasick in seconds"): the aces' turns as seen in Level 40's duel (medium, the
 * plan's fit with Rook, the autopilot; {@link #SEEDS} seeds), measured from their flown places (after every push) and
 * their noses. A turn is a run of steps turning one way faster than {@link #TURNING}°/s (the heading over {@link #W}
 * steps either side, above {@link #MOVING} px/s), counted from {@link #TURN}°; it is sharp from {@link #SHARP}° at a
 * radius under {@link #SHARP_RADIUS} px, a reversal when it turns the other way from the turn before. Before the step
 * (F6s4's flight, HEAD of round 79): 40–77 sharp turns and 22–52 reversals a minute an ace, turn radii down to 2 px,
 * the nose at up to 8 radians/s. Besides, the intent of the flight's other rules, as rates over the seeds: the aces
 * hover seldom, hold in their bands, keep 80 px apart but for brief transits, and the feinter never hovers over the
 * ship.
 */
class HonourGuardTurnsTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final long SEEDS = 4;
    /** The heading's window either side, steps (0.1 s), and the least speed it is read at, px/s. */
    static final int W = 6;

    static final double MOVING = 40;
    /** A turn: turning faster than this, °/s, by this much at least, °; sharp from this much at a radius under this. */
    static final double TURNING = 30;

    static final double TURN = 30;
    static final double SHARP = 45;
    static final double SHARP_RADIUS = 60;
    /** The least turn radius any ace flies, px (the flight's least turn, {@code AceFlight.TURN_RADIUS}). */
    static final double LEAST_RADIUS = 45;
    /**
     * The share of an ace's turning samples that may read tighter, averaged over the seeds: the exact solver's rare
     * shoves at its floors (two hulls {@code AceFlight.SOLVED_SLACK} under the separation, the ship's ring), the
     * ship's contact pushes.
     */
    static final double TIGHT_SHARE = 0.03;
    /** The nose's most turn, radians/s ({@code AceFlight.NOSE_RATE}). */
    static final double NOSE = 2.6;
    /** The most sharp turns and reversals a minute an ace, averaged (well under HEAD's 40–77 and 22–52). */
    static final double MOST_SHARP = 15;

    /**
     * M10 round 80 fixes, R80a: 16, not 15: Rook's Act 6 berth off death-burst units and told dashes changed his way
     * through the duel and with it the seeds' flights (Kestrel 15.1 averaged, one seed 21.5); the aces' flight unchanged.
     */
    static final double MOST_REVERSALS = 16;
    /** The most share of an ace's time it hovers (under {@link #HOVER} px/s), averaged. */
    static final double MOST_HOVERING = 0.03;

    static final double HOVER = 15;
    /**
     * The least share of its holding time (its station's orbit) the leader and the dancer fly inside their bands (20 px
     * slack), averaged; the feinter's orbit lifts over a ship in its lane's half when it cannot cross (its rate below).
     */
    static final double LEAST_IN_BAND = 0.95;

    static final double BAND_SLACK = 20;
    /**
     * Decision 2 (the separation steers): the most times a minute two hulls come nearer than 80 px (a run of steps one
     * time), the most share of the duel they are, and the deepest, px (the exact solver's floor).
     */
    static final double MOST_CLOSE = 12;

    static final double MOST_CLOSE_TIME = 0.15;
    static final double DEEPEST = 40.01;
    /** The most share of the duel an ace is inside its keep-off from the ship (the keep-offs steer too). */
    static final double MOST_IN_KEEP = 0.25;
    /** The ship's no-spawn ring, px: no ace ever inside it (never a ram; {@code AceFlight.SHIP_RING}). */
    static final double SHIP_RING = 72 - 0.01;
    /** A deep close pass, px under the separation. */
    static final double DEEP = 20;
    /**
     * The most share of its time the feinter hovers over the ship (within its hull's width of it, slower than 60),
     * averaged (HEAD's hovered there 20–27 s of a 60 s duel).
     */
    static final double MOST_OVER_SHIP = 0.08;

    /** One ace's flight over one duel. */
    static final class Ace {
        final String sign;
        boolean feinter;
        final List<double[]> at = new ArrayList<>();
        final List<String> tags = new ArrayList<>();
        int steps;
        int hovering;
        int holding;
        int inBand;
        int overShip;

        Ace(String sign) {
            this.sign = sign;
        }
    }

    /** One duel's aces and its close passes. */
    static final class Duel implements LevelMetrics.Probe {
        final long seed;
        Ace[] aces = new Ace[0];
        double steps;
        int close;
        double deepest;
        int closeSteps;
        int deepSteps;
        int keepSteps;
        double keepDeepest;
        /** The nearest the dancer or the feinter came to the ship, px. */
        double nearest = Double.POSITIVE_INFINITY;

        private boolean wasClose;

        Duel(long seed) {
            this.seed = seed;
        }

        @Override
        public void newAttempt() {
            aces = new Ace[0];
            steps = 0;
            close = 0;
            deepest = 0;
            wasClose = false;
        }

        @Override
        public void bossRetry() {
            newAttempt();
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SetPiece guard = GuardPlay.squadronBoss(sortie);
            if (guard == null) {
                return;
            }
            Squadron s = guard.squadron().orElseThrow();
            if (aces.length == 0) {
                aces = new Ace[s.aceCount()];
                for (int a = 0; a < aces.length; a++) {
                    aces[a] = new Ace(s.callSign(a));
                    aces[a].feinter = s.spec().aces().get(a).role() == SquadronSpec.Role.FEINTER;
                }
            }
            steps++;
            boolean near = false;
            double sx = sortie.ship().x();
            double sy = sortie.ship().y();
            for (int a = 0; a < aces.length; a++) {
                Ace ace = aces[a];
                boolean on = s.state(a) == Squadron.State.FLYING && !s.tumbling(a);
                ace.at.add(on ? new double[] {s.x(a), s.y(a), s.facing(a)} : null);
                ace.tags.add(on ? s.move(a) + "/" + s.leg(a) : "");
                if (!on) {
                    continue;
                }
                ace.steps++;
                int n = ace.at.size();
                double[] last = n > 1 ? ace.at.get(n - 2) : null;
                double speed =
                        last == null ? Double.NaN : Math.hypot(s.x(a) - last[0], s.y(a) - last[1]) * SimStep.PER_SECOND;
                ace.hovering += speed < HOVER ? 1 : 0;
                SquadronSpec.Ace spec = s.spec().aces().get(a);
                if (s.move(a) == AceMove.HOLD && s.leg(a) == AceLeg.HOLD) {
                    ace.holding++;
                    SquadronSpec.Station station = spec.station();
                    ace.inBand +=
                            s.y(a) >= station.yMin() - BAND_SLACK && s.y(a) <= station.yMax() + BAND_SLACK ? 1 : 0;
                }
                if (spec.role() == SquadronSpec.Role.FEINTER
                        && Math.abs(s.x(a) - sx) < 60
                        && s.y(a) > sy
                        && speed < 60) {
                    ace.overShip++;
                }
                for (int b = a + 1; b < aces.length; b++) {
                    if (s.state(b) == Squadron.State.FLYING && !s.tumbling(b)) {
                        double gap = s.gap(a, b);
                        if (gap < s.spec().separation() - 0.5) {
                            near = true;
                            deepest = Math.max(deepest, s.spec().separation() - gap);
                            deepSteps += gap < s.spec().separation() - DEEP ? 1 : 0;
                        }
                    }
                }
            }
            close += near && !wasClose ? 1 : 0;
            closeSteps += near ? 1 : 0;
            boolean inKeep = false;
            for (int a = 0; a < aces.length; a++) {
                if (s.state(a) == Squadron.State.FLYING && !s.tumbling(a) && s.keepOff(a) > 0) {
                    double toShip = Math.hypot(s.x(a) - sx, s.y(a) - sy);
                    nearest = Math.min(nearest, toShip);
                    double in = s.keepOff(a) - toShip;
                    inKeep |= in > 0.5;
                    keepDeepest = Math.max(keepDeepest, in);
                }
            }
            keepSteps += inKeep ? 1 : 0;
            wasClose = near;
        }

        double minutes() {
            return steps * SimStep.SECONDS / 60;
        }
    }

    /** One ace's turns over one duel. */
    record Turns(
            String sign,
            double minutes,
            double sharp,
            double reversals,
            double turns,
            double tightShare,
            double leastRadius,
            double nose,
            double hovering,
            double inBand,
            double overShip,
            String where) {
        @Override
        public String toString() {
            return String.format(
                    Locale.ROOT,
                    "%-7s %4.2f min: sharp %4.1f/min, reversals %4.1f/min, turns %4.1f/min, under %2.0f px %4.2f %%"
                            + " (least %4.1f), nose ≤ %4.2f rad/s, hovering %4.2f %%, in band %5.1f %%, over the ship"
                            + " %4.2f %%%s",
                    sign,
                    minutes,
                    sharp,
                    reversals,
                    turns,
                    LEAST_RADIUS,
                    100 * tightShare,
                    leastRadius,
                    nose,
                    100 * hovering,
                    100 * inBand,
                    100 * overShip,
                    where);
        }
    }

    static Turns turns(Ace ace) {
        List<double[]> p = ace.at;
        int m = p.size();
        double dt = SimStep.SECONDS;
        double[] omega = new double[m];
        double[] radius = new double[m];
        java.util.Arrays.fill(omega, Double.NaN);
        java.util.Arrays.fill(radius, Double.NaN);
        int samples = 0;
        int tight = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int i = W; i + W < m; i++) {
            boolean whole = true;
            double len = 0;
            for (int k = i - W; k < i + W && whole; k++) {
                whole = p.get(k) != null && p.get(k + 1) != null;
                len += whole ? Math.hypot(p.get(k + 1)[0] - p.get(k)[0], p.get(k + 1)[1] - p.get(k)[1]) : 0;
            }
            if (!whole) {
                continue;
            }
            double[] a0 = p.get(i - W);
            double[] a1 = p.get(i - W + 1);
            double[] b0 = p.get(i + W - 1);
            double[] b1 = p.get(i + W);
            if (Math.hypot(a1[0] - a0[0], a1[1] - a0[1]) / dt < MOVING
                    || Math.hypot(b1[0] - b0[0], b1[1] - b0[1]) / dt < MOVING) {
                continue;
            }
            double dh = Math.IEEEremainder(
                    Math.atan2(b1[1] - b0[1], b1[0] - b0[0]) - Math.atan2(a1[1] - a0[1], a1[0] - a0[0]), 2 * Math.PI);
            omega[i] = Math.toDegrees(dh) / ((2 * W - 1) * dt);
            samples++;
            if (Math.abs(dh) > Math.toRadians(5)) {
                radius[i] = len / Math.abs(dh);
                least = Math.min(least, radius[i]);
                tight += radius[i] < LEAST_RADIUS ? 1 : 0;
            }
        }
        int sharp = 0;
        int turns = 0;
        int reversals = 0;
        int lastDir = 0;
        StringBuilder where = new StringBuilder();
        for (int i = 0; i < m; ) {
            if (Double.isNaN(omega[i]) || Math.abs(omega[i]) <= TURNING) {
                i++;
                continue;
            }
            int dir = omega[i] > 0 ? 1 : -1;
            double total = 0;
            double tightest = Double.POSITIVE_INFINITY;
            int start = i;
            while (i < m && !Double.isNaN(omega[i]) && Math.abs(omega[i]) > TURNING && omega[i] * dir > 0) {
                total += Math.abs(omega[i]) * SimStep.SECONDS;
                if (!Double.isNaN(radius[i])) {
                    tightest = Math.min(tightest, radius[i]);
                }
                i++;
            }
            if (total >= TURN) {
                turns++;
                reversals += lastDir != 0 && dir != lastDir ? 1 : 0;
                lastDir = dir;
                if (total >= SHARP && tightest < SHARP_RADIUS) {
                    sharp++;
                    if (where.length() < 400) {
                        where.append(String.format(Locale.ROOT, " %.1f %s;", start * dt, ace.tags.get(start)));
                    }
                }
            }
        }
        double nose = 0;
        for (int k = 1; k < m; k++) {
            if (p.get(k) != null && p.get(k - 1) != null) {
                nose = Math.max(
                        nose,
                        Math.abs(Math.IEEEremainder(p.get(k)[2] - p.get(k - 1)[2], 2 * Math.PI)) * SimStep.PER_SECOND);
            }
        }
        double minutes = ace.steps * dt / 60;
        return new Turns(
                ace.sign,
                minutes,
                sharp / minutes,
                reversals / minutes,
                turns / minutes,
                samples == 0 ? 0 : (double) tight / samples,
                least,
                nose,
                (double) ace.hovering / Math.max(1, ace.steps),
                (double) ace.inBand / Math.max(1, ace.holding),
                (double) ace.overShip / Math.max(1, ace.steps),
                where.length() == 0 ? "" : "; sharp at" + where);
    }

    static Duel fly(long seed) {
        Sortie sortie = LevelMetrics.sortie(
                CONTENT,
                Level40Test.LEVEL,
                Difficulty.MEDIUM,
                Level40Test.planLoadout(CONTENT, Difficulty.MEDIUM),
                seed);
        Duel duel = new Duel(seed);
        GuardWatch watch = new GuardWatch(seed);
        LevelMetrics.fly(sortie, Level40Test.MAX_STEPS, watch, duel);
        System.out.printf(Locale.ROOT, "Honour Guard turns, seed %d: %s%n", seed, watch.duel());
        return duel;
    }

    @Test
    void theAcesTurnInWideSmoothArcs() {
        List<Duel> duels = LongStream.rangeClosed(1, SEEDS)
                .parallel()
                .mapToObj(HonourGuardTurnsTest::fly)
                .toList();
        int n = duels.getFirst().aces.length;
        double[] sharp = new double[n];
        double[] reversals = new double[n];
        double[] tight = new double[n];
        double[] hovering = new double[n];
        double[] inBand = new double[n];
        double[] overShip = new double[n];
        double nose = 0;
        double minutes = 0;
        int close = 0;
        double deepest = 0;
        double closeTime = 0;
        double inKeep = 0;
        double nearest = Double.POSITIVE_INFINITY;
        StringBuilder out = new StringBuilder("Honour Guard turns (Level 40, medium, the plan's fit with Rook)\n");
        for (Duel duel : duels) {
            out.append(String.format(
                    Locale.ROOT,
                    "seed %d: %.1f s, hulls under 80 px %d times, %.1f %% of the time (deepest %.1f px, %.1f %% over"
                            + " %.0f px deep); inside a keep-off %.1f %% of the time (deepest %.1f px)%n",
                    duel.seed,
                    duel.minutes() * 60,
                    duel.close,
                    100 * duel.closeSteps / duel.steps,
                    duel.deepest,
                    100 * duel.deepSteps / duel.steps,
                    DEEP,
                    100 * duel.keepSteps / duel.steps,
                    duel.keepDeepest));
            minutes += duel.minutes();
            close += duel.close;
            deepest = Math.max(deepest, duel.deepest);
            closeTime += duel.closeSteps / duel.steps;
            inKeep += duel.keepSteps / duel.steps;
            nearest = Math.min(nearest, duel.nearest);
            for (int a = 0; a < n; a++) {
                Turns t = turns(duel.aces[a]);
                out.append("  ").append(t).append('\n');
                sharp[a] += t.sharp() / SEEDS;
                reversals[a] += t.reversals() / SEEDS;
                tight[a] += t.tightShare() / SEEDS;
                hovering[a] += t.hovering() / SEEDS;
                inBand[a] += t.inBand() / SEEDS;
                overShip[a] += t.overShip() / SEEDS;
                nose = Math.max(nose, t.nose());
            }
        }
        out.append(
                String.format(Locale.ROOT, "close passes %.2f a minute, deepest %.1f px%n", close / minutes, deepest));
        System.out.println(out);
        for (int a = 0; a < n; a++) {
            String sign = duels.getFirst().aces[a].sign + ": ";
            assertTrue(sharp[a] <= MOST_SHARP, sign + "sharp turns a minute: " + sharp[a]);
            assertTrue(reversals[a] <= MOST_REVERSALS, sign + "reversals a minute: " + reversals[a]);
            assertTrue(tight[a] <= TIGHT_SHARE, sign + "turns tighter than " + LEAST_RADIUS + " px: " + tight[a]);
            assertTrue(hovering[a] <= MOST_HOVERING, sign + "hovering: " + hovering[a]);
            if (!duels.getFirst().aces[a].feinter) {
                assertTrue(inBand[a] >= LEAST_IN_BAND, sign + "holding in its band: " + inBand[a]);
            }
            assertTrue(overShip[a] <= MOST_OVER_SHIP, sign + "over the ship: " + overShip[a]);
        }
        assertTrue(nose <= NOSE + 1e-6, "the nose's most turn: " + nose);
        assertTrue(close / minutes <= MOST_CLOSE, "close passes a minute: " + close / minutes);
        assertTrue(closeTime / SEEDS <= MOST_CLOSE_TIME, "the share of the duel close: " + closeTime / SEEDS);
        assertTrue(deepest <= DEEPEST, "the deepest close pass: " + deepest);
        assertTrue(inKeep / SEEDS <= MOST_IN_KEEP, "the share of the duel inside a keep-off: " + inKeep / SEEDS);
        assertTrue(nearest >= SHIP_RING, "never inside the ship's ring: " + nearest);
    }
}
