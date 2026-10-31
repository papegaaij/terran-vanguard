package vanguard.content;

import java.util.List;
import vanguard.sim.Armament;
import vanguard.sim.Loadout;

/**
 * M10 part B, step B2: the balance plan's fit at Level 36 ({@code "36"} in design/player/balance-plan.yaml: the Tesla
 * Coil Pod L2 on the left wing in place of the Micro-missile Pod L4, part A's alternative to the hand plan's beam L5,
 * pending the user's review in round 73) with Rook in the slot: the Ion Beam L4, the Tesla Coil Pod L2, the right
 * Micro-missile Pod L4, the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP Burst, 21 of 22 MW. The stand-in
 * {@link PacingTest} and the level report fly until step B3 moves it into {@code Level36Test}.
 */
final class Level36Plan {
    private Level36Plan() {}

    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-36-cloud-divers";

    /** The plan's fit's load, MW (the Mk V "Helix" gives 22). */
    static final double LOAD = 21;

    /** The plan's fit with Rook (his Autocannon L1 at full armour). */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}). */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        content.shields().models().get(2).name(),
                        content.armour().plating().get(4).name(),
                        Level32Test.SPARE_POWER,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(
                        content,
                        Level35Test.OUTPUT,
                        LOAD,
                        content.shields().models().get(2).name(),
                        difficulty));
    }
}
