package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.io.File;
import org.junit.jupiter.api.Test;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.game.ui.Fonts;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.WarningEdge;

class WaveBannersTest {
    private static final int LEFT = WarningEdge.LEFT.bit();
    private static final int RIGHT = WarningEdge.RIGHT.bit();
    private static final int REAR = WarningEdge.BOTTOM.bit();
    private static final int TOP = WarningEdge.TOP.bit();
    private static final int BANNER_TICKS = SimStep.ticks(WaveBanners.SECONDS);

    private final WaveBanners banners = new WaveBanners(null, null, false);

    @Test
    void theWarnedEdgeNamesTheBanner() {
        assertEquals("WARNING — HOSTILES FROM THE REAR", WaveBanners.text(REAR));
        assertEquals("WARNING — HOSTILES FROM THE LEFT", WaveBanners.text(LEFT));
        assertEquals("WARNING — HOSTILES FROM THE RIGHT", WaveBanners.text(RIGHT));
        assertEquals("WARNING — HOSTILES FROM THE LEFT AND RIGHT", WaveBanners.text(LEFT | RIGHT));
        assertEquals("WARNING — HOSTILES FROM THE REAR AND LEFT", WaveBanners.text(REAR | LEFT));
        assertEquals("WARNING — HOSTILES ON ALL SIDES", WaveBanners.text(REAR | LEFT | RIGHT));
    }

    /**
     * M7 part G (design/campaign Level 20, Reverse scroll): by travel. The top edge is the rear; a bottom warning that
     * started while the scroll ran reversed is a wave from ahead (the lessons).
     */
    @Test
    void inAReversedSectionTheTopIsTheRearAndTheBottomAhead() {
        assertEquals("WARNING — HOSTILES FROM THE REAR", WaveBanners.text(TOP));
        assertEquals("WARNING — HOSTILES FROM THE REAR AND LEFT", WaveBanners.text(TOP | LEFT));
        assertEquals("WARNING — HOSTILES AHEAD", WaveBanners.text(REAR, true));
        assertEquals("WARNING — HOSTILES AHEAD AND RIGHT", WaveBanners.text(REAR | RIGHT, true));
        assertEquals("WARNING — HOSTILES AHEAD, LEFT AND RIGHT", WaveBanners.text(REAR | LEFT | RIGHT, true));
        assertEquals("WARNING — HOSTILES AHEAD AND BEHIND", WaveBanners.text(REAR | TOP, true));
        assertEquals("WARNING — HOSTILES FROM THE REAR", WaveBanners.text(REAR, false));
        // The bottom's warning names the banner by the travel as it started.
        banners.step(REAR, REAR, 0, true);
        assertEquals("WARNING — HOSTILES AHEAD", banners.shown());
        banners.clear();
        banners.step(REAR, REAR, 0, false);
        assertEquals("WARNING — HOSTILES FROM THE REAR", banners.shown());
        banners.clear();
        banners.step(TOP, TOP, 0, true);
        assertEquals("WARNING — HOSTILES FROM THE REAR", banners.shown());
    }

    @Test
    void everyBannerFitsItsBandInTheBodyFont() {
        BitmapFont.BitmapFontData body = new BitmapFont.BitmapFontData(
                new FileHandle(new File(System.getProperty("vanguard.assetsDir"), Fonts.BODY_FILE)), false);
        // M7 part G: the top edge too, and the bottom named by travel.
        for (int n = 1; n < 32; n++) {
            if ((n & 15) == 0) {
                continue;
            }
            String text = WaveBanners.text(n & 15, n >= 16);
            int width = 0;
            for (int i = 0; i < text.length(); i++) {
                BitmapFont.Glyph glyph = body.getGlyph(text.charAt(i));
                assertNotNull(glyph, "the font has '" + text.charAt(i) + "'");
                width += glyph.xadvance;
            }
            assertTrue(
                    width <= WaveBanners.WIDTH - 2 * WaveBanners.TEXT_INSET,
                    "'" + text + "' is " + width + " px, more than its band");
            assertTrue(body.capHeight <= WaveBanners.HEIGHT - 8, "the capitals fit the band's height");
        }
    }

    @Test
    void theBandSitsInThePlayFieldAboveTheBossBanner() {
        assertTrue(WaveBanners.WIDTH <= PlayField.WIDTH);
        assertTrue(WaveBanners.CENTRE_Y - WaveBanners.HEIGHT / 2 > 300 + 108 / 2, "clear of the boss banner's band");
        assertTrue(WaveBanners.CENTRE_Y + WaveBanners.HEIGHT / 2 < PlayField.HEIGHT - 40, "clear of the boss bar");
    }

    /**
     * 2026-10-08 (the round 32 capture: the rear warning over Lifeline One): with an air escort the
     * band sits above Level 10's shuttle stations, their sway and their sprites included, and stays
     * clear of the boss bar; without one it keeps its place.
     */
    @Test
    void withAnAirEscortTheBandClearsTheShuttleStations() {
        ObjectiveSpec.Escort escort = SimSpecs.level(
                        ContentLoader.fromClasspath(),
                        "act-2-homefront/level-10-evacuation-corridor",
                        Difficulty.MEDIUM)
                .objectives()
                .escort()
                .orElseThrow();
        double highest = 0;
        for (ObjectiveSpec.Station station : escort.air().orElseThrow().stations()) {
            highest = Math.max(
                    highest,
                    station.y()
                            + Math.abs(station.swayY())
                            + escort.ally().size().height() / 2);
        }
        int bottom = WaveBanners.centreY(true) - WaveBanners.HEIGHT / 2;
        assertTrue(bottom > highest + 8, "the band's bottom " + bottom + " over the stations' top " + highest);
        assertTrue(WaveBanners.centreY(true) + WaveBanners.HEIGHT / 2 < PlayField.HEIGHT - 40, "clear of the boss bar");
        assertEquals(WaveBanners.CENTRE_Y, WaveBanners.centreY(false));
    }

    @Test
    void aBannerOpensWithItsEdgeWarningAndShowsForTwoAndAHalfSeconds() {
        banners.step(REAR, REAR, 100);
        assertEquals(WaveBanners.text(REAR), banners.shown());

        banners.step(REAR, 0, 100 + BANNER_TICKS - 1);
        assertEquals(WaveBanners.text(REAR), banners.shown());
        banners.step(REAR, 0, 100 + BANNER_TICKS);
        assertEquals("", banners.shown());
    }

    @Test
    void oneBannerAtATimeTheNextWaitsForIt() {
        banners.step(LEFT, LEFT, 100);
        banners.step(LEFT | REAR, REAR, 130);
        assertEquals(WaveBanners.text(LEFT), banners.shown(), "the rear waits");

        banners.step(REAR, 0, 100 + BANNER_TICKS);

        assertEquals(WaveBanners.text(REAR), banners.shown());
    }

    @Test
    void aWaitingBannerIsDroppedOnceItsWarningIsOver() {
        banners.step(LEFT, LEFT, 100);
        banners.step(LEFT | RIGHT, RIGHT, 110);
        banners.step(LEFT, 0, 120);

        banners.step(0, 0, 100 + BANNER_TICKS);

        assertEquals("", banners.shown());
    }

    @Test
    void edgesStartingTogetherShareOneBanner() {
        banners.step(LEFT | RIGHT, LEFT | RIGHT, 100);

        assertEquals(WaveBanners.text(LEFT | RIGHT), banners.shown());
    }

    @Test
    void aRestartClearsIt() {
        banners.step(REAR, REAR, 100);

        banners.clear();

        assertEquals("", banners.shown());
    }

    @Test
    void itOpensAndFadesOut() {
        assertEquals(0, WaveBanners.grow(0));
        assertEquals(1, WaveBanners.grow(WaveBanners.GROW_SECONDS));
        assertEquals(1, WaveBanners.opacity(1));
        assertTrue(WaveBanners.opacity(WaveBanners.SECONDS - 0.1) < 1);
        assertEquals(0, WaveBanners.opacity(WaveBanners.SECONDS));
    }

    /**
     * Step GFX2 (the round 43 capture: the box was drawn empty for a moment while one warning handed over to the
     * next): the box never shows without its text; a banner that follows another takes the open box over (no fade
     * out, no opening again), and a banner opening from nothing has its text from its first frame.
     */
    @Test
    void theBoxIsNeverEmpty() {
        int handOver = -1;
        String first = null;
        for (long tick = 0; tick < 3 * BANNER_TICKS; tick++) {
            int edges = (tick < 4 * BANNER_TICKS ? LEFT : 0) | (tick >= 30 ? RIGHT : 0);
            int started = (tick == 0 ? LEFT : 0) | (tick == 30 ? RIGHT : 0);
            banners.step(edges, started, tick);
            if (tick == 0) {
                first = banners.shown();
                assertTrue(banners.openness(tick, 0) < 1, "it opens from nothing");
            }
            for (float alpha : new float[] {0, 0.5f}) {
                if (banners.boxOpacity(tick, alpha) > 0) {
                    assertFalse(banners.shown().isEmpty(), "the box at tick " + tick + " has its text");
                }
            }
            if (handOver < 0 && !banners.shown().equals(first)) {
                handOver = (int) tick;
            }
        }
        assertEquals(BANNER_TICKS, handOver, "the right edge's banner follows the left's");
        assertEquals("WARNING — HOSTILES FROM THE LEFT", first);
        // Flown again: through the hand-over the box stays fully open and opaque.
        banners.clear();
        for (long tick = 0; tick < handOver + 20; tick++) {
            banners.step(LEFT | (tick >= 30 ? RIGHT : 0), (tick == 0 ? LEFT : 0) | (tick == 30 ? RIGHT : 0), tick);
            if (tick >= handOver - SimStep.ticks(WaveBanners.FADE_SECONDS)) {
                assertEquals(1, banners.boxOpacity(tick, 0.5f), 1e-6, "no fade out at tick " + tick);
                assertEquals(1, banners.openness(tick, 0.5f), 1e-6, "no opening again at tick " + tick);
            }
        }
        assertEquals("WARNING — HOSTILES FROM THE RIGHT", banners.shown());
        // A banner with nothing after it still fades out.
        banners.clear();
        banners.step(LEFT, LEFT, 0);
        assertTrue(banners.boxOpacity(BANNER_TICKS - 2, 0) < 1, "the last banner fades out");
    }
}
