package vanguard.game.render;

import java.util.Arrays;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;

/**
 * M11 part F, step F6 (design/enemies/bosses/vornes-chimera, Tells: "the parts' own guns: each part's muzzle flash as
 * it fires"): which of the Chimera's parts fired, from the simulation's shot events. A part's gun fires from its centre
 * ({@code SetPieceGuns}, {@code RiseFire}: an aimed burst or a fan's {@link SimEvents.Type#ENEMY_FIRED}, the rack's
 * homing diamond's {@link SimEvents.Type#MISSILE_LAUNCHED}, the spine sac's bile shot's {@link
 * SimEvents.Type#MORTAR_LOBBED}); the shot is the living part's whose centre lies within {@value #REACH} px of it. A dead
 * part fires nothing, so its flash stops with its gun. Presentation only; nothing is allocated per step once the parts
 * are counted.
 */
final class ChimeraFire {
    /** A shot this near a part's centre is its gun's, px (the piece moves 3.4 px a step at most). */
    static final double REACH = 8;
    /** A part's flash: the port flash's four frames, two steps each. */
    static final int FLASH_TICKS = 8;

    private static final long NEVER = Long.MIN_VALUE / 2;

    /** Per part the step its gun last fired, NEVER before. */
    private long[] fired = new long[0];

    /** The level restarts (or the fight is retried): no flash. */
    void reset() {
        Arrays.fill(fired, NEVER);
    }

    /** After a simulation step at {@code tick}: the parts whose guns fired in it. */
    void stepped(SetPiece piece, SimEvents events, long tick) {
        if (fired.length != piece.partCount()) {
            fired = new long[piece.partCount()];
            Arrays.fill(fired, NEVER);
        }
        if (tick < maxFired()) {
            // A retry from the boss checkpoint.
            reset();
        }
        for (int i = 0; i < events.size(); i++) {
            SimEvents.Type type = events.type(i);
            if (type == SimEvents.Type.ENEMY_FIRED
                    || type == SimEvents.Type.MISSILE_LAUNCHED
                    || type == SimEvents.Type.MORTAR_LOBBED) {
                int p = firer(piece, events.x(i), events.y(i));
                if (p >= 0) {
                    fired[p] = tick;
                }
            }
        }
    }

    private long maxFired() {
        long most = NEVER;
        for (long t : fired) {
            most = Math.max(most, t);
        }
        return most;
    }

    /** The living part of {@code piece} whose gun fired a shot at (x, y): the nearest within {@value #REACH} px; -1. */
    static int firer(SetPiece piece, double x, double y) {
        int best = -1;
        double nearest = REACH * REACH;
        for (int p = 0; p < piece.partCount(); p++) {
            if (piece.partHp(p) <= 0) {
                continue;
            }
            double dx = piece.partX(p) - x;
            double dy = piece.partY(p) - y;
            double d = dx * dx + dy * dy;
            if (d <= nearest) {
                nearest = d;
                best = p;
            }
        }
        return best;
    }

    /** The steps since part {@code p} last fired at {@code tick}; -1 when its flash is over (or it never fired). */
    int flashTicks(int p, long tick) {
        if (p < 0 || p >= fired.length || fired[p] == NEVER) {
            return -1;
        }
        long since = tick - fired[p];
        return since >= 0 && since < FLASH_TICKS ? (int) since : -1;
    }
}
