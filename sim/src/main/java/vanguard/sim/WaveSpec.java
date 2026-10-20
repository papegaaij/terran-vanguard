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
        Optional<Pursuit> pursuit) {
    public WaveSpec {
        if (edge == Edge.TOP || edge == Edge.BOTTOM) {
            throw new IllegalArgumentException("a wave enters from no top or bottom edge: " + edge);
        }
        carried = List.copyOf(carried);
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
                Optional.of(block));
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
                pursuit);
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
                pursuit);
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
                pursuit);
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
                pursuit);
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
     * @param unit the unit's index in the group, or {@link #LAST}
     */
    public record Carried(PickupType pickup, int unit) {
        /** The group's last unit, whatever its size on the difficulty. */
        public static final int LAST = -1;

        /** Carried by the group's first or last unit. */
        public Carried(PickupType pickup, boolean lastUnit) {
            this(pickup, lastUnit ? LAST : 0);
        }

        /** Whether the last unit carries it. */
        public boolean lastUnit() {
            return unit == LAST;
        }
    }
}
