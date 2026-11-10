package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.sim.SimEvents;

/**
 * M11 part H, step H4 (design/campaign Level 50, Music &amp; ambience; design/audio/sfx, Act 7 sounds; round 84's picks,
 * step AUD-GH): Level 50's sounds are there and wired: the Heart's and the closing gate's one-shots, the hive bed's
 * heartbeat layer rising over the chambers and out at the warning, the bed's −12 dB over the race, the near side's
 * open-space bed; track 17 over the approach, track 20 with its stem alone after K and its coda from the control's
 * taking; the glyphs quieter each; the radio's noise off on the near side.
 */
class Level50SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(50).orElseThrow();

    @Test
    void theHeartsAndTheGatesSoundsAreThere() {
        Map<Sfx, String> files = Map.ofEntries(
                Map.entry(Sfx.HEART_BEAT, "sfx/heart-beat-r98-c.ogg"),
                Map.entry(Sfx.HEART_MEMBRANE, "sfx/heart-membrane-r84-a.ogg"),
                Map.entry(Sfx.HEART_CLOSING, "sfx/heart-closing-r84-a.ogg"),
                Map.entry(Sfx.HEART_LINE, "sfx/heart-line-r84-a.ogg"),
                Map.entry(Sfx.HEART_NODE_CUT, "sfx/heart-node-cut-r84-a.ogg"),
                Map.entry(Sfx.HEART_OPENING, "sfx/heart-opening-r84-a.ogg"),
                Map.entry(Sfx.HEART_DEATH, "sfx/heart-death-r84-a.ogg"),
                Map.entry(Sfx.GATE_GROAN, "sfx/gate-groan-r84-a.ogg"),
                Map.entry(Sfx.GATE_GREYING, "sfx/gate-greying-r84-b.ogg"),
                Map.entry(Sfx.WARP_HOME, "sfx/warp-home-r84-a.ogg"),
                Map.entry(Sfx.GLYPH_DARK, "sfx/glyph-dark-r84-a.ogg"),
                Map.entry(Sfx.POD_SEED_VAULT, "sfx/pod-seed-vault-r84-a.ogg"),
                Map.entry(Sfx.AMBIENCE_HIVE_HEARTBEAT, "sfx/vrell-hive-heartbeat-r98-c.ogg"),
                Map.entry(Sfx.FINAL_BOSS_CODA, "music/final-boss-coda.ogg"));
        files.forEach((sfx, file) -> {
            assertEquals(file, sfx.path(), sfx.name());
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
        });
        for (SimEvents.Type type : new SimEvents.Type[] {
            SimEvents.Type.HEARTBEAT,
            SimEvents.Type.PART_WINDOW_OPEN,
            SimEvents.Type.GATE_CLOSING,
            SimEvents.Type.WARP_HOME,
            SimEvents.Type.GLYPH_DARK
        }) {
            assertTrue(HeartCues.TYPES.contains(type), type.name());
        }
        float last = Float.POSITIVE_INFINITY;
        for (int n = 1; n <= 8; n++) {
            float level = HeartCues.glyphLevel(n);
            assertTrue(level < last && level > 0, "each glyph quieter: " + level);
            last = level;
        }
    }

    @Test
    void theBedsLayersAndTheMusicFollowTheLevel() {
        LevelData level = CONTENT.level(KEY);
        LevelData.Music music = level.music();
        assertEquals(Sfx.AMBIENCE_VRELL_HIVE, Sfx.ambience(music.ambience()));
        var layer = music.ambienceLayers().orElseThrow().getFirst();
        assertEquals(Sfx.AMBIENCE_HIVE_HEARTBEAT, Sfx.ambience(layer.ambience()));
        assertEquals(182, layer.to(), 1e-9, "out at the warning");
        assertTrue(layer.from() >= 40 && layer.fadeIn() >= 60, "rising chamber by chamber");
        assertEquals(-12, LevelMusic.KILL_BED_DB, 1e-9, "the bed −12 dB after K, over the race");
        assertEquals("belt-open", music.ambienceChangeList().getLast().ambience(), "open space on the near side");
        assertEquals(Sfx.AMBIENCE_BELT_OPEN, Sfx.ambience("belt-open"));
        assertEquals(17, music.track(), "Act 7 B over the approach");
        assertEquals("final-boss", music.bossTrack().orElseThrow(), "track 20");
        for (String file : new String[] {
            Tracks.path(17), Tracks.path("final-boss"), Tracks.motifPath("final-boss"), "music/final-boss-coda.ogg"
        }) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(file)), file);
        }
        assertEquals(1, LevelMusic.KILL_STEM_SECONDS, 1e-9, "a 1 s crossfade to the stem at K");
        assertEquals(2, LevelMusic.GATE_OUT_SECONDS, 1e-9, "out over 2 s under the groan");
        var noise = level.noiseChanges();
        assertEquals(1, noise.size());
        assertEquals("near-side", noise.getFirst().on());
        assertEquals(0, noise.getFirst().step(), "the near side's clean radio");
    }
}
