package vanguard.game.databank;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.game.render.FlashShader;

/**
 * Step DB2 (design/ui/databank, Enemy entries at sensor none): a unit's shape without its look: the sprite's (or a
 * plate's) alpha in a flat dark navy, a lit one-pixel rim round it, and scan lines across it. Drawn at run time with the
 * flash shader (each draw the region in one flat colour, its alpha kept): the rim colour shifted a pixel each way, the
 * navy body over it, then every {@value #SCAN_STEP}th pixel row of the body a shade lighter. No file of its own; the
 * rows' regions are made once per picture, never per frame.
 */
final class Silhouette {
    /** The body's navy. */
    static final Color NAVY = new Color(14 / 255f, 22 / 255f, 58 / 255f, 1);
    /** The scan lines: a lighter navy. */
    static final Color SCAN = new Color(34 / 255f, 52 / 255f, 112 / 255f, 1);
    /** The lit rim: a pale cyan. */
    static final Color RIM = new Color(110 / 255f, 200 / 255f, 255 / 255f, 1);
    /** Every this many pixel rows of the source one is a scan line. */
    static final int SCAN_STEP = 3;

    private TextureRegion source;
    private TextureRegion[] rows = new TextureRegion[0];

    /**
     * Draws {@code region}'s silhouette with its top-left at ({@code x}, {@code top}) in GL coordinates' x and its bottom
     * at {@code bottom} (y up), {@code scale} times its size.
     */
    void draw(SpriteBatch batch, FlashShader flash, TextureRegion region, float x, float bottom, float scale) {
        if (region != source) {
            source = region;
            int height = region.getRegionHeight();
            rows = new TextureRegion[(height + SCAN_STEP - 1) / SCAN_STEP];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = new TextureRegion(region, 0, i * SCAN_STEP, region.getRegionWidth(), 1);
            }
        }
        float width = region.getRegionWidth() * scale;
        float height = region.getRegionHeight() * scale;
        // Each colour is a uniform: the draws before it are flushed first (the batch keeps one shader for all three).
        batch.flush();
        flash.begin(batch, RIM, 1);
        batch.draw(region, x - 1, bottom, width, height);
        batch.draw(region, x + 1, bottom, width, height);
        batch.draw(region, x, bottom - 1, width, height);
        batch.draw(region, x, bottom + 1, width, height);
        batch.flush();
        flash.begin(batch, NAVY, 1);
        batch.draw(region, x, bottom, width, height);
        batch.flush();
        flash.begin(batch, SCAN, 1);
        for (int i = 0; i < rows.length; i++) {
            // Row i * SCAN_STEP from the region's top.
            float rowBottom = bottom + height - (i * SCAN_STEP + 1) * scale;
            batch.draw(rows[i], x, rowBottom, width, scale);
        }
        flash.end(batch);
    }
}
