package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Arrays;
import vanguard.sim.SimStep;

/**
 * Step GFX2 (the round 43 capture: Level 20's rim flak read as small dark smudges in the top band): a militia flak
 * burst ({@link MilitiaFlak}) opens with a bright flash core over its puff, additive: a warm white core shrinking and
 * fading over {@value #SECONDS} s inside a wider orange bloom, as the escort frigate's flak reads by its flash over the
 * sea. Presentation only, on the step count; a restart clears it. Nothing is allocated per frame.
 */
final class FlakFlashes {
    /** How long a burst's flash shows, s. */
    static final double SECONDS = 0.25;
    /** The core's diameter at the flash's start and end, px. */
    static final double CORE_START = 20;

    static final double CORE_END = 8;
    /** The bloom's diameter, px, and its opacity at the start. */
    static final double BLOOM = 46;

    static final float BLOOM_ALPHA = 0.7f;

    static final Color CORE = new Color(1f, 0.97f, 0.85f, 1f);
    static final Color BLOOM_TINT = new Color(1f, 0.6f, 0.2f, 1f);

    /** The bursts flashing at once at most (one every 1.5 s at Level 20: one at a time). */
    private static final int CAPACITY = 4;

    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private final long[] ticks = new long[CAPACITY];
    private int next;

    FlakFlashes() {
        clear();
    }

    /** A burst at the play-field point (x, y) at step {@code tick}. */
    void start(double x, double y, long tick) {
        xs[next] = x;
        ys[next] = y;
        ticks[next] = tick;
        next = (next + 1) % CAPACITY;
    }

    void clear() {
        Arrays.fill(ticks, Long.MIN_VALUE);
    }

    /** The flash's strength {@code seconds} after its burst: 1 at the start, easing out to 0 at {@link #SECONDS}. */
    static float strength(double seconds) {
        if (seconds < 0 || seconds >= SECONDS) {
            return 0;
        }
        double p = 1 - seconds / SECONDS;
        return (float) (p * p);
    }

    /** The core's diameter {@code seconds} after the burst, px. */
    static double coreDiameter(double seconds) {
        double p = Math.clamp(seconds / SECONDS, 0, 1);
        return CORE_START + (CORE_END - CORE_START) * p;
    }

    /** The bursts flashing at step {@code tick} plus {@code alpha}; how many were drawn. */
    int draw(SpriteBatch batch, SoftGlow glow, long tick, float alpha) {
        int drawn = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (ticks[i] == Long.MIN_VALUE) {
                continue;
            }
            double seconds = (tick - ticks[i] + alpha) * SimStep.SECONDS;
            float strength = strength(seconds);
            if (strength <= 0) {
                continue;
            }
            if (drawn == 0) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            }
            drawn++;
            SoftGlow.tint(batch, BLOOM_TINT, BLOOM_ALPHA * strength);
            glow.disc(batch, xs[i], ys[i], BLOOM);
            SoftGlow.tint(batch, CORE, strength);
            glow.disc(batch, xs[i], ys[i], coreDiameter(seconds));
            glow.disc(batch, xs[i], ys[i], coreDiameter(seconds) * 0.6);
        }
        if (drawn > 0) {
            batch.setColor(Color.WHITE);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        return drawn;
    }

    /** The bursts that would flash at step {@code tick} (for the tests). */
    int flashing(long tick) {
        int n = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (ticks[i] != Long.MIN_VALUE && strength((tick - ticks[i]) * SimStep.SECONDS) > 0) {
                n++;
            }
        }
        return n;
    }
}
