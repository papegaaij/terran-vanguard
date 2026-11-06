package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.game.render.LevelRenderer;
import vanguard.sim.SimEvents;

/**
 * M11 part A, step A3a (design/enemies, the {@code teleport} movement): the jumps' events on the play field. A unit's
 * jump away plays ART-A's violet-white shimmer over it ({@code teleport-fade}, 0.2 s, while the unit pass fades its
 * body out; nothing more without the art). The arrival ring (shrinking, sliding with its point) and the landing are the
 * teleport look's own (the units' state, {@code TeleportLooks}): never a flash.
 */
final class TeleportEffects {
    static final int FADE_FRAME_TICKS = 2;

    private final LevelRenderer renderer;
    private final Effects effects;

    TeleportEffects(LevelRenderer renderer, Effects effects) {
        this.renderer = renderer;
        this.effects = effects;
    }

    /** Event {@code i}, a jump's ({@code JUMP_OUT}, {@code JUMP_RING}, {@code ARRIVE}). */
    void react(SimEvents events, int i) {
        if (events.type(i) == SimEvents.Type.JUMP_OUT) {
            Array<AtlasRegion> fade = renderer.vrell().teleportFade();
            if (fade != null) {
                effects.start(fade, FADE_FRAME_TICKS, events.x(i), events.y(i));
            }
        }
    }
}
