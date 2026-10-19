package vanguard.sim;

/**
 * One hovering unit's laser sweep (design/enemies/air/mantis): the wait for the next telegraph, the
 * telegraph and the sweep across its arc, and whether the sweep hit the ship. Each {@link Enemy} owns one,
 * used only while its stat block has a sweep (its fields are hashed only then); the unit works out where a
 * sweep aims ({@link #start}).
 */
final class SweepBeam {
    /** Steps to the next telegraph, -1 when none comes. */
    private int sweepWait;
    /** Steps into the current telegraph and sweep; -1 while none runs. */
    private int sweepTicks;
    /** The sweep's centre: radians clockwise from straight down. */
    private double sweepCentre;
    /** Whether the current sweep hit the ship. */
    private boolean sweepHit;

    /** Back to a unit without a sweep. */
    void clear() {
        sweepWait = -1;
        sweepTicks = -1;
        sweepCentre = 0;
        sweepHit = false;
    }

    /** Hashed only for a unit with a sweep. */
    void addTo(StateHash hash) {
        hash.add(sweepWait).add(sweepTicks).add(sweepCentre).add(sweepHit ? 1 : 0);
    }

    /** The unit starts to hover: the first telegraph comes after {@code sweep}'s first delay. */
    void arm(EnemySpec.Sweep sweep) {
        sweepWait = SimStep.ticks(sweep.firstDelaySeconds());
    }

    /**
     * One step while it hovers with {@code holdTicks} left: a running sweep goes on or ends, the wait for the
     * next telegraph runs; returns whether a telegraph starts now (only if its sweep ends before the hover
     * does), which the unit then aims with {@link #start}.
     */
    boolean due(EnemySpec.Sweep sweep, int holdTicks) {
        int telegraph = SimStep.ticks(sweep.telegraphSeconds());
        int total = telegraph + SimStep.ticks(sweep.sweepSeconds());
        if (sweepTicks >= 0 && ++sweepTicks >= total) {
            sweepTicks = -1;
        }
        if (sweepWait < 0 || --sweepWait > 0) {
            return false;
        }
        if (holdTicks < total) {
            sweepWait = -1;
            return false;
        }
        return true;
    }

    /** A telegraph starts, its sweep centred on {@code centre}; the next comes after {@code sweep}'s interval. */
    void start(EnemySpec.Sweep sweep, double centre) {
        sweepCentre = centre;
        sweepTicks = 0;
        sweepHit = false;
        sweepWait = SimStep.ticks(sweep.intervalSeconds());
    }

    /** Whether a telegraph or a sweep runs now. */
    boolean running() {
        return sweepTicks >= 0;
    }

    /** Whether the sweep is in its telegraph now. */
    boolean telegraphing(EnemySpec.Sweep sweep) {
        return sweepTicks >= 0 && sweepTicks < SimStep.ticks(sweep.telegraphSeconds());
    }

    /** Whether the beam is sweeping now. */
    boolean sweeping(EnemySpec.Sweep sweep) {
        return sweepTicks >= 0 && sweepTicks >= SimStep.ticks(sweep.telegraphSeconds());
    }

    /** Whether the sweep just entered its beam this step. */
    boolean sweepStarted(EnemySpec.Sweep sweep) {
        return sweepTicks == SimStep.ticks(sweep.telegraphSeconds());
    }

    /** Whether the sweep's telegraph just started this step. */
    boolean telegraphStarted() {
        return sweepTicks == 0;
    }

    /** The sweep's centre, radians clockwise from straight down. */
    double centre() {
        return sweepCentre;
    }

    /** How far the beam is through its sweep, 0 to 1, {@code alpha} between steps. */
    double into(EnemySpec.Sweep sweep, double alpha) {
        int telegraph = SimStep.ticks(sweep.telegraphSeconds());
        return Math.max(0, sweepTicks - telegraph + alpha) / SimStep.ticks(sweep.sweepSeconds());
    }

    boolean hit() {
        return sweepHit;
    }

    void markHit() {
        sweepHit = true;
    }
}
