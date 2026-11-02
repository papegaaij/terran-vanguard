package vanguard.content;

import vanguard.sim.GroundObject;
import vanguard.sim.Pickup;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.Tow;

/**
 * M10 part I (step I2; see {@link Autopilot}): a level's hidden crate from Act 6 on. The autopilot lines up under a
 * secret's trigger on the ground (Level 39's survey hut lock, Level 41's lab vault)
 * or under a drifting tow's cable while it still holds its pod (Level 40's recorder, Level 42's strongbox; Level 30's
 * buoy rule), so its guns open it; the crate then falls to it and the pickup line takes it. Before, Levels 39, 41 and
 * 42's crates were left (the guns met them only when an air unit stood in their column): the playthrough sheet's A
 * grades there, 3 of 7 crates short of the expert's bound. Only a level of Act 6 or later takes this play, so no
 * golden flight (Levels 01-35) changes.
 */
final class SecretPlay {
    private SecretPlay() {}

    /** The first act whose levels take the play. */
    static final int FROM_ACT = 6;

    /** How far above the ship a trigger on the ground must be to line up under it, px. */
    static final double TRIGGER_ABOVE = 40;

    /** How far above the ship a tow's cable must be to line up under it, px. */
    static final double TOW_ABOVE = 60;

    /** Whether the level takes the play (Act 6 on). */
    static boolean seeks(Sortie sortie) {
        return sortie.script().act() >= FROM_ACT;
    }

    /**
     * The x to line up on for a secret now: its freed crate, else a trigger on the ground above the ship, else a present tow's cable still holding its pod above the ship and on the screen; NaN for none (or a level
     * before Act 6).
     */
    static double x(Sortie sortie, double shipY) {
        if (!seeks(sortie)) {
            return Double.NaN;
        }
        Pickup crate = crate(sortie);
        if (crate != null) {
            // the freed crate first, wherever it falls
            return crate.renderX();
        }
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            // a trigger on the ground anywhere above the ship (not only in the upper half, as TargetPlay#secretTarget
            // for Level 10's ferry hatch): the crate climbs to it here
            GroundObject trigger = sortie.groundObject(i);
            double y = trigger.renderY(1);
            if (!trigger.spent()
                    && trigger.spec().secretIndex() >= 0
                    && y > shipY + TRIGGER_ABOVE
                    && y < PlayField.HEIGHT + trigger.spec().size().height()) {
                return trigger.renderX();
            }
        }
        for (int i = 0; i < sortie.towCount(); i++) {
            Tow tow = sortie.tow(i);
            if (tow.present()
                    && tow.holding()
                    && tow.cableY() > shipY + TOW_ABOVE
                    && tow.cableY() < PlayField.HEIGHT
                    && tow.cableX() > 0
                    && tow.cableX() < PlayField.WIDTH) {
                return tow.cableX();
            }
        }
        return Double.NaN;
    }

    /** The freed hidden crate on the screen, or null (or a level before Act 6). */
    static Pickup crate(Sortie sortie) {
        if (!seeks(sortie)) {
            return null;
        }
        for (int i = 0; i < sortie.pickupCount(); i++) {
            Pickup pickup = sortie.pickup(i);
            if (pickup.type() == PickupType.HIDDEN_CRATE) {
                return pickup;
            }
        }
        return null;
    }
}
