package vanguard.sim;

/**
 * M10 part H, step H1b (design/enemies/bosses/ascendant, The hull breaks and The lifeboat; m10h-gaps.md §1 #10, #11,
 * §4, §5): an escaping boss's end ({@link EscapeSpec}), kept by its {@link SetPiece} behind its one accessor ({@link
 * SetPiece#escape()}). It runs on the boss's own steps from K, the death of the part the hull breaks on ({@link
 * #breakHull}: {@link SimEvents.Type#HULL_BROKEN}, raised by the sortie, which bursts and pays the other parts): the
 * flash ({@link SimEvents.Type#HULL_FLASH}, value the hull's credit shower), the lifeboat's breakout ({@link
 * SimEvents.Type#LIFEBOAT_AWAY}), its climb and wake, and at the top edge the escape (the sortie raises {@link
 * SimEvents.Type#BOSS_ESCAPED} on {@link #escapedNow()}: the fight is over, the clock resumes).
 *
 * <p>The climb: the lifeboat's height eases up from its breakout place to beyond the top edge over the climb's time, its
 * speed rising in the spec's ratio (20 : 90); its path weaves up to the weave either side on two swings whose phases
 * are drawn from the sortie's seeded random at the breakout (a replay weaves the same), easing in from the breakout
 * place. Damage counts toward its cap ({@link #take}); the sortie pays its bounty pro rata ({@link #share}) and shows
 * the credits as floating numbers summed a quarter second at a time. Its state is hashed with its piece's (only an
 * escaping boss's); the boss checkpoint and a retry start it afresh. Allocates nothing once made.
 */
public final class EscapeRun {
    /** The radio subject at K (Okafor's line as the hull breaks). */
    public static final String HULL_BREAKS = "hull-breaks";
    /** The radio subject at the breakout. */
    public static final String LIFEBOAT_AWAY = "lifeboat-away";
    /** The floating numbers of the lifeboat's pay are summed over this, s. */
    static final double PAY_SHOW_SECONDS = 0.25;
    /** The weave eases in over this from the breakout, s. */
    private static final double WEAVE_EASE_SECONDS = 1.5;
    /** The weave's two swings' periods, s. */
    private static final double SWING_A_SECONDS = 4.6;

    private static final double SWING_B_SECONDS = 2.9;

    private final SetPiece piece;
    private final EscapeSpec spec;
    private final int flashTicks;
    private final int breakoutTicks;
    private final int endTicks;
    private final int wakeTicksEvery;
    private final int showTicksEvery;
    /** The climb's height from the breakout place to beyond the top edge, px. */
    private final double climb;
    /** The lifeboat's half width (its path stays on the field). */
    private final double halfWidth;

    /** Steps since K; -1 before it. */
    private int since;

    private double dealt;
    private double paidShare;
    private int pending;
    private int showTicks;
    private boolean contacted;
    private int wakeTicks;
    private double swingA;
    private double swingB;
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    /** The hull's credits (the flash's credit shower), set at K. */
    private int hullCredits;
    /** Whether the sortie ended the fight at the top edge ({@link #end()}). */
    private boolean ended;

    EscapeRun(SetPiece piece, EscapeSpec spec, SetPieceSpec pieceSpec) {
        this.piece = piece;
        this.spec = spec;
        flashTicks = SimStep.ticks(spec.flashSeconds());
        breakoutTicks = Math.max(flashTicks, SimStep.ticks(spec.breakoutSeconds()));
        endTicks = breakoutTicks + Math.max(1, SimStep.ticks(spec.climbSeconds()));
        wakeTicksEvery = Math.max(1, SimStep.ticks(spec.wakeSeconds()));
        showTicksEvery = Math.max(1, SimStep.ticks(PAY_SHOW_SECONDS));
        Hitbox box = pieceSpec.parts().get(spec.part()).box();
        climb = PlayField.HEIGHT + box.height() / 2 - spec.fromY();
        halfWidth = box.width() / 2;
        reset();
    }

    /** Back to the level start (and the boss checkpoint): the hull whole, no lifeboat. */
    void reset() {
        since = -1;
        dealt = 0;
        paidShare = 0;
        pending = 0;
        showTicks = 0;
        contacted = false;
        wakeTicks = 0;
        swingA = 0;
        swingB = 0;
        x = prevX = spec.fromX();
        y = prevY = spec.fromY();
        hullCredits = 0;
        ended = false;
    }

    /** K: the part the hull breaks on died; the hull's credits are {@code credits} (its flash's shower). */
    void breakHull(int credits) {
        if (since < 0) {
            since = 0;
            hullCredits = credits;
        }
    }

    /**
     * One step of the boss from K on: the flash, the breakout (drawing the weave's phases), the climb and the wake
     * (fired only while {@code firing}).
     */
    void step(BossActions actions, boolean firing) {
        if (since < 0 || since >= endTicks) {
            return;
        }
        since++;
        prevX = x;
        prevY = y;
        if (since == flashTicks) {
            actions.ring(SimEvents.Type.HULL_FLASH, piece.x(), piece.y(), hullCredits);
        }
        if (since < breakoutTicks) {
            return;
        }
        if (since == breakoutTicks) {
            swingA = actions.draw() * 2 * StrictMath.PI;
            swingB = actions.draw() * 2 * StrictMath.PI;
            x = prevX = spec.fromX();
            y = prevY = spec.fromY();
            wakeTicks = wakeTicksEvery;
            actions.ring(SimEvents.Type.LIFEBOAT_AWAY, x, y, spec.part());
            actions.bossCue(LIFEBOAT_AWAY);
            return;
        }
        place((since - breakoutTicks) * SimStep.SECONDS);
        if (--wakeTicks <= 0) {
            wakeTicks = wakeTicksEvery;
            if (firing && since < endTicks) {
                double half = spec.wakeGap() / 2;
                double below = y - piece.spec().parts().get(spec.part()).box().height() / 2;
                actions.bullet(x - half, below, -StrictMath.PI / 2, spec.wakeSpeed(), spec.wakeDamage());
                actions.bullet(x + half, below, -StrictMath.PI / 2, spec.wakeSpeed(), spec.wakeDamage());
            }
        }
        if (showTicks > 0) {
            showTicks--;
        }
    }

    /** Its place {@code t} s into the climb. */
    private void place(double t) {
        double u = Math.min(1, t / spec.climbSeconds());
        double a = spec.speedFrom();
        double b = spec.speedTo();
        double share = (a * u + (b - a) * u * u / 2) / (a + (b - a) / 2);
        y = Math.max(spec.floorY(), spec.fromY() + climb * share);
        double ease = ScrollPlan.smoothstep(Math.min(1, t / WEAVE_EASE_SECONDS));
        double swing = 0.6 * (Trig.sin(2 * StrictMath.PI * t / SWING_A_SECONDS + swingA) - Trig.sin(swingA))
                + 0.4 * (Trig.sin(2 * StrictMath.PI * t / SWING_B_SECONDS + swingB) - Trig.sin(swingB));
        double sway = Math.clamp(swing, -1, 1) * spec.weave() * ease;
        x = Math.clamp(spec.fromX() + sway, halfWidth, PlayField.WIDTH - halfWidth);
    }

    /** Part {@code damage} toward the cap while the lifeboat may be hurt; returns the damage counted. */
    double take(double damage) {
        if (!touchable() || !(damage > 0)) {
            return 0;
        }
        double counted = Math.min(damage, spec.cap() - dealt);
        dealt += counted;
        if (dealt >= spec.cap() - 1e-9) {
            dealt = spec.cap();
        }
        return counted;
    }

    /** Its contact with the ship: whether it deals its damage now (once a run). */
    boolean strike() {
        if (contacted) {
            return false;
        }
        contacted = true;
        return true;
    }

    /** The share of its bounty paid so far; the sortie pays up to {@link #share()} and records it here. */
    double paidShare() {
        return paidShare;
    }

    /** The sortie paid up to {@code share} of its bounty, {@code credits} of them now (shown a quarter second later). */
    void paid(double share, int credits) {
        paidShare = share;
        if (credits > 0 && pending == 0) {
            showTicks = showTicksEvery;
        }
        pending += credits;
    }

    /** The credits to show now as one floating number (a quarter second's, or the last at the cap or the edge); 0: none. */
    int showDue() {
        if (pending == 0 || (showTicks > 0 && !capped() && since < endTicks)) {
            return 0;
        }
        int shown = pending;
        pending = 0;
        return shown;
    }

    // --- the sortie's view ----------------------------------------------------------------------------

    /** Whether part {@code p} is the lifeboat while it is not on the field (before the breakout, after the escape). */
    boolean absent(int p) {
        return p == spec.part() && (since < breakoutTicks || since >= endTicks);
    }

    /** Whether the lifeboat takes hits now: out, climbing, below its cap. */
    boolean touchable() {
        return since >= breakoutTicks && since < endTicks && dealt < spec.cap();
    }

    /** Whether the lifeboat has reached the top edge and the sortie has not ended the fight yet. */
    boolean escapedNow() {
        return since == endTicks && !ended;
    }

    /** The sortie ended the fight (once). */
    void end() {
        ended = true;
    }

    // --- the presentation's view ----------------------------------------------------------------------

    public EscapeSpec spec() {
        return spec;
    }

    /** Whether the hull has broken (K passed). */
    public boolean broken() {
        return since >= 0;
    }

    /** Seconds since K; negative before. */
    public double brokenSeconds() {
        return since < 0 ? -1 : since * SimStep.SECONDS;
    }

    /** Whether the lifeboat is out (from its breakout to its escape). */
    public boolean out() {
        return since >= breakoutTicks && since < endTicks;
    }

    /** Whether it has escaped (the fight is over: the bar reads {@code ESCAPED}). */
    public boolean escaped() {
        return since >= endTicks;
    }

    /** The damage counted toward its cap. */
    public double dealt() {
        return dealt;
    }

    /** The escape bar's gold fill: the damage's share of the cap, 0–1 ({@code MAX} at 1). */
    public double share() {
        return dealt / spec.cap();
    }

    /** Whether it reached its cap (hits spark, the bar reads {@code MAX}). */
    public boolean capped() {
        return dealt >= spec.cap();
    }

    /** The escape bar's time: the climb's seconds left (the full climb before the breakout, 0 after the escape). */
    public double secondsLeft() {
        if (since < breakoutTicks) {
            return spec.climbSeconds();
        }
        return Math.max(0, (endTicks - since) * SimStep.SECONDS);
    }

    /** The share of the climb's time left, 1 → 0 (the amber time bar). */
    public double timeShare() {
        return secondsLeft() / spec.climbSeconds();
    }

    /** The lifeboat's centre now, px from the left. */
    public double x() {
        return x;
    }

    /** The lifeboat's centre now, px up. */
    public double y() {
        return y;
    }

    /** Its centre between the last step and now ({@code alpha} in [0, 1]), px from the left. */
    public double x(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    /** As {@link #x(double)}, px up. */
    public double y(double alpha) {
        return prevY + (y - prevY) * alpha;
    }

    /** The hull's credit shower (its parts' credits), set at K. */
    public int hullCredits() {
        return hullCredits;
    }

    void addTo(StateHash hash) {
        hash.add(since)
                .add(dealt)
                .add(paidShare)
                .add(pending)
                .add(showTicks)
                .add(contacted ? 1 : 0)
                .add(wakeTicks)
                .add(swingA)
                .add(swingB)
                .add(x)
                .add(y)
                .add(hullCredits)
                .add(ended ? 1 : 0);
    }
}
