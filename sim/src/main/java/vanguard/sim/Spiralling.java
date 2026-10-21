package vanguard.sim;

/**
 * One whirl seed's spiral out of its cluster's release point and its ricochets off the side edges afterwards
 * (design/enemies/air/whirl-seed; see {@link EnemySpec.Spiral}). Each {@link Enemy} owns one; it places its unit
 * through the unit's movement fields.
 */
final class Spiralling {
    private final Enemy unit;

    private double releaseX;
    private double releaseY;
    private double spiralAngle;
    private int spiralTicks;
    private int ricochetsLeft;

    Spiralling(Enemy unit) {
        this.unit = unit;
    }

    /** No spiral run yet, {@code ricochets} bounces off the side edges left. */
    void reset(int ricochets) {
        spiralTicks = 0;
        ricochetsLeft = ricochets;
    }

    /** It spirals out of the release point (atX, atY) at the outward {@code angle}. */
    void release(double atX, double atY, double angle) {
        releaseX = atX;
        releaseY = atY;
        spiralAngle = angle;
    }

    /** The release point drifts along with its unit (M7 part A: an EMP stun's drift). */
    void drift(double dx, double dy) {
        releaseX += dx;
        releaseY += dy;
    }

    /** The release point is pushed sideways with its unit (M5 part F: the level's wind). */
    void push(double dx) {
        releaseX += dx;
    }

    /**
     * One step of its spiral: around the release point, which drifts down, at its turn rate and with a growing
     * radius; at its end it flies on along its outward angle, turned downward, and at least as steeply down as the
     * release point drifted (so a seed that ends the spiral pointing sideways still leaves through the bottom).
     */
    void step() {
        Enemy u = unit;
        EnemySpec.Spiral spiral = u.spec.spiral().orElseThrow();
        double t = ++spiralTicks * SimStep.SECONDS;
        double angle = spiralAngle + 2 * StrictMath.PI * spiral.turnsPerSecond() * t;
        double radius = spiral.growth() * t;
        double cos = Trig.cos(angle);
        double sin = Trig.sin(angle);
        u.x = releaseX + radius * cos;
        u.y = releaseY - spiral.drift() * t + radius * sin;
        if (t >= spiral.seconds()) {
            u.phase = Enemy.Phase.LEAVE;
            double down = Math.max(Math.abs(sin), spiral.drift() / u.speed);
            double length = Math.sqrt(cos * cos + down * down);
            u.vx = cos / length * u.speed;
            u.vy = -down / length * u.speed;
        }
    }

    /** A spiralled-out unit bounces off the side edges, up to its ricochets. */
    void ricochet() {
        Enemy u = unit;
        if (ricochetsLeft == 0) {
            return;
        }
        double half = u.box.width() / 2;
        if ((u.x < half && u.vx < 0) || (u.x > PlayField.WIDTH - half && u.vx > 0)) {
            u.vx = -u.vx;
            ricochetsLeft--;
        }
    }

    /** Steps into its spiral (hashed with the unit's own state). */
    int ticks() {
        return spiralTicks;
    }

    /** Its bounces left (hashed with the unit's own state). */
    int ricochetsLeft() {
        return ricochetsLeft;
    }
}
