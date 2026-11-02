package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.sim.InputRecording;

/** Step m10-f6r: the {@code --replay} debug option hands out a recording's commands a step at a time. */
class ReplayCommandsTest {
    @Test
    void handsOutTheRecordedStepsThenNone() {
        var replay = new ReplayCommands(new InputRecording(2185, new int[] {3, 3, 16}));
        assertEquals(3, replay.steps());
        assertEquals(3, replay.next());
        assertFalse(replay.ranOut());
        assertEquals(3, replay.next());
        assertEquals(16, replay.next());
        assertTrue(replay.ranOut(), "reported once the last step is handed out");
        assertFalse(replay.ranOut(), "only once");
        assertEquals(0, replay.next(), "no commands after the recording");
        assertEquals(0, replay.next());
    }
}
