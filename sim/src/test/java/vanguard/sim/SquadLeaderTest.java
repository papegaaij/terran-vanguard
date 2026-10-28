package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M9 part B, step B1 (design/enemies/air/talon, Squad leader; m9b-gaps.md §1.4): a V-wing's tip flies the leader's stat
 * block; killing it scatters the wing's living members: each breaks off sideways away from the wing's centre, holds its
 * fire and is past the nearer side edge within 1.5 s (hard: one parting volley each as it breaks off). A member killed
 * pays as ever; a member's death scatters nothing.
 */
class SquadLeaderTest {
    private static Sortie sortie(EnemySpec talon) {
        return TestSpecs.sortie(TestSpecs.level(20, List.of(Act5BSpecs.vWing(0.5, talon))));
    }

    /** The wing on the screen, holding. */
    private static Sortie holding(EnemySpec talon) {
        Sortie sortie = sortie(talon);
        for (int i = 0; i < SimStep.ticks(3.5); i++) {
            sortie.step(0);
        }
        assertEquals(5, sortie.enemyCount());
        return sortie;
    }

    private static int leader(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).leader()) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void theTipLeadsWithItsOwnStatBlock() {
        Sortie sortie = holding(Act5BSpecs.WING_TALON);
        int leaders = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy talon = sortie.enemy(i);
            leaders += talon.leader() ? 1 : 0;
            assertEquals(talon.leader() ? 25 : 12, talon.spec().bounty());
            assertEquals(talon.leader() ? 48 : 32, talon.spec().hp());
        }
        assertEquals(1, leaders);
    }

    @Test
    void killingTheLeaderScattersTheWingSidewaysOutWithoutFire() {
        Sortie sortie = holding(Act5BSpecs.WING_TALON);
        double centre = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            centre += sortie.enemy(i).x() / sortie.enemyCount();
        }
        sortie.destroyEnemy(leader(sortie));
        assertEquals(1, PartCSpecs.count(sortie, SimEvents.Type.SQUAD_SCATTER));
        assertEquals(4, sortie.enemyCount());
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy talon = sortie.enemy(i);
            assertTrue(talon.scattering());
        }
        int fired = 0;
        int steps = 0;
        while (sortie.enemyCount() > 0) {
            sortie.step(0);
            fired += PartCSpecs.count(sortie, SimEvents.Type.ENEMY_FIRED);
            steps++;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy talon = sortie.enemy(i);
                assertTrue((talon.x() - centre) * (talon.x() - talon.renderX(0)) >= 0, "away from the wing's centre");
            }
        }
        assertEquals(0, fired, "they hold their fire");
        assertTrue(steps <= SimStep.ticks(1.5) + 1, "past the side edges within 1.5 s: " + steps);
        assertEquals(1, sortie.kills(), "the scattered pay nothing");
    }

    @Test
    void onHardEachFiresOnePartingVolley() {
        Sortie sortie = holding(Act5BSpecs.wingTalon(Act5BSpecs.scatter(true)));
        sortie.destroyEnemy(leader(sortie));
        int parting = PartCSpecs.count(sortie, SimEvents.Type.ENEMY_FIRED);
        assertEquals(4, parting);
        int later = 0;
        while (sortie.enemyCount() > 0) {
            sortie.step(0);
            later += PartCSpecs.count(sortie, SimEvents.Type.ENEMY_FIRED);
        }
        assertEquals(0, later);
    }

    @Test
    void aMembersDeathScattersNothingAndAScatteringOnePays() {
        Sortie sortie = holding(Act5BSpecs.WING_TALON);
        int member = leader(sortie) == 0 ? 1 : 0;
        sortie.destroyEnemy(member);
        assertEquals(0, PartCSpecs.count(sortie, SimEvents.Type.SQUAD_SCATTER));
        for (int i = 0; i < sortie.enemyCount(); i++) {
            assertFalse(sortie.enemy(i).scattering());
        }
        int credits = sortie.credits();
        sortie.destroyEnemy(leader(sortie));
        int afterLeader = sortie.credits();
        sortie.destroyEnemy(0);
        assertTrue(sortie.credits() > afterLeader && afterLeader > credits, "a scattering Talon killed pays");
    }

    @Test
    void aLeaderLeadsAWingThatScatters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5BSpecs.vWing(0.5, TestSpecs.NEEDLER.withShield(java.util.Optional.empty())));
    }
}
