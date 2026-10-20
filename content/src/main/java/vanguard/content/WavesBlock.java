package vanguard.content;

import java.util.List;
import java.util.Optional;

/**
 * The level's {@code waves} ({@link LevelData#waves()}): each wave, its groups and its changes on easy and hard.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Wave}).
 */
public sealed interface WavesBlock permits LevelData {
    /**
     * A wave: one group ({@code formation}, {@code enemy}, {@code count}) or a mixed wave's
     * {@code groups}; {@link #groups()} gives both as a list.
     *
     * @param from the edge it enters from
     * @param edge which side of that edge; {@code sides} without an edge means both side edges
     * @param hold seconds the formation holds (pincer at the edge, circle orbiting)
     * @param warning seconds of radio warning before a rear wave
     * @param breakGroup how many units of a circle break toward the player together
     * @param speed px/s instead of the enemy's own speed
     * @param interval seconds between two units of a stream
     * @param at a whirl cluster's release point: {@code [x, y]}, px from the left and below the top edge
     * @param paths a walker wave's ground paths, one list of {@code [x, y]} points per unit (y below
     *     the top edge, at the wave's {@code t}; they then scroll with the ground)
     * @param easy changes on easy
     * @param hard changes on hard
     * @param skip the difficulties the wave is left out on (Level 06's hard-only Coilwyrm pair)
     * @param tag M5 part C: a name a secondary {@code escapes} objective may be scoped to (Level 09's
     *     {@code bridge} packs)
     * @param x M5 part E, a {@code field} wave: its area's centre, px from the left edge
     * @param size M5 part E, a {@code field} wave: its area {@code [w, h]}, px; its leading (bottom)
     *     edge enters at the top edge at {@code t}
     * @param spacing M5 part E, a {@code field} wave: the least distance between two units, px (1.5 ×
     *     the enemy's hit box width if not given)
     * @param current M5 part E, a {@code field} wave: the current its units drift along, ° from
     *     straight down, positive to the right (0 if not given)
     * @param clockwise M5 part F, a {@code circle} wave: it orbits clockwise on the screen (Level 12's
     *     eye); counter-clockwise if not given
     * @param lanes M5 part G (design/enemies/naval/skimmer, user decision G4 = a): a lane walker wave's
     *     lanes, one list of {@code [x, y]} points each (y below the top edge, at the wave's {@code t};
     *     outside the play field for an entry from a side or the rear; they scroll with the ground and
     *     stand still in a timed halt): unit i on lane i, wrapping; a {@code cross} has a lane per unit.
     *     M5 part G also: a {@code circle}'s {@code at} is its centre (Level 13's Needlers above the relay)
     * @param crawl M7 part C (design/enemies/ground/threadcrawler): a crawling chain's ground path, {@code [scroll, x]}
     *     points in the walls' rows (the level's {@code terrain}): its head starts at the first point at the wave's
     *     {@code t} with the body laid out straight behind it toward the lower rows (down-canyon), then crawls the
     *     path at its {@code crawl} speed on top of the scroll
     */
    public record Wave(
            double t,
            Optional<String> formation,
            Optional<String> enemy,
            Optional<Integer> count,
            Optional<List<Group>> groups,
            LevelData.Entry from,
            Optional<Edge> edge,
            Optional<Double> hold,
            Optional<Double> warning,
            Optional<Integer> breakGroup,
            Optional<Double> speed,
            Optional<Double> interval,
            Optional<Point> at,
            Optional<List<List<Point>>> paths,
            Optional<LoopBack> loopBack,
            Optional<Change> easy,
            Optional<Change> hard,
            Optional<List<String>> skip,
            Optional<String> tag,
            Optional<Double> x,
            Optional<Size> size,
            Optional<Double> spacing,
            Optional<Double> current,
            Optional<Boolean> clockwise,
            Optional<List<List<Point>>> lanes,
            Optional<List<TerrainBlock.GroundPoint>> crawl) {
        public Wave {
            crawl = crawl.map(List::copyOf);
            crawl.ifPresent(path -> Check.that(path.size() >= 2, "crawl: a ground path of two points or more"));
            Check.that(crawl.isEmpty() || (paths.isEmpty() && lanes.isEmpty()), "a wave crawls, walks paths or lanes");
            lanes.ifPresent(l -> {
                Check.that(!l.isEmpty(), "lanes: at least one lane");
                l.forEach(lane -> Check.that(lane.size() >= 2, "lanes: every lane has two points or more"));
            });
            Check.that(lanes.isEmpty() || paths.isEmpty(), "a wave walks paths or lanes, not both");
            spacing.ifPresent(d -> Check.positive("spacing", d));
            Check.notNegative("t", t);
            tag.ifPresent(name -> Check.that(!name.isBlank(), "tag: a name"));
            skip.ifPresent(names -> names.forEach(Difficulty::of));
            paths.ifPresent(p -> {
                Check.that(!p.isEmpty(), "paths: at least one path");
                p.forEach(path -> Check.that(!path.isEmpty(), "paths: every path has a point"));
            });
            speed.ifPresent(s -> Check.positive("speed", s));
            interval.ifPresent(i -> Check.positive("interval", i));
            boolean single = formation.isPresent() || enemy.isPresent() || count.isPresent();
            Check.that(
                    single != groups.isPresent(),
                    "give either formation, enemy and count, or the groups of a mixed wave");
            if (single) {
                groups = Optional.of(List.of(new Group(
                        formation.orElseThrow(() -> new IllegalArgumentException("formation is required")),
                        enemy.orElseThrow(() -> new IllegalArgumentException("enemy is required")),
                        count.orElseThrow(() -> new IllegalArgumentException("count is required")))));
            }
            boolean field = groups.orElseThrow().stream()
                    .anyMatch(group -> group.formation().equals(FIELD));
            Check.that(
                    !field || (single && x.isPresent() && size.isPresent()),
                    "a field wave is one group with its area's x and size");
            Check.that(
                    field || (x.isEmpty() && size.isEmpty() && spacing.isEmpty() && current.isEmpty()),
                    "only a field wave has an x, a size, a spacing or a current");
            Check.that(
                    clockwise.isEmpty()
                            || groups.orElseThrow().stream()
                                    .allMatch(group -> group.formation().equals("circle")),
                    "only a circle wave turns clockwise");
        }

        /** M5 part F: whether a circle wave orbits clockwise on the screen. */
        public boolean turnsClockwise() {
            return clockwise.orElse(false);
        }

        /** M5 part E: the formation of units scattered over an area that enters with the sea. */
        public static final String FIELD = "field";

        /** The wave's groups; one for a plain wave. */
        public List<Group> groupList() {
            return groups.orElseThrow();
        }

        /** Whether it flies on {@code difficulty}. */
        public boolean fliesOn(Difficulty difficulty) {
            return skip.map(names -> names.stream().map(Difficulty::of).noneMatch(difficulty::equals))
                    .orElse(true);
        }
    }

    /** {@code count} units of {@code enemy} flying {@code formation}. */
    public record Group(String formation, String enemy, int count) {
        public Group {
            Check.positive("count", count);
        }
    }

    public enum Edge {
        LEFT,
        RIGHT,
        ALTERNATING
    }

    /**
     * A difficulty's changes to a wave.
     *
     * @param hold part G: another hold, s (Level 07's easy Mantis pincer holding 4 s)
     * @param loops M5 part D: a swarm's loop-backs instead of its {@code loop_back.count} (Level 10's
     *     hard: 2)
     * @param pairs M5 part G: a {@code column}'s units enter in pairs side by side, the pairs this many s
     *     apart (Level 13's hard Stingers at t=210)
     */
    public record Change(
            Optional<LevelData.Entry> from,
            Optional<Edge> edge,
            Optional<Integer> count,
            Optional<Integer> breakGroup,
            Optional<Double> warning,
            Optional<Double> hold,
            Optional<Integer> loops,
            Optional<Double> pairs) {
        public Change {
            hold.ifPresent(h -> Check.notNegative("hold", h));
            loops.ifPresent(n -> Check.positive("loops", n));
            pairs.ifPresent(p -> Check.positive("pairs", p));
        }
    }

    /**
     * Part F: a segment chain's loop-back: {@code after} s past the end of its path (off the
     * screen) its head re-enters on {@code path} ({@code [x, y]} points, y px below the top edge),
     * shifted sideways to start at the head's x; without a path straight up from the bottom edge.
     * Its bottom-edge warning lasts the wave's {@code warning} (at least the 3 s minimum). M5 part
     * D: a swarm's leader point loops back {@code count} times (1 when left out), each re-entry
     * {@code after} s past the previous path's end; a chain loops back once.
     */
    public record LoopBack(double after, Optional<List<Point>> path, Optional<Integer> count) {
        public LoopBack {
            Check.positive("after", after);
            count.ifPresent(n -> Check.positive("count", n));
        }
    }
}
