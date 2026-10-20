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
     * M7 part H (H.7d): Level 21 ends Act 3 and the release: its outro's NEXT panel names the release's end and Act 4
     * from Act 3's {@code next_act}; the end card's texts and the hangar's line come from the same data.
     */
    @Test
    void afterAct3sOutroTheReleaseEndsWithAct4ComingSoon() {
        String directory = "act-3-red-dust";
        ActData act = content.acts().get(directory);
        assertEquals(21, act.levels().last(), "Act 3 ends with Level 21");
        assertTrue(content.levelKey(22).isEmpty(), "Level 22 is not built");
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

        assertEquals("THE END OF THIS RELEASE AND THE CREDITS. ACT IV - DEEP WATER IS COMING SOON.", onward.next());
        assertEquals("ENTER TO THE END", onward.hint());
        assertEquals("ACTS I-III COMPLETE", ReleaseTexts.title(content));
        assertEquals("ACT IV - DEEP WATER. EUROPA. COMING SOON.", ReleaseTexts.comingSoon(content));
        assertEquals(
                "CONTINUE IN THE MAIN MENU RETURNS TO THE HANGAR AT THE END OF ACT III.",
                ReleaseTexts.continueLine(content));
        assertEquals(
                "ACT IV - DEEP WATER: COMING SOON", ReleaseTexts.hangarLine(content), "the hangar before Level 22");
    }

    /** M6 part B: the end card, then the name entry only for a top-10 score, then the credits. */
    @Test
    void theReleasesEndShowsTheCardTheNameEntryAndTheCredits() {
        assertEquals(ReleaseEndScreen.Phase.ENTRY, ReleaseEndScreen.next(ReleaseEndScreen.Phase.CARD, true));
        assertEquals(ReleaseEndScreen.Phase.CREDITS, ReleaseEndScreen.next(ReleaseEndScreen.Phase.CARD, false));
        assertEquals(ReleaseEndScreen.Phase.CREDITS, ReleaseEndScreen.next(ReleaseEndScreen.Phase.ENTRY, true));
    }
}
