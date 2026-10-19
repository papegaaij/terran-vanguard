package vanguard.game.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.game.input.ActionInput;
import vanguard.game.input.Bindings;
import vanguard.game.input.FakeDevices;
import vanguard.game.input.FakePointer;
import vanguard.game.input.MenuInput;

class MenuTest {
    private final Menu<String> menu = new Menu<>(List.of(
            Menu.Item.disabled("continue", "CONTINUE"),
            Menu.Item.of("new", "NEW GAME"),
            Menu.Item.disabled("load", "LOAD GAME"),
            Menu.Item.of("options", "OPTIONS"),
            Menu.Item.of("quit", "QUIT")));

    @Test
    void theCursorStartsOnTheFirstEnabledItem() {
        assertEquals("new", menu.selectedId());
    }

    @Test
    void theCursorSkipsDisabledItems() {
        assertTrue(menu.move(1));

        assertEquals("options", menu.selectedId());
    }

    @Test
    void theCursorWrapsAtBothEnds() {
        menu.move(-1);
        assertEquals("quit", menu.selectedId());

        menu.move(1);

        assertEquals("new", menu.selectedId());
    }

    @Test
    void aSingleEnabledItemDoesNotMove() {
        var single = new Menu<>(List.of(Menu.Item.of("resume", "RESUME"), Menu.Item.disabled("abort", "ABORT")));

        assertFalse(single.move(1));
    }

    /** The rows of a list 30 px apart from y = 100. */
    private static Box row(int index) {
        return new Box(50, 100 + index * 30, 200, 30);
    }

    private final FakePointer mouse = new FakePointer();
    private final MenuInput input = idle();

    private MenuInput idle() {
        var actions = new ActionInput(Bindings.defaults());
        actions.update(new FakeDevices());
        return new MenuInput(actions, mouse.pointer);
    }

    @Test
    void hoveringAnEnabledItemSelectsItAsTheKeysWould() {
        mouse.move(row(3));

        assertTrue(menu.navigate(input, MenuTest::row));
        assertEquals("options", menu.selectedId());
        assertFalse(menu.confirmed(input, MenuTest::row));
    }

    @Test
    void hoveringADisabledItemKeepsTheSelection() {
        mouse.move(row(2));

        assertFalse(menu.navigate(input, MenuTest::row));
        assertEquals("new", menu.selectedId());
    }

    @Test
    void aClickOnAnItemSelectsAndConfirmsIt() {
        mouse.click(row(4));

        assertTrue(menu.confirmed(input, MenuTest::row));
        assertEquals("quit", menu.selectedId());
    }

    @Test
    void aClickOnADisabledItemOrBesideTheListDoesNothing() {
        mouse.click(row(0));
        assertFalse(menu.confirmed(input, MenuTest::row));

        mouse.click(new Box(260, 100, 10, 10));
        assertFalse(menu.confirmed(input, MenuTest::row));
        assertEquals("new", menu.selectedId());
    }
}
