package vanguard.sim;

/**
 * M11 part E, step E1 (design/enemies/air/choir-seraph, Formations: pairs; design/enemies, formation vocabulary {@code
 * pair}; m11e §4): the seeded draws of a cycling teleporter's wave. Each landing of a {@link
 * EnemySpec.Teleport.Arrival#MIRROR_ROW} unit draws its rows from its wave's seed and the landing's number, so both units
 * of a pair draw the same: the upper row {@link #UPPER} px below the top edge, the lower one {@link #GAP} px under it
 * (more than a hull's height: never overlapping); a wave's fixed {@link WaveSpec.Rows} replace the draw (a pair under a
 * Herald keeps its rows for its whole hold). Unit 0 takes the upper row (a single Seraph too), unit 1 the lower. At each
 * landing their spirals' arms start afresh: unit 1's first arm {@link #INTERLEAVE} after unit 0's (two arms each: the
 * four interleave, never stack). No state; allocates nothing.
 */
final class PairRows {
    /** The upper row's depth below the top edge, px (m11e §1 #3). */
    static final Range UPPER = new Range(110, 230);
    /** The lower row's distance under the upper one, px (at least 90: "two rows ≥ 90 px apart"). */
    static final Range GAP = new Range(90, 120);
    /** The least distance between a pair's two fixed rows, px. */
    static final double LEAST_GAP = 90;
    /** Unit 1's first arm after unit 0's, radians (90°). */
    static final double INTERLEAVE = StrictMath.PI / 2;

    private PairRows() {}

    /** The seed of the draws of the wave with index {@code wave} in its level. */
    static long seed(int wave) {
        return 0x3C6EF372FE94F82BL ^ (wave * 0x9E3779B97F4A7C15L);
    }

    /**
     * The row (depth below the top edge, px) of the unit at {@code place} in its wave at its landing {@code landing}
     * (0: its entry's), its wave seeded {@code seed}; {@code fixed} the wave's fixed rows (null: drawn).
     */
    static double row(long seed, int landing, int place, WaveSpec.Rows fixed) {
        boolean upper = place % 2 == 0;
        if (fixed != null) {
            return upper ? fixed.upper() : fixed.lower();
        }
        long draw = seed ^ ((landing + 1L) * 0xBF58476D1CE4E5B9L);
        double top = UPPER.at(Ramming.draw(draw ^ 0x510E527FADE682D1L));
        return upper ? top : top + GAP.at(Ramming.draw(draw ^ 0x9B05688C2B3E6C1FL));
    }

    /**
     * The first arm of the spiral of the unit at {@code place} in its wave from its landing {@code landing}, radians
     * counter-clockwise from the +x axis (y up): the wave's draw for that landing, plus {@link #INTERLEAVE} for unit 1.
     */
    static double arm(long seed, int landing, int place) {
        long draw = seed ^ ((landing + 1L) * 0x94D049BB133111EBL);
        double base = 2 * StrictMath.PI * Ramming.draw(draw ^ 0x1F83D9ABFB41BD6BL);
        return Math.IEEEremainder(base + (place % 2) * INTERLEAVE, 2 * StrictMath.PI);
    }
}
