package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.sim.TestSpecs.INFINITE;
import static vanguard.sim.TestSpecs.muzzle;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M8 part A, step A3 (design/player/wingmen, Heavy drone "Bulwark"; m8a-gaps.md §1.7, D10, D11): the Bulwark holds
 * its wing slot (56, +20) beside the ship, lagging with the water's drag; its {@code anti-sub} cannon fires a 14-damage
 * shell a second while the player fires and its kills pay; it draws 30 % of the aimed volleys at the ship while within
 * 150 px of it, from its own generator (the sortie's draws do not shift); the enemies' bullets hit its armour and at
 * zero it is out for the attempt; a retry brings it back.
 */
class BulwarkTest {
    private static final int FIRE = Command.FIRE.bit();

    /** One 14-damage shell a second straight ahead, anti-sub (design/player/wingmen/data.yaml, bulwark.gun). */
    static final WeaponSpec CANNON = TestSpecs.bolt(
                    "bulwark-cannon", 1, 14, 500, new Hitbox(6, 10), INFINITE, 1, muzzle(0, 18, 0))
            .antiSub(true);

    static BulwarkSpec bulwark(double drawRadius, double share) {
        return new BulwarkSpec(
                WingmanSpec.Side.RIGHT,
                120,
                120,
                36,
                new Hitbox(14, 14),
                200,
                new WingmanSpec.Offset(56, 20),
                CANNON,
                drawRadius,
                share);
    }

    static final BulwarkSpec BULWARK = bulwark(150, 0.3);

    /** No weapon of the ship's own: only the Bulwark shoots. */
    private static Loadout unarmed() {
        return TestSpecs.loadout();
    }

    private static Sortie sortie(BulwarkSpec spec, LevelScript level, Rules rules) {
        return new Sortie(1, unarmed().withBulwark(spec), level, rules, TestSpecs.FULL_ARMOUR);
    }

    private static Bulwark of(Sortie sortie) {
        return sortie.bulwark().orElseThrow();
    }

    private static void run(Sortie sortie, double seconds, int commands) {
        TorpedoTest.run(sortie, SimStep.ticks(seconds), commands);
    }

    /** A Needler (hovering, then circling) every 3 s, each firing an aimed shot of {@code damage} every 0.25 s. */
    private static LevelScript gunner(double damage) {
        EnemySpec gunner = TestSpecs.needler(EnemyGun.aimed(0.25, 0, 1, 150, damage, false));
        List<WaveSpec> waves = new java.util.ArrayList<>();
        for (int t = 0; t < 36; t += 3) {
            waves.add(TestSpecs.wave(
                    t, WaveSpec.Formation.LINE_ABREAST, gunner, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE));
        }
        return TestSpecs.level(40, waves);
    }

    private static EnemySpec target(double hp) {
        return EnemySpec.of("target", hp, new Hitbox(20, 20), Layer.AIR, 0, false, 12, 100)
                .withTerrain(true);
    }

    @Test
    void itHoldsItsWingSlotAndLagsMoreUnderWater() {
        Sortie air = sortie(BULWARK, TorpedoTest.units(false), TestSpecs.RULES);
        assertEquals(Ship.START_X + 56, of(air).x(), 1e-9);
        assertEquals(Ship.START_Y - 20, of(air).y(), 1e-9);
        // The ship with the Pressure hull flies as in the air; the Bulwark keeps its drag.
        Sortie water = new Sortie(
                1,
                unarmed().withPressureHull().withBulwark(BULWARK),
                UnderwaterTest.under(TorpedoTest.units(false)),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        Sortie left = sortie(
                new BulwarkSpec(
                        WingmanSpec.Side.LEFT,
                        120,
                        120,
                        36,
                        new Hitbox(14, 14),
                        200,
                        new WingmanSpec.Offset(56, 20),
                        CANNON,
                        150,
                        0.3),
                TorpedoTest.units(false),
                TestSpecs.RULES);
        assertEquals(Ship.START_X - 56, of(left).x(), 1e-9, "on the escort's side");
        int up = Command.UP.bit();
        run(air, 0.3, up);
        run(water, 0.3, up);
        assertEquals(air.ship().y(), water.ship().y(), 1e-9);
        double airLag = air.ship().y() - 20 - of(air).y();
        double waterLag = water.ship().y() - 20 - of(water).y();
        assertTrue(airLag > 0, "it trails the ship: " + airLag);
        assertTrue(waterLag > airLag, "the drag: " + waterLag + " vs " + airLag);
        run(air, 2, Command.NONE);
        assertEquals(air.ship().x() + 56, of(air).x(), 0.5, "back in its slot");
        assertEquals(air.ship().y() - 20, of(air).y(), 0.5);
    }

    @Test
    void itsCannonFiresAShellASecondWhileThePlayerFiresAtFullDamageUnderWaterAndItsKillsPay() {
        Sortie water = sortie(BULWARK, UnderwaterTest.under(TorpedoTest.units(false)), TestSpecs.RULES);
        int mount = water.wingmanMount();
        int shots = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            water.step(FIRE);
            SimEvents events = water.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.SHOT_FIRED && events.value(e) == mount) {
                    shots++;
                }
            }
        }
        assertEquals(3, shots);
        assertEquals(14, water.shot(0).damage(), 1e-12, "anti-sub: no × 0.7");
        assertEquals(500 * 0.7, water.shot(0).vy(), 1e-9, "its shell still flies at the water's speed");
        Sortie idle = sortie(BULWARK, TorpedoTest.units(false), TestSpecs.RULES);
        run(idle, 3, Command.NONE);
        assertEquals(0, idle.shotCount(), "silent while the player holds fire");

        Sortie kill = sortie(
                BULWARK,
                TorpedoTest.units(false, new GroundUnit(0.5, Ship.START_X + 56, target(20), -1)),
                TestSpecs.RULES.withInvulnerableShip());
        run(kill, 6, FIRE);
        assertTrue(kill.credits() > 0, "its kill paid");
        assertEquals(0, kill.enemyCount());
    }

    @Test
    void itDrawsAboutThreeTenthsOfTheAimedVolleysWhileNearTheShipFromItsOwnGenerator() {
        Sortie near = sortie(BULWARK, gunner(0), TestSpecs.RULES.withInvulnerableShip());
        int volleys = 0;
        for (int i = 0; i < SimStep.ticks(30); i++) {
            near.step(Command.NONE);
            volleys += near.events().count(SimEvents.Type.ENEMY_FIRED);
        }
        int drawn = of(near).drawn();
        assertTrue(volleys > 40, "volleys: " + volleys);
        double share = (double) drawn / volleys;
        assertTrue(share > 0.15 && share < 0.45, drawn + " of " + volleys);

        Sortie far = sortie(bulwark(30, 0.3), gunner(0), TestSpecs.RULES.withInvulnerableShip());
        run(far, 30, Command.NONE);
        assertEquals(0, of(far).drawn(), "beyond its draw radius");

        // Its own stream: the sortie's generator draws as it does without a Bulwark.
        Sortie without =
                new Sortie(1, unarmed(), gunner(0), TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        Sortie with = sortie(BULWARK, gunner(0), TestSpecs.RULES.withInvulnerableShip());
        for (int i = 0; i < SimStep.ticks(20); i++) {
            without.step(Command.NONE);
            with.step(Command.NONE);
            assertEquals(without.rng.state(), with.rng.state(), "step " + i);
        }
    }

    @Test
    void aDrawnVolleyIsAimedAtTheBulwark() {
        Sortie sortie = sortie(bulwark(150, 1), gunner(0), TestSpecs.RULES.withInvulnerableShip());
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                EnemyBullet bullet = sortie.bullet(sortie.bulletCount() - 1);
                Bulwark bulwark = of(sortie);
                double cross = bullet.vx() * (bulwark.y() - bullet.y()) - bullet.vy() * (bulwark.x() - bullet.x());
                double length = Math.hypot(bullet.vx(), bullet.vy())
                        * Math.hypot(bulwark.x() - bullet.x(), bulwark.y() - bullet.y());
                assertEquals(0, cross / length, 0.05, "along the line to the Bulwark");
                return;
            }
        }
        throw new AssertionError("nothing fired");
    }

    @Test
    void theEnemiesBulletsWreckItAndItIsOutForTheAttemptUntilARetry() {
        Sortie sortie = sortie(bulwark(150, 1), gunner(30), TestSpecs.RULES);
        boolean destroyed = false;
        for (int i = 0; i < SimStep.ticks(30) && !destroyed; i++) {
            sortie.step(Command.NONE);
            destroyed = sortie.events().count(SimEvents.Type.BULWARK_DESTROYED) > 0;
        }
        assertTrue(destroyed);
        Bulwark bulwark = of(sortie);
        assertTrue(bulwark.destroyed());
        assertEquals(0, bulwark.armour());
        int drawn = bulwark.drawn();
        run(sortie, 3, FIRE);
        assertEquals(drawn, bulwark.drawn(), "a wreck draws nothing");
        assertFalse(bulwark.drawing(sortie.ship().x(), sortie.ship().y()));
        assertEquals(0, sortie.shotCount(), "nor fires");

        sortie.retry(TestSpecs.FULL_ARMOUR, 90);
        sortie.step(Command.NONE);
        assertFalse(bulwark.destroyed());
        assertEquals(90, bulwark.armour(), "the retry's armour");
    }

    @Test
    void theEscortSlotHoldsRookOrABulwarkNotBoth() {
        Loadout rook = TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT));
        assertTrue(rook.withBulwark(BULWARK).wingman().isEmpty(), "the Bulwark takes his slot");
        assertThrows(IllegalArgumentException.class, () -> Loadout.builder(
                        TestSpecs.SHIP, rook.armament(), TestSpecs.SHIELD, TestSpecs.PLATING)
                .wingman(rook.wingman())
                .bulwark(Optional.of(BULWARK))
                .build());
        assertThrows(IllegalArgumentException.class, () -> BULWARK.withArmour(121));
    }

    @Test
    void itReplaysToTheSameHashAndDoesNotAllocate() {
        Supplier<Sortie> make = () -> sortie(BULWARK, gunner(5), TestSpecs.RULES);
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < 1200; i++) {
            int command = (i / 60) % 2 == 0 ? FIRE : Command.LEFT.bit();
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        long allocated = Allocations.least(make, sortie -> {
            for (int i = 0; i < 900; i++) {
                sortie.step((i / 60) % 2 == 0 ? FIRE : Command.RIGHT.bit());
            }
        });
        assertEquals(0, allocated);
    }
}
