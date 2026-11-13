package vanguard.sim;

/**
 * Step PT2 (the user's Level 50 playthrough, 2026-11-12; design/enemies/bosses/choir-heart, Phases: the lulls): a boss's
 * lulls ({@link BossLullsSpec}), kept by its {@link SetPiece} behind one accessor ({@link SetPiece#lulls()}), made only
 * for a boss with them. Per phase a clock counts the engaged steps from the phase's start; once a lull is due
 * ({@link #due}) the playbook draws no new move, and as soon as it rests the lull starts ({@link SimEvents.Type#BOSS_LULL}
 * value 1): for its length the phase's attacks but the kept ones are silent ({@link #silences}), the playbook waits and,
 * with {@link BossLullsSpec#open}, the living windowed parts hold open; at its end (value 0) the clock starts again. A
 * new phase drops a running lull. Its state is hashed with its piece's. Allocates nothing once made.
 */
public final class BossLulls {
    /** The look's ease into and out of a lull, s ({@link #share}). */
    public static final double EASE_SECONDS = 0.4;

    private final BossLullsSpec spec;
    private final SetPiece piece;
    private final boolean[] kept;
    private final int fireTicks;
    private final int lullTicks;
    private final int easeTicks;

    // The state (hashed).
    private int phase = -1;
    /** Steps fired since the phase's start or the last lull's end. */
    private int fired;
    /** Steps left of the running lull (0: none). */
    private int left;
    /** Steps since the last lull ended (the look's ease out); large when none. */
    private int since = Integer.MAX_VALUE / 2;

    BossLulls(BossLullsSpec spec, SetPiece piece, int attacks) {
        this.spec = spec;
        this.piece = piece;
        kept = new boolean[attacks];
        for (int a : spec.keep()) {
            kept[a] = true;
        }
        lullTicks = Math.max(1, SimStep.ticks(spec.seconds()));
        fireTicks = Math.max(1, SimStep.ticks(spec.everySeconds()) - lullTicks);
        easeTicks = Math.max(1, SimStep.ticks(EASE_SECONDS));
    }

    /** Back to the level start (and a checkpoint): no lull, the clock at 0. */
    void reset() {
        phase = -1;
        fired = 0;
        left = 0;
        since = Integer.MAX_VALUE / 2;
    }

    /**
     * One engaged step in phase {@code now}: the clock runs from the phase's start; a due lull starts when the playbook
     * {@code rests} and the phase's {@code opening} is over; a running one counts down. Its events through {@code
     * actions}.
     */
    void step(int now, boolean opening, boolean rests, BossActions actions) {
        if (now != phase) {
            if (left > 0) {
                end(actions);
            }
            phase = now;
            fired = 0;
        }
        if (since < Integer.MAX_VALUE / 2) {
            since++;
        }
        if (left > 0) {
            if (--left == 0) {
                end(actions);
            }
            return;
        }
        if (fired < fireTicks) {
            fired++;
            return;
        }
        if (rests && !opening) {
            left = lullTicks;
            actions.ring(SimEvents.Type.BOSS_LULL, piece.renderX(1), piece.renderY(1), 1);
            if (spec.open() && piece.partWindows != null) {
                // Its own step's countdown included: open on every step the lull runs.
                piece.partWindows.lull(lullTicks + 1);
            }
        }
    }

    private void end(BossActions actions) {
        left = 0;
        fired = 0;
        since = 0;
        actions.ring(SimEvents.Type.BOSS_LULL, piece.renderX(1), piece.renderY(1), 0);
    }

    /** Whether a lull runs now. */
    public boolean quiet() {
        return left > 0;
    }

    /** Whether a lull is due or runs (the playbook draws no new move). */
    public boolean due() {
        return left > 0 || fired >= fireTicks;
    }

    /** Whether attack {@code a} is silent now (a lull runs and it is not kept). */
    public boolean silences(int a) {
        return left > 0 && (a < 0 || a >= kept.length || !kept[a]);
    }

    /** How deep in a lull it is, for the looks: 0 outside, easing to 1 over {@link #EASE_SECONDS} and back out after. */
    public double share() {
        if (left > 0) {
            return Math.min(1, (double) (lullTicks - left + 1) / easeTicks);
        }
        return Math.max(0, 1 - (double) since / easeTicks);
    }

    public BossLullsSpec spec() {
        return spec;
    }

    void addTo(StateHash hash) {
        hash.add(phase).add(fired).add(left).add(since);
    }
}
