package vanguard.sim;

/**
 * M8 part E, the sonar slow (design/enemies/naval/siren; design/world/europa, Sonar slow): a timed status on a hull (one
 * on the {@link Ship}, one on Rook's {@link Wingman}): a Siren's ring that reaches it slows its speed and acceleration
 * to the ring's share (on top of the water's drag) for the ring's slow time, then it is immune to slows for the ring's
 * immunity. A ring that reaches it while it is slowed or immune does nothing. Its fields are in the state hash only in
 * a level with a ring unit ({@link EnemyForce#addRingsTo}); without a slow its factor is exactly 1. Allocation-free.
 */
public final class ShipSlow {
    /** Steps of the slow left; 0 when not slowed. */
    private int slowLeft;
    /** The slow's length, steps (for its share). */
    private int slowTicks;
    /** Steps of the immunity left after the slow. */
    private int immuneLeft;
    /** The immunity a slow brings once it ends, steps. */
    private int immuneTicks;
    /** The share of speed and acceleration while slowed; 1 when not. */
    private double factor = 1;

    /** A new attempt: neither slowed nor immune. */
    void reset() {
        slowLeft = 0;
        slowTicks = 0;
        immuneLeft = 0;
        immuneTicks = 0;
        factor = 1;
    }

    /**
     * A ring reaches the hull: slowed to {@code ring}'s share for its slow time (its immunity after), unless slowed or
     * immune now. Returns whether it took the slow.
     */
    boolean slow(EnemySpec.SlowRing ring) {
        if (slowLeft > 0 || immuneLeft > 0) {
            return false;
        }
        slowTicks = Math.max(1, SimStep.ticks(ring.slowSeconds()));
        slowLeft = slowTicks;
        immuneTicks = SimStep.ticks(ring.immunitySeconds());
        factor = ring.slow();
        return true;
    }

    /** After one step of the hull's flight (which took the slow): the slow runs down, then the immunity. */
    void step() {
        if (slowLeft > 0) {
            if (--slowLeft == 0) {
                factor = 1;
                immuneLeft = immuneTicks;
            }
        } else if (immuneLeft > 0) {
            immuneLeft--;
        }
    }

    /** The share of its speed and acceleration now: the slow's while slowed, else exactly 1. */
    double factor() {
        return factor;
    }

    /** Whether the hull is slowed now (the sonar shimmer). */
    public boolean slowed() {
        return slowLeft > 0;
    }

    /** Whether the hull is immune to slows now (after a slow). */
    public boolean immune() {
        return immuneLeft > 0;
    }

    /** How much of the slow is left, 1 just slowed to 0 (for the shimmer's fade); 0 when not slowed. */
    public double share() {
        return slowLeft > 0 ? (double) slowLeft / slowTicks : 0;
    }

    void addTo(StateHash hash) {
        hash.add(slowLeft).add(slowTicks).add(immuneLeft).add(immuneTicks).add(factor);
    }
}
