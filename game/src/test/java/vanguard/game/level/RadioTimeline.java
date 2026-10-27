package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.play;
import static vanguard.game.level.RadioPlayer.readVoiceSeconds;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.content.voice.VoiceLines;
import vanguard.game.level.RadioPlayer.EndRun;
import vanguard.game.settings.GameplaySettings;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;
import vanguard.sim.RadioCue;
import vanguard.sim.SimStep;

/**
 * The timed radio lines of Levels 01–15 play when the level scripts mean them to: the real queue,
 * stepped at the simulation's rate at the default text speed, with the event lines a player can
 * set off in between (an escaped Spore Bomber before the Leviathan's first pass, the lifeboat
 * secret, a convoy's first hit and loss, the Brood Carrier's phases, Rook's first kill, …), starts none of them more
 * than a second after its time. Event lines wait for a gap and may be dropped as stale; they never
 * push a timed line back. Every run is played with each fit a cue can require (a special, a homing
 * weapon, both, neither): a cue the fit does not allow is never queued, as in the game. Okafor's
 * low-armour line (design/player/armor), which the game queues as urgent, plays at once wherever it
 * falls and every timed line still plays after it. A line with Rook's side ({@code {side}}, Level
 * 08) is played with each side's text. M5 part C: a level with hold zones (Level 09) is flown through
 * them: the level clock (script time) slows to the hold's speed over its ramp while the queue runs
 * on real time, each hold lasting a typical 6 s and the medium window of about 10.6 s, the collapse
 * starting as the last cluster dies; a line is late by the real seconds after the level clock
 * reached its time, and a line that requires the escort plays only with him flying (both fits).
 * M5 part D: Level 10's scripted loss sets off its {@code scripted-loss} line at its time in every
 * run; its other events (the first decloak and loop-back, a shuttle hit or lost, the ferry cache)
 * where the autopilot's runs see them. M5 part H, step H9: a met secondary objective's line is set
 * off where a run lists it (Level 12's sondes); a closing line queued on a free radio goes ahead of
 * the timed lines (Level 14's root-grid line, its own test). M7 part D: a repeating event line (each
 * unit's first hit, {@code ally-hit}, and each loss, {@code ally-lost}) starts again for every event of a
 * run, its {@code {ally}} read as the event's name (Level 17's domes: "Lowell is hit!"), as the game does.
 *
 * <p>One subclass per level ({@code RadioTimelineLevelNNTest}, step BT2c): Gradle's test forks spread
 * test classes, not methods, and the levels' runs together took about 230 s in one class. Each subclass
 * runs these checks for its level's {@link #RUNS} and holds that level's own tests.
 *
 * <p>RF6: the runs live in {@link RadioRuns} (one method per act), the queue's player in {@link RadioPlayer}.
 */
abstract class RadioTimeline {
    static final Content CONTENT = ContentLoader.fromClasspath();
    static final double MAX_LATE_SECONDS = 1;
    static final Map<String, VoiceLines.VoiceLine> VOICES = VoiceLines.radioIndex(CONTENT);
    static final java.nio.file.Path ASSETS =
            java.nio.file.Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

    static final String LEVEL_01 = "act-1-first-contact/level-01-break-at-dawn";
    static final String LEVEL_02 = "act-1-first-contact/level-02-shipyard-burning";
    static final String LEVEL_03 = "act-1-first-contact/level-03-spore-drift";
    static final String LEVEL_04 = "act-1-first-contact/level-04-tranquility-run";
    static final String LEVEL_05 = "act-1-first-contact/level-05-crater-nest";
    static final String LEVEL_06 = "act-1-first-contact/level-06-farside";
    static final String LEVEL_07 = "act-1-first-contact/level-07-brood-carrier";
    static final String LEVEL_08 = "act-2-homefront/level-08-neon-skyline";
    static final String LEVEL_09 = "act-2-homefront/level-09-arcology-fall";
    static final String LEVEL_10 = "act-2-homefront/level-10-evacuation-corridor";
    static final String LEVEL_11 = "act-2-homefront/level-11-atlantic-convoy";
    static final String LEVEL_12 = "act-2-homefront/level-12-storm-front";
    static final String LEVEL_13 = "act-2-homefront/level-13-polar-relay";
    static final String LEVEL_14 = "act-2-homefront/level-14-siege-spire";
    static final String LEVEL_15 = "act-3-red-dust/level-15-olympus-descent";
    static final String LEVEL_16 = "act-3-red-dust/level-16-valles-canyon-run";
    static final String LEVEL_17 = "act-3-red-dust/level-17-dome-siege";
    static final String LEVEL_18 = "act-3-red-dust/level-18-dust-storm";
    static final String LEVEL_19 = "act-3-red-dust/level-19-foundry-of-tharsis";
    static final String LEVEL_20 = "act-3-red-dust/level-20-retreat-from-hellas";
    static final String LEVEL_21 = "act-3-red-dust/level-21-dust-colossus";
    static final String LEVEL_22 = "act-4-deep-water/level-22-ice-shelf";
    static final String LEVEL_23 = "act-4-deep-water/level-23-through-the-ice";
    static final String LEVEL_24 = "act-4-deep-water/level-24-abyssal-plain";
    static final String LEVEL_25 = "act-4-deep-water/level-25-siren-trench";
    static final String LEVEL_26 = "act-4-deep-water/level-26-blackwater";
    static final String LEVEL_27 = "act-4-deep-water/level-27-hydrothermal-vents";
    static final String LEVEL_28 = "act-4-deep-water/level-28-abyssal-maw";

    /** Rook's sides: a {@code {side}} line is played with each one's text. */
    static final List<String> SIDES = VoiceLines.SIDES;

    /**
     * Levels whose lines are not rendered yet (none: Level 21's were rendered in M7 part H's step VOX-H, Level 20's
     * in M7 part G's step VOX-G,
     * Level 14's in M5 part H's step H6,
     * Level 13's in M5 part G's step G6,
     * Level 12's in M5 part F's voice step, step 6, Level 11's in part E's, Level 10's in part D's,
     * Level 09's in part C's, Level 08's in M5 part B's step B9, Level 07's in M4 part G):
     * their voiced runs play the lines as text and list the late ones, without asking for the files.
     * An uncast speaker's line (marked {@code uncast} in the speaker table: none now, Level 13's Nansen
     * was until concept round 35 cast him) plays as text in any level.
     */
    static final Set<String> UNVOICED = Set.of();

    static final int ESCORT = RadioCue.FITTED_ESCORT;

    /** The fits a cue can require: neither, a special, a homing weapon, both; each with and without the escort flying. */
    static final int[] FITS = {
        0,
        RadioCue.FITTED_SPECIAL,
        RadioCue.FITTED_HOMING,
        RadioCue.FITTED_SPECIAL | RadioCue.FITTED_HOMING,
        ESCORT,
        ESCORT | RadioCue.FITTED_SPECIAL,
        ESCORT | RadioCue.FITTED_HOMING,
        ESCORT | RadioCue.FITTED_SPECIAL | RadioCue.FITTED_HOMING
    };

    /**
     * How long each hold zone lasts in real seconds in a level with holds (Level 09): a typical hold
     * (the README's 6 s) and the whole medium window (≈ 10.6 s at 30 px/s); NaN for a level without.
     */
    static final double[] HOLD_SECONDS = {6, 10.6};

    /**
     * Timed lines written to follow the line before them rather than to start at their time, by
     * level and t: Level 01's Varga answers the Choir's song ("That isn't noise…"), so she starts
     * once the Choir's line has closed.
     */
    static final Map<String, Set<Double>> FOLLOWING = Map.of(LEVEL_01, Set.of(161.0));

    /**
     * Closing lines whose voice may run this many seconds past the debrief's takeover: none since step RX
     * (2026-10-19), as the outro waits for the closing lines to be heard ({@link Outro}). Level 06's Rook
     * line, which answers Okafor's level-end line, had 0.35 s here while the outro ended at 15 s sharp.
     */
    static final Map<String, Double> OUTRO_ALLOWANCE = Map.of();

    /** Okafor's low-armour line, queued urgent by the game at the first armour hit to 15 % or below. */
    static final LevelData.RadioLine LOW_ARMOUR = CONTENT.armour().radio();

    static final String LOW_ARMOUR_EXPRESSION =
            LOW_ARMOUR.expression().orElse(Expression.NEUTRAL).slug();

    /**
     * An event a player sets off at script time {@code t}, or (M5 part C) {@code into} real seconds
     * after hold zone {@code hold} started (a node killed in its hold); M7 part D: {@code ally} is the
     * name a repeating event's line reads for {@code {ally}} (a dome's, Level 17), "" for the number word.
     */
    record Event(double t, CueTrigger trigger, String subject, int hold, double into, String ally) {
        Event(double t, CueTrigger trigger, String subject) {
            this(t, trigger, subject, -1, 0, "");
        }

        Event(double t, CueTrigger trigger, String subject, int hold, double into) {
            this(t, trigger, subject, hold, into, "");
        }

        /** M7 part D: a ground ally's event at {@code t} (its first hit, its loss), its line naming {@code ally}. */
        static Event named(double t, CueTrigger trigger, String ally) {
            return new Event(t, trigger, "", -1, 0, ally);
        }

        static Event inHold(int hold, double into, CueTrigger trigger, String subject) {
            return new Event(Double.NaN, trigger, subject, hold, into);
        }

        /**
         * M8 part H (step H6): an event {@code into} real seconds after the level clock halted at the arena's end
         * (Level 28's Maw: its phases, its dive, its kill); a run with one flies the halt ({@link #ARENA}).
         */
        static Event inArena(double into, CueTrigger trigger, String subject) {
            return new Event(Double.NaN, trigger, subject, ARENA, into);
        }
    }

    /**
     * M8 part H (step H6): the hold index of an event in the halted arena ({@link Event#inArena}). A run with one flies
     * the halt as the simulation does: the level clock stops a step short of the arena section's end while the boss
     * lives, its kill ({@code BOSS_DESTROYED} in the arena) holds it for the boss's death chain and the section's {@code
     * hold_after_boss}, then it runs on; the kill withdraws the phase lines still waiting, as the game does.
     */
    static final int ARENA = -2;

    /**
     * A cue and when its message opened, if it did (real seconds), and when it was due (real seconds:
     * when the level clock reached its time, for a timed cue), with its {@code text} as shown (M7 part D:
     * a repeating event cue is played once per event, each with its ally's name).
     */
    record Played(RadioCue cue, Optional<Double> opened, double due, String text) {
        /** How late a timed line opened after the level clock reached its time, real seconds. */
        double late() {
            return opened.orElseThrow() - due;
        }
    }

    /** Per level, the runs to check: each a list of events on top of the timed lines and the level end. */
    static final Map<String, List<List<Event>>> RUNS = RadioRuns.all();

    /** The real seconds each hold lasts to fly a level with: {@link #HOLD_SECONDS}, or NaN for a level without holds. */
    static double[] holdSeconds(LevelScript script) {
        return script.objectives().holds().isEmpty() ? new double[] {Double.NaN} : HOLD_SECONDS;
    }

    /** The level this class checks, one of {@link #RUNS}' keys. */
    final String level;

    RadioTimeline(String level) {
        this.level = level;
    }

    /** This level's runs. */
    private List<List<Event>> levelRuns() {
        List<List<Event>> runs = RUNS.get(level);
        assertTrue(runs != null, level + " has runs");
        return runs;
    }

    @Test
    void theTimedLinesStartAtMostASecondLate() {
        List<List<Event>> runs = levelRuns();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : runs) {
                for (int fitted : FITS) {
                    for (String side : SIDES) {
                        for (double hold : holdSeconds(script)) {
                            String where = level + " on " + difficulty + " with " + events + " (fit " + fitted + ", "
                                    + side + ", holds " + hold + " s): ";
                            List<Played> played =
                                    play(script, events, fitted, false, Double.NaN, new double[1], side, hold);
                            for (Played line : played) {
                                RadioCue cue = line.cue();
                                if (cue.trigger() != CueTrigger.TIME || !cue.allowedWith(fitted)) {
                                    continue;
                                }
                                assertTrue(
                                        line.opened().isPresent(), where + "the timed line at t=" + cue.t() + " plays");
                                if (FOLLOWING.getOrDefault(level, Set.of()).contains(cue.t())) {
                                    continue;
                                }
                                double late = line.late();
                                assertTrue(
                                        late <= MAX_LATE_SECONDS,
                                        String.format(
                                                "%s%s's line at t=%s starts %.1f s late: %s",
                                                where, cue.speaker(), cue.t(), late, cue.line()));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The same runs with the voices (design/audio/voice): a line holds the radio for the longer of
     * its text and its voice. Every timed line still plays; the ones that start more than a second
     * late are printed for the user to decide on a retiming (the data is not retimed here).
     */
    @Test
    void theVoicedTimelinePlaysEveryTimedLineAndListsTheLateOnes() {
        Map<String, Double> worst = new java.util.TreeMap<>();
        List<List<Event>> runs = levelRuns();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            for (List<Event> events : runs) {
                for (int fitted : FITS) {
                    for (String side : SIDES) {
                        for (double hold : holdSeconds(script)) {
                            for (Played line :
                                    play(script, events, fitted, true, Double.NaN, new double[1], side, hold)) {
                                RadioCue cue = line.cue();
                                if (cue.trigger() != CueTrigger.TIME || !cue.allowedWith(fitted)) {
                                    continue;
                                }
                                assertTrue(
                                        line.opened().isPresent(),
                                        level + ": the timed line at t=" + cue.t() + " plays");
                                assertTrue(
                                        VoiceLines.spoken(cue.line()).isEmpty()
                                                || voiceSeconds(cue, side) > 0
                                                || UNVOICED.contains(level)
                                                || CONTENT.voices().uncast(cue.speaker()),
                                        level + ": the timed line at t=" + cue.t() + " has its voice");
                                if (FOLLOWING.getOrDefault(level, Set.of()).contains(cue.t())) {
                                    continue;
                                }
                                double late = line.late();
                                if (late > MAX_LATE_SECONDS) {
                                    worst.merge(
                                            String.format("%s t=%s %s (%s)", level, cue.t(), cue.speaker(), difficulty),
                                            late,
                                            Math::max);
                                }
                            }
                        }
                    }
                }
            }
        }
        worst.forEach((line, late) -> System.out.printf("voiced line late by %.1f s: %s%n", late, line));
    }

    /**
     * When Rook's bark is queued before the level's end in a run with him flying, seconds before it (NaN:
     * none): a bark that opens as the level ends holds the radio while the closing lines wait (the user's
     * playtest of 2026-10-19, Level 15: "the last message is cut short"). Each run with him is played
     * without one and with one at each of these times.
     */
    static final double[] BARKS_BEFORE_END = {Double.NaN, 0.5, 3};

    static final double[] NO_BARK = {Double.NaN};

    /**
     * The bark played before the end ({@link #BARKS_BEFORE_END}): of Rook's event barks (the eject bark is urgent and ends his
     * flight) the one longest on the radio, its text time or its voice.
     */
    static Barks.Line longestBarkLine() {
        return LongestBark.LINE;
    }

    /** Read on first use: the voices' lengths ({@link #VOICE_SECONDS}) are set up after the fields above. */
    private static final class LongestBark {
        static final Barks.Line LINE = longestBark();
    }

    private static Barks.Line longestBark() {
        Barks.Line longest = null;
        float most = -1;
        for (vanguard.content.WingmenData.Bark bark : CONTENT.wingmen().barks().triggers()) {
            for (String text : bark.lines()) {
                Barks.Line line = new Barks.Line(
                        Barks.Trigger.KILL_STREAK,
                        CONTENT.wingmen().barks().speaker(),
                        bark.expression().slug(),
                        bark.shouted(),
                        text);
                if (bark.trigger().equals("rook-ejects")) {
                    continue;
                }
                float seconds = barkMessage(line).seconds(GameplaySettings.DEFAULT_TEXT_SPEED);
                if (seconds > most) {
                    most = seconds;
                    longest = line;
                }
            }
        }
        return longest;
    }

    /** {@code line} as the radio shows it, with its voice. */
    static RadioQueue.Message barkMessage(Barks.Line line) {
        return new RadioQueue.Message(
                line.speaker(),
                line.speaker(),
                line.expression(),
                RadioQueue.wrap(line.text()),
                false,
                Optional.empty(),
                voiceSeconds(line.speaker(), line.text(), line.expression()));
    }

    /** Per level, its {@link #endRuns()}: both outro checks read them, so the runs are played once per fork. */
    static final Map<String, List<EndRun>> END_RUNS = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Every voiced run of this level to its end: each difficulty, run of events, fit, side, hold length and
     * count of convoy units home (the level-end lines for each count start, each count in turn). A run whose
     * events come after the level's end is left out (one at the end itself, a secondary judged then, stays) (the arena's events after a halted clock: Level 11's
     * Kraken dies 15 s before its end, which the test's running clock does not model).
     */
    List<EndRun> endRuns() {
        return END_RUNS.computeIfAbsent(level, key -> RadioPlayer.endRuns(key, levelRuns()));
    }

    /** Where a line is, for the messages and {@link #OUTRO_ALLOWANCE}: the level, the speaker and the line. */
    static String where(String level, RadioCue cue) {
        return String.format("%s, %s's line \"%s\"", level, cue.speaker(), cue.line());
    }

    /**
     * The outro ends {@link LevelData#OUTRO_SECONDS} after the level's end, or once the closing lines
     * have been heard if that is later ({@link Outro}), when the debrief takes over and cuts the voice on
     * the radio: in every run with the voices, every level-end line opens, and every line that opens (the
     * level-end line, a closing line still in flight) has finished speaking by then. (Level 12's level-end
     * line opened at 218.1 with Rook flying and the sondes met late in the eye, and its last 4.7 s played
     * over the debrief: playtest 2026-10-18. Level 15's, with a bark of Rook's on the radio as the level
     * ended: playtest 2026-10-19.) The outro runs at most {@link #MAX_OUTRO_OVERRUN} past its 15 s, so the
     * backdrop the level holds until then (checked to {@link LevelData#outroEnd}) is not left far behind.
     */
    @Test
    void closingLineVoicesEndWithinTheOutro() {
        Map<String, Double> cut = new java.util.TreeMap<>();
        Map<String, String> example = new java.util.HashMap<>();
        double overrun = 0;
        String overrunRun = "";
        for (EndRun run : endRuns()) {
            assertTrue(!Double.isNaN(run.handover()), level + ": the debrief takes over within a minute (" + run + ")");
            double handOver = run.handover();
            if (handOver - run.ended() - LevelData.OUTRO_SECONDS > overrun) {
                overrun = handOver - run.ended() - LevelData.OUTRO_SECONDS;
                overrunRun = run.toString();
            }
            for (Played line : run.played()) {
                RadioCue cue = line.cue();
                if (!run.allows(cue)) {
                    continue;
                }
                String where = where(level, cue);
                if (cue.trigger() == CueTrigger.LEVEL_END) {
                    assertTrue(line.opened().isPresent(), where + " plays (" + run + ")");
                }
                if (line.opened().isEmpty()) {
                    continue;
                }
                double over =
                        line.opened().get() + voiceSeconds(cue.speaker(), line.text(), cue.expression()) - handOver;
                if (over > OUTRO_ALLOWANCE.getOrDefault(where, 0.0) && over > cut.getOrDefault(where, 0.0)) {
                    cut.put(where, over);
                    example.put(where, run.toString());
                }
            }
        }
        StringBuilder lines = new StringBuilder();
        cut.forEach((line, over) -> lines.append(
                String.format("%n%s: its voice runs %.1f s past the outro (e.g. %s)", line, over, example.get(line))));
        assertTrue(cut.isEmpty(), "voices cut by the debrief:" + lines);
        if (overrun > 0) {
            System.out.printf(
                    "%s: the outro runs %.1f s past its %.0f s (e.g. %s)%n",
                    level, overrun, LevelData.OUTRO_SECONDS, overrunRun);
        }
        assertTrue(
                overrun <= MAX_OUTRO_OVERRUN,
                String.format(
                        "%s: the outro runs %.1f s past its %.0f s (e.g. %s)",
                        level, overrun, LevelData.OUTRO_SECONDS, overrunRun));
    }

    /**
     * How far past {@link LevelData#OUTRO_SECONDS} the outro may run to let the closing lines be heard (step
     * RX: at most 2.1 s, Level 15 with a bark of Rook's as it ends and its secondary judged then). The
     * backdrops hold 3 s past {@link LevelData#outroEnd} except Levels 01 and 02 (an edge of the earth-limb or
     * the Vrell glow), whose outros never run over.
     */
    static final double MAX_OUTRO_OVERRUN = 3;

    /**
     * Levels whose radio lines do not all end in time today (the user's playtest of 2026-10-19: "On level 15
     * or 16, the last message is cut short ... Can't you add a test that prevents this from happening on any
     * level?"): {@link #everyRadioLineEndsBeforeTheLevelDoes} prints their late lines instead of failing,
     * until the content step FX-C retimes their data (briefs/fxc-from-fxg.md lists each line, its end, the
     * level's end and the seconds needed) and takes the level out of this set. Pending the user's review. None left: step FX-C lengthened Level 10's last section to t=201 and Level 13's to t=239.
     */
    static final Set<String> LINES_END_PENDING = Set.of();

    /** How far past its limit a line may end: one step, the clocks' grain. */
    static final double LINE_END_SLACK = SimStep.SECONDS;

    /**
     * How long a line speaks once it opens: its rendered voice take (assets/voice), or for a line without a
     * take the text-time rule, the time the radio shows it at the default text speed (typed out page by
     * page, each page held, the last one longest).
     */
    static double speaks(RadioCue cue, String text) {
        float voice = voiceSeconds(cue.speaker(), text, cue.expression());
        if (voice > 0) {
            return voice;
        }
        return new RadioQueue.Message(
                        cue.speaker(),
                        cue.portrait(),
                        cue.expression(),
                        RadioQueue.wrap(text),
                        cue.distorted(),
                        Optional.empty(),
                        0)
                .seconds(GameplaySettings.DEFAULT_TEXT_SPEED);
    }

    /** A radio line that ends too late: when it ends and its limit (real seconds), with the run that shows it. */
    record LateLine(String where, boolean timed, double ends, double limit, double levelEnd, String run) {
        double needed() {
            return ends - limit;
        }
    }

    /**
     * The user's rule (playtest 2026-10-19: the last radio message was cut short; "the level should be
     * extended to play the entire message in full"): in every voiced run every radio line that opens
     * finishes speaking ({@link #speaks}: its voice take, or its text time without one) before the level
     * is over. A timed line belongs to the flight: it ends before the level clock reaches the level's end
     * (when the flight ends, {@code LEVEL_COMPLETE}, and the outro starts). The other lines (the level-end
     * lines, a met secondary's, a boss's death, an event line in the last seconds) may run on into the
     * outro, but end before the debrief takes over ({@link Outro}: the radio idle, or {@link
     * LevelData#OUTRO_SECONDS} after the level's end once no closing line is left to be heard; the
     * debrief cuts a voice still speaking), within {@link #OUTRO_ALLOWANCE}. Step RX: a run with Rook
     * flying is also played with his longest bark just before the end ({@link #BARKS_BEFORE_END}). Every level with data has a subclass of this class (see
     * {@link RadioTimelineLevelsTest}), so this holds for each level.
     */
    @Test
    void everyRadioLineEndsBeforeTheLevelDoes() {
        Map<String, LateLine> late = new java.util.TreeMap<>();
        for (EndRun run : endRuns()) {
            for (Played line : run.played()) {
                RadioCue cue = line.cue();
                if (!run.allows(cue) || line.opened().isEmpty()) {
                    continue;
                }
                String where = where(level, cue);
                boolean timed = cue.trigger() == CueTrigger.TIME;
                double limit = timed ? run.ended() : run.handover() + OUTRO_ALLOWANCE.getOrDefault(where, 0.0);
                double ends = line.opened().get() + speaks(cue, line.text());
                LateLine before = late.get(where);
                if (ends - limit > LINE_END_SLACK && (before == null || ends - limit > before.needed())) {
                    late.put(where, new LateLine(where, timed, ends, limit, run.ended(), run.toString()));
                }
            }
        }
        StringBuilder lines = new StringBuilder();
        for (LateLine line : late.values()) {
            lines.append(String.format(
                    "%n%s (%s line) ends at %.1f s, the %s at %.1f s (level end %.1f s): %.1f s more needed (e.g. %s)",
                    line.where(),
                    line.timed() ? "timed" : "outro",
                    line.ends(),
                    line.timed() ? "level's end" : "debrief",
                    line.limit(),
                    line.levelEnd(),
                    line.needed(),
                    line.run()));
        }
        if (LINES_END_PENDING.contains(level)) {
            System.out.println("pending (LINES_END_PENDING): radio lines that end too late:" + lines);
            return;
        }
        assertTrue(late.isEmpty(), "radio lines that end after the level does:" + lines);
    }

    /**
     * The low-armour line, every 10 s of each level with each fit and its voice: it opens in the step
     * it is queued (urgent: it interrupts), and every timed line still plays; the timed lines it
     * pushes back by more than a second are printed (an urgent line may delay them, by design).
     */
    @Test
    void theLowArmourLineInterruptsAtOnceAndEveryTimedLineStillPlays() {
        assertTrue(
                voiceSeconds(LOW_ARMOUR.speaker(), LOW_ARMOUR.line(), LOW_ARMOUR_EXPRESSION) > 0,
                "the low-armour line has its voice");
        Map<String, Double> worst = new java.util.TreeMap<>();
        LevelScript script = SimSpecs.level(CONTENT, level, Difficulty.MEDIUM);
        for (double at = 5; at < script.seconds(); at += 10) {
            for (int fitted : FITS) {
                double[] opened = {Double.NaN};
                List<Played> played = play(script, List.of(), fitted, true, at, opened);
                String where = level + " with the line at t=" + at + " (fit " + fitted + "): ";
                assertTrue(
                        opened[0] - at <= 2 * SimStep.SECONDS, where + "the line opens at once, not at " + opened[0]);
                for (Played line : played) {
                    RadioCue cue = line.cue();
                    if (cue.trigger() != CueTrigger.TIME || !cue.allowedWith(fitted)) {
                        continue;
                    }
                    assertTrue(line.opened().isPresent(), where + "the timed line at t=" + cue.t() + " plays");
                    double late = line.late();
                    if (late > MAX_LATE_SECONDS
                            && !FOLLOWING.getOrDefault(level, Set.of()).contains(cue.t())) {
                        worst.merge(level + " t=" + cue.t() + " " + cue.speaker(), late, Math::max);
                    }
                }
            }
        }
        worst.forEach(
                (line, late) -> System.out.printf("pushed back by the low-armour line: %.1f s: %s%n", late, line));
    }

    /** The voice length of a radio line from its rendered file with Rook on {@code side}, 0 without one. */
    static float voiceSeconds(RadioCue cue, String side) {
        return voiceSeconds(cue.speaker(), text(cue, side), cue.expression());
    }

    /** A cue's line as shown: the third convoy unit's number word, Rook's {@code side}. */
    static String text(RadioCue cue, String side) {
        return VoiceLines.sideLine(VoiceLines.allyLine(cue.line(), 2), side);
    }

    /** M7 part D: a cue's line as shown with {@code ally} for {@code {ally}} (a dome's name); "" as {@link #text}. */
    static String text(RadioCue cue, String side, String ally) {
        return ally.isEmpty() ? text(cue, side) : VoiceLines.sideLine(cue.line().replace(VoiceLines.ALLY, ally), side);
    }

    /**
     * The voice length of {@code speaker}'s {@code text} in {@code expression} from its rendered file
     * (a stage direction's: the speaker's stage sound, the Choir's sung sting), 0 without one.
     */
    static float voiceSeconds(String speaker, String text, String expression) {
        // Read each file once per fork (step BT2c): the runs ask for the same lines thousands of times.
        return VOICE_SECONDS.computeIfAbsent(
                speaker + '\0' + text + '\0' + expression, key -> readVoiceSeconds(speaker, text, expression));
    }

    /** {@link #voiceSeconds(String, String, String)} by speaker, text and expression. */
    static final Map<String, Float> VOICE_SECONDS = new java.util.concurrent.ConcurrentHashMap<>();

    static boolean opened(List<Played> played, String subject) {
        return played.stream()
                .anyMatch(line ->
                        line.cue().subject().equals(subject) && line.opened().isPresent());
    }
}
