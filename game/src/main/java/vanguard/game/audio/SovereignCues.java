package vanguard.game.audio;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.RelayFlash;
import vanguard.sim.RingArena;
import vanguard.sim.RingCore;
import vanguard.sim.RingSpec;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part H, Level 35's sounds for {@link FlightSounds} (design/audio/sfx, the Iron Sovereign's and the mass relay's rows;
 * step AUD-H2's round 68 proposals, their a variants until the user's picks):
 *
 * <ul>
 *   <li>the rings: each ring's spin-up as it lights ({@code RING_LIT}, {@link Sfx#SOVEREIGN_SPINUP}; the station's
 *       klaxon with the first), the grind bed while any ring turns ({@link Sfx#SOVEREIGN_RING_TURN}, a loop from the
 *       rings being up to the last lock, the kill or the level's end: {@link #watch}), the inner ring's unlock at its
 *       step's tell and its latch as it holds ({@link Sfx#SOVEREIGN_STEP}, {@link Sfx#SOVEREIGN_HOLD}), a ring's lock
 *       ({@link Sfx#SOVEREIGN_LOCK});
 *   <li>the core: its fan at the shot, its glow's length after the slits light ({@code CORE_GLOW}, {@link
 *       Sfx#SOVEREIGN_FAN}), the klaxon and the shutters over its opening, the spiral's zap at most every 0.1 s while
 *       it runs ({@link Sfx#SOVEREIGN_SPIRAL}), its hex shield's hit, break and refill (the units' shared sounds);
 *   <li>the batteries' rail lines (the Rail Bunker's charge and shot, {@link Sfx#RAIL_CHARGE}, {@link Sfx#RAIL_SHOT});
 *       the SAM racks' hatches and the bays' Darts are {@code WalkerCues}' and {@code BossCues}' (the nest's hatch, the
 *       Dart's launch for a ringed boss);
 *   <li>its death: the cook-offs' chain is its death sound ({@code BossCues}, from the kill); after the last cook-off
 *       the core's implosion, its whoomph 1.15 s in with the {@code huge} rung;
 *   <li>the mass relay: its charge over the telegraph, timed to end at the flash (easy's longer telegraph starts it
 *       later), and its flash's thunder ({@link Sfx#RELAY_CHARGE}, {@link Sfx#RELAY_FLASH}).
 * </ul>
 */
final class SovereignCues extends SoundFamily {
    /** The one-shots sit at their files' levels (levelled by step AUD-H2). */
    static final float CUE_LEVEL = 1f;
    /** The grind bed under the fight. */
    static final float BED_LEVEL = 0.7f;
    /** The spiral's zaps at most this often (its two arms' 10 a second, hard's three). */
    static final int SPIRAL_GAP_STEPS = SimStep.ticks(0.1);
    /** The relay's charge file's length, s: it ends at the flash. */
    static final double RELAY_CHARGE_SECONDS = 1.2;
    /** The implosion's whoomph, s into its file: the {@code huge} rung there. */
    static final double WHOOMPH_SECONDS = 1.15;

    private long now;
    private long lastSpiral = Long.MIN_VALUE / 2;

    SovereignCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.of(
                SimEvents.Type.RING_LIT,
                SimEvents.Type.RING_STEP_TELL,
                SimEvents.Type.RING_HOLD,
                SimEvents.Type.RING_LOCKED,
                SimEvents.Type.RING_COOKOFF,
                SimEvents.Type.CORE_GLOW,
                SimEvents.Type.CORE_OPENING,
                SimEvents.Type.BOSS_LINE_TELEGRAPH,
                SimEvents.Type.BOSS_LINE_FIRED,
                SimEvents.Type.PART_SHIELD_HIT,
                SimEvents.Type.PART_SHIELD_BROKEN,
                SimEvents.Type.PART_SHIELD_REFILLED,
                SimEvents.Type.RELAY_TELEGRAPH,
                SimEvents.Type.RELAY_FIRED,
                SimEvents.Type.CORE_PLAY_TELL,
                SimEvents.Type.CORE_VOLLEY_TELEGRAPH,
                SimEvents.Type.CORE_VOLLEY_FIRED,
                SimEvents.Type.CORE_MINES_THROWN,
                SimEvents.Type.CORE_WRECK_THROWN,
                SimEvents.Type.CORE_WRECK_BROKEN);
    }

    @Override
    void begin(long now) {
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        SimEvents.Type type = events.type(i);
        switch (type) {
            case RING_LIT -> {
                bank.play(Sfx.SOVEREIGN_SPINUP, CUE_LEVEL, pitch(0.03), pan);
                if (events.value(i) == 0) {
                    bank.play(Sfx.SOVEREIGN_KLAXON, CUE_LEVEL, 1, 0);
                }
            }
            case CORE_OPENING -> {
                bank.play(Sfx.SOVEREIGN_KLAXON, CUE_LEVEL, 1, 0);
                bank.play(Sfx.SOVEREIGN_SHUTTERS, CUE_LEVEL, 1, pan);
            }
            // The fan fires at the glow's end.
            case CORE_GLOW -> later(Sfx.SOVEREIGN_FAN, CUE_LEVEL, pitch(0.03), pan, glowSteps());
            case RING_COOKOFF -> {
                if (lastCookoff(events.value(i))) {
                    // After the last cook-off, the core folds in and blows: the huge rung on the implosion's whoomph.
                    int gap = cookoffSteps();
                    later(Sfx.SOVEREIGN_IMPLOSION, CUE_LEVEL, 1, pan, gap);
                    later(Sfx.EXPLOSION_HUGE_A, CUE_LEVEL, 1, pan, gap + SimStep.ticks(WHOOMPH_SECONDS));
                }
            }
            case RELAY_TELEGRAPH -> later(Sfx.RELAY_CHARGE, CUE_LEVEL, 1, pan, relayChargeDelay());
            case RELAY_FIRED -> bank.play(Sfx.RELAY_FLASH, CUE_LEVEL, 1, pan);
            case PART_SHIELD_HIT -> bank.play(Sfx.ENEMY_SHIELD_HIT, CUE_LEVEL, pitch(0.06), pan);
            // Step BOSS70: each playbook attack's tell has its own sound (existing ones, pending a sounds row).
            case CORE_PLAY_TELL -> bank.play(tell(events.value(i)), CUE_LEVEL, pitch(0.03), pan);
            default -> bank.play(sound(type), CUE_LEVEL, pitch(0.03), pan);
        }
    }

    /** The sound an event of this family plays (the fan's, the implosion's and the relay's charge later). */
    static Sfx sound(SimEvents.Type type) {
        return switch (type) {
            case RING_LIT -> Sfx.SOVEREIGN_SPINUP;
            case RING_STEP_TELL -> Sfx.SOVEREIGN_STEP;
            case RING_HOLD -> Sfx.SOVEREIGN_HOLD;
            case RING_LOCKED -> Sfx.SOVEREIGN_LOCK;
            case RING_COOKOFF -> Sfx.SOVEREIGN_IMPLOSION;
            case CORE_GLOW -> Sfx.SOVEREIGN_FAN;
            case CORE_OPENING -> Sfx.SOVEREIGN_SHUTTERS;
            case BOSS_LINE_TELEGRAPH -> Sfx.RAIL_CHARGE;
            case BOSS_LINE_FIRED -> Sfx.RAIL_SHOT;
            case PART_SHIELD_HIT -> Sfx.ENEMY_SHIELD_HIT;
            case PART_SHIELD_BROKEN -> Sfx.ENEMY_SHIELD_BREAK;
            case PART_SHIELD_REFILLED -> Sfx.ENEMY_SHIELD_REFILL;
            case RELAY_TELEGRAPH -> Sfx.RELAY_CHARGE;
            case RELAY_FIRED -> Sfx.RELAY_FLASH;
            case CORE_VOLLEY_TELEGRAPH -> Sfx.RAIL_CHARGE;
            case CORE_VOLLEY_FIRED -> Sfx.RAIL_SHOT;
            case CORE_MINES_THROWN -> Sfx.LINE_MINE_DROP;
            case CORE_WRECK_THROWN -> Sfx.CABLE_SNAP;
            case CORE_WRECK_BROKEN -> Sfx.CRUMBLE_LARGE;
            default -> throw new IllegalArgumentException("no Level 35 sound for " + type);
        };
    }

    /**
     * Step BOSS70: the sound of the core's tell for play {@code play} ({@link RingSpec.Play} ordinal): the spiral's
     * reactor flare the Sentinel's charge, the red petals the SAM nest's lock, the gold bays the Gunship's doors, the
     * amber rim the line mine's blink, the gold wreck the walker wreck's groan.
     */
    static Sfx tell(int play) {
        return switch (RingSpec.Play.values()[play]) {
            case SPIRAL -> Sfx.SENTINEL_CHARGE;
            case VOLLEY -> Sfx.NEST_LOCK;
            case SURGE -> Sfx.GUNSHIP_DOORS;
            case MINES -> Sfx.MINE_BLINK;
            case WRECKS -> Sfx.WALKER_WRECK_SETTLE;
        };
    }

    /** The relay's charge starts this many steps into its telegraph, so it ends at the flash (easy: 0.3 s in). */
    static int relayChargeDelay(double telegraphSeconds) {
        return Math.max(0, SimStep.ticks(telegraphSeconds - RELAY_CHARGE_SECONDS));
    }

    private int relayChargeDelay() {
        Sortie watched = mix.watched;
        RelayFlash relay = watched == null ? null : watched.relay().orElse(null);
        return relay == null ? 0 : relayChargeDelay(relay.spec().telegraphSeconds());
    }

    /** The ringed boss of a sortie, or null (the Iron Sovereign). */
    static SetPiece ringed(Sortie sortie) {
        if (sortie == null) {
            return null;
        }
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.ring().isPresent()) {
                return piece;
            }
        }
        return null;
    }

    private RingSpec spec() {
        SetPiece piece = ringed(mix.watched);
        return piece == null ? null : piece.boss().flatMap(boss -> boss.rings()).orElse(null);
    }

    private int glowSteps() {
        RingSpec spec = spec();
        return SimStep.ticks(
                spec == null ? 0.5 : spec.core().map(RingSpec.Core::glowSeconds).orElse(0.5));
    }

    private int cookoffSteps() {
        RingSpec spec = spec();
        return SimStep.ticks(spec == null ? 0.15 : spec.cookoffSeconds());
    }

    /** Whether a cook-off's value ({@link RingArena#cookoffValue}) is the last battery's: the inner ring's last. */
    private boolean lastCookoff(int value) {
        RingSpec spec = spec();
        if (spec == null) {
            return false;
        }
        int ring = spec.rings().size() - 1;
        int last = spec.rings().get(ring).parts().size() - 1;
        return value == ring * RingArena.COOKOFF_RING + last;
    }

    /**
     * Once a step: the grind bed while the rings turn (up from the spin-up's end, any ring not locked, the station
     * alive); the open core's spiral at most every 0.1 s while it runs.
     */
    void watch(Sortie sortie) {
        SetPiece piece = ringed(sortie);
        if (piece == null) {
            return;
        }
        RingArena ring = piece.ring().orElseThrow();
        boolean turning = piece.present()
                && piece.engaged()
                && !piece.destroyed()
                && !ring.spinningUp()
                && ring.lockedCount() < ring.ringCount()
                && sortie.flying()
                && !sortie.complete();
        if (turning && !bank.looping(Sfx.SOVEREIGN_RING_TURN)) {
            bank.loop(Sfx.SOVEREIGN_RING_TURN, BED_LEVEL);
        } else if (!turning && bank.looping(Sfx.SOVEREIGN_RING_TURN)) {
            bank.stop(Sfx.SOVEREIGN_RING_TURN);
        }
        RingCore core = ring.core().orElse(null);
        long tick = sortie.tick();
        if (core != null
                && core.spiralOn()
                && !piece.destroyed()
                && !mix.resync
                && (tick - lastSpiral >= SPIRAL_GAP_STEPS || tick < lastSpiral)) {
            lastSpiral = tick;
            bank.play(Sfx.SOVEREIGN_SPIRAL, CUE_LEVEL, pitch(0.05), pan(piece.renderX(1)));
        }
    }

    /** The flight's loops are stopped as the level screen closes. */
    void silence(Sfx loop) {
        if (loop == Sfx.SOVEREIGN_RING_TURN && bank.looping(loop)) {
            bank.stop(loop);
        }
    }
}
