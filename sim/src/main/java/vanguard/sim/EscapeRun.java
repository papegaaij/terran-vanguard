package vanguard.sim;

/**
 * M10 part H, step H1b (design/enemies/bosses/ascendant, The hull breaks and The lifeboat; m10h-gaps.md §1 #10, #11,
 * §4, §5): an escaping boss's end ({@link EscapeSpec}), kept by its {@link SetPiece} behind its one accessor ({@link
 * SetPiece#escape()}). It runs on the boss's own steps from K, the death of the part the hull breaks on ({@link
 * #breakHull}: {@link SimEvents.Type#HULL_BROKEN}, raised by the sortie, which bursts and pays the other parts): the
 * flash ({@link SimEvents.Type#HULL_FLASH}, value the hull's credit shower), the lifeboat's breakout ({@link
 * SimEvents.Type#LIFEBOAT_AWAY}), its climb, and at the top edge the escape (the sortie raises {@link
 * SimEvents.Type#BOSS_ESCAPED} on {@link #escapedNow()}: the fight is over, the clock resumes).
 *
 * <p>The climb: the lifeboat's height eases up from its breakout place to beyond the top edge over the climb's time, its
 * speed rising in the spec's ratio; its path weaves up to the weave either side on two swings whose phases are drawn
 * from the sortie's seeded random at the breakout (a replay weaves the same), easing in from the breakout place. Round
 * 80 (B.1/A.4): a short scene, the lifeboat is never hit, pays nothing and fires nothing. Its state is hashed with its
 * piece's (only an escaping boss's); the boss checkpoint and a retry start it afresh. Allocates nothing once made.
 */
public final class EscapeRun {
    /** The radio subject at K (Okafor's line as the hull breaks). */
    public static final String HULL_BREAKS = "hull-breaks";
    /** The radio subject at the breakout. */
    public static final String LIFEBOAT_AWAY = "lifeboat-away";
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
    /** The climb's height from the breakout place to beyond the top edge, px. */
    private final double climb;
    /** The lifeboat's half width (its path stays on the field). */
    private final double halfWidth;

    /** Steps since K; -1 before it. */
    private int since;

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
        Hitbox box = pieceSpec.parts().get(spec.part()).box();
        climb = PlayField.HEIGHT + box.height() / 2 - spec.fromY();
        halfWidth = box.width() / 2;
        reset();
    }

    /** Back to the level start (and the boss checkpoint): the hull whole, no lifeboat. */
    void reset() {
        since = -1;
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

    /** One step of the boss from K on: the flash, the breakout (drawing the weave's phases) and the climb. */
    void step(BossActions actions) {
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
            actions.ring(SimEvents.Type.LIFEBOAT_AWAY, x, y, spec.part());
            actions.bossCue(LIFEBOAT_AWAY);
            return;
        }
        place((since - breakoutTicks) * SimStep.SECONDS);
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

    // --- the sortie's view ----------------------------------------------------------------------------

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

    /** Whether it has escaped (the fight is over). */
    public boolean escaped() {
        return since >= endTicks;
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
        hash.add(since).add(swingA).add(swingB).add(x).add(y).add(hullCredits).add(ended ? 1 : 0);
    }
}
