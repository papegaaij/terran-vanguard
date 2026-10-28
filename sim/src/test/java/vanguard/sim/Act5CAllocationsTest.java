package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part C, step C1: Level 30's mechanics together (hard's column of three Minelayers and their lines, a Gilded
 * Gunship with its Dart streams, a Rail Serpent cut into a wreck, Rook in the minefield, no ground) step without
 * allocating, never fill the mine pool, and replay.
 */
class Act5CAllocationsTest {
    private static final int FIRE = Command.FIRE.bit();

    static LevelScript level() {
        WaveSpec serpent = new WaveSpec(
                5,
                WaveSpec.Formation.SNAKE,
                Act5CSpecs.railSerpent(14),
                1,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                List.of(List.of(new WaveSpec.At(560, 200), new WaveSpec.At(-1200, 200))),
                Optional.empty());
        return LevelScript.builder(30, 5)
                .sections(List.of(new Section(30, 80)))
                .waves(List.of(
                        Act5CSpecs.layers(
                                0.5,
                                Act5CSpecs.MINELAYER,
                                3,
                                WaveSpec.Edge.ALTERNATING,
                                new WaveSpec.MineLines(
                                        List.of(100.0, 180.0, 260.0),
                                        List.of(Act5CSpecs.GAP_LEFT, Act5CSpecs.GAP_RIGHT))),
                        Act5CSpecs.gunship(2, Act5CSpecs.GUNSHIP),
                        serpent,
                        Act5CSpecs.layers(
                                12,
                                Act5CSpecs.MINELAYER,
                                3,
                                WaveSpec.Edge.RIGHT,
                                new WaveSpec.MineLines(List.of(100.0, 180.0, 260.0), List.of(Act5CSpecs.GAP_LEFT)))))
                .ground(GroundSpec.SPACE)
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    static Sortie sortie() {
        Loadout loadout = TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT));
        return new Sortie(5, loadout, level(), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** 26 s firing, the ship weaving; the Serpent's sixth car cut at 9 s. */
    static int fly(Sortie sortie) {
        int most = 0;
        for (int i = 0; i < SimStep.ticks(26); i++) {
            int weave = (i / SimStep.ticks(2)) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit();
            sortie.step(FIRE | weave);
            most = Math.max(most, sortie.mineCount());
            if (i == SimStep.ticks(9)) {
                for (int k = sortie.enemyCount() - 1; k >= 0; k--) {
                    Enemy u = sortie.enemy(k);
                    if (u.chain() != null && u.link() == 6 && !Chain.doomed(u)) {
                        sortie.destroyEnemy(k);
                        break;
                    }
                }
            }
        }
        return most;
    }

    @Test
    void level30sMechanicsStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act5CAllocationsTest::sortie, Act5CAllocationsTest::fly));
    }

    @Test
    void theyReplayAndTheMinePoolNeverFills() {
        Sortie one = sortie();
        Sortie two = sortie();
        int most = fly(one);
        fly(two);
        assertEquals(one.stateHash(), two.stateHash());
        assertTrue(most > 0 && most <= 40, "the line mines stay within their limit, far from the pool's 96: " + most);
    }
}
