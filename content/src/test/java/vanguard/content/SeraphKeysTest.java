package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import vanguard.sim.EnemySpec;

/**
 * M11 part E, step E2 (m11e §1 #1–#6, content side): Level 47's keys loaded and checked: the Choir Seraph's {@code
 * teleport.cycle} and its hook's {@code glide}, a hook's spiral interval taken as final, the formation {@code pair} and
 * its {@code rows}, and {@link SeraphChecks}' rows on Level 47's data with one thing moved: two pairs' holds overlapping,
 * an edge warning on a landing, a medium hull entering while a pair holds, centre-line popcorn in a hold, a pair from a
 * side or of three, rows closer than 90 px, a mirror-row cycle on a unit that does not mirror.
 */
class SeraphKeysTest {
    private static final String LEVEL_47 = "campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/data.yaml";
    private static final String SERAPH = "enemies/air/choir-seraph/data.yaml";

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
        EnemyData seraph = CONTENT.enemy("choir-seraph");
        EnemyData.Teleport teleport = seraph.movement().teleport().orElseThrow();
        assertEquals(
                new EnemyData.Teleport.Cycle(
                        EnemyData.Teleport.Cycle.Enter.RING,
                        1.0,
                        0.3,
                        EnemyData.Teleport.Cycle.Arrive.MIRROR,
                        EnemyData.Teleport.Cycle.Exit.JUMP,
                        0.8,
                        40),
                teleport.cycle().orElseThrow());
        assertTrue(teleport.jump().isEmpty()
                && teleport.angle().isEmpty()
                && teleport.firstGlide().isEmpty());
        EnemySpec medium = UnitRules.enemy(CONTENT, "choir-seraph", Difficulty.MEDIUM, java.util.Optional.empty());
        EnemySpec.Teleport jumps = medium.teleport().orElseThrow();
        assertEquals(2.5, jumps.glideSeconds());
        assertEquals(4, jumps.landings());
        assertEquals(0.25, jumps.fireAfterSeconds(), "the vocabulary's floor (it fires its spiral while it mirrors)");
        assertEquals(
                EnemySpec.Teleport.Arrival.MIRROR_ROW,
                jumps.cycle().orElseThrow().arrive());
        assertEquals(1 / 3.0, medium.gun().orElseThrow().spiral().orElseThrow().intervalSeconds(), 0.001);
        EnemySpec easy = UnitRules.enemy(CONTENT, "choir-seraph", Difficulty.EASY, java.util.Optional.empty());
        assertEquals(2.0, easy.teleport().orElseThrow().glideSeconds());
        assertEquals(3, easy.teleport().orElseThrow().landings());
        assertEquals(0.5, easy.gun().orElseThrow().spiral().orElseThrow().intervalSeconds(), "final: no lever");
        EnemySpec hard = UnitRules.enemy(CONTENT, "choir-seraph", Difficulty.HARD, java.util.Optional.empty());
        assertEquals(3.0, hard.teleport().orElseThrow().glideSeconds());
        assertEquals(5, hard.teleport().orElseThrow().landings());
        assertEquals(0.25, hard.gun().orElseThrow().spiral().orElseThrow().intervalSeconds(), "final: no lever");
        // The hold: 1.0 + 3 × 3.5 + 2.5 + 0.8 + 0.2 = 15 s at medium; easy 10, hard 21.
        assertEquals(15, SeraphChecks.hold(seraph, Difficulty.MEDIUM), 1e-9);
        assertEquals(10, SeraphChecks.hold(seraph, Difficulty.EASY), 1e-9);
        assertEquals(21, SeraphChecks.hold(seraph, Difficulty.HARD), 1e-9);
    }

    @Test
    void twoPairsOnTheScreenAreReported() {
        assertProblem(
                problems(
                        LEVEL_47,
                        "{t: 112, formation: pair, enemy: choir-seraph",
                        "{t: 100, formation: pair, enemy: choir-seraph"),
                "at most one pair on the screen");
    }

    @Test
    void anEdgeWarningOnALandingIsReported() {
        assertProblem(
                problems(
                        LEVEL_47,
                        "{t: 68, formation: stream, enemy: lamprey",
                        "{t: 69.5, formation: stream, enemy: lamprey"),
                "within 0.5 s of a Seraph's landing");
    }

    @Test
    void aMediumHullWhileAPairHoldsIsReported() {
        assertProblem(
                problems(
                        LEVEL_47,
                        "{t: 78, formation: single, enemy: mantis",
                        "{t: 70, formation: single, enemy: mantis"),
                "hull enters while a pair holds");
    }

    @Test
    void centrePopcornWhileAPairHoldsIsReported() {
        assertProblem(
                problems(
                        LEVEL_47,
                        "{t: 71, formation: snake, enemy: skitter, count: 8, from: front, edge: right}",
                        "{t: 71, formation: snake, enemy: skitter, count: 8, from: front}"),
                "the popcorn off the centre line");
    }

    @Test
    void aPairFromASideIsReported() {
        assertProblem(
                problems(
                        LEVEL_47,
                        "{t: 112, formation: pair, enemy: choir-seraph, count: 2, from: front",
                        "{t: 112, formation: pair, enemy: choir-seraph, count: 2, from: sides"),
                "a pair is two mirror-row teleporters from the front");
    }

    @Test
    void rowsTooCloseAreReported() {
        assertProblem(problems(LEVEL_47, "rows: [150, 245]", "rows: [150, 220]"), "at least 90 px apart");
    }

    @Test
    void aMirrorRowCycleWithoutAMirrorIsReported() {
        assertProblem(
                problems(SERAPH, "  mirror: {lag: 0.15, speed: 260, min_dy: 140}\n", ""),
                "a ring on its mirror row is a mirroring unit's");
    }
}
