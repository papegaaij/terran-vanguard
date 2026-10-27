package vanguard.content;

import java.util.List;
import java.util.Map;

/**
 * The checks across files, after every file has parsed: names resolve (a wave's enemy and
 * formation, an attack's bullet class, an item's availability), starters come first in their
 * lists, and each level's script fits its sections and its backdrop follows the art direction
 * ({@link BackdropCheck}). The checks live per concern ({@link CampaignChecks}, {@link EnemyChecks},
 * {@link BossChecks}, {@link LevelChecks}, {@link EscortChecks} and the parts' record checks); this
 * class runs them in their order, which is the order of the problem list.
 */
final class ContentValidator {
    private final Content content;
    private final Problems sink;
    private final CampaignChecks campaignChecks;
    private final EnemyChecks enemyChecks;
    private final LevelChecks levelChecks;

    ContentValidator(Content content, Map<Object, String> paths) {
        this.content = content;
        this.sink = new Problems(paths);
        BossChecks bossChecks = new BossChecks(content, sink);
        this.campaignChecks = new CampaignChecks(content, sink);
        this.enemyChecks = new EnemyChecks(content, sink, bossChecks);
        this.levelChecks = new LevelChecks(content, sink, bossChecks, new EscortChecks(content, sink));
    }

    List<String> problems() {
        campaignChecks.checkItems();
        content.weapons()
                .values()
                .forEach(weapon -> RetreatChecks.weapon(weapon, (field, message) -> problem(weapon, field, message)));
        campaignChecks.checkWingmen();
        campaignChecks.checkSmallActHp();
        campaignChecks.checkBeamEraHp();
        content.enemies().values().forEach(enemyChecks::checkEnemy);
        content.levels().values().forEach(levelChecks::checkLevel);
        content.levels().forEach(campaignChecks::checkAct);
        // M8 part C: the units' under-water keys (the Eel Swarm's, the Glassfin's, the returning units') and a level's.
        content.enemies()
                .values()
                .forEach(enemy ->
                        DeepWaterChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message)));
        content.levels()
                .values()
                .forEach(level ->
                        DeepWaterChecks.level(content, level, (field, message) -> problem(level, field, message)));
        // M8 part D: the rising unit and the hover above the ship (the Abyss Ray's, the Barbfin's) and a level's.
        content.enemies()
                .values()
                .forEach(
                        enemy -> AbyssChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message)));
        content.levels()
                .values()
                .forEach(
                        level -> AbyssChecks.level(content, level, (field, message) -> problem(level, field, message)));
        // M8 part E: the coil and the slowing ring (the Spiral Nautilus's, the Siren's) and a level's caves and nests.
        content.enemies()
                .values()
                .forEach(enemy -> TrenchChecks.enemy(enemy, (field, message) -> problem(enemy, field, message)));
        content.levels()
                .values()
                .forEach(level ->
                        TrenchChecks.level(content, level, (field, message) -> problem(level, field, message)));
        // M8 part F: the lunge (the Glow Angler's) and a level's dark keys, Rook's loss and centre carrier.
        content.enemies()
                .values()
                .forEach(enemy -> DarkChecks.enemy(enemy, (field, message) -> problem(enemy, field, message)));
        content.levels()
                .values()
                .forEach(level -> DarkChecks.level(content, level, (field, message) -> problem(level, field, message)));
        // M8 part G: the heat plumes, a column from the rear, the torpedoes along a unit's facing, the ground nests.
        content.enemies()
                .values()
                .forEach(enemy -> VentChecks.enemy(enemy, (field, message) -> problem(enemy, field, message)));
        content.levels()
                .values()
                .forEach(level -> VentChecks.level(content, level, (field, message) -> problem(level, field, message)));
        // M8 part A: the under-water rules, Rook's absence and a level's medium and special limits.
        ActFourChecks.europa(content, (field, message) -> problem(content.europa(), field, message));
        ActFourChecks.wingmen(content, (field, message) -> problem(content.wingmen(), field, message));
        content.levels()
                .forEach((key, level) -> ActFourChecks.level(
                        content, Content.levelNumber(key), level, (field, message) -> problem(level, field, message)));
        return sink.list();
    }

    private void problem(Object file, String field, String message) {
        sink.add(file, field, message);
    }
}
