package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import vanguard.sim.Loadout;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * The level report ({@link LevelReport}, {@code ./gradlew :content:levelReport}) prints what the campaign-wide tests
 * compute: its numbers for a level at medium equal the tests' helpers' ({@link PacingTest#fly}, {@link DensityTest},
 * {@link TypicalHaul}, {@link BeamEraHpTest#smallLife}) and a level test's autopilot run on the same seed.
 */
class LevelReportTest {
    private static final Content CONTENT = LevelMetrics.CONTENT;

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 18, 19})
    void theReportsNumbersAreTheTests(int number) throws Exception {
        String key = CONTENT.levelKey(number).orElseThrow();
        LevelReport.Level report;
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            report = LevelReport.measure(
                    CONTENT,
                    key,
                    LevelReport.Options.parse("--level", "" + number, "--difficulty", "medium", "--seeds", "1"),
                    pool);
        } finally {
            pool.shutdownNow();
        }
        System.out.print(LevelReport.text(report));
        LevelReport.ByDifficulty medium = report.difficulties().getFirst();
        Difficulty difficulty = Difficulty.MEDIUM;

        assertEquals(
                DensityTest.perMinute(SimSpecs.level(CONTENT, key, difficulty)),
                medium.density().perMinute());

        Loadout pacingFit = PacingTest.strict(key, difficulty)
                ? PacingTest.strongLoadout(key, difficulty)
                : PacingTest.planLoadout(key, difficulty);
        assertSameFlight(
                PacingTest.fly(key, difficulty, pacingFit), medium.pacing().flight());
        assertSameFlight(
                PacingTest.fly(key, difficulty, PacingTest.planLoadout(key, difficulty)),
                medium.floor().flight());

        TypicalHaul haul = TypicalHaul.of(CONTENT, key);
        assertEquals(haul, medium.haul());
        double fitted = TypicalHaul.of(CONTENT, key, difficulty, report.fittedBountyScale())
                .offBudget();
        assertTrue(Math.abs(fitted)
                <= Math.abs(TypicalHaul.of(CONTENT, key, difficulty, report.fittedBountyScale() + 0.01)
                        .offBudget()));
        assertTrue(Math.abs(fitted)
                <= Math.abs(TypicalHaul.of(CONTENT, key, difficulty, report.fittedBountyScale() - 0.01)
                        .offBudget()));

        if (report.hpApplies()) {
            assertEquals(BeamEraHpTest.smallLife(key), medium.floor().smallLife());
            assertEquals(
                    BeamEraHpTest.smallLife(
                            CONTENT.levelKey(BeamEraHpTest.REFERENCE_LEVEL).orElseThrow()),
                    report.referenceSmallLife());
        }

        // A level test's autopilot run (Level 19's): the plan's fit on seed 1, retried at full armour.
        Sortie sortie = LevelMetrics.sortie(CONTENT, key, difficulty, PacingTest.planLoadout(key, difficulty), 1);
        int steps = 0;
        while (!sortie.complete() && steps++ < LevelReport.AUTOPILOT_STEPS) {
            Level04Test.step(sortie);
        }
        LevelMetrics.Run run = medium.runs().getFirst();
        assertEquals(sortie.complete(), run.complete());
        assertEquals(sortie.attempt() - 1, run.retries());
        assertEquals(sortie.result().secondaryMet(), run.secondaryMet());
        assertEquals(sortie.result().credits().total(), run.credits());
        List<Double> fights = new ArrayList<>();
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.boss().isPresent()) {
                fights.add(piece.killSeconds());
            }
        }
        assertEquals(
                fights,
                run.bosses().stream().map(LevelMetrics.BossFight::seconds).toList());
    }

    private static void assertSameFlight(LevelMetrics.Flight expected, LevelMetrics.Flight actual) {
        assertEquals(expected.meanOnScreen(), actual.meanOnScreen());
        assertEquals(expected.shotsPerMinute(), actual.shotsPerMinute());
        assertEquals(expected.empty().size(), actual.empty().size());
        for (int i = 0; i < expected.empty().size(); i++) {
            assertArrayEquals(expected.empty().get(i), actual.empty().get(i));
        }
    }
}
