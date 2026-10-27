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
 * M8 part E, the Spiral Nautilus (design/enemies/naval/spiral-nautilus; m8e-gaps.md §1.3, §5 E7, E8): a coil orbits
 * the ship in its closed shell, spiralling in from 220 to 90 px over 3 s (never closer than 90), uncoils for 2.2 s
 * (0.6 s opening with the 0.4 s crown tell, 1.6 s open at 60 px/s, its spiral firing: 3 arms, a volley every 0.1 s,
 * the first at the ship), closes and breaks outward; a pair orbits in opposite directions, cycles 2 s apart. The
 * closed shell glances off all but the specials' blasts and {@code anti-sub} fire. In a trench too narrow for the
 * spiral its orbit is clamped near the channel's middle (round 53 A.1); after its wave's hold it leaves by the nearer
 * side. Deterministic and allocation-free.
 */
class SpiralNautilusTest {
    /** Medium: 220 → 90 px over 3 s, opening 0.6 s (tell 0.4 s), open 1.6 s at 60 px/s, a pair's offset 2 s. */
    static final EnemySpec.Coil COIL = new EnemySpec.Coil(220, 90, 3, 0.6, 0.4, 1.6, 60, 2);

    private static final int IN = SimStep.ticks(3);
    private static final int OPENING = SimStep.ticks(0.6);
    private static final int OPEN = SimStep.ticks(1.6);
    private static final int CYCLE = IN + OPENING + OPEN;

    static EnemyGun spiralGun(int arms) {
        return EnemyGun.aimed(TestSpecs.INFINITE, 0, 1, 100, 4, false)
                .withSpiral(Optional.of(new EnemyGun.SpiralSpec(arms, StrictMath.PI / 2, 0.1)));
    }

    /** 56×56 / 40×40, 100 HP, contact 15, bounty 30, 150 px/s on its spiral, the spiral gun of {@code arms} arms. */
    static EnemySpec nautilus(EnemySpec.Coil coil, int arms) {
        return EnemySpec.of("spiral-nautilus", 100, new Hitbox(40, 40), Layer.AIR, 15, false, 30, 150)
                .withGun(Optional.of(spiralGun(arms)))
                .withCoil(Optional.of(coil));
    }

    static final EnemySpec NAUTILUS = nautilus(COIL, 3);

    /** A {@code circle} of {@code count} at {@code t} holding {@code hold} s. */
    static WaveSpec pair(double t, EnemySpec enemy, int count, double hold) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.CIRCLE,
                enemy,
                count,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.of(hold),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                List.of());
    }

    /** Open water: a dry 40 s level at 40 px/s. */
    private static Sortie open(Loadout loadout, WaveSpec... waves) {
        return VortexTest.sortie(40, loadout, waves);
    }

    private static Sortie open(WaveSpec... waves) {
        return open(TestSpecs.LOADOUT, waves);
    }

    /** The 260 px trench of the Siren's tests (walls at 110 and 370). */
    private static Sortie trench(WaveSpec... waves) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT,
                SirenTest.trench(SirenTest.WALLS, List.of(waves)),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    /** Steps until the first unit is in, returning it. */
    private static Enemy firstIn(Sortie sortie) {
        return VortexTest.firstIn(sortie);
    }

    private static double distance(Enemy unit, Ship ship) {
        return Math.hypot(unit.x() - ship.x(), unit.y() - ship.y());
    }

    /** The commands that steer the ship toward (x, y), nothing within 3 px. */
    private static int toward(Sortie sortie, double x, double y) {
        Ship ship = sortie.ship();
        int commands = 0;
        if (ship.x() < x - 3) {
            commands |= Command.RIGHT.bit();
        } else if (ship.x() > x + 3) {
            commands |= Command.LEFT.bit();
        }
        if (ship.y() < y - 3) {
            commands |= Command.UP.bit();
        } else if (ship.y() > y + 3) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }

    @Test
    void aCoilIsAPlainAirUnitsAndItsNumbersHold() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Coil(80, 90, 3, 0.6, 0.4, 1.6, 60, 2));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Coil(220, 90, 3, 0.6, 0.8, 1.6, 60, 2));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Coil(220, 90, 3, 0.6, 0.4, 0, 60, 2));
        EnemySpec ground = EnemySpec.of("ground", 10, new Hitbox(20, 20), Layer.GROUND, 0, false, 1, 0);
        assertThrows(IllegalArgumentException.class, () -> ground.withCoil(Optional.of(COIL)));
        assertThrows(
                IllegalArgumentException.class,
                () -> NAUTILUS.withVortex(Optional.of(VortexTest.VORTEX)),
                "a coil is a plain air unit's");
        assertThrows(
                IllegalArgumentException.class,
                () -> open(new WaveSpec(
                        0,
                        WaveSpec.Formation.CIRCLE,
                        NAUTILUS,
                        2,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE,
                        Optional.empty(),
                        Optional.empty(),
                        1,
                        Optional.empty(),
                        Optional.empty(),
                        List.of())),
                "a coil's wave holds");
        assertEquals(
                CoilPhase.NONE,
                firstIn(VortexTest.slow(VortexTest.wave(0, VortexTest.devil(), 1)))
                        .coilPhase());
    }

    @Test
    void itSpiralsInFrom220To90OverThreeSecondsUncoilsForTwoPointTwoAndBreaksOutward() {
        Sortie sortie = open(pair(0, NAUTILUS, 1, 30));
        Enemy unit = firstIn(sortie);
        Ship ship = sortie.ship();
        List<Integer> uncoils = new ArrayList<>();
        List<Integer> coils = new ArrayList<>();
        double nearest = Double.MAX_VALUE;
        double at1 = 0;
        double atIn = 0;
        double farthestAfterClose = 0;
        for (int step = 1; step <= 2 * CYCLE; step++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.UNCOIL) > 0) {
                uncoils.add(step);
            }
            if (sortie.events().count(SimEvents.Type.COIL) > 0) {
                coils.add(step);
            }
            double d = distance(unit, ship);
            if (step >= SimStep.ticks(1.5)) {
                nearest = Math.min(nearest, d);
            }
            if (step == SimStep.ticks(1)) {
                at1 = d;
            }
            if (step == IN - 2) {
                atIn = d;
            }
            if (!coils.isEmpty() && step - coils.getFirst() < IN && step - coils.getFirst() > SimStep.ticks(0.5)) {
                farthestAfterClose = Math.max(farthestAfterClose, d);
            }
            CoilPhase phase = unit.coilPhase();
            // Its spawn's step was its first: after step n of these it is n + 1 steps into its cycles.
            int inCycle = (step + 1) % CYCLE;
            CoilPhase expected =
                    inCycle < IN ? CoilPhase.CLOSED : inCycle < IN + OPENING ? CoilPhase.OPENING : CoilPhase.OPEN;
            assertEquals(expected, phase, "at step " + step);
            assertEquals(phase != CoilPhase.OPEN, unit.armoured(), "armoured but while open, at step " + step);
            assertEquals(
                    phase == CoilPhase.OPENING && inCycle >= IN + OPENING - SimStep.ticks(0.4),
                    unit.coilTell(),
                    "the crown's tell the last 0.4 s of the opening, at step " + step);
        }
        assertEquals(List.of(IN - 1, CYCLE + IN - 1), uncoils, "it uncoils after 3 s closed, every 5.2 s");
        assertEquals(List.of(CYCLE - 1, 2 * CYCLE - 1), coils, "it closes after 2.2 s uncoiled");
        assertTrue(at1 > 150, "still out wide after 1 s: " + at1);
        assertEquals(90, atIn, 6, "at the inner radius as it uncoils");
        assertTrue(nearest > 84, "never much closer than 90 px: " + nearest);
        assertTrue(farthestAfterClose > 150, "it breaks outward after the close: " + farthestAfterClose);
        assertFalse(unit.coilClockwise(), "a wave's first unit orbits counter-clockwise");
    }

    @Test
    void aPairOrbitsInOppositeDirectionsTheirCyclesTwoSecondsApart() {
        Sortie sortie = open(pair(0, NAUTILUS, 2, 30));
        while (sortie.enemyCount() < 2) {
            sortie.step(Command.NONE);
        }
        Enemy first = sortie.enemy(0);
        Enemy second = sortie.enemy(1);
        if (first.x() > second.x()) {
            Enemy swap = first;
            first = second;
            second = swap;
        }
        assertFalse(first.coilClockwise(), "the left one counter-clockwise");
        assertTrue(second.coilClockwise(), "the right one clockwise");
        int firstOpens = 0;
        int secondOpens = 0;
        double turnFirst = 0;
        double turnSecond = 0;
        Ship ship = sortie.ship();
        for (int step = 1; step <= SimStep.ticks(8); step++) {
            double a1 = StrictMath.atan2(first.y() - ship.y(), first.x() - ship.x());
            double a2 = StrictMath.atan2(second.y() - ship.y(), second.x() - ship.x());
            sortie.step(Command.NONE);
            if (step > SimStep.ticks(1.5) && first.coilPhase() == CoilPhase.CLOSED) {
                turnFirst += Math.IEEEremainder(
                        StrictMath.atan2(first.y() - ship.y(), first.x() - ship.x()) - a1, 2 * StrictMath.PI);
            }
            if (step > SimStep.ticks(1.5) && second.coilPhase() == CoilPhase.CLOSED) {
                turnSecond += Math.IEEEremainder(
                        StrictMath.atan2(second.y() - ship.y(), second.x() - ship.x()) - a2, 2 * StrictMath.PI);
            }
            if (firstOpens == 0 && first.coilPhase() == CoilPhase.OPENING) {
                firstOpens = step;
            }
            if (secondOpens == 0 && second.coilPhase() == CoilPhase.OPENING) {
                secondOpens = step;
            }
        }
        assertTrue(turnFirst > 1, "the left one turns counter-clockwise round the ship: " + turnFirst);
        assertTrue(turnSecond < -1, "the right one clockwise: " + turnSecond);
        assertEquals(SimStep.ticks(2), secondOpens - firstOpens, "the second uncoils 2 s after the first");
    }

    @Test
    void itsSpiralFiresOnlyWhileOpenSixteenVolleysOfThreeArmsTheFirstAtTheShip() {
        Sortie sortie = open(pair(0, NAUTILUS, 1, 30));
        Enemy unit = firstIn(sortie);
        Ship ship = sortie.ship();
        List<Integer> steps = new ArrayList<>();
        List<Integer> bullets = new ArrayList<>();
        Integer opened = null;
        for (int step = 1; step <= CYCLE + 10; step++) {
            int before = sortie.bulletCount();
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                assertEquals(CoilPhase.OPEN, unit.coilPhase(), "only while open");
                if (steps.isEmpty()) {
                    opened = step;
                    int n = sortie.bulletCount();
                    EnemyBullet aimed = sortie.bullet(n - 3);
                    double aim = StrictMath.atan2(ship.y() - unit.y(), ship.x() - unit.x());
                    assertEquals(aim, StrictMath.atan2(aimed.vy(), aimed.vx()), 0.05, "the first arm at the ship");
                    assertEquals(100, Math.hypot(aimed.vx(), aimed.vy()), 1e-3);
                }
                steps.add(step);
                bullets.add(sortie.bulletCount() - before);
            }
        }
        assertEquals(IN + OPENING - 1, opened, "the first volley as it opens");
        assertEquals(16, steps.size(), "a volley every 0.1 s over 1.6 s: " + steps);
        assertEquals(SimStep.ticks(0.1), steps.get(1) - steps.get(0));
        // Bullets that left the play field in a step make up for some of the later volleys' counts.
        assertEquals(3, bullets.getFirst(), "a bullet per arm");
        assertTrue(bullets.stream().allMatch(b -> b <= 3), "a bullet per arm: " + bullets);
    }

    @Test
    void easysTwoArmsAndHardsFour() {
        for (int arms : new int[] {2, 4}) {
            Sortie sortie = open(pair(0, nautilus(COIL, arms), 1, 30));
            firstIn(sortie);
            List<Integer> added = new ArrayList<>();
            for (int step = 1; step <= CYCLE; step++) {
                int before = sortie.bulletCount();
                sortie.step(Command.NONE);
                if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                    added.add(sortie.bulletCount() - before);
                }
            }
            assertEquals(16, added.size());
            assertEquals(arms, added.getFirst(), arms + " arms");
            assertTrue(added.stream().allMatch(b -> b <= arms), arms + " arms: " + added);
        }
    }

    @Test
    void closedItsShellGlancesOffAllButTheSpecialsBlastsAndAntiSubFireOpenItsCrownTakesTheDamage() {
        Sortie sortie = open(pair(0, NAUTILUS, 1, 30));
        Enemy unit = firstIn(sortie);
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(CoilPhase.CLOSED, unit.coilPhase());
        assertTrue(unit.armoured());
        assertTrue(unit.glances(0, 1), "a shot sparks off");
        assertFalse(unit.damage(10));
        assertFalse(unit.damage(10, true), "the Lance and weapon blasts glance too");
        assertEquals(100, unit.hp(), 1e-12);
        assertFalse(unit.damage(10, true, true), "a Smart Bomb or Airstrike blast goes through");
        assertEquals(90, unit.hp(), 1e-12);
        assertFalse(unit.damage(10, false, false, true), "anti-sub fire cracks the shell (§5 E8)");
        assertEquals(80, unit.hp(), 1e-12);
        while (unit.coilPhase() != CoilPhase.OPENING) {
            sortie.step(Command.NONE);
        }
        assertFalse(unit.damage(10), "still closed while it opens");
        assertEquals(80, unit.hp(), 1e-12);
        while (unit.coilPhase() != CoilPhase.OPEN) {
            sortie.step(Command.NONE);
        }
        assertFalse(unit.armoured());
        assertFalse(unit.glances(0, 1));
        assertFalse(unit.damage(10));
        assertEquals(70, unit.hp(), 1e-12);
        assertTrue(unit.damage(70), "100 HP in all");
    }

    /** The HP its closed shell lost and the glances off it while the ship fires {@code weapon} up its column. */
    private static double[] closedFire(WeaponSpec weapon) {
        Loadout loadout = TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, weapon, weapon));
        Sortie sortie = open(loadout, pair(0, NAUTILUS, 1, 30));
        Enemy unit = firstIn(sortie);
        double lost = 0;
        int glanced = 0;
        for (int i = 0; i < 2 * CYCLE && sortie.enemyCount() > 0; i++) {
            double hp = unit.hp();
            boolean closed = unit.coilPhase() != CoilPhase.OPEN;
            sortie.step(Command.FIRE.bit());
            if (closed && unit.coilPhase() != CoilPhase.OPEN) {
                lost += hp - unit.hp();
                glanced += sortie.events().count(SimEvents.Type.SHOT_GLANCED);
            }
        }
        return new double[] {lost, glanced};
    }

    @Test
    void theShipsShotsSparkOffTheClosedShellButAntiSubShotsCrackIt() {
        double[] plain = closedFire(TestSpecs.PULSE);
        assertEquals(0, plain[0], 1e-12, "no damage while closed");
        assertTrue(plain[1] > 0, "the shell sparked");
        double[] antiSub = closedFire(TestSpecs.PULSE.antiSub(true));
        assertTrue(antiSub[0] > 0, "anti-sub shots crack the closed shell");
        assertEquals(0, antiSub[1], "no spark off anti-sub shots");
    }

    @Test
    void inATrenchTooNarrowForTheSpiralItsOrbitIsClampedNearTheChannelsMiddle() {
        Sortie sortie = trench(pair(1, NAUTILUS, 2, 30));
        double half = 20;
        double nearest = Double.MAX_VALUE;
        double widest = 0;
        boolean seen = false;
        for (int step = 0; step < SimStep.ticks(16); step++) {
            // The ship hugs the left wall (110) low on the screen.
            sortie.step(toward(sortie, 140, 96));
            for (Enemy unit : AbyssRayTest.all(sortie, "spiral-nautilus")) {
                if (unit.y() > PlayField.HEIGHT - 60 || step < SimStep.ticks(3.5)) {
                    continue;
                }
                seen = true;
                assertTrue(
                        unit.x() - half > 110 - 4 && unit.x() + half < 370 + 4,
                        "clear of the walls at step " + step + ": " + unit.x());
                nearest = Math.min(nearest, distance(unit, sortie.ship()));
                widest = Math.max(widest, Math.abs(unit.x() - 240));
            }
        }
        assertTrue(seen);
        assertTrue(widest < 130, "round the channel's middle: " + widest + " / " + nearest);
        assertTrue(nearest > 84, "pushed out to 90 px from a ship hugging a wall: " + nearest);
    }

    @Test
    void afterItsWavesHoldThePairBreaksOffByTheNearerSideClosedAndIsMissed() {
        Sortie sortie = open(pair(0, NAUTILUS, 2, 15));
        while (sortie.enemyCount() < 2) {
            sortie.step(Command.NONE);
        }
        List<Enemy> units = List.of(sortie.enemy(0), sortie.enemy(1));
        int[] left = new int[2];
        double[] dir = new double[2];
        double[] lastX = new double[2];
        int destroyed = 0;
        for (int step = 1; step <= SimStep.ticks(22) && sortie.enemyCount() > 0; step++) {
            double[] before = {units.get(0).x(), units.get(1).x()};
            CoilPhase[] was = {units.get(0).coilPhase(), units.get(1).coilPhase()};
            sortie.step(Command.NONE);
            destroyed += sortie.events().count(SimEvents.Type.ENEMY_DESTROYED);
            for (int k = 0; k < 2; k++) {
                Enemy unit = units.get(k);
                if (left[k] == 0 && unit.coilPhase() == CoilPhase.LEAVING) {
                    left[k] = step;
                    assertTrue(was[k] == CoilPhase.CLOSED, "it leaves closed");
                    assertTrue(unit.armoured(), "its shell closed as it leaves");
                }
                boolean in = AbyssRayTest.all(sortie, "spiral-nautilus").contains(unit);
                if (in && left[k] > 0 && step > left[k] && unit.coilPhase() == CoilPhase.LEAVING) {
                    dir[k] = Math.signum(unit.x() - before[k]);
                    lastX[k] = unit.x();
                }
            }
        }
        assertEquals(0, sortie.enemyCount(), "both gone");
        assertEquals(0, destroyed, "missed, not destroyed");
        for (int k = 0; k < 2; k++) {
            // Its spawn's step was its hold's first.
            assertTrue(left[k] >= SimStep.ticks(15) - 1, "not before its hold's end: " + left[k]);
            assertTrue(left[k] <= SimStep.ticks(15) - 1 + CYCLE, "at its next closed moment: " + left[k]);
            assertTrue(dir[k] != 0, "sideways");
            assertTrue(dir[k] < 0 ? lastX[k] < 40 : lastX[k] > PlayField.WIDTH - 40, "off its side: " + lastX[k]);
        }
    }

    @Test
    void aPairReplaysWithoutAllocating() {
        java.util.function.Supplier<Sortie> make = () -> trench(pair(1, NAUTILUS, 2, 15));
        Sortie a = make.get();
        Sortie b = make.get();
        for (int step = 0; step < SimStep.ticks(18); step++) {
            int commands = SortieTest.Pilot.commands(step);
            a.step(commands);
            b.step(commands);
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int step = 0; step < SimStep.ticks(18); step++) {
                sortie.step(SortieTest.Pilot.commands(step));
            }
        }));
    }
}
