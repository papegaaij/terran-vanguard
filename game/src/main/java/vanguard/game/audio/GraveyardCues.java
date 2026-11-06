package vanguard.game.audio;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M11 part D, step D4 (design/campaign Level 46, Music &amp; ambience; design/audio/sfx, Act 7 sounds; round 84's picks
 * levelled in their files): the dead fleet's sounds for {@link FlightSounds}:
 *
 * <ul>
 *   <li>one slow hull creak looping on the nearest drifting Husk on the screen ({@link Sfx#HUSK_DRIFT}, S.9 b; whole
 *       or cracked, not while the EMP holds it), panned to it, faded in and out at the unit loops' rate;
 *   <li>a Husk cracking ({@link Sfx#HUSK_CRACK}, S.10 a, on {@code CRACK_TELL}: its tell, also each turn of the quiet's
 *       cue) and its shard fan ({@link Sfx#HUSK_SHARDS}, S.11 a, on {@code CRACK_FAN});
 *   <li>a Husk's death and a seed casket broken are their own deaths instead of their rungs' ({@link Sfx#HUSK_DEATH}, S.12
 *       a; {@link Sfx#CASKET_CRACK}, S.50 b; the units' looks, {@code EnemyLooks}' death sounds, played by {@link
 *       UnitCues});
 *   <li>the warm hull's core taken by its own sound ({@link Sfx#WARM_HULL_TAKEN}, S.51 a: {@link ReefCues#TAKEN}); the
 *       sinews' tear is their built tissue tear;
 *   <li>the dying Leviathan's whale cry at {@value BossCues#DYING_CRY_PITCH}× pitch if it dies ({@link BossCues}; no new
 *       file).
 * </ul>
 */
final class GraveyardCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(SimEvents.Type.CRACK_TELL, SimEvents.Type.CRACK_FAN);

    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;

    /** Per enemy kind whether it cracks (the Husk); null until the first sortie is seen. */
    private boolean[] cracking;
    /** The drift loop's level now (0 silent). */
    private float driftLevel;

    GraveyardCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    private void noteKinds(List<EnemySpec> kinds) {
        cracking = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            cracking[k] = kinds.get(k).crack().isPresent();
        }
    }

    private void noted() {
        if (cracking == null && mix.watched != null) {
            noteKinds(mix.watched.enemyKinds());
        }
    }

    private boolean cracks(int kind) {
        return cracking != null && kind >= 0 && kind < cracking.length && cracking[kind];
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        noted();
        if (!cracks(events.value(i))) {
            return;
        }
        switch (events.type(i)) {
            case CRACK_TELL -> bank.play(Sfx.HUSK_CRACK, CUE_LEVEL, pitch(0.04), pan);
            case CRACK_FAN -> bank.play(Sfx.HUSK_SHARDS, CUE_LEVEL, pitch(0.05), pan);
            default -> {}
        }
    }

    /** Whether a Husk drifts audibly now: on the screen and not held by the EMP. */
    static boolean drifting(Enemy husk) {
        return CloudDiverCues.onScreen(husk) && !husk.empStunned();
    }

    /** Once a step: the drift creak on the nearest drifting Husk, faded in and out. */
    void watch(Sortie sortie) {
        if (cracking == null) {
            noteKinds(sortie.enemyKinds());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        Enemy nearest = null;
        double best = Double.POSITIVE_INFINITY;
        for (int e = 0; e < sortie.enemyCount() && flying; e++) {
            Enemy enemy = sortie.enemy(e);
            if (!cracks(enemy.kind()) || !drifting(enemy)) {
                continue;
            }
            double d = Math.hypot(enemy.renderX(1) - shipX, enemy.renderY(1) - shipY);
            if (d < best) {
                best = d;
                nearest = enemy;
            }
        }
        if (nearest == null && driftLevel <= 0) {
            return;
        }
        driftLevel = fadeLoop(
                Sfx.HUSK_DRIFT,
                driftLevel,
                nearest == null ? 0 : FlightSounds.UNIT_LOOP_LEVEL,
                nearest == null ? 0 : pan(nearest.renderX(1)));
    }

    /** The drift loop's level now, for the tests. */
    float driftLevel() {
        return driftLevel;
    }

    /** The flight closes: the drift loop is stopped ({@link FlightSounds#stopLoops}). */
    void silence(Sfx loop) {
        if (loop == Sfx.HUSK_DRIFT) {
            driftLevel = 0;
        }
    }
}
