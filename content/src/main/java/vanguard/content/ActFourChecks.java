package vanguard.content;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * M8 part A's static checks (design/world/europa, Under water rules; design/player/specials, Setting restrictions;
 * design/player/wingmen, the Bulwark and Rook's absence).
 *
 * <p>Europa's: every weapon its energy rule names is a beam or a laser that flies to the screen's edge, every weapon its
 * torpedo rule names is {@code anti-sub}, the hydro-kit's refit comes in Act 4. A level's: {@code medium: underwater}
 * (the level's or a section's) only from Act 4's first level on (the debug option flies a built level under water,
 * never its data); its and its sections' {@code special_limits} name known specials (by slug) that a setting of the
 * specials' {@code limits} lists, so each has its HUD reason word. The wingmen's: Rook is lost after a level of Act 4.
 */
final class ActFourChecks {
    private ActFourChecks() {}

    /** The first level of Act 4, as the shop's availability names it ({@code act 4}). */
    static int firstLevel(Content content) {
        return content.player().firstLevel("act 4");
    }

    /** Europa's under-water rules. */
    static void europa(Content content, BiConsumer<String, String> problem) {
        EuropaData.Underwater water = content.europa().underwater();
        List<String> energy = water.energyWeapons();
        for (int i = 0; i < energy.size(); i++) {
            WeaponData weapon = content.weapons().get(energy.get(i));
            if (weapon == null) {
                problem.accept("underwater.energy_weapons[" + i + "]", "no weapon '" + energy.get(i) + "'");
            } else if (!weapon.beam()
                    && weapon.range()
                            .filter(range -> range.kind() == WeaponData.Range.Kind.SCREEN)
                            .isEmpty()) {
                problem.accept(
                        "underwater.energy_weapons[" + i + "]",
                        energy.get(i) + " is neither a beam nor a laser that flies to the screen's edge");
            }
        }
        List<String> torpedoes = water.torpedoWeapons();
        for (int i = 0; i < torpedoes.size(); i++) {
            WeaponData weapon = content.weapons().get(torpedoes.get(i));
            if (weapon == null) {
                problem.accept("underwater.torpedo_weapons[" + i + "]", "no weapon '" + torpedoes.get(i) + "'");
            } else if (!weapon.antiSub()) {
                problem.accept("underwater.torpedo_weapons[" + i + "]", torpedoes.get(i) + " is not anti-sub");
            }
        }
        if (water.hydroKit() < firstLevel(content)) {
            problem.accept("underwater.hydro_kit", "the hydro-kit is fitted in Act 4, not before L" + water.hydroKit());
        }
    }

    /** Rook's absence starts after a level of Act 4. */
    static void wingmen(Content content, BiConsumer<String, String> problem) {
        WingmenData.Missing missing = content.wingmen().rook().missing();
        if (missing.after() < firstLevel(content)) {
            problem.accept("rook.missing.after", "Rook is lost in Act 4, not in Level " + missing.after());
        }
    }

    /** A level's medium and special limits. */
    static void level(Content content, int number, LevelData level, BiConsumer<String, String> problem) {
        if (level.underwater() && number < firstLevel(content)) {
            problem.accept(
                    "medium", "under water only from Act 4 (Level " + firstLevel(content) + "), not Level " + number);
        }
        level.specialLimits().ifPresent(limits -> specialLimits(content, "special_limits", limits, problem));
        for (int i = 0; i < level.sections().size(); i++) {
            String field = "sections[" + i + "].special_limits";
            level.sections().get(i).specialLimits().ifPresent(limits -> specialLimits(content, field, limits, problem));
        }
    }

    private static void specialLimits(
            Content content, String field, List<String> limits, BiConsumer<String, String> problem) {
        SpecialsData specials = content.specials();
        for (int i = 0; i < limits.size(); i++) {
            String slug = limits.get(i);
            String name;
            try {
                name = specials.named(slug);
            } catch (IllegalArgumentException e) {
                problem.accept(field + "[" + i + "]", "no special '" + slug + "' (a special's slug: orbital-lance)");
                continue;
            }
            if (specials.reason(name).isEmpty()) {
                problem.accept(
                        field + "[" + i + "]",
                        name + " is in no setting's limits (design/player/specials), so it has no reason word");
            }
        }
    }
}
