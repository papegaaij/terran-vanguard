package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M10 part F, step F1b: the Honour Guard's playbook and conductor ({@link AcePlaybook}; design/enemies/bosses/
 * honour-guard, The playbook) on {@link Act6FSpecs}'s played guards: each ace draws from its own pool, never the same
 * play twice running, each told 0.8 s ahead (easy's dives 1.0 s); the conductor's one told play, two firing aces and
 * one tell at a time; the pincer's spacing and the leader holding fire; the rests by company and by the deaths; the
 * survivors' edges and the last ace's borrowed signatures; a replay and a retry draw the same; no allocation.
 */
class AcePlaybookTest {
    private static final int H = Act6FSpecs.HARKER;
    private static final int V = Act6FSpecs.VESPER;
    private static final int K = Act6FSpecs.KESTREL;
    private static final SetPieceSpec[] DIFFICULTIES = {
        Act6FSpecs.PLAYED_EASY, Act6FSpecs.PLAYED, Act6FSpecs.PLAYED_HARD
    };

    /** One logged play: its ace, code, its tell's and its run's ticks, the ticks its rest began before it. */
    record Logged(int ace, int code, int tellAt, int runAt, int company, int deaths) {}

    /** Steps {@code rig} {@code seconds}, killing ace {@code kills[i][0]} at boss second {@code kills[i][1]}. */
    static List<Logged> fly(Act6FSpecs.Rig rig, double seconds, double[][] kills, Check check) {
        List<Logged> log = new ArrayList<>();
        AcePlaybook book = rig.squadron.playbook().orElseThrow();
        int[] open = {-1, -1, -1};
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            for (double[] kill : kills) {
                if (rig.piece.bossTicks() == SimStep.ticks(kill[1]) && rig.piece.partHp((int) kill[0]) > 0) {
                    Act6FSpecs.kill(rig.piece, (int) kill[0]);
                }
            }
            rig.step();
            for (int v : rig.values(SimEvents.Type.ACE_PLAY_TELL)) {
                int a = AcePlaybook.playAce(v);
                log.add(new Logged(
                        a, AcePlaybook.playCode(v), rig.tick, -1, rig.squadron.flyingCount(), rig.squadron.deaths()));
                open[a] = log.size() - 1;
            }
            for (int v : rig.values(SimEvents.Type.ACE_PLAY)) {
                int a = AcePlaybook.playAce(v);
                Logged told = log.get(open[a]);
                log.set(open[a], new Logged(a, told.code(), told.tellAt(), rig.tick, told.company(), told.deaths()));
            }
            if (check != null) {
                check.at(rig, book);
            }
        }
        return log;
    }

    interface Check {
        void at(Act6FSpecs.Rig rig, AcePlaybook book);
    }

    private static Set<SquadronPlaybook.Kind> own(int ace) {
        return switch (ace) {
            case H ->
                EnumSet.of(
                        SquadronPlaybook.Kind.SNIPER_LINE,
                        SquadronPlaybook.Kind.PAIR_VOLLEY,
                        SquadronPlaybook.Kind.FAN,
                        SquadronPlaybook.Kind.SLIDE);
            case V ->
                EnumSet.of(
                        SquadronPlaybook.Kind.SPIRAL_LOOP,
                        SquadronPlaybook.Kind.ROLL_FAN,
                        SquadronPlaybook.Kind.RIBBON);
            default ->
                EnumSet.of(
                        SquadronPlaybook.Kind.FEINT_DIVE,
                        SquadronPlaybook.Kind.REAR_RAKE,
                        SquadronPlaybook.Kind.OVERHEAD);
        };
    }

    @Test
    void eachAceDrawsFromItsOwnPoolNeverTheSameTwiceRunningEachToldAhead() {
        for (int d = 0; d < DIFFICULTIES.length; d++) {
            Set<Integer> seen = new java.util.HashSet<>();
            for (long seed = 1; seed <= 5; seed++) {
                Act6FSpecs.Rig rig = new Act6FSpecs.Rig(DIFFICULTIES[d], seed);
                // Step F6s3: the pincer is drawn only with its lanes under the leader's band (the fixture's 370-470:
                // the
                // ship at most 90 px up, 70 on easy with Kestrel's 130 px keep-off).
                rig.shipY = 60;
                List<Logged> log = fly(rig, 100, new double[0][], null);
                int[] last = {-1, -1, -1};
                for (Logged play : log) {
                    assertNotEquals(last[play.ace()], play.code(), "never the same play twice running: " + play);
                    last[play.ace()] = play.code();
                    seen.add(play.code());
                    if (play.code() == AcePlaybook.PINCER) {
                        assertTrue(play.ace() == V || play.ace() == K, "a flanker's pincer");
                    } else {
                        SquadronPlaybook.Kind kind = SquadronPlaybook.Kind.values()[play.code()];
                        assertTrue(own(play.ace()).contains(kind), "its own pool: " + play);
                        if (play.runAt() >= 0) {
                            boolean dive =
                                    kind == SquadronPlaybook.Kind.FEINT_DIVE || kind == SquadronPlaybook.Kind.OVERHEAD;
                            double tell = dive && d == 0 ? 1.0 : 0.8;
                            assertEquals(SimStep.ticks(tell), play.runAt() - play.tellAt(), "told ahead: " + play);
                        }
                        if (kind == SquadronPlaybook.Kind.SLIDE) {
                            assertTrue(play.company() >= 2, "the slide only with company");
                        }
                    }
                }
            }
            assertEquals(
                    SquadronPlaybook.Kind.values().length + 1, seen.size(), "every play and the pincer drawn: " + d);
        }
    }

    @Test
    void theConductorKeepsOneToldPlayTwoFiringAcesAndOneTellAtATime() {
        int pincers = 0;
        for (SetPieceSpec spec : DIFFICULTIES) {
            for (long seed = 1; seed <= 5; seed++) {
                Act6FSpecs.Rig rig = new Act6FSpecs.Rig(spec, seed);
                List<Integer> pincerDraws = new ArrayList<>();
                fly(rig, 110, new double[][] {{V, 70}, {H, 90}}, (r, book) -> {
                    assertTrue(book.told() <= 1, "one told line or dive at a time");
                    assertTrue(book.firing() <= 2, "two aces firing at most");
                    int telling = book.telling();
                    boolean pincerTell = book.pincer(V) && book.pincer(K);
                    assertTrue(telling <= 1 || (telling == 2 && pincerTell), "one tell at a time (the pincer's pair)");
                    if (book.holdingFire()) {
                        SquadronPlaybook.Kind harker = book.kind(H);
                        assertTrue(harker == null || !harker.fires(), "the leader holds fire through the pincer");
                    }
                    for (int v : r.values(SimEvents.Type.ACE_PLAY_TELL)) {
                        if (AcePlaybook.playCode(v) == AcePlaybook.PINCER && AcePlaybook.playAce(v) == V) {
                            pincerDraws.add(book.draws());
                            for (int a = 0; a < 3; a++) {
                                assertTrue(r.squadron.touchable(a), "the pincer only with all three flying");
                            }
                        }
                    }
                });
                for (int i = 1; i < pincerDraws.size(); i++) {
                    assertTrue(
                            pincerDraws.get(i) - pincerDraws.get(i - 1) >= 4,
                            "the pincer at most every fourth draw: " + pincerDraws);
                }
                pincers += pincerDraws.size();
            }
        }
        assertTrue(pincers > 0, "the pincer is flown");
    }

    @Test
    void theRestsFollowTheCompanyAndShortenWithTheDeaths() {
        for (long seed = 1; seed <= 5; seed++) {
            Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, seed);
            AcePlaybook book = rig.squadron.playbook().orElseThrow();
            int[] restFrom = {-1, -1, -1};
            AcePlaybook.Mode[] was = {AcePlaybook.Mode.REST, AcePlaybook.Mode.REST, AcePlaybook.Mode.REST};
            double[] least = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
            double[][] kills = {{V, 60}, {K, 80}};
            int[] regime = new int[3];
            for (int i = 0; i < SimStep.ticks(110); i++) {
                for (double[] kill : kills) {
                    if (rig.piece.bossTicks() == SimStep.ticks(kill[1])) {
                        Act6FSpecs.kill(rig.piece, (int) kill[0]);
                    }
                }
                rig.step();
                for (int a = 0; a < 3; a++) {
                    AcePlaybook.Mode now = book.mode(a);
                    if (was[a] == AcePlaybook.Mode.RUN && now == AcePlaybook.Mode.REST) {
                        restFrom[a] = rig.tick;
                        int deaths = rig.squadron.deaths();
                        regime[a] = deaths == 2 ? 3 : deaths == 1 ? 2 : rig.squadron.flyingCount() >= 2 ? 1 : 0;
                    }
                    if (was[a] == AcePlaybook.Mode.REST
                            && now == AcePlaybook.Mode.TELL
                            && restFrom[a] >= 0
                            && !book.pincer(a)) {
                        // (A partner's pincer cuts its rest short: the team move pulls it in.)
                        double rest = (rig.tick - restFrom[a]) * SimStep.SECONDS;
                        double[] expected = {1.2, 2.0, 2.0 * 0.7, 1.2 * 0.5};
                        assertTrue(
                                rest >= expected[regime[a]] - SimStep.SECONDS,
                                "a rest of " + rest + " s, at least " + expected[regime[a]] + " (" + regime[a]
                                        + ") ace "
                                        + a + " at " + rig.piece.bossSeconds() + " seed " + seed + " flying "
                                        + rig.squadron.flyingCount());
                        least[regime[a]] = Math.min(least[regime[a]], rest);
                    }
                    was[a] = now;
                }
            }
            assertEquals(1.2, least[0], 2.5 * SimStep.SECONDS, "alone: 1.2 s");
            assertEquals(0.6, least[3], 2.5 * SimStep.SECONDS, "the last ace: −50 %");
        }
    }

    @Test
    void eachDeathSharpensTheSurvivorsAndTheLastAceBorrowsTheDeadAcesSignatures() {
        int tapsBefore = 0;
        int tapsAfter = 0;
        int burstAfter = 0;
        int armsAfter = 0;
        Set<SquadronPlaybook.Kind> lastDrew = EnumSet.noneOf(SquadronPlaybook.Kind.class);
        for (long seed = 1; seed <= 5; seed++) {
            // Vesper dies first: Harker's triple tap, Kestrel's committed burst of 5; then Harker: Kestrel alone.
            Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, seed);
            AcePlaybook book = rig.squadron.playbook().orElseThrow();
            int[] lines = new int[1];
            int[] shots = new int[1];
            int start = 0;
            for (int i = 0; i < SimStep.ticks(150); i++) {
                int t = rig.piece.bossTicks();
                if (t == SimStep.ticks(60)) {
                    Act6FSpecs.kill(rig.piece, V);
                }
                if (t == SimStep.ticks(100)) {
                    Act6FSpecs.kill(rig.piece, H);
                }
                SquadronPlaybook.Kind before = book.kind(H);
                SquadronPlaybook.Kind kestrel = book.kind(K);
                start = rig.shots.size();
                rig.step();
                lines[0] += rig.values(SimEvents.Type.BOSS_LINE_FIRED).size();
                if (before == SquadronPlaybook.Kind.SNIPER_LINE && book.kind(H) != SquadronPlaybook.Kind.SNIPER_LINE) {
                    // A sniper play just ended: its lines.
                    if (rig.squadron.deaths() == 0) {
                        tapsBefore = Math.max(tapsBefore, lines[0]);
                    } else if (rig.squadron.deaths() == 1) {
                        tapsAfter = Math.max(tapsAfter, lines[0]);
                    }
                }
                if (book.kind(H) != SquadronPlaybook.Kind.SNIPER_LINE) {
                    lines[0] = 0;
                }
                if (kestrel == SquadronPlaybook.Kind.FEINT_DIVE && rig.squadron.deaths() == 1) {
                    shots[0] += from(rig, start, K, 0);
                    if (book.kind(K) != SquadronPlaybook.Kind.FEINT_DIVE) {
                        burstAfter = Math.max(burstAfter, shots[0]);
                        shots[0] = 0;
                    }
                } else {
                    shots[0] = 0;
                }
                if (rig.squadron.deaths() == 2) {
                    for (int v : rig.values(SimEvents.Type.ACE_PLAY_TELL)) {
                        lastDrew.add(SquadronPlaybook.Kind.values()[AcePlaybook.playCode(v)]);
                    }
                }
            }
            // Harker dies first: Vesper's spiral turns aggressive (3 arms).
            Act6FSpecs.Rig second = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, seed);
            AcePlaybook secondBook = second.squadron.playbook().orElseThrow();
            for (int i = 0; i < SimStep.ticks(120); i++) {
                if (second.piece.bossTicks() == SimStep.ticks(40)) {
                    Act6FSpecs.kill(second.piece, H);
                }
                int from = second.shots.size();
                second.step();
                if (secondBook.kind(V) == SquadronPlaybook.Kind.SPIRAL_LOOP && second.squadron.deaths() == 1) {
                    armsAfter = Math.max(armsAfter, from(second, from, V, 1));
                }
            }
        }
        assertEquals(2, tapsBefore, "medium: the double tap");
        assertEquals(3, tapsAfter, "after a death: the triple");
        assertEquals(5, burstAfter, "Kestrel commits: a burst of 5");
        assertEquals(3, armsAfter, "Vesper aggressive: 3 arms");
        assertTrue(
                lastDrew.contains(SquadronPlaybook.Kind.SNIPER_LINE)
                        && lastDrew.contains(SquadronPlaybook.Kind.SPIRAL_LOOP),
                "the last ace borrows the dead aces' line and loop: " + lastDrew);
        assertTrue(!lastDrew.contains(SquadronPlaybook.Kind.PAIR_VOLLEY), "only their signatures: " + lastDrew);
    }

    /** The shots of {@code type} (0 aimed, 1 bullet) since {@code start} fired from ace {@code a}'s place now. */
    static int from(Act6FSpecs.Rig rig, int start, int a, int type) {
        int n = 0;
        for (int i = start; i < rig.shots.size(); i++) {
            double[] shot = rig.shots.get(i);
            n += shot[1] == type && shot[2] == rig.squadron.x(a) && shot[3] == rig.squadron.y(a) ? 1 : 0;
        }
        return n;
    }

    @Test
    void theRadioHearsTheJoinsTheLossesAndTheLastAce() {
        Act6FSpecs.Rig rig = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 2);
        fly(rig, 120, new double[][] {{V, 60}, {H, 80}}, null);
        assertEquals(
                List.of(
                        "vesper-joins",
                        "kestrel-joins",
                        "vesper-down",
                        "first-loss-harker",
                        "harker-down",
                        "last-ace-kestrel"),
                rig.cues,
                "the subjects in order");
        Act6FSpecs.Rig early = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 2);
        fly(early, 30, new double[][] {{H, 10}}, null);
        assertEquals(List.of("harker-down"), early.cues.subList(0, 1), "Harker alone: nobody speaks the first loss");
        assertTrue(!early.cues.contains("first-loss-vesper"), "an ace not across its edge never speaks it");
    }

    @Test
    void aReplayDrawsTheSameAndAnotherSeedDrawsOtherwise() {
        Act6FSpecs.Rig one = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 7);
        Act6FSpecs.Rig two = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 7);
        Act6FSpecs.Rig other = new Act6FSpecs.Rig(Act6FSpecs.PLAYED, 8);
        List<Logged> a = fly(one, 90, new double[][] {{H, 50}}, null);
        List<Logged> b = fly(two, 90, new double[][] {{H, 50}}, null);
        List<Logged> c = fly(other, 90, new double[][] {{H, 50}}, null);
        assertEquals(a, b, "a replay draws the same");
        assertEquals(SquadronTest.pieceHash(one.piece), SquadronTest.pieceHash(two.piece), "and hashes the same");
        assertNotEquals(a, c, "another seed draws otherwise");
    }

    @Test
    void aRetryFromTheCheckpointRepeatsThePlayedDuelExactly() {
        Sortie sortie = Act6FSpecs.sortie(Act6FSpecs.PLAYED, TestSpecs.RULES.withInvulnerableShip());
        SetPiece boss = Act6FSpecs.boss(sortie);
        MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, Act6FSpecs.ARRIVE + 0.1, 0);
        int length = SimStep.ticks(50);
        List<Long> first = new ArrayList<>();
        while (boss.bossTicks() < length) {
            int t = boss.bossTicks();
            sortie.step(weave(t));
            first.add(SquadronTest.pieceHash(boss));
        }
        assertTrue(boss.squadron().orElseThrow().playbook().orElseThrow().draws() > 5, "plays were drawn");
        sortie.retryFromBoss();
        sortie.step(0);
        while (boss.bossTicks() < 0) {
            sortie.step(0);
        }
        List<Long> second = new ArrayList<>();
        while (boss.bossTicks() < length) {
            int t = boss.bossTicks();
            sortie.step(weave(t));
            second.add(SquadronTest.pieceHash(boss));
        }
        assertEquals(first, second, "the retry flies and draws the duel exactly again");
    }

    private static int weave(int t) {
        return Command.FIRE.bit() | ((t / 50) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
    }

    @Test
    void aPlayedDuelStepsWithoutAllocating() {
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = Act6FSpecs.sortie(Act6FSpecs.PLAYED, TestSpecs.RULES.withInvulnerableShip());
                    MawSpecs.until(sortie, SimEvents.Type.BOSS_ARRIVED, Act6FSpecs.ARRIVE + 0.1, 0);
                    return sortie;
                },
                sortie -> {
                    SetPiece boss = Act6FSpecs.boss(sortie);
                    for (int i = 0; i < SimStep.ticks(75); i++) {
                        sortie.step(weave(i));
                        if (i == SimStep.ticks(40)) {
                            Act6FSpecs.kill(sortie, V);
                        }
                        if (i == SimStep.ticks(60)) {
                            boss.damagePartFlat(H, 2000);
                        }
                    }
                });
        assertEquals(0, allocated, "the draws, tells, lines, volleys, spirals, ribbons and pincers allocate nothing");
    }
}
