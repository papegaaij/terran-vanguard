package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 48 (M11 part F, step F2), with and without the escort (every run is played
 * with each fit; RadioRuns#level48Runs): the timed lines, Rook's barks (t=11, t=150, t=189 and the rear sweep's two)
 * only with him, the archive's line, the cores' secondary met or failed, Vorne's nine lines on the fight's triggers. Its
 * lines are voiced at the radio's noise step 5 (step VOX-EF); the Choir's two lines are clean; Vorne speaks on the
 * enemy channel in his fused portrait and fused voice, his one shout at the fusion's pain.
 */
class RadioTimelineLevel48Test extends RadioTimeline {
    RadioTimelineLevel48Test() {
        super(LEVEL_48);
    }

    /** The sweep barks only with Rook. */
    @Test
    void theSweepBarksNeedRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                for (int fitted : FITS) {
                    boolean rook = (fitted & ESCORT) != 0;
                    List<Played> played =
                            play(script, events, fitted, false, Double.NaN, new double[1], SIDES.getFirst(), 6);
                    if (!rook) {
                        assertTrue(!opened(played, CueTrigger.REAR_SWEEP), difficulty + " fit " + fitted + ": no bark");
                    }
                }
            }
        }
    }

    /** Vorne on the enemy channel with the fused portrait (his eight of the nine fight lines), one shout; step 5. */
    @Test
    void vorneSpeaksFusedAndShoutsOnce() {
        LevelData data = CONTENT.level(LEVEL_48);
        assertEquals(5, data.radioNoiseStep(), "the act's fifth noise step");
        int vorne = 0;
        int shouts = 0;
        for (var cue : data.radio()) {
            if (cue.speaker().equals("Vorne")) {
                vorne++;
                assertEquals("vorne-fused", cue.portrait().orElseThrow());
                shouts += cue.shout().orElse(false) ? 1 : 0;
            }
        }
        assertEquals(8, vorne, "Vorne's lines 1-3 and 5-9 (line 4 is the Choir's)");
        assertEquals(1, shouts, "his one shout");
    }

    private static boolean opened(List<Played> played, CueTrigger trigger) {
        return played.stream()
                .anyMatch(p -> p.cue().trigger() == trigger && p.opened().isPresent());
    }
}
