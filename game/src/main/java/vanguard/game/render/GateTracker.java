package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import vanguard.game.ui.Fonts;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3b (design/ui/hud, Act 6 additions: the pylon count; design/enemies/ground/shield-pylon, The pylon
 * gates; Level 41): the objective line of a level with several held gates, a helper of {@link MissionPanel}. While a
 * gate's halt runs it reads {@code PYLONS n / m} (the gate's pylons destroyed of its own total); as its last pylon falls
 * the count flashes green and reads {@code OPEN}; the next gate's count starts at {@code 0 / m} when its halt begins.
 * After the count (or the label alone between gates) a row of small gate pips, one per gate, green once that gate is
 * open.
 */
final class GateTracker {
    static final String PYLONS = "PYLONS";
    static final String OPEN = "OPEN";
    /** A gate's pip: this wide, this far apart, this tall. */
    static final int PIP_WIDTH = 8;

    static final int PIP_STEP = 11;
    static final int PIP_HEIGHT = 10;
    /** The count flashes for this many frames after a gate opens, on and off every {@value #FLASH_PHASE}. */
    static final int FLASH_FRAMES = 90;

    static final int FLASH_PHASE = 8;
    static final Color OPEN_GREEN = Color.valueOf("40FF80");
    static final Color SHUT = Color.valueOf("3A4060");

    private final HudKit kit;
    /** The frame the current gate opened at (-1 while it stands), and the gate it was. */
    private int openedFrame = -1;

    private int openedGate = -1;

    GateTracker(HudKit kit) {
        this.kit = kit;
    }

    /** The count's text: {@code n / m}, or {@code OPEN} once the gate's last pylon fell. */
    static String count(int destroyed, int total) {
        return destroyed >= total ? OPEN : destroyed + " / " + total;
    }

    /** Whether gate {@code g} is open: one before the current, or the current with no holder standing. */
    static boolean open(int g, int current, int standing) {
        return g < current || (g == current && standing == 0);
    }

    /** Whether the count shows: while a gate's halt runs, or for its flash after it opened. */
    static boolean counting(boolean halting, int standing, int sinceOpened) {
        return (halting && standing > 0) || (sinceOpened >= 0 && sinceOpened < FLASH_FRAMES);
    }

    /** The level restarts: no gate opened. */
    void restart() {
        openedFrame = -1;
        openedGate = -1;
    }

    /** The line at baseline {@code y} in a well from {@code x}, {@code width} wide, at panel frame {@code frame}. */
    void draw(SpriteBatch batch, Sortie sortie, int x, int y, int width, int frame) {
        int gates = sortie.gates();
        int gate = sortie.gate();
        int standing = sortie.holdersStanding();
        if (standing == 0 && openedGate != gate) {
            openedGate = gate;
            openedFrame = frame;
        } else if (standing > 0 && openedGate == gate) {
            // A restart or the boss checkpoint: the gate stands again.
            restart();
        }
        int since = openedGate == gate && openedFrame >= 0 ? frame - openedFrame : -1;
        boolean flash = since >= 0 && since < FLASH_FRAMES && since / FLASH_PHASE % 2 == 0;
        kit.text(batch, kit.body, PYLONS, HudKit.LABEL, x, y, width);
        int pipsRight = x + width;
        if (counting(sortie.halting(), standing, since)) {
            int total = sortie.holders();
            String text = count(total - standing, total);
            kit.textRight(batch, kit.body, text, flash || standing == 0 ? OPEN_GREEN : HudKit.AMBER, x, y, width);
            pipsRight = x + width - Fonts.width(kit.body, text) - 6;
        }
        int left = pipsRight - gates * PIP_STEP + (PIP_STEP - PIP_WIDTH);
        for (int g = 0; g < gates; g++) {
            Color pip = open(g, gate, standing) ? OPEN_GREEN : SHUT;
            kit.fill(batch, pip, left + g * PIP_STEP, y - PIP_HEIGHT, PIP_WIDTH, PIP_HEIGHT);
        }
    }
}
