package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1b (design/enemies/ground/shield-pylon, The pylon gates; Level 41): a level holds several held
 * arenas, its gates, one after another. Each eases to its halt, stands while one of its own holders stands (the HUD
 * counting that gate's), runs the halt cycle from a cycle's start, opens at its last holder's fall ({@link
 * SimEvents.Type#GATE_OPENED}) and eases out; then the next gate is current. Level 29's one arena is unchanged (its
 * tests and the golden flights).
 */
class HeldGatesTest {
    /** 100 px/s to 10 s, gate 0 halting at 12 s, 120 px/s (eased over 2 s) to 20 s, gate 1 halting at 22 s, on to 40 s. */
    static final List<Section> SECTIONS = List.of(
            new Section(10, 100),
            new Section(12, 0, true),
            new Section(20, 120, false, 2, 2),
            new Section(22, 0, true),
            new Section(40, 120, false, 2, 2));

    /** Two clamps for gate 0 (entering at 9 s), one for gate 1 (at 19 s). */
    static List<GroundUnit> holders() {
        return List.of(
                new GroundUnit(9, 150, Act5BSpecs.CLAMP, -1).asHolder(),
                new GroundUnit(9, 330, Act5BSpecs.CLAMP, -1).asHolder(),
                new GroundUnit(19, 240, Act5BSpecs.CLAMP, -1).asHolder());
    }

    static LevelScript level(Optional<HaltCycle> cycle) {
        return LevelScript.builder(41, 6)
                .sections(SECTIONS)
                .groundUnits(holders())
                .objectives(ObjectiveSpec.NONE)
                .haltCycle(cycle)
                .build();
    }

    private static void untilHalt(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(30) && !sortie.halting(); i++) {
            sortie.step(0);
        }
        assertTrue(sortie.halting(), "a gate halts");
    }

    private static int run(Sortie sortie, double seconds, SimEvents.Type type) {
        return Act6BSpecs.run(sortie, SimStep.ticks(seconds), 0, type);
    }

    @Test
    void eachGateHoldsTheClockUntilItsOwnHoldersFall() {
        Sortie sortie = TestSpecs.sortie(level(Optional.empty()));
        assertEquals(2, sortie.gates());
        untilHalt(sortie);
        assertEquals(12, sortie.levelSeconds(), 2 * SimStep.SECONDS);
        assertEquals(0, sortie.gate());
        assertEquals(2, sortie.holders(), "gate 0's own");
        assertEquals(2, sortie.holdersStanding());
        double clock = sortie.levelSeconds();
        run(sortie, 3, SimEvents.Type.GATE_OPENED);
        assertEquals(clock, sortie.levelSeconds(), "the clock stands");
        assertEquals(3, sortie.haltSeconds(), 2 * SimStep.SECONDS);
        assertEquals(2, HeldArenaTest.killHolders(sortie));
        assertEquals(1, PartCSpecs.count(sortie, SimEvents.Type.GATE_OPENED));
        assertEquals(0, sortie.events().value(gateEvent(sortie)), "gate 0 opened");
        sortie.step(0);
        assertFalse(sortie.halting());
        assertEquals(1, sortie.gate(), "the next gate is current");
        assertEquals(1, sortie.holders());
        untilHalt(sortie);
        assertEquals(22, sortie.levelSeconds(), 2 * SimStep.SECONDS);
        assertTrue(sortie.haltSeconds() < 2 * SimStep.SECONDS, "the halt's clock starts over");
        clock = sortie.levelSeconds();
        run(sortie, 2, SimEvents.Type.GATE_OPENED);
        assertEquals(clock, sortie.levelSeconds(), "gate 1 holds the clock in its turn");
        assertEquals(1, HeldArenaTest.killHolders(sortie));
        assertEquals(1, sortie.events().value(gateEvent(sortie)), "gate 1 opened");
        run(sortie, 1, SimEvents.Type.GATE_OPENED);
        assertTrue(sortie.levelSeconds() > clock + 0.9, "the clock resumes");
        assertTrue(sortie.groundSpeed() > 0 && sortie.groundSpeed() < 120, "easing out over the section's 2 s");
    }

    private static int gateEvent(Sortie sortie) {
        for (int i = 0; i < sortie.events().size(); i++) {
            if (sortie.events().type(i) == SimEvents.Type.GATE_OPENED) {
                return i;
            }
        }
        throw new AssertionError("no gate opened");
    }

    @Test
    void theHaltCycleRunsAtEveryGate() {
        Sortie sortie = TestSpecs.sortie(level(Optional.of(Act5BSpecs.CYCLE)));
        untilHalt(sortie);
        run(sortie, 2, SimEvents.Type.GATE_OPENED);
        int first = sortie.force.haltReleased();
        assertTrue(first > 0, "gate 0's halt lets in its cycle");
        HeldArenaTest.killHolders(sortie);
        untilHalt(sortie);
        run(sortie, 2, SimEvents.Type.GATE_OPENED);
        assertTrue(sortie.force.haltReleased() > first, "gate 1's halt lets in the cycle again");
    }

    @Test
    void everyGateNeedsAHolderAndASwapNeedsTheOneArena() {
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(41, 6)
                .sections(SECTIONS)
                .groundUnits(holders().subList(0, 2))
                .objectives(ObjectiveSpec.NONE)
                .build());
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(41, 6)
                .sections(SECTIONS)
                .groundUnits(holders())
                .objectives(ObjectiveSpec.NONE)
                .escortSwap(Optional.of(Act5BSpecs.SWAP))
                .build());
        assertEquals(0, LevelScript.gateOf(SECTIONS, 9));
        assertEquals(1, LevelScript.gateOf(SECTIONS, 19));
        assertEquals(-1, LevelScript.gateOf(SECTIONS, 30));
    }
}
