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
    static final TravelFrame SCREEN = new TravelFrame(false);
    /** The play field mirrored top to bottom. */
    static final TravelFrame FLIPPED = new TravelFrame(true);

    private final boolean flipped;

    private TravelFrame(boolean flipped) {
        this.flipped = flipped;
    }

    /** {@link #FLIPPED} for {@code flipped}, {@link #SCREEN} otherwise. */
    static TravelFrame of(boolean flipped) {
        return flipped ? FLIPPED : SCREEN;
    }

    /** Whether it is the mirrored frame. */
    boolean flipped() {
        return flipped;
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
