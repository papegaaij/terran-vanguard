package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 parts C and D, step CD1: Levels 45 and 46's new mechanics together step without allocating and replay: two
 * spawning organs (clusters and Skitter pairs, held near the ship), two pits (a Mote Swarm and a drone trio rising), a
 * Husk trio drifting on their lines with a crack cue, a scenery Leviathan on the play plane under fire, the leaving
 * blink read every step, the first crack's line.
 */
class Act7CDAllocationsTest {
    private static final EnemySpec DRONE = EnemySpec.of("drone", 10, new Hitbox(24, 24), Layer.AIR, 10, true, 5, 40);

    static LevelScript level() {
        VrellLevel vrell = VrellLevel.NONE
                .withPits(Optional.of(new VrellLevel.PitSpec(
                        2.0, 0.5, 3.0, 120, List.of(new VrellLevel.Pit(100, 240), new VrellLevel.Pit(380, 420)))))
                .withCrackCue(Optional.of(new VrellLevel.CrackCue(10, 0.6)))
                .withBottomThird(true);
        WaveSpec swarm = WaveSpec.builder(
                        8, WaveSpec.Formation.SWARM, PartDSpecs.MOTE, 6, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(List.of(new WaveSpec.At(240, -60), new WaveSpec.At(240, 700))))
                .pit(0)
                .build();
        WaveSpec trio = WaveSpec.builder(
                        12, WaveSpec.Formation.LINE_ABREAST, DRONE, 3, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .pit(1)
                .build();
        WaveSpec husks = Act7CDSpecs.drift(
                1,
                Act7CDSpecs.husk(150),
                3,
                true,
                List.of(
                        Act7CDSpecs.line(-40, 60, 520, 120),
                        Act7CDSpecs.line(520, 160, -40, 100),
                        Act7CDSpecs.line(-40, 240, 520, 200)));
        return Act7CDSpecs.level(
                        24,
                        60,
                        List.of(husks, swarm, trio),
                        List.of(
                                new GroundUnit(0, 80, Act7CDSpecs.organ(120), -1),
                                new GroundUnit(4, 400, Act7CDSpecs.organ(120), -1)),
                        vrell)
                .toBuilder()
                .setPieces(List.of(Act7CDSpecs.leviathan(Layer.AIR, true)))
                .radio(List.of(
                        new RadioCue(CueTrigger.FIRST_CRACK, 0, "", "Rook", "Cracked.", false, "neutral", "Rook")))
                .build();
    }

    static Sortie sortie() {
        return new Sortie(3, TestSpecs.LOADOUT, level(), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static int command(int i) {
        int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
        int weave = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
        return fire | (i % 240 < 120 ? 0 : weave);
    }

    static int leaving;

    /** 20 s, firing in bursts and weaving, the leaving blink read every step. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(command(i));
            leaving += sortie.leaving();
        }
    }

    @Test
    void theNewMechanicsStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act7CDAllocationsTest::sortie, Act7CDAllocationsTest::fly));
    }

    @Test
    void theyReplay() {
        Sortie one = sortie();
        Sortie two = sortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(20); i++) {
            one.step(command(i));
            two.step(command(i));
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        for (SimEvents.Type type : List.of(
                SimEvents.Type.ORGAN_SWELL,
                SimEvents.Type.ORGAN_RELEASE,
                SimEvents.Type.SPAWN_RELEASED,
                SimEvents.Type.PIT_TELL,
                SimEvents.Type.PIT_OPEN,
                SimEvents.Type.PIT_CLOSE,
                SimEvents.Type.CRACK_CUE,
                SimEvents.Type.CRACK_TELL,
                SimEvents.Type.CRACK_FAN)) {
            assertTrue(seen[type.ordinal()] > 0, type.name());
        }
    }
}
