package vanguard.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** RF6, the {@code --shots} debug option: each shot is taken once, at the first frame at or past its tick. */
class ShotRunTest {
    @Test
    void takesEachShotOnceAtTheFirstFrameAtOrPastItsTick() {
        var run = new ShotRun(5, List.of(10L, 20L, 21L));
        assertNull(run.due(-1), "not in a level yet");
        assertNull(run.due(9));
        assertEquals("shot-05-10.png", run.due(10));
        assertNull(run.due(10), "taken");
        assertFalse(run.done());
        // A slow motion skips a tick: the late frame takes the shot, and one frame covers both ticks it passed.
        assertEquals("shot-05-20.png", run.due(22));
        assertTrue(run.done());
        assertNull(run.due(30));
    }
}
