package vanguard.sim;

/**
 * M9 part E (design/campaign Level 32, The forks; route choice Q5 a): the branch of a level's fork a wave, a ground
 * unit or a ground object belongs to: fork {@code fork} (from 0, in the order of {@link
 * GroundSpec.TerrainSpec#forks()}), its {@code side} channel ({@link #LEFT} or {@link #RIGHT} of the fork's island).
 * The chosen side's waves fly and its ground units and objects wake; the other side's waves never come and its ground
 * units and objects stay dormant ({@link Forks}).
 */
public record Branch(int fork, int side) {
    /** The channel left of the fork's island. */
    public static final int LEFT = 0;
    /** The channel right of it. */
    public static final int RIGHT = 1;
    /** The most forks a level has. */
    public static final int MOST_FORKS = 4;

    public Branch {
        if (fork < 0 || fork >= MOST_FORKS || (side != LEFT && side != RIGHT)) {
            throw new IllegalArgumentException("a branch is a fork's left or right channel: " + fork + ", " + side);
        }
    }

    /** Its code, fork × 2 + side ({@link Forks#code}). */
    public int code() {
        return Forks.code(fork, side);
    }

    /**
     * The subject of its radio cue ({@link CueTrigger#BRANCH}): the fork counted from 1, a colon and {@code left} or
     * {@code right} ({@code "1:right"}).
     */
    public String subject() {
        return Forks.subject(fork, side);
    }
}
