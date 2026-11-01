package vanguard.content.databank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import vanguard.content.Content;
import vanguard.content.DatabankData;
import vanguard.content.Difficulty;
import vanguard.content.EnemyData;
import vanguard.content.WeaponData;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.SaveFormat;

/**
 * The databank's index (step DB1, design/ui/databank): every entry the design data has, built from the content (the
 * units and bosses with a data file, the weapons, specials, core parts, utilities with the sensor suite, the escort, the
 * concept pages of the texts file), and each level's units as the simulation spawns them ({@link LevelUnits}, built
 * once per level and difficulty when first asked). Immutable apart from that cache, and safe to share between threads.
 * A campaign's view of it is {@link #view}.
 */
public final class Databank {
    /** The sensor suite's id, as the catalogue names its utility and as the entry is keyed. */
    public static final String SENSOR_SUITE = "Sensor suite";

    private final Content content;
    private final Catalogue catalogue;
    private final List<Entry> entries;
    private final Map<String, Entry> byId;
    private final Map<String, Optional<LevelUnits>> units = new ConcurrentHashMap<>();
    private final int lastLevel;

    private Databank(Content content) {
        this.content = content;
        this.catalogue = Catalogue.of(content);
        this.lastLevel = content.levels().keySet().stream()
                .mapToInt(Content::levelNumber)
                .max()
                .orElse(0);
        List<Entry> list = new ArrayList<>();
        units(list);
        weapons(list);
        ship(list);
        concepts(list);
        entries = List.copyOf(list);
        Map<String, Entry> ids = new LinkedHashMap<>();
        for (Entry entry : entries) {
            if (ids.put(entry.id(), entry) != null) {
                throw new IllegalStateException("two databank entries are '" + entry.id() + "'");
            }
        }
        byId = java.util.Collections.unmodifiableMap(ids);
    }

    /** The databank of {@code content}. */
    public static Databank of(Content content) {
        return new Databank(content);
    }

    public Content content() {
        return content;
    }

    public Catalogue catalogue() {
        return catalogue;
    }

    /** Every entry, by tab, in each tab's data order. */
    public List<Entry> entries() {
        return entries;
    }

    /** The entries of {@code tab}. */
    public List<Entry> entries(Entry.Tab tab) {
        return entries.stream().filter(entry -> entry.tab() == tab).toList();
    }

    /** The entry {@code id}, if there is one. */
    public Optional<Entry> entry(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** The entry of a unit or boss slug (the intel panel's portraits link to it). */
    public Optional<Entry> unit(String slug) {
        return entry(slug).filter(entry -> entry.kind().unit());
    }

    /** {@code campaign}'s view: what it knows, its sensor level, the lists and the pages. */
    public DatabankView view(Campaign campaign) {
        return new DatabankView(this, campaign);
    }

    /** Level {@code level}'s units at {@code difficulty}; empty for a level without data. */
    public Optional<LevelUnits> units(int level, Difficulty difficulty) {
        return units.computeIfAbsent(level + "/" + difficulty, key -> content.levelKey(level)
                .map(levelKey -> LevelUnits.of(content, levelKey, difficulty)));
    }

    /** The last level with data. */
    public int lastLevel() {
        return lastLevel;
    }

    /** The act of level {@code level} (1–7); a level no act's data holds is of the act after the last one. */
    public int act(int level) {
        return content.actOf(level)
                .map(act -> actNumber(act.getKey()))
                .orElseGet(() -> content.acts().keySet().stream()
                                .mapToInt(Databank::actNumber)
                                .max()
                                .orElse(0)
                        + 1);
    }

    private static int actNumber(String directory) {
        return Integer.parseInt(directory.substring("act-".length(), directory.indexOf('-', "act-".length())));
    }

    /** {@code ACT VI} for act 6. */
    public static String actLabel(int act) {
        String[] numerals = {"0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
        return "ACT " + (act >= 0 && act < numerals.length ? numerals[act] : Integer.toString(act));
    }

    /**
     * The roster the version 6 to 7 save migration reads ({@link SaveFormat.Roster}): every unit, set piece and boss of
     * the levels below the next level at the save's difficulty with the first of them it flies in; the gear and the
     * concepts a campaign before that level knows by its level alone.
     */
    public SaveFormat.Roster roster() {
        return new SaveFormat.Roster() {
            @Override
            public Map<String, Integer> firstMet(int nextLevel, Difficulty difficulty) {
                Map<String, Integer> first = new LinkedHashMap<>();
                for (int level = 1; level < nextLevel; level++) {
                    int at = level;
                    units(level, difficulty)
                            .ifPresent(flown -> flown.slugs().forEach(slug -> first.putIfAbsent(slug, at)));
                }
                return first;
            }

            @Override
            public List<String> known(int nextLevel) {
                return entries.stream()
                        .filter(entry -> !entry.kind().unit())
                        .filter(entry -> entry.kind() == Entry.Kind.CONCEPT
                                ? entry.level() < nextLevel
                                : entry.level() <= nextLevel)
                        .map(Entry::id)
                        .toList();
            }
        };
    }

    private void units(List<Entry> list) {
        Set<String> setPieces = new TreeSet<>();
        content.levels().values().forEach(level -> level.setPieces()
                .ifPresent(pieces -> pieces.forEach(piece -> setPieces.add(piece.enemy()))));
        Set<String> spared = new TreeSet<>();
        content.levels().values().forEach(level -> level.objectives()
                .secondary()
                .flatMap(secondary -> secondary.spared())
                .ifPresent(spared::add));
        Comparator<Map.Entry<String, EnemyData>> order = Comparator.comparingInt(
                        (Map.Entry<String, EnemyData> e) -> e.getValue().firstLevel())
                .thenComparing(e -> e.getValue().name());
        int[] index = {0, 0};
        content.enemies().entrySet().stream().sorted(order).forEach(e -> {
            String slug = e.getKey();
            EnemyData unit = e.getValue();
            boolean boss = unit.boss().isPresent();
            Optional<String> tag;
            if (boss) {
                tag = unit.boss().get().midBoss() ? Optional.of("MID-BOSS") : Optional.empty();
            } else if (unit.isNeutral()) {
                tag = Optional.of(spared.contains(slug) ? "NEUTRAL" : "ALLY");
            } else if (setPieces.contains(slug)) {
                tag = Optional.of("SET PIECE");
            } else {
                tag = Optional.empty();
            }
            int act = act(unit.firstLevel());
            list.add(new Entry(
                    slug,
                    boss ? Entry.Tab.BOSSES : Entry.Tab.ENEMIES,
                    boss ? Entry.Kind.BOSS : Entry.Kind.UNIT,
                    slug,
                    unit.name(),
                    actLabel(act),
                    act,
                    unit.firstLevel(),
                    index[boss ? 1 : 0]++,
                    tag,
                    Optional.empty(),
                    Optional.empty()));
        });
    }

    private void weapons(List<Entry> list) {
        int[] order = {0};
        for (WeaponData.Slot slot : WeaponData.Slot.values()) {
            String group =
                    switch (slot) {
                        case FRONT -> "FRONT";
                        case WING -> "WINGS";
                        case REAR -> "REAR";
                    };
            ItemKind kind =
                    switch (slot) {
                        case FRONT -> ItemKind.FRONT;
                        case WING -> ItemKind.WING;
                        case REAR -> ItemKind.REAR;
                    };
            content.weapons().entrySet().stream()
                    .filter(e -> e.getValue().slot() == slot)
                    .sorted(Comparator.comparingInt((Map.Entry<String, WeaponData> e) ->
                                    e.getValue().unlock())
                            .thenComparing(e -> e.getValue().name()))
                    .forEach(e -> list.add(gear(
                            e.getKey(),
                            Entry.Tab.WEAPONS,
                            Entry.Kind.WEAPON,
                            e.getKey(),
                            e.getValue().name(),
                            group,
                            e.getValue().unlock(),
                            order[0]++,
                            kind,
                            e.getKey())));
        }
        for (Catalogue.Item item : catalogue.items(ItemKind.SPECIAL)) {
            String slug = DatabankData.slug(item.id());
            list.add(gear(
                    "special:" + slug,
                    Entry.Tab.WEAPONS,
                    Entry.Kind.SPECIAL,
                    slug,
                    item.name(),
                    "SPECIALS",
                    item.unlock(),
                    order[0]++,
                    ItemKind.SPECIAL,
                    item.id()));
        }
    }

    private void ship(List<Entry> list) {
        int[] order = {0};
        record Part(ItemKind kind, Entry.Kind entry, String prefix) {}
        for (Part part : List.of(
                new Part(ItemKind.GENERATOR, Entry.Kind.GENERATOR, "generator"),
                new Part(ItemKind.SHIELD, Entry.Kind.SHIELD, "shield"),
                new Part(ItemKind.PLATING, Entry.Kind.PLATING, "plating"),
                new Part(ItemKind.ENGINE, Entry.Kind.ENGINE, "engine"))) {
            for (Catalogue.Item item : catalogue.items(part.kind())) {
                String slug = DatabankData.slug(item.id());
                list.add(gear(
                        part.prefix() + ":" + slug,
                        Entry.Tab.SHIP,
                        part.entry(),
                        slug,
                        item.name(),
                        "CORE",
                        item.unlock(),
                        order[0]++,
                        part.kind(),
                        item.id()));
            }
        }
        List<Catalogue.Item> utilities = new ArrayList<>(catalogue.items(ItemKind.UTILITY));
        utilities.sort(Comparator.comparing((Catalogue.Item item) -> !item.id().equals(SENSOR_SUITE)));
        for (Catalogue.Item item : utilities) {
            String slug = DatabankData.slug(item.id());
            boolean suite = item.id().equals(SENSOR_SUITE);
            list.add(gear(
                    "utility:" + slug,
                    Entry.Tab.SHIP,
                    suite ? Entry.Kind.SENSOR_SUITE : Entry.Kind.UTILITY,
                    slug,
                    item.name(),
                    "UTILITY",
                    item.unlock(),
                    order[0]++,
                    ItemKind.UTILITY,
                    item.id()));
        }
        var wingmen = content.wingmen();
        list.add(new Entry(
                "escort:rook",
                Entry.Tab.SHIP,
                Entry.Kind.ESCORT,
                "rook",
                "Rook",
                "ESCORT",
                act(wingmen.rook().joins()),
                wingmen.rook().joins(),
                order[0]++,
                Optional.empty(),
                Optional.of(ItemKind.ESCORT),
                Optional.empty()));
        int bulwark = content.player().firstLevel(wingmen.bulwark().available());
        list.add(new Entry(
                "escort:bulwark",
                Entry.Tab.SHIP,
                Entry.Kind.ESCORT,
                "bulwark",
                "Bulwark",
                "ESCORT",
                act(bulwark),
                bulwark,
                order[0]++,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));
    }

    private void concepts(List<Entry> list) {
        int[] order = {0};
        content.databank().concepts().entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, DatabankData.Concept> e) ->
                        e.getValue().after()))
                .forEach(e -> {
                    int act = act(e.getValue().after());
                    list.add(new Entry(
                            "concept:" + e.getKey(),
                            Entry.Tab.CONCEPTS,
                            Entry.Kind.CONCEPT,
                            e.getKey(),
                            e.getValue().title(),
                            actLabel(act),
                            act,
                            e.getValue().after(),
                            order[0]++,
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty()));
                });
    }

    private Entry gear(
            String id,
            Entry.Tab tab,
            Entry.Kind kind,
            String slug,
            String name,
            String group,
            int level,
            int order,
            ItemKind itemKind,
            String itemId) {
        return new Entry(
                id,
                tab,
                kind,
                slug,
                name,
                group,
                act(level),
                level,
                order,
                Optional.empty(),
                Optional.of(itemKind),
                Optional.of(itemId));
    }

    /** {@code text} in the screen's capitals. */
    static String upper(String text) {
        return text.toUpperCase(Locale.ROOT);
    }
}
