package vanguard.content.databank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import vanguard.content.Content;
import vanguard.content.DatabankData;
import vanguard.content.Difficulty;
import vanguard.content.EnemyData;
import vanguard.content.WeaponData;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.DatabankRecord;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Intel;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.LoadoutSlot;

/**
 * A campaign's databank (step DB1, design/ui/databank): what it knows (a unit or boss once met; a gear item once its
 * unlock is at or before the next level, or owned or unlocked; a concept once its level is flown), what it has not read
 * yet ({@code NEW}), the sensor level its enemy entries are read at (the best sensor suite it owns, fitted or stored, plus
 * the difficulty's bonus, at most 3: the hangar intel's rule counting a stored suite, pending the user's review in round
 * 82), each tab's list with the unknown rows of the acts reached, and the pages. A snapshot: build a new view after the
 * campaign changes.
 */
public final class DatabankView {
    private final Databank databank;
    private final Content content;
    private final Catalogue catalogue;
    private final Campaign campaign;
    private final DatabankRecord record;
    private final int sensor;
    private final List<Owned> owned;

    /** An item the campaign owns: its kind, catalogue id and level, and its slot when fitted. */
    private record Owned(ItemKind kind, String id, int level, Optional<LoadoutSlot> slot) {}

    DatabankView(Databank databank, Campaign campaign) {
        this.databank = databank;
        this.content = databank.content();
        this.catalogue = databank.catalogue();
        this.campaign = campaign;
        this.record = campaign.databank();
        List<Owned> items = new ArrayList<>();
        campaign.gear().loadout().forEach((slot, fitted) -> {
            if (slot != LoadoutSlot.ESCORT) {
                items.add(new Owned(slot.kind(), fitted.item(), fitted.level(), Optional.of(slot)));
            }
        });
        campaign.gear().inventory().forEach((kind, list) -> {
            if (kind != ItemKind.ESCORT) {
                list.forEach(fitted -> items.add(new Owned(kind, fitted.item(), fitted.level(), Optional.empty())));
            }
        });
        this.owned = List.copyOf(items);
        int suite = owned.stream()
                .filter(item -> item.kind() == ItemKind.UTILITY && item.id().equals(Databank.SENSOR_SUITE))
                .mapToInt(Owned::level)
                .max()
                .orElse(0);
        this.sensor = Math.min(Hangar.MAX_SENSOR, suite + catalogue.sensorBonus(campaign.difficulty()));
    }

    /** The sensor level the enemy entries are read at: 0 (none) to 3. */
    public int sensorLevel() {
        return sensor;
    }

    /** Whether the campaign owns a sensor suite (the badge's {@code NONE} otherwise, with easy's bonus aside). */
    public boolean ownsSuite() {
        return owned.stream()
                .anyMatch(item -> item.kind() == ItemKind.UTILITY && item.id().equals(Databank.SENSOR_SUITE));
    }

    /** The act the campaign is in: its next level's. */
    public int currentAct() {
        return databank.act(Math.min(campaign.nextLevel(), Math.max(1, databank.lastLevel() + 1)));
    }

    /** Whether the campaign knows {@code entry}. */
    public boolean known(Entry entry) {
        int next = campaign.nextLevel();
        return switch (entry.kind()) {
            case UNIT, BOSS -> record.met(entry.slug());
            case CONCEPT -> entry.level() < next;
            case ESCORT ->
                entry.slug().equals("rook")
                        ? campaign.gear().escort().hired() || entry.level() <= next
                        : campaign.gear().escort().bulwark().owned() || entry.level() <= next;
            default ->
                entry.level() <= next
                        || owns(entry)
                        || entry.itemId().filter(campaign.unlocks()::contains).isPresent();
        };
    }

    /** Whether {@code entry} is known and not opened yet: its {@code NEW} tag. */
    public boolean fresh(Entry entry) {
        return known(entry) && !record.wasRead(entry.id());
    }

    /** Whether {@code entry} counts as unknown in its tab: not known, and of an act the campaign has reached. */
    public boolean unknown(Entry entry) {
        return !known(entry) && entry.act() <= currentAct();
    }

    /** The known entries of {@code tab}. */
    public int knownCount(Entry.Tab tab) {
        return (int) databank.entries(tab).stream().filter(this::known).count();
    }

    /**
     * {@code tab}'s list: units and bosses by the act they were met in, in the order met (first level, then name),
     * each act's unknown rows after its known ones; gear by its group, concepts by their act, in the data's order; no
     * row of an act not reached.
     */
    public Pages.TabList list(Entry.Tab tab) {
        List<Entry> entries = databank.entries(tab);
        List<Pages.Row> rows = new ArrayList<>();
        int known = 0;
        int unknown = 0;
        if (tab == Entry.Tab.ENEMIES || tab == Entry.Tab.BOSSES) {
            Map<Integer, List<Pages.Row>> byAct = new java.util.TreeMap<>();
            List<Entry> met = entries.stream()
                    .filter(this::known)
                    .sorted(Comparator.comparingInt((Entry e) -> firstMet(e)).thenComparing(Entry::name))
                    .toList();
            for (Entry entry : met) {
                byAct.computeIfAbsent(databank.act(firstMet(entry)), act -> new ArrayList<>())
                        .add(new Pages.Row(
                                Optional.of(entry),
                                Databank.actLabel(databank.act(firstMet(entry))),
                                fresh(entry),
                                "L" + two(firstMet(entry))));
                known++;
            }
            for (Entry entry : entries) {
                if (unknown(entry)) {
                    byAct.computeIfAbsent(entry.act(), act -> new ArrayList<>())
                            .add(unknownRow(entry, Databank.actLabel(entry.act())));
                    unknown++;
                }
            }
            byAct.values().forEach(rows::addAll);
        } else {
            Map<String, List<Pages.Row>> byGroup = new LinkedHashMap<>();
            for (Entry entry : entries) {
                List<Pages.Row> group = byGroup.computeIfAbsent(entry.group(), g -> new ArrayList<>());
                if (known(entry)) {
                    group.add(new Pages.Row(Optional.of(entry), entry.group(), fresh(entry), right(entry)));
                    known++;
                } else if (unknown(entry)) {
                    group.add(unknownRow(entry, entry.group()));
                    unknown++;
                }
            }
            byGroup.values().forEach(rows::addAll);
        }
        return new Pages.TabList(tab, rows, known, unknown);
    }

    private static Pages.Row unknownRow(Entry entry, String group) {
        return new Pages.Row(Optional.empty(), group, false, Databank.actLabel(entry.act()));
    }

    private String right(Entry entry) {
        if (entry.kind() == Entry.Kind.CONCEPT) {
            return "L" + two(entry.level());
        }
        if (entry.kind() == Entry.Kind.ESCORT) {
            return escortFlying(entry) ? "FITTED" : escortOwned(entry) ? "OWNED" : "SHOP";
        }
        Optional<Owned> item = ownedItem(entry);
        return item.map(o -> o.slot().isPresent() ? "FITTED" : "OWNED").orElse("SHOP");
    }

    private static String two(int level) {
        return String.format(Locale.ROOT, "%02d", level);
    }

    /** The first level the campaign met a unit or boss in (its data's first level while unmet). */
    public int firstMet(Entry entry) {
        DatabankRecord.Met met = record.met().get(entry.slug());
        return met == null ? entry.level() : met.first();
    }

    /** The level a met unit's numbers are of: the last level flown that holds it, else its first met. */
    public int lastMet(Entry entry) {
        Difficulty difficulty = campaign.difficulty();
        for (int level = Math.min(campaign.nextLevel() - 1, databank.lastLevel()); level > firstMet(entry); level--) {
            Optional<LevelUnits> units = databank.units(level, difficulty);
            if (units.isPresent() && units.get().slugs().contains(entry.slug())) {
                return level;
            }
        }
        return firstMet(entry);
    }

    /**
     * Whether a met boss is beaten: killed, or its level is behind the campaign (a migrated version 6 save or a
     * {@code playthroughSave} campaign starts its kills at 0). Its level is the last one with data that holds it, so a
     * boss seen ahead of its fight is not beaten by the earlier level.
     */
    boolean beaten(Entry entry, int kills) {
        if (kills > 0) {
            return true;
        }
        if (!record.met(entry.slug())) {
            return false;
        }
        Difficulty difficulty = campaign.difficulty();
        for (int level = databank.lastLevel(); level >= firstMet(entry); level--) {
            Optional<LevelUnits> units = databank.units(level, difficulty);
            if (units.isPresent() && units.get().slugs().contains(entry.slug())) {
                return level < campaign.nextLevel();
            }
        }
        return false;
    }

    /** The page of a known unit or boss at the campaign's sensor level. */
    public Pages.EnemyPage enemyPage(Entry entry) {
        if (!entry.kind().unit()) {
            throw new IllegalArgumentException(entry.id() + " is not a unit or a boss");
        }
        EnemyData unit = content.enemy(entry.slug());
        DatabankData texts = content.databank();
        boolean boss = entry.kind() == Entry.Kind.BOSS;
        int first = firstMet(entry);
        int last = lastMet(entry);
        int kills = Optional.ofNullable(record.met().get(entry.slug()))
                .map(DatabankRecord.Met::kills)
                .orElse(0);
        Optional<UnitNumbers> numbers =
                databank.units(last, campaign.difficulty()).flatMap(units -> units.numbers(entry.slug()));
        List<Pages.Field> fields = new ArrayList<>();
        fields.add(new Pages.Field("CLASS", wrap(classLine(entry, unit, last)), 1));
        fields.add(new Pages.Field("LAYER", wrap(layerLine(unit)), 1));
        if (!boss) {
            fields.add(new Pages.Field("FROM", List.of(fromLine(entry)), 1));
        }
        fields.add(new Pages.Field("KILLS", List.of(Integer.toString(kills)), 1));
        if (boss) {
            String par = parLine(unit.boss().orElseThrow().par());
            fields.add(new Pages.Field(
                    "BEATEN", List.of(beaten(entry, kills) ? "L" + two(last) + "  " + par : "NOT YET  " + par), 1));
        }
        String at =
                "AT L" + two(last) + ", " + Databank.upper(campaign.difficulty().name());
        fields.add(new Pages.Field(
                "HP",
                numbers.map(n -> n.parts().isEmpty()
                                ? List.of(thousands(n.hp()) + "  " + at)
                                : concat(wrap(thousands(n.hp()) + "  " + split(n.parts())), List.of(at)))
                        .orElse(List.of("-")),
                2));
        numbers.flatMap(UnitNumbers::shield)
                .ifPresent(points -> fields.add(new Pages.Field("SHIELD", List.of(thousands(points) + " HEX"), 2)));
        numbers.flatMap(UnitNumbers::mirrorHeat)
                .ifPresent(heat -> fields.add(new Pages.Field("MIRROR", List.of("HEAT " + thousands(heat)), 2)));
        fields.add(new Pages.Field(
                "BOUNTY",
                List.of(numbers.map(n -> thousands(n.bounty()) + " CR").orElse("-")),
                2));
        fields.add(new Pages.Field("ATTACKS", attackLines(unit), 2));
        fields.add(new Pages.Field("WEAK", wrap(weakLine(unit)), 3));
        List<String> varga;
        List<String> phases = List.of();
        if (boss) {
            // A unit without its text (a test's stand-in) reads no lines.
            DatabankData.BossText text = texts.bosses().get(entry.slug());
            varga = text == null ? List.of() : sensor >= 3 ? text.tip() : text.vague();
            phases = text == null || sensor < 3 ? List.of() : text.phases();
        } else {
            DatabankData.UnitText text = texts.enemies().get(entry.slug());
            varga = text == null ? List.of() : sensor >= 3 ? text.tip() : text.vague();
        }
        List<Advice.Row> advice = List.of();
        if (unit.isNeutral()) {
            if (entry.tag().filter("NEUTRAL"::equals).isPresent()) {
                advice = List.of(new Advice.Row("DO NOT FIRE", "DESTROYING ONE COSTS THE SECONDARY", true, List.of()));
            }
        } else if (sensor >= 3) {
            advice = advice(entry.slug(), unit, numbers);
        }
        double size = Math.max(unit.size().width(), unit.size().height());
        Pages.Picture picture = new Pages.Picture(sensor < 2, boss || size > 128, size < 40 ? 2 : 1);
        return new Pages.EnemyPage(
                entry,
                sensor,
                first,
                Databank.upper(content.levelKey(first).map(Content::levelName).orElse("")),
                last,
                kills,
                entry.tag(),
                picture,
                fields,
                varga,
                advice,
                phases,
                numbers,
                sensor == 0);
    }

    /** The advice rows for a unit with the player's gear (fitted first, then owned). */
    List<Advice.Row> advice(String slug, EnemyData unit, Optional<UnitNumbers> numbers) {
        Optional<Double> beam = owned.stream()
                .filter(item -> item.slot().isPresent() && item.kind().weapon())
                .filter(item -> catalogue.item(item.kind(), item.id()).traits().contains("beam"))
                .map(item -> catalogue
                        .item(item.kind(), item.id())
                        .stats(item.level())
                        .get(Catalogue.Stat.DPS))
                .filter(java.util.Objects::nonNull)
                .findFirst();
        List<Advice.Row> rows = new ArrayList<>();
        for (Advice.Rule rule : Advice.rules(content, slug, unit, numbers.flatMap(UnitNumbers::mirrorHeat), beam)) {
            rows.add(new Advice.Row(rule.label(), rule.reason(), rule.red(), gear(rule)));
        }
        return rows;
    }

    private List<Advice.GearRef> gear(Advice.Rule rule) {
        List<Advice.GearRef> refs = new ArrayList<>();
        List<Owned> ordered = new ArrayList<>(owned);
        ordered.sort(Comparator.comparing((Owned item) -> item.slot().isEmpty()));
        for (Owned item : ordered) {
            if (!item.kind().weapon() && item.kind() != ItemKind.SPECIAL) {
                continue;
            }
            Catalogue.Item shop = catalogue.item(item.kind(), item.id());
            boolean match = rule.anyFront()
                    ? item.slot().filter(slot -> slot == LoadoutSlot.FRONT).isPresent()
                    : rule.matches().test(Advice.item(content, shop));
            if (match) {
                refs.add(new Advice.GearRef(
                        item.kind(),
                        item.id(),
                        shop.name(),
                        item.level(),
                        item.slot().isPresent()));
            }
        }
        return refs;
    }

    /** The page of a known gear item, in full. */
    public Pages.GearPage gearPage(Entry entry) {
        if (!entry.kind().gear()) {
            throw new IllegalArgumentException(entry.id() + " is not gear");
        }
        DatabankData texts = content.databank();
        if (entry.kind() == Entry.Kind.ESCORT) {
            boolean rook = entry.slug().equals("rook");
            String yours = rook
                    ? (campaign.gear().escort().hired()
                            ? "HIRED - ON THE "
                                    + Databank.upper(
                                            campaign.gear().escort().side().name())
                            : "JOINS AT L" + two(entry.level()))
                    : (campaign.gear().escort().bulwark().owned()
                            ? "OWNED"
                            : "NOT OWNED - "
                                    + thousands(content.wingmen().bulwark().price()) + " CR");
            return new Pages.GearPage(
                    entry,
                    note(texts.escort(), entry.slug()),
                    yours,
                    escortOwned(entry) ? 1 : 0,
                    escortFlying(entry),
                    rook ? catalogue.items(ItemKind.ESCORT) : List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of());
        }
        ItemKind kind = entry.itemKind().orElseThrow();
        Catalogue.Item item = catalogue.item(kind, entry.itemId().orElseThrow());
        Optional<Owned> mine = ownedItem(entry);
        String yours;
        if (kind == ItemKind.SPECIAL) {
            int charges = campaign.gear().specials().getOrDefault(item.id(), 0);
            yours = item.cooldownSpecial()
                    ? (mine.isPresent() ? "OWNED" : "NOT OWNED - " + thousands(item.price()) + " CR")
                    : charges + " OF " + Advice.number(item.stats(1).get(Catalogue.Stat.CHARGES)) + " CHARGES - "
                            + thousands(item.price()) + " CR EACH";
        } else {
            yours = mine.map(o -> o.slot().isPresent()
                            ? "L" + o.level() + "  FITTED" + where(o.slot().get())
                            : "OWNED L" + o.level())
                    .orElse("NOT OWNED - " + thousands(item.price()) + " CR");
        }
        List<String> note =
                switch (entry.kind()) {
                    case WEAPON -> note(texts.weapons(), entry.slug());
                    case SPECIAL -> note(texts.specials(), entry.slug());
                    case GENERATOR -> note(texts.generators(), entry.slug());
                    case SHIELD -> note(texts.shields(), entry.slug());
                    case PLATING -> note(texts.plating(), entry.slug());
                    case ENGINE -> note(texts.engines(), entry.slug());
                    case UTILITY -> note(texts.utilities(), entry.slug());
                    case SENSOR_SUITE -> texts.sensorSuite().note();
                    default -> List.of();
                };
        List<String> hits = List.of();
        List<String> not = List.of();
        if (entry.kind() == Entry.Kind.WEAPON) {
            List<List<String>> reach = reach(content.weapon(entry.slug()));
            hits = reach.get(0);
            not = reach.get(1);
        }
        List<Pages.Strong> strong = entry.kind() == Entry.Kind.WEAPON || entry.kind() == Entry.Kind.SPECIAL
                ? strongAgainst(Advice.item(content, item))
                : List.of();
        boolean suite = entry.kind() == Entry.Kind.SENSOR_SUITE;
        return new Pages.GearPage(
                entry,
                note,
                yours,
                mine.map(Owned::level).orElse(0),
                mine.flatMap(Owned::slot).isPresent(),
                List.of(item),
                hits,
                not,
                strong,
                suite && !ownsSuite() ? texts.sensorSuite().varga() : List.of(),
                suite ? ladder() : List.of());
    }

    /** An item's note; none for an item without one. */
    private static List<String> note(Map<String, DatabankData.Note> notes, String slug) {
        DatabankData.Note note = notes.get(slug);
        return note == null ? List.of() : note.note();
    }

    private static String where(LoadoutSlot slot) {
        return switch (slot) {
            case FRONT -> " AT THE FRONT";
            case REAR -> " AT THE REAR";
            case LEFT_WING -> " ON THE LEFT WING";
            case RIGHT_WING -> " ON THE RIGHT WING";
            default -> "";
        };
    }

    /** Up to four met units the advice rules name {@code item} for (not a red row), in the order met. */
    private List<Pages.Strong> strongAgainst(Advice.Item item) {
        List<Pages.Strong> strong = new ArrayList<>();
        List<Entry> met = databank.entries().stream()
                .filter(entry -> entry.kind().unit() && known(entry))
                .sorted(Comparator.comparingInt(this::firstMet).thenComparing(Entry::name))
                .toList();
        for (Entry entry : met) {
            EnemyData unit = content.enemy(entry.slug());
            if (unit.isNeutral()) {
                continue;
            }
            for (Advice.Rule rule :
                    Advice.rules(content, entry.slug(), unit, unit.mirror().map(m -> m.heat()), Optional.empty())) {
                if (!rule.red() && !rule.anyFront() && rule.matches().test(item)) {
                    strong.add(new Pages.Strong(entry.id(), entry.name(), rule.label(), rule.reason()));
                    break;
                }
            }
            if (strong.size() == 4) {
                break;
            }
        }
        return strong;
    }

    /**
     * A weapon's layers ({@code HITS} and {@code NOT}) from its {@code hits}, its traits and the layer rules: a
     * ground-only weapon reaches the ground alone, a torpedo or depth charge the {@code sub} layer and the surface,
     * homing and beam weapons {@code high-air} too; {@code anti-ground} doubles on the ground and opens the hardened.
     */
    static List<List<String>> reach(WeaponData weapon) {
        List<String> hits = new ArrayList<>();
        List<String> not = new ArrayList<>();
        boolean antiGround = weapon.traits().contains("anti-ground");
        boolean antiSub = weapon.traits().contains("anti-sub");
        String kind = weapon.hits();
        boolean groundOnly = kind.equals("ground-only");
        boolean torpedo = kind.equals("anti-sub") || kind.equals("depth-charge");
        boolean deflector = kind.equals("deflector");
        boolean high = weapon.traits().contains("homing") || weapon.traits().contains("beam");
        record Layer(String name, boolean reached) {}
        for (Layer layer : List.of(
                new Layer("AIR", !groundOnly && !torpedo && !deflector),
                new Layer("LOW-AIR", !groundOnly && !torpedo && !deflector),
                new Layer("HIGH-AIR", high && !groundOnly && !torpedo && !deflector),
                new Layer(antiGround ? "GROUND X2" : "GROUND", !deflector),
                new Layer("HARDENED", antiGround),
                new Layer("SUB", antiSub || torpedo))) {
            (layer.reached() ? hits : not).add(layer.name());
        }
        if (deflector) {
            hits.add(0, "ENEMY BULLETS");
        }
        return List.of(hits, not);
    }

    /** The sensor suite's table: what each level shows in the hangar intel and here. */
    private List<Pages.Rung> ladder() {
        List<Pages.Rung> rungs = new ArrayList<>();
        for (int level = 0; level <= Hangar.MAX_SENSOR; level++) {
            int at = level;
            List<String> intel = java.util.Arrays.stream(Intel.Field.values())
                    .filter(field -> field.sensor() == at)
                    .map(field -> field.name().replace('_', ' '))
                    .toList();
            List<String> here = new ArrayList<>();
            switch (level) {
                case 0 -> here.addAll(List.of("NAME", "SILHOUETTE", "FIRST MET", "VARGA'S FIRST LINE"));
                case 1 -> here.addAll(List.of("CLASS", "LAYER", "FROM", "KILLS"));
                case 2 -> here.addAll(List.of("PICTURE", "HP", "SHIELD OR MIRROR", "BOUNTY", "ATTACKS"));
                default -> here.addAll(List.of("WEAK", "WEAPON ADVICE", "VARGA'S TIP", "BOSS PHASES"));
            }
            rungs.add(new Pages.Rung(level, intel, here));
        }
        return rungs;
    }

    /** The concept entry that explains the weapon traits. */
    public static final String WEAPON_TRAITS = "weapon-traits";

    /** The page of a known concept. */
    public Pages.ConceptPage conceptPage(Entry entry) {
        if (entry.kind() != Entry.Kind.CONCEPT) {
            throw new IllegalArgumentException(entry.id() + " is not a concept");
        }
        DatabankData.Concept concept = content.databank().concepts().get(entry.slug());
        // The weapon traits' entry lists every trait with its meaning (the user's Act 6 playthrough, 2026-11-03).
        List<String> rows = entry.slug().equals(WEAPON_TRAITS)
                ? vanguard.content.campaign.TraitWords.ALL.stream()
                        .map(trait -> vanguard.content.campaign.TraitWords.explained(trait.tag()))
                        .toList()
                : List.of();
        return new Pages.ConceptPage(entry, concept.title(), concept.picture(), concept.lines(), rows);
    }

    private boolean owns(Entry entry) {
        return ownedItem(entry).isPresent();
    }

    private Optional<Owned> ownedItem(Entry entry) {
        if (entry.itemKind().isEmpty() || entry.itemId().isEmpty()) {
            return Optional.empty();
        }
        return owned.stream()
                .filter(item -> item.kind() == entry.itemKind().get()
                        && item.id().equals(entry.itemId().get()))
                .max(Comparator.comparing((Owned item) -> item.slot().isPresent()));
    }

    private boolean escortOwned(Entry entry) {
        return entry.slug().equals("rook")
                ? campaign.gear().escort().hired()
                : campaign.gear().escort().bulwark().owned();
    }

    private boolean escortFlying(Entry entry) {
        boolean bulwark = campaign.gear().escort().flies() == vanguard.content.campaign.Escort.Escorter.BULWARK;
        return escortOwned(entry) && (entry.slug().equals("rook") != bulwark);
    }

    /** {@code CLASS}: the faction (a {@code faction_from}'s from that level, an unmarked unit's alone), tier, machine. */
    private String classLine(Entry entry, EnemyData unit, int level) {
        String faction =
                content.databank().factionAt(entry.slug(), level).orElseGet(() -> unit.factionIn(databank.act(level)));
        if (faction.startsWith("Unmarked")) {
            faction = "Unmarked";
        }
        List<String> parts = new ArrayList<>();
        parts.add(Databank.upper(faction));
        if (entry.kind() == Entry.Kind.BOSS) {
            parts.add(entry.tag().isPresent() ? "MID-BOSS" : "ACT BOSS");
        } else {
            parts.add(Databank.upper(unit.tier().name()));
        }
        if (unit.isMachine()) {
            parts.add("MACHINE");
        }
        return String.join(" - ", parts);
    }

    /** {@code LAYER}: the layer with what reaches it. */
    static String layerLine(EnemyData unit) {
        return switch (unit.layer()) {
            case "ground" ->
                unit.armour().hardened() ? "GROUND - HARDENED: ANTI-GROUND ONLY" : "GROUND - ANTI-GROUND X2";
            case "high-air" -> "HIGH-AIR - HOMING AND BEAM ONLY";
            case "sub" -> "SUB - ANTI-SUB ONLY";
            case "low-air" -> "LOW-AIR - EVERY WEAPON REACHES IT";
            default -> Databank.upper(unit.layer()) + " - EVERY WEAPON REACHES IT";
        };
    }

    /** {@code FROM}: the shares of the edges its waves came in by in the levels flown, at the campaign's difficulty. */
    private String fromLine(Entry entry) {
        LevelUnits.Edges sum = new LevelUnits.Edges(0, 0, 0);
        int to = Math.min(Math.max(campaign.nextLevel() - 1, firstMet(entry)), databank.lastLevel());
        for (int level = firstMet(entry); level <= to; level++) {
            Optional<LevelUnits> units = databank.units(level, campaign.difficulty());
            if (units.isPresent()) {
                LevelUnits.Edges edges = units.get().edges().get(entry.slug());
                if (edges != null) {
                    sum = sum.plus(edges);
                }
            }
        }
        double total = sum.total();
        if (total <= 0) {
            return "-";
        }
        List<String> shares = new ArrayList<>();
        for (Map.Entry<String, Double> edge : List.of(
                Map.entry("FRONT", sum.front()), Map.entry("SIDES", sum.sides()), Map.entry("REAR", sum.rear()))) {
            long percent = Math.round(100 * edge.getValue() / total);
            if (percent > 0) {
                shares.add(edge.getKey() + " " + percent + " %");
            }
        }
        return String.join(" ", shares);
    }

    /** {@code ATTACKS}: one line each, its name, pattern, count, bullet class and damage, interval. */
    private List<String> attackLines(EnemyData unit) {
        List<String> lines = new ArrayList<>();
        for (var attack : unit.attacks()) {
            StringBuilder line = new StringBuilder(Databank.upper(attack.name().orElse(attack.pattern())));
            if (attack.name().isPresent() && !attack.name().get().equals(attack.pattern())) {
                line.append(" - ").append(Databank.upper(attack.pattern()));
            }
            attack.burst()
                    .or(attack::count)
                    .ifPresent(n -> line.append(' ').append(n).append(" X"));
            attack.bullet().ifPresent(bullet -> {
                line.append(' ').append(Databank.upper(bullet));
                content.enemyBasis().bulletDamage().stream()
                        .filter(b -> b.bullet().equals(bullet))
                        .findFirst()
                        .ifPresent(b -> line.append(" (")
                                .append(Advice.number(b.damage()))
                                .append(')'));
            });
            attack.interval()
                    .ifPresent(
                            s -> line.append(" EVERY ").append(Advice.number(s)).append(" S"));
            lines.addAll(wrap(line.toString()));
        }
        return lines.isEmpty() ? List.of("NONE") : lines;
    }

    /** {@code WEAK}: its weak points' names and a multi-part unit's vital parts. */
    private static String weakLine(EnemyData unit) {
        List<String> names = new ArrayList<>();
        unit.weakPoints().forEach(point -> names.add(Databank.upper(point.name())));
        unit.partList().ifPresent(parts -> parts.stream()
                .filter(part -> part.kind().equals("vital"))
                .map(part -> Databank.upper(part.name().replace('-', ' ')))
                .filter(name -> names.stream().noneMatch(named -> named.startsWith(name)))
                .forEach(name -> names.add(name + " (VITAL)")));
        return names.isEmpty() ? "NONE" : String.join(", ", names);
    }

    /** A boss's par as {@code PAR 2:25}. */
    private static String parLine(double par) {
        long seconds = Math.round(par);
        return String.format(Locale.ROOT, "PAR %d:%02d", seconds / 60, seconds % 60);
    }

    /**
     * A boss's HP split: the vital parts by name with their HP, the others counted by kind (their names without
     * numbers and sides), {@code CORE 6 600 + 6 X SAM + 4 X RAIL}.
     */
    static String split(List<UnitNumbers.Part> parts) {
        Map<String, Integer> others = new LinkedHashMap<>();
        List<String> pieces = new ArrayList<>();
        for (UnitNumbers.Part part : parts) {
            String base = Databank.upper(part.name()
                    .replaceAll("(^|[-_ ])(\\d+|left|right)(?=$|[-_ ])", "")
                    .replace('-', ' ')
                    .replace('_', ' ')
                    .trim());
            if (part.vital()) {
                pieces.add(base + " " + thousands(part.hp()));
            } else {
                others.merge(base, 1, Integer::sum);
            }
        }
        others.forEach((name, count) -> pieces.add(count == 1 ? name : count + " X " + name));
        return String.join(" + ", pieces);
    }

    /**
     * A generated value as label lines: the multiplication sign as an X (the label font's), the words wrapped at
     * {@link DatabankData#LINE_WIDTH}.
     */
    static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.replace('\u00d7', 'X').split(" ", -1)) {
            if (!line.isEmpty() && line.length() + 1 + word.length() > DatabankData.LINE_WIDTH) {
                lines.add(line.toString().stripTrailing());
                line.setLength(0);
            } else if (!line.isEmpty()) {
                line.append(' ');
            }
            line.append(word);
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static List<String> concat(List<String> first, List<String> then) {
        List<String> all = new ArrayList<>(first);
        all.addAll(then);
        return all;
    }

    /** A number with a space between thousands: {@code 17 220}. */
    static String thousands(double value) {
        return String.format(Locale.ROOT, "%,d", Math.round(value)).replace(',', ' ');
    }
}
