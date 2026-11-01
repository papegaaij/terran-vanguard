package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vanguard.sim.LevelScript;
import vanguard.sim.WaveSpec;

/**
 * M10 part C, step C6a (round 75 F.3, the user's note 2026-11-01): on a level whose air allies take the player's fire
 * ({@code player_hits}: Level 37's shuttles) no air unit hovers or flies in behind one in the line of fire while it is
 * on its station, sliding aside or climbing out ({@link ShuttleLanes}: longer than {@value ShuttleLanes#SECONDS} s above
 * it with more than half its width inside the shuttle's column), the script flown unharmed. The shorter crossings and
 * the ground units scrolling under a shuttle are printed.
 */
class ShuttleLaneTest {
    private static final Content CONTENT = LevelMetrics.CONTENT;

    static Stream<Arguments> levels() {
        return IntStream.rangeClosed(1, 50)
                .mapToObj(CONTENT::levelKey)
                .flatMap(java.util.Optional::stream)
                .filter(key -> SimSpecs.level(CONTENT, key, Difficulty.MEDIUM)
                        .objectives()
                        .airAllies()
                        .flatMap(allies -> allies.air())
                        .map(air -> air.playerHits())
                        .orElse(false))
                .flatMap(key -> Stream.of(Difficulty.values()).map(difficulty -> Arguments.of(key, difficulty)));
    }

    @ParameterizedTest
    @MethodSource("levels")
    void noUnitHidesBehindAShuttle(String key, Difficulty difficulty) {
        LevelScript level = SimSpecs.level(CONTENT, key, difficulty);
        double last = level.waves().stream().mapToDouble(WaveSpec::t).max().orElse(0);
        List<ShuttleLanes.Behind> spans = ShuttleLanes.behind(
                CONTENT,
                key,
                difficulty,
                PacingTest.planLoadout(key, difficulty),
                last + HullStackingTest.AFTER_LAST_WAVE);
        List<ShuttleLanes.Behind> breaches = ShuttleLanes.breaches(spans);
        StringBuilder out = new StringBuilder(String.format("Shuttle lanes %s %s:", key, difficulty));
        spans.forEach(span -> out.append("\n    ").append(span).append(breaches.contains(span) ? "  <- BREACH" : ""));
        System.out.println(out);
        assertTrue(breaches.isEmpty(), key + " " + difficulty + ": units behind a shuttle " + breaches);
    }
}
