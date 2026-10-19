package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import vanguard.sim.Enemy;
import vanguard.sim.Hitbox;
import vanguard.sim.Loadout;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
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
 * level's crowding lengthens it: the beam serves one target at a time).
 */
class BeamEraHpTest {
    /** The least ratio of a small unit's time on the screen to Level 10's. */
    static final double LEAST = 0.9;
    /** The most: tougher, not bullet sponges. */
    static final double MOST = 1.5;
    /** The user's reference level. */
    static final int REFERENCE_LEVEL = 10;

    private static final int MAX_STEPS = 60 * 60 * 20;
    private static final Content CONTENT = ContentLoader.fromClasspath();

    static Stream<String> levels() {
        return IntStream.rangeClosed(CONTENT.enemyBasis().beamEraHp().fromLevel(), 50)
                .mapToObj(CONTENT::levelKey)
                .flatMap(java.util.Optional::stream)
                .filter(Unmapped::mapped)
                .filter(key -> !PacingTest.PENDING_ACT_TWO_RULE.contains(key));
    }

    @ParameterizedTest
    @MethodSource("levels")
    void theSmallUnitsLiveAsLongAsOnLevelTen(String key) {
        double reference = smallLife(CONTENT.levelKey(REFERENCE_LEVEL).orElseThrow());
        double life = smallLife(key);
        System.out.printf(
                "Beam era %s: a killed small unit lives %.2f s on the screen, Level %02d %.2f s (%.2f×)%n",
                key, life, REFERENCE_LEVEL, reference, life / reference);
        assertTrue(
                life >= LEAST * reference && life <= MOST * reference,
                key + ": " + life + " s vs Level " + REFERENCE_LEVEL + "'s " + reference + " s");
    }

    /**
     * The mean time on the screen, from its entry to its death, of a {@code small} unit killed while on the screen, on
     * the attempt that completes the level at medium with the plan's fit.
     */
    static double smallLife(String key) {
        Loadout loadout = PacingTest.planLoadout(key, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                2185,
                loadout,
                SimSpecs.level(CONTENT, key, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, key, Difficulty.MEDIUM),
                loadout.plating().maxArmour());
        Map<Integer, int[]> entered = new HashMap<>(); // serial → {entry step, last step, on the screen last}
        int attempt = sortie.attempt();
        int step = 0;
        int killed = 0;
        long lived = 0;
        while (!sortie.complete() && step++ < MAX_STEPS) {
            Level04Test.step(sortie);
            if (sortie.attempt() != attempt) {
                attempt = sortie.attempt();
                entered.clear();
                killed = 0;
                lived = 0;
            }
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                EnemyData data = CONTENT.enemies().get(enemy.spec().slug()); // a chain's members have no stat block
                if (data == null || data.tier() != Tier.SMALL) {
                    continue;
                }
                boolean on = onScreen(
                        enemy.renderX(1), enemy.renderY(1), enemy.spec().hitbox());
                int[] track = entered.get(enemy.serial());
                if (track == null && on) {
                    entered.put(enemy.serial(), new int[] {step, step, 1});
                } else if (track != null) {
                    track[1] = step;
                    track[2] = on ? 1 : 0;
                }
            }
            for (var it = entered.values().iterator(); it.hasNext(); ) {
                int[] track = it.next();
                if (track[1] == step) {
                    continue;
                }
                // Gone this step: killed if it was on the screen, else it left.
                if (track[2] == 1) {
                    killed++;
                    lived += track[1] - track[0];
                }
                it.remove();
            }
        }
        assertTrue(sortie.complete(), key + " completes");
        assertTrue(killed > 0, key + ": small units killed");
        return lived * SimStep.SECONDS / killed;
    }

    private static boolean onScreen(double x, double y, Hitbox box) {
        return x + box.width() / 2 > 0
                && x - box.width() / 2 < PlayField.WIDTH
                && y + box.height() / 2 > 0
                && y - box.height() / 2 < PlayField.HEIGHT;
    }
}
