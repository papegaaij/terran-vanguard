package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_FLASH_TICKS;
import static vanguard.game.render.PlayFrame.HIT_WHITE;
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
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.LineBeam;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part F: Level 33's units and ground target (tools/art/belt_units_f.py, whose docstring gives the sprites;
 * design/enemies/space/void-leech, ground/rail-bunker, design/campaign Level 33's prey wings and *Claim 114*), beside
 * {@link EnemyLooks}' heading sets (the leech's swim, its stun, death and tatters) and {@link LampreyLooks} (the leech
 * latched on the ship, its sac's pulse and the drain's glow at its mouth):
 *
 * <ul>
 *   <li>a <b>prey</b> unit (a Talon a leech latched on: QF-2 a, pending round 66) drawn with its own frames darkened
 *       ({@link #PREY_TINT}, its lights dimmed with it) and tumbling slowly ({@value #TUMBLE_FPS} headings a second from
 *       its heading as it was preyed), and over it every leech riding it ({@code void-leech-prey}, the frame of the
 *       Talon's drawn heading at the sac's pulse, at the Talon's centre); a riding leech is not drawn on its own;
 *   <li>the <b>Rail Bunker</b> (in place of {@link LineLooks}' placeholder line): its fixed base ({@code rail-bunker})
 *       under the turret at 32 headings ({@code -turret}) turning toward the ship at {@value #TURRET_DEGREES}°/s and onto
 *       the locked line over the telegraph, the coils' gold glow over it as the telegraph charges ({@code -coils},
 *       additive, its opacity the charge); with the bullets the locked line ({@link LineLooks}' red telegraph line, round
 *       60's solid lock line), the rail ({@code -rail}: frames 0-1 live, 2-3 fading after it) tiled along the line from
 *       the muzzle's distance, and the muzzle flash ({@code -flash}) as it fires; its wreck ({@code -wreck}) is {@link
 *       EnemyLooks}' remains;
 *   <li><b>Claim 114</b> ({@code claim-shack}): its amber beacon blinking at {@value #BEACON_FPS} fps, shot open ({@code
 *       -open}, its last frame left on the ground).
 * </ul>
 *
 * <p>The simulation locks a line at the unit's centre when its telegraph starts and keeps it there while the bunker
 * scrolls on with the rock: the line, the rail and its flash are drawn where the simulation's line is (the hit band),
 * the turret where the bunker is.
 */
final class LeechFieldLooks {
    static final String LEECH = "void-leech";
    static final String BUNKER = "rail-bunker";
    static final String SHACK = "claim-shack";
    /**
     * M9 part H (Level 35's secret, tools/art/l35_props.py): the Ceres garrison lifeboat in the station's clamps, drawn as
     * Claim 114 is (its beacon blinking at the same 4 fps, shot open, its last frame left on the ground).
     */
    static final String LIFEBOAT = "ceres-lifeboat";
    /** The ground objects drawn this way, the first with its art in the level's atlas the one drawn. */
    static final List<String> SHACKS = List.of(SHACK, LIFEBOAT);

    /** The prey's (the Talon's) headings, and the riding leech's frames a heading (the sac's pulse). */
    static final int PREY_HEADINGS = 16;

    static final int PULSE_FRAMES = 4;
    /** The sac's pulse: one a second (frames 0..3), on the ship as on a prey. */
    static final double PULSE_FPS = 4;
    /** The preyed Talon's tint (the review's: its lights out, darkened). */
    static final Color PREY_TINT = new Color(0.50f, 0.46f, 0.56f, 1f);
    /** The preyed Talon's tumble: headings a second. */
    static final double TUMBLE_FPS = 2;

    static final int TURRET_HEADINGS = 32;
    static final double TURRET_DEGREES = 90;
    /** The turret's turn toward the ship, radians a step. */
    static final double TURRET_TURN = Math.toRadians(TURRET_DEGREES) / SimStep.PER_SECOND;
    /** The rail's strip: its live frames 0-1 alternate this often, its fading frames 2-3 show this long each. */
    static final int RAIL_FRAME_TICKS = 3;

    static final int RAIL_FRAMES = 4;
    /** The muzzle flash: steps a frame. */
    static final int FLASH_FRAME_TICKS = 2;
    /** The shack's beacon: frames a second, and its opening's steps a frame. */
    static final double BEACON_FPS = 4;

    static final int OPEN_FRAME_TICKS = 4;
    /**
     * FIX3 (H5's finding): Level 35's lifeboat opens slower, 1 s for its 6 frames: shot at the top edge under a beam its
     * 0.4 s burst went by unseen, and its last frame (the capsule, its hatch blown) reads close to the whole one.
     */
    static final int LIFEBOAT_OPEN_FRAME_TICKS = 10;
    /** A rail's live time when its bunker is gone (the data's 0.2 s). */
    private static final double DEFAULT_LIVE = 0.2;

    private static final int SLOTS = 16;
    /** A gone bunker's slot is kept this long after its last rail (its fade and flash). */
    private static final int KEEP_TICKS = 30;

    private static final long NEVER = Long.MIN_VALUE / 2;

    private final Sprites sprites;
    /** Per kind: whether it is a Rail Bunker these looks draw. */
    private final boolean[] bunker;
    /** Per kind: the leech's prey frames (empty for another kind). */
    private final Array<Array<AtlasRegion>> preyFrames = new Array<>();

    private final AtlasRegion base;
    private final Array<AtlasRegion> turret;
    private final Array<AtlasRegion> coils;
    private final Array<AtlasRegion> rail;
    private final Array<AtlasRegion> flash;
    /** The muzzle's distance from the bunker's centre, px (the pivot file's muzzle at heading 0). */
    private final double muzzle;

    /** The shack's look in this level: Claim 114's, or Level 35's lifeboat's. */
    private final String shackLook;

    private final Array<AtlasRegion> beacon;
    private final Array<AtlasRegion> opened;

    // Per followed unit (a bunker or a preyed unit), by serial.
    private final int[] serial = new int[SLOTS];
    private final boolean[] seen = new boolean[SLOTS];
    private final double[] heading = new double[SLOTS];
    /** A preyed unit: the step it was preyed and its heading index then. */
    private final long[] preyedAt = new long[SLOTS];

    private final int[] preyHeading = new int[SLOTS];
    /**
     * A bunker: its last line (kept for the fade after the rail; its origin riding with the bunker while it lives, round
     * 66's C.1 b), the step it fired.
     */
    private final double[] fromX = new double[SLOTS];

    private final double[] fromY = new double[SLOTS];
    private final double[] dirX = new double[SLOTS];
    private final double[] dirY = new double[SLOTS];
    private final boolean[] wasLive = new boolean[SLOTS];
    private final long[] firedAt = new long[SLOTS];
    private final double[] lastX = new double[SLOTS];
    private final double[] lastY = new double[SLOTS];

    // The opened shacks left on the ground.
    private final long[] openTick = new long[SLOTS];
    private final double[] openX = new double[SLOTS];
    private final double[] openGroundY = new double[SLOTS];
    private int nextOpen;

    LeechFieldLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        this.sprites = sprites;
        bunker = new boolean[kinds.size()];
        Array<AtlasRegion> none = new Array<>();
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            bunker[k] = slug.equals(BUNKER) && sprites.has(BUNKER + "-turret") && sprites.has(BUNKER);
            preyFrames.add(
                    sprites.has(slug + "-prey") && sprites.frames(slug + "-prey").size == PREY_HEADINGS * PULSE_FRAMES
                            ? sprites.frames(slug + "-prey")
                            : none);
        }
        base = sprites.has(BUNKER) ? sprites.region(BUNKER) : null;
        turret = optional(BUNKER + "-turret", none);
        coils = optional(BUNKER + "-coils", none);
        rail = optional(BUNKER + "-rail", none);
        flash = optional(BUNKER + "-flash", none);
        muzzle = muzzle(pivots.apply(BUNKER));
        shackLook = SHACKS.stream()
                .filter(look -> sprites.has(look) && sprites.has(look + "-open"))
                .findFirst()
                .orElse(SHACK);
        beacon = optional(shackLook, none);
        opened = optional(shackLook + "-open", none);
        restart();
    }

    private Array<AtlasRegion> optional(String name, Array<AtlasRegion> none) {
        return sprites.has(name) ? sprites.frames(name) : none;
    }

    /** The muzzle's distance from the turret frame's centre at heading 0 (the pivot file's {@code muzzle[0]}). */
    static double muzzle(JsonValue pivots) {
        if (pivots == null || !pivots.has("muzzle") || !pivots.has("turret")) {
            return 0;
        }
        float[] at = pivots.get("muzzle").get(0).asFloatArray();
        float[] size = pivots.get("turret").asFloatArray();
        return Math.hypot(at[0] - size[0] / 2.0, at[1] - size[1] / 2.0);
    }

    /** The level restarts: nothing followed, nothing left on the ground. */
    void restart() {
        Arrays.fill(serial, -1);
        Arrays.fill(firedAt, NEVER);
        Arrays.fill(openTick, NEVER);
        nextOpen = 0;
    }

    private boolean bunker(int kind) {
        return kind >= 0 && kind < bunker.length && bunker[kind];
    }

    private Array<AtlasRegion> preyOf(int kind) {
        return kind >= 0 && kind < preyFrames.size ? preyFrames.get(kind) : null;
    }

    /** Whether these looks draw the unit: a Rail Bunker with its art, or a preyed unit (with its riders). */
    boolean draws(Enemy enemy) {
        return bunker(enemy.kind()) || enemy.preyed();
    }

    /** Whether the unit is a leech riding a prey, drawn on its prey ({@link #draw}) and not on its own. */
    boolean riding(Enemy enemy) {
        Array<AtlasRegion> frames = preyOf(enemy.kind());
        return enemy.latchPhase() == Enemy.LatchPhase.PREY && frames != null && frames.size > 0;
    }

    /** Whether these looks draw the ground object (Claim 114 with its art) instead of {@link GroundPasses}. */
    boolean drawsObject(GroundObject object) {
        return object.spec().look().equals(shackLook) && beacon.size > 0;
    }

    // --- the frames ------------------------------------------------------------------------------

    /** The sac's pulse frame (0..3) at step {@code tick} for the unit at {@code phase}: one pulse a second. */
    static int pulseFrame(long tick, int phase) {
        return (int)
                Math.floorMod((long) Math.floor(tick * PULSE_FPS / SimStep.PER_SECOND) + phase, (long) PULSE_FRAMES);
    }

    /** The tumbling prey's heading index {@code steps} steps after it was preyed at heading index {@code from}. */
    static int tumbleHeading(int from, long steps) {
        return (int) Math.floorMod(
                from + (long) Math.floor(Math.max(0, steps) * TUMBLE_FPS / SimStep.PER_SECOND), (long) PREY_HEADINGS);
    }

    /** A riding leech's frame on a prey drawn at heading index {@code heading}. */
    static int preyIndex(int heading, int pulse) {
        return heading * PULSE_FRAMES + pulse;
    }

    /** The rail's frame {@code since} steps after it fired, live for {@code liveTicks}: 0-1 live, 2-3 fading, -1 after. */
    static int railFrame(long since, int liveTicks) {
        if (since < 0) {
            return -1;
        }
        if (since < liveTicks) {
            return (int) (since / RAIL_FRAME_TICKS % 2);
        }
        long fading = (since - liveTicks) / RAIL_FRAME_TICKS;
        return fading < 2 ? 2 + (int) fading : -1;
    }

    /** The opening's frame {@code since} steps after the shack of {@code look} was shot open: the last one stays. */
    static int openFrame(long since, int frames, String look) {
        int step = look.equals(LIFEBOAT) ? LIFEBOAT_OPEN_FRAME_TICKS : OPEN_FRAME_TICKS;
        return (int) Math.min(frames - 1, Math.max(0, since / step));
    }

    /** The beacon's frame at step {@code tick}. */
    static int beaconFrame(long tick, int frames) {
        return (int) Math.floorMod((long) Math.floor(tick * BEACON_FPS / SimStep.PER_SECOND), (long) frames);
    }

    // --- following the simulation ------------------------------------------------------------------

    /** After each step: the turrets' turns and lines, the prey's tumble, the rails fired, the shacks opened. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        Arrays.fill(seen, false);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            boolean isBunker = bunker(enemy.kind());
            if (!isBunker && !enemy.preyed()) {
                continue;
            }
            int s = slotOf(enemy.serial());
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (s < 0) {
                s = slotOf(-1);
                if (s < 0) {
                    continue;
                }
                serial[s] = enemy.serial();
                heading[s] = DeepCoreLooks.bearing(x, y, shipX, shipY);
                preyedAt[s] = tick;
                preyHeading[s] = EnemyLooks.heading(enemy.facing(), PREY_HEADINGS);
                wasLive[s] = false;
                firedAt[s] = NEVER;
            }
            seen[s] = true;
            lastX[s] = x;
            lastY[s] = y;
            if (isBunker) {
                followBunker(enemy, s, x, y, shipX, shipY, tick);
            }
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s] && serial[s] >= 0 && tick - firedAt[s] > KEEP_TICKS) {
                // Gone (a death leaves its wreck, EnemyLooks' remains); a rail it just fired fades out first.
                serial[s] = -1;
            }
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.GROUND_DESTROYED) {
                opened(sortie, events.value(i), events.x(i), events.y(i), tick);
            }
        }
    }

    private void followBunker(Enemy enemy, int s, double x, double y, double shipX, double shipY, long tick) {
        LineBeam beam = enemy.lineBeam();
        if (beam.running()) {
            fromX[s] = beam.fromX();
            fromY[s] = beam.fromY();
            dirX[s] = beam.dirX();
            dirY[s] = beam.dirY();
            heading[s] = DeepCoreLooks.bearing(0, 0, beam.dirX(), beam.dirY());
        } else {
            if (firedAt[s] != NEVER) {
                // The fading rail rides on with the bunker (the line's origin is its centre).
                fromX[s] = x;
                fromY[s] = y;
            }
            if (!enemy.empStunned()) {
                heading[s] = DeepCoreLooks.turn(heading[s], DeepCoreLooks.bearing(x, y, shipX, shipY), TURRET_TURN);
            }
        }
        boolean live = beam.live();
        if (live && !wasLive[s]) {
            firedAt[s] = tick;
        }
        wasLive[s] = live;
    }

    /** A ground object was destroyed: Claim 114 bursts open (its last frame stays). */
    private void opened(Sortie sortie, int index, double x, double y, long tick) {
        List<GroundObjectSpec> objects = sortie.script().groundObjects();
        if (index < 0 || index >= objects.size() || !objects.get(index).look().equals(shackLook) || opened.size == 0) {
            return;
        }
        openTick[nextOpen] = tick;
        openX[nextOpen] = x;
        openGroundY[nextOpen] = y + sortie.groundScroll();
        nextOpen = (nextOpen + 1) % SLOTS;
    }

    private int slotOf(int unit) {
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] == unit) {
                return s;
            }
        }
        return -1;
    }

    // --- drawing -----------------------------------------------------------------------------------

    /** On the ground under the ground units: the opened shacks. */
    void drawGround(SpriteBatch batch, Sortie sortie, double scroll) {
        long tick = sortie.tick();
        for (int k = 0; k < SLOTS; k++) {
            if (openTick[k] == NEVER) {
                continue;
            }
            double y = openGroundY[k] - scroll;
            if (y < -60 || y > PlayField.HEIGHT + 60) {
                continue;
            }
            int frame = openFrame(tick - openTick[k], opened.size, shackLook);
            drawCentred(batch, opened.get(frame), openX[k], y);
        }
    }

    /** The unit, whole: a Rail Bunker's base, turret and coils; a preyed unit darkened and tumbling, its riders on it. */
    void draw(
            SpriteBatch batch,
            Sortie sortie,
            Enemy enemy,
            EnemyLooks look,
            float alpha,
            BurrowerLooks.Flasher flasher) {
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        int s = slotOf(enemy.serial());
        if (bunker(enemy.kind())) {
            flasher.draw(batch, enemy, base, x, y);
            if (turret.size == TURRET_HEADINGS) {
                int k = EnemyLooks.heading(s >= 0 ? heading[s] : 0, TURRET_HEADINGS);
                flasher.draw(batch, enemy, turret.get(k), x, y);
                LineBeam beam = enemy.lineBeam();
                EnemySpec.LaserLine line = enemy.spec().laserLine().orElse(null);
                if (line != null && beam.telegraphing() && coils.size == TURRET_HEADINGS) {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    batch.setColor(1, 1, 1, (float) Math.clamp(beam.charge(line), 0, 1));
                    drawCentred(batch, coils.get(k), x, y);
                    batch.setColor(Color.WHITE);
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
            }
            return;
        }
        long tick = sortie.tick();
        int h = s >= 0
                ? tumbleHeading(preyHeading[s], tick - preyedAt[s])
                : EnemyLooks.heading(enemy.facing(), PREY_HEADINGS);
        AtlasRegion frame = look.frame(h * 2 * Math.PI / PREY_HEADINGS, 0);
        if (enemy.ticksSinceHit() < HIT_FLASH_TICKS) {
            flasher.draw(batch, enemy, frame, x, y);
        } else {
            batch.setColor(PREY_TINT);
            drawCentred(batch, frame, x, y);
            batch.setColor(Color.WHITE);
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy rider = sortie.enemy(i);
            if (rider.preySerial() != enemy.serial() || !riding(rider)) {
                continue;
            }
            Array<AtlasRegion> frames = preyOf(rider.kind());
            flasher.draw(batch, rider, frames.get(preyIndex(h, pulseFrame(tick, rider.serial()))), x, y);
        }
    }

    /**
     * With the bullets, in place of {@link LineLooks}' line for a bunker these looks draw: the locked line over the
     * telegraph, the rail live and fading, tiled from the muzzle's distance, the muzzle flash as it fires. Round 66's
     * C.1 b: the line's direction is locked, its origin rides with the bunker, drawn from where the bunker is drawn
     * ({@code alpha} between the steps) so it never leaves the turret.
     */
    void drawLines(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!bunker(enemy.kind())) {
                continue;
            }
            EnemySpec.LaserLine line = enemy.spec().laserLine().orElse(null);
            LineBeam beam = enemy.lineBeam();
            if (line != null && beam.telegraphing()) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                Color red = LineLooks.TELEGRAPH;
                batch.setColor(red.r, red.g, red.b, LineLooks.telegraphOpacity(beam.charge(line), tick));
                stroke(
                        batch,
                        enemy.renderX(alpha),
                        enemy.renderY(alpha),
                        beam.dirX(),
                        beam.dirY(),
                        LineLooks.TELEGRAPH_WIDTH);
                batch.setColor(Color.WHITE);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] < 0 || firedAt[s] == NEVER) {
                continue;
            }
            Enemy enemy = find(sortie, serial[s]);
            EnemySpec.LaserLine line =
                    enemy == null ? null : enemy.spec().laserLine().orElse(null);
            int liveTicks = SimStep.ticks(line == null ? DEFAULT_LIVE : line.liveSeconds());
            long since = tick - firedAt[s];
            // the origin on the bunker as drawn; its last place once it is gone
            double ox = enemy == null ? fromX[s] : enemy.renderX(alpha);
            double oy = enemy == null ? fromY[s] : enemy.renderY(alpha);
            int frame = railFrame(since, liveTicks);
            if (frame >= 0 && rail.size == RAIL_FRAMES) {
                drawRail(batch, rail.get(frame), s, ox, oy);
            }
            int flashFrame = (int) (since / FLASH_FRAME_TICKS);
            if (flashFrame < flash.size) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                drawCentred(batch, flash.get(flashFrame), ox + dirX[s] * muzzle, oy + dirY[s] * muzzle);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    private static Enemy find(Sortie sortie, int unit) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).serial() == unit) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    /** The rail's strip tiled from the muzzle's distance along the locked line from (ox, oy) past the screen's edge. */
    private void drawRail(SpriteBatch batch, AtlasRegion strip, int s, double ox, double oy) {
        float degrees = (float) Math.toDegrees(Math.atan2(dirY[s], dirX[s]));
        float w = strip.getRegionWidth();
        float h = strip.getRegionHeight();
        double length = 2 * (PlayField.WIDTH + PlayField.HEIGHT);
        for (double at = muzzle; at < length; at += w) {
            double cx = ox + dirX[s] * at;
            double cy = oy + dirY[s] * at;
            if (cx < -w || cx > PlayField.WIDTH + w || cy < -w || cy > PlayField.HEIGHT + w) {
                break;
            }
            batch.draw(strip, (float) (PlayFrame.X0 + cx), (float) (cy - h / 2), 0, h / 2, w, h, 1, 1, degrees);
        }
    }

    /** A line {@code width} px wide from (x, y) along (dx, dy) past the screen's edge. */
    private void stroke(SpriteBatch batch, double x, double y, double dx, double dy, float width) {
        float length = (float) (2 * (PlayField.WIDTH + PlayField.HEIGHT));
        float degrees = (float) Math.toDegrees(Math.atan2(dy, dx));
        batch.draw(
                sprites.pixel,
                (float) (PlayFrame.X0 + x + dx * muzzle),
                (float) (y + dy * muzzle - width / 2),
                0,
                width / 2,
                length,
                width,
                1,
                1,
                degrees);
    }

    /** Claim 114 on its ledge: the beacon blinking, a hit flashing white. */
    void drawObjects(SpriteBatch batch, Sortie sortie, float alpha, FlashShader flasher, float whiteFlash) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            if (!drawsObject(object)) {
                continue;
            }
            AtlasRegion region = beacon.get(beaconFrame(sortie.tick(), beacon.size));
            double x = object.renderX(alpha);
            double y = object.renderY(alpha);
            if (object.ticksSinceHit() < HIT_FLASH_TICKS) {
                flasher.draw(batch, region, Math.round(PlayFrame.X0 + x), Math.round(y), HIT_WHITE, whiteFlash);
            } else {
                drawCentred(batch, region, x, y);
            }
        }
    }
}
