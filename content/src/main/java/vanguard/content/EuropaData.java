package vanguard.content;

import java.util.List;

/**
 * M8 part A: design/world/europa/data.yaml, the under-water rules (design/world/europa, Under water rules): the
 * single definition of underwater play, flown wherever a level or a section of one has {@code medium: underwater}.
 */
public record EuropaData(EuropaData.Underwater underwater) {
    /**
     * The water's factors.
     *
     * @param bulletSpeed every bullet's speed (the player's and the enemies') at its spawn
     * @param acceleration the drag: the ship's (Rook's, the Bulwark's) acceleration and braking; the pressure hull
     *     removes it
     * @param damage a weapon without the {@code anti-sub} trait, after anti-ground's ×2; not specials or the
     *     enemy's damage; not an energy weapon's
     * @param energyDamage M8 AW (the user, 2026-10-23): an energy weapon's damage, in place of {@code damage}
     * @param reach the share of the play field's height an energy weapon reaches
     * @param energyWeapons the energy weapons (slugs): the beam and the lasers, their damage and reach cut
     * @param torpedoRange the factor on the range of {@code torpedoWeapons}
     * @param torpedoWeapons the torpedo-type weapons (slugs)
     * @param ease seconds the factors ease in or out over as the ship crosses a waterline (new projectiles only)
     * @param hydroKit the level whose hangar visit fits the hydro-kit's free refit (its one-off notice)
     */
    public record Underwater(
            double bulletSpeed,
            double acceleration,
            double damage,
            double energyDamage,
            double reach,
            List<String> energyWeapons,
            double torpedoRange,
            List<String> torpedoWeapons,
            double ease,
            int hydroKit) {
        public Underwater {
            Check.positive("bullet_speed", bulletSpeed);
            Check.positive("acceleration", acceleration);
            Check.positive("damage", damage);
            Check.share("energy_damage", energyDamage);
            Check.positive("energy_damage", energyDamage);
            Check.share("reach", reach);
            Check.positive("reach", reach);
            energyWeapons = List.copyOf(energyWeapons);
            Check.positive("torpedo_range", torpedoRange);
            torpedoWeapons = List.copyOf(torpedoWeapons);
            Check.notNegative("ease", ease);
            Check.positive("hydro_kit", hydroKit);
        }

        /** Whether the weapon is an energy weapon: its damage and its beam or laser reach are cut under water. */
        public boolean energy(String weapon) {
            return energyWeapons.contains(weapon);
        }

        /** Whether the weapon's range is a torpedo's, raised under water. */
        public boolean torpedo(String weapon) {
            return torpedoWeapons.contains(weapon);
        }
    }
}
