package vanguard.sim;

import static vanguard.sim.ShotGeometry.distanceSquared;
import static vanguard.sim.ShotGeometry.inBlast;
import static vanguard.sim.ShotGeometry.onField;

/**
 * M9 part A, the Tesla Coil Pod's arc (design/player/weapons/tesla-coil-pod; {@link WeaponSpec.Delivery#ARC}), for
 * {@link PlayerFire}: on its fire step each pod picks its target and strikes it at once, no projectile. The pick: the
 * nearest valid target within the weapon's reach of the pod in its forward spread (everything but the wedge straight
 * behind the ship), a <b>shielded</b> unit in reach before any other (the nearest of them), then the other units and a
 * boss's open parts, and only with none of those (QA-1) a seekable enemy missile; never a mine, a unit the seekers
 * ignore, one latched onto the hull, a hardened unit (the arc glances off it: no {@code anti-ground}), nor one on a
 * layer it does not reach ({@code high-air}, {@code sub}). A {@code shield-breaker} arc empties the struck unit's shield
 * and the whole damage goes to the hull ({@link Enemy#damageBreaking}); its sparks deal the splash share to the other
 * units within the spark radius of the struck one (not the struck one). Each pod keeps the point it struck last, for
 * the look. Allocates nothing.
 */
final class ArcFire {
    private final PlayerFire fire;
    private final Ship ship;
    private final SetPiece[] setPieces;
    private final SimEvents events;
    private final PlayerFire.Hits hits;
    /** Per mount: the point its last arc struck (the look's end), on the screen. */
    private final double[] arcX;

    private final double[] arcY;

    ArcFire(PlayerFire fire, Ship ship, SetPiece[] setPieces, SimEvents events, PlayerFire.Hits hits, int mounts) {
        this.fire = fire;
        this.ship = ship;
        this.setPieces = setPieces;
        this.events = events;
        this.hits = hits;
        arcX = new double[mounts];
        arcY = new double[mounts];
    }

    /** The point mount {@code m}'s last arc struck (0, 0 before its first). */
    double arcX(int m) {
        return arcX[m];
    }

    double arcY(int m) {
        return arcY[m];
    }

    /** Back to the level start: no arc struck yet. */
    void reset() {
        java.util.Arrays.fill(arcX, 0);
        java.util.Arrays.fill(arcY, 0);
    }

    /**
     * Mount {@code m}'s pod fires {@code weapon}'s arc if it has a target: the strike, its sparks and the {@link
     * SimEvents.Type#ARC} event; returns whether it fired (without a target it does not).
     */
    boolean strike(int m, WeaponSpec weapon, Pool<Enemy> enemies) {
        WeaponSpec.Muzzle muzzle = weapon.muzzles().getFirst();
        double x = ship.x() + muzzle.dx();
        double y = ship.y() + muzzle.dy();
        Medium medium = fire.medium();
        double reach = weapon.range() * weapon.range();
        double shielded = reach;
        double nearest = reach;
        int shieldedIndex = -1;
        int nearestIndex = -1;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (!onField(enemy)
                    || enemy.spec().seekersIgnore()
                    || (enemy.latched() && !fire.hitsLatched(m))
                    || !medium.reaches(weapon, enemy.layer())
                    || enemy.spec().hardened()
                    || enemy.growing()
                    || !inSpread(x, y, enemy.x(), enemy.y(), weapon)) {
                continue;
            }
            double d = distanceSquared(x, y, enemy.x(), enemy.y());
            if (enemy.shield() > 0 && d <= shielded) {
                shielded = d;
                shieldedIndex = j;
            }
            if (d <= nearest) {
                nearest = d;
                nearestIndex = j;
            }
        }
        int unit = shieldedIndex >= 0 ? shieldedIndex : nearestIndex;
        double damage = weapon.damage() * (medium.wet() ? medium.damage(weapon) : 1);
        int piece = -1;
        int part = -1;
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece candidate = setPieces[k];
            if (!candidate.present()) {
                continue;
            }
            for (int p = 0; p < candidate.partCount(); p++) {
                if (candidate.partWrecked(p)
                        || candidate.partShielded(p)
                        || !candidate.partOnField(p)
                        || !medium.reaches(weapon, candidate.partLayer(p))
                        || !inSpread(x, y, candidate.partX(p), candidate.partY(p), weapon)) {
                    continue;
                }
                double d = distanceSquared(x, y, candidate.partX(p), candidate.partY(p));
                // A shielded unit in reach comes first; otherwise the nearer of the unit and the part.
                if (shieldedIndex < 0 && d < nearest) {
                    nearest = d;
                    piece = k;
                    part = p;
                    unit = -1;
                }
            }
        }
        if (unit >= 0) {
            Enemy enemy = enemies.get(unit);
            int struck = enemy.serial();
            double ex = enemy.x();
            double ey = enemy.y();
            mark(m, ex, ey);
            fire.hitBy(struck);
            // M9 part C: facing armour takes the arc by its direction from the pod (after the shield).
            boolean destroyed = weapon.shieldBreaker()
                    ? enemy.damageBreaking(damage, ex - x, ey - y)
                    : enemy.damageFacing(damage, false, ex - x, ey - y);
            if (destroyed) {
                hits.enemyDestroyed(unit, m);
            }
            sparks(m, weapon, damage, struck, ex, ey, enemies);
            return true;
        }
        if (piece >= 0) {
            SetPiece target = setPieces[piece];
            double px = target.partX(part);
            double py = target.partY(part);
            mark(m, px, py);
            // M9 part H, step H1b: a breaker's arc empties a boss part's shield (the Sovereign's core).
            boolean destroyed = weapon.shieldBreaker()
                    ? target.damagePartBreaking(part, damage, px, py)
                    : target.damagePartAt(part, damage, px, py);
            if (destroyed) {
                hits.partDestroyed(piece, part, m);
            }
            sparks(m, weapon, damage, -1, px, py, enemies);
            return true;
        }
        return missile(m, weapon, damage, x, y, reach);
    }

    /** QA-1: with no unit or part in reach, the nearest seekable missile in reach and spread; returns whether struck. */
    private boolean missile(int m, WeaponSpec weapon, double damage, double x, double y, double reach) {
        Missiles missiles = fire.missiles();
        if (missiles == null || !fire.medium().reaches(weapon, Layer.AIR)) {
            return false;
        }
        int nearest = -1;
        for (int i = 0; i < missiles.size(); i++) {
            Missile missile = missiles.get(i);
            if (!missile.seekable()
                    || !PlayField.overlaps(missile.x(), missile.y(), missile.box())
                    || !inSpread(x, y, missile.x(), missile.y(), weapon)) {
                continue;
            }
            double d = distanceSquared(x, y, missile.x(), missile.y());
            if (d <= reach) {
                reach = d;
                nearest = i;
            }
        }
        if (nearest < 0) {
            return false;
        }
        Missile missile = missiles.get(nearest);
        mark(m, missile.x(), missile.y());
        if (missile.damage(damage)) {
            missiles.destroy(nearest, Missiles.SHOT_DOWN);
        }
        return true;
    }

    /** The arc struck (x, y): kept for the look and reported. */
    private void mark(int m, double x, double y) {
        arcX[m] = x;
        arcY[m] = y;
        events.add(SimEvents.Type.ARC, x, y, m);
        events.add(SimEvents.Type.ENEMY_HIT, x, y, m);
    }

    /**
     * The sparks: the weapon's splash share of {@code damage} on every other unit (not the one with serial {@code
     * struck}) within its spark radius of (x, y), on a layer it reaches; a hardened unit glances them off.
     */
    private void sparks(int m, WeaponSpec weapon, double damage, int struck, double x, double y, Pool<Enemy> enemies) {
        if (!(weapon.splash() > 0) || !(weapon.blast() > 0)) {
            return;
        }
        double spark = damage * weapon.splash();
        Medium medium = fire.medium();
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            if (enemy.serial() == struck
                    || !medium.reaches(weapon, enemy.layer())
                    || !onField(enemy)
                    || enemy.latched()
                    || enemy.spec().hardened()
                    || enemy.growing()
                    || !inBlast(x, y, weapon.blast(), enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            if (enemy.damage(spark)) {
                hits.enemyDestroyed(j, m);
            }
        }
    }

    /**
     * Whether (tx, ty) lies in the pod's forward spread seen from (x, y): within the weapon's half spread either side
     * of straight up the screen (the ship's nose, also while the scroll runs reversed).
     */
    private static boolean inSpread(double x, double y, double tx, double ty, WeaponSpec weapon) {
        double off = StrictMath.atan2(tx - x, ty - y);
        return Math.abs(off) <= weapon.coneHalfAngle();
    }
}
