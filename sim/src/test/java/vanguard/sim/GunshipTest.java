package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part C, step C1 (design/enemies/air/gilded-gunship, design/enemies/air/dart-drone): the Gunship's facing armour
 * (the shield first, then front ×0.5 within ±45°, engines ×2 within ±60°, the sides plain; blasts neither), its ring
 * fired all the way down the field, and its Dart streams: 3 every 5 s, 0.25 s apart, spread round its facing, straight
 * 0.4 s then a 60°/s chase for 3 s then straight on, at most 6 alive, counted as they launch.
 */
class GunshipTest {
    private static LevelScript level(List<WaveSpec> waves) {
        return LevelScript.builder(30, 5)
                .sections(List.of(new Section(60, 80)))
                .waves(waves)
                .ground(GroundSpec.SPACE)
                .objectives(ObjectiveSpec.NONE)
                .build();
    }

    private static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** A plated Gunship on the field (its wave at 0.2 s, 1.5 s later). */
    private static Enemy plated(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(1.7); i++) {
            sortie.step(Command.NONE);
        }
        return sortie.enemy(0);
    }

    @Test
    void theShieldSpendsItselfFirstThenTheArcsFactorTheOverflow() {
        double[][] cases = {
            // shot direction (vx, vy) and the HP left after a 20-point hit: 12 shield, 8 overflow
            {0, 1, 88 - 4}, // from in front (flying up into its nose): × 0.5
            {Math.sin(Math.toRadians(40)), Math.cos(Math.toRadians(40)), 88 - 4}, // inside ±45°
            {1, 0, 88 - 8}, // from the side
            {Math.sin(Math.toRadians(50)), Math.cos(Math.toRadians(50)), 88 - 8}, // just outside the front arc
            {0, -1, 88 - 16}, // from behind, into the engines: × 2
            {Math.sin(Math.toRadians(55)), -Math.cos(Math.toRadians(55)), 88 - 16}, // inside ±60° of its rear
        };
        for (double[] c : cases) {
            Sortie sortie = sortie(level(List.of(Act5CSpecs.gunship(0.2, Act5CSpecs.PLATED))));
            Enemy gunship = plated(sortie);
            assertEquals(12, gunship.shield(), 1e-9);
            gunship.damageFacing(20, false, c[0], c[1]);
            assertEquals(0, gunship.shield(), 1e-9, "the shield first, at full damage");
            assertEquals(c[2], gunship.hp(), 1e-9, "direction " + c[0] + ", " + c[1]);
        }
    }

    @Test
    void aHitTheShieldTakesWholeIsNotFactoredAndBlastsTakeNoArc() {
        Sortie sortie = sortie(level(List.of(Act5CSpecs.gunship(0.2, Act5CSpecs.PLATED))));
        Enemy gunship = plated(sortie);
        gunship.damageFacing(10, false, 0, -1);
        assertEquals(2, gunship.shield(), 1e-9, "10 of the shield's 12, the engines' factor unused");
        assertEquals(88, gunship.hp(), 1e-9);
        gunship.damage(10, true, true);
        assertEquals(88 - 8, gunship.hp(), 1e-9, "a blast from above: the 8 past the shield, no factor");
    }

    @Test
    void thePlayersBoltsFromBelowHitTheArmourPlate() {
        Sortie sortie = sortie(level(List.of(Act5CSpecs.gunship(0.2, Act5CSpecs.PLATED))));
        int front = 0;
        int rear = 0;
        double lastHp = 88;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.FIRE.bit());
            SimEvents events = sortie.events();
            for (int k = 0; k < events.size(); k++) {
                if (events.type(k) == SimEvents.Type.PLATING_HIT) {
                    front += events.value(k) == ArmourArcs.FRONT ? 1 : 0;
                    rear += events.value(k) == ArmourArcs.REAR ? 1 : 0;
                }
            }
            if (sortie.enemyCount() > 0) {
                lastHp = sortie.enemy(0).hp();
            }
        }
        assertTrue(front > 0, "the plate flashes");
        assertEquals(0, rear);
        assertTrue(lastHp < 88, "and the hull takes half");
    }

    @Test
    void facingArmourIsAPlainFlyersAndItsArcsDoNotOverlap() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.FacingArmour(2, 0.5, 2, 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> WardenTankTest.TANK.withFacingArmour(Optional.of(Act5CSpecs.ARCS)),
                "a walker has its frontal armour and rear plate");
    }

    // --- the ring and the Darts -------------------------------------------------------------------------------

    private static final class Dart {
        int launched = -1;
        double vx0;
        double vy0;
        double headingAtChase = Double.NaN;
        int chaseStart = -1;
        int chaseEnd = -1;
        double lastHeading;
    }

    @Test
    void itLaunchesStreamsOfThreeThatFlyStraightThenChaseThenFlyOn() {
        Sortie sortie = sortie(level(List.of(Act5CSpecs.gunship(0.2, Act5CSpecs.GUNSHIP))));
        Map<Integer, Dart> darts = new HashMap<>();
        List<Integer> launches = new ArrayList<>();
        int rings = 0;
        int released = 0;
        int mostAlive = 0;
        for (int i = 0; i < SimStep.ticks(14); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int k = 0; k < events.size(); k++) {
                if (events.type(k) == SimEvents.Type.LAUNCHED) {
                    launches.add(i);
                }
                if (events.type(k) == SimEvents.Type.ENEMY_FIRED) {
                    rings++;
                }
            }
            int alive = 0;
            for (int k = 0; k < sortie.enemyCount(); k++) {
                Enemy u = sortie.enemy(k);
                if (!u.spec().slug().equals("dart-drone")) {
                    continue;
                }
                alive++;
                Dart d = darts.computeIfAbsent(u.serial(), s -> new Dart());
                if (d.launched < 0) {
                    d.launched = i;
                    d.vx0 = u.vx;
                    d.vy0 = u.vy;
                    assertTrue(u.launchedBy() >= 0);
                }
                if (u.latchPhase() == Enemy.LatchPhase.CHASE && d.chaseStart < 0) {
                    d.chaseStart = i;
                }
                if (u.latchPhase() == Enemy.LatchPhase.ON && d.chaseEnd < 0) {
                    d.chaseEnd = i;
                }
            }
            mostAlive = Math.max(mostAlive, alive);
        }
        released = sortie.enemyTotal();
        assertTrue(launches.size() >= 6, "two streams at least: " + launches.size());
        assertEquals(SimStep.ticks(0.25), launches.get(1) - launches.get(0), "0.25 s apart");
        assertEquals(SimStep.ticks(0.25), launches.get(2) - launches.get(1));
        assertEquals(SimStep.ticks(5), launches.get(3) - launches.get(0), "a stream every 5 s");
        assertTrue(mostAlive <= 6, "at most 6 alive: " + mostAlive);
        assertTrue(rings >= 3, "its ring every 3 s on the way down: " + rings);
        assertTrue(released >= 1 + launches.size(), "the Darts count among the level's units as they launch");
        // A stream's spread: the first to its right... the middle straight down, the last to its left (±30°).
        List<Dart> first = darts.values().stream()
                .sorted(java.util.Comparator.comparingInt(d -> d.launched))
                .limit(3)
                .toList();
        assertEquals(-240, first.get(1).vy0, 1e-3, "the middle one straight down");
        assertEquals(0, first.get(1).vx0, 1e-3);
        assertEquals(240 * Math.sin(Math.toRadians(30)), Math.abs(first.get(0).vx0), 1e-3);
        for (Dart d : first) {
            assertEquals(SimStep.ticks(0.4), d.chaseStart - d.launched, 2, "straight 0.4 s, then its chase");
            assertTrue(d.chaseEnd < 0 || d.chaseEnd - d.chaseStart >= SimStep.ticks(3) - 1, "a 3 s chase");
        }
    }

    @Test
    void aStunnedOrDeadGunshipLaunchesNothingMore() {
        Sortie sortie = sortie(level(List.of(Act5CSpecs.gunship(0.2, Act5CSpecs.GUNSHIP))));
        int launches = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(Command.NONE);
            launches += sortie.events().count(SimEvents.Type.LAUNCHED);
        }
        assertEquals(3, launches, "its first stream");
        for (int k = sortie.enemyCount() - 1; k >= 0; k--) {
            if (sortie.enemy(k).spec().slug().equals("gilded-gunship")) {
                sortie.destroyEnemy(k);
            }
        }
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.NONE);
            launches += sortie.events().count(SimEvents.Type.LAUNCHED);
        }
        assertEquals(3, launches);
    }

    @Test
    void aLaunchedUnitIsAPlainAirUnit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpec.Launcher(Act5CSpecs.GUNSHIP, 3, 5, 0.25, 1, 240, 1, 0.4, 6));
        assertThrows(
                IllegalArgumentException.class,
                () -> new EnemySpec.Launcher(Act5CSpecs.DART, 3, 0.4, 0.25, 1, 240, 1, 0.4, 6),
                "the stream fits its cycle");
    }
}
