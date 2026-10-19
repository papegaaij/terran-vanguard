package vanguard.game.hangar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Action;
import vanguard.content.campaign.Hangar.Choice;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.Intel;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.sim.WingmanSpec;

/**
 * Where the player is in the hangar screen (design/ui/hangar) and what the keys do there, without
 * drawing: the selected loadout slot (previous / next tab), the shop row for it (up / down) and the
 * row's choice (left / right), or the command bar above the shop (undo, repair, save, launch), and
 * the repair panel with the points to repair. Confirm makes the choice; the shop's rules are
 * {@link Hangar}'s. Selling asks first, so it comes back as {@link Outcome#SELL} for the screen's
 * dialog and is made with {@link #sell()}.
 *
 * <p>Once Rook is hired (M5 part A, design/ui/hangar, Escort) the escort is one of the slots the tabs
 * cycle (after the rear mount): its rows are his guns, and a last row sets his side (left / right
 * pick it); the repair panel has a {@code SHIP} and a {@code ROOK} line (the tabs switch between
 * them), and the launch warns about his armour below 50 % or a grounded Rook.
 *
 * <p>M7 part A: the utility slots' shops end with a bay row (design/player/systems, Third utility
 * bay): in the starting bays' shops the bay for sale ({@code BUY}), in a bought bay's own shop that bay
 * ({@code SELL}, refused while a module is fitted in it; selling asks first like an item). A bought
 * bay's slot joins the slots the tabs cycle, after the second bay. A cooldown special's rows are the
 * shop's ({@code BUY} once, {@code SELL}).
 *
 * <p>The mouse ({@link HangarPointer}) reaches the same places directly: {@code point...} select a
 * slot, a row, a choice, a command, a repair line or the repair points, and the wheel scrolls the
 * shop list.
 */
public final class HangarState {
    /**
     * The slots in the order the tabs cycle through them: the mounts, the core parts, the special and the
     * two starting bays; a bought bay's slot follows them ({@link #slots()}).
     */
    public static final List<LoadoutSlot> SLOTS = List.of(
            LoadoutSlot.FRONT,
            LoadoutSlot.LEFT_WING,
            LoadoutSlot.RIGHT_WING,
            LoadoutSlot.REAR,
            LoadoutSlot.GENERATOR,
            LoadoutSlot.SHIELD,
            LoadoutSlot.ARMOUR,
            LoadoutSlot.ENGINE,
            LoadoutSlot.SPECIAL,
            LoadoutSlot.UTILITY_1,
            LoadoutSlot.UTILITY_2);

    /** The ship's slots and the escort's (once Rook is hired), in the order the tabs cycle them. */
    static final List<LoadoutSlot> ESCORT_SLOTS = withEscort();

    private static List<LoadoutSlot> withEscort() {
        List<LoadoutSlot> slots = new ArrayList<>(SLOTS);
        slots.add(slots.indexOf(LoadoutSlot.REAR) + 1, LoadoutSlot.ESCORT);
        return List.copyOf(slots);
    }

    /** The repair panel's lines: the Stormhawk's armour and Rook's. */
    public enum RepairLine {
        SHIP,
        ROOK
    }

    /** The shop list shows this many rows; the rest scrolls. */
    public static final int VISIBLE_ROWS = 8;

    public enum Focus {
        COMMANDS,
        SHOP,
        REPAIR
    }

    public enum Command {
        UNDO,
        REPAIR,
        SAVE,
        LAUNCH
    }

    /** What a key did, for the screen's sound and next step. */
    public enum Outcome {
        NONE,
        MOVED,
        DONE,
        REFUSED,
        /** The selected choice sells: the screen asks first. */
        SELL,
        SAVE,
        LAUNCH
    }

    private final Hangar hangar;
    private final boolean launchable;
    private int slot;
    private int row;
    private int top;
    private int choice;
    private Focus focus = Focus.SHOP;
    private Command command = Command.LAUNCH;
    private int repairPoints;
    private RepairLine repairLine = RepairLine.SHIP;
    private String message = "";
    /** M7 part A: the notice the message line shows until the next move, empty once it is gone. */
    private String notice = "";

    /** @param launchable whether the next level is built */
    public HangarState(Hangar hangar, boolean launchable) {
        this.hangar = hangar;
        this.launchable = launchable;
    }

    public Hangar hangar() {
        return hangar;
    }

    /**
     * The slots the tabs cycle: the escort's among them once Rook is hired, and (M7 part A) the bought
     * utility bays' after the starting ones.
     */
    public List<LoadoutSlot> slots() {
        List<LoadoutSlot> base = hangar.escortHired() ? ESCORT_SLOTS : SLOTS;
        if (!hangar.campaign().gear().open(LoadoutSlot.UTILITY_3)) {
            return base;
        }
        List<LoadoutSlot> slots = new ArrayList<>(base);
        slots.add(LoadoutSlot.UTILITY_3);
        return slots;
    }

    public LoadoutSlot slot() {
        List<LoadoutSlot> slots = slots();
        return slots.get(Math.min(slot, slots.size() - 1));
    }

    /** Whether the escort's shop shows its side row after his guns. */
    private boolean hasSideRow() {
        return slot() == LoadoutSlot.ESCORT && hangar.escortHired();
    }

    /** The rows the list shows: the shop's, and the escort's side row or a utility slot's bay row. */
    public int rowCount() {
        return rows().size() + (hasSideRow() || bayRow().isPresent() ? 1 : 0);
    }

    /**
     * M7 part A: the utility slot's bay row, after its shop rows: in a bought bay's own slot that bay's
     * line (to sell it), in another utility slot the next bay for sale (to buy it); empty elsewhere, and
     * before the bay is for sale (it is not listed while locked).
     */
    public Optional<Hangar.BayOffer> bayRow() {
        LoadoutSlot shown = slot();
        if (shown.kind() != ItemKind.UTILITY) {
            return Optional.empty();
        }
        List<Hangar.BayOffer> lines = hangar.bays();
        Optional<Hangar.BayOffer> own = lines.stream()
                .filter(line -> line.bay().slot() == shown && line.state() == Hangar.State.OWNED)
                .findFirst();
        if (own.isPresent()) {
            return own;
        }
        return lines.stream()
                .filter(line -> line.state() == Hangar.State.BUYABLE
                        && line.refusal()
                                .filter(Hangar.BayRefusal.ORDER::equals)
                                .isEmpty())
                .findFirst();
    }

    /** M7 part A: whether the bay row (after a utility slot's shop rows) is selected. */
    public boolean baySelected() {
        return row == rows().size() && bayRow().isPresent();
    }

    /** M7 part A: what the bay row offers: {@link Action#BUY} for a bay for sale, {@link Action#SELL} for a bought one. */
    static Action bayAction(Hangar.BayOffer line) {
        return line.state() == Hangar.State.OWNED ? Action.SELL : Action.BUY;
    }

    /**
     * The action the selected row's highlighted choice makes, for the screen's sound: the shop row's
     * choice, or the bay row's buy or sell.
     */
    public Optional<Action> selectedAction() {
        if (baySelected()) {
            return bayRow().map(HangarState::bayAction);
        }
        return choice().map(Choice::action);
    }

    /**
     * A sale the screen asks about.
     *
     * @param name what is sold, upper case
     * @param refund the credits it returns
     * @param bought it was bought during this visit, so all it cost comes back
     */
    public record Sale(String name, int refund, boolean bought) {}

    /** The sale the selected row's highlighted choice makes; empty when it is not a sale. */
    public Optional<Sale> sale() {
        if (baySelected()) {
            return bayRow().filter(line -> bayAction(line) == Action.SELL)
                    .map(line -> new Sale(Names.slot(line.bay().slot()), -line.credits(), line.bought()));
        }
        Optional<Offer> offer = selected();
        return choice().filter(chosen -> chosen.action() == Action.SELL)
                .flatMap(chosen ->
                        offer.map(sold -> new Sale(Names.of(sold.item().name()), -chosen.credits(), sold.bought())));
    }

    /** Whether the escort's side row (after his guns) is selected. */
    public boolean sideSelected() {
        return hasSideRow() && row == rows().size();
    }

    /** Rook's side (left or right of the player), the escort's setting. */
    public WingmanSpec.Side escortSide() {
        return hangar.campaign().gear().escort().side();
    }

    /** The shop rows for the selected slot. */
    public List<Offer> rows() {
        return hangar.shop(slot());
    }

    public int row() {
        return row;
    }

    /** The first row the list shows. */
    public int top() {
        return top;
    }

    public Optional<Offer> selected() {
        List<Offer> rows = rows();
        return row < rows.size() ? Optional.of(rows.get(row)) : Optional.empty();
    }

    /** The selected row's selected choice. */
    public Optional<Choice> choice() {
        return selected().filter(offer -> !offer.choices().isEmpty()).map(offer -> offer.choices()
                .get(Math.min(choice, offer.choices().size() - 1)));
    }

    public Focus focus() {
        return focus;
    }

    public Command command() {
        return command;
    }

    public boolean enabled(Command candidate) {
        return switch (candidate) {
            case UNDO -> hangar.canUndo();
            case REPAIR -> hangar.missingArmour() > 0 || hangar.escortMissingArmour() > 0;
            case SAVE -> true;
            case LAUNCH -> launchable;
        };
    }

    public int repairPoints() {
        return repairPoints;
    }

    /** The repair panel's line the points are for. */
    public RepairLine repairLine() {
        return repairLine;
    }

    /** The points missing on a repair line. */
    public int missing(RepairLine line) {
        return line == RepairLine.SHIP ? hangar.missingArmour() : hangar.escortMissingArmour();
    }

    /** The most points of a repair line the credits pay for. */
    public int affordable(RepairLine line) {
        return line == RepairLine.SHIP ? hangar.affordableRepair() : hangar.affordableEscortRepair();
    }

    /**
     * Shows {@code text} on the shop's message line (the visit's free charges) until the next move, slot
     * change or transaction (M7 part A, round 37 R.4 g: it no longer stays on every slot).
     */
    public void notice(String text) {
        message = text;
        notice = text;
    }

    /** M7 part A: a move ({@link Outcome#MOVED}) takes the visit's notice off the message line. */
    private Outcome moved(Outcome outcome) {
        if (outcome == Outcome.MOVED && !notice.isEmpty()) {
            if (message.equals(notice)) {
                message = "";
            }
            notice = "";
        }
        return outcome;
    }

    /** The notice for a special's free charges: "1 FREE AIRSTRIKE CHARGE, FITTED". */
    public static String freeChargesNotice(Campaign.FreeCharges free) {
        String name = free.special().toUpperCase(Locale.ROOT);
        String charges =
                free.charges() == 1 ? "1 FREE " + name + " CHARGE" : free.charges() + " FREE " + name + " CHARGES";
        return free.fitted() ? charges + ", FITTED" : charges;
    }

    /** What the last transaction did or why it was refused, or the visit's notice. */
    public String message() {
        return message;
    }

    public Outcome nextSlot() {
        return moved(selectSlot(slot + 1));
    }

    public Outcome previousSlot() {
        return moved(selectSlot(slot - 1));
    }

    private Outcome selectSlot(int index) {
        if (focus == Focus.REPAIR) {
            return switchRepairLine();
        }
        slot = Math.floorMod(index, slots().size());
        row = 0;
        top = 0;
        choice = 0;
        focus = Focus.SHOP;
        return Outcome.MOVED;
    }

    public Outcome up() {
        return moved(
                switch (focus) {
                    case SHOP -> {
                        if (row == 0) {
                            focus = Focus.COMMANDS;
                            if (!enabled(command)) {
                                command = Command.SAVE;
                            }
                        } else {
                            selectRow(row - 1);
                        }
                        yield Outcome.MOVED;
                    }
                    case REPAIR -> changeRepair(10);
                    case COMMANDS -> Outcome.NONE;
                });
    }

    public Outcome down() {
        return moved(
                switch (focus) {
                    case COMMANDS -> {
                        focus = Focus.SHOP;
                        yield Outcome.MOVED;
                    }
                    case SHOP -> {
                        if (row + 1 >= rowCount()) {
                            yield Outcome.NONE;
                        }
                        selectRow(row + 1);
                        yield Outcome.MOVED;
                    }
                    case REPAIR -> changeRepair(-10);
                });
    }

    public Outcome left() {
        return moved(sideways(-1));
    }

    public Outcome right() {
        return moved(sideways(1));
    }

    private Outcome sideways(int direction) {
        return switch (focus) {
            case COMMANDS -> moveCommand(direction);
            case SHOP -> {
                if (sideSelected()) {
                    yield side(direction < 0 ? WingmanSpec.Side.LEFT : WingmanSpec.Side.RIGHT);
                }
                int choices = selected().map(offer -> offer.choices().size()).orElse(0);
                if (choices < 2) {
                    yield Outcome.NONE;
                }
                choice = Math.floorMod(Math.min(choice, choices - 1) + direction, choices);
                yield Outcome.MOVED;
            }
            case REPAIR -> changeRepair(direction);
        };
    }

    private Outcome moveCommand(int direction) {
        Command[] commands = Command.values();
        int index = command.ordinal();
        for (int i = 0; i < commands.length; i++) {
            index = Math.floorMod(index + direction, commands.length);
            if (enabled(commands[index])) {
                Command before = command;
                command = commands[index];
                return command == before ? Outcome.NONE : Outcome.MOVED;
            }
        }
        return Outcome.NONE;
    }

    /** Sets Rook's side (left / right on the side row); free and not part of the undo (design/ui/hangar). */
    private Outcome side(WingmanSpec.Side side) {
        if (!hangar.escortSide(side)) {
            return Outcome.NONE;
        }
        message = "ROOK FLIES ON YOUR " + side.name();
        return Outcome.MOVED;
    }

    /** The tabs in the repair panel: the other line, when it has points missing. */
    private Outcome switchRepairLine() {
        RepairLine other = repairLine == RepairLine.SHIP ? RepairLine.ROOK : RepairLine.SHIP;
        if (missing(other) == 0) {
            return Outcome.NONE;
        }
        repairLine = other;
        repairPoints = Math.max(1, affordable(other));
        return Outcome.MOVED;
    }

    /** The mouse: a click on a slot's callout or tile selects it, as the tabs do. */
    public Outcome pointSlot(LoadoutSlot chosen) {
        int index = slots().indexOf(chosen);
        if (focus == Focus.REPAIR || index < 0 || index == slot && focus == Focus.SHOP) {
            return Outcome.NONE;
        }
        if (index == slot) {
            focus = Focus.SHOP;
            return moved(Outcome.MOVED);
        }
        return moved(selectSlot(index));
    }

    /** The mouse: the pointer on a shop row selects it (the focus goes to the shop). */
    public Outcome pointRow(int index) {
        if (focus == Focus.REPAIR || index < 0 || index >= rowCount()) {
            return Outcome.NONE;
        }
        if (focus == Focus.SHOP && index == row) {
            return Outcome.NONE;
        }
        focus = Focus.SHOP;
        if (index != row) {
            selectRow(index);
        }
        return moved(Outcome.MOVED);
    }

    /** The mouse: the pointer on one of the selected row's choices highlights it, as left / right do. */
    public Outcome pointChoice(int index) {
        int choices = selected().map(offer -> offer.choices().size()).orElse(0);
        if (focus != Focus.SHOP || index < 0 || index >= choices || index == Math.min(choice, choices - 1)) {
            return Outcome.NONE;
        }
        choice = index;
        return moved(Outcome.MOVED);
    }

    /** Whether the bay row's chip shows: the bay row is selected and the shop has the focus (its click confirms). */
    public boolean bayChipShown() {
        return focus == Focus.SHOP && baySelected();
    }

    /** The mouse: a click on the side row's LEFT or RIGHT chip sets Rook's side. */
    public Outcome pointSide(WingmanSpec.Side side) {
        if (focus != Focus.SHOP || !sideSelected()) {
            return Outcome.NONE;
        }
        return side(side);
    }

    /** The mouse: the pointer on an enabled command of the command bar highlights it. */
    public Outcome pointCommand(Command chosen) {
        if (focus == Focus.REPAIR || !enabled(chosen) || focus == Focus.COMMANDS && command == chosen) {
            return Outcome.NONE;
        }
        focus = Focus.COMMANDS;
        command = chosen;
        return Outcome.MOVED;
    }

    /** The mouse: a click on a line of the repair panel picks it, as the tabs do there. */
    public Outcome pointRepairLine(RepairLine line) {
        if (focus != Focus.REPAIR || line == repairLine) {
            return Outcome.NONE;
        }
        return switchRepairLine();
    }

    /** The mouse: the repair slider set to a share (0..1) of the points the credits pay for. */
    public Outcome pointRepair(double share) {
        if (focus != Focus.REPAIR) {
            return Outcome.NONE;
        }
        int most = Math.max(1, affordable(repairLine));
        int points = Math.clamp(Math.round(Math.clamp(share, 0, 1) * most), 1, most);
        return changeRepair(points - repairPoints);
    }

    /**
     * The mouse wheel over the shop list: the shown rows scroll by {@code notches}, the selection
     * stays; returns whether the list moved.
     */
    public boolean scroll(int notches) {
        int scrolled = Math.clamp(top + notches, 0, Math.max(0, rowCount() - VISIBLE_ROWS));
        if (scrolled == top) {
            return false;
        }
        top = scrolled;
        return true;
    }

    private Outcome changeRepair(int points) {
        int changed = Math.clamp(repairPoints + points, 1, Math.max(1, affordable(repairLine)));
        if (changed == repairPoints) {
            return Outcome.NONE;
        }
        repairPoints = changed;
        return Outcome.MOVED;
    }

    private void selectRow(int index) {
        row = index;
        choice = 0;
        if (row < top) {
            top = row;
        } else if (row >= top + VISIBLE_ROWS) {
            top = row - VISIBLE_ROWS + 1;
        }
    }

    public Outcome confirm() {
        return switch (focus) {
            case COMMANDS -> runCommand();
            case SHOP -> sideSelected() ? side(escortSide().other()) : baySelected() ? chooseBay() : choose();
            case REPAIR -> repair();
        };
    }

    /** Back closes the repair panel; elsewhere it is the screen's (quit to the main menu). */
    public boolean back() {
        if (focus != Focus.REPAIR) {
            return false;
        }
        focus = Focus.COMMANDS;
        return true;
    }

    private Outcome runCommand() {
        if (!enabled(command)) {
            return Outcome.REFUSED;
        }
        return switch (command) {
            case UNDO -> {
                hangar.undo();
                message = "UNDONE - FULL REFUND";
                // An undone bay purchase closes its slot again.
                keepSlot();
                afterChange(Optional.empty());
                if (!hangar.canUndo()) {
                    moveCommand(1);
                }
                yield Outcome.DONE;
            }
            case REPAIR -> {
                // The ship's line first while it has points missing that the credits pay for, else Rook's.
                Optional<RepairLine> line = java.util.Arrays.stream(RepairLine.values())
                        .filter(candidate -> missing(candidate) > 0 && affordable(candidate) > 0)
                        .findFirst();
                if (line.isEmpty()) {
                    message = "NOT ENOUGH CREDITS FOR A REPAIR";
                    yield Outcome.REFUSED;
                }
                repairLine = line.get();
                repairPoints = affordable(repairLine);
                focus = Focus.REPAIR;
                yield Outcome.MOVED;
            }
            case SAVE -> Outcome.SAVE;
            case LAUNCH -> Outcome.LAUNCH;
        };
    }

    private Outcome repair() {
        int cost = repairPoints * hangar.repairCost();
        boolean rook = repairLine == RepairLine.ROOK;
        if (!(rook ? hangar.repairEscort(repairPoints) : hangar.repair(repairPoints))) {
            message = "NOT ENOUGH CREDITS FOR A REPAIR";
            return Outcome.REFUSED;
        }
        message =
                String.format(Locale.ROOT, "REPAIRED %s%d POINTS FOR CR %d", rook ? "ROOK: " : "", repairPoints, cost);
        focus = Focus.COMMANDS;
        if (!enabled(Command.REPAIR)) {
            moveCommand(1);
        }
        return Outcome.DONE;
    }

    private Outcome choose() {
        Optional<Offer> offer = selected();
        Optional<Choice> chosen = choice();
        if (offer.isEmpty() || chosen.isEmpty()) {
            return Outcome.NONE;
        }
        Choice made = chosen.get();
        if (!made.allowed()) {
            message = refusal(made);
            return Outcome.REFUSED;
        }
        if (made.action() == Action.SELL) {
            return Outcome.SELL;
        }
        return apply(offer.get(), made);
    }

    /** M7 part A: the bay row's choice: buy the bay for sale, or ask before selling the bought one. */
    private Outcome chooseBay() {
        Hangar.BayOffer line = bayRow().orElseThrow();
        if (!line.allowed()) {
            message = bayRefusal(line.refusal().orElseThrow());
            return Outcome.REFUSED;
        }
        if (bayAction(line) == Action.SELL) {
            return Outcome.SELL;
        }
        if (!hangar.buyBay(line.bay())) {
            message = bayRefusal(Hangar.BayRefusal.CREDITS);
            return Outcome.REFUSED;
        }
        message = "BOUGHT " + Names.slot(line.bay().slot()) + " - ITS SLOT IS OPEN";
        // M7 part A (round 37, R.4 g): the cursor goes to the bay's new slot (its tile), on its first
        // row, not to the last row left in this slot (a locked one).
        int opened = slots().indexOf(line.bay().slot());
        if (opened >= 0) {
            selectSlot(opened);
        } else {
            afterChange(Optional.empty());
        }
        return Outcome.DONE;
    }

    /** M7 part A: sells the bay of the selected bay row; its slot closes, so the tabs leave it. */
    private Outcome sellBay() {
        Hangar.BayOffer line = bayRow().orElseThrow();
        if (bayAction(line) != Action.SELL || !hangar.sellBay(line.bay())) {
            line.refusal().ifPresent(refusal -> message = bayRefusal(refusal));
            return Outcome.REFUSED;
        }
        message = "SOLD " + Names.slot(line.bay().slot()) + " FOR " + Names.credits(-line.credits());
        keepSlot();
        afterChange(Optional.empty());
        return Outcome.DONE;
    }

    /** After a bought bay's slot closed: the selection stays within the slots the tabs cycle. */
    private void keepSlot() {
        int last = slots().size() - 1;
        if (slot > last) {
            slot = last;
            row = 0;
            top = 0;
            choice = 0;
        }
    }

    /** Why the bay row's choice cannot be made, for the shop's message line. */
    static String bayRefusal(Hangar.BayRefusal refusal) {
        return switch (refusal) {
            case CREDITS -> "NOT ENOUGH CREDITS";
            case IN_USE -> "UNFIT ITS MODULE FIRST";
            case ORDER -> "BUY THE BAY BEFORE IT FIRST";
        };
    }

    /** Makes the sale the screen asked about. */
    public Outcome sell() {
        if (baySelected()) {
            return sellBay();
        }
        Optional<Offer> offer = selected();
        Optional<Choice> chosen = choice();
        if (offer.isEmpty() || chosen.isEmpty() || chosen.get().action() != Action.SELL) {
            return Outcome.NONE;
        }
        return apply(offer.get(), chosen.get());
    }

    private Outcome apply(Offer offer, Choice made) {
        if (!hangar.apply(slot(), offer, made.action())) {
            message = refusal(made);
            return Outcome.REFUSED;
        }
        String name = Names.of(offer.item().name());
        message = switch (made.action()) {
            case BUY ->
                made.fits()
                        ? "BOUGHT AND FITTED " + name
                        : "BOUGHT " + name + " - IN THE INVENTORY: " + shortfall(made);
            case UPGRADE -> name + " UPGRADED TO L" + (offer.level() + 1);
            case FIT -> name + " FITTED";
            case UNFIT -> name + " MOVED TO THE INVENTORY";
            case SELL -> "SOLD " + name + " FOR CR " + -made.credits();
            case BUY_CHARGE -> "BOUGHT A CHARGE OF " + name;
        };
        afterChange(
                made.action() == Action.SELL
                        ? Optional.empty()
                        : Optional.of(offer.item().name()));
        return Outcome.DONE;
    }

    /** Keeps the cursor on the item that changed, or on a row that still exists. */
    private void afterChange(Optional<String> item) {
        List<Offer> rows = rows();
        List<String> names = new ArrayList<>();
        rows.forEach(offer -> names.add(offer.item().name()));
        int index = item.map(names::indexOf).filter(found -> found >= 0).orElse(Math.min(row, rowCount() - 1));
        selectRow(Math.max(0, index));
        top = Math.min(top, Math.max(0, rowCount() - VISIBLE_ROWS));
    }

    /** The launch warning for a grounded Rook. */
    static final String ROOK_GROUNDED = "ROOK IS GROUNDED AND STAYS HOME";

    /**
     * The launch warning for a {@code required} trait no source brings (M5 part C): {@code NO
     * ANTI-GROUND SOURCE FITTED} (a weapon, Rook's gun or a special's charge would do); M5 part D:
     * {@code NO REAR WEAPON FITTED} for {@code rear}, which only a rear-slot weapon brings.
     */
    static String requiredWarning(String trait) {
        if (trait.equals(Hangar.REAR)) {
            return "NO REAR WEAPON FITTED";
        }
        return "NO " + trait.toUpperCase(Locale.ROOT) + " SOURCE FITTED";
    }

    /** The launch warning for Rook's armour below 50 %: {@code ROOK'S ARMOUR 34/80}. */
    static String rookArmour(double armour, double max) {
        return String.format(Locale.ROOT, "ROOK'S ARMOUR %d/%d", (int) Math.ceil(armour), (int) max);
    }

    /** Why a choice cannot be made, for the shop's message line. */
    static String refusal(Choice choice) {
        return switch (choice.refusal().orElseThrow()) {
            case CREDITS -> "NOT ENOUGH CREDITS";
            case POWER -> "NEEDS " + shortfall(choice) + " MORE POWER";
            case MAX_LEVEL -> "AT ITS HIGHEST LEVEL";
            case FULL -> "CARRIES ITS MOST CHARGES";
        };
    }

    private static String shortfall(Choice choice) {
        return Names.megawatts(choice.load() - choice.output());
    }

    /**
     * What the launch confirmation warns about (design/ui/hangar, Launch): a level's {@code required}
     * traits that no source brings, at every sensor level (M5 part C: Level 09's {@code anti-ground},
     * counting Rook's gun while he flies and an Airstrike or Smart Bomb charge); the missing
     * recommended traits the sensor level shows (none below the level that reveals them), armour
     * below 50 %, and once Rook is hired a grounded Rook (he stays home) or his armour below 50 %.
     */
    public List<String> launchWarnings(Optional<Intel> intel) {
        List<String> warnings = new ArrayList<>();
        intel.ifPresent(level -> {
            List<String> required = level.requiredTraits();
            hangar.missingRequired(required).forEach(trait -> warnings.add(requiredWarning(trait)));
            level.markedTraits().stream()
                    .filter(trait -> !required.contains(trait))
                    .filter(trait -> !hangar.fittedTraits().contains(trait))
                    .forEach(trait -> warnings.add("NO " + trait.toUpperCase(Locale.ROOT) + " WEAPON FITTED"));
        });
        if (hangar.campaign().armour() < hangar.campaign().maxArmour() / 2) {
            warnings.add("ARMOUR BELOW 50 %");
        }
        Campaign campaign = hangar.campaign();
        if (campaign.escortGrounded()) {
            warnings.add(ROOK_GROUNDED);
        } else if (campaign.escortArmourLow()) {
            warnings.add(rookArmour(campaign.gear().escort().armour(), campaign.escortMaxArmour()));
        }
        return warnings;
    }
}
