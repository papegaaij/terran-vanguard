package vanguard.game.render;

import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M7 part D, a level's militia flak (design/campaign Level 17, Layout: the militia's flak guns at the spaceport
 * opening up at t≈160; the level's presentation key {@code flak}): from {@code from} s on a burst every {@code every}
 * s on {@code low-air} over the lower field, hitting nothing: the escort frigate's flak puff and its distant sound
 * (Level 11) without a ship. Presentation only, on the level clock: a restart that rewinds the clock replays the
 * bursts from there, and a frame that covers several steps shows only the latest burst. Its places come from the
 * burst's number, so a replay draws them where it did. Nothing is allocated per step. M7 part G: or in the top band
 * ({@code band: top}, Level 20's rim guns firing up past the ship from t=160: the bursts among the pursuers, below the
 * top edge), still on {@code low-air} and hitting nothing.
 */
public final class MilitiaFlak {
    /** The bursts' band over the lower field (y up): between these heights, and this far from the side edges. */
    static final double LOW_Y = 120;

    static final double HIGH_Y = 320;
    static final double SIDE_MARGIN = 50;
    /** M7 part G: the top band's bursts (y up): between these heights, the pursuit's band (50–150 px below the top). */
    static final double TOP_LOW_Y = PlayField.HEIGHT - 180;

    static final double TOP_HIGH_Y = PlayField.HEIGHT - 40;

    private final double from;
    private final double every;
    private final LevelData.FlakBand band;
    /** The bursts due at the last step. */
    private int shown;

    public MilitiaFlak(LevelData.Flak flak) {
        this(flak.from(), flak.every(), flak.bandOrDefault());
    }

    MilitiaFlak(double from, double every) {
        this(from, every, LevelData.FlakBand.LOWER);
    }

    MilitiaFlak(double from, double every, LevelData.FlakBand band) {
        this.from = from;
        this.every = every;
        this.band = band;
    }

    /** M7 part G: where its bursts fall: over the lower field or in the top band. */
    public LevelData.FlakBand band() {
        return band;
    }

    /** The bursts due by level time {@code seconds}: none before {@code from}, the first at {@code from}. */
    int due(double seconds) {
        return seconds < from ? 0 : (int) Math.floor((seconds - from) / every + 1e-9) + 1;
    }

    /**
     * The step at level time {@code seconds}: the number of the burst to start now, or -1 for none (none due since
     * the last step, or the clock went back).
     */
    public int step(double seconds) {
        int due = due(seconds);
        if (due <= shown) {
            shown = due;
            return -1;
        }
        shown = due;
        return due - 1;
    }

    /** Burst {@code n}'s x on the play field. */
    public static double x(int n) {
        return SIDE_MARGIN + unit(n, 0x2545F491) * (PlayField.WIDTH - 2 * SIDE_MARGIN);
    }

    /** Burst {@code n}'s height on the play field (y up), over the lower field. */
    public static double y(int n) {
        return y(n, LevelData.FlakBand.LOWER);
    }

    /** M7 part G: burst {@code n}'s height on the play field (y up) in its band. */
    public static double y(int n, LevelData.FlakBand band) {
        double low = band == LevelData.FlakBand.TOP ? TOP_LOW_Y : LOW_Y;
        double high = band == LevelData.FlakBand.TOP ? TOP_HIGH_Y : HIGH_Y;
        return low + unit(n, 0x6C8E9CF5) * (high - low);
    }

    /** A fixed spread of {@code n} into [0, 1). */
    private static double unit(int n, int salt) {
        int h = n * 0x9E3779B9 ^ salt;
        h ^= h >>> 16;
        h *= 0x7FEB352D;
        h ^= h >>> 15;
        h *= 0x846CA68B;
        h ^= h >>> 16;
        return (h >>> 8) / (double) (1 << 24);
    }
}
