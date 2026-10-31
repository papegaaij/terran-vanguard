package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vanguard.sim.LevelResult;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * Golden flights (refactoring step RF1): every level 01–35 on every difficulty, flown by the autopilot on the
 * balance plan's fit ({@link PacingTest#planLoadout}, the one {@code levelReport -Pfit=plan} flies) on
 * {@link LevelMetrics#SEED}, to its end ({@link LevelMetrics#fly}, capped at {@link PacingTest#MAX_STEPS}), compared
 * with the recorded end tick, kills, credits, state hash and boss-down ticks in {@code golden-flights.txt}. Where a
 * boss level's medium flight does not bring every boss down, the recording adds a medium flight on another seed that
 * does. Unlike the level tests (two runs equal), this pins recorded values, so a refactoring of the simulation that
 * changes any behaviour fails here.
 *
 * <p>The pin guards the simulation refactorings: it is recorded once and never re-recorded for a refactoring. A
 * deliberate rule or data change re-records it on purpose:
 * {@code ./gradlew :content:test --tests '*GoldenFlightsTest' -Dvanguard.recordDir=content/src/test/resources}
 * (the asserts are skipped while recording).
 */
class GoldenFlightsTest {
    private static final String FILE = "golden-flights.txt";
    private static final int LAST_LEVEL = 35;
    /** The seeds a boss level's extra medium flight tries, in order, when the plan's seed leaves a boss alive. */
    private static final long[] EXTRA_SEEDS = {1, 2, 3, 4, 5, 6, 7, 8};

    /** One flight's identity: level number, difficulty and seed. */
    record Key(int level, Difficulty difficulty, long seed) {
        @Override
        public String toString() {
            return String.format(
                    Locale.ROOT, "%02d %s %d", level, difficulty.name().toLowerCase(Locale.ROOT), seed);
        }

        static Key parse(String[] fields) {
            return new Key(Integer.parseInt(fields[0]), Difficulty.of(fields[1]), Long.parseLong(fields[2]));
        }
    }

    /** One flight's line: {@code level difficulty seed tick kills credits hash bosses}. */
    record Golden(Key key, long tick, int kills, int credits, String hash, String bosses) {
        String line() {
            return String.format(Locale.ROOT, "%s %d %d %d %s %s", key, tick, kills, credits, hash, bosses);
        }

        /** Whether the level has a boss (a set piece with a boss bar). */
        boolean hasBoss() {
            return !bosses.equals("-");
        }

        /** Whether every boss came down (each has its kill tick). */
        boolean bossesDown() {
            return hasBoss() && !bosses.contains("@-");
        }
    }

    /** Notes the tick each boss set piece came down on the attempt that ends the flight. */
    private static final class BossWatch implements LevelMetrics.Probe {
        private final long[] down;

        BossWatch(Sortie sortie) {
            down = new long[sortie.setPieceCount()];
            newAttempt();
        }

        @Override
        public void newAttempt() {
            Arrays.fill(down, -1);
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            for (int i = 0; i < down.length; i++) {
                if (down[i] < 0) {
                    SetPiece piece = sortie.setPiece(i);
                    if (piece.boss().isPresent() && piece.destroyed()) {
                        down[i] = sortie.tick();
                    }
                }
            }
        }

        /** {@code slug@tick} per boss in set-piece order ({@code slug@-} for one still alive), or {@code -}. */
        String bosses(Sortie sortie) {
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < down.length; i++) {
                SetPiece piece = sortie.setPiece(i);
                if (piece.boss().isPresent()) {
                    parts.add(piece.slug() + "@" + (down[i] < 0 ? "-" : Long.toString(down[i])));
                }
            }
            return parts.isEmpty() ? "-" : String.join(",", parts);
        }
    }

    static Golden fly(Key key) {
        Content content = LevelMetrics.CONTENT;
        String level = content.levelKey(key.level()).orElseThrow();
        Sortie sortie = LevelMetrics.sortie(
                content, level, key.difficulty(), PacingTest.planLoadout(level, key.difficulty()), key.seed());
        BossWatch watch = new BossWatch(sortie);
        LevelMetrics.fly(sortie, PacingTest.MAX_STEPS, watch);
        LevelResult result = sortie.result();
        return new Golden(
                key,
                sortie.tick(),
                result.kills(),
                result.credits().total(),
                Long.toHexString(sortie.stateHash()),
                watch.bosses(sortie));
    }

    /** Flies the flights in parallel, in their order. */
    private static List<Golden> flyAll(List<Key> keys) throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            List<Future<Golden>> futures = new ArrayList<>();
            for (Key key : keys) {
                futures.add(pool.submit(() -> fly(key)));
            }
            List<Golden> flights = new ArrayList<>();
            for (Future<Golden> future : futures) {
                flights.add(future.get());
            }
            return flights;
        } finally {
            pool.shutdownNow();
        }
    }

    /** Every level on every difficulty on the plan's seed. */
    private static List<Key> planFlights() {
        List<Key> keys = new ArrayList<>();
        for (int level = 1; level <= LAST_LEVEL; level++) {
            for (Difficulty difficulty : Difficulty.values()) {
                keys.add(new Key(level, difficulty, LevelMetrics.SEED));
            }
        }
        return keys;
    }

    private static Map<Key, String> recorded() throws IOException {
        Map<Key, String> lines = new LinkedHashMap<>();
        try (var in = GoldenFlightsTest.class.getResourceAsStream("/" + FILE)) {
            assertNotNull(in, FILE + " is recorded");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String trimmed = line.strip();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    lines.put(Key.parse(trimmed.split(" ")), trimmed);
                }
            }
        }
        return lines;
    }

    @Test
    @DisabledIfSystemProperty(named = "vanguard.recordDir", matches = ".+")
    void everyFlightEndsInItsRecordedState() throws Exception {
        Map<Key, String> expected = recorded();
        for (Key key : planFlights()) {
            assertTrue(expected.containsKey(key), "a recorded flight for " + key);
        }
        List<Golden> flights = flyAll(List.copyOf(expected.keySet()));
        List<String> differ = new ArrayList<>();
        for (Golden flight : flights) {
            String want = expected.get(flight.key());
            if (!want.equals(flight.line())) {
                differ.add("  recorded " + want + "\n  flown    " + flight.line());
            }
        }
        assertEquals(
                List.of(),
                differ,
                differ.size() + " of " + flights.size() + " flights differ (level difficulty seed tick kills credits"
                        + " hash bosses)");
    }

    @Test
    @EnabledIfSystemProperty(named = "vanguard.recordDir", matches = ".+")
    void recordAnew() throws Exception {
        List<Golden> flights = new ArrayList<>(flyAll(planFlights()));
        List<Golden> extra = new ArrayList<>();
        for (Golden flight : flights) {
            if (flight.key().difficulty() == Difficulty.MEDIUM && flight.hasBoss() && !flight.bossesDown()) {
                for (long seed : EXTRA_SEEDS) {
                    Golden other = fly(new Key(flight.key().level(), Difficulty.MEDIUM, seed));
                    if (other.bossesDown()) {
                        extra.add(other);
                        break;
                    }
                }
            }
        }
        flights.addAll(extra);
        flights.sort(Comparator.comparingInt((Golden flight) -> flight.key().level())
                .thenComparing(flight -> flight.key().difficulty())
                .thenComparingLong(flight -> flight.key().seed()));

        List<String> lines = new ArrayList<>();
        lines.add("# Golden flights (GoldenFlightsTest, refactoring step RF1): recorded once, never re-recorded to");
        lines.add("# make a refactoring pass. Columns: level difficulty seed tick kills credits hash bosses, where");
        lines.add(
                "# bosses is slug@tick per boss set piece (the tick it came down, - if alive at the end) or - for none.");
        for (Golden flight : flights) {
            lines.add(flight.line());
        }
        Path file = Path.of(System.getProperty("vanguard.recordDir")).resolve(FILE);
        Files.write(file, lines, StandardCharsets.US_ASCII);
        System.out.println("recorded " + flights.size() + " flights in " + file.toAbsolutePath());
    }
}
