package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.sim.SimStep;

/**
 * M11 part H, step H4 (design/ui/hud: {@code THE GATE IS CLOSING}, Level 50 at K+10.5, the closing gate scene; step H2's
 * call, pending the user's review in round 95): the HUD has no event line, so the gate's closing reads as a <b>neutral
 * banner</b> in the warning banner's shape and slot ({@link WaveBanners}: the hostile band's place, its 2.5 s), its
 * frame and text the event plane's violet-white on a dark band: neither the hostile amber nor the friendly green. Like
 * the other banners it needs no data of its own: the scene's {@code GATE_CLOSING} opens it. Geometry in play-field
 * pixels, y up.
 */
public final class GateBanner {
    /** The banner's text. */
    static final String TEXT = "THE GATE IS CLOSING";

    private static final int TICKS = SimStep.ticks(WaveBanners.SECONDS);
    /** The event plane's violet-white on a dark violet band (TransitWarp's violet, lifted toward white). */
    static final WaveBanners.Palette VIOLET = new WaveBanners.Palette(
            new Color(16 / 255f, 10 / 255f, 30 / 255f, 225 / 255f),
            Color.valueOf("C9B8F2"),
            Color.valueOf("F1EBFF"),
            new Color(14 / 255f, 6 / 255f, 28 / 255f, 1));

    private final TextureRegion pixel;
    private final BitmapFont font;
    private final int centreY;
    /** The tick the banner opened, or -1 while none shows. */
    private long since = -1;

    /**
     * @param pixel a white pixel
     * @param font the text's font (the 10x20 body font)
     * @param airEscort whether the level has an air escort: the hostile band then sits higher
     */
    public GateBanner(TextureRegion pixel, BitmapFont font, boolean airEscort) {
        this.pixel = pixel;
        this.font = font;
        centreY = WaveBanners.centreY(airEscort);
    }

    /** The scene's gate closing at {@code tick}: the banner opens. */
    public void open(long tick) {
        since = tick;
    }

    /** No banner, as when the level restarts. */
    public void clear() {
        since = -1;
    }

    /** Whether it shows at {@code tick}. */
    boolean showing(long tick) {
        return since >= 0 && tick - since < TICKS;
    }

    /** Draws it at the tick being shown ({@code tick} plus {@code alpha} of a step). */
    public void draw(SpriteBatch batch, long tick, float alpha) {
        if (!showing(tick)) {
            return;
        }
        double seconds = (tick - since + alpha) * SimStep.SECONDS;
        float opacity = WaveBanners.opacity(seconds);
        if (opacity <= 0) {
            return;
        }
        float frame = opacity * EdgeWarnings.pulse(seconds);
        WaveBanners.drawBand(batch, pixel, font, TEXT, centreY, WaveBanners.grow(seconds), opacity, frame, VIOLET);
    }
}
