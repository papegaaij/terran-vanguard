package vanguard.content;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M7 part D's static checks (design/campaign Level 17, Dome Siege; design/allies, Ares Landing dome; design/enemies/
 * air/choir-herald and ground/shellback). A level's {@code allies} block: every unit an ally that scrolls with the
 * ground ({@code follows: ground}) and counts hits, its footprint inside the play field and clear of every other
 * unit's on the ground, its {@code targeted_by} (and the easy/hard modes) naming known enemies, its {@code bomb_row}
 * on the play field; an {@code untouched} secondary only with ground allies; no ground target or secret trigger within
 * {@value #COMPOUND_CLEARANCE} px of the {@value #COMPOUND} backdrop placement (the Helix compound: nothing in the
 * level touches it); a pickup's {@code dropped_by.enemy} one of its wave's enemies; round 40 R.c: a walker with a
 * curl-roll (the Shellback) walks its paths between the ground allies, its body's hit box never over an ally's hit
 * box (a dome's glass; its roll still crushes a dome whose footprint the ball touches, in the simulation).
 *
 * <p>A unit's: an {@code aura} on an {@code air} unit that is not a boss, at most one, its hooks only with it; a
 * {@code roll} only on a ground walker, its ball inside the sprite, its hook only with it; a non-boss unit's parts
 * (the Shellback's vent) name the unit's attacks it silences.
 */
final class SiegeChecks {
    /** The Helix compound's backdrop piece (step ART-D's name). */
    static final String COMPOUND = "helix-compound";

    /** No ground target or trigger lies closer to the compound than this, px (design/campaign Level 17, Layout). */
    static final double COMPOUND_CLEARANCE = 100;

    private SiegeChecks() {}

    /** A unit's M7 part D keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        long auras = enemy.attacks().stream()
                .filter(attack -> attack.aura().isPresent())
                .count();
        if (auras > 1) {
            problem.accept("attacks", "a unit has at most one aura");
        }
        if (auras > 0 && (!enemy.layer().equals("air") || enemy.boss().isPresent())) {
            problem.accept("attacks", "an aura is a support flyer's: an air unit that is not a boss");
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            if (hook.flatMap(EnemyData.Hook::aura).isPresent() && auras == 0) {
                problem.accept("difficulty.aura", "an aura hook without an aura attack");
            }
            if (hook.flatMap(EnemyData.Hook::roll).isPresent() && enemy.roll().isEmpty()) {
                problem.accept("difficulty.roll", "a roll hook without a roll");
            }
        }
        enemy.roll().ifPresent(roll -> {
            if (!enemy.layer().equals("ground") || enemy.movement().walk().isEmpty()) {
                problem.accept("roll", "only a ground walker curls and rolls (layer ground, walk movement)");
            }
            if (roll.ball().width() > enemy.size().width()
                    || roll.ball().height() > enemy.size().height()) {
                problem.accept(
                        "roll.ball",
                        "the ball lies inside the " + enemy.size().width() + "×"
                                + enemy.size().height() + " sprite");
            }
        });
    }

    /** A level's M7 part D keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        boolean untouched = level.objectives()
                .secondary()
                .map(LevelData.Secondary::keepsAlliesUntouched)
                .orElse(false);
        if (untouched && level.allies().isEmpty()) {
            problem.accept("objectives.secondary.untouched", "an untouched secondary judges the level's ground allies");
        }
        level.allies().ifPresent(allies -> allies(content, level, allies, problem));
        level.allies().ifPresent(allies -> walks(content, level, allies, problem));
        compound(content, level, problem);
        for (int i = 0; i < level.pickups().size(); i++) {
            LevelData.PlacedPickup pickup = level.pickups().get(i);
            String field = "pickups[" + i + "].dropped_by.enemy";
            pickup.droppedBy().ifPresent(carrier -> carrier.enemy().ifPresent(enemy -> {
                boolean inWave = level.waves().stream()
                        .filter(wave -> wave.t() == carrier.waveT())
                        .flatMap(wave -> wave.groupList().stream())
                        .anyMatch(group -> group.enemy().equals(enemy));
                if (!inWave) {
                    problem.accept(field, "'" + enemy + "' flies in no wave at t=" + carrier.waveT());
                }
            }));
        }
    }

    private static void allies(
            Content content, LevelData level, LevelData.GroundAllies allies, BiConsumer<String, String> problem) {
        if (allies.bombRow() <= 0 || allies.bombRow() >= PlayField.HEIGHT) {
            problem.accept("allies.bomb_row", "the bombers' row lies on the play field, was " + allies.bombRow());
        }
        checkModes(content, "allies.targeted_by", allies.targetedBy(), problem);
        allies.easy()
                .flatMap(LevelData.AlliesChange::targetedBy)
                .ifPresent(modes -> checkModes(content, "allies.easy.targeted_by", modes, problem));
        allies.hard()
                .flatMap(LevelData.AlliesChange::targetedBy)
                .ifPresent(modes -> checkModes(content, "allies.hard.targeted_by", modes, problem));
        List<LevelData.GroundAlly> units = allies.units();
        for (int i = 0; i < units.size(); i++) {
            LevelData.GroundAlly unit = units.get(i);
            String field = "allies.units[" + i + "]";
            AlliesData.Ally spec = content.allies().allies().get(unit.ally());
            if (spec == null) {
                problem.accept(field + ".ally", "no ally '" + unit.ally() + "' in design/allies/data.yaml");
                continue;
            }
            if (!spec.standing() || !spec.countsHits()) {
                problem.accept(
                        field + ".ally",
                        "a level's ground ally scrolls with the ground and counts hits (follows: ground, hits)");
            }
            Size size = unit.size(spec);
            if (unit.at().x() - size.width() / 2 < 0 || unit.at().x() + size.width() / 2 > PlayField.WIDTH) {
                problem.accept(field + ".at", "its " + size.width() + " px footprint lies inside the play field");
            }
            checkTime(level, field + ".at", unit.at().t(), problem);
            for (int j = 0; j < i; j++) {
                LevelData.GroundAlly other = units.get(j);
                AlliesData.Ally otherSpec = content.allies().allies().get(other.ally());
                if (otherSpec != null && overlap(level, unit, size, other, other.size(otherSpec))) {
                    problem.accept(field + ".at", "its footprint overlaps " + other.name() + "'s on the ground");
                }
            }
        }
    }

    /** A walker's paths are sampled this many px apart (on each segment, its ends included). */
    static final double WALK_SAMPLE = 2;

    /**
     * Round 40 R.c (design/campaign Level 17, What goes for the domes): every walker with a curl-roll walks its wave's
     * paths between the ground allies: at no point of a path (on the ground: a path's points are the ground under
     * them at the unit's entry, a convoy's units {@code interval} s apart, 1.5 s without one) does its body's hit box
     * overlap an ally's hit box (a dome's glass) on its row of the ground.
     */
    private static void walks(
            Content content, LevelData level, LevelData.GroundAllies allies, BiConsumer<String, String> problem) {
        for (int w = 0; w < level.waves().size(); w++) {
            LevelData.Wave wave = level.waves().get(w);
            if (wave.paths().isEmpty()) {
                continue;
            }
            for (LevelData.Group group : wave.groupList()) {
                if (!content.enemies().containsKey(group.enemy())) {
                    continue;
                }
                EnemyData enemy = content.enemy(group.enemy());
                if (enemy.roll().isEmpty() || enemy.movement().walk().isEmpty()) {
                    continue;
                }
                List<List<Point>> paths = wave.paths().get();
                for (int i = 0; i < group.count(); i++) {
                    double t = wave.t() + i * wave.interval().orElse(CONVOY_INTERVAL);
                    List<Point> path = paths.get(i % paths.size());
                    String crossed = crossedAlly(content, level, allies, enemy.hitbox(), path, level.scrollAt(t));
                    if (crossed != null) {
                        problem.accept(
                                "waves[" + w + "].paths",
                                "the " + group.enemy() + " entering at t=" + t + " walks over " + crossed
                                        + "'s hit box (round 40 R.c: its walk passes between the domes)");
                    }
                }
            }
        }
    }

    /** A walker convoy's units enter this many s apart without the wave's {@code interval} (the simulation's). */
    static final double CONVOY_INTERVAL = 1.5;

    /**
     * The name of the first ground ally whose hit box a body of {@code body} walking {@code path} (its points the
     * ground under them while the ground has scrolled {@code scroll} px) overlaps; null for none.
     */
    static String crossedAlly(
            Content content,
            LevelData level,
            LevelData.GroundAllies allies,
            Size body,
            List<Point> path,
            double scroll) {
        for (LevelData.GroundAlly unit : allies.units()) {
            AlliesData.Ally spec = content.allies().allies().get(unit.ally());
            if (spec == null) {
                continue;
            }
            Size glass = unit.hitbox(spec);
            double row = level.placementRow(unit.at().t(), unit.size(spec).height());
            double reachX = (body.width() + glass.width()) / 2;
            double reachRow = (body.height() + glass.height()) / 2;
            for (int j = 0; j < path.size(); j++) {
                Point from = path.get(j);
                Point to = path.get(Math.min(j + 1, path.size() - 1));
                double length = Math.hypot(to.x() - from.x(), to.y() - from.y());
                int samples = Math.max(1, (int) Math.ceil(length / WALK_SAMPLE));
                for (int s = 0; s <= samples; s++) {
                    double x = from.x() + (to.x() - from.x()) * s / samples;
                    double y = from.y() + (to.y() - from.y()) * s / samples;
                    if (Math.abs(x - unit.at().x()) < reachX && Math.abs(scroll - y - row) < reachRow) {
                        return unit.name();
                    }
                }
            }
        }
        return null;
    }

    private static void checkModes(
            Content content, String field, Map<String, LevelData.HookMode> modes, BiConsumer<String, String> problem) {
        for (String slug : modes.keySet()) {
            if (!content.enemies().containsKey(slug)) {
                problem.accept(field, "unknown enemy '" + slug + "'");
            }
        }
    }

    private static void checkTime(LevelData level, String field, double t, BiConsumer<String, String> problem) {
        if (t < 0 || t > level.seconds()) {
            problem.accept(field, "t=" + t + " lies outside the level (0–" + level.seconds() + " s)");
        }
    }

    /** Whether two ground allies' footprints overlap on the ground (their rows from their entry times). */
    private static boolean overlap(
            LevelData level, LevelData.GroundAlly a, Size sizeA, LevelData.GroundAlly b, Size sizeB) {
        double rowA = level.placementRow(a.at().t(), sizeA.height());
        double rowB = level.placementRow(b.at().t(), sizeB.height());
        return Math.abs(a.at().x() - b.at().x()) < (sizeA.width() + sizeB.width()) / 2
                && Math.abs(rowA - rowB) < (sizeA.height() + sizeB.height()) / 2;
    }

    /** No ground target or trigger within {@link #COMPOUND_CLEARANCE} px of the Helix compound's placements. */
    private static void compound(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (!level.backdrop().pieces().containsKey(COMPOUND)) {
            return;
        }
        Size piece = level.backdrop().pieces().get(COMPOUND).size();
        for (BackdropData.PlacedPiece placed : level.backdrop().placements()) {
            if (!placed.piece().equals(COMPOUND)) {
                continue;
            }
            // A piece passing the middle of the screen at t lies on row scrollAt(t) − 270 (TerrainBlock.row).
            double pieceRow = TerrainBlock.row(level.scrollAt(placed.t()), PlayField.HEIGHT / 2.0);
            for (int i = 0; i < level.groundTargets().size(); i++) {
                LevelData.GroundTarget target = level.groundTargets().get(i);
                Size size = target.size()
                        .or(() -> target.enemy()
                                .filter(content.enemies()::containsKey)
                                .map(slug -> content.enemy(slug).size()))
                        .orElse(new Size(1, 1));
                for (LevelData.Placement at : target.at()) {
                    double row = level.placementRow(at.t(), size.height());
                    double dx = Math.max(0, Math.abs(at.x() - placed.x()) - (size.width() + piece.width()) / 2);
                    double dy = Math.max(0, Math.abs(row - pieceRow) - (size.height() + piece.height()) / 2);
                    if (Math.hypot(dx, dy) < COMPOUND_CLEARANCE) {
                        problem.accept(
                                "ground_targets[" + i + "].at",
                                "'" + target.target() + "' at t=" + at.t() + " lies within " + COMPOUND_CLEARANCE
                                        + " px of the Helix compound (nothing in the level touches it)");
                    }
                }
            }
        }
    }
}
