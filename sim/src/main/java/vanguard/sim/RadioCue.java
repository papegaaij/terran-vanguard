package vanguard.sim;

/**
 * A radio chatter line and what triggers it.
 *
 * @param t seconds from the level start, for {@link CueTrigger#TIME}
 * @param subject the enemy slug of a first kill, the secret's name or the group's name; empty otherwise
 * @param expression the speaker's portrait expression ({@code neutral}, {@code grim}, {@code fierce});
 *     presentation only, nothing in the simulation reads it
 * @param portrait the portrait's speaker when it is not the speaker's own (a generic one);
 *     presentation only
 * @param requiresSpecial it starts only with a special fitted
 * @param alliesMin a level-end cue starts only with at least this many convoy units home
 * @param alliesMax ... and at most this many
 * @param requires part G: it starts only with all of these fitted, as {@link #FITTED_SPECIAL}
 *     and {@link #FITTED_HOMING} bits (with {@code requiresSpecial}, {@link #FITTED_SPECIAL} is set);
 *     M5 part C: {@link #FITTED_ESCORT}, it starts only while an escort flies (Rook's scripted lines)
 * @param requiresNot part G: it starts only with none of these fitted (Level 07's line for a
 *     ship without a homing weapon)
 */
public record RadioCue(
        CueTrigger trigger,
        double t,
        String subject,
        String speaker,
        String line,
        boolean distorted,
        String expression,
        String portrait,
        boolean requiresSpecial,
        int alliesMin,
        int alliesMax,
        int requires,
        int requiresNot) {
    /** What is fitted: a special. */
    public static final int FITTED_SPECIAL = 1;
    /** What is fitted: a weapon with homing delivery. */
    public static final int FITTED_HOMING = 2;
    /**
     * M5 part C: an escort flies (hired, fitted and not ejected; Rook): it changes during the
     * attempt, so the radio asks for it when a cue is due.
     */
    public static final int FITTED_ESCORT = 4;

    public RadioCue {
        requires |= requiresSpecial ? FITTED_SPECIAL : 0;
        requiresSpecial = (requires & FITTED_SPECIAL) != 0;
        if ((requires & requiresNot) != 0) {
            throw new IllegalArgumentException("a radio cue cannot require what it requires not to be fitted");
        }
    }

    /** Without part G's requirements beyond a special. */
    public RadioCue(
            CueTrigger trigger,
            double t,
            String subject,
            String speaker,
            String line,
            boolean distorted,
            String expression,
            String portrait,
            boolean requiresSpecial,
            int alliesMin,
            int alliesMax) {
        this(
                trigger,
                t,
                subject,
                speaker,
                line,
                distorted,
                expression,
                portrait,
                requiresSpecial,
                alliesMin,
                alliesMax,
                0,
                0);
    }

    /** Whether it may start with {@code fitted} ({@link #FITTED_SPECIAL}, {@link #FITTED_HOMING} bits) on the ship. */
    public boolean allowedWith(int fitted) {
        return (fitted & requires) == requires && (fitted & requiresNot) == 0;
    }

    public RadioCue(
            CueTrigger trigger,
            double t,
            String subject,
            String speaker,
            String line,
            boolean distorted,
            String expression,
            String portrait) {
        this(trigger, t, subject, speaker, line, distorted, expression, portrait, false, 0, Integer.MAX_VALUE);
    }

    public RadioCue(
            CueTrigger trigger,
            double t,
            String subject,
            String speaker,
            String line,
            boolean distorted,
            String expression) {
        this(trigger, t, subject, speaker, line, distorted, expression, speaker);
    }
}
