package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G1 (design/campaign Level 49, the rush's arenas; m11g-gaps.md §1 #1): a rush's gates each have
 * their own arena ({@link GateArenas}): the first crawls under its descending boss, the second crawls under its
 * breaching boss (stilled while it is out of the sand, in that gate only), the third halts under its anchored boss,
 * which sits at its hover height as the gate halts. Every other level's one arena is untouched (the golden flights).
 */
class RushArenasTest {
    private static final double CRAWL_STEP = Act7GSpecs.CRAWL * SimStep.SECONDS;

    @Test
    void eachGateCrawlsOrHaltsAsItsArenaSays() {
        Sortie sortie = Act7GSpecs.sortie(Act7GSpecs.rush(Act7GSpecs.SECTIONS, Act7GSpecs.arenaBosses()));
        Act7GSpecs.untilFight(sortie, 0);
        for (int i = 0; i < SimStep.ticks(3); i++) {
            assertEquals(CRAWL_STEP, Act7GSpecs.step(sortie), 1e-9, "gate 0's ground crawls while its clock stands");
        }
        assertNull(sortie.bosses().breach(), "no breaching boss in gate 0");
        Act7GSpecs.kill(sortie, 0);
        Act7GSpecs.untilFight(sortie, 1);
        assertSame(sortie.setPiece(1), sortie.bosses().breach(), "gate 1's breaching boss");
        Act7GSpecs.kill(sortie, 1);
        Act7GSpecs.untilFight(sortie, 2);
        assertNull(sortie.bosses().breach(), "the breach is gate 1's only");
        for (int i = 0; i < SimStep.ticks(2); i++) {
            assertEquals(0, Act7GSpecs.step(sortie), 1e-9, "gate 2 halts");
        }
    }

    @Test
    void theCrawlIntoACrawlingGateEasesToItsSpeedNotToAHalt() {
        Sortie sortie = Act7GSpecs.sortie(Act7GSpecs.rush(Act7GSpecs.SECTIONS, Act7GSpecs.arenaBosses()));
        double least = Double.MAX_VALUE;
        while (sortie.levelSeconds() < 11.9) {
            double step = Act7GSpecs.step(sortie);
            if (sortie.levelSeconds() > 10) {
                least = Math.min(least, step);
            }
        }
        assertTrue(least >= CRAWL_STEP - 1e-9, "never below the crawl: " + least);
    }

    @Test
    void theBreachingBossStillsItsGatesGroundWhileOutOfTheSand() {
        Sortie sortie = Act7GSpecs.sortie(Act7GSpecs.rush(Act7GSpecs.SECTIONS, Act7GSpecs.arenaBosses()));
        Act7GSpecs.untilFight(sortie, 0);
        Act7GSpecs.kill(sortie, 0);
        Act7GSpecs.untilFight(sortie, 1);
        BreachArena breach = sortie.setPiece(1).breach().orElseThrow();
        boolean still = false;
        for (int i = 0; i < SimStep.ticks(20) && !still; i++) {
            double step = Act7GSpecs.step(sortie);
            still = breach.phase() == BreachArena.Phase.OPEN && step == 0;
        }
        assertTrue(still, "the ground stands while the mouth is open");
        assertTrue(sortie.crawlFactor() < 1e-9);
    }

    @Test
    void theAnchoredBossSitsAtItsHoverHeightAsItsGateHalts() {
        Sortie sortie = Act7GSpecs.sortie(Act7GSpecs.rush(Act7GSpecs.SECTIONS, Act7GSpecs.arenaBosses()));
        Act7GSpecs.untilFight(sortie, 0);
        Act7GSpecs.kill(sortie, 0);
        Act7GSpecs.untilFight(sortie, 1);
        Act7GSpecs.kill(sortie, 1);
        Act7GSpecs.untilFight(sortie, 2);
        SetPiece sovereign = sortie.setPiece(2);
        assertEquals(sovereign.boss().orElseThrow().hoverY(), sovereign.y(), 1.0, "at its hover height at the halt");
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(0);
        }
        assertTrue(sovereign.engaged(), "it engages on its halted gate");
        assertEquals(sovereign.boss().orElseThrow().hoverY(), sovereign.y(), 1.0, "it stays there");
    }

    @Test
    void aRushIsDeterministic() {
        LevelScript script = Act7GSpecs.rush(Act7GSpecs.SECTIONS, Act7GSpecs.arenaBosses());
        Sortie a = Act7GSpecs.sortie(script);
        Sortie b = Act7GSpecs.sortie(script);
        for (int i = 0; i < SimStep.ticks(45); i++) {
            if (i == SimStep.ticks(14)) {
                Act7GSpecs.kill(a, 0);
                Act7GSpecs.kill(b, 0);
            }
            a.step(0);
            b.step(0);
        }
        assertEquals(a.stateHash(), b.stateHash());
    }

    @Test
    void onlyARushsGatesCrawlOrHoldAnAnchoredOrBreachingBoss() {
        // A rush's breaching boss holds a crawling gate, its anchored one a halted gate.
        List<Section> halted = List.of(
                new Section(10, 100),
                new Section(12, 0, true),
                new Section(20, 120, false, 2, 2),
                new Section(30, 0, true),
                new Section(38, 120, false, 2, 2),
                new Section(40, 0, true),
                new Section(55, 120, false, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> Act7GSpecs.rush(halted, Act7GSpecs.arenaBosses()));
        List<Section> crawlingLast = List.of(
                new Section(10, 100),
                new Section(12, 0, true),
                new Section(20, 120, false, 2, 2),
                new Section(30, Act7GSpecs.CRAWL, true, 0, 0, 0, true),
                new Section(38, 120, false, 2, 2),
                new Section(40, Act7GSpecs.CRAWL, true, 0, 0, 0, true),
                new Section(55, 120, false, 2, 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> Act7GSpecs.sortie(Act7GSpecs.rush(crawlingLast, Act7GSpecs.arenaBosses())));
    }
}
