package vanguard.game.render;

import java.util.Locale;
import vanguard.content.EuropaData;

/**
 * M8 part C (design/campaign Level 23, the waterline beat; gap check D10): the hydro-kit's tell as the ship drops into
 * the water. The game reads the medium's edge (no simulation event, gap check D5): on the step the sortie goes under
 * water the rules strip shows ({@link #ACTION} and {@link #rules}, one line for {@value #SECONDS} s through an {@link
 * EventPrompt}), the weapon rows' {@code NO WATER} clears with one flash ({@link Hud#waterline}); the refit flash is
 * {@link HydroLooks}', the crossing's and refit's sounds the flight's. Not on the first step of an attempt (a level or a
 * retry that starts under water).
 */
public final class WaterlineTell {
    /** The rules strip's seconds. */
    public static final double SECONDS = 4;
    /** The strip's first word. */
    public static final String ACTION = "UNDER WATER";

    private boolean underwater;
    private boolean started;

    /**
     * The strip's rules from the water's factors: {@code BULLETS ×0.7 · DRAG · TORPEDOES +50 %} (with {@code times} the
     * font's multiplication sign).
     */
    public static String rules(EuropaData.Underwater water, String times) {
        String bullets = String.format(Locale.ROOT, "%.1f", water.bulletSpeed());
        long torpedoes = Math.round((water.torpedoRange() - 1) * 100);
        return "BULLETS " + times + bullets + " · DRAG · TORPEDOES +" + torpedoes + " %";
    }

    /** After a step: whether the sortie just went under water ({@code underwater} now, not at the last step). */
    public boolean step(boolean underwater) {
        boolean into = started && underwater && !this.underwater;
        this.underwater = underwater;
        started = true;
        return into;
    }

    /** A new attempt: its first step is no crossing. */
    public void reset() {
        started = false;
    }
}
