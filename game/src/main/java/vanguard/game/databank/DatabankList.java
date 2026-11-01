package vanguard.game.databank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.content.databank.Entry;
import vanguard.content.databank.Pages;
import vanguard.game.ui.Box;
import vanguard.game.ui.Fonts;

/**
 * Step DB2 (design/ui/databank, Screen, layout a): a tab's list on the left: its rows under their groups' rule headers
 * ({@code ACT VI - JOVIAN STORM}'s act label, {@code FRONT}, {@code CORE}), the known entries selectable, the unknown
 * ones ({@code ???}) skipped by the cursor as mission select's locked levels; {@value #VISIBLE} lines show and the
 * list scrolls with the keys, the wheel and the kit's markers. Plain state, no drawing.
 */
final class DatabankList {
    /** The list panel: left of the entry, under the tabs. */
    static final int X = 16;

    static final int Y = 82;
    static final int WIDTH = 300;
    static final int HEIGHT = 424;
    /** The count line ({@code 63 KNOWN - 3 UNKNOWN}) and the first line's top. */
    static final int COUNT_Y = Y + 10;

    static final int FIRST_Y = Y + 28;
    /** A line's height. */
    static final int ROW = 16;
    /** The lines that show: 24 (design/ui/databank). */
    static final int VISIBLE = 24;
    /** A row's text and its right column. */
    static final int TEXT_X = X + 14;

    static final int RIGHT = X + WIDTH - 12;

    /** A line: a group's header, or one of its rows. */
    sealed interface Line {
        record Header(String text) implements Line {}

        record Row(Pages.Row row) implements Line {}
    }

    private final Pages.TabList list;
    private final List<Line> lines = new ArrayList<>();
    private int selected = -1;
    private int top;

    DatabankList(Pages.TabList list) {
        this.list = list;
        String group = null;
        for (Pages.Row row : list.rows()) {
            if (!row.group().equals(group)) {
                group = row.group();
                lines.add(new Line.Header(group));
            }
            lines.add(new Line.Row(row));
        }
    }

    Pages.TabList tabList() {
        return list;
    }

    List<Line> lines() {
        return lines;
    }

    /** The line the cursor is on, -1 for none (no known entry). */
    int selected() {
        return selected;
    }

    int top() {
        return top;
    }

    /** The selected entry. */
    Optional<Entry> entry() {
        return selected < 0
                ? Optional.empty()
                : ((Line.Row) lines.get(selected)).row().entry();
    }

    /** The count line: {@code 63 KNOWN - 3 UNKNOWN}. */
    String count() {
        return list.known() + " KNOWN - " + list.unknown() + " UNKNOWN";
    }

    /** Whether line {@code index} is a known entry's row, which the cursor can take. */
    boolean selectable(int index) {
        return index >= 0
                && index < lines.size()
                && lines.get(index) instanceof Line.Row row
                && row.row().known();
    }

    /** The line of the entry {@code id}, -1 when the list does not hold it as a known row. */
    int lineOf(String id) {
        for (int i = 0; i < lines.size(); i++) {
            if (selectable(i)
                    && ((Line.Row) lines.get(i))
                            .row()
                            .entry()
                            .orElseThrow()
                            .id()
                            .equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Puts the cursor on {@code id} when the list holds it, else on the first {@code NEW} row, else on the first known
     * row (design/ui/databank, The ways in: the last entry opened in the tab, or the first new one).
     */
    void start(Optional<String> id) {
        int line = id.map(this::lineOf).orElse(-1);
        for (int i = 0; line < 0 && i < lines.size(); i++) {
            if (selectable(i) && ((Line.Row) lines.get(i)).row().fresh()) {
                line = i;
            }
        }
        for (int i = 0; line < 0 && i < lines.size(); i++) {
            if (selectable(i)) {
                line = i;
            }
        }
        select(line);
    }

    /** Puts the cursor on line {@code index} (a known row) and scrolls it into view. */
    boolean select(int index) {
        if (index == selected || index >= 0 && !selectable(index)) {
            return false;
        }
        selected = index;
        scroll();
        return true;
    }

    /** Moves the cursor to the next known row that way, without wrapping. */
    boolean move(int direction) {
        for (int i = selected + direction; i >= 0 && i < lines.size(); i += direction) {
            if (selectable(i)) {
                return select(i);
            }
        }
        return false;
    }

    /** The wheel: the first shown line after {@code notches}, stopping at either end. */
    boolean wheel(int notches) {
        int before = top;
        top = Math.clamp(top + notches, 0, Math.max(0, lines.size() - VISIBLE));
        return top != before;
    }

    /** Shows the lines from {@code first} on (as far as the list reaches), the cursor kept in view. */
    void scrollTo(int first) {
        top = Math.clamp(first, 0, Math.max(0, lines.size() - VISIBLE));
        if (selected >= 0 && (selected < top || selected >= top + VISIBLE)) {
            scroll();
        }
    }

    /** Keeps the cursor (and its group's header above it, when it fits) in view. */
    private void scroll() {
        int focus = Math.max(0, selected);
        if (focus - 1 < top) {
            top = Math.max(0, focus - 1);
        } else if (focus >= top + VISIBLE) {
            top = focus - VISIBLE + 1;
        }
    }

    /** Whether lines lie above or below the shown ones (the scroll markers). */
    boolean above() {
        return top > 0;
    }

    boolean below() {
        return top + VISIBLE < lines.size();
    }

    /** The top of the {@code shown}-th shown line's text. */
    static int lineY(int shown) {
        return FIRST_Y + shown * ROW;
    }

    /** The hit region of the {@code shown}-th shown line: its selection bar, the rows touching. */
    static Box row(int shown) {
        return Box.row(X + 2, WIDTH - 4, lineY(shown), Fonts.LABEL_CAP, ROW);
    }
}
