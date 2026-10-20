package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;

/**
 * An act boss's death flash (design/enemies/bosses: the death sequence's screen flash): the play
 * field white at the end of its chained death, held a moment, then fading, as the Smart Bomb's flash
 * ({@code FarsideLooks.drawSmartBomb}); the Gameplay tab's flash reduction tones it down like every
 * white flash. Round 42: a local flash ({@link #startAt}, the Siege Spire's) is a white glow round where
 * the boss died instead, so the field is never washed white. Advanced with the simulation steps; pure
 * presentation.
 */
public final class ScreenFlash {
    /** Its opacity while held, how long it holds and how long it fades, s. */
    static final float OPACITY = 0.9f;

    static final double HOLD_SECONDS = 0.12;
    static final double FADE_SECONDS = 0.6;
    /** A local flash: its glow's radius, px, and its centre's opacity while held (fading to nothing at its rim). */
    static final double LOCAL_RADIUS = 190;

    static final float LOCAL_OPACITY = 0.75f;
    private static final float X0 = PixelScreen.PLAY_FIELD_X;

    /** Steps until it starts (negative once running), or {@link Integer#MIN_VALUE} while none is due. */
    private int ticks = Integer.MIN_VALUE;
    /** A local flash's centre on the play field (y up); NaN for the whole field. */
    private double localX = Double.NaN;

    private double localY;
    private VertexFills fills;

    /** A flash over the whole play field {@code delayTicks} steps from now. */
    public void start(int delayTicks) {
        ticks = delayTicks;
        localX = Double.NaN;
    }

    /** A local flash round (x, y) on the play field (y up) {@code delayTicks} steps from now. */
    public void startAt(int delayTicks, double x, double y) {
        ticks = delayTicks;
        localX = x;
        localY = y;
    }

    /** Whether the flash due or running is local (round where the boss died). */
    boolean local() {
        return !Double.isNaN(localX);
    }

    public void step() {
        if (ticks != Integer.MIN_VALUE) {
            ticks--;
            if (opacity(-ticks * SimStep.SECONDS) <= 0 && ticks < 0) {
                ticks = Integer.MIN_VALUE;
            }
        }
    }

    /** No flash, as when the level restarts. */
    public void clear() {
        ticks = Integer.MIN_VALUE;
    }

    /** Its opacity {@code seconds} after it started: held, then fading to nothing; 0 before. */
    static float opacity(double seconds) {
        if (seconds < 0) {
            return 0;
        }
        if (seconds <= HOLD_SECONDS) {
            return OPACITY;
        }
        return (float) (OPACITY * Math.max(0, 1 - (seconds - HOLD_SECONDS) / FADE_SECONDS));
    }

    /** Draws it over the play field at {@code strength} (1, or the reduced white flash). */
    public void draw(SpriteBatch batch, TextureRegion pixel, float alpha, float strength) {
        if (ticks == Integer.MIN_VALUE) {
            return;
        }
        float opacity = opacity((-ticks + alpha) * SimStep.SECONDS) * strength;
        if (opacity <= 0) {
            return;
        }
        if (local()) {
            if (fills == null) {
                fills = new VertexFills(pixel);
            }
            fills.disc(batch, localX, localY, LOCAL_RADIUS, Color.WHITE, opacity * LOCAL_OPACITY / OPACITY);
            return;
        }
        batch.setColor(1, 1, 1, opacity);
        batch.draw(pixel, X0, 0, PlayField.WIDTH, PlayField.HEIGHT);
        batch.setColor(Color.WHITE);
    }
}
