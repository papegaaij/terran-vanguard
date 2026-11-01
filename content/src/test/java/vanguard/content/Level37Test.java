package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Ally;
import vanguard.sim.Armament;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M10 part C, step C3: Level 37 Fall of Aurelia from its data (design/campaign/act-6-jovian-storm/level-37-fall-of-aurelia;
 * the Mirror Interceptor, the Graft Turret, the Halcyon shuttles, the Vrell turn), flown by the autopilot ({@link
 * AureliaPlay}: the beam held on a mirror, the other guns only on its flank; a Graft Turret's root in its window; out of
 * the shuttles' band and their aimed Talons' lines, never guarding them) with the balance plan's fit ({@link
 * #planLoadout}), with Rook and without him ({@code --escort none}): its count per difficulty, the shuttles' secondary
 * per difficulty, the war's kills paying nothing, no hunter before the turn, Varga's and Okafor's lines with the
 * secondary. The pacing, the floor, the density, the beam era's HP and the haul are the campaign-wide tests' ({@link
 * PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}); the data as
 * the simulation gets it {@link FallOfAureliaDataTest}'s.
 */
class Level37Test {
    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-37-fall-of-aurelia";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;
    /** The Halcyon shuttles: three pairs, every difficulty. */
    static final int SHUTTLES = 6;

    /** The plan's fit's load, MW (the Mk V "Helix" gives 22): the beam's fifth level takes the last one. */
    static final double LOAD = 22;

    /** The turn (the level's {@code turn}): no hunter strikes its prey before it. */
    static final double TURN = 100;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 37 ({@code "37"} in design/player/balance-plan.yaml: the Ion Beam to L5 on Level
     * 36's fit, 22 of 22 MW, forward DPS 168 against the reference 148) with Rook in the slot: the Ion Beam L5, the Tesla
     * Coil Pod L2, the right Micro-missile Pod L4, the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP Burst.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}): Level 36's with the Ion Beam at L5. */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
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

    /**
     * Step C2's waves: the wave units counted as enemies 270 on easy, 348 at medium, 425 on hard (the levelReport's,
     * beside the 35 ground units: 27 Graft Turrets and 8 SAM Nests), the Mirror Interceptors 7, 10 and 12, and the six
     * shuttles on every difficulty.
     */
    @Test
    void theLevelHoldsItsCount() {
        Map<Difficulty, Integer> counted = new EnumMap<>(Difficulty.class);
        Map<Difficulty, Integer> mirrors = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            counted.put(
                    difficulty,
                    level.waves().stream()
                            .filter(wave -> wave.enemy().countsAsEnemy())
                            .mapToInt(WaveSpec::count)
                            .sum());
            mirrors.put(difficulty, count(level, "mirror-interceptor"));
            assertEquals(35, level.groundUnits().size(), difficulty + ": 27 Graft Turrets and 8 SAM Nests");
            assertEquals(
                    SHUTTLES,
                    level.objectives()
                            .airAllies()
                            .flatMap(allies -> allies.air())
                            .map(air -> air.stations().size())
                            .orElse(0),
                    difficulty + ": three pairs of shuttles");
        }
        System.out.println("Level 37 counted enemies: " + counted + ", Mirror Interceptors " + mirrors);
        assertEquals(Map.of(Difficulty.EASY, 270, Difficulty.MEDIUM, 348, Difficulty.HARD, 425), counted);
    }

    /** What one completing attempt saw. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** The shuttles lost: "t#k" each. */
        final List<String> lost = new ArrayList<>();
        /**
         * The share of a shuttle's armour each cause took, summed over the shuttles (1 = one shuttle's armour): a body's
         * slug (an air unit, or a Graft Turret firing from under the hull), a "bounced" player shot, a bullet from
         * "above" or "below".
         */
        final Map<String, Double> shuttleDamage = new TreeMap<>();

        private final double[] share = new double[SHUTTLES];
        /** The first prey hit (a hunter's bullet or ram on its prey), s; -1 for none. */
        double firstPreyHit = -1;
        /** Credits paid in the steps a prey unit was lost to the war (and nothing else died). */
        int preyPaid;
        /** When the met and failed lines, Rook's first-bounce and first-prey-kill barks went on the radio; -1 for none. */
        double metLine = -1;

        double failedLine = -1;
        double bounceLine = -1;
        double preyLine = -1;
        double met = -1;
        double failed = -1;
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new ArrayList<>();

        private final LevelScript level;
        /** Each shuttle's bullet (by where it came from) and body within {@link #NEAR} px after the last step. */
        private final String[] nearShot = new String[SHUTTLES];

        private final String[] nearBody = new String[SHUTTLES];
        private int credits;

        static final double NEAR = 40;

        Watch(Sortie sortie) {
            level = sortie.script();
            java.util.Arrays.fill(share, 1);
        }

        @Override
        public void newAttempt() {
            events.clear();
            lost.clear();
            shuttleDamage.clear();
            java.util.Arrays.fill(share, 1);
            firstPreyHit = -1;
            preyPaid = 0;
            metLine = -1;
            failedLine = -1;
            bounceLine = -1;
            preyLine = -1;
            met = -1;
            failed = -1;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            double t = sortie.levelSeconds();
            boolean preyLost = false;
            boolean killed = false;
            for (int i = 0; i < seen.size(); i++) {
                events.merge(seen.type(i), 1, Integer::sum);
                switch (seen.type(i)) {
                    case ALLY_HIT -> {
                        int k = seen.value(i);
                        String what = nearBody[k] != null ? nearBody[k] : nearShot[k] != null ? nearShot[k] : "other";
                        double now = sortie.ally(k).hpShare();
                        shuttleDamage.merge(what, share[k] - now, Double::sum);
                        share[k] = now;
                    }
                    case ALLY_LOST -> lost.add(String.format("%.1f#%d", t, seen.value(i)));
                    case PREY_HIT -> {
                        if (firstPreyHit < 0) {
                            firstPreyHit = t;
                        }
                    }
                    case PREY_KILLED -> preyLost = true;
                    case ENEMY_DESTROYED, ROOT_KILLED -> killed = true;
                    case OBJECTIVE_MET -> met = t;
                    case OBJECTIVE_FAILED -> failed = t;
                    case SHIP_DESTROYED -> deaths.add(String.format("%.1f@%.0f", t, seen.x(i)));
                    case RADIO -> {
                        CueTrigger trigger = level.radio().get(seen.value(i)).trigger();
                        switch (trigger) {
                            case SECONDARY_OBJECTIVE -> metLine = t;
                            case SECONDARY_FAILED -> failedLine = t;
                            case FIRST_BOUNCE -> bounceLine = t;
                            case FIRST_PREY_KILL -> preyLine = t;
                            default -> {}
                        }
                    }
                    default -> {}
                }
            }
            if (preyLost && !killed && met != t) {
                preyPaid += sortie.credits() - credits;
            }
            credits = sortie.credits();
            near(sortie);
        }

        /** Each shuttle's surroundings after this step, read at its hit in the next. */
        private void near(Sortie sortie) {
            for (int k = 0; k < Math.min(SHUTTLES, sortie.allyCount()); k++) {
                Ally ally = sortie.ally(k);
                nearShot[k] = null;
                nearBody[k] = null;
                if (!ally.alive()) {
                    continue;
                }
                for (int b = 0; b < sortie.bulletCount(); b++) {
                    EnemyBullet bullet = sortie.bullet(b);
                    if (Math.hypot(bullet.renderX(1) - ally.x(), bullet.renderY(1) - ally.y()) < NEAR) {
                        nearShot[k] = bullet.bounced() ? "bounced" : Math.cos(bullet.heading()) < 0 ? "above" : "below";
                    }
                }
                for (int e = 0; e < sortie.enemyCount(); e++) {
                    Enemy enemy = sortie.enemy(e);
                    if (enemy.layer().touchable()
                            && Math.hypot(enemy.renderX(1) - ally.x(), enemy.renderY(1) - ally.y()) < NEAR) {
                        nearBody[k] = enemy.spec().slug();
                    }
                }
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
            int home,
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
                sortie.alliesHome(),
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
                "%n    %s: retries %d, SHUTTLES %s (home %d, lost %s), credits %d; bounces %d, mirrors broken"
                        + " %d, roots killed %d of %d exposed, regrown %d; prey hits from %.1f, prey lost %d (paid %d);"
                        + " lines met %.1f failed %.1f bounce %.1f prey %.1f; secrets %d; losses %s",
                flight.at(),
                flight.run().retries(),
                flight.run().secondaryMet(),
                flight.home(),
                watch.lost,
                flight.run().credits(),
                watch.count(SimEvents.Type.MIRROR_BOUNCED),
                watch.count(SimEvents.Type.MIRROR_BROKEN),
                watch.count(SimEvents.Type.ROOT_KILLED),
                watch.count(SimEvents.Type.ROOT_EXPOSED),
                watch.count(SimEvents.Type.REGROWN),
                watch.firstPreyHit,
                watch.count(SimEvents.Type.PREY_KILLED),
                watch.preyPaid,
                watch.metLine,
                watch.failedLine,
                watch.bounceLine,
                watch.preyLine,
                flight.secretsFound(),
                watch.deaths);
    }

    /**
     * Each run's checks: completed; no hunter strikes its prey before the turn; the war's kills pay nothing; the met
     * line only with the secondary met and the failed line only with its failure, each in its step.
     */
    private static void check(Flight flight) {
        String at = flight.at();
        Watch watch = flight.watch();
        assertTrue(flight.complete(), at + ": completes");
        assertTrue(watch.firstPreyHit < 0 || watch.firstPreyHit >= TURN, at + ": a prey hit before the turn");
        assertEquals(0, watch.preyPaid, at + ": the war's kills paid");
        assertEquals(watch.failed, watch.failedLine, at + ": Okafor's line with the failure, only then");
        assertEquals(flight.run().secondaryMet(), watch.metLine >= 0, at + ": Aurelia Traffic's line with the met");
    }

    /**
     * The autopilot ({@link AureliaPlay}) completes the level with the plan's fit and Rook on every difficulty and seed;
     * the shuttles' secondary met on every easy and medium run (the README's tuning rule: 5 of 6 out in most medium
     * runs); on hard (all 6 out) at least {@link #HARD_HOME} home on every run, the secondary printed: met in 6 of 16
     * hard runs over 8 seeds with and without Rook (step C3, pending the user's review in round 75).
     */
    @Test
    void theAutopilotCrossesAureliaWithRook() {
        report("Rook", flights("Rook", Level37Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotCrossesAureliaWithoutRook() {
        report("--escort none", flights("--escort none", Level37Test::withoutRook));
    }

    /** The runs a difficulty's shuttles' secondary must be met on, of {@link #SEEDS}. */
    static final Map<Difficulty, Integer> MET =
            Map.of(Difficulty.EASY, SEEDS, Difficulty.MEDIUM, SEEDS, Difficulty.HARD, 0);

    /** The shuttles home on every hard run at least. */
    static final int HARD_HOME = 4;

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 37 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        Map<Difficulty, Map<String, Double>> damage = new EnumMap<>(Difficulty.class);
        for (Flight flight : flights) {
            flight.watch().shuttleDamage.forEach((what, share) -> damage.computeIfAbsent(
                            flight.difficulty(), d -> new TreeMap<>())
                    .merge(what, share, Double::sum));
        }
        damage.forEach((difficulty, shares) -> {
            report.append(String.format("%n    %s: the shuttles' armour taken over %d runs by", difficulty, SEEDS));
            shares.forEach((what, share) -> report.append(String.format(" %s %.2f", what, share)));
        });
        System.out.println(report);
        Map<Difficulty, Integer> met = new EnumMap<>(Difficulty.class);
        for (Flight flight : flights) {
            check(flight);
            if (flight.difficulty() == Difficulty.HARD) {
                assertTrue(flight.home() >= HARD_HOME, flight.at() + ": " + flight.home() + " shuttles home");
            }
            met.merge(flight.difficulty(), flight.run().secondaryMet() ? 1 : 0, Integer::sum);
        }
        met.forEach((difficulty, runs) -> assertTrue(
                runs >= MET.get(difficulty), fit + " " + difficulty + ": SHUTTLES met on " + runs + " runs"));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level37Test::flyToEnd);

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
