package vanguard.content;

import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * M8 part E (Level 25 Siren Trench): the autopilot's play in the trench (see {@link Autopilot}). A Siren's nest stands
 * on a wall's foot (design/enemies/naval/siren): its body is rock to the hull while it scrolls past ({@link
 * HazardPlay#canyon}); its sac is shot as any unit (shooting the sacs first, step E4 measured, left the small units
 * longer on the screen and the ship more often in the needles: hard lost more). While a ring has slowed the ship it looks further
 * ahead: at the rock (the pillar's fork, the narrows) and at the bullets. The trench (a terrain with notches) is looked
 * at further ahead than a canyon: the water's drag makes the ship slow to turn. No level before it has a nest, a ring
 * or a notch, so nothing here changes another level's flight.
 */
final class SirenPlay {
    private SirenPlay() {}

    /** The share of the ship's speed and acceleration while a ring slows it (the Siren's {@code slow.factor}). */
    static final double SLOW = 0.6;

    /** How far ahead it looks at the trench's rock, s (a canyon's {@link HazardPlay#ROCK_AHEAD}). */
    static final double TRENCH_AHEAD = 1.2;

    /** How far ahead it looks at the rock coming down at its height, s: further in a trench, and while slowed. */
    static double rockAhead(Sortie sortie, Terrain terrain) {
        if (terrain.notches().isEmpty()) {
            return HazardPlay.ROCK_AHEAD;
        }
        return TRENCH_AHEAD / (sortie.ship().slow().slowed() ? SLOW : 1);
    }

    /** The bullets' look-ahead while slowed: the slow's share longer (it moves slower); {@code range} unslowed. */
    static double dangerRange(Sortie sortie, double range) {
        return sortie.ship().slow().slowed() ? range / SLOW : range;
    }
}
