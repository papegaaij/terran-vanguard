package vanguard.sim;

/**
 * M9 part E, one fixed ground unit's laser sweep (design/enemies/ground/sentinel-tower, {@code laser-sweep}; {@link
 * EnemySpec.GroundSweep}): the wait for the next telegraph while the unit is on the play field, the telegraph and the
 * sweep (an arc centred on the ship's bearing, or every few sweeps a full turn), and whether the sweep hit the ship.
 * Each {@link Enemy} owns one, used (and hashed) only while its stat block has a ground sweep. Allocates nothing.
 */
final class GroundSweepBeam {
    /** {@link #wait}'s value before the unit first came onto the play field. */
    private static final int UNARMED = -2;

    /** Steps to the next telegraph; {@link #UNARMED} before the first. */
    private int wait;
    /** Steps into the current telegraph and sweep; -1 while none runs. */
    private int ticks;
    /** The sweeps started so far (the current one's number, from 1). */
    private int count;
    /** Whether the current sweep is a full turn. */
    private boolean full;
    /** The current sweep's telegraph and beam, steps. */
    private int telegraphTicks;

    private int liveTicks;
    /** The beam's first heading (radians clockwise from straight down) and its signed turn over the sweep. */
    private double from;

    private double span;
    /** Whether the current sweep hit the ship. */
    private boolean hit;

    /** Back to a unit without a sweep (or a new life). */
    void clear() {
        wait = UNARMED;
        ticks = -1;
        count = 0;
        full = false;
        telegraphTicks = 0;
        liveTicks = 0;
        from = 0;
        span = 0;
        hit = false;
    }

    void addTo(StateHash hash) {
        hash.add(wait)
                .add(ticks)
                .add(count)
                .add(full ? 1 : 0)
                .add(from)
                .add(span)
                .add(hit ? 1 : 0);
    }

    /**
     * One step of a unit at (x, y), {@code onField} whether it overlaps the play field: a running sweep goes on or
     * ends; the wait for the next telegraph runs while it is on the field (the first {@code sweep}'s first delay after
     * it came onto it); a telegraph that is due starts, centred on the ship's bearing from it.
     */
    void step(
            EnemySpec.Sweep sweep,
            EnemySpec.GroundSweep ground,
            boolean onField,
            double x,
            double y,
            double shipX,
            double shipY) {
        if (ticks >= 0 && ++ticks >= telegraphTicks + liveTicks) {
            ticks = -1;
        }
        if (!onField) {
            return;
        }
        if (wait == UNARMED) {
            wait = Math.max(1, SimStep.ticks(sweep.firstDelaySeconds()));
        }
        if (--wait > 0) {
            return;
        }
        start(sweep, ground, Enemy.heading(shipX - x, shipY - y));
    }

    private void start(EnemySpec.Sweep sweep, EnemySpec.GroundSweep ground, double bearing) {
        count++;
        full = ground.fullTurn(count);
        ticks = 0;
        hit = false;
        if (full) {
            // The full turns alternate their direction; each starts on the ship's bearing (the start ray).
            int turn = ground.fullTurnNumber(count);
            from = bearing;
            span = (turn % 2 == 1 ? 2 : -2) * StrictMath.PI;
            telegraphTicks = SimStep.ticks(ground.fullTurnTelegraphSeconds());
            liveTicks = Math.max(1, SimStep.ticks(ground.fullTurnSeconds()));
            wait = SimStep.ticks(ground.fullTurnSlotSeconds());
        } else {
            // The arcs sweep one way and the next the other, centred on the bearing.
            double direction = count % 2 == 1 ? 1 : -1;
            from = bearing - direction * sweep.arcRadians() / 2;
            span = direction * sweep.arcRadians();
            telegraphTicks = SimStep.ticks(sweep.telegraphSeconds());
            liveTicks = Math.max(1, SimStep.ticks(sweep.sweepSeconds()));
            wait = SimStep.ticks(sweep.intervalSeconds());
        }
    }

    /** The EMP Burst's stun starts: a running sweep is cut; the next telegraph comes as soon as the stun is over. */
    void cut() {
        if (ticks >= 0) {
            ticks = -1;
            wait = 1;
        }
    }

    boolean running() {
        return ticks >= 0;
    }

    boolean telegraphing() {
        return ticks >= 0 && ticks < telegraphTicks;
    }

    boolean sweeping() {
        return ticks >= telegraphTicks;
    }

    boolean telegraphStarted() {
        return ticks == 0;
    }

    boolean sweepStarted() {
        return ticks >= 0 && ticks == telegraphTicks;
    }

    /** Whether the current sweep (telegraph or beam) is a full turn. */
    boolean fullTurn() {
        return ticks >= 0 && full;
    }

    /** The sweeps started so far. */
    int count() {
        return count;
    }

    /** The beam's first heading, radians clockwise from straight down. */
    double from() {
        return from;
    }

    /** The beam's last heading. */
    double to() {
        return from + span;
    }

    /** The middle of the sweep (an arc's centre; a full turn's start ray). */
    double centre() {
        return full ? from : from + span / 2;
    }

    /** The beam's heading now, {@code alpha} between steps. */
    double beam(double alpha) {
        double into = Math.clamp((ticks - telegraphTicks + alpha) / liveTicks, 0, 1);
        return from + span * into;
    }

    boolean hit() {
        return hit;
    }

    void markHit() {
        hit = true;
    }
}
