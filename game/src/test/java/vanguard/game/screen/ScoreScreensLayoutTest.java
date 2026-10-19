package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.HighScores;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Box;
import vanguard.game.ui.NameEntry;
import vanguard.game.ui.Words;

/**
 * The high scores' screens (M6 part B): the table with its letter grid fits the game over screen's
 * and the release end's panels, the keys lie in two rows without overlapping, the SCORES tabs and
 * the main menu's eight items stay clear of the key hints, and the transmission fits its panel.
 */
class ScoreScreensLayoutTest {
    /** The key hints' plate starts here. */
    private static final int HINTS_TOP = PixelScreen.HEIGHT - 25;

    private static void gridInside(String screen, float x, float y, float width, Box panel) {
        assertTrue(ScoreTable.GRID_WIDTH <= width, screen + ": the grid fits the table's width");
        for (int i = 0; i < NameEntry.KEYS.size(); i++) {
            Box key = ScoreTable.key(x, y, width, i);
            assertTrue(
                    key.x() >= panel.x() + 4 && key.x() + key.width() <= panel.x() + panel.width() - 4,
                    screen + " key " + i);
            assertTrue(
                    key.y() + key.height() <= panel.y() + panel.height() - 4, screen + " key " + i + " in the panel");
            if (i % NameEntry.COLUMNS > 0) {
                Box left = ScoreTable.key(x, y, width, i - 1);
                assertTrue(left.x() + left.width() < key.x(), screen + ": keys " + (i - 1) + " and " + i + " apart");
                assertEquals(left.y(), key.y());
            }
        }
        Box last = ScoreTable.key(x, y, width, NameEntry.KEYS.size() - 1);
        Box above = ScoreTable.key(x, y, width, NameEntry.COLUMNS - 1);
        assertTrue(above.y() + above.height() < last.y(), screen + ": the rows apart");
        assertTrue(y + ScoreTable.HEIGHT <= ScoreTable.key(x, y, width, 0).y(), screen + ": the grid under the table");
        assertTrue(panel.y() + panel.height() < HINTS_TOP, screen + ": the panel above the hints");
        assertTrue(panel.x() >= 0 && panel.x() + panel.width() <= PixelScreen.WIDTH, screen + ": on the screen");
    }

    @Test
    void theGameOverTableAndGridFitTheRightPanel() {
        gridInside(
                "game over",
                GameOverScreen.TABLE_X,
                GameOverScreen.TABLE_Y,
                GameOverScreen.TABLE_WIDTH,
                new Box(
                        GameOverScreen.RIGHT_X,
                        GameOverScreen.PANEL_Y,
                        GameOverScreen.RIGHT_WIDTH,
                        GameOverScreen.PANEL_HEIGHT));
        assertTrue(GameOverScreen.LEFT_X + GameOverScreen.LEFT_WIDTH < GameOverScreen.RIGHT_X, "the panels apart");
    }

    @Test
    void theReleaseEndsTableAndGridFitItsPanel() {
        gridInside(
                "release end",
                ReleaseEndScreen.TABLE_X,
                ReleaseEndScreen.TABLE_Y,
                ReleaseEndScreen.TABLE_WIDTH,
                new Box(
                        ReleaseEndScreen.ENTRY_X,
                        ReleaseEndScreen.ENTRY_Y,
                        ReleaseEndScreen.ENTRY_WIDTH,
                        ReleaseEndScreen.ENTRY_HEIGHT));
        assertTrue(
                ReleaseEndScreen.PANEL_Y + ReleaseEndScreen.PANEL_HEIGHT < 356 - 8,
                "the card's panel above TO BE CONTINUED");
    }

    @Test
    void theTransmissionFitsAboveTheGameOverNote() {
        String line = ContentLoader.fromClasspath().retry().gameOver().line();
        int lines = Words.wrap(line, (GameOverScreen.LEFT_WIDTH - 2 * GameOverScreen.PADDING) / 8)
                .size();
        int bottom = GameOverScreen.PANEL_Y + 36 + 6 * 24 + 6 + 20 + lines * 14 + 2 + 12;
        assertTrue(lines <= 4, "a short line: " + lines);
        assertTrue(bottom < GameOverScreen.PANEL_Y + GameOverScreen.PANEL_HEIGHT - 52, "above the note");
    }

    @Test
    void theScoresTabsAndTableStayOnScreen() {
        for (int i = 0; i + 1 < Difficulty.values().length; i++) {
            Box tab = ScoresScreen.tab(i);
            Box next = ScoresScreen.tab(i + 1);
            assertTrue(tab.x() + tab.width() < next.x());
            assertTrue(tab.y() + tab.height() < ScoresScreen.PANEL_Y);
        }
        assertTrue(ScoresScreen.PANEL_Y + ScoresScreen.PANEL_HEIGHT + 14 + 8 < HINTS_TOP);
    }

    @Test
    void theMainMenusEightItemsStayAboveTheHints() {
        Box last = MainMenuScreen.row(7);
        assertTrue(last.y() + last.height() < HINTS_TOP - 8, "SCORES makes eight items with Continue");
        assertTrue(MainMenuScreen.row(0).y() > 172, "below the logo");
        assertEquals("v0.1", MainMenuScreen.shortVersion("0.1.0"));
        assertEquals("v1.2", MainMenuScreen.shortVersion("1.2.3-SNAPSHOT"));
        assertEquals("dev", MainMenuScreen.shortVersion("dev"));
    }

    @Test
    void theTableShowsTheNewRowInItsPlace() {
        LocalDate day = LocalDate.of(2026, 10, 12);
        HighScores scores = HighScores.empty();
        for (int i = 0; i < HighScores.SIZE; i++) {
            scores = scores.add(Difficulty.HARD, new HighScores.Entry("PILOT" + i, 1000 - 100L * i, 4, day));
        }
        List<HighScores.Entry> table = scores.table(Difficulty.HARD);
        List<ScoreTable.Row> rows = ScoreTable.rows(table, scores.rank(Difficulty.HARD, 750, day), "LAN", 750, 12);
        assertEquals(HighScores.SIZE, rows.size(), "the last one drops out");
        assertEquals(new ScoreTable.Row("LAN", 750, 12, true), rows.get(3));
        assertEquals("PILOT8", rows.getLast().name());
        assertFalse(ScoreTable.rows(table, OptionalInt.empty()).stream().anyMatch(ScoreTable.Row::entering));
        assertEquals("M12", ScoreTable.level(12));
    }
}
