package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part E, step E1: the Dust Devil's vortex (design/enemies/ground/dust-devil), on hand-built specs at the
 * spec's medium numbers. A 2.6 s cycle: 1.4 s closed on {@code ground} (sinking first after an opening), a 0.4 s
 * rise, 0.8 s open on {@code low-air}; a wave's units 0.8 s apart. Closed, its vanes glance off every shot but
 * the specials' blasts (Q.22e); open, its core takes damage. Its spiral fires only while open: 2 arms (hard 3), a
 * volley every 0.16 s, the first at the ship. It swirls round its centre line with the ground, never closer than
 * 90 px to the ship, and leaves at the bottom.
 */
class VortexTest {
    static final EnemySpec.Vortex VORTEX = new EnemySpec.Vortex(1.4, 0.4, 0.8, 0.8, 50, 60, 90);
    /** Without a wander: straight down its centre line with the ground. */
    static final EnemySpec.Vortex STILL = new EnemySpec.Vortex(1.4, 0.4, 0.8, 0.8, 50, 0, 90);

    static EnemyGun spiralGun(int arms) {
        return EnemyGun.aimed(TestSpecs.INFINITE, 0, 1, 110, 4, false)
                .withSpiral(Optional.of(new EnemyGun.SpiralSpec(arms, StrictMath.PI, 0.16)));
    }

    /** A Dust Devil at medium: 52 HP, a 26×26 core, its vortex, the 2-arm spiral and the pull. */
    static EnemySpec devil(EnemySpec.Vortex vortex, Optional<EnemyGun> gun) {
        return new EnemySpec(
                        "dust-devil",
                        52,
                        new Hitbox(26, 26),
                        Layer.GROUND,
                        0,
                        false,
                        26,
                        50,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        gun,
                        Optional.empty(),
                        Optional.empty(),
                        false)
                .withVortex(Optional.of(vortex))
                .withPull(Optional.of(PullTest.PULL));
    }

    static EnemySpec devil() {
        return devil(VORTEX, Optional.of(spiralGun(2)));
    }

    static WaveSpec wave(double t, EnemySpec enemy, int count) {
        return TestSpecs.wave(
                t, WaveSpec.Formation.LINE_ABREAST, enemy, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
    }

    /** A level at 130 px/s (a devil crosses the screen in about 4.4 s). */
    static Sortie sortie(WaveSpec... waves) {
        return sortie(130, TestSpecs.LOADOUT, waves);
    }

    /** A level at 40 px/s, so a devil stays on the screen for several cycles. */
    static Sortie slow(WaveSpec... waves) {
        return sortie(40, TestSpecs.LOADOUT, waves);
    }

    static Sortie sortie(double scroll, Loadout loadout, WaveSpec... waves) {
        LevelScript level = new LevelScript(
                1,
                1,
                0,
                List.of(new LevelScript.Section(40, scroll)),
                List.of(waves),
                List.of(),
                List.of(),
                0,
                List.of(),
                new LevelScript.Secondary(0.8, 50),
                List.of());
        return new Sortie(1, loadout, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Steps until the first unit is in, returning it. */
    static Enemy firstIn(Sortie sortie) {
        for (int i = 0; i < SimStep.PER_SECOND && sortie.enemyCount() == 0; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(1, Math.min(1, sortie.enemyCount()), "a unit came in");
        return sortie.enemy(0);
    }

    @Test
    void aVortexIsAPlainGroundUnitsAndItsNumbersHold() {
        assertThrows(IllegalArgumentException.class, () -> PartASpecs.dummy(Layer.AIR, 52)
                .withVortex(Optional.of(VORTEX)));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Vortex(0, 0.4, 0.8, 0.8, 50, 60, 90));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Vortex(1.4, 0.4, 0, 0.8, 50, 60, 90));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Vortex(1.4, 0.4, 0.8, -1, 50, 60, 90));
        assertThrows(IllegalArgumentException.class, () -> new EnemyGun.SpiralSpec(0, 1, 0.16));
        assertThrows(IllegalArgumentException.class, () -> new EnemyGun.SpiralSpec(2, 1, 0));
        assertEquals(2.6, VORTEX.cycleSeconds(), 1e-12);
        assertTrue(devil().gun().orElseThrow().spiral().isPresent());
        assertEquals(Enemy.VortexPhase.CLOSED, new Enemy().vortexPhase(), "any other unit reads closed");
    }

    @Test
    void itsCycleIsClosedRisingOpenThenSinkingOnTheGroundAndOpenOnLowAir() {
        Sortie sortie = slow(wave(0, devil(), 1));
        Enemy unit = firstIn(sortie);
        List<Integer> rising = new ArrayList<>();
        List<Integer> opened = new ArrayList<>();
        List<Integer> closed = new ArrayList<>();
        Map<Enemy.VortexPhase, Integer> seen = new HashMap<>();
        for (int i = 1; i <= 3 * 156 + 20; i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case VORTEX_RISING -> rising.add(i);
                    case VORTEX_OPENED -> opened.add(i);
                    case VORTEX_CLOSED -> closed.add(i);
                    default -> {}
                }
            }
            Enemy.VortexPhase phase = unit.vortexPhase();
            seen.merge(phase, 1, Integer::sum);
            boolean open = phase == Enemy.VortexPhase.OPEN;
            assertEquals(open ? Layer.LOW_AIR : Layer.GROUND, unit.layer(), "step " + i + " " + phase);
            assertEquals(!open, unit.armoured(), "its vanes are closed but while it is open");
            assertTrue(unit.vortexProgress() >= 0 && unit.vortexProgress() <= 1, "progress " + unit.vortexProgress());
            if (opened.isEmpty()) {
                assertNotEquals(Enemy.VortexPhase.SINKING, phase, "no sinking before its first opening");
            }
        }
        // The unit moved once in the step it came in: its cycle is one step on.
        assertEquals(84 - 1, (int) rising.getFirst(), "1.4 s closed from its entry");
        for (int k = 0; k < 3; k++) {
            assertEquals(24, opened.get(k) - rising.get(k), "a 0.4 s rise");
            assertEquals(48, closed.get(k) - opened.get(k), "a 0.8 s window");
        }
        assertEquals(156, rising.get(1) - rising.get(0), "a 2.6 s cycle");
        assertEquals(156, rising.get(2) - rising.get(1));
        assertEquals(3 * 48, seen.get(Enemy.VortexPhase.OPEN), "3 windows of 48 steps");
        assertEquals(3 * 24, seen.get(Enemy.VortexPhase.RISING));
        assertEquals(2 * 12 + 12, seen.get(Enemy.VortexPhase.SINKING), "0.2 s sinking after each closing");
    }

    @Test
    void aWavesUnitsOpenTheirWindowsOffsetSoTheyAlternate() {
        Sortie sortie = slow(wave(0, devil(), 3));
        Map<Integer, Integer> firstOpen = new HashMap<>();
        for (int i = 1; i <= 5 * SimStep.PER_SECOND; i++) {
            sortie.step(Command.NONE);
            for (int u = 0; u < sortie.enemyCount(); u++) {
                Enemy unit = sortie.enemy(u);
                if (unit.vortexPhase() == Enemy.VortexPhase.OPEN) {
                    firstOpen.putIfAbsent(unit.serial(), i);
                }
            }
        }
        List<Integer> opens = new ArrayList<>(firstOpen.values());
        java.util.Collections.sort(opens);
        assertEquals(3, opens.size(), "each opened: " + firstOpen);
        assertEquals(48, opens.get(1) - opens.get(0), "0.8 s apart");
        assertEquals(48, opens.get(2) - opens.get(1));
    }

    @Test
    void itSwirlsRoundItsCentreLineWithTheGroundKeepsOffTheShipAndLeavesAtTheBottom() {
        Sortie sortie = sortie(wave(0, devil(), 1));
        Enemy unit = firstIn(sortie);
        Ship ship = sortie.ship();
        double centreX = PlayField.WIDTH / 2.0;
        double startY = unit.y();
        double scroll = 130 * SimStep.SECONDS;
        int steps = 0;
        boolean nearShip = false;
        double minDistance = Double.POSITIVE_INFINITY;
        while (sortie.enemyCount() > 0 && steps < 10 * SimStep.PER_SECOND) {
            double x = unit.x();
            double y = unit.y();
            sortie.step(Command.NONE);
            steps++;
            if (sortie.enemyCount() == 0) {
                break;
            }
            double distance = Math.hypot(unit.x() - ship.x(), unit.y() - ship.y());
            minDistance = Math.min(minDistance, distance);
            assertTrue(distance >= 90 - 1e-9, "never closer than 90 px to the ship: " + distance);
            nearShip |= distance < 90 + 1e-6;
            if (unit.y() > ship.y() + 160) {
                assertTrue(Math.abs(unit.x() - centreX) <= 60 + 1e-9, "within its swirl: " + unit.x());
                double centreY = startY - scroll * steps;
                assertTrue(Math.abs(unit.y() - centreY) <= 30 + 2, "round its centre line: " + unit.y());
                double over = Math.hypot(unit.x() - x, unit.y() - (y - scroll));
                assertTrue(over <= 2 * 50 * SimStep.SECONDS + 1e-9, "at most twice its wander speed over the ground");
            }
        }
        assertEquals(0, sortie.enemyCount(), "it left at the bottom");
        assertTrue(nearShip, "it slid round the ship at 90 px: " + minDistance);
        assertTrue(steps > 4 * SimStep.PER_SECOND, "with the ground (130 px/s over 540 px): " + steps);
        assertEquals(0, sortie.events().count(SimEvents.Type.ENEMY_DESTROYED));
    }

    @Test
    void closedItsVanesGlanceOffAllButTheSpecialsBlastsOpenItsCoreTakesTheDamage() {
        Sortie sortie = slow(wave(0, devil(STILL, Optional.of(spiralGun(2))), 1));
        Enemy unit = firstIn(sortie);
        while (unit.y() > PlayField.HEIGHT - 20) {
            sortie.step(Command.NONE);
        }
        assertEquals(Enemy.VortexPhase.CLOSED, unit.vortexPhase());
        assertFalse(unit.damage(10));
        assertFalse(unit.damage(10, true), "the Lance and weapon blasts glance too");
        assertEquals(52, unit.hp(), 1e-12);
        assertTrue(sortie.events().count(SimEvents.Type.SHOT_GLANCED) >= 1, "the grey spark");
        assertTrue(unit.glances(0, 1), "a shot sparks off");
        assertFalse(unit.damage(10, true, true), "a Smart Bomb or Airstrike blast goes through (Q.22e)");
        assertEquals(42, unit.hp(), 1e-12);
        while (unit.vortexPhase() != Enemy.VortexPhase.OPEN) {
            sortie.step(Command.NONE);
        }
        assertFalse(unit.glances(0, 1));
        assertFalse(unit.damage(10));
        assertEquals(32, unit.hp(), 1e-12);
        assertTrue(unit.damage(40), "52 HP in all");
    }

    @Test
    void theShipsShotsSparkOffWhileItIsClosedAndHitItsCoreWhileItIsOpen() {
        // Straight down the ship's column: the Pulse Cannon's shots meet it.
        Sortie sortie = slow(wave(0, devil(STILL, Optional.of(spiralGun(2))), 1));
        Enemy unit = firstIn(sortie);
        int fire = Command.FIRE.bit();
        int glancedClosed = 0;
        int hitsOpen = 0;
        double hp = unit.hp();
        for (int i = 0; i < 4 * SimStep.PER_SECOND && sortie.enemyCount() > 0; i++) {
            sortie.step(fire);
            if (sortie.enemyCount() == 0) {
                break;
            }
            boolean open = unit.vortexPhase() == Enemy.VortexPhase.OPEN;
            int glanced = sortie.events().count(SimEvents.Type.SHOT_GLANCED);
            int hits = sortie.events().count(SimEvents.Type.ENEMY_HIT);
            if (open) {
                hitsOpen += hits;
                assertEquals(0, glanced, "no spark while open");
            } else {
                glancedClosed += glanced;
                assertEquals(0, hits, "no hit while closed");
                assertEquals(hp, unit.hp(), 1e-12, "no damage while closed");
            }
            hp = unit.hp();
        }
        assertTrue(glancedClosed > 0, "the vanes sparked");
        assertTrue(hitsOpen > 0, "the core was hit");
    }

    /** The steps the first unit's ENEMY_FIRED came on and the bullets each added. */
    private record Volleys(List<Integer> steps, List<Integer> bullets, List<Integer> opened) {}

    private static Volleys volleys(Sortie sortie, int steps) {
        List<Integer> at = new ArrayList<>();
        List<Integer> added = new ArrayList<>();
        List<Integer> opened = new ArrayList<>();
        for (int i = 1; i <= steps && sortie.enemyCount() > 0; i++) {
            int before = sortie.bulletCount();
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.VORTEX_OPENED) > 0) {
                opened.add(i);
            }
            if (sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                assertEquals(Enemy.VortexPhase.OPEN, sortie.enemy(0).vortexPhase(), "only while open");
                at.add(i);
                added.add(sortie.bulletCount() - before);
            }
        }
        return new Volleys(at, added, opened);
    }

    @Test
    void itsSpiralFiresOnlyWhileOpenFiveVolleysOfTwoArmsAWindowTheFirstAtTheShip() {
        Sortie sortie = slow(wave(0, devil(), 1));
        Enemy unit = firstIn(sortie);
        Ship ship = sortie.ship();
        while (sortie.events().count(SimEvents.Type.VORTEX_OPENED) == 0) {
            sortie.step(Command.NONE);
        }
        assertEquals(1, sortie.events().count(SimEvents.Type.ENEMY_FIRED), "the first volley as it opens");
        int n = sortie.bulletCount();
        assertEquals(2, n);
        double aim = StrictMath.atan2(ship.y() - unit.y(), ship.x() - unit.x());
        EnemyBullet first = sortie.bullet(n - 2);
        EnemyBullet second = sortie.bullet(n - 1);
        assertEquals(110, Math.hypot(first.vx(), first.vy()), 1e-5, "slow grit");
        assertEquals(aim, StrictMath.atan2(first.vy(), first.vx()), 1e-3, "the first arm at the ship");
        assertEquals(-first.vx(), second.vx(), 1e-3, "the second arm opposite");
        Volleys volleys = volleys(sortie, 156 + 100);
        // The rest of this window (4 more, 10 steps apart), then the next whole window.
        assertEquals(4 + 5, volleys.steps().size(), "5 volleys a window: " + volleys.steps());
        assertEquals(10, volleys.steps().get(1) - volleys.steps().get(0), "every 0.16 s");
        assertTrue(volleys.bullets().stream().allMatch(b -> b == 2), "a bullet per arm: " + volleys.bullets());
        assertEquals(volleys.opened().getFirst(), volleys.steps().get(4), "the next window's first volley as it opens");
    }

    @Test
    void hardsSpiralHasThreeArms() {
        Sortie sortie = slow(wave(0, devil(VORTEX, Optional.of(spiralGun(3))), 1));
        firstIn(sortie);
        Volleys volleys = volleys(sortie, 156 + 30);
        assertEquals(5, volleys.steps().size());
        assertTrue(volleys.bullets().stream().allMatch(b -> b == 3), "three arms: " + volleys.bullets());
    }

    @Test
    void vorticesTheirPullAndTheirHeldPickupsDoNotAllocate() {
        long allocated = Allocations.least(() -> slow(wave(0, devil(), 3)), sortie -> {
            for (int i = 0; i < SimStep.ticks(8); i++) {
                if (i == 60) {
                    Enemy unit = sortie.enemy(0);
                    sortie.drop(PickupType.SMALL_SALVAGE, unit.x(), unit.y() - 60);
                }
                sortie.step(SortieTest.Pilot.commands(i) | Command.FIRE.bit());
            }
        });
        assertEquals(0, allocated, "8 s allocated " + allocated + " bytes");
    }

    @Test
    void aWaveReplaysAndEachUnitSwirlsOnItsOwnSeed() {
        Sortie a = slow(wave(0, devil(), 2));
        Sortie b = slow(wave(0, devil(), 2));
        for (int i = 0; i < 3 * SimStep.PER_SECOND; i++) {
            a.step(Command.NONE);
            b.step(Command.NONE);
        }
        assertEquals(a.stateHash(), b.stateHash());
        Enemy left = a.enemy(0);
        Enemy right = a.enemy(1);
        double dx = left.x()
                - left.vortexCycle().centreX()
                - (right.x() - right.vortexCycle().centreX());
        assertNotEquals(0, dx, 1e-6, "their swirls differ");
    }
}
