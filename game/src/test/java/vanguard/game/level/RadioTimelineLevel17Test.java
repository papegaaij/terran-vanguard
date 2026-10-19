package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.content.voice.VoiceLines;
import vanguard.sim.LevelScript;
import vanguard.sim.LevelScript.CueTrigger;

/**
 * {@link RadioTimeline}'s checks for Level 17 (M7 part D), with and without Rook (every run is played with
 * each fit, the escort's included), and its dome lines by name: each dome's {@code ally-hit} and {@code
 * ally-lost} line reads and speaks its name, and in the runs a dome's line is queued for each dome hit or lost.
 */
class RadioTimelineLevel17Test extends RadioTimeline {
    RadioTimelineLevel17Test() {
        super(LEVEL_17);
    }

    static final List<String> DOMES = List.of("Bradbury", "Lowell", "Schiaparelli", "Burroughs", "Eden");

    /** The five domes in the level's order, each with its first hit's and its loss's line rendered by name. */
    @Test
    void everyDomeLineIsVoicedByItsName() {
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_17, Difficulty.MEDIUM);
        List<String> names = script.groundAllies().orElseThrow().units().stream()
                .map(LevelScript.GroundAlly::name)
                .toList();
        assertEquals(DOMES, names, "Level 17's domes");
        for (CueTrigger trigger : List.of(CueTrigger.ALLY_HIT, CueTrigger.ALLY_LOST)) {
            List<LevelScript.RadioCue> cues = script.radio().stream()
                    .filter(cue -> cue.trigger() == trigger)
                    .toList();
            assertEquals(1, cues.size(), "one " + trigger + " line");
            LevelScript.RadioCue cue = cues.getFirst();
            assertTrue(cue.line().contains(VoiceLines.ALLY), trigger + " names the dome");
            for (String name : names) {
                String text = text(cue, SIDES.getFirst(), name);
                assertTrue(text.contains(name) && !text.contains("{"), text);
                assertTrue(voiceSeconds(cue.speaker(), text, cue.expression()) > 0, "voiced: " + text);
            }
        }
    }

    /**
     * In every run, with each fit, voiced: a dome's event sets off its line once, with its name. Event lines
     * wait for a gap and are dropped as stale after 6 s (Bradbury's first hit under Varga's bomber warning,
     * say): the dropped ones are printed for the user, the rest open.
     */
    @Test
    void eachDomeEventQueuesItsLineByName() {
        java.util.Set<String> dropped = new java.util.TreeSet<>();
        int opened = 0;
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, LEVEL_17, difficulty);
            for (List<Event> events : RUNS.get(LEVEL_17)) {
                for (int fitted : FITS) {
                    List<Played> played = play(script, events, fitted, true);
                    for (Event event : events) {
                        if (event.ally().isEmpty()) {
                            continue;
                        }
                        List<Played> lines = played.stream()
                                .filter(line -> line.cue().trigger() == event.trigger()
                                        && line.text().contains(event.ally()))
                                .toList();
                        assertEquals(1, lines.size(), difficulty + ", fit " + fitted + ", " + event + ": one line");
                        if (lines.getFirst().opened().isPresent()) {
                            opened++;
                        } else {
                            dropped.add(String.format(
                                    "t=%s %s (%s)", event.t(), lines.getFirst().text(), events.size() + " events"));
                        }
                    }
                }
            }
        }
        assertTrue(opened > 0, "dome lines open");
        dropped.forEach(line -> System.out.println("dome line dropped as stale in some runs: " + line));
    }
}
