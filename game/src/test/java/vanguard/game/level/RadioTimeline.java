package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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
import vanguard.game.audio.VorbisFile;
import vanguard.game.settings.GameplaySettings;
import vanguard.sim.CueTrigger;
import vanguard.sim.GroundUnit;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.RadioCue;
import vanguard.sim.Section;
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
    }

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
    static final Map<String, List<List<Event>>> RUNS = runs();

    static Map<String, List<List<Event>>> runs() {
        Map<String, List<List<Event>>> runs = new java.util.LinkedHashMap<>(runs01To10());
        // M5 part E: Level 11's events as a flight sees them: a cargo ship's first hit early (the
        // popcorn over the muster) or next to Atlas Control's and Varga's lines, a ship lost, the
        // sunken pod's cache as the reef passes, then the arena's lines after the clock's halt
        // (every cue before 161 is timed)
        runs.put(
                LEVEL_11,
                List.of(
                        List.of(),
                        List.of(new Event(18, CueTrigger.FIRST_ALLY_HIT, "")),
                        List.of(
                                new Event(62, CueTrigger.FIRST_ALLY_HIT, ""),
                                new Event(95, CueTrigger.SECRET, "sunken pod")),
                        List.of(
                                new Event(21.5, CueTrigger.FIRST_ALLY_HIT, ""),
                                new Event(110.5, CueTrigger.ALLY_LOST, "")),
                        List.of(
                                new Event(140, CueTrigger.ALLY_LOST, ""),
                                new Event(147, CueTrigger.FIRST_ALLY_HIT, ""),
                                new Event(160, CueTrigger.FIRST_TELEGRAPH, "")),
                        List.of(
                                new Event(162, CueTrigger.FIRST_TELEGRAPH, ""),
                                new Event(170, CueTrigger.BOSS_PART_DESTROYED, "left arm"),
                                new Event(175, CueTrigger.ALLY_LOST, ""),
                                new Event(190, CueTrigger.BOSS_DESTROYED, "harbour-kraken")),
                        // Step FXL11: the first telegraph on a ship's lane sets off both telegraph cues in one
                        // step, the ship hit just after (round 44's capture)
                        List.of(
                                new Event(162, CueTrigger.FIRST_TELEGRAPH, ""),
                                new Event(162, CueTrigger.FIRST_SHIP_TELEGRAPH, ""),
                                Event.named(164, CueTrigger.ALLY_HIT, "Saint-Laurent"),
                                new Event(195, CueTrigger.BOSS_DESTROYED, "harbour-kraken"))));
        // M5 part F: Level 12's events as a flight sees them: the first latch at the introduction (just
        // after Varga's warning) or at a later swarm, the canister's cache in the lightning field, the
        // sondes' secondary as the third one reaches the ship in the eye (every cue is timed or an event)
        runs.put(
                LEVEL_12,
                List.of(
                        List.of(),
                        List.of(
                                new Event(33.5, CueTrigger.FIRST_LATCH, ""),
                                new Event(101, CueTrigger.SECRET, "drifting canister")),
                        List.of(
                                new Event(36, CueTrigger.FIRST_LATCH, ""),
                                new Event(104, CueTrigger.SECRET, "drifting canister"),
                                new Event(172.5, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(59, CueTrigger.FIRST_LATCH, ""),
                                new Event(171, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(89, CueTrigger.FIRST_LATCH, ""),
                                new Event(103.5, CueTrigger.SECRET, "drifting canister"),
                                new Event(174, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        // M5 part G: Level 13's events as the guarding autopilot's runs see them: the hut's cache early in
        // the approach, the relay below half late in the whiteout (medium) or in the walkers' phase and
        // below a quarter in the whiteout (hard), the secondary at the synchronisation's end with Nansen's
        // t=218 line (every other cue is timed)
        runs.put(
                LEVEL_13,
                List.of(
                        List.of(),
                        List.of(
                                new Event(14, CueTrigger.SECRET, "research hut cache"),
                                new Event(218, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(15.5, CueTrigger.SECRET, "research hut cache"),
                                new Event(201, CueTrigger.ALLY_BELOW, "0.5")),
                        List.of(
                                new Event(13, CueTrigger.SECRET, "research hut cache"),
                                new Event(131, CueTrigger.ALLY_BELOW, "0.5"),
                                new Event(198, CueTrigger.ALLY_BELOW, "0.25")),
                        List.of(
                                new Event(108, CueTrigger.ALLY_BELOW, "0.5"),
                                new Event(161.5, CueTrigger.ALLY_BELOW, "0.25"))));
        // M5 part H: Level 14's events: the armoured car's cache as it passes under Rook's and Hammer
        // Lead's lines (early, late, just after Rook's), then the arena's lines after the halt at t=108 (every
        // cue before it is timed; the level clock runs on here, as the test's clock does): the Spire's phases
        // and its death in a fast, a medium (about 135 s) and a slow fight
        runs.put(
                LEVEL_14,
                List.of(
                        List.of(),
                        List.of(
                                new Event(14.5, CueTrigger.SECRET, "armoured car"),
                                new Event(135, CueTrigger.BOSS_PHASE, "Maw"),
                                new Event(165, CueTrigger.BOSS_PHASE, "Free spire"),
                                new Event(190, CueTrigger.BOSS_DESTROYED, "siege-spire")),
                        List.of(
                                new Event(18, CueTrigger.SECRET, "armoured car"),
                                new Event(150, CueTrigger.BOSS_PHASE, "Maw"),
                                new Event(195, CueTrigger.BOSS_PHASE, "Free spire"),
                                new Event(243, CueTrigger.BOSS_DESTROYED, "siege-spire")),
                        List.of(
                                new Event(9, CueTrigger.SECRET, "armoured car"),
                                new Event(160, CueTrigger.BOSS_PHASE, "Maw"),
                                new Event(215, CueTrigger.BOSS_PHASE, "Free spire"),
                                new Event(249, CueTrigger.BOSS_DESTROYED, "siege-spire"))));
        // M7 part B: Level 15's events: the first Burrower surfacing in the first field (t=68) or later, the
        // drop pod's rubble at the cliff's foot early or late, the kill-ratio secondary judged at the level's end
        // (t=190, with the level-end line, as the game queues it: step RX; it was t=185 before, which left the
        // radio free for the level-end line and hid the playtest's cut, 2026-10-19)
        runs.put(
                LEVEL_15,
                List.of(
                        List.of(),
                        List.of(
                                new Event(68.5, CueTrigger.FIRST_SURFACING, ""),
                                new Event(126, CueTrigger.SECRET, "drop pod"),
                                new Event(190, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(71, CueTrigger.FIRST_SURFACING, ""),
                                new Event(131, CueTrigger.SECRET, "drop pod")),
                        List.of(
                                new Event(81, CueTrigger.FIRST_SURFACING, ""),
                                new Event(108, CueTrigger.SECRET, "drop pod"),
                                new Event(190, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        // M7 part C: Level 16's events: the first wall touch at the mouth, under Ares Control's line, just before
        // the fork's line or in the rapids; the relay hut in the left channel (t≈71) early or late; the secondary
        // as the last barrier (gate 6, t=152-153.5) breaks, early or late (t=159), before the Choir's song (t=166)
        runs.put(
                LEVEL_16,
                List.of(
                        List.of(),
                        List.of(
                                new Event(9, CueTrigger.FIRST_WALL_CONTACT, ""),
                                new Event(72, CueTrigger.SECRET, "relay hut"),
                                new Event(155, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(23, CueTrigger.FIRST_WALL_CONTACT, ""),
                                new Event(74.5, CueTrigger.SECRET, "relay hut"),
                                new Event(158, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(59, CueTrigger.FIRST_WALL_CONTACT, ""),
                                new Event(70.5, CueTrigger.SECRET, "relay hut")),
                        List.of(
                                new Event(95, CueTrigger.FIRST_WALL_CONTACT, ""),
                                new Event(159, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        // M7 part D: Level 17's events: the domes' first hits and losses by name (Bradbury enters at t=38,
        // Lowell 46, Schiaparelli 104, Burroughs 110, Eden 126; the hooked bombers from t=36.5, the t=46.5
        // Shellback's lobs and roll on Lowell), the first Herald's first ring (round 40: Varga's line, t≈55.8), the
        // seed vault under its frame (t≈76), the untouched secondary as Eden passes (t≈135.8) in a run without a
        // hit, and a run that loses every dome
        runs.put(LEVEL_17, level17Runs());
        // M7 part E: Level 18's events: the rover wreck (x 60, t≈90) shot as it enters under Rook's t=86 line, just
        // before Varga's t=93 line or late as it passes; the kill-ratio secondary judged at the level's end (t=183)
        runs.put(
                LEVEL_18,
                List.of(
                        List.of(),
                        List.of(new Event(89.5, CueTrigger.SECRET, "rover wreck")),
                        List.of(
                                new Event(92, CueTrigger.SECRET, "rover wreck"),
                                new Event(183, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(96, CueTrigger.SECRET, "rover wreck"),
                                new Event(183, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(new Event(183, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        // M7 part F: Level 19's events: the payroll safe (x 60, t≈52) shot as it enters or a little later, then the
        // arena's lines after the halt at t=133 (as Level 11's: the test's clock runs on): the walker's first flame
        // tell,
        // a pod lost and its death in a fast, a medium and a slow fight, and the grey tanks' secondary at the end
        runs.put(
                LEVEL_19,
                List.of(
                        List.of(),
                        List.of(new Event(52.5, CueTrigger.SECRET, "payroll safe")),
                        List.of(
                                new Event(54, CueTrigger.SECRET, "payroll safe"),
                                new Event(136, CueTrigger.FIRST_TELEGRAPH, ""),
                                new Event(150, CueTrigger.BOSS_PART_DESTROYED, "left pod"),
                                new Event(190, CueTrigger.BOSS_DESTROYED, "revenant-walker")),
                        List.of(
                                new Event(140, CueTrigger.FIRST_TELEGRAPH, ""),
                                new Event(165, CueTrigger.BOSS_PART_DESTROYED, "right pod"),
                                new Event(172, CueTrigger.SECONDARY_OBJECTIVE, ""),
                                new Event(205, CueTrigger.BOSS_DESTROYED, "revenant-walker")),
                        List.of(
                                new Event(138, CueTrigger.FIRST_TELEGRAPH, ""),
                                new Event(170, CueTrigger.BOSS_PART_DESTROYED, "left pod"),
                                new Event(185, CueTrigger.BOSS_PART_DESTROYED, "right pod"),
                                new Event(215, CueTrigger.BOSS_DESTROYED, "revenant-walker"))));
        // M7 part G: Level 20's events: the emergency cache on its ledge (x 430, t≈92) shot as it enters or late as
        // it rises past, and the kill-ratio secondary judged at the level's end (t=172)
        runs.put(
                LEVEL_20,
                List.of(
                        List.of(),
                        List.of(new Event(93, CueTrigger.SECRET, "emergency cache")),
                        List.of(
                                new Event(96.5, CueTrigger.SECRET, "emergency cache"),
                                new Event(172, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(new Event(172, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        // M7 part H: Level 21's events: the survey crawler (x 80, t≈44) shot as it enters or late as it passes, then
        // the crawling arena's phase lines and the kill (the arena's clock runs to t=245, so the timed t=111 line plays
        // in it): a fast fight, a slow one, and a kill as the clock reaches the arena's end (the test's clock runs on
        // from a kill; the game's jumps to t=245, which the last run's kill at t=245 stands for)
        runs.put(
                LEVEL_21,
                List.of(
                        List.of(),
                        List.of(new Event(45, CueTrigger.SECRET, "survey crawler")),
                        List.of(
                                new Event(47.5, CueTrigger.SECRET, "survey crawler"),
                                new Event(150, CueTrigger.BOSS_PHASE, "Hunting"),
                                new Event(185, CueTrigger.BOSS_PHASE, "Breaching anywhere"),
                                new Event(205, CueTrigger.BOSS_DESTROYED, "dust-colossus")),
                        List.of(
                                new Event(170, CueTrigger.BOSS_PHASE, "Hunting"),
                                new Event(215, CueTrigger.BOSS_PHASE, "Breaching anywhere"),
                                new Event(245, CueTrigger.BOSS_DESTROYED, "dust-colossus"))));
        // M8 part B: Level 22's events at the autopilot's times with the plan's fit (step B3b, levelReport's runs: the
        // Airstrike on each rig in the lower half of the screen): the first geyser row's flare (t=12, the first row's
        // tell); no rig broken (a fit without the strike or an anti-ground gun); medium: the first core at t≈47, the
        // core-sample cache at t≈73, the secondary met with the third core at t≈79; hard: the first core at t≈47.5, the
        // secondary with the fourth at t≈95.5, the cache left; easy: the secondary with the second core at t≈63, the
        // cache at t≈71; a player who lets the first rig go: the first core with the second rig at t≈63.5, the cache
        // shot as it enters (t≈70.9)
        runs.put(
                LEVEL_22,
                List.of(
                        List.of(new Event(12, CueTrigger.FIRST_GEYSER, "")),
                        List.of(
                                new Event(12, CueTrigger.FIRST_GEYSER, ""),
                                new Event(47.1, CueTrigger.TARGET_DESTROYED, ""),
                                new Event(73, CueTrigger.SECRET, "core-sample cache"),
                                new Event(79.2, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(12, CueTrigger.FIRST_GEYSER, ""),
                                new Event(47.5, CueTrigger.TARGET_DESTROYED, ""),
                                new Event(95.5, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(12, CueTrigger.FIRST_GEYSER, ""),
                                new Event(47.2, CueTrigger.TARGET_DESTROYED, ""),
                                new Event(63.2, CueTrigger.SECONDARY_OBJECTIVE, ""),
                                new Event(71, CueTrigger.SECRET, "core-sample cache")),
                        List.of(
                                new Event(12, CueTrigger.FIRST_GEYSER, ""),
                                new Event(63.5, CueTrigger.TARGET_DESTROYED, ""),
                                new Event(70.9, CueTrigger.SECRET, "core-sample cache"))));
        // M8 part C: Level 23's events (step VOX-C's runs from the data's rows): the waterline line is timed (the
        // waterline section's start, t=25.5); the survey probe (t≈52, the left wall's foot) shot as it enters or late
        // (it breaks by t≈58); the pumps' secondary (pumps at t=78, 90, 102, 114, 126) met with the third pump on
        // easy, the fourth on medium, the fifth on hard (up to t≈130), or not at all.
        runs.put(
                LEVEL_23,
                List.of(
                        List.of(),
                        List.of(
                                new Event(53.5, CueTrigger.SECRET, "survey probe"),
                                new Event(116.5, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(
                                new Event(57.5, CueTrigger.SECRET, "survey probe"),
                                new Event(104.5, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(new Event(128.5, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(new Event(130, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        // M8 part D: Level 24's events at the autopilot's times (step D4, the plan's fit, seeds 1-3): the first Ray's
        // shadow at its wave's entry (t=58.0 on every run); the reefs' secondary (reefs at t=36, 48, 64, 80) met with
        // the second reef on easy (t≈51.5), the third on medium (t≈66.7); M8 part E, step E5 (round 53 C.1: hard allows
        // one reef away too): on hard with the third (t≈67.5, its reefs 260 HP) or, one reef away, the fourth
        // (t≈82-84.5); the Meridian wreck (t≈105, x 70) shot at t≈107.1-110.8, or not at all
        runs.put(
                LEVEL_24,
                List.of(
                        List.of(new Event(58, CueTrigger.FIRST_RAY, "")),
                        List.of(
                                new Event(58, CueTrigger.FIRST_RAY, ""),
                                new Event(66.5, CueTrigger.SECONDARY_OBJECTIVE, ""),
                                new Event(107.5, CueTrigger.SECRET, "Meridian")),
                        List.of(
                                new Event(51.5, CueTrigger.SECONDARY_OBJECTIVE, ""),
                                new Event(58, CueTrigger.FIRST_RAY, ""),
                                new Event(110.5, CueTrigger.SECRET, "Meridian")),
                        List.of(
                                new Event(58, CueTrigger.FIRST_RAY, ""),
                                new Event(67.5, CueTrigger.SECONDARY_OBJECTIVE, ""),
                                new Event(107, CueTrigger.SECRET, "Meridian")),
                        List.of(
                                new Event(58, CueTrigger.FIRST_RAY, ""),
                                new Event(82, CueTrigger.SECONDARY_OBJECTIVE, ""),
                                new Event(109.5, CueTrigger.SECRET, "Meridian"))));
        // M8 part E: Level 25's events (step E3's runs from the data's rows): the survey sub inside the left wall's
        // notch
        // (t≈78, x 78; the autopilot leaves it) shot as it passes the ship (t≈79-82) or not at all; the kill-ratio
        // secondary judged at the level's end (t=173).
        runs.put(
                LEVEL_25,
                List.of(
                        List.of(),
                        List.of(new Event(79.5, CueTrigger.SECRET, "survey sub")),
                        List.of(
                                new Event(81.5, CueTrigger.SECRET, "survey sub"),
                                new Event(173, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(new Event(173, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        // M8 part F: Level 26's events (step F2's runs from the data's rows): the farm barge (t≈84, x 60, on the dark's
        // open left side) shot as it enters (t≈85) or as it passes the ship (t≈88), or not at all; the kill-ratio
        // secondary judged at the level's end (t=182). Rook's three lines are scripted (no escort needed); the Choir's
        // line at t=99 between Varga's t=63 and t=110.5.
        runs.put(
                LEVEL_26,
                List.of(
                        List.of(),
                        List.of(new Event(85.5, CueTrigger.SECRET, "farm barge")),
                        List.of(
                                new Event(88.5, CueTrigger.SECRET, "farm barge"),
                                new Event(182, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(new Event(182, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        return runs;
    }

    static List<List<Event>> level17Runs() {
        CueTrigger hit = CueTrigger.ALLY_HIT;
        CueTrigger lost = CueTrigger.ALLY_LOST;
        return List.of(
                List.of(),
                List.of(
                        new Event(55.8, CueTrigger.FIRST_RING, ""),
                        new Event(76.5, CueTrigger.SECRET, "seed vault"),
                        new Event(136, CueTrigger.SECONDARY_OBJECTIVE, "")),
                List.of(
                        Event.named(41, hit, "Bradbury"),
                        Event.named(47, hit, "Lowell"),
                        Event.named(50, lost, "Lowell"),
                        new Event(55.8, CueTrigger.FIRST_RING, ""),
                        new Event(78, CueTrigger.SECRET, "seed vault")),
                List.of(
                        Event.named(48.5, hit, "Lowell"),
                        Event.named(55, lost, "Lowell"),
                        Event.named(103, hit, "Schiaparelli"),
                        Event.named(112, hit, "Burroughs"),
                        Event.named(131, hit, "Eden")),
                List.of(
                        Event.named(40, hit, "Bradbury"),
                        Event.named(44.5, lost, "Bradbury"),
                        Event.named(47, hit, "Lowell"),
                        Event.named(49, lost, "Lowell"),
                        new Event(75, CueTrigger.SECRET, "seed vault"),
                        Event.named(106, hit, "Schiaparelli"),
                        Event.named(109, lost, "Schiaparelli"),
                        Event.named(113, hit, "Burroughs"),
                        Event.named(121, lost, "Burroughs"),
                        Event.named(128, hit, "Eden"),
                        Event.named(133, lost, "Eden")));
    }

    static Map<String, List<List<Event>>> runs01To10() {
        return Map.of(
                LEVEL_10,
                List.of(
                        List.of(),
                        // as the autopilot's runs see them: the first decloak at 55, the first loop-back at
                        // 67.6, a shuttle hit early or late, the ferry cache as it passes, a shuttle lost
                        List.of(
                                new Event(15, CueTrigger.FIRST_ALLY_HIT, ""),
                                new Event(55, CueTrigger.FIRST_DECLOAK, ""),
                                new Event(67.6, CueTrigger.FIRST_LOOP_BACK, "")),
                        List.of(
                                new Event(20.1, CueTrigger.FIRST_ALLY_HIT, ""),
                                new Event(55, CueTrigger.FIRST_DECLOAK, ""),
                                new Event(67.6, CueTrigger.FIRST_LOOP_BACK, ""),
                                new Event(150.5, CueTrigger.SECRET, "ferry cache"),
                                new Event(165.5, CueTrigger.ALLY_LOST, "")),
                        // a loss just before Lifeline Three's line, and one just after the lance
                        List.of(new Event(33, CueTrigger.FIRST_ALLY_HIT, ""), new Event(113, CueTrigger.ALLY_LOST, "")),
                        List.of(new Event(120, CueTrigger.ALLY_LOST, "")),
                        // a shuttle lost as Varga explains the lance, and the cache found just before
                        List.of(
                                new Event(137, CueTrigger.SECRET, "ferry cache"),
                                new Event(139.5, CueTrigger.ALLY_LOST, ""))),
                LEVEL_09,
                List.of(
                        List.of(),
                        // the first node dies early or late in the plaza's hold; the first pounce on the boulevard
                        List.of(
                                Event.inHold(0, 2, CueTrigger.FIRST_KILL, "hive-node"),
                                new Event(60, CueTrigger.FIRST_POUNCE, "")),
                        List.of(
                                Event.inHold(0, 5, CueTrigger.FIRST_KILL, "hive-node"),
                                new Event(75, CueTrigger.FIRST_POUNCE, ""),
                                new Event(88, CueTrigger.SECRET, "cocoon cache")),
                        // the pounce just after Varga's warning, the cocoon as it passes
                        List.of(
                                new Event(58.5, CueTrigger.FIRST_POUNCE, ""),
                                new Event(86, CueTrigger.SECRET, "cocoon cache"))),
                LEVEL_08,
                List.of(
                        List.of(),
                        // Rook's first kill as the autopilot flies it with him (the first Skitters at t=12),
                        // just after his side line, and late, before the t=43 line
                        List.of(new Event(17.3, CueTrigger.ESCORT_FIRST_KILL, "")),
                        List.of(new Event(12.5, CueTrigger.ESCORT_FIRST_KILL, "")),
                        List.of(new Event(36, CueTrigger.ESCORT_FIRST_KILL, "")),
                        // the billboard toppled as it enters, or late as it passes
                        List.of(new Event(101, CueTrigger.SECRET, "billboard cache")),
                        List.of(
                                new Event(17.3, CueTrigger.ESCORT_FIRST_KILL, ""),
                                new Event(105, CueTrigger.SECRET, "billboard cache"))),
                LEVEL_01,
                List.of(
                        List.of(),
                        List.of(new Event(9, CueTrigger.FIRST_KILL, "skitter")),
                        List.of(new Event(30, CueTrigger.FIRST_KILL, "skitter"))),
                LEVEL_02,
                List.of(
                        List.of(),
                        List.of(new Event(36, CueTrigger.GROUP_CLEARED, "Dock One")),
                        List.of(
                                new Event(40, CueTrigger.GROUP_LOST, "Dock One"),
                                new Event(40, CueTrigger.FIRST_GROUP_LOST, ""))),
                LEVEL_03,
                List.of(
                        List.of(),
                        // the run in the capture: a bomber of the t=42 line slips past just before the first pass
                        List.of(new Event(55, CueTrigger.ENEMY_ESCAPED, "spore-bomber")),
                        List.of(new Event(20, CueTrigger.ENEMY_ESCAPED, "spore-bomber")),
                        List.of(new Event(45, CueTrigger.SECRET, "lifeboat rack")),
                        List.of(
                                new Event(45, CueTrigger.SECRET, "lifeboat rack"),
                                new Event(52, CueTrigger.ENEMY_ESCAPED, "spore-bomber")),
                        List.of(new Event(150, CueTrigger.FIRST_KILL, "leviathan")),
                        // it leaves alive: up through the top edge 2.7 s after its rise at t=158
                        List.of(new Event(160.7, CueTrigger.ENEMY_ESCAPED, "leviathan"))),
                LEVEL_04,
                List.of(
                        List.of(),
                        // the first turret nest hits a crawler, a walker of the pincer claws one to death
                        List.of(new Event(41, CueTrigger.FIRST_ALLY_HIT, "")),
                        List.of(
                                new Event(41, CueTrigger.FIRST_ALLY_HIT, ""),
                                new Event(91, CueTrigger.FIRST_ALLY_LOST, "")),
                        // a crawler lost to the bridge turrets, just before Varga's walker line … and after it
                        List.of(new Event(72, CueTrigger.FIRST_ALLY_LOST, "")),
                        List.of(new Event(126, CueTrigger.SECRET, "prospector's cache")),
                        List.of(new Event(23, CueTrigger.FIRST_ALLY_HIT, ""))),
                LEVEL_05,
                List.of(
                        List.of(),
                        // the batteries cleared as the autopilot clears them, the stuck sled opened between two sleds
                        List.of(
                                new Event(57.5, CueTrigger.GROUP_CLEARED, "Battery A"),
                                new Event(90.5, CueTrigger.GROUP_CLEARED, "Battery B"),
                                new Event(110.5, CueTrigger.GROUP_CLEARED, "Battery C"),
                                new Event(137.5, CueTrigger.GROUP_CLEARED, "Battery D")),
                        List.of(new Event(33, CueTrigger.SECRET, "stuck sled")),
                        // the frigate's phases in a fast fight (the arena clock runs on), and its death
                        List.of(
                                new Event(137.5, CueTrigger.GROUP_CLEARED, "Battery D"),
                                new Event(170, CueTrigger.BOSS_PHASE, "Core"),
                                new Event(185, CueTrigger.BOSS_DESTROYED, "gorgon-frigate"))),
                LEVEL_06,
                List.of(
                        List.of(),
                        // the survey cache lit by the t=112 flare (or found by headlight on the way in)
                        List.of(new Event(112.5, CueTrigger.SECRET, "survey cache")),
                        List.of(new Event(111, CueTrigger.SECRET, "survey cache")),
                        // the data core collected as the terminal passes, before and between the last lines
                        List.of(new Event(182, CueTrigger.SECRET, "settlement log")),
                        List.of(new Event(178, CueTrigger.SECRET, "settlement log"))),
                LEVEL_07,
                List.of(
                        List.of(),
                        // the lifeboat's cable cut as the tow comes in, or late as it drifts out
                        List.of(new Event(25, CueTrigger.SECRET, "lifeboat tow")),
                        List.of(new Event(33, CueTrigger.SECRET, "lifeboat tow")),
                        // the carrier as the autopilot fights it at medium (the broadside 25 s after the
                        // bar, the core at about 69 s, the kill at about 90 s; the clock then jumps to the
                        // arena's end), and a fast fight
                        List.of(
                                new Event(125, CueTrigger.BOSS_PHASE, "Broadside"),
                                new Event(169, CueTrigger.BOSS_PHASE, "Core"),
                                new Event(190, CueTrigger.BOSS_DESTROYED, "brood-carrier")),
                        List.of(
                                new Event(33, CueTrigger.SECRET, "lifeboat tow"),
                                new Event(125, CueTrigger.BOSS_PHASE, "Broadside"),
                                new Event(150, CueTrigger.BOSS_PHASE, "Core"),
                                new Event(165, CueTrigger.BOSS_DESTROYED, "brood-carrier")),
                        // the broadside times out: 70 s after its 4 s move
                        List.of(
                                new Event(125, CueTrigger.BOSS_PHASE, "Broadside"),
                                new Event(199, CueTrigger.BOSS_TIMEOUT, "Core"),
                                new Event(199, CueTrigger.BOSS_PHASE, "Core"),
                                new Event(230, CueTrigger.BOSS_DESTROYED, "brood-carrier"))));
    }

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
     * One voiced run of the level to its end and through the outro: the run's settings, what played and
     * the real second the level clock reached the level's end ({@code ended}), when the flight ends
     * ({@code LEVEL_COMPLETE}) and the outro starts. {@code home} is the count of convoy units home the
     * level-end lines are chosen for.
     */
    record EndRun(
            Difficulty difficulty,
            List<Event> events,
            int fitted,
            String side,
            double hold,
            int home,
            List<Played> played,
            double ended,
            double bark,
            double handover) {
        /** Whether {@code cue} can start in this run: the fit allows it, and a level-end line is this run's count's. */
        boolean allows(RadioCue cue) {
            return cue.allowedWith(fitted)
                    && (cue.trigger() != CueTrigger.LEVEL_END || (home >= cue.alliesMin() && home <= cue.alliesMax()));
        }

        @Override
        public String toString() {
            return difficulty + ", " + events + ", fit " + fitted + ", " + side
                    + (Double.isNaN(hold) ? "" : ", holds " + hold + " s") + ", " + home + " home"
                    + (Double.isNaN(bark) ? "" : ", Rook's bark " + bark + " s before the end");
        }
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
        return END_RUNS.computeIfAbsent(level, key -> playEndRuns(key, levelRuns()));
    }

    private static List<EndRun> playEndRuns(String level, List<List<Event>> runs) {
        List<EndRun> endRuns = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, level, difficulty);
            int[] homes = script.radio().stream()
                    .filter(cue -> cue.trigger() == CueTrigger.LEVEL_END)
                    .mapToInt(RadioCue::alliesMin)
                    .distinct()
                    .toArray();
            for (List<Event> events : runs) {
                if (events.stream().anyMatch(event -> event.t() > script.seconds())) {
                    continue;
                }
                for (int fitted : FITS) {
                    for (String side : SIDES) {
                        for (double hold : holdSeconds(script)) {
                            for (int home : homes) {
                                for (double bark : (fitted & ESCORT) != 0 ? BARKS_BEFORE_END : NO_BARK) {
                                    double[] ended = {Double.NaN, Double.NaN};
                                    List<Played> played = play(
                                            script,
                                            events,
                                            fitted,
                                            true,
                                            Double.NaN,
                                            new double[1],
                                            side,
                                            hold,
                                            home,
                                            ended,
                                            bark);
                                    endRuns.add(new EndRun(
                                            difficulty,
                                            events,
                                            fitted,
                                            side,
                                            hold,
                                            home,
                                            played,
                                            ended[0],
                                            bark,
                                            ended[1]));
                                }
                            }
                        }
                    }
                }
            }
        }
        return endRuns;
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

    static float readVoiceSeconds(String speaker, String text, String expression) {
        Optional<String> path = VoiceLines.radioVoice(VOICES, CONTENT.voices(), speaker, text, expression);
        if (path.isEmpty()) {
            return 0;
        }
        try (VorbisFile file = new VorbisFile(java.nio.file.Files.readAllBytes(ASSETS.resolve(path.get())))) {
            return (float) file.frameCount() / file.sampleRate();
        } catch (java.io.IOException e) {
            return 0;
        }
    }

    /**
     * Whether one of {@code events} has set off the event cue by {@code scriptTick} (the level
     * clock), an event in a hold by {@code realTick} with hold {@code k} started at {@code holdStarts[k]}.
     */
    static boolean setOff(List<Event> events, RadioCue cue, int scriptTick, int realTick, int[] holdStarts) {
        return events.stream()
                .anyMatch(event -> event.trigger() == cue.trigger()
                        && event.subject().equals(cue.subject())
                        && happened(event, scriptTick, realTick, holdStarts));
    }

    /** Whether {@code event} has happened by {@code scriptTick} (the level clock), in a hold by {@code realTick}. */
    static boolean happened(Event event, int scriptTick, int realTick, int[] holdStarts) {
        return event.hold() < 0
                ? SimStep.ticks(event.t()) <= scriptTick
                : holdStarts[event.hold()] >= 0 && realTick - holdStarts[event.hold()] >= SimStep.ticks(event.into());
    }

    /** A cue queued in {@link #play}: its index, its text, when it was due and when its message opened. */
    private static final class Queued {
        final int cue;
        final String text;
        final double due;
        Double opened;

        Queued(int cue, String text, double due) {
            this.cue = cue;
            this.text = text;
            this.due = due;
        }
    }

    /**
     * M5 part C: a hold zone as the test flies it: from {@code start} (script time, when the first
     * unit of its groups reaches its depth below the top edge, scrolling at the section's speed) the
     * level clock eases (smoothstep on the speed, as the simulation) over {@code ramp} to {@code ratio}
     * of real time, for {@code seconds} real seconds (its groups' kill), and back; a hold over the
     * collapse's groups runs on through the collapse's warning, drop and blast until its dust has
     * settled (user decision, 2026-10-07), the collapse cue starting as its groups die.
     */
    record HoldRun(double start, double ratio, double ramp, double seconds, boolean collapse) {}

    static List<HoldRun> holdRuns(LevelScript script, double seconds) {
        List<HoldRun> runs = new ArrayList<>();
        if (Double.isNaN(seconds)) {
            return runs;
        }
        for (ObjectiveSpec.Hold hold : script.objectives().holds()) {
            double start = Double.POSITIVE_INFINITY;
            for (GroundUnit unit : script.groundUnits()) {
                if (unit.group() >= 0 && hold.waitsFor(unit.group())) {
                    double speed = sectionSpeed(script, unit.t());
                    start = Math.min(start, unit.t() + (unit.enemy().hitbox().height() / 2 + hold.depth()) / speed);
                }
            }
            boolean collapse = script.objectives()
                    .collapse()
                    .map(c -> c.groups().equals(hold.groups()))
                    .orElse(false);
            double extra = collapse
                    ? script.objectives().collapse().get().warningSeconds()
                            + script.objectives().collapse().get().dropSeconds()
                            + script.objectives().collapse().get().settleSeconds()
                    : 0;
            runs.add(new HoldRun(
                    start, hold.speed() / sectionSpeed(script, start), hold.rampSeconds(), seconds + extra, collapse));
        }
        return runs;
    }

    static double sectionSpeed(LevelScript script, double t) {
        for (Section section : script.sections()) {
            if (t < section.end()) {
                return section.speed();
            }
        }
        return script.sections().getLast().speed();
    }

    static boolean opened(List<Played> played, String subject) {
        return played.stream()
                .anyMatch(line ->
                        line.cue().subject().equals(subject) && line.opened().isPresent());
    }

    /**
     * Steps the queue over the level: the timed cues at their ticks and {@code events} at theirs,
     * each queued by its priority, the level-end cues at the end; the radio runs on after the end
     * until it is idle.
     */
    static List<Played> play(LevelScript script, List<Event> events) {
        return play(script, events, ESCORT, false);
    }

    /** As the game queues them with {@code fitted} on the ship: a cue it does not allow never starts. */
    static List<Played> play(LevelScript script, List<Event> events, int fitted, boolean voiced) {
        return play(script, events, fitted, voiced, Double.NaN, new double[1], SIDES.getFirst(), 6);
    }

    /**
     * As above, with the low-armour line queued urgent at {@code lowArmourAt} (NaN for never);
     * {@code lowArmourOpened[0]} gets the time its message opened.
     */
    static List<Played> play(
            LevelScript script,
            List<Event> events,
            int fitted,
            boolean voiced,
            double lowArmourAt,
            double[] lowArmourOpened) {
        return play(script, events, fitted, voiced, lowArmourAt, lowArmourOpened, SIDES.getFirst(), 6);
    }

    /**
     * As above with Rook on {@code side} and each hold zone lasting {@code holdSeconds} real seconds
     * (NaN or a level without holds: the level clock runs with real time). The loop steps real time;
     * the level clock follows the holds.
     */
    static List<Played> play(
            LevelScript script,
            List<Event> events,
            int fitted,
            boolean voiced,
            double lowArmourAt,
            double[] lowArmourOpened,
            String side,
            double holdSeconds) {
        return play(script, events, fitted, voiced, lowArmourAt, lowArmourOpened, side, holdSeconds, -1, new double[1]);
    }

    /**
     * As above with {@code allies} convoy units home at the end (only the level-end cues for that many
     * start; -1: every level-end cue); {@code endedAt[0]} gets the real second the level clock reached
     * the level's end.
     */
    static List<Played> play(
            LevelScript script,
            List<Event> events,
            int fitted,
            boolean voiced,
            double lowArmourAt,
            double[] lowArmourOpened,
            String side,
            double holdSeconds,
            int allies,
            double[] endedAt) {
        return play(
                script,
                events,
                fitted,
                voiced,
                lowArmourAt,
                lowArmourOpened,
                side,
                holdSeconds,
                allies,
                endedAt,
                Double.NaN);
    }

    /**
     * As above, with Rook's {@link #longestBarkLine} queued as an event line {@code barkBefore} seconds
     * before the level's end (level clock; NaN for none, and none without him flying). At the level's
     * end the radio plays out the closing lines ({@link RadioQueue#outro}) and the outro runs as the
     * game's ({@link Outro}): {@code endedAt[1]}, if there is room for it, gets the real second the
     * debrief takes over (NaN if not within a minute).
     */
    static List<Played> play(
            LevelScript script,
            List<Event> events,
            int fitted,
            boolean voiced,
            double lowArmourAt,
            double[] lowArmourOpened,
            String side,
            double holdSeconds,
            int allies,
            double[] endedAt,
            double barkBefore) {
        Outro outro = new Outro();
        int barkTick = Double.isNaN(barkBefore) || (fitted & ESCORT) == 0
                ? Integer.MAX_VALUE
                : SimStep.ticks(script.seconds() - barkBefore);
        boolean barked = false;
        int lowArmourTick = Double.isNaN(lowArmourAt) ? -1 : SimStep.ticks(lowArmourAt);
        List<String> lowArmourLines = RadioQueue.wrap(LOW_ARMOUR.line());
        RadioSchedule schedule = new RadioSchedule(script, fitted & ~ESCORT);
        boolean escort = (fitted & ESCORT) != 0;
        RadioQueue radio = new RadioQueue();
        List<RadioCue> cues = script.radio();
        // Round 45 B.a: the cues' delays after their events, as the game reads them from the level's data
        Optional<LevelData> level = CONTENT.levelKey(script.number()).map(CONTENT::level);
        float[] delays = new float[cues.size()];
        for (int i = 0; i < delays.length; i++) {
            int cue = i;
            delays[i] = level.map(data -> (float) data.radioDelay(cue)).orElse(0f);
        }
        boolean[] fired = new boolean[cues.size()];
        // M7 part D: the events of repeating triggers, each setting off its cues once
        boolean[] eventFired = new boolean[events.size()];
        List<Queued> queued = new ArrayList<>();
        int end = SimStep.ticks(script.seconds());
        // M5 part D: the scripted loss's time (Level 10's lance), never without one
        int lanceTick = script.objectives()
                .escort()
                .flatMap(ObjectiveSpec.Escort::air)
                .flatMap(ObjectiveSpec.Air::scriptedLoss)
                .map(loss -> SimStep.ticks(loss.t()))
                .orElse(Integer.MAX_VALUE);
        List<HoldRun> holds = holdRuns(script, holdSeconds);
        int[] holdStarts = new int[holds.size()];
        java.util.Arrays.fill(holdStarts, -1);
        // The level clock in fixed point, as the simulation's (1/65 536 of a step).
        long scriptFixed = 0;
        int scriptTick = 0;
        int afterEnd = -1;
        for (int tick = 1; afterEnd < 0 || tick <= afterEnd; tick++) {
            double seconds = (double) tick / SimStep.PER_SECOND;
            // The game's frame: the outro first, then the simulation's step and the radio.
            if (outro.update((float) SimStep.SECONDS, radio) && endedAt.length > 1 && Double.isNaN(endedAt[1])) {
                endedAt[1] = seconds - SimStep.SECONDS;
            }
            double rate = 1;
            boolean collapsing = false;
            for (int h = 0; h < holds.size(); h++) {
                HoldRun hold = holds.get(h);
                if (holdStarts[h] < 0 && scriptTick >= SimStep.ticks(hold.start())) {
                    holdStarts[h] = tick;
                }
                if (holdStarts[h] >= 0) {
                    double in = (tick - holdStarts[h]) * SimStep.SECONDS;
                    double out = in - hold.seconds();
                    double s = out < 0 ? Math.min(1, in / hold.ramp()) : Math.max(0, 1 - out / hold.ramp());
                    rate = Math.min(rate, 1 + (hold.ratio() - 1) * s * s * (3 - 2 * s));
                    collapsing |= hold.collapse() && in >= holdSeconds;
                }
            }
            scriptFixed += Math.max(1, Math.round(rate * 65536));
            scriptTick = (int) (scriptFixed >> 16);
            if (scriptTick >= end && afterEnd < 0) {
                afterEnd = tick + SimStep.ticks(60);
                endedAt[0] = seconds;
                // LEVEL_COMPLETE: the outro starts and the radio plays out the closing lines.
                outro.start();
                radio.outro();
            }
            if (!barked && scriptTick >= barkTick && scriptTick < end) {
                barked = true;
                Barks.Line line = longestBarkLine();
                RadioQueue.Message bark = barkMessage(line);
                radio.add(
                        bark.speaker(),
                        bark.portrait(),
                        bark.expression(),
                        line.text(),
                        false,
                        line.priority(),
                        Optional.empty(),
                        voiced ? bark.voiceSeconds() : 0);
            }
            double scriptSeconds = (double) scriptFixed / 65536 * SimStep.SECONDS;
            for (int i = 0; i < cues.size(); i++) {
                RadioCue cue = cues.get(i);
                // a fired or disallowed cue never queues again: skip it before asking whether it is due; a
                // repeating trigger's cue is queued by each of its events below
                if (fired[i] || !cue.allowedWith(fitted) || cue.trigger().repeats()) {
                    continue;
                }
                boolean due =
                        switch (cue.trigger()) {
                            case TIME -> scriptTick <= end && SimStep.ticks(cue.t()) <= scriptTick;
                            case LEVEL_END ->
                                scriptTick >= end
                                        && (allies < 0 || (allies >= cue.alliesMin() && allies <= cue.alliesMax()));
                            case HOLD_START -> holdStarts.length > 0 && holdStarts[0] >= 0;
                            case COLLAPSE -> collapsing;
                            // M5 part D: the lance comes at its time in every run (Level 10)
                            case SCRIPTED_LOSS -> scriptTick >= lanceTick;
                            default -> setOff(events, cue, scriptTick, tick, holdStarts);
                        };
                if (due) {
                    fired[i] = true;
                    double dueAt = cue.trigger() == CueTrigger.TIME && holds.isEmpty() ? cue.t() : seconds;
                    queued.add(queue(radio, schedule, cue, i, text(cue, side), dueAt, voiced, delays[i]));
                }
            }
            for (int e = 0; e < events.size(); e++) {
                Event event = events.get(e);
                if (eventFired[e] || !event.trigger().repeats() || !happened(event, scriptTick, tick, holdStarts)) {
                    continue;
                }
                eventFired[e] = true;
                for (int i = 0; i < cues.size(); i++) {
                    RadioCue cue = cues.get(i);
                    if (cue.trigger() == event.trigger()
                            && cue.subject().equals(event.subject())
                            && cue.allowedWith(fitted)) {
                        queued.add(queue(
                                radio, schedule, cue, i, text(cue, side, event.ally()), seconds, voiced, delays[i]));
                    }
                }
            }
            if (tick == lowArmourTick) {
                radio.add(
                        LOW_ARMOUR.speaker(),
                        LOW_ARMOUR.speaker(),
                        LOW_ARMOUR_EXPRESSION,
                        LOW_ARMOUR.line(),
                        LOW_ARMOUR.distorted().orElse(false),
                        RadioQueue.Priority.URGENT,
                        Optional.empty(),
                        voiced ? voiceSeconds(LOW_ARMOUR.speaker(), LOW_ARMOUR.line(), LOW_ARMOUR_EXPRESSION) : 0);
            }
            float untilTimed =
                    scriptTick < end ? schedule.untilTimed(scriptSeconds, rate, escort) : Float.POSITIVE_INFINITY;
            if (radio.update((float) SimStep.SECONDS, untilTimed) == RadioQueue.Change.OPENED) {
                RadioQueue.Message message = radio.current().orElseThrow();
                if (Double.isNaN(lowArmourOpened[0])
                        && message.speaker().equals(LOW_ARMOUR.speaker())
                        && message.lines().equals(lowArmourLines)) {
                    lowArmourOpened[0] = seconds;
                }
                for (Queued line : queued) {
                    if (line.opened == null
                            && cues.get(line.cue).speaker().equals(message.speaker())
                            && RadioQueue.wrap(line.text).equals(message.lines())) {
                        line.opened = seconds;
                        break;
                    }
                }
            }
        }
        List<Played> played = new ArrayList<>();
        for (int i = 0; i < cues.size(); i++) {
            boolean any = false;
            for (Queued line : queued) {
                if (line.cue == i) {
                    played.add(new Played(cues.get(i), Optional.ofNullable(line.opened), line.due, line.text));
                    any = true;
                }
            }
            if (!any) {
                played.add(new Played(cues.get(i), Optional.empty(), 0, text(cues.get(i), side)));
            }
        }
        return played;
    }

    /**
     * Queues cue {@code i} with {@code text} on {@code radio} as the game does, due at {@code due}, held {@code
     * delay} seconds (round 45 B.a).
     */
    private static Queued queue(
            RadioQueue radio,
            RadioSchedule schedule,
            RadioCue cue,
            int i,
            String text,
            double due,
            boolean voiced,
            float delay) {
        radio.add(
                cue.speaker(),
                cue.portrait(),
                cue.expression(),
                text,
                cue.distorted(),
                schedule.priority(i),
                Optional.empty(),
                voiced ? voiceSeconds(cue.speaker(), text, cue.expression()) : 0,
                delay);
        return new Queued(i, text, due);
    }
}
