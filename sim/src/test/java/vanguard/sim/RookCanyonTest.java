package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part C (design/campaign Level 16, Rook in the canyon): Rook keeps 24 px off rock, trails where the
 * canyon is under 240 px wide or split and is back beside the player where it is wide, follows the
 * player's channel past an island, his shots stop at rock like the player's, and touching rock costs him
 * the contact's damage on his armour.
 */
class RookCanyonTest {
    private static final int FIRE = Command.FIRE.bit();
    private static final double HALF = WingmanTest.CRAFT.hitbox().width() / 2;

    private static Terrain straight(double left, double right) {
        return new Terrain(
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {left, left})),
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {right, right})),
                List.of());
    }

    static WingmanSpec rook() {
        return WingmanTest.rook(WingmanSpec.Side.RIGHT);
    }

    private static Sortie sortie(Terrain terrain) {
        LevelScript level = TestSpecs.withGround(
                TestSpecs.level(30, List.of()),
                g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(terrain, 10, 28, 0.12, 0.5))));
        return WingmanTest.sortie(level, WingmanSpec.Side.RIGHT);
    }

    @Test
    void inANarrowCanyonHeTrailsAndKeeps24PxOffTheWalls() {
        Sortie sortie = sortie(straight(140, 340));
        for (int step = 0; step < SimStep.ticks(3); step++) {
            sortie.step(Command.NONE);
            Wingman rook = sortie.wingman().orElseThrow();
            assertTrue(rook.x() - HALF >= 140 + Wingman.ROCK_CLEARANCE - 1e-9, "off the left wall: " + rook.x());
            assertTrue(rook.x() + HALF <= 340 - Wingman.ROCK_CLEARANCE + 1e-9, "off the right wall: " + rook.x());
        }
        assertSame(Wingman.Formation.TRAIL, sortie.wingman().orElseThrow().formation());
        assertEquals(0, sortie.events().count(SimEvents.Type.WALL_CONTACT));
    }

    @Test
    void inAWideCanyonHeStaysBesideThePlayer() {
        Sortie sortie = sortie(straight(40, 440));
        for (int step = 0; step < SimStep.ticks(3); step++) {
            sortie.step(Command.NONE);
        }
        assertSame(Wingman.Formation.WING, sortie.wingman().orElseThrow().formation());
    }

    @Test
    void pastAnIslandHeFollowsThePlayersChannel() {
        // The fork island's tip reaches the top edge after 2 s (scroll 260) and the ship's row 3.4 s later.
        double tip = 260;
        Terrain fork = new Terrain(
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {40, 40})),
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {440, 440})),
                List.of(new Terrain.Island(
                        new double[] {240, 190, 190, 240, 290, 290},
                        new double[] {tip, tip + 540, tip + 1_980, tip + 2_520, tip + 1_980, tip + 540})));
        Sortie sortie = sortie(fork);
        // Into the left channel (40–190) first.
        while (sortie.ship().x() > 115) {
            sortie.step(Command.LEFT.bit());
        }
        boolean beside = false;
        for (int step = 0; step < SimStep.ticks(16); step++) {
            sortie.step(Command.NONE);
            Wingman rook = sortie.wingman().orElseThrow();
            assertTrue(!fork.rock(sortie.groundScroll(), rook.x(), rook.y(), HALF, HALF), "never in rock: " + rook.x());
            if (fork.channels(sortie.groundScroll(), rook.y()) > 1) {
                beside = true;
                assertTrue(rook.x() < 190, "in the player's (left) channel: " + rook.x());
            }
        }
        assertTrue(beside, "the island passed him");
        assertEquals(0, sortie.events().count(SimEvents.Type.WALL_CONTACT));
    }

    @Test
    void hisShotsStopAtRock() {
        // A block over the whole width above the field's middle.
        double foot = 400 + 65 - PlayField.HEIGHT;
        Terrain roof = new Terrain(
                Optional.empty(),
                Optional.empty(),
                List.of(new Terrain.Island(
                        new double[] {0, 480, 480, 0}, new double[] {foot, foot, foot + 3_000, foot + 3_000})));
        Sortie sortie = sortie(roof);
        int sparks = 0;
        int mount = TestSpecs.LOADOUT.armament().size();
        for (int step = 0; step < 60; step++) {
            sortie.step(FIRE);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.type(i) == SimEvents.Type.ROCK_SPARK && events.value(i) == mount) {
                    sparks++;
                }
            }
        }
        assertTrue(sparks > 0, "Rook's shots spark on the rock");
    }

    @Test
    void touchingRockCostsHimTheContactsDamageOnHisArmour() {
        // The right wall steps in from 470 to 290 over 10 rows: it comes down onto his slot (304, 68) from
        // above faster than his clearance can keep him off it; the player at 240 is never touched.
        Terrain step = new Terrain(
                Optional.empty(),
                Optional.of(new Terrain.Wall(
                        new double[] {-1_000, -260, -250, 100_000}, new double[] {470, 470, 290, 290})),
                List.of());
        Sortie sortie = sortie(step);
        double armour = sortie.wingman().orElseThrow().armour();
        int touches = 0;
        for (int i = 0; i < SimStep.ticks(3); i++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.WALL_CONTACT) {
                    assertEquals(2, events.value(e), "Rook's, not the player's");
                    touches++;
                }
            }
        }
        assertTrue(touches > 0, "he touched it");
        assertEquals(armour - 10 * touches, sortie.wingman().orElseThrow().armour(), 1e-9);
        Wingman rook = sortie.wingman().orElseThrow();
        assertTrue(!step.rock(sortie.groundScroll(), rook.x(), rook.y(), HALF, HALF), "out of it again");
    }
}
