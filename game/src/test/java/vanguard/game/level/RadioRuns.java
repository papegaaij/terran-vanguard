package vanguard.game.level;

import static vanguard.game.level.RadioPlayer.sectionSpeed;
import static vanguard.game.level.RadioTimeline.LEVEL_01;
import static vanguard.game.level.RadioTimeline.LEVEL_02;
import static vanguard.game.level.RadioTimeline.LEVEL_03;
import static vanguard.game.level.RadioTimeline.LEVEL_04;
import static vanguard.game.level.RadioTimeline.LEVEL_05;
import static vanguard.game.level.RadioTimeline.LEVEL_06;
import static vanguard.game.level.RadioTimeline.LEVEL_07;
import static vanguard.game.level.RadioTimeline.LEVEL_08;
import static vanguard.game.level.RadioTimeline.LEVEL_09;
import static vanguard.game.level.RadioTimeline.LEVEL_10;
import static vanguard.game.level.RadioTimeline.LEVEL_11;
import static vanguard.game.level.RadioTimeline.LEVEL_12;
import static vanguard.game.level.RadioTimeline.LEVEL_13;
import static vanguard.game.level.RadioTimeline.LEVEL_14;
import static vanguard.game.level.RadioTimeline.LEVEL_15;
import static vanguard.game.level.RadioTimeline.LEVEL_16;
import static vanguard.game.level.RadioTimeline.LEVEL_17;
import static vanguard.game.level.RadioTimeline.LEVEL_18;
import static vanguard.game.level.RadioTimeline.LEVEL_19;
import static vanguard.game.level.RadioTimeline.LEVEL_20;
import static vanguard.game.level.RadioTimeline.LEVEL_21;
import static vanguard.game.level.RadioTimeline.LEVEL_22;
import static vanguard.game.level.RadioTimeline.LEVEL_23;
import static vanguard.game.level.RadioTimeline.LEVEL_24;
import static vanguard.game.level.RadioTimeline.LEVEL_25;
import static vanguard.game.level.RadioTimeline.LEVEL_26;
import static vanguard.game.level.RadioTimeline.LEVEL_27;
import static vanguard.game.level.RadioTimeline.LEVEL_28;
import static vanguard.game.level.RadioTimeline.LEVEL_29;
import static vanguard.game.level.RadioTimeline.LEVEL_30;
import static vanguard.game.level.RadioTimeline.LEVEL_31;
import static vanguard.game.level.RadioTimeline.LEVEL_32;
import static vanguard.game.level.RadioTimeline.LEVEL_33;
import static vanguard.game.level.RadioTimeline.LEVEL_34;
import static vanguard.game.level.RadioTimeline.LEVEL_35;
import static vanguard.game.level.RadioTimeline.LEVEL_36;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import vanguard.game.level.RadioTimeline.Event;
import vanguard.sim.CueTrigger;
import vanguard.sim.EscortSwap;
import vanguard.sim.GroundUnit;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;

/**
 * RF6: the radio timeline's runs ({@link RadioTimeline#RUNS}): per level, the events a player sets off on top of the
 * timed lines and the level end, each list one run. One method per act; a new act adds its own method to {@link #all}.
 * The hold zones a level with holds is flown through ({@link #holdRuns}) are worked out from its script.
 */
final class RadioRuns {
    private RadioRuns() {}

    /** Every level's runs, by level key. */
    static Map<String, List<List<Event>>> all() {
        Map<String, List<List<Event>>> runs = new LinkedHashMap<>();
        runs.putAll(act1());
        runs.putAll(act2());
        runs.putAll(act3());
        runs.putAll(act4());
        runs.putAll(act5());
        runs.putAll(act6());
        return runs;
    }

    /** Act 6, Jovian Storm (M10 part B, step B4): Level 36 ({@link #level36Runs}). */
    static Map<String, List<List<Event>>> act6() {
        Map<String, List<List<Event>>> runs = new LinkedHashMap<>();
        runs.put(LEVEL_36, level36Runs());
        return runs;
    }

    /**
     * M10 part B (step B4): Level 36's runs (Rook flies with every fit that has the escort; the first strike kill's bark
     * is his, {@code requires: escort}). The harvesters' secondary met as the sixth comes down (the pairs enter at t=72,
     * 86, 100 and 114: the sixth down from t≈101 with every pair taken, t≈122 with the last pair's first) or failed as
     * the third gets away (t≈99 on hard's early loss, to t≈122); the first strike kill in a told column (the strikes at
     * t=138, 152, 170 and hard's tracking one at 160); the pathfinder pod broken under its bank (t≈147-151). An outcome
     * after t≈122 (the last pair's second hull) runs into Varga's t=129.5 lightning line: pending round 74 (the long
     * lines shortened, or the rule).
     */
    static List<List<Event>> level36Runs() {
        CueTrigger met = CueTrigger.SECONDARY_OBJECTIVE;
        CueTrigger failed = CueTrigger.SECONDARY_FAILED;
        CueTrigger strike = CueTrigger.FIRST_STRIKE_KILL;
        CueTrigger secret = CueTrigger.SECRET;
        String pod = "pathfinder pod";
        return List.of(
                List.of(),
                List.of(new Event(101, met, ""), new Event(138.3, strike, ""), new Event(148, secret, pod)),
                List.of(new Event(116, met, ""), new Event(147.5, secret, pod), new Event(152.3, strike, "")),
                List.of(new Event(121.5, met, ""), new Event(170.3, strike, "")),
                List.of(new Event(99, failed, ""), new Event(138.3, strike, ""), new Event(149, secret, pod)),
                List.of(new Event(113.5, failed, ""), new Event(160.3, strike, "")),
                List.of(new Event(122, failed, ""), new Event(150.5, secret, pod), new Event(170.3, strike, "")),
                List.of(new Event(138.3, strike, ""), new Event(148, secret, pod)),
                List.of(new Event(110, met, "")));
    }

    /** Act 1, First Contact: Levels 01–07. */
    static Map<String, List<List<Event>>> act1() {
        Map<String, List<List<Event>>> runs = new LinkedHashMap<>();
        runs.put(
                LEVEL_01,
                List.of(
                        List.of(),
                        List.of(new Event(9, CueTrigger.FIRST_KILL, "skitter")),
                        List.of(new Event(30, CueTrigger.FIRST_KILL, "skitter"))));
        runs.put(
                LEVEL_02,
                List.of(
                        List.of(),
                        List.of(new Event(36, CueTrigger.GROUP_CLEARED, "Dock One")),
                        List.of(
                                new Event(40, CueTrigger.GROUP_LOST, "Dock One"),
                                new Event(40, CueTrigger.FIRST_GROUP_LOST, ""))));
        runs.put(
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
                        List.of(new Event(160.7, CueTrigger.ENEMY_ESCAPED, "leviathan"))));
        runs.put(
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
                        List.of(new Event(23, CueTrigger.FIRST_ALLY_HIT, ""))));
        runs.put(
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
                                new Event(185, CueTrigger.BOSS_DESTROYED, "gorgon-frigate"))));
        runs.put(
                LEVEL_06,
                List.of(
                        List.of(),
                        // the survey cache lit by the t=112 flare (or found by headlight on the way in)
                        List.of(new Event(112.5, CueTrigger.SECRET, "survey cache")),
                        List.of(new Event(111, CueTrigger.SECRET, "survey cache")),
                        // the data core collected as the terminal passes, before and between the last lines
                        List.of(new Event(182, CueTrigger.SECRET, "settlement log")),
                        List.of(new Event(178, CueTrigger.SECRET, "settlement log"))));
        runs.put(
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
        return runs;
    }

    /** Act 2, Homefront: Levels 08–14. */
    static Map<String, List<List<Event>>> act2() {
        Map<String, List<List<Event>>> runs = new LinkedHashMap<>();
        runs.put(
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
                                new Event(105, CueTrigger.SECRET, "billboard cache"))));
        runs.put(
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
                                new Event(86, CueTrigger.SECRET, "cocoon cache"))));
        runs.put(
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
                                new Event(139.5, CueTrigger.ALLY_LOST, ""))));
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
        return runs;
    }

    /** Act 3, Red Dust: Levels 15–21. */
    static Map<String, List<List<Event>>> act3() {
        Map<String, List<List<Event>>> runs = new LinkedHashMap<>();
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
        return runs;
    }

    /** Act 4, Deep Water: Levels 22–28. */
    static Map<String, List<List<Event>>> act4() {
        Map<String, List<List<Event>>> runs = new LinkedHashMap<>();
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
        // secondary judged at the level's end (t=188 since round 55's retime). Rook's three lines are scripted (no
        // escort
        // needed); the Choir's line at t=99 between Varga's t=63 and t=110.5.
        runs.put(
                LEVEL_26,
                List.of(
                        List.of(),
                        List.of(new Event(85.5, CueTrigger.SECRET, "farm barge")),
                        List.of(
                                new Event(88.5, CueTrigger.SECRET, "farm barge"),
                                new Event(188, CueTrigger.SECONDARY_OBJECTIVE, "")),
                        List.of(new Event(188, CueTrigger.SECONDARY_OBJECTIVE, ""))));
        // M8 part G: Level 27's events (step G2's runs from the data's rows): the Marlin (t≈40, x 90, by her stack)
        // shot
        // as she enters (t≈41) or as she passes the ship (t≈45.5), or not at all; the Abyss Ray's shadow at its wave's
        // entry (`first-ray`, t=106, its only Ray); the beacons' secondary has no line; no Rook lines (he is missing).
        runs.put(
                LEVEL_27,
                List.of(
                        List.of(new Event(106, CueTrigger.FIRST_RAY, "")),
                        List.of(new Event(41, CueTrigger.SECRET, "Marlin"), new Event(106, CueTrigger.FIRST_RAY, "")),
                        List.of(
                                new Event(45.5, CueTrigger.SECRET, "Marlin"),
                                new Event(106, CueTrigger.FIRST_RAY, ""))));
        // M8 part H: Level 28's approach (step H3's runs from the data's rows): Pelagic-3 (t≈58, x 90, under the fallen
        // rack) shot as she enters (t≈59.5) or as she passes the ship (t≈62), or not at all. Step H6: the arena flown
        // on
        // its halted clock (Event.inArena, real seconds after the halt at the arena's end): the arrival line at t=104,
        // then the phases, the dive and the kill at the autopilot's fight lengths (step H4: ≈ 93 s easy, 126.5 s
        // medium, 172 s hard), a fight with the dive right on phase 2's line and the implants on the dive's (the kill
        // 31 s after phase 3's spark, its two lines spoken), and one killed before the dive; the finale's closing lines
        // count from the kill (K+3 to ≈ K+53, the
        // level's end at K+62).
        runs.put(LEVEL_28, level28Runs());
        return runs;
    }

    /**
     * Act 5, The Belt (M9 part B, step B4). Level 29's events (step B3's measures; the radio is the same with the loaned
     * Bulwark, an owned one and {@code --escort none}, whose Ember leaves upward: Rook's rescue line plays a join's
     * length after the release all the same, RadioEvents): the first SAM salvo (t≈35); the first wavering pair gone
     * (t≈85.5, its 8.5 s on the screen from t=77) or one shot down (t≈80); the rescue halt on its held clock
     * ({@link Event#inArena}: the clock stands at the halt while a clamp gun stands) with the last clamp at H+13 (medium's
     * typical halt, step B3b), H+7.6 (easy's) or H+19 (hard's), Rook in his slot a second later; the secondary met as the third pair leaves
     * (t≈148.5); and a run whose clamps all fall before the halt (no halt: the clock runs through). Step B4b: the last
     * clamp's cue names what the swap does ({@code EscortSwap#radioSubject}): the Bulwark flying ("Bulwark, RTB"), the
     * loaner lost on hard's long halt (no Okafor line) and {@code --escort none} (the Ember leaves: "Rook, you're
     * clear").
     */
    static Map<String, List<List<Event>>> act5() {
        Map<String, List<List<Event>>> runs = new LinkedHashMap<>();
        runs.put(LEVEL_29, level29Runs());
        runs.put(LEVEL_30, level30Runs());
        runs.put(LEVEL_31, level31Runs());
        runs.put(LEVEL_32, level32Runs());
        runs.put(LEVEL_33, level33Runs());
        runs.put(LEVEL_34, level34Runs());
        runs.put(LEVEL_35, level35Runs());
        return runs;
    }

    /**
     * M9 part H (step H4, step H2's data): Level 35's runs. The approach has no event line (the lifeboat's secret has
     * none); the arena starts at t=92 with the boss's arrival (Vorne's smug welcome, Varga's after it), the clock halts
     * at t=93 while the station lives; its phases come with its ring locks (any order: "Two rings", "One ring", then "The
     * core" as the third locks) and its kill starts the closing queue (Okafor at K+5, Vorne's offer, Rook's answer with
     * him or Okafor's without him, Varga, Ceres Control). Step H3's measures (the core 14,000 HP): at medium the first
     * lock ≈ 26-30 s into the halt, the second and third seconds after it (the beam burns every ring at once), the kill
     * ≈ 148 s (easy ≈ 108, hard ≈ 248); with an evenly paced fight, a fast one, a run whose second and third locks fall
     * in one step (two phase cues at once) and a short one.
     */
    static List<List<Event>> level35Runs() {
        String sovereign = "iron-sovereign";
        Event arrival = new Event(92, CueTrigger.BOSS_ARRIVAL, "");
        return List.of(
                List.of(),
                sovereign(arrival, 26, 28, 33, 148, sovereign),
                sovereign(arrival, 30, 60, 95, 145, sovereign),
                sovereign(arrival, 18, 19, 20, 108, sovereign),
                sovereign(arrival, 45, 52, 58, 250, sovereign),
                sovereign(arrival, 12, 14, 16, 40, sovereign),
                sovereign(arrival, 26, 33, 33, 140, sovereign));
    }

    /** A Level 35 run: the arrival, then the three locks' phases and the kill this many seconds into the halt. */
    private static List<Event> sovereign(
            Event arrival, double two, double one, double core, double kill, String sovereign) {
        return List.of(
                arrival,
                Event.inArena(two, CueTrigger.BOSS_PHASE, "Two rings"),
                Event.inArena(one, CueTrigger.BOSS_PHASE, "One ring"),
                Event.inArena(core, CueTrigger.BOSS_PHASE, "The core"),
                Event.inArena(kill, CueTrigger.BOSS_DESTROYED, sovereign));
    }

    /**
     * M9 part G (step G4, step G2's measures): Level 34's runs (Rook flies with every fit that has the escort; *Old
     * Marta*'s line is his, {@code requires: escort}). The three secrets shot open as their triggers come on the screen
     * (*Vigil* t≈56, *Sable Wren* t≈96, *Old Marta* t≈165); the Helix core collected once it is loose (the fourth clamp
     * down from t≈108.3, its 1 s rise: t≈110 at the earliest, t≈122.8 the latest that finds its gap before Rook's t=130)
     * or missed (it leaves the screen or its time runs out, t≈114–121); the secondary met as the needed container opens (easy the fourth,
     * t≈82; medium the fifth, t≈151; hard the sixth, t≈171) or at the level's end with one away (t=186). Step FIX2
     * (pending round 67) put back the runs step G4 left out: *Sable Wren*'s hatch shot as it passes the ship (t≈99.5;
     * her line trimmed to one page, so it fits before Varga's t=108), the secondary met at t≈151 with *Old Marta* shot
     * open after it (Okafor's t=159 moved to 157), and the clamps never shot: the core is missed as the last clamp
     * leaves the bottom edge (t≈113.8).
     */
    static List<List<Event>> level34Runs() {
        CueTrigger secret = CueTrigger.SECRET;
        CueTrigger collected = CueTrigger.STORY_COLLECTED;
        CueTrigger missed = CueTrigger.STORY_MISSED;
        CueTrigger met = CueTrigger.SECONDARY_OBJECTIVE;
        return List.of(
                List.of(),
                List.of(new Event(57, secret, "Vigil")),
                List.of(new Event(97, secret, "Sable Wren")),
                List.of(new Event(166.4, secret, "Old Marta")),
                List.of(new Event(110, collected, "")),
                List.of(new Event(116, collected, "")),
                List.of(new Event(122.8, collected, "")),
                List.of(new Event(114, missed, "")),
                List.of(new Event(120, missed, "")),
                List.of(new Event(82, met, "")),
                List.of(new Event(151, met, "")),
                List.of(new Event(171, met, "")),
                List.of(new Event(186, met, "")),
                List.of(
                        new Event(57, secret, "Vigil"),
                        new Event(97, secret, "Sable Wren"),
                        new Event(112, collected, ""),
                        new Event(151, met, "")),
                List.of(new Event(166.4, secret, "Old Marta"), new Event(171, met, "")),
                List.of(new Event(99.5, secret, "Sable Wren")),
                List.of(new Event(113.8, missed, "")),
                List.of(new Event(151, met, ""), new Event(166.4, secret, "Old Marta")),
                List.of(
                        new Event(57, secret, "Vigil"),
                        new Event(99.5, secret, "Sable Wren"),
                        new Event(116, collected, ""),
                        new Event(151, met, ""),
                        new Event(168, secret, "Old Marta")),
                List.of(
                        new Event(58, secret, "Vigil"),
                        new Event(82, met, ""),
                        new Event(97, secret, "Sable Wren"),
                        new Event(121, missed, ""),
                        new Event(168, secret, "Old Marta")));
    }

    /**
     * M9 part F (step F4, step F3's flights): Level 33's runs (Rook flies with every fit that has the escort; the secret's
     * line is his, {@code requires: escort}). *Claim 114* shot open as it comes on the screen at t=104 (by t≈108 at the
     * latest, the ledge's shack in the growth); the secondary met at the leech kill that makes it unable to fail (48 of
     * 63 at medium: from the Swarm on, t≈125, to the last stream, t≈172), alone and after the secret.
     */
    static List<List<Event>> level33Runs() {
        String claim = "Claim 114";
        CueTrigger secret = CueTrigger.SECRET;
        CueTrigger met = CueTrigger.SECONDARY_OBJECTIVE;
        return List.of(
                List.of(),
                List.of(new Event(104.5, secret, claim)),
                List.of(new Event(106, secret, claim)),
                List.of(new Event(108, secret, claim)),
                List.of(new Event(125, met, "")),
                List.of(new Event(140, met, "")),
                List.of(new Event(155, met, "")),
                List.of(new Event(165, met, "")),
                List.of(new Event(172, met, "")),
                List.of(new Event(105, secret, claim), new Event(150, met, "")),
                List.of(new Event(107, secret, claim), new Event(168, met, "")));
    }

    /**
     * M9 part E (step E4, step E2's measures): Level 32's runs per route (Rook flies with every fit that has the escort).
     * Fork one's choice (the ship's side as the island's tip reaches its row: t≈52.6 with the ship at the top edge to
     * t≈56.75 at the bottom, the latest) plays its side's line, the left (the old drift) or the right (the charge seam);
     * fork two has no line. The first topple crush (`topple-crush`, Rook's bark): the seam's Strider by its nests
     * (t≈63, the right only), the hall pair by theirs (t≈94), the refinery's lone one by the gantry trio (t≈163). The
     * secondary met as the fourth tower dies: the crucible's (t=150, on the screen to t≈155.5), the last of the four.
     */
    static List<List<Event>> level32Runs() {
        CueTrigger branch = CueTrigger.BRANCH;
        CueTrigger crush = CueTrigger.TOPPLE_CRUSH;
        CueTrigger met = CueTrigger.SECONDARY_OBJECTIVE;
        return List.of(
                List.of(),
                List.of(new Event(56.75, branch, "1:left")),
                List.of(new Event(56.75, branch, "1:right")),
                List.of(new Event(52.6, branch, "1:left"), new Event(94, crush, ""), new Event(151, met, "")),
                List.of(new Event(52.6, branch, "1:right"), new Event(63, crush, ""), new Event(153, met, "")),
                List.of(new Event(55, branch, "1:right"), new Event(94, crush, ""), new Event(155.5, met, "")),
                List.of(new Event(54, branch, "1:left"), new Event(163, crush, "")),
                List.of(new Event(56, branch, "1:right"), new Event(66, crush, ""), new Event(151, met, "")));
    }

    /**
     * M9 part C (step C4): Level 30's runs (Rook flies with every fit that has the escort; the Bulwark and {@code
     * --escort none} have no radio of their own, so the fits without the escort stand for them). The secondary met as the
     * convoy's last Gunship (t=110) dies, at step C3's autopilot times (easy t≈104.3, medium and hard t≈112.6–116.9;
     * Rook's bark at t=123.5 and Okafor's tug line at t=131 after it); or failed: the first Gunship to leave the screen
     * alive ({@code enemy-escaped}, a cue per enemy, not per tag), the lone t=40 one (t≈53.5), the first convoy Gunship
     * (t≈109.5) or the last (t≈123.5).
     */
    static List<List<Event>> level30Runs() {
        String gunship = "gilded-gunship";
        return List.of(
                List.of(),
                List.of(new Event(104.3, CueTrigger.SECONDARY_OBJECTIVE, "")),
                List.of(new Event(112.6, CueTrigger.SECONDARY_OBJECTIVE, "")),
                List.of(new Event(115.5, CueTrigger.SECONDARY_OBJECTIVE, "")),
                List.of(new Event(116.9, CueTrigger.SECONDARY_OBJECTIVE, "")),
                List.of(new Event(53.5, CueTrigger.ENEMY_ESCAPED, gunship)),
                List.of(new Event(109.5, CueTrigger.ENEMY_ESCAPED, gunship)),
                List.of(new Event(123.5, CueTrigger.ENEMY_ESCAPED, gunship)));
    }

    /**
     * M9 part D (step D4, step D2's measures): Level 31's runs (Rook flies with every fit that has the escort). The
     * tug's first section hit ({@code first-ally-hit}) alone at the times the autopilot saw it (t≈12.5–128), and after a
     * first hit at t=27 Dacosta's claim shot open (step FIX1's measures: a bolt gun under it t≈100.4, the plan's beam
     * t≈101.1, the autopilot's strays t≈103.4–103.5) or a section lost (t≈120–176: the closing line at 176.5
     * waits for it). Each line names the engines ({@code {section}}: the longest name, the widest line).
     */
    static List<List<Event>> level31Runs() {
        String section = "engines";
        List<List<Event>> runs = new java.util.ArrayList<>();
        runs.add(List.of());
        for (double t : new double[] {12.5, 14, 26.5, 27.5, 30, 41.5, 44, 60, 86, 93.5, 100.5, 108, 128}) {
            runs.add(List.of(Event.named(t, CueTrigger.FIRST_ALLY_HIT, section)));
        }
        Event hit = Event.named(27, CueTrigger.FIRST_ALLY_HIT, section);
        for (double t : new double[] {100.4, 101, 102, 103, 103.5}) {
            runs.add(List.of(hit, new Event(t, CueTrigger.SECRET, "Dacosta's claim")));
        }
        for (double t : new double[] {120, 132, 136, 140, 145, 150, 160, 168, 176}) {
            runs.add(List.of(hit, Event.named(t, CueTrigger.ALLY_LOST, section)));
        }
        return List.copyOf(runs);
    }

    /** M9 part B (step B4): Level 29's runs ({@link #act5()}'s note). */
    static List<List<Event>> level29Runs() {
        String hawk = "wavering-stormhawk";
        Event homing = new Event(35, CueTrigger.FIRST_HOMING, "");
        Event left = new Event(85.5, CueTrigger.NEUTRAL_LEFT, hawk);
        Event met = new Event(148.5, CueTrigger.SECONDARY_OBJECTIVE, "");
        return List.of(
                List.of(),
                List.of(homing, left, met),
                rescue(homing, left, met, 13, EscortSwap.SUBJECT_BULWARK),
                rescue(homing, left, met, 7.6, EscortSwap.SUBJECT_BULWARK),
                rescue(homing, left, met, 19, EscortSwap.SUBJECT_BULWARK),
                rescue(homing, left, met, 19, EscortSwap.SUBJECT_JOINS),
                rescue(homing, left, met, 13, EscortSwap.SUBJECT_LEAVES),
                List.of(
                        new Event(34, CueTrigger.FIRST_HOMING, ""),
                        new Event(80, CueTrigger.NEUTRAL_DOWN, hawk),
                        Event.inArena(13, CueTrigger.HOLDERS_DOWN, EscortSwap.SUBJECT_BULWARK),
                        Event.inArena(14, CueTrigger.ESCORT_JOINED, "")));
    }

    /**
     * A Level 29 run: its events with the rescue halt's last clamp {@code halt} seconds into it (the swap doing {@code
     * swap}), Rook a second later.
     */
    private static List<Event> rescue(Event homing, Event left, Event met, double halt, String swap) {
        return List.of(
                homing,
                left,
                Event.inArena(halt, CueTrigger.HOLDERS_DOWN, swap),
                Event.inArena(halt + 1, CueTrigger.ESCORT_JOINED, ""),
                met);
    }

    /** M8 part H (step H6): Level 28's runs ({@link #act4()}'s note). */
    static List<List<Event>> level28Runs() {
        String maw = "abyssal-maw";
        Event arrival = new Event(104, CueTrigger.BOSS_ARRIVAL, "");
        return List.of(
                List.of(),
                List.of(new Event(59.5, CueTrigger.SECRET, "Pelagic-3")),
                List.of(new Event(62, CueTrigger.SECRET, "Pelagic-3")),
                maw(arrival, 25, 45, 60, 93, maw),
                List.of(
                        new Event(59.5, CueTrigger.SECRET, "Pelagic-3"),
                        arrival,
                        Event.inArena(35, CueTrigger.BOSS_PHASE, "Tightening"),
                        Event.inArena(62, CueTrigger.BOSS_DIVE, ""),
                        Event.inArena(80, CueTrigger.BOSS_PHASE, "Implants"),
                        Event.inArena(126.5, CueTrigger.BOSS_DESTROYED, maw)),
                maw(arrival, 50, 85, 110, 172, maw),
                maw(arrival, 3, 4, 9, 40, maw),
                List.of(
                        arrival,
                        Event.inArena(30, CueTrigger.BOSS_PHASE, "Tightening"),
                        Event.inArena(45, CueTrigger.BOSS_DESTROYED, maw)));
    }

    /** A Level 28 run: the arrival, then phase 2, the dive, phase 3 and the kill this many seconds into the halt. */
    private static List<Event> maw(
            Event arrival, double tightening, double dive, double implants, double kill, String maw) {
        return List.of(
                arrival,
                Event.inArena(tightening, CueTrigger.BOSS_PHASE, "Tightening"),
                Event.inArena(dive, CueTrigger.BOSS_DIVE, ""),
                Event.inArena(implants, CueTrigger.BOSS_PHASE, "Implants"),
                Event.inArena(kill, CueTrigger.BOSS_DESTROYED, maw));
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
}
