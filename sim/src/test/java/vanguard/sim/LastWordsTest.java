package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, Death; m11f-gaps.md §1 #5, §4), on the test Chimera
 * ({@link Act7FSpecs}, its playbooks off so only the test's bullets fly): the capsule's death is K: {@code LAST_WORDS}
 * and {@code BULLETS_FADE} (every enemy bullet fading, harmless at once, gone at K+0.5), the kill counted and the parts
 * paid once; it hangs where it is, untouchable and silent, and bursts at K+4.5 ({@code LAST_BURST} with {@code
 * BOSS_DESTROYED} and its shower, nothing paid again); its fight's time is K's; the arena's hold after it counts from K
 * (8 s); a reset starts it afresh.
 */
class LastWordsTest {
    private static final SetPieceSpec QUIET = Act7FSpecs.chimera(List.of());

    private static Sortie phaseThree() {
        Sortie sortie = Act7FSpecs.sortie(QUIET, TestSpecs.LOADOUT, TestSpecs.RULES);
        SetPiece boss = sortie.setPiece(0);
        for (int i = 0; i < SimStep.ticks(15) && !boss.engaged(); i++) {
            sortie.step(0);
        }
        assertTrue(boss.engaged(), "settled");
        for (int p : Act7FSpecs.PHASE_1) {
            Act7FSpecs.kill(sortie, p);
        }
        for (int i = 0; i < SimStep.ticks(3.05); i++) {
            sortie.step(0);
        }
        for (int p : Act7FSpecs.SACS) {
            Act7FSpecs.kill(sortie, p);
        }
        for (int i = 0; i < SimStep.ticks(2.05); i++) {
            sortie.step(0);
        }
        assertEquals(2, boss.phase(), "phase 3");
        assertFalse(boss.partShielded(Act7FSpecs.CAPSULE), "the capsule open");
        return sortie;
    }

    private static int count(Sortie sortie, SimEvents.Type type) {
        return sortie.events().count(type);
    }

    @Test
    void theCapsulesDeathFadesTheBulletsHangsAndBurstsOnTheLastWord() {
        Sortie sortie = phaseThree();
        SetPiece boss = sortie.setPiece(0);
        Ship ship = sortie.ship();
        sortie.fireEnemyBullet(ship.x(), ship.y(), 0, 0, 5);
        sortie.fireEnemyBullet(100, 400, 0, -10, 5);
        sortie.fireEnemyBullet(300, 400, 0, -10, 5);
        int credits = sortie.credits();
        int kills = sortie.kills();
        Act7FSpecs.kill(sortie, Act7FSpecs.CAPSULE);
        assertEquals(1, count(sortie, SimEvents.Type.LAST_WORDS));
        assertEquals(1, count(sortie, SimEvents.Type.BULLETS_FADE));
        assertEquals(0, count(sortie, SimEvents.Type.BOSS_DESTROYED), "no death yet");
        int paid = sortie.credits() - credits;
        assertEquals(10, paid, "the capsule's bounty, paid at K");
        assertEquals(kills + 1, sortie.kills(), "the kill counted at K");
        LastWords words = boss.lastWords().orElseThrow();
        assertTrue(words.spoken());
        assertEquals(3, sortie.bulletCount());
        for (int i = 0; i < sortie.bulletCount(); i++) {
            assertTrue(sortie.bullet(i).fading(), "every bullet fades");
        }
        assertFalse(boss.destroyed());
        assertFalse(boss.bodyTouches(new Hitbox(30, 30), boss.x(), boss.y()), "no contact from K");
        double x = boss.x();
        double y = boss.y();
        int k = sortie.levelTick;
        double shield = ship.defences().shield();
        double armour = ship.defences().armour();
        int fade = SimStep.ticks(0.5);
        int burst = SimStep.ticks(4.5);
        for (int step = 1; step <= burst; step++) {
            sortie.step(0);
            assertTrue(ship.defences().armour() >= armour && ship.defences().shield() >= shield, "harmless at " + step);
            if (step < fade) {
                assertEquals(3, sortie.bulletCount(), "fading at " + step);
            } else {
                assertEquals(0, sortie.bulletCount(), "gone by K+0.5");
            }
            if (step < burst) {
                assertFalse(boss.destroyed(), "hangs at " + step);
                assertEquals(x, boss.x(), 1e-9, "where it is");
                assertEquals(y, boss.y(), 1e-9);
                assertEquals(0, count(sortie, SimEvents.Type.BOSS_DESTROYED));
            }
            assertEquals(k, sortie.levelTick, "the clock held");
        }
        assertEquals(1, count(sortie, SimEvents.Type.LAST_BURST), "the burst at K+4.5");
        assertEquals(1, count(sortie, SimEvents.Type.BOSS_DESTROYED));
        assertTrue(boss.destroyed());
        assertTrue(words.burstDone());
        assertEquals(credits + paid, sortie.credits(), "paid once");
        assertEquals(kills + 1, sortie.kills(), "counted once");
        for (int i = 0; i < SimStep.ticks(3.4); i++) {
            sortie.step(0);
            assertEquals(k, sortie.levelTick, "held 8 s from K");
        }
        int moved = -1;
        for (int i = 0; i < SimStep.ticks(3) && moved < 0; i++) {
            sortie.step(0);
            if (sortie.levelTick != k) {
                moved = i;
            }
        }
        assertTrue(moved >= 0 && moved <= SimStep.ticks(0.2), "the clock runs again after 8 s from K: " + moved);
    }

    /** The value of the step's first event of {@code type}; -1 for none. */
    private static int value(Sortie sortie, SimEvents.Type type) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                return events.value(i);
            }
        }
        return -1;
    }

    /**
     * Step F1b (the main agent's call, pending round 93): at K every enemy projectile fades, not only the bullets: a
     * homing diamond closing on the ship flies straight on, harmless and no target, and a bile lob lands as nothing
     * (no impact, no ring), both gone at K+0.5 with the bullets.
     */
    @Test
    void theHomingDiamondsAndTheLobsInFlightFadeToo() {
        Sortie sortie = phaseThree();
        Ship ship = sortie.ship();
        EnemyGun diamond = Act5BSpecs.samGun(2, 1, 4, 0, false);
        // A lob that would land on the ship 0.25 s after K, another still in flight at the fade's end.
        sortie.force.lobs().obtain().launch(Act6HSpecs.BILE, 240, 400, ship.x(), ship.y());
        for (int i = 0; i < SimStep.ticks(0.75); i++) {
            sortie.step(0);
        }
        assertEquals(1, sortie.lobCount());
        assertTrue(sortie.missiles().launch(ship.x(), ship.y() + 60, diamond, ship.x(), ship.y()));
        sortie.force.lobs().obtain().launch(Act6HSpecs.BILE, 240, 400, ship.x(), ship.y());
        Act7FSpecs.kill(sortie, Act7FSpecs.CAPSULE);
        assertEquals(3, value(sortie, SimEvents.Type.BULLETS_FADE), "a diamond and two lobs fade");
        assertTrue(sortie.missile(0).fading(), "the diamond fades");
        assertFalse(sortie.missile(0).seekable(), "and is no target");
        for (int i = 0; i < sortie.lobCount(); i++) {
            assertTrue(sortie.lob(i).fading(), "every lob fades");
        }
        double heading = sortie.missile(0).heading();
        double shield = ship.defences().shield();
        double armour = ship.defences().armour();
        int fade = SimStep.ticks(0.5);
        for (int step = 1; step <= SimStep.ticks(1.5); step++) {
            sortie.step(0);
            assertTrue(ship.defences().armour() >= armour && ship.defences().shield() >= shield, "harmless at " + step);
            assertEquals(0, count(sortie, SimEvents.Type.MORTAR_IMPACT), "no lob lands at " + step);
            assertEquals(0, count(sortie, SimEvents.Type.MISSILE_DESTROYED), "the diamond bursts on nothing");
            if (step < fade) {
                assertEquals(1, sortie.missileCount(), "fading at " + step);
                assertEquals(heading, sortie.missile(0).heading(), 1e-12, "it homes no more");
                assertTrue(sortie.lobCount() >= 1, "the late lob flies on at " + step);
            } else {
                assertEquals(0, sortie.missileCount(), "gone by K+0.5");
                assertEquals(0, sortie.lobCount(), "gone by K+0.5");
            }
        }
    }

    @Test
    void itsFightTimeIsKsAndAResetStartsItAfresh() {
        Sortie sortie = phaseThree();
        SetPiece boss = sortie.setPiece(0);
        Act7FSpecs.kill(sortie, Act7FSpecs.CAPSULE);
        double atK = boss.killSeconds();
        assertTrue(atK > 0);
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(0);
        }
        assertEquals(atK, boss.killSeconds(), 1e-9, "the fight's time is K's");
        boss.reset();
        LastWords words = boss.lastWords().orElseThrow();
        assertFalse(words.spoken());
        assertFalse(words.burstDone());
        assertEquals(1, words.bulletAlpha(), 1e-9);
    }

    @Test
    void itRunsTheSameEveryTime() {
        Sortie one = phaseThree();
        Sortie two = phaseThree();
        Act7FSpecs.kill(one, Act7FSpecs.CAPSULE);
        Act7FSpecs.kill(two, Act7FSpecs.CAPSULE);
        for (int i = 0; i < SimStep.ticks(6); i++) {
            one.step(0);
            two.step(0);
            assertEquals(one.stateHash(), two.stateHash());
        }
    }
}
