package vanguard.game.screen;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.DataFile;
import vanguard.content.Difficulty;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.DebugFit;
import vanguard.sim.LevelResult;
import vanguard.sim.ScoringRules;

/**
 * M11 part A, step A3b: the campaign end for the game's tests on a fixture act set (as content's {@code
 * CampaignEndTest}): the built content with Act 6 as the campaign's last act, its outro its ending pages on the ending's
 * track (M11 part B: Act 6 names no next act since Act 7 exists); and campaigns won through a level.
 */
final class CampaignEndFixture {
    static final String ACT_6 = "campaign/act-6-jovian-storm/data.yaml";
    static final Content BUILT = ContentLoader.fromClasspath();
    static final Content FINAL_ACT_6 =
            ContentLoader.load(files(ACT_6, "\noutro:", "\nending:", "music: act-complete", "music: ending"));

    private CampaignEndFixture() {}

    /** The classpath's data files, {@code path}'s text with each {@code [from, to]} pair of {@code edits} applied. */
    static List<DataFile> files(String path, String... edits) {
        List<DataFile> files = new ArrayList<>();
        for (String file : read(ContentLoader.INDEX)
                .lines()
                .filter(line -> !line.isBlank())
                // M11 part B: the act set before Act 7 (content's CampaignEndTest: the fixture's last act is Act 6).
                .filter(line -> !line.startsWith("campaign/act-7-"))
                .toList()) {
            String text = read("/design/" + file);
            if (file.equals(path)) {
                for (int i = 0; i < edits.length; i += 2) {
                    if (!text.contains(edits[i])) {
                        throw new IllegalStateException(path + " has no " + edits[i]);
                    }
                    text = text.replace(edits[i], edits[i + 1]);
                }
            }
            files.add(new DataFile(file, text));
        }
        return files;
    }

    private static String read(String resource) {
        try (InputStream in =
                Objects.requireNonNull(CampaignEndFixture.class.getResourceAsStream(resource), resource)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A medium campaign of {@code content} that has won every level up to {@code level}. */
    static Campaign wonThrough(Content content, int level) {
        Campaign campaign = DebugFit.startAt(CampaignRules.of(content), Difficulty.MEDIUM, level);
        campaign.complete(won(), 60);
        return campaign;
    }

    static LevelResult won() {
        return new LevelResult(
                80,
                95,
                10,
                1,
                1,
                30,
                2,
                true,
                new LevelResult.Credits(300, 50, 0, 0, 50),
                List.of(),
                10_000,
                new LevelResult.Rating(
                        new LevelResult.Rating.Part(34, 40),
                        new LevelResult.Rating.Part(25, 30),
                        new LevelResult.Rating.Part(15, 15),
                        new LevelResult.Rating.Part(6, 15),
                        new ScoringRules.Grade("A+", 85, 0.3)),
                new ScoringRules.Grade("A", 0, 0),
                80);
    }
}
