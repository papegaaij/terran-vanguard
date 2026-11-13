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
import vanguard.game.ui.Fonts;
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
        assertEquals("BOSS RUSH", killed.label());
        assertEquals("2:11 (PAR 2:30)", killed.middle());
        assertEquals("+2 000", killed.right(1));

        DebriefScreen.Row late = DebriefScreen.bossRow(withBoss(new LevelResult.BossTime(60, 75), List.of()))
                .orElseThrow();
        assertEquals("BOSS TIME", late.label(), "no rush bonus: the time alone");
        assertEquals("", late.right(1));

        assertTrue(DebriefScreen.bossRow(withBoss(LevelResult.BossTime.NONE, List.of()))
                .isEmpty());
    }

    /**
     * The user's Act 6 playthrough (2026-11-03): "0:46 (PAR 1:06) RUSH +12 000" ran together. The widest boss row, a
     * kill at 9:59 against a par of 9:59 with a 99 999 rush, keeps two characters between the time and the amount, at
     * every step of the amount's count-up; an escaped boss's "NOT KILLED" pays nothing.
     */
    @Test
    void theWidestBossRowKeepsAGap() {
        LevelResult widest =
                withBoss(new LevelResult.BossTime(599, 599), List.of(new LevelResult.BonusScore("Boss rush", 99_999)));
        DebriefScreen.Row row = DebriefScreen.bossRow(widest).orElseThrow();
        assertEquals("9:59 (PAR 9:59)", row.middle());
        assertEquals("+99 999", row.right(1));
        for (double progress = 0; progress <= 1; progress += 0.05) {
            assertTrue(DebriefScreen.gap(row, progress) >= 2 * vanguard.game.ui.Fonts.BODY_CELL, "at " + progress);
        }
        DebriefScreen.Row escaped = DebriefScreen.bossRow(withBoss(new LevelResult.BossTime(599, -1), List.of()))
                .orElseThrow();
        assertEquals("NOT KILLED (PAR 9:59)", escaped.middle());
        assertEquals("", escaped.right(1));
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
    void theActSummaryListsTheLevelsTheTotalsAndTheSecrets() {
        ActSummary summary = new ActSummary(
                "ACT I",
                "FIRST CONTACT",
                List.of(
                        new ActSummary.Mission(1, "BREAK AT DAWN", Optional.of("A"), Optional.empty()),
                        new ActSummary.Mission(
                                6, "FARSIDE", Optional.of("A+"), Optional.of(new SaveGame.LevelStats(1051, 212)))),
                212,
                1051,
                3,
                4,
                List.of(new ActSummary.DataCore("SETTLEMENT LOG", 6, "FARSIDE")));

        List<DebriefScreen.Row> rows = DebriefScreen.summaryRows(summary);

        assertEquals(
                List.of(
                        "MISSIONS",
                        "01 BREAK AT DAWN",
                        "06 FARSIDE",
                        "ACT TOTAL",
                        "1 OF 2 MISSIONS RECORDED",
                        "SECRETS",
                        "SECRETS FOUND",
                        "SETTLEMENT LOG"),
                rows.stream().map(DebriefScreen.Row::label).toList());
        assertEquals("--  A", rows.get(1).middle());
        assertEquals("", rows.get(1).right(1));
        assertEquals("212 KILLS  A+", rows.get(2).middle());
        assertEquals("1 051 CR", rows.get(2).right(1));
        assertEquals("1 051 CR", rows.get(3).right(1));
        assertEquals("3 OF 4", rows.get(6).middle());
        assertEquals("L06 FARSIDE", rows.get(7).middle());
    }

    /**
     * M5 part H (H6 = a): Level 14 ends Act 2: the summary page {@code ACT II COMPLETE} / {@code HOMEFRONT}
     * lists Levels 08–14, seven rows as Act 1's, the totals and the act's secrets found (step PT5; no data core:
     * none of the act's levels hides one).
     */
    @Test
    void theAct2SummaryListsItsSevenMissionsAndItsSecrets() {
        Content content = ContentLoader.fromClasspath();
        List<ActSummary.Mission> missions = new ArrayList<>();
        for (int number = 8; number <= 14; number++) {
            String name =
                    Content.levelName(content.levelKey(number).orElseThrow()).toUpperCase(Locale.ROOT);
            missions.add(new ActSummary.Mission(
                    number, name, Optional.of("A"), Optional.of(new SaveGame.LevelStats(1000, 100))));
        }
        ActSummary summary = new ActSummary("ACT II", "HOMEFRONT", missions, 700, 7000, 5, 9, List.of());

        List<DebriefScreen.Row> rows = DebriefScreen.summaryRows(summary);

        List<String> labels = rows.stream().map(DebriefScreen.Row::label).toList();
        assertEquals("MISSIONS", labels.getFirst());
        assertEquals("08 NEON SKYLINE", labels.get(1));
        assertEquals("14 SIEGE SPIRE", labels.get(7));
        assertEquals("ACT TOTAL", labels.get(8));
        assertEquals("SECRETS FOUND", labels.getLast());
        assertEquals("5 OF 9", rows.getLast().middle());
        assertEquals(
                labels.size(),
                DebriefScreen.summaryRows(
                                new ActSummary("ACT I", "FIRST CONTACT", missions, 700, 7000, 0, 0, List.of()))
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
    void anActWithoutSecretsShowsADash() {
        ActSummary summary = new ActSummary("ACT I", "FIRST CONTACT", List.of(), 0, 0, 0, 0, List.of());

        DebriefScreen.Row last = DebriefScreen.summaryRows(summary).getLast();
        assertEquals("SECRETS FOUND", last.label());
        assertEquals("--", last.middle());
    }

    /**
     * Step FIXGH (round 95: "46 GRAVEYARD OF THE CHOIR" ran into its kills): on every act's summary page, with the widest
     * numbers a level can show, every label fits its column (a long level name in the small font, cut only when even that
     * overruns), every middle ends before the amount and inside the panel's right edge, the data cores' rows included.
     */
    @Test
    void everyActsSummaryFitsItsColumns() {
        Content content = ContentLoader.fromClasspath();
        int small = 0;
        for (var act : content.acts().values()) {
            List<ActSummary.Mission> missions = new ArrayList<>();
            List<ActSummary.DataCore> cores = new ArrayList<>();
            for (int number = act.levels().first(); number <= act.levels().last(); number++) {
                var key = content.levelKey(number);
                if (key.isEmpty()) {
                    continue;
                }
                String name = Content.levelName(key.get()).toUpperCase(Locale.ROOT);
                missions.add(new ActSummary.Mission(
                        number, name, Optional.of("A+"), Optional.of(new SaveGame.LevelStats(99_999, 999))));
                for (var secret : content.level(key.get()).secrets()) {
                    if (secret.dataCore().isPresent()) {
                        cores.add(new ActSummary.DataCore(secret.name().toUpperCase(Locale.ROOT), number, name));
                    }
                }
            }
            ActSummary summary = new ActSummary(
                    act.titleCard().act(), act.titleCard().name(), missions, 9_999, 999_999, 99, 99, cores);
            for (DebriefScreen.Row row : DebriefScreen.summaryRows(summary)) {
                if (row.equals(DebriefScreen.Row.heading(row.label()))) {
                    continue;
                }
                String where = act.titleCard().act() + ": " + row.label();
                assertTrue(
                        DebriefScreen.labelWidth(row.label()) <= DebriefScreen.LABEL_WIDTH, where + " fits its column");
                assertEquals(row.label(), DebriefScreen.fittedLabel(row.label()), where + " is not cut");
                assertTrue(DebriefScreen.gap(row, 1) >= Fonts.BODY_CELL, where + ": a cell between middle and amount");
                assertTrue(
                        DebriefScreen.MIDDLE_X + row.middle().length() * Fonts.BODY_CELL <= DebriefScreen.AMOUNT_RIGHT,
                        where + ": its middle inside the panel");
                if (DebriefScreen.smallLabel(row.label())) {
                    small++;
                }
            }
        }
        assertTrue(DebriefScreen.smallLabel("46 GRAVEYARD OF THE CHOIR"), "Level 46's name in the small font");
        assertTrue(small >= 1, "at least Level 46's name is drawn small");
    }
}
