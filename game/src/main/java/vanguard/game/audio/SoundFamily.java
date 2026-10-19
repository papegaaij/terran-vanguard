package vanguard.game.audio;

import java.util.Set;
import vanguard.sim.SimEvents;

/**
 * One family of {@link FlightSounds}' sounds (the player's side, the bosses, the enemy units, the
 * allies, the arsenal, the level's own): the event types it plays, each type in exactly one family,
 * and the state it follows between the events. The families share one {@link SoundMix}.
 */
abstract class SoundFamily {
    final SoundMix mix;
    final SfxBank bank;

    SoundFamily(SoundMix mix) {
        this.mix = mix;
        this.bank = mix.bank;
    }

    /** The event types this family plays. */
    abstract Set<SimEvents.Type> types();

    /** Before one step's events; {@code now} is the step of the sortie seen at the last watch (0 before). */
    void begin(long now) {}

    /** Plays event {@code i} of {@code events}, one of {@link #types}, panned to {@code pan}. */
    abstract void play(SimEvents events, int i, float pan);

    /** After one step's events. */
    void end() {}

    final Sfx alternate(Sfx a, Sfx b) {
        return mix.alternate(a, b);
    }

    final float pitch(double variation) {
        return mix.pitch(variation);
    }

    final void later(Sfx sfx, float volume, float pitch, float pan, int steps) {
        mix.later(sfx, volume, pitch, pan, steps);
    }

    static float pan(double x) {
        return SoundMix.pan(x);
    }
}
