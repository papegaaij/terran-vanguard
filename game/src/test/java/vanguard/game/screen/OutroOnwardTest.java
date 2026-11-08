package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.ActData;
import vanguard.content.BriefingPage;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
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

    /**
     * A next level that is not built yet: the release ends (M6 part B). M11 part H: every level is built, so the
     * release-end path is kept for a content set short of Level 50 only: the real content without Level 50's data.
     */
    @Test
    void withoutTheNextLevelsDataTheReleaseEnds() {
        int unbuilt = 50;
        Content upTo49 = withoutLevelsFrom(content, unbuilt);
        assertTrue(upTo49.levelKey(unbuilt).isEmpty(), "the short content set has no Level 50");
        BriefingScreen.Onward onward = BriefingScreen.onward(upTo49, outro(49, unbuilt));

        assertEquals(ReleaseTexts.outroNext(upTo49), onward.next());
        assertEquals("ENTER TO THE END", onward.hint());
    }

    /** The content without the levels from {@code first} on: a release short of the campaign's end. */
    private static Content withoutLevelsFrom(Content content, int first) {
        Map<String, LevelData> levels = new HashMap<>(content.levels());
        levels.keySet().removeIf(key -> Content.levelNumber(key) >= first);
        return new Content(
                content.player(),
                content.ship(),
                content.shields(),
                content.armour(),
                content.generators(),
                content.systems(),
                content.specials(),
                content.weaponRules(),
                content.weapons(),
                content.enemyBasis(),
                content.enemies(),
                content.acts(),
                levels,
                content.economy(),
                content.difficulty(),
                content.scoring(),
                content.retry(),
                content.allies(),
                content.voices(),
                content.wingmen(),
                content.europa(),
                content.jovian(),
                content.vrellSpace(),
                content.databank(),
                content.credits());
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
        // M9 part B (step B3): the release ends after Level 29, inside Act 5 (its texts: ReleaseTexts' open act).
        assertTrue(content.levelKey(29).isPresent(), "Level 29 is built (M9 part B)");
    }

    /**
     * M8 part H (design/campaign Level 28, Act end): Level 28 ends Act 4 in Level 21's order: its outro's four voiced
     * pages with their images, the act-complete fanfare under page 1. M9 part B (step B3): Level 29 is built and Act 4
     * names no next act any more, so the outro leads on to Act 5's title card and the Level 29 briefing; the release
     * ends inside Act 5 (step B4, pending round 62): "ACTS I-IV COMPLETE", "ACT V - THE BELT. LEVEL 31 COMING SOON.",
     * the hangar before the next level "ACT V - THE BELT: LEVEL 31 COMING SOON". M9 part C (step C4): Level 30 is built,
     * so the release ends after it, before Level 31; M9 part D (step D4): Level 31 is built, it ends before Level 32;
     * M9 part E (step E4): Level 32 is built, it ends before Level 33;
     * M9 part F (step F4): Level 33 is built, it ends before Level 34; M9 part G (step G4): Level 34 is built, it ends
     * before Level 35; M9 part H (step H4): Level 35 is built and ends Act 5 ({@link #afterAct5sOutroComeAct6sTitleCardAndTheBriefingOfLevel36}).
     */
    @Test
    void afterAct4sOutroComeAct5sTitleCardAndTheBriefingOfLevel29() {
        String directory = "act-4-deep-water";
        ActData act = content.acts().get(directory);
        assertEquals(28, act.levels().last(), "Act 4 ends with Level 28");
        assertTrue(content.levelKey(29).isPresent(), "Level 29 is built (M9 part B)");
        assertTrue(act.nextAct().isEmpty(), "Act 4 names no next act any more");
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
        assertEquals("ACT V: THE BELT, THEN THE BRIEFING FOR MISSION 29: BETRAYAL AT CERES", onward.next());
        assertEquals("ENTER TO ACT V", onward.hint());
        assertTrue(content.levelKey(35).isPresent(), "Level 35 is built (M9 part H)");
    }

    /**
     * M9 part H (design/campaign Level 35, Act end; step H4): Level 35 ends Act 5 in Level 28's order: the act summary in
     * the debrief, then the outro's four voiced pages with their images (Okafor, Varga, Rook, Okafor), the act-complete
     * fanfare under page 1. M10 part B (step B4): Level 36 is built and Act 5 names no next act any more, so the outro
     * leads on to Act 6's title card and the Level 36 briefing.
     */
    @Test
    void afterAct5sOutroComeAct6sTitleCardAndTheBriefingOfLevel36() {
        String directory = "act-5-the-belt";
        ActData act = content.acts().get(directory);
        assertEquals(35, act.levels().last(), "Act 5 ends with Level 35");
        assertTrue(content.levelKey(36).isPresent(), "Level 36 is built (M10 part B)");
        assertTrue(act.nextAct().isEmpty(), "Act 5 names no next act any more");
        OutroScript outro = Briefings.outro(new CampaignRoute.ActEnd(directory, act, 35), 36)
                .orElseThrow();
        assertEquals("ACT V - THE BELT", outro.act());
        assertEquals(29, outro.first());
        assertEquals(35, outro.last());
        assertEquals(Optional.of("act-complete"), outro.music(), "track 24 under page 1");
        assertEquals(
                List.of("Okafor", "Varga", "Rook", "Okafor"),
                outro.pages().stream().map(BriefingPage::speaker).toList());
        assertEquals(
                List.of("act-5-outro-ceres", "act-5-outro-face", "act-5-outro-ember", "act-5-outro-jupiter"),
                outro.pages().stream().map(page -> page.image().orElseThrow()).toList());
        for (BriefingPage page : outro.pages()) {
            assertTrue(
                    Files.isRegularFile(
                            ASSETS.resolve("ui/briefing/" + page.image().orElseThrow() + ".png")),
                    page.image().orElseThrow());
        }
        BriefingScreen.Onward onward = BriefingScreen.onward(content, outro);
        assertEquals("ACT VI: JOVIAN STORM, THEN THE BRIEFING FOR MISSION 36: CLOUD DIVERS", onward.next());
        assertEquals("ENTER TO ACT VI", onward.hint());
    }

    /**
     * M10 part H (design/campaign Level 42, Act end; step H4b): Level 42 ends Act 6 in Level 35's order: the act summary
     * in the debrief, then the outro's four voiced pages with their images (Okafor grim, Varga grim, Rook, Okafor fierce;
     * Okafor's radio line "Then we follow him." not repeated), the act-complete fanfare under page 1. M11 part B (step
     * B4): Level 43 is built and Act 6 names no next act any more, so the outro leads on to Act 7's title card and the
     * Level 43 briefing; Act 7, the campaign's last act, names none either.
     */
    @Test
    void afterAct6sOutroComeAct7sTitleCardAndTheBriefingOfLevel43() {
        String directory = "act-6-jovian-storm";
        ActData act6 = content.acts().get(directory);
        assertEquals(42, act6.levels().last(), "Act 6 ends with Level 42");
        assertTrue(content.levelKey(43).isPresent(), "Level 43 is built (M11 part B)");
        assertTrue(act6.nextAct().isEmpty(), "Act 6 names no next act any more");
        OutroScript outro = Briefings.outro(new CampaignRoute.ActEnd(directory, act6, 42), 43)
                .orElseThrow();
        assertEquals("ACT VI - JOVIAN STORM", outro.act());
        assertEquals(36, outro.first());
        assertEquals(42, outro.last());
        assertEquals(Optional.of("act-complete"), outro.music(), "track 24 under page 1");
        assertEquals(
                List.of("Okafor", "Varga", "Rook", "Okafor"),
                outro.pages().stream().map(BriefingPage::speaker).toList());
        assertEquals(
                List.of("act-6-outro-callisto", "act-6-outro-silence", "act-6-outro-ember", "act-6-outro-gate"),
                outro.pages().stream().map(page -> page.image().orElseThrow()).toList());
        for (BriefingPage page : outro.pages()) {
            assertTrue(!page.line().contains("Then we follow him"), "the radio's last line is not repeated");
        }
        BriefingScreen.Onward onward = BriefingScreen.onward(content, outro);
        assertEquals("ACT VII: BEYOND THE GATE, THEN THE BRIEFING FOR MISSION 43: THE TETHER GATE", onward.next());
        assertEquals("ENTER TO ACT VII", onward.hint());
        assertEquals(
                List.of(),
                content.acts().entrySet().stream()
                        .filter(entry -> entry.getValue().nextAct().isPresent())
                        .map(java.util.Map.Entry::getKey)
                        .toList(),
                "no act names a next act: Act 7 is the campaign's last");
    }

    /**
     * M9 part B (step B4): a won level that ends no act while the content has no next level (Level 29 until Level 30
     * was built; M9 part C: Level 30 until Level 31 was; M9 part D: Level 31 until Level 32 was; M9 part E: Level 32 until Level 33 was; M9 part F: Level 33 until Level 34 was; M9 part G: Level 34 until Level 35 was) ends the release as an act outro does: the end card instead of the hangar; a briefing or an outro
     * never does, nor the hangar while the next level exists.
     */
    @Test
    void aWonLevelBeforeAnUnbuiltOneEndsTheRelease() {
        CampaignRoute.Step hangar = new CampaignRoute.Step.Hangar();
        assertTrue(HangarScreen.endsRelease(hangar, true));
        assertTrue(!HangarScreen.endsRelease(hangar, false), "the next level is built");
        assertTrue(
                !HangarScreen.endsRelease(new CampaignRoute.Step.Outro(outro(28, 29)), true), "the outro goes first");
        assertEquals("VORNE", vanguard.game.ui.Speaker.plate("Vorne"));
        assertEquals("JOVIAN ASCENDANCY", vanguard.game.ui.Speaker.role("Vorne"));
        assertEquals("HARKER", vanguard.game.ui.Speaker.plate("Harker"));
    }

    /** M6 part B: the end card, then the name entry only for a top-10 score, then the credits. */
    @Test
    void theReleasesEndShowsTheCardTheNameEntryAndTheCredits() {
        assertEquals(ReleaseEndScreen.Phase.ENTRY, ReleaseEndScreen.next(ReleaseEndScreen.Phase.CARD, true));
        assertEquals(ReleaseEndScreen.Phase.CREDITS, ReleaseEndScreen.next(ReleaseEndScreen.Phase.CARD, false));
        assertEquals(ReleaseEndScreen.Phase.CREDITS, ReleaseEndScreen.next(ReleaseEndScreen.Phase.ENTRY, true));
    }
}
