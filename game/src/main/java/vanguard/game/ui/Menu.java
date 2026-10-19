package vanguard.game.ui;

import java.util.List;
import java.util.function.IntFunction;
import vanguard.game.input.MenuInput;
import vanguard.game.input.Pointer;

/**
 * A vertical menu list: items that can be disabled (shown, but the cursor passes over them), the
 * cursor on one enabled item, moved with up and down and wrapping at the ends. With the mouse,
 * hovering an enabled item selects it and a click on one confirms it; the screen gives each item's
 * hit region, the rectangle it draws the item's selection bar in.
 */
public final class Menu<T> {
    /** One entry; {@code id} tells the screen what was chosen. */
    public record Item<I>(I id, String label, boolean enabled) {
        public static <I> Item<I> of(I id, String label) {
            return new Item<>(id, label, true);
        }

        public static <I> Item<I> disabled(I id, String label) {
            return new Item<>(id, label, false);
        }
    }

    private final List<Item<T>> items;
    private int selected;

    public Menu(List<Item<T>> items) {
        this.items = List.copyOf(items);
        selected = this.items.indexOf(
                this.items.stream().filter(Item::enabled).findFirst().orElseThrow());
    }

    public List<Item<T>> items() {
        return items;
    }

    public int selected() {
        return selected;
    }

    public T selectedId() {
        return items.get(selected).id();
    }

    /** Moves the cursor by one enabled item; returns whether it moved. */
    public boolean move(int direction) {
        int before = selected;
        do {
            selected = Math.floorMod(selected + direction, items.size());
        } while (!items.get(selected).enabled());
        return selected != before;
    }

    /** Moves the cursor on up or down; returns whether it moved. */
    public boolean navigate(MenuInput input) {
        if (input.up()) {
            return move(-1);
        }
        if (input.down()) {
            return move(1);
        }
        return false;
    }

    /**
     * Moves the cursor on up or down, or onto the enabled item a moving pointer hovers; returns
     * whether it moved.
     *
     * @param rows each item's hit region by its index
     */
    public boolean navigate(MenuInput input, IntFunction<Box> rows) {
        if (navigate(input)) {
            return true;
        }
        Pointer pointer = input.pointer();
        if (!pointer.moved()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).enabled() && pointer.hovers(rows.apply(i))) {
                int before = selected;
                selected = i;
                return selected != before;
            }
        }
        return false;
    }

    /**
     * Whether the selected item is confirmed in this frame: by the confirm key, or by a click on an
     * enabled item, which selects it first. A click on a disabled item or beside the list does nothing.
     */
    public boolean confirmed(MenuInput input, IntFunction<Box> rows) {
        if (input.confirm()) {
            return true;
        }
        Pointer pointer = input.pointer();
        if (!pointer.clicked()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (pointer.clicks(rows.apply(i))) {
                if (!items.get(i).enabled()) {
                    return false;
                }
                selected = i;
                return true;
            }
        }
        return false;
    }
}
