package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part C, the Threadcrawler (design/enemies/ground/threadcrawler): a segment chain on the ground. Its head
 * crawls an authored ground path at its speed on top of the scroll and the body follows on the history in
 * ground space; its segments' pores fire a travelling wave head to tail, each after its glow, then rest; a cut
 * stops the rear part, which dies from the cut backwards after 1.0 s and never regrows; a head-first kill pays
 * the whole and the tail first its bonus; it leaves once off the bottom edge; the Coilwyrm stays an air chain
 * (risk 5); nothing allocates.
 */
class ThreadcrawlerTest {
    private static final int FIRE = Command.FIRE.bit();
    static final EnemySpec.PoreWave PORES = new EnemySpec.PoreWave(0.25, 2.0, 0.3, 110, 4, false);

    private static EnemySpec part(String slug, double hp, Hitbox box, int bounty) {
        return EnemySpec.of(slug, hp, box, Layer.GROUND, 6, false, bounty, 0);
    }

    /** The member offsets along the path: head 56, segments 32, tail 40 px long, 0.8 × the mean apart. */
    static List<Double> offsets(int segments) {
        List<Double> lengths = new ArrayList<>(List.of(56.0));
        for (int i = 0; i < segments; i++) {
            lengths.add(32.0);
        }
        lengths.add(40.0);
        List<Double> offsets = new ArrayList<>(List.of(0.0));
        for (int i = 1; i < lengths.size(); i++) {
            offsets.add(offsets.get(i - 1) + 0.8 * (lengths.get(i - 1) + lengths.get(i)) / 2);
        }
        return offsets;
    }

    /** A Threadcrawler of {@code segments} crawling (xs[i], rows[i]) at 70 px/s with {@code pores}. */
    static EnemySpec crawler(int segments, EnemySpec.PoreWave pores, double[] rows, double[] xs) {
        List<Hitbox> boxes = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            boxes.add(new Hitbox(36, 24));
        }
        EnemySpec.ChainSpec chain = new EnemySpec.ChainSpec(
                part("threadcrawler-segment", 8, boxes.getFirst(), 2),
                boxes,
                part("threadcrawler-tail", 20, new Hitbox(28, 28), 2),
                4,
                null,
                0,
                0,
                offsets(segments),
                0.08,
                1,
                18);
        EnemySpec head = part("threadcrawler", 80, new Hitbox(40, 40), 30);
        return EnemySpec.of(
                        head.slug(),
                        head.hp(),
                        head.hitbox(),
                        head.layer(),
                        head.contactDamage(),
                        head.destroyedByRamming(),
                        head.bounty(),
                        head.speed())
                .withChain(Optional.of(chain))
                .withCrawl(Optional.of(new EnemySpec.Crawl(70, rows, xs, pores)));
    }

    static LevelScript level(double speed, EnemySpec crawler) {
        return LevelScript.builder(1, 3)
                .sections(List.of(new Section(60, speed)))
                .waves(List.of(TestSpecs.wave(
                        0.5, WaveSpec.Formation.SNAKE, crawler, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    /**
     * At 80 px/s a slow crawler (10 px/s down the screen) along x 100 whose head is at y 500 as it enters
     * (row 0 at t = 0.5 s), its whole body on the screen.
     */
    private static Sortie slow(EnemySpec.PoreWave pores, int segments) {
        EnemySpec spec = crawler(segments, pores, new double[] {0, 5_000}, new double[] {100, 100});
        return new Sortie(
                1, TestSpecs.LOADOUT, level(80, spec), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static int stepUntilChain(Sortie sortie) {
        int step = 0;
        while (sortie.chainCount() == 0) {
            sortie.step(Command.NONE);
            step++;
        }
        return step;
    }

    private static int member(Sortie sortie, int link) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).chain() != null && sortie.enemy(i).link() == link) {
                return i;
            }
        }
        return -1;
    }

    /** The distance of (x, row) from the polyline through (xs[i], rows[i]), its first leg extended backward. */
    private static double offPath(double x, double row, double[] xs, double[] rows) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i + 1 < xs.length; i++) {
            double dx = xs[i + 1] - xs[i];
            double dr = rows[i + 1] - rows[i];
            double t = ((x - xs[i]) * dx + (row - rows[i]) * dr) / (dx * dx + dr * dr);
            t = i == 0 ? Math.min(t, 1) : Math.clamp(t, 0, 1);
            double px = xs[i] + dx * t;
            double pr = rows[i] + dr * t;
            best = Math.min(best, Math.hypot(x - px, row - pr));
        }
        return best;
    }

    @Test
    void theHeadCrawlsItsGroundPathAndTheBodyFollowsItInGroundSpace() {
        // The head at the top edge as it enters (row 85 at t = 0.5 s), up-canyon to a bend at row 235. The
        // tail is the lowest member on the screen (it entered first and leaves first).
        double[] rows = {85, 235, 985};
        double[] xs = {120, 120, 320};
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT,
                level(130, crawler(12, PORES, rows, xs)),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        int spawned = stepUntilChain(sortie);
        Chain chain = sortie.chain(0);
        assertTrue(chain.ground());
        double headY = Double.NaN;
        for (int step = 1; step < SimStep.ticks(8); step++) {
            sortie.step(Command.NONE);
            double crawled = 70 * SimStep.SECONDS * (step + 1);
            Enemy head = sortie.enemy(member(sortie, 0));
            double row = Terrain.row(sortie.groundScroll(), head.y());
            if (crawled < 150) {
                assertEquals(120, head.x(), 1e-9);
                assertEquals(85 + crawled, row, 1e-6, "the head crawls 70 px/s along its path");
                if (!Double.isNaN(headY)) {
                    assertEquals((130 - 70) * SimStep.SECONDS, headY - head.y(), 1e-6, "on screen: the scroll less 70");
                }
                headY = head.y();
            }
            for (int i = 0; i < chain.size(); i++) {
                Enemy member = chain.member(i);
                assertSame(Layer.GROUND, member.layer());
                double off = offPath(member.x(), Terrain.row(sortie.groundScroll(), member.y()), xs, rows);
                assertTrue(off < 0.6, "member " + i + " on the path in ground space: " + off);
            }
        }
        assertTrue(spawned > 0);
        assertTrue(sortie.enemy(member(sortie, 0)).x() > 150, "the head took the bend");
        assertTrue(chain.member(chain.size() - 1).x() > 120, "and the tail followed it round");
    }

    @Test
    void thePoresFireHeadToTailAsATravellingWaveThenRest() {
        Sortie sortie = slow(PORES, 12);
        int start = stepUntilChain(sortie) - 1;
        Chain chain = sortie.chain(0);
        int glow = SimStep.ticks(0.3);
        int interval = SimStep.ticks(0.25);
        int cycle = glow + 11 * interval + SimStep.ticks(2.0);
        List<int[]> fired = new ArrayList<>();
        for (int step = start + 1; step < start + cycle + glow + 2; step++) {
            sortie.step(Command.NONE);
            int clock = step - start;
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.PORE_FIRED) {
                    fired.add(new int[] {clock, events.value(i)});
                    Enemy pore = chain.member(events.value(i));
                    assertEquals(pore.x(), events.x(i));
                    assertEquals(1, chain.poreGlow(events.value(i)), 1e-9, "fully lit as it fires");
                    // The newest spore flies at the ship at 110 px/s.
                    EnemyBullet spore = sortie.bullet(sortie.bulletCount() - 1);
                    double dx = sortie.ship().x() - pore.x();
                    double dy = sortie.ship().y() - pore.y();
                    double d = Math.hypot(dx, dy);
                    assertEquals(dx / d * 110, spore.vx(), 1e-6);
                    assertEquals(dy / d * 110, spore.vy(), 1e-6);
                }
            }
            if (clock == glow - 1) {
                assertEquals((double) (glow - 1) / glow, chain.poreGlow(1), 1e-9, "the tell before it fires");
                assertEquals(0, chain.poreGlow(3), "not yet");
            }
        }
        assertEquals(13, fired.size(), "a wave of 12 and the next wave's first: " + fired.size());
        for (int k = 0; k < 12; k++) {
            assertEquals(glow + k * interval, fired.get(k)[0], "pore " + (k + 1) + "'s turn");
            assertEquals(k + 1, fired.get(k)[1], "head to tail");
        }
        assertEquals(cycle + glow, fired.get(12)[0], "the next wave after the rest");
        assertEquals(1, fired.get(12)[1]);
    }

    @Test
    void onEasyEveryOtherPoreFiresAndOnHardTheRestIsShorter() {
        Sortie easy = slow(new EnemySpec.PoreWave(0.25, 2.0, 0.3, 110, 4, true), 10);
        Sortie hard = slow(new EnemySpec.PoreWave(0.25, 1.5, 0.3, 110, 4, false), 16);
        List<Integer> easyPores = new ArrayList<>();
        List<Integer> hardSteps = new ArrayList<>();
        for (int step = 0; step < SimStep.ticks(9); step++) {
            easy.step(Command.NONE);
            hard.step(Command.NONE);
            for (int i = 0; i < easy.events().size(); i++) {
                if (easy.events().type(i) == SimEvents.Type.PORE_FIRED) {
                    easyPores.add(easy.events().value(i));
                }
            }
            if (hard.events().count(SimEvents.Type.PORE_FIRED) > 0) {
                hardSteps.add(step);
            }
        }
        assertEquals(List.of(1, 3, 5, 7, 9), easyPores.subList(0, 5));
        assertEquals(0.0, easy.chain(0).poreGlow(2), "a pore that never fires never glows");
        // Hard: 16 pores, then the next wave 1.5 s plus the glow after the last.
        assertEquals(SimStep.ticks(1.5) + SimStep.ticks(0.3), hardSteps.get(16) - hardSteps.get(15), "the hard rest");
    }

    @Test
    void aCutStopsTheRearPartWhichDiesASecondLaterFromTheCutBackwards() {
        Sortie sortie = slow(PORES, 12);
        stepUntilChain(sortie);
        for (int step = 0; step < SimStep.ticks(1); step++) {
            sortie.step(Command.NONE);
        }
        Chain chain = sortie.chain(0);
        int cutIndex = member(sortie, 4);
        int kind = sortie.enemy(cutIndex).kind();
        sortie.destroyEnemy(cutIndex);
        assertEquals(1, sortie.events().count(SimEvents.Type.CHAIN_CUT));
        assertEquals(kind, findValue(sortie, SimEvents.Type.CHAIN_CUT), "at the cut segment's kind");
        for (int i = 5; i < chain.size(); i++) {
            assertTrue(chain.stopped(i), "member " + i + " stopped");
            assertTrue(Chain.doomed(chain.member(i)), "and waits for its burst");
        }
        assertFalse(chain.stopped(3));
        double rearRow = Terrain.row(sortie.groundScroll(), chain.member(9).y());
        double headRow = Terrain.row(sortie.groundScroll(), chain.member(0).y());
        int pops = 0;
        int firstPop = -1;
        for (int step = 1; step <= SimStep.ticks(2); step++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.CHAIN_REGROWN), "it never regrows");
            assertEquals(1, sortie.chainCount());
            if (chain.member(9) != null) {
                assertEquals(
                        rearRow,
                        Terrain.row(sortie.groundScroll(), chain.member(9).y()),
                        1e-6,
                        "it stays put");
            }
            assertEquals(
                    headRow + 70 * SimStep.SECONDS * step,
                    Terrain.row(sortie.groundScroll(), chain.member(0).y()),
                    1e-6,
                    "the front crawls on");
            int popped = sortie.events().count(SimEvents.Type.CHAIN_POP);
            if (popped > 0 && firstPop < 0) {
                firstPop = step;
                assertEquals(null, chain.member(5), "the first behind the cut bursts first");
                assertTrue(chain.member(6) != null);
            }
            pops += popped;
        }
        assertEquals(SimStep.ticks(1.0), firstPop, "1.0 s after the cut");
        assertEquals(9, pops, "the rear part died: 8 segments and the tail");
        for (int i = 0; i < 4; i++) {
            assertTrue(chain.member(i) != null, "the front lives on");
        }
    }

    private static int findValue(Sortie sortie, SimEvents.Type type) {
        for (int i = 0; i < sortie.events().size(); i++) {
            if (sortie.events().type(i) == type) {
                return sortie.events().value(i);
            }
        }
        return -1;
    }

    @Test
    void aHeadFirstKillPaysTheWholeAndTheTailFirstItsBonus() {
        double factor = TestSpecs.SCORING.creditFactor();
        Sortie headFirst = slow(PORES, 12);
        stepUntilChain(headFirst);
        int before = headFirst.credits();
        headFirst.destroyEnemy(member(headFirst, 0));
        assertEquals((30 + 18) * factor, headFirst.credits() - before, 1, "head first: the whole 48");

        Sortie segmentFirst = slow(PORES, 12);
        stepUntilChain(segmentFirst);
        segmentFirst.destroyEnemy(member(segmentFirst, 3));
        before = segmentFirst.credits();
        segmentFirst.destroyEnemy(member(segmentFirst, 0));
        assertEquals(30 * factor, segmentFirst.credits() - before, 1, "the head after a segment: 30");

        Sortie tailFirst = slow(PORES, 12);
        stepUntilChain(tailFirst);
        before = tailFirst.credits();
        tailFirst.destroyEnemy(member(tailFirst, 13));
        assertEquals((2 + 4) * factor, tailFirst.credits() - before, 1, "the tail first: 6");
        assertEquals(13, tailFirst.chain(0).size(), "the rest crawls on");
    }

    @Test
    void itLeavesOnceOffTheBottomEdgeAndTheCoilwyrmStaysAnAirChain() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT,
                level(130, crawler(12, PORES, new double[] {200, 400}, new double[] {380, 380})),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        stepUntilChain(sortie);
        int steps = 0;
        while (sortie.chainCount() > 0 && steps < SimStep.ticks(30)) {
            sortie.step(Command.NONE);
            steps++;
        }
        assertEquals(0, sortie.chainCount(), "gone");
        assertEquals(0, sortie.enemyCount());

        Sortie wyrm = new Sortie(
                1,
                TestSpecs.LOADOUT,
                LevelScript.builder(6, 1)
                        .sections(List.of(new Section(40, 130)))
                        .waves(List.of(new WaveSpec(
                                1,
                                WaveSpec.Formation.SNAKE,
                                FarsideTest.COILWYRM,
                                1,
                                WaveSpec.Entry.FRONT,
                                WaveSpec.Edge.NONE,
                                Optional.empty(),
                                Optional.empty(),
                                1,
                                Optional.empty(),
                                Optional.empty(),
                                List.of(),
                                Optional.empty(),
                                List.of(List.of(new WaveSpec.At(240, -60), new WaveSpec.At(240, 700))),
                                Optional.empty())))
                        .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                        .build(),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        stepUntilChain(wyrm);
        for (int step = 0; step < SimStep.ticks(2); step++) {
            wyrm.step(Command.NONE);
            assertFalse(wyrm.chain(0).ground());
            assertEquals(0, wyrm.chain(0).poreGlow(1));
            assertEquals(0, wyrm.events().count(SimEvents.Type.PORE_FIRED));
        }
    }

    @Test
    void crawlersDoNotAllocate() {
        EnemySpec spec = crawler(12, PORES, new double[] {0, 5_000}, new double[] {200, 200});
        LevelScript level = LevelScript.builder(1, 3)
                .sections(List.of(new Section(60, 80)))
                .waves(List.of(
                        TestSpecs.wave(
                                0.5, WaveSpec.Formation.SNAKE, spec, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE),
                        TestSpecs.wave(5, WaveSpec.Formation.SNAKE, spec, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
        long allocated = Allocations.least(
                () -> new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR), sortie -> {
                    for (int i = 0; i < 600; i++) {
                        int stick = i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
                        sortie.step(stick | FIRE);
                    }
                });
        assertEquals(0, allocated);
    }
}
