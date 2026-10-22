package vanguard.sim;

/**
 * M8 part D: where a unit with a rise is in it (design/enemies/naval/abyss-ray; {@link EnemySpec.Rise}), for the
 * presentation and the tests; {@link #NONE} for any other unit.
 */
public enum RisePhase {
    NONE,
    /** The shadow grows at its spot on {@link Layer#SUB} (the tell): only {@code anti-sub} fire reaches it. */
    SHADOW,
    /** Rising at its spot: on {@code sub} to the middle, on {@code air} after it; it touches nothing. */
    RISE,
    /** Gliding toward the far side on {@code air}, its gun firing. */
    GLIDE,
    /** Sinking as it glides on: on {@code air} to the middle, on {@code sub} after it; gone at its end. */
    SINK
}
