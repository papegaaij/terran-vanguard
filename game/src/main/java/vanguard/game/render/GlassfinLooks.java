package vanguard.game.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Sortie;

/**
 * M8 part C: the Glassfin (design/enemies/naval/glassfin; tools/art/glassfin.py, round 48 a "needle fry"), the tiny
 * school popcorn at 16 headings: its swimming frame, or while it glints before its ram ({@link Enemy#ramGlinting}) its
 * glint frame ({@code glassfin}, heading × 2 + glint); over a glinting fry, after the grade, the tell's sparkle ({@code
 * -glint}, additive, {@value #GLINT_FRAME_TICKS} steps a frame from the glint's start, then held). Its glass-shatter
 * death is its {@code -death} flash and {@code -shards} (EventEffects, {@link #SHARDS}). The schools' ribbons and the
 * rams are the simulation's. Presentation only.
 */
final class GlassfinLooks {
    /** The unit's slug. */
    static final String SLUG = "glassfin";
    /** Its shatter's glass slivers, solid, played with the flash. */
    static final String SHARDS = SLUG + "-shards";
    /** The art's headings and frames per heading (swimming, glint). */
    static final int HEADINGS = 16;

    static final int STATES = 2;
    /** The sparkle's frames show this many steps (0.3 s over its 4 frames). */
    static final int GLINT_FRAME_TICKS = 5;
    /** The most fry glinting at once that the sparkle follows. */
    static final int SLOTS = 64;

    private final Array<AtlasRegion> frames;
    private final Array<AtlasRegion> glint;
    private final boolean[] glassfin;
    /** The glinting fry by serial (-1 free) and the step their glint started. */
    private final int[] glintSerial = new int[SLOTS];

    private final long[] glintTick = new long[SLOTS];

    GlassfinLooks(Sprites sprites, List<EnemySpec> kinds) {
        frames = sprites.has(SLUG) ? sprites.frames(SLUG) : new Array<>(0);
        glint = sprites.has(SLUG + "-glint") ? sprites.frames(SLUG + "-glint") : new Array<>(0);
        glassfin = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            glassfin[k] = kinds.get(k).slug().equals(SLUG);
        }
        restart();
    }

    /** A new attempt: no glint remembered. */
    void restart() {
        java.util.Arrays.fill(glintSerial, -1);
    }

    /** Whether {@code enemy} is a Glassfin with its art. */
    boolean draws(Enemy enemy) {
        return frames.size >= HEADINGS * STATES && enemy.kind() < glassfin.length && glassfin[enemy.kind()];
    }

    /** Its frame: its heading's swimming frame, or its glint frame while it glints; null for another unit. */
    AtlasRegion frame(Enemy enemy) {
        if (!draws(enemy)) {
            return null;
        }
        int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
        return frames.get(heading * STATES + (enemy.ramGlinting() ? 1 : 0));
    }

    /** After each step: the fry that started to glint are noted, those that stopped forgotten. */
    void stepped(Sortie sortie) {
        if (glint.isEmpty()) {
            return;
        }
        for (int s = 0; s < SLOTS; s++) {
            if (glintSerial[s] >= 0 && !glinting(sortie, glintSerial[s])) {
                glintSerial[s] = -1;
            }
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (draws(enemy) && enemy.ramGlinting() && slot(enemy.serial()) < 0) {
                int free = slot(-1);
                if (free >= 0) {
                    glintSerial[free] = enemy.serial();
                    glintTick[free] = sortie.tick();
                }
            }
        }
    }

    private static boolean glinting(Sortie sortie, int serial) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.serial() == serial) {
                return enemy.ramGlinting();
            }
        }
        return false;
    }

    private int slot(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (glintSerial[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /** The sparkle's frame {@code ticks} steps into a glint: one every {@value #GLINT_FRAME_TICKS} steps, the last held. */
    static int glintFrame(long ticks, int frames) {
        return Math.clamp(ticks / GLINT_FRAME_TICKS, 0, frames - 1);
    }

    /** After the grade: the sparkle over every glinting fry. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (glint.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!draws(enemy) || !enemy.ramGlinting()) {
                continue;
            }
            int s = slot(enemy.serial());
            long since = s < 0 ? 0 : sortie.tick() - glintTick[s];
            PlayFrame.drawCentred(
                    batch, glint.get(glintFrame(since, glint.size)), enemy.renderX(alpha), enemy.renderY(alpha));
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
