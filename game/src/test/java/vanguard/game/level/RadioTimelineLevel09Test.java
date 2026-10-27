package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;
import static vanguard.game.level.RadioRuns.holdRuns;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.game.level.RadioRuns.HoldRun;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/** {@link RadioTimeline}'s checks for Level 09 and its own tests. */
class RadioTimelineLevel09Test extends RadioTimeline {
    RadioTimelineLevel09Test() {
        super(LEVEL_09);
    }

    /**
     * The holds are flown where Level 09's nodes reach their depth: three, in order, at a fifth of the
     * scroll on medium (30 of 150 px/s), the last one running on through the collapse.
     */
    @Test
    void level09sHoldsAreFlownWhereItsNodesReachTheirDepth() {
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        List<HoldRun> holds = holdRuns(script, 6);
        assertTrue(holds.size() == 3, "three holds: " + holds);
        for (int h = 0; h < holds.size(); h++) {
            HoldRun hold = holds.get(h);
            assertTrue(Double.isFinite(hold.start()) && hold.start() < script.seconds(), "hold " + h + ": " + hold);
            assertTrue(Math.abs(hold.ratio() - 0.2) < 1e-9, "hold " + h + " at a fifth: " + hold.ratio());
            assertTrue(h == 0 || hold.start() > holds.get(h - 1).start(), "in order");
        }
        assertTrue(
                holds.getLast().collapse() && holds.getLast().seconds() > 6, "the last runs on through the collapse");
        // A line inside a hold is due in real time: the Choir's 152.5 (between two holds) still plays at most 1 s late.
        for (Played line : play(script, List.of(), ESCORT, false, Double.NaN, new double[1], SIDES.getFirst(), 10.6)) {
            if (line.cue().trigger() == CueTrigger.TIME) {
                assertTrue(line.opened().isPresent() && line.late() <= MAX_LATE_SECONDS, line.toString());
            }
            if (line.cue().trigger() == CueTrigger.HOLD_START || line.cue().trigger() == CueTrigger.COLLAPSE) {
                assertTrue(line.opened().isPresent(), "the " + line.cue().trigger() + " line plays");
            }
        }
    }
}
