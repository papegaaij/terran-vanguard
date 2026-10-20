package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;

/**
 * The debrief waits for the radio after a won level, at most {@link LevelData#OUTRO_SECONDS} unless a
 * closing line has not been heard in full by then.
 */
class OutroTest {
    private static final float FRAME = 1 / 60f;
    /** Level 01's level-end line: 3 lines, 60 characters with the line breaks, 2 s at 30 per second. */
    private static final String LEVEL_END = "Good work, Aegis. That was the scouts. The rest are coming.";

    private static final String SECONDARY = "Clean sweep. I'll make sure High Command hears about it.";

    /** A voice file for a spoken line (the queue only carries its path). */
    private static final java.util.Optional<String> VOICE = java.util.Optional.of("voice/test.ogg");

    private final RadioQueue radio = new RadioQueue();
    private final Outro outro = new Outro();

    @Test
    void nothingHappensWhileTheLevelIsFlown() {
        assertFalse(outro.update(FRAME, radio));
    }

    @Test
    void theDebriefWaitsUntilTheLevelEndLineHasBeenShown() {
        outro.start();
        radio.add("Okafor", "neutral", LEVEL_END, false);

        assertEquals(2 + RadioQueue.LAST_PAGE_SECONDS, secondsUntilDebrief(), 2 * FRAME);
    }

    @Test
    void aQueuedLineIsShownToo() {
        radio.add("Okafor", "neutral", SECONDARY, false);
        radio.update(FRAME);
        run(5);
        outro.start();
        radio.add("Okafor", "neutral", LEVEL_END, false);

        // What is left of the secondary-objective line (57 characters typed in 1.9 s, then held),
        // the gap and the level-end line.
        double left = 1.9 + RadioQueue.LAST_PAGE_SECONDS - 5;
        assertEquals(left + RadioQueue.GAP_SECONDS + 2 + RadioQueue.LAST_PAGE_SECONDS, secondsUntilDebrief(), 0.1);
    }

    @Test
    void bothEndOfLevelLinesFitInTheCap() {
        outro.start();
        radio.add("Okafor", "neutral", LEVEL_END, false);
        radio.add("Okafor", "neutral", SECONDARY, false);

        assertTrue(secondsUntilDebrief() < LevelData.OUTRO_SECONDS, "the cap leaves room for both lines");
    }

    @Test
    void theRadioHoldsTheDebriefAtMostTheCap() {
        outro.start();
        radio.add("Okafor", "neutral", LEVEL_END, false);
        radio.add("Okafor", "neutral", SECONDARY, false);
        radio.add("Okafor", "neutral", LEVEL_END, false);

        assertEquals(LevelData.OUTRO_SECONDS, secondsUntilDebrief(), 2 * FRAME);
    }

    /**
     * The user's playtest of 2026-10-19 (Level 15): a bark of Rook's on the radio as the level ends, then
     * the met secondary's line and the level-end line. The bark closes once its voice has ended, and the
     * debrief waits past the cap until the level-end line has been heard in full.
     */
    @Test
    void theDebriefWaitsPastTheCapUntilTheClosingLinesAreHeard() {
        radio.add("Rook", "Rook", "fierce", "Ha! Save some for me!", false, RadioQueue.Priority.EVENT, VOICE, 2.3f);
        assertEquals(RadioQueue.Change.OPENED, radio.update(FRAME));
        run(0.5);
        outro.start();
        radio.outro();
        radio.add(
                "Ares Control",
                "generic-colonial",
                "neutral",
                SECONDARY,
                false,
                RadioQueue.Priority.CLOSING,
                VOICE,
                3.4f);
        RadioQueue.Message levelEnd =
                radio.add("Okafor", "Okafor", "neutral", LEVEL_END, false, RadioQueue.Priority.CLOSING, VOICE, 8f);

        double seconds = 0;
        double barkClosed = Double.NaN;
        double levelEndOpened = Double.NaN;
        while (!outro.update(FRAME, radio)) {
            RadioQueue.Change change = radio.update(FRAME);
            seconds += FRAME;
            if (change == RadioQueue.Change.CLOSED && Double.isNaN(barkClosed)) {
                barkClosed = seconds;
            }
            if (change == RadioQueue.Change.OPENED && radio.current().orElseThrow() == levelEnd) {
                levelEndOpened = seconds;
            }
        }
        // The bark's voice ends 2.3 s after it opened, 0.5 s before the level's end; its text time is longer.
        assertEquals(1.8, barkClosed, 2 * FRAME);
        // The secondary's line (1.9 s typed, its last page held 5 s), the gaps, the level-end voice.
        double heard = 1.8 + RadioQueue.GAP_SECONDS + 1.9 + RadioQueue.LAST_PAGE_SECONDS + RadioQueue.GAP_SECONDS + 8;
        assertEquals(heard - 8, levelEndOpened, 0.1);
        assertTrue(heard > LevelData.OUTRO_SECONDS, "the closing lines run past the cap");
        assertEquals(heard, seconds, 0.1);
    }

    @Test
    void inTheOutroNoEventLineQueues() {
        outro.start();
        radio.outro();
        radio.add("Rook", "Rook", "fierce", "Ha! Save some for me!", false, RadioQueue.Priority.EVENT, VOICE, 2.3f);

        assertTrue(radio.idle());
        assertTrue(outro.update(FRAME, radio));
    }

    @Test
    void withAQuietRadioTheDebriefFollowsAtOnce() {
        outro.start();

        assertTrue(outro.update(FRAME, radio));
    }

    @Test
    void aRestartStopsIt() {
        outro.start();
        outro.stop();

        assertFalse(outro.update(FRAME, radio));
    }

    /** Runs frames in the level screen's order (the outro, then the radio) until the debrief takes over. */
    private double secondsUntilDebrief() {
        double seconds = 0;
        while (!outro.update(FRAME, radio)) {
            radio.update(FRAME);
            seconds += FRAME;
        }
        return seconds;
    }

    private void run(double seconds) {
        for (double t = 0; t < seconds; t += FRAME) {
            radio.update(FRAME);
        }
    }
}
