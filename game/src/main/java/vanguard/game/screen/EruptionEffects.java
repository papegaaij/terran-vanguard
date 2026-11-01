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
 *
 * <p>M10 part E, step E4: with Level 39's art (tools/art/refinery_units.py) a broken rock flies apart in its own break
 * ({@code eruption-rock-break}, solid: chips flung out and a dust ring that settles, {@value #BREAK_FRAME_TICKS} steps a
 * frame, ≈ 16 fps) with its hot core's flash and embers over it ({@code eruption-rock-break-glow}, additive) instead of
 * the tiny burst; one broken on a hull keeps the small burst (the hull's hit) under its break.
 */
final class EruptionEffects {
    static final int IMPACT_FRAME_TICKS = 2;
    /** M10 part E: the rock's break, 4 steps a frame (8 frames, ≈ 0.5 s). */
    static final int BREAK_FRAME_TICKS = 4;

    static final String BREAK = "eruption-rock-break";

    private final Sortie sortie;
    private final LevelRenderer renderer;
    private final WeaponLooks weaponLooks;
    private final Effects effects;
    private final Array<AtlasRegion> tiny;
    private final Array<AtlasRegion> small;
    /** M10 part E: the solid pieces' effects and the rock's break and its glow (empty without the art). */
    private final Effects pieces;

    private final Array<AtlasRegion> rockBreak;
    private final Array<AtlasRegion> rockBreakGlow;

    EruptionEffects(
            Sortie sortie,
            LevelRenderer renderer,
            WeaponLooks weaponLooks,
            Effects effects,
            Effects pieces,
            Array<AtlasRegion> tiny,
            Array<AtlasRegion> small,
            Array<AtlasRegion> rockBreak,
            Array<AtlasRegion> rockBreakGlow) {
        this.sortie = sortie;
        this.renderer = renderer;
        this.weaponLooks = weaponLooks;
        this.effects = effects;
        this.pieces = pieces;
        this.tiny = tiny;
        this.small = small;
        this.rockBreak = rockBreak;
        this.rockBreakGlow = rockBreakGlow;
    }

    /** M10 part E: a rock's break at (x, y): its own chips and embers, or {@code standIn}'s burst without the art. */
    private void broken(double x, double y, Array<AtlasRegion> standIn) {
        if (rockBreak.isEmpty()) {
            effects.start(standIn, TINY_EXPLOSION_FRAME_TICKS, x, y);
            return;
        }
        pieces.start(rockBreak, BREAK_FRAME_TICKS, x, y);
        if (!rockBreakGlow.isEmpty()) {
            effects.start(rockBreakGlow, BREAK_FRAME_TICKS, x, y);
        }
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
            case ROCK_DESTROYED -> broken(x, y, tiny);
            case ROCK_STRUCK -> {
                // The hull's hit: the small burst, the rock's own break over it.
                if (!rockBreak.isEmpty()) {
                    effects.start(small, TINY_EXPLOSION_FRAME_TICKS, x, y);
                }
                broken(x, y, small);
            }
            default -> {}
        }
    }
}
