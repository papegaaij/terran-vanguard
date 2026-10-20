package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M7 part H, step H1 (design/campaign Level 21, the slowly scrolling arena): a crawling arena ({@link
 * Section#crawls()}) keeps its ground scrolling at its speed while the level clock is held, and the kill's jump moves
 * the clock only; it eases in from the section before over its ease. An arena that does not crawl stops its ground
 * as before. On {@link BreachArenaTest}'s colossus and level (the arena from 3 s to 23 s at 20 px/s).
 */
class CrawlingArenaTest {
    private static final double STEP_20 = 20 * SimStep.SECONDS;

    private static Sortie sortie(boolean crawl) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                BreachArenaTest.level(false, crawl),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Steps once and returns how far the ground scrolled in it. */
    private static double step(Sortie sortie) {
        double before = sortie.groundScroll();
        sortie.step(0);
        return sortie.groundScroll() - before;
    }

    @Test
    void theArenaEasesInFrom140To20OverItsFirstTwoSeconds() {
        Sortie sortie = sortie(true);
        assertTrue(sortie.crawling());
        int start = SimStep.ticks(BreachArenaTest.ARRIVE);
        double last = 0;
        for (int tick = 0; tick < start + SimStep.ticks(3); tick++) {
            int level = (int) Math.round(sortie.levelSeconds() * SimStep.PER_SECOND);
            double moved = step(sortie);
            if (level < start) {
                assertEquals(140 * SimStep.SECONDS, moved, 1e-9, "140 px/s before the arena");
            } else if (level < start + SimStep.ticks(2)) {
                assertTrue(moved < last + 1e-12, "easing down");
                assertTrue(moved > STEP_20 - 1e-12, "not below the crawl");
            } else {
                assertEquals(STEP_20, moved, 1e-9, "20 px/s once eased");
            }
            last = moved;
        }
        assertEquals(STEP_20, last, 1e-9);
    }

    @Test
    void theGroundCrawlsOnWhileTheClockIsHeldAndThroughTheKillsRamp() {
        Sortie sortie = sortie(true);
        while (sortie.levelSeconds() < BreachArenaTest.ARENA_END - 2) {
            sortie.step(0);
        }
        double moved = 0;
        for (int i = 0; i < SimStep.ticks(10); i++) {
            moved = step(sortie);
            assertEquals(STEP_20, moved, 1e-9, "the arena's 20 px/s, held or not");
        }
        assertTrue(sortie.arenaHalted(), "the level clock is held at the arena's end");
        assertEquals(BreachArenaTest.ARENA_END - SimStep.SECONDS, sortie.levelSeconds(), 1e-9);
        assertEquals(20, sortie.groundSpeed(), 1e-9);
        SetPiece boss = BreachArenaTest.boss(sortie);
        boss.damagePart(BreachArenaTest.MOUTH, 10 * BreachArenaTest.MOUTH_HP);
        sortie.objectivePasses.wreck(0, BreachArenaTest.MOUTH);
        double previous = moved;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            moved = step(sortie);
            assertTrue(moved >= previous - 1e-12, "it ramps up from the crawl, no jump");
            assertTrue(moved - previous < 3 * SimStep.SECONDS, "a smooth ramp");
            previous = moved;
        }
        assertEquals(140 * SimStep.SECONDS, moved, 1e-9, "the next section's speed");
        assertFalse(sortie.arenaHalted());
    }

    @Test
    void anEarlyKillsJumpMovesTheClockOnlyTheGroundCrawlsOnUnbroken() {
        Sortie sortie = sortie(true);
        BreachArenaTest.until(sortie, SimEvents.Type.BREACH_RISE, 12, 0);
        BreachArenaTest.steps(sortie, 1, 0);
        assertTrue(sortie.levelSeconds() < BreachArenaTest.ARENA_END - 5, "well before the arena's end");
        SetPiece boss = BreachArenaTest.boss(sortie);
        boss.damagePart(BreachArenaTest.MOUTH, 10 * BreachArenaTest.MOUTH_HP);
        sortie.objectivePasses.wreck(0, BreachArenaTest.MOUTH);
        double moved = step(sortie);
        assertTrue(sortie.levelSeconds() > BreachArenaTest.ARENA_END - 2 * SimStep.SECONDS, "the clock jumped");
        assertTrue(moved < 2 * STEP_20, "no visible ground moved with the jump: " + moved);
        assertTrue(moved >= STEP_20 - 1e-12, "it ramps up from the crawl");
    }

    @Test
    void anArenaThatDoesNotCrawlStopsItsGroundWhileTheClockIsHeld() {
        Sortie sortie = sortie(false);
        assertFalse(sortie.crawling());
        while (sortie.levelSeconds() < BreachArenaTest.ARENA_END) {
            sortie.step(0);
            assertTrue(sortie.levelSeconds() < BreachArenaTest.ARENA_END + 1, "held");
            if (sortie.arenaHalted()) {
                break;
            }
        }
        for (int i = 0; i < SimStep.ticks(2); i++) {
            assertEquals(0, step(sortie), 1e-12, "the ground stops while the clock is held, as before");
        }
        assertTrue(sortie.arenaHalted());
    }

    @Test
    void onlyAMovingArenaCrawlsAndOnlyACrawlingArenaEasesIn() {
        assertThrows(IllegalArgumentException.class, () -> new Section(10, 0, true, 0, 0, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new Section(10, 20, false, 0, 0, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new Section(10, 20, true, 0, 2, 0, false));
        assertTrue(new Section(10, 20, true, 0, 2, 0, true).crawls());
        assertFalse(new Section(10, 30, true).crawls(), "Levels 05 and 07's moving arenas do not crawl");
        // A crawling arena is a breaching boss's.
        LevelScript level = LevelScript.builder(21, 3)
                .sections(List.of(new Section(3, 140), new Section(23, 20, true, 0, 0, 0, true), new Section(30, 140)))
                .build();
        assertThrows(
                IllegalArgumentException.class,
                () -> new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR));
    }
}
