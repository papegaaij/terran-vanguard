package vanguard.game.audio;

import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.SplitMix64;

/**
 * What {@link FlightSounds}' families share (design/audio/sfx, Mixing rules): the bank, the sounds'
 * own generator for the pitch variation, the two variants in turn, the pan by the position, the
 * sounds waiting for their step, the sortie seen at the last watch and whether the next watch only
 * notes the state. One instance per flight; the families draw from it in the order the events come,
 * so the variation stays the same as one class drawing it.
 */
final class SoundMix {
    private static final int MAX_PENDING = 8;

    final SfxBank bank;
    /** The sounds' own generator (the pitch variation, the pounce's pick), apart from the simulation's. */
    final SplitMix64 random = new SplitMix64(0x5F3);
    /** Sounds waiting for their step (a set piece's break-up): what, how loud, pitch, pan, steps left. */
    private final Sfx[] pending = new Sfx[MAX_PENDING];

    private final float[] pendingVolume = new float[MAX_PENDING];
    private final float[] pendingPitch = new float[MAX_PENDING];
    private final float[] pendingPan = new float[MAX_PENDING];
    private final int[] pendingSteps = new int[MAX_PENDING];
    private int pendingCount;
    private boolean variantB;
    /** The sortie seen at the last {@link FlightSounds#watch} (the units a ring or a volley came from); null before. */
    Sortie watched;
    /** The next {@link FlightSounds#watch} only notes the state (after a restart or a boss checkpoint's restore). */
    boolean resync = true;

    SoundMix(SfxBank bank) {
        this.bank = bank;
    }

    /** Plays {@code sfx} in {@code steps} simulation steps (dropped when too many wait). */
    void later(Sfx sfx, float volume, float pitch, float pan, int steps) {
        if (pendingCount < MAX_PENDING) {
            pending[pendingCount] = sfx;
            pendingVolume[pendingCount] = volume;
            pendingPitch[pendingCount] = pitch;
            pendingPan[pendingCount] = pan;
            pendingSteps[pendingCount] = steps;
            pendingCount++;
        }
    }

    /** Advances the waiting sounds by one simulation step and plays the due ones. */
    void step() {
        for (int i = pendingCount - 1; i >= 0; i--) {
            if (--pendingSteps[i] <= 0) {
                bank.play(pending[i], pendingVolume[i], pendingPitch[i], pendingPan[i]);
                pendingCount--;
                pending[i] = pending[pendingCount];
                pendingVolume[i] = pendingVolume[pendingCount];
                pendingPitch[i] = pendingPitch[pendingCount];
                pendingPan[i] = pendingPan[pendingCount];
                pendingSteps[i] = pendingSteps[pendingCount];
            }
        }
    }

    /** Drops the waiting sounds (a restart). */
    void clearPending() {
        pendingCount = 0;
    }

    /** Two variants in turn, so no file plays twice in a row. */
    Sfx alternate(Sfx a, Sfx b) {
        variantB = !variantB;
        return variantB ? b : a;
    }

    /** A random pitch within {@code variation} of 1. */
    float pitch(double variation) {
        return (float) random.range(1 - variation, 1 + variation);
    }

    /** The subtle pan by the position {@code x} in the play field. */
    static float pan(double x) {
        return (float) (x / PlayField.WIDTH * 2 - 1) * FlightSounds.MAX_PAN;
    }
}
