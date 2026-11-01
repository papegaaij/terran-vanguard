package vanguard.content;

import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;

/**
 * M10 part F, step F2 (design/enemies/bosses/honour-guard, The escort (Rook); m10f-gaps.md §1 row 8): the autopilot's
 * first cut against a squadron mid-boss (Level 40's Honour Guard), enough for step F2's measure of the duel: it lines up
 * under the nearest ace on the field (the forward guns on Harker and Vesper, the rear mount on Kestrel below it) at its
 * cruise height, each move planned among the bullets ({@link MovePlanner#bossMove}); while no ace is on the field it
 * holds the middle. Step F3 grows it (out of the told sniper line, off a told dive's line, "turn for Kestrel").
 */
final class GuardPlay {
    private GuardPlay() {}

    /** The level's squadron mid-boss while it fights (present, not yet destroyed); null otherwise. */
    static SetPiece squadronBoss(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.squadron().isPresent() && piece.present() && !piece.destroyed()) {
                return piece;
            }
        }
        return null;
    }

    /** The move against {@code guard}'s nearest ace on the field (the middle while none is). */
    static int move(Sortie sortie, SetPiece guard) {
        Squadron squadron = guard.squadron().orElseThrow();
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        int nearest = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int a = 0; a < squadron.aceCount(); a++) {
            if (!squadron.touchable(a)) {
                continue;
            }
            double distance = Math.hypot(squadron.x(a) - shipX, squadron.y(a) - shipY);
            if (distance < best) {
                best = distance;
                nearest = a;
            }
        }
        double targetX = nearest < 0 ? vanguard.sim.PlayField.WIDTH / 2.0 : squadron.x(nearest);
        return MovePlanner.bossMove(sortie, targetX, Autopilot.CRUISE_Y);
    }
}
