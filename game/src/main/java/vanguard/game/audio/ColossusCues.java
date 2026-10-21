package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import vanguard.sim.BossSpec;
import vanguard.sim.BreachArena;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M7 part H: the Dust Colossus's sounds for {@link FlightSounds} (design/campaign Level 21, Music &amp; ambience;
 * design/audio/sfx; round 44's picks, a each (the ridge pass mirrored to run right to left); levelled in their files and
 * played as they are). The bulge at each breach's tell and each eruption's marker; the eruption as the mouth breaches
 * (the rise, an eruption); the suction's draw as one loop while the mouth is open and through its bite's tell, panned to
 * the mouth, louder the nearer the ship (its {@value #FAR_SHARE} share beyond the suction's outer radius, full within
 * its inner one), faded in and out over 0.2 s; the bite's snap; and Level 21's ridge pass (the level's {@code ridge},
 * given by {@link #ridgePass}) as one sound when the level clock reaches it, its pan in the file. Its death is the
 * boss's own ({@link Sfx#COLOSSUS_DEATH}, {@code BossCues}); the basin rumble's swell and cut and the wind's calm are
 * {@code LevelMusic}'s. Nothing here allocates per step.
 */
final class ColossusCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.BREACH_TELL,
            SimEvents.Type.ERUPTION_MARKER,
            SimEvents.Type.BREACH_RISE,
            SimEvents.Type.ERUPTION,
            SimEvents.Type.BITE_SNAP);

    /** The sounds are levelled in their files (the tells -16 LUFS, the rest -14, the loop -28 dB band RMS). */
    static final float COLOSSUS_LEVEL = 1f;
    /** The suction's draw beyond its outer radius, a share of its full level (it is heard across the field). */
    static final float FAR_SHARE = 0.35f;

    /** The ridge pass's level time, s; NaN for a level without one. */
    private double ridgeAt = Double.NaN;
    /** The level time at the last watch (a restart rewinds it); NaN before the first. */
    private double lastSeconds = Double.NaN;
    /** The suction loop's level now (its fade). */
    private float suctionLevel;

    ColossusCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** Level 21's ridge pass starts at level time {@code seconds} (the level's {@code ridge}): its rumble then. */
    void ridgePass(double seconds) {
        ridgeAt = seconds;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case BREACH_TELL, ERUPTION_MARKER -> bank.play(Sfx.COLOSSUS_BULGE, COLOSSUS_LEVEL, pitch(0.03), pan);
            case BREACH_RISE, ERUPTION -> bank.play(Sfx.COLOSSUS_ERUPTION, COLOSSUS_LEVEL, pitch(0.03), pan);
            case BITE_SNAP -> bank.play(Sfx.COLOSSUS_BITE, COLOSSUS_LEVEL, pitch(0.03), pan);
            default -> {}
        }
    }

    /**
     * Once a step: the ridge pass's rumble as the level clock reaches it (again after a restart to the start), and
     * the suction's draw while a breaching boss's mouth draws.
     */
    void watch(Sortie sortie) {
        double seconds = sortie.levelSeconds();
        if (ridgeDue(lastSeconds, seconds, ridgeAt) && sortie.flying() && !sortie.complete()) {
            bank.play(Sfx.COLOSSUS_RIDGE, COLOSSUS_LEVEL, 1, 0);
        }
        lastSeconds = seconds;
        boolean flying = sortie.flying() && !sortie.complete();
        float target = 0;
        float pan = 0;
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            Optional<BreachArena> view = piece.breach();
            if (view.isEmpty()
                    || !piece.present()
                    || piece.destroyed()
                    || !drawing(view.get().phase())) {
                continue;
            }
            BreachArena breach = view.get();
            Optional<BossSpec.Suction> suction = piece.boss().orElseThrow().suction();
            double distance = Math.hypot(
                    sortie.ship().renderX(1) - breach.mouthX(), sortie.ship().renderY(1) - breach.mouthY());
            target = flying
                    ? suction.isPresent()
                            ? draw(
                                    distance,
                                    suction.get().innerRadius(),
                                    suction.get().outerRadius())
                            : UNIT_LOOP_LEVEL
                    : 0;
            pan = pan(breach.mouthX());
        }
        suctionLevel = fadeLoop(Sfx.COLOSSUS_SUCTION, suctionLevel, target, pan);
    }

    /** Whether the mouth draws in a phase: open, and through its bite's tell (the suction's own rule). */
    static boolean drawing(BreachArena.Phase phase) {
        return phase == BreachArena.Phase.OPEN || phase == BreachArena.Phase.BITE;
    }

    /**
     * The draw's level with the ship {@code distance} px from the mouth: full within the suction's {@code inner} radius,
     * falling to {@link #FAR_SHARE} of it at its {@code outer} one and beyond.
     */
    static float draw(double distance, double inner, double outer) {
        double near = Math.clamp((outer - distance) / Math.max(1e-9, outer - inner), 0, 1);
        return (float) (UNIT_LOOP_LEVEL * (FAR_SHARE + (1 - FAR_SHARE) * near));
    }

    /**
     * Whether the ridge pass at {@code at} starts after the level clock {@code before} up to {@code now}: never at the
     * first watch ({@code before} NaN) nor as a restart or a boss checkpoint rewinds the clock (a pass already behind
     * the restored clock stays silent).
     */
    static boolean ridgeDue(double before, double now, double at) {
        if (Double.isNaN(at) || Double.isNaN(before) || now < before) {
            return false;
        }
        return before < at && at <= now;
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.COLOSSUS_SUCTION) {
            suctionLevel = 0;
        }
    }
}
