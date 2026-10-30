package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.content.Content;
import vanguard.content.EnemyData;
import vanguard.sim.EnemySpec;
import vanguard.sim.Missile;
import vanguard.sim.Sortie;

/**
 * M9 part A: the Ascendancy's homing missiles as homing diamonds (design/art-direction, the homing diamond, round 60
 * A.16 a; tools/art/act5_gear.py {@code diamond}, whose docstring gives the sprites): in a level whose homing missiles
 * wear the look ({@link #diamonds}: a kind's {@code homing: {look: diamond}}), every missile is drawn with the bullets
 * as the crimson diamond at the nearest of its {@value #HEADINGS} headings ({@code homing-diamond}, normal) over its
 * short pale-gold exhaust ({@code -exhaust}, additive, heading x 2 + a flicker every {@value #FLICKER_TICKS} steps); one
 * shot down or burst on a hull pops ({@code -pop}, {@value #POP_FRAME_TICKS} steps a frame), one at the end of its life
 * fizzles ({@code -fizzle}, {@value #FIZZLE_FRAME_TICKS} steps a frame). A level draws its missiles one way (the
 * walker's and the Depth Hunters' levels have their own): the simulation's missile carries no look of its own.
 */
public final class DiamondLooks {
    static final String ROOT = "homing-diamond";
    static final int HEADINGS = 16;
    static final int FLICKER_TICKS = 3;
    public static final int POP_FRAME_TICKS = 4;
    public static final int FIZZLE_FRAME_TICKS = 5;

    private final Array<AtlasRegion> diamond;
    private final Array<AtlasRegion> exhaust;
    private final Array<AtlasRegion> pop;
    private final Array<AtlasRegion> fizzle;

    private DiamondLooks(Sprites sprites) {
        diamond = sprites.frames(ROOT);
        exhaust = sprites.has(ROOT + "-exhaust") ? sprites.frames(ROOT + "-exhaust") : new Array<>(0);
        pop = sprites.has(ROOT + "-pop") ? sprites.frames(ROOT + "-pop") : sprites.explosionSmall;
        fizzle = sprites.has(ROOT + "-fizzle") ? sprites.frames(ROOT + "-fizzle") : sprites.explosionTiny;
    }

    /** The looks for a level whose missiles are diamonds ({@code diamonds}); null without, or while the art is missing. */
    static DiamondLooks of(Sprites sprites, boolean diamonds) {
        return diamonds && sprites.has(ROOT) ? new DiamondLooks(sprites) : null;
    }

    /** Whether any of the level's enemy kinds fires homing diamonds (a {@code homing} attack with {@code look: diamond}). */
    public static boolean diamonds(Content content, List<EnemySpec> kinds) {
        for (EnemySpec kind : kinds) {
            EnemyData data = content.enemies().get(kind.slug());
            if (data == null) {
                continue;
            }
            for (EnemyData.Attack attack : data.attacks()) {
                if (attack.homing().flatMap(homing -> homing.look()).isPresent()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The diamond's frame for a missile flying at {@code heading} (radians clockwise from straight down the screen, as
     * {@link Missile#heading()}): the art's headings are k x 360/n degrees clockwise from straight up.
     */
    static int heading(double heading, int headings) {
        return Math.floorMod((int) Math.rint((heading + Math.PI) / (2 * Math.PI / headings)), headings);
    }

    /** The exhaust's frame: the heading's two flicker frames, alternating every {@value #FLICKER_TICKS} steps. */
    static int exhaustFrame(int heading, long tick, int serial) {
        return heading * 2 + (int) Math.floorMod(tick / FLICKER_TICKS + serial, 2L);
    }

    /** A diamond shot down or burst on a hull: the spark pop (additive). */
    public Array<AtlasRegion> pop() {
        return pop;
    }

    /** A diamond at the end of its life: the faint ring (additive). */
    public Array<AtlasRegion> fizzle() {
        return fizzle;
    }

    /** With the bullets: every missile's exhaust (additive), then the diamonds over them. */
    void draw(SpriteBatch batch, Sortie sortie, float alpha) {
        int count = sortie.missileCount();
        if (count == 0) {
            return;
        }
        if (exhaust.size >= 2 * diamond.size) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            for (int m = 0; m < count; m++) {
                Missile missile = sortie.missile(m);
                if (missile.wreck() >= 0) {
                    // Step BOSS70: a thrown wreck is the Iron Sovereign's to draw (SovereignLooks).
                    continue;
                }
                int k = heading(missile.heading(), diamond.size);
                drawCentred(
                        batch,
                        exhaust.get(exhaustFrame(k, sortie.tick(), missile.serial())),
                        missile.renderX(alpha),
                        missile.renderY(alpha));
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        for (int m = 0; m < count; m++) {
            Missile missile = sortie.missile(m);
            if (missile.wreck() >= 0) {
                continue;
            }
            drawCentred(
                    batch,
                    diamond.get(heading(missile.heading(), diamond.size)),
                    missile.renderX(alpha),
                    missile.renderY(alpha));
        }
    }

    private static void drawCentred(SpriteBatch batch, AtlasRegion region, double x, double y) {
        batch.draw(region, (float) Math.round(PixelScreen.PLAY_FIELD_X + x - region.getRegionWidth() / 2f), (float)
                Math.round(y - region.getRegionHeight() / 2f));
    }
}
