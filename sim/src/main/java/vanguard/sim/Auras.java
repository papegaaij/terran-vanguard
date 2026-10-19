package vanguard.sim;

/**
 * M7 part D, the Choir Herald's {@code aura} (design/enemies/air/choir-herald; M8 = a): one step of every
 * Herald's shield rings over the units in flight, after their moves. A Herald sings while it is on the play
 * field and the EMP Burst does not stun it; while it is stunned the rings it feeds wait. Every unit its aura
 * reaches ({@link Enemy#inAura}) keeps its ring fed or, without a feeder that still reaches it, is claimed by
 * it ({@link EnemySpec.Aura}: fill, refill, keep and fade); the rings of a Herald that died collapse ({@link
 * EnemyForce#heraldGone}). The rings absorb damage in {@link Enemy#damage} (every damage path goes through it).
 * Default pending round 40: on every layer on the field but buried and under the water. Allocates nothing.
 */
final class Auras {
    private Auras() {}

    /** One step of the rings of every Herald in {@code enemies}. */
    static void step(Pool<Enemy> enemies) {
        int size = enemies.size();
        for (int i = 0; i < size; i++) {
            enemies.get(i).auraRing().scratch();
        }
        // The rings the Heralds fed already: kept fed where they still reach, waiting where their Herald is stunned.
        for (int h = 0; h < size; h++) {
            Enemy herald = enemies.get(h);
            if (herald.spec().aura().isEmpty()) {
                continue;
            }
            boolean stunned = herald.empStunned();
            boolean singing = herald.singing();
            double radius = herald.spec().aura().get().radius();
            for (int i = 0; i < size; i++) {
                Enemy unit = enemies.get(i);
                if (unit.ringFeeder() != herald.serial()) {
                    continue;
                }
                if (stunned) {
                    unit.auraRing().pause();
                } else if (singing && unit.inAura(herald, radius)) {
                    unit.auraRing().feed();
                }
            }
        }
        // The units a singing Herald reaches that no feeder holds now are its from this step.
        for (int h = 0; h < size; h++) {
            Enemy herald = enemies.get(h);
            if (!herald.singing()) {
                continue;
            }
            double radius = herald.spec().aura().get().radius();
            for (int i = 0; i < size; i++) {
                Enemy unit = enemies.get(i);
                if (!unit.auraRing().fedNow() && !unit.auraRing().pausedNow() && unit.inAura(herald, radius)) {
                    unit.claimRing(herald);
                }
            }
        }
        for (int i = 0; i < size; i++) {
            Enemy unit = enemies.get(i);
            if (unit.ringFeeder() >= 0) {
                unit.auraRing().step();
            }
        }
    }
}
