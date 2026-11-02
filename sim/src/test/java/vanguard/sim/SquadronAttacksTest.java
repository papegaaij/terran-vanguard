package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part F, step F1b: the Honour Guard's attacks ({@link AcePlaybook}; design/enemies/bosses/honour-guard, The
 * playbook) on {@link Act6FSpecs}'s played guards: the sniper line tracking the ship until 0.2 s before it fires
 * ({@link EnemySpec.LaserLine#lockBeforeSeconds}, {@link BossLines}) and its taps per difficulty; Harker's pair volleys
 * and fans; Vesper's spiral through her loop and her ribbon's eight bullets with two 50 px gaps; Kestrel's burst from
 * its pull-out; the pincer's two fans half a second apart.
 */
class SquadronAttacksTest {
    private static final int H = Act6FSpecs.HARKER;
    private static final int V = Act6FSpecs.VESPER;
    private static final int K = Act6FSpecs.KESTREL;

    /** Steps {@code rig} until ace {@code a} starts running {@code code} (at most {@code seconds}); false if never. */
    private static boolean untilPlay(Act6FSpecs.Rig rig, int a, int code, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            rig.step();
            for (int v : rig.values(SimEvents.Type.ACE_PLAY)) {
                if (AcePlaybook.playAce(v) == a && AcePlaybook.playCode(v) == code) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void aLineBeforeActSixLocksAtItsStartTheSnipersTracksUntilItsLock() {
        EnemySpec.LaserLine rail = new EnemySpec.LaserLine(1.0, 0.2, 8, 10, 4.5, 0.5, 100);
        assertFalse(rail.tracks(), "Level 35's rail lines lock at the telegraph's start");
        assertEquals(1.0, rail.lockBeforeSeconds());
        assertFalse(rail.withMostTelegraphing(1).tracks(), "kept through the gate's copy");
        assertTrue(Act6FSpecs.line(1.0).tracks());

        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 3);
        assertTrue(untilPlay(rig, H, SquadronPlaybook.Kind.SNIPER_LINE.ordinal(), 40), "a sniper line");
        BossLines lines = rig.piece.lines().orElseThrow();
        // The ship weaves sideways through the telegraph; the line follows it until 0.2 s before it fires.
        List<double[]> dirs = new ArrayList<>();
        double lockX = Double.NaN;
        double lockY = Double.NaN;
        int telegraph = SimStep.ticks(1.0);
        int lock = telegraph - SimStep.ticks(0.2);
        int started = -1;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            rig.shipX += 1.5;
            rig.step();
            if (started < 0 && !rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH).isEmpty()) {
                started = rig.tick;
            }
            if (started >= 0 && lines.telegraphing()) {
                int t = rig.tick - started;
                dirs.add(new double[] {t, lines.dirX(), lines.dirY()});
                if (t == lock - 1) {
                    lockX = rig.shipX;
                    lockY = rig.shipY;
                }
            }
            if (started >= 0 && lines.live()) {
                // At its live moment it passes through the ship's spot at the lock (its origin led).
                double dx = lockX - lines.fromX();
                double dy = lockY - lines.fromY();
                double off = Math.abs(dx * lines.dirY() - dy * lines.dirX());
                assertTrue(off < 3, "the line passes the ship's spot at its lock: " + off);
                break;
            }
        }
        assertTrue(started >= 0, "the line telegraphed");
        double[] first = dirs.get(0);
        double[] beforeLock = dirs.get(lock - 2);
        assertTrue(
                Math.abs(first[1] - beforeLock[1]) > 0.01, "it tracked the ship: " + first[1] + " → " + beforeLock[1]);
        for (double[] d : dirs) {
            if (d[0] >= lock) {
                assertEquals(dirs.get(lock - 1)[1], d[1], 1e-12, "locked from 0.2 s before it fires");
                assertEquals(dirs.get(lock - 1)[2], d[2], 1e-12);
            }
        }
    }

    /** The lines of one sniper play: their telegraph starts and fires, boss ticks. */
    private static List<int[]> sniperLines(SetPieceSpec spec, long seed) {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(spec, seed);
        assertTrue(untilPlay(rig, H, SquadronPlaybook.Kind.SNIPER_LINE.ordinal(), 40), "a sniper line");
        AcePlaybook book = rig.squadron.playbook().orElseThrow();
        List<int[]> lines = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(8) && book.kind(H) == SquadronPlaybook.Kind.SNIPER_LINE; i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.BOSS_LINE_TELEGRAPH).isEmpty()) {
                lines.add(new int[] {rig.tick, -1});
            }
            if (!rig.values(SimEvents.Type.BOSS_LINE_FIRED).isEmpty()) {
                lines.get(lines.size() - 1)[1] = rig.tick;
            }
        }
        return lines;
    }

    @Test
    void theDoubleTapIsReaimedAfterTheFirstLineEasyFiresOneHardThree() {
        List<int[]> medium = sniperLines(Act6FSpecs.PLAYED, 3);
        assertEquals(2, medium.size(), "medium: the line and its tap");
        assertEquals(SimStep.ticks(1.0), medium.get(0)[1] - medium.get(0)[0], "a 1.0 s line");
        assertEquals(SimStep.ticks(0.8), medium.get(1)[1] - medium.get(1)[0], "a 0.8 s tap");
        assertEquals(SimStep.ticks(0.6), medium.get(1)[0] - medium.get(0)[1], 1, "re-aimed 0.6 s after it fired");
        List<int[]> easy = sniperLines(Act6FSpecs.PLAYED_EASY, 3);
        assertEquals(1, easy.size(), "easy: a single line");
        assertEquals(SimStep.ticks(1.2), easy.get(0)[1] - easy.get(0)[0], "a 1.2 s line");
        List<int[]> hard = sniperLines(Act6FSpecs.PLAYED_HARD, 3);
        assertEquals(3, hard.size(), "hard: the triple");
        assertEquals(SimStep.ticks(0.8), hard.get(0)[1] - hard.get(0)[0], "a 0.8 s line");
    }

    /** The aimed shots ace {@code a} fires through its next {@code code} play: their ticks and fans. */
    private static List<double[]> volleys(SetPieceSpec spec, int a, int code, long seed, double within) {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(spec, seed);
        assertTrue(untilPlay(rig, a, code, within), "the play " + code);
        AcePlaybook book = rig.squadron.playbook().orElseThrow();
        List<double[]> shots = new ArrayList<>();
        for (int i = 0; i < SimStep.ticks(10) && book.play(a) == code; i++) {
            int from = rig.shots.size();
            rig.step();
            for (int s = from; s < rig.shots.size(); s++) {
                double[] shot = rig.shots.get(s);
                if (shot[2] == rig.squadron.x(a) && shot[3] == rig.squadron.y(a)) {
                    shots.add(shot);
                }
            }
        }
        return shots;
    }

    @Test
    void harkersPairVolleysAndFansFireOnTheirBeats() {
        List<double[]> pairs = volleys(Act6FSpecs.PLAYED, H, SquadronPlaybook.Kind.PAIR_VOLLEY.ordinal(), 5, 60);
        assertEquals(4, pairs.size(), "four pairs");
        for (int i = 1; i < pairs.size(); i++) {
            assertEquals(SimStep.ticks(0.4), pairs.get(i)[0] - pairs.get(i - 1)[0], 1e-9, "0.4 s apart");
            assertEquals(2, pairs.get(i)[7], "a pair");
        }
        List<double[]> fans = volleys(Act6FSpecs.PLAYED, H, SquadronPlaybook.Kind.FAN.ordinal(), 5, 60);
        assertEquals(2, fans.size(), "two fans");
        assertEquals(SimStep.ticks(0.5), fans.get(1)[0] - fans.get(0)[0], 1e-9, "0.5 s apart");
        assertEquals(5, fans.get(0)[7], "5-way");
    }

    @Test
    void vespersSpiralTurnsThroughHerLoopOnHerArms() {
        for (SetPieceSpec spec :
                new SetPieceSpec[] {Act6FSpecs.PLAYED_EASY, Act6FSpecs.PLAYED, Act6FSpecs.PLAYED_HARD}) {
            Act6FSpecs.Rig rig = new Act6FSpecs.Rig(spec, 4);
            assertTrue(untilPlay(rig, V, SquadronPlaybook.Kind.SPIRAL_LOOP.ordinal(), 90), "a spiral loop");
            int arms = spec == Act6FSpecs.PLAYED_EASY ? 1 : spec == Act6FSpecs.PLAYED ? 2 : 3;
            AcePlaybook book = rig.squadron.playbook().orElseThrow();
            List<double[]> volley = new ArrayList<>();
            int volleys = 0;
            double lastAngle = Double.NaN;
            int lastTick = -1;
            while (book.kind(V) == SquadronPlaybook.Kind.SPIRAL_LOOP) {
                int from = rig.shots.size();
                rig.step();
                volley.clear();
                for (int s = from; s < rig.shots.size(); s++) {
                    double[] shot = rig.shots.get(s);
                    if (shot[1] == 1 && shot[2] == rig.squadron.x(V) && shot[3] == rig.squadron.y(V)) {
                        volley.add(shot);
                    }
                }
                if (volley.isEmpty()) {
                    continue;
                }
                volleys++;
                assertEquals(arms, volley.size(), "its arms");
                assertEquals(130, volley.get(0)[5], 1e-9, "130 px/s");
                if (arms > 1) {
                    double step = ((volley.get(1)[4] - volley.get(0)[4]) % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI);
                    assertEquals(2 * Math.PI / arms, step, 1e-9, "arms evenly apart");
                }
                if (lastTick >= 0) {
                    assertEquals(SimStep.ticks(0.25), rig.tick - lastTick, "4 bullets a second an arm");
                    double turned = Math.IEEEremainder(volley.get(0)[4] - lastAngle, 2 * Math.PI);
                    assertEquals(Math.PI / 2 * 0.25, turned, 1e-9, "the arms turn 90°/s");
                }
                lastTick = rig.tick;
                lastAngle = volley.get(0)[4];
            }
            assertTrue(volleys >= 9 && volleys <= 11, "through her 2.5 s loop: " + volleys);
        }
    }

    @Test
    void theRibbonLaysEightSlowBulletsWithTwoFiftyPixelGaps() {
        for (long seed = 1; seed <= 5; seed++) {
            Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, seed);
            assertTrue(untilPlay(rig, V, SquadronPlaybook.Kind.RIBBON.ordinal(), 120), "a ribbon");
            AcePlaybook book = rig.squadron.playbook().orElseThrow();
            int from = rig.shots.size();
            while (book.kind(V) == SquadronPlaybook.Kind.RIBBON) {
                rig.step();
            }
            List<Double> xs = new ArrayList<>();
            double y = Double.NaN;
            for (int s = from; s < rig.shots.size(); s++) {
                double[] shot = rig.shots.get(s);
                if (shot[1] == 1 && shot[5] == 80) {
                    assertEquals(-Math.PI / 2, shot[4], 1e-12, "straight down");
                    assertEquals(4, shot[6], 1e-12, "small");
                    xs.add(shot[2]);
                    y = Double.isNaN(y) ? shot[3] : y;
                }
            }
            assertEquals(8, xs.size(), "eight bullets");
            xs.sort(Double::compare);
            double span = PlayField.WIDTH - 2 * AceFlight.RIBBON_INSET;
            double spacing = (span - 2 * 50) / 7;
            int gaps = 0;
            for (int i = 1; i < xs.size(); i++) {
                double d = xs.get(i) - xs.get(i - 1);
                if (Math.abs(d - spacing - 50) < 1e-6) {
                    gaps++;
                } else {
                    assertEquals(spacing, d, 1e-6, "even spacing");
                }
            }
            assertEquals(2, gaps, "two 50 px gaps");
            assertEquals(AceFlight.RIBBON_INSET, xs.get(0), 1e-6, "across the pass");
            // (step R79b2: inside her band, the pass's height bent at most its slack by her keep-off over the ship)
            SquadronSpec.Station band = rig.squadron.spec().aces().get(V).station();
            assertTrue(y >= band.yMin() - 20 && y <= band.yMax() + 20, "laid along the pass: " + y);
        }
    }

    @Test
    void kestrelBurstsFromItsPullOutAimedAtTheShip() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 2);
        assertTrue(untilPlay(rig, K, SquadronPlaybook.Kind.FEINT_DIVE.ordinal(), 120), "a dive");
        AcePlaybook book = rig.squadron.playbook().orElseThrow();
        List<double[]> burst = new ArrayList<>();
        AceLeg[] legs = new AceLeg[64];
        double least = Double.MAX_VALUE;
        while (book.kind(K) == SquadronPlaybook.Kind.FEINT_DIVE) {
            int from = rig.shots.size();
            rig.step();
            least = Math.min(least, Math.hypot(rig.squadron.x(K) - rig.shipX, rig.squadron.y(K) - rig.shipY));
            for (int s = from; s < rig.shots.size(); s++) {
                double[] shot = rig.shots.get(s);
                if (shot[2] == rig.squadron.x(K) && shot[3] == rig.squadron.y(K)) {
                    legs[burst.size()] = rig.squadron.leg(K);
                    burst.add(shot);
                }
            }
        }
        assertEquals(3, burst.size(), "a burst of 3");
        assertEquals(AceLeg.PULL_OUT, legs[0], "from the pull-out");
        assertEquals(6, burst.get(0)[6], 1e-12, "medium");
        assertTrue(least >= 110 - 1, "pulled out at 110 px: " + least);
    }

    @Test
    void thePincersFlankersFireTheirFansHalfASecondApartWhileHarkerHoldsFire() {
        // Step R79b2: over eight seeds (a rate, not one seed's pincer): most pincers fire both fans, every pair half a
        // second apart, Harker holding fire.
        int seen = 0;
        int both = 0;
        for (long seed = 1; seed <= 8; seed++) {
            Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, seed);
            // Step F6s3: the pincer is drawn only with its lanes under the leader's band (the ship low enough).
            rig.shipY = 80;
            if (!untilPlay(rig, V, AcePlaybook.PINCER, 140)) {
                continue;
            }
            seen++;
            AcePlaybook book = rig.squadron.playbook().orElseThrow();
            int[] fanAt = {-1, -1};
            while (book.pincer(V) || book.pincer(K)) {
                int from = rig.shots.size();
                rig.step();
                for (int s = from; s < rig.shots.size(); s++) {
                    double[] shot = rig.shots.get(s);
                    for (int f = 0; f < 2; f++) {
                        int a = f == 0 ? V : K;
                        if (shot[2] == rig.squadron.x(a) && shot[3] == rig.squadron.y(a)) {
                            fanAt[f] = rig.tick;
                            assertEquals(5, shot[7], "a fan");
                        }
                    }
                    assertFalse(shot[2] == rig.squadron.x(H) && shot[3] == rig.squadron.y(H), "Harker holds fire");
                }
            }
            if (fanAt[0] >= 0 && fanAt[1] >= 0) {
                both++;
                assertEquals(SimStep.ticks(0.5), fanAt[1] - fanAt[0], "half a second apart, seed " + seed);
            }
        }
        assertTrue(seen >= 3, "pincers flown: " + seen);
        assertTrue(both >= 0.75 * seen, "both fans fired: " + both + " of " + seen);
    }
}
