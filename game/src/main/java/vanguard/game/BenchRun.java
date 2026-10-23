package vanguard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The {@code --bench} mode: runs for a fixed time, then reports the frames so the game can exit. The report names when
 * the longest frame came and the first few frames of {@value #SLOW_MS} ms or more, so a spike can be traced to its
 * moment in the level.
 */
final class BenchRun {
    static final int SLOW_MS = 50;
    private static final int SLOW_LISTED = 8;

    private final double seconds;
    private final List<String> slow = new ArrayList<>();
    private double elapsed;
    private int frames;
    private int slowFrames;
    private float longestFrame;
    private double longestAt;

    BenchRun(double seconds) {
        this.seconds = seconds;
    }

    /** Counts a frame of {@code frameSeconds}; returns whether the run is over. */
    boolean frame(float frameSeconds) {
        elapsed += frameSeconds;
        frames++;
        if (frameSeconds > longestFrame) {
            longestFrame = frameSeconds;
            longestAt = elapsed;
        }
        if (frameSeconds * 1000 >= SLOW_MS) {
            slowFrames++;
            if (slow.size() < SLOW_LISTED) {
                slow.add(String.format(Locale.ROOT, "%.1f ms at %.1f s", frameSeconds * 1000, elapsed));
            }
        }
        return elapsed >= seconds;
    }

    String report() {
        var report = String.format(
                Locale.ROOT,
                "%d frames in %.1f s (%.0f fps), longest frame %.1f ms at %.1f s",
                frames,
                elapsed,
                frames / elapsed,
                longestFrame * 1000,
                longestAt);
        if (slowFrames == 0) {
            return report;
        }
        return report
                + String.format(Locale.ROOT, "; %d frames ≥ %d ms: ", slowFrames, SLOW_MS)
                + String.join(", ", slow);
    }
}
