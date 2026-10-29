package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.PlayField;

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
     * @param pursuit M7 part G (design/campaign Level 20, The pursuit swarm): a {@code pursuit} wave's level numbers
     *     (its refills, its break-off and any change to its unit's {@code pursuit} defaults); only on a pursuit
     * @param cave M8 part E (design/campaign Level 25, The caves): its units swim out of this notch of the level's
     *     {@code terrain.notches} (an index); it enters {@code from: sides} at the notch's wall ({@code edge})
     * @param wall M8 part E (design/campaign Level 25, Siren nests): its units are nests on the walls' feet, entering
     *     with the ground ({@code from: sides}, counted as side entries): {@code staggered} (a nest every 1.5 s, on the
     *     {@code edge}'s wall or both taking turns from the right) or {@code opposite} (pairs facing each other at one
     *     row, the left one's song 1.5 s later)
     * @param onGround M8 part G (design/campaign Level 27, the Sirens on a chimney stack; m8g-gaps.md §5 G8): a {@code
     *     turret nest}'s units stand on the ground at these xs (px from the left, one per unit), all at one row: they
     *     come in with the ground at the top edge at the wave's {@code t} ({@code from: front}), scrolling with it,
     *     their songs 1.5 s apart in turn (no terrain needed)
     * @param leader M9 part B (design/enemies/air/talon, Squad leader): {@code tip}, a V-wing's tip flies its unit's
     *     {@code leader} airframe; killing it scatters the wing
     * @param onRocks M9 part B (design/enemies, Rock riding): its units ride these rocks ({@link OnRocks}); its count is
     *     rocks × riders on every difficulty
     * @param prey M9 part F (design/enemies/space/void-leech, The uneasy alliance; Level 33, QF-1 a pending round 66):
     *     its units are prey ({@link PreyHold}): a {@code hunts: prey} leech latches on them instead of the ship; they
     *     fly a refuelling hold instead of their swoop (from the front)
     * @param hunts M9 part F: {@code prey}: its leeches drift and chase the nearest free prey on the screen instead of
     *     the ship (the ship once none is left); only on a wave of a unit whose latch has a {@code prey_drift}
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
            Optional<List<TerrainBlock.GroundPoint>> crawl,
            Optional<Pursuit> pursuit,
            Optional<Integer> cave,
            Optional<String> wall,
            Optional<List<Double>> onGround,
            Optional<String> leader,
            Optional<OnRocks> onRocks,
            Optional<MineLines> mineLines,
            Optional<TerrainBlock.Branch> branch,
            Optional<PreyHold> prey,
            Optional<String> hunts) {
        /** M9 part F: the {@code hunts} value of a leech wave sent at a prey wing. */
        public static final String PREY = "prey";

        /** M8 part E: a wall's nests one after the other ({@link #wall}). */
        public static final String STAGGERED = "staggered";

        /** M8 part E: a wall's nests in pairs facing each other ({@link #wall}). */
        public static final String OPPOSITE = "opposite";

        public Wave {
            // M9 part F (Level 33, the uneasy alliance): a prey wing holds from the front; a hunting wave hunts prey.
            Check.that(
                    prey.isEmpty() || (from == LevelData.Entry.FRONT && cave.isEmpty() && wall.isEmpty()),
                    "a prey wing flies its hold from the front (from: front)");
            hunts.ifPresent(h -> Check.that(PREY.equals(h), "hunts: prey, was '" + h + "'"));
            Check.that(prey.isEmpty() || hunts.isEmpty(), "a wave is prey or hunts it, not both");
            // M9 part E (design/campaign Level 32, The forks): a branch's wave flies in its channel from the front.
            Check.that(
                    branch.isEmpty() || (from == LevelData.Entry.FRONT && cave.isEmpty() && wall.isEmpty()),
                    "a branch's wave enters from the front (from: front), not out of a cave or off the walls");
            onGround = onGround.map(List::copyOf);
            onGround.ifPresent(xs -> {
                Check.that(!xs.isEmpty(), "on_ground: the nests' xs");
                xs.forEach(nestX ->
                        Check.that(nestX > 0 && nestX < PlayField.WIDTH, "on_ground: on the play field, was " + nestX));
            });
            Check.that(
                    onGround.isEmpty() || (cave.isEmpty() && wall.isEmpty() && from == LevelData.Entry.FRONT),
                    "ground nests come in with the ground (from: front), not out of a cave or off the walls");
            cave.ifPresent(n -> Check.notNegative("cave", n));
            wall.ifPresent(w -> Check.that(
                    STAGGERED.equals(w) || OPPOSITE.equals(w), "wall: staggered or opposite, was '" + w + "'"));
            Check.that(cave.isEmpty() || wall.isEmpty(), "a wave comes out of a cave or off the walls, not both");
            Check.that(
                    (cave.isEmpty() && wall.isEmpty()) || from == LevelData.Entry.SIDES,
                    "a cave's or the walls' wave enters from: sides");
            Check.that(
                    cave.isEmpty() || edge.filter(e -> e != Edge.ALTERNATING).isPresent(),
                    "a cave wave names its notch's wall (edge: left or right)");
            Check.that(
                    wall.isEmpty() || edge.filter(e -> e == Edge.ALTERNATING).isEmpty(),
                    "a wall's nests stand on the left, the right or both walls (no edge), not alternating");
            Check.that(
                    !OPPOSITE.equals(wall.orElse("")) || edge.isEmpty(),
                    "opposite nests stand on both walls (no edge)");
            Check.that(
                    (cave.isEmpty() && wall.isEmpty())
                            || (easy.isEmpty() && hard.isEmpty())
                            || java.util.stream.Stream.of(easy, hard)
                                    .flatMap(Optional::stream)
                                    .allMatch(change -> change.from().isEmpty()
                                            && change.edge().isEmpty()),
                    "a cave's or the walls' wave keeps its entry on every difficulty");
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
            boolean pursues = groups.orElseThrow().stream()
                    .anyMatch(group -> group.formation().equals(PURSUIT));
            Check.that(
                    !pursues || (single && pursuit.isPresent() && paths.isEmpty() && lanes.isEmpty()),
                    "a pursuit wave is one group with its pursuit block (break_at) and no paths");
            Check.that(pursues || pursuit.isEmpty(), "only a pursuit wave has a pursuit block");
            Check.that(
                    clockwise.isEmpty()
                            || groups.orElseThrow().stream()
                                    .allMatch(group -> group.formation().equals("circle")),
                    "only a circle wave turns clockwise");
            // M9 part B (design/enemies/air/talon, Squad leader): a V-wing's tip flies its unit's leader airframe.
            leader.ifPresent(place -> Check.that(LEADER_TIP.equals(place), "leader: only 'tip', was '" + place + "'"));
            Check.that(
                    leader.isEmpty()
                            || (single
                                    && groups.orElseThrow()
                                            .getFirst()
                                            .formation()
                                            .equals("V-wing")),
                    "leader: tip only on a V-wing of one group");
            // M9 part B (design/enemies, Rock riding): its units ride its rocks, riders on each.
            Check.that(
                    onRocks.isEmpty()
                            || (single
                                    && paths.isEmpty()
                                    && lanes.isEmpty()
                                    && easy.flatMap(Change::count).isEmpty()
                                    && hard.flatMap(Change::count).isEmpty()
                                    && groups.orElseThrow().getFirst().count()
                                            == onRocks.get().rocks().size()
                                                    * onRocks.get().riders()),
                    "on_rocks: one group of rocks × riders units on every difficulty, no paths");
        }

        /** M9 part B: the place of a V-wing's leader, its tip ({@link #leader}). */
        public static final String LEADER_TIP = "tip";

        /** M5 part F: whether a circle wave orbits clockwise on the screen. */
        public boolean turnsClockwise() {
            return clockwise.orElse(false);
        }

        /** M5 part E: the formation of units scattered over an area that enters with the sea. */
        public static final String FIELD = "field";

        /** M7 part G: the formation of a flock that holds a band behind the ship and lunges (Level 20's swarm). */
        public static final String PURSUIT = "pursuit";

        /** M8 part E: whether its units are nests on the walls ({@code wall}). */
        public boolean onWalls() {
            return wall.isPresent();
        }

        /** M8 part G: whether its units are nests standing on the ground ({@code on_ground}). */
        public boolean onGroundNests() {
            return onGround.isPresent();
        }

        /** M8 part E: whether its nests stand in pairs facing each other ({@code wall: opposite}). */
        public boolean opposite() {
            return wall.filter(OPPOSITE::equals).isPresent();
        }

        /** M7 part G: whether it is a pursuit (a {@code pursuit} wave). */
        public boolean pursues() {
            return pursuit.isPresent();
        }

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

    /**
     * M7 part G (design/campaign Level 20, The pursuit swarm): a {@code pursuit} wave's numbers. Its flock enters from
     * the top edge (from: rear, in a reversed section) at the wave's size, and {@code refill} more motes (the
     * formation lever's share on easy and hard) join from the top edge every {@code refill_every} s after its entry
     * while there is room in the flock, until {@code break_at} (level time), when the leader climbs off the top edge
     * and the members follow, alive. Every other number is its unit's {@code pursuit} block's unless given here: the
     * {@code band} (px below the top edge its leader holds), the sideways {@code lag} (px/s), {@code lunge_every} and
     * {@code tell} (s), the {@code dive}'s and the {@code climb}'s speeds (px/s) and the dive's {@code overshoot}
     * (px below the ship). The unit's difficulty hooks still apply on easy and hard.
     */
    public record Pursuit(
            double breakAt,
            Optional<Integer> refill,
            Optional<Double> refillEvery,
            Optional<Double> lungeEvery,
            Optional<Double> tell,
            Optional<Span> band,
            Optional<Double> lag,
            Optional<Double> dive,
            Optional<Double> overshoot,
            Optional<Double> climb) {
        public Pursuit {
            Check.positive("break_at", breakAt);
            refill.ifPresent(n -> Check.notNegative("refill", n));
            refillEvery.ifPresent(s -> Check.positive("refill_every", s));
            lungeEvery.ifPresent(s -> Check.positive("lunge_every", s));
            tell.ifPresent(s -> Check.positive("tell", s));
            band.ifPresent(
                    b -> Check.that(b.min() >= 0 && b.max() > b.min(), "band: [top, bottom] px below the top edge"));
            lag.ifPresent(s -> Check.positive("lag", s));
            dive.ifPresent(s -> Check.positive("dive", s));
            overshoot.ifPresent(d -> Check.notNegative("overshoot", d));
            climb.ifPresent(s -> Check.positive("climb", s));
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
    /**
     * M9 part B (design/campaign Level 29, the rescue halt): the held arena's halt cycle: {@code waves} (plain front
     * waves, each {@code t} s from the halt's start, below {@code every}) repeating every {@code every} s while a holder
     * stands, cut at the last one's fall.
     */
    public record HaltWaves(double every, List<Wave> waves) {
        public HaltWaves {
            waves = List.copyOf(waves);
            Check.positive("halt_waves.every", every);
            Check.notEmpty("halt_waves.waves", waves);
            for (Wave wave : waves) {
                Check.that(wave.t() < every, "halt_waves: every wave starts inside the cycle, was " + wave.t());
                Check.that(
                        wave.from() == LevelData.Entry.FRONT
                                && wave.warning().isEmpty()
                                && wave.onRocks().isEmpty()
                                && wave.leader().isEmpty(),
                        "halt_waves: plain waves from the front, without warnings, rocks or leaders");
            }
        }
    }

    /**
     * M9 part C (design/enemies/space/minelayer; design/campaign Level 30, Mine lines in this level): a mine layers'
     * wave's lines: unit i lays at the row {@code rows[i % n]} px below the top edge with its gap at the cluster boundary
     * nearest {@code gaps[i % m]} px from the left edge (left-centre ≈ 160, right-centre ≈ 320: authored per wave).
     */
    public record MineLines(List<Double> rows, List<Double> gaps) {
        public MineLines {
            rows = List.copyOf(rows);
            gaps = List.copyOf(gaps);
            Check.notEmpty("mine_lines.rows", rows);
            Check.notEmpty("mine_lines.gaps", gaps);
            rows.forEach(row -> Check.that(
                    row > 0 && row < PlayField.HEIGHT, "mine_lines.rows: px below the top edge, was " + row));
            gaps.forEach(gap ->
                    Check.that(gap >= 0 && gap <= PlayField.WIDTH, "mine_lines.gaps: an x on the field, was " + gap));
        }
    }

    /**
     * M9 part B (design/enemies, Rock riding; design/campaign Level 29): a wave's rocks, each a small breakable chunk of
     * the level's {@code debris} chunk {@code chunk}, carrying {@code riders} of the wave's units round its rim.
     */
    /**
     * M9 part F (Level 33, QF-1 a pending round 66): a prey wing's refuelling hold instead of its unit's swoop: its units
     * come down to {@code depth} ({@code [min, max]} px below the top edge, its formation's stops), hold {@code hold} s
     * and leave straight up; latched by a leech they leave it and drift down instead.
     */
    public record PreyHold(double hold, Span depth) {
        public PreyHold {
            Check.positive("prey.hold", hold);
            Check.that(
                    depth.min() > 0 && depth.max() < PlayField.HEIGHT,
                    "prey.depth: [min, max] px below the top edge, on the play field");
        }
    }

    public record OnRocks(String chunk, int riders, List<RockData> rocks) {
        public OnRocks {
            rocks = List.copyOf(rocks);
            Check.notEmpty("on_rocks.rocks", rocks);
            Check.that(riders >= 1 && riders <= 5, "on_rocks.riders: 1-5 a rock, was " + riders);
        }
    }

    /**
     * M9 part B: one rock of a wave on rocks: it enters at {@code at} ({@code [x, y]}, y px below the top edge: above
     * it for a rock from the front, a side edge's x for one from a side) and drifts at {@code drift} ({@code [x, y]}
     * px/s on the screen, the scroll included; y up as the debris': negative drifts down the screen).
     */
    public record RockData(Point at, Point drift) {
        public RockData {
            Check.that(drift.y() < 0 || drift.x() != 0, "on_rocks: a rock drifts onto and across the screen");
        }
    }

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
