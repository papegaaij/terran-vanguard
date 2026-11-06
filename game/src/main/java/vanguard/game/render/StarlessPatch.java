package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import vanguard.content.Content;
import vanguard.sim.PlayField;

/**
 * M11 part D, step D4 (design/campaign Level 46, Layout: the starless patch; m11d-gaps.md §1 #7): a soft region about
 * {@value #DIAMETER} px across on the {@code deep} layer where no star or nebula shows, centred {@value #X} px from the
 * play field's left and {@value #BELOW_TOP} px below its top, from the level's start to its end. It is the only thing in
 * the level that does not scroll: it never moves, grows or shows anything. Drawn after the deep layer's tiles and pieces
 * and before everything over them, in the deep's base colour ({@link #BASE}, tools/art/act7_deadfleet.py's {@code
 * SPACE}) with a smooth radial alpha: opaque to {@value #INNER} px from its centre, easing to nothing at {@value #OUTER}
 * px (a smoothstep, far more than 32 steps: no dither, no visible ring), well inside its {@value #SIZE} px canvas.
 *
 * <p>No data key: the level's {@code backdrop} block names it only in a note (step D2), so the game knows it by the
 * level ({@link #of}; pending the user's review in round 91). The mask is a generated texture, made at the first draw
 * and freed with the backdrop.
 */
final class StarlessPatch implements Disposable {
    /** The level that has it. */
    static final int LEVEL = 46;
    /** Its centre on the play field: x px, and px below the top edge. */
    static final int X = 360;

    static final int BELOW_TOP = 150;
    /** Its half-alpha diameter, px (Level 46: "about 120 px across"). */
    static final int DIAMETER = 120;
    /** Opaque inside this radius, nothing past that one, px: the edge a smoothstep {@value #OUTER} - {@value #INNER} px wide. */
    static final double INNER = 36;

    static final double OUTER = 84;
    /** The mask's canvas, px (the edge fades to 0 inside it). */
    static final int SIZE = 176;
    /** The deep's base colour (near-black violet). */
    static final Color BASE = new Color(6 / 255f, 5 / 255f, 10 / 255f, 1);

    private Texture texture;

    private StarlessPatch() {}

    /** The patch for level {@code levelKey}; null for a level without one. */
    static StarlessPatch of(String levelKey) {
        return levelKey != null && Content.levelNumber(levelKey) == LEVEL ? new StarlessPatch() : null;
    }

    /** Its alpha {@code r} px from its centre: 1 to {@link #INNER}, a smoothstep to 0 at {@link #OUTER}. */
    static float alpha(double r) {
        double t = Math.clamp((OUTER - r) / (OUTER - INNER), 0, 1);
        return (float) (t * t * (3 - 2 * t));
    }

    /** The mask's alpha per pixel, 0-255, row by row ({@value #SIZE}²): what the texture holds (for the tests). */
    static int[] mask() {
        int[] mask = new int[SIZE * SIZE];
        double half = SIZE / 2.0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                mask[y * SIZE + x] = Math.round(255 * alpha(Math.hypot(x + 0.5 - half, y + 0.5 - half)));
            }
        }
        return mask;
    }

    /** Its centre's screen y (y up), px. */
    static float centreY() {
        return PlayField.HEIGHT - BELOW_TOP;
    }

    /** Draws it at its fixed place, over the deep layer (normal blending), whatever the scroll. */
    void draw(SpriteBatch batch) {
        if (texture == null) {
            texture = texture();
        }
        float before = batch.getPackedColor();
        batch.setColor(BASE);
        batch.draw(texture, X0 + X - SIZE / 2f, centreY() - SIZE / 2f, SIZE, SIZE);
        batch.setPackedColor(before);
    }

    private static Texture texture() {
        Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        int[] mask = mask();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pixmap.drawPixel(x, y, 0xFFFFFF00 | mask[y * SIZE + x]);
            }
        }
        Texture made = new Texture(pixmap);
        made.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return made;
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
