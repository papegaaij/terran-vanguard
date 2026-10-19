package vanguard.sim;

/**
 * M7 part A: the state of one {@link WeaponSpec.Delivery#BEAM} mount (design/player/weapons/ion-beam,
 * Beam rules) for the presentation: whether it is on, where its column runs and what it ends on this
 * step. The simulation keeps these as fields (no event per step); {@link SimEvents.Type#BEAM_ON} and
 * {@link SimEvents.Type#BEAM_OFF} mark its start and stop.
 */
public final class Beam {
    /** A beam reaches its full length in 0.05 s (design/player/weapons/ion-beam, Beam rules). */
    public static final int GROW_TICKS = SimStep.ticks(0.05);

    /** A beam bores this many px into the target it ends on. */
    public static final double BORE = 4;

    /** {@link #target()} for a set piece's part starts here: + piece × MAX_PARTS + part. */
    public static final int PART_TARGET = PlayerFire.PART_SERIAL;

    /** What the column ended on in the last step. */
    public enum Contact {
        /** Nothing: it ran to its reach (off the top edge once it has grown). */
        NONE,
        /** A target it damages (the contact flare, the target's heat tint). */
        BURN,
        /**
         * Something it cannot damage that stops it: an armoured set-piece body or neck, a shielded part
         * or a unit that takes no damage now, above the ground layer (a glance at its edge).
         */
        BLOCKED,
        /**
         * M7 part C: a canyon wall or rock island (design/player/weapons/ion-beam, Walls): it ends at the
         * rock's edge with the rock-scorch impact; nothing beyond is hit.
         */
        ROCK
    }

    boolean on;
    int ticksOn;
    double x;
    double fromY;
    double endY;
    double width;
    Contact contact = Contact.NONE;
    int target = -1;
    double glanceY = Double.NaN;
    /** The trigger (a ground serial) the beam burnt in the last step, -1 for none, and its steps on it. */
    int trigger = -1;

    int triggerTicks;

    void reset() {
        on = false;
        ticksOn = 0;
        contact = Contact.NONE;
        target = -1;
        glanceY = Double.NaN;
        trigger = -1;
        triggerTicks = 0;
    }

    void addTo(StateHash hash) {
        hash.add(on ? 1 : 0).add(ticksOn).add(trigger).add(triggerTicks);
    }

    /** Whether fire is held and the beam runs. */
    public boolean on() {
        return on;
    }

    /** Steps since it came on (0 while off): it reaches full length in {@link #GROW_TICKS}. */
    public int ticksOn() {
        return ticksOn;
    }

    /** The column's centre line, play-field px. */
    public double x() {
        return x;
    }

    /** Where it starts: the muzzle at the nose, px up from the bottom edge. */
    public double fromY() {
        return fromY;
    }

    /**
     * Where it ends: at a target, the contact point bored {@link #BORE} px into it;
     * otherwise its reach (beyond the top edge once grown).
     */
    public double endY() {
        return endY;
    }

    /** Its hit width (and drawn core), px: the level's, or the overdrive's. */
    public double width() {
        return width;
    }

    public Contact contact() {
        return contact;
    }

    /**
     * The target it burns: an enemy's {@link Enemy#serial()}, or a set piece's part as {@link
     * #PART_TARGET} + piece × {@link LevelScript.SetPieceSpec#MAX_PARTS} + part; -1 for
     * anything else (a ground object, a spore mine, nothing).
     */
    public int target() {
        return target;
    }

    /**
     * Where it crosses the nearest hardened (or for now undamageable) ground target it passes over
     * below its end, for the faint glance spark; {@link Double#NaN} for none.
     */
    public double glanceY() {
        return glanceY;
    }
}
