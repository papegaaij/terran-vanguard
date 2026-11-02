package vanguard.content;

import java.util.List;
import vanguard.sim.Armament;
import vanguard.sim.Loadout;
import vanguard.sim.WingmanSpec;

/**
 * M10 part G, step G2's stand-in (step G3 moves it into Level41Test, as Level 40's): the balance plan's fit at Level 41
 * ({@code "41"} in design/player/balance-plan.yaml: the visit buys Composite V and fits Rook's Mortar) for the level
 * report and the pacing measure: Level 40's guns (the Ion Beam L5, the Tesla Coil Pod L2, the right Micro-missile Pod
 * L4, the Swarm Tail L3), the Shield Mk III, Composite V, the Crucible's 25 MW with 24 fitted, the IFF spoofer fitted
 * (it fools the fortress's guns and the Halo's), the EMP Burst, and Rook with his Mortar L1 in the slot.
 */
final class Level41Plan {
    private Level41Plan() {}

    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-41-callisto-fortress";

    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "mortar",
                        1,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        String shield = content.shields().models().get(2).name();
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        shield,
                        content.armour().plating().get(5).name(),
                        Level40Test.OUTPUT - Level40Test.LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(content, Level40Test.OUTPUT, Level40Test.LOAD, shield, difficulty))
                .withSpoofer(LoadoutRules.spoofDelay(content));
    }
}
