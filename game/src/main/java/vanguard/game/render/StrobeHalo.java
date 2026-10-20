package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Map;

/**
 * Step GFX2 (the round 43 capture: Level 20's emergency cache showed no visible strobe at play scale, its lamp a 3 px
 * dot): a trigger look whose {@code -glow} frames are a strobe gets a soft halo round its lamp while the lamp is lit,
 * drawn with the glow frames (additive, after the light pass): a wide yellow bloom and a bright core. Which frames are
 * lit and how bright comes from the art ({@code tools/art/l20_props.py}; {@code StrobeHaloTest} checks it against the
 * frames on disk), the lamp's place too. Presentation only.
 */
final class StrobeHalo {
    /** One strobe: its lamp's offset from the frame's middle (px, y up) and each glow frame's brightness (0 dark). */
    record Strobe(double dx, double dy, float[] brightness, Color colour) {
        /** The halo's strength at glow frame {@code frame}: its brightness, 0 for a frame past the table. */
        float strength(int frame) {
            return frame >= 0 && frame < brightness.length ? brightness[frame] : 0;
        }
    }

    /** The bloom's and the core's diameter, px, and their opacity at full brightness. */
    static final double BLOOM = 34;

    static final double CORE = 12;
    static final float BLOOM_ALPHA = 0.85f;
    static final float CORE_ALPHA = 1f;
    private static final Color CORE_TINT = new Color(1f, 0.98f, 0.8f, 1f);

    /**
     * The strobes by trigger look: Level 20's emergency cache, its lamp at pixel (27, 8) of the 36 x 32 frame (y down),
     * lit in glow frames 0, 1 (dimmer), 4 and 6 (the intact loop 0..3, the hit loop 4..7: {@link GroundGlow}).
     */
    static final Map<String, Strobe> STROBES = Map.of(
            "emergency-cache",
            new Strobe(
                    27.5 - 18, 16 - 8.5, new float[] {1, 0.66f, 0, 0, 1, 0, 0.97f, 0}, new Color(1f, 0.82f, 0.25f, 1)));

    private StrobeHalo() {}

    /** The strobe of trigger look {@code look}, null for a look without one. */
    static Strobe of(String look) {
        return STROBES.get(look);
    }

    /** The halo of {@code strobe} at glow frame {@code frame}, its frame centred on the play-field point (x, y). */
    static void draw(SpriteBatch batch, SoftGlow glow, Strobe strobe, int frame, double x, double y) {
        float strength = strobe.strength(frame);
        if (strength <= 0) {
            return;
        }
        double lampX = x + strobe.dx();
        double lampY = y + strobe.dy();
        SoftGlow.tint(batch, strobe.colour(), BLOOM_ALPHA * strength);
        glow.disc(batch, lampX, lampY, BLOOM);
        SoftGlow.tint(batch, CORE_TINT, CORE_ALPHA * strength);
        glow.disc(batch, lampX, lampY, CORE);
        batch.setColor(Color.WHITE);
    }
}
