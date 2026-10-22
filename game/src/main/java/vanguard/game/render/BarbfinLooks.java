package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part D: the Barbfin (design/enemies/naval/barbfin; tools/art/barbfin.py, round 48 a "bone lionfish") at its 32
 * headings: its swim (frames 0 and 1, {@value #SWIM_FPS} fps while it moves, {@value #HOLD_FPS} in its hold) and its
 * shot's tell (frame 2) for the {@value #TELL_SECONDS} s before each needle. The simulation tells no gun's timer, so the
 * tell is foreseen from its last needle ({@code ENEMY_FIRED} at it) and its gun's interval: its first needle comes
 * without a tell. After the grade the snout's violet sparkle ({@code -snout}, additive, {@value #SNOUT_FRAME_TICKS}
 * steps a frame from the tell and from each needle, at the heading's {@code snout} of {@code pivots/barbfin.json}); its
 * eyes' and barb's glow ({@code -glow}) is the air units' glow pass. Its death under water: the {@code small} burst, its
 * shell sinking ({@code -shell}, every 4th heading × 4 steps at 5 fps, drifting down) and an ink-grey cloud ({@code
 * -ink}). Its violet needle is {@link BulletLooks}'. Presentation only.
 */
final class BarbfinLooks {
    /** The unit's slug. */
    static final String SLUG = "barbfin";
    /** The art's headings and frames per heading (two swim frames, the tell). */
    static final int HEADINGS = 32;

    static final int STATES = 3;
    static final int TELL = 2;
    /** The swim's frame rate while it moves and in its hold (slower: its fins only stir). */
    static final double SWIM_FPS = 6;

    static final double HOLD_FPS = 2;
    /** Slower than this it holds, px/s. */
    static final double HOLD_SPEED = 12;
    /** The tell shows this long before a needle. */
    static final double TELL_SECONDS = 0.25;
    /** The snout sparkle's frames show this many steps each. */
    static final int SNOUT_FRAME_TICKS = 2;
    /** The shell's headings (every 4th of the 32), its steps (5 fps) and its sinking drift, px/s down. */
    static final int SHELL_HEADINGS = 8;

    static final int SHELL_STEPS = 4;
    static final int SHELL_FRAME_TICKS = 12;
    static final double SHELL_SINK = 22.5;
    /** The ink cloud plays at 10 fps, a few px below the kill. */
    static final int INK_FRAME_TICKS = 6;

    static final double INK_BELOW = 4;
    /** The most Barbfins followed at once. */
    static final int SLOTS = 64;
    /** A volley's event lies within this distance of the Barbfin that fired it; a death's within the second, px. */
    private static final double FIRED_REACH = 6;

    private static final double DEATH_REACH = 24;

    private final Array<AtlasRegion> frames;
    private final Array<AtlasRegion> snout;
    private final Array<Array<AtlasRegion>> shells = new Array<>();
    private final Array<AtlasRegion> ink;
    private final boolean[] barbfin;
    /** Per kind its gun's interval, steps (0 without a gun). */
    private final int[] interval;
    /** Per heading the snout's offset from the frame's centre (x right, y up), px. */
    private final float[][] snoutAt = new float[HEADINGS][2];
    /** The Barbfins followed by serial (-1 free): their last needle's step, place and heading, seen in the last step. */
    private final int[] serials = new int[SLOTS];

    private final long[] shot = new long[SLOTS];
    private final double[] lastX = new double[SLOTS];
    private final double[] lastY = new double[SLOTS];
    private final int[] lastHeading = new int[SLOTS];
    private final boolean[] seen = new boolean[SLOTS];
    private final Array<AtlasRegion> none = new Array<>(0);

    BarbfinLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        frames = optional(sprites, SLUG);
        snout = optional(sprites, SLUG + "-snout");
        ink = optional(sprites, SLUG + "-ink");
        Array<AtlasRegion> shell = optional(sprites, SLUG + "-shell");
        if (shell.size >= SHELL_HEADINGS * SHELL_STEPS) {
            for (int h = 0; h < SHELL_HEADINGS; h++) {
                Array<AtlasRegion> steps = new Array<>(SHELL_STEPS);
                for (int s = 0; s < SHELL_STEPS; s++) {
                    steps.add(shell.get(h * SHELL_STEPS + s));
                }
                shells.add(steps);
            }
        }
        barbfin = new boolean[kinds.size()];
        interval = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            EnemySpec kind = kinds.get(k);
            barbfin[k] = kind.slug().equals(SLUG);
            interval[k] = kind.gun()
                    .filter(gun -> Double.isFinite(gun.intervalSeconds()))
                    .map(gun -> SimStep.ticks(gun.intervalSeconds()))
                    .orElse(0);
        }
        JsonValue pivot = pivots.apply(SLUG);
        JsonValue snouts = pivot == null ? null : pivot.get("snout");
        double width =
                pivot == null || !pivot.has("frame") ? 40 : pivot.get("frame").getDouble(0);
        double height =
                pivot == null || !pivot.has("frame") ? 40 : pivot.get("frame").getDouble(1);
        for (int h = 0; snouts != null && h < HEADINGS && h < snouts.size; h++) {
            snoutAt[h][0] = (float) (snouts.get(h).getDouble(0) - width / 2);
            snoutAt[h][1] = (float) (height / 2 - snouts.get(h).getDouble(1));
        }
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** A new attempt: none followed. */
    void restart() {
        Arrays.fill(serials, -1);
    }

    /** Whether kind {@code kind} is the Barbfin. */
    boolean barbfin(int kind) {
        return kind >= 0 && kind < barbfin.length && barbfin[kind];
    }

    /** Whether {@code enemy} is a Barbfin with its art. */
    boolean draws(Enemy enemy) {
        return frames.size >= HEADINGS * STATES && barbfin(enemy.kind());
    }

    private int slot(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /** Whether a Barbfin whose last needle left at step {@code shot} tells its next at step {@code tick}. */
    static boolean telling(long tick, long shot, int interval) {
        if (interval <= 0 || shot == Long.MIN_VALUE) {
            return false;
        }
        long next = shot + interval;
        return tick >= next - SimStep.ticks(TELL_SECONDS) && tick < next;
    }

    /** The swim frame (0 or 1) at {@code tick}: {@value #SWIM_FPS} fps moving, {@value #HOLD_FPS} fps holding. */
    static int swimFrame(long tick, int phase, boolean holding) {
        double fps = holding ? HOLD_FPS : SWIM_FPS;
        return (int) Math.floorMod((long) Math.floor(tick * fps / SimStep.PER_SECOND) + phase, 2L);
    }

    /** Its frame: its heading's swim frame, or its tell frame before a needle; null for another unit. */
    AtlasRegion frame(Enemy enemy, long tick) {
        if (!draws(enemy)) {
            return null;
        }
        int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
        int s = slot(enemy.serial());
        if (s >= 0 && telling(tick, shot[s], interval[enemy.kind()])) {
            return frames.get(heading * STATES + TELL);
        }
        double speed = Math.hypot(enemy.renderX(1) - enemy.renderX(0), enemy.renderY(1) - enemy.renderY(0))
                * SimStep.PER_SECOND;
        return frames.get(heading * STATES + swimFrame(tick, enemy.serial(), speed < HOLD_SPEED));
    }

    /** After each step: the Barbfins on the field (their place and heading) and the needles they fired. */
    void stepped(Sortie sortie) {
        if (frames.isEmpty()) {
            return;
        }
        Arrays.fill(seen, false);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!barbfin(enemy.kind())) {
                continue;
            }
            int s = slot(enemy.serial());
            if (s < 0) {
                s = slot(-1);
                if (s < 0) {
                    continue;
                }
                serials[s] = enemy.serial();
                shot[s] = Long.MIN_VALUE;
            }
            seen[s] = true;
            lastX[s] = enemy.renderX(1);
            lastY[s] = enemy.renderY(1);
            lastHeading[s] = EnemyLooks.heading(enemy.facing(), HEADINGS);
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) != SimEvents.Type.ENEMY_FIRED) {
                continue;
            }
            for (int s = 0; s < SLOTS; s++) {
                if (seen[s]
                        && Math.abs(lastX[s] - events.x(e)) <= FIRED_REACH
                        && Math.abs(lastY[s] - events.y(e)) <= FIRED_REACH) {
                    shot[s] = sortie.tick();
                }
            }
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s]) {
                serials[s] = -1;
            }
        }
    }

    /** The sparkle's frame {@code ticks} steps after its start: -1 once it has played (or before it). */
    static int sparkleFrame(long ticks, int frames) {
        long frame = ticks / SNOUT_FRAME_TICKS;
        return ticks < 0 || frame >= frames ? -1 : (int) frame;
    }

    /** After the grade: the snout's sparkle over each Barbfin telling a needle or just after one. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (snout.isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int s = draws(enemy) ? slot(enemy.serial()) : -1;
            if (s < 0 || shot[s] == Long.MIN_VALUE) {
                continue;
            }
            int every = interval[enemy.kind()];
            int frame = sparkleFrame(tick - shot[s], snout.size);
            if (frame < 0 && every > 0) {
                frame = sparkleFrame(tick - (shot[s] + every - SimStep.ticks(TELL_SECONDS)), snout.size);
            }
            if (frame < 0) {
                continue;
            }
            int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
            drawCentred(
                    batch,
                    snout.get(frame),
                    enemy.renderX(alpha) + snoutAt[heading][0],
                    enemy.renderY(alpha) + snoutAt[heading][1]);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The heading (of 32) of the Barbfin last seen nearest (x, y); 0 without one. */
    private int headingNear(double x, double y) {
        int heading = 0;
        double best = DEATH_REACH;
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] < 0) {
                continue;
            }
            double d = Math.hypot(lastX[s] - x, lastY[s] - y);
            if (d <= best) {
                best = d;
                heading = lastHeading[s];
            }
        }
        return heading;
    }

    /** A killed Barbfin's sinking shell at its heading; empty for another kind. */
    Array<AtlasRegion> shell(int kind, double x, double y) {
        if (!barbfin(kind) || shells.isEmpty()) {
            return none;
        }
        return shells.get(headingNear(x, y) * SHELL_HEADINGS / HEADINGS);
    }

    /** A killed Barbfin's ink cloud; empty for another kind. */
    Array<AtlasRegion> ink(int kind) {
        return barbfin(kind) ? ink : none;
    }
}
