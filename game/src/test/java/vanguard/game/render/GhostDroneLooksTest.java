package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Tier;
import vanguard.game.audio.Sfx;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part E: the Ghost Drone's looks (design/enemies/air/ghost-drone; tools/art/storm_units.py's sprites and
 * assets/pivots/ghost-drone.json), on a hand-built sortie ({@link StormUnitsFixture}): 16 headings with their
 * additive eye and running-light glow, the lamps dark under the EMP, the eye's glint over the 0.3 s tell at the eye's
 * offset per heading, the mechanical death (sparks, smoke and fragments, its own sound on the {@code small} rung);
 * flown, the glint's frames span the tell from the glint to the burst's first shot.
 */
class GhostDroneLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        assertEquals(GhostDroneLooks.HEADINGS, frames("ghost-drone"), "16 headings");
        assertEquals(GhostDroneLooks.HEADINGS, frames("ghost-drone-glow"), "the eye and tips per heading (additive)");
        assertEquals(GhostDroneLooks.HEADINGS, frames("ghost-drone-stunned"), "the lamps dark per heading");
        assertEquals(6, frames("ghost-drone-glint"));
        assertTrue(frames("ghost-drone-death") > 0, "the sparks (EnemyLooks' death glow)");
        assertTrue(frames("ghost-drone-tatters") > 0, "the smoke and fragments (EnemyLooks' death pieces)");
    }

    @Test
    void theGlintPlaysOverTheTell() {
        assertEquals(-1, GhostDroneLooks.glintFrame(-1, 6));
        assertEquals(0, GhostDroneLooks.glintFrame(0, 6));
        assertEquals(5, GhostDroneLooks.glintFrame(17, 6));
        assertEquals(-1, GhostDroneLooks.glintFrame(18, 6), "over after the 0.3 s tell");
        assertEquals(SimStep.ticks(0.3), 6 * GhostDroneLooks.GLINT_FRAME_TICKS);
        assertEquals(-1, GhostDroneLooks.glintFrame(0, 0), "without its frames none");
    }

    @Test
    void theEyeSitsAtItsPivotInFrontAtEachHeading() throws IOException {
        JsonValue pivots = new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/ghost-drone.json")));
        double[][] eye = GhostDroneLooks.eyeOffsets(pivots);
        assertEquals(0, eye[0][0], 0.5, "heading 0 flies down");
        assertTrue(eye[0][1] < -5, "its eye below the centre (y up): " + eye[0][1]);
        assertTrue(eye[4][0] < -5, "heading 4 faces left: " + eye[4][0]);
        assertTrue(eye[8][1] > 5, "heading 8 flies up");
        assertTrue(eye[12][0] > 5, "heading 12 faces right");
        assertEquals(0, GhostDroneLooks.eyeOffsets(null)[3][1], "without the file: the sprite's centre");
    }

    @Test
    void itDiesWithItsOwnMechanicalBreakUpOnTheSmallRung() {
        assertEquals(Sfx.GHOST_DRONE_DEATH, EnemyLooks.explosionSound("ghost-drone", Tier.SMALL, false));
        assertEquals(Sfx.GHOST_DRONE_DEATH, EnemyLooks.explosionSound("ghost-drone", Tier.SMALL, true));
        assertEquals(Sfx.explosion(Tier.SMALL, true), EnemyLooks.explosionSound("needler", Tier.SMALL, true));
        assertEquals(Tier.SMALL, DomesFixture.CONTENT.enemy("ghost-drone").tier());
    }

    @Test
    void flownTheGlintSpansTheTellFromItsStartToTheFirstShot() {
        Sortie sortie = StormUnitsFixture.drone();
        long glint = -1;
        long fired = -1;
        int glintingSteps = 0;
        for (int i = 0; i < SimStep.ticks(8) && fired < 0; i++) {
            sortie.step(Command.NONE);
            Enemy drone = StormUnitsFixture.find(sortie);
            if (drone != null && drone.eyeGlinting()) {
                glintingSteps++;
            }
            if (glint < 0 && sortie.events().count(SimEvents.Type.EYE_GLINT) > 0) {
                glint = sortie.tick();
            }
            if (glint >= 0 && sortie.events().count(SimEvents.Type.ENEMY_FIRED) > 0) {
                fired = sortie.tick();
            }
        }
        assertTrue(glint >= 0 && fired > glint, "it glinted, then fired: " + glint + ", " + fired);
        assertEquals(SimStep.ticks(0.3), fired - glint, 1, "the tell");
        assertEquals(6 * GhostDroneLooks.GLINT_FRAME_TICKS, glintingSteps, 1, "the glint's frames span it");
    }

    private static int frames(String root) throws IOException {
        try (Stream<Path> files = Files.list(SPRITES)) {
            return (int) files.filter(file -> file.getFileName().toString().matches(root + "_\\d+\\.png"))
                    .count();
        }
    }
}
