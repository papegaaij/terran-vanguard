package vanguard.pipeline;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/**
 * Packs the sprite frames in {@code assets/} into the game's texture atlases (build output, never
 * committed), all with nearest filtering, since everything is drawn at its native size on whole
 * pixels: the shared {@code sprites} atlas with the game-wide sprites, one unit atlas {@code
 * level-NN} per level with every sprite that level uses (a unit of several levels is copied into each
 * of their atlases; {@link SpriteUse} decides from the levels' data, written out by {@link
 * SpriteClaims}) and one backdrop atlas {@code backdrop-level-NN} per backdrop subfolder, which the game also loads
 * only while that level runs. Subfolders become region names such as {@code level-01/earth}
 * or {@code hud/well} (a {@code .9.png} is a nine-patch with its splits); the sprites' subfolders (the
 * HUD, the UI kit, the hangar's icons and intel portraits, the speakers' portraits) are shared and
 * share the sprite pages, while each backdrop subfolder (one per level) gets pages of its own. The
 * packed pages are then checked against the production plan's budgets ({@link AtlasBudget}); an
 * exceeded budget fails the build.
 *
 * <p>Every atlas, and every backdrop subfolder, is packed on its own thread into a folder of its own;
 * each backdrop part is then written out as {@code backdrop-level-NN.atlas} with its pages renamed
 * {@code backdrop-level-NN.png}, {@code backdrop-level-NN2.png}, ... (startup step: one backdrop atlas for every
 * level made the game load all of them before the title screen). The output is the same for the same frames,
 * whatever the thread timing or the order in which the file system lists the folders.
 *
 * <p>M7 part F: the frames of a few large sprites the game only ever draws centred ({@link #CENTRED_CROP}) are cropped
 * on their way into the atlas, each frame by the same margin on both sides down to its visible pixels, so its centre
 * stays its centre and the game draws it unchanged; the source art keeps its size.
 *
 * <p>Usage: {@code AtlasPacker <spritesDir> <backdropDir> <claimsFile> <outputDir>}
 */
public final class AtlasPacker {
    /** A frame's sprite name: {@code skitter_3.png} and {@code skitter.png} are {@code skitter}. */
    private static final Pattern FRAME = Pattern.compile("(_\\d+)?(\\.9)?\\.png$");

    private static final String BACKDROP = "backdrop";

    /**
     * M7 part F: the sprites cropped about their centre in the atlas (design/art-direction/production, Budgets: ≤ 6
     * pages a level, the boss included): the Revenant Walker's legs, topple and wreck, 248 and 280 px frames whose
     * corners are empty (the walk's and the fall's reach), drawn only centred (WalkerLooks); Level 19 fits its 6 pages
     * with them cropped.
     */
    static final Set<String> CENTRED_CROP =
            Set.of("revenant-walker-legs", "revenant-walker-topple", "revenant-walker-wreck");

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
                backdropParts.add(new Job(input, parts.resolve(backdropAtlas(level)), BACKDROP, false, size(input)));
            }
        }
        List<Job> jobs = new ArrayList<>(atlases);
        jobs.addAll(backdropParts);
        packAll(jobs);
        for (Job atlas : atlases) {
            moveAll(atlas.output(), output);
        }
        for (Job part : backdropParts) {
            writeBackdrop(part, output);
        }
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
                BufferedImage cropped =
                        CENTRED_CROP.contains(spriteName(file)) ? centredCrop(ImageIO.read(frame.toFile())) : null;
                for (String atlas : atlases) {
                    Path target = staging.resolve(atlas);
                    Files.createDirectories(target);
                    if (cropped != null) {
                        ImageIO.write(cropped, "png", target.resolve(file).toFile());
                    } else {
                        Files.copy(frame, target.resolve(file));
                    }
                }
            }
        }
    }

    /**
     * A frame cropped by the same margin on its left and right (and on its top and bottom) down to its visible
     * pixels: the largest margins that cut none of them, so the crop keeps the frame's centre; an empty frame keeps a
     * pixel or two at its centre.
     */
    static BufferedImage centredCrop(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int left = width;
        int right = -1;
        int top = height;
        int bottom = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (image.getRGB(x, y) >>> 24 != 0) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        int marginX = right < 0 ? (width - 1) / 2 : Math.min(left, width - 1 - right);
        int marginY = bottom < 0 ? (height - 1) / 2 : Math.min(top, height - 1 - bottom);
        if (marginX == 0 && marginY == 0) {
            return image;
        }
        BufferedImage cropped =
                new BufferedImage(width - 2 * marginX, height - 2 * marginY, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < cropped.getHeight(); y++) {
            for (int x = 0; x < cropped.getWidth(); x++) {
                cropped.setRGB(x, y, image.getRGB(x + marginX, y + marginY));
            }
        }
        return cropped;
    }

    /** The atlas file name of a backdrop subfolder's images: {@code backdrop-level-NN}, without the extension. */
    static String backdropAtlas(String folder) {
        return BACKDROP + "-" + folder;
    }

    /**
     * Writes a backdrop part as its folder's own atlas, {@code backdrop-level-NN.atlas}: its pages renamed {@code
     * backdrop-level-NN.png}, {@code backdrop-level-NN2.png}, ...; nothing for a folder without images.
     */
    private static void writeBackdrop(Job part, Path output) throws IOException {
        Path partAtlas = part.output().resolve(BACKDROP + ".atlas");
        if (!Files.exists(partAtlas)) {
            return;
        }
        String name = part.output().getFileName().toString();
        StringBuilder atlas = new StringBuilder();
        int pages = 0;
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
                String page = name + (pages == 1 ? "" : String.valueOf(pages)) + ".png";
                Files.move(part.output().resolve(content), output.resolve(page));
                atlas.append(page).append(line, content.length(), line.length());
            } else {
                atlas.append(line);
            }
            at = end;
        }
        Files.writeString(output.resolve(name + ".atlas"), atlas);
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
