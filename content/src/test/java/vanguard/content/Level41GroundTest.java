package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.sim.Enemy;
import vanguard.sim.Layer;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * Step R79c (the user, round 79, on Level 41's recording: "the tanks are on top of each other [...] and one drives over
 * the gate. Isn't the gate a raised structure?"): flown by the autopilot on the plan's fit (the ship unharmed, so one
 * attempt flies the whole level), no two of the level's walkers (the Warden Tanks) on the screen stand closer than their
 * 64 px size, centre to centre, and in each gate's halt every walker on the screen stands on the apron below the gate's
 * wall (its 64 px body clear of the wall's lower face: ART-G2's walls at y 110, 116 and 110 below the top edge, 36 px
 * thick); the gate pairs park there ({@code park: true}) and never drive on over the wall.
 */
class Level41GroundTest {
    private static final Content CONTENT = LevelMetrics.CONTENT;

    /** A Warden's size (its stat block's 64 × 64): two closer than this, centre to centre, overlap on the screen. */
    static final double APART = 64;

    /** Half a Warden's size. */
    static final double HALF = 32;

    /** Each held gate's halt (the clock's stand, s) and the lower face of its wall, px below the top edge. */
    static final Map<Double, Double> WALL_FACE = Map.of(44.0, 128.0, 76.0, 134.0, 100.0, 128.0);

    /** What the flight saw: the overlaps and the walkers too near a wall, by level second, once each. */
    static final class Watch implements LevelMetrics.Probe {
        final Map<String, String> overlaps = new TreeMap<>();
        final Map<String, String> onWall = new TreeMap<>();
        int pairsSeen;
        int parkedSeen;

        @Override
        public void newAttempt() {
            overlaps.clear();
            onWall.clear();
        }

        @Override
        public void stepped(Sortie sortie, int step) {
            List<Enemy> walkers = new ArrayList<>();
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                double x = enemy.renderX(1);
                double y = enemy.renderY(1);
                if (enemy.layer() == Layer.GROUND
                        && enemy.spec().walker().isPresent()
                        && enemy.hp() > 0
                        && x + HALF > 0
                        && x - HALF < PlayField.WIDTH
                        && y + HALF > 0
                        && y - HALF < PlayField.HEIGHT) {
                    walkers.add(enemy);
                }
            }
            String at = String.format("t=%.1f", sortie.levelSeconds());
            for (int a = 0; a < walkers.size(); a++) {
                for (int b = a + 1; b < walkers.size(); b++) {
                    pairsSeen++;
                    double dx = walkers.get(a).renderX(1) - walkers.get(b).renderX(1);
                    double dy = walkers.get(a).renderY(1) - walkers.get(b).renderY(1);
                    double apart = Math.sqrt(dx * dx + dy * dy);
                    if (apart < APART) {
                        overlaps.putIfAbsent(at, String.format("%.0f px apart", apart));
                    }
                }
            }
            if (sortie.halting()) {
                // The clock stands at (a step before) the gate's section end while it holds.
                Double face = WALL_FACE.get((double) Math.round(sortie.levelSeconds()));
                if (face != null && Math.abs(sortie.levelSeconds() - Math.round(sortie.levelSeconds())) < 0.1) {
                    for (Enemy walker : walkers) {
                        parkedSeen++;
                        double top = PlayField.HEIGHT - walker.renderY(1) - HALF;
                        if (top < face) {
                            onWall.putIfAbsent(
                                    at,
                                    String.format(
                                            "a walker at (%.0f, %.0f below the top) over the wall's face at %.0f",
                                            walker.renderX(1), PlayField.HEIGHT - walker.renderY(1), face));
                        }
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void theTanksStandApartAndOffTheGatesWalls(Difficulty difficulty) {
        Sortie sortie = Level41Test.sortie(CONTENT, Level41Test.planLoadout(CONTENT, difficulty), 1, difficulty, true);
        Watch watch = new Watch();
        boolean complete = LevelMetrics.fly(sortie, Level41Test.MAX_STEPS, watch);
        System.out.printf(
                "Level 41 ground %s: %d walker pairs seen, %d walker steps in the gates' halts; overlaps %s; on a wall %s%n",
                difficulty, watch.pairsSeen, watch.parkedSeen, watch.overlaps, watch.onWall);
        assertTrue(complete, difficulty + ": the flight completes the level");
        assertTrue(watch.parkedSeen > 0, "walkers seen in the halts");
        assertTrue(watch.overlaps.isEmpty(), difficulty + ": walkers on top of each other " + watch.overlaps);
        assertTrue(watch.onWall.isEmpty(), difficulty + ": walkers on a gate's wall " + watch.onWall);
    }
}
