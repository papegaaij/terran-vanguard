package vanguard.sim;

/**
 * M9 part B (design/player/wingmen, Rook's rescue and return; design/campaign Level 29; m9b-gaps.md §1.7 and §5): the
 * level's mid-level escort swap, {@link RookLoss} in reverse, owned by the sortie's escort slot (made once). Until the
 * held arena's last holder falls ({@link #release}) the rescued Rook ({@link Loadout#rescue()}) waits in the tender's
 * hold, out of the fight and off the radio; then he launches from the swap's point and flies into his slot over its
 * join time ({@link SimEvents.Type#ESCORT_JOINED} at its end: his barks are back, his kills pay as ever), while the
 * Bulwark in the slot (the loaner or an owned one) peels away to its side's edge at its speed and is gone ({@link
 * SimEvents.Type#BULWARK_DEPARTED}); a Bulwark destroyed before changes nothing. With Rook flying from the start the
 * hold opens empty ({@link #EMPTY}); with no Rook to join the Ember leaves upward ({@link #LEAVES}, the look's) and a
 * Bulwark in the slot stays. Each attempt starts it afresh ({@link #reset()}), so a retry replays it. Hashed only in a
 * level with a swap. Allocation-free.
 */
public final class EscortSwap {
    /** {@link SimEvents.Type#ESCORT_SWAP}'s value: the rescued Rook launches and joins. */
    public static final int JOINS = 0;
    /** The hold opens empty: Rook flew in the slot from the start. */
    public static final int EMPTY = 1;
    /** No Rook to join: the Ember launches and leaves upward. */
    public static final int LEAVES = 2;

    /**
     * M9 part B, step B4b: the {@code holders-down} radio cue's subject (its {@code swap}) when the rescued Rook joins
     * and the Bulwark in the slot still flies: it peels away ("Bulwark, RTB").
     */
    public static final String SUBJECT_BULWARK = "bulwark";
    /** The subject when the rescued Rook joins and no Bulwark flies in the slot (the loaner lost before the release). */
    public static final String SUBJECT_JOINS = "joins";
    /** The subject when there is no Rook to join ({@link #LEAVES}: the Ember leaves upward). */
    public static final String SUBJECT_LEAVES = "leaves";
    /** The subject when Rook flew in the slot from the start ({@link #EMPTY}: the hold opens empty). */
    public static final String SUBJECT_EMPTY = "empty";

    /** The level's swap; null without one (then nothing of it happens or is hashed). */
    private final EscortSwapSpec spec;
    /** What the swap does in this sortie ({@link #JOINS}, {@link #EMPTY}, {@link #LEAVES}). */
    private final int kind;

    private final int joinTicks;

    private boolean released;
    /** Steps since the release. */
    private int ticks;

    private boolean joined;
    /** Whether the peeled-away Bulwark crossed its edge. */
    private boolean departed;

    /**
     * @param spec the level's swap; null for none
     * @param rescued whether a rescued Rook waits for it ({@link Loadout#rescue()})
     * @param rookFlying whether Rook flies in the slot from the start
     */
    EscortSwap(EscortSwapSpec spec, boolean rescued, boolean rookFlying) {
        this.spec = spec;
        kind = rescued ? JOINS : rookFlying ? EMPTY : LEAVES;
        joinTicks = spec == null ? 1 : Math.max(1, SimStep.ticks(spec.joinSeconds()));
        reset();
    }

    /** At an attempt's start: nothing of the swap has happened. */
    void reset() {
        released = false;
        ticks = 0;
        joined = false;
        departed = false;
    }

    /** Whether the level has a swap. */
    public boolean planned() {
        return spec != null;
    }

    /**
     * The held arena's last holder fell: the swap starts ({@link SimEvents.Type#ESCORT_SWAP} at its point); the Bulwark
     * in the slot, still flying, peels away when Rook joins.
     */
    void release(Bulwark bulwark, SimEvents events) {
        if (spec == null || released) {
            return;
        }
        released = true;
        ticks = 0;
        events.add(SimEvents.Type.ESCORT_SWAP, spec.x(), spec.y(), kind);
        if (kind == JOINS && bulwark != null && !bulwark.destroyed()) {
            bulwark.depart();
        }
    }

    /**
     * M9 part B, step B4b: the {@code holders-down} radio cue's subject at the release, from what the swap does and
     * whether {@code bulwark} (the slot's; null without one) still flies; empty without a swap. Allocation-free.
     */
    String radioSubject(Bulwark bulwark) {
        if (spec == null) {
            return "";
        }
        return switch (kind) {
            case JOINS -> bulwark != null && !bulwark.destroyed() ? SUBJECT_BULWARK : SUBJECT_JOINS;
            case EMPTY -> SUBJECT_EMPTY;
            default -> SUBJECT_LEAVES;
        };
    }

    /** One step: the release's clock runs. */
    void step() {
        if (released) {
            ticks++;
        }
    }

    /**
     * The rescued Rook's step while he is not yet in his slot: in the hold before the release, then into his slot beside
     * the ship at (shipX, shipY) over the join time (smoothstep). Returns whether he is still out of the fight (his own
     * AI and gun skipped).
     */
    boolean flyRook(Wingman rook, double shipX, double shipY, SimEvents events) {
        if (kind != JOINS || joined) {
            return false;
        }
        if (!released) {
            rook.holdAt(spec.x(), spec.y());
            return true;
        }
        double p = ScrollPlan.smoothstep(Math.min(1, (double) ticks / joinTicks));
        double toX = rook.slotXAt(shipX);
        double toY = rook.slotYAt(shipY);
        rook.spinTo(spec.x() + (toX - spec.x()) * p, spec.y() + (toY - spec.y()) * p);
        if (ticks >= joinTicks) {
            joined = true;
            events.add(SimEvents.Type.ESCORT_JOINED, rook.x(), rook.y(), 0);
        }
        return true;
    }

    /** The peeling Bulwark's step; returns whether it is out of the fight (its own flight and gun skipped). */
    boolean flyBulwark(Bulwark bulwark, SimEvents events) {
        if (!bulwark.departing()) {
            return false;
        }
        if (!departed && bulwark.peel()) {
            departed = true;
            events.add(SimEvents.Type.BULWARK_DEPARTED, bulwark.x(), bulwark.y(), 0);
        }
        return true;
    }

    /** Whether the swap has started in this attempt (the last holder fell). */
    public boolean released() {
        return released;
    }

    /** Whether the rescued Rook is in his slot (true for a sortie without a rescued Rook). */
    public boolean joined() {
        return kind != JOINS || joined;
    }

    /** What the swap does: {@link #JOINS}, {@link #EMPTY} or {@link #LEAVES}. */
    public int kind() {
        return kind;
    }

    /** How far the join is, 0 at the release to 1 in his slot; 0 before it. */
    public double joinProgress() {
        return released ? Math.min(1, (double) ticks / joinTicks) : 0;
    }

    /** Steps since the release; 0 before it (the look's launch and the Ember's flight up). */
    public int ticksSinceRelease() {
        return released ? ticks : 0;
    }

    /** The launch point, px from the left; NaN without a swap. */
    public double launchX() {
        return spec == null ? Double.NaN : spec.x();
    }

    /** The launch point, px up from the bottom edge; NaN without a swap. */
    public double launchY() {
        return spec == null ? Double.NaN : spec.y();
    }

    /** Whether the peeled-away Bulwark has crossed its edge (gone for the attempt). */
    public boolean bulwarkDeparted() {
        return departed;
    }

    /** Hashed only in a level with a swap. */
    void addTo(StateHash hash, Bulwark bulwark) {
        if (spec == null) {
            return;
        }
        hash.add(released ? 1 : 0).add(ticks).add(joined ? 1 : 0).add(departed ? 1 : 0);
        if (bulwark != null) {
            hash.add(bulwark.departing() ? 1 : 0);
        }
    }
}
