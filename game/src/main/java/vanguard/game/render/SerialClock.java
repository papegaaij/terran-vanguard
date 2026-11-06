package vanguard.game.render;

import java.util.Arrays;

/**
 * M11 part A, step A3a: when each unit's state began, by its serial, for a look that runs over the state's time but whose
 * unit tells only whether it is in it (a Gate Warden's leaving tell, a window shell's regrowth). Each step {@link
 * #begin}, then {@link #touch} every unit in the state, then {@link #end}: a unit out of it is forgotten. A fixed
 * table; the units past its size are ignored (a look then shows them at its start). Allocates nothing.
 */
final class SerialClock {
    private final int[] serials;
    private final long[] since;
    private final boolean[] seen;
    private int size;

    SerialClock(int capacity) {
        serials = new int[capacity];
        since = new long[capacity];
        seen = new boolean[capacity];
    }

    /** A new step: no unit is seen yet. */
    void begin() {
        Arrays.fill(seen, 0, size, false);
    }

    /** The unit {@code serial} is in the state at step {@code tick}: from now if it was not. */
    void touch(int serial, long tick) {
        int at = find(serial);
        if (at < 0) {
            if (size == serials.length) {
                return;
            }
            at = size++;
            serials[at] = serial;
            since[at] = tick;
        }
        seen[at] = true;
    }

    /** The step ends: the units not touched in it left the state. */
    void end() {
        int kept = 0;
        for (int i = 0; i < size; i++) {
            if (seen[i]) {
                serials[kept] = serials[i];
                since[kept] = since[i];
                seen[kept] = true;
                kept++;
            }
        }
        size = kept;
    }

    /** The step the unit {@code serial} entered the state, or {@code fallback} for one not in it (or not kept). */
    long since(int serial, long fallback) {
        int at = find(serial);
        return at < 0 ? fallback : since[at];
    }

    /** The units in the state now. */
    int size() {
        return size;
    }

    /** Forgets every unit (the level restarts). */
    void clear() {
        size = 0;
    }

    private int find(int serial) {
        for (int i = 0; i < size; i++) {
            if (serials[i] == serial) {
                return i;
            }
        }
        return -1;
    }
}
