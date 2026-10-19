package vanguard.game.hangar;

import java.util.Map;
import java.util.Optional;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.game.hangar.HangarState.Command;
import vanguard.game.hangar.HangarState.Focus;
import vanguard.game.hangar.HangarState.Outcome;
import vanguard.game.hangar.HangarState.RepairLine;
import vanguard.game.input.Pointer;
import vanguard.game.render.PixelScreen;
import vanguard.game.ui.Box;
import vanguard.sim.WingmanSpec;

/**
 * The hangar with the mouse (design/ui/hangar, Mouse): hovering a shop row, a choice chip or a
 * command highlights it as the keys would (so the test fire and the power bar follow the pointer),
 * a click on a choice or a command makes it as confirm does, a click on a slot's callout or tile
 * selects the slot as the tabs do (M7 part A: the bought third bay's tile too), a click on the bay row's
 * chip buys or sells the bay, and the wheel over the shop list scrolls it. While the repair
 * panel is open only it takes the mouse: a click picks the ship's or Rook's line, a click or a drag
 * on the slider sets the points and a click on its button repairs. The layout of the command bar
 * and the repair panel lives here, for the drawing and the hit regions alike.
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

    /** Follows the mouse for a frame. */
    public Result update(HangarState state, Pointer pointer) {
        if (state.focus() == Focus.REPAIR) {
            return repair(state, pointer);
        }
        dragging = false;
        if (ShopPanel.list().contains(pointer.x(), pointer.y()) && pointer.wheel() != 0) {
            state.scroll(pointer.wheel());
        }
        if (!pointer.moved() && !pointer.clicked()) {
            return Result.NONE;
        }
        for (Command command : Command.values()) {
            Box chip = command(command);
            if (pointer.clicks(chip)) {
                if (!state.enabled(command)) {
                    return Result.NONE;
                }
                state.pointCommand(command);
                return new Result(Outcome.NONE, true);
            }
            if (pointer.hovers(chip)) {
                return new Result(state.pointCommand(command), false);
            }
        }
        for (Map.Entry<LoadoutSlot, Box> slot : LoadoutPanel.slots(
                        state.hangar().escortHired(), state.slots().contains(LoadoutSlot.UTILITY_3))
                .entrySet()) {
            if (pointer.clicks(slot.getValue())) {
                return new Result(state.pointSlot(slot.getKey()), false);
            }
        }
        for (int shown = 0; shown < HangarState.VISIBLE_ROWS; shown++) {
            int index = state.top() + shown;
            Box row = ShopPanel.row(shown);
            if (index < state.rowCount() && (pointer.hovers(row) || pointer.clicks(row))) {
                return new Result(state.pointRow(index), false);
            }
        }
        return detail(state, pointer);
    }

    /** The selected row's chips: its choices, on the side row Rook's side, on the bay row its buy or sell. */
    private static Result detail(HangarState state, Pointer pointer) {
        if (state.focus() != Focus.SHOP) {
            return Result.NONE;
        }
        if (state.bayChipShown()) {
            // M7 part A: the bay row's one chip; a click buys or (asking first) sells, as confirm does.
            return pointer.clicks(ShopPanel.bayChip(state.bayRow().orElseThrow()))
                    ? new Result(Outcome.NONE, true)
                    : Result.NONE;
        }
        if (state.sideSelected()) {
            for (WingmanSpec.Side side : WingmanSpec.Side.values()) {
                if (pointer.clicks(ShopPanel.sideChip(side))) {
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
            Box chip = ShopPanel.choiceChip(offer.get(), i);
            if (pointer.clicks(chip)) {
                state.pointChoice(i);
                return new Result(Outcome.NONE, true);
            }
            if (pointer.hovers(chip)) {
                return new Result(state.pointChoice(i), false);
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
