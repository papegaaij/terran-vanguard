package vanguard.sim;

/**
 * M9 part C (design/enemies/air/gilded-gunship, the arcs): the factor facing armour ({@link EnemySpec.FacingArmour}) gives a
 * direct hit by the direction it arrives from, on a unit that faces straight down the screen in its frame.
 */
final class ArmourArcs {
    /** {@link SimEvents.Type#PLATING_HIT}'s value: the armour plate in front. */
    static final int FRONT = 0;
    /** {@link SimEvents.Type#PLATING_HIT}'s value: the engines at the rear. */
    static final int REAR = 1;

    private ArmourArcs() {}

    /**
     * The factor of a direct hit flying at (vx, vy) (y up, in the unit's frame): the front factor when it flies up into
     * the nose within the front half arc, the rear factor when it flies down onto the engines within the rear half arc,
     * else exactly 1 (and for a still shot).
     */
    static double factor(EnemySpec.FacingArmour plating, double vx, double vy) {
        double length = Math.sqrt(vx * vx + vy * vy);
        if (length == 0) {
            return 1;
        }
        // The unit faces down: a hit from the front flies up, against its facing; one from behind flies down.
        double against = vy / length;
        if (against >= Trig.cos(plating.frontHalfArcRadians())) {
            return plating.frontFactor();
        }
        if (-against >= Trig.cos(plating.rearHalfArcRadians())) {
            return plating.rearFactor();
        }
        return 1;
    }
}
