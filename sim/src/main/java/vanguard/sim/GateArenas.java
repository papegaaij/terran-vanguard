package vanguard.sim;

/**
 * M11 part G, step G1 (design/campaign Level 49, the rush's arenas; m11g-gaps.md §1 #1): a boss rush's per-gate arenas,
 * kept by its {@link LevelClock} (made only for a rush: every other level's clock reads its one arena as before). Each
 * held gate has its own arena: its <b>crawl</b> (a gate section that {@link Section#crawls() crawls} keeps its ground
 * scrolling at its speed while the clock stands at the gate's halt, and eases into that speed over the gate instead of
 * to 0: the Brood Carrier's gate and the Dust Colossus's, 20 px/s), its <b>breaching</b> boss (the crawl's ease to still
 * while that boss is out of the sand, {@link BreachArena#groundStill}, in that gate only) and its <b>anchored</b> boss
 * (an arena boss lying on the ground, anchored so that it sits at its hover height when its gate's halt starts, from
 * the scroll the level will run from the moment the gate before it opens: the Iron Sovereign's halted station).
 * Nothing of its own changes in play (the clock's gate and crawl level are the state), so nothing hashes here.
 */
final class GateArenas {
    /** Per gate, the ground's crawl while its clock stands, px/s; 0 for a halt. */
    private final double[] crawl;
    /** Per gate, the breaching boss holding it; null for none. */
    private final SetPiece[] breach;
    /** Per gate, the arena boss anchored on its ground; null for none. */
    private final SetPiece[] anchored;
    /** Whether a gate holds a breaching boss (then the crawl's ease runs, {@link LevelClock#crawlFactor()}). */
    private final boolean breaching;

    /**
     * @param crawl per gate, its section's speed when it crawls, else 0
     * @param gatePiece per gate, the set piece holding it (an index into {@code pieces}); -1 for none
     */
    GateArenas(double[] crawl, int[] gatePiece, SetPiece[] pieces) {
        int gates = crawl.length;
        this.crawl = crawl.clone();
        breach = new SetPiece[gates];
        anchored = new SetPiece[gates];
        boolean any = false;
        for (int g = 0; g < gates; g++) {
            SetPiece piece = gatePiece[g] < 0 ? null : pieces[gatePiece[g]];
            if (piece == null) {
                continue;
            }
            if (piece.breach().isPresent()) {
                if (crawl[g] <= 0) {
                    throw new IllegalArgumentException("a rush's breaching boss holds a crawling gate: gate " + g);
                }
                breach[g] = piece;
                any = true;
            } else if (piece.arena().isPresent()) {
                if (crawl[g] > 0) {
                    throw new IllegalArgumentException("a rush's anchored boss holds a halted gate: gate " + g);
                }
                anchored[g] = piece;
            }
        }
        breaching = any;
    }

    /** Gate {@code g}'s crawl while its clock stands, px/s; 0 for a halt. */
    double crawl(int g) {
        return crawl[g];
    }

    /** Gate {@code g}'s breaching boss; null for none. */
    SetPiece breach(int g) {
        return breach[g];
    }

    /** Gate {@code g}'s anchored arena boss; null for none. */
    SetPiece anchored(int g) {
        return anchored[g];
    }

    /** Whether a gate holds a breaching boss. */
    boolean breaching() {
        return breaching;
    }
}
