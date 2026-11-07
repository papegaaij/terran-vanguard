package vanguard.game.audio;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.SimEvents;

/**
 * M11 part G, step G4 (design/campaign Level 49, Music &amp; ambience; design/audio/sfx, Act 7 sounds; round 84's picks
 * levelled in their files, played as they are): the boss rush's echoes for {@link FlightSounds}:
 *
 * <ul>
 *   <li>an echo forming out of the memory ({@code ECHO_FORM}, at the boss): the soft rising shimmer ({@link
 *       Sfx#ECHO_FORM}, S.32 a), once per echo;
 *   <li>an echo dissolving into motes at its death chain's end ({@code ECHO_DISSOLVE}): the soft falling shimmer
 *       ({@link Sfx#ECHO_DISSOLVE}, S.33 a);
 *   <li>a unit it launched dissolving with it ({@code ECHO_UNIT_DISSOLVE}, at the unit): the same shimmer quieter and a
 *       shade higher, panned to the unit (its own voices: a brood dissolving at once plays three at most).
 * </ul>
 */
final class EchoCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES =
            EnumSet.of(SimEvents.Type.ECHO_FORM, SimEvents.Type.ECHO_DISSOLVE, SimEvents.Type.ECHO_UNIT_DISSOLVE);

    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** A launched unit's dissolve: this much of the echo's, a shade higher. */
    static final float UNIT_LEVEL = 0.35f;

    static final float UNIT_PITCH = 1.25f;

    EchoCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case ECHO_FORM -> bank.play(Sfx.ECHO_FORM, CUE_LEVEL, 1, pan);
            case ECHO_DISSOLVE -> bank.play(Sfx.ECHO_DISSOLVE, CUE_LEVEL, 1, pan);
            case ECHO_UNIT_DISSOLVE -> bank.play(Sfx.ECHO_DISSOLVE, CUE_LEVEL * UNIT_LEVEL, UNIT_PITCH, pan);
            default -> {}
        }
    }
}
