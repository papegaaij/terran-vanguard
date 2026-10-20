package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/** {@link RadioTimeline}'s checks for Level 14 and its own tests. */
class RadioTimelineLevel14Test extends RadioTimeline {
    RadioTimelineLevel14Test() {
        super(LEVEL_14);
    }

    /**
     * M5 part H, step H9: Level 14's root-grid line (the secondary's, a closing line) plays as the
     * roots are cleared in a normal run (the last growth dies at about t=93.5, the radio free since
     * Okafor's t=62 line): it goes ahead of the Choir's t=96 line, which waits for it, and every timed
     * line still plays. If the roots finish late, while the Choir sings, it waits for a gap as before
     * and is still waiting when the Spire's warning starts (t=108), where the game drops it.
     */
    @Test
    void level14sRootGridLinePlaysWhenTheRootsAreClearedAndDropsWhenLate() {
        double warning = 108;
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (boolean voiced : new boolean[] {false, true}) {
                String where = difficulty + (voiced ? ", voiced" : "") + ": ";
                List<Played> played =
                        play(script, List.of(new Event(93.5, CueTrigger.SECONDARY_OBJECTIVE, "")), ESCORT, voiced);
                for (Played line : played) {
                    if (line.cue().trigger() == CueTrigger.SECONDARY_OBJECTIVE) {
                        double opened = line.opened().orElse(Double.NaN);
                        assertTrue(
                                opened >= 93.5 && opened <= 94, where + "the root-grid line opens at once: " + opened);
                    } else if (line.cue().trigger() == CueTrigger.TIME) {
                        assertTrue(
                                line.opened().isPresent(),
                                where + "the timed line at t=" + line.cue().t() + " plays");
                    }
                }
                for (Played line :
                        play(script, List.of(new Event(97, CueTrigger.SECONDARY_OBJECTIVE, "")), ESCORT, voiced)) {
                    if (line.cue().trigger() == CueTrigger.SECONDARY_OBJECTIVE) {
                        assertTrue(
                                line.opened().map(opened -> opened >= warning).orElse(true),
                                where + "late roots: the line still waits at the warning: " + line.opened());
                    } else if (line.cue().trigger() == CueTrigger.TIME) {
                        assertTrue(line.late() <= MAX_LATE_SECONDS, where + "late roots push no timed line back");
                    }
                }
            }
        }
    }

    /**
     * M5 part H, step H7b: the lines a Siege Spire phase queues together all play, voiced: Rook's reply
     * after Okafor's as it tears loose, in a fast and a slow fight, on every difficulty.
     */
    @Test
    void theSpiresPhaseLinesPlayOneAfterTheOther() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (double phase3 : new double[] {130, 165, 215}) {
                List<Played> played = play(
                        script,
                        List.of(
                                new Event(phase3 - 30, CueTrigger.BOSS_PHASE, "Maw"),
                                new Event(phase3, CueTrigger.BOSS_PHASE, "Free spire")),
                        ESCORT,
                        true);
                for (Played line : played) {
                    if (line.cue().trigger() == CueTrigger.BOSS_PHASE) {
                        assertTrue(
                                line.opened().isPresent(),
                                difficulty + ", phase 3 at " + phase3 + ": "
                                        + line.cue().speaker() + "'s "
                                        + line.cue().line() + " plays");
                    }
                }
            }
        }
    }
}
