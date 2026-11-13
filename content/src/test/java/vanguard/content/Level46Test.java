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
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PartSpec;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.Rules;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.StoryPickups;
import vanguard.sim.VrellLevel;
import vanguard.sim.WaveSpec;

/**
 * M11 part D, step D2: Level 46 Graveyard of the Choir from its data (design/campaign/act-7-beyond-the-gate/
 * level-46-graveyard-of-the-choir: the outer wrecks, the graveyard lanes, the quiet, the remnants, the dead world; the
 * Husks drifting on their lines, unwarned, cracking at half their HP and on the quiet's cue at t=144; the dying Leviathan
 * as scenery over the quiet; the six seed caskets as the {@code escapes} secondary; the warm hull's four pinned sinews and
 * the Vrell core), flown by the autopilot (the guns held in the quiet: {@link GraveyardPlay}) with the balance plan's fit
 * ({@link #planLoadout}), with Rook and without him ({@code --escort none}): every run won on every difficulty, nothing
 * fired at the ship in the quiet, the secondary met on most runs. The pacing, the floor, the density and the haul are the
 * campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link TypicalHaulTest}).
 */
class Level46Test {
    /** The level's key. */
    static final String LEVEL = "act-7-beyond-the-gate/level-46-graveyard-of-the-choir";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The plan's fitted load at Level 46, MW (Level 45's: the Swarm Call draws no power). */
    static final double LOAD = 24.5;

    /** The runs of a fit (of {@link #SEEDS} on each difficulty) with the secondary met, at least ("most runs"). */
    static final int SECONDARY_RUNS = 5;

    /**
     * The quiet's window on the level clock, s (the README's t=120–144: nothing fires at the ship; the level's {@code
     * quiet} holds Rook from t=118, the last wave gone, and so does the autopilot).
     */
    static final double QUIET_FROM = 120;

    /** The level's {@code quiet}: Rook's hold and the autopilot's from t=118 (the last wave before it gone). */
    static final double HOLD_FROM = 118;

    static final double QUIET_TO = 144;

    /** The quiet's Husks (three from the left, two from the right), every one cracked by the cue at t=144. */
    static final int QUIET_HUSKS = 5;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 46 ({@code "46"} in design/player/balance-plan.yaml: the visit buys the Vrell Swarm
     * Call and fits it in place of the EMP Burst, and gives Rook his Autocannon again): Level 45's guns, 24.5 of 27 MW,
     * Rook flying the Autocannon (L1).
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
                        Level40Test.OUTPUT - LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, SimSpecs.SWARM_CALL, 0))
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
     * The data: the units counted as enemies {@link #COUNTED} per difficulty (the caskets not counted); the Husk's drift
     * (20–40 px/s, a 10–25°/s spin), its crack (at half its HP, a 0.5 s tell (hard 0.4), a fan of 5 (3/7) shards 15°
     * apart at the fast class) and its death ring of 8 (6/10); every Husk and casket wave on its lines, unwarned; the
     * crack cue at t=144, 0.6 s apart (hard 0.5); the quiet t=120–144; the dying Leviathan as scenery (2,932 HP whole at
     * medium, no bounty, a small salvage per part, a medium at its death); the warm hull's four pinned sinews and the
     * core's 2 s rise and 48 × 48 box.
     */
    @Test
    void theLevelHoldsItsHusksItsCasketsAndItsScenery() {
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
            List<WaveSpec> husks = level.waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("husk"))
                    .toList();
            assertEquals(11, husks.size(), at + ": the Husk waves");
            assertEquals(32, husks.stream().mapToInt(WaveSpec::count).sum(), at + ": the Husks");
            for (WaveSpec wave : husks) {
                assertTrue(wave.unwarned(), at + ": a Husk drifts in unwarned at t=" + wave.t());
                assertEquals(wave.count(), wave.lines().size(), at + ": a line a Husk at t=" + wave.t());
            }
            EnemySpec husk = husks.getFirst().enemy();
            assertEquals(
                    new EnemySpec.Drift(new vanguard.sim.Range(20, 40), new vanguard.sim.Range(10, 25)),
                    husk.lineDrift().orElseThrow());
            EnemySpec.Crack crack = husk.crack().orElseThrow();
            int fan =
                    switch (difficulty) {
                        case EASY -> 3;
                        case MEDIUM -> 5;
                        case HARD -> 7;
                    };
            assertEquals(0.5, crack.at(), 1e-9, at + ": the crack's share");
            assertEquals(difficulty == Difficulty.HARD ? 0.4 : 0.5, crack.tellSeconds(), 1e-9, at + ": the tell");
            assertEquals(fan, crack.fan().fan(), at + ": the fan");
            assertEquals(Math.toRadians(15 * (fan - 1)), crack.fan().spreadRadians(), 1e-9, at + ": 15° apart");
            assertEquals(4, crack.fan().damage(), 1e-9, at + ": small shards");
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 6;
                        case MEDIUM -> 8;
                        case HARD -> 10;
                    },
                    husk.deathBurst().orElseThrow().count(),
                    at + ": the death ring");
            assertTrue(husk.gun().isEmpty(), at + ": a Husk never fires whole");
            assertEquals(15, husk.contactDamage(), 1e-9, at + ": medium contact");
            List<WaveSpec> caskets = level.waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("seed-casket"))
                    .toList();
            assertEquals(
                    List.of(30.0, 66.0, 100.0, 160.0, 205.0, 238.0),
                    caskets.stream().map(WaveSpec::t).toList(),
                    at + ": C1–C6");
            EnemySpec casket = caskets.getFirst().enemy();
            assertTrue(!casket.countsAsEnemy() && casket.seekersIgnore(), at + ": uncounted, never a seeker's target");
            assertEquals(0, casket.contactDamage(), 1e-9, at + ": no contact");
            assertEquals(0, casket.bounty(), at + ": no bounty");
            assertEquals(PickupType.MEDIUM_SALVAGE, casket.drop().orElseThrow().pickup(), at + ": a medium salvage");
            VrellLevel vrell = level.vrell();
            assertEquals(
                    new VrellLevel.Quiet(HOLD_FROM, QUIET_TO), vrell.quiet().orElseThrow(), at + ": the quiet");
            assertEquals(
                    new VrellLevel.CrackCue(144, difficulty == Difficulty.HARD ? 0.5 : 0.6),
                    vrell.crackCue().orElseThrow(),
                    at + ": the cue");
            SetPieceSpec leviathan = level.setPieces().getFirst();
            assertTrue(leviathan.isScenery(), at + ": the dying Leviathan is scenery");
            assertEquals(0, leviathan.bounty(), at + ": no bounty");
            assertEquals(
                    PickupType.SMALL_SALVAGE,
                    leviathan.scenery().orElseThrow().partDrop().orElseThrow());
            assertEquals(PickupType.MEDIUM_SALVAGE, leviathan.drop().orElseThrow(), at + ": its death's salvage");
            List<GroundObjectSpec> sinews = level.groundObjects().stream()
                    .filter(object -> object.group() >= 0)
                    .toList();
            assertEquals(4, sinews.size(), at + ": the four sinews");
            assertTrue(sinews.stream().allMatch(GroundObjectSpec::pinnedLowAir), at + ": pinned on low-air");
            assertTrue(level.groundObjects().stream().allMatch(GroundObjectSpec::pinnedLowAir), at + ": the hatch too");
            assertEquals(
                    new StoryPickups.Story("vrell-core", 2.0, new Hitbox(48, 48)),
                    level.drops().story(),
                    at + ": the core's rise and box");
        }
        SetPieceSpec leviathan =
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).setPieces().getFirst();
        assertEquals(
                2932, leviathan.parts().stream().mapToDouble(PartSpec::hp).sum(), 1.0, "the dying Leviathan whole");
        System.out.println("Level 46 counted enemies: " + counted);
        assertEquals(COUNTED, counted);
    }

    /**
     * Step D2's waves' counts: the README's 64 waves (the Lamprey pincers as two edge-warned streams of 3) with the
     * denser mixed waves the act's floor asks for (Needler wings and columns, Stinger lines and columns, Chimera wings and
     * Harrow streams on the offbeats, Mote Swarms of 10 and Skitter streams of 8; the user's answer for Act 7, 2026-11-05),
     * the Husks, caskets and Mantises pinned, the formation lever sizing the rest; the caskets not counted.
     */
    static final Map<Difficulty, Integer> COUNTED =
            Map.of(Difficulty.EASY, 431, Difficulty.MEDIUM, 546, Difficulty.HARD, 661);

    /** What a run saw, over every attempt (the losses) and in its completing one (the quiet, the core, the caskets). */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new ArrayList<>();
        /** Crack tells and fans in the quiet's window, in the completing attempt. */
        int quietCracks;
        /** Enemy shots fired in the quiet's window, in the completing attempt. */
        int quietShots;
        /** The Husks hit in the quiet's window (a hull losing HP), in the completing attempt. */
        final java.util.Set<Integer> quietHit = new java.util.HashSet<>();
        /** The Husks' HP at the last step, by serial. */
        final Map<Integer, Double> hp = new java.util.HashMap<>();
        /** The cue's cracks (tells from t=144 to 150), in the completing attempt. */
        int cueTells;
        /** Steps Rook flew, in the completing attempt. */
        int rookSteps;
        /** The core taken (1), missed (-1) or neither (0), in the completing attempt, and when. */
        int story;

        double storyAt;
        /** Salvage pickups collected, in the completing attempt. */
        int salvage;
        /** The most Husks on the screen at once, in the completing attempt (the Husk's rule: at most 6). */
        int mostHusks;
        /** Husks alive and wholly on the screen at the first step from the cue (t=144), in the completing attempt. */
        int cueHusks = -1;

        @Override
        public void newAttempt() {
            quietCracks = 0;
            quietShots = 0;
            quietHit.clear();
            hp.clear();
            cueTells = 0;
            rookSteps = 0;
            story = 0;
            salvage = 0;
            mostHusks = 0;
            cueHusks = -1;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            double t = sortie.levelSeconds();
            boolean quiet = t >= QUIET_FROM && t < QUIET_TO;
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                SimEvents.Type type = events.type(i);
                if (type == SimEvents.Type.SHIP_DESTROYED) {
                    deaths.add(String.format("%.1f@%.0f", t, events.x(i)));
                } else if (type == SimEvents.Type.CRACK_TELL || type == SimEvents.Type.CRACK_FAN) {
                    quietCracks += quiet ? 1 : 0;
                    cueTells += type == SimEvents.Type.CRACK_TELL && t >= QUIET_TO && t < QUIET_TO + 6 ? 1 : 0;
                } else if (type == SimEvents.Type.ENEMY_FIRED) {
                    quietShots += quiet ? 1 : 0;
                } else if (type == SimEvents.Type.STORY_COLLECTED) {
                    story = 1;
                    storyAt = t;
                } else if (type == SimEvents.Type.STORY_MISSED) {
                    story = -1;
                    storyAt = t;
                } else if (type == SimEvents.Type.PICKUP_COLLECTED) {
                    salvage++;
                }
            }
            int husks = 0;
            int wholly = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.spec().slug().equals("husk")) {
                    continue;
                }
                if (enemy.hp() > 0 && LevelMetrics.onScreen(enemy.renderX(1), enemy.renderY(1), enemy.hitbox())) {
                    husks++;
                }
                if (enemy.hp() > 0 && wholly(enemy.renderX(1), enemy.renderY(1), enemy.hitbox())) {
                    wholly++;
                }
                Double before = hp.put(enemy.serial(), enemy.hp());
                if (quiet && before != null && enemy.hp() < before) {
                    quietHit.add(enemy.serial());
                }
            }
            mostHusks = Math.max(mostHusks, husks);
            if (cueHusks < 0 && t >= QUIET_TO) {
                cueHusks = wholly;
            }
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
                "%n    %s: %s, retries %d, losses %s; Husks at most %d, %d at the cue; the quiet: %d cracks, %d shots,"
                        + " %d Husks hit;"
                        + " the cue's tells %d;"
                        + " Rook %.0f s; the core %s at %.1f; pickups %d; %s, credits %d",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                watch.deaths,
                watch.mostHusks,
                watch.cueHusks,
                watch.quietCracks,
                watch.quietShots,
                watch.quietHit.size(),
                watch.cueTells,
                watch.rookSteps / 60.0,
                watch.story > 0 ? "taken" : watch.story < 0 ? "tugged" : "-",
                watch.storyAt,
                watch.salvage,
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.run().credits());
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 46 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            String at = flight.at();
            assertTrue(flight.complete(), at + ": completes");
            Watch watch = flight.watch();
            assertEquals(0, watch.quietCracks, at + ": no crack in the quiet");
            assertEquals(0, watch.quietShots, at + ": no shot in the quiet");
            assertEquals(0, watch.quietHit.size(), at + ": no Husk hit in the quiet (Rook holds too)");
            assertTrue(watch.story != 0, at + ": the core taken or tugged");
            assertTrue(watch.mostHusks <= 6, at + ": at most 6 Husks on the screen, was " + watch.mostHusks);
            assertEquals(
                    QUIET_HUSKS,
                    watch.cueHusks,
                    at + ": step D6 (round 91 C.7): the quiet's five Husks alive and wholly on the screen at the cue");
        }
        long met =
                flights.stream().filter(flight -> flight.run().secondaryMet()).count();
        assertTrue(
                met >= SECONDARY_RUNS,
                fit + ": runs with the secondary met: " + met + " of " + flights.size() + ", at least "
                        + SECONDARY_RUNS);
    }

    /**
     * Step D6 (round 91 C.7, accepted): the crack cue at t=144 catches all five of the quiet's Husks. A Husk draws its
     * drift from 20–40 px/s (seeded per unit, so per run), so each of the quiet's Husks is wholly on the screen at the
     * cue at its drift's slowest and at its fastest on every difficulty: in from t=132, 11–12 s before it, on the D2
     * lines (no wave key sets a drift's speed). The flights check the same per seed ({@link Watch#cueHusks}).
     */
    @Test
    void theCueCatchesTheQuietsHusksAtAnyDrift() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            double cue = level.vrell().crackCue().orElseThrow().t();
            int husks = 0;
            for (WaveSpec wave : level.waves()) {
                if (!wave.enemy().slug().equals("husk") || wave.t() < HOLD_FROM || wave.t() >= cue) {
                    continue;
                }
                EnemySpec husk = wave.enemy();
                var drift = husk.lineDrift().orElseThrow();
                for (int i = 0; i < wave.count(); i++) {
                    husks++;
                    WaveSpec.Line line = wave.lines().get(i % wave.lines().size());
                    double seconds =
                            cue - (wave.t() + i * wave.intervalSeconds().orElse(0.0));
                    for (double speed :
                            new double[] {drift.speed().min(), drift.speed().max()}) {
                        double length = Math.hypot(
                                line.to().x() - line.from().x(),
                                line.to().depth() - line.from().depth());
                        double s = speed * seconds / length;
                        double x =
                                line.from().x() + (line.to().x() - line.from().x()) * s;
                        double depth = line.from().depth()
                                + (line.to().depth() - line.from().depth()) * s;
                        assertTrue(
                                wholly(x, PlayField.HEIGHT - depth, husk.hitbox()),
                                String.format(
                                        java.util.Locale.ROOT,
                                        "%s: the t=%.1f Husk %d at %.0f px/s is at (%.0f, %.0f) at the cue",
                                        difficulty,
                                        wave.t(),
                                        i,
                                        speed,
                                        x,
                                        depth));
                    }
                }
            }
            assertEquals(QUIET_HUSKS, husks, difficulty + ": the quiet's Husks");
        }
    }

    /** A hit box wholly on the field. */
    static boolean wholly(double x, double y, Hitbox box) {
        return x - box.width() / 2 >= 0
                && x + box.width() / 2 <= PlayField.WIDTH
                && y - box.height() / 2 >= 0
                && y + box.height() / 2 <= PlayField.HEIGHT;
    }

    /** The autopilot wins the level with the plan's fit and Rook, on every difficulty and seed, the quiet kept. */
    @Test
    void theAutopilotCrossesTheDeadFleetWithRook() {
        report("Rook", flights("Rook", Level46Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotCrossesTheDeadFleetWithoutRook() {
        report("--escort none", flights("--escort none", Level46Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level46Test::flyToEnd);

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
