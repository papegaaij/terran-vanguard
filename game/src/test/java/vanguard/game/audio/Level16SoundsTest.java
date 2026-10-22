package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.EnemySpec;
import vanguard.sim.Sortie;

/**
 * M7 part C's sounds (design/audio/sfx and music, Level 16 Valles Canyon Run): round 39's proposals (a each)
 * until the pick, the {@code mars-canyon} ambience from the start, "Red Dust Run" from section 2 and its full mix
 * from section 4, and a Threadcrawler's cut told apart from a Coilwyrm's by its kinds.
 */
class Level16SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(16).orElseThrow();
    private static final LevelData LEVEL = CONTENT.level(KEY);

    @Test
    void everySoundHasItsFile() {
        for (Sfx sfx : List.of(
                Sfx.TENDRIL_HUM, Sfx.TENDRIL_BURN, Sfx.TENDRIL_BREAK, Sfx.CRAWLER_WALK, Sfx.AMBIENCE_MARS_CANYON)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            // Round 46's picks (the user, 2026-10-21): the tendril's break b, the rest a; the hum stays round 39's a.
            String pick =
                    sfx == Sfx.TENDRIL_HUM ? "-r39-a.ogg" : sfx == Sfx.TENDRIL_BREAK ? "-r46-b.ogg" : "-r46-a.ogg";
            assertTrue(sfx.path().endsWith(pick), sfx + ": " + sfx.path());
        }
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_MARS_CANYON.priority());
    }

    @Test
    void theCanyonsWindFromTheStartRedDustRunFromSectionTwoTheFullMixFromFour() {
        assertEquals(Sfx.AMBIENCE_MARS_CANYON, Sfx.ambience(LEVEL.music().ambience()));
        LevelData.Music music = LEVEL.music();
        assertEquals(8, music.track(), "Act 3 A, Red Dust Run");
        assertEquals(2, music.startSection(), "the base stem from Into the Canyon");
        assertFalse(music.full(3));
        assertTrue(music.full(4), "the full mix from the Coprates rapids");
    }

    @Test
    void theCrawlersPartsAreItsHeadSegmentAndTail() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM),
                60);
        List<EnemySpec> kinds = sortie.enemyKinds();
        boolean[] parts = FlightSounds.crawlerParts(kinds);
        int count = 0;
        for (int k = 0; k < kinds.size(); k++) {
            assertEquals(
                    kinds.get(k).slug().startsWith("threadcrawler"),
                    parts[k],
                    kinds.get(k).slug());
            count += parts[k] ? 1 : 0;
        }
        // a head per crawl path (each wave its own), the segment and the tail
        assertTrue(count >= 3, "head, segment and tail: " + count);
    }
}
