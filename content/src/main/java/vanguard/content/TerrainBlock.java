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
 */
public record TerrainBlock(
        Optional<List<WallPoint>> left,
        Optional<List<WallPoint>> right,
        Optional<List<Island>> islands,
        Contact contact) {
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
     * difficulty (shield first, overflow to armour), a push of {@code push} px along the wall's normal over {@code
     * push_seconds}, then {@code cooldown} s in which a further touch only clamps.
     */
    public record Contact(PerDifficulty<Double> damage, double push, double pushSeconds, double cooldown) {
        public Contact {
            for (Difficulty difficulty : Difficulty.values()) {
                Check.positive("damage." + difficulty.name().toLowerCase(java.util.Locale.ROOT), damage.of(difficulty));
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
