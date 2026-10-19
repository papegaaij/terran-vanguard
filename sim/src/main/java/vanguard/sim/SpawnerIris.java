package vanguard.sim;

/**
 * M5 part C: one periodic spawner's cycle (design/enemies/ground/hive-node): its wait between releases, its
 * iris that opens over the telegraph and shuts again, and the releases skipped while the ship is too close.
 * Each {@link Enemy} owns one, used only while its stat block has a spawner (its fields are hashed only then).
 */
final class SpawnerIris {
    /** Steps until its next release, -1 before its centre crossed the top edge. */
    private int spawnWait = -1;
    /** Steps since its iris started to open; -1 while it is shut. */
    private int irisTicks = -1;
    /** Whether the iris now opening releases its units at its end. */
    private boolean releasing;

    /** Back to a unit without a spawner. */
    void clear() {
        spawnWait = -1;
        irisTicks = -1;
        releasing = false;
    }

    /** Hashed only for a unit with a spawner. */
    void addTo(StateHash hash) {
        hash.add(spawnWait).add(irisTicks).add(releasing ? 1 : 0);
    }

    /**
     * One step of the cycle of {@code spawner} at (x, y), the ship at (shipX, shipY); see {@link
     * Enemy#spawnStep}.
     */
    int step(EnemySpec.Spawner spawner, double x, double y, double shipX, double shipY) {
        int telegraph = SimStep.ticks(spawner.telegraphSeconds());
        if (irisTicks >= 0 && ++irisTicks >= 2 * Math.max(1, telegraph)) {
            irisTicks = -1;
        }
        if (spawnWait < 0) {
            if (y > PlayField.HEIGHT) {
                return Enemy.SPAWN_NONE;
            }
            spawnWait = SimStep.ticks(spawner.everySeconds());
            return Enemy.SPAWN_NONE;
        }
        spawnWait--;
        double dx = shipX - x;
        double dy = shipY - y;
        boolean clear = dx * dx + dy * dy > spawner.shutWithin() * spawner.shutWithin() && y >= 0;
        int result = Enemy.SPAWN_NONE;
        if (spawnWait == telegraph) {
            releasing = clear;
            if (clear) {
                irisTicks = 0;
                result = Enemy.SPAWN_TELEGRAPH;
            }
        }
        if (spawnWait == 0) {
            spawnWait = SimStep.ticks(spawner.everySeconds());
            if (releasing && clear) {
                result = Enemy.SPAWN_RELEASE;
            }
            releasing = false;
        }
        return result;
    }

    /** How far the iris of {@code spawner} is open, 0 (shut) to 1 at the release; see {@link Enemy#iris()}. */
    double iris(EnemySpec.Spawner spawner) {
        if (irisTicks < 0) {
            return 0;
        }
        int telegraph = Math.max(1, SimStep.ticks(spawner.telegraphSeconds()));
        return irisTicks <= telegraph
                ? (double) irisTicks / telegraph
                : Math.max(0, (double) (2 * telegraph - irisTicks) / telegraph);
    }

    /** Whether its iris is opening for a release now: the spawn's telegraph. */
    boolean opening() {
        return releasing && irisTicks >= 0;
    }
}
