package vanguard.sim;

/**
 * M11 part F, step F1 (design/enemies/bosses/vornes-chimera, Death; m11f-gaps.md §1 #5, §4): a boss's death on its
 * pilot's last line ({@link LastWordsSpec}), kept by its {@link SetPiece} behind its one accessor ({@link
 * SetPiece#lastWords()}) in the boss-end slot beside {@link EscapeRun} (a boss has one or the other), made only for a boss
 * with it. K is its vital part's death: the sortie wrecks and pays its other parts, counts the kill, raises {@link
 * SimEvents.Type#LAST_WORDS} (the bar goes) and {@link SimEvents.Type#BULLETS_FADE} (every enemy projectile on the screen
 * fades out over the spec's fade, harmless from K, freed at its end: bullets, F1b missiles and lobs), cues its {@code boss-phase} subject {@link #SUBJECT}
 * (the last line) and starts the level's hold after its boss from K. From K it fires nothing, takes no hit and touches
 * nothing; it hangs where it is (no sway, no sweep). At K + its seconds it bursts: {@link SimEvents.Type#LAST_BURST},
 * then the death as at any boss's ({@link SimEvents.Type#BOSS_DESTROYED} with its credit shower, the death chain, the
 * {@code boss-destroyed} cue), paying nothing more. Its time from the arrival is K's. Its state is hashed with its
 * piece's; the boss checkpoint and a retry start it afresh. Allocates nothing once made.
 */
public final class LastWords {
    /** The radio subject at K (the boss's last line). */
    public static final String SUBJECT = "last-words";

    private final LastWordsSpec spec;
    private final int burstTicks;
    private final int fadeTicks;

    /** Steps since K; -1 before it. */
    private int since;
    /** The boss's credit shower, set at K. */
    private int credits;
    /** Whether the sortie burst it ({@link #burst()}). */
    private boolean burst;

    LastWords(LastWordsSpec spec) {
        this.spec = spec;
        burstTicks = SimStep.ticks(spec.seconds());
        fadeTicks = SimStep.ticks(spec.bulletsFadeSeconds());
        reset();
    }

    /** Back to the level start (and the boss checkpoint): not spoken. */
    void reset() {
        since = -1;
        credits = 0;
        burst = false;
    }

    /** K: its vital part died; its shower is {@code showerCredits}. Returns whether it was not spoken yet. */
    boolean speak(int showerCredits) {
        if (since >= 0) {
            return false;
        }
        since = 0;
        credits = showerCredits;
        return true;
    }

    /** One step of the boss from K on. */
    void step() {
        if (since >= 0 && !burst) {
            since++;
        }
    }

    // --- the sortie's view ----------------------------------------------------------------------------

    /** Whether the bullets' fade ends now (the sortie frees the fading bullets). */
    boolean fadeEndsNow() {
        return since == Math.max(1, fadeTicks) && !burst;
    }

    /** Whether it bursts now (the sortie destroys it). */
    boolean burstNow() {
        return since >= Math.max(1, burstTicks) && !burst;
    }

    /** The sortie burst it (once). */
    void burst() {
        burst = true;
    }

    // --- the presentation's view ----------------------------------------------------------------------

    public LastWordsSpec spec() {
        return spec;
    }

    /** Whether K has passed (the bar is gone, it hangs). */
    public boolean spoken() {
        return since >= 0;
    }

    /** Whether it has burst. */
    public boolean burstDone() {
        return burst;
    }

    /** Seconds since K; negative before. */
    public double spokenSeconds() {
        return since < 0 ? -1 : since * SimStep.SECONDS;
    }

    /** The fading bullets' opacity now, 1 to 0 over the fade from K; 1 before K, 0 after the fade. */
    public double bulletAlpha() {
        if (since < 0) {
            return 1;
        }
        return fadeTicks == 0 ? 0 : Math.max(0, 1 - (double) since / fadeTicks);
    }

    /** The credit shower at its burst, set at K. */
    public int credits() {
        return credits;
    }

    void addTo(StateHash hash) {
        hash.add(since).add(credits).add(burst ? 1 : 0);
    }
}
