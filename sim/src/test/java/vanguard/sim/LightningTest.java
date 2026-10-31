package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1b (design/world/jovian, Hazards & set pieces: lightning strikes): a strike's 40 px column is told
 * for 1.0 s, then strikes for 0.25 s: 10 to the ship (and Rook) once, 60 to every {@code air} enemy in it, the kills
 * paid as a special's; {@code low-air} untouched. The column's x is fixed at the tell's start: off the ship's x to the
 * easy clearance, on the ship's x for a tracking strike, off it near a wave's entry. Rook keeps out of a told column.
 */
class LightningTest {
    private static Sortie sortie(HazardSpec.Strikes strikes, List<GroundUnit> units, List<WaveSpec> waves) {
        return Act6BSpecs.sortie(Act6BSpecs.storm(20, strikes, units, waves), TestSpecs.LOADOUT);
    }

    private static Lightning lightning(Sortie sortie) {
        return sortie.lightning().orElseThrow();
    }

    @Test
    void aToldColumnStrikesAfterItsTellAndHitsTheShipOnce() {
        Sortie sortie =
                sortie(Act6BSpecs.strikes(0, new HazardSpec.Strike(1, Ship.START_X, false)), List.of(), List.of());
        int told = -1;
        int struck = -1;
        int hits = 0;
        for (int i = 1; i <= SimStep.ticks(3); i++) {
            sortie.step(0);
            if (Act6BSpecs.count(sortie, SimEvents.Type.STRIKE_TOLD) > 0) {
                told = sortie.levelTick;
            }
            if (Act6BSpecs.count(sortie, SimEvents.Type.STRIKE) > 0) {
                struck = sortie.levelTick;
            }
            hits += Act6BSpecs.count(sortie, SimEvents.Type.STRIKE_HIT);
        }
        assertEquals(SimStep.ticks(1), told);
        assertEquals(SimStep.ticks(1) + SimStep.ticks(1.0), struck, "after its 1.0 s tell");
        assertEquals(1, hits, "once a strike");
        assertEquals(10, TestSpecs.SHIELD.capacity() - sortie.ship.defences().shield(), 0.5, "10 to the shield first");
        assertEquals(Ship.START_X, lightning(sortie).x(0), 1e-9);
    }

    @Test
    void itKillsTheAirUnitsInItsColumnForTheirPayAndLeavesTheLowAir() {
        EnemySpec small = Act6BSpecs.target("small", Layer.AIR, 50);
        EnemySpec medium = Act6BSpecs.target("medium", Layer.AIR, 100);
        EnemySpec low = Act6BSpecs.target("low", Layer.LOW_AIR, 50);
        EnemySpec aside = Act6BSpecs.target("aside", Layer.AIR, 50);
        Sortie sortie = sortie(
                Act6BSpecs.strikes(0, new HazardSpec.Strike(1, 120, false)),
                List.of(
                        new GroundUnit(0, 120, small, -1),
                        new GroundUnit(0.3, 130, medium, -1),
                        new GroundUnit(0.6, 110, low, -1),
                        new GroundUnit(0.6, 200, aside, -1)),
                List.of());
        int struck = Act6BSpecs.run(sortie, SimStep.ticks(2.2), 0, SimEvents.Type.STRIKE);
        assertEquals(1, struck);
        assertNull(Act6BSpecs.find(sortie, "small"), "60 kills a 50-HP unit");
        assertEquals(40, Act6BSpecs.find(sortie, "medium").hp(), 1e-9, "a medium unit survives");
        assertEquals(50, Act6BSpecs.find(sortie, "low").hp(), 1e-9, "low-air is untouched");
        assertEquals(50, Act6BSpecs.find(sortie, "aside").hp(), 1e-9, "outside the column");
        assertEquals(1, sortie.kills(), "a lightning kill counts");
        assertTrue(sortie.credits() > 0, "and pays its bounty");
    }

    @Test
    void theColumnKeepsTheEasyClearanceAndATrackingOneLocksOnTheShip() {
        Sortie easy = sortie(
                Act6BSpecs.strikes(60, new HazardSpec.Strike(0.5, Ship.START_X + 10, false)), List.of(), List.of());
        Act6BSpecs.run(easy, SimStep.ticks(0.6), 0, SimEvents.Type.STRIKE_TOLD);
        assertEquals(Ship.START_X + 60, lightning(easy).x(0), 1e-9, "moved off the ship's x to the side it lies on");
        Sortie hard = sortie(Act6BSpecs.strikes(0, new HazardSpec.Strike(0.5, 40, true)), List.of(), List.of());
        double shipX = Double.NaN;
        for (int i = 0; i < SimStep.ticks(0.6) && Double.isNaN(shipX); i++) {
            hard.step(Command.LEFT.bit());
            if (Act6BSpecs.count(hard, SimEvents.Type.STRIKE_TOLD) > 0) {
                shipX = hard.ship.x();
            }
        }
        assertTrue(shipX < Ship.START_X - 10, "the ship moved");
        assertEquals(shipX, lightning(hard).x(0), 1e-9, "locked on the ship's x at the tell's start");
    }

    @Test
    void noTellStartsOnTheShipsXNearAWavesEntry() {
        WaveSpec wave = TestSpecs.wave(
                1.5, WaveSpec.Formation.SINGLE, TestSpecs.SKITTER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE);
        Sortie sortie =
                sortie(Act6BSpecs.strikes(0, new HazardSpec.Strike(1, Ship.START_X, false)), List.of(), List.of(wave));
        Act6BSpecs.run(sortie, SimStep.ticks(1.1), 0, SimEvents.Type.STRIKE_TOLD);
        assertTrue(Math.abs(lightning(sortie).x(0) - Ship.START_X) >= 20, "half its width off the ship");
    }

    @Test
    void rookKeepsOutOfAToldColumn() {
        LevelScript level =
                Act6BSpecs.storm(10, Act6BSpecs.strikes(0, new HazardSpec.Strike(0, 200, false)), List.of(), List.of());
        Lightning lightning =
                new Lightning(level.hazards().weather().orElseThrow().strikes().orElseThrow(), level);
        RookHazards hazards = new RookHazards(new Hitbox(30, 30), 20);
        SimEvents events = new SimEvents(16);
        lightning.step(0, Ship.START_X, events);
        assertTrue(lightning.told(0));
        hazards.at(205, 120);
        assertTrue(hazards.clearOfStorm(205, 120, lightning, null));
        assertTrue(Math.abs(hazards.x() - 200) >= 20 + 15 + RookHazards.GAP - 1e-9);
        assertTrue(hazards.x() > 200, "to the nearer side");
        assertEquals(120, hazards.y(), 1e-9);
        // M10 part B, step B3: his slot carried over the column's middle by the ship's drift, he stays on his side
        hazards.at(150, 120);
        assertTrue(hazards.clearOfStorm(205, 120, lightning, null));
        assertTrue(hazards.x() <= 200 - (20 + 15 + RookHazards.GAP) + 1e-9, "never across the told column");
    }
}
