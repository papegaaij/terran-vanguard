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
import vanguard.game.hangar.HangarState;
import vanguard.game.hangar.HangarState.Command;
import vanguard.game.hangar.HangarState.Focus;
import vanguard.game.hangar.HangarState.Outcome;
import vanguard.sim.LevelResult;
import vanguard.sim.ScoringRules;

/**
 * Every mission starts briefing, hangar, level (the user's decision of 2026-10-18): a Continue and a slot's load
 * open the next level's briefing (the act intro first when it is due), a mission select's replay opens the
 * level's briefing and then the replay hangar, which only refits (the user's correction of 2026-10-19): the
 * campaign keeps the refit, nothing is bought, sold, upgraded or repaired, and the flight stays a replay.
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
    void theReplayHangarOnlyRefitsAndRefusesBuyingSellingUpgradingAndRepairing() {
        Campaign campaign = flownWithAPurchase();
        int credits = campaign.credits();
        assertTrue(new HangarState(new Hangar(CATALOGUE, campaign), true).enabled(Command.REPAIR), "armour is missing");
        HangarState replay = new HangarState(new Hangar(CATALOGUE, campaign), true, true);

        assertFalse(replay.enabled(Command.REPAIR), "no repair in the replay hangar");
        assertEquals(HangarState.REFIT_ONLY, replay.message());
        for (LoadoutSlot slot : replay.slots()) {
            replay.pointSlot(slot);
            assertEquals(Optional.empty(), replay.bayRow(), "no bay to buy or sell");
            for (Hangar.Offer offer : replay.rows()) {
                assertTrue(
                        offer.state() == Hangar.State.FITTED || offer.state() == Hangar.State.OWNED,
                        "only owned items are listed: " + offer.item().name());
                offer.choices()
                        .forEach(choice -> assertTrue(
                                HangarState.refit(choice.action()),
                                choice.action() + " of " + offer.item().name()));
            }
        }
        // Every row and choice confirmed, and every sale asked for, leaves the credits alone.
        for (LoadoutSlot slot : replay.slots()) {
            replay.pointSlot(slot);
            for (int row = 0; row < replay.rowCount(); row++) {
                replay.pointRow(row);
                int choices =
                        replay.selected().map(offer -> offer.choices().size()).orElse(0);
                for (int choice = 0; choice < Math.max(1, choices); choice++) {
                    replay.pointChoice(choice);
                    assertTrue(replay.confirm() != Outcome.SELL, "nothing is sold");
                    assertEquals(Outcome.REFUSED, replay.sell());
                    assertEquals(credits, campaign.credits(), "the credits are unchanged");
                }
            }
        }
        // The command bar's keys skip the repair.
        replay.pointCommand(Command.LAUNCH);
        assertEquals(Focus.COMMANDS, replay.focus());
        for (int i = 0; i < 8; i++) {
            replay.right();
            assertTrue(replay.command() != Command.REPAIR);
        }
        assertEquals(credits, campaign.credits());
    }

    @Test
    void aRefitInTheReplayHangarIsKeptAndTheReplayStillEarnsNothing() {
        Campaign campaign = flownWithAPurchase();
        int credits = campaign.credits();
        SaveGame before = campaign.save(Instant.EPOCH);
        HangarState replay = new HangarState(new Hangar(CATALOGUE, campaign), true, true);

        assertTrue(refitSomething(replay), "an owned item can be fitted or unfitted");
        assertEquals(credits, campaign.credits(), "a refit costs nothing");

        Campaign flight = HangarScreen.replayFlight(RULES, campaign, SaveSlots.Slot.AUTOSAVE, 3);

        assertEquals(Optional.of(new Campaign.Replay(SaveSlots.Slot.AUTOSAVE, 3)), flight.replay());
        assertEquals(campaign.loadout(), flight.loadout(), "the replay flies the refitted loadout");
        assertEquals(flight.maxArmour(), flight.armour(), "at full armour");
        assertEquals(Optional.empty(), flight.retriesLeft(), "unlimited retries");

        flight.complete(won(), 30);

        assertEquals(credits, flight.credits(), "a replay earns nothing");
        assertEquals(3, flight.nextLevel(), "a replay's progress stays");
        SaveGame kept = campaign.save(Instant.EPOCH);
        assertEquals(credits, kept.credits());
        assertEquals(5, kept.nextLevel(), "the campaign's progress is unchanged");
        assertFalse(
                before.loadout().equals(kept.loadout()) && before.inventory().equals(kept.inventory()),
                "the refit is the campaign's");
    }

    @Test
    void theCampaignHangarStillBuys() {
        Campaign campaign = flown();
        int credits = campaign.credits();
        HangarState hangar = new HangarState(new Hangar(CATALOGUE, campaign), true);

        assertTrue(buySomething(hangar), "something in the shop is affordable before Level 05");
        assertTrue(campaign.credits() < credits, "the credits are spent");
    }

    /** A campaign that has won Levels 01-04, its next level 05. */
    private static Campaign flown() {
        Campaign flown = Campaign.start(RULES, Difficulty.MEDIUM);
        for (int level = 1; level <= 4; level++) {
            flown.complete(won(), 30);
        }
        return Campaign.load(RULES, flown.save(Instant.EPOCH));
    }

    /** {@link #flown()} with an item bought in its campaign hangar, so it has something to refit. */
    private static Campaign flownWithAPurchase() {
        Campaign campaign = flown();
        assertTrue(buySomething(new HangarState(new Hangar(CATALOGUE, campaign), true)));
        return campaign;
    }

    /** Buys the first affordable item of any slot through the hangar's keys. */
    private static boolean buySomething(HangarState state) {
        return confirmFirst(state, Hangar.Action.BUY);
    }

    /** Makes the first refit (a fit or an unfit) of any slot through the hangar's keys. */
    private static boolean refitSomething(HangarState state) {
        return confirmFirst(state, Hangar.Action.UNFIT) || confirmFirst(state, Hangar.Action.FIT);
    }

    private static boolean confirmFirst(HangarState state, Hangar.Action action) {
        for (LoadoutSlot slot : state.slots()) {
            state.pointSlot(slot);
            List<Hangar.Offer> rows = state.rows();
            for (int row = 0; row < rows.size(); row++) {
                List<Hangar.Choice> choices = rows.get(row).choices();
                for (int choice = 0; choice < choices.size(); choice++) {
                    if (choices.get(choice).action() == action
                            && choices.get(choice).allowed()) {
                        state.pointRow(row);
                        state.pointChoice(choice);
                        return state.confirm() == Outcome.DONE;
                    }
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
