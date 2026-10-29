package vanguard.sim;

/**
 * M9 part C: one unit's launcher clock (design/enemies/air/gilded-gunship, its Dart streams; see {@link
 * EnemySpec.Launcher}), or, on a launched unit, the serial of its carrier. A carrier's clock runs only while it is on
 * the play field and not stunned: the first stream its launcher's first delay after it entered, then a stream every
 * cycle, a stream's units its gap apart. Each {@link Enemy} owns one (hashed only on a carrier).
 */
final class Launching {
    /** M9 part H, step H1b: the carrier of a unit a ringed boss's bay launched ({@link #carrier()}). */
    static final int BOSS = -2;

    /** Whether its stat block has a launcher (its clock runs). */
    private boolean carrier;
    /** Steps to the next stream's first unit. */
    private int nextTicks;
    /** Units of the running stream still to launch, and the steps to the next of them. */
    private int left;

    private int gapTicks;
    /** On a launched unit: its carrier's serial; -1 for any other. */
    private int carrierSerial = -1;

    /** Back to a unit that neither launches nor was launched. */
    void clear() {
        carrier = false;
        nextTicks = 0;
        left = 0;
        gapTicks = 0;
        carrierSerial = -1;
    }

    /** A carrier with {@code launcher} enters: its first stream comes its first delay after it reached the field. */
    void start(EnemySpec.Launcher launcher) {
        carrier = true;
        nextTicks = Math.max(1, SimStep.ticks(launcher.firstSeconds()));
        left = 0;
        gapTicks = 0;
    }

    /**
     * One step of its clock while it is on the play field ({@code onField}; off it the clock waits): returns the place
     * in its stream of the unit it launches now (0 the stream's first), -1 for none.
     */
    int step(EnemySpec.Launcher launcher, boolean onField) {
        if (!carrier || !onField) {
            return -1;
        }
        // The cycle runs from one stream's first unit to the next's, through the stream.
        nextTicks--;
        if (left > 0) {
            if (--gapTicks > 0) {
                return -1;
            }
            int place = launcher.stream() - left;
            left--;
            gapTicks = SimStep.ticks(launcher.gapSeconds());
            return place;
        }
        if (nextTicks > 0) {
            return -1;
        }
        nextTicks = SimStep.ticks(launcher.everySeconds());
        left = launcher.stream() - 1;
        gapTicks = SimStep.ticks(launcher.gapSeconds());
        return 0;
    }

    /** A launched unit: {@code serial} is its carrier's. */
    void launchedBy(int serial) {
        carrierSerial = serial;
    }

    /** The serial of the carrier that launched it; -1 for none. */
    int carrier() {
        return carrierSerial;
    }

    /** Whether it is a carrier (its clock is hashed). */
    boolean active() {
        return carrier;
    }

    void addTo(StateHash hash) {
        hash.add(nextTicks).add(left).add(gapTicks);
    }
}
