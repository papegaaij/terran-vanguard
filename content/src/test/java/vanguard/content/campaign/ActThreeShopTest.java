package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Catalogue.Stat;
import vanguard.content.campaign.Hangar.Action;
import vanguard.content.campaign.Hangar.BayRefusal;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.Hangar.State;

/**
 * M7 part A (step A2): Act 3's shop in the content: the new weapons and specials priced from the data
 * but kept out of the live catalogue until their hangar icons exist ({@link Catalogue#AWAITING_ICONS}),
 * the Shield Overcharge bought once and sold back, the third utility bay bought and sold, the Orbital
 * Lance as an anti-ground source and the {@code utility3=} debug option.
 */
class ActThreeShopTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Catalogue COMPLETE = Catalogue.complete(CONTENT);
    private static final String OVERCHARGE = "Shield Overcharge";

    private static Hangar visit(Campaign campaign) {
        return new Hangar(COMPLETE, campaign);
    }

    @Test
    void theLiveCatalogueWaitsForTheActThreeIconsTheCompleteOneHasThem() {
        Catalogue live = Catalogue.of(CONTENT);
        for (ItemKind kind : ItemKind.values()) {
            for (Catalogue.Item item : COMPLETE.items(kind)) {
                boolean awaiting = Catalogue.AWAITING_ICONS.contains(item.id());
                assertEquals(
                        !awaiting,
                        live.items(kind).stream().anyMatch(other -> other.id().equals(item.id())),
                        item.id());
            }
        }
        assertEquals(
                Catalogue.AWAITING_ICONS.size(),
                Catalogue.AWAITING_ICONS.stream()
                        .filter(id -> List.of(ItemKind.values()).stream().anyMatch(kind -> COMPLETE.items(kind).stream()
                                .anyMatch(item -> item.id().equals(id))))
                        .count(),
                "every awaited item is in the data");
        // The thrusters' icon exists: they are in the live shop (from the L15 visit).
        assertTrue(
                live.items(ItemKind.UTILITY).stream().anyMatch(item -> item.id().equals("Evasive thrusters")));
    }

    @Test
    void theNewWeaponsArePricedFromTheirData() {
        Catalogue.Item beam = COMPLETE.item(ItemKind.FRONT, "ion-beam");
        assertEquals(List.of(1250, 2500, 5000, 10000), beam.upgrades());
        assertEquals(List.of(4.0, 5.0, 6.0, 7.0, 8.0), beam.draws());
        assertEquals(60, beam.stats(2).get(Stat.DPS), 1e-9);
        Catalogue.Item lance = COMPLETE.item(ItemKind.REAR, "rear-lance");
        assertEquals(List.of(3.0, 3.5, 4.0, 4.5, 5.0), lance.draws());
        assertEquals(18, lance.stats(1).get(Stat.DPS), 1e-9);
        Catalogue.Item deflector = COMPLETE.item(ItemKind.WING, "deflector-pod");
        assertEquals(List.of(2.0, 2.5, 2.5, 3.0, 3.0), deflector.draws());
        assertEquals(0, deflector.stats(5).get(Stat.DPS), 1e-9);
        Catalogue.Item drones = COMPLETE.item(ItemKind.WING, "light-drone-bay");
        assertEquals(List.of(1000, 2000, 4000, 8000), drones.upgrades());
        assertEquals(19.2, drones.stats(3).get(Stat.DPS), 1e-9);
    }

    @Test
    void theShieldOverchargeIsBoughtOnceAndSoldBack() {
        Campaign campaign = HangarTest.campaign(Difficulty.MEDIUM, 19, 6000, 60);
        Hangar hangar = visit(campaign);
        Catalogue.Item overcharge = COMPLETE.item(ItemKind.SPECIAL, OVERCHARGE);
        assertTrue(overcharge.cooldownSpecial());
        assertEquals(Optional.of(45.0), COMPLETE.cooldown(overcharge));
        assertEquals(Optional.empty(), COMPLETE.cooldown(COMPLETE.item(ItemKind.SPECIAL, "Airstrike")));

        Offer buyable = HangarTest.row(hangar, LoadoutSlot.SPECIAL, OVERCHARGE, State.BUYABLE);
        assertEquals(List.of(Action.BUY), actions(buyable));
        assertEquals(5000, buyable.choice(Action.BUY).orElseThrow().credits());
        assertTrue(hangar.apply(LoadoutSlot.SPECIAL, buyable, Action.BUY));

        assertEquals(1000, campaign.credits());
        assertEquals(1, campaign.gear().charges(OVERCHARGE));
        assertEquals(new Fitted(OVERCHARGE, 1), campaign.gear().loadout().get(LoadoutSlot.SPECIAL));
        Offer fitted = HangarTest.row(hangar, LoadoutSlot.SPECIAL, OVERCHARGE, State.FITTED);
        assertEquals(List.of(Action.SELL), actions(fitted));
        assertEquals(-5000, fitted.choice(Action.SELL).orElseThrow().credits(), "all of it during the visit");

        // At the next visit it sells for the sell-back share.
        Campaign next = Campaign.load(CampaignTest.RULES, campaign.save(java.time.Instant.EPOCH));
        Hangar later = visit(next);
        Offer owned = HangarTest.row(later, LoadoutSlot.SPECIAL, OVERCHARGE, State.FITTED);
        assertEquals(-3000, owned.choice(Action.SELL).orElseThrow().credits());
        assertTrue(later.apply(LoadoutSlot.SPECIAL, owned, Action.SELL));
        assertEquals(4000, next.credits());
        assertEquals(0, next.gear().charges(OVERCHARGE));
        assertFalse(next.gear().loadout().containsKey(LoadoutSlot.SPECIAL));
        assertTrue(later.undo());
        assertEquals(1, next.gear().charges(OVERCHARGE));
    }

    @Test
    void anOwnedOverchargeOutOfTheSlotIsFittedOrSold() {
        Campaign campaign = HangarTest.campaign(Difficulty.MEDIUM, 19, 6000, 60);
        Hangar hangar = visit(campaign);
        hangar.apply(
                LoadoutSlot.SPECIAL,
                HangarTest.row(hangar, LoadoutSlot.SPECIAL, OVERCHARGE, State.BUYABLE),
                Action.BUY);
        // An Airstrike charge fits the Airstrike; the Overcharge stays owned.
        assertTrue(hangar.apply(
                LoadoutSlot.SPECIAL,
                HangarTest.row(hangar, LoadoutSlot.SPECIAL, "Airstrike", State.BUYABLE),
                Action.BUY_CHARGE));
        Offer owned = HangarTest.row(hangar, LoadoutSlot.SPECIAL, OVERCHARGE, State.OWNED);
        assertEquals(List.of(Action.FIT, Action.SELL), actions(owned));
        assertTrue(owned.bought());
        assertEquals(-5000, owned.choice(Action.SELL).orElseThrow().credits());
        assertTrue(hangar.apply(LoadoutSlot.SPECIAL, owned, Action.FIT));
        assertEquals(new Fitted(OVERCHARGE, 1), campaign.gear().loadout().get(LoadoutSlot.SPECIAL));
        assertEquals(1, campaign.gear().charges("Airstrike"));
    }

    @Test
    void theThirdBayIsBoughtThenItsSlotOpensAndItIsSoldOnlyEmpty() {
        Campaign campaign = HangarTest.campaign(Difficulty.MEDIUM, 15, 9000, 60);
        Hangar hangar = visit(campaign);
        Catalogue.Bay third = COMPLETE.bays().getFirst();
        assertEquals(List.of(), hangar.shop(LoadoutSlot.UTILITY_3), "no shop rows before the bay is bought");
        assertEquals(State.BUYABLE, hangar.bays().getFirst().state());
        assertTrue(hangar.bays().getFirst().allowed());

        assertTrue(hangar.buyBay(third));
        assertEquals(4000, campaign.credits());
        assertEquals(1, campaign.gear().bays());
        assertTrue(campaign.gear().open(LoadoutSlot.UTILITY_3));
        assertEquals(State.OWNED, hangar.bays().getFirst().state());
        assertEquals(-5000, hangar.bays().getFirst().credits(), "all of it during the visit");
        assertFalse(hangar.buyBay(third), "bought once");

        Offer magnet = HangarTest.row(hangar, LoadoutSlot.UTILITY_3, "Pickup magnet", State.BUYABLE);
        assertTrue(hangar.apply(LoadoutSlot.UTILITY_3, magnet, Action.BUY));
        assertEquals(new Fitted("Pickup magnet", 1), campaign.gear().loadout().get(LoadoutSlot.UTILITY_3));
        assertEquals(Optional.of(BayRefusal.IN_USE), hangar.bays().getFirst().refusal());
        assertFalse(hangar.sellBay(third), "not while a module is fitted in it");

        assertTrue(hangar.undo());
        assertTrue(hangar.sellBay(third));
        assertEquals(9000, campaign.credits());
        assertEquals(0, campaign.gear().bays());
        assertTrue(hangar.undo());
        assertEquals(1, campaign.gear().bays());

        // The bay survives the save; at a later visit it sells for 60 %.
        Campaign next = Campaign.load(CampaignTest.RULES, campaign.save(java.time.Instant.EPOCH));
        Hangar later = visit(next);
        assertEquals(-3000, later.bays().getFirst().credits());
        assertTrue(later.sellBay(third));
        assertEquals(7000, next.credits());
    }

    @Test
    void theThirdBayIsRefusedWithoutTheCreditsAndLockedBeforeActThree() {
        Catalogue.Bay third = COMPLETE.bays().getFirst();
        Hangar poor = visit(HangarTest.campaign(Difficulty.MEDIUM, 15, 4999, 60));
        assertEquals(Optional.of(BayRefusal.CREDITS), poor.bays().getFirst().refusal());
        assertFalse(poor.buyBay(third));
        Hangar early = visit(HangarTest.campaign(Difficulty.MEDIUM, 14, 9000, 60));
        assertEquals(State.LOCKED, early.bays().getFirst().state());
        assertFalse(early.buyBay(third));
    }

    @Test
    void theOrbitalLanceBringsAntiGroundTheEmpDoesNot() {
        assertTrue(Hangar.HARDENED_SPECIALS.contains("Orbital Lance"));
        assertFalse(Hangar.HARDENED_SPECIALS.contains("EMP Burst"));
        Campaign campaign = HangarTest.campaign(Difficulty.MEDIUM, 17, 2000, 60);
        Hangar hangar = visit(campaign);
        assertTrue(hangar.apply(
                LoadoutSlot.SPECIAL,
                HangarTest.row(hangar, LoadoutSlot.SPECIAL, "Orbital Lance", State.BUYABLE),
                Action.BUY_CHARGE));
        assertTrue(hangar.sourceTraits().contains(Hangar.ANTI_GROUND));
    }

    @Test
    void theLoadoutDebugOptionFitsTheThirdBayAndCountsItBought() {
        Campaign campaign = DebugFit.startAt(CampaignTest.RULES, Difficulty.MEDIUM, 3);
        DebugFit.parse("utility3=pickup-magnet:2").applyTo(campaign, COMPLETE);

        assertEquals(new Fitted("Pickup magnet", 2), campaign.gear().loadout().get(LoadoutSlot.UTILITY_3));
        assertEquals(1, campaign.gear().bays());
    }

    @Test
    void aSaveWithTheBayAndTheOverchargeRoundTrips() throws SaveException {
        Campaign campaign = HangarTest.campaign(Difficulty.MEDIUM, 19, 20_000, 60);
        Gear gear = campaign.gear();
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(gear.loadout());
        loadout.put(LoadoutSlot.SPECIAL, new Fitted(OVERCHARGE, 1));
        campaign.gear(new Gear(
                gear.credits(), loadout, gear.inventory(), Map.of(OVERCHARGE, 1), gear.armour(), gear.escort(), 1));
        SaveGame save = campaign.save(java.time.Instant.EPOCH);

        assertEquals(save, SaveFormat.read(SaveFormat.write(save)));
        // A won level neither uses nor finds charges of a cooldown special.
        campaign.complete(CampaignTest.won("B", 0, 100), 60, 1, 0);
        assertEquals(1, campaign.gear().charges(OVERCHARGE));
    }

    private static List<Action> actions(Offer offer) {
        return offer.choices().stream().map(Hangar.Choice::action).toList();
    }
}
