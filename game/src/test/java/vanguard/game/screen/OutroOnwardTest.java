package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.ActData;
import vanguard.content.BriefingPage;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.campaign.Briefings;
import vanguard.content.campaign.CampaignRoute;
import vanguard.content.campaign.OutroScript;

/**
 * An act outro's NEXT panel and its last page's hint follow the campaign's route (Level 08's
 * capture, round 30: after Act 1 they named the hangar while Act 2's title card came first).
 */
class OutroOnwardTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private final Content content = ContentLoader.fromClasspath();

    private static OutroScript outro(int last, int nextLevel) {
        return new OutroScript(
                "act-1-first-contact",
                "ACT I - FIRST CONTACT",
                1,
                last,
                nextLevel,
                Optional.empty(),
                List.of(new BriefingPage("Okafor", "The carrier is dead.", Optional.empty(), Optional.empty())));
    }

    @Test
    void afterAct1ComeAct2sTitleCardAndTheBriefingOfLevel08() {
        BriefingScreen.Onward onward = BriefingScreen.onward(content, outro(7, 8));

        assertEquals("ACT II: HOMEFRONT, THEN THE BRIEFING FOR MISSION 08: NEON SKYLINE", onward.next());
        assertEquals("ENTER TO ACT II", onward.hint());
    }

    /** {@code --act-end} on Level 06: Level 07's briefing follows, in the same act. */
    @Test
    void anOutroWithinAnActLeadsToTheNextBriefing() {
        BriefingScreen.Onward onward = BriefingScreen.onward(content, outro(6, 7));

        assertEquals("THE BRIEFING FOR MISSION 07: BROOD CARRIER", onward.next());
        assertEquals("ENTER TO THE BRIEFING", onward.hint());
    }

    /**
     * M5 part H (H6 = a, H7 = a, H9 = a): Level 14 ends Act 2: the act summary in the debrief, then its four
     * voiced outro pages with the act-complete fanfare under the first; M7 part B: then Act 3's title card and
     * the Level 15 briefing (M6 part B's release end came here while Level 15 was not built).
     */
    @Test
    void afterAct2sFourOutroPagesComeAct3sTitleCardAndTheBriefingOfLevel15() {
        String directory = "act-2-homefront";
        ActData act = content.acts().get(directory);
        OutroScript outro = Briefings.outro(new CampaignRoute.ActEnd(directory, act, 14), 15)
                .orElseThrow();

        assertEquals("ACT II - HOMEFRONT", outro.act());
        assertEquals(8, outro.first());
        assertEquals(14, outro.last());
        assertEquals(4, outro.pages().size(), "four pages, one a paragraph (H7 = a)");
        assertEquals(Optional.of("act-complete"), outro.music(), "track 24 under page 1");
        assertTrue(outro.pages().stream().allMatch(page -> page.speaker().equals("Okafor")));
        assertTrue(content.levelKey(15).isPresent(), "Level 15 is built (M7 part B)");
        BriefingScreen.Onward onward = BriefingScreen.onward(content, outro);
        assertEquals("ACT III: RED DUST, THEN THE BRIEFING FOR MISSION 15: OLYMPUS DESCENT", onward.next());
        assertEquals("ENTER TO ACT III", onward.hint());
    }

    /** A next level that is not built yet: the release ends (M6 part B). */
    @Test
    void withoutTheNextLevelsDataTheReleaseEnds() {
        int unbuilt = 50;
        BriefingScreen.Onward onward = BriefingScreen.onward(content, outro(49, unbuilt));

        assertEquals(ReleaseTexts.outroNext(content), onward.next());
        assertEquals("ENTER TO THE END", onward.hint());
    }

    /**
     * M7 part H (H.7d): Level 21 ends Act 3; M8 part B: Level 22 is built, so Act 3's outro leads on to Act 4's title
     * card and the Level 22 briefing (Act 3's {@code next_act} is gone). The interim release ends with Act 4 still
     * open: no act names the act after it yet (part H adds Act 5's with Act 4's outro), so the end texts follow the act
     * with the highest levels, Act 4, with no act named ("MORE"), and the hangar before the first level not built yet
     * shows the release's line (M8 part E: Level 25 is built, so the hangar before Level 26).
     */
    @Test
    void afterAct3sOutroComeAct4sTitleCardAndTheBriefingOfLevel22() {
        String directory = "act-3-red-dust";
        ActData act = content.acts().get(directory);
        assertEquals(21, act.levels().last(), "Act 3 ends with Level 21");
        assertTrue(content.levelKey(22).isPresent(), "Level 22 is built (M8 part B)");
        assertTrue(act.nextAct().isEmpty(), "Act 3 names no next act any more");
        OutroScript outro = Briefings.outro(new CampaignRoute.ActEnd(directory, act, 21), 22)
                .orElseThrow();
        // Step HB: Act 3's outro, Level 14's order: four voiced pages with their images, the fanfare under page 1.
        assertEquals("ACT III - RED DUST", outro.act());
        assertEquals(15, outro.first());
        assertEquals(21, outro.last());
        assertEquals(Optional.of("act-complete"), outro.music(), "track 24 under page 1");
        assertEquals(
                List.of("Okafor", "Okafor", "Varga", "Okafor"),
                outro.pages().stream().map(BriefingPage::speaker).toList());
        assertEquals(
                List.of("act-3-outro-hellas", "act-3-outro-domes", "act-3-outro-europa", "act-3-outro-aegis"),
                outro.pages().stream().map(page -> page.image().orElseThrow()).toList());
        for (BriefingPage page : outro.pages()) {
            assertTrue(
                    Files.isRegularFile(
                            ASSETS.resolve("ui/briefing/" + page.image().orElseThrow() + ".png")),
                    page.image().orElseThrow());
        }
        BriefingScreen.Onward onward = BriefingScreen.onward(content, outro);

        assertEquals("ACT IV: DEEP WATER, THEN THE BRIEFING FOR MISSION 22: ICE SHELF", onward.next());
        assertEquals("ENTER TO ACT IV", onward.hint());
        // M8 part H (step H3): the release ends after Level 28, Act 4 naming Act 5 (its `next_act`).
        assertTrue(content.levelKey(28).isPresent(), "Level 28 is built (M8 part H)");
        assertEquals("ACTS I-IV COMPLETE", ReleaseTexts.title(content));
        assertEquals("ACT V - THE BELT. CERES. COMING SOON.", ReleaseTexts.comingSoon(content));
        assertEquals(
                "CONTINUE IN THE MAIN MENU RETURNS TO THE HANGAR AT THE END OF ACT IV.",
                ReleaseTexts.continueLine(content));
        assertEquals("ACT V - THE BELT: COMING SOON", ReleaseTexts.hangarLine(content), "the hangar before Level 29");
        assertEquals(
                "THE END OF THIS RELEASE AND THE CREDITS. ACT V - THE BELT IS COMING SOON.",
                ReleaseTexts.outroNext(content));
    }

    /**
     * M8 part H (design/campaign Level 28, Act end): Level 28 ends Act 4 in Level 21's order: its outro's four voiced
     * pages with their images, the act-complete fanfare under page 1, then the release's end (Level 29 is not built):
     * the NEXT panel names Act V, the end card reads "ACTS I-IV COMPLETE" and "ACT V - THE BELT. CERES. COMING SOON.",
     * the hangar before Level 29 "ACT V - THE BELT: COMING SOON" (Level 21's "ACT IV - DEEP WATER" line is gone).
     */
    @Test
    void afterAct4sOutroTheReleaseEndsWithActVComingSoon() {
        String directory = "act-4-deep-water";
        ActData act = content.acts().get(directory);
        assertEquals(28, act.levels().last(), "Act 4 ends with Level 28");
        assertTrue(content.levelKey(29).isEmpty(), "Level 29 is not built");
        assertTrue(act.nextAct().isPresent(), "Act 4 names the act after it");
        OutroScript outro = Briefings.outro(new CampaignRoute.ActEnd(directory, act, 28), 29)
                .orElseThrow();
        assertEquals("ACT IV - DEEP WATER", outro.act());
        assertEquals(22, outro.first());
        assertEquals(28, outro.last());
        assertEquals(Optional.of("act-complete"), outro.music(), "track 24 under page 1");
        assertEquals(
                List.of("Okafor", "Varga", "Okafor", "Okafor"),
                outro.pages().stream().map(BriefingPage::speaker).toList());
        assertEquals(
                List.of("act-4-outro-orbit", "act-4-outro-hullcode", "act-4-outro-ready-room", "act-4-outro-aegis"),
                outro.pages().stream().map(page -> page.image().orElseThrow()).toList());
        for (BriefingPage page : outro.pages()) {
            assertTrue(
                    Files.isRegularFile(
                            ASSETS.resolve("ui/briefing/" + page.image().orElseThrow() + ".png")),
                    page.image().orElseThrow());
        }
        BriefingScreen.Onward onward = BriefingScreen.onward(content, outro);
        assertEquals(ReleaseTexts.outroNext(content), onward.next());
        assertEquals("THE END OF THIS RELEASE AND THE CREDITS. ACT V - THE BELT IS COMING SOON.", onward.next());
        assertEquals("ENTER TO THE END", onward.hint());
        assertEquals("ACTS I-IV COMPLETE", ReleaseTexts.title(content));
        assertEquals("ACT V - THE BELT. CERES. COMING SOON.", ReleaseTexts.comingSoon(content));
        assertEquals("ACT V - THE BELT: COMING SOON", ReleaseTexts.hangarLine(content));
        assertTrue(
                content.acts().values().stream().filter(other -> other != act).allMatch(other -> other.nextAct()
                        .isEmpty()),
                "only Act 4 names a next act");
    }

    /** M6 part B: the end card, then the name entry only for a top-10 score, then the credits. */
    @Test
    void theReleasesEndShowsTheCardTheNameEntryAndTheCredits() {
        assertEquals(ReleaseEndScreen.Phase.ENTRY, ReleaseEndScreen.next(ReleaseEndScreen.Phase.CARD, true));
        assertEquals(ReleaseEndScreen.Phase.CREDITS, ReleaseEndScreen.next(ReleaseEndScreen.Phase.CARD, false));
        assertEquals(ReleaseEndScreen.Phase.CREDITS, ReleaseEndScreen.next(ReleaseEndScreen.Phase.ENTRY, true));
    }
}
