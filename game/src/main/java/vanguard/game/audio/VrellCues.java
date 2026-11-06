package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.PICKUPS;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.SporeClouds;

/**
 * M11 part A, step A3a (design/audio/sfx; design/enemies, Distorted space): the sounds of Vrell space's hazards for
 * {@link FlightSounds}: a reversal told 2 s ahead plays the soft rising two-tone with the HUD's chevron ({@link
 * Sfx#REVERSAL_TELL}, round 84 S.36 a, levelled in its file), under Rook's call. The reversal's run has no sound of its
 * own (the docs name a groan but round 84 picked none).
 *
 * <p>Step A3b (design/world/vrell-space, Spore clouds): while the ship's centre is inside a spore cloud ({@code
 * SPORE_INSIDE} until {@code SPORE_LEFT}) the quiet dry rustle loops ({@link Sfx#SPORE_INSIDE}, round 84 S.43 b, faded in
 * and out over 0.2 s); a cloud set burning crackles ({@link Sfx#SPORE_BURN}, S.44 a). And the boss rush's refill between
 * two fights (design/ui/hud, the echo counter: the armour bar's 1 s fill) plays the shield's restore chime.
 */
final class VrellCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.REVERSAL_TOLD,
            SimEvents.Type.SPORE_INSIDE,
            SimEvents.Type.SPORE_LEFT,
            SimEvents.Type.SPORE_BURN,
            SimEvents.Type.BOSS_REFILL);

    /** The tell is levelled in its file (quiet, -28 dB) and plays as it is, centred. */
    static final float CUE_LEVEL = 1f;

    /** Whether the ship's centre is inside a spore cloud (its rustle loops), and the loop's level now. */
    private boolean inside;

    private float rustle;

    VrellCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case REVERSAL_TOLD -> bank.play(Sfx.REVERSAL_TELL, CUE_LEVEL, 1, 0);
            case SPORE_INSIDE -> inside = true;
            case SPORE_LEFT -> inside = false;
            case SPORE_BURN -> bank.play(Sfx.SPORE_BURN, CUE_LEVEL * EXPLOSIONS, pitch(0.05), pan);
            case BOSS_REFILL -> bank.play(Sfx.SHIELD_RESTORE, PICKUPS, 1, 0);
            default -> {}
        }
    }

    /**
     * Once a step: the rustle follows the ship in or out of the clouds; after a restart (or out of the flight) it
     * follows the clouds' state, which raises no event.
     */
    void watch(Sortie sortie) {
        SporeClouds spores = sortie.spores();
        if (spores == null) {
            return;
        }
        boolean flying = sortie.flying() && !sortie.complete();
        if (mix.resync || !flying) {
            inside = flying && spores.inside(sortie.ship().x(), sortie.ship().y());
        }
        rustle = fadeLoop(Sfx.SPORE_INSIDE, rustle, inside ? FlightSounds.UNIT_LOOP_LEVEL : 0, 0);
    }

    /** The flight's loops stop: the rustle with them. */
    void silence(Sfx loop) {
        if (loop == Sfx.SPORE_INSIDE) {
            rustle = 0;
            inside = false;
        }
    }
}
