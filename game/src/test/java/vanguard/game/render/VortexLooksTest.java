package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part E: the Dust Devil's looks (design/enemies/ground/dust-devil; tools/art/storm_units.py's sprites), on a
 * hand-built sortie ({@link StormUnitsFixture}): the spin over its 8 radial frames, slow while closed and faster from
 * the rise; the vanes parting over the rise's three steps and closing backwards while it sinks; the grit swirl
 * tightening over the rise and loosening as it sinks; its collapse under its grit cloud; flown, the cycle runs
 * closed, rising, open, sinking and the looks follow it, drawn under the storm's veil in every phase.
 */
class VortexLooksTest {
    private static final Path SPRITES =
            Path.of(System.getProperty("vanguard.assetsDir", "../assets")).resolve("sprites");

    @Test
    void theSpinRunsOverItsEightFramesFasterFromTheRise() {
        assertEquals(0, VortexLooks.spinFrame(0));
        assertEquals(7, VortexLooks.spinFrame(7.9));
        assertEquals(0, VortexLooks.spinFrame(8), "8 frames cover the vanes' 72 degrees");
        assertEquals(VortexLooks.CLOSED_SPIN_FPS, VortexLooks.spinFps(Enemy.VortexPhase.CLOSED));
        assertEquals(VortexLooks.CLOSED_SPIN_FPS, VortexLooks.spinFps(Enemy.VortexPhase.SINKING));
        assertEquals(VortexLooks.OPEN_SPIN_FPS, VortexLooks.spinFps(Enemy.VortexPhase.RISING), "the tell spins up");
        assertEquals(VortexLooks.OPEN_SPIN_FPS, VortexLooks.spinFps(Enemy.VortexPhase.OPEN));
        assertTrue(VortexLooks.OPEN_SPIN_FPS > VortexLooks.CLOSED_SPIN_FPS);
    }

    @Test
    void theVanesPartOverTheRiseAndCloseBackwardsAsItSinks() {
        assertEquals(0, VortexLooks.riseStep(Enemy.VortexPhase.RISING, 0));
        assertEquals(1, VortexLooks.riseStep(Enemy.VortexPhase.RISING, 0.5));
        assertEquals(2, VortexLooks.riseStep(Enemy.VortexPhase.RISING, 1), "nearly open at its end");
        assertEquals(2, VortexLooks.riseStep(Enemy.VortexPhase.SINKING, 0), "the sink starts nearly open");
        assertEquals(0, VortexLooks.riseStep(Enemy.VortexPhase.SINKING, 0.99));
        assertEquals(-1, VortexLooks.riseStep(Enemy.VortexPhase.CLOSED, 0.5), "the closed set");
        assertEquals(-1, VortexLooks.riseStep(Enemy.VortexPhase.OPEN, 0.5), "the open set");
    }

    @Test
    void theSwirlTightensOverTheRiseAndLoosensAsItSinks() {
        assertEquals(0, VortexLooks.tightShare(Enemy.VortexPhase.CLOSED, 0.7), 1e-6);
        assertEquals(0.25, VortexLooks.tightShare(Enemy.VortexPhase.RISING, 0.25), 1e-6);
        assertEquals(1, VortexLooks.tightShare(Enemy.VortexPhase.OPEN, 0.1), 1e-6);
        assertEquals(0.75, VortexLooks.tightShare(Enemy.VortexPhase.SINKING, 0.25), 1e-6);
    }

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        assertEquals(VortexLooks.SPIN_FRAMES, frames("dust-devil"), "closed: the radial spin");
        assertEquals(VortexLooks.RISE_STEPS * VortexLooks.SPIN_FRAMES, frames("dust-devil-rise"));
        assertEquals(VortexLooks.SPIN_FRAMES, frames("dust-devil-open"));
        assertEquals(6, frames("dust-devil-core"), "the lime core's pulse");
        assertEquals(12, frames("dust-devil-swirl"));
        assertEquals(12, frames("dust-devil-swirl-tight"));
        assertEquals(8, frames("dust-devil-collapse"), "the collapse, 0.8 s at 10 fps");
        assertEquals(SimStep.ticks(0.1), EnemyLooks.COLLAPSE_FRAME_TICKS);
        assertTrue(frames("dust-devil-tatters") > 0, "the grit cloud (EnemyLooks' death pieces)");
        assertTrue(frames("dust-devil-death") > 0, "the lime flash (EnemyLooks' death glow)");
    }

    @Test
    void flownItCyclesClosedRisingOpenAndSinkingAndTheLooksFollow() {
        Sortie sortie = StormUnitsFixture.devil();
        Set<Enemy.VortexPhase> phases = EnumSet.noneOf(Enemy.VortexPhase.class);
        Set<Integer> riseSteps = new HashSet<>();
        Set<Integer> sinkSteps = new HashSet<>();
        float lastTight = -1;
        boolean tightened = false;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            Enemy devil = StormUnitsFixture.find(sortie);
            if (devil == null) {
                continue;
            }
            Enemy.VortexPhase phase = devil.vortexPhase();
            phases.add(phase);
            int step = VortexLooks.riseStep(phase, devil.vortexProgress());
            if (phase == Enemy.VortexPhase.RISING) {
                riseSteps.add(step);
                float tight = VortexLooks.tightShare(phase, devil.vortexProgress());
                tightened |= lastTight >= 0 && tight > lastTight;
                lastTight = tight;
            } else if (phase == Enemy.VortexPhase.SINKING) {
                sinkSteps.add(step);
            }
        }
        assertEquals(EnumSet.allOf(Enemy.VortexPhase.class), phases, "it ran its whole cycle on the screen");
        assertEquals(Set.of(0, 1, 2), riseSteps, "the vanes part in three steps over the rise");
        assertTrue(sinkSteps.contains(2) && sinkSteps.contains(0), "and close back as it sinks: " + sinkSteps);
        assertTrue(tightened, "the swirl tightens over the rise (the tell)");
    }

    /**
     * Round 41 K.a: in every phase the devil is drawn under the dust storm's veil, with the ground units while closed,
     * rising or sinking and on low-air while open, never with the flyers (over the veil, as a silhouette), so it
     * does not vanish into the dust as it opens outside the radius and come back as it closes.
     */
    @Test
    void itIsDrawnUnderTheVeilInEveryPhase() {
        Sortie sortie = StormUnitsFixture.devil();
        Set<Enemy.VortexPhase> phases = EnumSet.noneOf(Enemy.VortexPhase.class);
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.NONE);
            Enemy devil = StormUnitsFixture.find(sortie);
            if (devil == null) {
                continue;
            }
            Enemy.VortexPhase phase = devil.vortexPhase();
            phases.add(phase);
            LevelRenderer.Depth expected =
                    phase == Enemy.VortexPhase.OPEN ? LevelRenderer.Depth.LOW_AIR : LevelRenderer.Depth.GROUND;
            assertEquals(expected, LevelRenderer.Depth.of(devil), phase + " at step " + i);
        }
        assertEquals(EnumSet.allOf(Enemy.VortexPhase.class), phases, "it ran its whole cycle on the screen");
    }

    private static int frames(String root) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return (int) files.filter(file -> file.getFileName().toString().matches(root + "_\\d+\\.png"))
                    .count();
        }
    }
}
