package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.Arrays;
import vanguard.sim.GateTransit;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part H, step H4 (design/campaign Level 50, the closing gate; m11h-gaps.md §1 #11, #18; ART-H2's kit,
 * tools/art/act7_core.py, its numbers the proposal's {@code closing_gate} block in {@code
 * level-50-choir-heart/concept/backdrop-proposal.yaml}, a look only: constants here, no data key): the scene after the
 * Choir Heart's K, drawn over the section tiles in the behind pass (its layers change faster than a tile set's seam
 * allows), timed by the transit's clock from K ({@link GateTransit#sinceKillSeconds}):
 *
 * <ul>
 *   <li>K+10.5: the hive greying from the dead Heart outward over 3 s ({@code arena-grey} over the deep field round the
 *       halted arena, the Heart's collapse greyed the same way), each pixel at its {@code grey-mask} threshold R:
 *       {@code smoothstep(R - 0.08, R + 0.08, p x 1.16 - 0.08)}, p 0 to 1 ({@link MaskShader});
 *   <li>from K+12 the race's one grey strip ({@code race-1..5}, stored at half size, drawn at 2x) coming in from the
 *       top as the scroll starts, the event plane ahead growing while its rim contracts, the <i>Steadfast</i>'s group's
 *       lights fading into it, the folds closing behind in a 40 px band on the bottom edge (8 fps);
 *   <li>K+29 to K+30.5: the warp home ({@link TransitWarp} reversed): the near side unbending from 0.6 under the race,
 *       which fades out over it (the review's luminance one way: no flash, no inversion);
 *   <li>from K+30.5 the near side: the near space at the deep's factor, Neptune, the ring's upper arc with its mouth's
 *       plane shrinking to a point and going out (K+31-35.6), the glyphs going dark group by group on {@code GLYPH_DARK}
 *       (0.3 s each, {@link MaskShader}), the <i>Steadfast</i>'s group with its lights.
 * </ul>
 *
 * Positions are the kit's (y down from the field's top) turned y up. Nothing is allocated per frame once loaded.
 */
final class ClosingGateLooks implements Disposable {
    /** The scene's times from K, s. */
    static final double GREYING_FROM = 10.5;

    static final double GREYING_SECONDS = 3;
    static final double GREYING_BAND = 0.08;
    static final double RACE_FROM = 12;
    static final double FOLDS_FROM = 13;
    static final double WARP_FROM = 29;
    static final double WARP_SECONDS = 1.5;
    static final double NEAR_FROM = 30.5;
    /** The warp home's strength at its start (the near side unbending from it). */
    static final double WARP_STRENGTH = 0.6;
    /** The arena's grey: its height and how far its top lies above the screen at the halt, px. */
    static final int ARENA_GREY_TOP = -80;
    /** The deep layer's scroll factor (the hive's grey and the near side move with it). */
    static final double DEEP = 0.12;
    /** The race strip's pieces' height on the screen, px. */
    static final int RACE_HEIGHT = 1152;
    /** The plane over the race: its radius 40 to 240 px over 18.5 s, its rim 0.65 of it from K+16 over 13 s. */
    static final double PLANE_GROW = 18.5;

    static final double PLANE_DISC_RADIUS = 244;
    /** The folds' band, px, and their rise into it, s, and frame rate. */
    static final double FOLD_BAND = 40;

    static final double FOLD_RISE = 2;
    static final double FOLD_FPS = 8;
    /** The near side: Neptune's place, the ring's top, its mouth's centre in its image and radius, the group's place. */
    static final double[] NEPTUNE = {380, 90};

    static final double RING_TOP = 110;
    static final double RING_MOUTH_Y = 390;
    static final double RING_MOUTH_RADIUS = 300 - 56;
    static final double[] GROUP = {240, 64};
    /** The plane in the ring's mouth: shut over 4 s from K+31, its last 0.6 s fading. */
    static final double PLANE_OUT_FROM = 31;

    static final double PLANE_OUT_SECONDS = 4;
    static final double PLANE_OUT_FADE = 0.6;
    /** A glyph group's fade, s, and the groups. */
    static final double GLYPH_FADE = 0.3;

    static final int GLYPH_GROUPS = 8;

    private final AtlasRegion arenaGrey;
    private final AtlasRegion greyMask;
    private final Array<AtlasRegion> race = new Array<>();
    private final Array<AtlasRegion> folds;
    private final AtlasRegion planeDisc;
    private final AtlasRegion planeRim;
    private final AtlasRegion group;
    private final Array<AtlasRegion> groupGlow;
    private final AtlasRegion nearSpace;
    private final AtlasRegion neptune;
    private final AtlasRegion neptuneGlow;
    private final AtlasRegion ring;
    private final AtlasRegion ringGlyphs;
    private final AtlasRegion glyphOrder;

    private final MaskShader mask = new MaskShader();
    private final TransitWarp warp = new TransitWarp();
    private final TextureRegion part = new TextureRegion();

    /** The ground scroll at K, at K+12 and at K+30.5 (NaN before). */
    private double groundAtKill = Double.NaN;

    private double groundAtRace = Double.NaN;
    private double groundAtNear = Double.NaN;
    /** Per glyph group (1-8) its {@code GLYPH_DARK}'s time from K (a far time before). */
    private final float[] glyphAt = new float[GLYPH_GROUPS];

    private ClosingGateLooks(Sprites sprites, String folder) {
        arenaGrey = one(sprites, folder + "arena-grey");
        greyMask = one(sprites, folder + "grey-mask");
        for (int k = 1; sprites.hasBackdrop(folder + "race-" + k); k++) {
            race.add(one(sprites, folder + "race-" + k));
        }
        folds = sprites.hasBackdrop(folder + "fold") ? sprites.backdrop(folder + "fold") : new Array<>(0);
        planeDisc = one(sprites, folder + "plane-disc");
        planeRim = one(sprites, folder + "plane-rim");
        group = one(sprites, folder + "steadfast-group");
        groupGlow = sprites.hasBackdrop(folder + "steadfast-group-glow")
                ? sprites.backdrop(folder + "steadfast-group-glow")
                : new Array<>(0);
        nearSpace = one(sprites, folder + "near-space");
        neptune = one(sprites, folder + "neptune");
        neptuneGlow = one(sprites, folder + "neptune-glow");
        ring = one(sprites, folder + "ring-arc");
        ringGlyphs = one(sprites, folder + "ring-arc-glyphs");
        glyphOrder = one(sprites, folder + "glyph-order");
        reset();
    }

    private static AtlasRegion one(Sprites sprites, String name) {
        return sprites.hasBackdrop(name) ? sprites.backdrop(name).first() : null;
    }

    /**
     * The scene's looks for a level ending in a reversed transit timed from its boss's K (Level 50) whose backdrop holds
     * the kit; null for any other.
     */
    static ClosingGateLooks of(Sprites sprites, LevelScript script, String folder) {
        boolean scene = script.vrell()
                .transit()
                .filter(t -> t.reverse() && t.fromKill())
                .isPresent();
        return sprites != null && scene && sprites.hasBackdrop(folder + "arena-grey")
                ? new ClosingGateLooks(sprites, folder)
                : null;
    }

    /** The level restarts: the scene has not begun. */
    void reset() {
        groundAtKill = groundAtRace = groundAtNear = Double.NaN;
        Arrays.fill(glyphAt, 1e6f);
    }

    /** After each step: the scrolls at the scene's marks, the glyphs' events. */
    void stepped(Sortie sortie) {
        GateTransit transit = sortie.transit();
        double tk = transit == null ? -1 : transit.sinceKillSeconds();
        if (tk < 0) {
            if (!Double.isNaN(groundAtKill)) {
                reset();
            }
            return;
        }
        double ground = sortie.groundScroll();
        if (Double.isNaN(groundAtKill)) {
            groundAtKill = ground;
        }
        if (tk >= RACE_FROM && Double.isNaN(groundAtRace)) {
            groundAtRace = ground;
        }
        if (tk >= NEAR_FROM && Double.isNaN(groundAtNear)) {
            groundAtNear = ground;
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.GLYPH_DARK) {
                int n = events.value(i);
                if (n >= 1 && n <= GLYPH_GROUPS) {
                    glyphAt[n - 1] = (float) tk;
                }
            }
        }
    }

    /** The scene's time from K at the frame being drawn ({@code alpha} into the step); negative before K. */
    static double sinceKill(Sortie sortie, float alpha) {
        GateTransit transit = sortie.transit();
        double tk = transit == null ? -1 : transit.sinceKillSeconds();
        return tk < 0 ? -1 : tk - (1 - alpha) * SimStep.SECONDS;
    }

    static double smooth01(double x) {
        double c = Math.clamp(x, 0, 1);
        return c * c * (3 - 2 * c);
    }

    /** The greying's progress at {@code tk}, 0-1. */
    static double greying(double tk) {
        return Math.clamp((tk - GREYING_FROM) / GREYING_SECONDS, 0, 1);
    }

    /** The warp home's share at {@code tk}: 0 before it, 0-1 across it, 1 after. */
    static double warpShare(double tk) {
        return smooth01((tk - WARP_FROM) / WARP_SECONDS);
    }

    /** The warp's strength over the near side at {@code tk}: 0.6 at its start, unbending to 0 (0 outside it). */
    static double warpStrength(double tk) {
        return tk < WARP_FROM || tk >= NEAR_FROM ? 0 : WARP_STRENGTH * (1 - warpShare(tk));
    }

    /**
     * The scene in the behind pass, over the section tiles and under everything else: the dead Heart from K ({@code
     * heart}, its collapse greyed from K+10.5), the hive greying, the race, the warp home, the near side.
     */
    void drawBehind(SpriteBatch batch, Sortie sortie, float alpha, double scroll, HeartLooks heart) {
        double tk = sinceKill(sortie, alpha);
        if (tk < 0) {
            return;
        }
        if (tk < WARP_FROM) {
            drawFar(batch, sortie, alpha, tk, scroll, heart, 1);
            return;
        }
        drawNear(batch, tk, scroll);
        if (tk < NEAR_FROM) {
            // The warp home: the near side unbending under the race, which fades out over it.
            warp.apply(batch, warpStrength(tk));
            drawFar(batch, sortie, alpha, tk, scroll, heart, (float) (1 - warpShare(tk)));
        }
    }

    private void drawFar(
            SpriteBatch batch, Sortie sortie, float alpha, double tk, double scroll, HeartLooks heart, float a) {
        double dy = Double.isNaN(groundAtKill) ? 0 : (scroll - groundAtKill) * DEEP;
        float p = (float) greying(tk);
        if (p > 0 && arenaGrey != null && greyMask != null) {
            // Its top row ARENA_GREY_TOP px above the field's top edge at the halt, riding the deep layer after it.
            float bottom = (float) (PlayField.HEIGHT - (ARENA_GREY_TOP + dy + arenaGrey.getRegionHeight()));
            mask.greying(batch, arenaGrey, greyMask, 0, Math.round(bottom), p, false, a);
        }
        if (heart != null) {
            heart.drawDead(batch, sortie, alpha, scroll, a, p > 0 ? this : null, p);
        }
        if (tk >= RACE_FROM && !Double.isNaN(groundAtRace)) {
            drawRace(batch, scroll - groundAtRace, a);
        }
        drawPlane(batch, tk, a);
        if (tk >= FOLDS_FROM && !folds.isEmpty()) {
            double rise = FOLD_BAND * smooth01((tk - FOLDS_FROM) / FOLD_RISE);
            AtlasRegion fold = folds.get((int) Math.floor(tk * FOLD_FPS) % folds.size);
            batch.setColor(1, 1, 1, a);
            // Centred FOLD_BAND + 100 px below the field's bottom edge, raised by its rise (the kit's anchor).
            float y = (float) (rise - fold.getRegionHeight());
            batch.draw(fold, X0, Math.round(y));
        }
        batch.setColor(1, 1, 1, 1);
    }

    /** The Heart's collapse frame greyed from the Heart outward (its screen rectangle on the mask), for {@link HeartLooks}. */
    void greyHeart(SpriteBatch batch, TextureRegion frame, float x, float y, float p, float a) {
        mask.greying(batch, frame, greyMask, x, y, p, true, a);
    }

    /** The race's strip: row z of it at screen y (down) {@code offset - z}; race-1 lowest. */
    private void drawRace(SpriteBatch batch, double offset, float a) {
        if (race.isEmpty()) {
            return;
        }
        Texture texture = race.first().getTexture();
        Texture.TextureFilter min = texture.getMinFilter();
        Texture.TextureFilter mag = texture.getMagFilter();
        batch.flush();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        batch.setColor(1, 1, 1, a);
        for (int k = 0; k < race.size; k++) {
            float bottom = (float) (PlayField.HEIGHT - offset + (double) k * RACE_HEIGHT);
            if (bottom >= PlayField.HEIGHT || bottom + RACE_HEIGHT <= 0) {
                continue;
            }
            AtlasRegion piece = race.get(k);
            batch.draw(piece, X0, Math.round(bottom), PlayField.WIDTH, RACE_HEIGHT);
        }
        batch.flush();
        texture.setFilter(min, mag);
        batch.setColor(1, 1, 1, 1);
    }

    /** The event plane ahead over the race, growing while its rim contracts; the group's lights fading into it. */
    private void drawPlane(SpriteBatch batch, double tk, float a) {
        if (tk < GREYING_FROM || planeDisc == null) {
            return;
        }
        double grow = smooth01((tk - GREYING_FROM) / PLANE_GROW);
        double radius = 40 + 200 * grow;
        double rim = radius * (1 - 0.35 * smooth01((tk - 16) / 13.0));
        double cy = PlayField.HEIGHT - (-20 + 150 * grow);
        float in = (float) Math.min(1, tk - GREYING_FROM) * a;
        if (tk < 20.5 && group != null) {
            float fade = (float) (1 - smooth01((tk - 17.5) / 3.0)) * in;
            double gy = cy - (rim * 1.15 + 30 * (1 - grow));
            batch.setColor(1, 1, 1, fade);
            scaled(batch, group, 240 + 30 * Math.sin(tk * 0.3), gy, 0.35);
        }
        batch.setColor(1, 1, 1, in);
        scaled(batch, planeDisc, 240, cy, rim / PLANE_DISC_RADIUS);
        if (planeRim != null) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            scaled(batch, planeRim, 240, cy, rim / PLANE_DISC_RADIUS);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        batch.setColor(1, 1, 1, 1);
    }

    /** The near side from K+30.5 (and under the warp home before it). */
    private void drawNear(SpriteBatch batch, double tk, double scroll) {
        double dy = Double.isNaN(groundAtNear) ? 0 : (scroll - groundAtNear) * DEEP;
        if (nearSpace != null) {
            // The near space wraps at the deep layer's factor: its rows ride the scroll.
            int h = nearSpace.getRegionHeight();
            int offset = (int) Math.floorMod(Math.round(scroll * DEEP), (long) h);
            for (int y = -offset; y < PlayField.HEIGHT; y += h) {
                batch.draw(nearSpace, X0, y);
            }
        }
        double ny = PlayField.HEIGHT - (NEPTUNE[1] + dy * 0.3);
        if (neptuneGlow != null) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            PlayFrame.drawCentred(batch, neptuneGlow, NEPTUNE[0], ny);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        if (neptune != null) {
            PlayFrame.drawCentred(batch, neptune, NEPTUNE[0], ny);
        }
        if (ring != null) {
            double shut = smooth01((tk - PLANE_OUT_FROM) / PLANE_OUT_SECONDS);
            double radius = RING_MOUTH_RADIUS * (1 - shut);
            double mouth = PlayField.HEIGHT - (RING_TOP + RING_MOUTH_Y + dy);
            if (radius > 0.6 && planeDisc != null) {
                float fade = (float)
                        (1 - smooth01((tk - (PLANE_OUT_FROM + PLANE_OUT_SECONDS - PLANE_OUT_FADE)) / PLANE_OUT_FADE));
                batch.setColor(1, 1, 1, fade);
                scaled(batch, planeDisc, 240, mouth, radius / PLANE_DISC_RADIUS);
                if (planeRim != null) {
                    batch.setColor(1, 1, 1, 0.6f * fade);
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    scaled(batch, planeRim, 240, mouth, radius / PLANE_DISC_RADIUS);
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
                batch.setColor(1, 1, 1, 1);
            }
            float ringBottom = (float) (PlayField.HEIGHT - (RING_TOP + ring.getRegionHeight() + dy));
            batch.draw(ring, X0, Math.round(ringBottom));
            if (ringGlyphs != null && glyphOrder != null) {
                mask.glyphs(
                        batch, ringGlyphs, glyphOrder, Math.round(ringBottom), (float) tk, glyphAt, (float) GLYPH_FADE);
            }
        }
        if (group != null) {
            double gy = PlayField.HEIGHT - (GROUP[1] - dy * 2);
            PlayFrame.drawCentred(batch, group, GROUP[0], gy);
            if (!groupGlow.isEmpty()) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                PlayFrame.drawCentred(batch, groupGlow.get((int) Math.floor(tk * 4) % groupGlow.size), GROUP[0], gy);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    /** {@code region} centred at (x, y up) at {@code scale}. */
    private static void scaled(SpriteBatch batch, TextureRegion region, double x, double y, double scale) {
        float w = (float) (region.getRegionWidth() * scale);
        float h = (float) (region.getRegionHeight() * scale);
        batch.draw(region, (float) (X0 + x - w / 2), (float) (y - h / 2), w, h);
    }

    @Override
    public void dispose() {
        mask.dispose();
        warp.dispose();
    }

    /**
     * A shader drawing a region by a mask image's red channel R (a second texture): the greying ({@code smoothstep(R -
     * band, R + band, p x 1.16 - 0.08)}, the mask fixed on the screen, optionally the region's colour turned grey) or
     * the glyphs' groups (group {@code round(R x 8)} fading over its fade from its time). Its alpha times the batch
     * colour's; nothing is allocated per draw.
     */
    static final class MaskShader implements Disposable {
        private static final String VERTEX = """
                attribute vec4 a_position;
                attribute vec4 a_color;
                attribute vec2 a_texCoord0;
                uniform mat4 u_projTrans;
                varying vec4 v_color;
                varying vec2 v_texCoords;
                void main() {
                    v_color = a_color;
                    v_color.a = v_color.a * (255.0 / 254.0);
                    v_texCoords = a_texCoord0;
                    gl_Position = u_projTrans * a_position;
                }
                """;
        private static final String FRAGMENT = """
                #ifdef GL_ES
                precision mediump float;
                #endif
                varying vec4 v_color;
                varying vec2 v_texCoords;
                uniform sampler2D u_texture;
                uniform sampler2D u_mask;
                uniform vec4 u_region;
                uniform vec4 u_maskRegion;
                uniform vec4 u_affine;
                uniform float u_mode;
                uniform float u_p;
                uniform float u_band;
                uniform float u_grey;
                uniform float u_fade;
                uniform float u_glyphAt[8];
                const vec3 WEIGHTS = vec3(0.2126, 0.7152, 0.0722);
                void main() {
                    vec4 c = texture2D(u_texture, v_texCoords) * v_color;
                    vec2 local = (v_texCoords - u_region.xy) / u_region.zw;
                    vec2 m = vec2(local.x * u_affine.x + u_affine.y, local.y * u_affine.z + u_affine.w);
                    float inside = step(0.0, m.x) * step(m.x, 1.0) * step(0.0, m.y) * step(m.y, 1.0);
                    float r = mix(1.0, texture2D(u_mask, u_maskRegion.xy + clamp(m, 0.0, 1.0) * u_maskRegion.zw).r, inside);
                    float a;
                    if (u_mode < 0.5) {
                        a = smoothstep(r - u_band, r + u_band, u_p * 1.16 - 0.08);
                    } else {
                        float n = floor(r * 8.0 + 0.5);
                        float at = 1000000.0;
                        for (int i = 0; i < 8; i++) {
                            if (abs(float(i + 1) - n) < 0.5) {
                                at = u_glyphAt[i];
                            }
                        }
                        a = 1.0 - clamp((u_p - at) / u_fade, 0.0, 1.0);
                    }
                    if (u_grey > 0.5) {
                        float l = dot(c.rgb, WEIGHTS);
                        c.rgb = vec3(l * 0.95, l * 0.94, l);
                    }
                    gl_FragColor = vec4(c.rgb, c.a * a);
                }
                """;

        private ShaderProgram program;

        private ShaderProgram program() {
            if (program == null) {
                program = new ShaderProgram(VERTEX, FRAGMENT);
                if (!program.isCompiled()) {
                    throw new IllegalStateException("closing gate mask shader: " + program.getLog());
                }
            }
            return program;
        }

        /**
         * {@code region} with its bottom left at (x, y up) in field px, greyed in by {@code mask} (fixed on the field)
         * at progress {@code p}; turned grey when {@code grey}; at alpha {@code a}.
         */
        void greying(
                SpriteBatch batch,
                TextureRegion region,
                TextureRegion mask,
                float x,
                float y,
                float p,
                boolean grey,
                float a) {
            if (region == null || mask == null) {
                return;
            }
            float w = region.getRegionWidth();
            float h = region.getRegionHeight();
            float top = PlayField.HEIGHT - (y + h);
            begin(batch, region, mask);
            program.setUniformf(
                    "u_affine", w / PlayField.WIDTH, x / PlayField.WIDTH, h / PlayField.HEIGHT, top / PlayField.HEIGHT);
            program.setUniformf("u_mode", 0);
            program.setUniformf("u_p", p);
            program.setUniformf("u_band", (float) GREYING_BAND);
            program.setUniformf("u_grey", grey ? 1 : 0);
            batch.setColor(1, 1, 1, a);
            batch.draw(region, X0 + x, y);
            end(batch);
        }

        /** The glyphs over the ring (their image's bottom at {@code y}), each group by {@code mask}'s red fading at its time. */
        void glyphs(
                SpriteBatch batch,
                TextureRegion glyphs,
                TextureRegion mask,
                float y,
                float tk,
                float[] at,
                float fade) {
            begin(batch, glyphs, mask);
            program.setUniformf("u_affine", 1, 0, 1, 0);
            program.setUniformf("u_mode", 1);
            program.setUniformf("u_p", tk);
            program.setUniformf("u_fade", fade);
            program.setUniformf("u_grey", 0);
            program.setUniform1fv("u_glyphAt", at, 0, at.length);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.draw(glyphs, X0, y);
            end(batch);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        private void begin(SpriteBatch batch, TextureRegion region, TextureRegion mask) {
            batch.flush();
            mask.getTexture().bind(1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            batch.setShader(program());
            program.setUniformi("u_mask", 1);
            program.setUniformf(
                    "u_region",
                    region.getU(),
                    region.getV(),
                    region.getU2() - region.getU(),
                    region.getV2() - region.getV());
            program.setUniformf(
                    "u_maskRegion", mask.getU(), mask.getV(), mask.getU2() - mask.getU(), mask.getV2() - mask.getV());
        }

        private static void end(SpriteBatch batch) {
            batch.flush();
            batch.setShader(null);
            batch.setColor(1, 1, 1, 1);
        }

        @Override
        public void dispose() {
            if (program != null) {
                program.dispose();
            }
        }
    }
}
