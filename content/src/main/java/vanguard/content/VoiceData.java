package vanguard.content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * design/audio/voice/data.yaml: the speaker table of the spoken radio lines and briefings. The
 * renderer and the game compute a line's key from it (see {@code vanguard.content.voice.VoiceLines}).
 *
 * @param expressions the Chatterbox settings per expression ({@code neutral}, {@code grim},
 *     {@code fierce}) and for shouted lines ({@code shout})
 * @param speakers the voices by slug
 * @param respell M7 part B (design/audio/voice, Pronunciation; round 36 Q.24f): a word or name and its spelling as
 *     spoken ({@code Ares} → {@code Airees}), applied to the text as spoken of every line ({@link #respelled}); the
 *     subtitle keeps the real spelling; none if not given
 * @param respellIn M10 part I, step I3 (the Act 6 listen-through): respellings for the lines of some acts only, and of
 *     some voices if given ({@link Scoped}), over {@link #respell}: an act's new respelling leaves the other acts'
 *     rendered lines as they are; none if not given
 */
public record VoiceData(
        Map<String, Settings> expressions,
        Map<String, Speaker> speakers,
        Optional<Map<String, String>> respell,
        Optional<List<Scoped>> respellIn) {
    /** The row a shouted line uses whatever its expression. */
    public static final String SHOUT = "shout";

    public VoiceData {
        expressions = Map.copyOf(new TreeMap<>(expressions));
        speakers = Map.copyOf(new TreeMap<>(speakers));
        respell = respell.map(Map::copyOf);
        respell.ifPresent(map -> check("respell", map));
        respellIn = respellIn.map(List::copyOf);
        respellIn.ifPresent(list -> list.forEach(scoped -> check("respell_in", scoped.words())));
        for (Expression expression : Expression.values()) {
            Check.that(expressions.containsKey(expression.slug()), "expressions: missing '" + expression.slug() + "'");
        }
        Check.that(expressions.containsKey(SHOUT), "expressions: missing '" + SHOUT + "'");
        for (String slug : speakers.keySet()) {
            Check.that(slug.matches("[a-z0-9]+(-[a-z0-9]+)*"), "speakers: '" + slug + "' is not a kebab-case slug");
        }
    }

    private static void check(String field, Map<String, String> words) {
        words.forEach((word, spoken) -> {
            Check.that(word.matches("\\p{L}+( \\p{L}+)*"), field + ": '" + word + "' is not a word or words");
            Check.that(!spoken.isBlank() && !spoken.equals(word), field + ": '" + word + "' needs another spelling");
        });
    }

    /**
     * M10 part I, step I3: respellings for the lines of {@code acts} (an act's directory: its briefing and outro pages,
     * its levels' briefings, radio and secrets), of {@code voices} only if given.
     *
     * @param acts the acts' directories ({@code act-6-jovian-storm})
     * @param voices the voices (speaker table slugs) whose lines take them; every voice if not given
     * @param words a word or name and its spelling as spoken, as {@link #respell}
     */
    public record Scoped(List<String> acts, Optional<List<String>> voices, Map<String, String> words) {
        public Scoped {
            acts = List.copyOf(acts);
            voices = voices.map(List::copyOf);
            words = Map.copyOf(words);
            Check.that(!acts.isEmpty(), "respell_in: no acts");
            Check.that(!words.isEmpty(), "respell_in: no words");
        }

        /** Whether a line of {@code voice} from {@code source} (the act's directory first) takes these. */
        boolean applies(String source, String voice) {
            boolean act = acts.stream().anyMatch(a -> source.startsWith(a + " ") || source.startsWith(a + "/"));
            return act && voices.map(list -> list.contains(voice)).orElse(true);
        }
    }

    /** Chatterbox's emotion controls. */
    public record Settings(double exaggeration, double cfgWeight, double temperature) {
        public Settings {
            Check.notNegative("exaggeration", exaggeration);
            Check.notNegative("cfg_weight", cfgWeight);
            Check.positive("temperature", temperature);
        }

        /** This row with {@code shift} added. */
        public Settings plus(Shift shift) {
            return new Settings(
                    exaggeration + shift.exaggeration().orElse(0.0),
                    cfgWeight + shift.cfgWeight().orElse(0.0),
                    temperature + shift.temperature().orElse(0.0));
        }
    }

    /** A speaker's offsets to the expression's row (Rook livelier, Okafor drier). */
    public record Shift(Optional<Double> exaggeration, Optional<Double> cfgWeight, Optional<Double> temperature) {}

    /**
     * A voice.
     *
     * @param names the speaker's names as the level data writes them ({@code Dock One}, …)
     * @param ref the reference clip in design/audio/voice/refs, without {@code .wav}
     * @param fixed settings for every line instead of the expression's (the Choir)
     * @param layering {@code choir}: the Choir's layering in post
     * @param pins a line's take seed by the line's key, when the automatic pick sounds wrong
     * @param uncast a speaker whose voice is not cast yet (Level 05's Driver Control): no reference
     *     clip, its lines have no voice file and play as text with the radio blips
     * @param filter {@code pa}: its radio lines go through the public-address filter instead of the
     *     radio filter (Level 06's perimeter beacon, an automated message from loudspeakers)
     * @param stage the sound a radio line of this speaker plays when it is only a stage direction
     *     ({@code [the Choir sings]}), which is not spoken: a file in {@code assets/voice/<slug>/}
     *     (the Choir's sung sting, concept round 29)
     * @param lineShifts round 75 V.4 b: one line's own shift, added to its row, by the line's key at that row (the key
     *     without the shift; the line is keyed again with it, so its file and its pin go by the new key): the Act 6
     *     intro's page 4 at round 74's chosen take's slower grim row; none if not given
     */
    public record Speaker(
            List<String> names,
            Optional<String> ref,
            Optional<Shift> shift,
            Optional<Settings> fixed,
            Optional<String> layering,
            Optional<Map<String, Integer>> pins,
            Optional<Boolean> uncast,
            Optional<String> filter,
            Optional<String> stage,
            Optional<Map<String, Shift>> lineShifts) {
        public Speaker {
            Check.notEmpty("names", names);
            Check.that(
                    ref.isPresent() != uncast.orElse(false),
                    "ref: a cast speaker has its reference clip, an uncast one none");
            ref.ifPresent(clip ->
                    Check.that(clip.matches("ref-[a-z0-9-]+"), "ref: a clip name ref-<speaker>, was '" + clip + "'"));
            Check.that(layering.map("choir"::equals).orElse(true), "layering: only 'choir' is known");
            Check.that(
                    filter.map(name -> PA.equals(name) || ENEMY.equals(name)).orElse(true),
                    "filter: only 'pa' or 'enemy' is known");
            stage.ifPresent(file -> Check.that(
                    file.matches("[a-z0-9]+(-[a-z0-9]+)*\\.ogg"), "stage: an .ogg file name, was '" + file + "'"));
            Check.that(stage.isEmpty() || ref.isPresent(), "stage: only a cast speaker has a stage sound");
            lineShifts = lineShifts.map(Map::copyOf);
            lineShifts.ifPresent(map -> map.keySet()
                    .forEach(key -> Check.that(
                            key.matches("[0-9a-f]{12}"),
                            "line_shifts: a line's key (12 hex digits), was '" + key + "'")));
        }

        /** The public-address filter (an automated loudspeaker: Level 06's beacon). */
        public static final String PA = "pa";

        /**
         * M9 part A (design/audio/voice, Act 5 voices): the enemy channel's filter (Vorne, Harker): the radio filter
         * with a steady static bed; the speaker's lines and pages come over the enemy channel ({@link Channel#ENEMY}).
         */
        public static final String ENEMY = "enemy";

        /** M9 part A: the channel this speaker's lines come over: enemy with {@code filter: enemy}, else friendly. */
        public Channel channel() {
            return filter.filter(ENEMY::equals).isPresent() ? Channel.ENEMY : Channel.FRIENDLY;
        }
    }

    /** Without M7 part B's respellings. */
    public VoiceData(Map<String, Settings> expressions, Map<String, Speaker> speakers) {
        this(expressions, speakers, Optional.empty());
    }

    /** Without M10 part I's scoped respellings. */
    public VoiceData(
            Map<String, Settings> expressions, Map<String, Speaker> speakers, Optional<Map<String, String>> respell) {
        this(expressions, speakers, respell, Optional.empty());
    }

    /**
     * M7 part B: {@code text} as the voice is given it, every respelled word or name ({@link #respell}) replaced by
     * its spelling as spoken: whole words only, case-sensitive, the longest first (so {@code Valles Marineris} wins
     * over a single word in it).
     */
    public String respelled(String text) {
        return respelled(text, respell.orElse(Map.of()));
    }

    /**
     * M10 part I, step I3: {@code text} as {@code voice} is given it in a line from {@code source} (its act's
     * directory first, as {@code vanguard.content.voice.VoiceLines} names it): {@link #respell} with the {@link
     * #respellIn} entries for that act and voice over it.
     */
    public String respelled(String text, String source, String voice) {
        if (respellIn.isEmpty()) {
            return respelled(text);
        }
        Map<String, String> words = new TreeMap<>(respell.orElse(Map.of()));
        for (Scoped scoped : respellIn.get()) {
            if (scoped.applies(source, voice)) {
                words.putAll(scoped.words());
            }
        }
        return respelled(text, words);
    }

    private static String respelled(String text, Map<String, String> respell) {
        if (respell.isEmpty()) {
            return text;
        }
        List<String> words = new ArrayList<>(respell.keySet());
        words.sort(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()));
        StringBuilder pattern = new StringBuilder("(?<![\\p{L}\\p{N}'-])(");
        for (int i = 0; i < words.size(); i++) {
            pattern.append(i == 0 ? "" : "|").append(Pattern.quote(words.get(i)));
        }
        pattern.append(")(?![\\p{L}\\p{N}-])");
        Matcher matcher = Pattern.compile(pattern.toString()).matcher(text);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(out, Matcher.quoteReplacement(respell.get(matcher.group(1))));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    /**
     * M9 part A: the channel the speaker of this name speaks on ({@link Speaker#channel()}); friendly for a name in no
     * speaker's list.
     */
    public Channel channelOf(String name) {
        return speakers.values().stream()
                .filter(speaker -> speaker.names().contains(name))
                .findFirst()
                .map(Speaker::channel)
                .orElse(Channel.FRIENDLY);
    }

    /** The slug of the voice that speaks as {@code name}, if any; none for an uncast speaker. */
    public Optional<String> voiceOf(String name) {
        return speakers.entrySet().stream()
                .filter(entry -> entry.getValue().names().contains(name))
                .filter(entry -> entry.getValue().ref().isPresent())
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /** Whether {@code name} is a speaker marked {@code uncast}: its lines have no voice yet. */
    public boolean uncast(String name) {
        return speakers.values().stream()
                .anyMatch(speaker ->
                        speaker.names().contains(name) && speaker.uncast().orElse(false));
    }

    /** The settings a line of {@code voice} is spoken with. */
    public Settings settings(String voice, Expression expression, boolean shout) {
        Speaker speaker = speakers.get(voice);
        if (speaker.fixed().isPresent()) {
            return speaker.fixed().get();
        }
        Settings row = expressions.get(shout ? SHOUT : expression.slug());
        return speaker.shift().map(row::plus).orElse(row);
    }
}
