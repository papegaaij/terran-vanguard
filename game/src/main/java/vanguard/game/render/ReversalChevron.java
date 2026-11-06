package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import vanguard.sim.HazardSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M11 part A, step A3a (design/ui/hud, Act 7 additions: the reversal chevron; design/enemies, Distorted space): a
 * reversal told {@code told} s ahead shows a <b>double chevron</b>, two {@value #WIDTH} × {@value #HEIGHT} px
 * arrowheads {@value #GAP} px apart, at the middle of the play field's top and bottom edges, pointing the way the scroll
 * is about to run (up: the reversed scroll carries the field upward), in the event plane's violet-white on a dark
 * outline (neither the hostile warnings' amber nor Level 12's gust streaks), pulsing at {@value #PULSE_HZ} Hz ({@link
 * Show#TELL}). While the scroll eases through zero and runs reversed one dim chevron stays at the edge the scroll now
 * comes from, the bottom ({@link Show#DIM}); as it eases back it flashes once and goes ({@link Show#FLASH}, {@value
 * #FLASH_SECONDS} s). A function of the level's current and its clock ({@link #show}, {@link #opacity}); the chevron is
 * a texture generated once in smooth float alpha. Allocates nothing per frame.
 */
final class ReversalChevron implements Disposable {
    /** One arrowhead's size, px, its stroke, and the gap between the two. */
    static final int WIDTH = 24;

    static final int HEIGHT = 10;
    static final double STROKE = 3;
    static final int GAP = 3;
    /** The canvas round an arrowhead: its dark outline and the soft edge, px. */
    static final int PAD = 3;
    /** The outline's width past the stroke, px. */
    static final double OUTLINE = 1.2;
    /** The chevrons' inset from the field's edge, px. */
    static final int INSET = 6;
    /** The tell's pulse, Hz, and its opacity range. */
    static final double PULSE_HZ = 2;

    static final float PULSE_LOW = 0.4f;
    /** The dim chevron's opacity while the scroll runs reversed. */
    static final float DIM = 0.35f;
    /** The flash as the scroll eases back, s. */
    static final double FLASH_SECONDS = 0.3;
    /** The flash's phase at its start: sin(π × it) is about the dim chevron's opacity. */
    static final double FLASH_FROM = 0.114;
    /** A reversal's ease into and out of its reversed run, s (the simulation's {@code Current}: Level 20's 1 s). */
    static final double EASE_SECONDS = 1;
    /** The event plane's violet-white, and the dark outline. */
    static final Color FILL = Color.valueOf("ECE2FF");

    static final Color EDGE = Color.valueOf("160A26");

    /** What the chevron shows. */
    enum Show {
        NONE,
        TELL,
        DIM,
        FLASH
    }

    private Texture texture;

    /** What a level with {@code current} (null: none) shows at {@code seconds} on its clock. */
    static Show show(HazardSpec.Current current, double seconds) {
        if (current == null) {
            return Show.NONE;
        }
        for (int i = 0; i < current.reversals().size(); i++) {
            HazardSpec.Reversal reversal = current.reversals().get(i);
            double t = reversal.t();
            double back = t + EASE_SECONDS + reversal.runSeconds();
            if (seconds >= t - current.toldSeconds() && seconds < t) {
                return Show.TELL;
            }
            if (seconds >= t && seconds < back) {
                return Show.DIM;
            }
            if (seconds >= back && seconds < back + FLASH_SECONDS) {
                return Show.FLASH;
            }
        }
        return Show.NONE;
    }

    /** The chevron's opacity at {@code seconds} (as {@link #show}): the tell's 2 Hz pulse, dim, the one flash. */
    static float opacity(HazardSpec.Current current, double seconds) {
        Show show = show(current, seconds);
        if (show == Show.NONE) {
            return 0;
        }
        if (show == Show.DIM) {
            return DIM;
        }
        for (int i = 0; i < current.reversals().size(); i++) {
            HazardSpec.Reversal reversal = current.reversals().get(i);
            double t = reversal.t();
            if (show == Show.TELL && seconds >= t - current.toldSeconds() && seconds < t) {
                double age = seconds - (t - current.toldSeconds());
                return (float) (PULSE_LOW + (1 - PULSE_LOW) * (0.5 + 0.5 * Math.cos(2 * Math.PI * PULSE_HZ * age)));
            }
            double back = t + EASE_SECONDS + reversal.runSeconds();
            if (show == Show.FLASH && seconds >= back && seconds < back + FLASH_SECONDS) {
                double age = (seconds - back) / FLASH_SECONDS;
                // From the dim chevron up to full in its first ~40 %, then out to nothing.
                return (float) Math.sin(Math.PI * (FLASH_FROM + (1 - FLASH_FROM) * age));
            }
        }
        return 0;
    }

    /** Over the field with the warnings: the chevron the level's current shows now. */
    void draw(SpriteBatch batch, Sortie sortie) {
        if (sortie.current() == null) {
            return;
        }
        HazardSpec.Current current = sortie.script().hazards().current().orElse(null);
        double seconds = sortie.levelSeconds();
        Show show = show(current, seconds);
        if (show == Show.NONE) {
            return;
        }
        if (texture == null) {
            texture = texture();
        }
        float opacity = opacity(current, seconds);
        batch.setColor(1, 1, 1, opacity);
        float x = PlayFrame.X0 + PlayField.WIDTH / 2f - texture.getWidth() / 2f;
        float bottom = INSET - PAD;
        if (show == Show.TELL) {
            float top = PlayField.HEIGHT - INSET - PAD - (2 * HEIGHT + GAP);
            batch.draw(texture, x, top);
            batch.draw(texture, x, top + HEIGHT + GAP);
            batch.draw(texture, x, bottom);
            batch.draw(texture, x, bottom + HEIGHT + GAP);
        } else {
            batch.draw(texture, x, bottom);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * One arrowhead pointing up, rows from the top: the violet-white stroke over its dark outline, premultiplied into
     * straight alpha ({@link #coverage}); 0 on the canvas's border.
     */
    static float[][] pixels() {
        int width = WIDTH + 2 * PAD;
        int height = HEIGHT + 2 * PAD;
        float[][] rgba = new float[width * height][];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                double x = col + 0.5 - PAD;
                double y = row + 0.5 - PAD;
                double d = distance(x, y);
                double fill = coverage(d, STROKE / 2);
                double edge = coverage(d, STROKE / 2 + OUTLINE);
                double a = Math.max(fill, edge * 0.9);
                double mix = a <= 0 ? 0 : fill / a;
                rgba[row * width + col] = new float[] {
                    (float) (EDGE.r + (FILL.r - EDGE.r) * mix),
                    (float) (EDGE.g + (FILL.g - EDGE.g) * mix),
                    (float) (EDGE.b + (FILL.b - EDGE.b) * mix),
                    (float) a
                };
            }
        }
        return rgba;
    }

    /** A soft edge: 1 within {@code radius} px of the stroke's middle line, 0 past it plus a pixel. */
    static double coverage(double distance, double radius) {
        return Math.clamp(radius - distance + 0.5, 0, 1);
    }

    /** Distance, px, from (x, y) (y down) to the arrowhead's middle line: apex at the top middle, arms to the foot. */
    static double distance(double x, double y) {
        double inner = STROKE / 2;
        double apexX = WIDTH / 2.0;
        double apexY = inner;
        double footY = HEIGHT - inner;
        return Math.min(segment(x, y, apexX, apexY, inner, footY), segment(x, y, apexX, apexY, WIDTH - inner, footY));
    }

    private static double segment(double x, double y, double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double t = Math.clamp(((x - ax) * dx + (y - ay) * dy) / (dx * dx + dy * dy), 0, 1);
        return Math.hypot(x - (ax + t * dx), y - (ay + t * dy));
    }

    private static Texture texture() {
        int width = WIDTH + 2 * PAD;
        int height = HEIGHT + 2 * PAD;
        float[][] rgba = pixels();
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                float[] p = rgba[row * width + col];
                pixmap.drawPixel(col, row, Color.rgba8888(p[0], p[1], p[2], p[3]));
            }
        }
        Texture made = new Texture(pixmap);
        pixmap.dispose();
        return made;
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
