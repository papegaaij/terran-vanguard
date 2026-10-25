package vanguard.sim;

/**
 * M7 part F, an enemy's homing missile (design/enemies/bosses/revenant-walker, Attacks; the Revenant Walker's
 * pods): launched along a heading, it flies at its gun's bullet speed and turns toward the ship at up to its
 * {@link EnemyGun.HomingSpec#turnRadiansPerSecond()}; after its life it fizzles. On the player's layer: shots and
 * the beam hit it (its HP), and it bursts on the ship's or Rook's hull. Pooled ({@link Missiles}).
 */
public final class Missile implements Hashed {
    /** A missile's hit box, px. */
    public static final Hitbox BOX = new Hitbox(8, 8);

    private EnemyGun gun;
    private EnemyGun.HomingSpec spec;
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    /** Radians clockwise from straight down the screen, as {@link Enemy#facing()}. */
    private double heading;

    private double hp;
    private int age;
    private int lifeTicks;
    private int serial;
    /** M8 part A: its speed factor, fixed at its launch (the water's bullet speed); 1 in the air. */
    private double speedFactor = 1;
    /** M8 part G: whether a heat plume bent it already (once per missile; hashed only once set). */
    private boolean bent;

    /** Launched at (x, y) by {@code launcher} along {@code angle} (radians clockwise from straight down). */
    void launch(EnemyGun launcher, double startX, double startY, double angle, int id) {
        launch(launcher, startX, startY, angle, id, 1);
    }

    /** As {@link #launch(EnemyGun, double, double, double, int)}; M8 part A: flying at {@code speed} × its gun's. */
    void launch(EnemyGun launcher, double startX, double startY, double angle, int id, double speed) {
        speedFactor = speed;
        bent = false;
        gun = launcher;
        spec = launcher.homing().orElseThrow();
        x = prevX = startX;
        y = prevY = startY;
        heading = angle;
        hp = spec.hp();
        age = 0;
        lifeTicks = SimStep.ticks(spec.lifeSeconds());
        serial = id;
    }

    /**
     * One step: it turns toward (targetX, targetY) by at most its turn rate and flies on; returns false once its
     * life is over (it fizzles).
     */
    boolean fly(double targetX, double targetY) {
        prevX = x;
        prevY = y;
        double bearing = StrictMath.atan2(-(targetX - x), -(targetY - y));
        double off = Math.IEEEremainder(bearing - heading, 2 * StrictMath.PI);
        double most = spec.turnRadiansPerSecond() * SimStep.SECONDS;
        heading = Math.IEEEremainder(heading + Math.clamp(off, -most, most), 2 * StrictMath.PI);
        double step = gun.bulletSpeed() * speedFactor * SimStep.SECONDS;
        x -= Trig.sin(heading) * step;
        y -= Trig.cos(heading) * step;
        age++;
        return age < lifeTicks;
    }

    /** Takes damage; returns whether it was destroyed. */
    boolean damage(double amount) {
        hp -= amount;
        return hp <= 0;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(x).add(y).add(heading).add(hp).add(age).add(serial);
        if (speedFactor != 1) {
            // M8 part A: only one launched in the water, so the other levels hash as before.
            hash.add(speedFactor);
        }
        if (bent) {
            // M8 part G: only a torpedo a plume bent, so the other levels hash as before.
            hash.add(1);
        }
    }

    /** M8 part G (design/campaign Level 27; {@link HeatPlumes}): whether a heat plume bent it already. */
    boolean bent() {
        return bent;
    }

    /**
     * M8 part G: a heat plume turns it by {@code radians} (clockwise on the screen for a positive angle), once; it
     * steers on toward its target from its new heading.
     */
    void bend(double radians) {
        bent = true;
        heading = Math.IEEEremainder(heading + radians, 2 * StrictMath.PI);
    }

    /** M8 part G: its velocity across the screen, px/s (y up). */
    double vx() {
        return -Trig.sin(heading) * gun.bulletSpeed() * speedFactor;
    }

    double vy() {
        return -Trig.cos(heading) * gun.bulletSpeed() * speedFactor;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    /** Where it flies, radians clockwise from straight down the screen (its sprite's heading, its trail). */
    public double heading() {
        return heading;
    }

    /** Its number in the attempt, for its smoke trail (the game keeps a trail per missile). */
    public int serial() {
        return serial;
    }

    /** Its share of its life flown, 0–1. */
    public double age() {
        return Math.min(1, (double) age / Math.max(1, lifeTicks));
    }

    /** The damage its burst on a hull deals. */
    double damage() {
        return gun.damage();
    }

    public double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    public double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }
}
