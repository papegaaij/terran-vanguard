package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part B, step B1 (design/campaign Level 36, the pathfinder pod; gap check m10b §1 #6, §4): a small debris chunk
 * holding a secret (20 HP, blocking shots like a small rock); broken by the player's shots it frees its hidden crate
 * where it was, the secret counts as found and its {@code secret} radio cue plays.
 */
class PathfinderPodTest {
    /** Flies firing straight up until the pod breaks (at most 10 s); returns the step it broke, -1 for never. */
    private static int breakIt(Sortie sortie, int[] radio) {
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(Command.FIRE.bit());
            radio[0] += sortie.events().count(SimEvents.Type.RADIO);
            if (sortie.events().count(SimEvents.Type.DEBRIS_DESTROYED) > 0) {
                return i;
            }
        }
        return -1;
    }

    private static Sortie sortie(HazardSpec.DebrisSpec chunk) {
        return CloudDiversSpecs.sortie(CloudDiversSpecs.level(
                20,
                HazardSpec.NONE.withDebris(List.of(chunk)),
                List.of(),
                List.of(),
                1,
                List.of(CloudDiversSpecs.cue(CueTrigger.SECRET, "pathfinder-pod"))));
    }

    @Test
    void breakingThePodFreesItsCrateAndTheSecret() {
        Sortie sortie = sortie(CloudDiversSpecs.pod(0.5, Ship.START_X));
        int[] radio = {0};
        assertTrue(breakIt(sortie, radio) >= 0, "the pod breaks under fire");
        assertEquals(1, sortie.events().count(SimEvents.Type.SECRET_FOUND));
        assertEquals(1, radio[0], "its secret's line");
        assertEquals(1, sortie.objectives.secretsFound());
        Pickup crate = null;
        for (int i = 0; i < sortie.pickupCount(); i++) {
            if (sortie.pickup(i).type() == PickupType.HIDDEN_CRATE) {
                crate = sortie.pickup(i);
            }
        }
        assertTrue(crate != null, "its hidden crate");
        assertEquals(100, crate.credits());
    }

    @Test
    void aChunkWithoutASecretFreesNothing() {
        HazardSpec.DebrisSpec plain =
                new HazardSpec.DebrisSpec(0.5, Ship.START_X, "pod", new Hitbox(24, 24), 0, -40, 20, 0, 0);
        Sortie sortie = sortie(plain);
        int[] radio = {0};
        assertTrue(breakIt(sortie, radio) >= 0);
        assertEquals(0, sortie.events().count(SimEvents.Type.SECRET_FOUND));
        assertEquals(0, radio[0]);
        assertEquals(0, sortie.objectives.secretsFound());
        assertEquals(0, sortie.pickupCount());
    }

    @Test
    void onlyASmallChunkHoldsASecret() {
        HazardSpec.DebrisSpec large = new HazardSpec.DebrisSpec(
                0, 100, "large-a", new Hitbox(60, 60), 0, -40, Double.POSITIVE_INFINITY, 10, 0);
        assertThrows(IllegalArgumentException.class, () -> large.withSecret("pod", 100));
        assertThrows(IllegalArgumentException.class, () -> CloudDiversSpecs.pod(0, 100)
                .withSecret("pod", -1));
        assertTrue(CloudDiversSpecs.pod(0, 100).holdsSecret());
    }
}
