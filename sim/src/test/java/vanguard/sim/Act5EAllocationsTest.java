package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 part E, step E1: Level 32's mechanics together step without allocating and replay: the fork's choice with its
 * branch waves, dormant units and objects, a sweeping Sentinel Tower, a Strider whose leg the ship shoots off (its
 * stagger, fall and crush, its pod's salvos), mining charges bursting and a cache's lock, Rook beside the ship.
 */
class Act5EAllocationsTest {
    private static final int FIRE = Command.FIRE.bit();

    static LevelScript level() {
        LevelScript fork = Act5ESpecs.forkLevel();
        List<WaveSpec> waves = new java.util.ArrayList<>(fork.waves());
        waves.add(SiegeSpecs.walker(1, Act5ESpecs.STRIDER, new WaveSpec.At(90, 40), new WaveSpec.At(90, 4_000)));
        List<GroundUnit> units = new java.util.ArrayList<>(fork.groundUnits());
        units.add(new GroundUnit(0.5, 60, Act5ESpecs.SENTINEL, -1));
        units.add(new GroundUnit(0.6, 120, Act5ESpecs.BUNKER, -1));
        units.sort(java.util.Comparator.comparingDouble(GroundUnit::t));
        List<GroundObjectSpec> objects = new java.util.ArrayList<>(fork.groundObjects());
        objects.add(Act5ESpecs.charge(0.7, 90));
        objects.sort(java.util.Comparator.comparingDouble(GroundObjectSpec::t));
        return fork.toBuilder()
                .waves(waves)
                .groundUnits(units)
                .groundObjects(objects)
                .build();
    }

    static Sortie sortie() {
        Loadout loadout =
                Act5ESpecs.loadout(Act5ESpecs.bolt(20, false)).withWingman(WingmanTest.rook(WingmanSpec.Side.RIGHT));
        return new Sortie(5, loadout, level(), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** 20 s in the left channel, firing. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(Act5ESpecs.steer(sortie, Branch.LEFT, true));
        }
    }

    @Test
    void level32sMechanicsStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act5EAllocationsTest::sortie, Act5EAllocationsTest::fly));
    }

    @Test
    void theyReplay() {
        Sortie one = sortie();
        Sortie two = sortie();
        fly(one);
        fly(two);
        assertEquals(one.stateHash(), two.stateHash());
        assertEquals(Branch.LEFT, one.forkSide(0));
        assertTrue(one.kills() > 0);
    }
}
