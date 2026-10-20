package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Intel;
import vanguard.content.voice.VoiceLines;
import vanguard.sim.Layer;

/**
 * M7 part B's data (step B2; design/tech/architecture, Data file schemas): the Burrower's {@code burrow} and the
 * Dust Kite's triggered {@code swoop} with their hooks, the {@code buried} layer name, the Act 3 intro, Level 15's
 * keys (the moving sections' eases, {@code launch: drop}, {@code no_surface}, {@code first-surfacing}, the secret's
 * generic portrait), the intel's Burrower field marks and the voice's respellings; loaded strictly and checked.
 * The simulation's mapping of the new keys is step B3's.
 */
class PartBDataTest {
    private static final String KEY = "act-3-red-dust/level-15-olympus-descent";
    private static final Content CONTENT = ContentLoader.fromClasspath();

    private static List<DataFile> files(String path, UnaryOperator<String> change) {
        return DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(path) ? new DataFile(f.path(), change.apply(f.text())) : f)
                .toList();
    }

    private static void assertProblem(String path, String what, String with, String message) {
        Exception e = assertThrows(
                Exception.class,
                () -> ContentLoader.load(files(path, text -> {
                    assertTrue(text.contains(what), path + " has " + what);
                    return text.replace(what, with);
                })));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    @Test
    void theBurrowerBurrowsWithItsWindowsAndRings() {
        EnemyData burrower = CONTENT.enemy("burrower");
        EnemyData.Burrow burrow = burrower.movement().burrow().orElseThrow();

        assertEquals("ground", burrower.layer());
        assertEquals(44, burrower.hp());
        assertEquals(90, burrow.swim());
        assertEquals(new Span(1.0, 1.8), burrow.travel());
        assertEquals(new EnemyData.Spot(new Span(140, 240), 110, 40, 110), burrow.spot());
        assertEquals(List.of(1.0, 2.0, 0.4), List.of(burrow.bulge(), burrow.surfaced(), burrow.dive()));
        assertEquals(3, burrow.surfacings());
        assertEquals(List.of(new EnemyData.Ring(0.4, Optional.empty())), burrow.rings());
        assertEquals(0.8, burrow.stagger());
        assertEquals(2, burrow.maxUp());
        assertEquals(10, burrower.attack("ring").orElseThrow().count().orElseThrow());
        EnemyData.BurrowChange easy = burrower.difficulty()
                .flatMap(EnemyData.Hooks::easy)
                .flatMap(EnemyData.Hook::burrow)
                .orElseThrow();
        EnemyData.BurrowChange hard = burrower.difficulty()
                .flatMap(EnemyData.Hooks::hard)
                .flatMap(EnemyData.Hook::burrow)
                .orElseThrow();
        assertEquals(Optional.of(2.4), easy.surfaced());
        assertEquals(Optional.of(1.8), hard.surfaced());
        assertEquals(
                Optional.of(
                        List.of(new EnemyData.Ring(0.4, Optional.empty()), new EnemyData.Ring(1.4, Optional.of(8)))),
                hard.rings());
    }

    @Test
    void theDustKiteSwoopsWhenItNearsTheShipsRow() {
        EnemyData kite = CONTENT.enemy("dust-kite");
        EnemyData.Swoop swoop = kite.movement().swoop().orElseThrow();

        assertEquals(Optional.of(180.0), swoop.trigger());
        assertEquals(Optional.of(200.0), swoop.topSpeed());
        assertEquals(new EnemyData.Sine(50, 1.6), kite.movement().sine().orElseThrow());
        assertEquals(
                Optional.of(230.0),
                kite.difficulty().flatMap(EnemyData.Hooks::hard).flatMap(EnemyData.Hook::swoopSpeed));
        assertEquals(1, kite.hp());
        assertEquals(4, kite.bounty());
    }

    @Test
    void theBuriedLayerIsALayerNameButNoStatBlocksLayer() {
        assertEquals(Layer.BURIED, Layers.of("buried"));
        assertTrue(CONTENT.level(KEY).threatProfile().layers().contains("buried"));
        String path = "enemies/ground/burrower/data.yaml";
        assertProblem(path, "layer: ground     # while surfaced", "layer: buried     #", "not on buried");
        assertProblem(path, "layer: ground     # while surfaced", "layer: air     #", "surfaces on the ground layer");
        assertProblem(
                "enemies/air/dust-kite/data.yaml",
                "top_speed: 200, trigger: 180}",
                "top_speed: 200}",
                "a swoop speed without a triggered swoop");
    }

    @Test
    void actThreeOpensWithItsTitleCardAndFourIntroPages() {
        ActData act = CONTENT.acts().get("act-3-red-dust");

        assertEquals(new ActData.Levels(15, 21), act.levels());
        assertEquals(new ActData.TitleCard("ACT III", "RED DUST", "Mars · June 2185"), act.titleCard());
        assertEquals(
                List.of("act-3-arrival", "act-3-mars-front", "act-3-colonists", "act-3-drop"),
                act.briefing().stream().map(page -> page.image().orElseThrow()).toList());
        assertEquals(
                List.of("Okafor", "Varga", "Okafor", "Okafor"),
                act.briefing().stream().map(BriefingPage::speaker).toList());
        // M7 part H (step PH2): the act's outro after Level 21, four pages with act-complete under the first, and the
        // act after it named for the release's end (H.7d).
        ActData.Outro outro = act.outro().orElseThrow();
        assertEquals(Optional.of("act-complete"), outro.music());
        assertEquals(
                List.of("Okafor", "Okafor", "Varga", "Okafor"),
                outro.pages().stream().map(BriefingPage::speaker).toList());
        assertEquals(
                List.of("act-3-outro-hellas", "act-3-outro-domes", "act-3-outro-europa", "act-3-outro-aegis"),
                outro.pages().stream().map(page -> page.image().orElseThrow()).toList());
        ActData.NextAct next = act.nextAct().orElseThrow();
        assertEquals("ACT IV - DEEP WATER. EUROPA. COMING SOON.", next.comingSoon());
        assertEquals("ACT IV - DEEP WATER: COMING SOON", next.hangarLine());
    }

    /** The speed changes are eased over 1 s (the moving sections' ramp): each adds half its change × 1 s. */
    @Test
    void level15EasesEachSpeedChangeOverASecond() {
        LevelData level = CONTENT.level(KEY);

        assertEquals(
                List.of(0.0, 1.0, 1.0, 0.0, 1.0),
                level.sections().stream().map(LevelData.Section::easeSeconds).toList());
        assertEquals(190 * 30, level.scrollAt(30), 1e-6);
        assertEquals(190 * 30 + 170 * 35 + 20 * 0.5, level.scrollAt(65), 1e-6);
        assertEquals(190 * 30 + 170 * 35 + 10 + 150 * 50 + 10, level.scrollAt(115), 1e-6);
        assertEquals(19_170 + 150 * 35 + 160 * 40 - 5, level.scrollAt(190), 1e-6);
        // Halfway through the first ease the speed is halfway: the scroll lies between the two speeds' sums.
        double half = level.scrollAt(30.5) - level.scrollAt(30);
        assertTrue(half > 170 * 0.5 && half < 190 * 0.5, "eased: " + half);
    }

    @Test
    void aRampOnAnArenaIsRejected() {
        assertProblem(
                "campaign/act-2-homefront/level-14-siege-spire/data.yaml",
                "    speed: 0\n    arena: true",
                "    speed: 0\n    arena: true\n    ramp: 1",
                "not on an arena");
    }

    @Test
    void level15DropsInAndKeepsTheBurrowersOffTheStation() {
        LevelData level = CONTENT.level(KEY);

        assertTrue(level.dropLaunch());
        assertEquals(5, level.launchSeconds());
        assertEquals(5, level.noSurfaceZones().size());
        assertTrue(level.radio().stream()
                .anyMatch(cue -> cue.event().equals(Optional.of(LevelData.CueEvent.FIRST_SURFACING))));
        assertEquals(
                Optional.of("generic-colonial"),
                level.secrets().getFirst().radio().portrait());
        assertEquals(
                List.of(new LevelData.Music.AmbienceChange(3, "mars-dust", 2)),
                level.music().ambienceChangeList());
        assertProblem("campaign/" + KEY + "/data.yaml", "launch: drop ", "launch: bottom ", "launch: only 'drop'");
    }

    @Test
    void noSurfaceZonesAndFirstSurfacingNeedABurrowingEnemy() {
        String path = "campaign/act-2-homefront/level-14-siege-spire/data.yaml";
        assertProblem(
                path,
                "\nsecrets:\n",
                "\nno_surface: [{t: 20, x: 240, size: [100, 100]}]\nsecrets:\n",
                "no-surface zones in a level without a burrowing enemy");
        assertProblem(
                path,
                "  - {event: secondary-objective, speaker: Okafor,",
                "  - {event: first-surfacing, speaker: Okafor, line: \"x\"}\n"
                        + "  - {event: secondary-objective, speaker: Okafor,",
                "first-surfacing in a level without a burrowing enemy");
    }

    /** The wave strip marks the six Burrower fields (design/campaign Level 15, Threat profile). */
    @Test
    void theIntelMarksTheBurrowerFields() {
        Intel intel = Intel.of(CONTENT, KEY, 3);

        assertEquals(
                List.of(68.0, 80.0, 98.0, 125.0, 153.0, 172.0),
                intel.marks().stream()
                        .filter(mark -> mark.kind() == Intel.Mark.Kind.BURROW_FIELD)
                        .map(Intel.Mark::from)
                        .toList());
        assertTrue(Intel.of(CONTENT, Level14Test.LEVEL, 3).marks().stream()
                .noneMatch(mark -> mark.kind() == Intel.Mark.Kind.BURROW_FIELD));
    }

    /** design/audio/voice, Pronunciation: the respellings change the text as spoken and the key, not the subtitle. */
    @Test
    void theRespellingsAreSpokenButNotShown() {
        VoiceData voices = CONTENT.voices();

        assertEquals("Airees Landing, Airees Control.", voices.respelled("Ares Landing, Ares Control."));
        assertEquals(
                "Vallis Marinairis, the Coprateez rapids and the Lycus scarp",
                voices.respelled("Valles Marineris, the Coprates rapids and the Lycus scarp"));
        assertEquals("Aresian Ares-like", voices.respelled("Aresian Ares-like"), "whole words only");
        assertEquals("Skyaparelli, Burrows, then Eden.", voices.respelled("Schiaparelli, Burroughs, then Eden."));
        VoiceLines.VoiceLine line = VoiceLines.line(
                        voices,
                        "Ares Control",
                        "Aegis flight, Ares Landing.",
                        Expression.NEUTRAL,
                        false,
                        VoiceLines.Filter.RADIO,
                        "test")
                .orElseThrow();
        assertEquals("ares-control", line.voice());
        assertEquals("Aegis flight, Ares Landing.", line.text());
        assertEquals("Aegis flight, Airees Landing.", line.spoken());
        VoiceData plain = new VoiceData(voices.expressions(), voices.speakers());
        assertNotEquals(
                line.key(),
                VoiceLines.line(
                                plain,
                                "Ares Control",
                                "Aegis flight, Ares Landing.",
                                Expression.NEUTRAL,
                                false,
                                VoiceLines.Filter.RADIO,
                                "test")
                        .orElseThrow()
                        .key());
    }
}
