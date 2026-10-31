package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * M10 part B, step B4 (round 73 A.3 b, "soot under the flash"; tools/concept/explosion_bright_r73.py is the look): a
 * soft, normal-blended soot puff under a kill's additive flash, so the flash has a darker ground to light over Act 6's
 * bright cream bands (the round-72 worry: a small burst over the cream lifted it only +34..+55, a pale smudge). A warm
 * dark grey at most {@value #PEAK} opaque, a smooth radial falloff feathered to nothing over its outer 60 % with a
 * low-frequency lumpiness and no edge or ring (one generated texture, drawn once, no dither), coming in with the flash
 * (under its peak), growing from 0.9 to 1.35 of the burst's size and rising {@value #RISE} px as it fades out by 1.3 x
 * the flash's life.
 *
 * <p>Its scope is Act 6 ({@link #FIRST_LEVEL} on), from step B4's measure over Acts 1-5's darkest and brightest base
 * tiles (the medium and small bursts; the soot alone where the flash adds nothing): on the darkest (Level 29's outer
 * docks, black) it lifts its ring +12 luminance and on the night and space fields (Levels 01, 08, 26, 30) +5..+9, a
 * grey halo round every kill there, and darkens mid-tone fields (Level 06's terminator, 80) by 7; it earns its keep only
 * on bright fields (Level 22's ice -31, the Jovian cream). Pending the user's review in round 74.
 */
final class SootPuffs implements Disposable {
    /** The first level whose kills leave the soot (Act 6, the Jovian bands). */
    static final int FIRST_LEVEL = 36;
    /** The soot's opacity at its densest. */
    static final float PEAK = 0.42f;
    /** The warm dark grey. */
    static final Color SOOT = new Color(58 / 255f, 48 / 255f, 42 / 255f, 1);
    /** How long it lasts against its flash's life. */
    static final double LIFE = 1.3;
    /** It comes in over this share of its flash's life (under the flash's peak). */
    static final double IN = 0.15;
    /** Its size against the burst's at the start and at its end. */
    static final double GROW_FROM = 0.9;

    static final double GROW_TO = 1.35;
    /** How far it rises over its life, px. */
    static final double RISE = 6;
    /** The texture's size, px (drawn scaled). */
    static final int SIZE = 64;
    /**
     * The lumps reach in to this share of the texture's radius at the least, so the mask is nothing at its rim; the
     * texture is drawn this much larger again ({@link #DRAWN}), so the puff keeps the concept's mean size.
     */
    static final double LUMP_LEAST = 0.7;

    static final double DRAWN = 0.85 / LUMP_LEAST;

    private static final int CAPACITY = 32;

    private final long[] start = new long[CAPACITY];
    private final int[] life = new int[CAPACITY];
    private final double[] x = new double[CAPACITY];
    /** The play-field y, or for a puff on the ground the y plus the ground's scroll at its start. */
    private final double[] y = new double[CAPACITY];

    private final boolean[] onGround = new boolean[CAPACITY];
    private final float[] size = new float[CAPACITY];
    private int next;

    private Texture texture;
    private TextureRegion puff;

    SootPuffs() {
        restart();
    }

    /** Whether a level's kills leave the soot ({@link #FIRST_LEVEL} on). */
    static boolean in(int level) {
        return level >= FIRST_LEVEL;
    }

    /** The level restarts: no puffs. */
    void restart() {
        java.util.Arrays.fill(start, Long.MIN_VALUE / 2);
    }

    /**
     * A kill's flash starts at step {@code tick}: its soot, {@code side} px across like the burst, for {@code flashTicks}
     * steps of flash; on the ground at the ground's scroll {@code scroll}.
     */
    void start(long tick, double x, double y, int side, int flashTicks, boolean ground, double scroll) {
        if (side <= 0 || flashTicks <= 0) {
            return;
        }
        int p = next;
        next = (next + 1) % CAPACITY;
        start[p] = tick;
        life[p] = (int) Math.round(flashTicks * LIFE);
        this.x[p] = x;
        this.y[p] = ground ? y + scroll : y;
        onGround[p] = ground;
        size[p] = side;
    }

    /** Its opacity {@code share} (0..1) into its life: in under the flash's peak, out over its second half. */
    static float opacity(double share) {
        if (share < 0 || share >= 1) {
            return 0;
        }
        double in = Math.min(1, share * LIFE / IN);
        double out = 1 - Math.max(0, (share - 0.5) / 0.5);
        return (float) (PEAK * in * out);
    }

    /** The puffs at step {@code tick} (+ {@code alpha}), normal blend, the ground scrolled by {@code scroll}. */
    void draw(SpriteBatch batch, long tick, float alpha, double scroll) {
        boolean any = false;
        for (int p = 0; p < CAPACITY; p++) {
            double share = (tick - start[p] + alpha) / life[p];
            float opacity = life[p] > 0 ? opacity(share) : 0;
            if (opacity <= 0) {
                continue;
            }
            if (!any) {
                any = true;
                if (puff == null) {
                    texture = texture();
                    puff = new TextureRegion(texture);
                }
            }
            double d = DRAWN * size[p] * (GROW_FROM + (GROW_TO - GROW_FROM) * share);
            double py = (onGround[p] ? y[p] - scroll : y[p]) + RISE * share;
            batch.setColor(SOOT.r, SOOT.g, SOOT.b, opacity);
            float s = (float) d;
            batch.draw(puff, (float) (X0 + x[p] - d / 2), (float) (py - d / 2), s, s);
        }
        if (any) {
            batch.setColor(Color.WHITE);
        }
    }

    /** The mask's alpha at (u, v) of its square (0..1 each): feathered over the outer 60 %, lumpy, no edge. */
    static float maskAlpha(double u, double v) {
        double dx = u - 0.5;
        double dy = v - 0.5;
        double r = Math.hypot(dx, dy) / 0.5;
        double angle = Math.atan2(dy, dx);
        // a low-frequency lumpiness: a few slow waves round it (the concept's value noise, smooth and seamless)
        double lump =
                0.5 + 0.25 * Math.sin(3 * angle + 0.7) + 0.15 * Math.sin(5 * angle + 2.3) + 0.1 * Math.sin(2 * angle);
        r *= (0.85 + 0.30 * (lump - 0.5)) / LUMP_LEAST;
        double t = Math.clamp((1 - r) / 0.6, 0, 1);
        return (float) (t * t * (3 - 2 * t));
    }

    private static Texture texture() {
        Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, maskAlpha((x + 0.5) / SIZE, (y + 0.5) / SIZE)));
            }
        }
        Texture made = new Texture(pixmap);
        made.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return made;
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
            puff = null;
        }
    }
}
