package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Pickup;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.Wingman;

/**
 * M7 part E: Level 18's dust storm (design/campaign Level 18, Visibility in the storm; M6 = a, the look MA.2 = b), a
 * look only. A screen-space mask in a frame buffer (Level 06's light map is the pattern, {@link FarsideLooks#darken}),
 * centred on the ship, its radius the level's {@code visibility} at the level time and difficulty: the look-b edge
 * {@code smoothstep(R + 55, R - 65, d + 70 (a - 0.5) + 40 (b - 0.5))} for the data's 120 px falloff, broken by two
 * drifting noise fields ({@code storm-tongues-a}/{@code -b}, tools/art/backdrop_l18.py). The rust veil ({@code
 * storm-veil} with {@code storm-veil-drift} over it) is composited after the {@code low-air} layer, so the ground,
 * its units, the nests, the Dust Devils and the rover with its beacon go into the dust outside the radius, and tints
 * the clear part by a fifth as the storm closes in; a Dust Devil's open core glows through it faintly. After the
 * {@code air} units, a silhouette pass covers each of them by the mask with the dust in front of it (round 41 R.b:
 * seen through the dust, a ship shares the dust's colour): the veil's own colour at that place, a few per cent
 * denser with a faint trace of the unit's shading so its soft outline reads, its detail gone; only the pixels that
 * glow (Vrell teal and lime, the Ghost Drone's red eye and white tips) poke through, and the units' {@code -glow}
 * frames go on top; Rook outside the radius is such a silhouette with his running lights, a pickup outside it gets a
 * faint glint. Bullets,
 * markers, telegraphs, bursts, the HUD, warnings and threat arrows are drawn over it as ever.
 */
public final class StormLooks implements Disposable {
    /** The look-b edge for its 120 px falloff: clear to 65 px inside the radius, gone 55 px outside. */
    static final double EDGE_OUT = 55.0 / 120;

    static final double EDGE_IN = 65.0 / 120;
    /** How far the two noise fields push the edge, px (look b: 70 and 40 round their middle). */
    static final double TONGUE_A = 70;

    static final double TONGUE_B = 40;
    /** The noise fields' tile, px, and their drift, px/s (y up): the first down the screen, the second up. */
    static final float TONGUE_SIZE = 256;

    static final float DRIFT_A = -135;
    static final float DRIFT_B = 135;
    /** The veil's drift, px/s: its field down the screen, the lighter drift layer down faster and to the left. */
    private static final float VEIL_DOWN = 135;

    private static final float DRIFT_DOWN = 270;
    private static final float DRIFT_LEFT = 120;
    /** The storm tints even the clear part: this share of the veil at the storm's full strength (look b). */
    static final double CLEAR_TINT = 0.2;
    /** The ambience's swell at the storm's full strength, dB (design/campaign Level 18, Music: the walls' +3 dB). */
    static final double SWELL_DB = 3;
    /**
     * The silhouettes (round 41 R.b): the veil's colour times {@value #SILHOUETTE_SHADE} (a little denser dust where a
     * ship stands in it), plus {@value #SILHOUETTE_FORM} of the unit's own luminance round its middle (a trace of its
     * shading, no detail), the shade fading into the plain dust over {@value #SILHOUETTE_SOFT} texels round their
     * edge (a soft outline), their cover {@value #SILHOUETTE_COVER} over the unit drawn under them.
     */
    static final float SILHOUETTE_SHADE = 0.82f;

    static final float SILHOUETTE_FORM = 0.12f;
    static final float SILHOUETTE_SOFT = 2f;
    static final float SILHOUETTE_COVER = 1f;
    /** A Dust Devil's open core through the veil: its glow frames at this share. */
    private static final float CORE_THROUGH = 0.45f;
    /** The core's pulse: a frame every 5 steps (12 fps). */
    private static final int CORE_FRAME_TICKS = 5;
    /** A pickup outside the radius: the loot glint at this share, a frame every 4 steps. */
    private static final float PICKUP_GLINT = 0.6f;

    private static final int GLINT_FRAME_TICKS = 4;
    /** Rook's running lights: a red one on his left wing tip, a green one on his right, px from his centre. */
    private static final float WING_TIP_X = 17;

    private static final float WING_TIP_Y = -2;
    private static final float LIGHT_SIZE = 9;
    private static final Color PORT = new Color(1f, 0.2f, 0.16f, 1);
    private static final Color STARBOARD = new Color(0.3f, 1f, 0.45f, 1);
    /** A Dust Devil's core without its glow frames: a lime glow this big. */
    private static final Color LIME = Color.valueOf("A8FF3A");

    private static final float CORE_SIZE = 40;
    private static final int RADIAL_SIZE = 32;

    private static final String VERTEX = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            varying vec2 v_pos;
            void main() {
                v_color = a_color;
                v_color.a = v_color.a * (255.0 / 254.0);
                v_texCoords = a_texCoord0;
                v_pos = a_position.xy;
                gl_Position = u_projTrans * a_position;
            }
            """;
    /** The mask: 1 where the view is clear, 0 in the dust; the play field's px with y up, as the simulation's. */
    private static final String MASK = """
            #ifdef GL_ES
            #ifdef GL_FRAGMENT_PRECISION_HIGH
            precision highp float;
            #else
            precision mediump float;
            #endif
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            varying vec2 v_pos;
            uniform sampler2D u_texture;
            uniform sampler2D u_noise;
            uniform vec4 u_tongues;
            uniform vec4 u_edge;
            uniform vec2 u_ship;
            void main() {
                float a = texture2D(u_texture, (v_pos + u_tongues.xy) / u_edge.z).a;
                float b = texture2D(u_noise, (v_pos + u_tongues.zw) / u_edge.z).a;
                float d = distance(v_pos, u_ship) + u_edge.w * (a - 0.5) + 0.5714286 * u_edge.w * (b - 0.5);
                float x = clamp((d - u_edge.x) / (u_edge.y - u_edge.x), 0.0, 1.0);
                float v = x * x * (3.0 - 2.0 * x) * v_color.a;
                gl_FragColor = vec4(v, v, v, 1.0);
            }
            """;
    /**
     * Drawing through the mask: the texture where the dust hides (1 - mask, at least {@code u_floor}), or with {@code
     * u_sil.w} above 0 the sprite as a silhouette there in the dust's colour (the veil and its drift layer at that
     * place, {@code u_veilAt} + px x {@code u_veilPer}, as {@link #drawVeil} draws them), shaded by {@code u_sil.x}
     * plus {@code u_sil.y} of its own luminance, the shade fading into the plain dust over {@code u_sil.z} texels
     * round its edge ({@code u_texel}; it still covers the whole sprite, so a thin part shows no detail), its
     * glowing pixels left out (saturated and bright: the Vrell's light; a pale or white part such as a Needler's legs
     * goes into the dust, round 41 R.b, the Ghost Drone's white tips come back with its {@code -glow} frames).
     */
    private static final String MASKED = """
            #ifdef GL_ES
            #ifdef GL_FRAGMENT_PRECISION_HIGH
            precision highp float;
            #else
            precision mediump float;
            #endif
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            varying vec2 v_pos;
            uniform sampler2D u_texture;
            uniform sampler2D u_mask;
            uniform vec4 u_field;
            uniform float u_floor;
            uniform vec4 u_sil;
            uniform sampler2D u_veil;
            uniform sampler2D u_drift;
            uniform vec4 u_veilAt;
            uniform vec4 u_veilPer;
            uniform float u_drifting;
            uniform vec2 u_texel;
            void main() {
                vec4 texel = texture2D(u_texture, v_texCoords);
                vec4 c = v_color * texel;
                vec2 p = v_pos - u_field.xy;
                float v = texture2D(u_mask, p / u_field.zw).r;
                float hidden = 1.0 - v * (1.0 - u_floor);
                float hi = max(texel.r, max(texel.g, texel.b));
                float lo = min(texel.r, min(texel.g, texel.b));
                float lum = dot(texel.rgb, vec3(0.3, 0.59, 0.11));
                float sat = (hi - lo) / max(hi, 0.004);
                float glow = (sat > 0.6 && hi > 0.784) ? 1.0 : 0.0;
                vec3 dust = texture2D(u_veil, u_veilAt.xy + p * u_veilPer.xy).rgb;
                vec4 drift = texture2D(u_drift, u_veilAt.zw + p * u_veilPer.zw);
                dust = mix(dust, drift.rgb, drift.a * u_drifting);
                vec2 o = u_texel * u_sil.z;
                float soft = (2.0 * texel.a
                        + texture2D(u_texture, v_texCoords + vec2(o.x, o.y)).a
                        + texture2D(u_texture, v_texCoords + vec2(-o.x, o.y)).a
                        + texture2D(u_texture, v_texCoords + vec2(o.x, -o.y)).a
                        + texture2D(u_texture, v_texCoords + vec2(-o.x, -o.y)).a) / 6.0;
                float shade = mix(1.0, u_sil.x + u_sil.y * (lum - 0.5), soft * soft);
                vec4 sil = vec4(clamp(dust * shade, 0.0, 1.0), c.a * u_sil.w * (1.0 - glow));
                vec4 out_ = mix(c, sil, step(0.001, u_sil.w));
                gl_FragColor = vec4(out_.rgb, out_.a * hidden);
            }
            """;

    private final LevelData.Visibility visibility;
    private Difficulty difficulty = Difficulty.MEDIUM;
    private final FrameBuffer mask;
    private final ShaderProgram maskShader;
    private final ShaderProgram masked;
    private final Texture tongueA;
    private final Texture tongueB;
    private final Texture veil;
    /** The lighter drift layer over the veil; null without its texture. */
    private final Texture veilDrift;

    private final Texture radialTexture;
    private final Array<AtlasRegion> glint;
    private final Sprites sprites;
    /** A vortex's open-core glow frames by its kind, found once (empty: the drawn lime glow). */
    private final Map<EnemySpec, Array<AtlasRegion>> cores = new IdentityHashMap<>();

    private final Array<AtlasRegion> noCore = new Array<>(0);
    private final Matrix4 maskProjection = new Matrix4();
    private final Matrix4 saved = new Matrix4();
    /** The frame buffer and viewport the mask pass interrupts (the pixel screen's): restored after it. */
    private final java.nio.IntBuffer boundFramebuffer = com.badlogic.gdx.utils.BufferUtils.newIntBuffer(16);

    private final java.nio.IntBuffer boundViewport = com.badlogic.gdx.utils.BufferUtils.newIntBuffer(16);
    /** Whether the storm shows in this frame (its radius in from the full view); set by {@link #drawVeil}. */
    private boolean showing;
    /** The veil's and its drift layer's texture offsets in this frame (as {@link #drawVeil} drew them). */
    private float veilU;

    private float veilV;
    private float driftU;
    private float driftV;
    /** The texture whose texel size the silhouette pass has (its edge's softening); null before the first. */
    private Texture texelOf;

    private StormLooks(Sprites sprites, LevelData.Visibility visibility, String folder) {
        this.sprites = sprites;
        this.visibility = visibility;
        glint = sprites.glint;
        maskShader = shader(MASK);
        masked = shader(MASKED);
        mask = new FrameBuffer(Pixmap.Format.RGBA8888, PlayField.WIDTH, PlayField.HEIGHT, false);
        mask.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        maskProjection.setToOrtho2D(0, 0, PlayField.WIDTH, PlayField.HEIGHT);
        Map<Texture, Pixmap> pages = new IdentityHashMap<>();
        try {
            tongueA = wrapped(sprites, folder + "storm-tongues-a", pages, () -> noise(181));
            tongueB = wrapped(sprites, folder + "storm-tongues-b", pages, () -> noise(187));
            veil = wrapped(sprites, folder + "storm-veil", pages, StormLooks::flatVeil);
            veilDrift = sprites.hasBackdrop(folder + "storm-veil-drift")
                    ? wrapped(sprites, folder + "storm-veil-drift", pages, StormLooks::flatVeil)
                    : null;
        } finally {
            pages.values().forEach(Pixmap::dispose);
        }
        tongueA.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        tongueB.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        radialTexture = radialTexture();
        masked.bind();
        masked.setUniformi("u_mask", 1);
        masked.setUniformi("u_veil", 2);
        masked.setUniformi("u_drift", 3);
        maskShader.bind();
        maskShader.setUniformi("u_noise", 1);
    }

    /**
     * The level's storm looks, or null for a level without a {@code visibility} block. Its textures are the level's own
     * backdrop images ({@code level-NN/storm-veil}, ...), a drawn stand-in where one is missing.
     */
    public static StormLooks of(Sprites sprites, LevelData level, String levelKey) {
        return level.visibility()
                .map(visibility -> new StormLooks(sprites, visibility, Backdrop.folder(levelKey)))
                .orElse(null);
    }

    /** The difficulty's radii (easy wider, hard closer). */
    public void difficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * How strongly the storm shows at level time {@code t}, 0 (the full view) to 1 (the radius at its smallest on the
     * difficulty): the veil's tint over the clear part and the ambience's swell follow it.
     */
    static double strength(LevelData.Visibility visibility, Difficulty difficulty, double t) {
        double full = visibility.fullRadius();
        double span = full - visibility.smallest(difficulty);
        return span <= 0 ? 0 : Math.clamp((full - visibility.radiusAt(t, difficulty)) / span, 0, 1);
    }

    /**
     * M7 part E (design/campaign Level 18, Music): the dust-storm bed's swell at level time {@code t}, dB: {@value
     * #SWELL_DB} at the storm's full strength, following the visibility's ramps (in at the front and the back wall,
     * out into the eye and the clearing; Level 13's whiteout rise is the pattern); 0 for a level without a storm.
     */
    public static double swellDb(LevelData level, Difficulty difficulty, double t) {
        // Every frame on the render thread: no lambda, no allocation.
        return level.visibility().isEmpty() ? 0 : swellDb(level.visibility().get(), difficulty, t);
    }

    /** As {@link #swellDb(LevelData, Difficulty, double)} for the level's {@code visibility}. */
    public static double swellDb(LevelData.Visibility visibility, Difficulty difficulty, double t) {
        return SWELL_DB * strength(visibility, difficulty, t);
    }

    /**
     * The mask's value at a distance {@code d} px from the ship (1 clear, 0 in the dust), with the two noise fields'
     * values {@code a} and {@code b} (0..1) there, for a {@code radius} and {@code falloff}: the shader's formula.
     */
    static double visible(double d, double a, double b, double radius, double falloff) {
        double outer = radius + falloff * EDGE_OUT;
        double inner = radius - falloff * EDGE_IN;
        double pushed = d + TONGUE_A * falloff / 120 * (a - 0.5) + TONGUE_B * falloff / 120 * (b - 0.5);
        double x = Math.clamp((pushed - outer) / (inner - outer), 0, 1);
        return x * x * (3 - 2 * x);
    }

    /**
     * Renders the mask for this frame and composites the rust veil over what is drawn so far on the play field (the
     * ground and {@code low-air} layers and their units): gone into the dust outside the radius, tinted inside.
     * Nothing while the view is full.
     *
     * @param seconds the level time (the radius)
     * @param real the real time (the dust's drift)
     */
    public void drawVeil(SpriteBatch batch, Sortie sortie, float alpha, double seconds, double real) {
        double radius = visibility.radiusAt(seconds, difficulty);
        double strength = strength(visibility, difficulty, seconds);
        showing = strength > 0;
        if (!showing) {
            return;
        }
        renderMask(batch, sortie, alpha, radius, real);
        float floor = (float) (CLEAR_TINT * strength);
        begin(batch, floor, false);
        float t = (float) (real % 3600);
        veilU = 0;
        veilV = Math.round(VEIL_DOWN * t) / (float) veil.getHeight();
        drawTiled(batch, veil, 0, VEIL_DOWN * t);
        if (veilDrift != null) {
            driftU = Math.round(DRIFT_LEFT * t) / (float) veilDrift.getWidth();
            driftV = Math.round(DRIFT_DOWN * t) / (float) veilDrift.getHeight();
            drawTiled(batch, veilDrift, DRIFT_LEFT * t, DRIFT_DOWN * t);
        }
        end(batch);
    }

    /** The mask in its frame buffer: the radius round the ship, the edge broken by the two drifting noise fields. */
    private void renderMask(SpriteBatch batch, Sortie sortie, float alpha, double radius, double real) {
        batch.end();
        saved.set(batch.getProjectionMatrix());
        // FrameBuffer.end() binds the window, not the frame buffer that was bound before (no nesting).
        Gdx.gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, boundFramebuffer);
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, boundViewport);
        mask.begin();
        batch.setProjectionMatrix(maskProjection);
        batch.setShader(maskShader);
        batch.disableBlending();
        tongueB.bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        float falloff = (float) visibility.falloff();
        float t = (float) (real % 3600);
        batch.begin();
        maskShader.setUniformf("u_tongues", 0, -DRIFT_A * t, 0, -DRIFT_B * t);
        maskShader.setUniformf(
                "u_edge",
                (float) (radius + falloff * EDGE_OUT),
                (float) (radius - falloff * EDGE_IN),
                TONGUE_SIZE,
                (float) (TONGUE_A * falloff / 120));
        maskShader.setUniformf("u_ship", (float) sortie.ship().renderX(alpha), (float)
                sortie.ship().renderY(alpha));
        batch.setColor(Color.WHITE);
        batch.draw(tongueA, 0, 0, PlayField.WIDTH, PlayField.HEIGHT);
        batch.end();
        mask.end();
        batch.enableBlending();
        batch.setShader(null);
        Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, boundFramebuffer.get(0));
        Gdx.gl.glViewport(boundViewport.get(0), boundViewport.get(1), boundViewport.get(2), boundViewport.get(3));
        batch.setProjectionMatrix(saved);
        batch.begin();
    }

    /** The texture over the play field, wrapping, shifted by (dx, dy) px (whole px: the veil keeps its pixels). */
    private static void drawTiled(SpriteBatch batch, Texture texture, float dx, float dy) {
        float w = texture.getWidth();
        float h = texture.getHeight();
        float u = Math.round(dx) / w;
        float v = Math.round(dy) / h;
        batch.draw(
                texture,
                X0,
                0,
                PlayField.WIDTH,
                PlayField.HEIGHT,
                u,
                v,
                u + PlayField.WIDTH / w,
                v + PlayField.HEIGHT / h);
    }

    /**
     * Over the veil (the {@code drawHiddenGlows} pattern of Level 12): each open vortex's core, its glow frames at
     * {@value #CORE_THROUGH} of their light, only where the dust hides it (inside the radius its body shows it).
     */
    public void drawHiddenGlows(SpriteBatch batch, Sortie sortie, float alpha, double real) {
        if (!showing) {
            return;
        }
        begin(batch, 0, false);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.vortexPhase() != Enemy.VortexPhase.OPEN) {
                continue;
            }
            Array<AtlasRegion> core = cores.computeIfAbsent(enemy.spec(), this::coreFrames);
            float x = (float) enemy.renderX(alpha);
            float y = (float) enemy.renderY(alpha);
            if (core.isEmpty()) {
                batch.setColor(LIME.r, LIME.g, LIME.b, CORE_THROUGH);
                batch.draw(radialTexture, X0 + x - CORE_SIZE / 2, y - CORE_SIZE / 2, CORE_SIZE, CORE_SIZE);
            } else {
                batch.setColor(1, 1, 1, CORE_THROUGH);
                long frame = sortie.tick() / CORE_FRAME_TICKS + enemy.serial();
                drawCentred(batch, core.get((int) (frame % core.size)), x, y);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        end(batch);
    }

    private Array<AtlasRegion> coreFrames(EnemySpec spec) {
        String name = spec.slug() + "-core";
        return sprites.has(name) ? sprites.frames(name) : noCore;
    }

    /**
     * After the {@code air} units: each one darkened by the mask into a silhouette (its glowing pixels kept), then the
     * units' {@code -glow} frames over them (the Ghost Drone's eye and running lights), at full light everywhere.
     */
    void drawSilhouettes(SpriteBatch batch, Sortie sortie, float alpha, UnitPasses units) {
        if (!showing) {
            return;
        }
        begin(batch, 0, true);
        units.drawAirSilhouettes(batch, sortie, alpha, this);
        end(batch);
        units.drawAirGlows(batch, sortie, alpha);
    }

    /**
     * Before a silhouette's sprite: its texture's texel size for the edge's softening, set (the pending sprites drawn
     * first) when the texture changes.
     */
    void texelOf(SpriteBatch batch, Texture texture) {
        if (texture == texelOf) {
            return;
        }
        batch.flush();
        texelOf = texture;
        masked.setUniformf("u_texel", 1f / texture.getWidth(), 1f / texture.getHeight());
    }

    /**
     * A silhouette's shade over the veil's colour for a sprite pixel of luminance {@code lum} (0..1): the shader's
     * {@code u_sil.x + u_sil.y (lum - 0.5)}, a little denser dust with a trace of the unit's shading.
     */
    static double silhouetteShade(double lum) {
        return SILHOUETTE_SHADE + SILHOUETTE_FORM * (lum - 0.5);
    }

    /** Whether a sprite pixel (0..1 each) glows through the dust: the shader's rule (saturated and bright). */
    static boolean glows(double r, double g, double b) {
        double hi = Math.max(r, Math.max(g, b));
        double lo = Math.min(r, Math.min(g, b));
        double sat = (hi - lo) / Math.max(hi, 0.004);
        return sat > 0.6 && hi > 0.784;
    }

    /** Rook outside the radius: a silhouette over his craft, his running lights on its wing tips. */
    void drawWingman(SpriteBatch batch, Wingman rook, float alpha, WingmanLooks looks) {
        if (!showing || rook.ejected()) {
            return;
        }
        float x = (float) rook.renderX(alpha);
        float y = (float) rook.renderY(alpha);
        begin(batch, 0, true);
        AtlasRegion frame = looks.frame(rook);
        texelOf(batch, frame.getTexture());
        drawCentred(batch, frame, x, y);
        flushUniforms(batch, 0, false);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(PORT);
        light(batch, x - WING_TIP_X, y + WING_TIP_Y);
        batch.setColor(STARBOARD);
        light(batch, x + WING_TIP_X, y + WING_TIP_Y);
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        end(batch);
    }

    private void light(SpriteBatch batch, float x, float y) {
        batch.draw(radialTexture, X0 + x - LIGHT_SIZE / 2, y - LIGHT_SIZE / 2, LIGHT_SIZE, LIGHT_SIZE);
    }

    /** A pickup outside the radius: a faint glint over it (they are the player's to chase). */
    void drawPickupGlints(SpriteBatch batch, Sortie sortie, float alpha) {
        if (!showing || glint.isEmpty() || sortie.pickupCount() == 0) {
            return;
        }
        begin(batch, 0, false);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1, 1, 1, PICKUP_GLINT);
        for (int i = 0; i < sortie.pickupCount(); i++) {
            Pickup pickup = sortie.pickup(i);
            AtlasRegion frame = glint.get((int) ((sortie.tick() / GLINT_FRAME_TICKS + i) % glint.size));
            drawCentred(batch, frame, pickup.renderX(alpha), pickup.renderY(alpha));
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        end(batch);
    }

    /** Draws through the mask from here on: the textures where the dust hides, or the sprites' silhouettes there. */
    private void begin(SpriteBatch batch, float floor, boolean silhouette) {
        batch.setShader(masked);
        mask.getColorBufferTexture().bind(1);
        if (silhouette) {
            veil.bind(2);
            (veilDrift != null ? veilDrift : veil).bind(3);
        }
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        masked.setUniformf("u_field", X0, 0, PlayField.WIDTH, PlayField.HEIGHT);
        masked.setUniformf("u_floor", floor);
        if (silhouette) {
            masked.setUniformf("u_veilAt", veilU, veilV, driftU, driftV);
            masked.setUniformf(
                    "u_veilPer",
                    1f / veil.getWidth(),
                    1f / veil.getHeight(),
                    veilDrift != null ? 1f / veilDrift.getWidth() : 0,
                    veilDrift != null ? 1f / veilDrift.getHeight() : 0);
            masked.setUniformf("u_drifting", veilDrift != null ? 1 : 0);
            texelOf = null;
        }
        setDark(silhouette);
    }

    /** Switches the mode mid-pass (the batch's pending sprites drawn in the old one). */
    private void flushUniforms(SpriteBatch batch, float floor, boolean silhouette) {
        batch.flush();
        masked.setUniformf("u_floor", floor);
        setDark(silhouette);
    }

    private void setDark(boolean silhouette) {
        masked.setUniformf(
                "u_sil", SILHOUETTE_SHADE, SILHOUETTE_FORM, SILHOUETTE_SOFT, silhouette ? SILHOUETTE_COVER : 0);
    }

    private static void end(SpriteBatch batch) {
        batch.setShader(null);
    }

    private static ShaderProgram shader(String fragment) {
        ShaderProgram program = new ShaderProgram(VERTEX, fragment);
        if (!program.isCompiled()) {
            throw new IllegalStateException("storm shader: " + program.getLog());
        }
        return program;
    }

    /**
     * A backdrop image as a texture of its own that wraps both ways (the atlas's regions do not), copied out of its
     * atlas page; {@code fallback} draws a stand-in when the image is missing or packed in a way it cannot be copied.
     */
    private static Texture wrapped(
            Sprites sprites, String name, Map<Texture, Pixmap> pages, java.util.function.Supplier<Pixmap> fallback) {
        Pixmap pixmap = null;
        if (sprites.hasBackdrop(name)) {
            AtlasRegion region = sprites.backdrop(name).first();
            if (!region.rotate
                    && region.packedWidth == region.originalWidth
                    && region.packedHeight == region.originalHeight) {
                Pixmap page = pages.computeIfAbsent(region.getTexture(), StormLooks::pagePixmap);
                if (page != null) {
                    pixmap = new Pixmap(region.getRegionWidth(), region.getRegionHeight(), Pixmap.Format.RGBA8888);
                    pixmap.setBlending(Pixmap.Blending.None);
                    pixmap.drawPixmap(
                            page,
                            0,
                            0,
                            region.getRegionX(),
                            region.getRegionY(),
                            region.getRegionWidth(),
                            region.getRegionHeight());
                }
            }
        }
        if (pixmap == null) {
            pixmap = fallback.get();
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }

    /** An atlas page's pixels, read again from its file (a copy the caller disposes); null if they cannot be read. */
    private static Pixmap pagePixmap(Texture page) {
        TextureData data = page.getTextureData();
        try {
            if (!data.isPrepared()) {
                data.prepare();
            }
            Pixmap pixels = data.consumePixmap();
            if (data.disposePixmap()) {
                return pixels;
            }
            // The data keeps its pixmap: copy it, the caller disposes the copy.
            Pixmap copy = new Pixmap(pixels.getWidth(), pixels.getHeight(), pixels.getFormat());
            copy.setBlending(Pixmap.Blending.None);
            copy.drawPixmap(pixels, 0, 0);
            return copy;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** A stand-in noise field: smooth value noise, 64 px a cell, wrapping in its 256 px tile, in its alpha. */
    private static Pixmap noise(long seed) {
        int size = (int) TONGUE_SIZE;
        int cells = 4;
        double[] grid = new double[cells * cells];
        Random random = new Random(seed);
        for (int i = 0; i < grid.length; i++) {
            grid[i] = random.nextDouble();
        }
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        double cell = size / (double) cells;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int cx = (int) (x / cell);
                int cy = (int) (y / cell);
                double fx = smooth(x / cell - cx);
                double fy = smooth(y / cell - cy);
                double top = lerp(grid[cy * cells + cx], grid[cy * cells + (cx + 1) % cells], fx);
                double bottom = lerp(
                        grid[(cy + 1) % cells * cells + cx], grid[(cy + 1) % cells * cells + (cx + 1) % cells], fx);
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, (float) lerp(top, bottom, fy)));
            }
        }
        return pixmap;
    }

    private static double smooth(double x) {
        return x * x * (3 - 2 * x);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** A stand-in veil: look b's middle rust. */
    private static Pixmap flatVeil() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(176 / 255f, 118 / 255f, 74 / 255f, 1);
        pixmap.fill();
        return pixmap;
    }

    /** A white disc fading from its centre to its rim (the running lights, a core without its frames). */
    private static Texture radialTexture() {
        Pixmap pixmap = new Pixmap(RADIAL_SIZE, RADIAL_SIZE, Pixmap.Format.RGBA8888);
        double half = RADIAL_SIZE / 2.0;
        for (int y = 0; y < RADIAL_SIZE; y++) {
            for (int x = 0; x < RADIAL_SIZE; x++) {
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

    @Override
    public void dispose() {
        mask.dispose();
        maskShader.dispose();
        masked.dispose();
        tongueA.dispose();
        tongueB.dispose();
        veil.dispose();
        if (veilDrift != null) {
            veilDrift.dispose();
        }
        radialTexture.dispose();
    }
}
