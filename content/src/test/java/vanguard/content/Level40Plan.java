package vanguard.content;

import java.util.List;
import vanguard.sim.Armament;
import vanguard.sim.Loadout;

/**
 * M10 part F, step F2: the balance plan's fit at Level 40 ({@code "40"} in design/player/balance-plan.yaml: the visit
 * buys the Mk V+ "Crucible" (25 MW) and the IFF spoofer (2 MW), 24 of 25 MW, and fits Rook's Autocannon) with Rook in
 * the slot: Level 37's guns (the Ion Beam L5, the Tesla Coil Pod L2, the right Micro-missile Pod L4, the Swarm Tail L3),
 * the Shield Mk III, Composite IV, the EMP Burst, 1 MW spare. The spoofer fools nothing here (no turret; the aces are
 * pilots): it is only its load. The stand-in {@link PacingTest} and the level report fly until step F3 moves it into
 * {@code Level40Test}.
 */
final class Level40Plan {
    private Level40Plan() {}

    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-40-honour-guard";

    /** The Crucible's output, MW (design/player/generator). */
    static final double OUTPUT = 25;

    /** The fit's load, MW: Level 37's 22 and the IFF spoofer's 2. */
    static final double LOAD = 24;

    /** The plan's fit with Rook (his Autocannon L1 at full armour). */
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
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "tesla-coil-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "swarm-tail", 3)),
                        shield,
                        content.armour().plating().get(4).name(),
                        OUTPUT - LOAD,
                        difficulty)
                .withSpecial(SimSpecs.special(content, "EMP Burst", 1))
                .withPower(SimSpecs.power(content, OUTPUT, LOAD, shield, difficulty));
    }
}
