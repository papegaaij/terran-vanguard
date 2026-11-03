package vanguard.game.ui;

import java.util.ArrayList;
import java.util.List;
import vanguard.game.input.MenuInput;
import vanguard.game.input.Pointer;
import vanguard.game.render.PixelScreen;

/**
 * A confirmation (design/ui, the UI kit's confirm dialog): a question, a line of detail and two
 * buttons. The cursor starts on the safe answer; Back answers no. With the mouse, hovering a button
 * selects it and a click on one answers; a click beside the buttons does nothing. The dialog's
 * layout (centred on the screen, as wide as its text needs) lives here, for the drawing and the
 * hit regions alike. The detail may hold several lines (split at {@code \n}, each wrapped at {@link
 * #DETAIL_CHARS}): the dialog grows a label line for each (the launch warnings, 2026-11-03).
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

    /** The most label characters a detail line holds before it wraps (the dialog stays inside the screen). */
    public static final int DETAIL_CHARS = 110;

    /** The step between the detail's lines. */
    public static final int DETAIL_LINE = 14;

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

    /** The dialog's width: the question in the body font or the longest detail line in the label font, padded. */
    public int width() {
        int longest = detailLines().stream().mapToInt(String::length).max().orElse(0);
        return Math.max(MIN_WIDTH, Math.max(question.length() * Fonts.BODY_CELL, longest * Fonts.LABEL_CELL) + 40);
    }

    /** The detail's lines: split at {@code \n}, each wrapped at {@link #DETAIL_CHARS}. */
    public List<String> detailLines() {
        List<String> lines = new ArrayList<>();
        for (String line : detail.split("\n", -1)) {
            if (line.length() <= DETAIL_CHARS) {
                lines.add(line);
            } else {
                lines.addAll(Words.wrap(line, DETAIL_CHARS));
            }
        }
        return lines;
    }

    /** The dialog's height: {@link #HEIGHT}, and a label line more for each detail line after the first. */
    public int height() {
        return HEIGHT + DETAIL_LINE * Math.max(0, detailLines().size() - 1);
    }

    /** The dialog's rectangle, centred on the screen. */
    public Box box() {
        int width = width();
        int height = height();
        return new Box(
                Math.round((PixelScreen.WIDTH - width) / 2f),
                Math.round((PixelScreen.HEIGHT - height) / 2f),
                width,
                height);
    }

    /** The button that goes ahead, on the left. */
    public Box yesButton() {
        Box box = box();
        return new Box(box.x() + 12, box.y() + box.height() - 42, buttonWidth(), BUTTON_HEIGHT);
    }

    /** The button that stays, on the right. */
    public Box noButton() {
        Box box = box();
        return new Box(box.x() + 24 + buttonWidth(), box.y() + box.height() - 42, buttonWidth(), BUTTON_HEIGHT);
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
