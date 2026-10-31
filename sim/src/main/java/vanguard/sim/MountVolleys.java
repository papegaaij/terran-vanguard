package vanguard.sim;

import static vanguard.sim.ShotGeometry.distanceSquared;

import java.util.List;

/**
 * The mounts' volleys (RF9c, out of {@link PlayerFire}): a fitted weapon's muzzles, a drone bay's drone, a lobbed
 * shell's target, the mines out, a wingman's gun, and the water's factors on a shot as it is fired. {@link
 * PlayerFire#fire} decides which mount fires; the shots go into its pool. The medium is read through it.
 */
final class MountVolleys {
    private final PlayerFire fire;
    private final Ship ship;
    private final SimEvents events;
    private final Pool<Shot> shots;
    private final Drones drones;
    private final Homing homing;
    /** {@link PlayerFire}'s turret headings and locked targets (shared, as {@link Homing} shares them). */
    private final double[] turretHeadings;

    private final int[] turretTargets;

    MountVolleys(
            PlayerFire fire,
            Ship ship,
            SimEvents events,
            Pool<Shot> shots,
            Drones drones,
            Homing homing,
            double[] turretHeadings,
            int[] turretTargets) {
        this.fire = fire;
        this.ship = ship;
        this.events = events;
        this.shots = shots;
        this.drones = drones;
        this.homing = homing;
        this.turretHeadings = turretHeadings;
        this.turretTargets = turretTargets;
    }

    /**
     * M7 part A: drone bay {@code m}'s gun fires one shot from its drone, aimed at the convergence point
     * ahead of the ship's centre; M7 part G: a rear-guard drone's straight back.
     */
    void droneVolley(int m, WeaponSpec weapon) {
        double x = drones.x(m);
        double y = drones.y(m);
        Shot shot = shots.obtain();
        if (shot != null) {
            // M7 part G: a rear-guard drone fires straight back (down the screen).
            double heading = drones.spec(m).fireBack()
                    ? StrictMath.PI
                    : StrictMath.atan2(ship.x() - x, ship.y() + drones.spec(m).converge() - y);
            shot.fire(weapon, m, x, y, heading);
            soak(shot);
        }
        events.add(SimEvents.Type.SHOT_FIRED, x, y, m);
    }

    /**
     * M8 part A (design/world/europa, Under water rules; decided D3): the water's factors on a shot just fired, once:
     * its damage × the medium's (an {@code anti-sub} weapon's full), its speed × the bullet speed (a depth charge's
     * toss × the torpedo range too, D12), a torpedo-type weapon's range × the torpedo range, a reach-limited
     * weapon's (the lances') at most its reach of the field's height. Nothing in the air.
     */
    private void soak(Shot shot) {
        if (!fire.medium().wet()) {
            return;
        }
        WeaponSpec weapon = shot.weapon();
        double torpedo = fire.medium().torpedoRange(weapon);
        double reach = fire.medium().reach(weapon);
        shot.soak(
                fire.medium().damage(weapon),
                fire.medium().bulletSpeed() * (weapon.delivery() == WeaponSpec.Delivery.DEPTH_CHARGE ? torpedo : 1),
                torpedo,
                reach < 1 ? reach * PlayField.HEIGHT : Double.POSITIVE_INFINITY);
    }

    void volley(int mount, WeaponSpec weapon, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        List<WeaponSpec.Muzzle> muzzles = weapon.muzzles();
        double sumX = 0;
        double sumY = 0;
        // Indexed: an iterator would allocate on every volley.
        for (int i = 0; i < muzzles.size(); i++) {
            WeaponSpec.Muzzle muzzle = muzzles.get(i);
            double x = ship.x() + muzzle.dx();
            double y = ship.y() + muzzle.dy();
            sumX += x;
            sumY += y;
            Shot shot = shots.obtain();
            if (shot == null) {
                continue;
            }
            switch (weapon.delivery()) {
                case BOLT, HOMING, MINE, TORPEDO, DEPTH_CHARGE -> shot.fire(weapon, mount, x, y, muzzle.angle());
                case TURRET -> {
                    shot.fire(weapon, mount, x, y, turretHeadings[mount]);
                    // Its lock: a high-air boss's part is hit only by a shot aimed at it.
                    shot.lock(turretTargets[mount]);
                }
                case DROPPED -> shot.lob(weapon, mount, x, y, x, y);
                case LOBBED -> lob(shot, weapon, mount, x, y, enemies, ground);
                case BEAM, ARC, CHAIN -> throw new IllegalStateException("a beam or an arc fires no shots");
            }
            soak(shot);
            if (weapon.delivery() == WeaponSpec.Delivery.TORPEDO) {
                // It picks its target as it is fired (in its soaked range); without one it runs straight.
                shot.lock(homing.nearestInCone(shot, enemies, ground));
            }
        }
        events.add(SimEvents.Type.SHOT_FIRED, sumX / muzzles.size(), sumY / muzzles.size(), mount);
    }

    /** How many of mount {@code m}'s mines are out. */
    int liveMines(int m) {
        int live = 0;
        for (int i = 0; i < shots.size(); i++) {
            if (shots.get(i).mount() == m) {
                live++;
            }
        }
        return live;
    }

    /**
     * A shell lands {@code range} ahead of its muzzle, or on the nearest destructible ground target
     * (a ground enemy or a destructible ground object, never a trigger) within the snap radius.
     */
    private void lob(
            Shot shot,
            WeaponSpec weapon,
            int mount,
            double x,
            double y,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground) {
        double landX = x;
        double landY = y + weapon.range();
        double best = weapon.snap() * weapon.snap();
        double snapX = landX;
        double snapY = landY;
        for (int j = 0; j < ground.size(); j++) {
            GroundObject object = ground.get(j);
            double d = distanceSquared(landX, landY, object.x(), object.y());
            if (!object.spec().trigger() && d <= best) {
                best = d;
                snapX = object.x();
                snapY = object.y();
            }
        }
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            double d = distanceSquared(landX, landY, enemy.x(), enemy.y());
            if (enemy.layer() == Layer.GROUND && d <= best) {
                best = d;
                snapX = enemy.x();
                snapY = enemy.y();
            }
        }
        shot.lob(weapon, mount, x, y, snapX, snapY);
    }

    /**
     * A wingman's volley (design/player/wingmen): his gun fires from (x, y) as mount {@code mount}
     * (past the armament's, so its shots and events are told apart); a homing projectile starts
     * locked onto {@code target} (a serial as the homing locks use, -1 for none). He aims a lobbed
     * shell (user decision 2026-10-07): it lands on his ground target when that lies within the
     * lob's range of the muzzle, its flight shortened to the distance; without one it picks its
     * ground target as the player's do.
     */
    void wingmanVolley(
            int mount,
            WeaponSpec weapon,
            double x,
            double y,
            int target,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground) {
        wingmanVolley(mount, weapon, x, y, target, enemies, ground, 0);
    }

    /**
     * As {@link #wingmanVolley(int, WeaponSpec, double, double, int, Pool, Pool)}, its straight shots turned by {@code
     * aim} radians clockwise from straight up (M8 part G: the Bulwark's shell aimed at a Depth Hunter); 0 as its muzzles
     * point.
     */
    void wingmanVolley(
            int mount,
            WeaponSpec weapon,
            double x,
            double y,
            int target,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground,
            double aim) {
        List<WeaponSpec.Muzzle> muzzles = weapon.muzzles();
        double sumX = 0;
        double sumY = 0;
        for (int i = 0; i < muzzles.size(); i++) {
            WeaponSpec.Muzzle muzzle = muzzles.get(i);
            double mx = x + muzzle.dx();
            double my = y + muzzle.dy();
            sumX += mx;
            sumY += my;
            Shot shot = shots.obtain();
            if (shot == null) {
                continue;
            }
            if (weapon.delivery() == WeaponSpec.Delivery.LOBBED) {
                if (homing.located(target, enemies)
                        && distanceSquared(mx, my, homing.targetX(), homing.targetY())
                                <= weapon.range() * weapon.range()) {
                    double share =
                            Math.sqrt(distanceSquared(mx, my, homing.targetX(), homing.targetY())) / weapon.range();
                    shot.lob(weapon, mount, mx, my, homing.targetX(), homing.targetY(), share);
                } else {
                    lob(shot, weapon, mount, mx, my, enemies, ground);
                }
            } else if (weapon.delivery() == WeaponSpec.Delivery.DROPPED) {
                shot.lob(weapon, mount, mx, my, mx, my);
            } else {
                shot.fire(weapon, mount, mx, my, aim == 0 ? muzzle.angle() : muzzle.angle() + aim);
                if (weapon.delivery() == WeaponSpec.Delivery.HOMING) {
                    shot.lock(target);
                }
            }
            // M8 part A: Rook's guns take the water's factors as the ship's do (none of them is anti-sub).
            soak(shot);
        }
        events.add(SimEvents.Type.SHOT_FIRED, sumX / muzzles.size(), sumY / muzzles.size(), mount);
    }
}
