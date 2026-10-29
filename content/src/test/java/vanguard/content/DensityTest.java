package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import vanguard.sim.LevelScript;

/**
 * The campaign's minimum density (design/campaign, Difficulty curve): every level reaches its
 * act's minimum in enemies per minute of scroll at medium, air and ground units with the released
 * spawns (a periodic spawner's one release, M5 part C), the launch and the boss fight (an arena section and what follows it) excluded. The
 * warm-up Levels 01-03 have their own, lower minimum.
 */
class DensityTest {
    /** Enemies per minute per act (Act 1 first). */
    static final int[] ACT_MINIMUM = {40, 50, 55, 60, 70, 75, 80};
    /** The warm-up levels' minimum, Levels 01-03. */
    static final int WARM_UP_MINIMUM = 32;

    static final int LAST_WARM_UP = 3;

    private static final Content CONTENT = LevelMetrics.CONTENT;

    /** M9 part E: a forked level (Level 32) on each of its routes ({@link RoutePlay}). */
    static Stream<org.junit.jupiter.params.provider.Arguments> levels() {
        return CONTENT.levels().keySet().stream().flatMap(key -> RoutePlay.routes(CONTENT, key).stream()
                .map(route -> org.junit.jupiter.params.provider.Arguments.of(key, route)));
    }

    @ParameterizedTest
    @MethodSource("levels")
    void theLevelReachesItsMinimumDensity(String key, String route) {
        LevelScript level = SimSpecs.level(CONTENT, key, Difficulty.MEDIUM);
        double density = LevelMetrics.density(level, route).perMinute();
        int minimum = LevelMetrics.densityMinimum(level);
        String at = key + (route.isEmpty() ? "" : " route " + route);
        System.out.printf("Density %s: %.1f enemies per minute (minimum %d)%n", at, density, minimum);
        assertTrue(density >= minimum, at + ": " + density + " enemies per minute, minimum " + minimum);
    }

    /** The level's enemies per minute of scroll, from the end of the launch to its first arena. */
    static double perMinute(LevelScript level) {
        return LevelMetrics.density(level).perMinute();
    }
}
