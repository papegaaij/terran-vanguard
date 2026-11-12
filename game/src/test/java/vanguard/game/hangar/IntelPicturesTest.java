package vanguard.game.hangar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import vanguard.content.BriefingPage;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.campaign.Intel;
import vanguard.game.render.Portraits;

/**
 * The user's Act 7 playthrough (2026-11-12): the hangar crashed before Level 48 on {@code no region
 * 'intel/boss-vorne's-chimera' in the atlas} (the boss's name slugged with its apostrophe, and no silhouette for Act 7's
 * bosses). Every picture the intel panel draws for levels 01-50, laid out at sensor L3 the way the hangar lays it out
 * (the enemies' portraits in their liveries, an unclassified unit's silhouette, the boss's silhouette in its row and on
 * the wave strip, the contacts'), and every enemy type's portrait and every boss's silhouette by the names the panel
 * builds, resolves to a sprite in {@code assets/sprites/intel/}: the atlas packer puts that subfolder on the shared
 * sprite pages as the regions {@code intel/<name>}, so a missing file is a missing region.
 */
class IntelPicturesTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Path SPRITES =
            Path.of(System.getProperty("vanguard.assetsDir", "../assets")).resolve("sprites");

    /** Collects the regions the panel draws. */
    private static final class Pictures implements IntelPanel.Canvas {
        final Set<String> names = new TreeSet<>();

        @Override
        public void text(String text, Color colour, int x, int y, boolean body) {}

        @Override
        public void portrait(BriefingPage teaser, int x, int y, int size) {}

        @Override
        public void picture(String name, int x, int y, int size) {
            names.add(name);
        }

        @Override
        public void mark(String name, float x, int y, int size) {
            names.add(name);
        }

        @Override
        public void chip(String text, int x, int y, int width, int height) {}

        @Override
        public void bar(Color colour, int x, int y, int width, int height) {}

        @Override
        public void inset(int x, int y, int width, int height) {}

        @Override
        public void fill(Color colour, float x, int y, float width, int height) {}
    }

    /** A region's sprite: {@code intel/x} is {@code intel/x.png} (or its first frame {@code intel/x_0.png}). */
    private static boolean packed(String region) {
        return Files.isRegularFile(SPRITES.resolve(region + ".png"))
                || Files.isRegularFile(SPRITES.resolve(region + "_0.png"));
    }

    @Test
    void theApostropheIsLeftOutOfAName() {
        assertEquals("vornes-chimera", Portraits.slug("Vorne's Chimera"));
        assertEquals("the-echoes", Portraits.slug("The Echoes"));
        assertEquals("choir-heart", Portraits.slug("Choir Heart"));
        assertEquals(Optional.of("vornes-chimera"), IntelPanel.linkedSlug("intel/boss-vornes-chimera"));
    }

    @Test
    void everyPictureOfEveryLevelsIntelIsInTheAtlas() {
        Set<String> missing = new TreeSet<>();
        int levels = 0;
        for (String level : CONTENT.levels().keySet()) {
            levels++;
            int number = Content.levelNumber(level);
            Intel intel = Intel.of(CONTENT, level, 3);
            BriefingPage teaser = CONTENT.level(level).briefing().teaser();
            IntelPanel.Strip strip = IntelPanel.strip(CONTENT, level);
            // As the hangar shows it before the level is flown (a unit kept unclassified as its silhouette), and after.
            for (Map<String, String> unclassified : java.util.List.of(
                    IntelPanel.unclassified(CONTENT, intel.enemies(), number), Map.<String, String>of())) {
                Pictures pictures = new Pictures();
                IntelPanel.layout(pictures, intel, teaser, level, unclassified, Optional.empty(), strip, false);
                pictures.names.stream()
                        .filter(name -> !packed(name))
                        .forEach(name -> missing.add(number + ": " + name));
            }
            // Every type, also the ones past the panel's few portraits, and the boss, by the names the panel builds.
            for (String enemy : intel.enemies()) {
                String slug = Portraits.slug(enemy);
                for (String name : java.util.List.of(
                        "intel/" + slug, "intel/" + strip.liveries().getOrDefault(slug, slug))) {
                    if (!packed(name)) {
                        missing.add(number + ": " + name);
                    }
                }
            }
            String boss = intel.profile().boss();
            if (!boss.equals("none") && !packed("intel/boss-" + Portraits.slug(boss))) {
                missing.add(number + ": intel/boss-" + Portraits.slug(boss));
            }
        }
        assertEquals(50, levels, "levels 01-50");
        assertTrue(missing.isEmpty(), "pictures with no sprite: " + missing);
    }
}
