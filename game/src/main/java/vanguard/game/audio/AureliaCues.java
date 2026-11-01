package vanguard.game.audio;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Regrowing;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part C, step C4b: Level 37's own sounds and the Mirror Interceptor's and Graft Turret's for {@link FlightSounds}
 * (design/audio/sfx, Act 6 sounds; step AUD-C's round 75 proposals, their a variants until the picks, levelled in their
 * files and played as they are):
 *
 * <ul>
 *   <li>Aurelia's public-address chime as a shuttle pair is called to its pads, with the HOLD FIRE banner ({@link
 *       Sfx#PA_CHIME} at {@code FRIENDLIES_INBOUND}: t=42, 70 and 125);
 *   <li>the growth bursting over the decks at the turn ({@link Sfx#GROWTH_BURST} at the backdrop's {@code growth.t},
 *       t=100), the deck's groan as the city breaks ({@link Sfx#DECK_GROAN} at its {@code fall.t}, t=160) and each lift
 *       balloon tearing open ({@link Sfx#BALLOON_TEAR}, {@value #BALLOON_GAP_SECONDS} s apart from the fall's start);
 *   <li>a Mirror Interceptor's turbine hum, one loop for the nearest on the screen ({@link Sfx#MIRROR_HUM}), and its pod
 *       burst, once a burst ({@link Sfx#MIRROR_PODS});
 *   <li>an exposed root's heartbeat, one thump per pulse of the nearest root (the pulse {@code RegrowthLooks} draws:
 *       quickening from 1 to 4 a second as its window closes; {@link Sfx#GRAFT_HEARTBEAT}).
 * </ul>
 *
 * The Graft Turret's shot pair is its own ({@link Sfx#GRAFT_SHOT} in {@link CeresCues}, round 75 S.8 b), its shell's
 * crack and its root's crumble {@link JovianCues}'.
 */
final class AureliaCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(SimEvents.Type.FRIENDLIES_INBOUND);

    /** The cues are levelled in their files (step AUD-C) and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The balloons tear this far apart from the fall's start, s. */
    static final double BALLOON_GAP_SECONDS = 1.2;
    /** A mirror's burst is one cue: its next shot within this long is the same burst, s. */
    static final double BURST_SECONDS = 1.0;
    /** The root's pulse rate at its window's start and close, Hz (as {@code RegrowthLooks}). */
    static final double PULSE_FROM_HZ = 1;

    static final double PULSE_TO_HZ = 4;
    /** The heartbeat is quieter than the fire: it sits under it. */
    static final float HEARTBEAT_LEVEL = 0.8f;

    private static final int SLOTS = 16;

    private double growthT = Double.NaN;
    private double fallT = Double.NaN;
    private double[] balloonX = new double[0];

    private boolean[] mirrors;
    private boolean grown;
    private boolean fell;
    private int balloonsTorn;
    private float humLevel;
    private long now = -1;
    /** The mirrors that fired (serials, -1 free) and the step of their burst's first shot. */
    private final int[] burstSerial = new int[SLOTS];

    private final long[] burstTick = new long[SLOTS];
    private int nextBurst;
    /** The root whose heartbeat plays (its serial, -1 none) and its last pulse heard. */
    private int rootSerial = -1;

    private long rootBeat;

    AureliaCues(SoundMix mix) {
        super(mix);
        Arrays.fill(burstSerial, -1);
    }

    /**
     * The level's times: the growth's burst ({@code growthT}, NaN without one), the city's fall ({@code fallT}, NaN
     * without one) and its torn balloons' places ({@code balloonX}, play-field x).
     */
    void level(double growthT, double fallT, List<Double> balloonX) {
        this.growthT = growthT;
        this.fallT = fallT;
        this.balloonX = balloonX.stream().mapToDouble(Double::doubleValue).toArray();
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        if (events.type(i) == SimEvents.Type.FRIENDLIES_INBOUND) {
            bank.play(Sfx.PA_CHIME, CUE_LEVEL, 1, pan);
        }
    }

    /** Notes the level's kinds: which carry a mirror. */
    void noteKinds(List<EnemySpec> kinds) {
        mirrors = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            mirrors[k] = kinds.get(k).mirror().isPresent();
        }
    }

    private boolean mirror(int kind) {
        return mirrors != null && kind >= 0 && kind < mirrors.length && mirrors[kind];
    }

    /** An enemy fired: a mirror's pod burst (once a burst); false for another unit (its usual shot). */
    boolean fired(Enemy unit, float pan) {
        if (unit == null || !mirror(unit.kind())) {
            return false;
        }
        int slot = -1;
        for (int s = 0; s < SLOTS; s++) {
            if (burstSerial[s] == unit.serial()) {
                slot = s;
            }
        }
        long tick = now;
        if (slot >= 0 && tick - burstTick[slot] < SimStep.ticks(BURST_SECONDS) && tick >= burstTick[slot]) {
            return true;
        }
        if (slot < 0) {
            slot = nextBurst;
            nextBurst = (nextBurst + 1) % SLOTS;
            burstSerial[slot] = unit.serial();
        }
        burstTick[slot] = tick;
        bank.play(Sfx.MIRROR_PODS, CUE_LEVEL, pitch(0.04), pan);
        return true;
    }

    /**
     * The pulses a root's heartbeat has beaten {@code share} of the way through a window of {@code windowSeconds}: the
     * rate rising linearly from {@value #PULSE_FROM_HZ} to {@value #PULSE_TO_HZ} Hz (its integral; a beat at each whole
     * number, where {@code RegrowthLooks}' pulse peaks).
     */
    static double pulses(double share, double windowSeconds) {
        double s = Math.clamp(share, 0, 1);
        return windowSeconds * (PULSE_FROM_HZ * s + (PULSE_TO_HZ - PULSE_FROM_HZ) * s * s / 2);
    }

    /** Once a step: the level's timed sounds, the nearest mirror's hum and the nearest root's heartbeat. */
    void watch(Sortie sortie) {
        if (mirrors == null) {
            noteKinds(sortie.enemyKinds());
        }
        long tick = sortie.tick();
        if (tick < now) {
            // A retry or a checkpoint: the turn and the fall come again.
            grown = false;
            fell = false;
            balloonsTorn = 0;
            Arrays.fill(burstSerial, -1);
            rootSerial = -1;
        }
        now = tick;
        double t = sortie.levelSeconds();
        if (!grown && t >= growthT) {
            grown = true;
            bank.play(Sfx.GROWTH_BURST, CUE_LEVEL, 1, 0);
        }
        if (!fell && t >= fallT) {
            fell = true;
            bank.play(Sfx.DECK_GROAN, CUE_LEVEL, 1, 0);
        }
        while (fell && balloonsTorn < balloonX.length && t >= fallT + balloonsTorn * BALLOON_GAP_SECONDS) {
            bank.play(Sfx.BALLOON_TEAR, CUE_LEVEL, pitch(0.03), pan(balloonX[balloonsTorn]));
            balloonsTorn++;
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        Enemy hum = null;
        Enemy root = null;
        double humDistance = Double.POSITIVE_INFINITY;
        double rootDistance = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount() && flying; e++) {
            Enemy enemy = sortie.enemy(e);
            if (!CloudDiverCues.onScreen(enemy)) {
                continue;
            }
            double d = Math.hypot(enemy.renderX(1) - shipX, enemy.renderY(1) - shipY);
            if (mirror(enemy.kind()) && d < humDistance) {
                hum = enemy;
                humDistance = d;
            }
            if (enemy.spec().regrowth().isPresent()
                    && enemy.regrowth().phase() == Regrowing.Phase.ROOT
                    && d < rootDistance) {
                root = enemy;
                rootDistance = d;
            }
        }
        humLevel = fadeLoop(
                Sfx.MIRROR_HUM,
                humLevel,
                hum == null ? 0 : FlightSounds.UNIT_LOOP_LEVEL,
                hum == null ? 0 : pan(hum.renderX(1)));
        if (root == null) {
            rootSerial = -1;
            return;
        }
        long beat = (long) Math.floor(pulses(
                root.regrowth().windowShare(),
                root.spec().regrowth().orElseThrow().windowSeconds()));
        if (root.serial() != rootSerial) {
            rootSerial = root.serial();
            rootBeat = beat - 1;
        }
        if (beat > rootBeat) {
            rootBeat = beat;
            bank.play(Sfx.GRAFT_HEARTBEAT, HEARTBEAT_LEVEL, pitch(0.02), pan(root.renderX(1)));
        }
    }

    /** The flight closes: the hum loop is stopped ({@link FlightSounds#stopLoops}). */
    void silence(Sfx loop) {
        if (loop == Sfx.MIRROR_HUM) {
            humLevel = 0;
        }
    }
}
