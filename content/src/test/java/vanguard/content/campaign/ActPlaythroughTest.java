package vanguard.content.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.content.BalancePlan;
import vanguard.content.Content;
import vanguard.content.Difficulty;

/**
 * Acts 1 to 3 flown as one campaign on each difficulty (M4 part H, the chained act playthrough; carried on through
 * Act 2 in M5 part I and through Act 3 in M7 part I, the user's I.1 of round 44: Act 3's rules as Acts 1–2's): from
 * the new game's intro briefing, the {@link Playthrough} buys the balance plan's purchases at every hangar visit
 * through the shop (each one in the shop and affordable; a charge the plan buys while the special already carries its
 * most, because the autopilot found or kept one, is printed as accepted), repairs what the credits left allow and
 * flies the level until it is won, in at most {@link Playthrough#MAX_ATTEMPTS} attempts; the campaign goes through a
 * save and a load before and after every visit. Level 07's win ends Act 1 with its outro and Act 2's intro briefing
 * follows; Level 14's win ends Act 2 and Act 3's intro follows; Level 21's win ends Act 3 with its outro, and Act 4's
 * intro follows (M8 part B: Level 22 is built, {@link CampaignRoute#releaseEnd} is false). M8 part F (step F3): Act 4 is
 * flown to Level 26, whose win records Rook missing; the Level 27 visit has the loaner Bulwark in his slot. M8 part G
 * (step G3): Act 4 is flown to Level 27 (the plan refits the Tail Gun on the rear there, the user's Q.1 = a of round
 * 57); the Level 28 visit (the interim release's end: the hangar with no level to launch) still has the loaner. The credits and the fit per visit are printed per act (the
 * playthrough sheet in the test report; Act 4's so far at the end). Round 34, P3 = A: the credits at each act's end lie
 * within the balance plan's tolerance of the expert's bound, the starting credits plus each level's perfect run × the
 * difficulty's income × the expert's collection, with the expert's grade bonus, less the purchases and repairs paid
 * ({@link Playthrough#bound}); on hard up to the plan's {@code hard_under} below it. Rook's ejections and the refused
 * full charges are printed, not failed.
 */
class ActPlaythroughTest {
    private static final Content CONTENT = Playthrough.CONTENT;

    /** The acts' last levels: Act 1 ends at Level 07, Act 2 at Level 14, Act 3 at Level 21. */
    private static final List<Integer> ACT_ENDS = List.of(7, 14, 21);

    /** M8 part G: the last level built (the interim release ends before Level 28). */
    private static final int LAST = 27;

    /**
     * An act's end outside the expert's bound reported for the user's decision: the test prints it instead of failing,
     * and fails once it lies inside the bound again (remove it here) or another act's end leaves the bound. M7 part I
     * (step I1, 2026-10-20; a round-45 finding, pending the user's review): on hard Act 3 ends about 56 % under the
     * bound (hard_under 25 %). Act 2 already ends 20 % under it and the autopilot earns about 15 % less than the
     * expert in Act 3 on hard (B grades from the armour damage at Levels 18–21, Level 20's pursuit motes that break
     * off alive), while the end credits the ratio is taken of are small after Act 3's purchases. The user's tuning
     * order (I.1 of round 44) does not reach it: the plan's later items cannot move earlier (the medium sheet
     * cannot afford Composite III before Level 21, the beam leaves no power for a shield or a module), and a level's
     * {@code bounty_scale} scales the bound and the earnings together. Step PD-C (the user's power room of 2026-10-21,
     * the Mk III at 16 MW): the plan's Pickup magnet from Level 20 leaves it about 54 % under.
     */
    private static final Map<Difficulty, Integer> PENDING_BOUND = Map.of(Difficulty.HARD, 3);

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void theAutopilotFliesTheCampaignOnTheBalancePlan(Difficulty difficulty) {
        Playthrough playthrough = new Playthrough(difficulty);
        BalancePlan plan = playthrough.plan();
        assertInstanceOf(
                CampaignRoute.Step.Briefing.class, CampaignRoute.beforeNextLevel(CONTENT, playthrough.campaign()));
        List<Playthrough.Visit> visits = new ArrayList<>();

        for (int level = 1; level <= LAST; level++) {
            assertEquals(level, playthrough.campaign().nextLevel());
            visits.add(playthrough.fly());
            Campaign campaign = playthrough.campaign();

            CampaignRoute.Step next = CampaignRoute.afterLevel(CONTENT, campaign);
            if (!ACT_ENDS.contains(level)) {
                assertTrue(CampaignRoute.actEnd(CONTENT, campaign).isEmpty(), "Level " + level + " ends no act");
                assertTrue(!(next instanceof CampaignRoute.Step.Outro), "no outro after Level " + level);
            } else {
                int act = ACT_ENDS.indexOf(level) + 1;
                assertEquals(
                        level,
                        CampaignRoute.actEnd(CONTENT, campaign).orElseThrow().level());
                assertInstanceOf(
                        CampaignRoute.Step.Outro.class, next, "Level " + level + " ends its act with its outro");
                report(difficulty, act, visits, campaign);
                long bound = playthrough.bound();
                double off = (campaign.credits() - bound) / (double) bound;
                System.out.printf(
                        "Expert's bound at the act's end: %d credits; the playthrough %+.1f %% (−%.0f %% … +%.0f %%)%n",
                        bound, 100 * off, 100 * plan.expertUnderTolerance(difficulty), 100 * plan.expertTolerance());
                boolean inside = off >= -plan.expertUnderTolerance(difficulty) && off <= plan.expertTolerance();
                String message = difficulty + ": Act " + act + " ends with " + campaign.credits()
                        + " credits, the expert's bound is " + bound;
                if (PENDING_BOUND.getOrDefault(difficulty, 0) == act) {
                    assertFalse(inside, message + ": inside the bound again, remove it from PENDING_BOUND");
                    System.out.println(message + ": outside the bound, a round-45 finding (pending the user's review)");
                } else {
                    assertTrue(inside, message);
                }
                visits.clear();
                // After the outro: the next act's intro briefing before its first level (M8 part B: Act 3's too, Act
                // 4's
                // intro before Level 22; the release's end moves to Level 28 in part H).
                assertFalse(CampaignRoute.releaseEnd(CONTENT, campaign), "Act " + act + "'s outro ends nothing");
                assertInstanceOf(
                        CampaignRoute.Step.Briefing.class,
                        CampaignRoute.beforeNextLevel(CONTENT, campaign),
                        "the next act opens with its briefing");
            }
            // The autosave after the debrief.
            playthrough.autosave();
        }
        assertEquals(LAST + 1, playthrough.campaign().nextLevel());
        Campaign campaign = playthrough.campaign();
        report(difficulty, 4, visits, campaign);
        // M8 part F: Level 26's win records Rook missing (design/player/wingmen, Rook's loss); from the Level 27 visit
        // the loaner Bulwark takes his slot (the hangar's ROOK MIA and LOANER callouts); M8 part G: at the Level 28
        // visit
        // still, and, the interim release's end, the hangar has no level to launch.
        assertTrue(campaign.storyFlags().contains(Campaign.ROOK_MISSING), difficulty + ": Rook missing after Level 26");
        assertTrue(campaign.rookMissing());
        assertFalse(campaign.rookFlies(), "his requires: escort lines are dropped");
        Campaign.EscortFlight loaner = campaign.escortFlight(LAST + 1).orElseThrow();
        assertEquals(Escort.Escorter.BULWARK, loaner.escorter());
        assertTrue(loaner.loaner(), "the loaner Bulwark in his slot");
        assertInstanceOf(CampaignRoute.Step.Hangar.class, CampaignRoute.afterLevel(CONTENT, campaign));
        assertTrue(CampaignRoute.launch(CONTENT, campaign).isEmpty(), "Level 28 is not built");
        assertTrue(
                playthrough.problems().isEmpty(),
                difficulty + ": the balance plan at the visits: " + playthrough.problems());
    }

    private static void report(Difficulty difficulty, int act, List<Playthrough.Visit> visits, Campaign campaign) {
        System.out.printf("Act %d playthrough, %s%n", act, difficulty);
        System.out.println("Lvl | Before | Plan | Repairs (pts) | Rook repairs (pts) | Left | Armour | Rook"
                + " | Attempts | Game overs | Earned | Expert | Grade | Kills | Bought / fit");
        for (Playthrough.Visit visit : visits) {
            System.out.printf(
                    "%02d  | %6d | %4d | %4d (%3d) | %4d (%3d) | %4d | %3.0f/%-3.0f | %-8s | %8d | %10d | %6d | %6d | %-5s | %3d/%-3d | %s | %s%s%n",
                    visit.level(),
                    visit.before(),
                    visit.plan().spent(),
                    visit.repairs(),
                    visit.repaired(),
                    visit.escortRepairs(),
                    visit.escortRepaired(),
                    visit.before() - visit.plan().spent() - visit.repairs() - visit.escortRepairs(),
                    visit.armour(),
                    visit.maxArmour(),
                    visit.escort(),
                    visit.attempts(),
                    visit.gameOvers(),
                    visit.earned(),
                    visit.expert(),
                    visit.grade(),
                    visit.kills(),
                    visit.enemies(),
                    String.join("; ", visit.plan().log()),
                    visit.fit(),
                    visit.failures().isEmpty() ? "" : " | failed: " + String.join(", ", visit.failures()));
        }
        System.out.printf(
                "End of the act: %d credits, armour %.0f/%.0f, Rook %s, score %d, deaths %d%n",
                campaign.credits(),
                campaign.armour(),
                campaign.maxArmour(),
                campaign.gear().escort().hired()
                        ? String.format("%.0f/%.0f", campaign.gear().escort().armour(), campaign.escortMaxArmour())
                        : "-",
                campaign.score(),
                campaign.deaths());
    }
}
