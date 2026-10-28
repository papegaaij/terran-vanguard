package vanguard.content;

import java.util.function.BiConsumer;

/**
 * M9 part A's static checks (design/world/belt, Vacuum rules; design/player/specials, Act 5 specials; design/enemies,
 * Enemy shields).
 *
 * <p>A level's: {@code medium: vacuum} and {@code underground} (the level's or a section's) only from Act 5's first
 * level on; never vacuum and under water in one level; an underground level needs the specials' {@code underground}
 * limit (its {@code IN ROCK} word). A unit's: a {@code laser-line}'s bullet class is a known one; a shield or a homing
 * diamond only on a unit of Act 5 or later.
 */
final class BeltChecks {
    private BeltChecks() {}

    /** The first level of Act 5, as the shop's availability names it ({@code act 5}). */
    static int firstLevel(Content content) {
        return content.player().firstLevel("act 5");
    }

    /** A level's vacuum and underground keys. */
    static void level(Content content, int number, LevelData level, BiConsumer<String, String> problem) {
        boolean vacuum = level.levelMedium() == LevelData.Medium.VACUUM
                || level.sections().stream()
                        .anyMatch(section -> section.medium().orElse(null) == LevelData.Medium.VACUUM);
        boolean underground = level.underground().orElse(false)
                || level.sections().stream()
                        .anyMatch(section -> section.underground().orElse(false));
        if ((vacuum || underground) && number < firstLevel(content)) {
            problem.accept(
                    vacuum ? "medium" : "underground",
                    (vacuum ? "vacuum" : "underground") + " only from Act 5 (Level " + firstLevel(content)
                            + "), not Level " + number);
        }
        if (vacuum && level.underwater()) {
            problem.accept("medium", "a level is in vacuum or under water, not both");
        }
        if (underground
                && content.specials()
                        .forbids(java.util.Optional.of(SpecialsData.Setting.UNDERGROUND))
                        .isEmpty()) {
            problem.accept("underground", "the specials' limits name no underground setting (design/player/specials)");
        }
    }

    /**
     * M9 part B (design/campaign Level 29): a held arena's {@code holds} names the group of enemy ground targets that
     * stand on it (entering before its end); {@code halt_waves} and {@code escort_swap} only with a held arena, the swap
     * only in the level Rook is rescued in (his last missing level); {@code leader: tip} only of a unit with a {@code
     * leader}; {@code on_rocks} of a riding unit on a known small debris chunk; {@code spared} names a neutral kind the
     * level flies; {@code dormant_until} after the nest's entry and before the level's end, a holder's by its halt.
     */
    static void partB(Content content, int number, LevelData level, BiConsumer<String, String> problem) {
        java.util.List<LevelData.Section> held =
                level.sections().stream().filter(LevelData.Section::held).toList();
        for (LevelData.Section section : held) {
            String group = section.holds().orElseThrow();
            java.util.List<LevelData.GroundTarget> holders = level.groundTargets().stream()
                    .filter(target -> target.enemy().isPresent()
                            && target.group().filter(group::equals).isPresent())
                    .toList();
            if (holders.isEmpty()) {
                problem.accept("sections", "a held arena's holds names enemy ground targets' group: '" + group + "'");
            }
            for (LevelData.GroundTarget target : holders) {
                for (LevelData.Placement placement : target.at()) {
                    if (!(placement.t() < section.end())) {
                        problem.accept(
                                "ground_targets", "a held arena's holders enter before its end: " + placement.t());
                    }
                }
                // Step B3b (Level 29's clamp guns): the clock stands a step before the arena's end, so a holder asleep
                // until the end would never wake and the halt would never end.
                target.dormantUntil()
                        .filter(until -> vanguard.sim.SimStep.ticks(until) >= vanguard.sim.SimStep.ticks(section.end()))
                        .ifPresent(until -> problem.accept(
                                "ground_targets",
                                "a held arena's holder wakes by its halt (the clock stands a step before "
                                        + section.end()
                                        + "): dormant_until "
                                        + until));
            }
        }
        if (held.isEmpty()
                && (level.haltWaves().isPresent() || level.escortSwap().isPresent())) {
            problem.accept("halt_waves", "a halt cycle or an escort swap needs a held arena (holds)");
        }
        if (level.escortSwap().isPresent()
                && number != content.wingmen().rook().missing().back() - 1) {
            problem.accept("escort_swap", "Rook is rescued in his last missing level, not Level " + number);
        }
        // Step B4b: a holders-down line in a level with an escort swap plays for one of its outcomes (the simulation
        // cues it with the swap's); without a swap it names none.
        for (LevelData.RadioCue cue : level.radio()) {
            if (cue.event().orElse(null) == LevelData.CueEvent.HOLDERS_DOWN
                    && cue.swap().isPresent() != level.escortSwap().isPresent()) {
                problem.accept(
                        "radio",
                        "a holders-down line names the escort swap's outcome (swap) exactly when the level has a swap: "
                                + cue.line());
            }
        }
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            String slug = wave.groupList().getFirst().enemy();
            if (!content.enemies().containsKey(slug)) {
                continue;
            }
            EnemyData enemy = content.enemy(slug);
            if (wave.leader().isPresent() && enemy.leader().isEmpty()) {
                problem.accept("waves[" + i + "].leader", "'" + slug + "' has no leader airframe");
            }
            if (wave.onRocks().isPresent()) {
                LevelData.OnRocks rocks = wave.onRocks().get();
                if (enemy.movement().ride().isEmpty()) {
                    problem.accept("waves[" + i + "].on_rocks", "'" + slug + "' does not ride rocks (movement ride)");
                }
                LevelData.Chunk chunk = level.debris()
                        .map(field -> field.chunks().get(rocks.chunk()))
                        .orElse(null);
                if (chunk == null || chunk.large()) {
                    problem.accept(
                            "waves[" + i + "].on_rocks.chunk",
                            "a small debris chunk of the level: '" + rocks.chunk() + "'");
                }
            } else if (enemy.movement().ride().isPresent()) {
                problem.accept("waves[" + i + "]", "a riding unit ('" + slug + "') flies on rocks (on_rocks)");
            }
        }
        level.objectives().secondary().flatMap(LevelData.Secondary::spared).ifPresent(slug -> {
            boolean flown = level.waves().stream().anyMatch(wave -> wave.groupList().stream()
                    .anyMatch(g -> g.enemy().equals(slug)));
            if (!content.enemies().containsKey(slug) || !content.enemy(slug).isNeutral() || !flown) {
                problem.accept("objectives.secondary.spared", "a neutral craft the level flies: '" + slug + "'");
            }
        });
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.dormantUntil().isPresent()) {
                double until = target.dormantUntil().get();
                if (target.at().stream().anyMatch(p -> !(p.t() < until)) || !(until < level.seconds())) {
                    problem.accept(
                            "ground_targets[" + i + "].dormant_until",
                            "a nest wakes after its entry and before the level's end: " + until);
                }
            }
        }
    }

    /** A unit's Act 5 keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        // M9 part B: a neutral craft fires nothing and pays nothing; a leader's airframe flies a V-wing.
        if (enemy.isNeutral() && (!enemy.attacks().isEmpty() || enemy.bounty() != 0)) {
            problem.accept("neutral", "a neutral craft has no attacks and no bounty");
        }
        if (enemy.leader().isPresent()
                && enemy.formations().stream().noneMatch(f -> f.name().equals("V-wing"))) {
            problem.accept("leader", "a leader flies a V-wing's tip: the unit flies no V-wing");
        }
        for (int i = 0; i < enemy.attacks().size(); i++) {
            EnemyData.Attack attack = enemy.attacks().get(i);
            if (BeltRules.laserLine(attack)
                    && attack.bullet()
                            .filter(bullet -> content.enemyBasis().knowsBullet(bullet))
                            .isEmpty()) {
                problem.accept("attacks[" + i + "].bullet", "a laser-line's bullet class is unknown");
            }
            boolean diamond = attack.homing()
                    .filter(homing ->
                            homing.look().isPresent() || homing.seekable().orElse(false))
                    .isPresent();
            if (diamond && enemy.firstLevel() < firstLevel(content)) {
                problem.accept(
                        "attacks[" + i + "].homing",
                        "the homing diamond (look, seekable) is the Ascendancy's from Act 5 (Level "
                                + firstLevel(content) + ")");
            }
        }
        if (enemy.shield().isPresent() && enemy.firstLevel() < firstLevel(content)) {
            problem.accept("shield", "enemy shields come in Act 5 (Level " + firstLevel(content) + ")");
        }
    }
}
