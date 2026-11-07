package vanguard.content;

import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * M11 part G, step G2 (Level 49's boss rush, design/enemies/bosses, Boss rush; m11g-gaps.md §1 #16): the autopilot's play
 * in a rush (see {@link Autopilot}). Each echo is played as its original: the Dust Colossus's by {@link ColossusPlay}, the
 * Iron Sovereign's by {@link RingPlay} (both found by their own keys, fight by fight); the Brood Carrier's here: its beam
 * under the open sac with the least HP (the pair a move's end opened), else under the core once the iris is open, else
 * waiting under the hull's middle for the next pair (any of them within a short slide); every move planned as {@link
 * RingPlay}'s among the bullets and the launched units, off the hull's body box ({@link ChimeraPlay#hazards}). Allocation-free and
 * stateless.
 */
final class RushPlay {
    private RushPlay() {}

    /** The Brood Carrier's echo present and alive in a rush; null without one. */
    static SetPiece carrierEcho(Sortie sortie) {
        if (!sortie.bosses().rush()) {
            return null;
        }
        for (int f = 0; f < sortie.bosses().fights(); f++) {
            SetPiece piece = sortie.setPiece(sortie.bosses().fightPiece(f));
            if (piece.present()
                    && !piece.destroyed()
                    && piece.spec().slug().equals("brood-carrier")
                    && piece.boss().flatMap(boss -> boss.echo()).isPresent()) {
                return piece;
            }
        }
        return null;
    }

    /** One step's move against the Carrier's echo (the fire bit is the caller's). */
    static int carrierMove(Sortie sortie, SetPiece piece) {
        double shipX = sortie.ship().x();
        double targetX = piece.renderX(1);
        int part = carrierTarget(piece, shipX);
        if (part >= 0) {
            targetX = ChimeraPlay.aimX(piece, part, shipX);
        }
        targetX = Math.clamp(targetX, ChimeraPlay.EDGE, PlayField.WIDTH - ChimeraPlay.EDGE);
        int best = 0;
        double least = Double.POSITIVE_INFINITY;
        for (int m = 0; m < MovePlanner.MOVES.length; m++) {
            double dx = MovePlanner.MOVE_X[m];
            double dy = MovePlanner.MOVE_Y[m];
            double cost = RingPlay.cost(sortie, piece, null, dx, dy, targetX, ChimeraPlay.STAND_Y, ChimeraPlay.COLUMN)
                    + ChimeraPlay.hazards(sortie, piece, dx, dy);
            if (cost < least) {
                least = cost;
                best = m;
            }
        }
        return MovePlanner.MOVES[best];
    }

    /** The part its beam goes for: an open sac with the least HP, else the core if it takes damage; -1 for none. */
    static int carrierTarget(SetPiece piece, double shipX) {
        int best = -1;
        double least = Double.POSITIVE_INFINITY;
        for (int p = 0; p < piece.partCount(); p++) {
            if (piece.partHp(p) <= 0
                    || piece.partWrecked(p)
                    || piece.spec().parts().get(p).vital()
                    || !piece.partOpen(p)
                    || Double.isNaN(ChimeraPlay.aimX(piece, p, shipX))) {
                continue;
            }
            double score = piece.partHp(p) + 0.01 * Math.abs(piece.partX(p) - shipX);
            if (score < least) {
                least = score;
                best = p;
            }
        }
        if (best >= 0) {
            return best;
        }
        int core = Autopilot.vitalPart(piece);
        return core >= 0 && !Double.isNaN(ChimeraPlay.aimX(piece, core, shipX)) ? core : -1;
    }
}
