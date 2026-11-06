package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part C, step CD1 (design/world/vrell-space, living terrain; design/campaign Level 45, Pit swarms; m11c-gaps.md §1
 * #3, §4): a pit wave rises out of its spawning pit: the pit tells 2 s, opens at the wave's time, the units rise 0.5 s
 * untouchable and harmless, and it closes 3 s after it opened; a pit with the ship within 120 px holds closed and the
 * whole wave comes late; a pit off the bottom edge still held loses its wave; a level with pits never reverses.
 */
class PitTest {
    /** A slow plain flyer, so it stays near its pit. */
    private static final EnemySpec DRONE = EnemySpec.of("drone", 10, new Hitbox(24, 24), Layer.AIR, 10, true, 5, 40);

    private static VrellLevel pits(double x, double at) {
        return VrellLevel.NONE.withPits(
                Optional.of(new VrellLevel.PitSpec(2.0, 0.5, 3.0, 120, List.of(new VrellLevel.Pit(x, at)))));
    }

    private static WaveSpec pitWave(double t, EnemySpec unit, int count) {
        WaveSpec.Formation formation = count == 1 ? WaveSpec.Formation.SINGLE : WaveSpec.Formation.LINE_ABREAST;
        return WaveSpec.builder(t, formation, unit, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .pit(0)
                .build();
    }

    /** The ground at 30 px/s; the pit at {@code x}, at the top edge when the ground has scrolled {@code at} px. */
    private static Sortie sortie(double x, double at, WaveSpec wave) {
        return Act7CDSpecs.sortie(Act7CDSpecs.level(40, 30, List.of(wave), List.of(), pits(x, at)));
    }

    @Test
    void thePitTellsOpensLetsItsWaveRiseHarmlessAndClosesAfterIt() {
        // The wave at 20 s: the pit (x 100) is at the top edge at 4 s and 156 px up the field at 20 s.
        Sortie sortie = sortie(100, 120, pitWave(20, DRONE, 1));
        int tell = -1;
        int open = -1;
        int close = -1;
        int risen = -1;
        Enemy drone = null;
        for (int tick = 0; tick < SimStep.ticks(26); tick++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            if (events.count(SimEvents.Type.PIT_TELL) > 0) {
                tell = tick;
                assertEquals(Pits.State.TELL, sortie.pits().state(0));
            }
            if (events.count(SimEvents.Type.PIT_OPEN) > 0) {
                open = tick;
                drone = Act7CDSpecs.find(sortie, "drone");
                assertNotNull(drone, "its wave rose with the opening");
                assertEquals(sortie.pits().x(0), drone.x(), 1, "out of the pit's mouth");
                assertEquals(sortie.pits().renderY(0, 1), drone.y(), 2);
                assertTrue(sortie.pits().rising(drone));
                assertFalse(ShotGeometry.onField(drone), "untouchable while it rises");
                assertFalse(drone.bodyTouches(), "harmless while it rises");
                assertEquals(0, sortie.pits().rise(drone, 0), 0.05, "from the ground's scale");
            }
            if (drone != null && risen < 0 && !sortie.pits().rising(drone)) {
                risen = tick;
                assertTrue(ShotGeometry.onField(drone), "risen: shots reach it");
                assertTrue(drone.bodyTouches());
            }
            if (events.count(SimEvents.Type.PIT_CLOSE) > 0) {
                close = tick;
            }
            if (open < 0 && tick < SimStep.ticks(20) - 1) {
                assertNull(Act7CDSpecs.find(sortie, "drone"), "held out of the main plan");
            }
        }
        assertEquals(SimStep.ticks(18), tell, 1, "its 2 s tell");
        assertEquals(SimStep.ticks(20), open, 1, "open at the wave's time");
        assertEquals(open + SimStep.ticks(0.5), risen, 1, "a 0.5 s rise");
        assertEquals(open + SimStep.ticks(3), close, "closed 3 s after it opened");
        assertEquals(Pits.State.CLOSED, sortie.pits().state(0));
    }

    @Test
    void aPitWithTheShipNearHoldsClosedAndTheWholeWaveComesLate() {
        // The pit at the ship's height (96 px) at 20 s, at x 100: the ship sits under it, then leaves at 21 s.
        Sortie sortie = sortie(100, 156, pitWave(20, DRONE, 3));
        int held = -1;
        int open = -1;
        for (int tick = 0; tick < SimStep.ticks(26); tick++) {
            Ship ship = sortie.ship();
            int command = tick < SimStep.ticks(21) ? (ship.x() > 102 ? Command.LEFT.bit() : 0) : Command.RIGHT.bit();
            sortie.step(command);
            SimEvents events = sortie.events();
            if (events.count(SimEvents.Type.PIT_HELD) > 0) {
                assertEquals(-1, held, "told once");
                held = tick;
                assertEquals(Pits.State.HELD, sortie.pits().state(0));
            }
            if (events.count(SimEvents.Type.PIT_OPEN) > 0) {
                open = tick;
                double dx = sortie.ship().x() - sortie.pits().x(0);
                double dy = sortie.ship().y() - sortie.pits().renderY(0, 1);
                assertTrue(dx * dx + dy * dy >= 120 * 120, "opens only with the ship 120 px off");
                assertEquals(3, Act7CDSpecs.count(sortie, "drone"), "the whole wave together");
            }
            if (open < 0) {
                assertEquals(0, Act7CDSpecs.count(sortie, "drone"));
            }
        }
        assertEquals(SimStep.ticks(20), held, 1);
        assertTrue(open > SimStep.ticks(21), "late: " + open);
    }

    @Test
    void aPitHeldOffTheBottomEdgeLosesItsWave() {
        Sortie sortie = sortie(Ship.START_X, 156, pitWave(20, DRONE, 2));
        int before = sortie.enemyTotal();
        boolean closed = false;
        for (int tick = 0; tick < SimStep.ticks(30); tick++) {
            // The ship stays under the pit until it scrolls off the bottom.
            Ship ship = sortie.ship();
            double pitY = sortie.pits().renderY(0, 1);
            int command = ship.y() > pitY + 3 && ship.y() > 20 ? Command.DOWN.bit() : 0;
            sortie.step(command);
            closed |= sortie.events().count(SimEvents.Type.PIT_CLOSE) > 0;
            assertEquals(0, sortie.events().count(SimEvents.Type.PIT_OPEN));
        }
        assertTrue(closed);
        assertEquals(0, Act7CDSpecs.count(sortie, "drone"));
        assertEquals(before - 2, sortie.enemyTotal(), "its units counted nowhere");
    }

    @Test
    void aLevelWithPitsNeverReversesAndAPitWaveNamesAPlacedPit() {
        assertThrows(IllegalArgumentException.class, () -> LevelScript.builder(45, 7)
                .sections(List.of(new Section(10, 130), new Section(14, -130, false, 2, 1)))
                .objectives(ObjectiveSpec.NONE)
                .vrell(pits(100, 100))
                .build());
        WaveSpec stray = WaveSpec.builder(
                        5, WaveSpec.Formation.SINGLE, DRONE, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .pit(1)
                .build();
        assertThrows(IllegalArgumentException.class, () -> sortie(100, 100, stray));
    }

    @Test
    void aSwarmRisesAroundItsPitAndItsFlockFliesItsRouteFromThere() {
        WaveSpec swarm = WaveSpec.builder(
                        20, WaveSpec.Formation.SWARM, PartDSpecs.MOTE, 6, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(List.of(new WaveSpec.At(240, -60), new WaveSpec.At(240, 700))))
                .pit(0)
                .build();
        Sortie sortie = sortie(100, 120, swarm);
        while (sortie.events().count(SimEvents.Type.PIT_OPEN) == 0) {
            sortie.step(Command.NONE);
        }
        double pitX = sortie.pits().x(0);
        double pitY = sortie.pits().renderY(0, 1);
        assertEquals(1, sortie.flockCount());
        Flock flock = sortie.flock(0);
        assertEquals(pitX, flock.leaderX(), 20, "its leader point at the pit");
        assertEquals(6, Act7CDSpecs.count(sortie, "mote-swarm"));
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy mote = sortie.enemy(i);
            assertTrue(Math.hypot(mote.x() - pitX, mote.y() - pitY) < 60, "round the pit");
            assertTrue(sortie.pits().rising(mote));
        }
    }

    /**
     * M11 part C, step C2 (Level 45's Skitter swarms out of the pits): a pit's swarm of a unit without a flock rises as a
     * cloud round its pit, each unit on the route shifted by its own offset, none on top of another.
     */
    @Test
    void aFlocklessSwarmRisesAsACloudRoundItsPit() {
        WaveSpec swarm = WaveSpec.builder(
                        20, WaveSpec.Formation.SWARM, TestSpecs.SKITTER, 7, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(List.of(new WaveSpec.At(240, -60), new WaveSpec.At(240, 700))))
                .pit(0)
                .build();
        Sortie sortie = sortie(100, 120, swarm);
        while (sortie.events().count(SimEvents.Type.PIT_OPEN) == 0) {
            sortie.step(Command.NONE);
        }
        double pitX = sortie.pits().x(0);
        double pitY = sortie.pits().renderY(0, 1);
        assertEquals(7, Act7CDSpecs.count(sortie, "skitter"));
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy a = sortie.enemy(i);
            assertTrue(Math.hypot(a.x() - pitX, a.y() - pitY) < 60, "round the pit");
            assertTrue(sortie.pits().rising(a));
            for (int j = i + 1; j < sortie.enemyCount(); j++) {
                Enemy b = sortie.enemy(j);
                assertTrue(Math.hypot(a.x() - b.x(), a.y() - b.y()) > 10, "not on top of another");
            }
        }
    }
}
