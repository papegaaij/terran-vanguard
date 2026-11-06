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
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.StoryPickups;
import vanguard.sim.VrellLevel;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M11 part C, step C2: Level 45 Hive Reef from its data (design/campaign/act-7-beyond-the-gate/level-45-hive-reef: the
 * reef rim, the brood fields, the spine, the living bridge, the brood heart, the far rim; the eight spawning organs O1–O8
 * as the fail-at-once `destroy-targets` primary without holds, the 14 spawning pits, the 10 spore clouds, the call organ
 * on the spine, the three Threadcrawlers as the secondary), flown by the autopilot ({@link ReefPlay}: the organs first,
 * off the pits, the horn's roots then through it) with the balance plan's fit ({@link #planLoadout}), with Rook and
 * without him ({@code --escort none}): every run won on every difficulty with all eight organs (no failed primary in the
 * completing attempt), the secondary met on most runs. The pacing, the floor, the density, the beam era's HP and the
 * haul are the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link
 * BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level45Test {
    /** The level's key. */
    static final String LEVEL = "act-7-beyond-the-gate/level-45-hive-reef";

    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;

    /** The plan's fitted load at Level 45, MW (the Tesla Coil Pod L5). */
    static final double LOAD = 24.5;

    /** The runs of a fit (of {@link #SEEDS} on each difficulty) with the secondary met, at least ("most runs"). */
    static final int SECONDARY_RUNS = 5;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 45 ({@code "45"} in design/player/balance-plan.yaml: the visit raises the Tesla Coil
     * Pod to L5 and fits Rook's owned Mortar): Level 44's fit with the Tesla Coil Pod L5, 24.5 of 25 MW, Rook flying the
     * Mortar (L1) for the reef's ground and its spawning organs.
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
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 4)),
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
     * The data: the units counted as enemies {@link #COUNTED} per difficulty (the waves', the organs not counted), the 14
     * pit waves each out of its own pit, the eight organs (183 HP at medium, the spawner alternating a cluster of 6 (easy
     * 5, hard 8) and a Skitter pair (hard 3) every 3 s from 1.5 s, held near the ship, its clock EMP-proof), the three
     * Threadcrawlers whole 441 at medium, the call organ's story (2 s rise, 64 × 40), the amber blink's bottom third.
     */
    @Test
    void theLevelHoldsItsCountItsPitsAndItsOrgans() {
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
            VrellLevel.PitSpec pits = level.vrell().pits().orElseThrow();
            assertEquals(14, pits.pits().size(), at + ": the pits");
            assertEquals(2.0, pits.tellSeconds(), 1e-9, at + ": the tell");
            assertEquals(0.5, pits.riseSeconds(), 1e-9, at + ": the rise");
            assertEquals(3.0, pits.closeSeconds(), 1e-9, at + ": the close");
            assertEquals(120, pits.holdWithin(), 1e-9, at + ": the hold");
            List<Integer> used = level.waves().stream()
                    .filter(wave -> wave.pit() >= 0)
                    .map(WaveSpec::pit)
                    .toList();
            assertEquals(IntStream.range(0, 14).boxed().toList(), used, at + ": each pit's own wave, in order");
            assertTrue(level.vrell().bottomThird(), at + ": the amber blink's bottom third");
            assertEquals(
                    new StoryPickups.Story("call-organ", 2.0, new vanguard.sim.Hitbox(64, 40)),
                    level.drops().story(),
                    at + ": the call organ's lift and box");
            List<EnemySpec> organs = level.groundUnits().stream()
                    .map(vanguard.sim.GroundUnit::enemy)
                    .filter(enemy -> enemy.slug().equals("spawning-organ"))
                    .toList();
            assertEquals(8, organs.size(), at + ": the organs");
            EnemySpec organ = organs.getFirst();
            assertTrue(!organ.countsAsEnemy(), at + ": an organ is not counted");
            assertTrue(!organ.hardened(), at + ": an organ is soft");
            EnemySpec.Spawner spawner = organ.spawner().orElseThrow();
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 5;
                        case MEDIUM -> 6;
                        case HARD -> 8;
                    },
                    spawner.count(),
                    at + ": the cluster");
            assertEquals(
                    difficulty == Difficulty.HARD ? 3 : 2,
                    spawner.alternate().orElseThrow().count(),
                    at + ": the Skitters");
            assertEquals("whirl-seed", spawner.enemy().slug());
            assertEquals("skitter", spawner.alternate().orElseThrow().enemy().slug());
            assertTrue(spawner.cluster() && spawner.hold() && spawner.empImmune(), at + ": cluster, hold, EMP-proof");
            assertEquals(1.5, spawner.firstSeconds(), 1e-9, at + ": the first release");
            assertEquals(3.0, spawner.everySeconds(), 1e-9, at + ": the cycle");
            assertEquals(120, spawner.shutWithin(), 1e-9, at + ": the hold's reach");
        }
        EnemySpec organ = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).groundUnits().stream()
                .map(vanguard.sim.GroundUnit::enemy)
                .filter(enemy -> enemy.slug().equals("spawning-organ"))
                .findFirst()
                .orElseThrow();
        assertEquals(183, organ.hp(), "an organ's HP at medium");
        EnemySpec crawler = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).waves().stream()
                .filter(wave -> wave.enemy().chain().isPresent())
                .findFirst()
                .orElseThrow()
                .enemy();
        EnemySpec.ChainSpec chain = crawler.chain().orElseThrow();
        assertEquals(
                441,
                crawler.hp()
                        + chain.segmentBoxes().size() * chain.segment().hp()
                        + chain.tail().hp(),
                "a whole Threadcrawler");
        System.out.println("Level 45 counted enemies: " + counted);
        assertEquals(COUNTED, counted);
    }

    /**
     * Step C2's waves' counts: the README's 66 waves (425 units at medium) and the 3 Threadcrawlers, with the denser
     * mixed waves the act's floor asks for (Chimera, Needler and Harrow wings and streams, Stinger columns and Spore
     * Bomber lines on the offbeats; the user's answer for Act 7, 2026-11-05), the organs not counted.
     */
    static final Map<Difficulty, Integer> COUNTED =
            Map.of(Difficulty.EASY, 527, Difficulty.MEDIUM, 664, Difficulty.HARD, 801);

    /** What a run saw, over every attempt (the losses) and in its completing one (the organs, the horn, the pits). */
    static final class Watch implements LevelMetrics.Probe {
        /** The ship's losses over every attempt: "t@x" each. */
        final List<String> deaths = new ArrayList<>();
        /** The organs that got away over every attempt: "t:group" each. */
        final List<String> escaped = new ArrayList<>();
        /** The organs killed in the completing attempt: their seconds on the screen. */
        final List<Double> organTimes = new ArrayList<>();
        /** The organs' first-seen level times, by serial, in the completing attempt. */
        final Map<Integer, Double> seen = new java.util.HashMap<>();
        /** The organs alive at the last step, by serial. */
        final java.util.Set<Integer> alive = new java.util.HashSet<>();
        /** Pit openings held (the ship within 120 px), in the completing attempt. */
        int held;
        /** Pits opened, in the completing attempt. */
        int opened;
        /** Steps Rook flew, in the completing attempt. */
        int rookSteps;
        /** The call organ taken (1), missed (-1) or neither (0), in the completing attempt, and when. */
        int story;

        double storyAt;
        /** Organ releases held late, measured as the releases seen, in the completing attempt. */
        int releases;

        @Override
        public void newAttempt() {
            organTimes.clear();
            seen.clear();
            alive.clear();
            held = 0;
            opened = 0;
            rookSteps = 0;
            story = 0;
            releases = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                SimEvents.Type type = events.type(i);
                if (type == SimEvents.Type.SHIP_DESTROYED) {
                    deaths.add(String.format("%.1f@%.0f", sortie.levelSeconds(), events.x(i)));
                } else if (type == SimEvents.Type.GROUP_LOST) {
                    escaped.add(String.format("%.1f:O%d", sortie.levelSeconds(), events.value(i) + 1));
                } else if (type == SimEvents.Type.PIT_HELD) {
                    held++;
                } else if (type == SimEvents.Type.PIT_OPEN) {
                    opened++;
                } else if (type == SimEvents.Type.STORY_COLLECTED) {
                    story = 1;
                    storyAt = sortie.levelSeconds();
                } else if (type == SimEvents.Type.STORY_MISSED) {
                    story = -1;
                    storyAt = sortie.levelSeconds();
                } else if (type == SimEvents.Type.ORGAN_RELEASE || type == SimEvents.Type.SPAWN_RELEASED) {
                    releases++;
                }
            }
            if (sortie.wingman().filter(rook -> !rook.ejected()).isPresent()) {
                rookSteps++;
            }
            java.util.Set<Integer> now = new java.util.HashSet<>();
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (!enemy.spec().slug().equals("spawning-organ") || enemy.hp() <= 0) {
                    continue;
                }
                now.add(enemy.serial());
                if (LevelMetrics.onScreen(enemy.renderX(1), enemy.renderY(1), enemy.hitbox())) {
                    seen.putIfAbsent(enemy.serial(), sortie.levelSeconds());
                }
            }
            for (Integer serial : alive) {
                if (!now.contains(serial) && seen.containsKey(serial)) {
                    organTimes.add(sortie.levelSeconds() - seen.get(serial));
                }
            }
            alive.clear();
            alive.addAll(now);
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
                "%n    %s: %s, retries %d, losses %s, organs away %s; organs on the screen %s s; pits opened %d, held %d;"
                        + " releases %d; Rook %.0f s; the horn %s at %.1f; %s, credits %d",
                flight.at(),
                flight.complete() ? "won" : "LOST",
                flight.run().retries(),
                watch.deaths,
                watch.escaped,
                watch.organTimes.stream().map(t -> String.format("%.1f", t)).toList(),
                watch.opened,
                watch.held,
                watch.releases,
                watch.rookSteps / 60.0,
                watch.story > 0 ? "taken" : watch.story < 0 ? "missed" : "-",
                watch.storyAt,
                flight.run().secondaryMet() ? "MET" : "failed",
                flight.run().credits());
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 45 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        for (Flight flight : flights) {
            String at = flight.at();
            assertTrue(flight.complete(), at + ": completes");
            assertEquals(8, flight.watch().organTimes.size(), at + ": all eight organs in the completing attempt");
            assertTrue(flight.watch().story != 0, at + ": the call organ taken or tugged");
        }
        long met =
                flights.stream().filter(flight -> flight.run().secondaryMet()).count();
        assertTrue(
                met >= SECONDARY_RUNS,
                fit + ": runs with the secondary met: " + met + " of " + flights.size() + ", at least "
                        + SECONDARY_RUNS);
    }

    /** The autopilot wins the level with all eight organs, the plan's fit and Rook, on every difficulty and seed. */
    @Test
    void theAutopilotBurnsEveryOrganWithRook() {
        report("Rook", flights("Rook", Level45Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotBurnsEveryOrganWithoutRook() {
        report("--escort none", flights("--escort none", Level45Test::withoutRook));
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level45Test::flyToEnd);

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
