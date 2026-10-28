package vanguard.game.audio;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.Missile;
import vanguard.sim.Missiles;
import vanguard.sim.PowerState;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M9 part A: the Act 5 systems' sounds for {@link FlightSounds} (design/audio/sfx, Act 5 sounds; round 60's picks, the
 * preview cuts of step AUD-A levelled in their files, played as they are until part D's production mix): an enemy's
 * hex shield taking a hit that held ({@link Sfx#ENEMY_SHIELD_HIT}; the simulation spaces a unit's hit events),
 * emptied ({@link Sfx#ENEMY_SHIELD_BREAK}: by a hit, a {@code shield-breaker} arc over the arc's zap, or the EMP) and
 * full again ({@link Sfx#ENEMY_SHIELD_REFILL}); in a level whose homing missiles are the Ascendancy's homing diamonds
 * ({@link #diamonds}) each launch ({@link Sfx#HOMING_LAUNCH}, a salvo's launches {@value #LAUNCH_GAP_STEPS} steps
 * apart at least), one quiet run loop for the nearest diamond in flight ({@link Sfx#HOMING_RUN}, louder as it closes,
 * as the Depth Hunters' torpedoes' run) and a diamond shot down ({@link Sfx#HOMING_DOWN}; one that burst on a hull is
 * the hull's hit, one that ran out just goes); and the ship's brown-out hum while its load is over the drained output
 * ({@link Sfx#BROWNOUT_HUM}, design/player/generator, Enemy drain effects).
 */
final class BeltCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.ENEMY_SHIELD_HIT, SimEvents.Type.ENEMY_SHIELD_BROKEN, SimEvents.Type.ENEMY_SHIELD_REFILLED);

    /** The cues are levelled in their files (−23 to −38 dB, step AUD-A) and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** Launches within this many steps of the last launch sound share it (a rack's salvo). */
    static final int LAUNCH_GAP_STEPS = 6;
    /** The brown-out hum's level (levelled in its file, −35 dB). */
    static final float BROWNOUT_LEVEL = 1f;

    /** Whether the level's homing missiles are homing diamonds (from {@link #diamonds}). */
    private boolean diamonds;
    /** M9 part D: Level 31's cues (the Hornet's launch pitch); null before {@link #rig}. */
    private MiningRigCues rig;
    /** The step of the last launch sound, and this step's. */
    private long lastLaunch = Long.MIN_VALUE / 2;

    private long now;
    /** The run loop's level now (its fade). */
    private float runLevel;

    BeltCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The level's homing missiles are the Ascendancy's homing diamonds (their launch, run and fall). */
    void diamonds(boolean diamonds) {
        this.diamonds = diamonds;
    }

    /** Whether the level's homing missiles are homing diamonds. */
    boolean diamonds() {
        return diamonds;
    }

    @Override
    void begin(long now) {
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        Sfx cue = cue(events.type(i));
        if (cue != null) {
            bank.play(cue, CUE_LEVEL, pitch(cue == Sfx.ENEMY_SHIELD_HIT ? 0.06 : 0.04), pan);
        }
    }

    /** The cue of a hex shield's event: its hit that held, its break, its refill; null for another event. */
    static Sfx cue(SimEvents.Type type) {
        return switch (type) {
            case ENEMY_SHIELD_HIT -> Sfx.ENEMY_SHIELD_HIT;
            case ENEMY_SHIELD_BROKEN -> Sfx.ENEMY_SHIELD_BREAK;
            case ENEMY_SHIELD_REFILLED -> Sfx.ENEMY_SHIELD_REFILL;
            default -> null;
        };
    }

    /** A homing missile was launched: a diamond's launch (false, nothing played, in a level without diamonds). */
    boolean launched(float pan) {
        return launched(pan, 1);
    }

    /**
     * M9 part D: a homing missile was launched at (x, y): a diamond's launch, a Hornet's pitched up ({@link
     * MiningRigCues#launchPitch}); false (nothing played) in a level without diamonds.
     */
    boolean launched(float pan, double x, double y) {
        return launched(pan, diamonds && rig != null ? rig.launchPitch(x, y) : 1);
    }

    private boolean launched(float pan, float pitch) {
        if (!diamonds) {
            return false;
        }
        if (launchDue(lastLaunch, now)) {
            bank.play(Sfx.HOMING_LAUNCH, CUE_LEVEL, pitch * pitch(0.05), pan);
            lastLaunch = now;
        }
        return true;
    }

    /** M9 part D: Level 31's cues, for the Hornet's launch pitch. */
    void rig(MiningRigCues rig) {
        this.rig = rig;
    }

    /** Whether a launch sounds at step {@code now} after the last one at {@code last}. */
    static boolean launchDue(long last, long now) {
        return now - last >= LAUNCH_GAP_STEPS || now < last;
    }

    /**
     * A homing missile is gone for {@code cause} ({@code MISSILE_DESTROYED}'s value): a diamond shot down cracks, one
     * that burst on a hull or ran out plays nothing of its own; false (nothing played) in a level without diamonds.
     */
    boolean gone(int cause, float pan) {
        if (!diamonds) {
            return false;
        }
        if (cause == Missiles.SHOT_DOWN) {
            bank.play(Sfx.HOMING_DOWN, CUE_LEVEL, pitch(0.05), pan);
        }
        return true;
    }

    /** Once a step: the run loop for the nearest diamond in flight, the brown-out hum while the load is over E. */
    void watch(Sortie sortie) {
        boolean flying = sortie.flying() && !sortie.complete();
        double nearest = Double.POSITIVE_INFINITY;
        float runPan = 0;
        if (diamonds) {
            double shipX = sortie.ship().renderX(1);
            double shipY = sortie.ship().renderY(1);
            for (int m = 0; m < sortie.missileCount(); m++) {
                Missile missile = sortie.missile(m);
                double distance = Math.hypot(missile.x() - shipX, missile.y() - shipY);
                if (distance < nearest) {
                    nearest = distance;
                    runPan = pan(missile.x());
                }
            }
        }
        float runTarget = flying && nearest < Double.POSITIVE_INFINITY ? VentFieldCues.runLevel(nearest) : 0;
        runLevel = fadeLoop(Sfx.HOMING_RUN, runLevel, runTarget, runPan);
        boolean brownOut = flying && sortie.power().map(PowerState::overLoad).orElse(false);
        if (brownOut && !bank.looping(Sfx.BROWNOUT_HUM)) {
            bank.loop(Sfx.BROWNOUT_HUM, BROWNOUT_LEVEL);
        } else if (!brownOut && bank.looping(Sfx.BROWNOUT_HUM)) {
            bank.stop(Sfx.BROWNOUT_HUM);
        }
    }

    /** A loop stopped from outside (the level screen closing): its level is none. */
    void silence(Sfx loop) {
        if (loop == Sfx.HOMING_RUN) {
            runLevel = 0;
        }
    }

    /** The run loop's level now (for the tests). */
    float runLevel() {
        return runLevel;
    }
}
