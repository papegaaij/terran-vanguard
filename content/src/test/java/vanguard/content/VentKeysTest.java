package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.WaveSpec;

/**
 * M8 part G, step G1 (design/campaign Level 27; design/enemies/naval/depth-hunter; m8g-gaps.md §1.1–§1.4): the keys
 * Level 27 and the Depth Hunter will use, loaded, checked ({@link VentChecks}, {@link FoundryChecks}) and mapped for
 * the simulation, on fixtures: a stand-in hunter's stat block and Level 26's data with a plume block and the new waves
 * added (Level 27's own data is step G2's).
 */
class VentKeysTest {
    private static final String LEVEL_KEY = "act-4-deep-water/level-26-blackwater";
    private static final String LEVEL = "campaign/" + LEVEL_KEY + "/data.yaml";
    private static final String HUNTER = "enemies/naval/test-hunter/data.yaml";

    /** A stand-in Depth Hunter (the approved stat block's numbers; its own data.yaml is step G2's). */
    private static final String HUNTER_YAML = """
            name: Test Hunter
            faction: Unmarked
            layer: air
            tier: medium
            size: [72, 40]
            hitbox: [56, 28]
            parts: single
            orientation: 16 angles
            hp: 120
            armour: none
            speed: 80
            machine: true
            movement:
              hover: {seconds: 10, y: [380, 460], drift: 12}
            attacks:
              - pattern: aimed
                name: torpedo
                bullet: heavy
                interval: 3.5
                speed: 90
                first_shot_delay: 0.5
                aim: facing
                homing: {turn_rate: 70, life: 5, hp: 3, most: 8, hatch: 0}
            formations:
              - {name: column, size: [3, 4]}
            weak_points: []
            drops: []
            traits: [spread, homing, rear, anti-sub]
            bounty: 30
            first_level: 26
            difficulty:
              easy: {homing: {turn_rate: 55}, attacks: {torpedo: {interval: 4.5}}}
              hard: {homing: {turn_rate: 85}, attacks: {torpedo: {interval: 2.8}}}
            """;

    private static final String PLUMES = """
            vents:
              look: heat-plume
              shape: disc
              width: 64
              tell: 1.4
              burn: 1.6
              period: 4.5
              ripple: 0.5
              bullet: heavy
              enemy_damage: 40
              bend: 25
              easy: {tell: 1.8, period: 6}
              hard: {tell: 1.0, period: 4}
              rows:
                - {t: 26, xs: [120, 240, 360]}
                - {t: 40, xs: [360, 240]}

            secrets:""";

    private static final String WAVES = """
            waves:
              - {t: 2, formation: column, enemy: test-hunter, count: 3, from: rear}
              - {t: 3, formation: rear ambush, enemy: barbfin, count: 2, from: rear}
              - {t: 4, formation: turret nest, enemy: siren, count: 2, from: front, on_ground: [180, 300]}
            """;

    /** The design tree with the stand-in hunter and Level 26 carrying the plumes and the new waves. */
    private static List<DataFile> files(String plumes, String waves, String hunter) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            if (file.path().equals(LEVEL)) {
                String text = file.text();
                assertTrue(text.contains("\nsecrets:") && text.contains("\nwaves:\n"), LEVEL);
                text = text.replaceFirst("\nsecrets:", java.util.regex.Matcher.quoteReplacement("\n" + plumes));
                text = text.replaceFirst("\nwaves:\n", java.util.regex.Matcher.quoteReplacement("\n" + waves));
                files.add(new DataFile(LEVEL, text));
            } else {
                files.add(file);
            }
        }
        files.add(new DataFile(HUNTER, hunter));
        return files;
    }

    private static Content content() {
        return ContentLoader.load(files(PLUMES, WAVES, HUNTER_YAML));
    }

    @Test
    void thePlumesReachTheSimulationAsDiscsWithTheirBurnBendAndTimingsByDifficulty() {
        Content content = content();
        for (Difficulty difficulty : Difficulty.values()) {
            List<HazardSpec.VentRow> rows =
                    SimSpecs.level(content, LEVEL_KEY, difficulty).hazards().vents();
            assertEquals(2, rows.size());
            HazardSpec.VentRow row = rows.getFirst();
            assertTrue(row.disc());
            assertEquals(64, row.width());
            assertEquals(40, row.enemyDamage());
            assertEquals(Math.toRadians(25), row.bendRadians(), 1e-12);
            assertEquals(10, row.damage(), "`heavy`");
            assertEquals(1.6, row.burnSeconds());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 1.8;
                        case MEDIUM -> 1.4;
                        case HARD -> 1.0;
                    },
                    row.tellSeconds());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 6.0;
                        case MEDIUM -> 4.5;
                        case HARD -> 4.0;
                    },
                    row.periodSeconds());
        }
        // The geysers and the furnace vents stay square, burning no unit and bending nothing.
        HazardSpec.VentRow geyser = SimSpecs.level(content, "act-4-deep-water/level-22-ice-shelf", Difficulty.MEDIUM)
                .hazards()
                .vents()
                .getFirst();
        assertFalse(geyser.disc());
        assertEquals(0, geyser.enemyDamage());
        assertEquals(0, geyser.bendRadians());
    }

    @Test
    void theHuntersTorpedoesLeaveAlongItsFacingAndItDriftsAtItsDepth() {
        Content content = content();
        EnemySpec medium = SimSpecs.enemy(content, "test-hunter", Difficulty.MEDIUM, Optional.empty());
        EnemyGun gun = medium.gun().orElseThrow();
        EnemyGun.HomingSpec homing = gun.homing().orElseThrow();
        assertEquals(Math.toRadians(70), homing.turnRadiansPerSecond(), 1e-12);
        assertEquals(5, homing.lifeSeconds());
        assertEquals(3, homing.hp());
        assertEquals(8, homing.mostAlive());
        assertEquals(10, gun.damage(), "`heavy`");
        assertEquals(3.5, gun.intervalSeconds(), 1e-12);
        EnemySpec.Hover hover = medium.hover().orElseThrow();
        assertEquals(12, hover.drift());
        assertTrue(hover.aboveShip().isEmpty());
        assertEquals(380, hover.depth().min());
        assertEquals(460, hover.depth().max());
        EnemySpec easy = SimSpecs.enemy(content, "test-hunter", Difficulty.EASY, Optional.empty());
        assertEquals(
                Math.toRadians(55),
                easy.gun().orElseThrow().homing().orElseThrow().turnRadiansPerSecond(),
                1e-12);
        assertEquals(4.5, easy.gun().orElseThrow().intervalSeconds(), 1e-12);
        EnemySpec hard = SimSpecs.enemy(content, "test-hunter", Difficulty.HARD, Optional.empty());
        assertEquals(
                Math.toRadians(85),
                hard.gun().orElseThrow().homing().orElseThrow().turnRadiansPerSecond(),
                1e-12);
        assertEquals(2.8, hard.gun().orElseThrow().intervalSeconds(), 1e-12);
    }

    @Test
    void theRearColumnTheRearPairAndTheGroundNestsReachTheSimulation() {
        LevelScript script = SimSpecs.level(content(), LEVEL_KEY, Difficulty.MEDIUM);
        WaveSpec column = wave(script, "test-hunter");
        assertEquals(WaveSpec.Formation.COLUMN, column.formation());
        assertEquals(WaveSpec.Entry.REAR, column.entry());
        WaveSpec pair = script.waves().stream()
                .filter(w -> w.enemy().slug().equals("barbfin") && w.t() == 3)
                .findFirst()
                .orElseThrow();
        assertEquals(WaveSpec.Formation.REAR_AMBUSH, pair.formation());
        WaveSpec nests = wave(script, "siren");
        assertEquals(WaveSpec.Entry.FRONT, nests.entry());
        WaveSpec.Mount mount = nests.mount().orElseThrow();
        assertTrue(mount.ground());
        assertEquals(List.of(180.0, 300.0), mount.xs());
    }

    private static WaveSpec wave(LevelScript script, String slug) {
        return script.waves().stream()
                .filter(w -> w.enemy().slug().equals(slug))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void theChecksRejectKeysWithoutWhatTheyNeed() {
        assertProblem(files(PLUMES.replace("  shape: disc\n", ""), WAVES, HUNTER_YAML), "burn as discs");
        assertProblem(
                files(
                        PLUMES.replace("look: heat-plume", "look: ice-geyser").replace("  shape: disc\n", ""),
                        WAVES,
                        HUNTER_YAML),
                "only a plume's disc");
        assertProblem(files(PLUMES.replace("[360, 240]", "[300, 240]"), WAVES, HUNTER_YAML), "at least 100.0 px apart");
        assertProblem(files(PLUMES, WAVES, HUNTER_YAML.replace("    aim: facing\n", "")), "along its facing");
        assertProblem(
                files(
                        PLUMES,
                        WAVES,
                        HUNTER_YAML.replace(
                                "  hover: {seconds: 10, y: [380, 460], drift: 12}", "  straight: {speed: 80}")),
                "while it holds");
        assertProblem(
                files(
                        PLUMES,
                        WAVES.replace(
                                "enemy: barbfin, count: 2, from: rear", "enemy: test-hunter, count: 2, from: rear"),
                        HUNTER_YAML),
                "holds above the ship");
        assertProblem(
                files(
                        PLUMES,
                        WAVES.replace("formation: column, enemy: test-hunter", "formation: column, enemy: barbfin"),
                        HUNTER_YAML),
                "rises to its hover's y");
        assertProblem(
                files(
                        PLUMES,
                        WAVES.replace(
                                "formation: rear ambush, enemy: barbfin, count: 2, from: rear",
                                "formation: column, enemy: test-hunter, count: 2, from: rear"),
                        HUNTER_YAML),
                "one column from the rear on the screen at a time");
        assertProblem(
                files(PLUMES, WAVES.replace("on_ground: [180, 300]", "on_ground: [180]"), HUNTER_YAML), "its own x");
        assertProblem(
                files(
                        PLUMES,
                        WAVES.replace("count: 2, from: front, on_ground", "count: 2, from: sides, on_ground"),
                        HUNTER_YAML),
                "come in with the ground");
        // Step G3 (G2's finding): a Barbfin pincer without a hold failed in the simulation, not the checks.
        assertProblem(
                files(
                        PLUMES,
                        WAVES + "  - {t: 5, formation: pincer, enemy: barbfin, count: 2, from: sides}\n",
                        HUNTER_YAML),
                "it needs a hold on medium");
    }

    private static void assertProblem(List<DataFile> files, String message) {
        var e = assertThrows(ContentException.class, () -> ContentLoader.load(files));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }
}
