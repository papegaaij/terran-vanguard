package vanguard.sim;

/**
 * M7 part B: one unit's swoop (design/enemies/air/dust-kite): once it flies above the ship's centre by its
 * trigger distance or less it banks along a quadratic curve to where the ship's centre was, then flies on
 * straight along the curve's end. Each {@link Enemy} owns one, used only while its stat block has a swoop
 * (its fields are hashed only then); the unit takes its place from {@link #x()} and {@link #y()}.
 */
final class Swooping {
    private static final int SWOOP_STEPS = 8;

    /** Its side, -1 bending left and 1 right, from its place in its wave. */
    private int swoopSide = -1;
    /** Whether it swooped already (once per unit). */
    private boolean swooped;
    /** How far along its swoop's curve it is, 0 to 1, then on past its end along the end's direction. */
    private double swoopAlong;
    /** The swoop's curve: its start, its control point and its end (the ship's centre at the start). */
    private double swoopX0;

    private double swoopY0;
    private double swoopX1;
    private double swoopY1;
    private double swoopX2;
    private double swoopY2;
    /** The curve's length, px (a polyline of {@link #SWOOP_STEPS} pieces). */
    private double swoopLength;
    /** Whether its swoop started in this step (not hashed: set and read within the step). */
    private boolean swoopedNow;
    /** Its place after the last {@link #step} (not hashed: the unit's own position is). */
    private double x;

    private double y;

    /** Back to a unit that never swooped. */
    void clear() {
        swoopSide = -1;
        swooped = false;
        swoopAlong = 0;
        swoopX0 = swoopY0 = swoopX1 = swoopY1 = swoopX2 = swoopY2 = 0;
        swoopLength = 0;
        swoopedNow = false;
    }

    /** Hashed only for a unit with a swoop. */
    void addTo(StateHash hash) {
        hash.add(swoopSide)
                .add(swooped ? 1 : 0)
                .add(swoopAlong)
                .add(swoopX0)
                .add(swoopY0)
                .add(swoopX1)
                .add(swoopY1)
                .add(swoopX2)
                .add(swoopY2)
                .add(swoopLength);
    }

    /** A step starts: no swoop started in it yet. */
    void scratch() {
        swoopedNow = false;
    }

    /**
     * Once a unit at (unitX, unitY) with {@code box} flies above the ship's centre by {@code swoop}'s trigger
     * distance or less (on the play field, once per unit) it banks into its swoop; returns whether it did now.
     */
    boolean due(EnemySpec.Swoop swoop, double unitX, double unitY, Hitbox box, double shipX, double shipY) {
        if (swooped
                || !(unitY > shipY)
                || unitY - shipY > swoop.triggerAbove()
                || !PlayField.overlaps(unitX, unitY, box)) {
            return false;
        }
        swooped = true;
        swoopedNow = true;
        swoopAlong = 0;
        swoopX0 = unitX;
        swoopY0 = unitY;
        swoopX2 = shipX;
        swoopY2 = shipY;
        swoopX1 = unitX + swoopSide * swoop.bend();
        swoopY1 = (unitY + shipY) / 2;
        double length = 0;
        double fromX = unitX;
        double fromY = unitY;
        for (int k = 1; k <= SWOOP_STEPS; k++) {
            double t = (double) k / SWOOP_STEPS;
            double toX = curve(swoopX0, swoopX1, swoopX2, t);
            double toY = curve(swoopY0, swoopY1, swoopY2, t);
            length += Math.sqrt((toX - fromX) * (toX - fromX) + (toY - fromY) * (toY - fromY));
            fromX = toX;
            fromY = toY;
        }
        swoopLength = Math.max(1, length);
        return true;
    }

    /** One step of a swoop at {@code swoop}'s speed: along its curve, then straight on along the curve's end. */
    void step(EnemySpec.Swoop swoop) {
        swoopAlong += swoop.speed() * SimStep.SECONDS / swoopLength;
        if (swoopAlong <= 1) {
            x = curve(swoopX0, swoopX1, swoopX2, swoopAlong);
            y = curve(swoopY0, swoopY1, swoopY2, swoopAlong);
            return;
        }
        double dx = swoopX2 - swoopX1;
        double dy = swoopY2 - swoopY1;
        double norm = Math.sqrt(dx * dx + dy * dy);
        double past = (swoopAlong - 1) * swoopLength;
        x = swoopX2 + (norm > 0 ? dx / norm : 0) * past;
        y = swoopY2 + (norm > 0 ? dy / norm : -1) * past;
    }

    /** A quadratic Bézier coordinate at {@code t}. */
    private static double curve(double a, double b, double c, double t) {
        double u = 1 - t;
        return u * u * a + 2 * u * t * b + t * t * c;
    }

    /** M7 part A: its curve drifts with a stunned unit by (dx, dy). */
    void drift(double dx, double dy) {
        swoopX0 += dx;
        swoopX1 += dx;
        swoopX2 += dx;
        swoopY0 += dy;
        swoopY1 += dy;
        swoopY2 += dy;
    }

    /** Its place in its wave (from 0): a swoop bends left for an even place, right for an odd one. */
    void wavePlace(int place) {
        swoopSide = place % 2 == 0 ? -1 : 1;
    }

    /** Whether its swoop started in this step. */
    boolean swoopedNow() {
        return swoopedNow;
    }

    /** The side its swoop bends to, -1 left and 1 right. */
    int side() {
        return swoopSide;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }
}
