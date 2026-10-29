package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;

/**
 * M9 part F, step F1: Level 33's data keys load and the validator's part-F checks ({@link LeechChecks}): the Void
 * Leech's power-draining latch ({@code drains: power}, {@code emp: throw}, {@code prey_drift}), its drift and chase
 * within ({@code chase.within}, {@code chase.drift}), the chase-speed hook, the rail's {@code most_telegraphing}, a prey
 * wing's hold ({@code prey: {hold, depth}}) and a hunting wave ({@code hunts: prey}). Stand-in units sit beside the
 * design tree; Level 29 carries the wave keys for the test. The sim specs are step F2's.
 */
class LeechKeysTest {
    private static final String LEECH = "enemies/space/test-leech/data.yaml";
    private static final String RAIL = "enemies/ground/test-rail/data.yaml";
    private static final String LEVEL_29 = "campaign/act-5-the-belt/level-29-betrayal-at-ceres/data.yaml";

    /** A stand-in Void Leech at the approved stat block's medium numbers. */
    private static final String LEECH_YAML = """
            name: Test Leech
            faction: Vrell
            layer: air
            tier: small
            size: [32, 32]
            hitbox: [20, 24]
            parts: single
            orientation: 16 angles
            hp: 6
            armour: none
            speed: 50
            movement:
              chase: {speed: 190, turn_rate: 160, seconds: 4, within: 220, drift: 50}
            latch:
              slots: 3
              spots:
                left: [-21, -13]
                right: [20, -13]
                tail: [0, -34]
              damage_factor: 2
              shake: {reversals: 4, within: 1.2}
              stun: 1
              throw: 120
              drains: power
              emp: throw
              prey_drift: 60
            attacks:
              - pattern: drain
            formations:
              - {name: stream}
            weak_points: []
            drops: []
            traits: [homing, side, rear, area]
            bounty: 12
            first_level: 29
            difficulty:
              easy: {latch_slots: 2, chase: {speed: 160}}
              hard: {chase: {speed: 220, turn_rate: 200}}
            """;

    /** A stand-in Rail Bunker: its line with the gate of 2 telegraphs. */
    private static final String RAIL_YAML = """
            name: Test Rail
            faction: Unmarked
            layer: ground
            tier: medium
            size: [72, 72]
            hitbox: [56, 56]
            parts: single
            orientation: 32 angles
            hp: 100
            armour: none
            speed: 0
            machine: true
            movement:
              terrain: {}
            attacks:
              - pattern: laser-line
                name: rail
                bullet: heavy
                interval: 4.5
                first_shot_delay: 1.5
                line: {telegraph: 1.0, live: 0.2, band: 8, hold_within: 100, most_telegraphing: 2}
            formations:
              - {name: turret nest, size: [1, 3]}
            weak_points: []
            drops: []
            traits: [anti-ground]
            bounty: 45
            first_level: 33
            """;

    private static final String V_WING =
            "  - {t: 22, formation: V-wing, enemy: talon, count: 5, from: front, leader: tip, speed: 130, notes:";

    /** Level 29's first V-wing as a prey wing, a hunting leech stream 1 s after it from the left at its height. */
    private static final String[] PREYED = {
        LEVEL_29,
        V_WING,
        "  - {t: 22, formation: V-wing, enemy: talon, count: 5, from: front, leader: tip, speed: 130,"
                + " prey: {hold: 5, depth: [140, 200]}, notes:",
        LEVEL_29,
        "  - t: 25\n    formation: swarm\n    enemy: asteroid-mite",
        "  - {t: 23, formation: stream, enemy: test-leech, count: 3, from: sides, edge: left, warning: 3, interval: 0.5,"
                + " hunts: prey, paths: [[[-40, 170], [520, 170]]]}\n"
                + "  - t: 25\n    formation: swarm\n    enemy: asteroid-mite"
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
        files.add(new DataFile(LEECH, stand(LEECH, LEECH_YAML, edits)));
        files.add(new DataFile(RAIL, stand(RAIL, RAIL_YAML, edits)));
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
    void theStandInsLoadWithTheirPartFKeys() {
        Content content = ContentLoader.load(files());
        EnemyData leech = content.enemies().get("test-leech");
        EnemyData.Latch latch = leech.latch().orElseThrow();
        assertTrue(latch.drainsPower());
        assertTrue(latch.empThrows());
        assertEquals(60, latch.preyDrift().orElseThrow(), 1e-9);
        EnemyData.Chase chase = leech.movement().chase().orElseThrow();
        assertEquals(220, chase.within().orElseThrow(), 1e-9);
        assertEquals(50, chase.drift().orElseThrow(), 1e-9);
        assertEquals(
                160,
                leech.difficulty()
                        .orElseThrow()
                        .easy()
                        .orElseThrow()
                        .chase()
                        .orElseThrow()
                        .speed()
                        .orElseThrow(),
                1e-9);
        assertTrue(leech.attacks().getFirst().rate().isEmpty(), "a power drain has no rate");
        EnemyData rail = content.enemies().get("test-rail");
        assertEquals(
                2,
                rail.attacks()
                        .getFirst()
                        .line()
                        .orElseThrow()
                        .mostTelegraphing()
                        .orElseThrow());
        EnemyData lamprey = content.enemies().get("lamprey");
        assertTrue(!lamprey.latch().orElseThrow().drainsPower()
                && !lamprey.latch().orElseThrow().empThrows());
    }

    @Test
    void aPreyWingAndItsHuntersLoad() {
        Content content = ContentLoader.load(files(PREYED));
        LevelData level = content.levels().values().stream()
                .filter(l -> l.waves().stream().anyMatch(w -> w.prey().isPresent()))
                .findFirst()
                .orElseThrow();
        LevelData.Wave prey = level.waves().stream()
                .filter(w -> w.prey().isPresent())
                .findFirst()
                .orElseThrow();
        assertEquals(5, prey.prey().orElseThrow().hold(), 1e-9);
        assertEquals(200, prey.prey().orElseThrow().depth().max(), 1e-9);
        assertTrue(level.waves().stream()
                .anyMatch(w -> w.hunts().filter("prey"::equals).isPresent()));
    }

    @Test
    void aPowerDrainHasNoRateAndAShieldDrainHasOne() {
        assertTrue(has(problems(LEECH, "  - pattern: drain", "  - pattern: drain\n    rate: 4"), "no rate"));
        assertTrue(has(problems(LEECH, "  drains: power\n", "  drains: shield\n"), "has its rate"));
    }

    @Test
    void theLatchAndChaseKeysAreChecked() {
        assertTrue(has(problems(LEECH, "drains: power", "drains: armour"), "latch.drains"));
        assertTrue(has(problems(LEECH, "emp: throw", "emp: keep"), "latch.emp"));
        assertTrue(has(problems(LEECH, ", within: 220, drift: 50", ", drift: 50"), "drift goes with its within"));
        assertTrue(has(problems(RAIL, "most_telegraphing: 2", "most_telegraphing: 0"), "most_telegraphing"));
    }

    @Test
    void aHuntingWaveFollowsAPreyWingOfAPlainAirUnit() {
        assertTrue(has(
                problems(with(PREYED, LEVEL_29, " prey: {hold: 5, depth: [140, 200]},", "")), "follows a prey wave"));
        assertTrue(has(
                problems(with(PREYED, LEVEL_29, "enemy: test-leech, count: 3", "enemy: skitter, count: 3")),
                "latches on prey"));
        assertTrue(has(problems(with(PREYED, LEVEL_29, "hunts: prey,", "hunts: ship,")), "hunts: prey"));
        assertTrue(has(
                problems(with(
                        PREYED,
                        LEVEL_29,
                        "formation: V-wing, enemy: talon, count: 5, from: front, leader: tip, speed: 130, prey:",
                        "formation: column, enemy: talon, count: 5, from: front, speed: 130, prey:")),
                "holds as a V-wing"));
    }
}
