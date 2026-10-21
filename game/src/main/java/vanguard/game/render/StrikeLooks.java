package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StrideStrikes;

/**
 * Round 44 (step WR2; the user in round 43, W.a and W.e): the walking boss's rail lance and stomp, drawn from the
 * simulation's {@link StrideStrikes} with soft fills ({@link VertexFills}), no art of their own (pending the user's
 * review).
 *
 * <p>With the bullets ({@link #drawLance}): over the charge a thin line from the lance on its back through the point it
 * aims at, to the field's edge, red and pulsing, brighter as the charge runs out, and a glow at the lance; over the shot
 * a bright beam (a white-hot core in a red glow as wide as the line it hurts) fading over its 0.3 s; where it hits the
 * ship or Rook a small white-hot flash (a disc, never the screen: the round-42 flash rule). Red keeps it apart from the
 * Ion Beam's blue and the flame's orange.
 *
 * <p>Over the ground units ({@link #drawStomp}): over the brace a dusty ring on the ground at the stomp's full radius,
 * its rim brightening as the tell runs out, a faint fill inside; then the shockwave: a bright band of dust running out
 * from the feet to that radius with the simulation's ring (the brace's mark fading as it goes, the swept ground dusted
 * faintly), held there and fading over {@value #WAVE_FADE_SECONDS} s after (step GFX4: H6's capture showed it as a
 * flash at the feet, gone in about 8 frames).
 */
final class StrikeLooks {
    /** The charge's line width, px, and its glow's. */
    static final double CHARGE_WIDTH = 3;

    static final double CHARGE_GLOW = 10;
    /** The shot's white core, px wide (its glow is the lance's hurting width). */
    static final double CORE_WIDTH = 4;
    /** A hit's flash: its radius, px, and how long it fades, steps. */
    static final double HIT_RADIUS = 26;

    static final int HIT_TICKS = SimStep.ticks(0.25);
    /**
     * The stomp's ring: its rim's width, px, the shockwave's, and its slices (round 44 W.h, step GK: brighter and
     * thicker, the brace's rim with a dark edge outside it so it reads on the pale floors).
     */
    static final double RIM_WIDTH = 7;

    static final double WAVE_WIDTH = 20;
    static final double EDGE_WIDTH = 2;
    /** Step GFX4: the shockwave's bright band, px wide, and its fade at the full radius after the ring, s. */
    static final double WAVE_BAND = 12;

    static final double WAVE_FADE_SECONDS = 0.35;
    static final int WAVE_FADE_TICKS = SimStep.ticks(WAVE_FADE_SECONDS);
    private static final int RING_SLICES = 40;
    /** The hit flashes remembered at once. */
    private static final int FLASHES = 4;

    /** Red, apart from the Ion Beam's blue and the flame's orange (the first capture's pale blue read as the beam). */
    private static final Color LANCE = new Color(1f, 0.28f, 0.2f, 1f);

    private static final Color LANCE_CORE = new Color(1f, 0.93f, 0.86f, 1f);
    private static final Color DUST = new Color(1f, 0.84f, 0.62f, 1f);
    private static final Color DUST_EDGE = new Color(0.28f, 0.18f, 0.11f, 1f);
    private static final long NEVER = Long.MIN_VALUE / 2;

    private final VertexFills fills;
    private final RingShade ringShade = new RingShade();
    private final double[] flashX = new double[FLASHES];
    private final double[] flashY = new double[FLASHES];
    private final long[] flashTick = new long[FLASHES];
    private int nextFlash;
    /** Step GFX4: the last stomp's ring: its centre and full radius, and the last step it was expanding. */
    private double waveX;

    private double waveY;
    private double waveRadius;
    private long waveTick = NEVER;

    StrikeLooks(VertexFills fills) {
        this.fills = fills;
        reset();
    }

    /** The level restarts (or the fight is retried): no flash. */
    void reset() {
        java.util.Arrays.fill(flashTick, NEVER);
        nextFlash = 0;
        waveTick = NEVER;
    }

    /** After each simulation step: the lance's hits flash where they land; the stomp's ring is kept for its fade. */
    void stepped(Sortie sortie, SetPiece piece) {
        if (!piece.destroyed()) {
            StrideStrikes strikes = piece.stride().orElseThrow().strikes();
            if (strikes.stompPhase() == StrideStrikes.StompPhase.RING) {
                waveX = strikes.ringX();
                waveY = strikes.ringY();
                waveRadius = strikes.stompRadius();
                waveTick = sortie.tick();
            }
        }
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.LANCE_HIT) {
                flashX[nextFlash] = events.x(i);
                flashY[nextFlash] = events.y(i);
                flashTick[nextFlash] = sortie.tick();
                nextFlash = (nextFlash + 1) % FLASHES;
            }
        }
    }

    /** With the bullets: the lance's charging line or its shot, and its hits' flashes. */
    void drawLance(SpriteBatch batch, Sortie sortie, SetPiece piece, double realSeconds) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (piece != null && !piece.destroyed()) {
            StrideStrikes strikes = piece.stride().orElseThrow().strikes();
            StrideStrikes.LancePhase phase = strikes.lancePhase();
            if (phase != StrideStrikes.LancePhase.IDLE) {
                double ox = strikes.lanceX();
                double oy = strikes.lanceY();
                double ex = ox + strikes.lanceDirX() * StrideStrikes.LANCE_REACH;
                double ey = oy + strikes.lanceDirY() * StrideStrikes.LANCE_REACH;
                double share = strikes.lanceShare();
                if (phase == StrideStrikes.LancePhase.CHARGE) {
                    float pulse = (float) (0.75 + 0.25 * Math.sin(realSeconds * 28));
                    float strength = chargeStrength(share) * pulse;
                    line(batch, ox, oy, ex, ey, CHARGE_GLOW, LANCE, 0.18f * strength);
                    line(batch, ox, oy, ex, ey, CHARGE_WIDTH, LANCE, 0.85f * strength);
                    fills.disc(batch, ox, oy, 10 + 8 * share, LANCE, 0.7f * strength);
                } else {
                    float fade = shotFade(share);
                    line(batch, ox, oy, ex, ey, strikes.lanceWidth() * 1.6, LANCE, 0.35f * fade);
                    line(batch, ox, oy, ex, ey, strikes.lanceWidth(), LANCE, 0.6f * fade);
                    line(batch, ox, oy, ex, ey, CORE_WIDTH, LANCE_CORE, 0.95f * fade);
                    fills.disc(batch, ox, oy, 22, LANCE_CORE, 0.8f * fade);
                }
            }
        }
        long tick = sortie.tick();
        for (int f = 0; f < FLASHES; f++) {
            long age = tick - flashTick[f];
            if (age >= 0 && age < HIT_TICKS) {
                float left = 1 - (float) age / HIT_TICKS;
                fills.disc(batch, flashX[f], flashY[f], HIT_RADIUS * (0.7 + 0.3 * left), LANCE_CORE, 0.8f * left);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** Over the ground units: the stomp's ground ring over its brace, then its expanding shockwave and its fade. */
    void drawStomp(SpriteBatch batch, Sortie sortie, SetPiece piece, float alpha, double dx, double dy) {
        if (piece == null || piece.destroyed()) {
            return;
        }
        StrideStrikes strikes = piece.stride().orElseThrow().strikes();
        StrideStrikes.StompPhase phase = strikes.stompPhase();
        if (phase != StrideStrikes.StompPhase.RING) {
            float fade = waveFade(sortie.tick() - waveTick, alpha);
            if (fade > 0) {
                // Step GFX4: held at its full radius after the ring, thinning and fading.
                ring(batch, waveX, waveY, waveRadius, waveRadius + EDGE_WIDTH, DUST_EDGE, 0.45f * fade);
                ring(batch, waveX, waveY, waveRadius - WAVE_BAND * (0.4 + 0.6 * fade), waveRadius, DUST, 0.95f * fade);
                batch.setColor(Color.WHITE);
            }
        }
        if (phase == StrideStrikes.StompPhase.IDLE) {
            return;
        }
        double share = strikes.stompShare();
        double radius = strikes.stompRadius();
        if (phase == StrideStrikes.StompPhase.TELL) {
            // It braces where it stands: the ring follows it until it stomps.
            double cx = strikes.ringX() + dx;
            double cy = strikes.ringY() + dy;
            ring(batch, cx, cy, 0, radius, DUST, 0.08f + 0.10f * (float) share);
            ring(batch, cx, cy, radius, radius + EDGE_WIDTH, DUST_EDGE, 0.35f + 0.25f * (float) share);
            ring(batch, cx, cy, radius - RIM_WIDTH, radius, DUST, 0.65f + 0.35f * (float) share);
        } else {
            // Step GFX4: a bright band running out with the simulation's ring (the look only), the brace's mark
            // fading over its first half, the swept ground dusted faintly.
            double r = strikes.ringRadius();
            double x = strikes.ringX();
            double y = strikes.ringY();
            ring(batch, x, y, radius - RIM_WIDTH / 2, radius, DUST, 0.45f * (float) Math.max(0, 1 - 2 * share));
            ring(batch, x, y, 0, Math.max(0, r - WAVE_BAND), DUST, 0.14f);
            ring(batch, x, y, r, r + EDGE_WIDTH, DUST_EDGE, 0.45f);
            ring(batch, x, y, Math.max(0, r - WAVE_BAND), r, DUST, 0.95f);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * The stomp wave's fade (1–0) {@code age} steps (plus {@code alpha} of the next) after the ring last expanded: 0
     * while it expands and once {@link #WAVE_FADE_TICKS} have passed.
     */
    static float waveFade(long age, float alpha) {
        double t = age + alpha;
        if (age < 0 || t <= 0 || t >= WAVE_FADE_TICKS) {
            return 0;
        }
        return (float) (1 - t / WAVE_FADE_TICKS);
    }

    /** The charge's strength {@code share} through it: faint at first, full as it runs out. */
    static float chargeStrength(double share) {
        return (float) (0.35 + 0.65 * Math.clamp(share, 0, 1));
    }

    /** The shot's strength {@code share} through it: full at once, fading over its last two thirds. */
    static float shotFade(double share) {
        return (float) Math.clamp(1.5 * (1 - share), 0, 1);
    }

    /** A line from (x0, y0) to (x1, y1), {@code width} px wide, in {@code colour} at {@code alpha}. */
    private void line(
            SpriteBatch batch, double x0, double y0, double x1, double y1, double width, Color colour, float alpha) {
        double length = Math.hypot(x1 - x0, y1 - y0);
        if (length < 1e-6 || alpha <= 0) {
            return;
        }
        double nx = -(y1 - y0) / length * width / 2;
        double ny = (x1 - x0) / length * width / 2;
        float c = Color.toFloatBits(colour.r, colour.g, colour.b, Math.min(1, alpha));
        fills.quad(batch, x0 - nx, y0 - ny, c, x0 + nx, y0 + ny, c, x1 + nx, y1 + ny, c, x1 - nx, y1 - ny, c);
    }

    /** A ring round (x, y) from {@code from} to {@code to} px, in {@code colour} at {@code alpha}. */
    private void ring(SpriteBatch batch, double x, double y, double from, double to, Color colour, float alpha) {
        if (to <= from || alpha <= 0) {
            return;
        }
        ringShade.colour = Color.toFloatBits(colour.r, colour.g, colour.b, Math.min(1, alpha));
        fills.sector(batch, x, y, 0, Math.PI, from, to, RING_SLICES, 1, ringShade);
    }

    /** One colour all round. */
    private static final class RingShade implements VertexFills.Shade {
        private float colour;

        @Override
        public float at(double along, double across) {
            return colour;
        }
    }
}
