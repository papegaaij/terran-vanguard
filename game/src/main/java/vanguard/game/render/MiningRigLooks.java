package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.AirLinks;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part D: Level 31's units' looks beyond {@link EnemyLooks}' heading set (tools/art/belt_units_d.py, whose docstring
 * gives the sprites; design/enemies/air/hornet, space/shard-drone, air/buzzsaw-drone), a look only:
 *
 * <ul>
 *   <li>the Hornet (facing down only: {@code hornet} frame {@code hatch * 4 + fan}): its four lift fans turning at
 *       {@value #FAN_FPS} fps, its pod hatches opening over its gun's tell ({@link Enemy#gunTell()}) and held open
 *       through a salvo, their gold bloom over the hull ({@code -hatch-glow}, additive) and a launch flash at the wing
 *       root cells ({@code -launch}, additive, at {@code pivots/hornet.json}'s {@code cells}, alternating) as each
 *       missile leaves;
 *   <li>the Shard Drone: its prism spinning at 360°/s ({@code shard-drone}: 16 frames over its 120° symmetry), its
 *       group's air links ({@link AirLinks}): a lit link the {@code -link} strip tiled from end to end and crackling,
 *       its tell the {@code -link-tell} flicker, a link broken by a drone's death its {@code -link-snap} (additive, at the
 *       link's middle); at its death a few gold shards spinning off ({@code -shard});
 *   <li>the Buzzsaw Drone: its blades spinning with its phase (2 turns/s chasing, rising to 6 over the spin-up tell,
 *       the tell's red rim {@code -tell} alternating with the plain frames), the {@code -blur} while it dashes, a
 *       bounce's sparks ({@code -ricochet}, additive, turned to the bounce's normal) and at its death its blades flung
 *       off ({@code -blade}).
 * </ul>
 */
final class MiningRigLooks {
    static final String HORNET = "hornet";
    static final String SHARD = "shard-drone";
    static final String SAW = "buzzsaw-drone";

    /** The Hornet's frames: 4 hatch steps times 4 fan steps, facing down. */
    static final int HORNET_STEPS = 4;
    /** The Hornet's lift fans: 4 frames of a 120° loop at this rate. */
    static final double FAN_FPS = 12;
    /** A salvo's hatches stay open this long after its last launch, then close a step at a time. */
    static final int HATCH_HOLD_TICKS = SimStep.ticks(0.4);

    static final int HATCH_STEP_TICKS = 3;
    /** A missile launched within this many px of a Hornet's centre is its (it leaves from a wing root cell). */
    static final double LAUNCH_REACH = 40;
    /** The Shard Drone spins 360°/s; its 16 frames cover 120°: 3 loops a second. */
    static final double SHARD_FPS = 3 * 16;
    /** The Buzzsaw's 8 frames cover 60° of its six blades: a turn is 6 loops. */
    static final int SAW_SYMMETRY = 6;

    static final double SAW_CHASE_TURNS = 2;
    static final double SAW_SPUN_TURNS = 6;
    /** The spin-up tell's red rim alternates with the plain frames every this many steps. */
    static final int TELL_BLINK_TICKS = 3;
    /** The lit link's crackle (about 20 fps) and the tell's flicker, steps a frame. */
    static final int LINK_FRAME_TICKS = 3;

    static final int TELL_FRAME_TICKS = 4;
    /** The flashes (launch, snap, ricochet): steps a frame. */
    static final int FLASH_FRAME_TICKS = 3;
    /** The death pieces: how many, their speed (px/s), how long they fly. */
    static final int SHARDS = 4;

    static final int BLADES = 6;
    static final double PIECE_SPEED = 110;
    static final int PIECE_TICKS = SimStep.ticks(0.7);
    /** A bounce this close to a play field edge turns its sparks to that edge's normal. */
    static final double EDGE_REACH = 30;

    private static final int SLOTS = 16;
    private static final int FLASHES = 32;
    private static final int DEATHS = 16;

    private static final int NONE = 0;
    private static final int ROLE_HORNET = 1;
    private static final int ROLE_SHARD = 2;
    private static final int ROLE_SAW = 3;

    private final int[] role;
    private final Array<AtlasRegion> hatchGlow;
    private final Array<AtlasRegion> launch;
    private final Array<AtlasRegion> link;
    private final Array<AtlasRegion> linkTell;
    private final Array<AtlasRegion> linkSnap;
    private final Array<AtlasRegion> shards;
    private final Array<AtlasRegion> sawTell;
    private final Array<AtlasRegion> sawBlur;
    private final Array<AtlasRegion> ricochet;
    private final Array<AtlasRegion> blades;
    /** The Hornet's wing root cells from its centre, px, y up. */
    private final double[][] cells;

    /** Per watched Hornet: its serial, its last launch and its launches (the cell the next one leaves). */
    private final int[] serial = new int[SLOTS];

    private final long[] lastLaunch = new long[SLOTS];
    private final int[] launches = new int[SLOTS];
    private int nextSlot;
    /** The flashes: frames (null free), start step, where, on a Hornet (its serial, else -1), turn (degrees). */
    private final List<Array<AtlasRegion>> flash = new java.util.ArrayList<>(FLASHES);

    private final long[] flashTick = new long[FLASHES];
    private final double[] flashX = new double[FLASHES];
    private final double[] flashY = new double[FLASHES];
    private final int[] flashOn = new int[FLASHES];
    private final float[] flashTurn = new float[FLASHES];
    private int nextFlash;
    /** The deaths: their pieces (null free), the step, where. */
    private final List<Array<AtlasRegion>> death = new java.util.ArrayList<>(DEATHS);

    private final long[] deathTick = new long[DEATHS];
    private final double[] deathX = new double[DEATHS];
    private final double[] deathY = new double[DEATHS];
    private final int[] deathCount = new int[DEATHS];
    private int nextDeath;

    MiningRigLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        role = new int[kinds.size()];
        for (int k = 0; k < role.length; k++) {
            role[k] = switch (kinds.get(k).slug()) {
                case HORNET -> ROLE_HORNET;
                case SHARD -> ROLE_SHARD;
                case SAW -> ROLE_SAW;
                default -> NONE;
            };
        }
        hatchGlow = optional(sprites, HORNET + "-hatch-glow");
        launch = optional(sprites, HORNET + "-launch");
        link = optional(sprites, SHARD + "-link");
        linkTell = optional(sprites, SHARD + "-link-tell");
        linkSnap = optional(sprites, SHARD + "-link-snap");
        shards = optional(sprites, SHARD + "-shard");
        sawTell = optional(sprites, SAW + "-tell");
        sawBlur = optional(sprites, SAW + "-blur");
        ricochet = optional(sprites, SAW + "-ricochet");
        blades = optional(sprites, SAW + "-blade");
        cells = cells(pivots.apply(HORNET));
        for (int f = 0; f < FLASHES; f++) {
            flash.add(null);
        }
        for (int d = 0; d < DEATHS; d++) {
            death.add(null);
        }
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>();
    }

    /** The Hornet's wing root cells from its centre ([x, y] px from the frame's top left in the pivots; y up here). */
    static double[][] cells(JsonValue pivots) {
        JsonValue at = pivots == null ? null : pivots.get("cells");
        if (at == null) {
            return new double[0][];
        }
        double half = pivots.get("size").getDouble(0) / 2;
        double[][] cells = new double[at.size][2];
        for (int j = 0; j < at.size; j++) {
            cells[j][0] = at.get(j).getDouble(0) - half;
            cells[j][1] = half - at.get(j).getDouble(1);
        }
        return cells;
    }

    /** The level restarts: no clocks, flashes or deaths. */
    void restart() {
        Arrays.fill(serial, -1);
        Arrays.fill(lastLaunch, Long.MIN_VALUE / 2);
        java.util.Collections.fill(flash, null);
        java.util.Collections.fill(death, null);
        nextSlot = 0;
        nextFlash = 0;
        nextDeath = 0;
    }

    private int roleOf(int kind) {
        return kind >= 0 && kind < role.length ? role[kind] : NONE;
    }

    private int slotOf(int unit) {
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] == unit) {
                return s;
            }
        }
        return -1;
    }

    /** After each simulation step: the Hornets' launches, the links' snaps, the bounces, the deaths. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            double x = events.x(i);
            double y = events.y(i);
            switch (events.type(i)) {
                case MISSILE_LAUNCHED -> launched(sortie, x, y, tick);
                case AIR_LINK_BROKEN -> {
                    if (events.value(i) == 0) {
                        startFlash(linkSnap, tick, x, y, -1, 0);
                    }
                }
                case DASH_BOUNCE -> bounced(sortie, x, y, tick);
                case ENEMY_DESTROYED -> {
                    int kind = roleOf(events.value(i));
                    if (kind == ROLE_SHARD && shards.size > 0) {
                        startDeath(shards, SHARDS, tick, x, y);
                    } else if (kind == ROLE_SAW && blades.size > 0) {
                        startDeath(blades, BLADES, tick, x, y);
                    }
                }
                default -> {}
            }
        }
    }

    /** A homing missile left near (x, y): a Hornet's salvo holds its hatches open, the flash at its next cell. */
    private void launched(Sortie sortie, double x, double y, long tick) {
        Enemy hornet = nearest(sortie, x, y, ROLE_HORNET, LAUNCH_REACH);
        if (hornet == null) {
            return;
        }
        int s = slotOf(hornet.serial());
        if (s < 0) {
            s = nextSlot;
            nextSlot = (nextSlot + 1) % SLOTS;
            serial[s] = hornet.serial();
            launches[s] = 0;
        }
        lastLaunch[s] = tick;
        if (cells.length > 0) {
            double[] cell = cells[launches[s] % cells.length];
            startFlash(launch, tick, cell[0], cell[1], hornet.serial(), 0);
        }
        launches[s]++;
    }

    /** A dash bounced at (x, y): sparks fanning out along the edge's normal, or off a rock along the new heading. */
    private void bounced(Sortie sortie, double x, double y, long tick) {
        double nx;
        double ny;
        if (x < EDGE_REACH) {
            nx = 1;
            ny = 0;
        } else if (x > PlayField.WIDTH - EDGE_REACH) {
            nx = -1;
            ny = 0;
        } else if (y < EDGE_REACH) {
            nx = 0;
            ny = 1;
        } else if (y > PlayField.HEIGHT - EDGE_REACH) {
            nx = 0;
            ny = -1;
        } else {
            Enemy saw = nearest(sortie, x, y, ROLE_SAW, 24);
            nx = saw == null ? 0 : saw.renderX(1) - saw.renderX(0);
            ny = saw == null ? 1 : saw.renderY(1) - saw.renderY(0);
        }
        startFlash(ricochet, tick, x, y, -1, sparkTurn(nx, ny));
    }

    /** The ricochet's turn, degrees counter-clockwise: its sparks fan up in the frames, here along (nx, ny). */
    static float sparkTurn(double nx, double ny) {
        if (nx == 0 && ny == 0) {
            return 0;
        }
        return (float) (Math.toDegrees(Math.atan2(ny, nx)) - 90);
    }

    private void startFlash(Array<AtlasRegion> frames, long tick, double x, double y, int on, float turn) {
        if (frames.size == 0) {
            return;
        }
        flash.set(nextFlash, frames);
        flashTick[nextFlash] = tick;
        flashX[nextFlash] = x;
        flashY[nextFlash] = y;
        flashOn[nextFlash] = on;
        flashTurn[nextFlash] = turn;
        nextFlash = (nextFlash + 1) % FLASHES;
    }

    private void startDeath(Array<AtlasRegion> frames, int count, long tick, double x, double y) {
        death.set(nextDeath, frames);
        deathTick[nextDeath] = tick;
        deathX[nextDeath] = x;
        deathY[nextDeath] = y;
        deathCount[nextDeath] = count;
        nextDeath = (nextDeath + 1) % DEATHS;
    }

    private Enemy nearest(Sortie sortie, double x, double y, int wanted, double reach) {
        Enemy found = null;
        double best = reach;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (roleOf(enemy.kind()) != wanted) {
                continue;
            }
            double distance = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (distance <= best) {
                best = distance;
                found = enemy;
            }
        }
        return found;
    }

    private Enemy bySerial(Sortie sortie, int unit) {
        for (int e = 0; e < sortie.enemyCount(); e++) {
            if (sortie.enemy(e).serial() == unit) {
                return sortie.enemy(e);
            }
        }
        return null;
    }

    /**
     * The Hornet's hatch step (0 shut to 3 open): its gun's tell's share in steps (0, 1/3, 2/3, 1), held open {@link
     * #HATCH_HOLD_TICKS} after a salvo's last launch ({@code sinceLaunch} steps ago), then closing a step at a time.
     */
    static int hatch(double tell, long sinceLaunch) {
        int telling = (int) Math.round(Math.clamp(tell, 0, 1) * (HORNET_STEPS - 1));
        int open = 0;
        if (sinceLaunch >= 0) {
            open = sinceLaunch < HATCH_HOLD_TICKS
                    ? HORNET_STEPS - 1
                    : (int) Math.max(0, HORNET_STEPS - 1 - (sinceLaunch - HATCH_HOLD_TICKS) / HATCH_STEP_TICKS);
        }
        return Math.max(telling, open);
    }

    /** The Hornet's fan step at {@code tick}, each its own phase ({@code serial}). */
    static int fan(long tick, int serial) {
        return (int)
                Math.floorMod((long) Math.floor(tick * FAN_FPS / SimStep.PER_SECOND) + serial, (long) HORNET_STEPS);
    }

    /** The Buzzsaw's spin, turns per second: chasing, rising over its tell, spun up in its dash. */
    static double sawTurns(boolean dashing, boolean telling, double tellShare) {
        if (dashing) {
            return SAW_SPUN_TURNS;
        }
        if (telling) {
            return SAW_CHASE_TURNS + (SAW_SPUN_TURNS - SAW_CHASE_TURNS) * Math.clamp(tellShare, 0, 1);
        }
        return SAW_CHASE_TURNS;
    }

    private int hatchOf(Sortie sortie, Enemy hornet) {
        if (hornet.empStunned()) {
            return 0;
        }
        int s = slotOf(hornet.serial());
        long since = s < 0 ? -1 : sortie.tick() - lastLaunch[s];
        return hatch(hornet.gunTell(), since < 0 || lastLaunch[s] <= Long.MIN_VALUE / 2 ? -1 : since);
    }

    /** The unit's frame, or null when {@link EnemyLooks}' heading set draws it. */
    AtlasRegion frame(Sortie sortie, Enemy enemy, EnemyLooks look) {
        long tick = sortie.tick();
        Array<AtlasRegion> frames = look.frames();
        return switch (roleOf(enemy.kind())) {
            case ROLE_HORNET ->
                frames.size == HORNET_STEPS * HORNET_STEPS
                        ? frames.get(hatchOf(sortie, enemy) * HORNET_STEPS + fan(tick, enemy.serial()))
                        : null;
            case ROLE_SHARD ->
                frames.get((int) Math.floorMod(
                        (long) Math.floor(tick * SHARD_FPS / SimStep.PER_SECOND) + enemy.serial() * 5L,
                        (long) frames.size));
            case ROLE_SAW -> sawFrame(tick, enemy, frames);
            default -> null;
        };
    }

    private AtlasRegion sawFrame(long tick, Enemy saw, Array<AtlasRegion> frames) {
        if (saw.dashing() && sawBlur.size > 0) {
            return sawBlur.get((int) Math.floorMod(tick + saw.serial(), (long) sawBlur.size));
        }
        double fps = sawTurns(false, saw.dashTelling(), saw.dashTellShare()) * SAW_SYMMETRY * frames.size;
        int frame = (int) Math.floorMod(
                (long) Math.floor(tick * fps / SimStep.PER_SECOND) + saw.serial() * 3L, (long) frames.size);
        boolean red = saw.dashTelling() && sawTell.size == frames.size && tick / TELL_BLINK_TICKS % 2 == 0;
        return red ? sawTell.get(frame) : frames.get(frame);
    }

    /** Solid, under the flyers: the Shard groups' air links, lit (crackling) or in their tell (flickering). */
    void drawLinks(SpriteBatch batch, Sortie sortie) {
        AirLinks links = sortie.airLinks();
        if (links == null || link.size == 0) {
            return;
        }
        long tick = sortie.tick();
        for (int slot = 0; slot < AirLinks.SLOTS; slot++) {
            if (!links.shown(slot)) {
                continue;
            }
            AtlasRegion frame;
            if (links.lit(slot)) {
                frame = link.get((int) Math.floorMod(tick / LINK_FRAME_TICKS + slot * 3L, (long) link.size));
            } else if (linkTell.size > 0 && links.tell(slot) < 1) {
                frame = linkTell.get((int) Math.floorMod(tick / TELL_FRAME_TICKS + slot, (long) linkTell.size));
            } else {
                continue;
            }
            drawStrip(batch, frame, links.ax(slot), links.ay(slot), links.bx(slot), links.by(slot));
        }
    }

    /** The strip {@code frame} tiled from (ax, ay) to (bx, by), each tile stretched a little to fit a whole number. */
    private static void drawStrip(SpriteBatch batch, AtlasRegion frame, double ax, double ay, double bx, double by) {
        double length = Math.hypot(bx - ax, by - ay);
        if (length < 1) {
            return;
        }
        int tiles = tiles(length, frame.getRegionWidth());
        float width = (float) (length / tiles);
        float height = frame.getRegionHeight();
        float degrees = (float) Math.toDegrees(Math.atan2(by - ay, bx - ax));
        for (int j = 0; j < tiles; j++) {
            double share = (j + 0.5) / tiles;
            double cx = ax + (bx - ax) * share;
            double cy = ay + (by - ay) * share;
            batch.draw(
                    frame,
                    (float) (PlayFrame.X0 + cx - width / 2),
                    (float) (cy - height / 2),
                    width / 2,
                    height / 2,
                    width,
                    height,
                    1,
                    1,
                    degrees);
        }
    }

    /** How many tiles of {@code tile} px a link of {@code length} px takes: the nearest whole number, at least one. */
    static int tiles(double length, int tile) {
        return Math.max(1, (int) Math.round(length / Math.max(1, tile)));
    }

    /** Additive (the batch is): the Hornets' hatch bloom, the launch flashes, the snaps, the ricochets' sparks. */
    void drawGlints(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount() && hatchGlow.size > 0; i++) {
            Enemy enemy = sortie.enemy(i);
            if (roleOf(enemy.kind()) != ROLE_HORNET) {
                continue;
            }
            int hatch = hatchOf(sortie, enemy);
            if (hatch > 0) {
                drawCentred(
                        batch,
                        hatchGlow.get(Math.min(hatchGlow.size - 1, hatch)),
                        enemy.renderX(alpha),
                        enemy.renderY(alpha));
            }
        }
        for (int f = 0; f < FLASHES; f++) {
            Array<AtlasRegion> frames = flash.get(f);
            long age = tick - flashTick[f];
            if (frames == null || age < 0 || age >= (long) frames.size * FLASH_FRAME_TICKS) {
                continue;
            }
            double x = flashX[f];
            double y = flashY[f];
            if (flashOn[f] >= 0) {
                Enemy on = bySerial(sortie, flashOn[f]);
                if (on == null) {
                    continue;
                }
                x += on.renderX(alpha);
                y += on.renderY(alpha);
            }
            AtlasRegion frame = frames.get((int) (age / FLASH_FRAME_TICKS));
            if (flashTurn[f] == 0) {
                drawCentred(batch, frame, x, y);
            } else {
                float w = frame.getRegionWidth();
                float h = frame.getRegionHeight();
                batch.draw(
                        frame,
                        (float) (PlayFrame.X0 + x - w / 2),
                        (float) (y - h / 2),
                        w / 2,
                        h / 2,
                        w,
                        h,
                        1,
                        1,
                        flashTurn[f]);
            }
        }
    }

    /** Solid, with the death pieces: a Shard Drone's shards and a Buzzsaw's blades flung out, spinning, fading. */
    void drawPieces(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int d = 0; d < DEATHS; d++) {
            Array<AtlasRegion> frames = death.get(d);
            long age = tick - deathTick[d];
            if (frames == null || age < 0 || age >= PIECE_TICKS) {
                continue;
            }
            double seconds = (age + alpha) * SimStep.SECONDS;
            float fade = (float) Math.min(1, 2 * (1 - age / (double) PIECE_TICKS));
            batch.setColor(1, 1, 1, fade);
            int count = deathCount[d];
            for (int p = 0; p < count; p++) {
                double angle = 2 * Math.PI * (p + 0.3 * d) / count;
                double speed = PIECE_SPEED * (0.8 + 0.4 * ((p * 7 + d) % 5) / 4.0);
                double px = deathX[d] + Math.cos(angle) * speed * seconds;
                double py = deathY[d] + Math.sin(angle) * speed * seconds;
                int frame = (int) Math.floorMod(age / 3 + p * 3L, (long) frames.size);
                drawCentred(batch, frames.get(frame), px, py);
            }
            batch.setColor(1, 1, 1, 1);
        }
    }
}
