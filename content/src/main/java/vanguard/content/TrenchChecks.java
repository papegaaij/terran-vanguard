package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M8 part E's static checks (design/campaign Level 25 Siren Trench; design/enemies/naval, the Siren and the Spiral
 * Nautilus; m8e-gaps.md §1.1–1.4, §5 E1–E9, pending the user's review in round 54).
 *
 * <p>A unit's: a {@code coil} is an air unit's whole movement and goes with the {@code shell} armour (and the shell
 * with a coil); a slowing ring is a fixed air nest's ({@code terrain} movement), one to a unit; the difficulty's {@code
 * coil} and {@code slow} change what the unit has.
 *
 * <p>A level's: a cave wave ({@code cave}) names a notch of the level's terrain on its {@code edge}'s wall, on the screen
 * at the wave's time; a wall wave ({@code wall}) is a {@code turret nest} of a fixed air unit (and a turret nest is a
 * wall wave) on walls at its nests' rows, never on a notch's mouth or within a backdrop piece's {@code keep_clear} (the
 * relay beacons); on every difficulty at most {@value #MOST_NESTS} nests on the screen, never two of one wall within
 * {@value #NEST_GAP} px, every two rings at least {@value #PHASE_GAP} s apart and at most one ring in flight per wall;
 * no coiling unit holds while the flyable width at the top edge is under its orbit's least ({@code 2 × inner} plus its
 * hit box: no Nautilus in Level 25's 190 px narrows, §5 E9); in a trench (a terrain with notches) every enemy ground
 * target stands in the {@value TerrainBlock#FOOT_BAND} px foot band of a wall's or an island's line (the ledges, §5
 * E6), and the walls' notches are not rock.
 */
final class TrenchChecks {
    /** The most nests on the screen at once (design/enemies/naval/siren). */
    static final int MOST_NESTS = 4;

    /** The least distance between two nests of one wall, px. */
    static final double NEST_GAP = 90;

    /** The least time between two rings, s (the phase rule). */
    static final double PHASE_GAP = 1.5;

    /** The nests' stagger (the simulation's {@code WallMounts.STAGGER_SECONDS}), s. */
    static final double STAGGER = 1.5;

    /** How finely a nest's time on the screen is found, s. */
    private static final double STEP = 0.02;

    private static final double EPSILON = 1e-6;

    private TrenchChecks() {}

    /** A unit's M8 part E keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        EnemyData.Movement movement = enemy.movement();
        movement.coil().ifPresent(coil -> {
            if (!enemy.layer().equals("air")) {
                problem.accept("movement.coil", "a coiling unit orbits the ship on air");
            }
            if (movement.swoop().isPresent()
                    || movement.hover().isPresent()
                    || movement.straight().isPresent()
                    || movement.dive().isPresent()
                    || movement.path().isPresent()
                    || movement.flock().isPresent()
                    || movement.orbit().isPresent()
                    || movement.terrain().isPresent()
                    || movement.rise().isPresent()) {
                problem.accept("movement.coil", "a coil is the unit's whole movement");
            }
        });
        if (enemy.armour().shell() != movement.coil().isPresent()) {
            problem.accept("armour", "the shell armour goes with a coil (and a coil with its shell)");
        }
        long rings = enemy.attacks().stream()
                .filter(attack -> attack.slow().isPresent())
                .count();
        if (rings > 1) {
            problem.accept("attacks", "one slowing ring to a unit");
        }
        if (rings > 0
                && (!enemy.layer().equals("air")
                        || movement.terrain().isEmpty()
                        || enemy.orientation() != Orientation.FIXED)) {
            problem.accept(
                    "attacks", "a slowing ring is a fixed air nest's on the walls (layer air, movement terrain)");
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            if (hook.flatMap(EnemyData.Hook::coil).isPresent()
                    && movement.coil().isEmpty()) {
                problem.accept("difficulty.coil", "a coil change without a coil");
            }
            if (hook.flatMap(EnemyData.Hook::slow).isPresent() && rings == 0) {
                problem.accept("difficulty.slow", "a slow change without a slowing ring");
            }
        }
    }

    /** A level's M8 part E keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        Optional<TerrainBlock> terrain = level.terrain();
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            String field = "waves[" + i + "]";
            boolean nest = wave.groupList().stream()
                    .anyMatch(group -> group.formation().equals("turret nest"));
            if (nest != wave.onWalls()) {
                problem.accept(field + ".wall", "a turret nest wave stands on the walls (wall), and only it");
            }
            if (wave.cave().isPresent() || wave.onWalls()) {
                if (wave.groupList().size() != 1) {
                    problem.accept(field, "a cave's or the walls' wave is one group");
                }
                if (terrain.isEmpty()) {
                    problem.accept(field, "a cave's or the walls' wave in a level without terrain");
                    continue;
                }
            }
            if (wave.cave().isPresent()) {
                cave(level, terrain.get(), wave, field, problem);
            }
            if (wave.onWalls()) {
                wave.groupList().stream()
                        .map(group -> content.enemies().get(group.enemy()))
                        .filter(enemy -> enemy != null)
                        .filter(enemy -> !enemy.layer().equals("air")
                                || enemy.movement().terrain().isEmpty())
                        .findFirst()
                        .ifPresent(enemy -> problem.accept(
                                field + ".enemy", "a wall's nest is a fixed air unit (layer air, movement terrain)"));
            }
        }
        terrain.ifPresent(t -> nests(content, level, t, problem));
        coils(content, level, problem);
        terrain.filter(t -> !t.notchList().isEmpty()).ifPresent(t -> ledges(content, level, t, problem));
    }

    private static void cave(
            LevelData level,
            TerrainBlock terrain,
            LevelData.Wave wave,
            String field,
            BiConsumer<String, String> problem) {
        int index = wave.cave().get();
        if (index >= terrain.notchList().size()) {
            problem.accept(
                    field + ".cave",
                    "no notch " + index + " (" + terrain.notchList().size() + " notches)");
            return;
        }
        TerrainBlock.Notch notch = terrain.notchList().get(index);
        TerrainBlock.Side side =
                wave.edge().orElseThrow() == LevelData.Edge.LEFT ? TerrainBlock.Side.LEFT : TerrainBlock.Side.RIGHT;
        if (notch.side() != side) {
            problem.accept(field + ".edge", "notch " + index + " is cut into the " + lower(notch.side()) + " wall");
        }
        double top = level.scrollAt(wave.t());
        if (notch.low() < top - PlayField.HEIGHT - EPSILON || notch.high() > top + EPSILON) {
            problem.accept(
                    field + ".cave",
                    "notch " + index + " (rows " + num(notch.low()) + "–" + num(notch.high())
                            + ") is not on the screen at t=" + num(wave.t()) + " (rows " + num(top - PlayField.HEIGHT)
                            + "–" + num(top) + ")");
        }
    }

    /** One planned nest: its wall, its row, its x, its time on the screen and its rings. */
    private record Nest(
            String field, boolean left, double row, double x, double enter, double exit, List<Double> rings) {}

    /** Plans the walls' nests on {@code difficulty} as the simulation does ({@code WallMounts}). */
    private static List<Nest> plan(
            Content content,
            LevelData level,
            TerrainBlock terrain,
            Difficulty difficulty,
            BiConsumer<String, String> problem,
            boolean report) {
        List<Nest> out = new ArrayList<>();
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            if (!wave.onWalls() || !wave.fliesOn(difficulty) || wave.groupList().size() != 1) {
                continue;
            }
            LevelData.Group group = wave.groupList().getFirst();
            EnemyData enemy = content.enemies().get(group.enemy());
            if (enemy == null) {
                continue;
            }
            Optional<LevelData.Change> change =
                    switch (difficulty) {
                        case EASY -> wave.easy();
                        case MEDIUM -> Optional.empty();
                        case HARD -> wave.hard();
                    };
            int count = change.flatMap(LevelData.Change::count)
                    .orElseGet(() -> content.difficulty().formationSize(group.count(), difficulty));
            double half = enemy.hitbox().height() / 2;
            Optional<EnemyData.Attack> ring = enemy.slowRing();
            Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
            for (int n = 0; n < count; n++) {
                boolean left;
                double enter;
                double delay = 0;
                if (wave.opposite()) {
                    left = n % 2 == 1;
                    enter = wave.t() + (n / 2) * 2 * STAGGER;
                    delay = left ? STAGGER : 0;
                } else {
                    left = wave.edge().map(edge -> edge == LevelData.Edge.LEFT).orElse(n % 2 == 1);
                    enter = wave.t() + n * STAGGER;
                }
                String field = "waves[" + i + "]";
                double row = level.scrollAt(enter) + half;
                double line = left ? terrain.leftAt(row) : terrain.rightAt(row);
                if (Double.isNaN(line)) {
                    if (report) {
                        problem.accept(
                                field,
                                "nest " + n + " has no " + (left ? "left" : "right") + " wall at row " + num(row)
                                        + " (t=" + num(enter) + ")");
                    }
                    continue;
                }
                double x = line + (left ? 12 : -12);
                // It leaves when its centre is a half height below the bottom edge.
                double exit = enter;
                while (level.scrollAt(exit) - PlayField.HEIGHT - half < row && exit < level.seconds() + 30) {
                    exit += STEP;
                }
                List<Double> rings = new ArrayList<>();
                if (ring.isPresent()) {
                    EnemyData.Attack song = ring.get();
                    double interval = UnitRules.interval(content, song, hook, difficulty);
                    for (double at = enter + song.firstShotDelay().orElse(0.0) + delay; at < exit; at += interval) {
                        rings.add(at);
                    }
                }
                out.add(new Nest(field, left, row, x, enter, exit, rings));
            }
        }
        return out;
    }

    private static void nests(
            Content content, LevelData level, TerrainBlock terrain, BiConsumer<String, String> problem) {
        List<double[]> clear = keptClear(level);
        for (Difficulty difficulty : Difficulty.values()) {
            List<Nest> nests = plan(content, level, terrain, difficulty, problem, difficulty == Difficulty.MEDIUM);
            String on = " on " + lower(difficulty);
            for (int a = 0; a < nests.size(); a++) {
                Nest nest = nests.get(a);
                // On every difficulty: the formation lever adds nests on hard.
                {
                    for (double[] piece : clear) {
                        double distance = Math.hypot(nest.x() - piece[0], nest.row() + PlayField.HEIGHT - piece[1]);
                        if (distance < piece[2]) {
                            problem.accept(
                                    nest.field(),
                                    String.format(
                                                    Locale.ROOT,
                                                    "a nest (t=%s, x %.0f) stands %.0f px from a kept-clear piece"
                                                            + " (keep_clear %.0f px)",
                                                    num(nest.enter()),
                                                    nest.x(),
                                                    distance,
                                                    piece[2])
                                            + on);
                        }
                    }
                    TerrainBlock.Side side = nest.left() ? TerrainBlock.Side.LEFT : TerrainBlock.Side.RIGHT;
                    for (TerrainBlock.Notch notch : terrain.notchList()) {
                        if (notch.side() == side
                                && nest.row() > notch.low() - 20 - EPSILON
                                && nest.row() < notch.high() + 20 + EPSILON) {
                            problem.accept(
                                    nest.field(),
                                    "a nest (t=" + num(nest.enter()) + ", row " + num(nest.row())
                                            + ") stands on a notch's mouth (rows " + num(notch.low()) + "–"
                                            + num(notch.high()) + ")" + on);
                        }
                    }
                }
                int onScreen = 0;
                for (int b = 0; b < nests.size(); b++) {
                    Nest other = nests.get(b);
                    boolean overlaps = other.enter() <= nest.enter() + EPSILON && other.exit() > nest.enter();
                    if (overlaps) {
                        onScreen++;
                    }
                    if (b < a
                            && (overlaps || (nest.enter() <= other.enter() && nest.exit() > other.enter()))
                            && other.left() == nest.left()
                            && Math.abs(other.row() - nest.row()) < NEST_GAP) {
                        problem.accept(
                                nest.field(),
                                "two nests of one wall " + num(Math.abs(other.row() - nest.row())) + " px apart (t="
                                        + num(other.enter()) + " and " + num(nest.enter()) + ")" + on);
                    }
                }
                if (onScreen > MOST_NESTS) {
                    problem.accept(
                            nest.field(),
                            onScreen + " nests on the screen at t=" + num(nest.enter()) + on + " (at most " + MOST_NESTS
                                    + ")");
                }
            }
            rings(content, level, nests, on, problem);
        }
    }

    /** Every two rings at least the phase gap apart; on one wall, at least a ring's flight. */
    private static void rings(
            Content content, LevelData level, List<Nest> nests, String on, BiConsumer<String, String> problem) {
        double flight = level.waves().stream()
                .filter(LevelData.Wave::onWalls)
                .flatMap(wave -> wave.groupList().stream())
                .map(group -> content.enemies().get(group.enemy()))
                .filter(enemy -> enemy != null)
                .flatMap(enemy -> enemy.slowRing().stream()
                        .map(attack -> enemy.slowRing()
                                        .flatMap(EnemyData.Attack::slow)
                                        .orElseThrow()
                                        .radius()
                                / attack.speed().orElseThrow()))
                .max(Double::compare)
                .orElse(0.0);
        List<double[]> all = new ArrayList<>();
        for (Nest nest : nests) {
            for (double t : nest.rings()) {
                all.add(new double[] {t, nest.left() ? 1 : 0});
            }
        }
        all.sort((a, b) -> Double.compare(a[0], b[0]));
        for (int i = 1; i < all.size(); i++) {
            double gap = all.get(i)[0] - all.get(i - 1)[0];
            if (gap < PHASE_GAP - EPSILON) {
                problem.accept(
                        "waves",
                        "two rings " + num(gap) + " s apart at t=" + num(all.get(i - 1)[0]) + " and "
                                + num(all.get(i)[0]) + on + " (at least " + num(PHASE_GAP) + " s)");
            }
            for (int j = i - 1; j >= 0 && all.get(i)[0] - all.get(j)[0] < flight - EPSILON; j--) {
                if (all.get(j)[1] == all.get(i)[1]) {
                    problem.accept(
                            "waves",
                            "two rings in flight on the " + (all.get(i)[1] == 1 ? "left" : "right") + " wall at t="
                                    + num(all.get(i)[0]) + on + " (a ring flies " + num(flight) + " s)");
                }
            }
        }
    }

    /** The backdrop's pieces kept clear: {x, centre row from the bottom edge at the start, radius}. */
    private static List<double[]> keptClear(LevelData level) {
        List<double[]> out = new ArrayList<>();
        BackdropData backdrop = level.backdrop();
        if (backdrop == null) {
            return out;
        }
        for (BackdropData.PlacedPiece placed : backdrop.placed()) {
            BackdropData.Piece piece = backdrop.pieces().get(placed.piece());
            if (piece != null && piece.keepClear().isPresent()) {
                out.add(new double[] {
                    placed.x(), level.pieceCentre(placed), piece.keepClear().get()
                });
            }
        }
        return out;
    }

    /**
     * No coiling unit holds while the flyable width at the top edge is under its least orbit (twice its inner radius
     * plus its hit box), on any difficulty's hold.
     */
    private static void coils(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (level.terrain().isEmpty()) {
            return;
        }
        TerrainBlock terrain = level.terrain().get();
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            for (LevelData.Group group : wave.groupList()) {
                EnemyData enemy = content.enemies().get(group.enemy());
                if (enemy == null || enemy.movement().coil().isEmpty()) {
                    continue;
                }
                double least = 2 * enemy.movement().coil().get().inner()
                        + enemy.hitbox().width();
                double hold = java.util.stream.Stream.of(Optional.<LevelData.Change>empty(), wave.easy(), wave.hard())
                        .map(change -> change.flatMap(LevelData.Change::hold)
                                .or(wave::hold)
                                .orElse(0.0))
                        .max(Double::compare)
                        .orElse(0.0);
                for (double t = wave.t(); t <= wave.t() + hold + EPSILON; t += 0.1) {
                    double row = level.scrollAt(t);
                    double width = widest(terrain, row);
                    if (width < least - EPSILON) {
                        problem.accept(
                                "waves[" + i + "]",
                                "a coiling unit holds (t=" + num(wave.t()) + "–" + num(wave.t() + hold)
                                        + ") while the flyable width at the top edge is " + num(width)
                                        + " px (t=" + num(t) + "), under its orbit's " + num(least) + " px");
                        break;
                    }
                }
            }
        }
    }

    /** The widest flyable span on a row (the walls' lines, the islands), px. */
    private static double widest(TerrainBlock terrain, double row) {
        double best = 0;
        for (double[] span : terrain.spans(row)) {
            best = Math.max(best, span[1] - span[0]);
        }
        return best;
    }

    /** In a trench every enemy ground target stands on a ledge: the foot band of a wall's or an island's line. */
    private static void ledges(
            Content content, LevelData level, TerrainBlock terrain, BiConsumer<String, String> problem) {
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.enemy().isEmpty() || target.side().isPresent()) {
                continue;
            }
            EnemyData enemy = content.enemies().get(target.enemy().get());
            if (enemy == null) {
                continue;
            }
            double height = enemy.hitbox().height();
            for (List<LevelData.Placement> at : TerrainChecks.lists(target)) {
                for (int p = 0; p < at.size(); p++) {
                    LevelData.Placement placement = at.get(p);
                    double row = level.placementRow(placement.t(), height);
                    double off = terrain.toFoot(row, placement.x());
                    if (!terrain.rock(row, placement.x()) && off > TerrainBlock.FOOT_BAND + EPSILON) {
                        problem.accept(
                                "ground_targets[" + i + "].at[" + p + "]",
                                "a ground target in a trench stands on a ledge, in the " + num(TerrainBlock.FOOT_BAND)
                                        + " px foot band of a wall or an island: [" + num(placement.t()) + ", "
                                        + num(placement.x()) + "] is " + num(off) + " px off it");
                    }
                }
            }
        }
    }

    private static String lower(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    private static String num(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.2f", value);
    }
}
