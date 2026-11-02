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
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.Rules;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M10 part G, step G3: Level 41 Callisto Fortress from its data (design/campaign/act-6-jovian-storm/level-41-callisto-
 * fortress: the outer causeways, three pylon gates held while a pylon stands, each with its halt cycle and an archive
 * core's file, the Halo Platform held as the fourth gate with its checkpoint, the run to the empty berth; the secondary
 * {@code FILES} judged at the end), flown by the autopilot ({@link GatePlay} at the gates: the file first, then the awake
 * pylons from a column the domes' covered units leave clear; the Halo {@link RingPlay}'s) with the balance plan's fit
 * ({@link #planLoadout}), with Rook and without him ({@code --escort none}): every run won, every gate opened, the
 * secondary met exactly when the three files are in, the files all in on most medium runs, no pylon awake outside its
 * gate's halt. The halts' lengths, the cycles flown and the Halo's turret order are the level report's {@code gates}
 * part ({@link GateWatch}); the pacing, the floor, the density, the beam era's HP and the haul are the campaign-wide
 * tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link
 * TypicalHaulTest}).
 */
class Level41Test {
    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-41-callisto-fortress";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** A gate's halt this long or longer counts as stuck (the generic autopilot's stood 56-253 s), s. */
    static final double STUCK = 40;

    /** The medium runs of a fit (of {@link #SEEDS}) with all three files in, at least. */
    static final int MEDIUM_FILES = 2;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 41 ({@code "41"} in design/player/balance-plan.yaml: the visit buys Composite V;
     * M10 part I, step I2: Rook keeps Level 40's Autocannon, Level 31's at full armour, as {@link Level40Test}; on the
     * Mortar the fortress's Talons flew past and Act 6 ended under the expert's bound on medium): Level 40's guns (the Ion Beam L5, the Tesla Coil Pod L2, the right Micro-missile Pod L4, the Swarm
     * Tail L3), the Shield Mk III, Composite V, the Crucible's 25 MW with 24 fitted, the IFF spoofer fitted (it fools
     * the fortress's guns and the Halo's), the EMP Burst.
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
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        shield,
                        content.armour().plating().get(5).name(),
                        Level40Test.OUTPUT - Level40Test.LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(content, Level40Test.OUTPUT, Level40Test.LOAD, shield, difficulty))
                .withSpoofer(LoadoutRules.spoofDelay(content));
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
     * Step G2's data: the units counted as enemies {@link #COUNTED} per difficulty (the waves' and the ground's; the
     * halt cycles' waves are their gates', not counted here), the four held sections (three pylon gates and the Halo's),
     * the pylons ({@link #PYLONS}: easy drops pylon C at Gates Two and Three), the Halo the one held set piece (no boss
     * bar), the three archive cores and the secondary's {@code FILES}.
     */
    @Test
    void theLevelHoldsItsCount() {
        Map<Difficulty, Integer> counted = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            String at = difficulty.name();
            int waves = level.waves().stream()
                    .filter(wave -> wave.enemy().countsAsEnemy())
                    .mapToInt(WaveSpec::count)
                    .sum();
            counted.put(difficulty, waves + level.groundUnits().size());
            assertEquals(4, level.sections().stream().filter(Section::arena).count(), at + ": four held sections");
            assertEquals(
                    4,
                    level.gateCycles().stream()
                            .filter(java.util.Optional::isPresent)
                            .count(),
                    at + ": cycles");
            long pylons =
                    level.groundUnits().stream().filter(unit -> unit.holder()).count();
            assertEquals((long) PYLONS.get(difficulty), pylons, at + ": the pylons");
            assertEquals(1, level.setPieces().size(), at + ": the Halo, the one set piece");
            assertTrue(
                    level.setPieces().getFirst().boss().orElseThrow().held(),
                    at + ": held (no bar, warning or themes)");
            assertEquals(3, level.uplinks().size(), at + ": three archive cores");
        }
        assertEquals(
                3,
                content.level(LEVEL)
                        .objectives()
                        .secondary()
                        .orElseThrow()
                        .files()
                        .orElseThrow(),
                "FILES 3");
        System.out.println("Level 41 counted enemies: " + counted);
        assertEquals(COUNTED, counted);
    }

    /** The pylons per difficulty: Gates One, Two, Three of 2, 3, 3 (easy 2, 2, 2). */
    static final Map<Difficulty, Integer> PYLONS = Map.of(Difficulty.EASY, 6, Difficulty.MEDIUM, 8, Difficulty.HARD, 8);

    /** Step G2's waves' and ground's counts. */
    static final Map<Difficulty, Integer> COUNTED =
            Map.of(Difficulty.EASY, 238, Difficulty.MEDIUM, 282, Difficulty.HARD, 321);

    /** What a run saw, over every attempt (the losses) and in its completing one (the files, the pickups). */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new java.util.ArrayList<>();
        /** The pickups placed in the completing attempt, by type. */
        final Map<PickupType, Integer> placed = new EnumMap<>(PickupType.class);
        /** The pickups collected in the completing attempt, by type. */
        final Map<PickupType, Integer> collected = new EnumMap<>(PickupType.class);
        /** Steps with an awake pylon while no halt runs, over every attempt. */
        int awakeOutside;

        int filesLost;

        @Override
        public void newAttempt() {
            placed.clear();
            collected.clear();
            filesLost = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                switch (seen.type(i)) {
                    case SHIP_DESTROYED -> deaths.add(String.format("%.1f@%.0f", sortie.levelSeconds(), seen.x(i)));
                    case PICKUP_PLACED -> placed.merge(PickupType.values()[seen.value(i)], 1, Integer::sum);
                    case PICKUP_COLLECTED -> collected.merge(PickupType.values()[seen.value(i)], 1, Integer::sum);
                    case FILE_LOST -> filesLost++;
                    default -> {}
                }
            }
            if (!sortie.halting()) {
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy enemy = sortie.enemy(i);
                    if (enemy.holder() && !enemy.dormant() && enemy.hp() > 0) {
                        awakeOutside++;
                        break;
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
            GateWatch.Gates gates,
            int files) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch();
        GateWatch gates = GateWatch.of(sortie.script(), seed);
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch, gates);
        return new Flight(
                fit,
                difficulty,
                seed,
                complete,
                LevelMetrics.Run.of(sortie),
                watch,
                gates.gates(complete),
                sortie.uplinks().filesIn());
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
                "%n    %s: %s, retries %d, losses %s; files %d / 3 %s (lost %d), credits %d; placed %s, collected %s%n      %s",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                flight.watch().deaths,
                flight.files(),
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.watch().filesLost,
                flight.run().credits(),
                flight.watch().placed,
                flight.watch().collected,
                flight.gates());
    }

    /**
     * Each run's checks: completed with every gate opened, none stuck; the secondary met exactly when the three files
     * are in (judged at the end); no pylon awake outside its gate's halt (asleep, none falls on the way in).
     */
    private static void check(Flight flight) {
        String at = flight.at();
        assertTrue(flight.complete(), at + ": completes");
        for (GateWatch.Gate gate : flight.gates().gates()) {
            assertTrue(gate.opened(), at + ": " + gate);
            assertTrue(gate.halt() < STUCK, at + ": " + gate + " (stuck)");
        }
        assertEquals(flight.files() == 3, flight.run().secondaryMet(), at + ": FILES judged at the end");
        assertEquals(0, flight.watch().awakeOutside, at + ": steps with a pylon awake outside its halt");
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 41 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            check(flight);
        }
        long allFiles = flights.stream()
                .filter(flight -> flight.difficulty() == Difficulty.MEDIUM && flight.files() == 3)
                .count();
        assertTrue(allFiles >= MEDIUM_FILES, fit + ": medium runs with FILES 3 / 3: " + allFiles + " of " + SEEDS);
    }

    /** The autopilot completes the level with the plan's fit and Rook on every difficulty and seed. */
    @Test
    void theAutopilotBreaksTheGatesWithRook() {
        report("Rook", flights("Rook", Level41Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotBreaksTheGatesWithoutRook() {
        report("--escort none", flights("--escort none", Level41Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level41Test::flyToEnd);

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
