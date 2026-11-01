package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M10 part D's static checks (design/campaign Level 38 "Lightning Belt"; m10d-gaps.md §1 #3, #4, #5, #8, #10, #12).
 *
 * <p>A level's: a {@code kill_ratio}'s {@code enemy} a known unit that flies in the level on every difficulty; a debris
 * chunk's {@code drop} on a small chunk (one that breaks); the music's {@code ambience_db} naming its sections, each
 * rise more than 0 and at most {@value #MOST_AMBIENCE_DB} dB; on every difficulty a wave of bouncing dashers (the
 * Buzzsaw) and a wave of ramming dashers (the Harrow) at least {@value #DASHERS_APART} s apart (never two dashes and two
 * rams at once), no gust within {@value #GUST_CLEAR} s of a strike's tell, and no strike's column over a hovering
 * homing launcher's station (the Hornet's: its salvo's hatch tell) while its wave holds there ({@link #holdWindow}).
 */
final class StormLayerChecks {
    /** s between a Buzzsaw wave and a Harrow wave (design/campaign Level 38, Waves: the caps). */
    static final double DASHERS_APART = 7;

    /** s between a gust and a strike's tell (design/campaign Level 38, Wind). */
    static final double GUST_CLEAR = 2;

    /** The loudest rise of an ambience by section, dB (Level 13's whiteout +3; headroom for the mix). */
    static final double MOST_AMBIENCE_DB = 6;

    /** s a hovering line takes from its entry to its station, at most (its approach before the hold). */
    static final double STATION_APPROACH = 2;

    private StormLayerChecks() {}

    /** A level's part D keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        scopedSecondary(content, level, problem);
        debris(level, problem);
        ambience(level, problem);
        for (Difficulty difficulty : Difficulty.values()) {
            dashers(content, level, difficulty, problem);
            gusts(level, difficulty, problem);
            stations(content, level, difficulty, problem);
        }
    }

    private static String on(Difficulty difficulty) {
        return difficulty.name().toLowerCase(Locale.ROOT);
    }

    private static void scopedSecondary(Content content, LevelData level, BiConsumer<String, String> problem) {
        Optional<String> enemy = level.objectives().secondary().flatMap(LevelData.Secondary::enemy);
        if (enemy.isEmpty()) {
            return;
        }
        String slug = enemy.get();
        if (!content.enemies().containsKey(slug)) {
            problem.accept("objectives.secondary.enemy", "unknown enemy '" + slug + "'");
            return;
        }
        for (Difficulty difficulty : Difficulty.values()) {
            boolean flies = level.waves().stream()
                    .filter(wave -> wave.fliesOn(difficulty))
                    .flatMap(wave -> wave.groupList().stream())
                    .anyMatch(group -> group.enemy().equals(slug));
            if (!flies) {
                problem.accept(
                        "objectives.secondary.enemy", "no wave of '" + slug + "' to count (" + on(difficulty) + ")");
            }
        }
    }

    private static void debris(LevelData level, BiConsumer<String, String> problem) {
        if (level.debris().isEmpty()) {
            return;
        }
        LevelData.DebrisField field = level.debris().get();
        List<LevelData.PlacedChunk> placed = field.placed();
        for (int i = 0; i < placed.size(); i++) {
            LevelData.PlacedChunk chunk = placed.get(i);
            LevelData.Chunk kind = field.chunks().get(chunk.chunk());
            if (chunk.drop().isPresent() && kind != null && kind.large()) {
                problem.accept("debris.placed[" + i + "].drop", "only a small chunk (one that breaks) drops a pickup");
            }
        }
    }

    private static void ambience(LevelData level, BiConsumer<String, String> problem) {
        level.music()
                .ambienceDb()
                .ifPresent(levels -> levels.forEach((section, db) -> {
                    if (section < 1 || section > level.sections().size()) {
                        problem.accept("music.ambience_db", "no section " + section);
                    }
                    if (!(db > 0 && db <= MOST_AMBIENCE_DB)) {
                        problem.accept(
                                "music.ambience_db",
                                "section " + section + ": a rise of more than 0 and at most " + MOST_AMBIENCE_DB
                                        + " dB, was " + db);
                    }
                }));
    }

    /** The entry times of the waves on {@code difficulty} with a dasher whose dash is a ram ({@code ram}) or not. */
    private static List<Double> dasherWaves(Content content, LevelData level, Difficulty difficulty, boolean ram) {
        List<Double> times = new ArrayList<>();
        for (LevelData.Wave wave : level.waves()) {
            if (!wave.fliesOn(difficulty)) {
                continue;
            }
            for (LevelData.Group group : wave.groupList()) {
                Optional<EnemyData.Dash> dash = content.enemies().containsKey(group.enemy())
                        ? content.enemy(group.enemy()).movement().dash()
                        : Optional.empty();
                if (dash.isPresent() && dash.get().ram() == ram) {
                    times.add(wave.t());
                }
            }
        }
        return times;
    }

    private static void dashers(Content content, LevelData level, Difficulty difficulty, BiConsumer<String, String> p) {
        List<Double> bouncing = dasherWaves(content, level, difficulty, false);
        List<Double> ramming = dasherWaves(content, level, difficulty, true);
        for (double b : bouncing) {
            for (double r : ramming) {
                if (Math.abs(b - r) < DASHERS_APART - 1e-9) {
                    p.accept(
                            "waves",
                            "a wave of bouncing dashers and one of ramming dashers at least " + DASHERS_APART
                                    + " s apart (" + on(difficulty) + ": t=" + b + " and t=" + r + ")");
                }
            }
        }
    }

    /** The gusts' push starts on {@code difficulty}: the level's and that difficulty's extra ones. */
    static List<Double> gustsOn(LevelData level, Difficulty difficulty) {
        Optional<LevelData.Gusts> gusts = level.weather().flatMap(LevelData.Weather::gusts);
        if (gusts.isEmpty()) {
            return List.of();
        }
        List<Double> at = new ArrayList<>();
        gusts.get().at().forEach(gust -> at.add(gust.t()));
        Optional<LevelData.GustChange> change =
                switch (difficulty) {
                    case EASY -> gusts.get().easy();
                    case MEDIUM -> Optional.empty();
                    case HARD -> gusts.get().hard();
                };
        change.flatMap(LevelData.GustChange::extra).ifPresent(extra -> extra.forEach(gust -> at.add(gust.t())));
        return at;
    }

    private static void gusts(LevelData level, Difficulty difficulty, BiConsumer<String, String> problem) {
        List<Double> gusts = gustsOn(level, difficulty);
        for (LevelData.StrikeAt strike : JovianHazardRules.strikesOn(level, difficulty)) {
            for (double gust : gusts) {
                if (Math.abs(gust - strike.t()) < GUST_CLEAR - 1e-9) {
                    problem.accept(
                            "weather.gusts",
                            "no gust within " + GUST_CLEAR + " s of a strike's tell (" + on(difficulty)
                                    + ": a gust at t=" + gust + ", the strike at t=" + strike.t() + ")");
                }
            }
        }
    }

    /**
     * The level times a hovering wave holds its stations, from its entry: its approach and its hover's longest time;
     * none for a unit without a hover.
     */
    static Optional<double[]> holdWindow(EnemyData enemy, double t) {
        return enemy.movement().hover().flatMap(EnemyData.Hover::seconds).map(seconds ->
                new double[] {t, t + STATION_APPROACH + seconds.max()});
    }

    /** Whether the unit launches an aimed homing salvo with a hatch tell (the Hornet). */
    private static boolean homingLauncher(EnemyData enemy) {
        return enemy.attacks().stream()
                .anyMatch(attack ->
                        attack.homing().flatMap(EnemyData.Homing::aimed).orElse(false));
    }

    private static void stations(
            Content content, LevelData level, Difficulty difficulty, BiConsumer<String, String> problem) {
        List<LevelData.StrikeAt> strikes = JovianHazardRules.strikesOn(level, difficulty);
        if (strikes.isEmpty()) {
            return;
        }
        double column = content.jovian().strike().width();
        for (LevelData.Wave wave : level.waves()) {
            if (!wave.fliesOn(difficulty) || wave.from() != LevelData.Entry.FRONT) {
                continue;
            }
            for (LevelData.Group group : wave.groupList()) {
                if (!group.formation().equals("line abreast")
                        || !content.enemies().containsKey(group.enemy())) {
                    continue;
                }
                EnemyData enemy = content.enemy(group.enemy());
                Optional<double[]> window = holdWindow(enemy, wave.t());
                if (window.isEmpty() || !homingLauncher(enemy)) {
                    continue;
                }
                int count = (difficulty == Difficulty.EASY
                                ? wave.easy()
                                : difficulty == Difficulty.HARD ? wave.hard() : Optional.<LevelData.Change>empty())
                        .flatMap(LevelData.Change::count)
                        .orElse(group.count());
                double reach = enemy.hitbox().width() / 2.0 + column / 2;
                for (LevelData.StrikeAt strike : strikes) {
                    if (strike.tracks() || strike.t() < window.get()[0] || strike.t() > window.get()[1]) {
                        continue;
                    }
                    for (int i = 0; i < count; i++) {
                        double x = (i + 1) * PlayField.WIDTH / (count + 1.0);
                        if (Math.abs(strike.x() - x) < reach) {
                            problem.accept(
                                    "weather.strikes",
                                    "no column over a station of the t=" + wave.t() + " " + group.enemy()
                                            + " line while it holds (" + on(difficulty) + ": the strike at t="
                                            + strike.t() + ", x=" + strike.x() + ", a station at x=" + x + ")");
                        }
                    }
                }
            }
        }
    }
}
