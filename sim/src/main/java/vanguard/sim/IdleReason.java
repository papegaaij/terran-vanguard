package vanguard.sim;

/**
 * Why a mount fires nothing in this level ({@link Sortie#mountIdleReason}): the HUD's word on its row ({@link #word()});
 * the mount still draws its power.
 */
public enum IdleReason {
    /** It fires. */
    NONE(""),
    /** M5 part E: a torpedo-type weapon where there is no water. */
    NO_WATER("NO WATER"),
    /** M9 part C (design/campaign Level 30, {@code ground: none}): a bomb delivery where there is no ground layer. */
    NO_GROUND("NO GROUND");

    private final String word;

    IdleReason(String word) {
        this.word = word;
    }

    /** The HUD's word; empty for {@link #NONE}. */
    public String word() {
        return word;
    }
}
