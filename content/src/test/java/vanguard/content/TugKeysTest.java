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
 * M9 part D, step D1: Level 31's data keys load and the validator's part-D checks ({@link TugChecks}, the homing rule in
 * {@link FoundryChecks}): the mining tug's {@code follows: hull} and sections, a level's {@code escort.hull} with its
 * hook and unhooked waves, the Hornet's aimed homing salvo, the Shard Drone's {@code air-link} and the Buzzsaw's
 * {@code dash}. Stand-in units and a stand-in tug (the approved numbers) sit beside the design tree; Level 29 carries the
 * escort for the test. The sim specs are step D2's.
 */
class TugKeysTest {
    private static final String ALLIES = "allies/data.yaml";
    private static final String HORNET = "enemies/air/test-hornet/data.yaml";
    private static final String SHARD = "enemies/space/test-shard/data.yaml";
    private static final String SAW = "enemies/air/test-saw/data.yaml";
    private static final String LEVEL_29 = "campaign/act-5-the-belt/level-29-betrayal-at-ceres/data.yaml";

    /** A stand-in mining tug: 200×300 on the ground layer, its three sections (y down from the centre). */
    private static final String TUG_YAML = """
            test-tug:
              name: Test tug
              layer: ground
              size: [200, 300]
              hitbox: [200, 300]
              hp: 560
              damaged_by: {objective_aimed: false, claws: 0, missiles: 5, dives: 3, links: 4, link_cooldown: 0.5}
              follows: hull
              headings: {count: 1, step: 10}
              smoke_below: 0.5
              sections:
                - {name: bow, offset: [0, -108], box: [130, 76], smoke: [[-30, -110], [35, -100]]}
                - {name: cargo, offset: [0, -4], box: [180, 110]}
                - {name: engines, offset: [0, 103], box: [134, 84]}
            """;

    /** A stand-in Hornet: holding at the top, its aimed homing salvo of 4. */
    private static final String HORNET_YAML = """
            name: Test Hornet
            faction: Unmarked
            layer: air
            tier: medium
            size: [72, 64]
            hitbox: [52, 44]
            parts: single
            orientation: fixed
            hp: 96
            armour: none
            shield: 8
            speed: 140
            machine: true
            movement:
              hover: {seconds: 9, y: [90, 150], drift: 20}
            attacks:
              - pattern: aimed
                name: salvo
                bullet: medium
                interval: 4.4
                speed: 110
                first_shot_delay: 1.5
                burst: 4
                burst_gap: 0.2
                homing: {turn_rate: 80, life: 4, hp: 2, most: 8, hatch: 0.5, look: diamond, seekable: true, per_shooter: true, aimed: true}
            formations:
              - {name: line abreast, size: [2, 3]}
            weak_points: []
            drops: []
            traits: [forward, piercing, spread, homing]
            bounty: 26
            first_level: 31
            """;

    /** A stand-in Shard Drone: its energy links, 120 px apart in a line, a ring's centre descending. */
    private static final String SHARD_YAML = """
            name: Test Shard
            faction: Unmarked
            layer: air
            tier: small
            size: [40, 40]
            hitbox: [26, 26]
            parts: single
            orientation: radial
            hp: 28
            armour: none
            speed: 45
            machine: true
            movement:
              sine: {amplitude: 30, period: 5}
              orbit: {radius: 64, turn_rate: 60}
            attacks:
              - pattern: air-link
                bullet: medium
                air_link: {band: 6, width: 8, cooldown: 0.5, tell: 0.8, spacing: 120, ring: {descend: 40, follow: 10}}
              - pattern: aimed
                name: bolt
                bullet: small
                interval: 3
                speed: 150
            difficulty:
              hard: {air_link: {tell: 0.5}}
            formations:
              - {name: line abreast, size: [3, 4]}
              - {name: circle, size: [4, 4]}
            weak_points: []
            drops: []
            traits: [piercing, spread, forward]
            bounty: 14
            first_level: 31
            """;

    /** A stand-in Buzzsaw Drone: its dash cycle, contact only. */
    private static final String SAW_YAML = """
            name: Test Saw
            faction: Unmarked
            layer: air
            tier: small
            size: [36, 36]
            hitbox: [26, 26]
            parts: single
            orientation: radial
            hp: 20
            armour: none
            speed: 120
            machine: true
            movement:
              dash: {turn_rate: 120, chase: 2.5, tell: 0.6, tell_speed: 40, speed: 360, bounces: 3, slow: 0.5, dashes: 2}
            attacks: []
            difficulty:
              easy: {dash: {tell: 0.8, speed: 300, bounces: 2}}
              hard: {dash: {tell: 0.5, speed: 400, dashes: 3}}
            formations:
              - {name: stream, size: [3, 6]}
            weak_points: []
            drops: []
            traits: [spread, area, forward, piercing]
            bounty: 12
            first_level: 31
            """;

    /** Level 29 escorting the stand-in tug (its objectives replaced), with a Hornet pair, a Shard line and saws. */
    private static final String[] ESCORTED = {
        LEVEL_29,
        "  primary: reach-end\n",
        "  primary: escort\n"
                + "  escort:\n"
                + "    ally: test-tug\n"
                + "    credits: 40\n"
                + "    hull:\n"
                + "      station: {at: [240, 380], sway: [10, 2.5], period: 14, phase: 0}\n"
                + "      enter: 6\n"
                + "      climb: {t: 160, seconds: 6}\n"
                + "      hp: [240, 160, 160]\n"
                + "      targeted_by: {test-hornet: always, test-shard: always}\n"
                + "      unhooked: [1.5]\n"
                + "      easy: {hp: [320, 220, 220], targeted_by: {test-shard: none}}\n",
        LEVEL_29,
        "waves:\n",
        "waves:\n  - {t: 1, formation: line abreast, enemy: test-hornet, count: 2, from: front}\n"
                + "  - {t: 1.5, formation: line abreast, enemy: test-shard, count: 4, from: front}\n"
                + "  - {t: 2, formation: stream, enemy: test-saw, count: 3, from: sides, edge: left, interval: 0.6,"
                + " warning: 3}\n"
    };

    private static List<DataFile> files(String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            if (file.path().equals(ALLIES)) {
                text = text + "\n" + TUG_YAML;
            }
            for (int i = 0; i + 2 < edits.length; i += 3) {
                if (edits[i].equals(file.path())) {
                    assertTrue(text.contains(edits[i + 1]), file.path() + " has " + edits[i + 1]);
                    text = text.replaceFirst(
                            java.util.regex.Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        files.add(new DataFile(HORNET, stand(HORNET, HORNET_YAML, edits)));
        files.add(new DataFile(SHARD, stand(SHARD, SHARD_YAML, edits)));
        files.add(new DataFile(SAW, stand(SAW, SAW_YAML, edits)));
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
    void theStandInsLoadWithTheirPartDKeys() {
        Content content = ContentLoader.load(files());
        AlliesData.Ally tug = content.allies().allies().get("test-tug");
        assertTrue(tug.hull());
        assertEquals(3, tug.sections().orElseThrow().size());
        assertEquals("engines", tug.sections().orElseThrow().get(2).name());
        assertEquals(103, tug.sections().orElseThrow().get(2).offset().y(), 1e-9);
        assertEquals(5, tug.damagedBy().missiles().orElseThrow(), 1e-9);
        assertEquals(0.5, tug.damagedBy().linkCooldown().orElseThrow(), 1e-9);
        EnemyData hornet = content.enemies().get("test-hornet");
        assertTrue(hornet.attacks().getFirst().homing().orElseThrow().aimed().orElseThrow());
        EnemyData shard = content.enemies().get("test-shard");
        EnemyData.AirLink link = shard.attacks().getFirst().airLink().orElseThrow();
        assertEquals(120, link.spacing(), 1e-9);
        assertEquals(40, link.ring().orElseThrow().descend(), 1e-9);
        EnemyData saw = content.enemies().get("test-saw");
        assertEquals(360, saw.movement().dash().orElseThrow().speed(), 1e-9);
        assertEquals(
                3,
                saw.difficulty()
                        .orElseThrow()
                        .hard()
                        .orElseThrow()
                        .dash()
                        .orElseThrow()
                        .dashes()
                        .orElseThrow());
    }

    @Test
    void theEscortedLevelLoadsWithItsHull() {
        List<String> found = problems(ESCORTED);
        // Level 29's SAM nests stand where the hull passes: the only problems the escort brings.
        assertTrue(has(found, "passes under the hull's swayed footprint"));
        assertFalse(found.stream().anyMatch(p -> p.contains("objectives.escort")), found.toString());
        for (String problem : found) {
            assertTrue(problem.contains("ground_targets"), problem);
        }
    }

    @Test
    void aHullEscortIsCheckedAgainstItsAllyAndItsLevel() {
        assertTrue(has(problems(with(ESCORTED, LEVEL_29, "hp: [240, 160, 160]", "hp: [240, 160]")), "one per section"));
        assertTrue(has(
                problems(with(ESCORTED, LEVEL_29, "test-shard: always}", "test-shard: always, skitter: always}")),
                "a hooked skitter cannot hurt the hull"));
        assertTrue(has(
                problems(with(ESCORTED, LEVEL_29, "test-shard: always}", "test-shard: always, test-saw-x: always}")),
                "unknown enemy 'test-saw-x'"));
        assertTrue(has(
                problems(with(ESCORTED, LEVEL_29, "unhooked: [1.5]", "unhooked: [3]")),
                "no wave of a targeted_by kind"));
        assertTrue(has(
                problems(with(
                        ESCORTED,
                        LEVEL_29,
                        "targeted_by: {test-hornet: always,",
                        "targeted_by: {test-hornet: nearest,")),
                "a hull's hook is always or none"));
        assertTrue(has(
                problems(with(ESCORTED, LEVEL_29, "sway: [10, 2.5]", "sway: [150, 2.5]")),
                "the hull swings off the play field's sides"));
        assertTrue(has(
                problems(with(ESCORTED, LEVEL_29, "    ally: test-tug\n", "    ally: civilian-crawler\n")),
                "a hull block needs an ally that follows hull"));
    }

    @Test
    void aHullAllyHasItsSectionsInsideItAndOnlyHookedHurts() {
        assertTrue(has(
                problems(
                        ALLIES,
                        "{name: cargo, offset: [0, -4], box: [180, 110]}",
                        "{name: cargo, offset: [0, -4], box: [220, 110]}"),
                "cargo's box lies inside the hull's size"));
        assertTrue(has(
                problems(
                        ALLIES,
                        "objective_aimed: false, claws: 0, missiles: 5",
                        "objective_aimed: true, claws: 0, missiles: 5"),
                "a hull is hurt only by hooked missiles, dives and links"));
        assertTrue(has(
                problems(ALLIES, "links: 4, link_cooldown: 0.5", "links: 4"), "links and link_cooldown go together"));
    }

    @Test
    void aDashKeepsItsTellAndIsAPlainContactUnit() {
        assertTrue(has(problems(SAW, "tell: 0.6, tell_speed", "tell: 0.4, tell_speed"), "the tell is the rule"));
        assertTrue(has(problems(SAW, "hard: {dash: {tell: 0.5,", "hard: {dash: {tell: 0.3,"), "the tell is the rule"));
        assertTrue(has(
                problems(SAW, "attacks: []", "attacks:\n  - {pattern: aimed, bullet: small, interval: 2, speed: 150}"),
                "a dashing unit has no attack"));
        assertTrue(has(
                problems(SAW, "  dash: {", "  chase: {speed: 120, turn_rate: 60, seconds: 2}\n  dash: {"),
                "a dash is its only steering"));
    }

    @Test
    void anAirLinkedWaveIsALineOrACircleOfTwoToFour() {
        assertTrue(has(
                problems(with(
                        ESCORTED,
                        LEVEL_29,
                        "{t: 1.5, formation: line abreast, enemy: test-shard, count: 4",
                        "{t: 1.5, formation: snake, enemy: test-shard, count: 4")),
                "a linked group flies a line abreast or a circle"));
        assertTrue(has(
                problems(with(ESCORTED, LEVEL_29, "enemy: test-shard, count: 4", "enemy: test-shard, count: 5")),
                "a linked group has 2 to 4 units"));
        assertTrue(has(problems(SHARD, "    air_link: {band: 6", "    air_link: {band: 0"), "band"));
        assertTrue(has(
                problems(
                        SHARD,
                        "  hard: {air_link: {tell: 0.5}}",
                        "  hard: {air_link: {tell: 0.5}}\n  easy: {dash: {tell: 0.8}}"),
                "a dash change without a dash"));
    }

    @Test
    void anAimedHomingSalvoIsAUnitsOneAttack() {
        assertTrue(has(
                problems(HORNET, "per_shooter: true, aimed: true}", "per_shooter: true, aimed: true}\n    aim: facing"),
                "an aimed launcher is a unit's, launching at its target"));
        assertTrue(has(
                problems(HORNET, ", aimed: true}", "}"),
                "only a boss's parts, a unit's one aimed attack along its facing"));
    }
}
