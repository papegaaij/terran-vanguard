package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 part B, step B1 (design/enemies, Rock riding; design/enemies/space/asteroid-mite; m9b-gaps.md §1.5): Mites ride
 * a wave's rocks (small breakable chunks that block shots); the ship within 160 px of a rock sets its riders leaping in
 * a seeded order 0.15 s apart, each after a 0.4 s tell, straight at the ship; a broken rock's riders tumble off, then
 * leap; riders carried off the screen on their rock pay nothing. Deterministic.
 */
class RockRidingTest {
    private static final int FIRE = Command.FIRE.bit();

    private static Sortie sortie(int commands, WaveSpec wave) {
        Loadout loadout = commands == 0 ? TestSpecs.loadout() : TestSpecs.LOADOUT;
        return new Sortie(1, loadout, TestSpecs.level(20, List.of(wave)), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** Three riders on a rock coming straight down at the ship. */
    private static WaveSpec down() {
        return Act5BSpecs.onRocks(0.5, 3, new WaveSpec.Rock(Ship.START_X, PlayField.HEIGHT + 20, 0, -120));
    }

    private static int riders(Sortie sortie) {
        int riding = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            riding += sortie.enemy(i).riding() ? 1 : 0;
        }
        return riding;
    }

    @Test
    void ridersClingToTheirRockThenLeapInTurnAfterTheirTell() {
        Sortie sortie = sortie(0, down());
        while (sortie.enemyCount() == 0) {
            sortie.step(0);
        }
        assertEquals(3, riders(sortie));
        assertEquals(1, sortie.debrisCount());
        Debris rock = sortie.debris(0);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy mite = sortie.enemy(i);
            double d = Math.hypot(mite.x() - rock.x(), mite.y() - rock.y());
            assertEquals(20, d, 1e-6, "on the rim");
        }
        List<Integer> tells = new ArrayList<>();
        List<Integer> leaps = new ArrayList<>();
        double reach = Double.NaN;
        for (int step = 0; step < SimStep.ticks(6) && leaps.size() < 3; step++) {
            sortie.step(0);
            if (PartCSpecs.count(sortie, SimEvents.Type.RIDER_TELL) > 0) {
                if (tells.isEmpty() && sortie.debrisCount() > 0) {
                    reach = Math.hypot(
                            sortie.debris(0).x() - sortie.ship().x(),
                            sortie.debris(0).y() - sortie.ship().y());
                }
                tells.add(step);
            }
            if (PartCSpecs.count(sortie, SimEvents.Type.RIDER_LEAP) > 0) {
                leaps.add(step);
            }
        }
        assertEquals(3, tells.size());
        assertEquals(3, leaps.size());
        assertTrue(reach <= 160 && reach > 155, "the leaps start as the ship comes within 160 px: " + reach);
        int gap = SimStep.ticks(0.15);
        int tell = SimStep.ticks(0.4);
        for (int k = 0; k < 3; k++) {
            assertEquals(tells.get(0) + k * gap, tells.get(k), 1);
            assertEquals(tells.get(k) + tell, leaps.get(k), 1);
        }
    }

    @Test
    void aBrokenRockFreesItsRidersWhoTumbleThenLeap() {
        Sortie sortie = sortie(FIRE, down());
        boolean broken = false;
        boolean tumbled = false;
        int leaps = 0;
        for (int step = 0; step < SimStep.ticks(6); step++) {
            sortie.step(FIRE);
            broken |= PartCSpecs.count(sortie, SimEvents.Type.DEBRIS_DESTROYED) > 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                tumbled |= sortie.enemy(i).riderTumbling();
            }
            leaps += PartCSpecs.count(sortie, SimEvents.Type.RIDER_LEAP);
        }
        assertTrue(broken && tumbled && leaps > 0, "broken " + broken + ", tumbled " + tumbled + ", leaps " + leaps);
    }

    @Test
    void ridersCarriedOffOnTheirRockPayNothing() {
        WaveSpec across = Act5BSpecs.onRocks(0.5, 4, new WaveSpec.Rock(-20, PlayField.HEIGHT - 60, 140, -2));
        Sortie sortie = sortie(0, across);
        int boarded = 0;
        for (int step = 0; step < SimStep.ticks(6); step++) {
            sortie.step(0);
            boarded = Math.max(boarded, riders(sortie));
            assertEquals(0, PartCSpecs.count(sortie, SimEvents.Type.RIDER_TELL));
        }
        assertEquals(4, boarded);
        assertEquals(0, sortie.enemyCount(), "gone with the rock");
        assertEquals(0, sortie.debrisCount());
        assertEquals(0, sortie.kills());
        assertEquals(0, sortie.credits());
    }

    @Test
    void theRidesReplay() {
        Sortie one = sortie(FIRE, down());
        Sortie two = sortie(FIRE, down());
        for (int step = 0; step < SimStep.ticks(5); step++) {
            one.step(FIRE);
            two.step(FIRE);
            assertEquals(one.stateHash(), two.stateHash());
        }
    }
}
