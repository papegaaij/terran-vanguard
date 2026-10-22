package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M8 part D's static checks (design/campaign Level 24; design/enemies/naval, the Abyss Ray and the Barbfin; gap check
 * m8d-gaps.md §1.1–1.5).
 *
 * <p>A unit's: a {@code rise} enters on {@code sub} and is the unit's whole movement; a hover {@code above_ship} is an
 * air unit's that leaves after its hold (its {@code seconds}). A level's: a rising unit flies one to a wave and never
 * two on the screen at once (its waves at least a visit apart, on every difficulty); nothing touches a ground piece
 * kept clear ({@code keep_clear}: no ground target and no walker's walk start within its radius of the piece's centre,
 * Level 17's Helix compound rule as a check, gap check D7).
 */
final class AbyssChecks {
    private AbyssChecks() {}

    /** A unit's M8 part D keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        EnemyData.Movement movement = enemy.movement();
        movement.rise().ifPresent(rise -> {
            if (!enemy.layer().equals("sub")) {
                problem.accept("layer", "a rising unit enters as a shadow on sub");
            }
            if (movement.swoop().isPresent()
                    || movement.hover().isPresent()
                    || movement.straight().isPresent()
                    || movement.dive().isPresent()
                    || movement.path().isPresent()
                    || movement.flock().isPresent()
                    || movement.walk().isPresent()
                    || movement.terrain().isPresent()
                    || movement.snake().isPresent()
                    || movement.orbit().isPresent()) {
                problem.accept("movement.rise", "a rise is the unit's whole movement (shadow, rise, glide, sink)");
            }
        });
        enemy.attacks().stream()
                .flatMap(attack -> attack.spawn().stream())
                .filter(spawn -> spawn.ramAfter().isPresent())
                .forEach(spawn -> {
                    EnemyData kind = content.enemies().get(spawn.enemy());
                    if (kind == null
                            || kind.movement()
                                    .flock()
                                    .flatMap(EnemyData.Flock::ram)
                                    .isEmpty()) {
                        problem.accept("spawn.ram_after", "only for a kind that rams from a flock (the Glassfin)");
                    }
                });
        movement.hover().flatMap(EnemyData.Hover::aboveShip).ifPresent(above -> {
            if (!enemy.layer().equals("air")) {
                problem.accept("movement.hover.above_ship", "only an air unit holds above the ship");
            }
            if (movement.hover().flatMap(EnemyData.Hover::seconds).isEmpty()) {
                problem.accept("movement.hover.above_ship", "a hold above the ship ends: give its seconds");
            }
        });
    }

    /** A level's M8 part D keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        oneRiseAtATime(content, level, problem);
        keptClear(content, level, problem);
    }

    /**
     * M8 part D: the time of the level's first wave with a rising unit (the first Abyss Ray's shadow, the {@code
     * first-ray} cue's moment); none without one.
     */
    static Optional<Double> firstRise(Content content, LevelData level) {
        return level.waves().stream()
                .filter(wave -> rise(content, wave).isPresent())
                .map(LevelData.Wave::t)
                .min(Double::compare);
    }

    /** The rise of a wave's rising unit, none if it has none. */
    static Optional<EnemyData.Rise> rise(Content content, LevelData.Wave wave) {
        return wave.groupList().stream()
                .map(group -> content.enemies().get(group.enemy()))
                .filter(enemy -> enemy != null)
                .flatMap(enemy -> enemy.movement().rise().stream())
                .findFirst();
    }

    private static void oneRiseAtATime(Content content, LevelData level, BiConsumer<String, String> problem) {
        for (Difficulty difficulty : Difficulty.values()) {
            double freeFrom = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < level.waves().size(); i++) {
                LevelData.Wave wave = level.waves().get(i);
                Optional<EnemyData.Rise> rise = rise(content, wave);
                if (rise.isEmpty() || !wave.fliesOn(difficulty)) {
                    continue;
                }
                String field = "waves[" + i + "]";
                long risers = wave.groupList().stream()
                        .filter(group -> content.enemies().containsKey(group.enemy())
                                && content.enemy(group.enemy())
                                        .movement()
                                        .rise()
                                        .isPresent())
                        .mapToInt(LevelData.Group::count)
                        .sum();
                if (risers > 1) {
                    problem.accept(field, "one rising unit to a wave (never two on the screen)");
                }
                if (wave.t() < freeFrom) {
                    problem.accept(
                            field,
                            "a rising unit enters while the one before is still on the screen (until " + freeFrom
                                    + " s on " + difficulty + ")");
                }
                freeFrom = Math.max(freeFrom, wave.t() + rise.get().visit());
            }
        }
    }

    private static void keptClear(Content content, LevelData level, BiConsumer<String, String> problem) {
        BackdropData backdrop = level.backdrop();
        if (backdrop == null) {
            return;
        }
        Map<String, BackdropData.Piece> pieces = backdrop.pieces();
        List<double[]> things = new ArrayList<>();
        List<String> names = new ArrayList<>();
        // A ground target's placement enters at the top edge at its time: its centre's ground row.
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.side().isPresent()) {
                continue;
            }
            for (int p = 0; p < target.at().size(); p++) {
                GroundTargetsBlock.Placement at = target.at().get(p);
                things.add(new double[] {at.x(), level.scrollAt(at.t()) + PlayField.HEIGHT});
                names.add("ground_targets[" + i + "].at[" + p + "]");
            }
        }
        // A walker's walk start: its path's first point (y below the top edge at the wave's time).
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            boolean walks = wave.groupList().stream()
                    .anyMatch(group -> content.enemies().containsKey(group.enemy())
                            && content.enemy(group.enemy()).movement().walk().isPresent());
            if (!walks || wave.paths().isEmpty()) {
                continue;
            }
            List<List<Point>> paths = wave.paths().get();
            for (int p = 0; p < paths.size(); p++) {
                Point start = paths.get(p).getFirst();
                things.add(new double[] {start.x(), level.scrollAt(wave.t()) + PlayField.HEIGHT - start.y()});
                names.add("waves[" + i + "].paths[" + p + "]");
            }
        }
        for (BackdropData.PlacedPiece placed : backdrop.placed()) {
            BackdropData.Piece piece = pieces.get(placed.piece());
            if (piece == null || piece.keepClear().isEmpty()) {
                continue;
            }
            double clear = piece.keepClear().get();
            double row = level.pieceCentre(placed);
            for (int i = 0; i < things.size(); i++) {
                double[] at = things.get(i);
                double distance = Math.hypot(at[0] - placed.x(), at[1] - row);
                if (distance < clear) {
                    problem.accept(
                            names.get(i),
                            String.format(
                                    java.util.Locale.ROOT,
                                    "%.0f px from the %s at t=%s (keep_clear %.0f px)",
                                    distance,
                                    placed.piece(),
                                    placed.t(),
                                    clear));
                }
            }
        }
    }
}
