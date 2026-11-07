package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part G, step G1 (design/enemies/bosses/iron-sovereign, Echo; m11g-gaps.md §1 #7): the Iron Sovereign echo's keys
 * on the test Sovereign: the outer ring alone ({@link RingSpec} of one ring), the core open from its one phase, its
 * forming ({@link EchoSpec#formSeconds()}: untouchable and silent from its arrival), the wreck throw only once two of its
 * batteries are dead ({@link RingSpec.WreckThrow#withMinDead}) and an aimed volley from the core as each play ends
 * ({@link RingSpec.Pool#withCover}).
 */
class SovereignEchoTest {
    static final double FORM = 3;

    static SetPieceSpec echo() {
        SetPieceSpec piece = CorePlaybookTest.sovereign();
        BossSpec boss = piece.boss().orElseThrow();
        RingSpec rings = boss.rings().orElseThrow();
        RingSpec.Core core = rings.core().orElseThrow();
        RingSpec.Pool pool = core.pool().orElseThrow();
        RingSpec.Pool echoPool = new RingSpec.Pool(
                        pool.tellSeconds(),
                        1.0,
                        0.7,
                        pool.lowBelow(),
                        pool.moves(),
                        pool.volley(),
                        pool.surge(),
                        pool.mines(),
                        Optional.of(pool.wrecks().orElseThrow().withMinDead(2)))
                .withCover(Act5HSpecs.FAN_1);
        RingSpec.Core echoCore = new RingSpec.Core(
                        core.part(),
                        core.glowSeconds(),
                        0,
                        core.shield(),
                        core.spiral(),
                        core.spiralSeconds(),
                        core.restSeconds(),
                        core.spiralTellSeconds(),
                        core.rail(),
                        core.railBelow(),
                        core.bays())
                .withPool(Optional.of(echoPool));
        RingSpec outer = new RingSpec(
                        List.of(rings.rings().getFirst()),
                        rings.arcRadians(),
                        rings.bearing(),
                        1,
                        rings.grindSeconds(),
                        List.of(1.0),
                        List.of(0),
                        rings.cookoffSeconds())
                .withCore(Optional.of(echoCore));
        BossSpec.Phase theCore = new BossSpec.Phase(
                "The core",
                List.of(),
                0,
                List.of(Act5HSpecs.SAM),
                false,
                Optional.empty(),
                List.of(Act5HSpecs.CORE),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                0,
                Optional.empty(),
                Optional.empty(),
                BossSpec.PhaseArena.NONE);
        BossSpec echo = BossSpec.builder(
                        boss.arriveSeconds(),
                        boss.x(),
                        boss.hoverY(),
                        boss.descentSpeed(),
                        boss.sineAmplitude(),
                        boss.sinePeriod(),
                        boss.layer(),
                        false,
                        "ECHO",
                        boss.parSeconds(),
                        List.of(),
                        boss.attacks(),
                        List.of(theCore))
                .deathSeconds(2)
                .arena(boss.arena())
                .build()
                .withRings(Optional.of(outer))
                .withEcho(Optional.of(new EchoSpec(FORM, 1.0)));
        return new SetPieceSpec(
                piece.slug(),
                piece.size(),
                piece.body(),
                piece.contactDamage(),
                piece.parts(),
                piece.passes(),
                piece.drop(),
                Optional.of(echo));
    }

    static final class Rig extends Act5HSpecs.Rig {
        final SplitMix64 rng = new SplitMix64(11);
        final List<int[]> wrecks = new java.util.ArrayList<>();
        int plays;

        Rig() {
            super(echo());
        }

        @Override
        public double draw() {
            return rng.nextDouble();
        }

        @Override
        public boolean throwWreck(EnemyGun gun, double x, double y, int part) {
            wrecks.add(new int[] {part, tick});
            return true;
        }

        @Override
        public void corePlay(RingSpec.Play play, boolean low) {
            plays++;
        }
    }

    @Test
    void itFormsUntouchableThenOpensItsCoreUnderTheOneRing() {
        Rig rig = new Rig();
        rig.step();
        for (int i = 1; i < SimStep.ticks(FORM) - 1; i++) {
            assertFalse(rig.piece.engaged(), "forming");
            assertTrue(rig.piece.partShielded(Act5HSpecs.CORE));
            rig.step();
        }
        rig.steps(3);
        assertTrue(rig.piece.engaged());
        assertEquals(1, rig.rings().spec().rings().size());
        assertTrue(rig.rings().core().orElseThrow().open(), "the core open from its one phase");
    }

    @Test
    void theWreckThrowWaitsForTwoDeadBatteriesAndEachPlayEndsWithItsCover() {
        Rig rig = new Rig();
        rig.steps(FORM + 2);
        int covers = 0;
        for (int i = 0; i < SimStep.ticks(40); i++) {
            rig.step();
            for (double[] shot : rig.shots) {
                covers += Math.abs(shot[0] - Act5HSpecs.X) < 1e-6 && Math.abs(shot[1] - Act5HSpecs.Y) < 1e-6 ? 1 : 0;
            }
        }
        assertTrue(rig.plays >= 8, "plays: " + rig.plays);
        assertTrue(rig.wrecks.isEmpty(), "no wreck with every battery alive");
        assertTrue(covers >= rig.plays - 1, "a cover as each play ends: " + covers + " of " + rig.plays);
        List<Integer> sams = rig.rings().spec().rings().getFirst().parts();
        rig.piece.wreckPart(sams.get(0));
        rig.steps(20);
        assertTrue(rig.wrecks.isEmpty(), "not with one dead");
        rig.piece.wreckPart(sams.get(1));
        rig.steps(40);
        assertFalse(rig.wrecks.isEmpty(), "thrown once two are dead");
        for (int[] wreck : rig.wrecks) {
            assertTrue(wreck[0] == sams.get(0) || wreck[0] == sams.get(1), "the dead batteries' wrecks");
        }
    }
}
