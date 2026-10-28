package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * M9 part C's static checks (design/campaign Level 30; design/enemies/space/minelayer, air/gilded-gunship,
 * air/rail-serpent, air/dart-drone).
 *
 * <p>A unit's: a {@code lay} path goes with a mine attack whose mine has a {@code line}, on an {@code air} unit; a line
 * mine's blast bullet is a known class; facing armour ({@code armour.factor}) only on a unit that is not a walker or a
 * boss; a stream spawn launches a known plain air unit; a train's turret {@code cars} fire one of the unit's attacks by
 * name (an aimed shot or a fan) and its cut has its parts die.
 *
 * <p>A level's: {@code ground: none} only from Act 5, with nothing on the ground (no ground targets, road, convoy,
 * water, terrain, allies, belts, vents, ridge, no-surface zones, holds or sleds) and no wave of a ground unit; a wave's
 * {@code mine_lines} only on a mine layers' wave from the rear, and a mine layers' wave from the rear.
 */
final class MinefieldChecks {
    private MinefieldChecks() {}

    /** A unit's part-C keys. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        boolean lineAttack = false;
        for (int i = 0; i < enemy.attacks().size(); i++) {
            EnemyData.Attack attack = enemy.attacks().get(i);
            String field = "attacks[" + i + "]";
            Optional<EnemyData.Mine> mine = attack.mine();
            if (mine.isPresent() && mine.get().line().isPresent()) {
                lineAttack = true;
            }
            mine.flatMap(EnemyData.Mine::blast)
                    .filter(blast -> !content.enemyBasis().knowsBullet(blast.bullet()))
                    .ifPresent(blast -> problem.accept(
                            field + ".mine.blast.bullet", "unknown bullet class '" + blast.bullet() + "'"));
            Optional<EnemyData.Stream> stream = attack.spawn().flatMap(EnemyData.Spawn::stream);
            if (stream.isPresent()) {
                EnemyData launched = content.enemies().get(attack.spawn().get().enemy());
                if (launched != null
                        && (!launched.layer().equals("air")
                                || launched.segmentChain().isPresent()
                                || launched.attacks().stream().anyMatch(a -> a.spawn()
                                        .flatMap(EnemyData.Spawn::stream)
                                        .isPresent()))) {
                    problem.accept(field + ".spawn", "a stream launches a plain air unit (no chain, no streams)");
                }
            }
        }
        if (enemy.movement().lay().isPresent() != lineAttack) {
            problem.accept("movement.lay", "a mine layer's lay path goes with its mine attack's line, and only so");
        }
        if (enemy.movement().lay().isPresent() && !enemy.layer().equals("air")) {
            problem.accept("movement.lay", "a mine layer flies on the air layer");
        }
        if (enemy.armour().facing()
                && (enemy.movement().walk().isPresent()
                        || enemy.walks()
                        || enemy.boss().isPresent())) {
            problem.accept("armour.factor", "facing armour is a flying unit's (a walker's armour glances)");
        }
        enemy.segmentChain().flatMap(EnemyData.SegmentChain::cars).ifPresent(cars -> {
            List<EnemyData.Attack> named = enemy.attacks().stream()
                    .filter(a -> a.name().filter(cars.attack()::equals).isPresent())
                    .toList();
            if (named.size() != 1
                    || !(named.getFirst().pattern().equals("aimed")
                            || named.getFirst().pattern().equals("fan"))) {
                problem.accept(
                        "segment_chain.cars.attack",
                        "a turret car fires one of the unit's attacks by name, an aimed shot or a fan: '"
                                + cars.attack() + "'");
            }
            if (enemy.segmentChain().get().cut().isEmpty()) {
                problem.accept("segment_chain.cars", "a train's cut parts die (cut), they never regrow");
            }
        });
    }

    /** A level's part-C keys. */
    static void level(Content content, int number, LevelData level, BiConsumer<String, String> problem) {
        if (level.noGround()) {
            if (number < BeltChecks.firstLevel(content)) {
                problem.accept("ground", "ground: none only from Act 5 (Level " + BeltChecks.firstLevel(content) + ")");
            }
            boolean onGround = !level.groundTargets().isEmpty()
                    || level.road().isPresent()
                    || level.convoy().isPresent()
                    || level.water().orElse(false)
                    || level.terrain().isPresent()
                    || level.allies().isPresent()
                    || level.belts().isPresent()
                    || level.vents().isPresent()
                    || level.ridge().isPresent()
                    || level.noSurface().isPresent()
                    || level.holds().isPresent()
                    || level.sleds().isPresent();
            if (onGround) {
                problem.accept(
                        "ground",
                        "a level without a ground layer has no ground targets, road, convoy, water, terrain, allies,"
                                + " belts, vents, ridge, no-surface zones, holds or sleds");
            }
        }
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            String field = "waves[" + i + "]";
            for (String slug : slugs(wave)) {
                EnemyData enemy = content.enemies().get(slug);
                if (enemy == null) {
                    continue;
                }
                if (level.noGround() && grounded(enemy)) {
                    problem.accept(field, "a level without a ground layer flies no ground unit: " + slug);
                }
                boolean layer = enemy.movement().lay().isPresent();
                if (layer && wave.from() != LevelData.Entry.REAR) {
                    problem.accept(field, "mine layers come from the rear: " + slug);
                }
                if (wave.mineLines().isPresent() && !layer) {
                    problem.accept(field + ".mine_lines", "only a mine layers' wave lays mine lines: " + slug);
                }
            }
        }
    }

    private static List<String> slugs(LevelData.Wave wave) {
        if (wave.enemy().isPresent()) {
            return List.of(wave.enemy().get());
        }
        return wave.groups()
                .map(groups -> groups.stream().map(WavesBlock.Group::enemy).toList())
                .orElse(List.of());
    }

    /** Whether the unit lives on the ground layer (terrain, walkers, crawlers, anchors). */
    private static boolean grounded(EnemyData enemy) {
        EnemyData.Movement movement = enemy.movement();
        return enemy.layer().equals("ground")
                || movement.terrain().isPresent()
                || movement.walk().isPresent()
                || movement.crawl().isPresent()
                || movement.anchored().isPresent();
    }
}
