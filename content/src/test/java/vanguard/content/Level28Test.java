package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Enemy;
import vanguard.sim.GroundUnit;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.MediumSpec;
import vanguard.sim.RadioCue;
import vanguard.sim.Rules;
import vanguard.sim.Section;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M8 part H, step H4: Level 28 Abyssal Maw from its data (design/campaign/act-4-deep-water/level-28-abyssal-maw;
 * design/enemies/bosses/abyssal-maw): its count, its ten sections (the one-second halted arena, the climb, the
 * breakout, the run, the orbit), the first water → air crossing at the breakout (115 s), the three Depth Hunter columns
 * from the rear (one on the screen at a time, never with the Sirens), no wave after t=93.5 (the Choir's pause), the
 * boss checkpoint on easy and medium (none on hard), the armour floor from the kill, the kill ratio without the boss and
 * its shed Eel Swarms, the Rook-free radio, and whole runs of the autopilot with the balance plan's fit ({@link
 * VentPlay} on the approach, {@link MawPlay} in the arena, {@link RunPlay} in the cruisers' run) with the loaner Bulwark
 * and without an escort. The fight's length and calibration are {@link AbyssalMawTest}'s; the pacing, the floor, the
 * density, the beam era's HP and the haul the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link
 * DensityTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}); the coil, the cruisers and the escort's hazards
 * themselves the simulation's ({@code MawArenaTest}, {@code CruiserRunTest}, {@code MawHazardsTest}).
 */
class Level28Test {
    static final String LEVEL = "act-4-deep-water/level-28-abyssal-maw";
    static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The plan's load at the L28 visit (BalanceTest's sheet): 17 of the Mk IV's 20 MW. */
    static final double SPARE_POWER = 3;
    /** The Hunter columns' times. */
    static final double[] COLUMNS = {16, 46, 86};
    /** The last wave's time: none after it (the screen clears while the Choir sings at 97.5 s). */
    static final double LAST_WAVE = 93.5;
    /**
     * The most shield and armour the autopilot loses in the cruisers' run at medium, a mean over {@link #SEEDS} seeds
     * (the gap check's ≈ 15; step H4 measured 0-24 a run, the mean about 5).
     */
    static final double RUN_LOST = 15;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 28 ({@code "28"}: Level 27's with the Shield Mk III): the harpoon L4 in front,
     * both Micro-missile Pods L3, the Tail Gun L3, the Sonar Pulse, the Pressure hull, the Mk III shield, Composite III,
     * the Mk IV's spare 3 MW, and the loaner Bulwark in the escort slot (Rook is missing).
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
                        content.shields().models().get(2).name(),
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
     * The README: 137 enemies in waves at medium (Glassfin 74, Barbfin 40, Depth Hunter 10, Siren 4, Spiral Nautilus 4,
     * Eel Swarm 3, Abyss Ray 2); on the ground 3 + 3 Spine Turrets (hard 3 + 5) and 2 + 2 Polyp Mortars, and the
     * *Pelagic-3* trigger with its secret. Easy 111, hard 161 by the formation lever, easy's skipped Nautilus pair and
     * hard's third Siren.
     */
    @Test
    void theLevelHoldsTheReadmesCount() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        assertEquals(137, level.waves().stream().mapToInt(LevelMetrics::planned).sum());
        String[] slugs = {"glassfin", "barbfin", "depth-hunter", "siren", "spiral-nautilus", "eel-swarm", "abyss-ray"};
        int[] counts = {74, 40, 10, 4, 4, 3, 2};
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
        int[] waves = {111, 137, 161};
        int[] turrets = {6, 6, 8};
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript flown = SimSpecs.level(content, LEVEL, difficulty);
            assertEquals(
                    waves[difficulty.ordinal()],
                    flown.waves().stream().mapToInt(LevelMetrics::planned).sum(),
                    difficulty.name());
            List<GroundUnit> ground = flown.groundUnits();
            assertEquals(turrets[difficulty.ordinal()], count(ground, "spine-turret"), difficulty.name());
            assertEquals(4, count(ground, "polyp-mortar"), difficulty.name());
            assertEquals(turrets[difficulty.ordinal()] + 4, ground.size(), difficulty.name());
        }
    }

    private static long count(List<GroundUnit> ground, String slug) {
        return ground.stream().filter(unit -> unit.enemy().slug().equals(slug)).count();
    }

    /**
     * Ten sections: the gate at 120 px/s to 22 s, the galleries and the cavern at 110 to 50 and 80 s, the antechamber at
     * 100 to 104 s, the vent (the one-second halted arena, the clock held while the Maw lives and 5 s after the kill: its 4 s death chain and the 1 s hold after it), the
     * shaft at 150 to 115 s, the breakout and the rise at 150 to 118 and 122 s, the run at 200 to 157 s and the orbit at
     * 400 to 162 s (eased over 3 s). Under water to the breakout, then in the air: the medium's one switch back, at
     * 115 s; under water the Airstrike and the Orbital Lance cannot reach, in the air again they can and the Sonar Pulse
     * cannot (NO WATER).
     */
    @Test
    void theArenaHaltsAndTheBreakoutLeavesTheWater() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        List<Section> sections = level.sections();
        assertEquals(
                List.of(22.0, 50.0, 80.0, 104.0, 105.0, 115.0, 118.0, 122.0, 157.0, 162.0),
                sections.stream().map(Section::end).toList());
        assertEquals(
                List.of(120.0, 110.0, 110.0, 100.0, 0.0, 150.0, 150.0, 150.0, 200.0, 400.0),
                sections.stream().map(Section::speed).toList());
        Section arena = sections.get(4);
        assertTrue(arena.arena(), "the vent is the arena");
        // Step H6: the hold follows the Maw's 4 s death chain (the clock's rule), so it resumes at K+5 = t=105.
        assertEquals(1, arena.holdAfterBossSeconds());
        assertEquals(
                5,
                arena.holdAfterBossSeconds()
                        + level.setPieces().stream()
                                .flatMap(piece -> piece.boss().stream())
                                .mapToDouble(vanguard.sim.BossSpec::deathSeconds)
                                .findFirst()
                                .orElseThrow(),
                1e-9,
                "the clock resumes 5 s after the kill");
        assertEquals(3, sections.get(9).rampSeconds());
        assertEquals(
                1,
                level.setPieces().stream()
                        .filter(vanguard.sim.SetPieceSpec::isBoss)
                        .count());
        MediumSpec medium = level.medium();
        assertEquals(List.of(new MediumSpec.Switch(0, true), new MediumSpec.Switch(115, false)), medium.switches());
        LevelData data = content.level(LEVEL);
        for (int i = 0; i < data.sections().size(); i++) {
            List<String> forbidden = data.forbiddenSpecials(i, content.specials());
            boolean water = data.sectionStart(i) < 115;
            assertEquals(water, forbidden.contains("Airstrike"), "section " + (i + 1) + ": " + forbidden);
            assertEquals(water, forbidden.contains("Orbital Lance"), "section " + (i + 1) + ": " + forbidden);
            assertEquals(!water, forbidden.contains("Sonar Pulse"), "section " + (i + 1) + ": " + forbidden);
        }
    }

    /**
     * The Depth Hunters: three columns from the rear (t=16, 46 and 86) under a BOTTOM warning; flown without a shot,
     * one column on the screen at a time (three stretches with a Hunter on the screen, at most four at once), never
     * with the Sirens; and no wave after t=93.5 (the Choir's pause before the arena), no ground target after it either.
     */
    @Test
    void oneHunterColumnAtATimeNeverWithTheSirensAndNothingAfterTheLastWave() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<WaveSpec> columns = level.waves().stream()
                    .filter(wave -> wave.enemy().slug().equals("depth-hunter"))
                    .toList();
            assertEquals(
                    java.util.Arrays.stream(COLUMNS).boxed().toList(),
                    columns.stream().map(WaveSpec::t).toList(),
                    difficulty.name());
            for (WaveSpec column : columns) {
                assertEquals(WaveSpec.Formation.COLUMN, column.formation());
                assertEquals(WaveSpec.Entry.REAR, column.entry());
            }
            assertTrue(
                    level.waves().stream().allMatch(wave -> wave.t() <= LAST_WAVE),
                    difficulty + ": a wave after " + LAST_WAVE);
            assertTrue(
                    level.groundUnits().stream().allMatch(unit -> unit.t() <= LAST_WAVE),
                    difficulty + ": a ground target after " + LAST_WAVE);

            Sortie sortie = sortie(content, 1, difficulty, true);
            int stretches = 0;
            int most = 0;
            boolean before = false;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS && sortie.scriptSeconds() < 104) {
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
            }
            assertEquals(3, stretches, difficulty + ": the columns' stretches");
            assertTrue(most <= 4, difficulty + ": " + most + " Hunters at once");
        }
    }

    /**
     * The boss checkpoint at the arena's start, a retry from it on easy and medium, none on hard (design/systems/
     * difficulty); the armour
     * floor at 1 from the Maw's kill to the level's end on every difficulty (J2: the run cannot be lost); the kill
     * ratio's 65 % of the level's enemies without the boss and its shed Eel Swarms (147 at medium: 96).
     */
    @Test
    void theCheckpointTheFloorAndTheKillRatio() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = sortie(content, 2185, difficulty, false);
            SetPiece boss = null;
            boolean checkpoint = false;
            boolean floored = false;
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                Level04Test.step(sortie);
                boss = MawPlay.coiledBoss(sortie) != null ? MawPlay.coiledBoss(sortie) : boss;
                if (boss != null && boss.present() && !boss.destroyed()) {
                    checkpoint |= sortie.bossCheckpoint();
                    assertEquals(0, sortie.ship().defences().floor(), difficulty + ": no floor before the kill");
                }
                if (boss != null && boss.destroyed() && sortie.flying()) {
                    floored = true;
                    assertEquals(1, sortie.ship().defences().floor(), difficulty + ": the floor after the kill");
                    assertTrue(sortie.ship().defences().armour() >= 1, difficulty + ": the run cannot be lost");
                }
            }
            assertTrue(sortie.complete(), difficulty.name());
            assertTrue(floored, difficulty + ": flew on after the kill");
            assertTrue(checkpoint, difficulty + ": the boss checkpoint taken at the arena's start");
            assertEquals(
                    difficulty != Difficulty.HARD,
                    content.difficulty().bossCheckpoint().of(difficulty),
                    difficulty + ": a retry from it (none on hard)");
            if (difficulty == Difficulty.MEDIUM) {
                assertEquals(147, sortie.enemyTotal(), "the boss and its sheds not counted");
                assertEquals(96, sortie.requiredKills());
            }
        }
    }

    /**
     * The radio has no Rook line (he is missing since Level 26; the Bulwark has none): Okafor, Varga and the Choir only.
     */
    @Test
    void theRadioIsRookFree() {
        for (Difficulty difficulty : Difficulty.values()) {
            for (RadioCue cue : SimSpecs.level(content, LEVEL, difficulty).radio()) {
                assertTrue(
                        List.of("Okafor", "Varga", "The Choir").contains(cue.speaker()),
                        difficulty + ": " + cue.speaker() + ": " + cue.line());
            }
        }
    }

    /**
     * The autopilot completes the level with the plan's fit and the loaner Bulwark on every difficulty and seed, the
     * Maw's fight inside the act boss's window at medium ({@link MawPlay}), and at medium loses at most {@link #RUN_LOST}
     * a run in the cruisers' run on the mean ({@link RunPlay}). Printed: the secondary's rate, the credits, the retries,
     * the fight, the run's losses.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 28 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            double fights = 0;
            double runLost = 0;
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                RunWatch watch = new RunWatch();
                assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, watch), difficulty + " seed " + seed);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
                LevelMetrics.BossFight fight = run.bosses().getFirst();
                assertTrue(fight.killed(), difficulty + " seed " + seed);
                if (difficulty == Difficulty.MEDIUM) {
                    assertTrue(fight.inWindow(), "seed " + seed + ": the act boss's window, " + fight.seconds() + " s");
                }
                fights += fight.seconds();
                runLost += watch.lost;
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d, the Maw %.1f s, the run lost %.1f",
                    difficulty, met, SEEDS, credits / (double) SEEDS, retries, fights / SEEDS, runLost / SEEDS));
            if (difficulty == Difficulty.MEDIUM) {
                assertTrue(runLost / SEEDS <= RUN_LOST, "the run's losses: " + runLost / SEEDS);
            }
        }
        System.out.println(report);
    }

    /** The shield and armour lost in the cruisers' run on the attempt that completes the level. */
    static final class RunWatch implements LevelMetrics.Probe {
        double lost;
        private double before = Double.NaN;

        @Override
        public void newAttempt() {
            lost = 0;
            before = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            double now =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            if (RunPlay.running(sortie) && !Double.isNaN(before)) {
                lost += Math.max(0, before - now);
            }
            before = now;
        }
    }

    /** The autopilot completes the level without an escort too ({@code --escort none}), one seed per difficulty. */
    @Test
    void theAutopilotFliesTheLevelWithoutTheBulwark() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelMetrics.Run run = LevelMetrics.autopilot(
                    content, LEVEL, difficulty, withoutBulwark(content, difficulty), 1, MAX_STEPS);
            assertTrue(run.complete(), difficulty.name());
            System.out.printf(
                    "Level 28 without the Bulwark, %s: secondary met %s, credits %d, retries %d, the Maw %.1f s%n",
                    difficulty,
                    run.secondaryMet(),
                    run.credits(),
                    run.retries(),
                    run.bosses().getFirst().seconds());
        }
    }

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level28Test::flyToEnd);

        assertEquals(0, allocated, "the level allocated " + allocated + " bytes");
    }

    @Test
    void theSameSeedAndCommandsGiveTheSameState() {
        Sortie first = sortie(content, 77, Difficulty.MEDIUM, false);
        Sortie second = sortie(content, 77, Difficulty.MEDIUM, false);
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
