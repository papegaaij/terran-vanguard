package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * Step GFX2 (the round 43 capture's readability fixes): two generated white textures for the looks that need a light
 * at play scale where their art has none big enough: a soft disc (bright in its middle, fading to nothing at its rim)
 * and a thin soft ring. Drawn tinted, usually additive: the pursuit swarm's rim glow, tell glow and tell ring ({@link
 * PursuitLooks}), the rim guns' flak flash cores ({@link FlakFlashes}) and the emergency cache's strobe halo ({@link
 * StrobeHalo}). Built once per level renderer, freed with it.
 */
final class SoftGlow implements Disposable {
    /** The textures' size, px: drawn scaled to the size each look asks for. */
    static final int SIZE = 64;
    /** The ring's middle and half width, as shares of the texture's radius. */
    static final double RING_RADIUS = 0.82;

    static final double RING_HALF_WIDTH = 0.12;

    private final Texture discTexture = texture(false);
    private final Texture ringTexture = texture(true);
    private final TextureRegion disc = new TextureRegion(discTexture);
    private final TextureRegion ring = new TextureRegion(ringTexture);

    /** The disc's alpha {@code d} of its radius from its middle (0..1): a smooth bell, 1 in the middle, 0 at the rim. */
    static float discAlpha(double d) {
        double a = Math.max(0, 1 - d * d);
        return (float) (a * a);
    }

    /** The ring's alpha {@code d} of its radius from its middle: a soft band round {@link #RING_RADIUS}. */
    static float ringAlpha(double d) {
        double t = Math.abs(d - RING_RADIUS) / RING_HALF_WIDTH;
        return t >= 1 ? 0 : (float) ((1 - t * t) * (1 - t * t));
    }

    /** The disc {@code diameter} px across centred on the play-field point (x, y), at the batch's colour. */
    void disc(SpriteBatch batch, double x, double y, double diameter) {
        draw(batch, disc, x, y, diameter);
    }

    /** The ring, its middle line {@code radius} px from the play-field point (x, y), at the batch's colour. */
    void ring(SpriteBatch batch, double x, double y, double radius) {
        draw(batch, ring, x, y, 2 * radius / RING_RADIUS);
    }

    /** Sets the batch's colour to {@code colour} at {@code alpha}. */
    static void tint(SpriteBatch batch, Color colour, float alpha) {
        batch.setColor(colour.r, colour.g, colour.b, alpha);
    }

    private static void draw(SpriteBatch batch, TextureRegion region, double x, double y, double size) {
        float s = (float) size;
        batch.draw(region, (float) (X0 + x - size / 2), (float) (y - size / 2), s, s);
    }

    private static Texture texture(boolean ring) {
        Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        double half = SIZE / 2.0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double d = Math.hypot(x + 0.5 - half, y + 0.5 - half) / half;
                float a = d >= 1 ? 0 : ring ? ringAlpha(d) : discAlpha(d);
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, a));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        discTexture.dispose();
        ringTexture.dispose();
    }
}
