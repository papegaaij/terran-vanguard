package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M11 part G, step G6 (design/enemies/bosses, the three echoes' <i>Echo</i>: each subtarget its own gun, silent at its
 * death; step G5's sim): Level 49's echoes' fire reads at its ports ({@link EchoFire}). Flown on a blunt pilot
 * ({@link BossPilot}) through all three fights: a dead part never flashes, every shot a living part's port fires
 * flashes that part in the same step (the Colossus's burrow volley at its mouth's place, out of the bulge), every gun
 * kind flashes, and the Carrier's and the Sovereign's forming volleys flash while they form. The Sovereign's outer ring
 * of {@code sam, gun, rail} turrets is drawn in its original's mount art by name (the gun turrets in the laser
 * battery's: no gun battery in the original's sets), and its laser sweep leaves the core.
 */
class Level49EchoFireTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(49).orElseThrow();
    /** The echoes form over 3 s from their first step on the field (the bosses' Echo sections). */
    private static final double FORM_SECONDS = 3;

    private static final List<String> RING = List.of("sam-1", "gun-1", "rail-1", "sam-2", "gun-2", "rail-2");

    @Test
    void theEchoesPartsFlashAtTheirPortsAndADeadPartNever() throws IOException {
        Sortie sortie = BossPilot.sortie(CONTENT, KEY);
        EchoFire fire = EchoFire.following();
        int pieces = sortie.setPieceCount();
        int[][] flashes = new int[pieces][];
        int[][] forming = new int[pieces][];
        long[][] died = new long[pieces][];
        long[] formedAt = new long[pieces];
        Arrays.fill(formedAt, Long.MAX_VALUE);
        int burrow = 0;
        boolean sweptFromCore = false;
        SovereignLooks sovereign = null;
        int limit = SimStep.ticks(420);
        for (int step = 0; step < limit && !sortie.complete(); step++) {
            sortie.step(BossPilot.commands(sortie, piece -> piece.echo().isPresent()));
            fire.stepped(sortie);
            long tick = sortie.tick();
            for (int k = 0; k < pieces; k++) {
                SetPiece piece = sortie.setPiece(k);
                if (piece.echo().isEmpty()) {
                    continue;
                }
                if (flashes[k] == null) {
                    flashes[k] = new int[piece.partCount()];
                    forming[k] = new int[piece.partCount()];
                    died[k] = new long[piece.partCount()];
                    Arrays.fill(died[k], Long.MAX_VALUE);
                }
                if (piece.present() && formedAt[k] == Long.MAX_VALUE) {
                    formedAt[k] = tick;
                }
                for (int p = 0; p < piece.partCount(); p++) {
                    if (piece.partHp(p) <= 0 && died[k][p] == Long.MAX_VALUE) {
                        died[k][p] = tick;
                    }
                    int since = fire.flashTicks(k, p, tick);
                    if (since == 0) {
                        assertTrue(piece.partHp(p) > 0, "a dead part never fires: " + name(piece, p));
                        flashes[k][p]++;
                        if (tick - formedAt[k] < SimStep.ticks(FORM_SECONDS)) {
                            forming[k][p]++;
                        }
                    }
                    if (tick - died[k][p] >= (long) EchoFire.FRAMES * EchoFire.TICKS_PER_FRAME) {
                        assertEquals(-1, since, "a dead part's flash is over: " + name(piece, p));
                    }
                }
                if (!piece.present()) {
                    continue;
                }
                // Every shot a living part's port fired flashes that part in its step.
                for (int i = 0; i < sortie.events().size(); i++) {
                    SimEvents.Type type = sortie.events().type(i);
                    if (type != SimEvents.Type.ENEMY_FIRED
                            && type != SimEvents.Type.MISSILE_LAUNCHED
                            && type != SimEvents.Type.MORTAR_LOBBED) {
                        continue;
                    }
                    double x = sortie.events().x(i);
                    double y = sortie.events().y(i);
                    int p = EchoFire.firer(piece, x, y);
                    if (p >= 0) {
                        assertEquals(0, fire.flashTicks(k, p, tick), "flashed at its port: " + name(piece, p));
                    }
                    var breach = piece.breach().orElse(null);
                    if (breach != null && Math.hypot(x - breach.mouthX(), y - breach.mouthY()) < 1) {
                        assertEquals(breach.mouth(), p, "the burrow volley out of the mouth's place");
                    }
                    if (breach != null
                            && p == breach.mouth()
                            && Math.hypot(piece.partX(p) - x, piece.partY(p) - y) > EchoFire.REACH) {
                        // Out of the bulge ahead of the mouth, or (the ship on its lip) out of the spine.
                        burrow++;
                    }
                }
                if (piece.ring().isPresent()) {
                    if (sovereign == null) {
                        sovereign = SovereignLooks.following(piece.spec(), pivots());
                        for (String part : RING) {
                            int p = part(piece, part);
                            assertTrue(p >= 0, "the echo's outer ring has " + part);
                            String kind = sovereign.drawnKind(piece, p);
                            assertEquals(SovereignLooks.batteryKind(part), kind, part);
                            for (String sprite : List.of("", "-mount", "-wreck")) {
                                String file = "sprites/iron-sovereign-" + kind + sprite;
                                assertTrue(
                                        Files.exists(ASSETS.resolve(file + "_0.png"))
                                                || Files.exists(ASSETS.resolve(file + ".png")),
                                        part + " drawn in " + file);
                            }
                        }
                        assertNull(sovereign.drawnKind(piece, part(piece, "core")), "the core is no battery");
                    }
                    int core = part(piece, "core");
                    for (int a = 0; a < piece.attackCount(); a++) {
                        if (piece.sweepBeaming(a)) {
                            assertEquals(piece.partX(core), piece.sweepOriginX(a), 2, "the sweep leaves the core");
                            assertEquals(piece.partY(core), piece.sweepOriginY(a), 2, "the sweep leaves the core");
                            sweptFromCore = true;
                        }
                    }
                }
            }
        }
        assertTrue(sortie.complete(), "the pilot flies the rush to its end");
        assertNotNull(sovereign, "the Sovereign's echo flew");
        for (int k = 0; k < pieces; k++) {
            SetPiece piece = sortie.setPiece(k);
            if (flashes[k] == null) {
                continue;
            }
            String slug = piece.spec().slug();
            switch (slug) {
                case "brood-carrier" -> {
                    assertTrue(flashes[k][part(piece, "mandibles")] >= 3, "the mandibles' fans");
                    assertTrue(flashes[k][part(piece, "core")] >= 3, "the core's gun in stage 2");
                    int sacs = 0;
                    for (int p = 0; p < piece.partCount(); p++) {
                        sacs += name(piece, p).startsWith("bay") && flashes[k][p] > 0 ? 1 : 0;
                    }
                    assertTrue(sacs >= 6, "the sacs' own guns: " + sacs + " of 8");
                    assertTrue(
                            Arrays.stream(forming[k]).sum() >= 3,
                            "its forming volleys: " + Arrays.toString(forming[k]));
                }
                case "dust-colossus" -> {
                    // The vents' guns (when they fire on this flight) go by the per-shot check above.
                    assertTrue(flashes[k][part(piece, "mouth")] >= 3, "the mouth's fire");
                }
                case "iron-sovereign" -> {
                    assertTrue(flashes[k][part(piece, "core")] >= 3, "the core's fans and gun");
                    assertTrue(flashes[k][part(piece, "gun-1")] + flashes[k][part(piece, "gun-2")] >= 2, "the guns");
                    assertTrue(
                            Arrays.stream(forming[k]).sum() >= 3,
                            "its forming volleys: " + Arrays.toString(forming[k]));
                }
                default -> throw new AssertionError("an echo of " + slug);
            }
        }
        assertTrue(burrow >= 3, "the burrow volley flashed out of the bulge: " + burrow);
        assertTrue(sweptFromCore, "the laser sweep fired");
    }

    @Test
    void theRingsPartNamesNameTheirOriginalsArt() {
        assertEquals("sam", SovereignLooks.batteryKind("sam-2"));
        assertEquals("rail", SovereignLooks.batteryKind("rail-1"));
        assertEquals("laser", SovereignLooks.batteryKind("laser-3"));
        assertEquals(SovereignLooks.GUN_LOOK, SovereignLooks.batteryKind("gun-1"));
        assertNull(SovereignLooks.batteryKind("core"));
    }

    private static JsonValue pivots() {
        try {
            return new JsonReader().parse(Files.readString(ASSETS.resolve("pivots/iron-sovereign.json")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int part(SetPiece piece, String name) {
        for (int p = 0; p < piece.partCount(); p++) {
            if (name(piece, p).equals(name)) {
                return p;
            }
        }
        return -1;
    }

    private static String name(SetPiece piece, int p) {
        return piece.spec().parts().get(p).name();
    }
}
