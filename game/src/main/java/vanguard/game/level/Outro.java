package vanguard.game.level;

import vanguard.content.LevelData;

/**
 * The outro after a won level (design/ui/hud, Radio): the scroll runs on under the radio, and the
 * debrief takes over once the radio has shown its last message (the level-end line and whatever
 * is still queued), at the latest {@link LevelData#OUTRO_SECONDS} after the level's end, unless a
 * closing line (the level-end line, a met secondary's) has not been heard in full by then: the outro
 * waits for its voice ({@link RadioQueue#closingLeft}). The user's playtest of 2026-10-19: with a bark
 * of Rook's on the radio as Level 15 ended, its secondary's line and level-end line no longer fitted
 * in the 15 s and the debrief cut the level-end line short.
 */
public final class Outro {
    /** Seconds since the level's end; negative while the level is flown. */
    private double elapsed = -1;

    /** The level is won. */
    public void start() {
        elapsed = 0;
    }

    /** Whether the level is won and the outro runs. */
    public boolean running() {
        return elapsed >= 0;
    }

    /** The level starts over. */
    public void stop() {
        elapsed = -1;
    }

    /**
     * Advances the outro by a frame.
     *
     * @return whether the debrief takes over now
     */
    public boolean update(float seconds, RadioQueue radio) {
        if (elapsed < 0) {
            return false;
        }
        elapsed += seconds;
        return radio.idle() || (elapsed >= LevelData.OUTRO_SECONDS && !radio.closingLeft());
    }
}
