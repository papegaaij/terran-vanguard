package vanguard.sim;

/**
 * M8 part E: where a coiling unit is in its cycle (design/enemies/naval/spiral-nautilus; {@link EnemySpec.Coil}), for
 * the presentation and the tests; {@link #NONE} for any other unit.
 */
public enum CoilPhase {
    NONE,
    /** Its shell closed (armoured), spiralling in round the ship, or breaking outward after a close. */
    CLOSED,
    /** Uncoiling at the inner radius: still armoured; its crown glows (the tell) the last part of it. */
    OPENING,
    /** Uncoiled: its crown is hittable and its spiral fires. */
    OPEN,
    /** Its wave's hold is over: closed, it breaks off toward the nearer side and leaves the play field. */
    LEAVING
}
