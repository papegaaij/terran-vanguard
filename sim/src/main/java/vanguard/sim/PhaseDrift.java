package vanguard.sim;

import java.util.List;

/**
 * M11 part H, step H1a (design/enemies/bosses/choir-heart, phase 2: the drift; m11h-gaps.md §1 #4): distorted space inside
 * one boss phase while the scroll is halted ({@link PhaseDriftSpec}), kept by the level's {@link Current} (made for a
 * boss with a drift, a level without a current of its own too). A level's current runs on the level clock and the
 * scroll's step; the halted arena has neither, so this runs on the phase's own clock and a nominal arena drift speed:
 * the angle off its profile (smoothstep between the points), the drift that speed times sin θ times the reversal's
 * sign. Its one reversal is told ({@link SimEvents.Type#REVERSAL_TOLD}) at the death of the phase's {@code atKills}-th
 * part or {@code toldSeconds} before {@code atSeconds}, whichever comes first, and runs ({@link
 * SimEvents.Type#REVERSAL_RUN}) {@code toldSeconds} later: the drift eases through zero over 1 s, runs reversed, eases
 * back over 1 s. After the phase (or the boss's death) the angle eases to 0 over the ease out. Allocates nothing.
 */
public final class PhaseDrift {
    private final PhaseDriftSpec spec;
    private final SetPiece piece;
    /** The parts the phase ends on (its kills count toward the reversal). */
    private final int[] parts;

    private final int[] bendTicks;
    private final double[] bendDegrees;
    private final int toldTicks;
    private final int runTicks;
    private final int easeTicks;
    private final int latestTell;
    private final int easeOutTicks;
    /** The event value of its reversal (after the level's own reversals). */
    private final int index;

    // The state (hashed).
    /** Steps since its phase started; -1 before. */
    private int ticks;
    /** Steps since its phase ended (the ease out); -1 while it lasts or before. */
    private int outTicks;
    /** The step (on its clock) its reversal's tell started; -1 before. */
    private int toldAt;
    /** Whether the reversal's run was raised. */
    private boolean ran;
    /** The angle and the drift of this step. */
    private double degrees;

    private double drift;
    private double sign;

    PhaseDrift(PhaseDriftSpec spec, SetPiece piece, int index) {
        this.spec = spec;
        this.piece = piece;
        this.index = index;
        BossSpec boss = piece.boss().orElseThrow();
        if (spec.phase() >= boss.phases().size()) {
            throw new IllegalArgumentException(boss.barName() + ": its drift's phase is one of its phases");
        }
        List<Integer> until = boss.phases().get(spec.phase()).untilParts();
        parts = new int[until.size()];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = until.get(i);
        }
        int bends = spec.profile().size();
        bendTicks = new int[bends];
        bendDegrees = new double[bends];
        for (int i = 0; i < bends; i++) {
            bendTicks[i] = SimStep.ticks(spec.profile().get(i).t());
            bendDegrees[i] = spec.profile().get(i).degrees();
        }
        PhaseDriftSpec.Reversal reversal = spec.reversal().orElse(null);
        toldTicks = reversal == null ? 0 : SimStep.ticks(reversal.toldSeconds());
        easeTicks = SimStep.ticks(Current.EASE_SECONDS);
        runTicks = reversal == null ? 0 : SimStep.ticks(reversal.runSeconds()) + 2 * easeTicks;
        latestTell = reversal == null ? Integer.MAX_VALUE : SimStep.ticks(reversal.atSeconds()) - toldTicks;
        easeOutTicks = Math.max(1, SimStep.ticks(spec.easeOutSeconds()));
        reset();
    }

    /** Back to the level start: before its phase. */
    void reset() {
        ticks = -1;
        outTicks = -1;
        toldAt = -1;
        ran = false;
        degrees = 0;
        drift = 0;
        sign = 1;
    }

    /** A boss checkpoint's copy (its record) or restore of {@code other}'s state. */
    void copyFrom(PhaseDrift other) {
        ticks = other.ticks;
        outTicks = other.outTicks;
        toldAt = other.toldAt;
        ran = other.ran;
        degrees = other.degrees;
        drift = other.drift;
        sign = other.sign;
    }

    /** A copy for a boss checkpoint's record (made once, with the checkpoint). */
    PhaseDrift mark() {
        return new PhaseDrift(spec, piece, index);
    }

    /** One step: its phase's clock, the reversal's tell and run, the angle and the drift. */
    void step(SimEvents events) {
        boolean over = piece.destroyed() || (piece.engaged() && piece.phase() > spec.phase());
        if (ticks < 0) {
            if (over || !piece.engaged() || piece.phase() != spec.phase()) {
                return;
            }
            ticks = 0;
        } else if (outTicks >= easeOutTicks) {
            return;
        } else {
            ticks++;
        }
        if (over && outTicks < 0) {
            outTicks = 0;
        } else if (outTicks >= 0) {
            outTicks++;
        }
        if (spec.reversal().isPresent() && toldAt < 0 && outTicks < 0 && (ticks >= latestTell || killed())) {
            toldAt = ticks;
            events.add(SimEvents.Type.REVERSAL_TOLD, PlayField.WIDTH / 2.0, PlayField.HEIGHT / 2.0, index);
        }
        if (toldAt >= 0 && !ran && ticks >= toldAt + toldTicks) {
            ran = true;
            events.add(SimEvents.Type.REVERSAL_RUN, PlayField.WIDTH / 2.0, PlayField.HEIGHT / 2.0, index);
        }
        sign = sign();
        double out = outTicks < 0 ? 1 : 1 - ScrollPlan.smoothstep(Math.min(1.0, (double) outTicks / easeOutTicks));
        degrees = degreesAt(ticks) * out;
        drift = spec.speed() * SimStep.SECONDS * sign * Trig.sin(StrictMath.toRadians(degrees));
    }

    /** Whether at least {@code atKills} of the phase's parts are dead (never for 0). */
    private boolean killed() {
        int need = spec.reversal().orElseThrow().atKills();
        if (need == 0) {
            return false;
        }
        int dead = 0;
        for (int p : parts) {
            dead += piece.partWrecked(p) ? 1 : 0;
        }
        return dead >= need;
    }

    /** The reversal's factor now: 1, easing to -1 over 1 s from its run's start, -1, easing back. */
    private double sign() {
        if (!ran) {
            return 1;
        }
        int t = ticks - toldAt - toldTicks;
        if (t >= runTicks) {
            return 1;
        }
        if (t < easeTicks) {
            return 1 - 2 * ScrollPlan.smoothstep((double) t / easeTicks);
        }
        if (t >= runTicks - easeTicks) {
            return -1 + 2 * ScrollPlan.smoothstep((double) (t - (runTicks - easeTicks)) / easeTicks);
        }
        return -1;
    }

    /** The profile's angle {@code t} steps into the phase, degrees. */
    double degreesAt(int t) {
        if (t <= bendTicks[0]) {
            return bendDegrees[0];
        }
        for (int i = 1; i < bendTicks.length; i++) {
            if (t < bendTicks[i]) {
                double share = (double) (t - bendTicks[i - 1]) / (bendTicks[i] - bendTicks[i - 1]);
                double eased = share * share * (3 - 2 * share);
                return bendDegrees[i - 1] + (bendDegrees[i] - bendDegrees[i - 1]) * eased;
            }
        }
        return bendDegrees[bendDegrees.length - 1];
    }

    /** Whether it drifts now (its phase, or its ease out). */
    public boolean active() {
        return ticks >= 0 && outTicks < easeOutTicks;
    }

    /** The angle now, degrees (its profile's, eased out after the phase). */
    public double degrees() {
        return degrees;
    }

    /** The sideways drift this step, px (positive: to the right). */
    public double driftStep() {
        return drift;
    }

    /** Whether its reversal's tell runs now (the HUD's chevron, from its tell to its run). */
    public boolean telling() {
        return toldAt >= 0 && !ran;
    }

    /** Whether its reversal runs now (its eases and its reversed run). */
    public boolean running() {
        return ran && ticks - toldAt - toldTicks < runTicks && outTicks < easeOutTicks;
    }

    /** Whether the drift runs reversed now (its sign below 0). */
    public boolean reversed() {
        return sign < 0;
    }

    /** Whether its reversal was told (at most once a run). */
    public boolean told() {
        return toldAt >= 0;
    }

    /** Its reversal's event value (after the level's own reversals). */
    public int index() {
        return index;
    }

    /** Its spec. */
    public PhaseDriftSpec spec() {
        return spec;
    }

    void addTo(StateHash hash) {
        hash.add(ticks).add(outTicks).add(toldAt).add(ran ? 1 : 0).add(degrees).add(drift);
    }
}
