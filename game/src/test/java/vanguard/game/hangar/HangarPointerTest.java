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
    void hoveringAShopRowSelectsItAsDownDoes() {
        HangarState byKeys = state(campaign(2, 1000, 60), true);
        HangarState byMouse = state(campaign(2, 1000, 60), true);
        byKeys.down();

        assertEquals(Outcome.MOVED, hover(byMouse, ShopPanel.row(1)).outcome());
        assertEquals(byKeys.row(), byMouse.row());
        assertEquals(byKeys.selected(), byMouse.selected());
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
    void hoveringAChoiceHighlightsItAsRightDoes() {
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
        hover(byMouse, ShopPanel.row(row));

        assertEquals(
                Outcome.MOVED,
                hover(byMouse, ShopPanel.choiceChip(byMouse.selected().orElseThrow(), 1))
                        .outcome());
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
