package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part G (step G4): the Halo Platform's looks ({@link HaloLooks}; tools/art/halo_platform.py, assets/pivots/
 * halo-platform.json): the art has every set the looks read (the segment set by QG-1's rule, the turrets at 64 headings,
 * the core's ten frames, the barrier, the eye, the death's sets); the core's frames over the barrier's fade and the eye's
 * flare; the barrier's opacity; the tells' frames; the death's blast one cook-off gap after the last (as many as stand at
 * the kill, 0.15 s apart); the held piece: found among the set pieces, three rail turrets of six, no screen flash at its death.
 */
class HaloLooksTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Path SPRITES = ASSETS.resolve("sprites");
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(41).orElseThrow();

    private static JsonValue pivots() throws IOException {
        return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/halo-platform.json")));
    }

    private static SetPieceSpec halo() {
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        return script.setPieces().stream()
                .filter(SetPieceSpec::isBoss)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void theArtHasTheSetsTheLooksRead() throws IOException {
        JsonValue pivots = pivots();
        JsonValue ring = pivots.get("rings").child;
        assertEquals(ring.getInt("n8") + 1, frames("halo-platform-segment"), "the segment rule's set");
        assertEquals(ring.getInt("n8") + 1, ring.get("frames").size, "its pivots");
        assertEquals(HaloLooks.HEADINGS, frames("halo-platform-gun"), "the gun turret at 64 headings");
        assertEquals(HaloLooks.HEADINGS, frames("halo-platform-rail"), "the rail turret at 64 headings");
        assertEquals(10, frames("halo-platform-core"), "shut, waking, open, the flare");
        assertEquals(12, frames("halo-platform-barrier"));
        for (String set : new String[] {
            "rim", "shade", "arc", "lamp-gold", "lamp-red", "gun-wreck", "rail-wreck", "core-wreck", "scorch"
        }) {
            assertEquals(1, frames("halo-platform-" + set), set);
        }
        for (String set : new String[] {
            "gun-muzzle", "rail-charge", "barrier-hit", "eye-glow", "eye-flare", "cookoff", "cookoff-debris", "blowout"
        }) {
            assertTrue(frames("halo-platform-" + set) > 1, set);
        }
        assertEquals(HaloLooks.HEADINGS, pivots.get("turrets").get("gun").get("muzzle").size, "the guns' muzzles");
        assertEquals(HaloLooks.HEADINGS, pivots.get("turrets").get("rail").get("coils").size, "the rails' coils");
        assertTrue(frames("explosion-vacuum-large") > 0 && frames("rail-bunker-rail") > 0, "the shared sets");
    }

    @Test
    void theCoreTheBarrierAndTheTells() {
        int flare = SimStep.ticks(HaloLooks.FLARE_SECONDS);
        assertEquals(HaloLooks.CORE_SHUT, HaloLooks.coreFrame(false, false, 0, -1, flare), "shut under its barrier");
        assertEquals(1, HaloLooks.coreFrame(true, false, 0, -1, flare), "waking as the barrier fades");
        assertEquals(4, HaloLooks.coreFrame(true, false, 0.99, -1, flare));
        assertEquals(HaloLooks.CORE_OPEN, HaloLooks.coreFrame(false, true, 1, -1, flare), "open");
        assertEquals(HaloLooks.CORE_FLARE, HaloLooks.coreFrame(false, true, 1, 0, flare), "the flare's first");
        assertEquals(9, HaloLooks.coreFrame(false, true, 1, flare - 1, flare), "the flare's last");
        assertEquals(HaloLooks.CORE_OPEN, HaloLooks.coreFrame(false, true, 1, flare, flare), "back to open");
        assertEquals(1f, HaloLooks.barrierOpacity(false, false, 0), 1e-6f, "whole while the core is shut");
        assertEquals(0.5f, HaloLooks.barrierOpacity(true, false, 0.5), 1e-6f, "fading over the 0.5 s");
        assertEquals(0f, HaloLooks.barrierOpacity(false, true, 1), 1e-6f, "gone once open");
        int gun = SimStep.ticks(HaloLooks.GUN_TELL_SECONDS);
        assertEquals(-1, HaloLooks.tellFrame(-1, gun, 4));
        assertEquals(0, HaloLooks.tellFrame(0, gun, 4), "the muzzles rising");
        assertEquals(3, HaloLooks.tellFrame(gun - 1, gun, 4));
        assertEquals(-1, HaloLooks.tellFrame(gun, gun, 4), "over at the volley");
        assertFalse(HaloLooks.lampsLit(false, false), "dark while it sleeps");
        assertFalse(HaloLooks.lampsLit(true, true), "dark on a dead mount");
        assertTrue(HaloLooks.lampsLit(true, false));
    }

    /**
     * The held piece: found by its looks (no screen flash); its blast one 0.15 s gap
     * after the last of the turrets standing at the kill (all six cook off when none stands, the simulation's rule).
     */
    @Test
    void theHeldPieceAndItsDeath() throws IOException {
        assertEquals(
                SimStep.ticks(0.45), HaloLooks.blastTicks(3, 0.15), "three standing: 0.15 s apart, the blast after");
        assertEquals(SimStep.ticks(0.15), HaloLooks.blastTicks(0, 0.15), "at least one gap");
        SetPieceSpec spec = halo();
        HaloLooks looks = HaloLooks.following(spec, pivots());
        assertNotNull(looks);
        LevelScript script = SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM),
                60);
        SetPiece piece = HaloLooks.halo(sortie);
        assertNotNull(piece, "the held ringed piece");
        assertTrue(piece.held());
        assertEquals(SimStep.ticks(6 * 0.15), LevelRenderer.ringBlastTicks(piece), "all six standing");
        assertFalse(LevelRenderer.flashesAtDeath(piece), "no act boss's screen flash");
        int rails = 0;
        for (int p = 0; p < piece.partCount(); p++) {
            rails += looks.rail(sortie, p) ? 1 : 0;
        }
        assertEquals(3, rails, "alternating: three rail turrets");
        assertEquals(
                looks.corePart(),
                piece.ring().orElseThrow().core().orElseThrow().spec().part());
    }

    private static int frames(String name) throws IOException {
        if (Files.isRegularFile(SPRITES.resolve(name + ".png"))) {
            return 1;
        }
        try (Stream<Path> files = Files.list(SPRITES)) {
            return (int) files.map(path -> path.getFileName().toString())
                    .filter(file -> file.matches(java.util.regex.Pattern.quote(name) + "_\\d+\\.png"))
                    .count();
        }
    }
}
