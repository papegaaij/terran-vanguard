package vanguard.game.render;

import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;

/**
 * M8 part E: Level 25 (design/campaign Level 25, Siren Trench) for the game's tests: its data, its script and the level
 * flown at medium with the starter loadout and the ship invulnerable.
 */
public final class SirenTrenchFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 25's key. */
    public static final String KEY = CONTENT.levelKey(25).orElseThrow();

    private SirenTrenchFixture() {}

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
            if (enemy.spec().slug().equals(slug) && y > 0 && y < PlayField.HEIGHT) {
                return enemy;
            }
        }
        return null;
    }

    /** The commands that hold the ship at (x, y) without firing. */
    public static int hold(Sortie sortie, double x, double y) {
        return IceShelfFixture.toward(sortie, x, y) & ~vanguard.sim.Command.FIRE.bit();
    }
}
