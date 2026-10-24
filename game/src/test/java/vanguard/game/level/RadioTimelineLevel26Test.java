package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.LevelScript;
import vanguard.sim.RookLossSpec;

/**
 * {@link RadioTimeline}'s checks for Level 26 (M8 part F), with and without Rook (every run is played with each fit).
 * Its lines are voiced (step VOX-F), Rook's three scripted lines and the Choir's words among them.
 */
class RadioTimelineLevel26Test extends RadioTimeline {
    RadioTimelineLevel26Test() {
        super(LEVEL_26);
    }

    /**
     * Rook's cut line and his loss (the user's playtest of 2026-10-24: "the line comes several seconds after he's been
     * shot down"): with the voices, on every difficulty, "Lancer, something's below u—" opens before the trails rise and
     * the hits land while his voice still speaks, in its last word (the take's "u" from about 2.3 s), so the hits cut it.
     */
    @Test
    void theHitsCutRooksLastLine() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, LEVEL_26, difficulty);
            RookLossSpec loss = script.rookLoss().orElseThrow();
            for (int fitted : FITS) {
                Played cut = play(script, List.of(), fitted, true).stream()
                        .filter(line -> line.cue().line().startsWith("Lancer, something's below"))
                        .findFirst()
                        .orElseThrow();
                double opened = cut.opened().orElseThrow();
                double voice =
                        voiceSeconds(cut.cue().speaker(), cut.text(), cut.cue().expression());
                String where = difficulty + ", fit " + fitted + ": opened at " + opened + ", the hits at " + loss.t();
                assertTrue(opened < loss.trails(), where + ": before the trails");
                assertTrue(loss.t() - opened >= 2.0, where + ": the hits about 2 s into the line");
                assertTrue(loss.t() < opened + voice, where + ": the hits land while he speaks (" + voice + " s)");
            }
        }
    }
}
