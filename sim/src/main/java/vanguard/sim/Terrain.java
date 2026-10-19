package vanguard.sim;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * M7 part C: a level's rock (design/campaign Level 16, Terrain; M2 = a, camera model b): the two canyon
 * walls' collision lines and the rock islands, in ground space. Immutable, and its queries allocate
 * nothing.
 *
 * <p>Ground space: a <em>row</em> is the ground scroll at which that row of the ground reaches the play
 * field's top edge; a play-field point (x, y), y up from the bottom edge, lies on row {@link #row} =
 * {@code groundScroll + y − 540}. A wall is a polyline of {@code [row, x]} points, linear between
 * them (nothing smoother: Level 16's rock pillar needs exactly the points it gives), with no wall
 * before its first row or after its last; the left wall's rock is everything left of its line, the
 * right wall's everything right of it. An island is a polygon of {@code [x, row]} points (as the
 * level data writes it); its contact normal assumes a convex one (the rock test is exact for any simple
 * polygon). The data's third value per wall point (the wall's drawn height) is presentation only and
 * not kept here.
 */
public final class Terrain {
    /**
     * A wall's collision line.
     *
     * @param scrolls the rows of its points, increasing
     * @param xs the line's x at each, px from the field's left edge
     */
    public record Wall(double[] scrolls, double[] xs) {
        public Wall {
            scrolls = scrolls.clone();
            xs = xs.clone();
            if (scrolls.length < 2 || scrolls.length != xs.length) {
                throw new IllegalArgumentException("a wall has at least two points, each a row and an x");
            }
            for (int i = 1; i < scrolls.length; i++) {
                if (!(scrolls[i] > scrolls[i - 1])) {
                    throw new IllegalArgumentException("a wall's rows increase: " + Arrays.toString(scrolls));
                }
            }
        }

        @Override
        public double[] scrolls() {
            return scrolls.clone();
        }

        @Override
        public double[] xs() {
            return xs.clone();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Wall wall && Arrays.equals(scrolls, wall.scrolls) && Arrays.equals(xs, wall.xs);
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(scrolls) + Arrays.hashCode(xs);
        }

        @Override
        public String toString() {
            return "Wall" + Arrays.toString(scrolls) + Arrays.toString(xs);
        }
    }

    /**
     * A rock island's foot.
     *
     * @param xs its corners' x, px
     * @param scrolls its corners' rows
     */
    public record Island(double[] xs, double[] scrolls) {
        public Island {
            xs = xs.clone();
            scrolls = scrolls.clone();
            if (xs.length < 3 || xs.length != scrolls.length) {
                throw new IllegalArgumentException("an island has at least three corners, each an x and a row");
            }
        }

        @Override
        public double[] xs() {
            return xs.clone();
        }

        @Override
        public double[] scrolls() {
            return scrolls.clone();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Island island
                    && Arrays.equals(xs, island.xs)
                    && Arrays.equals(scrolls, island.scrolls);
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(xs) + Arrays.hashCode(scrolls);
        }

        @Override
        public String toString() {
            return "Island" + Arrays.toString(xs) + Arrays.toString(scrolls);
        }
    }

    private static final double WIDTH = PlayField.WIDTH;

    /** The left wall's rows and x; null without one. */
    private final double[] leftRows;

    private final double[] leftXs;
    private final double[] rightRows;
    private final double[] rightXs;
    /** Per island its corners, and its bounds. */
    private final double[][] islandXs;

    private final double[][] islandRows;
    private final double[] islandMinX;
    private final double[] islandMaxX;
    private final double[] islandMinRow;
    private final double[] islandMaxRow;

    public Terrain(Optional<Wall> left, Optional<Wall> right, List<Island> islands) {
        leftRows = left.map(Wall::scrolls).orElse(null);
        leftXs = left.map(Wall::xs).orElse(null);
        rightRows = right.map(Wall::scrolls).orElse(null);
        rightXs = right.map(Wall::xs).orElse(null);
        int n = islands.size();
        islandXs = new double[n][];
        islandRows = new double[n][];
        islandMinX = new double[n];
        islandMaxX = new double[n];
        islandMinRow = new double[n];
        islandMaxRow = new double[n];
        for (int i = 0; i < n; i++) {
            islandXs[i] = islands.get(i).xs();
            islandRows[i] = islands.get(i).scrolls();
            islandMinX[i] = Arrays.stream(islandXs[i]).min().orElseThrow();
            islandMaxX[i] = Arrays.stream(islandXs[i]).max().orElseThrow();
            islandMinRow[i] = Arrays.stream(islandRows[i]).min().orElseThrow();
            islandMaxRow[i] = Arrays.stream(islandRows[i]).max().orElseThrow();
        }
    }

    /** The ground row of play-field height {@code y} at {@code groundScroll}: {@code groundScroll + y − 540}. */
    public static double row(double groundScroll, double y) {
        return groundScroll + y - PlayField.HEIGHT;
    }

    /** The play-field height of ground row {@code row} at {@code groundScroll} (the inverse of {@link #row}). */
    public static double y(double groundScroll, double row) {
        return row - groundScroll + PlayField.HEIGHT;
    }

    /** The left wall's collision line at {@code row}, px; NaN where there is no left wall. */
    public double leftAt(double row) {
        return leftRows == null ? Double.NaN : at(leftRows, leftXs, row);
    }

    /** The right wall's collision line at {@code row}, px; NaN where there is no right wall. */
    public double rightAt(double row) {
        return rightRows == null ? Double.NaN : at(rightRows, rightXs, row);
    }

    /** The number of islands. */
    public int islands() {
        return islandXs.length;
    }

    /**
     * Whether the box of half size {@code halfW} × {@code halfH} around the play-field point (x, y)
     * overlaps rock (a wall's or an island's); touching its line does not count. A wall's rock counts on
     * the play field only: a wall at the field's edge (the canyon's mouth) is not touched by a hull that
     * reaches past the edge.
     */
    public boolean rock(double groundScroll, double x, double y, double halfW, double halfH) {
        double r0 = row(groundScroll, y - halfH);
        double r1 = row(groundScroll, y + halfH);
        if (leftRows != null) {
            double most = most(leftRows, leftXs, r0, r1, 1);
            if (Math.max(x - halfW, 0) < most) {
                return true;
            }
        }
        if (rightRows != null) {
            double least = -most(rightRows, rightXs, r0, r1, -1);
            if (Math.min(x + halfW, WIDTH) > least) {
                return true;
            }
        }
        for (int i = 0; i < islandXs.length; i++) {
            if (boxInIsland(i, x - halfW, x + halfW, r0, r1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The lowest play-field height at or above {@code fromY} where the column of half width {@code halfW}
     * around {@code x} meets rock (a beam's end), +∞ for none: {@code fromY} itself when it starts in rock.
     */
    public double ceiling(double groundScroll, double x, double halfW, double fromY) {
        double r0 = row(groundScroll, fromY);
        double xl = x - halfW;
        double xr = x + halfW;
        double best = Double.POSITIVE_INFINITY;
        if (leftRows != null) {
            best = Math.min(best, firstBeyond(leftRows, leftXs, r0, xl, 1));
        }
        if (rightRows != null) {
            best = Math.min(best, firstBeyond(rightRows, rightXs, r0, -xr, -1));
        }
        for (int i = 0; i < islandXs.length && best > r0; i++) {
            if (islandMaxX[i] <= xl || islandMinX[i] >= xr || islandMaxRow[i] < r0 || islandMinRow[i] >= best) {
                continue;
            }
            if (inside(i, x, r0)) {
                best = r0;
                break;
            }
            double[] xs = islandXs[i];
            double[] rows = islandRows[i];
            for (int k = 0; k < xs.length; k++) {
                int j = k + 1 == xs.length ? 0 : k + 1;
                double low = clipLow(xs[k], rows[k], xs[j], rows[j], xl, xr, r0, Double.POSITIVE_INFINITY);
                if (low < best) {
                    best = low;
                }
            }
        }
        return best == Double.POSITIVE_INFINITY ? best : y(groundScroll, best);
    }

    /**
     * The left edge of the flyable channel holding x at play-field height y, px: the left wall, an
     * island's right side or the field's left edge (0); NaN when x lies in rock there.
     */
    public double spanLeft(double groundScroll, double y, double x) {
        double r = row(groundScroll, y);
        double lo = boundLeft(r);
        double hi = boundRight(r);
        if (!(x > lo && x < hi) || inAnyIsland(x, r)) {
            return Double.NaN;
        }
        for (int i = 0; i < islandXs.length; i++) {
            if (r < islandMinRow[i] || r > islandMaxRow[i] || islandMinX[i] >= x) {
                continue;
            }
            double[] xs = islandXs[i];
            double[] rows = islandRows[i];
            for (int k = 0; k < xs.length; k++) {
                int j = k + 1 == xs.length ? 0 : k + 1;
                double cross = crossing(xs[k], rows[k], xs[j], rows[j], r);
                if (cross <= x && cross > lo) {
                    lo = cross;
                }
            }
        }
        return lo;
    }

    /**
     * The right edge of the flyable channel holding x at play-field height y, px: the right wall, an
     * island's left side or the field's right edge (480); NaN when x lies in rock there.
     */
    public double spanRight(double groundScroll, double y, double x) {
        double r = row(groundScroll, y);
        double lo = boundLeft(r);
        double hi = boundRight(r);
        if (!(x > lo && x < hi) || inAnyIsland(x, r)) {
            return Double.NaN;
        }
        for (int i = 0; i < islandXs.length; i++) {
            if (r < islandMinRow[i] || r > islandMaxRow[i] || islandMaxX[i] <= x) {
                continue;
            }
            double[] xs = islandXs[i];
            double[] rows = islandRows[i];
            for (int k = 0; k < xs.length; k++) {
                int j = k + 1 == xs.length ? 0 : k + 1;
                double cross = crossing(xs[k], rows[k], xs[j], rows[j], r);
                if (cross >= x && cross < hi) {
                    hi = cross;
                }
            }
        }
        return hi;
    }

    /**
     * The flyable channels side by side at play-field height y: 1 in an open canyon, one more per island
     * across it there (islands stand apart from the walls and from each other: the validator's rule).
     */
    public int channels(double groundScroll, double y) {
        double r = row(groundScroll, y);
        double lo = boundLeft(r);
        double hi = boundRight(r);
        int crossings = 0;
        for (int i = 0; i < islandXs.length; i++) {
            if (r < islandMinRow[i] || r > islandMaxRow[i]) {
                continue;
            }
            double[] xs = islandXs[i];
            double[] rows = islandRows[i];
            for (int k = 0; k < xs.length; k++) {
                int j = k + 1 == xs.length ? 0 : k + 1;
                double cross = crossing(xs[k], rows[k], xs[j], rows[j], r);
                if (cross > lo && cross < hi) {
                    crossings++;
                }
            }
        }
        return 1 + crossings / 2;
    }

    /**
     * The x nearest {@code x} at play-field height y where a box of half width {@code margin} (and no
     * height) stands clear of rock: {@code x} itself when it does; else beside the wall or the island it
     * is in (the nearer side of an island). The field's edges are not rock: off the field it changes only
     * what a wall reaches. For what rock pushes out (pickups, mines, a swoop, a swarm) and what lands on it
     * (bombs and shells burst at the wall's foot).
     */
    public double flyableX(double groundScroll, double y, double x, double margin) {
        double r = row(groundScroll, y);
        double left = leftAt(r);
        double right = rightAt(r);
        double out = x;
        for (int pass = 0; pass < 2; pass++) {
            double lo = Double.isNaN(left) ? Double.NEGATIVE_INFINITY : left + margin;
            double hi = Double.isNaN(right) ? Double.POSITIVE_INFINITY : right - margin;
            if (out < lo) {
                out = lo;
            } else if (out > hi) {
                out = hi;
            }
            for (int i = 0; i < islandXs.length; i++) {
                if (r < islandMinRow[i] || r > islandMaxRow[i]) {
                    continue;
                }
                double from = Double.POSITIVE_INFINITY;
                double to = Double.NEGATIVE_INFINITY;
                double[] xs = islandXs[i];
                double[] rows = islandRows[i];
                for (int k = 0; k < xs.length; k++) {
                    int j = k + 1 == xs.length ? 0 : k + 1;
                    double cross = crossing(xs[k], rows[k], xs[j], rows[j], r);
                    if (!Double.isNaN(cross)) {
                        from = Math.min(from, cross);
                        to = Math.max(to, cross);
                    }
                }
                if (from - margin < out && out < to + margin) {
                    out = out - (from - margin) <= (to + margin) - out ? from - margin : to + margin;
                }
            }
        }
        return out;
    }

    /**
     * Whether a hull around (x, y) touches rock (a wall's on the play field only, as {@link #rock}): then
     * {@code out} holds the deepest touch's normal (nx, ny: a unit vector out of the rock, y up) and its
     * depth, px along it.
     */
    public boolean contact(double groundScroll, Hull hull, double x, double y, double[] out) {
        out[0] = 0;
        out[1] = 0;
        out[2] = 0;
        boolean any = false;
        List<Hull.Part> parts = hull.parts();
        for (int p = 0; p < parts.size(); p++) {
            Hull.Part part = parts.get(p);
            double cx = x + part.dx();
            double cy = y + part.dy();
            double hw = part.box().width() / 2;
            double hh = part.box().height() / 2;
            double r0 = row(groundScroll, cy - hh);
            double r1 = row(groundScroll, cy + hh);
            if (leftRows != null) {
                any |= wallContact(leftRows, leftXs, r0, r1, Math.max(cx - hw, 0), 1, out);
            }
            if (rightRows != null) {
                any |= wallContact(rightRows, rightXs, r0, r1, Math.min(cx + hw, WIDTH), -1, out);
            }
            for (int i = 0; i < islandXs.length; i++) {
                any |= islandContact(i, cx, row(groundScroll, cy), hw, hh, out);
            }
        }
        return any;
    }

    /** The left boundary at row r: the left wall, else the field's edge. */
    private double boundLeft(double r) {
        double left = leftAt(r);
        return Double.isNaN(left) ? 0 : Math.max(0, left);
    }

    /** The right boundary at row r: the right wall, else the field's edge. */
    private double boundRight(double r) {
        double right = rightAt(r);
        return Double.isNaN(right) ? WIDTH : Math.min(WIDTH, right);
    }

    private boolean inAnyIsland(double x, double r) {
        for (int i = 0; i < islandXs.length; i++) {
            if (inside(i, x, r)) {
                return true;
            }
        }
        return false;
    }

    /** A polyline's value at row r, linear between its points; NaN outside its rows. */
    private static double at(double[] rows, double[] xs, double r) {
        int last = rows.length - 1;
        if (!(r >= rows[0] && r <= rows[last])) {
            return Double.NaN;
        }
        int k = segment(rows, r);
        return lerp(rows[k], xs[k], rows[k + 1], xs[k + 1], r);
    }

    /** The index of the segment holding row r (inside the rows), by binary search. */
    private static int segment(double[] rows, double r) {
        int lo = 0;
        int hi = rows.length - 1;
        while (hi - lo > 1) {
            int mid = (lo + hi) >>> 1;
            if (rows[mid] <= r) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    private static double lerp(double r0, double x0, double r1, double x1, double r) {
        return x0 + (x1 - x0) * (r - r0) / (r1 - r0);
    }

    /**
     * The largest of {@code sign × value} of a polyline over the rows [r0, r1] it covers (its ends and the
     * points between); −∞ where it covers none of them. Sign 1: the left wall's reach right; −1: the right
     * wall's (negated) reach left.
     */
    private static double most(double[] rows, double[] xs, double r0, double r1, int sign) {
        int last = rows.length - 1;
        double from = Math.max(r0, rows[0]);
        double to = Math.min(r1, rows[last]);
        if (from > to) {
            return Double.NEGATIVE_INFINITY;
        }
        double most = Math.max(sign * at(rows, xs, from), sign * at(rows, xs, to));
        for (int k = 0; k <= last; k++) {
            if (rows[k] > from && rows[k] < to) {
                most = Math.max(most, sign * xs[k]);
            }
        }
        return most;
    }

    /**
     * The first row at or above r0 where a polyline's {@code sign × value} exceeds {@code bound} (where the
     * wall's rock reaches into the column), +∞ for none.
     */
    private static double firstBeyond(double[] rows, double[] xs, double r0, double bound, int sign) {
        int last = rows.length - 1;
        if (r0 > rows[last]) {
            return Double.POSITIVE_INFINITY;
        }
        int k = r0 <= rows[0] ? 0 : segment(rows, r0);
        for (; k < last; k++) {
            double start = Math.max(rows[k], r0);
            double a = sign * lerp(rows[k], xs[k], rows[k + 1], xs[k + 1], start);
            double b = sign * xs[k + 1];
            if (a > bound) {
                return start;
            }
            if (b > bound) {
                return start + (bound - a) / (b - a) * (rows[k + 1] - start);
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    /** Where the edge from (ax, ar) to (bx, br) crosses row r, px; NaN when it does not (half-open in r). */
    private static double crossing(double ax, double ar, double bx, double br, double r) {
        if ((ar <= r) == (br <= r)) {
            return Double.NaN;
        }
        return lerp(ar, ax, br, bx, r);
    }

    /** Whether the point (x, row r) lies inside island i (even-odd rule). */
    private boolean inside(int i, double x, double r) {
        if (x <= islandMinX[i] || x >= islandMaxX[i] || r < islandMinRow[i] || r > islandMaxRow[i]) {
            return false;
        }
        double[] xs = islandXs[i];
        double[] rows = islandRows[i];
        boolean in = false;
        for (int k = 0, j = xs.length - 1; k < xs.length; j = k++) {
            double cross = crossing(xs[j], rows[j], xs[k], rows[k], r);
            if (cross > x) {
                in = !in;
            }
        }
        return in;
    }

    /** Whether the box [xl, xr] × [r0, r1] overlaps island i. */
    private boolean boxInIsland(int i, double xl, double xr, double r0, double r1) {
        if (islandMaxX[i] <= xl || islandMinX[i] >= xr || islandMaxRow[i] <= r0 || islandMinRow[i] >= r1) {
            return false;
        }
        if (inside(i, (xl + xr) / 2, (r0 + r1) / 2)) {
            return true;
        }
        double[] xs = islandXs[i];
        double[] rows = islandRows[i];
        for (int k = 0; k < xs.length; k++) {
            int j = k + 1 == xs.length ? 0 : k + 1;
            if (!Double.isNaN(clipLow(xs[k], rows[k], xs[j], rows[j], xl, xr, r0, r1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * The lowest row of the edge from (ax, ar) to (bx, br) clipped to the box [xl, xr] × [rl, rh]
     * (Liang–Barsky), NaN when it misses the box's inside.
     */
    private static double clipLow(
            double ax, double ar, double bx, double br, double xl, double xr, double rl, double rh) {
        double dx = bx - ax;
        double dr = br - ar;
        double t0 = 0;
        double t1 = 1;
        for (int side = 0; side < 4; side++) {
            double p = side == 0 ? -dx : side == 1 ? dx : side == 2 ? -dr : dr;
            double q = side == 0 ? ax - xl : side == 1 ? xr - ax : side == 2 ? ar - rl : rh - ar;
            if (p == 0) {
                if (q <= 0) {
                    return Double.NaN;
                }
            } else {
                double t = q / p;
                if (p < 0) {
                    t0 = Math.max(t0, t);
                } else {
                    t1 = Math.min(t1, t);
                }
            }
        }
        if (!(t0 < t1)) {
            return Double.NaN;
        }
        return Math.min(ar + dr * t0, ar + dr * t1);
    }

    /**
     * A wall's touch on a box whose side toward it is at {@code edge} over rows [r0, r1]: the wall's line
     * reaches past it by h px across at its farthest; the normal is the line's (its slope there), the depth
     * h along it. Records it in {@code out} when deeper than what is there. Sign 1: the left wall.
     */
    private static boolean wallContact(
            double[] rows, double[] xs, double r0, double r1, double edge, int sign, double[] out) {
        int last = rows.length - 1;
        double from = Math.max(r0, rows[0]);
        double to = Math.min(r1, rows[last]);
        if (from > to) {
            return false;
        }
        double bestRow = from;
        double best = sign * at(rows, xs, from);
        double atTo = sign * at(rows, xs, to);
        if (atTo > best) {
            best = atTo;
            bestRow = to;
        }
        for (int k = 0; k <= last; k++) {
            if (rows[k] > from && rows[k] < to && sign * xs[k] > best) {
                best = sign * xs[k];
                bestRow = rows[k];
            }
        }
        double across = best - sign * edge;
        if (!(across > 0)) {
            return false;
        }
        // The slope dx/drow at the farthest point: its segment's (the mean of two at a point between).
        int k = bestRow >= rows[last] ? last - 1 : segment(rows, bestRow);
        double slope = (xs[k + 1] - xs[k]) / (rows[k + 1] - rows[k]);
        if (rows[k] == bestRow && k > 0) {
            slope = (slope + (xs[k] - xs[k - 1]) / (rows[k] - rows[k - 1])) / 2;
        }
        double norm = Math.sqrt(1 + slope * slope);
        double depth = across / norm;
        if (depth > out[2]) {
            // Out of the rock: right for the left wall, left for the right one, across the line's lean.
            out[0] = sign / norm;
            out[1] = -sign * slope / norm;
            out[2] = depth;
        }
        return true;
    }

    /**
     * Island i's touch on the box of half size hw × hh around (cx, row cr) by the separating axes (the
     * box's two and the island's edge normals): the axis of least overlap is the normal, oriented away from
     * the island's centre, the overlap the depth. Records it in {@code out} when deeper.
     */
    private boolean islandContact(int i, double cx, double cr, double hw, double hh, double[] out) {
        if (islandMaxX[i] <= cx - hw
                || islandMinX[i] >= cx + hw
                || islandMaxRow[i] <= cr - hh
                || islandMinRow[i] >= cr + hh) {
            return false;
        }
        double[] xs = islandXs[i];
        double[] rows = islandRows[i];
        int n = xs.length;
        double centreX = 0;
        double centreR = 0;
        for (int k = 0; k < n; k++) {
            centreX += xs[k];
            centreR += rows[k];
        }
        centreX /= n;
        centreR /= n;
        double bestDepth = Double.POSITIVE_INFINITY;
        double bestX = 0;
        double bestY = 0;
        for (int a = -2; a < n; a++) {
            double ax;
            double ay;
            if (a == -2) {
                ax = 1;
                ay = 0;
            } else if (a == -1) {
                ax = 0;
                ay = 1;
            } else {
                int j = a + 1 == n ? 0 : a + 1;
                double ex = xs[j] - xs[a];
                double er = rows[j] - rows[a];
                double length = Math.sqrt(ex * ex + er * er);
                if (length == 0) {
                    continue;
                }
                ax = er / length;
                ay = -ex / length;
            }
            double low = Double.POSITIVE_INFINITY;
            double high = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < n; k++) {
                double d = xs[k] * ax + rows[k] * ay;
                low = Math.min(low, d);
                high = Math.max(high, d);
            }
            double centre = cx * ax + cr * ay;
            double reach = hw * Math.abs(ax) + hh * Math.abs(ay);
            double overlap = Math.min(high, centre + reach) - Math.max(low, centre - reach);
            if (!(overlap > 0)) {
                return false;
            }
            // The way out: to the near side of the island's projection.
            double pushHigh = high - (centre - reach);
            double pushLow = (centre + reach) - low;
            double depth = Math.min(pushHigh, pushLow);
            if (depth < bestDepth) {
                bestDepth = depth;
                double side;
                if (pushHigh != pushLow) {
                    side = pushHigh < pushLow ? 1 : -1;
                } else {
                    side = (cx - centreX) * ax + (cr - centreR) * ay >= 0 ? 1 : -1;
                }
                bestX = ax * side;
                bestY = ay * side;
            }
        }
        if (bestDepth > out[2]) {
            out[0] = bestX;
            out[1] = bestY;
            out[2] = bestDepth;
        }
        return true;
    }
}
