package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;
import vanguard.sim.CueTrigger;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.RadioCue;

/**
 * M10 part E, step E2 (design/campaign Level 39; m10e-gaps.md §1 #2, #6, #7, #10, #11, #12): the keys Level 39 brings
 * into the data, loaded and checked: a vent's {@code first}, the vents' tells on the field and their clearance from
 * the towers and pump stations, the radio events {@code first-target} and {@code topple-target}, a trigger spent by
 * damage and a backdrop piece's {@code dark_with}.
 */
class RefineryKeysTest {
    private static final String LEVEL_39 = "campaign/act-6-jovian-storm/level-39-io-flyby/data.yaml";
    private static final Content CONTENT = LevelMetrics.CONTENT;

    /** The design tree with {@code edits} (file, text, replacement; the first occurrence). */
    private static List<DataFile> files(String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (DataFile file : DesignTree.dataFiles()) {
            String text = file.text();
            for (int i = 0; i < edits.length; i += 3) {
                if (edits[i].equals(file.path())) {
                    assertTrue(text.contains(edits[i + 1]), file.path() + " has " + edits[i + 1]);
                    text = text.replaceFirst(
                            java.util.regex.Pattern.quote(edits[i + 1]), Matcher.quoteReplacement(edits[i + 2]));
                }
            }
            files.add(new DataFile(file.path(), text));
        }
        return files;
    }

    private static void assertProblem(String text, String... edits) {
        List<String> problems = assertThrows(ContentException.class, () -> ContentLoader.load(files(edits)))
                .problems();
        assertTrue(problems.stream().anyMatch(p -> p.contains(text)), "a problem with '" + text + "' in " + problems);
    }

    @Test
    void aVentsFirstTellIsItsOwn() {
        List<HazardSpec.Vent> vents = SimSpecs.level(CONTENT, Level39Plan.LEVEL, Difficulty.MEDIUM)
                .hazards()
                .eruptions()
                .orElseThrow()
                .vents();
        assertEquals(27, vents.size());
        assertTrue(vents.stream().allMatch(HazardSpec.Vent::firstSet), "every vent has its first tell");
        assertEquals(0.8, vents.getFirst().firstSeconds(), 1e-9);
        assertEquals(5, vents.getFirst().minPeriod(), 1e-9);
        assertEquals(7, vents.getFirst().maxPeriod(), 1e-9);
        // The plume field's rows ripple 0.4 s a vent.
        List<HazardSpec.Vent> row = vents.stream().filter(v -> v.t() == 145).toList();
        assertEquals(
                List.of(0.3, 0.7, 1.1),
                row.stream().map(HazardSpec.Vent::firstSeconds).toList());
    }

    @Test
    void aVentsTellStartsOnTheField() {
        assertProblem(
                "its first tell starts while it is on the field",
                LEVEL_39,
                "  - {t: 12, x: 340, first: 0.8}",
                "  - {t: 12, x: 340, first: 4.5}");
    }

    @Test
    void noVentLiesBesideATowerOrAPump() {
        // Loki A enters at t=43, x 150: a vent entering with it at x 200 lies 50 px off its centre's column.
        assertProblem(
                "no vent within 100 px of a cracking-tower",
                LEVEL_39,
                "  - {t: 43.985, x: 60, first: 1.12}",
                "  - {t: 43.6, x: 200, first: 1.12}");
        // P1 enters at t=68, x 150.
        assertProblem(
                "no vent within 100 px of a pump-station",
                LEVEL_39,
                "  - {t: 66, x: 60, first: 0.8}",
                "  - {t: 68.2, x: 160, first: 0.8}");
    }

    @Test
    void theTargetCuesAreTheSimulations() {
        LevelScript level = SimSpecs.level(CONTENT, Level39Plan.LEVEL, Difficulty.MEDIUM);
        List<CueTrigger> triggers =
                level.radio().stream().map(RadioCue::trigger).toList();
        assertTrue(triggers.contains(CueTrigger.FIRST_TARGET), "Rook's first tower down");
        assertTrue(triggers.contains(CueTrigger.TOPPLE_TARGET), "Okafor's topple onto a tower");
        assertTrue(triggers.contains(CueTrigger.MISSION_FAILED), "a tower passing");
        // Only with destroy-targets groups (Level 38 has none).
        assertProblem(
                "a first-target cue needs destroy-targets groups",
                "campaign/act-6-jovian-storm/level-38-lightning-belt/data.yaml",
                "  - {event: level-end, speaker: Okafor,",
                "  - {event: first-target, speaker: Okafor, line: \"Down.\"}\n  - {event: level-end, speaker: Okafor,");
    }

    @Test
    void theHutsLockIsSpentByDamage() {
        GroundObjectSpec lock = SimSpecs.level(CONTENT, Level39Plan.LEVEL, Difficulty.MEDIUM).groundObjects().stream()
                .filter(GroundObjectSpec::trigger)
                .findFirst()
                .orElseThrow();
        assertTrue(lock.damageSpent(), "a trigger spent by damage");
        assertEquals(30, lock.hp(), 1e-9);
        assertEquals("survey hut", lock.secret());
        // A trigger spent by damage gives hits: 1.
        assertProblem(
                "a trigger spent by damage gives hits: 1",
                LEVEL_39,
                "    hits: 1\n    hp: 30",
                "    hits: 2\n    hp: 30");
    }

    @Test
    void aPipeGoesDarkWithATowerOrAPump() {
        LevelData level = CONTENT.level(Level39Plan.LEVEL);
        List<String> dark = level.backdrop().placed().stream()
                .flatMap(piece -> piece.darkWith().stream())
                .toList();
        assertEquals(14, dark.size(), "six towers' pipes and eight pumps' racks");
        assertEquals(8, RefineryChecks.pumpStations(level));
        assertProblem("names neither a destroy-targets group", LEVEL_39, "dark_with: P8}", "dark_with: P9}");
        assertProblem(
                "has no 'jupiter-glow' piece",
                LEVEL_39,
                "    - {piece: jupiter, t: -6.154, x: 240}",
                "    - {piece: jupiter, t: -6.154, x: 240, dark_with: Loki A}");
    }
}
