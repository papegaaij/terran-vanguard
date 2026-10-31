package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1b: the Act 6 player and hazard side together steps without allocating and replays: a Plasma Arc
 * and a Reflex Shield under fire (bolts), the IFF spoofer on a turret, lightning strikes (a tracking one), an erupting
 * vent, a hunting wave whose links go live at the level's turn; and a level of two held gates opened in turn.
 */
class Act6BAllocationsTest {
    static LevelScript stormLevel() {
        HazardSpec.Strikes strikes = Act6BSpecs.strikes(
                0,
                new HazardSpec.Strike(1.5, 120, false),
                new HazardSpec.Strike(4, 0, true),
                new HazardSpec.Strike(7, 360, false));
        HazardSpec hazards = HazardSpec.NONE
                .withWeather(Optional.of(Act6BSpecs.storm(strikes)))
                .withEruptions(Optional.of(Act6BSpecs.eruptions(
                        new HazardSpec.Vent(0, 200, 2, 3, 3, 5), new HazardSpec.Vent(3, 300, 2, 3, 3, 5))));
        List<GroundUnit> units = List.of(
                new GroundUnit(0, Ship.START_X, Act6BSpecs.target("a", Layer.AIR, 40), -1),
                new GroundUnit(0.3, Ship.START_X + 50, Act6BSpecs.target("b", Layer.AIR, 40), -1),
                new GroundUnit(1, 150, Act6ASpecs.TURRET, -1),
                new GroundUnit(2, 120, Act6BSpecs.target("c", Layer.AIR, 80), -1));
        List<WaveSpec> waves = List.of(
                WaveSpec.builder(
                                0,
                                WaveSpec.Formation.SINGLE,
                                PreyWarTest.PREY,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)
                        .tag("prey")
                        .build(),
                WaveSpec.builder(
                                1,
                                WaveSpec.Formation.SINGLE,
                                PreyWarTest.SHOOTER,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)
                        .hunts("prey")
                        .build());
        return Act6BSpecs.level(40, hazards, units, waves).toBuilder()
                .turn(Optional.of(5.0))
                .build();
    }

    static Sortie stormSortie() {
        Loadout loadout = Loadout.builder(
                        TestSpecs.SHIP,
                        new Armament(List.of(
                                Act6BSpecs.arcMount(),
                                new Armament.Mount(Armament.Slot.RIGHT_WING, TestSpecs.PULSE, TestSpecs.PULSE_L2))),
                        Act6BSpecs.REFLEX,
                        new Plating(400))
                .build()
                .withSpoofer(0.5);
        return new Sortie(5, loadout, stormLevel(), TestSpecs.RULES, 400);
    }

    /** 12 s, firing in bursts and weaving. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(command(i));
        }
    }

    static int command(int i) {
        int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
        int weave = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
        return fire | (i % 240 < 120 ? 0 : weave);
    }

    static Sortie gateSortie() {
        return TestSpecs.sortie(HeldGatesTest.level(Optional.of(Act5BSpecs.CYCLE)));
    }

    /** Through both gates: each one's holders destroyed 2 s into its halt. */
    static void flyGates(Sortie sortie) {
        int halted = 0;
        for (int i = 0; i < SimStep.ticks(32); i++) {
            sortie.step(command(i));
            halted = sortie.halting() ? halted + 1 : 0;
            if (halted == SimStep.ticks(2)) {
                HeldArenaTest.killHolders(sortie);
            }
        }
    }

    @Test
    void theActSixPlayerAndHazardSideStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act6BAllocationsTest::stormSortie, Act6BAllocationsTest::fly));
        assertEquals(0, Allocations.least(Act6BAllocationsTest::gateSortie, Act6BAllocationsTest::flyGates));
    }

    @Test
    void itReplays() {
        Sortie one = stormSortie();
        Sortie two = stormSortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(12); i++) {
            one.step(command(i));
            two.step(command(i));
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        assertTrue(seen[SimEvents.Type.CHAIN_STRIKE.ordinal()] > 0);
        assertTrue(seen[SimEvents.Type.STRIKE.ordinal()] > 0);
        assertTrue(seen[SimEvents.Type.VENT_ERUPTED.ordinal()] > 0);
        assertTrue(seen[SimEvents.Type.SPOOF_HESITATE.ordinal()] > 0);
        Sortie gates = gateSortie();
        Sortie again = gateSortie();
        int opened = 0;
        int halted = 0;
        for (int i = 0; i < SimStep.ticks(32); i++) {
            gates.step(command(i));
            again.step(command(i));
            halted = gates.halting() ? halted + 1 : 0;
            if (halted == SimStep.ticks(2)) {
                HeldArenaTest.killHolders(gates);
                HeldArenaTest.killHolders(again);
            }
            assertEquals(gates.stateHash(), again.stateHash());
            opened += gates.events().count(SimEvents.Type.GATE_OPENED);
        }
        assertEquals(2, opened);
    }
}
