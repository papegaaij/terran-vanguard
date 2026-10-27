package vanguard.game.hangar;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Escort;
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
 * <p>M8 part A (design/player/wingmen, Heavy drone "Bulwark" and Rook's loss and absence): the escort's shop lists,
 * after Rook's guns and before the side row, the Bulwark's row ({@link #bulwarkRow()}): {@code BUY} while it is for
 * sale, once owned {@code FIT} (it flies instead of Rook) or {@code UNFIT} (Rook flies again) and {@code SELL} (asking
 * first like an item); all of it the visit's undo. While Rook is missing (Levels 27–29) his guns are not listed and
 * the owned Bulwark, or a free loaner, flies whatever the choice. The repair panel gains a {@code BULWARK} line for an
 * owned Bulwark, and Rook's line is gone while he is missing ({@link #repairLines()}).
 *
 * <p>The mouse ({@link HangarPointer}) reaches the same places directly: {@code point...} select a
 * slot, a row, a choice, a command, a repair line or the repair points, and the wheel scrolls the
 * shop list.
 *
 * <p>The replay hangar is refit only (the user's correction of 2026-10-19, design/ui/mission-select): its
 * rows are the owned items alone with their {@code FIT} and {@code UNFIT} choices (and a fitted weapon's power
 * dial, a free refit), the bay row and the
 * repair are gone, and nothing that spends or returns credits can be made ({@link #REFIT_ONLY}); Rook's
 * side stays.
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

    /** The repair panel's lines: the Stormhawk's armour, Rook's and (M8 part A) an owned Bulwark's. */
    public enum RepairLine {
        SHIP,
        ROOK,
        BULWARK
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

    /**
     * What the mouse pointer rests on (design/ui/hangar, Mouse; the user's playtest of 2026-10-19): drawn with
     * the hover look, a cyan outline, and never the selection, so moving the mouse from a clicked row to its
     * choices keeps that row selected. {@code index} is the shop row, the choice, the {@link Command}'s or
     * {@link WingmanSpec.Side}'s ordinal or the slot's index in {@link #slots()}; 0 for the bay chip.
     */
    public record Hover(Target target, int index) {}

    /** What a {@link Hover} is over. */
    public enum Target {
        ROW,
        CHOICE,
        COMMAND,
        SLOT,
        SIDE,
        BAY
    }

    /** The replay hangar's message line while nothing else is shown, and its refusal. */
    public static final String REFIT_ONLY = "REPLAY: REFIT ONLY";

    private final Hangar hangar;
    private final boolean launchable;
    /** The replay hangar: refitting with what the campaign owns, nothing bought, sold, upgraded or repaired. */
    private final boolean refitOnly;

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
    /** M8 AW: the visit's further notices, each shown in turn as the one before goes on a move. */
    private final ArrayDeque<String> notices = new ArrayDeque<>();
    /** What the pointer rests on, if anything. */
    private Optional<Hover> hover = Optional.empty();

    /** @param launchable whether the next level is built */
    public HangarState(Hangar hangar, boolean launchable) {
        this(hangar, launchable, false);
    }

    /**
     * @param launchable whether the next level is built
     * @param refitOnly the replay hangar (the user's correction of 2026-10-19): only the owned items are
     *     fitted and unfitted; nothing that costs or returns credits
     */
    public HangarState(Hangar hangar, boolean launchable, boolean refitOnly) {
        this.hangar = hangar;
        this.launchable = launchable;
        this.refitOnly = refitOnly;
    }

    /**
     * The hangar screen's state: for the campaign's next level, or (the replay hangar, refit only) for the mission
     * {@code replay} replays, whose escort it shows and fits: Rook, the Bulwark or none, as that mission's flight takes
     * it (the user's playtest of 2026-10-24: a replay of an earlier mission showed the Bulwark of Rook's absence).
     *
     * @param launchable whether the level is built
     */
    public static HangarState of(
            Catalogue catalogue, Campaign campaign, Optional<Campaign.Replay> replay, boolean launchable) {
        int level = replay.map(Campaign.Replay::level).orElse(campaign.nextLevel());
        return new HangarState(new Hangar(catalogue, campaign, level), launchable, replay.isPresent());
    }

    /** Whether this is the replay hangar, which only refits. */
    public boolean refitOnly() {
        return refitOnly;
    }

    /**
     * Whether an action is a refit, which the replay hangar allows: fitting or unfitting an owned item, and (the power
     * dial, 2026-10-21; stated default, design/ui/hangar) turning a fitted weapon's power down or up, which is free.
     */
    public static boolean refit(Action action) {
        return switch (action) {
            case FIT, UNFIT, POWER_DOWN, POWER_UP -> true;
            case BUY, UPGRADE, SELL, BUY_CHARGE -> false;
        };
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

    /**
     * The rows the list shows: the shop's, and the escort's Bulwark row (M8 part A) and side row or a utility slot's
     * bay row.
     */
    public int rowCount() {
        return rows().size() + (bulwarkRow().isPresent() ? 1 : 0) + (hasSideRow() || bayRow().isPresent() ? 1 : 0);
    }

    /**
     * M8 part A: the escort's Bulwark row, after Rook's guns: the Bulwark's shop line (locked with its unlock level,
     * for sale, or owned); empty in another slot and before Rook is hired, and in the replay hangar unless one is
     * owned and the replayed mission is from its unlock on (it is then only fitted and unfitted).
     */
    public Optional<Hangar.BulwarkOffer> bulwarkRow() {
        if (slot() != LoadoutSlot.ESCORT || !hangar.escortHired()) {
            return Optional.empty();
        }
        Hangar.BulwarkOffer line = hangar.bulwark();
        // The replay hangar: only an owned Bulwark, and only for a mission it can fly (from its unlock on).
        return refitOnly && (line.state() != Hangar.State.OWNED || hangar.level() < line.unlock())
                ? Optional.empty()
                : Optional.of(line);
    }

    /** M8 part A: whether the Bulwark row (after Rook's guns) is selected. */
    public boolean bulwarkSelected() {
        return row == rows().size() && bulwarkRow().isPresent();
    }

    /**
     * M8 part A: one of the Bulwark row's choices.
     *
     * @param action {@link Action#BUY}, {@link Action#FIT} (it flies instead of Rook), {@link Action#UNFIT} (Rook
     *     flies again) or {@link Action#SELL}
     * @param credits what it costs, or (negative) returns
     * @param refusal why it cannot be made now, for the message line; empty when it can
     */
    public record BulwarkChoice(Action action, int credits, Optional<String> refusal) {
        public boolean allowed() {
            return refusal.isEmpty();
        }
    }

    /** M8 part A: the Bulwark row's refusal while its price is more than the credits. */
    static final String BULWARK_CREDITS = "NOT ENOUGH CREDITS";

    /**
     * M8 part A: the Bulwark row's choices: none while it is locked; {@code BUY} while it is for sale; once owned
     * {@code FIT} or (while it is the slot's choice) {@code UNFIT}, then {@code SELL}. While Rook is missing the owned
     * Bulwark flies whatever the choice, so neither {@code FIT} nor {@code UNFIT} is offered. The replay hangar offers
     * only the refits.
     */
    public List<BulwarkChoice> bulwarkChoices() {
        Optional<Hangar.BulwarkOffer> shown = bulwarkRow();
        if (shown.isEmpty()) {
            return List.of();
        }
        Hangar.BulwarkOffer line = shown.get();
        List<BulwarkChoice> choices = new ArrayList<>();
        switch (line.state()) {
            case BUYABLE ->
                choices.add(new BulwarkChoice(
                        Action.BUY, line.credits(), line.refusal().map(refusal -> BULWARK_CREDITS)));
            case OWNED -> {
                if (!hangar.rookMissing()) {
                    choices.add(new BulwarkChoice(bulwarkFlies() ? Action.UNFIT : Action.FIT, 0, Optional.empty()));
                }
                if (!refitOnly) {
                    choices.add(new BulwarkChoice(Action.SELL, line.credits(), Optional.empty()));
                }
            }
            default -> {}
        }
        return choices;
    }

    /** M8 part A: the Bulwark row's highlighted choice. */
    public Optional<BulwarkChoice> bulwarkChoice() {
        List<BulwarkChoice> choices = bulwarkSelected() ? bulwarkChoices() : List.of();
        return choices.isEmpty() ? Optional.empty() : Optional.of(choices.get(Math.min(choice, choices.size() - 1)));
    }

    /**
     * M8 part A: whether an owned Bulwark is the escort slot's choice, or flies anyway while Rook is missing (the
     * Bulwark row's {@code FITTED}).
     */
    public boolean bulwarkFlies() {
        Escort escort = hangar.campaign().gear().escort();
        return escort.bulwark().owned() && (hangar.escortChoice() == Escort.Escorter.BULWARK || hangar.rookMissing());
    }

    /** The choices of the selected row: the shop row's, or the Bulwark row's. */
    private int choiceCount() {
        if (bulwarkSelected()) {
            return bulwarkChoices().size();
        }
        return selected().map(offer -> offer.choices().size()).orElse(0);
    }

    /**
     * M7 part A: the utility slot's bay row, after its shop rows: in a bought bay's own slot that bay's
     * line (to sell it), in another utility slot the next bay for sale (to buy it); empty elsewhere, and
     * before the bay is for sale (it is not listed while locked).
     */
    public Optional<Hangar.BayOffer> bayRow() {
        LoadoutSlot shown = slot();
        if (refitOnly || shown.kind() != ItemKind.UTILITY) {
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
        if (bulwarkSelected()) {
            return bulwarkChoice().map(BulwarkChoice::action);
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
        if (bulwarkSelected()) {
            return bulwarkChoice()
                    .filter(chosen -> chosen.action() == Action.SELL)
                    .map(chosen -> new Sale(
                            BULWARK, -chosen.credits(), hangar.bulwark().bought()));
        }
        Optional<Offer> offer = selected();
        return choice().filter(chosen -> chosen.action() == Action.SELL)
                .flatMap(chosen ->
                        offer.map(sold -> new Sale(Names.of(sold.item().name()), -chosen.credits(), sold.bought())));
    }

    /** Whether the escort's side row (after his guns and the Bulwark's row) is selected. */
    public boolean sideSelected() {
        return hasSideRow() && row == rows().size() + (bulwarkRow().isPresent() ? 1 : 0);
    }

    /** M8 part A: the Bulwark's name in the shop, its callout and the messages. */
    public static final String BULWARK = "BULWARK";

    /** Rook's side (left or right of the player), the escort's setting. */
    public WingmanSpec.Side escortSide() {
        return hangar.campaign().gear().escort().side();
    }

    /**
     * The shop rows for the selected slot; in the replay hangar only the fitted and owned items, each with
     * its refit choices alone.
     */
    public List<Offer> rows() {
        List<Offer> shop = hangar.shop(slot());
        if (!refitOnly) {
            return shop;
        }
        return shop.stream()
                .filter(offer -> offer.state() == Hangar.State.FITTED || offer.state() == Hangar.State.OWNED)
                .map(HangarState::refitChoices)
                .toList();
    }

    /** A row with only its refit choices (its {@code FIT} or {@code UNFIT}). */
    private static Offer refitChoices(Offer offer) {
        return new Offer(
                offer.item(),
                offer.state(),
                offer.level(),
                offer.owned(),
                offer.bought(),
                offer.isNew(),
                offer.choices().stream()
                        .filter(choice -> refit(choice.action()))
                        .toList());
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

    /** What the mouse pointer rests on: drawn with the hover look, never selected by it. */
    public Optional<Hover> hover() {
        return hover;
    }

    /** Sets what the pointer rests on; empty once a key is used (the last device used owns the look). */
    public void hover(Optional<Hover> over) {
        hover = over;
    }

    /** Whether the pointer rests on this target and index. */
    public boolean hovers(Target target, int index) {
        return hover.filter(h -> h.target() == target && h.index() == index).isPresent();
    }

    /**
     * The choice the test fire and the power bar preview: the selected row's choice under the pointer
     * while it rests on one of its chips, else the selected choice. The selection itself stays.
     */
    public Optional<Choice> previewChoice() {
        Optional<Offer> offer = selected();
        if (focus == Focus.SHOP && offer.isPresent() && hover.isPresent()) {
            Hover over = hover.get();
            List<Choice> choices = offer.get().choices();
            if (over.target() == Target.CHOICE && over.index() < choices.size()) {
                return Optional.of(choices.get(over.index()));
            }
        }
        return choice();
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
            case REPAIR -> !refitOnly && repairLines().stream().anyMatch(line -> missing(line) > 0);
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

    /**
     * The repair panel's lines: the ship's, Rook's once he is hired (M8 part A: not while he is missing) and an owned
     * Bulwark's (M8 part A).
     */
    public List<RepairLine> repairLines() {
        List<RepairLine> lines = new ArrayList<>(List.of(RepairLine.SHIP));
        if (hangar.escortHired() && !hangar.rookMissing()) {
            lines.add(RepairLine.ROOK);
        }
        if (hangar.campaign().gear().escort().bulwark().owned()) {
            lines.add(RepairLine.BULWARK);
        }
        return lines;
    }

    /** The points missing on a repair line. */
    public int missing(RepairLine line) {
        return switch (line) {
            case SHIP -> hangar.missingArmour();
            case ROOK -> hangar.escortMissingArmour();
            case BULWARK -> hangar.bulwarkMissingArmour();
        };
    }

    /** The most points of a repair line the credits pay for. */
    public int affordable(RepairLine line) {
        return switch (line) {
            case SHIP -> hangar.affordableRepair();
            case ROOK -> hangar.affordableEscortRepair();
            case BULWARK -> hangar.affordableBulwarkRepair();
        };
    }

    /** Credits per point on a repair line: the difficulty's, and (M8 part A) the Bulwark's own on every difficulty. */
    public int repairCost(RepairLine line) {
        return line == RepairLine.BULWARK ? hangar.bulwarkRepairCost() : hangar.repairCost();
    }

    /**
     * Shows {@code text} on the shop's message line (the visit's free charges) until the next move, slot
     * change or transaction (M7 part A, round 37 R.4 g: it no longer stays on every slot).
     */
    public void notice(String text) {
        if (!notice.isEmpty() && message.equals(notice)) {
            // M8 AW: a visit's second notice (the hydro-kit's, then the energy weapons' warning) waits its turn.
            notices.add(text);
            return;
        }
        message = text;
        notice = text;
    }

    /** M7 part A: a move ({@link Outcome#MOVED}) takes the visit's notice off the message line. */
    private Outcome moved(Outcome outcome) {
        if (outcome == Outcome.MOVED && !notice.isEmpty()) {
            if (message.equals(notice)) {
                // M8 AW: the next notice of the visit, if any, takes its place.
                String next = notices.isEmpty() ? "" : notices.poll();
                message = next;
                notice = next;
            } else {
                notice = "";
                notices.clear();
            }
        }
        return outcome;
    }

    /**
     * M8 part A: the hydro-kit's one-off notice (design/player/systems, Hydro-kit), at the first visit before an
     * under-water level (pending the user's review: the wording).
     */
    public static final String HYDRO_KIT_NOTICE = "HYDRO-KIT FITTED: THE STORMHAWK AND ROOK ARE SEALED FOR THE OCEAN";

    /**
     * M8 AW (design/world/europa, Under water rules: energy weapons ×0.2 damage, ×0.4 reach): the second notice of the
     * hydro-kit's visit, after {@link #HYDRO_KIT_NOTICE} (pending the user's review in round 56: the wording).
     */
    public static final String UNDER_WATER_NOTICE = "ENERGY WEAPONS SCATTER IN WATER: FIT TORPEDOES AND DEPTH CHARGES";

    /** The notice for a special's free charges: "1 FREE AIRSTRIKE CHARGE, FITTED". */
    public static String freeChargesNotice(Campaign.FreeCharges free) {
        String name = free.special().toUpperCase(Locale.ROOT);
        String charges =
                free.charges() == 1 ? "1 FREE " + name + " CHARGE" : free.charges() + " FREE " + name + " CHARGES";
        return free.fitted() ? charges + ", FITTED" : charges;
    }

    /**
     * What the last transaction did or why it was refused, or the visit's notice; in the replay hangar
     * {@link #REFIT_ONLY} while there is nothing else to say.
     */
    public String message() {
        return refitOnly && message.isEmpty() ? REFIT_ONLY : message;
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
                int choices = choiceCount();
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
        message = (bulwarkFliesNext() ? "THE BULWARK" : "ROOK") + " FLIES ON YOUR " + side.name();
        return Outcome.MOVED;
    }

    /**
     * M8 part A: whether the Bulwark (owned or the loaner) flies the hangar's level (the next one, or the replayed
     * mission), so the side row and its message name it instead of Rook.
     */
    public boolean bulwarkFliesNext() {
        return hangar.escortFlight()
                .filter(flight -> flight.escorter() == Escort.Escorter.BULWARK)
                .isPresent();
    }

    /** The tabs in the repair panel: the next line that has points missing (M8 part A: of up to three). */
    private Outcome switchRepairLine() {
        List<RepairLine> lines = repairLines();
        int at = lines.indexOf(repairLine);
        for (int step = 1; step < lines.size(); step++) {
            RepairLine other = lines.get(Math.floorMod(at + step, lines.size()));
            if (missing(other) > 0) {
                return switchRepairLine(other);
            }
        }
        return Outcome.NONE;
    }

    private Outcome switchRepairLine(RepairLine other) {
        if (missing(other) == 0 || other == repairLine) {
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

    /**
     * The mouse: a click on the selected row's detail (a choice, a side or the bay chip) gives the focus
     * back to the shop when the command bar had it; false while the repair panel is open.
     */
    public boolean pointShop() {
        if (focus == Focus.REPAIR) {
            return false;
        }
        focus = Focus.SHOP;
        return true;
    }

    /** The mouse: a click on a shop row selects it (the focus goes to the shop); hovering only highlights it. */
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

    /** The mouse: a click on one of the selected row's choices selects it, as left / right do (the click then confirms). */
    public Outcome pointChoice(int index) {
        int choices = choiceCount();
        if (focus != Focus.SHOP || index < 0 || index >= choices || index == Math.min(choice, choices - 1)) {
            return Outcome.NONE;
        }
        choice = index;
        return moved(Outcome.MOVED);
    }

    /** The mouse: a click on the side row's LEFT or RIGHT chip sets Rook's side. */
    public Outcome pointSide(WingmanSpec.Side side) {
        if (focus != Focus.SHOP || !sideSelected()) {
            return Outcome.NONE;
        }
        return side(side);
    }

    /** The mouse: a click on an enabled command of the command bar selects it (the click then confirms). */
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
        if (focus != Focus.REPAIR || line == repairLine || !repairLines().contains(line)) {
            return Outcome.NONE;
        }
        return switchRepairLine(line);
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
            case SHOP ->
                sideSelected()
                        ? side(escortSide().other())
                        : baySelected() ? chooseBay() : bulwarkSelected() ? chooseBulwark() : choose();
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
                message = refitOnly ? "UNDONE" : "UNDONE - FULL REFUND";
                // An undone bay purchase closes its slot again.
                keepSlot();
                afterChange(Optional.empty());
                if (!hangar.canUndo()) {
                    moveCommand(1);
                }
                yield Outcome.DONE;
            }
            case REPAIR -> {
                // The ship's line first while it has points missing that the credits pay for, else Rook's (M8 part A:
                // else the Bulwark's).
                Optional<RepairLine> line = repairLines().stream()
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
        int cost = repairPoints * repairCost(repairLine);
        boolean done =
                switch (repairLine) {
                    case SHIP -> hangar.repair(repairPoints);
                    case ROOK -> hangar.repairEscort(repairPoints);
                    case BULWARK -> hangar.repairBulwark(repairPoints);
                };
        if (!done) {
            message = "NOT ENOUGH CREDITS FOR A REPAIR";
            return Outcome.REFUSED;
        }
        message = String.format(
                Locale.ROOT, "REPAIRED %s%d POINTS FOR CR %d", repairPrefix(repairLine), repairPoints, cost);
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

    /** A repair's message and button name the escort's line: {@code ROOK: }, {@code BULWARK: }; nothing for the ship. */
    public static String repairPrefix(RepairLine line) {
        return line == RepairLine.SHIP ? "" : line.name() + ": ";
    }

    /**
     * M8 part A: the Bulwark row's choice: buy it (it flies at once), fit it (it flies instead of Rook), unfit it
     * (Rook flies again), or ask before selling it.
     */
    private Outcome chooseBulwark() {
        Optional<BulwarkChoice> chosen = bulwarkChoice();
        if (chosen.isEmpty()) {
            return Outcome.NONE;
        }
        BulwarkChoice made = chosen.get();
        if (!made.allowed()) {
            message = made.refusal().orElseThrow();
            return Outcome.REFUSED;
        }
        if (refitOnly && !refit(made.action())) {
            message = REFIT_ONLY;
            return Outcome.REFUSED;
        }
        if (made.action() == Action.SELL) {
            return Outcome.SELL;
        }
        boolean done =
                switch (made.action()) {
                    case BUY -> hangar.buyBulwark();
                    case FIT -> hangar.chooseEscort(Escort.Escorter.BULWARK);
                    case UNFIT -> hangar.chooseEscort(Escort.Escorter.ROOK);
                    default -> false;
                };
        if (!done) {
            message = made.action() == Action.BUY ? BULWARK_CREDITS : "";
            return Outcome.REFUSED;
        }
        message = switch (made.action()) {
            case BUY -> "BOUGHT THE BULWARK - IT FLIES IN THE ESCORT SLOT";
            case FIT -> "THE BULWARK FLIES IN THE ESCORT SLOT";
            default -> "ROOK FLIES IN THE ESCORT SLOT";
        };
        choice = 0;
        return Outcome.DONE;
    }

    /** M8 part A: sells the owned Bulwark of the selected row; the slot goes back to Rook. */
    private Outcome sellBulwark() {
        int refund = -hangar.bulwark().credits();
        if (!hangar.sellBulwark()) {
            return Outcome.REFUSED;
        }
        message = "SOLD THE BULWARK FOR " + Names.credits(refund);
        choice = 0;
        return Outcome.DONE;
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
        if (refitOnly) {
            message = REFIT_ONLY;
            return Outcome.REFUSED;
        }
        if (baySelected()) {
            return sellBay();
        }
        if (bulwarkSelected()) {
            return bulwarkChoice()
                            .filter(chosen -> chosen.action() == Action.SELL)
                            .isPresent()
                    ? sellBulwark()
                    : Outcome.NONE;
        }
        Optional<Offer> offer = selected();
        Optional<Choice> chosen = choice();
        if (offer.isEmpty() || chosen.isEmpty() || chosen.get().action() != Action.SELL) {
            return Outcome.NONE;
        }
        return apply(offer.get(), chosen.get());
    }

    private Outcome apply(Offer offer, Choice made) {
        if (refitOnly && !refit(made.action())) {
            message = REFIT_ONLY;
            return Outcome.REFUSED;
        }
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
            case UPGRADE -> upgraded(name, offer);
            case FIT -> name + " FITTED";
            case UNFIT -> name + " MOVED TO THE INVENTORY";
            case SELL -> "SOLD " + name + " FOR CR " + -made.credits();
            case BUY_CHARGE -> "BOUGHT A CHARGE OF " + name;
            case POWER_DOWN -> powered(name, offer.power() - 1, offer.level());
            case POWER_UP -> powered(name, offer.power() + 1, offer.level());
        };
        int dial = choice;
        afterChange(
                made.action() == Action.SELL
                        ? Optional.empty()
                        : Optional.of(offer.item().name()));
        if (made.action() == Action.POWER_DOWN || made.action() == Action.POWER_UP) {
            // The power dial stays on its chip, so that confirm turns it another step.
            choice = dial;
        }
        return Outcome.DONE;
    }

    /**
     * An upgrade's message: {@code PULSE CANNON UPGRADED TO L3}; a turned-down weapon keeps its power setting, so
     * its message says so: {@code PULSE CANNON UPGRADED TO L4 - POWER L2}.
     */
    static String upgraded(String name, Offer offer) {
        String text = name + " UPGRADED TO L" + (offer.level() + 1);
        return offer.owned()
                .filter(owned -> !owned.fullPower())
                .map(owned -> text + " - POWER L" + owned.power())
                .orElse(text);
    }

    /** The power dial's message: {@code PULSE CANNON POWER L2 OF 3}. */
    static String powered(String name, int setting, int level) {
        return name + " POWER L" + setting + " OF " + level;
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
            case MIN_POWER -> "AT ITS LOWEST POWER";
            case FULL_POWER -> "AT FULL POWER";
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
        // A replay flies at full armour (design/ui/mission-select), so the replay hangar does not warn.
        if (!refitOnly && hangar.campaign().armour() < hangar.campaign().maxArmour() / 2) {
            warnings.add("ARMOUR BELOW 50 %");
        }
        Campaign campaign = hangar.campaign();
        if (campaign.escortGrounded(hangar.level())) {
            warnings.add(ROOK_GROUNDED);
        } else if (campaign.escortArmourLow(hangar.level())) {
            warnings.add(rookArmour(campaign.rookArmour(hangar.level()), campaign.escortMaxArmour()));
        }
        return warnings;
    }
}
