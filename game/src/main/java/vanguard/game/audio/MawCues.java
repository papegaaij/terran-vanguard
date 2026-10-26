package vanguard.game.audio;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import vanguard.game.render.BreachLine;
import vanguard.sim.CoilSpec;
import vanguard.sim.CruiserRun;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part H, Level 28's sounds for {@link FlightSounds} (design/audio/sfx, the Abyssal Maw's and the finale's rows;
 * round 58's a variants as stand-ins until the user's pick): the coiled boss's gill breath ({@code SAC_SWELL}), throat
 * charge ({@code THROAT_GLOW}), a sweep's groan and swing ({@code COIL_SWEEP_TELL}, {@code COIL_SWEEP}), the dive and
 * the lunge, an implant's spark and zap ({@code IMPLANT_SPARK}, {@code IMPLANT_ARC}) and an Eel Swarm's shed (as its
 * plate's flare ends: {@code SHED_TELL} plus the shed's tell); its arrival roar and death are {@code BossCues}'. The
 * finale ({@link #watch}): the climb up the shaft as the clock resumes after the kill's hold, the breakout as the medium
 * turns to air; the cruisers' rise ({@code CRUISER_RISE}), a broadside's volley ({@code BROADSIDE}), a point-defence
 * burst (its first shot, {@code POINT_DEFENCE}) and a flak airburst (the lob's impact while the cruisers are on the
 * screen, {@link #flakImpact}).
 */
final class MawCues extends SoundFamily {
    /** The one-shots sit at their files' levels (levelled beside the Act 4 files by step AUD-H). */
    static final float CUE_LEVEL = 1f;
    /** A cruiser's point-defence shots within this long of its last start no new burst sound. */
    static final int PD_GAP_STEPS = SimStep.ticks(1.0);

    /** The steps from a shed's tell to its eel slipping out (the coil's {@code shed.tell}); 0 before the first watch. */
    private int shedSteps;
    /** Whether the level has the finale's cruisers (Level 28): its shaft, breakout and flak. */
    private boolean finale;
    /** What the last watch saw: the clock held after the kill, the ship under water, the cruisers on the screen. */
    private boolean wasHolding;

    private boolean wasUnderwater;
    /**
     * Step I2: the lip of the shaft's mouth (the level's breakout; empty: the breakout sounds as the medium turns) and
     * whether the ship has breached it in this attempt.
     */
    private Optional<BreachLine> lip = Optional.empty();

    private boolean breached;
    private boolean cruisersPresent;
    private boolean watched;
    /** This step, and per cruiser the step of its last point-defence shot. */
    private long now;

    private final long[] lastDefence = {Long.MIN_VALUE / 2, Long.MIN_VALUE / 2};

    MawCues(SoundMix mix) {
        super(mix);
    }

    /** Step I2: the breakout sounds as the ship breaches this lip, with the burst ({@link BreachLine}). */
    void breach(Optional<BreachLine> line) {
        lip = line;
        breached = false;
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.of(
                SimEvents.Type.SAC_SWELL,
                SimEvents.Type.THROAT_GLOW,
                SimEvents.Type.COIL_SWEEP_TELL,
                SimEvents.Type.COIL_SWEEP,
                SimEvents.Type.MAW_DIVE,
                SimEvents.Type.LUNGE,
                SimEvents.Type.IMPLANT_SPARK,
                SimEvents.Type.IMPLANT_ARC,
                SimEvents.Type.SHED_TELL,
                SimEvents.Type.CRUISER_RISE,
                SimEvents.Type.BROADSIDE,
                SimEvents.Type.POINT_DEFENCE);
    }

    @Override
    void begin(long now) {
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        SimEvents.Type type = events.type(i);
        switch (type) {
            // The plate flares for the shed's tell, then the eel slips out: the tear as it does.
            case SHED_TELL -> later(Sfx.MAW_SHED, CUE_LEVEL, pitch(0.04), pan, shedSteps);
            case CRUISER_RISE -> bank.play(Sfx.CRUISER_RISE, CUE_LEVEL, 1, 0);
            case POINT_DEFENCE -> {
                int side = Math.clamp(events.value(i), 0, 1);
                if (defenceDue(lastDefence[side], now)) {
                    bank.play(Sfx.CRUISER_PD, CUE_LEVEL, pitch(0.04), pan);
                }
                lastDefence[side] = now;
            }
            // The dive and the lunge at their files' pitch; the others varied a little.
            case MAW_DIVE, LUNGE -> bank.play(sound(type), CUE_LEVEL, 1, pan);
            default -> bank.play(sound(type), CUE_LEVEL, pitch(0.03), pan);
        }
    }

    /** The sound an event of this family plays (the shed's later, the point defence's at a burst's start). */
    static Sfx sound(SimEvents.Type type) {
        return switch (type) {
            case SAC_SWELL -> Sfx.MAW_GILL;
            case THROAT_GLOW -> Sfx.MAW_THROAT;
            case COIL_SWEEP_TELL -> Sfx.MAW_SWEEP_GROAN;
            case COIL_SWEEP -> Sfx.MAW_SWEEP_SWING;
            case MAW_DIVE -> Sfx.MAW_DIVE;
            case LUNGE -> Sfx.MAW_LUNGE;
            case IMPLANT_SPARK -> Sfx.IMPLANT_SPARK;
            case IMPLANT_ARC -> Sfx.IMPLANT_ZAP;
            case SHED_TELL -> Sfx.MAW_SHED;
            case CRUISER_RISE -> Sfx.CRUISER_RISE;
            case BROADSIDE -> Sfx.CRUISER_BROADSIDE;
            case POINT_DEFENCE -> Sfx.CRUISER_PD;
            default -> throw new IllegalArgumentException("no Level 28 sound for " + type);
        };
    }

    /** Whether a point-defence shot at step {@code now} starts a new burst after the last shot at {@code last}. */
    static boolean defenceDue(long last, long now) {
        return now - last >= PD_GAP_STEPS || now < last;
    }

    /**
     * A lob burst ({@code MORTAR_IMPACT}): while the cruisers are on the screen it is a flak shell's airburst, which
     * plays the flak's burst; false (nothing played) otherwise, a mortar's blob.
     */
    boolean flakImpact(float pan) {
        if (!cruisersPresent) {
            return false;
        }
        bank.play(Sfx.CRUISER_FLAK, CUE_LEVEL, pitch(0.05), pan);
        return true;
    }

    /** The steps from a coiled boss's shed tell to its eel slipping out; 0 for a level without one. */
    static int shedSteps(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            var shed = piece.boss().flatMap(boss -> boss.coil()).flatMap(CoilSpec::shed);
            if (shed.isPresent()) {
                return SimStep.ticks(shed.get().tellSeconds());
            }
        }
        return 0;
    }

    /**
     * Once a step: in the finale's level the climb up the shaft as the clock resumes after the kill's hold (once, the
     * step its hold ends) and the breakout as the ship breaches the lip of the shaft's mouth (step I2: the burst's step;
     * without a lip as the medium turns from water to air); whether the cruisers are on the screen
     * (for {@link #flakImpact}).
     */
    void watch(Sortie sortie) {
        if (!watched) {
            watched = true;
            shedSteps = shedSteps(sortie);
            finale = sortie.cruisers().isPresent();
            wasHolding = sortie.holdingAfterBoss();
            wasUnderwater = sortie.underwater();
        }
        boolean holding = sortie.holdingAfterBoss();
        boolean underwater = sortie.underwater();
        boolean flying = sortie.flying() && !sortie.complete() && !mix.resync;
        if (finale && flying && wasHolding && !holding && coilDown(sortie)) {
            bank.play(Sfx.SHAFT_CLIMB, CUE_LEVEL, 1, 0);
        }
        if (lip.isPresent()) {
            BreachLine line = lip.get();
            double scroll = sortie.groundScroll();
            if (line.ahead(scroll)) {
                breached = false;
            } else if (finale
                    && flying
                    && !breached
                    && line.breached(sortie.ship().x(), sortie.ship().y(), scroll)) {
                breached = true;
                bank.play(Sfx.BREAKOUT, CUE_LEVEL, 1, 0);
            }
        } else if (finale && flying && wasUnderwater && !underwater) {
            bank.play(Sfx.BREAKOUT, CUE_LEVEL, 1, 0);
        }
        wasHolding = holding;
        wasUnderwater = underwater;
        CruiserRun run = sortie.cruisers().orElse(null);
        cruisersPresent = run != null && run.present(levelTick(sortie));
    }

    /** Whether a coiled boss of the sortie is destroyed (its kill's hold, not a restart, ends the hold). */
    static boolean coilDown(Sortie sortie) {
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.coil().isPresent() && piece.destroyed()) {
                return true;
            }
        }
        return false;
    }

    /** The level clock's step now (as the cruisers' looks read it). */
    static int levelTick(Sortie sortie) {
        return (int) Math.floor(sortie.levelSeconds() * SimStep.PER_SECOND + 1e-6);
    }

    /** Whether the cruisers were on the screen at the last watch (for the tests). */
    boolean cruisersPresent() {
        return cruisersPresent;
    }
}
