package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.BriefingPage;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.content.campaign.Intel;
import vanguard.game.ui.Glass;
import vanguard.game.ui.Speaker;

/**
 * The intel panel fits every level at every sensor level: no two of its parts overlap, nothing
 * reaches past the panel's edges, and every field ends above Varga's quote, whose last line stays
 * inside the panel. Text is measured in the kit's monospaced fonts by its ink below the capitals'
 * top, with the one-pixel shadow: 8 px wide and 10 high in the 8×12 label font (capitals 7, a comma
 * reaching 8), 10 by 14 in the 10×20 body font (capitals 11, a Q or comma 13).
 */
class IntelPanelLayoutTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    /** Below the panel's header line. */
    private static final int TOP = IntelPanel.Y + 20;

    private static final int BOTTOM = IntelPanel.Y + IntelPanel.HEIGHT;
    private static final int RIGHT = IntelPanel.X + IntelPanel.WIDTH;
    private static final int LABEL_INK = 10;
    private static final int BODY_INK = 14;

    /** A part's box: px from the top left, {@code right} and {@code bottom} exclusive. */
    private record Box(String what, int left, int top, int right, int bottom) {
        boolean overlaps(Box other) {
            return left < other.right && other.left < right && top < other.bottom && other.top < bottom;
        }
    }

    /** A flat fill in the wave strip. */
    private record Fill(Color colour, float x, int y, float width, int height) {}

    /** Records where every part lands. */
    private static final class Measure implements IntelPanel.Canvas {
        final List<Box> parts = new ArrayList<>();
        final List<Box> quote = new ArrayList<>();

        @Override
        public void text(String text, Color colour, int x, int y, boolean body) {
            int cell = body ? 10 : 8;
            int height = body ? BODY_INK : LABEL_INK;
            parts.add(new Box("text \"" + text + "\"", x, y, x + cell * text.length(), y + height));
        }

        @Override
        public void portrait(BriefingPage teaser, int x, int y, int size) {
            parts.add(new Box("portrait", x - 3, y - 3, x + size + 3, y + size + 3));
        }

        @Override
        public void picture(String name, int x, int y, int size) {
            parts.add(new Box(name, x - 1, y - 1, x + size + 1, y + size + 1));
        }

        @Override
        public void chip(String text, int x, int y, int width, int height) {
            parts.add(new Box("chip " + text, x, y, x + width, y + height));
        }

        @Override
        public void bar(Color colour, int x, int y, int width, int height) {
            parts.add(new Box("bar", x, y, x + width, y + height));
        }

        @Override
        public void inset(int x, int y, int width, int height) {
            parts.add(new Box("wave strip", x, y, x + width, y + height));
        }

        /** The wave strip's fills: its ticks and marks, inside the strip. */
        final List<Fill> fills = new ArrayList<>();

        @Override
        public void fill(Color colour, float x, int y, float width, int height) {
            fills.add(new Fill(colour, x, y, width, height));
        }

        @Override
        public void quote(String text, int x, int y) {
            quote.add(new Box("quote \"" + text + "\"", x, y, x + 8 * text.length(), y + LABEL_INK));
        }

        /** M7 part H: the wave strip's pictures (the boss's silhouette at its arrival), over the strip. */
        final List<StripMark> marks = new ArrayList<>();

        @Override
        public void mark(String name, float x, int y, int size) {
            marks.add(new StripMark(name, x, y, size));
        }
    }

    /** M7 part H: a picture on the wave strip, its top left and size. */
    private record StripMark(String name, float x, int y, int size) {}

    @Test
    void everyLevelsIntelFitsThePanelAtEverySensorLevel() {
        for (String level : CONTENT.levels().keySet()) {
            BriefingPage teaser = CONTENT.level(level).briefing().teaser();
            for (int sensor = 0; sensor <= 3; sensor++) {
                assertFits(Intel.of(CONTENT, level, sensor), teaser, level, level + " at sensor L" + sensor + ": ");
            }
        }
    }

    /**
     * Varga has a line of her own for each sensor level of every level (design/ui/hangar, Intel; user
     * decision D4 of M4 part H): four different lines, each the one the panel quotes at that level.
     * That each fits the panel beside the fields it shows is checked above.
     */
    @Test
    void everyLevelHasVargasLineForEachSensorLevel() {
        for (String level : CONTENT.levels().keySet()) {
            Set<String> lines = new HashSet<>();
            for (int sensor = 0; sensor <= 3; sensor++) {
                Intel intel = Intel.of(CONTENT, level, sensor);
                String line = intel.varga();
                assertFalse(line == null || line.isBlank(), level + " has Varga's line for sensor L" + sensor);
                assertTrue(lines.add(line), level + " at sensor L" + sensor + " repeats a line: " + line);
                assertEquals(
                        String.join(" ", IntelPanel.quote(intel)),
                        Names.of("\"" + line + "\""),
                        level + " at sensor L" + sensor + ": the quote is the whole line, no word cut");
            }
        }
    }

    /**
     * An escort level's OBJECTIVE row (Level 04, from sensor L1) fits beside the other fields of every
     * level like it: one without set-piece contacts, whose row the escort levels do not have (Level 03
     * with both would run 2 px into Varga's quote). Level 04's own intel is checked above.
     */
    @Test
    void everyLevelsIntelFitsWithAnObjective() {
        for (String level : CONTENT.levels().keySet()) {
            BriefingPage teaser = CONTENT.level(level).briefing().teaser();
            for (int sensor = 0; sensor <= 3; sensor++) {
                Intel intel = Intel.of(CONTENT, level, sensor);
                if (!intel.contacts().isEmpty()) {
                    continue;
                }
                LevelData.ThreatProfile profile = intel.profile();
                LevelData.ThreatProfile escort = new LevelData.ThreatProfile(
                        profile.setting(),
                        profile.layers(),
                        profile.density(),
                        profile.traits(),
                        profile.hazards(),
                        profile.boss(),
                        profile.specials(),
                        Optional.of("ESCORT 5 CRAWLERS"),
                        profile.varga());
                Intel withObjective = new Intel(
                        intel.number(),
                        intel.sensor(),
                        escort,
                        intel.directions(),
                        intel.enemies(),
                        intel.waves(),
                        intel.contacts(),
                        intel.secrets(),
                        intel.seconds());
                Measure measure = assertFits(withObjective, teaser, level, level + " escort at L" + sensor + ": ");
                assertTrue(
                        measure.parts.stream().anyMatch(box -> box.what().equals("text \"OBJECTIVE\"")),
                        "the row is shown");
            }
        }
    }

    /**
     * A level whose fields fit keeps its rows 14 px apart (Level 01); a busy one closes them up so they end
     * above Varga's quote (Level 14 at sensor L2, eight enemy types after the Act 2 wave rework), with every
     * enemy type still named.
     */
    @Test
    void aBusyLevelsRowsCloseUpAndKeepEveryName() {
        assertEquals(14, rowStep("act-1-first-contact/level-01-break-at-dawn", 2));
        String level = "act-2-homefront/level-14-siege-spire";
        int step = rowStep(level, 2);
        assertTrue(step < 14 && step >= IntelPanel.LINE, "rows " + step + " px apart");
        Measure measure = new Measure();
        Intel intel = Intel.of(CONTENT, level, 2);
        IntelPanel.layout(measure, intel, CONTENT.level(level).briefing().teaser(), level);
        String names = String.join(
                " ",
                texts(measure)
                        .subList(
                                texts(measure).indexOf("ENEMIES") + 1,
                                texts(measure).indexOf("SECRETS")));
        assertEquals(Names.of(String.join(", ", intel.enemies())), names);
    }

    /**
     * Where closing up the rows is not enough (a level with fourteen enemy types), the names end on "+N
     * MORE" and the panel still fits.
     */
    @Test
    void tooManyEnemyTypesEndOnMore() {
        String level = "act-2-homefront/level-14-siege-spire";
        Intel intel = Intel.of(CONTENT, level, 2);
        List<String> enemies = new ArrayList<>(intel.enemies());
        for (int i = 0; enemies.size() < 14; i++) {
            enemies.add("Polyp Mortar " + i);
        }
        Intel busy = new Intel(
                intel.number(),
                intel.sensor(),
                intel.profile(),
                intel.directions(),
                enemies,
                intel.waves(),
                intel.contacts(),
                intel.secrets(),
                intel.seconds());
        Measure measure = assertFits(busy, CONTENT.level(level).briefing().teaser(), level, "fourteen types: ");
        List<String> texts = texts(measure);
        String last = texts.get(texts.indexOf("SECRETS") - 1);
        assertTrue(last.matches(".*\\+\\d+ MORE"), "ends on +N MORE: " + texts);
    }

    /** The panel's text lines in order, without the quote marks of the measure. */
    private static List<String> texts(Measure measure) {
        return measure.parts.stream()
                .map(Box::what)
                .filter(what -> what.startsWith("text \""))
                .map(what -> what.substring(6, what.length() - 1))
                .toList();
    }

    /** How far apart the SETTING and LAYERS rows are. */
    private static int rowStep(String level, int sensor) {
        Measure measure = new Measure();
        IntelPanel.layout(
                measure,
                Intel.of(CONTENT, level, sensor),
                CONTENT.level(level).briefing().teaser(),
                level);
        int setting = measure.parts.stream()
                .filter(box -> box.what().equals("text \"SETTING\""))
                .findFirst()
                .orElseThrow()
                .top();
        int layers = measure.parts.stream()
                .filter(box -> box.what().equals("text \"LAYERS\""))
                .findFirst()
                .orElseThrow()
                .top();
        return layers - setting;
    }

    /**
     * Level 14's strip at sensor L3 marks the Siege Spire's arrival at 108 s (its *Sensor-suite detail*;
     * step K10; M7 part H, the user's round 43 answer H.7e: the boss's silhouette at its time, drawn for Level 14
     * too): its sensor silhouette centred on the time, across the strip's depth, inside the panel and above Varga's
     * quote, and no red tick; below L3 there is no strip, so no mark.
     */
    @Test
    void level14sStripMarksTheBossAtItsArrival() {
        assertStripSilhouette(14, "siege-spire", 108);
        String level = CONTENT.levelKey(14).orElseThrow();
        Measure l2 = new Measure();
        IntelPanel.layout(
                l2, Intel.of(CONTENT, level, 2), CONTENT.level(level).briefing().teaser(), level);
        assertTrue(l2.fills.stream().noneMatch(fill -> fill.colour().equals(Glass.ALERT)), "no strip below L3");
        assertTrue(l2.marks.isEmpty(), "no strip below L3");
    }

    /**
     * M8 part B (Level 22's threat profile at L3: "the wave strip marking the four rig sites"): a red "T" at each Drill
     * Rig's time (40, 56, 72, 88 s; a tick over the strip's lower half and its crossbar), inside the strip; no other
     * level has one, and below L3 there is no strip.
     */
    @Test
    void level22sStripMarksTheFourRigSites() {
        String level = CONTENT.levelKey(22).orElseThrow();
        Intel intel = Intel.of(CONTENT, level, 3);
        List<Double> sites = intel.marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.TARGET)
                .map(Intel.Mark::from)
                .toList();
        assertEquals(List.of(40.0, 56.0, 72.0, 88.0), sites);
        Measure measure = new Measure();
        IntelPanel.layout(measure, intel, CONTENT.level(level).briefing().teaser(), level);
        Box strip = measure.parts.stream()
                .filter(part -> part.what().equals("wave strip"))
                .findFirst()
                .orElseThrow();
        List<Fill> red = measure.fills.stream()
                .filter(fill -> fill.colour().equals(Glass.ALERT))
                .toList();
        assertEquals(2 * sites.size(), red.size(), "a tick and a crossbar a site, no boss tick: " + red);
        for (Fill fill : red) {
            assertTrue(
                    fill.x() >= strip.left()
                            && fill.x() + fill.width() <= strip.right()
                            && fill.y() >= strip.top()
                            && fill.y() + fill.height() <= strip.bottom(),
                    "inside the strip: " + fill);
        }
        for (String other : CONTENT.levels().keySet()) {
            // M8 part C: Level 23's siphon pumps are an escapes secondary's sites too (its own test).
            boolean escapes = CONTENT.level(other)
                    .objectives()
                    .secondary()
                    .flatMap(LevelData.Secondary::escapes)
                    .isPresent();
            if (!other.equals(level) && !escapes) {
                assertTrue(
                        Intel.of(CONTENT, other, 3).marks().stream()
                                .noneMatch(mark -> mark.kind() == Intel.Mark.Kind.TARGET),
                        other);
            }
        }
        Measure l2 = new Measure();
        IntelPanel.layout(
                l2, Intel.of(CONTENT, level, 2), CONTENT.level(level).briefing().teaser(), level);
        assertTrue(l2.fills.stream().noneMatch(fill -> fill.colour().equals(Glass.ALERT)), "no strip below L3");
    }

    /**
     * M8 part C (design/campaign Level 23, Threat profile at L3: "the wave strip marking the waterline"): the strip marks
     * the waterline at 25.5 s with a teal tick and the water after it as a dim band to the strip's end, and the five
     * siphon pumps' sites as the secondary's red marks; no other level has a waterline yet, and below L3 no strip.
     */
    @Test
    void level23sStripMarksTheWaterlineAndThePumps() {
        String level = CONTENT.levelKey(23).orElseThrow();
        Intel intel = Intel.of(CONTENT, level, 3);
        List<Double> waterlines = intel.marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.WATERLINE)
                .map(Intel.Mark::from)
                .toList();
        assertEquals(List.of(25.5), waterlines);
        assertEquals(
                5,
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.TARGET)
                        .count(),
                "the pumps");
        Measure measure = new Measure();
        IntelPanel.layout(measure, intel, CONTENT.level(level).briefing().teaser(), level);
        Box strip = measure.parts.stream()
                .filter(part -> part.what().equals("wave strip"))
                .findFirst()
                .orElseThrow();
        List<Fill> tick = measure.fills.stream()
                .filter(fill -> fill.colour().equals(IntelPanel.WATERLINE))
                .toList();
        List<Fill> band = measure.fills.stream()
                .filter(fill -> fill.colour().equals(IntelPanel.WATER_BAND))
                .toList();
        assertEquals(1, tick.size(), "one waterline");
        assertEquals(1, band.size(), "the water after it");
        float at = strip.left() + 2 + (strip.right() - strip.left() - 4) * 25.5f / (float) intel.seconds();
        assertTrue(Math.abs(tick.getFirst().x() - at) <= 2, "at 25.5 s: " + tick.getFirst() + " vs " + at);
        assertTrue(band.getFirst().x() > tick.getFirst().x() - 2, "the band starts at the line");
        assertTrue(band.getFirst().x() + band.getFirst().width() <= strip.right(), "inside the strip");
        for (String other : CONTENT.levels().keySet()) {
            if (!other.equals(level) && !other.equals(LEVEL_28)) {
                assertTrue(
                        Intel.of(CONTENT, other, 3).marks().stream()
                                .noneMatch(mark -> mark.kind() == Intel.Mark.Kind.WATERLINE),
                        other);
            }
        }
        Measure l2 = new Measure();
        IntelPanel.layout(
                l2, Intel.of(CONTENT, level, 2), CONTENT.level(level).briefing().teaser(), level);
        assertTrue(l2.fills.stream().noneMatch(fill -> fill.colour().equals(IntelPanel.WATERLINE)), "no strip at L2");
    }

    /**
     * M8 part D (design/campaign Level 24, Threat profile at L3: "the wave strip marking the three Rays"): the strip marks
     * the three Abyss Rays' waves (58, 112, 150 s) with violet ticks and the four spawning reefs as the secondary's red
     * marks; no other level has a Ray yet, and below L3 no strip.
     */
    @Test
    void level24sStripMarksTheThreeRaysAndTheReefs() {
        String level = CONTENT.levelKey(24).orElseThrow();
        Intel intel = Intel.of(CONTENT, level, 3);
        List<Double> rays = intel.marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.RAY)
                .map(Intel.Mark::from)
                .toList();
        assertEquals(List.of(58.0, 112.0, 150.0), rays);
        assertEquals(
                4,
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.TARGET)
                        .count(),
                "the reefs");
        Measure measure = new Measure();
        IntelPanel.layout(measure, intel, CONTENT.level(level).briefing().teaser(), level);
        Box strip = measure.parts.stream()
                .filter(part -> part.what().equals("wave strip"))
                .findFirst()
                .orElseThrow();
        List<Fill> ticks = measure.fills.stream()
                .filter(fill -> fill.colour().equals(IntelPanel.RAY_VIOLET))
                .toList();
        assertEquals(3, ticks.size(), "three Rays");
        for (int r = 0; r < 3; r++) {
            float at = strip.left()
                    + 2
                    + (strip.right() - strip.left() - 4) * rays.get(r).floatValue() / (float) intel.seconds();
            assertTrue(Math.abs(ticks.get(r).x() - at) <= 2, rays.get(r) + " s: " + ticks.get(r) + " vs " + at);
        }
        // M8 part G (step G2): Level 27's one Ray (t=106) is marked too; its strip is step G4's (IntelPanelLayoutTest).
        String level27 = "act-4-deep-water/level-27-hydrothermal-vents";
        assertEquals(
                List.of(106.0),
                Intel.of(CONTENT, level27, 3).marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.RAY)
                        .map(Intel.Mark::from)
                        .toList());
        for (String other : CONTENT.levels().keySet()) {
            if (!other.equals(level) && !other.equals(level27) && !other.equals(LEVEL_28)) {
                assertTrue(
                        Intel.of(CONTENT, other, 3).marks().stream()
                                .noneMatch(mark -> mark.kind() == Intel.Mark.Kind.RAY),
                        other);
            }
        }
        Measure l2 = new Measure();
        IntelPanel.layout(
                l2, Intel.of(CONTENT, level, 2), CONTENT.level(level).briefing().teaser(), level);
        assertTrue(l2.fills.stream().noneMatch(fill -> fill.colour().equals(IntelPanel.RAY_VIOLET)), "no strip at L2");
    }

    /**
     * M8 part E (design/campaign Level 25, Threat profile at L3: "the side caves' entry points"): the strip marks the ten
     * cave waves (29 to 149.5 s) with slate ticks over its upper half; no other level has a cave yet, and below L3 no
     * strip.
     */
    @Test
    void level25sStripMarksTheCaveEntries() {
        String level = CONTENT.levelKey(25).orElseThrow();
        Intel intel = Intel.of(CONTENT, level, 3);
        List<Double> caves = intel.marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.CAVE)
                .map(Intel.Mark::from)
                .toList();
        assertEquals(List.of(29.0, 39.0, 49.0, 58.5, 72.0, 86.0, 101.0, 117.5, 133.5, 149.5), caves);
        Measure measure = new Measure();
        IntelPanel.layout(measure, intel, CONTENT.level(level).briefing().teaser(), level);
        Box strip = measure.parts.stream()
                .filter(part -> part.what().equals("wave strip"))
                .findFirst()
                .orElseThrow();
        List<Fill> ticks = measure.fills.stream()
                .filter(fill -> fill.colour().equals(IntelPanel.CAVE_SLATE))
                .toList();
        assertEquals(caves.size(), ticks.size(), "a tick a cave wave");
        for (int c = 0; c < caves.size(); c++) {
            float at = strip.left()
                    + 2
                    + (strip.right() - strip.left() - 4) * caves.get(c).floatValue() / (float) intel.seconds();
            assertTrue(Math.abs(ticks.get(c).x() - at) <= 2, caves.get(c) + " s: " + ticks.get(c) + " vs " + at);
        }
        for (String other : CONTENT.levels().keySet()) {
            if (!other.equals(level)) {
                assertTrue(
                        Intel.of(CONTENT, other, 3).marks().stream()
                                .noneMatch(mark -> mark.kind() == Intel.Mark.Kind.CAVE),
                        other);
            }
        }
        Measure l2 = new Measure();
        IntelPanel.layout(
                l2, Intel.of(CONTENT, level, 2), CONTENT.level(level).briefing().teaser(), level);
        assertTrue(l2.fills.stream().noneMatch(fill -> fill.colour().equals(IntelPanel.CAVE_SLATE)), "no strip at L2");
    }

    /**
     * M8 part F (design/campaign Level 26, Threat profile): from sensor L2 the Glow Angler's lure is tagged as no
     * pickup under the enemies' names (only a level with an Angler is tagged); at L3 the strip marks the scarp's seven wall
     * nests in the dark with a teal tick each.
     */
    @Test
    void level26sIntelTagsTheLureAndMarksTheNests() {
        String level = CONTENT.levelKey(26).orElseThrow();
        for (int sensor = 0; sensor <= 3; sensor++) {
            Measure measure = new Measure();
            IntelPanel.layout(
                    measure,
                    Intel.of(CONTENT, level, sensor),
                    CONTENT.level(level).briefing().teaser(),
                    level);
            boolean tagged = measure.parts.stream()
                    .anyMatch(part -> part.what().equals("text \"" + IntelPanel.NOT_A_PICKUP + "\""));
            assertEquals(sensor >= 2, tagged, "the lure's tag at L" + sensor);
            long nests = measure.fills.stream()
                    .filter(fill -> fill.colour().equals(IntelPanel.NEST_TEAL))
                    .count();
            assertEquals(sensor >= 3 ? 7 : 0, nests, "the nests' ticks at L" + sensor);
        }
        for (String other : CONTENT.levels().keySet()) {
            if (other.equals(level)) {
                continue;
            }
            Measure measure = new Measure();
            Intel intel = Intel.of(CONTENT, other, 3);
            IntelPanel.layout(measure, intel, CONTENT.level(other).briefing().teaser(), other);
            boolean lure = intel.enemies().stream()
                    .map(vanguard.game.render.Portraits::slug)
                    .anyMatch(IntelPanel.LURES::contains);
            assertEquals(
                    lure,
                    measure.parts.stream().anyMatch(part -> part.what().contains(IntelPanel.NOT_A_PICKUP)),
                    other);
        }
    }

    /**
     * M8 part G (design/campaign Level 27, Threat profile): from sensor L2 the Depth Hunter is tagged unmarked grey under
     * the enemies' names (only a level with Hunters is tagged); at L3 the strip marks the three Hunter columns (56, 96,
     * 126 s) with hull-grey ticks and the four relay beacons on the ridge (94–116 s) as the secondary's red marks.
     */
    @Test
    void level27sIntelTagsTheGreyHullsAndMarksTheColumnsAndBeacons() {
        String level = CONTENT.levelKey(27).orElseThrow();
        Intel intel = Intel.of(CONTENT, level, 3);
        assertEquals(
                List.of(56.0, 96.0, 126.0),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.COLUMN)
                        .map(Intel.Mark::from)
                        .toList());
        assertEquals(
                List.of(94.0, 101.0, 108.0, 116.0),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.TARGET)
                        .map(Intel.Mark::from)
                        .toList(),
                "the beacon ridge");
        for (int sensor = 0; sensor <= 3; sensor++) {
            Measure measure = assertFits(
                    Intel.of(CONTENT, level, sensor),
                    CONTENT.level(level).briefing().teaser(),
                    level,
                    "L" + sensor);
            boolean tagged = measure.parts.stream()
                    .anyMatch(part -> part.what().equals("text \"" + IntelPanel.UNMARKED_GREY + "\""));
            assertEquals(sensor >= 2, tagged, "the grey hulls' tag at L" + sensor);
            long columns = measure.fills.stream()
                    .filter(fill -> fill.colour().equals(IntelPanel.HULL_GREY))
                    .count();
            assertEquals(sensor >= 3 ? 3 : 0, columns, "the columns' ticks at L" + sensor);
        }
        for (String other : CONTENT.levels().keySet()) {
            if (other.equals(level)) {
                continue;
            }
            Intel otherIntel = Intel.of(CONTENT, other, 3);
            // M8 part H: Level 28's Hunter columns are marked too (level28sStripStopsAtTheMawsArrival); M9 part C:
            // Level 30's Minelayer columns from the rear too (its threat profile: "the wave strip marking the rear
            // entries").
            assertEquals(
                    other.equals(LEVEL_28) || other.equals(CONTENT.levelKey(30).orElseThrow()),
                    otherIntel.marks().stream().anyMatch(mark -> mark.kind() == Intel.Mark.Kind.COLUMN),
                    other);
            Measure measure = new Measure();
            IntelPanel.layout(
                    measure, otherIntel, CONTENT.level(other).briefing().teaser(), other);
            boolean hunters = otherIntel.enemies().stream()
                    .map(vanguard.game.render.Portraits::slug)
                    .anyMatch(IntelPanel.UNMARKED::contains);
            assertEquals(
                    hunters,
                    measure.parts.stream().anyMatch(part -> part.what().contains(IntelPanel.UNMARKED_GREY)),
                    other);
        }
    }

    /** M8 part H: Level 28's key. */
    private static final String LEVEL_28 = "act-4-deep-water/level-28-abyssal-maw";

    /**
     * M8 part H (design/campaign Level 28, Threat profile): at sensor L3 the strip marks the Maw's silhouette at its
     * arrival (104 s), the approach's Hunter columns and Abyss Rays; the finale after the boss is not on the strip (the
     * reveal is a surprise): the breakout's waterline (115 s) is in the intel but not drawn, no water band. Every sensor
     * level fits, Varga's lines included, and no other level marks anything after its boss.
     */
    @Test
    void level28sStripStopsAtTheMawsArrival() {
        assertStripSilhouette(28, "abyssal-maw", 104);
        Intel intel = Intel.of(CONTENT, LEVEL_28, 3);
        assertEquals(104, IntelPanel.finale(intel), "the strip ends its marks at the Maw's arrival");
        assertEquals(
                List.of(115.0),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.WATERLINE)
                        .map(Intel.Mark::from)
                        .toList(),
                "the breakout's waterline, in the finale");
        for (int sensor = 0; sensor <= 3; sensor++) {
            Measure measure = assertFits(
                    Intel.of(CONTENT, LEVEL_28, sensor),
                    CONTENT.level(LEVEL_28).briefing().teaser(),
                    LEVEL_28,
                    "L" + sensor);
            assertTrue(
                    measure.fills.stream()
                            .noneMatch(fill -> fill.colour().equals(IntelPanel.WATERLINE)
                                    || fill.colour().equals(IntelPanel.WATER_BAND)),
                    "no finale on the strip at L" + sensor);
            long columns = measure.fills.stream()
                    .filter(fill -> fill.colour().equals(IntelPanel.HULL_GREY))
                    .count();
            long expected = intel.marks().stream()
                    .filter(mark -> mark.kind() == Intel.Mark.Kind.COLUMN)
                    .count();
            assertEquals(sensor >= 3 ? expected : 0, columns, "the Hunter columns at L" + sensor);
            assertEquals(
                    sensor >= 3 ? 2 : 0,
                    measure.fills.stream()
                            .filter(fill -> fill.colour().equals(IntelPanel.CHOIR_LIME))
                            .count(),
                    "the Choir's song (its tick and its dot) at L" + sensor);
            assertEquals(
                    sensor >= 3
                            ? intel.marks().stream()
                                    .filter(mark -> mark.kind() == Intel.Mark.Kind.RAY)
                                    .count()
                            : 0,
                    measure.fills.stream()
                            .filter(fill -> fill.colour().equals(IntelPanel.RAY_VIOLET))
                            .count(),
                    "the Rays at L" + sensor);
        }
        assertTrue(expectedColumnsBeforeTheBoss(intel), "the columns guard the approach");
        // Step H6: the Choir's song at 97.5 s, before the boss, drawn at L3 only; no other level's strip marks it.
        assertEquals(
                List.of(97.5),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.CHOIR)
                        .map(Intel.Mark::from)
                        .toList(),
                "the Choir's song");
        for (String level : CONTENT.levels().keySet()) {
            Intel other = Intel.of(CONTENT, level, 3);
            double finale = IntelPanel.finale(other);
            if (!level.equals(LEVEL_28)) {
                assertTrue(
                        other.marks().stream().allMatch(mark -> mark.from() <= finale),
                        level + ": nothing after the boss " + other.marks());
            }
            assertEquals(
                    level.equals(LEVEL_28),
                    other.marks().stream().anyMatch(mark -> mark.kind() == Intel.Mark.Kind.CHOIR),
                    level + ": the Choir's song on the strip");
        }
    }

    private static boolean expectedColumnsBeforeTheBoss(Intel intel) {
        return intel.marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.COLUMN || mark.kind() == Intel.Mark.Kind.RAY)
                .allMatch(mark -> mark.from() < 104);
    }

    /** M7 part H (H.7e): Level 21's strip at sensor L3 marks the Dust Colossus's silhouette at 105 s. */
    @Test
    void level21sStripMarksTheColossusSilhouetteAtItsArrival() {
        assertStripSilhouette(21, "dust-colossus", 105);
    }

    /**
     * Round 44 Q.8 d (step GI1): Levels 20 and 21's strips at sensor L3 mark the Colossus's ridge pass as one sand band
     * from its surfacing to its head off the far edge (Level 20: 12–20 s, Level 21: 64–72 s), inside the strip; no
     * other level has one, and below L3 there is no strip.
     */
    @Test
    void levels20And21sStripsMarkTheRidgePass() {
        Map<Integer, double[]> ridges = Map.of(20, new double[] {12, 20}, 21, new double[] {64, 72});
        for (String level : CONTENT.levels().keySet()) {
            Intel intel = Intel.of(CONTENT, level, 3);
            Measure measure = assertFits(intel, CONTENT.level(level).briefing().teaser(), level, level + ": ");
            List<Fill> sand = measure.fills.stream()
                    .filter(fill -> fill.colour().equals(IntelPanel.RIDGE_SAND))
                    .toList();
            int number = intel.number();
            if (!ridges.containsKey(number)) {
                assertTrue(sand.isEmpty(), level + ": no ridge pass " + sand);
                continue;
            }
            assertEquals(1, sand.size(), level + ": one ridge band " + sand);
            Fill band = sand.getFirst();
            Box strip = measure.parts.stream()
                    .filter(box -> box.what().equals("wave strip"))
                    .findFirst()
                    .orElseThrow();
            float width = strip.right() - strip.left() - 4;
            double[] span = ridges.get(number);
            float from = (float) (strip.left() + 2 + width * span[0] / intel.seconds());
            float to = (float) (strip.left() + 2 + width * span[1] / intel.seconds());
            assertEquals(from + 1, band.x(), 0.01f, level + ": from the surfacing");
            assertEquals(to - 1, band.x() + band.width(), 0.01f, level + ": to the head off the edge");
            assertTrue(band.width() > 0, level + ": a span");
            assertTrue(
                    band.y() >= strip.top() && band.y() + band.height() <= strip.bottom(),
                    level + ": inside the strip " + band + " in " + strip);
            Measure l2 = new Measure();
            IntelPanel.layout(
                    l2,
                    Intel.of(CONTENT, level, 2),
                    CONTENT.level(level).briefing().teaser(),
                    level);
            assertTrue(
                    l2.fills.stream().noneMatch(fill -> fill.colour().equals(IntelPanel.RIDGE_SAND)),
                    level + ": no strip below L3");
        }
    }

    private static void assertStripSilhouette(int number, String boss, double arrival) {
        String level = CONTENT.levelKey(number).orElseThrow();
        Intel intel = Intel.of(CONTENT, level, 3);
        Measure measure = assertFits(intel, CONTENT.level(level).briefing().teaser(), level, level + ": ");
        Box strip = measure.parts.stream()
                .filter(box -> box.what().equals("wave strip"))
                .findFirst()
                .orElseThrow();
        assertTrue(
                measure.fills.stream().noneMatch(fill -> fill.colour().equals(Glass.ALERT)),
                "the silhouette instead of the red tick");
        assertEquals(1, measure.marks.size(), "one boss mark: " + measure.marks);
        StripMark mark = measure.marks.getFirst();
        assertEquals("intel/boss-" + boss, mark.name());
        float x = (float) (strip.left() + 2 + (strip.right() - strip.left() - 4) * arrival / intel.seconds());
        assertEquals(x + 1, mark.x() + mark.size() / 2f, 0.01f, "centred on " + arrival + " s of " + intel.seconds());
        assertTrue(
                mark.y() <= strip.top() + 2 && mark.y() + mark.size() >= strip.bottom() - 2,
                "across the strip's depth: " + mark + " in " + strip);
        assertTrue(
                mark.x() >= IntelPanel.X && mark.x() + mark.size() <= RIGHT && mark.y() >= TOP,
                "inside the panel: " + mark);
        assertTrue(mark.y() + mark.size() <= measure.quote.getFirst().top(), "above Varga's quote: " + mark);
    }

    /**
     * The teaser's speaker shows with the name plate and the role line of the briefing's speaker
     * (Level 08's capture, round 30: Rook's teaser read {@code LT. K. TANAKA / CDF INTEL}).
     */
    @Test
    void theTeasersSpeakerShowsWithTheirNameAndRole() {
        Map<String, List<String>> plates = Map.of(
                "act-2-homefront/level-08-neon-skyline", List.of("LT. K. TANAKA", "AEGIS TWO"),
                "act-1-first-contact/level-01-break-at-dawn", List.of("DR. E. VARGA", "CDF INTELLIGENCE"));
        plates.forEach((level, expected) -> {
            Measure measure = new Measure();
            IntelPanel.layout(
                    measure,
                    Intel.of(CONTENT, level, 3),
                    CONTENT.level(level).briefing().teaser(),
                    level);
            List<String> texts = measure.parts.stream().map(Box::what).toList();
            assertEquals(
                    List.of("text \"" + expected.get(0) + "\"", "text \"" + expected.get(1) + "\""),
                    texts.subList(1, 3),
                    level);
            assertEquals(
                    expected.get(1),
                    Speaker.role(CONTENT.level(level).briefing().teaser().speaker()));
        });
    }

    /**
     * M7 part E (round 36 Q.22h): before the campaign has flown Level 18, its intel shows the Ghost Drone as an
     * "unclassified, small, air" silhouette, unnamed, from sensor L2; once Level 18 is flown (a later level, or the
     * replay hangar's campaign) by its portrait and name. A unit without the flag is never unclassified.
     */
    @Test
    void theGhostDroneStaysUnclassifiedUntilLevel18IsFlown() {
        String level = CONTENT.levelKey(18).orElseThrow();
        BriefingPage teaser = CONTENT.level(level).briefing().teaser();
        Intel intel = Intel.of(CONTENT, level, 2);
        assertTrue(intel.enemies().contains("Ghost Drone"), "Level 18 flies it: " + intel.enemies());
        Map<String, String> unclassified = IntelPanel.unclassified(CONTENT, intel.enemies(), 18);
        assertEquals(Map.of("Ghost Drone", "Unclassified small air"), unclassified);
        assertEquals(Map.of(), IntelPanel.unclassified(CONTENT, intel.enemies(), 19), "flown: classified");
        assertEquals(
                Map.of(), IntelPanel.unclassified(CONTENT, List.of("Dust Devil"), 18), "the Dust Devil is not kept");
        Measure measure = assertFits(intel, teaser, level, "Level 18 unclassified: ", unclassified);
        List<String> parts = measure.parts.stream().map(Box::what).toList();
        assertTrue(parts.contains("intel/unclassified-ghost-drone"), "its silhouette: " + parts);
        assertFalse(parts.contains("intel/ghost-drone"), "not its portrait");
        assertTrue(parts.stream().noneMatch(part -> part.contains("GHOST")), "nor its name: " + parts);
        assertTrue(parts.stream().anyMatch(part -> part.contains("UNCLASSIFIED")), "but its size and layer");
        Measure flown = assertFits(intel, teaser, level, "Level 18 flown: ", Map.of());
        List<String> named = flown.parts.stream().map(Box::what).toList();
        assertFalse(named.contains("intel/unclassified-ghost-drone"), "flown: no silhouette");
        assertTrue(String.join(" ", named).contains("GHOST"), "flown: named " + named);
    }

    /**
     * M7 part F: Level 19's intel at sensor L2: its two hazards wrapped to the value column, the walker's "huge,
     * ground" contact silhouette at the boss row; a boss kept unclassified is named by its size and layer alone
     * (design/campaign Level 19: the walker unnamed before the fight), and the boss is offered to that check.
     */
    @Test
    void levelNineteensWalkerShowsItsSilhouetteAndAnUnclassifiedBossIsUnnamed() {
        String level = CONTENT.levelKey(19).orElseThrow();
        BriefingPage teaser = CONTENT.level(level).briefing().teaser();
        Intel intel = Intel.of(CONTENT, level, 2);
        assertTrue(IntelPanel.withBoss(intel).contains("Revenant Walker"), "the boss is checked too");
        assertFalse(intel.enemies().contains("Revenant Walker"), "not an enemy type of the row");
        Measure named = assertFits(intel, teaser, level, "Level 19: ");
        List<String> parts = named.parts.stream().map(Box::what).toList();
        assertTrue(parts.contains("intel/boss-revenant-walker"), "its contact silhouette: " + parts);
        assertTrue(parts.stream().anyMatch(part -> part.contains("FURNACE VENTS")), "the hazards: " + parts);
        Measure unnamed = assertFits(
                intel, teaser, level, "Level 19 unclassified: ", Map.of("Revenant Walker", "Unclassified huge ground"));
        List<String> texts = unnamed.parts.stream().map(Box::what).toList();
        assertTrue(texts.stream().noneMatch(part -> part.contains("REVENANT")), "unnamed: " + texts);
        assertTrue(texts.stream().anyMatch(part -> part.contains("UNCLASSIFIED HUGE")), "its size and layer: " + texts);
        assertTrue(texts.contains("intel/boss-revenant-walker"), "with its silhouette");
    }

    private static Measure assertFits(Intel intel, BriefingPage teaser, String level, String where) {
        return assertFits(intel, teaser, level, where, Map.of());
    }

    private static Measure assertFits(
            Intel intel, BriefingPage teaser, String level, String where, Map<String, String> unclassified) {
        return assertFits(intel, teaser, level, where, unclassified, IntelPanel.Strip.NONE);
    }

    /**
     * M9 part G (step G4; design/campaign Level 34, Sensor-suite detail): from L2 the Warden Tank's portrait in its
     * Ascendancy livery and the Helix core tagged as the objective's pickup under the names; at L3 the three hidden
     * crates' sections underlined in gold on the wave strip; M9 part H (step H4): from L2 the tank's faction in Act 5
     * under the names ({@code EnemyData.factionIn}). Every sensor level fits with the level's strip; no other
     * level has the tag or the gold.
     */
    @Test
    void level34sIntelShowsTheLiveryTheCoreAndTheCratesSections() {
        String level = CONTENT.levelKey(34).orElseThrow();
        IntelPanel.Strip strip = IntelPanel.strip(CONTENT, level);
        for (int sensor = 0; sensor <= 3; sensor++) {
            Measure measure = assertFits(
                    Intel.of(CONTENT, level, sensor),
                    CONTENT.level(level).briefing().teaser(),
                    level,
                    "L" + sensor + ": ",
                    Map.of(),
                    strip);
            List<String> texts = measure.parts.stream().map(Box::what).toList();
            assertFalse(texts.contains("intel/warden-tank"), "not the grey tank's portrait at L" + sensor);
            assertEquals(
                    sensor >= 2,
                    texts.contains("intel/warden-tank-asc"),
                    "the livery's portrait (pictured first) at L" + sensor + ": " + texts);
            assertEquals(
                    sensor >= 2,
                    texts.contains("text \"OBJECTIVE: HELIX CORE\""),
                    "the core's tag at L" + sensor + ": " + texts);
            // M9 part H (step H4, step G5's finding): the liveried tank named with its faction in Act 5.
            assertEquals(
                    sensor >= 2,
                    texts.stream().anyMatch(text -> text.startsWith("text \"WARDEN TANK: JOVIAN")),
                    "the tank's faction at L" + sensor + ": " + texts);
            long gold = measure.fills.stream()
                    .filter(fill -> fill.colour().equals(IntelPanel.CRATE_GOLD))
                    .count();
            assertEquals(sensor >= 3 ? 3 : 0, gold, "the crates' sections at L" + sensor);
        }
        assertEquals(java.util.Map.of("warden-tank", "warden-tank-asc"), strip.liveries());
        assertEquals(java.util.Map.of("warden-tank", "Jovian Ascendancy"), strip.factions());
        assertEquals("WARDEN TANK: JOVIAN ASCENDANCY", IntelPanel.factionTag("Warden Tank", "Jovian Ascendancy"));
        assertTrue(
                IntelPanel.factions(CONTENT, CONTENT.levelKey(19).orElseThrow()).isEmpty(),
                "Level 19's tank flies unmarked, no faction tag");
        for (String other : CONTENT.levels().keySet()) {
            if (other.equals(level)) {
                continue;
            }
            IntelPanel.Strip otherStrip = IntelPanel.strip(CONTENT, other);
            assertTrue(otherStrip.objective().isEmpty() && otherStrip.crates().isEmpty(), other);
            assertEquals(
                    vanguard.game.render.EnemyLooks.act(other) >= 5,
                    !otherStrip.liveries().isEmpty(),
                    other + ": the livery from Act 5 only");
        }
    }

    /**
     * M9 part H (step H4; design/campaign Level 35, Sensor-suite detail): at L3 the wave strip marks the mass relay's flash
     * (61 s: its telegraph from 59.8 s, 1.2 s) with a crimson tick and the boss's arrival (92 s) with its silhouette;
     * every sensor level fits with the level's strip; no other level has a relay.
     */
    @Test
    void level35sIntelMarksTheRelaysFlashAndTheBoss() {
        String level = CONTENT.levelKey(35).orElseThrow();
        IntelPanel.Strip strip = IntelPanel.strip(CONTENT, level);
        assertEquals(61, strip.relay().orElseThrow(), 1e-9, "the flash at 61 s");
        assertStripSilhouette(35, "iron-sovereign", 92);
        for (int sensor = 0; sensor <= 3; sensor++) {
            Measure measure = assertFits(
                    Intel.of(CONTENT, level, sensor),
                    CONTENT.level(level).briefing().teaser(),
                    level,
                    "L" + sensor + ": ",
                    Map.of(),
                    strip);
            List<String> texts = measure.parts.stream().map(Box::what).toList();
            long red = measure.fills.stream()
                    .filter(fill -> fill.colour().equals(IntelPanel.RELAY_RED))
                    .count();
            assertEquals(sensor >= 3 ? 2 : 0, red, "the relay's tick at L" + sensor);
            assertEquals(
                    sensor >= 3,
                    measure.marks.stream().anyMatch(mark -> mark.name().equals("intel/boss-iron-sovereign")),
                    "the boss's silhouette on the strip at L" + sensor + ": " + texts);
        }
        for (String other : CONTENT.levels().keySet()) {
            if (!other.equals(level)) {
                assertTrue(IntelPanel.strip(CONTENT, other).relay().isEmpty(), other);
            }
        }
    }

    private static Measure assertFits(
            Intel intel,
            BriefingPage teaser,
            String level,
            String where,
            Map<String, String> unclassified,
            IntelPanel.Strip strip) {
        Measure measure = new Measure();
        IntelPanel.layout(measure, intel, teaser, level, unclassified, java.util.Optional.empty(), strip);
        List<Box> all = new ArrayList<>(measure.parts);
        all.addAll(measure.quote);
        for (Box box : all) {
            assertTrue(
                    box.left() >= IntelPanel.X && box.right() <= RIGHT && box.top() >= TOP && box.bottom() <= BOTTOM,
                    where + box + " leaves the panel");
        }
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                assertFalse(all.get(i).overlaps(all.get(j)), where + all.get(i) + " overlaps " + all.get(j));
            }
        }
        assertFalse(measure.quote.isEmpty(), where + "Varga's line is shown");
        int quoteTop = measure.quote.getFirst().top();
        for (Box box : measure.parts) {
            assertTrue(box.bottom() <= quoteTop, where + box + " runs into Varga's quote");
        }
        return measure;
    }
}
