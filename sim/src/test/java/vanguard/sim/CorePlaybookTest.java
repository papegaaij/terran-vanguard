package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Step BOSS70 (design/enemies/bosses/iron-sovereign, The round 70 rework): the open core's playbook. After a rest it
 * draws one of five attacks with the seeded random, never the same twice running and the Dart surge at most every third
 * draw; each has its 0.8 s tell; the rail volley fans three lines through the ship's spot, the surge launches a stream
 * of 3 from all four bays, the mine ring throws 8 mines out to 120–220 px, the wreck throw hurls 2–3 dead batteries'
 * wrecks (200 px/s, 60 HP) and leaves their sockets empty; the timed bay streams give way.
 */
class CorePlaybookTest {
    static final double TELL = 0.8;
    static final double REST = 1.5;
    static final double LOW_REST = 0.8;

    /** A thrown wreck: 200 px/s, `heavy` 10 on contact, 60 HP, 5 s, at most 3 in the air. */
    static final EnemyGun WRECK = EnemyGun.aimed(1, 0, 1, 200, 10, false)
            .withHoming(Optional.of(new EnemyGun.HomingSpec(0, 5, 60, 3, 0, true)));

    static final RingSpec.Pool POOL = new RingSpec.Pool(
            TELL,
            REST,
            LOW_REST,
            0.5,
            List.of(
                    new RingSpec.Move(RingSpec.Play.SPIRAL, 1, 2),
                    new RingSpec.Move(RingSpec.Play.VOLLEY, 1, 2),
                    new RingSpec.Move(RingSpec.Play.SURGE, 1, 3),
                    new RingSpec.Move(RingSpec.Play.MINES, 1, 2),
                    new RingSpec.Move(RingSpec.Play.WRECKS, 1, 2)),
            Optional.of(new RingSpec.Volley(Act5HSpecs.CORE_RAIL, 3, Math.toRadians(22), 30)),
            Optional.of(new RingSpec.Surge(3)),
            Optional.of(new RingSpec.MineRing(Act5CSpecs.MINE_GUN, 8, 120, 220, 0.5, 8)),
            Optional.of(new RingSpec.WreckThrow(WRECK, 2, 3, 0.8)));

    /** The test Sovereign with the playbook (its core rail folded into it). */
    static SetPieceSpec sovereign() {
        RingSpec rings = Act5HSpecs.rings(120, 1);
        RingSpec.Core core = rings.core().orElseThrow();
        RingSpec.Core played = new RingSpec.Core(
                        core.part(),
                        core.glowSeconds(),
                        core.openingSeconds(),
                        core.shield(),
                        core.spiral(),
                        core.spiralSeconds(),
                        core.restSeconds(),
                        core.spiralTellSeconds(),
                        -1,
                        1,
                        core.bays())
                .withPool(Optional.of(POOL));
        return Act5HSpecs.sovereign(true, rings.withCore(Optional.of(played)));
    }

    /** The rig with a seeded random and the mines and wrecks it throws recorded. */
    static final class Rig extends Act5HSpecs.Rig {
        final SplitMix64 rng;
        final List<double[]> mines = new ArrayList<>();
        final List<int[]> wrecks = new ArrayList<>();
        final List<EnemyGun> wreckGuns = new ArrayList<>();
        final List<RingSpec.Play> plays = new ArrayList<>();

        Rig(long seed) {
            super(sovereign());
            rng = new SplitMix64(seed);
        }

        @Override
        public double draw() {
            return rng.nextDouble();
        }

        @Override
        public void coreMine(
                EnemyGun gun, double x, double y, double toX, double toY, double throwSeconds, double lifeSeconds) {
            mines.add(new double[] {x, y, toX, toY, throwSeconds, lifeSeconds});
        }

        @Override
        public boolean throwWreck(EnemyGun gun, double x, double y, int part) {
            wrecks.add(new int[] {part, tick});
            wreckGuns.add(gun);
            return true;
        }

        @Override
        public void corePlay(RingSpec.Play play, boolean low) {
            plays.add(play);
        }
    }

    /** The rig up, every ring killed, its core open, the ship straight below at (240, 60). */
    static Rig openRig(long seed) {
        Rig rig = new Rig(seed);
        for (int i = 0; i < SimStep.ticks(Act5HSpecs.SPIN_UP + 1) && rig.rings().spinningUp(); i++) {
            rig.step();
        }
        for (int ring = 0; ring < 3; ring++) {
            Act5HSpecs.killRing(rig.piece, ring);
        }
        rig.until(SimEvents.Type.CORE_OPEN, 8);
        return rig;
    }

    static RingCore core(Rig rig) {
        return rig.rings().core().orElseThrow();
    }

    /** The plays drawn in {@code seconds} of the open core. */
    static List<RingSpec.Play> plays(long seed, double seconds) {
        Rig rig = openRig(seed);
        rig.steps(seconds);
        return rig.plays;
    }

    @Test
    void theCoreDrawsAllFiveNeverTheSameTwiceRunningAndTheSurgeAtMostEveryThirdDraw() {
        Map<RingSpec.Play, Integer> counts = new EnumMap<>(RingSpec.Play.class);
        for (long seed = 1; seed <= 6; seed++) {
            List<RingSpec.Play> plays = plays(seed, 120);
            assertTrue(plays.size() > 15, "draws in 120 s: " + plays.size());
            for (int i = 1; i < plays.size(); i++) {
                assertNotEquals(plays.get(i - 1), plays.get(i), "never twice running: " + plays);
                if (plays.get(i) == RingSpec.Play.SURGE && i >= 2) {
                    assertNotEquals(
                            RingSpec.Play.SURGE, plays.get(i - 2), "a surge at most every third draw: " + plays);
                }
            }
            for (RingSpec.Play play : plays) {
                counts.merge(play, 1, Integer::sum);
            }
        }
        assertEquals(5, counts.size(), "all five drawn: " + counts);
    }

    @Test
    void aReplayDrawsTheSameAndAnotherSeedDrawsOtherwise() {
        assertEquals(plays(11, 40), plays(11, 40), "the same seed, the same draws");
        assertNotEquals(plays(11, 40), plays(77, 40), "another seed, another order");
    }

    @Test
    void eachAttackFollowsItsTellAndTheRestFollowsTheAttack() {
        Rig rig = openRig(3);
        int opened = rig.tick;
        rig.until(SimEvents.Type.CORE_PLAY_TELL, 3);
        assertEquals(SimStep.ticks(REST), rig.tick - opened, "the first draw after a rest");
        RingCore core = core(rig);
        for (int n = 0; n < 12; n++) {
            RingSpec.Play play = core.play();
            assertTrue(core.telling(), "its tell first");
            int told = rig.tick;
            while (core.telling()) {
                rig.step();
            }
            assertEquals(SimStep.ticks(TELL), rig.tick - told, "a 0.8 s tell: " + play);
            while (core.play() != null) {
                rig.step();
            }
            int ended = rig.tick;
            rig.until(SimEvents.Type.CORE_PLAY_TELL, 3);
            assertEquals(SimStep.ticks(REST) + 1, rig.tick - ended, 1, "a 1.5 s rest after " + play);
        }
    }

    @Test
    void belowHalfItsHpTheRestShortens() {
        Rig rig = openRig(5);
        rig.piece.damagePartFlat(Act5HSpecs.CORE, Act5HSpecs.CORE_HP * 0.6);
        RingCore core = core(rig);
        rig.until(SimEvents.Type.CORE_PLAY_TELL, 3);
        assertTrue(core.low(), "below half");
        while (core.play() != null) {
            rig.step();
        }
        int ended = rig.tick;
        rig.until(SimEvents.Type.CORE_PLAY_TELL, 3);
        assertEquals(SimStep.ticks(LOW_REST) + 1, rig.tick - ended, 1, "a 0.8 s rest");
    }

    /** Steps the rig until it draws {@code play}, at most 200 s. */
    static void untilDrawn(Rig rig, RingSpec.Play play) {
        for (int i = 0; i < SimStep.ticks(200); i++) {
            rig.step();
            if (!rig.values(SimEvents.Type.CORE_PLAY_TELL).isEmpty()
                    && core(rig).play() == play) {
                return;
            }
        }
        throw new AssertionError(play + " drawn within 200 s");
    }

    @Test
    void theRailVolleyFansThreeLinesThroughTheShipsSpotAndStrikesOnce() {
        Rig rig = openRig(7);
        untilDrawn(rig, RingSpec.Play.VOLLEY);
        rig.until(SimEvents.Type.CORE_VOLLEY_TELEGRAPH, TELL + 0.1);
        CoreVolley volley = core(rig).volley().orElseThrow();
        assertEquals(3, volley.lines());
        double cx = rig.piece.partX(Act5HSpecs.CORE);
        double cy = rig.piece.partY(Act5HSpecs.CORE);
        double aim = Math.atan2(rig.shipY - cy, rig.shipX - cx);
        for (int k = 0; k < 3; k++) {
            double angle = Math.atan2(volley.dirY(k), volley.dirX(k));
            assertEquals(aim + (k - 1) * Math.toRadians(22), angle, 1e-6, "fanned 22° about the ship's bearing");
            assertEquals(30, Math.hypot(volley.fromX(k) - cx, volley.fromY(k) - cy), 1e-6, "from the petals' rim");
        }
        assertTrue(volley.telegraphing());
        int started = rig.tick;
        rig.until(SimEvents.Type.CORE_VOLLEY_FIRED, 2);
        assertEquals(SimStep.ticks(1.0), rig.tick - started, "the Rail Bunker's 1 s telegraph");
        Hull hull = TestSpecs.HULL;
        assertTrue(volley.strikes(hull, rig.shipX, rig.shipY) == 1, "the middle line through the ship's spot");
        volley.markHit(1);
        assertEquals(-1, volley.strikes(hull, rig.shipX, rig.shipY), "once per line");
    }

    @Test
    void theSurgeLaunchesAStreamOfThreeFromEveryBayAndTheTimedStreamsAreGone() {
        Rig rig = openRig(9);
        int launched = 0;
        int surges = 0;
        for (int i = 0; i < SimStep.ticks(60); i++) {
            rig.step();
            assertTrue(rig.values(SimEvents.Type.BAY_TELL).isEmpty(), "no timed bay stream with a playbook");
            if (!rig.values(SimEvents.Type.CORE_PLAY_TELL).isEmpty()
                    && core(rig).play() == RingSpec.Play.SURGE) {
                surges++;
            }
            launched += rig.launches.size();
        }
        assertTrue(surges > 0, "a surge in 60 s");
        assertEquals(12 * surges, launched, "four bays × 3 a surge");
    }

    @Test
    void theMineRingThrowsEightMinesOutToTheirSeededDistances() {
        Rig rig = openRig(13);
        untilDrawn(rig, RingSpec.Play.MINES);
        rig.until(SimEvents.Type.CORE_MINES_THROWN, TELL + 0.1);
        assertEquals(8, rig.mines.size());
        double cx = rig.piece.partX(Act5HSpecs.CORE);
        double cy = rig.piece.partY(Act5HSpecs.CORE);
        List<Double> distances = new ArrayList<>();
        for (double[] mine : rig.mines) {
            double d = Math.hypot(mine[2] - cx, mine[3] - cy);
            assertTrue(d >= 120 - 1e-9 && d <= 220 + 1e-9, "120–220 px: " + d);
            assertEquals(cx, mine[0], 1e-9, "from the core");
            assertEquals(8, mine[5], 1e-9, "gone after 8 s");
            distances.add(d);
        }
        assertTrue(distances.stream().distinct().count() > 4, "seeded distances: " + distances);
    }

    @Test
    void theWreckThrowHurlsTwoOrThreeDeadBatteriesWrecksAndLeavesTheirSocketsEmpty() {
        Rig rig = openRig(17);
        untilDrawn(rig, RingSpec.Play.WRECKS);
        RingCore core = core(rig);
        int glowing = core.glowingWreck();
        assertTrue(rig.rings().battery(glowing) >= 0, "a battery's wreck glows in the tell");
        while (core.play() == RingSpec.Play.WRECKS) {
            rig.step();
        }
        int n = rig.wrecks.size();
        assertTrue(n >= 2 && n <= 3, "2–3 wrecks: " + n);
        assertEquals(glowing, rig.wrecks.getFirst()[0], "the glowing one first");
        for (int k = 0; k < n; k++) {
            int p = rig.wrecks.get(k)[0];
            assertTrue(core.thrown(p), "its socket empty");
            assertTrue(rig.piece.partHp(p) <= 0, "a dead battery");
            if (k > 0) {
                assertEquals(
                        SimStep.ticks(0.8), rig.wrecks.get(k)[1] - rig.wrecks.get(k - 1)[1], "0.8 s apart");
            }
            EnemyGun gun = rig.wreckGuns.get(k);
            assertEquals(200, gun.bulletSpeed(), "200 px/s");
            assertEquals(60, gun.homing().orElseThrow().hp(), "60 HP");
        }
        assertFalse(core.thrown(Act5HSpecs.CORE));
    }

    @Test
    void aThrownWreckFliesStraightAtTheShipsSpotAndThreeAtMostFly() {
        Missiles missiles = new Missiles(new SimEvents(64));
        assertTrue(missiles.launchWreck(240, 380, WRECK, 240, 60, 5));
        Missile wreck = missiles.get(0);
        assertEquals(5, wreck.wreck());
        assertEquals(Missile.WRECK_BOX, wreck.box());
        for (int i = 0; i < SimStep.ticks(1); i++) {
            missiles.fly(400, 60);
        }
        assertEquals(240, wreck.x(), 1e-6, "no turn toward the ship's new spot");
        assertEquals(380 - 200, wreck.y(), 1, "200 px in 1 s");
        assertTrue(missiles.launchWreck(240, 380, WRECK, 240, 60, 6));
        assertTrue(missiles.launchWreck(240, 380, WRECK, 240, 60, 7));
        assertFalse(missiles.launchWreck(240, 380, WRECK, 240, 60, 8), "at most 3 in the air");
        assertFalse(wreck.damage(59), "60 HP");
        assertTrue(wreck.damage(1));
    }
}
