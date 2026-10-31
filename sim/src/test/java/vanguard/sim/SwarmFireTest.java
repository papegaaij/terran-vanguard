package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part B, step B1 (design/enemies/air/chimera: "in a swarm at most 3 members fire at once, staggered 0.3 s"; gap
 * check m10b §4): a firing swarm's members fire their aimed gun in the Talon's 300 px band, their first volleys a
 * stagger apart by their place, at most the cap's volleys in any stagger window; a member over the cap skips its
 * volley ({@link SimEvents.Type#VOLLEY_CAPPED}), decided as its glint would start. A swarm without a cap holds its fire.
 */
class SwarmFireTest {
    /** The steps of every volley the swarm fired in {@code seconds}, the ship idle; the capped volleys in {@code capped}. */
    private static List<Integer> volleys(EnemySpec unit, int count, double seconds, int[] capped) {
        Sortie sortie =
                CloudDiversSpecs.sortie(CloudDiversSpecs.level(30, List.of(CloudDiversSpecs.swarm(0.5, unit, count))));
        List<Integer> fired = new ArrayList<>();
        for (int tick = 0; tick < SimStep.ticks(seconds); tick++) {
            sortie.step(Command.NONE);
            for (int n = sortie.events().count(SimEvents.Type.ENEMY_FIRED); n > 0; n--) {
                fired.add(tick);
            }
            capped[0] += sortie.events().count(SimEvents.Type.VOLLEY_CAPPED);
        }
        return fired;
    }

    /** The most volleys within any window of {@code window} steps. */
    private static int most(List<Integer> fired, int window) {
        int most = 0;
        for (int i = 0; i < fired.size(); i++) {
            int n = 0;
            for (int j = i; j < fired.size() && fired.get(j) - fired.get(i) < window; j++) {
                n++;
            }
            most = Math.max(most, n);
        }
        return most;
    }

    @Test
    void atMostThreeVolleysFallInAnyStaggerWindow() {
        int[] capped = {0};
        List<Integer> fired = volleys(CloudDiversSpecs.CHIMERA, 10, 12, capped);
        assertTrue(fired.size() >= 10, "the swarm fires: " + fired.size());
        assertTrue(most(fired, SimStep.ticks(0.3)) <= 3, "at most 3 at once: " + most(fired, SimStep.ticks(0.3)));
    }

    @Test
    void aTightCapSkipsVolleysWithTheirClockRunningOn() {
        EnemySpec tight = CloudDiversSpecs.chimera(new EnemySpec.SwarmFire(1, 1.0, 300));
        int[] capped = {0};
        List<Integer> fired = volleys(tight, 10, 12, capped);
        assertTrue(fired.size() > 0);
        assertEquals(1, most(fired, SimStep.ticks(1.0)), "one volley a second at most");
        assertTrue(capped[0] > 0, "the others skip theirs");
    }

    @Test
    void theMembersFirstVolleysAreAStaggerApartByTheirPlace() {
        Sortie sortie = CloudDiversSpecs.sortie(
                CloudDiversSpecs.level(30, List.of(CloudDiversSpecs.swarm(0, CloudDiversSpecs.CHIMERA, 8))));
        sortie.step(Command.NONE);
        List<Enemy> members = CloudDiversSpecs.all(sortie, "chimera");
        assertEquals(8, members.size());
        for (Enemy member : members) {
            int expected = SimStep.ticks(0.25) + 1 + SimStep.ticks(member.member() * 0.3) % SimStep.ticks(1.6);
            assertTrue(
                    member.volleyTicks == expected || member.volleyTicks == expected - 1,
                    "member " + member.member() + ": " + member.volleyTicks + " vs " + expected);
        }
    }

    @Test
    void aSwarmWithoutAFireCapHoldsItsFire() {
        EnemySpec plain = EnemySpec.of("chimera", 40, new Hitbox(28, 24), Layer.AIR, 10, true, 14, 220)
                .withGun(Optional.of(CloudDiversSpecs.CHIMERA_GUN))
                .withFlock(Optional.of(PartDSpecs.FLOCK));
        int[] capped = {0};
        assertEquals(0, volleys(plain, 8, 10, capped).size(), "a swarm before Act 6 never fires");
        assertEquals(0, capped[0]);
    }

    @Test
    void theCapCountsTheGrantedVolleysInItsWindow() {
        SwarmVolleys volleys = new SwarmVolleys();
        volleys.start(Optional.empty());
        assertFalse(volleys.capped());
        assertTrue(volleys.grant(0));
        volleys.start(Optional.of(CloudDiversSpecs.FIRE));
        assertTrue(volleys.capped());
        int window = SimStep.ticks(0.3);
        assertTrue(volleys.grant(10));
        assertTrue(volleys.grant(11));
        assertTrue(volleys.grant(12));
        assertFalse(volleys.grant(12), "a fourth in the window");
        assertFalse(volleys.grant(10 + window - 1));
        assertTrue(volleys.grant(10 + window), "the first one's window is over");
        assertFalse(volleys.grant(10 + window), "the second one's is not");
        assertTrue(volleys.grant(11 + window));
    }

    @Test
    void aFireCapAllowsOneToAFlocksMostVolleysInAFireBand() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.SwarmFire(0, 0.3, 300));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.SwarmFire(Flock.MAX_MEMBERS + 1, 0.3, 300));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.SwarmFire(3, -0.1, 300));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.SwarmFire(3, 0.3, 0));
        assertEquals(
                Optional.of(CloudDiversSpecs.FIRE),
                PartDSpecs.FLOCK
                        .withFire(Optional.of(CloudDiversSpecs.FIRE))
                        .withRibbon(Optional.empty(), Optional.empty())
                        .fire(),
                "the ribbon keeps the fire cap");
    }
}
