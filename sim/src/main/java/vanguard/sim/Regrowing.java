package vanguard.sim;

/**
 * M10 part A (design/enemies/ground/graft-turret, Regrowth): one ground unit's {@link EnemySpec.Regrowth} cycle. Its
 * turret's death ({@link #expose}) pays and counts the first time only and exposes the root (its HP and hit box) for
 * the window; a blast that killed the turret does not reach the root (it was covered when the blast landed: the root
 * takes hits from the next step on). The root's death ends the unit. The window closed, the turret knits back (not
 * hittable), then stands again with its full HP and fires its wake later. The window and the knit are biological: they
 * run on the real steps, an EMP stun's included. The unit stays in the pool through the cycle. Allocates nothing;
 * hashed only for a regrowing unit.
 */
public final class Regrowing {
    /** The cycle's phases. */
    public enum Phase {
        /** The turret stands (and fires). */
        TURRET,
        /** The turret is down, the root exposed for the window. */
        ROOT,
        /** The window closed: the turret knits back over the root, not hittable. */
        KNIT
    }

    private Phase phase = Phase.TURRET;
    private int ticks;
    private int windowTicks;
    private boolean paid;
    /** Whether the root was exposed in this step (it takes hits from the next). */
    private boolean fresh;

    void clear() {
        phase = Phase.TURRET;
        ticks = 0;
        windowTicks = 0;
        paid = false;
        fresh = false;
    }

    /**
     * The turret is destroyed: the root is exposed for the window ({@link SimEvents.Type#ROOT_EXPOSED}, value the
     * unit's kind). Returns whether this was its first death (the one that pays and counts).
     */
    boolean expose(EnemySpec.Regrowth spec, Enemy unit) {
        boolean first = !paid;
        paid = true;
        phase = Phase.ROOT;
        windowTicks = Math.max(1, SimStep.ticks(spec.windowSeconds()));
        ticks = windowTicks;
        fresh = true;
        unit.hp = spec.rootHp();
        unit.box = spec.rootHitbox();
        unit.burstLeft = 0;
        unit.events.add(SimEvents.Type.ROOT_EXPOSED, unit.x(), unit.y(), unit.kind());
        return first;
    }

    /**
     * One real step: the window counts down (at its end the knit starts: {@link SimEvents.Type#KNITTING}), the knit
     * counts down (at its end the turret stands again with its full HP and its first volley its wake later: {@link
     * SimEvents.Type#REGROWN}).
     */
    void tick(EnemySpec.Regrowth spec, Enemy unit) {
        fresh = false;
        if (phase == Phase.ROOT && --ticks <= 0) {
            phase = Phase.KNIT;
            ticks = Math.max(1, SimStep.ticks(spec.knitSeconds()));
            unit.events.add(SimEvents.Type.KNITTING, unit.x(), unit.y(), unit.kind());
        } else if (phase == Phase.KNIT && --ticks <= 0) {
            phase = Phase.TURRET;
            unit.hp = unit.spec().hp();
            unit.box = unit.spec().hitbox();
            unit.burstLeft = 0;
            unit.volleyTicks = SimStep.ticks(spec.wakeSeconds()) + 1;
            unit.events.add(SimEvents.Type.REGROWN, unit.x(), unit.y(), unit.kind());
        }
    }

    /** Its phase. */
    public Phase phase() {
        return phase;
    }

    /** Whether it takes no damage now: knitting, or its root exposed only in this step. */
    boolean shielded() {
        return phase == Phase.KNIT || (phase == Phase.ROOT && fresh);
    }

    /** Whether its gun is silent: its root exposed or knitting. */
    boolean quiet() {
        return phase != Phase.TURRET;
    }

    /** Whether its first death was paid (a later one pays and counts nothing; nor does it count as escaped). */
    public boolean paid() {
        return paid;
    }

    /**
     * How far its window has run, from 0 as the root is exposed to 1 as it closes (the pulse from 1 to 4 Hz, the
     * shrinking ring); 0 outside the window.
     */
    public double windowShare() {
        return phase == Phase.ROOT && windowTicks > 0 ? 1 - (double) ticks / windowTicks : 0;
    }

    /** Steps left of its knit; 0 outside it. */
    public int knitTicks() {
        return phase == Phase.KNIT ? ticks : 0;
    }

    void addTo(StateHash hash) {
        hash.add(phase.ordinal()).add(ticks).add(windowTicks).add(paid ? 1 : 0).add(fresh ? 1 : 0);
    }
}
