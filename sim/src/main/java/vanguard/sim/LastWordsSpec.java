package vanguard.sim;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, Death; m11f-gaps.md §1 #5, §4): a boss that dies on its
 * pilot's last line ({@link LastWords}): at K (its vital part at 0) it stops firing, its bar goes and the enemy bullets
 * on the screen fade out over {@code bulletsFadeSeconds} (harmless from K); it hangs {@code seconds} where it is, then
 * bursts (its death chain and credit shower). The level's {@code hold_after_boss} counts from K.
 *
 * <p>M11 part H, step H1b (design/enemies/bosses/choir-heart, Death; m11h-gaps.md §1 #10): with {@code seconds} 0 it
 * bursts at once (the fade without a hang: the Heart's chain starts at K) and the bullets' fade still runs to its end;
 * with {@code flashSeconds} at 0 or more the act boss's screen flash comes that long after K ({@link
 * SimEvents.Type#BOSS_FLASH}: the Heart's K + 3.5 s) instead of at the chain's start.
 *
 * @param seconds from K to the burst, s (4.5: Vorne's last line; 0: the Heart's)
 * @param bulletsFadeSeconds the bullets' fade from K, s (0.5)
 * @param flashSeconds M11 part H, step H1b: from K to the screen flash, s; negative: none of its own (the flash at
 *     the chain's start, every other boss)
 */
public record LastWordsSpec(double seconds, double bulletsFadeSeconds, double flashSeconds) {
    public LastWordsSpec {
        if (!(seconds >= 0) || !(bulletsFadeSeconds >= 0) || Double.isNaN(flashSeconds)) {
            throw new IllegalArgumentException(
                    "last words last 0 s or more, the bullets' fade too: " + seconds + ", " + bulletsFadeSeconds);
        }
    }

    /** Last words without a flash of their own (the flash at the chain's start). */
    public LastWordsSpec(double seconds, double bulletsFadeSeconds) {
        this(seconds, bulletsFadeSeconds, -1);
    }

    /** M11 part H, step H1b: these last words with the screen flash {@code seconds} after K ({@link #flashSeconds}). */
    public LastWordsSpec withFlashAt(double seconds) {
        return new LastWordsSpec(this.seconds, bulletsFadeSeconds, seconds);
    }

    /** M11 part H, step H1b: whether the flash comes at its own time after K. */
    public boolean flashes() {
        return flashSeconds >= 0;
    }
}
