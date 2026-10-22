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
 * M8 part C: the Vrell siphon pump on Thera Deep's farm racks (design/enemies/ground/siphon-pump, Level 23's secondary;
 * tools/art/siphon_pump.py): its feeder roots spread into the rack under it ({@code -roots}, a ground decal under the
 * ground units), its bellows heave at {@value #FPS} fps (its {@code siphon-pump_0..3}, {@link EnemyLooks}), the sump's
 * teal light over it after the grade ({@code -glow}, additive, the same frame). Its death (EventEffects) is the
 * under-water {@code medium} burst with its break ({@code -break}: the sap gushing up, {@value #BREAK_FRAME_TICKS} steps
 * a frame) and its remains on the ground: the dead pump over the withered roots ({@code -dead}, {@code -roots-dead}).
 */
final class SiphonPumpLooks {
    /** The unit's slug. */
    static final String SLUG = "siphon-pump";
    /** The heave loop's rate (the art's 6 fps). */
    static final double FPS = 6;
    /** The break's frames show this many steps. */
    static final int BREAK_FRAME_TICKS = 3;

    private final AtlasRegion roots;
    private final Array<AtlasRegion> glow;
    private final boolean[] pump;
    private final Array<AtlasRegion> breakFrames;
    private final Array<Array<AtlasRegion>> remains;

    SiphonPumpLooks(Sprites sprites, List<EnemySpec> kinds) {
        roots = sprites.has(SLUG + "-roots") ? sprites.frames(SLUG + "-roots").first() : null;
        glow = sprites.has(SLUG + "-glow") ? sprites.frames(SLUG + "-glow") : new Array<>(0);
        breakFrames = sprites.has(SLUG + "-break") ? sprites.frames(SLUG + "-break") : new Array<>(0);
        remains = new Array<>(2);
        for (String name : List.of(SLUG + "-roots-dead", SLUG + "-dead")) {
            if (sprites.has(name)) {
                Array<AtlasRegion> one = new Array<>(1);
                one.add(sprites.frames(name).first());
                remains.add(one);
            }
        }
        pump = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            pump[k] = kinds.get(k).slug().equals(SLUG);
        }
    }

    /** Whether enemy kind {@code kind} is a siphon pump. */
    boolean pump(int kind) {
        return kind >= 0 && kind < pump.length && pump[kind];
    }

    /** The break's frames (empty without). */
    Array<AtlasRegion> breakFrames() {
        return breakFrames;
    }

    /** The remains laid on the ground at the death, each a one-frame animation, the withered roots first. */
    Array<Array<AtlasRegion>> remains() {
        return remains;
    }

    /** On the ground, under the ground units: the live pumps' roots. */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha) {
        if (roots == null) {
            return;
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (pump(enemy.kind())) {
                PlayFrame.drawCentred(batch, roots, enemy.renderX(alpha), enemy.renderY(alpha));
            }
        }
    }

    /**
     * After the grade: each live pump's sump light at its body's frame ({@code looks}: the level's; its step as {@link
     * UnitPasses} draws the body, the enemy's index its phase).
     */
    void drawGlows(SpriteBatch batch, Sortie sortie, EnemyLooks[] looks, float alpha) {
        if (glow.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!pump(enemy.kind())) {
                continue;
            }
            long step = looks[enemy.kind()].step(sortie.tick(), i);
            PlayFrame.drawCentred(
                    batch, glow.get((int) (step % glow.size)), enemy.renderX(alpha), enemy.renderY(alpha));
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
