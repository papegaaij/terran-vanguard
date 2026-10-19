package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Sortie;

/**
 * M7 part E: a lane strafer's looks beyond its flight (the Ghost Drone, design/enemies/air/ghost-drone;
 * tools/art/storm_units.py, whose docstring gives the sprites), a look only. Flying it is drawn by {@link
 * EnemyLooks} (16 headings) with its eye and running lights ({@code -glow}, additive) over it in every level by
 * {@link UnitPasses#drawAirGlows}, after the storm's silhouette pass where there is one; it dies in its mechanical
 * burst there too ({@code -death} sparks, {@code -tatters} smoke and fragments, the {@code small} burst). Here: the
 * body with its lamps dark while the EMP Burst stuns it ({@code -stunned}, one per heading; its glow is out then),
 * and the red glint on its sensor eye over its gun's 0.3 s tell ({@code -glint_0..5}, additive, {@value
 * #GLINT_FRAME_TICKS} steps a frame from the step the tell starts), centred on the eye's offset at its heading
 * ({@code assets/pivots/ghost-drone.json}, {@code eye[k]}).
 */
final class GhostDroneLooks {
    /** The art's headings: heading k turns k x 22.5 degrees clockwise from straight down. */
    static final int HEADINGS = 16;
    /** The glint's frames show 3 steps each: its 6 frames are the tell's 0.3 s. */
    static final int GLINT_FRAME_TICKS = 3;
    /** The glints remembered at once (a line abreast of five, a pincer of four). */
    private static final int SLOTS = 8;

    /** Per enemy kind its frames; null for a kind that strafes no lanes (or without its art). */
    private final Frames[] frames;
    /** The units whose eye glints (their serials, -1 free) and the step their tell started. */
    private final int[] glintSerial = new int[SLOTS];

    private final long[] glintTick = new long[SLOTS];
    /** Per slot whether its unit glinted in the last step seen (a new tell starts when it glints again). */
    private final boolean[] glinting = new boolean[SLOTS];

    private int nextGlint;

    /**
     * A lane strafer's frames.
     *
     * @param eye per heading the sensor eye's centre, px from the sprite's centre, y up ({@code [heading][0]} x)
     */
    record Frames(Array<AtlasRegion> stunned, Array<AtlasRegion> glint, double[][] eye) {}

    GhostDroneLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        frames = new Frames[kinds.size()];
        for (int k = 0; k < frames.length; k++) {
            EnemySpec kind = kinds.get(k);
            String slug = kind.slug();
            if (kind.laneStrafe().isEmpty() || !sprites.has(slug)) {
                continue;
            }
            frames[k] = new Frames(
                    sprites.has(slug + "-stunned") ? sprites.frames(slug + "-stunned") : new Array<>(),
                    sprites.has(slug + "-glint") ? sprites.frames(slug + "-glint") : new Array<>(),
                    eyeOffsets(pivots.apply(slug)));
        }
        restart();
    }

    /**
     * The eye's centre per heading as px from the sprite's centre, y up, from the pivot file's {@code eye} ([dx, dy]
     * px from the centre, y down); without the file the sprite's centre.
     */
    static double[][] eyeOffsets(JsonValue pivots) {
        double[][] offsets = new double[HEADINGS][2];
        JsonValue eye = pivots == null ? null : pivots.get("eye");
        if (eye == null) {
            return offsets;
        }
        for (int h = 0; h < HEADINGS && h < eye.size; h++) {
            offsets[h][0] = eye.get(h).getDouble(0);
            offsets[h][1] = -eye.get(h).getDouble(1);
        }
        return offsets;
    }

    /** The level restarts: no glint shows. */
    void restart() {
        Arrays.fill(glintSerial, -1);
        Arrays.fill(glinting, false);
        nextGlint = 0;
    }

    /** Whether the kind has these looks. */
    boolean has(int kind) {
        return kind >= 0 && kind < frames.length && frames[kind] != null;
    }

    /** After each simulation step: a unit whose eye starts to glint (its pass trigger's tell) starts its glint. */
    void stepped(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind())) {
                continue;
            }
            int slot = slotOf(enemy.serial());
            boolean now = enemy.eyeGlinting();
            if (now && (slot < 0 || !glinting[slot])) {
                if (slot < 0) {
                    slot = nextGlint;
                    nextGlint = (nextGlint + 1) % SLOTS;
                    glintSerial[slot] = enemy.serial();
                }
                glintTick[slot] = sortie.tick();
            }
            if (slot >= 0) {
                glinting[slot] = now;
            }
        }
    }

    private int slotOf(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (glintSerial[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /** The glint's frame {@code age} steps into its tell; -1 once it has played out (or before it). */
    static int glintFrame(long age, int frames) {
        if (age < 0 || frames == 0) {
            return -1;
        }
        int frame = (int) (age / GLINT_FRAME_TICKS);
        return frame < frames ? frame : -1;
    }

    /** The unit's body with its lamps dark while the EMP Burst stuns it; null otherwise (its flight's frame). */
    AtlasRegion frame(Enemy enemy) {
        if (!has(enemy.kind()) || !enemy.empStunned()) {
            return null;
        }
        Array<AtlasRegion> stunned = frames[enemy.kind()].stunned();
        return stunned.isEmpty() ? null : stunned.get(EnemyLooks.heading(enemy.facing(), stunned.size));
    }

    /** The eye glints, additive (the batch is in additive blending): each on the eye at the unit's heading. */
    void drawGlints(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind())) {
                continue;
            }
            int slot = slotOf(enemy.serial());
            Frames own = frames[enemy.kind()];
            int frame = slot < 0 ? -1 : glintFrame(sortie.tick() - glintTick[slot], own.glint().size);
            if (frame < 0) {
                continue;
            }
            double[] eye = own.eye()[EnemyLooks.heading(enemy.facing(), HEADINGS)];
            drawCentred(batch, own.glint().get(frame), enemy.renderX(alpha) + eye[0], enemy.renderY(alpha) + eye[1]);
        }
    }
}
