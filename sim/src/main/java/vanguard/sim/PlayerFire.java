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
    /** M7 part A: the beam mounts and their burn. */
    private final Beams beams;
    /** The turrets' aim and the homing locks. */
    private final Homing homing;
    /** What lands and bursts, the shots below the air and in rock. */
    private final GroundFire groundFire;
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
        beams = new Beams(this, ship, armament, events, hits, setPieces);
        homing = new Homing(this, ship, setPieces, turretHeadings, turretTargets);
        groundFire = new GroundFire(this, shots, events, hits, setPieces);
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

    /** The level's rock; null without terrain. */
    Canyon canyon() {
        return canyon;
    }

    /** The level's tendril gates; null without. */
    Links links() {
        return links;
    }

    /** The player's shots damaged the enemy with {@code serial} now (the wingman's first target). */
    void hitBy(int serial) {
        lastHit = serial;
        sinceHit = 0;
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
        }
        beams.reset();
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
            if (beams.fitted(m)) {
                if (beams.step(m, weapon, held)) {
                    sinceShot[m] = 0;
                }
                continue;
            }
            if (weapon.delivery() == WeaponSpec.Delivery.TURRET) {
                homing.aim(m, weapon, enemies);
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
                    shot.lock(homing.nearestInCone(shot, enemies, ground));
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
                homing.home(shot, enemies, ground);
            }
            shot.move();
            if (shot.spent() || !PlayField.overlaps(shot.x(), shot.y(), weapon.size())) {
                shots.free(i);
            }
        }
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

    /** M7 part A: every beam that is on burns the first target in its column ({@link Beams#burn}). */
    void burn(Pool<Enemy> enemies, Pool<GroundObject> ground, Pool<Mine> spores) {
        beams.burn(enemies, ground, spores);
    }

    /** M7 part A: every beam goes off (the ship does not fly: its launch, its wreck, the level's end). */
    void ceaseBeams() {
        beams.ceaseBeams();
    }

    /** M7 part C: a shot in rock stops there ({@link GroundFire#stopAtRock}). */
    void stopAtRock(Pool<GroundObject> ground, Pool<Enemy> enemies) {
        groundFire.stopAtRock(ground, enemies);
    }

    /** Shots hit the ground objects below; bombs, shells and mines burst ({@link GroundFire#hitGround}). */
    void hitGround(Pool<GroundObject> ground, Pool<Enemy> enemies, Pool<Mine> spores) {
        groundFire.hitGround(ground, enemies, spores);
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
                // M7 part D: a unit part is tested before the body (the Shellback's vent); round 40 R.a: a shot over
                // the body whose line of flight crosses the part's box hits the part, from any side.
                boolean onBody = weapon.size().overlaps(shot.x(), shot.y(), enemy.hitbox(), enemy.x(), enemy.y());
                boolean onPart = enemy.partAlive()
                        && (weapon.size().overlaps(shot.x(), shot.y(), enemy.partBox(), enemy.partX(), enemy.partY())
                                || (onBody && towardPart(shot, weapon.size(), enemy)));
                if ((!onPart && !onBody) || (weapon.pierce() > 1 && shot.struck(2 * enemy.serial()))) {
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
     * M7 part D, round 40 R.a (design/enemies/ground/shellback, the lime vent): whether {@code shot}, of {@code
     * size}, over {@code enemy}'s body, flies on into the box of its standing unit part before it leaves the body:
     * its line of flight from where it is, as far as the body's box is wide and tall, crosses the part's box grown by
     * the shot's half size. Such a shot hits the part first, from any side (the vent on the back of the shell is seen
     * from above). Allocates nothing.
     */
    static boolean towardPart(Shot shot, Hitbox size, Enemy enemy) {
        double vx = shot.vx();
        double vy = shot.vy();
        double speed = Math.hypot(vx, vy);
        if (speed == 0) {
            return false;
        }
        Hitbox body = enemy.hitbox();
        double reach = body.width() + body.height();
        Hitbox part = enemy.partBox();
        return segmentCrossesBox(
                shot.x(),
                shot.y(),
                vx / speed * reach,
                vy / speed * reach,
                enemy.partX(),
                enemy.partY(),
                (part.width() + size.width()) / 2,
                (part.height() + size.height()) / 2);
    }

    /**
     * Whether the segment from (x, y) to (x + dx, y + dy) crosses the box of half sizes {@code halfW} by {@code
     * halfH} round (bx, by) (the slab test; touching edges do not count).
     */
    static boolean segmentCrossesBox(
            double x, double y, double dx, double dy, double bx, double by, double halfW, double halfH) {
        double enter = 0;
        double leave = 1;
        if (dx == 0) {
            if (Math.abs(x - bx) >= halfW) {
                return false;
            }
        } else {
            double a = (bx - halfW - x) / dx;
            double b = (bx + halfW - x) / dx;
            enter = Math.max(enter, Math.min(a, b));
            leave = Math.min(leave, Math.max(a, b));
        }
        if (dy == 0) {
            if (Math.abs(y - by) >= halfH) {
                return false;
            }
        } else {
            double a = (by - halfH - y) / dy;
            double b = (by + halfH - y) / dy;
            enter = Math.max(enter, Math.min(a, b));
            leave = Math.min(leave, Math.max(a, b));
        }
        return enter < leave;
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
            boolean overhead = Homing.overhead(piece);
            boolean anchored = piece.arena().isPresent();
            for (int i = shots.size() - 1; i >= 0; i--) {
                Shot shot = shots.get(i);
                WeaponSpec weapon = shot.weapon();
                if (weapon.delivery().landing()
                        || (!anchored && !weapon.delivery().reaches(piece.layer()))
                        || (overhead && homing.overhead(shot.target()) != k)) {
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

    /** Bolts and missiles of an {@code anti-ground} weapon do double damage on the ground layer. */
    static double groundFactor(WeaponSpec weapon, Layer layer) {
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

    static double distanceSquared(double x, double y, double ox, double oy) {
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
        beams.addTo(hash);
        drones.addTo(hash);
        deflectors.addTo(hash);
    }

    /** M7 part A: mount {@code m}'s beam, null unless it is a {@link WeaponSpec.Delivery#BEAM} mount. */
    Beam beam(int m) {
        return beams.beam(m);
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
