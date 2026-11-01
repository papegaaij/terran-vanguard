package vanguard.content;

import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M10 part D, step D3 (design/campaign Level 38 Lightning Belt; see {@link Autopilot}): the storm layer's lines. The
 * level's Hornets make it a hunted level, so its moves are {@link VentPlay}'s (every move planned among the bullets,
 * the missiles and the bodies, each flown on along its last step: a ram's straight line in its last moments and a
 * ring's bullets are flown round as they come) with {@link DivePlay#strikes}'s step out of a told column on top; its
 * line is this play's. It takes a Harrow while it still chases, at
 * range, before its tell (the nearest one above the ship); the Harrows from the side and rear edges below it are the
 * homing guns' (the Swarm Tail, the pods). It lines up under the CDF supply canister to shoot its latch (its brackets
 * mark a friendly drop, not a craft to spare) and under Aurelia's cargo pod to open it. Only a level with a unit whose
 * dash rams out through the far edge (the Harrow, Level 38 on) takes these plays, so nothing here moves another
 * level's flight.
 */
final class StormPlay {
    private StormPlay() {}

    /** Whether the level flies a unit whose dash rams out (the Harrow): the plays below apply. */
    static boolean storm(Sortie sortie) {
        for (int i = 0; i < sortie.enemyKinds().size(); i++) {
            if (rams(sortie.enemyKinds().get(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean rams(EnemySpec kind) {
        return kind.dash().isPresent() && kind.dash().get().variant().after() == EnemySpec.Dash.After.OUT;
    }

    /**
     * The Harrow to take now: the nearest one above the ship ({@code shipY} + 40) and on the screen that still chases
     * (neither in its tell nor in its ram), so it dies at range and its ring has the field to spread in; null for none.
     */
    static Enemy harrow(Sortie sortie, double shipY) {
        Enemy best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        double shipX = sortie.ship().x();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!rams(enemy.spec())
                    || enemy.dashTelling()
                    || enemy.dashing()
                    || enemy.cloaked()
                    || !enemy.layer().touchable()
                    || enemy.hp() <= 0) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (y <= shipY + 40 || y >= PlayField.HEIGHT - 10 || x < 0 || x > PlayField.WIDTH) {
                continue;
            }
            double distance = Math.hypot(x - shipX, y - shipY);
            if (distance < bestDistance) {
                best = enemy;
                bestDistance = distance;
            }
        }
        return best;
    }

    /**
     * The debris chunk to line up under now, in a storm level: the lowest above the ship and on the screen of the
     * chunks with a drop (the CDF canister's latch) or a secret (Aurelia's cargo pod); null for none.
     */
    static Debris chunk(Sortie sortie, double shipY) {
        if (!storm(sortie)) {
            return null;
        }
        Debris best = null;
        for (int i = 0; i < sortie.debrisCount(); i++) {
            Debris chunk = sortie.debris(i);
            if (chunk.spec().drop().isEmpty() && !chunk.spec().holdsSecret()) {
                continue;
            }
            double y = chunk.renderY(1);
            if (y <= shipY + 40 || y >= PlayField.HEIGHT - 10) {
                continue;
            }
            if (best == null || y < best.renderY(1)) {
                best = chunk;
            }
        }
        return best;
    }
}
