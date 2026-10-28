package vanguard.game.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.SplittableRandom;

/**
 * The transmission static over a portrait (design/ui/briefing, design/ui/hud): a burst of noise
 * that fades out as a transmission opens and fades in as it closes. The noise is one texture of
 * random pixels generated at start-up; each frame draws it from a random offset with a few brighter
 * rolling bands, so nothing is allocated while it plays.
 */
public final class TransmissionStatic implements Disposable {
    /** How long a burst lasts. */
    public static final float SECONDS = 0.35f;

    private static final int SIZE = 256;
    private static final int BANDS = 3;
    private static final int BAND_HEIGHT = 2;

    private final Texture noise;
    private final SplittableRandom random = new SplittableRandom(2185);

    public TransmissionStatic() {
        Pixmap pixels = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                float grey = 0.15f + 0.85f * random.nextFloat();
                pixels.drawPixel(x, y, rgba(0.85f * grey, 0.9f * grey, grey));
            }
        }
        noise = new Texture(pixels);
        noise.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        pixels.dispose();
    }

    /**
     * A random source offset for a span of the noise: inside the texture when the span fits, else
     * anywhere, the repeating texture tiling it (an intercepted briefing page's image is wider than
     * the noise).
     */
    private int offset(int span) {
        return random.nextInt(span < SIZE ? SIZE - span : SIZE);
    }

    private static int rgba(float r, float g, float b) {
        return Math.round(r * 255) << 24 | Math.round(g * 255) << 16 | Math.round(b * 255) << 8 | 0xFF;
    }

    /**
     * The static's strength, 0 (none) to 1 (only noise): the larger of the opening burst and the
     * closing one.
     *
     * @param sinceOpened seconds since the transmission opened
     * @param untilClosed seconds until it closes; infinite while that is not known yet
     */
    public static float strength(float sinceOpened, float untilClosed) {
        return Math.max(0, 1 - Math.min(sinceOpened, untilClosed) / SECONDS);
    }

    /** Draws the static at {@code strength} over a portrait whose bottom left corner is ({@code x}, {@code y}). */
    public void draw(Batch batch, float x, float y, int width, int height, float strength) {
        if (strength <= 0) {
            return;
        }
        float colour = batch.getPackedColor();
        batch.setColor(1, 1, 1, strength);
        batch.draw(noise, x, y, width, height, offset(width), offset(height), width, height, false, false);
        batch.setColor(1, 1, 1, Math.min(1, 1.6f * strength));
        for (int i = 0; i < BANDS; i++) {
            float bandY = y + random.nextInt(height - BAND_HEIGHT);
            batch.draw(
                    noise, x, bandY, width, BAND_HEIGHT, offset(width), random.nextInt(SIZE), width, 1, false, false);
        }
        batch.setPackedColor(colour);
    }

    /**
     * M9 part A (design/ui/briefing, Intercepted transmissions; design/ui/hud, Act 5 additions): the enemy channel's
     * steady interference, at about a third of the open and close bursts' strength for the whole transmission.
     */
    public static final float STEADY = 1 / 3f;
    /** The steady interference's slow rolling bands: their speeds, in the image's heights a second. */
    static final double[] ROLL_SPEEDS = {0.11, 0.17, 0.29};
    /** A tear every {@value #TEAR_EVERY} s give or take {@value #TEAR_SPREAD} s (2.5 to 4 s apart) for 0.1 s. */
    static final double TEAR_EVERY = 3.25;

    static final double TEAR_SPREAD = 0.375;
    static final double TEAR_SECONDS = 0.1;
    /** A tear's sideways shift, px, at most. */
    static final int TEAR_SHIFT = 6;

    private final TextureRegion torn = new TextureRegion();

    /** A number in [0, 1) for tear {@code k} and {@code salt}, the same every run. */
    static double hash(long k, int salt) {
        double u = Math.sin(k * 12.9898 + salt * 78.233) * 43758.5453;
        return u - Math.floor(u);
    }

    /** The tear due nearest {@code seconds}: its number. */
    static long tearNumber(double seconds) {
        return Math.round(seconds / TEAR_EVERY);
    }

    /** When tear {@code k} starts, s into the transmission (never in its first second). */
    static double tearStart(long k) {
        return k < 1 ? Double.NEGATIVE_INFINITY : k * TEAR_EVERY + TEAR_SPREAD * (2 * hash(k, 1) - 1);
    }

    /** Whether a tear shows at {@code seconds} into the transmission. */
    static boolean tearing(double seconds) {
        long k = tearNumber(seconds);
        double since = seconds - tearStart(k);
        return since >= 0 && since < TEAR_SECONDS;
    }

    /** The torn line's height, 2-4 px, of tear {@code k}. */
    static int tearHeight(long k) {
        return 2 + (int) (hash(k, 2) * 3);
    }

    /**
     * Draws the steady interference over {@code under} (the portrait or the page's image, its bottom left corner at
     * ({@code x}, {@code y}), {@code width} x {@code height}) {@code seconds} into the transmission: the noise at
     * {@code strength} (at least {@link #STEADY}), three slow bands rolling down it, and now and then a short tear, a
     * line of the image shifted sideways for 0.1 s.
     */
    public void drawSteady(
            Batch batch, TextureRegion under, float x, float y, int width, int height, float seconds, float strength) {
        float colour = batch.getPackedColor();
        float noiseStrength = Math.max(STEADY, strength);
        batch.setColor(1, 1, 1, noiseStrength);
        batch.draw(noise, x, y, width, height, offset(width), offset(height), width, height, false, false);
        batch.setColor(1, 1, 1, Math.min(1, 1.6f * noiseStrength));
        for (int i = 0; i < ROLL_SPEEDS.length; i++) {
            double roll = seconds * ROLL_SPEEDS[i] + i / 3.0;
            float bandY = y + height - BAND_HEIGHT - (float) ((roll - Math.floor(roll)) * (height - BAND_HEIGHT));
            batch.draw(
                    noise, x, bandY, width, BAND_HEIGHT, offset(width), random.nextInt(SIZE), width, 1, false, false);
        }
        batch.setPackedColor(colour);
        if (under != null && tearing(seconds)) {
            long k = tearNumber(seconds);
            int tear = Math.min(tearHeight(k), height);
            int row = (int) (hash(k, 3) * (height - tear));
            int shift = (int) Math.round((2 * hash(k, 4) - 1) * TEAR_SHIFT);
            int sourceWidth = Math.min(width, under.getRegionWidth());
            torn.setRegion(under, 0, row, sourceWidth, tear);
            batch.draw(torn, x + shift, y + height - row - tear, sourceWidth, tear);
        }
    }

    @Override
    public void dispose() {
        noise.dispose();
    }
}
