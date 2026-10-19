package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part B, step B1: the Dust Kite's {@code swoop} (design/enemies/air/dust-kite). It glides down its
 * path on its {@code sine}; once it is 180 px or less above the ship's row it banks into one shallow
 * curve through the point where the ship was (aimed, no homing) at its swoop speed, the units of a wave
 * bending to alternate sides, and flies on straight until it leaves the play field.
 */
class KiteSwoopTest {
    static final EnemySpec.Swoop SWOOP = new EnemySpec.Swoop(180, 200, 60);

    /** A Dust Kite at medium: 1 HP, 16×14, contact 6 and rammed away, gliding at 130 px/s. */
    static EnemySpec kite(EnemySpec.Swoop swoop) {
        EnemySpec glider = new EnemySpec(
                "dust-kite",
                1,
                new Hitbox(16, 14),
                Layer.AIR,
                6,
                true,
                4,
                130,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new EnemySpec.Sine(50, 1.6)),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        return glider.withSwoop(Optional.of(swoop));
    }

    private static Sortie sortie(EnemySpec kite, int count) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                TestSpecs.level(
                        30,
                        List.of(TestSpecs.wave(
                                0,
                                WaveSpec.Formation.LINE_ABREAST,
                                kite,
                                count,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE))),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void aSwoopIsAnAirUnitsWithoutAChase() {
        EnemySpec ground = PartASpecs.dummy(Layer.GROUND, 1, new Hitbox(16, 14), true);
        assertThrows(IllegalArgumentException.class, () -> ground.withSwoop(Optional.of(SWOOP)));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Swoop(0, 200, 60));
    }

    @Test
    void within180PxAboveTheShipItSwoopsThroughWhereTheShipWasAtItsSpeedAndFliesOff() {
        Sortie sortie = sortie(kite(SWOOP), 1);
        sortie.step(Command.NONE);
        Enemy kite = sortie.enemy(0);
        int swoopStep = -1;
        for (int i = 0; i < SimStep.ticks(10) && swoopStep < 0; i++) {
            assertFalse(kite.swooping());
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.SWOOP) > 0) {
                swoopStep = i;
            }
        }
        assertTrue(swoopStep >= 0, "it swoops");
        assertTrue(kite.swooping());
        double above = kite.y() - Ship.START_Y;
        assertTrue(above <= 180 && above > 170, "as it comes within 180 px above the ship: " + above);
        double nearest = Double.MAX_VALUE;
        double x = kite.x();
        double y = kite.y();
        int steps = 0;
        while (sortie.enemyCount() > 0 && steps < SimStep.ticks(10)) {
            sortie.step(Command.NONE);
            steps++;
            if (sortie.enemyCount() == 0) {
                break;
            }
            double moved = Math.hypot(kite.x() - x, kite.y() - y);
            assertTrue(moved > 150 * SimStep.SECONDS && moved < 260 * SimStep.SECONDS, "about 200 px/s: " + moved);
            x = kite.x();
            y = kite.y();
            nearest = Math.min(nearest, Math.hypot(x - Ship.START_X, y - Ship.START_Y));
        }
        assertTrue(nearest < 200 * SimStep.SECONDS, "through the point where the ship was: " + nearest);
        assertEquals(0, sortie.enemyCount(), "gone off the field");
        assertEquals(0, sortie.kills());
    }

    @Test
    void theUnitsOfAWaveBendToAlternateSides() {
        Sortie sortie = sortie(kite(SWOOP), 3);
        boolean[] seen = new boolean[3];
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            for (int j = 0; j < sortie.enemyCount(); j++) {
                Enemy kite = sortie.enemy(j);
                int place = kite.serial();
                if (place < 3 && !seen[place]) {
                    assertEquals(place % 2 == 0 ? -1 : 1, kite.swoopSide(), "unit " + place);
                    seen[place] = true;
                }
            }
        }
        assertTrue(seen[0] && seen[1] && seen[2]);
    }

    /**
     * M7 part B, step B3: a V-wing of units that neither hover nor dive (Level 15's Dust Kites) flies the V down
     * the screen, the leader first, its ranks 50 px apart across and following one another.
     */
    @Test
    void aVWingOfGlidersFliesTheVDownTheScreen() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT,
                TestSpecs.level(
                        30,
                        List.of(TestSpecs.wave(
                                0,
                                WaveSpec.Formation.V_WING,
                                kite(SWOOP),
                                5,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE))),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        sortie.step(Command.NONE);
        assertEquals(1, sortie.enemyCount(), "the leader first");
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(5, sortie.enemyCount(), "its ranks follow within a second");
        Enemy leader = sortie.enemy(0);
        for (int j = 1; j < sortie.enemyCount(); j++) {
            assertTrue(sortie.enemy(j).y() > leader.y(), "behind the leader");
        }
    }

    @Test
    void aStreamOfKitesDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(kite(SWOOP), 6), sortie -> {
            for (int i = 0; i < SimStep.ticks(12); i++) {
                sortie.step(Command.FIRE.bit());
            }
        });
        assertEquals(0, allocated);
    }
}
