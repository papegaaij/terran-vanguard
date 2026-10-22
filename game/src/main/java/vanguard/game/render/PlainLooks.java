package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.Enemy;
import vanguard.sim.Sortie;

/**
 * M8 part D: the walkers on the sea floor (Level 24's abyssal plain; tools/art/l24_props.py): under water each stride of
 * a Scuttler kicks up a puff of silt at its foot ({@code scuttler-silt}, 8 frames at 16 fps, a low grey-green cloud
 * spreading and settling, no bubbles), left on the ground where it rose. Presentation only.
 */
final class PlainLooks {
    /** The walker whose strides raise the silt. */
    static final String WALKER = "scuttler";
    /** The silt's frames show this many steps each (16 fps). */
    static final int SILT_FRAME_TICKS = 4;
    /** The puff rises this far behind the walker's centre, at its feet, px. */
    static final double FOOT_BEHIND = 6;
    /** The most walkers followed at once. */
    static final int SLOTS = 32;

    private final Array<AtlasRegion> silt;
    private final Effects puffs = Effects.solid();
    /** The walkers followed by serial (-1 free) and the strides they had walked at the last step. */
    private final int[] serials = new int[SLOTS];

    private final long[] strides = new long[SLOTS];
    private final boolean[] seen = new boolean[SLOTS];

    PlainLooks(Sprites sprites) {
        silt = sprites.has(WALKER + "-silt") ? sprites.frames(WALKER + "-silt") : new Array<>(0);
        Arrays.fill(serials, -1);
    }

    /** The stride a walker is in after walking {@code walked} px with strides of {@code stride} px. */
    static long strideOf(double walked, double stride) {
        return (long) Math.floor(walked / stride);
    }

    /** After each step: a puff under each Scuttler that began a stride under water. */
    void stepped(Sortie sortie, EnemyLooks[] looks) {
        if (silt.isEmpty() || !sortie.underwater()) {
            return;
        }
        Arrays.fill(seen, false);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!enemy.walking() || !enemy.spec().slug().equals(WALKER)) {
                continue;
            }
            double stride = looks[enemy.kind()].stride();
            long now = strideOf(enemy.walked(), stride);
            int s = slot(enemy.serial());
            if (s < 0) {
                s = slot(-1);
                if (s < 0) {
                    continue;
                }
                serials[s] = enemy.serial();
                strides[s] = now;
            }
            seen[s] = true;
            if (now != strides[s]) {
                strides[s] = now;
                double facing = enemy.facing();
                puffs.startOnGround(
                        silt,
                        SILT_FRAME_TICKS,
                        enemy.renderX(1) + Math.sin(facing) * FOOT_BEHIND,
                        enemy.renderY(1) + Math.cos(facing) * FOOT_BEHIND,
                        0,
                        sortie.groundScroll());
            }
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s]) {
                serials[s] = -1;
            }
        }
    }

    private int slot(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /** One simulation step of the puffs. */
    void step() {
        puffs.step();
    }

    /** The puffs on the ground, under the ground units. */
    void draw(SpriteBatch batch, double groundScroll) {
        puffs.draw(batch, groundScroll);
    }

    /** A new attempt: no puffs, no walkers followed. */
    void clear() {
        puffs.clear();
        Arrays.fill(serials, -1);
    }
}
