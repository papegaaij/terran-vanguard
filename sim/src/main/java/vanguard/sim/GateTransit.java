package vanguard.sim;

/**
 * M11 part B, step B1 (design/world/vrell-space, Hazards & set pieces: gate transit; Q11 a; design/campaign Level 43,
 * Layout; design/player/wingmen, Rook in Act 7): the gate transit at a level's end, owned by a {@link Sortie} only in a
 * level with a {@link VrellLevel.Transit} ({@link Sortie#transit()}).
 *
 * <p>From its form time on the level clock ({@link SimEvents.Type#TRANSIT_FORM}) the ship's commands are set aside (no
 * input: no flight, no fire, no special, no dash): it eases over {@link #FORM_SECONDS} from where it was to the
 * formation point ({@link #FORM_X}, {@link #FORM_Y}) and holds it; Rook, while he flies, eases the same way from where
 * he was to his wing slot beside it (no AI, no gun). Nothing hits the ship or the escort (the bullets in flight pass
 * through). At the warp's time {@link SimEvents.Type#TRANSIT_WARP} starts the renderer's warp ({@link #warp()}); the
 * level's {@code reach-end} ends it inside the warp. Allocates nothing.
 */
public final class GateTransit {
    /** The ease from where the ship (and Rook) were to the formation, s (default, pending the user's review). */
    static final double FORM_SECONDS = 2.0;
    /** The formation point: the field's centre column (toward the ring's centre ahead). */
    static final double FORM_X = PlayField.WIDTH / 2.0;
    /** The formation point's height: the launch's start height (the launch reversed). */
    static final double FORM_Y = Ship.START_Y;

    private final int formTicks;
    private final int warpTicks;
    private final int warpLengthTicks;
    private final int easeTicks;
    /** Whether the formation started in this attempt. */
    private boolean formed;
    /** The level tick of the last step. */
    private int tick;
    /** Where the ship and Rook were when the formation started (NaN for Rook out of the fight). */
    private double shipFromX;

    private double shipFromY;
    private double rookFromX = Double.NaN;
    private double rookFromY = Double.NaN;

    GateTransit(VrellLevel.Transit spec) {
        formTicks = SimStep.ticks(spec.form());
        warpTicks = SimStep.ticks(spec.warp());
        warpLengthTicks = Math.max(1, SimStep.ticks(spec.length()));
        easeTicks = Math.max(1, SimStep.ticks(FORM_SECONDS));
    }

    /** Back to the level start: not formed. */
    void reset() {
        formed = false;
        tick = 0;
        rookFromX = rookFromY = Double.NaN;
    }

    /** Back to a boss checkpoint at {@code levelTick}: one before the formation starts it afresh. */
    void restore(int levelTick) {
        tick = levelTick;
        if (levelTick < formTicks) {
            formed = false;
            rookFromX = rookFromY = Double.NaN;
        }
    }

    /**
     * At the step's start, the level clock at {@code levelTick}: the formation starts at its time (the ship flying;
     * where it and Rook are is remembered), the warp at its time.
     */
    void step(int levelTick, boolean flying, Ship ship, Wingman wingman, SimEvents events) {
        tick = levelTick;
        if (!formed && flying && levelTick >= formTicks) {
            formed = true;
            shipFromX = ship.x();
            shipFromY = ship.y();
            boolean rook = wingman != null && !wingman.out();
            rookFromX = rook ? wingman.x() : Double.NaN;
            rookFromY = rook ? wingman.y() : Double.NaN;
            events.add(SimEvents.Type.TRANSIT_FORM, ship.x(), ship.y());
        }
        if (formed && levelTick == warpTicks) {
            events.add(SimEvents.Type.TRANSIT_WARP, FORM_X, FORM_Y);
        }
    }

    /** Whether the formation holds: the ship's commands set aside, nothing hits it or the escort. */
    public boolean active() {
        return formed;
    }

    /** The eased share of the way to the formation now, 0–1 (smoothstep). */
    private double share() {
        double t = Math.min(1, (double) (tick - formTicks) / easeTicks);
        return t * t * (3 - 2 * t);
    }

    /** One step of the ship toward the formation point. */
    void flyShip(Ship ship) {
        double share = share();
        ship.formTo(shipFromX + (FORM_X - shipFromX) * share, shipFromY + (FORM_Y - shipFromY) * share);
    }

    /** One step of Rook toward his wing slot beside the ship; false when he was out at the formation (his own step). */
    boolean flyRook(Wingman wingman, Ship ship) {
        if (Double.isNaN(rookFromX)) {
            return false;
        }
        wingman.transit(rookFromX, rookFromY, ship.x(), ship.y(), share());
        return true;
    }

    /** The warp's share now, 0 before it to 1 at its end (the renderer's post-effect). */
    public double warp() {
        if (!formed || tick < warpTicks) {
            return 0;
        }
        return Math.min(1, (double) (tick - warpTicks) / warpLengthTicks);
    }

    /** The formation's share now, 0 at its start to 1 in formation (eased); 0 before it. */
    public double formation() {
        return formed ? share() : 0;
    }

    void addTo(StateHash hash) {
        hash.add(formed ? 1 : 0);
        if (formed) {
            hash.add(shipFromX).add(shipFromY).add(rookFromX).add(rookFromY);
        }
    }
}
