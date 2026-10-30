package vanguard.game;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/**
 * How long the start takes until the first frame: the time from the process start to {@code create()}, then each part
 * of {@code create()} by name, then the first frame, logged as one {@code startup} line (the time to the title screen).
 */
final class StartupTimes {
    private final StringBuilder parts = new StringBuilder();
    private final long created = System.nanoTime();
    private final double launched;
    private long last = created;

    StartupTimes() {
        launched = sinceLaunch();
    }

    /** The seconds since the process started (NaN where the platform does not tell). */
    static double sinceLaunch() {
        return ProcessHandle.current()
                .info()
                .startInstant()
                .map(start -> Duration.between(start, Instant.now()).toNanos() / 1e9)
                .orElse(Double.NaN);
    }

    /** Ends the part named {@code part}, which started at the last mark (or at {@code create()}). */
    void mark(String part) {
        long now = System.nanoTime();
        parts.append(String.format(Locale.ROOT, ", %s %.2f", part, (now - last) / 1e9));
        last = now;
    }

    /** The line to log once the first frame is drawn: the launch to {@code create()}, the parts, the total. */
    String report() {
        long now = System.nanoTime();
        return String.format(
                Locale.ROOT,
                "launch %.2f s%s, first frame %.2f; title after %.2f s",
                launched,
                parts,
                (now - last) / 1e9,
                launched + (now - created) / 1e9);
    }
}
