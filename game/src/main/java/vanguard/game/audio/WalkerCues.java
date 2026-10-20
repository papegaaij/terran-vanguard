package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.Missiles;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StrideArena;

/**
 * M7 part F: the Revenant Walker's sounds for {@link FlightSounds} (design/audio/sfx, Revenant Walker; round 42 a each,
 * proposed, levelled in their files as round 41's and played as they are): its stomp at each footfall; a pod's hatch
 * opening before its salvo (the tell); each homing missile's launch, at most one every {@value #MISSILE_GAP_STEPS}
 * steps (a salvo's missiles 0.2 s apart each sound, two pods firing in one step do not stack); a missile shot down
 * bursts on the {@code tiny} rung (one that burst on a hull is the hull's hit, one that ran out of life just goes); the
 * flamer's rising hiss with the cone's tell, and the flame's roar as one loop while the cone burns, panned to its
 * flamer and faded in and out over 0.2 s. Its death is the boss's chained death (the large rung of a mid-boss).
 */
final class WalkerCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.WALKER_STEP,
            SimEvents.Type.POD_HATCH,
            SimEvents.Type.MISSILE_LAUNCHED,
            SimEvents.Type.MISSILE_DESTROYED,
            SimEvents.Type.CONE_TELL,
            SimEvents.Type.CONE_BURN);

    /** The one-shots are levelled in their files (−19 to −22 dB, as round 41's) and play as they are. */
    static final float WALKER_LEVEL = 1f;
    /** Missiles launched within this many steps of the last launch sound share it. */
    static final int MISSILE_GAP_STEPS = SimStep.ticks(0.1);

    /** The step of the last launch sound, and this step's. */
    private long lastLaunch = Long.MIN_VALUE / 2;

    private long now;
    /** The flame loop's level now (its fade). */
    private float flameLevel;

    WalkerCues(SoundMix mix) {
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
            case WALKER_STEP -> bank.play(Sfx.WALKER_STOMP, WALKER_LEVEL, pitch(0.03), pan);
            case POD_HATCH -> bank.play(Sfx.WALKER_HATCH, WALKER_LEVEL, pitch(0.03), pan);
            case MISSILE_LAUNCHED -> {
                if (launchDue(lastLaunch, now)) {
                    bank.play(Sfx.WALKER_MISSILE, WALKER_LEVEL, pitch(0.04), pan);
                    lastLaunch = now;
                }
            }
            case MISSILE_DESTROYED -> {
                if (events.value(i) == Missiles.SHOT_DOWN) {
                    bank.play(alternate(Sfx.EXPLOSION_TINY_A, Sfx.EXPLOSION_TINY_B), EXPLOSIONS, pitch(0.05), pan);
                }
            }
            case CONE_TELL -> bank.play(Sfx.WALKER_FLAME_TELL, WALKER_LEVEL, pitch(0.02), pan);
            // The burn's roar is the loop (watch).
            default -> {}
        }
    }

    /** Whether a missile's launch sounds at step {@code now} after the last launch sound at {@code last}. */
    static boolean launchDue(long last, long now) {
        return now - last >= MISSILE_GAP_STEPS || now < last;
    }

    /** Once a step: the flame loop while a walker's cone burns on the screen (faded out otherwise). */
    void watch(Sortie sortie) {
        boolean flying = sortie.flying() && !sortie.complete();
        float target = 0;
        float flamePan = 0;
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            StrideArena stride = piece.stride().orElse(null);
            if (stride != null && burning(piece, stride)) {
                target = flying ? UNIT_LOOP_LEVEL : 0;
                flamePan = pan(stride.coneX());
            }
        }
        flameLevel = fadeLoop(Sfx.WALKER_FLAME, flameLevel, target, flamePan);
    }

    /** Whether a walker's cone burns on the screen. */
    static boolean burning(SetPiece piece, StrideArena stride) {
        return !piece.destroyed() && !stride.offScreen() && stride.conePhase() == StrideArena.ConePhase.BURN;
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.WALKER_FLAME) {
            flameLevel = 0;
        }
    }
}
