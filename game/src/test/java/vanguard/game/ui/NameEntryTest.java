package vanguard.game.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import java.util.Optional;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;
import vanguard.game.input.ActionInput;
import vanguard.game.input.Bindings;
import vanguard.game.input.FakeDevices;
import vanguard.game.input.FakePointer;
import vanguard.game.input.MenuInput;

/** The high scores' name entry: the letter grid, typing, the mouse and the name rules (M6 part B, Q4). */
class NameEntryTest {
    private final FakeDevices devices = new FakeDevices();
    private final ActionInput actions = new ActionInput(Bindings.defaults());
    private final FakePointer mouse = new FakePointer();
    private final MenuInput input = new MenuInput(actions, mouse.pointer);
    /** Keys 30 px apart in two rows, as a screen would place them. */
    private final IntFunction<Box> keys = index -> new Box(100 + index % 20 * 30, 300 + index / 20 * 30, 28, 28);

    /** A frame in which only {@code key} is down, then one in which it is released; returns the first frame's result. */
    private NameEntry.Result press(NameEntry entry, int key) {
        devices.keys.clear();
        devices.keys.add(key);
        actions.update(devices);
        input.update(1 / 60f);
        mouse.frame();
        NameEntry.Result result = entry.update(input, keys);
        devices.keys.clear();
        actions.update(devices);
        input.update(1 / 60f);
        mouse.frame();
        entry.update(input, keys);
        return result;
    }

    private NameEntry.Result type(NameEntry entry, String text) {
        devices.keys.clear();
        actions.update(devices);
        input.update(1 / 60f);
        mouse.type(text);
        return entry.update(input, keys);
    }

    @Test
    void theGridHoldsTwoRowsOfTwentyKeys() {
        assertEquals(40, NameEntry.KEYS.size());
        assertEquals("A", NameEntry.KEYS.get(0));
        assertEquals("T", NameEntry.KEYS.get(19));
        assertEquals("U", NameEntry.KEYS.get(20));
        assertEquals(NameEntry.END, NameEntry.KEYS.getLast());
    }

    @Test
    void theArrowsPickLettersAndConfirmAddsThem() {
        NameEntry entry = new NameEntry(Optional.empty());
        assertEquals(0, entry.selected(), "the cursor starts on A");
        press(entry, Keys.ENTER);
        press(entry, Keys.RIGHT);
        press(entry, Keys.ENTER);
        assertEquals(NameEntry.Result.MOVED, press(entry, Keys.LEFT));
        press(entry, Keys.LEFT);
        assertEquals(19, entry.selected(), "left wraps within the row");
        press(entry, Keys.ENTER);
        press(entry, Keys.DOWN);
        assertEquals(39, entry.selected(), "down keeps the column: END");
        assertEquals(NameEntry.Result.DONE, press(entry, Keys.ENTER));
        assertTrue(entry.done());
        assertEquals("ABT", entry.result());
        assertEquals(NameEntry.Result.NONE, press(entry, Keys.ENTER), "nothing more once done");
    }

    @Test
    void endRefusesAShortNameAndBackDeletes() {
        NameEntry entry = new NameEntry(Optional.empty());
        press(entry, Keys.ENTER);
        press(entry, Keys.ENTER);
        press(entry, Keys.UP);
        assertEquals(20, entry.selected(), "up from the first row goes to the second");
        for (int i = 0; i < 19; i++) {
            press(entry, Keys.RIGHT);
        }
        assertEquals(NameEntry.Result.REFUSED, press(entry, Keys.ENTER), "two letters are too few");
        assertFalse(entry.done());
        assertEquals(NameEntry.Result.EDITED, press(entry, Keys.ESCAPE));
        assertEquals("A", entry.name());
        press(entry, Keys.ESCAPE);
        assertEquals(NameEntry.Result.REFUSED, press(entry, Keys.ESCAPE), "nothing left to delete");
    }

    @Test
    void typingAddsAllowedCharactersAndPutsTheCursorOnEnd() {
        NameEntry entry = new NameEntry(Optional.empty());
        assertEquals(NameEntry.Result.EDITED, type(entry, "ro_ok.!"));
        assertEquals("ROOK", entry.name(), "lower case is raised, other characters are ignored");
        assertEquals(NameEntry.KEYS.indexOf(NameEntry.END), entry.selected());
        type(entry, "\b\bX-9 Z");
        assertEquals("ROX-9 Z", entry.name(), "backspace deletes");
        type(entry, "ABCDEFG");
        assertEquals(10, entry.name().length(), "at most 10 characters");
        assertEquals(NameEntry.Result.DONE, press(entry, Keys.ENTER), "Enter after typing takes the name");
        assertEquals("ROX-9 ZABC", entry.result());
    }

    @Test
    void spacesAtEitherEndAreDropped() {
        NameEntry entry = new NameEntry(Optional.empty());
        type(entry, " AB ");
        assertFalse(entry.valid(), "AB is too short without its spaces");
        type(entry, "C ");
        assertTrue(entry.valid());
        press(entry, Keys.ENTER);
        assertEquals("AB C", entry.result());
    }

    @Test
    void theLastNameIsOfferedWithTheCursorOnEnd() {
        NameEntry entry = new NameEntry(Optional.of("LANCER"));
        assertEquals("LANCER", entry.name());
        assertEquals(NameEntry.KEYS.indexOf(NameEntry.END), entry.selected());
        assertEquals(NameEntry.Result.DONE, press(entry, Keys.ENTER));
        assertEquals("LANCER", entry.result());

        assertEquals("", new NameEntry(Optional.of("x")).name(), "an invalid last name is not offered");
    }

    @Test
    void theMouseHoversAndClicksKeys() {
        NameEntry entry = new NameEntry(Optional.empty());
        mouse.move(keys.apply(25));
        assertEquals(NameEntry.Result.MOVED, entry.update(input, keys));
        assertEquals(25, entry.selected());
        mouse.click(keys.apply(2));
        assertEquals(NameEntry.Result.EDITED, entry.update(input, keys));
        mouse.click(keys.apply(0));
        entry.update(input, keys);
        mouse.click(keys.apply(NameEntry.KEYS.indexOf(NameEntry.SPACE)));
        entry.update(input, keys);
        mouse.click(keys.apply(7));
        entry.update(input, keys);
        assertEquals("CA H", entry.name());
        mouse.click(keys.apply(NameEntry.KEYS.indexOf(NameEntry.DELETE)));
        entry.update(input, keys);
        assertEquals("CA ", entry.name());
        mouse.press(Buttons.RIGHT);
        entry.update(input, keys);
        assertEquals("CA", entry.name(), "the right button deletes, as Esc");
        mouse.click(keys.apply(NameEntry.KEYS.indexOf(NameEntry.END)));
        assertEquals(NameEntry.Result.REFUSED, entry.update(input, keys));
    }
}
