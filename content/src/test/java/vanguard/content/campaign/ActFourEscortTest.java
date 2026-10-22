package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.sim.WingmanSpec;

/**
 * M8 part A (design/player/wingmen, Heavy drone "Bulwark" and Rook's loss and absence; design/systems/saves, version
 * 6): the escort slot holds Rook or the Bulwark, the loaner while Rook is missing, the Bulwark's shop line, choice and
 * repair, the story flags, the hydro-kit notice, the free Sonar Pulse charge and the {@code --escort bulwark} option.
 */
class ActFourEscortTest {
    private static final vanguard.content.Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.complete(CONTENT);

    /** A campaign at {@code level} with Rook hired and plenty of credits. */
    private static Campaign at(int level) {
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, level);
        campaign.gear(campaign.gear().withCredits(20_000));
        return campaign;
    }

    @Test
    void theRulesComeFromTheData() {
        assertEquals(new CampaignRules.BulwarkRules(120, 6000, 22, 8), RULES.bulwark());
        assertEquals(26, RULES.escort().lostAfter());
        assertEquals(30, RULES.escort().back());
        assertEquals(23, RULES.hydroKit());
        assertEquals(
                List.of(false, true, true, true, false),
                List.of(26, 27, 28, 29, 30).stream()
                        .map(level -> RULES.escort().missing(level))
                        .toList());
    }

    @Test
    void rookFliesUntilHeIsMissingAndALoanerBulwarkTakesHisSlotThen() {
        Campaign campaign = at(26);
        assertTrue(campaign.rookFlies());
        assertFalse(campaign.rookMissing());

        Campaign.EscortFlight loaner = campaign.escortFlight(27).orElseThrow();
        assertEquals(Escort.Escorter.BULWARK, loaner.escorter());
        assertTrue(loaner.loaner());
        assertEquals(Optional.empty(), loaner.gun());
        assertEquals(120, loaner.armour());
        assertTrue(campaign.escortFlight(29).orElseThrow().loaner());
        assertTrue(campaign.escortFlight(30).orElseThrow().rook(), "Rook is back from Level 30");

        Campaign missing = at(27);
        assertTrue(missing.rookMissing());
        assertTrue(missing.escortFlies(), "the slot is never empty");
        assertFalse(missing.rookFlies(), "his requires: escort lines are dropped");
        assertFalse(missing.escortGrounded());
        assertFalse(missing.escortArmourLow());
    }

    @Test
    void rookIsRecordedMissingFromLevel26sWinToLevel29s() {
        Campaign campaign = at(26);
        campaign.complete(CampaignTest.won("B", 0, 0), 50);
        assertTrue(campaign.storyFlags().contains(Campaign.ROOK_MISSING));
        campaign.complete(CampaignTest.won("B", 0, 0), 50);
        campaign.complete(CampaignTest.won("B", 0, 0), 50);
        assertTrue(campaign.storyFlags().contains(Campaign.ROOK_MISSING));
        campaign.complete(CampaignTest.won("B", 0, 0), 50);
        assertEquals(30, campaign.nextLevel());
        assertFalse(campaign.storyFlags().contains(Campaign.ROOK_MISSING));
        assertTrue(campaign.rookFlies());
    }

    @Test
    void theBulwarkIsBoughtFromLevel22ChosenAndSoldBack() {
        Hangar early = new Hangar(CATALOGUE, at(21));
        assertEquals(Hangar.State.LOCKED, early.bulwark().state());
        assertFalse(early.buyBulwark());

        Campaign campaign = at(22);
        Hangar hangar = new Hangar(CATALOGUE, campaign);
        assertEquals(Hangar.State.BUYABLE, hangar.bulwark().state());
        assertEquals(6000, hangar.bulwark().credits());
        assertTrue(hangar.buyBulwark());
        assertEquals(14_000, campaign.credits());
        assertEquals(new Escort.Bulwark(true, 120), campaign.gear().escort().bulwark());
        assertEquals(Escort.Escorter.BULWARK, campaign.gear().escort().flies());
        assertEquals(
                Escort.Escorter.BULWARK, campaign.escortFlight().orElseThrow().escorter());
        assertFalse(campaign.escortFlight().orElseThrow().loaner());
        assertEquals(-6000, hangar.bulwark().credits(), "bought during the visit: sold for all it cost");

        assertTrue(hangar.chooseEscort(Escort.Escorter.ROOK));
        assertTrue(campaign.rookFlies());
        assertFalse(hangar.chooseEscort(Escort.Escorter.ROOK), "chosen already");
        assertTrue(hangar.undo());
        assertEquals(Escort.Escorter.BULWARK, campaign.gear().escort().flies());
        assertTrue(hangar.undo());
        assertFalse(campaign.gear().escort().bulwark().owned());
        assertEquals(20_000, campaign.credits());

        assertTrue(hangar.buyBulwark());
        Hangar later = new Hangar(CATALOGUE, Campaign.load(RULES, campaign.save(Instant.EPOCH)));
        assertEquals(-3600, later.bulwark().credits(), "the sell-back share after the visit");
        assertTrue(later.sellBulwark());
        assertEquals(14_000 + 3600, later.campaign().credits());
        assertEquals(Escort.Escorter.ROOK, later.campaign().gear().escort().flies());
        assertFalse(later.chooseEscort(Escort.Escorter.BULWARK), "none owned");
    }

    @Test
    void theBulwarksArmourIsKeptRepairedAtItsOwnCostAndADestroyedOneIsRebuilt() {
        Campaign campaign = at(22);
        Hangar hangar = new Hangar(CATALOGUE, campaign);
        assertTrue(hangar.buyBulwark());

        campaign.complete(CampaignTest.won("B", 0, 0), 50, 0, 0, OptionalDouble.of(70));
        assertEquals(70, campaign.gear().escort().bulwark().armour());
        assertEquals(80, campaign.gear().escort().armour(), "Rook did not fly: his armour is unchanged");

        Hangar visit = new Hangar(CATALOGUE, campaign);
        assertEquals(50, visit.bulwarkMissingArmour());
        assertEquals(8, visit.bulwarkRepairCost());
        int credits = campaign.credits();
        assertTrue(visit.repairBulwark(10));
        assertEquals(credits - 80, campaign.credits());
        assertEquals(80, campaign.gear().escort().bulwark().armour());
        assertTrue(visit.undo());
        assertEquals(70, campaign.gear().escort().bulwark().armour());

        campaign.complete(CampaignTest.won("B", 0, 0), 50, 0, 0, OptionalDouble.of(0));
        assertEquals(120, campaign.gear().escort().bulwark().armour(), "rebuilt for the next level");
        assertEquals(120, campaign.escortFlight().orElseThrow().armour());
    }

    @Test
    void aLoanerFliesWholeAndKeepsNothing() {
        Campaign campaign = at(27);
        campaign.complete(CampaignTest.won("B", 0, 0), 50, 0, 0, OptionalDouble.of(10));
        assertFalse(campaign.gear().escort().bulwark().owned());
        assertEquals(120, campaign.escortFlight().orElseThrow().armour());
        assertEquals(80, campaign.gear().escort().armour(), "Rook's armour is not the loaner's");
    }

    @Test
    void anOwnedBulwarkTakesRooksSlotWhileHeIsMissing() {
        Campaign campaign = at(27);
        campaign.gear(campaign.gear().withEscort(campaign.gear().escort().withBulwark(new Escort.Bulwark(true, 90))));
        Campaign.EscortFlight flight = campaign.escortFlight().orElseThrow();
        assertEquals(Escort.Escorter.BULWARK, flight.escorter());
        assertFalse(flight.loaner());
        assertEquals(90, flight.armour());
    }

    @Test
    void whileRookIsMissingHisCardOffersNoGunsAndNoRepair() {
        Campaign campaign = at(27);
        campaign.gear(campaign.gear().withEscort(campaign.gear().escort().withArmour(30)));
        Hangar hangar = new Hangar(CATALOGUE, campaign);
        assertTrue(hangar.rookMissing());
        assertEquals(List.of(), hangar.shop(LoadoutSlot.ESCORT));
        assertEquals(0, hangar.escortMissingArmour());
        assertEquals(
                "autocannon", campaign.gear().loadout().get(LoadoutSlot.ESCORT).item(), "his gun is kept");
    }

    @Test
    void theHydroKitNoticeShowsOnceAtTheVisitBeforeLevel23() {
        Campaign before = at(22);
        assertFalse(before.hydroKitNotice());
        Campaign campaign = at(23);
        assertTrue(campaign.hydroKitNotice());
        assertFalse(campaign.hydroKitNotice());
        assertFalse(Campaign.load(RULES, campaign.save(Instant.EPOCH)).hydroKitNotice());
    }

    @Test
    void theSonarPulsesFreeChargeComesAtLevel22AndIsFittedOnlyIntoAnEmptySlot() {
        Campaign campaign = at(22);
        List<Campaign.FreeCharges> given = campaign.giveFreeCharges(CONTENT.specials());
        // The Airstrike's free charge (Level 04) came first and filled the slot.
        assertTrue(given.contains(new Campaign.FreeCharges("Sonar Pulse", 1, false)), given.toString());
        assertEquals(1, campaign.gear().charges("Sonar Pulse"));
        assertEquals(
                "Airstrike", campaign.gear().loadout().get(LoadoutSlot.SPECIAL).item());
        assertTrue(at(21).giveFreeCharges(CONTENT.specials()).stream()
                .noneMatch(free -> free.special().equals("Sonar Pulse")));
    }

    @Test
    void theEscortDebugOptionFliesTheBulwark() {
        DebugFit fit = DebugFit.NONE.withEscort("bulwark,side=right");
        assertEquals(
                Optional.of(new DebugFit.EscortFit(Optional.empty(), Optional.of(WingmanSpec.Side.RIGHT), true)),
                fit.escort());
        Campaign campaign = at(12);
        fit.applyTo(campaign, CATALOGUE);
        Campaign.EscortFlight flight = campaign.escortFlight().orElseThrow();
        assertEquals(Escort.Escorter.BULWARK, flight.escorter());
        assertEquals(WingmanSpec.Side.RIGHT, flight.side());
        assertEquals(120, flight.armour());
        assertFalse(campaign.gear().escort().bulwark().owned(), "never the gear's");
        assertThrows(IllegalArgumentException.class, () -> DebugFit.NONE.withEscort("bulwark:2"));
        assertThrows(IllegalArgumentException.class, () -> DebugFit.NONE.withEscort("warden"));
    }

    @Test
    void aDebugStartAtLevel27TreatsRookAsMissingUnlessHeIsGiven() {
        Campaign campaign = at(27);
        assertTrue(campaign.escortFlight().orElseThrow().loaner());
        DebugFit.NONE.withEscort("rook:missiles:2").applyTo(campaign, CATALOGUE);
        assertTrue(campaign.rookFlies());
    }
}
