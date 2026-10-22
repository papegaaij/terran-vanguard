package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.SimStep;

/**
 * M8 part C: the hydro-kit's effects under water (design/world/europa, Hydro-kit; tools/art/act4_gear.py {@code
 * hydrofx}, round 52; the round-50 note "no grade, no wake: part C"), for the Stormhawk ({@code ship-}) or Rook ({@code
 * rook-}): at each engine mount, in place of the engine flame, the pump-jet's wake trailing down the screen ({@code
 * -hydro-wake}, normal blending, its jet at the frame's top middle, 2 px down; {@value #WAKE_FRAME_TICKS} steps a frame:
 * 12 fps) under the jet's light ({@code -hydro-jet}, additive, centred on the mount, 12 fps); and the refit flash at a
 * waterline ({@code -refit}, additive, centred on the hull, {@value #REFIT_FRAME_TICKS} steps a frame), started so its
 * frame {@value #SWAP_FRAME} falls on the waterline, where the hull swaps to the kit ({@link PlayerPasses#hull}). On the
 * level clock: a retry after the crossing shows no flash; nothing is allocated per frame.
 */
final class HydroLooks {
    /** The wake's and the jet's frames show this many steps (12 fps). */
    static final int WAKE_FRAME_TICKS = 5;

    static final int JET_FRAME_TICKS = 5;
    /** The refit flash's frames show this many steps; the hull swaps at this frame. */
    static final int REFIT_FRAME_TICKS = 3;

    static final int SWAP_FRAME = 4;
    /** The wake's jet lies this far below its frame's top edge, px. */
    static final int WAKE_JET_DY = 2;

    private final Array<AtlasRegion> wake;
    private final Array<AtlasRegion> jet;
    private final Array<AtlasRegion> refit;
    /** The level's waterlines (level seconds). */
    private final double[] waterlines;

    /**
     * @param prefix {@code ship} or {@code rook}
     * @param waterlines the level's waterlines' times
     */
    HydroLooks(Sprites sprites, String prefix, List<Double> waterlines) {
        wake = optional(sprites, prefix + "-hydro-wake");
        jet = optional(sprites, prefix + "-hydro-jet");
        refit = optional(sprites, prefix + "-refit");
        this.waterlines = waterlines.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** Whether it has the wake's frames (else the engine flames stay under water). */
    boolean hasWake() {
        return !wake.isEmpty();
    }

    /**
     * One engine's wake and jet light under water, its mount at screen (x, y) (y up), at step {@code tick}; {@code
     * phase} staggers the second engine's loop.
     */
    void drawEngine(SpriteBatch batch, long tick, int phase, float x, float y) {
        if (wake.isEmpty()) {
            return;
        }
        AtlasRegion stream = wake.get((int) ((tick / WAKE_FRAME_TICKS + phase) % wake.size));
        batch.draw(
                stream,
                Math.round(x - stream.getRegionWidth() / 2f),
                Math.round(y + WAKE_JET_DY - stream.getRegionHeight()));
        if (jet.isEmpty()) {
            return;
        }
        AtlasRegion light = jet.get((int) ((tick / JET_FRAME_TICKS + phase) % jet.size));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.draw(light, Math.round(x - light.getRegionWidth() / 2f), Math.round(y - light.getRegionHeight() / 2f));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * The refit flash's frame at level time {@code seconds}: its frame {@value #SWAP_FRAME} at a waterline, -1 outside
     * every flash.
     */
    static int refitFrame(double[] waterlines, double seconds, int frames) {
        double lead = SWAP_FRAME * REFIT_FRAME_TICKS / (double) SimStep.PER_SECOND;
        for (double t : waterlines) {
            double since = seconds - (t - lead);
            int frame = (int) Math.floor(since * SimStep.PER_SECOND / REFIT_FRAME_TICKS);
            if (since >= 0 && frame < frames) {
                return frame;
            }
        }
        return -1;
    }

    /** The refit flash over the hull centred on screen (x, y) at level time {@code seconds}, additive; none outside. */
    void drawRefit(SpriteBatch batch, double seconds, float x, float y) {
        int frame = refitFrame(waterlines, seconds, refit.size);
        if (frame < 0) {
            return;
        }
        AtlasRegion flash = refit.get(frame);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.draw(flash, Math.round(x - flash.getRegionWidth() / 2f), Math.round(y - flash.getRegionHeight() / 2f));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
