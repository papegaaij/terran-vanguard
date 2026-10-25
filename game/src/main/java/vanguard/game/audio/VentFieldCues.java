package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.content.BackdropData;
import vanguard.content.LevelData;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Missile;
import vanguard.sim.Missiles;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part G, Level 27's sounds for {@link FlightSounds} (design/audio/sfx, Act 4 rows; round 57's a variants as
 * stand-ins until the user's pick): a plume bending a shot ({@code PLUME_BEND}: a soft swish, at most one every {@value
 * #BEND_GAP_STEPS} steps; the plumes' tell and eruption are the vents', {@link FoundryCues#plumes}); the Depth Hunter's
 * torpedo launch instead of the shot's report ({@link #fired}), the torpedoes' run (one quiet loop for the nearest
 * torpedo, louder as it closes on the ship), a torpedo bursting on a hull ({@code MISSILE_DESTROYED} struck: the ship,
 * the Bulwark, Rook) or shot down (the under-water {@code tiny} burst) ({@link #missileGone}), its hull break over its
 * {@code medium} burst ({@link #destroyed}); the relay beacons' hum (one quiet loop while a beacon lives on the screen,
 * the nearest) and break; the <i>Marlin</i>'s resin cracking as she is shot open ({@code GROUND_DESTROYED}); and Varga's
 * hull-code scan, one ping {@value #SCAN_LEAD_SECONDS} s before the wreck ({@value #WRECK}) passes mid-screen.
 */
final class VentFieldCues extends SoundFamily {
    /** The units and the ground object with sounds of their own, by their slugs and look. */
    static final String HUNTER = "depth-hunter";

    static final String BEACON = "relay-beacon";
    static final String MARLIN = "marlin-hauler";
    /** The backdrop piece Varga scans: the hull-code wreck, placed at the level time it passes mid-screen. */
    static final String WRECK = "hull-wreck";
    /** The scan's ping sounds this long before the wreck passes mid-screen (her line at t=70, the ping at 68), s. */
    static final double SCAN_LEAD_SECONDS = 2;
    /** Bends within this many steps of the last swish share it (a stream through a plume would swish on). */
    static final int BEND_GAP_STEPS = SimStep.ticks(0.25);
    /** The run loop's level: {@link FlightSounds#UNIT_LOOP_LEVEL} within this many px of the ship ... */
    static final double NEAR = 60;
    /** ... falling to {@link #FAR_LEVEL} of it at this many px and beyond. */
    static final double FAR = 400;

    static final float FAR_LEVEL = 0.3f;
    /** The one-shots sit at their files' levels (levelled beside round 46/47's imports). */
    static final float CUE_LEVEL = 1f;

    /** Per enemy kind (from the first note): a Depth Hunter, a relay beacon. */
    private boolean[] hunters;

    private boolean[] beacons;
    /** Whether the level has Depth Hunters: its homing missiles are their torpedoes. */
    private boolean torpedoes;
    /** Per ground object whether it is the Marlin (from the first watch); null before it. */
    private boolean[] marlins;
    /** The level time of the scan's ping (NaN: none), the level clock at the last watch. */
    private double scanAt = Double.NaN;

    private double lastSeconds = Double.POSITIVE_INFINITY;
    /** The loops' levels now (their fades). */
    private float runLevel;

    private float humLevel;
    /** This step and the step of the last bend's swish. */
    private long now;

    private long lastBend = Long.MIN_VALUE / 2;

    VentFieldCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.of(SimEvents.Type.PLUME_BEND);
    }

    @Override
    void begin(long now) {
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        if (events.type(i) == SimEvents.Type.PLUME_BEND && bendDue(lastBend, now) && !mix.resync) {
            bank.play(Sfx.BEND_SWISH, CUE_LEVEL, pitch(0.06), pan);
            lastBend = now;
        }
    }

    /** Whether a bend's swish sounds at step {@code now} after the last one at {@code last}. */
    static boolean bendDue(long last, long now) {
        return now - last >= BEND_GAP_STEPS || now < last;
    }

    /** Notes the level: the scan's time ({@value #WRECK}'s placement less {@value #SCAN_LEAD_SECONDS} s). */
    void noteLevel(LevelData level) {
        scanAt = scanSeconds(level);
    }

    /** The level time of the hull-code scan's ping: the wreck's placement less the lead; NaN without the wreck. */
    static double scanSeconds(LevelData level) {
        for (BackdropData.PlacedPiece placed : level.backdrop().placements()) {
            if (placed.piece().equals(WRECK)) {
                return placed.t() - SCAN_LEAD_SECONDS;
            }
        }
        return Double.NaN;
    }

    /** Notes the level's kinds: which are the Depth Hunter and the relay beacon. */
    void noteKinds(List<EnemySpec> kinds) {
        hunters = new boolean[kinds.size()];
        beacons = new boolean[kinds.size()];
        torpedoes = false;
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            hunters[k] = slug.equals(HUNTER);
            beacons[k] = slug.equals(BEACON);
            torpedoes |= hunters[k];
        }
    }

    private void noteGround(List<GroundObjectSpec> objects) {
        marlins = new boolean[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            marlins[i] = objects.get(i).look().equals(MARLIN);
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    /** Whether the level's homing missiles are the Depth Hunters' torpedoes (known from the first watch). */
    boolean torpedoes() {
        return torpedoes;
    }

    /** A volley left {@code unit}: a Depth Hunter's torpedo launch instead of the shot's report; false for another. */
    boolean fired(Enemy unit, float pan) {
        if (unit == null || !is(hunters, unit.kind())) {
            return false;
        }
        bank.play(Sfx.TORPEDO_LAUNCH, CUE_LEVEL, pitch(0.04), pan);
        return true;
    }

    /** An enemy of kind {@code kind} was destroyed: a Hunter's hull break, a beacon's break over the explosion. */
    boolean destroyed(int kind, float pan) {
        Sfx sfx = is(hunters, kind) ? Sfx.HUNTER_BREAK : is(beacons, kind) ? Sfx.BEACON_BREAK : null;
        if (sfx == null) {
            return false;
        }
        bank.play(sfx, CUE_LEVEL, pitch(0.03), pan);
        return true;
    }

    /**
     * A homing missile is gone for {@code cause} ({@code MISSILE_DESTROYED}'s value): in a level of torpedoes one that
     * burst on a hull plays the torpedo's impact, one shot down the under-water {@code tiny} burst, one that ran out
     * nothing; false (nothing played) in a level of the walker's missiles.
     */
    boolean missileGone(int cause, float pan) {
        if (!torpedoes) {
            return false;
        }
        if (cause == Missiles.STRUCK) {
            bank.play(Sfx.TORPEDO_IMPACT, CUE_LEVEL, pitch(0.03), pan);
        } else if (cause == Missiles.SHOT_DOWN) {
            bank.play(Sfx.EXPLOSION_DEEP_TINY, EXPLOSIONS, pitch(0.05), pan);
        }
        return true;
    }

    /** The run loop's level for the nearest torpedo {@code distance} px from the ship. */
    static float runLevel(double distance) {
        double u = Math.clamp((distance - NEAR) / (FAR - NEAR), 0, 1);
        return (float) (UNIT_LOOP_LEVEL * (1 - (1 - FAR_LEVEL) * u));
    }

    /** Once a step: the torpedoes' run and the beacons' hum for the nearest; the Marlin's crack; the scan's ping. */
    void watch(Sortie sortie) {
        if (hunters == null) {
            noteKinds(sortie.enemyKinds());
            noteGround(sortie.script().groundObjects());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double nearestRun = Double.POSITIVE_INFINITY;
        float runPan = 0;
        if (torpedoes) {
            for (int m = 0; m < sortie.missileCount(); m++) {
                Missile torpedo = sortie.missile(m);
                double distance = Math.hypot(torpedo.x() - shipX, torpedo.y() - shipY);
                if (distance < nearestRun) {
                    nearestRun = distance;
                    runPan = pan(torpedo.x());
                }
            }
        }
        double nearestHum = Double.POSITIVE_INFINITY;
        float humPan = 0;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (!is(beacons, enemy.kind())) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (y < 0 || y > PlayField.HEIGHT) {
                continue;
            }
            double distance = Math.hypot(x - shipX, y - shipY);
            if (distance < nearestHum) {
                nearestHum = distance;
                humPan = pan(x);
            }
        }
        float runTarget = flying && nearestRun < Double.POSITIVE_INFINITY ? runLevel(nearestRun) : 0;
        runLevel = fadeLoop(Sfx.TORPEDO_RUN, runLevel, runTarget, runPan);
        float humTarget = flying && nearestHum < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0;
        humLevel = fadeLoop(Sfx.BEACON_HUM, humLevel, humTarget, humPan);
        if (!mix.resync) {
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                int index = events.value(e);
                SimEvents.Type type = events.type(e);
                // Her hold opens as she is destroyed (an HP target that reveals the secret) or, as a trigger, spent.
                if ((type == SimEvents.Type.GROUND_DESTROYED || type == SimEvents.Type.TRIGGER_SPENT)
                        && marlins != null
                        && index >= 0
                        && index < marlins.length
                        && marlins[index]) {
                    bank.play(Sfx.MARLIN_CRACK, CUE_LEVEL, pitch(0.02), pan(events.x(e)));
                }
            }
        }
        double seconds = sortie.levelSeconds();
        if (scanDue(lastSeconds, seconds, scanAt) && sortie.flying() && !mix.resync) {
            bank.play(Sfx.SCAN_PING, CUE_LEVEL, 1, 0);
        }
        lastSeconds = seconds;
    }

    /** Whether the scan's ping at {@code scanAt} falls after the clock {@code before} and at or before {@code now}. */
    static boolean scanDue(double before, double now, double scanAt) {
        return before < scanAt && scanAt <= now;
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.TORPEDO_RUN) {
            runLevel = 0;
        } else if (loop == Sfx.BEACON_HUM) {
            humLevel = 0;
        }
    }

    /** The loops' levels now (for the tests). */
    float runLevel() {
        return runLevel;
    }

    float humLevel() {
        return humLevel;
    }
}
