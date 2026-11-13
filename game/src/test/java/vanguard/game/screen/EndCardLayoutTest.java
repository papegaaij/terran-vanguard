package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.campaign.CampaignRoute;
import vanguard.content.campaign.HighScores;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Fonts;
import vanguard.game.ui.Words;

/**
 * M11 part A, step A3b (design/ui/debrief, The campaign end; design/systems/scoring, The campaign end): the end card's
 * campaign form says what the doc's sketch says and fits the release's card (the heading at twice its size, the lines on
 * the screen, the rows in the panel, read in the UI kit's fonts without a GL context); the ending pages' NEXT panel
 * wraps; a finished campaign's high score reads {@code WON} where the furthest mission stands, in its room; the route
 * after the last act's last level runs through the ending pages on track 27 to the card, never after a replay, and the
 * built content keeps the release's end.
 */
class EndCardLayoutTest {
    private static final String ASSETS = System.getProperty("vanguard.assetsDir", "../assets");
    private static final BitmapFont.BitmapFontData HEADING = font(Fonts.HEADING_FILE);
    private static final BitmapFont.BitmapFontData BODY = font(Fonts.BODY_FILE);
    private static final BitmapFont.BitmapFontData LABEL = font(Fonts.LABEL_FILE);

    private static BitmapFont.BitmapFontData font(String file) {
        return new BitmapFont.BitmapFontData(new FileHandle(new File(ASSETS, file)), false);
    }

    @Test
    void theCardSaysWhatTheSketchSays() {
        assertEquals("CAMPAIGN COMPLETE", CampaignEnd.TITLE);
        assertEquals("MEDIUM - ALL 50 MISSIONS FLOWN", CampaignEnd.flown(Difficulty.MEDIUM, 50));
        assertEquals("38 OF 54", CampaignEnd.found(38, 54));
        assertEquals("THE GATE IS DARK.", CampaignEnd.DARK);
        assertEquals("EVERY MISSION IS OPEN FOR REPLAY IN MISSIONS.", CampaignEnd.REPLAY);
        int secrets = CampaignEnd.secrets(CampaignEndFixture.BUILT);
        assertTrue(secrets >= 42, "a secret a built level at least: " + secrets);
    }

    @Test
    void theCampaignFormFitsTheReleasesCard() {
        assertTrue(2 * width(HEADING, CampaignEnd.TITLE) + 40 <= PixelScreen.WIDTH, "the heading at twice its size");
        assertTrue(width(BODY, CampaignEnd.flown(Difficulty.MEDIUM, 50)) + 40 <= PixelScreen.WIDTH);
        assertTrue(width(HEADING, CampaignEnd.DARK) + 40 <= PixelScreen.WIDTH);
        assertTrue(width(BODY, CampaignEnd.REPLAY) + 40 <= PixelScreen.WIDTH);
        int room = ReleaseEndScreen.PANEL_WIDTH - 36;
        assertTrue(
                width(LABEL, "SECRETS FOUND") + 12 + width(BODY, CampaignEnd.found(54, 54)) <= room,
                "the secrets' row in the panel");
        assertTrue(width(LABEL, "A+ GRADES") + 12 + width(BODY, "50 OF 50") <= room);
    }

    @Test
    void theEndingPagesNextPanelWraps() {
        List<String> lines = Words.wrap(BriefingScreen.ENDING_NEXT, 40);
        assertTrue(lines.size() >= 2 && lines.size() <= 3, lines.toString());
        lines.forEach(line -> assertTrue(line.length() <= 40, line));
    }

    @Test
    void aFinishedCampaignsScoreReadsWonInGreenInItsRoom() {
        HighScores.Entry won = new HighScores.Entry("ACE", 4_812_300, 50, LocalDate.EPOCH, true);
        HighScores.Entry over = new HighScores.Entry("ROOK", 3_000_000, 50, LocalDate.EPOCH);
        List<ScoreTable.Row> rows = ScoreTable.rows(List.of(won, over), OptionalInt.of(0));
        assertEquals("WON", ScoreTable.level(rows.get(0)));
        assertEquals("M50", ScoreTable.level(rows.get(1)), "a game over in Level 50 reads M50");
        List<ScoreTable.Row> entering = ScoreTable.rows(List.of(over), OptionalInt.of(0), "NEW", 5_000_000, 50, true);
        assertTrue(entering.get(0).entering() && entering.get(0).complete());
        assertFalse(entering.get(1).complete());
        assertTrue(width(BODY, ScoreTable.WON) <= width(BODY, "M50") + 2, "WON fits the level column");
    }

    @Test
    void theLastActsLastLevelRunsThroughTheEndingPagesToTheCard() {
        var campaign = CampaignEndFixture.wonThrough(CampaignEndFixture.FINAL_ACT_6, 42);
        assertTrue(CampaignRoute.campaignEnd(CampaignEndFixture.FINAL_ACT_6, campaign));
        var ending = (CampaignRoute.Step.Outro) CampaignRoute.afterLevel(CampaignEndFixture.FINAL_ACT_6, campaign);
        assertEquals(Optional.of("ending"), ending.script().music(), "track 27");
        Optional<String> image = CampaignEnd.lastImage(ending.script().pages());
        image.ifPresent(path -> assertTrue(new File(ASSETS, path).exists(), path));

        var built = CampaignEndFixture.wonThrough(CampaignEndFixture.BUILT, 42);
        assertFalse(CampaignRoute.campaignEnd(CampaignEndFixture.BUILT, built), "the built content's release end");
    }

    /** The advance of the text's glyphs with their kerning, as libGDX lays out one line. */
    private static int width(BitmapFont.BitmapFontData font, String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            BitmapFont.Glyph glyph = font.getGlyph(text.charAt(i));
            assertNotNull(glyph, "the font has '" + text.charAt(i) + "' (" + text + ")");
            width += glyph.xadvance;
            if (i + 1 < text.length()) {
                width += glyph.getKerning(text.charAt(i + 1));
            }
        }
        return width;
    }
}
