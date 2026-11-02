package vanguard.sim;

/**
 * A walker's authored path (design/enemies/ground/scuttler): its points in play-field pixels (y up)
 * at the moment the walker enters, which then scroll with the ground. Shared by every attempt.
 * Past its last point a walker walks straight on, or, on a path that {@link #parks()} (step R79c, Level 41's gate
 * Warden pairs: {@code park: true}), stops there and stands, scrolling with the ground.
 */
public final class WalkPath {
    private final double[] xs;
    private final double[] ys;
    private final boolean parks;

    private WalkPath(double[] xs, double[] ys, boolean parks) {
        this.xs = xs;
        this.ys = ys;
        this.parks = parks;
    }

    /** The path through {@code x0, y0, x1, y1, ...}; at least one point. */
    public static WalkPath through(double... points) {
        if (points.length < 2 || points.length % 2 != 0) {
            throw new IllegalArgumentException("a walk path needs x, y pairs");
        }
        double[] xs = new double[points.length / 2];
        double[] ys = new double[points.length / 2];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = points[2 * i];
            ys[i] = points[2 * i + 1];
        }
        return new WalkPath(xs, ys, false);
    }

    /**
     * Step R79c: this path ending in a park: the walker stops at its last point and stands there, scrolling with the
     * ground (its tracks still, its turret and gun on), instead of walking straight on.
     */
    public WalkPath parked() {
        return new WalkPath(xs, ys, true);
    }

    /** Step R79c: whether the walker stops at its last point ({@link #parked()}). */
    public boolean parks() {
        return parks;
    }

    /** The path mirrored about the play field's vertical centre line. */
    public WalkPath mirrored() {
        double[] mirrored = new double[xs.length];
        for (int i = 0; i < xs.length; i++) {
            mirrored[i] = PlayField.WIDTH - xs[i];
        }
        return new WalkPath(mirrored, ys, parks);
    }

    public int points() {
        return xs.length;
    }

    public double x(int point) {
        return xs[point];
    }

    public double y(int point) {
        return ys[point];
    }
}
