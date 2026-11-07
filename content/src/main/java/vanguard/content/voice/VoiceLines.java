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

    /**
     * M11 part B (design/audio/voice, Act 7 voices: the Choir in Act 7, line 1): the radio noise step of an intercepted
     * page marked {@code clean: true} (the Act 7 intro's page 3, the Choir heard through the gate before the crossing):
     * radio filter b's static at Level 43's step over the Choir's dress, instead of the enemy channel's static bed.
     */
    public static final int CLEAN_INTERCEPT_NOISE = 1;

    /**
     * M11 part F (step VOX-EF; design/audio/voice, Act 7 voices: Vorne in Act 7): the end of the portrait a radio cue of
     * an enemy-channel speaker names when it is spoken in the fused voice ({@code vorne-fused}, Level 48): {@link
     * Filter#FUSED} up to the speaker's shout in the level, {@link Filter#FUSED_LOST} from it on.
     */
    public static final String FUSED_PORTRAIT = "-fused";

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
        ENEMY,
        /**
         * M11 part B (design/audio/voice, Act 7 voices: the Choir in Act 7, J3): radio filter b's band and saturation
         * without its static, crackle and clicks, for the Choir's radio lines in a level with a radio noise step (its
         * channel close and clean while the friendly channel grows noisier; round 84 V.1 a).
         */
        CLEAN,
        /**
         * M11 part F (step VOX-EF; design/audio/voice, Act 7 voices: Vorne in Act 7, round 84 V.2 b): the fused voice,
         * his take with the Choir's lower copies a breath behind it (three copies, a long hall), 8 dB under him, then
         * the enemy channel's filter: his cues in the fused portrait ({@link #FUSED_PORTRAIT}) before his shout.
         */
        FUSED,
        /**
         * M11 part F (step VOX-EF): the fused voice with the copies 4 dB under him (he is losing himself): his fused
         * cues from his shout on, in the level's order.
         */
        FUSED_LOST;

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
     * @param filter how it is post-processed
     * @param noise M11 part B (design/audio/voice, Act 7 voices: the radio's noise): the radio noise step baked into a
     *     {@link Filter#RADIO} or {@link Filter#DISTORTED} line (its level's {@code radio_noise}, 0-5: filter b's hiss
     *     +2.5 dB and its crackle's density x(1 + n/2) a step); 0 for every other line and before Act 7
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
            int noise,
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
        return line(voices, speaker, text, expression, shout, filter, 0, source);
    }

    /**
     * The line {@code speaker} says with the radio noise step {@code noise} (M11 part B: a level's {@code
     * radio_noise}; it applies to a {@link Filter#RADIO} or {@link Filter#DISTORTED} line only and is part of its key
     * when above 0, so the lines before Act 7 keep their keys).
     */
    public static Optional<VoiceLine> line(
            VoiceData voices,
            String speaker,
            String text,
            Expression expression,
            boolean shout,
            Filter filter,
            int noise,
            String source) {
        Optional<String> voice = voices.voiceOf(speaker);
        // M7 part B: the respellings (Ares → Airees) are part of the text as spoken, so of the key; M10 part I (step
        // I3):
        // an act's own, by the source's act directory.
        String spoken = voice.isPresent()
                ? voices.respelled(spoken(text), source, voice.get())
                : voices.respelled(spoken(text));
        if (voice.isEmpty() || spoken.isEmpty()) {
            return Optional.empty();
        }
        VoiceData.Speaker table = voices.speakers().get(voice.get());
        Filter post = filter == Filter.DRY
                        || filter == Filter.ENEMY
                        || filter == Filter.CLEAN
                        || filter == Filter.FUSED
                        || filter == Filter.FUSED_LOST
                ? filter
                : table.filter()
                        .map(name -> VoiceData.Speaker.ENEMY.equals(name) ? Filter.ENEMY : Filter.PA)
                        .orElse(filter);
        int step = post == Filter.RADIO || post == Filter.DISTORTED ? noise : 0;
        VoiceData.Settings row = voices.settings(voice.get(), expression, shout);
        String base = key(voice.get(), table, row, spoken, expression, shout, post, step);
        // Round 75 V.4 b: a line's own shift (the speaker's line_shifts, by the key at the speaker's row) moves that
        // one
        // line off its row (the Act 6 intro's page 4 at round 74's slower grim take), so it is keyed again.
        Optional<VoiceData.Shift> own = table.lineShifts().map(shifts -> shifts.get(base));
        VoiceData.Settings settings = own.map(row::plus).orElse(row);
        String key = own.isEmpty() ? base : key(voice.get(), table, settings, spoken, expression, shout, post, step);
        return Optional.of(new VoiceLine(
                voice.get(), speaker, text, spoken, expression, shout, post, step, settings, key, source));
    }

    private static String key(
            String voice,
            VoiceData.Speaker table,
            VoiceData.Settings settings,
            String spoken,
            Expression expression,
            boolean shout,
            Filter post,
            int noise) {
        // M11 part B: the noise step joins the key only above 0, so every line before Act 7 keeps its key and file.
        return key(String.join(
                        "\n",
                        VERSION,
                        voice,
                        table.ref().orElseThrow(),
                        format(settings.exaggeration()),
                        format(settings.cfgWeight()),
                        format(settings.temperature()),
                        table.layering().orElse("-"),
                        spoken,
                        expression.slug(),
                        Boolean.toString(shout),
                        post.slug())
                + (noise > 0 ? "\nnoise " + noise : ""));
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
        // M11 part B (design/audio/voice, Act 7 voices): the level's radio noise step, baked into its friendly lines;
        // the Choir's lines ignore it and come over the clean channel instead (J3).
        int noise = level.radioNoiseStep();
        // M11 part F (step VOX-EF): a speaker's fused cues (Vorne's in Level 48), the copies louder from his shout on.
        boolean lost = false;
        for (LevelData.RadioCue cue : level.radio()) {
            Expression expression = cue.expression().orElse(Expression.NEUTRAL);
            boolean shout = cue.shout().orElse(false);
            Filter filter = cue.distorted().orElse(false) ? Filter.DISTORTED : Filter.RADIO;
            if (filter == Filter.RADIO && noise > 0 && choir(voices, cue.speaker())) {
                filter = Filter.CLEAN;
            }
            if (filter == Filter.RADIO && fused(voices, cue)) {
                lost |= shout;
                filter = lost ? Filter.FUSED_LOST : Filter.FUSED;
            }
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
                                line(
                                        voices,
                                        cue.speaker(),
                                        groupLine(text, group),
                                        expression,
                                        shout,
                                        filter,
                                        noise,
                                        source));
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
                                        noise,
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
                                        noise,
                                        source));
                    }
                } else if (text.contains(ALLY)) {
                    for (int unit = 0; unit < Math.max(1, units); unit++) {
                        add(
                                lines,
                                line(
                                        voices,
                                        cue.speaker(),
                                        allyLine(text, unit),
                                        expression,
                                        shout,
                                        filter,
                                        noise,
                                        source));
                    }
                } else {
                    add(lines, line(voices, cue.speaker(), text, expression, shout, filter, noise, source));
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
                            noise,
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
        if (page.onChannel() == Channel.ENEMY && page.cleanIntercept()) {
            // M11 part B: the Act 7 intro's page 3, the Choir through the gate: radio filter b at Level 43's step
            return line(
                    voices,
                    page.speaker(),
                    page.line(),
                    page.portrait(),
                    false,
                    Filter.RADIO,
                    CLEAN_INTERCEPT_NOISE,
                    source);
        }
        Filter filter = page.onChannel() == Channel.ENEMY ? Filter.ENEMY : Filter.DRY;
        return line(voices, page.speaker(), page.line(), page.portrait(), false, filter, source);
    }

    /** M11 part B: whether {@code speaker} speaks with the Choir's layering (the speaker table's {@code layering}). */
    private static boolean choir(VoiceData voices, String speaker) {
        return voices.voiceOf(speaker)
                .flatMap(voice -> voices.speakers().get(voice).layering())
                .filter("choir"::equals)
                .isPresent();
    }

    /**
     * M11 part F (step VOX-EF): whether {@code cue} is spoken in the fused voice: an enemy-channel speaker's cue in a
     * portrait ending in {@link #FUSED_PORTRAIT}.
     */
    private static boolean fused(VoiceData voices, LevelData.RadioCue cue) {
        return cue.portrait()
                        .filter(portrait -> portrait.endsWith(FUSED_PORTRAIT))
                        .isPresent()
                && voices.voiceOf(cue.speaker())
                        .flatMap(voice -> voices.speakers().get(voice).filter())
                        .filter(VoiceData.Speaker.ENEMY::equals)
                        .isPresent();
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
