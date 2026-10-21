package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part H, step H1 (design/campaign Level 21, the slowly scrolling arena): a crawling arena ({@link
 * Section#crawls()}) keeps its ground scrolling at its speed while the level clock is held, and the kill's jump moves
 * the clock only; it eases in from the section before over its ease. An arena that does not crawl stops its ground
 * as before. On {@link BreachArenaTest}'s colossus and level (the arena from 3 s to 23 s at 20 px/s). Step SIM-K (round
 * 44, K.c): the crawl eases to a stop while the Colossus is out of the sand or its bulge is on it (from the tell
 * through the dive) and back.
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

    /** The most a smoothstep ease from 20 px/s to 0 over the crawl's ease changes a step's scroll by, px. */
    private static final double EASE_STEP = 1.5 * STEP_20 / SimStep.ticks(Sortie.CRAWL_EASE_SECONDS) + 1e-12;

    @Test
    void theGroundStandsStillWhileTheMouthIsOutAndCrawlsWhileItIsBuried() {
        Sortie sortie = sortie(true);
        BreachArena breach = BreachArenaTest.breach(sortie);
        while (sortie.levelSeconds() < BreachArenaTest.ARRIVE + 2.5) {
            sortie.step(0);
        }
        double previous = step(sortie);
        assertEquals(STEP_20, previous, 1e-9, "the intro crawls at 20 px/s");
        assertEquals(20, sortie.groundSpeed(), 1e-9);
        int still = 0;
        int moving = 0;
        int breaches = 0;
        boolean held = false;
        for (int i = 0; i < SimStep.ticks(40); i++) {
            double moved = step(sortie);
            BreachArena.Phase phase = breach.phase();
            assertTrue(moved >= 0 && moved <= STEP_20 + 1e-9, "between a stop and the crawl");
            assertTrue(Math.abs(moved - previous) <= EASE_STEP, "eased, never a jump: " + previous + " -> " + moved);
            if (phase == BreachArena.Phase.TELL
                    || phase == BreachArena.Phase.RISE
                    || phase == BreachArena.Phase.OPEN
                    || phase == BreachArena.Phase.BITE
                    || phase == BreachArena.Phase.DIVE) {
                assertEquals(0, moved, 0, "no ground motion under the bulge or the risen mouth (" + phase + ")");
                assertEquals(0, sortie.groundSpeed(), 0);
                still++;
            } else if (Math.abs(moved - STEP_20) < 1e-9) {
                moving++;
            }
            breaches = breach.breaches();
            held |= sortie.arenaHalted();
            previous = moved;
        }
        assertTrue(breaches >= 3, "several breaches: " + breaches);
        assertTrue(held, "on both sides of the clock's hold");
        assertTrue(still > SimStep.ticks(20), "still while out: " + still);
        assertTrue(moving > SimStep.ticks(2), "at full crawl in the intro: " + moving);
    }

    /** {@link BreachArenaTest}'s colossus lying {@code buried} s between a dive's end and the next rise. */
    private static Sortie buriedFor(double buried) {
        BossSpec.Breach b = BreachArenaTest.BREACH;
        BossSpec.Breach longer = new BossSpec.Breach(
                b.tellSeconds(),
                b.riseSeconds(),
                b.openSeconds(),
                b.biteTellSeconds(),
                b.diveSeconds(),
                buried,
                b.warnSeconds(),
                b.points(),
                b.clamps(),
                b.orders(),
                b.biteRadius(),
                b.biteDamage(),
                b.introSeconds(),
                b.glowSeconds());
        SetPieceSpec piece = BreachArenaTest.colossus(false);
        SetPieceSpec spec = new SetPieceSpec(
                piece.slug(),
                piece.size(),
                piece.body(),
                piece.contactDamage(),
                piece.parts(),
                piece.passes(),
                piece.drop(),
                Optional.of(piece.boss().orElseThrow().withBreach(Optional.of(longer))));
        LevelScript level = LevelScript.builder(21, 3)
                .sections(BreachArenaTest.level(false, true).sections())
                .setPieces(List.of(spec))
                .build();
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    @Test
    void theGroundStopsAsTheTellStartsAndStartsAgainOnlyOnceTheMouthHasDived() {
        // The design's cycle lies buried only as long as its tell (no crawl between breaches); 4 s shows both eases.
        Sortie sortie = buriedFor(4);
        BreachArena breach = BreachArenaTest.breach(sortie);
        double last = -1;
        while (breach.phase() != BreachArena.Phase.TELL) {
            last = step(sortie);
        }
        assertEquals(0, last, 0, "still from the tell's first step");
        while (breach.phase() != BreachArena.Phase.DIVE) {
            assertEquals(0, step(sortie), 0, "still through the tell, the rise, the open mouth and the bite");
        }
        while (breach.phase() == BreachArena.Phase.DIVE) {
            assertEquals(0, step(sortie), 0, "still through the whole dive");
        }
        assertEquals(BreachArena.Phase.BURIED, breach.phase());
        double moved = step(sortie);
        assertTrue(moved > 0 && moved < EASE_STEP, "then it eases in again: " + moved);
        double most = moved;
        while (breach.phase() == BreachArena.Phase.BURIED) {
            last = step(sortie);
            most = Math.max(most, last);
        }
        assertEquals(STEP_20, most, 1e-9, "at full crawl between the eases");
        assertEquals(BreachArena.Phase.TELL, breach.phase());
        assertEquals(0, last, 0, "and still again as the next tell starts");
    }

    @Test
    void aKillWhileTheMouthIsOutRampsTheGroundUpFromAStopNoJump() {
        Sortie sortie = sortie(true);
        while (!sortie.arenaHalted()) {
            sortie.step(0);
        }
        BreachArenaTest.untilPhase(sortie, BreachArena.Phase.OPEN, 15);
        assertEquals(0, step(sortie), 0, "still while open");
        SetPiece boss = BreachArenaTest.boss(sortie);
        boss.damagePart(BreachArenaTest.MOUTH, 10 * BreachArenaTest.MOUTH_HP);
        sortie.objectivePasses.wreck(0, BreachArenaTest.MOUTH);
        double previous = 0;
        double moved = 0;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            moved = step(sortie);
            assertTrue(moved >= previous - 1e-12, "it ramps up from the stop, no jump");
            assertTrue(moved - previous < 5 * SimStep.SECONDS, "a smooth ramp");
            previous = moved;
        }
        assertEquals(140 * SimStep.SECONDS, moved, 1e-9, "the next section's speed");
        assertFalse(sortie.arenaHalted());
        assertEquals(1, sortie.crawlFactor(), 0);
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
        assertTrue(
                moved >= 0 && moved <= EASE_STEP, "no ground moved with the jump; it eases in from the stop: " + moved);
    }

    @Test
    void aBreachingBossInAnArenaOfSpeedZeroLeavesItsGroundStill() {
        LevelScript level = LevelScript.builder(21, 3)
                .sections(List.of(
                        new Section(BreachArenaTest.ARRIVE, BreachArenaTest.SPEED),
                        new Section(BreachArenaTest.ARENA_END, 0, true),
                        new Section(BreachArenaTest.ARENA_END + 20, BreachArenaTest.SPEED)))
                .setPieces(List.of(BreachArenaTest.colossus(false)))
                .build();
        Sortie sortie =
                new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        assertFalse(sortie.crawling());
        while (sortie.levelSeconds() < BreachArenaTest.ARRIVE + 2) {
            sortie.step(0);
        }
        for (int i = 0; i < SimStep.ticks(20); i++) {
            assertEquals(0, step(sortie), 0, "an arena of speed 0 stands still, as before");
            assertEquals(1, sortie.crawlFactor(), 0, "no crawl to ease");
        }
        assertTrue(BreachArenaTest.breach(sortie).breaches() > 0);
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
