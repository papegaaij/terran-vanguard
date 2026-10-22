package vanguard.sim;

import java.util.List;

/**
 * M8 part C (design/enemies/naval/eel-swarm, Attack): a chain head's attacks ({@link EnemySpec.ChainSpec#headAttacks()}),
 * one clock each, one set per {@link Chain} (kept out of its body). Each clock runs while the wave's head lives, is not
 * doomed nor stunned and is on the play field: it starts at the attack's first-shot delay, raises the attack's tell
 * ({@link SimEvents.Type#EYE_GLINT} at the head, value its kind: the stripe running to the head) its {@link
 * EnemyGun#tellSeconds()} before each volley, fires the volley (an aimed shot or a fan) at the ship and starts again at the interval. Fixed
 * arrays, no allocation; hashed only for a chain that has head attacks.
 */
final class HeadAttacks {
    private final int[] ticks = new int[EnemySpec.ChainSpec.MOST_HEAD_ATTACKS];
    private int count;

    /** A wave's chain with {@code attacks}: each clock at its first-shot delay. */
    void start(List<EnemyGun> attacks) {
        count = attacks.size();
        for (int a = 0; a < count; a++) {
            // + 1: as a chain head's gun, the clock already counts down in the step it first is on the screen.
            ticks[a] = SimStep.ticks(attacks.get(a).firstShotDelay()) + 1;
        }
    }

    /** None (a rear part, a chain without head attacks). */
    void clear() {
        count = 0;
    }

    /**
     * One step of {@code chain}'s head attacks: counts each clock down while the wave's head can fire, raises the tells
     * and fires the volleys that are due when {@code firing}.
     */
    void step(Chain chain, Ship ship, boolean firing, EnemyFire guns, SimEvents events) {
        Enemy head = chain.member(0);
        if (count == 0
                || head == null
                || !chain.original()
                || chain.empStunned()
                || Chain.doomed(head)
                || !PlayField.overlaps(head.x(), head.y(), head.hitbox())) {
            return;
        }
        List<EnemyGun> attacks = chain.spec().headAttacks();
        for (int a = 0; a < count; a++) {
            EnemyGun gun = attacks.get(a);
            int left = --ticks[a];
            if (left > 0) {
                if (gun.tellSeconds() > 0 && left == SimStep.ticks(gun.tellSeconds())) {
                    events.add(SimEvents.Type.EYE_GLINT, head.x(), head.y(), head.kind());
                }
                continue;
            }
            ticks[a] = Math.max(1, SimStep.ticks(gun.intervalSeconds()));
            if (firing) {
                guns.fire(head.x(), head.y(), gun, false, 0, ship, -1, false);
            }
        }
    }

    /** Steps until attack {@code a}'s next volley (while the head is on the screen). */
    int ticksLeft(int a) {
        return ticks[a];
    }

    /** Hashed only for a chain with head attacks. */
    void addTo(StateHash hash) {
        for (int a = 0; a < count; a++) {
            hash.add(ticks[a]);
        }
    }
}
