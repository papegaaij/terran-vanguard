package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

/**
 * M9 part H, step H2 (design/campaign Level 35, m9h-gaps.md §1 #11 and §4): content's checks of Level 35's data, each
 * with a failing sample: the mass relay's quiet window (no wave entering 1.3 s before its telegraph to 2.5 s after its
 * flash), no line-firing unit on the field at the flash on any difficulty, nothing on the ground still on the field when
 * a ringed boss's arena starts (the halt boundary), and Vorne's own expressions ({@code smug}, {@code rage}).
 */
class Level35KeysTest {
    private static final String LEVEL = "campaign/act-5-the-belt/level-35-iron-sovereign/data.yaml";

    @Test
    void theLevelLoadsWithItsRelayAndVornesExpressions() {
        Content content = ContentLoader.fromClasspath();
        LevelData level = content.level("act-5-the-belt/level-35-iron-sovereign");
        assertEquals(59.8, level.relay().orElseThrow().t());
        assertEquals(1.5, level.relay().orElseThrow().telegraphOn(Difficulty.EASY));
        assertTrue(level.radio().stream()
                .anyMatch(cue -> cue.speaker().equals(Expression.VORNE)
                        && cue.expression().orElse(null) == Expression.RAGE));
        assertTrue(content.enemy("iron-sovereign").ringed());
    }

    @Test
    void aWaveInTheRelaysQuietWindowIsRejected() {
        assertProblem(
                text -> text.replace(
                        "  - {t: 63.5, formation: snake, enemy: skitter",
                        "  - {t: 62, formation: snake, enemy: skitter"),
                "no wave enters in the relay's quiet window");
    }

    @Test
    void aBunkerOnTheFieldAtTheFlashIsRejected() {
        assertProblem(
                text -> text.replace("at: [[55.5, 110], [55.5, 370]]", "at: [[57, 110], [57, 370]]"),
                "is on the field at the relay's flash");
    }

    @Test
    void aGroundUnitStillOnTheFieldAtTheHaltIsRejected() {
        assertProblem(
                text -> text.replace(
                        "    section: 3\n    enemy: rail-bunker\n    count: 2\n    staggered: true\n    at: [[78, 90], [78, 390]]",
                        "    section: 5\n    enemy: rail-bunker\n    count: 2\n    staggered: true\n    at: [[86, 90], [86, 390]]"),
                "still on the field at the halt");
    }

    @Test
    void smugIsVornesOnly() {
        assertProblem(
                text -> text.replace(
                        "{t: 1, speaker: Okafor, line:", "{t: 1, speaker: Okafor, expression: smug, line:"),
                "smug is Vorne's only");
    }

    /** Loads the design tree with Level 35's data edited; exactly one problem is reported, containing {@code part}. */
    private static void assertProblem(UnaryOperator<String> edit, String part) {
        List<DataFile> files = DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(LEVEL) ? new DataFile(LEVEL, edit.apply(f.text())) : f)
                .toList();
        assertTrue(files.stream().anyMatch(f -> f.path().equals(LEVEL)), LEVEL);
        var e = assertThrows(ContentException.class, () -> ContentLoader.load(files));
        assertEquals(1, e.problems().size(), e.getMessage());
        assertTrue(
                e.problems().getFirst().contains(part),
                "'" + part + "' in " + e.problems().getFirst());
    }
}
