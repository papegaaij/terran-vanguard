package vanguard.game.render;

import java.util.List;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.Sortie;
import vanguard.sim.WingmanSpec;

/**
 * M8 part F: Level 26 (design/campaign Level 26, Blackwater) for the game's tests: its data, its script and the level
 * flown at medium on the balance plan's fit (Harpoon Torpedoes L4, two Micro-Missile Pods L3, Depth Charges L3, the
 * Sonar Pulse, the Pressure hull) with Rook along (or not) and the ship invulnerable.
 */
public final class BlackwaterFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 26's key. */
    public static final String KEY = CONTENT.levelKey(26).orElseThrow();

    private BlackwaterFixture() {}

    public static LevelData level() {
        return CONTENT.levels().get(KEY);
    }

    public static LevelScript script(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, KEY, difficulty);
    }

    /** The plan's fit, with Rook (his autocannon at level 1, on the left) when {@code rook}. */
    public static Loadout loadout(boolean rook) {
        Loadout loadout = SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "harpoon-torpedoes", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "depth-charges", 3)),
                        CONTENT.shields().models().get(1).name(),
                        CONTENT.armour().plating().get(3).name(),
                        0,
                        Difficulty.MEDIUM)
                .withSpecial(SimSpecs.special(CONTENT, "Sonar Pulse", 1))
                .withPressureHull();
        return rook
                ? loadout.withWingman(SimSpecs.wingman(
                        CONTENT,
                        "autocannon",
                        1,
                        WingmanSpec.Side.LEFT,
                        CONTENT.wingmen().rook().armour()))
                : loadout;
    }

    /** The level flown at medium on the plan's fit with Rook along (or not), the ship invulnerable. */
    public static Sortie sortie(boolean rook) {
        Loadout loadout = loadout(rook);
        return new Sortie(
                1,
                loadout,
                script(Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                loadout.plating().maxArmour());
    }
}
