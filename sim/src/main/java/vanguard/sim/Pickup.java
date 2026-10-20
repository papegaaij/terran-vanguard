package vanguard.sim;

/**
 * A pickup drifting down the screen until the ship collects it or its time runs out (design/player);
 * in a fitted Pickup magnet's reach it flies at the ship instead (design/player/systems). M7 part E: a unit with
 * a pull draws it in and holds it (design/enemies/ground/dust-devil): a held pickup circles the unit's centre,
 * its time standing still, and cannot be collected until the unit dies and releases it.
 */
public final class Pickup implements Hashed {
    private PickupType type;
    private int credits;
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private int ticksLeft;
    /** M7 part E: the serial of the unit holding it; -1 when it is loose (hashed only while held). */
    private int heldBy = -1;
    /** M7 part E: its angle round its holder's centre while held, radians. */
    private double holdAngle;

    /** M7 part E: a held pickup circles its holder's centre this far out, px (default pending round 41). */
    static final double HOLD_RADIUS = 26;
    /** M7 part E: a held pickup circles its holder this fast, radians per second (one turn in 1.25 s). */
    static final double HOLD_TURN = 2 * StrictMath.PI / 1.25;

    void drop(PickupType pickupType, int pickupCredits, double startX, double startY, int lifetimeTicks) {
        type = pickupType;
        credits = pickupCredits;
        x = prevX = startX;
        y = prevY = startY;
        ticksLeft = lifetimeTicks;
        heldBy = -1;
        holdAngle = 0;
    }

    /** M7 part E: the unit with {@code serial} at (cx, cy) holds it from now, where it is round that centre. */
    void hold(int serial, double cx, double cy) {
        heldBy = serial;
        double dx = x - cx;
        double dy = y - cy;
        holdAngle = dx == 0 && dy == 0 ? 0 : StrictMath.atan2(dy, dx);
    }

    /** M7 part E: one step held: it circles its holder's centre (cx, cy); its time stands still. */
    void circle(double cx, double cy) {
        prevX = x;
        prevY = y;
        holdAngle = Math.IEEEremainder(holdAngle + HOLD_TURN * SimStep.SECONDS, 2 * StrictMath.PI);
        x = cx + HOLD_RADIUS * Trig.cos(holdAngle);
        y = cy + HOLD_RADIUS * Trig.sin(holdAngle);
    }

    /** M7 part E: its holder died: it is loose again where it is, with {@code lifetimeTicks} to live. */
    void release(int lifetimeTicks) {
        heldBy = -1;
        holdAngle = 0;
        ticksLeft = lifetimeTicks;
    }

    /** M7 part E: the serial of the unit holding it (a Dust Devil); -1 when it is loose. */
    public int heldBy() {
        return heldBy;
    }

    /**
     * Drifts down by {@code distance}; returns false once its time is up or it has left the screen (M7 part G: by the
     * top edge, drifting up with a negative {@code distance} in a reversed scroll).
     */
    boolean drift(double distance) {
        prevX = x;
        prevY = y;
        y -= distance;
        return --ticksLeft > 0 && (distance < 0 ? y < PlayField.HEIGHT : y > 0);
    }

    /** M7 part C: slides {@code dx} px across, off rock (after its drift of the step). */
    void slide(double dx) {
        x += dx;
    }

    /** Whether it is within {@code radius} of the point ({@code px}, {@code py}). */
    boolean within(double px, double py, double radius) {
        double dx = px - x;
        double dy = py - y;
        return dx * dx + dy * dy <= radius * radius;
    }

    /**
     * Flies {@code distance} straight at the point ({@code px}, {@code py}), stopping on it, instead
     * of drifting; returns false once its time is up or it has left the screen.
     */
    boolean pull(double px, double py, double distance) {
        prevX = x;
        prevY = y;
        double dx = px - x;
        double dy = py - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length <= distance) {
            x = px;
            y = py;
        } else {
            x += dx * distance / length;
            y += dy * distance / length;
        }
        return --ticksLeft > 0 && y > 0;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(type.ordinal()).add(credits).add(x).add(y).add(ticksLeft);
        if (heldBy >= 0) {
            // M7 part E: only while a unit holds it, so the other levels hash as before.
            hash.add(heldBy).add(holdAngle);
        }
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    /** Credits before the credit factor; 0 for pickups that pay none. */
    int credits() {
        return credits;
    }

    public PickupType type() {
        return type;
    }

    /** Steps until it is gone, for the blinking in its last moments. */
    public int ticksLeft() {
        return ticksLeft;
    }

    /** The position of the current step; a pulled pickup moves sideways too, see {@link #renderX(double)}. */
    public double renderX() {
        return x;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }
}
