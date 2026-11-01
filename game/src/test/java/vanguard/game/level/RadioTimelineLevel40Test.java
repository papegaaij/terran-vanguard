package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.Channel;
import vanguard.content.Difficulty;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.RadioBlock;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 40 (M10 part F, step F4), with and without the escort (every run is played
 * with each fit; RadioRuns#level40Runs): the approach's timed lines, Sentry Two's recorder (Okafor's), the duel's lines
 * (PHASE: Harker's taunt and Rook's answer at the arrival, Vesper's and Kestrel's as they join, the first loss by the
 * senior survivor, "Just us now" by the last ace, Rook's "for Ceres" at Harker's death whenever it comes, his kill
 * included) and the closing line after the kill (Rook's, or Okafor's {@code requires_not: escort}). The aces speak on the
 * enemy channel with their portraits (Harker's the guard's {@code harker-guard}).
 */
class RadioTimelineLevel40Test extends RadioTimeline {
    private static final String FOR_CERES = "That one's for Ceres, Harker.";
    private static final Set<String> ACES = Set.of("Harker", "Vesper", "Kestrel");

    RadioTimelineLevel40Test() {
        super(LEVEL_40);
    }

    /**
     * Rook's "for Ceres" opens in every run where Harker dies while Rook flies: Harker first, second, or last (his death
     * the kill, his line raised after the boss's in the same step); never without Rook. Step F4's own check.
     */
    @Test
    void rookSaysForCeresWheneverHarkerDies() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                boolean harkerDown = events.stream().anyMatch(e -> e.subject().equals("harker-down"));
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        boolean rook = (fitted & ESCORT) != 0;
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        boolean ceres = played.stream()
                                .anyMatch(p -> p.cue().line().equals(FOR_CERES)
                                        && p.opened().isPresent());
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        assertEquals(harkerDown && rook, ceres, run + ": Rook's 'for Ceres'");
                        if (!rook) {
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
    }

    /**
     * The typical duel plays every line of its own: Harker's taunt (and Rook's answer), both joins, the first loss,
     * Harker's death, "Just us now" and the closing line; each opens, none goes stale.
     */
    @Test
    void theTypicalDuelPlaysEveryLine() {
        List<Event> typical = RUNS.get(level).get(1);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (boolean voiced : new boolean[] {false, true}) {
                for (int fitted : new int[] {0, ESCORT}) {
                    boolean rook = (fitted & ESCORT) != 0;
                    List<Played> played =
                            play(script, typical, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                    List<String> duel = played.stream()
                            .filter(p -> (p.cue().trigger() == CueTrigger.BOSS_ARRIVAL
                                            || p.cue().trigger() == CueTrigger.BOSS_PHASE
                                            || p.cue().trigger() == CueTrigger.BOSS_DESTROYED)
                                    && p.opened().isPresent())
                            .map(p -> p.cue().speaker())
                            .toList();
                    String run = difficulty + (voiced ? " voiced" : "") + " fit " + fitted;
                    assertEquals(
                            rook
                                    ? List.of(
                                            "Harker", "Rook", "Vesper", "Kestrel", "Harker", "Rook", "Kestrel", "Rook")
                                    : List.of("Harker", "Vesper", "Kestrel", "Harker", "Kestrel", "Okafor"),
                            duel,
                            run + ": the duel's lines");
                }
            }
        }
    }

    /** The aces speak on the enemy channel, each with its radio portrait (Harker's the guard's), every line voiced. */
    @Test
    void theAcesSpeakOnTheEnemyChannelWithTheirPortraits() {
        LevelData data = CONTENT.level(LEVEL_40);
        int aces = 0;
        for (RadioBlock.RadioCue cue : data.radio()) {
            if (!ACES.contains(cue.speaker())) {
                continue;
            }
            aces++;
            assertEquals(Channel.ENEMY, cue.channel().orElse(Channel.FRIENDLY), cue.line());
            String portrait = cue.portrait().orElse(cue.speaker().toLowerCase(java.util.Locale.ROOT));
            if (cue.speaker().equals("Harker")) {
                assertEquals("harker-guard", portrait, "Harker in the guard's suit");
            }
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(
                    Files.isRegularFile(
                            ASSETS.resolve("sprites/portraits/radio-" + portrait + "-" + expression + ".png")),
                    portrait + "'s radio portrait");
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, "voiced: " + cue.line());
        }
        assertEquals(9, aces, "the taunts, the first losses and the last aces' lines");
    }
}
