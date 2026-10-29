package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Branch;
import vanguard.sim.Command;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M9 part E, step E3: Level 32 Deep Core from its data (design/campaign/act-5-the-belt/level-32-deep-core; the units in
 * design/enemies: the Sentinel Tower and the Strider (new), the SAM Nest, Talon, Dart Drone, Skitter, Needler, Mote
 * Swarm), on each of its four routes ({@link RoutePlay}: a side per fork, LL, LR, RL, RR): its count per route and
 * difficulty, the forks' branches (a branch's waves fly and its ground units and objects wake only when its side is
 * chosen, the other side's stay dormant), the Sentinels' sweeps (the telegraph before every sweep, the full turn every
 * third at medium, every second on hard, none on easy) and their hardened collar (shots glancing off it, the emitter
 * the target: {@link TowerPlay}), the Striders' topples (a lost leg's stagger, fall and crush of ground units only,
 * never a ground object) and their pod's salvo (silenced with the pod), the mining charges (80 in 64 px, no chain), the
 * blast doors' locks and caches and the secret, {@code TOWERS DOWN}, the radio's {@code branch} lines (the chosen
 * side's only) and Rook's, the special limits ({@code IN ROCK}), and whole runs of the autopilot on every route with
 * the balance plan's fit, with Rook and without him. The pacing, the floor, the density, the beam era's HP and the haul
 * are the campaign-wide tests' per route ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link
 * BeamEraHpTest}, {@link TypicalHaulTest}); the mechanics themselves the simulation's ({@code ForksTest}, {@code
 * GroundSweepTest}, {@code ToppleTest}, {@code ChargeTest} and their kin).
 */
class Level32Test {
    static final String LEVEL = "act-5-the-belt/level-32-deep-core";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each route is flown on per difficulty with Rook. */
    static final int SEEDS = 3;
    /** The four routes, fork one's side first. */
    static final List<String> ROUTES = List.of("LL", "LR", "RL", "RR");
    /** The L32 visit uses the last of the Mk IV's 20 MW (19.5 at L31 + the Swarm Tail L3's 0.5). */
    static final double SPARE_POWER = 0;
    /** The richer side of each fork (the README's forks: fork one's charge seam, fork two's cooling ducts). */
    static final String RICHER = "RL";
    /** Each route's typical haul lies within this share of the budget (the README's ±8 %; the mean ±5 %). */
    static final double ROUTE_TOLERANCE = 0.08;
    /** A branch cache's crate and the secret's, Act 1 terms (× the act factor 6.5536 in Act 5). */
    static final int CACHE_CRATE = 50;

    static final int SECRET_CRATE = 100;
    /** {@code TOWERS DOWN}: 100 in Act 1 terms. */
    static final int TOWERS_DOWN_PAY = 100;
    /** A mining charge's burst: radius px, damage. */
    static final double CHARGE_RADIUS = 64;

    static final double CHARGE_DAMAGE = 80;

    private final Content content = ContentLoader.fromClasspath();

    /** The plan's fit with Rook in the slot (the Autocannon L1, at full armour). */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /**
     * The balance plan's fit at Level 32 ({@code "32"}: Level 31's with the Swarm Tail L3) without Rook ({@code --escort
     * none}): the Ion Beam L4, the Micro-missile Pods L3 ×2, the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP
     * Burst (the Airstrike is forbidden in the rock).
     */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        content.shields().models().get(2).name(),
                        content.armour().plating().get(4).name(),
                        SPARE_POWER,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1));
    }

    static Sortie sortie(
            Content content, Loadout loadout, long seed, Difficulty difficulty, boolean invulnerable, String route) {
        Rules rules = SimSpecs.rules(content, LEVEL, difficulty);
        return RoutePlay.fly(
                new Sortie(
                        seed,
                        loadout,
                        SimSpecs.level(content, LEVEL, difficulty),
                        invulnerable ? rules.withInvulnerableShip() : rules,
                        loadout.plating().maxArmour()),
                route);
    }

    /** Whether {@code branch} flies on {@code route}. */
    private static boolean on(String route, Optional<Branch> branch) {
        return RoutePlay.on(route, branch);
    }

    private static int count(LevelScript level, String route, String slug) {
        int waves = level.waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug) && on(route, wave.branch()))
                .mapToInt(WaveSpec::count)
                .sum();
        int ground = (int) level.groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals(slug) && on(route, unit.branch()))
                .count();
        return waves + ground;
    }

    private static long objects(LevelScript level, String route, java.util.function.Predicate<GroundObjectSpec> kind) {
        return level.groundObjects().stream()
                .filter(object -> kind.test(object) && on(route, object.branch()))
                .count();
    }

    private static boolean charge(GroundObjectSpec object) {
        return object.blast().isPresent();
    }

    /** The level's two forks give its four routes; the playthrough's route is the mixed one nearest the routes' mean. */
    @Test
    void theForksGiveFourRoutesAndThePlaythroughFliesTheMixedOneNearestTheMean() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            assertEquals(2, RoutePlay.forks(level), difficulty.name());
            assertEquals(ROUTES, RoutePlay.routes(level), difficulty.name());
        }
        Map<String, Double> hauls = new LinkedHashMap<>();
        double mean = 0;
        double budget = 0;
        for (String route : ROUTES) {
            TypicalHaul haul = TypicalHaul.of(content, LEVEL, Difficulty.MEDIUM, route);
            hauls.put(route, haul.typical());
            mean += haul.typical() / ROUTES.size();
            budget = haul.budget();
            assertTrue(
                    Math.abs(haul.offBudget()) <= ROUTE_TOLERANCE,
                    route + ": typical " + haul.typical() + " vs " + haul.budget());
        }
        assertEquals(mean, TypicalHaul.of(content, LEVEL).typical(), 1e-6, "the level's haul is the routes' mean");
        String nearest = null;
        for (String route : ROUTES) {
            // mixed: one fork's richer side and the other's safer
            boolean mixed = (route.charAt(0) == RICHER.charAt(0)) != (route.charAt(1) == RICHER.charAt(1));
            if (mixed && (nearest == null || Math.abs(hauls.get(route) - mean) < Math.abs(hauls.get(nearest) - mean))) {
                nearest = route;
            }
        }
        System.out.printf("Level 32 typical hauls by route %s, mean %.0f, budget %.0f%n", hauls, mean, budget);
        assertEquals(nearest, RoutePlay.playthrough(LEVEL), "the playthrough's route");
        double richest = hauls.get(RICHER);
        assertTrue(hauls.values().stream().allMatch(haul -> haul <= richest), "the richer channels pay more: " + hauls);
    }

    /**
     * The README's waves at medium, every branch together (its totals: Skitter 97, Talon 168, Mote Swarm 80, Dart Drone
     * 36, Needler 32, Strider 8), and per route and difficulty (easy and hard by the formation lever); the ground
     * targets per route: the four Sentinels on every route (none in a fork's channel), the SAM Nests (the hall's pair
     * and the gantry's trio, the charge seam's pair, the cooling ducts' four), the mining charges (2 + 4, and 3 in each
     * richer channel), a cache in each richer channel, the secret's lock, the repair crate.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        String[] slugs = {"skitter", "talon", "mote-swarm", "dart-drone", "needler", "strider"};
        LevelScript medium = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Integer> all = new ArrayList<>();
        for (String slug : slugs) {
            all.add(count(medium, RoutePlay.NONE, slug));
        }
        assertEquals(List.of(97, 168, 80, 36, 32, 8), all, "every branch at medium: " + List.of(slugs));
        Map<String, List<Integer>> expected = Map.ofEntries(
                Map.entry("EASY LL", List.of(59, 127, 54, 24, 18, 7)),
                Map.entry("EASY LR", List.of(59, 125, 60, 21, 22, 7)),
                Map.entry("EASY RL", List.of(55, 135, 48, 27, 12, 8)),
                Map.entry("EASY RR", List.of(55, 133, 54, 24, 16, 8)),
                Map.entry("MEDIUM LL", List.of(72, 155, 72, 32, 26, 7)),
                Map.entry("MEDIUM LR", List.of(72, 152, 80, 28, 32, 7)),
                Map.entry("MEDIUM RL", List.of(67, 165, 64, 36, 18, 8)),
                Map.entry("MEDIUM RR", List.of(67, 162, 72, 32, 24, 8)),
                Map.entry("HARD LL", List.of(85, 183, 90, 40, 34, 7)),
                Map.entry("HARD LR", List.of(85, 179, 100, 35, 42, 7)),
                Map.entry("HARD RL", List.of(79, 195, 80, 45, 24, 8)),
                Map.entry("HARD RR", List.of(79, 191, 90, 40, 32, 8)));
        Map<String, List<Integer>> counted = new LinkedHashMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            for (String route : ROUTES) {
                List<Integer> counts = new ArrayList<>();
                for (String slug : slugs) {
                    counts.add(count(level, route, slug));
                }
                counted.put(difficulty + " " + route, counts);
                boolean seam = route.charAt(0) == 'R';
                boolean ducts = route.charAt(1) == 'L';
                String at = difficulty + " " + route;
                assertEquals(4, count(level, route, "sentinel-tower"), at + ": the four Sentinels");
                assertEquals(5 + (seam ? 2 : 0) + (ducts ? 4 : 0), count(level, route, "sam-nest"), at + ": SAM Nests");
                assertEquals(6 + (seam ? 3 : 0) + (ducts ? 3 : 0), objects(level, route, Level32Test::charge), at);
                assertEquals((seam ? 1 : 0) + (ducts ? 1 : 0), objects(level, route, GroundObjectSpec::cache), at);
                assertEquals(1, objects(level, route, object -> object.secretIndex() >= 0), at + ": the secret's lock");
                assertEquals(
                        1,
                        objects(level, route, object -> object.drop().equals(Optional.of(PickupType.ARMOUR_PATCH))),
                        at + ": the repair crate");
            }
            assertEquals(1, level.secrets(), difficulty.name());
        }
        System.out.println("Level 32 by route " + List.of(slugs) + ": " + counted);
        assertEquals(new java.util.TreeMap<>(expected), new java.util.TreeMap<>(counted));
    }

    /**
     * The new units as their stat blocks map them: the Sentinel's emitter behind its collar (the 28 px core in the 72 px
     * box), its sweep (120° over 1.6 s, telegraphed 0.8 s, 220 px long, every 4.5 s, the first 1.0 s after it comes
     * on) and the full turn every third (3.0 s, telegraphed 1.0 s, its slot 6.0 s); easy 100° arcs every 5.5 s without
     * a full turn, hard 160° every 3.6 s with every second a full turn. The Strider's legs topple it (stagger 0.6 s, fall
     * 0.8 s, a 48 × 140 line), each part paying its bounty again as a first bonus (a topple pays the whole 60), the pod
     * carrying the homing salvo (2 at medium, 1 on easy, 3 on hard).
     */
    @Test
    void theSentinelAndTheStriderAreBuiltAsTheirStatBlocks() {
        Map<Difficulty, double[]> sweeps = Map.of(
                Difficulty.EASY, new double[] {100, 5.5, 0},
                Difficulty.MEDIUM, new double[] {120, 4.5, 3},
                Difficulty.HARD, new double[] {160, 3.6, 2});
        Map<Difficulty, Integer> salvos = Map.of(Difficulty.EASY, 1, Difficulty.MEDIUM, 2, Difficulty.HARD, 3);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            EnemySpec tower = level.groundUnits().stream()
                    .map(GroundUnit::enemy)
                    .filter(enemy -> enemy.slug().equals("sentinel-tower"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(28, tower.core().orElseThrow().width(), 1e-9);
            assertEquals(72, tower.hitbox().width(), 1e-9);
            var sweep = tower.sweep().orElseThrow();
            double[] expected = sweeps.get(difficulty);
            String at = difficulty.name();
            assertEquals(expected[0], Math.toDegrees(sweep.arcRadians()), 1e-6, at + ": the arc");
            assertEquals(1.6, sweep.sweepSeconds(), 1e-9, at);
            assertEquals(0.8, sweep.telegraphSeconds(), 1e-9, at);
            assertEquals(220, sweep.length(), 1e-9, at);
            assertEquals(expected[1], sweep.intervalSeconds(), 1e-9, at + ": the interval");
            assertEquals(1.0, sweep.firstDelaySeconds(), 1e-9, at);
            var turn = tower.groundSweep().orElseThrow();
            assertEquals((int) expected[2], turn.fullTurnEvery(), at + ": a full turn every");
            assertTrue(turn.firstFullTurn(), at + ": the first sweep a full turn (QE-3 a; none on easy)");
            if (turn.fullTurnEvery() > 0) {
                assertEquals(3.0, turn.fullTurnSeconds(), 1e-9, at);
                assertEquals(1.0, turn.fullTurnTelegraphSeconds(), 1e-9, at);
                assertEquals(6.0, turn.fullTurnSlotSeconds(), 1e-9, at);
            }
            EnemySpec strider = level.waves().stream()
                    .map(WaveSpec::enemy)
                    .filter(enemy -> enemy.slug().equals("strider"))
                    .findFirst()
                    .orElseThrow();
            var topple = strider.topple().orElseThrow();
            assertEquals(0.6, topple.staggerSeconds(), 1e-9, at);
            assertEquals(0.8, topple.fallSeconds(), 1e-9, at);
            assertEquals(48, topple.lineWidth(), 1e-9, at);
            assertEquals(140, topple.lineLength(), 1e-9, at);
            assertEquals(
                    2,
                    strider.parts().stream().filter(EnemySpec.UnitPart::topples).count(),
                    at + ": the two legs topple it");
            assertEquals(
                    1,
                    strider.parts().stream()
                            .filter(EnemySpec.UnitPart::silencesSpit)
                            .count(),
                    at + ": the pod carries the salvo");
            for (EnemySpec.UnitPart part : strider.parts()) {
                assertEquals(part.bounty(), part.firstBonus(), at + ": " + part.name() + " pays its bounty again");
            }
            EnemyGun spit = strider.walker().orElseThrow().spit().orElseThrow();
            assertTrue(spit.homing().isPresent(), at + ": the salvo is homing");
            assertEquals(salvos.get(difficulty), spit.burst(), at + ": the salvo's missiles");
        }
    }

    /**
     * The mining charges (1 HP, no bounty, a 64 px burst of 80), the branch caches (20 HP locks freeing a crate of 50 in
     * Act 1 terms, no secret), the secret's dark lock (30 HP, the consortium store's crate of 100) and the repair crate
     * (10 HP, an armour patch); the branch ones on their channel's side.
     */
    @Test
    void theChargesLocksAndCachesAreBuiltAsTheData() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            String at = difficulty.name();
            int charges = 0;
            int caches = 0;
            for (GroundObjectSpec object : level.groundObjects()) {
                if (object.blast().isPresent()) {
                    charges++;
                    assertEquals(CHARGE_RADIUS, object.blast().get().radius(), 1e-9, at);
                    assertEquals(CHARGE_DAMAGE, object.blast().get().damage(), 1e-9, at);
                    assertEquals(0, object.bounty(), at + ": a charge pays nothing");
                    assertEquals(0, object.crateCredits(), at);
                    assertTrue(object.secretIndex() < 0, at);
                }
                if (object.cache()) {
                    caches++;
                    assertEquals(20, object.hp(), 1e-9, at + ": a cache's lock");
                    assertEquals(CACHE_CRATE, object.crateCredits(), at + ": Act 1 terms");
                    Branch branch = object.branch().orElseThrow();
                    assertEquals(
                            RICHER.charAt(branch.fork()) == 'L' ? Branch.LEFT : Branch.RIGHT,
                            branch.side(),
                            at + ": a cache in the richer channel");
                }
                if (object.secretIndex() >= 0) {
                    assertEquals(30, object.hp(), 1e-9, at + ": the secret's lock");
                    assertTrue(object.branch().isEmpty(), at);
                }
            }
            assertEquals(12, charges, at + ": 2 in the hall, 4 on the gantry, 3 in each richer channel");
            assertEquals(2, caches, at);
        }
        assertEquals(SECRET_CRATE, content.level(LEVEL).secrets().getFirst().crate(), "the secret's crate");
    }

    /** {@code TOWERS DOWN} is the secondary: all four Sentinels, 100 in Act 1 terms; the level is underground. */
    @Test
    void towersDownIsTheSecondaryAndTheRockGreysTheBombers() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            Secondary secondary = level.objectives().secondary();
            assertEquals("TOWERS DOWN", secondary.label());
            assertEquals(TOWERS_DOWN_PAY, secondary.credits(), difficulty.name());
        }
        assertTrue(content.level(LEVEL).undergroundOf(0), "IN ROCK: no Airstrike, no Orbital Lance");
    }

    /**
     * The radio: the fork-one {@code branch} lines (Okafor's, one a side, as {@code "1:left"} and {@code "1:right"}),
     * the first topple crush (Rook's), the secondary's line; Rook's lines need him flying ({@code requires: escort}).
     */
    @Test
    void theRadioHasTheBranchLines() {
        for (Difficulty difficulty : Difficulty.values()) {
            List<RadioCue> radio = SimSpecs.level(content, LEVEL, difficulty).radio();
            List<String> branches = radio.stream()
                    .filter(cue -> cue.trigger() == CueTrigger.BRANCH)
                    .map(cue -> cue.subject() + " " + cue.speaker())
                    .sorted()
                    .toList();
            assertEquals(List.of("1:left Okafor", "1:right Okafor"), branches, difficulty.name());
            RadioCue crush = radio.stream()
                    .filter(cue -> cue.trigger() == CueTrigger.TOPPLE_CRUSH)
                    .findFirst()
                    .orElseThrow();
            assertEquals("Rook", crush.speaker());
            assertEquals(
                    1,
                    radio.stream()
                            .filter(cue -> cue.trigger() == CueTrigger.SECONDARY_OBJECTIVE)
                            .count(),
                    difficulty.name());
            for (RadioCue cue : radio) {
                if (cue.speaker().equals("Rook")) {
                    assertTrue((cue.requires() & RadioCue.FITTED_ESCORT) != 0, cue.line());
                }
            }
        }
    }

    /** What one completing attempt saw. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** The radio cues that started, by trigger. */
        final Map<CueTrigger, Integer> radio = new EnumMap<>(CueTrigger.class);
        /** The {@code branch} cues' subjects heard. */
        final List<String> branchLines = new ArrayList<>();
        /** The radio lines Rook spoke. */
        int rookLines;
        /** A unit of a side not chosen (or not yet) seen awake, or spawned from such a wave. */
        int unchosenAwake;
        /** A ground object of a side not chosen seen awake or destroyed. */
        int unchosenObjects;
        /** The chosen sides' branch ground units seen awake (by serial). */
        final Set<Integer> woke = new HashSet<>();
        /** The shortest telegraph before an arc sweep and before a full turn, s. */
        double arcTell = Double.POSITIVE_INFINITY;

        double turnTell = Double.POSITIVE_INFINITY;
        /** The arc sweeps and full turns that started. */
        int arcs;

        int turns;
        /** Ground objects destroyed within a fall line's reach of a topple's crush, in its step. */
        int crushedObjects;
        /** The ground units the topples crushed. */
        int crushedUnits;
        /** Two charges within a burst's radius bursting in the same step (a chain). */
        int chains;
        /** Live charges left standing within a burst's radius after it. */
        int spared;
        /** Homing missiles launched at a Strider's pod while it stands, and after it was lost. */
        int podSalvos;

        int podAfterLoss;

        private final LevelScript level;
        private final Map<Integer, Integer> telling = new HashMap<>();
        private final Map<Integer, Boolean> tellingTurn = new HashMap<>();

        Watch(LevelScript level) {
            this.level = level;
        }

        @Override
        public void newAttempt() {
            events.clear();
            radio.clear();
            branchLines.clear();
            rookLines = 0;
            unchosenAwake = 0;
            unchosenObjects = 0;
            woke.clear();
            arcTell = Double.POSITIVE_INFINITY;
            turnTell = Double.POSITIVE_INFINITY;
            arcs = 0;
            turns = 0;
            crushedObjects = 0;
            crushedUnits = 0;
            chains = 0;
            spared = 0;
            podSalvos = 0;
            podAfterLoss = 0;
            telling.clear();
            tellingTurn.clear();
        }

        private static boolean chosen(Sortie sortie, int fork, int side) {
            return sortie.forkSide(fork) == side;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            List<double[]> crushes = new ArrayList<>();
            List<double[]> destroyed = new ArrayList<>();
            List<double[]> blasts = new ArrayList<>();
            for (int i = 0; i < seen.size(); i++) {
                SimEvents.Type type = seen.type(i);
                events.merge(type, 1, Integer::sum);
                switch (type) {
                    case RADIO -> {
                        RadioCue cue = sortie.script().radio().get(seen.value(i));
                        radio.merge(cue.trigger(), 1, Integer::sum);
                        rookLines += cue.speaker().equals("Rook") ? 1 : 0;
                        if (cue.trigger() == CueTrigger.BRANCH) {
                            branchLines.add(cue.subject());
                        }
                    }
                    case TOPPLE_CRUSH -> {
                        crushes.add(new double[] {seen.x(i), seen.y(i)});
                        crushedUnits += seen.value(i);
                    }
                    case GROUND_DESTROYED -> destroyed.add(new double[] {seen.x(i), seen.y(i)});
                    case CHARGE_BLAST -> blasts.add(new double[] {seen.x(i), seen.y(i)});
                    case MISSILE_LAUNCHED -> pod(sortie, seen.x(i), seen.y(i));
                    default -> {}
                }
            }
            for (double[] crush : crushes) {
                for (double[] object : destroyed) {
                    if (Math.hypot(crush[0] - object[0], crush[1] - object[1]) <= FALL_REACH) {
                        crushedObjects++;
                    }
                }
            }
            for (int a = 0; a < blasts.size(); a++) {
                for (int b = a + 1; b < blasts.size(); b++) {
                    if (Math.hypot(blasts.get(a)[0] - blasts.get(b)[0], blasts.get(a)[1] - blasts.get(b)[1])
                            <= CHARGE_RADIUS) {
                        chains++;
                    }
                }
                for (int k = 0; k < sortie.groundObjectCount(); k++) {
                    GroundObject object = sortie.groundObject(k);
                    if (object.spec().blast().isPresent()
                            && !object.spent()
                            && !object.dormant()
                            && Math.hypot(object.renderX(1) - blasts.get(a)[0], object.renderY(1) - blasts.get(a)[1])
                                    <= CHARGE_RADIUS) {
                        spared++;
                    }
                }
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                int code = enemy.branch();
                if (code >= 0) {
                    boolean mine = chosen(sortie, code / 2, code % 2);
                    if (!mine && !enemy.dormant()) {
                        unchosenAwake++;
                    }
                    if (mine && !enemy.dormant()) {
                        woke.add(enemy.serial());
                    }
                }
                if (enemy.wave() >= 0 && enemy.wave() < level.waves().size()) {
                    Optional<Branch> branch = level.waves().get(enemy.wave()).branch();
                    if (branch.isPresent()
                            && !chosen(sortie, branch.get().fork(), branch.get().side())) {
                        unchosenAwake++;
                    }
                }
                if (enemy.spec().groundSweep().isPresent()) {
                    sweep(enemy, step);
                }
            }
            for (int k = 0; k < sortie.groundObjectCount(); k++) {
                GroundObject object = sortie.groundObject(k);
                Optional<Branch> branch = object.spec().branch();
                if (branch.isPresent()
                        && !chosen(sortie, branch.get().fork(), branch.get().side())
                        && (!object.dormant() || object.spent())) {
                    unchosenObjects++;
                }
            }
        }

        /** A Sentinel's telegraph to its sweep: the tell's length, an arc's or a full turn's. */
        private void sweep(Enemy tower, int step) {
            int serial = tower.serial();
            if (tower.telegraphing()) {
                if (!telling.containsKey(serial)) {
                    telling.put(serial, step);
                    tellingTurn.put(serial, tower.sweepFullTurn());
                }
            } else if (tower.sweeping() && telling.containsKey(serial)) {
                double tell = (step - telling.remove(serial)) * SimStep.SECONDS;
                if (tellingTurn.remove(serial) || tower.sweepFullTurn()) {
                    turns++;
                    turnTell = Math.min(turnTell, tell);
                } else {
                    arcs++;
                    arcTell = Math.min(arcTell, tell);
                }
            } else if (!tower.sweeping()) {
                // an EMP's stun cut the telegraph
                telling.remove(serial);
                tellingTurn.remove(serial);
            }
        }

        /** A homing missile launched at (x, y): from a Strider's pod, standing or lost? */
        private void pod(Sortie sortie, double x, double y) {
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().topple().isEmpty()) {
                    continue;
                }
                List<EnemySpec.UnitPart> parts = enemy.spec().parts();
                for (int p = 0; p < parts.size(); p++) {
                    if (parts.get(p).silencesSpit()
                            && Math.hypot(enemy.partX(p) - x, enemy.partY(p) - y) <= POD_REACH) {
                        if (enemy.partAlive(p)) {
                            podSalvos++;
                        } else {
                            podAfterLoss++;
                        }
                    }
                }
            }
        }

        int count(SimEvents.Type type) {
            return events.getOrDefault(type, 0);
        }
    }

    /** How near a crush's centre (the fall line's middle) a ground object lies under the 48 × 140 line at most, px. */
    static final double FALL_REACH = Math.hypot(24, 70);

    /** How near a pod a missile's launch counts as the pod's, px. */
    static final double POD_REACH = 10;

    /** One whole flight's measures. */
    record Flight(
            String fit,
            String route,
            Difficulty difficulty,
            long seed,
            boolean complete,
            String flown,
            LevelMetrics.Run run,
            Watch watch,
            int secrets) {
        String at() {
            return fit + " " + route + " " + difficulty + " seed " + seed;
        }
    }

    private Flight fly(String fit, String route, Loadout loadout, Difficulty difficulty, long seed) {
        Sortie sortie = sortie(content, loadout, seed, difficulty, false, route);
        Watch watch = new Watch(sortie.script());
        boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, watch);
        return new Flight(
                fit,
                route,
                difficulty,
                seed,
                complete,
                RoutePlay.flown(sortie),
                LevelMetrics.Run.of(sortie),
                watch,
                complete ? sortie.result().secretsFound() : 0);
    }

    /** The flights of a fit over the routes, seeds and difficulties, flown in parallel, in their order. */
    List<Flight> flights(String fit, int seeds, BiFunction<Content, Difficulty, Loadout> loadout) {
        Difficulty[] difficulties = Difficulty.values();
        int per = ROUTES.size() * seeds;
        return IntStream.range(0, difficulties.length * per)
                .parallel()
                .mapToObj(i -> {
                    Difficulty difficulty = difficulties[i / per];
                    String route = ROUTES.get(i % per / seeds);
                    return fly(fit, route, loadout.apply(content, difficulty), difficulty, 1 + i % seeds);
                })
                .toList();
    }

    /**
     * Each run's checks: completed on its route (each fork's choice the route's side), no unit or object of a side not
     * chosen ever awake, the chosen richer side's ground units woken, each sweep after its full telegraph (an arc's
     * 0.8 s, a full turn's 1.0 s) and full turns on medium and hard only, no ground object crushed, no charge chain, no
     * salvo from a lost pod; the radio's {@code branch} line once, the chosen side's; Rook's lines only with Rook.
     */
    private static String check(Flight flight, boolean rook) {
        String at = flight.at();
        Watch watch = flight.watch();
        assertTrue(flight.complete(), at + ": completes");
        assertEquals(flight.route(), flight.flown(), at + ": the route flown");
        assertEquals(0, watch.unchosenAwake, at + ": a unit of a side not chosen awake");
        assertEquals(0, watch.unchosenObjects, at + ": an object of a side not chosen awake");
        assertEquals(2, watch.count(SimEvents.Type.BRANCH_CHOSEN), at + ": a choice a fork");
        assertTrue(watch.arcTell >= 0.8 - SimStep.SECONDS - 1e-9, at + ": an arc's tell " + watch.arcTell);
        assertTrue(watch.turnTell >= 1.0 - SimStep.SECONDS - 1e-9, at + ": a full turn's tell " + watch.turnTell);
        if (flight.difficulty() == Difficulty.EASY) {
            assertEquals(0, watch.turns, at + ": no full turn on easy");
        }
        assertEquals(0, watch.crushedObjects, at + ": a topple crushes ground units only");
        assertEquals(0, watch.chains, at + ": a charge sets off no other charge");
        assertEquals(0, watch.podAfterLoss, at + ": a salvo from a lost pod");
        String side = flight.route().charAt(0) == 'L' ? "1:left" : "1:right";
        assertEquals(List.of(side), watch.branchLines, at + ": the chosen side's branch line");
        if (!rook) {
            assertEquals(0, watch.rookLines, at + ": Rook's lines need him flying");
        }
        return String.format(
                "%n    %s: retries %d, TOWERS DOWN %s, credits %d, secret %d; woke %d; sweeps %d arcs (tell %.2f s),"
                        + " %d full turns (tell %.2f s), glanced %d; topples %d told, %d fell, %d crushes (%d units); charges %d"
                        + " (spared %d); caches %d; pod salvos %d",
                at,
                flight.run().retries(),
                flight.run().secondaryMet(),
                flight.run().credits(),
                flight.secrets(),
                watch.woke.size(),
                watch.arcs,
                watch.arcTell,
                watch.turns,
                watch.turnTell,
                watch.count(SimEvents.Type.SHOT_GLANCED),
                watch.count(SimEvents.Type.TOPPLE_TELL),
                watch.count(SimEvents.Type.TOPPLE_FALL),
                watch.count(SimEvents.Type.TOPPLE_CRUSH),
                watch.crushedUnits,
                watch.count(SimEvents.Type.CHARGE_BLAST),
                watch.spared,
                watch.count(SimEvents.Type.CACHE_OPENED),
                watch.podSalvos);
    }

    /**
     * The autopilot completes every route with the plan's fit and Rook on every difficulty and seed; over the runs the
     * Sentinels sweep and turn full circles, shots glance off their collars, Striders topple and crush ground units,
     * charges burst, the richer channels' caches open, the secret is found, and {@code TOWERS DOWN} is met on every
     * difficulty ({@link TowerPlay}; the tally per route and difficulty printed).
     */
    @Test
    void theAutopilotFliesEveryRouteWithRook() {
        List<Flight> flights = flights("Rook", SEEDS, Level32Test::planLoadout);
        StringBuilder report = new StringBuilder("Level 32 autopilot with Rook over " + SEEDS + " seeds a route:");
        Map<String, Integer> towersDown = new LinkedHashMap<>();
        Map<Difficulty, Integer> metBy = new EnumMap<>(Difficulty.class);
        int turns = 0;
        int glanced = 0;
        int crushed = 0;
        int crushedUnits = 0;
        int blasts = 0;
        int spared = 0;
        int salvos = 0;
        int secrets = 0;
        int richCaches = 0;
        int safeCaches = 0;
        int woke = 0;
        for (Flight flight : flights) {
            report.append(check(flight, true));
            Watch watch = flight.watch();
            boolean met = flight.run().secondaryMet();
            towersDown.merge(flight.difficulty() + " " + flight.route(), met ? 1 : 0, Integer::sum);
            metBy.merge(flight.difficulty(), met ? 1 : 0, Integer::sum);
            turns += watch.turns;
            glanced += watch.count(SimEvents.Type.SHOT_GLANCED);
            crushed += watch.count(SimEvents.Type.TOPPLE_CRUSH);
            crushedUnits += watch.crushedUnits;
            blasts += watch.count(SimEvents.Type.CHARGE_BLAST);
            spared += watch.spared;
            salvos += watch.podSalvos;
            secrets += flight.secrets();
            woke += watch.woke.size();
            int caches = watch.count(SimEvents.Type.CACHE_OPENED);
            if (flight.route().equals("LR")) {
                safeCaches += caches;
            } else {
                richCaches += caches;
            }
        }
        report.append("\n    TOWERS DOWN met (of ").append(SEEDS).append("): ").append(towersDown);
        report.append("\n    full turns ")
                .append(turns)
                .append(", ground units crushed ")
                .append(crushedUnits);
        System.out.println(report);
        for (Difficulty difficulty : Difficulty.values()) {
            assertTrue(metBy.getOrDefault(difficulty, 0) > 0, difficulty + ": TOWERS DOWN never met: " + towersDown);
        }
        assertTrue(glanced > 0, "shots glance off the collars");
        assertTrue(crushed > 0, "the Striders topple");
        assertTrue(blasts > 0, "the mining charges burst");
        assertTrue(spared > 0, "a burst leaves a charge in its radius standing");
        assertTrue(salvos > 0, "the pods fire their salvos");
        assertTrue(secrets > 0, "the consortium store is found");
        assertTrue(woke > 0, "the chosen richer sides' ground units wake");
        assertEquals(0, safeCaches, "no cache on the safer route");
        assertTrue(richCaches > 0, "the richer channels' caches open");
    }

    /** Without Rook ({@code --escort none}) the autopilot completes every route on every difficulty; his lines stay silent. */
    @Test
    void theAutopilotFliesEveryRouteWithoutRook() {
        StringBuilder report = new StringBuilder("Level 32 autopilot without Rook:");
        for (Flight flight : flights("--escort none", 1, Level32Test::withoutRook)) {
            report.append(check(flight, false));
        }
        System.out.println(report);
    }

    /**
     * The Sentinels left alone (the ship invulnerable, holding fire, the autopilot's moves; route LL): each sweeps while
     * it is on the field, each sweep after its full telegraph (an arc's 0.8 s, a full turn's 1.0 s). QE-3 a (step E3b,
     * pending the user's review in round 65): each tower is about 4.7 s on the field (5.1 s in the refinery), so it
     * sweeps once; Level 32's towers make that first sweep the full turn ({@code full_turn_first}) on medium and hard,
     * easy keeps its arcs. A full turn's telegraph starts its ray on the ship's bearing (the reach ring round it, {@link
     * Enemy#sweepFullTurn()}), and its beam turns most of the circle before the tower leaves.
     */
    @Test
    void theSentinelsSweepOnTheField() {
        StringBuilder report = new StringBuilder("Level 32 Sentinels left alone:");
        int live = SimStep.ticks(3.0);
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, planLoadout(content, difficulty), 1, difficulty, true, "LL");
            Watch watch = new Watch(sortie.script());
            Map<Integer, Integer> sweeps = new HashMap<>();
            Map<Integer, Integer> turning = new HashMap<>();
            double rayOff = 0;
            double nearest = Double.POSITIVE_INFINITY;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                sortie.step(Autopilot.commands(sortie) & ~Command.FIRE.bit() & ~Command.SPECIAL.bit());
                watch.stepped(sortie, steps);
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy tower = sortie.enemy(i);
                    if (tower.spec().groundSweep().isEmpty()) {
                        continue;
                    }
                    sweeps.put(tower.serial(), tower.sweepsStarted());
                    if (!tower.sweepFullTurn()) {
                        continue;
                    }
                    if (tower.telegraphing() && !turning.containsKey(tower.serial())) {
                        // The start ray on the ship's bearing from the tower (y up: radians clockwise from down).
                        double dx = sortie.ship().x() - tower.renderX(1);
                        double dy = sortie.ship().y() - tower.renderY(1);
                        double off =
                                Math.abs(Math.IEEEremainder(tower.sweepFrom() - Math.atan2(-dx, -dy), 2 * Math.PI));
                        rayOff = Math.max(rayOff, off);
                        nearest = Math.min(nearest, Math.hypot(dx, dy));
                        turning.put(tower.serial(), 0);
                    }
                    if (tower.sweeping()) {
                        turning.merge(tower.serial(), 1, Integer::sum);
                    }
                }
            }
            String at = difficulty.name();
            assertTrue(sortie.complete(), at);
            assertEquals(4, sweeps.size(), at + ": the four towers");
            assertTrue(sweeps.values().stream().allMatch(n -> n >= 1), at + ": each tower sweeps: " + sweeps);
            int leastTurn =
                    turning.values().stream().mapToInt(Integer::intValue).min().orElse(0);
            if (difficulty == Difficulty.EASY) {
                assertEquals(4, watch.arcs, at + ": an arc each");
                assertEquals(0, watch.turns, at + ": no full turn on easy");
                assertTrue(watch.arcTell >= 0.8 - SimStep.SECONDS - 1e-9, at + ": an arc's tell " + watch.arcTell);
            } else {
                assertEquals(4, watch.turns, at + ": each tower's first sweep the full turn");
                assertEquals(0, watch.arcs, at + ": no arc after it on the field");
                assertTrue(
                        watch.turnTell >= 1.0 - SimStep.SECONDS - 1e-9, at + ": a full turn's tell " + watch.turnTell);
                assertEquals(4, turning.size(), at + ": each tower's ring telegraph");
                assertTrue(rayOff < 0.05, at + ": the start ray on the ship's bearing, off by " + rayOff);
                assertTrue(leastTurn >= 0.85 * live, at + ": a full turn cut short, " + leastTurn + " of " + live);
            }
            report.append(String.format(
                    "%n    %s: sweeps started per tower %s, arcs %d (tell %.2f s), full turns %d (tell %.2f s; the beam"
                            + " live at least %.0f%% of its 3 s before the tower leaves; the ship %.0f px from a tower"
                            + " at its ring's telegraph at the nearest)",
                    at,
                    sweeps.values(),
                    watch.arcs,
                    watch.arcTell,
                    watch.turns,
                    watch.turnTell,
                    100.0 * leastTurn / live,
                    nearest));
        }
        System.out.println(report);
    }

    /**
     * The topples placed to crush (design/campaign Level 32, Blast doors, mining charges, topples): left alone (the ship
     * invulnerable, holding fire; route RL, the richer channels), each Strider that walks past SAM Nests has a moment
     * when a leg's fall line (48 × 140 px from its centre toward the leg) covers a nest: the hall pair's over the hall's
     * pair (t=91.3), the charge seam's over its pair (t=61.3), the refinery's lone Strider over the gantry's trio
     * (t=161.4).
     */
    @Test
    void theStridersFallLinesReachTheNestsTheyPass() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, planLoadout(content, difficulty), 1, difficulty, true, RICHER);
            Map<Double, Set<Integer>> crushable = new java.util.TreeMap<>();
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                sortie.step(Autopilot.commands(sortie) & ~Command.FIRE.bit() & ~Command.SPECIAL.bit());
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy strider = sortie.enemy(i);
                    if (strider.spec().topple().isPresent()
                            && !strider.dormant()
                            && strider.renderY(1) >= 0
                            && strider.renderY(1) <= HEIGHT) {
                        crushable(
                                sortie,
                                strider,
                                crushable.computeIfAbsent(
                                        sortie.script()
                                                .waves()
                                                .get(strider.wave())
                                                .t(),
                                        t -> new HashSet<>()));
                    }
                }
            }
            String at = difficulty + ": " + crushable;
            System.out.println("Level 32 nests under a Strider's fall line by its wave: " + at);
            assertEquals(2, crushable.get(91.3).size(), at + ": the hall's pair");
            assertEquals(2, crushable.get(61.3).size(), at + ": the charge seam's pair");
            assertEquals(3, crushable.get(161.4).size(), at + ": the gantry's trio");
        }
    }

    /** The field's height, px. */
    static final double HEIGHT = 540;

    /** Adds the serials of the awake SAM Nests a leg's fall line of {@code strider} covers now to {@code nests}. */
    private static void crushable(Sortie sortie, Enemy strider, Set<Integer> nests) {
        var topple = strider.spec().topple().orElseThrow();
        double x = strider.renderX(1);
        double y = strider.renderY(1);
        List<EnemySpec.UnitPart> parts = strider.spec().parts();
        for (int p = 0; p < parts.size(); p++) {
            if (!parts.get(p).topples()) {
                continue;
            }
            double dx = strider.partX(p) - x;
            double dy = strider.partY(p) - y;
            double length = Math.hypot(dx, dy);
            dx /= length;
            dy /= length;
            for (int j = 0; j < sortie.enemyCount(); j++) {
                Enemy nest = sortie.enemy(j);
                if (!nest.spec().slug().equals("sam-nest") || nest.dormant()) {
                    continue;
                }
                double nx = nest.renderX(1) - x;
                double ny = nest.renderY(1) - y;
                double along = nx * dx + ny * dy;
                double across = Math.abs(ny * dx - nx * dy);
                double half = nest.hitbox().width() / 2;
                if (along > -half && along < topple.lineLength() + half && across < topple.lineWidth() / 2 + half) {
                    nests.add(nest.serial());
                }
            }
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false, RICHER),
                Level32Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        for (String route : List.of("LR", "RL")) {
            Sortie first =
                    sortie(content, planLoadout(content, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false, route);
            Sortie second =
                    sortie(content, planLoadout(content, Difficulty.MEDIUM), 77, Difficulty.MEDIUM, false, route);
            flyToEnd(first);
            flyToEnd(second);

            assertEquals(first.stateHash(), second.stateHash(), route);
            assertTrue(first.complete(), route);
            assertEquals(route, RoutePlay.flown(first));
        }
        assertFalse(ROUTES.isEmpty());
    }

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
