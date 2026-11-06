package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.ENEMY_FIRE;
import static vanguard.game.audio.FlightSounds.EXPLOSIONS;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.SimEvents;

/**
 * M11 part A, step A3b: the Vrell Swarm Call's sounds for {@link FlightSounds} (design/player/specials, Vrell Swarm
 * Call (L46); design/audio/sfx, Act 7 sounds): the call is the low breathy horn swell ({@link Sfx#SWARM_CALL}, round 84
 * S.48 b, levelled in its file), a drone's thorn the Needler's spit (the enemies' small shot) at −6 dB under the
 * enemies' fire, at most once in {@value #THORN_GAP_SECONDS} s for the six, and a drone shot down the soft dry crunch
 * ({@link Sfx#SWARM_DRONE_BURST}, S.49 a). A drone struck by a bullet and the wither play nothing (a fade).
 */
final class SwarmCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES =
            EnumSet.of(SimEvents.Type.SWARM_CALLED, SimEvents.Type.SWARM_THORN, SimEvents.Type.SWARM_DRONE_LOST);

    /** The call and the burst are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The thorns: the Needler's spit 6 dB under the enemies' shots. */
    static final float THORN_LEVEL = ENEMY_FIRE * FlightSounds.decibels(-6);
    /** The six drones' thorns play at most once in this long. */
    static final double THORN_GAP_SECONDS = 0.08;

    private long now;
    private long lastThorn = Long.MIN_VALUE / 2;

    SwarmCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void begin(long now) {
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case SWARM_CALLED -> bank.play(Sfx.SWARM_CALL, CUE_LEVEL, 1, pan);
            case SWARM_THORN -> {
                if (JovianCues.due(lastThorn, now, THORN_GAP_SECONDS)) {
                    lastThorn = now;
                    bank.play(alternate(Sfx.ENEMY_SHOT_A, Sfx.ENEMY_SHOT_B), THORN_LEVEL, 1.1f * pitch(0.06), pan);
                }
            }
            case SWARM_DRONE_LOST -> bank.play(Sfx.SWARM_DRONE_BURST, CUE_LEVEL * EXPLOSIONS, pitch(0.05), pan);
            default -> {}
        }
    }
}
