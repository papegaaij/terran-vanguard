package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Arrays;
import vanguard.sim.Eruptions;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3b (design/world/jovian, Hazards &amp; set pieces: eruptions, J3; Level 39): Io's dry eruptions in
 * a level with them ({@link Sortie#eruptions()}), on {@link SoftGlow}'s smooth light (no ordered dither, drawn once),
 * stand-ins until Level 39's art:
 *
 * <ul>
 *   <li>on the ground, a <b>telling vent</b>'s mouth glowing orange-white and widening over its tell, a hot ring
 *       spreading round it (the ground cracking);
 *   <li>on the ground, the <b>umbrella</b> of sulphur haze each eruption opens over its vent ({@link #erupted}): it
 *       spreads to {@value #UMBRELLA_TO} px across over {@value #OPEN_SECONDS} s and settles, thinning out by {@value
 *       #UMBRELLA_SECONDS} s, scrolling away with the ground; a rising or falling rock's soft shadow under it;
 *   <li>on the play plane, the <b>rocks</b>: a dark stone body with a burning core and a faint orange halo, growing as
 *       they rise ({@value #SMALL} → 1 of the rock's {@value #ROCK_SIZE} px), full while they fly (shootable),
 *       shrinking as they fall back.
 * </ul>
 *
 * Nothing is allocated per frame.
 */
final class EruptionLooks {
    /** The vent's mouth: this wide at the tell's start, this at its end. */
    static final double MOUTH_FROM = 18;

    static final double MOUTH_TO = 56;
    /** The crack ring spreads to this radius over the tell. */
    static final double CRACKS_TO = 40;
    /** The rock's size (design: 24 × 24 px) and its share at the start of its rise, the end of its fall. */
    static final double ROCK_SIZE = 24;

    static final double SMALL = 0.45;
    /** The umbrella: it opens from this to that across, over this long, and is gone after this long. */
    static final double UMBRELLA_FROM = 30;

    static final double UMBRELLA_TO = 170;
    static final double OPEN_SECONDS = 1.0;
    static final double UMBRELLA_SECONDS = 4.0;
    /** The umbrella's strength at its densest. */
    static final float UMBRELLA_PEAK = 0.5f;
    /** The colours: the mouth's white-hot core and orange glow, the sulphur haze, the stone, its burning core. */
    static final Color HOT = new Color(1, 0.93f, 0.75f, 1);

    static final Color GLOW = new Color(1, 0.52f, 0.12f, 1);
    static final Color SULPHUR = new Color(0.86f, 0.80f, 0.42f, 1);
    static final Color STONE = new Color(0.22f, 0.17f, 0.13f, 1);
    static final Color EMBER = new Color(1, 0.45f, 0.10f, 1);

    private static final int UMBRELLAS = 8;

    private final SoftGlow glow;
    private final long[] umbrellaTick = new long[UMBRELLAS];
    private final double[] umbrellaX = new double[UMBRELLAS];
    private final double[] umbrellaY = new double[UMBRELLAS];
    private final double[] umbrellaScroll = new double[UMBRELLAS];
    private int next;

    EruptionLooks(SoftGlow glow) {
        this.glow = glow;
        restart();
    }

    /** The looks for a sortie with eruptions; null without them. */
    static EruptionLooks of(SoftGlow glow, Sortie sortie) {
        return sortie.eruptions().isPresent() ? new EruptionLooks(glow) : null;
    }

    /** The level restarts: no umbrella. */
    void restart() {
        Arrays.fill(umbrellaTick, Long.MIN_VALUE / 2);
        next = 0;
    }

    /** A vent at (x, y) erupted at step {@code tick} with the ground scrolled by {@code groundScroll}: its umbrella. */
    void erupted(double x, double y, long tick, double groundScroll) {
        umbrellaTick[next] = tick;
        umbrellaX[next] = x;
        umbrellaY[next] = y;
        umbrellaScroll[next] = groundScroll;
        next = (next + 1) % UMBRELLAS;
    }

    /** The umbrella's size {@code seconds} into it: opening fast, then settling a little wider. */
    static double umbrellaSize(double seconds) {
        double open = Math.clamp(seconds / OPEN_SECONDS, 0, 1);
        double eased = 1 - (1 - open) * (1 - open);
        double settle = Math.max(0, seconds - OPEN_SECONDS) / (UMBRELLA_SECONDS - OPEN_SECONDS);
        return UMBRELLA_FROM + (UMBRELLA_TO - UMBRELLA_FROM) * eased * (1 + 0.15 * Math.min(1, settle));
    }

    /** The umbrella's strength {@code seconds} into it: thick as it opens, thinning out to nothing at its end. */
    static float umbrellaAlpha(double seconds) {
        if (seconds < 0 || seconds >= UMBRELLA_SECONDS) {
            return 0;
        }
        double rise = Math.clamp(seconds / 0.2, 0, 1);
        double fade = 1 - Math.max(0, seconds - OPEN_SECONDS) / (UMBRELLA_SECONDS - OPEN_SECONDS);
        return (float) (UMBRELLA_PEAK * rise * fade * fade);
    }

    /** A rock's size share in phase {@code phase} at {@code progress} of it. */
    static double rockScale(Eruptions.Phase phase, double progress) {
        double p = Math.clamp(progress, 0, 1);
        return switch (phase) {
            case RISE -> SMALL + (1 - SMALL) * p;
            case FLY -> 1;
            case FALL -> 1 - (1 - SMALL) * p;
        };
    }

    /**
     * The ground side, over the ground units: the telling vents' glowing mouths and cracks, the umbrellas, and the soft
     * shadows of the rocks rising or falling over their vents.
     */
    void drawGround(SpriteBatch batch, Sortie sortie, double groundScroll) {
        Eruptions eruptions = sortie.eruptions().orElse(null);
        if (eruptions == null) {
            return;
        }
        long tick = sortie.tick();
        for (int u = 0; u < UMBRELLAS; u++) {
            double seconds = (tick - umbrellaTick[u]) * SimStep.SECONDS;
            float alpha = umbrellaAlpha(seconds);
            if (alpha <= 0) {
                continue;
            }
            SoftGlow.tint(batch, SULPHUR, alpha);
            glow.disc(batch, umbrellaX[u], umbrellaY[u] - (groundScroll - umbrellaScroll[u]), umbrellaSize(seconds));
        }
        for (int r = 0; r < eruptions.rocks(); r++) {
            if (!eruptions.rockUsed(r) || eruptions.rockPhase(r) == Eruptions.Phase.FLY) {
                continue;
            }
            double scale = rockScale(eruptions.rockPhase(r), eruptions.rockProgress(r));
            SoftGlow.tint(batch, Color.BLACK, (float) (0.35 * (1 - scale) + 0.1));
            glow.disc(batch, eruptions.rockX(r) + 6, eruptions.rockY(r) - 10, ROCK_SIZE * 1.3);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int v = 0; v < eruptions.vents(); v++) {
            if (!eruptions.ventPresent(v) || !eruptions.telling(v)) {
                continue;
            }
            double p = eruptions.tellProgress(v);
            double x = eruptions.ventX(v);
            double y = eruptions.ventY(v);
            SoftGlow.tint(batch, GLOW, (float) (0.45 + 0.55 * p));
            glow.disc(batch, x, y, MOUTH_FROM + (MOUTH_TO - MOUTH_FROM) * p);
            SoftGlow.tint(batch, HOT, (float) (0.3 + 0.7 * p));
            glow.disc(batch, x, y, 0.5 * (MOUTH_FROM + (MOUTH_TO - MOUTH_FROM) * p));
            SoftGlow.tint(batch, GLOW, (float) (0.5 * p));
            glow.ring(batch, x, y, 8 + CRACKS_TO * p);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }

    /** On the play plane: every rock, its stone body, then its burning core and halo. */
    void drawRocks(SpriteBatch batch, Sortie sortie) {
        Eruptions eruptions = sortie.eruptions().orElse(null);
        if (eruptions == null) {
            return;
        }
        for (int r = 0; r < eruptions.rocks(); r++) {
            if (!eruptions.rockUsed(r)) {
                continue;
            }
            double size = ROCK_SIZE * rockScale(eruptions.rockPhase(r), eruptions.rockProgress(r));
            SoftGlow.tint(batch, STONE, 1);
            glow.disc(batch, eruptions.rockX(r), eruptions.rockY(r), size * 1.5);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int r = 0; r < eruptions.rocks(); r++) {
            if (!eruptions.rockUsed(r)) {
                continue;
            }
            double size = ROCK_SIZE * rockScale(eruptions.rockPhase(r), eruptions.rockProgress(r));
            SoftGlow.tint(batch, EMBER, 0.35f);
            glow.disc(batch, eruptions.rockX(r), eruptions.rockY(r), size * 2.2);
            SoftGlow.tint(batch, HOT, 0.8f);
            glow.disc(batch, eruptions.rockX(r) - size * 0.08, eruptions.rockY(r) + size * 0.08, size * 0.6);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }
}
