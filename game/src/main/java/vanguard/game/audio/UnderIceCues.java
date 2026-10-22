package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.ENEMY_FIRE;
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
 * M8 part C, Level 23's sounds for {@link FlightSounds} (design/audio/sfx, Act 4 rows; round 52's a variants until the
 * user's pick): the waterline crossing and the hydro-kit's refit once as the ship drops into the water (the medium's
 * edge, no simulation event: gap check D5); the siphon pumps' hum, one quiet loop while a pump lives on the screen,
 * panned to the nearest; and, handed over by the other families, a kill's extra sound (a Glassfin's shatter, an eel
 * part's tear, a pump's break), an eel head's pulse shot and the wall scrape under water. The kills themselves sound on
 * the under-water rung below the waterline ({@link Sfx#underwater}). Plays no event type of its own.
 */
final class UnderIceCues extends SoundFamily {
    /** The units with sounds of their own, by their slugs (the eel's parts are {@code eel-swarm-segment}, ...). */
    static final String EEL = "eel-swarm";

    static final String GLASSFIN = "glassfin";
    static final String PUMP = "siphon-pump";
    /** The kill's extra sound a little under the burst it plays with (its file is levelled beside the burst's). */
    static final float EXTRA_LEVEL = EXPLOSIONS;

    /** Per enemy kind (from the first watch): an eel's part (head, segment, tail), an eel's head, a Glassfin, a pump. */
    private boolean[] eelParts;

    private boolean[] eelHeads;
    private boolean[] glassfins;
    private boolean[] pumps;
    /** Whether the ship flew under water at the last watch (the crossing is its edge). */
    private boolean wasUnderwater;

    private boolean watchedOnce;
    /** The hum's level now (its fade). */
    private float humLevel;

    UnderIceCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return EnumSet.noneOf(SimEvents.Type.class);
    }

    @Override
    void play(SimEvents events, int i, float pan) {}

    /** Notes the level's kinds: which are an eel's parts, its heads, Glassfins and siphon pumps. */
    void noteKinds(List<EnemySpec> kinds) {
        eelParts = new boolean[kinds.size()];
        eelHeads = new boolean[kinds.size()];
        glassfins = new boolean[kinds.size()];
        pumps = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            eelParts[k] = slug.equals(EEL) || slug.startsWith(EEL + "-");
            eelHeads[k] = slug.equals(EEL);
            glassfins[k] = slug.equals(GLASSFIN);
            pumps[k] = slug.equals(PUMP);
        }
    }

    /** Whether the watched sortie flies under water now (a kill's rung, the scrape). */
    boolean underwater() {
        return mix.watched != null && mix.watched.underwater();
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    /** Whether enemy kind {@code kind} is a part of an Eel Swarm (its head, a segment, its tail). */
    boolean eelPart(int kind) {
        return is(eelParts, kind);
    }

    /**
     * A kill of enemy kind {@code kind} (shot, or a chained pop): its extra sound over its burst: a Glassfin's glass
     * shatter, an eel segment's or tail's tear, a pump's break. False (nothing played) for another kind.
     */
    boolean destroyed(int kind, float pan) {
        Sfx extra = is(glassfins, kind)
                ? Sfx.GLASSFIN_SHATTER
                : is(pumps, kind) ? Sfx.PUMP_BREAK : is(eelParts, kind) && !is(eelHeads, kind) ? Sfx.EEL_SEGMENT : null;
        if (extra == null) {
            return false;
        }
        bank.play(extra, EXTRA_LEVEL, pitch(0.04), pan);
        return true;
    }

    /** A volley left {@code unit} (null: none found): an eel head's pulse shot instead of a gun's report. */
    boolean fired(Enemy unit, float pan) {
        if (unit == null || !is(eelHeads, unit.kind())) {
            return false;
        }
        bank.play(Sfx.EEL_PULSE, ENEMY_FIRE, pitch(0.05), pan);
        return true;
    }

    /** A wall touch: under water the muffled scrape instead of the canyon's; false (the canyon's plays) in the air. */
    boolean scrape(float level, float pan) {
        if (!underwater()) {
            return false;
        }
        bank.play(Sfx.SCRAPE_UNDER, level, pitch(0.04), pan);
        return true;
    }

    /**
     * Once a step: the crossing's swell and the refit's clamps as the ship drops through a waterline into the water,
     * and the pumps' hum for the nearest live pump on the screen (faded out without one).
     */
    void watch(Sortie sortie) {
        if (eelParts == null) {
            noteKinds(sortie.enemyKinds());
        }
        boolean flying = sortie.flying() && !sortie.complete();
        boolean under = sortie.underwater();
        if (watchedOnce && under && !wasUnderwater && flying && !mix.resync) {
            bank.play(Sfx.WATERLINE_CROSS, PLAYER_DAMAGE, 1, 0);
            bank.play(Sfx.HYDRO_REFIT, PLAYER_DAMAGE, 1, 0);
        }
        wasUnderwater = under;
        watchedOnce = true;
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        double nearest = Double.POSITIVE_INFINITY;
        float humPan = 0;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (!is(pumps, enemy.kind())) {
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
                humPan = pan(x);
            }
        }
        float target = flying && nearest < Double.POSITIVE_INFINITY ? UNIT_LOOP_LEVEL : 0;
        humLevel = fadeLoop(Sfx.PUMP_HUM, humLevel, target, humPan);
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.PUMP_HUM) {
            humLevel = 0;
        }
    }
}
