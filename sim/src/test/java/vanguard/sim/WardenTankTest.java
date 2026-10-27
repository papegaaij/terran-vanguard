package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part F, step F3: the Warden Tank (design/enemies/ground/warden-tank): a tracked walker whose turret turns on its
 * own toward the ship at 120°/s, stops turning 0.3 s before each 3-shot burst (the tell) and fires along its aim
 * every 2.5 s, up the screen too; its turret is a unit part (20 HP) whose loss silences the gun; its rear plate takes
 * ×1.5 from direct shots within ±50° of its rear. And the {@code arc: up} turret ({@link EnemyGun#arcFacingRadians()})
 * and the conveyor belts that carry ground units ({@link GroundSpec.Belt}).
 */
class WardenTankTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double SCROLL = 130;

    /** Its gun at medium: a 3-shot burst every 2.5 s, 0.15 s apart, 160 px/s, 6 damage. */
    static final EnemyGun TANK_GUN = new EnemyGun(
            2.5,
            0,
            3,
            160,
            6,
            false,
            1,
            0,
            TestSpecs.INFINITE,
            TestSpecs.INFINITE,
            Optional.empty(),
            Optional.empty(),
            0.15,
            false);

    static final EnemySpec.Turret TURRET = new EnemySpec.Turret(Math.toRadians(120), 0.3, Math.toRadians(10));

    /** The turret part: 20 HP, 22×22 on the hull's centre, 8 credits, its loss silencing the gun; whole 20 + 6. */
    static final EnemySpec.UnitPart TURRET_PART =
            new EnemySpec.UnitPart("turret", 20, new Hitbox(22, 22), 0, 0, 8, true, 6);

    static final EnemySpec.RearPlate REAR = new EnemySpec.RearPlate(Math.toRadians(50), 1.5);

    /** The Warden Tank at medium: hull 64 HP (46×56), 20 credits, 40 px/s turning at 60°/s. */
    static final EnemySpec TANK = EnemySpec.of("warden-tank", 64, new Hitbox(46, 56), Layer.GROUND, 15, false, 20, 40)
            .withGun(Optional.of(TANK_GUN))
            .withWalker(Optional.of(new EnemySpec.Walker(40, Math.toRadians(60), 32, 0, Optional.empty(), Math.PI)))
            .withPart(Optional.of(TURRET_PART))
            .withTurret(Optional.of(TURRET))
            .withRearPlate(Optional.of(REAR))
            .withMachine(true);

    /** A bolt of 4 damage, fast enough to hit at once. */
    private static final WeaponSpec BOLT =
            TestSpecs.bolt("bolt", 10, 4, 900, new Hitbox(4, 12), TestSpecs.INFINITE, 1, TestSpecs.muzzle(0, 21, 0));

    private static final Loadout BOLTS = TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, BOLT, BOLT));

    /** A level scrolling at 130 px/s with these waves, ground units and belts. */
    static LevelScript level(
            double seconds, List<WaveSpec> waves, List<GroundUnit> units, List<GroundSpec.Belt> belts) {
        return level(seconds, SCROLL, waves, units, belts);
    }

    /** A level scrolling at {@code scroll} px/s with these waves, ground units and belts. */
    static LevelScript level(
            double seconds, double scroll, List<WaveSpec> waves, List<GroundUnit> units, List<GroundSpec.Belt> belts) {
        return LevelScript.builder(19, 3)
                .sections(List.of(new Section(seconds, scroll)))
                .waves(waves)
                .groundUnits(units)
                .ground(GroundSpec.NONE.withBelts(belts))
                .build();
    }

    private static Sortie sortie(LevelScript level, Loadout loadout) {
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** One tank at t=0 walking the path through {@code path}. */
    private static Sortie tank(Loadout loadout, List<GroundSpec.Belt> belts, WaveSpec.At... path) {
        return sortie(level(30, List.of(SiegeSpecs.walker(0, TANK, path)), List.of(), belts), loadout);
    }

    /** One tank at t=0 walking the path through {@code path} over ground that scrolls at only 10 px/s. */
    private static Sortie slowTank(WaveSpec.At... path) {
        return sortie(
                level(60, 10, List.of(SiegeSpecs.walker(0, TANK, path)), List.of(), List.of()), TestSpecs.LOADOUT);
    }

    /** Flies the ship up for {@code seconds}, then holds it there. */
    private static void climb(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(Command.UP.bit());
        }
    }

    private static Enemy find(Sortie sortie) {
        Enemy enemy = SiegeSpecs.find(sortie, TANK);
        assertNotNull(enemy);
        return enemy;
    }

    private static double off(double a, double b) {
        return Math.abs(Math.IEEEremainder(a - b, 2 * Math.PI));
    }

    @Test
    void aTurretIsAWalkersWithAGunAndARearPlateAWalkers() {
        EnemySpec needler = TestSpecs.NEEDLER;
        assertThrows(IllegalArgumentException.class, () -> needler.withTurret(Optional.of(TURRET)));
        assertThrows(IllegalArgumentException.class, () -> needler.withRearPlate(Optional.of(REAR)));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Turret(0, 0.3, 0.1));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.RearPlate(1, 0));
        assertTrue(TANK.turret().isPresent());
        assertTrue(TANK.rearPlate().isPresent());
        assertTrue(TestSpecs.NEEDLER.turret().isEmpty(), "every other unit has none");
    }

    @Test
    void theTurretTurnsOnItsOwnWhileTheHullFacesItsPath() {
        // Across the screen from left to right, 200 px below the top edge; the ship below it.
        Sortie sortie = slowTank(new WaveSpec.At(-40, 200), new WaveSpec.At(1000, 200));
        sortie.step(Command.NONE);
        Enemy tank = find(sortie);
        assertEquals(-Math.PI / 2, tank.facing(), 1e-9, "the hull faces its path: right");
        assertEquals(tank.facing(), tank.turretAim(), 1e-9, "the turret starts along the hull");
        double most = TURRET.turnRate() * SimStep.SECONDS + 1e-9;
        double before = tank.turretAim();
        boolean turned = false;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
            assertEquals(-Math.PI / 2, tank.facing(), 1e-6, "the hull keeps its heading");
            assertTrue(off(tank.turretAim(), before) <= most, "at most 120°/s");
            turned |= off(tank.turretAim(), before) > 0;
            before = tank.turretAim();
        }
        assertTrue(turned);
        double toShip = StrictMath.atan2(
                -(sortie.ship().x() - tank.x()), -(sortie.ship().y() - tank.y()));
        assertTrue(off(tank.turretAim(), toShip) < Math.toRadians(10), "on the ship, not on the hull's heading");
    }

    @Test
    void itLocksForTheTellThenFiresThreeShotsAlongItsAimEveryTwoAndAHalfSeconds() {
        // Across the screen 150 px below the top edge: the ship below holds still.
        Sortie sortie = slowTank(new WaveSpec.At(-40, 150), new WaveSpec.At(1000, 150));
        sortie.step(Command.NONE);
        Enemy tank = find(sortie);
        List<Integer> shots = new ArrayList<>();
        int lockStart = -1;
        int lockLength = -1;
        double lockedAim = Double.NaN;
        for (int i = 0; i < SimStep.ticks(9); i++) {
            boolean wasLocked = tank.turretLocked();
            sortie.step(Command.NONE);
            if (tank.turretLocked() && !wasLocked) {
                lockStart = i;
                lockedAim = tank.turretAim();
            }
            if (tank.turretLocked()) {
                assertEquals(lockedAim, tank.turretAim(), 0, "it stops turning for the tell and the burst");
            }
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                if (lockLength < 0) {
                    lockLength = i - lockStart;
                }
                shots.add(i);
            }
        }
        assertEquals(SimStep.ticks(0.3), lockLength, "the 0.3 s tell before the first shot");
        assertTrue(shots.size() >= 6, "two bursts at least: " + shots);
        int gap = SimStep.ticks(0.15);
        assertEquals(gap, shots.get(1) - shots.get(0), "0.15 s apart");
        assertEquals(gap, shots.get(2) - shots.get(1));
        assertEquals(SimStep.ticks(2.5), shots.get(3) - shots.get(0), "a burst every 2.5 s");
    }

    @Test
    void afterPassingTheShipItFiresUpTheScreenAlongItsTurret() {
        // Down the screen at x 300 to below the ship (flown up the screen): its later bursts go up the screen.
        Sortie sortie = tank(TestSpecs.LOADOUT, List.of(), new WaveSpec.At(300, -40), new WaveSpec.At(300, 3000));
        climb(sortie, 1.2);
        Enemy tank = find(sortie);
        boolean up = false;
        for (int i = 0; i < SimStep.ticks(12) && !up && sortie.enemyCount() > 0; i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0
                    && tank.y() < sortie.ship().y() - 40) {
                for (int b = 0; b < sortie.bulletCount(); b++) {
                    EnemyBullet bullet = sortie.bullet(b);
                    up |= bullet.vy() > 100 && Math.abs(bullet.y() - tank.y()) < 20;
                }
            }
        }
        assertTrue(up, "a burst fired up the screen from behind the ship");
    }

    @Test
    void itsTurretTakesTheShotsFirstAndItsLossSilencesTheGun() {
        // Walking up the screen above the ship at x 240: the ship's bolts reach the turret over the hull.
        Sortie sortie = tank(BOLTS, List.of(), new WaveSpec.At(240, 150), new WaveSpec.At(240, -3000));
        sortie.step(Command.NONE);
        Enemy tank = find(sortie);
        boolean off = false;
        for (int i = 0; i < SimStep.ticks(2) && !off; i++) {
            sortie.step(FIRE);
            off = sortie.events().count(SimEvents.Type.UNIT_PART_DESTROYED) > 0;
        }
        assertTrue(off, "the turret is shot off");
        assertEquals(64, tank.hp(), 1e-9, "the hull untouched");
        assertEquals(8, sortie.credits(), "the turret's part bounty");
        double aim = tank.turretAim();
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_FIRED), "no burst without its turret");
            assertFalse(tank.turretLocked());
        }
        assertEquals(aim, tank.turretAim(), 0, "a dead turret no longer turns");
    }

    @Test
    void theRearPlateTakesHalfAsMuchAgainFromDirectShotsWithinFiftyDegreesOfTheRear() {
        Enemy down = new Enemy();
        down.spawn(walkSpawn(240, 400, 240, 100), 0);
        assertEquals(0, down.facing(), 1e-9, "facing down the screen");
        assertEquals(1.5, down.rearFactor(0, -1), 1e-12, "a shot flying down hits its rear");
        assertEquals(1.5, down.rearFactor(Math.sin(Math.toRadians(45)), -Math.cos(Math.toRadians(45))), 1e-12);
        assertEquals(1, down.rearFactor(Math.sin(Math.toRadians(55)), -Math.cos(Math.toRadians(55))), 1e-12);
        assertEquals(1, down.rearFactor(0, 1), 1e-12, "a shot from the front does its own damage");
        assertEquals(1, down.rearFactor(0, 0), 1e-12);

        // In flight: a tank walking up the screen shows its rear to the ship's bolts (turret first shot off).
        Sortie sortie = tank(BOLTS, List.of(), new WaveSpec.At(240, 150), new WaveSpec.At(240, -3000));
        sortie.step(Command.NONE);
        Enemy tank = find(sortie);
        tank.damagePart(100, false);
        double hp = tank.hp();
        while (tank.hp() == hp) {
            sortie.step(FIRE);
        }
        assertEquals(hp - 4 * 1.5, tank.hp(), 1e-9, "×1.5 from behind");
        // ... and one walking down shows its front: the bolt's own damage.
        Sortie front = tank(BOLTS, List.of(), new WaveSpec.At(240, -40), new WaveSpec.At(240, 3000));
        front.step(Command.NONE);
        Enemy other = find(front);
        other.damagePart(100, false);
        while (other.hp() == 64) {
            front.step(FIRE);
        }
        assertEquals(60, other.hp(), 1e-9, "×1 from the front");
    }

    private static Spawn walkSpawn(double... path) {
        return new Spawn(
                0,
                0,
                TANK,
                FlightPath.through(path[0], path[1], path[0], path[1] - 1),
                40,
                0,
                Optional.empty(),
                Spawn.Exit.DOWN,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(WalkPath.through(path)));
    }

    @Test
    void anEmpStunStopsTheTurret() {
        Sortie sortie = slowTank(new WaveSpec.At(-40, 200), new WaveSpec.At(1000, 200));
        sortie.step(Command.NONE);
        Enemy tank = find(sortie);
        for (int i = 0; i < 10; i++) {
            sortie.step(Command.NONE);
        }
        tank.empStun(SimStep.ticks(3));
        double aim = tank.turretAim();
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
            assertEquals(aim, tank.turretAim(), 0, "tracks and turret stop");
            assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_FIRED));
        }
    }

    // ---- the conveyor belts ----

    private static final GroundSpec.Belt BELT = new GroundSpec.Belt(140, 48, -10_000, 100_000, 40);

    @Test
    void aTankDrivingUpABeltMovesDownTheScreenAtFiftyPxASecond() {
        Sortie onBelt = tank(TestSpecs.LOADOUT, List.of(BELT), new WaveSpec.At(140, -40), new WaveSpec.At(140, -5000));
        Sortie offBelt = tank(TestSpecs.LOADOUT, List.of(), new WaveSpec.At(140, -40), new WaveSpec.At(140, -5000));
        for (int i = 0; i < SimStep.ticks(1); i++) {
            onBelt.step(Command.NONE);
            offBelt.step(Command.NONE);
        }
        Enemy carried = find(onBelt);
        Enemy walking = find(offBelt);
        double carriedY = carried.y();
        double walkingY = walking.y();
        for (int i = 0; i < SimStep.ticks(2); i++) {
            onBelt.step(Command.NONE);
            offBelt.step(Command.NONE);
        }
        assertEquals(-2 * 50, carried.y() - carriedY, 1e-6, "130 − 40 − 40 = 50 px/s down the screen");
        assertEquals(-2 * 90, walking.y() - walkingY, 1e-6, "130 − 40 off the belt");
        assertEquals(140, carried.x(), 1e-6);
        assertEquals(40, onBelt.beltSpeedAt(140, 300), 0);
        assertEquals(0, onBelt.beltSpeedAt(200, 300), 0, "off its column");
    }

    @Test
    void aBeltCarriesGroundUnitsOnlyOnItsStretchOfGround() {
        EnemySpec turret = groundTurret(EnemyGun.aimed(2.5, 0.8, 1, 150, 4, false));
        // The unit enters at ground distance 270 + 12 (a ground point at the top edge lies at scroll + 270) and keeps
        // it while it scrolls with the ground; the belt carries it on to 330, where the belt ends.
        GroundSpec.Belt stretch = new GroundSpec.Belt(240, 48, 250, 330, 40);
        Sortie sortie = sortie(
                level(30, List.of(), List.of(new GroundUnit(0, 240, turret, -1)), List.of(stretch)), TestSpecs.LOADOUT);
        sortie.step(Command.NONE);
        Enemy unit = SiegeSpecs.find(sortie, turret);
        assertNotNull(unit);
        Belts belts = sortie.belts();
        assertTrue(belts.any());
        int carried = 0;
        int still = 0;
        for (int i = 0; i < SimStep.ticks(6) && sortie.enemyCount() > 0; i++) {
            double y = unit.y();
            boolean on = belts.speedAt(unit.x(), y, sortie.groundScroll()) != 0;
            sortie.step(Command.NONE);
            double moved = unit.y() - y;
            if (on && belts.speedAt(unit.x(), unit.y(), sortie.groundScroll()) != 0) {
                assertEquals((-SCROLL + 40) * SimStep.SECONDS, moved, 1e-6, "carried up against the scroll");
                carried++;
            } else if (!on && belts.speedAt(unit.x(), unit.y(), sortie.groundScroll()) == 0) {
                assertEquals(-SCROLL * SimStep.SECONDS, moved, 1e-6, "with the ground only");
                still++;
            }
        }
        assertTrue(carried > 0 && still > 0, carried + " / " + still);
    }

    // ---- arc: up ----

    static EnemySpec groundTurret(EnemyGun gun) {
        return EnemySpec.of("spine-turret", 30, new Hitbox(24, 24), Layer.GROUND, 0, false, 10, 0)
                .withGun(Optional.of(gun))
                .withTerrain(true);
    }

    /** A turret's gun: aimed every second, turning 180°/s within ±60° of its arc's facing. */
    private static EnemyGun arcGun(double facing) {
        return new EnemyGun(
                        1.0,
                        0.2,
                        1,
                        150,
                        4,
                        false,
                        1,
                        0,
                        Math.PI,
                        Math.toRadians(60),
                        Optional.empty(),
                        Optional.empty())
                .withArcFacing(facing);
    }

    /** The shots the turret at x 240 fires while it is above the ship and after it passed below it. */
    private static int[] shotsAboveAndBelow(EnemySpec turret) {
        Sortie sortie = sortie(
                level(30, 60, List.of(), List.of(new GroundUnit(1.5, 240, turret, -1)), List.of()), TestSpecs.LOADOUT);
        climb(sortie, 1.2);
        while (sortie.enemyCount() == 0) {
            sortie.step(Command.NONE);
        }
        Enemy unit = SiegeSpecs.find(sortie, turret);
        int above = 0;
        int below = 0;
        while (sortie.enemyCount() > 0 && sortie.levelSeconds() < 20) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                if (unit.y() > sortie.ship().y()) {
                    above++;
                } else {
                    below++;
                }
            }
        }
        return new int[] {above, below};
    }

    @Test
    void anArcUpTurretFiresOnlyOnceTheShipIsPastIt() {
        EnemyGun up = arcGun(Math.PI);
        assertEquals(Math.PI, up.arcFacingRadians(), 0);
        assertEquals(0, TestSpecs.NEEDLER_GUN.arcFacingRadians(), 0, "straight down for every other gun");
        Enemy rooted = new Enemy();
        rooted.root(groundTurret(up), 0, 240, 0, -1);
        assertEquals(Math.PI, rooted.aim(), 1e-12, "its barrel starts up the screen");

        int[] upShots = shotsAboveAndBelow(groundTurret(up));
        assertEquals(0, upShots[0], "silent while the ship is below it");
        assertTrue(upShots[1] >= 2, "firing up the screen at the ship once it has passed: " + upShots[1]);
        int[] downShots = shotsAboveAndBelow(groundTurret(arcGun(0)));
        assertTrue(downShots[0] >= 1, "a turret facing down fires while the ship is below it");
        assertEquals(0, downShots[1]);
    }

    @Test
    void tanksOnABeltAreDeterministicAndAllocateNothing() {
        LevelScript level = level(
                30,
                List.of(
                        SiegeSpecs.walker(0, TANK, new WaveSpec.At(140, -40), new WaveSpec.At(140, -5000)),
                        SiegeSpecs.walker(1, TANK, new WaveSpec.At(300, -40), new WaveSpec.At(300, 5000))),
                List.of(),
                List.of(BELT));
        Sortie a = sortie(level, BOLTS);
        Sortie b = sortie(level, BOLTS);
        for (int i = 0; i < SimStep.ticks(10); i++) {
            int commands = (i / 40) % 2 == 0 ? Command.LEFT.bit() | FIRE : Command.RIGHT.bit();
            a.step(commands);
            b.step(commands);
        }
        assertEquals(a.stateHash(), b.stateHash());
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = sortie(level, BOLTS);
                    for (int i = 0; i < SimStep.ticks(1); i++) {
                        sortie.step(Command.NONE);
                    }
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(8); i++) {
                        sortie.step((i / 30) % 2 == 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
                    }
                });
        assertEquals(0, allocated, "the turrets, the rear plates and the belts allocate nothing per step");
    }
}
