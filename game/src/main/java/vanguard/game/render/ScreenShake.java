package vanguard.game.render;

/**
 * M7 part H: the play field's screen shake (design/ui/options, Gameplay: "screen shake 0..1 of the full shake"; the
 * game had none before). Only sideways, so the HUD's opaque side panels, drawn over the play field, hide its edges and
 * the HUD itself stays still. Its one use so far: the ridge pass's light tremble (design/campaign Level 21, the ridge
 * pass, "a light shake within the shake setting"), {@value #RIDGE_PX} px at most at the full setting, a mix of two
 * fast sines so it reads as a rumble, not a sway. Nothing is allocated.
 */
public final class ScreenShake {
    /** The ridge pass's tremble at its strongest and the full setting, px. */
    static final double RIDGE_PX = 2;
    /** The tremble's two frequencies, Hz, and their shares. */
    static final double FAST_HZ = 13;

    static final double SLOW_HZ = 7.3;
    static final double FAST_SHARE = 0.6;

    private ScreenShake() {}

    /** The ridge pass's sideways offset, px at the full setting, at strength {@code strength} (0–1) at real {@code seconds}. */
    static float ridge(double strength, double seconds) {
        if (!(strength > 0)) {
            return 0;
        }
        double wave = FAST_SHARE * Math.sin(2 * Math.PI * FAST_HZ * seconds)
                + (1 - FAST_SHARE) * Math.sin(2 * Math.PI * SLOW_HZ * seconds + 1.1);
        return (float) (RIDGE_PX * Math.min(1, strength) * wave);
    }

    /** The offset as drawn: {@code offset} at the full setting scaled by the setting {@code setting} (0–1), whole px. */
    public static float scaled(float offset, double setting) {
        return Math.round(offset * Math.clamp(setting, 0, 1));
    }
}
