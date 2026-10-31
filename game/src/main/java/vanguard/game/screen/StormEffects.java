package vanguard.game.screen;

import static vanguard.game.screen.LevelScreen.TINY_EXPLOSION_FRAME_TICKS;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import vanguard.game.render.Effects;
import vanguard.game.render.LevelRenderer;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M10 part A, step A3b (design/world/jovian, Hazards &amp; set pieces: lightning strikes): the told strikes' events on
 * the play field. A column told ({@code STRIKE_TOLD}) flashes the sky and sees through the banks; a strike ({@code
 * STRIKE}) starts its bolt's frames ({@code LightningLooks}, which draws the column and the bolt from the simulation's
 * state); a strike on the ship's or Rook's hull ({@code STRIKE_HIT}) bursts small where it hit (the hull's own shield
 * or armour hit is the ship's).
 */
final class StormEffects {
    private final Sortie sortie;
    private final LevelRenderer renderer;
    private final Effects effects;
    private final Array<AtlasRegion> burst;

    StormEffects(Sortie sortie, LevelRenderer renderer, Effects effects, Array<AtlasRegion> burst) {
        this.sortie = sortie;
        this.renderer = renderer;
        this.effects = effects;
        this.burst = burst;
    }

    /** Event {@code i}, a strike's ({@code STRIKE_TOLD}, {@code STRIKE}, {@code STRIKE_HIT}). */
    void react(SimEvents events, int i) {
        switch (events.type(i)) {
            case STRIKE_TOLD -> renderer.strikeTold(events.value(i), sortie.tick());
            case STRIKE -> renderer.strikeStruck(events.value(i), sortie.tick());
            case STRIKE_HIT -> effects.start(burst, TINY_EXPLOSION_FRAME_TICKS, events.x(i), events.y(i));
            default -> {}
        }
    }
}
