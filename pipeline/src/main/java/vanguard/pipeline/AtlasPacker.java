package vanguard.pipeline;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Packs the sprite frames in {@code assets/} into the game's texture atlases (build output, never
 * committed), all with nearest filtering, since everything is drawn at its native size on whole
 * pixels: the shared {@code sprites} atlas with the game-wide sprites, one unit atlas {@code
 * level-NN} per level with every sprite that level uses (a unit of several levels is copied into each
 * of their atlases; {@link SpriteUse} decides from the levels' data, written out by {@link
 * SpriteClaims}) and {@code backdrop}. Subfolders become region names such as {@code level-01/earth}
 * or {@code hud/well} (a {@code .9.png} is a nine-patch with its splits); the sprites' subfolders (the
 * HUD, the UI kit, the hangar's icons and intel portraits, the speakers' portraits) are shared and
 * share the sprite pages, while each backdrop subfolder (one per level) gets pages of its own. The
 * packed pages are then checked against the production plan's budgets ({@link AtlasBudget}); an
 * exceeded budget fails the build.
 *
 * <p>Every atlas, and every backdrop subfolder, is packed on its own thread into a folder of its own;
 * the backdrop's parts are then joined in the order of their folder names, their pages numbered on
 * ({@code backdrop.png}, {@code backdrop2.png}, ...), as one packer run over the whole folder would
 * write them. The output is the same for the same frames, whatever the thread timing or the order in
 * which the file system lists the folders.
 *
 * <p>Usage: {@code AtlasPacker <spritesDir> <backdropDir> <claimsFile> <outputDir>}
 */
public final class AtlasPacker {
    /** A frame's sprite name: {@code skitter_3.png} and {@code skitter.png} are {@code skitter}. */
    private static final Pattern FRAME = Pattern.compile("(_\\d+)?(\\.9)?\\.png$");

    private static final String BACKDROP = "backdrop";

    private AtlasPacker() {}

    /** The sprite name of a frame's file name. */
    static String spriteName(String file) {
        return FRAME.matcher(file).replaceFirst("");
    }

    /** One packer run: an input folder packed into a folder of its own. */
    private record Job(Path input, Path output, String name, boolean combineSubdirectories, long bytes) {}

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "usage: AtlasPacker <spritesDir> <backdropDir> <claimsFile> <outputDir>");
        }
        Path sprites = Path.of(args[0]);
        Path backdrop = Path.of(args[1]);
        Map<String, List<String>> claims = readClaims(Path.of(args[2]));
        Path output = Path.of(args[3]);
        Path work = output.resolveSibling(output.getFileName() + "-work");
        clear(output);
        clear(work);
        Path staging = work.resolve("input");
        Path parts = work.resolve("parts");
        stage(sprites, claims, staging);
        List<Job> atlases = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(staging)) {
            for (Path atlas : dirs.sorted().toList()) {
                String name = atlas.getFileName().toString();
                atlases.add(new Job(atlas, parts.resolve(name), name, true, size(atlas)));
            }
        }
        // Each backdrop subfolder alone, under its own name, so its regions keep the folder prefix.
        List<Job> backdropParts = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(backdrop)) {
            for (Path dir : dirs.sorted().toList()) {
                String level = dir.getFileName().toString();
                if (!Files.isDirectory(dir)) {
                    throw new IllegalStateException(
                            "a backdrop image belongs in a level's subfolder: " + backdrop.relativize(dir));
                }
                Path input = work.resolve("backdrop-input").resolve(level);
                copyTree(dir, input.resolve(level));
                backdropParts.add(new Job(input, parts.resolve(BACKDROP + "-" + level), BACKDROP, false, size(input)));
            }
        }
        List<Job> jobs = new ArrayList<>(atlases);
        jobs.addAll(backdropParts);
        packAll(jobs);
        for (Job atlas : atlases) {
            moveAll(atlas.output(), output);
        }
        joinBackdrop(backdropParts, output);
        try (Stream<Path> files = Files.list(output)) {
            for (Path atlas : files.filter(file -> file.toString().endsWith(".atlas"))
                    .sorted()
                    .toList()) {
                Files.writeString(atlas, sortAliases(Files.readString(atlas)));
            }
        }
        clear(work);
        Files.delete(work);
        AtlasBudget.report(output).forEach(System.out::println);
        AtlasBudget.enforce(output);
    }

    /** Runs the packer jobs side by side, the largest first. */
    private static void packAll(List<Job> jobs) throws InterruptedException {
        int threads = Math.max(1, Math.min(jobs.size(), Runtime.getRuntime().availableProcessors()));
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> running = new ArrayList<>();
            jobs.stream()
                    .sorted(Comparator.comparingLong(Job::bytes).reversed())
                    .forEach(job -> running.add(pool.submit(() -> pack(job))));
            for (Future<?> future : running) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    throw new IllegalStateException("packing an atlas failed", e.getCause());
                }
            }
        } finally {
            pool.shutdownNow();
        }
        System.out.println("packed " + jobs.size() + " atlases and backdrop parts on " + threads + " threads");
    }

    /** {@code <sprite> <atlas>[,<atlas>...]} per line, from {@link SpriteClaims}. */
    private static Map<String, List<String>> readClaims(Path file) throws IOException {
        Map<String, List<String>> claims = new TreeMap<>();
        for (String line : Files.readAllLines(file)) {
            if (!line.isBlank()) {
                String[] parts = line.split(" ", 2);
                claims.put(parts[0], List.of(parts[1].split(",")));
            }
        }
        return claims;
    }

    /**
     * Copies the sprite frames into one input folder per atlas: the subfolders and every sprite
     * {@link SpriteUse} shares into {@code sprites}, the sprites levels use into the {@code
     * level-NN} of each of those levels.
     */
    private static void stage(Path sprites, Map<String, List<String>> claims, Path staging) throws IOException {
        List<Path> frames;
        try (Stream<Path> files = Files.list(sprites)) {
            frames = files.sorted().toList();
        }
        for (Path frame : frames) {
            String file = frame.getFileName().toString();
            if (Files.isDirectory(frame)) {
                copyTree(frame, staging.resolve(SpriteUse.SHARED).resolve(file));
            } else if (file.endsWith(".png")) {
                List<String> atlases = claims.get(spriteName(file));
                if (atlases == null) {
                    throw new IllegalStateException("no claim for sprite " + file + "; rerun SpriteClaims");
                }
                for (String atlas : atlases) {
                    Path target = staging.resolve(atlas);
                    Files.createDirectories(target);
                    Files.copy(frame, target.resolve(file));
                }
            }
        }
    }

    /**
     * Joins the backdrop parts into one {@code backdrop} atlas in their order: their pages are renamed
     * {@code backdrop.png}, {@code backdrop2.png}, ... and their page blocks appended to {@code
     * backdrop.atlas}.
     */
    private static void joinBackdrop(List<Job> parts, Path output) throws IOException {
        StringBuilder atlas = new StringBuilder();
        int pages = 0;
        for (Job part : parts) {
            Path partAtlas = part.output().resolve(BACKDROP + ".atlas");
            if (!Files.exists(partAtlas)) {
                continue;
            }
            String text = Files.readString(partAtlas);
            boolean pageStart = true;
            int at = 0;
            while (at < text.length()) {
                int end = text.indexOf('\n', at);
                end = end < 0 ? text.length() : end + 1;
                String line = text.substring(at, end);
                String content = line.strip();
                if (content.isEmpty()) {
                    pageStart = true;
                    atlas.append(line);
                } else if (pageStart) {
                    pageStart = false;
                    pages++;
                    String page = BACKDROP + (pages == 1 ? "" : String.valueOf(pages)) + ".png";
                    Files.move(part.output().resolve(content), output.resolve(page));
                    atlas.append(page).append(line, content.length(), line.length());
                } else {
                    atlas.append(line);
                }
                at = end;
            }
        }
        Files.writeString(output.resolve(BACKDROP + ".atlas"), atlas);
    }

    /**
     * Sorts each run of regions that share one packed rectangle (identical frames) by name and index.
     * The packer writes a rectangle's aliases from a hash set of identity-hashed objects, in a
     * different order on every run; sorted, the same frames give the same atlas file.
     */
    static String sortAliases(String atlas) {
        List<String> lines = List.of(atlas.split("\n", -1));
        StringBuilder sorted = new StringBuilder();
        List<List<String>> run = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            boolean region = !line.isEmpty()
                    && !line.startsWith(" ")
                    && i + 1 < lines.size()
                    && lines.get(i + 1).startsWith("  ");
            if (!region) {
                flush(run, sorted);
                sorted.append(line);
                if (i + 1 < lines.size()) {
                    sorted.append('\n');
                }
                i++;
                continue;
            }
            List<String> entry = new ArrayList<>();
            entry.add(line);
            i++;
            while (i < lines.size() && lines.get(i).startsWith("  ")) {
                entry.add(lines.get(i));
                i++;
            }
            if (!run.isEmpty() && !rectangle(run.getFirst()).equals(rectangle(entry))) {
                flush(run, sorted);
            }
            run.add(entry);
        }
        flush(run, sorted);
        return sorted.toString();
    }

    /** A region entry's packed rectangle: its position, size and rotation lines. */
    private static List<String> rectangle(List<String> entry) {
        return entry.stream()
                .filter(line -> line.startsWith("  xy:") || line.startsWith("  size:") || line.startsWith("  rotate:"))
                .toList();
    }

    private static void flush(List<List<String>> run, StringBuilder sorted) {
        run.sort(Comparator.<List<String>, String>comparing(List::getFirst).thenComparingInt(AtlasPacker::index));
        for (List<String> entry : run) {
            entry.forEach(line -> sorted.append(line).append('\n'));
        }
        run.clear();
    }

    private static int index(List<String> entry) {
        return entry.stream()
                .filter(line -> line.startsWith("  index:"))
                .mapToInt(line ->
                        Integer.parseInt(line.substring("  index:".length()).strip()))
                .findFirst()
                .orElse(-1);
    }

    private static void moveAll(Path from, Path to) throws IOException {
        if (!Files.exists(from)) {
            return;
        }
        try (Stream<Path> files = Files.list(from)) {
            for (Path file : files.sorted().toList()) {
                Files.move(file, to.resolve(file.getFileName().toString()));
            }
        }
    }

    private static long size(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(Files::isRegularFile)
                    .mapToLong(file -> {
                        try {
                            return Files.size(file);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .sum();
        }
    }

    private static void copyTree(Path from, Path to) throws IOException {
        try (Stream<Path> files = Files.walk(from)) {
            for (Path file : files.toList()) {
                Path target = to.resolve(from.relativize(file).toString());
                if (Files.isDirectory(file)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(file, target);
                }
            }
        }
    }

    /** Deletes a folder's contents, so no atlas of a former split stays behind. */
    private static void clear(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            return;
        }
        try (Stream<Path> files = Files.walk(dir)) {
            files.sorted(Comparator.reverseOrder())
                    .filter(file -> !file.equals(dir))
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    private static void pack(Job job) {
        var settings = new TexturePacker.Settings();
        settings.maxWidth = 2048;
        settings.maxHeight = 2048;
        settings.paddingX = 2;
        settings.paddingY = 2;
        settings.duplicatePadding = true;
        settings.filterMin = TextureFilter.Nearest;
        settings.filterMag = TextureFilter.Nearest;
        settings.stripWhitespaceX = false;
        settings.stripWhitespaceY = false;
        settings.combineSubdirectories = job.combineSubdirectories();
        settings.silent = true;
        long start = System.nanoTime();
        TexturePacker.process(settings, job.input().toString(), job.output().toString(), job.name());
        System.out.printf("packed %s in %.1f s%n", job.output().getFileName(), (System.nanoTime() - start) / 1e9);
    }
}
