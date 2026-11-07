package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import vanguard.sim.Enemy;
import vanguard.sim.Sortie;

/**
 * M11 part F, step F4 (design/enemies/ground/reactor-core; Level 48's secondary; tools/art/act7_citadel.py): a reactor
 * core in its wreck's hold: whole, cracked below half its HP ({@code reactor-core_0..1}); its gold-white breath ({@code
 * -glow_0..7} additive at {@value #GLOW_FPS} fps), flickering while it is in the field's bottom band (about to scroll off
 * alive: the HUD's leaving blink, {@link Sortie#leaving()}). Its death's flash and debris ring ({@code -death}, {@code
 * -tatters}) and its {@code -stump} are the units' own; its veins going dark are {@link CitadelLooks}'.
 */
final class ReactorCoreLooks {
    static final String SLUG = CitadelLooks.CORE;
    /** The breath's rate, frames a second. */
    static final double GLOW_FPS = 4;
    /** The flicker in the bottom band: a new level every this many steps, between these. */
    static final int FLICKER_TICKS = 3;

    static final float FLICKER_LOW = 0.25f;

    private ReactorCoreLooks() {}

    /** Whether the unit is a reactor core. */
    static boolean is(Enemy enemy) {
        return enemy.spec().slug().equals(SLUG);
    }

    /** A reactor core's body: whole, or cracked below half its HP; null for any other unit. */
    static AtlasRegion frame(Enemy enemy, EnemyLooks look) {
        if (!is(enemy) || look.frames().size < 2) {
            return null;
        }
        return look.frames().get(cracked(enemy.hp(), enemy.spec().hp()) ? 1 : 0);
    }

    /** Whether a core with {@code hp} of {@code max} shows its cracked frame. */
    static boolean cracked(double hp, double max) {
        return hp < max / 2;
    }

    /** The breath's frame at step {@code tick} (of {@code frames}), each core its own phase by {@code serial}. */
    static int glowFrame(long tick, int serial, int frames) {
        return Math.floorMod((long) Math.floor(tick * vanguard.sim.SimStep.SECONDS * GLOW_FPS) + serial, frames);
    }

    /**
     * The glow's strength for a core at {@code y} (px above the bottom edge) at step {@code tick}: full, flickering
     * inside the level's bottom band ({@code band} px, 0 for none).
     */
    static float glowShare(double y, double band, long tick, int serial) {
        if (!(band > 0) || y >= band) {
            return 1;
        }
        long step = tick / FLICKER_TICKS + serial * 31L;
        long bits = step * 0x9E3779B97F4A7C15L;
        bits ^= bits >>> 29;
        double noise = (bits >>> 11) * 0x1.0p-53;
        return (float) (FLICKER_LOW + (1 - FLICKER_LOW) * noise);
    }
}
