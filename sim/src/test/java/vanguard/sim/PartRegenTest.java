package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M10 part H, step H1b (design/enemies, Regeneration; design/enemies/bosses/ascendant, phase 2; m10h-gaps.md §1 #8), on
 * the test Ascendant ({@link Act6HSpecs}): a hurt sac waits 1.5 s, then refills 4 % of its HP a second (the heart 2 %
 * after 2.0 s), never past its whole; steady fire and a held beam deny it; the EMP does not stop it; a dead part stays
 * dead; its healing's start and stop are events; it is hashed and a reset clears it.
 */
class PartRegenTest {
    private static final double EPS = 1e-6;

    private static Act6HSpecs.Rig phaseTwo(SimEvents events) {
        Act6HSpecs.Rig rig = new Act6HSpecs.Rig(Act6HSpecs.QUIET).settled();
        rig.piece.useEvents(events);
        return rig.toEruption().pastEruption();
    }

    @Test
    void aSacWaitsOneAndAHalfSecondsThenRefillsFourPercentASecondNeverPastWhole() {
        Act6HSpecs.Rig rig = phaseTwo(new SimEvents(64));
        SetPiece boss = rig.piece;
        boss.damagePart(Act6HSpecs.SAC_1, 650);
        assertEquals(650, boss.partHp(Act6HSpecs.SAC_1), EPS);
        rig.steps(1.5);
        assertEquals(650, boss.partHp(Act6HSpecs.SAC_1), EPS, "quiet for 1.5 s first");
        rig.steps(1);
        assertEquals(650 + 52, boss.partHp(Act6HSpecs.SAC_1), 52 * 2 * SimStep.SECONDS, "4 % of 1,300 a second");
        rig.steps(20);
        assertEquals(1300, boss.partHp(Act6HSpecs.SAC_1), EPS, "never past whole");
    }

    @Test
    void theHeartRefillsTwoPercentASecondAfterTwoSeconds() {
        Act6HSpecs.Rig rig = phaseTwo(new SimEvents(64));
        SetPiece boss = rig.piece;
        rig.kill(Act6HSpecs.SAC_1);
        rig.kill(Act6HSpecs.SAC_2);
        rig.step();
        assertFalse(boss.partShielded(Act6HSpecs.HEART), "open");
        boss.damagePart(Act6HSpecs.HEART, 1000);
        rig.steps(2);
        assertEquals(1500, boss.partHp(Act6HSpecs.HEART), EPS, "quiet for 2 s first");
        rig.steps(1);
        assertEquals(1550, boss.partHp(Act6HSpecs.HEART), 50 * 2 * SimStep.SECONDS, "2 % of 2,500 a second");
    }

    @Test
    void steadyFireAndAHeldBeamDenyIt() {
        Act6HSpecs.Rig rig = phaseTwo(new SimEvents(64));
        SetPiece boss = rig.piece;
        boss.damagePart(Act6HSpecs.SAC_1, 650);
        boss.damagePart(Act6HSpecs.SAC_2, 650);
        for (int i = 0; i < SimStep.ticks(6); i++) {
            if (i % SimStep.ticks(1.0) == 0) {
                // A shot a second: never 1.5 s quiet.
                boss.damagePart(Act6HSpecs.SAC_1, 1);
            }
            // A beam's every step.
            boss.damagePartAt(Act6HSpecs.SAC_2, 0.1, boss.partX(Act6HSpecs.SAC_2), boss.partY(Act6HSpecs.SAC_2));
            rig.step();
        }
        assertTrue(boss.partHp(Act6HSpecs.SAC_1) <= 650 - 6 + EPS, "steady fire: " + boss.partHp(Act6HSpecs.SAC_1));
        assertTrue(boss.partHp(Act6HSpecs.SAC_2) < 650, "a held beam: " + boss.partHp(Act6HSpecs.SAC_2));
        assertFalse(boss.regens().orElseThrow().healing(Act6HSpecs.SAC_1));
    }

    @Test
    void theEmpDoesNotStopItADeadPartStaysDead() {
        Act6HSpecs.Rig rig = phaseTwo(new SimEvents(64));
        SetPiece boss = rig.piece;
        boss.damagePart(Act6HSpecs.SAC_1, 650);
        rig.kill(Act6HSpecs.SAC_3);
        rig.steps(2);
        double healing = boss.partHp(Act6HSpecs.SAC_1);
        assertTrue(healing > 650);
        boss.stripShields(SimStep.ticks(3));
        rig.steps(1);
        assertTrue(boss.partHp(Act6HSpecs.SAC_1) > healing + 40, "the EMP never stops a boss's regeneration");
        assertTrue(boss.partWrecked(Act6HSpecs.SAC_3), "dead stays dead");
    }

    @Test
    void itsHealingStartsAndStopsAsEvents() {
        SimEvents events = new SimEvents(256);
        Act6HSpecs.Rig rig = phaseTwo(events);
        SetPiece boss = rig.piece;
        boss.damagePart(Act6HSpecs.SAC_1, 100);
        int started = 0;
        int stopped = 0;
        for (int i = 0; i < SimStep.ticks(10); i++) {
            events.clear();
            rig.step();
            started += count(events, SimEvents.Type.PART_HEALING, Act6HSpecs.SAC_1);
            stopped += count(events, SimEvents.Type.PART_HEALING_STOPPED, Act6HSpecs.SAC_1);
            if (i == SimStep.ticks(2)) {
                boss.damagePart(Act6HSpecs.SAC_1, 100);
            }
        }
        assertEquals(2, started, "after each quiet spell");
        assertEquals(2, stopped, "the hit, then whole again");
    }

    @Test
    void itIsHashedAndAResetClearsIt() {
        Act6HSpecs.Rig rig = phaseTwo(new SimEvents(64));
        SetPiece boss = rig.piece;
        boss.damagePart(Act6HSpecs.SAC_1, 650);
        rig.steps(2);
        assertTrue(boss.regens().orElseThrow().healing(Act6HSpecs.SAC_1));
        assertTrue(boss.regens().orElseThrow().healed(Act6HSpecs.SAC_1) > 0);
        boss.reset();
        assertFalse(boss.regens().orElseThrow().healing(Act6HSpecs.SAC_1));
        assertEquals(0, boss.regens().orElseThrow().healed(Act6HSpecs.SAC_1), EPS);
    }

    private static int count(SimEvents events, SimEvents.Type type, int value) {
        int n = 0;
        for (int i = 0; i < events.size(); i++) {
            n += events.type(i) == type && events.value(i) == value ? 1 : 0;
        }
        return n;
    }
}
