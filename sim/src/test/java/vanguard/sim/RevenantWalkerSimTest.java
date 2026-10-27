package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part F, step F1: the walking arena boss ({@link StrideArena}; design/enemies/bosses/revenant-walker) on a
 * test walker shaped like the design's medium stat block: a hull of 1,600 (110×120) with a front plate 110×30,
 * two pods of 300 on its shoulders, the vents 50×24 on its back (×2 while open), the rear flame cone 150 px ±22°;
 * passes on x 120/240/360 and y 200/330 from top, bottom, left, top, bottom, right, 3 s off between them, at
 * 45/55/60 px/s; the arena from 3 s to 4 s at speed 0.
 */
class RevenantWalkerSimTest {
    static final int HULL = 0;
    static final int LEFT_POD = 1;
    static final int RIGHT_POD = 2;
    static final double ARRIVE = 3;
    static final double SPEED = 140;
    static final double HULL_HP = 1600;
    private static final int FIRE = Command.FIRE.bit();

    static EnemyGun salvo(double interval, int burst, int most) {
        return EnemyGun.aimed(interval, 0, burst, 110, 6, false)
                .withHoming(Optional.of(new EnemyGun.HomingSpec(Math.PI / 2, 4, 2, most, 0.4)));
    }

    static final EnemyGun CHIN = EnemyGun.aimed(2.5, 0, 3, 150, 4, false);

    private static BossSpec.Phase phase(
            String name, List<Integer> until, List<Integer> attacks, BossSpec.PhaseArena keys) {
        return new BossSpec.Phase(
                name,
                until,
                0,
                attacks,
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                1,
                Optional.empty(),
                Optional.empty(),
                keys);
    }

    private static BossSpec.PhaseArena below(double share, Optional<PickupType> drop) {
        return new BossSpec.PhaseArena(
                0,
                Double.isNaN(share) ? List.of() : List.of(HULL),
                share,
                BossSpec.Slamming.NONE,
                0,
                Optional.empty(),
                Optional.empty(),
                drop);
    }

    /** The test cone: 150 px, ±22°, the burn holding the tell's axis. */
    static final BossSpec.Cone CONE =
            new BossSpec.Cone(150, Math.toRadians(22), 40, 3, 0.6, 1.5, 2, Math.toRadians(25), 10, 50);

    /** The test walker; {@code attacks}: with its salvos and chin bursts, at most {@code most} missiles alive. */
    static SetPieceSpec walker(boolean attacks, int most) {
        return walker(attacks, most, CONE, CHIN_FIRST);
    }

    /** The first phase's chin: a burst of 3 along its heading. */
    private static final int CHIN_FIRST = 2;

    /**
     * The test walker with {@code cone} and its first phase's chin attack {@code chin} ({@link #CHIN_FIRST}, or 4: round
     * 43's fan of 3 over 30° in a burst of 2 along its heading).
     */
    static SetPieceSpec walker(boolean attacks, int most, BossSpec.Cone cone, int chin) {
        var hull = new PartSpec("hull", 0, 0, new Hitbox(110, 120), HULL_HP, true, 210, Optional.empty(), 0, 1);
        var left = new PartSpec("left pod", -73, 0, new Hitbox(36, 36), 300, false, 26, Optional.empty(), 0, 1);
        var right = new PartSpec("right pod", 73, 0, new Hitbox(36, 36), 300, false, 26, Optional.empty(), 0, 1);
        List<Integer> pods = List.of(LEFT_POD, RIGHT_POD);
        var oneAtATime = new BossSpec.Attack(
                "salvo",
                BossSpec.Pattern.AIMED,
                salvo(3, 2, most),
                0.2,
                true,
                0,
                0,
                0,
                0,
                pods,
                false,
                Optional.empty());
        var staggered = new BossSpec.Attack(
                "salvo-hunt",
                BossSpec.Pattern.AIMED,
                salvo(4, 3, most),
                0.2,
                false,
                0,
                0,
                0,
                0,
                pods,
                true,
                Optional.empty());
        var along = new BossSpec.Attack(
                "chin",
                BossSpec.Pattern.AIMED,
                CHIN,
                0.15,
                false,
                0,
                0,
                0,
                0,
                List.of(HULL),
                false,
                Optional.empty(),
                List.of(HULL));
        var aimed =
                new BossSpec.Attack("chin-aimed", BossSpec.Pattern.AIMED, CHIN, 0.15, false, 0, 0, 0, 0, List.of(HULL));
        var fan = new BossSpec.Attack(
                "chin-fan",
                BossSpec.Pattern.FAN,
                new EnemyGun(
                        2,
                        0,
                        2,
                        150,
                        4,
                        false,
                        3,
                        Math.toRadians(30),
                        Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY),
                0.2,
                false,
                3,
                0,
                0,
                0,
                List.of(HULL),
                false,
                Optional.empty(),
                List.of(HULL));
        var stride = phase("Stride", pods, attacks ? List.of(0, chin) : List.of(), below(0.6, Optional.empty()));
        var hunt = phase("Hunt", List.of(), attacks ? List.of(1, 3) : List.of(), below(0.3, Optional.empty()));
        var overheat = phase(
                "Overheat",
                List.of(),
                attacks ? List.of(1, 3) : List.of(),
                below(Double.NaN, Optional.of(PickupType.SHIELD_CELL)));
        var arena = new BossSpec.Arena(
                BossSpec.Lanes.NONE,
                null,
                List.of(Layer.GROUND, Layer.GROUND, Layer.GROUND),
                List.of(new BossSpec.Spot("vents", HULL, 0, 45, new Hitbox(50, 24), 2, true)));
        BossSpec boss = BossSpec.builder(
                        ARRIVE,
                        PlayField.WIDTH / 2.0,
                        300,
                        0,
                        0,
                        1,
                        Layer.GROUND,
                        true,
                        "TEST WALKER",
                        60,
                        List.of(),
                        List.of(oneAtATime, staggered, along, aimed, fan),
                        List.of(stride, hunt, overheat))
                .deathSeconds(2)
                .arena(Optional.of(arena))
                .build()
                .withStride(Optional.of(new BossSpec.Stride(
                        new double[] {120, 240, 360},
                        new double[] {200, 330},
                        List.of(
                                WaveSpec.Edge.TOP,
                                WaveSpec.Edge.BOTTOM,
                                WaveSpec.Edge.LEFT,
                                WaveSpec.Edge.TOP,
                                WaveSpec.Edge.BOTTOM,
                                WaveSpec.Edge.RIGHT),
                        3,
                        1.5,
                        new double[] {45, 55, 60},
                        true,
                        true)))
                .withPlate(Optional.of(new BossSpec.Plate(HULL, new Hitbox(110, 30), 45)))
                .withCone(Optional.of(cone));
        return new SetPieceSpec(
                "test-walker",
                new Hitbox(170, 190),
                new Hitbox(110, 120),
                0,
                List.of(hull, left, right),
                List.of(),
                Optional.empty(),
                Optional.of(boss));
    }

    /** The arena level: 3 s at 140 px/s, the arena of 1 s at speed 0, 10 s after it. */
    static LevelScript level(boolean attacks, int most) {
        return LevelScript.builder(19, 2)
                .sections(List.of(
                        new Section(ARRIVE, SPEED), new Section(ARRIVE + 1, 0, true), new Section(ARRIVE + 11, SPEED)))
                .setPieces(List.of(walker(attacks, most)))
                .build();
    }

    /** Round 44: the test rail lance: after both pods or from the last phase, a 1 s charge, a 0.3 s shot, every 3.5 s (2.5 s). */
    static final BossSpec.Lance LANCE =
            new BossSpec.Lance(List.of(LEFT_POD, RIGHT_POD), 2, 1, 0.3, 3.5, 2.5, 14, 10, 20);
    /** Round 44: the test stomp: within 90 px, a 0.5 s tell, a ring to 110 px over 0.3 s, every 3 s at most, 6. */
    static final BossSpec.Stomp STOMP = new BossSpec.Stomp(90, 110, 0.5, 0.3, 3, 6);

    /** Round 44: the test walker with its pods armoured while shut, the rail lance and the stomp. */
    static SetPieceSpec striking(boolean attacks) {
        SetPieceSpec base = walker(attacks, 6);
        BossSpec boss = base.boss().orElseThrow();
        BossSpec armed = boss.withStride(
                Optional.of(boss.stride().orElseThrow().withStrikes(true, Optional.of(LANCE), Optional.of(STOMP))));
        return new SetPieceSpec(
                base.slug(),
                base.size(),
                base.body(),
                base.contactDamage(),
                base.parts(),
                base.passes(),
                base.drop(),
                Optional.of(armed));
    }

    /** Round 44: the arena level with the striking walker. */
    static LevelScript strikingLevel(boolean attacks) {
        return LevelScript.builder(19, 2)
                .sections(List.of(
                        new Section(ARRIVE, SPEED), new Section(ARRIVE + 1, 0, true), new Section(ARRIVE + 11, SPEED)))
                .setPieces(List.of(striking(attacks)))
                .build();
    }

    private static Sortie sortie(LevelScript level, Rules rules) {
        return new Sortie(1, TestSpecs.LOADOUT, level, rules, TestSpecs.FULL_ARMOUR);
    }

    private static Sortie sortie(boolean attacks, Rules rules) {
        return sortie(level(attacks, 6), rules);
    }

    private static SetPiece boss(Sortie sortie) {
        return sortie.setPiece(0);
    }

    private static StrideArena stride(Sortie sortie) {
        return boss(sortie).stride().orElseThrow();
    }

    /** Steps until {@code type} happens (returns its values in that step), at most {@code seconds}. */
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

    private static void steps(Sortie sortie, double seconds, int commands) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
        }
    }

    /** Flies the ship to {@code x} (holding {@code extra}), then lets it settle. */
    private static void steerTo(Sortie sortie, double x, int extra) {
        for (int i = 0; i < 600 && Math.abs(sortie.ship().x() - x) > 2; i++) {
            sortie.step((sortie.ship().x() < x ? Command.RIGHT : Command.LEFT).bit() | extra);
        }
        steps(sortie, 0.2, extra);
    }

    /** Steps until the walker's gait is {@code gait}, at most {@code seconds}. */
    private static void untilGait(Sortie sortie, StrideArena.Gait gait, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            if (stride(sortie).gait() == gait) {
                return;
            }
            sortie.step(0);
        }
        throw new AssertionError(gait + " within " + seconds + " s");
    }

    private static double health(Sortie sortie) {
        Defences defences = sortie.ship().defences();
        return defences.shield() + defences.armour();
    }

    @Test
    void theIntroWalksInFromTheTopBesideTheShipAndEngagesWhenTheHullIsOnTheScreen() {
        Sortie sortie = sortie(false, TestSpecs.RULES);
        SetPiece boss = boss(sortie);
        while (sortie.levelSeconds() < ARRIVE - SimStep.SECONDS) {
            sortie.step(0);
            assertFalse(boss.present(), "no bar before it arrives");
        }
        assertEquals(List.of(0), until(sortie, SimEvents.Type.BOSS_ARRIVED, 0.1, 0));
        StrideArena stride = stride(sortie);
        // The ship's x 240 is the centre lane: beside it, the left lane first.
        assertEquals(120, boss.x(), 1e-9, "the lane beside the ship's");
        assertEquals(0, stride.heading(), "walking straight down");
        assertEquals(WaveSpec.Edge.TOP, stride.nextEdge());
        assertTrue(boss.y() > PlayField.HEIGHT, "off the top edge");
        assertTrue(boss.partShielded(HULL), "invulnerable until the hull is on the screen");
        double y = boss.y();
        until(sortie, SimEvents.Type.BOSS_SETTLED, 5, 0);
        assertTrue(boss.y() + 60 <= PlayField.HEIGHT && boss.y() + 60 > PlayField.HEIGHT - 1, "the hull just in");
        assertFalse(boss.partShielded(HULL));
        assertTrue(sortie.bossCheckpoint(), "the checkpoint at its arrival");
        double seconds = (y - boss.y()) / 45;
        assertTrue(seconds > 3 && seconds < 4, "about 3.4 s of its intro at 45 px/s: " + seconds);
        y = boss.y();
        steps(sortie, 1, 0);
        assertEquals(45, y - boss.y(), 1e-6, "45 px/s in its first phase");
        assertTrue(sortie.arenaHalted(), "the level clock halts while it lives");
        assertEquals(0, sortie.groundSpeed());
        assertEquals(Layer.GROUND, boss.partLayer(HULL));
    }

    @Test
    void passesCycleTheEdgesOffTheScreenWithNoEdgeWarning() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_ARRIVED, 4, 0);
        StrideArena stride = stride(sortie);
        SetPiece boss = boss(sortie);
        List<WaveSpec.Edge> edges = new ArrayList<>(List.of(stride.nextEdge()));
        List<Double> lanes = new ArrayList<>(List.of(boss.x()));
        List<Integer> headings = new ArrayList<>(List.of(stride.heading()));
        int offSteps = 0;
        boolean rear = false;
        int passes = stride.passes();
        while (stride.passes() < 7) {
            sortie.step(0);
            if (stride.offScreen()) {
                offSteps++;
                // 2026-10-21 (user): a boss never warns; its entry from any edge is the surprise.
                assertEquals(0, sortie.edgeWarnings(), "no edge warning for its next entry " + stride.nextEdge());
            }
            if (stride.passes() != passes) {
                passes = stride.passes();
                edges.add(stride.nextEdge());
                boolean vertical = stride.heading() % 4 == 0;
                lanes.add(vertical ? boss.x() : boss.y());
                headings.add(stride.heading());
            }
            if (stride.nextEdge() == WaveSpec.Edge.BOTTOM && !stride.offScreen()) {
                rear |= stride.rear() == WarningEdge.BOTTOM.bit();
            }
            assertTrue(sortie.levelSeconds() < ARRIVE + 1 + 1e-9, "the level clock stays halted");
        }
        assertEquals(
                List.of(
                        WaveSpec.Edge.TOP,
                        WaveSpec.Edge.BOTTOM,
                        WaveSpec.Edge.LEFT,
                        WaveSpec.Edge.TOP,
                        WaveSpec.Edge.BOTTOM,
                        WaveSpec.Edge.RIGHT,
                        WaveSpec.Edge.TOP),
                edges);
        // The ship stays at (240, 96): beside its lanes, the centre one's sides in turn; the lower y lane's other.
        assertEquals(List.of(120.0, 360.0, 330.0, 120.0, 360.0, 330.0, 120.0), lanes);
        assertEquals(List.of(0, 4, 6, 0, 4, 2, 0), headings);
        assertEquals(6 * SimStep.ticks(3), offSteps, "3 s off the screen between two passes");
        assertTrue(rear, "a bottom entry is a rear wave for Rook");
    }

    @Test
    void fromPhaseTwoAPassTakesTheShipsLaneAtFiftyFivePixelsASecond() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        boss.damagePart(LEFT_POD, 1000);
        boss.damagePart(RIGHT_POD, 1000);
        assertEquals(List.of(1), until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0), "both pods down end phase 1");
        double y = boss.y();
        steps(sortie, 1, 0);
        assertEquals(55, y - boss.y(), 1e-6, "55 px/s from its second phase");
        StrideArena stride = stride(sortie);
        untilGait(sortie, StrideArena.Gait.OFF, 20);
        untilGait(sortie, StrideArena.Gait.WALK, 4);
        assertEquals(WaveSpec.Edge.BOTTOM, stride.nextEdge());
        assertEquals(240, boss.x(), 1e-9, "the ship's own lane");
    }

    @Test
    void phaseOneEndsWhenTheHullFallsBelowSixtyPercent() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        boss(sortie).damagePart(HULL, HULL_HP * 0.39);
        steps(sortie, 0.5, 0);
        assertEquals(0, boss(sortie).phase());
        boss(sortie).damagePart(HULL, HULL_HP * 0.02);
        assertEquals(List.of(1), until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0));
        assertFalse(stride(sortie).ventsOpen(), "the vents open only after a burn in phase 2");
    }

    @Test
    void theOverheatingPhaseTurnsRoundOnTheSpotWithItsConeBurningAndWalksBackInTheShipsLane() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        StrideArena stride = stride(sortie);
        boss.damagePart(HULL, HULL_HP * 0.75);
        assertEquals(List.of(2), until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0), "below 30 %: the overheat");
        boolean cell = false;
        for (int i = 0; i < sortie.pickupCount(); i++) {
            cell |= sortie.pickup(i).type() == PickupType.SHIELD_CELL;
        }
        assertTrue(cell, "a shield cell drops as it begins");
        assertTrue(stride.ventsOpen(), "the vents stay open");
        double y = boss.y();
        steps(sortie, 1, 0);
        assertEquals(60, y - boss.y(), 1e-6, "60 px/s");
        steerTo(sortie, 360, 0);
        untilGait(sortie, StrideArena.Gait.HOLD, 12);
        assertEquals(95, boss.y(), 1e-9, "it stops with its body just on the screen");
        assertTrue(values(sortie, SimEvents.Type.CONE_TELL).size() == 1, "the cone's tell before the turn");
        assertEquals(StrideArena.ConePhase.TELL, stride.conePhase());
        steps(sortie, 0.6, 0);
        assertEquals(StrideArena.Gait.TURN, stride.gait());
        assertEquals(StrideArena.ConePhase.BURN, stride.conePhase(), "the cone burns through the turn");
        double cone = stride.coneHeading();
        List<Integer> turnHeadings = new ArrayList<>();
        while (stride.gait() == StrideArena.Gait.TURN) {
            sortie.step(0);
            if (turnHeadings.isEmpty() || turnHeadings.getLast() != stride.heading()) {
                turnHeadings.add(stride.heading());
            }
        }
        assertEquals(List.of(0, 7, 6, 5, 4), turnHeadings, "round toward the ship on its right, via the diagonals");
        assertEquals(StrideArena.ConePhase.IDLE, stride.conePhase(), "the burn ends with the turn");
        assertTrue(
                Math.abs(Math.IEEEremainder(stride.coneHeading() - cone, 2 * Math.PI)) > Math.PI - 0.1,
                "the cone swept through 180°");
        assertEquals(StrideArena.Gait.WALK, stride.gait());
        sortie.step(0);
        assertEquals(5, stride.heading(), "diagonally up toward the ship's lane");
        for (int i = 0; i < SimStep.ticks(8) && stride.heading() != 4; i++) {
            sortie.step(0);
        }
        assertEquals(4, stride.heading());
        assertEquals(360, boss.x(), 1e-9, "walking back up the ship's lane");
        untilGait(sortie, StrideArena.Gait.HOLD, 12);
        assertEquals(PlayField.HEIGHT - 95, boss.y(), 1e-9, "it turns at the top too: it never leaves");
        assertFalse(stride.offScreen());
    }

    @Test
    void thePlateTurnsShotsOnItsHeadingSideAndTheBackTakesThem() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        steerTo(sortie, 120, 0);
        int glanced = 0;
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(FIRE);
            glanced += values(sortie, SimEvents.Type.SHOT_GLANCED).size();
        }
        assertTrue(glanced > 3, "the plate turns the shots from ahead: " + glanced);
        assertEquals(HULL_HP, boss.partHp(HULL), 1e-9, "the hull untouched behind its plate");
        assertTrue(boss.plateTouches(new Hitbox(4, 12), 120, boss.y() - 45), "the plate ahead, straight down");
        assertFalse(boss.plateTouches(new Hitbox(4, 12), 120, boss.y() + 45), "not on its back");
        // The second pass walks up from the bottom edge: its back faces the ship below.
        untilGait(sortie, StrideArena.Gait.OFF, 20);
        untilGait(sortie, StrideArena.Gait.WALK, 4);
        assertEquals(4, stride(sortie).heading());
        steerTo(sortie, boss.x(), 0);
        while (boss.y() < sortie.ship().y() + 150) {
            sortie.step(0);
        }
        assertTrue(boss.plateTouches(new Hitbox(4, 12), boss.x(), boss.y() + 45), "the plate ahead, up");
        steps(sortie, 1, FIRE);
        assertTrue(boss.partHp(HULL) < HULL_HP, "the back takes the shots");
    }

    @Test
    void theIonBeamStopsAtThePlateAndBurnsTheBack() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.loadout(PartASpecs.beamMount(1)),
                level(false, 6),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        steerTo(sortie, 120, 0);
        steps(sortie, 0.5, FIRE);
        Beam beam = sortie.beam(0);
        assertEquals(Beam.Contact.BLOCKED, beam.contact(), "the plate stops the beam");
        assertTrue(beam.endY() < boss.y() - 50, "at the plate's lower edge: " + beam.endY());
        assertEquals(HULL_HP, boss.partHp(HULL), 1e-9);
        untilGait(sortie, StrideArena.Gait.OFF, 20);
        untilGait(sortie, StrideArena.Gait.WALK, 4);
        steerTo(sortie, boss.x(), 0);
        while (boss.y() < sortie.ship().y() + 150) {
            sortie.step(0);
        }
        steps(sortie, 0.5, FIRE);
        assertEquals(Beam.Contact.BURN, sortie.beam(0).contact(), "it burns the back");
        assertTrue(boss.partHp(HULL) < HULL_HP);
    }

    @Test
    void theConeTellsBurnsOncePerBurnAndOpensTheVentsForTwoSeconds() {
        Sortie sortie = sortie(false, TestSpecs.RULES);
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        StrideArena stride = stride(sortie);
        steerTo(sortie, 120, 0);
        double before = health(sortie);
        until(sortie, SimEvents.Type.CONE_TELL, 15, 0);
        int tellTick = 0;
        assertEquals(StrideArena.ConePhase.TELL, stride.conePhase());
        assertTrue(sortie.ship().y() > stride.coneY(), "the ship is behind it");
        assertEquals(before, health(sortie), 1e-9, "the tell does not hurt");
        assertEquals(Math.PI, stride.coneHeading(), 1e-9, "against its heading: straight up");
        for (; stride.conePhase() == StrideArena.ConePhase.TELL; tellTick++) {
            sortie.step(0);
        }
        assertEquals(SimStep.ticks(0.6), tellTick, "a 0.6 s tell");
        assertEquals(StrideArena.ConePhase.BURN, stride.conePhase());
        int burn = 0;
        for (; stride.conePhase() == StrideArena.ConePhase.BURN; burn++) {
            sortie.step(0);
        }
        assertEquals(SimStep.ticks(1.5), burn, "a 1.5 s burn");
        assertEquals(before - 10, health(sortie), 1e-9, "`heavy` once per burn");
        assertTrue(stride.ventsOpen(), "the vents open after the burn");
        double vx = boss.partX(HULL);
        double vy = boss.partY(HULL) + 45;
        double hp = boss.partHp(HULL);
        boss.damagePartAt(HULL, 10, vx, vy, 0, -1);
        assertEquals(hp - 20, boss.partHp(HULL), 1e-9, "×2 on the open vents");
        boss.damagePartAt(HULL, 10, vx + 50, boss.partY(HULL), 0, 1);
        assertEquals(hp - 30, boss.partHp(HULL), 1e-9, "×1 off them");
        steps(sortie, 2, 0);
        assertFalse(stride.ventsOpen(), "shut 2 s after the burn");
        hp = boss.partHp(HULL);
        boss.damagePartAt(HULL, 10, boss.partX(HULL), boss.partY(HULL) + 45, 0, -1);
        assertEquals(hp - 10, boss.partHp(HULL), 1e-9, "×1 on shut vents");
    }

    /**
     * Step F4: the cone's first tell is the fight's first telegraph: the level's {@code first-telegraph} line plays
     * then (Varga's flame-tell line on Level 19), once.
     */
    @Test
    void theConesFirstTellCuesTheFirstTelegraphLine() {
        LevelScript base = level(false, 6);
        LevelScript level = LevelScript.builder(base.number(), base.act())
                .sections(base.sections())
                .radio(List.of(new RadioCue(CueTrigger.FIRST_TELEGRAPH, 0, "", "Varga", "Flamer!", false, "neutral")))
                .setPieces(base.setPieces())
                .build();
        Sortie sortie = sortie(level, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        steerTo(sortie, 120, 0);
        int lines = 0;
        int tells = 0;
        for (int i = 0; i < SimStep.ticks(40); i++) {
            sortie.step(0);
            tells += values(sortie, SimEvents.Type.CONE_TELL).size();
            for (int index : values(sortie, SimEvents.Type.RADIO)) {
                assertEquals(
                        CueTrigger.FIRST_TELEGRAPH,
                        sortie.script().radio().get(index).trigger());
                assertEquals(1, tells, "at the first tell");
                lines++;
            }
        }
        assertTrue(tells > 1, "the cone told more than once");
        assertEquals(1, lines, "the line plays once");
    }

    @Test
    void theConeWaitsItsCooldownAndItsGeometryIsALimitedSector() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        StrideArena stride = stride(sortie);
        steerTo(sortie, 120, 0);
        until(sortie, SimEvents.Type.CONE_TELL, 15, 0);
        double ox = stride.coneX();
        double oy = stride.coneY();
        assertEquals(boss(sortie).y() + 50, oy, 1e-9, "the flamer 50 px behind its centre");
        assertTrue(stride.inCone(ox, oy + 100));
        assertFalse(stride.inCone(ox, oy + 160), "150 px long");
        assertTrue(stride.inCone(ox + 30, oy + 100), "within 22°");
        assertFalse(stride.inCone(ox + 50, oy + 100), "outside 22°");
        assertFalse(stride.inCone(ox, oy - 20), "not ahead of the flamer");
        int steps = 0;
        do {
            sortie.step(0);
            steps++;
        } while (values(sortie, SimEvents.Type.CONE_TELL).isEmpty() && steps < SimStep.ticks(10));
        assertTrue(steps >= SimStep.ticks(3), "at most once every 3 s: " + steps);
    }

    @Test
    void inItsSecondPhaseTheConeSwivelsTowardTheShipDuringItsTell() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        boss(sortie).damagePart(HULL, HULL_HP * 0.5);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        StrideArena stride = stride(sortie);
        steerTo(sortie, 160, 0);
        until(sortie, SimEvents.Type.CONE_TELL, 15, 0);
        steps(sortie, 0.3, 0);
        double swivel = stride.coneHeading();
        assertTrue(
                swivel > Math.PI + 0.1 && swivel <= Math.PI + Math.toRadians(25) + 1e-9,
                "up to 25° toward the ship on its right: " + swivel);
    }

    /**
     * Round 43 (the walker's rework): a tracking burn in its second phase keeps turning toward the ship at its rate,
     * within its swivel either side of its back; without a track the burn holds the tell's axis.
     */
    @Test
    void aTrackingBurnKeepsTurningTowardTheShipWithinItsSwivel() {
        BossSpec.Cone tracking = new BossSpec.Cone(
                150, Math.toRadians(22), 40, 3, 0.6, 1.5, 2, Math.toRadians(25), 10, 50, Math.toRadians(40));
        double[] turned = new double[2];
        for (int k = 0; k < 2; k++) {
            LevelScript level = LevelScript.builder(19, 2)
                    .sections(List.of(
                            new Section(ARRIVE, SPEED),
                            new Section(ARRIVE + 1, 0, true),
                            new Section(ARRIVE + 11, SPEED)))
                    .setPieces(List.of(walker(false, 6, k == 0 ? CONE : tracking, CHIN_FIRST)))
                    .build();
            Sortie sortie = sortie(level, TestSpecs.RULES.withInvulnerableShip());
            until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
            boss(sortie).damagePart(HULL, HULL_HP * 0.5);
            until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
            StrideArena stride = stride(sortie);
            steerTo(sortie, 160, 0);
            until(sortie, SimEvents.Type.CONE_BURN, 15, 0);
            double start = stride.coneHeading();
            double back = stride.headingRadians() + Math.PI;
            // The ship flies in behind the walker through the burn.
            for (int i = 0; i < SimStep.ticks(0.5); i++) {
                int right = (sortie.ship().x() < stride.coneX() ? Command.RIGHT : Command.LEFT).bit();
                double before = stride.coneHeading();
                sortie.step(right);
                assertTrue(
                        Math.abs(Math.IEEEremainder(stride.coneHeading() - before, 2 * Math.PI))
                                <= Math.toRadians(40) * SimStep.SECONDS + 1e-9,
                        "at most 40°/s");
                assertTrue(
                        Math.abs(Math.IEEEremainder(stride.coneHeading() - back, 2 * Math.PI))
                                <= Math.toRadians(25) + 1e-9,
                        "within its swivel");
            }
            assertEquals(StrideArena.ConePhase.BURN, stride.conePhase());
            turned[k] = Math.abs(Math.IEEEremainder(stride.coneHeading() - start, 2 * Math.PI));
        }
        assertEquals(0, turned[0], 1e-12, "without a track the burn holds its axis");
        assertTrue(turned[1] > Math.toRadians(5), "a tracking burn follows the ship: " + Math.toDegrees(turned[1]));
    }

    @Test
    void podsOpenTheirHatchesBeforeEachSalvoAndTheMissilesHomeAndFizzle() {
        Sortie sortie = sortie(true, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        List<Integer> hatch = until(sortie, SimEvents.Type.POD_HATCH, 6, 0);
        assertEquals(List.of(LEFT_POD), hatch, "one pod at a time, the left first");
        assertTrue(boss.partHatchOpen(LEFT_POD));
        int steps = 0;
        do {
            sortie.step(0);
            steps++;
        } while (values(sortie, SimEvents.Type.MISSILE_LAUNCHED).isEmpty());
        assertEquals(SimStep.ticks(0.4), steps, "the hatch 0.4 s before its salvo");
        assertEquals(1, sortie.missileCount());
        Missile first = sortie.missile(0);
        assertEquals(boss.partX(LEFT_POD), first.x(), 3, "from the pod (one step flown)");
        double step = Math.PI / 8;
        // Launched along one of the pod's 16 headings (it has flown one step since).
        assertEquals(0, Math.abs(Math.IEEEremainder(first.heading(), step)), Math.PI / 2 / 60 + 1e-9);
        steps = 0;
        do {
            sortie.step(0);
            steps++;
        } while (values(sortie, SimEvents.Type.MISSILE_LAUNCHED).isEmpty());
        assertEquals(SimStep.ticks(0.2), steps, "a salvo of 2, 0.2 s apart");
        assertTrue(boss.partHatchOpen(LEFT_POD), "open until its salvo's last missile left");
        sortie.step(0);
        assertFalse(boss.partHatchOpen(LEFT_POD));
        double distance = Math.hypot(
                first.x() - sortie.ship().x(), first.y() - sortie.ship().y());
        steps(sortie, 1, 0);
        assertTrue(
                Math.hypot(
                                first.x() - sortie.ship().x(),
                                first.y() - sortie.ship().y())
                        < distance - 80,
                "it homes on the ship");
        assertEquals(List.of(RIGHT_POD), until(sortie, SimEvents.Type.POD_HATCH, 3, 0), "then the other pod");
        assertEquals(
                List.of(Missiles.FIZZLED),
                until(sortie, SimEvents.Type.MISSILE_DESTROYED, 3, 0),
                "4 s, then it fizzles");
    }

    @Test
    void atMostTheCapFliesAtOnce() {
        for (int most : new int[] {2, 1}) {
            Sortie sortie = sortie(level(true, most), TestSpecs.RULES.withInvulnerableShip());
            boolean full = false;
            for (int i = 0; i < SimStep.ticks(40); i++) {
                sortie.step(0);
                assertTrue(sortie.missileCount() <= most, "at most " + most + ": " + sortie.missileCount());
                full |= sortie.missileCount() == most;
            }
            assertTrue(full, "the cap is reached: " + most);
        }
        Sortie sortie = sortie(true, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        boss(sortie).damagePart(HULL, HULL_HP * 0.5);
        for (int i = 0; i < SimStep.ticks(40); i++) {
            sortie.step(0);
            assertTrue(sortie.missileCount() <= 6, "at most 6 in its second phase: " + sortie.missileCount());
        }
    }

    @Test
    void aMissileIsShotDownByShotsAndTheBeamAndBurstsOnTheHull() {
        Sortie sortie = sortie(false, TestSpecs.RULES);
        steps(sortie, 0.5, 0);
        EnemyGun gun = salvo(3, 2, 6);
        Missiles missiles = sortie.missiles();
        assertTrue(missiles.launch(
                sortie.ship().x(), 400, gun, sortie.ship().x(), sortie.ship().y()));
        assertEquals(List.of(Missiles.SHOT_DOWN), until(sortie, SimEvents.Type.MISSILE_DESTROYED, 2, FIRE));
        assertEquals(0, sortie.missileCount());
        double before = health(sortie);
        missiles.launch(
                sortie.ship().x() - 60,
                300,
                gun,
                sortie.ship().x(),
                sortie.ship().y());
        assertEquals(List.of(Missiles.STRUCK), until(sortie, SimEvents.Type.MISSILE_DESTROYED, 3, 0));
        assertEquals(before - 6, health(sortie), 1e-9, "`medium` on a hit");

        Sortie beam = new Sortie(
                1, TestSpecs.loadout(PartASpecs.beamMount(1)), level(false, 6), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        steps(beam, 0.5, 0);
        beam.missiles()
                .launch(beam.ship().x(), 400, gun, beam.ship().x(), beam.ship().y());
        assertEquals(List.of(Missiles.SHOT_DOWN), until(beam, SimEvents.Type.MISSILE_DESTROYED, 2, FIRE));
    }

    @Test
    void theChinBurstsAlongItsHeadingInPhaseOneAndAimsFromPhaseTwo() {
        Sortie sortie = sortie(true, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        // The walker walks down lane 120, the ship sits at x 240.
        EnemyBullet along = chinShot(sortie, 5);
        assertEquals(0, along.vx(), 1e-9, "straight down its heading");
        assertEquals(-150, along.vy(), 1e-9);
        boss(sortie).damagePart(HULL, HULL_HP * 0.5);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        steps(sortie, 1, 0);
        EnemyBullet aimed = chinShot(sortie, 5);
        assertTrue(aimed.vx() > 10, "aimed at the ship on its right: " + aimed.vx());
    }

    /**
     * Round 43: a walking boss's fan along its heading (the walker's first-phase chin): its bullets spread round its
     * heading, a burst of them {@code burst_gap} apart.
     */
    @Test
    void aFacingFanFiresItsBurstSpreadRoundItsHeading() {
        LevelScript level = LevelScript.builder(19, 2)
                .sections(List.of(
                        new Section(ARRIVE, SPEED), new Section(ARRIVE + 1, 0, true), new Section(ARRIVE + 11, SPEED)))
                .setPieces(List.of(walker(true, 6, CONE, 4)))
                .build();
        Sortie sortie = sortie(level, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        EnemyBullet first = chinShot(sortie, 5);
        List<Double> xs = new ArrayList<>();
        for (int b = 0; b < sortie.bulletCount(); b++) {
            EnemyBullet bullet = sortie.bullet(b);
            if (Math.hypot(bullet.x() - first.x(), bullet.y() - first.y()) < 5) {
                xs.add(bullet.vx());
                assertTrue(bullet.vy() < -140, "down its heading: " + bullet.vy());
            }
        }
        xs.sort(null);
        assertEquals(3, xs.size(), "a fan of 3");
        assertEquals(-150 * Math.sin(Math.toRadians(15)), xs.getFirst(), 1e-6);
        assertEquals(0, xs.get(1), 1e-6);
        int steps = 0;
        do {
            sortie.step(0);
            steps++;
        } while (values(sortie, SimEvents.Type.ENEMY_FIRED).isEmpty() && steps < SimStep.ticks(1));
        assertEquals(SimStep.ticks(0.2), steps, "the burst's second fan 0.2 s later");
    }

    /** Steps until a bullet lies by the hull's centre (its chin burst's, just fired), at most {@code seconds}. */
    private static EnemyBullet chinShot(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(0);
            SetPiece boss = boss(sortie);
            for (int b = 0; b < sortie.bulletCount(); b++) {
                EnemyBullet bullet = sortie.bullet(b);
                if (Math.hypot(bullet.x() - boss.partX(HULL), bullet.y() - boss.partY(HULL)) < 5) {
                    return bullet;
                }
            }
        }
        throw new AssertionError("a chin shot within " + seconds + " s");
    }

    @Test
    void itDiesTopplingAlongItsHeadingAndItsWreckScrollsWithTheGround() {
        Sortie sortie = sortie(false, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        boss.damagePart(HULL, 2 * HULL_HP);
        sortie.objectivePasses.wreck(0, HULL);
        assertEquals(1, values(sortie, SimEvents.Type.BOSS_DESTROYED).size());
        assertEquals(List.of(0), values(sortie, SimEvents.Type.BOSS_WRECK), "facing down as it falls");
        assertEquals(StrideArena.ConePhase.IDLE, stride(sortie).conePhase(), "its cone goes cold");
        assertFalse(stride(sortie).ventsOpen());
        double y = boss.y();
        steps(sortie, 3, 0);
        assertTrue(sortie.groundSpeed() > 0, "the scroll runs on");
        assertTrue(boss.y() < y - 50, "the wreck scrolls away with the ground: " + boss.y());
        assertEquals(0, values(sortie, SimEvents.Type.WALKER_STEP).size());
    }

    @Test
    void itStepsItsFeetAndRetryFromTheBossBringsItBackOffTheTopEdge() {
        Sortie sortie = sortie(true, TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        int footfalls = 0;
        for (int i = 0; i < SimStep.ticks(STRIDE_SECONDS); i++) {
            sortie.step(0);
            footfalls += values(sortie, SimEvents.Type.WALKER_STEP).size();
        }
        assertEquals(6, footfalls, "two footfalls a walk cycle");
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0, () -> boss(sortie).damagePart(HULL, HULL_HP * 0.5));
        sortie.retryFromBoss();
        sortie.step(0);
        SetPiece boss = boss(sortie);
        assertEquals(List.of(0), values(sortie, SimEvents.Type.BOSS_ARRIVED), "it arrives again at once");
        assertEquals(0, boss.phase());
        assertFalse(boss.engaged());
        assertEquals(0, sortie.missileCount(), "no missile left from before");
        assertEquals(120, boss.x(), 1e-9, "its intro beside the ship again");
        assertEquals(1, stride(sortie).passes());
    }

    /** Three walk cycles at 45 px/s. */
    private static final double STRIDE_SECONDS = 3 * StrideArena.STRIDE_PX / 45;

    private static void until(Sortie sortie, SimEvents.Type type, double seconds, int commands, Runnable first) {
        first.run();
        until(sortie, type, seconds, commands);
    }

    /**
     * Round 44 (the user in round 43, W.a: "The first missile hatch is destroyed before the walker has a chance of ever
     * using it"): with hatch armour a pod takes damage only while its hatch is open (the tell and its salvo); shut it is
     * shielded (shots glance off it and it stops the beam, as the front plate); without it a pod is always open.
     */
    @Test
    void withHatchArmourAPodTakesDamageOnlyWhileItsHatchIsOpen() {
        Sortie plain = sortie(true, TestSpecs.RULES.withInvulnerableShip());
        until(plain, SimEvents.Type.BOSS_SETTLED, 8, 0);
        assertFalse(boss(plain).partShielded(LEFT_POD), "without hatch armour a shut pod takes damage");
        assertFalse(boss(plain).partHatchArmoured(LEFT_POD));

        Sortie sortie = sortie(strikingLevel(true), TestSpecs.RULES.withInvulnerableShip());
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        assertTrue(boss.partHatchArmoured(LEFT_POD) && boss.partHatchArmoured(RIGHT_POD));
        assertFalse(boss.partHatchArmoured(HULL), "the hull fires no homing salvo");
        assertFalse(boss.partHatchOpen(LEFT_POD));
        assertTrue(boss.partShielded(LEFT_POD), "shut: armoured");
        assertTrue(boss.partShielded(RIGHT_POD));
        assertFalse(boss.partShielded(HULL));
        assertEquals(List.of(LEFT_POD), until(sortie, SimEvents.Type.POD_HATCH, 6, 0));
        assertFalse(boss.partShielded(LEFT_POD), "open over its tell and salvo: it takes damage");
        assertTrue(boss.partShielded(RIGHT_POD), "the other pod stays shut");
        int open = 0;
        while (boss.partHatchOpen(LEFT_POD)) {
            assertFalse(boss.partShielded(LEFT_POD));
            sortie.step(0);
            open++;
        }
        assertEquals(SimStep.ticks(0.4) + SimStep.ticks(0.2) + 1, open, "the 0.4 s tell and a salvo of 2, 0.2 s apart");
        assertTrue(boss.partShielded(LEFT_POD), "shut again");
    }

    /**
     * Round 44 (W.a: "When both missile hatches are destroyed, the walker is mostly toothless"): the rail lance wakes
     * once both pods are gone; a second later it charges for 1 s (the line from its back through where the ship was),
     * then fires along that line: the ship that stayed is hit once, for its damage; the next charge comes 3.5 s after
     * the last one.
     */
    @Test
    void theRailLanceWakesWithBothPodsGoneAndHitsTheShipThatStays() {
        Sortie sortie = sortie(strikingLevel(false), TestSpecs.RULES);
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        StrideStrikes strikes = stride(sortie).strikes();
        steps(sortie, 2, 0);
        assertEquals(StrideStrikes.LancePhase.IDLE, strikes.lancePhase(), "asleep while the pods live");
        boss.damagePart(LEFT_POD, 1000);
        boss.damagePart(RIGHT_POD, 1000);
        int wake = 0;
        do {
            sortie.step(0);
            wake++;
        } while (values(sortie, SimEvents.Type.LANCE_CHARGE).isEmpty() && wake < SimStep.ticks(3));
        assertEquals(SimStep.ticks(1), wake, "a second after it wakes");
        assertEquals(StrideStrikes.LancePhase.CHARGE, strikes.lancePhase());
        double toX = sortie.ship().x() - strikes.lanceX();
        double toY = sortie.ship().y() - strikes.lanceY();
        double d = Math.sqrt(toX * toX + toY * toY);
        assertEquals(toX / d, strikes.lanceDirX(), 1e-9, "through the ship");
        assertEquals(toY / d, strikes.lanceDirY(), 1e-9);
        assertEquals(boss.y() + 20, strikes.lanceY(), 1e-9, "the lance 20 px behind its centre");
        int charge = 0;
        double lost = 0;
        int hits = 0;
        boolean fired;
        do {
            double before = health(sortie);
            sortie.step(0);
            charge++;
            fired = !values(sortie, SimEvents.Type.LANCE_FIRE).isEmpty();
            hits += values(sortie, SimEvents.Type.LANCE_HIT).size();
            lost += hits > 0 ? before - health(sortie) : 0;
            assertTrue(fired || hits == 0, "the charge does not hurt");
        } while (!fired && charge < SimStep.ticks(2));
        assertEquals(SimStep.ticks(1), charge, "a 1 s charge");
        // The shot's steps after the one it fired in: it hurts in that one and the next ones up to 0.3 s.
        int fire = 0;
        while (strikes.lancePhase() == StrideStrikes.LancePhase.FIRE) {
            double before = health(sortie);
            sortie.step(0);
            fire++;
            int hit = values(sortie, SimEvents.Type.LANCE_HIT).size();
            hits += hit;
            lost += hit > 0 ? before - health(sortie) : 0;
        }
        assertEquals(SimStep.ticks(0.3), fire, "a 0.3 s shot");
        assertEquals(1, hits, "once a shot");
        assertEquals(10, lost, 1e-9, "its damage");
        int next = SimStep.ticks(1) + SimStep.ticks(0.3);
        while (values(sortie, SimEvents.Type.LANCE_CHARGE).isEmpty() && next < SimStep.ticks(6)) {
            sortie.step(0);
            next++;
        }
        assertEquals(SimStep.ticks(3.5), next, "every 3.5 s");
    }

    /** Round 44: the ship that moves off the line during the charge is not hit; in the last phase the lance is awake. */
    @Test
    void aShipThatMovesOffTheLineIsMissedAndTheLastPhaseWakesTheLance() {
        Sortie sortie = sortie(strikingLevel(false), TestSpecs.RULES);
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        boss.damagePart(HULL, HULL_HP * 0.75);
        until(sortie, SimEvents.Type.BOSS_PHASE, 0.1, 0);
        assertEquals(2, boss.phase());
        assertFalse(boss.partWrecked(LEFT_POD) || boss.partWrecked(RIGHT_POD), "the pods live");
        until(sortie, SimEvents.Type.LANCE_CHARGE, 1.5, 0);
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(1.5); i++) {
            sortie.step(Command.RIGHT.bit());
            hits += values(sortie, SimEvents.Type.LANCE_HIT).size();
        }
        assertEquals(0, hits, "moved off its line");
        int next = SimStep.ticks(1.5);
        while (values(sortie, SimEvents.Type.LANCE_CHARGE).isEmpty() && next < SimStep.ticks(6)) {
            sortie.step(0);
            next++;
        }
        assertEquals(SimStep.ticks(2.5), next, "every 2.5 s in its last phase");
    }

    /**
     * Round 44 (W.e: "stomp shockwave"): the ship within 90 px of its centre starts a 0.5 s tell, then the ring expands
     * to 110 px and hits it once; a ship that flies out during the tell is not hit; at most one stomp every 3 s.
     */
    @Test
    void theStompBracesForTheShipCloseByAndItsRingHitsOnce() {
        Sortie sortie = sortie(strikingLevel(false), TestSpecs.RULES);
        until(sortie, SimEvents.Type.BOSS_SETTLED, 8, 0);
        SetPiece boss = boss(sortie);
        StrideStrikes strikes = stride(sortie).strikes();
        steerTo(sortie, 120, 0);
        until(sortie, SimEvents.Type.STOMP_TELL, 15, 0);
        double dx = sortie.ship().x() - boss.x();
        double dy = sortie.ship().y() - boss.y();
        assertTrue(Math.sqrt(dx * dx + dy * dy) <= 90, "within 90 px");
        assertEquals(StrideStrikes.StompPhase.TELL, strikes.stompPhase());
        double before = health(sortie);
        int tell = 0;
        do {
            sortie.step(0);
            tell++;
        } while (values(sortie, SimEvents.Type.STOMP).isEmpty() && tell < SimStep.ticks(2));
        assertEquals(SimStep.ticks(0.5), tell, "a 0.5 s tell");
        assertEquals(before, health(sortie), 1e-9, "the tell does not hurt");
        int hits = values(sortie, SimEvents.Type.STOMP_HIT).size();
        double lost = hits > 0 ? before - health(sortie) : 0;
        double radius = 0;
        while (strikes.stompPhase() == StrideStrikes.StompPhase.RING) {
            radius = strikes.ringRadius();
            double was = health(sortie);
            sortie.step(0);
            int hit = values(sortie, SimEvents.Type.STOMP_HIT).size();
            hits += hit;
            lost += hit > 0 ? was - health(sortie) : 0;
        }
        assertEquals(110, radius, 1e-9, "out to 110 px");
        assertEquals(1, hits, "once a stomp");
        assertEquals(6, lost, 1e-9, "its damage");
        int again = 0;
        while (values(sortie, SimEvents.Type.STOMP_TELL).isEmpty() && again < SimStep.ticks(6)) {
            sortie.step(0);
            again++;
        }
        assertTrue(again >= SimStep.ticks(3) - SimStep.ticks(0.5) - SimStep.ticks(0.3) - 1, "its cooldown: " + again);
        // Flies out during the tell: the ring misses.
        hits = 0;
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.RIGHT.bit());
            hits += values(sortie, SimEvents.Type.STOMP_HIT).size();
        }
        assertEquals(0, hits, "out of its ring");
    }

    @Test
    void theStrikesAreDeterministicAndAllocateNothing() {
        Sortie a = sortie(strikingLevel(true), TestSpecs.RULES);
        Sortie b = sortie(strikingLevel(true), TestSpecs.RULES);
        for (int i = 0; i < SimStep.ticks(60); i++) {
            if (i == SimStep.ticks(9)) {
                for (Sortie s : List.of(a, b)) {
                    s.setPiece(0).damagePart(LEFT_POD, 1000);
                    s.setPiece(0).damagePart(RIGHT_POD, 1000);
                }
            }
            int commands = (i / 50) % 2 == 0 ? Command.LEFT.bit() | FIRE : Command.RIGHT.bit();
            a.step(commands);
            b.step(commands);
        }
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = sortie(strikingLevel(true), TestSpecs.RULES.withInvulnerableShip());
                    steps(sortie, 8, 0);
                    sortie.setPiece(0).damagePart(LEFT_POD, 1000);
                    sortie.setPiece(0).damagePart(RIGHT_POD, 1000);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(40); i++) {
                        sortie.step(FIRE | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                    }
                });
        assertEquals(0, allocated, "the lance and the stomp allocate nothing per step");
    }

    @Test
    void theFightIsDeterministicAndAllocatesNothing() {
        Sortie a = sortie(true, TestSpecs.RULES);
        Sortie b = sortie(true, TestSpecs.RULES);
        for (int i = 0; i < SimStep.ticks(60); i++) {
            int commands = (i / 50) % 2 == 0 ? Command.LEFT.bit() | FIRE : Command.RIGHT.bit();
            a.step(commands);
            b.step(commands);
        }
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = sortie(true, TestSpecs.RULES.withInvulnerableShip());
                    steps(sortie, 8, 0);
                    sortie.setPiece(0).damagePart(HULL, HULL_HP * 0.75);
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(40); i++) {
                        sortie.step(FIRE | ((i / 40) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit()));
                    }
                });
        assertEquals(0, allocated, "the walk, the cone, the hatches and the missiles allocate nothing per step");
    }
}
