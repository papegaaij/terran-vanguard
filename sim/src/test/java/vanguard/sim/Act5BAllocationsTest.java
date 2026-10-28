package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part B, step B1: Level 29's mechanics together (a held arena with its halt cycle and the rescue's escort swap, a
 * led wing scattering, Mites on a rock, a pair of neutral craft under a spared objective, a dormant nest) step without
 * allocating, and replay.
 */
class Act5BAllocationsTest {
    private static final int FIRE = Command.FIRE.bit();

    static LevelScript level() {
        List<GroundUnit> ground = new ArrayList<>();
        ground.add(new GroundUnit(0.5, 100, Act5BSpecs.NEST, -1).dormantUntil(4));
        ground.addAll(Act5BSpecs.clamps());
        return LevelScript.builder(29, 5)
                .sections(Act5BSpecs.SECTIONS)
                .waves(List.of(
                        Act5BSpecs.vWing(1, Act5BSpecs.wingTalon(Act5BSpecs.scatter(true))),
                        Act5BSpecs.onRocks(2, 3, new WaveSpec.Rock(Ship.START_X, PlayField.HEIGHT + 20, 0, -120)),
                        TestSpecs.wave(
                                3,
                                WaveSpec.Formation.LINE_ABREAST,
                                Act5BSpecs.STORMHAWK,
                                2,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE)))
                .groundUnits(ground)
                .objectives(ObjectiveSpec.NONE.withSecondary(Secondary.spared("wavering-stormhawk", 100, "HOLD FIRE")))
                .haltCycle(Optional.of(Act5BSpecs.CYCLE))
                .escortSwap(Optional.of(Act5BSpecs.SWAP))
                .build();
    }

    static Sortie sortie() {
        Loadout loadout =
                TestSpecs.LOADOUT.withBulwark(BulwarkTest.BULWARK).withRescue(WingmanTest.rook(WingmanSpec.Side.LEFT));
        return new Sortie(5, loadout, level(), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** 34 s firing (the 30 s level and its halt), the wing's leader shot down at 3 s, the clamps 2 s into the halt. */
    static void fly(Sortie sortie) {
        int halted = -1;
        for (int i = 0; i < SimStep.ticks(Act5BSpecs.END + 4); i++) {
            sortie.step(FIRE);
            if (i == SimStep.ticks(3)) {
                for (int k = sortie.enemyCount() - 1; k >= 0; k--) {
                    if (sortie.enemy(k).leader()) {
                        sortie.destroyEnemy(k);
                        break;
                    }
                }
            }
            if (halted < 0 && sortie.halting()) {
                halted = i;
            }
            if (halted >= 0 && i == halted + SimStep.ticks(2)) {
                HeldArenaTest.killHolders(sortie);
            }
        }
    }

    @Test
    void level29sMechanicsStepWithoutAllocating() {
        assertEquals(0, Allocations.least(Act5BAllocationsTest::sortie, Act5BAllocationsTest::fly));
    }

    @Test
    void theyReplay() {
        Sortie one = sortie();
        Sortie two = sortie();
        fly(one);
        fly(two);
        assertEquals(one.stateHash(), two.stateHash());
        assertTrue(one.complete(), "the level ends after the rescue");
        assertTrue(one.escortSwap().joined());
    }
}
