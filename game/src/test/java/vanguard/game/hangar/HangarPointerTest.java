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
import vanguard.content.campaign.Fitted;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveFormat;
import vanguard.content.campaign.SaveGame;
import vanguard.game.hangar.HangarState.Command;
import vanguard.game.hangar.HangarState.Focus;
import vanguard.game.hangar.HangarState.Outcome;
import vanguard.game.input.FakePointer;
import vanguard.game.ui.Box;

/** The hangar with the mouse: every click reaches what the keys reach, and does what they do there. */
class HangarPointerTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);

    private static Campaign campaign(int level, int credits, double armour) {
        return campaign(Difficulty.MEDIUM, level, credits, armour, RULES.starterLoadout());
    }

    private static Campaign campaign(
            Difficulty difficulty, int level, int credits, double armour, Map<LoadoutSlot, Fitted> loadout) {
        return Campaign.load(
                RULES,
                new SaveGame(
                        SaveFormat.VERSION,
                        Instant.EPOCH,
                        0,
                        difficulty,
                        level,
                        credits,
                        0,
                        loadout,
                        Map.of(),
                        List.of(),
                        Map.of(),
                        armour,
                        Optional.empty(),
                        Map.of(),
                        List.of(),
                        List.of(),
                        new SaveGame.Stats(0, 0)));
    }

    private static HangarState state(Campaign campaign, boolean launchable) {
        return new HangarState(new Hangar(CATALOGUE, campaign), launchable);
    }

    private final FakePointer mouse = new FakePointer();
    private final HangarPointer hangarPointer = new HangarPointer();

    private HangarPointer.Result click(HangarState state, Box box) {
        mouse.move(box);
        hangarPointer.update(state, mouse.pointer);
        mouse.click();
        return hangarPointer.update(state, mouse.pointer);
    }

    private HangarPointer.Result hover(HangarState state, Box box) {
        mouse.move(box);
        return hangarPointer.update(state, mouse.pointer);
    }

    @Test
    void hoveringAShopRowOnlyHighlightsItAndAClickSelectsItAsDownDoes() {
        HangarState byKeys = state(campaign(2, 1000, 60), true);
        HangarState byMouse = state(campaign(2, 1000, 60), true);
        byKeys.down();

        assertEquals(Outcome.NONE, hover(byMouse, ShopPanel.row(1)).outcome());
        assertEquals(0, byMouse.row(), "hover never moves the selection (playtest 2026-10-19)");
        assertEquals(Optional.of(new HangarState.Hover(HangarState.Target.ROW, 1)), byMouse.hover());

        assertEquals(Outcome.MOVED, click(byMouse, ShopPanel.row(1)).outcome());
        assertEquals(byKeys.row(), byMouse.row());
        assertEquals(byKeys.selected(), byMouse.selected());
    }

    /**
     * The user's playtest finding (2026-10-19): select a weapon with a click, move the mouse over the
     * other rows and the command bar down to its upgrade chip, and click it: the clicked weapon is the
     * one upgraded.
     */
    @Test
    void theClickedRowStaysSelectedWhileThePointerTravelsToItsChoices() {
        HangarState state = state(campaign(2, 5000, 60), true);
        int row = rowWithChoices(state);
        assertTrue(row > 0, "the travel passes another row");
        click(state, ShopPanel.row(row));
        Offer clicked = state.selected().orElseThrow();
        for (int shown = row - 1; shown >= 0; shown--) {
            hover(state, ShopPanel.row(shown));
        }
        for (Command command : Command.values()) {
            hover(state, HangarPointer.command(command));
        }
        for (Box slot : LoadoutPanel.slots(false, false).values()) {
            hover(state, slot);
        }
        for (int shown = 0; shown < HangarState.VISIBLE_ROWS; shown++) {
            hover(state, ShopPanel.row(shown));
        }

        assertEquals(row, state.row());
        assertEquals(Focus.SHOP, state.focus());
        assertEquals(clicked, state.selected().orElseThrow());
        assertEquals(
                Outcome.NONE, hover(state, ShopPanel.choiceChip(clicked, 1)).outcome());
        assertEquals(0, state.choice().map(clicked.choices()::indexOf).orElseThrow(), "hover leaves the choice");
        assertEquals(
                Optional.of(clicked.choices().get(1)),
                state.previewChoice(),
                "the test fire and the power bar preview the choice under the pointer");

        assertTrue(click(state, ShopPanel.choiceChip(clicked, 1)).confirm());
        assertEquals(clicked.choices().get(1), state.choice().orElseThrow());
    }

    @Test
    void movingOffAChoiceEndsItsPreviewAndAKeyTakesTheHoverOff() {
        HangarState state = state(campaign(2, 5000, 60), true);
        int row = rowWithChoices(state);
        click(state, ShopPanel.row(row));
        Offer offer = state.selected().orElseThrow();
        hover(state, ShopPanel.choiceChip(offer, 1));
        assertEquals(Optional.of(offer.choices().get(1)), state.previewChoice());

        mouse.move(-50, -50);
        hangarPointer.update(state, mouse.pointer);
        assertEquals(Optional.empty(), state.hover());
        assertEquals(state.choice(), state.previewChoice());

        hover(state, ShopPanel.choiceChip(offer, 1));
        state.hover(Optional.empty());
        assertEquals(state.choice(), state.previewChoice());
    }

    @Test
    void aClickOnAChoiceWhileTheCommandBarHasTheFocusMakesIt() {
        Campaign keyed = campaign(2, 1000, 60);
        Campaign clicked = campaign(2, 1000, 60);
        HangarState byKeys = state(keyed, true);
        HangarState byMouse = state(clicked, true);
        byMouse.up();
        assertEquals(Focus.COMMANDS, byMouse.focus());

        assertEquals(Outcome.DONE, byKeys.confirm());
        assertTrue(click(byMouse, ShopPanel.choiceChip(byMouse.selected().orElseThrow(), 0))
                .confirm());
        assertEquals(Focus.SHOP, byMouse.focus());
        assertEquals(Outcome.DONE, byMouse.confirm());
        assertEquals(keyed.loadout(), clicked.loadout());
    }

    @Test
    void hoveringACommandOrASlotOnlyHighlightsIt() {
        HangarState state = state(campaign(2, 1000, 60), true);
        LoadoutSlot slot = state.slot();

        assertEquals(
                Outcome.NONE,
                hover(state, HangarPointer.command(Command.LAUNCH)).outcome());
        assertEquals(Focus.SHOP, state.focus());
        assertTrue(state.hovers(HangarState.Target.COMMAND, Command.LAUNCH.ordinal()));
        hover(state, HangarPointer.command(Command.UNDO));
        assertEquals(Optional.empty(), state.hover(), "a disabled command gets no hover look");

        LoadoutSlot other = HangarState.SLOTS.get(2);
        hover(state, LoadoutPanel.slots(false, false).get(other));
        assertEquals(slot, state.slot());
        assertTrue(state.hovers(HangarState.Target.SLOT, state.slots().indexOf(other)));
    }

    @Test
    void theReplayHangarRefitsWithClicksToo() {
        Campaign campaign = campaign(2, 5000, 60);
        HangarState shop = state(campaign, true);
        rowWithChoices(shop);
        HangarState refit = new HangarState(new Hangar(CATALOGUE, campaign), true, true);
        for (int row = 0; row < refit.rowCount(); row++) {
            click(refit, ShopPanel.row(row));
            Offer offer = refit.selected().orElseThrow();
            if (offer.choices().isEmpty()) {
                continue;
            }
            hover(refit, ShopPanel.row((row + 1) % Math.min(refit.rowCount(), HangarState.VISIBLE_ROWS)));
            assertEquals(row, refit.row(), "hover keeps the replay hangar's selection too");
            assertTrue(click(refit, ShopPanel.choiceChip(offer, 0)).confirm());
            return;
        }
        throw new AssertionError("no refit choice in the replay hangar");
    }

    @Test
    void aClickOnAChoiceMakesItAsConfirmDoes() {
        Campaign keyed = campaign(2, 1000, 60);
        Campaign clicked = campaign(2, 1000, 60);
        HangarState byKeys = state(keyed, true);
        HangarState byMouse = state(clicked, true);

        assertEquals(Outcome.DONE, byKeys.confirm());
        HangarPointer.Result result =
                click(byMouse, ShopPanel.choiceChip(byMouse.selected().orElseThrow(), 0));

        assertTrue(result.confirm(), "the screen confirms, with the confirm's sound");
        assertEquals(Outcome.DONE, byMouse.confirm());
        assertEquals(keyed.loadout(), clicked.loadout());
        assertEquals(keyed.credits(), clicked.credits());
    }

    /** The first shown row with two choices or more, after buying the second row's item (the first goes to the inventory). */
    private static int rowWithChoices(HangarState state) {
        state.down();
        state.confirm();
        for (int row = 0; row < Math.min(state.rowCount(), HangarState.VISIBLE_ROWS); row++) {
            if (state.rows().get(row).choices().size() > 1) {
                return row;
            }
        }
        throw new AssertionError("no row with two choices");
    }

    @Test
    void aClickOnAChoiceSelectsItAsRightDoes() {
        HangarState byKeys = state(campaign(2, 5000, 60), true);
        HangarState byMouse = state(campaign(2, 5000, 60), true);
        int row = rowWithChoices(byKeys);
        assertEquals(row, rowWithChoices(byMouse));
        while (byKeys.row() < row) {
            byKeys.down();
        }
        while (byKeys.row() > row) {
            byKeys.up();
        }
        byKeys.right();
        click(byMouse, ShopPanel.row(row));

        assertTrue(click(byMouse, ShopPanel.choiceChip(byMouse.selected().orElseThrow(), 1))
                .confirm());
        assertEquals(byKeys.choice(), byMouse.choice());
    }

    @Test
    void aClickOnACalloutOrTileSelectsTheSlotAsTheTabsDo() {
        HangarState byKeys = state(campaign(2, 1000, 60), true);
        HangarState byMouse = state(campaign(2, 1000, 60), true);
        Map<LoadoutSlot, Box> slots = LoadoutPanel.slots(false, false);
        for (LoadoutSlot slot : HangarState.SLOTS) {
            while (byKeys.slot() != slot) {
                byKeys.nextSlot();
            }
            click(byMouse, slots.get(slot));
            assertEquals(byKeys.slot(), byMouse.slot(), slot.name());
            assertEquals(byKeys.rows(), byMouse.rows(), slot.name());
        }
    }

    @Test
    void aClickOnACommandRunsItAndADisabledOneDoesNothing() {
        HangarState state = state(campaign(2, 1000, 60), true);

        assertFalse(click(state, HangarPointer.command(Command.UNDO)).confirm(), "nothing to undo");
        HangarPointer.Result launch = click(state, HangarPointer.command(Command.LAUNCH));

        assertTrue(launch.confirm());
        assertEquals(Focus.COMMANDS, state.focus());
        assertEquals(Outcome.LAUNCH, state.confirm());
    }

    @Test
    void theRepairPanelTakesTheMouseAlone() {
        HangarState state = state(campaign(2, 1000, 40), true);
        click(state, HangarPointer.command(Command.REPAIR));
        assertEquals(Outcome.MOVED, state.confirm());
        assertEquals(Focus.REPAIR, state.focus());
        int most = state.affordable(HangarState.RepairLine.SHIP);

        Box slider = HangarPointer.repairSlider(false);
        mouse.move(slider.x() + slider.width() / 2, slider.y() + 5);
        hangarPointer.update(state, mouse.pointer);
        mouse.click();
        hangarPointer.update(state, mouse.pointer);
        assertEquals(Math.round(most / 2.0), state.repairPoints());
        assertFalse(click(state, ShopPanel.row(2)).confirm(), "the shop waits behind the panel");
        assertEquals(Focus.REPAIR, state.focus());

        assertTrue(click(state, HangarPointer.repairButton(false)).confirm());
        assertEquals(Outcome.DONE, state.confirm());
    }

    @Test
    void theWheelScrollsALongShopListAndLeavesAShortOneAlone() {
        HangarState state = state(campaign(2, 1000, 60), true);
        assertTrue(state.rowCount() <= HangarState.VISIBLE_ROWS);
        mouse.move(ShopPanel.row(2));
        mouse.turnWheel(1);
        hangarPointer.update(state, mouse.pointer);
        assertEquals(0, state.top(), "the front guns' list fits");

        HangarState longest = longestList();
        if (longest.rowCount() > HangarState.VISIBLE_ROWS) {
            mouse.turnWheel(1);
            hangarPointer.update(longest, mouse.pointer);
            assertEquals(1, longest.top());
        }
    }

    /** The slot with the most shop rows over the levels of Acts 1 and 2. */
    private static HangarState longestList() {
        HangarState longest = state(campaign(2, 1000, 60), true);
        for (int level = 2; level <= 14; level++) {
            HangarState state = state(campaign(level, 1000, 60), true);
            for (int slot = 0; slot < state.slots().size(); slot++) {
                if (state.rowCount() > longest.rowCount()) {
                    longest = state(campaign(level, 1000, 60), true);
                    while (longest.slot() != state.slot()) {
                        longest.nextSlot();
                    }
                }
                state.nextSlot();
            }
        }
        return longest;
    }

    @Test
    void scrollingStopsAtEitherEndOfTheList() {
        HangarState state = longestList();
        int most = Math.max(0, state.rowCount() - HangarState.VISIBLE_ROWS);

        assertFalse(state.scroll(-1));
        assertEquals(most > 0, state.scroll(most + 3));
        assertEquals(most, state.top());
    }

    @Test
    void theCommandsAndRepairPanelLieOnTheScreen() {
        for (Command command : Command.values()) {
            Box chip = HangarPointer.command(command);
            assertTrue(chip.x() >= 0 && chip.x() + chip.width() <= 960 && chip.y() >= 0, command.name());
        }
        Box panel = HangarPointer.repairPanel(true);
        Box button = HangarPointer.repairButton(true);
        assertTrue(button.y() + button.height() <= panel.y() + panel.height());
    }
}
