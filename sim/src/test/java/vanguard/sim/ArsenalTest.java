package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M7 part A's gear together (an Ion Beam, a Light Drone Bay and a Deflector Pod) over the mixed level:
 * stepping allocates nothing, replays hash alike, and every part of it acts.
 */
class ArsenalTest {
    private static Sortie sortie() {
        return new Sortie(
                3,
                TestSpecs.loadout(
                        PartASpecs.beamMount(2),
                        PartASpecs.droneBay(Armament.Slot.LEFT_WING, 2),
                        PartASpecs.deflectorPod(Armament.Slot.RIGHT_WING, 2)),
                SortieTest.mixedLevel(),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void steppingWithTheBeamTheDronesAndTheDeflectorDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> {
                    var sortie = sortie();
                    for (int i = 0; i < 600; i++) {
                        sortie.step(SortieTest.Pilot.commands(i));
                    }
                    return sortie;
                },
                sortie -> {
                    for (int i = 600; i < 600 + 3600; i++) {
                        sortie.step(SortieTest.Pilot.commands(i));
                    }
                });

        assertEquals(0, allocated, "3600 steps allocated " + allocated + " bytes");
    }

    @Test
    void replaysHashAlikeAndEveryPartActs() {
        var first = sortie();
        var second = sortie();
        int beamed = 0;
        int droneShots = 0;
        int caught = 0;
        int kills = 0;
        for (int i = 0; i < SimStep.ticks(40); i++) {
            int commands = SortieTest.Pilot.commands(i);
            first.step(commands);
            second.step(commands);
            beamed += first.beam(0).contact() == Beam.Contact.BURN ? 1 : 0;
            caught += first.events().count(SimEvents.Type.DEFLECTOR_CAUGHT);
            for (int e = 0; e < first.events().size(); e++) {
                if (first.events().type(e) == SimEvents.Type.SHOT_FIRED
                        && first.events().value(e) == 1) {
                    droneShots++;
                }
            }
        }
        kills = first.kills();
        assertEquals(first.stateHash(), second.stateHash());
        assertTrue(beamed > 0, "the beam burnt targets");
        assertTrue(droneShots > 0, "the drone fired");
        assertTrue(caught > 0, "the deflector caught bullets");
        assertTrue(kills > 0, "kills paid");
    }
}
