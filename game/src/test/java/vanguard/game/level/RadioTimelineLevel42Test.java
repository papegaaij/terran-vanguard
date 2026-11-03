package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;

import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
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
 * {@link RadioTimeline}'s checks for Level 42 (M10 part H, step H4b), with and without the escort (every run is played
 * with each fit; RadioRuns#level42Runs): the approach's timed lines, the tender's strongbox (Okafor's), the fight's
 * lines on the halted clock as {@code PHASE} lines (Vorne's arrival and Varga after it, his taunts at the first generator
 * and the cannon's half with Okafor's and Rook's answers, the Choir and Vorne's one shout at the eruption with Varga
 * after, "Two sacs down" at the heart's opening, Okafor at K, Vorne's goodbye at the breakout), dropped at the escape,
 * and the closing lines as {@code CLOSING} lines from {@code ESCAPED} (Varga's 2 s after, Rook's with him, Okafor's "Then
 * we follow him." last, at least 4 s after it).
 */
class RadioTimelineLevel42Test extends RadioTimeline {
    private static final String LAST = "Then we follow him.";

    RadioTimelineLevel42Test() {
        super(LEVEL_42);
    }

    /** The fight's and the closing lines that opened, by speaker, in the order they opened. */
    private static List<String> fight(List<Played> played) {
        return played.stream()
                .filter(p -> p.opened().isPresent()
                        && (p.cue().trigger() == CueTrigger.BOSS_ARRIVAL
                                || p.cue().trigger() == CueTrigger.BOSS_PHASE
                                || p.cue().trigger() == CueTrigger.BOSS_ESCAPED))
                .sorted(Comparator.comparingDouble(p -> p.opened().orElseThrow()))
                .map(p -> p.cue().speaker())
                .toList();
    }

    /**
     * The typical fight plays every line of its own in the boss document's order, on every difficulty, voiced or not:
     * each opens, none goes stale; Rook's only with him.
     */
    @Test
    void theTypicalFightPlaysEveryLineInOrder() {
        List<Event> typical = RUNS.get(level).get(1);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (boolean voiced : new boolean[] {false, true}) {
                for (int fitted : new int[] {0, ESCORT}) {
                    boolean rook = (fitted & ESCORT) != 0;
                    List<Played> played =
                            play(script, typical, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                    String run = difficulty + (voiced ? " voiced" : "") + " fit " + fitted;
                    assertEquals(
                            rook
                                    ? List.of(
                                            "Vorne",
                                            "Varga",
                                            "Vorne",
                                            "Okafor",
                                            "Vorne",
                                            "Rook",
                                            "The Choir",
                                            "Vorne",
                                            "Varga",
                                            "Varga",
                                            "Okafor",
                                            "Vorne",
                                            "Varga",
                                            "Rook",
                                            "Okafor")
                                    : List.of(
                                            "Vorne",
                                            "Varga",
                                            "Vorne",
                                            "Okafor",
                                            "Vorne",
                                            "The Choir",
                                            "Vorne",
                                            "Varga",
                                            "Varga",
                                            "Okafor",
                                            "Vorne",
                                            "Varga",
                                            "Okafor"),
                            fight(played),
                            run + ": the fight's lines");
                }
            }
        }
    }

    /**
     * In every run, on every difficulty, voiced or not, with or without Rook: the closing lines open in order from
     * {@code ESCAPED} (Varga's at least 2 s after it, Okafor's "Then we follow him." last and at least 4 s after it, the
     * level's last line); no line of the fight opens after the escape (its {@code PHASE} lines dropped there); Rook's
     * lines only with him.
     */
    @Test
    void theClosingLinesFollowTheEscapeAndTheFightsLinesAreDroppedThere() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : RUNS.get(level)) {
                if (events.stream().noneMatch(e -> e.trigger() == CueTrigger.BOSS_ESCAPED)) {
                    continue;
                }
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : new int[] {0, ESCORT}) {
                        closing(script, events, fitted, voiced, difficulty + " " + events);
                    }
                }
            }
        }
    }

    /**
     * A fight whose last phases come thick and fast (the heart open 3 s after the eruption, K 3 s after that, the 4 s
     * climb): the lines still waiting at the escape are dropped, never heard after it; the closing lines all play.
     */
    @Test
    void thePhaseLinesStillWaitingAtTheEscapeAreDropped() {
        List<Event> rushed = RadioRuns.ascendant42(null, 20, 24, 40, 43, 46);
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        for (boolean voiced : new boolean[] {false, true}) {
            for (int fitted : new int[] {0, ESCORT}) {
                List<Played> played = closing(script, rushed, fitted, voiced, "rushed");
                assertTrue(
                        played.stream()
                                .anyMatch(p -> p.cue().trigger() == CueTrigger.BOSS_PHASE
                                        && p.due() > 0
                                        && p.opened().isEmpty()),
                        "some fight lines were still waiting at the escape");
            }
        }
    }

    private List<Played> closing(LevelScript script, List<Event> events, int fitted, boolean voiced, String name) {
        boolean rook = (fitted & ESCORT) != 0;
        List<Played> played = play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
        String run = name + (voiced ? " voiced" : "") + " fit " + fitted;
        List<Played> closing = played.stream()
                .filter(p -> p.cue().trigger() == CueTrigger.BOSS_ESCAPED)
                .filter(p -> rook || !p.cue().speaker().equals("Rook"))
                .toList();
        assertEquals(rook ? 3 : 2, closing.size(), run);
        double escaped = closing.getFirst().due();
        double last = Double.NEGATIVE_INFINITY;
        for (Played line : closing) {
            assertTrue(line.opened().isPresent(), run + ": the closing line plays: " + line.text());
            assertTrue(line.opened().get() > last, run + ": in order: " + line.text());
            last = line.opened().get();
        }
        assertTrue(closing.getFirst().opened().get() >= escaped + 2 - 1e-6, run + ": Varga's 2 s after ESCAPED");
        Played okafor = closing.getLast();
        assertEquals(LAST, okafor.text(), run);
        assertTrue(okafor.opened().get() >= escaped + 4, run + ": Okafor's last line at least 4 s after ESCAPED");
        for (Played line : played) {
            if (line.opened().isEmpty()) {
                continue;
            }
            if (line.cue().trigger() == CueTrigger.BOSS_PHASE || line.cue().trigger() == CueTrigger.BOSS_ARRIVAL) {
                assertTrue(line.opened().get() < escaped, run + ": no fight line after the escape: " + line.text());
            }
            assertTrue(line.opened().get() <= okafor.opened().get(), run + ": Okafor's is the last: " + line.text());
            if (!rook) {
                assertTrue(!line.cue().speaker().equals("Rook"), run + ": no Rook line without him");
            }
        }
        return played;
    }

    /**
     * Vorne speaks on the enemy channel with his portraits, every line voiced; his line 5 is the campaign's one shout
     * (the voice check's exception), the eruption's, right after the Choir's line with its own 6 dB duck; the closing
     * lines queue as {@code CLOSING} (never stale), the fight's as {@code PHASE}.
     */
    @Test
    void vorneTheChoirAndTheShout() {
        LevelData data = CONTENT.level(LEVEL_42);
        LevelScript script = SimSpecs.level(CONTENT, LEVEL_42, Difficulty.MEDIUM);
        RadioSchedule schedule = new RadioSchedule(script);
        int vorne = 0;
        int shouts = 0;
        for (int i = 0; i < data.radio().size(); i++) {
            RadioBlock.RadioCue cue = data.radio().get(i);
            CueTrigger trigger = script.radio().get(i).trigger();
            if (trigger == CueTrigger.BOSS_ESCAPED) {
                assertEquals(RadioQueue.Priority.CLOSING, schedule.priority(i), cue.line());
            } else if (trigger == CueTrigger.BOSS_PHASE || trigger == CueTrigger.BOSS_ARRIVAL) {
                assertEquals(RadioQueue.Priority.PHASE, schedule.priority(i), cue.line());
            }
            if (cue.speaker().equals("The Choir")) {
                assertEquals(6.0, cue.musicDuckDb(), 1e-9, "the Choir's line ducks the music 6 dB");
            }
            if (!cue.speaker().equals("Vorne")) {
                continue;
            }
            vorne++;
            assertEquals(Channel.ENEMY, cue.channel().orElse(Channel.FRIENDLY), cue.line());
            String expression = cue.expression().orElse(Expression.NEUTRAL).slug();
            String portrait = cue.portrait().orElse("vorne");
            assertTrue(
                    Files.isRegularFile(
                            ASSETS.resolve("sprites/portraits/radio-" + portrait + "-" + expression + ".png")),
                    portrait + "'s radio portrait " + expression);
            assertTrue(voiceSeconds(cue.speaker(), cue.line(), expression) > 0, "voiced: " + cue.line());
            if (cue.shout().orElse(false)) {
                shouts++;
                assertEquals("eruption", cue.phase().orElseThrow(), "the shout at the eruption");
                assertEquals("The Choir", data.radio().get(i - 1).speaker(), "right after the Choir's line");
            }
        }
        assertEquals(5, vorne, "Vorne's five radio lines (the boss document's 1-3, 5 and 6; line 4 the Choir's)");
        assertEquals(1, shouts, "his one shout");
    }

    /**
     * Round 80 (the fight's capture: Vorne's arrival line, cut by Rook's eject bark while its last page still held, was
     * spoken again in full after the bark): no line of the level is heard twice. On the typical medium fight and the
     * slow hard one, on every difficulty, voiced or not, with each fit: the run uncut, then once for every line that
     * played with an urgent line (the low-armour warning, as the eject bark) cutting in 0.5 s before that line would
     * have left the radio. A line cut before it was heard in full (its voice still speaking) plays again once; one
     * already heard does not.
     */
    @Test
    void noLineIsHeardTwiceEvenWhenAnUrgentLineCutsIt() {
        List<List<Event>> runs = RUNS.get(level);
        int cuts = 0;
        for (int r : new int[] {1, 3}) {
            for (Difficulty difficulty : Difficulty.values()) {
                LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
                for (boolean voiced : new boolean[] {false, true}) {
                    for (int fitted : new int[] {0, ESCORT}) {
                        String run = "run " + r + " " + difficulty + (voiced ? " voiced" : "") + " fit " + fitted;
                        List<Played> uncut = play(
                                script, runs.get(r), fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
                        assertHeardOnce(uncut, run);
                        for (Played line : uncut) {
                            double at = line.left() - 0.5;
                            if (Double.isNaN(at) || at <= 0) {
                                continue;
                            }
                            double[] opened = {Double.NaN};
                            List<Played> cut =
                                    play(script, runs.get(r), fitted, voiced, at, opened, SIDES.getFirst(), 6);
                            String where = run + ", cut at " + at + " in \"" + line.text() + "\"";
                            assertTrue(!Double.isNaN(opened[0]), where + ": the urgent line opens");
                            assertHeardOnce(cut, where);
                            cuts++;
                        }
                    }
                }
            }
        }
        assertTrue(cuts > 100, "the lines cut: " + cuts);
    }

    private static void assertHeardOnce(List<Played> played, String run) {
        for (Played line : played) {
            assertTrue(line.heard() <= 1, run + ": heard " + line.heard() + " times: " + line.text());
        }
    }
}
