package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.sim.Sortie;

/**
 * The start of a death's animation shared by {@link EventEffects}' families (RF9e): on the ground (drifting with the
 * floe of the unit whose death is starting) or on the play field.
 */
final class EffectStarts {
    /**
     * M5 part G: the drift (px/s across and up) of the ground unit whose death {@link EventEffects#react} is starting,
     * from its {@link vanguard.sim.SimEvents.Type#WRECK_DRIFT}; 0 otherwise.
     */
    private double driftX;

    private double driftY;

    private final Sortie sortie;

    EffectStarts(Sortie sortie) {
        this.sortie = sortie;
    }

    /** Sets the drift of the death that is starting (0, 0 once it has started). */
    void drift(double x, double y) {
        driftX = x;
        driftY = y;
    }

    double driftX() {
        return driftX;
    }

    double driftY() {
        return driftY;
    }

    /** Starts an animation, if there is one, centred on the position: on the ground or on the play field. */
    void start(
            Effects into,
            Array<AtlasRegion> frames,
            int ticksPerFrame,
            double x,
            double y,
            int delayTicks,
            boolean onGround) {
        if (frames.isEmpty()) {
            return;
        }
        if (onGround) {
            into.startOnGround(frames, ticksPerFrame, x, y, delayTicks, sortie.groundScroll(), driftX, driftY);
        } else {
            into.start(frames, ticksPerFrame, x, y, delayTicks);
        }
    }
}
