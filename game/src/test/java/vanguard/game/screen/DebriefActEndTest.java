package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.campaign.ActSummary;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRoute;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.SaveGame;
import vanguard.sim.LevelResult;
import vanguard.sim.ScoringRules;

/** The debrief's BOSS TIME row and the act summary's page (design/ui/debrief). */
class DebriefActEndTest {
    @Test
    void theBossTimeShowsTheKillTimeAgainstThePar() {
        assertEquals("1:42 (PAR 2:30)", DebriefScreen.bossTime(new LevelResult.BossTime(150, 102.9)));
        assertEquals("0:59 (PAR 1:00)", DebriefScreen.bossTime(new LevelResult.BossTime(60, 59.99)));
        assertEquals("NOT KILLED (PAR 1:00)", DebriefScreen.bossTime(new LevelResult.BossTime(60, -1)));
    }

    /**
     * The boss row: BOSS TIME with the Boss rush; round 80: an escaping boss's too (Level 42's Ascendant, its time to the
     * escape); none without a boss.
     */
    @Test
    void theBossRowReadsTheBossTimeWithItsRush() {
        LevelResult.BossTime time = new LevelResult.BossTime(150, 131.4);
        LevelResult result = withBoss(time, List.of(new LevelResult.BonusScore("Boss rush", 2_000)));

        DebriefScreen.Row killed = DebriefScreen.bossRow(result).orElseThrow();
        assertEquals("BOSS TIME", killed.label());
        assertEquals("2:11 (PAR 2:30)", killed.middle());
        assertEquals("RUSH +2 000", killed.right(1));

        assertTrue(DebriefScreen.bossRow(withBoss(LevelResult.BossTime.NONE, List.of()))
                .isEmpty());
    }

    private static LevelResult withBoss(LevelResult.BossTime time, List<LevelResult.BonusScore> bonuses) {
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
                bonuses,
                10_000,
                new LevelResult.Rating(
                        new LevelResult.Rating.Part(34, 40),
                        new LevelResult.Rating.Part(25, 30),
                        new LevelResult.Rating.Part(15, 15),
                        new LevelResult.Rating.Part(6, 15),
                        new ScoringRules.Grade("A+", 85, 0.3)),
                new ScoringRules.Grade("A", 0, 0),
                80,
                LevelResult.Escort.NONE,
                time);
    }

    @Test
    void theActSummaryListsTheLevelsTheTotalsAndTheDataCores() {
        ActSummary summary = new ActSummary(
                "ACT I",
                "FIRST CONTACT",
                List.of(
                        new ActSummary.Mission(1, "BREAK AT DAWN", Optional.of("A"), Optional.empty()),
                        new ActSummary.Mission(
                                6, "FARSIDE", Optional.of("A+"), Optional.of(new SaveGame.LevelStats(1051, 212)))),
                212,
                1051,
                List.of(new ActSummary.DataCore("SETTLEMENT LOG", 6, "FARSIDE")));

        List<DebriefScreen.Row> rows = DebriefScreen.summaryRows(summary);

        assertEquals(
                List.of(
                        "MISSIONS",
                        "01 BREAK AT DAWN",
                        "06 FARSIDE",
                        "ACT TOTAL",
                        "1 OF 2 MISSIONS RECORDED",
                        "DATA CORES",
                        "SETTLEMENT LOG"),
                rows.stream().map(DebriefScreen.Row::label).toList());
        assertEquals("--  A", rows.get(1).middle());
        assertEquals("", rows.get(1).right(1));
        assertEquals("212 KILLS  A+", rows.get(2).middle());
        assertEquals("1 051 CR", rows.get(2).right(1));
        assertEquals("1 051 CR", rows.get(3).right(1));
        assertEquals("L06 FARSIDE", rows.get(6).middle());
    }

    /**
     * M5 part H (H6 = a): Level 14 ends Act 2: the summary page {@code ACT II COMPLETE} / {@code HOMEFRONT}
     * lists Levels 08–14, seven rows as Act 1's, the totals and no data cores (none of the act's levels
     * hides one).
     */
    @Test
    void theAct2SummaryListsItsSevenMissionsAndNoDataCores() {
        Content content = ContentLoader.fromClasspath();
        List<ActSummary.Mission> missions = new ArrayList<>();
        for (int number = 8; number <= 14; number++) {
            String name =
                    Content.levelName(content.levelKey(number).orElseThrow()).toUpperCase(Locale.ROOT);
            missions.add(new ActSummary.Mission(
                    number, name, Optional.of("A"), Optional.of(new SaveGame.LevelStats(1000, 100))));
        }
        ActSummary summary = new ActSummary("ACT II", "HOMEFRONT", missions, 700, 7000, List.of());

        List<DebriefScreen.Row> rows = DebriefScreen.summaryRows(summary);

        List<String> labels = rows.stream().map(DebriefScreen.Row::label).toList();
        assertEquals("MISSIONS", labels.getFirst());
        assertEquals("08 NEON SKYLINE", labels.get(1));
        assertEquals("14 SIEGE SPIRE", labels.get(7));
        assertEquals("ACT TOTAL", labels.get(8));
        assertEquals("NONE FOUND", labels.getLast());
        assertEquals(
                labels.size(),
                DebriefScreen.summaryRows(new ActSummary("ACT I", "FIRST CONTACT", missions, 700, 7000, List.of()))
                        .size(),
                "as many rows as Act 1's seven, which fit the page");
        assertEquals("7 000 CR", rows.get(8).right(1));
    }

    /**
     * M7 part H (step HB): Level 21 ends Act 3: the summary page {@code ACT III COMPLETE} / {@code RED DUST} lists Levels
     * 15–21, seven rows as Act 1's and Act 2's, from the campaign as the debrief builds it.
     */
    @Test
    void theAct3SummaryListsLevels15To21() {
        Content content = ContentLoader.fromClasspath();
        String directory = "act-3-red-dust";
        Campaign campaign = Campaign.start(CampaignRules.of(content), Difficulty.MEDIUM);
        ActSummary summary = ActSummary.of(
                content,
                campaign,
                new CampaignRoute.ActEnd(directory, content.acts().get(directory), 21));

        assertEquals("ACT III", summary.act(), "the page's title: ACT III COMPLETE");
        assertEquals("RED DUST", summary.name());
        List<String> labels = DebriefScreen.summaryRows(summary).stream()
                .map(DebriefScreen.Row::label)
                .toList();
        assertEquals("MISSIONS", labels.getFirst());
        assertEquals("15 OLYMPUS DESCENT", labels.get(1));
        assertEquals("21 DUST COLOSSUS", labels.get(7));
        assertEquals("ACT TOTAL", labels.get(8));
    }

    /**
     * M8 part H (design/campaign Level 28, Act end): Level 28 ends Act 4: the summary page {@code ACT IV COMPLETE} /
     * {@code DEEP WATER} lists Levels 22–28, seven rows as the acts before it.
     */
    @Test
    void theAct4SummaryListsLevels22To28() {
        Content content = ContentLoader.fromClasspath();
        String directory = "act-4-deep-water";
        Campaign campaign = Campaign.start(CampaignRules.of(content), Difficulty.MEDIUM);
        ActSummary summary = ActSummary.of(
                content,
                campaign,
                new CampaignRoute.ActEnd(directory, content.acts().get(directory), 28));

        assertEquals("ACT IV", summary.act(), "the page's title: ACT IV COMPLETE");
        assertEquals("DEEP WATER", summary.name());
        List<String> labels = DebriefScreen.summaryRows(summary).stream()
                .map(DebriefScreen.Row::label)
                .toList();
        assertEquals("MISSIONS", labels.getFirst());
        assertEquals("22 ICE SHELF", labels.get(1));
        assertEquals("28 ABYSSAL MAW", labels.get(7));
        assertEquals("ACT TOTAL", labels.get(8));
    }

    /**
     * M10 part H (design/campaign Level 42, Act end; step H4b): Level 42 ends Act 6: the summary page {@code ACT VI
     * COMPLETE} / {@code JOVIAN STORM} lists Levels 36–42.
     */
    @Test
    void theAct6SummaryListsLevels36To42() {
        Content content = ContentLoader.fromClasspath();
        String directory = "act-6-jovian-storm";
        assertEquals(42, content.acts().get(directory).levels().last(), "Level 42 ends Act 6");
        Campaign campaign = Campaign.start(CampaignRules.of(content), Difficulty.MEDIUM);
        ActSummary summary = ActSummary.of(
                content,
                campaign,
                new CampaignRoute.ActEnd(directory, content.acts().get(directory), 42));

        assertEquals("ACT VI", summary.act(), "the page's title: ACT VI COMPLETE");
        assertEquals("JOVIAN STORM", summary.name());
        List<String> labels = DebriefScreen.summaryRows(summary).stream()
                .map(DebriefScreen.Row::label)
                .toList();
        assertEquals("MISSIONS", labels.getFirst());
        assertEquals("36 CLOUD DIVERS", labels.get(1));
        assertEquals("42 ASCENDANT", labels.get(7));
        assertEquals("ACT TOTAL", labels.get(8));
    }

    @Test
    void anActWithoutDataCoresSaysSo() {
        ActSummary summary = new ActSummary("ACT I", "FIRST CONTACT", List.of(), 0, 0, List.of());

        assertEquals("NONE FOUND", DebriefScreen.summaryRows(summary).getLast().label());
    }
}
