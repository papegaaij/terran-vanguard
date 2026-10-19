package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRoute;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.DebugFit;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveGame;
import vanguard.content.campaign.SaveSlots;
import vanguard.sim.LevelResult;
import vanguard.sim.ScoringRules;

/**
 * Every mission starts briefing, hangar, level (the user's decision of 2026-10-18): a Continue and a slot's load
 * open the next level's briefing (the act intro first when it is due), a mission select's replay opens the
 * level's briefing and then the replay hangar, whose purchases the campaign keeps while the flight stays a replay.
 */
class MissionFlowTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);

    @Test
    void continueOpensTheNextLevelsBriefingAndTheHangarWithoutAnAutosave() {
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, 12);

        CampaignRoute.Step step = CampaignRoute.onContinue(CONTENT, campaign);

        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, step);
        assertEquals(12, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
        assertFalse(HangarScreen.continueAutosaves(step), "a save was just loaded: the hangar does not autosave");
    }

    @Test
    void aLoadedSaveAtAnActsFirstLevelGetsTheMissionBriefingWithoutTheActIntroOnceItWasShown() {
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, 8);
        campaign.markActIntroShown(2);
        Campaign loaded = Campaign.load(RULES, campaign.save(Instant.EPOCH));

        CampaignRoute.Step step = CampaignRoute.onContinue(CONTENT, loaded);

        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, step);
        assertEquals(8, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
        assertFalse(HangarScreen.continueAutosaves(step));
    }

    @Test
    void anActIntroThatIsDueComesFirstAndTheHangarAfterItAutosaves() {
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, 15);
        assertFalse(campaign.actIntroShown(3), "the Level 15 start has not seen Act 3's intro");

        CampaignRoute.Step step = CampaignRoute.onContinue(CONTENT, campaign);

        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, step);
        assertTrue(briefing.script().titleCard().isPresent(), "the act intro first");
        assertEquals(15, briefing.script().mission(), "then the level's briefing");
        assertTrue(campaign.actIntroShown(3));
        assertTrue(HangarScreen.continueAutosaves(step), "the hangar's autosave keeps the flag");
    }

    @Test
    void aReplayOpensTheLevelsBriefingWithoutTheActIntro() {
        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeReplay(CONTENT, 8));

        assertEquals(8, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
    }

    @Test
    void aPurchaseInTheReplayHangarIsKeptAndTheReplayStillEarnsNothing() {
        Campaign flown = Campaign.start(RULES, Difficulty.MEDIUM);
        for (int level = 1; level <= 4; level++) {
            flown.complete(won(), 30);
        }
        SaveGame save = flown.save(Instant.EPOCH);
        Campaign campaign = Campaign.load(RULES, save);
        int before = campaign.credits();
        Hangar hangar = new Hangar(CATALOGUE, campaign);

        assertTrue(buySomething(hangar), "something in the shop is affordable before Level 05");
        int after = campaign.credits();
        assertTrue(after < before, "the credits are spent");

        Campaign flight = HangarScreen.replayFlight(RULES, campaign, SaveSlots.Slot.AUTOSAVE, 3);

        assertEquals(Optional.of(new Campaign.Replay(SaveSlots.Slot.AUTOSAVE, 3)), flight.replay());
        assertEquals(campaign.loadout(), flight.loadout(), "the replay flies the hangar's loadout");
        assertEquals(after, flight.credits());
        assertEquals(flight.maxArmour(), flight.armour(), "at full armour");
        assertEquals(Optional.empty(), flight.retriesLeft(), "unlimited retries");

        flight.complete(won(), 30);

        assertEquals(after, flight.credits(), "a replay earns nothing");
        assertEquals(3, flight.nextLevel(), "a replay's progress stays");
        SaveGame kept = campaign.save(Instant.EPOCH);
        assertEquals(after, kept.credits(), "the purchase stays spent in the campaign");
        assertEquals(5, kept.nextLevel(), "the campaign's progress is unchanged");
        assertFalse(
                save.loadout().equals(kept.loadout()) && save.inventory().equals(kept.inventory()),
                "the bought item is the campaign's");
    }

    /** Buys the first affordable item of any slot. */
    private static boolean buySomething(Hangar hangar) {
        for (LoadoutSlot slot : LoadoutSlot.values()) {
            for (Hangar.Offer offer : hangar.shop(slot)) {
                if (offer.choice(Hangar.Action.BUY)
                        .filter(Hangar.Choice::allowed)
                        .isPresent()) {
                    return hangar.apply(slot, offer, Hangar.Action.BUY);
                }
            }
        }
        return false;
    }

    private static LevelResult won() {
        return new LevelResult(
                80,
                95,
                10,
                1,
                1,
                30,
                2,
                true,
                new LevelResult.Credits(300, 50, 0, 0, 50),
                List.of(),
                10_000,
                new LevelResult.Rating(
                        new LevelResult.Rating.Part(34, 40),
                        new LevelResult.Rating.Part(25, 30),
                        new LevelResult.Rating.Part(15, 15),
                        new LevelResult.Rating.Part(6, 15),
                        new ScoringRules.Grade("A+", 85, 0.3)),
                new ScoringRules.Grade("A", 0, 0),
                80);
    }
}
