package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;
import vanguard.game.input.Action;
import vanguard.game.input.ActionInput;
import vanguard.game.input.BindingSlot;
import vanguard.game.input.Bindings;
import vanguard.game.input.FakeDevices;
import vanguard.game.input.FakePointer;
import vanguard.game.input.MenuInput;
import vanguard.game.ui.Box;
import vanguard.game.ui.Menu;

/**
 * The mouse's hit regions of the out-of-game screens (design/ui, Mouse): a click on an item does
 * what moving the keys' cursor there and confirming does, the regions of a list touch without
 * overlapping, and the Options screen's tabs, chips, sliders, sound tests and binding cells lie
 * where it draws them.
 */
class MouseRegionsTest {
    private final FakeDevices devices = new FakeDevices();
    private final ActionInput actions = new ActionInput(Bindings.defaults());
    private final FakePointer mouse = new FakePointer();
    private final MenuInput input = new MenuInput(actions, mouse.pointer);

    /** A frame in which the keys in {@code keys} are down (none: they are released). */
    private void keys(int... keys) {
        devices.keys.clear();
        for (int key : keys) {
            devices.keys.add(key);
        }
        actions.update(devices);
        input.update(1 / 60f);
    }

    private static Menu<Integer> menu(int items) {
        List<Menu.Item<Integer>> list = new ArrayList<>();
        for (int i = 0; i < items; i++) {
            list.add(Menu.Item.of(i, "ITEM " + i));
        }
        return new Menu<>(list);
    }

    /** The item a click on row {@code index} confirms is the one the keys reach by going down {@code index} times. */
    private void clickMatchesKeys(String screen, int items, IntFunction<Box> rows) {
        for (int index = 0; index < items; index++) {
            Menu<Integer> byKeys = menu(items);
            for (int i = 0; i < index; i++) {
                keys(Keys.DOWN);
                byKeys.navigate(input);
                keys();
            }
            keys(Keys.ENTER);
            assertTrue(byKeys.confirmed(input, rows));
            keys();

            Menu<Integer> byMouse = menu(items);
            mouse.click(rows.apply(index));
            assertTrue(byMouse.confirmed(input, rows), screen + " row " + index);
            assertEquals(byKeys.selectedId(), byMouse.selectedId(), screen + " row " + index);
        }
        for (int index = 0; index + 1 < items; index++) {
            Box row = rows.apply(index);
            assertEquals(row.y() + row.height(), rows.apply(index + 1).y(), 0.5f, screen + ": the rows touch");
        }
    }

    @Test
    void theMenusClickWhereTheyDrawTheirItems() {
        clickMatchesKeys("main menu", 8, MainMenuScreen::row);
        clickMatchesKeys("pause", 5, PauseScreen::row);
        clickMatchesKeys("mission failed", 4, MissionFailedScreen::row);
        clickMatchesKeys("load game", 9, SlotsScreen::row);
        clickMatchesKeys("mission select", 14, MissionSelectScreen::row);
    }

    @Test
    void theDifficultyCardsLieLeftToRightAsTheKeysChooseThem() {
        for (int i = 0; i + 1 < 3; i++) {
            Box card = DifficultyScreen.card(i);
            Box next = DifficultyScreen.card(i + 1);
            assertTrue(card.x() + card.width() <= next.x(), "card " + i + " left of card " + (i + 1));
        }
    }

    @Test
    void theMissionSelectWheelStopsAtEitherEnd() {
        assertEquals(0, MissionSelectScreen.wheel(0, -3, 30));
        assertEquals(2, MissionSelectScreen.wheel(0, 2, 30));
        assertEquals(30 - 14, MissionSelectScreen.wheel(10, 20, 30));
        assertEquals(0, MissionSelectScreen.wheel(0, 2, 10), "a list that fits does not scroll");
    }

    private final List<OptionTabs.Tab> tabs = OptionTabs.all();

    private Optional<OptionsLayout.Hit> at(int tab, Box box) {
        return OptionsLayout.at(tabs, tab, box.x() + box.width() / 2, box.y() + box.height() / 2);
    }

    @Test
    void anOptionsTabIsClickedOnItsChip() {
        for (int t = 0; t < tabs.size(); t++) {
            assertEquals(Optional.of(new OptionsLayout.Hit.Tab(t)), at(0, OptionsLayout.tab(tabs, t)));
        }
    }

    @Test
    void everyOptionsRowIsHoveredOnItsLabel() {
        for (int t = 0; t < tabs.size(); t++) {
            List<OptionRow> rows = tabs.get(t).rows();
            for (int i = 0; i < rows.size(); i++) {
                int y = OptionsLayout.rowY(rows, i);
                Optional<OptionsLayout.Hit> hit = OptionsLayout.at(tabs, t, OptionsLayout.LEFT + 4, y + 4);
                assertEquals(
                        Optional.of(new OptionsLayout.Hit.Row(i)),
                        hit,
                        tabs.get(t).name() + " row " + i);
            }
        }
    }

    @Test
    void aChipSetsWhatTheKeysReachByStepping() {
        var target = new OptionTabsTest.Target();
        for (int t = 0; t < tabs.size(); t++) {
            List<OptionRow> rows = tabs.get(t).rows();
            for (int i = 0; i < rows.size(); i++) {
                if (!(rows.get(i) instanceof OptionRow.Choice choice)) {
                    continue;
                }
                int y = OptionsLayout.rowY(rows, i);
                for (int k = 0; k < choice.chips().size(); k++) {
                    assertEquals(
                            Optional.of(new OptionsLayout.Hit.Chip(i, k)),
                            at(t, OptionsLayout.chip(choice, y, k)),
                            choice.label() + " chip " + k);
                }
                // Left as far as it goes, then right once: the second chip, as a click on it sets.
                while (choice.change(target, -1)) {}
                choice.change(target, 1);
                assertEquals(1, choice.get().applyAsInt(target), choice.label());
            }
        }
    }

    @Test
    void aSliderTakesTheShareThePointerPointsAt() {
        var target = new OptionTabsTest.Target();
        List<OptionRow> audio = tabs.get(1).rows();
        var master = (OptionRow.Slider) audio.getFirst();
        Box track = OptionsLayout.slider(OptionsLayout.rowY(audio, 0));
        Optional<OptionsLayout.Hit> hit = OptionsLayout.at(
                tabs, 1, OptionsLayout.VALUE_X + OptionsLayout.SLIDER_WIDTH / 4f, track.y() + track.height() / 2);

        assertEquals(Optional.of(new OptionsLayout.Hit.Slider(0, 0.25)), hit);
        assertTrue(master.point(target, 0.25));
        assertEquals(0.25, master.get().applyAsDouble(target.settings()), 1e-9);
        assertFalse(master.point(target, 0.26), "the nearest step is the same");
        assertTrue(master.point(target, 2));
        assertEquals(1, master.get().applyAsDouble(target.settings()), 1e-9);
    }

    @Test
    void aSoundTestsArrowsPickAndItsNamePlays() {
        List<OptionRow> audio = tabs.get(1).rows();
        int music = audio.indexOf(audio.stream()
                .filter(OptionRow.Sound.class::isInstance)
                .findFirst()
                .orElseThrow());
        Box chip = OptionsLayout.sound(OptionsLayout.rowY(audio, music));
        float y = chip.y() + chip.height() / 2;

        assertEquals(Optional.of(new OptionsLayout.Hit.Sound(music, -1)), OptionsLayout.at(tabs, 1, chip.x() + 4, y));
        assertEquals(
                Optional.of(new OptionsLayout.Hit.Sound(music, 1)),
                OptionsLayout.at(tabs, 1, chip.x() + chip.width() - 4, y));
        assertEquals(Optional.of(new OptionsLayout.Hit.Sound(music, 0)), at(1, chip));
    }

    @Test
    void aBindingCellIsClickedInItsColumn() {
        List<OptionRow> controls = tabs.get(2).rows();
        assertTrue(OptionsLayout.tableRows(controls) > 0);
        for (int i = 0; i < OptionsLayout.tableRows(controls); i++) {
            int y = OptionsLayout.rowY(controls, i);
            for (BindingSlot slot : BindingSlot.values()) {
                assertEquals(
                        Optional.of(new OptionsLayout.Hit.Cell(i, slot)),
                        at(2, OptionsLayout.cell(slot, y)),
                        "row " + i + " " + slot);
            }
        }
    }

    @Test
    void theRightButtonCancelsACaptureAndIsNeverCaptured() {
        var remapping = new Remapping();
        Bindings bindings = Bindings.defaults();
        keys();
        remapping.start(new Bindings.Assignment(Action.SPECIAL, BindingSlot.PRIMARY), devices);
        mouse.click();
        keys();
        assertEquals(Optional.empty(), remapping.update(devices, input, bindings));
        assertTrue(remapping.active(), "a left click is no key");

        mouse.press(Buttons.RIGHT);
        keys();
        assertEquals(Optional.empty(), remapping.update(devices, input, bindings));
        assertFalse(remapping.active());
    }
}
