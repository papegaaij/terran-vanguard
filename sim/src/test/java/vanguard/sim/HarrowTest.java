package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * M10 part D, step D1 (design/enemies/air/harrow; gap check m10d §1 #1): the Harrow's chase and ram, a variant of the
 * Buzzsaw's dash cycle. Its chase lasts a seeded 1.5–2.5 s; its nose swings onto the ship through the 0.6 s lock tell and
 * the ram's bearing is the one at the tell's end; one straight ram at 300 px/s with no bounce, a miss gone through the far
 * edge with no ring; a ram on the ship pays once and bursts its ring; the EMP stun holds its tell; a ram meets no chunk.
 */
class HarrowTest {
    private static Enemy harrow(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().dash().isPresent()) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    /** One Harrow down from the top (a stream of one from the left at t=1). */
    private static LevelScript single() {
        return Act6DSpecs.level(30, List.of(Act6DSpecs.stream(1, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.LEFT)));
    }

    /** Steps with {@code commands} until a {@code type} event; returns the step count, -1 for none in 15 s. */
    private static int until(Sortie sortie, SimEvents.Type type, int commands) {
        for (int i = 0; i < SimStep.ticks(15); i++) {
            sortie.step(commands);
            if (sortie.events().count(type) > 0) {
                return i;
            }
        }
        return -1;
    }

    /** Each unit's chase steps (by serial) in a stream of 4 from the top, flown untouched. */
    private static Map<Integer, Integer> chases() {
        Sortie sortie = Act6DSpecs.untouchable(Act6DSpecs.level(
                30, List.of(Act6DSpecs.stream(1, 4, WaveSpec.Entry.FRONT, WaveSpec.Edge.ALTERNATING))));
        Map<Integer, Integer> chase = new HashMap<>();
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            for (int j = 0; j < sortie.enemyCount(); j++) {
                Enemy u = sortie.enemy(j);
                if (u.phase == Enemy.Phase.DASH && u.dashing.stage() == Dashing.Stage.CHASE) {
                    chase.merge(u.serial(), 1, Integer::sum);
                }
            }
        }
        assertEquals(0, sortie.enemyCount(), "every ram flown out");
        return chase;
    }

    @Test
    void itsChaseIsASeededTimeFromItsRangeThatReplays() {
        Map<Integer, Integer> one = chases();
        assertEquals(4, one.size());
        for (int steps : one.values()) {
            assertTrue(steps >= SimStep.ticks(1.5) && steps <= SimStep.ticks(2.5), "1.5–2.5 s: " + steps);
        }
        assertTrue(one.values().stream().distinct().count() > 1, "drawn per unit: " + one);
        assertEquals(one, chases(), "seeded: it replays");
    }

    @Test
    void theRamsBearingIsLockedAtTheTellsEndAndItsFullTellComesFirst() {
        Sortie sortie = Act6DSpecs.untouchable(single());
        int told = -1;
        int rammed = -1;
        double startX = Double.NaN;
        double startY = Double.NaN;
        for (int i = 0; i < SimStep.ticks(15) && rammed < 0; i++) {
            // The ship moves through the tell: the ram keeps the bearing at its end.
            sortie.step((i / SimStep.ticks(0.2)) % 2 == 0 ? Command.LEFT.bit() : Command.UP.bit());
            Enemy u = harrow(sortie);
            if (sortie.events().count(SimEvents.Type.DASH_TELL) > 0) {
                told = i;
                double dx = sortie.ship().x() - u.x();
                double dy = sortie.ship().y() - u.y();
                startX = dx / Math.hypot(dx, dy);
                startY = dy / Math.hypot(dx, dy);
            }
            if (sortie.events().count(SimEvents.Type.DASH_START) > 0) {
                rammed = i;
                double dx = sortie.ship().x() - u.x();
                double dy = sortie.ship().y() - u.y();
                double length = Math.hypot(dx, dy);
                assertEquals(dx / length, u.dashing.dirX(), 1e-9, "locked at the tell's end");
                assertEquals(dy / length, u.dashing.dirY(), 1e-9);
                assertTrue(
                        Math.abs(startX - u.dashing.dirX()) + Math.abs(startY - u.dashing.dirY()) > 1e-3,
                        "not the bearing at the tell's start");
            }
        }
        assertEquals(SimStep.ticks(0.6), rammed - told, "the full 0.6 s tell");
        Enemy u = harrow(sortie);
        double x = u.x();
        double y = u.y();
        sortie.step(Command.NONE);
        assertEquals(300 * SimStep.SECONDS, Math.hypot(u.x() - x, u.y() - y), 1e-9, "a straight ram at 300 px/s");
    }

    @Test
    void aSidewaysDodgeThroughTheRamMissesAndTheMissFliesOutThroughTheFarEdgeWithNoRing() {
        Sortie sortie = Act6DSpecs.sortie(single());
        assertTrue(until(sortie, SimEvents.Type.DASH_START, Command.NONE) >= 0);
        double shield = sortie.ship().defences().shield();
        int away = harrow(sortie).x() < sortie.ship().x() ? Command.RIGHT.bit() : Command.LEFT.bit();
        int bounces = 0;
        for (int i = 0; i < SimStep.ticks(4) && sortie.enemyCount() > 0; i++) {
            sortie.step(away);
            bounces += sortie.events().count(SimEvents.Type.DASH_BOUNCE);
        }
        assertEquals(0, sortie.enemyCount(), "gone through the far edge");
        assertEquals(0, bounces, "no bounce");
        assertEquals(0, sortie.kills(), "a miss is no kill");
        assertEquals(0, sortie.bulletCount(), "no ring off the screen");
        assertEquals(shield, sortie.ship().defences().shield(), 1e-9, "missed");
    }

    @Test
    void aRamOnTheShipPaysOnceAndBurstsItsRing() {
        Sortie sortie = Act6DSpecs.sortie(single());
        assertTrue(until(sortie, SimEvents.Type.DASH_START, Command.NONE) >= 0);
        int destroyed = 0;
        for (int i = 0; i < SimStep.ticks(3) && sortie.enemyCount() > 0; i++) {
            sortie.step(Command.NONE);
            destroyed += sortie.events().count(SimEvents.Type.ENEMY_DESTROYED);
        }
        assertEquals(0, sortie.enemyCount());
        assertEquals(1, destroyed, "destroyed by its ram");
        assertEquals(1, sortie.kills(), "a ram pays as a kill");
        assertEquals(8, sortie.bulletCount(), "its ring");
        assertTrue(sortie.ship().defences().shield() < TestSpecs.SHIELD.capacity(), "its contact damage");
    }

    @Test
    void theEmpStunHoldsItsTellAndNoRamStartsWhileItLasts() {
        Sortie sortie = Act6DSpecs.untouchable(single());
        assertTrue(until(sortie, SimEvents.Type.DASH_TELL, Command.NONE) >= 0);
        Enemy u = harrow(sortie);
        sortie.step(Command.NONE);
        u.empStun(SimStep.ticks(3));
        double x = u.x();
        double y = u.y();
        for (int i = 0; i < SimStep.ticks(3) - 1; i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.DASH_START), "no ram while stunned");
            assertEquals(Dashing.Stage.TELL, u.dashing.stage());
        }
        assertTrue(Math.hypot(u.x() - x, u.y() - y) > 0, "it drifts on along its line");
        assertTrue(until(sortie, SimEvents.Type.DASH_START, Command.NONE) <= SimStep.ticks(0.6), "its tell resumes");
    }

    @Test
    void aRamMeetsNoChunk() {
        Sortie sortie = Act6DSpecs.untouchable(single());
        assertTrue(until(sortie, SimEvents.Type.DASH_START, Command.NONE) >= 0);
        Enemy u = harrow(sortie);
        HazardSpec.DebrisSpec large = new HazardSpec.DebrisSpec(
                0, 0, "large-a", new Hitbox(60, 60), 0, -0.01, Double.POSITIVE_INFINITY, 10, 0);
        HazardSpec.DebrisSpec small =
                new HazardSpec.DebrisSpec(0, 0, "small-a", new Hitbox(28, 28), 0, -0.01, 12, 0, 0);
        List<Debris> chunks = new ArrayList<>();
        for (HazardSpec.DebrisSpec spec : List.of(small, large)) {
            Debris chunk = sortie.debris.obtain();
            double ahead = spec == small ? 30 : 90;
            chunk.enter(
                    spec,
                    70 + chunks.size(),
                    u.x() + u.dashing.dirX() * ahead,
                    u.y() + u.dashing.dirY() * ahead,
                    0,
                    -0.01);
            chunks.add(chunk);
        }
        int smashed = 0;
        int bounced = 0;
        for (int i = 0; i < SimStep.ticks(0.6); i++) {
            sortie.step(Command.NONE);
            smashed += sortie.events().count(SimEvents.Type.DASH_SMASH);
            bounced += sortie.events().count(SimEvents.Type.DASH_BOUNCE);
        }
        assertEquals(0, smashed, "the small chunk is not smashed");
        assertEquals(0, bounced, "nor does the large one bounce it");
        assertEquals(2, sortie.debris.size());
    }

    @Test
    void aRamHasNoBounceAndOneDashAndItsChaseRangeStartsAtItsChase() {
        EnemySpec.Dash.Variant ram = EnemySpec.Dash.Variant.ram(2.5);
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Dash(1.5, 1.5, 0.6, 50, 300, 1, 0, 1, ram));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Dash(1.5, 1.5, 0.6, 50, 300, 0, 0, 2, ram));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Dash(1.5, 3, 0.6, 50, 300, 0, 0, 1, ram));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Dash(1.5, 1.5, 0.4, 50, 300, 0, 0, 1, ram));
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5DSpecs.DASH.withVariant(
                        new EnemySpec.Dash.Variant(0, EnemySpec.Dash.Lock.TELL_START, EnemySpec.Dash.After.OUT)));
        assertTrue(Act6DSpecs.RAM.chaseRanged());
        assertEquals(EnemySpec.Dash.Variant.BUZZSAW, Act5DSpecs.DASH.variant(), "the Buzzsaw's as before");
        assertFalse(Act5DSpecs.DASH.chaseRanged());
        assertEquals(
                Act5DSpecs.DASH,
                Act5DSpecs.DASH.withVariant(EnemySpec.Dash.Variant.BUZZSAW),
                "the eight-number cycle is the Buzzsaw's");
    }
}
