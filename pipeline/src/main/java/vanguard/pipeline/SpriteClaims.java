package vanguard.pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import vanguard.content.ContentLoader;

/**
 * Writes the atlases every sprite in {@code assets/sprites} goes into, as {@link SpriteUse} decides
 * from the levels' data: one line {@code <sprite> <atlas>[,<atlas>...]} per sprite name, sorted.
 * {@link AtlasPacker} stages the frames from this file, so the packed atlases depend on the data's
 * claims, not on the whole content: a data or code change that moves no sprite reuses them.
 *
 * <p>Usage: {@code SpriteClaims <spritesDir> <claimsFile>}
 */
public final class SpriteClaims {
    private SpriteClaims() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("usage: SpriteClaims <spritesDir> <claimsFile>");
        }
        List<String> names;
        try (Stream<Path> files = Files.list(Path.of(args[0]))) {
            names = files.filter(Files::isRegularFile)
                    .map(frame -> frame.getFileName().toString())
                    .filter(file -> file.endsWith(".png"))
                    .map(AtlasPacker::spriteName)
                    .distinct()
                    .sorted()
                    .toList();
        }
        Map<String, Set<String>> atlases = SpriteUse.atlases(ContentLoader.fromClasspath(), names);
        List<String> lines = new ArrayList<>();
        for (String name : names) {
            lines.add(name + " "
                    + String.join(",", atlases.get(name).stream().sorted().toList()));
        }
        Path claims = Path.of(args[1]);
        Files.createDirectories(claims.toAbsolutePath().getParent());
        Files.write(claims, lines);
    }
}
