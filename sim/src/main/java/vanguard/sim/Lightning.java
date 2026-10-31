package vanguard.sim;

import java.util.Arrays;

/**
 * M10 part A, step A1b (design/world/jovian, Hazards & set pieces: lightning strikes; {@link HazardSpec.Strikes}): the
 * level's told lightning strikes. At its time on the level clock a strike's column is told ({@link
 * SimEvents.Type#STRIKE_TOLD}; its x fixed now: its own, or the ship's for a tracking strike, moved off the ship's x as
 * the spec says), the tell running on the real steps; then it strikes ({@link SimEvents.Type#STRIKE}) and stays live for
 * its live time: it hits the ship and Rook once each while live ({@link SimEvents.Type#STRIKE_HIT}, in the sortie's hit
 * passes) and, as it strikes, every {@code air} enemy in the column (the kills paid as a special's: {@link
 * #strikeEnemies}). A restart brings every strike back; the boss checkpoint leaves the strikes before it spent. Owned by
 * the sortie only in a level with strikes ({@link Sortie#lightning()}), so the other levels hash as before. Fixed
 * arrays, allocates nothing.
 */
public final class Lightning implements Hashed {
    /** {@link SimEvents.Type#STRIKE_HIT}'s value and the hit mask: the ship. */
    static final int SHIP = 1;
    /** The hit mask: Rook ({@link SimEvents.Type#STRIKE_HIT}'s value 2). */
    static final int ROOK = 2;

    private static final int WAITING = -1;
    private static final int SPENT = -2;

    private final HazardSpec.Strikes spec;
    private final int[] starts;
    private final int tellTicks;
    private final int liveTicks;
    /** The waves' entry times on the level clock, steps (the tell's shift off the ship near one). */
    private final int[] entries;

    private final int entryTicks;
    /** The column's whole height, the hit box the hulls are tested against at mid-field. */
    private final Hitbox column;
    /** Per strike: steps since its tell started ({@link #WAITING} before, {@link #SPENT} after). */
    private final int[] ticks;
    /** Per strike: its column's x, fixed at its tell's start. */
    private final double[] xs;
    /** Per strike: whom it hit ({@link #SHIP}, {@link #ROOK}). */
    private final int[] hit;

    Lightning(HazardSpec.Strikes spec, LevelScript script) {
        this.spec = spec;
        int n = spec.at().size();
        starts = new int[n];
        for (int i = 0; i < n; i++) {
            starts[i] = SimStep.ticks(spec.at().get(i).t());
        }
        tellTicks = Math.max(1, SimStep.ticks(spec.tellSeconds()));
        liveTicks = Math.max(1, SimStep.ticks(spec.liveSeconds()));
        entries = script.waves().stream()
                .mapToInt(wave -> SimStep.ticks(wave.t()))
                .sorted()
                .toArray();
        entryTicks = SimStep.ticks(spec.entrySeconds());
        column = new Hitbox(spec.width(), 2 * PlayField.HEIGHT);
        ticks = new int[n];
        xs = new double[n];
        hit = new int[n];
        reset();
    }

    /** Back to the level start: every strike to come. */
    void reset() {
        Arrays.fill(ticks, WAITING);
        Arrays.fill(xs, 0);
        Arrays.fill(hit, 0);
    }

    /** Back to a boss checkpoint at {@code levelTick}: the strikes told before it are spent, the later ones to come. */
    void restore(int levelTick) {
        reset();
        for (int i = 0; i < starts.length; i++) {
            if (starts[i] < levelTick) {
                ticks[i] = SPENT;
            }
        }
    }

    /**
     * One step at {@code levelTick} with the ship at x {@code shipX}: a strike whose time came is told (its column fixed
     * now), a told one strikes after its tell, a live one is spent after its live time. Returns whether a strike struck
     * in this step (its enemies are hit by {@link #strikeEnemies}).
     */
    boolean step(int levelTick, double shipX, SimEvents events) {
        boolean struck = false;
        for (int i = 0; i < starts.length; i++) {
            if (ticks[i] == SPENT) {
                continue;
            }
            if (ticks[i] == WAITING) {
                if (levelTick >= starts[i]) {
                    ticks[i] = 0;
                    hit[i] = 0;
                    xs[i] = place(i, shipX);
                    events.add(SimEvents.Type.STRIKE_TOLD, xs[i], PlayField.HEIGHT, i);
                }
                continue;
            }
            ticks[i]++;
            if (ticks[i] == tellTicks) {
                events.add(SimEvents.Type.STRIKE, xs[i], PlayField.HEIGHT, i);
                struck = true;
            } else if (ticks[i] >= tellTicks + liveTicks) {
                ticks[i] = SPENT;
            }
        }
        return struck;
    }

    /**
     * The x of strike {@code i}'s column at its tell's start with the ship at {@code shipX}: its own (the ship's for a
     * tracking one), moved off the ship's x to the side it lies on (the other one where the field ends) to the spec's
     * clearance and, near a wave's entry, to half its width.
     */
    private double place(int i, double shipX) {
        HazardSpec.Strike strike = spec.at().get(i);
        double x = strike.track() ? shipX : strike.x();
        double clear = spec.clearance();
        if (nearEntry(starts[i])) {
            clear = Math.max(clear, spec.width() / 2);
        }
        double half = spec.width() / 2;
        if (clear > 0 && Math.abs(x - shipX) < clear) {
            double side = x > shipX || (x == shipX && shipX < PlayField.WIDTH / 2.0) ? 1 : -1;
            double moved = shipX + side * clear;
            if (moved < half || moved > PlayField.WIDTH - half) {
                moved = shipX - side * clear;
            }
            x = moved;
        }
        return Math.clamp(x, half, PlayField.WIDTH - half);
    }

    /** Whether a wave enters within the entry window of level step {@code tick}. */
    private boolean nearEntry(int tick) {
        for (int entry : entries) {
            if (Math.abs(entry - tick) <= entryTicks) {
                return true;
            }
        }
        return false;
    }

    /**
     * The strikes that struck in this step hit every {@code air} enemy in their column on the play field (not one that
     * cannot be hurt now): {@link HazardSpec.Strikes#enemyDamage()} each; a kill is paid as a special's ({@code hits},
     * mount -1). Returns the units counted as enemies it killed (M10 part B: the radio's {@code first-strike-kill}).
     */
    int strikeEnemies(Pool<Enemy> enemies, PlayerFire.Hits hits) {
        int kills = 0;
        for (int i = 0; i < starts.length; i++) {
            if (ticks[i] != tellTicks) {
                continue;
            }
            double half = spec.width() / 2;
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (enemy.layer() != Layer.AIR
                        || !ShotGeometry.onField(enemy)
                        || enemy.invulnerable()
                        || Math.abs(enemy.x() - xs[i]) > half + enemy.hitbox().width() / 2) {
                    continue;
                }
                if (enemy.damage(spec.enemyDamage())) {
                    kills += enemy.spec().countsAsEnemy() ? 1 : 0;
                    hits.enemyDestroyed(j, -1);
                }
            }
        }
        return kills;
    }

    /**
     * The first live strike that has not hit {@code who} ({@link #SHIP}, {@link #ROOK}) yet whose column touches
     * {@code hull} at (x, y); it is marked as hit. -1 for none.
     */
    int strikes(Hull hull, double x, double y, int who) {
        for (int i = 0; i < starts.length; i++) {
            if (live(i) && (hit[i] & who) == 0 && hull.overlaps(x, y, column, xs[i], PlayField.HEIGHT / 2.0)) {
                hit[i] |= who;
                return i;
            }
        }
        return -1;
    }

    /** A strike's damage to the ship or Rook. */
    double shipDamage() {
        return spec.shipDamage();
    }

    /** Its spec. */
    public HazardSpec.Strikes spec() {
        return spec;
    }

    /** The level's strikes in all. */
    public int count() {
        return starts.length;
    }

    /** Whether strike {@code i}'s column is told now (its tell running). */
    public boolean told(int i) {
        return ticks[i] >= 0 && ticks[i] < tellTicks;
    }

    /** Whether strike {@code i} strikes now (live). */
    public boolean live(int i) {
        return ticks[i] >= tellTicks;
    }

    /** Whether strike {@code i}'s column is told or live now (Rook keeps out of it). */
    public boolean threatens(int i) {
        return ticks[i] >= 0;
    }

    /** Strike {@code i}'s column's x (fixed at its tell's start). */
    public double x(int i) {
        return xs[i];
    }

    /** The tell's progress of strike {@code i}, 0 to 1 (its line brightening); 1 while live, 0 before and after. */
    public double charge(int i) {
        if (ticks[i] < 0) {
            return 0;
        }
        return live(i) ? 1 : (double) ticks[i] / tellTicks;
    }

    /** The column's width, px. */
    public double width() {
        return spec.width();
    }

    @Override
    public void addTo(StateHash hash) {
        for (int i = 0; i < starts.length; i++) {
            hash.add(ticks[i]).add(xs[i]).add(hit[i]);
        }
    }
}
