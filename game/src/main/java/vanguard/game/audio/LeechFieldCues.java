package vanguard.game.audio;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PowerState;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M9 part F: Level 33's sounds for {@link FlightSounds} (design/audio/sfx, Act 5 sounds; step AUD-F's round 66
 * proposals, their a variants until the picks, and round 60's rail picks, all levelled in their files):
 *
 * <ul>
 *   <li>the rail line: its charge over the telegraph ({@link Sfx#RAIL_CHARGE} on {@code LINE_TELEGRAPH}, cut when no
 *       line telegraphs any more and none fired: the EMP's cancel) and its shot ({@link Sfx#RAIL_SHOT} on {@code
 *       LINE_FIRED});
 *   <li>the Void Leech: its latch on the hull ({@link Sfx#LEECH_LATCH}, in place of the Lamprey's, at its spot) or on a
 *       prey Talon (quieter, {@value #PREY_LATCH_LEVEL}), each leech that starts to drain ({@link Sfx#LEECH_DRAIN},
 *       {@value #DRAIN_DELAY_SECONDS} s after its latch), the hum while any drains ({@link Sfx#LEECH_HUM}, one loop
 *       ship-centred, its level by the latched count: {@link #humLevel}), a throw (a shake-off's or the EMP's: {@link
 *       Sfx#LEECH_SHAKE} once a step), its death over its rung ({@link Sfx#LEECH_DEATH});
 *   <li>the Rail Bunker's death over its rung ({@link Sfx#BUNKER_DEATH}).
 * </ul>
 *
 * <p>The brown-out warning with the hum's start is {@link BeltCues}' (any drain, not only the leech's).
 */
final class LeechFieldCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.LINE_TELEGRAPH,
            SimEvents.Type.LINE_FIRED,
            SimEvents.Type.PREY_LATCHED,
            SimEvents.Type.LATCH_THROWN);

    static final String LEECH = "void-leech";
    static final String BUNKER = "rail-bunker";

    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;
    /** A latch on a prey Talon, under the hull's. */
    static final float PREY_LATCH_LEVEL = 0.6f;
    /** A leech's drain starts this long after its latch (the output's −2 MW). */
    static final double DRAIN_DELAY_SECONDS = 0.1;
    /** The hum's level by the latched count (the file is three leeches'): −6 dB, −3 dB, full. */
    static final float[] HUM_LEVELS = {0, 0.5f, 0.71f, 1f};

    private boolean[] leeches;
    private boolean[] bunkers;
    private long now;
    /** The step of the last throw's sound (once a step). */
    private long lastThrow = Long.MIN_VALUE / 2;
    /** The drainers seen at the last watch, the lines telegraphing then, and whether a line fired since. */
    private int drainers;

    private int telegraphing;
    private boolean fired;

    LeechFieldCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The level's kinds: which are ours. */
    void noteKinds(List<EnemySpec> kinds) {
        leeches = new boolean[kinds.size()];
        bunkers = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            String slug = kinds.get(k).slug();
            leeches[k] = slug.equals(LEECH);
            bunkers[k] = slug.equals(BUNKER);
        }
    }

    private static boolean is(boolean[] kinds, int kind) {
        return kinds != null && kind >= 0 && kind < kinds.length && kinds[kind];
    }

    private static boolean any(boolean[] kinds) {
        if (kinds != null) {
            for (boolean kind : kinds) {
                if (kind) {
                    return true;
                }
            }
        }
        return false;
    }

    private void noted() {
        if (leeches == null && mix.watched != null) {
            noteKinds(mix.watched.enemyKinds());
        }
    }

    @Override
    void begin(long now) {
        if (now < this.now) {
            // A retry or a checkpoint.
            drainers = 0;
            telegraphing = 0;
            lastThrow = Long.MIN_VALUE / 2;
        }
        this.now = now;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        noted();
        switch (events.type(i)) {
            case LINE_TELEGRAPH -> bank.play(Sfx.RAIL_CHARGE, CUE_LEVEL, pitch(0.02), pan);
            case LINE_FIRED -> {
                bank.play(Sfx.RAIL_SHOT, CUE_LEVEL, pitch(0.04), pan);
                fired = true;
            }
            case PREY_LATCHED -> bank.play(Sfx.LEECH_LATCH, PREY_LATCH_LEVEL, pitch(0.05), pan);
            case LATCH_THROWN -> thrown(pan);
            default -> {}
        }
    }

    /**
     * A chaser latched on the hull ({@code LAMPREY_LATCHED}, UnitCues): a leech's latch in a level with leeches (true),
     * else false (the Lamprey's own).
     */
    boolean latched(float pan) {
        noted();
        if (!any(leeches)) {
            return false;
        }
        bank.play(Sfx.LEECH_LATCH, CUE_LEVEL, pitch(0.04), pan);
        return true;
    }

    /** The shake-off threw the latched ones off ({@code LAMPREY_SHAKEN}): a leech's throw (true), else false. */
    boolean shaken(float pan) {
        noted();
        if (!any(leeches)) {
            return false;
        }
        thrown(pan);
        return true;
    }

    private void thrown(float pan) {
        if (now != lastThrow) {
            bank.play(Sfx.LEECH_SHAKE, CUE_LEVEL, pitch(0.04), pan);
            lastThrow = now;
        }
    }

    /** A unit of {@code kind} was destroyed (UnitCues, over its rung): a leech's crack, a Rail Bunker's blast. */
    void destroyed(int kind, float pan) {
        noted();
        if (is(leeches, kind)) {
            bank.play(Sfx.LEECH_DEATH, CUE_LEVEL, pitch(0.05), pan);
        } else if (is(bunkers, kind)) {
            bank.play(Sfx.BUNKER_DEATH, CUE_LEVEL, pitch(0.03), pan);
        }
    }

    /** The hum's level for {@code drainers} latched drainers (a fourth drains nothing more: full). */
    static float humLevel(int drainers) {
        return HUM_LEVELS[Math.clamp(drainers, 0, HUM_LEVELS.length - 1)];
    }

    /** Once a step: the drain's whine for each new drainer, the hum's level, a cancelled rail charge cut. */
    void watch(Sortie sortie) {
        noted();
        boolean flying = sortie.flying() && !sortie.complete();
        PowerState power = sortie.power().isPresent() ? sortie.power().get() : null;
        int latched = flying && power != null ? power.drainers() : 0;
        for (int d = drainers; d < latched; d++) {
            later(Sfx.LEECH_DRAIN, CUE_LEVEL, pitch(0.04), 0, SimStep.ticks(DRAIN_DELAY_SECONDS));
        }
        drainers = latched;
        if (latched > 0) {
            if (!bank.looping(Sfx.LEECH_HUM)) {
                bank.loop(Sfx.LEECH_HUM, humLevel(latched));
            } else {
                bank.loopVolume(Sfx.LEECH_HUM, humLevel(latched));
            }
        } else if (bank.looping(Sfx.LEECH_HUM)) {
            bank.stop(Sfx.LEECH_HUM);
        }
        if (any(bunkers)) {
            int lines = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (is(bunkers, enemy.kind()) && enemy.lineBeam().telegraphing()) {
                    lines++;
                }
            }
            if (lines == 0 && telegraphing > 0 && !fired) {
                // The telegraph ended without its shot: the EMP cancelled it.
                bank.stop(Sfx.RAIL_CHARGE);
            }
            telegraphing = lines;
            fired = false;
        }
    }

    /** A loop stopped from outside (the level screen closing). */
    void silence(Sfx loop) {
        if (loop == Sfx.LEECH_HUM) {
            drainers = 0;
        }
    }
}
