package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.PLAYER_DAMAGE;
import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M8 part E, Level 25's sounds for {@link FlightSounds} (design/audio/sfx, Act 4 rows; round 54's a variants until the
 * user's pick): a Siren's song, one file from its sac's tell ({@code RING_TELL}) through the ring spreading out (the
 * ring itself is silent: the song carries it); the falling tone as a ring slows the ship ({@code SLOWED}, the ship's
 * only: Rook's slow is seen, not heard); a Siren's and a Spiral Nautilus's death over their bursts; a Nautilus's shell
 * scraping open at each uncoil ({@code UNCOIL}; the close is silent); and the shells' spin, one quiet loop while a
 * Nautilus is on the screen, panned to the nearest. A Siren shot during its tell does not cut its song's file short
 * (the bank stops a sound's every voice): its death sound plays over it.
 */
final class TrenchCues extends SoundFamily {
    /** The units with sounds of their own, by their slugs. */
    static final String SIREN = "siren";

    static final String NAUTILUS = "spiral-nautilus";
    /** The song and the uncoil sit at their files' levels (the song 3 dB under the Abyss Ray's rise in its file). */
    static final float SONG_LEVEL = 1f;
    /** The slow is the ship's own warning: the player-damage level. */
    static final float SLOW_LEVEL = PLAYER_DAMAGE;

    /** Per enemy kind (from the first note): a Siren, a Spiral Nautilus. */
    private boolean[] sirens;

    private boolean[] nautili;
    /** The spin loop's level now (its fade). */
    private float spinLevel;

    TrenchCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.of(SimEvents.Type.RING_TELL, SimEvents.Type.SLOWED, SimEvents.Type.UNCOIL);
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        if (mix.resync) {
            return;
        }
        switch (events.type(i)) {
            case RING_TELL -> bank.play(Sfx.SIREN_RING, SONG_LEVEL, pitch(0.03), pan);
            case SLOWED -> {
                if (events.value(i) == 0) {
                    bank.play(Sfx.SIREN_SLOW, SLOW_LEVEL, 1, pan);
                }
            }
            case UNCOIL -> bank.play(Sfx.NAUTILUS_UNCOIL, SONG_LEVEL, pitch(0.04), pan);
            default -> {}
        }
    }

    /** Notes the level's kinds: which are the Siren and the Spiral Nautilus. */
    void noteKinds(List<EnemySpec> kinds) {
        sirens = new boolean[kinds.size()];
        nautili = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            sirens[k] = slug.equals(SIREN);
            nautili[k] = slug.equals(NAUTILUS);
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    /** A kill of kind {@code kind}: a Siren's song choked off, a Nautilus's shell cracking; false for another kind. */
    boolean destroyed(int kind, float pan) {
        Sfx extra = is(sirens, kind) ? Sfx.SIREN_DEATH : is(nautili, kind) ? Sfx.NAUTILUS_DEATH : null;
        if (extra == null) {
            return false;
        }
        bank.play(extra, EXPLOSIONS, pitch(0.03), pan);
        return true;
    }

    /** Once a step: the shells' spin for the nearest Nautilus on the screen (faded out without one). */
    void watch(Sortie sortie) {
        if (sirens == null) {
            noteKinds(sortie.enemyKinds());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double nearest = Double.POSITIVE_INFINITY;
        float spinPan = 0;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (!is(nautili, enemy.kind())) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (y < 0 || y > PlayField.HEIGHT || x < 0 || x > PlayField.WIDTH) {
                continue;
            }
            double distance = Math.hypot(x - shipX, y - shipY);
            if (distance < nearest) {
                nearest = distance;
                spinPan = pan(x);
            }
        }
        float target = flying && nearest < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0;
        spinLevel = fadeLoop(Sfx.NAUTILUS_SPIN, spinLevel, target, spinPan);
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.NAUTILUS_SPIN) {
            spinLevel = 0;
        }
    }

    /** The spin loop's level now (for the tests). */
    float spinLevel() {
        return spinLevel;
    }
}
