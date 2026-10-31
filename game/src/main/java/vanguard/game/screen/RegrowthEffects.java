package vanguard.game.screen;

import static vanguard.game.screen.LevelScreen.TINY_EXPLOSION_FRAME_TICKS;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M10 part A (design/enemies/ground/graft-turret, Regrowth): a regrowing turret's events on the play field, on the
 * ground. Its shell bursts open as its root is exposed: at its first death that is the kill's own burst ({@code
 * ENEMY_DESTROYED} in the same step), a regrown turret's death bursts in the small explosion; the root's death crumbles
 * in the tiny burst. The root, its pulse and ring and the knit are the regrowth look's own (the unit's state).
 */
final class RegrowthEffects {
    private final Sortie sortie;
    private final Effects effects;
    private final Array<AtlasRegion> small;
    private final Array<AtlasRegion> tiny;

    RegrowthEffects(Sortie sortie, Effects effects, Array<AtlasRegion> small, Array<AtlasRegion> tiny) {
        this.sortie = sortie;
        this.effects = effects;
        this.small = small;
        this.tiny = tiny;
    }

    /** Event {@code i}, a regrowth's ({@code ROOT_EXPOSED}, {@code ROOT_KILLED}, {@code KNITTING}, {@code REGROWN}). */
    void react(SimEvents events, int i) {
        double x = events.x(i);
        double y = events.y(i);
        switch (events.type(i)) {
            case ROOT_EXPOSED -> {
                if (!killedHere(events, i)) {
                    effects.startOnGround(small, TINY_EXPLOSION_FRAME_TICKS, x, y, 0, sortie.groundScroll());
                }
            }
            case ROOT_KILLED -> effects.startOnGround(tiny, TINY_EXPLOSION_FRAME_TICKS, x, y, 0, sortie.groundScroll());
            default -> {}
        }
    }

    /**
     * Whether event {@code i}'s unit was killed (its first death, paid) in the same step: an {@code ENEMY_DESTROYED} of
     * its kind at its place before it, whose burst is the shell's.
     */
    static boolean killedHere(SimEvents events, int i) {
        for (int e = 0; e < i; e++) {
            if (events.type(e) == SimEvents.Type.ENEMY_DESTROYED
                    && events.value(e) == events.value(i)
                    && events.x(e) == events.x(i)
                    && events.y(e) == events.y(i)) {
                return true;
            }
        }
        return false;
    }
}
