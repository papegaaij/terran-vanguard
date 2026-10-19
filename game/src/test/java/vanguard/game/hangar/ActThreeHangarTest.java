package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Fitted;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.Hangar.Action;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveFormat;
import vanguard.content.campaign.SaveGame;
import vanguard.game.hangar.HangarState.Outcome;
import vanguard.game.input.FakePointer;

/**
 * M7 part A (step A5): the hangar's Act 3 rows: the third utility bay bought and sold from the utility
 * slots' bay row (its slot joining the tabs and the schematic only once bought, undo included), the
 * Shield Overcharge bought once and sold like gear with its cooldown on the tile, the Act 3 items in the
 * live shop with their icons, and the mouse on the bay row's chip and the third tile.
 */
class ActThreeHangarTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.of(CONTENT);
    private static final String OVERCHARGE = "Shield Overcharge";

    private static Campaign campaign(int level, int credits) {
        return campaign(level, credits, RULES.starterLoadout());
    }

    private static Campaign campaign(int level, int credits, Map<LoadoutSlot, Fitted> loadout) {
        return Campaign.load(
                RULES,
                new SaveGame(
                        SaveFormat.VERSION,
                        Instant.EPOCH,
                        0,
                        Difficulty.MEDIUM,
                        level,
                        credits,
                        0,
                        loadout,
                        Map.of(),
                        List.of(),
                        Map.of(),
                        60,
                        Optional.empty(),
                        Map.of(),
                        List.of(),
                        List.of(),
                        new SaveGame.Stats(0, 0)));
    }

    private static HangarState state(Campaign campaign) {
        return new HangarState(new Hangar(CATALOGUE, campaign), true);
    }

    private static void select(HangarState state, LoadoutSlot slot) {
        for (int i = 0; i < state.slots().size() && state.slot() != slot; i++) {
            state.nextSlot();
        }
        assertEquals(slot, state.slot());
    }

    /** Selects the slot's bay row, its last. */
    private static void selectBayRow(HangarState state) {
        assertEquals(Outcome.MOVED, state.pointRow(state.rowCount() - 1));
        assertTrue(state.baySelected());
    }

    @Test
    void beforeLevel15NoBayRowAndNoThirdSlot() {
        HangarState state = state(campaign(14, 9000));
        select(state, LoadoutSlot.UTILITY_1);
        assertEquals(Optional.empty(), state.bayRow());
        assertEquals(state.rows().size(), state.rowCount());
        assertFalse(state.slots().contains(LoadoutSlot.UTILITY_3));
    }

    @Test
    void theBayRowBuysTheThirdBayWhoseSlotJoinsTheTabs() {
        Campaign campaign = campaign(15, 9000);
        HangarState state = state(campaign);
        select(state, LoadoutSlot.UTILITY_2);
        selectBayRow(state);
        Hangar.BayOffer line = state.bayRow().orElseThrow();
        assertEquals(LoadoutSlot.UTILITY_3, line.bay().slot());
        assertEquals("BUY 5 000", ShopPanel.bayLabel(line));
        assertEquals(Optional.of(Action.BUY), state.selectedAction());
        assertEquals(Optional.empty(), state.sale());

        assertEquals(Outcome.DONE, state.confirm());

        assertEquals(4000, campaign.credits());
        assertEquals("BOUGHT UTILITY BAY 3 - ITS SLOT IS OPEN", state.message());
        assertEquals(LoadoutSlot.UTILITY_3, state.slots().getLast());
        // Round 37, R.4 (g): the cursor goes to the new slot (its tile), on its first row.
        assertEquals(LoadoutSlot.UTILITY_3, state.slot());
        assertEquals(0, state.row());
        assertFalse(state.baySelected());
        assertFalse(state.rows().isEmpty());
        assertTrue(state.selected().orElseThrow().state() != Hangar.State.LOCKED, "not a locked row");
        assertEquals(Hangar.State.OWNED, state.bayRow().orElseThrow().state(), "its own bay row sells it");
        state.previousSlot();
        assertEquals(Optional.empty(), state.bayRow(), "no second bay for sale");
        // The tabs reach the new slot after the second bay.
        state.nextSlot();
        assertEquals(LoadoutSlot.UTILITY_3, state.slot());
        state.nextSlot();
        assertEquals(state.slots().getFirst(), state.slot(), "the tabs wrap after it");
    }

    @Test
    void sellingTheBayAsksFirstAndLeavesItsSlot() {
        Campaign campaign = campaign(15, 9000);
        HangarState state = state(campaign);
        select(state, LoadoutSlot.UTILITY_1);
        selectBayRow(state);
        state.confirm();
        select(state, LoadoutSlot.UTILITY_3);
        selectBayRow(state);
        assertEquals("SELL 5 000", ShopPanel.bayLabel(state.bayRow().orElseThrow()));
        assertEquals(Optional.of(Action.SELL), state.selectedAction());

        assertEquals(Outcome.SELL, state.confirm(), "selling asks first");
        assertEquals(Optional.of(new HangarState.Sale("UTILITY BAY 3", 5000, true)), state.sale());
        assertEquals(Outcome.DONE, state.sell());

        assertEquals(9000, campaign.credits(), "bought during the visit: all of it back");
        assertEquals(0, campaign.gear().bays());
        assertFalse(state.slots().contains(LoadoutSlot.UTILITY_3));
        assertEquals(LoadoutSlot.UTILITY_2, state.slot(), "the selection leaves the closed slot");
        assertEquals("SOLD UTILITY BAY 3 FOR CR 5 000", state.message());
    }

    @Test
    void aBayWithAModuleInItIsNotSold() {
        Campaign campaign = campaign(15, 20000);
        HangarState state = state(campaign);
        select(state, LoadoutSlot.UTILITY_1);
        selectBayRow(state);
        state.confirm();
        select(state, LoadoutSlot.UTILITY_3);
        // Buy the first buyable module into the new bay.
        int buyable = -1;
        for (int i = 0; i < state.rows().size(); i++) {
            if (state.rows().get(i).state() == Hangar.State.BUYABLE
                    && state.rows().get(i).choice(Action.BUY).orElseThrow().allowed()) {
                buyable = i;
                break;
            }
        }
        assertTrue(buyable >= 0);
        state.pointRow(buyable);
        assertEquals(Outcome.DONE, state.confirm());
        assertTrue(campaign.gear().loadout().containsKey(LoadoutSlot.UTILITY_3));
        selectBayRow(state);

        assertEquals(
                Optional.of(Hangar.BayRefusal.IN_USE),
                state.bayRow().orElseThrow().refusal());
        assertEquals(Outcome.REFUSED, state.confirm());
        assertEquals("UNFIT ITS MODULE FIRST", state.message());
        assertEquals(1, campaign.gear().bays());
    }

    @Test
    void undoingTheBayClosesItsSlotAgain() {
        Campaign campaign = campaign(15, 9000);
        HangarState state = state(campaign);
        select(state, LoadoutSlot.UTILITY_1);
        selectBayRow(state);
        state.confirm();
        select(state, LoadoutSlot.UTILITY_3);
        state.up();
        while (state.focus() != HangarState.Focus.COMMANDS) {
            state.up();
        }
        while (state.command() != HangarState.Command.UNDO) {
            state.left();
        }

        assertEquals(Outcome.DONE, state.confirm());

        assertEquals(9000, campaign.credits());
        assertFalse(state.slots().contains(LoadoutSlot.UTILITY_3));
        assertEquals(LoadoutSlot.UTILITY_2, state.slot());
    }

    @Test
    void theShieldOverchargeIsBoughtOnceAndShowsItsCooldown() {
        Campaign campaign = campaign(19, 9000);
        HangarState state = state(campaign);
        select(state, LoadoutSlot.SPECIAL);
        int row = -1;
        for (int i = 0; i < state.rows().size(); i++) {
            if (state.rows().get(i).item().id().equals(OVERCHARGE)) {
                row = i;
            }
        }
        assertTrue(row >= 0, "in the special's shop at the L19 visit");
        state.pointRow(row);
        assertEquals(
                List.of(Action.BUY),
                state.selected().orElseThrow().choices().stream()
                        .map(Hangar.Choice::action)
                        .toList());
        assertEquals("BUY 5 000", ShopPanel.choiceLabel(state.choice().orElseThrow()));

        assertEquals(Outcome.DONE, state.confirm());

        assertEquals(4000, campaign.credits());
        assertEquals(OVERCHARGE, campaign.loadout().get(LoadoutSlot.SPECIAL).item());
        var item = CATALOGUE.item(ItemKind.SPECIAL, OVERCHARGE);
        assertEquals(
                "45S", LoadoutPanel.value(campaign, CATALOGUE, LoadoutSlot.SPECIAL, Optional.of(item)), "its tile");
        assertEquals("OVRCHG", Names.tile(item.name()));
        // Fitted, it is sold back like gear, never charged.
        assertEquals(
                List.of(Action.SELL),
                state.rows().getFirst().choices().stream()
                        .map(Hangar.Choice::action)
                        .toList());
        // Round 37, R.4 (e): bought this visit, its sale returns the full price and the detail says so;
        // without, the 60 % sell-back.
        Hangar.Offer fitted = state.rows().getFirst();
        assertTrue(ShopPanel.fullRefund(fitted));
        assertEquals(-5000, fitted.choice(Action.SELL).orElseThrow().credits());
        assertEquals(
                "SELL 5 000", ShopPanel.choiceLabel(fitted.choice(Action.SELL).orElseThrow()));
        HangarState next = state(campaign);
        select(next, LoadoutSlot.SPECIAL);
        Hangar.Offer owned = next.rows().getFirst();
        assertEquals(OVERCHARGE, owned.item().id());
        assertFalse(ShopPanel.fullRefund(owned), "a later visit sells it back at the share");
        assertEquals(
                -CATALOGUE.sellPrice(item, 1),
                owned.choice(Action.SELL).orElseThrow().credits());
    }

    /** Round 37, R.4 (e): the Overcharge against a fitted charge special shares no stat: no empty VS FITTED line. */
    @Test
    void aSpecialWithNothingToCompareHasNoVsFittedLine() {
        var overcharge = CATALOGUE.item(ItemKind.SPECIAL, OVERCHARGE);
        var airstrike = CATALOGUE.item(ItemKind.SPECIAL, "Airstrike");
        assertEquals(List.of(), ShopPanel.deltas(overcharge, 1, airstrike, 1));
        var emp = CATALOGUE.item(ItemKind.SPECIAL, "EMP Burst");
        assertFalse(ShopPanel.deltas(emp, 1, airstrike, 1).isEmpty(), "the charges still compare");
    }

    /**
     * Round 37, R.4 (f): specials draw no power, so a loadout already over its generator's output (a
     * hand-written save) still fits the EMP Burst; a change that adds draw is still refused there.
     */
    @Test
    void anOverdrawnLoadoutStillFitsASpecialButNoMoreDraw() {
        Map<LoadoutSlot, Fitted> loadout = new java.util.EnumMap<>(RULES.starterLoadout());
        for (LoadoutSlot slot :
                List.of(LoadoutSlot.FRONT, LoadoutSlot.REAR, LoadoutSlot.LEFT_WING, LoadoutSlot.RIGHT_WING)) {
            var heaviest = CATALOGUE.items(slot.kind()).stream()
                    .max(java.util.Comparator.comparingDouble(candidate -> candidate.draw(candidate.maxLevel())))
                    .orElseThrow();
            loadout.put(slot, new Fitted(heaviest.id(), heaviest.maxLevel()));
        }
        Campaign campaign = campaign(19, 90000, loadout);
        campaign.giveFreeCharges(CONTENT.specials());
        Hangar hangar = new Hangar(CATALOGUE, campaign);
        assertTrue(hangar.load() > hangar.output(), "over its output: " + hangar.load() + " / " + hangar.output());
        Hangar.Offer emp = hangar.shop(LoadoutSlot.SPECIAL).stream()
                .filter(offer -> offer.item().id().equals("EMP Burst"))
                .findFirst()
                .orElseThrow();
        assertEquals(Hangar.State.OWNED, emp.state(), "its free charge");
        Hangar.Choice fit = emp.choice(Action.FIT).orElseThrow();
        assertEquals(Optional.empty(), fit.refusal(), "it draws nothing");
        assertTrue(hangar.apply(LoadoutSlot.SPECIAL, emp, Action.FIT));
        assertEquals("EMP Burst", campaign.loadout().get(LoadoutSlot.SPECIAL).item());
        // An upgrade that adds draw is still refused for power.
        hangar.shop(LoadoutSlot.UTILITY_1).stream()
                .filter(offer ->
                        offer.state() == Hangar.State.BUYABLE && offer.item().draw(1) > 0)
                .findFirst()
                .ifPresent(offer ->
                        assertFalse(offer.choice(Action.BUY).orElseThrow().fits(), "into the inventory"));
    }

    /** Round 37, R.4 (g): the visit's free-charge notice leaves the message line on the next move. */
    @Test
    void theFreeChargeNoticeGoesOnTheNextMove() {
        HangarState state = state(campaign(19, 9000));
        state.notice("1 FREE EMP BURST CHARGE");
        assertEquals("1 FREE EMP BURST CHARGE", state.message());
        state.nextSlot();
        assertEquals("", state.message());
        state.notice("1 FREE EMP BURST CHARGE");
        state.down();
        assertEquals("", state.message(), "a row move too");
        // A transaction's message stays until the next transaction.
        select(state, LoadoutSlot.UTILITY_1);
        selectBayRow(state);
        state.confirm();
        state.down();
        assertEquals("BOUGHT UTILITY BAY 3 - ITS SLOT IS OPEN", state.message());
    }

    @Test
    void theActThreeItemsAreInTheLiveShop() {
        assertEquals(Catalogue.AWAITING_ICONS, java.util.Set.of());
        for (String weapon : List.of("ion-beam", "rear-lance", "deflector-pod", "light-drone-bay")) {
            assertTrue(
                    List.of(ItemKind.FRONT, ItemKind.REAR, ItemKind.WING).stream()
                            .anyMatch(kind -> CATALOGUE.items(kind).stream()
                                    .anyMatch(item -> item.id().equals(weapon))),
                    weapon);
        }
        for (String special : List.of("EMP Burst", "Orbital Lance", OVERCHARGE)) {
            assertEquals(special, CATALOGUE.item(ItemKind.SPECIAL, special).id());
        }
        assertEquals("special-emp-burst", ItemIcons.name(CATALOGUE.item(ItemKind.SPECIAL, "EMP Burst")));
        assertEquals("DRONE BAY", Names.callout("Light Drone Bay"));
        assertEquals("DEFLECTOR", Names.callout("Deflector Pod"));
    }

    @Test
    void theMouseBuysTheBayByItsChipAndPicksItsTile() {
        HangarState state = state(campaign(15, 9000));
        FakePointer mouse = new FakePointer();
        HangarPointer pointer = new HangarPointer();
        select(state, LoadoutSlot.UTILITY_1);
        selectBayRow(state);
        mouse.move(ShopPanel.bayChip(state.bayRow().orElseThrow()));
        pointer.update(state, mouse.pointer);
        mouse.click();
        HangarPointer.Result click = pointer.update(state, mouse.pointer);
        assertTrue(click.confirm(), "a click on the chip confirms");
        assertEquals(Outcome.DONE, state.confirm());

        var tile = LoadoutPanel.slots(false, true).get(LoadoutSlot.UTILITY_3);
        mouse.move(tile);
        pointer.update(state, mouse.pointer);
        mouse.click();
        pointer.update(state, mouse.pointer);
        assertEquals(LoadoutSlot.UTILITY_3, state.slot());
    }
}
