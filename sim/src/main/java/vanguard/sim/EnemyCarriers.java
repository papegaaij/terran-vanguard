package vanguard.sim;

import java.util.List;

/**
 * The carriers of an {@link EnemyForce} and what they release (out of the force): the spawner that entered last, which
 * the escorts entering after it circle (a brood pod, M7 part D's Herald, M8 part D's rising carrier holding its
 * escorts until its rise's middle); a brood's hatch, a periodic spawner's cycle, and the escorts breaking off or
 * peeling off when their carrier is gone.
 */
final class EnemyCarriers {
    private final EnemyForce force;
    private final Pool<Enemy> enemies;
    private final SimEvents events;
    private final EnemyForce.Escapes escapes;
    /** Per enemy kind, the kind a spawner releases; -1 for others. */
    private final int[] broodKinds;
    /** M9 part C: per enemy kind, the kind its launcher launches; -1 for others. */
    private final int[] launchKinds;
    /** The spawner that entered last, which the escorts entering after it circle. */
    private Enemy lastCarrier;
    /**
     * M10 part B (design/campaign Level 36, the harvesters): the drifting carrier of the last one's wave that entered
     * before it (the pair's other hull), whose midpoint with it the escorts circle; null for none.
     */
    private Enemy lastPartner;
    /** M8 part D: the escorts held by a rising carrier until its rise's middle. */
    private final RiseEscorts riseEscorts = new RiseEscorts();

    EnemyCarriers(
            EnemyForce force,
            List<EnemySpec> kinds,
            Pool<Enemy> enemies,
            SimEvents events,
            EnemyForce.Escapes escapes) {
        this.force = force;
        this.enemies = enemies;
        this.events = events;
        this.escapes = escapes;
        broodKinds = EnemyKinds.broods(kinds);
        launchKinds = new int[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            launchKinds[k] = kinds.get(k).launcher().isPresent()
                    ? kinds.indexOf(kinds.get(k).launcher().get().unit())
                    : -1;
        }
    }

    /** Back to the level start: no carrier, no held escorts. */
    void reset() {
        lastCarrier = null;
        lastPartner = null;
        riseEscorts.reset();
    }

    /** The spawner that entered last; null for none (or it is gone). */
    Enemy lastCarrier() {
        return lastCarrier;
    }

    /** M10 part B: the last carrier's partner (a drifting carrier of its wave); null for none (or it is gone). */
    Enemy lastPartner() {
        return lastPartner;
    }

    /**
     * M8 part D: whether escort {@code spawn} is held unspawned (with {@code serial} and {@code tag}) until its rising
     * carrier's layer flips: it peels off then.
     */
    boolean held(Spawn spawn, int serial, String tag) {
        return spawn.escort().isPresent()
                && lastCarrier != null
                && lastCarrier.riseHolding()
                && riseEscorts.hold(spawn, lastCarrier, serial, tag);
    }

    /**
     * Unit {@code enemy} of {@code spec} entered (null when the pool had no room): a spawner, a Herald (M7 part D: its
     * escorts circle it inside its aura) and a rising unit (M8 part D: the Abyss Ray) are the last carrier.
     */
    void entered(EnemySpec spec, Enemy enemy) {
        if (spec.carrierDrift().isPresent()) {
            // M10 part B (design/campaign Level 36): a drifting carrier pairs with the one of its wave before it.
            lastPartner = enemy != null
                            && lastCarrier != null
                            && lastCarrier.wave == enemy.wave
                            && lastCarrier.spec().carrierDrift().isPresent()
                    ? lastCarrier
                    : null;
            lastCarrier = enemy;
            return;
        }
        if (spec.brood().isPresent()
                || spec.aura().isPresent()
                || spec.rise().isPresent()
                || spec.launcher().isPresent()) {
            lastCarrier = enemy;
            lastPartner = null;
        }
    }

    /**
     * A spawner ends, destroyed or bursting on its own (design/enemies/air/brood-pod): its units
     * fly out of its centre, spread evenly over its arc centred on the direction to the ship, and
     * its escorts break off.
     */
    void hatch(Enemy pod, Ship ship) {
        EnemySpec.Brood brood = pod.spec().brood().orElseThrow();
        fanOut(pod, brood.enemy(), brood.count(), brood.arcRadians(), brood.speed(), ship);
        events.add(SimEvents.Type.BROOD_HATCHED, pod.x(), pod.y(), pod.kind());
        carrierEnded(pod);
    }

    /**
     * {@code count} units of {@code enemy} fly out of {@code from}'s centre, spread evenly over
     * {@code arc} radians centred on the direction to the ship, at {@code speed} px/s.
     */
    private void fanOut(Enemy from, EnemySpec enemy, int count, double arc, double speed, Ship ship) {
        int kind = broodKinds[from.kind()];
        // M7 part G: worked out in the spawner's frame, which its units live in too.
        TravelFrame frame = from.frame();
        double fromY = from.frameY();
        double toShip = StrictMath.atan2(frame.y(ship.y()) - fromY, ship.x() - from.x());
        for (int k = 0; k < count; k++) {
            double angle = count == 1 ? toShip : toShip - arc / 2 + k * arc / (count - 1);
            Enemy unit = enemies.obtain();
            int serial = force.nextSerial();
            if (unit != null) {
                unit.hatch(enemy, kind, from.x(), fromY, Trig.cos(angle) * speed, Trig.sin(angle) * speed, serial);
                if (frame.flipped()) {
                    unit.flip();
                }
            }
        }
    }

    /**
     * A periodic spawner's step (M5 part C, design/enemies/ground/hive-node): its cycle runs, its
     * iris's telegraph is announced, and a due release (while the enemies fire) sends its units out
     * like a brood's, counted among the level's enemies as they are released.
     */
    void spawnCycle(Enemy enemy, Ship ship, boolean firing) {
        int step = enemy.spawnStep(ship.x(), ship.y());
        if (step == Enemy.SPAWN_TELEGRAPH) {
            events.add(SimEvents.Type.SPAWN_TELEGRAPH, enemy.x(), enemy.y(), enemy.kind());
        } else if (step == Enemy.SPAWN_RELEASE && firing) {
            EnemySpec.Spawner spawner = enemy.spec().spawner().orElseThrow();
            fanOut(enemy, spawner.enemy(), spawner.count(), spawner.arcRadians(), spawner.speed(), ship);
            events.add(SimEvents.Type.SPAWN_RELEASED, enemy.x(), enemy.y(), enemy.kind());
            escapes.released(enemy, spawner.count());
        }
    }

    /**
     * M9 part C (design/enemies/air/gilded-gunship): a launcher's step while it flies unstunned: its clock runs on the
     * play field; a due unit (while the enemies fire, and fewer than its cap of its units alive) flies out of its
     * centre at the launcher's speed, the stream spread evenly round its facing (straight down), counted among the
     * level's enemies as it is launched ({@link SimEvents.Type#LAUNCHED}).
     */
    void launch(Enemy carrier, boolean firing) {
        EnemySpec.Launcher launcher = carrier.spec().launcher().orElseThrow();
        int place = carrier.launching.step(launcher, PlayField.overlaps(carrier.x(), carrier.y(), carrier.hitbox()));
        if (place < 0 || !firing || alive(carrier.serial()) >= launcher.mostAlive()) {
            return;
        }
        int n = launcher.stream();
        double spread = launcher.spreadRadians();
        // Straight down in its frame (y up), the stream's units spread evenly round it.
        double angle = -StrictMath.PI / 2 + (n == 1 ? 0 : -spread / 2 + place * spread / (n - 1));
        TravelFrame frame = carrier.frame();
        Enemy unit = enemies.obtain();
        int serial = force.nextSerial();
        if (unit == null) {
            return;
        }
        double speed = launcher.speed();
        unit.hatch(
                launcher.unit(),
                launchKinds[carrier.kind()],
                carrier.x(),
                carrier.frameY(),
                Trig.cos(angle) * speed,
                Trig.sin(angle) * speed,
                serial);
        unit.launching.launchedBy(carrier.serial());
        if (launcher.unit().chase().isPresent()) {
            unit.chasing.launch(SimStep.ticks(launcher.straightSeconds()));
        }
        if (frame.flipped()) {
            unit.flip();
        }
        events.add(SimEvents.Type.LAUNCHED, carrier.x(), carrier.y(), launchKinds[carrier.kind()]);
        escapes.released(carrier, 1);
    }

    /**
     * M9 part H, step H1b (design/enemies/bosses/iron-sovereign, Dart Drones): one unit of {@code launcher} out of a
     * ringed boss's rim bay at (x, y) along {@code angle} (y up) at the launcher's speed, flying straight its time before
     * its chase, its carrier {@link Launching#BOSS}; skipped while the launcher's most of the boss's units are alive.
     * Returns whether it was launched.
     */
    boolean bayLaunch(EnemySpec.Launcher launcher, int kind, double x, double y, double angle) {
        if (alive(Launching.BOSS) >= launcher.mostAlive()) {
            return false;
        }
        Enemy unit = enemies.obtain();
        int serial = force.nextSerial();
        if (unit == null) {
            return false;
        }
        double speed = launcher.speed();
        unit.hatch(launcher.unit(), kind, x, y, Trig.cos(angle) * speed, Trig.sin(angle) * speed, serial);
        unit.launching.launchedBy(Launching.BOSS);
        if (launcher.unit().chase().isPresent()) {
            unit.chasing.launch(SimStep.ticks(launcher.straightSeconds()));
        }
        return true;
    }

    /** M9 part C: the units alive that the carrier with {@code serial} launched. */
    private int alive(int serial) {
        int n = 0;
        for (int i = 0; i < enemies.size(); i++) {
            n += enemies.get(i).launchedBy() == serial ? 1 : 0;
        }
        return n;
    }

    /** See {@link EnemyForce#heraldGone}. */
    void heraldGone(Enemy herald, boolean killed) {
        carrierEnded(herald);
        if (!killed) {
            return;
        }
        int collapsed = 0;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.ringFeeder() == herald.serial()) {
                unit.collapseRing();
                collapsed++;
            }
        }
        if (collapsed > 0) {
            events.add(SimEvents.Type.RINGS_COLLAPSED, herald.x(), herald.y(), collapsed);
        }
    }

    /**
     * M8 part D: rising carrier {@code carrier} is gone (destroyed or away, before it is freed): the escorts it still
     * held peel off where it was.
     */
    void riseEnded(Enemy carrier, Ship ship) {
        peel(carrier, ship);
        carrierEnded(carrier);
    }

    /**
     * M8 part D (design/enemies/naval/abyss-ray, design/enemies/naval/barbfin): the escorts {@code carrier} held peel
     * off from its centre, in their entry order, to hold points spread evenly across their circle's diameter around
     * its x at their hover's height above the ship ({@link EnemyStarts#peel}); they keep their own cadence after it
     * leaves or dies.
     */
    void peel(Enemy carrier, Ship ship) {
        int count = riseEscorts.count(carrier);
        for (int i = 0; i < count; i++) {
            int k = riseEscorts.first(carrier);
            Spawn plan = riseEscorts.plan(k);
            int serial = riseEscorts.serial(k);
            String tag = riseEscorts.tag(k);
            riseEscorts.remove(k);
            double radius = plan.escort().isPresent() ? plan.escort().get().radius() : 0;
            double offset = count == 1 ? 0 : radius * (2.0 * i / (count - 1) - 1);
            Enemy unit = enemies.obtain();
            if (unit != null) {
                unit.peel(plan, carrier.x(), carrier.y(), offset, ship.y(), serial);
                unit.tag(tag);
            }
        }
    }

    /** M8 part D: the escorts held by rising carriers (hashed only while there are any). */
    void addRiseEscortsTo(StateHash hash) {
        if (riseEscorts.size() > 0) {
            riseEscorts.addTo(hash);
        }
    }

    /** The escorts circling {@code carrier} break off: it is gone. */
    void carrierEnded(Enemy carrier) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.escorts(carrier)) {
                // M10 part B: a pair's escort circles the partner left (else breaks off, as before).
                enemy.escorting.ended(carrier);
            }
        }
        if (lastPartner == carrier) {
            lastPartner = null;
        }
        if (lastCarrier == carrier) {
            lastCarrier = lastPartner;
            lastPartner = null;
        }
    }
}
