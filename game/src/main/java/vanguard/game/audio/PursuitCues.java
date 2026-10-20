package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.UNIT_LOOP_LEVEL;

import java.util.EnumSet;
import java.util.Set;
import vanguard.sim.Enemy;
import vanguard.sim.Flock;
import vanguard.sim.PlayField;
import vanguard.sim.Pursuit;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part G: the pursuit swarm's sounds for {@link FlightSounds} (design/campaign Level 20, The pursuit swarm; design/
 * audio/sfx; round 43 a each, proposed, levelled in their files and played as they are). The band hum, one loop while
 * a pursuing flock has members on the screen, panned to its leader point and faded in and out over 0.2 s (its motes
 * leaving by the top at the break-off take it with them); and the lunge's rising hum at each tell: the 0.8 s sound
 * swells in and glides up through the tell, its whoosh landing on the dive (started later in a longer tell, at once
 * in a shorter one). No pop: the sounds open softly. The swarm's rush of wings as it enters is {@link UnitCues}'.
 */
final class PursuitCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(SimEvents.Type.PURSUIT_TELL);

    /** The sounds are levelled in their files (the hum −32 dB, the lunge −20 dB) and play as they are. */
    static final float PURSUIT_LEVEL = 1f;
    /** The lunge sound's length, s: its whoosh comes at its end, on the dive. */
    static final double LUNGE_SECONDS = 0.8;

    /** The band hum's level now (its fade). */
    private float humLevel;

    PursuitCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        if (events.type(i) != SimEvents.Type.PURSUIT_TELL) {
            return;
        }
        Sortie sortie = mix.watched;
        double tell = sortie == null
                ? LUNGE_SECONDS
                : sortie.pursuit().map(pursuit -> pursuit.spec().tellSeconds()).orElse(LUNGE_SECONDS);
        int delay = lungeDelay(tell);
        if (delay <= 0) {
            bank.play(Sfx.SWARM_LUNGE, PURSUIT_LEVEL, pitch(0.02), pan);
        } else {
            later(Sfx.SWARM_LUNGE, PURSUIT_LEVEL, pitch(0.02), pan, delay);
        }
    }

    /** The steps after a tell's start the lunge sound starts, so its end lands on the dive ({@code tell} s later). */
    static int lungeDelay(double tellSeconds) {
        return Math.max(0, SimStep.ticks(tellSeconds - LUNGE_SECONDS));
    }

    /** Once a step: the band hum while a pursuing flock has members on the screen, panned to its leader point. */
    void watch(Sortie sortie) {
        boolean flying = sortie.flying() && !sortie.complete();
        float pan = 0;
        boolean shown = false;
        for (int f = 0; f < sortie.flockCount() && !shown; f++) {
            Flock flock = sortie.flock(f);
            if (!flock.pursuing()) {
                continue;
            }
            shown = onScreen(flock);
            if (shown) {
                Pursuit pursuit = flock.pursuit().orElse(null);
                pan = pursuit == null ? 0 : pan(pursuit.leaderX());
            }
        }
        humLevel = fadeLoop(Sfx.SWARM_HUM, humLevel, hum(flying, shown), pan);
    }

    /** The hum's target level: on while the flight runs and a pursuing flock shows. */
    static float hum(boolean flying, boolean shown) {
        return flying && shown ? UNIT_LOOP_LEVEL : 0;
    }

    /** Whether any member of the flock is on the play field. */
    private static boolean onScreen(Flock flock) {
        for (int i = 0; i < flock.size(); i++) {
            Enemy member = flock.member(i);
            if (member != null && PlayField.overlaps(member.renderX(1), member.renderY(1), member.hitbox())) {
                return true;
            }
        }
        return false;
    }

    /** A loop the flight held is stopped (the level screen closes): its level is back to none. */
    void silence(Sfx loop) {
        if (loop == Sfx.SWARM_HUM) {
            humLevel = 0;
        }
    }
}
