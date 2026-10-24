package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.TestSpecs.INFINITE;
import static vanguard.sim.TestSpecs.loadout;
import static vanguard.sim.TestSpecs.muzzle;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M8 part A, step A1 (design/world/europa, Under water rules; m8a-gaps.md §1.1): the {@link Medium} and its factors.
 * Under water every bullet (the ship's, Rook's, the drones', the enemies' bullets and missiles) flies at × 0.7 from
 * its spawn, a weapon without {@code anti-sub} does × 0.7 once (after {@code anti-ground}'s ×2), the ship and Rook
 * accelerate and brake at × 0.6, torpedo-type weapons run × 1.5 and torpedoes reach every layer in the water but
 * {@code high-air}; a crossing eases the factors over 1 s; the air changes nothing. M8 AW (the user, 2026-10-23): the
 * energy weapons (the beam and the lances) do × 0.2 in place of × 0.7 and reach 0.4 of the field's height; in the
 * Sonar Pulse's window every weapon does full damage.
 */
class UnderwaterTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double X = Ship.START_X;
    /** The torpedo pod's muzzle (TorpedoTest's left pod). */
    private static final double POD_X = Ship.START_X - 16;

    /** The approved numbers (design/world/europa/README.md, Under water rules, Q1 = b). */
    static final MediumSpec.Rules EUROPA = new MediumSpec.Rules(
            0.7,
            0.6,
            0.7,
            0.2,
            0.4,
            1.5,
            1,
            Set.of("ion-beam", "lance-laser", "rear-lance"),
            Set.of("torpedo-pod", "harpoon-torpedoes", "depth-charges"));

    static WeaponSpec bolt(String slug, boolean antiGround, double range) {
        return new WeaponSpec(
                slug,
                "pulse",
                "pulse",
                WeaponSpec.Delivery.BOLT,
                antiGround,
                4,
                10,
                600,
                new Hitbox(4, 10),
                range,
                INFINITE,
                1,
                0,
                0,
                Math.PI,
                0,
                0,
                List.of(muzzle(0, 21, 0)));
    }

    static final WeaponSpec BOLT = bolt("pulse-laser", false, INFINITE);

    static Sortie sortie(LevelScript level, Armament.Mount... mounts) {
        return new Sortie(1, loadout(mounts), level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    static Armament.Mount front(WeaponSpec weapon) {
        return new Armament.Mount(Armament.Slot.FRONT, weapon, weapon);
    }

    /** {@code level} under water from its start. */
    static LevelScript under(LevelScript level) {
        return level.toBuilder().medium(MediumSpec.underwater(EUROPA)).build();
    }

    /** Units coming down with the scroll (130 px/s) from the top edge, over land. */
    static LevelScript units(GroundUnit... units) {
        return TorpedoTest.units(false, units);
    }

    static void run(Sortie sortie, int steps, int commands) {
        TorpedoTest.run(sortie, steps, commands);
    }

    @Test
    void theAirScalesNothingAndHashesAsBefore() {
        Medium air = Medium.air();
        assertFalse(air.wet());
        assertFalse(air.underwater());
        assertEquals(1, air.bulletSpeed());
        assertEquals(1, air.acceleration());
        assertEquals(1, air.damage(BOLT));
        assertEquals(1, air.reach(PartASpecs.ionBeam(1)));
        assertEquals(1, air.torpedoRange(TorpedoTest.TORPEDO));

        LevelScript level = units(new GroundUnit(1, X, TorpedoTest.dummy(Layer.AIR, 500), -1));
        // The water's rules without a switch to it: the air all the same.
        LevelScript ruled =
                level.toBuilder().medium(new MediumSpec(EUROPA, List.of())).build();
        Sortie plain = sortie(level, front(BOLT));
        Sortie same = sortie(ruled, front(BOLT));
        for (int i = 0; i < 300; i++) {
            plain.step(FIRE);
            same.step(FIRE);
            assertEquals(plain.stateHash(), same.stateHash(), "step " + i);
        }
        assertFalse(same.underwater());
        assertEquals(0, same.waterBlend());
    }

    @Test
    void aSwitchIsInTimeOrderBeforeTheLevelsEnd() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediumSpec(EUROPA, List.of(new MediumSpec.Switch(5, true), new MediumSpec.Switch(5, false))));
        assertThrows(IllegalArgumentException.class, () -> units().toBuilder()
                .medium(new MediumSpec(EUROPA, List.of(new MediumSpec.Switch(60, true))))
                .build());
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediumSpec.Rules(0, 0.6, 0.7, 0.2, 0.4, 1.5, 1, Set.of(), Set.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediumSpec.Rules(0.7, 0.6, 0.7, 0, 0.4, 1.5, 1, Set.of(), Set.of()));
    }

    @Test
    void underWaterAShotFliesAtSevenTenthsAndDoesSevenTenthsOfItsDamage() {
        Sortie air = sortie(units(), front(BOLT));
        Sortie water = sortie(under(units()), front(BOLT));
        run(air, 10, Command.NONE);
        run(water, 10, Command.NONE);
        air.step(FIRE);
        water.step(FIRE);
        assertEquals(600, air.shot(0).vy(), 1e-9);
        assertEquals(10, air.shot(0).damage());
        assertEquals(1, air.shot(0).speedFactor());
        assertEquals(600 * 0.7, water.shot(0).vy(), 1e-9);
        assertEquals(7, water.shot(0).damage(), 1e-12);
        assertEquals(0.7, water.shot(0).speedFactor(), 1e-12);
        double y = water.shot(0).y();
        water.step(Command.NONE);
        assertEquals(420 * SimStep.SECONDS, water.shot(0).y() - y, 1e-9, "a step at 420 px/s");
    }

    @Test
    void antiGroundsDoubleAndTheWatersShareLandOnceAndAnAntiSubWeaponDoesFullDamage() {
        // One shot each into a 100 HP unit on the ground: 10 × 2 × 0.7 = 14 for an anti-ground bolt.
        record Case(WeaponSpec weapon, Layer layer, double damage) {}
        for (Case c : List.of(
                new Case(bolt("pulse-laser", true, INFINITE), Layer.GROUND, 14),
                new Case(BOLT, Layer.GROUND, 7),
                new Case(BOLT, Layer.AIR, 7),
                new Case(BOLT.antiSub(true), Layer.AIR, 10),
                new Case(BOLT.antiSub(true), Layer.SUB, 10))) {
            Sortie sortie = sortie(
                    under(units(new GroundUnit(0, X, TorpedoTest.dummy(c.layer(), 100), -1))), front(c.weapon()));
            run(sortie, 30, Command.NONE);
            sortie.step(FIRE);
            assertEquals(
                    1,
                    TorpedoTest.count(sortie, SimStep.ticks(2), Command.NONE, SimEvents.Type.ENEMY_HIT),
                    c.toString());
            assertEquals(100 - c.damage(), sortie.enemy(0).hp(), 1e-9, c.toString());
        }
        // A plain bolt still passes over the sub layer under water.
        Sortie sub = sortie(under(units(new GroundUnit(0, X, TorpedoTest.dummy(Layer.SUB, 100), -1))), front(BOLT));
        run(sub, 30, Command.NONE);
        sub.step(FIRE);
        assertEquals(0, TorpedoTest.count(sub, SimStep.ticks(2), Command.NONE, SimEvents.Type.ENEMY_HIT));
    }

    @Test
    void theEnemiesBulletsAndMissilesFlyAtSevenTenthsFromTheirSpawn() {
        Sortie air = sortie(units(), front(BOLT));
        Sortie water = sortie(under(units()), front(BOLT));
        run(air, 5, Command.NONE);
        run(water, 5, Command.NONE);
        air.fireEnemyBullet(X, 400, 30, -100, 4);
        water.fireEnemyBullet(X, 400, 30, -100, 4);
        assertEquals(-100, air.bullet(0).vy(), 1e-12);
        assertEquals(-70, water.bullet(0).vy(), 1e-9);
        assertEquals(21, water.bullet(0).vx(), 1e-9);
        assertEquals(4, water.bullet(0).damage(), "the enemies' damage is untouched");

        EnemyGun pods = RevenantWalkerSimTest.salvo(2, 1, 4);
        Medium under = new Medium(MediumSpec.underwater(EUROPA));
        Missiles missiles = new Missiles(new SimEvents(16));
        missiles.medium(under);
        assertTrue(missiles.launch(X, 400, pods, X, 0));
        Missile missile = missiles.get(0);
        double y = missile.y();
        missiles.fly(X, 0);
        assertEquals(110 * 0.7 * SimStep.SECONDS, y - missile.y(), 1e-9);
    }

    @Test
    void underWaterTheShipAcceleratesAndBrakesAtSixTenths() {
        Sortie air = sortie(units(), front(BOLT));
        Sortie water = sortie(under(units()), front(BOLT));
        run(air, 5, Command.NONE);
        run(water, 5, Command.NONE);
        int right = Command.RIGHT.bit();
        air.step(right);
        water.step(right);
        double step = TestSpecs.SHIP.speed() / TestSpecs.SHIP.accelerationSeconds() * SimStep.SECONDS;
        assertEquals(step, air.ship().vx(), 1e-9);
        assertEquals(step * 0.6, water.ship().vx(), 1e-9);
        run(water, 30, right);
        assertEquals(TestSpecs.SHIP.speed(), water.ship().vx(), 1e-9, "the top speed is unchanged");
        water.step(Command.NONE);
        double stop = TestSpecs.SHIP.speed() / TestSpecs.SHIP.stopSeconds() * SimStep.SECONDS;
        assertEquals(TestSpecs.SHIP.speed() - stop * 0.6, water.ship().vx(), 1e-9);
    }

    @Test
    void rooksGunsDoSevenTenthsAndHisCraftTakesTheDrag() {
        LevelScript level = TestSpecs.level(30, List.of());
        Supplier<Sortie> air = () -> WingmanTest.sortie(level, WingmanSpec.Side.LEFT);
        Sortie water = WingmanTest.sortie(under(level), WingmanSpec.Side.LEFT);
        Sortie dry = air.get();
        run(water, 120, FIRE);
        run(dry, 120, FIRE);
        boolean seen = false;
        for (int i = 0; i < water.shotCount(); i++) {
            Shot shot = water.shot(i);
            if (shot.mount() == water.wingmanMount()) {
                assertEquals(WingmanTest.GUN.damage() * 0.7, shot.damage(), 1e-12);
                assertEquals(0.7, shot.speedFactor(), 1e-12);
                seen = true;
            }
        }
        assertTrue(seen, "Rook fired");
        // He flies to his slot from the same start: slower off the mark under water.
        Sortie a = air.get();
        Sortie w = WingmanTest.sortie(under(level), WingmanSpec.Side.LEFT);
        run(a, 5, Command.RIGHT.bit());
        run(w, 5, Command.RIGHT.bit());
        Wingman rookAir = a.wingman().orElseThrow();
        Wingman rookWater = w.wingman().orElseThrow();
        assertTrue(
                Math.abs(rookWater.renderX(1) - rookWater.renderX(0))
                        < Math.abs(rookAir.renderX(1) - rookAir.renderX(0)),
                "he answers the stick more slowly");
    }

    @Test
    void theBeamReachesTwoFifthsOfTheFieldAndBurnsAtOneFifth() {
        Sortie empty = sortie(under(units()), PartASpecs.beamMount(1));
        run(empty, 60, FIRE);
        Beam beam = empty.beam(0);
        assertEquals(Beam.Contact.NONE, beam.contact());
        assertEquals(0.4 * PlayField.HEIGHT, beam.endY() - beam.fromY(), 1e-9);

        LevelScript level = units(new GroundUnit(0, X, TorpedoTest.dummy(Layer.AIR, 1000), -1));
        Sortie air = sortie(level, PartASpecs.beamMount(1));
        Sortie water = sortie(under(level), PartASpecs.beamMount(1));
        run(air, SimStep.ticks(2), FIRE);
        run(water, SimStep.ticks(2), FIRE);
        double airHp = air.enemy(0).hp();
        double waterHp = water.enemy(0).hp();
        run(air, 60, FIRE);
        run(water, 60, FIRE);
        assertEquals(PartASpecs.BEAM_DPS[1], airHp - air.enemy(0).hp(), 1e-6);
        assertEquals(PartASpecs.BEAM_DPS[1] * 0.2, waterHp - water.enemy(0).hp(), 1e-6);
    }

    @Test
    void theEnergyFactorReplacesTheGunsAndTheSonarPulsesWindowLiftsBoth() {
        Medium under = new Medium(MediumSpec.underwater(EUROPA));
        WeaponSpec beam = PartASpecs.ionBeam(3);
        WeaponSpec lance = bolt("lance-laser", false, INFINITE);
        WeaponSpec rearLance = bolt("rear-lance", false, INFINITE);
        for (WeaponSpec energy : List.of(beam, lance, rearLance)) {
            assertEquals(0.2, under.damage(energy), 1e-12, energy.slug() + ": × 0.2, not × 0.7 × 0.2");
            assertEquals(0.4, under.reach(energy), 1e-12, energy.slug());
        }
        assertEquals(0.7, under.damage(BOLT), 1e-12, "a gun keeps × 0.7");
        assertEquals(1, under.reach(BOLT));
        assertEquals(1, under.damage(BOLT.antiSub(true)));
        under.sonar(true);
        for (WeaponSpec weapon : List.of(beam, lance, rearLance, BOLT)) {
            assertEquals(1, under.damage(weapon), weapon.slug() + ": full damage in the window");
        }
        assertEquals(0.4, under.reach(beam), 1e-12, "the window leaves the reach");
        Medium air = Medium.air();
        assertEquals(1, air.damage(beam), "nothing changes in the air");
        assertEquals(1, air.reach(beam));
    }

    @Test
    void aLancesBoltsGoNoFartherThanTheBeamsReach() {
        Sortie water = sortie(under(units()), front(bolt("lance-laser", false, INFINITE)));
        run(water, 5, Command.NONE);
        water.step(FIRE);
        Shot lance = water.shot(0);
        assertEquals(0.4 * PlayField.HEIGHT, lance.range(), 1e-9);
        assertEquals(2, lance.damage(), 1e-12, "an energy weapon: × 0.2");
        double startY = lance.y();
        double lastY = startY;
        while (water.shotCount() > 0) {
            lastY = water.shot(0).y();
            water.step(Command.NONE);
        }
        // It flew its first step in the step it was fired.
        assertEquals(0.4 * PlayField.HEIGHT, lastY - startY, 2 * 420 * SimStep.SECONDS + 1e-9);
        Sortie bolt = sortie(under(units()), front(BOLT));
        bolt.step(FIRE);
        assertEquals(INFINITE, bolt.shot(0).range(), "a weapon not named in the rules flies to the edge");
    }

    @Test
    void underWaterATorpedoRunsHalfAgainAsFarAndStrikesFlyersButNotTheIceCeiling() {
        WeaponSpec pod = TorpedoTest.TORPEDO.antiSub(true);
        Sortie water = TorpedoTest.sortie(under(units()), pod);
        assertFalse(water.mountIdle(0), "under water the pod runs without a water level");
        water.step(FIRE);
        assertEquals(750, water.shot(0).range(), 1e-9);
        assertEquals(300 * 0.7, water.shot(0).vy(), 1e-9);

        for (Layer layer : List.of(Layer.AIR, Layer.LOW_AIR, Layer.GROUND, Layer.SUB)) {
            Sortie sortie =
                    TorpedoTest.sortie(under(units(new GroundUnit(0, POD_X, TorpedoTest.dummy(layer, 100), -1))), pod);
            run(sortie, 30, Command.NONE);
            sortie.step(FIRE);
            assertEquals(
                    1,
                    TorpedoTest.count(sortie, SimStep.ticks(2), Command.NONE, SimEvents.Type.ENEMY_HIT),
                    layer.name());
            assertEquals(100 - 12.5, sortie.enemy(0).hp(), 1e-9, layer + ": anti-sub, full damage");
        }
        Sortie ice = TorpedoTest.sortie(
                under(units(new GroundUnit(0, POD_X, TorpedoTest.dummy(Layer.HIGH_AIR, 100), -1))), pod);
        run(ice, 30, Command.NONE);
        ice.step(FIRE);
        assertEquals(0, TorpedoTest.count(ice, SimStep.ticks(2), Command.NONE, SimEvents.Type.ENEMY_HIT));
    }

    @Test
    void steppingUnderWaterWithEveryWaterWeaponAndRookDoesNotAllocate() {
        // Waterlines at 7, 12 and 17 s: the eases run while it is measured.
        LevelScript level = units(
                        new GroundUnit(2, X, TorpedoTest.dummy(Layer.AIR, 400), -1),
                        new GroundUnit(4, X + 30, TorpedoTest.dummy(Layer.SUB, 400), -1),
                        new GroundUnit(6, X - 40, TorpedoTest.dummy(Layer.GROUND, 400), -1),
                        new GroundUnit(8, X + 10, TorpedoTest.dummy(Layer.LOW_AIR, 400), -1),
                        new GroundUnit(10, X, TorpedoTest.dummy(Layer.AIR, 400), -1),
                        new GroundUnit(14, X - 20, TorpedoTest.dummy(Layer.SUB, 400), -1))
                .toBuilder()
                .medium(new MediumSpec(
                        EUROPA,
                        List.of(
                                new MediumSpec.Switch(0, true),
                                new MediumSpec.Switch(7, false),
                                new MediumSpec.Switch(12, true),
                                new MediumSpec.Switch(17, false))))
                .build();
        WeaponSpec lance = bolt("lance-laser", false, INFINITE);
        Supplier<Sortie> prepare = () -> new Sortie(
                3,
                loadout(
                                front(HarpoonTorpedoTest.HARPOON),
                                DepthChargeTest.rear(DepthChargeTest.CHARGES),
                                new Armament.Mount(Armament.Slot.LEFT_WING, lance, lance),
                                new Armament.Mount(Armament.Slot.RIGHT_WING, TorpedoTest.TORPEDO, TorpedoTest.TORPEDO))
                        .withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = prepare.get();
                    run(sortie, 300, FIRE | Command.UP.bit());
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < 1200; i++) {
                        sortie.step(FIRE);
                        if (i % 30 == 0) {
                            sortie.fireEnemyBullet(X, 500, 0, -120, 4);
                        }
                    }
                });
        assertEquals(0, allocated, "1200 steps allocated " + allocated + " bytes");

        Sortie beam = sortie(
                under(units(new GroundUnit(1, X, TorpedoTest.dummy(Layer.AIR, 5000), -1))), PartASpecs.beamMount(1));
        long burnt = Allocations.least(
                () -> {
                    Sortie sortie = sortie(
                            under(units(new GroundUnit(1, X, TorpedoTest.dummy(Layer.AIR, 5000), -1))),
                            PartASpecs.beamMount(1));
                    run(sortie, 120, FIRE);
                    return sortie;
                },
                sortie -> run(sortie, 600, FIRE));
        assertEquals(0, burnt, "the beam under water allocated " + burnt + " bytes");
        assertTrue(beam.underwater());
    }

    @Test
    void aCrossingEasesTheFactorsOverASecondAndSwitchesTheLayersAtOnce() {
        LevelScript level = units().toBuilder()
                .medium(new MediumSpec(
                        EUROPA, List.of(new MediumSpec.Switch(2, true), new MediumSpec.Switch(5, false))))
                .build();
        Sortie sortie = sortie(level, front(TorpedoTest.TORPEDO));
        run(sortie, SimStep.ticks(2) - 1, Command.NONE);
        assertFalse(sortie.underwater());
        assertEquals(0, sortie.waterBlend());
        assertTrue(sortie.mountIdle(0), "the torpedo reads NO WATER above the waterline");
        sortie.step(Command.NONE);
        assertTrue(sortie.underwater(), "the waterline");
        assertFalse(sortie.mountIdle(0));
        double previous = sortie.waterBlend();
        assertTrue(previous > 0 && previous < 0.01, "the ease starts: " + previous);
        run(sortie, 29, Command.NONE);
        double half = (30.0 / 60) * (30.0 / 60) * (3 - 2 * (30.0 / 60));
        assertEquals(half, sortie.waterBlend(), 1e-9, "smoothstep halfway");
        // A shot fired in the ease keeps the factors of its spawn.
        Sortie bolts = sortie(level, front(BOLT));
        run(bolts, SimStep.ticks(2) + 29, Command.NONE);
        bolts.step(FIRE);
        double blend = bolts.waterBlend();
        assertEquals(10 * (1 - 0.3 * blend), bolts.shot(0).damage(), 1e-9);
        assertEquals(600 * (1 - 0.3 * blend), bolts.shot(0).vy(), 1e-9);
        run(sortie, SimStep.ticks(1), Command.NONE);
        assertEquals(1, sortie.waterBlend());
        // So far 2 s + 29 steps + 1 s.
        run(sortie, SimStep.ticks(5) - (SimStep.ticks(3) + 29) - 1, Command.NONE);
        assertTrue(sortie.underwater());
        sortie.step(Command.NONE);
        assertFalse(sortie.underwater(), "back above the water");
        assertTrue(sortie.waterBlend() < 1 && sortie.waterBlend() > 0.99);
        run(sortie, SimStep.ticks(1), Command.NONE);
        assertEquals(0, sortie.waterBlend());
    }
}
