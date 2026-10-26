package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData.Entry;
import vanguard.content.Tier;
import vanguard.content.campaign.Intel.Field;

class IntelTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL_01 = "act-1-first-contact/level-01-break-at-dawn";
    private static final String LEVEL_03 = "act-1-first-contact/level-03-spore-drift";

    /** Round 44 Q.8 d: the wave strip marks a level's ridge pass from its surfacing to its head off the far edge. */
    @Test
    void theStripMarksTheRidgePassOfLevels20And21() {
        assertEquals(List.of(new Intel.Mark(12, 20, Intel.Mark.Kind.RIDGE)), ridges(20), "Level 20's, t=12 for 8 s");
        assertEquals(
                List.of(new Intel.Mark(64, 72, Intel.Mark.Kind.RIDGE)),
                ridges(21),
                "Level 21's ridge pass, 64–72 s as its threat profile says");
        assertEquals(List.of(), ridges(19), "a level without a ridge marks none");
    }

    private static List<Intel.Mark> ridges(int number) {
        return Intel.of(CONTENT, CONTENT.levelKey(number).orElseThrow(), 3).marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.RIDGE)
                .toList();
    }

    /**
     * M8 part F (design/campaign Level 26, Threat profile at L3): in the dark the strip marks each wave of the scarp's
     * wall nests (seven Sirens at t=61.2-94.8); a level with nests in the light (Level 25) or dark without nests (Level
     * 06) marks none.
     */
    @Test
    void theStripMarksTheNestsInTheDarkOfLevel26() {
        assertEquals(
                List.of(61.2, 66.8, 72.4, 78.0, 83.6, 89.2, 94.8),
                nests(26).stream().map(Intel.Mark::from).toList());
        assertEquals(List.of(), nests(25), "the trench's nests show in its light");
        assertEquals(List.of(), nests(6), "Farside's dark has no wall nests");
    }

    /**
     * M8 part H (design/campaign Level 28, Threat profile at L3: "the timeline strip marks the Choir's song (97.5 s)"):
     * the level's {@code strip: [choir-song]} marks its timed Choir song; the levels whose Choir sings without asking
     * for it (Levels 01 and 21) mark none; an unknown strip mark is refused.
     */
    @Test
    void theStripMarksTheChoirsSongOfLevel28() {
        assertEquals(List.of(new Intel.Mark(97.5, 97.5, Intel.Mark.Kind.CHOIR)), choir(28));
        assertEquals(List.of(), choir(1), "Level 01's song is not on its strip");
        assertEquals(List.of(), choir(21), "nor Level 21's");
        var profile = CONTENT.level(CONTENT.levelKey(28).orElseThrow()).threatProfile();
        assertTrue(profile.stripMarks(vanguard.content.LevelData.ThreatProfile.CHOIR_SONG));
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new vanguard.content.LevelData.ThreatProfile(
                        profile.setting(),
                        profile.layers(),
                        profile.density(),
                        profile.traits(),
                        profile.hazards(),
                        profile.boss(),
                        profile.specials(),
                        profile.objective(),
                        profile.varga(),
                        profile.required(),
                        java.util.Optional.of(List.of("finale"))));
    }

    private static List<Intel.Mark> choir(int number) {
        return Intel.of(CONTENT, CONTENT.levelKey(number).orElseThrow(), 3).marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.CHOIR)
                .toList();
    }

    private static List<Intel.Mark> nests(int number) {
        return Intel.of(CONTENT, CONTENT.levelKey(number).orElseThrow(), 3).marks().stream()
                .filter(mark -> mark.kind() == Intel.Mark.Kind.NEST)
                .toList();
    }

    @Test
    void withoutASensorSuiteLevel01ShowsTheSettingTheLayersAndTheMainDirection() {
        Intel intel = Intel.of(CONTENT, LEVEL_01, 0);

        assertEquals("Earth orbit", intel.profile().setting());
        assertEquals(List.of("air"), intel.profile().layers());
        assertEquals(Entry.FRONT, intel.mainDirection());
        assertTrue(intel.shows(Field.MAIN_DIRECTION));
        assertFalse(intel.shows(Field.DENSITY));
        assertEquals("Our scans are patchy, Lancer. Small, fast, lots of them.", intel.varga());
        assertEquals(List.of(), intel.markedTraits(), "the shop marks traits from sensor L3 on");
    }

    @Test
    void theDirectionsAreTheEnemiesSharesAsInTheThreatProfile() {
        Intel intel = Intel.of(CONTENT, LEVEL_01, 1);

        assertEquals(Map.of(Entry.FRONT, 71, Entry.SIDES, 23, Entry.REAR, 6), intel.directions());
        assertTrue(intel.shows(Field.DENSITY));
        assertEquals(1, intel.profile().density());
    }

    @Test
    void theHigherSensorLevelsAddEnemiesTraitsWavesAndSecrets() {
        Intel intel = Intel.of(CONTENT, LEVEL_01, 3);

        assertEquals(List.of("Skitter", "Needler"), intel.enemies());
        assertEquals(List.of("forward"), intel.markedTraits());
        assertEquals(1, intel.secrets());
        assertEquals(
                "Your cannon is enough; keep it forward. One cache: a crane beacon blinking mid-run. Three hits.",
                intel.varga(),
                "Varga's line for sensor L3");
    }

    @Test
    void aSetPieceIsAnUnknownContactOfItsSizeTierFromSensorL2() {
        Intel l1 = Intel.of(CONTENT, LEVEL_03, 1);
        Intel l2 = Intel.of(CONTENT, LEVEL_03, 2);

        assertEquals(List.of(new Intel.Contact("leviathan", Tier.HUGE)), l2.contacts());
        assertEquals("unknown huge contact", l2.contacts().getFirst().label());
        assertFalse(l1.shows(Field.CONTACTS));
        assertTrue(l2.shows(Field.CONTACTS));
        assertFalse(l2.enemies().contains("Leviathan"), "the intel does not name it");
        assertEquals(List.of(), Intel.of(CONTENT, LEVEL_01, 3).contacts());
    }
}
