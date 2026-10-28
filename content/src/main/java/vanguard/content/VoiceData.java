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
 */
public record VoiceData(
        Map<String, Settings> expressions, Map<String, Speaker> speakers, Optional<Map<String, String>> respell) {
    /** The row a shouted line uses whatever its expression. */
    public static final String SHOUT = "shout";

    public VoiceData {
        expressions = Map.copyOf(new TreeMap<>(expressions));
        speakers = Map.copyOf(new TreeMap<>(speakers));
        respell = respell.map(Map::copyOf);
        respell.ifPresent(map -> map.forEach((word, spoken) -> {
            Check.that(word.matches("\\p{L}+( \\p{L}+)*"), "respell: '" + word + "' is not a word or words");
            Check.that(!spoken.isBlank() && !spoken.equals(word), "respell: '" + word + "' needs another spelling");
        }));
        for (Expression expression : Expression.values()) {
            Check.that(expressions.containsKey(expression.slug()), "expressions: missing '" + expression.slug() + "'");
        }
        Check.that(expressions.containsKey(SHOUT), "expressions: missing '" + SHOUT + "'");
        for (String slug : speakers.keySet()) {
            Check.that(slug.matches("[a-z0-9]+(-[a-z0-9]+)*"), "speakers: '" + slug + "' is not a kebab-case slug");
        }
    }

    /** Chatterbox's emotion controls. */
    public record Settings(double exaggeration, double cfgWeight, double temperature) {
        public Settings {
            Check.notNegative("exaggeration", exaggeration);
            Check.notNegative("cfg_weight", cfgWeight);
            Check.positive("temperature", temperature);
        }

        Settings plus(Shift shift) {
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
            Optional<String> stage) {
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

    /**
     * M7 part B: {@code text} as the voice is given it, every respelled word or name ({@link #respell}) replaced by
     * its spelling as spoken: whole words only, case-sensitive, the longest first (so {@code Valles Marineris} wins
     * over a single word in it).
     */
    public String respelled(String text) {
        if (respell.isEmpty() || respell.get().isEmpty()) {
            return text;
        }
        List<String> words = new ArrayList<>(respell.get().keySet());
        words.sort(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()));
        StringBuilder pattern = new StringBuilder("(?<![\\p{L}\\p{N}'-])(");
        for (int i = 0; i < words.size(); i++) {
            pattern.append(i == 0 ? "" : "|").append(Pattern.quote(words.get(i)));
        }
        pattern.append(")(?![\\p{L}\\p{N}-])");
        Matcher matcher = Pattern.compile(pattern.toString()).matcher(text);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(
                    out, Matcher.quoteReplacement(respell.get().get(matcher.group(1))));
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
