package vanguard.game.screen;

import java.util.List;
import java.util.Optional;
import vanguard.game.input.BindingSlot;
import vanguard.game.ui.Box;
import vanguard.game.ui.Fonts;

/**
 * Where the Options screen draws its tabs and rows, and what of it lies under the mouse pointer
 * (design/ui/options, Mouse): a tab, a row's label, one of a choice's chips, a slider's track (with
 * the share the pointer points at), a sound test's arrows or name, or a cell of the bindings table.
 */
final class OptionsLayout {
    static final int PANEL_X = 71;
    static final int PANEL_Y = 31;
    static final int PANEL_WIDTH = 818;
    static final int PANEL_HEIGHT = 472;
    static final int LEFT = 89;
    static final int VALUE_X = 380;
    static final int ROWS_Y = 100;
    static final int ROW_STEP = 40;
    static final int TABLE_Y = 112;
    static final int TABLE_STEP = 22;
    static final int SLIDER_WIDTH = 300;
    static final int CHIP_HEIGHT = 16;
    /** The x of the Controls table's columns: action, primary, alternative, gamepad. */
    static final int[] COLUMNS = {LEFT, 329, 499, 659};
    /** A cell's chip in the bindings table. */
    static final int CELL_WIDTH = 150;

    static final int TABS_X = 300;
    static final int TABS_Y = 42;
    static final int TAB_HEIGHT = 24;
    static final int TAB_GAP = 8;
    /** A sound test's chip: a click this near either end picks the previous or next entry. */
    static final int ARROW_WIDTH = 48;

    /** What the pointer is over. */
    sealed interface Hit {
        record Tab(int index) implements Hit {}

        /** A row, but none of its values: its label. */
        record Row(int row) implements Hit {}

        record Chip(int row, int chip) implements Hit {}

        record Slider(int row, double share) implements Hit {}

        /** A sound test's chip: -1 or +1 near its arrows, 0 on its name (play). */
        record Sound(int row, int direction) implements Hit {}

        record Cell(int row, BindingSlot slot) implements Hit {}
    }

    private OptionsLayout() {}

    /** A tab's chip. */
    static Box tab(List<OptionTabs.Tab> tabs, int index) {
        float x = TABS_X;
        for (int i = 0; i < index; i++) {
            x += tabWidth(tabs.get(i)) + TAB_GAP;
        }
        return new Box(x, TABS_Y, tabWidth(tabs.get(index)), TAB_HEIGHT);
    }

    private static int tabWidth(OptionTabs.Tab tab) {
        return tab.name().length() * Fonts.BODY_CELL + 20;
    }

    /** How many rows lead the tab as the bindings table. */
    static int tableRows(List<OptionRow> rows) {
        return (int) rows.stream().takeWhile(OptionRow.Remap.class::isInstance).count();
    }

    /** The y of a row's label: the bindings table's rows, then the others under it. */
    static int rowY(List<OptionRow> rows, int index) {
        int table = tableRows(rows);
        if (index < table) {
            return TABLE_Y + index * TABLE_STEP;
        }
        int first = table > 0 ? TABLE_Y + table * TABLE_STEP + 14 : ROWS_Y;
        return first + (index - table) * ROW_STEP;
    }

    /** A row's hit region: across the panel, the rows touching. */
    static Box row(List<OptionRow> rows, int index) {
        boolean binding = rows.get(index) instanceof OptionRow.Remap;
        return Box.row(
                PANEL_X + 2, PANEL_WIDTH - 4, rowY(rows, index), Fonts.BODY_CAP, binding ? TABLE_STEP : ROW_STEP);
    }

    /** A choice's chips, each as wide as its text plus padding. */
    static Box chip(OptionRow.Choice choice, int y, int index) {
        float x = VALUE_X;
        for (int i = 0; i < index; i++) {
            x += chipWidth(choice.chips().get(i)) + 6;
        }
        return new Box(x, y - 2, chipWidth(choice.chips().get(index)), CHIP_HEIGHT);
    }

    private static int chipWidth(String label) {
        return label.length() * Fonts.LABEL_CELL + 16;
    }

    /** A slider's hit region: its track, with room above and below for the knob. */
    static Box slider(int y) {
        return new Box(VALUE_X - 6, y - 6, SLIDER_WIDTH + 12, 24);
    }

    /** The share of a slider's range at the pointer's x, 0..1. */
    static double share(float x) {
        return Math.clamp((x - VALUE_X) / SLIDER_WIDTH, 0, 1);
    }

    /** A sound test's chip. */
    static Box sound(int y) {
        return new Box(VALUE_X, y - 2, SLIDER_WIDTH, CHIP_HEIGHT);
    }

    /** A cell of the bindings table, as wide as its chip. */
    static Box cell(BindingSlot slot, int y) {
        return new Box(COLUMNS[slot.ordinal() + 1] - 6, y - 3, CELL_WIDTH, 16);
    }

    /** What lies under the point on a tab. */
    static Optional<Hit> at(List<OptionTabs.Tab> tabs, int shown, float x, float y) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tab(tabs, i).contains(x, y)) {
                return Optional.of(new Hit.Tab(i));
            }
        }
        List<OptionRow> rows = tabs.get(shown).rows();
        for (int i = 0; i < rows.size(); i++) {
            int rowY = rowY(rows, i);
            Optional<Hit> value = value(rows.get(i), i, rowY, x, y);
            if (value.isPresent()) {
                return value;
            }
            if (row(rows, i).contains(x, y)) {
                return Optional.of(new Hit.Row(i));
            }
        }
        return Optional.empty();
    }

    private static Optional<Hit> value(OptionRow option, int row, int rowY, float x, float y) {
        switch (option) {
            case OptionRow.Choice choice -> {
                for (int k = 0; k < choice.chips().size(); k++) {
                    if (chip(choice, rowY, k).contains(x, y)) {
                        return Optional.of(new Hit.Chip(row, k));
                    }
                }
            }
            case OptionRow.Slider slider -> {
                if (slider(rowY).contains(x, y)) {
                    return Optional.of(new Hit.Slider(row, share(x)));
                }
            }
            case OptionRow.Sound sound -> {
                Box chip = sound(rowY);
                if (chip.contains(x, y)) {
                    int direction =
                            x < chip.x() + ARROW_WIDTH ? -1 : x >= chip.x() + chip.width() - ARROW_WIDTH ? 1 : 0;
                    return Optional.of(new Hit.Sound(row, direction));
                }
            }
            case OptionRow.Remap remap -> {
                for (BindingSlot slot : BindingSlot.values()) {
                    if (cell(slot, rowY).contains(x, y)) {
                        return Optional.of(new Hit.Cell(row, slot));
                    }
                }
            }
            case OptionRow.Button button -> {}
        }
        return Optional.empty();
    }
}
