package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M11 part B, step B3: Level 44 Crossing from its data (design/campaign/act-7-beyond-the-gate/level-44-crossing: the
 * far mouth, the current, the narrows, the second reversal, out of the gate; distorted space's drift up to ±25° and its
 * two reversals told 2 s ahead; the kill-ratio secondary, 65 % judged at the end), flown by the autopilot ({@link
 * DriftPlay}: the drift read, a landing ring kept off, the rear wave of a reversal turned to) with the balance plan's
 * fit ({@link #planLoadout}), with Rook and without him ({@code --escort none}): every run won on every difficulty, the
 * secondary met on most runs, no chain on the screen in a reversal. The pacing (a reversal a scripted pause: {@link
 * LevelMetrics#REVERSAL_SCRIPTED}), the floor, the density, the beam era's HP and the haul are the campaign-wide tests'
 * ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level44Test {
    /** The level's key. */
    static final String LEVEL = "act-7-beyond-the-gate/level-44-crossing";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The plan's fitted load at Level 44, MW (the Swarm Tail L4). */
    static final double LOAD = 24;

    /** The runs of a fit (of {@link #SEEDS} on each difficulty) with the secondary met, at least ("most runs"). */
    static final int SECONDARY_RUNS = 6;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 44 ({@code "44"} in design/player/balance-plan.yaml: the visit raises the Swarm
     * Tail to L4) with Rook in the slot (his Autocannon, at full armour): Level 43's fit ({@link Level43Test#actSeven})
     * with the Swarm Tail L4, 24 of 25 MW fitted.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level43Test.actSeven(content, difficulty, 4, LOAD);
    }

    static Sortie sortie(Content content, Loadout loadout, long seed, Difficulty difficulty, boolean invulnerable) {
        Rules rules = SimSpecs.rules(content, LEVEL, difficulty);
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, LEVEL, difficulty),
                invulnerable ? rules.withInvulnerableShip() : rules,
                loadout.plating().maxArmour());
    }

    /**
     * Step B2's data: the units counted as enemies {@link #COUNTED} per difficulty (the waves'), the three Coilwyrm
     * chains (hard's t=210 a crossing pair: four), each 337 HP whole at medium (head 135, 12 segments of 14, tail 34:
     * the act factor ×176/52 on Level 06's 40, 4 and 10), the current's profile within ±25° (easy ±15°) and its two
     * reversals of 4 s and 5 s (easy 3 s each), told 2 s ahead.
     */
    @Test
    void theLevelHoldsItsCountAndItsCurrent() {
        Map<Difficulty, Integer> counted = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            String at = difficulty.name();
            counted.put(
                    difficulty,
                    level.waves().stream()
                            .filter(wave -> wave.enemy().countsAsEnemy())
                            .mapToInt(WaveSpec::count)
                            .sum());
            List<WaveSpec> chains = level.waves().stream()
                    .filter(wave -> wave.enemy().chain().isPresent())
                    .toList();
            assertEquals(
                    difficulty == Difficulty.HARD ? 4 : 3,
                    chains.stream().mapToInt(WaveSpec::count).sum(),
                    at + ": the chains");
            HazardSpec.Current current = level.hazards().current().orElseThrow();
            double most = difficulty == Difficulty.EASY ? 15 : 25;
            for (HazardSpec.Bend bend : current.profile()) {
                assertTrue(Math.abs(bend.degrees()) <= most + 1e-9, at + ": " + bend + " within ±" + most);
            }
            assertEquals(
                    difficulty == Difficulty.EASY ? List.of(3.0, 3.0) : List.of(4.0, 5.0),
                    current.reversals().stream()
                            .map(HazardSpec.Reversal::runSeconds)
                            .toList(),
                    at + ": the reversals' runs");
            assertEquals(2, current.toldSeconds(), 1e-9, at + ": told 2 s ahead");
        }
        EnemySpec wyrm = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).waves().stream()
                .filter(wave -> wave.enemy().chain().isPresent())
                .findFirst()
                .orElseThrow()
                .enemy();
        EnemySpec.ChainSpec chain = wyrm.chain().orElseThrow();
        assertEquals(135, wyrm.hp(), "the head");
        assertEquals(14, chain.segment().hp(), "a segment");
        assertEquals(34, chain.tail().hp(), "the tail");
        assertEquals(
                337,
                wyrm.hp()
                        + chain.segmentBoxes().size() * chain.segment().hp()
                        + chain.tail().hp(),
                "a whole chain");
        System.out.println("Level 44 counted enemies: " + counted);
        assertEquals(COUNTED, counted);
    }

    /** Step B3b's waves' counts (B2's 403 / 527 / 652 with the denser mixed waves, the user's answer of 2026-11-05). */
    static final Map<Difficulty, Integer> COUNTED =
            Map.of(Difficulty.EASY, 497, Difficulty.MEDIUM, 648, Difficulty.HARD, 800);

    /** What a run saw, over every attempt (the losses) and in its completing one (the reversals, the kills). */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new java.util.ArrayList<>();
        /** Steps with a chain's member on the screen while a reversal runs, in the completing attempt. */
        int chainInReversal;
        /** Steps of the reversals' runs, in the completing attempt. */
        int reversed;
        /** Rift Skaters that landed within 50 px of the ship's centre, in the completing attempt. */
        int landedClose;

        @Override
        public void newAttempt() {
            chainInReversal = 0;
            reversed = 0;
            landedClose = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                if (seen.type(i) == SimEvents.Type.SHIP_DESTROYED) {
                    deaths.add(String.format("%.1f@%.0f", sortie.levelSeconds(), seen.x(i)));
                } else if (seen.type(i) == SimEvents.Type.ARRIVE
                        && Math.hypot(
                                        seen.x(i) - sortie.ship().x(),
                                        seen.y(i) - sortie.ship().y())
                                < 50) {
                    landedClose++;
                }
            }
            if (sortie.current().running(SimStep.ticks(sortie.levelSeconds())) < 0) {
                return;
            }
            reversed++;
            for (int c = 0; c < sortie.chainCount(); c++) {
                var chain = sortie.chain(c);
                for (int k = 0; k < chain.size(); k++) {
                    Enemy member = chain.member(k);
                    if (member != null
                            && member.hp() > 0
                            && LevelMetrics.onScreen(member.renderX(1), member.renderY(1), member.hitbox())) {
                        chainInReversal++;
                        return;
                    }
                }
            }
        }
    }

    /** One whole flight's measures. */
    record Flight(
            String fit,
            Difficulty difficulty,
            long seed,
            boolean complete,
            LevelMetrics.Run run,
            Watch watch,
            int kills,
            int required) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch();
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch);
        return new Flight(
                fit,
                difficulty,
                seed,
                complete,
                LevelMetrics.Run.of(sortie),
                watch,
                sortie.secondaryKills(),
                sortie.requiredKills());
    }

    /** The flights of a fit over the seeds and difficulties, flown in parallel, in their order. */
    List<Flight> flights(String fit, BiFunction<Content, Difficulty, Loadout> loadout) {
        Difficulty[] difficulties = Difficulty.values();
        return IntStream.range(0, difficulties.length * SEEDS)
                .parallel()
                .mapToObj(i -> {
                    Difficulty difficulty = difficulties[i / SEEDS];
                    return fly(fit, loadout.apply(content, difficulty), difficulty, 1 + i % SEEDS);
                })
                .toList();
    }

    private static String summary(Flight flight) {
        return String.format(
                "%n    %s: %s, retries %d, losses %s; kills %d / %d %s, credits %d; reversed %.1f s, a chain on the"
                        + " screen %.1f s of it; Skaters landed within 50 px %d",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                flight.watch().deaths,
                flight.kills(),
                flight.required(),
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.run().credits(),
                flight.watch().reversed * SimStep.SECONDS,
                flight.watch().chainInReversal * SimStep.SECONDS,
                flight.watch().landedClose);
    }

    /**
     * Each run's checks: completed; the secondary met exactly when its kills reach the target (judged at the end); no
     * chain on the screen while a reversal runs (design/campaign/act-7-beyond-the-gate/level-44-crossing: no chain within
     * 10 s before a tell, a loop-back due in a reversal waits).
     */
    private static void check(Flight flight) {
        String at = flight.at();
        assertTrue(flight.complete(), at + ": completes");
        assertEquals(
                flight.kills() >= flight.required(), flight.run().secondaryMet(), at + ": the kill ratio at the end");
        assertEquals(0, flight.watch().chainInReversal, at + ": steps with a chain on the screen in a reversal");
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 44 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            check(flight);
        }
        long met =
                flights.stream().filter(flight -> flight.run().secondaryMet()).count();
        assertTrue(
                met >= SECONDARY_RUNS,
                fit + ": runs with the secondary met: " + met + " of " + flights.size() + ", at least "
                        + SECONDARY_RUNS);
    }

    /** The autopilot completes the level with the plan's fit and Rook on every difficulty and seed. */
    @Test
    void theAutopilotCrossesWithRook() {
        report("Rook", flights("Rook", Level44Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotCrossesWithoutRook() {
        report("--escort none", flights("--escort none", Level44Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level44Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, planLoadout(content, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(content, planLoadout(content, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false);
        flyToEnd(first);
        flyToEnd(second);

        assertEquals(first.stateHash(), second.stateHash());
        assertTrue(first.complete());
    }

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
