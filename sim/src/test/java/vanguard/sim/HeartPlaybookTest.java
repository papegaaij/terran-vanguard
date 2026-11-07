package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M11 part H, step H1b (design/enemies/bosses/choir-heart, The playbooks; m11h-gaps.md §1 #2, #6): on the test Heart
 * ({@link Act7HPlaySpecs}) phase 1's songs come each from its own membrane, never the same membrane twice running, each
 * living one at least once in any eight draws over 500 draws a seed, the Mote cloud at most every third; from the half
 * (three membranes dead) two sing at once, the second's tell 0.4 s after the first's, never two releasing, never one
 * membrane twice; the last membrane sings its own song and the Herald's by turns; phase 2 draws a line at least every
 * third draw, spits from the walls' vein points clear of the ship's height and broods from the wall pits; phase 3 draws
 * two at once, never two lines, never the last pair again, a line at least every third draw; a run replays.
 */
class HeartPlaybookTest {
    private static final long[] SEEDS = {1, 7, 42};

    /** One draw: its move, the part it fires from, the step it was drawn at and whether it was a pair's second. */
    private record Draw(int move, int source, int step, boolean second) {}

    /**
     * Steps the sortie (the ship still) until {@code count} draws are seen in the boss's current phase (at most {@code
     * seconds}); checks on every step that each windowed part is open exactly while a lane sings from it.
     */
    private static List<Draw> draws(Sortie sortie, int count, double seconds) {
        SetPiece heart = sortie.setPiece(0);
        BossPlaybook book = heart.playbook().orElseThrow();
        PartWindows windows = heart.partWindows().orElseThrow();
        List<Draw> seen = new ArrayList<>();
        int before = book.draws();
        for (int i = 0; i < SimStep.ticks(seconds) && seen.size() < count; i++) {
            sortie.step(0);
            int now = book.draws();
            if (now < before) {
                before = 0;
            }
            for (int d = before; d < now; d++) {
                int lane = d - before;
                seen.add(new Draw(book.move(lane), book.source(lane), i, lane == 1));
            }
            before = now;
            for (int p : Act7HPlaySpecs.MEMBRANES) {
                boolean sings = false;
                for (int l = 0; l < BossPlaybook.LANES; l++) {
                    sings |= book.mode(l) != BossPlaybook.Mode.REST && book.source(l) == p;
                }
                assertEquals(
                        sings || windows.held(p),
                        windows.open(p) && heart.partHp(p) > 0,
                        "membrane " + p + " open exactly while it sings, step " + i);
            }
        }
        assertEquals(count, seen.size(), "the draws came");
        return seen;
    }

    private static Sortie engaged(long seed, SetPieceSpec heart) {
        Sortie sortie = Act7HPlaySpecs.invulnerable(seed, heart);
        Act7HSpecs.untilEngaged(sortie);
        return sortie;
    }

    @Test
    void eachMembraneSingsItsOwnSongNeverTwiceRunningEveryOneInAnyEightDraws() {
        SetPieceSpec heart = Act7HPlaySpecs.heart(Act7HPlaySpecs.boss(true));
        for (long seed : SEEDS) {
            Sortie sortie = engaged(seed, heart);
            List<Draw> draws = draws(sortie, 500, 3000);
            int lastMotes = -10;
            for (int i = 0; i < draws.size(); i++) {
                Draw draw = draws.get(i);
                assertTrue(!draw.second(), "one at a time before the half");
                assertEquals(Act7HPlaySpecs.MEMBRANES[draw.move()], draw.source(), "a song from its own membrane");
                if (i > 0) {
                    assertNotEquals(draws.get(i - 1).source(), draw.source(), "never the same membrane twice running");
                }
                if (draw.move() == Act7HPlaySpecs.SONG_MOTES) {
                    assertTrue(i - lastMotes >= 3, "the Mote cloud at most every third draw: " + i);
                    lastMotes = i;
                }
                if (i + 8 <= draws.size()) {
                    Set<Integer> window = new HashSet<>();
                    for (int k = i; k < i + 8; k++) {
                        window.add(draws.get(k).source());
                    }
                    assertEquals(6, window.size(), "every membrane in draws " + i + "–" + (i + 7) + ", seed " + seed);
                }
            }
        }
    }

    @Test
    void fromTheHalfTwoSingAtOnceTheSecondsTellAGapLaterNeverTwoReleasing() {
        for (long seed : SEEDS) {
            Sortie sortie = engaged(seed, Act7HPlaySpecs.HEART);
            Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.M1, Act7HPlaySpecs.M2);
            for (Draw draw : draws(sortie, 40, 400)) {
                assertTrue(!draw.second(), "before the half one at a time");
            }
            Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.M3);
            BossPlaybook book = sortie.setPiece(0).playbook().orElseThrow();
            int pairs = 0;
            int waited = -1;
            int telling = -1;
            List<Draw> draws = new ArrayList<>();
            int before = book.draws();
            for (int i = 0; i < SimStep.ticks(600) && draws.size() < 200; i++) {
                boolean wasWaiting = book.waiting(1);
                sortie.step(0);
                int now = book.draws();
                for (int d = before; d < now; d++) {
                    draws.add(new Draw(book.move(d - before), book.source(d - before), i, d - before == 1));
                }
                if (now - before == 2) {
                    pairs++;
                    waited = i;
                    PhasePlaybook.Kind a = book.kind(0);
                    PhasePlaybook.Kind b = book.kind(1);
                    assertTrue(!(PhasePlaybook.releases(a) && PhasePlaybook.releases(b)), "at most one releasing");
                    assertNotEquals(book.source(0), book.source(1), "two membranes");
                }
                before = now;
                if (wasWaiting && book.mode(1) == BossPlaybook.Mode.TELL) {
                    telling = i;
                    assertEquals(SimStep.ticks(0.4), telling - waited, "the second's tell 0.4 s after the first's");
                }
            }
            assertTrue(pairs > 20, "pairs from the half: " + pairs);
            assertTrue(telling >= 0);
            // The three living membranes each still in any eight draws.
            for (int i = 0; i + 8 <= draws.size(); i++) {
                Set<Integer> window = new HashSet<>();
                for (int k = i; k < i + 8; k++) {
                    window.add(draws.get(k).source());
                }
                assertEquals(3, window.size(), "every living membrane in any eight draws, seed " + seed);
            }
        }
    }

    @Test
    void theLastMembraneSingsItsOwnSongAndTheHeraldsByTurns() {
        Sortie sortie = engaged(3, Act7HPlaySpecs.HEART);
        Act7HPlaySpecs.kill(
                sortie, Act7HPlaySpecs.M1, Act7HPlaySpecs.M2, Act7HPlaySpecs.M3, Act7HPlaySpecs.M5, Act7HPlaySpecs.M6);
        List<Draw> draws = draws(sortie, 31, 300);
        draws = draws.subList(1, draws.size());
        for (int i = 0; i < draws.size(); i++) {
            Draw draw = draws.get(i);
            assertEquals(Act7HPlaySpecs.M4, draw.source(), "from the last membrane");
            assertTrue(!draw.second(), "one membrane sings one song at a time");
            if (i > 0) {
                assertNotEquals(draws.get(i - 1).move(), draw.move(), "its own and the Herald's by turns");
            }
            assertTrue(
                    draw.move() == Act7HPlaySpecs.SONG_SEEDS || draw.move() == Act7HPlaySpecs.SONG_HERALD,
                    "its own song or the Herald's: " + draw.move());
        }
    }

    private static Sortie intoArena(long seed) {
        Sortie sortie = engaged(seed, Act7HPlaySpecs.HEART);
        Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.MEMBRANES);
        Act7HSpecs.untilPhase(sortie, 1);
        return sortie;
    }

    @Test
    void theArenaDrawsALineEveryThirdSpitsFromTheWallsAndBroodsFromThePits() {
        for (long seed : SEEDS) {
            Sortie sortie = intoArena(seed);
            SetPiece heart = sortie.setPiece(0);
            BossPlaybook book = heart.playbook().orElseThrow();
            ArenaWalls walls = sortie.arenaWalls();
            int sinceLine = 0;
            int spits = 0;
            int pitUnits = 0;
            int before = book.draws();
            int drawn = 0;
            for (int i = 0; i < SimStep.ticks(400) && drawn < 100; i++) {
                Set<Object> old = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
                for (int b = 0; b < sortie.bulletCount(); b++) {
                    old.add(sortie.bullet(b));
                }
                for (int u = 0; u < sortie.enemyCount(); u++) {
                    old.add(sortie.enemy(u));
                }
                double shipY = sortie.ship().y();
                sortie.step(0);
                for (int d = before; d < book.draws(); d++) {
                    drawn++;
                    sinceLine = PhasePlaybook.lance(book.kind(0)) ? 0 : sinceLine + 1;
                    assertTrue(sinceLine < 3, "a line at least every third draw, seed " + seed);
                }
                before = book.draws();
                if (book.move() == Act7HPlaySpecs.ARENA_SPIT && book.mode() == BossPlaybook.Mode.RUN) {
                    for (int b = 0; b < sortie.bulletCount(); b++) {
                        EnemyBullet bullet = sortie.bullet(b);
                        if (old.contains(bullet)) {
                            continue;
                        }
                        double face = bullet.x() < PlayField.WIDTH / 2.0
                                ? walls.leftAt(bullet.y()) + BossPlaybook.SPIT_INSET
                                : walls.rightAt(bullet.y()) - BossPlaybook.SPIT_INSET;
                        if (Math.abs(bullet.x() - face) < 6) {
                            spits++;
                            assertTrue(
                                    Math.abs(bullet.y() - shipY) >= 120 - 6, "a vein point clear of the ship's height");
                        }
                    }
                }
                if (book.move() == Act7HPlaySpecs.ARENA_PITS) {
                    for (int u = 0; u < sortie.enemyCount(); u++) {
                        Enemy unit = sortie.enemy(u);
                        if (old.contains(unit)) {
                            continue;
                        }
                        boolean left = Math.abs(unit.y() - Act7HPlaySpecs.PIT_LEFT_Y) < 12
                                && Math.abs(unit.x() - walls.leftAt(unit.y()) - 6) < 12;
                        boolean right = Math.abs(unit.y() - Act7HPlaySpecs.PIT_RIGHT_Y) < 12
                                && Math.abs(unit.x() - walls.rightAt(unit.y()) + 6) < 12;
                        assertTrue(left || right, "out of a wall pit: " + unit.x() + ", " + unit.y());
                        pitUnits++;
                    }
                }
            }
            assertTrue(drawn >= 100, "the arena draws: " + drawn);
            assertTrue(spits > 0, "the walls spit");
            assertTrue(pitUnits > 0, "the pits brood");
        }
    }

    @Test
    void theStormDrawsTwoAtOnceNeverTwoLinesNeverTheSamePairALineEveryThird() {
        for (long seed : SEEDS) {
            Sortie sortie = intoArena(seed);
            Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.NODES);
            Act7HSpecs.untilPhase(sortie, 2);
            BossPlaybook book = sortie.setPiece(0).playbook().orElseThrow();
            int sinceLine = 0;
            int pairs = 0;
            int lastA = -1;
            int lastB = -1;
            int before = book.draws();
            int drawn = 0;
            for (int i = 0; i < SimStep.ticks(600) && drawn < 300; i++) {
                sortie.step(0);
                int now = book.draws();
                for (int d = before; d < now; d++) {
                    drawn++;
                    sinceLine = PhasePlaybook.lance(book.kind(d - before)) ? 0 : sinceLine + 1;
                    assertTrue(sinceLine < 3, "a line at least every third draw, seed " + seed);
                }
                if (now - before == 2) {
                    pairs++;
                    int a = book.move(0);
                    int b = book.move(1);
                    assertTrue(
                            !(PhasePlaybook.lance(book.kind(0)) && PhasePlaybook.lance(book.kind(1))),
                            "never two told lines together");
                    assertTrue(!((a == lastA && b == lastB) || (a == lastB && b == lastA)), "never the same pair");
                    lastA = a;
                    lastB = b;
                } else if (now - before == 1) {
                    lastA = book.move(0);
                    lastB = -1;
                }
                before = now;
            }
            assertTrue(drawn >= 300, "the storm draws: " + drawn);
            assertTrue(pairs * 2 >= drawn * 3 / 4, "mostly two at once: " + pairs + " pairs of " + drawn);
        }
    }

    @Test
    void aRunReplays() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = engaged(5, Act7HPlaySpecs.HEART);
            Act7HSpecs.steps(sortie, 20);
            Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.M1, Act7HPlaySpecs.M2, Act7HPlaySpecs.M3);
            Act7HSpecs.steps(sortie, 20);
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1]);
    }

    @Test
    void aPlaceClearOfAPointLiesOnEitherSideByTheirSizes() {
        assertEquals(24, BossPlaybook.clearOf(0, 24, 456, 300, 80), 1e-9);
        assertEquals(380, BossPlaybook.clearOf((196.0 + 1e-9) / 272, 24, 456, 300, 80), 1e-6);
        double right = BossPlaybook.clearOf(0.99, 24, 456, 300, 80);
        assertTrue(right >= 380 && right <= 456);
        assertEquals(456, BossPlaybook.clearOf(0.5, 24, 456, 200, 300), 1e-9, "no room: the farther end");
    }
}
