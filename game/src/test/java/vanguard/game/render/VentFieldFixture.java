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
 * M8 part G: Level 27 (design/campaign Level 27, Hydrothermal Vents) for the game's tests: its data, its script and the
 * level flown at medium on the balance plan's fit (Harpoon Torpedoes L4, two Micro-Missile Pods L3, the Tail Gun L3 on
 * the rear, the Sonar Pulse, the Pressure hull) with the loaner Bulwark in the escort slot (or none), the ship
 * invulnerable.
 */
public final class VentFieldFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 27's key. */
    public static final String KEY = CONTENT.levelKey(27).orElseThrow();
    /** The Mk IV's spare power at the plan's L27 visit (15.5 of 20 MW). */
    static final double SPARE_POWER = 4.5;

    private VentFieldFixture() {}

    public static LevelData level() {
        return CONTENT.levels().get(KEY);
    }

    public static LevelScript script(Difficulty difficulty) {
        return SimSpecs.level(CONTENT, KEY, difficulty);
    }

    /** The plan's fit, with the loaner Bulwark (on the left) when {@code bulwark}. */
    public static Loadout loadout(boolean bulwark) {
        Loadout loadout = SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "harpoon-torpedoes", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "tail-gun", 3)),
                        CONTENT.shields().models().get(1).name(),
                        CONTENT.armour().plating().get(3).name(),
                        SPARE_POWER,
                        Difficulty.MEDIUM)
                .withSpecial(SimSpecs.special(CONTENT, "Sonar Pulse", 1))
                .withPressureHull();
        return bulwark
                ? loadout.withBulwark(SimSpecs.bulwark(
                        CONTENT,
                        WingmanSpec.Side.LEFT,
                        CONTENT.wingmen().bulwark().armour()))
                : loadout;
    }

    /** The level flown at medium on the plan's fit with the Bulwark (or not), the ship invulnerable. */
    public static Sortie sortie(boolean bulwark) {
        Loadout loadout = loadout(bulwark);
        return new Sortie(
                1,
                loadout,
                script(Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                loadout.plating().maxArmour());
    }
}
