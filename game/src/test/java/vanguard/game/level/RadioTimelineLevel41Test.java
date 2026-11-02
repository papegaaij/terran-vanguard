package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.RadioBlock;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 41 (M10 part G, step G4), with and without the escort (every run is played
 * with each fit; RadioRuns#level41Runs): the timed lines on the clock that stands in the four halts, the halts' lines,
 * Varga's file lines (0 to 3 files), short and long halts, and the Silence line after the Halo's core ({@code
 * halo-destroyed}, {@code delay: 2}): it never goes stale ({@code stale: never}, its row's −6 dB duck) and always plays,
 * voiced or not, with Rook or without. The event lines that still go stale in a run are listed for the report (a file
 * line queued behind a halt's line may wait out its 6 s).
 */
class RadioTimelineLevel41Test extends RadioTimeline {
    private static final String SILENCE = "Vorne's files call it the Silence.";

    RadioTimelineLevel41Test() {
        super(LEVEL_41);
    }

    /** The Silence line's row: never stale, 2 s after the Halo's core, the music ducked 6 dB under it, Varga's grim. */
    @Test
    void theSilenceLineIsNeverStaleAndDucksTheMusic() {
        LevelData data = CONTENT.level(LEVEL_41);
        RadioBlock.RadioCue silence = data.radio().stream()
                .filter(cue -> cue.line().endsWith(SILENCE))
                .findFirst()
                .orElseThrow();
        int index = data.radio().indexOf(silence);
        assertTrue(silence.neverStale(), "stale: never");
        assertEquals(2.0, data.radioDelay(index), 1e-9, "2 s after the core");
        assertEquals(6.0, silence.musicDuckDb(), 1e-9, "the music ducks 6 dB under it");
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_41, Difficulty.MEDIUM);
        assertEquals(CueTrigger.HALO_DESTROYED, script.radio().get(index).trigger());
        RadioSchedule schedule = new RadioSchedule(script);
        assertEquals(RadioQueue.Priority.CLOSING, schedule.priority(index, true), "it waits for the radio and plays");
        assertEquals(RadioQueue.Priority.EVENT, schedule.priority(index, false), "as an event line it could go stale");
    }

    /**
     * In every run, on every difficulty, voiced or not, with or without Rook, the Silence line opens after the Halo's core
     * dies; Rook's lines never play without him; the halts' lines of the typical run all open.
     */
    @Test
    void theSilenceLinePlaysInEveryRunAndRookOnlyWithHim() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : new int[] {0, ESCORT}) {
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        assertTrue(
                                played.stream()
                                        .anyMatch(p -> p.cue().line().endsWith(SILENCE)
                                                && p.opened().isPresent()),
                                run + ": the Silence line plays");
                        if ((fitted & ESCORT) == 0) {
                            assertTrue(
                                    played.stream()
                                            .noneMatch(p -> p.cue().speaker().equals("Rook")
                                                    && p.opened().isPresent()),
                                    run + ": no Rook line without him");
                        }
                    }
                }
            }
        }
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        List<Event> typical = RUNS.get(level).getFirst();
        for (boolean voiced : new boolean[] {false, true}) {
            List<Played> played = play(script, typical, ESCORT, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
            for (Played line : played) {
                if (line.cue().trigger() == CueTrigger.GATE_HALT) {
                    assertTrue(line.opened().isPresent(), (voiced ? "voiced: " : "") + "the halt line " + line.text());
                }
            }
        }
    }

    /**
     * The event lines that go stale in the runs (voiced, with Rook, medium), listed for the report: each by its line and
     * the runs it is lost in. Not a failure: the halts are as long as the player makes them.
     */
    @Test
    void theStaleEventLinesAreListed() {
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        Map<String, Integer> stale = new TreeMap<>();
        int runs = 0;
        for (List<Event> events : RUNS.get(level)) {
            for (boolean voiced : new boolean[] {false, true}) {
                runs++;
                for (Played line :
                        play(script, events, ESCORT, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6)) {
                    // Queued (due) but never opened: dropped as stale.
                    if (line.cue().trigger() != CueTrigger.TIME
                            && line.due() > 0
                            && line.opened().isEmpty()) {
                        stale.merge(
                                (voiced ? "voiced " : "text ") + line.cue().speaker() + ": " + line.text(),
                                1,
                                Integer::sum);
                    }
                }
            }
        }
        System.out.println("Level 41: event lines gone stale (of " + runs + " runs): " + stale);
        assertTrue(stale.keySet().stream().noneMatch(line -> line.endsWith(SILENCE)), "never the Silence line");
    }
}
