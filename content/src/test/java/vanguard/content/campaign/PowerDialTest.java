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
import vanguard.content.campaign.Catalogue.Item;
import vanguard.content.campaign.Hangar.Action;
import vanguard.content.campaign.Hangar.Choice;
import vanguard.content.campaign.Hangar.Offer;
import vanguard.content.campaign.Hangar.Refusal;
import vanguard.content.campaign.Hangar.State;
import vanguard.sim.Armament;

/**
 * The power dial (the user, 2026-10-21, design/player#power-budget): a fitted weapon's power setting turns down to 1
 * and up to its bought level, for free; the weapon draws and flies at its setting.
 */
class PowerDialTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Item PULSE = HangarTest.CATALOGUE.item(ItemKind.FRONT, "pulse-cannon");

    /** A campaign before Level 02 with the starter loadout but for {@code changes}. */
    private static Campaign campaign(int credits, Map<LoadoutSlot, Fitted> changes) {
        Campaign campaign = HangarTest.campaign(Difficulty.MEDIUM, 2, credits, 60);
        Gear gear = campaign.gear();
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(gear.loadout());
        loadout.putAll(changes);
        campaign.gear(
                new Gear(gear.credits(), loadout, gear.inventory(), gear.specials(), gear.armour(), gear.escort()));
        return campaign;
    }

    private static Offer pulse(Hangar hangar) {
        return HangarTest.row(hangar, LoadoutSlot.FRONT, "Pulse Cannon", State.FITTED);
    }

    private static Choice choice(Hangar hangar, Action action) {
        return pulse(hangar).choice(action).orElseThrow();
    }

    @Test
    void theDialTurnsAWeaponDownAndUpForFreeAndChangesItsDraw() {
        Campaign campaign = campaign(500, Map.of(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 3)));
        Hangar hangar = new Hangar(HangarTest.CATALOGUE, campaign);
        double full = hangar.load();
        assertEquals(
                Optional.of(Refusal.FULL_POWER), choice(hangar, Action.POWER_UP).refusal());
        Choice down = choice(hangar, Action.POWER_DOWN);
        assertTrue(down.allowed());
        assertEquals(0, down.credits());
        assertEquals(full - PULSE.draw(3) + PULSE.draw(2), down.load(), 1e-9);

        assertTrue(hangar.apply(LoadoutSlot.FRONT, pulse(hangar), Action.POWER_DOWN));

        assertEquals(new Fitted("pulse-cannon", 3, 2), campaign.loadout().get(LoadoutSlot.FRONT));
        assertEquals(full - PULSE.draw(3) + PULSE.draw(2), hangar.load(), 1e-9);
        assertEquals(500, campaign.credits());
        assertEquals(3, pulse(hangar).level());
        assertEquals(2, pulse(hangar).power());

        assertTrue(hangar.apply(LoadoutSlot.FRONT, pulse(hangar), Action.POWER_DOWN));
        assertEquals(1, pulse(hangar).power());
        assertEquals(
                Optional.of(Refusal.MIN_POWER),
                choice(hangar, Action.POWER_DOWN).refusal());
        assertFalse(hangar.apply(LoadoutSlot.FRONT, pulse(hangar), Action.POWER_DOWN));

        assertTrue(hangar.apply(LoadoutSlot.FRONT, pulse(hangar), Action.POWER_UP));
        assertEquals(new Fitted("pulse-cannon", 3, 2), campaign.loadout().get(LoadoutSlot.FRONT));
        assertTrue(hangar.undo(), "a turn of the dial is part of the visit's undo");
        assertEquals(new Fitted("pulse-cannon", 3, 1), campaign.loadout().get(LoadoutSlot.FRONT));
        assertEquals(500, campaign.credits());
    }

    @Test
    void onlyTheShipsWeaponsHaveADial() {
        Hangar hangar = new Hangar(HangarTest.CATALOGUE, campaign(0, Map.of()));
        for (LoadoutSlot slot : List.of(LoadoutSlot.GENERATOR, LoadoutSlot.SHIELD, LoadoutSlot.ENGINE)) {
            Offer fitted = hangar.shop(slot).getFirst();
            assertEquals(State.FITTED, fitted.state());
            assertTrue(fitted.choice(Action.POWER_DOWN).isEmpty(), slot.toString());
            assertTrue(fitted.choice(Action.POWER_UP).isEmpty(), slot.toString());
        }
        assertEquals(
                Optional.of(Refusal.MIN_POWER),
                choice(hangar, Action.POWER_DOWN).refusal());
    }

    /** Turned down, a weapon makes room for another item; raising it again is checked like an upgrade. */
    @Test
    void theRoomATurnedDownWeaponMakesCanBeUsedAndRaisingItIsCheckedAgainstTheOutput() {
        Campaign campaign = campaign(
                0,
                Map.of(
                        LoadoutSlot.FRONT, new Fitted("pulse-cannon", 5, 1),
                        LoadoutSlot.LEFT_WING, new Fitted("autocannon-pod", 1),
                        LoadoutSlot.RIGHT_WING, new Fitted("autocannon-pod", 1),
                        LoadoutSlot.UTILITY_1, new Fitted(Hangar.SENSOR_SUITE, 1),
                        LoadoutSlot.UTILITY_2, new Fitted("Pickup magnet", 1)));
        Hangar hangar = new Hangar(HangarTest.CATALOGUE, campaign);
        assertEquals(hangar.output(), hangar.load(), 1e-9, "the Mk I's 8 MW all used, the Pulse Cannon at setting 1");
        assertTrue(hangar.output() < hangar.load() - PULSE.draw(1) + PULSE.draw(5), "at full power it would not fit");

        Choice up = choice(hangar, Action.POWER_UP);

        assertEquals(Optional.of(Refusal.POWER), up.refusal());
        assertEquals(hangar.load() - PULSE.draw(1) + PULSE.draw(2), up.load(), 1e-9);
        assertFalse(hangar.apply(LoadoutSlot.FRONT, pulse(hangar), Action.POWER_UP));
        assertTrue(hangar.apply(
                LoadoutSlot.UTILITY_2,
                HangarTest.row(hangar, LoadoutSlot.UTILITY_2, "Pickup magnet", State.FITTED),
                Action.UNFIT));
        assertTrue(choice(hangar, Action.POWER_UP).allowed());
    }

    @Test
    void anUpgradeRaisesTheSettingOnlyAtFullPower() {
        Campaign campaign = campaign(10_000, Map.of(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 2)));
        Hangar hangar = new Hangar(HangarTest.CATALOGUE, campaign);

        assertTrue(hangar.apply(LoadoutSlot.FRONT, pulse(hangar), Action.UPGRADE));
        assertEquals(new Fitted("pulse-cannon", 3, 3), campaign.loadout().get(LoadoutSlot.FRONT));

        assertTrue(hangar.apply(LoadoutSlot.FRONT, pulse(hangar), Action.POWER_DOWN));
        double load = hangar.load();
        Choice upgrade = choice(hangar, Action.UPGRADE);
        assertEquals(load, upgrade.load(), 1e-9, "a turned-down weapon's upgrade draws nothing more");
        assertEquals(PULSE.upgradeCost(3), upgrade.credits());
        assertTrue(hangar.apply(LoadoutSlot.FRONT, pulse(hangar), Action.UPGRADE));
        assertEquals(new Fitted("pulse-cannon", 4, 2), campaign.loadout().get(LoadoutSlot.FRONT));
        assertEquals(load, hangar.load(), 1e-9);
    }

    @Test
    void anUnfittedWeaponKeepsItsSettingAndRefitsAtIt() {
        Campaign campaign = campaign(0, Map.of(LoadoutSlot.REAR, new Fitted("tail-gun", 3, 1)));
        Hangar hangar = new Hangar(HangarTest.CATALOGUE, campaign);
        Item tail = HangarTest.CATALOGUE.item(ItemKind.REAR, "tail-gun");
        double load = hangar.load();

        assertTrue(hangar.apply(
                LoadoutSlot.REAR, HangarTest.row(hangar, LoadoutSlot.REAR, "Tail Gun", State.FITTED), Action.UNFIT));
        assertEquals(List.of(new Fitted("tail-gun", 3, 1)), campaign.gear().inventory(ItemKind.REAR));
        Offer owned = HangarTest.row(hangar, LoadoutSlot.REAR, "Tail Gun", State.OWNED);
        assertEquals(1, owned.power());
        assertEquals(load, owned.choice(Action.FIT).orElseThrow().load(), 1e-9);
        assertEquals(load - tail.draw(1), hangar.load(), 1e-9);
    }

    /** The weapon flies at its setting: the same mount (and overdrive, one level more) as one bought at that level. */
    @Test
    void aTurnedDownWeaponFliesAtItsSetting() {
        Flight dialled = Flight.of(
                CONTENT,
                HangarTest.CATALOGUE,
                campaign(0, Map.of(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 4, 2))));
        Flight bought = Flight.of(
                CONTENT, HangarTest.CATALOGUE, campaign(0, Map.of(LoadoutSlot.FRONT, new Fitted("pulse-cannon", 2))));

        assertEquals(List.of(new Flight.Weapon(Armament.Slot.FRONT, "Pulse Cannon", 2)), dialled.weapons());
        assertEquals(bought.loadout(), dialled.loadout());
        assertEquals(bought.sparePower(), dialled.sparePower());
        assertEquals(8 - 2 - PULSE.draw(2), dialled.sparePower(), 1e-9, "Mk I 8 MW less shield Mk I 2 and the setting");
    }
}
