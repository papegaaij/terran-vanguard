package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.RadioBlock;
import vanguard.sim.CueTrigger;
import vanguard.sim.SimStep;

/**
 * {@link RadioTimeline}'s checks for Level 21 (M7 part H), with and without Rook (every run is played with each fit).
 * Its lines are voiced (step VOX-H), the Choir's dying words with its layering on speech.
 */
class RadioTimelineLevel21Test extends RadioTimeline {
    RadioTimelineLevel21Test() {
        super(LEVEL_21);
    }

    /** The death's four lines come in order: the Choir's speech, then Varga, Okafor and Ares Control. */
    @Test
    void theDeathsLinesComeInOrder() {
        List<String> speakers = CONTENT.level(LEVEL_21).radio().stream()
                .filter(cue -> cue.event().orElse(null) == RadioBlock.CueEvent.BOSS_DESTROYED)
                .map(RadioBlock.RadioCue::speaker)
                .toList();
        assertEquals(List.of("The Choir", "Varga", "Okafor", "Ares Control"), speakers);
    }

    /**
     * Step HB: the death's lines are voiced, the Choir's words too (its layering on speech), and the Choir speaks from
     * its glyph in the portrait frame (its looping radio portrait).
     */
    @Test
    void theDeathsLinesAreVoicedAndTheChoirSpeaksFromItsGlyph() {
        for (RadioBlock.RadioCue cue : CONTENT.level(LEVEL_21).radio()) {
            if (cue.event().orElse(null) != RadioBlock.CueEvent.BOSS_DESTROYED) {
                continue;
            }
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, cue.speaker() + ": " + cue.line());
        }
        assertTrue(Files.isRegularFile(ASSETS.resolve("sprites/portraits/radio-the-choir-neutral_0.png")), "the glyph");
        assertTrue(Files.isRegularFile(ASSETS.resolve("sprites/portraits/radio-the-choir-neutral_1.png")), "a loop");
    }

    /**
     * Round 45 B.a (the user: the Choir's words are drowned by the death roar, "it needs a delay of about 4-5
     * seconds"): in every voiced run to the end the Choir's line opens its delay after the kill, not sooner and at
     * most a second later, and Varga's, Okafor's and Ares Control's lines queue behind it: each opens, in order,
     * once the line before it has finished speaking; the debrief waits for them (checked for every line by {@link
     * #closingLineVoicesEndWithinTheOutro}).
     */
    @Test
    void theChoirsDeathLineWaitsOutTheRoarAndTheOthersFollowIt() {
        LevelData data = CONTENT.level(LEVEL_21);
        int choir = -1;
        for (int i = 0; i < data.radio().size(); i++) {
            if (data.radio().get(i).event().orElse(null) == RadioBlock.CueEvent.BOSS_DESTROYED) {
                choir = i;
                break;
            }
        }
        double delay = data.radioDelay(choir);
        assertTrue(delay >= 4 && delay <= 5, "the Choir's line is held 4-5 s: " + delay);
        int runs = 0;
        for (EndRun run : endRuns()) {
            List<Played> deaths = run.played().stream()
                    .filter(line -> line.cue().trigger() == CueTrigger.BOSS_DESTROYED
                            && line.opened().isPresent())
                    .toList();
            if (deaths.isEmpty()) {
                continue;
            }
            runs++;
            assertEquals(
                    List.of("The Choir", "Varga", "Okafor", "Ares Control"),
                    deaths.stream().map(line -> line.cue().speaker()).toList(),
                    run.toString());
            Played first = deaths.getFirst();
            double held = first.opened().orElseThrow() - first.due();
            assertTrue(
                    held >= delay - SimStep.SECONDS - 1e-6 && held <= delay + 1,
                    "the Choir's line opens " + held + " s after the kill (" + run + ")");
            for (int i = 1; i < deaths.size(); i++) {
                Played before = deaths.get(i - 1);
                Played line = deaths.get(i);
                double spoken = before.opened().orElseThrow() + speaks(before.cue(), before.text());
                assertTrue(
                        line.opened().orElseThrow() >= spoken,
                        line.cue().speaker() + " opens at " + line.opened().get() + " before "
                                + before.cue().speaker() + " has spoken (" + spoken + "; " + run + ")");
            }
            double last = deaths.getLast().opened().orElseThrow()
                    + speaks(deaths.getLast().cue(), deaths.getLast().text());
            assertTrue(last <= run.handover(), "the debrief waits for Ares Control (" + run + ")");
        }
        assertTrue(runs > 0, "runs with the kill");
    }

    /** Rook's phase line only while he flies. */
    @Test
    void rooksPhaseLineRequiresTheEscort() {
        assertTrue(CONTENT.level(LEVEL_21).radio().stream()
                .filter(cue -> cue.speaker().equals("Rook"))
                .allMatch(cue -> cue.requires().orElse("").equals("escort")));
    }
}
