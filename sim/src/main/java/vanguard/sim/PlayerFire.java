package vanguard.sim;

import java.util.List;

/**
 * The ship's weapons in flight: every {@link Armament.Mount} fires on its own clock while fire is
 * held, an overdrive switches every mount to its overdrive pattern, and the projectiles fly, seek,
 * fall and hit by the layer rules of design/enemies (Layer rules): bolts and homing missiles hit
 * what they pass on the layers their {@link WeaponSpec.Delivery} reaches and the ground objects
 * below; bombs and shells burst on the ground only. Hardened ground targets and (M5 part C)
 * hardened enemies take damage from {@code anti-ground} weapons only; other shots and blasts glance
 * off, and a homing missile does not lock onto a hardened enemy unless it is anti-ground. Every
 * rule reads an enemy's current layer ({@link Enemy#layer()}: a pounce's air window). Armed spore mines on the player's
 * layer are shot like enemies; a set piece's parts take the hits on their layer and its armoured
 * body makes the rest glance; homing missiles lock onto parts too. A boss on {@code high-air} is
 * above the play field: a missile seeks its open parts all round (not only in its cone), climbs to
 * the one it locks onto, turning at {@link #CLIMB_TURN} times its rate, and passes beneath the hull
 * and every other part. A turret (the Swivel Gun) aims its pod at the nearest enemy all round,
 * favouring the flanks and the rear, and fires straight shots its way only while it has a target.
 * Proximity mines hold their screen position, arm, and burst when an enemy on {@code air} or
 * {@code low-air} comes close; the blast hits every {@code air}, {@code low-air} and {@code ground}
 * target within it once (the {@code area} rule). M5 part E: torpedoes run under the water only in a
 * level over water (elsewhere their mount fires nothing), seek the nearest {@code sub} or
 * {@code ground} unit in their cone and strike the first {@code sub} or {@code ground} target they
 * touch; nothing else reaches the {@code sub} layer (homing missiles and turrets do not seek it,
 * blasts and mines pass over it), and a sunken trigger is hit by torpedoes only. What a hit destroys
 * is handed to {@link Hits}, which the {@link Sortie} implements.
 *
 * <p>M7 part A: a {@link WeaponSpec.Delivery#BEAM} mount fires no shots; while fire is held its {@link
 * Beam} runs from the muzzle straight up and {@link #burn} damages the first target in its column every
 * step (see there). A drone bay's gun fires from its {@link Drones drone} at the convergence point, and
 * only while the drone is whole; a Deflector Pod ({@link Deflectors}) fires nothing.
 */
final class PlayerFire {
    private static final int SHOT_CAPACITY = 256;

    /** What the sortie does when a shot destroys something. */
    interface Hits {
        /** The enemy at {@code index} was destroyed (by a special: no mount). */
        default void enemyDestroyed(int index) {
            enemyDestroyed(index, -1);
        }

        /**
         * The enemy at {@code index} was destroyed by a shot or blast of {@code mount} (a fitted
         * weapon's, or {@link Sortie#wingmanMount()} for the wingman's); -1 for none.
         */
        void enemyDestroyed(int index, int mount);

        /** The destructible ground object at {@code index} was destroyed. */
        void groundDestroyed(int index);

        /** The trigger at {@code index} took its last hit and releases its secret. */
        void triggerReleased(int index);

        /** The spore mine at {@code index} was shot. */
        void mineDestroyed(int index);

        /** Part {@code part} of set piece {@code piece} was destroyed (by a special: no mount). */
        default void partDestroyed(int piece, int part) {
            partDestroyed(piece, part, -1);
        }

        /** Part {@code part} of set piece {@code piece} was destroyed by a shot or blast of {@code mount}; -1 for none. */
        void partDestroyed(int piece, int part, int mount);

        /** M7 part D: the unit part of the enemy at {@code index} was destroyed (by a special: no mount). */
        default void unitPartDestroyed(int index) {
            unitPartDestroyed(index, -1);
        }

        /**
         * M7 part D: the unit part of the enemy at {@code index} (design/enemies/ground/shellback, the vent) was
         * destroyed by a shot or blast of {@code mount}; -1 for none. The enemy stays.
         */
        void unitPartDestroyed(int index, int mount);
    }

    /** Homing locks on a set piece's part use serials from here: unit serials stay far below it. */
    static final int PART_SERIAL = 1 << 24;

    /**
     * M5 part E: a torpedo's lock on a sunken trigger (a ground object's serial added), above the
     * parts' serials.
     */
    static final int TRIGGER_SERIAL = 1 << 25;

    /**
     * How many times faster than its rate a missile turns while it climbs to a high-air boss's part:
     * an open sac beside the ship (the Brood Carrier's first pair over a ship under it) lies inside
     * the turning circle of the weapon's own rate.
     */
    static final double CLIMB_TURN = 2;

    /**
     * A turret's flank preference (design/player/weapons/swivel-gun): an enemy outside its forward
     * cone wins when it is no farther than this many times the nearest enemy's distance (20 %).
     */
    static final double FLANK_PREFERENCE = 1.2;

    /** M7 part A: a grown beam's length from its muzzle, px: past the top edge from anywhere on the field. */
    static final double BEAM_REACH = PlayField.HEIGHT;

    /**
     * M7 part A: a beam on a trigger (which counts hits, not damage) counts one hit per this many steps
     * of contact, the first at once: 10 a second, like a fast gun (default, pending the user's review).
     */
    static final int BEAM_TRIGGER_TICKS = 6;

    private final Ship ship;
    private final Armament armament;
    private final SimEvents events;
    private final Hits hits;
    private final SetPiece[] setPieces;
    /** M5 part E: whether the level is over water, where torpedoes run. */
    private final boolean water;

    private final Pool<Shot> shots = new Pool<>(SHOT_CAPACITY, Shot::new, Shot[]::new);
    private final int[] cooldowns;
    private final int[] sinceShot;
    /** A turret mount's heading, radians clockwise from up; its locked target (a serial), -1 for none. */
    private final double[] turretHeadings;

    private final int[] turretTargets;
    /** M7 part A: per mount its beam (a {@link WeaponSpec.Delivery#BEAM} mount's), null for others. */
    private final Beam[] beams;

    private final boolean anyBeam;
    /** M7 part A: the drone bays' drones and the Deflector Pods' fields (empty when none is fitted). */
    private final Drones drones;

    private final Deflectors deflectors;
    private int overdriveTicks;
    /** M7 part C: the level's rock and the scroll now; null in a level without terrain. */
    private Canyon canyon;
    /** M7 part C: the level's tendril gates, which a beam burns on its way past; null without. */
    private Links links;
    /** The enemy (a serial) the player's shots damaged last, for the wingman's first target; -1 for none. */
    private int lastHit;
    /** Steps since {@link #lastHit} was damaged. */
    private int sinceHit;

    /** The homing target found by {@link #locked} or {@link #nearestInCone}. */
    private double targetX;

    private double targetY;
    /** The layer of the target {@link #located} found last. */
    private Layer targetLayer = Layer.AIR;

    /** Over land: a torpedo pod fires nothing. */
    PlayerFire(Ship ship, Armament armament, SimEvents events, Hits hits, SetPiece[] setPieces) {
        this(ship, armament, events, hits, setPieces, false);
    }

    /** @param water whether the level is over water (M5 part E), where torpedoes run */
    PlayerFire(Ship ship, Armament armament, SimEvents events, Hits hits, SetPiece[] setPieces, boolean water) {
        this.ship = ship;
        this.armament = armament;
        this.events = events;
        this.hits = hits;
        this.setPieces = setPieces;
        this.water = water;
        cooldowns = new int[armament.size()];
        sinceShot = new int[armament.size()];
        turretHeadings = new double[armament.size()];
        turretTargets = new int[armament.size()];
        beams = new Beam[armament.size()];
        boolean beam = false;
        for (int m = 0; m < armament.size(); m++) {
            if (armament.mount(m).fires() && armament.mount(m).weapon().delivery() == WeaponSpec.Delivery.BEAM) {
                beams[m] = new Beam();
                beam = true;
            }
        }
        anyBeam = beam;
        drones = new Drones(ship, armament);
        deflectors = new Deflectors(ship, armament);
        reset();
    }

    /**
     * M7 part C (design/campaign Level 16, What walls stop): the level's rock, which stops shots and the
     * beam, keeps mines on the flyable side and lands bombs and shells at the wall's foot.
     */
    void canyon(Canyon rock) {
        canyon = rock;
    }

    /** M7 part C: the level's tendril gates (design/enemies/ground/tendril-anchor); null without. */
    void links(Links gates) {
        links = gates;
    }

    /** Back to the level start: no shots, every weapon ready, no overdrive. */
    void reset() {
        Pools.clear(shots);
        for (int m = 0; m < armament.size(); m++) {
            cooldowns[m] = 0;
            sinceShot[m] = Integer.MAX_VALUE;
            turretHeadings[m] = armament.mount(m).fires()
                    ? armament.mount(m).weapon().muzzles().getFirst().angle()
                    : 0;
            turretTargets[m] = -1;
            if (beams[m] != null) {
                beams[m].reset();
            }
        }
        drones.reset();
        deflectors.reset();
        overdriveTicks = 0;
        lastHit = -1;
        sinceHit = Integer.MAX_VALUE;
    }

    /** Starts an overdrive of {@code ticks} steps; a running one starts over. */
    void overdrive(int ticks) {
        overdriveTicks = ticks;
    }

    /**
     * One step of the guns while the ship flies: the overdrive runs down, and every mount whose
     * weapon is ready fires a volley if fire is held. A lobbed shell picks its ground target from
     * {@code enemies} and {@code ground} as it leaves.
     */
    void fire(int commands, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        if (overdriveTicks > 0 && --overdriveTicks == 0) {
            events.add(SimEvents.Type.OVERDRIVE_ENDED, ship.x(), ship.y());
        }
        boolean held = Command.FIRE.in(commands);
        for (int m = 0; m < armament.size(); m++) {
            if (sinceShot[m] < Integer.MAX_VALUE) {
                sinceShot[m]++;
            }
            if (cooldowns[m] > 0) {
                cooldowns[m]--;
            }
            Armament.Mount mount = armament.mount(m);
            if (!mount.fires()) {
                // M7 part A: a Deflector Pod fires nothing.
                continue;
            }
            WeaponSpec weapon = overdriveTicks > 0 ? mount.overdrive() : mount.weapon();
            if (beams[m] != null) {
                beam(m, weapon, held);
                continue;
            }
            if (weapon.delivery() == WeaponSpec.Delivery.TURRET) {
                aim(m, weapon, enemies);
            }
            if (cooldowns[m] > 0
                    || !held
                    || idle(weapon)
                    // M7 part A: a broken (or fading-in) drone does not fire.
                    || (mount.bay() && !drones.whole(m))
                    || (weapon.delivery() == WeaponSpec.Delivery.TURRET && turretTargets[m] < 0)
                    || (weapon.delivery() == WeaponSpec.Delivery.MINE
                            && liveMines(m) >= weapon.mines().maxLive())) {
                // A turret without a target holds fire; at its mine cap a mount waits for one to go;
                // a torpedo pod over land fires nothing (M5 part E).
                continue;
            }
            cooldowns[m] = weapon.intervalTicks();
            sinceShot[m] = 0;
            if (mount.bay()) {
                droneVolley(m, weapon);
            } else {
                volley(m, weapon, enemies, ground);
            }
        }
    }

    /**
     * M7 part A: beam mount {@code m} this step: on while fire is held ({@link SimEvents.Type#BEAM_ON}
     * as it comes on), off otherwise ({@link SimEvents.Type#BEAM_OFF}); its muzzle glows while it runs.
     */
    private void beam(int m, WeaponSpec weapon, boolean held) {
        Beam beam = beams[m];
        WeaponSpec.Muzzle muzzle = weapon.muzzles().getFirst();
        beam.x = ship.x() + muzzle.dx();
        beam.fromY = ship.y() + muzzle.dy();
        beam.width = weapon.size().width();
        if (!held) {
            ceaseBeam(m);
            return;
        }
        if (!beam.on) {
            beam.on = true;
            beam.ticksOn = 0;
            events.add(SimEvents.Type.BEAM_ON, beam.x, beam.fromY, m);
        }
        beam.ticksOn++;
        sinceShot[m] = 0;
    }

    /** M7 part A: every beam goes off (the ship does not fly: its launch, its wreck, the level's end). */
    void ceaseBeams() {
        if (!anyBeam) {
            return;
        }
        for (int m = 0; m < beams.length; m++) {
            if (beams[m] != null) {
                ceaseBeam(m);
            }
        }
    }

    private void ceaseBeam(int m) {
        Beam beam = beams[m];
        if (beam.on) {
            events.add(SimEvents.Type.BEAM_OFF, beam.x, beam.fromY, m);
        }
        beam.reset();
    }

    /**
     * M7 part A: drone bay {@code m}'s gun fires one shot from its drone, aimed at the convergence point
     * ahead of the ship's centre.
     */
    private void droneVolley(int m, WeaponSpec weapon) {
        double x = drones.x(m);
        double y = drones.y(m);
        Shot shot = shots.obtain();
        if (shot != null) {
            double heading =
                    StrictMath.atan2(ship.x() - x, ship.y() + drones.spec(m).converge() - y);
            shot.fire(weapon, m, x, y, heading);
        }
        events.add(SimEvents.Type.SHOT_FIRED, x, y, m);
    }

    /**
     * M7 part A: one step of the drones (their orbit, rebuild and fade-in) and the Deflector Pods (their
     * charges), with the overdrive's numbers while one runs; nothing without them.
     */
    void tend() {
        drones.update(overdriveTicks > 0, events);
        deflectors.update(overdriveTicks > 0, events);
    }

    /**
     * M5 part E (user decision E1 = a): whether {@code weapon} fires nothing in this level: a
     * torpedo over land (its pod still draws its power; the HUD reads NO WATER).
     */
    boolean idle(WeaponSpec weapon) {
        return weapon != null && weapon.delivery() == WeaponSpec.Delivery.TORPEDO && !water;
    }

    private void volley(int mount, WeaponSpec weapon, Pool<Enemy> enemies, Pool<GroundObject> ground) {
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
                case BOLT, HOMING, MINE -> shot.fire(weapon, mount, x, y, muzzle.angle());
                case TORPEDO -> {
                    // It picks its target as it is fired; without one it runs straight.
                    shot.fire(weapon, mount, x, y, muzzle.angle());
                    shot.lock(nearestInCone(shot, enemies, ground));
                }
                case TURRET -> {
                    shot.fire(weapon, mount, x, y, turretHeadings[mount]);
                    // Its lock: a high-air boss's part is hit only by a shot aimed at it.
                    shot.lock(turretTargets[mount]);
                }
                case DROPPED -> shot.lob(weapon, mount, x, y, x, y);
                case LOBBED -> lob(shot, weapon, mount, x, y, enemies, ground);
            }
        }
        events.add(SimEvents.Type.SHOT_FIRED, sumX / muzzles.size(), sumY / muzzles.size(), mount);
    }

    /** How many of mount {@code m}'s mines are out. */
    private int liveMines(int m) {
        int live = 0;
        for (int i = 0; i < shots.size(); i++) {
            if (shots.get(i).mount() == m) {
                live++;
            }
        }
        return live;
    }

    /**
     * Turns mount {@code m}'s turret by at most its slew rate for one step: towards the nearest
     * enemy (or set-piece part) within its reach all round, where one outside its forward cone wins
     * when it is no farther than {@link #FLANK_PREFERENCE} times the nearest; back to forward
     * without one.
     */
    private void aim(int m, WeaponSpec weapon, Pool<Enemy> enemies) {
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
            // a turret's shots pass unless its mount may hit it.
            if (!onField(enemy) || !weapon.delivery().reaches(enemy.layer()) || (enemy.latched() && !hitsLatched(m))) {
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
                if (!weapon.delivery().reaches(piece.partLayer(p)) || !partTarget(piece, p)) {
                    continue;
                }
                double d = distanceSquared(x, y, targetX, targetY);
                int serial = PART_SERIAL + k * LevelScript.SetPieceSpec.MAX_PARTS + p;
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
     * Moves every projectile by one step: bolts fly on until their range is flown or they leave
     * the screen, homing missiles and torpedoes steer at their target first, bombs and shells fall
     * towards their landing point, which scrolls down with the ground by {@code groundScroll}. A
     * torpedo also seeks a sunken trigger among the {@code ground} objects (M5 part E).
     */
    void move(double groundScroll, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        if (sinceHit < Integer.MAX_VALUE) {
            sinceHit++;
        }
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (weapon.delivery().landing()) {
                shot.fall(groundScroll);
                if (canyon != null) {
                    // M7 part C: over rock it lands (and bursts) at the wall's foot instead.
                    shot.landAt(canyon.flyableX(shot.landY(), shot.landX(), 0));
                }
                continue;
            }
            if (weapon.delivery() == WeaponSpec.Delivery.MINE) {
                // A mine holds its place on the screen (it does not scroll) until it bursts or fizzles.
                shot.drift();
                if (canyon != null) {
                    // M7 part C: laid and pushed on the flyable side only; against a wall it stays at its edge.
                    shot.placeX(
                            canyon.flyableX(shot.y(), shot.x(), weapon.size().width() / 2));
                }
                if (shot.spent() || !PlayField.overlaps(shot.x(), shot.y(), weapon.size())) {
                    shots.free(i);
                } else if (shot.armsNow()) {
                    events.add(SimEvents.Type.PROXIMITY_MINE_ARMED, shot.x(), shot.y(), shot.mount());
                }
                continue;
            }
            if (weapon.delivery().seeks()) {
                home(shot, enemies, ground);
            }
            shot.move();
            if (shot.spent() || !PlayField.overlaps(shot.x(), shot.y(), weapon.size())) {
                shots.free(i);
            }
        }
    }

    /**
     * Steers at the locked target, or locks onto the nearest enemy (or set-piece part) in range and
     * in the cone on a layer it reaches when it has none. A torpedo (M5 part E) picks its target as
     * it is fired and again only when that target dies (or leaves its reach), a sunken trigger among
     * them; without one it runs straight.
     */
    private void home(Shot shot, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        boolean found = locked(shot, enemies, ground);
        if (!found && shot.weapon().delivery() == WeaponSpec.Delivery.TORPEDO && shot.target() < 0) {
            return;
        }
        if (!found) {
            int target = nearestInCone(shot, enemies, ground);
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
    private int overhead(int target) {
        if (target < PART_SERIAL) {
            return -1;
        }
        int piece = (target - PART_SERIAL) / LevelScript.SetPieceSpec.MAX_PARTS;
        return piece < setPieces.length && overhead(setPieces[piece]) ? piece : -1;
    }

    /** Whether {@code piece} is a boss above the play field: missiles climb to the part they lock onto. */
    private static boolean overhead(SetPiece piece) {
        return piece.layer() == Layer.HIGH_AIR && piece.boss().isPresent();
    }

    /**
     * Whether the shot's locked target is still on the field, on a layer the shot reaches (M5 part
     * E: a unit that dives is lost to a missile); it is then at the target position.
     */
    private boolean locked(Shot shot, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        if (shot.target() >= TRIGGER_SERIAL) {
            return trigger(shot.target() - TRIGGER_SERIAL, ground);
        }
        return located(shot.target(), enemies) && shot.weapon().delivery().reaches(targetLayer);
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
        LevelScript.GroundObjectSpec spec = object.spec();
        return spec.trigger()
                && spec.submerged()
                && object.hittable()
                && PlayField.overlaps(object.x(), object.y(), spec.size());
    }

    /**
     * Whether {@code target} (a serial as the homing locks use, -1 for none) is on the field; it is
     * then at the target position, on the target layer.
     */
    private boolean located(int target, Pool<Enemy> enemies) {
        if (target < 0) {
            return false;
        }
        if (target >= PART_SERIAL) {
            int piece = (target - PART_SERIAL) / LevelScript.SetPieceSpec.MAX_PARTS;
            int part = (target - PART_SERIAL) % LevelScript.SetPieceSpec.MAX_PARTS;
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
    private int nearestInCone(Shot shot, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        WeaponSpec weapon = shot.weapon();
        double best = weapon.range() * weapon.range();
        int nearest = -1;
        double nearestX = 0;
        double nearestY = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            // M5 part C: a hardened unit is no target unless the weapon is anti-ground; M5 part E:
            // only one on a layer the shot reaches (a torpedo's sub and ground, a missile's the rest).
            if (onField(enemy)
                    && !enemy.latched()
                    && weapon.delivery().reaches(enemy.layer())
                    && (weapon.antiGround() || !enemy.spec().hardened())
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
                if (weapon.delivery().reaches(piece.partLayer(p))
                        && partTarget(piece, p)
                        && (overhead(piece)
                                ? distanceSquared(shot.x(), shot.y(), targetX, targetY) <= best
                                : inCone(shot, targetX, targetY, best))) {
                    best = distanceSquared(shot.x(), shot.y(), targetX, targetY);
                    nearest = PART_SERIAL + k * LevelScript.SetPieceSpec.MAX_PARTS + p;
                    nearestX = targetX;
                    nearestY = targetY;
                }
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

    /** M7 part A: what the beam cast found nearest along the column (see {@link #burn}). */
    private static final int CAST_NONE = 0;

    private static final int CAST_ENEMY = 1;
    private static final int CAST_PART = 2;
    private static final int CAST_SPORE = 3;
    private static final int CAST_GROUND = 4;
    private static final int CAST_BLOCKED = 5;
    /**
     * Round 37 R.4 (c): a set piece's armour (a part that takes no damage now, its body or neck): it stops
     * the beam on every layer, the ground's included (a boss is not hardened ground); stored as {@link
     * #CAST_BLOCKED}.
     */
    private static final int CAST_ARMOUR = 6;

    /** M7 part A: the beam cast's nearest find: its kind, height, layer, index (and part) so far. */
    private int castKind;

    private double castY;
    private Layer castLayer;
    private int castIndex;
    private int castPart;
    /** M7 part A: the nearest glance crossing the cast passed over. */
    private double castGlance;

    /**
     * M7 part A (design/player/weapons/ion-beam, Beam rules): every beam that is on casts its column
     * (its width, from its muzzle straight up to its reach, which grows over {@link #Beam.GROW_TICKS}
     * after it comes on) across every hit path at once, without allocating: enemies (segment chains
     * and swarm members among them), set pieces' parts (an armoured body or neck stops it unless a
     * living part of that piece is in the column: it burns over the back to it), armed spore mines and
     * ground objects. The nearest touch wins, a tie the higher layer. What it can damage takes the
     * weapon's damage for this step (a part times its weak-point multiplier; a trigger counts a hit
     * every {@link #BEAM_TRIGGER_TICKS} steps) and its kill pays as a shot's. On the ground layer what
     * it cannot damage (hardened ground objects and units without {@code anti-ground}, a walker's
     * frontal armour, a growth still growing) does not stop it: it passes over with a glance ({@link
     * Beam#glanceY()}); above the ground such a thing stops it without damage ({@link
     * Beam.Contact#BLOCKED}). Round 37 R.4 (c): a set piece's armour stops it on every layer, the ground's
     * included (the Siege Spire's trunk, its maw and core before their phases), and so does a high-air
     * boss's body (the Brood Carrier's hull in its first phase): the beam reaches high air, so it is not
     * beneath the hull as a bolt is. Units under the water, a Lamprey latched onto the
     * hull (front shots pass it), allies, pickups and enemy bullets do not stop it.
     *
     * <p>M7 part C: a canyon wall or rock island in the column ends it there ({@link #wallAhead}, {@link
     * Beam.Contact#ROCK}); a gate's tendril it crosses short of its end burns, and it goes on past it ({@link
     * Links#burn}; M9 = a). M7 part D: a Herald's aura ring absorbs its damage in {@link Enemy#damage}.
     */
    void burn(Pool<Enemy> enemies, Pool<GroundObject> ground, Pool<Mine> spores) {
        if (!anyBeam) {
            return;
        }
        for (int m = 0; m < beams.length; m++) {
            Beam beam = beams[m];
            if (beam == null || !beam.on) {
                continue;
            }
            WeaponSpec weapon = overdriveTicks > 0
                    ? armament.mount(m).overdrive()
                    : armament.mount(m).weapon();
            double half = weapon.size().width() / 2;
            double reach = beam.fromY + BEAM_REACH * Math.min(1, (double) beam.ticksOn / Beam.GROW_TICKS);
            double limit = Math.min(reach, wallAhead(beam.x, half, beam.fromY));
            cast(m, weapon, beam.x, half, beam.fromY, limit, enemies, ground, spores);
            if (links != null) {
                // M7 part C (M9 = a): the tendrils it crosses short of its end burn, and it goes on past them.
                links.burn(beam.x, half, beam.fromY, castKind == CAST_NONE ? limit : castY, weapon.damage());
            }
            beam.glanceY = castGlance < castY ? castGlance : Double.NaN;
            beam.target = -1;
            if (castKind == CAST_NONE) {
                // M7 part C: ended by rock short of its reach: the rock-scorch impact.
                beam.contact = limit < reach ? Beam.Contact.ROCK : Beam.Contact.NONE;
                beam.endY = limit;
                beam.trigger = -1;
                beam.triggerTicks = 0;
                continue;
            }
            beam.endY = castY + Beam.BORE;
            beam.contact = castKind == CAST_BLOCKED ? Beam.Contact.BLOCKED : Beam.Contact.BURN;
            int trigger = -1;
            double damage = weapon.damage() * groundFactor(weapon, castLayer);
            switch (castKind) {
                case CAST_ENEMY -> {
                    Enemy enemy = enemies.get(castIndex);
                    beam.target = enemy.serial();
                    lastHit = enemy.serial();
                    sinceHit = 0;
                    if (castPart >= 0) {
                        // M7 part D: its unit part lay nearer in the column (the Shellback's vent).
                        if (enemy.damagePart(damage, false)) {
                            hits.unitPartDestroyed(castIndex, m);
                        }
                    } else if (enemy.damage(damage)) {
                        hits.enemyDestroyed(castIndex, m);
                    }
                }
                case CAST_PART -> {
                    SetPiece piece = setPieces[castIndex];
                    beam.target = PART_SERIAL + castIndex * LevelScript.SetPieceSpec.MAX_PARTS + castPart;
                    if (piece.damagePartAt(castPart, damage, beam.x, castY, 0, 1)) {
                        hits.partDestroyed(castIndex, castPart, m);
                    }
                }
                case CAST_SPORE -> {
                    if (spores.get(castIndex).damage(damage)) {
                        hits.mineDestroyed(castIndex);
                    }
                }
                case CAST_GROUND -> {
                    GroundObject object = ground.get(castIndex);
                    if (object.spec().trigger()) {
                        trigger = object.serial();
                        beam.triggerTicks = beam.trigger == trigger ? beam.triggerTicks + 1 : 0;
                        if (beam.triggerTicks % BEAM_TRIGGER_TICKS == 0) {
                            strike(castIndex, object, damage);
                        }
                    } else {
                        strike(castIndex, object, damage);
                    }
                }
                default -> {
                    // CAST_BLOCKED: it stops at the edge without damage.
                }
            }
            beam.trigger = trigger;
            if (trigger < 0) {
                beam.triggerTicks = 0;
            }
        }
    }

    /**
     * M7 part A, a hook for part C (canyon walls and rock islands, M2 = a): the height at which a wall
     * in the column from {@code fromY} up stops the beam; none yet.
     */
    private double wallAhead(double x, double halfWidth, double fromY) {
        // M7 part C: the first rock in the column (a wall's or an island's), +∞ without terrain.
        return canyon == null ? Double.POSITIVE_INFINITY : canyon.terrain.ceiling(canyon.scroll(), x, halfWidth, fromY);
    }

    /**
     * M7 part C (design/campaign Level 16, What walls stop; M2 = a), after the hit passes so that a unit at
     * a wall's foot is hit first: a shot in rock stops there with a {@link SimEvents.Type#ROCK_SPARK}
     * (bolts, turret shots, the Rear Lance, the wing, rear, drone and wingman guns); a homing missile
     * detonates there, its blast (if its weapon has one) hitting the ground units and objects in its
     * radius. Bombs and shells pass over rock (they land at the wall's foot, {@link #move}) and mines are
     * kept off it.
     */
    void stopAtRock(Pool<GroundObject> ground, Pool<Enemy> enemies) {
        if (canyon == null) {
            return;
        }
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            WeaponSpec.Delivery delivery = weapon.delivery();
            if (delivery.landing() || delivery == WeaponSpec.Delivery.MINE) {
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
            LevelScript.GroundObjectSpec spec = object.spec();
            if (object.hittable()
                    && !spec.submerged()
                    && (weapon.antiGround() || !spec.hardened())
                    && inBlast(x, y, weapon.blast(), object.x(), object.y(), spec.size())) {
                strike(j, object, shot.damage());
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

    /** The beam cast for mount {@code m}: sets the {@code cast*} fields to the nearest find below {@code limit}. */
    private void cast(
            int m,
            WeaponSpec weapon,
            double x,
            double half,
            double fromY,
            double limit,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground,
            Pool<Mine> spores) {
        castKind = CAST_NONE;
        castY = limit;
        castLayer = null;
        castIndex = -1;
        castPart = -1;
        castGlance = Double.POSITIVE_INFINITY;
        WeaponSpec.Delivery delivery = weapon.delivery();
        for (int j = 0; j < enemies.size(); j++) {
            Enemy enemy = enemies.get(j);
            Layer layer = enemy.layer();
            if (!delivery.reaches(layer) || !onField(enemy) || (enemy.latched() && !hitsLatched(m))) {
                continue;
            }
            double y = columnContact(x, half, fromY, enemy.x(), enemy.y(), enemy.hitbox());
            boolean damageable =
                    (weapon.antiGround() || !enemy.spec().hardened()) && !enemy.glances(0, 1) && !enemy.growing();
            if (enemy.partAlive()) {
                // M7 part D: a unit part first, so a tie goes to it (the Shellback's vent).
                double partY = columnContact(x, half, fromY, enemy.partX(), enemy.partY(), enemy.partBox());
                consider(damageable ? CAST_ENEMY : CAST_BLOCKED, partY, layer, j, 0);
            }
            consider(damageable ? CAST_ENEMY : CAST_BLOCKED, y, layer, j, -1);
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            boolean anchored = piece.arena().isPresent();
            if (!piece.present() || (!anchored && !delivery.reaches(piece.layer()))) {
                continue;
            }
            boolean partInColumn = false;
            for (int p = 0; p < piece.partCount(); p++) {
                Layer partLayer = piece.partLayer(p);
                if (piece.partWrecked(p) || !piece.partOnField(p) || (anchored && !delivery.reaches(partLayer))) {
                    continue;
                }
                double y = piece.partColumn(p, x, half, fromY);
                if (y < Double.POSITIVE_INFINITY) {
                    partInColumn = true;
                }
                consider(piece.partShielded(p) ? CAST_ARMOUR : CAST_PART, y, partLayer, k, p);
            }
            if (anchored || partInColumn) {
                // An arena boss's platform is scenery; over the armoured back (a high-air boss's hull too,
                // round 37 R.4 c) the beam burns on to a part in its column.
                continue;
            }
            if (PlayField.overlaps(piece.x(), piece.y(), piece.body())) {
                consider(
                        CAST_ARMOUR,
                        columnContact(x, half, fromY, piece.x(), piece.y(), piece.body()),
                        piece.layer(),
                        k,
                        -1);
            }
            consider(CAST_ARMOUR, piece.neckColumn(x, half, fromY), piece.layer(), k, -1);
        }
        if (delivery.reaches(Layer.AIR)) {
            for (int j = 0; j < spores.size(); j++) {
                Mine spore = spores.get(j);
                if (spore.armed()) {
                    consider(
                            CAST_SPORE,
                            columnContact(x, half, fromY, spore.x(), spore.y(), EnemyGun.MineSpec.BOX),
                            Layer.AIR,
                            j,
                            -1);
                }
            }
        }
        for (int j = 0; j < ground.size(); j++) {
            GroundObject object = ground.get(j);
            LevelScript.GroundObjectSpec spec = object.spec();
            if (!object.hittable() || spec.submerged()) {
                continue;
            }
            double y = columnContact(x, half, fromY, object.x(), object.y(), spec.size());
            boolean damageable = weapon.antiGround() || !spec.hardened();
            consider(damageable ? CAST_GROUND : CAST_BLOCKED, y, spec.lowAir() ? Layer.LOW_AIR : Layer.GROUND, j, -1);
        }
    }

    /**
     * M7 part A: a find of {@code kind} touched at height {@code y} on {@code layer}: nearer than the
     * cast's find so far (a tie on a higher layer) it becomes the find; something it cannot damage on
     * the ground layer is passed over instead (a glance crossing), a set piece's armour ({@link
     * #CAST_ARMOUR}) excepted.
     */
    private void consider(int kind, double y, Layer layer, int index, int part) {
        boolean nearer = y < castY;
        boolean tie = y == castY && castKind != CAST_NONE && layer.ordinal() > castLayer.ordinal();
        if (!nearer && !tie) {
            return;
        }
        if (kind == CAST_BLOCKED && layer == Layer.GROUND) {
            castGlance = Math.min(castGlance, y);
            return;
        }
        castKind = kind == CAST_ARMOUR ? CAST_BLOCKED : kind;
        castY = y;
        castLayer = layer;
        castIndex = index;
        castPart = part;
    }

    /**
     * M7 part A: where a beam's column centred on {@code cx}, {@code halfWidth} either side and running
     * up from {@code fromY}, first touches a box of {@code box} around (bx, by): its bottom edge, or the
     * column's start when the box reaches below it; {@link Double#POSITIVE_INFINITY} when the column
     * misses it (touching edges do not count, as for {@link Hitbox#overlaps}).
     */
    static double columnContact(double cx, double halfWidth, double fromY, double bx, double by, Hitbox box) {
        if (Math.abs(bx - cx) * 2 >= box.width() + 2 * halfWidth || by + box.height() / 2 <= fromY) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.max(fromY, by - box.height() / 2);
    }

    /** Bolts and missiles hit the enemies they pass on the layers they reach. */
    void hitEnemies(Pool<Enemy> enemies) {
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            WeaponSpec weapon = shot.weapon();
            if (weapon.delivery().landing() || weapon.delivery() == WeaponSpec.Delivery.MINE) {
                continue;
            }
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                Layer layer = enemy.layer();
                if (!weapon.delivery().reaches(layer)
                        || !onField(enemy)
                        || (enemy.latched() && !hitsLatched(shot.mount()))) {
                    continue;
                }
                // M7 part D: a unit part is tested before the body (the Shellback's vent).
                boolean onPart = enemy.partAlive()
                        && weapon.size().overlaps(shot.x(), shot.y(), enemy.partBox(), enemy.partX(), enemy.partY());
                if ((!onPart && !weapon.size().overlaps(shot.x(), shot.y(), enemy.hitbox(), enemy.x(), enemy.y()))
                        || (weapon.pierce() > 1 && shot.struck(2 * enemy.serial()))) {
                    continue;
                }
                if ((enemy.spec().hardened() && !weapon.antiGround())
                        || enemy.glances(shot.vx(), shot.vy())
                        || enemy.growing()) {
                    // A hardened unit (M5 part C) without anti-ground, or a walker's frontal armour
                    // for a direct shot from ahead, or (M5 part H) a root burst's growth still growing: it sparks off.
                    events.add(SimEvents.Type.SHOT_GLANCED, shot.x(), shot.y(), shot.mount());
                    shots.free(i);
                    break;
                }
                events.add(SimEvents.Type.ENEMY_HIT, shot.x(), shot.y(), shot.mount());
                if (shot.mount() < armament.size()) {
                    lastHit = enemy.serial();
                    sinceHit = 0;
                }
                boolean spent = shot.pierced();
                if (onPart) {
                    if (enemy.damagePart(shot.damage() * groundFactor(weapon, layer), false)) {
                        hits.unitPartDestroyed(j, shot.mount());
                    }
                } else if (enemy.damage(shot.damage() * groundFactor(weapon, layer))) {
                    hits.enemyDestroyed(j, shot.mount());
                }
                if (spent) {
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
    private boolean hitsLatched(int mount) {
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
            if (weapon.delivery().landing() || !weapon.delivery().reaches(Layer.AIR)) {
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
            List<LevelScript.PartSpec> parts = piece.spec().parts();
            boolean overhead = overhead(piece);
            boolean anchored = piece.arena().isPresent();
            for (int i = shots.size() - 1; i >= 0; i--) {
                Shot shot = shots.get(i);
                WeaponSpec weapon = shot.weapon();
                if (weapon.delivery().landing()
                        || (!anchored && !weapon.delivery().reaches(piece.layer()))
                        || (overhead && overhead(shot.target()) != k)) {
                    continue;
                }
                int lockedPart = overhead ? (shot.target() - PART_SERIAL) % LevelScript.SetPieceSpec.MAX_PARTS : -1;
                boolean gone = false;
                for (int p = 0; p < parts.size() && piece.present(); p++) {
                    if (overhead && p != lockedPart) {
                        continue;
                    }
                    Layer partLayer = piece.partLayer(p);
                    if (piece.partWrecked(p)
                            || (anchored && !weapon.delivery().reaches(partLayer))
                            || !piece.partTouches(p, weapon.size(), shot.x(), shot.y())
                            || !piece.partOnField(p)
                            || (weapon.pierce() > 1 && shot.struck(-1 - k * LevelScript.SetPieceSpec.MAX_PARTS - p))) {
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
     * Whether a living part on the field lies ahead of the shot in its line of flight: a shot over
     * the armoured body flies on over the unit's back towards it (the vents and the blowhole sit on
     * the body); a shot with nothing ahead of it glances off the armour.
     */
    private static boolean partAhead(SetPiece piece, Shot shot) {
        double dirX = Trig.sin(shot.heading());
        double dirY = Trig.cos(shot.heading());
        List<LevelScript.PartSpec> parts = piece.spec().parts();
        for (int p = 0; p < parts.size(); p++) {
            Hitbox box = parts.get(p).box();
            double px = piece.partX(p);
            double py = piece.partY(p);
            if (piece.partWrecked(p) || !PlayField.overlaps(px, py, box)) {
                continue;
            }
            double relX = px - shot.x();
            double relY = py - shot.y();
            double reach =
                    (Math.max(box.width(), box.height()) + shot.weapon().size().width()) / 2;
            if (relX * dirX + relY * dirY > 0 && Math.abs(relX * dirY - relY * dirX) < reach) {
                return true;
            }
        }
        return false;
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
            for (int j = ground.size() - 1; j >= 0; j--) {
                GroundObject object = ground.get(j);
                LevelScript.GroundObjectSpec spec = object.spec();
                if (!object.hittable()
                        || (spec.submerged() && weapon.delivery() != WeaponSpec.Delivery.TORPEDO)
                        // M5 part F: a drifting trigger on low-air is above the water a torpedo runs in.
                        || (spec.lowAir() && weapon.delivery() == WeaponSpec.Delivery.TORPEDO)
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
                strike(j, object, shot.damage() * groundFactor(weapon, Layer.GROUND));
                if (spent) {
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
            LevelScript.GroundObjectSpec spec = object.spec();
            if (object.hittable()
                    && !spec.submerged()
                    && !spec.lowAir()
                    && (weapon.antiGround() || !spec.hardened())
                    && inBlast(x, y, weapon.blast(), object.x(), object.y(), spec.size())) {
                strike(j, object, shot.damage());
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
            List<LevelScript.PartSpec> parts = piece.spec().parts();
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
            LevelScript.GroundObjectSpec spec = object.spec();
            if (object.hittable()
                    && !spec.submerged()
                    && (weapon.antiGround() || !spec.hardened())
                    && inBlast(x, y, radius, object.x(), object.y(), spec.size())) {
                strike(j, object, mine.damage());
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
            List<LevelScript.PartSpec> parts = piece.spec().parts();
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
     * The layers a mine's blast reaches (the {@code area} rule): {@code air}, {@code low-air} and
     * {@code ground}, not {@code high-air} above it nor (M5 part E, user decision E2 = a) {@code sub}
     * under the water.
     */
    private static boolean blastReaches(Layer layer) {
        return layer != Layer.HIGH_AIR && layer != Layer.SUB && layer.touchable();
    }

    /** A hit on a ground object: a trigger counts it, a destructible takes the damage. */
    private void strike(int index, GroundObject object, double damage) {
        if (object.spec().trigger()) {
            if (object.countHit()) {
                hits.triggerReleased(index);
            }
        } else if (object.damage(damage)) {
            hits.groundDestroyed(index);
        }
    }

    /** Bolts and missiles of an {@code anti-ground} weapon do double damage on the ground layer. */
    private static double groundFactor(WeaponSpec weapon, Layer layer) {
        return layer == Layer.GROUND
                        && weapon.antiGround()
                        && !weapon.delivery().landing()
                ? 2
                : 1;
    }

    /** Whether a blast of {@code radius} around (x, y) reaches a box around (bx, by). */
    static boolean inBlast(double x, double y, double radius, double bx, double by, Hitbox box) {
        double dx = Math.max(Math.abs(x - bx) - box.width() / 2, 0);
        double dy = Math.max(Math.abs(y - by) - box.height() / 2, 0);
        return dx * dx + dy * dy <= radius * radius;
    }

    /**
     * Whether shots, blasts and specials can reach {@code enemy}: on the play field, and not a chain
     * member waiting for its burst ({@link Chain#doomed}).
     */
    static boolean onField(Enemy enemy) {
        return PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox()) && !Chain.doomed(enemy);
    }

    private static double distanceSquared(double x, double y, double ox, double oy) {
        return (x - ox) * (x - ox) + (y - oy) * (y - oy);
    }

    void addTo(StateHash hash) {
        for (int m = 0; m < armament.size(); m++) {
            hash.add(cooldowns[m]).add(sinceShot[m]);
            if (armament.mount(m).fires() && armament.mount(m).weapon().delivery() == WeaponSpec.Delivery.TURRET) {
                hash.add(turretHeadings[m]).add(turretTargets[m]);
            }
        }
        hash.add(overdriveTicks);
        // M7 part A: only with a beam, a drone bay or a Deflector Pod fitted, so other sorties hash as before.
        if (anyBeam) {
            for (Beam beam : beams) {
                if (beam != null) {
                    beam.addTo(hash);
                }
            }
        }
        drones.addTo(hash);
        deflectors.addTo(hash);
    }

    /** M7 part A: mount {@code m}'s beam, null unless it is a {@link WeaponSpec.Delivery#BEAM} mount. */
    Beam beam(int m) {
        return beams[m];
    }

    Drones drones() {
        return drones;
    }

    Deflectors deflectors() {
        return deflectors;
    }

    Pool<Shot> shots() {
        return shots;
    }

    Armament armament() {
        return armament;
    }

    /** Steps since mount {@code m} last fired, for its muzzle flash; {@link Integer#MAX_VALUE} before its first shot. */
    int ticksSinceShot(int m) {
        return sinceShot[m];
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
                if (located(target, enemies)
                        && distanceSquared(mx, my, targetX, targetY) <= weapon.range() * weapon.range()) {
                    double share = Math.sqrt(distanceSquared(mx, my, targetX, targetY)) / weapon.range();
                    shot.lob(weapon, mount, mx, my, targetX, targetY, share);
                } else {
                    lob(shot, weapon, mount, mx, my, enemies, ground);
                }
            } else if (weapon.delivery() == WeaponSpec.Delivery.DROPPED) {
                shot.lob(weapon, mount, mx, my, mx, my);
            } else {
                shot.fire(weapon, mount, mx, my, muzzle.angle());
                if (weapon.delivery() == WeaponSpec.Delivery.HOMING) {
                    shot.lock(target);
                }
            }
        }
        events.add(SimEvents.Type.SHOT_FIRED, sumX / muzzles.size(), sumY / muzzles.size(), mount);
    }

    /** The enemy (a serial) the player's shots damaged within the last {@code ticks} steps; -1 for none. */
    int recentHit(int ticks) {
        return sinceHit <= ticks ? lastHit : -1;
    }

    /** The player's last hit, which steers a wingman: hashed only when one flies. */
    void addRecentHitTo(StateHash hash) {
        hash.add(lastHit).add(sinceHit);
    }

    int overdriveTicks() {
        return overdriveTicks;
    }

    /** Mount {@code m}'s turret heading, radians clockwise from up (its muzzle's angle for other weapons). */
    double turretHeading(int m) {
        return turretHeadings[m];
    }
}
