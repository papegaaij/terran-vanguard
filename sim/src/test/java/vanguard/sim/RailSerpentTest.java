package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part C, step C1 (design/enemies/air/rail-serpent): a train's turret cars (every third car, their own aimed guns,
 * staggered, a lamp before each shot, dark once doomed) and the machine split: the cars behind a cut lose power, drift
 * from 40 % of the train's speed to a stop over 1.5 s touching nothing, then blow from the cut backwards 0.1 s apart,
 * each paying its bounty, while the front runs on with the engine.
 */
class RailSerpentTest {
    /** The train's route: in from beyond the right edge 160 px below the top, straight across to the left. */
    private static final double DEPTH = 160;

    private static WaveSpec train(double t, EnemySpec serpent) {
        return WaveSpec.builder(t, WaveSpec.Formation.SNAKE, serpent, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(List.of(new WaveSpec.At(560, DEPTH), new WaveSpec.At(-900, DEPTH))))
                .build();
    }

    private static Sortie sortie(EnemySpec serpent) {
        LevelScript level = LevelScript.builder(30, 5)
                .sections(List.of(new Section(60, 80)))
                .waves(List.of(train(0.5, serpent)))
                .ground(GroundSpec.SPACE)
                .objectives(ObjectiveSpec.NONE)
                .build();
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static int member(Sortie sortie, Chain chain, int link) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).chain() == chain && sortie.enemy(i).link() == link) {
                return i;
            }
        }
        return -1;
    }

    private static void steps(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(Command.NONE);
        }
    }

    @Test
    void everyThirdCarIsATurretCarAndTheLastTheTail() {
        Sortie sortie = sortie(Act5CSpecs.SERPENT);
        steps(sortie, 1);
        Chain chain = sortie.chain(0);
        assertEquals(11, chain.size(), "the engine and its 10 cars");
        for (int i = 1; i < chain.size(); i++) {
            String slug = chain.member(i).spec().slug();
            String expected = i == 10 ? "rail-serpent-tail" : i % 3 == 0 ? "rail-serpent-turret" : "rail-serpent-car";
            assertEquals(expected, slug, "car " + i);
        }
        assertTrue(chain.member(0).spec().gun().isEmpty(), "the engine fires nothing");
    }

    @Test
    void theTurretCarsFireStaggeredEachAfterItsLamp() {
        Sortie sortie = sortie(Act5CSpecs.SERPENT);
        List<Integer> tells = new ArrayList<>();
        List<Integer> shots = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int k = 0; k < events.size(); k++) {
                if (events.type(k) == SimEvents.Type.CAR_TELL) {
                    tells.add(i);
                } else if (events.type(k) == SimEvents.Type.ENEMY_FIRED) {
                    shots.add(i);
                }
            }
        }
        assertTrue(shots.size() >= 3, "the three turret cars fire: " + shots);
        assertTrue(tells.size() >= shots.size(), "each shot after its lamp");
        assertEquals(SimStep.ticks(0.2), shots.get(0) - tells.get(0), "a 0.2 s lamp");
    }

    @Test
    void aCutStopsTheCarsBehindItWhichBlowFromTheCutBackwardsPayingTheirBounty() {
        Sortie sortie = sortie(Act5CSpecs.SERPENT);
        steps(sortie, 2.5);
        Chain front = sortie.chain(0);
        int creditsBefore = sortie.credits();
        // Cut at car 5: cars 6 (turret 5), 7 (3), 8 (3), 9 (turret 5) and the tail (0) lose power.
        sortie.destroyEnemy(member(sortie, front, 5));
        int paidForCut = sortie.credits() - creditsBefore;
        assertEquals(3, paidForCut, "the cut car pays its own");
        assertEquals(1, sortie.events().count(SimEvents.Type.CHAIN_WRECK));
        assertEquals(2, sortie.chainCount());
        Chain wreck = sortie.chain(1);
        assertTrue(wreck.wreck());
        assertFalse(front.wreck());
        assertNull(wreck.member(0));
        for (int i = 1; i < wreck.size(); i++) {
            assertTrue(Chain.doomed(wreck.member(i)), "nothing hits a powerless car, it touches nothing");
        }
        double startX = wreck.member(1).x();
        double frontX = front.member(0).x();
        List<Integer> pops = new ArrayList<>();
        int wreckShots = 0;
        double stoppedX = Double.NaN;
        for (int i = 1; i <= SimStep.ticks(3); i++) {
            Enemy car1 = wreck.member(1);
            sortie.step(Command.NONE);
            pops.addAll(java.util.Collections.nCopies(sortie.events().count(SimEvents.Type.CHAIN_POP), i));
            if (i == SimStep.ticks(1.5) - 1 && car1 != null) {
                stoppedX = car1.x();
            }
            for (int k = 0; k < sortie.events().size(); k++) {
                if (sortie.events().type(k) == SimEvents.Type.ENEMY_FIRED
                        && sortie.events().x(k) > frontX + 1) {
                    wreckShots++;
                }
            }
        }
        assertEquals(5, pops.size(), "its five cars");
        assertEquals(SimStep.ticks(1.5), pops.getFirst(), "after 1.5 s of drift");
        for (int k = 1; k < pops.size(); k++) {
            assertEquals(SimStep.ticks(0.1), pops.get(k) - pops.get(k - 1), "0.1 s apart");
        }
        // 68 px/s slowing evenly to 0 over 1.5 s: 51 px on its heading (leftward, as the train flew).
        assertEquals(-68 * 1.5 / 2, stoppedX - startX, 3);
        assertEquals(5 + 3 + 3 + 5 + 0, sortie.credits() - creditsBefore - paidForCut, "every car pays as it blows");
        assertEquals(0, wreckShots, "its turrets are dark");
        assertTrue(front.alive(), "the front runs on with the engine");
        assertEquals(5, front.size());
    }

    @Test
    void aTrainNeverRegrowsAndHasATurretCar() {
        EnemySpec.ChainSpec chain = Act5CSpecs.SERPENT.chain().orElseThrow();
        assertThrows(
                IllegalArgumentException.class,
                () -> chain.withCars(Optional.of(new EnemySpec.Cars(TestSpecs.SKITTER, 3, 0.6))),
                "a turret car has a gun");
        assertEquals(3, chain.turretsBefore(10));
        assertEquals(1, chain.turretsBefore(6));
    }
}
