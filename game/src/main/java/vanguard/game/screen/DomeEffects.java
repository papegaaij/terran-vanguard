package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.game.render.LevelRenderer;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M10 part A (design/enemies/ground/shield-pylon, The dome): the domes' events on the play field. A hit spent on a
 * dome sparks gold where it landed ({@code shield-pylon-dome-hit}, on the ground; the shared glance until its art is
 * on the pages); a dome collapsing for good shrinks to its pylon's crown ({@code JovianLooks}). A dome dropping and
 * flickering back is the dome look's own (the domes' state).
 */
final class DomeEffects {
    static final int HIT_FRAME_TICKS = 2;

    private final Sortie sortie;
    private final LevelRenderer renderer;
    private final Effects effects;
    private final Array<AtlasRegion> glance;

    DomeEffects(Sortie sortie, LevelRenderer renderer, Effects effects, Array<AtlasRegion> glance) {
        this.sortie = sortie;
        this.renderer = renderer;
        this.effects = effects;
        this.glance = glance;
    }

    /** Event {@code i}, a dome's ({@code DOME_HIT}, {@code _DOWN}, {@code _UP}, {@code _COLLAPSED}). */
    void react(SimEvents events, int i) {
        double x = events.x(i);
        double y = events.y(i);
        switch (events.type(i)) {
            case DOME_HIT -> {
                Array<AtlasRegion> hit = renderer.jovian().domeHit();
                effects.startOnGround(hit != null ? hit : glance, HIT_FRAME_TICKS, x, y, 0, sortie.groundScroll());
            }
            case DOME_COLLAPSED -> renderer.jovian().domeCollapsed(sortie, x, y);
            default -> {}
        }
    }
}
