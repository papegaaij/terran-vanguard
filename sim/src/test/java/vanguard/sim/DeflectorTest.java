package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.PartASpecs.deflectorPod;
import static vanguard.sim.PartASpecs.sortie;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The Deflector Pod (design/player/weapons/deflector-pod; M7 part A): a half-disc field per pod side
 * that catches enemy bullets while it holds charges, the centre line to the fuller pod, charges coming
 * back one at a time, the overdrive's higher cap (default, pending the user's review), and a pair
 * keeping bullets off the shield.
 */
class DeflectorTest {
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final double X = Ship.START_X;
    private static final double Y = Ship.START_Y;

    private static Sortie pair(int level) {
        return sortie(
                TestSpecs.level(30, List.of()),
                deflectorPod(Armament.Slot.LEFT_WING, level),
                deflectorPod(Armament.Slot.RIGHT_WING, level));
    }

    @Test
    void aPodCatchesOnlyOnItsSideWithinItsRadiusWhileItHoldsACharge() {
        var sortie = sortie(TestSpecs.level(30, List.of()), deflectorPod(Armament.Slot.LEFT_WING, 1));
        Deflectors pods = sortie.deflectors();
        SimEvents events = sortie.events();
        assertEquals(3, pods.charges(0));
        assertFalse(pods.catches(X + 10, Y, events), "the right half is not its field");
        assertFalse(pods.catches(X - 23, Y, events), "beyond 22 px");
        assertTrue(pods.catches(X - 15, Y + 15, events));
        assertEquals(2, pods.charges(0));
        assertEquals(1, events.count(SimEvents.Type.DEFLECTOR_CAUGHT));
        assertTrue(pods.catches(X, Y - 20, events), "the centre line counts for a lone pod");
        assertTrue(pods.catches(X - 1, Y, events));
        assertEquals(0, pods.charges(0));
        assertFalse(pods.catches(X - 1, Y, events), "an empty pod catches nothing");
    }

    @Test
    void theCentreLineGoesToThePodWithMoreChargesTheLeftOnATie() {
        var sortie = pair(1);
        Deflectors pods = sortie.deflectors();
        SimEvents events = sortie.events();
        assertTrue(pods.catches(X, Y + 5, events));
        assertEquals(2, pods.charges(LEFT));
        assertEquals(3, pods.charges(RIGHT));
        assertTrue(pods.catches(X, Y + 5, events));
        assertEquals(2, pods.charges(RIGHT));
        assertTrue(pods.catches(X + 5, Y, events));
        assertEquals(1, pods.charges(RIGHT), "off the line each side keeps its own");
    }

    @Test
    void chargesComeBackOneAtATimeAtTheLevelsInterval() {
        var sortie = sortie(TestSpecs.level(30, List.of()), deflectorPod(Armament.Slot.RIGHT_WING, 1));
        sortie.step(Command.NONE);
        Deflectors pods = sortie.deflectors();
        for (int i = 0; i < 3; i++) {
            pods.catches(X + 5, Y, sortie.events());
        }
        assertEquals(0, pods.charges(0));
        // 0.67 s: 40 steps a charge.
        for (int i = 0; i < 39; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(0, pods.charges(0));
        sortie.step(Command.NONE);
        assertEquals(1, pods.charges(0));
        assertEquals(1, sortie.events().count(SimEvents.Type.DEFLECTOR_CHARGED));
        for (int i = 0; i < 80; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(3, pods.charges(0));
        for (int i = 0; i < 100; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(3, pods.charges(0), "never above its cap");
    }

    @Test
    void anOverdriveRaisesTheCapWithTheNewChargesEmptyAndDropsItAfter() {
        var sortie = sortie(TestSpecs.level(30, List.of()), deflectorPod(Armament.Slot.LEFT_WING, 4));
        Deflectors pods = sortie.deflectors();
        sortie.overdrive(1);
        sortie.step(Command.NONE);
        assertEquals(4, pods.charges(0));
        assertEquals(5, pods.spec(0).charges());
        assertEquals(30, pods.spec(0).radius());
        // The L5 interval, 0.4 s (24 steps), for the fifth charge.
        for (int i = 0; i < 23; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(5, pods.charges(0));
        for (int i = 0; i < SimStep.PER_SECOND; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(4, pods.charges(0), "back to its own cap");
        assertEquals(28, pods.spec(0).radius());
    }

    @Test
    void aMortarBlobLandingInTheFieldIsCaughtInsteadOfHittingTheShip() {
        var sortie = sortie(
                PartASpecs.units(PartASpecs.unit(0, 40, CraterNestTest.MORTAR)),
                deflectorPod(Armament.Slot.LEFT_WING, 1));
        int direct = -1;
        int caught = 0;
        for (int i = 0; i < SimStep.ticks(4) && direct < 0; i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.MORTAR_IMPACT) {
                    direct = events.value(e);
                    caught = events.count(SimEvents.Type.DEFLECTOR_CAUGHT);
                }
            }
        }
        assertEquals(0, direct, "no direct hit");
        assertTrue(caught >= 1, "the blob (and ring bullets still in the field) caught");
        assertEquals(TestSpecs.SHIELD.capacity(), sortie.ship().defences().shield(), 1e-9);
    }

    @Test
    void aPairKeepsBulletsOffTheShieldWithoutScoring() {
        LevelScript level = SortieTest.mixedLevel();
        var bare = new Sortie(1, TestSpecs.loadout(), level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        var guarded = new Sortie(
                1,
                TestSpecs.loadout(deflectorPod(Armament.Slot.LEFT_WING, 5), deflectorPod(Armament.Slot.RIGHT_WING, 5)),
                level,
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        int caught = 0;
        int bareHits = 0;
        int guardedHits = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            bare.step(Command.NONE);
            guarded.step(Command.NONE);
            caught += guarded.events().count(SimEvents.Type.DEFLECTOR_CAUGHT);
            bareHits += bare.events().count(SimEvents.Type.SHIELD_HIT)
                    + bare.events().count(SimEvents.Type.ARMOUR_HIT);
            guardedHits += guarded.events().count(SimEvents.Type.SHIELD_HIT)
                    + guarded.events().count(SimEvents.Type.ARMOUR_HIT);
        }
        assertTrue(caught > 0, "the pods caught bullets");
        assertTrue(guardedHits < bareHits, guardedHits + " hits with the pods, " + bareHits + " without");
        assertEquals(bare.score(), guarded.score(), "a caught bullet scores nothing");
    }
}
