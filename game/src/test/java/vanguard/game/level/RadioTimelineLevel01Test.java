package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** {@link RadioTimeline}'s checks for Level 01. */
class RadioTimelineLevel01Test extends RadioTimeline {
    RadioTimelineLevel01Test() {
        super(LEVEL_01);
    }

    /**
     * Every level with runs has its subclass, {@code RadioTimelineLevelNNTest}, so a level added to
     * {@link #RUNS} is not left out.
     */
    @Test
    void everyLevelWithRunsHasItsTestClass() {
        for (String path : RUNS.keySet()) {
            Matcher number = Pattern.compile("/level-(\\d\\d)-").matcher(path);
            assertTrue(number.find(), path);
            String name = RadioTimeline.class.getPackageName() + ".RadioTimelineLevel" + number.group(1) + "Test";
            try {
                assertTrue(RadioTimeline.class.isAssignableFrom(Class.forName(name)), name + " extends RadioTimeline");
            } catch (ClassNotFoundException e) {
                throw new AssertionError(path + " has runs but no test class " + name, e);
            }
        }
    }
}
