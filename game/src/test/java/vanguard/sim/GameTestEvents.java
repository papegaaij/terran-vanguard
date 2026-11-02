package vanguard.sim;

/**
 * M10 part H (step H4b), for the game's tests only: a step's events written by hand (the sim's {@link SimEvents} is
 * filled by the simulation alone), to play a fight's rare moments (the hull's break, the lifeboat) through the sounds
 * without flying the whole fight.
 */
public final class GameTestEvents {
    private final SimEvents events = new SimEvents(16);

    /** Adds an event at ({@code x}, {@code y}) with {@code value}. */
    public GameTestEvents add(SimEvents.Type type, double x, double y, int value) {
        events.add(type, x, y, value);
        return this;
    }

    /** The events added. */
    public SimEvents events() {
        return events;
    }
}
