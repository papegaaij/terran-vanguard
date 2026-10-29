package vanguard.game.audio;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part E: Level 32's sounds for {@link FlightSounds} (design/audio/sfx, Act 5 sounds; step AUD-E's round 65
 * proposals, their a variants until the round's picks, levelled in their files and played as they are until the
 * production mix):
 *
 * <ul>
 *   <li>the Sentinel Tower: its sweep's telegraph ({@link Sfx#SENTINEL_CHARGE}; a full turn's from 0.2 s into its 1.0 s
 *       telegraph, the reach ring drawn first), its live beam for the nearest sweeping tower ({@link Sfx#SENTINEL_BEAM},
 *       stopped at once by the EMP's stun) with the head's servo under it ({@link Sfx#SENTINEL_SERVO}, lower on a full
 *       turn), its death over the {@code medium} rung ({@link Sfx#SENTINEL_DEATH});
 *   <li>the Strider: each footfall ({@link Sfx#STRIDER_STEP}, two a walk cycle, at most one per 0.25 s a walker), the
 *       torso starting a turn toward the ship ({@link Sfx#STRIDER_SERVO}, at most once per 0.5 s a walker), each fan of
 *       its twin cannons ({@link Sfx#STRIDER_CANNON}, once a fan), its pod's hatch over the salvo's 0.4 s tell (round
 *       62's SAM Nest hatch, {@link Sfx#NEST_HATCH}; the salvo's launch is the homing diamonds', {@link BeltCues}), a leg
 *       lost: the snap and, with it, the topple's fall, whose crash lands on the crush 1.4 s later ({@link
 *       Sfx#STRIDER_SNAP}, {@link Sfx#STRIDER_FALL}; a stun does not stop it);
 *   <li>a blast door's lock destroyed: the doors grinding open ({@link Sfx#DOOR_GRIND}); a mining charge's burst ({@link
 *       Sfx#CHARGE_BLAST}).
 * </ul>
 *
 * <p>The torso's turn and the hatch follow the same model as the looks (DeepCoreLooks): the torso turns toward the ship
 * at 120 degrees/s; the salvo's clock runs from the walker's first step on the screen (half its interval) and each
 * launch at its pod.
 */
final class DeepCoreCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.TOPPLE_TELL,
            SimEvents.Type.TOPPLE_FALL,
            SimEvents.Type.TOPPLE_CRUSH,
            SimEvents.Type.CHARGE_BLAST,
            SimEvents.Type.CACHE_OPENED);

    static final String TOWER = "sentinel-tower";
    static final String STRIDER = "strider";

    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** A full turn's charge starts this far into its telegraph (the reach ring first). */
    static final double FULL_TURN_CHARGE_DELAY = 0.2;
    /** The servo's pitch on a full turn. */
    static final float FULL_TURN_SERVO_PITCH = 0.85f;
    /** A walker's footfalls at most this often, steps. */
    static final int STEP_GAP = SimStep.ticks(0.25);
    /** A walker's torso servo at most this often, steps. */
    static final int SERVO_GAP = SimStep.ticks(0.5);
    /** The torso turns toward the ship at most this fast, radians a step (DeepCoreLooks' 120 degrees/s). */
    static final double TORSO_TURN = Math.toRadians(120) / SimStep.PER_SECOND;
    /** A shot within this of a walker's centre is its fan, within {@link #POD_REACH} of its pod its salvo. */
    static final double REACH = 6;

    static final double POD_REACH = 12;
    /** Footfalls a walk cycle (two feet). */
    static final int STEPS_A_CYCLE = 2;

    private static final int SLOTS = 16;
    private static final long NEVER = Long.MIN_VALUE / 2;

    private boolean[] towers;
    private boolean[] striders;
    private long now;
    private long lastFan = NEVER;

    private final int[] serial = new int[SLOTS];
    private final boolean[] seen = new boolean[SLOTS];
    private final long[] firstSeen = new long[SLOTS];
    private final long[] lastStep = new long[SLOTS];
    private final long[] footfall = new long[SLOTS];
    private final long[] lastServo = new long[SLOTS];
    private final double[] torso = new double[SLOTS];
    private final boolean[] turning = new boolean[SLOTS];
    private final long[] lastSalvo = new long[SLOTS];
    private final long[] hatchSounded = new long[SLOTS];

    private float beamLevel;
    private float servoLevel;

    DeepCoreCues(SoundMix mix) {
        super(mix);
        Arrays.fill(serial, -1);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The level's kinds: which are ours. */
    void noteKinds(List<EnemySpec> kinds) {
        towers = new boolean[kinds.size()];
        striders = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            towers[k] = slug.equals(TOWER);
            striders[k] = slug.equals(STRIDER);
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    private void noted() {
        if (towers == null && mix.watched != null) {
            noteKinds(mix.watched.enemyKinds());
        }
    }

    @Override
    void begin(long now) {
        if (now < this.now) {
            // A retry or a checkpoint.
            Arrays.fill(serial, -1);
            lastFan = NEVER;
        }
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        noted();
        switch (events.type(i)) {
            case TOPPLE_TELL -> {
                // The snap with the fall: its groan covers the stagger and the fall, its crash lands on the crush.
                bank.play(Sfx.STRIDER_SNAP, CUE_LEVEL, pitch(0.04), pan);
                bank.play(Sfx.STRIDER_FALL, CUE_LEVEL, 1, pan);
            }
            case CHARGE_BLAST -> bank.play(Sfx.CHARGE_BLAST, CUE_LEVEL, pitch(0.05), pan);
            case TOPPLE_FALL, TOPPLE_CRUSH, CACHE_OPENED -> {
                // The fall's file holds the crash; the cache's doors grind as their lock is destroyed (watch).
            }
            default -> {}
        }
    }

    /**
     * A sweep's telegraph or beam at a unit of kind {@code kind} (UnitCues' events): a Sentinel's charge (its beam is a
     * loop, {@link #watch}); false for any other unit (the Mantis's sounds).
     */
    boolean sweep(SimEvents.Type type, int kind, double x, double y, float pan) {
        noted();
        if (!is(towers, kind)) {
            return false;
        }
        if (type == SimEvents.Type.SWEEP_TELEGRAPH) {
            Enemy tower = at(x, y, REACH, towers);
            if (tower != null && tower.sweepFullTurn()) {
                later(Sfx.SENTINEL_CHARGE, CUE_LEVEL, pitch(0.03), pan, SimStep.ticks(FULL_TURN_CHARGE_DELAY));
            } else {
                bank.play(Sfx.SENTINEL_CHARGE, CUE_LEVEL, pitch(0.03), pan);
            }
        }
        return true;
    }

    /** A unit of kind {@code kind} died: a Sentinel's crash and its head's fall over its rung. */
    void destroyed(int kind, float pan) {
        noted();
        if (is(towers, kind)) {
            bank.play(Sfx.SENTINEL_DEATH, CUE_LEVEL, pitch(0.03), pan);
        }
    }

    /**
     * A shot at (x, y) (UnitCues' {@code ENEMY_FIRED}): a Strider's fan plays its twin cannons once, its pod's salvo
     * nothing of its own (the launch is the diamonds'); false when no Strider fired it.
     */
    boolean fired(double x, double y, float pan) {
        noted();
        Sortie sortie = mix.watched;
        if (sortie == null || striders == null) {
            return false;
        }
        if (at(x, y, REACH, striders) != null) {
            if (lastFan != now) {
                bank.play(Sfx.STRIDER_CANNON, CUE_LEVEL, pitch(0.04), pan);
                lastFan = now;
            }
            return true;
        }
        return atPod(sortie, x, y) != null;
    }

    /** The unit of those kinds whose centre lies within {@code reach} of (x, y), null for none. */
    private Enemy at(double x, double y, double reach, boolean[] kinds) {
        Sortie sortie = mix.watched;
        if (sortie == null) {
            return null;
        }
        Enemy found = null;
        double best = reach;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            double distance = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (is(kinds, enemy.kind()) && distance <= best) {
                best = distance;
                found = enemy;
            }
        }
        return found;
    }

    /** The Strider whose pod lies within {@link #POD_REACH} of (x, y), null for none. */
    private Enemy atPod(Sortie sortie, double x, double y) {
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            int p = pod(enemy);
            if (is(striders, enemy.kind())
                    && p >= 0
                    && Math.hypot(enemy.partX(p) - x, enemy.partY(p) - y) <= POD_REACH) {
                return enemy;
            }
        }
        return null;
    }

    private static int pod(Enemy enemy) {
        for (int p = 0; p < enemy.partCount(); p++) {
            if (enemy.spec().parts().get(p).silencesSpit()) {
                return p;
            }
        }
        return -1;
    }

    /**
     * Once a step: the nearest sweeping Sentinel's beam and servo loops; the Striders' footfalls, torso servo and pod
     * hatch; the blast doors grinding open as their locks are destroyed.
     */
    void watch(Sortie sortie) {
        if (towers == null) {
            noteKinds(sortie.enemyKinds());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double beam = Double.POSITIVE_INFINITY;
        float beamPan = 0;
        boolean fullTurn = false;
        boolean cut = false;
        Arrays.fill(seen, false);
        for (int e = 0; flying && e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (is(towers, enemy.kind())) {
                cut |= enemy.empStunned();
                double distance = Math.hypot(x - shipX, y - shipY);
                if (enemy.sweeping() && distance < beam) {
                    beam = distance;
                    beamPan = pan(x);
                    fullTurn = enemy.sweepFullTurn();
                }
            } else if (is(striders, enemy.kind()) && MinefieldCues.onScreen(x, y)) {
                strider(sortie, enemy, x, y, shipX, shipY);
            }
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!seen[s]) {
                serial[s] = -1;
            }
        }
        float target = beam < Double.POSITIVE_INFINITY ? FlightSounds.UNIT_LOOP_LEVEL : 0;
        if (target == 0 && cut && beamLevel > 0) {
            // The EMP's stun cuts the beam at once.
            bank.stop(Sfx.SENTINEL_BEAM);
            bank.stop(Sfx.SENTINEL_SERVO);
            beamLevel = 0;
            servoLevel = 0;
        }
        beamLevel = fadeLoop(Sfx.SENTINEL_BEAM, beamLevel, target, beamPan);
        servoLevel = fadeLoop(Sfx.SENTINEL_SERVO, servoLevel, target, beamPan, servoPitch(fullTurn));
        if (servoLevel > 0) {
            bank.loopPitch(Sfx.SENTINEL_SERVO, servoPitch(fullTurn));
        }
        if (!mix.resync) {
            doors(sortie);
        }
    }

    /** The servo's pitch: lower on a full turn. */
    static float servoPitch(boolean fullTurn) {
        return fullTurn ? FULL_TURN_SERVO_PITCH : 1;
    }

    /** A Strider's footfalls, torso servo and pod hatch this step. */
    private void strider(Sortie sortie, Enemy enemy, double x, double y, double shipX, double shipY) {
        int s = slot(enemy);
        if (s < 0) {
            return;
        }
        seen[s] = true;
        double stride = enemy.spec().walker().map(EnemySpec.Walker::stride).orElse(32.0);
        long step = footfall(enemy.walked(), stride);
        if (step != footfall[s] && !enemy.toppling()) {
            if (now - lastStep[s] >= STEP_GAP && !mix.resync) {
                bank.play(Sfx.STRIDER_STEP, CUE_LEVEL, pitch(0.05), pan(x));
                lastStep[s] = now;
            }
            footfall[s] = step;
        }
        if (enemy.toppling() || enemy.empStunned()) {
            turning[s] = false;
        } else {
            double bearing = Math.atan2(-(shipX - x), -(shipY - y));
            double delta = Math.abs(Math.IEEEremainder(bearing - torso[s], 2 * Math.PI));
            boolean turns = delta > Math.toRadians(11.25);
            if (turns && !turning[s] && now - lastServo[s] >= SERVO_GAP && !mix.resync) {
                bank.play(Sfx.STRIDER_SERVO, CUE_LEVEL, pitch(0.04), pan(x));
                lastServo[s] = now;
            }
            turning[s] = turns;
            double d = Math.IEEEremainder(bearing - torso[s], 2 * Math.PI);
            torso[s] += Math.abs(d) <= TORSO_TURN ? d : Math.signum(d) * TORSO_TURN;
        }
        hatch(sortie, enemy, s, x);
    }

    /** The footfall a walker is on after walking {@code walked} px with strides of {@code stride} px. */
    static long footfall(double walked, double stride) {
        return (long) Math.floor(walked / stride * STEPS_A_CYCLE);
    }

    private int slot(Enemy enemy) {
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] == enemy.serial()) {
                return s;
            }
        }
        for (int s = 0; s < SLOTS; s++) {
            if (serial[s] < 0) {
                serial[s] = enemy.serial();
                firstSeen[s] = now;
                lastStep[s] = NEVER;
                lastServo[s] = NEVER;
                lastSalvo[s] = NEVER;
                hatchSounded[s] = NEVER;
                footfall[s] = footfall(
                        enemy.walked(),
                        enemy.spec().walker().map(EnemySpec.Walker::stride).orElse(32.0));
                torso[s] = enemy.facing();
                turning[s] = false;
                return s;
            }
        }
        return -1;
    }

    /** The pod's hatch as the salvo's tell starts on its clock (the launches at its pod reset it). */
    private void hatch(Sortie sortie, Enemy enemy, int s, double x) {
        SimEvents events = sortie.events();
        int p = pod(enemy);
        for (int i = 0; p >= 0 && i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.MISSILE_LAUNCHED
                    && Math.hypot(enemy.partX(p) - events.x(i), enemy.partY(p) - events.y(i)) <= POD_REACH) {
                lastSalvo[s] = now;
            }
        }
        EnemyGun gun = enemy.spec().walker().flatMap(EnemySpec.Walker::spit).orElse(null);
        if (p < 0 || !enemy.partAlive(p) || gun == null || gun.homing().isEmpty() || enemy.empStunned()) {
            return;
        }
        double interval = gun.intervalSeconds();
        long due = lastSalvo[s] > NEVER
                ? lastSalvo[s] + SimStep.ticks(interval)
                : firstSeen[s] + SimStep.ticks(interval / 2);
        long until = due - now;
        int tell = SimStep.ticks(gun.homing().get().hatchSeconds());
        if (until > 0 && until <= tell && now - hatchSounded[s] > tell && !mix.resync) {
            bank.play(Sfx.NEST_HATCH, CUE_LEVEL, pitch(0.03), pan(x));
            hatchSounded[s] = now;
        }
    }

    /**
     * The doors of a blast door whose lock was destroyed this step grind open; M9 part G (Level 34): a CDF or Helix cargo
     * container bursts open, a hold clamp unlocks (each of the four, close together as they fall).
     */
    private void doors(Sortie sortie) {
        SimEvents events = sortie.events();
        List<GroundObjectSpec> objects = sortie.script().groundObjects();
        for (int i = 0; i < events.size(); i++) {
            int index = events.value(i);
            if (events.type(i) != SimEvents.Type.GROUND_DESTROYED || index < 0 || index >= objects.size()) {
                continue;
            }
            Sfx sound = breakSound(objects.get(index).look());
            if (sound != null) {
                bank.play(sound, CUE_LEVEL, pitch(0.02), pan(events.x(i)));
            }
        }
    }

    /**
     * The own sound of a ground object of {@code look} destroyed, over the ground break's; null for none (Level 01's
     * plain cargo container keeps the ground break alone: only the container's variants burst).
     */
    static Sfx breakSound(String look) {
        if (look.startsWith("blast-door")) {
            return Sfx.DOOR_GRIND;
        }
        if (look.startsWith(GroundObjectSpec.CARGO_CONTAINER + "-")) {
            return Sfx.CONTAINER_BURST;
        }
        // Round 67's S.2 "dock": a hold clamp's release is Level 29's Dock 9 clamp release.
        return look.startsWith("hold-clamp") ? Sfx.CLAMP_RELEASE : null;
    }

    /** A loop stopped from outside (the level screen closing): its level is none. */
    void silence(Sfx loop) {
        switch (loop) {
            case SENTINEL_BEAM -> beamLevel = 0;
            case SENTINEL_SERVO -> servoLevel = 0;
            default -> {}
        }
    }
}
