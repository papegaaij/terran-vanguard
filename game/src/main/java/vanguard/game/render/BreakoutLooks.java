package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part H, step H5: a level leaving the water through the ice (design/campaign Level 28, The Breakout and Orbit;
 * tools/art/l28_props.py's {@code ice-shatter} and {@code ice-slab}, briefs/m8arth2-rows.md): as the level clock reaches
 * the first section in the air after the water (Level 28's t=115) the ice ceiling bursts round the ship ({@code
 * ice-shatter}, additive, {@value #SHATTER_TICKS} steps a frame, centred on the ship's x at the breakout line, scrolling
 * down with the ground) and four slabs tumble out of the field ({@code ice-slab_<slab × 8 + tumble>}, ~10 fps, growing
 * toward the camera as they fly and fading); in its last section (the orbit) the field fades to white. Over the front
 * layer, under the bullets. Presentation only; nothing is allocated per frame.
 */
final class BreakoutLooks {
    /** The burst's frames (20 fps). */
    static final int SHATTER_TICKS = 3;
    /** The slabs: how many, their tumble frames, a frame's steps (~10 fps). */
    static final int SLABS = 4;

    static final int TUMBLE = 8;
    static final int TUMBLE_TICKS = 6;
    /** Each slab's direction (degrees, 0 right, y up) and speed, px/s; they grow to this scale and fade over their flight. */
    static final double[] SLAB_ANGLE = {125, 55, 195, 340};

    static final double[] SLAB_SPEED = {300, 340, 270, 320};
    static final double SLAB_SECONDS = 1.8;
    static final float SLAB_GROWTH = 1.2f;
    /** The orbit's white at the level's end. */
    static final float ORBIT_WHITE = 0.85f;

    private static final long NEVER = Long.MIN_VALUE / 2;

    private final Array<AtlasRegion> shatter;
    private final Array<AtlasRegion> slab;
    private final TextureRegion pixel;
    /** The breakout's level time; the orbit's start and end. */
    private final double breakAt;

    private final double orbitFrom;
    private final double orbitTo;
    /** The burst's step, the ship's place and the ground scroll then. */
    private long burstTick = NEVER;

    private double burstX;
    private double burstY;
    private double burstScroll;

    private BreakoutLooks(Sprites sprites, LevelData level) {
        shatter = optional(sprites, "ice-shatter");
        slab = optional(sprites, "ice-slab");
        pixel = sprites == null ? null : sprites.pixel;
        breakAt = breakAt(level);
        int last = level.sections().size() - 1;
        orbitFrom = level.sectionStart(last);
        orbitTo = level.sections().get(last).end();
    }

    /** The looks of a level that leaves the water, with the burst's sprites; null otherwise. */
    static BreakoutLooks of(Sprites sprites, LevelData level) {
        if (Double.isNaN(breakAt(level)) || !sprites.has("ice-shatter") || !sprites.has("ice-slab")) {
            return null;
        }
        return new BreakoutLooks(sprites, level);
    }

    /** For the tests: the looks without their sprites (they draw nothing). */
    static BreakoutLooks following(LevelData level) {
        return new BreakoutLooks(null, level);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** The start of the level's first section in the air after one under water (Level 28's t=115); NaN for none. */
    static double breakAt(LevelData level) {
        for (int i = 1; i < level.sections().size(); i++) {
            if (level.mediumOf(i - 1) == SectionsBlock.Medium.UNDERWATER
                    && level.mediumOf(i) != SectionsBlock.Medium.UNDERWATER) {
                return level.sectionStart(i);
            }
        }
        return Double.NaN;
    }

    double breakAt() {
        return breakAt;
    }

    /** The orbit's white at level time {@code seconds}: rising over the level's last section. */
    float orbitWhite(double seconds) {
        if (seconds <= orbitFrom || orbitTo <= orbitFrom) {
            return 0;
        }
        double t = Math.clamp((seconds - orbitFrom) / (orbitTo - orbitFrom), 0, 1);
        return (float) (ORBIT_WHITE * t * t * (3 - 2 * t));
    }

    /** The level restarts: no burst yet. */
    void reset() {
        burstTick = NEVER;
    }

    /** After each step: the burst as the level clock reaches the breakout (forgotten if a retry goes back before it). */
    void stepped(Sortie sortie) {
        double seconds = sortie.levelSeconds();
        if (seconds < breakAt) {
            burstTick = NEVER;
        } else if (burstTick == NEVER) {
            burstTick = sortie.tick();
            burstX = sortie.ship().x();
            burstY = sortie.ship().y();
            burstScroll = sortie.groundScroll();
        }
    }

    /** The burst's age, s, between the last step and the next; -1 before it. */
    double age(long tick, float alpha) {
        return burstTick == NEVER ? -1 : (tick + alpha - burstTick) * SimStep.SECONDS;
    }

    /** The burst's frame {@code age} s after it, -1 once over. */
    static int shatterFrame(double age, int frames) {
        int frame = (int) (age / (SHATTER_TICKS * SimStep.SECONDS));
        return age < 0 || frame >= frames ? -1 : frame;
    }

    /** Slab {@code s}'s tumble frame {@code age} s after the burst. */
    static int slabFrame(int s, double age) {
        return s * TUMBLE + Math.floorMod((int) (age / (TUMBLE_TICKS * SimStep.SECONDS)) + 2 * s, TUMBLE);
    }

    /** The burst, the slabs flying out and the orbit's white, over the front layer. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, double scroll, double seconds) {
        double age = age(sortie.tick(), alpha);
        if (age >= 0 && age < SLAB_SECONDS) {
            double y = burstY - (scroll - burstScroll);
            if (slab.size >= SLABS * TUMBLE) {
                double t = age / SLAB_SECONDS;
                batch.setColor(1, 1, 1, (float) (1 - t * t));
                for (int s = 0; s < SLABS; s++) {
                    double a = Math.toRadians(SLAB_ANGLE[s]);
                    double d = SLAB_SPEED[s] * age;
                    drawScaled(
                            batch,
                            slab.get(slabFrame(s, age)),
                            burstX + Math.cos(a) * d,
                            burstY + Math.sin(a) * d,
                            (float) (1 + SLAB_GROWTH * t));
                }
                batch.setColor(Color.WHITE);
            }
            int frame = shatterFrame(age, shatter.size);
            if (frame >= 0) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                drawScaled(batch, shatter.get(frame), burstX, y, 1);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
        float white = orbitWhite(seconds);
        if (white > 0 && pixel != null) {
            batch.setColor(1, 1, 1, white);
            batch.draw(pixel, X0, 0, PlayField.WIDTH, PlayField.HEIGHT);
            batch.setColor(Color.WHITE);
        }
    }
}
