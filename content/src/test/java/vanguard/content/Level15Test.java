package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.sim.Allocations;
import vanguard.sim.CueTrigger;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Layer;
import vanguard.sim.LevelResult;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M7 part B, step B3: Level 15 Olympus Descent from its data (design/campaign/act-3-red-dust/level-15-olympus-descent;
 * M7 = a, the user's Q1 = a of 2026-10-14 and the stated defaults, pending the user's review in round 38): the
 * README's totals, the five sections with their eased speed changes, the drop launch, the Burrower's burrow and the
 * Dust Kite's swoop at each difficulty, the first Burrower field alone, the no-surface zones, the rubble trigger,
 * the radio's events, the pickups, and the autopilot flying the level to its end with the plan's fit and Rook on
 * every difficulty (printed: the Burrowers' real kill share, the README's 80 %).
 */
class Level15Test {
    static final String LEVEL = "act-3-red-dust/level-15-olympus-descent";
    private static final int MAX_STEPS = 60 * 60 * 30;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 15: Level 14's (the L15 visit buys one EMP charge, kept in the inventory, and
     * fits the Airstrike again).
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level14Test.planLoadout(content, difficulty);
    }

    static Sortie sortie(Content content, long seed, Difficulty difficulty) {
        Loadout loadout = planLoadout(content, difficulty);
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, LEVEL, difficulty),
                SimSpecs.rules(content, LEVEL, difficulty),
                loadout.plating().maxArmour());
    }

    @Test
    void aPerfectRunAtMediumEarnsTheReadmesTotal() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        int kills = 0;
        for (WaveSpec wave : level.waves()) {
            kills += wave.count() * wave.enemy().bounty();
        }
        int ground = level.groundUnits().stream()
                .mapToInt(unit -> unit.enemy().bounty())
                .sum();
        int crates = content.level(LEVEL).secrets().stream()
                .mapToInt(LevelData.Secret::crate)
                .sum();
        assertEquals(1_805, Math.round(content.economy().budget().of(level.number())), "700 × 1.07¹⁴");
        assertEquals(84 * 4 + 37 * 5 + 73 * 12 + 56 * 15 + 4 * 25 + 27 * 24 + 8 * 18 + 3 * 22, kills);
        assertEquals(4 * 15 + 10 * 12, ground, "the Polyp Mortars and the Spine Turrets");
        assertEquals(100, crates, "the drop pod's crate in Act 1 terms (pays 256)");
        assertEquals(80, level.objectives().secondary().credits(), "clear the slopes, in Act 1 terms (pays 205)");
        assertEquals(
                0.26, content.level(LEVEL).bounties(), "fitted by TypicalHaulTest (step PE15: typical 1,796, -0.5 %)");
    }

    @Test
    void theEnemiesAreTheReadmesTotals() {
        Map<Difficulty, Integer> enemies = new TreeMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            Map<String, Integer> waves = new TreeMap<>();
            for (WaveSpec wave : level.waves()) {
                waves.merge(wave.enemy().slug(), wave.count(), Integer::sum);
            }
            Map<String, Integer> ground = new TreeMap<>();
            for (GroundUnit unit : level.groundUnits()) {
                ground.merge(unit.enemy().slug(), 1, Integer::sum);
            }
            if (difficulty == Difficulty.MEDIUM) {
                assertEquals(
                        Map.of(
                                "dust-kite", 84,
                                "skitter", 37,
                                "needler", 73,
                                "stinger", 56,
                                "spore-bomber", 4,
                                "burrower", 27,
                                "ravager", 8,
                                "creeper", 3),
                        waves);
                assertEquals(Map.of("spine-turret", 10, "polyp-mortar", 4), ground, "292 + 14 = 306 enemies");
            }
            assertEquals(
                    difficulty == Difficulty.HARD ? 12 : 10,
                    ground.get("spine-turret"),
                    difficulty + ": the rail-line pair on hard only");
            enemies.put(
                    difficulty,
                    level.waves().stream().mapToInt(WaveSpec::count).sum()
                            + level.groundUnits().size());
        }
        System.out.println("Level 15 enemies per difficulty (waves and ground units): " + enemies);
    }

    /**
     * The five sections at 190, 170, 150, 150 and 160 px/s, each speed change eased over 1 s (the moving sections'
     * {@code ramp}); the launch drops the ship in from above ({@code launch: drop}, a default pending the user's review).
     */
    @Test
    void theSectionsEaseTheirSpeedChangesAndTheLaunchDrops() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        assertEquals(
                List.of(30.0, 65.0, 115.0, 150.0, 190.0),
                level.sections().stream().map(Section::end).toList());
        assertEquals(
                List.of(190.0, 170.0, 150.0, 150.0, 160.0),
                level.sections().stream().map(Section::speed).toList());
        assertEquals(
                List.of(0.0, 1.0, 1.0, 0.0, 1.0),
                level.sections().stream().map(Section::easeSeconds).toList());
        assertTrue(level.launchDrop());
        assertEquals(5, level.launchSeconds());
        // The README's scroll column: the eased changes run a few px past the round numbers.
        LevelData data = content.level(LEVEL);
        assertEquals(30_815, Math.round(data.scrollAt(190)), 1);
        assertEquals(5_700, data.scrollAt(30), 1e-6);
    }

    /**
     * The Burrower's burrow per difficulty (its stat block and hooks): medium a ring of 10 at 0.4 s and 2 s up; easy
     * a ring of 8 and 2.4 s; hard a ring of 12, a second of 8 at 1.4 s and 1.8 s; three surfacings, the spot rule,
     * the stagger of 0.8 s and two up at once; ground while up, no gun. The Dust Kite swoops within 180 px of the ship
     * at 200 px/s (hard 230).
     */
    @Test
    void theBurrowerAndTheKiteFlyTheirStatBlocks() {
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec burrower = SimSpecs.enemy(content, "burrower", difficulty, Optional.empty());
            EnemySpec.Burrow burrow = burrower.burrow().orElseThrow();
            assertEquals(Layer.GROUND, burrower.layer());
            assertTrue(burrower.gun().isEmpty(), "its ring is the burrow's");
            assertEquals(90, burrow.swimSpeed());
            assertEquals(new EnemySpec.SurfaceSpot(140, 240, 110, 40, 110), burrow.spot());
            assertEquals(1.0, burrow.bulgeSeconds());
            assertEquals(0.4, burrow.diveSeconds());
            assertEquals(3, burrow.surfacings());
            assertEquals(0.8, burrow.staggerSeconds());
            assertEquals(2, burrow.maxUp());
            assertEquals(MarsRules.BURROW_LIFE_SECONDS, burrow.lifeSeconds());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 2.4;
                        case MEDIUM -> 2.0;
                        case HARD -> 1.8;
                    },
                    burrow.surfacedSeconds());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 8;
                        case MEDIUM -> 10;
                        case HARD -> 12;
                    },
                    burrow.ring().count());
            assertEquals(0.4, burrow.ring().atSeconds());
            assertEquals(
                    difficulty == Difficulty.HARD
                            ? Optional.of(new EnemySpec.BurrowRing(
                                    8,
                                    burrow.ring().bulletSpeed(),
                                    burrow.ring().damage(),
                                    1.4))
                            : Optional.empty(),
                    burrow.secondRing());

            EnemySpec kite = SimSpecs.enemy(content, "dust-kite", difficulty, Optional.empty());
            EnemySpec.Swoop swoop = kite.swoop().orElseThrow();
            assertEquals(180, swoop.triggerAbove());
            assertEquals(difficulty == Difficulty.HARD ? 230 : 200, swoop.speed());
            assertEquals(MarsRules.SWOOP_BEND, swoop.bend());
            assertTrue(kite.sine().isPresent(), "it glides on a sine until it swoops");
        }
        EnemySpec skitter = SimSpecs.enemy(content, "skitter", Difficulty.MEDIUM, Optional.empty());
        assertTrue(skitter.burrow().isEmpty() && skitter.swoop().isEmpty(), "nothing else burrows or swoops");
    }

    /** The first Burrower field enters alone at t=68: no other wave until t=73; six fields in all. */
    @Test
    void theFirstFieldIsAlone() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Double> fields = level.waves().stream()
                .filter(wave -> wave.enemy().burrow().isPresent())
                .map(WaveSpec::t)
                .toList();
        assertEquals(List.of(68.0, 80.0, 98.0, 125.0, 153.0, 172.0), fields);
        assertTrue(level.waves().stream()
                .filter(wave -> wave.enemy().burrow().isEmpty())
                .noneMatch(wave -> wave.t() > 62.5 && wave.t() < 73));
        assertTrue(level.groundUnits().stream().noneMatch(unit -> unit.t() >= 68 && unit.t() < 73));
        assertTrue(level.waves().stream()
                .filter(wave -> wave.enemy().burrow().isPresent())
                .allMatch(wave -> wave.formation() == WaveSpec.Formation.FIELD
                        && wave.field().isPresent()));
    }

    /**
     * The no-surface zones: the station's pads and hab, the escarpment's cliff face and the rail line, each across
     * its width and between the times its near and far rows cross the top edge (its centre passes the middle of the
     * screen at its {@code t}).
     */
    @Test
    void theNoSurfaceZonesCoverTheStationTheCliffAndTheRailLine() {
        LevelData data = content.level(LEVEL);
        List<GroundSpec.NoSurface> zones =
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).ground().noSurface();
        assertEquals(data.noSurfaceZones().size(), zones.size());
        assertEquals(5, zones.size());
        for (int i = 0; i < zones.size(); i++) {
            LevelData.NoSurface placed = data.noSurfaceZones().get(i);
            GroundSpec.NoSurface zone = zones.get(i);
            assertEquals(placed.x() - placed.size().width() / 2.0, zone.left(), 1e-9);
            assertEquals(placed.x() + placed.size().width() / 2.0, zone.right(), 1e-9);
            double centre = data.scrollAt(placed.t());
            assertEquals(
                    centre - placed.size().height() / 2.0 - PlayField.HEIGHT / 2.0, data.scrollAt(zone.from()), 0.01);
            assertEquals(
                    centre + placed.size().height() / 2.0 - PlayField.HEIGHT / 2.0, data.scrollAt(zone.to()), 0.01);
        }
        GroundSpec.NoSurface rail = zones.getLast();
        assertTrue(rail.from() > 180 && rail.to() < 190, "the rail line crosses the top edge before the end");
        assertTrue(
                SimSpecs.level(content, "act-2-homefront/level-14-siege-spire", Difficulty.MEDIUM)
                        .ground()
                        .noSurface()
                        .isEmpty(),
                "only with Burrowers");
    }

    @Test
    void theRubbleTheRadioAndThePickups() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            GroundObjectSpec rubble = level.groundObjects().stream()
                    .filter(object -> object.secretIndex() >= 0)
                    .findFirst()
                    .orElseThrow();
            assertEquals(difficulty == Difficulty.HARD ? 6 : 4, rubble.hits(), difficulty + ": the rubble's hits");
            assertEquals(100, rubble.crateCredits());
            assertEquals(1, level.secrets());
        }
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<CueTrigger> events = level.radio().stream()
                .map(RadioCue::trigger)
                .filter(trigger -> trigger != CueTrigger.TIME)
                .toList();
        assertEquals(
                List.of(
                        CueTrigger.FIRST_SURFACING,
                        CueTrigger.SECONDARY_OBJECTIVE,
                        CueTrigger.LEVEL_END,
                        CueTrigger.SECRET),
                events,
                "the first surfacing's, the secondary's, the end's and the secret's lines are events");
        assertEquals(
                List.of(PickupType.OVERDRIVE, PickupType.ARMOUR_PATCH),
                level.waves().stream()
                        .flatMap(wave -> wave.carried().stream())
                        .map(WaveSpec.Carried::pickup)
                        .toList(),
                "the Stinger leader's overdrive, the last Stinger's patch");
        LevelData data = content.level(LEVEL);
        assertEquals("reach-end", data.objectives().primary());
        assertEquals(2, data.briefing().pages().size());
    }

    /**
     * The autopilot flies the level to its end with the plan's fit and Rook on every difficulty. Printed: the
     * attempts, the Burrowers' surfacings and their real kill share (killed against killed plus escaped; the
     * README counts them at the ground rate, 80 %), the secret, the secondary, the credits and the grade.
     */
    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void theAutopilotFliesTheLevelToItsEnd(Difficulty difficulty) {
        Sortie sortie = sortie(content, 2185, difficulty);
        int burrower = -1;
        for (int k = 0; k < sortie.enemyKinds().size(); k++) {
            if (sortie.enemyKinds().get(k).burrow().isPresent()) {
                burrower = k;
            }
        }
        int firstSurfacingCue = cueIndex(sortie.script(), CueTrigger.FIRST_SURFACING);
        int kind = burrower;
        Watch watch = new Watch();
        watch.t = sortie.levelSeconds();
        LevelMetrics.fly(sortie, MAX_STEPS, new LevelMetrics.Probe() {
            @Override
            public void newAttempt() {
                watch.deaths += String.format("%.0f ", watch.t);
                watch.surfaced = watch.killed = watch.escaped = watch.secrets = watch.firstSurfacing = 0;
            }

            @Override
            public void stepped(Sortie flown, int step) {
                SimEvents events = flown.events();
                for (int i = 0; i < events.size(); i++) {
                    switch (events.type(i)) {
                        case BURROW_SURFACED -> watch.surfaced++;
                        case BURROW_ESCAPED -> watch.escaped++;
                        case ENEMY_DESTROYED -> watch.killed += events.value(i) == kind ? 1 : 0;
                        case SECRET_FOUND -> watch.secrets++;
                        case RADIO -> watch.firstSurfacing += events.value(i) == firstSurfacingCue ? 1 : 0;
                        default -> {}
                    }
                }
                watch.t = flown.levelSeconds(); // the next step's start: where a loss is reported
            }
        });
        String deaths = watch.deaths;
        int surfaced = watch.surfaced;
        int killed = watch.killed;
        int escaped = watch.escaped;
        int secrets = watch.secrets;
        int firstSurfacing = watch.firstSurfacing;
        LevelResult result = sortie.result();
        System.out.printf(
                "Level 15 %s: attempt %d%s, real %.1f s, Burrowers surfaced %d times, killed %d, escaped %d (%.0f %%"
                        + " killed), kills %d/%d, secondary %s, pod %s, credits %d (%s), Rook %s, grade %s%n",
                difficulty,
                sortie.attempt(),
                deaths.isEmpty() ? "" : " (lost at t=" + deaths.strip() + ")",
                sortie.realSeconds(),
                surfaced,
                killed,
                escaped,
                100.0 * killed / Math.max(1, killed + escaped),
                result.kills(),
                result.enemies(),
                result.secondaryMet() ? "met" : "failed",
                secrets > 0 ? "found" : "missed",
                result.credits().total(),
                result.credits(),
                sortie.wingman()
                        .map(rook -> rook.ejected() ? "ejected" : "home")
                        .orElse("none"),
                result.grade().letter());
        assertTrue(sortie.complete());
        assertTrue(surfaced > 0, "the Burrowers surface");
        assertEquals(1, firstSurfacing, "the first-surfacing line once");
    }

    /** What {@link #theAutopilotFliesTheLevelToItsEnd} counts on the attempt that completes the level. */
    private static final class Watch {
        double t;
        String deaths = "";
        int surfaced;
        int killed;
        int escaped;
        int secrets;
        int firstSurfacing;
    }

    /** The index of the script's first radio cue with {@code trigger}; -1 for none. */
    private static int cueIndex(LevelScript script, CueTrigger trigger) {
        for (int i = 0; i < script.radio().size(); i++) {
            if (script.radio().get(i).trigger() == trigger) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD), Level15Test::fly);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, 77, Difficulty.MEDIUM);
        Sortie second = sortie(content, 77, Difficulty.MEDIUM);
        fly(first);
        fly(second);

        assertEquals(first.stateHash(), second.stateHash());
    }

    private static void fly(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
