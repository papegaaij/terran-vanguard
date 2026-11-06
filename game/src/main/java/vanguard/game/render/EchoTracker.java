package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import vanguard.game.ui.Fonts;
import vanguard.sim.BossFights;
import vanguard.sim.Sortie;

/**
 * M11 part A, step A3b (design/ui/hud, Act 7 additions: the echo counter; design/enemies/bosses, Boss rush; Level 49):
 * the objective line of a boss rush, a helper of {@link MissionPanel}. It reads {@code ECHO n / 3}, n the fight now (the
 * gate halted last, {@link BossFights#fight()}; 1 before the first), and ends in a small pip per fight, lit in the
 * spectral tint as each fight's boss dies. Between two fights (the last one's boss dead, the next one's gate not yet
 * halted) the count flashes and the next pip blinks at 2 Hz until the next fight's warning.
 */
final class EchoTracker {
    static final String ECHO = "ECHO";
    /** The spectral tint (design/ui/hud: a pale violet-white): the lit pips, the rush's boss bars. */
    static final Color SPECTRAL = Color.valueOf("E0D4FF");
    /** The blink's period, steps (2 Hz: lit for the first half). */
    static final int BLINK_TICKS = 30;

    private final HudKit kit;

    EchoTracker(HudKit kit) {
        this.kit = kit;
    }

    /** The line's text: {@code ECHO n / m} for fight {@code fight} (0 first; -1 before the first) of {@code fights}. */
    static String count(int fight, int fights) {
        return ECHO + " " + (Math.max(0, fight) + 1) + " / " + fights;
    }

    /** Whether the line is between two fights: fight {@code fight}'s boss dead and a fight after it. */
    static boolean between(int fight, int fights, boolean dead) {
        return fight >= 0 && fight < fights - 1 && dead;
    }

    /** Whether the blink is lit on step {@code tick}. */
    static boolean lit(long tick) {
        return Math.floorMod(tick, BLINK_TICKS) < BLINK_TICKS / 2;
    }

    /** The line at baseline {@code y} in a well from {@code x}, {@code width} wide. */
    void draw(SpriteBatch batch, Sortie sortie, int x, int y, int width) {
        BossFights rush = sortie.bosses();
        int fights = rush.fights();
        int fight = rush.fight();
        boolean between = between(
                fight,
                fights,
                fight >= 0 && sortie.setPiece(rush.fightPiece(fight)).destroyed());
        boolean lit = lit(sortie.tick());
        String text = count(fight, fights);
        kit.text(batch, kit.body, text, between && !lit ? HudKit.LABEL : HudKit.READOUT, x, y, width);
        int left = x + width - fights * GateTracker.PIP_STEP + (GateTracker.PIP_STEP - GateTracker.PIP_WIDTH);
        left = Math.max(left, x + Fonts.width(kit.body, text) + 6);
        for (int f = 0; f < fights; f++) {
            boolean dead = sortie.setPiece(rush.fightPiece(f)).destroyed();
            boolean next = between && f == fight + 1;
            Color pip = dead ? SPECTRAL : next && lit ? HudKit.AMBER : GateTracker.SHUT;
            kit.fill(
                    batch,
                    pip,
                    left + f * GateTracker.PIP_STEP,
                    y - GateTracker.PIP_HEIGHT,
                    GateTracker.PIP_WIDTH,
                    GateTracker.PIP_HEIGHT);
        }
    }
}
