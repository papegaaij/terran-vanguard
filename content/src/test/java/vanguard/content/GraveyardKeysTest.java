package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * M11 part D, step D2 (m11d-gaps.md §1 #1–#6, content side): Level 46's keys loaded and checked: the Husk's {@code
 * line_drift} and {@code crack}, the seed casket's {@code contact: false}, a wave's {@code lines} and {@code warn: false},
 * the level's {@code crack_cue}, a set piece's {@code scenery}, a ground target's {@code pinned}, and {@link
 * GraveyardChecks}' rows on Level 46's data with one thing moved: two drift lines too close, a line entering inside the
 * field, fewer lines than Husks, lines on a wave of a unit that does not drift, a crack naming no fan, a descending scenery
 * set piece, a pinned object on the ground.
 */
class GraveyardKeysTest {
    private static final String LEVEL_46 = "campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/data.yaml";
    private static final String HUSK = "enemies/space/husk/data.yaml";

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
    void theKeysLoad() {
        EnemyData husk = CONTENT.enemy("husk");
        assertEquals(new EnemyData.Crack(0.5, 0.5, "shards"), husk.crack().orElseThrow());
        assertEquals(20, husk.movement().lineDrift().orElseThrow().speed().min());
        assertEquals(false, CONTENT.enemy("seed-casket").contact().orElseThrow());
        LevelData level = CONTENT.levels().get(Level46Test.LEVEL);
        assertEquals(144, level.crackCue().orElseThrow().t());
        assertEquals(0.5, level.crackCue().orElseThrow().gapOn(Difficulty.HARD));
        assertEquals(0.6, level.crackCue().orElseThrow().gapOn(Difficulty.EASY));
        assertTrue(level.setPieces().orElseThrow().getFirst().isScenery());
        assertTrue(level.groundTargets().stream()
                .allMatch(target -> target.pinned().orElse(false)));
    }

    @Test
    void twoDriftLinesTooCloseAreReported() {
        assertProblem(
                problems(
                        LEVEL_46,
                        "[-50, 240, 530, 320], [-50, 330, 530, 410]",
                        "[-50, 240, 530, 320], [-50, 300, 530, 380]"),
                "come 59 px close");
    }

    @Test
    void aLineEnteringInsideTheFieldIsReported() {
        assertProblem(problems(LEVEL_46, "[[515, 110, -50, 60]]", "[[400, 110, -50, 60]]"), "enters from outside");
    }

    @Test
    void fewerLinesThanHusksAreReported() {
        assertProblem(
                problems(
                        LEVEL_46,
                        "lines: [[530, 152, -50, 122], [530, 316, -50, 286]]",
                        "lines: [[530, 152, -50, 122]]"),
                "a line for each drifting unit");
    }

    @Test
    void linesOnAWaveThatDoesNotDriftAreReported() {
        assertProblem(
                problems(
                        LEVEL_46,
                        "enemy: chimera, count: 6, from: front}",
                        "enemy: chimera, count: 6, from: front, warn: false}"),
                "only a drifting unit's wave enters unwarned");
    }

    @Test
    void aCrackNamingNoFanIsReported() {
        assertProblem(problems(HUSK, "attack: shards}", "attack: ring}"), "one of the unit's fan attacks");
    }

    @Test
    void aPinnedObjectOnTheGroundIsReported() {
        assertProblem(
                problems(LEVEL_46, "    layer: low-air\n    pinned: true", "    layer: ground\n    pinned: true"),
                "only a destructible or trigger on low-air is pinned");
    }

    @Test
    void aScenerySetPieceOnThePlayPlaneIsReported() {
        assertProblem(
                problems(
                        LEVEL_46,
                        "        layer: high-air\n        heading: 58",
                        "        layer: air\n        heading: 58"),
                "a scenery set piece flies on high-air");
    }
}
