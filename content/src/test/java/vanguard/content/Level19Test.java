package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.EnemySpec;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PartSpec;
import vanguard.sim.Rules;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.StrideArena;
import vanguard.sim.WaveSpec;
import vanguard.sim.WingmanSpec;

/**
 * M7 part F, step F4: Level 19 Foundry of Tharsis from its data (design/campaign/act-3-red-dust/
 * level-19-foundry-of-tharsis; design/enemies/bosses/revenant-walker, design/enemies/ground/warden-tank): the credits
 * of a perfect run and the walker's share, and whole runs of the autopilot with the balance plan's fit (its walker
 * play, its vent dodging). The keys and their mapping are {@link FoundryKeysTest}'s, the walker's fight {@link
 * RevenantWalkerTest}'s; the pacing, the floor, the beam era's HP and the haul are the campaign-wide tests' ({@link
 * PacingTest}, {@link ActFloorTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level19Test {
    static final String LEVEL = "act-3-red-dust/level-19-foundry-of-tharsis";
    private static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 19 ({@code "19"}: the Ion Beam upgraded to L3, Rook's owned Mortar fitted for
     * the level's {@code anti-ground}): Level 18's with the beam at L3: two Micro-missile Pods L2, the Tail Gun L2, the
     * second shield, Composite II, the Airstrike and Rook's Mortar.
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

    /** The plan's fit without Rook ({@code --escort none}; M7 part I, step I1). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 3),
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

    /**
     * The README's credit table (step F4: the floor's waves, {@code bounty_scale} 0.28): a perfect run at medium in
     * Act 1 terms, and the walker's bounty at about 15 % of budget(19) once paid (BalanceTest's mid-boss share).
     */
    @Test
    void aPerfectRunAtMediumEarnsTheReadmesTotal() {
        LevelScript level = SimSpecs.level(content, LEVEL, Difficulty.MEDIUM);
        int kills = 0;
        for (WaveSpec wave : level.waves()) {
            kills += wave.count() * wave.enemy().bounty();
            kills += wave.count()
                    * wave.enemy().parts().stream()
                            .mapToInt(EnemySpec.UnitPart::bounty)
                            .sum();
        }
        int ground = level.groundUnits().stream()
                .mapToInt(unit -> unit.enemy().bounty())
                .sum();
        int boss = level.setPieces().getFirst().parts().stream()
                .mapToInt(PartSpec::bounty)
                .sum();
        double budget = content.economy().budget().of(level.number());
        assertEquals(2_366, Math.round(budget), "the typical haul's target, 700 × 1.07¹⁸");
        assertEquals(
                61 * 4 + 73 * 12 + 59 * 15 + 23 * 5 + 14 * 28 + 6 * 30 + 6 * 22 + 10 * 14 + 30 + 2 * 52 + 4 * 18,
                kills,
                "Dust Kite, Needler, Stinger, Skitter, Warden Tank (by parts), Wraith, Creeper, Ghost Drone, Choir"
                        + " Herald, Shellback (by parts), Ravager");
        assertEquals(26 * 12 + 11 * 15, ground, "the Spine Turret nests and the Polyp Mortars");
        assertEquals(395 + 2 * 50, boss, "the walker's hull and pods (Act 1 terms)");
        assertEquals(0.28, content.level(LEVEL).bounties(), "step F4: from the measured waves (step F2's paper 0.35)");
        double paid = boss
                * content.economy().actFactor()
                * content.economy().actFactor()
                * content.level(LEVEL).bounties();
        System.out.printf(
                "Level 19 (Act 1 terms): kills %d, ground %d, walker %d (paid %.0f, %.1f %% of the budget)%n",
                kills, ground, boss, paid, 100 * paid / budget);
        assertEquals(0.15, paid / budget, 0.005, "the mid-boss's share, about 15 %");
    }

    /**
     * The autopilot completes the level with the plan's fit on every difficulty and seed (the walker play keeps it
     * out of the cone, the vent filter off the hot grates); the walker's fight at medium lasts 45–75 s in the level as
     * in the fixture. Printed: the secondary's rate, the credits, the retries, the vent and cone hits, the fight.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 19 autopilot over " + SEEDS + " seeds:");
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int retries = 0;
            long credits = 0;
            int ventHits = 0;
            int coneBurns = 0;
            double fights = 0;
            for (long seed = 1; seed <= SEEDS; seed++) {
                Sortie sortie = sortie(content, seed, difficulty, false);
                int[] hits = new int[2]; // vent hits, cone burns
                boolean complete = LevelMetrics.fly(sortie, MAX_STEPS, (flown, step) -> {
                    SimEvents events = flown.events();
                    for (int i = 0; i < events.size(); i++) {
                        if (events.type(i) == SimEvents.Type.VENT_HIT && events.value(i) == 0) {
                            hits[0]++;
                        }
                    }
                    hits[1] += events.count(SimEvents.Type.CONE_BURN);
                });
                ventHits += hits[0];
                coneBurns += hits[1];
                assertTrue(complete, difficulty + " seed " + seed);
                SetPiece walker = sortie.setPiece(0);
                assertTrue(walker.destroyed()
                        && walker.stride().map(StrideArena::passes).orElse(0) > 1);
                LevelMetrics.Run run = LevelMetrics.Run.of(sortie);
                LevelMetrics.BossFight fight = run.bosses().getFirst();
                if (difficulty == Difficulty.MEDIUM) {
                    assertTrue(fight.inWindow(), "the mid-boss window: " + fight.seconds());
                }
                fights += fight.seconds();
                met += run.secondaryMet() ? 1 : 0;
                retries += run.retries();
                credits += run.credits();
            }
            report.append(String.format(
                    "%n  %s: secondary met %d/%d, credits %.0f, retries %d, vent hits %.1f a run, cone burns %.1f a"
                            + " run, the walker's fight %.1f s",
                    difficulty,
                    met,
                    SEEDS,
                    credits / (double) SEEDS,
                    retries,
                    ventHits / (double) SEEDS,
                    coneBurns / (double) SEEDS,
                    fights / SEEDS));
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
                    "Level 19 without Rook, %s: %s after %d retries",
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
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level19Test::flyToEnd);

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
