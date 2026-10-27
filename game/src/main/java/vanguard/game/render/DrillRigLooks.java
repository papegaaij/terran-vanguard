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
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part B: the Drill Rig's own looks beyond its collar (design/enemies/ground/drill-rig; tools/art/drill_rig.py, whose
 * docstring gives the sprites, and assets/pivots/drill-rig.json). The core is drawn by {@link EnemyLooks}: the collar's
 * 8 frames spinning at {@value EnemyLooks#RIG_FPS} fps with its additive {@code -glow} (the bore's lime eye), its hit
 * flash and the generic glance's spark when a shot glances off its hardened shell ({@code SHOT_GLANCED}), its {@code
 * large} explosion. Here, on the ground under the ground units: the feed hoses' and root ring's decal under each live
 * rig ({@code -base}), its four bellows pumps at their parts' offsets heaving at {@value #PUMP_FPS} fps, or burst once
 * destroyed ({@code -pump-dead}); where a rig died, the cracked ice with the open bore ({@code -crack}, replacing the
 * base) and the stilled collar ({@code -stump}), both kept on the ground (they scroll with it) until they leave the
 * screen. Over the ground units: the steam plume rising off a dead bore ({@code -steam}, {@value #STEAM_FPS} fps, normal
 * blending: stepped translucency) and, additively, the muzzle's tell, lime light gathering on the collar's rim over the
 * {@value #TELL_SECONDS} s before each ring ({@code -muzzle_0..3}), then its release flash ({@code -muzzle_4..5}, {@value
 * #FLASH_FRAME_TICKS} steps each). The simulation keeps the ring's timer to itself, so the next ring is foreseen from the
 * last one: the stat block's interval plus each lost pump's slowdown at that ring (as the simulation sets it), the
 * first ring its first shot delay after the rig came in sight; like the simulation's timer it waits while the ship is
 * outside the gun's arc (behind the rig, {@link #inArc}).
 */
final class DrillRigLooks {
    /** The Drill Rig's slug: its kind has these looks. */
    static final String SLUG = "drill-rig";
    /** The pumps' heave: 4 frames at this rate. */
    static final double PUMP_FPS = 6;
    /** The steam plume's loop: 8 frames at this rate. */
    static final double STEAM_FPS = 8;
    /** The muzzle's tell before each ring, s, over its first 4 frames. */
    static final double TELL_SECONDS = 0.5;

    static final int TELL_TICKS = SimStep.ticks(TELL_SECONDS);
    static final int TELL_FRAMES = 4;
    /** The release flash: the muzzle's frames 4 and 5, this many steps each. */
    static final int FLASH_FRAME_TICKS = 3;

    static final int FLASH_FRAMES = 2;
    /** A shot is the rig's when it leaves within this of its centre, px (the ring leaves from the core). */
    static final double FIRED_REACH = 24;
    /** The steam frame's centre lies this far above the bore (its bore at (64, 112) of the 128x160 frame), px. */
    static final double STEAM_ABOVE_BORE = 32;
    /** A rig's remains are dropped once this far below the bottom edge, px (the crack decal is 192 px). */
    static final double GONE_BELOW = 120;
    /** The rigs remembered at once, and the remains. */
    private static final int SLOTS = 8;

    private static final int WRECKS = 8;
    private static final long NEVER = Long.MIN_VALUE / 2;

    /** The Drill Rig's enemy kind in the sortie; -1 without one (or without its art). */
    private final int kind;

    private final Array<AtlasRegion> pumps;
    private final AtlasRegion pumpDead;
    private final AtlasRegion base;
    private final AtlasRegion crack;
    private final AtlasRegion stump;
    private final Array<AtlasRegion> steam;
    private final Array<AtlasRegion> muzzle;
    /** The steam frame's centre above the bore, px (from the pivot file, else {@link #STEAM_ABOVE_BORE}). */
    private final double steamAbove;
    /** The rigs seen (their serials, -1 free), their last ring's step and the next ring's foreseen step. */
    private final int[] serial = new int[SLOTS];

    private final long[] shotTick = new long[SLOTS];
    private final long[] nextShot = new long[SLOTS];
    private int nextSlot;
    /** The remains: x, their place on the ground (screen y plus the ground's scroll), the step of the death. */
    private final double[] wreckX = new double[WRECKS];

    private final double[] wreckY = new double[WRECKS];
    private final long[] wreckTick = new long[WRECKS];
    private final boolean[] wreckUsed = new boolean[WRECKS];
    private int nextWreck;

    DrillRigLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        int found = -1;
        for (int k = 0; k < kinds.size(); k++) {
            if (kinds.get(k).slug().equals(SLUG) && sprites.has(SLUG)) {
                found = k;
            }
        }
        kind = found;
        pumps = optional(sprites, SLUG + "-pump");
        pumpDead = sprites.has(SLUG + "-pump-dead") ? sprites.region(SLUG + "-pump-dead") : null;
        base = sprites.has(SLUG + "-base") ? sprites.region(SLUG + "-base") : null;
        crack = sprites.has(SLUG + "-crack") ? sprites.region(SLUG + "-crack") : null;
        stump = sprites.has(SLUG + "-stump") ? sprites.region(SLUG + "-stump") : null;
        steam = optional(sprites, SLUG + "-steam");
        muzzle = optional(sprites, SLUG + "-muzzle");
        steamAbove = steamAboveBore(pivots.apply(SLUG));
        restart();
    }

    /** The steam frame's centre above the bore, px: the pivot file's {@code steam} bore under its frame's centre. */
    static double steamAboveBore(JsonValue pivots) {
        JsonValue steam = pivots == null ? null : pivots.get("steam");
        if (steam == null || steam.get("bore") == null || steam.get("size") == null) {
            return STEAM_ABOVE_BORE;
        }
        return steam.get("bore").getDouble(1) - steam.get("size").getDouble(1) / 2;
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>();
    }

    /** The level restarts: no rig is remembered, no remains lie. */
    void restart() {
        Arrays.fill(serial, -1);
        Arrays.fill(shotTick, NEVER);
        Arrays.fill(nextShot, NEVER);
        Arrays.fill(wreckUsed, false);
        nextSlot = 0;
        nextWreck = 0;
    }

    /** Whether the sortie has Drill Rigs with their art. */
    boolean any() {
        return kind >= 0;
    }

    private boolean rig(Enemy enemy) {
        return kind >= 0 && enemy.kind() == kind && DrawPasses.Depth.of(enemy) == DrawPasses.Depth.GROUND;
    }

    // --- the frames --------------------------------------------------------------------------------

    /** A live pump's heave frame at step {@code tick}, pump {@code p} a frame on from the one before it. */
    static int pumpFrame(long tick, int p, int frames) {
        return (int) Math.floorMod((long) Math.floor(tick * PUMP_FPS / SimStep.PER_SECOND) + p, (long) frames);
    }

    /** The steam plume's frame {@code age} steps after the rig's death (a loop). */
    static int steamFrame(long age, int frames) {
        return (int) Math.floorMod((long) Math.floor(Math.max(0, age) * STEAM_FPS / SimStep.PER_SECOND), (long) frames);
    }

    /**
     * The muzzle's frame: the tell's frames 0-3 over the {@value #TELL_TICKS} steps before the ring due at {@code next},
     * the release flash's 4-5 over the steps after the ring at {@code last}; -1 for none.
     */
    static int muzzleFrame(long tick, long last, long next) {
        long since = tick - last;
        if (last != NEVER && since >= 0 && since < (long) FLASH_FRAMES * FLASH_FRAME_TICKS) {
            return TELL_FRAMES + (int) (since / FLASH_FRAME_TICKS);
        }
        long left = next - tick;
        if (next != NEVER && left > 0 && left <= TELL_TICKS) {
            return Math.clamp((TELL_TICKS - left) * TELL_FRAMES / TELL_TICKS, 0, TELL_FRAMES - 1);
        }
        return -1;
    }

    /**
     * The steps from a ring to the next as the simulation sets them as it fires: the stat block's interval, longer by
     * each lost pump's slowdown (the pumps' {@code slows}).
     */
    static int interval(Enemy enemy) {
        EnemyGun gun = enemy.spec().gun().orElseThrow();
        double seconds = gun.intervalSeconds();
        List<EnemySpec.UnitPart> parts = enemy.spec().parts();
        for (int p = 0; p < parts.size(); p++) {
            if (!enemy.partAlive(p)) {
                seconds += parts.get(p).slowsGunSeconds();
            }
        }
        return SimStep.ticks(seconds);
    }

    /**
     * Whether the ship at ({@code shipX}, {@code shipY}) is inside the rig's gun's arc, as the simulation's turret
     * tracking decides it: the rig on the screen and the ship within the arc's angle of its facing.
     */
    static boolean inArc(Enemy enemy, double shipX, double shipY) {
        double x = enemy.renderX(1);
        double y = enemy.renderY(1);
        if (!PlayField.overlaps(x, y, enemy.hitbox())) {
            return false;
        }
        EnemyGun gun = enemy.spec().gun().orElseThrow();
        double wanted = StrictMath.atan2(-(shipX - x), -(shipY - y));
        double off = gun.arcFacingRadians() == 0
                ? wanted
                : Math.IEEEremainder(wanted - gun.arcFacingRadians(), 2 * StrictMath.PI);
        return Math.abs(off) <= gun.arcRadians();
    }

    // --- following the simulation ------------------------------------------------------------------

    /**
     * After each simulation step: a rig coming in sight foresees its first ring, a ring the next one; a rig's death
     * leaves its remains on the ground.
     */
    void stepped(Sortie sortie) {
        if (kind < 0) {
            return;
        }
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (rig(enemy) && slot(enemy.serial()) < 0 && enemy.spec().gun().isPresent()) {
                int s = nextSlot;
                nextSlot = (nextSlot + 1) % SLOTS;
                serial[s] = enemy.serial();
                shotTick[s] = NEVER;
                nextShot[s] = tick + SimStep.ticks(enemy.spec().gun().get().firstShotDelay());
            }
        }
        // The simulation's ring timer runs only while the ship is inside the gun's arc: the ring comes a step later
        // for each step it is not.
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int s = rig(enemy) ? slot(enemy.serial()) : -1;
            if (s >= 0 && enemy.spec().gun().isPresent() && !inArc(enemy, shipX, shipY)) {
                nextShot[s]++;
            }
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            switch (events.type(e)) {
                case ENEMY_FIRED -> {
                    Enemy unit = rigAt(sortie, events.x(e), events.y(e));
                    int s = unit == null ? -1 : slot(unit.serial());
                    if (s >= 0) {
                        shotTick[s] = tick;
                        nextShot[s] = tick + interval(unit);
                    }
                }
                case ENEMY_DESTROYED -> {
                    if (events.value(e) == kind) {
                        wreckX[nextWreck] = events.x(e);
                        wreckY[nextWreck] = events.y(e) + sortie.groundScroll();
                        wreckTick[nextWreck] = tick;
                        wreckUsed[nextWreck] = true;
                        nextWreck = (nextWreck + 1) % WRECKS;
                    }
                }
                default -> {}
            }
        }
        double scroll = sortie.groundScroll();
        for (int w = 0; w < WRECKS; w++) {
            if (wreckUsed[w] && wreckY[w] - scroll < -GONE_BELOW) {
                wreckUsed[w] = false;
            }
        }
    }

    private int slot(int unit) {
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] == unit) {
                return s;
            }
        }
        return -1;
    }

    /** The rig nearest (x, y) within {@link #FIRED_REACH}; null for none. */
    private Enemy rigAt(Sortie sortie, double x, double y) {
        Enemy found = null;
        double best = FIRED_REACH;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!rig(enemy)) {
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

    // --- drawing -----------------------------------------------------------------------------------

    /**
     * On the ground under the ground units: the remains' cracked ice and stilled collars, then under each live rig its
     * hoses' decal and its pumps (heaving, or burst).
     */
    void drawGround(SpriteBatch batch, Sortie sortie, float alpha, double scroll) {
        if (kind < 0) {
            return;
        }
        for (int w = 0; w < WRECKS; w++) {
            if (!wreckUsed[w]) {
                continue;
            }
            double y = wreckY[w] - scroll;
            if (crack != null) {
                drawCentred(batch, crack, wreckX[w], y);
            }
            if (stump != null) {
                drawCentred(batch, stump, wreckX[w], y);
            }
        }
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!rig(enemy)) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (base != null) {
                drawCentred(batch, base, x, y);
            }
            List<EnemySpec.UnitPart> parts = enemy.spec().parts();
            for (int p = 0; p < parts.size(); p++) {
                EnemySpec.UnitPart part = parts.get(p);
                AtlasRegion frame = enemy.partAlive(p)
                        ? (pumps.isEmpty() ? null : pumps.get(pumpFrame(tick, p, pumps.size)))
                        : pumpDead;
                if (frame != null) {
                    drawCentred(batch, frame, x + part.dx(), y + part.dy());
                }
            }
        }
    }

    /**
     * Over the ground units: the steam plumes off the dead bores (normal blending), then additively each live rig's
     * bore glow ({@code -glow}, at the collar's frame; dimmed as an EMP dims a machine's lights) and its muzzle tell and
     * release flash.
     */
    void drawOver(SpriteBatch batch, Sortie sortie, EnemyLooks[] looks, float alpha, double scroll) {
        if (kind < 0) {
            return;
        }
        long tick = sortie.tick();
        if (!steam.isEmpty()) {
            for (int w = 0; w < WRECKS; w++) {
                if (wreckUsed[w]) {
                    AtlasRegion frame = steam.get(steamFrame(tick - wreckTick[w], steam.size));
                    drawCentred(batch, frame, wreckX[w], wreckY[w] - scroll + steamAbove);
                }
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!rig(enemy)) {
                continue;
            }
            EnemyLooks look = looks[enemy.kind()];
            float glow = ArsenalLooks.glowShare(enemy);
            if (!look.glow().isEmpty() && look.glow().size == look.frames().size && glow > 0) {
                // The collar's frame as UnitPasses draws it (its step's phase is the unit's index).
                batch.setColor(1, 1, 1, glow);
                AtlasRegion frame = look.glowFrame(enemy.facing(), look.step(tick, i, enemy.burstSeconds()));
                drawCentred(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha));
                batch.setColor(Color.WHITE);
            }
            int s = slot(enemy.serial());
            if (s < 0 || muzzle.isEmpty()) {
                continue;
            }
            // Silent once its last pump is gone (the ring is bound to the pumps): only a release flash still showing.
            boolean silent = enemy.partCount() > 0 && enemy.partsStanding() == 0;
            int frame = muzzleFrame(tick, shotTick[s], silent ? NEVER : nextShot[s]);
            if (frame >= 0) {
                drawCentred(batch, muzzle.get(frame % muzzle.size), enemy.renderX(alpha), enemy.renderY(alpha));
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Whether the remains lie at (x, y) on the screen, the ground scrolled {@code scroll} px (for the tests). */
    boolean remainsAt(double x, double y, double scroll) {
        for (int w = 0; w < WRECKS; w++) {
            if (wreckUsed[w] && Math.abs(wreckX[w] - x) < 1 && Math.abs(wreckY[w] - scroll - y) < 1) {
                return true;
            }
        }
        return false;
    }

    /** The remains lying now (for the tests). */
    int remains() {
        int count = 0;
        for (boolean used : wreckUsed) {
            count += used ? 1 : 0;
        }
        return count;
    }

    /** The step the rig's next ring is foreseen at; {@link Long#MIN_VALUE} / 2 for an unknown rig (for the tests). */
    long nextRing(Enemy enemy) {
        int s = slot(enemy.serial());
        return s < 0 ? NEVER : nextShot[s];
    }
}
