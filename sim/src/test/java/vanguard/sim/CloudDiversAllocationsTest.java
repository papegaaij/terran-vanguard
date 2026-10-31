package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part B, step B1: Level 36's unit rules together step without allocating and replay: the fleet's drop craft from
 * the bottom edge, a firing Chimera swarm under its cap, a harvester pair with its escorts, the pathfinder pod and a
 * strike that kills (its {@code first-strike-kill} line).
 */
class CloudDiversAllocationsTest {
    static LevelScript level() {
        List<WaveSpec> waves = new ArrayList<>();
        waves.add(CloudDiversSpecs.dropCraft(1));
        waves.add(CloudDiversSpecs.swarm(2, CloudDiversSpecs.CHIMERA, 10));
        waves.addAll(CloudDiversSpecs.pair(3, CloudDiversSpecs.HARVESTER, 2));
        HazardSpec hazards = HazardSpec.NONE
                .withDebris(List.of(CloudDiversSpecs.pod(1, Ship.START_X)))
                .withWeather(Optional.of(Act6BSpecs.storm(Act6BSpecs.strikes(
                        0, new HazardSpec.Strike(4, 120, false), new HazardSpec.Strike(8, 0, true)))));
        return CloudDiversSpecs.level(
                40,
                hazards,
                List.of(new GroundUnit(3, 120, Act6BSpecs.target("struck", Layer.AIR, 40), -1)),
                waves,
                1,
                List.of(
                        CloudDiversSpecs.cue(CueTrigger.FIRST_STRIKE_KILL, ""),
                        CloudDiversSpecs.cue(CueTrigger.SECRET, "pathfinder-pod")));
    }

    static Sortie sortie() {
        return CloudDiversSpecs.sortie(level());
    }

    /** 14 s, firing in bursts and weaving. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(14); i++) {
            sortie.step(Act6BAllocationsTest.command(i));
        }
    }

    @Test
    void levelThirtySixsUnitRulesStepWithoutAllocating() {
        assertEquals(0, Allocations.least(CloudDiversAllocationsTest::sortie, CloudDiversAllocationsTest::fly));
    }

    @Test
    void itReplays() {
        Sortie one = sortie();
        Sortie two = sortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(14); i++) {
            one.step(Act6BAllocationsTest.command(i));
            two.step(Act6BAllocationsTest.command(i));
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        assertEquals(2, seen[SimEvents.Type.NEUTRAL_LEFT.ordinal()], "the drop craft away");
        assertTrue(seen[SimEvents.Type.ENEMY_FIRED.ordinal()] > 0, "the swarm fires");
        assertTrue(seen[SimEvents.Type.STRIKE.ordinal()] > 0);
        assertTrue(seen[SimEvents.Type.RADIO.ordinal()] > 0);
    }
}
