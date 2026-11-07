package vanguard.game.audio;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Debris;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part E, step E4 (design/campaign Level 47, Music &amp; ambience; design/enemies/air/choir-seraph; design/audio/sfx,
 * Act 7 sounds; round 84's picks levelled in their files): the inner sphere's sounds for {@link FlightSounds}:
 *
 * <ul>
 *   <li>one glass-harp tone looping on the nearest Choir Seraph while it mirrors the ship ({@link Sfx#SERAPH_MIRROR}, S.13
 *       a), panned to it and pitched up with its sideways speed ({@link #mirrorPitch}: {@value #PITCH_FROM} at rest to
 *       {@value #PITCH_TO} at its {@value #MIRROR_SPEED} px/s cap), faded in and out at the unit loops' rate; silent
 *       through its fold, its fade and while it is gone;
 *   <li>a Seraph's arrival, its rising shimmer under the built arrive whoosh ({@link Sfx#SERAPH_ARRIVE}, S.14 b; {@link
 *       GateCues} hands its {@code ARRIVE} here: {@link #arrived});
 *   <li>its spiral's soft tick ({@link Sfx#SERAPH_SPIRAL}, S.15 a) in place of the gun's report, at most once in
 *       {@value #SPIRAL_GAP_SECONDS} s over every Seraph ({@link UnitCues} hands its {@code ENEMY_FIRED} here: {@link
 *       #fired});
 *   <li>its leaving tell's rising chime ({@link Sfx#SERAPH_LEAVE}, S.16 b, on {@code LEAVING_TELL});
 *   <li>the memory pearl broken: its click and high ring over the built crate break ({@link Sfx#POD_PEARL}, S.53 a, on
 *       the {@code SECRET_FOUND} where the pearl was: once a level at most).
 * </ul>
 *
 * <p>The fold ({@code JUMP_FOLD}) is the jump's silent tell: no sound of its own; the jump's whoosh is the built {@code
 * JUMP_OUT}'s.
 */
final class SphereCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES =
            EnumSet.of(SimEvents.Type.LEAVING_TELL, SimEvents.Type.SECRET_FOUND);

    static final String SERAPH = "choir-seraph";
    /** The memory pearl's debris sprite (design/campaign Level 47, the secret). */
    static final String PEARL = "debris-memory-pearl";
    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** The spiral's ticks play at most once in this long, every Seraph together, s. */
    static final double SPIRAL_GAP_SECONDS = 0.1;
    /** The mirror loop's pitch at rest and at its sideways speed cap, and that cap, px/s. */
    static final float PITCH_FROM = 1f;

    static final float PITCH_TO = 1.25f;
    static final double MIRROR_SPEED = 260;
    /** A secret found within this of where the pearl was last step is the pearl's, px. */
    static final double PEARL_REACH = 40;

    private long now;
    private long lastSpiral = Long.MIN_VALUE / 2;
    /** Per enemy kind whether it is a Choir Seraph; null until the first sortie is seen. */
    private boolean[] seraphs;
    /** The mirror loop's level now (0 silent). */
    private float mirrorLevel;
    /** The pearl's place at the last step (NaN: none on the field). */
    private double pearlX = Double.NaN;

    private double pearlY = Double.NaN;

    SphereCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void begin(long now) {
        if (now < this.now) {
            // A retry or a checkpoint.
            lastSpiral = Long.MIN_VALUE / 2;
        }
        this.now = now;
    }

    private void noteKinds(List<EnemySpec> kinds) {
        seraphs = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            seraphs[k] = kinds.get(k).slug().equals(SERAPH);
        }
    }

    private void noted() {
        if (seraphs == null && mix.watched != null) {
            noteKinds(mix.watched.enemyKinds());
        }
    }

    private boolean seraph(int kind) {
        return seraphs != null && kind >= 0 && kind < seraphs.length && seraphs[kind];
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        noted();
        switch (events.type(i)) {
            case LEAVING_TELL -> {
                if (seraph(events.value(i))) {
                    bank.play(Sfx.SERAPH_LEAVE, CUE_LEVEL, pitch(0.03), pan);
                }
            }
            case SECRET_FOUND -> {
                if (!Double.isNaN(pearlX) && Math.hypot(events.x(i) - pearlX, events.y(i) - pearlY) <= PEARL_REACH) {
                    bank.play(Sfx.POD_PEARL, CUE_LEVEL, 1, pan);
                }
            }
            default -> {}
        }
    }

    /** A unit landed at its ring's end ({@code ARRIVE}, value {@code kind}): a Seraph's shimmer under the whoosh. */
    void arrived(int kind, float pan) {
        noted();
        if (seraph(kind)) {
            bank.play(Sfx.SERAPH_ARRIVE, CUE_LEVEL, pitch(0.04), pan);
        }
    }

    /** A shot by {@code unit}: a Seraph's spiral ticks (throttled) instead of the gun's report; false for another. */
    boolean fired(Enemy unit, float pan) {
        noted();
        if (unit == null || !seraph(unit.kind())) {
            return false;
        }
        if (JovianCues.due(lastSpiral, now, SPIRAL_GAP_SECONDS)) {
            lastSpiral = now;
            bank.play(Sfx.SERAPH_SPIRAL, CUE_LEVEL, pitch(0.06), pan);
        }
        return true;
    }

    /** A unit's sideways speed this step, px/s. */
    static double sidewaysSpeed(Enemy unit) {
        return (unit.renderX(1) - unit.renderX(0)) * SimStep.PER_SECOND;
    }

    /** The mirror loop's pitch at a sideways speed of {@code vx} px/s (either way). */
    static float mirrorPitch(double vx) {
        double share = Math.clamp(Math.abs(vx) / MIRROR_SPEED, 0, 1);
        return (float) (PITCH_FROM + (PITCH_TO - PITCH_FROM) * share);
    }

    /** Whether a Seraph mirrors the ship now (on the field, not in its fold, its fade or gone). */
    static boolean mirroring(Enemy unit) {
        return unit.spec().shadow().isPresent()
                && unit.shadow().mirroring()
                && !unit.teleport().gone()
                && !unit.empStunned();
    }

    /** Once a step: the mirror loop on the nearest mirroring Seraph; the pearl's place. */
    void watch(Sortie sortie) {
        if (seraphs == null) {
            noteKinds(sortie.enemyKinds());
        }
        pearlX = Double.NaN;
        pearlY = Double.NaN;
        for (int d = 0; d < sortie.debrisCount(); d++) {
            Debris chunk = sortie.debris(d);
            if (chunk.sprite().equals(PEARL)) {
                pearlX = chunk.renderX(1);
                pearlY = chunk.renderY(1);
            }
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        Enemy nearest = null;
        double best = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount() && flying; e++) {
            Enemy enemy = sortie.enemy(e);
            if (!seraph(enemy.kind()) || !mirroring(enemy)) {
                continue;
            }
            double d = Math.hypot(enemy.renderX(1) - shipX, enemy.renderY(1) - shipY);
            if (d < best) {
                best = d;
                nearest = enemy;
            }
        }
        if (nearest == null && mirrorLevel <= 0) {
            return;
        }
        float pitch = nearest == null ? PITCH_FROM : mirrorPitch(sidewaysSpeed(nearest));
        mirrorLevel = fadeLoop(
                Sfx.SERAPH_MIRROR,
                mirrorLevel,
                nearest == null ? 0 : FlightSounds.UNIT_LOOP_LEVEL,
                nearest == null ? 0 : pan(nearest.renderX(1)),
                pitch);
        if (mirrorLevel > 0 && nearest != null) {
            bank.loopPitch(Sfx.SERAPH_MIRROR, pitch);
        }
    }

    /** The mirror loop's level now, for the tests. */
    float mirrorLevel() {
        return mirrorLevel;
    }

    /** The flight closes: the mirror loop is stopped ({@link FlightSounds#stopLoops}). */
    void silence(Sfx loop) {
        if (loop == Sfx.SERAPH_MIRROR) {
            mirrorLevel = 0;
        }
    }
}
