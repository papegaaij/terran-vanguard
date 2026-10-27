package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Escort;
import vanguard.content.campaign.Fitted;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Action;
import vanguard.content.campaign.Hangar.State;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveFormat;
import vanguard.content.campaign.SaveGame;
import vanguard.content.campaign.SaveSlots;
import vanguard.game.hangar.HangarState.BulwarkChoice;
import vanguard.game.hangar.HangarState.Command;
import vanguard.game.hangar.HangarState.Focus;
import vanguard.game.hangar.HangarState.Outcome;
import vanguard.game.hangar.HangarState.RepairLine;
import vanguard.game.ui.Words;
import vanguard.sim.Armament;

/**
 * M8 part A (step A5): the hangar for Act 4's gear and escorts: the new items in the live shop with their names, the
 * escort's Bulwark row (buy, fit, unfit, sell, undo), Rook missing in action with the loaner, the Bulwark's repair
 * line, the hydro-kit's notice, the intel's {@code SPECIAL LIMITED} and the Depth Charges' test range.
 */
class ActFourHangarTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);
    private static final String GUN = RULES.escort().starterGun();

    /** A campaign before {@code level} with Rook hired and, if {@code bulwark}, a Bulwark owned at that armour. */
    private static Campaign campaign(int level, int credits, Optional<Double> bulwark, boolean bulwarkFlies) {
        return campaign(level, credits, bulwark, bulwarkFlies, RULES.escort().maxArmour(), List.of());
    }

    /** A campaign as above, with Rook at {@code rookArmour} and those story flags. */
    private static Campaign campaign(
            int level,
            int credits,
            Optional<Double> bulwark,
            boolean bulwarkFlies,
            double rookArmour,
            List<String> storyFlags) {
        return Campaign.load(
                RULES,
                new SaveGame(
                        SaveFormat.VERSION,
                        Instant.EPOCH,
                        0,
                        Difficulty.MEDIUM,
                        level,
                        credits,
                        0,
                        RULES.starterLoadout(),
                        Map.of(),
                        List.of(),
                        Map.of(),
                        RULES.starterArmour(),
                        new SaveGame.EscortSlot(
                                true,
                                "left",
                                Optional.of(GUN),
                                List.of(new Fitted(GUN, 1)),
                                rookArmour,
                                bulwarkFlies ? "bulwark" : "rook",
                                bulwark.map(armour -> new SaveGame.BulwarkSlot(true, armour))
                                        .orElse(SaveGame.BulwarkSlot.NOT_OWNED)),
                        Optional.empty(),
                        Map.of(),
                        List.of(),
                        storyFlags,
                        new SaveGame.Stats(0, 0)));
    }

    private static HangarState atEscort(Campaign campaign) {
        HangarState state = new HangarState(new Hangar(CATALOGUE, campaign), true);
        while (state.slot() != LoadoutSlot.ESCORT) {
            assertEquals(Outcome.MOVED, state.nextSlot());
        }
        return state;
    }

    /** Down to the Bulwark row, after Rook's guns. */
    private static HangarState atBulwark(Campaign campaign) {
        HangarState state = atEscort(campaign);
        while (!state.bulwarkSelected()) {
            assertEquals(Outcome.MOVED, state.down());
        }
        return state;
    }

    private static List<Action> actions(HangarState state) {
        return state.bulwarkChoices().stream().map(BulwarkChoice::action).toList();
    }

    @Test
    void theActFourItemsAreInTheLiveShopWithTheirNames() {
        assertEquals(java.util.Set.of(), Catalogue.AWAITING_ICONS);
        assertTrue(CATALOGUE.items(ItemKind.FRONT).stream()
                .anyMatch(item -> item.id().equals("harpoon-torpedoes")));
        assertTrue(CATALOGUE.items(ItemKind.REAR).stream()
                .anyMatch(item -> item.id().equals("depth-charges")));
        for (String special : List.of("Sonar Pulse", "Decoy Flares")) {
            assertTrue(
                    CATALOGUE.items(ItemKind.SPECIAL).stream()
                            .anyMatch(item -> item.id().equals(special)),
                    special);
        }
        assertEquals("HARPOON", Names.callout("Harpoon Torpedoes"));
        assertEquals("DEPTH CHARGES", Names.callout("Depth Charges"));
        assertEquals("SONAR", Names.tile("Sonar Pulse"));
        assertEquals("FLARES", Names.tile("Decoy Flares"));
        assertEquals("HULL", Names.tile("Pressure hull"));
        assertEquals("NANITE", Names.tile("Auto-repair nanites"));
    }

    @Test
    void theBulwarkIsBoughtFittedUnfittedSoldAndUndoneOnItsRow() {
        Campaign campaign = campaign(22, 10_000, Optional.empty(), false);
        HangarState state = atBulwark(campaign);
        assertEquals(State.BUYABLE, state.bulwarkRow().orElseThrow().state());
        assertEquals(List.of(Action.BUY), actions(state));
        assertEquals(Optional.of(Action.BUY), state.selectedAction());

        assertEquals(Outcome.DONE, state.confirm());
        assertEquals(4_000, campaign.credits());
        assertTrue(state.bulwarkFlies());
        assertTrue(state.bulwarkFliesNext());
        assertEquals(
                Escort.Escorter.BULWARK, campaign.escortFlight().orElseThrow().escorter());
        assertEquals("BOUGHT THE BULWARK - IT FLIES IN THE ESCORT SLOT", state.message());
        assertEquals("FITTED", ShopPanel.bulwarkStatus(state.bulwarkRow().orElseThrow(), state.bulwarkFlies()));
        assertEquals(List.of(Action.UNFIT, Action.SELL), actions(state));

        assertEquals(Outcome.DONE, state.confirm(), "unfit: Rook flies again");
        assertTrue(campaign.escortFlight().orElseThrow().rook());
        assertEquals(List.of(Action.FIT, Action.SELL), actions(state));
        assertEquals(Outcome.DONE, state.confirm(), "fit: the Bulwark flies");
        assertFalse(campaign.escortFlight().orElseThrow().rook());

        assertEquals(Outcome.MOVED, state.right());
        assertEquals(Outcome.SELL, state.confirm(), "selling asks first");
        HangarState.Sale sale = state.sale().orElseThrow();
        assertEquals(new HangarState.Sale(HangarState.BULWARK, 6_000, true), sale, "bought this visit: all back");
        assertEquals(Outcome.DONE, state.sell());
        assertEquals(10_000, campaign.credits());
        assertTrue(campaign.escortFlight().orElseThrow().rook(), "the slot goes back to Rook");
        assertEquals(State.BUYABLE, state.bulwarkRow().orElseThrow().state());

        state.confirm();
        assertEquals(4_000, campaign.credits());
        state.up();
        while (state.focus() != Focus.COMMANDS) {
            state.up();
        }
        while (state.command() != Command.UNDO) {
            state.left();
        }
        assertEquals(Outcome.DONE, state.confirm());
        assertEquals(10_000, campaign.credits(), "undone, full refund");
        assertFalse(campaign.gear().escort().bulwark().owned());
    }

    @Test
    void aBulwarkTooDearIsRefusedWithItsReason() {
        HangarState state = atBulwark(campaign(22, 1_000, Optional.empty(), false));

        assertFalse(state.bulwarkChoice().orElseThrow().allowed());
        assertEquals(Outcome.REFUSED, state.confirm());
        assertEquals("NOT ENOUGH CREDITS", state.message());
    }

    /**
     * The replay hangar shows the escort of the mission it replays, the one its flight takes (the user's playtests of
     * 2026-10-24 and 2026-10-26): for every level 01–28 and a campaign before Rook's loss (Level 26 next) and after it
     * (Level 29 next, the Bulwark owned), with the campaign's own choice Rook or the Bulwark: no escort, no escort slot
     * and no Rook warning before Level 08; Rook with his gun in Levels 08–26 whatever the campaign chose, at his campaign
     * armour with its warning before his loss and whole without a warning after it (neither a replay nor the campaign
     * can repair him); the Bulwark in Levels 27–28. Fitting the Bulwark in the replay hangar (from its unlock) flies it in
     * that replay only: the campaign's own choice stays, and the undo brings Rook back.
     */
    @Test
    void theReplayHangarShowsTheEscortOfTheMissionItReplays() {
        double rookArmour = 30;
        double max = RULES.escort().maxArmour();
        for (boolean lost : new boolean[] {false, true}) {
            for (boolean bulwarkChosen : new boolean[] {false, true}) {
                int next = lost ? 29 : 26;
                Campaign campaign = campaign(
                        next,
                        10_000,
                        lost || bulwarkChosen ? Optional.of(RULES.bulwark().maxArmour()) : Optional.empty(),
                        bulwarkChosen,
                        rookArmour,
                        lost ? List.of(Campaign.ROOK_MISSING) : List.of());
                Escort own = campaign.gear().escort();
                for (int level = 1; level < next; level++) {
                    HangarState replay = HangarState.of(
                            CATALOGUE,
                            campaign,
                            Optional.of(new Campaign.Replay(SaveSlots.Slot.AUTOSAVE, level)),
                            true);
                    Hangar hangar = replay.hangar();
                    Campaign flight = Campaign.replay(
                            RULES,
                            SaveSlots.Slot.AUTOSAVE,
                            campaign.save(Instant.EPOCH),
                            level,
                            campaign.replayEscort());
                    String where = "a replay of Level " + level + (lost ? " after Rook's loss" : " before Rook's loss")
                            + (bulwarkChosen ? ", the Bulwark chosen" : "");
                    assertEquals(flight.escortFlight(), hangar.escortFlight(), where + ": the flight's escort");
                    List<String> warnings = replay.launchWarnings(Optional.empty());
                    boolean rookWarned = warnings.stream().anyMatch(warning -> warning.startsWith("ROOK"));
                    if (level < RULES.escort().joins()) {
                        assertTrue(flight.escortFlight().isEmpty(), where);
                        assertFalse(replay.slots().contains(LoadoutSlot.ESCORT), where + ": no escort slot");
                        assertFalse(rookWarned, where + ": no Rook warning " + warnings);
                        continue;
                    }
                    Campaign.EscortFlight escort = flight.escortFlight().orElseThrow();
                    if (RULES.escort().missing(level)) {
                        assertTrue(hangar.rookMissing(), where);
                        assertEquals(Escort.Escorter.BULWARK, escort.escorter(), where);
                        assertFalse(escort.loaner(), where + ": the owned Bulwark");
                        assertFalse(rookWarned, where + ": no Rook warning " + warnings);
                        continue;
                    }
                    assertFalse(hangar.rookMissing(), where);
                    assertEquals(Escort.Escorter.ROOK, escort.escorter(), where + ": Rook flies");
                    assertFalse(replay.bulwarkFliesNext(), where);
                    assertEquals(lost ? max : rookArmour, escort.armour(), where + ": Rook's armour");
                    assertEquals(
                            lost ? List.of() : List.of(HangarState.rookArmour(rookArmour, max)),
                            warnings,
                            where + ": the launch warnings");
                    assertEquals(
                            flight.escortArmourLow(level), campaign.escortArmourLow(level), where + ": the warning");
                    while (replay.slot() != LoadoutSlot.ESCORT) {
                        assertEquals(Outcome.MOVED, replay.nextSlot());
                    }
                    assertFalse(replay.rows().isEmpty(), where + ": Rook's guns listed");
                    boolean unlocked = level >= RULES.bulwark().unlock();
                    boolean owned = own.bulwark().owned();
                    assertEquals(unlocked && owned, replay.bulwarkRow().isPresent(), where + ": the Bulwark row");
                    if (!unlocked || !owned) {
                        continue;
                    }
                    assertEquals(List.of(Action.FIT), actions(replay), where + ": a refit, no sale");
                    assertTrue(hangar.chooseEscort(Escort.Escorter.BULWARK), where + ": the Bulwark fitted");
                    assertTrue(replay.bulwarkFliesNext(), where);
                    Campaign fitted = Campaign.replay(
                            RULES,
                            SaveSlots.Slot.AUTOSAVE,
                            campaign.save(Instant.EPOCH),
                            level,
                            campaign.replayEscort());
                    assertEquals(fitted.escortFlight(), hangar.escortFlight(), where + ": the fitted Bulwark flies");
                    assertEquals(
                            Escort.Escorter.BULWARK,
                            fitted.escortFlight().orElseThrow().escorter(),
                            where);
                    assertEquals(own, campaign.gear().escort(), where + ": the campaign's own choice stays");
                    assertTrue(hangar.undo(), where);
                    assertEquals(
                            Escort.Escorter.ROOK,
                            hangar.escortFlight().orElseThrow().escorter(),
                            where + ": undo");
                }
                HangarState mine = HangarState.of(CATALOGUE, campaign, Optional.empty(), true);
                assertEquals(
                        lost || bulwarkChosen,
                        mine.bulwarkFliesNext(),
                        "the campaign's own hangar keeps its choice" + (lost ? " (the Bulwark flies for Rook)" : ""));
            }
        }
    }

    @Test
    void whileRookIsMissingALoanerFliesAndHisGunsAndRepairLineAreGone() {
        Campaign campaign = campaign(27, 10_000, Optional.empty(), false);
        HangarState state = atEscort(campaign);

        assertTrue(state.rows().isEmpty(), "no gun rows while he is MIA");
        assertTrue(state.bulwarkSelected(), "the Bulwark's row is the first");
        assertEquals(2, state.rowCount(), "the Bulwark's row and the side row");
        Campaign.EscortFlight flight = campaign.escortFlight().orElseThrow();
        assertTrue(flight.loaner());
        assertTrue(state.bulwarkFliesNext());
        assertEquals(
                "ROOK IS MIA: A LOANER FLIES FREE",
                ShopPanel.bulwarkNote(
                        state,
                        state.bulwarkRow().orElseThrow(),
                        CONTENT.wingmen().bulwark()));
        assertEquals(List.of(RepairLine.SHIP), state.repairLines());

        state.confirm();
        assertFalse(campaign.escortFlight().orElseThrow().loaner(), "the bought one flies");
        assertEquals(List.of(Action.SELL), actions(state), "no FIT or UNFIT: it flies while Rook is missing");
    }

    @Test
    void anOwnedBulwarksArmourIsRepairedOnItsOwnLineAtItsOwnCost() {
        Campaign campaign = campaign(23, 10_000, Optional.of(100.0), true);
        HangarState state = atEscort(campaign);
        assertEquals(List.of(RepairLine.SHIP, RepairLine.ROOK, RepairLine.BULWARK), state.repairLines());
        assertEquals(20, state.missing(RepairLine.BULWARK));
        assertEquals(CONTENT.wingmen().bulwark().repair(), state.repairCost(RepairLine.BULWARK));
        assertTrue(state.enabled(Command.REPAIR));
        while (state.focus() != Focus.COMMANDS) {
            state.up();
        }
        while (state.command() != Command.REPAIR) {
            state.left();
        }

        assertEquals(Outcome.MOVED, state.confirm());
        assertEquals(RepairLine.BULWARK, state.repairLine(), "the only line with points missing");
        assertEquals(Outcome.DONE, state.confirm());
        assertEquals(10_000 - 20 * 8, campaign.credits());
        assertEquals("REPAIRED BULWARK: 20 POINTS FOR CR 160", state.message());
        assertEquals(0, state.missing(RepairLine.BULWARK));
    }

    @Test
    void theHydroKitsNoticeShowsOnceAndFitsTheMessageLines() {
        Campaign campaign = campaign(23, 0, Optional.empty(), false);

        assertTrue(campaign.hydroKitNotice());
        assertFalse(campaign.hydroKitNotice(), "once");
        assertTrue(Words.wrap(HangarState.HYDRO_KIT_NOTICE, (ShopPanel.WIDTH - 20) / 8)
                        .size()
                <= 2);
        assertTrue(Words.wrap(HangarState.UNDER_WATER_NOTICE, (ShopPanel.WIDTH - 20) / 8)
                        .size()
                <= 2);
    }

    /** M8 AW: the hydro-kit's visit shows its two notices in turn, each until the next move. */
    @Test
    void theUnderWaterWarningFollowsTheHydroKitsNotice() {
        HangarState state = new HangarState(new Hangar(CATALOGUE, campaign(23, 5_000, Optional.empty(), false)), true);
        state.notice(HangarState.HYDRO_KIT_NOTICE);
        state.notice(HangarState.UNDER_WATER_NOTICE);
        assertEquals(HangarState.HYDRO_KIT_NOTICE, state.message());
        state.down();
        assertEquals(HangarState.UNDER_WATER_NOTICE, state.message(), "the second on the first move");
        state.down();
        assertEquals("", state.message(), "both gone");
    }

    @Test
    void theIntelMarksAFittedSpecialTheLevelForbids() {
        String dry = CONTENT.levelKey(1).orElseThrow();
        assertEquals(
                Optional.of("SONAR PULSE: NO WATER"), IntelPanel.limited(CONTENT, dry, Optional.of("Sonar Pulse")));
        assertEquals(Optional.empty(), IntelPanel.limited(CONTENT, dry, Optional.of("Airstrike")));
        assertEquals(Optional.empty(), IntelPanel.limited(CONTENT, dry, Optional.empty()));
        String water = CONTENT.levelKey(11).orElseThrow();
        assertEquals(Optional.empty(), IntelPanel.limited(CONTENT, water, Optional.of("Sonar Pulse")), "over water");
    }

    @Test
    void theDepthChargesTestRangeIsWaterWithSubmergedDummiesThatOneBurstDestroys() {
        var script = TestFire.script(CONTENT.weapon("depth-charges"), TestFire.line(Armament.Slot.REAR));

        assertTrue(script.ground().water());
        assertEquals(
                vanguard.sim.Layer.SUB, script.groundUnits().getFirst().enemy().layer());
        assertEquals(
                CONTENT.weapon("depth-charges").levels().getFirst().damage(),
                script.groundUnits().getFirst().enemy().hp(),
                1e-9);
    }
}
