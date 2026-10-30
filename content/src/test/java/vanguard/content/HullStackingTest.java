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
 * Step FIX7 (the user's re-test 2026-10-30, Levels 30 and 31: "the large droneships fly on top of each other"): no two
 * big hulls on the air layer ({@code medium} and up: the Gilded Gunships, Minelayers, Hornets) overlap on the screen for
 * more than {@value LevelMetrics#STACK_SECONDS} s while the script flies unharmed ({@link LevelMetrics#stacking}: the
 * ship holds the lower centre without firing). Asserted from Act 5 on; Acts 1-4 (released) are printed only.
 */
class HullStackingTest {
    private static final Content CONTENT = LevelMetrics.CONTENT;

    /** The first act the rule is asserted for. */
    static final int FIRST_ACT = 5;

    /** Seconds flown past the level's last wave. */
    static final double AFTER_LAST_WAVE = 30;

    static Stream<Arguments> levels() {
        return IntStream.rangeClosed(1, 50)
                .mapToObj(CONTENT::levelKey)
                .flatMap(java.util.Optional::stream)
                .flatMap(key -> Stream.of(Difficulty.values()).map(difficulty -> Arguments.of(key, difficulty)));
    }

    /** The stackings of level {@code key} at {@code difficulty} over the rule's time. */
    static List<LevelMetrics.Stack> stackings(String key, Difficulty difficulty) {
        LevelScript level = SimSpecs.level(CONTENT, key, difficulty);
        double last = level.waves().stream().mapToDouble(WaveSpec::t).max().orElse(0);
        return LevelMetrics.stacking(
                CONTENT, key, difficulty, PacingTest.planLoadout(key, difficulty), last + AFTER_LAST_WAVE);
    }

    @ParameterizedTest
    @MethodSource("levels")
    void bigHullsNeverFlyOnTopOfEachOther(String key, Difficulty difficulty) {
        List<LevelMetrics.Stack> over = stackings(key, difficulty);
        boolean asserted = SimSpecs.level(CONTENT, key, difficulty).act() >= FIRST_ACT;
        if (!over.isEmpty()) {
            System.out.printf("Hull stacking %s %s%s: %s%n", key, difficulty, asserted ? "" : " (report only)", over);
        }
        if (asserted) {
            assertTrue(over.isEmpty(), key + " " + difficulty + ": big hulls stacked " + over);
        }
    }
}
