package vanguard.game.render;

import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;

/**
 * M10 part F (step F6g; design/enemies/bosses/honour-guard, Death): the last ace's wreck after the kill, the one path its
 * hull, its cockpit fire and its 2 s chain of bursts share. The other aces' hulls tumble away down and out at the
 * simulation's speed ({@code AceFlight}: {@value #TUMBLE_SIDE} px/s to the nearer side, {@value #TUMBLE_DOWN} px/s down);
 * the last one tumbles the same way, slowed where it would leave the field before its chain ends, so it stays on the
 * field, its centre at least {@value #MARGIN} px inside, until the chain's last {@code large} burst takes it
 * ({@value #SHOWN_SECONDS} s after the kill). The piece itself is destroyed at the kill (not {@code present}); the hull is
 * presentation only (no hit, no contact). An instance follows one squadron: the kill's step, the last ace and where it
 * died ({@link #stepped}); the level screen's chain starts from the same point (the ace's position at the kill's step).
 */
public final class LastAceWreck {
    private static final long NEVER = Long.MIN_VALUE / 4;
    /** The simulation's tumble ({@code AceFlight.TUMBLE_SIDE}, {@code TUMBLE_DOWN}), px/s. */
    static final double TUMBLE_SIDE = 80;

    static final double TUMBLE_DOWN = 160;
    /** The chain round the hull before the credit shower (design: "a 2 s large chain"). */
    public static final double CHAIN_SECONDS = 2;
    /** The hull shows this long after the kill: its chain's last burst (at 2 s, {@code large}) covers it as it goes. */
    public static final double SHOWN_SECONDS = CHAIN_SECONDS + 0.1;
    /** Its centre stays this far inside the field's bottom and side edges while it shows (half its 72 px frame). */
    static final double MARGIN = 36;

    private int ace = -1;
    private long tick = NEVER;
    private double x0;
    private double y0;

    /** Before the kill (a new flight, or a retry from the boss checkpoint). */
    public void reset() {
        ace = -1;
        tick = NEVER;
    }

    /**
     * After a simulation step: at the step {@code piece} (the squadron's) is destroyed, its last ace and where it died;
     * returns whether this step is the kill.
     */
    public boolean stepped(Sortie sortie, SetPiece piece) {
        if (ace >= 0 || !piece.destroyed()) {
            return false;
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            int k = events.value(i);
            if (events.type(i) == SimEvents.Type.SET_PIECE_DESTROYED
                    && k >= 0
                    && k < sortie.setPieceCount()
                    && sortie.setPiece(k) == piece) {
                Squadron squadron = piece.squadron().orElseThrow();
                int a = ace(piece, squadron);
                if (a < 0) {
                    return false;
                }
                ace = a;
                tick = sortie.tick();
                x0 = squadron.renderX(a, 1);
                y0 = squadron.renderY(a, 1);
                return true;
            }
        }
        return false;
    }

    /** The last ace, or -1 before the kill. */
    public int ace() {
        return ace;
    }

    /** Where it died (play-field px, y up). */
    public double x0() {
        return x0;
    }

    public double y0() {
        return y0;
    }

    /** Its age after the kill at step {@code now} plus {@code alpha} of the next, s; NaN before the kill. */
    public double age(long now, float alpha) {
        return ace < 0 ? Double.NaN : (now - tick + alpha) * SimStep.SECONDS;
    }

    /** Whether its wreck shows now: from the kill until its chain's last burst takes it. */
    public boolean shows(long now, float alpha) {
        return ace >= 0 && shows(age(now, alpha));
    }

    /** Its wreck's centre now (play-field px, y up); NaN before the kill. */
    public double x(long now, float alpha) {
        return ace < 0 ? Double.NaN : x(x0, y0, age(now, alpha));
    }

    public double y(long now, float alpha) {
        return ace < 0 ? Double.NaN : y(x0, y0, age(now, alpha));
    }

    /** The ace whose death took the piece: the one hit last (its part's hit is the newest). */
    public static int ace(SetPiece piece, Squadron squadron) {
        int last = -1;
        int newest = Integer.MAX_VALUE;
        for (int a = 0; a < squadron.aceCount(); a++) {
            int since = piece.partTicksSinceHit(squadron.part(a));
            if (since < newest) {
                newest = since;
                last = a;
            }
        }
        return last;
    }

    /** Which side it tumbles to: -1 left (it died in the field's left half), +1 right; as the simulation's hulls. */
    static double side(double x0) {
        return x0 < PlayField.WIDTH / 2.0 ? -1 : 1;
    }

    /** The share of the simulation's tumble speed it falls at from ({@code x0}, {@code y0}): 1, less near an edge. */
    static double pace(double x0, double y0) {
        double down = Math.max(0, y0 - MARGIN) / SHOWN_SECONDS / TUMBLE_DOWN;
        double room = side(x0) < 0 ? x0 - MARGIN : PlayField.WIDTH - MARGIN - x0;
        double across = Math.max(0, room) / SHOWN_SECONDS / TUMBLE_SIDE;
        return Math.min(1, Math.min(down, across));
    }

    /** Its centre's x {@code age} s after the kill at ({@code x0}, {@code y0}) (play-field px). */
    public static double x(double x0, double y0, double age) {
        return x0 + side(x0) * TUMBLE_SIDE * pace(x0, y0) * Math.max(0, age);
    }

    /** Its centre's y {@code age} s after the kill (play-field px, y up). */
    public static double y(double x0, double y0, double age) {
        return y0 - TUMBLE_DOWN * pace(x0, y0) * Math.max(0, age);
    }

    /** Whether it shows {@code age} s after the kill. */
    public static boolean shows(double age) {
        return age >= 0 && age < SHOWN_SECONDS;
    }
}
