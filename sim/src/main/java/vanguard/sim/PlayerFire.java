package vanguard.sim;

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
 *
 * <p>M8 part A (design/world/europa, Under water rules): the {@link Medium}'s water factors land on a shot once, as it
 * is fired ({@link MountVolleys#soak}: its damage, speed and range; the ship's, the drones' and Rook's), and per step on the beam
 * ({@link Beams}); every layer rule reads {@link Medium#reaches}. A torpedo runs where there is water (over water or
 * under it) and, with a splash (the Harpoon Torpedoes), its hit deals a share of its damage to the units round it; a
 * {@link WeaponSpec.Delivery#DEPTH_CHARGE} is tossed forward (M8 AW), hovers, sinks with the scroll and bursts on its fuse
 * or, under water, on touching a swimmer.
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

        /** M7 part D: unit part {@code part} of the enemy at {@code index} was destroyed (by a special: no mount). */
        default void unitPartDestroyed(int index, int part) {
            unitPartDestroyed(index, part, -1);
        }

        /**
         * M7 part D: unit part {@code part} of the enemy at {@code index} (design/enemies/ground/shellback, the vent;
         * M8 part B: a Drill Rig's pump) was destroyed by a shot or blast of {@code mount}; -1 for none. The enemy
         * stays.
         */
        void unitPartDestroyed(int index, int part, int mount);
    }

    /** Homing locks on a set piece's part use serials from here: unit serials stay far below it. */
    static final int PART_SERIAL = 1 << 24;

    /**
     * M5 part E: a torpedo's lock on a sunken trigger (a ground object's serial added), above the
     * parts' serials.
     */
    static final int TRIGGER_SERIAL = 1 << 25;

    /**
     * M9 part A (QA-1): a homing lock's serial for a seekable enemy missile ({@link EnemyGun.HomingSpec#seekable()}):
     * this plus the missile's {@link Missile#serial()}.
     */
    static final int MISSILE_SERIAL = 1 << 26;

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
    /** M9 part C: whether the level has no ground layer (the bomb deliveries are idle). */
    private boolean noGround;

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
    /** M9 part A: the Tesla Coil Pods' arcs (design/player/weapons/tesla-coil-pod). */
    private final ArcFire arcs;
    /** M9 part A: the power budget in flight under a drain ({@link PowerState}); null without one. */
    private PowerState power;

    private int overdriveTicks;
    /** M7 part C: the level's rock and the scroll now; null in a level without terrain. */
    private Canyon canyon;
    /** M7 part F: the enemies' homing missiles, which shots and the beam bring down; null until the sortie sets them. */
    private Missiles missiles;
    /** M7 part C: the level's tendril gates, which a beam burns on its way past; null without. */
    private Links links;
    /** M8 part H: the finale's cruisers, whose hulls stop the beam; null without. */
    private CruiserRun cruisers;
    /** M8 part A: the medium the ship flies in (the air unless the sortie sets the level's). */
    private Medium medium = Medium.air();
    /** M9 part A: the direction of travel (1, or -1 while the scroll runs reversed); not hashed (the clock's). */
    private int travel = 1;
    /** RF9c: the hit tests, the mounts' volleys. */
    private final ShotHits shotHits;

    private final MountVolleys volleys;

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
        arcs = new ArcFire(this, ship, setPieces, events, hits, armament.size());
        shotHits = new ShotHits(this, armament, events, hits, setPieces, shots, homing);
        volleys = new MountVolleys(this, ship, events, shots, drones, homing, turretHeadings, turretTargets);
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

    /** M7 part F: the enemies' homing missiles (the sortie's, from its enemy force). */
    void missiles(Missiles flying) {
        missiles = flying;
    }

    /** M7 part F: the enemies' homing missiles; null before the sortie sets them. */
    Missiles missiles() {
        return missiles;
    }

    /**
     * M9 part A: the direction of travel now ({@link Sortie#travel()}: 1, or -1 while the scroll runs reversed), for a
     * rear-first missile's "behind" (design/player/weapons/swarm-tail).
     */
    void travel(int sign) {
        travel = sign;
    }

    /**
     * M9 part A (design/player/generator, Enemy drain effects): the power budget in flight; a drainer's latch then
     * sheds it ({@link #drain}). Without one nothing is ever shed.
     */
    void power(Loadout.Power budget) {
        power = new PowerState(budget, armament.size());
        deflectors.power(power);
    }

    /**
     * M9 part A: a drainer latched ({@code delta} +1) or let go (−1); returns the drainers latched now (0 without a
     * power budget, which nothing drains). A {@link SimEvents.Type#POWER_DRAIN} at the ship.
     */
    int drain(int delta) {
        if (power == null) {
            return 0;
        }
        int now = power.drain(delta);
        events.add(SimEvents.Type.POWER_DRAIN, ship.x(), ship.y(), now);
        return now;
    }

    /** M9 part A: the power budget in flight (the HUD's drain row); null without one. */
    PowerState power() {
        return power;
    }

    /** M9 part A: a beam's damage factor now: the drain's rate factor ({@link PowerState#factor()}); 1 without. */
    double powerFactor() {
        return power == null ? 1 : power.factor();
    }

    /** M9 part A: the point mount {@code m}'s last arc struck, on the screen (the arc's look runs from the pod to it). */
    public double arcX(int m) {
        return arcs.arcX(m);
    }

    public double arcY(int m) {
        return arcs.arcY(m);
    }

    /** M9 part A: the direction of travel now (1, or -1 while the scroll runs reversed). */
    int travel() {
        return travel;
    }

    /** M8 part A: the level's medium (design/world/europa, Under water rules). */
    void medium(Medium water) {
        medium = water;
    }

    /** M8 part A: the medium the ship flies in. */
    Medium medium() {
        return medium;
    }

    /** M8 AW: the ship (a depth charge over its hull touches a unit latched on it, {@link GroundFire}). */
    Ship ship() {
        return ship;
    }

    /** The level's rock; null without terrain. */
    Canyon canyon() {
        return canyon;
    }

    /** M8 part H, step H2: the finale's cruisers (Level 28), whose hulls stop the beam; null without. */
    void cruisers(CruiserRun run) {
        cruisers = run;
    }

    /** The finale's cruisers; null without. */
    CruiserRun cruisers() {
        return cruisers;
    }

    /** The level's tendril gates; null without. */
    Links links() {
        return links;
    }

    /** The player's shots damaged the enemy with {@code serial} now (the wingman's first target). */
    void hitBy(int serial) {
        shotHits.hitBy(serial);
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
        arcs.reset();
        if (power != null) {
            power.reset();
        }
        overdriveTicks = 0;
        shotHits.reset();
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
            if (cooldowns[m] > 0 && (power == null || power.runs(m))) {
                // M9 part A: under a drain a mount's cooldown runs only on its power's steps (f × its rate).
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
                    || (weapon.delivery().laid()
                            && volleys.liveMines(m) >= weapon.mines().maxLive())) {
                // A turret without a target holds fire; at its mine cap a mount waits for one to go;
                // a torpedo pod over land fires nothing (M5 part E).
                continue;
            }
            if (weapon.delivery() == WeaponSpec.Delivery.ARC) {
                // M9 part A: an arc fires only with a target in reach (no arc into empty space).
                if (!arcs.strike(m, weapon, enemies)) {
                    continue;
                }
                cooldowns[m] = weapon.intervalTicks();
                sinceShot[m] = 0;
                events.add(
                        SimEvents.Type.SHOT_FIRED,
                        ship.x() + weapon.muzzles().getFirst().dx(),
                        ship.y() + weapon.muzzles().getFirst().dy(),
                        m);
                continue;
            }
            cooldowns[m] = weapon.intervalTicks();
            sinceShot[m] = 0;
            if (mount.bay()) {
                volleys.droneVolley(m, weapon);
            } else {
                volleys.volley(m, weapon, enemies, ground);
            }
        }
    }

    /**
     * M7 part A: one step of the drones (their orbit, rebuild and fade-in) and the Deflector Pods (their
     * charges), with the overdrive's numbers while one runs; nothing without them.
     */
    void tend() {
        if (power != null) {
            // M9 part A: the mounts' clocks under a drain, once a step before the guns and the pods.
            power.step();
        }
        drones.update(overdriveTicks > 0, events);
        deflectors.update(overdriveTicks > 0, events);
    }

    /**
     * M5 part E (user decision E1 = a): whether {@code weapon} fires nothing in this level: a
     * torpedo over land (its pod still draws its power; the HUD reads NO WATER). M8 part A: a torpedo (the Harpoon's
     * too) or a depth charge where there is no water: not over water and not under it now.
     */
    boolean idle(WeaponSpec weapon) {
        return idleReason(weapon) != IdleReason.NONE;
    }

    /**
     * Why {@code weapon} fires nothing in this level ({@link #idle}): M9 part C: a bomb delivery where the level has no
     * ground layer reads {@link IdleReason#NO_GROUND} (its rack still draws its power); {@link IdleReason#NONE} for a
     * weapon that fires.
     */
    IdleReason idleReason(WeaponSpec weapon) {
        if (weapon == null) {
            return IdleReason.NONE;
        }
        if (weapon.delivery().needsWater() && !water && !medium.underwater()) {
            return IdleReason.NO_WATER;
        }
        if (noGround && weapon.delivery().needsGround()) {
            return IdleReason.NO_GROUND;
        }
        return IdleReason.NONE;
    }

    /** M9 part C: the level has no ground layer (Level 30): the bomb deliveries are idle. */
    void noGround(boolean none) {
        noGround = none;
    }
    /**
     * Moves every projectile by one step: bolts fly on until their range is flown or they leave
     * the screen, homing missiles and torpedoes steer at their target first, bombs and shells fall
     * towards their landing point, which scrolls down with the ground by {@code groundScroll}. A
     * torpedo also seeks a sunken trigger among the {@code ground} objects (M5 part E).
     */
    void move(double groundScroll, Pool<Enemy> enemies, Pool<GroundObject> ground) {
        shotHits.age();
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
            if (weapon.delivery() == WeaponSpec.Delivery.DEPTH_CHARGE) {
                // M8 part A: it is tossed, hovers, then sinks with the ground; it bursts on its fuse or a swimmer
                // (hitGround).
                shot.sink(groundScroll);
                if (canyon != null) {
                    shot.placeX(
                            canyon.flyableX(shot.y(), shot.x(), weapon.size().width() / 2));
                }
                if (!PlayField.overlaps(shot.x(), shot.y(), weapon.size())) {
                    shots.free(i);
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

    /** Bolts and missiles hit the enemies they pass on the layers they reach ({@link ShotHits#hitEnemies}). */
    void hitEnemies(Pool<Enemy> enemies) {
        shotHits.hitEnemies(enemies);
    }

    /** Whether the shots of {@code mount} hit a Lamprey latched onto the hull ({@link ShotHits#hitsLatched}). */
    boolean hitsLatched(int mount) {
        return shotHits.hitsLatched(mount);
    }

    /** Bolts and missiles hit the armed spore mines on the player's layer ({@link ShotHits#hitMines}). */
    void hitMines(Pool<Mine> mines) {
        shotHits.hitMines(mines);
    }

    /** M7 part F: shots that reach the air hit the enemies' homing missiles ({@link ShotHits#hitMissiles}). */
    void hitMissiles() {
        shotHits.hitMissiles();
    }

    /** Bolts and missiles that reach a set piece's layer hit its living parts ({@link ShotHits#hitSetPieces}). */
    void hitSetPieces() {
        shotHits.hitSetPieces();
    }

    /** M8 part A: a torpedo's splash round the unit it struck ({@link ShotHits#splash}). */
    void splash(Shot shot, int struck, Pool<Enemy> enemies) {
        shotHits.splash(shot, struck, enemies);
    }

    /** M9 part B: whether a shot of {@code mount} passes through {@code enemy} ({@link ShotHits#passesThrough}). */
    boolean passesThrough(Enemy enemy, int mount) {
        return shotHits.passesThrough(enemy, mount);
    }

    /** A hit on a ground object: a trigger counts it, a destructible takes the damage. */
    void strike(int index, GroundObject object, double damage) {
        shotHits.strike(index, object, damage);
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
        if (power != null) {
            // M9 part A: only once a drainer latched in the attempt, so the other sorties hash as before.
            power.addTo(hash);
        }
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

    /** A wingman's volley ({@link MountVolleys#wingmanVolley(int, WeaponSpec, double, double, int, Pool, Pool)}). */
    void wingmanVolley(
            int mount,
            WeaponSpec weapon,
            double x,
            double y,
            int target,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground) {
        volleys.wingmanVolley(mount, weapon, x, y, target, enemies, ground);
    }

    /** A wingman's volley turned by {@code aim} ({@link MountVolleys#wingmanVolley(int, WeaponSpec, double, double, int, Pool, Pool, double)}). */
    void wingmanVolley(
            int mount,
            WeaponSpec weapon,
            double x,
            double y,
            int target,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground,
            double aim) {
        volleys.wingmanVolley(mount, weapon, x, y, target, enemies, ground, aim);
    }

    /** The enemy (a serial) the player's shots damaged within the last {@code ticks} steps; -1 for none. */
    int recentHit(int ticks) {
        return shotHits.recentHit(ticks);
    }

    /** The player's last hit, which steers a wingman: hashed only when one flies. */
    void addRecentHitTo(StateHash hash) {
        shotHits.addRecentHitTo(hash);
    }

    int overdriveTicks() {
        return overdriveTicks;
    }

    /** Mount {@code m}'s turret heading, radians clockwise from up (its muzzle's angle for other weapons). */
    double turretHeading(int m) {
        return turretHeadings[m];
    }
}
