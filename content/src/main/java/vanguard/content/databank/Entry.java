package vanguard.content.databank;

import java.util.Optional;
import vanguard.content.campaign.ItemKind;

/**
 * An entry of the databank's index (step DB1, design/ui/databank): its id (a unit's, a boss's or a weapon's slug;
 * another gear item's {@code <kind>:<slug>}, {@code generator:mk-ii}, {@code utility:sensor-suite}, {@code escort:rook};
 * a concept's {@code concept:<slug>}), its tab, its group in the tab's list for gear and concepts ({@code FRONT},
 * {@code WINGS}, {@code REAR}, {@code SPECIALS}; {@code CORE}, {@code UTILITY}, {@code ESCORT}; the act's for a concept;
 * a unit's is the act it was met in, {@link DatabankView}), its name, its act and level (a unit's first level in its
 * data, a gear item's unlock, a concept's {@code after}), a tag ({@code ALLY}, {@code NEUTRAL}, {@code SET PIECE},
 * {@code MID-BOSS}) and a gear item's catalogue kind and id.
 *
 * @param slug the unit's, item's or concept's slug (a gear item's within its kind)
 * @param order its place among its group's entries in the data's order
 */
public record Entry(
        String id,
        Tab tab,
        Kind kind,
        String slug,
        String name,
        String group,
        int act,
        int level,
        int order,
        Optional<String> tag,
        Optional<ItemKind> itemKind,
        Optional<String> itemId) {
    /** The five tabs, the user's five words. */
    public enum Tab {
        ENEMIES,
        BOSSES,
        WEAPONS,
        SHIP,
        CONCEPTS
    }

    /** What an entry is. */
    public enum Kind {
        UNIT,
        BOSS,
        WEAPON,
        SPECIAL,
        GENERATOR,
        SHIELD,
        PLATING,
        ENGINE,
        UTILITY,
        SENSOR_SUITE,
        ESCORT,
        CONCEPT;

        /** Whether it is the player's gear (shown in full whatever the sensor suite). */
        public boolean gear() {
            return this != UNIT && this != BOSS && this != CONCEPT;
        }

        /** Whether it is a unit or a boss (shown by the sensor suite's level). */
        public boolean unit() {
            return this == UNIT || this == BOSS;
        }
    }
}
