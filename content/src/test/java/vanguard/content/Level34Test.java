package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.CueTrigger;
import vanguard.sim.DropSpec;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObject;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.HostSpecs;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.StoryPickups;
import vanguard.sim.WaveSpec;

/**
 * M9 part G, step G3: Level 34 Salvage Run from its data (design/campaign/act-5-the-belt/level-34-salvage-run; the
 * Warden Tank in the Ascendancy's livery, the Talon, Needler, Stinger, Asteroid Mite, Skitter, Mote Swarm): its count per
 * difficulty, {@code Strip the graveyard} over the six cargo containers per difficulty (an opened one drops its medium
 * salvage, one leaving the bottom edge counts away), the hold's four clamps freeing the Helix core (a story pickup: it
 * rises untouchable for 1 s, is collected by flying through it and pays nothing, no secret, no rating; its radio lines),
 * the mining charges beside the tank roads (enemies only), the three hidden crates and the beacon's drop, the tank's
 * livery (its stats unchanged); and whole runs of the autopilot ({@link SalvagePlay}) with the balance plan's fit, with
 * Rook and without him. The pacing, the floor, the density, the beam era's HP and the haul are the campaign-wide tests'
 * ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}); the
 * mechanics themselves the simulation's ({@code ObjectEscapesTest}, {@code StoryPickupTest}, {@code ChargeTest}).
 */
class Level34Test {
    static final String LEVEL = "act-5-the-belt/level-34-salvage-run";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds each difficulty is flown on, with Rook and without. */
    static final int SEEDS = 3;
    /** The containers that may get away for {@code Strip the graveyard}, by difficulty (of six). */
    static final Map<Difficulty, Integer> ALLOWED =
            Map.of(Difficulty.EASY, 2, Difficulty.MEDIUM, 1, Difficulty.HARD, 0);
    /** The cargo containers (CDF and Helix variants of the look). */
    static final int CONTAINERS = 6;
    /** The hidden crates' triggers' HP by difficulty and the crates (Act 1 terms). */
    static final Map<Difficulty, Double> SECRET_HP =
            Map.of(Difficulty.EASY, 20.0, Difficulty.MEDIUM, 20.0, Difficulty.HARD, 28.0);

    static final int CRATE = 100;
    /** A mining charge's burst: radius px, damage. */
    static final double CHARGE_RADIUS = 64;

    static final double CHARGE_DAMAGE = 80;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 34 with Rook in the slot: the plan's L34 visit buys nothing (saving for the Mk V),
     * so Level 33's fit with its power budget, 20 of 20 MW.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level33Test.planLoadout(content, difficulty);
    }

    /** The same without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level33Test.withoutRook(content, difficulty);
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
        int waves = level.waves().stream()
                .filter(wave -> wave.enemy().slug().equals(slug))
                .mapToInt(WaveSpec::count)
                .sum();
        int ground = (int) level.groundUnits().stream()
                .filter(unit -> unit.enemy().slug().equals(slug))
                .count();
        return waves + ground;
    }

    private static EnemySpec enemy(LevelScript level, String slug) {
        return level.waves().stream()
                .map(WaveSpec::enemy)
                .filter(enemy -> enemy.slug().equals(slug))
                .findFirst()
                .orElseThrow();
    }

    private static boolean container(LevelScript level, GroundObjectSpec object) {
        return !object.trigger() && level.objectives().secondary().countsObject(object.look());
    }

    private static boolean clamp(GroundObjectSpec object) {
        return object.group() >= 0 && object.groupDrop().equals(Optional.of(PickupType.STORY));
    }

    /**
     * Step G2's waves (the README's with the offbeat Talon trios, the Skitter snakes, the Mites on rocks): 407 enemies at
     * medium, 314 on easy, 457 on hard; the seven Warden Tank convoys (2 on easy, 3 on hard).
     */
    @Test
    void theLevelHoldsItsCount() {
        String[] slugs = {"skitter", "talon", "asteroid-mite", "mote-swarm", "needler", "warden-tank", "stinger"};
        Map<Difficulty, List<Integer>> counted = new EnumMap<>(Difficulty.class);
        Map<Difficulty, Integer> totals = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<Integer> counts = new ArrayList<>();
            for (String slug : slugs) {
                counts.add(count(level, slug));
            }
            counted.put(difficulty, counts);
            totals.put(
                    difficulty,
                    level.waves().stream().mapToInt(WaveSpec::count).sum()
                            + level.groundUnits().size());
        }
        System.out.println("Level 34 counts " + List.of(slugs) + ": " + counted + ", totals " + totals);
        assertEquals(Map.of(Difficulty.EASY, 314, Difficulty.MEDIUM, 407, Difficulty.HARD, 457), totals);
        assertEquals(TANKS, counted.get(Difficulty.MEDIUM).get(5), "medium: the convoys' tanks");
        assertEquals(EASY_TANKS, counted.get(Difficulty.EASY).get(5), "easy: convoys of 2");
        assertEquals(HARD_TANKS, counted.get(Difficulty.HARD).get(5), "hard: convoys of 3");
    }

    static final int TANKS = 18;
    static final int EASY_TANKS = 14;
    static final int HARD_TANKS = 21;

    /**
     * {@code Strip the graveyard}: {@code escapes: cargo-container} over the six containers (the CDF and Helix variants
     * of the look; 12 HP, a medium salvage each), label {@code CONTAINERS}, 100 in Act 1 terms; 2 may get away on easy,
     * 1 at medium, none on hard.
     */
    @Test
    void stripTheGraveyardIsTheSecondaryPerDifficulty() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            Secondary secondary = level.objectives().secondary();
            assertTrue(secondary.objects(), difficulty + ": over ground objects");
            assertEquals("cargo-container", secondary.escapes(), difficulty.name());
            assertEquals(ALLOWED.get(difficulty), secondary.escapesAllowed(), difficulty.name());
            assertEquals("CONTAINERS", secondary.label());
            assertEquals(100, secondary.credits());
            List<GroundObjectSpec> containers = level.groundObjects().stream()
                    .filter(object -> container(level, object))
                    .toList();
            assertEquals(CONTAINERS, containers.size(), difficulty + ": the six containers");
            for (GroundObjectSpec object : containers) {
                assertEquals(12, object.hp(), 1e-9, difficulty + ": a container's HP");
                assertEquals(Optional.of(PickupType.MEDIUM_SALVAGE), object.drop(), difficulty.name());
                assertTrue(object.look().startsWith("cargo-container-"), object.look());
            }
            Sortie sortie = sortie(content, planLoadout(content, difficulty), 1, difficulty, true);
            assertTrue(sortie.secondaryByEscapes(), difficulty.name());
            assertEquals(CONTAINERS, sortie.escapesTotal(), difficulty + ": CONTAINERS n/6");
        }
    }

    /**
     * Left alone (no shot fired, invulnerable), every container leaves the bottom edge and counts away: the secondary
     * fails at medium with the second one ({@code OBJECTIVE_FAILED} once), and nothing is opened.
     */
    @Test
    void aContainerLeavingTheBottomCountsAway() {
        Sortie sortie = sortie(content, withoutRook(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, true);
        int failed = 0;
        int lostAtFail = -1;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            sortie.step(vanguard.content.MovePlanner.steer(
                    sortie.ship().x(), sortie.ship().y(), 40, 40));
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                if (seen.type(i) == SimEvents.Type.OBJECTIVE_FAILED) {
                    failed++;
                    lostAtFail = sortie.escapesLost();
                }
            }
        }
        assertTrue(sortie.complete());
        assertEquals(CONTAINERS, sortie.escapesLost(), "every container away");
        assertEquals(0, sortie.escapesDestroyed());
        assertEquals(1, failed, "the secondary fails once");
        assertEquals(ALLOWED.get(Difficulty.MEDIUM) + 1, lostAtFail, "failed by the second away");
        assertTrue(sortie.secondaryFailed());
        assertFalse(sortie.result().secondaryMet());
    }

    /**
     * The hold: four clamps (15 HP, easy 10) round the Helix crate at t≈108 in one destructible group, whose last frees
     * the story pickup {@code helix-core} (never a {@code drop}); its two radio lines (collected, missed); the three hidden
     * crates (<em>Vigil</em>, <em>Sable Wren</em>, <em>Old Marta</em>: 20 HP, hard 28, crates of 100, Rook's line only
     * with him); the CDF beacon's armour patch at (235, 380) at t=60; the V-wings' leaders' shield cell and overdrive.
     */
    @Test
    void theHoldTheCratesAndTheDrops() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<GroundObjectSpec> clamps =
                    level.groundObjects().stream().filter(Level34Test::clamp).toList();
            assertEquals(4, clamps.size(), difficulty + ": four clamps");
            for (GroundObjectSpec clamp : clamps) {
                assertEquals(difficulty == Difficulty.EASY ? 10 : 15, clamp.hp(), 1e-9, difficulty.name());
                assertEquals(4, clamp.groupSize(), difficulty.name());
                assertTrue(clamp.drop().isEmpty(), difficulty + ": the core is the group's drop only");
                assertTrue(clamp.t() > 108 && clamp.t() < 109, difficulty + ": the hold at t≈108");
            }
            assertEquals(
                    240,
                    clamps.stream().mapToDouble(GroundObjectSpec::x).average().orElseThrow(),
                    1e-9);
            List<GroundObjectSpec> secrets = level.groundObjects().stream()
                    .filter(object -> object.secretIndex() >= 0)
                    .toList();
            assertEquals(
                    List.of("Vigil", "Sable Wren", "Old Marta"),
                    secrets.stream().map(GroundObjectSpec::secret).toList(),
                    difficulty.name());
            for (GroundObjectSpec secret : secrets) {
                assertEquals(SECRET_HP.get(difficulty), secret.hp(), 1e-9, difficulty + ": " + secret.secret());
                assertFalse(secret.hardened(), "the plan's beam opens it");
            }
            assertEquals(
                    List.of(new DropSpec.PlacedPickup(60, 235, 380, PickupType.ARMOUR_PATCH)),
                    level.drops().placedPickups(),
                    difficulty + ": the beacon's armour patch");
            List<RadioCue> radio = level.radio();
            assertEquals(
                    1,
                    radio.stream()
                            .filter(cue -> cue.trigger() == CueTrigger.STORY_COLLECTED)
                            .count(),
                    difficulty.name());
            assertEquals(
                    1,
                    radio.stream()
                            .filter(cue -> cue.trigger() == CueTrigger.STORY_MISSED)
                            .count(),
                    difficulty.name());
        }
        LevelData data = content.level(LEVEL);
        assertEquals(3, data.secrets().size());
        data.secrets().forEach(secret -> assertEquals(CRATE, secret.crate(), secret.name()));
        assertEquals("Rook", data.secrets().get(2).radio().speaker());
        assertEquals("nav-buoy", data.released().getFirst().sprite().orElseThrow(), "the CDF beacon");
    }

    /**
     * The Warden Tank in the Ascendancy's livery from Act 5 (its look and the intel's faction, content only): its stats
     * as the stat block's with the act HP factor, nothing else changed.
     */
    @Test
    void theTankFliesInLiveryWithItsStatsUnchanged() {
        EnemyData tank = content.enemy("warden-tank");
        EnemyData.Livery livery = tank.livery().orElseThrow();
        assertEquals(5, livery.fromAct());
        assertEquals(Optional.of("warden-tank-asc"), tank.liverySprite(5));
        assertEquals(Optional.empty(), tank.liverySprite(4), "its own look before Act 5");
        assertEquals("Jovian Ascendancy", tank.factionIn(5));
        double factor = SimSpecs.actHpFactor(content, "warden-tank", 5, 34);
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec base = SimSpecs.enemy(content, "warden-tank", difficulty, Optional.empty());
            EnemySpec here = enemy(SimSpecs.level(content, LEVEL, difficulty), "warden-tank");
            assertEquals(base.hp() * factor, here.hp(), 1, difficulty + ": the hull × the act HP factor");
            assertEquals(base.parts().size(), here.parts().size());
            for (int p = 0; p < base.parts().size(); p++) {
                HostSpecs.UnitPart part = base.parts().get(p);
                assertEquals(part.hp() * factor, here.parts().get(p).hp(), 1, difficulty + ": " + part.name());
                assertEquals(part.bounty(), here.parts().get(p).bounty(), difficulty + ": " + part.name());
            }
            assertEquals(base.bounty(), here.bounty(), difficulty + ": the bounty");
            System.out.printf(
                    "Level 34 Warden Tank (%s): hull %.0f, parts %s, factor %.3f%n",
                    difficulty,
                    here.hp(),
                    here.parts().stream()
                            .map(p -> p.name() + " " + Math.round(p.hp()))
                            .toList(),
                    factor);
        }
    }

    /** What one completing attempt saw. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<SimEvents.Type, Integer> events = new EnumMap<>(SimEvents.Type.class);
        /** The containers opened and away: "t x" each. */
        final List<String> opened = new ArrayList<>();

        final List<String> away = new ArrayList<>();
        /** The hidden crates found: "t x" each. */
        final List<String> found = new ArrayList<>();
        /** A container destroyed without its medium salvage dropping there in that step. */
        int noSalvage;
        /** Tanks killed by a charge's burst, and charge bursts that hurt the ship or Rook. */
        int tankByCharge;

        int chargeHurt;
        /** The story pickup: when it rose, was collected or missed; credits paid and secrets found in that step. */
        double rose = -1;

        double collected = -1;
        double missed = -1;
        int storyPaid;
        int storySecret;
        /** Steps the rising core was touchable (in the pool while rising). */
        int risingInPool;
        /** When the core's collected and missed lines went on the radio. */
        double collectedLine = -1;

        double missedLine = -1;

        private final LevelScript level;
        private final int tank;
        private int credits;
        private double shipHealth;
        private double rookHealth;
        private int lost;
        private int salvageBefore;

        Watch(Sortie sortie) {
            level = sortie.script();
            int kind = -1;
            List<EnemySpec> kinds = sortie.enemyKinds();
            for (int k = 0; k < kinds.size(); k++) {
                if (kinds.get(k).slug().equals("warden-tank")) {
                    kind = k;
                }
            }
            tank = kind;
        }

        @Override
        public void newAttempt() {
            events.clear();
            opened.clear();
            away.clear();
            found.clear();
            noSalvage = 0;
            tankByCharge = 0;
            chargeHurt = 0;
            rose = -1;
            collected = -1;
            missed = -1;
            storyPaid = 0;
            storySecret = 0;
            risingInPool = 0;
            collectedLine = -1;
            missedLine = -1;
            credits = 0;
            lost = 0;
            shipHealth = -1;
            rookHealth = -1;
            salvageBefore = 0;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SimEvents seen = sortie.events();
            double t = sortie.levelSeconds();
            int paid = sortie.credits() - credits;
            double ship =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            double rook = sortie.wingman().map(w -> w.armour()).orElse(0.0);
            int salvage = 0;
            for (int i = 0; i < sortie.pickupCount(); i++) {
                salvage += sortie.pickup(i).type() == PickupType.MEDIUM_SALVAGE ? 1 : 0;
                if (sortie.pickup(i).type() == PickupType.STORY
                        && sortie.storyPickup().orElseThrow().rising()) {
                    risingInPool++;
                }
            }
            boolean secretFound = false;
            for (int i = 0; i < seen.size(); i++) {
                events.merge(seen.type(i), 1, Integer::sum);
                secretFound |= seen.type(i) == SimEvents.Type.SECRET_FOUND;
            }
            int droppedSalvage = 0;
            for (int i = 0; i < seen.size(); i++) {
                switch (seen.type(i)) {
                    case GROUND_DESTROYED -> {
                        GroundObjectSpec object = level.groundObjects().get(seen.value(i));
                        if (container(level, object)) {
                            opened.add(String.format("%.1f@%.0f", t, seen.x(i)));
                            droppedSalvage++;
                        }
                    }
                    case CHARGE_BLAST -> {
                        double bx = seen.x(i);
                        double by = seen.y(i);
                        for (int k = 0; k < seen.size(); k++) {
                            if (seen.type(k) == SimEvents.Type.ENEMY_DESTROYED
                                    && seen.value(k) == tank
                                    && Math.hypot(seen.x(k) - bx, seen.y(k) - by) <= CHARGE_RADIUS + 32) {
                                tankByCharge++;
                            }
                        }
                        boolean near =
                                Math.hypot(sortie.ship().x() - bx, sortie.ship().y() - by) <= CHARGE_RADIUS + 30;
                        if (near && shipHealth >= 0 && ship < shipHealth - 1e-9 && !hit(seen)) {
                            chargeHurt++;
                        }
                        if (sortie.wingman().isPresent()
                                && rookHealth >= 0
                                && rook < rookHealth - 1e-9
                                && !hit(seen)
                                && Math.hypot(
                                                sortie.wingman().orElseThrow().x() - bx,
                                                sortie.wingman().orElseThrow().y() - by)
                                        <= CHARGE_RADIUS + 30) {
                            chargeHurt++;
                        }
                    }
                    case SECRET_FOUND -> found.add(String.format("%.1f@%.0f", t, seen.x(i)));
                    case STORY_RISE -> rose = t;
                    case STORY_COLLECTED -> {
                        collected = t;
                        storyPaid += paid;
                        storySecret += secretFound ? 1 : 0;
                    }
                    case STORY_MISSED -> missed = t;
                    case RADIO -> {
                        CueTrigger trigger = level.radio().get(seen.value(i)).trigger();
                        if (trigger == CueTrigger.STORY_COLLECTED) {
                            collectedLine = t;
                        } else if (trigger == CueTrigger.STORY_MISSED) {
                            missedLine = t;
                        }
                    }
                    default -> {}
                }
            }
            if (droppedSalvage > 0 && salvage < salvageBefore + droppedSalvage) {
                noSalvage++;
            }
            if (sortie.escapesLost() > lost) {
                away.add(String.format("%.1f", t));
                lost = sortie.escapesLost();
            }
            credits = sortie.credits();
            shipHealth = ship;
            rookHealth = rook;
            salvageBefore = salvage;
        }

        /** Whether something else hit the ship or Rook in this step (a bullet, a body). */
        private static boolean hit(SimEvents seen) {
            for (int i = 0; i < seen.size(); i++) {
                switch (seen.type(i)) {
                    case SHIELD_HIT, ARMOUR_HIT, PLATING_HIT, WINGMAN_HIT -> {
                        return true;
                    }
                    default -> {}
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
            int secretsFound,
            StoryPickups.State story) {
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
                complete ? sortie.result().secretsFound() : 0,
                sortie.storyPickup().orElseThrow().state());
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
                "%n    %s: retries %d, CONTAINERS %s (opened %s, away %s), credits %d; core %s (rose %.1f, collected"
                        + " %.1f, its line %.1f); secrets %d %s; charges %d (tanks %d)",
                flight.at(),
                flight.run().retries(),
                flight.run().secondaryMet(),
                watch.opened,
                watch.away,
                flight.run().credits(),
                flight.story(),
                watch.rose,
                watch.collected,
                watch.collectedLine,
                flight.secretsFound(),
                watch.found,
                watch.count(SimEvents.Type.CHARGE_BLAST),
                watch.tankByCharge);
    }

    /**
     * Each run's checks: completed; every opened container dropped its medium salvage; the core rose (untouchable while
     * rising) and was collected (flown through), paying nothing and finding no secret; no charge hurt the ship or Rook.
     */
    private static void check(Flight flight) {
        String at = flight.at();
        Watch watch = flight.watch();
        assertTrue(flight.complete(), at + ": completes");
        assertEquals(0, watch.noSalvage, at + ": an opened container without its medium salvage");
        assertTrue(watch.rose > 0, at + ": the clamps fall and the core rises");
        assertEquals(0, watch.risingInPool, at + ": the rising core is untouchable");
        assertEquals(StoryPickups.State.COLLECTED, flight.story(), at + ": the core flown through");
        assertTrue(watch.collected >= watch.rose + StoryPickups.RISE_SECONDS - 1e-9, at + ": collected after its rise");
        assertTrue(watch.collectedLine >= watch.collected, at + ": Okafor's line on the core collected");
        assertEquals(-1, watch.missedLine, at + ": no tug line");
        assertEquals(0, watch.storyPaid, at + ": the core pays nothing");
        assertEquals(0, watch.storySecret, at + ": the core is no secret");
        assertEquals(0, watch.chargeHurt, at + ": a charge's burst hurt the ship or Rook");
    }

    /**
     * The autopilot ({@link SalvagePlay}) completes the level with the plan's fit and Rook on every difficulty and seed,
     * opens the containers before they leave ({@code CONTAINERS} met on every run, all six on hard), shoots the clamps and
     * flies through the core, finds the hidden crates; the charges burst and kill passing tanks on easy and at medium (the
     * tally per run printed).
     */
    @Test
    void theAutopilotStripsTheGraveyardWithRook() {
        report("Rook", flights("Rook", Level34Test::planLoadout));
    }

    /** Without Rook ({@code --escort none}) the same. */
    @Test
    void theAutopilotStripsTheGraveyardWithoutRook() {
        report("--escort none", flights("--escort none", Level34Test::withoutRook));
    }

    private static void report(String fit, List<Flight> flights) {
        StringBuilder report = new StringBuilder("Level 34 autopilot (" + fit + ", " + SEEDS + " seeds):");
        for (Flight flight : flights) {
            report.append(summary(flight));
        }
        System.out.println(report);
        Map<Difficulty, int[]> tally = new EnumMap<>(Difficulty.class);
        for (Flight flight : flights) {
            check(flight);
            int[] sum = tally.computeIfAbsent(flight.difficulty(), d -> new int[4]);
            sum[0] += flight.run().secondaryMet() ? 1 : 0;
            sum[1] += flight.secretsFound();
            sum[2] += flight.watch().count(SimEvents.Type.CHARGE_BLAST);
            sum[3] += flight.watch().tankByCharge;
        }
        tally.forEach((difficulty, sum) -> {
            String at = fit + " " + difficulty;
            assertEquals(SEEDS, sum[0], at + ": CONTAINERS met on every run (all six on hard)");
            assertTrue(sum[1] > 0, at + ": hidden crates found");
            assertTrue(sum[2] > 0, at + ": the charges burst");
            if (difficulty != Difficulty.HARD) {
                // on hard a burst's 80 leaves a hull of 127 standing unless the guns took 48 of it first
                assertTrue(sum[3] > 0, at + ": a charge's burst kills a passing tank");
            }
        });
    }

    /**
     * The core missed: flown by the autopilot until the clamps fall and the core rises, then away to the bottom-left
     * corner without a shot: the core drifts off uncollected ({@code STORY_MISSED}, the tug's line on the radio), pays
     * nothing and the level still completes (the Mk V comes with the win).
     */
    @Test
    void aMissedCorePlaysTheTugsLine() {
        Sortie sortie = sortie(content, planLoadout(content, Difficulty.MEDIUM), 1, Difficulty.MEDIUM, true);
        StoryPickups story = sortie.storyPickup().orElseThrow();
        int steps = 0;
        while (!story.rising() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
        assertTrue(story.rising(), "the clamps fell");
        int missedEvents = 0;
        int collectedEvents = 0;
        int tugLines = 0;
        int corePaid = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            int credits = sortie.credits();
            sortie.step(MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), 30, 30));
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                switch (seen.type(i)) {
                    case STORY_MISSED -> {
                        missedEvents++;
                        corePaid += sortie.credits() - credits;
                    }
                    case STORY_COLLECTED -> collectedEvents++;
                    case RADIO ->
                        tugLines +=
                                sortie.script().radio().get(seen.value(i)).trigger() == CueTrigger.STORY_MISSED ? 1 : 0;
                    default -> {}
                }
            }
        }
        System.out.printf(
                "Level 34 the core missed: state %s, level end t %.1f%n", story.state(), sortie.levelSeconds());
        assertTrue(sortie.complete(), "the level completes without the core");
        assertEquals(StoryPickups.State.MISSED, story.state());
        assertEquals(1, missedEvents, "STORY_MISSED once");
        assertEquals(0, collectedEvents);
        assertEquals(1, tugLines, "the tug's line");
        assertEquals(0, corePaid, "a missed core pays nothing");
    }

    /**
     * A charge shot as a convoy passes: flown invulnerable with a Pulse Cannon L1 alone and no Rook, holding fire until a
     * Warden Tank of the t=50 convoy is in the picket charge's reach (out of the bolts' column below it), then shooting
     * the charge: its burst ({@value #CHARGE_DAMAGE} in {@value #CHARGE_RADIUS} px) takes the turret (silenced) of every
     * tank in reach and kills a tank whose hull it overwhelms: on easy (hull 73) the fresh tank itself, at medium and on
     * hard (97, 127) only one already hit (the autopilot's runs: {@link #theAutopilotStripsTheGraveyardWithRook}).
     */
    @Test
    void aChargeShotAsAConvoyPassesHitsItsTanks() {
        for (Difficulty difficulty : Difficulty.values()) {
            int[] hit = chargeAsConvoyPasses(difficulty);
            System.out.printf(
                    "Level 34 a charge shot as a convoy passes (%s): tanks killed %d, turrets lost %d%n",
                    difficulty, hit[0], hit[1]);
            assertTrue(hit[0] + hit[1] > 0, difficulty + ": the burst hit the convoy's tanks");
            if (difficulty == Difficulty.EASY) {
                assertTrue(hit[0] > 0, "easy: the burst kills a fresh tank (hull 73)");
            } else {
                assertEquals(0, hit[0], difficulty + ": a fresh hull outlasts the burst");
                assertTrue(hit[1] > 0, difficulty + ": the burst takes a turret");
            }
        }
    }

    /** The tanks killed and the tank turrets lost in the picket charge's burst. */
    private int[] chargeAsConvoyPasses(Difficulty difficulty) {
        Loadout bolts = SimSpecs.loadout(
                content,
                content.systems().engines().getFirst().name(),
                List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "pulse-cannon", 1)),
                content.shields().models().get(2).name(),
                content.armour().plating().get(4).name(),
                0,
                difficulty);
        Sortie sortie = sortie(content, bolts, 1, difficulty, true);
        int tank = -1;
        for (int k = 0; k < sortie.enemyKinds().size(); k++) {
            tank = sortie.enemyKinds().get(k).slug().equals("warden-tank") ? k : tank;
        }
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            GroundObject charge = null;
            for (int i = 0; i < sortie.groundObjectCount() && charge == null; i++) {
                GroundObject object = sortie.groundObject(i);
                if (object.spec().blast().isPresent() && object.renderY(1) < PlayField.HEIGHT - 20) {
                    charge = object;
                }
            }
            int fire = 0;
            double x = PlayField.WIDTH / 2.0;
            if (charge != null) {
                x = charge.renderX();
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy unit = sortie.enemy(i);
                    double dx = unit.renderX(1) - charge.renderX();
                    double dy = unit.renderY(1) - charge.renderY(1);
                    // in reach, and not in the bolts' column below the charge (they would hit the tank first)
                    if (unit.kind() == tank
                            && (dy > 0 || Math.abs(dx) > 30)
                            && Math.hypot(dx, dy) < CHARGE_RADIUS - 16
                            && Math.abs(sortie.ship().x() - charge.renderX()) < 6) {
                        fire = Command.FIRE.bit();
                    }
                }
            }
            sortie.step(
                    fire | MovePlanner.steer(sortie.ship().x(), sortie.ship().y(), x, 60));
            SimEvents seen = sortie.events();
            for (int i = 0; i < seen.size(); i++) {
                if (seen.type(i) == SimEvents.Type.CHARGE_BLAST) {
                    int killed = 0;
                    int turrets = 0;
                    for (int k = 0; k < seen.size(); k++) {
                        killed += seen.type(k) == SimEvents.Type.ENEMY_DESTROYED && seen.value(k) == tank ? 1 : 0;
                        turrets += seen.type(k) == SimEvents.Type.UNIT_PART_DESTROYED ? 1 : 0;
                    }
                    return new int[] {killed, turrets};
                }
            }
        }
        throw new AssertionError(difficulty + ": no charge burst as a convoy passed");
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(
                () -> sortie(content, planLoadout(content, Difficulty.HARD), 2185, Difficulty.HARD, false),
                Level34Test::flyToEnd);

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
