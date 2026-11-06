package vanguard.content;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M11 part A, step A2b: Act 7's level keys checked (design/world/vrell-space, Hazards &amp; set pieces: spore clouds, the
 * quiet; design/enemies/bosses, Boss rush; design/player/specials, Act 7 specials).
 *
 * <p>The {@code spores}: each cloud's radius in the world's range ({@link VrellSpaceData.Spores#radius()}), its centre
 * on the play field; never two clouds overlapping and at most the world's {@code most} on the screen at once (the clouds
 * drift down together with the scroll, so two overlap where their centres lie closer than their radii's sum); none
 * resting over the ship's band longer than the world's {@code band_seconds} (its crossing at the scroll of its time). The
 * {@code quiet} inside the level's time. A {@code rush}: no {@code boss} beside it; each boss a stat block with a bar (a
 * boss script, not a held set piece nor a mid-boss), arriving in its own arena section of speed 0 (a held gate). {@code
 * beyond_gate} only on a {@code vacuum} level (Vrell space). M11 part B, step B2: the {@code transit}'s warp holds
 * the level's end.
 */
final class VrellLevelChecks {
    /** The step the clouds' drift is integrated with, s. */
    private static final double STEP = 0.05;

    private VrellLevelChecks() {}

    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (!level.sporeClouds().isEmpty()) {
            spores(content, level, problem);
        }
        level.quiet().ifPresent(quiet -> {
            if (quiet.to() > level.sections().getLast().end()) {
                problem.accept(
                        "quiet",
                        "the quiet ends inside the level (by "
                                + level.sections().getLast().end() + " s)");
            }
        });
        level.rush().ifPresent(rush -> rush(content, level, rush, problem));
        // M11 part B, step B2 (Level 43): the level ends inside the transit's warp or at its end, never before it.
        level.transit().ifPresent(transit -> {
            double end = level.sections().getLast().end();
            if (end <= transit.warp() || end > transit.warp() + transit.length() + VrellChecks.EPSILON) {
                problem.accept(
                        "transit",
                        "the level ends inside the warp (" + transit.warp() + "–" + (transit.warp() + transit.length())
                                + " s), was " + end + " s");
            }
        });
        if (level.beyondTheGate() && level.levelMedium() != LevelData.Medium.VACUUM) {
            problem.accept("beyond_gate", "beyond the gate is Vrell space: a vacuum level (medium: vacuum)");
        }
    }

    private static void spores(Content content, LevelData level, BiConsumer<String, String> problem) {
        VrellSpaceData.Spores numbers = content.vrellSpace().spores();
        double factor = level.backdrop().scrollFactors().getOrDefault(BackdropLayer.HIGH_AIR, numbers.scrollFactor());
        List<LevelData.SporeCloud> clouds = level.sporeClouds();
        for (int i = 0; i < clouds.size(); i++) {
            LevelData.SporeCloud cloud = clouds.get(i);
            String at = "spores[" + i + "]";
            if (cloud.radius() < numbers.radius().min()
                    || cloud.radius() > numbers.radius().max()) {
                problem.accept(
                        at + ".radius",
                        "a spore cloud's radius is " + numbers.radius().min() + "–"
                                + numbers.radius().max() + " px, was " + cloud.radius());
            }
            if (cloud.x() < 0 || cloud.x() > PlayField.WIDTH) {
                problem.accept(at + ".x", "a spore cloud's centre lies on the play field, was " + cloud.x());
            }
            double band = bandSeconds(level, cloud, factor, numbers.shipBand());
            if (band > numbers.bandSeconds() + VrellChecks.EPSILON) {
                problem.accept(
                        at,
                        "a spore cloud never rests over the ship's band longer than " + numbers.bandSeconds()
                                + " s, was " + Math.round(band * 100) / 100.0 + " s");
            }
        }
        for (int i = 0; i < clouds.size(); i++) {
            int onScreen = 1;
            for (int j = 0; j < clouds.size(); j++) {
                if (j == i) {
                    continue;
                }
                LevelData.SporeCloud a = clouds.get(i);
                LevelData.SporeCloud b = clouds.get(j);
                double ya = y(level, a, factor, Math.max(a.t(), b.t()));
                double yb = y(level, b, factor, Math.max(a.t(), b.t()));
                double apart = Math.hypot(a.x() - b.x(), ya - yb);
                if (j > i && apart < a.radius() + b.radius()) {
                    problem.accept(
                            "spores[" + j + "]", "never two spore clouds overlapping: it overlaps spores[" + i + "]");
                }
                if (onScreenAt(level, b, factor, a.t())) {
                    onScreen++;
                }
            }
            if (onScreen > numbers.most()) {
                problem.accept(
                        "spores[" + i + "]",
                        "at most " + numbers.most() + " spore clouds on the screen at once, " + onScreen
                                + " as it enters");
            }
        }
    }

    /** The cloud's centre y at level time {@code t} (from its entry above the top edge, drifting with the scroll). */
    static double y(LevelData level, LevelData.SporeCloud cloud, double factor, double t) {
        double y = -cloud.radius();
        for (double now = cloud.t(); now < t; now += STEP) {
            y += speedAt(level, now) * factor * Math.min(STEP, t - now);
        }
        return y;
    }

    private static boolean onScreenAt(LevelData level, LevelData.SporeCloud cloud, double factor, double t) {
        if (t < cloud.t()) {
            return false;
        }
        double y = y(level, cloud, factor, t);
        return y - cloud.radius() < PlayField.HEIGHT;
    }

    /** The seconds the cloud lies over the ship's band (its lower edge inside the bottom {@code band} px). */
    private static double bandSeconds(LevelData level, LevelData.SporeCloud cloud, double factor, double band) {
        double seconds = 0;
        double y = -cloud.radius();
        double end = level.sections().getLast().end();
        for (double now = cloud.t(); now < end && y - cloud.radius() < PlayField.HEIGHT; now += STEP) {
            if (y + cloud.radius() > PlayField.HEIGHT - band) {
                seconds += STEP;
            }
            y += speedAt(level, now) * factor * STEP;
        }
        return seconds;
    }

    /** The scroll's speed at level time {@code t} (the section's own, else the level's; ramps left out). */
    private static double speedAt(LevelData level, double t) {
        for (LevelData.Section section : level.sections()) {
            if (t < section.end()) {
                return section.speed().orElse(level.scrollSpeed());
            }
        }
        return level.sections().getLast().speed().orElse(level.scrollSpeed());
    }

    private static void rush(
            Content content, LevelData level, LevelData.Rush rush, BiConsumer<String, String> problem) {
        Set<Integer> gates = new HashSet<>();
        for (int i = 0; i < rush.bosses().size(); i++) {
            LevelData.BossPlacement boss = rush.bosses().get(i);
            String at = "rush.bosses[" + i + "]";
            EnemyData enemy = content.enemies().get(boss.enemy());
            if (enemy == null) {
                problem.accept(at + ".enemy", "no enemy '" + boss.enemy() + "'");
            } else if (enemy.boss().isEmpty() || enemy.boss().get().held()) {
                problem.accept(at + ".enemy", "a rush's boss fights with its bar: a boss script, not a held set piece");
            }
            int index = boss.section() - 1;
            if (index >= level.sections().size()) {
                problem.accept(at + ".section", "no section " + boss.section());
                continue;
            }
            LevelData.Section section = level.sections().get(index);
            double start = index == 0 ? 0 : level.sections().get(index - 1).end();
            if (!section.arena().orElse(false)
                    || section.speed().orElse(level.scrollSpeed()) != 0
                    || boss.t() < start
                    || boss.t() >= section.end()) {
                problem.accept(
                        at,
                        "a rush's boss arrives in its own held gate: an arena section of speed 0 that holds its time");
            }
            if (!gates.add(index)) {
                problem.accept(at + ".section", "one boss a gate: section " + boss.section() + " holds two");
            }
        }
    }
}
