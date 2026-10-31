package vanguard.sim;

/**
 * M10 part A (design/player/systems, the IFF spoofer): one {@link EnemySpec#spoofable()} unit's late volleys. While
 * the ship carries the spoofer ({@link EnemyForce#spoof}) every aimed volley that comes due on the unit is held for the
 * spoofer's delay and then fired where the ship was when it came due (its stale aim); the unit's cycle runs on
 * unchanged (the next volley is due on time) and a burst's later shots keep their spacing after the delayed first. A
 * told attack (a {@code laser-line}, a ground sweep) starts its tell late instead ({@link LineBeam}, {@link
 * GroundSweepBeam}). Each held volley reports {@link SimEvents.Type#SPOOF_HESITATE} (the amber stutter). The held shots
 * wait while the unit is stunned and are lost with it. Fixed capacity, allocates nothing; hashed only while it holds a
 * shot.
 */
final class Spoofing {
    /** The held shot is fired at its stale point ({@link EnemyFire#fireAt}, a barrel along its stale aim). */
    static final int AIMED = 0;
    /** A homing missile launched at its stale point. */
    static final int LAUNCH_AT = 1;
    /** A homing missile launched along its stale heading (a SAM Nest's rack). */
    static final int LAUNCH_ALONG = 2;
    /** A walker's turret burst along its stale aim (the Warden Tank's). */
    static final int TURRET = 3;
    /** A walker's homing salvo along its stale bearing (the Strider's pod). */
    static final int SALVO = 4;

    private static final int CAPACITY = 8;

    /** The spoofer's delay for this unit this step, steps; 0 when it is not fooled. Set before its step. */
    int delay;

    private final int[] ticks = new int[CAPACITY];
    private final int[] kinds = new int[CAPACITY];
    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private final double[] aims = new double[CAPACITY];
    private int size;

    void clear() {
        delay = 0;
        size = 0;
    }

    /** Whether it fools this unit now (the ship carries the spoofer and the unit is spoofable). */
    boolean active() {
        return delay > 0;
    }

    /**
     * A volley of {@code kind} came due with its aim at (x, y) and the barrel or heading {@code aim}: held for the
     * delay ({@link SimEvents.Type#SPOOF_HESITATE} from {@code unit}). Returns false when it cannot be held (full: it
     * fires at once).
     */
    boolean hold(Enemy unit, int kind, double x, double y, double aim) {
        if (size == CAPACITY) {
            return false;
        }
        ticks[size] = delay;
        kinds[size] = kind;
        xs[size] = x;
        ys[size] = y;
        aims[size] = aim;
        size++;
        unit.events.add(SimEvents.Type.SPOOF_HESITATE, unit.x(), unit.y(), unit.kind());
        return true;
    }

    /** One step of the unit's own clock: every held shot comes a step nearer. */
    void tick() {
        for (int k = 0; k < size; k++) {
            ticks[k]--;
        }
    }

    /** The first held shot that is due now, -1 for none; {@link #release} takes it out once it is fired. */
    int due() {
        for (int k = 0; k < size; k++) {
            if (ticks[k] <= 0) {
                return k;
            }
        }
        return -1;
    }

    int kind(int k) {
        return kinds[k];
    }

    double x(int k) {
        return xs[k];
    }

    double y(int k) {
        return ys[k];
    }

    double aim(int k) {
        return aims[k];
    }

    /** Held shot {@code k} was fired: out of the queue (the later ones keep their order). */
    void release(int k) {
        for (int i = k; i < size - 1; i++) {
            ticks[i] = ticks[i + 1];
            kinds[i] = kinds[i + 1];
            xs[i] = xs[i + 1];
            ys[i] = ys[i + 1];
            aims[i] = aims[i + 1];
        }
        size--;
    }

    /** Whether it holds a shot now (the amber stutter goes on). */
    boolean holding() {
        return size > 0;
    }

    void addTo(StateHash hash) {
        if (size > 0) {
            hash.add(size);
            for (int k = 0; k < size; k++) {
                hash.add(ticks[k]).add(kinds[k]).add(xs[k]).add(ys[k]).add(aims[k]);
            }
        }
    }
}
