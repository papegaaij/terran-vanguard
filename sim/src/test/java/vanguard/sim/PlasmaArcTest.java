package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1b (design/player/weapons/plasma-arc, Behaviour): the first strike lands on the nearest valid
 * target in reach in the 90° cone from the nose (no fire without one); the chain jumps on to the nearest unit not yet
 * struck within the jump radius, × 0.75 a jump, at most its count, each unit once; a hardened unit glances it off and a
 * standing mirror's face earths it (both end the chain), a mirror's flank takes a jump; its kills pay.
 */
class PlasmaArcTest {
    /** The ship's nose and the targets' row 100 px above it. */
    private static final double ROW = Ship.START_Y + 21 + 100;

    /** Units riding the ground side by side at {@code xs} (one row), of {@code specs}. */
    private static Sortie sortie(List<EnemySpec> specs, double... xs) {
        List<GroundUnit> units = new ArrayList<>();
        for (int i = 0; i < xs.length; i++) {
            units.add(new GroundUnit(0, xs[i], specs.get(i), -1));
        }
        Loadout loadout = TestSpecs.loadout(Act6BSpecs.arcMount());
        return new Sortie(
                1,
                loadout,
                Act6BSpecs.level(20, HazardSpec.NONE, units, List.of()),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Steps without fire until the row is 100 px above the nose, then one step with fire held. */
    private static void discharge(Sortie sortie) {
        while (sortie.enemyCount() == 0 || sortie.enemy(0).y() > ROW) {
            sortie.step(0);
        }
        sortie.step(Command.FIRE.bit());
    }

    private static EnemySpec unit(String slug, double hp) {
        return Act6BSpecs.target(slug, Layer.AIR, hp);
    }

    @Test
    void theChainJumpsToTheNearestUnstruckUnitAtThreeQuarters() {
        Sortie sortie = sortie(
                List.of(unit("a", 100), unit("b", 100), unit("c", 100), unit("d", 100)),
                Ship.START_X,
                Ship.START_X + 60,
                Ship.START_X + 120,
                Ship.START_X + 180);
        discharge(sortie);
        assertEquals(3, Act6BSpecs.count(sortie, SimEvents.Type.CHAIN_STRIKE), "the chain's count");
        assertEquals(1, Act6BSpecs.count(sortie, SimEvents.Type.SHOT_FIRED));
        assertEquals(100 - 7.5, Act6BSpecs.find(sortie, "a").hp(), 1e-9, "the first strike");
        assertEquals(100 - 7.5 * 0.75, Act6BSpecs.find(sortie, "b").hp(), 1e-9, "the first jump");
        assertEquals(100 - 7.5 * 0.75 * 0.75, Act6BSpecs.find(sortie, "c").hp(), 1e-9, "the second jump");
        assertEquals(100, Act6BSpecs.find(sortie, "d").hp(), 1e-9, "past the count");
        ChainFire chain = sortie.fire.chain();
        assertEquals(3, chain.strikes(0));
        assertEquals(Ship.START_X + 120, chain.x(0, 2), 1e-9);
        int last = -1;
        for (int e = 0; e < sortie.events().size(); e++) {
            if (sortie.events().type(e) == SimEvents.Type.CHAIN_STRIKE) {
                int value = sortie.events().value(e);
                assertEquals(0, ChainFire.eventMount(value));
                assertEquals(last + 1, ChainFire.eventIndex(value));
                last = ChainFire.eventIndex(value);
            }
        }
    }

    @Test
    void eachUnitIsStruckOnceAndAJumpNeedsAUnitInReach() {
        Sortie sortie = sortie(List.of(unit("a", 100), unit("far", 100)), Ship.START_X, Ship.START_X + 100);
        discharge(sortie);
        assertEquals(1, Act6BSpecs.count(sortie, SimEvents.Type.CHAIN_STRIKE), "100 px is past the 90 px jump");
        Sortie two = sortie(List.of(unit("a", 100), unit("b", 100)), Ship.START_X, Ship.START_X + 60);
        discharge(two);
        assertEquals(2, Act6BSpecs.count(two, SimEvents.Type.CHAIN_STRIKE), "never back to a struck unit");
    }

    @Test
    void withNoTargetInTheConeItDoesNotFire() {
        // 120 px to the side of the nose at 100 px up: 50° off straight up, outside the 45°.
        Sortie sortie = sortie(List.of(unit("aside", 100)), Ship.START_X + 120);
        discharge(sortie);
        assertEquals(0, Act6BSpecs.count(sortie, SimEvents.Type.CHAIN_STRIKE));
        assertEquals(0, Act6BSpecs.count(sortie, SimEvents.Type.SHOT_FIRED), "no arc into empty space");
        assertEquals(100, Act6BSpecs.find(sortie, "aside").hp(), 1e-9);
    }

    @Test
    void aHardenedUnitGlancesItOffAndEndsTheChain() {
        Sortie sortie = sortie(
                List.of(unit("a", 100), unit("hard", 100).withHardened(true), unit("c", 100)),
                Ship.START_X,
                Ship.START_X + 60,
                Ship.START_X + 120);
        discharge(sortie);
        assertEquals(2, Act6BSpecs.count(sortie, SimEvents.Type.CHAIN_STRIKE));
        assertEquals(1, Act6BSpecs.count(sortie, SimEvents.Type.SHOT_GLANCED));
        assertEquals(100, Act6BSpecs.find(sortie, "hard").hp(), 1e-9);
        assertEquals(100, Act6BSpecs.find(sortie, "c").hp(), 1e-9, "the chain ended at the hardened unit");
    }

    @Test
    void aMirrorsFaceEarthsTheArcAndItsFlankTakesAJump() {
        Sortie faced = sortie(List.of(Act6ASpecs.mirrored(100)), Ship.START_X);
        discharge(faced);
        assertEquals(1, Act6BSpecs.count(faced, SimEvents.Type.MIRROR_ABSORBED), "earthed on the face");
        assertEquals(100, faced.enemy(0).hp(), 1e-9);
        // The mirror 60 px right of the first target and below it (entering earlier): outside the nose's cone, so the
        // chain reaches it only by the jump, which comes in from above its side.
        Sortie flank = new Sortie(
                1,
                TestSpecs.loadout(Act6BSpecs.arcMount()),
                Act6BSpecs.level(
                        20,
                        HazardSpec.NONE,
                        List.of(
                                new GroundUnit(0, Ship.START_X + 60, Act6ASpecs.mirrored(100), -1),
                                new GroundUnit(60 / 130.0, Ship.START_X, unit("a", 100), -1)),
                        List.of()),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        while (Act6BSpecs.find(flank, "a") == null
                || Act6BSpecs.find(flank, "a").y() > ROW) {
            flank.step(0);
        }
        flank.step(Command.FIRE.bit());
        assertEquals(0, Act6BSpecs.count(flank, SimEvents.Type.MIRROR_ABSORBED));
        assertEquals(100 - 7.5, Act6BSpecs.find(flank, "a").hp(), 1e-9);
        assertEquals(
                100 - 7.5 * 0.75, Act6BSpecs.find(flank, "mirror-interceptor").hp(), 1e-9, "the flank is open");
        assertEquals(2, Act6BSpecs.count(flank, SimEvents.Type.CHAIN_STRIKE));
    }

    @Test
    void itsKillsPay() {
        Sortie sortie = sortie(List.of(unit("a", 5), unit("b", 5)), Ship.START_X, Ship.START_X + 60);
        discharge(sortie);
        assertNull(Act6BSpecs.find(sortie, "a"));
        assertNull(Act6BSpecs.find(sortie, "b"), "the jump's 5.6 kills the second");
        assertEquals(2, sortie.kills());
        assertTrue(sortie.credits() > 0);
    }
}
