package vanguard.content.voice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import vanguard.content.ActData;
import vanguard.content.AlliesData;
import vanguard.content.BriefingPage;
import vanguard.content.Channel;
import vanguard.content.Content;
import vanguard.content.Expression;
import vanguard.content.LevelData;
import vanguard.content.SpecialsData;
import vanguard.content.VoiceData;
import vanguard.content.WingmenData;

/**
 * The spoken lines (design/audio/voice, Offline pipeline): every radio cue of every level (with its
 * easy and hard variants, one line per convoy unit for {@code {ally}}, one per escort side for
 * {@code {side}} and one per hull section for {@code {section}}), the secrets' lines, the
 * specials' calls, the low-armour line (source {@code armour}), the game over transmission (source {@code
 * retry game-over}), Rook's radio barks (source
 * {@code wingmen barks <trigger>}) and every page of the levels' and acts' briefings and of the acts' outros (spoken
 * dry, as briefings), each with the key its rendered file is named by. The renderer (tools/art/voice.py, through {@code :pipeline:voiceLines}) and
 * the game share this list, so the game finds a line's file by the same key.
 */
public final class VoiceLines {
    /** The placeholder in a convoy line that names the unit it is about ("Crawler {ally} is hit!"). */
    public static final String ALLY = "{ally}";

    /**
     * M5 part B: the placeholder in a line that names the escort's side, {@code left} or {@code right}
     * ("Lancer, I'm on your {side}"; design/player/wingmen, Scripted lines about him).
     */
    public static final String SIDE = "{side}";

    /** The words {@link #SIDE} becomes: one voiced line each. */
    public static final List<String> SIDES = List.of("left", "right");

    /**
     * M9 part D: the placeholder in a hull escort's line that names the section it is about ("We're hit in the
     * {section}!"; Level 31's mining tug, design/allies/mining-tug): one voiced line per section name.
     */
    public static final String SECTION = "{section}";

    /** The placeholder in a line that names the ground-target group it is about ("{group} is behind you"). */
    public static final String GROUP = "{group}";

    /** The source of Rook's radio barks (design/player/wingmen, Radio barks), followed by the trigger. */
    public static final String BARKS = "wingmen barks";

    /** M7 part G: the suffix of the source of a bark's variants in a reversed section ({@code wingmen barks rear-wave top}). */
    public static final String TOP = "top";

    /** Bumped when the rendering changes in a way that must redo every line. */
    private static final String VERSION = "voice-1";

    private static final String[] NUMBERS = {
        "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"
    };

    private VoiceLines() {}

    /** How a line is post-processed. */
    public enum Filter {
        /** Radio filter b. */
        RADIO,
        /** Radio filter b with more static and dropouts: a damaged channel. */
        DISTORTED,
        /** No radio filter: a briefing page, spoken face to face. */
        DRY,
        /**
         * The public-address filter instead of the radio filter: horn loudspeakers with echoes, for a
         * speaker whose table says {@code filter: pa} (Level 06's perimeter beacon).
         */
        PA,
        /**
         * M9 part B (design/audio/voice, Act 5 voices): the enemy channel's filter, radio filter b's band and saturation
         * with a steady static bed and no crackle, for a speaker whose table says {@code filter: enemy} (Vorne, Harker)
         * and for an intercepted briefing page ({@code channel: enemy}).
         */
        ENEMY;

        public String slug() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * A spoken line.
     *
     * @param voice the speaker table's voice slug ({@code dock})
     * @param speaker the speaker's name in the data ({@code Dock One})
     * @param text the line as the subtitle shows it
     * @param spoken the text the voice says: markup and stage directions removed, M7 part B's respellings applied
     * @param settings the Chatterbox settings it is spoken with
     * @param key the hash its file is named by
     * @param source where it comes from, for the renderer's log
     */
    public record VoiceLine(
            String voice,
            String speaker,
            String text,
            String spoken,
            Expression expression,
            boolean shout,
            Filter filter,
            VoiceData.Settings settings,
            String key,
            String source) {
        /** The file under assets/. */
        public String path() {
            return VoiceLines.path(voice, key);
        }
    }

    /** The asset path of a voice file: {@code voice/<voice>/<key>.ogg}. */
    public static String path(String voice, String key) {
        return "voice/" + voice + "/" + key + ".ogg";
    }

    /** A cue's line with {@link #ALLY} as the convoy unit's number word ({@code unit} from 0). */
    public static String allyLine(String line, int unit) {
        if (unit < 0 || !line.contains(ALLY)) {
            return line;
        }
        return line.replace(ALLY, unit < NUMBERS.length ? NUMBERS[unit] : Integer.toString(unit + 1));
    }

    /** A cue's line with {@link #SIDE} as the escort's side ({@code left} or {@code right}). */
    public static String sideLine(String line, String side) {
        return line.replace(SIDE, side);
    }

    /** M9 part D: a cue's line with {@link #SECTION} as the hull section's name ({@code bow}). */
    public static String sectionLine(String line, String section) {
        return line.replace(SECTION, section);
    }

    /**
     * M9 part D: the names of the sections of a level's hull escort (the ally's {@code sections} in hull order: bow,
     * cargo, engines), what {@link #SECTION} becomes; empty without a hull escort.
     */
    public static List<String> sections(Content content, LevelData level) {
        return level.objectives()
                .escort()
                .filter(escort -> escort.hull().isPresent())
                .map(escort -> content.allies().allies().get(escort.ally()))
                .flatMap(ally -> ally == null ? Optional.empty() : ally.sections())
                .map(list -> list.stream().map(AlliesData.HullSection::name).toList())
                .orElse(List.of());
    }

    /** A cue's line with {@link #GROUP} as the group's name; unchanged without a group. */
    public static String groupLine(String line, String group) {
        return group.isEmpty() ? line : line.replace(GROUP, group);
    }

    /**
     * The text as the voice says it: square-bracketed stage directions left out, the {@code *}
     * of italics removed and a dash read as a comma; empty when nothing is left to say.
     */
    public static String spoken(String text) {
        String spoken = text.replaceAll("\\[[^]]*]", " ")
                .replace("*", "")
                .replaceAll("\\s*[—–]\\s*", ", ")
                .replaceAll("\\s+", " ")
                .strip();
        return spoken.matches("[\\p{Punct}\\s]*") ? "" : spoken;
    }

    /**
     * Whether {@code text} is only a stage direction in square brackets ({@code [the Choir sings]}):
     * nothing in it is spoken.
     */
    public static boolean stageDirection(String text) {
        return text.contains("[") && spoken(text).isEmpty();
    }

    /**
     * The sound a radio line that is only a stage direction plays in place of a voice: the speaker's
     * {@code stage} sound in the speaker table, {@code voice/<voice>/<file>} (the Choir's sung sting
     * for {@code [the Choir sings]}); empty for a spoken line or a speaker without one.
     */
    public static Optional<String> stageSound(VoiceData voices, String speaker, String text) {
        if (!stageDirection(text)) {
            return Optional.empty();
        }
        return voices.voiceOf(speaker)
                .flatMap(voice -> voices.speakers().get(voice).stage().map(file -> "voice/" + voice + "/" + file));
    }

    /** Every speaker's stage sound under assets/: files no rendered line names, kept by the renderer. */
    public static Set<String> stageSounds(VoiceData voices) {
        Set<String> sounds = new TreeSet<>();
        voices.speakers().forEach((voice, speaker) -> speaker.stage()
                .ifPresent(file -> sounds.add("voice/" + voice + "/" + file)));
        return Set.copyOf(sounds);
    }

    /**
     * The voice a radio line as shown plays, its asset path: the rendered file of the line in
     * {@code index} ({@link #radioIndex}), or for a stage direction the speaker's {@link #stageSound};
     * empty when it has neither (the line shows as text).
     */
    public static Optional<String> radioVoice(
            Map<String, VoiceLine> index, VoiceData voices, String speaker, String text, String expression) {
        VoiceLine line = index.get(indexKey(speaker, text, expression));
        return line != null ? Optional.of(line.path()) : stageSound(voices, speaker, text);
    }

    /** The line {@code speaker} says, if the speaker has a voice and the text anything to say. */
    public static Optional<VoiceLine> line(
            VoiceData voices,
            String speaker,
            String text,
            Expression expression,
            boolean shout,
            Filter filter,
            String source) {
        Optional<String> voice = voices.voiceOf(speaker);
        // M7 part B: the respellings (Ares → Airees) are part of the text as spoken, so of the key.
        String spoken = voices.respelled(spoken(text));
        if (voice.isEmpty() || spoken.isEmpty()) {
            return Optional.empty();
        }
        VoiceData.Speaker table = voices.speakers().get(voice.get());
        Filter post = filter == Filter.DRY || filter == Filter.ENEMY
                ? filter
                : table.filter()
                        .map(name -> VoiceData.Speaker.ENEMY.equals(name) ? Filter.ENEMY : Filter.PA)
                        .orElse(filter);
        VoiceData.Settings settings = voices.settings(voice.get(), expression, shout);
        String key = key(String.join(
                "\n",
                VERSION,
                voice.get(),
                table.ref().orElseThrow(),
                format(settings.exaggeration()),
                format(settings.cfgWeight()),
                format(settings.temperature()),
                table.layering().orElse("-"),
                spoken,
                expression.slug(),
                Boolean.toString(shout),
                post.slug()));
        return Optional.of(
                new VoiceLine(voice.get(), speaker, text, spoken, expression, shout, post, settings, key, source));
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    /** The first 12 hex digits of the SHA-256 of {@code text}. */
    static String key(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Every spoken line of the game, each key once, in a stable order. */
    public static List<VoiceLine> all(Content content) {
        Map<String, VoiceLine> lines = new LinkedHashMap<>();
        for (VoiceLine line : every(content)) {
            lines.putIfAbsent(line.key(), line);
        }
        return List.copyOf(lines.values());
    }

    /** Every spoken line, a line shared by several speakers (the four docks) once per speaker. */
    private static List<VoiceLine> every(Content content) {
        Map<String, VoiceLine> lines = new LinkedHashMap<>();
        VoiceData voices = content.voices();
        SpecialsData.Radio call = content.specials().airstrike().radio();
        add(lines, line(voices, call.speaker(), call.line(), Expression.NEUTRAL, false, Filter.RADIO, "specials"));
        // M7 part A: the Orbital Lance's call, Lance Control (cast in round 37, step Q2).
        SpecialsData.Radio lance = content.specials().orbitalLance().radio();
        add(lines, line(voices, lance.speaker(), lance.line(), Expression.NEUTRAL, false, Filter.RADIO, "specials"));
        LevelData.RadioLine lowArmour = content.armour().radio();
        add(
                lines,
                line(
                        voices,
                        lowArmour.speaker(),
                        lowArmour.line(),
                        lowArmour.expression().orElse(Expression.NEUTRAL),
                        false,
                        lowArmour.distorted().orElse(false) ? Filter.DISTORTED : Filter.RADIO,
                        "armour"));
        LevelData.RadioLine gameOver = content.retry().gameOver();
        add(
                lines,
                line(
                        voices,
                        gameOver.speaker(),
                        gameOver.line(),
                        gameOver.expression().orElse(Expression.NEUTRAL),
                        false,
                        gameOver.distorted().orElse(false) ? Filter.DISTORTED : Filter.RADIO,
                        "retry game-over"));
        WingmenData.Barks barks = content.wingmen().barks();
        for (WingmenData.Bark bark : barks.triggers()) {
            for (String text : bark.lines()) {
                add(
                        lines,
                        line(
                                voices,
                                barks.speaker(),
                                text,
                                bark.expression(),
                                bark.shouted(),
                                Filter.RADIO,
                                BARKS + " " + bark.trigger()));
            }
            // M7 part G: the rear wave's variants in a reversed section ("Up top!", Level 20).
            for (String text : bark.topLines().orElse(List.of())) {
                add(
                        lines,
                        line(
                                voices,
                                barks.speaker(),
                                text,
                                bark.expression(),
                                bark.shouted(),
                                Filter.RADIO,
                                BARKS + " " + bark.trigger() + " " + TOP));
            }
        }
        for (Map.Entry<String, ActData> act : new TreeMap<>(content.acts()).entrySet()) {
            briefing(lines, voices, act.getValue().briefing(), act.getKey() + " briefing");
            act.getValue().outro().ifPresent(outro -> briefing(lines, voices, outro.pages(), act.getKey() + " outro"));
        }
        for (Map.Entry<String, LevelData> level : new TreeMap<>(content.levels()).entrySet()) {
            LevelData data = level.getValue();
            String where = level.getKey();
            briefing(lines, voices, data.briefing().pages(), where + " briefing");
            for (VoiceLine line : radio(voices, data, sections(content, data), where)) {
                add(lines, Optional.of(line));
            }
        }
        return List.copyOf(lines.values());
    }

    /** The radio lines of one level (its cues with their variants and convoy units, and its secrets). */
    public static List<VoiceLine> radio(VoiceData voices, LevelData level, String where) {
        return radio(voices, level, List.of(), where);
    }

    /**
     * The radio lines of one level, a {@link #SECTION} line once per name in {@code sections} (M9 part D: the hull
     * escort's sections, {@link #sections}; a line with it is kept as written when there are none).
     */
    public static List<VoiceLine> radio(VoiceData voices, LevelData level, List<String> sections, String where) {
        Map<String, VoiceLine> lines = new LinkedHashMap<>();
        int units = level.objectives().escort().map(LevelData.Escort::units).orElse(0);
        for (LevelData.RadioCue cue : level.radio()) {
            Expression expression = cue.expression().orElse(Expression.NEUTRAL);
            boolean shout = cue.shout().orElse(false);
            Filter filter = cue.distorted().orElse(false) ? Filter.DISTORTED : Filter.RADIO;
            List<String> texts = new ArrayList<>();
            texts.add(cue.line());
            cue.easy().ifPresent(change -> texts.add(change.line()));
            cue.hard().ifPresent(change -> texts.add(change.line()));
            String source = where + " radio "
                    + cue.t()
                            .map(t -> "t=" + t)
                            .orElseGet(() -> cue.event().orElseThrow().toString());
            List<String> sided = new ArrayList<>();
            for (String text : texts) {
                if (text.contains(SIDE)) {
                    SIDES.forEach(side -> sided.add(sideLine(text, side)));
                } else if (text.contains(SECTION) && !sections.isEmpty()) {
                    sections.forEach(section -> sided.add(sectionLine(text, section)));
                } else {
                    sided.add(text);
                }
            }
            for (String text : sided) {
                if (text.contains(GROUP)) {
                    for (String group : level.objectives().groups()) {
                        add(
                                lines,
                                line(voices, cue.speaker(), groupLine(text, group), expression, shout, filter, source));
                    }
                } else if (text.contains(ALLY) && level.convoy().isPresent()) {
                    // M5 part E: a naval convoy's unit is named by its name ("The Mbeki is hit!"); the
                    // escort frigate (it leaves instead of taking a lane) is never hit or lost
                    for (LevelData.ConvoyUnit unit :
                            level.convoy().orElseThrow().units()) {
                        if (unit.lane().isEmpty()) {
                            continue;
                        }
                        add(
                                lines,
                                line(
                                        voices,
                                        cue.speaker(),
                                        text.replace(ALLY, unit.name()),
                                        expression,
                                        shout,
                                        filter,
                                        source));
                    }
                } else if (text.contains(ALLY) && !level.groundAllies().isEmpty()) {
                    // M7 part D: a ground ally is named by its name ("Bradbury is hit!"; Level 17's domes)
                    for (LevelData.GroundAlly ally : level.groundAllies()) {
                        add(
                                lines,
                                line(
                                        voices,
                                        cue.speaker(),
                                        text.replace(ALLY, ally.name()),
                                        expression,
                                        shout,
                                        filter,
                                        source));
                    }
                } else if (text.contains(ALLY)) {
                    for (int unit = 0; unit < Math.max(1, units); unit++) {
                        add(
                                lines,
                                line(voices, cue.speaker(), allyLine(text, unit), expression, shout, filter, source));
                    }
                } else {
                    add(lines, line(voices, cue.speaker(), text, expression, shout, filter, source));
                }
            }
        }
        for (LevelData.Secret secret : level.secrets()) {
            LevelData.RadioLine line = secret.radio();
            Filter filter = line.distorted().orElse(false) ? Filter.DISTORTED : Filter.RADIO;
            add(
                    lines,
                    line(
                            voices,
                            line.speaker(),
                            line.line(),
                            line.expression().orElse(Expression.NEUTRAL),
                            false,
                            filter,
                            where + " secret " + secret.name()));
        }
        return List.copyOf(lines.values());
    }

    private static void briefing(
            Map<String, VoiceLine> lines, VoiceData voices, List<BriefingPage> pages, String source) {
        for (int i = 0; i < pages.size(); i++) {
            BriefingPage page = pages.get(i);
            add(lines, briefingPage(voices, page, source + " page " + (i + 1)));
        }
    }

    /**
     * A briefing page's line, spoken without the radio filter; an intercepted page ({@code channel: enemy}, M9 part
     * B) through the enemy channel's filter.
     */
    public static Optional<VoiceLine> briefingPage(VoiceData voices, BriefingPage page, String source) {
        Filter filter = page.onChannel() == Channel.ENEMY ? Filter.ENEMY : Filter.DRY;
        return line(voices, page.speaker(), page.line(), page.portrait(), false, filter, source);
    }

    /** Adds a line once per speaker and key. */
    private static void add(Map<String, VoiceLine> lines, Optional<VoiceLine> line) {
        line.ifPresent(found -> lines.putIfAbsent(found.speaker() + "\n" + found.key(), found));
    }

    /**
     * The radio lines by speaker, text as shown and expression, for the game to find a cue's voice:
     * the shout flag and the filter come with the line.
     */
    public static Map<String, VoiceLine> radioIndex(Content content) {
        Map<String, VoiceLine> index = new TreeMap<>();
        for (VoiceLine line : every(content)) {
            // M9 part B: an intercepted briefing page has the enemy filter, but it is a page, not a radio line.
            if (line.filter() != Filter.DRY && !line.source().matches(".* (briefing|outro) page \\d+")) {
                index.putIfAbsent(
                        indexKey(line.speaker(), line.text(), line.expression().slug()), line);
            }
        }
        return Map.copyOf(index);
    }

    /** The {@link #radioIndex} key of a line. */
    public static String indexKey(String speaker, String text, String expression) {
        return speaker + "\n" + text + "\n" + expression;
    }
}
