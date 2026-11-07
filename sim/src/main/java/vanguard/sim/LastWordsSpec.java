package vanguard.sim;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, Death; m11f-gaps.md §1 #5, §4): a boss that dies on its
 * pilot's last line ({@link LastWords}): at K (its vital part at 0) it stops firing, its bar goes and the enemy bullets
 * on the screen fade out over {@code bulletsFadeSeconds} (harmless from K); it hangs {@code seconds} where it is, then
 * bursts (its death chain and credit shower). The level's {@code hold_after_boss} counts from K.
 *
 * @param seconds from K to the burst, s (4.5: Vorne's last line)
 * @param bulletsFadeSeconds the bullets' fade from K, s (0.5)
 */
public record LastWordsSpec(double seconds, double bulletsFadeSeconds) {
    public LastWordsSpec {
        if (!(seconds >= 0) || !(bulletsFadeSeconds >= 0)) {
            throw new IllegalArgumentException(
                    "last words last 0 s or more, the bullets' fade too: " + seconds + ", " + bulletsFadeSeconds);
        }
    }
}
