package vanguard.sim;

/**
 * M8 part D (design/enemies/naval/abyss-ray, the escorts peel off its shadow as it rises): the escorts of a {@code
 * carrier + escorts} wave whose carrier has a rise, held unspawned (in entry order, with the serial and tag they
 * entered with) until their carrier's layer flips to {@code air}, or it ends first; {@link EnemyForce} then peels
 * them off. Fixed arrays: allocation-free.
 */
final class RiseEscorts {
    /** The most escorts held at once (two Rays' four, with room to spare). */
    static final int CAPACITY = 16;

    private final Spawn[] plans = new Spawn[CAPACITY];
    private final Enemy[] carriers = new Enemy[CAPACITY];
    private final int[] serials = new int[CAPACITY];
    private final String[] tags = new String[CAPACITY];
    private int size;

    /** A new attempt: none held. */
    void reset() {
        for (int k = 0; k < size; k++) {
            plans[k] = null;
            carriers[k] = null;
            tags[k] = null;
        }
        size = 0;
    }

    /** Holds {@code plan}'s escort for {@code carrier}; false (nothing held) when the store is full. */
    boolean hold(Spawn plan, Enemy carrier, int serial, String tag) {
        if (size == CAPACITY) {
            return false;
        }
        plans[size] = plan;
        carriers[size] = carrier;
        serials[size] = serial;
        tags[size] = tag;
        size++;
        return true;
    }

    int size() {
        return size;
    }

    /** How many escorts it holds for {@code carrier}. */
    int count(Enemy carrier) {
        int count = 0;
        for (int k = 0; k < size; k++) {
            if (carriers[k] == carrier) {
                count++;
            }
        }
        return count;
    }

    /** The first held escort of {@code carrier}, or -1. */
    int first(Enemy carrier) {
        for (int k = 0; k < size; k++) {
            if (carriers[k] == carrier) {
                return k;
            }
        }
        return -1;
    }

    Spawn plan(int k) {
        return plans[k];
    }

    int serial(int k) {
        return serials[k];
    }

    String tag(int k) {
        return tags[k];
    }

    /** Lets go of held escort {@code k}, the later ones moving up (their order kept). */
    void remove(int k) {
        System.arraycopy(plans, k + 1, plans, k, size - k - 1);
        System.arraycopy(carriers, k + 1, carriers, k, size - k - 1);
        System.arraycopy(serials, k + 1, serials, k, size - k - 1);
        System.arraycopy(tags, k + 1, tags, k, size - k - 1);
        size--;
        plans[size] = null;
        carriers[size] = null;
        tags[size] = null;
    }

    /** Hashed only while it holds escorts: their serials and their carriers'. */
    void addTo(StateHash hash) {
        hash.add(size);
        for (int k = 0; k < size; k++) {
            hash.add(serials[k]).add(carriers[k].serial());
        }
    }
}
