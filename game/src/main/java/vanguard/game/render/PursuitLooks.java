package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import vanguard.sim.Flock;
import vanguard.sim.Pursuit;

/**
 * M7 part G, the pursuit swarm's lunge tell (design/campaign Level 20, The pursuit swarm: "the flock contracts into a
 * tight ball ≈ 60 px across, brightens, a rising hum"): the contraction is the simulation's; the look is each member's
 * own frame drawn again additively in a warm white over its body, rising through the tell to {@link #TELL_GLOW} and
 * fading out along the dive, so the ball flares just before it drops and the streak cools as it goes. Nothing in the
 * band, the climb or the break-off. The rising hum is {@code FlightSounds}' (no pop). Presentation only.
 */
public final class PursuitLooks {
    /** The glow's strength at the end of the tell (additive alpha over the member's frame). */
    static final float TELL_GLOW = 0.85f;
    /** The glow's tint: a warm white (the Vrell lime would vanish into the ochre ground). */
    static final Color TINT = new Color(1f, 0.94f, 0.78f, 1f);

    /**
     * Step GFX2 (the round 43 capture: the motes read as tiny brown specks on the tan basin): a pursuing mote's rim
     * light, a pale teal halo (the ochre ground's opposite) of this diameter and opacity round each member, additive,
     * with its body lit by the same teal at {@link #RIM_BODY}; it gives way to the tell's warm glow as that rises.
     */
    static final double RIM_DIAMETER = 22;

    static final float RIM_ALPHA = 0.45f;
    static final float RIM_BODY = 0.4f;
    static final Color RIM = new Color(0.6f, 1f, 0.92f, 1f);
    /**
     * Step GFX2: the tell's glow round each member at play size: a reddish-white disc growing from {@link
     * #TELL_DISC_MIN} to {@link #TELL_DISC_MAX} px with the glow, up to {@link #TELL_DISC_ALPHA} at its peak (on top
     * of the member's frame drawn again in {@link #TINT}; the ball's discs overlap, so it stays below a white-out).
     */
    static final double TELL_DISC_MIN = 18;

    static final double TELL_DISC_MAX = 30;
    static final float TELL_DISC_ALPHA = 0.45f;
    static final Color TELL_DISC = new Color(1f, 0.78f, 0.66f, 1f);
    /**
     * Step GFX2: one short ring round the contracting ball through the tell's second part, widening from {@link
     * #RING_START} to {@link #RING_END} px (its middle line's radius) and fading out by the dive.
     */
    static final double RING_FROM = 0.4;

    static final double RING_START = 22;
    static final double RING_END = 64;
    static final float RING_ALPHA = 0.9f;
    /**
     * Step GFX2: a pursuing mote's drop shadow at this share of the flyers' (the capture: at 50 % the shadows of the
     * swarm read as a second flock on the sand; a cloud of embers that high casts only a faint one).
     */
    static final float SHADOW_SHARE = 0.3f;

    private PursuitLooks() {}

    /** Step GFX2: the rim light's share for a member at tell glow {@code glow}: full, gone at the tell's peak. */
    static float rim(float glow) {
        return Math.clamp(1 - glow / TELL_GLOW, 0, 1);
    }

    /** Step GFX2: the tell disc's opacity at glow {@code glow}. */
    static float tellDiscAlpha(float glow) {
        return TELL_DISC_ALPHA * Math.clamp(glow / TELL_GLOW, 0, 1);
    }

    /** Step GFX2: the tell disc's diameter at glow {@code glow}, px. */
    static double tellDisc(float glow) {
        return TELL_DISC_MIN + (TELL_DISC_MAX - TELL_DISC_MIN) * Math.clamp(glow / TELL_GLOW, 0, 1);
    }

    /**
     * Step GFX2: the tell ring's opacity in {@code phase} at {@code progress}: none before {@link #RING_FROM} of the
     * tell, then full and fading out to its end; none in any other phase.
     */
    static float ringAlpha(Pursuit.Phase phase, double progress) {
        if (phase != Pursuit.Phase.TELL || progress < RING_FROM) {
            return 0;
        }
        double p = Math.clamp((progress - RING_FROM) / (1 - RING_FROM), 0, 1);
        return (float) (RING_ALPHA * (1 - p));
    }

    /** Step GFX2: the tell ring's radius at {@code progress} of the tell, px. */
    static double ringRadius(double progress) {
        double p = Math.clamp((progress - RING_FROM) / (1 - RING_FROM), 0, 1);
        return RING_START + (RING_END - RING_START) * Math.sqrt(p);
    }

    /** Step GFX2: whether a member of {@code flock} wears the pursuit's look (rim light, faint shadow). */
    static boolean pursuing(Flock flock) {
        return flock != null && flock.pursuing();
    }

    /**
     * The glow over a pursuit's members, 0 to {@link #TELL_GLOW}: rising (eased in and out) through the tell,
     * falling back along the dive, none in any other phase.
     */
    public static float glow(Pursuit.Phase phase, double progress) {
        double p = Math.clamp(progress, 0, 1);
        return switch (phase) {
            case TELL -> (float) (TELL_GLOW * p * p * (3 - 2 * p));
            case DIVE -> (float) (TELL_GLOW * (1 - p));
            case BAND, CLIMB, BREAK -> 0;
        };
    }

    /** The glow over a member of {@code flock}: its pursuit's, 0 for a flock that does not pursue (or none). */
    static float glow(Flock flock) {
        if (flock == null || !flock.pursuing()) {
            return 0;
        }
        Pursuit pursuit = flock.pursuit().orElse(null);
        return pursuit == null ? 0 : glow(pursuit.phase(), pursuit.progress());
    }
}
