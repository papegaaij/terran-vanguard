package vanguard.sim;

/**
 * M9 part A: one unit's hex shield (design/enemies, Enemy shields; {@link EnemySpec.Shield}): its pool over the hull,
 * spent first by every hit ({@link EnemyDamage}), the refill after its delay, a {@code shield-breaker} hit's and the
 * EMP Burst's emptying with the refill held back. Each {@link Enemy} owns one; it is used, stepped and hashed only for
 * a unit whose stat block has a shield, so the other units hash as before. Allocates nothing.
 */
final class EnemyShield {
    /** The pool left and its maximum (the stat block's HP); 0 for a unit without a shield. */
    private double shield;

    private double max;
    /** Steps since the last hit (the refill's delay); counts from 0 after a hit. */
    private int idle;
    /** Steps the refill is still held back (a breaker's hit, the EMP's stun); 0 when none. */
    private int hold;
    /** Steps until a hit is reported again (not hashed: it only spaces the events). */
    private int quiet;

    /** A new life with {@code spec}'s shield, full; none without one. */
    void start(EnemySpec spec) {
        max = spec.shield().isPresent() ? spec.shield().get().hp() : 0;
        shield = max;
        idle = 0;
        hold = 0;
        quiet = 0;
    }

    /** Hashed only for a unit with a shield. */
    void addTo(StateHash hash) {
        hash.add(shield).add(idle).add(hold);
    }

    /** Whether the unit carries a shield at all (its stat block has one). */
    boolean fitted() {
        return max > 0;
    }

    /** Its pool left; 0 without one or while it is down. */
    double shield() {
        return shield;
    }

    /** Its maximum; 0 without one. */
    double max() {
        return max;
    }

    /** Its share left, 0 to 1; 0 without one. */
    double share() {
        return max > 0 ? shield / max : 0;
    }

    /** Whether its refill is held back now (a breaker's hit, the EMP Burst). */
    boolean held() {
        return hold > 0;
    }

    /**
     * One step of {@code spec}'s shield, on the real steps (an EMP stun holds the refill by its own hold): the hold runs
     * down, then the refill after the delay; a {@link SimEvents.Type#ENEMY_SHIELD_REFILLED} when it is full again at (x, y).
     */
    void step(EnemySpec.Shield spec, SimEvents events, double x, double y, int kind) {
        if (quiet > 0) {
            quiet--;
        }
        if (hold > 0) {
            hold--;
            return;
        }
        if (idle < Integer.MAX_VALUE) {
            idle++;
        }
        if (shield >= max || idle <= SimStep.ticks(spec.delaySeconds())) {
            return;
        }
        boolean wasDown = shield < max;
        shield = Math.min(max, shield + spec.refillShare() * max * SimStep.SECONDS);
        if (wasDown && shield >= max) {
            events.add(SimEvents.Type.ENEMY_SHIELD_REFILLED, x, y, kind);
        }
    }

    /**
     * Spends the pool on {@code amount} of damage; returns what is left for the hull. A hit is a {@link
     * SimEvents.Type#ENEMY_SHIELD_HIT} (spaced as an aura ring's absorb) and the pool emptied a {@link
     * SimEvents.Type#ENEMY_SHIELD_BROKEN}.
     */
    double spend(double amount, SimEvents events, double x, double y, int kind, int eventGap) {
        if (max <= 0) {
            return amount;
        }
        // A hit on a shield that is down still counts for the refill's delay.
        idle = 0;
        if (!(shield > 0)) {
            return amount;
        }
        double taken = Math.min(shield, amount);
        shield -= taken;
        if (shield <= 1e-9) {
            shield = 0;
            events.add(SimEvents.Type.ENEMY_SHIELD_BROKEN, x, y, kind);
        } else if (quiet == 0) {
            events.add(SimEvents.Type.ENEMY_SHIELD_HIT, x, y, kind);
            quiet = eventGap;
        }
        return amount - taken;
    }

    /**
     * A {@code shield-breaker} hit or the EMP Burst: the pool emptied at once and the refill held back {@code ticks}
     * steps; it refills from the hold's end unless a hit comes later (then its delay runs from that hit). Returns
     * whether it held anything.
     */
    boolean empty(EnemySpec.Shield spec, int ticks, SimEvents events, double x, double y, int kind) {
        if (max <= 0) {
            return false;
        }
        boolean had = shield > 0;
        shield = 0;
        idle = SimStep.ticks(spec.delaySeconds());
        hold = Math.max(hold, ticks);
        if (had) {
            events.add(SimEvents.Type.ENEMY_SHIELD_BROKEN, x, y, kind);
        }
        return had;
    }
}
