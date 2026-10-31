package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.CueTrigger;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M10 part B, step B3: Level 36 Cloud Divers from its data (design/campaign/act-6-jovian-storm/level-36-cloud-divers;
 * the Chimera, the harvester, the fleet Stormhawk), flown by the autopilot ({@link DivePlay}: the beam held on one
 * Chimera until it dies, the harvesters before they get away, out of a told strike column; the drop craft never a
 * target) with the balance plan's fit ({@link #planLoadout}: part A's Tesla Coil Pod L2 on the left wing, pending the
 * user's review in round 73), with Rook and without him ({@code --escort none}): its count per difficulty, the
 * harvesters' secondary per difficulty, Varga's failed-secondary line at the failure, the drop craft never hit, no
 * strike on the ship or Rook, the pod's crate. The pacing, the floor, the density, the beam era's HP and the haul are
 * the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest},
 * {@link TypicalHaulTest}); the data as the simulation gets it {@link CloudDiversDataTest}'s.
 */
class Level36Test {
    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-36-cloud-divers";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;
    /** The harvesters (four pairs) and the drop craft (two pairs), every difficulty. */
    static final int HARVESTERS = 8;

    static final int DROP_CRAFT = 4;

    /** The plan's fit's load, MW (the Mk V "Helix" gives 22). */
    static final double LOAD = 21;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 36 ({@code "36"} in design/player/balance-plan.yaml: the Tesla Coil Pod L2 on the
     * left wing in place of the Micro-missile Pod L4, part A's alternative to the hand plan's beam L5, pending the user's
     * review in round 73) with Rook in the slot: the Ion Beam L4, the Tesla Coil Pod L2, the right Micro-missile Pod L4,
     * the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP Burst, 21 of 22 MW.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        content.shields().models().get(2).name(),
                        content.armour().plating().get(4).name(),
                        Level32Test.SPARE_POWER,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(
                        content,
                        Level35Test.OUTPUT,
                        LOAD,
                        content.shields().models().get(2).name(),
                        difficulty));
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

    private static int count(LevelScript level, String slug) {
        return level.waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .mapToInt(WaveSpec::count)
                .sum();
    }

    private static int kind(Sortie sortie, String slug) {
        List<EnemySpec> kinds = sortie.enemyKinds();
        for (int k = 0; k < kinds.size(); k++) {
            if (kinds.get(k).slug().equals(slug)) {
                return k;
            }
        }
        return -1;
    }

    /**
     * Step B2's waves: the units counted as enemies 252 on easy, 315 at medium, 382 on hard (the levelReport's), with the
     * eight harvesters and the four drop craft on every difficulty beside them, counted as none.
     */
    @Test
    void theLevelHoldsItsCount() {
        Map<Difficulty, Integer> counted = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            counted.put(
                    difficulty,
                    level.waves().stream()
                            .filter(wave -> wave.enemy().countsAsEnemy())
                            .mapToInt(WaveSpec::count)
                            .sum());
            assertEquals(HARVESTERS, count(level, "harvester"), difficulty + ": four pairs of harvesters");
            assertEquals(DROP_CRAFT, count(level, "fleet-stormhawk"), difficulty + ": two pairs of drop craft");
            assertEquals(0, level.groundUnits().size(), difficulty + ": no ground");
        }
        System.out.println("Level 36 counted enemies: " + counted);
        assertEquals(Map.of(Difficulty.EASY, 252, Difficulty.MEDIUM, 315, Difficulty.HARD, 382), counted);
    }

    /** What one completing attempt saw. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** The harvesters away: "t" each; killed: "t@x". */
        final List<String> away = new ArrayList<>();

        final List<String> killed = new ArrayList<>();
        /** Strikes on the ship (1) and Rook (2); enemies a strike killed (the first-strike-kill line's). */
        int strikeShip;

        int strikeRook;
        /** When the failed-secondary line, the first strike kill's line and the secret's line went on the radio. */
        double failedLine = -1;

        double strikeLine = -1;
        double failed = -1;

        final List<String> hits = new ArrayList<>();
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new ArrayList<>();

        private final LevelScript level;
        private final int harvester;
        private int lost;

        Watch(Sortie sortie) {
            level = sortie.script();
            harvester = kind(sortie, "harvester");
        }

        @Override
        public void newAttempt() {
            events.clear();
            away.clear();
            killed.clear();
            strikeShip = 0;
            strikeRook = 0;
            failedLine = -1;
            strikeLine = -1;
            failed = -1;
            lost = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            double t = sortie.levelSeconds();
            for (int i = 0; i < seen.size(); i++) {
                events.merge(seen.type(i), 1, Integer::sum);
                switch (seen.type(i)) {
                    case ENEMY_DESTROYED -> {
                        if (seen.value(i) == harvester) {
                            killed.add(String.format("%.1f@%.0f", t, seen.x(i)));
                        }
                    }
                    case STRIKE_HIT -> {
                        hits.add(String.format(
                                "%.2f@%.0f %s ship %.0f,%.0f%s",
                                t,
                                seen.x(i),
                                seen.value(i) == 1 ? "ship" : "Rook",
                                sortie.ship().x(),
                                sortie.ship().y(),
                                sortie.wingman()
                                        .map(w -> String.format(" rook %.0f,%.0f", w.x(), w.y()))
                                        .orElse("")));
                        if (seen.value(i) == 1) {
                            strikeShip++;
                        } else {
                            strikeRook++;
                        }
                    }
                    case OBJECTIVE_FAILED -> failed = t;
                    case SHIP_DESTROYED -> deaths.add(String.format("%.1f@%.0f", t, seen.x(i)));
                    case RADIO -> {
                        CueTrigger trigger = level.radio().get(seen.value(i)).trigger();
                        if (trigger == CueTrigger.SECONDARY_FAILED) {
                            failedLine = t;
                        } else if (trigger == CueTrigger.FIRST_STRIKE_KILL) {
                            strikeLine = t;
                        }
                    }
                    default -> {}
                }
            }
            if (sortie.escapesLost() > lost) {
                away.add(String.format("%.1f", t));
                lost = sortie.escapesLost();
            }
        }

        int count(SimEvents.Type type) {
            return events.getOrDefault(type, 0);
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
            int secretsFound) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch(sortie);
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch);
        return new Flight(
                fit,
                difficulty,
                seed,
                complete,
                LevelMetrics.Run.of(sortie),
                watch,
                complete ? sortie.result().secretsFound() : 0);
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
                "%n    %s: retries %d, HARVESTERS %s (killed %s, away %s), credits %d; strikes on the ship %d, Rook %d,"
                        + " strike-kill line %.1f; drop craft left %d, down %d; secrets %d; losses %s %s",
                flight.at(),
                flight.run().retries(),
                flight.run().secondaryMet(),
                watch.killed,
                watch.away,
                flight.run().credits(),
                watch.strikeShip,
                watch.strikeRook,
                watch.strikeLine,
                watch.count(SimEvents.Type.NEUTRAL_LEFT),
                watch.count(SimEvents.Type.NEUTRAL_DOWN),
                flight.secretsFound(),
                watch.deaths,
                watch.hits);
    }

    /**
     * Each run's checks: completed; the drop craft all left unharmed (none down); no strike on the ship or Rook; the
     * failed-secondary line only with the failure, in its step.
     */
    private static void check(Flight flight) {
        String at = flight.at();
        Watch watch = flight.watch();
        assertTrue(flight.complete(), at + ": completes");
        assertEquals(0, watch.count(SimEvents.Type.NEUTRAL_DOWN), at + ": a drop craft destroyed");
        assertEquals(0, watch.strikeShip, at + ": a strike on the ship");
        assertEquals(0, watch.strikeRook, at + ": a strike on Rook");
        assertEquals(watch.failed, watch.failedLine, at + ": Varga's line with the failure, only then");
    }

    /**
     * The autopilot ({@link DivePlay}) completes the level with the plan's fit and Rook on every difficulty and seed and
     * meets {@code HARVESTERS} on every run (7 of 8 on hard), the drop craft untouched, no strike on the ship or Rook.
     */
    @Test
    void theAutopilotDivesWithRook() {
        report("Rook", flights("Rook", Level36Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotDivesWithoutRook() {
        report("--escort none", flights("--escort none", Level36Test::withoutRook));
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 36 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        Map<Difficulty, Integer> met = new EnumMap<>(Difficulty.class);
        for (Flight flight : flights) {
            check(flight);
            met.merge(flight.difficulty(), flight.run().secondaryMet() ? 1 : 0, Integer::sum);
        }
        met.forEach((difficulty, runs) ->
                assertEquals(SEEDS, runs, fit + " " + difficulty + ": HARVESTERS met on every run"));
    }

    /**
     * Varga's "Too many got through" waits for the secondary's failure ({@code secondary-failed}, step B3), not the first
     * harvester away: flown invulnerable without a shot (the ship in the bottom-left corner, Rook not fitted), at medium
     * the first two get away ({@code allowed: 2}) and the third fails it; the line plays once, in that step.
     */
    @Test
    void vargasLinePlaysWhenTheThirdHarvesterGetsAway() {
        Sortie sortie = sortie(content, withoutRook(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, true);
        List<Double> away = new ArrayList<>();
        List<Double> lines = new ArrayList<>();
        double failed = -1;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            sortie.step(MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), 30, 30));
            SimEvents seen = sortie.events();
            double t = sortie.levelSeconds();
            for (int i = 0; i < seen.size(); i++) {
                if (seen.type(i) == SimEvents.Type.OBJECTIVE_FAILED) {
                    failed = t;
                } else if (seen.type(i) == SimEvents.Type.RADIO
                        && sortie.script().radio().get(seen.value(i)).trigger() == CueTrigger.SECONDARY_FAILED) {
                    lines.add(t);
                }
            }
            if (sortie.escapesLost() > away.size()) {
                away.add(t);
            }
        }
        System.out.printf(
                "Level 36 no shot: harvesters away at %s, failed %.1f, Varga's line %s%n", away, failed, lines);
        assertTrue(sortie.complete());
        assertEquals(3, away.size(), "two allowed, the third fails it: " + away);
        assertEquals(away.get(2), failed, 1e-9, "the third away fails it");
        assertEquals(List.of(failed), lines, "the line once, as it fails");
        assertTrue(!sortie.result().secondaryMet());
    }

    /**
     * The t=138 strike through the t=136.5 Chimera wing's lane kills and pays (a special's kill) and calls Rook's
     * {@code first-strike-kill} bark: flown invulnerable without a shot, the ship low in the middle (the column's x 240,
     * the wing swooping at it), Rook beside it, at medium.
     */
    @Test
    void theFirstStrikeKillsInTheChimeraLaneAndPays() {
        Sortie sortie = sortie(content, planLoadout(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, true);
        int steps = 0;
        int killed = 0;
        int paid = 0;
        double line = -1;
        while (!sortie.complete() && steps++ < MAX_STEPS && sortie.levelSeconds() < 140) {
            int credits = sortie.credits();
            sortie.step(MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), PlayField.WIDTH / 2.0, 40));
            SimEvents seen = sortie.events();
            boolean struck = seen.count(SimEvents.Type.STRIKE) > 0;
            for (int i = 0; i < seen.size(); i++) {
                if (struck && seen.type(i) == SimEvents.Type.ENEMY_DESTROYED) {
                    killed++;
                } else if (seen.type(i) == SimEvents.Type.RADIO
                        && sortie.script().radio().get(seen.value(i)).trigger() == CueTrigger.FIRST_STRIKE_KILL) {
                    line = sortie.levelSeconds();
                }
            }
            if (struck && sortie.levelSeconds() < 139) {
                paid += sortie.credits() - credits;
            }
        }
        System.out.printf(
                "Level 36 the first strike: %d killed, %d credits, Rook's bark at %.2f%n", killed, paid, line);
        assertTrue(killed > 0, "the strike kills in the Chimera lane");
        assertTrue(paid > 0, "a strike kill pays");
        assertTrue(line >= 138 && line < 139, "Rook's bark with it: " + line);
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level36Test::flyToEnd);

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
