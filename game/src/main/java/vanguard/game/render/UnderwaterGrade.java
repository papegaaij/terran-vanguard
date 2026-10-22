package vanguard.game.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock.UnderwaterLook;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.Wingman;

/**
 * M8 part C (design/world/europa, Palette and lighting; round 48's grade a, lamp-lit to black): the under-water render
 * grade of a level whose sections run under water (Level 23 from its waterline). A light map in a frame buffer (Level
 * 06's darkness is the pattern, {@link FarsideLooks#darken}) multiplied over what is drawn up to the ship: the ground,
 * its units, the flyers, Rook and the ship; the bullets, the glows (drawn after it, {@link LevelRenderer}), the bursts
 * and the HUD stay ungraded. Its base is the section's {@code underwater_look} ({@link LevelData#underwaterLookOf}: the
 * ambient times the tint), eased over {@value #LOOK_EASE} s into the next section's (Level 24 darkens section by
 * section), blended by the medium's share ({@link Sortie#waterBlend}). Over the base the light: the animated caustics
 * (two wrapping layers of a generated ripple net, riding the ground and drifting on the real clock), the hydro-kit's
 * twin headlight cones ahead of the ship, a soft pool round the ship and Rook (so the hulls read) and round each of the
 * player's shots and each pickup (so they keep their light).
 *
 * <p><b>The waterline wipe</b>: while a waterline's row ({@link #waterlineRows}: the ground row the ship's start height
 * passes at the waterline's time, the row the backdrop's waterline band lies on) is on the screen, the field above it
 * takes the medium after the waterline (Level 23: graded, "the water ahead") and the field below it the medium's share
 * now (dry until the crossing, easing to graded over the water's 1 s); then the whole field. The marine snow ({@link
 * #drawSnow}) is drawn over the high-air layer on the real clock, as many motes as the look's {@code snow}, only where
 * the field is graded. Nothing is allocated per frame.
 */
final class UnderwaterGrade implements Disposable {
    /** The section's look eases into the next one's over this long from its start, s. */
    static final double LOOK_EASE = 2;
    /** The ship's start height: a waterline's row is the ground row it passes then (sim {@code Ship.START_Y}). */
    static final double SHIP_ROW = 96;
    /** The caustics' share of the light at their look's 1 (a soft net over the floor, never a full white). */
    static final float CAUSTIC_GAIN = 0.35f;
    /** The caustics' ridges: where a sum of waves is within this of zero (the net's line width). */
    static final double RIDGE = 0.7;
    /** The headlights' light at their look's 1: soft cones at about 58 % (L26's hard cone is part F's). */
    static final float HEADLIGHT_GAIN = 0.58f;
    /** The cones: their length, px, their half-angle, their toe-out, and the lamps' places on the hull (dx, dy). */
    static final int CONE_LENGTH = 200;

    static final double CONE_HALF_ANGLE = Math.toRadians(17);
    static final float CONE_TOE = 5;
    static final float LAMP_DX = 8;
    static final float LAMP_DY = 14;
    /** A pool round the ship, Rook, a shot and a pickup: radius px and strength. */
    static final float SHIP_POOL = 44;

    static final float SHIP_POOL_LIGHT = 0.45f;
    static final float ROOK_POOL = 38;
    static final float ROOK_POOL_LIGHT = 0.4f;
    static final float SHOT_POOL = 10;
    static final float SHOT_POOL_LIGHT = 0.9f;
    static final float PICKUP_POOL = 20;
    static final float PICKUP_POOL_LIGHT = 0.7f;
    /** The caustics' tile, px, and their layers' drifts, px/s on the real clock. */
    static final int CAUSTIC_TILE = 128;

    static final float DRIFT_A_X = 7;
    static final float DRIFT_A_Y = -5;
    static final float DRIFT_B_X = -5;
    static final float DRIFT_B_Y = -9;
    /** Marine snow: the most motes, their sinking, px/s, their sway and their light (at most this). */
    static final int MOST_MOTES = 160;

    static final double SINK_MIN = 4;
    static final double SINK_MAX = 14;
    static final double SWAY = 3;
    static final float MOTE_LIGHT = 0.5f;
    /** The motes hang between the camera and the floor: they pass at this share of the ground's scroll. */
    static final double MOTE_PARALLAX = 1.2;

    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    private static final int WIDTH = PlayField.WIDTH;
    private static final int HEIGHT = PlayField.HEIGHT;
    private static final int MARGIN = 8;
    private static final Color HEADLIGHT = new Color(1f, 0.96f, 0.86f, 1);
    private static final Color CAUSTIC = new Color(0.75f, 1f, 0.95f, 1);
    private static final Color PICKUP = new Color(1f, 0.85f, 0.55f, 1);
    private static final Color MOTE = new Color(0.82f, 0.95f, 0.93f, 1);

    /** Per section its look (empty: the air) and its start. */
    private final List<Optional<UnderwaterLook>> looks;

    private final double[] starts;
    /** Per waterline its ground row and whether the medium after it is under water. */
    private final double[] rows;

    private final boolean[] intoWater;
    /** Per waterline its level time. */
    private final double[] times;

    private final TextureRegion pixel;
    private final FrameBuffer lightMap;
    private final Texture radialTexture;
    private final TextureRegion radial;
    private final Texture coneTexture;
    private final Texture causticTexture;
    private final Matrix4 lightProjection = new Matrix4();
    private final Matrix4 saved = new Matrix4();
    private final java.nio.IntBuffer boundFramebuffer = com.badlogic.gdx.utils.BufferUtils.newIntBuffer(16);
    private final java.nio.IntBuffer boundViewport = com.badlogic.gdx.utils.BufferUtils.newIntBuffer(16);
    /** The look now (refreshed per frame, no allocation): ambient, tint r g b, caustics, snow, headlights. */
    private final float[] now = new float[7];

    private final float[] moteX = new float[MOST_MOTES];
    private final float[] moteY = new float[MOST_MOTES];
    private final float[] moteSink = new float[MOST_MOTES];
    private final float[] motePhase = new float[MOST_MOTES];
    private final float[] moteLight = new float[MOST_MOTES];
    /** This frame's wipe: the waterline's screen y (NaN: none on the screen), the strength above and below it. */
    private double wipeY = Double.NaN;

    private float above;
    private float below;
    private final double[] wipeOut = new double[4];
    /** The look above a waterline on the screen (the medium after it); {@link #now} below it and without one. */
    private final float[] nowAbove = new float[7];

    private UnderwaterGrade(LevelData level, TextureRegion pixel) {
        this.pixel = pixel;
        looks = new ArrayList<>();
        starts = new double[level.sections().size()];
        for (int i = 0; i < level.sections().size(); i++) {
            looks.add(level.underwaterLookOf(i));
            starts[i] = level.sectionStart(i);
        }
        List<Double> waterlines = level.waterlines();
        rows = new double[waterlines.size()];
        times = new double[waterlines.size()];
        intoWater = new boolean[waterlines.size()];
        for (int w = 0; w < rows.length; w++) {
            double t = waterlines.get(w);
            times[w] = t;
            rows[w] = waterlineRow(level, t);
            intoWater[w] = level.mediumOf(level.sectionAt(t) - 1) == LevelData.Medium.UNDERWATER;
        }
        lightMap = new FrameBuffer(Pixmap.Format.RGBA8888, WIDTH, HEIGHT, false);
        lightProjection.setToOrtho2D(0, 0, WIDTH, HEIGHT);
        radialTexture = radialTexture();
        radial = new TextureRegion(radialTexture);
        coneTexture = coneTexture();
        causticTexture = causticTexture();
        SplittableRandom random = new SplittableRandom(2300);
        for (int i = 0; i < MOST_MOTES; i++) {
            moteX[i] = (float) random.nextDouble(WIDTH + 2 * MARGIN);
            moteY[i] = (float) random.nextDouble(HEIGHT + 2 * MARGIN);
            double depth = random.nextDouble();
            moteSink[i] = (float) (SINK_MIN + (SINK_MAX - SINK_MIN) * depth);
            motePhase[i] = (float) random.nextDouble(Math.PI * 2);
            moteLight[i] = (float) (MOTE_LIGHT * (0.4 + 0.6 * depth));
        }
    }

    /** The level's grade, or null for a level with no section under water with a look. */
    static UnderwaterGrade of(LevelData level, TextureRegion pixel) {
        return hasLook(level) ? new UnderwaterGrade(level, pixel) : null;
    }

    /** Whether any section of the level has an under-water look. */
    static boolean hasLook(LevelData level) {
        for (int i = 0; i < level.sections().size(); i++) {
            if (level.underwaterLookOf(i).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The ground row of a waterline at level time {@code t}: the row the ship's start height passes then (the backdrop's
     * waterline band, tools/art/backdrop_l23.py: ground row 2,646 for Level 23's 25.5 s).
     */
    static double waterlineRow(LevelData level, double t) {
        return level.scrollAt(t) + SHIP_ROW;
    }

    /** The ground rows of the level's waterlines. */
    double[] waterlineRows() {
        return rows.clone();
    }

    /**
     * The look at level time {@code seconds} into {@code out} (ambient, tint r, g, b, caustics, snow, headlights): its
     * section's, eased from the section before over {@value #LOOK_EASE} s; a section in the air (no look) is the unlit
     * scene (ambient 1, white, no light of its own), so a waterline's wipe and the medium's share do the change.
     */
    static void lookAt(List<Optional<UnderwaterLook>> looks, double[] starts, double seconds, float[] out) {
        int i = 0;
        while (i + 1 < starts.length && seconds >= starts[i + 1]) {
            i++;
        }
        put(looks.get(i), out, 0, 1);
        double into = seconds - starts[i];
        if (i > 0
                && into < LOOK_EASE
                && looks.get(i).isPresent()
                && looks.get(i - 1).isPresent()) {
            double u = into / LOOK_EASE;
            u = u * u * (3 - 2 * u);
            put(looks.get(i - 1), out, 0, (float) (1 - u));
            put(looks.get(i), out, 1, (float) u);
        }
    }

    /** Writes (or, {@code add} 1, adds) {@code weight} of {@code look} into {@code out}; the air is the unlit scene. */
    private static void put(Optional<UnderwaterLook> look, float[] out, int add, float weight) {
        UnderwaterLook l = look.orElse(null);
        int tint = l == null ? 0xffffff : Integer.parseInt(l.tint(), 16);
        add(out, 0, add, weight, l == null ? 1 : (float) l.ambient());
        add(out, 1, add, weight, ((tint >> 16) & 0xff) / 255f);
        add(out, 2, add, weight, ((tint >> 8) & 0xff) / 255f);
        add(out, 3, add, weight, (tint & 0xff) / 255f);
        add(out, 4, add, weight, l == null ? 0 : (float) l.caustics());
        add(out, 5, add, weight, l == null ? 0 : l.snow());
        add(out, 6, add, weight, l == null ? 0 : (float) l.headlights());
    }

    private static void add(float[] out, int k, int add, float weight, float value) {
        out[k] = add == 0 ? weight * value : out[k] + weight * value;
    }

    /**
     * The grade's strength above and below the waterline on the screen at ground scroll {@code scroll}, 0 (dry) to 1
     * (graded), into {@code out} ({@code [0]} the waterline's screen y, NaN when none is on the screen; {@code [1]}
     * above it, {@code [2]} below it, both the medium's share {@code blend} without a line on the screen).
     */
    static void wipe(double[] rows, boolean[] intoWater, double scroll, double blend, double[] out) {
        out[0] = Double.NaN;
        out[1] = blend;
        out[2] = blend;
        out[3] = -1;
        for (int w = 0; w < rows.length; w++) {
            double y = rows[w] - scroll;
            if (y > 0 && y < HEIGHT) {
                out[0] = y;
                out[1] = intoWater[w] ? 1 : 0;
                out[3] = w;
                return;
            }
        }
    }

    /** This frame's wipe and look (call before {@link #active}, {@link #draw}, {@link #drawSnow}). */
    void prepare(Sortie sortie, double scroll, double seconds) {
        double[] out = wipeOut;
        wipe(rows, intoWater, scroll, sortie.waterBlend(), out);
        wipeY = out[0];
        above = (float) out[1];
        below = (float) out[2];
        lookAt(looks, starts, seconds, now);
        // Above a waterline on the screen the medium after it: its section's look (the water ahead).
        int w = (int) out[3];
        lookAt(looks, starts, w < 0 ? seconds : Math.max(seconds, times[w]), nowAbove);
    }

    /** Whether any of the field is graded this frame (the glows are then drawn after it). */
    boolean active() {
        return above > 0 || below > 0;
    }

    /** Whether a point at play-field height {@code y} lies in the water this frame (its burst the under-water one). */
    boolean underwaterAt(double y) {
        float strength = !Double.isNaN(wipeY) && y > wipeY ? above : below;
        return strength >= 0.5f;
    }

    /** The look at play-field height {@code y} this frame (above a waterline on the screen, the water's ahead). */
    private float[] lookAt(double y) {
        return !Double.isNaN(wipeY) && y > wipeY ? nowAbove : now;
    }

    /** The grade's strength at play-field height {@code y} this frame. */
    private float strengthAt(double y) {
        return !Double.isNaN(wipeY) && y > wipeY ? above : below;
    }

    /**
     * Grades what is drawn so far on the play field: renders the light map (the base by the wipe, the caustics, the
     * headlights and the pools) and multiplies it over the field.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, double scroll, double real) {
        if (!active()) {
            return;
        }
        batch.end();
        saved.set(batch.getProjectionMatrix());
        // FrameBuffer.end() binds the window, not the frame buffer that was bound before (no nesting).
        Gdx.gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, boundFramebuffer);
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, boundViewport);
        lightMap.begin();
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(lightProjection);
        batch.begin();
        batch.disableBlending();
        if (Double.isNaN(wipeY)) {
            base(batch, now, below, 0, HEIGHT);
        } else {
            int line = (int) Math.round(wipeY);
            base(batch, now, below, 0, line);
            base(batch, nowAbove, above, line, HEIGHT - line);
        }
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        drawCaustics(batch, scroll, real);
        drawLights(batch, sortie, alpha);
        batch.setColor(Color.WHITE);
        batch.end();
        lightMap.end();
        Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, boundFramebuffer.get(0));
        Gdx.gl.glViewport(boundViewport.get(0), boundViewport.get(1), boundViewport.get(2), boundViewport.get(3));
        batch.setProjectionMatrix(saved);
        batch.begin();
        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
        batch.draw(lightMap.getColorBufferTexture(), X0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT, false, true);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The base over rows {@code from} to {@code from + rows}: white blended toward ambient × tint by {@code strength}. */
    private void base(SpriteBatch batch, float[] look, float strength, int from, int height) {
        if (height <= 0) {
            return;
        }
        float k = look[0];
        batch.setColor(
                1 + (k * look[1] - 1) * strength,
                1 + (k * look[2] - 1) * strength,
                1 + (k * look[3] - 1) * strength,
                1);
        batch.draw(pixel, 0, from, WIDTH, height);
    }

    /** The caustics: two layers of the ripple net riding the ground and drifting apart on the real clock. */
    private void drawCaustics(SpriteBatch batch, double scroll, double real) {
        float light = CAUSTIC_GAIN * Math.max(nowAbove[4] * above, now[4] * below);
        if (light <= 0) {
            return;
        }
        double t = real % 3600;
        batch.setColor(CAUSTIC.r, CAUSTIC.g, CAUSTIC.b, light * 0.6f);
        tiled(batch, DRIFT_A_X * t, scroll + DRIFT_A_Y * t, 1);
        batch.setColor(CAUSTIC.r, CAUSTIC.g, CAUSTIC.b, light * 0.5f);
        tiled(batch, DRIFT_B_X * t + 41, scroll + DRIFT_B_Y * t + 77, 1.37f);
    }

    /** The caustic tile over the field at {@code scale}, shifted by (dx, dy) px (y: the ground's rows, up the field). */
    private void tiled(SpriteBatch batch, double dx, double dy, float scale) {
        float size = CAUSTIC_TILE * scale;
        float u = (float) (dx / size);
        float v = (float) (-dy / size);
        batch.draw(causticTexture, 0, 0, WIDTH, HEIGHT, u, v + HEIGHT / size, u + WIDTH / size, v);
    }

    /** The headlights, the pools round the ship and Rook, the shots' and the pickups' pools. */
    private void drawLights(SpriteBatch batch, Sortie sortie, float alpha) {
        if (sortie.flying()) {
            float x = (float) sortie.ship().renderX(alpha);
            float y = (float) sortie.ship().renderY(alpha);
            float cone = lookAt(y + LAMP_DY)[6] * HEADLIGHT_GAIN * strengthAt(y + LAMP_DY);
            if (cone > 0) {
                batch.setColor(HEADLIGHT.r, HEADLIGHT.g, HEADLIGHT.b, cone);
                drawCone(batch, x - LAMP_DX, y + LAMP_DY, CONE_TOE);
                drawCone(batch, x + LAMP_DX, y + LAMP_DY, -CONE_TOE);
            }
            batch.setColor(1, 1, 1, SHIP_POOL_LIGHT);
            pool(batch, x, y, SHIP_POOL);
        }
        Wingman rook = sortie.wingman().orElse(null);
        if (rook != null && !rook.ejected()) {
            batch.setColor(1, 1, 1, ROOK_POOL_LIGHT);
            pool(batch, rook.renderX(alpha), rook.renderY(alpha), ROOK_POOL);
        }
        batch.setColor(1, 1, 1, SHOT_POOL_LIGHT);
        for (int i = 0; i < sortie.shotCount(); i++) {
            pool(batch, sortie.shot(i).renderX(alpha), sortie.shot(i).renderY(alpha), SHOT_POOL);
        }
        batch.setColor(PICKUP.r, PICKUP.g, PICKUP.b, PICKUP_POOL_LIGHT);
        for (int i = 0; i < sortie.pickupCount(); i++) {
            pool(batch, sortie.pickup(i).renderX(alpha), sortie.pickup(i).renderY(alpha), PICKUP_POOL);
        }
    }

    /** One cone from the lamp at (x, y) up the screen, turned {@code degrees} (counter-clockwise). */
    private void drawCone(SpriteBatch batch, float x, float y, float degrees) {
        int w = coneTexture.getWidth();
        int h = coneTexture.getHeight();
        batch.draw(coneTexture, x - w / 2f, y, w / 2f, 0, w, h, 1, 1, degrees, 0, 0, w, h, false, false);
    }

    private void pool(SpriteBatch batch, double x, double y, float radius) {
        batch.draw(radial, (float) (x - radius), (float) (y - radius), 2 * radius, 2 * radius);
    }

    /**
     * The marine snow over the high-air layer: as many motes as the look's {@code snow}, sinking and passing with the
     * ground at {@value #MOTE_PARALLAX} its scroll, swaying on the real clock; only where the field is graded.
     */
    void drawSnow(SpriteBatch batch, double scroll, double real) {
        int shown = Math.min(MOST_MOTES, Math.round(Math.max(now[5], Double.isNaN(wipeY) ? 0 : nowAbove[5])));
        if (shown == 0 || !active()) {
            return;
        }
        int spanX = WIDTH + 2 * MARGIN;
        int spanY = HEIGHT + 2 * MARGIN;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        double t = real % 3600;
        for (int i = 0; i < shown; i++) {
            double sway = SWAY * Math.sin(t * 0.6 + motePhase[i]);
            float x = (float) Math.floorMod(Math.round(moteX[i] + sway), (long) spanX) - MARGIN;
            float y =
                    (float) Math.floorMod(Math.round(moteY[i] - moteSink[i] * t - MOTE_PARALLAX * scroll), (long) spanY)
                            - MARGIN;
            float strength = strengthAt(y);
            if (strength <= 0) {
                continue;
            }
            batch.setColor(MOTE.r, MOTE.g, MOTE.b, moteLight[i] * strength);
            batch.draw(pixel, X0 + x, y, 1, 1);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** A white disc fading from its centre to its rim (the pools). */
    private static Texture radialTexture() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        double half = size / 2.0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double d = Math.hypot(x + 0.5 - half, y + 0.5 - half) / half;
                float a = (float) Math.max(0, 1 - d);
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, a * a * (3 - 2 * a)));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /** A soft cone {@value #CONE_LENGTH} px long pointing up, its tip at the bottom middle, fading to its end and sides. */
    private static Texture coneTexture() {
        int height = CONE_LENGTH;
        int width = (int) Math.ceil(2 * CONE_LENGTH * Math.tan(CONE_HALF_ANGLE)) + 2;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        double half = width / 2.0;
        for (int y = 0; y < height; y++) {
            // Pixmap rows run down: row 0 is the far end.
            double up = height - y - 0.5;
            double along = up / CONE_LENGTH;
            for (int x = 0; x < width; x++) {
                double off = Math.abs(Math.atan2(x + 0.5 - half, up)) / CONE_HALF_ANGLE;
                double side = Math.clamp(1 - off, 0, 1);
                double a = side * side * (3 - 2 * side) * (1 - along) * (1 - along) * Math.min(1, along * 8);
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, (float) a));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * The caustics' wrapping tile: a net of bright ridges where two sums of whole-period waves cross zero, so it tiles
     * seamlessly on both axes.
     */
    private static Texture causticTexture() {
        int size = CAUSTIC_TILE;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float c = (float) caustic((x + 0.5) / size, (y + 0.5) / size);
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, c));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        pixmap.dispose();
        return texture;
    }

    /** The caustics' brightness at (u, v) of the tile, 0 to 1 (periodic in both). */
    static double caustic(double u, double v) {
        double tau = 2 * Math.PI;
        double a = Math.sin(tau * (4 * u + 2 * v))
                + Math.sin(tau * (-3 * u + 5 * v) + 1.3)
                + Math.sin(tau * (5 * u - 3 * v) + 2.1);
        double b = Math.sin(tau * (3 * u - 5 * v) + 0.7)
                + Math.sin(tau * (-5 * u - 2 * v) + 2.9)
                + Math.sin(tau * (4 * u + 4 * v) + 4.4);
        double ra = Math.max(0, 1 - Math.abs(a) / RIDGE);
        double rb = Math.max(0, 1 - Math.abs(b) / RIDGE);
        return Math.min(1, ra * ra * ra + rb * rb * rb);
    }

    @Override
    public void dispose() {
        lightMap.dispose();
        radialTexture.dispose();
        coneTexture.dispose();
        causticTexture.dispose();
    }
}
