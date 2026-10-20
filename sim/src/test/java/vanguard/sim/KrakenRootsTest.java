package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Round 40 (the reworked Harbour Kraken, proposal 3a of the Act 2 playtest, pending the user's review): rooted
 * one-lane slam arms ({@link BossSpec.Lanes#rooted()}), on a test Kraken laid out as the proposal: the platform's
 * centre 20 px below the top edge, the head 150 px below it, four roots of 40 × 40 at y 210 below the top edge
 * over the lane centres 60/180/300/420, 200 HP, ×2 while raised; the slam telegraph 1.4 s; phase 1 until two roots
 * are cut or 25 s, phase 2 the head up, phase 3 the living roots' volleys every 4 s.
 */
class KrakenRootsTest {
    static final int HEAD = 0;
    /** The root over lane n is part n. */
    static final int ROOT_1 = 1;

    static final int ROOT_2 = 2;
    static final int ROOT_3 = 3;
    static final int ROOT_4 = 4;
    static final double ARRIVE = 3;
    static final double SPEED = 140;
    static final double CENTRE_Y = PlayField.HEIGHT - 20;
    static final double ROOT_Y = PlayField.HEIGHT - 210;
    static final double TELEGRAPH = 1.4;

    /** The test Kraken; phase 1 ends once at most {@code rootsLeft} roots live (the proposal: 2) or after 25 s. */
    static SetPieceSpec kraken(int rootsLeft) {
        List<PartSpec> parts = new ArrayList<>();
        parts.add(new PartSpec("head", 0, -150, new Hitbox(130, 200), 300, true, 124, Optional.empty(), 0, 0.5));
        List<BossSpec.Arm> arms = new ArrayList<>();
        List<BossSpec.Chain> chains = new ArrayList<>();
        for (int lane = 1; lane <= 4; lane++) {
            double dx = (lane - 0.5) * 120 - PlayField.WIDTH / 2.0;
            parts.add(
                    new PartSpec("root " + lane, dx, -190, new Hitbox(40, 40), 200, false, 36, Optional.empty(), 0, 1));
            arms.add(new BossSpec.Arm(lane, 1 << lane));
            chains.add(new BossSpec.Chain("arm " + lane, dx * 0.6, -170, lane, 13, new Hitbox(30, 30), 0.1, 0, true));
        }
        var fan = new BossSpec.Attack(
                "beak-fan", BossSpec.Pattern.FAN, SlamArenaTest.FAN, 0.15, false, 5, 0, 0, 0, List.of(HEAD));
        var roots = List.of(ROOT_1, ROOT_2, ROOT_3, ROOT_4);
        var armsPhase = new BossSpec.Phase(
                "Arms",
                roots,
                rootsLeft,
                List.of(),
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                25,
                1,
                Optional.empty(),
                Optional.empty(),
                new BossSpec.PhaseArena(
                        0,
                        List.of(),
                        Double.NaN,
                        BossSpec.Slamming.CHAIN,
                        0,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
        var headUp = new BossSpec.Phase(
                "Head up",
                List.of(),
                0,
                List.of(0),
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                0,
                Optional.empty(),
                Optional.empty(),
                new BossSpec.PhaseArena(
                        0,
                        List.of(HEAD),
                        0.4,
                        BossSpec.Slamming.AFTER_DIVE,
                        0,
                        Optional.of(new BossSpec.Surface(HEAD, 2, 8, 1.5, 0.8, false)),
                        Optional.empty(),
                        Optional.empty()));
        var livingRoots = new BossSpec.Phase(
                "Living roots",
                List.of(HEAD),
                0,
                List.of(0),
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                1,
                Optional.empty(),
                Optional.empty(),
                new BossSpec.PhaseArena(
                        0,
                        List.of(),
                        Double.NaN,
                        BossSpec.Slamming.VOLLEY,
                        4,
                        Optional.of(new BossSpec.Surface(HEAD, 2, 0, 0, 0.8, true)),
                        Optional.empty(),
                        Optional.empty()));
        var arena = new BossSpec.Arena(
                new BossSpec.Lanes(4, 120, PlayField.HEIGHT - 20 - 76, arms, true, 2),
                new BossSpec.Slam(TELEGRAPH, 0.5, 1.5, 0.6, 10, 6, 120, 4, 2, 0.5),
                List.of(Layer.SUB, Layer.GROUND, Layer.GROUND, Layer.GROUND, Layer.GROUND),
                List.of());
        return new SetPieceSpec(
                "test-kraken",
                new Hitbox(344, 380),
                new Hitbox(208, 152),
                25,
                parts,
                List.of(),
                Optional.empty(),
                Optional.of(new BossSpec(
                        ARRIVE,
                        PlayField.WIDTH / 2.0,
                        CENTRE_Y,
                        0,
                        0,
                        1,
                        Layer.GROUND,
                        true,
                        "TEST KRAKEN",
                        60,
                        chains,
                        List.of(fan),
                        List.of(armsPhase, headUp, livingRoots),
                        false,
                        List.of(),
                        List.of(),
                        0,
                        Optional.of(arena))));
    }

    /** The arena level with the naval convoy (cargo ships in lanes 1, 2 and 4) and the root hint on the radio. */
    static LevelScript level(int rootsLeft) {
        return LevelScript.builder(11, 2)
                .sections(List.of(
                        new Section(ARRIVE, SPEED), new Section(ARRIVE + 1, 0, true), new Section(ARRIVE + 11, SPEED)))
                .radio(List.of(new RadioCue(
                        CueTrigger.FIRST_SHIP_TELEGRAPH,
                        0,
                        "",
                        "okafor",
                        "Cut a root and that lane is safe!",
                        false,
                        "")))
                .setPieces(List.of(kraken(rootsLeft)))
                .ground(GroundSpec.NONE.withWater(true))
                .objectives(ObjectiveSpec.NONE.withConvoy(Optional.of(NavalConvoyTest.convoy(NavalConvoyTest.CARGO))))
                .build();
    }

    private static Sortie sortie() {
        return sortie(2);
    }

    private static Sortie sortie(int rootsLeft) {
        return new Sortie(
                1, TestSpecs.LOADOUT, level(rootsLeft), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static SetPiece boss(Sortie sortie) {
        return sortie.setPiece(0);
    }

    private static SlamArena arena(Sortie sortie) {
        return boss(sortie).arena().orElseThrow();
    }

    private static List<Integer> until(Sortie sortie, SimEvents.Type type, double seconds, int commands) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
            List<Integer> values = values(sortie, type);
            if (!values.isEmpty()) {
                return values;
            }
        }
        throw new AssertionError(type + " within " + seconds + " s");
    }

    private static List<Integer> values(Sortie sortie, SimEvents.Type type) {
        List<Integer> values = new ArrayList<>();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                values.add(events.value(i));
            }
        }
        return values;
    }

    private static void steps(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(0);
        }
    }

    private static int armOf(int lane) {
        return lane - 1;
    }

    @Test
    void theRootsAreAlwaysHittableStayPutAndTakeDoubleWhileTheirArmIsRaised() {
        Sortie sortie = sortie();
        until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        SetPiece boss = boss(sortie);
        SlamArena arena = arena(sortie);
        assertTrue(arena.rooted());
        assertEquals(Layer.SUB, boss.partLayer(HEAD), "the head under the water");
        assertEquals(Layer.GROUND, boss.partLayer(ROOT_3), "the root on the surface");
        assertFalse(boss.partShielded(ROOT_3));
        assertEquals(300, boss.partX(ROOT_3), 1e-9);
        assertEquals(ROOT_Y, boss.partY(ROOT_3), 1e-9, "210 px below the top edge");
        double hp = boss.partHp(ROOT_3);
        boss.damagePartAt(ROOT_3, 10, 300, ROOT_Y - 20, 0, 1);
        assertEquals(hp - 10, boss.partHp(ROOT_3), 1e-9, "×1 at rest");

        // The ship's start x 240 lies in lane 3.
        assertEquals(List.of(3), until(sortie, SimEvents.Type.TELEGRAPH, 1.1, 0));
        assertTrue(arena.armRaised(armOf(3)));
        assertEquals(TELEGRAPH + 0.5, arena.untilImpact(3), 1e-9, "the 1.4 s telegraph and the rise");
        boss.damagePartAt(ROOT_3, 10, 300, ROOT_Y - 20, 0, 1);
        assertEquals(hp - 30, boss.partHp(ROOT_3), 1e-9, "×2 while raised (the tell)");
        steps(sortie, TELEGRAPH);
        assertEquals(SlamArena.ArmState.RISE, arena.armState(armOf(3)));
        assertEquals(Layer.GROUND, boss.partLayer(ROOT_3));
        assertEquals(300, boss.partX(ROOT_3), 1e-9, "the root stays put while its arm slams");
        assertEquals(ROOT_Y, boss.partY(ROOT_3), 1e-9);
        assertEquals(ROOT_Y - 20, arena.armTop(armOf(3)), 1e-9, "the arm lies from the root's lower edge");
        until(sortie, SimEvents.Type.SLAM, 0.6, 0);
        steps(sortie, 1.5 + 0.6);
        assertEquals(SlamArena.ArmState.IDLE, arena.armState(armOf(3)));
        assertEquals(Layer.GROUND, boss.partLayer(ROOT_3), "still hittable after the sink");
        assertFalse(arena.armRaised(armOf(3)));
    }

    @Test
    void theAwashArmIsTheRootsHitBoxAndOnlyThen() {
        Sortie sortie = sortie();
        until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        SetPiece boss = boss(sortie);
        SlamArena arena = arena(sortie);
        Hitbox shot = new Hitbox(4, 4);
        double low = 120;
        assertFalse(boss.partTouches(ROOT_3, shot, 300, low), "no arm in the lane at rest");
        until(sortie, SimEvents.Type.TELEGRAPH, 1.1, 0);
        assertFalse(boss.partTouches(ROOT_3, shot, 300, low), "still under the water in its telegraph");
        until(sortie, SimEvents.Type.SLAM, TELEGRAPH + 0.6, 0);
        sortie.step(0);
        assertEquals(SlamArena.ArmState.AWASH, arena.armState(armOf(3)));
        assertTrue(boss.partTouches(ROOT_3, shot, 300, low), "the awash arm along lane 3");
        assertTrue(boss.partTouches(ROOT_3, shot, 300, ROOT_Y), "and the root");
        steps(sortie, 1.5);
        assertEquals(SlamArena.ArmState.SINK, arena.armState(armOf(3)));
        assertFalse(boss.partTouches(ROOT_3, shot, 300, low), "the sinking arm is under the water");
        assertTrue(boss.partTouches(ROOT_3, shot, 300, ROOT_Y));
    }

    @Test
    void aShotOnARootIsARootHit() {
        Sortie sortie = sortie();
        until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        SetPiece boss = boss(sortie);
        while (sortie.ship().x() < 300) {
            sortie.step(Command.RIGHT.bit());
        }
        double hp = boss.partHp(ROOT_3);
        List<Integer> hits = until(sortie, SimEvents.Type.ROOT_HIT, 2, Command.FIRE.bit());
        assertEquals(List.of(ROOT_3), hits);
        assertTrue(boss.partHp(ROOT_3) < hp);
        assertEquals(boss.spec().parts().get(HEAD).hp(), boss.partHp(HEAD), 1e-9, "the submerged head untouched");
    }

    @Test
    void slamsAlternateTheShipsLaneAndTheNearestLivingShipLaneAndACutRootsLaneIsSafeForGood() {
        Sortie sortie = sortie();
        until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        // Mbeki's lane 2 is the nearest ship's: cut its root.
        sortie.destroyPart(0, ROOT_2);
        List<Integer> lanes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            lanes.addAll(until(sortie, SimEvents.Type.TELEGRAPH, 5, 0));
        }
        // Halvorsen (lane 1, x 60) and Saint-Laurent (lane 4, x 420) lie 180 px from x 240: the lower lane.
        assertEquals(List.of(3, 1, 3, 1), lanes);
    }

    @Test
    void theFirstTelegraphOnAShipsLaneCuesTheRootHint() {
        Sortie sortie = sortie();
        until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        until(sortie, SimEvents.Type.TELEGRAPH, 1.1, 0);
        assertTrue(values(sortie, SimEvents.Type.RADIO).isEmpty(), "the ship's own lane holds no cargo ship");
        assertEquals(List.of(2), until(sortie, SimEvents.Type.TELEGRAPH, 5, 0));
        assertEquals(List.of(0), values(sortie, SimEvents.Type.RADIO), "Mbeki's lane: the hint");
    }

    @Test
    void phaseOneEndsWhenTwoRootsAreCutOrAfter25Seconds() {
        Sortie sortie = sortie();
        until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        sortie.destroyPart(0, ROOT_4);
        steps(sortie, 1);
        assertEquals(0, boss(sortie).phase());
        sortie.destroyPart(0, ROOT_1);
        assertEquals(List.of(1), until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0));

        Sortie timed = sortie();
        until(timed, SimEvents.Type.BOSS_SETTLED, 5, 0);
        assertEquals(List.of(1), until(timed, SimEvents.Type.BOSS_PHASE, 25.1, 0));
        assertTrue(boss(timed).endedOnTimeout(0));
    }

    @Test
    void phaseThreeSlamsTheShipsLaneAndOneLivingShipLaneAndSkipsACutOne() {
        // Phase 1 ends on the first cut here, so three roots live on.
        Sortie sortie = sortie(3);
        until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        sortie.destroyPart(0, ROOT_1);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        SlamArena arena = arena(sortie);
        while (arena.armState(1) != SlamArena.ArmState.IDLE
                || arena.armState(2) != SlamArena.ArmState.IDLE
                || arena.armState(3) != SlamArena.ArmState.IDLE) {
            sortie.step(0);
        }
        boss(sortie).damagePart(HEAD, 400);
        assertEquals(List.of(2), until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0));
        // The ship's lane 3 and Mbeki's lane 2; Saint-Laurent's living root (lane 4) sits the volley out.
        assertEquals(
                List.of(2, 3),
                until(sortie, SimEvents.Type.TELEGRAPH, 1.1, 0).stream()
                        .sorted()
                        .toList());
        assertEquals(2, until(sortie, SimEvents.Type.SLAM, TELEGRAPH + 0.6, 0).size(), "both at once");
        sortie.destroyPart(0, ROOT_3);
        // The ship's lane is safe now: the two living ship lanes.
        assertEquals(
                List.of(2, 4),
                until(sortie, SimEvents.Type.TELEGRAPH, 4, 0).stream().sorted().toList());
    }

    @Test
    void aRootedArmOwnsOneLaneOfItsOwn() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpec.Lanes(4, 120, 400, List.of(new BossSpec.Arm(1, 0b110)), true, 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpec.Lanes(
                        4, 120, 400, List.of(new BossSpec.Arm(1, 0b10), new BossSpec.Arm(2, 0b10)), true, 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BossSpec.Lanes(4, 120, 400, List.of(new BossSpec.Arm(1, 0b10)), false, 2));
    }

    @Test
    void theRootedFightIsDeterministicAndAllocatesNothing() {
        Sortie a = sortie();
        Sortie b = sortie();
        for (int i = 0; i < SimStep.ticks(40); i++) {
            int commands = (i / 50) % 2 == 0 ? Command.LEFT.bit() | Command.FIRE.bit() : Command.RIGHT.bit();
            a.step(commands);
            b.step(commands);
        }
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = sortie();
                    steps(sortie, 4);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(40); i++) {
                        sortie.step(
                                Command.FIRE.bit() | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                    }
                });
        assertEquals(0, allocated, "the rooted slams, the root hits and the volleys allocate nothing per step");
    }
}
