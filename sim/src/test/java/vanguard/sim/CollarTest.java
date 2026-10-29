package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part E, step E1 (design/enemies/ground/sentinel-tower, the hardened collar; m9e-gaps.md §1 #5): a shot without
 * {@code anti-ground} on the collar outside the emitter glances off; one on the emitter damages the tower; an {@code
 * anti-ground} shot damages it anywhere; a beam reaches it where its column crosses the emitter; a plain hardened unit
 * (no core) is as before.
 */
class CollarTest {
    private static final int FIRE = Command.FIRE.bit();

    /** The tower at {@code x} (it comes down at 10 px/s), {@code loadout} firing from x 240 for 4 s; its HP left. */
    private static double[] fire(EnemySpec tower, double x, Loadout loadout) {
        LevelScript level = Act5ESpecs.slow(List.of(new GroundUnit(0, x, tower, -1)), List.of(), List.of());
        Sortie sortie = new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        int glances = 0;
        for (int step = 0; step < SimStep.ticks(4); step++) {
            sortie.step(FIRE);
            glances += sortie.events().count(SimEvents.Type.SHOT_GLANCED);
        }
        Enemy enemy = PartCSpecs.find(sortie, tower.slug());
        return new double[] {enemy == null ? 0 : enemy.hp(), glances};
    }

    @Test
    void aBoltOnTheCollarGlancesOff_onTheEmitterItHits() {
        double[] collar = fire(Act5ESpecs.COLLAR, 270, TestSpecs.LOADOUT);
        assertEquals(120, collar[0], "the bolts at x 240 cross the collar beside the emitter (256–284)");
        assertTrue(collar[1] > 0, "they spark off");
        double[] emitter = fire(Act5ESpecs.COLLAR, 250, TestSpecs.LOADOUT);
        assertTrue(emitter[0] < 120, "on the emitter (236–264) they damage it: " + emitter[0]);
    }

    @Test
    void anAntiGroundShotDamagesItAnywhere() {
        double[] collar = fire(Act5ESpecs.COLLAR, 270, Act5ESpecs.loadout(Act5ESpecs.bolt(2, true)));
        assertTrue(collar[0] < 120, "" + collar[0]);
    }

    @Test
    void aBeamReachesItWhereItsColumnCrossesTheEmitter() {
        Loadout beam = TestSpecs.loadout(PartASpecs.beamMount(4));
        assertEquals(120, fire(Act5ESpecs.COLLAR, 270, beam)[0], "a 12 px column at 234–246 misses the emitter");
        assertTrue(fire(Act5ESpecs.COLLAR, 255, beam)[0] < 120, "a column across the emitter (241–269) reaches it");
    }

    @Test
    void aPlainHardenedUnitIsAsBefore() {
        EnemySpec plain = Act5ESpecs.COLLAR.withCore(Optional.empty());
        assertEquals(120, fire(plain, 240, TestSpecs.LOADOUT)[0]);
    }

    @Test
    void aCoreLiesInsideAHardenedUnitsBox() {
        assertThrows(IllegalArgumentException.class, () -> Act5ESpecs.COLLAR.withHardened(false));
        assertThrows(IllegalArgumentException.class, () -> Act5ESpecs.COLLAR.withCore(Optional.of(new Hitbox(80, 20))));
    }
}
