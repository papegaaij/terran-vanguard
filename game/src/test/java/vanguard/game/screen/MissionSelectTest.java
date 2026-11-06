package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Missions;
import vanguard.content.campaign.SaveGame;

/**
 * M11 part A, step A3b (design/ui/mission-select, After the campaign): a completed campaign's mission select opens every
 * mission with every act's name, its header reads {@code REPLAY - MEDIUM - CAMPAIGN COMPLETE}, and its cursor starts on
 * the last mission flown; a campaign still under way keeps {@code REPLAY - MEDIUM} and its locked missions. The
 * completed save is {@code nextLevel} 51, or one past a fixture act set's last level.
 */
class MissionSelectTest {
    @Test
    void aCompletedCampaignOpensEveryMissionUnderItsHeader() {
        SaveGame save = CampaignEndFixture.wonThrough(CampaignEndFixture.FINAL_ACT_6, 42)
                .save(Instant.EPOCH);
        assertTrue(CampaignEnd.completed(CampaignEndFixture.FINAL_ACT_6, save));
        assertEquals(
                "REPLAY - MEDIUM - CAMPAIGN COMPLETE",
                CampaignEnd.missionsHeader(CampaignEndFixture.FINAL_ACT_6, save));
        List<Missions.Act> acts = Missions.of(CampaignEndFixture.FINAL_ACT_6, save);
        for (Missions.Act act : acts) {
            assertTrue(act.name().isPresent(), act.label() + "'s name shows");
            act.missions().forEach(mission -> assertTrue(mission.open(), act.label() + " " + mission));
        }
        Missions.Mission last = acts.getLast().missions().getLast();
        assertTrue(last.open(), "the cursor's start: the last mission flown");
    }

    @Test
    void theSaveOfTheCampaignsLastLevelIsCompleteOnEveryBuild() {
        SaveGame complete =
                CampaignEndFixture.wonThrough(CampaignEndFixture.BUILT, 50).save(Instant.EPOCH);
        assertEquals(SaveGame.COMPLETED, complete.nextLevel());
        assertTrue(CampaignEnd.completed(CampaignEndFixture.BUILT, complete));
        assertTrue(
                CampaignEnd.missionsHeader(CampaignEndFixture.BUILT, complete).endsWith(CampaignEnd.COMPLETE));
    }

    @Test
    void aCampaignUnderWayKeepsItsHeaderAndItsLocks() {
        SaveGame save =
                CampaignEndFixture.wonThrough(CampaignEndFixture.BUILT, 42).save(Instant.EPOCH);
        assertFalse(CampaignEnd.completed(CampaignEndFixture.BUILT, save), "Act 6 names Act 7: the release's end");
        assertEquals("REPLAY - MEDIUM", CampaignEnd.missionsHeader(CampaignEndFixture.BUILT, save));
        SaveGame early = CampaignEndFixture.wonThrough(CampaignEndFixture.FINAL_ACT_6, 20)
                .save(Instant.EPOCH);
        assertFalse(CampaignEnd.completed(CampaignEndFixture.FINAL_ACT_6, early));
        assertTrue(Missions.of(CampaignEndFixture.FINAL_ACT_6, early).stream()
                .flatMap(act -> act.missions().stream())
                .anyMatch(mission -> !mission.open()));
    }
}
