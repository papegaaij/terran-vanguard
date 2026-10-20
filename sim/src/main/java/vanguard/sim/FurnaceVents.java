package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * M7 part F, the furnace vents (design/campaign Level 19, Hazards; Level 05's {@link Sled} is the model): rows of
 * grates on the ground ({@link HazardSpec.VentRow}), each entering at the top edge at its time and scrolling with the
 * ground. From its time on, every period, a row fires as a ripple: each grate glows for its tell, then its flame column
 * stands for its burn, hitting the ship and Rook whatever their layer, once per burst each. The cycle follows the level
 * clock, so a retry replays it exactly; the vents are cold from the arena on (the level clock at the arena section's
 * start) and once a row has left the bottom edge.
 *
 * <p>The grates are numbered through the level, row by row in time order ({@link #grate(int, int)}); the presentation
 * reads each one's {@link #phase(int)}, {@link #progress(int)} and place.
 */
public final class FurnaceVents implements Hashed {
    /** Where a grate is in its cycle. */
    public enum Phase {
        /** Cold (or between bursts). */
        IDLE,
        /** Glowing orange to white before its flame: the tell. */
        TELL,
        /** Its flame column stands: it hits. */
        BURN
    }

    /** {@link #strike}: the ship. */
    static final int SHIP = 1;
    /** {@link #strike}: Rook. */
    static final int ROOK = 2;

    private final HazardSpec.VentRow[] rows;
    private final int[] rowTicks;
    private final int[] periodTicks;
    private final int[] tellTicks;
    private final int[] burnTicks;
    private final int[] rippleTicks;
    /** The first grate's number of each row. */
    private final int[] firstGrate;
    /** Each grate's row. */
    private final int[] grateRow;

    private final Hitbox[] boxes;
    /** The level clock's tick from which the vents are cold (the arena's start); {@link Integer#MAX_VALUE} without one. */
    private final int coldTicks;
    /** The ground's scroll when each row entered at the top edge; NaN before. */
    private final double[] entryScroll;
    /** What each grate's current burst hit ({@link #SHIP}, {@link #ROOK}). */
    private final int[] hits;

    private int tick;
    private double scroll;

    /**
     * @param ventRows the level's rows, in time order
     * @param coldFromTicks the level clock's tick from which they are cold (the arena section's start); {@link
     *     Integer#MAX_VALUE} for never
     */
    FurnaceVents(List<HazardSpec.VentRow> ventRows, int coldFromTicks) {
        rows = ventRows.toArray(HazardSpec.VentRow[]::new);
        int n = rows.length;
        rowTicks = new int[n];
        periodTicks = new int[n];
        tellTicks = new int[n];
        burnTicks = new int[n];
        rippleTicks = new int[n];
        firstGrate = new int[n];
        entryScroll = new double[n];
        int grates = 0;
        for (int r = 0; r < n; r++) {
            HazardSpec.VentRow row = rows[r];
            rowTicks[r] = SimStep.ticks(row.t());
            periodTicks[r] = Math.max(1, SimStep.ticks(row.periodSeconds()));
            tellTicks[r] = SimStep.ticks(row.tellSeconds());
            burnTicks[r] = Math.max(1, SimStep.ticks(row.burnSeconds()));
            rippleTicks[r] = SimStep.ticks(row.rippleSeconds());
            firstGrate[r] = grates;
            grates += row.grates();
        }
        grateRow = new int[grates];
        boxes = new Hitbox[n];
        for (int r = 0; r < n; r++) {
            Arrays.fill(grateRow, firstGrate[r], firstGrate[r] + rows[r].grates(), r);
            boxes[r] = new Hitbox(rows[r].width(), rows[r].width());
        }
        hits = new int[grates];
        coldTicks = coldFromTicks;
        reset();
    }

    /** Back to the level start. */
    void reset() {
        Arrays.fill(entryScroll, Double.NaN);
        Arrays.fill(hits, 0);
        tick = 0;
        scroll = 0;
    }

    /**
     * Back to a boss checkpoint at {@code levelTick} with the ground scrolled {@code groundScroll}: the rows entered
     * after it enter again; no burst has hit yet.
     */
    void restore(int levelTick, double groundScroll) {
        for (int r = 0; r < rows.length; r++) {
            if (rowTicks[r] > levelTick) {
                entryScroll[r] = Double.NaN;
            }
        }
        Arrays.fill(hits, 0);
        tick = levelTick;
        scroll = groundScroll;
    }

    /**
     * Places the rows and their cycles for the level clock's {@code levelTick} and the ground's {@code groundScroll}
     * (after this step's scroll); a row enters at the top edge once the clock reaches its time. When the clock ticked
     * ({@code clockTicked}), a grate whose tell or burn starts now raises {@link SimEvents.Type#VENT_TELL} or {@link
     * SimEvents.Type#VENT_BURST} at it.
     */
    void update(int levelTick, double groundScroll, boolean clockTicked, SimEvents events) {
        tick = levelTick;
        scroll = groundScroll;
        for (int r = 0; r < rows.length; r++) {
            if (Double.isNaN(entryScroll[r]) && levelTick >= rowTicks[r]) {
                entryScroll[r] = groundScroll;
            }
        }
        if (!clockTicked) {
            return;
        }
        for (int g = 0; g < grateRow.length; g++) {
            int r = grateRow[g];
            int into = cycle(g);
            if (into < 0) {
                continue;
            }
            if (into == 0 && tellTicks[r] > 0) {
                events.add(SimEvents.Type.VENT_TELL, x(g), y(g), g);
            } else if (into == tellTicks[r]) {
                hits[g] = 0;
                events.add(SimEvents.Type.VENT_BURST, x(g), y(g), g);
            }
        }
    }

    /**
     * Steps of grate {@code g}'s current burst so far, from its tell's start; -1 while it is cold or between bursts.
     */
    private int cycle(int g) {
        int r = grateRow[g];
        if (Double.isNaN(entryScroll[r]) || tick >= coldTicks || !onField(g)) {
            return -1;
        }
        int k = tick - rowTicks[r];
        if (k < 0) {
            return -1;
        }
        int into = k % periodTicks[r] - (g - firstGrate[r]) * rippleTicks[r];
        return into >= 0 && into < tellTicks[r] + burnTicks[r] ? into : -1;
    }

    /** Whether grate {@code g} has not left the bottom edge yet (its box's top above it; it enters at the top edge). */
    private boolean onField(int g) {
        return y(g) + boxes[grateRow[g]].height() / 2 > 0;
    }

    /** The level's grates in all. */
    public int grates() {
        return grateRow.length;
    }

    /** The number of grate {@code i} of row {@code row} (rows in time order) through the level. */
    public int grate(int row, int i) {
        return firstGrate[row] + i;
    }

    /** Grate {@code g}'s row (an index into the level's {@link HazardSpec#vents()}). */
    public int row(int g) {
        return grateRow[g];
    }

    /** Whether grate {@code g} is on the screen: its row entered at the top edge and has not left the bottom edge. */
    public boolean present(int g) {
        return !Double.isNaN(entryScroll[grateRow[g]]) && onField(g);
    }

    /** Grate {@code g}'s centre, px from the left. */
    public double x(int g) {
        return rows[grateRow[g]].x(g - firstGrate[grateRow[g]]);
    }

    /**
     * Grate {@code g}'s centre, px up from the bottom edge: just above the top edge when its row enters, then with the
     * ground; above the top edge before.
     */
    public double y(int g) {
        int r = grateRow[g];
        double entered = Double.isNaN(entryScroll[r]) ? 0 : scroll - entryScroll[r];
        return PlayField.HEIGHT + rows[r].width() / 2 - entered;
    }

    /** Grate {@code g}'s box (the flame column's footprint), its row's width square. */
    public Hitbox box(int g) {
        return boxes[grateRow[g]];
    }

    /** Where grate {@code g} is in its cycle now. */
    public Phase phase(int g) {
        int into = cycle(g);
        if (into < 0) {
            return Phase.IDLE;
        }
        return into < tellTicks[grateRow[g]] ? Phase.TELL : Phase.BURN;
    }

    /** How far grate {@code g}'s tell or burn has run, 0–1; 0 while it is cold. */
    public double progress(int g) {
        int into = cycle(g);
        if (into < 0) {
            return 0;
        }
        int r = grateRow[g];
        return into < tellTicks[r] ? (double) into / tellTicks[r] : (double) (into - tellTicks[r]) / burnTicks[r];
    }

    /** Whether a grate is in its tell or its burn now (Rook keeps clear of it). */
    boolean hot(int g) {
        return cycle(g) >= 0;
    }

    /** A hit's damage of grate {@code g}'s flame. */
    double damage(int g) {
        return rows[grateRow[g]].damage();
    }

    /**
     * The first grate from {@code from} on whose burning flame touches {@code hull} at (hx, hy) and has not hit {@code
     * who} ({@link #SHIP}, {@link #ROOK}) in this burst; it is marked as hit. -1 for none.
     */
    int strike(Hull hull, double hx, double hy, int who, int from) {
        for (int g = from; g < grateRow.length; g++) {
            if ((hits[g] & who) != 0) {
                continue;
            }
            int into = cycle(g);
            if (into < tellTicks[grateRow[g]] || !hull.overlaps(hx, hy, box(g), x(g), y(g))) {
                continue;
            }
            hits[g] |= who;
            return g;
        }
        return -1;
    }

    @Override
    public void addTo(StateHash hash) {
        for (double entered : entryScroll) {
            hash.add(Double.isNaN(entered) ? -1 : entered);
        }
        for (int hit : hits) {
            hash.add(hit);
        }
    }
}
