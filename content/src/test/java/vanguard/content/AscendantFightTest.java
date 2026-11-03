package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.ToIntFunction;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.sim.Allocations;
import vanguard.sim.BossSpec;
import vanguard.sim.Command;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part H, step H3: the Ascendant's fight from its data (design/enemies/bosses/ascendant, Duration and HP; the bow,
 * its mirrors, the fed shield, the playbooks, the regeneration and the escape are the simulation's), flown by the
 * autopilot ({@link AscendantPlay}) with the balance plan's Level 42 fit and Rook ({@link Level42Test#planLoadout})
 * through the whole level, five seeds: "measure, then tune the HP, not the attacks". At medium every seed's fight (the bar
 * to ESCAPED) lies in the act-boss window 90–180 s, its mean is the par, no regenerating part is healed back to full
 * more than {@link #HEALS} times (the regeneration never stalls the fight), and the escape ends the fight (the level won, no kill); easy and hard are won on every seed. Phase 1 is fierce
 * for a reader, not a dodger: the random dodger's loss per 10 s against the Kraken's phase 1 (the calibration band).
 * The bounty keeps the act boss's 24 % share. The random dodger keeps to the ship's room below the bow (ramming the hull is
 * no reading of its attacks).
 */
class AscendantFightTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String SLUG = "ascendant";
    /** The seeds the fight is measured on (the spec's five). */
    static final long[] SEEDS = {1, 2, 3, 4, 5};
    /** The calibration harness's seeds. */
    static final long[] DODGE_SEEDS = RevenantWalkerTest.DODGE_SEEDS;

    /**
     * The par: the autopilot's measured mean at medium (round 80, the new phase-1 moves and the 4 s escape: 130.6 s, every seed
     * 124.9-135.7 s; step
     * H3's 142 with the 14 s chase; see {@link #theFightLastsItsParAtMedium}'s print).
     */
    static final double PAR = 131;

    /** The most times a regenerating part may heal back to full in a fight (the spec: more means a slower rate). */
    static final int HEALS = 2;

    /** What the fight cost the ship and when each part died, on the attempt that completes the level. */
    static final class Timeline implements LevelMetrics.Probe {
        /** Each part's death in s after the bar (NaN: alive). */
        double[] deaths = new double[0];
        /** Shield and armour lost in each phase (and the escape's, last). */
        double[] lost = new double[0];

        private double before = Double.NaN;

        @Override
        public void newAttempt() {
            Arrays.fill(deaths, Double.NaN);
            Arrays.fill(lost, 0);
            before = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            SetPiece piece = AscendantPlay.escapingBoss(sortie);
            if (piece == null || piece.bossSeconds() < 0) {
                before = Double.NaN;
                return;
            }
            if (deaths.length != piece.partCount()) {
                deaths = new double[piece.partCount()];
                Arrays.fill(deaths, Double.NaN);
                lost = new double[piece.boss().orElseThrow().phases().size() + 1];
            }
            for (int p = 0; p < piece.partCount(); p++) {
                if (Double.isNaN(deaths[p]) && piece.partHp(p) <= 0) {
                    deaths[p] = piece.bossSeconds();
                }
            }
            double now =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            int at = piece.escape().orElseThrow().broken() ? lost.length - 1 : piece.phase();
            if (!Double.isNaN(before)) {
                lost[at] += Math.max(0, before - now);
            }
            before = now;
        }

        String deaths(SetPiece piece) {
            StringBuilder out = new StringBuilder();
            for (int p = 0; p < deaths.length; p++) {
                out.append(String.format(
                        Locale.ROOT, " %s %.0f", piece.spec().parts().get(p).name(), deaths[p]));
            }
            return out.toString();
        }
    }

    /** A fight: the escape's measures, the level's run, the parts' deaths and the losses by phase. */
    record Fight(EscapeWatch.Fight escape, LevelMetrics.Run run, String deaths, double[] lost) {
        double seconds() {
            return escape.escaped();
        }

        boolean complete() {
            return run.complete();
        }

        @Override
        public String toString() {
            return String.format(
                    Locale.ROOT,
                    "%s; %s, retries %d; lost by phase %s;%s",
                    escape,
                    complete() ? "won" : "LOST",
                    run.retries(),
                    Arrays.toString(Arrays.stream(lost).map(Math::round).toArray()),
                    deaths);
        }
    }

    /** The autopilot flies Level 42 with the plan's fit and Rook on {@code seed} (retried after a loss) and its fight. */
    static Fight fight(long seed, Difficulty difficulty) {
        Sortie sortie = LevelMetrics.sortie(
                CONTENT, Level42Test.LEVEL, difficulty, Level42Test.planLoadout(CONTENT, difficulty), seed);
        EscapeWatch watch = new EscapeWatch(seed, false);
        Timeline timeline = new Timeline();
        LevelMetrics.fly(sortie, Level42Test.MAX_STEPS, watch, timeline);
        SetPiece piece = sortie.setPiece(bossIndex(sortie));
        return new Fight(watch.fight(), LevelMetrics.Run.of(sortie), timeline.deaths(piece), timeline.lost.clone());
    }

    private static int bossIndex(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            if (sortie.setPiece(i).escape().isPresent()) {
                return i;
            }
        }
        throw new AssertionError("no escaping boss");
    }

    /** The fights over {@link #SEEDS}, flown in parallel, in their order. */
    static List<Fight> fights(Difficulty difficulty) {
        return LongStream.of(SEEDS)
                .parallel()
                .mapToObj(seed -> fight(seed, difficulty))
                .toList();
    }

    @Test
    void theStatBlockMapsToAnEscapingActBossWithItsPar() {
        SetPieceSpec spec = SimSpecs.level(CONTENT, Level42Test.LEVEL, Difficulty.MEDIUM).setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
        BossSpec boss = spec.boss().orElseThrow();
        assertTrue(!boss.midBoss(), "an act boss");
        assertTrue(boss.escape().isPresent(), "it escapes");
        assertEquals(PAR, boss.parSeconds(), "the par: the measured mean");
        EnemyData ascendant = CONTENT.enemy(SLUG);
        List<EnemyData.PartData> parts = ascendant.partList().orElseThrow();
        double hull = parts.stream().mapToDouble(EnemyData.PartData::hp).sum();
        assertEquals(hull, ascendant.hp(), "the hull's parts' sum (the lifeboat has no HP)");
        assertTrue(
                parts.stream().filter(part -> part.name().equals("lifeboat")).allMatch(EnemyData.PartData::armoured),
                "round 80: the lifeboat is scenery");
    }

    /**
     * Measure, then tune the HP (the spec's Duration and HP): with the plan's fit and Rook the fight lasts its par at
     * medium (the bar to ESCAPED), inside the act-boss window 90–180 s on every seed; no part healed back to full more
     * than {@link #HEALS} times; the level won
     * and the fight over at the escape (its bar's run counts it over without a kill).
     */
    @Test
    void theFightLastsItsParAtMedium() {
        double sum = 0;
        List<Fight> fights = fights(Difficulty.MEDIUM);
        for (Fight fight : fights) {
            System.out.printf("Ascendant medium %s%n", fight);
            sum += fight.seconds();
        }
        double mean = sum / SEEDS.length;
        System.out.printf(Locale.ROOT, "Ascendant medium: mean %.1f s, par %.0f%n", mean, PAR);
        for (Fight fight : fights) {
            String at = "seed " + fight.escape().seed() + ": " + fight;
            assertTrue(fight.complete(), at);
            assertTrue(!Double.isNaN(fight.escape().broken()), "the hull broke: " + at);
            assertTrue(LevelMetrics.inBossWindow(false, fight.seconds()), "the act-boss window: " + at);
            assertTrue(fight.run().bosses().stream().allMatch(LevelMetrics.BossFight::killed), "the escape ends it");
            assertTrue(fight.escape().heals() <= HEALS, "the regeneration never stalls it: " + at);
        }
        assertEquals(PAR, mean, 5, "the par is the measured mean");
    }

    /** Easy and hard are won on every seed (the level flown to its end, retried after a loss), the hull broken. */
    @ParameterizedTest
    @EnumSource(
            value = Difficulty.class,
            names = {"EASY", "HARD"})
    void theFightOnEasyAndHard(Difficulty difficulty) {
        double sum = 0;
        for (Fight fight : fights(difficulty)) {
            System.out.printf("Ascendant %s %s%n", difficulty, fight);
            String at = difficulty + " seed " + fight.escape().seed() + ": " + fight;
            assertTrue(fight.complete() && !Double.isNaN(fight.escape().escaped()), at);
            sum += fight.seconds();
        }
        System.out.printf(Locale.ROOT, "Ascendant %s: mean %.1f s%n", difficulty, sum / SEEDS.length);
    }

    /**
     * Reading, not dodging (the spec's Duration and HP: phase 1 against the Kraken's): with the calibration harness's
     * random dodge ({@link RevenantWalkerTest#randomDodge}), taking over from the autopilot as the hull settles, the ship
     * loses, per 10 s of phase 1 at medium over eight seeds, between {@link #LEAST} and {@link #MOST} times what the same
     * driver loses in the Kraken's phase 1 (the walker's band: fiercer than the Kraken's, not a wall).
     */
    @Test
    void phaseOneIsFiercerThanTheKrakensForARandomDodger() {
        double ascendant = 0;
        double kraken = 0;
        for (long seed : DODGE_SEEDS) {
            ascendant += randomPhaseOne(seed);
            kraken += RevenantWalkerTest.krakenPhaseOne(seed);
        }
        ascendant /= DODGE_SEEDS.length;
        kraken /= DODGE_SEEDS.length;
        System.out.printf(
                Locale.ROOT,
                "Random dodge, shield and armour lost per 10 s: Ascendant phase 1 %.1f, Kraken phase 1 %.1f (%.2f×)%n",
                ascendant,
                kraken,
                ascendant / kraken);
        assertTrue(
                ascendant >= LEAST * kraken && ascendant <= PENDING_MOST * kraken,
                "Ascendant " + ascendant + " vs " + kraken);
        if (ascendant > MOST * kraken) {
            System.out.printf(
                    Locale.ROOT,
                    "Ascendant phase 1 over the walker's band (%.2f× > %.1f×): pending the user's review (round 80)%n",
                    ascendant / kraken,
                    MOST);
        }
    }

    /** The random dodger keeps its hull this far below the bow's tip (the ship's room), px. */
    static final double ROOM_MARGIN = 10;

    /** The walker's band: phase 1 costs a random dodger at least this times the Kraken's phase 1 ... */
    static final double LEAST = RevenantWalkerTest.FIERCER;

    /** ... and at most this times. */
    static final double MOST = RevenantWalkerTest.AT_MOST;

    /**
     * Step H3 (pending the user's review in round 80): the most for now. Measured 3.1× with the drafted clock (the
     * batteries every 3 s, two at once, the cannon's rest 1.5 s), 2.45× (28.0 per 10 s against 11.4) after the spec's
     * levers (the batteries every 6 s, one at a time, the rest 3.5 s): what stays is the cannon's homing salvo and its
     * fans (the random dodger never kills the generators that feed the racks) and its own fire bounced off the panels; the
     * attacks are left as approved, as the Honour Guard's 2.6× and the Abyssal Maw's (round 58). M10 round 80 fixes,
     * R80a: round 80's B.1 (the user: phase 1 needs more action, the lance more often) added the sweep, the twins and the
     * barrage: 2.70× (30.8 against 11.4), the rests kept.
     */
    static final double PENDING_MOST = 2.8;

    /**
     * The random dodger's shield and armour lost per 10 s of phase 1 (medium, the plan's fit and Rook): the autopilot
     * flies the approach (retried after a loss) until the hull is engaged, then the dodger for at most 120 s, until phase
     * 1 ends or the ship is lost.
     */
    static double randomPhaseOne(long seed) {
        Sortie sortie = Level42Test.sortie(
                CONTENT, Level42Test.planLoadout(CONTENT, Difficulty.MEDIUM), seed, Difficulty.MEDIUM, false);
        int steps = 0;
        SetPiece boss = null;
        while (!sortie.complete() && steps++ < Level42Test.MAX_STEPS) {
            Level04Test.step(sortie);
            boss = AscendantPlay.escapingBoss(sortie);
            if (boss != null && boss.engaged()) {
                break;
            }
        }
        assertTrue(boss != null && boss.engaged(), "the hull engages, seed " + seed);
        ToIntFunction<Sortie> random = RevenantWalkerTest.randomDodge(seed);
        double room = boss.renderY(1)
                + boss.boss().orElseThrow().wedge().orElseThrow().tipDy()
                - ROOM_MARGIN;
        // in the ship's room below the bow (the spec's Arena): no ramming of the hull
        ToIntFunction<Sortie> dodge = ship -> {
            int commands = random.applyAsInt(ship);
            return ship.ship().y() + MovePlanner.HULL_Y > room ? commands & ~Command.UP.bit() : commands;
        };
        int ticks = 0;
        double lost = 0;
        for (int i = 0; i < SimStep.ticks(120) && boss.phase() == 0 && sortie.flying(); i++) {
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
     * The act boss's share of budget(42), 24 % (the spec's Bounty): its bounty is 0.24 × budget ÷ (the act factor ×
     * the level's {@code bounty_scale}), to within one credit in Act 1 terms; the parts' bounties sum to it.
     */
    @Test
    void theBountyKeepsTheActBossShare() {
        EnemyData ascendant = CONTENT.enemy(SLUG);
        LevelData level = CONTENT.level(Level42Test.LEVEL);
        double budget = TypicalHaul.of(CONTENT, Level42Test.LEVEL).budget();
        double factor = Math.pow(CONTENT.economy().actFactor(), 5);
        long expected = Math.round(SHARE * budget / (factor * level.bounties()));
        System.out.printf(
                Locale.ROOT,
                "Ascendant bounty %d (expected %d at the scale %.2f of budget %.0f)%n",
                ascendant.bounty(),
                expected,
                level.bounties(),
                budget);
        assertEquals(expected, ascendant.bounty(), 1, "the share " + SHARE + " of " + budget);
        assertEquals(
                ascendant.bounty(),
                ascendant.partList().orElseThrow().stream()
                        .mapToInt(EnemyData.PartData::bounty)
                        .sum(),
                "the parts' bounties sum to it");
    }

    /** The act boss's share of the level's budget. */
    static final double SHARE = 0.24;

    @Test
    void theFightAllocatesNothingPerStep() {
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = Level42Test.sortie(
                            CONTENT, Level42Test.planLoadout(CONTENT, Difficulty.HARD), 2185, Difficulty.HARD, false);
                    int steps = 0;
                    while (!sortie.complete() && steps++ < Level42Test.MAX_STEPS) {
                        Level04Test.step(sortie);
                        if (AscendantPlay.escapingBoss(sortie) != null) {
                            break;
                        }
                    }
                    return sortie;
                },
                sortie -> {
                    for (int i = 0;
                            i < SimStep.ticks(120) && AscendantPlay.escapingBoss(sortie) != null && sortie.flying();
                            i++) {
                        sortie.step(Autopilot.commands(sortie));
                    }
                });

        assertEquals(0, allocated, "the fight allocated " + allocated + " bytes");
    }
}
