package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 part E, step E1 (design/world/belt, Mining charges; design/campaign Level 32, Blast doors; m9e-gaps.md §1 #7–8): a
 * charge bursts when shot: 80 to every enemy in its 64 px (air and ground, hardened included), the kills the player's,
 * never the ship, and no other charge goes off; a cache's lock frees its crate without a secret.
 */
class ChargeTest {
    private static final int FIRE = Command.FIRE.bit();

    /**
     * A charge at x 240 entering at 0.5 s (10 px/s) with units round it entering with it; the ship fires from x 240
     * until it bursts; returns the sortie.
     */
    private static Sortie burst(List<GroundUnit> units, List<GroundObjectSpec> more) {
        List<GroundObjectSpec> objects = new java.util.ArrayList<>(List.of(Act5ESpecs.charge(0.5, 240)));
        objects.addAll(more);
        LevelScript level = Act5ESpecs.slow(units, List.of(), objects);
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        for (int step = 0; step < SimStep.ticks(6); step++) {
            sortie.step(FIRE);
            if (sortie.events().count(SimEvents.Type.CHARGE_BLAST) > 0) {
                return sortie;
            }
        }
        throw new AssertionError("the charge never burst");
    }

    private static int blastValue(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.CHARGE_BLAST) {
                return events.value(e);
            }
        }
        return -1;
    }

    @Test
    void itDestroysTheEnemiesInItsRadius_airAndGround_hardenedIncluded() {
        EnemySpec flyer = PartASpecs.dummy(Layer.AIR, 32, new Hitbox(28, 24), false);
        Sortie sortie = burst(
                List.of(
                        new GroundUnit(0.5, 290, Act5ESpecs.NEST, -1),
                        new GroundUnit(0.5, 190, Act5ESpecs.BUNKER, -1),
                        new GroundUnit(0.5, 300, flyer, -1),
                        new GroundUnit(0.5, 400, Act5ESpecs.NEST, -1)),
                List.of());
        assertEquals(3, blastValue(sortie), "the turret, the bunker and the flyer");
        assertEquals(3, sortie.kills(), "the player's kills");
        assertNotNull(PartCSpecs.find(sortie, "sam-nest"), "the turret 160 px off stands");
        assertTrue(sortie.credits() > 0);
    }

    @Test
    void itHalvesAnEmitterAndTakesAStridersLeg() {
        Sortie sortie = burst(List.of(new GroundUnit(0.5, 280, Act5ESpecs.COLLAR, -1)), List.of());
        assertEquals(40, PartCSpecs.find(sortie, "sentinel-tower").hp(), 1e-9, "80 of its 120 through the collar");
    }

    @Test
    void itSetsOffNoOtherCharge() {
        Sortie sortie = burst(List.of(), List.of(Act5ESpecs.charge(0.5, 280)));
        assertEquals(1, sortie.events().count(SimEvents.Type.CHARGE_BLAST));
        for (int step = 0; step < SimStep.ticks(1); step++) {
            sortie.step(0);
            assertEquals(0, sortie.events().count(SimEvents.Type.CHARGE_BLAST), "the other one stays");
        }
        assertEquals(1, sortie.groundObjectCount());
    }

    @Test
    void aCachesLockFreesItsCrateWithoutASecret() {
        LevelScript level = Act5ESpecs.slow(List.of(), List.of(), List.of(Act5ESpecs.lock(0.5, 240)));
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        int opened = 0;
        boolean crate = false;
        for (int step = 0; step < SimStep.ticks(6); step++) {
            sortie.step(FIRE);
            opened += sortie.events().count(SimEvents.Type.CACHE_OPENED);
            for (int i = 0; i < sortie.pickupCount(); i++) {
                crate |= sortie.pickup(i).type() == PickupType.HIDDEN_CRATE
                        && sortie.pickup(i).credits() == 50;
            }
        }
        assertEquals(1, opened);
        assertTrue(crate, "its crate of 50 floats out");
        assertEquals(0, sortie.result().secretsFound(), "no secret");
    }

    @Test
    void aChargeIsAPlainDestructible() {
        GroundObjectSpec.Blast blast = new GroundObjectSpec.Blast(64, 80);
        assertThrows(
                IllegalArgumentException.class, () -> Act5ESpecs.lock(1, 100).withBlast(blast));
        assertThrows(IllegalArgumentException.class, () -> new GroundObjectSpec.Blast(0, 80));
    }
}
