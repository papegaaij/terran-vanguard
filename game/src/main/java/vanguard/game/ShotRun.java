package vanguard.game;

import java.util.List;
import java.util.Locale;

/**
 * RF6, the {@code --shots} debug option (a pixel pin for the renderer): the level start runs the simulation one step a
 * frame, and at each of the level ticks the frame is written as {@code shot-<level>-<tick>.png}; after the last the game
 * exits. The first frame whose level tick has reached a shot's tick takes it (a slow motion steps every other frame).
 */
final class ShotRun {
    private final int level;
    private final List<Long> ticks;
    private int next;

    /** @param ticks the level ticks, ascending */
    ShotRun(int level, List<Long> ticks) {
        this.level = level;
        this.ticks = List.copyOf(ticks);
    }

    /** The file name of the shot due at {@code tick} (the level's tick now, -1 outside a level), or null for none. */
    String due(long tick) {
        if (next >= ticks.size() || tick < ticks.get(next)) {
            return null;
        }
        long shot = ticks.get(next);
        while (next < ticks.size() && ticks.get(next) <= tick) {
            next++;
        }
        return String.format(Locale.ROOT, "shot-%02d-%d.png", level, shot);
    }

    /** Whether every shot is taken. */
    boolean done() {
        return next >= ticks.size();
    }
}
