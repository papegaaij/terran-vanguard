package vanguard.sim;

/**
 * M7 part F, the enemies' homing missiles (design/enemies/bosses/revenant-walker, Attacks): a pool of {@link
 * Missile}s launched by a boss's homing gun ({@link EnemyGun#homing()}), at most its {@link
 * EnemyGun.HomingSpec#mostAlive()} at once, each steering toward the ship until it hits a hull, is shot down or
 * fizzles. Owned by the {@link EnemyFire}; nothing here allocates once made.
 */
public final class Missiles {
    /** A {@link SimEvents.Type#MISSILE_DESTROYED} value: a shot or the beam brought it down. */
    public static final int SHOT_DOWN = 0;
    /** A {@link SimEvents.Type#MISSILE_DESTROYED} value: it burst on the ship's or Rook's hull. */
    public static final int STRUCK = 1;
    /** A {@link SimEvents.Type#MISSILE_DESTROYED} value: its life ran out. */
    public static final int FIZZLED = 2;

    private static final int CAPACITY = 16;
    /** One this far outside the play field is gone without a trace, px. */
    private static final double FIELD_MARGIN = 60;
    /** A launch's heading snaps to the pod's 16 bearings (design: the pods snap to the ship's bearing). */
    private static final double BEARING_STEP = StrictMath.PI / 8;

    private final SimEvents events;
    private final Pool<Missile> pool = new Pool<>(CAPACITY, Missile::new, Missile[]::new);
    /** The missiles launched in this attempt: the next one's serial. */
    private int launched;

    Missiles(SimEvents events) {
        this.events = events;
    }

    /** Back to the level start: none in flight. */
    void reset() {
        Pools.clear(pool);
        launched = 0;
    }

    /**
     * One missile of {@code gun} from (x, y), heading along the bearing to (shipX, shipY) snapped to the 16 pod
     * headings; left out when {@code gun}'s most are in flight already. Returns whether it was launched.
     */
    boolean launch(double x, double y, EnemyGun gun, double shipX, double shipY) {
        EnemyGun.HomingSpec spec = gun.homing().orElseThrow();
        if (pool.size() >= spec.mostAlive()) {
            return false;
        }
        Missile missile = pool.obtain();
        if (missile == null) {
            return false;
        }
        double bearing = StrictMath.atan2(-(shipX - x), -(shipY - y));
        missile.launch(gun, x, y, Math.rint(bearing / BEARING_STEP) * BEARING_STEP, launched++);
        events.add(SimEvents.Type.MISSILE_LAUNCHED, x, y, missile.serial());
        return true;
    }

    /** The missiles fly toward (shipX, shipY); one whose life ran out fizzles, one far off the field is gone. */
    void fly(double shipX, double shipY) {
        for (int i = pool.size() - 1; i >= 0; i--) {
            Missile missile = pool.get(i);
            if (!missile.fly(shipX, shipY)) {
                destroy(i, FIZZLED);
            } else if (!PlayField.overlaps(missile.x(), missile.y(), FAR)) {
                pool.free(i);
            }
        }
    }

    private static final Hitbox FAR = new Hitbox(2 * FIELD_MARGIN, 2 * FIELD_MARGIN);

    /** Missile {@code index} is gone ({@link #SHOT_DOWN}, {@link #STRUCK} or {@link #FIZZLED}): its event. */
    void destroy(int index, int cause) {
        Missile missile = pool.get(index);
        events.add(SimEvents.Type.MISSILE_DESTROYED, missile.x(), missile.y(), cause);
        pool.free(index);
    }

    Pool<Missile> pool() {
        return pool;
    }

    /** How many fly now. */
    public int size() {
        return pool.size();
    }

    /** Missile {@code index} in flight (0 ≤ index &lt; {@link #size()}). */
    public Missile get(int index) {
        return pool.get(index);
    }

    void addTo(StateHash hash) {
        hash.add(launched);
        Pools.addAll(hash, pool);
    }
}
