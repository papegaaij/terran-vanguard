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

    /** Launched at (x, y) by {@code launcher} along {@code angle} (radians clockwise from straight down). */
    void launch(EnemyGun launcher, double startX, double startY, double angle, int id) {
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
        double step = gun.bulletSpeed() * SimStep.SECONDS;
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
