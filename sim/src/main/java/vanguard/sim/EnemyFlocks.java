package vanguard.sim;

/**
 * The swarms of an {@link EnemyForce} (M5 part D; out of the force): the flocks in flight, their members' entry (a
 * swarm's members enter together, M7 part G's pursuit refills in a free slot), their flight a step with the pursuits'
 * and the rams' events, and their members' escapes.
 */
final class EnemyFlocks {
    /** M5 part D: swarms in flight. */
    private static final int FLOCK_CAPACITY = 8;

    private final EnemyForce force;
    private final Pool<Enemy> enemies;
    private final SimEvents events;
    private final EnemyForce.Escapes escapes;
    private final Pool<Flock> flocks = new Pool<>(FLOCK_CAPACITY, Flock::new, Flock[]::new);
    /** Flocks started in this attempt, for their serials. */
    private int flocksSpawned;
    /** The flock the members entering in this step join (a swarm's members enter together); null for none. */
    private Flock joining;
    /**
     * M7 part G: the pursuits' refill motes left out in this attempt because their flock had no room (or was gone):
     * planned, they count nowhere ({@link #skipped()}).
     */
    private int skipped;

    EnemyFlocks(EnemyForce force, Pool<Enemy> enemies, SimEvents events, EnemyForce.Escapes escapes) {
        this.force = force;
        this.enemies = enemies;
        this.events = events;
        this.escapes = escapes;
    }

    /** Back to the level start: no flocks. */
    void reset() {
        Pools.clear(flocks);
        flocksSpawned = 0;
        joining = null;
        skipped = 0;
    }

    /** The members entering from now join no flock (a step's entries are over). */
    void entriesOver() {
        joining = null;
    }

    /**
     * M5 part D: a swarm's member enters with {@code serial}: the first of its wave starts the flock (null when the pool
     * has none free: then the wave's members are left out), the others join it in entry order.
     */
    void enter(Spawn spawn, WaveSchedule schedule, int serial) {
        Spawn.Swarm swarm = spawn.swarm().orElseThrow();
        if (swarm.refill()) {
            refill(spawn, swarm, schedule, serial);
            return;
        }
        if (swarm.member() == 0) {
            joining = flocks.obtain();
            if (joining != null) {
                joining.start(
                        spawn.enemy().flock().orElseThrow(),
                        flocksSpawned++,
                        swarm.route(),
                        schedule.lastEntry(),
                        schedule.lastFlipped(),
                        spawn.tick() * SimStep.SECONDS);
            }
        }
        if (joining != null) {
            Enemy enemy = enemies.obtain();
            if (enemy != null) {
                enemy.join(joining, swarm.member(), spawn, serial, swarm.x(), swarm.y(), joining.startFacing());
                if (joining.flipped()) {
                    // M7 part G: a member lives in its flock's frame.
                    enemy.flip();
                }
                enemy.entered(schedule.lastEntry());
                enemy.tag(schedule.lastTag());
                enemy.hunts = schedule.lastHunts();
                joining.member(swarm.member(), enemy, swarm.x(), swarm.y());
            }
        }
    }

    /**
     * M7 part G (design/campaign Level 20, The pursuit swarm): a pursuit's refill mote joins its wave's flock (the
     * flock of its route) in a free slot, from the top edge at the leader point's x; with no room (or no flock) it is
     * left out and counts nowhere ({@link #skipped()}).
     */
    private void refill(Spawn spawn, Spawn.Swarm swarm, WaveSchedule schedule, int serial) {
        Flock flock = null;
        for (int f = 0; f < flocks.size(); f++) {
            if (flocks.get(f).route() == swarm.route()) {
                flock = flocks.get(f);
                break;
            }
        }
        int slot = flock == null ? -1 : flock.freeSlot();
        Enemy enemy = slot < 0 ? null : enemies.obtain();
        if (enemy == null) {
            skipped++;
        } else {
            double x = Math.clamp(flock.leaderX() + swarm.x(), 0, PlayField.WIDTH);
            enemy.join(flock, slot, spawn, serial, x, swarm.y(), flock.joinFacing(x, swarm.y()));
            enemy.entered(schedule.lastEntry());
            enemy.tag(schedule.lastTag());
            enemy.hunts = schedule.lastHunts();
            flock.join(enemy);
        }
    }

    /** M7 part G: the pursuits' refill motes left out so far in this attempt (their flock was full). */
    int skipped() {
        return skipped;
    }

    /** Back to a boss checkpoint: the refill motes left out by then. */
    void skipped(int skippedUnits) {
        skipped = skippedUnits;
    }

    /** See {@link EnemyForce#moveFlocks(double, double)}. */
    void move(double shipX, double shipY) {
        Canyon canyon = force.canyon();
        for (int f = flocks.size() - 1; f >= 0; f--) {
            Flock flock = flocks.get(f);
            if (flock.pursuing()) {
                flock.advance(
                        canyon,
                        Double.isNaN(shipX) ? flock.leaderX() : shipX,
                        Double.isNaN(shipY) ? flock.leaderY() : shipY);
                pursuitEvents(flock);
            } else if (flock.spec().ram().isPresent() && !Double.isNaN(shipX)) {
                // M8 part C: its rams dive at the ship (design/enemies/naval/glassfin).
                flock.advance(canyon, shipX, shipY);
            } else {
                flock.advance(canyon);
            }
            for (int i = 0; i < flock.size(); i++) {
                if (flock.rammedOff(i)) {
                    // M8 part C: a ram that dove off the play field is gone (it escaped); none loops back.
                    Enemy member = flock.member(i);
                    flock.drop(i);
                    escapes.escaped(member);
                    force.free(member);
                }
            }
            if (flock.loopedBackNow() > 0) {
                events.add(SimEvents.Type.LOOP_BACK, flock.leaderX(), flock.leaderY(), flock.loopedBackNow());
            }
            if (flock.finished()) {
                boolean lingered = flock.lingered();
                for (int i = 0; i < flock.size(); i++) {
                    Enemy member = flock.member(i);
                    if (member != null && (lingered || !PlayField.overlaps(member.x(), member.y(), member.hitbox()))) {
                        flock.drop(i);
                        escapes.escaped(member);
                        force.free(member);
                    }
                }
            }
            if (!flock.alive() && flock.ended()) {
                flocks.free(f);
            }
        }
    }

    /** M7 part G: a pursuit's tell, lunge and break-off events of this step, only while its flock has members. */
    private void pursuitEvents(Flock flock) {
        Pursuit pursuit = flock.pursuit().orElseThrow();
        if (!flock.alive()) {
            return;
        }
        if (pursuit.brokeNow()) {
            events.add(SimEvents.Type.PURSUIT_BREAK, pursuit.leaderX(), pursuit.leaderY(), flock.serial());
        }
        if (pursuit.toldNow()) {
            events.add(SimEvents.Type.PURSUIT_TELL, pursuit.leaderX(), pursuit.leaderY(), flock.serial());
        }
        if (pursuit.lungedNow()) {
            events.add(SimEvents.Type.PURSUIT_LUNGE, pursuit.leaderX(), pursuit.leaderY(), flock.serial());
        }
    }

    /** M7 part G: the first pursuit flock's pursuit, empty for none; no allocation. */
    java.util.Optional<Pursuit> pursuit() {
        for (int f = 0; f < flocks.size(); f++) {
            if (flocks.get(f).pursuing()) {
                return flocks.get(f).pursuit();
            }
        }
        return java.util.Optional.empty();
    }

    Pool<Flock> pool() {
        return flocks;
    }
}
