package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M7 part G, step G4: Level 20 Retreat from Hellas from its data (design/campaign/act-3-red-dust/
 * level-20-retreat-from-hellas; design/enemies/air/mote-swarm): the pursuits mapped per difficulty, the waves ahead by
 * travel, the kill-ratio secondary within reach, and whole runs of the autopilot with the balance plan's fit (its
 * retreat play, {@link RetreatPlay}). The keys are {@link RetreatKeysTest}'s; the pacing, the floor, the beam era's HP
 * and the haul the campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link BeamEraHpTest}, {@link
 * TypicalHaulTest}).
 */
class Level20Test {
    static final String LEVEL = "act-3-red-dust/level-20-retreat-from-hellas";
    private static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;

    /** The scroll's zero at the turnabout (+140 → −120 over t = 20–24), s. */
    private static final double TURN = 22.2;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 20 ({@code "20"}: the Tail Gun upgraded to L3, Rook's owned Autocannon, the
     * user's Q.2 of 2026-10-20): Level 19's with the rear at L3: the Ion Beam L3, two Micro-missile Pods L2, the second
     * shield, Composite II, the Airstrike and Rook's Autocannon.
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
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "tail-gun", 3)),
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

    /**
     * The two pursuits per difficulty: the entry and the refills by the formation lever (8 + 2, 10 + 3, 12 + 4), eight
     * refills before the first break-off at t=108 and three before the second at t=163, the Mote Swarm's lunge cadence
     * and tell by its hooks (easy 8 s and 1.2 s, medium 6 s and 0.8 s, hard 4.5 s), the band and the lag its
     * defaults.
     */
    @Test
    void thePursuitsMapPerDifficulty() {
        int[] entry = {8, 10, 12};
        int[] refill = {2, 3, 4};
        double[] every = {8, 6, 4.5};
        double[] tell = {1.2, 0.8, 0.8};
        for (Difficulty difficulty : Difficulty.values()) {
            List<WaveSpec> pursuits = SimSpecs.level(content, LEVEL, difficulty).waves().stream()
                    .filter(w -> w.formation() == WaveSpec.Formation.PURSUIT)
                    .toList();
            assertEquals(2, pursuits.size(), difficulty.name());
            int d = difficulty.ordinal();
            for (WaveSpec wave : pursuits) {
                WaveSpec.Pursuit pursuit = wave.pursuit().orElseThrow();
                assertEquals(WaveSpec.Entry.REAR, wave.entry());
                assertEquals(entry[d], wave.count(), difficulty + " entry");
                assertEquals(refill[d], pursuit.refill(), difficulty + " refill");
                assertEquals(10, pursuit.refillEvery());
                assertEquals(every[d], pursuit.lungeEvery(), difficulty + " lunge cadence");
                assertEquals(tell[d], pursuit.tellSeconds(), difficulty + " tell");
                assertEquals(50, pursuit.bandTop());
                assertEquals(150, pursuit.bandBottom());
                assertEquals(90, pursuit.lagSpeed());
            }
            assertEquals(
                    List.of(108.0, 163.0),
                    pursuits.stream()
                            .map(w -> w.pursuit().orElseThrow().breakAt())
                            .toList());
            assertEquals(
                    List.of(8, 3),
                    pursuits.stream()
                            .map(w -> w.pursuit().orElseThrow().refills(w.t()))
                            .toList());
        }
    }

    /** The README: at least 60 % of the waves after the turn come from ahead by travel (the bottom edge). */
    @Test
    void mostWavesAfterTheTurnComeFromAhead() {
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript level = SimSpecs.level(content, LEVEL, difficulty);
            List<WaveSpec> after =
                    level.waves().stream().filter(w -> w.t() > TURN).toList();
            long ahead = after.stream()
                    .filter(w -> w.entry() == WaveSpec.Entry.FRONT)
                    .count();
            assertTrue(ahead >= 0.6 * after.size(), difficulty + ": " + ahead + " of " + after.size());
            after.forEach(w -> assertEquals(-1, Sortie.travelAt(level, w.t()), "reversed at t=" + w.t()));
        }
    }

    /**
     * The secondary's 65 % kill ratio is within reach: at medium the units other than the motes alone make up more than
     * 65 % of the level's planned enemies, so the motes that break off alive never put it out of reach.
     */
    @Test
    void theKillRatioIsReachable() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        int planned = level.groundUnits().size();
        int motes = 0;
        for (WaveSpec wave : level.waves()) {
            planned += LevelMetrics.planned(wave);
            if (wave.formation() == WaveSpec.Formation.PURSUIT) {
                motes += LevelMetrics.planned(wave);
            }
        }
        assertEquals(53, motes, "10 + 8 × 3 and 10 + 3 × 3");
        assertEquals(0.65, level.objectives().secondary().killRatio(), 1e-9);
        assertTrue(planned - motes > 0.65 * planned, (planned - motes) + " of " + planned);
    }

    /**
     * The autopilot completes the level with the plan's fit on every difficulty and seed (the retreat play holds the
     * middle band, steps out of the lunges and turns the rear guns on what comes up from below). Printed: the
     * secondary's rate, the credits, the retries.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 20 autopilot over " + SEEDS + " seeds:");
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

    @Test
    void steppingAWholeLevelDoesNotAllocate() {
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level20Test::flyToEnd);

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
