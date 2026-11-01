package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M10 part C's static checks (design/campaign Level 37 Fall of Aurelia; m10c-gaps.md §1, §4).
 *
 * <p>A level's: its {@code air_allies} an ally that {@code follows: lanes}, in one of its {@code liveries}, beside no
 * escort, defend primary, naval convoy, ground allies or holds, every {@code hooked} time a wave's; a {@code saved}
 * secondary only with air allies and a target (on every difficulty) of at most their number; the backdrop's {@code
 * growth} and {@code fall} naming its pieces and lying inside the level, no ground unit on the screen when the fall
 * begins (the world's rule: units on falling decks would stop firing and leave unpaid); the music's theme changes inside
 * the level, Vorne's motif out before the first ({@code motif.to}: a changed theme plays without it).
 */
final class AureliaChecks {
    private AureliaChecks() {}

    /** A level's part C keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        airAllies(content, level, problem);
        level.objectives().secondary().flatMap(LevelData.Secondary::saved).ifPresent(saved -> {
            int units = level.airAllies().map(LevelData.AirAllies::units).orElse(0);
            if (units == 0) {
                problem.accept("objectives.secondary.saved", "a saved secondary counts the level's air allies home");
            }
            for (Difficulty difficulty : Difficulty.values()) {
                int target = level.objectives().secondary().orElseThrow().savedTarget(difficulty);
                if (units > 0 && target > units) {
                    problem.accept(
                            "objectives.secondary.saved",
                            difficulty + ": " + target + " home of " + units + " air allies");
                }
            }
        });
        backdrop(content, level, problem);
        music(level, problem);
    }

    private static void airAllies(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (level.airAllies().isEmpty()) {
            return;
        }
        LevelData.AirAllies allies = level.airAllies().get();
        AlliesData.Ally ally = content.allies().allies().get(allies.ally());
        if (ally == null) {
            problem.accept("air_allies.ally", "no ally '" + allies.ally() + "'");
        } else {
            if (!ally.follows().equals(AlliesData.Ally.LANES)) {
                problem.accept("air_allies.ally", "an air ally follows its lanes: '" + allies.ally() + "'");
            }
            allies.livery()
                    .filter(livery -> !ally.liveries().orElse(List.of()).contains(livery))
                    .ifPresent(livery -> problem.accept(
                            "air_allies.livery", "'" + allies.ally() + "' has no livery '" + livery + "'"));
        }
        if (level.objectives().escort().isPresent()
                || level.objectives().defend().isPresent()
                || level.convoy().isPresent()
                || level.allies().isPresent()
                || !level.holds().orElse(List.of()).isEmpty()) {
            problem.accept(
                    "air_allies",
                    "air allies fly beside no escort, defend primary, naval convoy, ground allies or holds");
        }
        List<Double> hooked = allies.hooked().orElse(List.of());
        for (int i = 0; i < hooked.size(); i++) {
            double t = hooked.get(i);
            if (level.waves().stream().noneMatch(wave -> wave.t() == t)) {
                problem.accept("air_allies.hooked[" + i + "]", "no wave enters at t=" + t);
            }
        }
        for (int i = 0; i < allies.groups().size(); i++) {
            LevelData.LiftGroup group = allies.groups().get(i);
            if (group.climbStart() + group.climb().seconds() > level.seconds()) {
                problem.accept("air_allies.groups[" + i + "].climb", "climbs out after the level end");
            }
        }
    }

    private static void backdrop(Content content, LevelData level, BiConsumer<String, String> problem) {
        BackdropData backdrop = level.backdrop();
        backdrop.growth().ifPresent(growth -> {
            pieces(backdrop, "backdrop.growth", growth.pieces(), problem);
            if (growth.t() + growth.seconds() > level.seconds()) {
                problem.accept("backdrop.growth.t", "the burst ends after the level end");
            }
        });
        backdrop.fall().ifPresent(fall -> {
            pieces(backdrop, "backdrop.fall", fall.pieces(), problem);
            if (fall.t() >= level.seconds()) {
                problem.accept("backdrop.fall.t", "the fall begins after the level end");
            }
            groundAtFall(content, level, fall.t(), problem);
        });
    }

    private static void pieces(
            BackdropData backdrop, String field, List<String> names, BiConsumer<String, String> problem) {
        names.stream()
                .filter(name -> !backdrop.pieces().containsKey(name))
                .forEach(name -> problem.accept(field, "no backdrop piece '" + name + "'"));
    }

    /**
     * No ground unit on the screen at {@code t}: a ground target (at its every placement, on every difficulty) has
     * scrolled off the bottom edge by then, or enters after it.
     */
    private static void groundAtFall(Content content, LevelData level, double t, BiConsumer<String, String> problem) {
        double fallRow = LevelScroll.scrollAt(level.sections(), level.scrollSpeed(), t);
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            double height = target.enemy()
                    .filter(content.enemies()::containsKey)
                    .map(slug -> content.enemy(slug).size().height())
                    .or(() -> target.size().map(Size::height))
                    .orElse(0.0);
            List<LevelData.Placement> placements = new java.util.ArrayList<>(target.at());
            target.easy().flatMap(LevelData.Placements::at).ifPresent(placements::addAll);
            target.hard().flatMap(LevelData.Placements::at).ifPresent(placements::addAll);
            for (LevelData.Placement at : placements) {
                double entered = LevelScroll.scrollAt(level.sections(), level.scrollSpeed(), at.t());
                if (at.t() <= t && fallRow - entered < PlayField.HEIGHT + height / 2) {
                    problem.accept(
                            "ground_targets[" + i + "]",
                            "'" + target.target() + "' (t=" + at.t() + ") is still on the screen when the city falls"
                                    + " (t=" + t + "): no ground unit on a falling deck");
                }
            }
        }
    }

    private static void music(LevelData level, BiConsumer<String, String> problem) {
        LevelData.Music music = level.music();
        List<LevelData.Music.ThemeChange> themes = music.themeList();
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).t() >= level.seconds()) {
                problem.accept("music.themes[" + i + "].t", "after the level end");
            }
        }
        if (!themes.isEmpty()) {
            Optional<Double> motifTo = music.motif().flatMap(LevelData.Music.Motif::to);
            if (music.motif().isPresent()
                    && (motifTo.isEmpty() || motifTo.get() > themes.getFirst().t())) {
                problem.accept(
                        "music.motif.to", "the motif ends before the first theme change (it never plays over it)");
            }
        }
    }
}
