package vanguard.sim;

import java.util.Arrays;

/**
 * M11 part H, step H1b (design/enemies/bosses/choir-heart, the membranes; design/enemies, Window shields; m11h-gaps.md
 * §1 #1): a boss's part windows ({@link PartWindowsSpec}), kept by its {@link SetPiece} behind one accessor ({@link
 * SetPiece#partWindows()}), made only for a boss with them. A windowed part is <b>closed</b> ({@link #closed}: the part
 * is shielded, {@link SetPiece#partShielded}, so Rook, the Swarm Call and the seekers skip it and shots glance off it)
 * except while it sings ({@link #sing}: its playbook move's tell's first frame to the move's last bullet, told by the
 * boss's {@link BossPlaybook}), while a {@code shield-breaker} hit's hold lasts ({@link #breaker}: the Tesla arc, which
 * may pick a closed part when nothing else is in reach) and while the EMP Burst's does ({@link #emp}: every part).
 * {@link SimEvents.Type#PART_WINDOW_OPEN} and {@link SimEvents.Type#PART_WINDOW_CLOSE} (value the part) as a living part
 * opens and closes; the iris (the look) draws back and closes over {@link #IRIS_SECONDS}. Its state is hashed with its
 * piece's. Allocates nothing once made.
 */
public final class PartWindows {
    /** The iris's draw back or close, s (the look; the window itself opens on its first frame). */
    public static final double IRIS_SECONDS = 0.25;

    private final PartWindowsSpec spec;
    private final SetPiece piece;
    /** Per part, whether it is windowed. */
    private final boolean[] fitted;

    private final int breakerTicks;
    private final int empTicks;
    private final int irisTicks;

    // The state (hashed).
    /** Per part, whether it sings now (its playbook move's tell to its last bullet). */
    private final boolean[] singing;
    /** Per part, the steps left of a breaker's or the EMP's hold. */
    private final int[] hold;
    /** Per part, whether it was open at the end of its last step (its open and close events). */
    private final boolean[] open;
    /** Per part, the iris's steps toward open, 0 (closed) to the iris time. */
    private final int[] iris;

    PartWindows(PartWindowsSpec spec, SetPiece piece, int parts) {
        this.spec = spec;
        this.piece = piece;
        fitted = new boolean[parts];
        for (int p : spec.parts()) {
            if (p >= parts) {
                throw new IllegalArgumentException("a part window is one of the boss's parts: " + p);
            }
            fitted[p] = true;
        }
        breakerTicks = SimStep.ticks(spec.breakerSeconds());
        empTicks = SimStep.ticks(spec.empSeconds());
        irisTicks = Math.max(1, SimStep.ticks(IRIS_SECONDS));
        singing = new boolean[parts];
        hold = new int[parts];
        open = new boolean[parts];
        iris = new int[parts];
    }

    /** Back to the level start (and a checkpoint): every window closed, nothing held. */
    void reset() {
        Arrays.fill(singing, false);
        Arrays.fill(hold, 0);
        Arrays.fill(open, false);
        Arrays.fill(iris, 0);
    }

    /** The playbook: part {@code p} starts ({@code on}) or stops singing; nothing for a part without a window. */
    void sing(int p, boolean on) {
        if (p >= 0 && fitted[p]) {
            singing[p] = on;
        }
    }

    /** The playbook's new phase or reset: no part sings. */
    void silence() {
        Arrays.fill(singing, false);
    }

    /**
     * Step PT2 ({@link BossLulls}): a lull opens every living windowed part for {@code ticks} steps (as a hold: the
     * parts stay hittable while the boss rests).
     */
    void lull(int ticks) {
        for (int p = 0; p < fitted.length; p++) {
            if (fitted[p] && piece.partHp(p) > 0) {
                hold[p] = Math.max(hold[p], Math.max(1, ticks));
            }
        }
    }

    /** A {@code shield-breaker} hit on part {@code p}: it opens at once for the breaker's hold (restarting a running one). */
    void breaker(int p) {
        if (fitted[p]) {
            hold[p] = Math.max(hold[p], Math.max(1, breakerTicks));
        }
    }

    /** The EMP Burst: every windowed part opens at once for the EMP's hold. */
    void emp() {
        for (int p = 0; p < fitted.length; p++) {
            if (fitted[p]) {
                hold[p] = Math.max(hold[p], Math.max(1, empTicks));
            }
        }
    }

    /**
     * One step (after the boss acted): the holds count down; {@link SimEvents.Type#PART_WINDOW_OPEN} or {@link
     * SimEvents.Type#PART_WINDOW_CLOSE} through {@code actions} as a living part's window changes; the irises follow.
     */
    void step(BossActions actions) {
        for (int p = 0; p < fitted.length; p++) {
            if (!fitted[p]) {
                continue;
            }
            if (hold[p] > 0) {
                hold[p]--;
            }
            boolean now = isOpen(p) && piece.partHp(p) > 0;
            if (now != open[p]) {
                open[p] = now;
                actions.ring(
                        now ? SimEvents.Type.PART_WINDOW_OPEN : SimEvents.Type.PART_WINDOW_CLOSE,
                        piece.partX(p),
                        piece.partY(p),
                        p);
            }
            iris[p] = now ? Math.min(irisTicks, iris[p] + 1) : Math.max(0, iris[p] - 1);
        }
    }

    private boolean isOpen(int p) {
        return singing[p] || hold[p] > 0;
    }

    /** Whether part {@code p} has a window. */
    public boolean fitted(int p) {
        return fitted[p];
    }

    /** Whether part {@code p} has a window and it is closed now (the part takes nothing, no one targets it). */
    public boolean closed(int p) {
        return fitted[p] && !isOpen(p);
    }

    /** Whether part {@code p} has a window and it is open now (it sings or a hold keeps it open). */
    public boolean open(int p) {
        return fitted[p] && isOpen(p);
    }

    /** Whether part {@code p} sings now (its move's tell to its last bullet). */
    public boolean singing(int p) {
        return singing[p];
    }

    /** Whether a breaker's or the EMP's hold keeps part {@code p} open now. */
    public boolean held(int p) {
        return hold[p] > 0;
    }

    /** Steps left of part {@code p}'s hold (0 without one). */
    public int holdTicks(int p) {
        return hold[p];
    }

    /** How far part {@code p}'s iris is drawn back, 0 (closed) to 1 (open). */
    public double iris(int p) {
        return (double) iris[p] / irisTicks;
    }

    public PartWindowsSpec spec() {
        return spec;
    }

    void addTo(StateHash hash) {
        for (int p = 0; p < fitted.length; p++) {
            if (fitted[p]) {
                hash.add(singing[p] ? 1 : 0).add(hold[p]).add(open[p] ? 1 : 0).add(iris[p]);
            }
        }
    }
}
