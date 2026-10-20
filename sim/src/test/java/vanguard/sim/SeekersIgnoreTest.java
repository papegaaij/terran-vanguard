package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.PartCSpecs.find;
import static vanguard.sim.PartCSpecs.plainTarget;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M7 part G (user decision 2026-10-20, "Homing ignores motes"): a unit the seekers ignore ({@code seekers_ignore: true},
 * the Mote Swarm's motes) is never picked as a target by a homing shot or by Rook, while a shot that flies into it
 * still hits.
 */
class SeekersIgnoreTest {
    private static final double HP = 1000;

    /** The target scrolls in at 130 px/s above the ship's column and passes under the ship after about 3.6 s. */
    private static Sortie sortie(Loadout loadout, boolean ignored) {
        LevelScript level = PartCSpecs.level(
                20, 130, List.of(), List.of(new GroundUnit(0, Ship.START_X, plainTarget(HP, ignored), -1)));
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static Loadout armed(WeaponSpec weapon) {
        return TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, weapon, weapon));
    }

    @Test
    void aHomingMissileNeverLocksOntoIt() {
        assertTrue(locks(sortie(armed(PartCSpecs.homing(false)), false)) > 0, "a plain target is locked onto");
        assertEquals(0, locks(sortie(armed(PartCSpecs.homing(false)), true)));
        assertEquals(0, locks(sortie(armed(PartCSpecs.homing(true)), true)), "nor by an anti-ground missile");
    }

    /** The shots locked onto the target over 3 s of fire. */
    private static int locks(Sortie sortie) {
        int locked = 0;
        for (int tick = 0; tick < SimStep.ticks(3); tick++) {
            sortie.step(Command.FIRE.bit());
            for (int i = 0; i < sortie.shotCount(); i++) {
                locked += sortie.shot(i).target() >= 0 ? 1 : 0;
            }
        }
        return locked;
    }

    @Test
    void rookNeverPicksIt() {
        assertTrue(rookTarget(false) >= 0, "a plain target is his");
        assertEquals(-1, rookTarget(true));
    }

    /** Rook's target after 3 s, the target on the field ahead of him; -1 for none. */
    private static int rookTarget(boolean ignored) {
        WingmanSpec rook =
                new WingmanSpec(WingmanSpec.Side.LEFT, 80, WingmanTest.CRAFT, WingmanTest.AI, PartCSpecs.bolt(false));
        Sortie sortie = sortie(TestSpecs.LOADOUT.withWingman(rook), ignored);
        int target = -1;
        for (int tick = 0; tick < SimStep.ticks(3); tick++) {
            sortie.step(Command.NONE);
            target = Math.max(target, sortie.wingman().orElseThrow().target());
        }
        return target;
    }

    @Test
    void aShotThatFliesIntoItStillHits() {
        Sortie sortie = sortie(armed(PartCSpecs.bolt(false)), true);
        for (int tick = 0; tick < SimStep.ticks(3); tick++) {
            sortie.step(Command.FIRE.bit());
        }
        assertTrue(find(sortie, "bunker").hp() < HP, "the bolts hit it");
    }
}
