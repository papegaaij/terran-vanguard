package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.sim.Sortie;

/**
 * M7 part F: Level 19's secondary tracker (design/campaign Level 19, Secondary): "Stop 12 of the 14 grey tanks" reads
 * {@code GREY TANKS n / 12}, the tanks destroyed of those it needs (2 of the 14 may get away), as Level 03's {@code
 * n / all} without an allowance; DONE once met, FAILED at the third tank away (the simulation's judgement).
 */
class Level19HudTest {
    @Test
    void theCountIsOfTheTanksItNeeds() {
        assertEquals("3 / 12", MissionPanel.escapesCount(3, 14, 2));
        assertEquals("0 / 14", MissionPanel.escapesCount(0, 14, 0), "Level 03's: every one");
        assertEquals("0 / 0", MissionPanel.escapesCount(0, 1, 2), "never below none");
    }

    @Test
    void level19sSecondaryIsTwelveOfTheFourteenGreyTanks() {
        Sortie sortie = FoundryFixture.sortie();
        assertTrue(sortie.secondaryByEscapes(), "an escapes objective");
        assertEquals(2, sortie.script().objectives().secondary().escapesAllowed());
        assertEquals(14, sortie.escapesTotal(), "14 grey tanks at medium");
        assertEquals("GREY TANKS", sortie.script().objectives().secondary().label(), "the tracker's label");
        assertEquals(
                "0 / 12",
                MissionPanel.escapesCount(
                        sortie.escapesDestroyed(),
                        sortie.escapesTotal(),
                        sortie.script().objectives().secondary().escapesAllowed()));
    }
}
