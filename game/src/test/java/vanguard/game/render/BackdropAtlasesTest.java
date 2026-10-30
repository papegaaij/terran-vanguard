package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.BackdropData;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;

/**
 * Startup step: the backdrop atlases load per level. A level's own folder loads when it is entered, another level's
 * folder only when looked up (and is held for the entered level), and leaving frees what no entered level holds; every
 * level's backdrop names resolve with only its own atlases loaded.
 */
class BackdropAtlasesTest {
    private static final Path BACKDROP = Path.of(System.getProperty("vanguard.assetsDir", "../assets"), "backdrop");

    private final List<String> loads = new ArrayList<>();
    private final List<String> disposed = new ArrayList<>();
    private final BackdropAtlases<String> atlases = new BackdropAtlases<>(
            folder -> {
                loads.add(folder);
                return folder.equals("level-99") ? null : folder;
            },
            disposed::add);

    @Test
    void enteringALevelLoadsOnlyItsOwnFolder() {
        atlases.enter(19);

        assertEquals(List.of("level-19"), loads);
        assertEquals("level-19", atlases.atlas("level-19/canyon-floor"));
        assertEquals(List.of("level-19"), loads);
    }

    @Test
    void leavingFreesTheLevelsAtlasesAndTheNextLevelKeepsItsOwn() {
        atlases.enter(19);
        atlases.enter(35);
        atlases.leave(19);

        assertEquals(List.of("level-19"), disposed);
        assertFalse(atlases.loaded("level-19"));
        assertTrue(atlases.loaded("level-35"));
        atlases.leave(35);
        assertEquals(List.of("level-19", "level-35"), disposed);
    }

    @Test
    void anotherLevelsImagesLoadForTheEnteredLevelAndGoWithIt() {
        atlases.enter(14);

        assertEquals("level-09", atlases.atlas("level-09/megacity"));
        assertEquals(List.of("level-14", "level-09"), loads);
        atlases.leave(14);
        assertEquals(List.of("level-14", "level-09"), disposed);
    }

    @Test
    void aSharedFolderStaysWhileAnyHolderRuns() {
        atlases.enter(9);
        atlases.enter(14);
        atlases.atlas("level-09/megacity");
        atlases.leave(9);

        assertTrue(atlases.loaded("level-09"), "Level 14 still holds Level 09's images");
        atlases.leave(14);
        assertEquals(List.of("level-09", "level-14"), disposed.stream().sorted().toList());
    }

    @Test
    void aRetryEnteringTheLevelAgainKeepsItsAtlases() {
        atlases.enter(19);
        atlases.enter(19);
        atlases.leave(19);

        assertEquals(List.of(), disposed);
        assertEquals(List.of("level-19"), loads);
        atlases.leave(19);
        assertEquals(List.of("level-19"), disposed);
    }

    @Test
    void aNameWithoutAFolderOrAFolderWithoutAnAtlasHasNone() {
        atlases.enter(1);

        assertNull(atlases.atlas("earth"));
        assertNull(atlases.atlas("level-99/earth"));
        assertNull(atlases.atlas("level-99/moon"));
        assertEquals(List.of("level-01", "level-99"), loads, "a folder without an atlas is looked for once");
        atlases.leave(1);
        assertEquals(List.of("level-01"), disposed);
    }

    /**
     * Every level's tile sets and set pieces resolve in the folder atlases from assets/backdrop with only that level
     * entered, and leaving it leaves no atlas loaded.
     */
    @Test
    void everyLevelsBackdropResolvesWithItsOwnAtlasesAndLeavesNoneLoaded() {
        Content content = ContentLoader.fromClasspath();
        BackdropAtlases<Set<String>> real = new BackdropAtlases<>(BackdropAtlasesTest::images, images -> {});
        for (Map.Entry<String, LevelData> entry : content.levels().entrySet()) {
            int number = Content.levelNumber(entry.getKey());
            String folder = Backdrop.folder(entry.getKey(), entry.getValue());
            BackdropData backdrop = entry.getValue().backdrop();
            real.enter(number);
            for (String id : names(backdrop)) {
                Set<String> atlas = real.atlas(folder + id);
                assertTrue(
                        atlas != null && atlas.contains(folder + id),
                        entry.getKey() + ": " + folder + id + " resolves");
            }
            real.leave(number);
            for (int other = 1; other <= 50; other++) {
                assertFalse(real.loaded(BackdropAtlases.folder(other)), entry.getKey() + " left level " + other);
            }
        }
    }

    private static Set<String> names(BackdropData backdrop) {
        Set<String> names = new TreeSet<>(backdrop.tileSets().keySet());
        backdrop.pieces().forEach((id, piece) -> {
            if (piece.imageCount() == 1) {
                names.add(id);
            }
        });
        return names;
    }

    /** The image names a folder's atlas would hold ({@code level-NN/<id>}, without a frame index), or null. */
    private static Set<String> images(String folder) {
        Path dir = BACKDROP.resolve(folder);
        if (!Files.isDirectory(dir)) {
            return null;
        }
        try (Stream<Path> files = Files.list(dir)) {
            Set<String> names = new TreeSet<>();
            files.map(file -> file.getFileName().toString())
                    .filter(file -> file.endsWith(".png"))
                    .forEach(file -> names.add(folder + "/" + file.substring(0, file.length() - ".png".length())));
            return names;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
