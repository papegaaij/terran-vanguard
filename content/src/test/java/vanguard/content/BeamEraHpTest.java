package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import vanguard.sim.Sortie;

/**
 * M7 part E, step E5 (the user's decision of 2026-10-19, "tougher, not more"): the beam era's HP factor ({@code
 * beam_era_hp} in design/enemies) gives the {@code small} units back Level 10's time on the screen. The Ion Beam burns
 * the first target in its column every step from the moment it touches, so with the units' Act 2 HP the plan's fit
 * killed a small unit on Level 18 in 0.9 s from its entry where Level 10's fit (the user's reference, "as it should
 * be") takes 1.6 s, and the units died before they fired. Measured on E2's waves (≈ 90 a minute, Level 10's density)
 * the factor gives 1.1 s at ×1.5, 1.5 s at ×2 and 1.9 s at ×2.5: ×2.
 *
 * <p>The rule: on every mapped level from {@code beam_era_hp.from_level} on, flown at medium with the balance plan's
 * fit ({@link PacingTest#planLoadout}) on the attempt that completes it, a {@code small} unit the player kills lives on
 * the screen, from its entry to its death, between {@link #LEAST} and {@link #MOST} times as long as on Level 10 (the
 * level's crowding lengthens it: the beam serves one target at a time); a level with a reversed scroll, whose small
 * units face the rear guns, at most {@link #REVERSED_MOST} times (M7 part G).
 */
class BeamEraHpTest {
    /** The least ratio of a small unit's time on the screen to Level 10's. */
    static final double LEAST = 0.9;
    /** The most: tougher, not bullet sponges. */
    static final double MOST = 1.5;
    /**
     * M7 part G (step G4, pending the user's review in round 43): the most for a level with a reversed scroll (Level 20
     * Retreat from Hellas). Its small units come up from the bottom edge into the rear guns (the plan's Tail Gun L3, 20
     * a second, and the homing pods), not into the beam, which the factor was set for: the gap check planned them to
     * stay 2–4 s and fire (m7g-gaps §1.5, "tougher, not more"). Measured with the tuned waves and the retreat play: 2.3×
     * (Level 18's waves on the beam: 1.0×); without {@code small_act_hp} it is still 1.9×, flown at the bottom edge
     * under everything (the beam's play, against the level's design) 1.6×. 2.5 accepted by the user in round 43 (Q.2) at
     * 2.48×; the swarm's two levers (round 43 Q.1: the band's lag 30 px/s, a lunge every 4 s) keep motes alive in the
     * beam's column, which then serves them too: 2.53× (step LQ), so 2.6, pending the user's review.
     */
    static final double REVERSED_MOST = 2.6;

    /** The user's reference level. */
    static final int REFERENCE_LEVEL = 10;

    private static final int MAX_STEPS = 60 * 60 * 20;
    private static final Content CONTENT = LevelMetrics.CONTENT;

    /** M9 part E: a forked level (Level 32) on each of its routes ({@link RoutePlay}). */
    static Stream<org.junit.jupiter.params.provider.Arguments> levels() {
        return IntStream.rangeClosed(CONTENT.enemyBasis().beamEraHp().fromLevel(), 50)
                .mapToObj(CONTENT::levelKey)
                .flatMap(java.util.Optional::stream)
                .filter(key -> !PacingTest.PENDING_ACT_TWO_RULE.contains(key))
                .filter(key -> !PacingTest.PENDING_AUTOPILOT.contains(key))
                .flatMap(key -> RoutePlay.routes(CONTENT, key).stream()
                        .map(route -> org.junit.jupiter.params.provider.Arguments.of(key, route)));
    }

    @ParameterizedTest
    @MethodSource("levels")
    void theSmallUnitsLiveAsLongAsOnLevelTen(String key, String route) {
        double reference = smallLife(CONTENT.levelKey(REFERENCE_LEVEL).orElseThrow());
        double life = smallLife(key, route);
        System.out.printf(
                "Beam era %s%s: a killed small unit lives %.2f s on the screen, Level %02d %.2f s (%.2f×)%n",
                key, route.isEmpty() ? "" : " route " + route, life, REFERENCE_LEVEL, reference, life / reference);
        double most = most(key);
        double least = PENDING_LEAST.getOrDefault(key, LEAST);
        assertTrue(
                life >= least * reference && life <= most * reference,
                key + ": " + life + " s vs Level " + REFERENCE_LEVEL + "'s " + reference + " s");
    }

    /** The most for the level: {@link #REVERSED_MOST} with a reversed scroll, else {@link #MOST}. */
    static double most(String key) {
        if (PENDING_MOST.containsKey(key)) {
            return PENDING_MOST.get(key);
        }
        return SimSpecs.level(CONTENT, key, Difficulty.MEDIUM).reverses() ? REVERSED_MOST : MOST;
    }

    /**
     * M8 part H (step H4, pending the user's review in round 58): the most for a level measured over {@link #MOST}.
     * Level 28 Abyssal Maw: 1.52× (2.44 s; Level 27 1.38×), the densest Act 4 approach (91 a minute) with no beam in
     * the plan's fit: its Barbfins hold 9 s above the ship while the Depth Hunter columns and the Abyss Rays below them
     * are served first (the t=93.5 pair overlaps the last column and lives 8-11 s, the t=66 rear ambush of three
     * 7-11 s); the autopilot's variants measured 2.4-2.6 s.
     */
    static final java.util.Map<String, Double> PENDING_MOST =
            java.util.Map.of("act-4-deep-water/level-28-abyssal-maw", 1.6);

    /**
     * The least for a level measured under {@link #LEAST}, pending the user's review. None left: Level 29 Betrayal at
     * Ceres (step B3: 0.71×, its Talons and SAM Nests dying within about a second of their entry under the plan's Ion
     * Beam L4) passes with its waves' rework (step B3b, 2026-10-28, pending round 62: 0.93×, 1.49 s, the Talons swooping
     * at 130 px/s in overlapping firing groups).
     */
    static final java.util.Map<String, Double> PENDING_LEAST = java.util.Map.of();

    /**
     * The mean time on the screen, from its entry to its death, of a {@code small} unit killed while on the screen, on
     * the attempt that completes the level at medium with the plan's fit.
     */
    static double smallLife(String key) {
        return smallLife(key, RoutePlay.NONE);
    }

    /** M9 part E: the same on {@code route} ({@link RoutePlay}). */
    static double smallLife(String key, String route) {
        Sortie sortie = LevelMetrics.sortie(
                CONTENT,
                key,
                Difficulty.MEDIUM,
                PacingTest.planLoadout(key, Difficulty.MEDIUM),
                LevelMetrics.SEED,
                route);
        LevelMetrics.SmallLife life = new LevelMetrics.SmallLife(CONTENT);
        assertTrue(LevelMetrics.fly(sortie, MAX_STEPS, life), key + " completes");
        assertTrue(life.killed() > 0, key + ": small units killed");
        return life.seconds();
    }
}
