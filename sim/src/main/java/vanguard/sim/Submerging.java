package vanguard.sim;

/**
 * M5 part E: a submerging unit's swap timer (design/enemies/naval/driftjelly): it waits a random time,
 * then swaps between the surface and {@link Layer#SUB} (its layer flips at the swap's middle). Each {@link
 * Enemy} owns one, used only while its stat block has a submerge; its draws come from its own seed (a
 * SplitMix64 state), so the timer replays.
 */
final class Submerging {
    /** Whether its current layer is {@link Layer#SUB}. */
    private boolean submerged;
    /** Its swap timer's random state, its own seed (a SplitMix64 state stepped by {@link #nextSwap()}). */
    private long swapSeed;
    /** Steps until its next swap starts. */
    private int swapWait;
    /** Steps into the swap running now; -1 while none runs. */
    private int swapTicks = -1;
    /** Whether the swap running now goes down (from the surface). */
    private boolean swapDown;

    /** Back to a unit without a submerge. */
    void clear() {
        submerged = false;
        swapSeed = 0;
        swapWait = 0;
        swapTicks = -1;
        swapDown = false;
    }

    /**
     * Starts {@code under} the surface or on it, its swap timer seeded with {@code seed}; a field's units
     * start at a seeded point of their first wait ({@code anyPoint}: a wait drawn from 0 to the longest), so
     * a field swaps on the screen and not in step.
     */
    void start(EnemySpec.Submerge submerge, boolean under, long seed, boolean anyPoint) {
        submerged = under;
        swapSeed = seed;
        swapTicks = -1;
        swapDown = false;
        double wait = anyPoint
                ? nextSwap() * submerge.everyMax()
                : submerge.everyMin() + nextSwap() * (submerge.everyMax() - submerge.everyMin());
        swapWait = Math.max(1, SimStep.ticks(wait));
    }

    /** The next draw of its swap timer, uniform in [0, 1): SplitMix64 on its own state. */
    private double nextSwap() {
        long z = (swapSeed += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return ((z ^ (z >>> 31)) >>> 11) * 0x1.0p-53;
    }

    /**
     * One real step of the timer: a running swap goes on (the layer flips at its middle) or ends, drawing
     * the next wait; otherwise the wait counts down to the next swap.
     */
    void step(EnemySpec.Submerge submerge) {
        int total = Math.max(2, SimStep.ticks(submerge.swapSeconds()));
        if (swapTicks >= 0) {
            if (++swapTicks == total / 2) {
                submerged = swapDown;
            }
            if (swapTicks >= total) {
                swapTicks = -1;
                double wait = submerge.everyMin() + nextSwap() * (submerge.everyMax() - submerge.everyMin());
                swapWait = Math.max(1, SimStep.ticks(wait));
            }
        } else if (--swapWait <= 0) {
            swapTicks = 0;
            swapDown = !submerged;
        }
    }

    /** Hashed only for a unit with a submerge. */
    void addTo(StateHash hash) {
        hash.add(submerged ? 1 : 0).add(swapSeed).add(swapWait).add(swapTicks).add(swapDown ? 1 : 0);
    }

    /** Whether its current layer is {@link Layer#SUB}, under the surface. */
    boolean submerged() {
        return submerged;
    }

    /**
     * How far it is through the swap running now, between the previous and the current step, 0 to 1 (its
     * layer flips at 0.5); -1 while none runs.
     */
    double swap(EnemySpec.Submerge submerge, double alpha) {
        if (swapTicks < 0) {
            return -1;
        }
        int total = Math.max(2, SimStep.ticks(submerge.swapSeconds()));
        return Math.min(1, Math.max(0, (swapTicks - 1 + alpha) / total));
    }

    /** Whether the swap running now goes down (it dives); false for one that surfaces or none. */
    boolean diving() {
        return swapTicks >= 0 && swapDown;
    }
}
