package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.ToIntFunction;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.Command;
import vanguard.sim.PlayField;
import vanguard.sim.RingArena;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part G, step G2 (design/enemies/bosses, Boss rush; Level 49; m11g-gaps.md §1 #11): the three echoes' fights from
 * their data, flown by the autopilot through Level 49 with the balance plan's fit and Rook ({@link Level49Test#planLoadout})
 * on five seeds: "measure, then tune the HP". At medium every echo lasts 40–60 s from the bar to the kill on every
 * seed (step G5b: the Colossus's too, its vents in turn and its mouth sealing once hurt in a breach) and none is quiet
 * over {@link #QUIET} (nothing of it closing on the ship: a bullet, a launched unit); the rush (the first bar to the last
 * kill) about its par ({@link Level49Test#PAR}); easy's and hard's times are reported.
 *
 * <p>Step G5 (round 93 L.3, the user's boss recipe): each echo in two stages, its subtargets (each its own gun) and then
 * its heart: a dead subtarget never fires again, stage 2 starts only once every subtarget is dead, each echo fires while
 * it forms or first comes into view, and each fight fires at least {@link #SHOTS_FLOOR} shots a minute; a ship that
 * stands still loses its armour ({@link #standingStillLosesTheArmour}).
 */
class EchoFightTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    /** The seeds the fights are measured on. */
    static final long[] SEEDS = {1, 2, 3, 4, 5};

    /** An echo's window from its bar to its kill, s (the bosses' Boss rush rule). */
    static final double LEAST = 40;

    static final double MOST = 60;

    /** The echoes' names, in the rush's order. */
    static final String[] NAMES = {"Carrier", "Colossus", "Sovereign"};

    /**
     * The longest quiet allowed every echo, s: the rule's 1.0 s (step G5b: the Colossus's too, its spine firing at a
     * ship on the mouth's lip).
     */
    static final double QUIET = AscendantPressure.MOST_QUIET;

    /**
     * Step G5: the least enemy shots a minute in each echo's fight at medium ({@link #shotsNow}); before the rework the
     * fights fired a third to a half of it.
     */
    static final double SHOTS_FLOOR = 150;

    /** Step G5: the least shots an echo fires as it forms or first comes into view (before its bar and its first 3 s). */
    static final int ENTRY_SHOTS = 4;

    /** Step G5: a shot within this of a part's centre (each axis) comes from that part, px (ChimeraFightTest's). */
    static final double PLACE = ChimeraFightTest.PLACE;

    /** The three fights' measures on the attempt that completes the level (a retry at a fight's checkpoint resets it). */
    static final class Watch implements LevelMetrics.Probe {
        /** Per fight, the real seconds its bar showed (engaged) and its kill; NaN before. */
        final double[] bars = new double[3];

        final double[] kills = new double[3];
        /** Per fight, the longest stretch without a threat from its first one, steps. */
        final int[] quiet = new int[3];
        /** Retries at a fight's checkpoint. */
        int retries;

        /** Per fight, where its longest quiet ended: the boss phase and s after the bar. */
        final String[] quietAt = new String[3];

        /** Step G5: per fight, the real seconds its stage 2 began; NaN before. */
        final double[] stage2 = new double[3];
        /** Step G5: per fight and stage, the enemy shots (one per {@code ENEMY_FIRED}). */
        final int[][] shots = new int[3][2];
        /** Step G5: per fight and stage, the shield and armour the ship lost. */
        final double[][] lost = new double[3][2];
        /** Step G5: per fight, the shots fired as it formed or came into view (before its bar, and its first 3 s). */
        final int[] entry = new int[3];
        /** Step G5: per fight, shots, lobs, missiles and launches from the place of a dead subtarget in stage 1. */
        final int[] fromDead = new int[3];
        /** Step G5: per fight, whether stage 2 began with a subtarget alive. */
        final boolean[] early = new boolean[3];

        private final int[] run = new int[3];
        private final boolean[] threatened = new boolean[3];
        private final double[][] deaths = new double[3][];
        private double before = Double.NaN;

        Watch() {
            newAttempt();
        }

        @Override
        public void newAttempt() {
            Arrays.fill(bars, Double.NaN);
            Arrays.fill(kills, Double.NaN);
            Arrays.fill(stage2, Double.NaN);
            Arrays.fill(quiet, 0);
            Arrays.fill(run, 0);
            Arrays.fill(threatened, false);
            Arrays.fill(entry, 0);
            Arrays.fill(fromDead, 0);
            Arrays.fill(early, false);
            for (int f = 0; f < 3; f++) {
                Arrays.fill(shots[f], 0);
                Arrays.fill(lost[f], 0);
                deaths[f] = null;
            }
            before = Double.NaN;
        }

        @Override
        public void bossRetry() {
            retries++;
            // the fight in progress starts again; the earlier ones stay as they were
            for (int f = 0; f < 3; f++) {
                if (Double.isNaN(kills[f])) {
                    bars[f] = Double.NaN;
                    stage2[f] = Double.NaN;
                    quiet[f] = 0;
                    run[f] = 0;
                    threatened[f] = false;
                    entry[f] = 0;
                    fromDead[f] = 0;
                    early[f] = false;
                    Arrays.fill(shots[f], 0);
                    Arrays.fill(lost[f], 0);
                    deaths[f] = null;
                }
            }
            before = Double.NaN;
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            if (!sortie.bosses().rush()) {
                return;
            }
            double now =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            double drop = Double.isNaN(before) ? 0 : Math.max(0, before - now);
            before = now;
            for (int f = 0; f < sortie.bosses().fights(); f++) {
                SetPiece piece = sortie.setPiece(sortie.bosses().fightPiece(f));
                if (piece.destroyed()) {
                    if (!Double.isNaN(bars[f]) && Double.isNaN(kills[f])) {
                        kills[f] = sortie.realSeconds();
                    }
                    continue;
                }
                if (!piece.present()) {
                    continue;
                }
                int fired = shotsNow(sortie);
                if (!piece.engaged()) {
                    entry[f] += fired;
                    continue;
                }
                if (Double.isNaN(bars[f])) {
                    bars[f] = sortie.realSeconds();
                    deaths[f] = new double[piece.partCount()];
                    Arrays.fill(deaths[f], Double.NaN);
                }
                double since = sortie.realSeconds() - bars[f];
                int stage = Math.min(1, piece.phase());
                if (stage == 1 && Double.isNaN(stage2[f])) {
                    stage2[f] = sortie.realSeconds();
                    for (int p : subtargets(piece)) {
                        early[f] |= piece.partHp(p) > 0;
                    }
                }
                shots[f][stage] += fired;
                lost[f][stage] += drop;
                if (since <= 3) {
                    entry[f] += fired;
                }
                if (stage == 0) {
                    // stage 1's guns (stage 2 fires from the heart: the Sovereign's core hurls the dead turrets'
                    // wrecks)
                    fromDead[f] += fromDeadParts(sortie, piece, deaths[f]);
                }
                for (int p = 0; p < piece.partCount(); p++) {
                    if (Double.isNaN(deaths[f][p]) && piece.partHp(p) <= 0) {
                        deaths[f][p] = sortie.realSeconds();
                    }
                }
                if (AscendantPressure.threat(sortie, piece)) {
                    threatened[f] = true;
                    run[f] = 0;
                } else if (threatened[f]) {
                    run[f]++;
                    if (run[f] > quiet[f]) {
                        quiet[f] = run[f];
                        quietAt[f] = String.format(Locale.ROOT, "phase %d at %.1f s", piece.phase(), since);
                    }
                }
            }
        }

        /**
         * This step's shots, lobs, missiles and launches from the place of a subtarget dead before this step (and of no
         * living part, no launched unit).
         */
        private static int fromDeadParts(Sortie sortie, SetPiece piece, double[] deaths) {
            SimEvents events = sortie.events();
            int found = 0;
            for (int i = 0; i < events.size(); i++) {
                SimEvents.Type type = events.type(i);
                if (type == SimEvents.Type.ENEMY_FIRED
                        || type == SimEvents.Type.MISSILE_LAUNCHED
                        || type == SimEvents.Type.MORTAR_LOBBED
                        || type == SimEvents.Type.BOSS_LAUNCHED) {
                    found += nearDead(piece, deaths, events.x(i), events.y(i))
                                    && !nearLiving(piece, events.x(i), events.y(i))
                                    && !nearUnit(sortie, events.x(i), events.y(i))
                            ? 1
                            : 0;
                }
            }
            return found;
        }

        private static boolean nearUnit(Sortie sortie, double x, double y) {
            for (int i = 0; i < sortie.enemyCount(); i++) {
                if (Math.abs(sortie.enemy(i).renderX(1) - x) <= PLACE
                        && Math.abs(sortie.enemy(i).renderY(1) - y) <= PLACE) {
                    return true;
                }
            }
            return false;
        }

        private static boolean nearLiving(SetPiece piece, double x, double y) {
            for (int p = 0; p < piece.partCount(); p++) {
                if (piece.partHp(p) > 0
                        && Math.abs(piece.partX(p) - x) <= PLACE
                        && Math.abs(piece.partY(p) - y) <= PLACE) {
                    return true;
                }
            }
            return false;
        }

        private static boolean nearDead(SetPiece piece, double[] deaths, double x, double y) {
            for (int p : subtargets(piece)) {
                if (!Double.isNaN(deaths[p])
                        && Math.abs(piece.partX(p) - x) <= PLACE
                        && Math.abs(piece.partY(p) - y) <= PLACE) {
                    return true;
                }
            }
            return false;
        }

        double seconds(int f) {
            return kills[f] - bars[f];
        }

        double rush() {
            return kills[2] - bars[0];
        }

        /** Step G5: fight {@code f}'s enemy shots a minute from its bar to its kill. */
        double shotsPerMinute(int f) {
            return (shots[f][0] + shots[f][1]) * 60 / seconds(f);
        }

        /** Step G5: the seconds fight {@code f} spent in stage {@code stage}. */
        double stageSeconds(int f, int stage) {
            return stage == 0 ? stage2[f] - bars[f] : kills[f] - stage2[f];
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            for (int f = 0; f < 3; f++) {
                out.append(String.format(
                        Locale.ROOT,
                        "%n  %s: %.1f s (stages %.1f + %.1f; quiet %.2f s, %s), shots/min %.0f (%.0f / %.0f),"
                                + " lost/s %.1f / %.1f, entry shots %d, from dead parts %d%s",
                        NAMES[f],
                        seconds(f),
                        stageSeconds(f, 0),
                        stageSeconds(f, 1),
                        quiet[f] * SimStep.SECONDS,
                        quietAt[f],
                        shotsPerMinute(f),
                        shots[f][0] * 60 / stageSeconds(f, 0),
                        shots[f][1] * 60 / stageSeconds(f, 1),
                        lost[f][0] / stageSeconds(f, 0),
                        lost[f][1] / stageSeconds(f, 1),
                        entry[f],
                        fromDead[f],
                        early[f] ? ", STAGE 2 EARLY" : ""));
            }
            return out.append(String.format(Locale.ROOT, "%n  rush %.1f s, %d retries", rush(), retries))
                    .toString();
        }
    }

    /**
     * Step G5: this step's enemy shots: one per {@code ENEMY_FIRED} (a fan or a burst counts once, a spiral's bullets
     * each), {@code MISSILE_LAUNCHED} (a homing diamond) and {@code MORTAR_LOBBED} (a lob).
     */
    static int shotsNow(Sortie sortie) {
        return sortie.events().count(SimEvents.Type.ENEMY_FIRED)
                + sortie.events().count(SimEvents.Type.MISSILE_LAUNCHED)
                + sortie.events().count(SimEvents.Type.MORTAR_LOBBED);
    }

    /**
     * Step G5: the echo's subtargets, stage 1's: the parts its first phase ends on, or (a ringed echo's, whose stage
     * ends on its ring's lock) its ring's turrets.
     */
    static List<Integer> subtargets(SetPiece piece) {
        BossSpec boss = piece.boss().orElseThrow();
        List<Integer> parts = boss.phases().getFirst().untilParts();
        if (!parts.isEmpty()) {
            return parts;
        }
        RingArena ring = piece.ring().orElse(null);
        List<Integer> turrets = new ArrayList<>();
        for (int p = 0; ring != null && p < piece.partCount(); p++) {
            if (ring.battery(p) >= 0) {
                turrets.add(p);
            }
        }
        return turrets;
    }

    record Fight(long seed, boolean won, Watch watch) {}

    static Fight fight(long seed, Difficulty difficulty) {
        Sortie sortie = LevelMetrics.sortie(
                CONTENT, Level49Test.LEVEL, difficulty, Level49Test.planLoadout(CONTENT, difficulty), seed);
        Watch watch = new Watch();
        boolean won = LevelMetrics.fly(sortie, Level49Test.MAX_STEPS, watch);
        return new Fight(seed, won, watch);
    }

    static List<Fight> fights(Difficulty difficulty) {
        return LongStream.of(SEEDS)
                .parallel()
                .mapToObj(seed -> fight(seed, difficulty))
                .toList();
    }

    @Test
    void everyEchoLastsItsWindowAtMedium() {
        List<String> out = new ArrayList<>();
        List<String> quiet = new ArrayList<>();
        List<String> recipe = new ArrayList<>();
        double rush = 0;
        double[] mean = new double[3];
        for (Fight fight : fights(Difficulty.MEDIUM)) {
            String at = "medium seed " + fight.seed() + ":" + fight.watch();
            System.out.println(at);
            if (!fight.won()) {
                out.add("lost: " + at);
            }
            for (int f = 0; f < 3; f++) {
                Watch watch = fight.watch();
                double seconds = watch.seconds(f);
                mean[f] += seconds / SEEDS.length;
                if (!(seconds >= LEAST && seconds <= MOST)) {
                    out.add(NAMES[f] + " " + at);
                }
                if (watch.quiet[f] * SimStep.SECONDS > QUIET) {
                    quiet.add(NAMES[f] + " " + at);
                }
                // step G5: the recipe
                if (watch.fromDead[f] > 0 || watch.early[f]) {
                    recipe.add(NAMES[f] + ": a dead subtarget fired or stage 2 came early, seed " + fight.seed());
                }
                if (watch.entry[f] < ENTRY_SHOTS) {
                    recipe.add(NAMES[f] + ": entry shots " + watch.entry[f] + ", seed " + fight.seed());
                }
                if (!(watch.shotsPerMinute(f) >= SHOTS_FLOOR)) {
                    recipe.add(NAMES[f] + ": shots a minute " + watch.shotsPerMinute(f) + ", seed " + fight.seed());
                }
            }
            rush += fight.watch().rush();
        }
        System.out.printf(
                Locale.ROOT,
                "medium means %.1f / %.1f / %.1f s, the rush %.1f s%n",
                mean[0],
                mean[1],
                mean[2],
                rush / SEEDS.length);
        assertTrue(recipe.isEmpty(), "the boss recipe: " + recipe);
        assertTrue(Math.abs(rush / SEEDS.length - Level49Test.PAR) <= 10, "the rush about its par");
        assertTrue(out.isEmpty(), "every echo 40-60 s: " + out);
        assertTrue(quiet.isEmpty(), "never quiet over 1 s: " + quiet);
    }

    @Test
    void easyAndHardAreReported() {
        for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
            for (Fight fight : fights(difficulty)) {
                String at = difficulty + " seed " + fight.seed() + ":" + fight.watch();
                System.out.println(at);
                assertTrue(fight.won(), at);
            }
        }
    }

    /** It stands still for at most this long, s. */
    static final double STILL_MOST = 40;

    /**
     * Step G5 (round 81's rule, round 93 L.3's pressure): the autopilot flies Level 49 (retried at the checkpoints) until
     * echo f engages; then the ship flies back to its start place and stands there, its guns firing (and Rook's: a stage
     * may end under them, or the echo die): at medium it loses armour on every seed; the time to its armour's loss (a
     * retry) and the shield and armour lost a second are printed (the recipe's ≈ 15 s in stage 1, the Chimera's ≈ 14 s).
     */
    @Test
    void standingStillLosesTheArmour() {
        List<String> out = new ArrayList<>();
        for (int f = 0; f < 3; f++) {
            for (long seed : new long[] {1, 2, 3}) {
                double[] still = stillShip(f, seed);
                String at = String.format(
                        Locale.ROOT,
                        "%s still seed %d: armour gone in %.1f s (stage 1 for %.1f s), lost a second %.1f, armour lost %.0f",
                        NAMES[f],
                        seed,
                        still[0],
                        still[3],
                        still[1],
                        still[2]);
                System.out.println(at);
                if (!(still[2] > 0)) {
                    out.add(at);
                }
            }
        }
        assertTrue(out.isEmpty(), "standing still costs armour: " + out);
    }

    /**
     * {s to the armour's loss (∞ if kept or the echo died first), shield and armour lost a second, armour lost, s of
     * stage 1 stood} of a ship standing still against echo f.
     */
    static double[] stillShip(int f, long seed) {
        Sortie sortie = LevelMetrics.sortie(
                CONTENT,
                Level49Test.LEVEL,
                Difficulty.MEDIUM,
                Level49Test.planLoadout(CONTENT, Difficulty.MEDIUM),
                seed);
        double homeX = PlayField.WIDTH / 2.0;
        double homeY = sortie.ship().y();
        SetPiece boss = null;
        int steps = 0;
        while (!sortie.complete() && steps++ < Level49Test.MAX_STEPS) {
            Level04Test.step(sortie);
            if (sortie.bosses().rush() && sortie.bosses().fights() > f) {
                SetPiece piece = sortie.setPiece(sortie.bosses().fightPiece(f));
                if (piece.present() && piece.engaged() && !piece.destroyed()) {
                    boss = piece;
                    break;
                }
            }
        }
        if (boss == null) {
            throw new AssertionError(NAMES[f] + " engages, seed " + seed);
        }
        ToIntFunction<Sortie> home = s -> {
            double dx = homeX - s.ship().x();
            double dy = homeY - s.ship().y();
            int c = Command.FIRE.bit();
            c |= dx > 2 ? Command.RIGHT.bit() : dx < -2 ? Command.LEFT.bit() : 0;
            c |= dy > 2 ? Command.UP.bit() : dy < -2 ? Command.DOWN.bit() : 0;
            return c;
        };
        double lost = 0;
        double armour = 0;
        int stood = 0;
        int stage1 = -1;
        int attempt = sortie.attempt();
        while (stood < SimStep.ticks(STILL_MOST) && !boss.destroyed()) {
            double a = sortie.ship().defences().armour();
            double before = a + sortie.ship().defences().shield();
            sortie.step(home.applyAsInt(sortie));
            stood++;
            if (stage1 < 0 && boss.phase() > 0) {
                stage1 = stood;
            }
            if (!sortie.flying()
                    || sortie.attempt() != attempt
                    || sortie.ship().defences().armour() <= 0) {
                return new double[] {
                    stood * SimStep.SECONDS,
                    (lost + before) / (stood * SimStep.SECONDS),
                    armour + a,
                    (stage1 < 0 ? stood : stage1) * SimStep.SECONDS
                };
            }
            armour += Math.max(0, a - sortie.ship().defences().armour());
            lost += Math.max(
                    0,
                    before
                            - sortie.ship().defences().armour()
                            - sortie.ship().defences().shield());
        }
        return new double[] {
            Double.POSITIVE_INFINITY,
            lost / (stood * SimStep.SECONDS),
            armour,
            (stage1 < 0 ? stood : stage1) * SimStep.SECONDS
        };
    }
}
