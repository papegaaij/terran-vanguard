package vanguard.game.hangar;

import java.util.Map;
import java.util.Optional;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.game.hangar.HangarState.Command;
import vanguard.game.hangar.HangarState.Focus;
import vanguard.game.hangar.HangarState.Hover;
import vanguard.game.hangar.HangarState.Outcome;
import vanguard.game.hangar.HangarState.RepairLine;
import vanguard.game.hangar.HangarState.Target;
import vanguard.game.input.Pointer;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Box;
import vanguard.sim.WingmanSpec;

/**
 * The hangar with the mouse (design/ui/hangar, Mouse): hovering a shop row, a choice chip, a slot or a
 * command only gives it the hover look (the user's playtest of 2026-10-19: hover used to select, so the
 * selection followed the pointer on its way from a row to that row's choices); a click selects a row or a
 * slot (its callout or tile, M7 part A: the bought third bay's tile too), a click on a choice or a command
 * makes it as confirm does, a click on the bay row's chip buys or sells the bay, and the wheel over the
 * shop list scrolls it. While the pointer rests on one of the selected row's choices the test fire and
 * the power bar preview it ({@link HangarState#previewChoice()}). While the repair panel is open only it
 * takes the mouse: a click picks the ship's or Rook's line, a click or a drag on the slider sets the
 * points and a click on its button repairs. The layout of the command bar and the repair panel lives
 * here, for the drawing and the hit regions alike.
 */
public final class HangarPointer {
    /** What the mouse did in a frame: the outcome for the screen's sound, and whether it confirmed. */
    public record Result(Outcome outcome, boolean confirm) {
        static final Result NONE = new Result(Outcome.NONE, false);
    }

    private static final int REPAIR_WIDTH = 400;
    private static final int SLIDER_WIDTH = 260;

    /** Whether the repair slider follows the held button. */
    private boolean dragging;

    /** A command's chip in the top bar: undo, repair and save, then the larger launch. */
    public static Box command(Command command) {
        return command == Command.LAUNCH
                ? new Box(814, 10, 124, 32)
                : new Box(448 + command.ordinal() * 70, 15, 64, 22);
    }

    /** The repair panel, centred: one line per ship, with Rook's once he is hired. */
    public static Box repairPanel(boolean rook) {
        int height = 124 + 16 * (rook ? 2 : 1);
        return new Box(
                (PixelScreen.WIDTH - REPAIR_WIDTH) / 2f, (PixelScreen.HEIGHT - height) / 2f, REPAIR_WIDTH, height);
    }

    /** A line of the repair panel: the ship's first, then Rook's. */
    public static Box repairLine(boolean rook, int index) {
        Box panel = repairPanel(rook);
        return new Box(panel.x() + 12, panel.y() + 28 + 16 * index, panel.width() - 24, 16);
    }

    /** The y of the repair panel's rate line, under its lines. */
    public static float repairRateY(boolean rook) {
        return repairPanel(rook).y() + 32 + 16 * (rook ? 2 : 1);
    }

    /** The repair panel's slider track (10 px high). */
    public static Box repairSlider(boolean rook) {
        Box panel = repairPanel(rook);
        return new Box(panel.x() + 12, repairRateY(rook) + 24, SLIDER_WIDTH, 10);
    }

    /** The repair panel's button, which repairs. */
    public static Box repairButton(boolean rook) {
        Box panel = repairPanel(rook);
        return new Box(panel.x() + 12, repairRateY(rook) + 52, panel.width() - 24, 24);
    }

    /**
     * Follows the mouse for a frame: what the pointer rests on gets the hover look ({@link
     * HangarState#hover()}), and only a click selects or confirms.
     */
    public Result update(HangarState state, Pointer pointer) {
        if (state.focus() == Focus.REPAIR) {
            state.hover(Optional.empty());
            return repair(state, pointer);
        }
        dragging = false;
        boolean scrolled = ShopPanel.list().contains(pointer.x(), pointer.y())
                && pointer.wheel() != 0
                && state.scroll(pointer.wheel());
        if (!pointer.moved() && !pointer.clicked() && !scrolled) {
            return Result.NONE;
        }
        state.hover(over(state, pointer));
        if (!pointer.clicked()) {
            return Result.NONE;
        }
        for (Command command : Command.values()) {
            if (pointer.clicks(command(command))) {
                if (!state.enabled(command)) {
                    return Result.NONE;
                }
                state.pointCommand(command);
                return new Result(Outcome.NONE, true);
            }
        }
        for (Map.Entry<LoadoutSlot, Box> slot : slots(state).entrySet()) {
            if (pointer.clicks(slot.getValue())) {
                return new Result(state.pointSlot(slot.getKey()), false);
            }
        }
        for (int shown = 0; shown < HangarState.VISIBLE_ROWS; shown++) {
            int index = state.top() + shown;
            if (index < state.rowCount() && pointer.clicks(ShopPanel.row(shown))) {
                return new Result(state.pointRow(index), false);
            }
        }
        return detail(state, pointer);
    }

    private static Map<LoadoutSlot, Box> slots(HangarState state) {
        return LoadoutPanel.slots(state.hangar().escortHired(), state.slots().contains(LoadoutSlot.UTILITY_3));
    }

    /** What the pointer rests on: a command, a slot, a shown row or one of the selected row's chips. */
    static Optional<Hover> over(HangarState state, Pointer pointer) {
        for (Command command : Command.values()) {
            if (pointer.over(command(command)) && state.enabled(command)) {
                return Optional.of(new Hover(Target.COMMAND, command.ordinal()));
            }
        }
        for (Map.Entry<LoadoutSlot, Box> slot : slots(state).entrySet()) {
            int index = state.slots().indexOf(slot.getKey());
            if (index >= 0 && pointer.over(slot.getValue())) {
                return Optional.of(new Hover(Target.SLOT, index));
            }
        }
        for (int shown = 0; shown < HangarState.VISIBLE_ROWS; shown++) {
            int index = state.top() + shown;
            if (index < state.rowCount() && pointer.over(ShopPanel.row(shown))) {
                return Optional.of(new Hover(Target.ROW, index));
            }
        }
        if (state.baySelected()) {
            return pointer.over(ShopPanel.bayChip(state.bayRow().orElseThrow()))
                    ? Optional.of(new Hover(Target.BAY, 0))
                    : Optional.empty();
        }
        if (state.sideSelected()) {
            for (WingmanSpec.Side side : WingmanSpec.Side.values()) {
                if (pointer.over(ShopPanel.sideChip(side))) {
                    return Optional.of(new Hover(Target.SIDE, side.ordinal()));
                }
            }
            return Optional.empty();
        }
        Optional<Offer> offer = state.selected().filter(o -> o.state() != Hangar.State.LOCKED);
        for (int i = 0; offer.isPresent() && i < offer.get().choices().size(); i++) {
            if (pointer.over(ShopPanel.choiceChip(offer.get(), i))) {
                return Optional.of(new Hover(Target.CHOICE, i));
            }
        }
        return Optional.empty();
    }

    /**
     * A click on the selected row's chips: its choices, on the side row Rook's side, on the bay row its
     * buy or sell. It gives the focus back to the shop when the command bar had it.
     */
    private static Result detail(HangarState state, Pointer pointer) {
        if (state.baySelected()) {
            // M7 part A: the bay row's one chip; a click buys or (asking first) sells, as confirm does.
            return pointer.clicks(ShopPanel.bayChip(state.bayRow().orElseThrow())) && state.pointShop()
                    ? new Result(Outcome.NONE, true)
                    : Result.NONE;
        }
        if (state.sideSelected()) {
            for (WingmanSpec.Side side : WingmanSpec.Side.values()) {
                if (pointer.clicks(ShopPanel.sideChip(side)) && state.pointShop()) {
                    return new Result(state.pointSide(side), false);
                }
            }
            return Result.NONE;
        }
        Optional<Offer> offer = state.selected().filter(o -> o.state() != Hangar.State.LOCKED);
        if (offer.isEmpty()) {
            return Result.NONE;
        }
        for (int i = 0; i < offer.get().choices().size(); i++) {
            if (pointer.clicks(ShopPanel.choiceChip(offer.get(), i)) && state.pointShop()) {
                state.pointChoice(i);
                return new Result(Outcome.NONE, true);
            }
        }
        return Result.NONE;
    }

    private Result repair(HangarState state, Pointer pointer) {
        boolean rook = state.hangar().escortHired();
        Box slider = repairSlider(rook);
        Box track = new Box(slider.x() - 6, slider.y() - 6, slider.width() + 12, slider.height() + 12);
        if (dragging && pointer.held()) {
            return new Result(state.pointRepair(share(slider, pointer.x())), false);
        }
        dragging = false;
        if (!pointer.clicked()) {
            return Result.NONE;
        }
        if (pointer.clicks(track)) {
            dragging = true;
            return new Result(state.pointRepair(share(slider, pointer.x())), false);
        }
        if (pointer.clicks(repairButton(rook))) {
            return new Result(Outcome.NONE, true);
        }
        RepairLine[] lines = RepairLine.values();
        for (int i = 0; i < (rook ? lines.length : 1); i++) {
            if (pointer.clicks(repairLine(rook, i))) {
                return new Result(state.pointRepairLine(lines[i]), false);
            }
        }
        return Result.NONE;
    }

    private static double share(Box slider, float x) {
        return (x - slider.x()) / slider.width();
    }
}
