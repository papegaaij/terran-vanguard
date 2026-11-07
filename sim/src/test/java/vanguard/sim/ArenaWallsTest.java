package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part H, step H1a (design/enemies/bosses/choir-heart, Arena: the walls of phase 2; m11h-gaps.md §1 #3): the arena
 * walls grow in the closing to the corridor (per difficulty), breathe no faster than their top speed, never move toward
 * a ship within 40 px, stand as rock (shots, beams, bullets, the ship's contact with its push and cooldown), carry the
 * nodes on their inner faces, and a cut node's half goes back to its edge; opt-in (no walls, no rock), deterministic.
 */
class ArenaWallsTest {
    private static final double EPS = 1e-9;

    private static Sortie arena(ArenaWallsSpec walls) {
        Sortie sortie =
                Act7HSpecs.invulnerable(Act7HSpecs.heart(Act7HSpecs.boss(Optional.of(walls), Optional.empty(), true)));
        Act7HSpecs.intoArena(sortie);
        return sortie;
    }

    @Test
    void theWallsStandOnlyForABossWithThem() {
        Sortie plain = Act7HSpecs.sortie(Act7HSpecs.heart(Act7HSpecs.boss(Optional.empty(), Optional.empty(), false)));
        assertNull(plain.arenaWalls());
        assertNull(plain.fire.canyon(), "no walls, no terrain: no rock at all");
        Sortie walled = Act7HSpecs.sortie(Act7HSpecs.HEART);
        assertNotNull(walled.arenaWalls());
        assertNotNull(walled.fire.canyon());
    }

    @Test
    void beforeTheirPhaseTheWallsLieAtTheEdges() {
        Sortie sortie = Act7HSpecs.sortie(Act7HSpecs.HEART);
        Act7HSpecs.untilEngaged(sortie);
        Act7HSpecs.steps(sortie, 3);
        ArenaWalls walls = sortie.arenaWalls();
        assertFalse(walls.standing());
        for (int h = 0; h < ArenaWalls.HALVES; h++) {
            assertEquals(0, walls.inset(h));
        }
        assertFalse(sortie.fire.canyon().rock(4, 300, 2, 2), "a wall at its edge is no rock");
    }

    @Test
    void theClosingToldThenGrownToTheCorridorPerDifficulty() {
        for (double corridor : new double[] {320, 360, 300}) {
            Sortie sortie = Act7HSpecs.invulnerable(Act7HSpecs.heart(
                    Act7HSpecs.boss(Optional.of(Act7HSpecs.WALLS.withCorridor(corridor)), Optional.empty(), true)));
            Act7HSpecs.untilEngaged(sortie);
            Act7HSpecs.kill(sortie, Act7HSpecs.M1);
            Act7HSpecs.kill(sortie, Act7HSpecs.M2);
            int told = 0;
            for (int i = 0; i < SimStep.ticks(15) && sortie.setPiece(0).phase() != 1; i++) {
                sortie.step(0);
            }
            told += sortie.events().count(SimEvents.Type.WALL_TOLD);
            ArenaWalls walls = sortie.arenaWalls();
            // The phase's first step: every half told, none moved yet.
            sortie.step(0);
            told += sortie.events().count(SimEvents.Type.WALL_TOLD);
            assertEquals(4, told, "every half told as the closing starts");
            assertTrue(walls.told(0));
            Act7HSpecs.steps(sortie, 0.9);
            assertEquals(0, walls.inset(0), "nothing moves in the tell");
            double edge = (PlayField.WIDTH - corridor) / 2;
            double[] most = new double[ArenaWalls.HALVES];
            for (int i = 0; i < SimStep.ticks(3.2); i++) {
                sortie.step(0);
                for (int h = 0; h < ArenaWalls.HALVES; h++) {
                    most[h] = Math.max(most[h], walls.inset(h));
                }
            }
            for (int h = 0; h < ArenaWalls.HALVES; h++) {
                assertEquals(edge, most[h], 1e-6, "grown to the corridor's edge in the closing: " + corridor);
            }
        }
    }

    @Test
    void theBreathKeepsToTheTopSpeedAndItsSwing() {
        Sortie sortie = arena(Act7HSpecs.WALLS);
        ArenaWalls walls = sortie.arenaWalls();
        Act7HSpecs.steps(sortie, 4);
        double edge = 80;
        double most = 20 * SimStep.SECONDS + EPS;
        double[] last = new double[ArenaWalls.HALVES];
        double low = Double.POSITIVE_INFINITY;
        double high = Double.NEGATIVE_INFINITY;
        for (int h = 0; h < ArenaWalls.HALVES; h++) {
            last[h] = walls.inset(h);
        }
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(0);
            for (int h = 0; h < ArenaWalls.HALVES; h++) {
                assertTrue(Math.abs(walls.inset(h) - last[h]) <= most, "never faster than 20 px/s");
                last[h] = walls.inset(h);
            }
            low = Math.min(low, walls.inset(0));
            high = Math.max(high, walls.inset(0));
        }
        assertTrue(low >= edge - 12 - EPS && high <= edge + 12 + EPS, "within ±12 px: " + low + ".." + high);
        assertTrue(high - low > 12, "it breathes: " + low + ".." + high);
    }

    @Test
    void everyInwardMoveIsTold() {
        Sortie sortie = arena(Act7HSpecs.WALLS);
        ArenaWalls walls = sortie.arenaWalls();
        // Into the grow's last 0.6 s: the breath's first tell comes in it, its first inward move after the grow.
        Act7HSpecs.steps(sortie, 3.4);
        int toldTicks = SimStep.ticks(1.0);
        int sinceTell = Integer.MAX_VALUE / 2;
        double last = walls.inset(0);
        int moves = 0;
        for (int i = 0; i < SimStep.ticks(20); i++) {
            sortie.step(0);
            for (int e = 0; e < sortie.events().size(); e++) {
                if (sortie.events().type(e) == SimEvents.Type.WALL_TOLD
                        && sortie.events().value(e) == 0) {
                    sinceTell = 0;
                }
            }
            if (walls.inset(0) > last + EPS && i >= SimStep.ticks(0.6)) {
                moves++;
                assertTrue(sinceTell >= toldTicks - 1, "an inward move after its full tell");
                assertTrue(sinceTell < toldTicks + SimStep.ticks(1.2), "an inward move inside its told half-period");
            }
            last = walls.inset(0);
            sinceTell++;
        }
        assertTrue(moves > 0);
    }

    @Test
    void aHalfNeverMovesTowardAShipWithin40Px() {
        Sortie sortie = Act7HSpecs.invulnerable(Act7HSpecs.HEART);
        Act7HSpecs.untilEngaged(sortie);
        Act7HSpecs.kill(sortie, Act7HSpecs.M1);
        Act7HSpecs.kill(sortie, Act7HSpecs.M2);
        ArenaWalls walls = sortie.arenaWalls();
        Hull hull = sortie.ship().spec().hull();
        double half = 0;
        double below = 0;
        double above = 0;
        for (Hull.Part part : hull.parts()) {
            half = Math.max(half, Math.abs(part.dx()) + part.box().width() / 2);
            below = Math.max(below, part.box().height() / 2 - part.dy());
            above = Math.max(above, part.dy() + part.box().height() / 2);
        }
        double[] last = new double[ArenaWalls.HALVES];
        int held = 0;
        for (int i = 0; i < SimStep.ticks(30); i++) {
            // Hug the left side, then weave: the walls' grow and breath meet the ship.
            int command = i < SimStep.ticks(12) || i % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit();
            sortie.step(command);
            double shipLeft = sortie.ship().x() - half;
            double shipRight = sortie.ship().x() + half;
            for (int h = 0; h < ArenaWalls.HALVES; h++) {
                boolean band = h % 2 == 0
                        ? sortie.ship().y() + above > walls.split() - ArenaWalls.JOIN
                        : sortie.ship().y() - below < walls.split() + ArenaWalls.JOIN;
                if (walls.inset(h) > last[h] + EPS && band) {
                    double gap = h < 2 ? shipLeft - walls.inset(h) : PlayField.WIDTH - walls.inset(h) - shipRight;
                    assertTrue(gap >= 40 - 1e-6, "an inward move leaves 40 px to the ship: " + gap);
                }
                if (h < 2 && band && walls.standing() && walls.inset(h) < 80 - 1 && i < SimStep.ticks(12)) {
                    held++;
                }
                last[h] = walls.inset(h);
            }
        }
        assertTrue(held > 0, "the ship on the left held the left wall short of the corridor");
    }

    @Test
    void theWallsStopShotsBeamsAndBullets() {
        Sortie sortie = arena(Act7HSpecs.WALLS);
        Act7HSpecs.steps(sortie, 4.2);
        Canyon canyon = sortie.fire.canyon();
        double face = sortie.arenaWalls().leftAt(300);
        assertTrue(canyon.rock(face - 5, 300, 2, 6), "inside the left wall");
        assertFalse(canyon.rock(240, 300, 2, 6), "the corridor is open");
        assertTrue(canyon.rock(PlayField.WIDTH - face + 5, 300, 2, 6), "inside the right wall");
        assertEquals(100, canyon.ceiling(face - 10, 2, 100), "a beam in a wall's column ends at once");
        assertEquals(Double.POSITIVE_INFINITY, canyon.ceiling(240, 2, 100), "the corridor's column is clear");
        assertEquals(face, canyon.spanLeft(300, 240), 1e-9);
        assertEquals(PlayField.WIDTH - face, canyon.spanRight(300, 240), 1e-9);
        assertTrue(Double.isNaN(canyon.spanLeft(300, face - 10)));
        assertEquals(face + 6, canyon.flyableX(300, 20, 6), 1e-9, "a pickup or a unit is put out of the wall");
        // An enemy bullet inside a wall stops with a spark.
        EnemyBullet bullet = sortie.force.bullets().obtain();
        bullet.fire(face - 10, 300, 0, -1, 4);
        sortie.step(0);
        for (int i = 0; i < sortie.force.bullets().size(); i++) {
            assertTrue(sortie.force.bullets().get(i).x() > face - 5, "the bullet in the wall is gone");
        }
        assertTrue(sortie.events().count(SimEvents.Type.ROCK_SPARK) > 0);
    }

    @Test
    void theShipTouchesAWallAsACanyonWall() {
        Sortie hurt = Act7HSpecs.sortie(Act7HSpecs.HEART);
        Act7HSpecs.intoArena(hurt);
        Act7HSpecs.steps(hurt, 4.2);
        double before = hurt.ship().defences().shield() + hurt.ship().defences().armour();
        int contacts = 0;
        int first = -1;
        int last = -1;
        double face = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            hurt.step(Command.LEFT.bit());
            face = hurt.arenaWalls().leftAt(hurt.ship().y());
            int n = hurt.events().count(SimEvents.Type.WALL_CONTACT);
            if (n > 0) {
                assertTrue(last < 0 || i - last >= SimStep.ticks(0.5), "a new touch only after the cooldown");
                contacts += n;
                first = first < 0 ? i : first;
                last = i;
            }
        }
        assertTrue(contacts >= 2, "pressing into the wall touches it again after each cooldown: " + contacts);
        assertTrue(hurt.ship().x() > face, "the push keeps the ship out of the wall");
        double after = hurt.ship().defences().shield() + hurt.ship().defences().armour();
        assertTrue(before - after >= 10 - EPS, "each touch deals the contact's damage: " + (before - after));
    }

    @Test
    void theNodesRideTheirHalfWallsInnerFace() {
        Sortie sortie = arena(Act7HSpecs.WALLS);
        SetPiece heart = sortie.setPiece(0);
        ArenaWalls walls = sortie.arenaWalls();
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(0);
            assertEquals(walls.inset(0), heart.partX(Act7HSpecs.N_LU), 1e-9);
            assertEquals(walls.inset(1), heart.partX(Act7HSpecs.N_LL), 1e-9);
            assertEquals(PlayField.WIDTH - walls.inset(2), heart.partX(Act7HSpecs.N_RU), 1e-9);
            assertEquals(PlayField.WIDTH - walls.inset(3), heart.partX(Act7HSpecs.N_RL), 1e-9);
            assertEquals(Act7HSpecs.NODE_UPPER, heart.partY(Act7HSpecs.N_LU), 1e-9);
            assertEquals(Act7HSpecs.NODE_LOWER, heart.partY(Act7HSpecs.N_RL), 1e-9);
        }
    }

    @Test
    void aCutNodesHalfGoesBackToItsEdge() {
        Sortie sortie = arena(Act7HSpecs.WALLS);
        ArenaWalls walls = sortie.arenaWalls();
        Act7HSpecs.steps(sortie, 4.5);
        assertTrue(Act7HSpecs.kill(sortie, Act7HSpecs.N_LL), "the node is open after the closing");
        sortie.step(0);
        boolean told = false;
        for (int e = 0; e < sortie.events().size(); e++) {
            told |= sortie.events().type(e) == SimEvents.Type.WALL_TOLD
                    && sortie.events().value(e) == 1;
        }
        assertTrue(told, "the cut half's veins tell its retreat");
        assertTrue(walls.retreating(1));
        double standing = walls.inset(1);
        Act7HSpecs.steps(sortie, 0.9);
        assertEquals(standing, walls.inset(1), 1e-9, "it waits out its tell");
        double last = walls.inset(1);
        for (int i = 0; i < SimStep.ticks(5); i++) {
            sortie.step(0);
            assertTrue(last - walls.inset(1) <= 20 * SimStep.SECONDS + EPS, "back at the top speed");
            last = walls.inset(1);
        }
        assertEquals(0, walls.inset(1), "back at its edge");
        assertFalse(walls.retreating(0));
        assertTrue(walls.inset(0) > 60, "the other halves stand");
        assertEquals(walls.inset(1), walls.leftAt(Act7HSpecs.NODE_LOWER), 1e-9);
    }

    @Test
    void afterThePhaseEveryHalfGoesBack() {
        Sortie sortie = arena(Act7HSpecs.WALLS);
        Act7HSpecs.steps(sortie, 4.5);
        for (int p : Act7HSpecs.NODES) {
            assertTrue(Act7HSpecs.kill(sortie, p));
        }
        Act7HSpecs.steps(sortie, 6);
        assertEquals(2, sortie.setPiece(0).phase());
        for (int h = 0; h < ArenaWalls.HALVES; h++) {
            assertEquals(0, sortie.arenaWalls().inset(h));
        }
        assertFalse(sortie.fire.canyon().rock(10, 300, 4, 4));
    }

    @Test
    void theWallsReplay() {
        Sortie one = Act7HSpecs.sortie(Act7HSpecs.HEART);
        Sortie two = Act7HSpecs.sortie(Act7HSpecs.HEART);
        for (Sortie sortie : new Sortie[] {one, two}) {
            Act7HSpecs.intoArena(sortie);
            for (int i = 0; i < SimStep.ticks(20); i++) {
                sortie.step(i % 100 < 50 ? Command.LEFT.bit() | Command.FIRE.bit() : Command.RIGHT.bit());
            }
        }
        assertEquals(one.stateHash(), two.stateHash());
    }
}
