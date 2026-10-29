package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;

/**
 * M9 part E, step E1: Level 32's data keys load and the validator's part-E checks ({@link DeepCoreChecks}): the
 * Sentinel Tower's hardened collar and its sweep's full turn with their hooks, the Strider's legs that topple it and its
 * walk's topple, a terrain's {@code forks} with branch waves, ground targets (a cache's {@code crate}, a charge's {@code
 * blast}) and radio cues, the towers' and locks' placement (QE-1 a) and their passage. Stand-in units sit beside the
 * design tree; Level 16 (its fork mesa) and Level 29 carry the level keys for the test. The sim specs are step E2's.
 */
class DeepCoreKeysTest {
    private static final String SENTINEL = "enemies/ground/test-sentinel/data.yaml";
    private static final String STRIDER = "enemies/ground/test-strider/data.yaml";
    private static final String LEVEL_16 = "campaign/act-3-red-dust/level-16-valles-canyon-run/data.yaml";
    private static final String LEVEL_29 = "campaign/act-5-the-belt/level-29-betrayal-at-ceres/data.yaml";

    /** A stand-in Sentinel Tower: its collar round the 28 px emitter, its sweep all round with a full turn. */
    private static final String SENTINEL_YAML = """
            name: Test Sentinel
            faction: Unmarked
            layer: ground
            tier: medium
            size: [80, 80]
            hitbox: [72, 72]
            parts: single
            orientation: fixed
            hp: 120
            armour: {collar: [28, 28]}
            speed: 0
            machine: true
            movement:
              terrain: {}
            attacks:
              - pattern: laser-sweep
                name: sweep
                bullet: laser
                aim: target
                interval: 4.5
                first_shot_delay: 1.0
                sweep: {arc: 120, duration: 1.6, telegraph: 0.8, length: 220, width: 6, full_turn: {every: 3, duration: 3.0, telegraph: 1.0, slot: 6.0}}
            formations:
              - {name: turret nest, size: [1, 2]}
            weak_points: []
            drops: []
            traits: [anti-ground, beam, piercing]
            bounty: 40
            first_level: 32
            difficulty:
              easy: {sweep_arc: 100, full_turn_every: 0, attacks: {sweep: {interval: 5.5}}}
              hard: {sweep_arc: 160, full_turn_every: 2, attacks: {sweep: {interval: 3.6}}}
            """;

    /** A stand-in Strider: its legs topple it, its pod carries the homing salvo. */
    private static final String STRIDER_YAML = """
            name: Test Strider
            faction: Unmarked
            layer: ground
            tier: large
            size: [120, 120]
            hitbox: [64, 64]
            parts: multi
            part_list:
              - {name: hull, offset: [0, 0], hitbox: [64, 64], hp: 220, kind: vital, bounty: 40}
              - {name: leg-left, offset: [-42, 0], hitbox: [22, 40], hp: 80, kind: destroyable, bounty: 6, first_bonus: 6, topples: true}
              - {name: leg-right, offset: [42, 0], hitbox: [22, 40], hp: 80, kind: destroyable, bounty: 6, first_bonus: 6, topples: true}
              - {name: pod, offset: [-20, 18], hitbox: [24, 24], hp: 30, kind: destroyable, bounty: 8, first_bonus: 8, attack: salvo}
            orientation: 16 angles
            hp: 410
            armour: none
            speed: 32
            machine: true
            movement:
              walk: {speed: 32, turn_rate: 45, stride: 32, topple: {stagger: 0.6, fall: 0.8, line: [48, 140]}}
            attacks:
              - pattern: fan
                name: cannons
                bullet: medium
                interval: 2.4
                speed: 160
                count: 3
                spread: 12
              - pattern: aimed
                name: salvo
                bullet: medium
                interval: 5
                speed: 110
                burst: 2
                burst_gap: 0.25
                homing: {turn_rate: 80, life: 4, hp: 2, most: 8, hatch: 0.4, look: diamond, seekable: true, per_shooter: true, aimed: true}
            formations:
              - {name: convoy, size: [1, 2]}
            weak_points: []
            drops: []
            traits: [anti-ground, piercing, homing]
            bounty: 60
            first_level: 32
            """;

    /** Level 16 with forks at its fork mesa (tip row 12,830: the latest choice at t≈69, the island gone by t≈80). */
    private static final String[] FORKED = {
        LEVEL_16,
        "  contact: {damage: {easy: 6, medium: 10, hard: 10}",
        "  forks: [{island: 0}]\n  contact: {damage: {easy: 6, medium: 10, hard: 10}",
        LEVEL_16,
        "  - {t: 70, formation: snake, enemy: skitter, count: 4, from: front, edge: right, notes:",
        "  - {t: 70, formation: snake, enemy: skitter, count: 3, from: front, branch: {fork: 1, side: left}}\n"
                + "  - {t: 70, formation: snake, enemy: skitter, count: 4, from: front, edge: right, notes:",
        LEVEL_16,
        "  - target: outflow nest\n",
        "  - target: left cache\n"
                + "    section: 3\n"
                + "    layer: ground\n"
                + "    size: [24, 24]\n"
                + "    hp: 20\n"
                + "    bounty: 0\n"
                + "    crate: 50\n"
                + "    sprite: blast-door\n"
                + "    branch: {fork: 1, side: left}\n"
                + "    at: [[72, 100]]\n"
                + "  - target: left charge\n"
                + "    section: 3\n"
                + "    layer: ground\n"
                + "    size: [20, 20]\n"
                + "    hp: 1\n"
                + "    bounty: 0\n"
                + "    blast: {radius: 64, damage: 80}\n"
                + "    branch: {fork: 1, side: left}\n"
                + "    at: [[72.5, 120]]\n"
                + "  - target: outflow nest\n"
    };

    private static List<DataFile> files(String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            for (int i = 0; i + 2 < edits.length; i += 3) {
                if (edits[i].equals(file.path())) {
                    assertTrue(text.contains(edits[i + 1]), file.path() + " has " + edits[i + 1]);
                    text = text.replaceFirst(
                            java.util.regex.Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        files.add(new DataFile(SENTINEL, stand(SENTINEL, SENTINEL_YAML, edits)));
        files.add(new DataFile(STRIDER, stand(STRIDER, STRIDER_YAML, edits)));
        return files;
    }

    private static String stand(String path, String text, String... edits) {
        for (int i = 0; i + 2 < edits.length; i += 3) {
            if (edits[i].equals(path)) {
                assertTrue(text.contains(edits[i + 1]), path + " has " + edits[i + 1]);
                text = text.replace(edits[i + 1], edits[i + 2]);
            }
        }
        return text;
    }

    private static String[] with(String[] base, String... more) {
        String[] all = new String[base.length + more.length];
        System.arraycopy(base, 0, all, 0, base.length);
        System.arraycopy(more, 0, all, base.length, more.length);
        return all;
    }

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
    }

    private static boolean has(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(problem -> problem.contains(text)), text + " in " + problems);
        return true;
    }

    @Test
    void theStandInsLoadWithTheirPartEKeys() {
        Content content = ContentLoader.load(files());
        EnemyData sentinel = content.enemies().get("test-sentinel");
        assertTrue(sentinel.armour().hardened());
        assertEquals(28, sentinel.armour().collar().orElseThrow().width(), 1e-9);
        EnemyData.FullTurn turn =
                sentinel.attacks().getFirst().sweep().orElseThrow().fullTurn().orElseThrow();
        assertEquals(3, turn.every());
        assertEquals(6.0, turn.slot(), 1e-9);
        assertEquals(
                0,
                sentinel.difficulty()
                        .orElseThrow()
                        .easy()
                        .orElseThrow()
                        .fullTurnEvery()
                        .orElseThrow());
        assertEquals(
                2,
                sentinel.difficulty()
                        .orElseThrow()
                        .hard()
                        .orElseThrow()
                        .fullTurnEvery()
                        .orElseThrow());
        EnemyData strider = content.enemies().get("test-strider");
        EnemyData.Topple topple =
                strider.movement().walk().orElseThrow().topple().orElseThrow();
        assertEquals(140, topple.line().height(), 1e-9);
        assertEquals(
                2,
                strider.partList().orElseThrow().stream()
                        .filter(EnemyData.PartData::toppling)
                        .count());
    }

    @Test
    void theForkedLevelLoadsWithItsBranches() {
        Content content = ContentLoader.load(files(FORKED));
        LevelData level = content.levels().values().stream()
                .filter(l -> l.terrain().map(t -> !t.forkList().isEmpty()).orElse(false))
                .findFirst()
                .orElseThrow();
        assertEquals(0, level.terrain().orElseThrow().forkList().getFirst().island());
        assertEquals(
                vanguard.sim.Branch.LEFT,
                level.waves().stream()
                        .filter(w -> w.branch().isPresent())
                        .findFirst()
                        .orElseThrow()
                        .branch()
                        .orElseThrow()
                        .sim()
                        .side());
        LevelData.GroundTarget cache = level.groundTargets().stream()
                .filter(target -> target.crate().isPresent())
                .findFirst()
                .orElseThrow();
        assertEquals(50, cache.crate().orElseThrow());
        assertTrue(
                level.groundTargets().stream().anyMatch(target -> target.blast().isPresent()));
        assertEquals("1:left", new TerrainBlock.Branch(1, "left").sim().subject());
    }

    @Test
    void aBranchComesAfterItsForksLatestChoiceInItsChannel() {
        assertTrue(has(
                problems(with(
                        FORKED,
                        LEVEL_16,
                        "count: 3, from: front, branch: {fork: 1, side: left}}",
                        "count: 3, from: front, branch: {fork: 2, side: left}}")),
                "fork 2 of the terrain's 1 forks"));
        assertTrue(has(problems(with(FORKED, LEVEL_16, "at: [[72, 100]]", "at: [[66, 100]]")), "latest choice"));
        assertTrue(has(problems(with(FORKED, LEVEL_16, "at: [[72, 100]]", "at: [[72, 380]]")), "its left channel"));
        assertTrue(has(problems(with(FORKED, LEVEL_16, "at: [[72, 100]]", "at: [[85, 100]]")), "while its island"));
        assertTrue(has(
                problems(with(FORKED, LEVEL_16, "  forks: [{island: 0}]\n", "")), "fork 1 of the terrain's 0 forks"));
        assertTrue(has(
                problems(with(FORKED, LEVEL_16, "  forks: [{island: 0}]\n", "  forks: [{island: 5}]\n")),
                "one of the islands"));
    }

    @Test
    void aCacheAndAChargeArePlainDestructibles() {
        assertTrue(has(
                problems(with(
                        FORKED,
                        LEVEL_16,
                        "    hp: 1\n    bounty: 0\n    blast:",
                        "    hp: 1\n    bounty: 0\n    crate: 5\n    blast:")),
                "a mining charge (blast) is a plain destructible"));
        assertTrue(has(
                problems(with(
                        FORKED,
                        LEVEL_16,
                        "    hp: 20\n    bounty: 0\n    crate: 50\n",
                        "    hits: 2\n    reveals: relay hut\n    bounty: 0\n    crate: 50\n")),
                "a cache's lock (crate) is a destructible revealing no secret"));
    }

    @Test
    void aTowersEmitterAndALockStand40PxInsideTheField() {
        String[] tower = {
            LEVEL_29,
            "  - target: Helix nests\n",
            "  - target: tower\n    section: 2\n    enemy: test-sentinel\n    at: [[34, 50]]\n  - target: Helix nests\n"
        };
        assertTrue(has(problems(tower), "a collar's emitter lies at least 40 px inside the field"));
        assertTrue(
                has(problems(with(FORKED, LEVEL_16, "at: [[72, 100]]", "at: [[72, 45]]")), "a blast-door lock lies"));
    }

    @Test
    void aSweepingTowerNeverStandsBesideANarrowPassageOrAFork() {
        String[] tower = with(
                FORKED,
                LEVEL_16,
                "  - target: outflow nest\n",
                "  - target: tower\n    section: 3\n    enemy: test-sentinel\n    at: [[68, 100]]\n  - target: outflow nest\n");
        assertTrue(has(problems(tower), "a sweeping tower stands beside a passage"));
    }

    /** Step E3b (QE-3 a): a ground target's {@code full_turn_first} only on a unit whose sweep has full turns. */
    @Test
    void aFirstFullTurnIsAGroundSweepingNestsOnly() {
        List<String> problems = new ArrayList<>();
        try {
            ContentLoader.load(files(
                    LEVEL_16,
                    "  - target: outflow nest\n",
                    "  - target: tower\n    section: 6\n    enemy: test-sentinel\n    full_turn_first: true\n"
                            + "    at: [[68, 240]]\n  - target: outflow nest\n"));
        } catch (ContentException e) {
            problems.addAll(e.problems());
        }
        assertTrue(
                problems.stream().noneMatch(problem -> problem.contains("full_turn_first")),
                "a tower's first full turn: " + problems);
        assertTrue(has(
                problems(
                        LEVEL_16,
                        "    enemy: spine-turret\n    count: 3\n",
                        "    enemy: spine-turret\n    count: 3\n" + "    full_turn_first: true\n"),
                "a first full turn is a ground-sweeping unit's"));
    }

    @Test
    void theUnitsKeysAreChecked() {
        assertTrue(has(
                problems(SENTINEL, "armour: {collar: [28, 28]}", "armour: {collar: [80, 28]}"),
                "inside the unit's hit box"));
        assertTrue(has(
                problems(SENTINEL, "  terrain: {}\n", "  hover: {seconds: 9, y: [90, 150], drift: 20}\n"),
                "a full turn is a ground unit's sweep"));
        assertTrue(has(
                problems(SENTINEL, ", full_turn: {every: 3, duration: 3.0, telegraph: 1.0, slot: 6.0}}", "}"),
                "full_turn_every changes a sweep's full_turn"));
        assertTrue(has(
                problems(STRIDER, ", topple: {stagger: 0.6, fall: 0.8, line: [48, 140]}}", "}"),
                "a walker's topple goes with a part that topples it"));
        assertTrue(has(
                problems(
                        STRIDER,
                        "hp: 30, kind: destroyable, bounty: 8, first_bonus: 8, attack: salvo}",
                        "hp: 30, kind: vital, bounty: 8, first_bonus: 8, topples: true}"),
                "only a destroyable part topples its walker"));
    }

    /**
     * Step E2: the ledge rule (an enemy ground target in a notched terrain stands in a wall's foot band) is a trench's
     * under water: Level 25's Reef Spitters still need their ledges, Level 32's tunnels (dry) take nests on the floor.
     */
    @Test
    void theLedgeRuleIsATrenchsUnderWater() {
        assertTrue(has(
                problems(
                        "campaign/act-4-deep-water/level-25-siren-trench/data.yaml",
                        "at: [[30, 114], [30.7, 116]]",
                        "at: [[30, 180], [30.7, 116]]"),
                "foot band"));
        LevelData deepCore = ContentLoader.load(files()).levels().values().stream()
                .filter(level ->
                        level.terrain().map(t -> !t.forkList().isEmpty()).orElse(false))
                .findFirst()
                .orElseThrow();
        assertFalse(deepCore.underwater());
        assertFalse(deepCore.terrain().orElseThrow().notchList().isEmpty());
    }

    @Test
    void aToppleCrushCueNeedsAToppler_andABranchCueItsFork() {
        String[] crush = {
            LEVEL_29,
            "radio:  # trigger: t or event.",
            "radio:  # trigger: t or event.\n  - {event: topple-crush, speaker: Varga, line: \"Crushed.\"}\n #"
        };
        assertTrue(has(problems(crush), "a topple-crush cue in a level where nothing topples"));
        assertFalse(problems(crush).isEmpty());
    }
}
