package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * M7 part C: the look of touching a canyon's rock (design/campaign Level 16, Wall contact and What walls
 * stop): a hull's touch ({@code WALL_CONTACT}) throws the wall spark, fanning away from the rock, with
 * its dust; a shot or an enemy bullet stopped at rock ({@code ROCK_SPARK}) a small one (half size). Both
 * stay on the rock as it scrolls. The ship's short hit flash follows its wall contact cooldown ({@link
 * Sortie#wallCooling()}): white for the cooldown's first {@value #FLASH_TICKS} steps. The sprites
 * ({@code wall-spark_0..5}, additive, sparks toward +x for the left wall, mirrored for the right; {@code
 * wall-spark-dust_0..5}, solid) are loaded with the first terrain; nothing is allocated per step.
 */
final class CanyonLooks {
    /** The ship's white flash at a wall touch, steps (0.1 s: "a short hit flash"). */
    static final int FLASH_TICKS = 6;
    /** The sprites run at 30 fps. */
    static final int FRAME_TICKS = 2;
    /** A shot's or bullet's spark at rock, against a hull's touch. */
    static final float SMALL = 0.5f;
    /** How far beside the event a probe looks for the rock, px. */
    private static final double PROBE = 6;

    private final Sprites sprites;
    private final Effects sparks = Effects.glowing();
    private final Effects dust = Effects.solid();
    private Array<AtlasRegion> spark;
    private Array<AtlasRegion> sparkMirrored;
    private Array<AtlasRegion> dustFrames;
    private Array<AtlasRegion> dustMirrored;

    /**
     * M8 part C: an ice shaft's walls (Level 23): a touch throws ice chips (the dust frames in pale ice, {@link
     * #ICE_CHIP}); under water no sparks fly, only the chips.
     */
    private boolean ice;

    /** M8 part C: the ice chips' colour, the dust frames tinted pale blue-white. */
    static final com.badlogic.gdx.graphics.Color ICE_CHIP = new com.badlogic.gdx.graphics.Color(0.86f, 0.95f, 1f, 1);

    private boolean cooling;
    private long flashFrom = Long.MIN_VALUE / 2;

    CanyonLooks(Sprites sprites) {
        this.sprites = sprites;
    }

    /** M8 part C: the walls are an ice shaft's: chips instead of rock dust, no sparks under water. */
    void ice(boolean ice) {
        this.ice = ice;
    }

    /** A new attempt: the sparks and the flash are gone. */
    void restart() {
        sparks.clear();
        dust.clear();
        cooling = false;
        flashFrom = Long.MIN_VALUE / 2;
    }

    /** After each simulation step: the touches' and stopped shots' sparks start, the old ones advance. */
    void stepped(Sortie sortie) {
        sparks.step();
        dust.step();
        Terrain terrain = sortie.terrain();
        if (terrain == null) {
            return;
        }
        boolean nowCooling = sortie.wallCooling();
        if (nowCooling && !cooling) {
            flashFrom = sortie.tick();
        }
        cooling = nowCooling;
        if (spark == null) {
            if (!sprites.has("wall-spark")) {
                return;
            }
            spark = sprites.frames("wall-spark");
            sparkMirrored = mirrored(spark);
            dustFrames = sprites.frames("wall-spark-dust");
            dustMirrored = mirrored(dustFrames);
        }
        SimEvents events = sortie.events();
        double scroll = sortie.groundScroll();
        for (int i = 0; i < events.size(); i++) {
            SimEvents.Type type = events.type(i);
            if (type != SimEvents.Type.WALL_CONTACT && type != SimEvents.Type.ROCK_SPARK) {
                continue;
            }
            double x = events.x(i);
            double y = events.y(i);
            int value = events.value(i);
            boolean rockRight = type == SimEvents.Type.WALL_CONTACT && value != 2
                    ? value == 1
                    : rockRight(terrain, scroll, x, y, sortie.tick());
            float scale = type == SimEvents.Type.WALL_CONTACT ? 1 : SMALL;
            dust.startOnGroundScaled(rockRight ? dustMirrored : dustFrames, FRAME_TICKS, x, y, 0, scroll, scale, 1);
            if (!ice || !sortie.underwater()) {
                sparks.startOnGroundScaled(rockRight ? sparkMirrored : spark, FRAME_TICKS, x, y, 0, scroll, scale, 1);
            }
        }
    }

    /**
     * Whether the rock at a touch on (x, y) lies to its right (the spark then fans to the left): rock a
     * few px to one side and not the other; else (an island's tip, a shot from below) by the step's parity.
     */
    static boolean rockRight(Terrain terrain, double groundScroll, double x, double y, long tick) {
        boolean left = terrain.rock(groundScroll, x - PROBE, y, 0.5, 0.5);
        boolean right = terrain.rock(groundScroll, x + PROBE, y, 0.5, 0.5);
        if (left != right) {
            return right;
        }
        return (tick & 1) == 1;
    }

    /** Whether the ship shows its wall touch's white flash at step {@code tick}. */
    boolean shipFlash(long tick) {
        return cooling && tick - flashFrom < FLASH_TICKS;
    }

    /** The dust, then the sparks over it (over the units, with the other impacts). */
    void draw(SpriteBatch batch, double groundScroll) {
        if (ice) {
            batch.setColor(ICE_CHIP);
        }
        dust.draw(batch, groundScroll);
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        sparks.draw(batch, groundScroll);
    }

    private static Array<AtlasRegion> mirrored(Array<AtlasRegion> frames) {
        Array<AtlasRegion> out = new Array<>(frames.size);
        for (AtlasRegion frame : frames) {
            AtlasRegion copy = new AtlasRegion(frame);
            copy.flip(true, false);
            out.add(copy);
        }
        return out;
    }
}
