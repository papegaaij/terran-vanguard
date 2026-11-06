package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M11 part A, step A2a (m11a-gaps.md §1 #1–#5, content side): Act 7's unit and level keys loaded, checked and mapped for
 * the simulation (A1a's {@code EnemySpec.WindowShield}, the straight {@code LaserLine}, the {@code Hover}'s rise out of
 * the top edge, the {@code Teleport}, the {@code Shadow}; the wave's line aim and {@code LineStagger}; the level's
 * {@code HazardSpec.Current}) on stand-ins where no Act 7 unit has its data yet: a Warden-like shelled hover with a
 * straight and an aimed line, a teleporting skirmisher, a mirroring unit, and a drift level (Level 30's data turned into
 * one: two reversals, a chain, the Wardens, the skirmishers and the mirroring unit), each loaded from data and flown.
 */
class VrellKeysTest {
    private static final double SHIP_X = PlayField.WIDTH / 2.0;

    private static final String WARDEN = "enemies/space/test-warden/data.yaml";
    private static final String SKATER = "enemies/space/test-skater/data.yaml";
    private static final String SERAPH = "enemies/air/test-seraph/data.yaml";
    private static final String LEVEL_30 = "campaign/act-5-the-belt/level-30-minefield/data.yaml";

    /** A stand-in Gate Warden (the approved stat block: 360 HP, the window shield, the line every 4.0 s, 14 s up). */
    private static final String WARDEN_YAML = """
            name: Test Warden
            faction: Vrell
            layer: air
            tier: medium
            size: [76, 84]
            hitbox: [56, 60]
            parts: single
            orientation: fixed
            hp: 360
            armour: none
            speed: 90
            window_shield: {hold: 3.0, regrow: 0.4, iris: 0.15, tick_gap: 0.15}
            movement:
              hover: {seconds: 14, y: [100, 180], exit: up, exit_speed: 90, leaving_tell: 2.0}
            attacks:
              - pattern: laser-line
                name: eye
                bullet: heavy
                interval: 4.0
                first_shot_delay: 0.5
                line: {telegraph: 0.8, live: 1.0, band: 10, hold_within: 0, most_telegraphing: 2, aim: straight}
            formations:
              - {name: line abreast, size: [2, 3]}
            weak_points: []
            drops: []
            traits: [shield-breaker, forward]
            bounty: 40
            first_level: 30
            difficulty:
              easy: {line_aim: straight, attacks: {eye: {interval: 4.5}}}
              hard: {attacks: {eye: {interval: 3.5}}}
            """;

    /** A stand-in Rift Skater (small, 16 HP, its jumps and its burst after each landing). */
    private static final String SKATER_YAML = """
            name: Test Skater
            faction: Vrell
            layer: air
            tier: small
            size: [40, 32]
            hitbox: [26, 20]
            parts: single
            orientation: 16 angles
            hp: 16
            armour: none
            speed: 140
            movement:
              straight: {speed: 140}
              teleport: {fade: 0.2, ring: 0.5, min_distance: 120, jump: [90, 130], angle: [30, 60], first_glide: 1.2, glide: 1.0, jumps: 3, fire_after: 0.25, most_rings: 4}
            attacks:
              - {pattern: aimed, name: burst, bullet: small, interval: 10, speed: 160, burst: 3, burst_gap: 0.15}
            formations:
              - {name: stream, size: [4, 6]}
            weak_points: []
            drops: []
            traits: [spread, homing]
            bounty: 14
            first_level: 30
            difficulty:
              easy: {burst: 2, teleport: {ring: 0.7, jumps: 2}}
              hard: {teleport: {jumps: 4}}
            """;

    /** A stand-in mirroring unit (the Choir Seraph's mirror and spiral, at its hover's station). */
    private static final String SERAPH_YAML = """
            name: Test Seraph
            faction: Vrell
            layer: air
            tier: medium
            size: [72, 76]
            hitbox: [48, 52]
            parts: single
            orientation: fixed
            hp: 90
            armour: none
            speed: 160
            movement:
              hover: {seconds: 8, y: 120}
              mirror: {lag: 0.15, speed: 260, min_dy: 140}
            attacks:
              - {pattern: spiral, name: spiral, bullet: small, interval: 0.333, speed: 120, arms: 2, turn_rate: 120}
            formations:
              - {name: single}
            weak_points: []
            drops: []
            traits: [homing, beam]
            bounty: 35
            first_level: 30
            """;

    /** The drift level's sections: calm, reversal 1 at t=40 (4 s), on, reversal 2 at t=100 (5 s), on to Level 30's end. */
    private static final String SECTIONS = """
            sections:
              - {name: Calm, end: 40, atmosphere: clear, tiles: [lane-space, lane-dust]}
              - {name: Reversal 1, end: 45, speed: -120, ramp: 1, atmosphere: clear, tiles: [lane-space, lane-dust]}
              - {name: Current, end: 100, speed: 120, ramp: 1, atmosphere: clear, tiles: [lane-space, lane-dust]}
              - {name: Reversal 2, end: 106, speed: -120, ramp: 1, atmosphere: clear, tiles: [lane-space, lane-dust]}
              - {name: Out, end: 178, speed: 120, ramp: 1, atmosphere: clear, tiles: [lane-space, lane-dust]}
            """;

    /** The current: +20° t=13–30, −20° t=53–85, calm round each reversal; easy at 0.6 (12°), its reversals 3 s. */
    private static final String CURRENT = """
            current:
              profile: [[0, 0], [10, 0], [13, 20], [30, 20], [33, 0], [50, 0], [53, -20], [85, -20], [88, 0]]
              reversals: [{t: 40, run: 4}, {t: 100, run: 5}]
              easy: {scale: 0.6, run: 3}
            """;

    /** The drift level's waves: Skaters calm and upwind, the Warden pair, a rear wave per reversal, a chain, a Seraph. */
    private static final String WAVES = """
            waves:
              - {t: 3, formation: stream, enemy: test-skater, count: 4, from: front, interval: 0.5}
              - {t: 15, formation: stream, enemy: test-skater, count: 4, from: sides, edge: left, interval: 0.5, warning: 3}
              - {t: 20, formation: line abreast, enemy: test-warden, count: 2, from: front, line: aimed, line_stagger: 1.3, line_order: middle-out, hard: {line_stagger: 1.0}}
              - {t: 41, formation: single, enemy: test-skater, count: 1, from: front, warning: 3}
              - t: 60
                formation: snake
                enemy: coilwyrm
                count: 1
                from: front
                paths: [[[380, -60], [340, 140], [200, 240], [110, 170], [150, 70], [260, 100], [300, 250], [220, 400], [170, 640]]]
              - {t: 70, formation: single, enemy: test-seraph, count: 1, from: front}
              - {t: 101, formation: single, enemy: test-skater, count: 1, from: front, warning: 3}
              - {t: 120, formation: stream, enemy: test-skater, count: 4, from: front, interval: 0.5}
            """;

    /** The design tree with the stand-ins ({@code fixtures}: a stand-in's path → its text instead) and the edits. */
    private static List<DataFile> files(Map<String, String> fixtures, String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.path().equals(LEVEL_30) ? driftLevel(file.text()) : file.text();
            for (int i = 0; i < edits.length; i += 3) {
                if (edits[i].equals(file.path())) {
                    assertTrue(text.contains(edits[i + 1]), file.path() + " has " + edits[i + 1]);
                    text = text.replaceFirst(Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        Map<String, String> standIns = new java.util.LinkedHashMap<>(Map.of(
                WARDEN, WARDEN_YAML,
                SKATER, SKATER_YAML,
                SERAPH, SERAPH_YAML));
        standIns.putAll(fixtures);
        standIns.forEach((path, text) -> {
            String edited = text;
            for (int i = 0; i < edits.length; i += 3) {
                if (edits[i].equals(path)) {
                    assertTrue(edited.contains(edits[i + 1]), path + " has " + edits[i + 1]);
                    edited = edited.replaceFirst(Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(path, edited));
        });
        return files;
    }

    /**
     * Level 30's data as the drift level: its sections, waves, objectives, secrets, pickups, radio and tow replaced
     * (a reversing level holds no tow), the current added; its debris, music and backdrop kept.
     */
    private static String driftLevel(String text) {
        String out = block(text, "sections", SECTIONS);
        out = block(out, "waves", WAVES);
        out = block(out, "tows", "");
        out = block(out, "secrets", "secrets: []\n");
        out = block(out, "pickups", "pickups: []\n");
        out = block(out, "drops", "");
        out = block(out, "radio", "radio:\n  - {t: 1, speaker: Okafor, line: \"Aegis, ride the current.\"}\n");
        out = block(out, "objectives", "objectives:\n  primary: reach-end\n");
        return out + CURRENT;
    }

    /** {@code text} with its top-level {@code key} block (to the next top-level key) replaced by {@code with}. */
    private static String block(String text, String key, String with) {
        Matcher start = Pattern.compile("(?m)^" + key + ":").matcher(text);
        assertTrue(start.find(), "a top-level " + key);
        Matcher next = Pattern.compile("(?m)^[a-z_]+:").matcher(text);
        int end = next.find(start.end()) ? next.start() : text.length();
        return text.substring(0, start.start()) + with + text.substring(end);
    }

    private static final Content STAND_INS = ContentLoader.load(files(Map.of()));

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(Map.of(), edits)))
                .problems();
    }

    private static void assertProblem(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(p -> p.contains(text)), "a problem with '" + text + "' in " + problems);
    }

    private static EnemySpec unit(String slug, Difficulty difficulty) {
        return SimSpecs.enemy(STAND_INS, slug, difficulty, Optional.empty(), 5, 30);
    }

    private static String key() {
        return STAND_INS.levelKey(30).orElseThrow();
    }

    private static LevelScript script(Difficulty difficulty) {
        return SimSpecs.level(STAND_INS, key(), difficulty);
    }

    private static WaveSpec wave(Difficulty difficulty, String slug) {
        return script(difficulty).waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .findFirst()
                .orElseThrow();
    }

    private static Sortie fly(Difficulty difficulty) {
        Loadout loadout = ActThreeArsenalFlightTest.loadout(
                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1));
        return new Sortie(
                1,
                loadout,
                script(difficulty),
                SimSpecs.rules(STAND_INS, key(), difficulty).withInvulnerableShip(),
                loadout.plating().maxArmour());
    }

    // --- the window shield, the line, the hover's exit -------------------------------------------

    @Test
    void theWardensShellLineAndExitTakeTheirNumbersFromTheStatBlock() {
        EnemySpec medium = unit("test-warden", Difficulty.MEDIUM);
        assertEquals(Optional.of(new EnemySpec.WindowShield(3.0, 0.4, 0.15, 0.15)), medium.windowShield());
        EnemySpec.LaserLine line = medium.laserLine().orElseThrow();
        assertTrue(line.straight(), "the stat block's aim: straight");
        assertEquals(0.8, line.telegraphSeconds());
        assertEquals(1.0, line.liveSeconds());
        assertEquals(4.0, line.intervalSeconds(), 1e-9);
        assertEquals(2, line.mostTelegraphing());
        EnemySpec.Hover hover = medium.hover().orElseThrow();
        assertEquals(90, hover.exitUp());
        assertEquals(2.0, hover.leavingTellSeconds());
        assertTrue(medium.sideHover().isEmpty(), "exit: up is not a side hover's exit back");
        assertTrue(
                unit("test-warden", Difficulty.EASY).laserLine().orElseThrow().straight());
        assertEquals(
                3.5,
                unit("test-warden", Difficulty.HARD).laserLine().orElseThrow().intervalSeconds(),
                1e-9);
        // A unit without the keys has none of them.
        EnemySpec skater = unit("test-skater", Difficulty.MEDIUM);
        assertTrue(skater.windowShield().isEmpty());
        assertTrue(skater.shadow().isEmpty());
    }

    /** The wave's {@code line} over the unit's aim (not over easy's hook), its stagger in its order, hard's stagger. */
    @Test
    void aWavesLineAimAndStaggerReachTheSimulation() {
        WaveSpec medium = wave(Difficulty.MEDIUM, "test-warden");
        assertFalse(medium.enemy().laserLine().orElseThrow().straight(), "the wave's line: aimed");
        assertEquals(Optional.of(new WaveSpec.LineStagger(1.3, true)), medium.lineStagger());
        WaveSpec easy = wave(Difficulty.EASY, "test-warden");
        assertTrue(easy.enemy().laserLine().orElseThrow().straight(), "easy: only straight lines (the hook)");
        assertEquals(
                1.0,
                wave(Difficulty.HARD, "test-warden").lineStagger().orElseThrow().seconds(),
                1e-9);
        // A wave without the keys keeps the unit's line and no stagger.
        assertTrue(wave(Difficulty.MEDIUM, "test-skater").lineStagger().isEmpty());
    }

    /** Flown on the range: closed, the front gun's hits do nothing; open (its line's window), they hit. */
    @Test
    void theShelledWardenTakesHitsOnlyWhileItsWindowIsOpen() {
        EnemySpec warden = unit("test-warden", Difficulty.MEDIUM)
                .withHover(Optional.empty())
                .withTerrain(true);
        Sortie sortie = ActThreeArsenalFlightTest.range(
                ActThreeArsenalFlightTest.loadout(
                        new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1)),
                true,
                ActThreeArsenalFlightTest.at(SHIP_X, warden));
        boolean hitOpen = false;
        int opened = 0;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            Optional<Enemy> before = ActThreeArsenalFlightTest.find(sortie, warden);
            boolean closed = before.map(unit -> unit.window().closed()).orElse(true);
            double hp = before.map(Enemy::hp).orElse(0.0);
            sortie.step(Command.FIRE.bit());
            opened += sortie.events().count(SimEvents.Type.SHELL_OPEN);
            Optional<Enemy> after = ActThreeArsenalFlightTest.find(sortie, warden);
            if (before.isPresent() && after.isPresent() && after.get().hp() < hp) {
                assertFalse(closed && after.get().window().closed(), "a closed shell takes no hit");
                hitOpen = true;
            }
        }
        assertTrue(opened > 0, "its windows opened");
        assertTrue(hitOpen, "open, it was hit");
    }

    // --- the teleport and the mirror -------------------------------------------------------------

    @Test
    void theTeleportTakesItsNumbersAndHooks() {
        EnemySpec.Teleport medium =
                unit("test-skater", Difficulty.MEDIUM).teleport().orElseThrow();
        assertEquals(
                new EnemySpec.Teleport(
                        0.2,
                        0.5,
                        120,
                        new vanguard.sim.Range(90, 130),
                        new vanguard.sim.Range(30, 60),
                        1.2,
                        1.0,
                        3,
                        0.25,
                        4),
                medium);
        EnemySpec.Teleport easy =
                unit("test-skater", Difficulty.EASY).teleport().orElseThrow();
        assertEquals(0.7, easy.ringSeconds());
        assertEquals(2, easy.jumps());
        assertEquals(2, unit("test-skater", Difficulty.EASY).gun().orElseThrow().burst());
        assertEquals(
                4, unit("test-skater", Difficulty.HARD).teleport().orElseThrow().jumps());
    }

    @Test
    void theMirrorTakesItsNumbersAndFiresItsSpiral() {
        EnemySpec seraph = unit("test-seraph", Difficulty.MEDIUM);
        assertEquals(Optional.of(new EnemySpec.Shadow(0.15, 260, 140)), seraph.shadow());
        assertTrue(seraph.gun().orElseThrow().spiral().isPresent(), "a spiral, never aimed shots");
        assertTrue(seraph.hover().isPresent(), "it mirrors at its hover's station");
    }

    // --- the drift level -------------------------------------------------------------------------

    @Test
    void theCurrentAndItsReversalsReachTheSimulation() {
        HazardSpec.Current medium =
                script(Difficulty.MEDIUM).hazards().current().orElseThrow();
        assertEquals(9, medium.profile().size());
        assertEquals(new HazardSpec.Bend(13, 20), medium.profile().get(2));
        assertEquals(List.of(new HazardSpec.Reversal(40, 4), new HazardSpec.Reversal(100, 5)), medium.reversals());
        assertEquals(LevelData.Current.TOLD_SECONDS, medium.toldSeconds());
        HazardSpec.Current easy = script(Difficulty.EASY).hazards().current().orElseThrow();
        assertEquals(12, easy.profile().get(2).degrees(), 1e-9, "easy: 0.6 of the angle");
        assertEquals(List.of(new HazardSpec.Reversal(40, 3), new HazardSpec.Reversal(100, 3)), easy.reversals());
        // Easy's reversed sections end after its 3 s runs; medium's after the level's.
        List<Section> mediumSections = script(Difficulty.MEDIUM).sections();
        List<Section> easySections = script(Difficulty.EASY).sections();
        assertEquals(45, mediumSections.get(1).end(), 1e-9);
        assertEquals(44, easySections.get(1).end(), 1e-9);
        assertEquals(106, mediumSections.get(3).end(), 1e-9);
        assertEquals(104, easySections.get(3).end(), 1e-9);
        assertEquals(mediumSections.get(2).end(), easySections.get(2).end(), 1e-9);
        // A level without a current has none.
        assertTrue(SimSpecs.level(STAND_INS, STAND_INS.levelKey(29).orElseThrow(), Difficulty.MEDIUM)
                .hazards()
                .current()
                .isEmpty());
    }

    /**
     * Flown on medium to t=110: both reversals told and run, the drift at +20° in the first swing, the Skaters jumping,
     * the Wardens' aimed lines and leaving tell, the Seraph mirroring.
     */
    @Test
    void theDriftLevelFliesItsCurrentAndItsUnits() {
        Sortie sortie = fly(Difficulty.MEDIUM);
        assertNotNull(sortie.current(), "a level with a current");
        int told = 0;
        int run = 0;
        int jumps = 0;
        int leaving = 0;
        boolean swung = false;
        boolean mirrored = false;
        boolean aimed = false;
        for (int i = 0; i < SimStep.ticks(330) && sortie.levelSeconds() < 110; i++) {
            sortie.step(Command.NONE);
            told += sortie.events().count(SimEvents.Type.REVERSAL_TOLD);
            run += sortie.events().count(SimEvents.Type.REVERSAL_RUN);
            jumps += sortie.events().count(SimEvents.Type.JUMP_OUT);
            leaving += sortie.events().count(SimEvents.Type.LEAVING_TELL);
            if (sortie.levelSeconds() > 20 && sortie.levelSeconds() < 28) {
                swung |= Math.abs(sortie.current().degrees() - 20) < 1e-6;
            }
            for (int e = 0; e < sortie.enemyCount(); e++) {
                Enemy enemy = sortie.enemy(e);
                mirrored |= enemy.shadow().mirroring();
                aimed |= enemy.spec().slug().equals("test-warden")
                        && !enemy.spec().laserLine().orElseThrow().straight();
            }
        }
        assertTrue(sortie.levelSeconds() >= 110, "flown to t=110: " + sortie.levelSeconds());
        assertEquals(2, told, "both reversals told");
        assertEquals(2, run, "both reversals run");
        assertTrue(swung, "the current at +20° in its first swing");
        assertTrue(jumps > 0, "the Skaters jumped");
        assertTrue(leaving > 0, "the Wardens showed their leaving tell");
        assertTrue(aimed, "the wave's aimed lines");
        assertTrue(mirrored, "the Seraph mirrored the ship");
    }

    // --- the checks ------------------------------------------------------------------------------

    @Test
    void theUnitsKeysAreChecked() {
        // A window shield needs a laser line; a mirroring unit fires only a spiral; a teleport is a flying unit's.
        assertProblem(
                problems(
                        WARDEN,
                        "pattern: laser-line",
                        "pattern: aimed",
                        WARDEN,
                        "line: {telegraph: 0.8, live: 1.0, band: 10, hold_within: 0, most_telegraphing: 2, aim: straight}",
                        "speed: 160",
                        WARDEN,
                        "easy: {line_aim: straight, ",
                        "easy: {"),
                "window shield opens with a laser-line");
        assertProblem(
                problems(
                        SERAPH,
                        "{pattern: spiral, name: spiral, bullet: small, interval: 0.333, speed: 120, arms: 2,"
                                + " turn_rate: 120}",
                        "{pattern: aimed, name: spiral, bullet: small, interval: 1.0, speed: 120}"),
                "never aimed shots");
        assertProblem(problems(SKATER, "layer: air", "layer: ground"), "a teleport is a single flying unit's");
        // exit: up from the top only; the leaving tell inside the hold; easy's aim only with a line.
        assertProblem(
                problems(WARDEN, "y: [100, 180], exit: up", "y: [100, 180], edge_x: 60, exit: up"),
                "exit: up ends a hover from the top");
        assertProblem(problems(WARDEN, "leaving_tell: 2.0", "leaving_tell: 14"), "the leaving tell is the last");
        assertProblem(
                problems(SKATER, "easy: {burst: 2,", "easy: {line_aim: straight, burst: 2,"),
                "a line's aim changes a unit with a laser-line");
        assertProblem(problems(WARDEN, "aim: straight}", "aim: sideways}"), "line.aim: straight or aimed");
        assertProblem(problems(SKATER, "fire_after: 0.25", "fire_after: 0.2"), "teleport.fire_after: at least 0.25");
    }

    @Test
    void theDriftLevelsRulesAreChecked() {
        // |θ| ≤ 25° (easy's ≤ 15°), ≤ 10°/s.
        assertProblem(problems(LEVEL_30, "[30, 20], [33, 0]", "[30, 30], [33, 0]"), "at most 25.0°");
        assertProblem(problems(LEVEL_30, "easy: {scale: 0.6", "easy: {scale: 0.9"), "easy's drift turns at most");
        assertProblem(problems(LEVEL_30, "[10, 0], [13, 20]", "[10, 0], [11, 20]"), "at most 10.0°/s");
        // The reversals: held by the sections, ≥ 20 s apart, 3–5 s, the drift calm round them.
        assertProblem(problems(LEVEL_30, "{t: 40, run: 4}", "{t: 40, run: 3}"), "the sections hold it");
        assertProblem(problems(LEVEL_30, "{t: 100, run: 5}", "{t: 100, run: 6}"), "a reversal runs reversed");
        assertProblem(problems(LEVEL_30, "[50, 0], [53, -20]", "[38, 0], [41, -20]"), "the drift is at 0°");
        assertProblem(
                problems(LEVEL_30, "reversals: [{t: 40, run: 4}, ", "reversals: ["),
                "a reversed section in a level with a current is one of its reversals");
        // A side wave upwind; no chain in a reversal's tell; one warned wave a reversal; no ground units.
        assertProblem(
                problems(LEVEL_30, "from: sides, edge: left", "from: sides, edge: right"), "a side wave enters upwind");
        assertProblem(
                problems(LEVEL_30, "  - t: 60\n    formation: snake", "  - t: 38\n    formation: snake"),
                "no segment chain enters");
        assertProblem(
                problems(
                        LEVEL_30,
                        "  - {t: 41, formation: single",
                        "  - {t: 41.5, formation: single, enemy: test-skater, count: 1, from: front, warning: 3}\n"
                                + "  - {t: 41, formation: single"),
                "at most one wave enters during a reversal");
        assertProblem(
                problems(
                        LEVEL_30, "count: 1, from: front, warning: 3}\n  - t: 60", "count: 1, from: front}\n  - t: 60"),
                "a reversal's rear wave is warned");
        assertProblem(
                problems(
                        LEVEL_30,
                        "  - {t: 70, formation: single, enemy: test-seraph",
                        "  - {t: 75, formation: single, enemy: scuttler, count: 1, from: front}\n"
                                + "  - {t: 70, formation: single, enemy: test-seraph"),
                "a level with a current has no ground units");
        // The line's keys belong to laser-line waves.
        assertProblem(
                problems(
                        LEVEL_30,
                        "{t: 3, formation: stream, enemy: test-skater, count: 4, from: front, interval: 0.5}",
                        "{t: 3, formation: stream, enemy: test-skater, count: 4, from: front, interval: 0.5,"
                                + " line: straight}"),
                "a line's aim and stagger belong to a wave of laser-line units");
    }
}
