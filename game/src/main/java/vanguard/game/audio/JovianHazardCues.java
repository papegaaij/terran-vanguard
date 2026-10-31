package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.HITS;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.SimEvents;

/**
 * M10 part A, step A3b: the sounds of the Jovian hazards for {@link FlightSounds} (design/audio/sfx, Act 6 sounds;
 * design/world/jovian, Hazards &amp; set pieces), round 72's picks levelled in their files:
 *
 * <ul>
 *   <li>a lightning column's telegraph, the Tesla coil's buzz swelling over its 1.0 s ({@link Sfx#LIGHTNING_TELL}), and
 *       the strike, the close crack and the thunder's roll ({@link Sfx#LIGHTNING_STRIKE}); a strike on the hull plays
 *       the shield's or the armour's hit as any shot;
 *   <li>an eruption at its tell's start ({@link Sfx#ERUPTION}: the cracking ground swelling over the tell, then the
 *       roar as the rocks fly); a rock shot ({@link #KNOCK_STAND_IN} quietly, throttled), a rock broken or burst on the
 *       hull ({@link #CRACK_STAND_IN}; the hull's hit is the shield's or the armour's), until round 73 picks the belt's
 *       rock sounds for them.
 * </ul>
 */
final class JovianHazardCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.STRIKE_TOLD,
            SimEvents.Type.STRIKE,
            SimEvents.Type.ERUPTION_TELL,
            SimEvents.Type.ROCK_HIT,
            SimEvents.Type.ROCK_DESTROYED,
            SimEvents.Type.ROCK_STRUCK);

    /** The rocks' stand-ins: a stone's dry crumble, quiet for a shot that only chips it. */
    static final Sfx KNOCK_STAND_IN = Sfx.CRUMBLE_SMALL;

    static final Sfx CRACK_STAND_IN = Sfx.CRUMBLE_LARGE;
    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** A rock shot plays at most once in this long. */
    static final double KNOCK_GAP_SECONDS = 0.1;

    private long now;
    private long lastKnock = Long.MIN_VALUE / 2;

    JovianHazardCues(SoundMix mix) {
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
            case STRIKE_TOLD -> bank.play(Sfx.LIGHTNING_TELL, CUE_LEVEL, pitch(0.02), pan);
            case STRIKE -> bank.play(Sfx.LIGHTNING_STRIKE, CUE_LEVEL, pitch(0.03), pan);
            case ERUPTION_TELL -> bank.play(Sfx.ERUPTION, CUE_LEVEL, pitch(0.03), pan);
            case ROCK_HIT -> {
                if (JovianCues.due(lastKnock, now, KNOCK_GAP_SECONDS)) {
                    lastKnock = now;
                    bank.play(KNOCK_STAND_IN, 0.6f * HITS, 1.2f * pitch(0.05), pan);
                }
            }
            case ROCK_DESTROYED -> bank.play(CRACK_STAND_IN, EXPLOSIONS, 1.1f * pitch(0.04), pan);
            case ROCK_STRUCK -> bank.play(CRACK_STAND_IN, EXPLOSIONS, pitch(0.04), pan);
            default -> {}
        }
    }
}
