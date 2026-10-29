package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.RadioBlock;
import vanguard.content.SimSpecs;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;

/**
 * {@link RadioTimeline}'s checks for Level 35 (M9 part H, step H4), with and without the escort (every run is played
 * with each fit; RadioRuns#level35Runs): the approach's nine timed lines (two of them Rook's), the arena's arrival and
 * phase lines (PHASE: Vorne's and Varga's at the arrival, Vorne's at the first lock, Rook's at the second, Vorne's and
 * Varga's as the core opens) and the closing queue after the kill (CLOSING): Okafor, Vorne's offer, then Rook's answer
 * with him or Okafor's ({@code requires_not: escort}) without him, Varga and Ceres Control.
 */
class RadioTimelineLevel35Test extends RadioTimeline {
    private static final String ROOK_ANSWER = "Funny. We were just heading your way.";
    private static final String OKAFOR_ANSWER = "Our pilots are not for sale, Vorne.";

    RadioTimelineLevel35Test() {
        super(LEVEL_35);
    }

    /**
     * After the kill every closing line the fit allows opens, in the data's order and each after the one before it: Rook answers Vorne
     * while he flies, Okafor when he does not (never both), text and voiced, on every difficulty.
     */
    @Test
    void theClosingQueueAnswersVorneWithRookOrWithOkafor() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                if (events.stream().noneMatch(e -> e.trigger() == CueTrigger.BOSS_DESTROYED)) {
                    continue;
                }
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : FITS) {
                        boolean rook = (fitted & ESCORT) != 0;
                        List<Played> played =
                                play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        List<Played> closing = played.stream()
                                .filter(p -> p.cue().trigger() == CueTrigger.BOSS_DESTROYED
                                        && p.opened().isPresent())
                                .toList();
                        String run = difficulty + " " + events + (voiced ? " voiced" : "") + " fit " + fitted;
                        assertEquals(
                                rook
                                        ? List.of("Okafor", "Vorne", "Rook", "Varga", "Ceres Hub Control")
                                        : List.of("Okafor", "Vorne", "Okafor", "Varga", "Ceres Hub Control"),
                                closing.stream().map(p -> p.cue().speaker()).toList(),
                                run + ": the closing lines");
                        assertTrue(
                                closing.stream()
                                        .anyMatch(p -> p.cue().line().equals(rook ? ROOK_ANSWER : OKAFOR_ANSWER)),
                                run + ": the answer to Vorne's offer");
                        double last = Double.NEGATIVE_INFINITY;
                        for (Played line : closing) {
                            assertTrue(
                                    line.opened().get() >= last,
                                    run + ": in order: " + line.cue().line());
                            last = line.opened().get();
                        }
                    }
                }
            }
        }
    }

    /**
     * The arena's phase lines in the typical fight (step H3: the locks 26, 28 and 33 s into the halt, the kill at 148 s): Vorne's welcome and
     * Varga's at the arrival, Vorne's at the first lock, Rook's at the second while he flies, Vorne's and Varga's as the
     * core opens; each opens (none goes stale behind another, none is left out), with and without Rook.
     */
    @Test
    void theTypicalFightPlaysEveryPhaseLine() {
        List<Event> typical = RUNS.get(level).get(1);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (boolean voiced : new boolean[] {false, true}) {
                for (int fitted : new int[] {0, ESCORT}) {
                    boolean rook = (fitted & ESCORT) != 0;
                    List<Played> played =
                            play(script, typical, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                    List<Played> phase = played.stream()
                            .filter(p -> (p.cue().trigger() == CueTrigger.BOSS_ARRIVAL
                                            || p.cue().trigger() == CueTrigger.BOSS_PHASE)
                                    && p.opened().isPresent())
                            .toList();
                    String run = difficulty + (voiced ? " voiced" : "") + " fit " + fitted;
                    assertEquals(
                            rook
                                    ? List.of("Vorne", "Varga", "Vorne", "Rook", "Vorne", "Varga")
                                    : List.of("Vorne", "Varga", "Vorne", "Vorne", "Varga"),
                            phase.stream().map(p -> p.cue().speaker()).toList(),
                            run + ": the arena's lines");
                }
            }
        }
    }

    /**
     * Vorne speaks on the enemy channel in his moods (design/campaign Level 35, Radio chatter): smug at the arrival,
     * neutral at the first lock and in his offer, rage (cold) as the core opens; each mood has its radio portrait, and
     * each of his lines its voice.
     */
    @Test
    void vorneWearsHisMoodsOnTheEnemyChannel() {
        LevelData data = CONTENT.level(LEVEL_35);
        List<String> moods = data.radio().stream()
                .filter(cue -> cue.speaker().equals(Expression.VORNE))
                .map(cue -> cue.expression().orElse(Expression.NEUTRAL).slug())
                .toList();
        assertEquals(List.of("smug", "neutral", "rage", "neutral"), moods);
        for (RadioBlock.RadioCue cue : data.radio()) {
            if (!cue.speaker().equals(Expression.VORNE)) {
                continue;
            }
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            assertTrue(
                    Files.isRegularFile(ASSETS.resolve("sprites/portraits/radio-vorne-" + expression + ".png")),
                    "Vorne's " + expression + " radio portrait");
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, "voiced: " + cue.line());
        }
    }
}
