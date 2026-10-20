package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.GroundSpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part F: Level 19's own sounds for {@link FlightSounds} (design/audio/sfx, Warden Tank, Furnace vent, Conveyor
 * belts; round 42 a each, proposed, levelled in their files as round 41's and played as they are). The Warden Tank's
 * tracks, one loop for the nearest tank on the screen, panned to it and faded in and out over 0.2 s; its turret's servo
 * whine as the turret stops on the ship (the 0.3 s hold before a burst, the tell), at most one every {@value
 * #SLEW_GAP_STEPS} steps; its burst once, at the first shot of each burst, instead of the shots' reports; its death is
 * its explosion ({@code EnemyLooks}). A furnace vent's rising roar as each grate starts to glow (the tell) and its fire
 * blast as the flame column stands up (40 ms attack: no pop). The belts' rumble, one quiet loop while a belt runs on
 * the screen, panned to the one nearest the ship, under the {@code mars-foundry} bed.
 */
final class FoundryCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(SimEvents.Type.VENT_TELL, SimEvents.Type.VENT_BURST);

    /** The one-shots are levelled in their files (−16 to −24 dB, as round 41's) and play as they are. */
    static final float FOUNDRY_LEVEL = 1f;
    /** A tank's burst (its shots 0.15 s apart, 2 to 4 of them): one sound for the shots within this of the first. */
    static final int BURST_STEPS = SimStep.ticks(0.9);
    /** Turrets locking within this many steps of the last whine share it (tanks in a convoy lock together). */
    static final int SLEW_GAP_STEPS = SimStep.ticks(0.3);
    /** The tanks remembered at once. */
    private static final int SLOTS = 16;

    /** The tanks whose burst sounded (their serials, -1 free) and the step it did. */
    private final int[] burstSerial = new int[SLOTS];

    private final long[] burstTick = new long[SLOTS];
    private int nextBurst;
    /** The tanks whose turret was locked at the last watch (their serials, -1 free). */
    private final int[] lockedSerial = new int[SLOTS];
    /** The step of the last turret whine. */
    private long lastSlew = Long.MIN_VALUE / 2;
    /** The tracks loop's and the belts' loop's levels now (their fades). */
    private float tracksLevel;

    private float beltLevel;
    /** This step's step. */
    private long now;

    FoundryCues(SoundMix mix) {
        super(mix);
        Arrays.fill(burstSerial, -1);
        Arrays.fill(lockedSerial, -1);
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
            case VENT_TELL -> bank.play(Sfx.VENT_TELL, FOUNDRY_LEVEL, pitch(0.03), pan);
            case VENT_BURST -> bank.play(Sfx.VENT_BURST, FOUNDRY_LEVEL, pitch(0.03), pan);
            default -> {}
        }
    }

    /**
     * A volley left {@code unit} (null: none found where it left): a turret unit's burst at the first shot of its
     * burst, nothing for its later shots; false for another unit, whose shot plays as ever.
     */
    boolean fired(Enemy unit, float pan) {
        if (unit == null || unit.spec().turret().isEmpty()) {
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
        bank.play(Sfx.TANK_BURST, FOUNDRY_LEVEL, pitch(0.03), pan);
        return true;
    }

    /** Whether a turret's whine is due at step {@code now} after the last one at {@code last}. */
    static boolean slewDue(long last, long now) {
        return now - last >= SLEW_GAP_STEPS || now < last;
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
     * Once a step: the tracks loop for the nearest turret unit on the screen (faded out without one), each turret's
     * whine as it locks on the ship, and the belts' rumble while a belt runs on the screen.
     */
    void watch(Sortie sortie) {
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double nearest = Double.POSITIVE_INFINITY;
        float tracksPan = 0;
        int[] locked = new int[SLOTS];
        Arrays.fill(locked, -1);
        int lockedCount = 0;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (enemy.spec().turret().isEmpty()) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (y < 0 || y > PlayField.HEIGHT) {
                continue;
            }
            double distance = Math.hypot(x - shipX, y - shipY);
            if (distance < nearest) {
                nearest = distance;
                tracksPan = pan(x);
            }
            if (enemy.turretLocked() && enemy.partAlive()) {
                if (lockedCount < SLOTS) {
                    locked[lockedCount++] = enemy.serial();
                }
                if (slot(lockedSerial, enemy.serial()) < 0
                        && flying
                        && !mix.resync
                        && slewDue(lastSlew, sortie.tick())) {
                    bank.play(Sfx.TANK_TURRET, FOUNDRY_LEVEL, pitch(0.03), pan(x));
                    lastSlew = sortie.tick();
                }
            }
        }
        System.arraycopy(locked, 0, lockedSerial, 0, SLOTS);
        float target = flying && nearest < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0;
        tracksLevel = fadeLoop(Sfx.TANK_TRACKS, tracksLevel, target, tracksPan);
        double belt = nearestBelt(sortie, shipX);
        float beltTarget = flying && !Double.isNaN(belt) ? UNIT_LOOP_LEVEL : 0;
        beltLevel = fadeLoop(Sfx.BELT_RUMBLE, beltLevel, beltTarget, Double.isNaN(belt) ? 0 : pan(belt));
    }

    /** The x of the belt on the screen nearest {@code shipX}, or NaN while no belt runs on the screen. */
    static double nearestBelt(Sortie sortie, double shipX) {
        double scroll = sortie.groundScroll();
        double best = Double.NaN;
        for (GroundSpec.Belt belt : sortie.script().ground().belts()) {
            if (onScreen(belt, scroll) && (Double.isNaN(best) || Math.abs(belt.x() - shipX) < Math.abs(best - shipX))) {
                best = belt.x();
            }
        }
        return best;
    }

    /** Whether some of {@code belt}'s run is on the screen, the ground scrolled {@code scroll} px. */
    static boolean onScreen(GroundSpec.Belt belt, double scroll) {
        double bottom = scroll - PlayField.HEIGHT / 2.0;
        double top = scroll + PlayField.HEIGHT / 2.0;
        return belt.from() < top && belt.to() > bottom;
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.TANK_TRACKS) {
            tracksLevel = 0;
        } else if (loop == Sfx.BELT_RUMBLE) {
            beltLevel = 0;
        }
    }
}
