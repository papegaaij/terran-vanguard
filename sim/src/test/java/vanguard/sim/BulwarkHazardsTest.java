package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M8 part G, step G1 (design/player/wingmen, Heavy drone "Bulwark"; design/campaign Level 27, Escort; m8g-gaps.md §1.3,
 * §5 G7): the Bulwark's goal keeps out of a heat plume's disc while it tells or burns and off a torpedo's line over
 * its next second; while a Depth Hunter is on the screen its shell aims at the nearest one, else straight ahead.
 */
class BulwarkHazardsTest {
    private static final Hitbox BOX = new Hitbox(14, 14);
    private static final double LIMIT = 22;

    private static FurnaceVents telling() {
        FurnaceVents vents = new FurnaceVents(List.of(HeatPlumesTest.plumes(0, 240)), Integer.MAX_VALUE);
        SimEvents events = new SimEvents(16);
        double scroll = 0;
        for (int tick = 0; tick <= SimStep.ticks(2.5); tick++) {
            scroll += 110 * SimStep.SECONDS;
            vents.update(tick, scroll, true, events);
        }
        return vents;
    }

    @Test
    void itsGoalLeavesAHotPlumesDisc() {
        FurnaceVents vents = telling();
        assertEquals(FurnaceVents.Phase.BURN, vents.phase(0));
        BulwarkHazards hazards = new BulwarkHazards(BOX, LIMIT);
        double cx = vents.x(0);
        double cy = vents.y(0);
        assertTrue(hazards.clear(cx + 10, cy, vents, null));
        double reach = 32 + 7 + BulwarkHazards.GAP;
        assertEquals(reach, Math.hypot(hazards.x() - cx, hazards.y() - cy), 1e-9, "out to the rim, its half and a gap");
        assertTrue(hazards.x() > cx, "straight away from the centre");
        assertFalse(hazards.clear(cx + 60, cy, vents, null), "already clear");
        assertEquals(cx + 60, hazards.x());
    }

    @Test
    void itsGoalLeavesATorpedosLine() {
        Missiles missiles = new Missiles(new SimEvents(16));
        // A torpedo at (200, 100) heading straight up (90 px/s): its line runs up to y 190.
        missiles.launchAlong(200, 100, DepthHunterTest.TORPEDO, Math.PI);
        BulwarkHazards hazards = new BulwarkHazards(BOX, LIMIT);
        assertTrue(hazards.clear(205, 150, null, missiles));
        assertEquals(200 + BulwarkHazards.LINE + 7, hazards.x(), 1e-9, "across to its side");
        assertEquals(150, hazards.y(), 1e-9);
        assertFalse(hazards.clear(205, 300, null, missiles), "beyond its next second");
        assertFalse(hazards.clear(240, 150, null, missiles), "off the line already");
    }

    @Test
    void itsShellAimsAtTheNearestHunterWhileOneIsOnTheScreen() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.loadout().withBulwark(BulwarkTest.BULWARK),
                TestSpecs.level(40, List.of(DepthHunterTest.column(3, 1))),
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        int mount = sortie.wingmanMount();
        // Before the Hunter: straight ahead.
        boolean straight = false;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.FIRE.bit());
            for (int k = 0; k < sortie.shotCount(); k++) {
                if (sortie.shot(k).mount() == mount) {
                    assertEquals(0, sortie.shot(k).heading(), 1e-12);
                    straight = true;
                }
            }
        }
        assertTrue(straight);
        boolean aimed = false;
        for (int i = 0; i < SimStep.ticks(8) && !aimed; i++) {
            sortie.step(Command.FIRE.bit());
            Enemy hunter = AbyssRayTest.find(sortie, "depth-hunter");
            Bulwark bulwark = sortie.bulwark().orElseThrow();
            if (hunter == null
                    || !PlayField.overlaps(hunter.x(), hunter.y(), hunter.hitbox())
                    || bulwark.ticksSinceShot() != 0) {
                continue;
            }
            for (int k = 0; k < sortie.shotCount(); k++) {
                Shot shot = sortie.shot(k);
                if (shot.mount() == mount && shot.age() <= 1) {
                    double toward = Math.atan2(hunter.x() - bulwark.x(), hunter.y() - bulwark.y());
                    assertEquals(toward, shot.heading(), 0.25, "at the hunter");
                    aimed = true;
                }
            }
        }
        assertTrue(aimed, "a shell aimed at the Hunter");
    }
}
