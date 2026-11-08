package vanguard.sim;

import java.util.List;

/**
 * M11 part G, step G1 (design/enemies/bosses/dust-colossus, Echo: "at each dive a seeded draw of the next breach";
 * m11g-gaps.md §1 #6): a breaching boss's next point drawn with the simulation's seeded random instead of its phases'
 * fixed {@link BossSpec.Order orders} ({@link BossSpec.Breach#draw()}; kept by its {@link BreachArena}). Each draw takes
 * one of {@link Spec#points()} (following the ship when {@link Spec#follow()}), evenly: never the point drawn last;
 * the {@link BreachArena.Pose#REAR rear} at least every {@link Spec#rearEvery()}-th draw (step G5b: the {@link
 * BreachArena.Pose#FRONT front} at least every {@link Spec#frontEvery()}-th, before it); or the eruption (an {@link
 * BreachArena.Pose#UNDER UNDER} point: its marker, the open mouth) at most every {@link Spec#eruptionEvery()}-th draw
 * and never the first when {@link Spec#eruptionNotFirst()}. The next point is drawn as soon as it is asked for (the
 * cycle's timing reads it) and kept until it is taken. Its state is hashed with its arena's (only with a draw).
 */
public final class BreachDraw {
    /**
     * The draw's rules.
     *
     * @param points the edge points drawn from (no {@code UNDER}), at least two
     * @param follow whether the points follow the ship (a front or rear point the ship's x, a side its y)
     * @param rearEvery the rear at least every this many draws (0: no such rule)
     * @param eruptionEvery the eruption at most every this many draws (0: no eruption)
     * @param eruptionNotFirst never the eruption as the first draw
     * @param eruptionFrom M11 part G, step G5: the eruption only from this boss phase on (0: from the first); a later
     *     stage's new trick (the Dust Colossus echo's hunting stage)
     * @param frontFirst M11 part G, step G5: the front as the first draw (its first breach at the far edge from a ship
     *     that waits low or hugs a side: its entry fire reaches it)
     * @param frontEvery M11 part G, step G5b: the front at least every this many draws (0: no such rule); before the
     *     rear's rule when both fall on one draw (a steadier fight: the front is the breach every loadout reaches)
     */
    public record Spec(
            List<BreachArena.Pose> points,
            boolean follow,
            int rearEvery,
            int eruptionEvery,
            boolean eruptionNotFirst,
            int eruptionFrom,
            boolean frontFirst,
            int frontEvery) {
        /** Without a front rule (step G5's). */
        public Spec(
                List<BreachArena.Pose> points,
                boolean follow,
                int rearEvery,
                int eruptionEvery,
                boolean eruptionNotFirst,
                int eruptionFrom,
                boolean frontFirst) {
            this(points, follow, rearEvery, eruptionEvery, eruptionNotFirst, eruptionFrom, frontFirst, 0);
        }

        /** The eruption from the first boss phase on, any point first (step G1's). */
        public Spec(
                List<BreachArena.Pose> points,
                boolean follow,
                int rearEvery,
                int eruptionEvery,
                boolean eruptionNotFirst) {
            this(points, follow, rearEvery, eruptionEvery, eruptionNotFirst, 0, false, 0);
        }

        public Spec {
            points = List.copyOf(points);
            if (((frontFirst || frontEvery > 0) && !points.contains(BreachArena.Pose.FRONT))
                    || frontEvery == 1
                    || frontEvery < 0
                    || points.size() < 2
                    || points.contains(BreachArena.Pose.UNDER)
                    || points.stream().distinct().count() != points.size()
                    || rearEvery < 0
                    || eruptionEvery < 0
                    || eruptionFrom < 0
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
    /** Step G5b: points taken since the last front (or since the start). */
    private int sinceFront;
    /** Points taken since the last eruption (or since the start). */
    private int sinceEruption;
    /** Points taken. */
    private int taken;

    /** Step G5b: per boss phase (the last for later ones) the edge points it draws from; empty: all of the spec's. */
    private final List<BossSpec.Order> stages;

    BreachDraw(Spec spec, boolean eruption) {
        this(spec, eruption, List.of());
    }

    /**
     * M11 part G, step G5b: a draw from {@code spec}'s points that a boss phase's order (in {@code stages}, the last for
     * later phases) also lists (a stage's own points: the Dust Colossus echo hunts from ahead and behind); its rules
     * for a point the phase leaves out lapse.
     */
    BreachDraw(Spec spec, boolean eruption, List<BossSpec.Order> stages) {
        if (spec.eruptionEvery() > 0 && !eruption) {
            throw new IllegalArgumentException("a breach draw's eruption needs the boss's eruption");
        }
        this.spec = spec;
        this.stages = List.copyOf(stages);
        reset();
    }

    /** Step G5b: whether boss phase {@code bossPhase} draws {@code pose} (its order lists it, or no orders). */
    private boolean inStage(BreachArena.Pose pose, int bossPhase) {
        return stages.isEmpty()
                || stages.get(Math.min(bossPhase, stages.size() - 1)).poses().contains(pose);
    }

    /** The sortie's seeded random (a replay draws the same). */
    void draws(SplitMix64 random) {
        rng = random;
    }

    void reset() {
        next = -1;
        last = -1;
        sinceRear = 0;
        sinceFront = 0;
        sinceEruption = 0;
        taken = 0;
    }

    /** The next point (drawn now if not yet). */
    BreachArena.Pose peek() {
        return peek(0);
    }

    /** The next point (drawn now in boss phase {@code bossPhase} if not yet). */
    BreachArena.Pose peek(int bossPhase) {
        if (next < 0 || (next != BreachArena.Pose.UNDER.ordinal() && !inStage(POSES[next], bossPhase))) {
            // Step G5b: drawn again when a new stage leaves the drawn point out.
            next = draw(bossPhase);
        }
        return POSES[next];
    }

    /** The next point, taken: the following one is drawn when asked for. */
    BreachArena.Pose take() {
        return take(0);
    }

    /** The next point, taken in boss phase {@code bossPhase} (drawn then if not yet). */
    BreachArena.Pose take(int bossPhase) {
        BreachArena.Pose pose = peek(bossPhase);
        next = -1;
        last = pose.ordinal();
        taken++;
        sinceRear = pose == BreachArena.Pose.REAR ? 0 : sinceRear + 1;
        sinceFront = pose == BreachArena.Pose.FRONT ? 0 : sinceFront + 1;
        sinceEruption = pose == BreachArena.Pose.UNDER ? 0 : sinceEruption + 1;
        return pose;
    }

    /** Whether the points follow the ship. */
    boolean follow() {
        return spec.follow();
    }

    private int draw(int bossPhase) {
        // Step G5b: the front's rule first (a front never follows a front: it is never due right after one).
        boolean mustFront = spec.frontEvery() > 0
                && sinceFront >= spec.frontEvery() - 1
                && inStage(BreachArena.Pose.FRONT, bossPhase);
        boolean mustRear = !mustFront
                && spec.rearEvery() > 0
                && sinceRear >= spec.rearEvery() - 1
                && inStage(BreachArena.Pose.REAR, bossPhase);
        int n = 0;
        List<BreachArena.Pose> points = spec.points();
        for (int i = 0; i < points.size(); i++) {
            BreachArena.Pose pose = points.get(i);
            if (pose.ordinal() != last
                    && inStage(pose, bossPhase)
                    && (!mustRear || pose == BreachArena.Pose.REAR)
                    && (!mustFront || pose == BreachArena.Pose.FRONT)
                    && !(taken == 0 && spec.frontFirst() && pose != BreachArena.Pose.FRONT)) {
                candidates[n++] = pose.ordinal();
            }
        }
        boolean erupts = spec.eruptionEvery() > 0
                && bossPhase >= spec.eruptionFrom()
                && !mustRear
                && !mustFront
                && last != BreachArena.Pose.UNDER.ordinal()
                && (taken > 0 || !spec.eruptionNotFirst())
                && (taken == 0 || sinceEruption >= spec.eruptionEvery() - 1 || !tookEruption());
        if (erupts) {
            candidates[n++] = BreachArena.Pose.UNDER.ordinal();
        }
        if (n == 0) {
            // The rear forced right after a rear cannot happen (it resets the count); the first other point then (of
            // the
            // stage's, if it has another; step G5b).
            for (int i = 0; i < points.size(); i++) {
                BreachArena.Pose pose = points.get(i);
                if (n == 0 && pose.ordinal() != last && inStage(pose, bossPhase)) {
                    candidates[n++] = pose.ordinal();
                }
            }
            for (int i = 0; i < points.size(); i++) {
                if (n == 0 && points.get(i).ordinal() != last) {
                    candidates[n++] = points.get(i).ordinal();
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
        hash.add(next)
                .add(last)
                .add(sinceRear)
                .add(sinceFront)
                .add(sinceEruption)
                .add(taken);
    }
}
