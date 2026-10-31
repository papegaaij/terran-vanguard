package vanguard.sim;

import static vanguard.sim.PlayerFire.PART_SERIAL;
import static vanguard.sim.ShotGeometry.groundFactor;
import static vanguard.sim.ShotGeometry.inBlast;
import static vanguard.sim.ShotGeometry.onCore;
import static vanguard.sim.ShotGeometry.onField;
import static vanguard.sim.ShotGeometry.partAhead;
import static vanguard.sim.ShotGeometry.struckPart;

import java.util.List;

/**
 * The player's shots hit what they meet (RF9c, out of {@link PlayerFire}): the enemies, the set pieces' parts, the spore
 * mines and the enemies' homing missiles, a torpedo's splash, a hit on a ground object, and the player's recent hit
 * that steers a wingman. The shots are {@link PlayerFire}'s pool; the medium and the enemies' missiles are read through
 * it, as the sortie sets them.
 */
final class ShotHits {
    private final PlayerFire fire;
    private final Armament armament;
    private final SimEvents events;
    private final PlayerFire.Hits hits;
    private final SetPiece[] setPieces;
    private final Pool<Shot> shots;
    private final Homing homing;
    /** M10 part A (design/enemies, Reflect): the mirrors' bounces and absorbs. */
    private final Reflections reflections;
    /** The enemy (a serial) the player's shots damaged last, for the wingman's first target; -1 for none. */
    private int lastHit;
    /** Steps since {@link #lastHit} was damaged. */
    private int sinceHit;

    ShotHits(
            PlayerFire fire,
            Armament armament,
            SimEvents events,
            PlayerFire.Hits hits,
            SetPiece[] setPieces,
            Pool<Shot> shots,
            Homing homing) {
        this.fire = fire;
        this.armament = armament;
        this.events = events;
        this.hits = hits;
        this.setPieces = setPieces;
        this.shots = shots;
        this.homing = homing;
        reflections = new Reflections(armament, events, hits);
    }

    /** Back to the level start: no recent hit. */
    void reset() {
        lastHit = -1;
        sinceHit = Integer.MAX_VALUE;
    }

    /** One step older: the steps since {@link #lastHit} was damaged count on. */
    void age() {
        if (sinceHit < Integer.MAX_VALUE) {
            sinceHit++;
        }
    }

    /** The player's shots damaged the enemy with {@code serial} now (the wingman's first target). */
    void hitBy(int serial) {
        lastHit = serial;
        sinceHit = 0;
    }

    /** Bolts and missiles hit the enemies they pass on the layers they reach. */
    void hitEnemies(Pool<Enemy> enemies) {
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (weapon.delivery().landing() || weapon.delivery().laid()) {
                continue;
            }
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                Layer layer = enemy.layer();
                if (!fire.medium().reaches(weapon, layer)
                        || !onField(enemy)
                        || (enemy.latched() && !hitsLatched(shot.mount()))
                        || passesThrough(enemy, shot.mount())) {
                    continue;
                }
                // M7 part D: a unit part is tested before the body (the Shellback's vent); round 40 R.a: a shot over
                // the body whose line of flight crosses the part's box hits the part, from any side.
                boolean onBody = weapon.size().overlaps(shot.x(), shot.y(), enemy.hitbox(), enemy.x(), enemy.y());
                int part = struckPart(shot, weapon.size(), enemy, onBody);
                boolean onPart = part >= 0;
                if ((!onPart && !onBody) || (weapon.pierce() > 1 && shot.struck(2 * enemy.serial()))) {
                    continue;
                }
                if (enemy.spec().mirror().isPresent() && reflections.meet(shot, enemy)) {
                    // M10 part A (design/enemies, Reflect): the mirror bounced or absorbed it.
                    shots.free(i);
                    break;
                }
                if ((enemy.spec().hardened() && !weapon.antiGround() && !onCore(shot, weapon.size(), enemy))
                        || (enemy.glances(shot.vx(), shot.vy()) && !(weapon.antiSub() && enemy.shellClosed()))
                        || enemy.invulnerable()) {
                    // A hardened unit (M5 part C) without anti-ground, or a walker's frontal armour
                    // for a direct shot from ahead, or (M5 part H) a root burst's growth still growing: it sparks off.
                    // M8 part E: anti-sub fire cracks a coil's closed shell (§5 E8). M10 part A: a unit under a dome
                    // spends it with a gold spark; a regrowing turret knitting sparks it off.
                    events.add(
                            enemy.covered() ? SimEvents.Type.DOME_HIT : SimEvents.Type.SHOT_GLANCED,
                            shot.x(),
                            shot.y(),
                            shot.mount());
                    shots.free(i);
                    break;
                }
                events.add(SimEvents.Type.ENEMY_HIT, shot.x(), shot.y(), shot.mount());
                if (shot.mount() < armament.size()) {
                    lastHit = enemy.serial();
                    sinceHit = 0;
                }
                boolean spent = shot.pierced();
                int struck = enemy.serial();
                if (onPart) {
                    if (enemy.damagePart(part, shot.damage() * groundFactor(weapon, layer), false)) {
                        hits.unitPartDestroyed(j, part, shot.mount());
                    }
                } else if (enemy.damageFacing(
                        shot.damage() * groundFactor(weapon, layer) * enemy.rearFactor(shot.vx(), shot.vy()),
                        weapon.antiSub(),
                        shot.vx(),
                        shot.vy())) {
                    // M7 part F: a direct shot into a rear plate does its multiplier (the Warden Tank's); M9 part C:
                    // facing armour's factor by the shot's direction, after the shield (the Gilded Gunship's).
                    hits.enemyDestroyed(j, shot.mount());
                }
                if (spent) {
                    splash(shot, struck, enemies);
                    shots.free(i);
                    break;
                }
            }
        }
    }

    /**
     * M5 part F (design/enemies/air/lamprey, user decision F3 = a): whether the shots of {@code mount}
     * hit a Lamprey latched onto the hull: the rear mount's and those of a weapon with the {@code side}
     * or {@code rear} trait; front and forward wing shots pass it, and so do the wingman's.
     */
    boolean hitsLatched(int mount) {
        if (mount >= armament.size()) {
            return false;
        }
        Armament.Mount fitted = armament.mount(mount);
        return fitted.slot() == Armament.Slot.REAR
                || (fitted.fires()
                        && (fitted.weapon().flanking() || fitted.overdrive().flanking()));
    }

    /** Bolts and missiles hit the armed spore mines on the player's layer. */
    void hitMines(Pool<Mine> mines) {
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (weapon.delivery().landing() || !fire.medium().reaches(weapon, Layer.AIR)) {
                continue;
            }
            for (int j = mines.size() - 1; j >= 0; j--) {
                Mine mine = mines.get(j);
                if (!mine.armed()
                        || !weapon.size().overlaps(shot.x(), shot.y(), EnemyGun.MineSpec.BOX, mine.x(), mine.y())) {
                    continue;
                }
                events.add(SimEvents.Type.ENEMY_HIT, shot.x(), shot.y(), shot.mount());
                boolean spent = shot.pierced();
                if (mine.damage(shot.damage())) {
                    hits.mineDestroyed(j);
                }
                if (spent) {
                    shots.free(i);
                    break;
                }
            }
        }
    }

    /**
     * M7 part F: shots that reach the air (bolts, missiles, homing shots, Rook's) hit the enemies' homing
     * missiles: each takes the shot's damage and is brought down at 0 HP ({@link Missiles#SHOT_DOWN}).
     */
    void hitMissiles() {
        Missiles missiles = fire.missiles();
        if (missiles == null || missiles.size() == 0) {
            return;
        }
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (weapon.delivery().landing() || !fire.medium().reaches(weapon, Layer.AIR)) {
                continue;
            }
            for (int j = missiles.size() - 1; j >= 0; j--) {
                Missile missile = missiles.get(j);
                if (!weapon.size().overlaps(shot.x(), shot.y(), missile.box(), missile.x(), missile.y())) {
                    continue;
                }
                events.add(SimEvents.Type.ENEMY_HIT, shot.x(), shot.y(), shot.mount());
                boolean spent = shot.pierced();
                if (missile.damage(shot.damage())) {
                    missiles.destroy(j, Missiles.SHOT_DOWN);
                }
                if (spent) {
                    shots.free(i);
                    break;
                }
            }
        }
    }

    /**
     * Bolts and missiles that reach a set piece's layer hit its living parts; what touches its
     * armoured body (a boss's in its current pose) instead glances off, as does a shot on a part
     * that takes no damage now (a boss's fire-only turrets, a shut window). A boss on high air is
     * hit only by a missile on the part it is locked onto; it passes beneath the rest. M5 part E: an
     * arena boss's parts are hit on their own layers (a slam arm's segments are its hit boxes, an open
     * weak spot takes its multiplier, an {@code anti-ground} weapon doubles on a surfaced part), and it
     * has no armoured body (its platform is scenery: shots fly over it).
     */
    void hitSetPieces() {
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            if (!piece.present()) {
                continue;
            }
            List<PartSpec> parts = piece.spec().parts();
            boolean overhead = Homing.overhead(piece);
            boolean anchored = piece.arena().isPresent();
            for (int i = shots.size() - 1; i >= 0; i--) {
                Shot shot = shots.get(i);
                WeaponSpec weapon = shot.weapon();
                if (weapon.delivery().landing()
                        || (!anchored && !fire.medium().reaches(weapon, piece.layer()))
                        || (overhead && homing.overhead(shot.target()) != k)) {
                    continue;
                }
                int lockedPart = overhead ? (shot.target() - PART_SERIAL) % SetPieceSpec.MAX_PARTS : -1;
                if (piece.plateTouches(weapon.size(), shot.x(), shot.y())
                        && (!anchored || fire.medium().reaches(weapon, piece.layer()))) {
                    // M7 part F: a walking boss's front plate turns every shot that meets it.
                    events.add(SimEvents.Type.SHOT_GLANCED, shot.x(), shot.y(), shot.mount());
                    shots.free(i);
                    continue;
                }
                boolean gone = false;
                for (int p = 0; p < parts.size() && piece.present(); p++) {
                    if (overhead && p != lockedPart) {
                        continue;
                    }
                    Layer partLayer = piece.partLayer(p);
                    if (piece.partWrecked(p)
                            || (anchored && !fire.medium().reaches(weapon, partLayer))
                            || !piece.partTouches(p, weapon.size(), shot.x(), shot.y())
                            || !piece.partOnField(p)
                            || (weapon.pierce() > 1 && shot.struck(-1 - k * SetPieceSpec.MAX_PARTS - p))) {
                        continue;
                    }
                    if (piece.partShielded(p)) {
                        // A boss part that takes no damage yet (its descent, a core before its phase).
                        events.add(SimEvents.Type.SHOT_GLANCED, shot.x(), shot.y(), shot.mount());
                        shots.free(i);
                        gone = true;
                        break;
                    }
                    events.add(SimEvents.Type.ENEMY_HIT, shot.x(), shot.y(), shot.mount());
                    boolean spent = shot.pierced();
                    if (piece.damagePartAt(
                            p,
                            shot.damage() * groundFactor(weapon, partLayer),
                            shot.x(),
                            shot.y(),
                            Trig.sin(shot.heading()),
                            Trig.cos(shot.heading()))) {
                        hits.partDestroyed(k, p, shot.mount());
                    }
                    if (spent) {
                        shots.free(i);
                        gone = true;
                        break;
                    }
                }
                if (!gone
                        && !overhead
                        && !anchored
                        && piece.present()
                        && ((weapon.size().overlaps(shot.x(), shot.y(), piece.body(), piece.x(), piece.y())
                                        && !partAhead(piece, shot))
                                || piece.neckTouches(weapon.size(), shot.x(), shot.y()))) {
                    events.add(SimEvents.Type.SHOT_GLANCED, shot.x(), shot.y(), shot.mount());
                    shots.free(i);
                }
            }
        }
    }

    /**
     * M8 part A (design/player/weapons/harpoon-torpedoes; decided D13): a torpedo with a splash that struck at its
     * place deals its {@link WeaponSpec#splash()} share of its damage to every other unit within its blast on a layer
     * it reaches (the unit with the serial {@code struck} excepted; -1 for none); hardened units without {@code
     * anti-ground} and growths are not hurt. Units only (not ground objects or set pieces' parts, default pending the
     * user's review).
     */
    void splash(Shot shot, int struck, Pool<Enemy> enemies) {
        WeaponSpec weapon = shot.weapon();
        if (!(weapon.splash() > 0) || !(weapon.blast() > 0)) {
            return;
        }
        double damage = shot.damage() * weapon.splash();
        double x = shot.x();
        double y = shot.y();
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            Layer layer = enemy.layer();
            if (enemy.serial() == struck
                    || !fire.medium().reaches(weapon, layer)
                    || !onField(enemy)
                    || passesThrough(enemy, shot.mount())
                    || enemy.latched()
                    || (enemy.spec().hardened() && !weapon.antiGround())
                    || enemy.invulnerable()
                    || !inBlast(x, y, weapon.blast(), enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            if (enemy.damage(damage * groundFactor(weapon, layer), false, false, weapon.antiSub())) {
                hits.enemyDestroyed(j, shot.mount());
            }
        }
    }

    /**
     * M9 part B (design/campaign Level 29, the wavering Stormhawks): whether a shot of {@code mount} passes through
     * {@code enemy}: the escort's (Rook's or the Bulwark's, one past the armament's mounts) pass a neutral craft.
     */
    boolean passesThrough(Enemy enemy, int mount) {
        return mount >= armament.size() && enemy.spec().neutral();
    }

    /** A hit on a ground object: a trigger counts it, a destructible takes the damage. */
    void strike(int index, GroundObject object, double damage) {
        if (object.spec().trigger()) {
            if (object.countHit()) {
                hits.triggerReleased(index);
            }
        } else if (object.damage(damage)) {
            hits.groundDestroyed(index);
        }
    }

    /** The enemy (a serial) the player's shots damaged within the last {@code ticks} steps; -1 for none. */
    int recentHit(int ticks) {
        return sinceHit <= ticks ? lastHit : -1;
    }

    /** The player's last hit, which steers a wingman: hashed only when one flies. */
    void addRecentHitTo(StateHash hash) {
        hash.add(lastHit).add(sinceHit);
    }
}
