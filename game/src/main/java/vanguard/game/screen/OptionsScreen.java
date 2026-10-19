package vanguard.game.screen;

import static vanguard.game.screen.OptionsLayout.CHIP_HEIGHT;
import static vanguard.game.screen.OptionsLayout.COLUMNS;
import static vanguard.game.screen.OptionsLayout.LEFT;
import static vanguard.game.screen.OptionsLayout.PANEL_HEIGHT;
import static vanguard.game.screen.OptionsLayout.PANEL_WIDTH;
import static vanguard.game.screen.OptionsLayout.PANEL_X;
import static vanguard.game.screen.OptionsLayout.PANEL_Y;
import static vanguard.game.screen.OptionsLayout.ROWS_Y;
import static vanguard.game.screen.OptionsLayout.ROW_STEP;
import static vanguard.game.screen.OptionsLayout.SLIDER_WIDTH;
import static vanguard.game.screen.OptionsLayout.TABLE_STEP;
import static vanguard.game.screen.OptionsLayout.TABLE_Y;
import static vanguard.game.screen.OptionsLayout.TABS_X;
import static vanguard.game.screen.OptionsLayout.TABS_Y;
import static vanguard.game.screen.OptionsLayout.TAB_GAP;
import static vanguard.game.screen.OptionsLayout.TAB_HEIGHT;
import static vanguard.game.screen.OptionsLayout.VALUE_X;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.List;
import java.util.Optional;
import vanguard.game.GameServices;
import vanguard.game.audio.Sfx;
import vanguard.game.audio.SoundTest;
import vanguard.game.input.Action;
import vanguard.game.input.Binding;
import vanguard.game.input.BindingSlot;
import vanguard.game.input.Bindings;
import vanguard.game.input.MenuInput;
import vanguard.game.input.Pointer;
import vanguard.game.settings.Settings;
import vanguard.game.ui.Fonts;
import vanguard.game.ui.Glass;
import vanguard.game.ui.Words;

/**
 * The Options screen (design/ui/options, chosen options-r08-a), the same from the main menu and
 * the pause menu: a glass panel over the dimmed title scene with the Video, Audio, Controls and
 * Gameplay tabs. Up and down select a row, left and right change it, Q / E or the bumpers switch
 * tabs, back closes the screen. Every change applies at once; the settings file is written when
 * the screen closes. The Audio tab's sound test stops when the screen closes.
 *
 * <p>With the mouse ({@link OptionsLayout}): a click on a tab shows it, hovering a row selects it, a
 * click sets a chip, a click or a drag on a slider's track sets its value, a sound test's arrows pick
 * and its name plays, and a click on a cell of the bindings table captures a key for it.
 */
public final class OptionsScreen implements GameScreen {
    private static final String[] COLUMN_NAMES = {"ACTION", "PRIMARY", "ALTERNATIVE", "GAMEPAD"};

    private final GameServices services;
    private final List<OptionTabs.Tab> tabs = OptionTabs.all();
    private final Remapping remapping = new Remapping();
    private final OptionsTarget target;
    private final SoundTest soundTest;
    private int tab;
    private int row;
    /** The column of the bindings table the cursor is in. */
    private BindingSlot column = BindingSlot.PRIMARY;
    /** The row whose slider the mouse drags; -1 for none. */
    private int dragging = -1;

    public OptionsScreen(GameServices services) {
        this.services = services;
        soundTest = new SoundTest(services.audio, services.files, services.mixer, services.sfx);
        target = new OptionsTarget() {
            @Override
            public Settings settings() {
                return services.settings();
            }

            @Override
            public void change(Settings changed) {
                services.change(changed);
            }

            @Override
            public boolean fullScreen() {
                return services.display.fullScreen();
            }

            @Override
            public void toggleFullScreen() {
                services.display.toggle();
            }

            @Override
            public SoundTest soundTest() {
                return soundTest;
            }
        };
    }

    @Override
    public Transition update(float seconds) {
        soundTest.update();
        MenuInput input = services.menu;
        if (remapping.active()) {
            Optional<Bindings> changed = remapping.update(
                    services.devices, input, services.settings().controls().bindings());
            changed.ifPresent(bindings -> {
                services.change(services.settings()
                        .withControls(services.settings().controls().withBindings(bindings)));
                services.play(Sfx.MENU_CONFIRM);
            });
            return Transition.STAY;
        }
        if (input.back()) {
            services.saveSettings();
            services.play(Sfx.MENU_BACK);
            return Transition.BACK;
        }
        if (input.previousTab() || input.nextTab()) {
            tab = Math.floorMod(tab + (input.nextTab() ? 1 : -1), tabs.size());
            row = 0;
            services.play(Sfx.MENU_MOVE);
            return Transition.STAY;
        }
        List<OptionRow> rows = tabs.get(tab).rows();
        if (pointer(input.pointer(), rows)) {
            return Transition.STAY;
        }
        if (input.up() || input.down()) {
            row = Math.floorMod(row + (input.down() ? 1 : -1), rows.size());
            services.play(Sfx.MENU_MOVE);
            return Transition.STAY;
        }
        OptionRow current = rows.get(row);
        int direction = input.right() ? 1 : input.left() ? -1 : 0;
        if (current instanceof OptionRow.Remap) {
            moveColumn(direction);
        } else if (direction != 0 && current.change(target, direction)) {
            services.play(current instanceof OptionRow.Slider slider ? slider.feedback() : Sfx.MENU_MOVE);
        }
        if (input.confirm()) {
            confirm(current);
        }
        return Transition.STAY;
    }

    /** Follows the mouse; returns whether a click acted, so the keys wait for the next frame. */
    private boolean pointer(Pointer pointer, List<OptionRow> rows) {
        if (dragging >= 0) {
            if (pointer.held() && rows.get(dragging) instanceof OptionRow.Slider slider) {
                if (slider.point(target, OptionsLayout.share(pointer.x()))) {
                    services.play(slider.feedback());
                }
                return true;
            }
            dragging = -1;
        }
        if (!pointer.moved() && !pointer.clicked()) {
            return false;
        }
        Optional<OptionsLayout.Hit> hit = OptionsLayout.at(tabs, tab, pointer.x(), pointer.y());
        if (hit.isEmpty()) {
            return false;
        }
        if (!pointer.clicked()) {
            hover(hit.get());
            return false;
        }
        switch (hit.get()) {
            case OptionsLayout.Hit.Tab chosen -> {
                if (chosen.index() != tab) {
                    tab = chosen.index();
                    row = 0;
                    services.play(Sfx.MENU_MOVE);
                }
            }
            case OptionsLayout.Hit.Row clicked -> {
                row = clicked.row();
                if (rows.get(row) instanceof OptionRow.Button button) {
                    confirm(button);
                }
            }
            case OptionsLayout.Hit.Chip chip -> {
                row = chip.row();
                OptionRow.Choice choice = (OptionRow.Choice) rows.get(row);
                if (choice.get().applyAsInt(target) != chip.chip()) {
                    choice.set().accept(target, chip.chip());
                    services.play(Sfx.MENU_MOVE);
                }
            }
            case OptionsLayout.Hit.Slider track -> {
                row = track.row();
                dragging = row;
                OptionRow.Slider slider = (OptionRow.Slider) rows.get(row);
                if (slider.point(target, track.share())) {
                    services.play(slider.feedback());
                }
            }
            case OptionsLayout.Hit.Sound sound -> {
                row = sound.row();
                OptionRow current = rows.get(row);
                if (sound.direction() == 0) {
                    confirm(current);
                } else if (current.change(target, sound.direction())) {
                    services.play(Sfx.MENU_MOVE);
                }
            }
            case OptionsLayout.Hit.Cell cell -> {
                row = cell.row();
                column = cell.slot();
                confirm(rows.get(row));
            }
        }
        return true;
    }

    /** Hovering selects the row under the pointer, and in the bindings table the cell. */
    private void hover(OptionsLayout.Hit hit) {
        int before = row;
        BindingSlot columnBefore = column;
        switch (hit) {
            case OptionsLayout.Hit.Tab chosen -> {}
            case OptionsLayout.Hit.Row over -> row = over.row();
            case OptionsLayout.Hit.Chip chip -> row = chip.row();
            case OptionsLayout.Hit.Slider track -> row = track.row();
            case OptionsLayout.Hit.Sound sound -> row = sound.row();
            case OptionsLayout.Hit.Cell cell -> {
                row = cell.row();
                column = cell.slot();
            }
        }
        if (row != before || column != columnBefore) {
            services.play(Sfx.MENU_MOVE);
        }
    }

    private void moveColumn(int direction) {
        if (direction == 0) {
            return;
        }
        int next = Math.clamp(column.ordinal() + direction, 0, BindingSlot.values().length - 1);
        if (next != column.ordinal()) {
            column = BindingSlot.values()[next];
            services.play(Sfx.MENU_MOVE);
        }
    }

    private void confirm(OptionRow current) {
        switch (current) {
            case OptionRow.Button button -> {
                button.action().accept(target);
                services.play(Sfx.MENU_CONFIRM);
            }
            case OptionRow.Remap remap -> {
                if (remap.action().remappable(column)) {
                    remapping.start(new Bindings.Assignment(remap.action(), column), services.devices);
                    services.play(Sfx.MENU_CONFIRM);
                }
            }
            case OptionRow.Sound sound -> soundTest.play(sound.kind());
            case OptionRow.Choice choice -> {}
            case OptionRow.Slider slider -> {}
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        Glass glass = services.glass;
        services.titleScene.draw(batch, 0.4f);
        glass.panel(batch, PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, 0.82f);
        glass.shadowed(batch, glass.fonts.heading, "OPTIONS", Glass.WHITE, LEFT, 46);
        float tabX = TABS_X;
        for (int i = 0; i < tabs.size(); i++) {
            String name = tabs.get(i).name();
            float width = Fonts.width(glass.fonts.body, name) + 20;
            glass.tab(batch, glass.fonts.body, name, tabX, TABS_Y, width, TAB_HEIGHT, i == tab);
            tabX += width + TAB_GAP;
        }
        glass.rule(batch, LEFT, PANEL_X + PANEL_WIDTH - 18, 77);
        OptionsTab drawn = new OptionsTab(batch, glass);
        List<OptionRow> rows = tabs.get(tab).rows();
        int firstOther =
                (int) rows.stream().takeWhile(OptionRow.Remap.class::isInstance).count();
        int y = firstOther > 0 ? drawn.table() : ROWS_Y;
        for (int i = firstOther; i < rows.size(); i++) {
            drawn.row(rows.get(i), y, i == row);
            y += ROW_STEP;
        }
        String note = remapping.message().orElse(tabs.get(tab).note());
        Color colour = remapping.active() ? Glass.ALERT : Glass.LABEL;
        List<String> lines = Words.wrap(note, (PANEL_WIDTH - 36) / Fonts.advance(glass.fonts.label));
        for (int i = 0; i < lines.size(); i++) {
            glass.shadowed(batch, glass.fonts.label, lines.get(i), colour, LEFT, y + 4 + i * 14);
        }
        glass.hints(batch, "UP/DOWN SELECT    LEFT/RIGHT CHANGE    Q/E TAB    ESC BACK");
    }

    /** Draws the rows of the shown tab. */
    private final class OptionsTab {
        private final SpriteBatch batch;
        private final Glass glass;

        OptionsTab(SpriteBatch batch, Glass glass) {
            this.batch = batch;
            this.glass = glass;
        }

        void row(OptionRow option, int y, boolean selected) {
            glass.item(
                    batch, glass.fonts.body, option.label(), LEFT, y, PANEL_X + 2, PANEL_WIDTH - 4, 28, selected, true);
            Settings settings = services.settings();
            switch (option) {
                case OptionRow.Choice choice -> {
                    int on = choice.get().applyAsInt(target);
                    glass.chips(batch, choice.chips().toArray(String[]::new), on, VALUE_X, y - 2, CHIP_HEIGHT);
                    String note = choice.note().get(on);
                    if (!note.isEmpty()) {
                        glass.shadowed(batch, glass.fonts.label, note, Glass.LABEL, VALUE_X, y + 20);
                    }
                }
                case OptionRow.Slider slider ->
                    glass.slider(
                            batch,
                            VALUE_X,
                            y + 1,
                            SLIDER_WIDTH,
                            slider.share(settings),
                            slider.format().apply(slider.get().applyAsDouble(settings)));
                case OptionRow.Sound sound -> sound(sound.kind(), y);
                case OptionRow.Button button -> {}
                case OptionRow.Remap remap -> {}
            }
        }

        /** A sound test list: the picked entry between arrows, its place in the list and what it is. */
        private void sound(SoundTest.Kind kind, int y) {
            List<String> names = soundTest.names(kind);
            int picked = soundTest.picked(kind);
            boolean playing = kind == SoundTest.Kind.MUSIC && soundTest.playing();
            glass.chip(
                    batch,
                    glass.fonts.label,
                    "< " + names.get(picked) + " >",
                    VALUE_X,
                    y - 2,
                    SLIDER_WIDTH,
                    CHIP_HEIGHT,
                    playing,
                    true);
            String place = (picked + 1) + " / " + names.size();
            glass.shadowed(batch, glass.fonts.label, place, Glass.LABEL, VALUE_X + SLIDER_WIDTH + 12, y + 2);
            if (playing) {
                glass.shadowed(batch, glass.fonts.label, "PLAYING", Glass.GREEN, VALUE_X + SLIDER_WIDTH + 72, y + 2);
            }
            glass.shadowed(batch, glass.fonts.label, soundTest.note(kind), Glass.LABEL, VALUE_X, y + 20);
        }

        /** The bindings table of the Controls tab; returns the y below it. */
        int table() {
            for (int c = 0; c < COLUMNS.length; c++) {
                glass.shadowed(batch, glass.fonts.label, COLUMN_NAMES[c], Glass.LABEL, COLUMNS[c], TABLE_Y - 14);
            }
            List<OptionRow> rows = tabs.get(tab).rows();
            int y = TABLE_Y;
            for (int i = 0; i < rows.size() && rows.get(i) instanceof OptionRow.Remap remap; i++) {
                bindingRow(remap.action(), y, i == row);
                y += TABLE_STEP;
            }
            return y + 14;
        }

        private void bindingRow(Action action, int y, boolean selected) {
            glass.row(batch, PANEL_X + 18, y - 4, PANEL_WIDTH - 36, 20);
            glass.item(batch, glass.fonts.body, Remapping.name(action), LEFT, y, PANEL_X + 2, 230, 20, selected, true);
            Binding binding = services.settings().controls().bindings().get(action);
            String[] cells = {
                // M7 part A: Dash's primary input is the double-tap of a direction, fixed text.
                action == Action.DASH ? Remapping.DOUBLE_TAP : Remapping.keyName(binding.primaryKey()),
                Remapping.keyName(binding.alternativeKey()),
                Remapping.gamepadName(action, binding)
            };
            for (BindingSlot slot : BindingSlot.values()) {
                int x = COLUMNS[slot.ordinal() + 1];
                boolean capturing = remapping
                        .target()
                        .filter(t -> t.action() == action && t.slot() == slot)
                        .isPresent();
                boolean cursor = selected && slot == column;
                boolean fixed = !action.remappable(slot);
                if (capturing) {
                    glass.chip(batch, "PRESS...", x - 6, y - 3, 150, 16, true);
                } else if (cursor) {
                    glass.chip(batch, glass.fonts.label, cells[slot.ordinal()], x - 6, y - 3, 150, 16, true, !fixed);
                } else {
                    glass.shadowed(
                            batch, glass.fonts.label, cells[slot.ordinal()], fixed ? Glass.DIM : Glass.CYAN, x, y + 1);
                }
            }
        }
    }

    @Override
    public void dispose() {
        soundTest.close();
    }
}
