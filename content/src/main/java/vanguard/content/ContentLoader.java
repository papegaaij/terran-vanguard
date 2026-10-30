package vanguard.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.PropertyBindingException;
import tools.jackson.databind.exc.ValueInstantiationException;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Loads the design tree's data files into {@link Content} and validates them: every file parses
 * into its schema (required fields present, no unknown fields, values in range), references
 * between files resolve and levels are consistent. All problems are collected and reported
 * together, each naming its file and field.
 *
 * <p>The build copies {@code design/**}{@code /data.yaml} into the classpath under
 * {@code /design/} with an index, {@value #INDEX} (see content/build.gradle.kts).
 */
public final class ContentLoader {
    /** The list of data file paths on the classpath, relative to {@code /design/}. */
    public static final String INDEX = "/design/data-files.txt";

    private static final Pattern WEAPON = Pattern.compile("player/weapons/([a-z0-9-]+)/data\\.yaml");
    private static final Pattern ENEMY = Pattern.compile("enemies/[a-z-]+/([a-z0-9-]+)/data\\.yaml");
    private static final Pattern ACT = Pattern.compile("campaign/(act-\\d+-[a-z0-9-]+)/data\\.yaml");
    private static final Pattern LEVEL =
            Pattern.compile("campaign/(act-\\d+-[a-z0-9-]+/level-\\d{2}-[a-z0-9-]+)/data\\.yaml");
    private static final Map<String, Class<?>> PARTS = Map.ofEntries(
            Map.entry("player/data.yaml", PlayerData.class),
            Map.entry("player/ship/data.yaml", ShipData.class),
            Map.entry("player/shields/data.yaml", ShieldData.class),
            Map.entry("player/armor/data.yaml", ArmourData.class),
            Map.entry("player/generator/data.yaml", GeneratorData.class),
            Map.entry("player/systems/data.yaml", SystemsData.class),
            Map.entry("player/specials/data.yaml", SpecialsData.class),
            Map.entry("player/weapons/data.yaml", WeaponRulesData.class),
            Map.entry("enemies/data.yaml", EnemyBasisData.class),
            Map.entry("systems/economy/data.yaml", EconomyData.class),
            Map.entry("systems/difficulty/data.yaml", DifficultyData.class),
            Map.entry("systems/scoring/data.yaml", ScoringData.class),
            Map.entry("systems/retry/data.yaml", RetryData.class),
            Map.entry("allies/data.yaml", AlliesData.class),
            Map.entry("audio/voice/data.yaml", VoiceData.class),
            Map.entry("player/wingmen/data.yaml", WingmenData.class),
            Map.entry("world/europa/data.yaml", EuropaData.class));

    private ContentLoader() {}

    /** Loads the data files the build put on the classpath. */
    public static Content fromClasspath() {
        Parsed parsed = parseClasspath();
        parsed.check();
        return parsed.content();
    }

    /**
     * Startup step: parses the data files the build put on the classpath without the validator's checks across files
     * and levels, which take longer than the parsing (the backdrop's screens): the game shows its title screen on the
     * parsed content and runs {@link Parsed#check} on its loading thread meanwhile.
     *
     * @throws ContentException listing every problem found while parsing
     */
    public static Parsed parseClasspath() {
        List<DataFile> files = new ArrayList<>();
        for (String path : read(INDEX).lines().filter(line -> !line.isBlank()).toList()) {
            files.add(new DataFile(path, read("/design/" + path)));
        }
        return parse(files);
    }

    /** Parsed content whose validator checks are still to run ({@link #check}); the content is immutable. */
    public static final class Parsed {
        private final Content content;
        private final Map<Object, String> paths;

        private Parsed(Content content, Map<Object, String> paths) {
            this.content = content;
            this.paths = paths;
        }

        public Content content() {
            return content;
        }

        /**
         * Runs the validator's checks on the content.
         *
         * @throws ContentException listing every problem found
         */
        public void check() {
            List<String> problems = new ContentValidator(content, paths).problems();
            if (!problems.isEmpty()) {
                throw new ContentException(problems);
            }
        }
    }

    private static String read(String resource) {
        try (InputStream in = ContentLoader.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("missing resource " + resource + " (built by :content:designData)");
            }
            try (var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n", "", "\n"));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Parses and validates the given files.
     *
     * @throws ContentException listing every problem found
     */
    public static Content load(List<DataFile> files) {
        Parsed parsed = parse(files);
        parsed.check();
        return parsed.content();
    }

    /** Parses the given files, without the validator's checks. */
    private static Parsed parse(List<DataFile> files) {
        YAMLMapper mapper = DataMapper.create();
        List<String> problems = new ArrayList<>();
        Map<String, Object> parts = new TreeMap<>();
        Map<String, WeaponData> weapons = new TreeMap<>();
        Map<String, EnemyData> enemies = new TreeMap<>();
        Map<String, ActData> acts = new TreeMap<>();
        Map<String, LevelData> levels = new TreeMap<>();
        Map<Object, String> paths = new IdentityHashMap<>();
        // Startup step: the files parse side by side (the mapper is thread-safe); their results are kept in file order.
        List<Read> reads =
                files.parallelStream().map(file -> read(mapper, file)).toList();
        for (int i = 0; i < files.size(); i++) {
            DataFile file = files.get(i);
            Read read = reads.get(i);
            Class<?> part = PARTS.get(file.path());
            Matcher weapon = WEAPON.matcher(file.path());
            Matcher enemy = ENEMY.matcher(file.path());
            Matcher act = ACT.matcher(file.path());
            Matcher level = LEVEL.matcher(file.path());
            if (part != null) {
                parse(read, file, part, problems, paths).ifPresent(data -> parts.put(file.path(), data));
            } else if (weapon.matches()) {
                parse(read, file, WeaponData.class, problems, paths)
                        .ifPresent(data -> weapons.put(weapon.group(1), data));
            } else if (enemy.matches()) {
                parse(read, file, EnemyData.class, problems, paths)
                        .ifPresent(data -> enemies.put(enemy.group(1), data));
            } else if (act.matches()) {
                parse(read, file, ActData.class, problems, paths).ifPresent(data -> acts.put(act.group(1), data));
            } else if (level.matches()) {
                parse(read, file, LevelData.class, problems, paths).ifPresent(data -> levels.put(level.group(1), data));
            } else {
                problems.add("design/" + file.path() + ": no schema for a data file at this path");
            }
        }
        for (String path : PARTS.keySet()) {
            if (!parts.containsKey(path)
                    && files.stream().noneMatch(f -> f.path().equals(path))) {
                problems.add("design/" + path + ": missing");
            }
        }
        if (!problems.isEmpty()) {
            throw new ContentException(problems);
        }
        // M9 part H: a boss's attacks written `from` a ground unit take the unit's attack (RingRules.resolve).
        Map<String, EnemyData> written = Map.copyOf(enemies);
        for (Map.Entry<String, EnemyData> entry : enemies.entrySet()) {
            EnemyData data = entry.getValue();
            EnemyData resolved = RingRules.resolve(paths.get(data), data, written, problems);
            if (resolved != data) {
                paths.put(resolved, paths.get(data));
                entry.setValue(resolved);
            }
        }
        if (!problems.isEmpty()) {
            throw new ContentException(problems);
        }
        Content content = new Content(
                part(parts, PlayerData.class),
                part(parts, ShipData.class),
                part(parts, ShieldData.class),
                part(parts, ArmourData.class),
                part(parts, GeneratorData.class),
                part(parts, SystemsData.class),
                part(parts, SpecialsData.class),
                part(parts, WeaponRulesData.class),
                weapons,
                part(parts, EnemyBasisData.class),
                enemies,
                acts,
                levels,
                part(parts, EconomyData.class),
                part(parts, DifficultyData.class),
                part(parts, ScoringData.class),
                part(parts, RetryData.class),
                part(parts, AlliesData.class),
                part(parts, VoiceData.class),
                part(parts, WingmenData.class),
                part(parts, EuropaData.class));
        return new Parsed(content, paths);
    }

    private static <T> T part(Map<String, Object> parts, Class<T> type) {
        return parts.values().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElseThrow();
    }

    /** A file read into its schema's type: its data (null for an empty file), or the parse failure. */
    private record Read(Object data, JacksonException error) {}

    /** The schema type of a data file at {@code path}, or null when there is none. */
    private static Class<?> schema(String path) {
        Class<?> part = PARTS.get(path);
        if (part != null) {
            return part;
        } else if (WEAPON.matcher(path).matches()) {
            return WeaponData.class;
        } else if (ENEMY.matcher(path).matches()) {
            return EnemyData.class;
        } else if (ACT.matcher(path).matches()) {
            return ActData.class;
        } else if (LEVEL.matcher(path).matches()) {
            return LevelData.class;
        }
        return null;
    }

    /** Reads one file into its schema's type (any thread); nothing for a path without a schema. */
    private static Read read(YAMLMapper mapper, DataFile file) {
        Class<?> type = schema(file.path());
        if (type == null) {
            return new Read(null, null);
        }
        try {
            return new Read(mapper.readValue(file.text(), type), null);
        } catch (JacksonException e) {
            return new Read(null, e);
        }
    }

    /** One file's result; on success remembers its path for the validator's messages. */
    private static <T> Optional<T> parse(
            Read read, DataFile file, Class<T> type, List<String> problems, Map<Object, String> paths) {
        if (read.error() != null) {
            problems.add(describe(file.path(), read.error()));
            return Optional.empty();
        }
        if (read.data() == null) {
            problems.add("design/" + file.path() + ": empty file");
            return Optional.empty();
        }
        T data = type.cast(read.data());
        paths.put(data, file.path());
        return Optional.of(data);
    }

    /** "design/<file>:<line>: <field path>: <message>" for a parse failure. */
    static String describe(String file, JacksonException e) {
        StringBuilder where = new StringBuilder("design/").append(file);
        if (e.getLocation() != null && e.getLocation().getLineNr() > 0) {
            where.append(':').append(e.getLocation().getLineNr());
        }
        String path = path(e.getPath());
        return where + ": " + (path.isEmpty() ? "" : path + ": ") + message(e);
    }

    private static String path(List<JacksonException.Reference> references) {
        StringBuilder path = new StringBuilder();
        for (JacksonException.Reference reference : references) {
            if (reference.getPropertyName() != null) {
                path.append(path.isEmpty() ? "" : ".").append(reference.getPropertyName());
            } else if (reference.getIndex() >= 0) {
                path.append('[').append(reference.getIndex()).append(']');
            }
        }
        return path.toString();
    }

    private static String message(JacksonException e) {
        if (e instanceof PropertyBindingException unknown) {
            return "unknown field (known: " + sorted(unknown.getKnownPropertyIds()) + ")";
        }
        if (e instanceof ValueInstantiationException && e.getCause() instanceof IllegalArgumentException cause) {
            return cause.getMessage();
        }
        String message = e.getOriginalMessage();
        return message.startsWith("Missing required creator property") ? "required field is missing" : message;
    }

    private static String sorted(Collection<Object> names) {
        return names.stream().map(String::valueOf).sorted().collect(Collectors.joining(", "));
    }
}
