package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import vanguard.content.AlliesData;
import vanguard.content.ContentLoader;

/**
 * The debrief's escort row (design/ui/debrief): a convoy counts its units by the ally's name ("CRAWLERS HOME 4 / 5"),
 * a hull escort its sections (step FIX1, Level 31's mining tug: "SECTIONS HOME 3 / 3", not "TUGS").
 */
class DebriefEscortLabelTest {
    private static final AlliesData ALLIES = ContentLoader.fromClasspath().allies();

    @Test
    void aConvoyCountsItsUnitsAndAHullItsSections() {
        assertEquals("CRAWLERS", DebriefScreen.alliesLabel("civilian-crawler", ALLIES));
        assertEquals("SHUTTLES", DebriefScreen.alliesLabel("evacuation-shuttle", ALLIES));
        assertEquals("SECTIONS", DebriefScreen.alliesLabel("mining-tug", ALLIES));
    }
}
