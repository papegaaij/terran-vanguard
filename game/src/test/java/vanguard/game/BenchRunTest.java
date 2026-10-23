package vanguard.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BenchRunTest {
    @Test
    void endsOnceTheTimeIsUpAndReportsTheFrames() {
        var run = new BenchRun(0.5);

        assertFalse(run.frame(0.125f));
        assertFalse(run.frame(0.25f));
        assertTrue(run.frame(0.125f));

        assertEquals(
                "3 frames in 0.5 s (6 fps), longest frame 250.0 ms at 0.4 s; 3 frames ≥ 50 ms: 125.0 ms at 0.1 s, 250.0 ms at 0.4 s, 125.0 ms at 0.5 s",
                run.report());
    }

    @Test
    void aSmoothRunListsNoSlowFrames() {
        var run = new BenchRun(0.03);

        run.frame(0.02f);
        run.frame(0.01f);

        assertEquals("2 frames in 0.0 s (67 fps), longest frame 20.0 ms at 0.0 s", run.report());
    }
}
