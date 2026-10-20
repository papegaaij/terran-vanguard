package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.SplittableRandom;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.sim.BossSpec;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Missiles;
import vanguard.sim.Section;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.StrideArena;

/**
 * M7 part F, step F4: the Revenant Walker's fight from its data (design/enemies/bosses/revenant-walker), flown by the
 * autopilot with the balance plan's Level 19 fit ({@link Level19Test#planLoadout}: the Ion Beam L3, two Micro-missile
 * Pods L2, the Tail Gun L2, Rook's Mortar) in a fixture of Level 19's arena: the yard's halt with the wreck beat's
 * hold and the crawl after it. The mechanics (passes, lanes, edges, plate, cone, vents, missiles, the phase-3 turn) are
 * the simulation's ({@code RevenantWalkerSimTest}); here: the fight's length is the par at medium (the mid-boss window
 * 45–75 s on every seed, the HP tuned to it: "measure, then tune the HP, not the attacks"), the autopilot's walker
 * play keeps out of the cone, easy and hard are won, and phase 1 is fierce but fair (round 43, K.a of round 42: "It
 * needs to do a lot more shooting": a random dodger loses clearly more armour per 10 s than at the Harbour Kraken's
 * phase 1, but not twice as much).
 */
class RevenantWalkerTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String SLUG = "revenant-walker";
    /** The arena's start in the fixture, s of level time. */
    static final double ARENA = 3;
    /** The seeds the autopilot's fight is measured on. */
    static final long[] SEEDS = {2185, 11, 77, 1301, 4242};
    /** The calibration harness's seeds (eight, as the Siege Spire's round 34). */
    static final long[] DODGE_SEEDS = {1, 2, 3, 4, 5, 6, 7, 8};

    static SetPieceSpec walker(Difficulty difficulty) {
        return SimSpecs.boss(CONTENT, new LevelData.BossPlacement(SLUG, ARENA, 240, 2), difficulty);
    }

    /** Level 19's arena in small: a short approach, the yard's halt (its hold after the walker), the crawl. */
    static LevelScript level(Difficulty difficulty) {
        return LevelScript.builder(19, 3)
                .sections(List.of(
                        new Section(ARENA, 120),
                        new Section(ARENA + 1, 0, true, 0, 0, 1),
                        new Section(ARENA + 13, 20, false, 0, 2)))
                .setPieces(List.of(walker(difficulty)))
                .build();
    }

    static Sortie sortie(long seed, Difficulty difficulty, Loadout loadout) {
        return new Sortie(
                seed,
                loadout,
                level(difficulty),
                SimSpecs.rules(CONTENT, Level19Test.LEVEL, difficulty),
                loadout.plating().maxArmour());
    }

    /** What one run of the fight saw. */
    record Fight(
            double seconds,
            boolean killed,
            double damage,
            double armourLost,
            int coneHits,
            int missileHits,
            double phaseOneSeconds,
            double phaseOneDamage,
            boolean flying,
            double[] phaseSeconds,
            int[] phaseShots) {
        /**
         * Round 43: its shots a minute in phase {@code phase} (0–2), as the level report counts them (one per {@code
         * ENEMY_FIRED} event: a fan or a burst's shot counts once) plus one per homing missile.
         */
        double shotsPerMinute(int phase) {
            return phaseSeconds[phase] > 0 ? phaseShots[phase] * 60 / phaseSeconds[phase] : 0;
        }

        /** Round 43: its shots a minute over the whole fight. */
        double shotsPerMinute() {
            double seconds = phaseSeconds[0] + phaseSeconds[1] + phaseSeconds[2];
            return seconds > 0 ? (phaseShots[0] + phaseShots[1] + phaseShots[2]) * 60 / seconds : 0;
        }

        /** The shield and armour lost per 10 s of phase 1 (the calibration harness's measure). */
        double phaseOneLostPerTen() {
            return phaseOneSeconds > 0 ? phaseOneDamage * 10 / phaseOneSeconds : 0;
        }

        @Override
        public String toString() {
            return String.format(
                    "%.1f s%s, damage %.0f (armour %.0f), cone hits %d, missile hits %d, phase 1 %.1f s (lost %.0f,"
                            + " %.1f per 10 s), phases 2 and 3 %.1f + %.1f s, shots a minute %.0f (%.0f / %.0f / %.0f)%s",
                    seconds,
                    killed ? "" : " NOT KILLED",
                    damage,
                    armourLost,
                    coneHits,
                    missileHits,
                    phaseOneSeconds,
                    phaseOneDamage,
                    phaseOneLostPerTen(),
                    phaseSeconds[1],
                    phaseSeconds[2],
                    shotsPerMinute(),
                    shotsPerMinute(0),
                    shotsPerMinute(1),
                    shotsPerMinute(2),
                    flying ? "" : ", shot down");
        }
    }

    /** The autopilot flies the fixture with the plan's fit until the walker dies (at most 3 minutes). */
    static Fight fight(long seed, Difficulty difficulty) {
        return fight(sortie(seed, difficulty, Level19Test.planLoadout(CONTENT, difficulty)), Autopilot::commands, 180);
    }

    /** The fight in {@code sortie} flown by {@code pilot}, at most {@code seconds}. */
    static Fight fight(Sortie sortie, ToIntFunction<Sortie> pilot, double seconds) {
        SetPiece boss = sortie.setPiece(0);
        StrideArena walker = boss.stride().orElseThrow();
        double startArmour = sortie.ship().defences().armour();
        double damage = 0;
        int coneHits = 0;
        int missileHits = 0;
        int phaseOneTicks = 0;
        double phaseOneDamage = 0;
        double[] phaseSeconds = new double[3];
        int[] phaseShots = new int[3];
        for (int i = 0; i < SimStep.ticks(seconds) && !boss.destroyed() && sortie.flying(); i++) {
            double shield = sortie.ship().defences().shield();
            double armour = sortie.ship().defences().armour();
            boolean burning = walker.conePhase() == StrideArena.ConePhase.BURN;
            sortie.step(pilot.applyAsInt(sortie));
            double lost = Math.max(
                    0,
                    shield
                            + armour
                            - sortie.ship().defences().shield()
                            - sortie.ship().defences().armour());
            damage += lost;
            SimEvents events = sortie.events();
            boolean struck = false;
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.MISSILE_DESTROYED && events.value(e) == Missiles.STRUCK) {
                    struck = true;
                }
            }
            missileHits += struck && lost > 0 ? 1 : 0;
            coneHits += burning && lost >= 10 && !struck ? 1 : 0;
            if (boss.engaged() && boss.phase() == 0) {
                phaseOneTicks++;
                phaseOneDamage += lost;
            }
            if (boss.engaged()) {
                int phase = Math.min(2, boss.phase());
                phaseSeconds[phase] += SimStep.SECONDS;
                phaseShots[phase] +=
                        events.count(SimEvents.Type.ENEMY_FIRED) + events.count(SimEvents.Type.MISSILE_LAUNCHED);
            }
        }
        return new Fight(
                boss.destroyed() ? boss.killSeconds() : boss.bossSeconds(),
                boss.destroyed(),
                damage,
                startArmour - sortie.ship().defences().armour(),
                coneHits,
                missileHits,
                phaseOneTicks * SimStep.SECONDS,
                phaseOneDamage,
                sortie.flying(),
                phaseSeconds,
                phaseShots);
    }

    /**
     * The par: the autopilot's measured mean at medium (step F4: 52.7 s with the HP at 3,740 and the pods at the art's
     * mounts, ±53 px; the fight's length steps with the passes, 56.5 s at 3,900 and 68.0 s at 3,980, so the HP sit
     * inside a plateau). Round 43's rework (far more fire, the bigger cone): 57.1 s at 3,170 (hull 2,310, pods 430);
     * 47 s at 2,800, 80 s and more at 3,740.
     */
    static final double PAR = 57;

    @Test
    void theStatBlockMapsToAWalkingMidBossWithItsPar() {
        SetPieceSpec spec = walker(Difficulty.MEDIUM);
        BossSpec boss = spec.boss().orElseThrow();
        assertTrue(boss.midBoss());
        assertTrue(boss.stride().isPresent()
                && boss.plate().isPresent()
                && boss.cone().isPresent());
        assertEquals(PAR, boss.parSeconds(), "the par: the measured mean");
        assertEquals(3, boss.phases().size());
    }

    /**
     * Measure, then tune the HP (Harbour Kraken E6, Siege Spire H10): the autopilot's fight with the plan's fit lasts
     * its par at medium (the bar appearing to the kill), inside the mid-boss window 45–75 s on every seed.
     */
    @Test
    void theAutopilotsFightLastsItsParAtMedium() {
        double sum = 0;
        List<Fight> fights = new java.util.ArrayList<>();
        for (long seed : SEEDS) {
            Fight fight = fight(seed, Difficulty.MEDIUM);
            System.out.printf("Walker medium seed %d: %s%n", seed, fight);
            fights.add(fight);
            sum += fight.seconds();
        }
        for (Fight fight : fights) {
            assertTrue(fight.killed(), "killed: " + fight);
            assertTrue(LevelMetrics.inBossWindow(true, fight.seconds()), "the mid-boss window: " + fight);
        }
        double mean = sum / SEEDS.length;
        System.out.printf("Walker medium: mean %.1f s, par %.0f%n", mean, PAR);
        assertEquals(PAR, mean, 3, "the par is the measured mean");
    }

    /**
     * "Get behind it, but not in the cone" (Passes): the autopilot's walker play keeps out of the flame cone (at most
     * one burn reaches it in a fight at medium) and keeps most of its armour.
     */
    @Test
    void theAutopilotKeepsOutOfTheCone() {
        for (long seed : SEEDS) {
            Fight fight = fight(seed, Difficulty.MEDIUM);
            assertTrue(fight.coneHits() <= 1, "out of the cone: " + fight);
        }
    }

    /**
     * Easy is won on every seed; hard (×1.3 HP, salvos of 4, wider chin fans, longer flank bursts, the longer cone and
     * swivel) on most.
     */
    @ParameterizedTest
    @EnumSource(
            value = Difficulty.class,
            names = {"EASY", "HARD"})
    void theFightOnEasyAndHard(Difficulty difficulty) {
        double sum = 0;
        int killed = 0;
        for (long seed : SEEDS) {
            Fight fight = fight(seed, difficulty);
            System.out.printf("Walker %s seed %d: %s%n", difficulty, seed, fight);
            if (fight.killed()) {
                killed++;
                sum += fight.seconds();
            }
        }
        System.out.printf(
                "Walker %s: %d of %d won, mean %.1f s%n", difficulty, killed, SEEDS.length, sum / Math.max(1, killed));
        assertTrue(killed >= (difficulty == Difficulty.EASY ? SEEDS.length : SEEDS.length - 1), killed + " won");
    }

    /**
     * A fierce but fair phase 1 (round 43, K.a of round 42; step F4 had made it kind, as the Siege Spire's round 34 P2):
     * with the calibration harness's random dodge (firing all the time, a new way of the nine every {@link
     * #DODGE_SECONDS} s) the ship loses clearly more shield and armour per 10 s of phase 1 at medium, over eight seeds,
     * than the same driver loses in the Harbour Kraken's phase 1 ({@link KrakenTest}'s fixture and fit; 11.4), at least
     * {@link #FIERCER} times as much, but less than {@link #AT_MOST} times.
     */
    @Test
    void phaseOneIsFiercerThanTheKrakensForARandomDodger() {
        double walker = 0;
        double kraken = 0;
        for (long seed : DODGE_SEEDS) {
            Fight fight = fight(
                    sortie(seed, Difficulty.MEDIUM, Level19Test.planLoadout(CONTENT, Difficulty.MEDIUM)),
                    randomDodge(seed),
                    120);
            walker += fight.phaseOneLostPerTen();
            kraken += krakenPhaseOne(seed);
        }
        walker /= DODGE_SEEDS.length;
        kraken /= DODGE_SEEDS.length;
        System.out.printf(
                "Random dodge, shield and armour lost per 10 s of phase 1: walker %.1f, Kraken %.1f%n", walker, kraken);
        assertTrue(
                walker >= FIERCER * kraken && walker < AT_MOST * kraken, "walker " + walker + " vs Kraken " + kraken);
    }

    /**
     * Round 43: phase 1 costs a random dodger at least this times what the Kraken's does (step F4 measured 12.3 against
     * 11.4; the rework 21.0, pending the user's review in round 43).
     */
    static final double FIERCER = 1.3;

    /** Round 43: ... and less than this times. */
    static final double AT_MOST = 2;

    /** The calibration harness's driver: a new way of the nine every this many s. */
    static final double DODGE_SECONDS = 0.4;

    private static final int[] WAYS = {
        0,
        Command.LEFT.bit(),
        Command.RIGHT.bit(),
        Command.UP.bit(),
        Command.DOWN.bit(),
        Command.LEFT.bit() | Command.UP.bit(),
        Command.RIGHT.bit() | Command.UP.bit(),
        Command.LEFT.bit() | Command.DOWN.bit(),
        Command.RIGHT.bit() | Command.DOWN.bit()
    };

    /** Fires all the time and flies one of the nine ways, a new one (seeded) every {@link #DODGE_SECONDS} s. */
    static ToIntFunction<Sortie> randomDodge(long seed) {
        SplittableRandom random = new SplittableRandom(seed);
        int hold = SimStep.ticks(DODGE_SECONDS);
        int[] state = {0, 0};
        return sortie -> {
            if (state[1]-- <= 0) {
                state[0] = WAYS[random.nextInt(WAYS.length)];
                state[1] = hold - 1;
            }
            return Command.FIRE.bit() | state[0];
        };
    }

    /** The random dodger's shield and armour lost per 10 s of the Harbour Kraken's phase 1 (its fixture and fit, medium). */
    private static double krakenPhaseOne(long seed) {
        Sortie sortie = KrakenTest.sortie(seed, Difficulty.MEDIUM);
        SetPiece boss = sortie.setPiece(0);
        ToIntFunction<Sortie> pilot = randomDodge(seed);
        int ticks = 0;
        double lost = 0;
        for (int i = 0; i < SimStep.ticks(120) && !boss.destroyed() && sortie.flying(); i++) {
            double before =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            sortie.step(pilot.applyAsInt(sortie));
            if (boss.engaged() && boss.phase() == 0) {
                ticks++;
                lost += Math.max(
                        0,
                        before
                                - sortie.ship().defences().shield()
                                - sortie.ship().defences().armour());
            }
        }
        return ticks > 0 ? lost * 10 / (ticks * SimStep.SECONDS) : 0;
    }
}
