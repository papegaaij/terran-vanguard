package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.PlayField;

/**
 * Round 44 K.b "something extra" (step GK; tools/art/dust_colossus.py's throe sets, briefs/l21-art-names.md "Round 44:
 * the death throe"; pending the user's review in round 45): the Dust Colossus's {@value #THROE_SECONDS} s death throe
 * from the kill, all at the mouth's place at the kill and scrolling with the ground (the simulation's death chain stays
 * 3 s). Its forebody rears up out of the sand and screams ({@code -rear}), collapses ({@code -collapse}) and sinks (the
 * death set's last two frames); a chain of geysers rolls along the split ridge from the tail to the head ({@code
 * -geyser}); a ring of fissures opens round the mouth and stays with the collapsed crater ({@code -fissure}); rocks
 * rain over the field, each landing in a puff of dust ({@code -debris}, the ridge's dust); a dust plume climbs and
 * spreads as the body sinks ({@code -plume}); a shockwave ring runs out as it goes under, the play field shakes (within
 * the screen-shake setting, {@link #shake}), and the white flash ({@value #FLASH_SECONDS} s) and the credit shower
 * ({@value #SHOWER_SECONDS} s) close it. Pure functions of the time since the kill plus the drawing; nothing is
 * allocated per frame.
 */
final class ColossusThroe {
    /** The throe's length from the kill, s: the credit shower at its end. */
    static final double THROE_SECONDS = 4.5;
    /** The white flash starts this long after the kill, s; the credit shower this long. */
    static final double FLASH_SECONDS = 4.2;

    static final double SHOWER_SECONDS = THROE_SECONDS;
    /** The body's sets: the rearing forebody, its collapse, the death set (its sinking frames). */
    static final int REAR = 0;

    static final int COLLAPSE = 1;
    static final int DEATH = 2;
    /** The body's frames in order, each until its end time (s since the kill): set * 8 + frame. */
    static final int[] BODY_FRAMES = {
        REAR * 8,
        REAR * 8 + 1,
        REAR * 8 + 2,
        REAR * 8 + 3,
        REAR * 8 + 2,
        REAR * 8 + 3,
        COLLAPSE * 8,
        COLLAPSE * 8 + 1,
        COLLAPSE * 8 + 2,
        DEATH * 8 + 4,
        DEATH * 8 + 5
    };

    static final double[] BODY_ENDS = {0.30, 0.65, 1.10, 1.35, 1.60, 1.85, 2.20, 2.55, 2.90, 3.25, 3.60};
    /** The geysers along the ridge: at most this many, the first's start and the spread to the last's, s. */
    static final int GEYSERS = 9;

    static final double GEYSER_START = 0.5;
    static final double GEYSER_SPREAD = 1.5;
    static final double GEYSER_FRAME_SECONDS = 0.09;
    static final int GEYSER_FRAMES = 8;
    /**
     * Step GFX4 (H6's capture: the geysers did not read): each drawn this much larger, growing by the share over its
     * frames; under it its frame in dark sand this much larger still for its first {@value #GEYSER_BASE_FRAMES} frames
     * (the churned base, fading); over it its frame in lime, additive, this much of its size (the core, fading out by
     * {@value #GEYSER_CORE_FRAMES} frames).
     */
    static final float GEYSER_SCALE = 1.45f;

    static final float GEYSER_GROWTH = 0.25f;
    static final float GEYSER_BASE_SCALE = 1.15f;
    static final float GEYSER_BASE_ALPHA = 0.8f;
    static final int GEYSER_BASE_FRAMES = 5;
    static final float GEYSER_CORE_SCALE = 0.42f;
    static final float GEYSER_CORE_ALPHA = 0.6f;
    static final int GEYSER_CORE_FRAMES = 5;
    /** How far the core's frame is recoloured to the lime (the rest its own shading). */
    static final float GEYSER_CORE_TINT = 0.75f;
    /** Step GFX4: a geyser's layers, drawn for all of them in turn: the dark base, the jet, the lime core. */
    static final int BASE = 0;

    static final int JET = 1;
    static final int CORE = 2;

    /** Without a ridge to carry them, the geysers' ring round the mouth: its radius, px (outside the fissures). */
    static final double GEYSER_RING = 130;
    /**
     * A ridge shorter than this many points, or reaching less than this far from the mouth's place, px (step GFX4:
     * hidden under the rearing body, about 95 px), has the geysers round the mouth instead.
     */
    static final int GEYSER_LEAST_POINTS = 3;

    static final double GEYSER_LEAST_REACH = 140;
    /** The fissure ring's frames' starts, s; the last stays. */
    static final double[] FISSURE_STARTS = {0.9, 1.2, 1.5, 1.9};
    /**
     * The dust plume: its frames' starts (the last held to its end), its growth, its fade's start and its end (step
     * GFX4: past the throe, the dust hanging over the sinkhole), its dense core's and heart's scales and its shadow's
     * offset, px, and opacity.
     */
    static final double[] PLUME_STARTS = {1.9, 2.7, 3.5};

    static final double PLUME_GROWTH = 1.0;
    static final double PLUME_FADE = 4.6;
    static final double PLUME_END = 6.5;
    static final float PLUME_CORE = 0.7f;
    static final float PLUME_HEART = 0.45f;
    static final double PLUME_SHADOW_X = 10;
    static final double PLUME_SHADOW_Y = -14;
    static final float PLUME_SHADOW_ALPHA = 0.55f;
    /** The collapsed crater and the furrow fade in over this stretch, s. */
    static final double SETTLE_START = 3.3;

    static final double SETTLE_END = 3.9;
    /** The rock rain: how many, when they are born, their fall, its drift down the screen and their dust puff. */
    static final int ROCKS = 18;

    static final double ROCK_FIRST = 0.7;
    static final double ROCK_LAST = 3.0;
    static final double ROCK_FALL_SECONDS = 0.9;
    static final double ROCK_DRIFT = 40;
    static final float ROCK_HIGH_SCALE = 1.6f;
    static final double PUFF_SECONDS = 0.4;
    static final double ROCK_TUMBLE_FPS = 10;
    static final int ROCK_SHAPES = 4;
    static final int ROCK_TUMBLE = 4;
    /** The shockwave ring as the body goes under: its start, s, its run, its reach and width, px. */
    static final double SHOCK_AT = 2.9;

    static final double SHOCK_SECONDS = 0.7;
    static final double SHOCK_RADIUS = 300;
    static final double SHOCK_WIDTH = 10;
    static final int SHOCK_SEGMENTS = 96;
    /** The play field's shake at its strongest and the full setting, px (the ridge pass's is 2). */
    static final double SHAKE_PX = 7;

    private static final Color SHOCK = new Color(0.95f, 0.84f, 0.66f, 1f);
    private static final Color SHOCK_EDGE = new Color(0.30f, 0.20f, 0.12f, 1f);
    /** Step GFX4: the geysers' churned base and their core (the Vrell lime of the cracks). */
    private static final Color SAND_DARK = new Color(0.24f, 0.15f, 0.09f, 1f);

    private static final Color LIME = Color.valueOf("A8FF3A");

    private final Array<AtlasRegion> rear;
    private final Array<AtlasRegion> collapse;
    private final Array<AtlasRegion> death;
    private final Array<AtlasRegion> geyser;
    private final Array<AtlasRegion> fissure;
    private final Array<AtlasRegion> plume;
    private final Array<AtlasRegion> debris;
    private final Array<AtlasRegion> dust;
    private final TextureRegion pixel;
    private final FlashShader flash;

    ColossusThroe(
            Array<AtlasRegion> rear,
            Array<AtlasRegion> collapse,
            Array<AtlasRegion> death,
            Array<AtlasRegion> geyser,
            Array<AtlasRegion> fissure,
            Array<AtlasRegion> plume,
            Array<AtlasRegion> debris,
            Array<AtlasRegion> dust,
            TextureRegion pixel,
            FlashShader flash) {
        this.rear = rear;
        this.collapse = collapse;
        this.death = death;
        this.geyser = geyser;
        this.fissure = fissure;
        this.plume = plume;
        this.debris = debris;
        this.dust = dust;
        this.pixel = pixel;
        this.flash = flash;
    }

    // --- the timeline ------------------------------------------------------------------------------

    /** The body's frame {@code age} s after the kill (set * 8 + frame, {@link #BODY_FRAMES}); -1 before and after. */
    static int bodyFrame(double age) {
        if (age < 0) {
            return -1;
        }
        for (int k = 0; k < BODY_ENDS.length; k++) {
            if (age < BODY_ENDS[k]) {
                return BODY_FRAMES[k];
            }
        }
        return -1;
    }

    /** Whether the geysers roll along a ridge of {@code points} points reaching {@code reach} px from the mouth. */
    static boolean alongRidge(int points, double reach) {
        return points >= GEYSER_LEAST_POINTS && reach >= GEYSER_LEAST_REACH;
    }

    /** How many geysers a ridge of {@code points} points gets. */
    static int geysers(int points) {
        return Math.min(GEYSERS, points);
    }

    /** Geyser {@code j}'s (0 the tail's) point of {@code points} (head first), spread evenly from the tail to the head. */
    static int geyserPoint(int j, int count, int points) {
        if (count <= 1) {
            return 0;
        }
        return points - 1 - (int) Math.round((double) j * (points - 1) / (count - 1));
    }

    /** Geyser {@code j}'s (0 the tail's) start of {@code count}, s since the kill: tail to head over the spread. */
    static double geyserStart(int j, int count) {
        return GEYSER_START + (count <= 1 ? GEYSER_SPREAD : GEYSER_SPREAD * j / (count - 1));
    }

    /** Geyser {@code j}'s frame of {@code count} {@code age} s after the kill; -1 before it and once it has played. */
    static int geyserFrame(int j, int count, double age) {
        double into = age - geyserStart(j, count);
        if (into < 0) {
            return -1;
        }
        int frame = (int) (into / GEYSER_FRAME_SECONDS);
        return frame < GEYSER_FRAMES ? frame : -1;
    }

    /** The fissure ring's frame {@code age} s after the kill; -1 before it opens, the last one from then on. */
    static int fissureFrame(double age) {
        for (int k = FISSURE_STARTS.length - 1; k >= 0; k--) {
            if (age >= FISSURE_STARTS[k]) {
                return k;
            }
        }
        return -1;
    }

    /** The plume's frame {@code age} s after the kill; -1 before it and from its end. */
    static int plumeFrame(double age) {
        if (age >= PLUME_END) {
            return -1;
        }
        for (int k = PLUME_STARTS.length - 1; k >= 0; k--) {
            if (age >= PLUME_STARTS[k]) {
                return k;
            }
        }
        return -1;
    }

    /** The plume's scale: 1 at its start, growing by {@link #PLUME_GROWTH} to its end. */
    static float plumeScale(double age) {
        double u = Math.clamp((age - PLUME_STARTS[0]) / (PLUME_END - PLUME_STARTS[0]), 0, 1);
        return (float) (1 + PLUME_GROWTH * u);
    }

    /** The plume's opacity: full until {@link #PLUME_FADE}, then fading to nothing at its end. */
    static float plumeAlpha(double age) {
        return (float) Math.clamp((PLUME_END - age) / (PLUME_END - PLUME_FADE), 0, 1);
    }

    /** A geyser's scale at its {@code frame}: {@link #GEYSER_SCALE}, growing by {@link #GEYSER_GROWTH} over it. */
    static float geyserScale(int frame) {
        return GEYSER_SCALE * (1 + GEYSER_GROWTH * frame / (GEYSER_FRAMES - 1));
    }

    /** A geyser's dark base's opacity at its {@code frame}: fading out over its first frames. */
    static float geyserBaseAlpha(int frame) {
        return Math.max(0, GEYSER_BASE_ALPHA * (1 - (float) frame / GEYSER_BASE_FRAMES));
    }

    /** A geyser's lime core's opacity at its {@code frame}: full for its first half, then fading out. */
    static float geyserCoreAlpha(int frame) {
        int full = GEYSER_CORE_FRAMES / 2;
        return frame < full
                ? GEYSER_CORE_ALPHA
                : Math.max(0, GEYSER_CORE_ALPHA * (1 - (float) (frame - full) / (GEYSER_CORE_FRAMES - full)));
    }

    /** How far the collapsed crater and the furrow have faded in (0–1). */
    static float settled(double age) {
        return (float) Math.clamp((age - SETTLE_START) / (SETTLE_END - SETTLE_START), 0, 1);
    }

    /**
     * The shake's strength (0–1) {@code age} s after the kill: the rearing body's shudder growing, the collapse
     * strong, the plunge under the sand the strongest kick, dying away, and a last kick at the flash.
     */
    static double shake(double age) {
        if (age < 0 || age >= FLASH_SECONDS + 0.45) {
            return 0;
        }
        if (age < BODY_ENDS[5]) {
            return 0.3 + 0.2 * age / BODY_ENDS[5];
        }
        if (age < SHOCK_AT) {
            return 0.75;
        }
        if (age < SETTLE_START + 0.3) {
            return 1 - 0.8 * (age - SHOCK_AT) / (SETTLE_START + 0.3 - SHOCK_AT);
        }
        if (age < FLASH_SECONDS) {
            return 0.2 * (FLASH_SECONDS - age) / (FLASH_SECONDS - SETTLE_START - 0.3);
        }
        return 0.5 * (1 - (age - FLASH_SECONDS) / 0.45);
    }

    /** The play field's sideways shake {@code age} s after the kill at real {@code seconds}, px at the full setting. */
    static float shakeX(double age, double seconds) {
        double strength = shake(age);
        if (!(strength > 0)) {
            return 0;
        }
        double wave = 0.55 * Math.sin(2 * Math.PI * 17 * seconds) + 0.45 * Math.sin(2 * Math.PI * 9.1 * seconds + 0.7);
        return (float) (SHAKE_PX * strength * wave);
    }

    /** The shockwave's radius {@code age} s after the kill, px; 0 outside its run. */
    static double shockRadius(double age) {
        double u = (age - SHOCK_AT) / SHOCK_SECONDS;
        if (u < 0 || u >= 1) {
            return 0;
        }
        return SHOCK_RADIUS * (1 - (1 - u) * (1 - u));
    }

    /** Rock {@code k}'s fixed random share {@code salt} (0–1). */
    static double random(int k, int salt) {
        double v = Math.sin(k * 12.9898 + salt * 78.233) * 43758.5453;
        return v - Math.floor(v);
    }

    /** Rock {@code k}'s birth, s since the kill. */
    static double rockBorn(int k) {
        return ROCK_FIRST + (ROCK_LAST - ROCK_FIRST) * random(k, 1);
    }

    /** Rock {@code k}'s landing point: x, and y up (on the field, clear of its edges). */
    static double rockX(int k) {
        return 30 + (PlayField.WIDTH - 60) * random(k, 2);
    }

    static double rockY(int k) {
        return 50 + (PlayField.HEIGHT - 100) * random(k, 3);
    }

    // --- drawing -----------------------------------------------------------------------------------

    /** On the ground, under the collapsed crater: the fissure ring round the mouth's place (x, y up). */
    void drawFissure(SpriteBatch batch, double age, double x, double y) {
        int frame = fissureFrame(age);
        if (frame >= 0 && frame < fissure.size) {
            drawCentred(batch, fissure.get(frame), x, y);
        }
    }

    /**
     * On the ground, over the split ridge: the geysers rolling from its tail to its head, at its points ({@code xs},
     * {@code ys}: x and y up, head first).
     */
    void drawGeysers(SpriteBatch batch, double age, double[] xs, double[] ys, int points) {
        if (geyser.size < GEYSER_FRAMES || points <= 0) {
            return;
        }
        int count = geysers(points);
        for (int layer = BASE; layer <= CORE; layer++) {
            if (!beginLayer(batch, layer)) {
                continue;
            }
            for (int j = 0; j < count; j++) {
                int frame = geyserFrame(j, count, age);
                int p = geyserPoint(j, count, points);
                if (frame >= 0 && ys[p] > -100 && ys[p] < PlayField.HEIGHT + 100) {
                    drawGeyser(batch, layer, frame, xs[p], ys[p]);
                }
            }
            endLayer(batch, layer);
        }
    }

    /** Sets the batch up for {@code layer}; false if it cannot be drawn (no shader for the tinted layers). */
    private boolean beginLayer(SpriteBatch batch, int layer) {
        if (layer == JET) {
            return true;
        }
        if (flash == null) {
            return false;
        }
        if (layer == CORE) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            flash.begin(batch, LIME, GEYSER_CORE_TINT);
        } else {
            flash.begin(batch, SAND_DARK, 1);
        }
        return true;
    }

    private void endLayer(SpriteBatch batch, int layer) {
        batch.setColor(Color.WHITE);
        if (layer == JET) {
            return;
        }
        flash.end(batch);
        if (layer == CORE) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** One geyser's {@code layer} at its {@code frame}, centred on (x, y up). */
    private void drawGeyser(SpriteBatch batch, int layer, int frame, double x, double y) {
        AtlasRegion region = geyser.get(frame);
        float scale = geyserScale(frame);
        switch (layer) {
            case BASE -> {
                float a = geyserBaseAlpha(frame);
                if (a > 0) {
                    batch.setColor(1, 1, 1, a);
                    drawScaled(batch, region, x, y - 3, scale * GEYSER_BASE_SCALE);
                }
            }
            case CORE -> {
                float a = geyserCoreAlpha(frame);
                if (a > 0) {
                    batch.setColor(1, 1, 1, a);
                    drawScaled(batch, region, x, y, scale * GEYSER_CORE_SCALE);
                }
            }
            default -> drawScaled(batch, region, x, y, scale);
        }
    }

    /**
     * The geysers where the split ridge is too short to carry them (the last breach rose where the one before it was:
     * phase 3's eruptions under a ship that stood still): round the mouth's place (x, y up) on a ring just outside the
     * fissures, rolling round it in the same order and timing.
     */
    void drawGeysersRound(SpriteBatch batch, double age, double x, double y) {
        if (geyser.size < GEYSER_FRAMES) {
            return;
        }
        for (int layer = BASE; layer <= CORE; layer++) {
            if (!beginLayer(batch, layer)) {
                continue;
            }
            for (int j = 0; j < GEYSERS; j++) {
                int frame = geyserFrame(j, GEYSERS, age);
                if (frame >= 0) {
                    double angle = 2 * Math.PI * j / GEYSERS + 0.35;
                    drawGeyser(
                            batch, layer, frame, x + Math.cos(angle) * GEYSER_RING, y + Math.sin(angle) * GEYSER_RING);
                }
            }
            endLayer(batch, layer);
        }
    }

    /** Over the ground units: the body rearing, collapsing and sinking, then the shockwave as it goes under. */
    void drawBody(SpriteBatch batch, double age, double x, double y) {
        int code = bodyFrame(age);
        if (code >= 0) {
            Array<AtlasRegion> set =
                    switch (code / 8) {
                        case REAR -> rear;
                        case COLLAPSE -> collapse;
                        default -> death;
                    };
            int frame = code % 8;
            if (frame < set.size) {
                drawCentred(batch, set.get(frame), x, y);
            }
        }
        double r = shockRadius(age);
        if (r > 0 && pixel != null) {
            double u = (age - SHOCK_AT) / SHOCK_SECONDS;
            float fade = (float) (1 - u);
            double width = SHOCK_WIDTH * (1 - 0.6 * u) + 2;
            ring(batch, x, y, r + width / 2 + 1.5, 3, SHOCK_EDGE, 0.5f * fade);
            ring(batch, x, y, r, width, SHOCK, 0.8f * fade);
            batch.setColor(Color.WHITE);
        }
    }

    /**
     * With the air layer: the dust plume climbing over the body (scrolling with the ground at (x, y up)), then the rocks
     * raining over the field, each landing in a puff of dust.
     */
    void drawAir(SpriteBatch batch, double age, double x, double y) {
        int frame = plumeFrame(age);
        if (frame >= 0 && frame < plume.size) {
            // Step GFX4 (H6: a faint haze): its shadow on the sand, then the plume and its denser core over it.
            AtlasRegion cloud = plume.get(frame);
            float fade = plumeAlpha(age);
            float scale = plumeScale(age);
            if (flash != null) {
                flash.begin(batch, SAND_DARK, 1);
                batch.setColor(1, 1, 1, PLUME_SHADOW_ALPHA * fade);
                drawScaled(batch, cloud, x + PLUME_SHADOW_X, y + PLUME_SHADOW_Y, scale);
                flash.end(batch);
            }
            batch.setColor(1, 1, 1, fade);
            drawScaled(batch, cloud, x, y, scale);
            drawScaled(batch, cloud, x, y, scale * PLUME_CORE);
            drawScaled(batch, cloud, x, y, scale * PLUME_HEART);
            batch.setColor(Color.WHITE);
        }
        if (debris.size < ROCK_SHAPES * ROCK_TUMBLE) {
            return;
        }
        for (int k = 0; k < ROCKS; k++) {
            double into = age - rockBorn(k);
            if (into < 0) {
                continue;
            }
            if (into < ROCK_FALL_SECONDS) {
                double u = into / ROCK_FALL_SECONDS;
                int tumble = (int) Math.floorMod((long) Math.floor(age * ROCK_TUMBLE_FPS) + k, (long) ROCK_TUMBLE);
                AtlasRegion rock = debris.get((k % ROCK_SHAPES) * ROCK_TUMBLE + tumble);
                float scale = (float) (ROCK_HIGH_SCALE - (ROCK_HIGH_SCALE - 1) * u);
                drawScaled(batch, rock, rockX(k), rockY(k) + ROCK_DRIFT * (1 - u), scale);
            } else if (into < ROCK_FALL_SECONDS + PUFF_SECONDS && dust.size > 0) {
                int puff = (int) ((into - ROCK_FALL_SECONDS) / PUFF_SECONDS * dust.size);
                drawCentred(batch, dust.get(Math.min(dust.size - 1, puff)), rockX(k), rockY(k));
            }
        }
    }

    /** A ring {@code width} px wide round (x, y) of {@value #SHOCK_SEGMENTS} short lengths. */
    private void ring(SpriteBatch batch, double x, double y, double radius, double width, Color colour, float alpha) {
        batch.setColor(colour.r, colour.g, colour.b, alpha);
        double step = 2 * Math.PI / SHOCK_SEGMENTS;
        float length = (float) (2 * radius * Math.tan(step / 2) + 1);
        float w = (float) width;
        for (int k = 0; k < SHOCK_SEGMENTS; k++) {
            double angle = k * step;
            double cx = x + Math.cos(angle) * radius;
            double cy = y + Math.sin(angle) * radius;
            batch.draw(
                    pixel,
                    (float) (X0 + cx - length / 2),
                    (float) (cy - w / 2),
                    length / 2,
                    w / 2,
                    length,
                    w,
                    1,
                    1,
                    (float) Math.toDegrees(angle + Math.PI / 2));
        }
    }
}
