package vanguard.game.render;

import java.util.List;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Sortie;

/**
 * M8 part B: Level 22 (design/campaign Level 22, Ice Shelf) for the game's tests: its data, its script and the level
 * flown at medium with the ship invulnerable, with the starter loadout or with bomb racks on both wings (anti-ground:
 * the Drill Rigs' hardened shells take their bombs).
 */
public final class IceShelfFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 22's key. */
    public static final String KEY = CONTENT.levelKey(22).orElseThrow();

    private IceShelfFixture() {}

    public static LevelData level() {
        return CONTENT.levels().get(KEY);
    }

    public static LevelScript script(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, KEY, difficulty);
    }

    /** The level flown at medium with the starter loadout and the ship invulnerable. */
    public static Sortie sortie() {
        return sortie(SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM));
    }

    /** The level flown at medium with the Pulse Cannon and a bomb rack on each wing, the ship invulnerable. */
    public static Sortie bomber() {
        return sortie(SimSpecs.loadout(
                CONTENT,
                CONTENT.systems().engines().getFirst().name(),
                List.of(
                        new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 3),
                        new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "bomb-rack", 3),
                        new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "bomb-rack", 3)),
                CONTENT.shields().models().getFirst().name(),
                CONTENT.armour().plating().getFirst().name(),
                0,
                Difficulty.MEDIUM));
    }

    private static Sortie sortie(Loadout loadout) {
        return new Sortie(
                1,
                loadout,
                script(Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** The commands that steer the ship toward {@code x} and {@code y} (within 4 px) and fire. */
    public static int toward(Sortie sortie, double x, double y) {
        double shipX = sortie.ship().renderX(1);
        double shipY = sortie.ship().renderY(1);
        int commands = Command.FIRE.bit();
        if (shipX < x - 4) {
            commands |= Command.RIGHT.bit();
        } else if (shipX > x + 4) {
            commands |= Command.LEFT.bit();
        }
        if (shipY < y - 4) {
            commands |= Command.UP.bit();
        } else if (shipY > y + 4) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }

    /** The first Drill Rig on the screen, or null. */
    public static Enemy rig(Sortie sortie) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().slug().equals(DrillRigLooks.SLUG)) {
                return enemy;
            }
        }
        return null;
    }
}
