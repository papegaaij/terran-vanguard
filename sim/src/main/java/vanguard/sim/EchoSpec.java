package vanguard.sim;

/**
 * M11 part G, step G1 (design/campaign Level 49, the echoes; design/enemies/bosses, Boss rush; m11g-gaps.md §1 #3,
 * #4): a boss rush's remembered boss, a variant of its original's script ({@link BossSpec#echo()}), run by its {@link
 * EchoDissolve}. It <b>forms</b> as it arrives ({@link SimEvents.Type#ECHO_FORM}): a descending echo by its {@link
 * BossSpec.Rise rise} from its own station (in place, untouchable until formed); an anchored arena echo (the Iron
 * Sovereign's) holds untouchable and silent {@code formSeconds} from its arrival on its halted gate before it engages.
 * At its death chain's end ({@link BossSpec#deathSeconds()}) it <b>dissolves</b> ({@link SimEvents.Type#ECHO_DISSOLVE}):
 * every unit a boss launched still alive goes with it, unpaid and uncounted ({@link SimEvents.Type#ECHO_UNIT_DISSOLVE}
 * each), and every enemy projectile on the screen fades out over {@code fadeSeconds} (F1b's fade: harmless at once,
 * freed at its end), so each gap starts clean.
 *
 * @param formSeconds an anchored echo's forming from its arrival, s (0: none; a descending echo forms by its rise)
 * @param dissolveSeconds the dissolve's look, s (the violet motes; the presentation's)
 * @param fadeSeconds the projectiles' fade from the dissolve, s
 */
public record EchoSpec(double formSeconds, double dissolveSeconds, double fadeSeconds) {
    /** F1b's fade of the enemy projectiles, s (the dissolve's default). */
    public static final double FADE_SECONDS = 0.5;

    public EchoSpec {
        if (!(formSeconds >= 0) || !(dissolveSeconds >= 0) || !(fadeSeconds > 0)) {
            throw new IllegalArgumentException("an echo forms and dissolves in 0 s or more, its projectiles fading: "
                    + formSeconds + ", " + dissolveSeconds + ", " + fadeSeconds);
        }
    }

    /** An echo whose projectiles fade over {@link #FADE_SECONDS}. */
    public EchoSpec(double formSeconds, double dissolveSeconds) {
        this(formSeconds, dissolveSeconds, FADE_SECONDS);
    }
}
