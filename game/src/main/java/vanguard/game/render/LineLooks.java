package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.LineBeam;
import vanguard.sim.Sortie;

/**
 * M9 part A: the {@code laser-line} as the player sees it (design/enemies/ground/rail-bunker, the Sovereign's batteries,
 * the mass relay; {@link LineBeam}), drawn with the bullets, additive, until its own art (part F's Rail Bunker): over
 * the telegraph a thin red line locked from the unit through where the ship was to the screen's edge, brightening with
 * the charge and flickering in its last fifth ({@link #telegraphOpacity}); then the live rail for its live time, a
 * band as wide as the line's hit band ({@code band}) in the Ascendancy's gold round a white core. Nothing for a unit without a line or while none runs.
 */
final class LineLooks {
    /** The telegraph's line: 2 px of the Ascendancy's warning red. */
    static final Color TELEGRAPH = Color.valueOf("FF3A28");

    static final float TELEGRAPH_WIDTH = 2;
    /** The telegraph's opacity from its start to its charge, and the flicker in its last share. */
    static final float TELEGRAPH_LOW = 0.3f;

    static final float TELEGRAPH_HIGH = 0.9f;
    static final double FLICKER_FROM = 0.8;
    /** The live rail: its gold band and white core. */
    static final Color RAIL = Color.valueOf("FFC850");

    static final Color CORE = Color.WHITE;
    static final float RAIL_OPACITY = 0.7f;
    static final float CORE_WIDTH = 2;

    private final TextureRegion pixel;

    LineLooks(TextureRegion pixel) {
        this.pixel = pixel;
    }

    /** The telegraph's opacity at its {@code charge} (0..1) on step {@code tick}: rising, flickering at its end. */
    static float telegraphOpacity(double charge, long tick) {
        float opacity = (float) (TELEGRAPH_LOW + (TELEGRAPH_HIGH - TELEGRAPH_LOW) * Math.clamp(charge, 0, 1));
        return charge >= FLICKER_FROM && tick / 2 % 2 == 1 ? opacity * 0.4f : opacity;
    }

    /** With the bullets: every running line. */
    void draw(SpriteBatch batch, Sortie sortie) {
        boolean any = false;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().laserLine().isEmpty() || !enemy.lineBeam().running()) {
                continue;
            }
            if (!any) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                any = true;
            }
            EnemySpec.LaserLine line = enemy.spec().laserLine().get();
            LineBeam beam = enemy.lineBeam();
            if (beam.telegraphing()) {
                batch.setColor(
                        TELEGRAPH.r, TELEGRAPH.g, TELEGRAPH.b, telegraphOpacity(beam.charge(line), sortie.tick()));
                drawLine(batch, beam, TELEGRAPH_WIDTH);
            } else if (beam.live()) {
                batch.setColor(RAIL.r, RAIL.g, RAIL.b, RAIL_OPACITY);
                drawLine(batch, beam, (float) Math.max(CORE_WIDTH + 2, line.bandPx()));
                batch.setColor(CORE);
                drawLine(batch, beam, CORE_WIDTH);
            }
        }
        if (any) {
            batch.setColor(Color.WHITE);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** The locked line from its origin to past the screen's edge, {@code width} px wide. */
    private void drawLine(SpriteBatch batch, LineBeam beam, float width) {
        float length = (float) (2 * (vanguard.sim.PlayField.WIDTH + vanguard.sim.PlayField.HEIGHT));
        float degrees = (float) Math.toDegrees(Math.atan2(beam.dirY(), beam.dirX()));
        batch.draw(
                pixel,
                (float) (PixelScreen.PLAY_FIELD_X + beam.fromX()),
                (float) (beam.fromY() - width / 2),
                0,
                width / 2,
                length,
                width,
                1,
                1,
                degrees);
    }
}
