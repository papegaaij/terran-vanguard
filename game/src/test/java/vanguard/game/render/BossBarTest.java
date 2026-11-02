package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.io.File;
import org.junit.jupiter.api.Test;
import vanguard.game.ui.Fonts;
import vanguard.sim.PlayField;

class BossBarTest {
    @Test
    void thePlainBarIsCentredBelowTheTopEdge() {
        BossBar.Layout plain = BossBar.layout(BossBar.WIDTH, 0, 0, 0, 0, 0);

        assertEquals(40, plain.fillX());
        assertEquals(PlayField.HEIGHT - BossBar.TOP - BossBar.PLAIN_HEIGHT, plain.fillY());
        assertEquals(BossBar.PLAIN_HEIGHT, plain.fillHeight());
        assertEquals(0, plain.plateWidth());
    }

    @Test
    void thePlateWrapsTheFillWhichFillsItsContentBox() {
        // A 20 px plate with 6 px padding at the sides, 5 at the top and 7 at the bottom.
        BossBar.Layout mid = BossBar.layout(BossBar.MID_WIDTH, 6, 6, 5, 7, 20);

        assertEquals(120, mid.fillX());
        assertEquals(BossBar.MID_WIDTH, mid.fillWidth());
        assertEquals(8, mid.fillHeight());
        assertEquals(PlayField.HEIGHT - BossBar.TOP, mid.fillY() + mid.fillHeight(), "the fill's top stays");
        assertEquals(114, mid.plateX());
        assertEquals(BossBar.MID_WIDTH + 12, mid.plateWidth());
        assertEquals(mid.fillY() - 7, mid.plateY());
        assertEquals(20, mid.plateHeight());
    }

    /**
     * M10 part H (design/ui/hud, the escape bar): the seconds left rounded up to whole seconds ({@code 12s}), the gold
     * payout fill over the time bar's lower half, {@code ESCAPED} flashing at 4 Hz; the plate's name, {@code MAX} and the
     * seconds (or {@code ESCAPED}) side by side on the act boss's bar in the bar's font, none overlapping.
     */
    @Test
    void theEscapeBarsTextsAndFills() {
        assertEquals("14s", BossBar.secondsText(14));
        assertEquals("12s", BossBar.secondsText(11.2), "whole seconds, rounded up");
        assertEquals("0s", BossBar.secondsText(0));
        assertEquals("0s", BossBar.secondsText(-1));
        assertEquals(4, BossBar.payoutHeight(8));
        assertEquals(4, BossBar.payoutHeight(7), "the lower half, rounded up");
        assertTrue(BossBar.escapedLit(0));
        assertFalse(BossBar.escapedLit(0.13), "off for an eighth of a second");
        assertTrue(BossBar.escapedLit(0.26));
        BitmapFont.BitmapFontData body = new BitmapFont.BitmapFontData(
                new FileHandle(new File(System.getProperty("vanguard.assetsDir"), Fonts.BODY_FILE)), false);
        int name = width(body, AscendantLooks.ESCAPE_NAME);
        int max = width(body, BossBar.MAX);
        int seconds = width(body, "14s");
        int escaped = width(body, BossBar.ESCAPED);
        float w = BossBar.WIDTH;
        assertTrue(name < (w - max) / 2, "the name left of MAX");
        assertTrue((w + max) / 2 < w - seconds, "MAX left of the seconds");
        assertTrue(name + 8 < w - escaped, "ESCAPED clear of the name");
        assertEquals("VORNE", AscendantLooks.ESCAPE_NAME);
    }

    private static int width(BitmapFont.BitmapFontData font, String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            BitmapFont.Glyph glyph = font.getGlyph(text.charAt(i));
            assertNotNull(glyph, "the font has '" + text.charAt(i) + "'");
            width += glyph.xadvance;
        }
        return width;
    }
}
