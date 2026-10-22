package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * M8 part C's static checks (design/campaign Level 23; design/enemies/naval, the Eel Swarm, the Glassfin and the
 * returning units under water).
 *
 * <p>A unit's: a part fires a list of {@code attacks} only as a segment chain's head (its vital part); a scattering cut
 * only on an air chain; a path's {@code sway} only on a segment chain's head; a flock's {@code ram} hook only on a unit
 * whose flock rams; the {@code underwater} block's {@code submerge: false} only on a unit that submerges, its {@code
 * drift} only on a {@code terrain} unit, its {@code look: floor} only on a ground unit. A level's: an {@code
 * underwater_look} only on a level that is under water somewhere, a section's change only on an under-water section; a pickup's {@code
 * dropped_by.head_first} only on the first unit of a wave of a segment chain.
 */
final class DeepWaterChecks {
    private DeepWaterChecks() {}

    /** A unit's M8 part C keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        List<EnemyData.PartData> parts = enemy.partList().orElse(List.of());
        for (int i = 0; i < parts.size(); i++) {
            EnemyData.PartData part = parts.get(i);
            if (part.attacks().isPresent()
                    && (enemy.segmentChain().isEmpty() || !part.kind().equals("vital"))) {
                problem.accept(
                        "part_list[" + i + "].attacks", "only a segment chain's head (its vital part) fires a list");
            }
        }
        enemy.segmentChain().flatMap(EnemyData.SegmentChain::cut).ifPresent(cut -> {
            if (cut.scatters() && !enemy.layer().equals("air")) {
                problem.accept("segment_chain.cut.scatter", "only an air chain's cut scatters (a ground chain's dies)");
            }
        });
        enemy.movement().path().flatMap(EnemyData.PathMove::sway).ifPresent(sway -> {
            if (enemy.segmentChain().isEmpty()) {
                problem.accept("movement.path.sway", "only a segment chain's head sways about its path");
            }
        });
        boolean rams = enemy.movement().flock().flatMap(EnemyData.Flock::ram).isPresent();
        enemy.difficulty().ifPresent(hooks -> {
            hooks.easy().flatMap(EnemyData.Hook::ram).ifPresent(change -> {
                if (!rams) {
                    problem.accept("difficulty.easy.ram", "a ram hook needs a flock that rams (movement.flock.ram)");
                }
            });
            hooks.hard().flatMap(EnemyData.Hook::ram).ifPresent(change -> {
                if (!rams) {
                    problem.accept("difficulty.hard.ram", "a ram hook needs a flock that rams (movement.flock.ram)");
                }
            });
        });
        enemy.underwater().ifPresent(water -> {
            if (water.submerge().isPresent() && enemy.submerge().isEmpty()) {
                problem.accept("underwater.submerge", "only a unit that submerges drops its swap");
            }
            if (water.drift().isPresent() && enemy.movement().terrain().isEmpty()) {
                problem.accept("underwater.drift", "only a terrain unit drifts (movement.terrain)");
            }
            if (water.look().isPresent() && !enemy.layer().equals("ground")) {
                problem.accept("underwater.look", "a floor base only under a ground unit");
            }
            if (water.layer().isEmpty()
                    && water.submerge().isEmpty()
                    && water.drift().isEmpty()
                    && water.look().isEmpty()) {
                problem.accept("underwater", "changes nothing (leave it out)");
            }
        });
    }

    /** A level's M8 part C keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (!level.underwater() && level.underwaterLook().isPresent()) {
            problem.accept("underwater_look", "only on a level that is under water somewhere");
        }
        for (int i = 0; i < level.sections().size(); i++) {
            if (level.sections().get(i).underwaterLook().isPresent()
                    && level.mediumOf(i) != LevelData.Medium.UNDERWATER) {
                problem.accept("sections[" + i + "].underwater_look", "only on a section under water");
            }
            if (level.sections().get(i).underwaterLook().isPresent()
                    && level.underwaterLook().isEmpty()) {
                problem.accept("sections[" + i + "].underwater_look", "changes the level's underwater_look: give it");
            }
        }
        List<LevelData.PlacedPickup> pickups = level.pickups();
        for (int i = 0; i < pickups.size(); i++) {
            LevelData.PlacedPickup pickup = pickups.get(i);
            if (!pickup.carried() || !pickup.carrier().dropsHeadFirst()) {
                continue;
            }
            String field = "pickups[" + i + "].dropped_by.head_first";
            LevelData.Carrier carrier = pickup.carrier();
            if (carrier.unit() != LevelData.CarrierUnit.FIRST) {
                problem.accept(field, "a head-first drop is the wave's first unit's");
            }
            Optional<LevelData.Wave> wave =
                    level.waves().stream().filter(w -> w.t() == carrier.waveT()).findFirst();
            boolean chain = wave.isPresent()
                    && wave.get().groupList().stream()
                            .map(group -> content.enemies().get(group.enemy()))
                            .anyMatch(enemy ->
                                    enemy != null && enemy.segmentChain().isPresent());
            if (!chain) {
                problem.accept(field, "only a segment chain's wave drops on a head-first kill");
            }
        }
    }
}
