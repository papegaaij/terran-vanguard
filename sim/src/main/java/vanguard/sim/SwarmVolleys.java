package vanguard.sim;

/**
 * M10 part B (design/enemies/air/chimera; {@link EnemySpec.SwarmFire}): a firing flock's fire cap: the steps (on the
 * flock's own clock) of its members' last granted volleys, so that at most the cap's volleys fall within any stagger
 * window. Every member asks as its glint starts, its gun's first-shot delay before the shot; they share that lead, so
 * the volleys are granted in the order they fall and the oldest of the last cap ones decides. Owned by its {@link
 * Flock}; idle (and out of the hash) for a swarm without a fire cap. Fixed arrays, allocates nothing.
 */
final class SwarmVolleys {
    private static final int NONE = Integer.MIN_VALUE / 2;

    /** The steps of the last granted volleys, a ring of the cap's size. */
    private final int[] granted = new int[EnemySpec.SwarmFire.MOST];
    /** The cap now; 0 for a flock without one. */
    private int cap;
    /** The stagger window, steps (at least 1: two volleys in one step are always at once). */
    private int window;
    /** The ring's oldest entry, the next one replaced. */
    private int next;

    /** A flock starts: under {@code fire}'s cap, nothing granted; idle without one. */
    void start(java.util.Optional<EnemySpec.SwarmFire> fire) {
        cap = fire.isPresent() ? fire.get().cap() : 0;
        window = fire.isPresent() ? Math.max(1, SimStep.ticks(fire.get().staggerSeconds())) : 0;
        next = 0;
        java.util.Arrays.fill(granted, NONE);
    }

    /** Whether the flock fires under a cap. */
    boolean capped() {
        return cap > 0;
    }

    /**
     * Whether a member's volley falling at step {@code at} (the flock's clock) may fire: fewer than the cap of the
     * granted volleys fall within the stagger window before it. A granted volley is counted.
     */
    boolean grant(int at) {
        if (cap == 0) {
            return true;
        }
        if (at - granted[next] < window) {
            return false;
        }
        granted[next] = at;
        next = (next + 1) % cap;
        return true;
    }

    /** Its state (only a capped flock's). */
    void addTo(StateHash hash) {
        hash.add(next);
        for (int i = 0; i < cap; i++) {
            hash.add(granted[i]);
        }
    }
}
