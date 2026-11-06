package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * M11 part A, step A2a: Act 7's static checks (design/enemies, Window shields and Distorted space; the movement
 * vocabulary's {@code teleport} and {@code mirror}; design/enemies/space/gate-warden, rift-skater;
 * design/enemies/air/choir-seraph; design/world/vrell-space, Hazards &amp; set pieces).
 *
 * <p>A unit's: a {@code window_shield} on a single unit (no parts, chain, flock or boss) with a {@code laser-line},
 * beside neither a hex shield nor a mirror; a line's {@code aim: straight} never a boss's; a hook's {@code line_aim}
 * only for a unit with a laser line, its {@code teleport} only for a teleporting one; a hover's {@code exit: up} only on
 * a hover from the top at a {@code y} (no {@code edge_x}, {@code above_ship}, peel or station), its leaving tell inside
 * its hold; a {@code movement.teleport} or {@code movement.mirror} only on a single flying unit ({@code air} or {@code
 * low-air}; no parts, chain, flock, boss, rise, terrain or walk); a mirroring unit holds a hover at a {@code y} or
 * teleports, and fires only a {@code spiral}, never aimed shots.
 *
 * <p>A level's: a wave's {@code line}, {@code line_stagger} and {@code line_order} (and a difficulty's {@code
 * line_stagger}) only on a wave of laser-line units; a {@code current}: |θ| ≤ {@value #MOST_DEGREES}° (easy's scaled
 * ≤ {@value #EASY_MOST_DEGREES}°), turning at ≤ {@value #MOST_RATE}°/s from point to point, its reversals running
 * {@value #RUN_MIN}–{@value #RUN_MAX} s, ≥ {@value #APART} s apart, at most two a minute (easy one), the drift at 0°
 * from each reversal's start to its ease back's end, each held by the level's sections (a section of a negative speed
 * from its {@code t}, ramp 1 s, to its run's end, then a forward one with a ramp of 1 s) and every reversed section a
 * listed reversal; no ground units (no ground targets, no {@code ground} unit in its waves); a side wave enters by the
 * edge the drift comes from (|θ| ≥ {@value #CALM_DEGREES}°); no segment chain enters from a reversal's tell to its
 * ease back's end; at most one wave enters during a reversal, warned (its rear wave).
 */
final class VrellChecks {
    /** The drift's largest angle, degrees (design/enemies, Distorted space). */
    static final double MOST_DEGREES = 25;

    /** Easy's largest angle, degrees (design/world/vrell-space, Distorted space). */
    static final double EASY_MOST_DEGREES = 15;

    /** The drift's fastest turn, degrees a second, from point to point of the profile. */
    static final double MOST_RATE = 10;

    /** A reversal's shortest and longest reversed run, s. */
    static final double RUN_MIN = 3;

    static final double RUN_MAX = 5;

    /** The least time between two reversals, s. */
    static final double APART = 20;

    /** Below this angle the drift is calm: a side wave may enter from either edge (the Rift Skater's 5°). */
    static final double CALM_DEGREES = 5;

    /** Time and angle tolerance. */
    static final double EPSILON = 1e-6;

    private VrellChecks() {}

    /** A unit's Act 7 keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        // M11 part D, step D2: a crack fires one of the unit's fans (the Husk).
        GraveyardChecks.unit(enemy, problem);
        boolean single = enemy.partList().isEmpty()
                && enemy.segmentChain().isEmpty()
                && enemy.movement().flock().isEmpty()
                && enemy.boss().isEmpty();
        Optional<EnemyData.Attack> line =
                enemy.attacks().stream().filter(BeltRules::laserLine).findFirst();
        if (enemy.windowShield().isPresent()) {
            if (!single
                    || line.isEmpty()
                    || enemy.shield().isPresent()
                    || enemy.mirror().isPresent()) {
                problem.accept(
                        "window_shield",
                        "a window shield opens with a laser-line on a single unit (no parts, chain, flock or boss),"
                                + " beside neither a hex shield nor a mirror");
            }
        }
        if (line.flatMap(EnemyData.Attack::line).map(EnemyData.Line::straight).orElse(false)
                && enemy.boss().isPresent()) {
            problem.accept("attacks.line.aim", "a boss's line is always aimed (a straight line is a unit's)");
        }
        Stream.of(
                        enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                        enemy.difficulty().flatMap(EnemyData.Hooks::hard))
                .flatMap(Optional::stream)
                .forEach(hook -> {
                    if (hook.lineAim().isPresent() && line.isEmpty()) {
                        problem.accept("difficulty.line_aim", "a line's aim changes a unit with a laser-line");
                    }
                    if (hook.teleport().isPresent()
                            && enemy.movement().teleport().isEmpty()) {
                        problem.accept("difficulty.teleport", "changes a teleport the unit does not have");
                    }
                });
        enemy.movement().hover().ifPresent(hover -> {
            if (hover.exit().filter(EnemyData.Hover.EXIT_UP::equals).isEmpty()) {
                return;
            }
            if (hover.y().isEmpty()
                    || hover.edgeX().isPresent()
                    || hover.peel().isPresent()
                    || enemy.movement().station().isPresent()) {
                problem.accept(
                        "movement.hover.exit",
                        "exit: up ends a hover from the top at a y (no edge_x, above_ship, peel or station)");
            }
            double tell = hover.leavingTell().orElse(0.0);
            if (hover.seconds().map(seconds -> tell >= seconds.min()).orElse(true)) {
                problem.accept(
                        "movement.hover.leaving_tell", "the leaving tell is the last seconds of a hover's timed hold");
            }
        });
        boolean flying = enemy.layer().equals("air") || enemy.layer().equals("low-air");
        boolean moves = enemy.movement().rise().isPresent()
                || enemy.movement().terrain().isPresent()
                || enemy.movement().walk().isPresent();
        enemy.movement().teleport().ifPresent(teleport -> {
            if (!single || !flying || moves) {
                problem.accept(
                        "movement.teleport",
                        "a teleport is a single flying unit's (air or low-air; no parts, chain, flock, boss, rise,"
                                + " terrain or walk)");
            }
        });
        enemy.movement().mirror().ifPresent(mirror -> {
            if (!single || !flying || moves) {
                problem.accept(
                        "movement.mirror",
                        "a mirror is a single flying unit's (air or low-air; no parts, chain, flock, boss, rise,"
                                + " terrain or walk)");
            }
            boolean station =
                    enemy.movement().hover().map(hover -> hover.y().isPresent()).orElse(false);
            if (!station && enemy.movement().teleport().isEmpty()) {
                problem.accept("movement.mirror", "a mirroring unit mirrors at its hover's y or while it teleports");
            }
            for (int i = 0; i < enemy.attacks().size(); i++) {
                if (!enemy.attacks().get(i).pattern().equals(StormRules.SPIRAL)) {
                    problem.accept(
                            "attacks[" + i + "]",
                            "a mirroring unit fires an unaimed spiral while it mirrors, never aimed shots");
                }
            }
        });
    }

    /** A level's Act 7 keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        // M11 part C, step C2: the reef's pits, organs, crawlers and clouds (Level 45).
        ReefChecks.level(content, level, problem);
        // M11 part D, step D2: the dead fleet's drift lines, crack cue and scenery set piece (Level 46).
        GraveyardChecks.level(content, level, problem);
        List<LevelData.Wave> waves = level.waves();
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            boolean lined = wave.line().isPresent()
                    || wave.lineStagger().isPresent()
                    || Stream.of(wave.easy(), wave.hard())
                            .flatMap(Optional::stream)
                            .anyMatch(change -> change.lineStagger().isPresent());
            if (lined
                    && units(content, wave)
                            .anyMatch(unit -> unit.attacks().stream().noneMatch(BeltRules::laserLine))) {
                problem.accept(
                        "waves[" + i + "].line", "a line's aim and stagger belong to a wave of laser-line units");
            }
        }
        level.current().ifPresent(current -> current(content, level, current, problem));
    }

    private static void current(
            Content content, LevelData level, LevelData.Current current, BiConsumer<String, String> problem) {
        List<LevelData.Bend> profile = current.profile();
        double easy = current.scaleOn(Difficulty.EASY);
        for (int i = 0; i < profile.size(); i++) {
            double degrees = profile.get(i).degrees();
            if (Math.abs(degrees) > MOST_DEGREES + EPSILON) {
                problem.accept(
                        "current.profile[" + i + "]",
                        "the drift turns at most " + MOST_DEGREES + "° off straight down, was " + degrees);
            }
            if (Math.abs(degrees * easy) > EASY_MOST_DEGREES + EPSILON) {
                problem.accept(
                        "current.easy.scale",
                        "easy's drift turns at most " + EASY_MOST_DEGREES + "°, was " + degrees * easy + " at t="
                                + profile.get(i).t());
            }
            if (i > 0) {
                LevelData.Bend from = profile.get(i - 1);
                double rate =
                        Math.abs(degrees - from.degrees()) / (profile.get(i).t() - from.t());
                if (rate > MOST_RATE + EPSILON) {
                    problem.accept(
                            "current.profile[" + i + "]",
                            "the drift turns at most " + MOST_RATE + "°/s, was " + rate + " from t=" + from.t());
                }
            }
        }
        List<LevelData.Reversal> reversals = current.reversalList();
        for (int i = 0; i < reversals.size(); i++) {
            LevelData.Reversal reversal = reversals.get(i);
            String field = "current.reversals[" + i + "]";
            for (Difficulty difficulty : List.of(Difficulty.MEDIUM, Difficulty.EASY)) {
                double run = current.runOn(reversal, difficulty);
                if (run < RUN_MIN - EPSILON || run > RUN_MAX + EPSILON) {
                    problem.accept(field, "a reversal runs reversed " + RUN_MIN + "–" + RUN_MAX + " s, was " + run);
                }
            }
            if (i > 0 && reversal.t() - reversals.get(i - 1).t() < APART - EPSILON) {
                problem.accept(field, "reversals are at least " + APART + " s apart");
            }
            if (i > 1 && reversal.t() - reversals.get(i - 2).t() < 60 - EPSILON) {
                problem.accept(field, "at most 2 reversals a minute");
            }
            if (i > 0 && reversal.t() - reversals.get(i - 1).t() < 60 - EPSILON) {
                problem.accept(field, "easy: at most 1 reversal a minute");
            }
            double end = reversal.runEnd(reversal.run()) + LevelData.Current.EASE_SECONDS;
            for (double t = reversal.t(); t <= end + EPSILON; t += 0.1) {
                if (Math.abs(current.degreesAt(t)) > EPSILON) {
                    problem.accept(
                            field, "the drift is at 0° from a reversal's start to its ease back's end (t=" + t + ")");
                    break;
                }
            }
            if (!held(level, reversal)) {
                problem.accept(
                        field,
                        "the sections hold it: a negative speed from its t (ramp 1) to its run's end, then a forward"
                                + " section with a ramp of 1");
            }
        }
        double start = 0;
        for (int i = 0; i < level.sections().size(); i++) {
            LevelData.Section section = level.sections().get(i);
            double from = start;
            if (section.reversed()
                    && reversals.stream().noneMatch(reversal -> Math.abs(reversal.t() - from) < EPSILON)) {
                problem.accept(
                        "sections[" + i + "]", "a reversed section in a level with a current is one of its reversals");
            }
            start = section.end();
        }
        // A drift level has no ground units (the drift moves the units in the event plane; no surface).
        if (!level.groundTargets().isEmpty()) {
            problem.accept("ground_targets", "a level with a current has no ground units");
        }
        List<LevelData.Wave> waves = level.waves();
        for (int i = 0; i < waves.size(); i++) {
            LevelData.Wave wave = waves.get(i);
            String field = "waves[" + i + "]";
            if (units(content, wave).anyMatch(unit -> unit.layer().equals("ground"))) {
                problem.accept(field, "a level with a current has no ground units");
            }
            upwind(wave, current, field, problem);
            boolean chain =
                    units(content, wave).anyMatch(unit -> unit.segmentChain().isPresent());
            for (LevelData.Reversal reversal : reversals) {
                double end = reversal.runEnd(reversal.run()) + LevelData.Current.EASE_SECONDS;
                if (chain
                        && wave.t() >= reversal.t() - LevelData.Current.TOLD_SECONDS - EPSILON
                        && wave.t() <= end + EPSILON) {
                    problem.accept(field, "no segment chain enters from a reversal's tell to its ease back's end");
                }
            }
        }
        for (int r = 0; r < reversals.size(); r++) {
            LevelData.Reversal reversal = reversals.get(r);
            double end = reversal.runEnd(reversal.run()) + LevelData.Current.EASE_SECONDS;
            List<LevelData.Wave> during = waves.stream()
                    .filter(wave -> wave.t() >= reversal.t() - EPSILON && wave.t() < end - EPSILON)
                    .toList();
            if (during.size() > 1) {
                problem.accept(
                        "current.reversals[" + r + "]",
                        "at most one wave enters during a reversal (its rear wave), was " + during.size());
            }
            if (during.stream().anyMatch(wave -> wave.warning().isEmpty())) {
                problem.accept("current.reversals[" + r + "]", "a reversal's rear wave is warned (warning)");
            }
        }
    }

    /** Whether the level's sections hold {@code reversal} (Level 20's turnabout, eased over 1 s each way). */
    private static boolean held(LevelData level, LevelData.Reversal reversal) {
        List<LevelData.Section> sections = level.sections();
        double start = 0;
        for (int i = 0; i < sections.size(); i++) {
            LevelData.Section section = sections.get(i);
            if (Math.abs(start - reversal.t()) < EPSILON && section.reversed()) {
                boolean easedIn = Math.abs(section.easeSeconds() - LevelData.Current.EASE_SECONDS) < EPSILON;
                boolean runs = Math.abs(section.end() - reversal.runEnd(reversal.run())) < EPSILON;
                boolean easedBack = i + 1 < sections.size()
                        && sections.get(i + 1).speed().orElse(level.scrollSpeed()) > 0
                        && Math.abs(sections.get(i + 1).easeSeconds() - LevelData.Current.EASE_SECONDS) < EPSILON;
                return easedIn && runs && easedBack;
            }
            start = section.end();
        }
        return false;
    }

    /** A side wave enters by the edge the drift comes from (θ > 0: the left edge), unless the drift is calm. */
    private static void upwind(
            LevelData.Wave wave, LevelData.Current current, String field, BiConsumer<String, String> problem) {
        double degrees = current.degreesAt(wave.t());
        if (Math.abs(degrees) < CALM_DEGREES) {
            return;
        }
        LevelData.Edge upwind = degrees > 0 ? LevelData.Edge.LEFT : LevelData.Edge.RIGHT;
        Stream.of(Optional.<LevelData.Change>empty(), wave.easy(), wave.hard()).forEach(change -> {
            LevelData.Entry from = change.flatMap(LevelData.Change::from).orElse(wave.from());
            Optional<LevelData.Edge> edge =
                    change.flatMap(LevelData.Change::edge).or(wave::edge);
            if (from == LevelData.Entry.SIDES && edge.filter(upwind::equals).isEmpty()) {
                problem.accept(
                        field + ".edge",
                        "a side wave enters upwind in a drift: the "
                                + upwind.name().toLowerCase(java.util.Locale.ROOT) + " edge at t=" + wave.t() + " ("
                                + Math.round(degrees) + "°)");
            }
        });
    }

    /** The stat blocks a wave's groups fly (the known ones). */
    private static Stream<EnemyData> units(Content content, LevelData.Wave wave) {
        return wave.groupList().stream()
                .map(LevelData.Group::enemy)
                .filter(content.enemies()::containsKey)
                .map(content::enemy);
    }
}
