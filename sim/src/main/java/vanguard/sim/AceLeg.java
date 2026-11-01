package vanguard.sim;

/**
 * M10 part F: which leg of its move an ace of a {@link Squadron} flies ({@link AceFlight}), so the playbook times its
 * fire on it: a dive's burst as its pull-out starts, the rake's pairs over its climb, the ribbon and the pincer's fans
 * over their pass.
 */
public enum AceLeg {
    /** Its first entry. */
    ENTRY,
    /** A leg of its station (a strafe, an eight's circle, the lane). */
    HOLD,
    /** A wait on its station (the feinter between its hold points). */
    WAIT,
    /** To a move's start (the ribbon's edge, the pincer's side, the overhead's run above the top edge). */
    APPROACH,
    /** A dive's run along its locked line at the ship's spot. */
    RUN,
    /** A dive's pull-out turn. */
    PULL_OUT,
    /** A pass: the slide, the roll to its middle, the ribbon across, the pincer's crossing. */
    PASS,
    /** The rake's and the overhead's climb up a side. */
    CLIMB,
    /** A loop: the dancer's full loop, the rake's turn over the top. */
    TURN,
    /** Back to its station. */
    RETURN
}
