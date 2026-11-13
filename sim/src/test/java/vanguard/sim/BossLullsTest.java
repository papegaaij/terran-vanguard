package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Step PT2 ({@link BossLulls}; the user's Level 50 playthrough, 2026-11-12; design/enemies/bosses/choir-heart, Phases:
 * the lulls): once a phase has fired {@code every − seconds} s the next lull is due, the playbook draws no move, and it
 * starts as soon as the playbook rests: for its length only the kept attacks fire, every living membrane is open, no
 * move is drawn; its start and end raise {@link SimEvents.Type#BOSS_LULL}; then the clock starts again. A run replays.
 */
class BossLullsTest {
    /** A lull of 3.5 s every 13 s, the heartbeat kept. */
    static final BossLullsSpec LULLS = new BossLullsSpec(13, 3.5, List.of(Act7HPlaySpecs.HEARTBEAT), true);

    /** The test Heart with its lulls (the builder's samples' too). */
    static final SetPieceSpec HEART =
            Act7HPlaySpecs.heart(Act7HPlaySpecs.boss(true).withLulls(Optional.of(LULLS)));

    private static Sortie engaged() {
        Sortie sortie = Act7HPlaySpecs.invulnerable(1, HEART);
        Act7HSpecs.untilEngaged(sortie);
        return sortie;
    }

    private static int lullEvents(Sortie sortie, int value) {
        SimEvents events = sortie.events();
        int n = 0;
        for (int i = 0; i < events.size(); i++) {
            n += events.type(i) == SimEvents.Type.BOSS_LULL && events.value(i) == value ? 1 : 0;
        }
        return n;
    }

    @Test
    void aLullComesOnceThePlaybookRestsAndSilencesAllButTheHeartbeat() {
        Sortie sortie = engaged();
        SetPiece heart = sortie.setPiece(0);
        BossLulls lulls = heart.lulls().orElseThrow();
        BossPlaybook book = heart.playbook().orElseThrow();
        PartWindows windows = heart.partWindows().orElseThrow();
        int fire = SimStep.ticks(13) - SimStep.ticks(3.5);
        int steps = 0;
        int seen = 0;
        int lastEnd = -1;
        while (seen < 2 && steps < SimStep.ticks(60)) {
            sortie.step(0);
            steps++;
            if (lullEvents(sortie, 1) == 0) {
                continue;
            }
            seen++;
            assertTrue(steps - Math.max(0, lastEnd) >= fire, "a lull only after its fire time: " + steps);
            assertTrue(book.resting(), "it starts once the playbook rests");
            int draws = book.draws();
            int length = 0;
            while (lulls.quiet()) {
                assertTrue(lulls.silences(Act7HPlaySpecs.HERALD), "the songs and guns silent");
                assertFalse(lulls.silences(Act7HPlaySpecs.HEARTBEAT), "the heartbeat beats on");
                for (int m : Act7HPlaySpecs.MEMBRANES) {
                    if (heart.partHp(m) > 0) {
                        assertTrue(windows.open(m), "membrane " + m + " open in the lull");
                    }
                }
                assertTrue(lulls.share() > 0);
                sortie.step(0);
                steps++;
                length++;
                if (lulls.quiet()) {
                    assertEquals(draws, book.draws(), "no move drawn in a lull");
                }
            }
            assertEquals(1, lullEvents(sortie, 0), "its end raised");
            assertEquals(SimStep.ticks(3.5), length, "3.5 s long");
            lastEnd = steps;
        }
        assertEquals(2, seen, "two lulls in the first 60 s");
    }

    @Test
    void aRunWithLullsReplays() {
        Sortie a = engaged();
        Sortie b = engaged();
        for (int i = 0; i < SimStep.ticks(40); i++) {
            a.step(0);
            b.step(0);
        }
        assertEquals(a.stateHash(), b.stateHash());
    }

    @Test
    void aLullIsShorterThanItsPeriodAndKeepsItsOwnAttacks() {
        assertThrows(IllegalArgumentException.class, () -> new BossLullsSpec(3, 3.5, List.of(), true));
        assertThrows(IllegalArgumentException.class, () -> Act7HPlaySpecs.boss(true)
                .withLulls(Optional.of(new BossLullsSpec(13, 3.5, List.of(99), true))));
    }
}
