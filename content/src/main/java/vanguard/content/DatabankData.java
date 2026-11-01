package vanguard.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * design/ui/databank/data.yaml (step DB1): the databank's authored texts (design/ui/databank, Where the data comes
 * from; the schema in design/tech/architecture/schemas-player, Databank texts). Every text is a list of label lines of at
 * most {@value #LINE_WIDTH} characters, so the screen wraps nothing. Keys are slugs: a unit's or a boss's, a gear item's
 * within its kind (a name in lower case, hyphenated, its nickname left out: {@code Mk V+ "Crucible"} is {@code
 * mk-v-plus}), a concept's. The slugs' and the levels' cross-file checks are {@link DatabankChecks}.
 *
 * @param factionFrom per unit slug, the faction its entry shows from a level on ({@code {34: Jovian Ascendancy}})
 * @param enemies per unit slug (set pieces, allies and neutrals included): Varga's two lines
 * @param bosses per boss slug: Varga's two lines and the phase notes
 * @param sensorSuite the sensor suite's note and Varga's line asking for one
 * @param concepts per concept slug: its page
 */
public record DatabankData(
        Map<String, Map<Integer, String>> factionFrom,
        Map<String, UnitText> enemies,
        Map<String, BossText> bosses,
        Map<String, Note> weapons,
        Map<String, Note> specials,
        Map<String, Note> generators,
        Map<String, Note> shields,
        Map<String, Note> plating,
        Map<String, Note> engines,
        Map<String, Note> utilities,
        Map<String, Note> escort,
        @JsonProperty("sensor-suite") SensorText sensorSuite,
        Map<String, Concept> concepts) {
    /** The most characters of a label line (8×12 over the entry page's 504 px). */
    public static final int LINE_WIDTH = 63;

    public DatabankData {
        factionFrom = sorted(factionFrom);
        factionFrom.forEach((slug, levels) -> {
            Check.notEmpty("faction_from." + slug, List.copyOf(levels.values()));
            levels.forEach((level, faction) -> {
                Check.that(level >= 1 && level <= 50, "faction_from." + slug + ": a level 1-50, was " + level);
                Check.that(!faction.isBlank(), "faction_from." + slug + "." + level + ": a faction");
            });
        });
        enemies = sorted(enemies);
        bosses = sorted(bosses);
        weapons = sorted(weapons);
        specials = sorted(specials);
        generators = sorted(generators);
        shields = sorted(shields);
        plating = sorted(plating);
        engines = sorted(engines);
        utilities = sorted(utilities);
        escort = sorted(escort);
        concepts = sorted(concepts);
    }

    private static <V> Map<String, V> sorted(Map<String, V> map) {
        return java.util.Collections.unmodifiableMap(new TreeMap<>(map));
    }

    /** The gear sections by their key in the file ({@code weapons}, …, {@code escort}), in the file's order. */
    public Map<String, Map<String, Note>> gear() {
        Map<String, Map<String, Note>> gear = new java.util.LinkedHashMap<>();
        gear.put("weapons", weapons);
        gear.put("specials", specials);
        gear.put("generators", generators);
        gear.put("shields", shields);
        gear.put("plating", plating);
        gear.put("engines", engines);
        gear.put("utilities", utilities);
        gear.put("escort", escort);
        return java.util.Collections.unmodifiableMap(gear);
    }

    /** The faction {@code slug}'s entry shows in Level {@code level}: the last {@code faction_from} at or before it. */
    public java.util.Optional<String> factionAt(String slug, int level) {
        Map<Integer, String> from = factionFrom.getOrDefault(slug, Map.of());
        return from.entrySet().stream()
                .filter(entry -> entry.getKey() <= level)
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue);
    }

    /** A unit's lines: {@code vague} (sensor none) and {@code tip} (sensor L3), one to three each. */
    public record UnitText(List<String> vague, List<String> tip) {
        public UnitText {
            vague = checked("vague", vague, 1, 3);
            tip = checked("tip", tip, 1, 3);
        }
    }

    /** A boss's lines: as a unit's, and three to five lines of {@code phases}. */
    public record BossText(List<String> vague, List<String> tip, List<String> phases) {
        public BossText {
            vague = checked("vague", vague, 1, 3);
            tip = checked("tip", tip, 1, 3);
            phases = checked("phases", phases, 3, 5);
        }
    }

    /** A gear item's note, one or two lines. */
    public record Note(List<String> note) {
        public Note {
            note = checked("note", note, 1, 2);
        }
    }

    /** The sensor suite's {@code note} (one or two lines) and Varga's {@code varga} line asking for one (one to three). */
    public record SensorText(List<String> note, List<String> varga) {
        public SensorText {
            note = checked("note", note, 1, 2);
            varga = checked("varga", varga, 1, 3);
        }
    }

    /**
     * A concept page: its {@code title}, the level {@code after} whose flight opens it, what its {@code picture} shows,
     * four to eight {@code lines}.
     */
    public record Concept(String title, int after, String picture, List<String> lines) {
        public Concept {
            Check.that(!title.isBlank(), "title: a title");
            Check.that(after >= 1 && after <= 50, "after: a level 1-50, was " + after);
            Check.that(!picture.isBlank(), "picture: what the page shows");
            lines = checked("lines", lines, 4, 8);
        }
    }

    /** A text's lines: {@code fewest} to {@code most}, none blank, each at most {@link #LINE_WIDTH} characters. */
    static List<String> checked(String field, List<String> lines, int fewest, int most) {
        Check.that(
                lines.size() >= fewest && lines.size() <= most,
                field + ": " + fewest + " to " + most + " lines, found " + lines.size());
        for (String line : lines) {
            Check.that(!line.isBlank(), field + ": a blank line");
            Check.that(
                    line.length() <= LINE_WIDTH,
                    field + ": a line of " + line.length() + " characters, at most " + LINE_WIDTH + ": '" + line + "'");
        }
        return List.copyOf(lines);
    }

    /**
     * A gear item's slug from its name: its nickname in quotes left out, a {@code +} read "plus", in lower case,
     * hyphenated: {@code Mk V+ "Crucible"} is {@code mk-v-plus}, {@code Ascendancy IFF spoofer} {@code
     * ascendancy-iff-spoofer}.
     */
    public static String slug(String name) {
        return name.replaceAll("\"[^\"]*\"", "")
                .replace("+", " plus")
                .trim()
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
