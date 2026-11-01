package vanguard.sim;

/**
 * M10 part F (design/enemies/bosses/honour-guard, The playbook): what an ace of a {@link Squadron} flies now ({@link
 * AceFlight}). Its moves end back on its station ({@link #HOLD}).
 */
public enum AceMove {
    /** Its first entry from beyond its edge to its station's point. */
    ENTRY,
    /** On its station: the leader's strafe, the dancer's figure-eights, the feinter's side lane. */
    HOLD,
    /** The leader's side-step to the other side of the top band (no fire: its breath). */
    SLIDE,
    /** The dancer's full loop (its spiral's). */
    LOOP,
    /** The dancer's barrel roll past the ship's side (its fan at the roll's middle: the {@link AceLeg#PASS}'s end). */
    ROLL,
    /** The dancer's fast pass across the field (the ribbon is laid along its {@link AceLeg#PASS}). */
    RIBBON,
    /** The feinter's dive at the ship's spot: its {@link AceLeg#RUN}, its {@link AceLeg#PULL_OUT} (its burst), home. */
    DIVE,
    /** The feinter's climb up a side ({@link AceLeg#CLIMB}, its aimed pairs), its loop over and back down. */
    RAKE,
    /** The feinter's climb out of the top edge and across, then a dive from the front. */
    OVERHEAD,
    /** The two flankers' crossing from opposite sides ({@link AceLeg#PASS}, their fans). */
    PINCER,
    /** A dead ace's hull tumbling away down and out. */
    TUMBLE
}
