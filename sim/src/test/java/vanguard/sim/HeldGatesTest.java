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

    /** Steps until the current gate halts and through the halt's first held step; that step's events of {@code type}. */
    private static int haltEvents(Sortie sortie, SimEvents.Type type) {
        untilHalt(sortie);
        sortie.step(0);
        return sortie.events().count(type);
    }

    /** The units of {@code slug} on the field. */
    private static int units(Sortie sortie, String slug) {
        int n = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            n += sortie.enemy(i).spec().slug().equals(slug) ? 1 : 0;
        }
        return n;
    }

    @Test
    void eachGateRunsItsOwnWavesAndPeriodCutAtItsLastHolder() {
        // M10 part G, step G1a (Level 41): gate 0 two Skitters every 2 s, gate 1 one Needler every 3 s.
        LevelScript level = level(Optional.empty()).toBuilder()
                .gateCycles(List.of(Optional.of(Act6GSpecs.SKITTERS), Optional.of(Act6GSpecs.NEEDLERS)))
                .build();
        Sortie sortie = TestSpecs.sortie(level);
        untilHalt(sortie);
        run(sortie, 5, SimEvents.Type.GATE_OPENED);
        assertEquals(6, sortie.force.haltReleased(), "H+0.5, 2.5 and 4.5: three pairs of Skitters");
        assertEquals(0, units(sortie, "needler"), "gate 0 lets in its own cycle only");
        HeldArenaTest.killHolders(sortie);
        run(sortie, 1, SimEvents.Type.GATE_OPENED);
        assertEquals(6, sortie.force.haltReleased(), "cut at the gate's last holder");
        untilHalt(sortie);
        assertEquals(6, sortie.force.haltReleased(), "nothing between the halts");
        run(sortie, 5, SimEvents.Type.GATE_OPENED);
        assertEquals(8, sortie.force.haltReleased(), "H+1 and H+4: two Needlers, one a wave");
        HeldArenaTest.killHolders(sortie);
        run(sortie, 4, SimEvents.Type.GATE_OPENED);
        assertEquals(8, sortie.force.haltReleased(), "gate 1's cycle is cut too");
    }

    @Test
    void aGateWithoutACycleLetsInNone() {
        LevelScript level = level(Optional.empty()).toBuilder()
                .gateCycles(List.of(Optional.empty(), Optional.of(Act6GSpecs.NEEDLERS)))
                .build();
        Sortie sortie = TestSpecs.sortie(level);
        untilHalt(sortie);
        run(sortie, 3, SimEvents.Type.GATE_OPENED);
        assertEquals(0, sortie.force.haltReleased());
        HeldArenaTest.killHolders(sortie);
        untilHalt(sortie);
        run(sortie, 2, SimEvents.Type.GATE_OPENED);
        assertEquals(1, sortie.force.haltReleased());
    }

    @Test
    void aSetPieceHoldsItsGateWhileItLivesAndItsFallOpensIt() {
        Sortie sortie = TestSpecs.sortie(Act6GSpecs.pieceLevel(List.of()));
        assertEquals(2, sortie.gates());
        assertEquals(-1, sortie.script().gatePiece(0));
        assertEquals(0, sortie.script().gatePiece(1));
        assertEquals(1, haltEvents(sortie, SimEvents.Type.GATE_HALTED), "gate 0's halt starts");
        assertEquals(1, Act6GSpecs.radioed(sortie, 0), "its halt line (gate-1)");
        HeldArenaTest.killHolders(sortie);
        sortie.step(0);
        assertEquals(1, sortie.gate());
        assertEquals(1, sortie.holders(), "the piece is the gate's one holder");
        assertEquals(1, haltEvents(sortie, SimEvents.Type.GATE_HALTED), "the piece's gate halts");
        assertEquals(1, Act6GSpecs.radioed(sortie, 1), "its halt line (the piece's slug)");
        assertEquals(22, sortie.levelSeconds(), 2 * SimStep.SECONDS);
        double clock = sortie.levelSeconds();
        run(sortie, 3, SimEvents.Type.GATE_OPENED);
        assertEquals(clock, sortie.levelSeconds(), "the clock stands while the piece lives");
        assertTrue(sortie.setPiece(0).present());
        sortie.objectivePasses.wreck(0, 0);
        assertEquals(1, sortie.events().value(gateEvent(sortie)), "its fall opens gate 1");
        assertEquals(1, PartCSpecs.count(sortie, SimEvents.Type.HOLDERS_DOWN));
        assertEquals(1, Act6GSpecs.radioed(sortie, 2) + countNext(sortie, 2), "the halo-destroyed line");
        run(sortie, 1, SimEvents.Type.GATE_OPENED);
        assertTrue(sortie.levelSeconds() > clock + 0.9, "the clock resumes");
        assertTrue(sortie.groundSpeed() > 0 && sortie.groundSpeed() < 120, "easing out over the section's 2 s");
    }

    /** The cue {@code cue}'s starts in the next step. */
    private static int countNext(Sortie sortie, int cue) {
        sortie.step(0);
        return Act6GSpecs.radioed(sortie, cue);
    }

    @Test
    void thePiecesCheckpointIsRecordedAtItsHaltAndRestoresInIt() {
        Sortie sortie = TestSpecs.sortie(
                Act6GSpecs.pieceLevel(List.of(Optional.of(Act6GSpecs.SKITTERS), Optional.of(Act6GSpecs.NEEDLERS))));
        untilHalt(sortie);
        run(sortie, 1, SimEvents.Type.GATE_OPENED);
        HeldArenaTest.killHolders(sortie);
        sortie.step(0);
        assertFalse(sortie.bossCheckpoint(), "not before the piece's halt");
        untilHalt(sortie);
        assertFalse(sortie.bossCheckpoint(), "not before the halt's first step");
        sortie.step(0);
        assertTrue(sortie.bossCheckpoint(), "recorded at its gate's halt");
        assertEquals(1, sortie.events().count(SimEvents.Type.GATE_HALTED));
        double clock = sortie.levelSeconds();
        run(sortie, 2, SimEvents.Type.GATE_OPENED);
        assertEquals(2 + 1, sortie.force.haltReleased(), "gate 0's Skitter pair in its 1 s, gate 1's Needler at H+1");
        sortie.retryFromBoss();
        sortie.step(0);
        assertEquals(1, sortie.events().count(SimEvents.Type.BOSS_RETRY));
        assertEquals(1, sortie.gate(), "the piece's gate is current again");
        assertEquals(1, sortie.holdersStanding());
        assertEquals(clock, sortie.levelSeconds(), "in its halt");
        assertTrue(sortie.halting());
        assertEquals(1, sortie.events().count(SimEvents.Type.GATE_HALTED), "the halt starts again");
        assertTrue(sortie.haltSeconds() < 2 * SimStep.SECONDS, "its halt's clock from its start");
        assertEquals(2, sortie.force.haltReleased(), "the halt's units let in by the record");
        run(sortie, 2, SimEvents.Type.GATE_OPENED);
        assertEquals(2 + 1, sortie.force.haltReleased(), "the halt's cycle again from its start (H+1)");
        assertEquals(clock, sortie.levelSeconds(), "the clock stands again while the piece lives");
        sortie.objectivePasses.wreck(0, 0);
        assertEquals(1, sortie.events().count(SimEvents.Type.GATE_OPENED));
        run(sortie, 1, SimEvents.Type.GATE_OPENED);
        assertTrue(sortie.levelSeconds() > clock + 0.9, "the clock resumes");
    }

    @Test
    void theGatesOwnCyclesAndAPieceAreChecked() {
        LevelScript.Builder gates = level(Optional.empty()).toBuilder();
        assertThrows(
                IllegalArgumentException.class,
                () -> gates.gateCycles(List.of(Optional.of(Act6GSpecs.SKITTERS)))
                        .build(),
                "one cycle a gate");
        assertThrows(
                IllegalArgumentException.class,
                () -> level(Optional.of(Act5BSpecs.CYCLE)).toBuilder()
                        .gateCycles(List.of(Optional.empty(), Optional.empty()))
                        .build(),
                "not beside the level's one cycle");
        assertThrows(
                IllegalArgumentException.class,
                () -> level(Optional.empty()).toBuilder()
                        .setPieces(List.of(Act6GSpecs.piece(20.5)))
                        .build(),
                "a piece holds a gate of its own, not one of ground holders");
        assertThrows(
                IllegalArgumentException.class,
                () -> Act6GSpecs.pieceLevel(List.of()).toBuilder()
                        .setPieces(List.of(Act6GSpecs.piece(30)))
                        .build(),
                "a piece arrives in its gate");
        assertThrows(
                IllegalArgumentException.class,
                () -> LevelScript.builder(1, 1)
                        .sections(SECTIONS)
                        .gateCycles(List.of(Optional.empty(), Optional.empty()))
                        .build(),
                "gate cycles need held gates");
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
