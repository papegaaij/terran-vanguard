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
        campaignChecks.checkHeavyShields();
        campaignChecks.checkUnlocksFly();
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
        // M9 part A: a level's vacuum and underground keys, a unit's shield, laser-line and homing diamond.
        content.levels()
                .forEach((key, level) -> BeltChecks.level(
                        content, Content.levelNumber(key), level, (field, message) -> problem(level, field, message)));
        // M9 part B (Level 29): the held arena, the halt cycle, the escort swap, leaders, rocks, spared, dormant nests.
        content.levels()
                .forEach((key, level) -> BeltChecks.partB(
                        content, Content.levelNumber(key), level, (field, message) -> problem(level, field, message)));
        content.enemies()
                .values()
                .forEach(enemy -> BeltChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message)));
        // M9 part C (Level 30): ground: none, mine lines and layers, facing armour, Dart streams, turret cars.
        content.levels()
                .forEach((key, level) -> MinefieldChecks.level(
                        content, Content.levelNumber(key), level, (field, message) -> problem(level, field, message)));
        content.enemies()
                .values()
                .forEach(enemy ->
                        MinefieldChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message)));
        // M9 part D (Level 31): the hull escort, its hook and the ground under it, linked waves, dashes, air links.
        content.levels()
                .forEach((key, level) ->
                        TugChecks.level(content, level, (field, message) -> problem(level, field, message)));
        content.enemies()
                .values()
                .forEach(enemy -> TugChecks.enemy(enemy, content, (field, message) -> problem(enemy, field, message)));
        // M9 part E (Level 32): the forks' branches, the towers' and locks' placement, the collar, the topple.
        content.levels()
                .forEach((key, level) ->
                        DeepCoreChecks.level(content, level, (field, message) -> problem(level, field, message)));
        content.enemies()
                .values()
                .forEach(enemy -> DeepCoreChecks.enemy(enemy, (field, message) -> problem(enemy, field, message)));
        // M9 part F (Level 33): the power-draining latch, the drift, prey wings and their hunters.
        content.levels()
                .forEach((key, level) ->
                        LeechChecks.level(content, level, (field, message) -> problem(level, field, message)));
        content.enemies()
                .values()
                .forEach(enemy -> LeechChecks.enemy(enemy, (field, message) -> problem(enemy, field, message)));
        // M9 part G (Level 34): object escapes, the story pickup and its cues, a unit's livery.
        content.levels()
                .forEach((key, level) ->
                        SalvageChecks.level(content, level, (field, message) -> problem(level, field, message)));
        content.enemies()
                .values()
                .forEach(enemy ->
                        SalvageChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message)));
        // M9 part H (Level 35): a ringed boss's rings, fire arc, locks, batteries and core; a level's placement of it.
        content.enemies()
                .values()
                .forEach(enemy -> RingChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message)));
        content.levels()
                .values()
                .forEach(level -> RingChecks.level(content, level, (field, message) -> problem(level, field, message)));
        // M10 part A (Act 6): mirrors, regrowth, domes, death-bursts and the spoofer's flag; prey links and crowns.
        content.enemies()
                .values()
                .forEach(enemy ->
                        JovianChecks.enemy(content, enemy, (field, message) -> problem(enemy, field, message)));
        content.levels()
                .values()
                .forEach(level ->
                        JovianChecks.level(content, level, (field, message) -> problem(level, field, message)));
        // M10 part A, step A2b: a level's strikes, eruptions, foreground banks, held gates and turn.
        content.levels()
                .values()
                .forEach(level ->
                        JovianHazardChecks.level(content, level, (field, message) -> problem(level, field, message)));
        return sink.list();
    }

    private void problem(Object file, String field, String message) {
        sink.add(file, field, message);
    }
}
