package vanguard.sim;

/**
 * Round 44 (step WR2): test hooks for the other modules' walker tests (the game's sounds and looks): a set piece's part
 * damaged or destroyed outright, as the simulation's own tests do it.
 */
public final class WalkerHooks {
    private WalkerHooks() {}

    /** Part {@code p} of set piece {@code k} takes {@code amount} (its multiplier applied); a part at 0 is not wrecked yet. */
    public static void damage(Sortie sortie, int k, int p, double amount) {
        sortie.setPiece(k).damagePart(p, amount);
    }

    /** Part {@code p} of set piece {@code k} is destroyed now (its vital part: the boss dies and its death plays). */
    public static void destroy(Sortie sortie, int k, int p) {
        sortie.setPiece(k).damagePart(p, 1e9);
        sortie.objectivePasses.wreck(k, p);
    }
}
