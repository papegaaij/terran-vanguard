package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.CampaignRoute;
import vanguard.content.campaign.SaveGame;

/**
 * M11 part A, step A3b (design/ui/main-menu, After the campaign; design/systems/saves, The completed campaign): Continue
 * (and Load game on its slot) with a completed campaign opens the mission select, never a briefing and the hangar; any
 * other save goes on with its next level's briefing as ever; the completed slot reads {@code CAMPAIGN COMPLETE}.
 */
class MainMenuContinueTest {
    @Test
    void aCompletedCampaignContinuesToTheMissionSelect() {
        SaveGame complete =
                CampaignEndFixture.wonThrough(CampaignEndFixture.BUILT, 50).save(Instant.EPOCH);
        assertTrue(MainMenuScreen.continuesToMissions(CampaignEndFixture.BUILT, complete));
        SaveGame fixture = CampaignEndFixture.wonThrough(CampaignEndFixture.FINAL_ACT_6, 42)
                .save(Instant.EPOCH);
        assertTrue(MainMenuScreen.continuesToMissions(CampaignEndFixture.FINAL_ACT_6, fixture));
        assertEquals("CAMPAIGN COMPLETE", CampaignEnd.COMPLETE);
    }

    @Test
    void anyOtherSaveContinuesToItsBriefing() {
        var campaign = CampaignEndFixture.wonThrough(CampaignEndFixture.BUILT, 12);
        assertFalse(MainMenuScreen.continuesToMissions(CampaignEndFixture.BUILT, campaign.save(Instant.EPOCH)));
        assertTrue(CampaignRoute.onContinue(CampaignEndFixture.BUILT, campaign) instanceof CampaignRoute.Step.Briefing);
        // The v0.4 save at the Level 43 hangar: Act 6 names Act 7, so it is no completed campaign.
        var release = CampaignEndFixture.wonThrough(CampaignEndFixture.BUILT, 42);
        assertFalse(MainMenuScreen.continuesToMissions(CampaignEndFixture.BUILT, release.save(Instant.EPOCH)));
    }
}
