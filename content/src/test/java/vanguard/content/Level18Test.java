package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PickupType;
import vanguard.sim.RadioCue;
import vanguard.sim.Range;
import vanguard.sim.Rules;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M7 part E, step E4: Level 18 Dust Storm from its data (design/campaign/act-3-red-dust/level-18-dust-storm;
 * design/enemies/ground/dust-devil, design/enemies/air/ghost-drone; round 36's M6 = a, Q.18, Q.22e/h and the gap
 * check's defaults, pending the user's review in round 41): the six sections and the eye, the quiet windows (the first
 * Dust Devils alone, the Ghost Drones alone at hint 1 with a ground nest), the Dust Devil's cycle, vanes, pull and
 * grit spiral, the Ghost Drone's lane strafe and its pass-triggered burst, the rover-wreck secret, the pickups (the
 * overdrive, the supply drone's armour patch, the shield cell of the last devil), the kill-ratio secondary, hard's
 * Wraiths and second pass, and whole runs of the autopilot.
 */
class Level18Test {
    static final String LEVEL = "act-3-red-dust/level-18-dust-storm";
    private static final int MAX_STEPS = 60 * 60 * 30;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 18 ({@code "18"}: the Ion Beam bought at L2, the Pulse Cannon L4 sold): Level
     * 17's with the beam in front: two Micro-missile Pods L2 (homing, as the briefing recommends), the Tail Gun L2,
     * the second shield, Composite II, the Airstrike and Rook's Autocannon.
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "autocannon",
                        1,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()));
    }

    /** The plan's fit without Rook ({@code --escort none}; M7 part I, step I1). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "tail-gun", 2)),
                        content.shields().models().get(1).name(),
                        content.armour().plating().get(2).name(),
                        0,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "Airstrike", 4));
    }

    static Sortie sortie(Content content, long seed, Difficulty difficulty, boolean invulnerable) {
        Loadout loadout = planLoadout(content, difficulty);
        Rules rules = SimSpecs.rules(content, LEVEL, difficulty);
        return new Sortie(
                seed,
                loadout,
                SimSpecs.level(content, LEVEL, difficulty),
                invulnerable ? rules.withInvulnerableShip() : rules,
                loadout.plating().maxArmour());
    }

    /** The six sections (150 / 140 / 120 / 120 / 120 / 150 px/s), the speed changes eased over 2 s. */
    @Test
    void theSixSectionsAndTheirSpeeds() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        assertEquals(
                List.of(25.0, 55.0, 110.0, 130.0, 170.0, 183.0),
                level.sections().stream().map(Section::end).toList());
        assertEquals(
                List.of(150.0, 140.0, 120.0, 120.0, 120.0, 150.0),
                level.sections().stream().map(Section::speed).toList());
        assertEquals(18, level.number());
        assertEquals(3, level.act());
        assertEquals(5, level.launchSeconds());
        assertTrue(level.setPieces().isEmpty(), "no boss: the act's breather");
    }

    /**
     * The quiet windows: no wave but the first Dust Devils' over 41.5–45 and none but the first Ghost Drones' over
     * 69.5–73.5 (no Vrell air at hint 1), with a ground nest on the screen through it; the anchor waves at their times.
     */
    @Test
    void theQuietWindowsAndTheAnchorWaves() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<String> first = level.waves().stream()
                    .filter(w -> w.t() >= 41.5 && w.t() < 45)
                    .map(w -> w.t() + " " + w.enemy().slug())
                    .toList();
            assertEquals(List.of("41.5 dust-devil"), first, difficulty + ": the first devils alone");
            List<String> hint = level.waves().stream()
                    .filter(w -> w.t() >= 69.5 && w.t() < 73.5)
                    .map(w -> w.t() + " " + w.enemy().slug())
                    .toList();
            assertEquals(List.of("69.5 ghost-drone"), hint, difficulty + ": hint 1 alone, no Vrell air");
            assertTrue(
                    level.groundUnits().stream().anyMatch(unit -> unit.t() >= 68 && unit.t() <= 70),
                    "a ground nest in the hint-1 window");
            assertEquals(
                    List.of(41.5, 77.0, 116.5, 138.5),
                    times(level, "dust-devil"),
                    difficulty + ": the Dust Devils' anchor waves");
            assertEquals(
                    List.of(69.5, 87.5, 131.5, 145.5, 166.5),
                    times(level, "ghost-drone"),
                    difficulty + ": the Ghost Drones' anchor waves");
            assertEquals(
                    difficulty == Difficulty.HARD,
                    level.waves().stream()
                            .anyMatch(w -> w.enemy().slug().equals("wraith") && w.t() >= 149 && w.t() <= 151),
                    difficulty + ": hard's extra Wraith pair at the rear");
        }
    }

    private static List<Double> times(LevelScript level, String slug) {
        return level.waves().stream()
                .filter(w -> w.enemy().slug().equals(slug))
                .map(WaveSpec::t)
                .distinct()
                .toList();
    }

    /**
     * The Dust Devil: 52 HP (the act's HP factor of a {@code medium} unit), ground; its cycle 1.4 closed, 0.4 rise, 0.8
     * open (easy 1.0), 0.8 s apart in a wave, wandering 50 px/s up to 60 px, kept 90 px from the ship; its pull 130 px
     * at 35 px/s (easy 25, hard 45), 170 px on the pickups at 90; its grit a 2-arm spiral (hard 3) at 180°/s, a bullet
     * per arm every 0.16 s (the fire-rate lever), 110 px/s.
     */
    @Test
    void theDustDevilsCyclePullAndSpiral() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            EnemySpec devil = enemy(level, "dust-devil");
            assertEquals(Layer.GROUND, devil.layer());
            EnemySpec.Vortex vortex = devil.vortex().orElseThrow();
            assertEquals(1.4, vortex.closedSeconds());
            assertEquals(0.4, vortex.riseSeconds());
            assertEquals(difficulty == Difficulty.EASY ? 1.0 : 0.8, vortex.openSeconds(), difficulty + ": the window");
            assertEquals(0.8, vortex.offsetSeconds());
            assertEquals(50, vortex.wanderSpeed());
            assertEquals(60, vortex.wanderAmplitude());
            assertEquals(90, vortex.keepAway());
            EnemySpec.Pull pull = devil.pull().orElseThrow();
            assertEquals(130, pull.shipRadius());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 25;
                        case MEDIUM -> 35;
                        case HARD -> 45;
                    },
                    pull.shipSpeed(),
                    difficulty + ": the pull on the ship");
            assertEquals(170, pull.pickupRadius());
            assertEquals(90, pull.pickupSpeed());
            EnemyGun gun = devil.gun().orElseThrow();
            EnemyGun.SpiralSpec spiral = gun.spiral().orElseThrow();
            assertEquals(difficulty == Difficulty.HARD ? 3 : 2, spiral.arms(), difficulty + ": the arms");
            assertEquals(-Math.PI, spiral.turnRadiansPerSecond(), 1e-9, "180°/s clockwise on the screen");
            double lever = content.difficulty().enemyFireRate().of(difficulty);
            assertEquals(0.16 / lever, spiral.intervalSeconds(), 1e-9);
            assertEquals(110 * content.difficulty().enemyBulletSpeed().of(difficulty), gun.bulletSpeed(), 1e-9);
            assertFalse(gun.passTrigger());
        }
        assertEquals(
                52,
                enemy(SimSpecs.level(content, LEVEL, Difficulty.MEDIUM), "dust-devil")
                        .hp(),
                1e-9);
    }

    /**
     * The Ghost Drone: 14 HP doubled by the beam era (28; step E5), air, a machine; its lane strafe (180 px/s down to its lane 120–220 px below the top, 40
     * px apart, 150 px/s across; hard's second pass); its burst fired once per pass within 60 px of the ship's x, 0.3 s
     * after its eye glint, each shot re-aimed, 3 shots (easy 2, hard 4) 0.15 s apart at 210 px/s.
     */
    @Test
    void theGhostDronesLaneStrafeAndBurst() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            EnemySpec drone = enemy(level, "ghost-drone");
            assertEquals(Layer.AIR, drone.layer());
            assertEquals(
                    content.difficulty().enemyHp(14 * 2, difficulty),
                    drone.hp(),
                    1e-9,
                    "the beam era doubles a small unit's HP");
            assertTrue(drone.machine(), "the EMP Burst stuns it longest");
            EnemySpec.LaneStrafe strafe = drone.laneStrafe().orElseThrow();
            assertEquals(180, strafe.descentSpeed());
            assertEquals(150, strafe.strafeSpeed());
            assertEquals(new Range(120, 220), strafe.laneDepth());
            assertEquals(40, strafe.laneSpacing());
            assertEquals(difficulty == Difficulty.HARD, strafe.secondPass(), difficulty + ": the second pass");
            EnemyGun gun = drone.gun().orElseThrow();
            assertTrue(gun.passTrigger());
            assertEquals(60, gun.passWithinX());
            assertEquals(0.3, gun.tellSeconds());
            assertTrue(gun.reaim());
            assertEquals(
                    switch (difficulty) {
                        case EASY -> 2;
                        case MEDIUM -> 3;
                        case HARD -> 4;
                    },
                    gun.burst(),
                    difficulty + ": the burst");
            assertEquals(0.15, gun.burstGapSeconds());
            assertEquals(210 * content.difficulty().enemyBulletSpeed().of(difficulty), gun.bulletSpeed(), 1e-9);
            assertEquals(Double.POSITIVE_INFINITY, gun.intervalSeconds());
        }
    }

    /**
     * The rover wreck (a 20 HP ground trigger at x 60, t 90, crate 100), the overdrive on the t=101.5 leader, the
     * supply drone's armour patch at t=118 and the shield cell on the t=138.5 pack's last devil; the kill-ratio
     * secondary at 65 % for 100 (judged at the end).
     */
    @Test
    void theSecretPickupsAndTheSecondary() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<GroundObjectSpec> rover = level.groundObjects().stream()
                .filter(object -> object.crateCredits() > 0)
                .toList();
        assertEquals(1, rover.size());
        assertEquals(90, rover.getFirst().t());
        assertEquals(60, rover.getFirst().x());
        assertEquals(20, rover.getFirst().hp());
        assertEquals(100, rover.getFirst().crateCredits());
        assertEquals("rover wreck", rover.getFirst().secret());
        assertEquals(1, level.secrets());
        assertEquals(
                List.of(new WaveSpec.Carried(PickupType.OVERDRIVE, 0)),
                level.waves().stream()
                        .filter(w -> w.t() == 101.5)
                        .findFirst()
                        .orElseThrow()
                        .carried());
        WaveSpec lastPack = level.waves().stream()
                .filter(w -> w.t() == 138.5 && w.enemy().slug().equals("dust-devil"))
                .findFirst()
                .orElseThrow();
        assertEquals(List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, WaveSpec.Carried.LAST)), lastPack.carried());
        assertTrue(
                level.drops().placedPickups().stream()
                        .anyMatch(p -> p.t() == 118 && p.pickup() == PickupType.ARMOUR_PATCH),
                "the supply drone's armour patch in the eye");
        assertEquals(0.65, level.objectives().secondary().killRatio());
        assertEquals(100, level.objectives().secondary().credits());
    }

    /**
     * Flown invulnerable at medium without firing a shot: every devil's vanes are armoured unless it is open (it opens
     * at least once on the screen), the pull on the ship never passes the stat block's 35 px/s (never a trap at 250+),
     * the Ghost Drones glint before they fire, and the drones of the first wave are still flying at t=71.
     */
    @Test
    void theDevilsWindowsTheBoundedPullAndTheDronesBursts() {
        Sortie sortie = sortie(content, 18, Difficulty.MEDIUM, true);
        int steps = 0;
        int opened = 0;
        int glints = 0;
        int armouredOpen = 0;
        double pull = 0;
        boolean droneAt71 = false;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            sortie.step(0);
            SimEvents events = sortie.events();
            opened += events.count(SimEvents.Type.VORTEX_OPENED);
            glints += events.count(SimEvents.Type.EYE_GLINT);
            pull = Math.max(pull, Math.hypot(sortie.pullX(), sortie.pullY()));
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy e = sortie.enemy(i);
                if (e.hp() <= 0) {
                    continue;
                }
                if (e.spec().vortex().isPresent()) {
                    boolean open = e.vortexPhase() == Enemy.VortexPhase.OPEN;
                    if (open == e.armoured()) {
                        armouredOpen++;
                    }
                    if (open) {
                        assertEquals(Layer.LOW_AIR, e.layer(), "an open devil is on low-air");
                    }
                }
                if (e.spec().slug().equals("ghost-drone")
                        && sortie.levelSeconds() >= 71
                        && sortie.levelSeconds() < 71.1) {
                    droneAt71 = true;
                }
            }
        }
        assertTrue(sortie.complete());
        System.out.printf(
                "Level 18 untouched run: %d devil windows opened, %d drone glints, pull at most %.1f px/s%n",
                opened, glints, pull);
        assertEquals(0, armouredOpen, "the vanes are armoured exactly while the devil is not open");
        assertTrue(opened >= 9, "every devil opens on the screen: " + opened);
        assertTrue(glints >= 10, "the drones glint before their bursts: " + glints);
        assertTrue(pull > 0 && pull <= 35 + 1e-9, "the pull on the ship: " + pull);
        assertTrue(droneAt71, "a Ghost Drone flies at t=71 for Varga's line");
    }

    /** The units per kind at medium (waves and ground units), printed for the README's credit budget. */
    @Test
    void theEnemiesPerDifficulty() {
        Map<String, Integer> enemies = new TreeMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            Map<String, Integer> units = new TreeMap<>();
            for (WaveSpec wave : level.waves()) {
                units.merge(wave.enemy().slug(), wave.count(), Integer::sum);
            }
            enemies.put(
                    difficulty + " " + units,
                    units.values().stream().mapToInt(Integer::intValue).sum()
                            + level.groundUnits().size());
        }
        System.out.println("Level 18 enemies per difficulty (waves and ground units): " + enemies);
        assertEquals(
                24,
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).groundUnits().size(),
                "the README's nests and mortars");
    }

    /** The radio's data: the timed lines in order and at least 4 s apart; Rook's four lines only while he flies. */
    @Test
    void theRadiosData() {
        List<RadioCue> radio = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).radio();
        List<RadioCue> timed =
                radio.stream().filter(cue -> cue.trigger() == CueTrigger.TIME).toList();
        for (int i = 1; i < timed.size(); i++) {
            assertTrue(
                    timed.get(i).t() - timed.get(i - 1).t() >= 4,
                    timed.get(i - 1).t() + " and " + timed.get(i).t());
        }
        assertEquals(14, timed.size());
        assertEquals(
                List.of(14.0, 49.0, 86.0, 146.0),
                timed.stream()
                        .filter(cue -> (cue.requires() & RadioCue.FITTED_ESCORT) != 0)
                        .map(RadioCue::t)
                        .toList());
        radio.forEach(cue -> assertFalse(cue.line().isBlank()));
        assertEquals(
                List.of(CueTrigger.SECONDARY_OBJECTIVE, CueTrigger.LEVEL_END, CueTrigger.SECRET),
                radio.stream()
                        .map(RadioCue::trigger)
                        .filter(trigger -> trigger != CueTrigger.TIME)
                        .toList());
    }

    /** Seeds per difficulty for the autopilot's runs. */
    static final int SEEDS = 4;

    /**
     * The autopilot (holding fire on closed vanes, the core in its window, out of the pull; the Ghost Drones before
     * their pass, out of their lane) flies the level to its end with the plan's fit on every difficulty, over {@link
     * #SEEDS} seeds. Printed per difficulty: the kill-ratio secondary's rate, the mean kill ratio, the credits, the
     * attempts, the secret and the devils' windows used.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 18 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            int secrets = 0;
            long credits = 0;
            double ratio = 0;
            int devilsKilled = 0;
            int glanced = 0;
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                int devilKind = sortie.enemyKinds().stream()
                        .map(EnemySpec::slug)
                        .toList()
                        .indexOf("dust-devil");
                int[] seen = new int[2]; // shots glanced, devils killed
                boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, (flown, step) -> {
                    SimEvents events = flown.events();
                    seen[0] += events.count(SimEvents.Type.SHOT_GLANCED);
                    for (int i = 0; i < events.size(); i++) {
                        if (events.type(i) == SimEvents.Type.ENEMY_DESTROYED && events.value(i) == devilKind) {
                            seen[1]++;
                        }
                    }
                });
                glanced += seen[0];
                devilsKilled += seen[1];
                assertTrue(complete, difficulty + " seed " + seed);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                secrets += sortie.result().secretsFound();
                credits += run.credits();
                ratio += sortie.result().kills() / (double) sortie.result().enemies();
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, kill ratio %.2f, credits %.0f, retries %d, secrets %d, devils killed"
                            + " %.1f a run, shots glanced %.0f a run",
                    difficulty,
                    met,
                    SEEDS,
                    ratio / SEEDS,
                    credits / (double) SEEDS,
                    retries,
                    secrets,
                    devilsKilled / (double) SEEDS,
                    glanced / (double) SEEDS));
        }
        System.out.println(report);
    }

    /**
     * The autopilot completes the level without Rook too ({@code --escort none}), one seed per difficulty (M7 part I,
     * step I1). On hard it does not (it is still retrying after 30 minutes): printed, a round-45 finding pending the
     * user's review; the test fails once it does, so that the exception is removed here.
     */
    @Test
    void theAutopilotFliesTheLevelWithoutRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelMetrics.Run run =
                    LevelMetrics.autopilot(content, LEVEL, difficulty, withoutRook(content, difficulty), 1, MAX_STEPS);
            String outcome = String.format(
                    "Level 18 without Rook, %s: %s after %d retries",
                    difficulty, run.complete() ? "won" : "not won", run.retries());
            System.out.println(outcome);
            if (difficulty == Difficulty.HARD) {
                assertFalse(run.complete(), outcome + ": remove the hard exception");
            } else {
                assertTrue(run.complete(), outcome);
            }
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level18Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(content, 77, Difficulty.MEDIUM, false);
        flyToEnd(first);
        flyToEnd(second);

        assertEquals(first.stateHash(), second.stateHash());
    }

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }

    private static EnemySpec enemy(LevelScript level, String slug) {
        return level.waves().stream()
                .map(WaveSpec::enemy)
                .filter(enemy -> enemy.slug().equals(slug))
                .findFirst()
                .orElseThrow();
    }
}
