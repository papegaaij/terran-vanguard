package vanguard.sim;

import java.util.List;

/**
 * M11 part A (design/enemies, Distorted space; design/world/vrell-space; Level 44): a level's current in play, owned by
 * a {@link Sortie} only in a level with a {@link HazardSpec.Current} ({@link Sortie#current()}). Each step it reads the
 * angle θ off its profile on the level clock (smoothstep between the points) and the sideways drift, the step's scroll
 * times sin θ (signed: it flips with a reversal's scroll and stops in a halt); its reversals' tells come {@code told}
 * ahead ({@link SimEvents.Type#REVERSAL_TOLD}, then {@link SimEvents.Type#REVERSAL_RUN}). The drift carries the units
 * not holding a station ({@link EnemyMoves}), the chains' histories ({@link Chain}), the enemy bullets, the pickups and
 * the debris ({@link #carry}); the ship, its shots, Rook and the player's drones fly in the screen's frame and never
 * drift; an aimed volley is led for it ({@link EnemyFire}); a laser line is locked on the screen. A function of the level
 * clock and the scroll alone, but for its next tell; allocates nothing.
 *
 * <p>M11 part H, step H1a (design/enemies/bosses/choir-heart, phase 2): it keeps a boss's {@link PhaseDrift} (the
 * drift inside a boss phase while the scroll is halted), made for a boss with a drift, also in a level without a
 * current of its own (then its level profile is a straight 0°): its drift adds to the level's, its angle stands for the
 * level's while it drifts, its reversal tells, runs and breaks the calm as a level's does (its index after theirs).
 */
public final class Current {
    /** A reversal's ease into and out of its reversed run, s (Level 20's turnabout). */
    static final double EASE_SECONDS = 1;

    private final int[] bendTicks;
    private final double[] bendDegrees;
    private final int[] reversalTicks;
    private final int[] reversalEnds;
    private final int toldTicks;
    /** The next reversal still to tell, and the next still to start. */
    private int nextTold;

    private int nextRun;
    /** The angle and the drift of the current step. */
    private double degrees;

    private double drift;
    /** M11 part H, step H1a: a boss's drift inside its phase; null without. */
    private final PhaseDrift phase;

    Current(HazardSpec.Current spec) {
        this(spec, null, null);
    }

    /**
     * The level's current {@code spec} (null: none of its own, only the boss's drift) and the boss {@code piece}'s
     * {@code phaseDrift} (null: none).
     */
    Current(HazardSpec.Current spec, PhaseDriftSpec phaseDrift, SetPiece piece) {
        if (spec == null) {
            spec = new HazardSpec.Current(List.of(new HazardSpec.Bend(0, 0)), List.of(), 0);
        }
        int bends = spec.profile().size();
        bendTicks = new int[bends];
        bendDegrees = new double[bends];
        for (int i = 0; i < bends; i++) {
            bendTicks[i] = SimStep.ticks(spec.profile().get(i).t());
            bendDegrees[i] = spec.profile().get(i).degrees();
        }
        int reversals = spec.reversals().size();
        reversalTicks = new int[reversals];
        reversalEnds = new int[reversals];
        for (int i = 0; i < reversals; i++) {
            HazardSpec.Reversal reversal = spec.reversals().get(i);
            reversalTicks[i] = SimStep.ticks(reversal.t());
            reversalEnds[i] = SimStep.ticks(reversal.t() + 2 * EASE_SECONDS + reversal.runSeconds());
        }
        toldTicks = SimStep.ticks(spec.toldSeconds());
        phase = phaseDrift == null ? null : new PhaseDrift(phaseDrift, piece, reversals);
    }

    /** Back to the level start: no reversal told. */
    void reset() {
        nextTold = 0;
        nextRun = 0;
        degrees = bendDegrees[0];
        drift = 0;
        if (phase != null) {
            phase.reset();
        }
    }

    /** Back to a boss checkpoint at {@code levelTick}: the tells and runs due by then were raised. */
    void restore(int levelTick) {
        reset();
        while (nextTold < reversalTicks.length && reversalTicks[nextTold] - toldTicks <= levelTick) {
            nextTold++;
        }
        while (nextRun < reversalTicks.length && reversalTicks[nextRun] <= levelTick) {
            nextRun++;
        }
        degrees = degreesAt(levelTick);
    }

    /** The profile's angle at {@code levelTick}, degrees (positive: the drift to the right in a forward scroll). */
    double degreesAt(int levelTick) {
        if (levelTick <= bendTicks[0]) {
            return bendDegrees[0];
        }
        for (int i = 1; i < bendTicks.length; i++) {
            if (levelTick < bendTicks[i]) {
                double share = (double) (levelTick - bendTicks[i - 1]) / (bendTicks[i] - bendTicks[i - 1]);
                double eased = share * share * (3 - 2 * share);
                return bendDegrees[i - 1] + (bendDegrees[i] - bendDegrees[i - 1]) * eased;
            }
        }
        return bendDegrees[bendDegrees.length - 1];
    }

    /**
     * The step at {@code levelTick} whose scroll moved {@code scrollStep} px down the screen (negative: reversed): its
     * angle and drift; the tells and runs due ({@link SimEvents.Type#REVERSAL_TOLD}, {@link
     * SimEvents.Type#REVERSAL_RUN}, value the reversal's index).
     */
    void step(int levelTick, double scrollStep, SimEvents events) {
        degrees = degreesAt(levelTick);
        drift = scrollStep * Trig.sin(StrictMath.toRadians(degrees));
        while (nextTold < reversalTicks.length && reversalTicks[nextTold] - toldTicks <= levelTick) {
            events.add(SimEvents.Type.REVERSAL_TOLD, PlayField.WIDTH / 2.0, PlayField.HEIGHT / 2.0, nextTold);
            nextTold++;
        }
        while (nextRun < reversalTicks.length && reversalTicks[nextRun] <= levelTick) {
            events.add(SimEvents.Type.REVERSAL_RUN, PlayField.WIDTH / 2.0, PlayField.HEIGHT / 2.0, nextRun);
            nextRun++;
        }
        if (phase != null) {
            // M11 part H, step H1a: the boss's drift inside its phase (the arena halted: the level's drift is 0).
            phase.step(events);
            if (phase.active()) {
                degrees = phase.degrees();
                drift += phase.driftStep();
            }
        }
    }

    /** M11 part H, step H1a: the boss's drift inside its phase; null without. */
    public PhaseDrift phaseDrift() {
        return phase;
    }

    /** M11 part H, step H1a: whether the boss's drift runs reversed now (its jumps' side). */
    boolean driftReversed() {
        return phase != null && phase.reversed();
    }

    /** The angle now, degrees off straight down (the backdrop's slide, the jumps' side). */
    public double degrees() {
        return degrees;
    }

    /** The sideways drift this step, px (positive: to the right). */
    public double driftStep() {
        return drift;
    }

    /** The sideways drift now, px/s (positive: to the right). */
    public double drift() {
        return drift * SimStep.PER_SECOND;
    }

    /** Whether no reversal is told or running at {@code levelTick} (a chain's loop-back waits otherwise). */
    public boolean calm(int levelTick) {
        if (phase != null && (phase.telling() || phase.running())) {
            return false;
        }
        for (int i = 0; i < reversalTicks.length; i++) {
            if (levelTick >= reversalTicks[i] - toldTicks && levelTick < reversalEnds[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * The index of the reversal whose tell runs at {@code levelTick} (the HUD's chevron, from its tell to its start);
     * -1 for none.
     */
    public int told(int levelTick) {
        if (phase != null && phase.telling()) {
            return phase.index();
        }
        for (int i = 0; i < reversalTicks.length; i++) {
            if (levelTick >= reversalTicks[i] - toldTicks && levelTick < reversalTicks[i]) {
                return i;
            }
        }
        return -1;
    }

    /** The index of the reversal running at {@code levelTick} (its eases and its reversed run); -1 for none. */
    public int running(int levelTick) {
        if (phase != null && phase.running()) {
            return phase.index();
        }
        for (int i = 0; i < reversalTicks.length; i++) {
            if (levelTick >= reversalTicks[i] && levelTick < reversalEnds[i]) {
                return i;
            }
        }
        return -1;
    }

    /** The enemy bullets, the pickups and the debris ride the drift of this step. */
    void carry(Pool<EnemyBullet> bullets, Pool<Pickup> pickups, Pool<Debris> debris) {
        if (drift == 0) {
            return;
        }
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).shift(drift);
        }
        for (int i = 0; i < pickups.size(); i++) {
            pickups.get(i).shift(drift);
        }
        for (int i = 0; i < debris.size(); i++) {
            debris.get(i).shift(drift);
        }
    }

    void addTo(StateHash hash) {
        hash.add(nextTold).add(nextRun);
        if (phase != null) {
            // M11 part H, step H1a: only with a boss's drift.
            phase.addTo(hash);
        }
    }
}
