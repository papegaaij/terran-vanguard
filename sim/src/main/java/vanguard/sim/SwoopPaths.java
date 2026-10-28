package vanguard.sim;

/**
 * M9 part B (design/enemies/air/talon, Movement; {@link EnemySpec.SwoopPath}): the curves a swoop path's wave flies,
 * sampled cubic Bézier curves in play-field coordinates (y up), planned once with the wave. A front curve enters at the
 * top edge heading down, bottoms out exactly at its depth halfway across and leaves heading up through the top edge
 * (or a side edge beyond it); a side curve enters at the left edge {@link #SIDE_ENTRY} px below the top edge, bottoms
 * out at its depth and leaves heading up through the top edge (the right wing flies its mirror image).
 */
final class SwoopPaths {
    /** The samples along a curve: 32 straight pieces. */
    private static final int PIECES = 32;
    /** How far below the top edge a side curve enters, px. */
    static final double SIDE_ENTRY = 60;

    private SwoopPaths() {}

    /**
     * A front curve from the top edge at {@code entryX}, bottoming out {@code lowest} px below the top edge {@code
     * across} / 2 px toward {@code side} (1 right, -1 left), out through the top edge {@code across} px from its entry.
     */
    static FlightPath front(double entryX, int side, double across, double lowest) {
        double top = PlayField.HEIGHT + Formations.OUTSIDE;
        double low = PlayField.HEIGHT - lowest;
        double exitX = entryX + side * across;
        // A symmetric cubic bottoms out at its middle: (2 top + 6 control) / 8 = low.
        double control = (8 * low - 2 * top) / 6;
        return cubic(entryX, top, entryX, control, exitX, control, exitX, top);
    }

    /**
     * A side curve from the left edge, {@link #SIDE_ENTRY} px below the top edge, bottoming out {@code lowest} px below
     * the top edge and out through the top edge {@code across} px in from the left edge.
     */
    static FlightPath fromLeft(double across, double lowest) {
        double entryY = PlayField.HEIGHT - SIDE_ENTRY;
        double top = PlayField.HEIGHT + Formations.OUTSIDE;
        double low = PlayField.HEIGHT - lowest;
        double entryX = -Formations.OUTSIDE;
        double midX = across / 2;
        // The curve's lowest point rises with its controls' height: halve toward the height that bottoms out at low.
        double below = low - 8 * (top - low);
        double above = low;
        for (int i = 0; i < 60; i++) {
            double control = (below + above) / 2;
            if (lowest(entryY, control, top) < low) {
                below = control;
            } else {
                above = control;
            }
        }
        double control = (below + above) / 2;
        return cubic(entryX, entryY, midX, control, across, control, across, top);
    }

    /** The lowest sampled height of a cubic from {@code start} through two controls at {@code control} to {@code end}. */
    private static double lowest(double start, double control, double end) {
        double least = Math.min(start, end);
        for (int i = 1; i < PIECES; i++) {
            least = Math.min(least, bezier(start, control, control, end, (double) i / PIECES));
        }
        return least;
    }

    private static FlightPath cubic(
            double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        double[] xs = new double[PIECES + 1];
        double[] ys = new double[PIECES + 1];
        for (int i = 0; i <= PIECES; i++) {
            double t = (double) i / PIECES;
            xs[i] = bezier(ax, bx, cx, dx, t);
            ys[i] = bezier(ay, by, cy, dy, t);
        }
        return FlightPath.sampled(xs, ys);
    }

    private static double bezier(double a, double b, double c, double d, double t) {
        double u = 1 - t;
        return u * u * u * a + 3 * u * u * t * b + 3 * u * t * t * c + t * t * t * d;
    }
}
