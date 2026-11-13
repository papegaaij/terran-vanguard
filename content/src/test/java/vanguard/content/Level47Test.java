package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M11 part E, step E2: Level 47 Seraph Gauntlet from its data (design/campaign/act-7-beyond-the-gate/
 * level-47-seraph-gauntlet: the shell's edge, the ribs, the Choir hall, the core light; the Choir Seraphs entering by
 * their rings, mirroring the ship and folding away, one alone then in pairs, three pairs under a Herald at its station;
 * the {@code escapes} secondary, at most three Seraphs away; the memory pearl), flown by the autopilot (the centre line
 * held for a mirrored Seraph: {@link SeraphPlay}) with the balance plan's fit ({@link #planLoadout}), with Rook and
 * without him ({@code --escort none}): every run won on every difficulty, at most one pair on the screen, the ring cap
 * never starving a pair long. The secondary's met rate is reported (the risk, m11e §5). The pacing, the floor, the
 * density and the haul are the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest},
 * {@link TypicalHaulTest}); the level's static rules are {@link SeraphChecks}' ({@link SeraphKeysTest}).
 */
class Level47Test {
    /** The level's key. */
    static final String LEVEL = "act-7-beyond-the-gate/level-47-seraph-gauntlet";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The plan's fitted load at Level 47, MW (Level 46's guns: the Swarm Call draws no power). */
    static final double LOAD = 24.5;

    /** The Mk VI "Choir Core"'s output, MW (bought on the L47 visit). */
    static final double OUTPUT = 30;

    /** The Seraphs the level sends per difficulty (easy 15, medium 17, hard 18) and the secondary's target. */
    static final Map<Difficulty, Integer> SERAPHS =
            Map.of(Difficulty.EASY, 15, Difficulty.MEDIUM, 17, Difficulty.HARD, 18);

    static final Map<Difficulty, Integer> TARGET =
            Map.of(Difficulty.EASY, 11, Difficulty.MEDIUM, 14, Difficulty.HARD, 16);

    /** The longest a Seraph's entry and jumps may wait for a free ring in all, s (the ring cap, m11e §5). */
    static final double MOST_WAITED = 1.0;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 47 ({@code "47"} in design/player/balance-plan.yaml: the visit buys the Generator
     * Mk VI "Choir Core"): Level 46's guns and the Swarm Call, 24.5 of 30 MW, Rook flying the Autocannon (L1).
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
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 4)),
                        shield,
                        content.armour().plating().get(5).name(),
                        OUTPUT - LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, SimSpecs.SWARM_CALL, 0))
                .withPower(SimSpecs.power(content, OUTPUT, LOAD, shield, difficulty));
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
     * The data: the Seraph's cycle per difficulty (in by its ring, 1.0 s first, a 0.3 s fold, the mirror's 2.0/2.5/3.0
     * s at each arrival, 3/4/5 cycles, out by a jump after a 0.8 s tell rising 40 px), its spiral (two arms, 4/6/8 shots
     * a second), the Seraphs per difficulty (one alone at t=40, pairs; easy's singles at t=62 and 88; hard's pair at
     * t=40), the Heralds' stations (240, 70) with their holds (9.5/14.5/20.5 s: gone as their pairs' leaving tells begin) and their pairs' fixed rows (150, 245), the
     * secondary's target per difficulty.
     */
    @Test
    void theLevelSendsItsSeraphsAndHeralds() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            int seraphs = 0;
            int pairs = 0;
            int heralds = 0;
            for (WaveSpec wave : level.waves()) {
                EnemySpec enemy = wave.enemy();
                if (enemy.slug().equals("choir-seraph")) {
                    seraphs += wave.count();
                    pairs += wave.formation() == WaveSpec.Formation.PAIR ? 1 : 0;
                    EnemySpec.Teleport jumps = enemy.teleport().orElseThrow();
                    EnemySpec.Teleport.Cycle cycle = jumps.cycle().orElseThrow();
                    assertTrue(cycle.enterByRing() && cycle.arrive() == EnemySpec.Teleport.Arrival.MIRROR_ROW);
                    assertEquals(EnemySpec.Teleport.Exit.JUMP, cycle.exit());
                    assertEquals(1.0, cycle.firstRingSeconds(), 1e-9);
                    assertEquals(0.3, cycle.foldSeconds(), 1e-9);
                    assertEquals(0.8, cycle.leaveFoldSeconds(), 1e-9);
                    assertEquals(40, cycle.leaveRise(), 1e-9);
                    assertEquals(0.5, jumps.ringSeconds(), 1e-9);
                    assertEquals(
                            switch (difficulty) {
                                case EASY -> 2.0;
                                case MEDIUM -> 2.5;
                                case HARD -> 3.0;
                            },
                            jumps.glideSeconds(),
                            1e-9);
                    assertEquals(
                            switch (difficulty) {
                                case EASY -> 3;
                                case MEDIUM -> 4;
                                case HARD -> 5;
                            },
                            jumps.landings());
                    var spiral = enemy.gun().orElseThrow().spiral().orElseThrow();
                    assertEquals(2, spiral.arms());
                    assertEquals(
                            switch (difficulty) {
                                case EASY -> 4.0;
                                case MEDIUM -> 6.0;
                                case HARD -> 8.0;
                            },
                            spiral.arms() / spiral.intervalSeconds(),
                            0.02,
                            difficulty + ": the spiral's shots a second");
                    assertTrue(enemy.shadow().isPresent(), "it mirrors");
                    if (wave.rows().isPresent()) {
                        assertEquals(new WaveSpec.Rows(150, 245), wave.rows().get());
                    }
                } else if (enemy.slug().equals("choir-herald")) {
                    heralds++;
                    assertEquals(new WaveSpec.At(240, 70), wave.at().orElseThrow());
                    assertEquals(
                            switch (difficulty) {
                                case EASY -> 9.5;
                                case MEDIUM -> 14.5;
                                case HARD -> 20.5;
                            },
                            wave.holdSeconds().orElseThrow(),
                            1e-9);
                }
            }
            assertEquals(SERAPHS.get(difficulty), seraphs, difficulty + ": the Seraphs");
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 6;
                        case MEDIUM -> 8;
                        case HARD -> 9;
                    },
                    pairs,
                    difficulty + ": the pairs");
            assertEquals(3, heralds, difficulty + ": the Heralds");
            Sortie sortie = sortie(content, planLoadout(content, difficulty), 1, difficulty, false);
            assertEquals(SERAPHS.get(difficulty), sortie.escapesTotal(), difficulty + ": the secondary's Seraphs");
            assertEquals(
                    SERAPHS.get(difficulty) - TARGET.get(difficulty),
                    level.objectives().secondary().escapesAllowed(),
                    difficulty + ": escapes allowed");
        }
    }

    /** What a run saw, over every attempt (the losses) and in its completing one. */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new ArrayList<>();
        /** The most Seraphs on the screen at once (landed), in the completing attempt. */
        int mostSeraphs;
        /** The longest a Seraph waited for a ring (its entry and jumps in all), steps, in the completing attempt. */
        int mostWaited;
        /** Seraphs killed, escaped, in the completing attempt (the sortie's counts at its end). */
        int killed;

        int escaped;
        /** Seraphs killed in their leaving tell, in the completing attempt. */
        int inTell;
        /** Steps Rook flew, in the completing attempt. */
        int rookSteps;

        @Override
        public void newAttempt() {
            mostSeraphs = 0;
            mostWaited = 0;
            killed = 0;
            escaped = 0;
            inTell = 0;
            rookSteps = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            double t = sortie.levelSeconds();
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.SHIP_DESTROYED) {
                    deaths.add(String.format("%.1f@%.0f", t, events.x(i)));
                }
            }
            int landed = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.spec().slug().equals("choir-seraph")) {
                    continue;
                }
                mostWaited = Math.max(mostWaited, enemy.teleport().waited());
                if (enemy.hp() > 0 && !enemy.teleport().gone()) {
                    landed++;
                }
                if (enemy.hp() <= 0 && enemy.leavingTell()) {
                    inTell++;
                }
            }
            mostSeraphs = Math.max(mostSeraphs, landed);
            killed = sortie.escapesDestroyed();
            escaped = sortie.escapesLost();
            if (sortie.wingman().filter(rook -> !rook.ejected()).isPresent()) {
                rookSteps++;
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
        Watch watch = flight.watch();
        return String.format(
                "%n    %s: %s, retries %d, losses %s; Seraphs killed %d (%d in the tell), away %d, at most %d landed,"
                        + " waited %.2f s; Rook %.0f s; %s, credits %d",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                watch.deaths,
                watch.killed,
                watch.inTell,
                watch.escaped,
                watch.mostSeraphs,
                watch.mostWaited / 60.0,
                watch.rookSteps / 60.0,
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.run().credits());
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 47 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        for (Difficulty difficulty : Difficulty.values()) {
            long met = flights.stream()
                    .filter(f -> f.difficulty() == difficulty && f.run().secondaryMet())
                    .count();
            report.append(String.format("%n    %s: the secondary met %d of %d", difficulty, met, SEEDS));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            String at = flight.at();
            assertTrue(flight.complete(), at + ": completes");
            Watch watch = flight.watch();
            assertTrue(watch.mostSeraphs <= 2, at + ": at most one pair on the screen, was " + watch.mostSeraphs);
            assertTrue(
                    watch.mostWaited / 60.0 <= MOST_WAITED,
                    at + ": the ring cap starved a Seraph " + watch.mostWaited / 60.0 + " s");
        }
    }

    /** The autopilot wins the level with the plan's fit and Rook, on every difficulty and seed. */
    @Test
    void theAutopilotCrossesTheSphereWithRook() {
        report("Rook", flights("Rook", Level47Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotCrossesTheSphereWithoutRook() {
        report("--escort none", flights("--escort none", Level47Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level47Test::flyToEnd);

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
