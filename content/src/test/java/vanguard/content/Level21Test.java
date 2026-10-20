package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.Loadout;
import vanguard.sim.Rules;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;
import vanguard.sim.WingmanSpec;

/**
 * M7 part H, step H4: Level 21 Dust Colossus from its data (design/campaign/act-3-red-dust/level-21-dust-colossus;
 * design/enemies/bosses/dust-colossus), flown whole by the autopilot with the balance plan's fit (its colossus play,
 * {@link ColossusPlay}): the arena crawls at 20 px/s while the clock is held at its end, the breaching boss is fought
 * to its kill on every difficulty and seed. The keys and the waves' plan are {@link ColossusKeysTest}'s; the fight's
 * length, par and calibration {@link DustColossusTest}'s; the pacing, the floor, the beam era's HP and the haul the
 * campaign-wide tests' ({@link PacingTest}, {@link ActFloorTest}, {@link BeamEraHpTest}, {@link TypicalHaulTest}).
 */
class Level21Test {
    static final String LEVEL = "act-3-red-dust/level-21-dust-colossus";
    private static final int MAX_STEPS = 60 * 60 * 30;
    /** The seeds the whole level is flown on per difficulty. */
    static final int SEEDS = 3;
    /** The arena's start (the boss's arrival) and end (the clock held there while it lives), s. */
    static final double ARENA_START = 105;

    static final double ARENA_END = 245;

    private final Content content = ContentLoader.fromClasspath();

    /**
     * The balance plan's fit at Level 21 ({@code "21"}: Composite III; Rook keeps the Autocannon, the user's H.2 of
     * 2026-10-20): Level 20's with the third plating: the Ion Beam L3, two Micro-missile Pods L2, the Tail Gun L3, the
     * second shield, Composite III, the Airstrike and Rook's Autocannon.
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
                        content.armour().plating().get(3).name(),
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
     * The slowly scrolling arena (H.7a): from the boss's arrival the level clock holds at the arena's end while the
     * ground crawls on at 20 px/s; the kill jumps the clock to the end (section 6, the settling dust).
     */
    @Test
    void theArenaCrawlsWhileTheColossusLives() {
        Sortie sortie = sortie(content, 2185, Difficulty.MEDIUM, true);
        SetPiece boss = null;
        int crawled = 0;
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            sortie.step(Autopilot.commands(sortie));
            boss = ColossusPlay.breachingBoss(sortie);
            if (boss != null && boss.engaged() && sortie.realSeconds() > ARENA_START + 10) {
                assertTrue(sortie.crawling(), "crawling at " + sortie.realSeconds());
                assertEquals(20, sortie.groundSpeed(), 1e-9, "the crawl at " + sortie.realSeconds());
                assertTrue(sortie.levelSeconds() <= ARENA_END + 1e-9, "the clock held at " + sortie.levelSeconds());
                crawled++;
            }
        }
        assertTrue(sortie.complete(), "completed");
        assertTrue(crawled > 0, "the fight crawled");
        assertTrue(sortie.setPiece(0).destroyed(), "the Colossus killed");
    }

    /**
     * The autopilot completes the level with the plan's fit on every difficulty and seed (its colossus play stands off
     * the open gullet, leaves the bite circle and the marker, dodges the rings, fans, spiral and bursts). Printed: the
     * secondary's rate, the credits, the retries.
     */
    @Test
    void theAutopilotFliesTheLevel() {
        StringBuilder report = new StringBuilder("Level 21 autopilot over " + SEEDS + " seeds:");
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
        long allocated = Allocations.least(() -> sortie(content, 2185, Difficulty.HARD, false), Level21Test::flyToEnd);

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
