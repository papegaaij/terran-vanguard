package vanguard.content;

import vanguard.sim.Armament;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.RisePhase;
import vanguard.sim.Sortie;

/**
 * M8 part D (Level 24 Abyssal Plain): the autopilot's play against the Abyss Ray and the Barbfins (see {@link
 * Autopilot}). A Ray's shadow grows in the ship's lane ahead of it (design/enemies/naval/abyss-ray): with a front gun
 * that reaches the {@code sub} layer (the Harpoon Torpedoes) it holds its line under the shadow and its rise, so the
 * torpedoes hit it before it fans. A Barbfin holds above the ship and spits needles (design/enemies/naval/barbfin): the
 * lowest one on the screen above the ship is shot before the mute fry and the drifting jellies. The spawning reefs are
 * the escapes objective's ({@link WaterPlay#escapee}: low on the screen, before the Barbfins), the Glassfin rams {@link
 * WaterPlay#ramThreat}'s. No other level has
 * a rising unit or a hover above the ship, so nothing here changes another level's flight.
 */
final class AbyssPlay {
    private AbyssPlay() {}

    /**
     * A rising unit in its shadow or its rise, when a front gun reaches it there ({@code anti-sub}); null without one.
     */
    static Enemy shadow(Sortie sortie) {
        if (!frontReachesSub(sortie)) {
            return null;
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            RisePhase phase = enemy.risePhase();
            if (phase == RisePhase.SHADOW || phase == RisePhase.RISE) {
                return enemy;
            }
        }
        return null;
    }

    /**
     * The lowest unit holding (or coming down to hold) above the ship with a gun (the Barbfin's needles), above the
     * ship and on the screen; null without one.
     */
    static Enemy holder(Sortie sortie, double shipY) {
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (enemy.spec().hover().isPresent()
                    && enemy.spec().hover().get().aboveShip().isPresent()
                    && enemy.spec().gun().isPresent()
                    && y > shipY + 40
                    && y < PlayField.HEIGHT - 10
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = enemy;
            }
        }
        return lowest;
    }

    private static boolean frontReachesSub(Sortie sortie) {
        Armament armament = sortie.armament();
        for (int m = 0; m < armament.size(); m++) {
            Armament.Mount mount = armament.mount(m);
            if (mount.slot() == Armament.Slot.FRONT
                    && mount.weapon() != null
                    && mount.weapon().antiSub()) {
                return true;
            }
        }
        return false;
    }
}
