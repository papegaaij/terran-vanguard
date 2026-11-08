package vanguard.content.voice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.BriefingPage;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Expression;
import vanguard.content.LevelData;

/**
 * Every spoken line has its rendered file in assets/voice (design/audio/voice, Build and CI): a
 * changed line without a render fails with the list of missing lines. The check is switched on per
 * level ({@link #RENDERED}); the lines of the other levels are only listed. Render with
 * {@code python3 tools/art/voice.py}.
 */
class VoiceFilesTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    /**
     * The levels whose lines must have their files, the acts whose briefings must, and the other
     * sources by their prefix (the specials' calls, the low-armour line, Rook's barks).
     */
    private static final Set<String> RENDERED = Set.of(
            "act-1-first-contact/level-01-break-at-dawn",
            "act-1-first-contact/level-02-shipyard-burning",
            "act-1-first-contact/level-03-spore-drift",
            "act-1-first-contact/level-04-tranquility-run",
            "act-1-first-contact/level-05-crater-nest",
            "act-1-first-contact/level-06-farside",
            "act-1-first-contact/level-07-brood-carrier",
            "act-1-first-contact briefing",
            "act-1-first-contact outro",
            "act-2-homefront/level-08-neon-skyline",
            "act-2-homefront/level-09-arcology-fall",
            "act-2-homefront/level-10-evacuation-corridor",
            "act-2-homefront/level-11-atlantic-convoy",
            "act-2-homefront/level-12-storm-front",
            "act-2-homefront/level-13-polar-relay",
            "act-2-homefront/level-14-siege-spire",
            "act-2-homefront briefing",
            "act-2-homefront outro",
            "act-3-red-dust/level-15-olympus-descent",
            "act-3-red-dust/level-16-valles-canyon-run",
            "act-3-red-dust/level-17-dome-siege",
            "act-3-red-dust/level-18-dust-storm",
            "act-3-red-dust/level-19-foundry-of-tharsis",
            "act-3-red-dust/level-20-retreat-from-hellas",
            "act-3-red-dust/level-21-dust-colossus",
            "act-3-red-dust briefing",
            "act-3-red-dust outro",
            "act-4-deep-water/level-22-ice-shelf",
            "act-4-deep-water/level-23-through-the-ice",
            "act-4-deep-water/level-24-abyssal-plain",
            "act-4-deep-water/level-25-siren-trench",
            "act-4-deep-water/level-26-blackwater",
            "act-4-deep-water briefing",
            "act-5-the-belt/level-29-betrayal-at-ceres",
            "act-5-the-belt/level-30-minefield",
            "act-5-the-belt/level-31-mining-rig-escort",
            "act-5-the-belt/level-32-deep-core",
            "act-5-the-belt/level-33-leech-field",
            "act-5-the-belt/level-34-salvage-run",
            "act-5-the-belt/level-35-iron-sovereign",
            "act-5-the-belt briefing",
            "act-5-the-belt outro",
            "act-6-jovian-storm/level-36-cloud-divers",
            "act-6-jovian-storm/level-37-fall-of-aurelia",
            "act-6-jovian-storm/level-38-lightning-belt",
            "act-6-jovian-storm/level-39-io-flyby",
            "act-6-jovian-storm/level-40-honour-guard",
            "act-6-jovian-storm/level-41-callisto-fortress",
            "act-6-jovian-storm/level-42-ascendant",
            "act-6-jovian-storm briefing",
            "act-6-jovian-storm outro",
            "act-7-beyond-the-gate/level-43-the-tether-gate",
            "act-7-beyond-the-gate/level-44-crossing",
            "act-7-beyond-the-gate/level-45-hive-reef",
            "act-7-beyond-the-gate/level-46-graveyard-of-the-choir",
            "act-7-beyond-the-gate/level-47-seraph-gauntlet",
            "act-7-beyond-the-gate/level-48-vornes-end",
            "act-7-beyond-the-gate/level-49-echoes",
            "act-7-beyond-the-gate/level-50-choir-heart",
            "act-7-beyond-the-gate briefing",
            "act-7-beyond-the-gate ending",
            "specials",
            "armour",
            VoiceLines.BARKS);

    /**
     * Lines of the rendered sources re-keyed by a respelling (design/audio/voice, Pronunciation), by source, and
     * the files of their old keys, until {@code python3 tools/art/voice.py} renders them; empty again since M7
     * part B's step VOX-B rendered the Act 2 outro's page 3 with "Airees".
     */
    private static final Set<String> RESPELLED = Set.of();

    private static final Set<String> RESPELLED_OLD = Set.of();

    /**
     * Sources of a rendered prefix whose lines are new and wait for their files, until {@code tools/art/voice.py}
     * renders them; empty again since M7 part G's step VOX-G rendered Rook's "Up top!" barks of a reversed section
     * (the rear-wave bark's {@code top_lines}).
     */
    private static final Set<String> AWAITING = Set.of();

    private static boolean rendered(VoiceLines.VoiceLine line) {
        return RENDERED.stream().anyMatch(where -> line.source().startsWith(where))
                && !RESPELLED.contains(line.source())
                && !AWAITING.contains(line.source());
    }

    @Test
    void everyLineOfTheRenderedLevelsHasItsVoiceFile() {
        List<VoiceLines.VoiceLine> lines = VoiceLines.all(CONTENT);
        String missing = lines.stream()
                .filter(VoiceFilesTest::rendered)
                .filter(line -> !Files.isRegularFile(ASSETS.resolve(line.path())))
                .map(line -> line.path() + " (" + line.source() + "): " + line.spoken())
                .collect(Collectors.joining("\n"));
        assertEquals("", missing, "lines without their voice file; run python3 tools/art/voice.py");
        lines.stream()
                .filter(line -> !rendered(line) && !Files.isRegularFile(ASSETS.resolve(line.path())))
                .forEach(line -> System.out.println("not rendered yet: " + line.source() + ": " + line.spoken()));
    }

    @Test
    void everyVoiceFileBelongsToALine() throws IOException {
        Set<String> used = VoiceLines.all(CONTENT).stream()
                .map(VoiceLines.VoiceLine::path)
                .collect(Collectors.toCollection(HashSet::new));
        used.addAll(VoiceLines.stageSounds(CONTENT.voices()));
        Path voice = ASSETS.resolve("voice");
        if (!Files.isDirectory(voice)) {
            return;
        }
        try (Stream<Path> files = Files.walk(voice)) {
            List<String> unused = files.filter(Files::isRegularFile)
                    .map(file -> ASSETS.relativize(file).toString().replace('\\', '/'))
                    .filter(path -> !used.contains(path))
                    .filter(path -> !RESPELLED_OLD.contains(path))
                    .toList();
            assertEquals(List.of(), unused, "voice files no line uses; tools/art/voice.py deletes them");
        }
    }

    /** Every speaker has a voice; only a speaker marked {@code uncast} in the speaker table may go without. */
    @Test
    void everySpeakerOfALevelOrBriefingHasAVoice() {
        for (LevelData level : CONTENT.levels().values()) {
            for (LevelData.RadioCue cue : level.radio()) {
                assertTrue(
                        CONTENT.voices().voiceOf(cue.speaker()).isPresent()
                                || CONTENT.voices().uncast(cue.speaker()),
                        "no voice for " + cue.speaker());
            }
            level.briefing()
                    .pages()
                    .forEach(page -> assertTrue(
                            CONTENT.voices().voiceOf(page.speaker()).isPresent(), "no voice for " + page.speaker()));
        }
    }

    /**
     * The speakers of a rendered level whose casting round is still open: their lines play as text
     * until it closes (none now: Level 08's Civilian was cast in concept round 30, Level 09's Kilo Lead in
     * round 31, Level 10's Lifeline and Lifeline Three in round 32, Level 11's Atlas Control in round 33,
     * Level 13's Nansen in round 35, picked in round 34's review). Each must still be marked uncast in the
     * speaker table;
     * once cast, it leaves this set and its lines need their files.
     */
    private static final Set<String> AUDITIONING = Set.of();

    /**
     * The rendered levels have no uncast speaker left but the ones in an open audition
     * ({@link #AUDITIONING}): Level 07's Lifeboat Seven was cast in round 25, Level 08's Civilian in
     * round 30, Level 09's Kilo Lead in round 31, Level 10's Lifeline and Lifeline Three in round 32,
     * Level 11's Atlas Control in round 33 and Level 13's Nansen in round 35, so their lines have a voice (and their files, above) instead of playing as text.
     */
    @Test
    void everySpeakerOfARenderedLevelIsCast() {
        AUDITIONING.forEach(speaker ->
                assertTrue(CONTENT.voices().uncast(speaker), speaker + " is cast: remove it from AUDITIONING"));
        CONTENT.levels().forEach((id, level) -> {
            if (RENDERED.contains(id)) {
                level.radio()
                        .forEach(cue -> assertTrue(
                                CONTENT.voices().voiceOf(cue.speaker()).isPresent()
                                        || AUDITIONING.contains(cue.speaker()),
                                id + ": " + cue.speaker() + " is not cast"));
            }
        });
        assertEquals(
                "lifeboat-seven",
                CONTENT.voices().voiceOf("Lifeboat Seven").orElse(null),
                "Lifeboat Seven speaks with its own voice");
        assertEquals(
                "civilian",
                CONTENT.voices().voiceOf("Civilian").orElse(null),
                "the Civilian speaks with her own voice");
    }

    /**
     * The acts' briefings and outros are spoken too: every page's speaker has a voice and a line. M11 part H (step
     * VOX-GH): so are the campaign's ending pages (Act 7's, in the outro's place), dry.
     */
    @Test
    void everyPageOfAnActBriefingOrOutroIsALine() {
        for (var act : CONTENT.acts().entrySet()) {
            List<BriefingPage> pages = new ArrayList<>(act.getValue().briefing());
            act.getValue().outro().ifPresent(outro -> pages.addAll(outro.pages()));
            act.getValue().ending().ifPresent(ending -> pages.addAll(ending.pages()));
            act.getValue().ending().ifPresent(ending -> {
                List<VoiceLines.VoiceLine> lines = VoiceLines.all(CONTENT).stream()
                        .filter(line -> line.source().startsWith(act.getKey() + " ending page "))
                        .toList();
                assertEquals(ending.pages().size(), lines.size(), "the ending's pages are lines: " + lines);
                lines.forEach(line -> assertEquals(VoiceLines.Filter.DRY, line.filter(), line.source()));
            });
            for (var page : pages) {
                assertTrue(CONTENT.voices().voiceOf(page.speaker()).isPresent(), "no voice for " + page.speaker());
            }
            act.getValue().outro().ifPresent(outro -> {
                List<String> sources = VoiceLines.all(CONTENT).stream()
                        .map(VoiceLines.VoiceLine::source)
                        .filter(source -> source.startsWith(act.getKey() + " outro page "))
                        .toList();
                assertEquals(outro.pages().size(), sources.size(), "the outro's pages are lines: " + sources);
            });
        }
    }

    @Test
    void stageDirectionsAndMarkupAreNotSpoken() {
        assertEquals("", VoiceLines.spoken("[the Choir sings]"));
        assertEquals("The Kestrel's lifeboat stores.", VoiceLines.spoken("The *Kestrel*'s lifeboat stores."));
        assertEquals("on the hull, burn it off us!", VoiceLines.spoken("on the hull — burn it off us!"));
        assertTrue(VoiceLines.line(
                        CONTENT.voices(),
                        "The Choir",
                        "[the Choir sings]",
                        Expression.NEUTRAL,
                        false,
                        VoiceLines.Filter.DISTORTED,
                        "test")
                .isEmpty());
    }

    /**
     * A radio line that is only a stage direction plays its speaker's stage sound (the Choir sings,
     * round 29), which is in the assets; a spoken line, or a stage direction of a speaker without
     * one, does not. Every Choir cue of the levels finds it through the radio index, as the game does.
     */
    @Test
    void theChoirsStageDirectionPlaysItsStageSound() {
        var voices = CONTENT.voices();
        String sings =
                VoiceLines.stageSound(voices, "The Choir", "[the Choir sings]").orElseThrow();
        assertTrue(sings.matches("voice/choir/voice-choir-sings-r29-[a-d]\\.ogg"), sings);
        assertTrue(VoiceLines.stageDirection("[the Choir sings]"));
        assertTrue(!VoiceLines.stageDirection("…many… we are many…"));
        assertTrue(VoiceLines.stageSound(voices, "The Choir", "…many… we are many…")
                .isEmpty());
        assertTrue(VoiceLines.stageSound(voices, "Rook", "[static]").isEmpty());
        for (String sound : VoiceLines.stageSounds(voices)) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sound)), sound + " is not in the assets");
        }
        var index = VoiceLines.radioIndex(CONTENT);
        int cues = 0;
        for (LevelData level : CONTENT.levels().values()) {
            for (LevelData.RadioCue cue : level.radio()) {
                if (cue.speaker().equals("The Choir") && VoiceLines.stageDirection(cue.line())) {
                    String expression =
                            cue.expression().orElse(Expression.NEUTRAL).slug();
                    assertEquals(
                            sings,
                            VoiceLines.radioVoice(index, voices, cue.speaker(), cue.line(), expression)
                                    .orElse(null));
                    cues++;
                }
            }
        }
        assertTrue(cues >= 5, "Act 1's five Choir cues, found " + cues);
    }

    /** Level 06's perimeter beacon (round 23): its radio lines go through the public-address filter. */
    @Test
    void thePerimeterBeaconSpeaksThroughThePublicAddressFilter() {
        var radio = VoiceLines.line(
                        CONTENT.voices(),
                        "Perimeter beacon",
                        "All residents report to shelter.",
                        Expression.NEUTRAL,
                        false,
                        VoiceLines.Filter.RADIO,
                        "t")
                .orElseThrow();
        assertEquals(VoiceLines.Filter.PA, radio.filter());
        var rook = VoiceLines.line(
                        CONTENT.voices(),
                        "Rook",
                        "All residents report to shelter.",
                        Expression.NEUTRAL,
                        false,
                        VoiceLines.Filter.RADIO,
                        "t")
                .orElseThrow();
        assertEquals(VoiceLines.Filter.RADIO, rook.filter());
    }

    @Test
    void aShoutedLineUsesTheShoutRowAndAnotherKey() {
        var calm = VoiceLines.line(
                        CONTENT.voices(), "Rook", "Both edges!", Expression.FIERCE, false, VoiceLines.Filter.RADIO, "t")
                .orElseThrow();
        var shout = VoiceLines.line(
                        CONTENT.voices(), "Rook", "Both edges!", Expression.FIERCE, true, VoiceLines.Filter.RADIO, "t")
                .orElseThrow();
        assertEquals(1.2, shout.settings().exaggeration(), 1e-9);
        assertTrue(!calm.key().equals(shout.key()));
        assertEquals(12, shout.key().length());
    }

    /**
     * M10 part H (step VOX-H): Vorne's one shout, Level 42's line 5, is round 73 R.4 b's take, not a plain pipeline
     * take: the shout row on his theatrical base, its key named in tools/art/voice.py's {@code TREATMENTS} (his clip
     * lowered 4 semitones, the pinned seed 105 only, the cut and the WORLD lowering), so a changed line or row fails
     * here until the treatment follows its new key. M11 part F (step F2): Level 48's line 5 ("Get out of my head!", the
     * fusion's pain) is his only other shout (the voice README's two exceptions); its treated take is step VOX-EF's.
     */
    @Test
    void vornesShoutIsTheTreatedTake() throws IOException {
        List<VoiceLines.VoiceLine> shouts = VoiceLines.all(CONTENT).stream()
                .filter(line -> line.voice().equals("vorne") && line.shout())
                .toList();
        assertEquals(2, shouts.size(), "Vorne's two shouts (Levels 42 and 48): " + shouts);
        assertTrue(
                shouts.stream()
                        .anyMatch(line -> line.source().startsWith("act-7-beyond-the-gate/level-48-vornes-end radio")
                                && line.spoken().equals("Get out of my head!")),
                "Level 48's shout at the fusion's pain: " + shouts);
        VoiceLines.VoiceLine shout = shouts.stream()
                .filter(line -> line.source().startsWith("act-6-jovian-storm/level-42-ascendant"))
                .findFirst()
                .orElseThrow();
        assertTrue(shout.source().startsWith("act-6-jovian-storm/level-42-ascendant radio"), shout.source());
        assertEquals("No! We had an agreement!", shout.spoken());
        assertEquals(1.3, shout.settings().exaggeration(), 1e-9);
        assertEquals(0.2, shout.settings().cfgWeight(), 1e-9);
        assertEquals(0.8, shout.settings().temperature(), 1e-9);
        assertEquals("90d82c165dc1", shout.key());
        assertEquals(
                Integer.valueOf(105),
                CONTENT.voices().speakers().get("vorne").pins().orElseThrow().get(shout.key()));
        String tool =
                Files.readString(ASSETS.toAbsolutePath().normalize().getParent().resolve("tools/art/voice.py"));
        assertTrue(
                tool.contains("\"" + shout.key() + "\": {\"ref_semitones\": -4.0"),
                "tools/art/voice.py's TREATMENTS names the shout's key");
        // M11 part F (step VOX-EF): Level 48's shout, round 73's treatment on round 84 V.2 b's take (seed 120), in the
        // fused voice with the copies 4 dB under him.
        VoiceLines.VoiceLine fused = shouts.stream()
                .filter(line -> line.source().startsWith("act-7-beyond-the-gate/level-48-vornes-end"))
                .findFirst()
                .orElseThrow();
        assertEquals(VoiceLines.Filter.FUSED_LOST, fused.filter());
        assertEquals(1.3, fused.settings().exaggeration(), 1e-9);
        assertEquals("35b7899ef803", fused.key());
        assertEquals(
                Integer.valueOf(120),
                CONTENT.voices().speakers().get("vorne").pins().orElseThrow().get(fused.key()));
        assertTrue(
                tool.contains("\"" + fused.key() + "\": {\"ref_semitones\": -4.0"),
                "tools/art/voice.py's TREATMENTS names Level 48's shout's key");
    }

    /**
     * M11 part F (step VOX-EF; design/audio/voice, Act 7 voices: Vorne in Act 7, round 84 V.2 b): Vorne's nine Level 48
     * lines are in his fused voice, the copies 8 dB under him before his shout (lines 1-3) and 4 dB from it on (lines
     * 5-9); his last line ends on the dash (cut into static), and his other levels' lines keep the enemy filter.
     */
    @Test
    void vornesLevel48LinesAreInTheFusedVoice() {
        List<VoiceLines.VoiceLine> vorne = VoiceLines.all(CONTENT).stream()
                .filter(line -> line.voice().equals("vorne"))
                .toList();
        List<VoiceLines.VoiceLine> fused = vorne.stream()
                .filter(line -> line.source().startsWith("act-7-beyond-the-gate/level-48-vornes-end radio"))
                .toList();
        assertEquals(8, fused.size(), "Vorne's Level 48 lines (line 4 is the Choir's): " + fused);
        assertEquals(
                List.of(
                        VoiceLines.Filter.FUSED,
                        VoiceLines.Filter.FUSED,
                        VoiceLines.Filter.FUSED,
                        VoiceLines.Filter.FUSED_LOST,
                        VoiceLines.Filter.FUSED_LOST,
                        VoiceLines.Filter.FUSED_LOST,
                        VoiceLines.Filter.FUSED_LOST,
                        VoiceLines.Filter.FUSED_LOST),
                fused.stream().map(VoiceLines.VoiceLine::filter).toList());
        assertTrue(
                fused.getLast().text().endsWith("trying to—"), fused.getLast().text());
        vorne.stream()
                .filter(line -> !fused.contains(line))
                .forEach(line -> assertEquals(VoiceLines.Filter.ENEMY, line.filter(), line.source()));
    }

    /**
     * M11 part H (step VOX-GH; design/audio/voice, Act 7 voices: the Choir in Act 7, round 84 V.1 a): the Choir Heart's
     * five Level 50 lines come over the clean channel, fearful and slower: tools/art/voice.py's {@code TREATMENTS} names
     * each one's key ({@code fearful}: the take at tempo 0.85, the copies' stagger 0-80 ms), the last one's with {@code
     * drop_out} (its copies fading out in turn into one voice), so a changed line fails here until the table follows it.
     */
    @Test
    void theHeartsLinesAreFearfulAndTheLastEndsInOneVoice() throws IOException {
        List<VoiceLines.VoiceLine> heart = VoiceLines.all(CONTENT).stream()
                .filter(line -> line.source().startsWith("act-7-beyond-the-gate/level-50-choir-heart radio"))
                .filter(line -> line.voice().equals("choir"))
                .toList();
        assertEquals(5, heart.size(), "the Heart's five lines: " + heart);
        String tool =
                Files.readString(ASSETS.toAbsolutePath().normalize().getParent().resolve("tools/art/voice.py"));
        for (VoiceLines.VoiceLine line : heart) {
            assertEquals(VoiceLines.Filter.CLEAN, line.filter(), line.source());
            boolean last = line.spoken().contains("it has always been behind us");
            assertTrue(
                    tool.contains(
                            "\"" + line.key() + "\": {\"fearful\": True" + (last ? ", \"drop_out\": True}" : "}")),
                    "tools/art/voice.py's TREATMENTS names the Heart's line " + line.key() + ": " + line.spoken());
        }
        assertEquals(
                1,
                heart.stream()
                        .filter(line -> line.spoken().contains("it has always been behind us"))
                        .count(),
                "the last line, into one voice");
    }

    /**
     * Round 75 V.4 b: the Act 6 intro's page 4 is spoken at round 74's chosen take's row (grim, slower: the speaker's
     * {@code line_shifts}, by the page's key at the grim row) and keyed by it, so the game plays that take.
     */
    @Test
    void aLineShiftMovesOneLineOffItsRowAndKeysItAgain() {
        VoiceLines.VoiceLine page = VoiceLines.all(CONTENT).stream()
                .filter(line -> line.source().equals("act-6-jovian-storm briefing page 4"))
                .findFirst()
                .orElseThrow();
        assertEquals(Expression.GRIM, page.expression());
        assertEquals(0.45, page.settings().exaggeration(), 1e-9);
        assertEquals(0.4, page.settings().cfgWeight(), 1e-9);
        assertEquals(0.7, page.settings().temperature(), 1e-9);
        assertTrue(CONTENT.voices()
                .speakers()
                .get("okafor")
                .lineShifts()
                .orElseThrow()
                .containsKey("0220c4dee15f"));
        assertEquals("7c7dedd95094", page.key());
        var other = VoiceLines.line(
                        CONTENT.voices(), "Okafor", "Hold.", Expression.GRIM, false, VoiceLines.Filter.RADIO, "t")
                .orElseThrow();
        assertEquals(0.5, other.settings().cfgWeight(), 1e-9);
    }

    /**
     * M11 part B (step VOX-B; design/audio/voice, Act 7 voices): a level's {@code radio_noise} step is baked into its
     * friendly radio lines and keys them (Level 43 step 1, Level 44 step 2, Level 45 step 3, Level 46 step 4, Levels 47
     * to 50 step 5; steps VOX-B, VOX-CD, VOX-EF and VOX-GH); the Choir's lines there come over the clean channel (no
     * noise; once a level, twice in Levels 47 to 49, five times in Level 50, the Heart's); Level 50's lines from the near
     * side on (its {@code radio_noise_changes}, K + 30.5 = t 206.5) are at step 0; Vorne's Level 48 lines are in the fused voice; the Act 7 intro's page 3, the
     * Choir intercepted through the gate ({@code clean: true}), is radio filter b at step 1; and a line without noise
     * keeps the key it had before Act 7.
     */
    @Test
    void theRadioNoiseStepIsBakedIntoTheFriendlyLinesAndTheChoirStaysClean() {
        List<VoiceLines.VoiceLine> lines = VoiceLines.all(CONTENT);
        List<String> levels = List.of(
                "level-43-the-tether-gate",
                "level-44-crossing",
                "level-45-hive-reef",
                "level-46-graveyard-of-the-choir",
                "level-47-seraph-gauntlet",
                "level-48-vornes-end",
                "level-49-echoes",
                "level-50-choir-heart");
        List<Integer> choirLines = List.of(1, 1, 1, 1, 2, 2, 2, 5);
        int nearSide = 0;
        for (String level : levels) {
            int levelStep = Math.min(5, levels.indexOf(level) + 1);
            List<VoiceLines.VoiceLine> radio = lines.stream()
                    .filter(line -> line.source().startsWith("act-7-beyond-the-gate/" + level + " "))
                    .filter(line -> !line.source().contains(" briefing page "))
                    .toList();
            assertTrue(radio.size() >= 15, level + ": " + radio.size() + " radio lines");
            for (VoiceLines.VoiceLine line : radio) {
                if (line.voice().equals("choir")) {
                    assertEquals(VoiceLines.Filter.CLEAN, line.filter(), line.source());
                    assertEquals(0, line.noise(), line.source());
                } else if (line.voice().equals("vorne")) {
                    assertTrue(
                            line.filter() == VoiceLines.Filter.FUSED || line.filter() == VoiceLines.Filter.FUSED_LOST,
                            line.source());
                    assertEquals(0, line.noise(), line.source());
                } else {
                    assertEquals(VoiceLines.Filter.RADIO, line.filter(), line.source());
                    boolean quiet = level.equals("level-50-choir-heart")
                            && line.source().matches(".* radio t=[0-9.]+")
                            && Double.parseDouble(line.source().replaceAll(".* radio t=", "")) >= 206.5;
                    nearSide += quiet ? 1 : 0;
                    assertEquals(quiet ? 0 : levelStep, line.noise(), line.source());
                }
            }
            assertEquals(
                    (long) choirLines.get(levels.indexOf(level)),
                    radio.stream().filter(line -> line.voice().equals("choir")).count(),
                    level + ": the Choir's lines");
            lines.stream()
                    .filter(line -> line.source().startsWith("act-7-beyond-the-gate/" + level + " briefing page "))
                    .forEach(page -> {
                        assertEquals(VoiceLines.Filter.DRY, page.filter(), page.source());
                        assertEquals(0, page.noise(), page.source());
                    });
        }
        assertEquals(5, nearSide, "Level 50's closing lines on the near side (K + 32 twice, K + 39.5, K + 51, K + 58)");
        VoiceLines.VoiceLine intercept = lines.stream()
                .filter(line -> line.source().equals("act-7-beyond-the-gate briefing page 3"))
                .findFirst()
                .orElseThrow();
        assertEquals("choir", intercept.voice());
        assertEquals(VoiceLines.Filter.RADIO, intercept.filter());
        assertEquals(VoiceLines.CLEAN_INTERCEPT_NOISE, intercept.noise());
        lines.stream()
                .filter(line -> line.source().matches("act-7-beyond-the-gate briefing page [124]"))
                .forEach(page -> assertEquals(VoiceLines.Filter.DRY, page.filter(), page.source()));
        var plain = VoiceLines.line(
                        CONTENT.voices(), "Okafor", "Hold.", Expression.NEUTRAL, false, VoiceLines.Filter.RADIO, "t")
                .orElseThrow();
        var zero = VoiceLines.line(
                        CONTENT.voices(), "Okafor", "Hold.", Expression.NEUTRAL, false, VoiceLines.Filter.RADIO, 0, "t")
                .orElseThrow();
        var one = VoiceLines.line(
                        CONTENT.voices(), "Okafor", "Hold.", Expression.NEUTRAL, false, VoiceLines.Filter.RADIO, 1, "t")
                .orElseThrow();
        var two = VoiceLines.line(
                        CONTENT.voices(), "Okafor", "Hold.", Expression.NEUTRAL, false, VoiceLines.Filter.RADIO, 2, "t")
                .orElseThrow();
        var dry = VoiceLines.line(
                        CONTENT.voices(), "Okafor", "Hold.", Expression.NEUTRAL, false, VoiceLines.Filter.DRY, 2, "t")
                .orElseThrow();
        assertEquals(plain.key(), zero.key());
        assertTrue(!plain.key().equals(one.key()) && !one.key().equals(two.key()));
        assertEquals(0, dry.noise(), "a dry page carries no noise");
    }
}
