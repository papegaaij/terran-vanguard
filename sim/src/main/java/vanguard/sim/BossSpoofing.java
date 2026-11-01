package vanguard.sim;

/**
 * M10 part G, step G1b (design/player/systems, the IFF spoofer; design/enemies/ground/halo-platform; Level 41): a
 * {@link BossSpec#spoofable() spoofable} set piece's late volleys, the set piece's counterpart of a unit's {@link
 * Spoofing}. While the ship carries the spoofer every volley that comes due on one of its parts is held for the
 * spoofer's delay ({@link SimEvents.Type#BOSS_SPOOFED} at the part, the amber stutter) and then fired from where the
 * part is by then: an aimed shot (or fan) where the ship was when it came due (its stale aim), a ring as it is, a rail
 * line's request late (its telegraph then starts as any line's does, locked as it starts). The piece's cycles run on
 * unchanged (the next volley is due on time) and a burst's later shots keep their spacing after the delayed first. A
 * held volley whose part died is lost (a battery turned out of the fire arc meanwhile still fires it); all are lost
 * with the piece. Kept by
 * its {@link SetPiece} only for a spoofable boss; fixed capacity, allocates nothing; hashed only while it holds one.
 */
final class BossSpoofing {
    /** An aimed shot or fan, fired at its stale point. */
    static final int SHOT = 0;
    /** A ring of bullets. */
    static final int RING = 1;
    /** A rail line's request ({@link BossLines#request}). */
    static final int LINE = 2;

    private static final int CAPACITY = 24;

    /** The spoofer's delay, steps; 0 while the ship carries none (set once for the sortie). */
    private int delay;

    private final int[] ticks = new int[CAPACITY];
    private final int[] kinds = new int[CAPACITY];
    private final int[] attacks = new int[CAPACITY];
    private final int[] parts = new int[CAPACITY];
    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private int size;

    /** The ship carries the spoofer: every volley is held {@code delayTicks} steps (0: none). */
    void delay(int delayTicks) {
        delay = Math.max(0, delayTicks);
    }

    /** Back to the level start (and the checkpoint, the kill): nothing held. */
    void clear() {
        size = 0;
    }

    /** Whether it fools the piece now. */
    boolean active() {
        return delay > 0;
    }

    /**
     * Attack {@code a}'s volley of {@code kind} from part {@code p} came due, the ship at (x, y): held for the delay.
     * Returns false when it cannot be held (full: it fires at once).
     */
    boolean hold(int kind, int a, int p, double x, double y) {
        if (size == CAPACITY) {
            return false;
        }
        ticks[size] = delay;
        kinds[size] = kind;
        attacks[size] = a;
        parts[size] = p;
        xs[size] = x;
        ys[size] = y;
        size++;
        return true;
    }

    /** One step: every held volley comes a step nearer. */
    void tick() {
        for (int k = 0; k < size; k++) {
            ticks[k]--;
        }
    }

    /** The first held volley that is due now, -1 for none; {@link #release} takes it out once it is fired. */
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

    int attack(int k) {
        return attacks[k];
    }

    int part(int k) {
        return parts[k];
    }

    double x(int k) {
        return xs[k];
    }

    double y(int k) {
        return ys[k];
    }

    /** Held volley {@code k} was fired (or lost): out of the queue, the later ones keeping their order. */
    void release(int k) {
        for (int i = k; i < size - 1; i++) {
            ticks[i] = ticks[i + 1];
            kinds[i] = kinds[i + 1];
            attacks[i] = attacks[i + 1];
            parts[i] = parts[i + 1];
            xs[i] = xs[i + 1];
            ys[i] = ys[i + 1];
        }
        size--;
    }

    /** Whether it holds a volley now (the amber stutter goes on). */
    boolean holding() {
        return size > 0;
    }

    void addTo(StateHash hash) {
        if (size > 0) {
            hash.add(size);
            for (int k = 0; k < size; k++) {
                hash.add(ticks[k])
                        .add(kinds[k])
                        .add(attacks[k])
                        .add(parts[k])
                        .add(xs[k])
                        .add(ys[k]);
            }
        }
    }
}
