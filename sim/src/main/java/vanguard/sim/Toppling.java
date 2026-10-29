package vanguard.sim;

/**
 * M9 part E, one walker's topple (design/enemies/ground/strider, The topple; {@link EnemySpec.Topple}): the steps since
 * it lost a part that topples it and the direction of its fall line (from its centre toward the lost part, fixed at the
 * loss). Each {@link Enemy} owns one, used (and hashed) only while its stat block has a topple. Allocates nothing.
 */
final class Toppling {
    /** Steps since the loss; -1 while it stands. */
    private int ticks = -1;
    /** The fall line's direction, a unit vector (y up). */
    private double dirX;

    private double dirY;
    /** Whether it started this step (its first count comes the next step); false again by the step's end. */
    private boolean fresh;

    void clear() {
        ticks = -1;
        dirX = 0;
        dirY = 0;
        fresh = false;
    }

    void addTo(StateHash hash) {
        hash.add(ticks).add(dirX).add(dirY);
    }

    /**
     * It lost a part that topples it, whose centre lies (dx, dy) from its own: it staggers toward it; returns whether
     * this started its topple (false once it topples already).
     */
    boolean start(double dx, double dy) {
        if (ticks >= 0) {
            return false;
        }
        double length = Math.hypot(dx, dy);
        ticks = 0;
        fresh = true;
        dirX = length > 0 ? dx / length : 0;
        dirY = length > 0 ? dy / length : -1;
        return true;
    }

    /** Whether it topples (staggers or falls). */
    boolean active() {
        return ticks >= 0;
    }

    /** Steps since the loss; -1 while it stands. */
    int ticks() {
        return ticks;
    }

    /** A step of the topple (none in the step of the loss); returns the steps since the loss now. */
    int step() {
        if (fresh) {
            fresh = false;
            return ticks;
        }
        return ++ticks;
    }

    double dirX() {
        return dirX;
    }

    double dirY() {
        return dirY;
    }

    /**
     * Whether a box of half size hw × hh round (bx, by) overlaps the fall line: the strip {@code width} wide and {@code
     * length} long from (x, y) along (dirX, dirY) (by the separating axes: the screen's two and the strip's two).
     */
    static boolean underLine(
            double x,
            double y,
            double dirX,
            double dirY,
            double width,
            double length,
            double bx,
            double by,
            double hw,
            double hh) {
        double cx = x + dirX * length / 2;
        double cy = y + dirY * length / 2;
        double ax = Math.abs(dirX);
        double ay = Math.abs(dirY);
        double dx = bx - cx;
        double dy = by - cy;
        if (Math.abs(dx) > hw + ax * length / 2 + ay * width / 2) {
            return false;
        }
        if (Math.abs(dy) > hh + ay * length / 2 + ax * width / 2) {
            return false;
        }
        if (Math.abs(dx * dirX + dy * dirY) > length / 2 + hw * ax + hh * ay) {
            return false;
        }
        return Math.abs(-dx * dirY + dy * dirX) <= width / 2 + hw * ay + hh * ax;
    }
}
