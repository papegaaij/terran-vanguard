package vanguard.sim;

/**
 * M10 part H, step H1a (round 70's lesson; m10h-gaps.md §2): the draw that every boss playbook shares, extracted from F's
 * {@link AcePlaybook} for H's {@link BossPlaybook} (the Iron Sovereign's core keeps its own copy, {@link RingCore}, so
 * Level 35 cannot move). A draw offers its candidates in order, each with its weight when the rules allow it now (0
 * otherwise: never the one drawn last, a spaced one not within its spacing, ...); one draw of the simulation's seeded
 * random ({@link BossActions#draw}, so a replay draws the same) times their total picks one, the last allowed taking
 * the rounding's rest. Allocates nothing once made.
 */
final class Playbook {
    /** No candidate: none allowed, or nothing drawn yet. */
    static final int NONE = -1;

    private final double[] weights;
    private int count;
    private double total;

    /** A draw of at most {@code most} candidates. */
    Playbook(int most) {
        weights = new double[most];
    }

    /** A new draw starts: no candidates yet. */
    void clear() {
        count = 0;
        total = 0;
    }

    /** The next candidate: its {@code weight} when {@code allowed} now, else 0 (it cannot be drawn); returns its index. */
    int offer(double weight, boolean allowed) {
        double w = allowed ? weight : 0;
        weights[count] = w;
        total += w;
        return count++;
    }

    /** The allowed candidates' weights together (0: none allowed). */
    double total() {
        return total;
    }

    /**
     * The candidate among the first {@code to} that {@code r} (0 up to their total) falls on, in their order: each
     * allowed one takes its weight off it until it goes below 0; the last allowed one takes the rounding's rest. {@link
     * #NONE} when none is allowed.
     */
    int pick(double r, int to) {
        int picked = NONE;
        for (int i = 0; i < to; i++) {
            double w = weights[i];
            if (w > 0) {
                picked = i;
                r -= w;
                if (r < 0) {
                    break;
                }
            }
        }
        return picked;
    }

    /** {@link #pick(double, int)} among every candidate offered. */
    int pick(double r) {
        return pick(r, count);
    }

    /**
     * Whether a candidate drawn last at draw {@code lastAt} (the draws counted from 1; anything far below for never) may
     * be drawn as the next one, draw {@code draws + 1}, when it is drawn at most every {@code every}-th draw.
     */
    static boolean spaced(int draws, int lastAt, int every) {
        return draws + 1 - lastAt >= every;
    }

    /** The draw count for a candidate never drawn (always {@link #spaced}). */
    static final int NEVER = Integer.MIN_VALUE / 2;
}
