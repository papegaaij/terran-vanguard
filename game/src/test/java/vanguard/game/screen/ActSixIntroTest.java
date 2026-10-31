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
import vanguard.content.Channel;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.LevelData;
import vanguard.game.audio.Tracks;

/**
 * M10 part B (step B4; design/campaign Act 6, Act intro and outro): the Act 6 intro on screen and in the speakers: the
 * title card over the act's still with its lettering, the four pages with their images, page 3 Vorne's invitation
 * intercepted on the enemy channel playing track 14's motif stem alone (the briefing music crossing out and back over
 * 1 s); Level 36's briefing pages with their images.
 */
class ActSixIntroTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String ACT = "act-6-jovian-storm";

    @Test
    void theTitleCardAndTheFourPages() {
        ActData act = CONTENT.acts().get(ACT);
        assertEquals("ACT VI", act.titleCard().act());
        assertEquals("JOVIAN STORM", act.titleCard().name());
        assertTrue(Files.isRegularFile(ASSETS.resolve("ui/" + ACT + "-title.png")), "the lettering");
        assertTrue(Files.isRegularFile(ASSETS.resolve("ui/" + ACT + "-still.png")), "the still");
        List<BriefingPage> pages = act.briefing();
        assertEquals(
                List.of("Okafor", "Okafor", "Vorne", "Okafor"),
                pages.stream().map(BriefingPage::speaker).toList());
        assertEquals(
                List.of("act-6-jupiter", "act-6-the-plan", "act-6-invitation", "act-6-every-channel"),
                pages.stream().map(page -> page.image().orElseThrow()).toList());
        for (BriefingPage page : pages) {
            assertTrue(Files.isRegularFile(
                    ASSETS.resolve("ui/briefing/" + page.image().get() + ".png")));
        }
        BriefingPage invitation = pages.get(2);
        assertEquals(Channel.ENEMY, invitation.onChannel(), "page 3 intercepted");
        assertEquals(Optional.of(BriefingPage.PageMusic.MOTIF), invitation.music());
        for (int p : new int[] {0, 1, 3}) {
            assertEquals(Channel.FRIENDLY, pages.get(p).onChannel());
            assertTrue(pages.get(p).music().isEmpty(), "the briefing music");
        }
    }

    /** Page 3 plays track 14's motif stem alone; the briefing music fades out and back over 1 s. */
    @Test
    void theInvitationPlaysTheMotifStem() {
        assertEquals(Optional.of("eye-of-the-storm"), Tracks.name(14));
        Optional<String> motif = BriefingScreen.interceptMotif(CONTENT, ACT);
        assertEquals(Optional.of("music/eye-of-the-storm-motif.ogg"), motif);
        assertTrue(Files.isRegularFile(ASSETS.resolve(motif.get())));
        assertEquals(1f, BriefingScreen.MOTIF_PAGE_FADE_SECONDS);
        for (String stem : new String[] {Tracks.path(14), Tracks.basePath("eye-of-the-storm")}) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(stem)), stem);
        }
    }

    /** Level 36's two briefing pages with their images. */
    @Test
    void levelThirtySixsBriefingAndTracker() {
        LevelData level = CONTENT.level(CONTENT.levelKey(36).orElseThrow());
        List<BriefingPage> pages = level.briefing().pages();
        assertEquals(
                List.of("level-36-the-dive", "level-36-chimera"),
                pages.stream().map(page -> page.image().orElseThrow()).toList());
        for (BriefingPage page : pages) {
            assertTrue(Files.isRegularFile(
                    ASSETS.resolve("ui/briefing/" + page.image().get() + ".png")));
        }
    }
}
