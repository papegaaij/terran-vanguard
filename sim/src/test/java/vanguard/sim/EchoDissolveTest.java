package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G1 (design/campaign Level 49, the echoes; m11g-gaps.md §1 #4, §4): an echo forms as it arrives
 * ({@link SimEvents.Type#ECHO_FORM}); at its death chain's end it dissolves ({@link SimEvents.Type#ECHO_DISSOLVE}): the
 * units a boss launched go with it, unpaid and uncounted, and every enemy projectile fades (harmless at once, freed
 * 0.5 s later).
 */
class EchoDissolveTest {
    private static Sortie sortie() {
        return Act7GSpecs.sortie(Act7GSpecs.level(Act7GSpecs.carrierEcho(1)));
    }

    private static int launched(Sortie sortie) {
        int n = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy unit = sortie.enemy(i);
            n += unit.launched() || unit.launchedBy() == Launching.BOSS ? 1 : 0;
        }
        return n;
    }

    @Test
    void itFormsOnceAsItArrives() {
        Sortie sortie = sortie();
        int forms = 0;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(0);
            forms += sortie.events().count(SimEvents.Type.ECHO_FORM);
        }
        assertEquals(1, forms);
    }

    @Test
    void itsLaunchedUnitsDissolveUnpaidAndItsBulletsFade() {
        Sortie sortie = sortie();
        for (int i = 0; i < SimStep.ticks(40) && launched(sortie) < 2; i++) {
            sortie.step(0);
        }
        assertTrue(launched(sortie) >= 2, "its brood flies");
        sortie.objectivePasses.wreck(0, Act7GSpecs.CORE);
        int dissolveAt = -1;
        int unitsGone = 0;
        int before = launched(sortie);
        for (int i = 0; i < SimStep.ticks(3) && dissolveAt < 0; i++) {
            before = launched(sortie);
            int credits = sortie.credits();
            int kills = sortie.kills();
            sortie.step(0);
            if (sortie.events().count(SimEvents.Type.ECHO_DISSOLVE) > 0) {
                dissolveAt = i;
                unitsGone = sortie.events().count(SimEvents.Type.ECHO_UNIT_DISSOLVE);
                assertEquals(credits, sortie.credits(), "unpaid");
                assertEquals(kills, sortie.kills(), "uncounted");
                for (int b = 0; b < sortie.bulletCount(); b++) {
                    assertTrue(sortie.bullet(b).fading(), "every bullet fades");
                }
            }
        }
        assertEquals(SimStep.ticks(2), dissolveAt + 1, 1, "at its 2 s death chain's end");
        assertEquals(before, unitsGone, "its launched units go with it");
        assertEquals(0, launched(sortie));
        for (int i = 0; i < SimStep.ticks(0.5) + 1; i++) {
            sortie.step(0);
        }
        for (int b = 0; b < sortie.bulletCount(); b++) {
            assertTrue(!sortie.bullet(b).fading(), "the faded bullets are freed after 0.5 s");
        }
        assertTrue(sortie.setPiece(0).echo().orElseThrow().dissolved());
    }

    @Test
    void anEchoIsDeterministic() {
        Sortie a = sortie();
        Sortie b = sortie();
        for (int i = 0; i < SimStep.ticks(30); i++) {
            if (i == SimStep.ticks(20)) {
                a.objectivePasses.wreck(0, Act7GSpecs.CORE);
                b.objectivePasses.wreck(0, Act7GSpecs.CORE);
            }
            a.step(Command.FIRE.bit());
            b.step(Command.FIRE.bit());
        }
        assertEquals(a.stateHash(), b.stateHash());
    }
}
