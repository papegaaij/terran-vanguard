package vanguard.game.render;

import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.Sortie;

/**
 * M7 part F: Level 19 (design/campaign Level 19, Foundry of Tharsis) for the game's tests: its data, its script and the
 * level flown with the starter loadout and the ship invulnerable at medium, as content maps it.
 */
public final class FoundryFixture {
    public static final Content CONTENT = WalkerFixture.CONTENT;
    /** Level 19's key. */
    public static final String KEY = WalkerFixture.LEVEL;

    private FoundryFixture() {}

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

    /** The commands that steer the ship toward {@code x} (within 4 px) and fire. */
    public static int toward(Sortie sortie, double x) {
        double shipX = sortie.ship().renderX(1);
        if (shipX < x - 4) {
            return Command.of(Command.RIGHT, Command.FIRE);
        }
        if (shipX > x + 4) {
            return Command.of(Command.LEFT, Command.FIRE);
        }
        return Command.of(Command.FIRE);
    }
}
