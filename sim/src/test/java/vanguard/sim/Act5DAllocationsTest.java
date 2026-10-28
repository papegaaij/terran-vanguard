package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part D, step D1: Level 31's mechanics together (the tug's hull escort under three Hornets' salvos, rear and front
 * Dart streams, a Shard line and a Shard ring sweeping the hull, Buzzsaws dashing among drifting rocks, Rook beside the
 * ship) step without allocating, keep the missiles within their per-shooter caps and the pool, and replay.
 */
class Act5DAllocationsTest {
    private static final int FIRE = Command.FIRE.bit();

    static LevelScript level() {
        HazardSpec.DebrisSpec large = new HazardSpec.DebrisSpec(
                7, 120, "large-a", new Hitbox(64, 64), 10, -40, Double.POSITIVE_INFINITY, 15, 0);
        HazardSpec.DebrisSpec small =
                new HazardSpec.DebrisSpec(9, 360, "small-a", new Hitbox(28, 28), -15, -50, 12, 0, 0);
        return Act5DSpecs.level(
                        30,
                        List.of(
                                Act5DSpecs.hornets(6.5, 3),
                                Act5DSpecs.darts(7, 5, WaveSpec.Entry.FRONT, WaveSpec.Edge.LEFT),
                                Act5DSpecs.saws(8, 4, WaveSpec.Edge.ALTERNATING),
                                Act5DSpecs.shardLine(9, 4),
                                Act5DSpecs.shardRing(12),
                                Act5DSpecs.darts(14, 5, WaveSpec.Entry.REAR, WaveSpec.Edge.RIGHT)),
                        Act5DSpecs.ESCORT)
                .toBuilder()
                .hazards(HazardSpec.NONE.withDebris(List.of(large, small)))
                .build();
    }

    static Sortie sortie() {
        Loadout loadout = TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT));
        return new Sortie(5, loadout, level(), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** 24 s firing, the ship weaving; returns the most missiles in flight at once. */
    static int fly(Sortie sortie) {
        int most = 0;
        for (int i = 0; i < SimStep.ticks(24); i++) {
            int weave = (i / SimStep.ticks(2)) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit();
            sortie.step(FIRE | weave);
            most = Math.max(most, sortie.missileCount());
        }
        return most;
    }

    @Test
    void level31sMechanicsStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act5DAllocationsTest::sortie, Act5DAllocationsTest::fly));
    }

    @Test
    void theyReplayAndTheMissilesStayWithinTheirCaps() {
        Sortie one = sortie();
        Sortie two = sortie();
        int most = fly(one);
        fly(two);
        assertEquals(one.stateHash(), two.stateHash());
        assertTrue(most > 0 && most <= 3 * 8, "three Hornets' caps of 8, inside the pool's 32: " + most);
        assertTrue(Optional.ofNullable(one.airLinks()).isPresent());
    }
}
