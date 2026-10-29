package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 33 (M9 part F, step F4), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot): the timed lines (step F2 trimmed each to one HUD page, the Choir's two
 * at t=118 with its static lead and duck), *Claim 114*'s line and the secondary met (RadioRuns#level33Runs). Its 18
 * lines are voiced (step F2).
 */
class RadioTimelineLevel33Test extends RadioTimeline {
    RadioTimelineLevel33Test() {
        super(LEVEL_33);
    }

    /** The secret's line is Rook's: it plays only while he flies (requires: escort), voiced or not. */
    @Test
    void theSecretsLineNeedsRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                if (events.stream().noneMatch(e -> e.trigger() == CueTrigger.SECRET)) {
                    continue;
                }
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        boolean spoken = played.stream()
                                .anyMatch(p -> p.cue().trigger() == CueTrigger.SECRET
                                        && p.opened().isPresent());
                        boolean rook = (fitted & ESCORT) != 0;
                        assertTrue(
                                spoken == rook,
                                difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted
                                        + ": the secret's line " + (rook ? "plays" : "waits for Rook"));
                    }
                }
            }
        }
    }
}
