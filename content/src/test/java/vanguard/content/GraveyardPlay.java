package vanguard.content;

import vanguard.sim.Sortie;
import vanguard.sim.VrellLevel;

/**
 * M11 part D, step D2 (design/campaign Level 46, the quiet; see {@link Autopilot}): the player's way through the quiet:
 * the guns held from its start until the Husks' crack cue (nothing fires unless the player fires first), so the drifting
 * hulls stay asleep and the dying Leviathan passes overhead untouched. No level before Level 46 has a quiet, so nothing
 * here moves another level's flights.
 */
final class GraveyardPlay {
    private GraveyardPlay() {}

    /** Whether the level's quiet holds the guns now: from its start to its end (the crack cue). */
    static boolean holdFire(Sortie sortie) {
        VrellLevel.Quiet quiet = sortie.script().vrell().quiet().orElse(null);
        if (quiet == null) {
            return false;
        }
        double t = sortie.levelSeconds();
        return t >= quiet.from() && t < quiet.to();
    }
}
