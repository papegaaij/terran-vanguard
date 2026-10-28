package vanguard.game.audio;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part C: Level 30's sounds for {@link FlightSounds} (design/audio/sfx, Act 5 sounds; round 60's picks and step
 * AUD-C's round 63 proposals, their a variants until the round's picks, levelled in their files and played as they are
 * until part D's production mix):
 *
 * <ul>
 *   <li>the line mines: each laid ({@link Sfx#LINE_MINE_DROP}, {@code MINE_DROPPED} value 1) and arming its arm time later
 *       ({@link Sfx#LINE_MINE_ARM}), the proximity blink's rising ticks ({@link Sfx#MINE_BLINK}), a burst by proximity, shot
 *       or touch ({@link Sfx#MINE_BURST}) and a chain's ripple ({@link Sfx#MINE_CHAIN}, once a chain: its first set-off
 *       mine bursts one hop, 0.08 s, after the shot one);
 *   <li>the Gilded Gunship: its ring's thump ({@link Sfx#GUNSHIP_RING}), its belly doors before each Dart stream
 *       ({@link Sfx#GUNSHIP_DOORS}) and each Dart's launch ({@link Sfx#DART_LAUNCH}); its engine, the Ascendancy turbine,
 *       one loop for the nearest on the screen ({@link Sfx#GUNSHIP_ENGINE}), the Minelayer's drone the same turbine lower
 *       ({@link Sfx#MINELAYER_DRONE});
 *   <li>the Rail Serpent: a turret car's shot ({@link Sfx#SERPENT_SHOT}), the train's rumble for the nearest on the screen
 *       ({@link Sfx#SERPENT_RUMBLE}) and a cut's snap and power-down ({@link Sfx#SERPENT_SPLIT}, {@code CHAIN_WRECK});
 *   <li>a lane buoy's strobe as it passes ({@link Sfx#BUOY_TICK}, from the backdrop's draw).
 * </ul>
 */
final class MinefieldCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.MINE_BLINK,
            SimEvents.Type.MINE_CHAINED,
            SimEvents.Type.LAUNCHED,
            SimEvents.Type.CHAIN_WRECK);

    static final String LAYER = "minelayer";
    static final String GUNSHIP = "gilded-gunship";
    static final String SERPENT = "rail-serpent";
    static final String TURRET = SERPENT + "-turret";

    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The turbine's pitch for the Gunship (the recorded one) and the Minelayer's lower drone. */
    static final float GUNSHIP_PITCH = 1f;

    static final float LAYER_PITCH = 0.8f;
    /** A chain's ripple sounds once: set-offs within this many steps of the last share it. */
    static final int CHAIN_GAP_STEPS = SimStep.ticks(0.5);
    /** A launch this long after the one before near the same Gunship opens its doors again (a new stream). */
    static final int STREAM_GAP_STEPS = SimStep.ticks(1);

    private static final int SLOTS = 16;

    private boolean[] layers;
    private boolean[] gunships;
    private boolean[] serpents;
    private boolean[] turrets;
    /** Whether the level's units lay line mines (its mine events are theirs). */
    private boolean lines;
    /** The line mines' arm time (the layer's gun), steps. */
    private int armSteps;

    private long now;
    private long lastChain = Long.MIN_VALUE / 2;
    /** Per Gunship (by serial) its last launch, for its doors. */
    private final int[] carrier = new int[SLOTS];

    private final long[] carrierLaunch = new long[SLOTS];
    private int nextCarrier;

    private float engineLevel;
    private float droneLevel;
    private float rumbleLevel;

    MinefieldCues(SoundMix mix) {
        super(mix);
        java.util.Arrays.fill(carrier, -1);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The level's kinds: which are ours, and the line mines' arm time. */
    void noteKinds(List<EnemySpec> kinds) {
        layers = new boolean[kinds.size()];
        gunships = new boolean[kinds.size()];
        serpents = new boolean[kinds.size()];
        turrets = new boolean[kinds.size()];
        lines = false;
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            layers[k] = slug.equals(LAYER);
            gunships[k] = slug.equals(GUNSHIP);
            serpents[k] = slug.equals(SERPENT) || slug.startsWith(SERPENT + "-");
            turrets[k] = slug.equals(TURRET);
            var mine = kinds.get(k).gun().flatMap(EnemyGun::mine);
            if (mine.flatMap(EnemyGun.MineSpec::line).isPresent()) {
                lines = true;
                armSteps = SimStep.ticks(mine.get().armSeconds());
            }
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    private void noted() {
        if (layers == null && mix.watched != null) {
            noteKinds(mix.watched.enemyKinds());
        }
    }

    @Override
    void begin(long now) {
        if (now < this.now) {
            // A retry or a checkpoint.
            lastChain = Long.MIN_VALUE / 2;
            java.util.Arrays.fill(carrier, -1);
        }
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        noted();
        switch (events.type(i)) {
            case MINE_BLINK -> bank.play(Sfx.MINE_BLINK, CUE_LEVEL, pitch(0.03), pan);
            case MINE_CHAINED -> {
                if (now - lastChain >= CHAIN_GAP_STEPS || now < lastChain) {
                    bank.play(Sfx.MINE_CHAIN, CUE_LEVEL, pitch(0.04), pan);
                }
                lastChain = now;
            }
            case LAUNCHED -> launched(events.x(i), events.y(i), pan);
            case CHAIN_WRECK -> bank.play(Sfx.SERPENT_SPLIT, CUE_LEVEL, pitch(0.04), pan);
            default -> {}
        }
    }

    /** A Dart left the Gunship at (x, y): the doors first if a new stream starts, its launch thump. */
    private void launched(double x, double y, float pan) {
        Enemy gunship = mix.watched == null ? null : nearest(mix.watched, x, y);
        int unit = gunship == null ? -1 : gunship.serial();
        int slot = -1;
        for (int s = 0; s < SLOTS; s++) {
            if (carrier[s] == unit) {
                slot = s;
            }
        }
        if (slot < 0) {
            slot = nextCarrier;
            nextCarrier = (nextCarrier + 1) % SLOTS;
            carrier[slot] = unit;
            carrierLaunch[slot] = Long.MIN_VALUE / 2;
        }
        if (now - carrierLaunch[slot] > STREAM_GAP_STEPS || now < carrierLaunch[slot]) {
            bank.play(Sfx.GUNSHIP_DOORS, CUE_LEVEL, pitch(0.03), pan);
        }
        carrierLaunch[slot] = now;
        bank.play(Sfx.DART_LAUNCH, CUE_LEVEL, pitch(0.06), pan);
    }

    private Enemy nearest(Sortie sortie, double x, double y) {
        Enemy found = null;
        double best = 8;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (!is(gunships, enemy.kind())) {
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

    /**
     * A mine event in a level with line mines ({@code MINE_DROPPED}, {@code MINE_BURST}, {@code MINE_DESTROYED}, with
     * its value): their own sounds; false (nothing played) in a level without line mines.
     */
    boolean mine(SimEvents.Type type, int value, float pan) {
        noted();
        if (!lines) {
            return false;
        }
        switch (type) {
            case MINE_DROPPED -> {
                if (value == 1) {
                    bank.play(Sfx.LINE_MINE_DROP, CUE_LEVEL, pitch(0.05), pan);
                    later(Sfx.LINE_MINE_ARM, CUE_LEVEL, pitch(0.03), pan, Math.max(1, armSteps));
                }
            }
            case MINE_BURST, MINE_DESTROYED -> bank.play(Sfx.MINE_BURST, CUE_LEVEL, pitch(0.05), pan);
            default -> {
                return false;
            }
        }
        return true;
    }

    /** An enemy of ours fired: a Gunship's ring, a turret car's shot; false for another unit (its usual shot). */
    boolean fired(Enemy unit, float pan) {
        noted();
        if (unit == null) {
            return false;
        }
        if (is(gunships, unit.kind())) {
            bank.play(Sfx.GUNSHIP_RING, CUE_LEVEL, pitch(0.04), pan);
            return true;
        }
        if (is(turrets, unit.kind())) {
            bank.play(Sfx.SERPENT_SHOT, CUE_LEVEL, pitch(0.05), pan);
            return true;
        }
        return false;
    }

    /** A lane buoy's strobe flashed at play-field x {@code x}. */
    void buoyTick(double x) {
        bank.play(Sfx.BUOY_TICK, CUE_LEVEL, pitch(0.02), pan(x));
    }

    /** Once a step: the loops for the nearest Gunship, Minelayer and Rail Serpent on the screen. */
    void watch(Sortie sortie) {
        if (layers == null) {
            noteKinds(sortie.enemyKinds());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double engine = Double.POSITIVE_INFINITY;
        double drone = Double.POSITIVE_INFINITY;
        double rumble = Double.POSITIVE_INFINITY;
        float enginePan = 0;
        float dronePan = 0;
        float rumblePan = 0;
        for (int e = 0; flying && e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            int kind = enemy.kind();
            if (!is(gunships, kind) && !is(layers, kind) && !is(serpents, kind)) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (!onScreen(x, y)) {
                continue;
            }
            double distance = Math.hypot(x - shipX, y - shipY);
            if (is(gunships, kind) && distance < engine) {
                engine = distance;
                enginePan = pan(x);
            } else if (is(layers, kind) && distance < drone) {
                drone = distance;
                dronePan = pan(x);
            } else if (is(serpents, kind) && distance < rumble) {
                rumble = distance;
                rumblePan = pan(x);
            }
        }
        float on = FlightSounds.UNIT_LOOP_LEVEL;
        engineLevel = fadeLoop(
                Sfx.GUNSHIP_ENGINE, engineLevel, engine < Double.POSITIVE_INFINITY ? on : 0, enginePan, GUNSHIP_PITCH);
        droneLevel = fadeLoop(
                Sfx.MINELAYER_DRONE, droneLevel, drone < Double.POSITIVE_INFINITY ? on : 0, dronePan, LAYER_PITCH);
        rumbleLevel = fadeLoop(Sfx.SERPENT_RUMBLE, rumbleLevel, rumble < Double.POSITIVE_INFINITY ? on : 0, rumblePan);
    }

    /** Whether (x, y) is on the play field. */
    static boolean onScreen(double x, double y) {
        return x >= 0 && x <= PlayField.WIDTH && y >= 0 && y <= PlayField.HEIGHT;
    }

    /** A loop stopped from outside (the level screen closing): its level is none. */
    void silence(Sfx loop) {
        switch (loop) {
            case GUNSHIP_ENGINE -> engineLevel = 0;
            case MINELAYER_DRONE -> droneLevel = 0;
            case SERPENT_RUMBLE -> rumbleLevel = 0;
            default -> {}
        }
    }
}
