package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * One group of a wave in the level script, with the difficulty's changes applied (a mixed wave
 * becomes one spec per group at the same time).
 *
 * @param t seconds from the level start when its first unit enters
 * @param formation how the units are placed and fly (design/enemies, formation vocabulary)
 * @param count units in the group
 * @param entry the edge it enters from
 * @param edge which side of that edge
 * @param holdSeconds how long a pincer holds at the edge or a circle orbits
 * @param warningSeconds how long before {@code t} the edge warning starts, if longer than the minimum
 * @param breakGroup how many units of a circle break toward the player together
 * @param speed px/s instead of the enemy's own speed
 * @param intervalSeconds the gap between two units of a stream
 * @param carried pickups that a unit of this group drops when destroyed
 * @param at a whirl cluster's release point
 * @param paths a walker wave's ground paths, one per unit (a pincer mirrors the first for the
 *     units on the right, a convoy repeats it), in play-field points at {@code t}; a segment
 *     chain's flight paths, one per unit (its head flies it); M5 part D: a swarm's route (its
 *     leader point flies it) and a snake's (every unit flies it)
 * @param loopBack a segment chain's loop-back after its path; M5 part D: a swarm's leader point's
 *     (with its count)
 * @param tag M5 part C: the wave's tag (Level 09's {@code bridge}), which a secondary {@code escapes}
 *     objective may be scoped to; empty for none
 * @param field M5 part E: a {@code field} wave's area (design/enemies/naval/driftjelly)
 * @param clockwise M5 part F: a {@code circle} orbits clockwise on the screen (Level 12's eye); its
 *     enemy's orbit runs counter-clockwise otherwise
 * @param lanes M5 part G (design/enemies/naval/skimmer, user decision G4 = a): a lane walker wave's
 *     lanes, ground-space polylines in play-field points at {@code t} (x, depth below the top edge;
 *     outside the play field for an entry from a side or the rear) that scroll with the ground: each
 *     unit follows its lane (a Catmull-Rom curve through the points, as a flight path) at its speed
 *     with its {@code sine} weave as an offset across it, and straight on past its end. Unit i takes
 *     lane i (wrapping; a pincer with one lane mirrors it for every second unit; a {@code cross} needs a
 *     lane per unit); empty for other waves. M5 part G: also a {@code circle}'s centre is {@code at}
 * @param pairSeconds M5 part G (design/campaign Level 13, hard's t=210 Stingers): a {@code column}'s
 *     units enter in pairs side by side, the pairs this far apart; empty for one by one
 * @param pursuit M7 part G (design/campaign Level 20, The pursuit swarm): a {@code pursuit} wave's band, lunges,
 *     refills and break-off ({@link Pursuit}); empty for every other wave
 * @param mount M8 part E (design/campaign Level 25): where its units come out of the trench's walls: a cave's ({@link
 *     Mount#cave}: they spawn at a notch's back and swim out) or the walls' feet ({@link Mount#wall}: nests mounted
 *     on them); empty for every other wave
 */
public record WaveSpec(
        double t,
        Formation formation,
        EnemySpec enemy,
        int count,
        Entry entry,
        Edge edge,
        Optional<Double> holdSeconds,
        Optional<Double> warningSeconds,
        int breakGroup,
        Optional<Double> speed,
        Optional<Double> intervalSeconds,
        List<Carried> carried,
        Optional<At> at,
        List<List<At>> paths,
        Optional<LoopBack> loopBack,
        String tag,
        Optional<Field> field,
        boolean clockwise,
        List<List<At>> lanes,
        Optional<Double> pairSeconds,
        Optional<Pursuit> pursuit,
        Optional<Mount> mount,
        Optional<EnemySpec> leader,
        Optional<OnRocks> onRocks,
        Optional<MineLines> mineLines,
        Optional<Branch> branch) {
    public WaveSpec {
        java.util.Objects.requireNonNull(mineLines);
        java.util.Objects.requireNonNull(branch);
        if (branch.isPresent() && entry != Entry.FRONT) {
            throw new IllegalArgumentException(
                    enemy.slug() + ": a branch's wave flies in its channel from the front (M9 part E)");
        }
        if (mineLines.isPresent() && (enemy.mineLayer().isEmpty() || entry != Entry.REAR)) {
            throw new IllegalArgumentException(
                    enemy.slug() + ": a wave's mine lines are a mine layer's, entering from the rear (M9 part C)");
        }
        java.util.Objects.requireNonNull(leader);
        java.util.Objects.requireNonNull(onRocks);
        if (leader.isPresent()
                && (count < 2
                        || enemy.scatter().isEmpty()
                        || enemy.chain().isPresent()
                        || enemy.flock().isPresent()
                        || mount.isPresent()
                        || formation == Formation.PURSUIT)) {
            throw new IllegalArgumentException(enemy.slug()
                    + ": a wave's leader leads a plain wing of at least two whose units scatter (M9 part B)");
        }
        if (onRocks.isPresent()
                && (enemy.ride().isEmpty()
                        || count != onRocks.get().rocks().size() * onRocks.get().riders())) {
            throw new IllegalArgumentException(enemy.slug()
                    + ": a wave on rocks carries a rider unit's riders on each of its rocks (M9 part B): " + count);
        }
        if (onRocks.isPresent() && (mount.isPresent() || leader.isPresent() || !carried.isEmpty())) {
            throw new IllegalArgumentException(enemy.slug() + ": a wave on rocks has no mount, leader or pickup");
        }
        if (edge == Edge.TOP || edge == Edge.BOTTOM) {
            throw new IllegalArgumentException("a wave enters from no top or bottom edge: " + edge);
        }
        carried = List.copyOf(carried);
        if (carried.stream().anyMatch(Carried::headFirst) && enemy.chain().isEmpty()) {
            throw new IllegalArgumentException("only a segment chain's head drops a pickup for a head-first kill");
        }
        if (carried.stream().anyMatch(Carried::centreUnit) && (formation != Formation.FIELD || field.isEmpty())) {
            throw new IllegalArgumentException("only a field's unit nearest its centre carries a centre pickup");
        }
        paths = paths.stream().map(List::copyOf).toList();
        lanes = lanes.stream().map(List::copyOf).toList();
        if (lanes.stream().anyMatch(List::isEmpty)) {
            throw new IllegalArgumentException("a lane has points");
        }
        if (pairSeconds.isPresent() && !(pairSeconds.get() > 0)) {
            throw new IllegalArgumentException("pairs enter some time apart: " + pairSeconds.get());
        }
        if (field.isPresent() != (formation == Formation.FIELD)) {
            throw new IllegalArgumentException("a field wave has its area, other waves none");
        }
        if (pursuit.isPresent() != (formation == Formation.PURSUIT)) {
            throw new IllegalArgumentException("a pursuit wave has its pursuit block, other waves none");
        }
        if (pursuit.isPresent() && !(pursuit.get().breakAt() > t)) {
            throw new IllegalArgumentException(
                    "a pursuit breaks off after it entered: " + pursuit.get().breakAt());
        }
        java.util.Objects.requireNonNull(mount);
        if (mount.isPresent() && (mount.get().wall() || mount.get().ground()) && entry != Entry.FRONT) {
            throw new IllegalArgumentException(
                    "a wall's or the ground's nests come in with the ground, at the front: " + entry);
        }
        if (mount.isPresent()
                && !mount.get().wall()
                && !mount.get().ground()
                && (entry != Entry.SIDES || (edge != Edge.LEFT && edge != Edge.RIGHT))) {
            throw new IllegalArgumentException("a cave wave enters from its notch's side edge: " + entry + " " + edge);
        }
        if (mount.isPresent() && mount.get().wall() && mount.get().opposite() && edge != Edge.NONE) {
            throw new IllegalArgumentException("opposite nests stand on both walls: " + edge);
        }
    }

    /** A wave of no fork's branch (before M9 part E). */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack,
            String tag,
            Optional<Field> field,
            boolean clockwise,
            List<List<At>> lanes,
            Optional<Double> pairSeconds,
            Optional<Pursuit> pursuit,
            Optional<Mount> mount,
            Optional<EnemySpec> leader,
            Optional<OnRocks> onRocks,
            Optional<MineLines> mineLines) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                onRocks,
                mineLines,
                Optional.empty());
    }

    /**
     * M9 part E (design/campaign Level 32, The forks): this wave as one of {@code fork}'s branch's: it flies only when
     * that side is chosen (else it never comes and counts nowhere). Set it last: the older {@code withX} copies leave it
     * out.
     */
    public WaveSpec withBranch(Branch fork) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                onRocks,
                mineLines,
                Optional.of(fork));
    }

    /** A wave without M9 part B's leader and rocks. */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack,
            String tag,
            Optional<Field> field,
            boolean clockwise,
            List<List<At>> lanes,
            Optional<Double> pairSeconds,
            Optional<Pursuit> pursuit,
            Optional<Mount> mount) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                Optional.empty(),
                Optional.empty());
    }

    /** Without M9 part C's mine lines. */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack,
            String tag,
            Optional<Field> field,
            boolean clockwise,
            List<List<At>> lanes,
            Optional<Double> pairSeconds,
            Optional<Pursuit> pursuit,
            Optional<Mount> mount,
            Optional<EnemySpec> leader,
            Optional<OnRocks> onRocks) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                onRocks,
                Optional.empty());
    }

    /**
     * M9 part C (design/enemies/space/minelayer; Level 30): this mine layers' wave with its lines: unit i lays at
     * {@code lines}' row and gap i (cycling). Set it last: the older {@code withX} copies leave it out.
     */
    public WaveSpec withMineLines(MineLines lines) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                onRocks,
                Optional.of(lines));
    }

    /**
     * M9 part C (design/enemies/space/minelayer; design/campaign Level 30, Mine lines in this level): where a mine
     * layers' wave lays its lines. Unit i rises in its lane (from the wave's edge: {@code left}, {@code right}, or
     * alternating from the left) to the lay row {@code rows[i % n]} px below the top edge and lays its line with its gap
     * at the cluster boundary nearest {@code gapXs[i % m]} px from the left edge (authored per wave: Level 30's
     * left-centre ≈ 160 or right-centre ≈ 320); the units enter the wave's interval apart (the staggered entry, 2.5 s at
     * Level 30).
     */
    public record MineLines(List<Double> rows, List<Double> gapXs) {
        public MineLines {
            rows = List.copyOf(rows);
            gapXs = List.copyOf(gapXs);
            if (rows.isEmpty() || gapXs.isEmpty()) {
                throw new IllegalArgumentException("mine lines have a lay row and a gap at least");
            }
            for (double row : rows) {
                if (!(row > 0) || !(row < PlayField.HEIGHT)) {
                    throw new IllegalArgumentException("a lay row lies inside the play field: " + row);
                }
            }
            for (double x : gapXs) {
                if (!(x >= 0) || !(x <= PlayField.WIDTH)) {
                    throw new IllegalArgumentException("a line's gap lies inside the play field: " + x);
                }
            }
        }

        /** Unit {@code i}'s lay row, px below the top edge. */
        public double row(int i) {
            return rows.get(i % rows.size());
        }

        /** Unit {@code i}'s gap point, px from the left edge. */
        public double gapX(int i) {
            return gapXs.get(i % gapXs.size());
        }
    }

    /**
     * M9 part B (design/enemies/air/talon, Squad leader): this wave led by {@code leaderSpec}: its first unit (a
     * V-wing's tip, place 0) flies the leader's stat block (the same airframe, its own HP, shield and bounty); killing it
     * scatters the wave's living members ({@link EnemySpec#scatter()}). A leader leaving the screen alive changes
     * nothing.
     */
    public WaveSpec withLeader(EnemySpec leaderSpec) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                Optional.of(leaderSpec),
                onRocks,
                mineLines);
    }

    /**
     * M9 part B (design/enemies, Rock riding): this wave's units ride {@code rocks}: {@link OnRocks#riders()} on each
     * rock, in the rocks' order (its count is rocks × riders; its formation, entry and edge are not read for the
     * units, which ride; the entry's edge warning is).
     */
    public WaveSpec withRocks(OnRocks rocks) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                Optional.of(rocks),
                mineLines);
    }

    /**
     * M9 part B (design/enemies, Rock riding; design/enemies/space/asteroid-mite): a wave's rocks, each a small
     * breakable chunk of {@code chunk}'s kind (its size, HP, damage; its own t, x and drift are not read) entering at
     * its {@link Rock}'s point and drifting at its velocity, carrying {@code riders} of the wave's units round its rim
     * (2–5). The rocks block shots of both sides as the debris does.
     */
    public record OnRocks(List<Rock> rocks, int riders, HazardSpec.DebrisSpec chunk) {
        /** The most riders a rock carries. */
        public static final int MOST_RIDERS = 5;

        public OnRocks {
            rocks = List.copyOf(rocks);
            if (rocks.isEmpty() || riders < 1 || riders > MOST_RIDERS || chunk.large()) {
                throw new IllegalArgumentException("a wave on rocks has rocks, each a small chunk with 1-" + MOST_RIDERS
                        + " riders: " + rocks.size() + " rocks, " + riders + " riders");
            }
        }
    }

    /**
     * M9 part B: one rock of a wave on rocks: it enters at ({@code x}, {@code y}) (px from the left, px up from the
     * bottom edge: above the top edge for a rock from the front, beyond a side edge for one from a side) at the wave's
     * time and drifts at ({@code vx}, {@code vy}) px/s on the screen (the scroll included).
     */
    public record Rock(double x, double y, double vx, double vy) {
        public Rock {
            if (!(vy < 0) && vx == 0) {
                throw new IllegalArgumentException("a rock drifts onto and across the screen: " + vx + ", " + vy);
            }
        }
    }

    /** A wave without M8 part E's mount. */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack,
            String tag,
            Optional<Field> field,
            boolean clockwise,
            List<List<At>> lanes,
            Optional<Double> pairSeconds,
            Optional<Pursuit> pursuit) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                Optional.empty());
    }

    /**
     * M8 part E (design/campaign Level 25, The caves): this wave out of the cave {@code notch} (an index into {@link
     * Terrain#notches()}): its units spawn at the notch's back, each at its own time, and swim out of its mouth (the
     * scroll carrying the notch down meanwhile) on to where its formation puts it. It enters from the notch's side
     * ({@link Entry#SIDES}, its {@link Edge}), warned at that edge as any side wave.
     */
    public WaveSpec withCave(int notch) {
        return withMount(Mount.cave(notch));
    }

    /**
     * M8 part E (design/campaign Level 25, Siren nests): this wave's units are nests mounted on the walls' feet ({@link
     * WallMounts}), coming in with the ground at the front: on the left wall ({@link Edge#LEFT}), the right one ({@link
     * Edge#RIGHT}) or both ({@link Edge#NONE} or {@link Edge#ALTERNATING}): staggered, a nest every {@link
     * WallMounts#STAGGER_SECONDS} s, the walls taking turns from the right; or, {@code opposite}, in pairs facing each
     * other across the trench at one row, the left one's song {@link WallMounts#STAGGER_SECONDS} s behind the right
     * one's (the phase rule).
     */
    public WaveSpec withWall(boolean opposite) {
        return withMount(Mount.wall(opposite));
    }

    /**
     * M8 part G (design/campaign Level 27, the Sirens on a chimney stack; §5 G8): this wave's units are nests standing on
     * the ground at {@code xs} (px from the left; unit i at xs[i]), all at one row: they come in with the ground at the
     * top edge at the wave's time and scroll with it, facing down the screen; unit i's song starts i × {@link
     * WallMounts#STAGGER_SECONDS} s behind the first's (the phase rule). No terrain needed.
     */
    public WaveSpec withGroundNests(List<Double> xs) {
        return withMount(Mount.ground(xs));
    }

    private WaveSpec withMount(Mount point) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                Optional.of(point),
                leader,
                onRocks);
    }

    /**
     * M8 part E: where a wave's units come out of the trench's walls.
     *
     * @param cave the notch they swim out of (an index into {@link Terrain#notches()}); -1 for nests on the walls
     * @param opposite nests only: in pairs across the trench at one row (else staggered)
     * @param xs M8 part G: nests standing on the ground at these xs, px from the left, one per unit ({@link
     *     WaveSpec#withGroundNests}); empty for the walls' nests and a cave's units
     */
    public record Mount(int cave, boolean opposite, List<Double> xs) {
        public Mount {
            xs = List.copyOf(xs);
            if (cave < -1 || (cave >= 0 && opposite) || (!xs.isEmpty() && (cave >= 0 || opposite))) {
                throw new IllegalArgumentException(
                        "a mount is a cave's notch, the walls' nests or the ground's: " + cave);
            }
        }

        /** M8 part E: a cave's notch or the walls' nests. */
        public Mount(int cave, boolean opposite) {
            this(cave, opposite, List.of());
        }

        /** M8 part G: nests standing on the ground at {@code xs}. */
        public static Mount ground(List<Double> xs) {
            if (xs.isEmpty()) {
                throw new IllegalArgumentException("ground nests stand at their xs");
            }
            return new Mount(-1, false, xs);
        }

        /** M8 part G: whether its units are nests standing on the ground at {@link #xs()}. */
        public boolean ground() {
            return !xs.isEmpty();
        }

        /** Out of the cave {@code notch}. */
        public static Mount cave(int notch) {
            if (notch < 0) {
                throw new IllegalArgumentException("a cave's notch is an index: " + notch);
            }
            return new Mount(notch, false);
        }

        /** Nests on the walls' feet, {@code opposite} in pairs or staggered. */
        public static Mount wall(boolean opposite) {
            return new Mount(-1, opposite);
        }

        /** Whether its units are nests on the walls (else they swim out of a cave or stand on the ground). */
        public boolean wall() {
            return cave < 0 && xs.isEmpty();
        }
    }

    /** A wave without M7 part G's pursuit. */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack,
            String tag,
            Optional<Field> field,
            boolean clockwise,
            List<List<At>> lanes,
            Optional<Double> pairSeconds) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                Optional.empty());
    }

    /**
     * M7 part G: this wave as a {@code pursuit} with {@code block} (its formation becomes {@link Formation#PURSUIT}).
     */
    public WaveSpec withPursuit(Pursuit block) {
        return new WaveSpec(
                t,
                Formation.PURSUIT,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                Optional.of(block),
                mount,
                leader,
                onRocks);
    }

    /**
     * M7 part G, a pursuit wave's block (design/campaign Level 20, The pursuit swarm; design/enemies/air/mote-swarm's
     * {@code pursuit} defaults with their easy and hard hooks, the difficulty's applied): its flock's leader point
     * holds a band below the top edge whatever the scroll's direction (behind the ship in Level 20's reversed scroll),
     * following the ship's x, and lunges down the ship's lane at a cadence; refills join from the top edge; it breaks
     * off at a level time. The levers if the beam melts the swarm are {@code lungeEvery} and {@code lagSpeed} (the
     * user's Q.3 of 2026-10-20), not the motes' HP or the refills.
     *
     * @param bandTop the band's top, px below the top edge (50)
     * @param bandBottom the band's bottom, px below the top edge (150)
     * @param lagSpeed px/s the leader point follows the ship's x sideways (90)
     * @param lungeEvery s from one lunge's tell to the next (6; easy 8, hard 4.5)
     * @param tellSeconds the tell before a dive: the flock contracts into a ball (0.8; easy 1.2)
     * @param ballSize px across the contracted flock (60)
     * @param diveSpeed px/s of the dive down the ship's lane, no homing (240)
     * @param overshoot px below where the ship was at the tell's start the dive ends (140)
     * @param climbSpeed px/s back to the band after a dive, and into it on entry (200)
     * @param refill motes joining from the top edge each refill, while the flock has room (3; easy 2, hard 4)
     * @param refillEvery s between two refills, from the wave's time (10)
     * @param breakAt the level time of the break-off, s from the level start (a section's end)
     * @param breakSpeed px/s the leader point climbs off the top at its break-off, the members following alive (200)
     */
    public record Pursuit(
            double bandTop,
            double bandBottom,
            double lagSpeed,
            double lungeEvery,
            double tellSeconds,
            double ballSize,
            double diveSpeed,
            double overshoot,
            double climbSpeed,
            int refill,
            double refillEvery,
            double breakAt,
            double breakSpeed) {
        public Pursuit {
            if (!(bandTop >= 0) || !(bandBottom > bandTop) || !(bandBottom < PlayField.HEIGHT)) {
                throw new IllegalArgumentException(
                        "a pursuit's band lies below the top edge, its top above its bottom: " + bandTop + " to "
                                + bandBottom);
            }
            if (!(lagSpeed >= 0) || !(lungeEvery > 0) || !(tellSeconds >= 0) || !(ballSize > 0)) {
                throw new IllegalArgumentException(
                        "a pursuit has a lag >= 0, a lunge cadence > 0, a tell >= 0 and a" + " ball > 0");
            }
            if (!(diveSpeed > 0) || !(overshoot >= 0) || !(climbSpeed > 0) || !(breakSpeed > 0)) {
                throw new IllegalArgumentException(
                        "a pursuit dives, climbs and breaks off at speeds > 0, its" + " overshoot >= 0");
            }
            if (refill < 0 || !(refillEvery > 0) || !(breakAt > 0)) {
                throw new IllegalArgumentException("a pursuit's refills are >= 0 every > 0 s, its break-off > 0 s");
            }
        }

        /** The refills a pursuit wave entering at {@code t} gets: one every {@code refillEvery} s before its break-off. */
        public int refills(double t) {
            if (refill == 0) {
                return 0;
            }
            int k = 0;
            // A refill due at the break-off itself does not come (the margin keeps a rounded t on the same side).
            while (t + (k + 1) * refillEvery < breakAt - 1e-9) {
                k++;
            }
            return k;
        }
    }

    /** A wave without M5 part G's lanes and pairs. */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack,
            String tag,
            Optional<Field> field,
            boolean clockwise) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                List.of(),
                Optional.empty());
    }

    /** M5 part G: this wave with the lane walkers' {@code walkerLanes} (see {@link #lanes()}). */
    public WaveSpec withLanes(List<List<At>> walkerLanes) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                walkerLanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                onRocks);
    }

    /** M5 part G: this wave with its units entering in pairs {@code seconds} apart (see {@link #pairSeconds()}). */
    public WaveSpec withPairs(double seconds) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                Optional.of(seconds),
                pursuit,
                mount,
                leader,
                onRocks);
    }

    /** M5 part G: this wave with {@code point} as its {@link #at()} (a whirl cluster's release point, a circle's centre). */
    public WaveSpec withAt(At point) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                Optional.of(point),
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                onRocks);
    }

    /** A wave without M5 part F's clockwise circle. */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack,
            String tag,
            Optional<Field> field) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                false);
    }

    /** This wave with M5 part F's {@code clockwise} (a circle's sense of orbit on the screen). */
    public WaveSpec clockwise(boolean turning) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                turning,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                onRocks);
    }

    /** A wave without M5 part E's field. */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack,
            String tag) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                Optional.empty());
    }

    /**
     * M5 part E, a {@code field} wave's area (design/enemies/naval/driftjelly; the stated default of
     * 2026-10-08): {@code width} × {@code height} px centred on {@code x} px from the left edge, its
     * leading (bottom) edge entering at the top edge at the wave's {@code t}; its units scattered in
     * it by the level's seeded scatter at least {@code spacing} px apart, scrolling with the ground
     * and drifting at the enemy's speed along the current, {@code currentRadians} from straight down
     * (positive to the right).
     */
    public record Field(double x, double width, double height, double spacing, double currentRadians) {
        public Field {
            if (!(width > 0) || !(height > 0) || !(spacing > 0)) {
                throw new IllegalArgumentException("a field has an area and a spacing");
            }
        }

        /** The units a field holds at its spacing: whole cells of the spacing across and down. */
        public int capacity() {
            return (int) Math.floor(width / spacing) * (int) Math.floor(height / spacing);
        }
    }

    /** A wave without a tag. */
    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths,
            Optional<LoopBack> loopBack) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                "");
    }

    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at,
            List<List<At>> paths) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                Optional.empty());
    }

    /**
     * A segment chain's loop-back (design/enemies/air/coilwyrm): {@code afterSeconds} after its
     * head reached its path's end (off the screen) it re-enters on {@code path}, shifted sideways
     * so it starts at the head's x; without points straight up from below the bottom edge. M5 part
     * D: a swarm's leader point loops back {@code count} times (each loop-back the next re-entry
     * {@code afterSeconds} after the previous path's end); a chain loops back once.
     */
    public record LoopBack(double afterSeconds, List<At> path, int count) {
        public LoopBack {
            path = List.copyOf(path);
            if (count < 1) {
                throw new IllegalArgumentException("a loop-back comes at least once: " + count);
            }
        }

        /** A single loop-back. */
        public LoopBack(double afterSeconds, List<At> path) {
            this(afterSeconds, path, 1);
        }
    }

    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried,
            Optional<At> at) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                List.of());
    }

    public WaveSpec(
            double t,
            Formation formation,
            EnemySpec enemy,
            int count,
            Entry entry,
            Edge edge,
            Optional<Double> holdSeconds,
            Optional<Double> warningSeconds,
            int breakGroup,
            Optional<Double> speed,
            Optional<Double> intervalSeconds,
            List<Carried> carried) {
        this(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                Optional.empty());
    }

    /** A point in the play field: {@code x} from the left edge, {@code depth} px below the top edge. */
    public record At(double x, double depth) {}

    /** The formations of design/enemies that the simulation flies so far. */
    public enum Formation {
        /** One unit on its own. */
        SINGLE,
        /** One behind the other on the same path. */
        COLUMN,
        /** A column that comes down and turns across the screen at its strafe height. */
        CONVOY,
        /** Tiny spinners released from one point, spiralling out. */
        WHIRL_CLUSTER,
        SNAKE,
        V_WING,
        LINE_ABREAST,
        STREAM,
        PINCER,
        CIRCLE,
        /** A spawner (the carrier) with the units that circle it as escorts (design/enemies/air/brood-pod). */
        CARRIER_ESCORTS,
        /**
         * M5 part C: walkers entering together, each on its own ground path, {@link
         * Formations#PACK_INTERVAL_SECONDS} apart (design/enemies/ground/ravager).
         */
        PACK,
        /**
         * M5 part D: a flock round a leader point that flies the wave's route, with its loop-backs
         * (design/enemies/air/mote-swarm).
         */
        SWARM,
        /**
         * M5 part D: 1–4 units, each in its own lane across the bottom edge, x = (i + 1) × 480 ÷ (n + 1)
         * (design/enemies/air/wraith): an {@link EnemySpec.Ambush} unit enters at the top and comes
         * back up from below.
         */
        REAR_AMBUSH,
        /**
         * M5 part E: units scattered over an area that enters with the sea and drifts on the wave's
         * current (design/enemies/naval/driftjelly), a {@link Field}.
         */
        FIELD,
        /**
         * M5 part G (design/enemies, formation vocabulary; Level 13): groups from all four edges entering
         * together, each unit on its own lane ({@link #lanes()}: one per unit, its edge where the lane
         * starts); without lanes, {@code count} ÷ 4 units per edge, unit i from the top, the right, the
         * bottom and the left edge in turn, flying straight across. Every edge with a unit is warned.
         */
        CROSS,
        /**
         * M7 part G (design/campaign Level 20, The pursuit swarm): a flock whose leader point holds a band below the
         * top edge, follows the ship and lunges down its lane, with refills from the top edge and a break-off
         * ({@link WaveSpec#pursuit()}, flown by {@link vanguard.sim.Pursuit}). Its units are never flipped.
         */
        PURSUIT
    }

    /** The play-field edge a wave enters from. */
    public enum Entry {
        FRONT,
        SIDES,
        REAR
    }

    /** Which side of the entry edge: {@code NONE} at the front means centred, at the sides both edges. */
    public enum Edge {
        NONE,
        LEFT,
        RIGHT,
        ALTERNATING,
        /** M7 part F: only a walking boss's entry edge ({@link BossSpec.Stride#edgeCycle()}); no wave's. */
        TOP,
        /** M7 part F: only a walking boss's entry edge ({@link BossSpec.Stride#edgeCycle()}); no wave's. */
        BOTTOM;

        /** M7 part F: whether it names one screen edge (a walking boss's entry: top, bottom, left or right). */
        public boolean screenEdge() {
            return this == TOP || this == BOTTOM || this == LEFT || this == RIGHT;
        }
    }

    /**
     * A pickup carried by one unit of the group.
     *
     * @param unit the unit's index in the group, {@link #LAST} or (M8 part F, a field only) {@link #CENTRE}
     * @param headFirst M8 part C (design/campaign Level 23, the shield cell's {@code dropped_by: {head_first: true}}): a
     *     segment chain's head drops it only when it is destroyed before any other part of its chain; false for every
     *     other pickup
     */
    public record Carried(PickupType pickup, int unit, boolean headFirst) {
        /** The group's last unit, whatever its size on the difficulty. */
        public static final int LAST = -1;

        /**
         * M8 part F (design/campaign Level 26, the shield cell of the constellation; m8f-gaps.md §5 F10): a field's
         * unit nearest its area's centre where the units are scattered (the first of them on a tie); only on a field.
         */
        public static final int CENTRE = -2;

        /** Dropped by its unit however it dies. */
        public Carried(PickupType pickup, int unit) {
            this(pickup, unit, false);
        }

        /** Carried by the group's first or last unit. */
        public Carried(PickupType pickup, boolean lastUnit) {
            this(pickup, lastUnit ? LAST : 0);
        }

        /** The index in a group of {@code count} units of the unit that carries it. */
        int carrier(int count) {
            return unit == LAST ? count - 1 : Math.min(unit, count - 1);
        }

        /** Whether the last unit carries it. */
        public boolean lastUnit() {
            return unit == LAST;
        }

        /** M8 part F: whether a field's unit nearest its centre carries it ({@link #CENTRE}). */
        public boolean centreUnit() {
            return unit == CENTRE;
        }
    }
}
