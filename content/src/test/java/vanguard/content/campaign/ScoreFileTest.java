package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vanguard.content.Difficulty;

/** scores.yaml next to the settings file: written and read back, robust to a missing or damaged file. */
class ScoreFileTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 12);

    private static HighScores sample() {
        return HighScores.empty()
                .add(Difficulty.HARD, new HighScores.Entry("LANCER", 1_482_650, 12, DAY))
                .add(Difficulty.HARD, new HighScores.Entry("ROOK-2", 980_400, 9, DAY.minusDays(3)))
                .add(Difficulty.MEDIUM, new HighScores.Entry("A B", 310_900, 14, DAY));
    }

    @Test
    void writtenScoresReadBack(@TempDir Path directory) throws IOException {
        ScoreFile file = ScoreFile.of(directory.resolve("config").resolve(ScoreFile.FILE_NAME));
        assertTrue(file.write(sample()), "the directory is made");
        HighScores read = file.read();
        assertEquals(sample().table(Difficulty.HARD), read.table(Difficulty.HARD));
        assertEquals(sample().table(Difficulty.MEDIUM), read.table(Difficulty.MEDIUM));
        assertTrue(read.table(Difficulty.EASY).isEmpty());
        assertEquals(Optional.of("A B"), read.lastName(), "the last name entered");
        assertFalse(Files.exists(file.path().resolveSibling(ScoreFile.FILE_NAME + ".tmp")));
    }

    @Test
    void aMissingFileIsAnEmptyTable(@TempDir Path directory) {
        HighScores read = ScoreFile.of(directory.resolve(ScoreFile.FILE_NAME)).read();
        assertTrue(read.table(Difficulty.HARD).isEmpty());
        assertEquals(Optional.empty(), read.lastName());
    }

    @Test
    void aDamagedFileIsAnEmptyTableAndKeptAsBad(@TempDir Path directory) throws IOException {
        Path path = directory.resolve(ScoreFile.FILE_NAME);
        Files.writeString(path, "easy: [ {name: \"ACE\"\n  :::", StandardCharsets.UTF_8);
        ScoreFile file = ScoreFile.of(path);
        assertTrue(file.read().table(Difficulty.EASY).isEmpty());

        file.write(sample());
        assertEquals(sample().table(Difficulty.HARD), file.read().table(Difficulty.HARD));
        assertTrue(Files.readString(directory.resolve(ScoreFile.FILE_NAME + ".bad"), StandardCharsets.UTF_8)
                .startsWith("easy: ["));
    }

    @Test
    void invalidEntriesAreLeftOut(@TempDir Path directory) throws IOException {
        Path path = directory.resolve(ScoreFile.FILE_NAME);
        Files.writeString(path, """
                version: 1
                last_name: 42
                hard:
                  - {name: "ACE", score: 900, level: 7, date: "2026-10-01"}
                  - {name: "x", score: 800, level: 7, date: "2026-10-01"}
                  - {name: "BOLT", score: -5, level: 7, date: "2026-10-01"}
                  - {name: "SAM", score: 700, level: 0, date: "2026-10-01"}
                  - {name: "JUNO", score: 600, level: 3, date: "yesterday"}
                  - {name: "REX", score: "500", level: 3, date: "2026-10-01"}
                  - "ZED"
                medium: "none"
                """, StandardCharsets.UTF_8);
        HighScores read = ScoreFile.of(path).read();
        assertEquals(
                java.util.List.of(new HighScores.Entry("ACE", 900, 7, LocalDate.of(2026, 10, 1))),
                read.table(Difficulty.HARD));
        assertTrue(read.table(Difficulty.MEDIUM).isEmpty());
        assertEquals(Optional.empty(), read.lastName());
    }

    @Test
    void aDebugRunNeverWrites(@TempDir Path directory) throws IOException {
        ScoreFile file = ScoreFile.readOnly(directory.resolve("config").resolve(ScoreFile.FILE_NAME));
        assertFalse(file.writable());
        assertFalse(file.write(sample()));
        assertFalse(Files.exists(directory.resolve("config")), "not even the directory");
    }
}
