package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.HITS;
import static vanguard.game.audio.FlightSounds.PICKUPS;
import static vanguard.game.audio.FlightSounds.PLAYER_DAMAGE;
import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import vanguard.sim.Armament;
import vanguard.sim.Shot;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WeaponSpec;

/**
 * M8 part A, the Act 4 systems' sounds for {@link FlightSounds} (design/audio/sfx, round 50's picks; the Torpedo Pod
 * keeps its r03 a): the special row's limit chime at a waterline, the Sonar Pulse's ping, the Decoy Flares' launch, the
 * Bulwark's hits and its loss; the Harpoon Torpedoes' hit for the hits the player's and the units' families hand over
 * ({@link #harpoonHit}), and its run: one loop for every harpoon in flight, held while any runs (decided in step A6:
 * one shared loop, not a voice per torpedo, so two weapons' worth of harpoons do not stack into a drone).
 */
final class DeepWaterCues extends SoundFamily {
    /** The Bulwark's hit: the drones' metal hit (round 46's light drone sounds reused), a little lower and louder. */
    private static final float BULWARK_HIT_PITCH = 0.85f;
    /** Its loss: the light drone's break lowered, on the medium rung. */
    private static final float BULWARK_BREAK_PITCH = 0.75f;

    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.SPECIAL_LIMIT,
            SimEvents.Type.SONAR_PULSE,
            SimEvents.Type.FLARES_LAUNCHED,
            SimEvents.Type.BULWARK_HIT,
            SimEvents.Type.BULWARK_DESTROYED);

    /** Per shot mount (the escort's past the armament's), whether it fires Harpoon Torpedoes. */
    private final boolean[] harpoon;
    /** The run loop's level now, faded in and out ({@link #fadeLoop}). */
    private float runLevel;

    DeepWaterCues(SoundMix mix, Armament armament, Optional<WeaponSpec> escort) {
        super(mix);
        harpoon = new boolean[armament.size() + (escort.isPresent() ? 1 : 0)];
        for (int m = 0; m < armament.size(); m++) {
            harpoon[m] = armament.mount(m).fires() && harpoon(armament.mount(m).weapon());
        }
        escort.ifPresent(gun -> harpoon[armament.size()] = harpoon(gun));
    }

    /** Whether a weapon plays the harpoon's sounds: its sound family {@code harpoon}. */
    static boolean harpoon(WeaponSpec weapon) {
        return weapon.sfx().equals("harpoon");
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /**
     * A hit by mount {@code mount}'s shot (an {@code ENEMY_HIT} or {@code GROUND_HIT}): a harpoon's plays its heavy
     * thud and returns true; any other plays nothing here and returns false.
     */
    boolean harpoonHit(int mount, float pan) {
        if (mount < 0 || mount >= harpoon.length || !harpoon[mount]) {
            return false;
        }
        bank.play(Sfx.HARPOON_HIT, EXPLOSIONS, pitch(0.04), pan);
        return true;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            // The row changed state at a waterline: the soft chime, under the warnings like the ready chime.
            case SPECIAL_LIMIT -> bank.play(Sfx.SPECIAL_LIMIT, PICKUPS, 1, 0);
            case SONAR_PULSE -> bank.play(Sfx.SPECIAL_SONAR, PLAYER_DAMAGE, 1, 0);
            case FLARES_LAUNCHED -> bank.play(Sfx.SPECIAL_FLARES, PLAYER_DAMAGE, pitch(0.03), pan);
            case BULWARK_HIT ->
                bank.play(alternate(Sfx.HIT_METAL_A, Sfx.HIT_METAL_B), HITS, BULWARK_HIT_PITCH * pitch(0.05), pan);
            case BULWARK_DESTROYED -> {
                bank.play(Sfx.DRONE_BREAK, EXPLOSIONS, BULWARK_BREAK_PITCH, pan);
                bank.play(alternate(Sfx.EXPLOSION_MEDIUM_A, Sfx.EXPLOSION_MEDIUM_B), EXPLOSIONS, pitch(0.04), pan);
            }
            default -> {}
        }
    }

    /** The harpoons' run: one loop while any of the player's harpoons is in flight, panned to their middle. */
    void watch(Sortie sortie) {
        boolean flying = sortie.flying() && !sortie.complete() && !mix.resync;
        int running = 0;
        double sumX = 0;
        if (flying) {
            for (int i = 0; i < sortie.shotCount(); i++) {
                Shot shot = sortie.shot(i);
                if (shot.mount() < harpoon.length && harpoon[shot.mount()]) {
                    running++;
                    sumX += shot.renderX(1);
                }
            }
        }
        runLevel = fadeLoop(
                Sfx.HARPOON_RUN, runLevel, running > 0 ? UNIT_LOOP_LEVEL : 0, running > 0 ? pan(sumX / running) : 0);
    }

    /** Stops the run loop (the level screen closes). */
    void silence() {
        if (bank.looping(Sfx.HARPOON_RUN)) {
            bank.stop(Sfx.HARPOON_RUN);
        }
        runLevel = 0;
    }
}
