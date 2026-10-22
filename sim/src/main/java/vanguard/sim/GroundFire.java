package vanguard.sim;

import static vanguard.sim.PlayerFire.groundFactor;
import static vanguard.sim.PlayerFire.inBlast;
import static vanguard.sim.PlayerFire.onField;

import java.util.List;

/**
 * What the {@link PlayerFire}'s shots do below the air and in rock: bolts, missiles and torpedoes hit the
 * ground objects under them, bombs and shells burst where they land, proximity mines burst when an enemy
 * comes close (the {@code area} rule), and (M7 part C) a shot in a canyon's rock stops there, a homing
 * missile bursting on it. It holds no state of its own.
 */
final class GroundFire {
    private final PlayerFire fire;
    private final Pool<Shot> shots;
    private final SimEvents events;
    private final PlayerFire.Hits hits;
    private final SetPiece[] setPieces;

    GroundFire(PlayerFire fire, Pool<Shot> shots, SimEvents events, PlayerFire.Hits hits, SetPiece[] setPieces) {
        this.fire = fire;
        this.shots = shots;
        this.events = events;
        this.hits = hits;
        this.setPieces = setPieces;
    }

    /**
     * M7 part C (design/campaign Level 16, What walls stop; M2 = a), after the hit passes so that a unit at
     * a wall's foot is hit first: a shot in rock stops there with a {@link SimEvents.Type#ROCK_SPARK}
     * (bolts, turret shots, the Rear Lance, the wing, rear, drone and wingman guns); a homing missile
     * detonates there, its blast (if its weapon has one) hitting the ground units and objects in its
     * radius. Bombs and shells pass over rock (they land at the wall's foot, {@link PlayerFire#move}) and mines are
     * kept off it.
     */
    void stopAtRock(Pool<GroundObject> ground, Pool<Enemy> enemies) {
        Canyon canyon = fire.canyon();
        if (canyon == null) {
            return;
        }
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            WeaponSpec.Delivery delivery = weapon.delivery();
            if (delivery.landing() || delivery.laid()) {
                continue;
            }
            Hitbox size = weapon.size();
            if (!canyon.rock(shot.x(), shot.y(), size.width() / 2, size.height() / 2)) {
                continue;
            }
            events.add(SimEvents.Type.ROCK_SPARK, shot.x(), shot.y(), shot.mount());
            if (delivery == WeaponSpec.Delivery.HOMING && weapon.blast() > 0) {
                rockBurst(shot, ground, enemies);
            }
            shots.free(i);
        }
    }

    /** M7 part C: a homing missile's blast on rock: the ground objects and ground units in its radius. */
    private void rockBurst(Shot shot, Pool<GroundObject> ground, Pool<Enemy> enemies) {
        WeaponSpec weapon = shot.weapon();
        double x = shot.x();
        double y = shot.y();
        events.add(SimEvents.Type.BLAST, x, y, shot.mount());
        for (int j = ground.size() - 1; j >= 0; j--) {
            GroundObject object = ground.get(j);
            GroundObjectSpec spec = object.spec();
            if (object.hittable()
                    && !spec.submerged()
                    && (weapon.antiGround() || !spec.hardened())
                    && inBlast(x, y, weapon.blast(), object.x(), object.y(), spec.size())) {
                fire.strike(j, object, shot.damage());
            }
        }
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            if (enemy.layer() != Layer.GROUND
                    || !onField(enemy)
                    || !inBlast(x, y, weapon.blast(), enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            if ((enemy.spec().hardened() && !weapon.antiGround()) || enemy.growing()) {
                events.add(SimEvents.Type.SHOT_GLANCED, enemy.x(), enemy.y(), shot.mount());
            } else {
                blastPart(j, enemy, x, y, weapon.blast(), shot.damage(), shot.mount());
                if (enemy.damage(shot.damage(), true)) {
                    hits.enemyDestroyed(j, shot.mount());
                }
            }
        }
    }

    /**
     * M7 part D: a blast of {@code radius} around (x, y) that reaches enemy {@code j} hurts its standing unit
     * part too when it covers it (the gap check's default: both), before the body.
     */
    private void blastPart(int j, Enemy enemy, double x, double y, double radius, double damage, int mount) {
        if (enemy.partInBlast(x, y, radius) && enemy.damagePart(damage, true)) {
            hits.unitPartDestroyed(j, mount);
        }
    }

    /**
     * Bolts and missiles that missed the air hit the ground objects below (design/enemies, layer
     * rules), torpedoes those they run into, a sunken trigger included (M5 part E: nothing else hits
     * one); bombs and shells that have landed burst, and so do armed mines with an enemy close.
     */
    void hitGround(Pool<GroundObject> ground, Pool<Enemy> enemies, Pool<Mine> spores) {
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (weapon.delivery().landing()) {
                if (shot.airProgress(0) >= 1) {
                    burst(shot, ground, enemies);
                    shots.free(i);
                }
                continue;
            }
            if (weapon.delivery() == WeaponSpec.Delivery.MINE) {
                if (shot.armed() && triggered(shot, enemies)) {
                    mineBurst(shot, ground, enemies, spores);
                    shots.free(i);
                }
                continue;
            }
            if (weapon.delivery() == WeaponSpec.Delivery.DEPTH_CHARGE) {
                if (shot.fused()) {
                    chargeBurst(shot, ground, enemies, spores);
                    shots.free(i);
                }
                continue;
            }
            Medium medium = fire.medium();
            for (int j = ground.size() - 1; j >= 0; j--) {
                GroundObject object = ground.get(j);
                GroundObjectSpec spec = object.spec();
                if (!object.hittable()
                        // M8 part A: a sunken object is hit by what reaches the sub layer (torpedoes, anti-sub
                        // weapons).
                        || (spec.submerged() && !medium.reaches(weapon, Layer.SUB))
                        // M5 part F: a drifting trigger on low-air is above the water a torpedo runs in (M8 part A:
                        // under
                        // water the torpedo reaches it).
                        || (spec.lowAir() && !medium.reaches(weapon, Layer.LOW_AIR))
                        || !weapon.size().overlaps(shot.x(), shot.y(), spec.size(), object.x(), object.y())
                        || (weapon.pierce() > 1 && shot.struck(2 * object.serial() + 1))) {
                    continue;
                }
                if (spec.hardened() && !weapon.antiGround()) {
                    events.add(SimEvents.Type.SHOT_GLANCED, shot.x(), shot.y(), shot.mount());
                    shots.free(i);
                    break;
                }
                events.add(SimEvents.Type.GROUND_HIT, shot.x(), shot.y(), shot.mount());
                boolean spent = shot.pierced();
                fire.strike(j, object, shot.damage() * groundFactor(weapon, Layer.GROUND));
                if (spent) {
                    // M8 part A: a harpoon's splash round the object it struck.
                    fire.splash(shot, -1, enemies);
                    shots.free(i);
                    break;
                }
            }
        }
    }

    /**
     * A landed bomb or shell damages every ground object and ground enemy (on the ground now: a
     * pounce's air window is missed, M5 part E: so is a unit under the water and a sunken trigger)
     * within its blast once; hardened ones only with anti-ground, else a hardened enemy sparks.
     */
    private void burst(Shot shot, Pool<GroundObject> ground, Pool<Enemy> enemies) {
        WeaponSpec weapon = shot.weapon();
        double x = shot.landX();
        double y = shot.landY();
        events.add(SimEvents.Type.BLAST, x, y, shot.mount());
        for (int j = ground.size() - 1; j >= 0; j--) {
            GroundObject object = ground.get(j);
            GroundObjectSpec spec = object.spec();
            if (object.hittable()
                    && !spec.submerged()
                    && !spec.lowAir()
                    && (weapon.antiGround() || !spec.hardened())
                    && inBlast(x, y, weapon.blast(), object.x(), object.y(), spec.size())) {
                fire.strike(j, object, shot.damage());
            }
        }
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            if (enemy.layer() != Layer.GROUND
                    || !onField(enemy)
                    || !inBlast(x, y, weapon.blast(), enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            if ((enemy.spec().hardened() && !weapon.antiGround()) || enemy.growing()) {
                events.add(SimEvents.Type.SHOT_GLANCED, enemy.x(), enemy.y(), shot.mount());
            } else {
                blastPart(j, enemy, x, y, weapon.blast(), shot.damage(), shot.mount());
                if (enemy.damage(shot.damage(), true)) {
                    hits.enemyDestroyed(j, shot.mount());
                }
            }
        }
        // M5 part E: a set piece's part on the ground now (an arena boss's surfaced head, an awash arm).
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            for (int p = 0; p < piece.partCount() && piece.present(); p++) {
                if (piece.partLayer(p) == Layer.GROUND
                        && !piece.partWrecked(p)
                        && !piece.partShielded(p)
                        && piece.partOnField(p)
                        && piece.partInBlast(p, x, y, weapon.blast())
                        && piece.damagePartAt(p, shot.damage(), x, y)) {
                    hits.partDestroyed(k, p, shot.mount());
                }
            }
        }
    }

    /**
     * Whether an enemy on {@code air} or {@code low-air} (a unit, or a set piece's body or living
     * part on one of those layers) is within an armed mine's trigger distance.
     */
    private boolean triggered(Shot mine, Pool<Enemy> enemies) {
        double x = mine.x();
        double y = mine.y();
        double trigger = mine.weapon().mines().trigger();
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            if (triggers(enemy.layer())
                    && onField(enemy)
                    && inBlast(x, y, trigger, enemy.x(), enemy.y(), enemy.hitbox())) {
                return true;
            }
        }
        for (SetPiece piece : setPieces) {
            if (!piece.present() || !triggers(piece.layer())) {
                continue;
            }
            if (PlayField.overlaps(piece.x(), piece.y(), piece.body())
                    && inBlast(x, y, trigger, piece.x(), piece.y(), piece.body())) {
                return true;
            }
            List<PartSpec> parts = piece.spec().parts();
            for (int p = 0; p < parts.size(); p++) {
                double px = piece.partX(p);
                double py = piece.partY(p);
                Hitbox box = parts.get(p).box();
                if (!piece.partWrecked(p) && PlayField.overlaps(px, py, box) && inBlast(x, y, trigger, px, py, box)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The layers whose enemies set off a proximity mine: the player's plane and the one below it. */
    private static boolean triggers(Layer layer) {
        return layer == Layer.AIR || layer == Layer.LOW_AIR;
    }

    /**
     * A mine's blast (the {@code area} rule): every enemy on {@code air}, {@code low-air} and
     * {@code ground}, every ground object but a sunken trigger (hardened ones glance off without
     * {@code anti-ground}),
     * every living set-piece part on those layers that takes damage and every armed spore mine within
     * the blast radius takes the damage once.
     */
    private void mineBurst(Shot mine, Pool<GroundObject> ground, Pool<Enemy> enemies, Pool<Mine> spores) {
        WeaponSpec weapon = mine.weapon();
        double x = mine.x();
        double y = mine.y();
        double radius = weapon.blast();
        events.add(SimEvents.Type.BLAST, x, y, mine.mount());
        for (int j = ground.size() - 1; j >= 0; j--) {
            GroundObject object = ground.get(j);
            GroundObjectSpec spec = object.spec();
            if (object.hittable()
                    && !spec.submerged()
                    && (weapon.antiGround() || !spec.hardened())
                    && inBlast(x, y, radius, object.x(), object.y(), spec.size())) {
                fire.strike(j, object, mine.damage());
            }
        }
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            if (!blastReaches(enemy.layer())
                    || !onField(enemy)
                    || !inBlast(x, y, radius, enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            if ((enemy.spec().hardened() && !weapon.antiGround()) || enemy.growing()) {
                // M5 part C: a hardened unit glances off as a hardened ground object does (M5 part H: and a growth).
                events.add(SimEvents.Type.SHOT_GLANCED, enemy.x(), enemy.y(), mine.mount());
            } else {
                blastPart(j, enemy, x, y, radius, mine.damage(), mine.mount());
                if (enemy.damage(mine.damage(), true)) {
                    hits.enemyDestroyed(j, mine.mount());
                }
            }
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            if (!piece.present()) {
                continue;
            }
            List<PartSpec> parts = piece.spec().parts();
            for (int p = 0; p < parts.size() && piece.present(); p++) {
                // M5 part E: on the part's own layer (an arena boss's part under the water is out of reach).
                if (blastReaches(piece.partLayer(p))
                        && !piece.partWrecked(p)
                        && !piece.partShielded(p)
                        && piece.partOnField(p)
                        && piece.partInBlast(p, x, y, radius)
                        && piece.damagePartAt(p, mine.damage(), x, y)) {
                    hits.partDestroyed(k, p, mine.mount());
                }
            }
        }
        for (int j = spores.size() - 1; j >= 0; j--) {
            Mine spore = spores.get(j);
            if (spore.armed()
                    && inBlast(x, y, radius, spore.x(), spore.y(), EnemyGun.MineSpec.BOX)
                    && spore.damage(mine.damage())) {
                hits.mineDestroyed(j);
            }
        }
    }

    /**
     * M8 part A (design/player/weapons/depth-charges): a depth charge bursts on its fuse: its pressure wave deals its
     * damage once to every target within its blast on a layer {@link Medium#chargeReaches} (under water every layer in
     * the water, never {@code high-air}; above water {@code sub} and the naval {@code ground}): units (their standing
     * unit part too), ground objects (sunken ones included; above water not the drifting low-air ones), set pieces'
     * living parts (once per charge) and, under water, armed spore mines. Hardened units and objects glance off it
     * without {@code anti-ground} (the mines' rule); it never hurts the ship or Rook.
     */
    private void chargeBurst(Shot charge, Pool<GroundObject> ground, Pool<Enemy> enemies, Pool<Mine> spores) {
        Medium medium = fire.medium();
        WeaponSpec weapon = charge.weapon();
        double x = charge.x();
        double y = charge.y();
        double radius = weapon.blast();
        double damage = charge.damage();
        events.add(SimEvents.Type.BLAST, x, y, charge.mount());
        for (int j = ground.size() - 1; j >= 0; j--) {
            GroundObject object = ground.get(j);
            GroundObjectSpec spec = object.spec();
            Layer layer = spec.submerged() ? Layer.SUB : spec.lowAir() ? Layer.LOW_AIR : Layer.GROUND;
            if (object.hittable()
                    && medium.chargeReaches(layer)
                    && (weapon.antiGround() || !spec.hardened())
                    && inBlast(x, y, radius, object.x(), object.y(), spec.size())) {
                fire.strike(j, object, damage);
            }
        }
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            if (!medium.chargeReaches(enemy.layer())
                    || !onField(enemy)
                    || !inBlast(x, y, radius, enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            if ((enemy.spec().hardened() && !weapon.antiGround()) || enemy.growing()) {
                events.add(SimEvents.Type.SHOT_GLANCED, enemy.x(), enemy.y(), charge.mount());
            } else {
                blastPart(j, enemy, x, y, radius, damage, charge.mount());
                if (enemy.damage(damage, true)) {
                    hits.enemyDestroyed(j, charge.mount());
                }
            }
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            for (int p = 0; p < piece.partCount() && piece.present(); p++) {
                if (medium.chargeReaches(piece.partLayer(p))
                        && !piece.partWrecked(p)
                        && !piece.partShielded(p)
                        && piece.partOnField(p)
                        && piece.partInBlast(p, x, y, radius)
                        && piece.damagePartAt(p, damage, x, y)) {
                    hits.partDestroyed(k, p, charge.mount());
                }
            }
        }
        if (medium.chargeReaches(Layer.AIR)) {
            for (int j = spores.size() - 1; j >= 0; j--) {
                Mine spore = spores.get(j);
                if (spore.armed()
                        && inBlast(x, y, radius, spore.x(), spore.y(), EnemyGun.MineSpec.BOX)
                        && spore.damage(damage)) {
                    hits.mineDestroyed(j);
                }
            }
        }
    }

    /**
     * The layers a mine's blast reaches (the {@code area} rule): {@code air}, {@code low-air} and
     * {@code ground}, not {@code high-air} above it nor (M5 part E, user decision E2 = a) {@code sub}
     * under the water.
     */
    private static boolean blastReaches(Layer layer) {
        return layer != Layer.HIGH_AIR && layer != Layer.SUB && layer.touchable();
    }
}
