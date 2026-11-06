package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1a: the Act 7 enemy side together steps without allocating and replays: a staggered pair of
 * shelled Gate Wardens under a Tesla pod and a beam (closed sparks, breaker holds, the windows, the leaving tell, the
 * exit up), a stream of jumping Rift Skaters, a mirroring Seraph's spiral and a chain, all in a drifting current with a
 * reversal and its tells.
 */
class Act7AAllocationsTest {
    static LevelScript level() {
        WaveSpec wardens = WaveSpec.builder(
                        0,
                        WaveSpec.Formation.LINE_ABREAST,
                        Act7ASpecs.warden(6, true),
                        2,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .lineStagger(Optional.of(new WaveSpec.LineStagger(1.3, false)))
                .build();
        WaveSpec chain = WaveSpec.builder(
                        1, WaveSpec.Formation.SNAKE, FarsideTest.COILWYRM, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(
                        List.of(new WaveSpec.At(120, -60), new WaveSpec.At(120, 300), new WaveSpec.At(120, 700))))
                .build();
        HazardSpec.Current current = new HazardSpec.Current(
                List.of(new HazardSpec.Bend(0, 0), new HazardSpec.Bend(3, 15), new HazardSpec.Bend(9, -10)),
                List.of(new HazardSpec.Reversal(10, 2)),
                2);
        return Act7ASpecs.level(
                        30,
                        List.of(
                                wardens,
                                chain,
                                Act7ASpecs.stream(0.5, Act7ASpecs.SKATER, 5, 0.5),
                                Act7ASpecs.single(2, Act7ASpecs.SERAPH)),
                        Optional.of(current))
                .toBuilder()
                .sections(List.of(new Section(10, 130), new Section(14, -130, false, 2, 1), new Section(30, 130)))
                .build();
    }

    static Sortie sortie() {
        return new Sortie(
                3,
                TestSpecs.loadout(
                        PartASpecs.beamMount(5),
                        new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA),
                        new Armament.Mount(Armament.Slot.RIGHT_WING, TestSpecs.PULSE, TestSpecs.PULSE_L2)),
                level(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** 16 s, firing in bursts and weaving. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(16); i++) {
            sortie.step(command(i));
        }
    }

    private static int command(int i) {
        int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
        int weave = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
        return fire | (i % 240 < 120 ? 0 : weave);
    }

    @Test
    void theActSevenEnemySideStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7AAllocationsTest::sortie, Act7AAllocationsTest::fly));
    }

    @Test
    void itReplays() {
        Sortie one = sortie();
        Sortie two = sortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(16); i++) {
            one.step(command(i));
            two.step(command(i));
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        for (SimEvents.Type type : List.of(
                SimEvents.Type.SHELL_OPEN,
                SimEvents.Type.SHELL_FLAKE,
                SimEvents.Type.JUMP_RING,
                SimEvents.Type.ARRIVE,
                SimEvents.Type.REVERSAL_TOLD,
                SimEvents.Type.REVERSAL_RUN)) {
            assertTrue(seen[type.ordinal()] > 0, type.name());
        }
    }

    /**
     * M11 part A, step A1b: the Act 7 player side and hazards together: a Resonator up front, the Swarm Call called
     * twice (a pickup-free cooldown of 8 s here), Rook beside the ship, spore clouds burnt by the waves' targets' blasts
     * and the Tesla's sparks, a quiet broken by the first hit, Needlers firing at the ship and the drones.
     */
    static LevelScript playerLevel() {
        VrellLevel vrell = VrellLevel.NONE
                .withSpores(Optional.of(SporeCloudTest.spores(List.of(
                        new VrellLevel.Cloud(0, Ship.START_X, 90), new VrellLevel.Cloud(5, Ship.START_X - 80, 70)))))
                .withQuiet(Optional.of(new VrellLevel.Quiet(0, 4)));
        List<GroundUnit> column = new java.util.ArrayList<>();
        for (int k = 0; k < 8; k++) {
            column.add(new GroundUnit(1 + 1.5 * k, Ship.START_X, Act6BSpecs.target("target", Layer.AIR, 120), -1));
        }
        return Act6ASpecs.level(
                        30,
                        column,
                        List.of(
                                Act7ASpecs.stream(0.5, TestSpecs.NEEDLER, 8, 0.7),
                                Act7ASpecs.stream(1, TestSpecs.SKITTER, 10, 0.5)))
                .toBuilder()
                .vrell(vrell)
                .build();
    }

    static Sortie playerSortie() {
        SpecialSpec call = SpecialSpec.cooldown("Vrell Swarm Call", 0.5, 8, 10, SwarmCallTest.SWARM);
        return new Sortie(
                5,
                TestSpecs.loadout(
                                ResonatorTest.mount(),
                                new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA))
                        .withSpecial(call)
                        .withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                playerLevel(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** 16 s, firing in bursts and weaving, the special pressed every 11.5 s. */
    static void flyPlayer(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(16); i++) {
            sortie.step(command(i) | (i % SimStep.ticks(11.5) == 30 ? Command.SPECIAL.bit() : 0));
        }
    }

    @Test
    void theActSevenPlayerSideStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7AAllocationsTest::playerSortie, Act7AAllocationsTest::flyPlayer));
    }

    @Test
    void thePlayerSideReplays() {
        Sortie one = playerSortie();
        Sortie two = playerSortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(16); i++) {
            int commands = command(i) | (i % SimStep.ticks(11.5) == 30 ? Command.SPECIAL.bit() : 0);
            one.step(commands);
            two.step(commands);
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        for (SimEvents.Type type : List.of(
                SimEvents.Type.WAVE_FIRED,
                SimEvents.Type.WAVE_HIT,
                SimEvents.Type.SWARM_CALLED,
                SimEvents.Type.SWARM_THORN,
                SimEvents.Type.SWARM_WITHER,
                SimEvents.Type.SPORE_INSIDE,
                SimEvents.Type.SPORE_BURN,
                SimEvents.Type.QUIET_BROKEN)) {
            assertTrue(seen[type.ordinal()] > 0, type.name());
        }
    }

    /** The rush: three fights, the refills, a Retry from boss in the second, under fire held throughout. */
    static void flyRush(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(40); i++) {
            sortie.step(Command.FIRE.bit());
            if (i == SimStep.ticks(26) && sortie.bossCheckpoint()) {
                sortie.retryFromBoss();
            }
        }
    }

    @Test
    void theRushStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(BossRushTest::sortie, Act7AAllocationsTest::flyRush));
    }

    /**
     * M11 part B, step B1: the gate transit (formation at 8 s, the warp from 12.5 s, the end at 14 s) with Rook beside
     * the ship, the Resonator against a rock slab, a right-to-left Warden line, a Skater stream under the landing kill's
     * and the kind kills' lines, and a chain whose tail carries the armour patch.
     */
    static LevelScript transitLevel() {
        WaveSpec wardens = WaveSpec.builder(
                        0,
                        WaveSpec.Formation.LINE_ABREAST,
                        Act7ASpecs.warden(5, true),
                        2,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .lineStagger(Optional.of(new WaveSpec.LineStagger(1.3, false, true)))
                .build();
        WaveSpec chain = WaveSpec.builder(
                        1, WaveSpec.Formation.SNAKE, FarsideTest.COILWYRM, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(
                        List.of(new WaveSpec.At(240, -60), new WaveSpec.At(240, 300), new WaveSpec.At(240, 700))))
                .carried(List.of(new WaveSpec.Carried(PickupType.ARMOUR_PATCH, 0, false, true)))
                .build();
        List<GroundUnit> column = new java.util.ArrayList<>();
        for (int k = 0; k < 6; k++) {
            column.add(new GroundUnit(0.5 + k, Ship.START_X - 30, Act6BSpecs.target("target", Layer.AIR, 60), -1));
        }
        Terrain slab = new Terrain(
                Optional.empty(),
                Optional.empty(),
                List.of(new Terrain.Island(new double[] {300, 330, 330, 300}, new double[] {-400, -400, 600, 600})));
        RadioCue[] cues = {
            new RadioCue(CueTrigger.FIRST_LANDING_KILL, 0, "", "Rook", "Line.", false, "neutral", "Rook"),
            new RadioCue(CueTrigger.FIRST_KIND_KILL, 0, "coilwyrm", "Rook", "Line.", false, "neutral", "Rook"),
            new RadioCue(CueTrigger.ESCORT_FIRST_KILL, 0, "rift-skater", "Rook", "Line.", false, "neutral", "Rook")
        };
        LevelScript level = Act6ASpecs.level(
                        14, column, List.of(wardens, chain, Act7ASpecs.stream(0.5, Act7ASpecs.SKATER, 6, 0.5)))
                .toBuilder()
                .radio(List.of(cues))
                .vrell(VrellLevel.NONE.withTransit(Optional.of(new VrellLevel.Transit(8, 12.5, 1.5))))
                .build();
        return TestSpecs.withGround(
                level, g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(slab, 10, 28, 0.12, 0.5))));
    }

    static Sortie transitSortie() {
        return new Sortie(
                7,
                TestSpecs.loadout(
                                ResonatorTest.mount(),
                                new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA))
                        .withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                transitLevel(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** To the level's end inside the warp, firing in bursts (no weaving: the slab stands right of the ship). */
    static void flyTransit(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(14) && !sortie.complete(); i++) {
            sortie.step(i % 90 < 50 ? Command.FIRE.bit() : 0);
        }
    }

    @Test
    void theTransitTheTriggersAndTheRockedWaveStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7AAllocationsTest::transitSortie, Act7AAllocationsTest::flyTransit));
    }

    @Test
    void theTransitReplays() {
        Sortie one = transitSortie();
        Sortie two = transitSortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(14) && !one.complete(); i++) {
            int commands = i % 90 < 50 ? Command.FIRE.bit() : 0;
            one.step(commands);
            two.step(commands);
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        assertTrue(one.complete());
        for (SimEvents.Type type : List.of(
                SimEvents.Type.TRANSIT_FORM,
                SimEvents.Type.TRANSIT_WARP,
                SimEvents.Type.WAVE_FIRED,
                SimEvents.Type.ARRIVE,
                SimEvents.Type.LINE_TELEGRAPH)) {
            assertTrue(seen[type.ordinal()] > 0, type.name());
        }
    }
}
