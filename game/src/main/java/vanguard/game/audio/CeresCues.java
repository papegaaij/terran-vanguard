package vanguard.game.audio;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.EscortSwap;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part B: Level 29's sounds for {@link FlightSounds} (design/audio/sfx, Act 5 sounds; step AUD-B's round 62
 * proposals, their a variants until round 62 picks, levelled in their files and played as they are): the Talon's whine
 * as it swoops in (once per kind, spaced) and its twin crack; a squad leader's death squelch; the SAM Nest's hatch with
 * its lock beep over the salvo's tell (timed, as its look, from its last salvo: the simulation has no read-out of a
 * gun's time to its shot) and its wake; the turning station's relays and klaxon at the first wake (Block C declaring);
 * an Asteroid Mite's tell and leap; a clamp gun's shot and its clamp springing open as it dies; at the escort swap the
 * Ember's launch from the tender's hold and the loaned Bulwark's departure.
 */
final class CeresCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.WOKE,
            SimEvents.Type.SQUAD_SCATTER,
            SimEvents.Type.RIDER_TELL,
            SimEvents.Type.RIDER_LEAP,
            SimEvents.Type.ESCORT_SWAP);

    static final String TALON = "talon";
    static final String NEST = "sam-nest";
    static final String CLAMP = "clamp-gun";

    /** The cues are levelled in their files (step AUD-B) and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** A Talon's whine at most this often, steps (a wing swooping in is one whine). */
    static final int WHINE_GAP_STEPS = SimStep.ticks(0.6);
    /** The station's klaxon after its relays, steps (the lights switch, then the alarm). */
    static final int KLAXON_DELAY_STEPS = SimStep.ticks(0.5);

    private static final int SLOTS = 16;

    private boolean[] talons;
    private boolean[] nests;
    private boolean[] clamps;
    /** Whether the station has turned in this attempt (its first wake). */
    private boolean turned;

    private long now;
    private long lastWhine = Long.MIN_VALUE / 2;
    /** The nests' clocks: per nest (serial, -1 free) its last salvo and when it came into view or woke. */
    private final int[] nestSerial = new int[SLOTS];

    private final long[] nestShot = new long[SLOTS];
    private final long[] nestSeen = new long[SLOTS];
    /** The Talons seen (serials, a ring): a new one swooping in whines. */
    private final int[] talonSeen = new int[SLOTS];

    private int nextTalon;
    private int nextNest;

    CeresCues(SoundMix mix) {
        super(mix);
        reset();
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    private void reset() {
        turned = false;
        lastWhine = Long.MIN_VALUE / 2;
        Arrays.fill(nestSerial, -1);
        Arrays.fill(talonSeen, -1);
        nextTalon = 0;
        nextNest = 0;
    }

    /** Notes the level's kinds: which are the Talon, the SAM Nest and the clamp gun. */
    void noteKinds(List<EnemySpec> kinds) {
        talons = new boolean[kinds.size()];
        nests = new boolean[kinds.size()];
        clamps = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            // M10 part B, step B4: the Chimera (the Talon's airframe) whines and cracks as the Talon until round 74's
            // QB-2 pick (its own whine and shot, a and b, in the round).
            talons[k] = slug.equals(TALON) || slug.equals(CloudDiverCues.CHIMERA);
            nests[k] = slug.equals(NEST);
            clamps[k] = slug.equals(CLAMP);
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    @Override
    void begin(long now) {
        if (now < this.now) {
            // A retry or a checkpoint: the station turns again.
            reset();
        }
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case WOKE -> {
                if (talons == null && mix.watched != null) {
                    noteKinds(mix.watched.enemyKinds());
                }
                if (!is(nests, events.value(i))) {
                    // Another dormant unit (Level 29's clamp guns until the halt) wakes without the nest's spin-up.
                    return;
                }
                if (!turned) {
                    // The first nest's wake is the station turning: its relays slam, then its klaxon.
                    turned = true;
                    bank.play(Sfx.STATION_RELAYS, CUE_LEVEL, 1, 0);
                    later(Sfx.STATION_KLAXON, CUE_LEVEL, 1, 0, KLAXON_DELAY_STEPS);
                }
                bank.play(Sfx.NEST_WAKE, CUE_LEVEL, pitch(0.05), pan);
            }
            case SQUAD_SCATTER -> bank.play(Sfx.LEADER_SQUELCH, CUE_LEVEL, pitch(0.03), pan);
            case RIDER_TELL -> bank.play(Sfx.MITE_TELL, CUE_LEVEL, pitch(0.06), pan);
            case RIDER_LEAP -> bank.play(Sfx.MITE_LEAP, CUE_LEVEL, pitch(0.06), pan);
            case ESCORT_SWAP -> {
                int kind = events.value(i);
                if (kind == EscortSwap.JOINS || kind == EscortSwap.LEAVES) {
                    bank.play(Sfx.EMBER_LAUNCH, CUE_LEVEL, 1, pan);
                }
                Sortie watched = mix.watched;
                if (kind != EscortSwap.EMPTY
                        && watched != null
                        && watched.bulwark()
                                .map(bulwark -> !bulwark.destroyed())
                                .orElse(false)) {
                    bank.play(Sfx.BULWARK_DEPART, CUE_LEVEL, 1, 0);
                }
            }
            default -> {}
        }
    }

    /** An enemy of ours fired: a Talon's twin crack, a clamp gun's shot; false for another unit (its usual shot). */
    boolean fired(Enemy unit, float pan) {
        if (unit == null) {
            return false;
        }
        if (is(talons, unit.kind())) {
            bank.play(Sfx.TALON_SHOT, CUE_LEVEL, pitch(0.05), pan);
            return true;
        }
        if (is(clamps, unit.kind())) {
            bank.play(Sfx.CLAMP_GUN_SHOT, CUE_LEVEL, pitch(0.05), pan);
            return true;
        }
        return false;
    }

    /** An enemy of kind {@code kind} was destroyed: a clamp gun's clamp springs open over its burst. */
    void destroyed(int kind, float pan) {
        if (is(clamps, kind)) {
            bank.play(Sfx.CLAMP_RELEASE, CUE_LEVEL, pitch(0.03), pan);
        }
    }

    /** A homing missile was launched at (x, y): the salvo's start of the nest it left, for its next hatch. */
    void launched(Sortie sortie, double x, double y) {
        Enemy nest = nearest(sortie, x, y);
        if (nest == null) {
            return;
        }
        int slot = slotOf(nest.serial());
        EnemyGun gun = nest.spec().gun().orElse(null);
        if (slot >= 0 && gun != null && now - nestShot[slot] > SimStep.ticks(gun.intervalSeconds() / 2)) {
            nestShot[slot] = now;
        }
    }

    private Enemy nearest(Sortie sortie, double x, double y) {
        Enemy found = null;
        double best = 24;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (!is(nests, enemy.kind())) {
                continue;
            }
            double distance = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (distance <= best) {
                best = distance;
                found = enemy;
            }
        }
        return found;
    }

    private int slotOf(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (nestSerial[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    private boolean seen(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (talonSeen[s] == serial) {
                return true;
            }
        }
        return false;
    }

    /** Once a step: a Talon swooping into view whines; a nest whose hatch opens for its salvo clunks and beeps. */
    void watch(Sortie sortie) {
        if (talons == null) {
            noteKinds(sortie.enemyKinds());
        }
        if (!sortie.flying() || sortie.complete()) {
            return;
        }
        long tick = sortie.tick();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.MISSILE_LAUNCHED) {
                launched(sortie, events.x(i), events.y(i));
            }
        }
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (is(talons, enemy.kind()) && !seen(enemy.serial())) {
                talonSeen[nextTalon] = enemy.serial();
                nextTalon = (nextTalon + 1) % SLOTS;
                if (tick - lastWhine >= WHINE_GAP_STEPS) {
                    bank.play(Sfx.TALON_WHINE, CUE_LEVEL, pitch(0.05), pan(enemy.renderX(1)));
                    lastWhine = tick;
                }
            }
            if (!is(nests, enemy.kind()) || enemy.dormant() || enemy.waking() || enemy.empStunned()) {
                continue;
            }
            int slot = slotOf(enemy.serial());
            if (slot < 0) {
                slot = nextNest;
                nextNest = (nextNest + 1) % SLOTS;
                nestSerial[slot] = enemy.serial();
                nestSeen[slot] = tick;
                nestShot[slot] = Long.MIN_VALUE / 2;
            }
            EnemyGun gun = enemy.spec().gun().orElse(null);
            if (gun == null || gun.homing().isEmpty()) {
                continue;
            }
            long due = nestShot[slot] > Long.MIN_VALUE / 2
                    ? nestShot[slot] + SimStep.ticks(gun.intervalSeconds())
                    : nestSeen[slot] + SimStep.ticks(gun.firstShotDelay());
            double close = Math.hypot(
                    sortie.ship().renderX(1) - enemy.renderX(1), sortie.ship().renderY(1) - enemy.renderY(1));
            if (due - tick == SimStep.ticks(gun.homing().get().hatchSeconds())
                    && close >= gun.homing().get().holdWithin()) {
                float at = pan(enemy.renderX(1));
                bank.play(Sfx.NEST_HATCH, CUE_LEVEL, pitch(0.04), at);
                bank.play(Sfx.NEST_LOCK, CUE_LEVEL, 1, at);
            }
        }
    }
}
