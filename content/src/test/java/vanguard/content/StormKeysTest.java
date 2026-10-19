package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * M7 part E (step E2): the loader's records and {@link StormChecks} for Level 18 Dust Storm's keys (design/campaign
 * Level 18: the {@code visibility} radius and its ramps, the music's {@code motif}) and its two units' (design/enemies/
 * ground/dust-devil: {@code vortex}, {@code pull}, the {@code while: open} spiral, the armour {@code vanes};
 * design/enemies/air/ghost-drone: {@code lane_strafe}, the pass {@code trigger}, {@code tell}, {@code reaim}, the hard
 * {@code second_pass}, {@code intel.unclassified_until_flown}). Step E4 maps them for the simulation.
 */
class StormKeysTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL_KEY = "act-3-red-dust/level-18-dust-storm";
    private static final String LEVEL = "campaign/act-3-red-dust/level-18-dust-storm/data.yaml";
    private static final String DEVIL = "enemies/ground/dust-devil/data.yaml";
    private static final String DRONE = "enemies/air/ghost-drone/data.yaml";

    private static LevelData level() {
        return CONTENT.level(LEVEL_KEY);
    }

    @Test
    void theStormsRadiusRampsInToTheDeepStormOpensInTheEyeAndClosesForTheBackWall() {
        LevelData.Visibility storm = level().visibility().orElseThrow();
        double full = storm.fullRadius();
        assertEquals(Math.hypot(480, 540) + 120, full, 1e-9, "the field's diagonal plus the falloff");
        assertEquals(120, storm.falloff());
        assertEquals(full, storm.radiusAt(10), "clear plains");
        assertEquals(full, storm.radiusAt(28), "the ramp starts at t=28");
        assertEquals((full + 170) / 2, storm.radiusAt(32), 1e-9, "half way (smoothstep)");
        assertEquals(170, storm.radiusAt(36));
        assertEquals(170, storm.radiusAt(90), "the deep storm");
        assertEquals(full, storm.radiusAt(120), "the eye");
        assertEquals(150, storm.radiusAt(150), "the back wall");
        assertEquals(full, storm.radiusAt(180), "the clearing");
        assertEquals(200, storm.radiusAt(90, Difficulty.EASY));
        assertEquals(180, storm.radiusAt(150, Difficulty.EASY));
        assertEquals(150, storm.radiusAt(90, Difficulty.HARD));
        assertEquals(130, storm.radiusAt(150, Difficulty.HARD), "hard's back wall: the Dust Devil's pull");
        assertTrue(
                CONTENT.level("act-3-red-dust/level-17-dome-siege").visibility().isEmpty());
    }

    @Test
    void theMotifLayerPlaysFromHintOneButNotInTheEye() {
        LevelData.Music music = level().music();
        assertEquals(9, music.track());
        assertEquals(69.5, music.motif().orElseThrow().from());
        assertFalse(music.motifOn(3, 69), "before hint 1");
        assertTrue(music.motifOn(3, 70));
        assertFalse(music.motifOn(4, 120), "the eye");
        assertTrue(music.motifOn(5, 140));
        assertEquals(Map.of(4, false), music.motif().orElseThrow().sections());
        assertFalse(CONTENT.level("act-3-red-dust/level-17-dome-siege").music().motifOn(4, 120));
        assertEquals("mars-storm", music.ambienceChangeList().get(0).ambience());
    }

    @Test
    void theDustDevilsCycleOpensItsVanesAndPullsTheShipAndThePickups() {
        EnemyData devil = CONTENT.enemy("dust-devil");
        EnemyData.Vortex vortex = devil.vortex().orElseThrow();
        assertEquals(2.6, vortex.cycle(), 1e-9);
        assertEquals(0.8, vortex.open());
        assertEquals(0.8, vortex.offset());
        assertEquals(90, vortex.keepAway());
        assertEquals(1.0, devil.vortex(Difficulty.EASY).orElseThrow().open(), "easy's longer window");
        assertEquals(0.8, devil.vortex(Difficulty.HARD).orElseThrow().open());
        assertTrue(devil.armour().vanes());
        assertEquals("ground", devil.layer());

        EnemyData.Pull pull = devil.pull().orElseThrow();
        assertEquals(130, pull.shipRadius());
        assertEquals(35, devil.pull(Difficulty.MEDIUM).orElseThrow().shipSpeed());
        assertEquals(25, devil.pull(Difficulty.EASY).orElseThrow().shipSpeed());
        assertEquals(45, devil.pull(Difficulty.HARD).orElseThrow().shipSpeed());
        assertEquals(170, pull.pickupRadius());
        assertEquals(90, pull.pickupSpeed());

        EnemyData.Attack grit = devil.attack("grit").orElseThrow();
        assertEquals("spiral", grit.pattern());
        assertTrue(grit.whileOpen());
        assertEquals(2, grit.arms().orElseThrow());
        assertEquals(
                3,
                devil.difficulty()
                        .orElseThrow()
                        .hard()
                        .orElseThrow()
                        .attacks()
                        .orElseThrow()
                        .get("grit")
                        .arms()
                        .orElseThrow());
    }

    @Test
    void theGhostDroneStrafesItsLaneAndBurstsOncePerPass() {
        EnemyData drone = CONTENT.enemy("ghost-drone");
        EnemyData.LaneStrafe strafe = drone.movement().laneStrafe().orElseThrow();
        assertEquals(180, strafe.descentSpeed());
        assertEquals(150, strafe.strafeSpeed());
        assertEquals(120, strafe.laneDepth().min());
        assertEquals(220, strafe.laneDepth().max());
        assertEquals(40, strafe.laneSpacing());

        EnemyData.Attack burst = drone.attack("burst").orElseThrow();
        assertEquals(60, burst.passWithinX());
        assertEquals(0.3, burst.tell().orElseThrow());
        assertTrue(burst.reaim().orElseThrow());
        assertEquals(3, burst.burst().orElseThrow());
        assertTrue(burst.interval().isEmpty(), "once per pass");
        assertTrue(Double.isNaN(CONTENT.enemy("needler").attacks().get(0).passWithinX()));
        assertTrue(drone.difficulty()
                .orElseThrow()
                .hard()
                .orElseThrow()
                .secondPass()
                .orElseThrow());
        assertTrue(drone.isMachine());
        assertTrue(drone.unclassifiedUntilFlown());
        assertFalse(CONTENT.enemy("dust-devil").unclassifiedUntilFlown());
    }

    @Test
    void levelEighteenFliesTheAnchorWavesAndJoinsTheSmallActHp() {
        List<LevelData.Wave> waves = level().waves();
        for (double t : new double[] {41.5, 77, 116.5, 138.5}) {
            assertTrue(waves.stream()
                    .anyMatch(w -> w.t() == t && w.groupList().get(0).enemy().equals("dust-devil")));
        }
        for (double t : new double[] {69.5, 87.5, 131.5, 145.5, 166.5}) {
            assertTrue(waves.stream()
                    .anyMatch(w -> w.t() == t && w.groupList().get(0).enemy().equals("ghost-drone")));
        }
        assertTrue(CONTENT.enemyBasis().smallActHp().applies("needler", 18));
        assertEquals(
                0.65, level().objectives().secondary().orElseThrow().killRatio().orElseThrow());
    }

    @Test
    void aRadiusInsideAPullOnTheShipIsRejected() {
        assertProblem(
                replace(LEVEL, "hard: {deep: 150, wall: 130}", "hard: {deep: 150, wall: 120}"), "pull on the ship");
        assertProblem(
                replace(LEVEL, "    - {from: 170, to: 176, radius: full}", "    - {from: 170, to: 190, radius: full}"),
                "inside the level");
        assertProblem(
                replace(LEVEL, "radius: deep}    # into the front", "radius: thick}    # into the front"), "radii");
    }

    @Test
    void aMotifOutsideThePlayedSectionsIsRejected() {
        assertProblem(replace(LEVEL, "motif: {from: 69.5,", "motif: {from: 10,"), "motif");
        assertProblem(replace(LEVEL, "sections: {4: false}}", "sections: {9: false}}"), "sections");
    }

    @Test
    void theStormUnitsKeysNeedWhatTheyChange() {
        assertProblem(replace(DEVIL, "armour: vanes ", "armour: none "), "vanes");
        assertProblem(replace(DEVIL, "    while: open\n", "\n"), "while: open");
        assertProblem(
                replace(DEVIL, "layer: ground     # while closed", "layer: air     # while closed"), "ground unit");
        assertProblem(replace(DRONE, "  lane_strafe:\n", "  lane_strafe_off:\n"), "lane_strafe_off");
        assertProblem(replace(DRONE, "    reaim: true ", "    reaim: true\n    interval: 2 "), "once per pass");
        assertProblem(replace(DRONE, "first_level: 18", "first_level: 17"), "flies no 'ghost-drone'");
    }

    private static void assertProblem(List<DataFile> files, String message) {
        var e = assertThrows(ContentException.class, () -> ContentLoader.load(files));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    private static List<DataFile> replace(String path, String from, String to) {
        assertTrue(DesignTree.dataFiles().stream().anyMatch(f -> f.path().equals(path)), path);
        return DesignTree.dataFiles().stream()
                .map(f -> {
                    if (!f.path().equals(path)) {
                        return f;
                    }
                    assertTrue(f.text().contains(from), path + " has " + from);
                    return new DataFile(path, f.text().replace(from, to));
                })
                .toList();
    }
}
