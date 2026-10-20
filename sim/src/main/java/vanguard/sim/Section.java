package vanguard.sim;

/**
 * A stretch of the scroll ending at {@code end} seconds, scrolling at {@code speed} px/s. In a
 * boss {@code arena} the level clock halts at the section's end while the boss lives, and jumps
 * to that end when the boss dies earlier (design/enemies/bosses, the arena).
 *
 * <p>M5 part G (design/campaign Level 13, The halt): a section of speed 0 without an arena is a
 * <em>timed halt</em>: the level clock runs on (one step per step, so the waves keep their times),
 * the scroll eases from the section before's speed to 0 over that section's last {@code
 * rampSeconds} and back up to the next section's speed over its first {@code rampSeconds}
 * (smoothstep, on the level clock); the {@code ramp} is read only on a timed halt.
 *
 * <p>M7 part B (design/campaign Level 15: "each change eased over 1 s"): a moving section with an
 * {@code easeSeconds} above 0 that follows a moving section (not an arena, not a timed halt) eases
 * from that section's speed to its own over its first {@code easeSeconds} (smoothstep, on the level
 * clock: step k of n runs at the old speed + (new − old) × smoothstep((k + 1) ÷ n)); 0, the default,
 * changes the speed at once, as before.
 *
 * <p>M7 part F (design/campaign Level 19, the wreck crawl): a moving section right after an arena with an {@code
 * easeSeconds} above 0 eases up from a halt to its speed over its first {@code easeSeconds} (smoothstep, on the
 * real steps from the clock's resumption) instead of the arena's 1 s ramp. An arena's {@code
 * holdAfterBossSeconds} above 0 keeps the level clock halted after its boss dies until its death chain ({@link
 * BossSpec#deathSeconds()}) and then this long have passed (0: the clock goes on at once, as before).
 *
 * <p>M7 part G (design/campaign Level 20, Reverse scroll): a moving section's {@code speed} is signed: below 0 the
 * scroll runs reversed (the ground moves up the screen, the ship flies backwards toward the bottom); an ease runs
 * through zero as any other (Level 20's turnabout, +140 → −120 over 4 s). An arena never runs reversed; a timed halt
 * is 0.
 */
public record Section(
        double end, double speed, boolean arena, double rampSeconds, double easeSeconds, double holdAfterBossSeconds) {
    /** M5 part G: a timed halt's eases when its data gives no {@code ramp}, s. */
    public static final double TIMED_HALT_RAMP_SECONDS = 2;

    public Section {
        if (arena && speed < 0) {
            throw new IllegalArgumentException("an arena's scroll does not run reversed: " + speed);
        }
        if (!(rampSeconds >= 0)) {
            throw new IllegalArgumentException("a section's ramp is 0 s or more: " + rampSeconds);
        }
        if (!(easeSeconds >= 0) || (easeSeconds > 0 && (speed == 0 || arena))) {
            throw new IllegalArgumentException("only a moving section eases in, over 0 s or more: " + easeSeconds);
        }
        if (!(holdAfterBossSeconds >= 0) || (holdAfterBossSeconds > 0 && !arena)) {
            throw new IllegalArgumentException(
                    "only an arena holds the clock after its boss, 0 s or more: " + holdAfterBossSeconds);
        }
    }

    /** A section without M7 part F's hold after the boss. */
    public Section(double end, double speed, boolean arena, double rampSeconds, double easeSeconds) {
        this(end, speed, arena, rampSeconds, easeSeconds, 0);
    }

    /** A section that changes its speed at once (without M7 part B's ease). */
    public Section(double end, double speed, boolean arena, double rampSeconds) {
        this(end, speed, arena, rampSeconds, 0);
    }

    /** A section with the timed halt's default ramp ({@link #TIMED_HALT_RAMP_SECONDS}, read only on one). */
    public Section(double end, double speed, boolean arena) {
        this(end, speed, arena, TIMED_HALT_RAMP_SECONDS);
    }

    public Section(double end, double speed) {
        this(end, speed, false);
    }

    /** M7 part G: whether its scroll runs reversed (its speed is below 0). */
    public boolean reversed() {
        return speed < 0;
    }

    /** M5 part G: whether it is a timed halt (speed 0 without an arena). */
    public boolean timedHalt() {
        return speed == 0 && !arena;
    }
}
