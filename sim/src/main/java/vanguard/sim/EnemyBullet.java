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
    /**
     * M10 part A (design/enemies, Reflect): a player shot a mirror bounced back (magenta): never bounced again, by a
     * mirror or the Reflex Shield; hashed only once set.
     */
    private boolean bounced;
    /** M10 part A (design/enemies/air/harrow): steps left in which it cannot hit the ship (a ram's ring); 0 for none. */
    private int grace;
    /**
     * M10 part A (design/enemies, The three-way war): the tag of the prey wave a hunter fired it at (it also hits that
     * wave's units at its damage); null for none.
     */
    private String prey;

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
        bounced = false;
        grace = 0;
        prey = null;
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
        if (grace > 0) {
            grace--;
        }
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
        if (bounced || grace > 0 || prey != null) {
            // M10 part A: only a bounced bullet, a ram ring's or a hunter's, so the other levels hash as before.
            hash.add(bounced ? 1 : 0).add(grace).add(prey == null ? 0 : prey.hashCode());
        }
    }

    /** M10 part A (design/enemies, Reflect): whether a mirror bounced it out of a player shot (drawn magenta). */
    public boolean bounced() {
        return bounced;
    }

    /** M10 part A: marks it a bounced player shot. */
    void markBounced() {
        bounced = true;
    }

    /** M10 part A (design/enemies/air/harrow): it cannot hit the ship for its first {@code ticks} steps. */
    void grace(int ticks) {
        grace = ticks;
    }

    /** M10 part A: whether it can hit the ship now (past a ram ring's grace). */
    boolean hitsShip() {
        return grace == 0;
    }

    /** M10 part A (The three-way war): it was fired at the prey wave tagged {@code tag}, whose units it hits too. */
    void prey(String tag) {
        prey = tag;
    }

    /** M10 part A: the tag of the prey wave it hits; null for none. */
    String preyTag() {
        return prey;
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
