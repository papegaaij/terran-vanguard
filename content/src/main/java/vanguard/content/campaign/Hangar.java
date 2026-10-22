package vanguard.content.campaign;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import vanguard.content.campaign.Catalogue.Item;
import vanguard.content.campaign.Catalogue.Stat;
import vanguard.sim.WingmanSpec;

/**
 * A hangar visit's shop and loadout rules (design/ui/hangar, design/player, design/systems/economy)
 * on the campaign's {@link Gear}: buying (fitted at once when the power budget allows, otherwise
 * into the inventory), upgrading, fitting and unfitting owned items for free, selling for the
 * sell-back share of all that was spent on an item (for all of it when the item was bought during
 * this visit), special charges, armour repair and the visit's undo, which returns every transaction
 * of the visit in reverse order for 100 %.
 *
 * <p>Rook's guns (design/player/wingmen, Escort inventory) are the {@link LoadoutSlot#ESCORT}
 * slot's items once he is hired: bought, upgraded, fitted and sold like the player's weapons, his
 * fitted gun never unfitted or sold, no power drawn. His armour has its own repair line at the same
 * cost per point, which is part of the visit's undo; his side is a setting outside it (design/ui/hangar):
 * an undo keeps the side he has now.
 *
 * <p>M7 part A: a cooldown special (the Shield Overcharge) is bought once ({@link Action#BUY}) and
 * sold back ({@link Action#SELL}) like other gear instead of by the charge; the gear holds 1 for an
 * owned one. The third utility bay is bought and sold through {@link #bays}, {@link #buyBay} and
 * {@link #sellBay}, part of the visit's undo; its slot {@link LoadoutSlot#UTILITY_3} has no shop
 * rows until it is bought, and it cannot be sold while a module is fitted in it.
 *
 * <p>M8 part A (design/player/wingmen, Heavy drone "Bulwark"; design/ui/hangar): the Bulwark is bought once ({@link
 * #buyBulwark}, chosen for the escort slot at once) and sold back ({@link #sellBulwark}) like other gear; the slot's
 * choice between Rook and an owned Bulwark ({@link #chooseEscort}) is free; the Bulwark's armour has its own repair
 * line at its own cost per point ({@link #repairBulwark}); all of it part of the visit's undo. While Rook is missing
 * (Levels 27–29) his card is MIA ({@link #rookMissing}): no gun rows and no repair line, his guns kept.
 *
 * <p>The power load is the sum of the fitted items' draws and may not exceed the generator's
 * output (a loadout already over it may make changes that do not widen the gap); the front gun and
 * the core parts are always fitted. The power dial (the user, 2026-10-21): a fitted weapon's
 * {@link Action#POWER_DOWN} and {@link Action#POWER_UP} turn its power setting ({@link Fitted#power}) down to 1
 * and up to its bought level, for free and as part of the visit's undo; the weapon draws and flies at its setting,
 * raising it checks the load like an upgrade, and an upgrade of a weapon at full power raises the setting with
 * it (a turned-down one keeps its setting). A plating swap keeps the damage: the
 * missing armour points stay missing (at least 1 point is left).
 */
public final class Hangar {
    /** Where a shop row's item is. */
    public enum State {
        /** In the selected slot. */
        FITTED,
        /** In the inventory (a special: charges carried, not selected). */
        OWNED,
        BUYABLE,
        /** Not in the shop yet: its unlock level is shown. */
        LOCKED
    }

    public enum Action {
        BUY,
        UPGRADE,
        FIT,
        UNFIT,
        SELL,
        BUY_CHARGE,
        /**
         * The power dial (the user, 2026-10-21): a fitted weapon flies and draws one level lower, for free; refused
         * at setting 1 ({@link Refusal#MIN_POWER}).
         */
        POWER_DOWN,
        /**
         * The power dial: a turned-down weapon one level higher again, for free, up to its bought level ({@link
         * Refusal#FULL_POWER}); the load is checked like an upgrade's ({@link Refusal#POWER}).
         */
        POWER_UP
    }

    /** Why a choice cannot be made. */
    public enum Refusal {
        CREDITS,
        /** The load would exceed the generator's output. */
        POWER,
        MAX_LEVEL,
        /** The special carries its most charges. */
        FULL,
        /** The power dial: the weapon is at setting 1. */
        MIN_POWER,
        /** The power dial: the weapon is at its bought level. */
        FULL_POWER
    }

    /**
     * M8 part A: the Bulwark's shop line (design/player/wingmen, Heavy drone "Bulwark").
     *
     * @param state {@link State#OWNED} when bought, {@link State#BUYABLE} when for sale at this visit, {@link
     *     State#LOCKED} before its unlock
     * @param bought it was bought during this visit, so it sells for all it cost
     * @param credits what buying it costs, or (negative) what selling it returns
     * @param refusal why buying it cannot be done now ({@link Refusal#CREDITS}); empty when it can, and for the
     *     owned and the locked line
     * @param unlock the level from whose hangar visit it is for sale
     */
    public record BulwarkOffer(State state, boolean bought, int credits, Optional<Refusal> refusal, int unlock) {
        public boolean allowed() {
            return state != State.LOCKED && refusal.isEmpty();
        }
    }

    /** M7 part A: why a utility bay cannot be bought or sold. */
    public enum BayRefusal {
        CREDITS,
        /** A module is fitted in it (or in a bay bought after it): it cannot be sold. */
        IN_USE,
        /** A bay before it is not bought yet. */
        ORDER
    }

    /**
     * M7 part A: a bought utility bay's shop line (design/player/systems, Third utility bay).
     *
     * @param state {@link State#OWNED} when bought, {@link State#BUYABLE} when for sale at this visit,
     *     {@link State#LOCKED} before its unlock
     * @param bought it was bought during this visit, so it sells for all it cost
     * @param credits what buying it costs, or (negative) what selling it returns
     * @param refusal why that cannot be done now; empty when it can (never for a locked bay's line,
     *     which offers nothing)
     */
    public record BayOffer(Catalogue.Bay bay, State state, boolean bought, int credits, Optional<BayRefusal> refusal) {
        public boolean allowed() {
            return state != State.LOCKED && refusal.isEmpty();
        }
    }

    /**
     * Something a shop row offers.
     *
     * @param credits what it costs; negative for a refund
     * @param load the power load after it, MW
     * @param output the generator output after it, MW
     * @param fits whether a bought item is fitted at once; otherwise it goes to the inventory
     */
    public record Choice(
            Action action, int credits, double load, double output, boolean fits, Optional<Refusal> refusal) {
        public boolean allowed() {
            return refusal.isEmpty();
        }
    }

    /**
     * A shop row for a slot.
     *
     * @param level the owned level (1 for a buyable or locked item)
     * @param owned the owned item this row stands for (with its power setting); empty for a buyable or locked item
     * @param bought the owned item was bought during this visit, so it sells for all spent on it
     * @param isNew in the shop since the visit before
     */
    public record Offer(
            Item item,
            State state,
            int level,
            Optional<Fitted> owned,
            boolean bought,
            boolean isNew,
            List<Choice> choices) {
        public Offer {
            choices = List.copyOf(choices);
        }

        public Optional<Choice> choice(Action action) {
            return choices.stream().filter(choice -> choice.action() == action).findFirst();
        }

        /**
         * The power setting the owned item flies at (the power dial), its level when at full power; the level for a
         * buyable or locked item. The shop's numbers for the row are {@code item().stats(power())}.
         */
        public int power() {
            return owned.map(Fitted::power).orElse(level);
        }
    }

    /** The utility module that decides the hangar intel's detail (design/player/systems). */
    public static final String SENSOR_SUITE = "Sensor suite";

    /** The highest sensor level. */
    public static final int MAX_SENSOR = 3;

    private static final double EPSILON = 1e-9;

    private final Catalogue catalogue;
    private final Campaign campaign;
    private Bought bought = Bought.NONE;
    private final Deque<Step> undo = new ArrayDeque<>();

    public Hangar(Catalogue catalogue, Campaign campaign) {
        this.catalogue = catalogue;
        this.campaign = campaign;
    }

    public Campaign campaign() {
        return campaign;
    }

    /** The item fitted in a slot. */
    public Optional<Item> fitted(LoadoutSlot slot) {
        return Optional.ofNullable(campaign.gear().loadout().get(slot)).map(fitted -> item(slot.kind(), fitted));
    }

    private Item item(ItemKind kind, Fitted fitted) {
        return catalogue.item(kind, fitted.item());
    }

    /** The fitted items' draw at their power settings, MW. */
    public double load() {
        return load(campaign.gear().loadout());
    }

    private double load(Map<LoadoutSlot, Fitted> loadout) {
        return loadout.entrySet().stream()
                .mapToDouble(entry -> item(entry.getKey().kind(), entry.getValue())
                        .draw(entry.getValue().power()))
                .sum();
    }

    /** The fitted generator's output, MW. */
    public double output() {
        return output(campaign.gear().loadout());
    }

    private double output(Map<LoadoutSlot, Fitted> loadout) {
        return item(ItemKind.GENERATOR, loadout.get(LoadoutSlot.GENERATOR))
                .stats(1)
                .get(Stat.OUTPUT);
    }

    /** The intel detail: the best fitted sensor suite's level plus the difficulty's bonus, at most 3. */
    public int sensorLevel() {
        int fitted = campaign.gear().loadout().entrySet().stream()
                .filter(entry -> entry.getKey().kind() == ItemKind.UTILITY)
                .map(Map.Entry::getValue)
                .filter(module -> module.item().equals(SENSOR_SUITE))
                .mapToInt(Fitted::level)
                .max()
                .orElse(0);
        return Math.min(MAX_SENSOR, fitted + catalogue.sensorBonus(campaign.difficulty()));
    }

    /** The traits of the fitted weapons. */
    public Set<String> fittedTraits() {
        Set<String> traits = new LinkedHashSet<>();
        campaign.gear().loadout().forEach((slot, fitted) -> {
            if (slot.kind().weapon()) {
                traits.addAll(item(slot.kind(), fitted).traits());
            }
        });
        return traits;
    }

    /** The trait of the sources that damage hardened targets (design/enemies, hardened armour). */
    public static final String ANTI_GROUND = "anti-ground";

    /**
     * The specials that damage hardened targets (design/player/specials: the Airstrike's bombs and the
     * Smart Bomb's flash hit them; M7 part A: the Orbital Lance comes from above and damages them);
     * the Decoy Flares, the EMP Burst and the Shield Overcharge do not.
     */
    public static final Set<String> HARDENED_SPECIALS = Set.of("Airstrike", "Smart Bomb", "Orbital Lance");

    /** M5 part D: the trait of the weapons that fire behind the ship (Level 10's {@code required: [rear]}). */
    public static final String REAR = "rear";

    /**
     * M5 part C (design/ui/hangar, Launch, user decision D7 = a): the traits the next flight brings
     * from every source a level's {@code required} trait counts: the fitted weapons' traits, the
     * traits of Rook's fitted gun while he flies (hired, fitted and not grounded; his Mortar is
     * {@code anti-ground}), and {@value #ANTI_GROUND} for an Airstrike or a Smart Bomb fitted with at
     * least one charge. M5 part D (user decision D9 = a): {@value #REAR} comes only from the weapon
     * fitted in the rear slot (homing and Rook's guns do not reach behind the ship).
     */
    public Set<String> sourceTraits() {
        Set<String> traits = new LinkedHashSet<>();
        campaign.gear().loadout().forEach((slot, fitted) -> {
            if (slot.kind().weapon()) {
                item(slot.kind(), fitted).traits().stream()
                        .filter(trait -> slot == LoadoutSlot.REAR || !trait.equals(REAR))
                        .forEach(traits::add);
            }
        });
        campaign.escortFlight()
                .flatMap(Campaign.EscortFlight::gun)
                .ifPresent(gun -> item(ItemKind.ESCORT, gun).traits().stream()
                        .filter(trait -> !trait.equals(REAR))
                        .forEach(traits::add));
        Fitted special = campaign.gear().loadout().get(LoadoutSlot.SPECIAL);
        if (special != null
                && HARDENED_SPECIALS.contains(special.item())
                && campaign.gear().charges(special.item()) > 0) {
            traits.add(ANTI_GROUND);
        }
        return traits;
    }

    /** The {@code required} traits of a level that no source of the next flight brings ({@link #sourceTraits}). */
    public List<String> missingRequired(List<String> required) {
        Set<String> sources = sourceTraits();
        return required.stream().filter(trait -> !sources.contains(trait)).toList();
    }

    /**
     * Whether an item is in the shop at this visit: from its unlock level on, or unlocked early; never
     * an item that is not for sale yet (its early unlock stays in the campaign for later).
     */
    public boolean available(Item item) {
        return item.forSale()
                && (item.unlock() <= campaign.nextLevel() || campaign.unlocks().contains(item.id()));
    }

    /** Whether a bought utility bay is for sale at this visit: from its unlock level (the third bay from Act 3). */
    public boolean available(Catalogue.Bay bay) {
        return bay.unlock() <= campaign.nextLevel();
    }

    /** M7 part A: the lines of the utility bays that are bought, in their order. */
    public List<BayOffer> bays() {
        List<BayOffer> offers = new ArrayList<>();
        List<Catalogue.Bay> all = catalogue.bays();
        int owned = campaign.gear().bays();
        for (int i = 0; i < all.size(); i++) {
            Catalogue.Bay bay = all.get(i);
            if (i < owned) {
                boolean isBought = i >= owned - bought.bays();
                boolean inUse = all.subList(i, all.size()).stream()
                        .anyMatch(later -> campaign.gear().loadout().containsKey(later.slot()));
                offers.add(new BayOffer(
                        bay,
                        State.OWNED,
                        isBought,
                        -(isBought ? bay.price() : catalogue.sellPrice(bay)),
                        inUse ? Optional.of(BayRefusal.IN_USE) : Optional.empty()));
            } else if (!available(bay)) {
                offers.add(new BayOffer(bay, State.LOCKED, false, bay.price(), Optional.empty()));
            } else {
                Optional<BayRefusal> refusal = i > owned
                        ? Optional.of(BayRefusal.ORDER)
                        : campaign.credits() < bay.price() ? Optional.of(BayRefusal.CREDITS) : Optional.empty();
                offers.add(new BayOffer(bay, State.BUYABLE, false, bay.price(), refusal));
            }
        }
        return offers;
    }

    /**
     * M7 part A: buys a utility bay that is for sale; its slot opens empty.
     *
     * @return whether it was bought
     */
    public boolean buyBay(Catalogue.Bay bay) {
        Optional<BayOffer> line = bays().stream()
                .filter(offer -> offer.bay().equals(bay) && offer.state() == State.BUYABLE)
                .findFirst();
        if (line.isEmpty() || !line.get().allowed()) {
            return false;
        }
        Step before = step();
        Gear gear = before.gear();
        undo.push(before);
        restore(new Step(gear.withCredits(gear.credits() - bay.price()).withBays(gear.bays() + 1), bought.withBay(+1)));
        return true;
    }

    /**
     * M7 part A: sells the last bought utility bay back, for all it cost when it was bought during the
     * visit, the sell-back share otherwise; never while a module is fitted in it.
     *
     * @return whether it was sold
     */
    public boolean sellBay(Catalogue.Bay bay) {
        Optional<BayOffer> line = bays().stream()
                .filter(offer -> offer.bay().equals(bay) && offer.state() == State.OWNED)
                .findFirst();
        if (line.isEmpty() || !line.get().allowed()) {
            return false;
        }
        Step before = step();
        Gear gear = before.gear();
        undo.push(before);
        restore(new Step(
                gear.withCredits(gear.credits() - line.get().credits()).withBays(gear.bays() - 1),
                line.get().bought() ? bought.withBay(-1) : bought));
        return true;
    }

    /**
     * The shop rows for a slot: the fitted item, the owned ones, the buyable ones by price, the locked
     * ones by unlock. An item that is not for sale is not listed unless it is owned.
     */
    public List<Offer> shop(LoadoutSlot slot) {
        ItemKind kind = slot.kind();
        Gear gear = campaign.gear();
        if (kind == ItemKind.ESCORT && (!gear.escort().hired() || campaign.rookMissing())) {
            // The escort slot opens when Rook joins (Level 08); M8 part A: while he is missing his card is MIA, his
            // guns kept in the inventory but not offered.
            return List.of();
        }
        if (!gear.open(slot)) {
            // M7 part A: the third utility bay's slot opens when the bay is bought.
            return List.of();
        }
        List<Offer> offers = new ArrayList<>();
        Set<String> listed = new LinkedHashSet<>();
        Optional.ofNullable(gear.loadout().get(slot)).ifPresent(fitted -> {
            offers.add(fittedOffer(slot, fitted));
            listed.add(fitted.item());
        });
        if (kind == ItemKind.SPECIAL) {
            for (Item special : catalogue.items(kind)) {
                if (gear.charges(special.id()) > 0 && !listed.contains(special.id())) {
                    boolean isBought = bought.specials().contains(special.id());
                    offers.add(new Offer(
                            special,
                            State.OWNED,
                            1,
                            Optional.of(new Fitted(special.id(), 1)),
                            isBought,
                            false,
                            List.of(
                                    fitChoice(slot, new Fitted(special.id(), 1)),
                                    special.cooldownSpecial()
                                            ? sellChoice(special, 1, 0, isBought)
                                            : chargeChoice(special))));
                    listed.add(special.id());
                }
            }
        }
        // Of equal owned items the first rows stand for the ones bought during the visit.
        List<Fitted> boughtHere = new ArrayList<>(bought.stored(kind));
        for (Fitted owned : gear.inventory(kind)) {
            Item item = item(kind, owned);
            boolean isBought = boughtHere.remove(owned);
            offers.add(new Offer(
                    item,
                    State.OWNED,
                    owned.level(),
                    Optional.of(owned),
                    isBought,
                    false,
                    List.of(fitChoice(slot, owned), sellChoice(item, owned.level(), 0, isBought))));
            listed.add(owned.item());
        }
        catalogue.items(kind).stream()
                .filter(item -> item.price() > 0 && available(item) && !listed.contains(item.id()))
                .sorted(Comparator.comparingInt(Item::price).thenComparing(Item::name))
                .forEach(item -> offers.add(new Offer(
                        item,
                        State.BUYABLE,
                        1,
                        Optional.empty(),
                        false,
                        item.unlock() == campaign.nextLevel() && campaign.nextLevel() > 1,
                        List.of(
                                kind != ItemKind.SPECIAL
                                        ? buyChoice(slot, item)
                                        : item.cooldownSpecial() ? cooldownBuyChoice(item) : chargeChoice(item)))));
        catalogue.items(kind).stream()
                .filter(item -> item.forSale() && !available(item))
                .sorted(Comparator.comparingInt(Item::unlock).thenComparingInt(Item::price))
                .forEach(item ->
                        offers.add(new Offer(item, State.LOCKED, 1, Optional.empty(), false, false, List.of())));
        return offers;
    }

    private Offer fittedOffer(LoadoutSlot slot, Fitted fitted) {
        ItemKind kind = slot.kind();
        Item item = item(kind, fitted);
        boolean cooldown = item.cooldownSpecial();
        boolean isBought = cooldown
                ? bought.specials().contains(item.id())
                : bought.fitted().contains(slot);
        List<Choice> choices = new ArrayList<>();
        if (cooldown) {
            // M7 part A: a cooldown special is sold back like other gear; it draws no power.
            choices.add(sellChoice(item, 1, 0, isBought));
        } else if (kind == ItemKind.SPECIAL) {
            choices.add(chargeChoice(item));
        } else if (item.maxLevel() > 1) {
            choices.add(upgradeChoice(item, fitted));
        }
        if (kind.weapon()) {
            choices.add(powerDownChoice(item, fitted));
            choices.add(powerUpChoice(item, fitted));
        }
        if (kind.optional() && kind != ItemKind.SPECIAL) {
            double draw = item.draw(fitted.power());
            choices.add(new Choice(Action.UNFIT, 0, load() - draw, output(), false, Optional.empty()));
            choices.add(sellChoice(item, fitted.level(), draw, isBought));
        }
        return new Offer(item, State.FITTED, fitted.level(), Optional.of(fitted), isBought, false, choices);
    }

    private Choice upgradeChoice(Item item, Fitted fitted) {
        int level = fitted.level();
        if (level >= item.maxLevel()) {
            return new Choice(Action.UPGRADE, 0, load(), output(), true, Optional.of(Refusal.MAX_LEVEL));
        }
        int cost = item.upgradeCost(level);
        double load =
                load() - item.draw(fitted.power()) + item.draw(fitted.upgraded().power());
        return new Choice(Action.UPGRADE, cost, load, output(), true, refusal(cost, load, output()));
    }

    /** The power dial one step down: free, never over the budget (it draws less). */
    private Choice powerDownChoice(Item item, Fitted fitted) {
        if (fitted.power() <= 1) {
            return new Choice(Action.POWER_DOWN, 0, load(), output(), true, Optional.of(Refusal.MIN_POWER));
        }
        double load = load() - item.draw(fitted.power()) + item.draw(fitted.power() - 1);
        return new Choice(Action.POWER_DOWN, 0, load, output(), true, Optional.empty());
    }

    /** The power dial one step up: free, up to the bought level, and only when the load stays within the output. */
    private Choice powerUpChoice(Item item, Fitted fitted) {
        if (fitted.fullPower()) {
            return new Choice(Action.POWER_UP, 0, load(), output(), true, Optional.of(Refusal.FULL_POWER));
        }
        double load = load() - item.draw(fitted.power()) + item.draw(fitted.power() + 1);
        return new Choice(Action.POWER_UP, 0, load, output(), true, refusal(0, load, output()));
    }

    private Choice buyChoice(LoadoutSlot slot, Item item) {
        Map<LoadoutSlot, Fitted> fitted = withFitted(slot, new Fitted(item.id(), 1));
        double load = load(fitted);
        double output = output(fitted);
        boolean fits = !overdrawn(load, output);
        Optional<Refusal> refusal = campaign.credits() < item.price() ? Optional.of(Refusal.CREDITS) : Optional.empty();
        return new Choice(Action.BUY, item.price(), load, output, fits, refusal);
    }

    /** M7 part A: a cooldown special is bought once, at its price, and fitted at once (it draws no power). */
    private Choice cooldownBuyChoice(Item special) {
        Optional<Refusal> refusal =
                campaign.credits() < special.price() ? Optional.of(Refusal.CREDITS) : Optional.empty();
        return new Choice(Action.BUY, special.price(), load(), output(), true, refusal);
    }

    /** Fitting an owned item at its level and power setting. */
    private Choice fitChoice(LoadoutSlot slot, Fitted owned) {
        Map<LoadoutSlot, Fitted> fitted = withFitted(slot, owned);
        double load = load(fitted);
        double output = output(fitted);
        return new Choice(Action.FIT, 0, load, output, true, refusal(0, load, output));
    }

    /** A sale refunds all spent on an item bought during this visit, the sell-back share otherwise. */
    private Choice sellChoice(Item item, int level, double draw, boolean isBought) {
        int refund = isBought ? item.spent(level) : catalogue.sellPrice(item, level);
        return new Choice(Action.SELL, -refund, load() - draw, output(), false, Optional.empty());
    }

    private Choice chargeChoice(Item special) {
        Optional<Refusal> refusal =
                campaign.gear().charges(special.id()) >= special.stats(1).get(Stat.CHARGES)
                        ? Optional.of(Refusal.FULL)
                        : campaign.credits() < special.price() ? Optional.of(Refusal.CREDITS) : Optional.empty();
        return new Choice(Action.BUY_CHARGE, special.price(), load(), output(), true, refusal);
    }

    private Optional<Refusal> refusal(int cost, double load, double output) {
        if (campaign.credits() < cost) {
            return Optional.of(Refusal.CREDITS);
        }
        return overdrawn(load, output) ? Optional.of(Refusal.POWER) : Optional.empty();
    }

    /**
     * Whether a change to {@code load} and {@code output} breaks the power budget: the load exceeds the
     * output by more than it does now. M7 part A (round 37, R.4 f): a loadout already over its output (a
     * save from before a draw changed, a hand-written one) is not stuck: a change that draws no more
     * than before (fitting a special, which draws nothing) is made, one that widens the gap is refused.
     */
    private boolean overdrawn(double load, double output) {
        return load > output + EPSILON && load - output > load() - output() + EPSILON;
    }

    private Map<LoadoutSlot, Fitted> withFitted(LoadoutSlot slot, Fitted item) {
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(campaign.gear().loadout());
        loadout.put(slot, item);
        return loadout;
    }

    /**
     * Makes one of a row's choices in the slot the row was listed for; the rules are checked again.
     *
     * @return whether it was made
     */
    public boolean apply(LoadoutSlot slot, Offer offer, Action action) {
        Optional<Offer> current =
                shop(slot).stream().filter(row -> same(row, offer)).findFirst();
        Optional<Choice> choice = current.flatMap(row -> row.choice(action));
        if (choice.isEmpty() || !choice.get().allowed()) {
            return false;
        }
        Step before = step();
        Change change = new Change(before);
        Item item = offer.item();
        Offer row = current.get();
        change.credits -= choice.get().credits();
        switch (action) {
            case BUY -> {
                Fitted bought = new Fitted(item.id(), 1);
                if (slot.kind() == ItemKind.SPECIAL) {
                    // M7 part A: a cooldown special, owned (1) and fitted; the special it replaces keeps its charges.
                    change.specials.put(item.id(), 1);
                    change.loadout.put(slot, bought);
                    change.specialsBought.add(item.id());
                } else if (choice.get().fits()) {
                    change.fit(slot, bought, true);
                } else {
                    change.store(slot.kind(), bought, true);
                }
            }
            case UPGRADE -> {
                Fitted fitted = before.gear().loadout().get(slot);
                change.loadout.put(slot, fitted.upgraded());
            }
            case POWER_DOWN, POWER_UP -> {
                Fitted fitted = before.gear().loadout().get(slot);
                change.loadout.put(slot, fitted.withPower(fitted.power() + (action == Action.POWER_UP ? 1 : -1)));
            }
            case FIT -> {
                Fitted owned = row.owned().orElseThrow();
                if (slot.kind() != ItemKind.SPECIAL) {
                    change.take(slot.kind(), owned, row.bought());
                }
                change.fit(slot, owned, row.bought());
            }
            case UNFIT -> change.store(slot.kind(), change.loadout.remove(slot), change.fitted.remove(slot));
            case SELL -> {
                if (slot.kind() == ItemKind.SPECIAL) {
                    // M7 part A: a cooldown special, fitted or owned.
                    change.specials.remove(item.id());
                    change.specialsBought.remove(item.id());
                    if (row.state() == State.FITTED) {
                        change.loadout.remove(slot);
                        change.fitted.remove(slot);
                    }
                } else if (row.state() == State.FITTED) {
                    change.loadout.remove(slot);
                    change.fitted.remove(slot);
                } else {
                    change.take(slot.kind(), row.owned().orElseThrow(), row.bought());
                }
            }
            case BUY_CHARGE -> {
                change.specials.merge(item.id(), 1, Integer::sum);
                change.loadout.put(slot, new Fitted(item.id(), 1));
            }
        }
        undo.push(before);
        restore(change.step());
        return true;
    }

    private static boolean same(Offer row, Offer offer) {
        return row.item().equals(offer.item())
                && row.state() == offer.state()
                && row.owned().equals(offer.owned())
                && row.bought() == offer.bought();
    }

    private Step step() {
        return new Step(campaign.gear(), bought);
    }

    /** Back to {@code step}'s gear, Rook keeping the side he has now: his side is not undone. */
    private void restore(Step step) {
        Gear restored = step.gear();
        Escort escort = restored.escort();
        campaign.gear(
                restored.withEscort(escort.withSide(campaign.gear().escort().side())));
        bought = step.bought();
    }

    /** The whole armour points missing. */
    public int missingArmour() {
        return (int) Math.ceil(campaign.maxArmour() - campaign.armour() - EPSILON);
    }

    /** Credits per armour point on the campaign's difficulty. */
    public int repairCost() {
        return catalogue.repairCost(campaign.difficulty());
    }

    /** The most points the credits repair now: "repair all" while they last. */
    public int affordableRepair() {
        int cost = repairCost();
        return cost == 0 ? missingArmour() : Math.min(missingArmour(), campaign.credits() / cost);
    }

    /**
     * Repairs armour points at the difficulty's cost per point.
     *
     * @return whether it was done: at least one point, no more than are missing or affordable
     */
    public boolean repair(int points) {
        if (points < 1 || points > affordableRepair()) {
            return false;
        }
        Gear before = campaign.gear();
        undo.push(step());
        campaign.gear(before.withCredits(before.credits() - points * repairCost())
                .withArmour(Math.min(campaign.maxArmour(), before.armour() + points)));
        return true;
    }

    /** Whether Rook is hired: the escort slot is open. */
    public boolean escortHired() {
        return campaign.gear().escort().hired();
    }

    /** Rook's whole armour points missing; 0 before he is hired and (M8 part A) while he is missing. */
    public int escortMissingArmour() {
        Escort escort = campaign.gear().escort();
        return escort.hired() && !campaign.rookMissing()
                ? (int) Math.ceil(campaign.escortMaxArmour() - escort.armour() - EPSILON)
                : 0;
    }

    /** M8 part A: whether Rook is missing in the next level: his card is MIA (no gun rows, no repair line). */
    public boolean rookMissing() {
        return campaign.rookMissing();
    }

    /** M8 part A: the Bulwark's shop line. */
    public BulwarkOffer bulwark() {
        CampaignRules.BulwarkRules rules = campaign.bulwarkRules();
        Escort escort = campaign.gear().escort();
        if (escort.bulwark().owned()) {
            int refund = bought.bulwark() ? rules.price() : (int) Math.rint(rules.price() * catalogue.sellBack());
            return new BulwarkOffer(State.OWNED, bought.bulwark(), -refund, Optional.empty(), rules.unlock());
        }
        if (!escort.hired() || rules.unlock() > campaign.nextLevel()) {
            return new BulwarkOffer(State.LOCKED, false, rules.price(), Optional.empty(), rules.unlock());
        }
        return new BulwarkOffer(
                State.BUYABLE,
                false,
                rules.price(),
                campaign.credits() < rules.price() ? Optional.of(Refusal.CREDITS) : Optional.empty(),
                rules.unlock());
    }

    /**
     * M8 part A: buys the Bulwark when it is for sale and affordable; it comes whole and is chosen for the escort slot.
     *
     * @return whether it was bought
     */
    public boolean buyBulwark() {
        BulwarkOffer offer = bulwark();
        if (offer.state() != State.BUYABLE || !offer.allowed()) {
            return false;
        }
        Step before = step();
        Gear gear = before.gear();
        undo.push(before);
        restore(new Step(
                gear.withCredits(gear.credits() - offer.credits())
                        .withEscort(gear.escort()
                                .withBulwark(new Escort.Bulwark(true, campaign.bulwarkMaxArmour()))
                                .withFlies(Escort.Escorter.BULWARK)),
                bought.withBulwark(true)));
        return true;
    }

    /**
     * M8 part A: sells the owned Bulwark, for all it cost when it was bought during the visit, the sell-back share of
     * its price otherwise; the slot's choice goes back to Rook.
     *
     * @return whether it was sold
     */
    public boolean sellBulwark() {
        BulwarkOffer offer = bulwark();
        if (offer.state() != State.OWNED) {
            return false;
        }
        Step before = step();
        Gear gear = before.gear();
        undo.push(before);
        restore(new Step(
                gear.withCredits(gear.credits() - offer.credits())
                        .withEscort(gear.escort()
                                .withBulwark(new Escort.Bulwark(false, campaign.bulwarkMaxArmour()))
                                .withFlies(Escort.Escorter.ROOK)),
                bought.withBulwark(false)));
        return true;
    }

    /**
     * M8 part A: chooses who flies in the escort slot, for free and as part of the visit's undo: Rook while he is
     * hired, or the owned Bulwark. While Rook is missing the slot holds the Bulwark whatever the choice.
     *
     * @return whether the choice changed
     */
    public boolean chooseEscort(Escort.Escorter escorter) {
        Gear gear = campaign.gear();
        Escort escort = gear.escort();
        boolean allowed = escorter == Escort.Escorter.ROOK
                ? escort.hired()
                : escort.bulwark().owned();
        if (!allowed || escort.flies() == escorter) {
            return false;
        }
        undo.push(step());
        restore(new Step(gear.withEscort(escort.withFlies(escorter)), bought));
        return true;
    }

    /** M8 part A: the owned Bulwark's whole armour points missing; 0 without one. */
    public int bulwarkMissingArmour() {
        Escort.Bulwark bulwark = campaign.gear().escort().bulwark();
        return bulwark.owned() ? (int) Math.ceil(campaign.bulwarkMaxArmour() - bulwark.armour() - EPSILON) : 0;
    }

    /** M8 part A: credits per Bulwark armour point (on every difficulty). */
    public int bulwarkRepairCost() {
        return campaign.bulwarkRules().repair();
    }

    /** M8 part A: the most of the Bulwark's points the credits repair now. */
    public int affordableBulwarkRepair() {
        int cost = bulwarkRepairCost();
        return cost == 0 ? bulwarkMissingArmour() : Math.min(bulwarkMissingArmour(), campaign.credits() / cost);
    }

    /**
     * M8 part A: repairs the owned Bulwark's armour points at its cost per point.
     *
     * @return whether it was done: at least one point, no more than are missing or affordable
     */
    public boolean repairBulwark(int points) {
        if (points < 1 || points > affordableBulwarkRepair()) {
            return false;
        }
        Gear before = campaign.gear();
        undo.push(step());
        Escort escort = before.escort();
        campaign.gear(before.withCredits(before.credits() - points * bulwarkRepairCost())
                .withEscort(escort.withBulwark(escort.bulwark()
                        .withArmour(Math.min(
                                campaign.bulwarkMaxArmour(), escort.bulwark().armour() + points)))));
        return true;
    }

    /** The most of Rook's points the credits repair now, at the same cost per point as the ship's. */
    public int affordableEscortRepair() {
        int cost = repairCost();
        return cost == 0 ? escortMissingArmour() : Math.min(escortMissingArmour(), campaign.credits() / cost);
    }

    /**
     * Repairs Rook's armour points at the difficulty's cost per point; any repair ends his grounding.
     *
     * @return whether it was done: at least one point, no more than are missing or affordable
     */
    public boolean repairEscort(int points) {
        if (points < 1 || points > affordableEscortRepair()) {
            return false;
        }
        Gear before = campaign.gear();
        undo.push(step());
        Escort escort = before.escort();
        campaign.gear(before.withCredits(before.credits() - points * repairCost())
                .withEscort(escort.withArmour(Math.min(campaign.escortMaxArmour(), escort.armour() + points))));
        return true;
    }

    /**
     * Sets Rook's side (the hangar toggle on the escort tile): a setting, not a transaction, so it is
     * not part of the visit's undo and no undo moves him back.
     *
     * @return whether it changed: he is hired and was on the other side
     */
    public boolean escortSide(WingmanSpec.Side side) {
        Gear before = campaign.gear();
        if (!before.escort().hired() || before.escort().side() == side) {
            return false;
        }
        campaign.gear(before.withEscort(before.escort().withSide(side)));
        return true;
    }

    public boolean canUndo() {
        return !undo.isEmpty();
    }

    /** Returns the visit's last transaction for 100 %; returns whether there was one. */
    public boolean undo() {
        if (undo.isEmpty()) {
            return false;
        }
        restore(undo.pop());
        return true;
    }

    /** The state a transaction changes: the gear and which of its items were bought during the visit. */
    private record Step(Gear gear, Bought bought) {}

    /**
     * The items bought during the visit, wherever they are now.
     *
     * @param fitted the slots they are fitted in
     * @param stored the ones in the inventory, by kind
     * @param specials M7 part A: the cooldown specials bought, wherever they are
     * @param bays M7 part A: the utility bays bought (the last ones the gear has)
     * @param bulwark M8 part A: the Bulwark was bought
     */
    private record Bought(
            Set<LoadoutSlot> fitted,
            Map<ItemKind, List<Fitted>> stored,
            Set<String> specials,
            int bays,
            boolean bulwark) {
        static final Bought NONE = new Bought(Set.of(), Map.of(), Set.of(), 0, false);

        Bought {
            fitted = Set.copyOf(fitted);
            stored = Gear.inventoryCopy(stored);
            specials = Set.copyOf(specials);
        }

        /** M7 part A: with {@code change} more (or fewer) utility bays bought during the visit. */
        Bought withBay(int change) {
            return new Bought(fitted, stored, specials, bays + change, bulwark);
        }

        /** M8 part A: with the Bulwark bought during the visit, or not. */
        Bought withBulwark(boolean changed) {
            return new Bought(fitted, stored, specials, bays, changed);
        }

        List<Fitted> stored(ItemKind kind) {
            return stored.getOrDefault(kind, List.of());
        }
    }

    /** A transaction's changes to the gear and to what the visit bought. */
    private final class Change {
        int credits;
        final Map<LoadoutSlot, Fitted> loadout;
        final Map<ItemKind, List<Fitted>> inventory = new EnumMap<>(ItemKind.class);
        final Map<String, Integer> specials;
        double armour;
        final Escort escort;
        final Set<LoadoutSlot> fitted = EnumSet.noneOf(LoadoutSlot.class);
        final Map<ItemKind, List<Fitted>> stored = new EnumMap<>(ItemKind.class);
        final Set<String> specialsBought = new java.util.TreeSet<>();
        final int bays;
        final int baysBought;
        final boolean bulwarkBought;

        Change(Step step) {
            Gear gear = step.gear();
            credits = gear.credits();
            loadout = new EnumMap<>(LoadoutSlot.class);
            loadout.putAll(gear.loadout());
            gear.inventory().forEach((kind, items) -> inventory.put(kind, new ArrayList<>(items)));
            specials = new HashMap<>(gear.specials());
            armour = gear.armour();
            escort = gear.escort();
            fitted.addAll(step.bought().fitted());
            step.bought().stored().forEach((kind, items) -> stored.put(kind, new ArrayList<>(items)));
            specialsBought.addAll(step.bought().specials());
            bays = gear.bays();
            baysBought = step.bought().bays();
            bulwarkBought = step.bought().bulwark();
        }

        /**
         * Fits an item; the one it replaces goes to the inventory (a special's charges stay where
         * they are).
         *
         * @param isBought the item was bought during the visit
         */
        void fit(LoadoutSlot slot, Fitted item, boolean isBought) {
            Fitted old = loadout.put(slot, item);
            boolean oldBought = fitted.remove(slot);
            if (isBought) {
                fitted.add(slot);
            }
            if (slot.kind() == ItemKind.PLATING) {
                double damage = maxArmour(old) - armour;
                armour = Math.max(1, maxArmour(item) - damage);
            }
            if (old != null && slot.kind() != ItemKind.SPECIAL) {
                store(slot.kind(), old, oldBought);
            }
        }

        private double maxArmour(Fitted plating) {
            return item(ItemKind.PLATING, plating).stats(1).get(Stat.ARMOUR);
        }

        void store(ItemKind kind, Fitted item, boolean isBought) {
            inventory.computeIfAbsent(kind, k -> new ArrayList<>()).add(item);
            if (isBought) {
                stored.computeIfAbsent(kind, k -> new ArrayList<>()).add(item);
            }
        }

        void take(ItemKind kind, Fitted item, boolean isBought) {
            if (!inventory.getOrDefault(kind, new ArrayList<>()).remove(item)
                    || isBought && !stored.getOrDefault(kind, new ArrayList<>()).remove(item)) {
                throw new IllegalStateException(item + " is not in the inventory");
            }
        }

        Step step() {
            return new Step(
                    new Gear(credits, loadout, inventory, specials, armour, escort, bays),
                    new Bought(fitted, stored, specialsBought, baysBought, bulwarkBought));
        }
    }
}
