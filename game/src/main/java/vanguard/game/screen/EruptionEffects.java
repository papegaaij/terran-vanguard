package vanguard.game.screen;

import static vanguard.game.screen.LevelScreen.TINY_EXPLOSION_FRAME_TICKS;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.game.render.LevelRenderer;
import vanguard.game.render.WeaponLooks;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3b (design/world/jovian, Hazards &amp; set pieces: eruptions; Level 39): the eruptions' events on
 * the play field. A vent throwing its rocks ({@code VENT_ERUPTED}) opens its umbrella of sulphur haze ({@code
 * EruptionLooks}, which draws the telling vents and the rocks from the simulation's state); a shot spent on a rock
 * ({@code ROCK_HIT}) makes its weapon's impact there; a rock broken under fire ({@code ROCK_DESTROYED}) bursts tiny, one
 * broken on a hull ({@code ROCK_STRUCK}) small.
 */
final class EruptionEffects {
    static final int IMPACT_FRAME_TICKS = 2;

    private final Sortie sortie;
    private final LevelRenderer renderer;
    private final WeaponLooks weaponLooks;
    private final Effects effects;
    private final Array<AtlasRegion> tiny;
    private final Array<AtlasRegion> small;

    EruptionEffects(
            Sortie sortie,
            LevelRenderer renderer,
            WeaponLooks weaponLooks,
            Effects effects,
            Array<AtlasRegion> tiny,
            Array<AtlasRegion> small) {
        this.sortie = sortie;
        this.renderer = renderer;
        this.weaponLooks = weaponLooks;
        this.effects = effects;
        this.tiny = tiny;
        this.small = small;
    }

    /** Event {@code i}, an eruption's ({@code VENT_ERUPTED}, {@code ROCK_HIT}, {@code ROCK_DESTROYED}, {@code _STRUCK}). */
    void react(SimEvents events, int i) {
        double x = events.x(i);
        double y = events.y(i);
        switch (events.type(i)) {
            case VENT_ERUPTED -> renderer.erupted(x, y, sortie.tick(), sortie.groundScroll());
            case ROCK_HIT ->
                // A special's blast (mount -1) chips it in the tiny burst.
                effects.start(
                        events.value(i) >= 0 ? weaponLooks.impact(events.value(i)) : tiny, IMPACT_FRAME_TICKS, x, y);
            case ROCK_DESTROYED -> effects.start(tiny, TINY_EXPLOSION_FRAME_TICKS, x, y);
            case ROCK_STRUCK -> effects.start(small, TINY_EXPLOSION_FRAME_TICKS, x, y);
            default -> {}
        }
    }
}
