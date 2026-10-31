package vanguard.sim;

/**
 * The walkers' attacks (out of {@link EnemyForce}; design/enemies/ground/scuttler): the fan, the spit, the pounce's
 * events, a turret's bursts, and M5 part B's staggered walker waves' volley clocks.
 */
final class WalkerFire {
    private final SimEvents events;
    private final EnemyFire guns;
    private final ObjectiveHook objectiveHook;
    /**
     * M5 part B: per staggered walker wave its volley clock, in steps since the first of its units
     * came onto the screen (counting that step as 1); 0 before (see {@link Enemy#staggeredVolley}).
     */
    private final int[] volleyClocks;

    WalkerFire(int volleyGroups, SimEvents events, EnemyFire guns, ObjectiveHook objectiveHook) {
        this.events = events;
        this.guns = guns;
        this.objectiveHook = objectiveHook;
        volleyClocks = new int[volleyGroups];
    }

    /** Back to the level start: no volley clock running. */
    void reset() {
        java.util.Arrays.fill(volleyClocks, 0);
    }

    /** The running volley clocks advance a step. */
    void tick() {
        for (int g = 0; g < volleyClocks.length; g++) {
            if (volleyClocks[g] > 0) {
                volleyClocks[g]++;
            }
        }
    }

    /** Adds the staggered waves' volley clocks to {@code hash}; nothing without one (Act 1's hashes stay). */
    void addTo(StateHash hash) {
        for (int clock : volleyClocks) {
            hash.add(clock);
        }
    }

    /**
     * A walker's attacks (design/enemies/ground/scuttler): its fan along its facing, which aims at
     * nobody, or (M5 part B, design/enemies/ground/creeper) centred on the ship, on its own clock or
     * its wave's staggered one; and its spit, aimed (at the ship, or the convoy unit the hook picks)
     * while the ship is behind it.
     */
    void fire(Enemy enemy, Ship ship, boolean firing) {
        fire(enemy, ship, firing, false);
    }

    /**
     * As {@link #fire(Enemy, Ship, boolean)}; M10 part A (design/player/systems, the IFF spoofer): while {@code spoofed}
     * its turret's shots and its homing salvo are held at their stale aim ({@link Spoofing}; {@link #fireHeld} fires
     * them).
     */
    void fire(Enemy enemy, Ship ship, boolean firing, boolean spoofed) {
        if (enemy.spec().turret().isPresent()) {
            // M7 part F (design/enemies/ground/warden-tank): its turret's bursts along the turret's aim, any way.
            if (enemy.trigger()
                    && firing
                    && !(spoofed
                            && enemy.spoofing.hold(enemy, Spoofing.TURRET, ship.x(), ship.y(), enemy.turretAim()))) {
                guns.fire(
                        enemy.partX(),
                        enemy.partY(),
                        enemy.spec().gun().orElseThrow(),
                        false,
                        enemy.turretAim(),
                        ship,
                        -1,
                        true,
                        enemy.frame());
            }
            return;
        }
        boolean fan = enemy.volleyGroup() >= 0 ? enemy.staggeredVolley(volleyClocks) : enemy.trigger();
        // M9 part E: a quiet second attack (the Strider's pod gone, or it topples) waits; earlier walkers never are.
        boolean spit = !enemy.spitQuiet() && enemy.spit(ship.x(), ship.y());
        if (enemy.landedNow()) {
            events.add(SimEvents.Type.POUNCE_LANDED, enemy.x(), enemy.y(), enemy.kind());
        }
        if (!firing) {
            return;
        }
        if (enemy.pounce(ship.x(), ship.y())) {
            events.add(SimEvents.Type.POUNCE, enemy.x(), enemy.y(), enemy.kind());
        }
        if (fan && enemy.spec().gun().orElseThrow().mortar().isPresent()) {
            // M7 part D: a walker with a mortar (design/enemies/ground/shellback) lobs instead of its fan.
            guns.lob(enemy, enemy.spec().gun().get(), ship);
        } else if (fan) {
            boolean atShip = enemy.spec().walker().orElseThrow().fanAtShip();
            int inArc = objectiveHook.mode(enemy.kind()) == HookMode.IN_ARC ? objectiveHook.inArc(enemy) : -1;
            if (inArc >= 0) {
                // M5 part G: the objective within its armour's arc: the fan centred on it, aimed at it.
                guns.fire(
                        enemy.x(),
                        enemy.y(),
                        enemy.spec().gun().orElseThrow(),
                        false,
                        0,
                        ship,
                        inArc,
                        false,
                        enemy.frame());
            } else if (!(atShip
                    && spoofed
                    && enemy.spoofing.hold(enemy, Spoofing.AIMED, ship.x(), ship.y(), enemy.facing()))) {
                // M10 part A (the IFF spoofer): a fan at the ship is held at its stale aim; one along the facing is
                // not.
                guns.fire(
                        enemy.x(),
                        enemy.y(),
                        enemy.spec().gun().orElseThrow(),
                        false,
                        enemy.facing(),
                        ship,
                        -1,
                        !atShip,
                        enemy.frame());
            }
        }
        if (spit
                && enemy.spec()
                        .walker()
                        .orElseThrow()
                        .spit()
                        .orElseThrow()
                        .homing()
                        .isPresent()) {
            // M9 part E (design/enemies/ground/strider, the shoulder pod): a homing salvo at the ship.
            EnemyGun pod = enemy.spec().walker().get().spit().get();
            double bearing = bearing(enemy, ship);
            if (!(spoofed && enemy.spoofing.hold(enemy, Spoofing.SALVO, ship.x(), ship.y(), bearing))) {
                salvo(enemy, pod, bearing);
            }
        } else if (spit) {
            EnemyGun gun = enemy.spec().walker().orElseThrow().spit().orElseThrow();
            int target = objectiveHook.mode(enemy.kind()) == HookMode.NEAREST
                    ? objectiveHook.convoy().nearest(enemy.x(), enemy.y(), ship.x(), ship.y())
                    : -1;
            guns.fire(enemy.x(), enemy.y(), gun, false, 0, ship, target, false, enemy.frame());
        }
    }

    /**
     * M9 part E (design/enemies/ground/strider): a walker's homing salvo from its pod (its second attack): {@code gun}'s
     * {@link EnemyGun#burst()} missiles at once from the pod (its part carrying the attack, else its centre), launched
     * along the bearing to the ship spread {@link EnemyGun#spreadRadians()} apart, each under the gun's cap.
     */
    private void salvo(Enemy enemy, EnemyGun gun, double bearing) {
        int pod = podOf(enemy);
        double x = pod < 0 ? enemy.x() : enemy.partX(pod);
        double y = pod < 0 ? enemy.y() : enemy.partY(pod);
        int count = gun.burst();
        for (int k = 0; k < count; k++) {
            double heading = bearing + (k - (count - 1) / 2.0) * gun.spreadRadians();
            if (guns.missiles().launchAlong(x, y, gun, heading, enemy.serial())) {
                events.add(SimEvents.Type.ENEMY_FIRED, x, y, (int) gun.damage());
            }
        }
    }

    /** The bearing from {@code enemy}'s pod (its centre without one) to the ship. */
    private static double bearing(Enemy enemy, Ship ship) {
        int pod = podOf(enemy);
        double x = pod < 0 ? enemy.x() : enemy.partX(pod);
        double y = pod < 0 ? enemy.y() : enemy.partY(pod);
        return Enemy.heading(ship.x() - x, ship.y() - y);
    }

    /**
     * M10 part A (the IFF spoofer): a held turret shot ({@link Spoofing#TURRET}) along its stale turret aim, or a held
     * homing salvo ({@link Spoofing#SALVO}) along its stale bearing.
     */
    void fireHeld(Enemy enemy, Ship ship, int kind, double aim) {
        if (kind == Spoofing.TURRET) {
            guns.fire(
                    enemy.partX(),
                    enemy.partY(),
                    enemy.spec().gun().orElseThrow(),
                    false,
                    aim,
                    ship,
                    -1,
                    true,
                    enemy.frame());
        } else {
            salvo(enemy, enemy.spec().walker().orElseThrow().spit().orElseThrow(), aim);
        }
    }

    /** The part carrying a walker's second attack (its pod), -1 for none. */
    private static int podOf(Enemy enemy) {
        for (int p = 0; p < enemy.partCount(); p++) {
            if (enemy.spec().parts().get(p).silencesSpit()) {
                return p;
            }
        }
        return -1;
    }
}
