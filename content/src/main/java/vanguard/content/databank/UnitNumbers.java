package vanguard.content.databank;

import java.util.List;
import java.util.Optional;
import vanguard.content.Difficulty;

/**
 * A unit's numbers in a level at a difficulty (design/ui/databank, Enemy entries: {@code HP}, {@code SHIELD} or {@code
 * MIRROR}, {@code BOUNTY}), from the spec the simulation spawns there ({@link LevelUnits}).
 *
 * @param level the level they are of
 * @param hp its HP; a boss's or a set piece's the sum of its parts'
 * @param shield its hex shield's HP; none without one
 * @param mirrorHeat its mirror's heat; none without one
 * @param bounty the credits one kill pays there; a boss's or set piece's its parts' together
 * @param parts a boss's or set piece's parts with HP, in its order; empty for a unit
 */
public record UnitNumbers(
        int level,
        Difficulty difficulty,
        double hp,
        Optional<Double> shield,
        Optional<Double> mirrorHeat,
        int bounty,
        List<Part> parts) {
    public UnitNumbers {
        parts = List.copyOf(parts);
    }

    /** A part of a boss or set piece: its name, HP, whether it is vital, the credits it pays. */
    public record Part(String name, double hp, boolean vital, int bounty) {}
}
