package vanguard.game.render;

import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.Sortie;

/**
 * M8 part C: Level 23 (design/campaign Level 23, Through the Ice) for the game's tests: its data, its script and the
 * level flown at medium with the starter loadout and the ship invulnerable.
 */
public final class ThroughIceFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 23's key. */
    public static final String KEY = CONTENT.levelKey(23).orElseThrow();

    private ThroughIceFixture() {}

    public static LevelData level() {
        return CONTENT.levels().get(KEY);
    }

    public static LevelScript script(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, KEY, difficulty);
    }

    /** The level flown at medium with the starter loadout and the ship invulnerable. */
    public static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                script(Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** The first unit of the slug on the screen, or null. */
    public static Enemy first(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (enemy.spec().slug().equals(slug) && y > 0 && y < vanguard.sim.PlayField.HEIGHT) {
                return enemy;
            }
        }
        return null;
    }
}
