package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.sim.Sortie;
import vanguard.sim.SporeClouds;

/**
 * M11 part A, step A3b: the spore clouds (design/world/vrell-space, Hazards & set pieces; Level 45; the stand-in of
 * tools/art/act7_gear.py {@code spore}, whose docstring gives the sprites), each scaled to its radius ({@value
 * #ART_RADIUS} px in the art): the smooth dark violet veil (normal blend, at most the clouds' veil, 25 %) over the
 * backdrop and the play-plane units under it, then (additive) the cloud's dim glow and drifting specks ({@code
 * spore-cloud_*}, a 4 s loop, {@value #CLOUD_FRAME_TICKS} steps a frame), or while it burns the ember edge eating inward
 * ({@code spore-cloud-burn_*}, over its 1 s) at the whole cloud's size with the veil shrinking to the burning radius.
 * Drawn under the pickups, the shots, Rook, the ship and the bullets ({@code DrawPasses}).
 */
final class SporeLooks {
    static final String VEIL = "spore-cloud-veil";
    static final String CLOUD = "spore-cloud";
    static final String BURN = "spore-cloud-burn";
    /** The radius of the cloud the art draws, px. */
    static final double ART_RADIUS = 64;

    static final int CLOUD_FRAME_TICKS = 30;
    /** The veil's alpha in the art (64 of 255); the clouds' veil share scales it. */
    static final double ART_VEIL = 0.25;

    private final AtlasRegion veil;
    private final Array<AtlasRegion> cloud;
    private final Array<AtlasRegion> burn;

    SporeLooks(AtlasRegion veil, Array<AtlasRegion> cloud, Array<AtlasRegion> burn) {
        this.veil = veil;
        this.cloud = cloud;
        this.burn = burn;
    }

    /** The looks for a level with spore clouds; null without them or their art. */
    static SporeLooks of(Sprites sprites, Sortie sortie) {
        if (sortie.spores() == null || !sprites.has(VEIL) || !sprites.has(CLOUD) || !sprites.has(BURN)) {
            return null;
        }
        return new SporeLooks(sprites.region(VEIL), sprites.frames(CLOUD), sprites.frames(BURN));
    }

    /** A burning cloud's whole radius from its radius now and its share burnt. */
    static double wholeRadius(double radius, double burnt) {
        return burnt < 1 ? radius / (1 - burnt) : 0;
    }

    /** The clouds (the caller's normal blend, restored). */
    void draw(SpriteBatch batch, Sortie sortie) {
        SporeClouds spores = sortie.spores();
        if (spores.count() == 0) {
            return;
        }
        float shade = (float) Math.clamp(spores.veil() / ART_VEIL, 0, 1);
        batch.setColor(1, 1, 1, shade);
        for (int i = 0; i < spores.count(); i++) {
            drawScaled(batch, veil, spores.x(i), spores.y(i), spores.radius(i));
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        long tick = sortie.tick();
        for (int i = 0; i < spores.count(); i++) {
            if (spores.burning(i)) {
                double burnt = spores.burnt(i);
                int frame = Math.min(burn.size - 1, (int) (burnt * burn.size));
                drawScaled(batch, burn.get(frame), spores.x(i), spores.y(i), wholeRadius(spores.radius(i), burnt));
            } else {
                // Each cloud a step apart in the loop, so two never swirl together.
                int frame = (int) ((tick / CLOUD_FRAME_TICKS + 3L * i) % cloud.size);
                drawScaled(batch, cloud.get(frame), spores.x(i), spores.y(i), spores.radius(i));
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void drawScaled(SpriteBatch batch, AtlasRegion region, double x, double y, double radius) {
        float size = (float) (region.getRegionWidth() * radius / ART_RADIUS);
        float height = (float) (region.getRegionHeight() * radius / ART_RADIUS);
        batch.draw(region, (float) (PixelScreen.PLAY_FIELD_X + x - size / 2), (float) (y - height / 2), size, height);
    }
}
