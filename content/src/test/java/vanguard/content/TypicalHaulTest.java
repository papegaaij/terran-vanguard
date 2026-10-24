package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * Every level's typical haul lands within 5 % of its budget, and a perfect run earns well above it
 * (design/systems/economy, per-level budget; user decision 2026-10-04).
 */
class TypicalHaulTest {
    /** The most the typical haul may be off its budget, as a share of it. */
    static final double TOLERANCE = 0.05;
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
                        Map.entry(26, 6_769)),
                perfect);
    }

    private Map<Integer, TypicalHaul> hauls() {
        Map<Integer, TypicalHaul> hauls = new TreeMap<>();
        for (String key : content.levels().keySet()) {
            if (!Unmapped.mapped(key)) {
                continue; // M7 part C: a level not mapped yet (Unmapped)
            }
            hauls.put(Content.levelNumber(key), TypicalHaul.of(content, key));
        }
        return hauls;
    }
}
