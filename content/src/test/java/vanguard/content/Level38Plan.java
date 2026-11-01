package vanguard.content;

import vanguard.sim.Loadout;

/**
 * M10 part D, step D2: the balance plan's fit at Level 38 ({@code "38"} in design/player/balance-plan.yaml: the visit
 * buys nothing, saving for the Crucible; Level 37's fit, 22 of 22 MW) with Rook in the slot: the Ion Beam L5, the Tesla
 * Coil Pod L2, the right Micro-missile Pod L4, the Swarm Tail L3, the Shield Mk III, Composite IV, the EMP Burst. The
 * stand-in {@link PacingTest} and the level report fly until step D3 moves it into {@code Level38Test}.
 */
final class Level38Plan {
    private Level38Plan() {}

    /** The level's key. */
    static final String LEVEL = "act-6-jovian-storm/level-38-lightning-belt";

    /** The plan's fit with Rook (his Autocannon L1 at full armour): Level 37's. */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return Level37Test.planLoadout(content, difficulty);
    }

    /** The plan's fit without Rook ({@code --escort none}): Level 37's. */
    static Loadout withoutRook(Content content, Difficulty difficulty) {
        return Level37Test.withoutRook(content, difficulty);
    }
}
