package vanguard.sim;

/**
 * M10 part A (design/enemies, Regeneration; design/enemies/air/chimera): one unit's {@link EnemySpec.Regeneration}.
 * Any damage restarts its idle count ({@link EnemyDamage}: a shot, a blast, a ram, a beam's every step); once it has
 * gone its delay without one it refills its rate of its maximum HP a second (its stat block's, already scaled), never
 * past it. It runs on the unit's own clock, so an EMP stun pauses both the delay and the healing. A kill pays once:
 * healed HP pays nothing. {@link SimEvents.Type#REGENERATING} marks the healing's start (value 1) and its end (value
 * 0: a hit or full again). Allocates nothing; hashed only for a regenerating unit.
 */
public final class Regenerating {
    private int idle;
    private boolean healing;

    void clear() {
        idle = 0;
        healing = false;
    }

    /** It took damage: the delay starts again (the healing stops). */
    void hit(Enemy unit) {
        idle = 0;
        if (healing) {
            healing = false;
            unit.events.add(SimEvents.Type.REGENERATING, unit.x(), unit.y(), 0);
        }
    }

    /** One step of its own clock: the delay counts, then the HP refills toward its maximum. */
    void step(EnemySpec.Regeneration spec, Enemy unit) {
        if (idle < Integer.MAX_VALUE) {
            idle++;
        }
        double most = unit.spec().hp();
        if (idle <= SimStep.ticks(spec.delaySeconds()) || !(unit.hp < most) || !(unit.hp > 0)) {
            return;
        }
        unit.hp = Math.min(most, unit.hp + spec.rate() * most * SimStep.SECONDS);
        boolean full = unit.hp >= most;
        if (!healing && !full) {
            healing = true;
            unit.events.add(SimEvents.Type.REGENERATING, unit.x(), unit.y(), 1);
        } else if (healing && full) {
            healing = false;
            unit.events.add(SimEvents.Type.REGENERATING, unit.x(), unit.y(), 0);
        }
    }

    /** Whether it is healing now (the teal seam pulse, the Targeting computer's teal edge). */
    public boolean healing() {
        return healing;
    }

    void addTo(StateHash hash) {
        hash.add(idle).add(healing ? 1 : 0);
    }
}
