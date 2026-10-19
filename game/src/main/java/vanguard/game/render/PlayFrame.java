package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.sim.SetPiece;

/**
 * What the play field's passes share in one drawn frame ({@link LevelRenderer} sets it before the
 * first pass): the strength of the white hit flashes and what the beam burns, plus the drawing
 * helpers every pass uses. One instance per renderer, so nothing is allocated per frame.
 */
final class PlayFrame {
    static final float X0 = PixelScreen.PLAY_FIELD_X;
    /**
     * Loot targets (design/art-direction, readability rule 7): a white hit flash for two steps, a
     * glint about every 2 s (each target at its own phase), and the secret's beacon blinks at 1 Hz.
     */
    static final int HIT_FLASH_TICKS = 2;

    static final Color HIT_WHITE = Color.WHITE;

    /** The white flashes' strength: 1, or less with the Gameplay tab's flash reduction on. */
    float whiteFlash = 1;
    /**
     * M7 part A: what a beam burns in the drawn frame, for its steady heat tint: an enemy's serial (or
     * another {@link ArsenalLooks#heatTarget}), the set piece and part, the ground object's x (NaN).
     */
    int heatTarget = -1;

    SetPiece heatPiece;
    int heatPart = -1;
    double groundHeatX = Double.NaN;

    static void drawScaled(SpriteBatch batch, TextureRegion region, double x, double y, float scale) {
        float width = region.getRegionWidth();
        float height = region.getRegionHeight();
        batch.draw(
                region,
                Math.round(X0 + x - width / 2),
                Math.round(y - height / 2),
                width / 2,
                height / 2,
                width,
                height,
                scale,
                scale,
                0);
    }

    static void drawCentred(SpriteBatch batch, TextureRegion region, double x, double y) {
        batch.draw(
                region,
                Math.round(X0 + x - region.getRegionWidth() / 2.0),
                Math.round(y - region.getRegionHeight() / 2.0));
    }
}
