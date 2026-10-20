package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vanguard.sim.Armament;
import vanguard.sim.Loadout;
import vanguard.sim.Sortie;
import vanguard.sim.WingmanSpec;

/**
 * The campaign's pacing rule (design/campaign, Pacing rules): from Level 04 on, the screen is never
 * empty of enemies for longer than {@link #EMPTY_SECONDS} after the launch, with at most {@link
 * #LONG_PAUSES} longer pauses per level. Levels 01-03 are the warm-up and are exempt.
 *
 * <p>From Act 2 on the rule is stricter (the user's Act 2 playtest, 2026-10-18, PT2-B 2a): at medium and hard no
 * more than {@link #ACT_TWO_LONG_PAUSES} stretches over {@link #ACT_TWO_EMPTY_SECONDS}, flown with a fit as strong
 * as the user's (Levels 08-10: {@link #userLoadout}; from Level 11 on the balance plan's fit, which reaches it),
 * since a strong fit kills each wave before the next one enters ({@link #strict}).
 *
 * <p>The autopilot flies each level, so the waves it kills quickly leave the screen early, as for a
 * good player. A stretch is empty when no live enemy overlaps the play field: air and ground units
 * alike (turrets, walkers, released broods; M7 part B: a Burrower, buried or up; M7 part C: a gate's standing
 * tendril and a Threadcrawler's members). A set piece on screen (a crane, a boss) counts as an
 * enemy: it is a scripted moment. The stretches are measured on the attempt that completes the
 * level, from the end of its launch to the end of the scroll, in real seconds (M5 part C: in a hold
 * zone the level clock slows with the scroll, {@link Sortie#realSeconds()}).
 */
class PacingTest {
    /** The first level the rule applies to; Levels 01-03 are the warm-up. */
    static final int FIRST_LEVEL = 4;
    /** The longest a screen may stay empty of enemies, in seconds (Act 1). */
    static final double EMPTY_SECONDS = 3;
    /** How many longer pauses a level may have (Act 1). */
    static final int LONG_PAUSES = 2;
    /** From Act 2 on: the longest a screen may stay empty, in seconds (PT2-B 2a). */
    static final double ACT_TWO_EMPTY_SECONDS = 2;
    /** From Act 2 on: how many longer pauses a level may have. */
    static final int ACT_TWO_LONG_PAUSES = 3;
    /** The first level flown with the user's fit, the first of Act 2. */
    static final int FIRST_USER_FIT_LEVEL = 8;
    /** The last level flown with the user's fit; from the next one on the plan's fit is as strong. */
    static final int LAST_USER_FIT_LEVEL = 10;

    static final int MAX_STEPS = 60 * 60 * 20;
    private static final Content CONTENT = LevelMetrics.CONTENT;

    static Stream<Arguments> levels() {
        return IntStream.rangeClosed(FIRST_LEVEL, 50)
                .mapToObj(CONTENT::levelKey)
                .flatMap(java.util.Optional::stream)
                .filter(Unmapped::mapped) // M7 part C: levels not mapped yet (Unmapped)
                .flatMap(key -> Stream.of(Difficulty.values()).map(difficulty -> Arguments.of(key, difficulty)));
    }

    @ParameterizedTest
    @MethodSource("levels")
    void theScreenIsNeverEmptyForLong(String key, Difficulty difficulty) {
        boolean strict = strict(key, difficulty);
        double limit = strict ? ACT_TWO_EMPTY_SECONDS : EMPTY_SECONDS;
        int allowed = strict ? ACT_TWO_LONG_PAUSES : LONG_PAUSES;
        Loadout loadout = strict ? strongLoadout(key, difficulty) : planLoadout(key, difficulty);
        LevelMetrics.Flight flight = fly(key, difficulty, loadout);
        List<double[]> pauses = flight.pausesOver(limit);

        StringBuilder text = new StringBuilder();
        for (double[] pause : pauses) {
            text.append(String.format(" %.1f-%.1f s (%.1f s)", pause[0], pause[1], pause[1] - pause[0]));
        }
        System.out.printf(
                "Pacing %s %s: %.2f on the screen, %.1f shots a minute, %d pauses over %.0f s:%s%n",
                key, difficulty, flight.meanOnScreen(), flight.shotsPerMinute(), pauses.size(), limit, text);
        assertTrue(pauses.size() <= allowed, "pauses over " + limit + " s:" + text);
    }

    /**
     * Whether the Act 2 rule applies: from Act 2 on at medium and hard (easy thins the waves by design and keeps
     * Act 1's rule), except the levels still {@link #PENDING_ACT_TWO_RULE}.
     */
    static boolean strict(String key, Difficulty difficulty) {
        return SimSpecs.level(CONTENT, key, difficulty).act() > 1
                && difficulty != Difficulty.EASY
                && !PENDING_ACT_TWO_RULE.contains(key);
    }

    /**
     * The levels measured over the Act 2 rule when it came in (step CA, 2026-10-18; medium, pauses over 2 s: L11
     * 10, L12 22, L13 14, L14 12, L15 17, L16 15, L17 15): they keep Act 1's rule until their waves are reworked,
     * pending the user's review. Remove a level once it passes. None left: Level 17, the last, passes with its rework
     * (step PE17, 2026-10-19); a new level that cannot meet the rule yet is listed here.
     */
    static final Set<String> PENDING_ACT_TWO_RULE = Set.of();

    /**
     * The user's Act 2 fit (playtest 2026-10-18): the Pulse Cannon at L4, a Micro-missile Pod at L2 on the right
     * wing, a Bomb Rack at L3 on the left, the Side Splitter at the rear, the second shield and Composite I, and
     * Rook with the Autocannon at L2.
     */
    static Loadout userLoadout(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "bomb-rack", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "side-splitter", 1)),
                        content.shields().models().get(1).name(),
                        content.armour().plating().get(1).name(),
                        0,
                        difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "autocannon",
                        2,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()))
                .withSpecial(SimSpecs.special(content, "Airstrike", 2));
    }

    /** A fit at least as strong as the user's: the user's up to Level 10, the plan's from Level 11 on. */
    static Loadout strongLoadout(String key, Difficulty difficulty) {
        return userFitLevel(key) ? userLoadout(CONTENT, difficulty) : planLoadout(key, difficulty);
    }

    /** Whether the strong fit of the level is the user's (Levels 08-10), not the plan's. */
    static boolean userFitLevel(String key) {
        return Content.levelNumber(key) >= FIRST_USER_FIT_LEVEL && Content.levelNumber(key) <= LAST_USER_FIT_LEVEL;
    }

    /**
     * The fit the level tests fly: the starter fit; for Levels 05 to 07, where the starter cannot clear the
     * batteries in time (Level 05), finish hard (Level 06) or bring down the Brood Carrier (Level 07), the balance
     * plan's fit for the level; from Level 08 on the plan's fit with Rook on its wing (Level 09: the Bomb Rack and
     * Rook's Mortar for the hardened nodes, D8 = c of M5 part C; Level 10: the Tail Gun and Rook's Autocannon, D7 =
     * a of M5 part D; Level 11: the front at L4 and Rook's Mortar again, M5 part E; Level 12: two Micro-missile Pods
     * and Rook's Autocannon, M5 part F; Level 13: Armour Mk III and the pods at level 2, M5 part G; Level 14: the
     * Tail Gun at L2, M5 part H; Level 15: Level 14's, M7 part B; M7 part C: Level 16 with its plan's fit (Level
     * 15's) and Rook; M7 part D: Level 17 with its plan's fit (Level 16's); M7 part E: Level 18 with its plan's fit,
     * the Ion Beam at L2 in front; M7 part F: Level 19 with its plan's fit, the beam at L3 and Rook's Mortar; M7 part
     * G: Level 20 with its plan's fit, the Tail Gun at L3 and Rook's Autocannon).
     */
    static Loadout planLoadout(String key, Difficulty difficulty) {
        BiFunction<Content, Difficulty, Loadout> plan = PLAN_FITS.get(key);
        return plan != null ? plan.apply(CONTENT, difficulty) : SimSpecs.starterLoadout(CONTENT, difficulty);
    }

    private static final Map<String, BiFunction<Content, Difficulty, Loadout>> PLAN_FITS = Map.ofEntries(
            Map.entry(Level05Test.LEVEL, Level05Test::planLoadout),
            Map.entry(Level06Test.LEVEL, Level06Test::planLoadout),
            Map.entry(Level07Test.LEVEL, Level07Test::planLoadout),
            Map.entry(Level08Test.LEVEL, Level08Test::planLoadout),
            Map.entry(Level09Test.LEVEL, Level09Test::planLoadout),
            Map.entry(Level10Test.LEVEL, Level10Test::planLoadout),
            Map.entry(Level11Test.LEVEL, Level11Test::planLoadout),
            Map.entry(Level12Test.LEVEL, Level12Test::planLoadout),
            Map.entry(Level13Test.LEVEL, Level13Test::planLoadout),
            Map.entry(Level14Test.LEVEL, Level14Test::planLoadout),
            Map.entry(Level15Test.LEVEL, Level15Test::planLoadout),
            Map.entry(Level16Test.LEVEL, Level16Test::planLoadout),
            Map.entry(Level17Test.LEVEL, Level17Test::planLoadout),
            Map.entry(Level18Test.LEVEL, Level18Test::planLoadout),
            Map.entry(Level19Test.LEVEL, Level19Test::planLoadout),
            Map.entry(Level20Test.LEVEL, Level20Test::planLoadout));

    /** Flies the level with the autopilot on {@link LevelMetrics#SEED} and measures it ({@link LevelMetrics.Pacing}). */
    static LevelMetrics.Flight fly(String key, Difficulty difficulty, Loadout loadout) {
        Sortie sortie = LevelMetrics.sortie(CONTENT, key, difficulty, loadout, LevelMetrics.SEED);
        LevelMetrics.Pacing pacing = new LevelMetrics.Pacing();
        assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, pacing), key + " " + difficulty + " completes");
        return pacing.flight();
    }
}
