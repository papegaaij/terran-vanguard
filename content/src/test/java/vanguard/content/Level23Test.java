package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.CueTrigger;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.MediumSpec;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialLimits;
import vanguard.sim.Terrain;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M8 part C, step C3: Level 23 Through the Ice from its data (design/campaign/act-4-deep-water/level-23-through-the-ice;
 * design/enemies/naval/eel-swarm, glassfin; design/enemies/ground/siphon-pump): its count, its five sections and the
 * waterline at 25.5 s with the special limits it switches, the drilled shaft's walls and their touch, the quiet windows,
 * the Eel Swarm (its segments, the scattering cut, the head's two attacks and its sway) and the Glassfin's ribbon and
 * ram by difficulty, the returning units under water (the Driftjelly on the play plane, the Reef Spitter on the floor),
 * the head-first shield cell, the PUMPS secondary, the radio, and whole runs of the autopilot with the balance plan's fit
 * ({@link WaterPlay}: the drag, the pumps, the rams) with and without Rook. The dry shaft (round 50 Q.1 a: the NO WATER
 * lesson) is measured and printed. The pacing, the floor, the beam era's HP and the haul are the campaign-wide tests'
 * ({@link PacingTest}, {@link ActFloorTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level23Test {
    static final String LEVEL = "act-4-deep-water/level-23-through-the-ice";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The waterline: the start of the second data section (gap check D1). */
    static final double WATERLINE = 25.5;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 23 ({@code "23"}: the Tail Gun sold, the Depth Charges bought and to L3, the
     * Harpoon Torpedoes fitted in front and to L3, the Sonar Pulse fitted with its free charge): the wings' two
     * Micro-missile Pods L2, the Mk II shield, Composite III, and Rook's Autocannon.
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

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "harpoon-torpedoes", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "depth-charges", 3)),
                        content.shields().models().get(1).name(),
                        content.armour().plating().get(3).name(),
                        0,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "Sonar Pulse", 1));
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

    /**
     * The README: 352 enemies at medium, the waves' 334 (Glassfin 238, Driftjelly 31, Lamprey 30, Eel Swarm 10, Stinger
     * 9, Needler 6, Mote Swarm 6, Skitter 4) and the ground targets' 18 (6 Spine Turrets, 7 Reef Spitters, 5 siphon
     * pumps); the survey probe is a trigger with its secret.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        assertEquals(334, level.waves().stream().mapToInt(LevelMetrics::planned).sum());
        assertEquals(
                238,
                level.waves().stream()
                        .filter(wave -> wave.enemy().slug().equals("glassfin"))
                        .mapToInt(WaveSpec::count)
                        .sum());
        assertEquals(
                10,
                level.waves().stream()
                        .filter(wave -> wave.enemy().slug().equals("eel-swarm"))
                        .mapToInt(WaveSpec::count)
                        .sum());
        assertEquals(18, level.groundUnits().size());
        assertEquals(1, level.secrets());
    }

    /**
     * Five data sections for the README's four (gap check D1): the shaft mouth at 100 px/s to the waterline at 25.5 s,
     * the waterline to 28 s, the wet shaft at 90 to 72 s, Thera Deep at 80 to 140 s and the plain's edge at 100 to 165
     * s, each change of speed eased over 2 s; the water from the waterline on.
     */
    @Test
    void theSectionsDiveAtTheWaterline() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Section> sections = level.sections();
        assertEquals(
                List.of(25.5, 28.0, 72.0, 140.0, 165.0),
                sections.stream().map(Section::end).toList());
        assertEquals(
                List.of(100.0, 100.0, 90.0, 80.0, 100.0),
                sections.stream().map(Section::speed).toList());
        for (int i = 2; i < 5; i++) {
            assertEquals(2, sections.get(i).rampSeconds(), "section " + (i + 1));
        }
        MediumSpec medium = level.medium();
        assertTrue(medium.water());
        assertEquals(
                List.of(new MediumSpec.Switch(WATERLINE, true)),
                medium.switches().stream().filter(MediumSpec.Switch::underwater).toList());
        assertEquals(List.of(WATERLINE), content.level(LEVEL).waterlines());
    }

    /**
     * No {@code special_limits} key (gap check D9): the script carries no limits of its own; the setting's apply by the
     * medium: in the dry shaft the Sonar Pulse has no water, from the waterline on the Airstrike and the Orbital Lance
     * cannot reach under the ice and the Sonar Pulse is free.
     */
    @Test
    void theSpecialLimitsSwitchAtTheWaterline() {
        assertEquals(
                SpecialLimits.NONE,
                SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).specialLimits());
        LevelData level = content.level(LEVEL);
        for (int i = 0; i < level.sections().size(); i++) {
            List<String> forbidden = level.forbiddenSpecials(i, content.specials());
            if (level.sectionStart(i) < WATERLINE) {
                assertEquals(List.of("Sonar Pulse"), forbidden, "section " + (i + 1));
            } else {
                assertTrue(forbidden.containsAll(List.of("Airstrike", "Orbital Lance")), "section " + (i + 1));
                assertFalse(forbidden.contains("Sonar Pulse"), "section " + (i + 1));
            }
        }
    }

    /**
     * The drilled shaft (Level 16's canyon rule): walls at the field's edges from t=6, the shaft 320 px wide from t=14
     * to 66, open to the full 480 by t≈74; a touch costs 4 (easy 2), pushes 30 px for 0.12 s, once a second.
     */
    @Test
    void theShaftsWallsAndTheirTouch() {
        for (Difficulty difficulty : Difficulty.values()) {
            GroundSpec.TerrainSpec terrain = SimSpecs.level(content, LEVEL, difficulty)
                    .ground()
                    .terrain()
                    .orElseThrow();
            assertEquals(difficulty == Difficulty.EASY ? 2 : 4, terrain.damage(), difficulty.name());
            assertEquals(30, terrain.push());
            assertEquals(0.12, terrain.pushSeconds());
            assertEquals(1.0, terrain.cooldownSeconds());
            Terrain rock = terrain.terrain();
            assertEquals(480, rock.rightAt(600) - rock.leftAt(600), 1e-9, "the mouth");
            assertEquals(320, rock.rightAt(3_000) - rock.leftAt(3_000), 1e-9, "the shaft");
            assertEquals(320, rock.rightAt(6_230) - rock.leftAt(6_230), 1e-9, "the shaft's end");
            assertEquals(480, rock.rightAt(6_940) - rock.leftAt(6_940), 1e-9, "the farms");
        }
    }

    /** The quiet windows: no other wave enters t=28.5–32 (the first school) or t=42–45.5 (the first eel). */
    @Test
    void theNewUnitsHaveTheirQuietWindows() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                double t = wave.t();
                assertFalse(t > 28.5 && t < 32, difficulty + ": a wave at t=" + t);
                assertFalse(t > 42 && t < 45.5, difficulty + ": a wave at t=" + t);
            }
        }
    }

    /**
     * The Eel Swarm: 12 segments (easy 10, hard 16), a cut's rear part scattering at 70 px/s for 3 s (no regrowth),
     * the head's aimed shot every 1.8 s (hard 1.4 s) and its 3-way fan every 5 s (none on easy), a 0.3 s tell before
     * each, the aimed shot first; its head sways 36 px at 0.8 Hz about its path; a head-first kill pays 48.
     */
    @Test
    void theEelSwarmCutsScatterAndItsHeadFiresTwice() {
        int[] segments = {10, 12, 16};
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec eel = wave(difficulty, 42).enemy();
            EnemySpec.ChainSpec chain = eel.chain().orElseThrow();
            assertEquals(segments[difficulty.ordinal()], chain.segmentBoxes().size(), difficulty.name());
            assertEquals(new EnemySpec.Scatter(70, 3), chain.scatter().orElseThrow());
            assertEquals(null, chain.regrown());
            assertTrue(eel.gun().isEmpty(), "the head's attacks are its chain's");
            assertEquals(new EnemySpec.Sine(36, 1.25), eel.sine().orElseThrow());
            assertEquals(18, chain.headFirstBonus());
            assertEquals(4, chain.tailFirstBonus());
            List<EnemyGun> attacks = chain.headAttacks();
            double rate = content.difficulty().enemyFireRate().of(difficulty);
            assertEquals(difficulty == Difficulty.EASY ? 1 : 2, attacks.size(), difficulty.name());
            EnemyGun aimed = attacks.getFirst();
            assertEquals(1, aimed.fan());
            assertEquals((difficulty == Difficulty.HARD ? 1.4 : 1.8 / rate), aimed.intervalSeconds(), 1e-9);
            assertEquals(0.3, aimed.tellSeconds());
            if (difficulty != Difficulty.EASY) {
                EnemyGun fan = attacks.get(1);
                assertEquals(3, fan.fan());
                assertEquals(Math.toRadians(40), fan.spreadRadians(), 1e-12);
                assertEquals(5 / rate, fan.intervalSeconds(), 1e-9);
                assertTrue(aimed.firstShotDelay() < fan.firstShotDelay(), "the aimed shot first");
            }
        }
    }

    /**
     * The Glassfin: a ribbon swaying 14 px at 1.4 Hz, the ram after 3 s on the screen with a 0.3 s glint and the dive
     * 0.5 s after it at 230 px/s; a school's share that rams 0.15 easy, 0.25 medium, 0.40 hard; the seekers ignore it.
     * The ribbons ({@code column}) fly straight columns down the shaft's sides (a column flies no path).
     */
    @Test
    void theGlassfinSchoolsRibbonAndRam() {
        double[] share = {0.15, 0.25, 0.40};
        for (Difficulty difficulty : Difficulty.values()) {
            EnemySpec fry = wave(difficulty, 28.5).enemy();
            EnemySpec.FlockSpec flock = fry.flock().orElseThrow();
            assertEquals(new EnemySpec.Sine(14, 1 / 1.4), flock.sway().orElseThrow());
            assertEquals(
                    new EnemySpec.Ram(3, share[difficulty.ordinal()], 0.3, 0.5, 230),
                    flock.ram().orElseThrow());
            assertTrue(fry.seekersIgnore());
            assertEquals(1, fry.hp());
            WaveSpec column = wave(difficulty, 35.5);
            assertEquals(WaveSpec.Formation.COLUMN, column.formation());
            assertEquals(WaveSpec.Edge.LEFT, column.edge());
        }
    }

    /**
     * The returning units under water (design/enemies/naval, gap check D2): a Driftjelly field after the waterline
     * drifts on the play plane ({@code air}) without its surface/submerge swap; a Reef Spitter nest sits on the ledges
     * and the racks without a raft's drift. The schools of 16 are 12 on easy and 22 on hard.
     */
    @Test
    void theReturningUnitsChangeUnderWater() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            EnemySpec jelly = wave(difficulty, 32).enemy();
            assertEquals(Layer.AIR, jelly.layer());
            assertTrue(jelly.submerge().isEmpty());
            for (GroundUnit unit : level.groundUnits()) {
                if (unit.enemy().slug().equals("reef-spitter")) {
                    assertEquals(0, unit.enemy().drift(), "t=" + unit.t());
                }
            }
            int[] school = {12, 16, 22};
            assertEquals(school[difficulty.ordinal()], wave(difficulty, 69).count(), difficulty.name());
        }
    }

    /**
     * The pickups: the shield cell only from a head-first kill of the first Eel Swarm (t=42), the overdrive from the
     * t=54.5 Lamprey stream's leader.
     */
    @Test
    void theFirstEelsHeadDropsTheShieldCell() {
        WaveSpec eel = wave(Difficulty.MEDIUM, 42);
        assertEquals(List.of(new WaveSpec.Carried(PickupType.SHIELD_CELL, 0, true)), eel.carried());
        assertEquals(
                List.of(new WaveSpec.Carried(PickupType.OVERDRIVE, 0, false)),
                wave(Difficulty.MEDIUM, 54.5).carried());
    }

    /** PUMPS: at least 4 of the 5 siphon pumps broken at medium, 3 on easy, all 5 on hard; 100 credits. */
    @Test
    void thePumpsSecondaryAllowsEscapesByDifficulty() {
        int[] allowed = {2, 1, 0};
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            Secondary secondary = level.objectives().secondary();
            assertEquals("siphon-pump", secondary.escapes());
            assertEquals(allowed[difficulty.ordinal()], secondary.escapesAllowed(), difficulty.name());
            assertEquals("PUMPS", secondary.label());
            assertEquals(100, secondary.credits());
            List<GroundUnit> pumps = level.groundUnits().stream()
                    .filter(unit -> unit.enemy().slug().equals("siphon-pump"))
                    .toList();
            assertEquals(
                    List.of(78.0, 90.0, 102.0, 114.0, 126.0),
                    pumps.stream().map(GroundUnit::t).toList());
            assertEquals(Layer.GROUND, pumps.getFirst().enemy().layer());
            assertTrue(pumps.getFirst().enemy().gun().isEmpty());
        }
    }

    /**
     * The radio: Varga's waterline line plays at the waterline (the {@code waterline} event as a timed line, gap check
     * D5), Rook's line only with him on the wing, Thera Deep Control's at the secondary met.
     */
    @Test
    void theWaterlineLinePlaysAtTheCrossing() {
        List<RadioCue> radio = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).radio();
        RadioCue waterline = radio.stream()
                .filter(cue -> cue.line().startsWith("Under the ice."))
                .findFirst()
                .orElseThrow();
        assertEquals(CueTrigger.TIME, waterline.trigger());
        assertEquals(WATERLINE, waterline.t());
        assertEquals(
                1,
                radio.stream()
                        .filter(cue -> cue.speaker().equals("Rook") && cue.requires() != 0)
                        .count());
        assertTrue(radio.stream()
                .anyMatch(cue -> cue.trigger() == CueTrigger.SECONDARY_OBJECTIVE
                        && cue.speaker().equals("Thera Deep Control")));
    }

    /**
     * The autopilot completes the level with the plan's fit on every difficulty and seed and meets PUMPS on most seeds
     * ({@link WaterPlay}: the pumps low on the screen first). Printed: the secondary's rate, the credits, the retries.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 23 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS), difficulty + " seed " + seed);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d",
                    difficulty, met, SEEDS, credits / (double) SEEDS, retries));
            assertTrue(met * 2 > SEEDS, difficulty + ": the PUMPS secondary on most seeds: " + met);
        }
        System.out.println(report);
    }

    /** The autopilot completes the level without Rook too ({@code --escort none}), one seed per difficulty. */
    @Test
    void theAutopilotFliesTheLevelWithoutRook() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelMetrics.Run run =
                    LevelMetrics.autopilot(content, LEVEL, difficulty, withoutRook(content, difficulty), 1, MAX_STEPS);
            assertTrue(run.complete(), difficulty.name());
        }
    }

    /**
     * Never more than two Eel Swarms on the screen (the stat block's formations note), on hard with the plan's fit; and
     * the first eel's head-first kill drops its shield cell when the autopilot shoots the head first.
     */
    @Test
    void neverMoreThanTwoEelsOnTheScreen() {
        Sortie sortie = sortie(content, 1, Difficulty.HARD, true);
        int most = 0;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
            int eels = 0;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().slug().equals("eel-swarm")
                        && PlayField.overlaps(enemy.renderX(1), enemy.renderY(1), enemy.hitbox())) {
                    eels++;
                }
            }
            most = Math.max(most, eels);
        }
        assertTrue(sortie.complete());
        assertTrue(most <= 2, "eel heads on the screen at once: " + most);
    }

    /**
     * The dry shaft (round 50 Q.1 a, kept as approved): until the waterline the Harpoon, the Depth Charges and the Sonar
     * Pulse read NO WATER, so only the wing pods and Rook fire. Measured at medium on seed 1 with the plan's fit, with and
     * without Rook: the units the section sends and those killed in it, the mean on the screen and the enemy shots a
     * minute, the shield and armour lost there, the credits earned by the waterline; printed for round 52. Asserted:
     * the attempt that completes the level loses no armour plate's worth in the shaft (at most a quarter of its armour)
     * and kills most of the section's units.
     */
    @Test
    void theDryShaftIsMeasured() {
        StringBuilder report = new StringBuilder("Level 23's dry shaft (t < 25.5 s, medium, seed 1):");
        for (boolean rook : new boolean[] {true, false}) {
            Loadout loadout = rook ? planLoadout(content, Difficulty.MEDIUM) : withoutRook(content, Difficulty.MEDIUM);
            Sortie sortie = LevelMetrics.sortie(content, LEVEL, Difficulty.MEDIUM, loadout, 1);
            DryShaft shaft = new DryShaft();
            assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, shaft));
            LevelScript script = sortie.script();
            int sent = script.waves().stream()
                            .filter(wave -> wave.t() < WATERLINE)
                            .mapToInt(LevelMetrics::planned)
                            .sum()
                    + (int) script.groundUnits().stream()
                            .filter(unit -> unit.t() < WATERLINE)
                            .count();
            report.append(String.format(
                    "%n  %s: %d of the section's %d units killed, %.2f on the screen, %.1f shots a minute, %.1f shield and %.1f armour lost,"
                            + " %d credits by the waterline, retries %d",
                    rook ? "with Rook" : "without Rook",
                    shaft.killed,
                    sent,
                    shaft.onScreen / (double) Math.max(1, shaft.steps),
                    shaft.shots * 60 / (shaft.steps * vanguard.sim.SimStep.SECONDS),
                    shaft.shieldLost,
                    shaft.armourLost,
                    shaft.credits,
                    sortie.attempt() - 1));
            assertTrue(
                    shaft.armourLost <= loadout.plating().maxArmour() / 4,
                    "armour lost in the dry shaft: " + shaft.armourLost);
        }
        System.out.println(report);
    }

    /** {@link #theDryShaftIsMeasured}'s watch over the attempt that completes the level, until the waterline. */
    private static final class DryShaft implements LevelMetrics.Probe {
        int killed;
        long onScreen;
        int steps;
        int shots;
        double shieldLost;
        double armourLost;
        int credits;
        private double shield = Double.NaN;
        private double armour = Double.NaN;

        @Override
        public void newAttempt() {
            killed = 0;
            onScreen = 0;
            steps = 0;
            shots = 0;
            shieldLost = 0;
            armourLost = 0;
            credits = 0;
            shield = Double.NaN;
            armour = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            if (sortie.launching() || sortie.levelSeconds() >= WATERLINE) {
                return;
            }
            steps++;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (PlayField.overlaps(enemy.renderX(1), enemy.renderY(1), enemy.hitbox())) {
                    onScreen++;
                }
            }
            SimEvents events = sortie.events();
            killed += events.count(SimEvents.Type.ENEMY_DESTROYED);
            shots += events.count(SimEvents.Type.ENEMY_FIRED);
            double nowShield = sortie.ship().defences().shield();
            double nowArmour = sortie.ship().defences().armour();
            if (!Double.isNaN(shield)) {
                shieldLost += Math.max(0, shield - nowShield);
                armourLost += Math.max(0, armour - nowArmour);
            }
            shield = nowShield;
            armour = nowArmour;
            credits = sortie.credits();
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level23Test::flyToEnd);

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

    private WaveSpec wave(Difficulty difficulty, double t) {
        return SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                .filter(wave -> wave.t() == t)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no wave at t=" + t));
    }

    private static void flyToEnd(Sortie sortie) {
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            Level04Test.step(sortie);
        }
    }
}
