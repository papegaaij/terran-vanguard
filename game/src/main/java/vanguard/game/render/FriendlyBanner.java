package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.game.ui.Glass;
import vanguard.sim.SimStep;

/**
 * Step MARK70 (design/campaign Level 29, Friendly marker; round 70 B.1 d, the rendering friendly-marker-r70-d): the
 * banner "FRIENDLIES INBOUND — HOLD FIRE" in the warning banner's shape ({@link WaveBanners}) in CDF green, opened as
 * the simulation announces a wave of neutral craft ({@code Sortie.friendliesInbound()}, 2 s before it enters) and shown
 * for the warning banner's 2.5 s. It has its <b>own slot</b> just under the hostile band, so it is stacked under an
 * amber banner showing at the same moment (Level 29's pincers at t=77 and t=97) and never waits for one: the two are
 * read together, the hostile on top. Like the warning banner it needs no data of its own: the level's neutral waves name
 * it. Geometry in play-field pixels, y up.
 */
public final class FriendlyBanner {
    /** The banner's text. */
    static final String TEXT = "FRIENDLIES INBOUND — HOLD FIRE";
    /** Its middle under the hostile band's: a band's height and a 6 px gap lower. */
    static final int BELOW = WaveBanners.HEIGHT + 6;

    private static final int TICKS = SimStep.ticks(WaveBanners.SECONDS);
    private static final WaveBanners.Palette GREEN = new WaveBanners.Palette(
            new Color(6 / 255f, 34 / 255f, 16 / 255f, 225 / 255f),
            Glass.GREEN,
            Color.valueOf("C8FFD2"),
            new Color(0, 20 / 255f, 8 / 255f, 1));

    private final TextureRegion pixel;
    private final BitmapFont font;
    private final int centreY;
    /** The tick the banner opened, or -1 while none shows. */
    private long since = -1;
    /** Whether a neutral wave was announced at the step before. */
    private boolean announced;

    /**
     * @param pixel a white pixel
     * @param font the text's font (the 10x20 body font)
     * @param airEscort whether the level has an air escort: the hostile band, and this one under it, then sit higher
     */
    public FriendlyBanner(TextureRegion pixel, BitmapFont font, boolean airEscort) {
        this.pixel = pixel;
        this.font = font;
        centreY = centreY(airEscort);
    }

    /** The band's middle above the play field's bottom edge, px: under the hostile band's. */
    static int centreY(boolean airEscort) {
        return WaveBanners.centreY(airEscort) - BELOW;
    }

    /**
     * Follows the simulation after a step: the banner opens as an announcement starts.
     *
     * @param inbound whether a neutral wave is announced now ({@code Sortie.friendliesInbound()})
     */
    public void step(boolean inbound, long tick) {
        if (since >= 0 && tick - since >= TICKS) {
            since = -1;
        }
        if (inbound && !announced) {
            since = tick;
        }
        announced = inbound;
    }

    /** No banner, as when the level restarts. */
    public void clear() {
        since = -1;
        announced = false;
    }

    /** Whether it shows now. */
    boolean showing() {
        return since >= 0;
    }

    /** Draws it at the tick being shown ({@code tick} plus {@code alpha} of a step). */
    public void draw(SpriteBatch batch, long tick, float alpha) {
        if (since < 0) {
            return;
        }
        double seconds = (tick - since + alpha) * SimStep.SECONDS;
        float opacity = WaveBanners.opacity(seconds);
        if (opacity <= 0) {
            return;
        }
        float frame = opacity * EdgeWarnings.pulse(seconds);
        WaveBanners.drawBand(batch, pixel, font, TEXT, centreY, WaveBanners.grow(seconds), opacity, frame, GREEN);
    }
}
