package vanguard.sim;

/**
 * M8 part E, one unit's song (design/enemies/naval/siren; {@link EnemySpec.SlowRing}): the steps to its next ring and
 * the sac's tell before it; {@link EnemyForce} sends the ring ({@link SonarRings}) in the step it is due. Each {@link
 * Enemy} owns one, active only for a unit with a slowing ring (its fields are hashed only then); its clock stops while
 * the EMP Burst stuns the unit. Allocation-free.
 */
final class SirenSong {
    private boolean active;
    /** Steps to its next ring. */
    private int untilRing;
    /** The tell's length and the steps from one ring to the next. */
    private int tellTicks;

    private int intervalTicks;
    /** Within a step (not hashed): whether its tell began in it, whether its ring is due in it. */
    private boolean tellNow;

    private boolean ringNow;

    /** Back to a unit without a song. */
    void clear() {
        active = false;
        untilRing = 0;
        tellTicks = 0;
        intervalTicks = 0;
        tellNow = false;
        ringNow = false;
    }

    /**
     * It enters with {@code spec}'s ring, if any: its first ring {@code firstSeconds} after this step plus {@code
     * delaySeconds} (a nest's place in the phase rule, {@link WallMounts}).
     */
    void start(EnemySpec spec, double delaySeconds) {
        clear();
        if (spec.slowRing().isEmpty()) {
            return;
        }
        EnemySpec.SlowRing ring = spec.slowRing().get();
        active = true;
        untilRing = Math.max(1, SimStep.ticks(ring.firstSeconds() + delaySeconds));
        tellTicks = SimStep.ticks(ring.tellSeconds());
        intervalTicks = Math.max(1, SimStep.ticks(ring.intervalSeconds()));
    }

    boolean active() {
        return active;
    }

    /** One step of its clock: its tell begins {@code tellTicks} before its ring; the ring is due when it runs out. */
    void step() {
        tellNow = false;
        ringNow = false;
        if (!active) {
            return;
        }
        untilRing--;
        if (untilRing == tellTicks && tellTicks > 0) {
            tellNow = true;
        }
        if (untilRing <= 0) {
            ringNow = true;
            untilRing = intervalTicks;
        }
    }

    /** Whether its sac's tell began in this step ({@link SimEvents.Type#RING_TELL}). */
    boolean tellNow() {
        return tellNow;
    }

    /** Whether its ring leaves its sac in this step. */
    boolean ringNow() {
        return ringNow;
    }

    /**
     * How far its sac's tell has run between the previous and the current step, 0 to 1 (1 as the ring leaves); 0
     * outside the tell or without a song.
     */
    double tell(double alpha) {
        if (!active || tellTicks == 0 || untilRing > tellTicks) {
            return 0;
        }
        double into = tellTicks - untilRing + alpha;
        return Math.clamp(into / tellTicks, 0, 1);
    }

    /** Seconds to its next ring; NaN without a song. */
    double secondsToRing() {
        return active ? untilRing * SimStep.SECONDS : Double.NaN;
    }

    void addTo(StateHash hash) {
        hash.add(untilRing).add(tellTicks).add(intervalTicks);
    }
}
