package vanguard.game.render;

import static vanguard.game.render.PlayFrame.X0;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import vanguard.content.Content;
import vanguard.sim.PlayField;

/**
 * M11 part E, step E4 (design/campaign Level 47, Layout: the core's light; m11e-gaps.md §1 #9; tools/art/act7_sphere.py
 * {@code sphere}): the inner sphere's core light, the Choir core's gold-white glow at the top of the screen that grows
 * from section 3 to fill the top half by the level's end. ART-E's {@code core-light} (720 × 720, additive, smooth far
 * past 32 steps and fading to nothing inside its canvas) is drawn in screen space, after the {@code deep} layer's tiles
 * and before its pieces (so Level 48's citadel lies dark over it): centred {@value #X} px from the play field's left
 * and {@link #CURVE}'s y px below its top edge (negative: above it), scaled and weighted (its opacity) by the curve,
 * linear between its points, nothing before the first and the last point held after it. The curve is ART-E's (the
 * level's {@code backdrop} block names it in a note: no data key), so the game knows it by the level ({@link #of};
 * pending the user's review in round 92). Allocates nothing per frame.
 */
final class SphereLooks {
    /** The level that has it. */
    static final int LEVEL = 47;
    /** The light's piece in the level's backdrop atlas. */
    static final String CORE_LIGHT = "core-light";
    /** Its centre's x on the play field, px. */
    static final double X = 240;
    /** ART-E's curve: {@code [level time s, centre y px below the top edge, scale, weight]}. */
    static final double[][] CURVE = {
        {118.0, -330.0, 1.0, 0.0},
        {130.0, -320.0, 1.0, 0.3},
        {175.0, -300.0, 1.0, 0.45},
        {200.0, -280.0, 1.05, 0.55},
        {230.0, -230.0, 1.15, 0.75},
        {266.0, -150.0, 1.3, 1.0}
    };

    private final AtlasRegion light;

    private SphereLooks(AtlasRegion light) {
        this.light = light;
    }

    /** The core light for level {@code levelKey} whose atlas folder is {@code folder}; null for a level without one. */
    static SphereLooks of(Sprites sprites, String levelKey, String folder) {
        if (sprites == null
                || levelKey == null
                || Content.levelNumber(levelKey) != LEVEL
                || !sprites.hasBackdrop(folder + CORE_LIGHT)) {
            return null;
        }
        return new SphereLooks(sprites.backdrop(folder + CORE_LIGHT).first());
    }

    /** The curve's value {@code column} (1: y, 2: scale, 3: weight) at level time {@code t}: linear, held at its ends. */
    static double at(double t, int column) {
        if (t <= CURVE[0][0]) {
            return CURVE[0][column];
        }
        for (int i = 1; i < CURVE.length; i++) {
            if (t <= CURVE[i][0]) {
                double share = (t - CURVE[i - 1][0]) / (CURVE[i][0] - CURVE[i - 1][0]);
                return CURVE[i - 1][column] + (CURVE[i][column] - CURVE[i - 1][column]) * share;
            }
        }
        return CURVE[CURVE.length - 1][column];
    }

    /** Its opacity at level time {@code t}: nothing before the curve's first point. */
    static float weight(double t) {
        return t < CURVE[0][0] ? 0 : (float) at(t, 3);
    }

    /** Its centre's screen y (y up) at level time {@code t}, px. */
    static double centreY(double t) {
        return PlayField.HEIGHT - at(t, 1);
    }

    /** Over the deep layer's tiles at level time {@code t} (script time): the light, additive. */
    void draw(SpriteBatch batch, double t) {
        float weight = weight(t);
        if (weight <= 0) {
            return;
        }
        float before = batch.getPackedColor();
        float scale = (float) at(t, 2);
        float w = light.getRegionWidth() * scale;
        float h = light.getRegionHeight() * scale;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1, 1, 1, weight);
        batch.draw(light, (float) (X0 + X - w / 2), (float) (centreY(t) - h / 2), w, h);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setPackedColor(before);
    }
}
