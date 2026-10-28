package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;

/**
 * M9 part C, step C1: Level 30's data keys load and the validator's part-C checks ({@link MinefieldChecks}): the
 * Minelayer's line mines and lay path, the Gilded Gunship's facing armour and Dart streams, the Rail Serpent's turret
 * cars and wreck, a wave's {@code mine_lines} and a level's {@code ground: none}. Stand-in units (the approved numbers)
 * sit beside the design tree; the sim specs are step C2's.
 */
class MinefieldKeysTest {
    private static final String LAYER = "enemies/space/test-layer/data.yaml";
    private static final String GUNSHIP = "enemies/air/test-plated/data.yaml";
    private static final String DART = "enemies/air/test-dart/data.yaml";
    private static final String EEL = "enemies/naval/eel-swarm/data.yaml";
    private static final String SCUTTLER = "enemies/ground/scuttler/data.yaml";
    private static final String LEVEL_01 = "campaign/act-1-first-contact/level-01-break-at-dawn/data.yaml";
    private static final String LEVEL_29 = "campaign/act-5-the-belt/level-29-betrayal-at-ceres/data.yaml";

    /** A stand-in Minelayer: up its lane from behind, laying three clusters of 4 with one 112 px gap. */
    private static final String LAYER_YAML = """
            name: Test Layer
            faction: Unmarked
            layer: air
            tier: medium
            size: [88, 64]
            hitbox: [64, 44]
            parts: single
            orientation: 16 angles
            hp: 110
            armour: none
            speed: 120
            machine: true
            movement:
              lay: {rise: 120, strafe: 150, lane: 70}
            attacks:
              - pattern: mine
                bullet: medium
                interval: 1
                speed: 100
                mine:
                  arm: 1.0
                  life: 30
                  drift: 0
                  hp: 1
                  ring: 8
                  ring_bullet: small
                  credits: 0
                  proximity: 48
                  blink: 0.6
                  blast: {radius: 40, bullet: heavy}
                  chain: 40
                  hop: 0.08
                  line: {clusters: 3, per_cluster: 4, spacing: 32, cluster_gap: 56, gap: 112, most: 40}
            formations:
              - {name: column, size: [2, 3]}
            weak_points: []
            drops: []
            traits: [rear, homing]
            bounty: 26
            first_level: 30
            """;

    /** A stand-in Dart Drone: launched, it chases at 60°/s for 3 s. */
    private static final String DART_YAML = """
            name: Test Dart
            faction: Unmarked
            layer: air
            tier: tiny
            size: [20, 20]
            hitbox: [12, 12]
            parts: single
            orientation: 16 angles
            hp: 1
            armour: none
            speed: 240
            machine: true
            movement:
              chase: {speed: 240, turn_rate: 60, seconds: 3}
            attacks: []
            formations:
              - {name: stream}
            weak_points: []
            drops: []
            traits: [spread]
            bounty: 2
            first_level: 30
            """;

    /** A stand-in Gilded Gunship: its arcs, its ring and its Dart streams. */
    private static final String GUNSHIP_YAML = """
            name: Test Plated
            faction: Unmarked
            layer: air
            tier: medium
            size: [88, 100]
            hitbox: [64, 76]
            parts: single
            orientation: fixed
            hp: 88
            armour: {front_arc: 45, factor: 0.5}
            rear_plate: {arc: 60, multiplier: 2}
            shield: 12
            speed: 45
            machine: true
            movement:
              drift: {speed: 45}
              sine: {amplitude: 20, period: 4}
            attacks:
              - pattern: ring
                bullet: small
                interval: 3.0
                speed: 110
                count: 12
                tell: 0.4
              - pattern: spawn
                spawn:
                  enemy: test-dart
                  count: 3
                  every: 5
                  telegraph: 0
                  arc: 60
                  speed: 240
                  stream: {gap: 0.25, first: 1, straight: 0.4, most: 6}
            formations:
              - {name: column, size: [1, 2]}
            weak_points: []
            drops: []
            traits: [rear, homing]
            bounty: 28
            first_level: 30
            """;

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
        files.add(new DataFile(LAYER, stand(LAYER, LAYER_YAML, edits)));
        files.add(new DataFile(DART, DART_YAML));
        files.add(new DataFile(GUNSHIP, stand(GUNSHIP, GUNSHIP_YAML, edits)));
        return files;
    }

    /** A stand-in's text with the edits that name it. */
    private static String stand(String path, String text, String... edits) {
        for (int i = 0; i + 2 < edits.length; i += 3) {
            if (edits[i].equals(path)) {
                assertTrue(text.contains(edits[i + 1]), path + " has " + edits[i + 1]);
                text = text.replace(edits[i + 1], edits[i + 2]);
            }
        }
        return text;
    }

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
    }

    /** Whether one of {@code problems} says {@code text} (failing with the list when none does). */
    private static boolean has(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(problem -> problem.contains(text)), text + " in " + problems);
        return true;
    }

    @Test
    void theStandInsLoadWithTheirPartCKeys() {
        Content content = ContentLoader.load(files());
        EnemyData layer = content.enemies().get("test-layer");
        EnemyData.Mine mine = layer.attacks().getFirst().mine().orElseThrow();
        assertTrue(mine.lineMine());
        assertEquals(40, mine.blast().orElseThrow().radius(), 1e-9);
        assertEquals(112, mine.line().orElseThrow().gap(), 1e-9);
        assertEquals(70, layer.movement().lay().orElseThrow().lane(), 1e-9);
        EnemyData gunship = content.enemies().get("test-plated");
        assertTrue(gunship.armour().facing());
        assertEquals(0.5, gunship.armour().factor().orElseThrow(), 1e-9);
        EnemyData.Stream stream =
                gunship.attacks().get(1).spawn().orElseThrow().stream().orElseThrow();
        assertEquals(6, stream.most());
    }

    @Test
    void aLineMineHoldsItsPlaceAndItsLayerHasALayPath() {
        assertTrue(has(problems(LAYER, "drift: 0", "drift: 20"), "a line mine holds its place: drift 0"));
        assertTrue(has(
                problems(LAYER, "  lay: {rise: 120, strafe: 150, lane: 70}", "  drift: {speed: 45}"),
                "movement.lay: a mine layer's lay path goes with its mine attack's line"));
        assertTrue(has(problems(LAYER, "bullet: heavy}", "bullet: hevy}"), "unknown bullet class 'hevy'"));
        assertTrue(has(problems(LAYER, "\n      blink: 0.6", ""), "proximity, blink and hop together"));
    }

    @Test
    void facingArmourIsAFlyersAndItsRearPlateGoesWithIt() {
        assertTrue(has(
                problems(SCUTTLER, "  front_arc: 45", "  front_arc: 45\n  factor: 0.5"),
                "armour.factor: facing armour is a flying unit's"));
        assertTrue(has(
                problems(GUNSHIP, "armour: {front_arc: 45, factor: 0.5}", "armour: none"),
                "rear_plate: a rear plate is a walker's (movement walk) or goes with facing armour"));
        assertTrue(has(
                problems(GUNSHIP, "armour: {front_arc: 45, factor: 0.5}", "armour: {factor: 0.5}"),
                "an armour factor goes with its front_arc"));
    }

    @Test
    void aStreamIsPeriodicAndLaunchesAPlainAirUnit() {
        assertTrue(has(
                problems(GUNSHIP, "enemy: test-dart", "enemy: test-layer\n      shut_within: 10"),
                "a stream launcher has an every and no shut_within or ram_after"));
        assertTrue(
                has(problems(GUNSHIP, "enemy: test-dart", "enemy: eel-swarm"), "a stream launches a plain air unit"));
    }

    @Test
    void aTrainsTurretCarsFireANamedAttackAndItsCutWrecks() {
        String cut = "  cut: {scatter: 70, seconds: 3}";
        String wreck = "  cut: {scatter: 68, seconds: 1.5, wreck: true}\n"
                + "  cars: {every: 3, stagger: 0.6, hp: 24, bounty: 5, attack: head-aimed}";
        // The Eel Swarm as a train: it loads with its cars and its wreck.
        // Step C2: its 4 turret cars carry their own HP and bounty (the parts' sums: 301 HP, 68 credits).
        Content content =
                ContentLoader.load(files(EEL, cut, wreck, EEL, "hp: 245", "hp: 301", EEL, "bounty: 56", "bounty: 68"));
        EnemyData.SegmentChain chain =
                content.enemies().get("eel-swarm").segmentChain().orElseThrow();
        assertTrue(chain.cut().orElseThrow().wrecks());
        assertEquals(3, chain.cars().orElseThrow().every());
        assertTrue(has(
                problems(EEL, cut, wreck.replace("attack: head-aimed", "attack: rail-gun")),
                "segment_chain.cars.attack: a turret car fires one of the unit's attacks by name"));
        assertTrue(has(
                problems(EEL, cut, "  cut: {scatter: 68, seconds: 1.5, wreck: false}"),
                "wreck: true, with its scatter"));
    }

    @Test
    void groundNoneOnlyFromAct5WithNothingOnTheGround() {
        List<String> early = problems(LEVEL_01, "bounty_scale:", "ground: none\nbounty_scale:");
        assertTrue(has(early, "ground: ground: none only from Act 5"));
        assertTrue(has(early, "a level without a ground layer has no ground targets"));
        List<String> ceres = problems(LEVEL_29, "bounty_scale:", "ground: none\nbounty_scale:");
        assertTrue(has(ceres, "a level without a ground layer has no ground targets"));
        assertTrue(has(problems(LEVEL_29, "bounty_scale:", "ground: some\nbounty_scale:"), "ground: only 'none'"));
    }

    @Test
    void mineLayersComeFromTheRearAndOnlyTheyLayLines() {
        List<String> found = problems(
                LEVEL_29,
                "waves:\n",
                "waves:\n  - {t: 5, formation: column, enemy: test-layer, count: 1, from: front}\n"
                        + "  - {t: 6, formation: snake, enemy: skitter, count: 3, from: front,"
                        + " mine_lines: {rows: [100], gaps: [160]}}\n");
        assertTrue(has(found, "mine layers come from the rear: test-layer"), found.toString());
        assertTrue(has(found, "mine_lines: only a mine layers' wave lays mine lines: skitter"), found.toString());
    }
}
