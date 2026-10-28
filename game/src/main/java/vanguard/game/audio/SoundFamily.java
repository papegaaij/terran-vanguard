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

    /**
     * M7 part C: moves a unit loop's level from {@code level} toward {@code target} by a fade step (0.2 s from none to
     * {@link FlightSounds#UNIT_LOOP_LEVEL}), starting or stopping it, panned to {@code pan}; returns its new level.
     */
    final float fadeLoop(Sfx loop, float level, float target, float pan) {
        return fadeLoop(loop, level, target, pan, 1);
    }

    /** M9 part C: as {@link #fadeLoop(Sfx, float, float, float)}, the loop started at {@code pitch}. */
    final float fadeLoop(Sfx loop, float level, float target, float pan, float pitch) {
        if (target <= 0 && level <= 0) {
            if (bank.looping(loop)) {
                bank.stop(loop);
            }
            return 0;
        }
        float now = level;
        if (!bank.looping(loop)) {
            now = 0;
            if (pitch == 1) {
                bank.loop(loop, 0);
            } else {
                bank.loop(loop, 0, pitch);
            }
        }
        float step = FlightSounds.UNIT_LOOP_LEVEL / FlightSounds.UNIT_FADE_STEPS;
        now = target > now ? Math.min(target, now + step) : Math.max(target, now - step);
        if (now <= 0) {
            bank.stop(loop);
            return 0;
        }
        bank.loopVolume(loop, now);
        bank.loopPan(loop, pan);
        return now;
    }
}
