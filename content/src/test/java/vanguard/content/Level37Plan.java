package vanguard.content;

import java.util.List;
import vanguard.sim.Armament;
import vanguard.sim.Loadout;

/**
 * M10 part C, step C2: the balance plan's fit at Level 37 ({@code "37"} in design/player/balance-plan.yaml: the Ion
 * Beam to L5 on Level 36's fit, 22 of 22 MW, forward DPS 168 against the reference 148) with Rook in the slot: the Ion
 * Beam L5, the Tesla Coil Pod L2, the right Micro-missile Pod L4, the Swarm Tail L3, the Shield Mk III, Composite IV,
 * the EMP Burst. The stand-in {@link PacingTest} and the level report fly until step C3 moves it into {@code
 * Level37Test}.
 */
final class Level37Plan {
    private Level37Plan() {}

    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-37-fall-of-aurelia";

    /** The plan's fit's load, MW (the Mk V "Helix" gives 22): the beam's fifth level takes the last one. */
    static final double LOAD = 22;

    /** The plan's fit with Rook (his Autocannon L1 at full armour). */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty).withWingman(Level31Test.rook(content));
    }

    /** The plan's fit without Rook ({@code --escort none}): Level 36's with the Ion Beam at L5. */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
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
