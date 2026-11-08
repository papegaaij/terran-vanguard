package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Sortie;
import vanguard.sim.VrellLevel;

/**
 * M11 part H, step H2: Level 50 Choir Heart from its data (design/campaign/act-7-beyond-the-gate/level-50-choir-heart:
 * the hive core's approach, the Choir Heart's three phases, the closing gate's scene), flown by the autopilot ({@link
 * HeartPlay} for the Heart) with the balance plan's fit ({@link #planLoadout}: Level 49's, the visit buys nothing), with
 * Rook and without him ({@code --escort none}): every run won on every difficulty (hard without a boss checkpoint),
 * through the scene to the level's end. The fight's measures are {@link HeartFightTest}'s; the pacing, the floor, the
 * density and the haul the campaign-wide tests'.
 */
class Level50Test {
    /** The level's key. */
    static final String LEVEL = "act-7-beyond-the-gate/level-50-choir-heart";

    static final int MAX_STEPS = 60 * 60 * 30;

    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    private final Content content = ContentLoader.fromClasspath();

    /** The balance plan's fit at Level 50 ({@code "50"}: the visit buys nothing): Level 49's, Rook's Autocannon. */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level49Test.planLoadout(content, difficulty);
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level49Test.withoutRook(content, difficulty);
    }

    /**
     * The data: the Heart warned 5 s before it shows, its windows, walls, drift and phase checkpoints, the closing gate
     * timed from its K and reversed, the kill ratio judged by the warning, no wave after the warning.
     */
    @Test
    void theLevelSendsTheHeartAndTheClosingGate() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<SetPieceSpec> bosses =
                    level.setPieces().stream().filter(SetPieceSpec::isBoss).toList();
            assertEquals(
                    List.of("choir-heart"),
                    bosses.stream().map(SetPieceSpec::slug).toList());
            BossSpec boss = bosses.getFirst().boss().orElseThrow();
            assertTrue(boss.incomingSeconds() >= 5, "warned before it shows");
            assertTrue(boss.partWindows().isPresent(), "the membranes' windows");
            assertTrue(boss.walls().isPresent(), "the arena walls");
            assertTrue(boss.drift().isPresent(), "the drift");
            assertEquals(
                    difficulty != Difficulty.EASY,
                    boss.drift().orElseThrow().reversal().isPresent(),
                    "one reversal, none on easy");
            double corridor =
                    switch (difficulty) {
                        case EASY -> 360;
                        case MEDIUM -> 320;
                        case HARD -> 300;
                    };
            assertEquals(corridor, boss.walls().orElseThrow().corridor(), 1e-9, "the corridor");
            assertTrue(boss.phaseCheckpoints(), "a checkpoint at each phase's start (where the difficulty has one)");
            assertTrue(boss.heartbeat() >= 0 && boss.heartbeatFast() >= 0, "the heartbeat's two beats");
            VrellLevel.Transit gate = level.vrell().transit().orElseThrow();
            assertTrue(gate.fromKill() && gate.reverse(), "the closing gate from K, the warp home");
            assertEquals(12, gate.form(), 1e-9, "control taken at K + 12");
            assertEquals(29, gate.warp(), 1e-9, "the warp at K + 29");
            assertTrue(level.objectives().secondary().untilWarning(), "KILLS n / 219 by the warning");
            assertEquals(
                    0, level.waves().stream().filter(wave -> wave.t() > 181).count(), "no wave after the warning");
            assertFalse(level.waves().isEmpty());
        }
    }

    /** The autopilot's runs on {@code difficulty} with the loadout {@code fit} gives. */
    private List<LevelMetrics.Run> runs(Difficulty difficulty, BiFunction<Content, Difficulty, Loadout> fit) {
        return IntStream.range(1, SEEDS + 1)
                .parallel()
                .mapToObj(seed -> LevelMetrics.autopilot(
                        content, LEVEL, difficulty, fit.apply(content, difficulty), seed, MAX_STEPS))
                .toList();
    }

    @Test
    void theAutopilotWinsEveryDifficultyWithRookAndWithout() {
        List<String> out = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            for (boolean rook : new boolean[] {true, false}) {
                BiFunction<Content, Difficulty, Loadout> fit =
                        rook ? Level50Test::planLoadout : Level50Test::withoutRook;
                for (LevelMetrics.Run run : runs(difficulty, fit)) {
                    String at = difficulty + (rook ? " with Rook" : " without Rook") + ": " + run;
                    System.out.println(at);
                    if (!run.complete()) {
                        out.add(at);
                    }
                }
            }
        }
        assertTrue(out.isEmpty(), "every run won: " + out);
    }

    /** The sortie of the plan's fit (for the probes). */
    static Sortie sortie(Content content, Loadout loadout, long seed, Difficulty difficulty) {
        return LevelMetrics.sortie(content, LEVEL, difficulty, loadout, seed);
    }
}
