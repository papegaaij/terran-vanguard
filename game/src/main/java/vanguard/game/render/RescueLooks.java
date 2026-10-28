package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.EscortSwap;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Wingman;

/**
 * M9 part B (design/campaign Level 29, the rescue; design/player/wingmen, Rook's rescue and return): the escort swap's
 * looks, a look only. The rescued Rook is not drawn while he waits in the prison tender's hold; at the release (the
 * last clamp gun's fall) the hold opens ({@link Backdrop#holdOpened}) and his Ember rises out of it ({@code
 * rook-launch_0..7}, tools/art/belt_units.py: growing from the hold's shadow into the sun, {@value #LAUNCH_FRAME_TICKS}
 * steps a frame) on his way into the slot, then he flies as ever. With no Rook to join ({@link EscortSwap#LEAVES}: an
 * {@code --escort none} run) the Ember rises the same and leaves upward off the screen at {@value #LEAVE_SPEED} px/s;
 * with Rook flying from the start ({@link EscortSwap#EMPTY}) the hold opens empty.
 */
final class RescueLooks {
    /** The launch's frames show 4 steps each (its 8 frames over 0.53 s). */
    static final int LAUNCH_FRAME_TICKS = 4;
    /** The Ember that has no slot to join leaves upward at this speed, px/s. */
    static final double LEAVE_SPEED = 320;
    /** The level banking frame of Rook's craft ({@code rook_2}), the one the launch hands over to. */
    static final int LEVEL_FRAME = 2;

    private final Array<AtlasRegion> launch;
    private final AtlasRegion level;

    RescueLooks(Sprites sprites) {
        launch = sprites.has("rook-launch") ? sprites.frames("rook-launch") : new Array<>();
        level = sprites.has("rook") ? sprites.frames("rook").get(LEVEL_FRAME) : null;
    }

    /** The launch's length, steps. */
    int launchTicks() {
        return launch.size * LAUNCH_FRAME_TICKS;
    }

    /** Whether the launch shows its frame {@code ticks} steps after the release; -1 after (or without its art). */
    static int launchFrame(int ticks, int frames) {
        int frame = ticks / LAUNCH_FRAME_TICKS;
        return ticks >= 0 && frame < frames ? frame : -1;
    }

    /** Whether Rook's own craft is not drawn now: he waits in the hold, or his launch frames stand in for it. */
    boolean hides(Sortie sortie, Wingman rook) {
        if (rook.waiting()) {
            return true;
        }
        EscortSwap swap = sortie.escortSwap();
        return rook.joining() && launchFrame(swap.ticksSinceRelease(), launch.size) >= 0;
    }

    /**
     * The Ember's launch from the hold: over the rescued Rook on his way into the slot, or (no Rook to join) at the
     * hold, then flying up and away.
     */
    void draw(SpriteBatch batch, Sortie sortie, Wingman rook, float alpha) {
        EscortSwap swap = sortie.escortSwap();
        if (!swap.planned() || !swap.released()) {
            return;
        }
        int ticks = swap.ticksSinceRelease();
        int frame = launchFrame(ticks, launch.size);
        if (swap.kind() == EscortSwap.JOINS) {
            if (rook != null && rook.joining() && frame >= 0) {
                drawCentred(batch, launch.get(frame), rook.renderX(alpha), rook.renderY(alpha));
            }
            return;
        }
        if (swap.kind() != EscortSwap.LEAVES) {
            return;
        }
        if (frame >= 0) {
            drawCentred(batch, launch.get(frame), swap.launchX(), swap.launchY());
            return;
        }
        if (level == null) {
            return;
        }
        double y = leftY(swap.launchY(), ticks - launchTicks() + alpha);
        if (y - level.getRegionHeight() / 2.0 < PlayField.HEIGHT) {
            drawCentred(batch, level, swap.launchX(), y);
        }
    }

    /** The leaving Ember's height {@code steps} steps after its launch ended at {@code fromY} (y up). */
    static double leftY(double fromY, double steps) {
        return fromY + LEAVE_SPEED * steps * SimStep.SECONDS;
    }
}
