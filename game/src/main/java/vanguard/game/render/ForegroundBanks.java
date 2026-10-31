package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M10 part A, step A3b (design/world/jovian, Hazards &amp; set pieces: foreground cloud banks, Q2; round 72 A.12 a "the
 * cream veil"): the level's {@code backdrop.banks}, translucent banks on the {@code high-air} scenery drawn over the
 * {@code air} bodies, owned by {@link BackdropAtmosphere}. Each bank is its authored rectangle ({@code w} × {@code h} px,
 * centred on {@code x}, passing the middle of the screen at {@code t} and drifting down with the {@code high-air}
 * factor, as content's {@code JovianHazardChecks} places it), filled with one smooth veil in the band's own lit cream
 * (bright on top, a warmer cream below; {@link #veilAlpha}: soft billowed edges, no ordered dither, drawn once) at
 * {@value #PEAK} at its densest, so a body under it reads at about 35 % and fades toward cream. Clouds at different
 * altitudes share one colour (the user, round 72). A lightning flash makes the banks see-through ({@link #draw}'s {@code
 * seeThrough}). {@link #cover} tells how much a point is under them (the units' glows drawn again at full over them).
 * M10 part B, step B4: a bank with a {@code piece} (Level 36's bank-a..f, ART-B's images of exactly its rectangle, the
 * veil at most {@value #PEAK} in them) is drawn as that image instead of the code veil ({@link #pieces}); its cover
 * keeps the veil's shape (the images' soft edges match it closely). Nothing is allocated per frame; a level without
 * banks draws nothing.
 */
final class ForegroundBanks implements Disposable {
    /** The veil's opacity at its densest: a body under it reads at 1 − this (design: ≈ 35 %). */
    static final float PEAK = 0.65f;
    /** The veil texture's size, px (drawn stretched to each bank's rectangle). */
    static final int SIZE = 128;
    /** The share of each side the veil's edge takes to fade in. */
    static final double EDGE = 0.28;
    /** The cream ramp: the bank's shadowed underside, its middle and its sunlit top (tools/concept/act6_r72.py, a). */
    static final Color LOW = new Color(196 / 255f, 160 / 255f, 120 / 255f, 1);

    static final Color MID = new Color(236 / 255f, 214 / 255f, 178 / 255f, 1);
    static final Color HIGH = new Color(1, 248 / 255f, 230 / 255f, 1);

    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    /** The banks a difficulty draws, the ground's scroll at a level time, the {@code high-air} layer's factor. */
    private final Function<Difficulty, List<BackdropData.Bank>> banksOn;

    private final DoubleUnaryOperator scrollAt;
    private final double factor;
    private List<BackdropData.Bank> banks;
    /** Per bank: its rectangle's centre and half sizes this frame ({@link #update}). */
    private double[] centreX = new double[0];

    private double[] centreY = new double[0];
    private double[] halfW = new double[0];
    private double[] halfH = new double[0];
    /** Per bank: the ground scroll at which its centre passes the middle of the screen. */
    private double[] passAt = new double[0];

    private int shown;
    /** The veil's opacity at its densest now (0 before, or while a flash sees through it entirely). */
    private float opacity;

    private Texture texture;
    private TextureRegion veil;
    /** M10 part B: the image each bank is drawn as ({@code piece}), null for the code veil; per {@link #banks}. */
    private AtlasRegion[] images = new AtlasRegion[0];
    /** M10 part B: the backdrop atlas's bank images by piece name (null when it has none). */
    private Function<String, AtlasRegion> pieceImages = name -> null;

    ForegroundBanks(LevelData level) {
        this(level.backdrop()::banksOn, level::scrollAt, level.backdrop().factor(BackdropLayer.HIGH_AIR));
    }

    ForegroundBanks(
            Function<Difficulty, List<BackdropData.Bank>> banksOn, DoubleUnaryOperator scrollAt, double factor) {
        this.banksOn = banksOn;
        this.scrollAt = scrollAt;
        this.factor = factor;
        difficulty(Difficulty.MEDIUM);
    }

    /** Whether the level has foreground banks at all. */
    static boolean any(LevelData level) {
        return level.backdrop().banks().isPresent();
    }

    /** M10 part B: the bank images by their piece's name (the backdrop atlas's), null where the atlas has none. */
    void pieces(Function<String, AtlasRegion> images) {
        pieceImages = images;
        placeImages();
    }

    private void placeImages() {
        images = new AtlasRegion[banks.size()];
        for (int b = 0; b < images.length; b++) {
            images[b] = banks.get(b).piece().map(pieceImages).orElse(null);
        }
    }

    /** M10 part B: whether bank {@code b} (of the difficulty's) is drawn as its piece's image. */
    boolean drawnAsPiece(int b) {
        return images[b] != null;
    }

    /** The banks the difficulty draws (easy leaves some out). */
    void difficulty(Difficulty difficulty) {
        banks = banksOn.apply(difficulty);
        placeImages();
        int n = banks.size();
        centreX = new double[n];
        centreY = new double[n];
        halfW = new double[n];
        halfH = new double[n];
        passAt = new double[n];
        for (int b = 0; b < n; b++) {
            BackdropData.Bank bank = banks.get(b);
            centreX[b] = bank.x();
            halfW[b] = bank.w() / 2;
            halfH[b] = bank.h() / 2;
            passAt[b] = scrollAt.applyAsDouble(bank.t());
        }
        shown = 0;
    }

    /**
     * Places the banks for the ground scrolled by {@code groundScroll} px (each centre {@code HEIGHT / 2} above the
     * bottom when the ground has scrolled to its pass, the {@code high-air} factor faster after), seen through by {@code
     * seeThrough}: a lightning flash's reveal, 0 none, 1 the banks gone; their veil at {@value #PEAK} × (1 − it).
     */
    void update(double groundScroll, float seeThrough) {
        shown = banks.size();
        opacity = shown == 0 ? 0 : PEAK * (1 - Math.clamp(seeThrough, 0, 1));
        for (int b = 0; b < shown; b++) {
            centreY[b] = centreY(passAt[b], groundScroll, factor);
        }
    }

    /** A bank's centre's height over the play field's bottom: the middle at its pass, drifting down with the factor. */
    static double centreY(double passAt, double groundScroll, double factor) {
        return PlayField.HEIGHT / 2.0 + factor * (passAt - groundScroll);
    }

    /** The banks as {@link #update} placed them, normal blend. */
    void draw(SpriteBatch batch) {
        if (opacity <= 0) {
            return;
        }
        if (veil == null) {
            Pixmap pixels = pixmap();
            texture = new Texture(pixels);
            pixels.dispose();
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            veil = new TextureRegion(texture);
        }
        batch.setColor(1, 1, 1, opacity);
        for (int b = 0; b < shown; b++) {
            if (!onField(b)) {
                continue;
            }
            if (images[b] != null) {
                // M10 part B: the bank's image carries its veil (at most PEAK): drawn at the flash's share of it.
                batch.setColor(1, 1, 1, opacity / PEAK);
                batch.draw(
                        images[b],
                        (float) (X0 + centreX[b] - halfW[b]),
                        (float) (centreY[b] - halfH[b]),
                        (float) (2 * halfW[b]),
                        (float) (2 * halfH[b]));
                batch.setColor(1, 1, 1, opacity);
                continue;
            }
            batch.draw(
                    veil,
                    (float) (X0 + centreX[b] - halfW[b]),
                    (float) (centreY[b] - halfH[b]),
                    (float) (2 * halfW[b]),
                    (float) (2 * halfH[b]));
        }
        batch.setColor(Color.WHITE);
    }

    private boolean onField(int b) {
        return centreY[b] + halfH[b] > 0 && centreY[b] - halfH[b] < PlayField.HEIGHT;
    }

    /**
     * How much the play-field point (x, y) is under the banks as {@link #update} placed them, 0..{@value #PEAK}: the densest bank's veil
     * there (the share of a body's light the banks took, drawn back over them).
     */
    float cover(double x, double y) {
        float most = 0;
        if (opacity <= 0) {
            return 0;
        }
        for (int b = 0; b < shown; b++) {
            double u = (x - centreX[b] + halfW[b]) / (2 * halfW[b]);
            double v = (y - centreY[b] + halfH[b]) / (2 * halfH[b]);
            if (u > 0 && u < 1 && v > 0 && v < 1) {
                most = Math.max(most, opacity * veilAlpha(u, v));
            }
        }
        return most;
    }

    /** Whether any bank lies on the play field now. */
    boolean showing() {
        for (int b = 0; b < shown; b++) {
            if (onField(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The veil's alpha at (u, v) of its rectangle (0..1 each, v up): 1 inside, easing out over the outer {@link #EDGE} of
     * each side, the edge billowed (a few slow waves along it) so the bank reads as cloud, not a box; smooth.
     */
    static float veilAlpha(double u, double v) {
        double billowX = 0.06 * Math.sin(v * 9.4 + 0.7) + 0.035 * Math.sin(v * 21.0 + 2.1);
        double billowY = 0.06 * Math.sin(u * 8.2 + 1.3) + 0.035 * Math.sin(u * 19.0 + 0.4);
        double edgeX = Math.min(u, 1 - u) + billowX * Math.min(u, 1 - u) / 0.5;
        double edgeY = Math.min(v, 1 - v) + billowY * Math.min(v, 1 - v) / 0.5;
        return (float) (smooth(edgeX / EDGE) * smooth(edgeY / EDGE));
    }

    /** The veil's cream at (u, v): the warm underside below, the lit top above, a little lighter in the billows. */
    static void shade(double u, double v, Color out) {
        double s = Math.clamp(0.35 + 0.55 * v + 0.08 * Math.sin(u * 11.0 + v * 7.0), 0, 1);
        if (s < 0.5) {
            out.set(LOW).lerp(MID, (float) (s / 0.5));
        } else {
            out.set(MID).lerp(HIGH, (float) ((s - 0.5) / 0.5));
        }
    }

    private static double smooth(double p) {
        double c = Math.clamp(p, 0, 1);
        return c * c * (3 - 2 * c);
    }

    /** The veil's texture: {@link #shade}'s cream at {@link #veilAlpha}, straight alpha. */
    static Pixmap pixmap() {
        Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
        Color colour = new Color();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double u = (x + 0.5) / SIZE;
                double v = 1 - (y + 0.5) / SIZE;
                shade(u, v, colour);
                colour.a = veilAlpha(u, v);
                pixmap.drawPixel(x, y, Color.rgba8888(colour));
            }
        }
        return pixmap;
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
            veil = null;
        }
    }
}
