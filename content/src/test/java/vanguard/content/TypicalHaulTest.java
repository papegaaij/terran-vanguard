package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * Every level's typical haul lands within 5 % of its budget, and a perfect run earns well above it
 * (design/systems/economy, per-level budget; user decision 2026-10-04). M9 part E (design/campaign Level 32, Credit
 * budget): a forked level's haul is the mean of its routes' ({@link RoutePlay}), each route within {@link
 * #ROUTE_TOLERANCE} of the budget.
 */
class TypicalHaulTest {
    /** The most the typical haul may be off its budget, as a share of it. */
    static final double TOLERANCE = 0.05;
    /** M9 part E: the most a forked level's route's typical haul may be off the budget (the README's ±8 %). */
    static final double ROUTE_TOLERANCE = 0.08;
    /** A perfect run earns at least this many budgets. */
    static final double PERFECT_BUDGETS = 1.4;

    private final Content content = ContentLoader.fromClasspath();

    @Test
    void theTypicalHaulLandsOnTheBudget() {
        Map<Integer, TypicalHaul> hauls = hauls();
        StringBuilder off = new StringBuilder();
        hauls.forEach((number, haul) -> {
            System.out.printf(
                    "Level %02d: typical %.0f, budget %.0f (%+.1f %%), perfect %d (%.2f × budget)%n",
                    number,
                    haul.typical(),
                    haul.budget(),
                    100 * haul.offBudget(),
                    haul.perfect(),
                    haul.perfect() / haul.budget());
            if (Math.abs(haul.offBudget()) > TOLERANCE) {
                off.append(String.format(" Level %02d: typical %.0f vs %.0f;", number, haul.typical(), haul.budget()));
            }
        });
        assertTrue(off.isEmpty(), "off budget:" + off);
    }

    /** M9 part E: each route of a forked level lands within {@link #ROUTE_TOLERANCE} of the budget (Level 32's four). */
    @Test
    void eachRouteOfAForkedLevelLandsNearTheBudget() {
        StringBuilder off = new StringBuilder();
        int forked = 0;
        for (String key : content.levels().keySet()) {
            List<String> routes = RoutePlay.routes(content, key);
            if (routes.size() == 1) {
                continue;
            }
            forked++;
            for (String route : routes) {
                TypicalHaul haul = TypicalHaul.of(content, key, Difficulty.MEDIUM, route);
                System.out.printf(
                        "Level %02d route %s: typical %.0f, budget %.0f (%+.1f %%), perfect %d%n",
                        Content.levelNumber(key),
                        route,
                        haul.typical(),
                        haul.budget(),
                        100 * haul.offBudget(),
                        haul.perfect());
                if (Math.abs(haul.offBudget()) > ROUTE_TOLERANCE) {
                    off.append(String.format(
                            " Level %02d %s: %.0f vs %.0f;",
                            Content.levelNumber(key), route, haul.typical(), haul.budget()));
                }
            }
        }
        assertTrue(forked > 0, "Level 32 has forks");
        assertTrue(off.isEmpty(), "a route off budget:" + off);
    }

    @Test
    void aPerfectRunEarnsWellAboveTheBudget() {
        hauls().forEach((number, haul) -> assertTrue(
                haul.perfect() >= PERFECT_BUDGETS * haul.budget(), "Level " + number + ": perfect " + haul.perfect()));
    }

    @Test
    void thePerfectRunsAreTheLevelTestsTotals() {
        // The level tests count each source by hand; the README tables show the same totals.
        Map<Integer, Integer> perfect = new TreeMap<>();
        hauls().forEach((number, haul) -> perfect.put(number, haul.perfect()));
        assertEquals(
                Map.ofEntries(
                        Map.entry(1, 1_175),
                        Map.entry(2, 1_184),
                        Map.entry(3, 1_272),
                        Map.entry(4, 1_257),
                        Map.entry(5, 1_334),
                        Map.entry(6, 1_615),
                        Map.entry(7, 1_542),
                        Map.entry(8, 1_776),
                        Map.entry(9, 1_913),
                        Map.entry(10, 2_113),
                        Map.entry(11, 2_043),
                        Map.entry(12, 2_532),
                        Map.entry(13, 2_494),
                        Map.entry(14, 2_396),
                        Map.entry(15, 2_833),
                        // M8 part C (step C3): the Threadcrawlers taken apart with the tail first (its first bonus)
                        Map.entry(16, 3_080),
                        Map.entry(17, 3_145),
                        Map.entry(18, 3_567),
                        Map.entry(19, 3_561),
                        Map.entry(20, 3_875),
                        Map.entry(21, 4_007),
                        Map.entry(22, 4_538),
                        // M8 part C (step C3): the scale fitted to 0.49; an eel taken apart with the tail first
                        Map.entry(23, 5_330),
                        // M8 part D (step D3): the scale fitted to 0.42; one release per reef; M8 AW (step AW2): a
                        // Barbfin more in most groups, the scale refitted to 0.40
                        Map.entry(24, 5_508),
                        // M8 part E (step E3): the README's scale 0.48 (typical −1.7 %; Level25Test's sources); M8 AW
                        // (step AW2): more Barbfins, the scale refitted to 0.42 (typical −1.9 %)
                        Map.entry(25, 5_870),
                        // M8 part F (step F3): the README's scale 0.43 (typical +1.3 %; the README's credit table)
                        Map.entry(26, 6_769),
                        // M8 part G (step G2): the scale fitted to 0.79 (levelReport: typical -1.5 % at 0.78)
                        Map.entry(27, 6_804),
                        // M8 part H (step H3): the README's scale 0.74 (levelReport: typical +0.0 %; the README's
                        // credit
                        // table)
                        Map.entry(28, 6_665),
                        // M9 part B (step B3b): the waves' rework, the scale re-fitted to 0.35 (levelReport: typical
                        // +0.6 % at 0.35; the README's credit table 7,889 counts the leaders' extra by hand)
                        Map.entry(29, 7_924),
                        // M9 part C (step C2): the README's waves with the offbeat waves, a Gunship's Darts as two
                        // streams, the scale fitted to 0.43 (levelReport: typical +0.3 %)
                        Map.entry(30, 8_654),
                        // M9 part D (step D2): the README's waves with the offbeat waves and rock groups, a Gunship's
                        // Darts as two streams, each tug section home paid, the scale fitted to 0.36 (levelReport:
                        // typical -0.5 %)
                        Map.entry(31, 8_618),
                        // M9 part E (step E3): the routes' mean (LL 9,578, LR 9,215, RL 10,088, RR 9,725), the scale
                        // fitted to 0.34 in step E2; the README's credit table the same since step E3b
                        // (tools/sync_tables.py pays each of a Strider's parts its own rounded bounty, as the game)
                        Map.entry(32, 9_652),
                        // M9 part F (step F3): step F2's waves with the offbeat Talon groups, the scale fitted to 0.43
                        // with the hardened Rail Bunkers at 0 % in the typical haul (TypicalHaul.UNPAID_HARDENED; the
                        // perfect run takes them: levelReport 11,712; the README's credit table 11,706 pays a leader's
                        // extra as its rounded bounty less the wing's, 6 × 36, here 6 × 37 as Level 29's)
                        Map.entry(33, 11_712),
                        // M9 part G (step G3): step G2's waves with the offbeat Talon trios, the scale fitted to 0.27;
                        // the six containers' medium salvage, the three crates, the secondary; the Helix core (a STORY
                        // pickup) pays 0 (levelReport and the README's credit table 10,894)
                        Map.entry(34, 10_894),
                        // M9 part H (step H3): Level 35's kills, leaders, hull guns, the Sovereign (433), its 84 bay
                        // Darts
                        // in a typical fight (the core tuned to 14,000), the lifeboat's crate and the secondary at the
                        // scale 0.59 (levelReport 10,717; step H2's 10,467 counted 52 Darts)
                        Map.entry(35, 10_717)),
                perfect);
    }

    private Map<Integer, TypicalHaul> hauls() {
        Map<Integer, TypicalHaul> hauls = new TreeMap<>();
        for (String key : content.levels().keySet()) {
            hauls.put(Content.levelNumber(key), TypicalHaul.of(content, key));
        }
        return hauls;
    }
}
