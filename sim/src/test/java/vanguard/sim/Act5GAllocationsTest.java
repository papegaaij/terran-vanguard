package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 part G, step G1: Level 34's mechanics together step without allocating and replay: the hold's clamps shot down, the
 * Helix core rising and collected by flying through it, cargo containers opened and away under an object escapes
 * secondary.
 */
class Act5GAllocationsTest {
    static LevelScript level() {
        LevelScript hold = StoryPickupTest.hold();
        List<GroundObjectSpec> objects = new ArrayList<>(hold.groundObjects());
        objects.add(ObjectEscapesTest.container(0.5, 240, GroundObjectSpec.CARGO_CONTAINER));
        objects.add(ObjectEscapesTest.container(2, 420, "cargo-container-helix"));
        objects.add(ObjectEscapesTest.container(3, 60, GroundObjectSpec.CARGO_CONTAINER));
        objects.sort(Comparator.comparingDouble(GroundObjectSpec::t));
        return hold.toBuilder()
                .groundObjects(objects)
                .objectives(ObjectiveSpec.NONE.withSecondary(
                        Secondary.objectEscapes(GroundObjectSpec.CARGO_CONTAINER, 1, 100, "CONTAINERS")))
                .build();
    }

    static Sortie sortie() {
        return new Sortie(
                3,
                Act5ESpecs.loadout(Act5ESpecs.bolt(20, false)),
                level(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** 12 s: after the lowest clamp while one stands, then after the core, firing. */
    static void fly(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(12); i++) {
            GroundObject clamp = null;
            for (int k = 0; k < sortie.groundObjectCount(); k++) {
                GroundObject object = sortie.groundObject(k);
                if (object.spec().look().equals("hold-clamp")
                        && (clamp == null || object.renderY(1) < clamp.renderY(1))) {
                    clamp = object;
                }
            }
            Pickup core = StoryPickupTest.storyPickup(sortie);
            int command = clamp != null
                    ? StoryPickupTest.toward(sortie, clamp.renderX(), Double.NaN, true)
                    : core != null ? StoryPickupTest.toward(sortie, core.renderX(), core.renderY(1), true) : 0;
            sortie.step(command);
        }
    }

    @Test
    void level34sMechanicsStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act5GAllocationsTest::sortie, Act5GAllocationsTest::fly));
    }

    @Test
    void theyReplay() {
        Sortie one = sortie();
        Sortie two = sortie();
        fly(one);
        fly(two);
        assertEquals(one.stateHash(), two.stateHash());
        assertTrue(one.storyPickup().orElseThrow().collected());
        assertEquals(3, one.escapesTotal());
        assertTrue(one.escapesDestroyed() + one.escapesLost() >= 2);
    }
}
