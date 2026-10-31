package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part D, step D1 (design/campaign Level 38; gap check m10d §1 #7, #8): a lightning strike crossing a debris chunk
 * that holds a secret opens it (Aurelia's cargo pod: its crate, the secret found, its line) and passes every other
 * chunk; a chunk with a drop (the CDF supply canister) lets its pickup fall when the player's shot breaks its latch, and
 * carries the friendly marker's flag.
 */
class StormDebrisTest {
    /** A storm level with a strike at t=1 (told) over x=100 and {@code chunk} entering at the top at t=0. */
    private static Sortie struck(HazardSpec.DebrisSpec chunk) {
        HazardSpec.Strikes strikes = Act6BSpecs.strikes(0, new HazardSpec.Strike(1, 100, false));
        HazardSpec hazards = HazardSpec.NONE
                .withWeather(Optional.of(Act6BSpecs.storm(strikes)))
                .withDebris(List.of(chunk));
        return Act6DSpecs.sortie(Act6DSpecs.level(10, hazards, List.of(), new Secondary(0.8, 50)));
    }

    private static int pickups(Sortie sortie, PickupType type) {
        int n = 0;
        for (int i = 0; i < sortie.pickupCount(); i++) {
            n += sortie.pickup(i).type() == type ? 1 : 0;
        }
        return n;
    }

    @Test
    void aStrikeCrossingThePodOpensItsSecret() {
        Sortie sortie = struck(Act6DSpecs.pod(0, 100));
        int radio = 0;
        int found = 0;
        for (int i = 0; i < SimStep.ticks(2.2); i++) {
            sortie.step(Command.NONE);
            radio += sortie.events().count(SimEvents.Type.RADIO);
            found += sortie.events().count(SimEvents.Type.SECRET_FOUND);
            if (sortie.events().count(SimEvents.Type.STRIKE) > 0) {
                assertEquals(1, sortie.events().count(SimEvents.Type.DEBRIS_DESTROYED), "opened as it strikes");
            }
        }
        assertEquals(0, sortie.debris.size());
        assertEquals(1, found);
        assertEquals(1, sortie.objectives.secretsFound());
        assertEquals(1, radio, "its secret's line");
        assertEquals(1, pickups(sortie, PickupType.HIDDEN_CRATE));
    }

    @Test
    void aStrikePassesEveryOtherChunkAndOneOutsideItsColumn() {
        for (HazardSpec.DebrisSpec chunk : List.of(
                new HazardSpec.DebrisSpec(0, 100, "small-a", new Hitbox(28, 28), 0, -60, 12, 0, 0),
                Act6DSpecs.canister(0, 100),
                Act6DSpecs.pod(0, 160))) {
            Sortie sortie = struck(chunk);
            assertEquals(1, Act6BSpecs.run(sortie, SimStep.ticks(2.2), 0, SimEvents.Type.STRIKE));
            assertEquals(1, sortie.debris.size(), chunk.chunk() + " at " + chunk.x());
            assertEquals(0, sortie.pickupCount());
        }
    }

    @Test
    void shootingTheCanistersLatchReleasesItsArmourPatch() {
        Sortie sortie = Act6DSpecs.sortie(Act6DSpecs.level(
                10,
                HazardSpec.NONE.withDebris(List.of(Act6DSpecs.canister(0.5, Ship.START_X))),
                List.of(),
                new Secondary(0.8, 50)));
        int released = -1;
        for (int i = 0; i < SimStep.ticks(8) && released < 0; i++) {
            sortie.step(Command.FIRE.bit());
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.DEBRIS_RELEASED) {
                    released = sortie.events().value(e);
                }
            }
        }
        assertEquals(PickupType.ARMOUR_PATCH.ordinal(), released, "its latch shot, it lets go");
        assertEquals(1, pickups(sortie, PickupType.ARMOUR_PATCH));
        assertEquals(0, sortie.events().count(SimEvents.Type.SECRET_FOUND));
        assertEquals(0, sortie.debris.size());
    }

    @Test
    void aDropIsASmallChunksAndTheMarkerAFlag() {
        HazardSpec.DebrisSpec canister = Act6DSpecs.canister(0, 100);
        assertTrue(canister.marked());
        assertEquals(Optional.of(PickupType.ARMOUR_PATCH), canister.drop());
        assertFalse(Act6DSpecs.pod(0, 100).marked());
        assertEquals(Optional.empty(), Act6DSpecs.pod(0, 100).drop());
        HazardSpec.DebrisSpec large = new HazardSpec.DebrisSpec(
                0, 100, "large-a", new Hitbox(60, 60), 0, -40, Double.POSITIVE_INFINITY, 10, 0);
        assertThrows(IllegalArgumentException.class, () -> large.withDrop(PickupType.ARMOUR_PATCH));
        assertThrows(
                IllegalArgumentException.class, () -> Act6DSpecs.pod(0, 100).withDrop(PickupType.ARMOUR_PATCH));
        assertThrows(IllegalArgumentException.class, () -> canister.withSecret("pod", 100));
    }
}
