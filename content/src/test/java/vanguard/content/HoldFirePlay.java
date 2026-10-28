package vanguard.content;

import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M9 part B (design/campaign Level 29, the "Hold fire" secondary; see {@link Autopilot}): neutral craft (the wavering
 * Ceres Wing Stormhawks) are never lined up under, the ship keeps its x out of the band below each one, and it holds
 * its fire while one is in that band above it (the beam and the wing pods' missiles leave straight up before they
 * seek). No level before Level 29 has a neutral craft, so nothing here moves another level's flights.
 */
final class HoldFirePlay {
    /**
     * Half the band below a neutral craft it keeps its guns out of, px beyond the craft's half hitbox: the wing pods'
     * mounts and their missiles' first straight stretch (the beam itself is 12 px wide at L4).
     */
    static final double BAND = 34;

    private HoldFirePlay() {}

    /** Whether a unit is a neutral craft: never a target. */
    static boolean neutral(Enemy enemy) {
        return enemy.spec().neutral();
    }

    /** Whether a neutral craft on the screen is above the ship within its band of the ship's x: no shot then. */
    static boolean holdFire(Sortie sortie) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (neutral(enemy) && above(enemy, shipY) && Math.abs(enemy.renderX(1) - shipX) < half(enemy)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code targetX} moved out of the band below every neutral craft above the ship, to the band's nearer side (one
     * pass: two crafts' bands side by side push it on through the second).
     */
    static double clearX(Sortie sortie, double targetX) {
        double shipY = sortie.ship().y();
        double x = targetX;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!neutral(enemy) || !above(enemy, shipY)) {
                continue;
            }
            double dx = x - enemy.renderX(1);
            double half = half(enemy) + 2;
            if (Math.abs(dx) < half) {
                double left = enemy.renderX(1) - half;
                double right = enemy.renderX(1) + half;
                boolean goLeft = dx < 0 ? left > 20 : right > PlayField.WIDTH - 20;
                x = goLeft ? left : right;
            }
        }
        return x;
    }

    private static boolean above(Enemy enemy, double shipY) {
        double y = enemy.renderY(1);
        return y > shipY && y < PlayField.HEIGHT + enemy.spec().hitbox().height() / 2;
    }

    private static double half(Enemy enemy) {
        return enemy.spec().hitbox().width() / 2 + BAND;
    }
}
