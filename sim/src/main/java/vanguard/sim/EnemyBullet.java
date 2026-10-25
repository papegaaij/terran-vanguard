package vanguard.sim;

/** An enemy bullet on the player's plane, flying straight until it hits the ship or leaves the screen. */
public final class EnemyBullet implements Hashed {
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private double vx;
    private double vy;
    private double damage;
    /** The convoy unit it was aimed at by the target-the-objective hook; -1 for the ship. */
    private int target;
    /** M8 part A: the level's medium (its bullet speed at the spawn); null in the air. */
    private Medium medium;
    /** M8 part G: whether a heat plume bent it already (once per bullet; hashed only once set). */
    private boolean bent;

    /** M8 part A: the level's medium, whose bullet speed scales every bullet as it is fired. */
    void medium(Medium water) {
        medium = water;
    }

    void fire(double startX, double startY, double velocityX, double velocityY, double bulletDamage) {
        fire(startX, startY, velocityX, velocityY, bulletDamage, -1);
    }

    /** @param aimedAt the convoy unit it is aimed at; -1 for the ship */
    void fire(double startX, double startY, double velocityX, double velocityY, double bulletDamage, int aimedAt) {
        target = aimedAt;
        bent = false;
        x = prevX = startX;
        y = prevY = startY;
        vx = velocityX;
        vy = velocityY;
        damage = bulletDamage;
        if (medium != null && medium.wet()) {
            // M8 part A (design/world/europa, Under water rules; D3): its speed × the water's, fixed at its spawn.
            double factor = medium.bulletSpeed();
            vx *= factor;
            vy *= factor;
        }
    }

    void move() {
        prevX = x;
        prevY = y;
        x += vx * SimStep.SECONDS;
        y += vy * SimStep.SECONDS;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(x).add(y).add(vx).add(vy).add(damage);
        if (target >= 0) {
            hash.add(target);
        }
        if (bent) {
            // M8 part G: only a bullet a plume bent, so the other levels hash as before.
            hash.add(1);
        }
    }

    /** M8 part G (design/campaign Level 27; {@link HeatPlumes}): whether a heat plume bent it already. */
    boolean bent() {
        return bent;
    }

    /** M8 part G: a heat plume turns it by {@code radians} (clockwise on the screen for a positive angle), once. */
    void bend(double radians) {
        bent = true;
        double cos = Trig.cos(radians);
        double sin = Trig.sin(radians);
        double turnedX = vx * cos + vy * sin;
        vy = -vx * sin + vy * cos;
        vx = turnedX;
    }

    /** Whether the target-the-objective hook aimed it at a convoy unit rather than the ship. */
    boolean objectiveAimed() {
        return target >= 0;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    /** Its velocity, px/s (y up). */
    double vx() {
        return vx;
    }

    double vy() {
        return vy;
    }

    /** The damage of a hit, by its bullet class (design/enemies, balancing basis). */
    public double damage() {
        return damage;
    }

    /** The direction of flight in radians, 0 = straight up the screen, clockwise. */
    public double heading() {
        return StrictMath.atan2(vx, vy);
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
