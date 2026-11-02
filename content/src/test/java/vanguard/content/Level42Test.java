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
import vanguard.sim.Armament;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M10 part H, step H3: Level 42 Ascendant from its data (design/campaign/act-6-jovian-storm/level-42-ascendant: the run
 * through Callisto orbit under the cruisers' broadsides, the halt and the Ascendant's arena; the fight ends at ESCAPED,
 * the secondary {@code Clear the orbit} judged at the end), flown by the autopilot ({@link AscendantPlay} in the fight)
 * with the balance plan's fit ({@link #planLoadout}), with Rook and without him ({@code --escort none}): every run won on
 * every difficulty, the hull broken and the lifeboat escaped on the completing attempt (the escape ends the fight, no
 * kill). The fight's length, par, calibration, heals and the lifeboat's share are {@link AscendantFightTest}'s; the
 * pacing, the floor, the density, the beam era's HP and the haul the campaign-wide tests' ({@link PacingTest}, {@link
 * ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level42Test {
    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-42-ascendant";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The plan's fitted load at Level 42, MW (the spoofer stowed). */
    static final double LOAD = 23;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 42 ({@code "42"} in design/player/balance-plan.yaml: the visit raises the right
     * Micro-missile Pod to L5 and the Tesla Coil Pod to L3, stows the IFF spoofer and fits Rook's Autocannon) with Rook in
     * the slot (his Autocannon L1 at full armour): the Ion Beam L5, the Tesla Coil Pod L3, the right Micro-missile Pod
     * L5, the Swarm Tail L3, the Shield Mk III, Composite V, the Crucible's 25 MW with 23 fitted, the EMP Burst.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        String shield = content.shields().models().get(2).name();
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        shield,
                        content.armour().plating().get(5).name(),
                        Level40Test.OUTPUT - LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(content, Level40Test.OUTPUT, LOAD, shield, difficulty));
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
     * Step H2's data: the units counted as enemies {@link #COUNTED} per difficulty (the waves'), one boss set piece (the
     * Ascendant, an act boss that escapes: no kill), the boss checkpoint on easy and medium only.
     */
    @Test
    void theLevelHoldsItsCount() {
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
            List<SetPieceSpec> bosses =
                    level.setPieces().stream().filter(SetPieceSpec::isBoss).toList();
            assertEquals(1, bosses.size(), at + ": the Ascendant");
            var boss = bosses.getFirst().boss().orElseThrow();
            assertTrue(!boss.midBoss() && boss.escape().isPresent(), at + ": an act boss that escapes");
        }
        System.out.println("Level 42 counted enemies: " + counted);
        assertEquals(COUNTED, counted);
    }

    /** Step H2's waves' counts. */
    static final Map<Difficulty, Integer> COUNTED =
            Map.of(Difficulty.EASY, 127, Difficulty.MEDIUM, 159, Difficulty.HARD, 186);

    /** What a run saw over every attempt (the losses) and in its completing one (the break and the escape). */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new java.util.ArrayList<>();

        boolean broken;
        boolean escaped;

        @Override
        public void newAttempt() {
            broken = false;
            escaped = false;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                switch (seen.type(i)) {
                    case SHIP_DESTROYED -> deaths.add(String.format("%.1f@%.0f", sortie.levelSeconds(), seen.x(i)));
                    case HULL_BROKEN -> broken = true;
                    case BOSS_ESCAPED -> escaped = true;
                    default -> {}
                }
            }
        }
    }

    /** One whole flight's measures. */
    record Flight(String fit, Difficulty difficulty, long seed, boolean complete, LevelMetrics.Run run, Watch watch) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch();
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch);
        return new Flight(fit, difficulty, seed, complete, LevelMetrics.Run.of(sortie), watch);
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
                "%n    %s: %s, retries %d, losses %s; secondary %s, credits %d; %s",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                flight.watch().deaths,
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.run().credits(),
                flight.run().bosses());
    }

    /** Each run: completed, the hull broken and the lifeboat escaped on the completing attempt, the fight over. */
    private static void check(Flight flight) {
        String at = flight.at();
        assertTrue(flight.complete(), at + ": completes");
        assertTrue(flight.watch().broken, at + ": the hull broke");
        assertTrue(flight.watch().escaped, at + ": the lifeboat escaped");
        assertTrue(
                flight.run().bosses().stream().allMatch(LevelMetrics.BossFight::killed),
                at + ": the escape ends the fight");
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 42 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        flights.forEach(Level42Test::check);
    }

    /** The autopilot completes the level with the plan's fit and Rook on every difficulty and seed (3/3 each). */
    @Test
    void theAutopilotOutlastsTheAscendantWithRook() {
        report("Rook", flights("Rook", Level42Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotOutlastsTheAscendantWithoutRook() {
        report("--escort none", flights("--escort none", Level42Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level42Test::flyToEnd);

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
