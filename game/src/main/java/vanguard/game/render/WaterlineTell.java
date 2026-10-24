package vanguard.game.render;

import java.util.Locale;
import vanguard.content.EuropaData;
import vanguard.sim.PlayField;

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
     * The strip's rules from the water's factors: {@code BULLETS ×0.7 · BEAMS ×0.2 · TORPEDOES +50 %} (with {@code
     * times} the font's multiplication sign). Round 56 Q.1 (the user, 2026-10-24: yes): the energy weapons' damage is
     * named too; to keep the strip on one line inside the play field ({@link #fits}) the {@code DRAG} it named before
     * gave way (the drag is felt at once).
     */
    public static String rules(EuropaData.Underwater water, String times) {
        String bullets = String.format(Locale.ROOT, "%.1f", water.bulletSpeed());
        String beams = String.format(Locale.ROOT, "%.1f", water.energyDamage());
        long torpedoes = Math.round((water.torpedoRange() - 1) * 100);
        return "BULLETS " + times + bullets + " · BEAMS " + times + beams + " · TORPEDOES +" + torpedoes + " %";
    }

    /**
     * Whether the strip's box ({@link EventPrompt}: the action, a gap and the rules, inset at both ends), {@code
     * actionWidth} and {@code rulesWidth} px of text, keeps at least {@code margin} px from the play field's edges.
     */
    public static boolean fits(int actionWidth, int rulesWidth, int margin) {
        return EventPrompt.width(actionWidth + EventPrompt.GAP + rulesWidth) + 2 * margin <= PlayField.WIDTH;
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
