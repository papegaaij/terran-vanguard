package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part D, step D1: Level 38's mechanics together step without allocating and replay: Harrow streams from the top
 * and the bottom and a cross from all four edges (seeded chases, locked rams, rings), lightning over a secret pod, the
 * supply canister under fire and a kill ratio scoped to the Harrows.
 */
class Act6DAllocationsTest {
    static LevelScript level() {
        HazardSpec.Strikes strikes =
                Act6BSpecs.strikes(0, new HazardSpec.Strike(1.5, 60, false), new HazardSpec.Strike(5, 0, true));
        HazardSpec hazards = HazardSpec.NONE
                .withWeather(Optional.of(Act6BSpecs.storm(strikes)))
                .withDebris(List.of(Act6DSpecs.pod(0.5, 60), Act6DSpecs.canister(1, Ship.START_X)));
        List<WaveSpec> waves = List.of(
                Act6DSpecs.stream(0.5, 4, WaveSpec.Entry.FRONT, WaveSpec.Edge.ALTERNATING),
                Act6DSpecs.stream(3, 3, WaveSpec.Entry.REAR, WaveSpec.Edge.ALTERNATING),
                Act6DSpecs.cross(6, 4));
        return Act6DSpecs.level(30, hazards, waves, Secondary.killRatioOf("harrow", 0.65, 100, "HARROWS"));
    }

    static Sortie sortie() {
        return new Sortie(7, TestSpecs.LOADOUT, level(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** 12 s, firing in bursts and weaving. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Act6BAllocationsTest.command(i));
        }
    }

    @Test
    void levelThirtyEightsMechanicsStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act6DAllocationsTest::sortie, Act6DAllocationsTest::fly));
    }

    @Test
    void itReplays() {
        Sortie one = sortie();
        Sortie two = sortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(12); i++) {
            one.step(Act6BAllocationsTest.command(i));
            two.step(Act6BAllocationsTest.command(i));
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        assertTrue(seen[SimEvents.Type.DASH_START.ordinal()] > 0);
        assertTrue(seen[SimEvents.Type.SECRET_FOUND.ordinal()] > 0, "the pod opened");
        assertTrue(seen[SimEvents.Type.DEBRIS_RELEASED.ordinal()] > 0, "the canister released");
    }
}
