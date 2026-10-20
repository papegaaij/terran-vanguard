package vanguard.game.render;

import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Flock;
import vanguard.sim.LevelScript;
import vanguard.sim.Sortie;

/**
 * M7 part G: Level 20 (design/campaign Level 20, Retreat from Hellas) for the game's tests: its data, its script and the
 * level flown with the starter loadout and the ship invulnerable at medium, as content maps it (step G4: until its
 * {@code pursuit} waves are mapped the script throws, and the flights that need it fail).
 */
public final class RetreatFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 20's key. */
    public static final String KEY = CONTENT.levelKey(20).orElseThrow();

    private RetreatFixture() {}

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

    /** The pursuing flock in flight, or null for none. */
    public static Flock pursuing(Sortie sortie) {
        for (int f = 0; f < sortie.flockCount(); f++) {
            if (sortie.flock(f).pursuing()) {
                return sortie.flock(f);
            }
        }
        return null;
    }
}
