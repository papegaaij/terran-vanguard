package vanguard.game.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import vanguard.content.ActData;
import vanguard.content.BriefingPage;
import vanguard.content.Content;
import vanguard.content.ContentLoader;

/**
 * Every act intro's speaker has a briefing name plate and role: M11 part B's step B5 found the Act 7 intro's page 3 (the
 * Choir, intercepted) crashing on a missing plate.
 */
class SpeakerPlatesTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    @Test
    void everyActIntroSpeakerHasAPlate() {
        for (ActData act : CONTENT.acts().values()) {
            for (BriefingPage page : act.briefing()) {
                assertFalse(Speaker.plate(page.speaker()).isBlank(), page.speaker());
                assertFalse(Speaker.role(page.speaker()).isBlank(), page.speaker());
            }
        }
    }

    @Test
    void theChoirsPlate() {
        assertEquals("THE CHOIR", Speaker.plate("The Choir"));
        assertEquals("VRELL", Speaker.role("The Choir"));
    }
}
