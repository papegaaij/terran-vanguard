package vanguard.sim;

/**
 * M11 part D, step CD1 (design/campaign Level 46, the quiet's end; design/enemies/space/husk; m11d-gaps.md §1 #2, §4):
 * the level's crack cue ({@link VrellLevel.CrackCue}). At its time every unit on the screen with an armed crack is
 * taken in order, the farthest from the ship first (the order fixed then; {@link SimEvents.Type#CRACK_CUE}, value how
 * many), and cracks in its turn, the cue's gap apart, each with its own tell ({@link Cracking}); a unit cracking,
 * cracked or gone by its turn is skipped. Fixed arrays (an insertion order, no sort): allocation-free. Absent ({@code
 * null} in the sortie) in a level without a cue.
 */
final class CrackCues implements Hashed {
    /** The most units the cue takes (the farthest of them). */
    static final int MOST = 32;

    private final int cueTick;
    private final int gapTicks;
    private final int[] serials = new int[MOST];
    private final double[] distances = new double[MOST];
    private int size;
    /** The next turn's index; the turns before it are done. */
    private int next;

    private boolean started;

    CrackCues(VrellLevel.CrackCue cue) {
        cueTick = SimStep.ticks(cue.t());
        gapTicks = Math.max(1, SimStep.ticks(cue.gapSeconds()));
    }

    /** A new attempt: the cue to come. */
    void reset() {
        size = 0;
        next = 0;
        started = false;
    }

    /** One step at {@code levelTick}: the cue's start, then the turns that are due. */
    void step(int levelTick, EnemyForce force, Ship ship, SimEvents events) {
        if (!started) {
            if (levelTick < cueTick) {
                return;
            }
            started = true;
            take(force.enemies(), ship);
            events.add(SimEvents.Type.CRACK_CUE, ship.x(), ship.y(), size);
        }
        while (next < size && levelTick >= cueTick + next * gapTicks) {
            Enemy unit = find(force.enemies(), serials[next]);
            if (unit != null && unit.cracking.armed()) {
                force.tellCrack(unit);
            }
            next++;
        }
    }

    /** The armed cracking units on the screen, the farthest from the ship first. */
    private void take(Pool<Enemy> enemies, Ship ship) {
        size = 0;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            if (unit.spec().crack().isEmpty() || !unit.cracking.armed() || !ShotGeometry.onField(unit)) {
                continue;
            }
            double far = ShotGeometry.distanceSquared(unit.x(), unit.y(), ship.x(), ship.y());
            int at = size;
            while (at > 0
                    && (distances[at - 1] < far || (distances[at - 1] == far && serials[at - 1] > unit.serial()))) {
                at--;
            }
            if (at >= MOST) {
                continue;
            }
            int last = Math.min(size, MOST - 1);
            System.arraycopy(serials, at, serials, at + 1, last - at);
            System.arraycopy(distances, at, distances, at + 1, last - at);
            serials[at] = unit.serial();
            distances[at] = far;
            size = Math.min(size + 1, MOST);
        }
    }

    private static Enemy find(Pool<Enemy> enemies, int serial) {
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).serial() == serial) {
                return enemies.get(i);
            }
        }
        return null;
    }

    /** Whether the cue has started in this attempt. */
    boolean started() {
        return started;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(started ? 1 : 0).add(size).add(next);
        for (int k = 0; k < size; k++) {
            hash.add(serials[k]);
        }
    }
}
