package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.PlayField;

/**
 * The Dust Colossus's body under the sand as {@code colossus-ridge*} draws it (tools/art/l20_props.py), shared by the
 * travelling ridge ({@link RidgeLooks}: Level 20's and Level 21's ridge pass) and the boss's ridge between its breaches
 * ({@link ColossusLooks}, M7 part H): its points head first, back to front the tail, a length every point with the
 * track's heading there and the head at the front (or, behind a risen mouth, a length: the mouth is the head), the
 * crest cracks' lime glow over the lengths (additive, at the given alpha), a sand slide puffing off a flank at the
 * middle. Draws nothing it has no sprites for; allocates nothing.
 */
final class RidgeBody {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    /** The dust puffs' frame rate. */
    static final double DUST_FPS = 10;
    /** A point counts as on the play field within this of its edges, px (a sprite's half width). */
    private static final double MARGIN = 64;

    private final Array<AtlasRegion> lengths;
    private final Array<AtlasRegion> heads;
    private final Array<AtlasRegion> tails;
    private final Array<AtlasRegion> glows;
    private final Array<AtlasRegion> dust;

    private RidgeBody(
            Array<AtlasRegion> lengths,
            Array<AtlasRegion> heads,
            Array<AtlasRegion> tails,
            Array<AtlasRegion> glows,
            Array<AtlasRegion> dust) {
        this.lengths = lengths;
        this.heads = heads;
        this.tails = tails;
        this.glows = glows;
        this.dust = dust;
    }

    /** Whether the atlas has the ridge's lengths at every heading. */
    static boolean drawable(Sprites sprites) {
        return sprites.has(RidgeLooks.ROOT) && sprites.frames(RidgeLooks.ROOT).size >= RidgeLooks.HEADINGS;
    }

    /** The ridge's sprites from the atlas ({@link #drawable} first); a missing head, tail, glow or dust set falls back. */
    static RidgeBody of(Sprites sprites) {
        Array<AtlasRegion> lengths = sprites.frames(RidgeLooks.ROOT);
        return new RidgeBody(
                lengths,
                optional(sprites, RidgeLooks.ROOT + "-head", lengths),
                optional(sprites, RidgeLooks.ROOT + "-tail", lengths),
                optional(sprites, RidgeLooks.ROOT + "-glow", new Array<>(0)),
                optional(sprites, RidgeLooks.ROOT + "-dust", new Array<>(0)));
    }

    /** No sprites: draws nothing (the tests' tracks). */
    static RidgeBody none() {
        Array<AtlasRegion> none = new Array<>(0);
        return new RidgeBody(none, none, none, none, none);
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name, Array<AtlasRegion> fallback) {
        return sprites.has(name) ? sprites.frames(name) : fallback;
    }

    /**
     * The body of {@code points} points (head first: x, height above the bottom edge, heading index) at level or real
     * time {@code seconds}: {@code head} draws the head sprite at the front (else a length), the glow at {@code glow}
     * (0: none), the whole body at {@code alpha}.
     */
    void draw(
            SpriteBatch batch,
            double[] xs,
            double[] ys,
            int[] headings,
            int points,
            double seconds,
            boolean head,
            float glow,
            float alpha) {
        if (points == 0 || lengths.size == 0 || !onScreen(xs, ys, points) || !(alpha > 0)) {
            return;
        }
        batch.setColor(1, 1, 1, alpha);
        for (int i = points - 1; i >= 0; i--) {
            Array<AtlasRegion> set = i == 0 && head ? heads : i == points - 1 && points > 1 ? tails : lengths;
            drawCentred(batch, set.get(headings[i] % set.size), xs[i], ys[i]);
        }
        if (glows.size >= RidgeLooks.HEADINGS && glow > 0) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(1, 1, 1, glow * alpha);
            for (int i = points - 2; i >= (head ? 1 : 0); i--) {
                drawCentred(batch, glows.get(headings[i] % glows.size), xs[i], ys[i]);
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        batch.setColor(1, 1, 1, alpha);
        if (dust.size > 0 && points > 2) {
            int middle = points / 2;
            AtlasRegion puff = dust.get((int) Math.floorMod((long) Math.floor(seconds * DUST_FPS), (long) dust.size));
            drawCentred(batch, puff, xs[middle] + 30, ys[middle] - 8);
        }
        batch.setColor(1, 1, 1, 1);
    }

    /** Whether any point lies within reach of the play field (a sprite's half width outside it). */
    static boolean onScreen(double[] xs, double[] ys, int points) {
        for (int i = 0; i < points; i++) {
            if (xs[i] > -MARGIN
                    && xs[i] < PlayField.WIDTH + MARGIN
                    && ys[i] > -MARGIN
                    && ys[i] < PlayField.HEIGHT + MARGIN) {
                return true;
            }
        }
        return false;
    }

    private static void drawCentred(SpriteBatch batch, AtlasRegion region, double x, double y) {
        batch.draw(
                region,
                Math.round(X0 + x - region.getRegionWidth() / 2.0),
                Math.round(y - region.getRegionHeight() / 2.0));
    }
}
