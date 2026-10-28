package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Locale;
import vanguard.sim.PowerState;

/**
 * M9 part A: the right panel's power row under an enemy drain (design/ui/hud, Act 5 additions; design/player/generator,
 * Enemy drain effects), drawn in place of the spare-power row while a drainer is latched: the label reads {@value
 * #LABEL} in the warning colour; the gauge's scale is the generator's output, its cells lit to the load (the part of it
 * over the effective output in the alert red) and the drained MW as dark red hatched cells at its top end; the readout
 * gives the effective output ({@code 14/20}), or while the load is over it {@value #REGEN_OFF}, or once the weapons are
 * slowed too the rate factor ({@code FIRE 88%}). While the load is over the effective output the gauge flashes in the
 * low-armour flash's rhythm (1.2 s).
 */
final class PowerRow {
    static final String LABEL = "POWER DRAIN";
    static final String REGEN_OFF = "REGEN OFF";
    /** The cells' pitch and inset as {@link HudKit#segments} draws them. */
    static final int SEGMENT_PITCH = 13;
    /** The drained cells: dark red under lighter hatch strokes. */
    static final Color DRAINED = Color.valueOf("3A0A08");

    static final Color HATCH = Color.valueOf("8A1E14");
    static final Color LOAD = Color.valueOf("40FF80");
    static final Color EMPTY = Color.valueOf("0B2A15");
    /** The flash: lit for this many steps, then dark as long (the low-armour flash at 30 %: 1.2 s a cycle). */
    static final int FLASH_PHASE_TICKS = ShipPanel.LOW_ARMOUR_PHASE_TICKS;

    private final HudKit kit;

    PowerRow(HudKit kit) {
        this.kit = kit;
    }

    /** Whether the row shows the drain now: a drainer is latched. */
    static boolean draining(PowerState power) {
        return power != null && power.drainers() > 0;
    }

    /** The readout: the rate factor while the weapons are slowed, {@value #REGEN_OFF} while the load is over E, else E. */
    static String readout(PowerState power) {
        return readout(power.factor(), power.overLoad(), power.effectiveOutput(), power.output());
    }

    /**
     * The readout for the weapons' rate factor {@code factor}, whether the load is over E ({@code overLoad}), E and the
     * generator's output: {@code FIRE 88%}, {@value #REGEN_OFF} or {@code 14/20}.
     */
    static String readout(double factor, boolean overLoad, double effective, double output) {
        if (factor < 1) {
            return "FIRE " + Math.min(99, Math.round(factor * 100)) + "%";
        }
        if (overLoad) {
            return REGEN_OFF;
        }
        return mw(effective) + "/" + mw(output);
    }

    /** A power in MW as the HUD writes it: whole, or with one decimal when it has one. */
    static String mw(double value) {
        double rounded = Math.round(value * 10) / 10.0;
        return rounded == Math.rint(rounded)
                ? Long.toString(Math.round(rounded))
                : String.format(Locale.ROOT, "%.1f", rounded);
    }

    /** Whether the gauge flashes at step {@code tick}: while the load is over E, lit every other phase. */
    static boolean flashing(PowerState power, long tick) {
        return power.overLoad() && tick / FLASH_PHASE_TICKS % 2 == 0;
    }

    /** The cells of a gauge {@code width} px wide. */
    static int cells(int width) {
        return width / SEGMENT_PITCH;
    }

    /** The cells lit to {@code mw} of {@code output}, out of {@code cells} (any part of a cell lights it). */
    static int cellsOf(double mw, double output, int cells) {
        return (int) Math.ceil(cells * Math.clamp(mw / output, 0, 1) - 1e-9);
    }

    /** The cells of the effective output {@code mw} of {@code output}: whole cells only, the drained part rounded up. */
    static int effectiveCells(double mw, double output, int cells) {
        return (int) Math.floor(cells * Math.clamp(mw / output, 0, 1) + 1e-9);
    }

    /** The row with its label's text left at {@code x} and the plate's top 4 px above {@code y}. */
    void draw(SpriteBatch batch, PowerState power, long tick, int x, int y, int width, int barHeight) {
        kit.label(batch, LABEL, HudKit.AMBER, x, y);
        boolean flash = flashing(power, tick);
        kit.textRight(batch, kit.body, readout(power), power.overLoad() ? HudKit.ALERT : HudKit.AMBER, x, y, width);
        int barY = y - 34;
        kit.lcd(batch, x, barY, width, barHeight);
        int count = cells(width);
        double output = power.output();
        int effective = effectiveCells(power.effectiveOutput(), output, count);
        int load = cellsOf(power.load(), output, count);
        for (int i = 0; i < count; i++) {
            float cellX = x + 1 + i * SEGMENT_PITCH;
            float cellY = barY + 1;
            float cellWidth = SEGMENT_PITCH - 1;
            float cellHeight = barHeight - 2;
            if (i >= effective) {
                // The drained MW: dark red cells with three hatch strokes, lit red where the load runs into them.
                kit.fill(batch, i < load ? HudKit.ALERT : DRAINED, cellX, cellY, cellWidth, cellHeight);
                hatch(batch, cellX, cellY, cellWidth, cellHeight);
            } else {
                kit.fill(batch, i < load ? LOAD : EMPTY, cellX, cellY, cellWidth, cellHeight);
            }
        }
        if (flash) {
            kit.glow(batch, HudKit.ALERT, x, barY, width, barHeight);
        }
    }

    /** Diagonal hatch strokes across a cell (1 px steps up to the right). */
    private void hatch(SpriteBatch batch, float x, float y, float width, float height) {
        for (int stroke = -(int) height; stroke < width; stroke += 4) {
            for (int step = 0; step < height; step++) {
                float px = x + stroke + step;
                if (px >= x && px < x + width) {
                    kit.flat(batch, HATCH, px, y + step, 1, 1);
                }
            }
        }
    }
}
