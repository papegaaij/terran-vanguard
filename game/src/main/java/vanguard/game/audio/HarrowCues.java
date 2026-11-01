package vanguard.game.audio;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.FlightSpecs;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part D, step D4: the Harrow's sounds for {@link FlightSounds} (design/enemies/air/harrow, design/audio/sfx, Act 6
 * sounds; round 72's picks and round 76's a variants until its picks, levelled in their files and played as they are):
 *
 * <ul>
 *   <li>the hunting whistle: one loop for the nearest hunting Harrow on the screen ({@link Sfx#HARROW_WHISTLE} at
 *       {@link FlightSounds#UNIT_LOOP_LEVEL}), its pitch rising from {@value #WHISTLE_FROM} to {@value #WHISTLE_TO} over
 *       its chase (the chase's middle length, {@link #chaseShare}); a Harrow in its lock tell, ramming or stunned no
 *       longer hunts, so the loop fades out at the tell when no other Harrow hunts;
 *   <li>the lock tell ({@link Sfx#HARROW_LOCK}) at its {@code DASH_TELL} ({@link MiningRigCues} hands a ram's tell
 *       over, {@link #tell}); the ram's whoosh ({@link Sfx#HARROW_RAM}) at its {@code DASH_START};
 *   <li>its burst on every death is its explosion ({@code EnemyLooks}' death sounds: {@link Sfx#HARROW_BURST} in its
 *       rung's place).
 * </ul>
 *
 * A Harrow is any unit whose dash is a ram ({@code after: out}: one straight dash and out through the far edge).
 */
final class HarrowCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(SimEvents.Type.DASH_START);

    /** The cues are levelled in their files (steps AUD-D and AUD-R) and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The whistle's pitch at the chase's start and at its middle length's end. */
    static final float WHISTLE_FROM = 0.85f;

    static final float WHISTLE_TO = 1.25f;

    private static final int SLOTS = 32;

    private boolean[] rams;
    private float whistleLevel;
    private long now = -1;
    /** The Harrows seen hunting on the screen (serials, -1 free) and the step their chase was first heard. */
    private final int[] huntSerial = new int[SLOTS];

    private final long[] huntStart = new long[SLOTS];
    private int nextHunt;

    HarrowCues(SoundMix mix) {
        super(mix);
        Arrays.fill(huntSerial, -1);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** Notes the level's kinds: which ram. */
    void noteKinds(List<EnemySpec> kinds) {
        rams = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            rams[k] = rams(kinds.get(k));
        }
    }

    /** Whether a kind's dash is a ram (the Harrow's: one dash, then out through the far edge). */
    static boolean rams(EnemySpec kind) {
        return kind.dash().isPresent() && kind.dash().get().variant().after() == FlightSpecs.Dash.After.OUT;
    }

    private void noted() {
        if (rams == null && mix.watched != null) {
            noteKinds(mix.watched.enemyKinds());
        }
    }

    private boolean ram(int kind) {
        return rams != null && kind >= 0 && kind < rams.length && rams[kind];
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        noted();
        if (events.type(i) == SimEvents.Type.DASH_START && ram(events.value(i))) {
            bank.play(Sfx.HARROW_RAM, CUE_LEVEL, pitch(0.04), pan);
        }
    }

    /** A dashing unit's tell started ({@code DASH_TELL}, value its kind): a ram's lock; false for another dasher. */
    boolean tell(int kind, float pan) {
        noted();
        if (!ram(kind)) {
            return false;
        }
        bank.play(Sfx.HARROW_LOCK, CUE_LEVEL, pitch(0.03), pan);
        return true;
    }

    /** Whether a Harrow hunts now: in its chase (not telling, not ramming), not stunned. */
    static boolean hunting(Enemy harrow) {
        return !harrow.dashTelling() && !harrow.dashing() && !harrow.empStunned() && harrow.dashes() == 0;
    }

    /** The whistle's pitch {@code share} of the way through a chase (clamped). */
    static float whistlePitch(double share) {
        return (float) (WHISTLE_FROM + (WHISTLE_TO - WHISTLE_FROM) * Math.clamp(share, 0, 1));
    }

    /** The share of a chase {@code ticks} steps long: of the middle of its seeded range (its fixed time otherwise). */
    static double chaseShare(EnemySpec kind, long ticks) {
        var dash = kind.dash().orElseThrow();
        double most = Math.max(dash.chaseSeconds(), dash.variant().chaseMostSeconds());
        double middle = (dash.chaseSeconds() + most) / 2;
        return middle > 0 ? ticks * SimStep.SECONDS / middle : 1;
    }

    /** Once a step: the nearest hunting Harrow's whistle, pitched by how far its chase has run. */
    void watch(Sortie sortie) {
        if (rams == null) {
            noteKinds(sortie.enemyKinds());
        }
        long tick = sortie.tick();
        if (tick < now) {
            // A retry or a checkpoint.
            Arrays.fill(huntSerial, -1);
        }
        now = tick;
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        Enemy nearest = null;
        long nearestStart = tick;
        double best = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount() && flying; e++) {
            Enemy enemy = sortie.enemy(e);
            if (!ram(enemy.kind()) || !CloudDiverCues.onScreen(enemy) || !hunting(enemy)) {
                continue;
            }
            long start = huntStart(enemy.serial(), tick);
            double d = Math.hypot(enemy.renderX(1) - shipX, enemy.renderY(1) - shipY);
            if (d < best) {
                best = d;
                nearest = enemy;
                nearestStart = start;
            }
        }
        float pitch = nearest == null ? WHISTLE_FROM : whistlePitch(chaseShare(nearest.spec(), tick - nearestStart));
        whistleLevel = fadeLoop(
                Sfx.HARROW_WHISTLE,
                whistleLevel,
                nearest == null ? 0 : FlightSounds.UNIT_LOOP_LEVEL,
                nearest == null ? 0 : pan(nearest.renderX(1)),
                pitch);
        if (whistleLevel > 0) {
            bank.loopPitch(Sfx.HARROW_WHISTLE, pitch);
        }
    }

    /** The step a hunting Harrow's chase was first heard (now, the first time it is seen). */
    private long huntStart(int serial, long tick) {
        for (int s = 0; s < SLOTS; s++) {
            if (huntSerial[s] == serial) {
                return huntStart[s];
            }
        }
        huntSerial[nextHunt] = serial;
        huntStart[nextHunt] = tick;
        nextHunt = (nextHunt + 1) % SLOTS;
        return tick;
    }

    /** The whistle's level now, for the tests. */
    float whistleLevel() {
        return whistleLevel;
    }

    /** The flight closes: the whistle loop is stopped ({@link FlightSounds#stopLoops}). */
    void silence(Sfx loop) {
        if (loop == Sfx.HARROW_WHISTLE) {
            whistleLevel = 0;
        }
    }
}
