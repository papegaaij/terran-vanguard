package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;

/** The top 10 per difficulty (design/systems/scoring, High scores): ranking, ties and the name rules. */
class HighScoresTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 12);

    private static HighScores.Entry entry(String name, long score, int level, LocalDate date) {
        return new HighScores.Entry(name, score, level, date);
    }

    /** A full hard table: 1000, 900, … 100, all entered on {@code DAY - 1}. */
    private static HighScores fullHard() {
        HighScores scores = HighScores.empty();
        for (int i = 0; i < HighScores.SIZE; i++) {
            scores = scores.add(Difficulty.HARD, entry("PILOT" + i, 1000 - 100L * i, 5, DAY.minusDays(1)));
        }
        return scores;
    }

    @Test
    void anEmptyTableTakesAnyScoreAboveZero() {
        HighScores empty = HighScores.empty();
        assertEquals(OptionalInt.of(0), empty.rank(Difficulty.EASY, 1, DAY));
        assertFalse(empty.qualifies(Difficulty.EASY, 0, DAY), "a score of 0 is no high score");
        assertTrue(empty.table(Difficulty.MEDIUM).isEmpty());
        assertEquals(Optional.empty(), empty.lastName());
    }

    @Test
    void entriesRankByScoreAndAFullTableDropsItsLast() {
        HighScores scores = fullHard();
        assertEquals(OptionalInt.of(3), scores.rank(Difficulty.HARD, 750, DAY));
        assertFalse(scores.qualifies(Difficulty.HARD, 99, DAY));

        HighScores added = scores.add(Difficulty.HARD, entry("LANCER", 750, 12, DAY));
        List<HighScores.Entry> table = added.table(Difficulty.HARD);
        assertEquals(HighScores.SIZE, table.size());
        assertEquals("LANCER", table.get(3).name());
        assertEquals(200, table.get(HighScores.SIZE - 1).score(), "100 dropped out");
        assertEquals(Optional.of("LANCER"), added.lastName());
        assertEquals(1000, scores.table(Difficulty.HARD).getFirst().score(), "the old table is unchanged");
        assertTrue(added.table(Difficulty.EASY).isEmpty(), "other difficulties are apart");
    }

    @Test
    void anEqualScoreRanksBelowTheEarlierEntries() {
        HighScores scores = fullHard();
        assertEquals(OptionalInt.of(6), scores.rank(Difficulty.HARD, 500, DAY), "below the 500 of yesterday");
        assertEquals(
                OptionalInt.of(5),
                scores.rank(Difficulty.HARD, 500, DAY.minusDays(2)),
                "an earlier date ranks above (stored entries)");
        assertFalse(scores.qualifies(Difficulty.HARD, 100, DAY), "a tie with the 10th does not push it out");

        HighScores same = HighScores.empty()
                .add(Difficulty.EASY, entry("FIRST", 300, 3, DAY))
                .add(Difficulty.EASY, entry("SECOND", 300, 3, DAY));
        assertEquals(
                List.of("FIRST", "SECOND"),
                same.table(Difficulty.EASY).stream().map(HighScores.Entry::name).toList(),
                "the same day: the one entered first ranks first");
    }

    @Test
    void storedTablesAreSortedAndCut() {
        List<HighScores.Entry> stored = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            stored.add(entry("PILOT" + i, 100L * (i + 1), 1, DAY.minusDays(i)));
        }
        stored.add(entry("TIED", 1200, 1, DAY.minusDays(20)));
        HighScores scores = HighScores.of(Map.of(Difficulty.MEDIUM, stored), Optional.of("bad name"));
        List<HighScores.Entry> table = scores.table(Difficulty.MEDIUM);
        assertEquals(HighScores.SIZE, table.size());
        assertEquals("TIED", table.get(0).name(), "1200 of the earlier date first");
        assertEquals("PILOT11", table.get(1).name());
        assertEquals(400, table.getLast().score());
        assertEquals(Optional.empty(), scores.lastName(), "an invalid last name is dropped");
    }

    @Test
    void namesAreThreeToTenOfTheAllowedCharacters() {
        assertTrue(HighScores.validName("ACE"));
        assertTrue(HighScores.validName("LANCER-2 X"));
        assertFalse(HighScores.validName("AB"), "too short");
        assertFalse(HighScores.validName("ABCDEFGHIJK"), "too long");
        assertFalse(HighScores.validName("ace"), "lower case is raised before it is stored");
        assertFalse(HighScores.validName(" ACE"), "no space at either end");
        assertFalse(HighScores.validName("A.C"), "no dot");
        assertFalse(HighScores.validName("ÄCE"), "A-Z only");
        assertTrue(HighScores.allowed('q'));
        assertTrue(HighScores.allowed('-'));
        assertFalse(HighScores.allowed('_'));
        assertEquals("ROOK", HighScores.normalise("  rook "));
        assertThrows(IllegalArgumentException.class, () -> entry("AB", 10, 1, DAY));
        assertThrows(IllegalArgumentException.class, () -> entry("ACE", 0, 1, DAY));
        assertThrows(
                IllegalArgumentException.class,
                () -> fullHard().add(Difficulty.HARD, entry("LOW", 50, 1, DAY)),
                "only a qualifying score is added");
    }
}
