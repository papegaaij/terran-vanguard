package vanguard.sim;

import java.util.List;

/**
 * Step PT2 (the user's Level 50 playthrough, 2026-11-12: "keep the intensity but add breaks"; design/enemies/bosses/
 * choir-heart, Phases: the lulls): a boss's <b>lulls</b> ({@link BossLulls}): in every phase, once the phase has fired
 * {@code everySeconds − seconds} s since its start or since the last lull's end, the next lull is due; it
 * starts as soon as the playbook rests (and the phase's opening is over) (no move is drawn meanwhile) and lasts {@code
 * seconds}: the phase's attacks but the {@code keep} ones are silent (their clocks run on), the playbook draws nothing,
 * and with {@code open} every living windowed part ({@link PartWindows}) stays open through it.
 *
 * @param everySeconds s from one lull's start to the next (with no deferral)
 * @param seconds a lull's length, s
 * @param keep the attacks (indices into {@link BossSpec#attacks()}) that fire on through a lull (the heartbeat)
 * @param open whether the windowed parts open for the lull (the parts stay hittable)
 */
public record BossLullsSpec(double everySeconds, double seconds, List<Integer> keep, boolean open) {
    public BossLullsSpec {
        keep = List.copyOf(keep);
        if (!(seconds > 0) || !(everySeconds > seconds)) {
            throw new IllegalArgumentException(
                    "a lull lasts a while, shorter than its period: " + seconds + " every " + everySeconds);
        }
    }

    /** Checks its kept attacks against the boss's {@code attacks}. */
    void checkAttacks(String slug, int attacks) {
        for (int a : keep) {
            if (a < 0 || a >= attacks) {
                throw new IllegalArgumentException(slug + ": a lull keeps one of its attacks: " + a);
            }
        }
    }
}
