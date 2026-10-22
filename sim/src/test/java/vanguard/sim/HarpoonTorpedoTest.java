package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.TestSpecs.INFINITE;
import static vanguard.sim.TestSpecs.muzzle;
import static vanguard.sim.UnderwaterTest.front;
import static vanguard.sim.UnderwaterTest.run;
import static vanguard.sim.UnderwaterTest.sortie;
import static vanguard.sim.UnderwaterTest.under;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M8 part A, step A1 (design/player/weapons/harpoon-torpedoes): the Harpoon Torpedoes are the {@link
 * WeaponSpec.Delivery#TORPEDO} delivery fired from the nose, {@code anti-sub}: 360 → 520 px/s over 0.6 s, range 600 px,
 * turning 90°/s in a 70° cone. Under water it flies at × 0.7, runs 900 px and strikes every target in the water at full
 * damage, its hit splashing half its damage on the units within 20 px; above water (a level over water) it is the
 * Torpedo Pod's: {@code sub} and the naval ground; in a dry level it reads NO WATER.
 */
class HarpoonTorpedoTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double X = Ship.START_X;

    /** The data's L1: 0.8/s, 37.5 damage, from the nose. */
    static final WeaponSpec HARPOON = new WeaponSpec(
                    "harpoon-torpedoes",
                    "torpedo",
                    "torpedo",
                    WeaponSpec.Delivery.TORPEDO,
                    false,
                    0.8,
                    37.5,
                    360,
                    new Hitbox(6, 22),
                    600,
                    INFINITE,
                    1,
                    0,
                    Math.toRadians(90),
                    Math.toRadians(35),
                    0,
                    0,
                    List.of(muzzle(0, 21, 0)),
                    520,
                    0.6,
                    WeaponSpec.Mines.NONE)
            .antiSub(true)
            .splash(20, 0.5);

    static LevelScript units(boolean water, GroundUnit... units) {
        return TorpedoTest.units(water, units);
    }

    static GroundUnit unit(double x, Layer layer) {
        return new GroundUnit(0, x, TorpedoTest.dummy(layer, 100), -1);
    }

    @Test
    void underWaterItLeavesTheNoseAtSevenTenthsOfItsSpeedsAndRunsHalfAgainAsFar() {
        Sortie sortie = sortie(under(units(false)), front(HARPOON));
        assertFalse(sortie.mountIdle(0));
        sortie.step(FIRE);
        Shot harpoon = sortie.shot(0);
        assertEquals(X, harpoon.x(), 1e-9, "from the nose");
        assertEquals(360 * 0.7, harpoon.vy(), 1e-9);
        assertEquals(900, harpoon.range(), 1e-9);
        assertEquals(37.5, harpoon.damage(), "anti-sub: full damage");
        run(sortie, SimStep.ticks(0.6), Command.NONE);
        assertEquals(520 * 0.7, harpoon.vy(), 1e-9);
    }

    @Test
    void itReadsNoWaterInADryLevelAndRunsAsThePodAboveWater() {
        Sortie dry = sortie(units(false), front(HARPOON));
        assertTrue(dry.mountIdle(0));
        run(dry, SimStep.ticks(2), FIRE);
        assertEquals(0, dry.shotCount());

        Sortie surface = sortie(units(true), front(HARPOON));
        surface.step(FIRE);
        assertEquals(360, surface.shot(0).vy(), 1e-9);
        assertEquals(600, surface.shot(0).range(), 1e-9);
        for (Layer layer : List.of(Layer.SUB, Layer.GROUND, Layer.AIR, Layer.LOW_AIR)) {
            Sortie sortie = sortie(units(true, unit(X, layer)), front(HARPOON));
            run(sortie, 30, Command.NONE);
            sortie.step(FIRE);
            boolean reached = layer == Layer.SUB || layer == Layer.GROUND;
            assertEquals(
                    reached ? 1 : 0,
                    TorpedoTest.count(sortie, SimStep.ticks(2), Command.NONE, SimEvents.Type.ENEMY_HIT),
                    layer.name());
            assertEquals(reached ? 62.5 : 100, sortie.enemy(0).hp(), 1e-9, layer.name());
        }
    }

    @Test
    void underWaterItStrikesAFlyerAndSplashesHalfItsDamageWithinTwentyPixels() {
        // Two flyers side by side (2 px between their boxes) and one well off.
        Sortie sortie = sortie(
                under(units(false, unit(X, Layer.AIR), unit(X + 22, Layer.AIR), unit(X + 70, Layer.AIR))),
                front(HARPOON));
        run(sortie, 30, Command.NONE);
        sortie.step(FIRE);
        assertEquals(1, TorpedoTest.count(sortie, SimStep.ticks(2), Command.NONE, SimEvents.Type.ENEMY_HIT));
        double[] hp = new double[3];
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double dx = enemy.x() - X;
            hp[dx < 11 ? 0 : dx < 40 ? 1 : 2] = enemy.hp();
        }
        assertEquals(100 - 37.5, hp[0], 1e-9, "the struck flyer");
        assertEquals(100 - 18.75, hp[1], 1e-9, "its neighbour: half");
        assertEquals(100, hp[2], "out of the splash");
    }

    @Test
    void aHarpoonWithoutASplashHurtsOnlyWhatItStrikes() {
        WeaponSpec plain = HARPOON.splash(0, 0);
        Sortie sortie = sortie(under(units(false, unit(X, Layer.AIR), unit(X + 22, Layer.AIR))), front(plain));
        run(sortie, 30, Command.NONE);
        sortie.step(FIRE);
        run(sortie, SimStep.ticks(2), Command.NONE);
        double total = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            total += 100 - sortie.enemy(i).hp();
        }
        assertEquals(37.5, total, 1e-9);
    }
}
