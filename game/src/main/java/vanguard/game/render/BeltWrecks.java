package vanguard.game.render;

import java.util.Arrays;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part F: the wrecks a level's ground units leave that the presentation keeps (the Warden Tank's, {@link
 * TankLooks}): each lies where its unit died, scrolls with the ground and rides a conveyor belt it lies on at the belt's
 * speed ({@link Sortie#beltSpeedAt}; the simulation keeps no ground wrecks, risk 7 of part F's gap check), until it
 * has left the screen at the bottom. A wreck carried off its belt's end stays where the belt left it.
 */
final class BeltWrecks {
    /** A wreck this far below the bottom edge has gone, px. */
    static final double GONE_BELOW = PlayField.HEIGHT / 4.0;

    /** Per slot its kind (-1 free), x, place on the ground (its screen y plus the scroll at its death), its frame. */
    private final int[] kind;

    private final double[] x;
    private final double[] groundY;
    private final int[] frame;
    /** How far a belt has carried each wreck up the screen since its death, px. */
    private final double[] carried;

    private int next;

    BeltWrecks(int slots) {
        kind = new int[slots];
        x = new double[slots];
        groundY = new double[slots];
        frame = new int[slots];
        carried = new double[slots];
        clear();
    }

    /** No wreck lies (the level restarts). */
    void clear() {
        Arrays.fill(kind, -1);
        next = 0;
    }

    /** A unit of {@code kind} died at (x, y) on the screen, the ground scrolled {@code scroll}: its wreck at {@code frame}. */
    void add(int kind, double x, double y, int frame, double scroll) {
        this.kind[next] = kind;
        this.x[next] = x;
        this.groundY[next] = y + scroll;
        this.frame[next] = frame;
        this.carried[next] = 0;
        next = (next + 1) % this.kind.length;
    }

    /** After each simulation step: each wreck on a belt rides it one step; a wreck past the bottom edge goes. */
    void stepped(Sortie sortie) {
        double scroll = sortie.groundScroll();
        for (int w = 0; w < kind.length; w++) {
            if (kind[w] < 0) {
                continue;
            }
            double y = y(w, scroll);
            if (y < -GONE_BELOW) {
                kind[w] = -1;
                continue;
            }
            carried[w] += sortie.beltSpeedAt(x[w], y) * SimStep.SECONDS;
        }
    }

    /** The slots. */
    int slots() {
        return kind.length;
    }

    /** Slot {@code w}'s kind, -1 when it is free. */
    int kind(int w) {
        return kind[w];
    }

    int frame(int w) {
        return frame[w];
    }

    double x(int w) {
        return x[w];
    }

    /** Slot {@code w}'s place on the screen, y up, the ground scrolled {@code scroll}: down with it, up its belt. */
    double y(int w, double scroll) {
        return groundY[w] - scroll + carried[w];
    }

    /** How far its belt has carried slot {@code w}'s wreck, px. */
    double carried(int w) {
        return carried[w];
    }

    /** The wrecks lying now. */
    int count() {
        int n = 0;
        for (int k : kind) {
            n += k >= 0 ? 1 : 0;
        }
        return n;
    }
}
