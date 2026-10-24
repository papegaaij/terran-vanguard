package vanguard.sim;

/**
 * M8 part F: where a lunging unit is (design/enemies/naval/glow-angler; {@link EnemySpec.Lunge}), for the presentation
 * and the tests; {@link #NONE} for any other unit.
 */
public enum LurePhase {
    NONE,
    /** Hanging in the water with its lure, drifting with it (armed or re-arming). */
    DRIFT,
    /** Diving at where the ship was when it triggered (facing it). */
    LUNGE,
    /** Backing away from the ship after its dive (its ring fired, or due while the ship is too close). */
    RETREAT
}
