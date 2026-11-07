package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

/**
 * Step DB1 (design/ui/databank, Where the data comes from): the databank's texts load with the design data, and their
 * checks: every slug resolves, every built unit, boss and reachable gear item has its lines, every line fits the label
 * width, a concept page opens after a built level.
 */
class DatabankTextsTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String TEXTS = "ui/databank/data.yaml";

    @Test
    void theTextsLoadWithTheDesignData() {
        DatabankData texts = CONTENT.databank();

        assertEquals(
                CONTENT.enemies().values().stream()
                        .filter(enemy -> !DatabankChecks.boss(enemy))
                        .count(),
                texts.enemies().size());
        assertEquals(
                CONTENT.enemies().values().stream().filter(DatabankChecks::boss).count(),
                texts.bosses().size());
        assertEquals(CONTENT.weapons().keySet(), texts.weapons().keySet());
        assertEquals(14, texts.concepts().size());
        assertEquals(java.util.Optional.of("Jovian Ascendancy"), texts.factionAt("warden-tank", 34));
        assertEquals(java.util.Optional.empty(), texts.factionAt("warden-tank", 33));
        assertTrue(texts.bosses().values().stream()
                .allMatch(boss -> boss.phases().size() >= 3 && boss.phases().size() <= 5));
    }

    @Test
    void everyBuiltUnitBossAndReachableItemHasItsText() {
        assertEquals(List.of(), DatabankChecks.missing(CONTENT));
        // A unit without its text is not a load problem (a test's stand-in units have none), but it is missing.
        List<DataFile> files = DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(TEXTS)
                        ? new DataFile(
                                TEXTS,
                                f.text().replaceFirst("\n  needler:\n(    .*\n)+?  skitter:\n", "\n  skitter:\n"))
                        : f)
                .toList();
        assertEquals(List.of("enemies: needler"), DatabankChecks.missing(ContentLoader.load(files)));
    }

    @Test
    void aGearSlugDropsTheNicknameAndReadsAPlus() {
        assertEquals("mk-v-plus", DatabankData.slug("Mk V+ \"Crucible\""));
        assertEquals("mk-ii", DatabankData.slug("Mk II \"Arc\""));
        assertEquals("ascendancy-iff-spoofer", DatabankData.slug("Ascendancy IFF spoofer"));
        assertEquals("auto-repair-nanites", DatabankData.slug("Auto-repair nanites"));
    }

    @Test
    void aLineOverTheLabelWidthIsReported() {
        String line = "x".repeat(DatabankData.LINE_WIDTH + 1);
        List<String> problems = problems(text ->
                text.replace("    - Ivory crab-things with guns. They shoot back, Lancer, slowly.", "    - " + line));

        assertTrue(
                problems.stream().anyMatch(p -> p.contains("vague") && p.contains("64 characters")),
                problems::toString);
    }

    @Test
    void anUnknownUnitIsReported() {
        List<String> problems = problems(text -> text.replaceFirst("\n  needler:\n", "\n  needle:\n"));

        assertTrue(
                problems.stream().anyMatch(p -> p.contains("enemies.needle") && p.contains("not a unit")),
                problems::toString);
    }

    @Test
    void aBossUnderEnemiesIsReported() {
        List<String> problems = problems(text -> text.replaceFirst(
                "\nenemies:\n", "\nenemies:\n  iron-sovereign:\n    vague:\n    - A.\n    tip:\n    - B.\n"));

        assertTrue(problems.stream().anyMatch(p -> p.contains("enemies.iron-sovereign")), problems::toString);
    }

    @Test
    void anUnknownGearItemIsReported() {
        List<String> problems = problems(text -> text.replaceFirst("\n  pulse-cannon:\n", "\n  pulse-canon:\n"));

        assertTrue(
                problems.stream().anyMatch(p -> p.contains("weapons.pulse-canon") && p.contains("no item")),
                problems::toString);
    }

    @Test
    void aConceptAfterAnUnbuiltLevelIsReported() {
        // M11 part C: Level 45 is built; M11 part D: Level 46 too; M11 part E: Level 47 too; M11 part F: Level 48 too;
        // M11 part G: Level 49 too; Level 50 is not.
        List<String> problems = problems(text -> text.replaceFirst("    after: 37\n", "    after: 50\n"));

        assertTrue(
                problems.stream().anyMatch(p -> p.contains(".after") && p.contains("Level 50 has no data")),
                problems::toString);
    }

    @Test
    void aFactionFromOfAnUnknownUnitIsReported() {
        List<String> problems =
                problems(text -> text.replaceFirst("\n  warden-tank:\n    34:", "\n  warden-tonk:\n    34:"));

        assertTrue(problems.stream().anyMatch(p -> p.contains("faction_from.warden-tonk")), problems::toString);
    }

    /** The problems of the design tree with the texts edited. */
    private static List<String> problems(UnaryOperator<String> edit) {
        List<DataFile> files = DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(TEXTS) ? new DataFile(TEXTS, edit.apply(f.text())) : f)
                .toList();
        DataFile original = DesignTree.dataFiles().stream()
                .filter(f -> f.path().equals(TEXTS))
                .findFirst()
                .orElseThrow();
        DataFile edited =
                files.stream().filter(f -> f.path().equals(TEXTS)).findFirst().orElseThrow();
        assertNotEquals(original.text(), edited.text(), "the edit applies");
        return assertThrows(ContentException.class, () -> ContentLoader.load(files))
                .problems();
    }
}
