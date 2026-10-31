package vanguard.sim;

import static vanguard.sim.PlayerFire.CLIMB_TURN;
import static vanguard.sim.PlayerFire.FLANK_PREFERENCE;
import static vanguard.sim.PlayerFire.MISSILE_SERIAL;
import static vanguard.sim.PlayerFire.PART_SERIAL;
import static vanguard.sim.PlayerFire.TRIGGER_SERIAL;
import static vanguard.sim.ShotGeometry.distanceSquared;
import static vanguard.sim.ShotGeometry.onField;

/**
 * What the ship's seeking weapons aim at, for {@link PlayerFire}: a turret's pod turning to the nearest
 * enemy all round (favouring the flanks and the rear), homing missiles and torpedoes locking onto and
 * steering at a target in their cone (a high-air boss's parts all round, a sunken trigger for a
 * torpedo), and the lookup of a lock's target (the wingman's lobbed shell). It holds the target
 * position the last search found; the turrets' headings and targets are the fire's arrays.
 */
final class Homing {
    private final PlayerFire fire;
    private final Ship ship;
    private final SetPiece[] setPieces;
    /** A turret mount's heading, radians clockwise from up; its locked target (a serial), -1 for none. */
    private final double[] turretHeadings;

    private final int[] turretTargets;

    /** The homing target found by {@link #locked} or {@link #nearestInCone}. */
    private double targetX;

    private double targetY;
    /** The layer of the target {@link #located} found last. */
    private Layer targetLayer = Layer.AIR;

    Homing(PlayerFire fire, Ship ship, SetPiece[] setPieces, double[] turretHeadings, int[] turretTargets) {
        this.fire = fire;
        this.ship = ship;
        this.setPieces = setPieces;
        this.turretHeadings = turretHeadings;
        this.turretTargets = turretTargets;
    }

    /** The target position the last search found. */
    double targetX() {
        return targetX;
    }

    double targetY() {
        return targetY;
    }

    /**
     * Turns mount {@code m}'s turret by at most its slew rate for one step: towards the nearest
     * enemy (or set-piece part) within its reach all round, where one outside its forward cone wins
     * when it is no farther than {@link PlayerFire#FLANK_PREFERENCE} times the nearest; back to forward
     * without one.
     */
    void aim(int m, WeaponSpec weapon, Pool<Enemy> enemies) {
        WeaponSpec.Muzzle muzzle = weapon.muzzles().getFirst();
        double x = ship.x() + muzzle.dx();
        double y = ship.y() + muzzle.dy();
        double reach = weapon.range() * weapon.range();
        double nearest = reach;
        double flank = reach;
        int nearestSerial = -1;
        int flankSerial = -1;
        double nearestX = 0;
        double nearestY = 0;
        double flankX = 0;
        double flankY = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            // M5 part E: never one under the water; M5 part F: nor a Lamprey latched onto the hull, which
            // a turret's shots pass unless its mount may hit it. M7 part G: nor one the seekers ignore (a mote).
            if (!onField(enemy)
                    || enemy.spec().seekersIgnore()
                    || !fire.medium().reaches(weapon, enemy.layer())
                    || (enemy.latched() && !fire.hitsLatched(m))) {
                continue;
            }
            double d = distanceSquared(x, y, enemy.x(), enemy.y());
            if (d <= nearest) {
                nearest = d;
                nearestSerial = enemy.serial();
                nearestX = enemy.x();
                nearestY = enemy.y();
            }
            if (d <= flank && outsideCone(x, y, enemy.x(), enemy.y(), muzzle.angle(), weapon.coneHalfAngle())) {
                flank = d;
                flankSerial = enemy.serial();
                flankX = enemy.x();
                flankY = enemy.y();
            }
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            for (int p = 0; p < piece.partCount(); p++) {
                // M5 part E: on the part's own layer (an arena boss's part under the water is none).
                if (!fire.medium().reaches(weapon, piece.partLayer(p)) || !partTarget(piece, p)) {
                    continue;
                }
                double d = distanceSquared(x, y, targetX, targetY);
                int serial = PART_SERIAL + k * SetPieceSpec.MAX_PARTS + p;
                if (d <= nearest) {
                    nearest = d;
                    nearestSerial = serial;
                    nearestX = targetX;
                    nearestY = targetY;
                }
                if (d <= flank && outsideCone(x, y, targetX, targetY, muzzle.angle(), weapon.coneHalfAngle())) {
                    flank = d;
                    flankSerial = serial;
                    flankX = targetX;
                    flankY = targetY;
                }
            }
        }
        double wanted = muzzle.angle();
        if (flankSerial >= 0 && flank <= FLANK_PREFERENCE * FLANK_PREFERENCE * nearest) {
            turretTargets[m] = flankSerial;
            wanted = StrictMath.atan2(flankX - x, flankY - y);
        } else if (nearestSerial >= 0) {
            turretTargets[m] = nearestSerial;
            wanted = StrictMath.atan2(nearestX - x, nearestY - y);
        } else {
            turretTargets[m] = -1;
        }
        double delta = Math.IEEEremainder(wanted - turretHeadings[m], 2 * StrictMath.PI);
        double most = weapon.turnRate() * SimStep.SECONDS;
        turretHeadings[m] = Math.IEEEremainder(turretHeadings[m] + Math.clamp(delta, -most, most), 2 * StrictMath.PI);
    }

    /** Whether (tx, ty) lies outside the cone of {@code halfAngle} either side of {@code forward} seen from (x, y). */
    private static boolean outsideCone(double x, double y, double tx, double ty, double forward, double halfAngle) {
        double off = Math.IEEEremainder(StrictMath.atan2(tx - x, ty - y) - forward, 2 * StrictMath.PI);
        return Math.abs(off) > halfAngle;
    }

    /**
     * Steers at the locked target, or locks onto the nearest enemy (or set-piece part) in range and
     * in the cone on a layer it reaches when it has none. A torpedo (M5 part E) picks its target as
     * it is fired and again only when that target dies (or leaves its reach), a sunken trigger among
     * them; without one it runs straight.
     */
    void home(Shot shot, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        boolean found = locked(shot, enemies, ground);
        if (!found && shot.weapon().delivery() == WeaponSpec.Delivery.TORPEDO && shot.target() < 0) {
            return;
        }
        if (!found) {
            // M9 part A (design/player/weapons/swarm-tail): a rear-first missile picks behind the nose line first.
            int target = shot.weapon().seek() == WeaponSpec.Seek.REAR_FIRST
                    ? rearFirst(shot, enemies)
                    : nearestInCone(shot, enemies, ground);
            shot.lock(target);
            found = target >= 0;
        }
        if (found) {
            shot.steer(targetX, targetY, overhead(shot.target()) >= 0 ? CLIMB_TURN : 1);
        }
    }

    /**
     * The index of the set piece whose part {@code target} (a lock's serial) is, if that set piece
     * is a boss on high air (the missile climbs to it); -1 otherwise.
     */
    int overhead(int target) {
        if (target < PART_SERIAL) {
            return -1;
        }
        int piece = (target - PART_SERIAL) / SetPieceSpec.MAX_PARTS;
        return piece < setPieces.length && overhead(setPieces[piece]) ? piece : -1;
    }

    /** Whether {@code piece} is a boss above the play field: missiles climb to the part they lock onto. */
    static boolean overhead(SetPiece piece) {
        return piece.layer() == Layer.HIGH_AIR && piece.boss().isPresent();
    }

    /**
     * Whether the shot's locked target is still on the field, on a layer the shot reaches (M5 part
     * E: a unit that dives is lost to a missile); it is then at the target position.
     */
    private boolean locked(Shot shot, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        if (shot.target() >= MISSILE_SERIAL) {
            return missile(shot.target()) && fire.medium().reaches(shot.weapon(), Layer.AIR);
        }
        if (shot.target() >= TRIGGER_SERIAL) {
            return trigger(shot.target() - TRIGGER_SERIAL, ground);
        }
        return located(shot.target(), enemies) && fire.medium().reaches(shot.weapon(), targetLayer);
    }

    /**
     * M5 part E: whether the sunken trigger with the ground serial {@code serial} is still to be
     * freed and on the field; it is then at the target position.
     */
    private boolean trigger(int serial, Pool<GroundObject> ground) {
        for (int j = 0; j < ground.size(); j++) {
            GroundObject object = ground.get(j);
            if (object.serial() == serial) {
                if (!sunkenTrigger(object)) {
                    return false;
                }
                targetX = object.x();
                targetY = object.y();
                return true;
            }
        }
        return false;
    }

    /** M5 part E: a sunken trigger still to be freed, on the field: what a torpedo seeks besides units and parts. */
    private static boolean sunkenTrigger(GroundObject object) {
        GroundObjectSpec spec = object.spec();
        return spec.trigger()
                && spec.submerged()
                && object.hittable()
                && PlayField.overlaps(object.x(), object.y(), spec.size());
    }

    /**
     * Whether {@code target} (a serial as the homing locks use, -1 for none) is on the field; it is
     * then at the target position, on the target layer.
     */
    boolean located(int target, Pool<Enemy> enemies) {
        if (target < 0) {
            return false;
        }
        if (target >= MISSILE_SERIAL) {
            targetLayer = Layer.AIR;
            return missile(target);
        }
        if (target >= PART_SERIAL) {
            int piece = (target - PART_SERIAL) / SetPieceSpec.MAX_PARTS;
            int part = (target - PART_SERIAL) % SetPieceSpec.MAX_PARTS;
            if (piece >= setPieces.length || !partTarget(setPieces[piece], part)) {
                return false;
            }
            targetLayer = setPieces[piece].partLayer(part);
            return true;
        }
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (enemy.serial() == target) {
                // M5 part F (user decision F3 = a): a homing shot loses a target that latched onto the hull.
                // M7 part B: and one that went under the sand.
                if (!onField(enemy) || enemy.latched() || !enemy.layer().touchable()) {
                    return false;
                }
                targetX = enemy.x();
                targetY = enemy.y();
                targetLayer = enemy.layer();
                return true;
            }
        }
        return false;
    }

    /** Whether a set piece's part can be a homing target: present, alive and on the field; it is then the target position. */
    private boolean partTarget(SetPiece piece, int part) {
        if (!piece.present() || piece.partWrecked(part) || piece.partShielded(part)) {
            return false;
        }
        double x = piece.partX(part);
        double y = piece.partY(part);
        if (!PlayField.overlaps(x, y, piece.spec().parts().get(part).box())) {
            return false;
        }
        targetX = x;
        targetY = y;
        return true;
    }

    /**
     * The serial of the nearest target in range and in the cone (a high-air boss's parts in range
     * all round; for a torpedo, M5 part E, a sunken trigger too), -1 for none; the target position is
     * set.
     */
    int nearestInCone(Shot shot, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        WeaponSpec weapon = shot.weapon();
        Medium medium = fire.medium();
        // M8 part A: its range as fired (a torpedo's under water × the torpedo range).
        double best = shot.range() * shot.range();
        int nearest = -1;
        double nearestX = 0;
        double nearestY = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            // M5 part C: a hardened unit is no target unless the weapon is anti-ground; M5 part E:
            // only one on a layer the shot reaches (a torpedo's sub and ground, a missile's the rest). M7 part
            // G (user decision 2026-10-20): never one the seekers ignore (a mote); a shot flying into it still hits.
            if (onField(enemy)
                    && !enemy.latched()
                    && !enemy.spec().seekersIgnore()
                    && medium.reaches(weapon, enemy.layer())
                    // M9 part E: a hardened collar's core (the Sentinel's emitter) is a target: the shot aims at it.
                    && (weapon.antiGround()
                            || !enemy.spec().hardened()
                            || enemy.spec().core().isPresent())
                    && inCone(shot, enemy.x(), enemy.y(), best)) {
                best = distanceSquared(shot.x(), shot.y(), enemy.x(), enemy.y());
                nearest = enemy.serial();
                nearestX = enemy.x();
                nearestY = enemy.y();
            }
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            for (int p = 0; p < piece.partCount(); p++) {
                // A high-air boss's open parts are sought all round: the missile climbs to them; M5
                // part E: on the part's own layer (a torpedo's sub and ground, a missile's the rest).
                if (medium.reaches(weapon, piece.partLayer(p))
                        && partTarget(piece, p)
                        && (overhead(piece)
                                ? distanceSquared(shot.x(), shot.y(), targetX, targetY) <= best
                                : inCone(shot, targetX, targetY, best))) {
                    best = distanceSquared(shot.x(), shot.y(), targetX, targetY);
                    nearest = PART_SERIAL + k * SetPieceSpec.MAX_PARTS + p;
                    nearestX = targetX;
                    nearestY = targetY;
                }
            }
        }
        if (nearest < 0 && weapon.delivery() == WeaponSpec.Delivery.HOMING) {
            // M9 part A (QA-1): a seekable missile only when no unit or part is in reach.
            nearest = nearestMissile(shot, best, true);
            if (nearest >= 0) {
                return nearest;
            }
        }
        if (weapon.delivery() == WeaponSpec.Delivery.TORPEDO) {
            for (int j = 0; j < ground.size(); j++) {
                GroundObject object = ground.get(j);
                if (sunkenTrigger(object) && inCone(shot, object.x(), object.y(), best)) {
                    best = distanceSquared(shot.x(), shot.y(), object.x(), object.y());
                    nearest = TRIGGER_SERIAL + object.serial();
                    nearestX = object.x();
                    nearestY = object.y();
                }
            }
        }
        targetX = nearestX;
        targetY = nearestY;
        return nearest;
    }

    /** Whether (x, y) is within {@code best} (squared) of the shot and inside its cone. */
    private static boolean inCone(Shot shot, double x, double y, double best) {
        double dx = x - shot.x();
        double dy = y - shot.y();
        double d = dx * dx + dy * dy;
        if (d > best) {
            return false;
        }
        double off = Math.IEEEremainder(StrictMath.atan2(dx, dy) - shot.heading(), 2 * StrictMath.PI);
        return Math.abs(off) <= shot.weapon().coneHalfAngle();
    }

    /**
     * M9 part A (design/player/weapons/swarm-tail, Rear first): the serial of the nearest enemy behind the ship's nose
     * line (below the row of its centre; above it while the scroll runs reversed) within the shot's seek radius, all
     * round; with none there the nearest anywhere within it; with none at all (QA-1) the nearest seekable missile;
     * -1 for none. Mines and what the seekers ignore are never picked, nor a hardened unit without {@code
     * anti-ground}. The target position is set.
     */
    int rearFirst(Shot shot, Pool<Enemy> enemies) {
        WeaponSpec weapon = shot.weapon();
        Medium medium = fire.medium();
        double reach = shot.range() * shot.range();
        double behind = reach;
        double anywhere = reach;
        int behindSerial = -1;
        int anywhereSerial = -1;
        double behindX = 0;
        double behindY = 0;
        double anywhereX = 0;
        double anywhereY = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (!onField(enemy)
                    || enemy.latched()
                    || enemy.spec().seekersIgnore()
                    || !medium.reaches(weapon, enemy.layer())
                    || (!weapon.antiGround()
                            && enemy.spec().hardened()
                            && enemy.spec().core().isEmpty())) {
                continue;
            }
            double d = distanceSquared(shot.x(), shot.y(), enemy.x(), enemy.y());
            if (d > reach) {
                continue;
            }
            if (d <= anywhere) {
                anywhere = d;
                anywhereSerial = enemy.serial();
                anywhereX = enemy.x();
                anywhereY = enemy.y();
            }
            boolean back = fire.travel() < 0 ? enemy.y() > ship.y() : enemy.y() < ship.y();
            if (back && d <= behind) {
                behind = d;
                behindSerial = enemy.serial();
                behindX = enemy.x();
                behindY = enemy.y();
            }
        }
        if (behindSerial >= 0) {
            targetX = behindX;
            targetY = behindY;
            return behindSerial;
        }
        if (anywhereSerial >= 0) {
            targetX = anywhereX;
            targetY = anywhereY;
            return anywhereSerial;
        }
        return nearestMissile(shot, reach, false);
    }

    /**
     * M9 part A (QA-1): the lock serial of the nearest seekable enemy missile within {@code best} (squared) of the shot
     * (and in its cone when {@code cone}), -1 for none; the target position is set.
     */
    private int nearestMissile(Shot shot, double best, boolean cone) {
        Missiles missiles = fire.missiles();
        if (missiles == null || !fire.medium().reaches(shot.weapon(), Layer.AIR)) {
            return -1;
        }
        int nearest = -1;
        for (int i = 0; i < missiles.size(); i++) {
            Missile missile = missiles.get(i);
            if (!missile.seekable() || !PlayField.overlaps(missile.x(), missile.y(), missile.box())) {
                continue;
            }
            boolean inReach = cone
                    ? inCone(shot, missile.x(), missile.y(), best)
                    : distanceSquared(shot.x(), shot.y(), missile.x(), missile.y()) <= best;
            if (inReach) {
                best = distanceSquared(shot.x(), shot.y(), missile.x(), missile.y());
                nearest = MISSILE_SERIAL + missile.serial();
                targetX = missile.x();
                targetY = missile.y();
            }
        }
        return nearest;
    }

    /**
     * M9 part A: whether the seekable missile with the lock serial {@code target} still flies on the field; it is then
     * at the target position.
     */
    private boolean missile(int target) {
        Missiles missiles = fire.missiles();
        if (missiles == null) {
            return false;
        }
        int serial = target - MISSILE_SERIAL;
        for (int i = 0; i < missiles.size(); i++) {
            Missile missile = missiles.get(i);
            if (missile.serial() == serial) {
                if (!PlayField.overlaps(missile.x(), missile.y(), missile.box())) {
                    return false;
                }
                targetX = missile.x();
                targetY = missile.y();
                return true;
            }
        }
        return false;
    }
}
