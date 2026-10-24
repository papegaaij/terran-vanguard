package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;

/**
 * M8 part F (design/campaign Level 26, The Choir): the Choir's static lead: the portrait's interference swelling over
 * the 2 s before its line (t=97-99) and its sound starting at 97; Level 14's Choir has none.
 */
class StaticLeadsTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    @Test
    void theChoirsStaticSwellsOverTheTwoSecondsBeforeItsLine() {
        StaticLeads leads = new StaticLeads(
                CONTENT.level(CONTENT.levelKey(26).orElseThrow()).radio());
        assertTrue(leads.any());
        assertEquals(0, leads.strength(96.9));
        assertEquals(0, leads.strength(97), 1e-6, "from nothing");
        assertEquals(0.5, leads.strength(98), 1e-6, "half way");
        assertTrue(leads.strength(98.9) > 0.95, "nearly full as the line opens");
        assertEquals(0, leads.strength(99), "the transmission's own burst takes over");
        assertTrue(leads.started(96.99, 97.01), "its sound at t=97");
        assertFalse(leads.started(97.01, 98));
        assertFalse(
                new StaticLeads(CONTENT.level(CONTENT.levelKey(14).orElseThrow())
                                .radio())
                        .any(),
                "Level 14's none");
    }
}
