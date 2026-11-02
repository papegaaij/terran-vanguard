package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Step R79c (design/campaign Level 41, the gates' Warden pairs; round 79: "one drives over the gate"): a walker wave
 * with {@code park} stops at its paths' last points and stands there, scrolling with the ground (its tracks still, its
 * turret on), instead of walking straight on past them as every other walker does.
 */
class WalkerParkTest {
    /** Down from the top edge, then right to its park point 260 px below the top edge (at its entry). */
    private static final WaveSpec.At[] PATH = {
        new WaveSpec.At(240, -40), new WaveSpec.At(240, 200), new WaveSpec.At(300, 260)
    };

    private static Sortie sortie(boolean park) {
        WaveSpec wave = SiegeSpecs.walker(0, WardenTankTest.TANK, PATH).toBuilder()
                .park(park)
                .build();
        LevelScript level = WardenTankTest.level(60, 10, List.of(wave), List.of(), List.of());
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    @Test
    void aParkedWalkerStandsAtItsPathsEndAndScrollsWithTheGround() {
        Sortie sortie = sortie(true);
        sortie.step(Command.NONE);
        Enemy tank = SiegeSpecs.find(sortie, WardenTankTest.TANK);
        assertNotNull(tank);
        // 40 px/s over ≈ 300 px of path: there well within 12 s.
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(300, tank.x(), 6.5, "at its park point");
        double x = tank.x();
        double y = tank.y();
        double walked = tank.walked();
        double facing = tank.facing();
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(x, tank.x(), 0, "it stands");
        assertEquals(y - 5 * 10, tank.y(), 1e-6, "only the ground's scroll moves it");
        assertEquals(walked, tank.walked(), 0, "its tracks still");
        assertEquals(facing, tank.facing(), 0, "its hull keeps its heading");
    }

    @Test
    void withoutParkItWalksStraightOnPastItsEnd() {
        Sortie sortie = sortie(false);
        sortie.step(Command.NONE);
        Enemy tank = SiegeSpecs.find(sortie, WardenTankTest.TANK);
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
        }
        double walked = tank.walked();
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(walked + 40, tank.walked(), 1e-6, "walking on at 40 px/s");
    }

    @Test
    void onlyAWalkerWaveOnItsPathsParks() {
        assertThrows(IllegalArgumentException.class, () -> WaveSpec.builder(
                        0,
                        WaveSpec.Formation.LINE_ABREAST,
                        TestSpecs.NEEDLER,
                        3,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .park(true)
                .build());
        assertTrue(WalkPath.through(0, 0, 1, 1).parked().parks());
        assertTrue(WalkPath.through(0, 0, 1, 1).parked().mirrored().parks(), "a mirrored path parks too");
    }
}
