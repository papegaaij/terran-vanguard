package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import vanguard.sim.EnemySpec;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.StoryPickups;

/**
 * M11 part C, step C2 (m11c-gaps.md §1 #1–#6, content side): Level 45's keys loaded and checked: the spawning organ's
 * spawn keys (its first release, cluster, hold, EMP-proof clock and alternate, the hook {@code alternate_count}), the
 * level's {@code pits} and a wave's {@code pit}, {@code leaving: {bottom_third}}, the story pickup's {@code rise} and
 * {@code box}, the music's timed duck, and {@link ReefChecks}' rows on Level 45's data with one thing moved: a wave
 * naming no pit, a pit serving two waves, a pit off the screen through its tell, a pit under an organ, a crawler's path
 * over a pit, two organs on the screen, a spore cloud over an organ.
 */
class ReefKeysTest {
    private static final String LEVEL_45 = "campaign/act-7-beyond-the-gate/level-45-hive-reef/data.yaml";
    private static final String ORGAN = "enemies/ground/spawning-organ/data.yaml";

    /** The design tree with {@code edits} ([path, from, to]...) applied. */
    private static List<DataFile> files(String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            for (int i = 0; i < edits.length; i += 3) {
                if (edits[i].equals(file.path())) {
                    assertTrue(text.contains(edits[i + 1]), file.path() + " has " + edits[i + 1]);
                    text = text.replaceFirst(Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        return files;
    }

    private static List<String> problems(String... edits) {
        return assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
    }

    private static void assertProblem(List<String> problems, String text) {
        assertTrue(problems.stream().anyMatch(p -> p.contains(text)), "a problem with '" + text + "' in " + problems);
    }

    private static final Content CONTENT = LevelMetrics.CONTENT;

    @Test
    void theLevelsKeysLoad() {
        LevelData level = CONTENT.levels().get(Level45Test.LEVEL);
        assertEquals(14, level.pitList().size());
        assertEquals(new VrellBlock.Pit("P1", 160, 970), level.pitList().getFirst());
        assertEquals(8, level.pitIndex("P9"));
        assertTrue(level.leaving().orElseThrow().bottomThird());
        LevelData.Music.Duck duck = level.music().duck().orElseThrow();
        assertEquals(LevelData.Music.DuckOn.TIME, duck.on());
        assertEquals(Optional.of(126.0), duck.at());
        assertEquals(12, duck.seconds(), 1e-9);
        assertEquals(-6, duck.db(), 1e-9);
        LevelScript script = SimSpecs.level(CONTENT, Level45Test.LEVEL, Difficulty.MEDIUM);
        assertEquals(
                new StoryPickups.Story("call-organ", 2.0, new Hitbox(64, 40)),
                script.drops().story());
        assertEquals(
                StoryPickups.Story.HELIX_CORE,
                SimSpecs.level(CONTENT, Level34Test.LEVEL, Difficulty.MEDIUM)
                        .drops()
                        .story(),
                "Level 34's Helix core as before");
        EnemySpec seedless =
                SimSpecs.level(CONTENT, CONTENT.levelKey(9).orElseThrow(), Difficulty.MEDIUM).groundUnits().stream()
                        .map(vanguard.sim.GroundUnit::enemy)
                        .filter(enemy -> enemy.slug().equals("hive-node"))
                        .findFirst()
                        .orElseThrow();
        EnemySpec.Spawner node = seedless.spawner().orElseThrow();
        assertTrue(
                Double.isNaN(node.firstSeconds()) && !node.cluster() && !node.hold() && !node.empImmune(),
                "the Hive Node's spawn as before");
        assertTrue(node.alternate().isEmpty());
    }

    @Test
    void anOrgansSpawnKeysAreChecked() {
        assertProblem(problems(ORGAN, "first: 1.5", "first: 0.4"), "first: after the telegraph");
        assertProblem(problems(ORGAN, "formation: whirl cluster", "formation: stream"), "formation: only");
        assertProblem(
                problems(ORGAN, "enemy: skitter, count: 2", "enemy: skitters, count: 2"), "spawn.alternate.enemy");
        assertProblem(problems(ORGAN, "alternate_count: 3", "alternate_count: 0"), "alternate_count");
    }

    @Test
    void aPitWaveNamesOneOfItsOwnPits() {
        assertProblem(problems(LEVEL_45, "pit: P1,", "pit: P0,"), "names one of the level's pits");
        assertProblem(problems(LEVEL_45, "pit: P2,", "pit: P1,"), "each pit serves one wave");
    }

    @Test
    void aPitIsOnTheScreenThroughItsTell() {
        // P1's wave at t=12: its pit's centre at the top edge at t=9.7; 500 px further up it is off the screen.
        assertProblem(
                problems(LEVEL_45, "{id: P1, x: 160, at: 970}", "{id: P1, x: 160, at: 1470}"), "through its tell");
    }

    @Test
    void noPitLiesUnderAnOrganNorACrawlersPath() {
        // O3 at t=80, x 150 (its centre row 8040): P5 moved under it.
        assertProblem(
                problems(LEVEL_45, "{id: P5, x: 120, at: 9070}", "{id: P5, x: 150, at: 8040}"),
                "no pit under a ground target");
        // P5 moved onto T1's path (the row 9090 at x 0 to 9590 at x 480).
        assertProblem(
                problems(LEVEL_45, "{id: P5, x: 120, at: 9070}", "{id: P5, x: 120, at: 9215}"), "clear of the pits");
    }

    @Test
    void neverTwoOrgansOnTheScreenNorACloudOverOne() {
        assertProblem(problems(LEVEL_45, "at: [[58, 330]]", "at: [[30, 330]]"), "never two organs on the screen");
        // The t=117 cloud moved from x 120 over organ O4 (x 310, on the screen t=112-118).
        assertProblem(problems(LEVEL_45, "{t: 117, x: 120, radius: 80}", "{t: 115, x: 310, radius: 80}"), "over O4");
    }
}
