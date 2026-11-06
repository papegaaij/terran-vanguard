package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.game.render.LevelRenderer;
import vanguard.game.render.SwarmLooks;
import vanguard.sim.SimEvents;

/**
 * M11 part A, step A3b (design/player/specials, Vrell Swarm Call (L46)): the Swarm Call's events on the play field. A
 * drone shot down bursts into a puff of spores where it was: its wither frames played twice as fast ({@link
 * SwarmLooks#BURST_FRAME_TICKS} steps a frame, no damage, no flash); nothing without the art. The bloom, the drones and
 * their wither are the call's look itself ({@code SwarmLooks}).
 */
final class SwarmEffects {
    private final LevelRenderer renderer;
    private final Effects effects;

    SwarmEffects(LevelRenderer renderer, Effects effects) {
        this.renderer = renderer;
        this.effects = effects;
    }

    /** Event {@code i}, a drone's ({@code SWARM_DRONE_LOST}). */
    void react(SimEvents events, int i) {
        if (events.type(i) != SimEvents.Type.SWARM_DRONE_LOST) {
            return;
        }
        SwarmLooks looks = renderer.swarm();
        Array<AtlasRegion> burst = looks != null ? looks.burst() : null;
        if (burst != null) {
            effects.start(burst, SwarmLooks.BURST_FRAME_TICKS, events.x(i), events.y(i));
        }
    }
}
