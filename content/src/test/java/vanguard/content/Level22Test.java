package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.CueTrigger;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M8 part B, step B3: Level 22 Ice Shelf from its data (design/campaign/act-4-deep-water/level-22-ice-shelf;
 * design/enemies/ground/drill-rig): its count, sections and quiet window, the four Drill Rigs with their pumps, ring and
 * first bonus, the ice geysers (Level 19's vent rows with a row's own period), the per-difficulty RIGS secondary, the
 * radio events, and whole runs of the autopilot with the balance plan's fit (the Airstrike on a rig about to get away,
 * {@link TargetPlay#hardenedEscapee}, in the lower half of the screen: {@link TargetPlay#rigStrikeNow}). The pacing, the floor, the beam era's HP and the haul are the campaign-wide
 * tests' ({@link PacingTest}, {@link ActFloorTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level22Test {
    static final String LEVEL = "act-4-deep-water/level-22-ice-shelf";
    private static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 22 ({@code "22"}: the Harpoon Torpedoes bought into the inventory, the Ion Beam
     * L3 fitted for the dry ice): Level 21's, the Airstrike with its four charges and Rook's Autocannon. No gun of it is
     * {@code anti-ground}: the rigs fall to the Airstrike, one charge a rig.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level21Test.planLoadout(content, difficulty);
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level21Test.withoutRook(content, difficulty);
    }

    static Sortie sortie(Content content, long seed, Difficulty difficulty, boolean invulnerable) {
        Loadout loadout = planLoadout(content, difficulty);
        Rules rules = SimSpecs.rules(content, LEVEL, difficulty);
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, LEVEL, difficulty),
                invulnerable ? rules.withInvulnerableShip() : rules,
                loadout.plating().maxArmour());
    }

    /**
     * The README: 273 enemies at medium, the waves' 243 and the ground targets' 30 (the crevasse lip is an object); the
     * approved draft's 48 waves and step B3b's 19 firing groups (the Act 2+ floor).
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        int waves = level.waves().stream().mapToInt(LevelMetrics::planned).sum();
        assertEquals(67, level.waves().size());
        assertEquals(243, waves);
        assertEquals(30, level.groundUnits().size());
        assertEquals(1, level.groundObjects().size(), "the crevasse lip");
        assertEquals(1, level.secrets());
    }

    /** Three sections: the shelf at 120 px/s, the rig field at 60 and the run-in at 110, each change eased over 2 s. */
    @Test
    void theSectionsSlowForTheRigField() {
        List<Section> sections =
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).sections();
        assertEquals(3, sections.size());
        assertEquals(
                List.of(36.0, 104.0, 150.0), sections.stream().map(Section::end).toList());
        assertEquals(
                List.of(120.0, 60.0, 110.0),
                sections.stream().map(Section::speed).toList());
        assertEquals(2, sections.get(1).rampSeconds());
        assertEquals(2, sections.get(2).rampSeconds());
    }

    /** The first rig's quiet window: no wave enters t=39.5–43.5 on any difficulty (the rig and its nest only). */
    @Test
    void theFirstRigHasItsQuietWindow() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                assertTrue(wave.t() < 39.5 || wave.t() >= 43.5, difficulty + ": a wave at t=" + wave.t());
            }
        }
    }

    /**
     * The four Drill Rigs (t=40, 56, 72, 88; x 130, 340, 150, 330): a hardened core of 150 HP paying 40, four pumps of
     * 24 HP paying 4 each that carry the ring and slow it 0.5 s each once lost, the core's first bonus of 16 shared as 4
     * a pump (a core-first kill pays the whole 56); the ring of 10 every 3.5 s (easy 4.5 s), hard with an aimed shard.
     */
    @Test
    void theRigsCarryTheirPumpsAndTheirRing() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<GroundUnit> rigs = SimSpecs.level(content, LEVEL, difficulty).groundUnits().stream()
                    .filter(unit -> unit.enemy().slug().equals("drill-rig"))
                    .toList();
            assertEquals(
                    List.of(40.0, 56.0, 72.0, 88.0),
                    rigs.stream().map(GroundUnit::t).toList());
            assertEquals(
                    List.of(130.0, 340.0, 150.0, 330.0),
                    rigs.stream().map(GroundUnit::x).toList());
            EnemySpec rig = rigs.getFirst().enemy();
            assertTrue(rig.hardened());
            assertEquals(40, rig.bounty());
            assertEquals(4, rig.parts().size());
            for (EnemySpec.UnitPart pump : rig.parts()) {
                assertEquals(4, pump.bounty());
                assertEquals(4, pump.firstBonus(), "16 shared by four pumps");
                assertTrue(pump.silencesGun(), "the pumps carry the ring");
                assertEquals(0.5, pump.slowsGunSeconds());
            }
            EnemyGun ring = rig.gun().orElseThrow();
            assertEquals(10, ring.fan());
            assertEquals(110 * content.difficulty().enemyBulletSpeed().of(difficulty), ring.bulletSpeed(), 1e-9);
            switch (difficulty) {
                case EASY -> {
                    assertEquals(4.5, ring.intervalSeconds(), 1e-9);
                    assertEquals(0, ring.aimedShards());
                }
                case MEDIUM -> {
                    assertEquals(150, rig.hp());
                    assertEquals(24, rig.parts().getFirst().hp());
                    assertEquals(3.5, ring.intervalSeconds(), 1e-9);
                    assertEquals(0, ring.aimedShards());
                }
                case HARD -> assertEquals(1, ring.aimedShards());
            }
        }
    }

    /**
     * The ice geysers: eight rows of three cracks (40 px, heavy 10), tell 1.2 s, burn 0.8 s; the shelf and the rig field
     * every 4 s (easy 5, hard 3.2), the run-in's three rows every 3.2 s (easy 4, hard 2.6 with a 0.3 s ripple).
     */
    @Test
    void theGeysersRunFasterOnTheRunIn() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<HazardSpec.VentRow> rows =
                    SimSpecs.level(content, LEVEL, difficulty).hazards().vents();
            assertEquals(
                    List.of(12.0, 31.0, 50.5, 66.0, 84.0, 107.0, 118.5, 136.5),
                    rows.stream().map(HazardSpec.VentRow::t).toList());
            double[] period = {5, 4, 3.2};
            double[] runIn = {4, 3.2, 2.6};
            int d = difficulty.ordinal();
            for (HazardSpec.VentRow row : rows) {
                assertEquals(3, row.grates());
                assertEquals(40, row.width());
                assertEquals(10, row.damage(), "heavy");
                assertEquals(1.2, row.tellSeconds());
                assertEquals(0.8, row.burnSeconds());
                boolean late = row.t() > 104;
                assertEquals(late ? runIn[d] : period[d], row.periodSeconds(), 1e-9, difficulty + " t=" + row.t());
                assertEquals(late && difficulty == Difficulty.HARD ? 0.3 : 0.4, row.rippleSeconds(), 1e-9);
            }
        }
    }

    /** RIGS: at least 3 of the 4 cores broken at medium (one may get away), 2 on easy, all 4 on hard; 100 credits. */
    @Test
    void theRigsSecondaryAllowsEscapesByDifficulty() {
        int[] allowed = {2, 1, 0};
        for (Difficulty difficulty : Difficulty.values()) {
            Secondary secondary =
                    SimSpecs.level(content, LEVEL, difficulty).objectives().secondary();
            assertEquals("drill-rig", secondary.escapes());
            assertEquals(allowed[difficulty.ordinal()], secondary.escapesAllowed(), difficulty.name());
            assertEquals("RIGS", secondary.label());
            assertEquals(100, secondary.credits());
        }
    }

    /**
     * The radio's events: Varga's crack line at the first geyser's tell (t≈12), Dr. Brandt's at the first rig core
     * destroyed and at the secondary met; Rook's two lines only with him on the wing. Flown at medium on seed 1.
     */
    @Test
    void theRadioEventsFireInTheRun() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<RadioCue> radio = level.radio();
        int geyser = index(radio, CueTrigger.FIRST_GEYSER);
        int target = index(radio, CueTrigger.TARGET_DESTROYED);
        assertTrue(geyser >= 0 && target >= 0);
        assertEquals(
                2,
                radio.stream()
                        .filter(cue -> cue.speaker().equals("Rook") && cue.requires() != 0)
                        .count(),
                "Rook's lines need him fitted");
        Sortie sortie = sortie(content, 1, Difficulty.MEDIUM, false);
        double geyserAt = -1;
        double targetAt = -1;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.RADIO && events.value(i) == geyser && geyserAt < 0) {
                    geyserAt = sortie.levelSeconds();
                } else if (events.type(i) == SimEvents.Type.RADIO && events.value(i) == target && targetAt < 0) {
                    targetAt = sortie.levelSeconds();
                }
            }
        }
        assertTrue(geyserAt > 11 && geyserAt < 14, "the first geyser's line: " + geyserAt);
        assertTrue(targetAt > 40, "the first rig destroyed: " + targetAt);
    }

    private static int index(List<RadioCue> radio, CueTrigger trigger) {
        for (int i = 0; i < radio.size(); i++) {
            if (radio.get(i).trigger() == trigger) {
                return i;
            }
        }
        return -1;
    }

    /**
     * The autopilot completes the level with the plan's fit on every difficulty and seed, and meets the RIGS secondary
     * on most seeds of every difficulty with the Airstrike (the plan's fit has no {@code anti-ground} gun): step B3b's
     * core of 150 HP (hard 195) falls to two of a strike's blasts, under its cap of 300, so the four charges break all
     * four rigs on hard. Printed: the secondary's rate, the credits, the retries.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 22 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS), difficulty + " seed " + seed);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d",
                    difficulty, met, SEEDS, credits / (double) SEEDS, retries));
            assertTrue(met * 2 > SEEDS, difficulty + ": the RIGS secondary on most seeds: " + met);
        }
        System.out.println(report);
    }

    /** The autopilot completes the level without Rook too ({@code --escort none}), one seed per difficulty. */
    @Test
    void theAutopilotFliesTheLevelWithoutRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelMetrics.Run run =
                    LevelMetrics.autopilot(content, LEVEL, difficulty, withoutRook(content, difficulty), 1, MAX_STEPS);
            assertTrue(run.complete(), difficulty.name());
        }
    }

    /**
     * With an {@code anti-ground} gun (the Bomb Rack L3 on a wing, no Airstrike, no Rook) the autopilot flies over each
     * rig and bombs its lowest pump first ({@link TargetPlay#overRig}). Printed: the pumps and rigs broken (step B3b: the
     * Bomb Rack L3 alone, about 27 DPS, breaks four pumps but still no 150 HP core in the autopilot's run).
     */
    @Test
    void theAutopilotBombsTheRigsPumpsFirstWithAnAntiGroundGun() {
        Loadout bombs = SimSpecs.loadout(
                content,
                content.systems().engines().getFirst().name(),
                List.of(
                        new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 3),
                        new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "bomb-rack", 3),
                        new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 2),
                        new SimSpecs.FittedWeapon(Armament.Slot.REAR, "tail-gun", 3)),
                content.shields().models().get(1).name(),
                content.armour().plating().get(3).name(),
                0,
                Difficulty.MEDIUM);
        Sortie sortie = LevelMetrics.sortie(content, LEVEL, Difficulty.MEDIUM, bombs, 1);
        int rigKind = sortie.enemyKinds().stream().map(EnemySpec::slug).toList().indexOf("drill-rig");
        int rigs = 0;
        int pumps = 0;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.value(i) == rigKind && events.type(i) == SimEvents.Type.ENEMY_DESTROYED) {
                    rigs++;
                } else if (events.value(i) == rigKind && events.type(i) == SimEvents.Type.UNIT_PART_DESTROYED) {
                    pumps++;
                }
            }
        }
        System.out.println("Level 22 with the Bomb Rack L3: " + pumps + " pumps and " + rigs + " rigs broken");
        assertTrue(sortie.complete());
        assertTrue(pumps >= 4, "pumps bombed: " + pumps);
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level22Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(content, 77, Difficulty.MEDIUM, false);
        flyToEnd(first);
        flyToEnd(second);

        assertEquals(first.stateHash(), second.stateHash());
    }

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
