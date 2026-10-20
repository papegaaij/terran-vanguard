package vanguard.sim;

import java.util.Optional;

/**
 * A part of a set piece: a hit box at ({@code dx}, {@code dy}) px from the centre in the unit's
 * own frame (facing down the screen: dx to the right, dy up towards its tail), turned with the
 * pass's heading.
 *
 * @param vital destroying it destroys the rest of the unit
 * @param gun the attack it fires while the unit is on the player's layer
 * @param firstShotSeconds when it first fires after the unit has reached the player's layer
 * @param multiplier the damage it takes is multiplied by this (a weak point: a boss's lime eyes)
 */
public record PartSpec(
        String name,
        double dx,
        double dy,
        Hitbox box,
        double hp,
        boolean vital,
        int bounty,
        Optional<EnemyGun> gun,
        double firstShotSeconds,
        double multiplier) {
    public PartSpec(
            String name,
            double dx,
            double dy,
            Hitbox box,
            double hp,
            boolean vital,
            int bounty,
            Optional<EnemyGun> gun,
            double firstShotSeconds) {
        this(name, dx, dy, box, hp, vital, bounty, gun, firstShotSeconds, 1);
    }
}
