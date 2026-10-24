package vanguard.sim;

/**
 * M8 part F: Rook's scripted loss in a level with a {@link RookLossSpec} (design/player/wingmen, Rook's loss;
 * design/campaign Level 26; m8f-gaps.md §1.2, §5 F6, F7), split out of {@link Wingman}, which owns one (made once).
 * On the level clock: at the trails' time, while he flies (not ejected), his trails start ({@link
 * SimEvents.Type#ROOK_TRAILS}) and he stops counting as flying for the radio ({@link Wingman#onRadio()}: his barks and
 * the {@code requires: escort} lines end); at the loss's time, while he still flies, he is lost ({@link
 * SimEvents.Type#ROOK_LOST}): no more firing, hits or AI, and he spirals down from where he was toward the side away
 * from the ship at a steady speed that takes him below the bottom edge within the spin's time and is at least {@link
 * #FASTER} times the scroll's then (he falls faster than the field), then is gone for the attempt. Not an
 * eject: no pod, his armour stays as it was. Ejected before either time, neither happens (the loss is the radio's
 * only). Each attempt starts it afresh ({@link #reset()}), so a retry replays it. Its state is hashed only in a level
 * with a loss. Allocation-free.
 */
public final class RookLoss {
    /** How far he drifts toward his side over the spin, px. */
    static final double DRIFT = 40;
    /** The spiral's radius across, px. */
    static final double RADIUS = 16;
    /** The spiral's turns over the spin (across), and his hull's turns (the sprite's spin). */
    static final double TURNS = 2;

    static final double HULL_TURNS = 3;
    /** He ends at least this far below the bottom edge (his hull's top to it), px. */
    static final double BELOW = 8;
    /** His fall is at least this many times the scroll's speed at the loss. */
    static final double FASTER = 1.25;

    /** The level's loss; null without one (then nothing of it is hashed and nothing happens). */
    private RookLossSpec spec;

    private int trailsTick;
    private int lossTick;
    private int spinTicks;

    private boolean trailing;
    private boolean lost;
    /** Steps since the loss. */
    private int lossTicks;
    /** Where he was at the loss. */
    private double fromX;

    private double fromY;
    /** The side he spins to: -1 left, 1 right (away from the ship). */
    private int side;
    /** His fall's speed, px/s. */
    private double fall;

    /** The level's loss (once, at the sortie's start). */
    void plan(RookLossSpec loss) {
        spec = loss;
        trailsTick = SimStep.ticks(loss.trails());
        lossTick = SimStep.ticks(loss.t());
        spinTicks = Math.max(1, SimStep.ticks(loss.spin()));
        reset();
    }

    /** At an attempt's start: he flies, nothing of the loss has happened. */
    void reset() {
        trailing = false;
        lost = false;
        lossTicks = 0;
        fromX = fromY = 0;
        side = 0;
        fall = 0;
    }

    /** Whether the level has a loss. */
    public boolean planned() {
        return spec != null;
    }

    /**
     * One step at {@code levelTick} for {@code rook} (his position remembered already), the ship at {@code shipX}, the
     * ground scrolling {@code scrollStep} px in this step: the trails' and the loss's starts, then his spin. Returns
     * whether he is lost (his own flight and gun are skipped).
     */
    boolean step(int levelTick, Wingman rook, double shipX, double scrollStep, SimEvents events) {
        if (spec == null) {
            return false;
        }
        if (!lost) {
            if (rook.ejected()) {
                return false;
            }
            if (!trailing && levelTick >= trailsTick) {
                trailing = true;
                events.add(SimEvents.Type.ROOK_TRAILS, rook.x(), rook.y(), 0);
            }
            if (levelTick < lossTick) {
                return false;
            }
            trailing = true;
            lost = true;
            lossTicks = 0;
            fromX = rook.x();
            fromY = rook.y();
            side = fromX < shipX ? -1 : 1;
            double below = fromY + rook.spec().craft().hitbox().height() / 2 + BELOW;
            fall = Math.max(below / spec.spin(), FASTER * Math.abs(scrollStep) / SimStep.SECONDS);
            rook.lose();
            events.add(SimEvents.Type.ROOK_LOST, fromX, fromY, side);
            return true;
        }
        if (lossTicks < spinTicks) {
            lossTicks++;
            double p = (double) lossTicks / spinTicks;
            rook.spinTo(
                    fromX + side * (DRIFT * p + RADIUS * Trig.sin(2 * StrictMath.PI * TURNS * p)),
                    fromY - fall * lossTicks * SimStep.SECONDS);
        } else {
            rook.spinTo(rook.x(), rook.y());
        }
        return true;
    }

    /** Whether his trails have started (from the trails' time, while he flew then, through his loss). */
    public boolean trailing() {
        return trailing;
    }

    /** Whether he is lost in this attempt (spinning down or gone). */
    public boolean lost() {
        return lost;
    }

    /** His fall's speed, px/s; 0 before his loss. */
    public double fallSpeed() {
        return fall;
    }

    /** Whether his spin is over: he is below the bottom edge, gone for the attempt. */
    public boolean gone() {
        return lost && lossTicks >= spinTicks;
    }

    /** Steps since his loss; 0 before it. */
    public int ticksSinceLoss() {
        return lost ? lossTicks : 0;
    }

    /** How far his spin is, 0 at the loss to 1 off the bottom edge; 0 before it. */
    public double spinProgress() {
        return lost ? (double) lossTicks / spinTicks : 0;
    }

    /** His hull's turn in the spin, radians toward his side (clockwise for the right); 0 before it. */
    public double hullTurn() {
        return side * 2 * StrictMath.PI * HULL_TURNS * spinProgress();
    }

    /** The side he spins to: -1 left, 1 right; 0 before his loss. */
    public int side() {
        return side;
    }

    /** Hashed only in a level with a loss. */
    void addTo(StateHash hash) {
        if (spec == null) {
            return;
        }
        hash.add(trailing ? 1 : 0)
                .add(lost ? 1 : 0)
                .add(lossTicks)
                .add(fromX)
                .add(fromY)
                .add(side)
                .add(fall);
    }
}
