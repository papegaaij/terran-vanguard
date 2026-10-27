package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M8 part F, step F1: the Glow Angler's lunge (design/enemies/naval/glow-angler; m8f-gaps.md §1.1, §5 F2). It hangs
 * drifting with the water (a field's current at 20 px/s on top of the scroll) and, armed and on the play field with the
 * ship's centre within 130 px of its own (the lure inside that), lunges at 220 px/s at where the ship was then,
 * stopping 60 px short of that point or at 0.6 s; its ring of 8 (100 px/s, × 0.7 under water) is then due and fires as
 * soon as the ship is far enough for bullets to spawn, while it retreats 1.5 s at 100 px/s away from the ship; a ring
 * that never finds room is dropped. It re-arms 5 s after its ring. Easy and hard change only its numbers (trigger
 * 100 / 160, ring 6 / 10). Deterministic and allocation-free.
 */
class GlowAnglerTest {
    /** Medium: trigger 130 px, 220 px/s for 0.6 s, 60 px short, a ring of 8 at 100 px/s (4 each), 100 px/s for 1.5 s, 5 s. */
    static final EnemySpec.Lunge LUNGE = new EnemySpec.Lunge(130, 220, 0.6, 60, 8, 100, 4, 100, 1.5, 5);

    static final EnemySpec.Lunge EASY = new EnemySpec.Lunge(100, 220, 0.6, 60, 6, 100, 4, 100, 1.5, 5);
    static final EnemySpec.Lunge HARD = new EnemySpec.Lunge(160, 220, 0.6, 60, 10, 100, 4, 100, 1.5, 5);

    /** The scroll of the test levels, px/s. */
    private static final double SCROLL = 40;

    /** 40×32 / 30×24, 28 HP (L26's beam era), contact 10 (destroyed by ramming), bounty 14, drift 20 px/s. */
    static EnemySpec angler(EnemySpec.Lunge lunge) {
        return EnemySpec.of("glow-angler", 28, new Hitbox(30, 24), Layer.AIR, 10, true, 14, 20)
                .withLunge(Optional.of(lunge));
    }

    static final EnemySpec ANGLER = angler(LUNGE);

    /** One unit of {@code enemy} at x 240, 25 px above the top edge, its current straight down. */
    static WaveSpec one(double t, EnemySpec enemy) {
        return DriftjellyTest.field(t, enemy, 1, new WaveSpec.Field(240, 50, 50, 50, 0));
    }

    /** A 40 s level at {@link #SCROLL} px/s. */
    static LevelScript level(WaveSpec... waves) {
        return LevelScript.builder(26, 4)
                .sections(List.of(new Section(40, SCROLL)))
                .waves(List.of(waves))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    private static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    private static double distance(Enemy unit, double x, double y) {
        return Math.hypot(unit.x() - x, unit.y() - y);
    }

    /** Steps (the ship idle) until {@code type} happens, at most {@code seconds}; returns the steps taken, -1 if not. */
    private static int until(Sortie sortie, SimEvents.Type type, double seconds) {
        for (int step = 1; step <= SimStep.ticks(seconds); step++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(type) > 0) {
                return step;
            }
        }
        return -1;
    }

    @Test
    void aLungeIsAPlainAirUnitsAndItsNumbersHold() {
        assertThrows(
                IllegalArgumentException.class, () -> new EnemySpec.Lunge(60, 220, 0.6, 60, 8, 100, 4, 100, 1.5, 5));
        assertThrows(
                IllegalArgumentException.class, () -> new EnemySpec.Lunge(130, 220, 0.6, 60, 0, 100, 4, 100, 1.5, 5));
        assertThrows(
                IllegalArgumentException.class, () -> new EnemySpec.Lunge(130, 0, 0.6, 60, 8, 100, 4, 100, 1.5, 5));
        assertThrows(
                IllegalArgumentException.class, () -> new EnemySpec.Lunge(130, 220, 0.6, 60, 8, 100, 4, 100, -1, 5));
        assertThrows(
                IllegalArgumentException.class,
                () -> PartASpecs.dummy(Layer.GROUND, 20).withLunge(Optional.of(LUNGE)),
                "an air unit's");
        assertThrows(
                IllegalArgumentException.class,
                () -> SpiralNautilusTest.NAUTILUS.withLunge(Optional.of(LUNGE)),
                "not beside a coil or a gun: its ring is its attack");
        assertEquals(Optional.of(LUNGE), ANGLER.lunge());
        assertEquals(Optional.empty(), SpiralNautilusTest.NAUTILUS.lunge(), "no other unit lunges");

        Sortie jelly = sortie(
                level(DriftjellyTest.field(0, DriftjellyTest.jelly(), 1, new WaveSpec.Field(240, 50, 50, 50, 0))));
        jelly.step(Command.NONE);
        assertEquals(LurePhase.NONE, jelly.enemy(0).lurePhase(), "any other unit reads none");
    }

    @Test
    void itHangsDriftingWithTheWaterUntilTheShipIsWithin130() {
        Sortie sortie = sortie(level(one(0, ANGLER)));
        sortie.step(Command.NONE);
        Enemy unit = sortie.enemy(0);
        Ship ship = sortie.ship();
        assertEquals(LurePhase.DRIFT, unit.lurePhase());
        assertFalse(unit.grounded(), "an air unit: its kill pays as a kill, not a ground target");
        assertTrue(unit.y() > PlayField.HEIGHT, "it enters above the top edge with the water");
        double before = distance(unit, ship.x(), ship.y());
        int steps = 0;
        while (sortie.events().count(SimEvents.Type.LURE_LUNGE) == 0 && steps < SimStep.ticks(20)) {
            double y = unit.y();
            double x = unit.x();
            before = distance(unit, ship.x(), ship.y());
            sortie.step(Command.NONE);
            steps++;
            if (sortie.events().count(SimEvents.Type.LURE_LUNGE) == 0) {
                assertEquals(LurePhase.DRIFT, unit.lurePhase());
                assertEquals(x, unit.x(), 1e-9, "its current is straight down");
                assertEquals((SCROLL + 20) * SimStep.SECONDS, y - unit.y(), 1e-9, "the scroll and its 20 px/s drift");
            }
        }
        double at = distance(unit, ship.x(), ship.y());
        assertTrue(at <= 130 && before > 130, "it triggers as the ship comes within 130 px: " + before + " → " + at);
        assertEquals(LurePhase.LUNGE, unit.lurePhase());
        assertEquals(0, unit.facing(), 1e-9, "facing the ship below it");
    }

    @Test
    void itDivesAt220StoppingSixtyShortThenRingsWithRoomAndRetreatsForOnePointFiveSeconds() {
        Sortie sortie = sortie(level(one(0, ANGLER)));
        assertTrue(until(sortie, SimEvents.Type.LURE_LUNGE, 20) > 0);
        Enemy unit = sortie.enemy(0);
        Ship ship = sortie.ship();
        double toX = ship.x();
        double toY = ship.y();
        int lungeSteps = 0;
        while (unit.lurePhase() == LurePhase.LUNGE) {
            double x = unit.x();
            double y = unit.y();
            sortie.step(Command.NONE);
            lungeSteps++;
            double moved = Math.hypot(unit.x() - x, unit.y() - y);
            assertTrue(moved <= 220 * SimStep.SECONDS + 1e-9, "at 220 px/s: " + moved);
            if (unit.lurePhase() == LurePhase.LUNGE) {
                assertEquals(220 * SimStep.SECONDS, moved, 1e-9, "at its full speed until its stop");
            }
        }
        assertTrue(lungeSteps <= SimStep.ticks(0.6), "within 0.6 s: " + lungeSteps);
        assertEquals(60, distance(unit, toX, toY), 1e-6, "60 px short of where the ship was");
        assertEquals(LurePhase.RETREAT, unit.lurePhase());
        assertEquals(0, sortie.events().count(SimEvents.Type.LURE_RING), "no bullet spawns within 72 px of the ship");

        int bullets = sortie.bulletCount();
        int retreat = 0;
        int ringAt = -1;
        while (unit.lurePhase() == LurePhase.RETREAT) {
            double x = unit.x();
            double y = unit.y();
            sortie.step(Command.NONE);
            retreat++;
            if (unit.lurePhase() == LurePhase.RETREAT) {
                assertEquals(100 * SimStep.SECONDS, Math.hypot(unit.x() - x, unit.y() - y), 1e-9, "at 100 px/s");
                assertTrue(unit.y() > y, "away from the ship below it");
            }
            if (sortie.events().count(SimEvents.Type.LURE_RING) > 0) {
                assertEquals(-1, ringAt, "one ring");
                ringAt = retreat;
                assertTrue(distance(unit, ship.x(), ship.y()) >= 72, "with room for its bullets");
                assertEquals(1, sortie.events().count(SimEvents.Type.ENEMY_FIRED));
                assertEquals(bullets + 8, sortie.bulletCount(), "a ring of 8");
                for (int i = bullets; i < sortie.bulletCount(); i++) {
                    EnemyBullet bullet = sortie.bullet(i);
                    assertEquals(100, Math.hypot(bullet.vx(), bullet.vy()), 1e-9, "at 100 px/s in the air");
                    assertEquals(4, bullet.damage(), 1e-12);
                }
            }
        }
        assertTrue(ringAt > 0 && ringAt < SimStep.ticks(0.2), "as soon as it has room: " + ringAt);
        assertEquals(SimStep.ticks(1.5), retreat, "it retreats for 1.5 s");
        assertEquals(LurePhase.DRIFT, unit.lurePhase());
    }

    @Test
    void itReArmsFiveSecondsAfterItsRing() {
        Sortie sortie = sortie(level(one(0, ANGLER)));
        assertTrue(until(sortie, SimEvents.Type.LURE_LUNGE, 20) > 0);
        assertTrue(until(sortie, SimEvents.Type.LURE_RING, 2) > 0);
        Enemy unit = sortie.enemy(0);
        Ship ship = sortie.ship();
        int within = -1;
        for (int step = 1; step <= SimStep.ticks(5); step++) {
            sortie.step(Command.NONE);
            if (within < 0 && unit.lurePhase() == LurePhase.DRIFT && distance(unit, ship.x(), ship.y()) <= 130) {
                within = step;
            }
            if (step < SimStep.ticks(5)) {
                assertEquals(0, sortie.events().count(SimEvents.Type.LURE_LUNGE), "not re-armed at step " + step);
            } else {
                assertEquals(1, sortie.events().count(SimEvents.Type.LURE_LUNGE), "re-armed 5 s after its ring");
            }
        }
        assertTrue(within > 0 && within < SimStep.ticks(5), "the ship was within its trigger before: " + within);
    }

    @Test
    void aRingThatNeverFindsRoomIsDroppedAtItsRetreatsEnd() {
        // It does not back away: the idle ship stays 60 px from it, closer than bullets may spawn.
        EnemySpec still = angler(new EnemySpec.Lunge(130, 220, 0.6, 60, 8, 100, 4, 0, 1.5, 5));
        Sortie sortie = sortie(level(one(0, still)));
        assertTrue(until(sortie, SimEvents.Type.LURE_LUNGE, 20) > 0);
        Enemy unit = sortie.enemy(0);
        int steps = 0;
        while (unit.lurePhase() != LurePhase.DRIFT) {
            sortie.step(Command.NONE);
            steps++;
            assertEquals(0, sortie.events().count(SimEvents.Type.LURE_RING), "no room: no ring");
        }
        assertTrue(steps <= SimStep.ticks(0.6) + SimStep.ticks(1.5), "back to its drift after its retreat: " + steps);
        sortie.step(Command.NONE);
        assertEquals(LurePhase.DRIFT, unit.lurePhase(), "re-arming, not lunging again at once");
        assertEquals(0, sortie.bulletCount(), "the dropped ring never fires");
    }

    @Test
    void easyAndHardChangeItsTriggerAndItsRing() {
        for (EnemySpec.Lunge lunge : List.of(EASY, HARD)) {
            Sortie sortie = sortie(level(one(0, angler(lunge))));
            int lunged = until(sortie, SimEvents.Type.LURE_LUNGE, 20);
            assertTrue(lunged > 0);
            Enemy unit = sortie.enemy(0);
            double at = distance(unit, sortie.ship().x(), sortie.ship().y());
            assertTrue(
                    at <= lunge.trigger() && at > lunge.trigger() - (SCROLL + 20) * SimStep.SECONDS - 1e-9,
                    "triggered at " + lunge.trigger() + " px: " + at);
            assertTrue(until(sortie, SimEvents.Type.LURE_RING, 2) > 0);
            assertEquals(lunge.ring(), sortie.bulletCount(), "a ring of " + lunge.ring());
        }
    }

    @Test
    void underWaterItsRingCarriesTheWatersFactor() {
        Sortie sortie = sortie(UnderwaterTest.under(level(one(0, ANGLER))));
        assertTrue(until(sortie, SimEvents.Type.LURE_LUNGE, 20) > 0);
        assertTrue(until(sortie, SimEvents.Type.LURE_RING, 2) > 0);
        assertEquals(8, sortie.bulletCount());
        for (int i = 0; i < sortie.bulletCount(); i++) {
            assertEquals(70, Math.hypot(sortie.bullet(i).vx(), sortie.bullet(i).vy()), 1e-9, "100 px/s × 0.7");
        }
    }

    @Test
    void aFieldReplaysWithoutAllocating() {
        WaveSpec field = DriftjellyTest.field(1, ANGLER, 6, new WaveSpec.Field(240, 360, 240, 80, 0.2));
        Supplier<Sortie> make = () -> sortie(level(field, one(9, ANGLER)));
        Sortie a = make.get();
        Sortie b = make.get();
        int lunges = 0;
        int rings = 0;
        for (int step = 0; step < SimStep.ticks(20); step++) {
            int commands = SortieTest.Pilot.commands(step);
            a.step(commands);
            b.step(commands);
            lunges += a.events().count(SimEvents.Type.LURE_LUNGE);
            rings += a.events().count(SimEvents.Type.LURE_RING);
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
        assertTrue(lunges >= 3 && rings >= 1, "the field lunged and rang: " + lunges + ", " + rings);
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int step = 0; step < SimStep.ticks(20); step++) {
                sortie.step(SortieTest.Pilot.commands(step));
            }
        }));
    }
}
