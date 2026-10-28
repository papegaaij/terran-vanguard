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
 * flamer and faded in and out over 0.2 s. Its death is the boss's chained death on its own rung ({@link
 * Sfx#WALKER_DEATH}, {@code BossCues}); the wreck's groan ({@link Sfx#WALKER_WRECK_SETTLE}) once as the topple lands; a
 * hit on its open vents a metal hit of its own ({@link Sfx#WALKER_VENT_HIT}, at most one each {@value
 * #VENT_HIT_GAP_SECONDS} s) instead of the plain hit ({@link #ventHit}, asked by {@code UnitCues}).
 *
 * <p>Round 44 (step WR2; round 44's picks: the charge b, the shot and the shockwave a, levelled in their files): the
 * rail lance's charge ({@link Sfx#WALKER_LANCE_CHARGE}) and shot ({@link Sfx#WALKER_LANCE_SHOT}), the stomp's
 * shockwave ({@link Sfx#WALKER_SHOCKWAVE}); the stomp's brace stays a placeholder, the hatch's clank pitched down.
 */
final class WalkerCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.WALKER_STEP,
            SimEvents.Type.POD_HATCH,
            SimEvents.Type.MISSILE_LAUNCHED,
            SimEvents.Type.MISSILE_DESTROYED,
            SimEvents.Type.CONE_TELL,
            SimEvents.Type.CONE_BURN,
            SimEvents.Type.LANCE_CHARGE,
            SimEvents.Type.LANCE_FIRE,
            SimEvents.Type.STOMP_TELL,
            SimEvents.Type.STOMP,
            SimEvents.Type.BOSS_WRECK);

    /** The one-shots are levelled in their files (−19 to −22 dB, as round 41's) and play as they are. */
    static final float WALKER_LEVEL = 1f;
    /** Round 44: the stomp's brace, a placeholder (the hatch's clank) at this pitch. */
    static final float STOMP_TELL_PITCH = 0.7f;
    /** A hit on the open vents sounds at most once each this many s. */
    static final double VENT_HIT_GAP_SECONDS = 0.15;

    static final int VENT_HIT_GAP_STEPS = SimStep.ticks(VENT_HIT_GAP_SECONDS);
    /** A hit this close to the open vents' box counts as on them, px (a shot stops at the hull's edge). */
    static final double VENT_HIT_REACH = 10;
    /** Missiles launched within this many steps of the last launch sound share it. */
    static final int MISSILE_GAP_STEPS = SimStep.ticks(0.1);

    /** The step of the last launch sound, and this step's. */
    private long lastLaunch = Long.MIN_VALUE / 2;

    private long now;
    /** The step of the last vent hit's sound. */
    private long lastVentHit = Long.MIN_VALUE / 2;
    /** The flame loop's level now (its fade). */
    private float flameLevel;

    /** M8 part G: Level 27's sounds, whose torpedoes are the level's homing missiles ({@link VentFieldCues}). */
    private final VentFieldCues ventField;

    /** M9 part A: the Act 5 homing diamonds' launch and fall ({@link BeltCues}). */
    private final BeltCues belt;

    WalkerCues(SoundMix mix, VentFieldCues ventField, BeltCues belt) {
        super(mix);
        this.ventField = ventField;
        this.belt = belt;
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
                // M8 part G: a Depth Hunter's torpedo launches with its volley's report (VentFieldCues.fired).
                // M9 part A: a homing diamond's launch (BeltCues).
                if (!ventField.torpedoes()
                        && !belt.launched(pan, events.x(i), events.y(i))
                        && launchDue(lastLaunch, now)) {
                    bank.play(Sfx.WALKER_MISSILE, WALKER_LEVEL, pitch(0.04), pan);
                    lastLaunch = now;
                }
            }
            case MISSILE_DESTROYED -> {
                // M8 part G: a torpedo's impact or under-water burst (VentFieldCues).
                // M9 part A: a homing diamond's fall (BeltCues).
                if (!ventField.missileGone(events.value(i), pan)
                        && !belt.gone(events.value(i), pan)
                        && events.value(i) == Missiles.SHOT_DOWN) {
                    bank.play(alternate(Sfx.EXPLOSION_TINY_A, Sfx.EXPLOSION_TINY_B), EXPLOSIONS, pitch(0.05), pan);
                }
            }
            case CONE_TELL -> bank.play(Sfx.WALKER_FLAME_TELL, WALKER_LEVEL, pitch(0.02), pan);
            case LANCE_CHARGE -> bank.play(Sfx.WALKER_LANCE_CHARGE, WALKER_LEVEL, pitch(0.02), pan);
            case LANCE_FIRE -> bank.play(Sfx.WALKER_LANCE_SHOT, WALKER_LEVEL, pitch(0.02), pan);
            case STOMP_TELL -> bank.play(Sfx.WALKER_HATCH, WALKER_LEVEL, STOMP_TELL_PITCH * pitch(0.02), pan);
            case STOMP -> bank.play(Sfx.WALKER_SHOCKWAVE, WALKER_LEVEL, pitch(0.02), pan);
            // The topple lands its death's seconds later: the wreck's groan, under the hint-2 crawl.
            case BOSS_WRECK -> later(Sfx.WALKER_WRECK_SETTLE, WALKER_LEVEL, 1, pan, toppleSteps());
            // The burn's roar is the loop (watch).
            default -> {}
        }
    }

    /** The watched walker's topple (its death's seconds) in steps; 2 s without one. */
    private int toppleSteps() {
        Sortie watched = mix.watched;
        if (watched != null) {
            for (int k = 0; k < watched.setPieceCount(); k++) {
                SetPiece piece = watched.setPiece(k);
                if (piece.stride().isPresent()) {
                    return SimStep.ticks(piece.boss().orElseThrow().deathSeconds());
                }
            }
        }
        return SimStep.ticks(2);
    }

    /**
     * Round 43: whether a hit at (x, y) lands on a walker's open vents ({@code UnitCues} then plays nothing of its own):
     * its metal hit plays, at most once each {@value #VENT_HIT_GAP_SECONDS} s.
     */
    boolean ventHit(double x, double y, float pan) {
        Sortie watched = mix.watched;
        if (watched == null) {
            return false;
        }
        for (int k = 0; k < watched.setPieceCount(); k++) {
            SetPiece piece = watched.setPiece(k);
            StrideArena stride = piece.stride().orElse(null);
            if (stride != null && !piece.destroyed() && stride.onOpenVents(x, y, VENT_HIT_REACH)) {
                if (ventHitDue(lastVentHit, now)) {
                    bank.play(Sfx.WALKER_VENT_HIT, WALKER_LEVEL, pitch(0.04), pan);
                    lastVentHit = now;
                }
                return true;
            }
        }
        return false;
    }

    /** Whether a vent hit sounds at step {@code now} after the last one at {@code last}. */
    static boolean ventHitDue(long last, long now) {
        return now - last >= VENT_HIT_GAP_STEPS || now < last;
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
