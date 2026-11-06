package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1a (the movement vocabulary's {@code mirror}; design/enemies/air/choir-seraph): while it holds its
 * station a mirroring unit's x follows the field's width minus the ship's x, 0.15 s late, at most 260 px/s, never
 * jumping; its row stays 140 px off the ship's; two mirroring units keep their rows a hull's height apart; it fires
 * only its unaimed spiral, only while it mirrors.
 */
class ShadowingTest {
    private static Sortie sortie(WaveSpec... waves) {
        return Act7ASpecs.sortie(Act7ASpecs.level(40, List.of(waves), Optional.empty()));
    }

    private static Enemy holding(Sortie sortie) {
        Enemy unit = null;
        while (unit == null || unit.phase != Enemy.Phase.HOLD) {
            sortie.step(Command.NONE);
            unit = Act7ASpecs.find(sortie, "choir-seraph");
        }
        return unit;
    }

    @Test
    void itFollowsTheMirroredShipLateCappedAndNeverJumps() {
        Sortie sortie = sortie(Act7ASpecs.single(0, Act7ASpecs.SERAPH));
        Enemy seraph = holding(sortie);
        for (int i = 0; i < SimStep.ticks(1.5); i++) {
            sortie.step(Command.NONE);
        }
        assertTrue(seraph.shadow().mirroring());
        assertEquals(PlayField.WIDTH - sortie.ship().x(), seraph.x(), 1e-6, "settled on the mirrored point");
        double cap = 260 * SimStep.SECONDS + 1e-9;
        double x = seraph.x();
        int still = 0;
        boolean moved = false;
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(Command.RIGHT.bit());
            assertTrue(Math.abs(seraph.x() - x) <= cap, "at most 260 px/s");
            moved |= seraph.x() != x;
            still += moved ? 0 : 1;
            x = seraph.x();
        }
        assertEquals(SimStep.ticks(0.15), still, 1, "it follows 0.15 s late");
        assertTrue(seraph.x() < PlayField.WIDTH / 2.0, "the ship went right: it went left");
    }

    @Test
    void itsRowStaysOneHundredFortyPixelsOffTheShips() {
        Sortie sortie = sortie(Act7ASpecs.single(0, Act7ASpecs.seraph(8, 200)));
        Enemy seraph = holding(sortie);
        for (int i = 0; i < SimStep.ticks(1.0); i++) {
            sortie.step(Command.UP.bit());
            if (i > SimStep.ticks(0.5)) {
                assertTrue(
                        seraph.y() - sortie.ship().y() >= 140 - 260 * SimStep.SECONDS,
                        "pushed up ahead of the ship: "
                                + (seraph.y() - sortie.ship().y()));
            }
        }
        assertTrue(sortie.ship().y() > PlayField.HEIGHT - 200 - 140, "the ship climbed past its old row's reach");
    }

    @Test
    void twoMirroringUnitsKeepTheirRowsAHullApart() {
        Sortie sortie = sortie(
                Act7ASpecs.single(0, Act7ASpecs.seraph(8, 120)), Act7ASpecs.single(0.3, Act7ASpecs.seraph(8, 140)));
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
        }
        Enemy a = sortie.enemy(0);
        Enemy b = sortie.enemy(1);
        assertTrue(a.shadow().mirroring() && b.shadow().mirroring());
        assertTrue(Math.abs(a.y() - b.y()) >= 52 - 1e-9, "a hull's height apart: " + Math.abs(a.y() - b.y()));
    }

    @Test
    void itFiresOnlyItsSpiralOnlyWhileItMirrors() {
        Sortie sortie = sortie(Act7ASpecs.single(0, Act7ASpecs.seraph(3, 120)));
        int volleys = 0;
        int outside = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.NONE);
            Enemy seraph = Act7ASpecs.find(sortie, "choir-seraph");
            int fired = sortie.events().count(SimEvents.Type.ENEMY_FIRED);
            volleys += fired;
            if (seraph == null || !seraph.shadow().mirroring()) {
                outside += fired;
            }
        }
        assertEquals(0, outside, "never while not mirroring");
        assertEquals(3 * 3, volleys, 2, "a two-arm volley every 1/3 s over its 3 s");
    }
}
