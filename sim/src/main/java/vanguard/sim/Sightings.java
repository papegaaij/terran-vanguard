package vanguard.sim;

/**
 * The radio's first-sighting cues of a {@link Sortie}: once per attempt, the first pounce (M5 part C),
 * decloak and loop-back (M5 part D), Burrower surfacing (M7 part B) and Herald ring (M7 part D) seen in a step's events start
 * their cues. The boss checkpoint keeps a copy ({@link #copyFrom}).
 */
final class Sightings {
    private final SimEvents events;
    private final Radio radio;
    /** Whether a pounce took off in this attempt (the radio's {@code first-pounce}). */
    private boolean pounced;
    /** M5 part D: whether a unit decloaked in this attempt (the radio's {@code first-decloak}). */
    private boolean decloakSeen;
    /** M5 part D: whether a swarm looped back in this attempt (the radio's {@code first-loop-back}). */
    private boolean loopBackSeen;
    /** M7 part B: whether a Burrower surfaced in this attempt (the radio's {@code first-surfacing}). */
    private boolean surfacingSeen;
    /** M7 part D, round 40 K.a: whether a Herald sang a ring in this attempt (the radio's {@code first-ring}). */
    private boolean ringSeen;

    /** With {@code events} and {@code radio} null: a copy for the boss checkpoint, never stepped. */
    Sightings(SimEvents events, Radio radio) {
        this.events = events;
        this.radio = radio;
    }

    /** The step's first sightings: their cues. */
    void step() {
        if (!pounced && events.count(SimEvents.Type.POUNCE) > 0) {
            pounced = true;
            radio.cue(LevelScript.CueTrigger.FIRST_POUNCE, "");
        }
        if (!decloakSeen && events.count(SimEvents.Type.DECLOAK) > 0) {
            decloakSeen = true;
            radio.cue(LevelScript.CueTrigger.FIRST_DECLOAK, "");
        }
        if (!loopBackSeen && events.count(SimEvents.Type.LOOP_BACK) > 0) {
            loopBackSeen = true;
            radio.cue(LevelScript.CueTrigger.FIRST_LOOP_BACK, "");
        }
        if (!surfacingSeen && events.count(SimEvents.Type.BURROW_SURFACED) > 0) {
            surfacingSeen = true;
            radio.cue(LevelScript.CueTrigger.FIRST_SURFACING, "");
        }
        if (!ringSeen && events.count(SimEvents.Type.RING_SUNG) > 0) {
            ringSeen = true;
            radio.cue(LevelScript.CueTrigger.FIRST_RING, "");
        }
    }

    /** A new attempt: nothing seen. */
    void reset() {
        pounced = false;
        decloakSeen = false;
        loopBackSeen = false;
        surfacingSeen = false;
        ringSeen = false;
    }

    /** Takes over what {@code other} has seen. */
    void copyFrom(Sightings other) {
        pounced = other.pounced;
        decloakSeen = other.decloakSeen;
        loopBackSeen = other.loopBackSeen;
        surfacingSeen = other.surfacingSeen;
        ringSeen = other.ringSeen;
    }

    /** Each sighting only once seen, so the levels without it hash as before. */
    void addTo(StateHash hash) {
        if (pounced) {
            hash.add(1);
        }
        if (decloakSeen || loopBackSeen) {
            // M5 part D: only once a unit decloaked or a swarm looped back, so the other levels hash as before.
            hash.add(decloakSeen ? 1 : 0).add(loopBackSeen ? 1 : 0);
        }
        if (surfacingSeen) {
            // M7 part B: only once a Burrower surfaced, so the other levels hash as before.
            hash.add(1);
        }
        if (ringSeen) {
            // M7 part D: only once a Herald sang a ring, so the other levels hash as before.
            hash.add(1);
        }
    }
}
