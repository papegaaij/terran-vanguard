package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.COLLAPSE_AT_WARNING;
import static vanguard.game.audio.FlightSounds.DRAIN_LEVEL;
import static vanguard.game.audio.FlightSounds.ENEMY_FIRE;
import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.GUST_LEVEL;
import static vanguard.game.audio.FlightSounds.GUST_PAN;
import static vanguard.game.audio.FlightSounds.SCRAPE_LEVEL;
import static vanguard.game.audio.FlightSounds.SONDE_LEVEL;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * The levels' own sounds for {@link FlightSounds}: Level 05's sled, Level 06's flares, Level 09's
 * collapse, Level 12's storm (the thunder after each flash, the gust with its telegraph, the sonde's
 * chime, the drain loop while a Lamprey drains), the canyon's wall scrape.
 */
final class LevelCues extends SoundFamily {
    /** The sled whine's loop (0.78 s) in simulation steps: its second play over the lights' 1.5 s chase. */
    private static final int SLED_WHINE_LOOP_STEPS = 47;
    /** The flare burn's seamless loop (round 23 a), played back to back while a flare burns. */
    private static final double FLARE_BURN_LOOP_SECONDS = 3;

    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.LIGHTNING,
            SimEvents.Type.SONDE_COLLECTED,
            SimEvents.Type.WALL_CONTACT,
            SimEvents.Type.SLED_LIGHTS,
            SimEvents.Type.SLED_LAUNCHED,
            SimEvents.Type.COLLAPSE_WARNING,
            SimEvents.Type.COLLAPSE_FALL,
            SimEvents.Type.FLARE_FIRED);

    /** How long a flare burns (the level's darkness, by difficulty); 0 without flares. */
    private double flareSeconds;
    /** M5 part F: the gust telegraph seen at the last watch: 1 from the left, -1 from the right, 0 none. */
    private int lastGust;

    LevelCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    void flareSeconds(double seconds) {
        flareSeconds = seconds;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            // M5 part F, Level 12 (round 34, the user's picks): the thunder after each flash and the sonde's
            // chime; the gust and the drain loop follow the state (watch).
            case LIGHTNING -> {
                int flash = events.value(i);
                later(
                        FlightSounds.thunder(flash),
                        EXPLOSIONS,
                        pitch(0.04),
                        FlightSounds.thunderPan(flash),
                        FlightSounds.thunderSteps(flash));
            }
            case SONDE_COLLECTED -> bank.play(Sfx.SONDE_CHIME, SONDE_LEVEL, 1, pan);
            // M7 part C: a wall touch's scrape on the rock's side (-1 left, 1 right; Rook's by his x).
            case WALL_CONTACT ->
                bank.play(Sfx.CANYON_SCRAPE, SCRAPE_LEVEL, pitch(0.04), FlightSounds.scrapePan(events.value(i), pan));
            // Level 05's sounds (round 21): the rail's charge hum, its loop played twice over the lights'
            // 1.5 s chase, and the sled's pass.
            case SLED_LIGHTS -> {
                bank.play(Sfx.SLED_WHINE, 0.7f, 1f, pan);
                later(Sfx.SLED_WHINE, 0.7f, 1f, pan, SLED_WHINE_LOOP_STEPS);
            }
            case SLED_LAUNCHED -> bank.play(Sfx.SLED_PASS, 0.8f, 1f, pan);
            // M5 part C: the arcology's collapse.
            case COLLAPSE_WARNING -> {
                if (COLLAPSE_AT_WARNING) {
                    bank.play(Sfx.ARCOLOGY_COLLAPSE, EXPLOSIONS, 1, 0);
                }
            }
            case COLLAPSE_FALL -> {
                if (!COLLAPSE_AT_WARNING) {
                    bank.play(Sfx.ARCOLOGY_COLLAPSE, EXPLOSIONS, 1, 0);
                }
            }
            // The perimeter beacon's flare (round 23 a): the flare-gun shot, then the road flare's
            // 3 s loop back to back while it burns, the last play quieter as the pool fades.
            case FLARE_FIRED -> {
                bank.play(Sfx.FLARE_LAUNCH, 0.6f * EXPLOSIONS, pitch(0.03), pan);
                bank.play(Sfx.FLARE_BURN, ENEMY_FIRE, 1, pan);
                for (int k = 1; k * FLARE_BURN_LOOP_SECONDS < flareSeconds; k++) {
                    boolean last = (k + 1) * FLARE_BURN_LOOP_SECONDS >= flareSeconds;
                    later(
                            Sfx.FLARE_BURN,
                            (last ? 0.5f : 1) * ENEMY_FIRE,
                            1,
                            pan,
                            SimStep.ticks(k * FLARE_BURN_LOOP_SECONDS));
                }
            }
            default -> {}
        }
    }

    /**
     * M5 part F (Level 12): a gust's wind as its telegraph starts, from the upwind side (not after a
     * restart that skipped into one); the drain's loop while a Lamprey drains the shield in flight.
     */
    void watchWeather(Sortie sortie) {
        int gust = sortie.wind().present() ? sortie.wind().telegraph(SimStep.ticks(sortie.levelSeconds())) : 0;
        if (gust != 0 && lastGust == 0 && !mix.resync) {
            bank.play(Sfx.GUST, GUST_LEVEL, 1, -gust * GUST_PAN);
        }
        lastGust = gust;
        boolean draining = sortie.draining() && sortie.flying() && !sortie.complete();
        if (draining && !bank.looping(Sfx.LAMPREY_DRAIN)) {
            bank.loop(Sfx.LAMPREY_DRAIN, DRAIN_LEVEL);
        } else if (!draining && bank.looping(Sfx.LAMPREY_DRAIN)) {
            bank.stop(Sfx.LAMPREY_DRAIN);
        }
    }
}
