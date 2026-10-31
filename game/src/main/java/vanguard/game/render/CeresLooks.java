package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part B: Level 29's units' looks beyond {@link EnemyLooks}' heading set (tools/art/belt_units.py, whose docstring
 * gives the sprites; design/enemies/air/talon, ground/sam-nest, space/asteroid-mite, ground/clamp-gun, design/campaign
 * Level 29's wavering Stormhawks), a look only:
 *
 * <ul>
 *   <li>the Talon: a wave's leader in its gold-striped frames ({@code talon-leader}), the red glint on its twin muzzles
 *       over its gun's {@code first_shot_delay} before each pair ({@code talon-glint}, on {@code pivots/talon.json}'s
 *       {@code muzzles}), and at its death a wing spinning off over the burst ({@code talon-wing});
 *   <li>the SAM Nest: its fixed round base under the rack ({@code sam-nest-base}, white-lit {@code -base-dormant}
 *       while dormant), the folded rack while dormant ({@code sam-nest-dormant}), its 1 s wake ({@code sam-nest-wake}),
 *       the open hatch over the salvo's lock tell ({@code sam-nest-open}), its wreck ({@link EnemyLooks}' remains);
 *   <li>the Asteroid Mite: its crawl on a rock (4 frames at 8 fps), the crouch of its tell, the leap ({@code heading *
 *       6 + pose});
 *   <li>the wavering Stormhawk: its bank ({@code heading * 5 + bank}), banked into its peel-off, its white IFF light
 *       blinking at 1 Hz ({@code -iff}) and red for a moment when hit ({@code -hit});
 *   <li>the clamp gun: its docking clamp under it, jaw toward the tender ({@code clamp-gun-clamp-left|right}), the
 *       0.3 s red glint on its muzzle before each shot ({@code clamp-gun-glint}, {@code pivots/clamp-gun.json}), and at
 *       its death the clamp springing open ({@code -open}, the last frame left on the ground) with its burst of gas at
 *       the jaw ({@code clamp-gun-gas}, additive);
 *   <li>the belt's small rocks: a rock shot apart ({@code debris-<chunk>-break});
 *   <li>M10 part B, step B4: the Chimera (the Talon's airframe and gun): the Talon's red glint on its twin muzzles before
 *       each pair, on {@code pivots/chimera.json}'s {@code muzzles} (no leader frames, no wing: its airframe tears,
 *       {@link CloudDiverLooks}); over Level 36's foreground banks the glints are drawn again at the banks' cover.
 * </ul>
 *
 * <p>The simulation has no read-out of a gun's time to its next shot: the glints and the hatch are timed from the
 * unit's last shot (its {@code ENEMY_FIRED} or {@code MISSILE_LAUNCHED}) and its gun's interval, the first from when it
 * came into view (or woke): {@link #untilShot}. A gun that holds fire (a Talon past its fire band, a nest with the ship
 * close) shows none.
 */
final class CeresLooks {
    static final String TALON = "talon";
    static final String NEST = "sam-nest";
    static final String MITE = "asteroid-mite";
    static final String HAWK = "wavering-stormhawk";
    static final String CLAMP = "clamp-gun";
    static final String CHIMERA = CloudDiverLooks.CHIMERA;

    /** The art's headings: heading k turns k x 22.5 degrees clockwise from straight down. */
    static final int HEADINGS = 16;
    /** The mite's poses per heading: 0-3 the crawl loop, 4 the crouch, 5 the leap. */
    static final int MITE_POSES = 6;

    static final int MITE_CROUCH = 4;
    static final int MITE_LEAP = 5;
    /** The mite's crawl loop, frames per second. */
    static final double CRAWL_FPS = 8;
    /** The Stormhawk's banks per heading: -30, -15, 0, +15, +30 degrees of roll. */
    static final int BANKS = 5;

    static final int LEVEL_BANK = 2;
    /** The IFF light's blink: on for half of each second. */
    static final int BLINK_TICKS = SimStep.PER_SECOND / 2;
    /** A hit shows the red IFF set this long, steps. */
    static final int HIT_TICKS = SimStep.ticks(0.25);
    /** The clamp's opening: 3 steps a frame. */
    static final int OPEN_FRAME_TICKS = 3;
    /** The gas burst at the jaw: 3 steps a frame. */
    static final int GAS_FRAME_TICKS = 3;
    /** The wing's spin: 3 steps a frame; it flies 0.8 s, fading over the last half. */
    static final int WING_FRAME_TICKS = 3;

    static final int WING_TICKS = SimStep.ticks(0.8);
    /** The wing's flight off the burst, px/s across and up. */
    static final double WING_SPEED_X = 70;

    static final double WING_SPEED_Y = 40;
    /** A rock's break: 2 steps a frame. */
    static final int BREAK_FRAME_TICKS = 2;
    /** An event lies on its unit within this, px (a fire event carries the unit's place; a launch its rack's). */
    static final double REACH = 6;

    static final double LAUNCH_REACH = 24;

    private static final int SLOTS = 32;
    private static final int PIECES = 16;

    private final int[] role;

    private static final int NONE = 0;
    private static final int ROLE_TALON = 1;
    private static final int ROLE_NEST = 2;
    private static final int ROLE_MITE = 3;
    private static final int ROLE_HAWK = 4;
    private static final int ROLE_CLAMP = 5;
    private static final int ROLE_CHIMERA = 6;

    private final Array<AtlasRegion> leader;
    private final Array<AtlasRegion> talonGlint;
    private final Array<AtlasRegion> wing;
    /** Per heading the twin muzzles' centres, px from the sprite's centre, y up: {@code [heading][muzzle][x|y]}. */
    private final double[][][] muzzles;
    /** M10 part B: the Chimera's twin muzzles per heading, as {@link #muzzles}. */
    private final double[][][] chimeraMuzzles;

    private final AtlasRegion nestBase;
    private final AtlasRegion nestBaseDormant;
    private final AtlasRegion nestDormant;
    private final Array<AtlasRegion> nestWake;
    private final Array<AtlasRegion> nestOpen;

    private final Array<AtlasRegion> hawkIff;
    private final Array<AtlasRegion> hawkHit;

    private final AtlasRegion[] clamp = new AtlasRegion[2];
    private final List<Array<AtlasRegion>> clampOpen;
    private final Array<AtlasRegion> gas;
    private final Array<AtlasRegion> clampGlint;
    /** Per heading the clamp gun's muzzle, px from its centre, y up. */
    private final double[][] clampMuzzle;
    /** Per side (0 left, 1 right) the jaw's centre, px from the clamp's centre, y up. */
    private final double[][] jaw;

    private final Sprites sprites;

    /** The guns' clocks: per unit (serial, -1 free) the step of its last shot and the step it came into view. */
    private final int[] gunSerial = new int[SLOTS];

    private final long[] lastShot = new long[SLOTS];
    private final long[] seen = new long[SLOTS];
    private int nextGun;

    /** The wings flying off (start step, place, direction) and the opened clamps (start step, place on the ground). */
    private final long[] wingTick = new long[PIECES];

    private final double[] wingX = new double[PIECES];
    private final double[] wingY = new double[PIECES];
    private final int[] wingSide = new int[PIECES];
    private int nextWing;

    private final long[] clampTick = new long[PIECES];
    private final double[] clampX = new double[PIECES];
    /** The opened clamp's place on the ground: its screen y plus the ground scroll at its death. */
    private final double[] clampGroundY = new double[PIECES];

    private final int[] clampSide = new int[PIECES];
    private int nextClamp;

    /** The rocks of the last step (serial, place, sprite) and the rocks shot apart (start step, place, frames). */
    private final int[] rockSerial = new int[SLOTS];

    private final double[] rockX = new double[SLOTS];
    private final double[] rockY = new double[SLOTS];
    private final String[] rockSprite = new String[SLOTS];
    private int rocks;

    private final long[] breakTick = new long[PIECES];
    private final double[] breakX = new double[PIECES];
    private final double[] breakY = new double[PIECES];

    private final List<Array<AtlasRegion>> breakFrames = new java.util.ArrayList<>(PIECES);

    private int nextBreak;

    CeresLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        this.sprites = sprites;
        role = new int[kinds.size()];
        for (int k = 0; k < role.length; k++) {
            String slug = kinds.get(k).slug();
            if (!sprites.has(slug)) {
                continue;
            }
            role[k] = switch (slug) {
                case TALON -> ROLE_TALON;
                case NEST -> ROLE_NEST;
                case MITE -> ROLE_MITE;
                case HAWK -> ROLE_HAWK;
                case CLAMP -> ROLE_CLAMP;
                case CHIMERA -> ROLE_CHIMERA;
                default -> NONE;
            };
        }
        Array<AtlasRegion> none = new Array<>();
        leader = optional(TALON + "-leader", none);
        talonGlint = optional(TALON + "-glint", none);
        wing = optional(TALON + "-wing", none);
        muzzles = muzzles(pivots.apply(TALON));
        chimeraMuzzles = role.length > 0 && hasRole(ROLE_CHIMERA) ? muzzles(pivots.apply(CHIMERA)) : muzzles;
        nestBase = single(NEST + "-base");
        nestBaseDormant = single(NEST + "-base-dormant");
        nestDormant = single(NEST + "-dormant");
        nestWake = optional(NEST + "-wake", none);
        nestOpen = optional(NEST + "-open", none);
        hawkIff = optional(HAWK + "-iff", none);
        hawkHit = optional(HAWK + "-hit", none);
        clamp[0] = single(CLAMP + "-clamp-left");
        clamp[1] = single(CLAMP + "-clamp-right");
        clampOpen = List.of(optional(CLAMP + "-clamp-left-open", none), optional(CLAMP + "-clamp-right-open", none));
        for (int b = 0; b < PIECES; b++) {
            breakFrames.add(null);
        }
        gas = optional(CLAMP + "-gas", none);
        clampGlint = optional(CLAMP + "-glint", none);
        JsonValue clampPivots = pivots.apply(CLAMP);
        clampMuzzle = clampMuzzle(clampPivots);
        jaw = jaw(clampPivots);
        restart();
    }

    private Array<AtlasRegion> optional(String name, Array<AtlasRegion> none) {
        return sprites.has(name) ? sprites.frames(name) : none;
    }

    private AtlasRegion single(String name) {
        return sprites.has(name) ? sprites.region(name) : null;
    }

    /** The Talon's twin muzzles per heading from {@code muzzles[k]} ([x, y] px from the frame's top left). */
    static double[][][] muzzles(JsonValue pivots) {
        double[][][] at = new double[HEADINGS][2][2];
        JsonValue muzzles = pivots == null ? null : pivots.get("muzzles");
        if (muzzles == null) {
            return at;
        }
        double half = pivots.get("size").getDouble(0) / 2;
        for (int h = 0; h < HEADINGS && h < muzzles.size; h++) {
            for (int m = 0; m < 2; m++) {
                at[h][m][0] = muzzles.get(h).get(m).getDouble(0) - half;
                at[h][m][1] = half - muzzles.get(h).get(m).getDouble(1);
            }
        }
        return at;
    }

    /** The clamp gun's muzzle per heading from {@code muzzle[k]} ([x, y] px from the gun frame's top left). */
    static double[][] clampMuzzle(JsonValue pivots) {
        double[][] at = new double[HEADINGS][2];
        JsonValue muzzle = pivots == null ? null : pivots.get("muzzle");
        if (muzzle == null) {
            return at;
        }
        double half = pivots.get("size").getDouble(0) / 2;
        for (int h = 0; h < HEADINGS && h < muzzle.size; h++) {
            at[h][0] = muzzle.get(h).getDouble(0) - half;
            at[h][1] = half - muzzle.get(h).getDouble(1);
        }
        return at;
    }

    /** The jaw's centre per side from {@code jaw.left|right} ([x, y] px from the clamp's top left). */
    static double[][] jaw(JsonValue pivots) {
        double[][] at = new double[2][2];
        JsonValue jaw = pivots == null ? null : pivots.get("jaw");
        JsonValue size = pivots == null ? null : pivots.get("clamp_size");
        if (jaw == null || size == null) {
            return at;
        }
        String[] sides = {"left", "right"};
        for (int s = 0; s < 2; s++) {
            at[s][0] = jaw.get(sides[s]).getDouble(0) - size.getDouble(0) / 2;
            at[s][1] = size.getDouble(1) / 2 - jaw.get(sides[s]).getDouble(1);
        }
        return at;
    }

    private boolean hasRole(int wanted) {
        for (int r : role) {
            if (r == wanted) {
                return true;
            }
        }
        return false;
    }

    /** Whether any of the level's kinds has these looks. */
    boolean any() {
        for (int r : role) {
            if (r != NONE) {
                return true;
            }
        }
        return false;
    }

    private int roleOf(int kind) {
        return kind >= 0 && kind < role.length ? role[kind] : NONE;
    }

    /** The level restarts: no clocks, pieces, opened clamps or breaks. */
    void restart() {
        Arrays.fill(gunSerial, -1);
        Arrays.fill(wingTick, Long.MIN_VALUE / 2);
        Arrays.fill(clampTick, Long.MIN_VALUE / 2);
        Arrays.fill(breakTick, Long.MIN_VALUE / 2);
        java.util.Collections.fill(breakFrames, null);
        nextGun = 0;
        nextWing = 0;
        nextClamp = 0;
        nextBreak = 0;
        rocks = 0;
    }

    /** The clamp's side: 0 on the left of the field's middle (its jaw points right, at the tender), 1 on the right. */
    static int side(double x) {
        return x < PlayField.WIDTH / 2.0 ? 0 : 1;
    }

    /** After each simulation step: the guns' shots, the deaths' pieces, the rocks shot apart. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int r = roleOf(enemy.kind());
            if ((r == ROLE_TALON || r == ROLE_CHIMERA || r == ROLE_CLAMP || r == ROLE_NEST)
                    && !enemy.dormant()
                    && !enemy.waking()) {
                if (slotOf(enemy.serial()) < 0) {
                    int slot = nextGun;
                    nextGun = (nextGun + 1) % SLOTS;
                    gunSerial[slot] = enemy.serial();
                    seen[slot] = tick;
                    lastShot[slot] = Long.MIN_VALUE / 2;
                }
            }
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            double x = events.x(i);
            double y = events.y(i);
            switch (events.type(i)) {
                case ENEMY_FIRED -> shot(sortie, x, y, REACH, tick);
                case MISSILE_LAUNCHED -> shot(sortie, x, y, LAUNCH_REACH, tick);
                case ENEMY_DESTROYED -> {
                    int r = roleOf(events.value(i));
                    if (r == ROLE_TALON && wing.size > 0) {
                        wingTick[nextWing] = tick;
                        wingX[nextWing] = x;
                        wingY[nextWing] = y;
                        wingSide[nextWing] = (int) (Math.floorMod(tick + Math.round(x), 2L)) * 2 - 1;
                        nextWing = (nextWing + 1) % PIECES;
                    } else if (r == ROLE_CLAMP) {
                        clampTick[nextClamp] = tick;
                        clampX[nextClamp] = x;
                        clampGroundY[nextClamp] = y + sortie.groundScroll();
                        clampSide[nextClamp] = side(x);
                        nextClamp = (nextClamp + 1) % PIECES;
                    }
                }
                case DEBRIS_DESTROYED -> rockBroken(sortie, x, y, tick);
                default -> {}
            }
        }
        rocks = 0;
        for (int d = 0; d < sortie.debrisCount() && rocks < SLOTS; d++) {
            Debris chunk = sortie.debris(d);
            if (chunk.thrown() || chunk.pulled() || chunk.large()) {
                continue;
            }
            rockSerial[rocks] = chunk.serial();
            rockX[rocks] = chunk.renderX(1);
            rockY[rocks] = chunk.renderY(1);
            rockSprite[rocks] = chunk.sprite();
            rocks++;
        }
    }

    /** A shot or a launch at (x, y): the step of the last shot of the gun it came from, if it is one of ours. */
    private void shot(Sortie sortie, double x, double y, double reach, long tick) {
        double best = reach;
        int found = -1;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int r = roleOf(enemy.kind());
            if (r != ROLE_TALON && r != ROLE_CHIMERA && r != ROLE_CLAMP && r != ROLE_NEST) {
                continue;
            }
            double distance = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (distance <= best) {
                best = distance;
                found = enemy.serial();
            }
        }
        int slot = found < 0 ? -1 : slotOf(found);
        if (slot >= 0) {
            // A burst's (or a salvo's) shots after its first keep the burst's start.
            EnemyGun gun = gunOf(sortie, found);
            double interval = gun == null ? 1 : gun.intervalSeconds();
            if (tick - lastShot[slot] > SimStep.ticks(interval / 2)) {
                lastShot[slot] = tick;
            }
        }
    }

    private static EnemyGun gunOf(Sortie sortie, int serial) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.serial() == serial) {
                return enemy.spec().gun().orElse(null);
            }
        }
        return null;
    }

    /** A rock broke at (x, y): the one of the last step nearest it, if it has its break frames. */
    private void rockBroken(Sortie sortie, double x, double y, long tick) {
        int nearest = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int r = 0; r < rocks; r++) {
            if (alive(sortie, rockSerial[r])) {
                continue;
            }
            double distance = Math.hypot(rockX[r] - x, rockY[r] - y);
            if (distance < best) {
                best = distance;
                nearest = r;
            }
        }
        if (nearest < 0 || !sprites.has(rockSprite[nearest] + "-break")) {
            return;
        }
        breakTick[nextBreak] = tick;
        breakX[nextBreak] = rockX[nearest];
        breakY[nextBreak] = rockY[nearest];
        breakFrames.set(nextBreak, sprites.frames(rockSprite[nearest] + "-break"));
        nextBreak = (nextBreak + 1) % PIECES;
    }

    private static boolean alive(Sortie sortie, int serial) {
        for (int d = 0; d < sortie.debrisCount(); d++) {
            if (sortie.debris(d).serial() == serial) {
                return true;
            }
        }
        return false;
    }

    private int slotOf(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (gunSerial[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /**
     * Steps until the unit's next shot as its gun's clock predicts: its interval after its last shot, or its first-shot
     * delay after it came into view; negative when overdue (it held fire), {@link Long#MAX_VALUE} without a clock.
     */
    long untilShot(Enemy enemy, long tick) {
        int slot = slotOf(enemy.serial());
        EnemyGun gun = enemy.spec().gun().orElse(null);
        if (slot < 0 || gun == null) {
            return Long.MAX_VALUE;
        }
        long due = lastShot[slot] > Long.MIN_VALUE / 2
                ? lastShot[slot] + SimStep.ticks(gun.intervalSeconds())
                : seen[slot] + SimStep.ticks(gun.firstShotDelay());
        return due - tick;
    }

    /** The share (0 to 1) of a tell of {@code tellSeconds} before the predicted shot; -1 outside it. */
    static double tellShare(long until, double tellSeconds) {
        int tell = SimStep.ticks(tellSeconds);
        if (until <= 0 || until > tell || tell <= 0) {
            return -1;
        }
        return 1 - until / (double) tell;
    }

    /** The unit's frame, or null when {@link EnemyLooks}' heading set draws it. */
    AtlasRegion frame(Sortie sortie, Enemy enemy, EnemyLooks look) {
        int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
        long tick = sortie.tick();
        return switch (roleOf(enemy.kind())) {
            case ROLE_TALON -> enemy.leader() && leader.size == HEADINGS ? leader.get(heading) : null;
            case ROLE_NEST -> nestFrame(sortie, enemy, look, heading, tick);
            case ROLE_MITE -> {
                if (look.frames().size != HEADINGS * MITE_POSES) {
                    yield null;
                }
                yield look.frames().get(heading * MITE_POSES + mitePose(enemy, tick));
            }
            case ROLE_HAWK -> {
                if (look.frames().size != HEADINGS * BANKS) {
                    yield null;
                }
                int index = heading * BANKS + bank(enemy);
                Array<AtlasRegion> set = enemy.ticksSinceHit() < HIT_TICKS && hawkHit.size == look.frames().size
                        ? hawkHit
                        : iffLit(tick) && hawkIff.size == look.frames().size ? hawkIff : look.frames();
                yield set.get(index);
            }
            default -> null;
        };
    }

    private AtlasRegion nestFrame(Sortie sortie, Enemy enemy, EnemyLooks look, int heading, long tick) {
        if (enemy.dormant() && nestDormant != null) {
            return nestDormant;
        }
        if (enemy.waking() && nestWake.size > 0) {
            return nestWake.get(Math.min(nestWake.size - 1, (int) (enemy.wakeShare() * nestWake.size)));
        }
        EnemyGun gun = enemy.spec().gun().orElse(null);
        if (gun != null && gun.homing().isPresent() && nestOpen.size == HEADINGS) {
            double hatch = gun.homing().get().hatchSeconds();
            double share = tellShare(untilShot(enemy, tick), hatch);
            boolean close = Math.hypot(
                            sortie.ship().renderX(1) - enemy.renderX(1),
                            sortie.ship().renderY(1) - enemy.renderY(1))
                    < gun.homing().get().holdWithin();
            if (share >= 0 && !close) {
                return nestOpen.get(heading);
            }
        }
        return look.frames().size == HEADINGS ? look.frames().get(heading) : null;
    }

    /** The mite's pose: the crouch of its tell, the leap off the rock, else the crawl loop. */
    static int mitePose(Enemy enemy, long tick) {
        if (enemy.riderTelling()) {
            return MITE_CROUCH;
        }
        if (!enemy.riding() && !enemy.riderTumbling()) {
            return MITE_LEAP;
        }
        return (int) Math.floorMod((long) (tick * CRAWL_FPS / SimStep.PER_SECOND) + enemy.serial(), 4L);
    }

    /** The Stormhawk's bank: level, or rolled into its peel-off toward the nearer side edge. */
    static int bank(Enemy enemy) {
        if (!enemy.peeling()) {
            return LEVEL_BANK;
        }
        return enemy.renderX(1) < PlayField.WIDTH / 2.0 ? 0 : BANKS - 1;
    }

    /** Whether the IFF light is lit: the white blink, on for the first half of each second. */
    static boolean iffLit(long tick) {
        return Math.floorMod(tick, 2L * BLINK_TICKS) < BLINK_TICKS;
    }

    /**
     * On the ground under the ground units: the SAM Nests' bases, the live clamp guns' clamps and the opened clamps
     * left by the dead ones.
     */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        long tick = sortie.tick();
        for (int c = 0; c < PIECES; c++) {
            int side = clampSide[c];
            Array<AtlasRegion> open = clampOpen.get(side);
            if (clampTick[c] < 0 || open.size == 0) {
                continue;
            }
            int frame = (int) Math.min(open.size - 1, (tick - clampTick[c]) / OPEN_FRAME_TICKS);
            drawCentred(batch, open.get(Math.max(0, frame)), clampX[c], clampGroundY[c] - scroll);
        }
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int r = roleOf(enemy.kind());
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (r == ROLE_NEST) {
                AtlasRegion base = enemy.dormant() && nestBaseDormant != null ? nestBaseDormant : nestBase;
                if (base != null) {
                    drawCentred(batch, base, x, y);
                }
            } else if (r == ROLE_CLAMP) {
                AtlasRegion under = clamp[side(x)];
                if (under != null) {
                    drawCentred(batch, under, x, y);
                }
            }
        }
    }

    /**
     * The glints (the Talons' twin muzzles, the clamp guns' muzzle) and the clamps' gas, additive (the batch is in
     * additive blending).
     */
    void drawGlints(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        drawMuzzleGlints(batch, sortie, alpha, null);
        long tick = sortie.tick();
        for (int c = 0; c < PIECES; c++) {
            long age = tick - clampTick[c];
            if (clampTick[c] < 0 || gas.size == 0 || age >= (long) gas.size * GAS_FRAME_TICKS) {
                continue;
            }
            double[] at = jaw[clampSide[c]];
            drawCentred(
                    batch, gas.get((int) (age / GAS_FRAME_TICKS)), clampX[c] + at[0], clampGroundY[c] - scroll + at[1]);
        }
    }

    /**
     * M10 part B: the muzzle glints again over the foreground banks, each at the banks' cover where it is (so a Talon's
     * or a Chimera's tell reads at full under a bank), additive (the batch is in additive blending).
     */
    void drawGlints(SpriteBatch batch, Sortie sortie, float alpha, ForegroundBanks banks) {
        drawMuzzleGlints(batch, sortie, alpha, banks);
    }

    /** The muzzle glints (the Talons', the Chimeras', the clamp guns'), each at {@code banks}' cover (null: full). */
    private void drawMuzzleGlints(SpriteBatch batch, Sortie sortie, float alpha, ForegroundBanks banks) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int r = roleOf(enemy.kind());
            if (r != ROLE_TALON && r != ROLE_CHIMERA && r != ROLE_CLAMP) {
                continue;
            }
            boolean twin = r == ROLE_TALON || r == ROLE_CHIMERA;
            Array<AtlasRegion> glint = twin ? talonGlint : clampGlint;
            EnemyGun gun = enemy.spec().gun().orElse(null);
            if (glint.size == 0 || gun == null || enemy.empStunned()) {
                continue;
            }
            var path = enemy.spec().swoopPath();
            if (twin
                    && path.isPresent()
                    && PlayField.HEIGHT - enemy.renderY(1) > path.get().fireBand()) {
                continue;
            }
            double tell = twin ? gun.firstShotDelay() : CLAMP_TELL_SECONDS;
            double share = tellShare(untilShot(enemy, tick), tell);
            if (share < 0) {
                continue;
            }
            AtlasRegion frame = glint.get(Math.min(glint.size - 1, (int) (share * glint.size)));
            int heading = EnemyLooks.heading(enemy.facing(), HEADINGS);
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            float cover = banks == null ? 1 : banks.cover(x, y);
            if (cover <= 0) {
                continue;
            }
            batch.setColor(1, 1, 1, cover);
            if (twin) {
                for (double[] muzzle : (r == ROLE_CHIMERA ? chimeraMuzzles : muzzles)[heading]) {
                    drawCentred(batch, frame, x + muzzle[0], y + muzzle[1]);
                }
            } else {
                drawCentred(batch, frame, x + clampMuzzle[heading][0], y + clampMuzzle[heading][1]);
            }
        }
        batch.setColor(1, 1, 1, 1);
    }

    /** The clamp gun's muzzle glint (design/enemies/ground/clamp-gun: "a 0.3 s red muzzle glint"). */
    static final double CLAMP_TELL_SECONDS = 0.3;

    /** The death pieces: the Talons' wings spinning off, the rocks shot apart (solid, over the bursts). */
    void drawPieces(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int w = 0; w < PIECES; w++) {
            long age = tick - wingTick[w];
            if (wingTick[w] < 0 || age < 0 || age >= WING_TICKS || wing.size == 0) {
                continue;
            }
            double seconds = (age + alpha) * SimStep.SECONDS;
            int frame = (int) Math.floorMod((wingSide[w] > 0 ? age : -age) / WING_FRAME_TICKS, (long) wing.size);
            float fade = (float) Math.min(1, 2 * (1 - age / (double) WING_TICKS));
            batch.setColor(1, 1, 1, fade);
            drawCentred(
                    batch,
                    wing.get(frame),
                    wingX[w] + wingSide[w] * WING_SPEED_X * seconds,
                    wingY[w] + WING_SPEED_Y * seconds);
        }
        batch.setColor(1, 1, 1, 1);
        for (int b = 0; b < PIECES; b++) {
            Array<AtlasRegion> frames = breakFrames.get(b);
            long age = tick - breakTick[b];
            if (frames == null || age < 0 || age >= (long) frames.size * BREAK_FRAME_TICKS) {
                continue;
            }
            drawCentred(batch, frames.get((int) (age / BREAK_FRAME_TICKS)), breakX[b], breakY[b]);
        }
    }
}
