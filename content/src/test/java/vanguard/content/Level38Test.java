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
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Lightning;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M10 part D, step D3: Level 38 Lightning Belt from its data (design/campaign/act-6-jovian-storm/level-38-lightning-belt;
 * the Harrow, the Buzzsaw's return, the strikes with their lures, the two hunts, Aurelia's cargo pod and the CDF supply
 * canister), flown by the autopilot ({@link StormPlay}'s lines on {@link VentPlay}'s planned moves, out of the told columns:
 * the Harrows taken at range before their tell, a ram's last moments sidestepped, never back into a ring, the
 * canister's latch shot under its brackets, the pod opened) with the balance plan's fit ({@link #planLoadout}: Level 37's, the
 * plan's L38 visit buys nothing), with Rook and without him ({@code --escort none}): its count per difficulty, the
 * Harrows' secondary ({@code HARROWS n / 23}) per difficulty, the caps (design Waves: 6 Harrows, 2 in their tell or ram;
 * 6 Buzzsaws, 2 dashing; never two dashes and two rams at once; 3 Hornets, 16 homing missiles; no strike over a
 * Buzzsaw's or a Hornet's tell) on the flown runs, the war's kills paying nothing, the lures' kills counting, the secret
 * (shot or struck) and the canister's armour patch. The pacing, the floor, the density, the beam era's HP and the haul
 * are the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest},
 * {@link TypicalHaulTest}); the data as the simulation gets it {@link LightningBeltDataTest}'s.
 */
class Level38Test {
    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-38-lightning-belt";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 38 ({@code "38"} in design/player/balance-plan.yaml: the visit buys nothing, saving
     * for the Crucible; Level 37's fit, 22 of 22 MW) with Rook in the slot: the Ion Beam L5, the Tesla Coil Pod L2, the
     * right Micro-missile Pod L4, the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP Burst.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level37Test.planLoadout(content, difficulty);
    }

    /** The plan's fit without Rook ({@code --escort none}): Level 37's. */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level37Test.withoutRook(content, difficulty);
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

    /**
     * Step D2's waves: the wave units counted as enemies 298 on easy, 375 at medium, 448 on hard (the levelReport's; no
     * ground), the Harrows 31, 35 and 35 (the secondary's target 65 % of them, rounded up: 21, 23, 23), the Buzzsaws 17
     * at medium.
     */
    @Test
    void theLevelHoldsItsCount() {
        Map<Difficulty, Integer> counted = new EnumMap<>(Difficulty.class);
        Map<Difficulty, Integer> harrows = new EnumMap<>(Difficulty.class);
        Map<Difficulty, Integer> targets = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            counted.put(
                    difficulty,
                    level.waves().stream()
                            .filter(wave -> wave.enemy().countsAsEnemy())
                            .mapToInt(WaveSpec::count)
                            .sum());
            harrows.put(difficulty, count(level, "harrow"));
            targets.put(
                    difficulty,
                    sortie(content, planLoadout(content, difficulty), 1, difficulty, false)
                            .requiredKills());
            assertEquals(0, level.groundUnits().size(), difficulty + ": no ground");
        }
        System.out.println(
                "Level 38 counted enemies: " + counted + ", Harrows " + harrows + ", HARROWS target " + targets);
        assertEquals(Map.of(Difficulty.EASY, 298, Difficulty.MEDIUM, 375, Difficulty.HARD, 448), counted);
        assertEquals(35, harrows.get(Difficulty.MEDIUM), "35 Harrows at medium");
        harrows.forEach((difficulty, n) -> assertEquals(
                (int) Math.ceil(0.65 * n), targets.get(difficulty), difficulty + ": 65 % of the Harrows flown"));
        assertEquals(23, targets.get(Difficulty.MEDIUM), "HARROWS n / 23 at medium");
        assertEquals(17, count(SimSpecs.level(content, LEVEL, Difficulty.MEDIUM), "buzzsaw-drone"), "17 Buzzsaws");
    }

    /** What one completing attempt saw (the caps' counts are {@link LevelMetrics.Stacking}'s). */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** Credits paid in the steps a prey unit was lost to the war (and nothing else died). */
        int preyPaid;
        /** The Harrows a strike killed (in its column, in its step). */
        int harrowStrikeKills;
        /** The Harrows that died on the ship's hull (a ram or a contact). */
        int rams;
        /** How the secret was found: "shot", "struck", or "" for not found. */
        String secret = "";
        /** When the canister let its armour patch fall, s; -1 for not. */
        double patchReleased = -1;
        /** Whether the ship took the armour patch. */
        boolean patchTaken;
        /** The most Hornets and homing missiles on the screen at once. */
        int hornets;

        int missiles;
        /** When the met line went on the radio, s; -1 for none; when the secondary was met, s. */
        double metLine = -1;

        double met = -1;
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new ArrayList<>();

        private final LevelScript level;
        private final int armourPatch = PickupType.ARMOUR_PATCH.ordinal();
        private int credits;

        Watch(Sortie sortie) {
            level = sortie.script();
        }

        @Override
        public void newAttempt() {
            events.clear();
            preyPaid = 0;
            harrowStrikeKills = 0;
            rams = 0;
            secret = "";
            patchReleased = -1;
            patchTaken = false;
            hornets = 0;
            missiles = 0;
            metLine = -1;
            met = -1;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            double t = sortie.levelSeconds();
            boolean preyLost = false;
            boolean killed = false;
            boolean struck = false;
            for (int i = 0; i < seen.size(); i++) {
                struck |= seen.type(i) == SimEvents.Type.STRIKE;
            }
            for (int i = 0; i < seen.size(); i++) {
                SimEvents.Type type = seen.type(i);
                events.merge(type, 1, Integer::sum);
                switch (type) {
                    case PREY_KILLED -> preyLost = true;
                    case ENEMY_DESTROYED -> {
                        killed = true;
                        EnemySpec kind = sortie.enemyKinds().get(seen.value(i));
                        if (kind.slug().equals("harrow")) {
                            if (struck && inStrike(sortie, seen, seen.x(i), kind)) {
                                harrowStrikeKills++;
                            } else if (Math.abs(seen.x(i) - sortie.ship().x()) < 30
                                    && Math.abs(seen.y(i) - sortie.ship().y()) < 30) {
                                rams++;
                            }
                        }
                    }
                    case SECRET_FOUND -> secret = struck ? "struck" : "shot";
                    case DEBRIS_RELEASED -> {
                        if (seen.value(i) == armourPatch) {
                            patchReleased = t;
                        }
                    }
                    case PICKUP_COLLECTED -> patchTaken |= seen.value(i) == armourPatch;
                    case OBJECTIVE_MET -> met = t;
                    case SHIP_DESTROYED -> deaths.add(String.format("%.1f@%.0f", t, seen.x(i)));
                    case RADIO -> {
                        if (level.radio().get(seen.value(i)).trigger() == CueTrigger.SECONDARY_OBJECTIVE) {
                            metLine = t;
                        }
                    }
                    default -> {}
                }
            }
            if (preyLost && !killed && met != t) {
                preyPaid += sortie.credits() - credits;
            }
            credits = sortie.credits();
            int h = 0;
            for (int e = 0; e < sortie.enemyCount(); e++) {
                Enemy enemy = sortie.enemy(e);
                double y = enemy.renderY(1);
                if (enemy.spec().slug().equals("hornet") && y > 0 && y < PlayField.HEIGHT) {
                    h++;
                }
            }
            hornets = Math.max(hornets, h);
            missiles = Math.max(missiles, sortie.missileCount());
        }

        /** Whether a kill at {@code x} lies in the column of a strike that struck in this step. */
        private static boolean inStrike(Sortie sortie, SimEvents seen, double x, EnemySpec kind) {
            Lightning lightning = sortie.lightning().orElseThrow();
            for (int i = 0; i < seen.size(); i++) {
                if (seen.type(i) == SimEvents.Type.STRIKE
                        && Math.abs(lightning.x(seen.value(i)) - x)
                                <= lightning.width() / 2 + kind.hitbox().width() / 2.0 + 1) {
                    return true;
                }
            }
            return false;
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
            LevelMetrics.Dashers dashers,
            int kills,
            int required) {
        String at() {
            return fit + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false);
        Watch watch = new Watch(sortie);
        LevelMetrics.Stacking stacking = new LevelMetrics.Stacking(content);
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch, stacking);
        return new Flight(
                fit,
                difficulty,
                seed,
                complete,
                LevelMetrics.Run.of(sortie),
                watch,
                stacking.dashers(),
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
        Watch watch = flight.watch();
        LevelMetrics.Dashers d = flight.dashers();
        return String.format(
                "%n    %s: retries %d, HARROWS %d / %d %s, credits %d; strikes on the ship %d, rams %d, Harrows struck"
                        + " %d; caps: Harrows %d (%d in tell or ram), Buzzsaws %d (%d dashing), both %.1f s, Hornets %d,"
                        + " missiles %d, %d rings; prey lost %d (paid %d); secret %s, patch %.1f (taken %b); losses %s",
                flight.at(),
                flight.run().retries(),
                flight.kills(),
                flight.required(),
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.run().credits(),
                watch.count(SimEvents.Type.STRIKE_HIT),
                watch.rams,
                watch.harrowStrikeKills,
                d.harrows(),
                d.harrowsTelling(),
                d.buzzsaws(),
                d.buzzsawsDashing(),
                d.bothSeconds(),
                watch.hornets,
                watch.missiles,
                d.rings(),
                watch.count(SimEvents.Type.PREY_KILLED),
                watch.preyPaid,
                watch.secret.isEmpty() ? "-" : watch.secret,
                watch.patchReleased,
                watch.patchTaken,
                watch.deaths);
    }

    /**
     * Each run's checks: completed; the caps held (design Waves; easy at most 4 Harrows on the screen, their page); no
     * strike over a Buzzsaw's spin-up or a Hornet's hatch tell; the war's kills pay nothing; the met line only with the
     * secondary met; the cargo pod opened (shot, or by the t=122.5 strike on medium and hard).
     */
    private static void check(Flight flight) {
        String at = flight.at();
        Watch watch = flight.watch();
        LevelMetrics.Dashers d = flight.dashers();
        assertTrue(flight.complete(), at + ": completes");
        int harrowCap = flight.difficulty() == Difficulty.EASY ? 4 : 6;
        assertTrue(d.harrows() <= harrowCap, at + ": " + d.harrows() + " Harrows on the screen");
        assertTrue(d.harrowsTelling() <= 2, at + ": " + d.harrowsTelling() + " Harrows in their tell or ram");
        assertTrue(d.buzzsaws() <= 6, at + ": " + d.buzzsaws() + " Buzzsaws on the screen");
        assertTrue(d.buzzsawsDashing() <= 2, at + ": " + d.buzzsawsDashing() + " Buzzsaws dashing");
        assertEquals(0, d.bothSeconds(), at + ": two dashes and two rams at once");
        assertTrue(d.strikesOverBuzzsawTells().isEmpty(), at + ": strikes over a Buzzsaw's tell");
        assertTrue(d.strikesOverHornetTells().isEmpty(), at + ": strikes over a Hornet's tell");
        assertTrue(watch.hornets <= 3, at + ": " + watch.hornets + " Hornets on the screen");
        assertTrue(watch.missiles <= 16, at + ": " + watch.missiles + " homing missiles");
        assertEquals(0, watch.preyPaid, at + ": the war's kills paid");
        assertEquals(flight.run().secondaryMet(), watch.metLine >= 0, at + ": Varga's line with the met");
        assertTrue(!watch.secret.isEmpty(), at + ": the cargo pod opened");
    }

    /**
     * The autopilot ({@link StormPlay}) completes the level with the plan's fit and Rook on every difficulty and seed,
     * within the caps; {@code HARROWS} met in most medium runs; the lures' strikes kill Harrows that count; the secret
     * found on every run (shot, or opened by the t=122.5 strike on medium and hard); the canister's armour patch
     * released on most runs of each difficulty ({@link #PATCH_RUNS}).
     */
    @Test
    void theAutopilotCrossesTheStormWithRook() {
        report("Rook", flights("Rook", Level38Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotCrossesTheStormWithoutRook() {
        report("--escort none", flights("--escort none", Level38Test::withoutRook));
    }

    /** The medium runs {@code HARROWS} must be met on, of {@link #SEEDS}: most. */
    static final int MEDIUM_MET = 2;

    /**
     * The runs of each difficulty the CDF canister's latch must be shot on (its armour patch released), of {@link
     * #SEEDS}: most. The plan's fit has no straight gun for it: the Ion Beam passes debris (only shots break a chunk),
     * the Tesla Coil's arcs seek units, so the micro-missiles crossing it and Rook's cannon break it (step D3, pending the
     * user's review in round 76: QD-4).
     */
    static final int PATCH_RUNS = 2;

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 38 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        int mediumMet = 0;
        int strikeKills = 0;
        Map<Difficulty, Integer> patches = new EnumMap<>(Difficulty.class);
        for (Flight flight : flights) {
            check(flight);
            if (flight.difficulty() == Difficulty.MEDIUM) {
                mediumMet += flight.run().secondaryMet() ? 1 : 0;
            }
            strikeKills += flight.watch().harrowStrikeKills;
            patches.merge(flight.difficulty(), flight.watch().patchReleased >= 0 ? 1 : 0, Integer::sum);
        }
        assertTrue(mediumMet >= MEDIUM_MET, fit + ": HARROWS met on " + mediumMet + " medium runs");
        assertTrue(strikeKills > 0, fit + ": no Harrow killed by a strike");
        patches.forEach((difficulty, runs) -> assertTrue(
                runs >= PATCH_RUNS, fit + " " + difficulty + ": the canister's patch released on " + runs + " runs"));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level38Test::flyToEnd);

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
