package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Round 42: soft shapes filled from the white pixel with a colour per corner, so they shade smoothly without art of
 * their own: a round glow fading out from its centre (the walker's local heat, a local death flash) and a sector
 * filled by slices (the walker's flame cone). Coordinates are the play field's (x from its left edge, y up); the
 * batch's blending applies. Allocates nothing per draw.
 */
final class VertexFills {
    /** A disc's slices around its centre. */
    static final int DISC_SLICES = 24;
    /** A disc's alpha at half its radius, as a share of its centre's (then fading to nothing at the rim). */
    static final float DISC_MID = 0.45f;

    private final TextureRegion pixel;
    private final float u;
    private final float v;
    private final float[] quad = new float[20];

    VertexFills(TextureRegion pixel) {
        this.pixel = pixel;
        u = (pixel.getU() + pixel.getU2()) / 2;
        v = (pixel.getV() + pixel.getV2()) / 2;
    }

    /** A quad through its four corners in order, each with its packed colour. */
    void quad(
            SpriteBatch batch,
            double x1,
            double y1,
            float c1,
            double x2,
            double y2,
            float c2,
            double x3,
            double y3,
            float c3,
            double x4,
            double y4,
            float c4) {
        corner(0, x1, y1, c1);
        corner(1, x2, y2, c2);
        corner(2, x3, y3, c3);
        corner(3, x4, y4, c4);
        batch.draw(pixel.getTexture(), quad, 0, quad.length);
    }

    private void corner(int k, double x, double y, float colour) {
        int at = k * 5;
        quad[at] = (float) (PlayFrame.X0 + x);
        quad[at + 1] = (float) y;
        quad[at + 2] = colour;
        quad[at + 3] = u;
        quad[at + 4] = v;
    }

    /**
     * A round glow at (x, y): {@code colour} at {@code alpha} in its centre, {@link #DISC_MID} of that at half its
     * {@code radius}, nothing at its rim.
     */
    void disc(SpriteBatch batch, double x, double y, double radius, Color colour, float alpha) {
        if (alpha <= 0 || radius <= 0) {
            return;
        }
        float centre = Color.toFloatBits(colour.r, colour.g, colour.b, Math.min(1, alpha));
        float mid = Color.toFloatBits(colour.r, colour.g, colour.b, Math.min(1, alpha * DISC_MID));
        float rim = Color.toFloatBits(colour.r, colour.g, colour.b, 0);
        double half = radius / 2;
        for (int i = 0; i < DISC_SLICES; i++) {
            double a0 = 2 * Math.PI * i / DISC_SLICES;
            double a1 = 2 * Math.PI * (i + 1) / DISC_SLICES;
            double c0 = Math.cos(a0);
            double s0 = Math.sin(a0);
            double c1 = Math.cos(a1);
            double s1 = Math.sin(a1);
            quad(
                    batch,
                    x,
                    y,
                    centre,
                    x,
                    y,
                    centre,
                    x + c1 * half,
                    y + s1 * half,
                    mid,
                    x + c0 * half,
                    y + s0 * half,
                    mid);
            quad(
                    batch,
                    x + c0 * half,
                    y + s0 * half,
                    mid,
                    x + c1 * half,
                    y + s1 * half,
                    mid,
                    x + c1 * radius,
                    y + s1 * radius,
                    rim,
                    x + c0 * radius,
                    y + s0 * radius,
                    rim);
        }
    }

    /** The colour of a sector's corner at a share {@code along} its length and {@code across} its width (−1–1). */
    interface Shade {
        float at(double along, double across);
    }

    /**
     * A sector from (x, y) along {@code axis} (radians clockwise from straight down, as the walker's cone), {@code
     * half} radians either side, from {@code from} to {@code length} px out, in {@code slices} × {@code rings} quads,
     * each corner coloured by {@code shade} (its share along from the apex, 0–1, and across, −1–1).
     */
    void sector(
            SpriteBatch batch,
            double x,
            double y,
            double axis,
            double half,
            double from,
            double length,
            int slices,
            int rings,
            Shade shade) {
        for (int r = 0; r < rings; r++) {
            double d0 = from + (length - from) * r / rings;
            double d1 = from + (length - from) * (r + 1) / rings;
            for (int s = 0; s < slices; s++) {
                double w0 = -1 + 2.0 * s / slices;
                double w1 = -1 + 2.0 * (s + 1) / slices;
                double a0 = axis + w0 * half;
                double a1 = axis + w1 * half;
                // Clockwise from down, y up: the direction is (−sin, −cos).
                quad(
                        batch,
                        x - Math.sin(a0) * d0,
                        y - Math.cos(a0) * d0,
                        shade.at(d0 / length, w0),
                        x - Math.sin(a1) * d0,
                        y - Math.cos(a1) * d0,
                        shade.at(d0 / length, w1),
                        x - Math.sin(a1) * d1,
                        y - Math.cos(a1) * d1,
                        shade.at(d1 / length, w1),
                        x - Math.sin(a0) * d1,
                        y - Math.cos(a0) * d1,
                        shade.at(d1 / length, w0));
            }
        }
    }
}
