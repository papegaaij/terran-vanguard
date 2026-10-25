package vanguard.content;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * M8 part G's static checks (design/campaign Level 27, Hydrothermal Vents; design/enemies/naval/depth-hunter;
 * m8g-gaps.md §1.1–§1.4).
 *
 * <p>A unit's: its homing torpedoes along its facing are launched while it holds (a {@code hover}).
 *
 * <p>A level's: the vents' {@code look: heat-plume} burns as discs ({@code shape: disc}), and only a disc has an {@code
 * enemy_damage} or a {@code bend}; a row's neighbouring discs lie at least {@link #DISC_GAP} px apart (centre to
 * centre); a {@code column} {@code from: rear} is of a unit that hovers at its {@code y} (not above the ship, not a
 * diver), never two such columns on the screen at once; a wave {@code on_ground} is one group with an x for each unit on every difficulty, of a fixed air unit (layer air, movement terrain).
 */
final class VentChecks {
    /** The least gap between two neighbouring plume discs of a row, centre to centre, px (m8g-gaps.md §1.2). */
    static final double DISC_GAP = 100;

    private VentChecks() {}

    /** A unit's M8 part G keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        boolean torpedoes = enemy.boss().isEmpty()
                && enemy.attacks().stream().anyMatch(attack -> attack.homing().isPresent());
        if (torpedoes && enemy.movement().hover().isEmpty()) {
            problem.accept("movement.hover", "a unit's torpedoes leave along its facing while it holds (a hover)");
        }
    }

    /** A level's M8 part G keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        level.vents().ifPresent(vents -> {
            if (vents.lookOrDefault().equals(HazardsBlock.Vents.HEAT_PLUME) != vents.disc()) {
                problem.accept(
                        "vents.shape", "the heat plumes (look: heat-plume) burn as discs (shape: disc), only they");
            }
            if (!vents.disc()
                    && (vents.enemyDamage().isPresent() || vents.bend().isPresent())) {
                problem.accept("vents", "only a plume's disc (shape: disc) burns units (enemy_damage) or bends (bend)");
            }
            if (vents.disc()) {
                for (int i = 0; i < vents.rows().size(); i++) {
                    List<Double> xs = vents.rows().get(i).xs().stream().sorted().toList();
                    for (int k = 1; k < xs.size(); k++) {
                        if (xs.get(k) - xs.get(k - 1) < DISC_GAP) {
                            problem.accept(
                                    "vents.rows[" + i + "].xs",
                                    "neighbouring plume discs lie at least " + DISC_GAP + " px apart");
                        }
                    }
                }
            }
        });
        double columnGone = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            String field = "waves[" + i + "]";
            for (LevelData.Group group : wave.groupList()) {
                EnemyData enemy = content.enemies().get(group.enemy());
                if (enemy == null) {
                    continue;
                }
                if (group.formation().equals("column") && wave.from() == LevelData.Entry.REAR) {
                    boolean holds = enemy.movement()
                                    .hover()
                                    .filter(hover -> hover.y().isPresent())
                                    .isPresent()
                            && enemy.movement().dive().isEmpty();
                    if (!holds) {
                        problem.accept(
                                field + ".from",
                                "a column from the rear rises to its hover's y (the Depth Hunter's), '" + group.enemy()
                                        + "' has none");
                    } else {
                        // Never two columns on the screen (design/enemies/naval/depth-hunter).
                        if (wave.t() < columnGone) {
                            problem.accept(
                                    field + ".t",
                                    "one column from the rear on the screen at a time: the one before leaves at t="
                                            + Math.round(columnGone * 10) / 10.0);
                        }
                        columnGone = Math.max(columnGone, wave.t() + onScreen(enemy, group.count()));
                    }
                }
            }
            if (wave.onGroundNests()) {
                List<Double> xs = wave.onGround().orElseThrow();
                if (wave.groupList().size() != 1) {
                    problem.accept(field, "a wave of ground nests is one group");
                    continue;
                }
                LevelData.Group group = wave.groupList().getFirst();
                for (Difficulty difficulty : Difficulty.values()) {
                    if (wave.fliesOn(difficulty) && countOn(content, wave, group, difficulty) > xs.size()) {
                        problem.accept(
                                field + ".on_ground",
                                difficulty + ": each ground nest stands at its own x (" + xs.size() + " xs)");
                    }
                }
                EnemyData enemy = content.enemies().get(group.enemy());
                if (enemy != null
                        && (!enemy.layer().equals("air")
                                || enemy.movement().terrain().isEmpty())) {
                    problem.accept(field + ".enemy", "a ground nest is a fixed air unit (layer air, movement terrain)");
                }
            }
        }
    }

    /**
     * About how long a column of {@code count} hovering units from the rear stays on the screen, s: the last one's
     * rise (one behind the other, a hit box and 16 px apart) to its lowest hold, its longest hold and its way down off
     * the bottom edge (the simulation's {@code Formations} rear column).
     */
    static double onScreen(EnemyData enemy, int count) {
        EnemyData.Hover hover = enemy.movement().hover().orElseThrow();
        double speed = enemy.speed();
        double gap = Math.max(0.4, (enemy.hitbox().height() + 16) / speed);
        double up = vanguard.sim.PlayField.HEIGHT - hover.heightFromTop().max() + 40;
        double hold = hover.seconds().map(Span::max).orElse(0.0);
        return (count - 1) * gap + 2 * up / speed + hold;
    }

    /** The units of {@code group} of {@code wave} on {@code difficulty}, as the simulation plans them. */
    private static int countOn(Content content, LevelData.Wave wave, LevelData.Group group, Difficulty difficulty) {
        java.util.Optional<LevelData.Change> change =
                switch (difficulty) {
                    case EASY -> wave.easy();
                    case MEDIUM -> java.util.Optional.empty();
                    case HARD -> wave.hard();
                };
        return change.flatMap(LevelData.Change::count)
                .orElseGet(() -> content.difficulty().formationSize(group.count(), difficulty));
    }
}
