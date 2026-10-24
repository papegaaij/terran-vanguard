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
import vanguard.content.SectionsBlock.Headlights;
import vanguard.content.SectionsBlock.UnderwaterLook;
import vanguard.content.TerrainBlock;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;
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
 * twin headlights ahead of the ship and Rook, a soft pool round the ship and Rook (so the hulls read) and round each of
 * the player's shots and each pickup (so they keep their light).
 *
 * <p><b>The headlights</b> (step LX, round 54's F.2: "headlights that illuminate the world") are real light: inside a
 * cone the light map rises from the base toward full (the world drawn at its own colour, slightly warm), by the look's
 * {@code headlights} strength with a soft falloff along and across the beam; outside, the base. Their reach and the
 * sector they light are the look's ({@link Headlights}), Rook's beams {@value #ROOK_REACH} of the ship's reach.
 * <b>The rim</b> (the look's {@code rim}) is a faint light along the terrain's rock edges (the walls with their cave
 * notches open, the islands' feet), so the trench's shape reads in the dark outside the beams.
 *
 * <p><b>The waterline wipe</b>: while a waterline's row ({@link #waterlineRows}: the ground row the ship's start height
 * passes at the waterline's time, the row the backdrop's waterline band lies on) is on the screen, the field above it
 * takes the medium after the waterline (Level 23: graded, "the water ahead") and the field below it the medium's share
 * now (dry until the crossing, easing to graded over the water's 1 s); then the whole field. The marine snow ({@link
 * #drawSnow}) is drawn over the high-air layer on the real clock, as many motes as the look's {@code snow}, only where
 * the field is graded. Nothing is allocated per frame.
 *
 * <p>M8 part F: the ambient follows a section's {@code ramp} ({@link UnderwaterLook#ambientAt}: Level 26's lamps going
 * out, 20 → 6 % over t=8–18), held at least at the Dark levels assist's floor ({@link #ambientFloor}); in a dark level
 * ({@link DarkLight}) its static lights, the units' rim, glow and sonar pools and the escort's lamp go into the light
 * map with the headlights, and the marine snow is lit only inside the cones ({@link #coneLight}).
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
    /** The look's values in {@link #lookAt}'s array: ambient, tint r g b, caustics, snow, the headlights, the rim. */
    static final int LOOK_SIZE = 10;

    static final int AMBIENT = 0;
    static final int CAUSTICS = 4;
    static final int SNOW = 5;
    static final int LIGHTS = 6;
    static final int REACH = 7;
    static final int ANGLE = 8;
    static final int RIM = 9;
    /**
     * The cone texture: its length, px, and its half-angle (drawn stretched to the look's reach and angle). Each lamp's
     * cone spans {@value #CONE_SHARE} of the look's sector, toed out by {@value #TOE_SHARE} of it, so the two overlap
     * straight ahead and together light the whole sector.
     */
    static final int CONE_LENGTH = 256;

    static final double CONE_HALF_ANGLE = Math.toRadians(20);
    static final double CONE_SHARE = 0.84;
    static final double TOE_SHARE = 0.08;
    /** Along a beam: full from the lamp to this share of its reach, then fading to nothing at its end. */
    static final double BEAM_HOLD = 0.3;
    /** Across a beam: the outer share of its half-angle over which it fades to its edge. */
    static final double BEAM_EDGE = 0.45;
    /** Rook's beams: this share of the ship's reach, the same strength and sector. */
    static final double ROOK_REACH = 0.8;
    /** The lamps' places on the hull (dx, dy). */
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
    private static final Color HEADLIGHT = new Color(1f, 0.97f, 0.9f, 1);
    /** The rim's light: cool, at most this share of the way to full at the rock edge, its band's sides px. */
    private static final Color RIM_LIGHT = new Color(0.8f, 0.92f, 1f, 1);

    static final int RIM_IN = 32;
    static final int RIM_OUT = 6;
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
    /** M8 part F: the beam's haze, the cone's shape with its light strongest at the lamp ({@link #drawHaze}). */
    private final Texture hazeTexture;

    private final Texture rimTexture;
    /** The rim's band standing up (the water at its top), for a notch's edges across the wall. */
    private final Texture rimAcross;
    /** Per island its foot polygon (x, row), as the simulation's {@link Terrain.Island}. */
    private final double[][] islandXs;

    private final double[][] islandRows;
    private final double[] islandLow;
    private final double[] islandHigh;
    /** The cone's colour this frame: the light still missing to full where the lamps are (per channel). */
    private final float[] missing = new float[3];

    private final Texture causticTexture;
    private final Matrix4 lightProjection = new Matrix4();
    private final Matrix4 saved = new Matrix4();
    private final java.nio.IntBuffer boundFramebuffer = com.badlogic.gdx.utils.BufferUtils.newIntBuffer(16);
    private final java.nio.IntBuffer boundViewport = com.badlogic.gdx.utils.BufferUtils.newIntBuffer(16);
    /** The look now (refreshed per frame, no allocation): ambient, tint r g b, caustics, snow, headlights. */
    private final float[] now = new float[LOOK_SIZE];

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
    private final float[] nowAbove = new float[LOOK_SIZE];
    /** M8 part F: the dark level's lights and pools, null in another level ({@link DarkLight}). */
    private DarkLight dark;
    /** M8 part F: Rook's loss's lamp (its flicker and place while he spins down); null before {@link #rookLoss}. */
    private RookLossLooks rookLoss;
    /** M8 part F: the Dark levels assist's ambient floor (0: off). */
    private float ambientFloor;
    /** M8 part F: this frame's hulls with lamps (the ship, Rook) for the snow's cone: place and whether lit. */
    private double shipLampX;

    private double shipLampY;
    private boolean shipLamps;
    private double rookLampX;
    private double rookLampY;
    private boolean rookLamps;

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
        hazeTexture = hazeTexture();
        rimTexture = rimTexture(false);
        rimAcross = rimTexture(true);
        causticTexture = causticTexture();
        List<TerrainBlock.Island> islands =
                level.terrain().map(TerrainBlock::islandList).orElse(List.of());
        islandXs = new double[islands.size()][];
        islandRows = new double[islands.size()][];
        islandLow = new double[islands.size()];
        islandHigh = new double[islands.size()];
        for (int i = 0; i < islands.size(); i++) {
            List<vanguard.content.Point> points = islands.get(i).points();
            islandXs[i] = new double[points.size()];
            islandRows[i] = new double[points.size()];
            islandLow[i] = Double.POSITIVE_INFINITY;
            islandHigh[i] = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < points.size(); k++) {
                islandXs[i][k] = points.get(k).x();
                islandRows[i][k] = points.get(k).y();
                islandLow[i] = Math.min(islandLow[i], islandRows[i][k]);
                islandHigh[i] = Math.max(islandHigh[i], islandRows[i][k]);
            }
        }
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

    /** M8 part F: the dark level's lights and pools ({@link DarkLight}), drawn into the light map. */
    /** M8 part F: Rook's loss's looks, for his lamp's flicker and place as he spins down. */
    void rookLoss(RookLossLooks looks) {
        rookLoss = looks;
    }

    void dark(DarkLight darkLight) {
        dark = darkLight;
    }

    /** M8 part F: the Dark levels assist: the ambient is held at least at {@code floor} (0: off). */
    void ambientFloor(float floor) {
        ambientFloor = floor;
    }

    /** The ground rows of the level's waterlines. */
    double[] waterlineRows() {
        return rows.clone();
    }

    /**
     * The look at level time {@code seconds} into {@code out} ({@value #LOOK_SIZE} values: ambient, tint r, g, b,
     * caustics, snow, the headlights' strength, reach and angle, the rim): its
     * section's, eased from the section before over {@value #LOOK_EASE} s; a section in the air (no look) is the unlit
     * scene (ambient 1, white, no light of its own), so a waterline's wipe and the medium's share do the change. M8 part
     * F: a look's ambient at {@code seconds} ({@link UnderwaterLook#ambientAt}: its ramp).
     */
    static void lookAt(List<Optional<UnderwaterLook>> looks, double[] starts, double seconds, float[] out) {
        int i = 0;
        while (i + 1 < starts.length && seconds >= starts[i + 1]) {
            i++;
        }
        put(looks.get(i), out, 0, 1, seconds);
        double into = seconds - starts[i];
        if (i > 0
                && into < LOOK_EASE
                && looks.get(i).isPresent()
                && looks.get(i - 1).isPresent()) {
            double u = into / LOOK_EASE;
            u = u * u * (3 - 2 * u);
            put(looks.get(i - 1), out, 0, (float) (1 - u), seconds);
            put(looks.get(i), out, 1, (float) u, seconds);
        }
    }

    /**
     * M8 part F: as {@link #lookAt(List, double[], double, float[])}, the ambient held at least at {@code floor} (the
     * Dark levels assist; the air's ambient is 1 anyway).
     */
    static void lookAt(
            List<Optional<UnderwaterLook>> looks, double[] starts, double seconds, float floor, float[] out) {
        lookAt(looks, starts, seconds, out);
        out[AMBIENT] = Math.max(out[AMBIENT], floor);
    }

    /** Writes (or, {@code add} 1, adds) {@code weight} of {@code look} into {@code out}; the air is the unlit scene. */
    private static void put(Optional<UnderwaterLook> look, float[] out, int add, float weight, double seconds) {
        UnderwaterLook l = look.orElse(null);
        int tint = l == null ? 0xffffff : Integer.parseInt(l.tint(), 16);
        add(out, 0, add, weight, l == null ? 1 : (float) l.ambientAt(seconds));
        add(out, 1, add, weight, ((tint >> 16) & 0xff) / 255f);
        add(out, 2, add, weight, ((tint >> 8) & 0xff) / 255f);
        add(out, 3, add, weight, (tint & 0xff) / 255f);
        add(out, 4, add, weight, l == null ? 0 : (float) l.caustics());
        add(out, 5, add, weight, l == null ? 0 : l.snow());
        Headlights lights = l == null ? null : l.headlights();
        add(out, LIGHTS, add, weight, lights == null ? 0 : (float) lights.strength());
        add(out, REACH, add, weight, (float) (lights == null ? Headlights.REACH : lights.reach()));
        add(out, ANGLE, add, weight, (float) (lights == null ? Headlights.ANGLE : lights.angle()));
        add(out, RIM, add, weight, l == null ? 0 : (float) l.rimLight());
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
        lookAt(looks, starts, seconds, ambientFloor, now);
        // Above a waterline on the screen the medium after it: its section's look (the water ahead).
        int w = (int) out[3];
        lookAt(looks, starts, w < 0 ? seconds : Math.max(seconds, times[w]), ambientFloor, nowAbove);
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
    float[] lookAt(double y) {
        return !Double.isNaN(wipeY) && y > wipeY ? nowAbove : now;
    }

    /** The grade's strength at play-field height {@code y} this frame. */
    float strengthAt(double y) {
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
        drawRim(batch, sortie.terrain(), scroll);
        drawLights(batch, sortie, alpha);
        if (dark != null) {
            // M8 part F: the static lights, the units' rim, glow and sonar pools, the escort's lamp.
            dark.drawLight(batch, this, sortie, alpha, scroll);
        }
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
        float light = CAUSTIC_GAIN * Math.max(nowAbove[CAUSTICS] * above, now[CAUSTICS] * below);
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
        Wingman rook = sortie.wingman().orElse(null);
        // M8 part F: his headlights go out with his loss (his lamp's pool flickers and dies, below).
        rookLamps = rook != null && !rook.ejected() && !rook.loss().lost();
        shipLamps = sortie.flying();
        if (rookLamps) {
            rookLampX = rook.renderX(alpha);
            rookLampY = rook.renderY(alpha);
            beams(batch, (float) rookLampX, (float) rookLampY, ROOK_REACH);
        }
        if (sortie.flying()) {
            shipLampX = sortie.ship().renderX(alpha);
            shipLampY = sortie.ship().renderY(alpha);
            beams(
                    batch,
                    (float) sortie.ship().renderX(alpha),
                    (float) sortie.ship().renderY(alpha),
                    1);
            batch.setColor(1, 1, 1, SHIP_POOL_LIGHT);
            pool(batch, sortie.ship().renderX(alpha), sortie.ship().renderY(alpha), SHIP_POOL);
        }
        if (rook != null && !rook.ejected() && (dark == null || dark.escortLamp() <= 0)) {
            // M8 part F: in a dark level with an escort's lamp that lamp is his pool (DarkLight); after his loss it
            // flickers and dies with his spin (RookLossLooks).
            float lamp = RookLossLooks.lamp(rook);
            if (lamp > 0) {
                batch.setColor(1, 1, 1, ROOK_POOL_LIGHT * lamp);
                pool(
                        batch,
                        rookLoss == null ? rook.renderX(alpha) : rookLoss.lampX(rook, alpha),
                        rookLoss == null ? rook.renderY(alpha) : rookLoss.lampY(rook, alpha),
                        ROOK_POOL);
            }
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

    /**
     * The twin beams of a hull at (x, y), its reach {@code share} of the look's: each lamp's cone adds the light still
     * missing to full there (white less the base, slightly warm) times the look's strength, so the world in the cone
     * comes up toward its own colour whatever the ambient; nothing where the field is dry.
     */
    private void beams(SpriteBatch batch, float x, float y, double share) {
        float lampY = y + LAMP_DY;
        float[] look = lookAt(lampY);
        float strength = strengthAt(lampY);
        float lights = look[LIGHTS];
        if (lights <= 0 || strength <= 0) {
            return;
        }
        for (int c = 0; c < 3; c++) {
            missing[c] = (1 - look[AMBIENT] * look[1 + c]) * strength;
        }
        batch.setColor(HEADLIGHT.r * missing[0], HEADLIGHT.g * missing[1], HEADLIGHT.b * missing[2], lights);
        double sector = look[ANGLE];
        float reach = (float) (look[REACH] * share);
        float toe = (float) (sector * TOE_SHARE);
        float stretch = (float) (Math.tan(Math.toRadians(sector * CONE_SHARE / 2)) / Math.tan(CONE_HALF_ANGLE));
        drawCone(batch, x - LAMP_DX, lampY, toe, stretch, reach / CONE_LENGTH);
        drawCone(batch, x + LAMP_DX, lampY, -toe, stretch, reach / CONE_LENGTH);
    }

    /**
     * M8 part F: the headlights' light at play-field point (x, y) this frame, 0 to 1 (the look's strength times the
     * brightest of the ship's and Rook's lamps' cones there, as {@link #drawCone} draws them); 0 where the field is dry.
     */
    float coneLight(double x, double y) {
        float best = 0;
        if (shipLamps) {
            best = Math.max(best, hullLight(x, y, shipLampX, shipLampY, 1));
        }
        if (rookLamps) {
            best = Math.max(best, hullLight(x, y, rookLampX, rookLampY, ROOK_REACH));
        }
        return best;
    }

    private float hullLight(double x, double y, double hullX, double hullY, double share) {
        double lampY = hullY + LAMP_DY;
        float[] look = lookAt(lampY);
        float lights = look[LIGHTS] * strengthAt(lampY);
        if (lights <= 0) {
            return 0;
        }
        double sector = look[ANGLE];
        double tall = look[REACH] * share / CONE_LENGTH;
        double toe = sector * TOE_SHARE;
        double wide = Math.tan(Math.toRadians(sector * CONE_SHARE / 2)) / Math.tan(CONE_HALF_ANGLE);
        double left = beamAt(x - (hullX - LAMP_DX), y - lampY, toe, wide, tall);
        double right = beamAt(x - (hullX + LAMP_DX), y - lampY, -toe, wide, tall);
        return (float) (lights * Math.max(left, right));
    }

    /**
     * One beam's light at (dx, dy) from its lamp, 0 to 1: the cone texture's alpha there, the texture stretched {@code
     * wide} across and {@code tall} along and turned {@code degrees} counter-clockwise, as {@link #drawCone} draws it.
     */
    static double beamAt(double dx, double dy, double degrees, double wide, double tall) {
        double angle = Math.toRadians(degrees);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double across = (dx * cos + dy * sin) / wide;
        double up = (-dx * sin + dy * cos) / tall;
        if (up <= 0) {
            return 0;
        }
        double off = Math.abs(Math.atan2(across, up)) / CONE_HALF_ANGLE;
        return beamAlong(up / CONE_LENGTH) * beamAcross(off);
    }

    /** One cone from the lamp at (x, y) up the screen, stretched, turned {@code degrees} (counter-clockwise). */
    private void drawCone(SpriteBatch batch, float x, float y, float degrees, float wide, float tall) {
        drawCone(batch, coneTexture, x, y, degrees, wide, tall);
    }

    private static void drawCone(
            SpriteBatch batch, Texture texture, float x, float y, float degrees, float wide, float tall) {
        int w = texture.getWidth();
        int h = texture.getHeight();
        batch.draw(texture, x - w / 2f, y, w / 2f, 0, w, h, wide, tall, degrees, 0, 0, w, h, false, false);
    }

    /**
     * M8 part F (the user on Level 25: "I can't see the cones of the headlights at all"): the beams' faint volume, so a
     * cone reads in empty black water where it lights nothing (Levels 23–26 alike). After the grade, additive, ungraded:
     * per lamp of the ship and of Rook (while his headlights burn) a soft haze in the beam's shape, {@value #HAZE} of the
     * look's headlight strength at the lamp and fading along the beam well before its reach ({@link #hazeAlong}), so it
     * never washes out the dark. Call after {@link #draw} (it uses that frame's lamps).
     */
    void drawHaze(SpriteBatch batch) {
        if (!active()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (shipLamps) {
            haze(batch, (float) shipLampX, (float) shipLampY, 1);
        }
        if (rookLamps) {
            haze(batch, (float) rookLampX, (float) rookLampY, ROOK_REACH);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** M8 part F: the haze's strength at the lamp, of the look's headlight strength (the two beams overlap ahead). */
    static final float HAZE = 0.07f;
    /** M8 part F: inside a cone a mote of the marine snow is lit up to this much more (it shows the beam). */
    static final float SNOW_CONE_BOOST = 0.8f;

    private void haze(SpriteBatch batch, float x, float y, double share) {
        float lampY = y + LAMP_DY;
        float[] look = lookAt(lampY);
        float strength = strengthAt(lampY) * look[LIGHTS];
        if (strength <= 0) {
            return;
        }
        batch.setColor(HEADLIGHT.r, HEADLIGHT.g, HEADLIGHT.b, HAZE * strength);
        double sector = look[ANGLE];
        float reach = (float) (look[REACH] * share);
        float toe = (float) (sector * TOE_SHARE);
        float stretch = (float) (Math.tan(Math.toRadians(sector * CONE_SHARE / 2)) / Math.tan(CONE_HALF_ANGLE));
        drawCone(batch, hazeTexture, X0 + x - LAMP_DX, lampY, toe, stretch, reach / CONE_LENGTH);
        drawCone(batch, hazeTexture, X0 + x + LAMP_DX, lampY, -toe, stretch, reach / CONE_LENGTH);
    }

    /**
     * The rim: along each rock edge on the screen (the walls' with their notches open, the islands' feet), a band of
     * cool light {@value #RIM_IN} px into the rock and {@value #RIM_OUT} px into the water, at its brightest on the
     * edge, adding the look's {@code rim} share of the light missing to full; every second screen row.
     */
    private void drawRim(SpriteBatch batch, Terrain terrain, double scroll) {
        if (terrain == null) {
            return;
        }
        int w = rimTexture.getWidth();
        for (int y = 0; y < HEIGHT; y += 2) {
            float[] look = lookAt(y);
            float strength = strengthAt(y) * look[RIM];
            if (strength <= 0) {
                continue;
            }
            batch.setColor(
                    RIM_LIGHT.r * (1 - look[AMBIENT] * look[1]),
                    RIM_LIGHT.g * (1 - look[AMBIENT] * look[2]),
                    RIM_LIGHT.b * (1 - look[AMBIENT] * look[3]),
                    strength);
            double row = Terrain.row(scroll, y + 1);
            double left = terrain.leftRockAt(row);
            if (!Double.isNaN(left)) {
                // The texture's rock side is its left: the left wall's rock lies left of its edge.
                batch.draw(rimTexture, (float) left - RIM_IN, y, w, 2, 0, 0, w, 1, false, false);
            }
            double right = terrain.rightRockAt(row);
            if (!Double.isNaN(right)) {
                batch.draw(rimTexture, (float) right - RIM_OUT, y, w, 2, 0, 0, w, 1, true, false);
            }
            for (int i = 0; i < islandXs.length; i++) {
                if (row < islandLow[i] || row > islandHigh[i]) {
                    continue;
                }
                double a = crossing(islandXs[i], islandRows[i], row, true);
                double b = crossing(islandXs[i], islandRows[i], row, false);
                if (a < b) {
                    batch.draw(rimTexture, (float) a - RIM_OUT, y, w, 2, 0, 0, w, 1, true, false);
                    batch.draw(rimTexture, (float) b - RIM_IN, y, w, 2, 0, 0, w, 1, false, false);
                }
            }
        }
        // The cave notches' edges across the wall: the rock below the opening (its low row) and above it (its high).
        List<Terrain.Notch> notches = terrain.notches();
        for (int n = 0; n < notches.size(); n++) {
            Terrain.Notch notch = notches.get(n);
            notchEdge(batch, terrain, notch, notch.low(), scroll, false);
            notchEdge(batch, terrain, notch, notch.high(), scroll, true);
        }
    }

    /** One edge of a notch across its wall at terrain row {@code row}: the rock above it ({@code above}) or below. */
    private void notchEdge(
            SpriteBatch batch, Terrain terrain, Terrain.Notch notch, double row, double scroll, boolean above) {
        double y = Terrain.y(scroll, row);
        if (y < -RIM_IN || y > HEIGHT + RIM_IN) {
            return;
        }
        float[] look = lookAt(y);
        float strength = strengthAt(y) * look[RIM];
        if (strength <= 0) {
            return;
        }
        boolean left = notch.side() == Terrain.Side.LEFT;
        double line = left ? terrain.leftAt(row) : terrain.rightAt(row);
        if (Double.isNaN(line)) {
            return;
        }
        double back = left ? line - notch.depth() : line + notch.depth();
        batch.setColor(
                RIM_LIGHT.r * (1 - look[AMBIENT] * look[1]),
                RIM_LIGHT.g * (1 - look[AMBIENT] * look[2]),
                RIM_LIGHT.b * (1 - look[AMBIENT] * look[3]),
                strength);
        int h = rimAcross.getHeight();
        float x0 = (float) Math.min(line, back);
        float width = (float) Math.abs(line - back);
        float bottom = (float) (above ? y - RIM_OUT : y - RIM_IN);
        batch.draw(rimAcross, x0, bottom, width, h, 0, 0, 1, h, false, above);
    }

    /** A polygon's leftmost ({@code first}) or rightmost crossing of {@code row} (±∞ off it). */
    static double crossing(double[] xs, double[] rows, double row, boolean first) {
        double best = first ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        int n = xs.length;
        for (int c = 0; c < n; c++) {
            int d = (c + 1) % n;
            double low = Math.min(rows[c], rows[d]);
            double high = Math.max(rows[c], rows[d]);
            if (row < low || row > high || high == low) {
                continue;
            }
            double x = xs[c] + (xs[d] - xs[c]) * (row - rows[c]) / (rows[d] - rows[c]);
            best = first ? Math.min(best, x) : Math.max(best, x);
        }
        return best;
    }

    void pool(SpriteBatch batch, double x, double y, float radius) {
        batch.draw(radial, (float) (x - radius), (float) (y - radius), 2 * radius, 2 * radius);
    }

    /**
     * The marine snow over the high-air layer: as many motes as the look's {@code snow}, sinking and passing with the
     * ground at {@value #MOTE_PARALLAX} its scroll, swaying on the real clock; only where the field is graded. M8 part F:
     * in a dark level only inside the headlight cones ({@link #coneLight}).
     */
    void drawSnow(SpriteBatch batch, double scroll, double real) {
        int shown = Math.min(MOST_MOTES, Math.round(Math.max(now[SNOW], Double.isNaN(wipeY) ? 0 : nowAbove[SNOW])));
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
            // M8 part F: a mote in a cone is lit more, so the beam shows in the snow (in a dark level only there).
            float cone = coneLight(x, y);
            strength *= dark != null ? cone * (1 + SNOW_CONE_BOOST) : 1 + SNOW_CONE_BOOST * cone;
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

    /**
     * A beam {@value #CONE_LENGTH} px long pointing up, its tip at the bottom middle: full from the lamp to {@value
     * #BEAM_HOLD} of its length, then fading to its end; across, full in the middle and fading over the outer {@value
     * #BEAM_EDGE} of its half-angle.
     */
    private static Texture coneTexture() {
        int height = CONE_LENGTH;
        int width = (int) Math.ceil(2 * CONE_LENGTH * Math.tan(CONE_HALF_ANGLE)) + 2;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        double half = width / 2.0;
        for (int y = 0; y < height; y++) {
            // Pixmap rows run down: row 0 is the far end.
            double up = height - y - 0.5;
            double along = beamAlong(up / CONE_LENGTH);
            for (int x = 0; x < width; x++) {
                double off = Math.abs(Math.atan2(x + 0.5 - half, up)) / CONE_HALF_ANGLE;
                double a = along * beamAcross(off);
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, (float) a));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * M8 part F: the haze's texture: the cone's shape and edge, its light by {@link #hazeAlong} along the beam and softer
     * toward the edge across it.
     */
    private static Texture hazeTexture() {
        int height = CONE_LENGTH;
        int width = (int) Math.ceil(2 * CONE_LENGTH * Math.tan(CONE_HALF_ANGLE)) + 2;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        double half = width / 2.0;
        for (int y = 0; y < height; y++) {
            double up = height - y - 0.5;
            double along = hazeAlong(up / CONE_LENGTH);
            for (int x = 0; x < width; x++) {
                double off = Math.abs(Math.atan2(x + 0.5 - half, up)) / CONE_HALF_ANGLE;
                double a = along * beamAcross(off) * (1 - 0.5 * Math.min(1, off));
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, (float) a));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * M8 part F: the haze's light at share {@code t} of the beam's reach, 0 to 1: rising over the lamp's first few px,
     * strongest near the lamp and falling off as {@code (1 − t)²} (the water scatters the beam close to the hull).
     */
    static double hazeAlong(double t) {
        if (t <= 0 || t >= 1) {
            return 0;
        }
        return Math.min(1, t * 16) * (1 - t) * (1 - t);
    }

    /** A beam's light at share {@code t} of its reach, 0 to 1: full to {@value #BEAM_HOLD}, then a smooth fade. */
    static double beamAlong(double t) {
        if (t <= 0 || t >= 1) {
            return 0;
        }
        double u = Math.clamp((t - BEAM_HOLD) / (1 - BEAM_HOLD), 0, 1);
        double fade = 1 - u * u * (3 - 2 * u);
        return Math.min(1, t * 12) * fade;
    }

    /** A beam's light at share {@code off} of its half-angle off its axis, 0 to 1: full, then a smooth edge. */
    static double beamAcross(double off) {
        double u = Math.clamp((1 - off) / BEAM_EDGE, 0, 1);
        return u * u * (3 - 2 * u);
    }

    /**
     * The rim's band, {@value #RIM_IN} + {@value #RIM_OUT} px long, one px across: lying (the rock on the left) or
     * standing (the rock at the bottom), the edge between, its light falling off into the rock over the band's length
     * there and quickly into the water.
     */
    private static Texture rimTexture(boolean standing) {
        int length = RIM_IN + RIM_OUT;
        Pixmap pixmap = standing
                ? new Pixmap(1, length, Pixmap.Format.RGBA8888)
                : new Pixmap(length, 1, Pixmap.Format.RGBA8888);
        for (int i = 0; i < length; i++) {
            if (standing) {
                // Pixmap rows run down: the water at the top, the rock at the bottom.
                pixmap.drawPixel(0, i, Color.rgba8888(1, 1, 1, (float) rim(RIM_OUT - i - 0.5)));
            } else {
                pixmap.drawPixel(i, 0, Color.rgba8888(1, 1, 1, (float) rim(i + 0.5 - RIM_IN)));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /** The rim's light {@code dx} px from the rock edge (negative: into the rock), 0 to 1. */
    static double rim(double dx) {
        if (dx >= 0) {
            double u = Math.clamp(1 - dx / RIM_OUT, 0, 1);
            return u * u;
        }
        double u = Math.clamp(1 + dx / RIM_IN, 0, 1);
        return u * u * (0.35 + 0.65 * u);
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
        hazeTexture.dispose();
        rimTexture.dispose();
        rimAcross.dispose();
        causticTexture.dispose();
    }
}
