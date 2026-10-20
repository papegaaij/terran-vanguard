package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;

/**
 * Every level with a data file has its {@link RadioTimeline} checks (the user's playtest of 2026-10-19:
 * a radio line cut short by the level's end must not happen "on any level"): its runs in {@link
 * RadioTimeline#RUNS} and its subclass {@code RadioTimelineLevelNNTest}, so a new level's data cannot
 * skip them.
 */
class RadioTimelineLevelsTest {
    @Test
    void everyLevelWithDataHasItsRadioTimeline() {
        for (String level : RadioTimeline.CONTENT.levels().keySet()) {
            assertTrue(RadioTimeline.RUNS.containsKey(level), level + " has its runs in RadioTimeline.RUNS");
            String name = String.format(
                    Locale.ROOT, "vanguard.game.level.RadioTimelineLevel%02dTest", Content.levelNumber(level));
            Class<?> test;
            try {
                test = Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new AssertionError(level + " has its test class " + name, e);
            }
            assertTrue(RadioTimeline.class.isAssignableFrom(test), name + " extends RadioTimeline");
        }
    }
}
