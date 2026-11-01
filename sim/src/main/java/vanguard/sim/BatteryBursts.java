package vanguard.sim;

/**
 * M10 part H, step H1a (design/enemies/bosses/ascendant, Fire rules and Tells; m10h-gaps.md §1 #5): a boss's batteries'
 * told bursts ({@link BatteryFire}). Each battery's clock runs its gun's interval (the first ones spread over it: the
 * k-th of n due after (k + 1)/n of it, decided in step H1a pending round 80); when due it waits until its panel faces
 * within its fire angle of the ship ({@link PartMirrors#aimed}), fewer than the limit are telling or bursting, and the
 * last tell started at least the spacing ago; then it tells ({@link SimEvents.Type#BATTERY_TELL}: the panel's red rim,
 * at the part, value the part) and fires its burst of aimed shots, its panel holding still through both, and its clock
 * starts again. A part that takes no damage now (its descent, a phase not yet its own) starts no tell. Kept by its
 * {@link SetPiece}, made only for a boss with battery fire and hashed only then. Allocates nothing once made.
 */
public final class BatteryBursts {
    private static final int IDLE = 0;
    private static final int TELL = 1;
    private static final int BURST = 2;

    private final SetPiece piece;
    private final BatteryFire spec;
    private final BossSpec.Attack attack;
    private final int[] parts;
    private final int intervalTicks;
    private final int tellTicks;
    private final int gapTicks;
    private final int apartTicks;
    /** Per battery: the steps to its next burst's due time (0: due), its state and the steps in it. */
    private final int[] clock;

    private final int[] state;
    private final int[] stateTicks;
    /** Per battery: its burst's shots still to fire and the steps to the next. */
    private final int[] shotsLeft;

    private final int[] shotTicks;
    /** Steps since the last tell started. */
    private int sinceStart;

    BatteryBursts(SetPiece piece, BossSpec boss, BatteryFire spec) {
        this.piece = piece;
        this.spec = spec;
        attack = boss.attacks().get(spec.attack());
        parts = spec.parts().stream().mapToInt(Integer::intValue).toArray();
        intervalTicks = Math.max(1, SimStep.ticks(attack.gun().intervalSeconds()));
        tellTicks = SimStep.ticks(spec.tellSeconds());
        gapTicks = Math.max(1, SimStep.ticks(attack.burstGapSeconds()));
        apartTicks = SimStep.ticks(spec.apartSeconds());
        clock = new int[parts.length];
        state = new int[parts.length];
        stateTicks = new int[parts.length];
        shotsLeft = new int[parts.length];
        shotTicks = new int[parts.length];
        reset();
    }

    /** Back to the level start (and the boss checkpoint): every clock at its first due time, none telling. */
    void reset() {
        for (int k = 0; k < parts.length; k++) {
            clock[k] = Math.max(1, (int) ((long) intervalTicks * (k + 1) / parts.length));
            state[k] = IDLE;
            stateTicks[k] = 0;
            shotsLeft[k] = 0;
            shotTicks[k] = 0;
        }
        sinceStart = Integer.MAX_VALUE / 2;
    }

    /**
     * One engaged step with the ship at (shipX, shipY): the tells and bursts under way go on, the clocks run, and each
     * due battery whose turn the rules allow starts its tell; shots reach the level only while {@code firing}. The
     * panels ({@code mirrors}, null for none) hold still while their battery tells or bursts.
     */
    void step(double shipX, double shipY, PartMirrors mirrors, BossActions actions, boolean firing) {
        if (sinceStart < Integer.MAX_VALUE / 2) {
            sinceStart++;
        }
        for (int k = 0; k < parts.length; k++) {
            int p = parts[k];
            if (piece.partHp(p) <= 0) {
                state[k] = IDLE;
            } else if (state[k] == TELL) {
                if (++stateTicks[k] >= tellTicks) {
                    state[k] = BURST;
                    stateTicks[k] = 0;
                    shotsLeft[k] = attack.gun().burst();
                    shotTicks[k] = 1;
                }
            } else if (state[k] == IDLE && clock[k] > 0) {
                clock[k]--;
            }
            if (state[k] == BURST) {
                stateTicks[k]++;
                if (--shotTicks[k] <= 0) {
                    shotsLeft[k]--;
                    shotTicks[k] = gapTicks;
                    if (firing) {
                        actions.aimed(piece.partX(p), piece.partY(p), attack.gun());
                    }
                }
                if (shotsLeft[k] <= 0) {
                    state[k] = IDLE;
                    stateTicks[k] = 0;
                    clock[k] = intervalTicks;
                }
            }
            if (state[k] == IDLE
                    && clock[k] == 0
                    && piece.partHp(p) > 0
                    && !piece.partShielded(p)
                    && (mirrors == null || mirrors.aimed(p, shipX, shipY))
                    && busy() < spec.atOnce()
                    && sinceStart >= apartTicks) {
                state[k] = TELL;
                stateTicks[k] = 0;
                sinceStart = 0;
                actions.ring(SimEvents.Type.BATTERY_TELL, piece.partX(p), piece.partY(p), p);
            }
            if (mirrors != null && mirrors.fitted(p)) {
                mirrors.hold(p, state[k] != IDLE);
            }
        }
    }

    /** The batteries telling or bursting now. */
    public int busy() {
        int n = 0;
        for (int k = 0; k < parts.length; k++) {
            n += state[k] != IDLE ? 1 : 0;
        }
        return n;
    }

    /** The batteries bursting now (their shots under way). */
    public int bursting() {
        int n = 0;
        for (int k = 0; k < parts.length; k++) {
            n += state[k] == BURST ? 1 : 0;
        }
        return n;
    }

    /** Whether part {@code p} is telling its burst now (its panel's red rim). */
    public boolean telling(int p) {
        int k = battery(p);
        return k >= 0 && state[k] == TELL;
    }

    /** How far part {@code p}'s tell has run, 0–1 (the rim brightening); 0 when it does not tell. */
    public double tellShare(int p) {
        int k = battery(p);
        return k < 0 || state[k] != TELL ? 0 : Math.min(1, (double) stateTicks[k] / Math.max(1, tellTicks));
    }

    /** Whether part {@code p} is firing its burst now. */
    public boolean bursting(int p) {
        int k = battery(p);
        return k >= 0 && state[k] == BURST;
    }

    /** Part {@code p}'s index among the batteries; -1 for a part that is none. */
    private int battery(int p) {
        for (int k = 0; k < parts.length; k++) {
            if (parts[k] == p) {
                return k;
            }
        }
        return -1;
    }

    void addTo(StateHash hash) {
        hash.add(sinceStart);
        for (int k = 0; k < parts.length; k++) {
            hash.add(clock[k])
                    .add(state[k])
                    .add(stateTicks[k])
                    .add(shotsLeft[k])
                    .add(shotTicks[k]);
        }
    }
}
