package vanguard.game.render;

import java.util.Arrays;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part B: the Dust Kite's entry fade (design/enemies/air/dust-kite, Behaviour: it emerges from the dust
 * and fades to full contrast over {@value #SECONDS} s; a look only, it is hittable and collides from its
 * first frame). A unit that swoops fades in from the step it first shows on the screen, tracked per
 * {@link Enemy#serial()} (a ring of the last {@value #TRACKED} such units), so a unit fades in once. M7 part G
 * (design/campaign Level 20, Reverse scroll): a kite from ahead in a reversed section enters at the bottom edge, so a
 * unit shows once it is within the margin of either edge.
 */
final class KiteFade {
    /** The fade's length, s. */
    static final double SECONDS = 0.3;
    /** The units tracked at once (a level's kites on the screen together, with room to spare). */
    static final int TRACKED = 64;
    /**
     * A unit shows on the screen once its centre is this far beyond the top edge or less (M7 part G: or beyond the
     * bottom edge, in a reversed section), px.
     */
    private static final double TOP_MARGIN = 12;

    private final int[] serials = new int[TRACKED];
    /** The step each tracked unit first showed. */
    private final long[] shown = new long[TRACKED];

    private int next;

    KiteFade() {
        restart();
    }

    void restart() {
        Arrays.fill(serials, -1);
        next = 0;
    }

    /** After each simulation step: the swooping units that show on the screen for the first time. */
    void stepped(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().swoop().isPresent() && shows(enemy.renderY(1)) && find(enemy.serial()) < 0) {
                serials[next] = enemy.serial();
                shown[next] = sortie.tick();
                next = (next + 1) % TRACKED;
            }
        }
    }

    /** A unit's opacity at {@code alpha} of a step after {@code tick}: 1 for a unit that does not fade. */
    float opacity(Enemy enemy, long tick, float alpha) {
        if (enemy.spec().swoop().isEmpty()) {
            return 1;
        }
        int k = find(enemy.serial());
        if (k < 0) {
            return 0;
        }
        return opacity((tick - shown[k] + alpha) * SimStep.SECONDS);
    }

    /** Whether a unit at {@code y} (its centre, px up from the bottom edge) shows on the screen. */
    static boolean shows(double y) {
        return y < PlayField.HEIGHT + TOP_MARGIN && y > -TOP_MARGIN;
    }

    /** The opacity {@code seconds} after it first showed: 0 to 1 over the fade, eased. */
    static float opacity(double seconds) {
        float s = (float) Math.clamp(seconds / SECONDS, 0, 1);
        return s * s * (3 - 2 * s);
    }

    private int find(int serial) {
        for (int k = 0; k < TRACKED; k++) {
            if (serials[k] == serial) {
                return k;
            }
        }
        return -1;
    }
}
