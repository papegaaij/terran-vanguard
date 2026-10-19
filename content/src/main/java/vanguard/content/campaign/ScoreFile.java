package vanguard.content.campaign;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;
import vanguard.content.Difficulty;

/**
 * The high-score file (design/systems/scoring, High scores): {@code scores.yaml} next to the
 * settings file, like the saves directory. Reading never fails: a missing file is an empty table,
 * a file that is not YAML is an empty table too (and is kept as {@code scores.yaml.bad} when the
 * next score is written), and an entry that does not hold a valid name, score, level and date is
 * left out. It is written next to its place and moved in, so a crash never leaves half a file. The
 * file of a debug run ({@link #readOnly}) is only read, by the rule of the save slots.
 *
 * <pre>
 * version: 1
 * last_name: "LANCER"
 * easy: []
 * medium:
 *   - {name: "LANCER", score: 1482650, level: 12, date: "2026-10-12"}
 * hard: []
 * </pre>
 */
public final class ScoreFile {
    public static final String FILE_NAME = "scores.yaml";
    static final int VERSION = 1;

    private static final YAMLMapper MAPPER = YAMLMapper.builder().build();

    private final Path path;
    private final boolean writable;

    private ScoreFile(Path path, boolean writable) {
        this.path = path;
        this.writable = writable;
    }

    /** The file of a normal run: read and written. */
    public static ScoreFile of(Path path) {
        return new ScoreFile(path, true);
    }

    /** The file of a debug run: read, never written. */
    public static ScoreFile readOnly(Path path) {
        return new ScoreFile(path, false);
    }

    public Path path() {
        return path;
    }

    /** Whether scores are written: false in a debug run. */
    public boolean writable() {
        return writable;
    }

    /** The tables in the file; empty when it is missing or unreadable. */
    public HighScores read() {
        if (!Files.isRegularFile(path)) {
            return HighScores.empty();
        }
        try {
            return parse(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException | JacksonException e) {
            return HighScores.empty();
        }
    }

    /**
     * Writes the tables, replacing the file; a read-only file writes nothing. A file there that is
     * not YAML is kept as {@code scores.yaml.bad} first.
     *
     * @return whether the file was written: false for a read-only file
     */
    public boolean write(HighScores scores) throws IOException {
        if (!writable) {
            return false;
        }
        Path directory = path.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        if (Files.isRegularFile(path) && !readable(path)) {
            Files.copy(path, path.resolveSibling(path.getFileName() + ".bad"), StandardCopyOption.REPLACE_EXISTING);
        }
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, format(scores), StandardCharsets.UTF_8);
        Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        return true;
    }

    private static boolean readable(Path file) {
        try {
            parse(Files.readString(file, StandardCharsets.UTF_8));
            return true;
        } catch (IOException | JacksonException e) {
            return false;
        }
    }

    /** The tables in a file's text; an entry that is not valid is left out. */
    static HighScores parse(String text) {
        JsonNode tree = MAPPER.readTree(text);
        if (tree == null || !tree.isObject()) {
            return HighScores.empty();
        }
        Map<Difficulty, List<HighScores.Entry>> tables = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            List<HighScores.Entry> entries = new ArrayList<>();
            JsonNode list = tree.get(key(difficulty));
            if (list != null && list.isArray()) {
                for (JsonNode row : list) {
                    entry(row).ifPresent(entries::add);
                }
            }
            tables.put(difficulty, entries);
        }
        JsonNode last = tree.get("last_name");
        Optional<String> lastName =
                last != null && last.isString() ? Optional.of(last.stringValue()) : Optional.empty();
        return HighScores.of(tables, lastName);
    }

    private static Optional<HighScores.Entry> entry(JsonNode row) {
        JsonNode name = row.get("name");
        JsonNode score = row.get("score");
        JsonNode level = row.get("level");
        JsonNode date = row.get("date");
        if (name == null
                || !name.isString()
                || score == null
                || !score.isIntegralNumber()
                || !score.canConvertToLong()
                || level == null
                || !level.isInt()
                || date == null
                || !date.isString()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new HighScores.Entry(
                    name.stringValue(), score.longValue(), level.intValue(), LocalDate.parse(date.stringValue())));
        } catch (IllegalArgumentException | DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /** The file's text: names and dates quoted, one entry per line. */
    static String format(HighScores scores) {
        StringBuilder text = new StringBuilder();
        text.append("# Terran Vanguard high scores: the top 10 per difficulty (design/systems/scoring)\n");
        text.append("version: ").append(VERSION).append('\n');
        scores.lastName()
                .ifPresent(name -> text.append("last_name: \"").append(name).append("\"\n"));
        for (Difficulty difficulty : Difficulty.values()) {
            List<HighScores.Entry> table = scores.table(difficulty);
            text.append(key(difficulty)).append(':');
            if (table.isEmpty()) {
                text.append(" []\n");
                continue;
            }
            text.append('\n');
            for (HighScores.Entry entry : table) {
                text.append(String.format(
                        Locale.ROOT,
                        "  - {name: \"%s\", score: %d, level: %d, date: \"%s\"}\n",
                        entry.name(),
                        entry.score(),
                        entry.level(),
                        entry.date()));
            }
        }
        return text.toString();
    }

    private static String key(Difficulty difficulty) {
        return difficulty.name().toLowerCase(Locale.ROOT);
    }
}
