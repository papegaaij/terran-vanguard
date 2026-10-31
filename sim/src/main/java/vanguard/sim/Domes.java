package vanguard.sim;

/**
 * M10 part A (design/enemies/ground/shield-pylon, The dome): the Shield Pylons' domes, owned by a {@link Sortie} only
 * in a level with a pylon ({@link Sortie#domes()}). Each step, after the units moved and before the hit passes, it
 * marks every ground unit whose centre lies inside a standing dome {@link Enemy#covered() covered} (no damage from any
 * path: {@link Enemy#invulnerable()}); the pylons themselves and the units above the ground layer never are. A unit
 * entering or leaving a dome is covered or not at once. While an uncovered target is on the screen the seekers (homing
 * shots, Rook) skip the covered ones ({@link Enemy#shunned}).
 *
 * <p>A pylon's dome drops while the pylon is stunned by the EMP Burst and for its breaker time after a {@code
 * shield-breaker} hit on the pylon ({@link #breakerHit}); then it comes back over its back time (the flicker; still
 * down), {@link SimEvents.Type#DOME_DOWN} and {@link SimEvents.Type#DOME_UP} at its centre. The pylon's death (or its
 * leaving the screen) drops it for good ({@link SimEvents.Type#DOME_COLLAPSED}). Fixed capacity (a gate's pylons and
 * the next gate's), allocates nothing; hashed by its sortie.
 */
public final class Domes {
    /** The most pylons tracked at once. */
    static final int CAPACITY = 8;

    private final Pool<Enemy> enemies;
    private final SimEvents events;
    /** Per slot: its pylon's serial, -1 for a free slot. */
    private final int[] serials = new int[CAPACITY];
    /** Per slot: steps left of a breaker's drop. */
    private final int[] drop = new int[CAPACITY];
    /** Per slot: steps left of its coming back (the flicker); the dome is down until it ends. */
    private final int[] back = new int[CAPACITY];
    /** Per slot: whether the dome is down now. */
    private final boolean[] down = new boolean[CAPACITY];
    /** Per slot: its dome's centre and radius (the look, the collapse). */
    private final double[] xs = new double[CAPACITY];

    private final double[] ys = new double[CAPACITY];
    private final double[] radii = new double[CAPACITY];
    /** Per slot: whether its pylon was seen in this step's scan. */
    private final boolean[] seen = new boolean[CAPACITY];

    Domes(Pool<Enemy> enemies, SimEvents events) {
        this.enemies = enemies;
        this.events = events;
        reset();
    }

    /** Back to the level start: no pylon tracked. */
    void reset() {
        java.util.Arrays.fill(serials, -1);
        java.util.Arrays.fill(drop, 0);
        java.util.Arrays.fill(back, 0);
        java.util.Arrays.fill(down, false);
    }

    /**
     * The step's domes: the pylons on the screen (new ones tracked, gone ones collapsed), their drops and flickers,
     * then every unit's cover.
     */
    void step() {
        java.util.Arrays.fill(seen, false);
        for (int i = 0; i < enemies.size(); i++) {
            Enemy pylon = enemies.get(i);
            if (pylon.spec().dome().isEmpty()) {
                continue;
            }
            int slot = slotOf(pylon.serial());
            if (slot < 0) {
                slot = freeSlot();
                if (slot < 0) {
                    continue;
                }
                serials[slot] = pylon.serial();
                drop[slot] = 0;
                back[slot] = 0;
                down[slot] = false;
            }
            seen[slot] = true;
            EnemySpec.Dome dome = pylon.spec().dome().get();
            xs[slot] = dome.centreX(pylon.x());
            ys[slot] = dome.centreY(pylon.y());
            radii[slot] = dome.radius();
            boolean dropped = pylon.empStunned() || drop[slot] > 0;
            if (drop[slot] > 0) {
                drop[slot]--;
            }
            if (dropped) {
                back[slot] = SimStep.ticks(dome.backSeconds());
                if (!down[slot]) {
                    down[slot] = true;
                    events.add(SimEvents.Type.DOME_DOWN, xs[slot], ys[slot], pylon.kind());
                }
            } else if (back[slot] > 0) {
                back[slot]--;
            }
            if (down[slot] && !dropped && back[slot] == 0) {
                down[slot] = false;
                events.add(SimEvents.Type.DOME_UP, xs[slot], ys[slot], pylon.kind());
            }
        }
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (serials[slot] >= 0 && !seen[slot]) {
                // Its pylon is gone (destroyed or off the screen): the dome collapses for good.
                events.add(SimEvents.Type.DOME_COLLAPSED, xs[slot], ys[slot], 0);
                serials[slot] = -1;
            }
        }
        cover();
    }

    /** Every ground unit (not a pylon) whose centre lies in a standing dome is covered; the seekers' skip after. */
    private void cover() {
        boolean uncovered = false;
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            unit.covered = unit.layer() == Layer.GROUND && unit.spec().dome().isEmpty() && inStandingDome(unit);
            if (!unit.covered
                    && unit.spec().countsAsEnemy()
                    && !unit.spec().seekersIgnore()
                    && ShotGeometry.onField(unit)) {
                uncovered = true;
            }
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy unit = enemies.get(i);
            unit.shunned = unit.covered && uncovered;
        }
    }

    private boolean inStandingDome(Enemy unit) {
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (serials[slot] < 0 || down[slot]) {
                continue;
            }
            double dx = unit.x() - xs[slot];
            double dy = unit.y() - ys[slot];
            if (dx * dx + dy * dy <= radii[slot] * radii[slot]) {
                return true;
            }
        }
        return false;
    }

    /** A {@code shield-breaker} hit on {@code pylon} (a pylon or not): its dome drops for its breaker time. */
    void breakerHit(Enemy pylon) {
        if (pylon.spec().dome().isEmpty()) {
            return;
        }
        int slot = slotOf(pylon.serial());
        if (slot >= 0) {
            drop[slot] =
                    Math.max(drop[slot], SimStep.ticks(pylon.spec().dome().get().breakerDropSeconds()));
        }
    }

    /**
     * The glance event of a hit on {@code unit} that does it no damage: {@link SimEvents.Type#DOME_HIT} (a gold spark
     * on the dome) for a covered unit, {@link SimEvents.Type#SHOT_GLANCED} otherwise.
     */
    static SimEvents.Type glance(Enemy unit) {
        return unit.covered() ? SimEvents.Type.DOME_HIT : SimEvents.Type.SHOT_GLANCED;
    }

    private int slotOf(int serial) {
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (serials[slot] == serial) {
                return slot;
            }
        }
        return -1;
    }

    private int freeSlot() {
        return slotOf(-1);
    }

    /** The dome slots (some free: {@link #tracked}). */
    public int capacity() {
        return CAPACITY;
    }

    /** Whether slot {@code slot} holds a pylon's dome. */
    public boolean tracked(int slot) {
        return serials[slot] >= 0;
    }

    /** Its dome's centre across, px. */
    public double x(int slot) {
        return xs[slot];
    }

    /** Its dome's centre up, px. */
    public double y(int slot) {
        return ys[slot];
    }

    /** Its dome's radius, px. */
    public double radius(int slot) {
        return radii[slot];
    }

    /** Whether its dome stands (covers) now. */
    public boolean standing(int slot) {
        return serials[slot] >= 0 && !down[slot];
    }

    /** Steps left of its dome's coming back (the flicker); 0 when it stands or is still dropped. */
    public int backTicks(int slot) {
        return down[slot] ? back[slot] : 0;
    }

    /** The serial of its pylon; -1 for a free slot. */
    public int pylon(int slot) {
        return serials[slot];
    }

    void addTo(StateHash hash) {
        for (int slot = 0; slot < CAPACITY; slot++) {
            hash.add(serials[slot]).add(drop[slot]).add(back[slot]).add(down[slot] ? 1 : 0);
        }
    }
}
