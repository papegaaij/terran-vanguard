package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.sim.Enemy;
import vanguard.sim.Layer;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.Terrain;

/**
 * M7 part C, step C4 (risk 1 of part C's plan; design/campaign Level 16, Terrain; the default pending the user's
 * review in round 39): in the canyon no air unit's centre on the play field lies in rock, sampled every step of an
 * autopilot flight to the level's end on every difficulty, in either channel. Formations are laid out on the flyable
 * span at their entry row (the simulation's formation span); this proves the residual cases (authored points, side
 * entries, spreads and sweeps under moving walls) stay off the rock. Ground units stand at the walls' foot by the
 * validator's rules ({@code TerrainChecks}).
 */
class TerrainClearanceTest {
    private static final int MAX_STEPS = 60 * 60 * 30;
    private final Content content = ContentLoader.fromClasspath();

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void noAirUnitFliesInRock(Difficulty difficulty) {
        Map<String, Integer> inRock = new TreeMap<>();
        int sampled = 0;
        for (Autopilot.Channel channel : Autopilot.Channel.values()) {
            Sortie sortie = Level16Test.sortie(content, 2185, difficulty);
            Terrain terrain = sortie.terrain();
            int steps = 0;
            while (!sortie.complete() && steps++ < MAX_STEPS) {
                if (!sortie.flying() || sortie.primaryFailed()) {
                    sortie.retry(sortie.ship().defences().maxArmour());
                }
                sortie.step(Autopilot.commands(sortie, channel));
                double scroll = sortie.groundScroll();
                for (int i = 0; i < sortie.enemyCount(); i++) {
                    Enemy enemy = sortie.enemy(i);
                    if (enemy.layer() == Layer.GROUND) {
                        continue;
                    }
                    double x = enemy.renderX(1);
                    double y = enemy.renderY(1);
                    if (x < 0 || x > PlayField.WIDTH || y < 0 || y > PlayField.HEIGHT) {
                        continue;
                    }
                    sampled++;
                    if (terrain.rock(scroll, x, y, 0.5, 0.5)) {
                        inRock.merge(
                                String.format("%s t=%.0f", enemy.spec().slug(), sortie.levelSeconds()),
                                1,
                                Integer::sum);
                    }
                }
            }
            assertTrue(sortie.complete(), difficulty + " " + channel);
        }
        System.out.printf("Level 16 %s: %d air samples, in rock %s%n", difficulty, sampled, inRock);
        assertEquals(Map.of(), inRock, difficulty + ": air units in rock (slug, level second: steps)");
    }
}
