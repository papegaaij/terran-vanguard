package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;

class CampaignRouteTest {
    private final Content content = ContentLoader.fromClasspath();

    @Test
    void aNewGameBriefsThenLaunchesLevel01FromTheHangar() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);

        var step =
                assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));

        assertEquals(1, step.script().mission());
        assertEquals(
                Optional.of("act-1-first-contact/level-01-break-at-dawn"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel01TheBriefingOfLevel02Comes() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        campaign.complete(CampaignTest.won("A", 80, 1000), 60);

        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(
                Optional.of("act-1-first-contact/level-02-shipyard-burning"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel02TheBriefingOfLevel03Comes() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        campaign.complete(CampaignTest.won("A", 80, 1000), 60);

        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(Optional.of("act-1-first-contact/level-03-spore-drift"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel03TheBriefingOfLevel04Comes() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        campaign.complete(CampaignTest.won("A", 80, 1000), 60);

        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(
                Optional.of("act-1-first-contact/level-04-tranquility-run"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel04TheBriefingOfLevel05Comes() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 4; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(Optional.of("act-1-first-contact/level-05-crater-nest"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel05TheBriefingOfLevel06Comes() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 5; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(Optional.of("act-1-first-contact/level-06-farside"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel06TheBriefingOfLevel07Comes() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 6; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(
                Optional.of("act-1-first-contact/level-07-brood-carrier"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel07TheActOutroComesThenTheAct2IntroAndTheBriefingOfLevel08() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 7; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        var outro = assertInstanceOf(CampaignRoute.Step.Outro.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals("act-1-first-contact", outro.script().actDirectory());
        assertEquals(7, outro.script().last());
        assertEquals(8, outro.script().nextLevel());
        assertEquals(4, outro.script().pages().size());
        // After the outro: Act 2's title card and act briefing, then Level 08's pages, then its hangar.
        var briefing =
                assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(8, briefing.script().mission());
        assertEquals("HOMEFRONT", briefing.script().titleCard().orElseThrow().name());
        assertEquals(Optional.of("act-2-homefront/level-08-neon-skyline"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel08ComesLevel09sBriefingAndLaunch() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 8; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        // M5 part C: Level 09 has its data: its own briefing pages (no act intro), then the launch.
        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals(9, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(Optional.of("act-2-homefront/level-09-arcology-fall"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel09ComesLevel10sBriefingAndLaunch() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 9; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        // M5 part D: Level 10 has its data: its own briefing pages (no act intro), then the launch.
        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals(10, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(
                Optional.of("act-2-homefront/level-10-evacuation-corridor"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel10ComesLevel11sBriefingAndLaunch() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 10; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        // M5 part E: Level 11 has its data: its own briefing pages (no act intro), then the launch.
        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals(11, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(Optional.of("act-2-homefront/level-11-atlantic-convoy"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel11ComesLevel12sBriefingAndLaunch() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 11; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        // M5 part F: Level 12 has its data: its own briefing pages (no act intro), then the launch.
        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals(12, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(Optional.of("act-2-homefront/level-12-storm-front"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel12ComesLevel13sBriefingAndLaunch() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 12; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        // M5 part G: Level 13 has its data: its own briefing pages (no act intro), then the launch.
        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals(13, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(Optional.of("act-2-homefront/level-13-polar-relay"), CampaignRoute.launch(content, campaign));
    }

    @Test
    void afterLevel13ComesLevel14sBriefingAndLaunch() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 13; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        // M5 part H: Level 14 has its data: its own briefing pages (no act intro), then the launch.
        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals(14, briefing.script().mission());
        assertTrue(briefing.script().titleCard().isEmpty());
        assertEquals(2, briefing.script().pages().size());
        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals(Optional.of("act-2-homefront/level-14-siege-spire"), CampaignRoute.launch(content, campaign));
    }

    /**
     * M5 part H (H6 = a, H9 = a): Level 14 ends Act 2 as Level 07 ended Act 1: its outro's four pages; M7 part B:
     * then Act 3's title card and intro pages and Level 15's briefing, the hangar and Level 15 (the release's
     * end no longer follows Act 2's outro: the interim state until part H moves it to Level 21).
     */
    @Test
    void afterLevel14TheActOutroComesThenTheActThreeIntroAndLevel15() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 14; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        assertTrue(CampaignRoute.actEnd(content, campaign).isPresent(), "Level 14 ends Act 2");
        var outro = assertInstanceOf(CampaignRoute.Step.Outro.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals("act-2-homefront", outro.script().actDirectory());
        assertEquals(14, outro.script().last());
        assertEquals(15, outro.script().nextLevel());
        assertEquals(4, outro.script().pages().size());
        assertFalse(CampaignRoute.releaseEnd(content, campaign), "Level 15 is built: Act 2's outro ends nothing");
        var briefing =
                assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(content, campaign));
        assertEquals("act-3-red-dust", briefing.script().actDirectory());
        assertEquals("RED DUST", briefing.script().titleCard().orElseThrow().name());
        assertEquals(4 + 2, briefing.script().pages().size(), "the act's four intro pages, then Level 15's two");
        assertTrue(campaign.actIntroShown(3), "the intro is recorded as shown");
        assertEquals(Optional.of("act-3-red-dust/level-15-olympus-descent"), CampaignRoute.launch(content, campaign));
    }

    /**
     * M7 part B (L.6): a new game's intro briefing records Act 1's intro as shown; Continue then opens Level 01's own
     * briefing without the act intro (2026-10-18: every mission starts with its briefing).
     */
    @Test
    void aNewGameRecordsActOnesIntroAndContinueOpensTheMissionBriefing() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.onContinue(content, campaign));

        CampaignRoute.beforeNextLevel(content, campaign);

        assertTrue(
                campaign.storyFlags().contains("act-intro:1"),
                campaign.storyFlags().toString());
        assertMissionBriefing(1, CampaignRoute.onContinue(content, campaign));
    }

    /**
     * M7 part B (L.6 of round 36): a save at the Level 15 hangar from the M6 release (format version 3, Acts 1–2
     * only) plays the Act 3 intro and Level 15's briefing once on Continue, then the hangar; the next Continue opens
     * the hangar.
     */
    @Test
    void aReleaseSaveAtTheLevel15HangarPlaysTheActThreeIntroOnceOnContinue() throws SaveException {
        Campaign campaign = Campaign.load(CampaignTest.RULES, SaveFormat.read(version3At(15)));
        assertTrue(campaign.actIntroShown(1) && campaign.actIntroShown(2) && !campaign.actIntroShown(3));

        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.onContinue(content, campaign));

        assertEquals("act-3-red-dust", briefing.script().actDirectory());
        assertTrue(briefing.script().titleCard().isPresent());
        assertEquals(15, briefing.script().mission());
        assertTrue(campaign.actIntroShown(3));
        Campaign reloaded =
                Campaign.load(CampaignTest.RULES, SaveFormat.read(SaveFormat.write(campaign.save(Instant.EPOCH))));
        assertEquals(
                2,
                assertMissionBriefing(15, CampaignRoute.onContinue(content, reloaded))
                        .pages()
                        .size());
    }

    /**
     * M7 part B (L.6): a release save at the Level 08 hangar has seen Act 2's intro: Continue opens Level 08's own
     * briefing, without the act intro (2026-10-18).
     */
    @Test
    void aReleaseSaveAtTheLevel08HangarOpensTheMissionBriefing() throws SaveException {
        Campaign campaign = Campaign.load(CampaignTest.RULES, SaveFormat.read(version3At(8)));

        assertMissionBriefing(8, CampaignRoute.onContinue(content, campaign));
        assertFalse(campaign.actIntroShown(3));
    }

    /** 2026-10-18: a save inside an act opens the next level's briefing on Continue whatever its flags. */
    @Test
    void aSaveInsideAnActOpensTheMissionBriefingOnContinue() {
        Campaign campaign = DebugFit.startAt(CampaignTest.RULES, Difficulty.MEDIUM, 16);

        assertMissionBriefing(16, CampaignRoute.onContinue(content, campaign));
    }

    /** 2026-10-18: a replay opens the level's own briefing, without the act intro. */
    @Test
    void aReplayOpensTheMissionBriefing() {
        var full = Briefings.before(content, 8).orElseThrow();
        var replay = assertMissionBriefing(8, CampaignRoute.beforeReplay(content, 8));
        int intro = content.actOf(8).orElseThrow().getValue().briefing().size();

        assertTrue(intro > 0);
        assertEquals(full.pages().subList(intro, full.pages().size()), replay.pages());
        assertEquals(full.objectives(), replay.objectives());
        assertMissionBriefing(9, CampaignRoute.beforeReplay(content, 9));
    }

    /** A Continue's or replay's step is the level's own briefing: no title card, no act intro. */
    private static BriefingScript assertMissionBriefing(int level, CampaignRoute.Step step) {
        var briefing = assertInstanceOf(CampaignRoute.Step.Briefing.class, step);
        assertTrue(briefing.script().titleCard().isEmpty(), "no act intro");
        assertEquals(level, briefing.script().mission());
        return briefing.script();
    }

    /** A format version 3 save (the M6 release) whose next level is {@code level}. */
    static String version3At(int level) {
        Campaign campaign = DebugFit.startAt(CampaignTest.RULES, Difficulty.MEDIUM, level);
        ObjectNode tree =
                (ObjectNode) JsonMapper.builder().build().readTree(SaveFormat.write(campaign.save(Instant.EPOCH)));
        tree.put("version", 3);
        tree.remove("bays");
        ((ObjectNode) tree.get("escort")).remove(java.util.List.of("flies", "bulwark")); // M8 part A: version 6's
        tree.putArray("storyFlags");
        return tree.toString();
    }

    @Test
    void aLevelInsideTheActEndsNoAct() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 6; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }

        assertEquals(Optional.empty(), CampaignRoute.actEnd(content, campaign));
        assertInstanceOf(CampaignRoute.Step.Briefing.class, CampaignRoute.afterLevel(content, campaign), "Level 07's");
    }

    @Test
    void aReplayOfTheActsLastLevelHasNoOutro() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < 7; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }
        Campaign replay =
                Campaign.replay(CampaignTest.RULES, SaveSlots.Slot.AUTOSAVE, campaign.save(java.time.Instant.EPOCH), 7);
        replay.complete(CampaignTest.won("A+", 80, 1000), 60);

        assertEquals(Optional.empty(), CampaignRoute.actEnd(content, replay));
        assertFalse(CampaignRoute.afterLevel(content, replay) instanceof CampaignRoute.Step.Outro);
        assertFalse(CampaignRoute.releaseEnd(content, campaign), "Level 08 is built: Act 1's outro ends nothing");
    }

    @Test
    void theActEndDebugOptionEndsTheActAfterTheLevelStart() {
        Campaign campaign = DebugFit.startAt(CampaignTest.RULES, Difficulty.MEDIUM, 6);
        campaign.debugActEnd();
        campaign.complete(CampaignTest.won("A", 80, 1000), 60);

        var outro = assertInstanceOf(CampaignRoute.Step.Outro.class, CampaignRoute.afterLevel(content, campaign));
        assertEquals(6, outro.script().last());
        assertEquals(7, outro.script().nextLevel());
        assertFalse(CampaignRoute.releaseEnd(content, campaign), "Level 07 follows: the release has not ended");
    }

    @Test
    void aFailedLevelIsLaunchedAgainFromTheHangar() {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.HARD);
        campaign.fail();
        campaign.retry();

        assertEquals(
                Optional.of("act-1-first-contact/level-01-break-at-dawn"), CampaignRoute.launch(content, campaign));
    }
}
