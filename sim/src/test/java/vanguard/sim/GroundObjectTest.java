package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part E, step E1 (design/campaign Level 39, Secrets: the survey hut's 30 HP lock; m10e-gaps.md §1 #10): a trigger
 * with an {@code hp} is spent by damage, not by counted hits: every hit takes its damage (a beam each step's), the
 * secret is found once at none; a trigger without one counts its hits as before.
 */
class GroundObjectTest {
    /** A trigger revealing the level's one secret: {@code hp} for one spent by damage, else {@code hits} counted. */
    private static GroundObjectSpec lock(double hp, int hits) {
        return new GroundObjectSpec(
                0.5, Ship.START_X, new Hitbox(24, 24), hp, 0, Optional.empty(), hits, 100, "survey hut", false, 0, 1);
    }

    @Test
    void aLockWithHpIsSpentByItsDamageOnce() {
        GroundObjectSpec spec = lock(30, 1);
        assertTrue(spec.trigger() && spec.damageSpent());
        assertFalse(lock(0, 3).damageSpent(), "a counted trigger");
        assertFalse(
                new GroundObjectSpec(0, 0, new Hitbox(8, 8), 30, 5, Optional.empty(), 0, 0, "", false).damageSpent(),
                "a destructible");
        GroundObject hut = new GroundObject();
        hut.place(spec, 0);
        assertFalse(hut.countHit(12));
        assertEquals(0.6, hut.hpShare(), 1e-9);
        assertFalse(hut.countHit(12));
        assertTrue(hut.hittable() && !hut.spent());
        assertTrue(hut.countHit(12), "30 damage spends it");
        assertTrue(hut.spent());
        assertFalse(hut.hittable());
        assertEquals(0, hut.hpShare(), 1e-9);
        assertFalse(hut.countHit(12), "once");
        GroundObject beacon = new GroundObject();
        beacon.place(lock(0, 3), 1);
        assertFalse(beacon.countHit(50), "a counted trigger ignores the damage");
        assertFalse(beacon.countHit(50));
        assertTrue(beacon.countHit(1));
    }

    /** The steps with a hit on the level's one trigger, until its secret is found (with that step) over 10 s of fire. */
    private static int hitsUntilFound(Sortie sortie) {
        int hits = 0;
        for (int step = 0; step < SimStep.ticks(10); step++) {
            sortie.step(Command.FIRE.bit());
            if (sortie.groundObjectCount() > 0 && sortie.groundObject(0).ticksSinceHit() == 0) {
                hits++;
            }
            if (sortie.events().count(SimEvents.Type.SECRET_FOUND) > 0) {
                return hits;
            }
        }
        throw new AssertionError("no secret found");
    }

    private static final Armament.Mount PULSE =
            new Armament.Mount(Armament.Slot.FRONT, TestSpecs.PULSE, TestSpecs.PULSE_L2);

    @Test
    void thePulseCannonSpendsASevenHpLockInFourShots_aThreeHitBeaconInThree() {
        // The Pulse Cannon's shot does 2.
        assertEquals(4, hitsUntilFound(PartASpecs.sortie(PartASpecs.units(List.of(lock(7, 1))), PULSE)));
        assertEquals(3, hitsUntilFound(PartASpecs.sortie(PartASpecs.units(List.of(lock(0, 3))), PULSE)));
    }

    @Test
    void aBeamBurnsALockDownByItsDamage() {
        // The ion beam at level 1: 45 damage a second, each step's on the lock, 36 HP in 0.8 s.
        int steps = hitsUntilFound(PartASpecs.sortie(PartASpecs.units(List.of(lock(36, 1))), PartASpecs.beamMount(1)));
        assertEquals(SimStep.ticks(0.8), steps, 2);
    }
}
