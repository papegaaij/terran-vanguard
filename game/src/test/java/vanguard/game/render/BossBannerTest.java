package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.sim.SimStep;

/** The boss warning banner's band and its early close (round 41, the 2026-10-19 playtest). */
class BossBannerTest {
    @Test
    void somethingUnderTheBandIsUnderIt() {
        assertTrue(BossBanner.under(BossBanner.BAND_BOTTOM + 1, 0));
        assertTrue(BossBanner.under(BossBanner.BAND_TOP + 20, 32), "a part reaching into the band");
        assertFalse(BossBanner.under(BossBanner.BAND_TOP + 40, 32), "a part above it");
        assertFalse(BossBanner.under(BossBanner.BAND_BOTTOM - 40, 32), "a part below it");
    }

    @Test
    void anEarlyCloseGrowsItOutFromThatTickAndNeverOpensItAgain() {
        long opened = 100;
        long tick = opened + SimStep.ticks(1);
        long since = BossBanner.closedSince(opened, tick);
        double at = (tick - since) * SimStep.SECONDS;
        assertEquals(BossBanner.SECONDS - BossBanner.GROW_SECONDS, at, 1e-9, "it starts growing out now");
        assertTrue(BossBanner.visible(at));
        assertFalse(BossBanner.visible((tick + SimStep.ticks(BossBanner.GROW_SECONDS) - since) * SimStep.SECONDS));

        long closing = opened + SimStep.ticks(BossBanner.SECONDS - 0.1);
        assertEquals(opened, BossBanner.closedSince(opened, closing), "one closing already goes on as it was");
    }
}
