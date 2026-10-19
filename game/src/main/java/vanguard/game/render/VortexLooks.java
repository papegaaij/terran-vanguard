package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part E: a vortex's looks (the Dust Devil, design/enemies/ground/dust-devil; tools/art/storm_units.py, whose
 * docstring gives the sprites), a look only. Its body spins in radial frames ({@code dust-devil}, 5-fold: 8 frames
 * cover 72 degrees), {@value #CLOSED_SPIN_FPS} frames/s while closed and {@value #OPEN_SPIN_FPS} from its rise on,
 * the spin carried on per unit so a change of speed does not jump; over its 0.4 s rise (the tell) its vanes part in
 * three steps ({@code -rise}, step x 8 + spin) and close again backwards while it sinks; open on {@code low-air} it
 * shows its open set ({@code -open}) with the lime core's glow pulsing over it ({@code -core}, additive; through the
 * storm's veil {@link StormLooks} draws it faintly). Under it on the ground its ochre grit swirl turns ({@code
 * -swirl}, about 150 px), crossfading over the rise into the tightened, brighter swirl ({@code -swirl-tight}) it
 * keeps while open and back while it sinks. Its death collapses the funnel ({@code -collapse}, {@link EnemyLooks}'
 * collapse under the grit cloud of its {@code -tatters}, with the {@code medium} burst and its {@code -death} light).
 * The pickups it holds circle in its swirl where the simulation carries them; its grit is the standard {@code small}
 * enemy bullet, the closed vanes' glance the game's glance spark.
 */
final class VortexLooks {
    /** The spin frames of a set: 8 frames over the 5-fold vanes' 72 degrees. */
    static final int SPIN_FRAMES = 8;
    /** The rise's steps (a third, two thirds, nearly open), each a set of spin frames. */
    static final int RISE_STEPS = 3;
    /** The spin, frames/s: closed (180 degrees/s), and faster from the rise on (270 degrees/s). */
    static final double CLOSED_SPIN_FPS = 20;

    static final double OPEN_SPIN_FPS = 30;
    /** The grit swirl's loop, frames/s (its 12 frames turn it 120 degrees), and the tightened one's. */
    static final double SWIRL_FPS = 12;

    static final double TIGHT_FPS = 16;
    /** The core's pulse: a frame every 5 steps (12 fps), as through the veil ({@link StormLooks}). */
    static final int CORE_FRAME_TICKS = 5;
    /** The vortices whose spin is carried on at once (a level's trio, a few more on the screen at most). */
    private static final int SLOTS = 12;

    /** Per enemy kind its frames; null for a kind without a vortex (or without its art). */
    private final Frames[] frames;
    /** The vortices whose spin is carried on (their serials, -1 free) and how far each has spun, in frames. */
    private final int[] spinSerial = new int[SLOTS];

    private final double[] spun = new double[SLOTS];
    private int nextSpin;

    /** A vortex's frames; {@code rise}, {@code core}, the swirls and {@code collapse} may be empty. */
    record Frames(
            Array<AtlasRegion> closed,
            Array<AtlasRegion> rise,
            Array<AtlasRegion> open,
            Array<AtlasRegion> core,
            Array<AtlasRegion> swirl,
            Array<AtlasRegion> tight) {}

    VortexLooks(Sprites sprites, List<EnemySpec> kinds) {
        frames = new Frames[kinds.size()];
        for (int k = 0; k < frames.length; k++) {
            EnemySpec kind = kinds.get(k);
            String slug = kind.slug();
            if (kind.vortex().isEmpty() || !sprites.has(slug) || !sprites.has(slug + "-open")) {
                continue;
            }
            frames[k] = new Frames(
                    sprites.frames(slug),
                    optional(sprites, slug + "-rise"),
                    sprites.frames(slug + "-open"),
                    optional(sprites, slug + "-core"),
                    optional(sprites, slug + "-swirl"),
                    optional(sprites, slug + "-swirl-tight"));
        }
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>();
    }

    /** The level restarts: no spin is carried on. */
    void restart() {
        Arrays.fill(spinSerial, -1);
        Arrays.fill(spun, 0);
        nextSpin = 0;
    }

    /** Whether the kind has its vortex looks. */
    boolean has(int kind) {
        return kind >= 0 && kind < frames.length && frames[kind] != null;
    }

    /** The spin's speed in a phase, frames/s: slow while closed or sinking, fast from the rise to the close. */
    static double spinFps(Enemy.VortexPhase phase) {
        return phase == Enemy.VortexPhase.RISING || phase == Enemy.VortexPhase.OPEN ? OPEN_SPIN_FPS : CLOSED_SPIN_FPS;
    }

    /** After each simulation step: each vortex's spin turns on by its phase's speed. */
    void stepped(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind())) {
                continue;
            }
            int slot = slot(enemy.serial());
            spun[slot] += spinFps(enemy.vortexPhase()) / SimStep.PER_SECOND;
        }
    }

    /** The unit's spin slot, taken over from the oldest when it has none (a new unit starts at its own phase). */
    private int slot(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (spinSerial[s] == serial) {
                return s;
            }
        }
        int slot = nextSpin;
        nextSpin = (nextSpin + 1) % SLOTS;
        spinSerial[slot] = serial;
        spun[slot] = Math.floorMod(serial * 3, SPIN_FRAMES);
        return slot;
    }

    /** How far the unit has spun, in frames (its serial's phase before its first step). */
    private double spun(Enemy enemy) {
        for (int s = 0; s < SLOTS; s++) {
            if (spinSerial[s] == enemy.serial()) {
                return spun[s];
            }
        }
        return Math.floorMod(enemy.serial() * 3, SPIN_FRAMES);
    }

    /** The spin frame after spinning {@code spun} frames. */
    static int spinFrame(double spun) {
        return (int) Math.floorMod((long) Math.floor(spun), (long) SPIN_FRAMES);
    }

    /**
     * The rise's step {@code progress} into a phase: its three steps over the rise, backwards while it sinks; -1 in
     * the other phases.
     */
    static int riseStep(Enemy.VortexPhase phase, double progress) {
        int step = Math.clamp((int) Math.floor(progress * RISE_STEPS), 0, RISE_STEPS - 1);
        return switch (phase) {
            case RISING -> step;
            case SINKING -> RISE_STEPS - 1 - step;
            case CLOSED, OPEN -> -1;
        };
    }

    /**
     * The tightened swirl's share {@code progress} into a phase (the loose swirl takes the rest): none while closed,
     * growing over the rise (the tell), all of it while open, shrinking while it sinks.
     */
    static float tightShare(Enemy.VortexPhase phase, double progress) {
        float share = (float) Math.clamp(progress, 0, 1);
        return switch (phase) {
            case CLOSED -> 0;
            case RISING -> share;
            case OPEN -> 1;
            case SINKING -> 1 - share;
        };
    }

    /** The unit's body frame in its phase; null for a unit without these looks. */
    AtlasRegion frame(Enemy enemy) {
        if (!has(enemy.kind())) {
            return null;
        }
        Frames own = frames[enemy.kind()];
        int spin = spinFrame(spun(enemy));
        Enemy.VortexPhase phase = enemy.vortexPhase();
        if (phase == Enemy.VortexPhase.OPEN) {
            return own.open().get(spin % own.open().size);
        }
        int step = riseStep(phase, enemy.vortexProgress());
        if (step >= 0 && own.rise().size == RISE_STEPS * SPIN_FRAMES) {
            return own.rise().get(step * SPIN_FRAMES + spin);
        }
        return own.closed().get(spin % own.closed().size);
    }

    /** The grit swirls under the vortices, on the ground under the ground units: loose, tightened, or crossfading. */
    void drawSwirls(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind())) {
                continue;
            }
            Frames own = frames[enemy.kind()];
            float tight = own.tight().isEmpty() ? 0 : tightShare(enemy.vortexPhase(), enemy.vortexProgress());
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (tight < 1 && !own.swirl().isEmpty()) {
                batch.setColor(1, 1, 1, 1 - tight);
                drawCentred(batch, loop(own.swirl(), tick, SWIRL_FPS, enemy.serial()), x, y);
            }
            if (tight > 0) {
                batch.setColor(1, 1, 1, tight);
                drawCentred(batch, loop(own.tight(), tick, TIGHT_FPS, enemy.serial()), x, y);
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** A loop's frame at {@code fps} at step {@code tick}, each unit at its own phase. */
    private static AtlasRegion loop(Array<AtlasRegion> set, long tick, double fps, int phase) {
        long frame = (long) Math.floor(tick * fps / SimStep.PER_SECOND) + phase;
        return set.get((int) Math.floorMod(frame, (long) set.size));
    }

    /** Over an open vortex's body (additive): its lime core's pulse. */
    void drawCore(SpriteBatch batch, Enemy enemy, long tick, float alpha) {
        if (!has(enemy.kind()) || enemy.vortexPhase() != Enemy.VortexPhase.OPEN) {
            return;
        }
        Array<AtlasRegion> core = frames[enemy.kind()].core();
        if (core.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        long frame = tick / CORE_FRAME_TICKS + enemy.serial();
        drawCentred(batch, core.get((int) (frame % core.size)), enemy.renderX(alpha), enemy.renderY(alpha));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
