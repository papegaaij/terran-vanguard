package vanguard.sim;

import java.util.Optional;

/**
 * A shield's numbers, from design/player/shields/data.yaml (built by {@code vanguard.content.SimSpecs}).
 *
 * @param capacity shield points when full
 * @param regenPerSecond points regained per second once regenerating
 * @param regenDelaySeconds time without hits before regeneration starts
 * @param breakSeconds extra time the shield stays down after dropping to zero, before the delay
 * @param reflect M10 part A (design/player/shields, Reflex Shield (Act 6)): the Reflex Shield's reflect; empty for every
 *     other shield
 */
public record ShieldModel(
        double capacity,
        double regenPerSecond,
        double regenDelaySeconds,
        double breakSeconds,
        Optional<Reflect> reflect) {
    public ShieldModel {
        java.util.Objects.requireNonNull(reflect);
    }

    /** A shield without M10 part A's reflect (every shield but the Reflex). */
    public ShieldModel(double capacity, double regenPerSecond, double regenDelaySeconds, double breakSeconds) {
        this(capacity, regenPerSecond, regenDelaySeconds, breakSeconds, Optional.empty());
    }

    /** M10 part A: this shield with the Reflex Shield's {@code rule}. */
    public ShieldModel withReflect(Reflect rule) {
        return new ShieldModel(capacity, regenPerSecond, regenDelaySeconds, breakSeconds, Optional.of(rule));
    }

    /**
     * M10 part A (design/player/shields, Reflex Shield (Act 6)): while the shield holds at least {@code window} of its
     * capacity now (the Overcharge's raised capacity included) an enemy bullet or homing missile that hits it costs only
     * {@code share} of its damage and comes back as a bolt of the ship's: from the hit point straight up the screen at
     * {@code speed} px/s, dealing {@code factor} × the bullet's damage ({@link PlayerFire#addBolt}). A bounced bullet
     * (a mirror's) is never reflected; lasers, beams, lightning, rocks, contact and a boss's vents, cones and slams never
     * are (they never take the bullet path).
     */
    public record Reflect(double window, double share, double speed, double factor) {
        public Reflect {
            if (!(window > 0 && window <= 1) || !(share >= 0 && share <= 1) || !(speed > 0) || !(factor > 0)) {
                throw new IllegalArgumentException(
                        "a reflect has a window and a share in (0, 1], a speed and a factor: " + window + ", " + share
                                + ", " + speed + ", " + factor);
            }
        }
    }
}
