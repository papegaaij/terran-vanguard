package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import vanguard.sim.PlayField;

/**
 * M7 part C, a level's {@code terrain} (design/campaign Level 16, Terrain; M1–M3 = a, the camera model MA.1 = b of
 * round 36): the collision lines of the canyon's two walls and its rock islands on the play plane, which scroll with
 * the ground (factor 1), and what a touch of rock costs. Named so beside {@link EnemyData.Terrain}, the {@code
 * terrain} movement.
 *
 * <p><b>Coordinates.</b> A wall point is {@code [scroll, x]}: the ground scroll at which that row of the ground
 * reaches the play field's <em>top edge</em>, and the wall's collision line (its foot) at {@code x} px from the
 * field's left edge, linear between points; a wall without points at a row is no wall there. An island is a polygon
 * of {@code [x, scroll]} points, the same rows. A point of the screen at {@code y} px from the bottom edge (y up)
 * lies on row {@link #row(double, double) S = groundScroll + y − 540} (the one helper; design/tech/architecture). A
 * ground placement {@code [t, x]} entering at the top edge at {@code t} (its bottom edge there, as the simulation
 * roots it) has its centre on row {@code scrollAt(t) + h / 2}; a backdrop piece passing the middle at {@code t} on
 * row {@code scrollAt(t) − 270}.
 *
 * @param left the left wall's collision line, rock left of it
 * @param right the right wall's collision line, rock right of it
 * @param islands the rock islands (Level 16's fork mesa and rock pillar), rock inside
 * @param contact what a touch of rock costs the ship (and Rook)
 * @param notches M8 part E (design/campaign Level 25, The caves; m8e-gaps.md §1.1, §5 E5): caves cut into the walls,
 *     flyable dead ends with rock on three sides; the geometry rules (width, channels, slope) read the walls' lines
 *     without them, {@link #rock} opens them
 * @param forks M9 part E (design/campaign Level 32, The forks; route choice Q5 a): the islands where the route is chosen,
 *     fork 1 first: when an island's tip reaches the ship's row, the channel the ship's centre is in is the branch; the
 *     waves, ground targets and radio cues of a fork's {@code branch} fly, wake or play only on their side
 */
public record TerrainBlock(
        Optional<List<WallPoint>> left,
        Optional<List<WallPoint>> right,
        Optional<List<Island>> islands,
        Contact contact,
        Optional<List<Notch>> notches,
        Optional<List<Fork>> forks) {
    /** The flyable width the validator asks for wherever there are walls, px (the ship's hull is 36 px wide). */
    public static final double MIN_WIDTH = 180;

    /** The least width of a channel beside an island, px. */
    public static final double MIN_CHANNEL = 130;

    /** The most a wall moves sideways per px of scroll (at 230 px/s, 115 px/s). */
    public static final double MAX_SLOPE = 0.5;

    /** The band of floor along a collision line where gameplay at a wall stands (anchors, turrets, the crawler). */
    public static final double FOOT_BAND = 24;

    public TerrainBlock {
        Check.that(
                left.isPresent() || right.isPresent() || islands.isPresent(),
                "terrain: give a wall (left, right) or islands");
        left = left.map(points -> wall("left", points));
        right = right.map(points -> wall("right", points));
        islands = islands.map(List::copyOf);
        islands.ifPresent(list -> Check.notEmpty("islands", list));
        forks = forks.map(List::copyOf);
        int islandCount = islands.map(List::size).orElse(0);
        forks.ifPresent(list -> {
            Check.that(
                    !list.isEmpty() && list.size() <= vanguard.sim.Branch.MOST_FORKS,
                    "forks: one to " + vanguard.sim.Branch.MOST_FORKS + " forks");
            for (int i = 0; i < list.size(); i++) {
                int island = list.get(i).island();
                Check.that(island >= 0 && island < islandCount, "forks[" + i + "]: one of the islands (from 0)");
                for (int j = 0; j < i; j++) {
                    Check.that(list.get(j).island() != island, "forks[" + i + "]: each island is one fork");
                }
            }
        });
        notches = notches.map(List::copyOf);
        if (notches.isPresent()) {
            List<Notch> list = notches.get();
            Check.notEmpty("notches", list);
            for (int i = 0; i < list.size(); i++) {
                Notch notch = list.get(i);
                Optional<List<WallPoint>> wall = notch.side() == Side.LEFT ? left : right;
                Check.that(
                        wall.isPresent()
                                && notch.low() >= wall.get().getFirst().scroll()
                                && notch.high() <= wall.get().getLast().scroll(),
                        "notches[" + i + "]: a notch lies on its wall's rows");
                for (int j = 0; j < i; j++) {
                    Notch other = list.get(j);
                    Check.that(
                            other.side() != notch.side() || notch.low() >= other.high() || notch.high() <= other.low(),
                            "notches[" + i + "]: the notches of one wall do not overlap");
                }
            }
        }
    }

    /** The walls and islands without M8 part E's notches. */
    public TerrainBlock(
            Optional<List<WallPoint>> left,
            Optional<List<WallPoint>> right,
            Optional<List<Island>> islands,
            Contact contact) {
        this(left, right, islands, contact, Optional.empty(), Optional.empty());
    }

    /** M9 part E: the forks, none if not given (fork 1 first). */
    public List<Fork> forkList() {
        return forks.orElse(List.of());
    }

    /** M9 part E (design/campaign Level 32, The forks): a fork at island {@code island} (its index, from 0). */
    public record Fork(int island) {}

    /**
     * M9 part E (design/campaign Level 32, The forks): a wave's, ground target's or radio cue's branch: the {@code
     * side} ({@code left} or {@code right}) channel of fork {@code fork} (from 1).
     */
    public record Branch(int fork, String side) {
        public Branch {
            Check.that(fork >= 1, "branch.fork: from 1, was " + fork);
            Check.that(side.equals("left") || side.equals("right"), "branch.side: left or right, was '" + side + "'");
        }

        /** The simulation's branch (fork from 0). */
        public vanguard.sim.Branch sim() {
            return new vanguard.sim.Branch(
                    fork - 1, side.equals("left") ? vanguard.sim.Branch.LEFT : vanguard.sim.Branch.RIGHT);
        }
    }

    /** M8 part E: the notches, none if not given (in the order given: a cave wave names its notch by this index). */
    public List<Notch> notchList() {
        return notches.orElse(List.of());
    }

    /**
     * M8 part E: the notch cut at {@code row} into the wall on {@code side}, or null: the one whose rows hold it.
     */
    public Notch notchAt(Side side, double row) {
        for (Notch notch : notchList()) {
            if (notch.side() == side && row >= notch.low() && row <= notch.high()) {
                return notch;
            }
        }
        return null;
    }

    private static List<WallPoint> wall(String side, List<WallPoint> points) {
        Check.that(points.size() >= 2, side + ": a wall has two points or more");
        for (int i = 1; i < points.size(); i++) {
            Check.that(
                    points.get(i).scroll() > points.get(i - 1).scroll(),
                    side + "[" + i + "]: the points in scroll order, each after the one before");
        }
        return List.copyOf(points);
    }

    /**
     * The row of the ground under a point of the screen: {@code y} px from the bottom edge (y up) while the ground has
     * scrolled {@code groundScroll} px; the row a wall point's or an island point's scroll names (it is at the top
     * edge when the ground has scrolled that far).
     */
    public static double row(double groundScroll, double y) {
        return groundScroll + y - PlayField.HEIGHT;
    }

    /** The left wall's collision line at a row, px; NaN where it has no point (no wall). */
    public double leftAt(double row) {
        return left.map(points -> at(points, row, false)).orElse(Double.NaN);
    }

    /** The right wall's collision line at a row, px; NaN where it has no point (no wall). */
    public double rightAt(double row) {
        return right.map(points -> at(points, row, false)).orElse(Double.NaN);
    }

    /**
     * The left wall's height at a row, 0 (flat: the floor runs on, Level 16's canyon mouth) to 1 (its full rise to the
     * plateau at about 1.25); NaN where it has no wall. Presentation only: the simulation reads the collision line.
     */
    public double leftHeightAt(double row) {
        return left.map(points -> at(points, row, true)).orElse(Double.NaN);
    }

    /** The right wall's height at a row, as {@link #leftHeightAt}. */
    public double rightHeightAt(double row) {
        return right.map(points -> at(points, row, true)).orElse(Double.NaN);
    }

    private static double at(List<WallPoint> points, double row, boolean height) {
        if (row < points.getFirst().scroll() || row > points.getLast().scroll()) {
            return Double.NaN;
        }
        for (int i = 1; i < points.size(); i++) {
            WallPoint a = points.get(i - 1);
            WallPoint b = points.get(i);
            if (row <= b.scroll()) {
                double u = (row - a.scroll()) / (b.scroll() - a.scroll());
                double from = height ? a.wallHeight() : a.x();
                double to = height ? b.wallHeight() : b.x();
                return from + (to - from) * u;
            }
        }
        return Double.NaN;
    }

    /** The islands, none if not given. */
    public List<Island> islandList() {
        return islands.orElse(List.of());
    }

    /**
     * Whether the point {@code x} on {@code row} is rock: beyond a wall's collision line or inside an island. A point
     * exactly on a line is not.
     */
    public boolean rock(double row, double x) {
        double l = leftAt(row);
        double r = rightAt(row);
        // M8 part E: a notch opens its wall's rock back by its depth over its rows.
        Notch leftNotch = Double.isNaN(l) ? null : notchAt(Side.LEFT, row);
        Notch rightNotch = Double.isNaN(r) ? null : notchAt(Side.RIGHT, row);
        if (leftNotch != null) {
            l -= leftNotch.depth();
        }
        if (rightNotch != null) {
            r += rightNotch.depth();
        }
        if (!Double.isNaN(l) && x < l || !Double.isNaN(r) && x > r) {
            return true;
        }
        for (Island island : islandList()) {
            double[] span = island.spanAt(row);
            if (span != null && x > span[0] && x < span[1]) {
                return true;
            }
        }
        return false;
    }

    /** The px from {@code x} to the nearest collision line on {@code row} (a wall's foot or an island's); +∞ for none. */
    public double toFoot(double row, double x) {
        double best = Double.POSITIVE_INFINITY;
        for (double line : new double[] {leftAt(row), rightAt(row)}) {
            if (!Double.isNaN(line)) {
                best = Math.min(best, Math.abs(x - line));
            }
        }
        for (Island island : islandList()) {
            double[] span = island.spanAt(row);
            if (span != null) {
                best = Math.min(best, Math.min(Math.abs(x - span[0]), Math.abs(x - span[1])));
            }
        }
        return best;
    }

    /**
     * The flyable spans on a row, left to right ({@code [from, to]} px): the field between the walls (its edges where
     * there is no wall) less the islands; two channels beside an island.
     */
    public List<double[]> spans(double row) {
        double l = leftAt(row);
        double r = rightAt(row);
        List<double[]> rocks = new ArrayList<>();
        for (Island island : islandList()) {
            double[] span = island.spanAt(row);
            if (span != null) {
                rocks.add(span);
            }
        }
        rocks.sort((a, b) -> Double.compare(a[0], b[0]));
        List<double[]> out = new ArrayList<>();
        double from = Double.isNaN(l) ? 0 : l;
        for (double[] rock : rocks) {
            out.add(new double[] {from, rock[0]});
            from = rock[1];
        }
        out.add(new double[] {from, Double.isNaN(r) ? PlayField.WIDTH : r});
        return out;
    }

    /**
     * Every row where a collision line bends (the walls' and the islands' points), in order: the flyable width and
     * every channel are linear between two of them, so their least values lie on these rows.
     */
    public List<Double> breaks() {
        TreeSet<Double> rows = new TreeSet<>();
        left.ifPresent(points -> points.forEach(p -> rows.add(p.scroll())));
        right.ifPresent(points -> points.forEach(p -> rows.add(p.scroll())));
        for (Island island : islandList()) {
            island.points().forEach(p -> rows.add(p.y()));
        }
        return List.copyOf(rows);
    }

    /** M8 part E: the wall a notch is cut into. */
    public enum Side {
        LEFT,
        RIGHT
    }

    /**
     * M8 part E (design/campaign Level 25, The caves): a cave cut into the wall on {@code side}: over the rows {@code
     * scroll ± width / 2} its rock steps back {@code depth} px from the wall's line, open water with a floor, a back and
     * a ceiling (70 deep and 90 wide in Level 25).
     *
     * @param scroll the row of its middle (the ground scroll at which it reaches the top edge)
     * @param width its opening along the wall, rows (px)
     * @param depth how far its back lies behind the wall's line, px
     */
    public record Notch(Side side, double scroll, double width, double depth) {
        public Notch {
            Check.notNegative("scroll", scroll);
            Check.positive("width", width);
            Check.positive("depth", depth);
        }

        /** Its lowest row (its floor). */
        public double low() {
            return scroll - width / 2;
        }

        /** Its highest row (its ceiling). */
        public double high() {
            return scroll + width / 2;
        }
    }

    /**
     * A wall's point, written {@code [scroll, x]} or {@code [scroll, x, height]}: the row (the ground scroll at which
     * it reaches the top edge), the collision line's x and (optional) the wall's height there, 0 (flat) to 1 (its full
     * rise; Level 16's canyon mouth and outflow ramp it), 1 if not given, linear between points.
     */
    public record WallPoint(double scroll, double x, Optional<Double> height) {
        public WallPoint {
            Check.notNegative("scroll", scroll);
            Check.that(x >= 0 && x <= PlayField.WIDTH, "x: on the play field (0–480), was " + x);
            height.ifPresent(h -> Check.that(h >= 0 && h <= 1, "height: 0 (flat) to 1 (full), was " + h));
        }

        /** The wall's height at this point: its {@code height}, 1 if not given. */
        public double wallHeight() {
            return height.orElse(1.0);
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static WallPoint of(List<Double> values) {
            Check.that(
                    values != null && (values.size() == 2 || values.size() == 3),
                    "a wall point is [scroll, x] or [scroll, x, height], was " + values);
            return new WallPoint(
                    values.get(0), values.get(1), values.size() == 3 ? Optional.of(values.get(2)) : Optional.empty());
        }
    }

    /**
     * A rock island (a mesa rising from the floor like the walls, Level 16's fork mesa and rock pillar): its {@code
     * points} a polygon of {@code [x, scroll]} (its foot, the collision line), its {@code look} the art's name (the
     * backdrop's {@code <look>-top} and {@code <look>-face} images).
     */
    public record Island(String name, String look, List<Point> points) {
        public Island {
            Check.that(!name.isBlank(), "name: the island's name");
            Check.that(look.matches("[a-z0-9-]+"), "look: an art name in kebab case, was '" + look + "'");
            Check.that(points.size() >= 3, "points: an island is a polygon of three points or more");
            points.forEach(p -> Check.that(
                    p.x() >= 0 && p.x() <= PlayField.WIDTH, "points: x on the play field (0–480), was " + p.x()));
            points = List.copyOf(points);
        }

        /** Its first and last row. */
        public double[] rows() {
            double from = Double.POSITIVE_INFINITY;
            double to = Double.NEGATIVE_INFINITY;
            for (Point p : points) {
                from = Math.min(from, p.y());
                to = Math.max(to, p.y());
            }
            return new double[] {from, to};
        }

        /** Its rock on a row, {@code [from, to]} px (the outermost crossings of its outline), or null off it. */
        public double[] spanAt(double row) {
            double from = Double.POSITIVE_INFINITY;
            double to = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < points.size(); i++) {
                Point a = points.get(i);
                Point b = points.get((i + 1) % points.size());
                double low = Math.min(a.y(), b.y());
                double high = Math.max(a.y(), b.y());
                if (row < low || row > high) {
                    continue;
                }
                if (a.y() == b.y()) {
                    from = Math.min(from, Math.min(a.x(), b.x()));
                    to = Math.max(to, Math.max(a.x(), b.x()));
                } else {
                    double x = a.x() + (b.x() - a.x()) * (row - a.y()) / (b.y() - a.y());
                    from = Math.min(from, x);
                    to = Math.max(to, x);
                }
            }
            return from <= to ? new double[] {from, to} : null;
        }
    }

    /**
     * What a touch of rock costs (design/campaign Level 16, Wall contact, M1 = a): {@code damage} per touch on each
     * difficulty (shield first, overflow to armour; 0: a push without damage, Level 28's shaft), a push of {@code push} px along the wall's normal over {@code
     * push_seconds}, then {@code cooldown} s in which a further touch only clamps.
     */
    public record Contact(PerDifficulty<Double> damage, double push, double pushSeconds, double cooldown) {
        public Contact {
            for (Difficulty difficulty : Difficulty.values()) {
                // M8 part H (Level 28's shaft, gap check §5 H9): 0 pushes without damage.
                Check.notNegative(
                        "damage." + difficulty.name().toLowerCase(java.util.Locale.ROOT), damage.of(difficulty));
            }
            Check.positive("push", push);
            Check.positive("push_seconds", pushSeconds);
            Check.positive("cooldown", cooldown);
        }
    }

    /**
     * A point of a ground path, written {@code [scroll, x]} in the walls' rows (a Threadcrawler's {@code crawl}, Level
     * 16): the row and its x.
     */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record GroundPoint(double scroll, double x) {
        public GroundPoint {
            Check.notNegative("scroll", scroll);
            Check.that(x >= 0 && x <= PlayField.WIDTH, "x: on the play field (0–480), was " + x);
        }
    }
}
