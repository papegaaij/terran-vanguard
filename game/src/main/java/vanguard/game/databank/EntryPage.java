package vanguard.game.databank;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import vanguard.content.campaign.Catalogue;
import vanguard.content.databank.Advice;
import vanguard.content.databank.Entry;
import vanguard.content.databank.Pages;
import vanguard.game.ui.Box;
import vanguard.game.ui.Glass;
import vanguard.game.ui.Speaker;
import vanguard.game.ui.Words;

/**
 * Step DB2 (design/ui/databank, Screen): the entry page on the right, one page, no scrolling: the picture inset (136×136,
 * 196×196 for a boss's or a large unit's 192×192 databank plate), the name beside it in the heading font, the fields
 * below the name (labels in the 8×12 label font, values cyan, numbers white, credits amber; a locked field's {@code - - -
 * -} with its {@code SENSOR Ln} tag, which leads to the sensor suite's entry), then the callout to the suite (sensor
 * none), a boss's phases, the weapon advice with the player's matching gear in green, or a gear item's level table,
 * test fire, {@code STRONG AGAINST} and the suite's ladder, and Dr. Varga's line with her 72×72 radio portrait (a gear
 * item's note in an inset of its own). The page lays itself out on a {@link Canvas}: the screen, or a test's measure of
 * where every part lands.
 */
final class EntryPage {
    /** The entry panel: right of the list. */
    static final int X = 324;

    static final int Y = 82;
    static final int WIDTH = 620;
    static final int HEIGHT = 424;
    static final int INNER = X + 12;
    static final int RIGHT = X + WIDTH - 12;
    /** The page's header line (what the entry is, where it was met). */
    static final int HEADER_Y = Y + 10;
    /** The picture inset's top and its sizes. */
    static final int PICTURE_Y = Y + 28;

    static final int PICTURE = 136;
    /** The picture box of a page with a 192×192 plate (a boss, the Leviathan, the Abyss Ray, the cruiser concept). */
    static final int PLATE_PICTURE = 196;
    /** A line of the label font. */
    static final int LINE = 12;
    /** The label column of the fields. */
    static final int LABEL_WIDTH = 72;
    /** The bottom of the page's last line of text. */
    static final int FOOT = Y + HEIGHT - 10;
    /** Varga's radio portrait and her inset at the page's foot. */
    static final int PORTRAIT = 72;

    static final int VARGA_HEIGHT = PORTRAIT + 6;
    static final int VARGA_Y = FOOT - VARGA_HEIGHT;
    /** The level table's columns and the test-fire box beside it (the hangar's box size). */
    static final int TEST_FIRE_WIDTH = 264;

    static final int TEST_FIRE_HEIGHT = 64;
    /** The sensor suite's entry, where every locked field's tag and the callout's chip lead. */
    static final String SUITE = "utility:sensor-suite";
    /** The callout's chip. */
    static final String SUITE_CHIP = "SENSOR SUITE >";

    /** The speaker of the page's inset (design/ui/databank: text only, the hangar intel's Varga). */
    static final String VARGA = "Varga";

    private static final int CHAR = 8;

    /** Where the page's parts go, each with its top left in screen px from the top left. */
    interface Canvas {
        /** A line in the 8×12 label font. */
        void text(String text, Color colour, int x, int y);

        /** A line in the heading font ({@code body}: the 10×20 body font, for a name too long for it). */
        void name(String text, int x, int y, boolean body);

        /** A rule header: amber label caps with the trim line on to {@code right}. */
        void header(String text, int x, int right, int y);

        void tag(String text, Color colour, int x, int y, int width);

        /** The picture inset, {@code size} px square: the screen draws the entry's picture in it. */
        void picture(int x, int y, int size);

        /** Varga's radio portrait in its frame. */
        void portrait(int x, int y, int size);

        void inset(int x, int y, int width, int height);

        void chip(String text, int x, int y, int width, int height, boolean on);

        /** A lit row of a table (the level shown): the selection's band. */
        void lit(int x, int y, int width, int height);

        /** The test-fire box: the screen loops the weapon in it. */
        void testFire(int x, int y, int width, int height);

        /** A place the mouse follows to entry {@code id}. */
        default void link(Box box, String id) {}
    }

    private EntryPage() {}

    /** The picture box's size for a page: the plate's for a plate. */
    static int pictureSize(boolean plate) {
        return plate ? PLATE_PICTURE : PICTURE;
    }

    /** Where the fields run: beside the picture, then below it at the full width. */
    private static final class Flow {
        int y;
        final int besideX;
        final int pictureBottom;

        Flow(int y, int besideX, int pictureBottom) {
            this.y = y;
            this.besideX = besideX;
            this.pictureBottom = pictureBottom;
        }

        int labelX() {
            return y + LINE <= pictureBottom ? besideX : INNER;
        }

        int valueX() {
            return labelX() + LABEL_WIDTH;
        }

        int chars() {
            return (RIGHT - valueX()) / CHAR;
        }

        /** Below the picture, before a block that runs the full width. */
        void below() {
            y = Math.max(y, pictureBottom);
        }
    }

    /** The lines refitted to {@code chars} when one is longer (the generated lines wrap at 63). */
    static List<String> fit(List<String> lines, int chars) {
        if (lines.stream().allMatch(line -> line.length() <= chars)) {
            return lines;
        }
        List<String> fitted = new ArrayList<>();
        for (String line : lines) {
            fitted.addAll(Words.wrap(line, chars));
        }
        return fitted;
    }

    /** A paragraph's lines rewrapped to {@code chars} when one is longer (a concept's page, a note). */
    static List<String> refit(List<String> lines, int chars) {
        if (lines.stream().allMatch(line -> line.length() <= chars)) {
            return lines;
        }
        return Words.wrap(String.join(" ", lines), chars);
    }

    private static String upper(String text) {
        return text.toUpperCase(Locale.ROOT);
    }

    private static int tagWidth(String text) {
        return text.length() * CHAR + 6;
    }

    /** The top of the page's name, the picture and the first field. */
    private static int nameX(int size) {
        return INNER + size + 12;
    }

    private static void name(Canvas canvas, String name, int size) {
        int x = nameX(size);
        boolean fits = name.length() * 20 <= RIGHT - x;
        canvas.name(name, x, PICTURE_Y + (fits ? 0 : 4), !fits);
    }

    // ---- Enemy and boss entries --------------------------------------------------------------------------------

    /** What a unit's page header calls it: {@code ENEMY}, {@code BOSS}, {@code ALLY}, {@code NEUTRAL}. */
    static String kindWord(Pages.EnemyPage page) {
        String tag = page.tag().orElse("");
        if (tag.equals("ALLY") || tag.equals("NEUTRAL")) {
            return tag;
        }
        return page.entry().tab() == Entry.Tab.BOSSES ? "BOSS" : "ENEMY";
    }

    /** A tag's colour: an ally or a neutral craft green, a new entry amber, the others cyan. */
    static Color tagColour(String tag) {
        return switch (tag) {
            case "ALLY", "NEUTRAL" -> Glass.GREEN;
            case "NEW" -> Glass.AMBER;
            default -> Glass.CYAN;
        };
    }

    /**
     * A known unit's or boss's page at its sensor level; returns the bottom of its content above Varga's inset.
     */
    static int enemy(Canvas canvas, Pages.EnemyPage page) {
        int size = pictureSize(page.picture().plate());
        String header = String.format(
                Locale.ROOT, "%s - FIRST MET L%02d %s", kindWord(page), page.firstMet(), page.firstMetName());
        int headerRight = RIGHT;
        if (page.tag().isPresent()) {
            String tag = page.tag().get();
            int width = tagWidth(tag);
            canvas.tag(tag, tagColour(tag), RIGHT - width, HEADER_Y - 2, width);
            headerRight = RIGHT - width - 6;
        }
        canvas.header(header, INNER, headerRight, HEADER_Y);
        canvas.picture(INNER, PICTURE_Y, size);
        name(canvas, upper(page.entry().name()), size);
        Flow flow = new Flow(PICTURE_Y + 30, nameX(size), PICTURE_Y + size + 6);
        boolean boss = page.entry().kind() == Entry.Kind.BOSS;
        for (Pages.Field field : page.fields()) {
            field(canvas, flow, boss && field.label().equals("ATTACKS") ? attackNames(field) : field, page.sensor());
        }
        if (page.suiteCallout()) {
            flow.below();
            flow.y += 4;
            callout(canvas, flow.y);
            flow.y += CALLOUT_HEIGHT + 6;
        }
        if (!page.phases().isEmpty()) {
            flow.below();
            flow.y += SECTION_GAP;
            canvas.header("PHASES", INNER, RIGHT, flow.y);
            flow.y += LINE + SECTION_GAP;
            for (String line : fit(page.phases().stream().map(EntryPage::upper).toList(), (RIGHT - INNER) / CHAR)) {
                canvas.text(line, Glass.BODY, INNER, flow.y);
                flow.y += LINE;
            }
        }
        if (!page.advice().isEmpty()) {
            flow.below();
            flow.y += SECTION_GAP;
            canvas.header("WEAPON ADVICE", INNER, RIGHT, flow.y);
            flow.y += LINE + SECTION_GAP;
            List<Advice.Row> plain = new ArrayList<>();
            List<Advice.Row> group = new ArrayList<>();
            for (Advice.Row row : page.advice()) {
                if (plain(row)) {
                    plain.add(row);
                    continue;
                }
                if (!group.isEmpty() && !sameReason(group.getFirst(), row)) {
                    flow.y = advice(canvas, group, flow.y);
                    group.clear();
                }
                group.add(row);
            }
            if (!group.isEmpty()) {
                flow.y = advice(canvas, group, flow.y);
            }
            if (!plain.isEmpty()) {
                flow.y = effective(canvas, plain, flow.y);
            }
        }
        flow.below();
        if (boss) {
            compactVarga(canvas, page.varga());
        } else {
            varga(canvas, page.varga());
        }
        return flow.y;
    }

    /** The callout that tells what a suite would add (sensor none), with its chip to the suite's entry. */
    static final int CALLOUT_HEIGHT = 40;

    static final List<String> CALLOUT = List.of(
            "A SENSOR SUITE SHOWS MORE: CLASS, LAYER AND KILLS (L1), THE",
            "PICTURE, NUMBERS AND ATTACKS (L2), WEAK POINTS AND ADVICE (L3).");

    private static void callout(Canvas canvas, int y) {
        canvas.inset(INNER, y, RIGHT - INNER, CALLOUT_HEIGHT);
        int chipWidth = tagWidth(SUITE_CHIP) + 10;
        int textY = y + 7;
        for (String line : refit(CALLOUT, (RIGHT - INNER - chipWidth - 20) / CHAR)) {
            canvas.text(line, Glass.BODY, INNER + 8, textY);
            textY += LINE;
        }
        Box chip = new Box(RIGHT - chipWidth - 8, y + 9, chipWidth, 22);
        canvas.chip(SUITE_CHIP, (int) chip.x(), (int) chip.y(), (int) chip.width(), (int) chip.height(), true);
        canvas.link(chip, SUITE);
    }

    /** A field: its label, its value lines (or, locked, {@code - - - -} and the tag naming the level that shows it). */
    private static void field(Canvas canvas, Flow flow, Pages.Field field, int sensor) {
        int labelX = flow.labelX();
        int valueX = flow.valueX();
        canvas.text(field.label(), Glass.LABEL, labelX, flow.y);
        if (field.locked(sensor)) {
            canvas.text(Pages.LOCKED, Glass.DIM, valueX, flow.y);
            String tag = field.tag();
            int width = tagWidth(tag);
            int tagX = valueX + Pages.LOCKED.length() * CHAR + 10;
            canvas.tag(tag, Glass.CYAN, tagX, flow.y - 2, width);
            canvas.link(new Box(tagX, flow.y - 2, width, LINE), SUITE);
            flow.y += LINE;
            return;
        }
        Color colour = valueColour(field.label());
        for (String line : fit(field.value(), flow.chars())) {
            canvas.text(line, colour, valueX, flow.y);
            flow.y += LINE;
        }
    }

    /** A field's value colour: numbers white, credits amber, the rest cyan (design/ui/databank, Screen). */
    static Color valueColour(String label) {
        return switch (label) {
            case "HP", "KILLS", "SHIELD", "MIRROR" -> Glass.WHITE;
            case "BOUNTY" -> Glass.AMBER;
            default -> Glass.CYAN;
        };
    }

    /**
     * An advice row: its label as a tag (red for a weapon that hurts the player), its reason, and the player's matching
     * gear in green on the right (the fitted first), on the next line when it does not fit beside the reason.
     */
    private static int advice(Canvas canvas, List<Advice.Row> rows, int y) {
        Advice.Row row = rows.getFirst();
        int reasonX = INNER;
        for (Advice.Row each : rows) {
            String label = upper(each.label());
            int width = tagWidth(label);
            canvas.tag(label, row.red() ? Glass.ALERT : Glass.CYAN, reasonX, y - 2, width);
            reasonX += width + 4;
        }
        reasonX += 2;
        String gear = gear(gearOf(rows));
        List<String> reason = Words.wrap(upper(row.reason()), (RIGHT - reasonX) / CHAR);
        int line = y;
        for (String part : reason) {
            canvas.text(part, row.red() ? Glass.ALERT : Glass.BODY, reasonX, line);
            line += LINE;
        }
        if (!gear.isEmpty()) {
            int gearWidth = gear.length() * CHAR;
            int last = reason.isEmpty() ? 0 : reason.getLast().length() * CHAR;
            int gearY = reasonX + last + 2 * CHAR + gearWidth <= RIGHT ? line - LINE : line;
            canvas.text(gear, Glass.GREEN, RIGHT - gearWidth, gearY);
            line = Math.max(line, gearY + LINE);
        }
        return line + 2;
    }

    /** Whether two advice rows say the same (their tags share one row: {@code HOMING} {@code BEAM} on a high-air unit). */
    static boolean sameReason(Advice.Row first, Advice.Row next) {
        return first.red() == next.red() && first.reason().equals(next.reason());
    }

    /** The player's gear the rows name, each once, the fitted first. */
    private static List<Advice.GearRef> gearOf(List<Advice.Row> rows) {
        List<Advice.GearRef> refs = new ArrayList<>();
        rows.forEach(row -> row.gear().stream()
                .filter(ref -> refs.stream().noneMatch(seen -> seen.id().equals(ref.id())))
                .forEach(refs::add));
        refs.sort(java.util.Comparator.comparing((Advice.GearRef ref) -> !ref.fitted()));
        return refs;
    }

    /** The space before a section's header and after it. */
    static final int SECTION_GAP = 2;

    /** An effective trait's row with no rule of its own: its label and {@code EFFECTIVE} (they share one line). */
    static boolean plain(Advice.Row row) {
        return !row.red() && row.reason().equalsIgnoreCase(EFFECTIVE);
    }

    static final String EFFECTIVE = "EFFECTIVE";

    /**
     * The effective traits with no rule of their own on one line, {@code EFFECTIVE} then their tags (wrapping), the
     * player's gear any of them names in green on the right (decided as trivia, pending the user's review: a row each
     * left a boss's page no room).
     */
    private static int effective(Canvas canvas, List<Advice.Row> rows, int y) {
        String gear = gear(gearOf(rows));
        int gearX = gear.isEmpty() ? RIGHT : RIGHT - gear.length() * CHAR;
        canvas.text(EFFECTIVE, Glass.BODY, INNER, y);
        int x = INNER + (EFFECTIVE.length() + 1) * CHAR;
        int line = y;
        for (Advice.Row row : rows) {
            String label = upper(row.label());
            int width = tagWidth(label);
            int limit = line == y ? gearX - 2 * CHAR : RIGHT;
            if (x + width > limit) {
                line += LINE + 2;
                x = INNER + (EFFECTIVE.length() + 1) * CHAR;
            }
            canvas.tag(label, Glass.CYAN, x, line - 2, width);
            x += width + 4;
        }
        if (!gear.isEmpty()) {
            canvas.text(gear, Glass.GREEN, gearX, y);
        }
        return line + LINE + 2;
    }

    /**
     * A boss's {@code ATTACKS} by name alone (the phases tell their patterns; decided as trivia, pending the user's
     * review: a line each ran a boss's page past its foot): each attack line's name (before its {@code -}, else its
     * pattern), a numbered series once ({@code CORE-FAN-1}, {@code -2} as {@code CORE-FAN}).
     */
    static Pages.Field attackNames(Pages.Field field) {
        List<String> names = new ArrayList<>();
        for (String line : field.value()) {
            String first = line.contains(" - ") ? line.substring(0, line.indexOf(" - ")) : line.split(" ")[0];
            if (first.isEmpty() || !Character.isLetter(first.charAt(0)) || first.equals("EVERY")) {
                continue;
            }
            String name = first.replaceAll("-\\d+$", "");
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        return new Pages.Field(field.label(), List.of(String.join(", ", names)), field.needs());
    }

    /** The player's gear a row names: the first two (the fitted first), with their levels. */
    static String gear(List<Advice.GearRef> refs) {
        List<String> names = new ArrayList<>();
        for (Advice.GearRef ref : refs.subList(0, Math.min(2, refs.size()))) {
            names.add(upper(ref.name()) + " L" + ref.level());
        }
        return String.join(", ", names);
    }

    /** Dr. Varga's inset at the page's foot: her radio portrait, her name plate, her line in quotes. */
    private static void varga(Canvas canvas, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        canvas.inset(INNER, VARGA_Y, RIGHT - INNER, VARGA_HEIGHT);
        canvas.portrait(INNER + 3, VARGA_Y + 3, PORTRAIT);
        int textX = INNER + PORTRAIT + 12;
        canvas.text(Speaker.plate(VARGA), Glass.AMBER, textX, VARGA_Y + 6);
        int y = VARGA_Y + 22;
        for (String line : Words.wrap(upper("\"" + String.join(" ", lines) + "\""), (RIGHT - textX - 4) / CHAR)) {
            canvas.text(line, Glass.CYAN, textX, y);
            y += LINE;
        }
    }

    /** A boss page's Varga inset: her portrait at half size beside her line (decided as trivia, pending review). */
    static final int SMALL_PORTRAIT = 36;

    /** The boss page's inset at the foot: half-size portrait, name plate and line beside it; returns its top. */
    private static void compactVarga(Canvas canvas, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        int textX = INNER + SMALL_PORTRAIT + 12;
        List<String> quote = Words.wrap(upper("\"" + String.join(" ", lines) + "\""), (RIGHT - textX - 4) / CHAR);
        int height = Math.max(SMALL_PORTRAIT + 6, 20 + LINE * quote.size());
        int top = FOOT - height;
        canvas.inset(INNER, top, RIGHT - INNER, height);
        canvas.portrait(INNER + 3, top + 3, SMALL_PORTRAIT);
        canvas.text(Speaker.plate(VARGA), Glass.AMBER, textX, top + 4);
        int y = top + 18;
        for (String line : quote) {
            canvas.text(line, Glass.CYAN, textX, y);
            y += LINE;
        }
    }

    /** The top of a boss page's Varga inset. */
    static int compactVargaTop(List<String> lines) {
        int textX = INNER + SMALL_PORTRAIT + 12;
        List<String> quote = Words.wrap(upper("\"" + String.join(" ", lines) + "\""), (RIGHT - textX - 4) / CHAR);
        return FOOT - Math.max(SMALL_PORTRAIT + 6, 20 + LINE * quote.size());
    }

    /** The top of the content's last allowed line's bottom: above Varga's inset or a note's. */
    static int contentFoot() {
        return VARGA_Y - 4;
    }

    // ---- Gear entries -----------------------------------------------------------------------------------------

    /** What a gear page's header calls it: {@code WEAPON - FRONT}, {@code SPECIAL}, {@code SHIP - CORE}. */
    static String kindWord(Entry entry) {
        return switch (entry.kind()) {
            case WEAPON -> "WEAPON - " + entry.group();
            case SPECIAL -> "SPECIAL";
            case GENERATOR, SHIELD, PLATING, ENGINE ->
                "SHIP - " + entry.group() + " - " + entry.kind().name();
            default -> "SHIP - " + entry.group();
        };
    }

    /**
     * A ship part's list name with its kind ahead ({@code GEN MK I "SPARK"}, {@code SHIELD MK I}, {@code ENGINE MK I}):
     * the core parts share their names across kinds (decided as trivia, pending the user's review).
     */
    static String listName(Entry entry) {
        String name = upper(entry.name());
        return switch (entry.kind()) {
            case GENERATOR -> "GEN " + name;
            case SHIELD -> "SHIELD " + name;
            case PLATING -> "PLATING " + name;
            case ENGINE -> "ENGINE " + name;
            default -> name;
        };
    }

    /**
     * A gear item's page, in full; {@code shown} the level the table lights and the test fire loops (LEFT / RIGHT
     * step it), {@code testFire} whether the screen loops a weapon in the test-fire box. Returns the content's bottom.
     */
    static int gear(Canvas canvas, Pages.GearPage page, int shown, boolean testFire) {
        int size = PICTURE;
        canvas.header(kindWord(page.entry()), INNER, RIGHT, HEADER_Y);
        canvas.picture(INNER, PICTURE_Y, size);
        name(canvas, upper(page.entry().name()), size);
        Flow flow = new Flow(PICTURE_Y + 30, nameX(size), PICTURE_Y + size + 6);
        boolean mine = page.level() > 0 || page.fitted();
        line(canvas, flow, "YOURS", List.of(upper(page.yours())), mine ? Glass.GREEN : Glass.AMBER);
        if (page.items().size() == 1) {
            Catalogue.Item item = page.items().getFirst();
            if (!item.traits().isEmpty()) {
                line(canvas, flow, "TRAITS", List.of(upper(String.join(", ", item.traits()))), Glass.CYAN);
            }
        }
        if (!page.hits().isEmpty()) {
            line(canvas, flow, "HITS", List.of(String.join(" ", page.hits())), Glass.CYAN);
        }
        if (!page.not().isEmpty()) {
            line(canvas, flow, "NOT", List.of(String.join(" ", page.not())), Glass.DIM);
        }
        if (testFire || page.items().size() > 1) {
            flow.below();
        }
        if (!page.items().isEmpty()) {
            flow.y += 4;
            flow.y = levels(canvas, page, shown, testFire, flow.y, flow.labelX());
        }
        flow.below();
        if (!page.strongAgainst().isEmpty()) {
            flow.y += 4;
            canvas.header("STRONG AGAINST", INNER, RIGHT, flow.y);
            flow.y += LINE + 4;
            for (Pages.Strong strong : page.strongAgainst()) {
                String name = upper(strong.name());
                canvas.text(name, Glass.WHITE, INNER, flow.y);
                int tagX = INNER + 22 * CHAR;
                String label = upper(strong.label());
                int width = tagWidth(label);
                canvas.tag(label, Glass.CYAN, tagX, flow.y - 2, width);
                int reasonX = tagX + width + 6;
                List<String> reason = Words.wrap(upper(strong.reason()), (RIGHT - reasonX) / CHAR);
                canvas.link(
                        new Box(INNER, flow.y - 2, RIGHT - INNER, LINE * Math.max(1, reason.size())), strong.unit());
                for (String part : reason) {
                    canvas.text(part, Glass.BODY, reasonX, flow.y);
                    flow.y += LINE;
                }
            }
        }
        if (!page.ladder().isEmpty()) {
            flow.y += 4;
            flow.y = ladder(canvas, page, flow.y);
        }
        List<String> note = page.note().stream().map(EntryPage::upper).toList();
        if (!page.varga().isEmpty()) {
            if (!note.isEmpty()) {
                flow.y += 4;
                for (String line : refit(note, (RIGHT - INNER) / CHAR)) {
                    canvas.text(line, Glass.BODY, INNER, flow.y);
                    flow.y += LINE;
                }
            }
            varga(canvas, page.varga());
        } else if (!note.isEmpty()) {
            notes(canvas, note);
        }
        return flow.y;
    }

    private static void line(Canvas canvas, Flow flow, String label, List<String> value, Color colour) {
        int labelX = flow.labelX();
        int valueX = flow.valueX();
        canvas.text(label, Glass.LABEL, labelX, flow.y);
        for (String part : fit(value, flow.chars())) {
            canvas.text(part, colour, valueX, flow.y);
            flow.y += LINE;
        }
    }

    /** A gear item's note in an inset at the page's foot. */
    private static void notes(Canvas canvas, List<String> note) {
        List<String> lines = refit(note, (RIGHT - INNER - 16) / CHAR);
        int height = 22 + LINE * lines.size();
        int y = FOOT - height;
        canvas.inset(INNER, y, RIGHT - INNER, height);
        canvas.text("NOTE", Glass.AMBER, INNER + 8, y + 6);
        int line = y + 20;
        for (String part : lines) {
            canvas.text(part, Glass.BODY, INNER + 8, line);
            line += LINE;
        }
    }

    /** The table's columns: the level, its numbers, the draw and the cost. */
    static final int LEVEL_COLUMN = 48;

    static final int NUMBER_COLUMN = 72;

    /**
     * The level table: L1 to the item's last level with its numbers (DPS by the shop's single-target rule, output,
     * regeneration, armour, speed, charges), its draw and its cost; the shown level lit, the player's marked {@code
     * YOURS}; Rook's guns one row each with their DPS by level. The test-fire box beside it for a weapon.
     */
    private static int levels(Canvas canvas, Pages.GearPage page, int shown, boolean testFire, int y, int left) {
        int right = testFire ? RIGHT - TEST_FIRE_WIDTH - 12 : RIGHT;
        canvas.header("LEVELS", left, right, y);
        if (testFire) {
            canvas.testFire(RIGHT - TEST_FIRE_WIDTH, y, TEST_FIRE_WIDTH, TEST_FIRE_HEIGHT);
        }
        int top = y;
        y += LINE + 4;
        if (page.items().size() > 1) {
            // Rook's guns: one row each, their DPS from L1.
            int levels = page.items().stream()
                    .mapToInt(Catalogue.Item::maxLevel)
                    .max()
                    .orElse(1);
            canvas.text("GUN", Glass.LABEL, INNER, y);
            for (int level = 1; level <= levels; level++) {
                canvas.text("L" + level, Glass.LABEL, INNER + 160 + (level - 1) * 56, y);
            }
            y += LINE;
            for (Catalogue.Item item : page.items()) {
                canvas.text(upper(item.name()), Glass.CYAN, INNER, y);
                for (int level = 1; level <= item.maxLevel(); level++) {
                    Double dps = item.stats(level).get(Catalogue.Stat.DPS);
                    canvas.text(dps == null ? "-" : number(dps), Glass.WHITE, INNER + 160 + (level - 1) * 56, y);
                }
                y += LINE;
            }
            return Math.max(y, testFire ? top + TEST_FIRE_HEIGHT + 4 : y);
        }
        Catalogue.Item item = page.items().getFirst();
        List<Catalogue.Stat> stats = new ArrayList<>(item.stats(1).keySet());
        stats.sort(null);
        int x = left;
        canvas.text("LEVEL", Glass.LABEL, x, y);
        x += LEVEL_COLUMN;
        for (Catalogue.Stat stat : stats) {
            canvas.text(stat.name(), Glass.LABEL, x, y);
            x += NUMBER_COLUMN;
        }
        canvas.text("DRAW", Glass.LABEL, x, y);
        canvas.text("COST", Glass.LABEL, x + NUMBER_COLUMN, y);
        y += LINE + 2;
        for (int level = 1; level <= item.maxLevel(); level++) {
            boolean lit = level == shown;
            if (lit) {
                canvas.lit(left - 4, y - 3, right - left + 4, LINE + 2);
            }
            Color colour = lit ? Glass.AMBER : Glass.WHITE;
            x = left;
            canvas.text("L" + level, lit ? Glass.AMBER : Glass.LABEL, x, y);
            x += LEVEL_COLUMN;
            Map<Catalogue.Stat, Double> numbers = item.stats(level);
            for (Catalogue.Stat stat : stats) {
                Double value = numbers.get(stat);
                canvas.text(value == null ? "-" : number(value), colour, x, y);
                x += NUMBER_COLUMN;
            }
            canvas.text(number(item.draw(level)) + " MW", colour, x, y);
            int cost = level == 1 ? item.price() : item.upgradeCost(level - 1);
            canvas.text(
                    (level == 1 ? "" : "+") + thousands(cost) + " CR",
                    lit ? Glass.AMBER : Glass.VALUE,
                    x + NUMBER_COLUMN,
                    y);
            if (level == page.level()) {
                canvas.text("YOURS", Glass.GREEN, right - 5 * CHAR, y);
            }
            y += LINE + 2;
        }
        return Math.max(y, testFire ? top + TEST_FIRE_HEIGHT + 4 : y);
    }

    /** The sensor suite's ladder: what each level shows in the hangar intel and here, the suite's level lit. */
    private static int ladder(Canvas canvas, Pages.GearPage page, int y) {
        canvas.header("WHAT EACH LEVEL SHOWS", INNER, RIGHT, y);
        y += LINE + 4;
        int intelX = INNER + 7 * CHAR;
        int hereX = INNER + 36 * CHAR;
        canvas.text("LEVEL", Glass.LABEL, INNER, y);
        canvas.text("HANGAR INTEL", Glass.LABEL, intelX, y);
        canvas.text("DATABANK", Glass.LABEL, hereX, y);
        y += LINE + 2;
        for (Pages.Rung rung : page.ladder()) {
            List<String> intel = Words.wrap(String.join(", ", rung.intel()), (hereX - intelX) / CHAR - 2);
            List<String> here = Words.wrap(String.join(", ", rung.databank()), (RIGHT - hereX) / CHAR);
            int lines = Math.max(1, Math.max(intel.size(), here.size()));
            boolean lit = rung.level() == page.level();
            if (lit) {
                canvas.lit(INNER - 4, y - 3, RIGHT - INNER + 4, LINE * lines + 2);
            }
            canvas.text(rung.level() == 0 ? "NONE" : "L" + rung.level(), lit ? Glass.AMBER : Glass.WHITE, INNER, y);
            int line = y;
            for (String part : intel.isEmpty() ? List.of("-") : intel) {
                canvas.text(part, Glass.CYAN, intelX, line);
                line += LINE;
            }
            line = y;
            for (String part : here) {
                canvas.text(part, Glass.CYAN, hereX, line);
                line += LINE;
            }
            y += LINE * lines + 2;
        }
        return y;
    }

    /** A number as the shop shows it: whole, or with one decimal. */
    static String number(double value) {
        double rounded = Math.round(value * 10) / 10.0;
        return rounded == Math.rint(rounded)
                ? Long.toString(Math.round(rounded))
                : String.format(Locale.ROOT, "%.1f", rounded);
    }

    private static String thousands(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ');
    }

    // ---- Concept entries --------------------------------------------------------------------------------------

    /** A concept's page: its title beside the picture, its lines beside and below it. */
    static int concept(Canvas canvas, Pages.ConceptPage page, boolean plate) {
        int size = pictureSize(plate);
        canvas.header(
                String.format(
                        Locale.ROOT,
                        "CONCEPT - %s - AFTER L%02d",
                        page.entry().group(),
                        page.entry().level()),
                INNER,
                RIGHT,
                HEADER_Y);
        canvas.picture(INNER, PICTURE_Y, size);
        name(canvas, upper(page.title()), size);
        Flow flow = new Flow(PICTURE_Y + 30, nameX(size), PICTURE_Y + size + 6);
        List<String> lines = page.lines().stream().map(EntryPage::upper).toList();
        // The lines beside the picture refit to its column; the rest run the full width.
        List<String> beside = refit(lines, (RIGHT - flow.besideX) / CHAR);
        int besideLines = Math.max(0, (flow.pictureBottom - flow.y) / LINE);
        if (beside.size() <= besideLines) {
            for (String line : beside) {
                canvas.text(line, Glass.BODY, flow.besideX, flow.y);
                flow.y += LINE;
            }
        } else {
            flow.below();
            for (String line : refit(lines, (RIGHT - INNER) / CHAR)) {
                canvas.text(line, Glass.BODY, INNER, flow.y);
                flow.y += LINE;
            }
        }
        return flow.y;
    }
}
