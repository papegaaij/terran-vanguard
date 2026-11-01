package vanguard.content;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

/**
 * Step DB1 (design/ui/databank, Where the data comes from): the databank texts' checks across files. Every key names
 * what the game has built: a unit's or boss's slug (a unit under {@code enemies}, a boss, one with a {@code boss} block,
 * under {@code bosses}), a weapon, a special, a core part, a utility or an escort; a concept page opens after a level
 * whose data exists. The line widths and counts are the records' own ({@link DatabankData}). That every built unit, boss
 * and reachable gear item has its lines is {@link #missing}, which the content tests assert on the design data (a test's
 * stand-in units have none, and the screen reads a missing text as no lines).
 */
final class DatabankChecks {
    private DatabankChecks() {}

    static void check(Content content, BiConsumer<String, String> problem) {
        DatabankData texts = content.databank();
        Set<String> units = new TreeSet<>();
        Set<String> bosses = new TreeSet<>();
        content.enemies().forEach((slug, enemy) -> (enemy.boss().isPresent() ? bosses : units).add(slug));
        keys("enemies", texts.enemies().keySet(), units, "a unit (a boss's lines go under bosses)", problem);
        keys("bosses", texts.bosses().keySet(), bosses, "a boss", problem);
        for (String slug : texts.factionFrom().keySet()) {
            if (!content.enemies().containsKey(slug)) {
                problem.accept("faction_from." + slug, "no unit '" + slug + "'");
            }
        }
        gear(content).forEach((section, items) -> {
            for (String slug : texts.gear().get(section).keySet()) {
                if (!items.containsKey(slug)) {
                    problem.accept(section + "." + slug, "no item '" + slug + "' (known: " + items.keySet() + ")");
                }
            }
        });
        texts.concepts().forEach((slug, concept) -> {
            if (content.levelKey(concept.after()).isEmpty()) {
                problem.accept(
                        "concepts." + slug + ".after",
                        "Level " + concept.after() + " has no data: a page opens after a built level");
            }
        });
    }

    /** {@code keys} must all be in {@code known}. */
    private static void keys(
            String section, Set<String> keys, Set<String> known, String what, BiConsumer<String, String> problem) {
        for (String slug : keys) {
            if (!known.contains(slug)) {
                problem.accept(section + "." + slug, "not " + what + " with a data file");
            }
        }
    }

    /**
     * What has no text yet: every unit and boss with a data file without its lines, every gear item the campaign can
     * reach (its unlock at or before the last level with data) without its note, each as {@code section: slug}.
     */
    static java.util.List<String> missing(Content content) {
        DatabankData texts = content.databank();
        java.util.List<String> missing = new java.util.ArrayList<>();
        content.enemies().forEach((slug, enemy) -> {
            boolean boss = enemy.boss().isPresent();
            if (!(boss ? texts.bosses() : texts.enemies()).containsKey(slug)) {
                missing.add((boss ? "bosses" : "enemies") + ": " + slug);
            }
        });
        int lastLevel = content.levels().keySet().stream()
                .mapToInt(Content::levelNumber)
                .max()
                .orElse(0);
        gear(content)
                .forEach((section, items) -> items.forEach((slug, unlock) -> {
                    if (unlock <= lastLevel && !texts.gear().get(section).containsKey(slug)) {
                        missing.add(section + ": " + slug);
                    }
                }));
        return missing;
    }

    /** The level an availability names, or one past every level when it names none (its own check reports it). */
    private static int firstLevel(PlayerData player, String available) {
        try {
            return player.firstLevel(available);
        } catch (IllegalArgumentException e) {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Every gear item by its texts section, slug to the level from whose hangar visit the campaign can know it (the
     * shop's unlock; Rook's join, the Bulwark's availability). The sensor suite has its own section.
     */
    static Map<String, Map<String, Integer>> gear(Content content) {
        PlayerData player = content.player();
        Map<String, Map<String, Integer>> gear = new LinkedHashMap<>();
        Map<String, Integer> weapons = new LinkedHashMap<>();
        content.weapons().forEach((slug, weapon) -> weapons.put(slug, weapon.unlock()));
        gear.put("weapons", weapons);
        Map<String, Integer> specials = new LinkedHashMap<>();
        content.specials()
                .specials()
                .forEach(special -> specials.put(DatabankData.slug(special.name()), special.unlock()));
        gear.put("specials", specials);
        Map<String, Integer> generators = new LinkedHashMap<>();
        content.generators()
                .models()
                .forEach(model ->
                        generators.put(DatabankData.slug(model.name()), firstLevel(player, model.available())));
        gear.put("generators", generators);
        Map<String, Integer> shields = new LinkedHashMap<>();
        content.shields()
                .models()
                .forEach(model -> shields.put(DatabankData.slug(model.name()), firstLevel(player, model.available())));
        gear.put("shields", shields);
        Map<String, Integer> plating = new LinkedHashMap<>();
        content.armour()
                .plating()
                .forEach(model -> plating.put(DatabankData.slug(model.name()), firstLevel(player, model.available())));
        gear.put("plating", plating);
        Map<String, Integer> engines = new LinkedHashMap<>();
        content.systems()
                .engines()
                .forEach(model -> engines.put(DatabankData.slug(model.name()), firstLevel(player, model.available())));
        gear.put("engines", engines);
        Map<String, Integer> utilities = new LinkedHashMap<>();
        content.systems().utility().stream()
                .filter(module -> !DatabankData.slug(module.name()).equals(SENSOR_SUITE))
                .forEach(module ->
                        utilities.put(DatabankData.slug(module.name()), firstLevel(player, module.available())));
        gear.put("utilities", utilities);
        Map<String, Integer> escort = new LinkedHashMap<>();
        escort.put("rook", content.wingmen().rook().joins());
        escort.put("bulwark", firstLevel(player, content.wingmen().bulwark().available()));
        gear.put("escort", escort);
        return gear;
    }

    /** The sensor suite's slug: its texts are their own section, {@code sensor-suite}. */
    static final String SENSOR_SUITE = "sensor-suite";
}
