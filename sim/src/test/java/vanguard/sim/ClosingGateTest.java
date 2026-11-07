package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * M11 part H, step H1b (design/enemies/bosses/choir-heart, Death; design/campaign Level 50, the closing gate;
 * m11h-gaps.md §1 #10, #11): the Heart's K (its core at 0) on its last words at 0 s: the heartbeat stops, it bursts at
 * once (its chain from K), the bullets fade out and are gone at K + 0.5 s, the act's flash at K + 3.5 s; the closing gate
 * is a transit timed from K: the gate's beat at K + 10.5, control taken at K + 12 (the ship eased to (240, 420 down) over
 * 1.5 s and held there whatever the commands, no shot), the level clock held to K + 12 and on after it, the warp home at
 * K + 29 (not the forward warp), the near side, the plane out and the glyphs at their times; a run replays.
 */
class ClosingGateTest {
    /** From the level start to just before K: every membrane and node cut, the core's phase open, the ship firing. */
    private static Sortie beforeKill(long seed) {
        Sortie sortie = Act7HPlaySpecs.invulnerable(seed, Act7HPlaySpecs.HEART);
        Act7HSpecs.untilEngaged(sortie);
        Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.MEMBRANES);
        Act7HSpecs.untilPhase(sortie, 1);
        Act7HPlaySpecs.kill(sortie, Act7HPlaySpecs.NODES);
        Act7HSpecs.untilPhase(sortie, 2);
        Act7HSpecs.steps(sortie, 8);
        return sortie;
    }

    private static final SimEvents.Type[] WATCHED = {
        SimEvents.Type.LAST_BURST,
        SimEvents.Type.BOSS_FLASH,
        SimEvents.Type.GATE_CLOSING,
        SimEvents.Type.TRANSIT_FORM,
        SimEvents.Type.WARP_HOME,
        SimEvents.Type.TRANSIT_WARP,
        SimEvents.Type.NEAR_SIDE,
        SimEvents.Type.PLANE_OUT,
        SimEvents.Type.GLYPH_DARK,
        SimEvents.Type.HEARTBEAT
    };

    @Test
    void theSceneRunsOnItsTimesFromK() {
        Sortie sortie = beforeKill(1);
        assertTrue(sortie.bulletCount() > 0, "the storm's bullets in flight at K");
        Act7HSpecs.kill(sortie, Act7HPlaySpecs.CORE);
        Map<SimEvents.Type, Integer> first = new EnumMap<>(SimEvents.Type.class);
        int heartbeats = 0;
        int glyphs = 0;
        int heldTick = -1;
        int form = SimStep.ticks(Act7HPlaySpecs.FORM);
        for (int i = 1; i <= SimStep.ticks(40); i++) {
            int command = i < form ? 0 : Command.LEFT.bit() | Command.UP.bit() | Command.FIRE.bit();
            sortie.step(command);
            SimEvents events = sortie.events();
            for (SimEvents.Type type : WATCHED) {
                if (events.count(type) > 0) {
                    first.putIfAbsent(type, i);
                }
            }
            heartbeats += events.count(SimEvents.Type.HEARTBEAT);
            glyphs += events.count(SimEvents.Type.GLYPH_DARK);
            if (i == SimStep.ticks(0.5) + 1) {
                assertEquals(0, sortie.bulletCount(), "the bullets gone by K + 0.5 s");
            }
            if (i == 2) {
                heldTick = sortie.levelTick;
            } else if (i > 2 && i < form - 1) {
                assertEquals(heldTick, sortie.levelTick, "the level clock held to K + 12");
            }
            if (i >= form + SimStep.ticks(1.5)) {
                assertEquals(Act7HPlaySpecs.FORM_X, sortie.ship().x(), 1e-6, "in formation: no input");
                assertEquals(Act7HPlaySpecs.FORM_Y, sortie.ship().y(), 1e-6);
            }
            if (i >= form + 2) {
                assertEquals(0, sortie.shotCount(), "no shot from the formation");
            }
        }
        assertTrue(sortie.levelTick > heldTick + SimStep.ticks(20), "the clock runs on after K + 12");
        assertEquals(0, heartbeats, "the heartbeat stops at K");
        assertEquals(1, first.get(SimEvents.Type.LAST_BURST), "the burst at once: its chain from K");
        assertEquals(SimStep.ticks(3.5), first.get(SimEvents.Type.BOSS_FLASH), "the flash at K + 3.5 s");
        assertEquals(SimStep.ticks(Act7HPlaySpecs.GATE_CLOSING), first.get(SimEvents.Type.GATE_CLOSING));
        assertEquals(form, first.get(SimEvents.Type.TRANSIT_FORM), "control taken at K + 12");
        assertEquals(SimStep.ticks(Act7HPlaySpecs.WARP), first.get(SimEvents.Type.WARP_HOME), "the warp home");
        assertEquals(null, first.get(SimEvents.Type.TRANSIT_WARP), "not the forward warp");
        assertEquals(SimStep.ticks(Act7HPlaySpecs.NEAR_SIDE), first.get(SimEvents.Type.NEAR_SIDE));
        assertEquals(SimStep.ticks(Act7HPlaySpecs.PLANE_OUT), first.get(SimEvents.Type.PLANE_OUT));
        assertEquals(SimStep.ticks(35), first.get(SimEvents.Type.GLYPH_DARK));
        assertEquals(2, glyphs, "the glyphs dark one by one");
        assertTrue(sortie.transit().reverse());
        assertEquals(1, sortie.transit().warp(), 1e-9, "the warp's share runs out");
    }

    @Test
    void aBossCheckpointBeforeKStartsTheSceneAfresh() {
        Sortie sortie = beforeKill(2);
        Act7HSpecs.kill(sortie, Act7HPlaySpecs.CORE);
        Act7HSpecs.steps(sortie, 5);
        assertTrue(sortie.transit().sinceKillSeconds() > 4.9);
        assertTrue(sortie.bossCheckpoint());
        sortie.retryFromBoss();
        sortie.step(0);
        assertEquals(-1, sortie.transit().sinceKillSeconds(), 1e-9);
        assertTrue(!sortie.transit().active());
    }

    @Test
    void aRunReplays() {
        long[] hashes = new long[2];
        for (int run = 0; run < 2; run++) {
            Sortie sortie = beforeKill(3);
            Act7HSpecs.kill(sortie, Act7HPlaySpecs.CORE);
            for (int i = 0; i < SimStep.ticks(32); i++) {
                sortie.step(i % 90 < 45 ? Command.LEFT.bit() : Command.RIGHT.bit());
            }
            hashes[run] = sortie.stateHash();
        }
        assertEquals(hashes[0], hashes[1]);
    }

    @Test
    void aLevelFortyThreeTransitKeepsItsFormationAndForwardWarp() {
        VrellLevel.Transit transit = new VrellLevel.Transit(10, 14.5, 1.5);
        assertEquals(GateTransit.FORM_X, transit.toX());
        assertEquals(GateTransit.FORM_Y, transit.toY());
        assertEquals(GateTransit.FORM_SECONDS, transit.easeSeconds());
        assertTrue(!transit.reverse() && !transit.fromKill() && transit.beats().isEmpty());
        assertEquals(List.of(), transit.beats());
    }
}
