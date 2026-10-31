package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1a: the Act 6 enemy side together steps without allocating and replays: a mirrored unit under a
 * beam, a Tesla pod and bolts (bounces, absorbs, heat, the shatter), a regenerating Chimera, a regrowing Graft Turret, a
 * Harrow's ring, a pylon's dome over a turret, spoofed volleys and a laser line, and a hunting wave on its prey.
 */
class Act6AAllocationsTest {
    static LevelScript level() {
        List<GroundUnit> units = new ArrayList<>(List.of(
                new GroundUnit(0, Ship.START_X, Act6ASpecs.MIRRORED, -1),
                new GroundUnit(0.5, Ship.START_X - 20, Act6ASpecs.CHIMERA, -1),
                new GroundUnit(1, Ship.START_X + 60, Act6ASpecs.GRAFT, -1),
                new GroundUnit(1.5, Ship.START_X + 16, Act6ASpecs.HARROW, -1),
                new GroundUnit(2, 120, Act6ASpecs.PYLON, -1),
                new GroundUnit(2 + 140 / 130.0, 150, Act6ASpecs.TURRET, -1),
                new GroundUnit(3, 400, Act5Specs.RAIL_BUNKER.withSpoofable(true), -1)));
        units.sort(Comparator.comparingDouble(GroundUnit::t));
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
                                1.5,
                                WaveSpec.Formation.SINGLE,
                                PreyWarTest.SHOOTER,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)
                        .hunts("prey")
                        .build());
        return Act6ASpecs.level(40, 40, units, waves);
    }

    static Sortie sortie() {
        Sortie sortie = new Sortie(
                3,
                TestSpecs.loadout(
                        PartASpecs.beamMount(5),
                        new Armament.Mount(Armament.Slot.LEFT_WING, Act5Specs.TESLA, Act5Specs.TESLA),
                        new Armament.Mount(Armament.Slot.RIGHT_WING, TestSpecs.PULSE, TestSpecs.PULSE_L2)),
                level(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        sortie.force.spoof(0.5);
        return sortie;
    }

    /** 12 s, firing in bursts and weaving. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(12); i++) {
            int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
            int weave = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
            sortie.step(fire | (i % 240 < 120 ? 0 : weave));
        }
    }

    @Test
    void theActSixEnemySideStepsWithoutAllocating() {
        assertEquals(0, Allocations.least(Act6AAllocationsTest::sortie, Act6AAllocationsTest::fly));
    }

    @Test
    void itReplays() {
        Sortie one = sortie();
        Sortie two = sortie();
        int[] seen = new int[SimEvents.Type.values().length];
        for (int i = 0; i < SimStep.ticks(12); i++) {
            int fire = i % 90 < 50 ? Command.FIRE.bit() : 0;
            int weave = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
            int command = fire | (i % 240 < 120 ? 0 : weave);
            one.step(command);
            two.step(command);
            assertEquals(one.stateHash(), two.stateHash());
            for (int e = 0; e < one.events().size(); e++) {
                seen[one.events().type(e).ordinal()]++;
            }
        }
        assertTrue(seen[SimEvents.Type.MIRROR_ABSORBED.ordinal()] + seen[SimEvents.Type.MIRROR_BOUNCED.ordinal()] > 0);
        assertTrue(seen[SimEvents.Type.SPOOF_HESITATE.ordinal()] > 0);
    }
}
