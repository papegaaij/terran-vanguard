package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.TestSpecs.INFINITE;
import static vanguard.sim.TestSpecs.muzzle;
import static vanguard.sim.UnderwaterTest.run;
import static vanguard.sim.UnderwaterTest.sortie;
import static vanguard.sim.UnderwaterTest.under;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M8 part A, step A1, reworked in M8 AW (design/player/weapons/depth-charges; the user, 2026-10-23): a {@link
 * WeaponSpec.Delivery#DEPTH_CHARGE} barrel is tossed forward from the stern rack at 380 px/s (under water × 0.7 × 1.5
 * = 399 px/s, decided D12), slows to a hover about 200 px ahead over 1 s, sinks with the scroll and bursts on its 1.4 s
 * fuse or, under water, on touching a swimmer (a unit on {@code air} or {@code low-air}, a Lamprey latched on the hull
 * included), dealing its damage once to everything in its 48 px radius in the water (never {@code high-air}, never the
 * ship); above water it bursts on its fuse only, on {@code sub} and the naval ground; dry, it reads NO WATER. Hardened
 * targets glance off it; it damages nothing on its way.
 */
class DepthChargeTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double X = Ship.START_X;
    private static final double SCROLL = 130;
    private static final int FUSE = SimStep.ticks(1.4);

    /** The data's L1: 1 volley/s, 28 damage, 48 px, tossed forward from the stern; at most {@code live} out. */
    static WeaponSpec charges(int live) {
        return new WeaponSpec(
                        "depth-charges",
                        "depth-charge",
                        "depth-charge",
                        WeaponSpec.Delivery.DEPTH_CHARGE,
                        false,
                        1,
                        28,
                        380,
                        new Hitbox(12, 12),
                        INFINITE,
                        INFINITE,
                        1,
                        48,
                        0,
                        0,
                        0,
                        0,
                        List.of(muzzle(0, -20, 0)),
                        380,
                        0,
                        WeaponSpec.Mines.charge(1.0, 1.4, live))
                .antiSub(true);
    }

    static final WeaponSpec CHARGES = charges(8);

    static Armament.Mount rear(WeaponSpec weapon) {
        return new Armament.Mount(Armament.Slot.REAR, weapon, weapon);
    }

    /** A 160 × 200 px target on {@code layer} entering at 1 s above the ship's column. */
    static LevelScript target(boolean water, Layer layer) {
        return TorpedoTest.units(
                water, new GroundUnit(1, X, PartASpecs.dummy(layer, 1000, new Hitbox(160, 200), false), -1));
    }

    /**
     * The ship holds for 2.5 s (the target 300 px ahead, coming down), tosses one charge and holds; returns the step
     * after the toss in which it burst (1 is the first) from its {@link SimEvents.Type#BLAST}, after checking the target
     * lay inside the burst then; -1 without a burst in 2 s.
     */
    static int tossOne(Sortie sortie) {
        run(sortie, SimStep.ticks(2.5), Command.NONE);
        sortie.step(FIRE);
        for (int i = 1; i <= SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.BLAST) {
                    for (int j = 0; j < sortie.enemyCount(); j++) {
                        Enemy enemy = sortie.enemy(j);
                        assertTrue(
                                ShotGeometry.inBlast(
                                        events.x(e), events.y(e), 48, enemy.x(), enemy.y(), enemy.hitbox()),
                                "the target lies in the burst");
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    @Test
    void underWaterItIsTossedForwardHoversAboutTwoHundredPxAheadSinksAndBurstsOnItsFuse() {
        Sortie sortie = sortie(under(TorpedoTest.units(false)), rear(CHARGES));
        assertFalse(sortie.mountIdle(0));
        run(sortie, 5, Command.NONE);
        double launch = sortie.ship().y() - 20;
        sortie.step(FIRE);
        Shot charge = sortie.shot(0);
        assertEquals(WeaponSpec.Delivery.DEPTH_CHARGE, charge.weapon().delivery());
        assertEquals(380 * 0.7 * 1.5, charge.vy(), 1e-9, "399 px/s forward");
        assertEquals(0, charge.vx(), 1e-9);
        assertEquals(28, charge.damage(), "anti-sub: full damage");
        int bursts = 0;
        double before = charge.y();
        for (int i = 1; i < FUSE + 5 && bursts == 0; i++) {
            sortie.step(Command.NONE);
            bursts += sortie.events().count(SimEvents.Type.BLAST);
            if (i == SimStep.ticks(1.0)) {
                assertEquals(200, charge.y() - launch, 8, "it hovers about 200 px ahead of the stern rack");
            }
            if (bursts == 0 && i > SimStep.ticks(1.0)) {
                assertEquals(-SCROLL * SimStep.SECONDS, charge.y() - before, 1e-9, "it sinks with the scroll");
            }
            before = charge.y();
            if (bursts > 0) {
                assertEquals(FUSE - 1, i, "it bursts on its fuse, counted from its launch");
            }
        }
        assertEquals(1, bursts);
        assertEquals(0, sortie.shotCount(), "spent");
    }

    @Test
    void underWaterItBurstsOnTouchingASwimmerAndItsBurstHitsEveryLayerInTheWaterButTheIceCeiling() {
        for (Layer layer : List.of(Layer.AIR, Layer.LOW_AIR, Layer.GROUND, Layer.SUB, Layer.HIGH_AIR)) {
            Sortie sortie = sortie(under(target(false, layer)), rear(CHARGES));
            int at = tossOne(sortie);
            boolean swimmer = layer == Layer.AIR || layer == Layer.LOW_AIR;
            if (swimmer) {
                assertTrue(at > 0 && at < FUSE - 1, layer + ": it burst on touching it, before its fuse: " + at);
            } else {
                assertEquals(FUSE - 1, at, layer + ": not a swimmer, its fuse");
            }
            assertEquals(layer == Layer.HIGH_AIR ? 1000 : 972, sortie.enemy(0).hp(), 1e-9, layer.name());
        }
    }

    @Test
    void aChargeTossedWithALampreyOnTheHullBurstsOnItAtOnceAndNeverHitsTheShip() {
        WaveSpec lamprey = TestSpecs.wave(
                0, WaveSpec.Formation.SINGLE, LampreyTest.LAMPREY, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        Sortie sortie = new Sortie(
                1,
                TestSpecs.loadout(rear(CHARGES)),
                under(TestSpecs.level(30, List.of(lamprey))),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        for (int i = 0; sortie.latched() == 0; i++) {
            assertTrue(i < 600, "it latches");
            sortie.step(Command.NONE);
        }
        Enemy latched = sortie.enemy(0);
        assertTrue(latched.latched());
        assertTrue(Math.abs(latched.x() - sortie.ship().x()) > 10, "on a side spot, off the barrel's path");
        sortie.step(FIRE);
        int bursts = sortie.events().count(SimEvents.Type.BLAST);
        if (bursts == 0) {
            sortie.step(Command.NONE);
            bursts = sortie.events().count(SimEvents.Type.BLAST);
        }
        assertEquals(1, bursts, "it bursts over the hull at once");
        assertEquals(0, sortie.latched(), "the Lamprey (3 HP) is gone");
        assertEquals(TestSpecs.FULL_ARMOUR, sortie.ship().defences().armour(), "never the ship");
    }

    @Test
    void aboveWaterItBurstsOnItsFuseOnlyOnTheSubLayerAndTheNavalGround() {
        Sortie surface = sortie(TorpedoTest.units(true), rear(CHARGES));
        assertFalse(surface.mountIdle(0));
        run(surface, 5, Command.NONE);
        surface.step(FIRE);
        assertEquals(380, surface.shot(0).vy(), 1e-9, "forward");
        for (Layer layer : List.of(Layer.SUB, Layer.GROUND, Layer.AIR, Layer.LOW_AIR, Layer.HIGH_AIR)) {
            Sortie sortie = sortie(target(true, layer), rear(CHARGES));
            assertEquals(FUSE - 1, tossOne(sortie), layer + ": nothing swims above the water, its fuse");
            boolean reached = layer == Layer.SUB || layer == Layer.GROUND;
            assertEquals(reached ? 972 : 1000, sortie.enemy(0).hp(), 1e-9, layer.name());
        }
    }

    @Test
    void dryItReadsNoWaterAndTossesNothing() {
        Sortie dry = sortie(TorpedoTest.units(false), rear(CHARGES));
        assertTrue(dry.mountIdle(0));
        run(dry, SimStep.ticks(2), FIRE);
        assertEquals(0, dry.shotCount());
    }

    @Test
    void itDamagesNothingOnItsWayAndHardenedTargetsGlanceOffIt() {
        // A unit on the sea floor under its path: the barrel passes over it and only its burst hurts it.
        Sortie sortie = sortie(under(target(false, Layer.GROUND)), rear(CHARGES));
        run(sortie, SimStep.ticks(2.5), Command.NONE);
        sortie.step(FIRE);
        int hits = 0;
        for (int i = 0; i < FUSE - 2; i++) {
            sortie.step(Command.NONE);
            hits += sortie.events().count(SimEvents.Type.ENEMY_HIT);
        }
        assertEquals(0, hits);
        assertEquals(1000, sortie.enemy(0).hp());

        Sortie bunker = sortie(
                under(TorpedoTest.units(false, new GroundUnit(1, X, PartCSpecs.hardenedTarget(100), -1))),
                rear(CHARGES));
        run(bunker, SimStep.ticks(2.5), Command.NONE);
        bunker.step(FIRE);
        assertEquals(1, TorpedoTest.count(bunker, SimStep.ticks(1.5), Command.NONE, SimEvents.Type.SHOT_GLANCED));
        assertEquals(100, bunker.enemy(0).hp());
    }

    @Test
    void atMostItsLiveChargesAreOut() {
        Sortie sortie = sortie(under(TorpedoTest.units(false)), rear(charges(1)));
        run(sortie, 5, Command.NONE);
        int tossed = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(FIRE);
            tossed += sortie.events().count(SimEvents.Type.SHOT_FIRED);
            assertTrue(sortie.shotCount() <= 1);
        }
        // 1 a second, but each is out for its 1.4 s fuse: the cap holds it to one a fuse (0, 1.4 and 2.8 s).
        assertEquals(3, tossed);
    }
}
