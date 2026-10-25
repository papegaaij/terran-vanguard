package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
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
import vanguard.sim.Missile;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part G: the Depth Hunter (design/enemies/naval/depth-hunter; tools/art/depth_hunter.py, round 48 b "the flat
 * glider"), the grey submarine drone of Level 27 that rises in columns from below and fires homing torpedoes up the
 * screen. Its frame is its heading's (16, {@code k} × 22.5° from straight down, as {@link EnemyLooks#heading}) × 2 + its
 * nozzle's flicker ({@value #MOVING_FPS} fps while it moves, {@value #HOLDING_FPS} while it holds); for its first
 * {@value #RISE_SECONDS} s on the field it fades in from {@value #RISE_FROM} of itself (the column rising out of the
 * deep). After the grade, ungraded and additive ({@link #drawGlows}): its glow-only frame (the slit and the nozzles), as
 * faded. Its death (EventEffects' deep death): the hull break ({@code -break}, {@value #BREAK_FRAME_TICKS} steps a
 * frame) over its oil bloom ({@code -oil}, {@value #OIL_FRAME_TICKS} steps a frame) under the {@code medium} under-water
 * burst; no wreck.
 *
 * <p>Its torpedoes are the level's homing missiles (no walking boss's in its levels; {@link #drawTorpedoes}, with the
 * bullets): each at its heading ({@code -torpedo}, its motor's flicker at {@value #MOTOR_FPS} fps) with its motor glow
 * ({@code -torpedo-glow}, additive), its wake a puff every {@value #WAKE_EVERY_TICKS} steps behind it ({@code -wake},
 * {@value #WAKE_FRAME_TICKS} steps a frame), its launch a puff at the Hunter's tube ({@code -launch} at the pivot file's
 * {@code tubes}, the two tubes in turn, {@value #LAUNCH_FRAME_TICKS} steps a frame). Presentation only; nothing is
 * allocated per frame or step.
 */
final class HunterLooks {
    /** The unit's slug. */
    static final String SLUG = "depth-hunter";
    /** The art's headings and nozzle frames; its frame, px. */
    static final int HEADINGS = 16;

    static final int FLICKERS = 2;
    static final int FRAME = 76;
    /** The nozzle's flicker while it moves (rises, leaves) and while it holds. */
    static final double MOVING_FPS = 8;

    static final double HOLDING_FPS = 4;
    /** It moves when it went more than this many px down or up the screen in the last step. */
    static final double MOVING_STEP = 0.5;
    /** The rise's fade: from this share of itself to full over this long, s. */
    static final double RISE_SECONDS = 1.2;

    static final float RISE_FROM = 0.35f;
    /** Its death: the hull break (10 fps), the oil bloom under it (6 fps). */
    static final int BREAK_FRAME_TICKS = 6;

    static final int OIL_FRAME_TICKS = 10;
    /** The torpedo's motor flicker; its wake's puffs, their frames' steps and how far behind it they start, px. */
    static final double MOTOR_FPS = 12;

    static final int WAKE_EVERY_TICKS = 9;
    static final int WAKE_FRAME_TICKS = 5;
    static final double WAKE_BEHIND = 10;
    /** The launch puff's frames' steps (15 fps). */
    static final int LAUNCH_FRAME_TICKS = 4;
    /** The Hunters followed at once. */
    static final int SLOTS = 32;
    /** A launch lies at its Hunter's centre within this, px. */
    private static final double REACH = 6;

    private final Array<AtlasRegion> frames;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> torpedo;
    private final Array<AtlasRegion> torpedoGlow;
    private final Array<AtlasRegion> wake;
    private final Array<AtlasRegion> launch;
    private final Array<AtlasRegion> breakFrames;
    private final Array<AtlasRegion> oil;
    private final boolean[] hunter;
    /** Per heading its two tubes from the frame's centre (x right, y up), px. */
    private final float[][][] tubes = new float[HEADINGS][2][2];
    /** The torpedoes' wakes and the launches' puffs on the play field. */
    private final Effects wakes = Effects.solid();

    private final Effects launches = Effects.solid();
    /** Per followed Hunter its serial (-1 free), the step it was first seen, its last place, its launches so far. */
    private final int[] serials = new int[SLOTS];

    private final long[] firstSeen = new long[SLOTS];
    private final double[] lastX = new double[SLOTS];
    private final double[] lastY = new double[SLOTS];
    private final int[] launched = new int[SLOTS];
    private final boolean[] seen = new boolean[SLOTS];

    HunterLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        frames = optional(sprites, SLUG);
        glow = optional(sprites, SLUG + "-glow");
        torpedo = optional(sprites, SLUG + "-torpedo");
        torpedoGlow = optional(sprites, SLUG + "-torpedo-glow");
        wake = optional(sprites, SLUG + "-wake");
        launch = optional(sprites, SLUG + "-launch");
        breakFrames = optional(sprites, SLUG + "-break");
        oil = optional(sprites, SLUG + "-oil");
        hunter = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            hunter[k] = kinds.get(k).slug().equals(SLUG);
        }
        readTubes(pivots.apply(SLUG));
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** The pivot file's {@code tubes} per heading (frame px, y down) from the frame's centre; without it the centre. */
    private void readTubes(JsonValue pivot) {
        JsonValue points = pivot == null ? null : pivot.get("tubes");
        double half = FRAME / 2.0;
        for (int h = 0; h < HEADINGS; h++) {
            for (int k = 0; k < 2; k++) {
                if (points != null && h < points.size && k < points.get(h).size) {
                    JsonValue point = points.get(h).get(k);
                    tubes[h][k][0] = (float) (point.getDouble(0) - half);
                    tubes[h][k][1] = (float) (half - point.getDouble(1));
                }
            }
        }
    }

    /** A new attempt: nothing followed, no wake or launch playing. */
    void restart() {
        Arrays.fill(serials, -1);
        wakes.clear();
        launches.clear();
    }

    /** Whether kind {@code kind} is the Depth Hunter. */
    boolean hunter(int kind) {
        return kind >= 0 && kind < hunter.length && hunter[kind];
    }

    /** Whether {@code enemy} is a Depth Hunter with its art. */
    boolean draws(Enemy enemy) {
        return frames.size >= HEADINGS * FLICKERS && hunter(enemy.kind());
    }

    /** Whether the level has Depth Hunters with their torpedo art: its homing missiles are drawn as torpedoes. */
    boolean torpedoes() {
        if (torpedo.size < HEADINGS * FLICKERS) {
            return false;
        }
        for (boolean kind : hunter) {
            if (kind) {
                return true;
            }
        }
        return false;
    }

    /** The heading drawn for a facing or a missile's heading (radians from straight down). */
    static int heading(double radians) {
        return EnemyLooks.heading(radians, HEADINGS);
    }

    /** The nozzle's flicker frame at step {@code tick} for the unit {@code serial} (its own phase), moving or holding. */
    static int flicker(long tick, int serial, boolean moving) {
        double fps = moving ? MOVING_FPS : HOLDING_FPS;
        return (int) Math.floorMod((long) Math.floor(tick * fps / SimStep.PER_SECOND) + serial, (long) FLICKERS);
    }

    /** The rise's opacity {@code seconds} after it was first seen: {@value #RISE_FROM} to full over the rise. */
    static float riseOpacity(double seconds) {
        if (seconds >= RISE_SECONDS) {
            return 1;
        }
        return (float) (RISE_FROM + (1 - RISE_FROM) * Math.clamp(seconds / RISE_SECONDS, 0, 1));
    }

    /** The torpedo's motor frame at step {@code tick} for the torpedo {@code serial}. */
    static int motor(long tick, int serial) {
        return (int) Math.floorMod((long) Math.floor(tick * MOTOR_FPS / SimStep.PER_SECOND) + serial, (long) FLICKERS);
    }

    private static boolean moving(Enemy enemy) {
        return Math.abs(enemy.renderY(1) - enemy.renderY(0)) > MOVING_STEP;
    }

    /** Its frame index now. */
    int frameIndex(Sortie sortie, Enemy enemy) {
        return heading(enemy.facing()) * FLICKERS + flicker(sortie.tick(), enemy.serial(), moving(enemy));
    }

    /** Its frame now; null for another unit. */
    AtlasRegion frame(Sortie sortie, Enemy enemy) {
        return draws(enemy) ? frames.get(frameIndex(sortie, enemy)) : null;
    }

    private int slot(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /** How opaque it is drawn now (its rise's fade in). */
    float opacity(Sortie sortie, Enemy enemy, float alpha) {
        int s = slot(enemy.serial());
        if (s < 0) {
            return RISE_FROM;
        }
        return riseOpacity((sortie.tick() - firstSeen[s] + alpha) * SimStep.SECONDS);
    }

    /** A Hunter, fading in through its rise, flashing after a hit once risen. */
    void draw(SpriteBatch batch, Sortie sortie, Enemy enemy, float alpha, BurrowerLooks.Flasher flasher) {
        AtlasRegion frame = frames.get(frameIndex(sortie, enemy));
        float shown = opacity(sortie, enemy, alpha);
        if (shown < 1) {
            batch.setColor(1, 1, 1, shown);
            drawCentred(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha));
            batch.setColor(Color.WHITE);
            return;
        }
        flasher.draw(batch, enemy, frame, enemy.renderX(alpha), enemy.renderY(alpha));
    }

    /**
     * After each step: each launch's puff at the tube of the Hunter it left (the two in turn), every Hunter on the field
     * followed (its first step seen), a wake puff behind each torpedo every {@value #WAKE_EVERY_TICKS} steps.
     */
    void stepped(Sortie sortie) {
        if (frames.isEmpty()) {
            return;
        }
        long tick = sortie.tick();
        wakes.step();
        launches.step();
        Arrays.fill(seen, false);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!hunter(enemy.kind())) {
                continue;
            }
            int s = slot(enemy.serial());
            if (s < 0) {
                s = slot(-1);
                if (s < 0) {
                    continue;
                }
                serials[s] = enemy.serial();
                firstSeen[s] = tick;
                launched[s] = 0;
            }
            seen[s] = true;
            lastX[s] = enemy.renderX(1);
            lastY[s] = enemy.renderY(1);
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.MISSILE_LAUNCHED) {
                launched(sortie, events.x(e), events.y(e));
            }
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s]) {
                serials[s] = -1;
            }
        }
        if (!wake.isEmpty() && torpedoes() && tick % WAKE_EVERY_TICKS == 0) {
            for (int m = 0; m < sortie.missileCount(); m++) {
                Missile flying = sortie.missile(m);
                double heading = flying.heading();
                wakes.start(
                        wake,
                        WAKE_FRAME_TICKS,
                        flying.x() + Math.sin(heading) * WAKE_BEHIND,
                        flying.y() + Math.cos(heading) * WAKE_BEHIND);
            }
        }
    }

    /** A torpedo left (x, y): the puff at the tube of the Hunter there (its next tube), at its heading. */
    private void launched(Sortie sortie, double x, double y) {
        if (launch.isEmpty()) {
            return;
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!hunter(enemy.kind()) || Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y) > REACH) {
                continue;
            }
            int s = slot(enemy.serial());
            int tube = s < 0 ? 0 : launched[s]++ % 2;
            int h = heading(enemy.facing());
            launches.start(launch, LAUNCH_FRAME_TICKS, x + tubes[h][tube][0], y + tubes[h][tube][1]);
            return;
        }
        launches.start(launch, LAUNCH_FRAME_TICKS, x, y);
    }

    /** After the grade, additive: each Hunter's glow-only frame (its slit and nozzles), as faded as its body. */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (glow.size < HEADINGS * FLICKERS) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!draws(enemy)) {
                continue;
            }
            float shown = opacity(sortie, enemy, alpha) * ArsenalLooks.glowShare(enemy);
            if (shown <= 0) {
                continue;
            }
            batch.setColor(1, 1, 1, shown);
            drawCentred(batch, glow.get(frameIndex(sortie, enemy)), enemy.renderX(alpha), enemy.renderY(alpha));
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** With the bullets: the wakes and launch puffs, then each torpedo at its heading and its motor glow (additive). */
    void drawTorpedoes(SpriteBatch batch, Sortie sortie, float alpha) {
        if (!torpedoes()) {
            return;
        }
        wakes.draw(batch, 0);
        launches.draw(batch, 0);
        long tick = sortie.tick();
        for (int m = 0; m < sortie.missileCount(); m++) {
            Missile flying = sortie.missile(m);
            int frame = heading(flying.heading()) * FLICKERS + motor(tick, flying.serial());
            drawCentred(batch, torpedo.get(frame), flying.renderX(alpha), flying.renderY(alpha));
        }
        if (torpedoGlow.size < HEADINGS * FLICKERS) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int m = 0; m < sortie.missileCount(); m++) {
            Missile flying = sortie.missile(m);
            int frame = heading(flying.heading()) * FLICKERS + motor(tick, flying.serial());
            drawCentred(batch, torpedoGlow.get(frame), flying.renderX(alpha), flying.renderY(alpha));
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Its hull break (solid), empty for another kind. */
    Array<AtlasRegion> breakUp(int kind) {
        return hunter(kind) ? breakFrames : EMPTY;
    }

    /** Its oil bloom (solid, under the break), empty for another kind. */
    Array<AtlasRegion> oil(int kind) {
        return hunter(kind) ? oil : EMPTY;
    }

    private static final Array<AtlasRegion> EMPTY = new Array<>(0);

    /** The wake puffs and launch puffs playing (for the tests). */
    int wakes() {
        return wakes.size();
    }

    int launches() {
        return launches.size();
    }
}
