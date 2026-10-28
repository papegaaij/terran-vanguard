package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part C, step C1 (design/campaign Level 30, {@code ground: none}): without a ground layer the bomb deliveries fire
 * nothing and read {@code NO GROUND} (their rack still in the loadout, drawing its power); the guns fire as ever. A level
 * with ground keeps its bombs.
 */
class NoGroundTest {
    private static final Loadout RACK = TestSpecs.loadout(
            new Armament.Mount(Armament.Slot.FRONT, TestSpecs.PULSE, TestSpecs.PULSE_L2),
            new Armament.Mount(Armament.Slot.LEFT_WING, Act5CSpecs.BOMB_RACK, Act5CSpecs.BOMB_RACK));

    private static Sortie sortie(GroundSpec ground) {
        LevelScript level = LevelScript.builder(30, 5)
                .sections(List.of(new Section(20, 80)))
                .ground(ground)
                .objectives(ObjectiveSpec.NONE)
                .build();
        return new Sortie(1, RACK, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** The volleys mount {@code m} fires in {@code seconds} with fire held. */
    private static int volleys(Sortie sortie, int m, double seconds) {
        int n = 0;
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(Command.FIRE.bit());
            SimEvents events = sortie.events();
            for (int k = 0; k < events.size(); k++) {
                n += events.type(k) == SimEvents.Type.SHOT_FIRED && events.value(k) == m ? 1 : 0;
            }
        }
        return n;
    }

    @Test
    void theBombRackReadsNoGroundAndFiresNothingWhileTheGunFires() {
        Sortie sortie = sortie(GroundSpec.SPACE);
        assertEquals(IdleReason.NO_GROUND, sortie.mountIdleReason(1));
        assertEquals("NO GROUND", sortie.mountIdleReason(1).word());
        assertTrue(sortie.mountIdle(1));
        assertEquals(IdleReason.NONE, sortie.mountIdleReason(0));
        assertEquals(0, volleys(sortie, 1, 3), "no bombs");
        Sortie again = sortie(GroundSpec.SPACE);
        assertTrue(volleys(again, 0, 3) > 0, "the gun fires");
    }

    @Test
    void overGroundTheRackBombsAsEver() {
        Sortie sortie = sortie(GroundSpec.NONE);
        assertEquals(IdleReason.NONE, sortie.mountIdleReason(1));
        assertTrue(volleys(sortie, 1, 3) > 0);
    }

    @Test
    void aLevelWithoutGroundHasNothingOnIt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GroundSpec(Optional.empty(), true, Optional.empty(), List.of(), List.of(), List.of(), true));
        assertTrue(WeaponSpec.Delivery.DROPPED.needsGround());
        assertTrue(WeaponSpec.Delivery.LOBBED.needsGround());
        assertTrue(!WeaponSpec.Delivery.BOLT.needsGround());
    }
}
