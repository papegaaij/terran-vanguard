package vanguard.sim;

/**
 * M11 part D, step CD1 (design/enemies/space/husk, the crack; m11d-gaps.md §1 #2): one unit's {@link EnemySpec.Crack}.
 * It is {@link Phase#ARMED} until its HP first falls to the crack's share of its whole (or the level's crack cue
 * reaches it, {@link CrackCues}); then it {@link Phase#TELLING tells} for the crack's tell (the hull flares, {@link
 * SimEvents.Type#CRACK_TELL}; the EMP's stun holds the tell's clock), then fires its fan once at the ship ({@link
 * SimEvents.Type#CRACK_FAN}) and is {@link Phase#SPENT}. A unit killed in its tell fires nothing (it is gone). Each
 * {@link Enemy} owns one, used and hashed only for a unit with a crack. Allocates nothing.
 */
public final class Cracking {
    /** Where its crack is. */
    public enum Phase {
        /** Whole: it cracks at its threshold or the cue. */
        ARMED,
        /** Its tell runs: the fan comes at its end. */
        TELLING,
        /** Cracked: its fan is fired (or its tell's end came with the enemies holding fire). */
        SPENT
    }

    private Phase phase = Phase.ARMED;
    /** Steps left of its tell. */
    private int tellLeft;
    /** Its tell's length, steps. */
    private int tellTicks;

    /** A new unit: armed. */
    void clear() {
        phase = Phase.ARMED;
        tellLeft = 0;
        tellTicks = 0;
    }

    /** Hashed only for a unit with a crack. */
    void addTo(StateHash hash) {
        hash.add(phase.ordinal()).add(tellLeft);
    }

    /** Starts its tell of {@code crack} if it is armed; returns whether it started. */
    boolean tell(EnemySpec.Crack crack) {
        if (phase != Phase.ARMED) {
            return false;
        }
        phase = Phase.TELLING;
        tellTicks = SimStep.ticks(crack.tellSeconds());
        tellLeft = tellTicks;
        return true;
    }

    /**
     * One step of its tell ({@code stunned}: the EMP holds it); returns whether the tell is over now: its fan is due
     * this step (and it is spent).
     */
    boolean step(boolean stunned) {
        if (phase != Phase.TELLING || stunned) {
            return false;
        }
        if (--tellLeft > 0) {
            return false;
        }
        phase = Phase.SPENT;
        return true;
    }

    /** Where its crack is. */
    public Phase phase() {
        return phase;
    }

    /** Whether it is still whole and armed (the cue picks only these). */
    public boolean armed() {
        return phase == Phase.ARMED;
    }

    /**
     * How far its tell has run, 0 to 1, between the previous and the current step ({@code alpha} in [0, 1]): the
     * flare's look; 0 outside the tell.
     */
    public double tell(double alpha) {
        if (phase != Phase.TELLING || tellTicks <= 0) {
            return 0;
        }
        double run = tellTicks - tellLeft + alpha;
        return Math.clamp(run / tellTicks, 0, 1);
    }
}
