package vanguard.sim;

/**
 * Round 69's B.1/B.2 (the user; design/enemies/ground/rail-bunker, The lead): a laser line's locked direction, led for
 * its moving origin. At the telegraph's start the origin is predicted where it will be when the line goes live (its
 * move over the telegraph: a bunker's scroll, a battery's turn on its ring), and the direction runs from that predicted
 * origin through the ship's position at the lock. The line then rides rigidly with its origin ({@link LineBeam#ride},
 * {@link BossLines}), so at its live moment it passes through the locked ship point; a still origin aims as before.
 * The lock still does not track the ship: a sidestep in the telegraph dodges. The one place both {@link LineBeam} and
 * {@link BossLines} lock their lines. Allocates nothing.
 */
final class LineAim {
    private double dirX;
    private double dirY;

    /** No line locked. */
    void clear() {
        dirX = 0;
        dirY = 0;
    }

    /**
     * Locks the direction from the origin where it will be when the line goes live, (leadX, leadY), through the ship at
     * (shipX, shipY). Should the predicted origin fall on the ship, it aims from the origin now, (fromX, fromY), which
     * the caller keeps off the ship (its hold distance).
     */
    void lock(double leadX, double leadY, double fromX, double fromY, double shipX, double shipY) {
        double dx = shipX - leadX;
        double dy = shipY - leadY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) {
            dx = shipX - fromX;
            dy = shipY - fromY;
            distance = Math.sqrt(dx * dx + dy * dy);
        }
        dirX = dx / distance;
        dirY = dy / distance;
    }

    /**
     * M10 round 80 fixes, R80a (the Ascendant's lance sweep): turns the locked direction by the angle whose cosine and
     * sine are given (counter-clockwise, y up), renormalised.
     */
    void rotate(double cos, double sin) {
        double x = dirX * cos - dirY * sin;
        double y = dirX * sin + dirY * cos;
        double length = Math.sqrt(x * x + y * y);
        if (length > 0) {
            dirX = x / length;
            dirY = y / length;
        }
    }

    /** The locked direction, a unit vector (y up the screen). */
    double dirX() {
        return dirX;
    }

    double dirY() {
        return dirY;
    }
}
