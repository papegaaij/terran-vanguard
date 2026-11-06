package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1b (design/world/vrell-space, Spore clouds; J13; Level 45): the clouds drift with the scroll, slow
 * the ship and Rook to 0.7 eased over 0.3 s while inside, burn away in 1 s under an area hit or a Plasma Arc strike,
 * pay nothing and touch nothing else.
 */
class SporeCloudTest {
    /** Level 45's numbers: 0.7, eased 0.3 s, burnt in 1 s, at most 2, a 25 % veil; drifting at the scroll's speed. */
    static VrellLevel.Spores spores(List<VrellLevel.Cloud> clouds) {
        return new VrellLevel.Spores(0.7, 0.3, 1.0, 2, 0.25, 1.0, clouds);
    }

    /** A cloud of radius 100 over the ship's column entering at 0 s, on a 130 px/s level of 30 s. */
    static LevelScript level() {
        LevelScript level = Act6ASpecs.level(30, List.of(), List.of());
        return level.toBuilder()
                .vrell(VrellLevel.NONE.withSpores(
                        Optional.of(spores(List.of(new VrellLevel.Cloud(0, Ship.START_X, 100))))))
                .build();
    }

    static Sortie sortie(LevelScript level) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Steps until the ship is inside the cloud; the steps taken. */
    private static int untilInside(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(0);
            if (sortie.events().count(SimEvents.Type.SPORE_INSIDE) > 0) {
                return i;
            }
        }
        throw new AssertionError("never inside");
    }

    @Test
    void aCloudDriftsWithTheScrollAndSlowsTheShipAndRookEased() {
        Sortie sortie = sortie(level());
        sortie.step(0);
        SporeClouds clouds = sortie.spores();
        assertEquals(1, clouds.count());
        double y = clouds.y(0);
        sortie.step(0);
        assertEquals(130 * SimStep.SECONDS, y - clouds.y(0), 1e-9, "down with the scroll");
        assertEquals(1.0, sortie.ship().slow().cloudShare(), "exactly 1 outside");
        untilInside(sortie);
        double share = sortie.ship().slow().cloudShare();
        assertTrue(share < 1 && share > 0.95, "easing in: " + share);
        for (int i = 0; i < SimStep.ticks(0.3); i++) {
            sortie.step(0);
        }
        assertEquals(0.7, sortie.ship().slow().cloudShare(), 1e-9, "0.7 after the 0.3 s ease");
        Wingman rook = sortie.wingman().orElseThrow();
        double least = 1;
        int left = 0;
        for (int i = 0; i < SimStep.ticks(1.5); i++) {
            sortie.step(0);
            least = Math.min(least, rook.slow().cloudShare());
            left += sortie.events().count(SimEvents.Type.SPORE_LEFT);
        }
        assertEquals(0.7, least, 1e-9, "Rook the same, in his slot inside the cloud");
        for (int i = 0; i < SimStep.ticks(6) && left == 0; i++) {
            sortie.step(0);
            left += sortie.events().count(SimEvents.Type.SPORE_LEFT);
        }
        assertEquals(1, left, "the cloud drifts past");
        for (int i = 0; i < SimStep.ticks(0.3); i++) {
            sortie.step(0);
        }
        assertEquals(1.0, sortie.ship().slow().cloudShare(), "eased out to exactly 1");
    }

    @Test
    void theSlowCutsTheShipsSpeed() {
        Sortie slowed = sortie(level());
        untilInside(slowed);
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            slowed.step(0);
        }
        double x = slowed.ship().x();
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            slowed.step(Command.LEFT.bit());
        }
        double inside = x - slowed.ship().x();
        Sortie free = sortie(Act6ASpecs.level(30, List.of(), List.of()));
        for (int i = 0; i < SimStep.ticks(4); i++) {
            free.step(0);
        }
        double fx = free.ship().x();
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            free.step(Command.LEFT.bit());
        }
        double outside = fx - free.ship().x();
        assertTrue(inside < 0.75 * outside, "slowed: " + inside + " vs " + outside);
    }

    @Test
    void anAreaHitOrAnArcStrikeBurnsACloudAwayInOneSecond() {
        Sortie sortie = sortie(level());
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(0);
        }
        SporeClouds clouds = sortie.spores();
        double cx = clouds.x(0);
        double cy = clouds.y(0);
        sortie.fire.burnSpores(cx + 140, cy, 30);
        assertFalse(clouds.burning(0), "a blast that does not touch it");
        sortie.fire.burnSpores(cx + 120, cy, 30);
        assertTrue(clouds.burning(0), "a blast whose radius touches it");
        assertEquals(1, sortie.events().count(SimEvents.Type.SPORE_BURN));
        sortie.step(0);
        assertTrue(clouds.radius(0) < 100);
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(0);
        }
        assertEquals(0, clouds.count(), "burnt away in 1 s");

        Sortie arc = sortie(level());
        for (int i = 0; i < SimStep.ticks(1); i++) {
            arc.step(0);
        }
        SporeClouds second = arc.spores();
        double x = second.x(0);
        double y = second.y(0);
        arc.fire.arcSpores(x - 150, y + 110, x + 150, y + 110);
        assertFalse(second.burning(0), "a strike passing below it");
        arc.fire.arcSpores(x - 150, y + 150, x + 10, y - 20);
        assertTrue(second.burning(0), "a strike through it");
    }

    @Test
    void aBlastFromTheGunsReachesTheClouds() {
        // The Mortar's shells blast through GroundFire, which burns the clouds as every player blast does.
        Sortie sortie = new Sortie(
                1,
                TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, PartCSpecs.mortar(), PartCSpecs.mortar())),
                level(),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        int burns = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.FIRE.bit());
            burns += sortie.events().count(SimEvents.Type.SPORE_BURN);
        }
        assertEquals(1, burns, "a shell landing in the cloud sets it burning, once");
    }

    @Test
    void aRestartAndTheStateHash() {
        Sortie a = sortie(level());
        Sortie b = sortie(level());
        for (int i = 0; i < SimStep.ticks(5); i++) {
            a.step(0);
            b.step(0);
        }
        assertEquals(a.stateHash(), b.stateHash());
        b.step(Command.LEFT.bit());
        a.step(0);
        assertTrue(a.stateHash() != b.stateHash());
    }
}
