package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part B, step B1 (design/campaign Level 44, the t=140 chain: "its tail drops the Armour patch"): a chain wave's
 * pickup carried by its tail ({@link WaveSpec.Carried#tail()}) drops where the tail dies, shot or popping in the
 * chained death; the head carries nothing then.
 */
class ChainTailDropTest {
    private static Sortie sortie(WaveSpec.Carried carried) {
        double x = PlayField.WIDTH / 2.0;
        WaveSpec wave = WaveSpec.builder(
                        1, WaveSpec.Formation.SNAKE, FarsideTest.COILWYRM, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(List.of(new WaveSpec.At(x, -60), new WaveSpec.At(x, 300), new WaveSpec.At(x, 700))))
                .carried(List.of(carried))
                .build();
        LevelScript level = Act6ASpecs.level(40, List.of(), List.of(wave));
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        Act6ASpecs.run(sortie, SimStep.ticks(3.5), Command.NONE);
        return sortie;
    }

    private static int member(Sortie sortie, int link) {
        Chain chain = sortie.chain(0);
        int wanted = link < 0 ? chain.size() - 1 : link;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).chain() == chain && sortie.enemy(i).link() == wanted) {
                return i;
            }
        }
        throw new AssertionError("no member " + wanted);
    }

    private static int patches(Sortie sortie) {
        int n = 0;
        for (int i = 0; i < sortie.pickupCount(); i++) {
            n += sortie.pickup(i).type() == PickupType.ARMOUR_PATCH ? 1 : 0;
        }
        return n;
    }

    private static final WaveSpec.Carried TAIL = new WaveSpec.Carried(PickupType.ARMOUR_PATCH, 0, false, true);

    @Test
    void theTailShotDropsIt() {
        Sortie sortie = sortie(TAIL);
        sortie.destroyEnemy(member(sortie, -1));
        assertEquals(1, patches(sortie));
    }

    @Test
    void theHeadCarriesNothingAndTheTailPoppingInTheChainedDeathDropsIt() {
        Sortie sortie = sortie(TAIL);
        sortie.destroyEnemy(member(sortie, 0));
        assertEquals(0, patches(sortie), "not the head's");
        Act6ASpecs.run(sortie, SimStep.ticks(2), Command.NONE);
        assertEquals(1, patches(sortie), "the tail's, as it pops");
    }

    @Test
    void aHeadsPickupStaysTheHeads() {
        Sortie sortie = sortie(new WaveSpec.Carried(PickupType.ARMOUR_PATCH, 0));
        sortie.destroyEnemy(member(sortie, -1));
        assertEquals(0, patches(sortie));
        sortie.destroyEnemy(member(sortie, 0));
        assertEquals(1, patches(sortie));
    }

    @Test
    void onlyAChainHasATailAndAPickupIsNotBoth() {
        assertThrows(
                IllegalArgumentException.class, () -> new WaveSpec.Carried(PickupType.ARMOUR_PATCH, 0, true, true));
        assertThrows(IllegalArgumentException.class, () -> WaveSpec.builder(
                        1, WaveSpec.Formation.SINGLE, Act7ASpecs.SKATER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .carried(List.of(TAIL))
                .build());
    }
}
