package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Enemy;
import vanguard.sim.GroundUnit;
import vanguard.sim.HazardSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.MediumSpec;
import vanguard.sim.PickupType;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M8 part G, step G3: Level 27 Hydrothermal Vents from its data (design/campaign/act-4-deep-water/
 * level-27-hydrothermal-vents; design/enemies/naval/depth-hunter; design/enemies/ground/relay-beacon): its count, its
 * five sections under water from the start, the heat plumes (discs, burn and bend, timings by difficulty, neighbours
 * apart, the first row alone), the three Depth Hunter columns from the rear (one on the screen at a time, never with the
 * Sirens), the relay beacons and their secondary (3 of 4), the carriers, the Rook-free radio, and whole runs of the
 * autopilot with the balance plan's fit ({@link VentPlay}: every move planned among the discs, the torpedoes and the
 * bullets, the beacons first) with the loaner Bulwark and without an escort. The pacing, the floor, the density, the beam
 * era's HP and the haul are the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link DensityTest},
 * {@link BeamEraHpTest}, {@link TypicalHaulTest}); the plumes, the Hunters and the Bulwark's hazards themselves the
 * simulation's ({@code HeatPlumesTest}, {@code DepthHunterTest}, {@code BulwarkHazardsTest}).
 */
class Level27Test {
    static final String LEVEL = "act-4-deep-water/level-27-hydrothermal-vents";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The plan's load at the L27 visit (BalanceTest's sheet): 15.5 of the Mk IV's 20 MW. */
    static final double SPARE_POWER = 4.5;
    /** The Hunter columns' times. */
    static final double[] COLUMNS = {56, 96, 126};

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 27 ({@code "27"}: the Mk IV generator, and, the user's Q.1 = a of round 57, the
     * owned Tail Gun L3 on the rear again in place of the Depth Charges): the harpoon L4 in front, both Micro-missile
     * Pods L3, the Tail Gun L3, the Sonar Pulse, the Pressure hull, the Mk II shield, Composite III, the Mk IV's spare
     * 4.5 MW, and the loaner Bulwark in the escort slot (Rook is missing).
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutBulwark(content, difficulty)
                .withBulwark(SimSpecs.bulwark(
                        content,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().bulwark().armour()));
    }

    /** The plan's fit without an escort ({@code --escort none}). */
    static Loadout withoutBulwark(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "harpoon-torpedoes", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "tail-gun", 3)),
                        content.shields().models().get(1).name(),
                        content.armour().plating().get(3).name(),
                        SPARE_POWER,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "Sonar Pulse", 1))
                .withPressureHull();
    }

    static Sortie sortie(Content content, long seed, Difficulty difficulty, boolean invulnerable) {
        return sortie(content, planLoadout(content, difficulty), seed, difficulty, invulnerable);
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
     * The README: 219 enemies in waves at medium (Glassfin 132, Barbfin 67 with the Ray's four, Depth Hunter 10, Glow
     * Angler 3, Eel Swarm 2, Siren 2, Spiral Nautilus 2, Abyss Ray 1); on the ground 3 + 3 Spine Turrets (hard 3 + 5), 2
     * Polyp Mortars and the 4 relay beacons, and the *Marlin* trigger with its secret. Easy 175, hard 259 by the
     * formation lever, easy's skipped pincers and the Hunter columns' own counts.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        assertEquals(219, level.waves().stream().mapToInt(LevelMetrics::planned).sum());
        String[] slugs = {
            "glassfin", "barbfin", "depth-hunter", "glow-angler", "eel-swarm", "siren", "spiral-nautilus", "abyss-ray"
        };
        int[] counts = {132, 67, 10, 3, 2, 2, 2, 1};
        for (int i = 0; i < slugs.length; i++) {
            String slug = slugs[i];
            assertEquals(
                    counts[i],
                    level.waves().stream()
                            .filter(wave -> wave.enemy().slug().equals(slug))
                            .mapToInt(WaveSpec::count)
                            .sum(),
                    slug);
        }
        assertEquals(1, level.secrets());
        int[] waves = {175, 219, 259};
        int[] turrets = {6, 6, 8};
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript flown = SimSpecs.level(content, LEVEL, difficulty);
            assertEquals(
                    waves[difficulty.ordinal()],
                    flown.waves().stream().mapToInt(LevelMetrics::planned).sum(),
                    difficulty.name());
            List<GroundUnit> ground = flown.groundUnits();
            assertEquals(turrets[difficulty.ordinal()], count(ground, "spine-turret"), difficulty.name());
            assertEquals(2, count(ground, "polyp-mortar"), difficulty.name());
            assertEquals(4, count(ground, "relay-beacon"), difficulty.name());
            assertEquals(turrets[difficulty.ordinal()] + 6, ground.size(), difficulty.name());
        }
    }

    private static long count(List<GroundUnit> ground, String slug) {
        return ground.stream().filter(unit -> unit.enemy().slug().equals(slug)).count();
    }

    /**
     * Five sections, the whole level under water from its start: the edge at 120 px/s to 24 s, the smokers and the
     * chimney garden at 110 to 56 and 90 s, the beacon ridge at 120 to 124 s, the gate at 130 to 150 s, each change of
     * speed eased over 2 s; under water the Airstrike and the Orbital Lance cannot reach, the Sonar Pulse and the Decoy
     * Flares (unlocked here) can.
     */
    @Test
    void theWholeLevelIsUnderWater() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Section> sections = level.sections();
        assertEquals(
                List.of(24.0, 56.0, 90.0, 124.0, 150.0),
                sections.stream().map(Section::end).toList());
        assertEquals(
                List.of(120.0, 110.0, 110.0, 120.0, 130.0),
                sections.stream().map(Section::speed).toList());
        for (int i : new int[] {1, 3, 4}) {
            assertEquals(2, sections.get(i).rampSeconds(), "section " + (i + 1));
        }
        MediumSpec medium = level.medium();
        assertEquals(List.of(new MediumSpec.Switch(0, true)), medium.switches());
        LevelData data = content.level(LEVEL);
        assertEquals(List.of(), data.waterlines());
        for (int i = 0; i < data.sections().size(); i++) {
            List<String> forbidden = data.forbiddenSpecials(i, content.specials());
            assertTrue(forbidden.containsAll(List.of("Airstrike", "Orbital Lance")), "section " + (i + 1));
            assertFalse(forbidden.contains("Sonar Pulse"), "section " + (i + 1));
            assertFalse(forbidden.contains("Decoy Flares"), "section " + (i + 1));
        }
    }

    /**
     * The heat plumes (the README's Hazards): 16 rows of 2–3 discs 64 px wide from t=26 to t=133, heavy 10 on the ship
     * once a burst, 40 on every swimmer over a burning disc, a crossing shot bent 25°; tell 1.4 s, burn 1.6 s, every 4.5
     * s, 0.5 s from one disc to the next (easy tell 1.8 s, every 6 s; hard tell 1.0 s, every 4 s). Neighbouring discs
     * leave at least 100 px between them, and the first row erupts alone, 7 s before the next.
     */
    @Test
    void thePlumesBurnAndBendByDifficulty() {
        double[] tell = {1.8, 1.4, 1.0};
        double[] period = {6, 4.5, 4};
        for (Difficulty difficulty : Difficulty.values()) {
            List<HazardSpec.VentRow> rows =
                    SimSpecs.level(content, LEVEL, difficulty).hazards().vents();
            assertEquals(
                    List.of(
                            26.0, 33.0, 40.0, 47.0, 52.0, 58.0, 65.0, 72.0, 79.0, 85.0, 94.0, 101.0, 108.0, 115.0,
                            126.0, 133.0),
                    rows.stream().map(HazardSpec.VentRow::t).toList());
            int d = difficulty.ordinal();
            for (HazardSpec.VentRow row : rows) {
                String at = difficulty + " t=" + row.t();
                assertTrue(row.disc(), at);
                assertEquals(64, row.width(), at);
                assertEquals(10, row.damage(), at);
                assertEquals(40, row.enemyDamage(), at);
                assertEquals(Math.toRadians(25), row.bendRadians(), 1e-9, at);
                assertEquals(tell[d], row.tellSeconds(), 1e-9, at);
                assertEquals(1.6, row.burnSeconds(), 1e-9, at);
                assertEquals(period[d], row.periodSeconds(), 1e-9, at);
                assertEquals(0.5, row.rippleSeconds(), 1e-9, at);
                assertTrue(row.xs().length == 2 || row.xs().length == 3, at);
                double[] xs = row.xs().clone();
                Arrays.sort(xs);
                for (int i = 1; i < xs.length; i++) {
                    assertTrue(xs[i] - xs[i - 1] - row.width() >= 100, at + ": " + Arrays.toString(row.xs()));
                }
            }
        }
    }

    /**
     * The Depth Hunters: three columns from the rear (t=56, 96 and 126) of 3 / 4 / 3 at medium (easy 3 / 3 / 3, hard
     * 3 / 4 / 4) under a BOTTOM warning; flown without a shot, one column on the screen at a time (three stretches with a
     * Hunter on the screen, at most four at once), never with the Sirens, and their torpedoes in the water.
     */
    @Test
    void oneHunterColumnAtATimeAndNeverWithTheSirens() {
        int[][] counts = {{3, 3, 3}, {3, 4, 3}, {3, 4, 4}};
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> columns = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("depth-hunter"))
                    .toList();
            assertEquals(
                    Arrays.stream(COLUMNS).boxed().toList(),
                    columns.stream().map(WaveSpec::t).toList(),
                    difficulty.name());
            for (int c = 0; c < columns.size(); c++) {
                WaveSpec column = columns.get(c);
                assertEquals(WaveSpec.Formation.COLUMN, column.formation());
                assertEquals(WaveSpec.Entry.REAR, column.entry());
                assertEquals(counts[difficulty.ordinal()][c], column.count(), difficulty + " t=" + column.t());
            }

            Sortie sortie = sortie(content, 1, difficulty, true);
            int stretches = 0;
            int most = 0;
            boolean before = false;
            boolean torpedoes = false;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                sortie.step(0);
                int hunters = 0;
                boolean siren = false;
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy enemy = sortie.enemy(i);
                    if (!LevelMetrics.onScreen(
                            enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox())) {
                        continue;
                    }
                    hunters += enemy.spec().slug().equals("depth-hunter") ? 1 : 0;
                    siren |= enemy.spec().slug().equals("siren");
                }
                assertFalse(hunters > 0 && siren, difficulty + ": a Siren with a Hunter at " + sortie.scriptSeconds());
                if (hunters > 0 && !before) {
                    stretches++;
                }
                before = hunters > 0;
                most = Math.max(most, hunters);
                torpedoes |= sortie.missileCount() > 0;
            }
            assertTrue(sortie.complete(), difficulty.name());
            assertEquals(3, stretches, difficulty + ": the columns' stretches");
            assertTrue(most <= 4, difficulty + ": " + most + " Hunters at once");
            assertTrue(torpedoes, difficulty + ": torpedoes");
        }
    }

    /**
     * The relay beacons: four `ground` units on the ridge (t=94 at x 80, 101 at 400, 108 at 240, 116 at 90); the
     * secondary BEACONS: at most one of them gets away (3 of 4), 100 credits, on every difficulty.
     */
    @Test
    void theFourBeaconsAndTheirSecondary() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<double[]> beacons = new ArrayList<>();
            for (GroundUnit unit : level.groundUnits()) {
                if (unit.enemy().slug().equals("relay-beacon")) {
                    beacons.add(new double[] {unit.t(), unit.x()});
                }
            }
            assertEquals(4, beacons.size(), difficulty.name());
            double[][] expected = {{94, 80}, {101, 400}, {108, 240}, {116, 90}};
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i][0], beacons.get(i)[0], 1e-9, difficulty + " beacon " + i);
                assertEquals(expected[i][1], beacons.get(i)[1], 1e-9, difficulty + " beacon " + i);
            }
            Secondary secondary = level.objectives().secondary();
            assertEquals("relay-beacon", secondary.escapes());
            assertEquals(1, secondary.escapesAllowed(), difficulty.name());
            assertEquals("BEACONS", secondary.label());
            assertEquals(100, secondary.credits());
        }
    }

    /**
     * The carriers: the overdrive is the Abyss Ray's (t=106), the shield cell the second Hunter column's last unit
     * (t=96), on every difficulty.
     */
    @Test
    void theRayAndTheSecondColumnCarryThePickups() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (WaveSpec wave : SimSpecs.level(content, LEVEL, difficulty).waves()) {
                List<WaveSpec.Carried> carried = wave.carried();
                if (wave.t() == 106 && wave.enemy().slug().equals("abyss-ray")) {
                    assertEquals(1, carried.size(), difficulty.name());
                    assertEquals(PickupType.OVERDRIVE, carried.getFirst().pickup());
                } else if (wave.t() == 96) {
                    assertEquals(1, carried.size(), difficulty.name());
                    assertEquals(PickupType.SHIELD_CELL, carried.getFirst().pickup());
                    assertEquals(WaveSpec.Carried.LAST, carried.getFirst().unit());
                } else {
                    assertEquals(List.of(), carried, difficulty + " t=" + wave.t());
                }
            }
        }
    }

    /**
     * The radio has no Rook line (he is missing since Level 26; the Bulwark has none): Okafor, Varga and Thera Deep
     * Control only; Varga's warning before the first column (t=52), the hull code at the wreck (t=70) and the beacons'
     * order (t=94.5).
     */
    @Test
    void theRadioIsRookFree() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (RadioCue cue : SimSpecs.level(content, LEVEL, difficulty).radio()) {
                assertTrue(
                        List.of("Okafor", "Varga", "Thera Deep Control").contains(cue.speaker()),
                        difficulty + ": " + cue.speaker() + ": " + cue.line());
            }
        }
        List<RadioCue> radio = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM).radio();
        assertEquals(52, line(radio, "Contacts rising from below.").t());
        assertEquals(70, line(radio, "That wreck on the floor").t());
        assertEquals(94.5, line(radio, "The ridge beacons").t());
    }

    private static RadioCue line(List<RadioCue> radio, String start) {
        return radio.stream()
                .filter(cue -> cue.line().startsWith(start))
                .findFirst()
                .orElseThrow();
    }

    /**
     * The autopilot completes the level with the plan's fit and the loaner Bulwark on every difficulty and seed ({@link
     * VentPlay}) and silences 3 of the 4 beacons on most seeds (step G3 measured every seed of five on every
     * difficulty). Printed: the secondary's rate, the credits, the retries.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 27 autopilot over " + SEEDS + " seeds:");
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
            assertTrue(met * 2 > SEEDS, difficulty + ": the beacons on most seeds: " + met);
        }
        System.out.println(report);
    }

    /** The autopilot completes the level without an escort too ({@code --escort none}), one seed per difficulty. */
    @Test
    void theAutopilotFliesTheLevelWithoutTheBulwark() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelMetrics.Run run = LevelMetrics.autopilot(
                    content, LEVEL, difficulty, withoutBulwark(content, difficulty), 1, MAX_STEPS);
            assertTrue(run.complete(), difficulty.name());
            System.out.printf(
                    "Level 27 without the Bulwark, %s: secondary met %s, credits %d, retries %d%n",
                    difficulty, run.secondaryMet(), run.credits(), run.retries());
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level27Test::flyToEnd);

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
}
