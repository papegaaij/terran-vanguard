package vanguard.game.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import vanguard.content.campaign.HighScores;
import vanguard.game.input.MenuInput;
import vanguard.game.input.Pointer;

/**
 * The high scores' name entry (design/systems/scoring, High scores; chosen game-over-r08-a): a
 * letter grid of two rows of 20 keys (A–T; U–Z, 0–9, space, {@code -}, DEL and END) for the arrows
 * and the gamepad, the keyboard's typing, and the mouse. The name starts as the last name entered
 * (then the cursor waits on END, so confirm keeps it), otherwise empty with the cursor on A.
 *
 * <ul>
 *   <li>The arrows move the cursor (left and right wrap within a row, up and down change the row);
 *       confirm presses its key: a character is added, DEL removes the last one, END takes the name.
 *   <li>Typing adds A–Z (raised to upper case), 0–9, space and {@code -}; backspace removes the last
 *       character. A typed character puts the cursor on END, so Enter then takes the name.
 *   <li>Back (Esc, B, the right button) removes the last character.
 *   <li>The pointer hovers a key to select it; a click presses it.
 * </ul>
 *
 * A name holds at most 10 characters; END takes it only when it has 3 to 10 without the spaces at
 * either end ({@link HighScores#validName}).
 */
public final class NameEntry {
    /** Keys per row. */
    public static final int COLUMNS = 20;

    public static final String SPACE = "SP";
    public static final String DELETE = "DEL";
    public static final String END = "END";

    /** The grid's keys, row by row. */
    public static final List<String> KEYS = keys();

    /** What a frame's input did, for its sound. */
    public enum Result {
        NONE,
        MOVED,
        /** A character was added or removed. */
        EDITED,
        /** A key that cannot act now: a full name, an empty one to delete from, END on a short name. */
        REFUSED,
        DONE
    }

    private final StringBuilder name = new StringBuilder();
    private int selected;
    private boolean done;

    /** @param lastName the name entered last, offered again */
    public NameEntry(Optional<String> lastName) {
        lastName.filter(HighScores::validName).ifPresent(name::append);
        selected = name.isEmpty() ? 0 : KEYS.indexOf(END);
    }

    private static List<String> keys() {
        List<String> keys = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            keys.add(String.valueOf(c));
        }
        for (char c = '0'; c <= '9'; c++) {
            keys.add(String.valueOf(c));
        }
        keys.addAll(List.of(SPACE, "-", DELETE, END));
        if (keys.size() != 2 * COLUMNS) {
            throw new IllegalStateException("the grid has two full rows");
        }
        return Collections.unmodifiableList(keys);
    }

    /** The name as typed so far, upper case. */
    public String name() {
        return name.toString();
    }

    /** The name END takes, once it is {@link #done}: no spaces at either end. */
    public String result() {
        return HighScores.normalise(name.toString());
    }

    /** Whether END would take the name now. */
    public boolean valid() {
        return HighScores.validName(result());
    }

    /** The selected key's index in {@link #KEYS}. */
    public int selected() {
        return selected;
    }

    /** Whether END took the name. */
    public boolean done() {
        return done;
    }

    /**
     * Reads a frame's input.
     *
     * @param keys each key's hit region by its index, where the screen draws it
     */
    public Result update(MenuInput input, IntFunction<Box> keys) {
        if (done) {
            return Result.NONE;
        }
        Result result = Result.NONE;
        String typed = input.typed();
        for (int i = 0; i < typed.length(); i++) {
            char c = typed.charAt(i);
            Result step = Result.NONE;
            if (c == '\b' || c == 127) {
                step = delete();
            } else if (HighScores.allowed(c)) {
                step = add(Character.toUpperCase(c));
            }
            if (step != Result.NONE) {
                selected = KEYS.indexOf(END);
                result = step;
            }
        }
        if (input.back()) {
            return delete();
        }
        int column = selected % COLUMNS;
        int row = selected / COLUMNS;
        if (input.left()) {
            selected = row * COLUMNS + Math.floorMod(column - 1, COLUMNS);
            return Result.MOVED;
        }
        if (input.right()) {
            selected = row * COLUMNS + Math.floorMod(column + 1, COLUMNS);
            return Result.MOVED;
        }
        if (input.up() || input.down()) {
            selected = (1 - row) * COLUMNS + column;
            return Result.MOVED;
        }
        if (input.confirm()) {
            return press(selected);
        }
        Pointer pointer = input.pointer();
        for (int i = 0; i < KEYS.size(); i++) {
            Box key = keys.apply(i);
            if (pointer.clicks(key)) {
                selected = i;
                return press(i);
            }
            if (pointer.hovers(key) && selected != i) {
                selected = i;
                result = result == Result.NONE ? Result.MOVED : result;
            }
        }
        return result;
    }

    /** Presses a key of the grid. */
    Result press(int index) {
        String key = KEYS.get(index);
        return switch (key) {
            case DELETE -> delete();
            case END -> {
                if (!valid()) {
                    yield Result.REFUSED;
                }
                done = true;
                yield Result.DONE;
            }
            case SPACE -> add(' ');
            default -> add(key.charAt(0));
        };
    }

    private Result add(char c) {
        if (name.length() >= HighScores.MAX_NAME) {
            return Result.REFUSED;
        }
        name.append(c);
        return Result.EDITED;
    }

    private Result delete() {
        if (name.isEmpty()) {
            return Result.REFUSED;
        }
        name.setLength(name.length() - 1);
        return Result.EDITED;
    }
}
