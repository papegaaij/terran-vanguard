package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * M10 part A, step A3b (design/ui/hud, Act 6 additions: the Reflex Shield): with a Reflex Shield fitted the shield bar
 * has a tick at its reflect window ({@code window} of the bar, 75 %), and while the window is open (the shield at or
 * over the window's share of its capacity, the Overcharge's raised one included) the filled cells above the tick are
 * gold; below it the bar keeps the shield's blue. A helper of {@link ShipPanel}, over the gauge it draws.
 */
final class ReflexTick {
    /** The open window's cells (the shield's white-gold, tools/art/act6_gear.py {@code reflex}). */
    static final Color GOLD = Color.valueOf("FFC840");
    /** The tick: a thin bright line across the bar, this far over and under it. */
    static final Color TICK = HudKit.LABEL;

    static final int TICK_OVER = 2;

    private ReflexTick() {}

    /** The first of {@code count} cells at or above the window ({@code window} of the bar). */
    static int tickCell(int count, double window) {
        return (int) Math.round(count * window);
    }

    /** Whether the reflect window is open: the shield holds at least {@code window} of its {@code capacity}. */
    static boolean open(double shield, double capacity, double window) {
        return window > 0 && capacity > 0 && shield >= window * capacity;
    }

    /**
     * Over the shield gauge's bar at (x, y), {@code width} × {@code height} ({@link HudKit#segments}): the gold cells of
     * an open window from the tick to the fill ({@code share} of the bar, 0 while the bar is dark), then the tick.
     */
    static void draw(
            HudKit kit,
            SpriteBatch batch,
            int x,
            int y,
            int width,
            int height,
            double share,
            boolean open,
            double window) {
        int count = width / HudKit.SEGMENT_PITCH;
        int tick = tickCell(count, window);
        if (open) {
            int lit = (int) Math.ceil(count * Math.clamp(share, 0, 1));
            for (int i = tick; i < lit; i++) {
                kit.fill(batch, GOLD, x + 1 + i * HudKit.SEGMENT_PITCH, y + 1, HudKit.SEGMENT_PITCH - 1, height - 2);
            }
        }
        kit.flat(batch, TICK, x + tick * HudKit.SEGMENT_PITCH, y - TICK_OVER, 1, height + 2 * TICK_OVER);
    }
}
