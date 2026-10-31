package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.CreditSource;
import vanguard.sim.CueTrigger;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.RadioCue;
import vanguard.sim.WaveSpec;

/**
 * M10 part B, step B2: Level 36's data as the simulation gets it (design/campaign Level 36; the Chimera, the harvester
 * and the fleet Stormhawk's stat blocks; the Act 6 act data). The fixtures of step B1's sim tests
 * ({@code CloudDiversSpecs}) are what this data must map to.
 */
class CloudDiversDataTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    private static final String KEY = Level36Test.LEVEL;

    private static LevelScript level(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, KEY, difficulty);
    }

    private static List<WaveSpec> waves(Difficulty difficulty, String slug) {
        return level(difficulty).waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .toList();
    }

    @Test
    void theChimeraSwoopsInItsWingsAndFlocksUnderItsFireCapInASwarm() {
        List<WaveSpec> chimeras = waves(Difficulty.MEDIUM, "chimera");
        WaveSpec pair = chimeras.getFirst();
        assertEquals(34, pair.t());
        assertEquals(WaveSpec.Formation.LINE_ABREAST, pair.formation());
        assertEquals(2, pair.count());
        EnemySpec wing = pair.enemy();
        assertEquals(40, wing.hp(), "20 base, the beam era's ×2");
        assertEquals(Optional.of(new EnemySpec.Regeneration(0.20, 1.0)), wing.regeneration());
        assertTrue(wing.swoopPath().isPresent());
        assertTrue(wing.flock().isEmpty());
        assertTrue(wing.machine());
        assertEquals(14, wing.bounty());
        WaveSpec swarm = chimeras.stream()
                .filter(wave -> wave.formation() == WaveSpec.Formation.SWARM)
                .findFirst()
                .orElseThrow();
        assertEquals(81, swarm.t());
        EnemySpec member = swarm.enemy();
        assertTrue(member.swoopPath().isEmpty(), "a swarm's Chimera flocks");
        EnemySpec.FlockSpec flock = member.flock().orElseThrow();
        assertEquals(Optional.of(new EnemySpec.SwarmFire(3, 0.3, 300)), flock.fire());
        assertEquals(160, flock.speed());
        assertEquals(
                Optional.of(new EnemySpec.Regeneration(0.15, 1.3)),
                waves(Difficulty.EASY, "chimera").getFirst().enemy().regeneration());
        assertEquals(
                Optional.of(new EnemySpec.Regeneration(0.25, 0.8)),
                waves(Difficulty.HARD, "chimera").getFirst().enemy().regeneration());
    }

    @Test
    void theHarvestersAreUncountedDriftingCarriersPaidWithTheGroundTargets() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> pairs = waves(difficulty, "harvester");
            assertEquals(
                    List.of(72.0, 86.0, 100.0, 114.0),
                    pairs.stream().map(WaveSpec::t).toList());
            for (WaveSpec pair : pairs) {
                assertEquals(WaveSpec.Formation.CARRIER_ESCORTS, pair.formation());
                assertEquals(2, pair.count(), "a pair on every difficulty");
            }
        }
        EnemySpec harvester = waves(Difficulty.MEDIUM, "harvester").getFirst().enemy();
        assertEquals(160, harvester.hp(), "no act or beam-era factor");
        assertEquals(120, waves(Difficulty.EASY, "harvester").getFirst().enemy().hp());
        assertEquals(208, waves(Difficulty.HARD, "harvester").getFirst().enemy().hp());
        assertEquals(Layer.LOW_AIR, harvester.layer(), "no contact: the layer rules (below the play plane)");
        assertFalse(harvester.countsAsEnemy());
        assertFalse(harvester.neutral(), "hostile: picked, hit and paid");
        assertEquals(Optional.of(CreditSource.GROUND_TARGETS), harvester.haul());
        assertEquals(Optional.of(new EnemySpec.CarrierDrift(-8, true)), harvester.carrierDrift());
        assertTrue(harvester.gun().isEmpty());
        assertEquals(30, harvester.bounty());
        // Each pair's escorts: two Talons in orbit (three on hard), right after the pair in the wave list.
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> all = level(difficulty).waves();
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).enemy().slug().equals("harvester")) {
                    WaveSpec escorts = all.get(i + 1);
                    assertEquals("talon", escorts.enemy().slug());
                    assertEquals(WaveSpec.Formation.CARRIER_ESCORTS, escorts.formation());
                    assertEquals(difficulty == Difficulty.HARD ? 3 : 2, escorts.count());
                    assertTrue(escorts.enemy().orbit().isPresent());
                }
            }
        }
    }

    @Test
    void theSecondaryCountsTheHarvestersToItsTarget() {
        assertEquals(
                "harvester", level(Difficulty.MEDIUM).objectives().secondary().escapes());
        assertEquals(2, level(Difficulty.MEDIUM).objectives().secondary().escapesAllowed());
        assertEquals(3, level(Difficulty.EASY).objectives().secondary().escapesAllowed());
        assertEquals(1, level(Difficulty.HARD).objectives().secondary().escapesAllowed());
        assertEquals(
                "HARVESTERS", level(Difficulty.MEDIUM).objectives().secondary().label());
    }

    @Test
    void theDropCraftAreNeutralPairsUpFromTheBottomEdge() {
        List<WaveSpec> craft = waves(Difficulty.MEDIUM, "fleet-stormhawk");
        assertEquals(List.of(4.0, 10.0), craft.stream().map(WaveSpec::t).toList());
        for (WaveSpec pair : craft) {
            assertEquals(WaveSpec.Entry.REAR, pair.entry());
            assertEquals(Optional.of(400.0), pair.speed());
            assertEquals(2, pair.count());
            assertTrue(pair.enemy().neutral());
            assertFalse(pair.enemy().countsAsEnemy());
            assertEquals(0, pair.enemy().bounty());
            assertTrue(pair.enemy().gun().isEmpty());
        }
    }

    @Test
    void thePathfinderPodFreesTheSecretsCrate() {
        List<HazardSpec.DebrisSpec> debris = level(Difficulty.MEDIUM).hazards().debris();
        assertEquals(1, debris.size());
        HazardSpec.DebrisSpec pod = debris.getFirst();
        assertEquals(146, pod.t());
        assertEquals(400, pod.x());
        assertEquals(20, pod.hp());
        assertTrue(pod.holdsSecret());
        assertEquals("pathfinder pod", pod.secret());
        assertEquals(100, pod.crateCredits());
        assertEquals(1, level(Difficulty.MEDIUM).secrets());
    }

    @Test
    void theRadioCarriesTheFirstStrikeKillAndRooksBarksNeedHim() {
        List<RadioCue> radio = level(Difficulty.MEDIUM).radio();
        RadioCue strike = radio.stream()
                .filter(cue -> cue.trigger() == CueTrigger.FIRST_STRIKE_KILL)
                .findFirst()
                .orElseThrow();
        assertEquals("Rook", strike.speaker());
        assertEquals(RadioCue.FITTED_ESCORT, strike.requires());
        assertEquals(
                1,
                radio.stream().filter(cue -> cue.trigger() == CueTrigger.SECRET).count());
        assertEquals(
                0, radio.stream().filter(cue -> cue.speaker().equals("Vorne")).count(), "Vorne is silent here");
        // step B3: Varga's "Too many got through" waits for the secondary's failure, not the first harvester away
        RadioCue failed = radio.stream()
                .filter(cue -> cue.trigger() == CueTrigger.SECONDARY_FAILED)
                .findFirst()
                .orElseThrow();
        assertEquals("Varga", failed.speaker());
        assertTrue(failed.line().startsWith("Too many got through"));
        assertEquals(
                0,
                radio.stream()
                        .filter(cue -> cue.trigger() == CueTrigger.ENEMY_ESCAPED)
                        .count());
    }

    @Test
    void theStrikesAndGustsAreTheLevelsAndTheBanksDrawTheirPieces() {
        HazardSpec.Weather medium = level(Difficulty.MEDIUM).hazards().weather().orElseThrow();
        assertEquals(
                List.of(
                        new HazardSpec.Strike(137, 240, false),
                        new HazardSpec.Strike(151, 120, false),
                        new HazardSpec.Strike(169, 360, false)),
                medium.strikes().orElseThrow().at());
        assertEquals(
                2,
                level(Difficulty.EASY)
                        .hazards()
                        .weather()
                        .orElseThrow()
                        .strikes()
                        .orElseThrow()
                        .at()
                        .size());
        assertEquals(
                4,
                level(Difficulty.HARD)
                        .hazards()
                        .weather()
                        .orElseThrow()
                        .strikes()
                        .orElseThrow()
                        .at()
                        .size());
        BackdropData backdrop = CONTENT.level(KEY).backdrop();
        List<BackdropData.Bank> banks = backdrop.banks().orElseThrow();
        assertEquals(31, banks.size());
        for (BackdropData.Bank bank : banks) {
            BackdropData.Piece piece = backdrop.pieces().get(bank.piece().orElseThrow());
            assertEquals(bank.w(), piece.size().width());
            assertEquals(bank.h(), piece.size().height());
        }
    }

    @Test
    void theActSixIntroHasFourPagesThirdOneInterceptedOverTheMotif() {
        ActData act = CONTENT.acts().get("act-6-jovian-storm");
        assertEquals(new ActData.Levels(36, 42), act.levels());
        assertEquals(new ActData.TitleCard("ACT VI", "JOVIAN STORM", "Jupiter · September 2185"), act.titleCard());
        assertEquals(4, act.briefing().size());
        BriefingPage invitation = act.briefing().get(2);
        assertEquals("Vorne", invitation.speaker());
        assertEquals(Channel.ENEMY, invitation.onChannel());
        assertEquals(Optional.of(BriefingPage.PageMusic.MOTIF), invitation.music());
        assertEquals(Optional.of("act-6-invitation"), invitation.image());
        for (int page : new int[] {0, 1, 3}) {
            assertEquals(Channel.FRIENDLY, act.briefing().get(page).onChannel());
            assertTrue(act.briefing().get(page).music().isEmpty());
        }
        assertEquals(Optional.of(new ActData.NextAct("ACT VII", "BEYOND THE GATE", "THE TETHER GATE")), act.nextAct());
        assertTrue(act.outro().isEmpty(), "the outro is part H's");
    }
}
