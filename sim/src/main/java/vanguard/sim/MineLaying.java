package vanguard.sim;

/**
 * M9 part C: one mine layer's lay path and its line (design/enemies/space/minelayer; see {@link EnemySpec.MineLayer}).
 * It rises up its side lane from below the bottom edge to its lay row, then strafes across toward the far edge,
 * laying a line mine at each of its line's x as it passes it (the mines between its lane and its own edge at once as
 * it turns); past the far edge it is gone. Its lane, lay row and gap come from its planned path ({@link
 * Formations}: start in the lane, end at the gap point on the lay row). Each {@link Enemy} owns one, used only while
 * its stat block has a mine layer (hashed only then); the unit takes its place from {@link #x()} and {@link #y()}.
 * Allocation-free: the line's x are kept in a fixed array.
 */
final class MineLaying {
    /** Rising up its lane. */
    static final int RISE = 0;
    /** Strafing across its lay row, laying. */
    static final int LAY = 1;

    private final double[] xs = new double[EnemySpec.MineLayer.MOST_MINES];

    private EnemySpec.MineLayer spec;
    private int phase = RISE;
    /** Its strafe's direction: 1 to the right, -1 to the left. */
    private int dir = 1;

    private double x;
    private double y;
    /** Its lay row's height (y up). */
    private double rowY;
    /** The line's gap boundary (between cluster b and b + 1). */
    private int gapBoundary;
    /** The line's mines, left to right, and how many of them it has laid (in its strafe's order). */
    private int count;

    private int laid;

    /** Back to a unit without a mine layer. */
    void clear() {
        spec = null;
        phase = RISE;
        dir = 1;
        x = y = rowY = 0;
        gapBoundary = 0;
        count = 0;
        laid = 0;
    }

    /**
     * Starts the lay path of a unit appearing at (startX, startY), its planned path ending at (endX, endY): its lane
     * is the start's x, its lay row the end's height, its gap the boundary nearest the end's x; it strafes away from
     * its lane's side.
     */
    void start(EnemySpec.MineLayer layer, double startX, double startY, double endX, double endY) {
        clear();
        spec = layer;
        x = startX;
        y = startY;
        rowY = endY;
        dir = startX <= PlayField.WIDTH / 2.0 ? 1 : -1;
        gapBoundary = layer.gapBoundary(endX);
        count = layer.positions(gapBoundary, xs);
    }

    /** One step of its flight. */
    void step() {
        double seconds = SimStep.SECONDS;
        if (phase == RISE) {
            y += spec.riseSpeed() * seconds;
            if (y >= rowY) {
                // Its turn: the rest of the step goes across.
                double over = (y - rowY) / spec.riseSpeed() * spec.strafeSpeed();
                y = rowY;
                x += dir * over;
                phase = LAY;
            }
            return;
        }
        x += dir * spec.strafeSpeed() * seconds;
    }

    /** M7 part A: its flight drifts with a stunned unit by (dx, dy). */
    void drift(double dx, double dy) {
        x += dx;
        y += dy;
        rowY += dy;
    }

    /**
     * The x of the next mine of its line it lays now, NaN for none: while it strafes, the next mine in its strafe's
     * order that it has reached or passed. {@link #laid()} moves on to the one after it.
     */
    double due() {
        if (phase != LAY || laid >= count) {
            return Double.NaN;
        }
        double next = xs[dir > 0 ? laid : count - 1 - laid];
        return dir > 0 ? (next <= x ? next : Double.NaN) : (next >= x ? next : Double.NaN);
    }

    /** The mine {@link #due()} gave is laid (or refused at the limit: it is not laid later). */
    void laid() {
        laid++;
    }

    /** Hashed only for a unit with a mine layer. */
    void addTo(StateHash hash) {
        hash.add(phase)
                .add(dir)
                .add(x)
                .add(y)
                .add(rowY)
                .add(gapBoundary)
                .add(count)
                .add(laid);
    }

    /** {@link #RISE} or {@link #LAY}. */
    int phase() {
        return phase;
    }

    /** Its strafe's direction: 1 to the right, -1 to the left. */
    int side() {
        return dir;
    }

    /** The mines of its line it has laid so far (refused ones too), and the line's mines. */
    int laidCount() {
        return laid;
    }

    int lineMines() {
        return count;
    }

    /** The x of mine {@code k} of its line, left to right. */
    double mineX(int k) {
        return xs[k];
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }
}
