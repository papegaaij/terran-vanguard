package vanguard.sim;

/**
 * The layers enemies live on, with the layer hit rules of design/enemies/README.md (Layer rules)
 * for weapons without the {@code anti-ground}, {@code homing} or {@code beam} traits, such as the
 * Pulse Cannon. M5 part E (user decision E2 = a): {@link #SUB}, under the water's surface, is hit
 * only by the torpedo ({@link WeaponSpec.Delivery#TORPEDO}, the {@code anti-sub} delivery) and the
 * Smart Bomb; nothing on it touches the ship. M7 part B (design/enemies/ground/burrower, M7 = a):
 * {@link #BURIED}, under the sand, is hit by nothing at all ({@link #touchable()}). The {@code space}
 * and {@code deep} layers follow with the levels that use them.
 */
public enum Layer {
    GROUND(true, false),
    LOW_AIR(true, false),
    /** The player's plane. */
    AIR(true, true),
    HIGH_AIR(false, false),
    /**
     * M5 part E: under the water's surface (a submerged unit's current layer, a sunken trigger).
     * Appended last so the earlier layers keep their ordinals (and the replay hashes theirs).
     */
    SUB(false, false),
    /**
     * M7 part B (design/enemies/ground/burrower, M7 = a): under the sand (a Burrower's current layer
     * while it swims, bulges and from its dive's middle): hit by nothing, not even the Smart Bomb or
     * the Airstrike, no contact, never a target of homing shots, turrets, drones or the wingman.
     * Appended last so the earlier layers keep their ordinals (and the replay hashes theirs).
     */
    BURIED(false, false);

    private final boolean hitByStandardShots;
    private final boolean collidesWithPlayer;

    Layer(boolean hitByStandardShots, boolean collidesWithPlayer) {
        this.hitByStandardShots = hitByStandardShots;
        this.collidesWithPlayer = collidesWithPlayer;
    }

    /** Whether shots of a weapon without special traits hit enemies on this layer. */
    public boolean hitByStandardShots() {
        return hitByStandardShots;
    }

    /**
     * M7 part B: whether anything can touch an enemy on this layer at all: a shot of any delivery, a
     * blast, a special, a beam, the wingman; false only for {@link #BURIED}. Every hit and target path
     * checks it (or a delivery's {@link WeaponSpec.Delivery#reaches}, which does).
     */
    public boolean touchable() {
        return this != BURIED;
    }

    /** Whether enemies on this layer deal contact damage to the player. */
    public boolean collidesWithPlayer() {
        return collidesWithPlayer;
    }
}
