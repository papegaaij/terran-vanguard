package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.content.Tier;
import vanguard.game.audio.Sfx;
import vanguard.sim.Command;
import vanguard.sim.Debris;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part D, step D4 (design/campaign Level 38; design/enemies/air/harrow): Level 38's looks and HUD by their rules: the
 * Harrow's sac glow by state, its tell's halo frames, its shell's fade and its burst as its death sound; the pod's flash
 * outline; the canister's sway; the storm's walls and cells pulsing on a told strike; the HARROWS tracker's label; the
 * CDF canister bracketed friendly while it is on the screen (flown on medium, invulnerable, holding fire).
 */
class Level38LooksTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(38).orElseThrow();

    @Test
    void theSacGlowsByItsState() {
        assertEquals(0.2, StormLayerLooks.sacGlow(true, true, 0.5, true, 0), 1e-9, "dim while stunned");
        assertEquals(0.75, StormLayerLooks.sacGlow(false, true, 0, false, 0), 1e-9, "the tell's start");
        assertEquals(1.0, StormLayerLooks.sacGlow(false, true, 1, false, 0), 1e-9, "the tell's peak");
        assertEquals(0.9, StormLayerLooks.sacGlow(false, false, 0, true, 0), 1e-9, "ramming");
        assertEquals(0.6, StormLayerLooks.sacGlow(false, false, 0, false, 0), 1e-9, "hunting, the pulse's middle");
        assertEquals(0.75, StormLayerLooks.sacGlow(false, false, 0, false, 0.25), 1e-9, "the pulse's top");
        assertEquals(0.45, StormLayerLooks.sacGlow(false, false, 0, false, 0.75), 1e-9, "its bottom, once a second");
        assertEquals(0, StormLayerLooks.tellFrame(0, 12));
        assertEquals(6, StormLayerLooks.tellFrame(0.5, 12));
        assertEquals(11, StormLayerLooks.tellFrame(1, 12), "the lock's flare held");
        assertEquals(1, StormLayerLooks.shellAlpha(0), 1e-6);
        assertEquals(0.5, StormLayerLooks.shellAlpha(SimStep.ticks(0.5)), 1e-6);
        assertEquals(0, StormLayerLooks.shellAlpha(SimStep.ticks(1)), 1e-6, "gone after 1 s");
        assertEquals(
                Sfx.HARROW_BURST, EnemyLooks.explosionSound("harrow", Tier.SMALL, false), "its burst, every death");
        assertEquals(Sfx.HARROW_BURST, EnemyLooks.explosionSound("harrow", Tier.SMALL, true));
    }

    @Test
    void theFlashesOutlineThePodAndPulseTheWalls() {
        assertEquals(1, StormLayerLooks.outlineAlpha(0), 1e-6);
        assertEquals(0, StormLayerLooks.outlineAlpha(SimStep.ticks(0.5)), 1e-6, "the README's 0.5 s");
        assertEquals(0, StormLayerLooks.outlineAlpha(-1), 1e-6);
        assertEquals(1, LightningLooks.pulseAt(0), 1e-6);
        assertEquals(0, LightningLooks.pulseAt(SimStep.ticks(LightningLooks.PULSE_SECONDS)), 1e-6);
        assertTrue(BackdropPieces.strikeLit("cloud-wall-a"));
        assertTrue(BackdropPieces.strikeLit("storm-cell-b"));
        assertFalse(BackdropPieces.strikeLit("aurelia-wreck"), "the wreck's fires do not flash");
        assertFalse(BackdropPieces.strikeLit("storm-wall"), "Level 36's storm wall keeps its own clock");
        assertEquals(0, PropPasses.swayFrame(0, 8));
        assertEquals(4, PropPasses.swayFrame(SimStep.ticks(1), 8), "4 fps");
        assertEquals(0, PropPasses.swayFrame(SimStep.ticks(2), 8), "a 2 s loop");
        assertEquals("HARROWS", MissionPanel.killsLabel("Harrows"));
        assertEquals("KILLS", MissionPanel.killsLabel(""));
    }

    /** The canister (t=100, down the middle) wears the brackets while it is on the screen; the cargo pod never does. */
    @Test
    void theCanisterIsMarkedFriendly() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FriendlyMarks marks = new FriendlyMarks(null, null);
        long limit = SimStep.ticks(130);
        int marked = 0;
        boolean pod = false;
        while (!sortie.complete() && sortie.tick() < limit) {
            sortie.step(sortie.ship().x() < 400 ? Command.RIGHT.bit() : 0);
            marks.plan(sortie, 1);
            for (int d = 0; d < sortie.debrisCount(); d++) {
                Debris chunk = sortie.debris(d);
                boolean on = FriendlyMarks.markedChunk(chunk, chunk.renderX(1), chunk.renderY(1));
                if (chunk.spec().chunk().equals("canister") && on) {
                    marked++;
                    boolean found = false;
                    for (int m = 0; m < marks.count(); m++) {
                        found |= Math.abs(marks.x(m) - chunk.renderX(1)) <= 1
                                && Math.abs(marks.y(m) - chunk.renderY(1)) <= 1;
                    }
                    assertTrue(found, "a mark on the canister at t=" + sortie.levelSeconds());
                }
                pod |= chunk.spec().chunk().equals("cargo-pod") && on;
            }
        }
        assertTrue(marked > SimStep.ticks(5), "the canister marked for its drift down the screen: " + marked);
        assertFalse(pod, "the pod is no friendly");
    }
}
