package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M7 part C (design/campaign Level 16, Wall contact; M1 = a): the ship touching rock takes the contact's
 * damage shield first, is pushed back 28 px over 0.12 s and clamped, and for 0.5 s further contact only
 * clamps; the first touch cues {@code first-wall-contact}; a dash clamps without damage (default pending
 * the user's review); a level without terrain is unchanged.
 */
class WallContactTest {
    private static final int LEFT = Command.LEFT.bit();
    private static final int DASH_LEFT = Command.of(Command.LEFT, Command.DASH);
    private static final double DAMAGE = 10;

    /** A straight canyon 180 px wide: walls at 150 and 330 over the whole level. */
    static final Terrain CANYON = new Terrain(
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {150, 150})),
            Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {330, 330})),
            List.of());

    static LevelScript canyon(Terrain terrain) {
        RadioCue cue =
                new RadioCue(CueTrigger.FIRST_WALL_CONTACT, 0, "", "Rook", "Careful, the walls!", false, "neutral");
        return TestSpecs.withGround(
                TestSpecs.level(30, List.of(), List.of(), List.of(cue)),
                g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(terrain, DAMAGE, 28, 0.12, 0.5))));
    }

    private static double health(Sortie sortie) {
        return sortie.ship().defences().shield() + sortie.ship().defences().armour();
    }

    /** The deepest overlap of the ship's hull with rock now, px (0 when clear). */
    private static double depth(Sortie sortie) {
        double[] out = new double[3];
        Terrain terrain = sortie.terrain();
        return terrain.contact(
                        sortie.groundScroll(),
                        TestSpecs.HULL,
                        sortie.ship().x(),
                        sortie.ship().y(),
                        out)
                ? out[2]
                : 0;
    }

    @Test
    void aTouchDealsItsDamageShieldFirstAndPushesTheShipBack() {
        Sortie sortie = TestSpecs.sortie(canyon(CANYON));
        double before = health(sortie);
        int steps = 0;
        while (sortie.events().count(SimEvents.Type.WALL_CONTACT) == 0 && steps++ < 300) {
            sortie.step(LEFT);
        }
        assertTrue(steps < 300, "the ship reaches the left wall");
        assertEquals(-1, valueOf(sortie, SimEvents.Type.WALL_CONTACT), "the rock on its left");
        assertEquals(DAMAGE, before - health(sortie), 1e-6);
        assertEquals(TestSpecs.FULL_ARMOUR, sortie.ship().defences().armour(), 1e-9, "the shield took it");
        assertTrue(depth(sortie) < 1e-6, "clamped out of the rock");
        double touchedAt = sortie.ship().x();
        for (int i = 0; i < 12; i++) {
            sortie.step(Command.NONE);
        }
        assertTrue(
                sortie.ship().x() >= touchedAt + 26,
                "pushed back about 28 px: " + sortie.ship().x());
        assertEquals(DAMAGE, before - health(sortie), 1e-6, "no second touch");
    }

    @Test
    void holdingIntoTheWallCostsItsDamageEveryHalfSecondAndNeverEntersTheRock() {
        Sortie sortie = TestSpecs.sortie(canyon(CANYON));
        int touches = 0;
        int lastTouch = -1;
        double before = health(sortie);
        for (int step = 0; step < 240; step++) {
            double health = health(sortie);
            sortie.step(LEFT);
            double lost = health - health(sortie);
            assertTrue(depth(sortie) < 1e-6, "never inside the rock after a step");
            if (sortie.events().count(SimEvents.Type.WALL_CONTACT) > 0) {
                assertEquals(DAMAGE, lost, 1e-3, "a touch deals its damage");
                if (lastTouch >= 0) {
                    assertEquals(SimStep.ticks(0.5), step - lastTouch, "one touch per cooldown");
                }
                lastTouch = step;
                touches++;
            } else {
                assertTrue(lost <= 1e-9, "the cooldown's contacts only clamp");
            }
        }
        assertTrue(touches >= 6, "about 2 touches a second: " + touches);
        assertTrue(before - health(sortie) >= touches * DAMAGE - 1, "10 each");
        assertTrue(sortie.ship().x() > 150, "on the flyable side");
    }

    @Test
    void theFirstTouchCuesTheRadioOnce() {
        Sortie sortie = TestSpecs.sortie(canyon(CANYON));
        int cues = 0;
        for (int step = 0; step < 240; step++) {
            sortie.step(LEFT);
            cues += sortie.events().count(SimEvents.Type.RADIO);
        }
        assertEquals(1, cues, "first-wall-contact");
    }

    @Test
    void aDashIntoTheWallIsClampedAndPushedWithoutDamage() {
        Sortie sortie = new Sortie(
                1,
                TestSpecs.LOADOUT.withThrusters(DashTest.DASH),
                canyon(CANYON),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        // Close to the wall first, then dash into it.
        while (sortie.ship().x() > 190) {
            sortie.step(LEFT);
        }
        double before = health(sortie);
        sortie.step(DASH_LEFT);
        int contacts = 0;
        for (int i = 0; i < 10; i++) {
            contacts += sortie.events().count(SimEvents.Type.WALL_CONTACT);
            assertTrue(depth(sortie) < 1e-6);
            sortie.step(Command.NONE);
        }
        assertTrue(contacts > 0, "it touched");
        assertEquals(before, health(sortie), 1e-9, "contacts and hazards do not hurt it in a dash");
    }

    @Test
    void aWallAtTheFieldsEdgeIsNotTouchedFromThePlayField() {
        Terrain mouth = new Terrain(
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {0, 0})),
                Optional.of(new Terrain.Wall(new double[] {-1_000, 100_000}, new double[] {480, 480})),
                List.of());
        Sortie sortie = TestSpecs.sortie(canyon(mouth));
        for (int step = 0; step < 120; step++) {
            sortie.step(LEFT);
        }
        assertEquals(0, sortie.events().count(SimEvents.Type.WALL_CONTACT));
        assertEquals(TestSpecs.SHIP.edgeLimit(), sortie.ship().x(), 1e-9, "at the field's edge");
    }

    @Test
    void aLevelWithoutTerrainHasNoneAndTerrainOnlyChangesTheHashWhereItIs() {
        LevelScript plain = TestSpecs.level(30, List.of());
        Sortie without = TestSpecs.sortie(plain);
        Sortie emptied = TestSpecs.sortie(TestSpecs.withGround(plain, g -> g.withTerrain(Optional.empty())));
        assertNull(without.terrain());
        Sortie with = TestSpecs.sortie(canyon(CANYON));
        for (int step = 0; step < 60; step++) {
            without.step(Command.NONE);
            emptied.step(Command.NONE);
            with.step(Command.NONE);
        }
        assertEquals(without.stateHash(), emptied.stateHash());
        assertNotEquals(without.stateHash(), with.stateHash(), "its contact state is hashed");
    }

    @Test
    void scrapingAlongTheWallsWhileFiringDoesNotAllocate() {
        WaveSpec vee = TestSpecs.wave(
                0.5, WaveSpec.Formation.V_WING, TestSpecs.NEEDLER, 5, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        LevelScript level = TestSpecs.withGround(
                TestSpecs.level(30, List.of(vee)),
                g -> g.withTerrain(Optional.of(new GroundSpec.TerrainSpec(CANYON, DAMAGE, 28, 0.12, 0.5))));
        long allocated = Allocations.least(
                () -> new Sortie(
                        1,
                        TestSpecs.LOADOUT.withWingman(RookCanyonTest.rook()),
                        level,
                        TestSpecs.RULES,
                        TestSpecs.FULL_ARMOUR),
                sortie -> {
                    for (int i = 0; i < 600; i++) {
                        int stick = i % 120 < 60 ? LEFT : Command.RIGHT.bit();
                        sortie.step(stick | Command.FIRE.bit());
                    }
                });
        assertEquals(0, allocated);
    }

    private static int valueOf(Sortie sortie, SimEvents.Type type) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                return events.value(i);
            }
        }
        throw new AssertionError("no " + type);
    }
}
