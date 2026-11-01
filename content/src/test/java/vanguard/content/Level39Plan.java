package vanguard.content;

import vanguard.sim.Loadout;
import vanguard.sim.WingmanSpec;

/**
 * M10 part E, step E2: the balance plan's fit at Level 39 ({@code "39"} in design/player/balance-plan.yaml: the visit
 * buys nothing, saving for the Crucible and the spoofer; Level 38's fit, 22 of 22 MW) with Rook in the slot flying his
 * owned Mortar (L1) for Io's refinery targets. The stand-in {@link PacingTest} and the level report fly until step E3
 * moves it into {@code Level39Test}.
 */
final class Level39Plan {
    private Level39Plan() {}

    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-39-io-flyby";

    /** The plan's fit with Rook (his Mortar L1 at full armour): Level 38's guns. */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return withoutRook(content, difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "mortar",
                        1,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()));
    }

    /** The plan's fit without Rook ({@code --escort none}): Level 38's. */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level38Test.withoutRook(content, difficulty);
    }
}
