package vanguard.game.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Keys;
import org.junit.jupiter.api.Test;
import vanguard.game.input.ActionInput;
import vanguard.game.input.Bindings;
import vanguard.game.input.FakeDevices;
import vanguard.game.input.FakePointer;
import vanguard.game.input.MenuInput;

class DialogTest {
    private final FakeDevices devices = new FakeDevices();
    private final ActionInput input = new ActionInput(Bindings.defaults());
    private final FakePointer mouse = new FakePointer();
    private final MenuInput menu = new MenuInput(input, mouse.pointer);
    private final Dialog dialog = new Dialog("QUIT TO MAIN MENU?", "", "YES, QUIT", "NO, BACK");

    private Dialog.Answer press(int key) {
        devices.keys.clear();
        input.update(devices);
        menu.update(1 / 60f);
        devices.keys.add(key);
        input.update(devices);
        menu.update(1 / 60f);
        return dialog.update(menu);
    }

    @Test
    void confirmAtOnceAnswersTheSafeNo() {
        assertEquals(Dialog.Answer.NO, press(Keys.ENTER));
    }

    @Test
    void yesNeedsTheCursorMovedFirst() {
        assertEquals(Dialog.Answer.NONE, press(Keys.LEFT));

        assertEquals(Dialog.Answer.YES, press(Keys.ENTER));
    }

    @Test
    void backAnswersNo() {
        press(Keys.LEFT);

        assertEquals(Dialog.Answer.NO, press(Keys.ESCAPE));
    }

    private Dialog.Answer idle() {
        devices.keys.clear();
        input.update(devices);
        menu.update(1 / 60f);
        return dialog.update(menu);
    }

    @Test
    void hoveringAButtonSelectsIt() {
        mouse.move(dialog.yesButton());
        assertEquals(Dialog.Answer.NONE, idle());
        assertTrue(dialog.yesSelected());

        mouse.move(dialog.noButton());
        idle();
        assertFalse(dialog.yesSelected());
    }

    @Test
    void aClickOnAButtonAnswers() {
        mouse.click(dialog.yesButton());
        assertEquals(Dialog.Answer.YES, idle());

        mouse.click(dialog.noButton());
        assertEquals(Dialog.Answer.NO, idle());
    }

    @Test
    void aClickBesideTheButtonsDoesNothing() {
        Box box = dialog.box();
        mouse.click(new Box(box.x() + 20, box.y() + 20, 4, 4));

        assertEquals(Dialog.Answer.NONE, idle());
    }

    @Test
    void theButtonsLieInTheDialogSideBySide() {
        Box box = dialog.box();
        Box yes = dialog.yesButton();
        Box no = dialog.noButton();

        assertTrue(box.contains(yes.x(), yes.y()) && box.contains(no.x() + no.width() - 1, no.y() + no.height() - 1));
        assertTrue(yes.x() + yes.width() < no.x());
    }
}
