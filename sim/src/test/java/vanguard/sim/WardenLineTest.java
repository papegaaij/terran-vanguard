package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1a (design/enemies/space/gate-warden): a straight line runs down its unit's frame whatever the
 * ship; a line abreast's first lines come in turn (left to right, or from the middle out) its stagger apart; a hover
 * with an exit up rises out through the top edge after its hold (an escape), its last 2 s the leaving tell (one event,
 * the sortie's count for the HUD's blink).
 */
class WardenLineTest {
    @Test
    void aStraightLineRunsDownItsFrameWhateverTheShip() {
        SimEvents events = new SimEvents(64);
        Pool<Enemy> pool = new Pool<>(2, () -> new Enemy(events), Enemy[]::new);
        Enemy unit = pool.obtain();
        unit.root(Act7ASpecs.shelled(0.5), 0, 300, 0, -1);
        unit.move(60, 96, 200);
        for (int i = 0; i < SimStep.ticks(0.6); i++) {
            unit.move(60, 96, 0);
            LineBeam.stepAll(pool, 60, 96, events);
        }
        assertTrue(unit.lineBeam().telegraphing());
        assertEquals(0, unit.lineBeam().dirX(), 1e-12);
        assertEquals(-1, unit.lineBeam().dirY(), 1e-12, "straight down from its eye, the ship 240 px aside");
    }

    /** The steps and x of the first line telegraph of each of a staggered line abreast of three Wardens. */
    private static List<double[]> firstLines(boolean middleOut) {
        WaveSpec wave = WaveSpec.builder(
                        0,
                        WaveSpec.Formation.LINE_ABREAST,
                        Act7ASpecs.warden(14, true),
                        3,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .lineStagger(Optional.of(new WaveSpec.LineStagger(1.3, middleOut)))
                .build();
        Sortie sortie = Act7ASpecs.sortie(Act7ASpecs.level(30, List.of(wave), Optional.empty()));
        List<double[]> lines = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(12) && lines.size() < 3; i++) {
            sortie.step(Command.NONE);
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.LINE_TELEGRAPH) {
                    lines.add(new double[] {i, sortie.events().x(e)});
                }
            }
        }
        assertEquals(3, lines.size());
        return lines;
    }

    @Test
    void aLineAbreastOpensItsWindowsInTurnLeftToRight() {
        List<double[]> lines = firstLines(false);
        for (int k = 1; k < 3; k++) {
            assertTrue(lines.get(k)[1] > lines.get(k - 1)[1], "left to right");
            assertEquals(SimStep.ticks(1.3), lines.get(k)[0] - lines.get(k - 1)[0], 1, "1.3 s apart");
        }
    }

    @Test
    void orFromTheMiddleOut() {
        List<double[]> lines = firstLines(true);
        assertEquals(PlayField.WIDTH / 2.0, lines.get(0)[1], 40, "the middle one first");
        assertTrue(lines.get(1)[1] < lines.get(0)[1] && lines.get(2)[1] > lines.get(0)[1], "then left, then right");
    }

    @Test
    void itRisesOutThroughTheTopAfterItsHoldItsLastTwoSecondsTheLeavingTell() {
        LevelScript level =
                Act7ASpecs.level(40, List.of(Act7ASpecs.single(0, Act7ASpecs.warden(5, true))), Optional.empty());
        level = TestSpecs.withObjectives(
                level,
                objectives -> objectives.withSecondary(
                        Secondary.builder().credits(50).escapes("gate-warden").build()));
        Sortie sortie = Act7ASpecs.sortie(level);
        Enemy warden = null;
        while (warden == null || warden.phase != Enemy.Phase.HOLD) {
            sortie.step(Command.NONE);
            warden = Act7ASpecs.find(sortie, "gate-warden");
        }
        int held = 1;
        int tells = 0;
        int tellSteps = 0;
        while (warden.phase == Enemy.Phase.HOLD) {
            assertEquals(warden.leavingTell() ? 1 : 0, sortie.leaving(), "the HUD's count follows the tell");
            tellSteps += warden.leavingTell() ? 1 : 0;
            sortie.step(Command.NONE);
            tells += sortie.events().count(SimEvents.Type.LEAVING_TELL);
            held++;
        }
        assertEquals(SimStep.ticks(5), held, 1, "its hold");
        assertEquals(1, tells, "the tell raised once");
        assertEquals(SimStep.ticks(2), tellSteps, 1, "its last 2 s");
        assertEquals(Enemy.Phase.LEAVE, warden.phase);
        double y = warden.y();
        sortie.step(Command.NONE);
        assertEquals(90 * SimStep.SECONDS, warden.y() - y, 1e-9, "straight up at 90 px/s");
        assertEquals(0, warden.vx, 1e-12);
        for (int i = 0; i < SimStep.ticks(5) && Act7ASpecs.find(sortie, "gate-warden") != null; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(null, Act7ASpecs.find(sortie, "gate-warden"), "out through the top edge");
        assertTrue(sortie.secondaryFailed(), "an escape, not a kill");
        assertEquals(0, sortie.kills());
    }
}
