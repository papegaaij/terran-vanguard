package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import vanguard.sim.SimStep;

/**
 * M5 part F, a latching chaser's own poses (design/enemies/air/lamprey; tools/art/lamprey.py): its
 * latched pose at its hull spot ({@code lamprey-latched}, {@value #LATCHED_FRAMES} frames a spot,
 * indexed {@code spot * 4 + frame}) drawn over the ship, and its stun, thrown off and tumbling a full
 * turn ({@code lamprey-stunned}, the frame by the stun's progress). Chasing, flying on and fleeing it
 * is drawn with its 16-angle frames, and it dies in its {@code small} wet pop ({@link EnemyLooks}).
 *
 * <p>The latch's look is the art's (its pivot file's {@code latch_look}): look b, "siphon" (the user's
 * pick in round 34, 2026-10-12), when the art has its {@code lamprey-drain} frames: the tail lashing
 * in the latched frames, and at the mouth's contact with the hull cyan shield motes spiralling into a
 * teal glow ({@link #drain}, additive, the latched frame's own 10 fps), no shield ring. Look a,
 * "gulp", without them: the ship's own shield ring pulsing with each swallow (the {@code gulp_pulse}
 * of the pivot file, 0.55, 0.3, 0.12, 0 of the ring's strength on the gulp's frames).
 *
 * @param latched the latched frames, {@value #LATCHED_FRAMES} per spot
 * @param stunned the stun's tumble
 * @param gulp the shield ring's strength on each latched frame (look a; all 0 for look b)
 * @param drain the siphon's glow at the mouth, {@value #LATCHED_FRAMES} frames (look b); empty for look a
 * @param fps the latched frames' rate: the Lamprey's {@value #FPS}; M9 part F, the Void Leech's sac: one pulse a second
 *     ({@link LeechFieldLooks#PULSE_FPS}, its pivot file's {@code mouth} names it)
 * @param drainAt where the drain's glow is centred at each spot, px from the ship's centre, y up: {@link #DRAIN_AT}, or
 *     the pivot file's {@code mouth} (y down there)
 */
record LampreyLooks(
        Array<AtlasRegion> latched,
        Array<AtlasRegion> stunned,
        float[] gulp,
        Array<AtlasRegion> drain,
        double fps,
        int[][] drainAt) {
    /** The latched pose's frames a spot (a 0.4 s gulp or lash at 10 fps). */
    static final int LATCHED_FRAMES = 4;

    static final double FPS = 10;
    /** Latch look a's gulp (tools/art/lamprey.py), used when the pivot file does not give it. */
    static final float[] GULP = {0.55f, 0.3f, 0.12f, 0};
    /** Look b: no shield ring. */
    static final float[] NO_GULP = {0};

    /**
     * Look b: where the siphon's glow is centred at each spot (left, right, tail), px from the ship's
     * centre, y up: lamprey.py's {@code SPOT_CONTACT}, the mouth's contact on the Stormhawk's 48 px
     * level frame, so 16 px from the latched sprite's centre toward the hull.
     */
    static final int[][] DRAIN_AT = {{-6, -13}, {5, -13}, {0, -19}};

    /** The looks of a unit with them ({@code <slug>-latched} and {@code -stunned}); null for another. */
    static LampreyLooks of(Sprites sprites, String slug, JsonValue pivots) {
        if (!sprites.has(slug + "-latched") || !sprites.has(slug + "-stunned")) {
            return null;
        }
        Array<AtlasRegion> drain = sprites.has(slug + "-drain") ? sprites.frames(slug + "-drain") : new Array<>();
        int[][] mouth = mouth(pivots);
        return new LampreyLooks(
                sprites.frames(slug + "-latched"),
                sprites.frames(slug + "-stunned"),
                gulp(pivots, drain.size > 0),
                drain,
                mouth == null ? FPS : LeechFieldLooks.PULSE_FPS,
                mouth == null ? DRAIN_AT : mouth);
    }

    /**
     * M9 part F: the drain's contact at each spot (left, right, tail) from the pivot file's {@code mouth} (px from the
     * ship's centre, y down there, so flipped); null without it (the Lamprey's {@link #DRAIN_AT}).
     */
    static int[][] mouth(JsonValue pivots) {
        if (pivots == null || !pivots.has("mouth") || !pivots.get("mouth").isObject()) {
            return null;
        }
        JsonValue mouth = pivots.get("mouth");
        String[] spots = {"left", "right", "tail"};
        int[][] at = new int[spots.length][];
        for (int i = 0; i < spots.length; i++) {
            if (!mouth.has(spots[i])) {
                return null;
            }
            float[] xy = mouth.get(spots[i]).asFloatArray();
            at[i] = new int[] {Math.round(xy[0]), -Math.round(xy[1])};
        }
        return at;
    }

    /** The shield ring's strengths: none for look b (the siphon), else the pivot file's or {@link #GULP}. */
    static float[] gulp(JsonValue pivots, boolean siphon) {
        if (siphon) {
            return NO_GULP;
        }
        if (pivots != null
                && pivots.has("gulp_pulse")
                && pivots.get("gulp_pulse").isArray()) {
            return pivots.get("gulp_pulse").asFloatArray();
        }
        return GULP;
    }

    /** The latched frame's index at {@code spot} at step {@code tick}, each unit at its {@code phase}. */
    static int latchedIndex(int spot, long tick, int phase) {
        return latchedIndex(spot, tick, phase, FPS);
    }

    /** The latched frame's index at {@code spot} at step {@code tick} at {@code fps}. */
    static int latchedIndex(int spot, long tick, int phase, double fps) {
        return spot * LATCHED_FRAMES + gulpFrame(tick, phase, fps);
    }

    /** The gulp's (or lash's) frame (0 to 3) at step {@code tick}. */
    static int gulpFrame(long tick, int phase) {
        return gulpFrame(tick, phase, FPS);
    }

    /** The latched frames' frame (0 to 3) at step {@code tick} at {@code fps}. */
    static int gulpFrame(long tick, int phase, double fps) {
        return (int) Math.floorMod((long) Math.floor(tick * fps / SimStep.PER_SECOND) + phase, (long) LATCHED_FRAMES);
    }

    /** This unit's latched frame at {@code spot} at step {@code tick}. */
    int frameAt(int spot, long tick, int phase) {
        return latchedIndex(spot, tick, phase, fps);
    }

    /** The stun's frame at its progress (0 thrown off, 1 the stun's end): a full turn over the stun. */
    static int stunnedIndex(double progress, int frames) {
        return Math.clamp((int) Math.floor(progress * frames), 0, frames - 1);
    }

    /** The shield ring's strength on gulp frame {@code frame}. */
    float gulpStrength(int frame) {
        return gulp[Math.floorMod(frame, gulp.length)];
    }

    /** True for latch look b, the siphon (its glow at the mouth instead of the shield ring's pulse). */
    boolean siphon() {
        return drain.size > 0;
    }

    /**
     * Look b: the siphon's glow of a Lamprey latched at {@code spot} on a ship whose centre is at
     * ({@code shipX}, {@code shipY}), additive, on the latched frame's own frame; nothing for look a.
     */
    void drawDrain(SpriteBatch batch, int spot, long tick, int phase, float shipX, float shipY) {
        if (!siphon()) {
            return;
        }
        AtlasRegion frame = drain.get(Math.floorMod(gulpFrame(tick, phase, fps), drain.size));
        int[] at = drainAt[Math.clamp(spot, 0, drainAt.length - 1)];
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.draw(
                frame,
                Math.round(shipX + at[0] - frame.getRegionWidth() / 2f),
                Math.round(shipY + at[1] - frame.getRegionHeight() / 2f));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
