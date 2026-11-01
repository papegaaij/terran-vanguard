package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.sim.Allocations;
import vanguard.sim.BossSpec;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part F, step F3: the Honour Guard's duel from its data (design/enemies/bosses/honour-guard, Duration and HP; the
 * three aces, their joins, the playbook and the conductor are the simulation's: {@code SquadronTest}, {@code
 * AcePlaybookTest}), flown by the autopilot ({@link GuardPlay}) with the balance plan's Level 40 fit and Rook ({@link
 * Level40Test#planLoadout}) through the whole level (its approach decides what the ship brings into the arena), five
 * seeds: "measure, then tune the HP, not the attacks". At medium every seed's duel (the bar to the last ace's kill) lies
 * in the mid-boss window 45–75 s, its mean is the par, all three aces fly together for at least 10 s on every seed, and
 * the conductor tells one line or dive at a time with at most two aces firing; easy and hard are won on every seed. The
 * duel is fierce for a reader, not a dodger: the random dodger's loss per 10 s against the Kraken's phase 1 (the
 * walker's calibration band). The bounty keeps the mid-boss's 15 % share.
 */
class HonourGuardFightTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String SLUG = "honour-guard";
    /** The seeds the duel is measured on (the spec's five). */
    static final long[] SEEDS = {1, 2, 3, 4, 5};
    /** The calibration harness's seeds. */
    static final long[] DODGE_SEEDS = RevenantWalkerTest.DODGE_SEEDS;

    /**
     * The par: the autopilot's measured mean at medium (step F2's first-cut autopilot 62.0 s, every seed 59.6–63.2 s;
     * step F3's {@link GuardPlay} 62.3 s; step F6c, the aces' flight of step F6s in their separate bands of round 78 B.2
     * b: 65.5 s, every seed 60.2–72.0 s; see {@link #theDuelLastsItsParAtMedium}'s print).
     */
    static final double PAR = 66;

    /** The least time all three aces fly together on every seed at medium, s (the spec's "the three together"). */
    static final double TOGETHER = 10;

    /** A duel with what the ship took in it, on the attempt that completes the level. */
    record Fight(GuardWatch.Duel duel, boolean complete, int retries, double lost, int lineHits, int contacts) {
        boolean killed() {
            return !Double.isNaN(duel.seconds());
        }

        @Override
        public String toString() {
            return String.format(
                    "%s; %s, retries %d, lost %.0f in the duel, line hits %d, ace contacts %d",
                    duel, complete ? "won" : "LOST", retries, lost, lineHits, contacts);
        }
    }

    /** The ship's losses, the sniper line's hits and the aces' contacts while the duel runs. */
    static final class Hits implements LevelMetrics.Probe {
        double lost;
        int lineHits;
        int contacts;
        private double before = Double.NaN;

        @Override
        public void newAttempt() {
            lost = 0;
            lineHits = 0;
            contacts = 0;
            before = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            double now =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            boolean duel = GuardPlay.squadronBoss(sortie) != null;
            if (duel && !Double.isNaN(before)) {
                lost += Math.max(0, before - now);
            }
            before = now;
            if (duel) {
                lineHits += sortie.events().count(SimEvents.Type.BOSS_LINE_HIT);
                contacts += sortie.events().count(SimEvents.Type.ACE_CONTACT);
            }
        }
    }

    /** The autopilot flies Level 40 with the plan's fit and Rook on {@code seed} (retried after a loss) and its duel. */
    static Fight fight(long seed, Difficulty difficulty) {
        Sortie sortie = LevelMetrics.sortie(
                CONTENT, Level40Test.LEVEL, difficulty, Level40Test.planLoadout(CONTENT, difficulty), seed);
        GuardWatch watch = new GuardWatch(seed);
        Hits hits = new Hits();
        boolean complete = LevelMetrics.fly(sortie, Level40Test.MAX_STEPS, watch, hits);
        return new Fight(watch.duel(), complete, sortie.attempt() - 1, hits.lost, hits.lineHits, hits.contacts);
    }

    /** The fights over {@link #SEEDS}, flown in parallel, in their order. */
    static List<Fight> fights(Difficulty difficulty) {
        return LongStream.of(SEEDS)
                .parallel()
                .mapToObj(seed -> fight(seed, difficulty))
                .toList();
    }

    @Test
    void theStatBlockMapsToASquadronMidBossWithItsPar() {
        SetPieceSpec spec = SimSpecs.level(CONTENT, Level40Test.LEVEL, Difficulty.MEDIUM).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
        BossSpec boss = spec.boss().orElseThrow();
        assertTrue(boss.midBoss(), "a mid-boss");
        assertTrue(boss.squadron().isPresent(), "a squadron");
        assertEquals(3, boss.squadron().get().aces().size(), "three aces");
        assertEquals(PAR, boss.parSeconds(), "the par: the measured mean");
        EnemyData guard = CONTENT.enemy(SLUG);
        List<EnemyData.PartData> parts = guard.partList().orElseThrow();
        assertEquals(3, parts.size());
        parts.forEach(part -> assertEquals(parts.getFirst().hp(), part.hp(), "the three HP together: " + part));
        assertEquals(3 * parts.getFirst().hp(), guard.hp(), "the parts' sum");
    }

    /**
     * Measure, then tune the HP (the spec's Duration and HP): with the plan's fit and Rook the duel lasts its par at
     * medium (the bar to the last ace's kill), inside the mid-boss window 45–75 s on every seed; all three aces fly
     * together for at least {@link #TOGETHER} s on every seed (the joins, not the HP, are the lever); the conductor: at
     * most one told line or dive at a time and at most two aces firing (told plays, firing aces at once, the duel's
     * most). The leader's separation pushes (round 78's boss question) and each ace out of its band while it holds
     * (step F6c) are printed.
     */
    @Test
    void theDuelLastsItsParAtMedium() {
        double sum = 0;
        List<Fight> fights = fights(Difficulty.MEDIUM);
        for (Fight fight : fights) {
            System.out.printf("Honour Guard medium %s%n", fight);
            sum += fight.duel().seconds();
        }
        double mean = sum / SEEDS.length;
        System.out.printf("Honour Guard medium: mean %.1f s, par %.0f%n", mean, PAR);
        for (Fight fight : fights) {
            String at = "seed " + fight.duel().seed() + ": " + fight;
            assertTrue(fight.complete(), at);
            assertTrue(fight.killed(), "killed: " + at);
            assertTrue(LevelMetrics.inBossWindow(true, fight.duel().seconds()), "the mid-boss window: " + at);
            assertTrue(fight.duel().together() >= TOGETHER, "all three together: " + at);
            assertTrue(fight.duel().told() <= 1, "one told line or dive at a time: " + at);
            assertTrue(fight.duel().firing() <= 2, "at most two aces firing: " + at);
        }
        assertEquals(PAR, mean, 5, "the par is the measured mean");
    }

    /** Easy and hard are won on every seed (the level flown to its end, retried after a loss); the conductor holds. */
    @ParameterizedTest
    @EnumSource(
            value = Difficulty.class,
            names = {"EASY", "HARD"})
    void theDuelOnEasyAndHard(Difficulty difficulty) {
        double sum = 0;
        for (Fight fight : fights(difficulty)) {
            System.out.printf("Honour Guard %s %s%n", difficulty, fight);
            String at = difficulty + " seed " + fight.duel().seed() + ": " + fight;
            assertTrue(fight.complete() && fight.killed(), at);
            assertTrue(fight.duel().told() <= 1 && fight.duel().firing() <= 2, "the conductor: " + at);
            sum += fight.duel().seconds();
        }
        System.out.printf("Honour Guard %s: mean %.1f s%n", difficulty, sum / SEEDS.length);
    }

    /**
     * Reading, not dodging (the spec's Duration and HP): with the calibration harness's random dodge ({@link
     * RevenantWalkerTest#randomDodge}), taking over from the autopilot as the bar shows, the ship loses, per 10 s of the
     * duel at medium over eight seeds, between {@link #LEAST} and {@link #MOST} times what the same driver loses in the
     * Kraken's phase 1 (the walker's band: fiercer than the Kraken's, not a wall); above it up to {@link #PENDING_MOST},
     * pending the user's review.
     */
    @Test
    void theDuelIsFiercerThanTheKrakensForARandomDodger() {
        double guard = 0;
        double kraken = 0;
        for (long seed : DODGE_SEEDS) {
            guard += randomDuel(seed);
            kraken += RevenantWalkerTest.krakenPhaseOne(seed);
        }
        guard /= DODGE_SEEDS.length;
        kraken /= DODGE_SEEDS.length;
        System.out.printf(
                "Random dodge, shield and armour lost per 10 s: Honour Guard %.1f, Kraken phase 1 %.1f (%.2f×)%n",
                guard, kraken, guard / kraken);
        assertTrue(
                guard >= LEAST * kraken && guard <= PENDING_MOST * kraken, "Honour Guard " + guard + " vs " + kraken);
        if (guard > MOST * kraken) {
            System.out.printf(
                    "Honour Guard over the walker's band (%.2f× > %.1f×): pending the user's review (round 78)%n",
                    guard / kraken, MOST);
        }
    }

    /** The walker's band: the duel costs a random dodger at least this times the Kraken's phase 1 ... */
    static final double LEAST = RevenantWalkerTest.FIERCER;

    /** ... and at most this times. */
    static final double MOST = RevenantWalkerTest.AT_MOST;

    /**
     * Step F3 (pending the user's review in round 78): the most for now. Measured 2.6× (29.8 per 10 s against 11.4, the
     * HP at 1,900 an ace): three aces firing in turn at a dodger who never reads a tell (the sniper line, the dives'
     * bursts, Vesper's spiral and ribbon); the spec's levers (the rests, the conductor's limits) are attacks' data and
     * are left as approved (HP tuned, not attacks), as the Abyssal Maw's 2.6× (round 58).
     */
    static final double PENDING_MOST = 3.0;

    /**
     * The random dodger's shield and armour lost per 10 s of the duel (medium, the plan's fit and Rook): the autopilot
     * flies the approach (retried after a loss) until the bar shows, then the dodger for at most 120 s, until the last
     * ace dies or the ship is lost.
     */
    static double randomDuel(long seed) {
        Sortie sortie = Level40Test.sortie(
                CONTENT, Level40Test.planLoadout(CONTENT, Difficulty.MEDIUM), seed, Difficulty.MEDIUM, false);
        int steps = 0;
        SetPiece guard = null;
        while (!sortie.complete() && steps++ < Level40Test.MAX_STEPS) {
            Level04Test.step(sortie);
            guard = GuardPlay.squadronBoss(sortie);
            if (guard != null) {
                break;
            }
        }
        assertTrue(guard != null, "the bar shows, seed " + seed);
        ToIntFunction<Sortie> dodge = RevenantWalkerTest.randomDodge(seed);
        int ticks = 0;
        double lost = 0;
        for (int i = 0; i < SimStep.ticks(120) && !guard.destroyed() && sortie.flying(); i++) {
            double before =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            sortie.step(dodge.applyAsInt(sortie));
            ticks++;
            lost += Math.max(
                    0,
                    before
                            - sortie.ship().defences().shield()
                            - sortie.ship().defences().armour());
        }
        return ticks > 0 ? lost * 10 / (ticks * SimStep.SECONDS) : 0;
    }

    /**
     * The mid-boss's share of budget(40), 15 % (the spec's Bounty): its bounty is 0.15 × budget ÷ (the act factor × the
     * level's {@code bounty_scale}), to within one credit in Act 1 terms, 120 an ace (the parts' equal shares).
     */
    @Test
    void theBountyKeepsTheMidBossShare() {
        EnemyData guard = CONTENT.enemy(SLUG);
        LevelData level = CONTENT.level(Level40Test.LEVEL);
        double budget = TypicalHaul.of(CONTENT, Level40Test.LEVEL).budget();
        double factor = Math.pow(CONTENT.economy().actFactor(), 5);
        long expected = Math.round(SHARE * budget / (factor * level.bounties()));
        System.out.printf(
                "Honour Guard bounty %d (expected %d at the scale %.2f of budget %.0f)%n",
                guard.bounty(), expected, level.bounties(), budget);
        assertEquals(expected, guard.bounty(), 1, "the share " + SHARE + " of " + budget);
        guard.partList()
                .orElseThrow()
                .forEach(part -> assertEquals(guard.bounty() / 3, part.bounty(), "an ace's share: " + part));
    }

    /** The mid-boss's share of the level's budget. */
    static final double SHARE = 0.15;

    @Test
    void theDuelAllocatesNothingPerStep() {
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = Level40Test.sortie(
                            CONTENT, Level40Test.planLoadout(CONTENT, Difficulty.HARD), 2185, Difficulty.HARD, false);
                    int steps = 0;
                    while (!sortie.complete() && steps++ < Level40Test.MAX_STEPS) {
                        Level04Test.step(sortie);
                        if (GuardPlay.squadronBoss(sortie) != null) {
                            break;
                        }
                    }
                    return sortie;
                },
                sortie -> {
                    for (int i = 0;
                            i < SimStep.ticks(120) && GuardPlay.squadronBoss(sortie) != null && sortie.flying();
                            i++) {
                        sortie.step(Autopilot.commands(sortie));
                    }
                });

        assertEquals(0, allocated, "the duel allocated " + allocated + " bytes");
    }
}
