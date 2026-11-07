package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.ActData;
import vanguard.content.Content;
import vanguard.content.ContentException;
import vanguard.content.ContentLoader;
import vanguard.content.DataFile;
import vanguard.content.Difficulty;

/**
 * M11 part A, step A2b (m11a-gaps.md §1 #9, content side; design/ui/debrief, The campaign end; Q12 a): the campaign end
 * on a fixture act set whose last act is Act 6 with the {@code ending} pages in its outro's place and no {@code
 * next_act}: after its last level the ending pages (track 27's {@code ending}) and {@link CampaignRoute#campaignEnd};
 * never in a replay, never after {@code --act-end} on an earlier level, and never in the built content, whose last act
 * names the act after it (a build short of the campaign's last level keeps the release's end). M11 part B, step B2: the
 * fixture leaves Act 7 out (the act set before it was built); the built content's last act is Act 7, whose last level
 * is not built yet: its release ends after Level 44 (M11 part C: after Level 45; M11 part D: after Level 46).
 */
class CampaignEndTest {
    private static final String ACT_6 = "campaign/act-6-jovian-storm/data.yaml";
    private static final String ACT_5 = "campaign/act-5-the-belt/data.yaml";
    private static final Path ROOT = Path.of(System.getProperty("vanguard.designDir"));

    private static final Content BUILT = ContentLoader.fromClasspath();

    /** The design tree's data files, {@code path}'s text with each {@code [from, to]} pair of {@code edits} applied. */
    private static List<DataFile> files(String path, String... edits) {
        try (Stream<Path> files = Files.walk(ROOT)) {
            return files.filter(p -> p.getFileName().toString().equals("data.yaml"))
                    // M11 part B, step B2: the act set before Act 7 (its fixture's last act is Act 6).
                    .filter(p ->
                            !ROOT.relativize(p).toString().replace('\\', '/').startsWith("campaign/act-7-"))
                    .sorted()
                    .map(file -> {
                        try {
                            String relative = ROOT.relativize(file).toString().replace('\\', '/');
                            String text = Files.readString(file);
                            if (relative.equals(path)) {
                                for (int i = 0; i < edits.length; i += 2) {
                                    assertTrue(text.contains(edits[i]), path + " has " + edits[i]);
                                    text = text.replace(edits[i], edits[i + 1]);
                                }
                            }
                            return new DataFile(relative, text);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Act 6 as the campaign's last act: its outro its ending pages on the ending's track, no next act. */
    private static final Content FINAL_ACT_6 =
            ContentLoader.load(files(ACT_6, "\noutro:", "\nending:", "music: act-complete", "music: ending"));

    private static Campaign wonThrough(int level) {
        Campaign campaign = Campaign.start(CampaignTest.RULES, Difficulty.MEDIUM);
        for (int i = 0; i < level; i++) {
            campaign.complete(CampaignTest.won("A", 80, 1000), 60);
        }
        return campaign;
    }

    @Test
    void theLastActsLastLevelEndsTheCampaignWithTheEndingPages() {
        ActData act = FINAL_ACT_6.acts().get("act-6-jovian-storm");
        assertTrue(act.outro().isEmpty() && act.nextAct().isEmpty());
        assertEquals(Optional.of("ending"), act.ending().orElseThrow().music());

        Campaign campaign = wonThrough(41);
        assertFalse(CampaignRoute.campaignEnd(FINAL_ACT_6, campaign), "Level 41 ends nothing");
        campaign.complete(CampaignTest.won("A", 80, 1000), 60);

        assertTrue(CampaignRoute.campaignEnd(FINAL_ACT_6, campaign));
        var ending = assertInstanceOf(CampaignRoute.Step.Outro.class, CampaignRoute.afterLevel(FINAL_ACT_6, campaign));
        assertEquals("act-6-jovian-storm", ending.script().actDirectory());
        assertEquals(42, ending.script().last());
        assertEquals(Optional.of("ending"), ending.script().music());
        assertEquals(act.ending().orElseThrow().pages(), ending.script().pages());
        assertTrue(CampaignRoute.releaseEnd(FINAL_ACT_6, campaign), "no level after it either");
    }

    @Test
    void aReplayOrAnEarlierActEndNeverEndsTheCampaign() {
        Campaign campaign = wonThrough(42);
        SaveGame save = campaign.save(Instant.EPOCH);
        Campaign replay = Campaign.replay(CampaignTest.RULES, SaveSlots.Slot.AUTOSAVE, save, 42);
        replay.complete(CampaignTest.won("A", 80, 1000), 60);
        assertFalse(CampaignRoute.campaignEnd(FINAL_ACT_6, replay), "a replay of the last level ends at its debrief");

        Campaign debug = wonThrough(39);
        debug.debugActEnd();
        debug.complete(CampaignTest.won("A", 80, 1000), 60);
        assertTrue(CampaignRoute.actEnd(FINAL_ACT_6, debug).isPresent(), "--act-end on Level 40");
        assertFalse(CampaignRoute.campaignEnd(FINAL_ACT_6, debug), "only the last act's last level");
    }

    @Test
    void theBuiltContentKeepsTheReleasesEnd() {
        Campaign campaign = wonThrough(42);
        assertFalse(CampaignRoute.campaignEnd(BUILT, campaign), "Act 6 is not the last act");
        assertFalse(CampaignRoute.releaseEnd(BUILT, campaign), "M11 part B: Level 43 is built");
        var outro = assertInstanceOf(CampaignRoute.Step.Outro.class, CampaignRoute.afterLevel(BUILT, campaign));
        assertEquals(Optional.of("act-complete"), outro.script().music(), "Act 6's own outro");

        // M11 part B, step B2: the release ended after Level 44 until Level 45 was built; M11 part C, step C2: after
        // Level 45 until Level 46 was built; M11 part D, step D2: after Level 46 until Level 47 was built; M11 part E,
        // step E2: after Level 47 until Level 48 was built; M11 part F, step F2: after Level 48 until Level 49 is
        // built.
        Campaign through44 = wonThrough(44);
        assertFalse(CampaignRoute.releaseEnd(BUILT, through44), "M11 part C: Level 45 is built");
        Campaign through45 = wonThrough(45);
        assertFalse(CampaignRoute.releaseEnd(BUILT, through45), "M11 part D: Level 46 is built");
        Campaign through46 = wonThrough(46);
        assertFalse(CampaignRoute.releaseEnd(BUILT, through46), "M11 part E: Level 47 is built");
        Campaign through47 = wonThrough(47);
        assertFalse(CampaignRoute.releaseEnd(BUILT, through47), "M11 part F: Level 48 is built");
        Campaign through48 = wonThrough(48);
        assertFalse(CampaignRoute.campaignEnd(BUILT, through48), "Act 7's last level is not built");
        assertTrue(CampaignRoute.releaseEnd(BUILT, through48));
    }

    @Test
    void onlyTheLastActEndsOnTheEndingPagesWithoutANextAct() {
        List<String> earlier = assertThrows(
                        ContentException.class, () -> ContentLoader.load(files(ACT_5, "\noutro:", "\nending:")))
                .problems();
        assertTrue(earlier.stream().anyMatch(p -> p.contains("only the campaign's last act")), earlier.toString());
        List<String> beside = assertThrows(
                        ContentException.class,
                        // M11 part B, step B2: Act 6 names no next act any more; the fixture gives it one beside.
                        () -> ContentLoader.load(
                                files(
                                        ACT_6,
                                        "\noutro:",
                                        "\nnext_act: {act: ACT VII, name: BEYOND THE GATE, setting: THE TETHER GATE}\nending:")))
                .problems();
        assertTrue(beside.stream().anyMatch(p -> p.contains("no outro and no next_act")), beside.toString());
    }
}
