package vanguard.game.databank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.DebugFit;
import vanguard.content.databank.Databank;
import vanguard.content.databank.DatabankView;
import vanguard.content.databank.Entry;
import vanguard.content.databank.Pages;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Box;

/**
 * Step DB2 (design/ui/databank): the databank screen's layout at 960×540 (the top bar, the tabs, the list's 24 lines,
 * the entry page inside its panel above Varga's inset for every entry a full campaign knows), the ladder of an enemy's
 * page by sensor level (the locked fields' tags, the callout at none, the picture and numbers from L2, the advice at L3)
 * and the list with its counters.
 */
class DatabankScreenTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);
    private static final Databank DATABANK = Databank.of(CONTENT);

    /** What a page drew: every text and box, measured. */
    private static final class Measure implements EntryPage.Canvas {
        final List<String> texts = new ArrayList<>();
        final List<String> tags = new ArrayList<>();
        final List<Box> boxes = new ArrayList<>();
        final List<String> links = new ArrayList<>();
        final List<String> headers = new ArrayList<>();
        /** The ink of every text, tag, chip and name, which must not overlap. */
        final List<Box> ink = new ArrayList<>();

        final List<String> inkTexts = new ArrayList<>();
        int pictures;
        int portraits;
        int chips;
        int testFires;

        private void box(String what, float x, float y, float width, float height) {
            boxes.add(new Box(x, y, width, height));
            texts.add(what);
        }

        private void ink(String what, float x, float y, float width, float height) {
            ink.add(new Box(x, y, width, height));
            inkTexts.add(what);
        }

        @Override
        public void text(String text, Color colour, int x, int y) {
            box(text, x, y, text.length() * 8, 12);
            ink(text, x, y, text.length() * 8, 10);
        }

        @Override
        public void name(String text, int x, int y, boolean body) {
            box(text, x, y, text.length() * (body ? 10 : 20), body ? 20 : 30);
            ink(text, x, y, text.length() * (body ? 10 : 20), body ? 14 : 24);
        }

        @Override
        public void header(String text, int x, int right, int y) {
            headers.add(text);
            box(text, x, y, Math.max(right - x, text.length() * 8), 12);
            ink(text, x, y, text.length() * 8, 10);
        }

        @Override
        public void tag(String text, Color colour, int x, int y, int width) {
            tags.add(text);
            box(text, x, y, width, 12);
            ink(text, x, y, width, 12);
        }

        @Override
        public void picture(int x, int y, int size) {
            pictures++;
            box("picture", x, y, size, size);
        }

        @Override
        public void portrait(int x, int y, int size) {
            portraits++;
            box("portrait", x - 3, y - 3, size + 6, size + 6);
        }

        @Override
        public void inset(int x, int y, int width, int height) {
            box("inset", x, y, width, height);
        }

        @Override
        public void chip(String text, int x, int y, int width, int height, boolean on) {
            chips++;
            box(text, x, y, width, height);
            ink(text, x, y, width, height);
        }

        /** No two texts, tags, chips or names overlap. */
        void assertNoOverlap(String what) {
            for (int i = 0; i < ink.size(); i++) {
                for (int j = i + 1; j < ink.size(); j++) {
                    Box a = ink.get(i);
                    Box b = ink.get(j);
                    boolean overlaps = a.x() < b.x() + b.width()
                            && b.x() < a.x() + a.width()
                            && a.y() < b.y() + b.height()
                            && b.y() < a.y() + a.height();
                    assertFalse(overlaps, what + ": '" + inkTexts.get(i) + "' overlaps '" + inkTexts.get(j) + "'");
                }
            }
        }

        @Override
        public void lit(int x, int y, int width, int height) {}

        @Override
        public void testFire(int x, int y, int width, int height) {
            testFires++;
            box("test fire", x, y, width, height);
        }

        @Override
        public void link(Box box, String id) {
            links.add(id);
        }

        /** Where the content must end: above the inset at the page's foot (Varga's, a note's), or at the foot. */
        int footInset() {
            int foot = EntryPage.FOOT;
            for (int i = 0; i < boxes.size(); i++) {
                Box box = boxes.get(i);
                if (texts.get(i).equals("inset") && box.y() + box.height() == EntryPage.FOOT) {
                    foot = Math.min(foot, (int) box.y() - 4);
                }
            }
            return foot;
        }

        /** Every part inside the entry panel's inner edges. */
        void assertInsidePage(String what) {
            for (int i = 0; i < boxes.size(); i++) {
                Box box = boxes.get(i);
                String part = what + ": '" + texts.get(i) + "'";
                assertTrue(box.x() >= EntryPage.X + 4, part + " left of the page at " + box.x());
                assertTrue(
                        box.x() + box.width() <= EntryPage.X + EntryPage.WIDTH - 4,
                        part + " runs past the page's right edge: " + (box.x() + box.width()));
                assertTrue(box.y() >= EntryPage.Y, part + " above the page");
                assertTrue(
                        box.y() + box.height() <= EntryPage.Y + EntryPage.HEIGHT - 2,
                        part + " below the page's foot: " + (box.y() + box.height()));
            }
        }
    }

    /** A Level {@code level} campaign that met every unit of the levels before it, with {@code fit}'s gear. */
    private static Campaign campaign(int level, Difficulty difficulty, String fit) {
        Campaign campaign = DebugFit.startAt(RULES, difficulty, level);
        if (!fit.isEmpty()) {
            DebugFit.parse(fit).applyTo(campaign, CATALOGUE);
        }
        DATABANK.roster()
                .firstMet(level, difficulty)
                .forEach((slug, first) -> campaign.recordAttempt(first, List.of(slug), Map.of(slug, 2)));
        return campaign;
    }

    private static Pages.EnemyPage enemy(Campaign campaign, String slug) {
        DatabankView view = DATABANK.view(campaign);
        return view.enemyPage(DATABANK.unit(slug).orElseThrow());
    }

    @Test
    void theTopBarTabsAndPanelsLieOnTheScreenWithoutOverlapping() {
        for (int i = 0; i < Entry.Tab.values().length; i++) {
            Box tab = DatabankScreen.tab(i);
            assertTrue(tab.x() >= DatabankScreen.BAR_X && tab.x() + tab.width() <= PixelScreen.WIDTH - 16);
            String label = DatabankScreen.tabLabel(Entry.Tab.values()[i], 99);
            assertTrue(label.length() * 10 <= tab.width() - 8, label + " fits its tab");
            if (i > 0) {
                Box before = DatabankScreen.tab(i - 1);
                assertTrue(before.x() + before.width() < tab.x(), "the tabs do not touch");
            }
            assertTrue(tab.y() >= DatabankScreen.BAR_Y + DatabankScreen.BAR_HEIGHT, "under the top bar");
            assertTrue(tab.y() + tab.height() <= DatabankList.Y, "above the list");
        }
        int longest = 0;
        for (int level = 1; level <= 51; level++) {
            longest =
                    Math.max(longest, DatabankScreen.missionLine(CONTENT, level).length());
        }
        assertTrue(
                DatabankScreen.MISSION_X + longest * 8 < DatabankScreen.BADGE.x(),
                "the longest mission line (" + longest + " characters) ends before the sensor badge");
        assertTrue(DatabankScreen.BADGE.x() + DatabankScreen.BADGE.width() < DatabankScreen.BACK.x());
        assertTrue(DatabankScreen.BACK.x() + DatabankScreen.BACK.width()
                <= DatabankScreen.BAR_X + DatabankScreen.BAR_WIDTH);
        for (int sensor = 0; sensor <= 3; sensor++) {
            for (String line : DatabankScreen.badge(sensor, false)) {
                assertTrue(line.length() * 8 <= DatabankScreen.BADGE.width() - 16, line + " fits the badge");
            }
        }
        assertEquals(DatabankList.X + DatabankList.WIDTH + 8, EntryPage.X, "the list, a gap, the entry");
        assertEquals(PixelScreen.WIDTH - 16, EntryPage.X + EntryPage.WIDTH, "the page's right edge at the bar's");
        assertEquals(DatabankList.Y, EntryPage.Y);
        assertTrue(EntryPage.Y + EntryPage.HEIGHT <= PixelScreen.HEIGHT - 26, "above the key hints");
    }

    @Test
    void theListShowsTwentyFourLinesInsideItsPanel() {
        assertEquals(24, DatabankList.VISIBLE);
        Box first = DatabankList.row(0);
        Box last = DatabankList.row(DatabankList.VISIBLE - 1);
        assertTrue(first.y() > DatabankList.COUNT_Y + 8, "under the count line");
        assertTrue(last.y() + last.height() <= DatabankList.Y + DatabankList.HEIGHT - 4, "inside the panel");
        for (int i = 0; i + 1 < DatabankList.VISIBLE; i++) {
            assertEquals(
                    DatabankList.row(i).y() + DatabankList.row(i).height(),
                    DatabankList.row(i + 1).y(),
                    0.5f);
        }
    }

    @Test
    void anEnemysPageClimbsTheSensorLadder() {
        for (int sensor = 0; sensor <= 3; sensor++) {
            String fit = sensor == 0 ? "" : "utility=sensor-suite:" + sensor;
            Campaign campaign = campaign(40, Difficulty.MEDIUM, fit);
            assertEquals(sensor, DATABANK.view(campaign).sensorLevel(), "the suite's level on medium");
            Pages.EnemyPage page = enemy(campaign, "mirror-interceptor");
            Measure measure = new Measure();
            int bottom = EntryPage.enemy(measure, page);

            long locked = measure.tags.stream()
                    .filter(tag -> tag.startsWith("SENSOR L"))
                    .count();
            int at = sensor;
            long expected =
                    page.fields().stream().filter(field -> field.needs() > at).count();
            assertEquals(expected, locked, "every field the suite does not show is tagged at sensor " + sensor);
            assertEquals(locked + (sensor == 0 ? 1 : 0), measure.links.size(), "each tag and the callout's chip link");
            assertTrue(measure.links.stream().allMatch(EntryPage.SUITE::equals), "to the sensor suite's entry");
            for (int level = 1; level <= 3; level++) {
                int needs = level;
                assertEquals(
                        sensor < needs && page.fields().stream().anyMatch(field -> field.needs() == needs),
                        measure.tags.contains("SENSOR L" + needs),
                        "the tag SENSOR L" + needs + " at sensor " + sensor);
            }
            assertEquals(sensor == 0, measure.texts.contains(EntryPage.SUITE_CHIP), "the callout at sensor none");
            assertEquals(sensor == 0, page.suiteCallout());
            assertEquals(sensor < 2, page.picture().silhouette(), "the silhouette below L2");
            assertEquals(sensor == 3, measure.headers.contains("WEAPON ADVICE"), "the advice at L3");
            assertEquals(
                    sensor >= 2, measure.texts.stream().anyMatch(text -> text.endsWith(" CR")), "the bounty from L2");
            assertEquals(1, measure.portraits, "Varga's inset");
            assertEquals(1, measure.pictures);
            assertTrue(bottom <= measure.footInset(), "above Varga's inset at sensor " + sensor + ": " + bottom);
            measure.assertInsidePage("mirror interceptor at sensor " + sensor);
        }
    }

    @Test
    void aBossPageHasTheLargerPictureBoxForItsPlate() {
        Campaign campaign = campaign(40, Difficulty.MEDIUM, "utility=sensor-suite:3");
        Pages.EnemyPage page = enemy(campaign, "iron-sovereign");
        assertTrue(page.picture().plate());
        Measure measure = new Measure();
        EntryPage.enemy(measure, page);
        Box picture = measure.boxes.get(measure.texts.indexOf("picture"));
        assertEquals(EntryPage.PLATE_PICTURE, picture.width(), "a 192×192 plate fits");
        assertTrue(EntryPage.PLATE_PICTURE >= 192 + 4);
        assertTrue(measure.headers.contains("PHASES"), "a boss's phases at L3");
    }

    /**
     * Every entry a campaign at the last level knows (everything met), at sensor none and L3, on easy and hard: the page
     * keeps inside its panel and its content ends above Varga's inset or the note.
     */
    @Test
    void everyKnownPageFitsItsPanel() {
        int last = DATABANK.lastLevel() + 1;
        List<String> problems = new ArrayList<>();
        for (String fit : List.of("", "utility=sensor-suite:3")) {
            Campaign campaign = campaign(last, Difficulty.HARD, fit);
            DatabankView view = DATABANK.view(campaign);
            for (Entry entry : DATABANK.entries()) {
                if (!view.known(entry)) {
                    continue;
                }
                Measure measure = new Measure();
                int bottom;
                if (entry.kind().unit()) {
                    bottom = EntryPage.enemy(measure, view.enemyPage(entry));
                } else if (entry.kind() == Entry.Kind.CONCEPT) {
                    Pages.ConceptPage concept = view.conceptPage(entry);
                    bottom = EntryPage.concept(measure, concept, EntryPictures.conceptPlate(concept));
                } else {
                    Pages.GearPage gear = view.gearPage(entry);
                    bottom = EntryPage.gear(measure, gear, 1, entry.kind() == Entry.Kind.WEAPON);
                }
                String what = entry.id() + (fit.isEmpty() ? " (no suite)" : " (L3)");
                try {
                    measure.assertInsidePage(what);
                    measure.assertNoOverlap(what);
                    int foot = measure.footInset();
                    assertTrue(bottom <= foot, what + " ends at " + bottom + ", below " + foot);
                } catch (AssertionError e) {
                    problems.add(e.getMessage());
                }
            }
        }
        assertTrue(problems.isEmpty(), problems.size() + " pages do not fit:\n" + String.join("\n", problems));
    }

    @Test
    void theListCountsItsKnownAndUnknownEntriesAndSkipsTheUnknown() {
        Campaign campaign = campaign(37, Difficulty.MEDIUM, "");
        DatabankView view = DATABANK.view(campaign);
        DatabankList list = new DatabankList(view.list(Entry.Tab.ENEMIES));
        Pages.TabList tab = list.tabList();
        assertEquals(tab.known() + " KNOWN - " + tab.unknown() + " UNKNOWN", list.count());
        assertEquals(view.knownCount(Entry.Tab.ENEMIES), tab.known());
        assertTrue(tab.unknown() > 0, "the act's units not met yet count as unknown");
        long known = list.lines().stream()
                .filter(line ->
                        line instanceof DatabankList.Line.Row row && row.row().known())
                .count();
        assertEquals(tab.known(), known);
        assertTrue(list.lines().getFirst() instanceof DatabankList.Line.Header, "a group header leads");

        list.start(Optional.empty());
        assertTrue(list.selectable(list.selected()));
        for (int step = 0; step < list.lines().size(); step++) {
            list.move(1);
            assertTrue(list.selectable(list.selected()), "the cursor skips headers and ??? rows");
        }
        int lastKnown = list.selected();
        assertFalse(list.move(1), "no wrap past the last known row");
        assertEquals(lastKnown, list.selected());
        assertTrue(list.selected() >= list.top() && list.selected() < list.top() + DatabankList.VISIBLE, "in view");

        list.start(Optional.of("chimera"));
        assertEquals("chimera", list.entry().orElseThrow().id(), "the last entry opened in the tab");
    }

    @Test
    void theCursorStartsOnTheFirstNewEntry() {
        Campaign campaign = campaign(12, Difficulty.MEDIUM, "");
        DatabankView view = DATABANK.view(campaign);
        DatabankList list = new DatabankList(view.list(Entry.Tab.ENEMIES));
        list.start(Optional.empty());
        DatabankList.Line.Row row = (DatabankList.Line.Row) list.lines().get(list.selected());
        assertTrue(row.row().fresh(), "nothing read: the first new entry");

        Entry first = row.row().entry().orElseThrow();
        campaign.openedEntry(first.id());
        DatabankList after = new DatabankList(DATABANK.view(campaign).list(Entry.Tab.ENEMIES));
        after.start(Optional.empty());
        assertFalse(after.entry().orElseThrow().id().equals(first.id()), "the next new one once it is read");
    }

    @Test
    void gearPagesShowTheirLevelTableAndAWeaponItsTestFire() {
        Campaign campaign = campaign(20, Difficulty.MEDIUM, "front=pulse-cannon:2");
        DatabankView view = DATABANK.view(campaign);
        Pages.GearPage page = view.gearPage(DATABANK.entry("pulse-cannon").orElseThrow());
        Measure measure = new Measure();
        EntryPage.gear(measure, page, 2, true);
        assertTrue(measure.headers.contains("LEVELS"));
        assertEquals(1, measure.testFires);
        assertTrue(measure.texts.contains("YOURS"), "the player's level marked");
        measure.assertInsidePage("pulse cannon");

        Pages.GearPage suite = view.gearPage(DATABANK.entry(EntryPage.SUITE).orElseThrow());
        Measure ladder = new Measure();
        EntryPage.gear(ladder, suite, 1, false);
        assertTrue(ladder.headers.contains("WHAT EACH LEVEL SHOWS"), "the suite's ladder");
        assertTrue(ladder.texts.contains("NONE") && ladder.texts.contains("L3"));
        assertEquals(1, ladder.portraits, "Varga asks for one while the campaign owns none");
        ladder.assertInsidePage("sensor suite");
    }
}
