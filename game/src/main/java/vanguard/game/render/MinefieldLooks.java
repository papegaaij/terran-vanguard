package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import vanguard.sim.Chain;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part C: Level 30's units' looks beyond {@link EnemyLooks}' heading set (tools/art/belt_units_c.py, whose docstring
 * gives the sprites; design/enemies/air/gilded-gunship, air/rail-serpent, space/minelayer), a look only:
 *
 * <ul>
 *   <li>the Minelayer: its drum glowing while it lays ({@link MineLooks#layerFrame});
 *   <li>the Gilded Gunship (facing down only: {@code gilded-gunship} frame {@code bays * 4 + lamps}): its gold ring lamps
 *       lighting over the ring's tell before each ring ({@link EnemyGun#tellSeconds()}, from its last ring and its gun's
 *       interval as {@link CeresLooks#untilShot} predicts it), its belly doors opening for a Dart stream and closing
 *       after the last Dart ({@code LAUNCHED}), its four engines' flickering glow over the hull ({@code -engines},
 *       additive), a bay's flash as a Dart leaves it ({@code -bay-flash} at {@code pivots/gilded-gunship.json}'s {@code
 *       bays}), a shot glancing off the gold plate ({@code -plate-spark} at {@code plate}, on {@code PLATING_HIT} value
 *       {@link #PLATE_FRONT}); at its death its hull breaking in two behind the plate ({@code -halves}, drifting
 *       with the scroll in place of the vacuum wreck) and a few gold plates flung out ({@code -plates});
 *   <li>the Rail Serpent: a turret car's red lamp over its gun's tell ({@code rail-serpent-turret-tell}, from its {@code
 *       CAR_TELL}), a split's rear cars dead and scorched while they drift ({@code rail-serpent-<car>-wreck} for the
 *       members of a {@link Chain#wreck()}) with their sparks looping over them ({@code -sparks}, additive), and the
 *       coupler snapping at the cut ({@code -snap}, additive, on {@code CHAIN_WRECK}).
 * </ul>
 */
final class MinefieldLooks {
    static final String GUNSHIP = "gilded-gunship";
    static final String SERPENT = "rail-serpent";
    static final String TURRET = SERPENT + "-turret";
    /** The Gunship's frames: 4 door steps times 4 lamp steps, facing down. */
    static final int GUNSHIP_STEPS = 4;
    /** A door step takes this many steps (opening and closing). */
    static final int DOOR_STEP_TICKS = 3;
    /** The doors stay open this long after a stream's last Dart. */
    static final int DOOR_HOLD_TICKS = SimStep.ticks(0.3);
    /** A launch this long after the one before starts a new stream (its doors open again). */
    static final int STREAM_GAP_TICKS = SimStep.ticks(1);
    /** The engines' flicker, a frame this many steps. */
    static final int ENGINE_FRAME_TICKS = 4;
    /** The bay flash, the plate spark and the snap: a frame this many steps. */
    static final int FLASH_FRAME_TICKS = 3;
    /** The sparks over a wreck: 2 steps a frame (the generator's loop). */
    static final int SPARK_FRAME_TICKS = 2;
    /** The halves' break-up: a frame this many steps. */
    static final int HALVES_FRAME_TICKS = 5;
    /** The gold plates flung out at a Gunship's death, their speed (px/s) and how long they fly. */
    static final int PLATES = 4;

    static final double PLATE_SPEED = 90;
    static final int PLATE_TICKS = SimStep.ticks(1.2);
    /** A plate hit is the Gunship's within this many px of its centre. */
    static final double REACH = 64;
    /** A ring's shot or a launch is the Gunship's within this many px of its centre (both leave from it). */
    static final double FIRE_REACH = 6;
    /** {@code PLATING_HIT}'s value for the gold plate in front (the simulation's {@code ArmourArcs.FRONT}). */
    static final int PLATE_FRONT = 0;

    private static final int SLOTS = 16;
    private static final int PIECES = 32;

    private static final int NONE = 0;
    private static final int ROLE_LAYER = 1;
    private static final int ROLE_GUNSHIP = 2;
    private static final int ROLE_TURRET = 3;

    private final int[] role;
    private final Sprites sprites;

    private final Array<AtlasRegion> engines;
    private final Array<AtlasRegion> bayFlash;
    private final Array<AtlasRegion> plateSpark;
    private final Array<AtlasRegion> halves;
    private final Array<AtlasRegion> plates;
    private final Array<AtlasRegion> turretTell;
    private final Array<AtlasRegion> sparks;
    private final Array<AtlasRegion> snap;
    /** The wreck frames of a chain member's slug ({@code <slug>-wreck}), looked up once; empty without. */
    private final Map<String, Array<AtlasRegion>> wrecks = new HashMap<>();
    /** The Gunship's bays and plate from its centre, px, y up. */
    private final double[][] bays;

    private final double[] plate;

    /** Per watched Gunship: its serial, when it came on, its last ring, its stream's first and last launch, its launches. */
    private final int[] serial = new int[SLOTS];

    private final long[] seen = new long[SLOTS];
    private final long[] lastRing = new long[SLOTS];
    private final long[] streamStart = new long[SLOTS];
    private final long[] lastLaunch = new long[SLOTS];
    private final int[] launches = new int[SLOTS];
    private int nextSlot;
    /** Per turret car (by serial) the step of its last tell. */
    private final int[] tellSerial = new int[SLOTS * 2];

    private final long[] tellTick = new long[SLOTS * 2];
    private int nextTell;
    /** The flashes: frames (null free), start step, where, whether on a moving unit (its serial, else -1) and offset. */
    private final List<Array<AtlasRegion>> flash = new java.util.ArrayList<>(PIECES);

    private final long[] flashTick = new long[PIECES];
    private final double[] flashX = new double[PIECES];
    private final double[] flashY = new double[PIECES];
    private final int[] flashOn = new int[PIECES];
    private int nextFlash;
    /** The Gunships' deaths: the step, where (y with the ground scroll then). */
    private final long[] deathTick = new long[SLOTS];

    private final double[] deathX = new double[SLOTS];
    private final double[] deathY = new double[SLOTS];
    private int nextDeath;

    MinefieldLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        this.sprites = sprites;
        role = new int[kinds.size()];
        for (int k = 0; k < role.length; k++) {
            String slug = kinds.get(k).slug();
            role[k] = switch (slug) {
                case MineLooks.LAYER -> ROLE_LAYER;
                case GUNSHIP -> sprites.has(GUNSHIP) ? ROLE_GUNSHIP : NONE;
                case TURRET -> ROLE_TURRET;
                default -> NONE;
            };
        }
        engines = optional(GUNSHIP + "-engines");
        bayFlash = optional(GUNSHIP + "-bay-flash");
        plateSpark = optional(GUNSHIP + "-plate-spark");
        halves = optional(GUNSHIP + "-halves");
        plates = optional(GUNSHIP + "-plates");
        turretTell = optional(TURRET + "-tell");
        sparks = optional(SERPENT + "-sparks");
        snap = optional(SERPENT + "-snap");
        JsonValue gunship = pivots.apply(GUNSHIP);
        bays = bays(gunship);
        plate = point(gunship, "plate");
        for (int f = 0; f < PIECES; f++) {
            flash.add(null);
        }
        restart();
    }

    private Array<AtlasRegion> optional(String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>();
    }

    /** The Gunship's 4 bays from its centre ({@code bays[j]}, [x, y] px from the frame's top left; y up here). */
    static double[][] bays(JsonValue pivots) {
        JsonValue bays = pivots == null ? null : pivots.get("bays");
        if (bays == null) {
            return new double[0][];
        }
        double half = pivots.get("size").getDouble(0) / 2;
        double[][] at = new double[bays.size][2];
        for (int j = 0; j < bays.size; j++) {
            at[j][0] = bays.get(j).getDouble(0) - half;
            at[j][1] = half - bays.get(j).getDouble(1);
        }
        return at;
    }

    /** A point of the Gunship's pivots from its centre (y up), or its centre without. */
    static double[] point(JsonValue pivots, String name) {
        JsonValue at = pivots == null ? null : pivots.get(name);
        if (at == null) {
            return new double[2];
        }
        double half = pivots.get("size").getDouble(0) / 2;
        return new double[] {at.getDouble(0) - half, half - at.getDouble(1)};
    }

    /** The level restarts: no clocks, flashes or deaths. */
    void restart() {
        Arrays.fill(serial, -1);
        Arrays.fill(tellSerial, -1);
        Arrays.fill(deathTick, Long.MIN_VALUE / 2);
        java.util.Collections.fill(flash, null);
        nextSlot = 0;
        nextTell = 0;
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

    /** After each simulation step: the Gunships' rings, launches and plate hits, the turret cars' tells, the cuts. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (roleOf(enemy.kind()) == ROLE_GUNSHIP && slotOf(enemy.serial()) < 0) {
                int s = nextSlot;
                nextSlot = (nextSlot + 1) % SLOTS;
                serial[s] = enemy.serial();
                seen[s] = tick;
                lastRing[s] = Long.MIN_VALUE / 2;
                streamStart[s] = Long.MIN_VALUE / 2;
                lastLaunch[s] = Long.MIN_VALUE / 2;
                launches[s] = 0;
            }
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            double x = events.x(i);
            double y = events.y(i);
            switch (events.type(i)) {
                case ENEMY_FIRED -> {
                    Enemy gunship = nearest(sortie, x, y, ROLE_GUNSHIP, FIRE_REACH);
                    if (gunship != null) {
                        lastRing[slotOf(gunship.serial())] = tick;
                    }
                }
                case LAUNCHED -> launched(sortie, x, y, tick);
                case PLATING_HIT -> {
                    Enemy gunship = nearest(sortie, x, y, ROLE_GUNSHIP, REACH);
                    if (gunship != null && events.value(i) == PLATE_FRONT) {
                        startFlash(plateSpark, tick, plate[0], plate[1], gunship.serial());
                    }
                }
                case CAR_TELL -> {
                    Enemy car = nearest(sortie, x, y, ROLE_TURRET, 8);
                    if (car != null) {
                        tellSerial[nextTell] = car.serial();
                        tellTick[nextTell] = tick;
                        nextTell = (nextTell + 1) % tellSerial.length;
                    }
                }
                case CHAIN_WRECK -> startFlash(snap, tick, x, y, -1);
                case ENEMY_DESTROYED -> {
                    if (roleOf(events.value(i)) == ROLE_GUNSHIP && halves.size > 0) {
                        deathTick[nextDeath] = tick;
                        deathX[nextDeath] = x;
                        deathY[nextDeath] = y + sortie.groundScroll();
                        nextDeath = (nextDeath + 1) % SLOTS;
                    }
                }
                default -> {}
            }
        }
    }

    /** A Dart left a Gunship near (x, y): its stream's doors, the flash at the next bay. */
    private void launched(Sortie sortie, double x, double y, long tick) {
        Enemy gunship = nearest(sortie, x, y, ROLE_GUNSHIP, FIRE_REACH);
        if (gunship == null) {
            return;
        }
        int s = slotOf(gunship.serial());
        if (tick - lastLaunch[s] > STREAM_GAP_TICKS) {
            streamStart[s] = tick;
        }
        lastLaunch[s] = tick;
        if (bays.length > 0) {
            double[] bay = bays[launches[s] % bays.length];
            startFlash(bayFlash, tick, bay[0], bay[1], gunship.serial());
        }
        launches[s]++;
    }

    private void startFlash(Array<AtlasRegion> frames, long tick, double x, double y, int on) {
        if (frames.size == 0) {
            return;
        }
        flash.set(nextFlash, frames);
        flashTick[nextFlash] = tick;
        flashX[nextFlash] = x;
        flashY[nextFlash] = y;
        flashOn[nextFlash] = on;
        nextFlash = (nextFlash + 1) % PIECES;
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
     * The Gunship's lamp step (0 dark to 3 lit) {@code until} steps before its next ring with a tell of {@code tell} s:
     * lighting one by one over the tell.
     */
    static int lamps(long until, double tell) {
        double share = CeresLooks.tellShare(until, tell);
        return share < 0 ? 0 : Math.min(GUNSHIP_STEPS - 1, 1 + (int) (share * (GUNSHIP_STEPS - 1)));
    }

    /**
     * The Gunship's door step (0 shut to 3 open) {@code sinceStart} steps after its stream's first Dart and {@code
     * sinceLast} after its last: opening a step at a time, held open, then closing.
     */
    static int doors(long sinceStart, long sinceLast) {
        if (sinceStart < 0 || sinceLast < 0) {
            return 0;
        }
        long opening = 1 + sinceStart / DOOR_STEP_TICKS;
        long closing = sinceLast < DOOR_HOLD_TICKS
                ? GUNSHIP_STEPS - 1
                : GUNSHIP_STEPS - 1 - (sinceLast - DOOR_HOLD_TICKS) / DOOR_STEP_TICKS;
        return Math.clamp(Math.min(opening, closing), 0, GUNSHIP_STEPS - 1);
    }

    /** The unit's frame, or null when {@link EnemyLooks}' heading set draws it. */
    AtlasRegion frame(Sortie sortie, Enemy enemy, EnemyLooks look, MineLooks mines) {
        return switch (roleOf(enemy.kind())) {
            case ROLE_LAYER -> mines.layerFrame(enemy, look.headings());
            case ROLE_GUNSHIP -> gunshipFrame(sortie, enemy, look);
            default -> null;
        };
    }

    private AtlasRegion gunshipFrame(Sortie sortie, Enemy enemy, EnemyLooks look) {
        if (look.frames().size != GUNSHIP_STEPS * GUNSHIP_STEPS) {
            return null;
        }
        int s = slotOf(enemy.serial());
        long tick = sortie.tick();
        int lamp = 0;
        int door = 0;
        EnemyGun gun = enemy.spec().gun().orElse(null);
        if (s >= 0 && gun != null && !enemy.empStunned()) {
            long due = lastRing[s] > Long.MIN_VALUE / 2
                    ? lastRing[s] + SimStep.ticks(gun.intervalSeconds())
                    : seen[s] + SimStep.ticks(gun.firstShotDelay());
            lamp = lamps(due - tick, gun.tellSeconds());
        }
        if (s >= 0) {
            door = doors(tick - streamStart[s], tick - lastLaunch[s]);
        }
        return look.frames().get(door * GUNSHIP_STEPS + lamp);
    }

    /** A chain member's own frame: a turret car's lamp over its tell, a split's dead car; null for the heading set's. */
    AtlasRegion memberFrame(Sortie sortie, Chain chain, Enemy member) {
        String slug = member.spec().slug();
        if (!slug.startsWith(SERPENT + "-")) {
            return null;
        }
        if (chain.wreck()) {
            Array<AtlasRegion> wreck =
                    wrecks.computeIfAbsent(slug, s -> sprites.has(s + "-wreck") ? sprites.frames(s + "-wreck") : null);
            return wreck == null ? null : wreck.get(EnemyLooks.heading(member.facing(), wreck.size));
        }
        if (roleOf(member.kind()) == ROLE_TURRET && turretTell.size > 0 && telling(sortie, member)) {
            return turretTell.get(EnemyLooks.heading(member.facing(), turretTell.size));
        }
        return null;
    }

    private boolean telling(Sortie sortie, Enemy car) {
        EnemyGun gun = car.spec().gun().orElse(null);
        int tell = gun == null ? 0 : Math.max(1, SimStep.ticks(gun.tellSeconds()));
        for (int t = 0; t < tellSerial.length; t++) {
            if (tellSerial[t] == car.serial() && sortie.tick() - tellTick[t] < tell) {
                return true;
            }
        }
        return false;
    }

    /** Whether the unit of {@code kind} breaks up in its own pieces at its death (no vacuum wreck): the Gunship. */
    boolean breaksUp(int kind) {
        return roleOf(kind) == ROLE_GUNSHIP && halves.size > 0;
    }

    /**
     * Additive (the batch is): the Gunships' engines, their bays' flashes and plate sparks; the sparks over a split
     * train's dead cars and the snaps at its cuts.
     */
    void drawGlints(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount() && engines.size > 0; i++) {
            Enemy enemy = sortie.enemy(i);
            if (roleOf(enemy.kind()) != ROLE_GUNSHIP || enemy.empStunned()) {
                continue;
            }
            int frame = (int) Math.floorMod(tick / ENGINE_FRAME_TICKS + enemy.serial(), (long) engines.size);
            drawCentred(batch, engines.get(frame), enemy.renderX(alpha), enemy.renderY(alpha));
        }
        for (int c = 0; c < sortie.chainCount() && sparks.size > 0; c++) {
            Chain chain = sortie.chain(c);
            if (!chain.wreck()) {
                continue;
            }
            for (int k = 0; k < chain.size(); k++) {
                Enemy member = chain.member(k);
                if (member == null) {
                    continue;
                }
                int frame = (int) Math.floorMod(tick / SPARK_FRAME_TICKS + k * 3L, (long) sparks.size);
                drawCentred(batch, sparks.get(frame), member.renderX(alpha), member.renderY(alpha));
            }
        }
        for (int f = 0; f < PIECES; f++) {
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
            drawCentred(batch, frames.get((int) (age / FLASH_FRAME_TICKS)), x, y);
        }
    }

    /** Solid, with the death pieces: a dead Gunship's halves drifting with the scroll, its gold plates flung out. */
    void drawPieces(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        long tick = sortie.tick();
        for (int d = 0; d < SLOTS; d++) {
            long age = tick - deathTick[d];
            long halvesTicks = (long) halves.size * HALVES_FRAME_TICKS;
            if (age < 0 || age >= Math.max(halvesTicks, PLATE_TICKS)) {
                continue;
            }
            double y = deathY[d] - scroll;
            if (age < halvesTicks) {
                drawCentred(batch, halves.get((int) (age / HALVES_FRAME_TICKS)), deathX[d], y);
            }
            if (plates.size == 0 || age >= PLATE_TICKS) {
                continue;
            }
            double seconds = (age + alpha) * SimStep.SECONDS;
            float fade = (float) Math.min(1, 2 * (1 - age / (double) PLATE_TICKS));
            batch.setColor(1, 1, 1, fade);
            for (int p = 0; p < PLATES; p++) {
                // Flung out forward and to the sides of the plate, each its own way and turn.
                double angle = Math.PI * (1.15 + 0.7 * p / (PLATES - 1.0)) + d * 0.3;
                double px = deathX[d] + plate[0] + Math.cos(angle) * PLATE_SPEED * seconds;
                double py = y + plate[1] + Math.sin(angle) * PLATE_SPEED * seconds;
                int frame = (int) Math.floorMod(age / 3 + p * 2L, (long) plates.size);
                drawCentred(batch, plates.get(frame), px, py);
            }
            batch.setColor(1, 1, 1, 1);
        }
    }
}
