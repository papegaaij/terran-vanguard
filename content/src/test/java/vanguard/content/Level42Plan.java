package vanguard.content;

import java.util.List;
import vanguard.sim.Armament;
import vanguard.sim.Loadout;

/**
 * M10 part H, step H2's stand-in (step H3 moves it into Level42Test, as Levels 40's and 41's): the balance plan's fit at
 * Level 42 ({@code "42"} in design/player/balance-plan.yaml: the visit raises the right Micro-missile Pod to L5 and the
 * Tesla Coil Pod to L3, stows the IFF spoofer and fits Rook's Autocannon) for the level report and the pacing measure:
 * the Ion Beam L5, the Tesla Coil Pod L3, the right Micro-missile Pod L5, the Swarm Tail L3, the Shield Mk III,
 * Composite V, the Crucible's 25 MW with 23 fitted, the EMP Burst, and Rook with his Autocannon in the slot.
 */
final class Level42Plan {
    private Level42Plan() {}

    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-42-ascendant";

    /** The plan's fitted load at Level 42, MW (the spoofer stowed). */
    static final double LOAD = 23;

    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        String shield = content.shields().models().get(2).name();
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        shield,
                        content.armour().plating().get(5).name(),
                        Level40Test.OUTPUT - LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(content, Level40Test.OUTPUT, LOAD, shield, difficulty));
    }
}
