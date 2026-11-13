package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.BossSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M11 part F, step F2: Level 48 Vorne's End from its data (design/campaign/act-7-beyond-the-gate/level-48-vornes-end:
 * the outer growth, the gold decks, the spire road, the spire well and its halt, the heart, through the spire; the rear
 * waves, the turret nests, the ten reactor cores of the {@code escapes} secondary; Vorne's archive; the mid-boss Vorne's
 * Chimera), flown by the autopilot (the Chimera's fight: {@link ChimeraPlay}) with the balance plan's fit ({@link
 * #planLoadout}), with Rook and without him ({@code --escort none}): every run won on every difficulty. The secondary's
 * met rate is reported. The pacing, the floor, the density and the haul are the campaign-wide tests' ({@link PacingTest},
 * {@link ActFloorTest}, {@link DensityTest}, {@link TypicalHaulTest}); the fight's measures {@link ChimeraFightTest}'s;
 * the level's static rules {@link CitadelChecks}' ({@link CitadelKeysTest}).
 */
class Level48Test {
    /** The level's key. */
    static final String LEVEL = "act-7-beyond-the-gate/level-48-vornes-end";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The plan's fitted load at Level 48, MW (Level 47's with the Swarm Tail at L5). */
    static final double LOAD = 25;

    /** The Mk VI "Choir Core"'s output, MW. */
    static final double OUTPUT = 30;

    /** The cores the secondary must burn per difficulty (of 10). */
    static final Map<Difficulty, Integer> TARGET =
            Map.of(Difficulty.EASY, 6, Difficulty.MEDIUM, 8, Difficulty.HARD, 10);

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 48 ({@code "48"} in design/player/balance-plan.yaml: the visit raises the Swarm Tail
     * to L5 and fits Rook's owned Mortar): Level 47's fit with the Swarm Tail L5, 25 of 30 MW, Rook flying the Mortar (L1)
     * for the citadel's ground.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "mortar",
                        1,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()));
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
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 5)),
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
     * The data: the mid-boss in the arena at t=186 (the sting; its rise out of the well at (240, 60), its rear sweep,
     * its last words, the capsule's own shield, Rook's order), the ten reactor cores (uncounted) and the secondary's
     * target per difficulty, the rear V-wings up in the lane away from the ship, the bottom band of 120 px.
     */
    @Test
    void theLevelSendsItsMidBossCoresAndRearWings() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            SetPieceSpec chimera = level.setPieces().stream()
                    .filter(SetPieceSpec::isBoss)
                    .findFirst()
                    .orElseThrow();
            BossSpec boss = chimera.boss().orElseThrow();
            assertEquals("vornes-chimera", chimera.slug());
            assertTrue(boss.midBoss(), "a mid-boss");
            assertEquals(186, boss.arriveSeconds(), 1e-9, "the sting at t=186");
            assertTrue(boss.rearSweep().isPresent()
                    && boss.lastWords().isPresent()
                    && boss.rise().isPresent());
            assertEquals(4.5, boss.lastWords().get().seconds(), 1e-9);
            assertEquals(60, vanguard.sim.PlayField.HEIGHT - boss.rise().get().fromY(), 1e-9, "the well's point");
            assertEquals(1, boss.partShields().size(), "the capsule's hex shield");
            assertEquals(3, boss.phases().size());
            int rearAway = 0;
            for (WaveSpec wave : level.waves()) {
                if (wave.lane() == WaveSpec.Lane.AWAY) {
                    rearAway++;
                    assertEquals(WaveSpec.Entry.REAR, wave.entry());
                }
            }
            assertTrue(rearAway == 6, difficulty + ": the rear V-wings come up away from the ship: " + rearAway);
            long cores = level.groundUnits().stream()
                    .filter(unit -> unit.enemy().slug().equals("reactor-core"))
                    .count();
            assertEquals(10, cores, difficulty + ": ten cores");
            assertFalse(
                    level.groundUnits().stream()
                            .filter(unit -> unit.enemy().slug().equals("reactor-core"))
                            .anyMatch(unit -> unit.enemy().countsAsEnemy()),
                    "the cores are not counted");
            assertEquals(
                    10 - TARGET.get(difficulty), level.objectives().secondary().escapesAllowed(), "allowed");
            assertEquals(120, level.vrell().bottomBand(), 1e-9, "the amber blink's band");
        }
    }

    /** What a run saw, over every attempt (the losses) and in its completing one. */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new ArrayList<>();
        /** Cores burnt and lost, in the completing attempt. */
        int burnt;

        int lost;
        /** The Chimera's fight, s from the bar to K (NaN: no K), in the completing attempt. */
        double fight = Double.NaN;
        /** Its rear sweeps, in the completing attempt. */
        int sweeps;
        /** Steps the climbing hull touched the ship, in the completing attempt. */
        int touched;

        @Override
        public void newAttempt() {
            burnt = 0;
            lost = 0;
            fight = Double.NaN;
            sweeps = 0;
            touched = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.SHIP_DESTROYED) {
                    deaths.add(String.format("%.1f@%.0f", sortie.levelSeconds(), events.x(i)));
                }
            }
            burnt = sortie.escapesDestroyed();
            lost = sortie.escapesLost();
            SetPiece piece = ChimeraPlay.chimeraBoss(sortie);
            if (piece == null) {
                return;
            }
            var sweep = piece.rearSweep().orElseThrow();
            sweeps = sweep.sweeps();
            if (sweep.climbing() && ChimeraFightTest.touching(sortie, piece)) {
                touched++;
            }
            if (Double.isNaN(fight)
                    && piece.lastWords().filter(words -> words.spoken()).isPresent()) {
                fight = piece.bossSeconds();
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

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 48 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            Watch watch = flight.watch();
            report.append(String.format(
                    "%n    %s: %s, retries %d, losses %s; fight %.1f s, sweeps %d, hull touches %d; cores burnt %d,"
                            + " away %d, %s; credits %d",
                    flight.at(),
                    flight.complete() ? "won" : "LOST",
                    flight.run().retries(),
                    watch.deaths,
                    watch.fight,
                    watch.sweeps,
                    watch.touched,
                    watch.burnt,
                    watch.lost,
                    flight.run().secondaryMet() ? "MET" : "failed",
                    flight.run().credits()));
        }
        for (Difficulty difficulty : Difficulty.values()) {
            long met = flights.stream()
                    .filter(f -> f.difficulty() == difficulty && f.run().secondaryMet())
                    .count();
            report.append(String.format("%n    %s: the secondary met %d of %d", difficulty, met, SEEDS));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            assertTrue(flight.complete(), flight.at() + ": completes");
        }
    }

    /** The autopilot wins the level with the plan's fit and Rook, on every difficulty and seed. */
    @Test
    void theAutopilotEndsVorneWithRook() {
        report("Rook", flights("Rook", Level48Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotEndsVorneWithoutRook() {
        report("--escort none", flights("--escort none", Level48Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level48Test::flyToEnd);

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
