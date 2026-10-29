package vanguard.sim;

/**
 * M9 part G (design/campaign Level 34, The freighter; m9g-gaps.md §1 #4, QG-2 a): a level's story pickup, the Helix core.
 * A destructible group's last destructible (the hold's fourth clamp) frees it: it {@link State#RISING rises} out of the
 * hold at the group's centre for {@value #RISE_SECONDS} s, scrolling with the ground and untouchable (its look scaling
 * up: {@link #riseShare(double)}), then it is {@link State#LOOSE loose}: a {@link PickupType#STORY} pickup in the pool
 * that drifts (and a Pickup magnet pulls) as any pickup and is collected when the flying ship's collection radius
 * touches its {@link #BOX} ({@link State#COLLECTED}, the cue {@link CueTrigger#STORY_COLLECTED}), or missed when it
 * leaves the screen or its time runs out ({@link State#MISSED}, {@link CueTrigger#STORY_MISSED}: the tug's line); so
 * is a core never released, when its group's last part leaves the screen uncleared or the level ends first. It
 * pays nothing, is never a secret and plays no {@code SECRET} cue. One per level; absent ({@code null} in the sortie)
 * in a level without one, so the other levels hash as before.
 */
public final class StoryPickups implements Hashed {
    /** Where the story pickup is in this attempt. */
    public enum State {
        /** Its group still stands (or it has not come yet). */
        WAITING,
        /** Freed: rising out of the hold, untouchable, scrolling with the ground. */
        RISING,
        /** A {@link PickupType#STORY} pickup in the pool, drifting until collected or gone. */
        LOOSE,
        /** The ship flew through it. */
        COLLECTED,
        /**
         * It left the screen or its time ran out uncollected, or was never released (its group gone uncleared, the
         * level's end): the tug picks it up, radio only.
         */
        MISSED
    }

    /** How long it rises out of the hold, untouchable, s (design/campaign Level 34; QG-2 a). */
    public static final double RISE_SECONDS = 1.0;

    /** Its rise in steps. */
    static final int RISE_TICKS = SimStep.ticks(RISE_SECONDS);

    /** The crate's box, px (≈ 60 × 40, the Helix emblem on its seal): the ship collects it by touching it. */
    public static final Hitbox BOX = new Hitbox(60, 40);

    private final SimEvents events;
    private final Radio radio;
    private State state = State.WAITING;
    private double x;
    private double y;
    private double prevY;
    private int riseTicks;

    StoryPickups(SimEvents events, Radio radio) {
        this.events = events;
        this.radio = radio;
    }

    /** A new attempt: its group stands again. */
    void reset() {
        state = State.WAITING;
        x = y = prevY = 0;
        riseTicks = 0;
    }

    /** Its group's last destructible fell: it starts to rise at ({@code atX}, {@code atY}), the group's centre. */
    void rise(double atX, double atY) {
        if (state != State.WAITING) {
            return;
        }
        state = State.RISING;
        x = atX;
        y = prevY = atY;
        riseTicks = 0;
        events.add(SimEvents.Type.STORY_RISE, x, y);
    }

    /**
     * One step: rising, it scrolls with the ground by {@code distance}; at the rise's end it is dropped into the pool as
     * a {@link PickupType#STORY} pickup where it is (tried again next step when the pool is full).
     */
    void step(double distance, PickupPasses pickups) {
        if (state != State.RISING) {
            return;
        }
        prevY = y;
        y -= distance;
        if (riseTicks < RISE_TICKS) {
            riseTicks++;
        }
        if (riseTicks >= RISE_TICKS && pickups.drop(PickupType.STORY, x, y)) {
            state = State.LOOSE;
        }
    }

    /** The ship collected its pickup at ({@code px}, {@code py}). */
    void collected(double px, double py) {
        if (state != State.LOOSE) {
            return;
        }
        state = State.COLLECTED;
        x = px;
        y = prevY = py;
        events.add(SimEvents.Type.STORY_COLLECTED, px, py);
        radio.cue(CueTrigger.STORY_COLLECTED, "");
    }

    /** Its pickup left the screen or ran out of time at ({@code px}, {@code py}), uncollected. */
    void missed(double px, double py) {
        if (state != State.LOOSE) {
            return;
        }
        miss(px, py);
    }

    /**
     * Step FIX2 (pending round 67): it can no longer be freed, the core never released: its group's last part left the
     * screen at ({@code px}, {@code py}) with the group not cleared, or the level ended first (the ship's place). It is
     * {@link State#MISSED missed} as a released core that leaves: the event and the tug's line, once.
     */
    void lost(double px, double py) {
        if (state != State.WAITING) {
            return;
        }
        miss(px, py);
    }

    private void miss(double px, double py) {
        state = State.MISSED;
        x = px;
        y = prevY = py;
        events.add(SimEvents.Type.STORY_MISSED, px, py);
        radio.cue(CueTrigger.STORY_MISSED, "");
    }

    /**
     * Whether a ship at ({@code shipX}, {@code shipY}) with the collection {@code radius} touches the story pickup's
     * {@link #BOX} centred at ({@code px}, {@code py}): flying through it.
     */
    static boolean touches(double px, double py, double shipX, double shipY, double radius) {
        double dx = Math.max(Math.abs(shipX - px) - BOX.width() / 2, 0);
        double dy = Math.max(Math.abs(shipY - py) - BOX.height() / 2, 0);
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    public void addTo(StateHash hash) {
        hash.add(state.ordinal()).add(x).add(y).add(riseTicks);
    }

    /** Where it is in this attempt. */
    public State state() {
        return state;
    }

    /** Whether it is rising out of the hold now (untouchable; drawn from here, not from the pool). */
    public boolean rising() {
        return state == State.RISING;
    }

    /** Whether the ship collected it in this attempt. */
    public boolean collected() {
        return state == State.COLLECTED;
    }

    /** Whether it was missed in this attempt (the tug's line). */
    public boolean missed() {
        return state == State.MISSED;
    }

    /**
     * How far it has risen, 0 to 1, between the previous and the current step ({@code alpha} in [0, 1]): its look's
     * scale while {@link #rising()}; 1 once loose.
     */
    public double riseShare(double alpha) {
        if (state == State.WAITING) {
            return 0;
        }
        if (state != State.RISING) {
            return 1;
        }
        double ticks = Math.max(0, riseTicks - 1 + alpha);
        return Math.min(1, ticks / RISE_TICKS);
    }

    /** Where it rises (or was collected or missed), x px. */
    public double renderX() {
        return x;
    }

    /** Where it rises between the previous and the current step, y px (y up); {@code alpha} in [0, 1]. */
    public double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }
}
