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
 * M7 part F: a turret unit's own looks beyond its hull (the Warden Tank, design/enemies/ground/warden-tank;
 * tools/art/warden_tank.py, whose docstring gives the sprites, and assets/pivots/warden-tank.json). Its hull is drawn by
 * {@link EnemyLooks} as a walker (16 headings x 2 tread frames, one cycle per stride, facing its path). Here, over the
 * ground units: the turret at the nearest of its 32 aims to {@link Enemy#turretAim()}, turning on its own toward the
 * ship and still over the 0.3 s hold before each burst (the tell: the art has no frames for it), its recoil frame for
 * {@value #RECOIL_TICKS} steps after each shot with the muzzle flash at its muzzle (additive, 2 ages of {@value
 * #FLASH_AGE_TICKS} steps); once the turret part is destroyed, the torn turret ring. On the ground under the ground
 * units: the burnt grey wreck it leaves at its last heading, which scrolls with the ground and rides a conveyor belt it
 * lies on ({@link Sortie#beltSpeedAt}: the simulation keeps no ground wrecks, risk 7 of the part's gap check) until it
 * leaves the screen.
 */
final class TankLooks {
    /** The hull's headings (22.5° steps clockwise from straight down), and the wreck's. */
    static final int HEADINGS = 16;
    /** The turret's aims (11.25° steps clockwise from straight down). */
    static final int AIMS = 32;
    /** A shot shows the recoil frame for this many steps (~0.1 s). */
    static final int RECOIL_TICKS = SimStep.ticks(0.1);
    /** The muzzle flash's two ages, each this many steps (~0.05 s). */
    static final int FLASH_AGE_TICKS = 3;
    /** A shot is the tank's when it leaves within this of its centre, px (the turret's muzzle is 37 px out). */
    static final double FIRED_REACH = 48;
    /** The wrecks remembered at once (a level has a few tanks on the screen at most). */
    static final int WRECKS = 24;
    /** The tanks whose shots are remembered at once. */
    private static final int SLOTS = 16;

    private static final long NEVER = Long.MIN_VALUE / 2;

    /** Per enemy kind its frames; null for a kind without a turret (or without its art). */
    private final Frames[] frames;
    /** The tanks that fired (their serials, -1 free) and the step of their last shot. */
    private final int[] shotSerial = new int[SLOTS];

    private final long[] shotTick = new long[SLOTS];
    private int nextShot;
    /** The wrecks on the ground, carried by their belts. */
    private final BeltWrecks wrecks = new BeltWrecks(WRECKS);

    /**
     * A turret unit's frames.
     *
     * @param muzzle the muzzle's distance along the aim from the centre at rest, px
     * @param muzzleRecoil the same in the recoil frame
     */
    record Frames(
            Array<AtlasRegion> turret,
            AtlasRegion stub,
            Array<AtlasRegion> flash,
            Array<AtlasRegion> wreck,
            double muzzle,
            double muzzleRecoil) {}

    TankLooks(Sprites sprites, List<EnemySpec> kinds, Function<String, JsonValue> pivots) {
        frames = new Frames[kinds.size()];
        for (int k = 0; k < frames.length; k++) {
            EnemySpec kind = kinds.get(k);
            String slug = kind.slug();
            if (kind.turret().isEmpty() || !sprites.has(slug + "-turret")) {
                continue;
            }
            JsonValue pivot = pivots.apply(slug);
            double muzzle = pivot == null ? 0 : pivot.getDouble("muzzle", 0);
            frames[k] = new Frames(
                    sprites.frames(slug + "-turret"),
                    sprites.has(slug + "-turret-stub") ? sprites.region(slug + "-turret-stub") : null,
                    sprites.has(slug + "-flash") ? sprites.frames(slug + "-flash") : new Array<>(),
                    sprites.has(slug + "-wreck") ? sprites.frames(slug + "-wreck") : new Array<>(),
                    muzzle,
                    pivot == null ? muzzle : pivot.getDouble("muzzle_recoil", muzzle));
        }
        restart();
    }

    /** The level restarts: no shot shows, no wreck lies. */
    void restart() {
        Arrays.fill(shotSerial, -1);
        Arrays.fill(shotTick, NEVER);
        wrecks.clear();
        nextShot = 0;
    }

    /** Whether the kind has its turret looks. */
    boolean has(int kind) {
        return kind >= 0 && kind < frames.length && frames[kind] != null;
    }

    // --- the frames --------------------------------------------------------------------------------

    /** The turret's frame: the nearest of its 32 aims to {@code aim} (radians clockwise from down), at rest or recoiled. */
    static int turretFrame(double aim, boolean recoil) {
        return WalkerLooks.nearest(aim, AIMS) * 2 + (recoil ? 1 : 0);
    }

    /** The muzzle flash's frame {@code age} steps after a shot at {@code aim}; -1 once it is over. */
    static int flashFrame(double aim, long age) {
        if (age < 0 || age >= 2L * FLASH_AGE_TICKS) {
            return -1;
        }
        return WalkerLooks.nearest(aim, AIMS) * 2 + (int) (age / FLASH_AGE_TICKS);
    }

    /** Whether a shot {@code age} steps ago still shows the recoil frame. */
    static boolean recoiled(long age) {
        return age >= 0 && age < RECOIL_TICKS;
    }

    /** The wreck's frame at {@code facing} (radians clockwise from down): the nearest of its 16 headings. */
    static int wreckFrame(double facing) {
        return EnemyLooks.heading(facing, HEADINGS);
    }

    /** The point {@code distance} px along {@code aim} (radians clockwise from straight down) from (x, y), y up. */
    static double alongX(double x, double aim, double distance) {
        return x - Math.sin(aim) * distance;
    }

    static double alongY(double y, double aim, double distance) {
        return y - Math.cos(aim) * distance;
    }

    // --- following the simulation ------------------------------------------------------------------

    /** After each simulation step: a tank's shot starts its recoil and flash; a tank's death leaves its wreck. */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        long tick = sortie.tick();
        for (int i = 0; i < events.size(); i++) {
            switch (events.type(i)) {
                case ENEMY_FIRED -> {
                    Enemy unit = unitAt(sortie, events.x(i), events.y(i));
                    if (unit != null) {
                        shot(unit.serial(), tick);
                    }
                }
                case WALKER_DOWN -> {
                    int kind = SimEvents.walkerKind(events.value(i));
                    if (has(kind) && !frames[kind].wreck().isEmpty()) {
                        wrecks.add(
                                kind,
                                events.x(i),
                                events.y(i),
                                wreckFrame(SimEvents.walkerFacing(events.value(i))),
                                sortie.groundScroll());
                    }
                }
                default -> {}
            }
        }
        wrecks.stepped(sortie);
    }

    private void shot(int serial, long tick) {
        for (int s = 0; s < SLOTS; s++) {
            if (shotSerial[s] == serial) {
                shotTick[s] = tick;
                return;
            }
        }
        shotSerial[nextShot] = serial;
        shotTick[nextShot] = tick;
        nextShot = (nextShot + 1) % SLOTS;
    }

    /** The turret unit with these looks nearest (x, y), within {@link #FIRED_REACH}; null for none. */
    private Enemy unitAt(Sortie sortie, double x, double y) {
        Enemy found = null;
        double best = FIRED_REACH;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind())) {
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

    /** The steps since the unit's last shot; -1 for none remembered. */
    long shotAge(Enemy enemy, long tick) {
        for (int s = 0; s < SLOTS; s++) {
            if (shotSerial[s] == enemy.serial() && shotTick[s] != NEVER) {
                return tick - shotTick[s];
            }
        }
        return -1;
    }

    // --- drawing -----------------------------------------------------------------------------------

    /** On the ground under the ground units: the wrecks, carried down by the ground's scroll and up their belts. */
    void drawWrecks(SpriteBatch batch, double scroll) {
        for (int w = 0; w < WRECKS; w++) {
            int kind = wrecks.kind(w);
            if (kind < 0) {
                continue;
            }
            Array<AtlasRegion> wreck = frames[kind].wreck();
            drawCentred(batch, wreck.get(wrecks.frame(w) % wreck.size), wrecks.x(w), wrecks.y(w, scroll));
        }
    }

    /**
     * Over the ground units: each turret (or its torn ring) on its hull, recoiled just after a shot, then additively
     * the muzzle flashes.
     */
    void drawOver(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind()) || LevelRenderer.Depth.of(enemy) != LevelRenderer.Depth.GROUND) {
                continue;
            }
            Frames own = frames[enemy.kind()];
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (!enemy.partAlive()) {
                if (own.stub() != null) {
                    drawCentred(batch, own.stub(), x, y);
                }
                continue;
            }
            boolean recoil = recoiled(shotAge(enemy, tick));
            drawCentred(batch, own.turret().get(turretFrame(enemy.turretAim(), recoil) % own.turret().size), x, y);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!has(enemy.kind())
                    || !enemy.partAlive()
                    || LevelRenderer.Depth.of(enemy) != LevelRenderer.Depth.GROUND) {
                continue;
            }
            Frames own = frames[enemy.kind()];
            double aim = enemy.turretAim();
            long age = shotAge(enemy, tick);
            int frame = flashFrame(aim, age);
            if (frame < 0 || own.flash().isEmpty()) {
                continue;
            }
            double muzzle = recoiled(age) ? own.muzzleRecoil() : own.muzzle();
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            drawCentred(
                    batch, own.flash().get(frame % own.flash().size), alongX(x, aim, muzzle), alongY(y, aim, muzzle));
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
