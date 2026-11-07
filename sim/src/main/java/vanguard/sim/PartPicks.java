package vanguard.sim;

import java.util.List;

/**
 * M10 part H, step H1b (design/enemies/bosses/ascendant, The escort (Rook); m10h-gaps.md §1 #12): Rook's picks on a
 * boss with per-part mechanics (a fed shield, mirror panels or regenerating parts: the Ascendant). In phase 1
 * the feeders of a fed shield first (the generators), then a mirrored part from its open side if he is there (a panel
 * that would meet his shot absorbs it: never his target), then the rest (the cannon); while regenerating parts are up
 * the one with the least HP (his focus denies its healing), the boss-launched units beside him as much (the Chimeras in
 * his reach); after the hull's break nothing (round 80: the lifeboat is scenery). Ranks as {@link Wingman}'s (lower first; the nearest among
 * equals). Every other boss keeps his nearest-part pick. Allocates nothing.
 *
 * <p>M11 part F, step F1 (design/enemies/bosses/vornes-chimera, The escort (Rook); m11f-gaps.md §1 #7): in a phase with
 * one of the boss's {@link BossSpec.RookOrder}s its order goes first: the first of its parts he can take (living,
 * touchable, not a panel facing him) is {@link #FIRST}, its other parts {@link #OPEN}, every other part {@link #REST} (a
 * panel facing him still never his target). A phase without an order keeps the rules above (the weakest sac).
 */
final class PartPicks {
    /** Never his target (a panel facing him). */
    static final int SKIP = -1;
    /** His pick among the boss's parts. */
    static final int FIRST = 1;
    /** An open battery. */
    static final int OPEN = 2;
    /** Any other part. */
    static final int REST = 3;

    private PartPicks() {}

    /** Whether {@code piece} takes these picks. */
    static boolean applies(SetPiece piece) {
        if (piece.boss().isEmpty()) {
            return false;
        }
        BossSpec boss = piece.boss().get();
        return !boss.fedShields().isEmpty()
                || piece.mirrors != null
                || piece.regens != null
                || !boss.rookOrders().isEmpty();
    }

    /** Rook's rank for part {@code p} of {@code piece} from (rx, ry); {@link #SKIP}: not his target. */
    static int rank(SetPiece piece, int p, double rx, double ry) {
        if (piece.mirrors != null
                && piece.mirrors.fitted(p)
                && piece.mirrors.meets(p, piece.partX(p) - rx, piece.partY(p) - ry)) {
            return SKIP;
        }
        BossSpec.RookOrder order = order(piece);
        if (order != null) {
            // M11 part F, step F1: the phase's order (the Dart bay, then the rack, then a battery from its open side).
            List<Integer> listed = order.parts();
            if (p == first(piece, listed, rx, ry)) {
                return FIRST;
            }
            return listed.contains(p) ? OPEN : REST;
        }
        if (piece.regens != null && piece.regens.fitted(p)) {
            return p == weakest(piece) ? FIRST : REST;
        }
        List<FedShield> fed = piece.boss().orElseThrow().fedShields();
        for (int i = 0; i < fed.size(); i++) {
            if (fed.get(i).feeders().contains(p)) {
                return FIRST;
            }
        }
        return piece.mirrors != null && piece.mirrors.fitted(p) ? OPEN : REST;
    }

    /** M11 part F, step F1: the boss's order for its phase now; null for none. */
    static BossSpec.RookOrder order(SetPiece piece) {
        List<BossSpec.RookOrder> orders = piece.boss().orElseThrow().rookOrders();
        int phase = piece.phase();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).phase() == phase) {
                return orders.get(i);
            }
        }
        return null;
    }

    /**
     * M11 part F, step F1: the first of {@code listed} he can take from (rx, ry): living, touchable and not a panel facing
     * him; -1 for none.
     */
    static int first(SetPiece piece, List<Integer> listed, double rx, double ry) {
        for (int i = 0; i < listed.size(); i++) {
            int p = listed.get(i);
            if (piece.partWrecked(p) || piece.partShielded(p)) {
                continue;
            }
            if (piece.mirrors != null
                    && piece.mirrors.fitted(p)
                    && piece.mirrors.meets(p, piece.partX(p) - rx, piece.partY(p) - ry)) {
                continue;
            }
            return p;
        }
        return -1;
    }

    /** The living, touchable regenerating part with the least HP (the first of equals); -1 for none. */
    static int weakest(SetPiece piece) {
        int best = -1;
        for (int p = 0; p < piece.partCount(); p++) {
            if (!piece.regens.fitted(p) || piece.partWrecked(p) || piece.partShielded(p)) {
                continue;
            }
            if (best < 0 || piece.partHp(p) < piece.partHp(best)) {
                best = p;
            }
        }
        return best;
    }

    /** Whether one of {@code setPieces} has its regenerating parts up now (Rook takes the brood beside him). */
    static boolean broodUp(SetPiece[] setPieces) {
        for (SetPiece piece : setPieces) {
            if (piece.regens != null
                    && piece.present()
                    && !piece.destroyed()
                    && (piece.escape == null || !piece.escape.broken())
                    && weakest(piece) >= 0) {
                return true;
            }
        }
        return false;
    }
}
