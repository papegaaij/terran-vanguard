package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * M11 part F, step F2 (design/campaign Level 48 Waves and The reactor core; m11f §1 #10): the static rules of a level whose
 * rear waves come up away from the ship (a wave with {@code lane: away}: the Vrell citadel), on every difficulty. Its rear
 * waves (every {@code from: rear} wave, the Wraiths' ambushes too) enter at least {@value #REAR_APART} s apart, and none
 * within the {@value #BEFORE_NEST} s before a turret nest (a ground target of enemies) scrolls in, so the ship is never
 * pinned between a nest's fire and a wing rising behind it. Its secondary's {@code escapes} units placed as ground
 * targets (the reactor cores) lie beside the nests, never under them: each at least {@value #CORE_CLEAR} px from every
 * nest unit on the ground (the scroll between their times at the section's speed).
 */
final class CitadelChecks {
    private CitadelChecks() {}

    /** The least time between two rear waves' entries, s. */
    static final double REAR_APART = 6;

    /** No rear wave enters this long before a turret nest scrolls in, s. */
    static final double BEFORE_NEST = 2;

    /** A secondary's ground object keeps this far from every nest unit, px. */
    static final double CORE_CLEAR = 60;

    /** The rules, for a level with a {@code lane: away} wave. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (level.waves().stream().noneMatch(wave -> wave.lane().isPresent())) {
            return;
        }
        List<Double> nests = new ArrayList<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.enemy().isPresent() && !escaping(level, target)) {
                target.at().forEach(at -> nests.add(at.t()));
            }
        }
        for (Difficulty difficulty : Difficulty.values()) {
            List<Double> rear = new ArrayList<>();
            List<Integer> index = new ArrayList<>();
            for (int i = 0; i < level.waves().size(); i++) {
                LevelData.Wave wave = level.waves().get(i);
                if (wave.fliesOn(difficulty) && wave.from() == LevelData.Entry.REAR) {
                    rear.add(wave.t());
                    index.add(i);
                }
            }
            for (int k = 1; k < rear.size(); k++) {
                if (rear.get(k) - rear.get(k - 1) < REAR_APART - 1e-9) {
                    problem.accept(
                            "waves[" + index.get(k) + "].t",
                            difficulty + ": a rear wave " + (rear.get(k) - rear.get(k - 1)) + " s after the one before"
                                    + " (at least " + REAR_APART + ")");
                }
            }
            for (int k = 0; k < rear.size(); k++) {
                double t = rear.get(k);
                if (nests.stream().anyMatch(n -> n - t >= 0 && n - t < BEFORE_NEST)) {
                    problem.accept(
                            "waves[" + index.get(k) + "].t",
                            difficulty + ": a rear wave within " + BEFORE_NEST + " s before a turret nest scrolls in");
                }
            }
        }
        List<LevelData.GroundTarget> targets = level.groundTargets();
        for (int i = 0; i < targets.size(); i++) {
            LevelData.GroundTarget core = targets.get(i);
            if (!escaping(level, core)) {
                continue;
            }
            for (LevelData.Placement at : core.at()) {
                for (LevelData.GroundTarget nest : targets) {
                    if (nest.enemy().isEmpty() || escaping(level, nest)) {
                        continue;
                    }
                    for (LevelData.Placement unit : nest.at()) {
                        double dy = Math.abs(unit.t() - at.t()) * speedAt(level, Math.min(unit.t(), at.t()));
                        if (Math.hypot(unit.x() - at.x(), dy) < CORE_CLEAR) {
                            problem.accept(
                                    "ground_targets[" + i + "].at",
                                    core.target() + " lies under a nest unit (" + nest.target() + "): at least "
                                            + CORE_CLEAR + " px off");
                        }
                    }
                }
            }
        }
    }

    /** Whether the ground target's units are the secondary's {@code escapes} units. */
    private static boolean escaping(LevelData level, LevelData.GroundTarget target) {
        return target.enemy().isPresent()
                && level.objectives()
                        .secondary()
                        .flatMap(LevelData.Secondary::escapes)
                        .filter(target.enemy().get()::equals)
                        .isPresent();
    }

    /** The scroll's speed at {@code t}: its section's (the level's when the section sets none). */
    private static double speedAt(LevelData level, double t) {
        double speed = level.scrollSpeed();
        for (LevelData.Section section : level.sections()) {
            speed = section.speed().orElse(speed);
            if (t < section.end()) {
                return speed;
            }
        }
        return speed;
    }
}
