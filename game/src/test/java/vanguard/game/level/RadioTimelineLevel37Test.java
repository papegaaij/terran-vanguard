package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Channel;
import vanguard.content.Difficulty;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 37 (M10 part C, step C4b), with and without the escort (every run is played
 * with each fit: Rook flies in the escort slot, {@code --escort none} without him; RadioRuns#level37Runs): the timed
 * lines (step C4b retimed them to the game's typed pages, pending round 75), the shuttles' secondary met or failed
 * (Aurelia Traffic's thanks or Okafor's line, both closing lines: they wait for the gap after Varga's t=129.5 line),
 * the first bounce's and the first prey kill's barks (Rook's), the CDF locker's line (Okafor's); the Choir's line
 * distorted with its 2 s static lead and the music's 6 dB duck, Vorne's on the enemy channel. Its lines are voiced
 * (step VOX-C).
 */
class RadioTimelineLevel37Test extends RadioTimeline {
    RadioTimelineLevel37Test() {
        super(LEVEL_37);
    }

    /**
     * The secondary's outcome is always told: met, Aurelia Traffic's thanks; failed, Okafor's line; never both, never
     * left out, voiced or not, with Rook or without him; both queue as closing lines.
     */
    @Test
    void theShuttlesOutcomeIsAlwaysTold() {
        LevelScript medium = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        RadioSchedule schedule = new RadioSchedule(medium);
        for (int c = 0; c < medium.radio().size(); c++) {
            CueTrigger trigger = medium.radio().get(c).trigger();
            if (trigger == CueTrigger.SECONDARY_FAILED || trigger == CueTrigger.SECONDARY_OBJECTIVE) {
                assertEquals(RadioQueue.Priority.CLOSING, schedule.priority(c), trigger.name());
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                boolean met = events.stream().anyMatch(e -> e.trigger() == CueTrigger.SECONDARY_OBJECTIVE);
                boolean failed = events.stream().anyMatch(e -> e.trigger() == CueTrigger.SECONDARY_FAILED);
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        assertEquals(met, opened(played, CueTrigger.SECONDARY_OBJECTIVE), run + ": the met line");
                        assertEquals(failed, opened(played, CueTrigger.SECONDARY_FAILED), run + ": the failed line");
                    }
                }
            }
        }
    }

    /**
     * The first bounce's and the first prey kill's barks are Rook's: they play only while he flies (or wait and go
     * stale); his t=56 line is scripted and plays with every fit (the data's: no {@code requires}).
     */
    @Test
    void rooksBarksNeedRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        boolean rook = (fitted & ESCORT) != 0;
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        if (!rook) {
                            assertTrue(!opened(played, CueTrigger.FIRST_BOUNCE), run + ": the bounce bark needs Rook");
                            assertTrue(!opened(played, CueTrigger.FIRST_PREY_KILL), run + ": the prey bark needs Rook");
                        }
                        boolean scripted = played.stream()
                                .anyMatch(p -> p.cue().trigger() == CueTrigger.TIME
                                        && p.cue().t() == 56
                                        && p.opened().isPresent());
                        assertTrue(scripted, run + ": Rook's t=56 line");
                    }
                }
            }
        }
    }

    /**
     * The CDF locker's line is Okafor's (the secret's {@code radio}). Step C4b: in the runs it never finds a gap (the
     * timed lines fill t=1-100 on the typed pages and its line goes stale after 6 s), as the first bounce's, the first
     * ally lost's and the first prey kill's do before t≈140: pending the user's review in round 75 (shorter lines).
     */
    @Test
    void theLockersLineIsOkafors() {
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        assertTrue(
                script.radio().stream()
                        .anyMatch(cue -> cue.trigger() == CueTrigger.SECRET
                                && cue.speaker().equals("Okafor")),
                "Okafor's locker line");
    }

    /**
     * The Choir's line at t=100: distorted, its static from t=98 (the portrait's interference swelling over the 2 s
     * before it), the music ducked 6 dB under it; Vorne's at t=110 over the enemy channel; every line voiced.
     */
    @Test
    void theChoirsStaticAndDuckVorneOnTheEnemyChannelAndEveryLineVoiced() {
        LevelData data = CONTENT.level(LEVEL_37);
        LevelData.RadioCue choir = data.radio().stream()
                .filter(cue -> cue.speaker().equals("The Choir"))
                .findFirst()
                .orElseThrow();
        assertTrue(choir.distorted().orElse(false), "distorted");
        assertEquals(2, choir.staticLeadSeconds(), 1e-9, "its 2 s static lead");
        assertEquals(6, choir.musicDuckDb(), 1e-9, "the music ducked 6 dB");
        StaticLeads leads = new StaticLeads(data.radio());
        assertEquals(0, leads.strength(97.9));
        assertEquals(0.5, leads.strength(99), 1e-6, "half way through its lead");
        assertTrue(leads.started(97.99, 98.01), "its static from t=98");
        LevelData.RadioCue vorne = data.radio().stream()
                .filter(cue -> cue.speaker().equals(Expression.VORNE))
                .findFirst()
                .orElseThrow();
        assertEquals(Channel.ENEMY, vorne.channel(CONTENT.voices()), "Vorne on the enemy channel");
        for (var cue : data.radio()) {
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, "voiced: " + cue.line());
        }
    }

    private static boolean opened(List<Played> played, CueTrigger trigger) {
        return played.stream()
                .anyMatch(p -> p.cue().trigger() == trigger && p.opened().isPresent());
    }
}
