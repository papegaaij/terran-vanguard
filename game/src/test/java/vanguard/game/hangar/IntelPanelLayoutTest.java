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
    }

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
     * step K10): one red tick the strip's depth, inside the strip and over the waves; below L3 there is no
     * strip, so no mark.
     */
    @Test
    void level14sStripMarksTheBossAtItsArrival() {
        String level = CONTENT.levelKey(14).orElseThrow();
        Intel intel = Intel.of(CONTENT, level, 3);
        Measure measure = new Measure();
        IntelPanel.layout(measure, intel, CONTENT.level(level).briefing().teaser(), level);
        Box strip = measure.parts.stream()
                .filter(box -> box.what().equals("wave strip"))
                .findFirst()
                .orElseThrow();
        List<Fill> boss = measure.fills.stream()
                .filter(fill -> fill.colour().equals(Glass.ALERT))
                .toList();
        assertEquals(1, boss.size(), "one boss mark: " + boss);
        Fill mark = boss.getFirst();
        float x = (float) (strip.left() + 2 + (strip.right() - strip.left() - 4) * 108 / intel.seconds());
        assertEquals(x, mark.x(), 0.01f, "at 108 s of " + intel.seconds());
        assertTrue(
                mark.y() > strip.top() && mark.y() + mark.height() < strip.bottom() && mark.height() >= 8,
                "the strip's depth, inside it: " + mark + " in " + strip);
        assertEquals(boss.getFirst(), measure.fills.getLast(), "drawn over the waves");

        Measure l2 = new Measure();
        IntelPanel.layout(
                l2, Intel.of(CONTENT, level, 2), CONTENT.level(level).briefing().teaser(), level);
        assertTrue(l2.fills.stream().noneMatch(fill -> fill.colour().equals(Glass.ALERT)), "no strip below L3");
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

    private static Measure assertFits(Intel intel, BriefingPage teaser, String level, String where) {
        Measure measure = new Measure();
        IntelPanel.layout(measure, intel, teaser, level);
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
