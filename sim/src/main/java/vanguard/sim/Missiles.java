package vanguard.sim;

/**
 * M7 part F, the enemies' homing missiles (design/enemies/bosses/revenant-walker, Attacks; M8 part G: the Depth Hunter's
 * torpedoes, launched along its facing by {@link #launchAlong}): a pool of {@link
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
    /** M8 part A: a {@link SimEvents.Type#MISSILE_DESTROYED} value: it burst on a decoy flare ({@link Flares}). */
    public static final int DECOYED = 3;

    private static final int CAPACITY = 16;
    /** One this far outside the play field is gone without a trace, px. */
    private static final double FIELD_MARGIN = 60;
    /** A launch's heading snaps to the pod's 16 bearings (design: the pods snap to the ship's bearing). */
    private static final double BEARING_STEP = StrictMath.PI / 8;

    private final SimEvents events;
    private final Pool<Missile> pool = new Pool<>(CAPACITY, Missile::new, Missile[]::new);
    /** The missiles launched in this attempt: the next one's serial. */
    private int launched;
    /** M8 part A: the level's medium (its bullet speed at the launch); null in the air. */
    private Medium medium;

    /** M8 part A: the fitted Decoy Flares; null without them. */
    private Flares decoys;

    /** M8 part A: the fitted Decoy Flares, whose burning flares the missiles go for instead of the ship. */
    void decoys(Flares flares) {
        decoys = flares;
    }

    /** M8 part A: the level's medium, whose bullet speed scales every missile as it is launched. */
    void medium(Medium water) {
        medium = water;
    }

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
     * headings (M8 part A: to the nearest burning decoy flare while one burns); left out when {@code gun}'s most are
     * in flight already. Returns whether it was launched.
     */
    boolean launch(double x, double y, EnemyGun gun, double shipX, double shipY) {
        int flare = decoys == null ? -1 : decoys.nearest(x, y);
        if (flare >= 0) {
            shipX = decoys.get(flare).x();
            shipY = decoys.get(flare).y();
        }
        EnemyGun.HomingSpec spec = gun.homing().orElseThrow();
        if (pool.size() >= spec.mostAlive()) {
            return false;
        }
        Missile missile = pool.obtain();
        if (missile == null) {
            return false;
        }
        double bearing = StrictMath.atan2(-(shipX - x), -(shipY - y));
        missile.launch(
                gun,
                x,
                y,
                Math.rint(bearing / BEARING_STEP) * BEARING_STEP,
                launched++,
                medium != null && medium.wet() ? medium.bulletSpeed() : 1);
        events.add(SimEvents.Type.MISSILE_LAUNCHED, x, y, missile.serial());
        return true;
    }

    /**
     * M8 part G (design/enemies/naval/depth-hunter, Attack; m8g-gaps.md §1.1): one missile (a torpedo) of {@code gun}
     * from (x, y) along {@code heading} (radians clockwise from straight down: the unit's facing), not snapped to the
     * ship's bearing; it steers toward the ship (or a burning decoy flare) from its next step on. Left out when {@code
     * gun}'s most are in flight already. Returns whether it was launched.
     */
    boolean launchAlong(double x, double y, EnemyGun gun, double heading) {
        EnemyGun.HomingSpec spec = gun.homing().orElseThrow();
        if (pool.size() >= spec.mostAlive()) {
            return false;
        }
        Missile missile = pool.obtain();
        if (missile == null) {
            return false;
        }
        missile.launch(gun, x, y, heading, launched++, medium != null && medium.wet() ? medium.bulletSpeed() : 1);
        events.add(SimEvents.Type.MISSILE_LAUNCHED, x, y, missile.serial());
        return true;
    }

    /**
     * The missiles fly toward (shipX, shipY); one whose life ran out fizzles, one far off the field is gone. M8 part
     * A: while a decoy flare burns each flies toward the burning flare nearest to it instead, and one that reaches it
     * bursts on it ({@link #DECOYED}; the flare burns on).
     */
    void fly(double shipX, double shipY) {
        boolean decoyed = decoys != null && decoys.burning();
        for (int i = pool.size() - 1; i >= 0; i--) {
            Missile missile = pool.get(i);
            int flare = decoyed ? decoys.nearest(missile.x(), missile.y()) : -1;
            double targetX = flare < 0 ? shipX : decoys.get(flare).x();
            double targetY = flare < 0 ? shipY : decoys.get(flare).y();
            if (!missile.fly(targetX, targetY)) {
                destroy(i, FIZZLED);
            } else if (flare >= 0 && Flares.BOX.overlaps(targetX, targetY, Missile.BOX, missile.x(), missile.y())) {
                destroy(i, DECOYED);
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
