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
    /** M11 part C: whether its alternate release has the next turn (hashed only for an Act 7 spawner). */
    private boolean alternateTurn;
    /** M11 part C: the telegraph of the release its iris opens (or closes) for, steps. */
    private int irisSpan;

    /** Back to a unit without a spawner. */
    void clear() {
        spawnWait = -1;
        irisTicks = -1;
        releasing = false;
        alternateTurn = false;
        irisSpan = 0;
    }

    /** Hashed only for a unit with a spawner; M11 part C's turn only for a spawner with its options. */
    void addTo(StateHash hash, EnemySpec.Spawner spawner) {
        hash.add(spawnWait).add(irisTicks).add(releasing ? 1 : 0);
        if (spawner.actSeven()) {
            hash.add(alternateTurn ? 1 : 0);
        }
    }

    /**
     * One step of the cycle of {@code spawner} at (x, y), the ship at (shipX, shipY); see {@link
     * Enemy#spawnStep}.
     */
    int step(EnemySpec.Spawner spawner, double x, double y, double shipX, double shipY) {
        int telegraph = SimStep.ticks(spawner.release(alternateTurn).telegraphSeconds());
        if (irisTicks >= 0 && ++irisTicks >= 2 * Math.max(1, irisSpan)) {
            irisTicks = -1;
        }
        if (spawnWait < 0) {
            if (y > PlayField.HEIGHT) {
                return Enemy.SPAWN_NONE;
            }
            // M11 part C: its first release after its own time (a whole cycle without one).
            spawnWait = SimStep.ticks(
                    Double.isNaN(spawner.firstSeconds()) ? spawner.everySeconds() : spawner.firstSeconds());
            return Enemy.SPAWN_NONE;
        }
        double dx = shipX - x;
        double dy = shipY - y;
        boolean clear = dx * dx + dy * dy > spawner.shutWithin() * spawner.shutWithin() && y >= 0;
        if (spawner.hold()) {
            return held(spawner, telegraph, clear);
        }
        spawnWait--;
        int result = Enemy.SPAWN_NONE;
        if (spawnWait == telegraph) {
            releasing = clear;
            if (clear) {
                irisTicks = 0;
                irisSpan = telegraph;
                result = Enemy.SPAWN_TELEGRAPH;
            }
        }
        if (spawnWait == 0) {
            spawnWait = SimStep.ticks(spawner.everySeconds());
            if (releasing && clear) {
                result = Enemy.SPAWN_RELEASE;
                alternateTurn = spawner.alternate().isPresent() && !alternateTurn;
            }
            releasing = false;
        }
        return result;
    }

    /**
     * M11 part C (design/enemies/ground/spawning-organ: "holds a release while the ship is within 120 px"): a due
     * opening waits shut, and a due release waits with its iris open, while the ship is too close (never skipped); the
     * next release is due a whole cycle after the late one.
     */
    private int held(EnemySpec.Spawner spawner, int telegraph, boolean clear) {
        int result = Enemy.SPAWN_NONE;
        if (!releasing) {
            if (spawnWait > telegraph) {
                spawnWait--;
            }
            if (spawnWait > telegraph || !clear) {
                return Enemy.SPAWN_NONE;
            }
            releasing = true;
            irisTicks = 0;
            irisSpan = telegraph;
            result = Enemy.SPAWN_TELEGRAPH;
            if (telegraph > 0) {
                return result;
            }
        }
        if (spawnWait > 0) {
            spawnWait--;
        }
        if (spawnWait > 0) {
            return result;
        }
        if (!clear) {
            // Held open: the iris stays at its widest until the ship is off.
            irisTicks = irisSpan;
            return result;
        }
        spawnWait = SimStep.ticks(spawner.everySeconds());
        releasing = false;
        alternateTurn = spawner.alternate().isPresent() && !alternateTurn;
        return Enemy.SPAWN_RELEASE;
    }

    /** How far its iris is open, 0 (shut) to 1 at the release; see {@link Enemy#iris()}. */
    double iris() {
        if (irisTicks < 0) {
            return 0;
        }
        int telegraph = Math.max(1, irisSpan);
        return irisTicks <= telegraph
                ? (double) irisTicks / telegraph
                : Math.max(0, (double) (2 * telegraph - irisTicks) / telegraph);
    }

    /**
     * M11 part C: the release of {@code spawner} whose turn it is now (its own, or its alternate's).
     */
    EnemySpec.Spawner release(EnemySpec.Spawner spawner) {
        return spawner.release(alternateTurn);
    }

    /** M11 part C: whether its opening iris is a whirl cluster's swell (the pod's), not an iris's. */
    boolean swelling(EnemySpec.Spawner spawner) {
        return opening() && spawner.release(alternateTurn).cluster();
    }

    /** Whether its iris is opening for a release now: the spawn's telegraph. */
    boolean opening() {
        return releasing && irisTicks >= 0;
    }
}
