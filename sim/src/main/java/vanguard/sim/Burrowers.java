package vanguard.sim;

/**
 * M7 part B, the Burrowers of an {@link EnemyForce} (design/enemies/ground/burrower): each unit's
 * surfacing, dive and rings, and its wave's stagger, which spaces the bulges of the wave's units and caps how
 * many of them are up at once.
 */
final class Burrowers {
    private final Pool<Enemy> enemies;
    private final SimEvents events;
    private final EnemyFire guns;
    /**
     * M7 part B: per Burrower wave its stagger clock (design/enemies/ground/burrower, Swarm stagger): steps
     * until the next bulge of its units may start; 0 when one may now.
     */
    private final int[] burrowWaits;

    /** @param waves the Burrower waves of the level */
    Burrowers(int waves, Pool<Enemy> enemies, SimEvents events, EnemyFire guns) {
        this.enemies = enemies;
        this.events = events;
        this.guns = guns;
        burrowWaits = new int[waves];
    }

    /** Back to the level start: every wave may bulge. */
    void reset() {
        java.util.Arrays.fill(burrowWaits, 0);
    }

    /** One step of the waves' stagger clocks. */
    void tick() {
        for (int g = 0; g < burrowWaits.length; g++) {
            if (burrowWaits[g] > 0) {
                burrowWaits[g]--;
            }
        }
    }

    /** The waves' stagger clocks; nothing without one. */
    void addTo(StateHash hash) {
        for (int wait : burrowWaits) {
            hash.add(wait);
        }
    }

    /**
     * M7 part B, a Burrower's step after its move (design/enemies/ground/burrower): its surfacing's and
     * dive's events, its rings when due (while {@code firing}; none while the ship is closer than bullets
     * may spawn), and, waiting at its spot, its wave's stagger: a spot that is no longer allowed (the ship
     * within the clearance, a no-surface zone under it) sends it to a new one; otherwise it bulges once its
     * wave's clock lets it and fewer than its cap of its wave would still be up when it surfaces.
     */
    void burrow(Enemy enemy, Ship ship, boolean firing) {
        Burrowing cycle = enemy.burrow();
        int now = cycle.now();
        if ((now & Burrowing.NOW_SURFACED) != 0) {
            events.add(SimEvents.Type.BURROW_SURFACED, enemy.x(), enemy.y(), enemy.kind());
        }
        if ((now & Burrowing.NOW_DIVE) != 0) {
            events.add(SimEvents.Type.BURROW_DIVE, enemy.x(), enemy.y(), enemy.kind());
        }
        EnemySpec.BurrowRing ring = cycle.ringDue();
        if (ring != null && firing) {
            guns.burst(enemy.x(), enemy.y(), ring.count(), ring.bulletSpeed(), ring.damage(), ship);
            events.add(SimEvents.Type.ENEMY_FIRED, enemy.x(), enemy.y(), (int) ring.damage());
        }
        if (!cycle.waiting()) {
            return;
        }
        if (!cycle.spotAllowed(ship.x(), ship.y())) {
            cycle.repick(ship.x(), ship.y());
            return;
        }
        int group = enemy.burrowGroup();
        if (group >= 0 && burrowWaits[group] > 0) {
            return;
        }
        EnemySpec.Burrow spec = enemy.spec().burrow().orElseThrow();
        if (group >= 0 && upWhenSurfacing(enemy, group, cycle.bulgeTicks()) >= spec.maxUp()) {
            return;
        }
        cycle.bulge();
        if (group >= 0) {
            burrowWaits[group] = SimStep.ticks(spec.staggerSeconds());
        }
        events.add(SimEvents.Type.BURROW_BULGE, enemy.x(), enemy.y(), enemy.kind());
    }

    /** M7 part B: how many other units of Burrower wave {@code group} will still be up {@code ticks} steps from now. */
    private int upWhenSurfacing(Enemy enemy, int group, int ticks) {
        int up = 0;
        for (int j = 0; j < enemies.size(); j++) {
            Enemy other = enemies.get(j);
            if (other != enemy && other.burrowGroup() == group && other.burrow().upFor() > ticks) {
                up++;
            }
        }
        return up;
    }
}
