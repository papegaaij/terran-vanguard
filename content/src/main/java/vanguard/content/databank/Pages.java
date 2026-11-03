package vanguard.content.databank;

import java.util.List;
import java.util.Optional;
import vanguard.content.campaign.Catalogue;

/** The databank's page models (step DB1, design/ui/databank): what the screen draws for an entry, nothing drawn. */
public final class Pages {
    private Pages() {}

    /** The fields' placeholder while locked. */
    public static final String LOCKED = "- - - -";

    /**
     * A field of an enemy or boss entry: its {@code label} ({@code CLASS}, {@code HP}, …), its value as label lines,
     * and the sensor level it {@code needs}.
     */
    public record Field(String label, List<String> value, int needs) {
        public Field {
            value = List.copyOf(value);
        }

        /** Whether a suite of level {@code sensor} leaves it locked: it reads {@link #LOCKED} and {@link #tag}. */
        public boolean locked(int sensor) {
            return sensor < needs;
        }

        /** The tag a locked field shows, naming the level that would show it: {@code SENSOR L2}. */
        public String tag() {
            return "SENSOR L" + needs;
        }

        /** What it reads at {@code sensor}. */
        public List<String> shown(int sensor) {
            return locked(sensor) ? List.of(LOCKED) : value;
        }
    }

    /**
     * The picture inset: the {@code silhouette} (below sensor L2) or the unit's look, a still databank {@code plate} (a
     * boss, a unit over 128 px; its silhouette from the plate too) or the animated sprite at {@code scale} (2 for a unit
     * under 40 px).
     */
    public record Picture(boolean silhouette, boolean plate, int scale) {}

    /**
     * An enemy's or a boss's page at the campaign's sensor level.
     *
     * @param sensor the sensor level it is read at (0 none – 3)
     * @param firstMet the first level the campaign met it in, {@code firstMetName} that level's name
     * @param lastMet the level its numbers are of: the last level flown that holds it (its first if none)
     * @param fields every field in the screen's order, each with the level it needs ({@link Field#shown})
     * @param varga Varga's line: the vague one below L3, the precise tip at L3
     * @param advice the advice rows (L3; a neutral craft's {@code DO NOT FIRE} at every level), empty for an ally
     * @param phases a boss's phase notes at L3, empty otherwise
     * @param numbers its numbers at {@code lastMet} (HP, shield or mirror, bounty), if that level flies it
     * @param suiteCallout whether the page shows the callout that explains what a suite would add (sensor none)
     */
    public record EnemyPage(
            Entry entry,
            int sensor,
            int firstMet,
            String firstMetName,
            int lastMet,
            int kills,
            Optional<String> tag,
            Picture picture,
            List<Field> fields,
            List<String> varga,
            List<Advice.Row> advice,
            List<String> phases,
            Optional<UnitNumbers> numbers,
            boolean suiteCallout) {
        public EnemyPage {
            fields = List.copyOf(fields);
            varga = List.copyOf(varga);
            advice = List.copyOf(advice);
            phases = List.copyOf(phases);
        }

        /** The field {@code label}, if the page has it. */
        public Optional<Field> field(String label) {
            return fields.stream().filter(field -> field.label().equals(label)).findFirst();
        }
    }

    /**
     * A unit a weapon or special is strong against ({@code STRONG AGAINST}): a met unit's id and name and the advice
     * row that names the item.
     */
    public record Strong(String unit, String name, String label, String reason) {}

    /**
     * A gear entry's page, always in full.
     *
     * @param note the authored note
     * @param yours the campaign's status: {@code L2  FITTED ON THE LEFT WING}, {@code OWNED L3}, {@code NOT OWNED - 1 200
     *     CR}; a special's charges, Rook's side
     * @param level the owned level (0: not owned)
     * @param fitted whether it is fitted (Rook: flying in the escort slot)
     * @param items the catalogue items its tables come from (an item's own; Rook's four guns), for the level table
     * @param hits a weapon's layers it reaches ({@code AIR}, {@code GROUND X2}, {@code HARDENED}), empty for others
     * @param not a weapon's layers it cannot reach
     * @param strongAgainst up to four met units the advice rules name it for
     * @param varga the sensor suite's Varga line asking for one (while the campaign owns none), else empty
     * @param ladder the sensor suite's table of what each level shows, empty for other gear
     */
    public record GearPage(
            Entry entry,
            List<String> note,
            String yours,
            int level,
            boolean fitted,
            List<Catalogue.Item> items,
            List<String> hits,
            List<String> not,
            List<Strong> strongAgainst,
            List<String> varga,
            List<Rung> ladder) {
        public GearPage {
            note = List.copyOf(note);
            items = List.copyOf(items);
            hits = List.copyOf(hits);
            not = List.copyOf(not);
            strongAgainst = List.copyOf(strongAgainst);
            varga = List.copyOf(varga);
            ladder = List.copyOf(ladder);
        }
    }

    /**
     * A row of the sensor suite's table: what level {@code level} (0 none – 3) shows in the hangar intel and in the
     * databank.
     */
    public record Rung(int level, List<String> intel, List<String> databank) {
        public Rung {
            intel = List.copyOf(intel);
            databank = List.copyOf(databank);
        }
    }

    /**
     * A concept's page: its title, what its picture shows, its lines, and its {@code rows} under them, each a line of
     * its own (the weapon traits' meanings, {@link vanguard.content.campaign.TraitWords}; 2026-11-03).
     */
    public record ConceptPage(Entry entry, String title, String picture, List<String> lines, List<String> rows) {
        public ConceptPage {
            lines = List.copyOf(lines);
            rows = List.copyOf(rows);
        }
    }

    /**
     * A row of a tab's list: a known entry (with {@code NEW} while unread and the right column: an enemy's first-met
     * level {@code L37}, a gear item's {@code FITTED}, {@code OWNED} or {@code SHOP}), or an unknown one ({@code ???},
     * dimmed, its act on the right, skipped by the cursor), under its {@code group}'s rule header.
     */
    public record Row(Optional<Entry> entry, String group, boolean fresh, String right) {
        public boolean known() {
            return entry.isPresent();
        }
    }

    /** A tab's list: its rows in order, the known and the unknown counted ({@code 63 KNOWN - 3 UNKNOWN}). */
    public record TabList(Entry.Tab tab, List<Row> rows, int known, int unknown) {
        public TabList {
            rows = List.copyOf(rows);
        }
    }
}
