package vanguard.game.screen;

import static vanguard.game.screen.LevelScreen.TINY_EXPLOSION_FRAME_TICKS;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.game.render.LevelRenderer;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M10 part A (design/enemies, Reflect): the mirrors' events on the play field. A bounced shot leaves a bright spark
 * where it met the plate (its magenta bullet is the bullets' look), an absorbed one a smaller spark; a broken or
 * shattered mirror throws its shards ({@code JovianLooks}, whose plate also shows it down and its regrowth's shimmer
 * from the unit's state).
 */
final class MirrorEffects {
    /** The spark's frames show this many steps each. */
    static final int SPARK_FRAME_TICKS = 2;
    /** An absorbed shot's spark is drawn this much smaller than a bounce's. */
    static final float ABSORBED_SCALE = 0.6f;

    private final Sortie sortie;
    private final LevelRenderer renderer;
    private final Effects effects;
    private final Array<AtlasRegion> spark;
    private final Array<AtlasRegion> tiny;

    MirrorEffects(
            Sortie sortie, LevelRenderer renderer, Effects effects, Array<AtlasRegion> spark, Array<AtlasRegion> tiny) {
        this.sortie = sortie;
        this.renderer = renderer;
        this.effects = effects;
        this.spark = spark;
        this.tiny = tiny;
    }

    /** Event {@code i}, a mirror's ({@code MIRROR_BOUNCED}, {@code _ABSORBED}, {@code _BROKEN}, {@code _REGROWING}). */
    void react(SimEvents events, int i) {
        double x = events.x(i);
        double y = events.y(i);
        switch (events.type(i)) {
            case MIRROR_BOUNCED -> effects.start(spark, SPARK_FRAME_TICKS, x, y);
            case MIRROR_ABSORBED -> effects.start(spark, SPARK_FRAME_TICKS, x, y, 0, ABSORBED_SCALE, 1);
            case MIRROR_BROKEN -> {
                effects.start(tiny, TINY_EXPLOSION_FRAME_TICKS, x, y);
                renderer.jovian().mirrorBroken(sortie, x, y, events.value(i));
            }
            // The silver shimmer is the plate's own (its regrowth's state).
            default -> {}
        }
    }
}
