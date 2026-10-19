package vanguard.game.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Buttons;
import org.junit.jupiter.api.Test;
import vanguard.game.ui.Box;

class PointerTest {
    private final FakePointer mouse = new FakePointer();
    private final Box box = new Box(100, 100, 50, 20);

    @Test
    void hoverNeedsThePointerToMove() {
        assertTrue(mouse.move(box).hovers(box));

        assertFalse(mouse.frame().hovers(box), "a resting pointer leaves the selection to the keys");
        assertTrue(mouse.pointer.over(box));
    }

    @Test
    void aClickCountsInsideTheBoxOnly() {
        mouse.move(box);
        assertTrue(mouse.click().clicks(box));

        mouse.move(160, 110);
        Pointer outside = mouse.click();
        assertTrue(outside.clicked());
        assertFalse(outside.clicks(box));
        assertFalse(mouse.frame().clicked(), "a click lasts one frame");
    }

    @Test
    void theRightAndBackButtonsGoBack() {
        assertTrue(mouse.press(Buttons.RIGHT).back());
        assertTrue(mouse.press(Buttons.BACK).back());
        assertFalse(mouse.press(Buttons.LEFT).back());
    }

    @Test
    void theClickThatFocusesTheWindowConfirmsNothing() {
        var fake = new FakePointer();
        fake.pointer.update(fake, false, 1);
        fake.move(box);
        fake.press(Buttons.LEFT);
        // The window got the focus with this click.
        assertFalse(fake.pointer.clicked());

        fake.pointer.update(fake, true, Pointer.FOCUS_SECONDS);
        assertTrue(fake.click().clicks(box));
    }

    @Test
    void aWindowThatNeverReportsTheFocusTakesClicks() {
        mouse.focused = false;
        mouse.frame();
        mouse.move(box);

        assertTrue(mouse.click().clicks(box), "as on X11 without a window manager");
    }

    @Test
    void aDisabledPointerDoesNothing() {
        mouse.pointer.enabled(false);
        mouse.move(box);
        mouse.turnWheel(2);

        assertFalse(mouse.click().clicked());
        assertFalse(mouse.move(box.x() + 1, box.y() + 1).hovers(box));
        assertEquals(0, mouse.pointer.wheel());
        assertFalse(mouse.pointer.over(box));
    }

    @Test
    void theWheelCountsWholeNotchesAndKeepsTheRest() {
        assertEquals(0, mouse.turnWheel(0.6f).wheel());
        assertEquals(1, mouse.turnWheel(0.6f).wheel());
        assertEquals(-2, mouse.turnWheel(-2.2f).wheel());
    }

    @Test
    void heldFollowsTheLeftButton() {
        assertTrue(mouse.hold(true).held());
        assertFalse(mouse.hold(false).held());
    }

    @Test
    void theRightButtonIsBackInTheMenuInput() {
        var input = new ActionInput(Bindings.defaults());
        input.update(new FakeDevices());
        var menu = new MenuInput(input, mouse.pointer);

        mouse.press(Buttons.RIGHT);

        assertTrue(menu.back());
        assertFalse(menu.confirm());
        assertFalse(menu.clicked());
    }
}
