package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.sim.Loadout;

/**
 * M9 part A, step A2 (m9a-gaps.md §1 #6, #11, #12, #14): Act 5's shop (the Tesla Coil Pod from the L29 visit, the Swarm
 * Tail from L30's, the Mk V "Helix" from L35's), the flight's power budget (the generator's output, the fitted load and
 * the shield's draw, for a drainer's latch), and Rook's return: at Level 29's win he is back with full armour, the slot
 * his again, the loaner gone and an owned Bulwark kept.
 */
class BeltCampaignTest {
    private static final vanguard.content.Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.complete(CONTENT);

    /** A campaign at {@code level} with Rook hired and plenty of credits. */
    private static Campaign at(int level) {
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, level);
        campaign.gear(campaign.gear().withCredits(40_000));
        return campaign;
    }

    private static Hangar.State state(int level, LoadoutSlot slot, String id) {
        return new Hangar(CATALOGUE, at(level))
                .shop(slot).stream()
                        .filter(offer -> offer.item().id().equals(id))
                        .findFirst()
                        .orElseThrow()
                        .state();
    }

    @Test
    void theShopSellsAct5sItemsFromTheirVisits() {
        assertEquals(Hangar.State.LOCKED, state(28, LoadoutSlot.LEFT_WING, "tesla-coil-pod"));
        assertEquals(Hangar.State.BUYABLE, state(29, LoadoutSlot.LEFT_WING, "tesla-coil-pod"));
        assertEquals(3000, CATALOGUE.item(ItemKind.WING, "tesla-coil-pod").price());
        assertEquals(Hangar.State.LOCKED, state(29, LoadoutSlot.REAR, "swarm-tail"));
        assertEquals(Hangar.State.BUYABLE, state(30, LoadoutSlot.REAR, "swarm-tail"));
        assertEquals(3500, CATALOGUE.item(ItemKind.REAR, "swarm-tail").price());
        String helix = CATALOGUE.items(ItemKind.GENERATOR).get(4).id();
        assertEquals(35, CATALOGUE.items(ItemKind.GENERATOR).get(4).unlock());
        assertEquals(Hangar.State.LOCKED, state(34, LoadoutSlot.GENERATOR, helix));
        assertEquals(Hangar.State.BUYABLE, state(35, LoadoutSlot.GENERATOR, helix));
    }

    @Test
    void theFlightCarriesItsPowerBudget() {
        Campaign campaign = at(29);
        Flight flight = Flight.of(CONTENT, CATALOGUE, campaign);
        Loadout.Power power = flight.loadout().power().orElseThrow();
        Map<LoadoutSlot, Fitted> loadout = campaign.gear().loadout();
        double output = CATALOGUE
                .item(ItemKind.GENERATOR, loadout.get(LoadoutSlot.GENERATOR).item())
                .stats(1)
                .get(Catalogue.Stat.OUTPUT);
        assertEquals(output, power.output(), 1e-9);
        assertEquals(output - flight.sparePower(), power.load(), 1e-9);
        assertEquals(
                CONTENT.shields().models().stream()
                        .filter(model -> model.name()
                                .equals(loadout.get(LoadoutSlot.SHIELD).item()))
                        .findFirst()
                        .orElseThrow()
                        .draw(),
                power.shieldDraw(),
                1e-9);
        assertEquals(2, power.drain().perDrainer(), 1e-9);
        assertEquals(6, power.drain().most(), 1e-9);
        assertEquals(0.5, power.drain().floor(), 1e-9);
    }

    @Test
    void rookIsBackAtLevel29sWinWithFullArmourAndTheSlotHis() {
        Campaign campaign = at(26);
        Hangar hangar = new Hangar(CATALOGUE, campaign);
        assertTrue(hangar.buyBulwark(), "an owned Bulwark");
        campaign.complete(CampaignTest.won("B", 0, 0), 50, 0, 0, OptionalDouble.of(90));
        campaign.complete(CampaignTest.won("B", 0, 0), 50, 0, 0, OptionalDouble.of(70));
        campaign.complete(CampaignTest.won("B", 0, 0), 50, 0, 0, OptionalDouble.of(60));
        assertEquals(29, campaign.nextLevel());
        assertTrue(campaign.rookMissing());
        // His armour as he was lost (low), the owned Bulwark flying his slot.
        campaign.gear(campaign.gear().withEscort(campaign.gear().escort().withArmour(5)));
        assertEquals(
                Escort.Escorter.BULWARK, campaign.escortFlight().orElseThrow().escorter());
        double bulwark = campaign.gear().escort().bulwark().armour();

        campaign.complete(CampaignTest.won("B", 0, 0), 50, 0, 0, OptionalDouble.of(40));
        assertEquals(30, campaign.nextLevel());
        assertFalse(campaign.rookMissing());
        assertFalse(campaign.storyFlags().contains(Campaign.ROOK_MISSING));
        assertEquals(campaign.escortMaxArmour(), campaign.gear().escort().armour(), 1e-9, "a fresh start");
        assertEquals(Escort.Escorter.ROOK, campaign.gear().escort().flies(), "the slot defaults to Rook");
        assertTrue(campaign.rookFlies());
        assertTrue(campaign.gear().escort().bulwark().owned(), "the owned Bulwark stays");
        assertEquals(40, campaign.gear().escort().bulwark().armour(), 1e-9, "with its armour as it left");
        assertTrue(bulwark > 0);
        assertFalse(new Hangar(CATALOGUE, campaign).rookMissing(), "his card and repair line are back");
        assertTrue(new Hangar(CATALOGUE, campaign).chooseEscort(Escort.Escorter.BULWARK), "still a choice");
    }

    @Test
    void withoutAnOwnedBulwarkTheLoanerIsGoneFromLevel30() {
        Campaign campaign = at(29);
        assertTrue(campaign.escortFlight().orElseThrow().loaner());
        assertTrue(campaign.escortFlight(30).orElseThrow().rook());
        assertFalse(campaign.gear().escort().bulwark().owned());
    }

    /**
     * M9 part B, step B2 (design/player/wingmen, Rook's rescue and return): Level 29's flight carries the loaner and Rook
     * waiting in the prison tender's hold with the gun fitted at his loss, at its level and full armour; Level 28's has
     * no rescue, and the {@code --escort} debug options never carry one (Rook from the start: no swap to join; none: the
     * Ember leaves).
     */
    @Test
    void levelTwentyNinesFlightCarriesRooksRescue() {
        Campaign campaign = at(29);
        Fitted gun = campaign.gear().loadout().get(LoadoutSlot.ESCORT);
        Campaign.EscortFlight flight = campaign.escortFlight().orElseThrow();
        assertTrue(flight.loaner());
        assertEquals(java.util.Optional.of(gun), flight.rescue());
        assertTrue(campaign.escortFlight(28).orElseThrow().rescue().isEmpty(), "Level 28: no rescue");
        Loadout loadout = Flight.of(CONTENT, CATALOGUE, campaign).loadout();
        assertTrue(loadout.bulwark().isPresent(), "the loaner in the slot");
        assertTrue(loadout.wingman().isEmpty(), "Rook is not in the slot yet");
        vanguard.sim.WingmanSpec rook = loadout.rescue().orElseThrow();
        assertEquals(campaign.escortMaxArmour(), rook.armour(), 1e-9, "full armour");

        campaign.debugEscort(java.util.Optional.of(gun), java.util.Optional.empty());
        assertTrue(campaign.escortFlight().orElseThrow().rescue().isEmpty(), "--escort rook: he flies from the start");
        campaign.debugEscort(java.util.Optional.empty(), java.util.Optional.empty());
        assertTrue(campaign.escortFlight().isEmpty(), "--escort none: nobody, no rescue");
    }
}
