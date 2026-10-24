package vanguard.sim;

import java.util.List;
import java.util.Set;

/**
 * M8 part A (design/world/europa, Under water rules): the medium a level is flown in, built by {@code
 * vanguard.content.SimSpecs} from the level's {@code medium} and its sections' (the waterline is a section's start).
 * Air by default ({@link #AIR}: no switch, nothing of it is hashed, every level before Act 4 flies as before).
 *
 * @param rules the under-water factors (design/world/europa/data.yaml, {@code underwater})
 * @param switches the level clock's seconds at which the medium changes, in time order; one at 0 s sets the level's
 *     medium from its start (no ease), a later one is a crossing: the factors ease to the new medium over the rules'
 *     {@link Rules#easeSeconds()}
 */
public record MediumSpec(Rules rules, List<Switch> switches) {
    /** The air: no water medium at all (every level before Act 4). */
    public static final MediumSpec AIR = new MediumSpec(Rules.NONE, List.of());

    public MediumSpec {
        switches = List.copyOf(switches);
        for (int i = 0; i < switches.size(); i++) {
            if (i > 0 && !(switches.get(i).atSeconds() > switches.get(i - 1).atSeconds())) {
                throw new IllegalArgumentException("the medium's switches are in time order: " + switches);
            }
        }
    }

    /** A level under water from its start to its end. */
    public static MediumSpec underwater(Rules rules) {
        return new MediumSpec(rules, List.of(new Switch(0, true)));
    }

    /** Whether the level has any water medium (then the {@link Medium} is hashed). */
    public boolean water() {
        return switches.stream().anyMatch(Switch::underwater);
    }

    /**
     * From {@code atSeconds} on the level clock the level is flown {@code underwater} (true) or in the air (false).
     */
    public record Switch(double atSeconds, boolean underwater) {
        public Switch {
            if (!(atSeconds >= 0)) {
                throw new IllegalArgumentException("a medium switch is at 0 s or later: " + atSeconds);
            }
        }
    }

    /**
     * The under-water rules' numbers (design/world/europa/README.md, Under water rules; the data's {@code underwater}
     * block).
     *
     * @param bulletSpeed every bullet's speed factor at its spawn (the player's, Rook's and the enemies')
     * @param acceleration the ship's and Rook's acceleration and braking factor (the drag)
     * @param damage the damage factor of a weapon without the {@code anti-sub} trait, after {@code anti-ground}'s ×2
     *     (not an energy weapon's)
     * @param energyDamage M8 AW (the user, 2026-10-23): an energy weapon's damage factor, in place of {@code damage}
     * @param reach an energy weapon's reach, a share of the play field's height from its muzzle
     * @param torpedoRange a torpedo-type weapon's range factor (a depth charge's toss speed too)
     * @param easeSeconds how long the factors take to change at a crossing
     * @param energyWeapons the energy weapons (slugs): the beam and the lances, their reach limited and their damage
     *     {@code energyDamage}
     * @param torpedoWeapons the torpedo-type weapons (slugs)
     */
    public record Rules(
            double bulletSpeed,
            double acceleration,
            double damage,
            double energyDamage,
            double reach,
            double torpedoRange,
            double easeSeconds,
            Set<String> energyWeapons,
            Set<String> torpedoWeapons) {
        /** No factors at all (the air's). */
        public static final Rules NONE = new Rules(1, 1, 1, 1, 1, 1, 0, Set.of(), Set.of());

        public Rules {
            energyWeapons = Set.copyOf(energyWeapons);
            torpedoWeapons = Set.copyOf(torpedoWeapons);
            if (!(bulletSpeed > 0)
                    || !(acceleration > 0)
                    || !(damage > 0)
                    || !(energyDamage > 0)
                    || !(reach > 0)
                    || !(torpedoRange > 0)) {
                throw new IllegalArgumentException("the water's factors are above 0");
            }
            if (!(easeSeconds >= 0)) {
                throw new IllegalArgumentException("the water's ease is 0 s or more: " + easeSeconds);
            }
        }
    }
}
