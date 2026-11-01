package vanguard.sim;

/**
 * M7 part G (design/campaign Level 20, Reverse scroll; default pending the user's review): the frame a unit lives in.
 * A unit spawned while the scroll runs reversed ({@link Sortie#travelAt}{@code < 0}) is <em>flipped</em>: it lives in
 * the play field mirrored top to bottom, so its behaviour code runs unchanged and comes out by travel (its "ahead" is
 * the screen's bottom, its "behind" the top). The conversions are their own inverse: y' = {@link PlayField#HEIGHT} −
 * y, vy' = −vy, facing' = π − facing (radians clockwise from straight down, kept in [−π, π]), the ground's scroll' =
 * −scroll. {@link #SCREEN} changes nothing, so an unflipped unit steps bit for bit as before.
 */
final class TravelFrame {
    /** The screen's own frame: every conversion returns its argument. */
    static final TravelFrame SCREEN = new TravelFrame(false, WarningEdge.BOTTOM);
    /** The play field mirrored top to bottom (a unit of a wave in a reversed scroll: its rear is the screen's top). */
    static final TravelFrame FLIPPED = new TravelFrame(true, WarningEdge.TOP);
    /**
     * M10 part F (design/campaign Level 40, the Talon V-wings from the rear; step F1c): the play field mirrored top to
     * bottom for a front formation rising from the bottom edge in a forward scroll ({@link WaveSchedule}): its units fly
     * their front formation in the mirrored frame, so they enter at the bottom edge, but their wave stays a rear wave
     * of the screen's bottom edge (by travel they come from behind).
     */
    static final TravelFrame RISING = new TravelFrame(true, WarningEdge.BOTTOM);

    private final boolean flipped;
    /** The screen edge a rear wave of a unit in this frame entered from (a {@link WarningEdge} bit). */
    private final int rearEdge;

    private TravelFrame(boolean flipped, WarningEdge rearEdge) {
        this.flipped = flipped;
        this.rearEdge = rearEdge.bit();
    }

    /** {@link #FLIPPED} for {@code flipped}, {@link #SCREEN} otherwise. */
    static TravelFrame of(boolean flipped) {
        return flipped ? FLIPPED : SCREEN;
    }

    /** Whether it is the mirrored frame. */
    boolean flipped() {
        return flipped;
    }

    /**
     * The screen edge, as a {@link WarningEdge} bit, a unit in this frame whose wave entered from the rear came in at:
     * the bottom one, the top one for a unit flipped by a reversed scroll ({@link #FLIPPED}).
     */
    int rearEdge() {
        return rearEdge;
    }

    /** A height (px up from the bottom edge) in the other frame. */
    double y(double y) {
        return flipped ? PlayField.HEIGHT - y : y;
    }

    /** A vertical velocity or offset (px up) in the other frame. */
    double vy(double vy) {
        return flipped ? -vy : vy;
    }

    /** A heading (radians clockwise from straight down) in the other frame. */
    double facing(double facing) {
        return flipped ? Math.IEEEremainder(StrictMath.PI - facing, 2 * StrictMath.PI) : facing;
    }

    /** An angle counter-clockwise from the +x axis (y up), as a bullet's or a fan's, in the other frame. */
    double angle(double angle) {
        return flipped ? -angle : angle;
    }

    /** The ground's scroll (px down the screen) in the other frame. */
    double scroll(double scroll) {
        return flipped ? -scroll : scroll;
    }
}
