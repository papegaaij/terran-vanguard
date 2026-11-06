package vanguard.sim;

/**
 * M11 part A, step A1b (design/world/vrell-space, The quiet; design/player/wingmen, Rook in Act 7; Level 46): the
 * quiet's hold on Rook's pick, owned by a {@link Sortie} only in a level with a {@link VrellLevel.Quiet} ({@link
 * Sortie#quiet()}). From the quiet's start to its end on the level clock his pick skips the units on the screen (the
 * drifting Husks: the scene holds its breath) until the quiet is broken: the first step in which a shot hits a unit
 * ({@link SimEvents.Type#ENEMY_HIT}) or a unit fires ({@link SimEvents.Type#ENEMY_FIRED}) ends the hold for the rest
 * of the attempt ({@link SimEvents.Type#QUIET_BROKEN}). His shots still hit what is in their way, and he still keeps
 * clear of what is beside him. Allocates nothing.
 */
public final class QuietHold {
    private final int fromTicks;
    private final int toTicks;
    /** Whether the quiet has been broken in this attempt. */
    private boolean broken;

    QuietHold(VrellLevel.Quiet spec) {
        fromTicks = SimStep.ticks(spec.from());
        toTicks = SimStep.ticks(spec.to());
    }

    /** Back to the level start: not broken. */
    void reset() {
        broken = false;
    }

    /**
     * Back to a boss checkpoint at {@code levelTick}: a checkpoint before the quiet starts it afresh; one in it or after
     * it keeps the quiet as it is (a level's boss comes after its quiet).
     */
    void restore(int levelTick) {
        if (levelTick < fromTicks) {
            broken = false;
        }
    }

    /** Whether Rook's pick holds at {@code levelTick} (in the quiet, unbroken). */
    boolean holds(int levelTick) {
        return !broken && levelTick >= fromTicks && levelTick < toTicks;
    }

    /** At a step's end at {@code levelTick}: the step's events break the quiet (a hit on a unit, a unit's shot). */
    void step(int levelTick, SimEvents events, Ship ship) {
        if (holds(levelTick)
                && (events.count(SimEvents.Type.ENEMY_HIT) > 0 || events.count(SimEvents.Type.ENEMY_FIRED) > 0)) {
            broken = true;
            events.add(SimEvents.Type.QUIET_BROKEN, ship.x(), ship.y());
        }
    }

    /** Whether the quiet has been broken in this attempt. */
    public boolean broken() {
        return broken;
    }

    void addTo(StateHash hash) {
        hash.add(broken ? 1 : 0);
    }
}
