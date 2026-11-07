package vanguard.sim;

import java.util.List;

/**
 * M11 part G, step G1 (design/enemies/bosses/dust-colossus, Echo: "at each dive a seeded draw of the next breach";
 * m11g-gaps.md §1 #6): a breaching boss's next point drawn with the simulation's seeded random instead of its phases'
 * fixed {@link BossSpec.Order orders} ({@link BossSpec.Breach#draw()}; kept by its {@link BreachArena}). Each draw takes
 * one of {@link Spec#points()} (following the ship when {@link Spec#follow()}), evenly: never the point drawn last;
 * the {@link BreachArena.Pose#REAR rear} at least every {@link Spec#rearEvery()}-th draw; or the eruption (an {@link
 * BreachArena.Pose#UNDER UNDER} point: its marker, the open mouth) at most every {@link Spec#eruptionEvery()}-th draw
 * and never the first when {@link Spec#eruptionNotFirst()}. The next point is drawn as soon as it is asked for (the
 * cycle's timing reads it) and kept until it is taken. Its state is hashed with its arena's (only with a draw).
 */
final class BreachDraw {
    /**
     * The draw's rules.
     *
     * @param points the edge points drawn from (no {@code UNDER}), at least two
     * @param follow whether the points follow the ship (a front or rear point the ship's x, a side its y)
     * @param rearEvery the rear at least every this many draws (0: no such rule)
     * @param eruptionEvery the eruption at most every this many draws (0: no eruption)
     * @param eruptionNotFirst never the eruption as the first draw
     */
    public record Spec(
            List<BreachArena.Pose> points, boolean follow, int rearEvery, int eruptionEvery, boolean eruptionNotFirst) {
        public Spec {
            points = List.copyOf(points);
            if (points.size() < 2
                    || points.contains(BreachArena.Pose.UNDER)
                    || points.stream().distinct().count() != points.size()
                    || rearEvery < 0
                    || eruptionEvery < 0
                    || (rearEvery > 0 && !points.contains(BreachArena.Pose.REAR))) {
                throw new IllegalArgumentException("a breach draw takes two edge points or more, each once, the rear"
                        + " among them for its rule: " + points);
            }
        }
    }

    private static final BreachArena.Pose[] POSES = BreachArena.Pose.values();

    private final Spec spec;
    /** The draw's candidates this time, by pose ordinal (no allocation per draw). */
    private final int[] candidates = new int[POSES.length];
    /** The sortie's seeded random ({@link #draws}); null until given. */
    private SplitMix64 rng;
    /** The point drawn next (a pose ordinal); -1 until drawn. */
    private int next;
    /** The point taken last; -1 for none. */
    private int last;
    /** Points taken since the last rear (or since the start). */
    private int sinceRear;
    /** Points taken since the last eruption (or since the start). */
    private int sinceEruption;
    /** Points taken. */
    private int taken;

    BreachDraw(Spec spec, boolean eruption) {
        if (spec.eruptionEvery() > 0 && !eruption) {
            throw new IllegalArgumentException("a breach draw's eruption needs the boss's eruption");
        }
        this.spec = spec;
        reset();
    }

    /** The sortie's seeded random (a replay draws the same). */
    void draws(SplitMix64 random) {
        rng = random;
    }

    void reset() {
        next = -1;
        last = -1;
        sinceRear = 0;
        sinceEruption = 0;
        taken = 0;
    }

    /** The next point (drawn now if not yet). */
    BreachArena.Pose peek() {
        if (next < 0) {
            next = draw();
        }
        return POSES[next];
    }

    /** The next point, taken: the following one is drawn when asked for. */
    BreachArena.Pose take() {
        BreachArena.Pose pose = peek();
        next = -1;
        last = pose.ordinal();
        taken++;
        sinceRear = pose == BreachArena.Pose.REAR ? 0 : sinceRear + 1;
        sinceEruption = pose == BreachArena.Pose.UNDER ? 0 : sinceEruption + 1;
        return pose;
    }

    /** Whether the points follow the ship. */
    boolean follow() {
        return spec.follow();
    }

    private int draw() {
        boolean mustRear = spec.rearEvery() > 0 && sinceRear >= spec.rearEvery() - 1;
        int n = 0;
        List<BreachArena.Pose> points = spec.points();
        for (int i = 0; i < points.size(); i++) {
            BreachArena.Pose pose = points.get(i);
            if (pose.ordinal() != last && (!mustRear || pose == BreachArena.Pose.REAR)) {
                candidates[n++] = pose.ordinal();
            }
        }
        boolean erupts = spec.eruptionEvery() > 0
                && !mustRear
                && last != BreachArena.Pose.UNDER.ordinal()
                && (taken > 0 || !spec.eruptionNotFirst())
                && (taken == 0 || sinceEruption >= spec.eruptionEvery() - 1 || !tookEruption());
        if (erupts) {
            candidates[n++] = BreachArena.Pose.UNDER.ordinal();
        }
        if (n == 0) {
            // The rear forced right after a rear cannot happen (it resets the count); the first other point then.
            for (int i = 0; i < points.size(); i++) {
                BreachArena.Pose pose = points.get(i);
                if (n == 0 && pose.ordinal() != last) {
                    candidates[n++] = pose.ordinal();
                }
            }
        }
        return candidates[Math.min(n - 1, (int) (rng.nextDouble() * n))];
    }

    /** Whether an eruption was taken yet ({@link #sinceEruption} counts from the start until then). */
    private boolean tookEruption() {
        return sinceEruption < taken;
    }

    void addTo(StateHash hash) {
        hash.add(next).add(last).add(sinceRear).add(sinceEruption).add(taken);
    }
}
