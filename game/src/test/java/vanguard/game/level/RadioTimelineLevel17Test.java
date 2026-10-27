package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.content.voice.VoiceLines;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.RadioCue;

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
        List<String> names = script.objectives().groundAllies().orElseThrow().units().stream()
                .map(ObjectiveSpec.GroundAlly::name)
                .toList();
        assertEquals(DOMES, names, "Level 17's domes");
        for (CueTrigger trigger : List.of(CueTrigger.ALLY_HIT, CueTrigger.ALLY_LOST)) {
            List<RadioCue> cues = script.radio().stream()
                    .filter(cue -> cue.trigger() == trigger)
                    .toList();
            assertEquals(1, cues.size(), "one " + trigger + " line");
            RadioCue cue = cues.getFirst();
            assertTrue(cue.line().contains(VoiceLines.ALLY), trigger + " names the dome");
            for (String name : names) {
                String text = text(cue, SIDES.getFirst(), name);
                assertTrue(text.contains(name) && !text.contains("{"), text);
                assertTrue(voiceSeconds(cue.speaker(), text, cue.expression()) > 0, "voiced: " + text);
            }
        }
    }

    /**
     * Round 40 K.a: Varga's line about the Herald's rings is the {@code first-ring} event. In every run with it and no
     * dome line queued before it, with each fit, voiced, it opens within a second of the first ring (t≈55.8), while
     * the Herald still sings; behind a dome's lines it waits like them, or is dropped as stale (printed).
     */
    @Test
    void vargasRingLineOpensAtTheFirstRing() {
        int clear = 0;
        java.util.Set<String> waited = new java.util.TreeSet<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, LEVEL_17, difficulty);
            for (List<Event> events : RUNS.get(LEVEL_17)) {
                Event ring = events.stream()
                        .filter(event -> event.trigger() == CueTrigger.FIRST_RING)
                        .findFirst()
                        .orElse(null);
                if (ring == null) {
                    continue;
                }
                boolean domes = events.stream().anyMatch(event -> !event.ally().isEmpty() && event.t() < ring.t());
                for (int fitted : FITS) {
                    List<Played> lines = play(script, events, fitted, true).stream()
                            .filter(line -> line.cue().trigger() == CueTrigger.FIRST_RING)
                            .toList();
                    assertEquals(1, lines.size(), difficulty + ", fit " + fitted);
                    java.util.Optional<Double> opened = lines.getFirst().opened();
                    if (!domes) {
                        assertTrue(
                                opened.orElseThrow() - ring.t() <= 1,
                                difficulty + ", fit " + fitted + ": opened at " + opened);
                        clear++;
                    } else if (opened.isEmpty() || opened.get() - ring.t() > 1) {
                        waited.add(opened.map(t -> String.format("opened at t=%.1f", t))
                                .orElse("dropped as stale"));
                    }
                }
            }
        }
        assertTrue(clear > 0);
        waited.forEach(line -> System.out.println("Level 17 ring line behind the dome lines: " + line));
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
