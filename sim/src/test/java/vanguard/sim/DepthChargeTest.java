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
 * M8 part A, step A1 (design/player/weapons/depth-charges): a {@link WeaponSpec.Delivery#DEPTH_CHARGE} barrel leaves
 * the stern at 140 px/s backwards (under water × 0.7 × 1.5 = 147 px/s, decided D12), slows to a hover over 0.6 s,
 * sinks with the scroll and bursts on its 0.9 s fuse, dealing its damage once to everything in its 40 px radius in the
 * water (never {@code high-air}); above water it bursts on {@code sub} and the naval ground; dry, it reads NO WATER.
 * Hardened targets glance off it; it hits nothing on its way.
 */
class DepthChargeTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double X = Ship.START_X;
    private static final double SCROLL = 130;

    /** The data's L1: 1 volley/s, 25 damage, 40 px, from the stern; at most {@code live} out. */
    static WeaponSpec charges(int live) {
        return new WeaponSpec(
                        "depth-charges",
                        "depth-charge",
                        "depth-charge",
                        WeaponSpec.Delivery.DEPTH_CHARGE,
                        false,
                        1,
                        25,
                        140,
                        new Hitbox(10, 10),
                        INFINITE,
                        INFINITE,
                        1,
                        40,
                        0,
                        0,
                        0,
                        0,
                        List.of(muzzle(0, -20, 180)),
                        140,
                        0,
                        WeaponSpec.Mines.charge(0.6, 0.9, live))
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
     * The ship climbs for 1.5 s, drops one charge and holds; returns the burst's place (x, y) from its {@link
     * SimEvents.Type#BLAST}, after checking the target lay inside it then; NaN without a burst in 2 s.
     */
    static double[] dropOne(Sortie sortie) {
        run(sortie, SimStep.ticks(1.5), Command.UP.bit());
        sortie.step(FIRE);
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.BLAST) {
                    for (int j = 0; j < sortie.enemyCount(); j++) {
                        Enemy enemy = sortie.enemy(j);
                        assertTrue(
                                PlayerFire.inBlast(events.x(e), events.y(e), 40, enemy.x(), enemy.y(), enemy.hitbox()),
                                "the target lies in the burst");
                    }
                    return new double[] {events.x(e), events.y(e)};
                }
            }
        }
        return new double[] {Double.NaN, Double.NaN};
    }

    @Test
    void underWaterItRollsOutBackwardsHoversSinksWithTheScrollAndBurstsOnItsFuse() {
        Sortie sortie = sortie(under(TorpedoTest.units(false)), rear(CHARGES));
        assertFalse(sortie.mountIdle(0));
        run(sortie, SimStep.ticks(1.5), Command.UP.bit());
        sortie.step(FIRE);
        Shot charge = sortie.shot(0);
        assertEquals(WeaponSpec.Delivery.DEPTH_CHARGE, charge.weapon().delivery());
        assertEquals(-140 * 0.7 * 1.5, charge.vy(), 1e-9, "147 px/s backwards");
        assertEquals(25, charge.damage(), "anti-sub: full damage");
        int fused = SimStep.ticks(0.9);
        int bursts = 0;
        double before = charge.y();
        for (int i = 1; i < fused + 5 && bursts == 0; i++) {
            sortie.step(Command.NONE);
            bursts += sortie.events().count(SimEvents.Type.BLAST);
            if (bursts == 0 && i > SimStep.ticks(0.6)) {
                assertEquals(-SCROLL * SimStep.SECONDS, charge.y() - before, 1e-9, "it sinks with the scroll");
            }
            before = charge.y();
            if (bursts > 0) {
                assertEquals(fused - 1, i, "it bursts on its fuse, counted from its launch");
            }
        }
        assertEquals(1, bursts);
        assertEquals(0, sortie.shotCount(), "spent");
    }

    @Test
    void underWaterTheBurstHitsEveryLayerInTheWaterButTheIceCeiling() {
        for (Layer layer : List.of(Layer.AIR, Layer.LOW_AIR, Layer.GROUND, Layer.SUB, Layer.HIGH_AIR)) {
            Sortie sortie = sortie(under(target(false, layer)), rear(CHARGES));
            double[] at = dropOne(sortie);
            assertFalse(Double.isNaN(at[0]), layer + ": it burst");
            assertEquals(layer == Layer.HIGH_AIR ? 1000 : 975, sortie.enemy(0).hp(), 1e-9, layer.name());
        }
    }

    @Test
    void aboveWaterItBurstsOnTheSubLayerAndTheNavalGroundOnly() {
        Sortie surface = sortie(TorpedoTest.units(true), rear(CHARGES));
        assertFalse(surface.mountIdle(0));
        run(surface, 5, Command.NONE);
        surface.step(FIRE);
        assertEquals(-140, surface.shot(0).vy(), 1e-9);
        for (Layer layer : List.of(Layer.SUB, Layer.GROUND, Layer.AIR, Layer.LOW_AIR, Layer.HIGH_AIR)) {
            Sortie sortie = sortie(target(true, layer), rear(CHARGES));
            double[] at = dropOne(sortie);
            assertFalse(Double.isNaN(at[0]), layer + ": it burst");
            boolean reached = layer == Layer.SUB || layer == Layer.GROUND;
            assertEquals(reached ? 975 : 1000, sortie.enemy(0).hp(), 1e-9, layer.name());
        }
    }

    @Test
    void dryItReadsNoWaterAndDropsNothing() {
        Sortie dry = sortie(TorpedoTest.units(false), rear(CHARGES));
        assertTrue(dry.mountIdle(0));
        run(dry, SimStep.ticks(2), FIRE);
        assertEquals(0, dry.shotCount());
    }

    @Test
    void itHitsNothingOnItsWayAndHardenedTargetsGlanceOffIt() {
        // A flyer right behind the stern: the barrel rolls through it and only its burst hurts it.
        Sortie sortie = sortie(under(target(false, Layer.AIR)), rear(CHARGES));
        run(sortie, SimStep.ticks(1.5), Command.UP.bit());
        sortie.step(FIRE);
        int hits = 0;
        for (int i = 0; i < SimStep.ticks(0.8); i++) {
            sortie.step(Command.NONE);
            hits += sortie.events().count(SimEvents.Type.ENEMY_HIT);
        }
        assertEquals(0, hits);
        assertEquals(1000, sortie.enemy(0).hp());

        Sortie bunker = sortie(
                under(TorpedoTest.units(false, new GroundUnit(1, X, PartCSpecs.hardenedTarget(100), -1))),
                rear(CHARGES));
        run(bunker, SimStep.ticks(1.5), Command.UP.bit());
        bunker.step(FIRE);
        assertEquals(1, TorpedoTest.count(bunker, SimStep.ticks(1), Command.NONE, SimEvents.Type.SHOT_GLANCED));
        assertEquals(100, bunker.enemy(0).hp());
    }

    @Test
    void atMostItsLiveChargesAreOut() {
        Sortie sortie = sortie(under(TorpedoTest.units(false)), rear(charges(1)));
        run(sortie, SimStep.ticks(1.5), Command.UP.bit());
        int dropped = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(FIRE);
            dropped += sortie.events().count(SimEvents.Type.SHOT_FIRED);
            assertTrue(sortie.shotCount() <= 1);
        }
        // 1 a second, each out for its 0.9 s fuse: the cap does not slow it here.
        assertEquals(3, dropped);
    }
}
