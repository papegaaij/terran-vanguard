package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Catalogue.Item;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.ItemKind;

/**
 * Every level 01–42 has a loadout that raises no trait warning (PT2, the user's yes of 2026-11-03, design/ui/hangar,
 * Trait warnings in plain words): of the weapons and specials in the shop at that level's hangar visit, one per slot
 * (front, the two wings, rear, the special; the wings and the rear may stay empty), the weapons turned down to L1 on the
 * power dial and drawing within what the balance plan's generator leaves after its core parts and modules at that
 * visit, some combination covers every recommended and required trait of the level by the launch warning's own rules
 * ({@link Hangar#covers}: any front weapon and the Tesla Coil Pod for {@code forward}, {@code rear} only from a rear
 * weapon; for a required {@code anti-ground} also a special that breaks hardened targets; Rook's gun is left out).
 */
class TraitCoverageTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);

    @Test
    void everyLevelHasALoadoutWithoutATraitWarning() {
        List<String> failing = new ArrayList<>();
        Set<Integer> checked = new LinkedHashSet<>();
        for (BalanceTest.Row row : BalanceTest.sheet()) {
            int level = row.level();
            if (level > 42) {
                continue;
            }
            checked.add(level);
            String key = CONTENT.levelKey(level).orElseThrow();
            var profile = CONTENT.level(key).threatProfile();
            Set<String> recommended = new LinkedHashSet<>(profile.traits());
            Set<String> required = new LinkedHashSet<>(profile.requiredTraits());
            double budget = row.output() - (row.load() - row.weaponsLoad());
            Optional<String> missing = search(level, recommended, required, budget);
            missing.ifPresent(
                    text -> failing.add(String.format("L%02d: %s (%.1f MW for the weapons)", level, text, budget)));
        }
        assertEquals(42, checked.size(), "Levels 01-42 in the balance plan");
        assertEquals(List.of(), failing);
    }

    /** Empty when a loadout covers every trait; else the traits the best loadout still misses. */
    private static Optional<String> search(int level, Set<String> recommended, Set<String> required, double budget) {
        List<Item> fronts = shop(ItemKind.FRONT, level);
        List<Optional<Item>> wings = optional(shop(ItemKind.WING, level));
        List<Optional<Item>> rears = optional(shop(ItemKind.REAR, level));
        boolean hardenedSpecial = shop(ItemKind.SPECIAL, level).stream()
                .anyMatch(special -> Hangar.HARDENED_SPECIALS.contains(special.id()));
        Set<String> wanted = new LinkedHashSet<>(recommended);
        wanted.addAll(required);
        Set<String> bestMissing = wanted;
        for (Item front : fronts) {
            for (Optional<Item> left : wings) {
                for (Optional<Item> right : wings) {
                    for (Optional<Item> rear : rears) {
                        List<Item> fitted = new ArrayList<>(List.of(front));
                        left.ifPresent(fitted::add);
                        right.ifPresent(fitted::add);
                        rear.ifPresent(fitted::add);
                        double draw = fitted.stream()
                                .mapToDouble(item -> item.draw(1))
                                .sum();
                        if (draw > budget + 1e-9) {
                            continue;
                        }
                        Set<String> missing = new LinkedHashSet<>();
                        for (String trait : wanted) {
                            boolean covered = fitted.stream().anyMatch(item -> Hangar.covers(item, trait))
                                    || (required.contains(trait)
                                            && trait.equals(Hangar.ANTI_GROUND)
                                            && hardenedSpecial);
                            if (!covered) {
                                missing.add(trait);
                            }
                        }
                        if (missing.isEmpty()) {
                            return Optional.empty();
                        }
                        if (missing.size() < bestMissing.size()) {
                            bestMissing = missing;
                        }
                    }
                }
            }
        }
        return Optional.of("misses " + bestMissing + " of " + wanted);
    }

    /** The items of a kind in the shop at level {@code level}'s visit (the starter's too). */
    private static List<Item> shop(ItemKind kind, int level) {
        return CATALOGUE.items(kind).stream()
                .filter(item -> item.forSale() && item.unlock() <= level)
                .toList();
    }

    private static List<Optional<Item>> optional(List<Item> items) {
        List<Optional<Item>> all = new ArrayList<>();
        all.add(Optional.empty());
        items.forEach(item -> all.add(Optional.of(item)));
        return all;
    }
}
