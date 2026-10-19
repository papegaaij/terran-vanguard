package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part E: Level 18's unit sounds for {@link FlightSounds} (design/audio/sfx, Dust Devil and Ghost Drone; round 41
 * a each, proposed, levelled in their files as round 40's and played as they are): the Dust Devil's spin, one loop
 * for the nearest one on the screen, panned to it and faded in and out over 0.2 s; its rise and opening from the
 * start of its rise (the tell); its grit spiral's hiss for its volleys instead of a gun's report, at most one every
 * {@value #SPIRAL_GAP_STEPS} steps. The Ghost Drone's turbine whine as it drops into its lane from the top; its lock
 * tick with the eye glint; its burst once, at the first shot of each pass's burst, instead of the shots' reports; its
 * mechanical death is its explosion ({@code EnemyLooks}).
 */
final class StormCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(SimEvents.Type.VORTEX_RISING, SimEvents.Type.EYE_GLINT);

    /** The one-shots are levelled in their files (−19 to −24 dB, as round 40's) and play as they are. */
    static final float STORM_LEVEL = 1f;
    /** A spiral's volleys (a bullet per arm every 0.16 s): one hiss a volley, its arms' shots together. */
    static final int SPIRAL_GAP_STEPS = SimStep.ticks(0.16) - 1;
    /** A drone's burst (its shots 0.15 s apart, 2 to 4 of them): one sound for the shots within this of the first. */
    static final int BURST_STEPS = SimStep.ticks(1.0);
    /** The drones and devils remembered at once. */
    private static final int SLOTS = 16;

    /** The step of the last spiral hiss. */
    private long lastSpiral = Long.MIN_VALUE / 2;
    /** The drones whose burst sounded (their serials, -1 free) and the step it did. */
    private final int[] burstSerial = new int[SLOTS];

    private final long[] burstTick = new long[SLOTS];
    private int nextBurst;
    /** The drones whose drop into their lane sounded (their serials, -1 free). */
    private final int[] dropSerial = new int[SLOTS];

    private int nextDrop;
    /** The spin loop's level now (its fade). */
    private float spinLevel;
    /** This step's step. */
    private long now;

    StormCues(SoundMix mix) {
        super(mix);
        Arrays.fill(burstSerial, -1);
        Arrays.fill(dropSerial, -1);
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
            case VORTEX_RISING -> bank.play(Sfx.DEVIL_OPEN, STORM_LEVEL, pitch(0.03), pan);
            case EYE_GLINT -> bank.play(Sfx.GHOST_DRONE_GLINT, STORM_LEVEL, pitch(0.02), pan);
            default -> {}
        }
    }

    /** Whether a spiral's hiss is due at step {@code now} after the last one at {@code last}. */
    static boolean spiralDue(long last, long now) {
        return now - last >= SPIRAL_GAP_STEPS || now < last;
    }

    /**
     * A volley left {@code unit} (null: none found where it left): a vortex's hiss, a lane strafer's burst at the
     * first shot of its burst, nothing for its later shots; false for another unit, whose shot plays as ever.
     */
    boolean fired(Enemy unit, float pan) {
        if (unit == null) {
            return false;
        }
        if (unit.spec().vortex().isPresent()) {
            if (spiralDue(lastSpiral, now)) {
                bank.play(Sfx.DEVIL_SPIRAL, STORM_LEVEL, pitch(0.04), pan);
                lastSpiral = now;
            }
            return true;
        }
        if (unit.spec().laneStrafe().isEmpty()) {
            return false;
        }
        int slot = slot(burstSerial, unit.serial());
        if (slot >= 0 && burstTick[slot] <= now && now - burstTick[slot] < BURST_STEPS) {
            return true;
        }
        if (slot < 0) {
            slot = nextBurst;
            nextBurst = (nextBurst + 1) % SLOTS;
            burstSerial[slot] = unit.serial();
        }
        burstTick[slot] = now;
        bank.play(Sfx.GHOST_DRONE_BURST, STORM_LEVEL, pitch(0.03), pan);
        return true;
    }

    private static int slot(int[] serials, int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (serials[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    /**
     * Once a step: the spin loop for the nearest Dust Devil on the screen (faded out without one), and the whine of
     * each Ghost Drone that starts its drop on the screen.
     */
    void watch(Sortie sortie) {
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double nearest = Double.POSITIVE_INFINITY;
        float spinPan = 0;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (y < 0 || y > PlayField.HEIGHT) {
                continue;
            }
            if (enemy.spec().vortex().isPresent()) {
                double distance = Math.hypot(x - shipX, y - shipY);
                if (distance < nearest) {
                    nearest = distance;
                    spinPan = pan(x);
                }
            } else if (enemy.laneStrafer()
                    && enemy.strafePhase() == Enemy.StrafePhase.DESCENT
                    && slot(dropSerial, enemy.serial()) < 0) {
                dropSerial[nextDrop] = enemy.serial();
                nextDrop = (nextDrop + 1) % SLOTS;
                if (flying && !mix.resync) {
                    bank.play(Sfx.GHOST_DRONE_DROP, STORM_LEVEL, pitch(0.03), pan(x));
                }
            }
        }
        float target = flying && nearest < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0;
        spinLevel = fadeLoop(Sfx.DEVIL_SPIN, spinLevel, target, spinPan);
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.DEVIL_SPIN) {
            spinLevel = 0;
        }
    }
}
