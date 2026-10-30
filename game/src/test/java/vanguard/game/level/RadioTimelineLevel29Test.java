package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Sortie;

/**
 * {@link RadioTimeline}'s checks for Level 29 (M9 part B, steps B4 and B4b), with and without the escort (every run is
 * played with each fit; Rook's lines are scripted, the Bulwark has no radio): the timed lines through the turn, Harker
 * on the enemy channel, the wavering pairs' lines, the rescue halt on its held clock (the last clamp's line for the
 * swap's outcome, Rook's rescue line) and the closing line. The runs stand for the loaned Bulwark, an owned one, the
 * loaner lost before the swap and {@code --escort none} (RadioRuns#act5). Step B4b trimmed the lines to one HUD page
 * each and retimed four (pending round 62), so the timing checks run.
 */
class RadioTimelineLevel29Test extends RadioTimeline {
    private static final String HAWK = "wavering-stormhawk";

    RadioTimelineLevel29Test() {
        super(LEVEL_29);
    }

    /**
     * Step FIX6 (the user's playthrough 2026-10-30): Varga's "white blinkers" warning ends before the first wavering
     * pair's first pixel is on the screen (the sim's spawn, its sprite's height), on every difficulty, so the player is
     * told before the pair can be shot.
     */
    @Test
    void theHoldFireWarningEndsBeforeTheFirstPairIsOnTheScreen() {
        double half = CONTENT.enemy(HAWK).size().height() / 2;
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            RadioCue warning = script.radio().stream()
                    .filter(cue ->
                            cue.trigger() == CueTrigger.TIME && cue.line().contains("white blinkers"))
                    .findFirst()
                    .orElseThrow();
            double ends = warning.t() + voiceSeconds(warning, "left");
            Sortie sortie = new Sortie(
                    1,
                    SimSpecs.starterLoadout(CONTENT, difficulty),
                    script,
                    SimSpecs.rules(CONTENT, level, difficulty).withInvulnerableShip(),
                    60);
            double seen = Double.NaN;
            while (Double.isNaN(seen) && sortie.levelSeconds() < 120) {
                sortie.step(0);
                for (int e = 0; e < sortie.enemyCount(); e++) {
                    Enemy enemy = sortie.enemy(e);
                    if (enemy.spec().neutral() && enemy.renderY(1) - half < PlayField.HEIGHT) {
                        seen = sortie.levelSeconds();
                    }
                }
            }
            assertTrue(
                    ends <= seen,
                    String.format(
                            "%s: the warning (t=%s) ends at %.2f s, the first pair is on the screen at %.2f s",
                            difficulty, warning.t(), ends, seen));
            System.out.printf(
                    "%s: Varga's warning ends at %.2f s, the first wavering pixel at %.2f s%n", difficulty, ends, seen);
        }
    }
}
