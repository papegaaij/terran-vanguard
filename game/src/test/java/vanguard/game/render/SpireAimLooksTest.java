package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * Round 40 (the Siege Spire's lanes, proposal 4a; pending the user's review): a pod's aim glow before its
 * aimed shot, on {@link RoundFortyFixture}'s Spire: the reused emitter charge's frames and the pod's crimson
 * tint grow with the simulation's glow share, only on the pod whose turn it is, and only through its glow.
 */
class SpireAimLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));

    @Test
    void theGlowReusesTheEmitterChargeAndGrowsWithItsShare() {
        for (int i = 0; i < 4; i++) {
            assertTrue(Files.isRegularFile(ASSETS.resolve("sprites/siege-spire-emitter_" + i + ".png")));
        }
        assertEquals(0, SpireLooks.aimGlowFrame(0, 4));
        assertEquals(1, SpireLooks.aimGlowFrame(0.3, 4));
        assertEquals(3, SpireLooks.aimGlowFrame(0.99, 4));
        assertEquals(3, SpireLooks.aimGlowFrame(1, 4), "the full charge as it fires");
        assertEquals(SpireLooks.AIM_GLOW_FROM, SpireLooks.aimGlowStrength(0), 1e-6, "seen from its start");
        assertEquals(1, SpireLooks.aimGlowStrength(1), 1e-6);
        assertEquals(0, SpireLooks.aimTint(0), 1e-6);
        assertEquals(SpireLooks.AIM_TINT, SpireLooks.aimTint(1), 1e-6);
        assertTrue(SpireLooks.AIM_TINT < 0.5f, "the pod stays readable under its tint");
    }

    @Test
    void onlyThePodWhoseTurnItIsGlowsThroughItsTell() {
        Sortie sortie = RoundFortyFixture.spire(1);
        SetPiece boss = sortie.setPiece(0);
        int pod = -1;
        for (int i = 0; i < SimStep.ticks(20) && pod < 0; i++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.AIM_TELL) {
                    pod = events.value(e);
                }
            }
        }
        assertTrue(pod >= 0, "an aim tell within 20 s");
        for (int p = 0; p < boss.partCount(); p++) {
            if (p != pod) {
                assertEquals(-1, SpireLooks.aimShare(boss, p), 1e-9, "part " + p + " does not glow");
            }
        }
        double last = -1;
        int glowing = 0;
        while (SpireLooks.aimShare(boss, pod) >= 0) {
            double share = SpireLooks.aimShare(boss, pod);
            assertTrue(share >= last && share <= 1, "growing: " + share);
            last = share;
            glowing++;
            sortie.step(0);
        }
        assertTrue(last > 0.9, "full as it fires: " + last);
        assertEquals(SimStep.ticks(RoundFortyFixture.AIM_GLOW), glowing, 2, "through its 0.6 s glow");
    }
}
