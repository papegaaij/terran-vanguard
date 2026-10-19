package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.BEAM_LEVEL;
import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.HITS;
import static vanguard.game.audio.FlightSounds.LANCE_LEVEL;
import static vanguard.game.audio.FlightSounds.PICKUPS;
import static vanguard.game.audio.FlightSounds.PLAYER_DAMAGE;
import static vanguard.game.audio.FlightSounds.SIZZLE_LEVEL;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.Beam;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * The specials' and the Act 3 arsenal's sounds for {@link FlightSounds} (round 37's picks; design/audio/sfx):
 * the Airstrike, the Smart Bomb, a special denied or ready again, the Deflector Pod, the drones, the EMP
 * Burst, the Orbital Lance, the overcharge and the dash; and the held loops that follow a state: the
 * beam's loop and sizzle, the lance's loop and rumble.
 */
final class ArsenalCues extends SoundFamily {
    /** M7 part A: a drone's absorbed hit, a soft ping under the hits; the deflector's return and the rebuild. */
    private static final float DRONE_HIT_LEVEL = FlightSounds.decibels(-12);

    private static final float ARSENAL_CUE = FlightSounds.decibels(-6);

    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.AIRSTRIKE_INBOUND,
            SimEvents.Type.AIRSTRIKE_BLAST,
            SimEvents.Type.SPECIAL_DENIED,
            SimEvents.Type.SMART_BOMB,
            SimEvents.Type.DEFLECTOR_CAUGHT,
            SimEvents.Type.DEFLECTOR_CHARGED,
            SimEvents.Type.DRONE_HIT,
            SimEvents.Type.DRONE_BROKEN,
            SimEvents.Type.DRONE_REBUILT,
            SimEvents.Type.SPECIAL_READY,
            SimEvents.Type.EMP_BURST,
            SimEvents.Type.EMP_STUNNED,
            SimEvents.Type.LANCE_LOCK,
            SimEvents.Type.OVERCHARGE,
            SimEvents.Type.OVERCHARGE_ENDED,
            SimEvents.Type.DASH);

    /** M7 part A: whether an EMP's power-down already played in this step. */
    private boolean powerDownThisStep;

    ArsenalCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void begin(long now) {
        powerDownThisStep = false;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            // The Airstrike (design/audio/sfx, Specials): the jets' flyby as the bombers enter,
            // the bomb carpet from the first blast; its single blasts play no sound of their own.
            case AIRSTRIKE_INBOUND -> bank.play(Sfx.AIRSTRIKE_JETS, EXPLOSIONS, 1, pan);
            case AIRSTRIKE_BLAST -> {
                if (events.value(i) == 0) {
                    bank.play(Sfx.AIRSTRIKE_BOMBS, EXPLOSIONS, 1, pan);
                }
            }
            case SPECIAL_DENIED -> bank.play(Sfx.SPECIAL_DENIED, PICKUPS, 1, 0);
            // The Smart Bomb (round 08 a): its energy blast, swelling over 0.5 s, on the huge rung's
            // sub-heavy boom, which gives the instant flash its punch.
            case SMART_BOMB -> {
                bank.play(Sfx.SMART_BOMB, PLAYER_DAMAGE, 1, 0);
                bank.play(Sfx.EXPLOSION_HUGE_B, 0.7f * EXPLOSIONS, 1, pan);
            }
            // M7 part A, the Act 3 arsenal (round 37's picks of 2026-10-13; design/audio/sfx).
            case DEFLECTOR_CAUGHT -> bank.play(Sfx.DEFLECTOR_TICK, HITS, pitch(0.05), pan);
            case DEFLECTOR_CHARGED -> bank.play(Sfx.DEFLECTOR_RETURN, ARSENAL_CUE, 1, pan);
            case DRONE_HIT ->
                bank.play(alternate(Sfx.HIT_METAL_A, Sfx.HIT_METAL_B), DRONE_HIT_LEVEL, 1.2f * pitch(0.05), pan);
            case DRONE_BROKEN -> bank.play(Sfx.DRONE_BREAK, EXPLOSIONS, pitch(0.04), pan);
            case DRONE_REBUILT -> bank.play(Sfx.DRONE_REBUILD, ARSENAL_CUE, 1, pan);
            case SPECIAL_READY -> bank.play(Sfx.COOLDOWN_READY, PICKUPS, 1, 0);
            case EMP_BURST -> bank.play(Sfx.EMP_RING, PLAYER_DAMAGE, 1, pan);
            case EMP_STUNNED -> {
                // One power-down a step, whatever the ring reaches in it; a machine's at full level, a
                // Vrell unit's quieter (pending the user's review).
                if (!powerDownThisStep) {
                    powerDownThisStep = true;
                    boolean machine = machine(events.value(i));
                    bank.play(Sfx.EMP_POWERDOWN, machine ? EXPLOSIONS : HITS, pitch(0.04), pan);
                }
            }
            case LANCE_LOCK -> bank.play(Sfx.LANCE_LOCK, PLAYER_DAMAGE, 1, pan);
            case OVERCHARGE -> bank.play(Sfx.OVERCHARGE_WHOOSH, PLAYER_DAMAGE, 1, 0);
            case OVERCHARGE_ENDED -> bank.play(Sfx.OVERCHARGE_END, PICKUPS, 1, 0);
            case DASH -> bank.play(Sfx.DASH, EXPLOSIONS, pitch(0.04), pan);
            default -> {}
        }
    }

    /** M7 part A: whether the level's enemy kind {@code kind} is a machine (its EMP stun's power-down). */
    private boolean machine(int kind) {
        Sortie watched = mix.watched;
        return watched != null
                && kind >= 0
                && kind < watched.enemyKinds().size()
                && watched.enemyKinds().get(kind).machine();
    }

    /**
     * M7 part A: the Act 3 arsenal's held sounds, which follow a state: while a beam runs its loop
     * (its start played with it as it comes on, its stop as it goes off: design/player/weapons/ion-beam,
     * Beam rules), the sizzle while it burns a target (on rock, a ground object, the rock sizzle); while
     * the Orbital Lance burns the `beam` family's loop c with the rumble under it.
     */
    void watch(Sortie sortie) {
        boolean flying = sortie.flying() && !sortie.complete();
        boolean beam = false;
        Beam.Contact contact = Beam.Contact.NONE;
        boolean rock = false;
        float beamPan = 0;
        for (int m = 0; m < sortie.armament().size(); m++) {
            Beam b = sortie.beam(m);
            if (b != null && b.on()) {
                beam = true;
                beamPan = pan(b.x());
                if (b.contact() == Beam.Contact.BURN) {
                    contact = Beam.Contact.BURN;
                    rock = b.target() < 0;
                }
            }
        }
        beam &= flying;
        if (beam && !bank.looping(Sfx.ION_BEAM_LOOP)) {
            bank.play(Sfx.ION_BEAM_START, BEAM_LEVEL, 1, beamPan);
            bank.loop(Sfx.ION_BEAM_LOOP, BEAM_LEVEL);
        } else if (!beam && bank.looping(Sfx.ION_BEAM_LOOP)) {
            bank.stop(Sfx.ION_BEAM_LOOP);
            if (!mix.resync && flying) {
                bank.play(Sfx.ION_BEAM_STOP, BEAM_LEVEL, 1, beamPan);
            }
        }
        boolean burning = beam && contact == Beam.Contact.BURN;
        hold(Sfx.BEAM_IMPACT, burning && !rock, SIZZLE_LEVEL);
        hold(Sfx.BEAM_IMPACT_ROCK, burning && rock, SIZZLE_LEVEL);
        boolean lance = flying && sortie.special().lanceStrength(1) > 0;
        hold(Sfx.LANCE_LOOP, lance, LANCE_LEVEL);
        hold(Sfx.LANCE_RUMBLE, lance, LANCE_LEVEL);
    }

    /** Starts or stops a held loop to follow {@code on}. */
    private void hold(Sfx loop, boolean on, float volume) {
        if (on && !bank.looping(loop)) {
            bank.loop(loop, volume);
        } else if (!on && bank.looping(loop)) {
            bank.stop(loop);
        }
    }
}
