package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1b (design/player/wingmen, Rook in Act 7: teleports): while a teleporting unit is gone its arrival
 * ring is a told point for Rook's weave: a goal on it moves out by the arriving body's half size and his hull's gap.
 */
class RookRingTest {
    @Test
    void aGoalOnAFormingRingMovesOutOfIt() {
        Sortie sortie = Act7ASpecs.sortie(
                Act7ASpecs.level(20, List.of(Act7ASpecs.stream(0.5, Act7ASpecs.SKATER, 4, 0.5)), Optional.empty()));
        Enemy gone = null;
        for (int i = 0; i < SimStep.ticks(10) && gone == null; i++) {
            sortie.step(0);
            for (int j = 0; j < sortie.enemyCount(); j++) {
                if (sortie.enemy(j).teleport().gone()) {
                    gone = sortie.enemy(j);
                }
            }
        }
        assertTrue(gone != null, "a Skater jumps");
        RookHazards hazards = new RookHazards(new Hitbox(30, 30), 20);
        double rx = gone.teleport().ringX();
        double ry = gone.teleport().ringY();
        hazards.at(rx + 5, ry);
        assertTrue(hazards.clearOfRings(rx + 5, ry, sortie.force.enemies()), "a goal on the ring moves");
        double dx = hazards.x() - rx;
        double dy = hazards.y() - ry;
        double half = Math.max(gone.hitbox().width(), gone.hitbox().height()) / 2;
        assertTrue(Math.sqrt(dx * dx + dy * dy) >= half + 15, "out of the arriving body and his own half");
    }

    @Test
    void aGoalFarFromEveryRingStays() {
        Sortie sortie = Act7ASpecs.sortie(Act7ASpecs.level(20, List.of(), Optional.empty()));
        RookHazards hazards = new RookHazards(new Hitbox(30, 30), 20);
        assertFalse(hazards.clearOfRings(200, 200, sortie.force.enemies()));
    }
}
