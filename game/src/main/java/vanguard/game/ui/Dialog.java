package vanguard.game.ui;

import vanguard.game.input.MenuInput;
import vanguard.game.input.Pointer;
import vanguard.game.render.PixelScreen;

/**
 * A confirmation (design/ui, the UI kit's confirm dialog): a question, a line of detail and two
 * buttons. The cursor starts on the safe answer; Back answers no. With the mouse, hovering a button
 * selects it and a click on one answers; a click beside the buttons does nothing. The dialog's
 * layout (centred on the screen, as wide as its text needs) lives here, for the drawing and the
 * hit regions alike.
 */
public final class Dialog {
    /** What the player did in a frame. */
    public enum Answer {
        NONE,
        YES,
        NO
    }

    /** The dialog's height and its least width. */
    public static final int HEIGHT = 132;

    private static final int MIN_WIDTH = 360;
    private static final int BUTTON_HEIGHT = 26;

    private final String question;
    private final String detail;
    private final String yes;
    private final String no;
    private boolean yesSelected;

    /**
     * @param yes the label of the button that goes ahead, such as "YES, QUIT"
     * @param no the label of the button that stays, such as "NO, BACK"
     */
    public Dialog(String question, String detail, String yes, String no) {
        this.question = question;
        this.detail = detail;
        this.yes = yes;
        this.no = no;
    }

    public Answer update(MenuInput input) {
        if (input.back()) {
            return Answer.NO;
        }
        if (input.confirm()) {
            return yesSelected ? Answer.YES : Answer.NO;
        }
        if (input.left() || input.right() || input.up() || input.down()) {
            yesSelected = !yesSelected;
            return Answer.NONE;
        }
        Pointer pointer = input.pointer();
        if (pointer.clicks(yesButton())) {
            yesSelected = true;
            return Answer.YES;
        }
        if (pointer.clicks(noButton())) {
            yesSelected = false;
            return Answer.NO;
        }
        if (pointer.hovers(yesButton())) {
            yesSelected = true;
        } else if (pointer.hovers(noButton())) {
            yesSelected = false;
        }
        return Answer.NONE;
    }

    /** The dialog's width: the question in the body font or the detail in the label font, padded. */
    public int width() {
        return Math.max(
                MIN_WIDTH, Math.max(question.length() * Fonts.BODY_CELL, detail.length() * Fonts.LABEL_CELL) + 40);
    }

    /** The dialog's rectangle, centred on the screen. */
    public Box box() {
        int width = width();
        return new Box(
                Math.round((PixelScreen.WIDTH - width) / 2f),
                Math.round((PixelScreen.HEIGHT - HEIGHT) / 2f),
                width,
                HEIGHT);
    }

    /** The button that goes ahead, on the left. */
    public Box yesButton() {
        Box box = box();
        return new Box(box.x() + 12, box.y() + HEIGHT - 42, buttonWidth(), BUTTON_HEIGHT);
    }

    /** The button that stays, on the right. */
    public Box noButton() {
        Box box = box();
        return new Box(box.x() + 24 + buttonWidth(), box.y() + HEIGHT - 42, buttonWidth(), BUTTON_HEIGHT);
    }

    private float buttonWidth() {
        return (width() - 36) / 2f;
    }

    public String question() {
        return question;
    }

    public String detail() {
        return detail;
    }

    public String yes() {
        return yes;
    }

    public String no() {
        return no;
    }

    public boolean yesSelected() {
        return yesSelected;
    }
}
