package vanguard.game.render;

import vanguard.sim.Pits;
import vanguard.sim.SimStep;

/**
 * M11 part C, step C4 (design/world/vrell-space, Living terrain; design/campaign Level 45; tools/art/act7_reef.py's
 * {@code pit}): the frame of each of the level's spawning pits by its {@link Pits} state, for the backdrop's placed
 * {@code pit} pieces (the level's pits in their order): shut, breathing ({@value #SHUT_FRAMES} frames at {@value
 * #SHUT_FPS} fps, a loop); its tell over 2 s (frames 8–15 by how far it has run; held, frame 15); open (frames 16–19 at
 * {@value #OPEN_FPS} fps, a loop); closing (frames 20–23 over {@value #CLOSE_SECONDS} s), then shut again. The states
 * are followed after each step (the step each began); the frames go by the level's steps, the shut loop by the real
 * clock as the data's loop. Fixed arrays: nothing is allocated per frame.
 */
final class PitFrames {
    static final int SHUT_FRAMES = 8;
    static final double SHUT_FPS = 2;
    static final int TELL_FIRST = 8;
    static final int TELL_FRAMES = 8;
    static final int OPEN_FIRST = 16;
    static final int OPEN_FRAMES = 4;
    static final double OPEN_FPS = 4;
    static final int CLOSE_FIRST = 20;
    static final int CLOSE_FRAMES = 4;
    static final double CLOSE_SECONDS = 0.5;
    /** The frames of a pit's art: shut, tell, open and closing. */
    static final int FRAMES = CLOSE_FIRST + CLOSE_FRAMES;

    private final Pits.State[] states;
    /** Per pit the step its state began, and its tell's share at the last step. */
    private final long[] since;

    private final double[] tell;
    private long tick;

    PitFrames(int pits) {
        states = new Pits.State[pits];
        since = new long[pits];
        tell = new double[pits];
        restart();
    }

    /** The number of pits followed. */
    int count() {
        return states.length;
    }

    /** A new attempt: every pit shut. */
    void restart() {
        java.util.Arrays.fill(states, Pits.State.SHUT);
        java.util.Arrays.fill(since, 0);
        java.util.Arrays.fill(tell, 0);
        tick = 0;
    }

    /** After each step {@code tick}: each pit's state, and when it began. */
    void stepped(Pits pits, long tick) {
        this.tick = tick;
        int n = Math.min(states.length, pits.count());
        for (int p = 0; p < n; p++) {
            Pits.State state = pits.state(p);
            if (state != states[p]) {
                states[p] = state;
                since[p] = tick;
            }
            tell[p] = pits.tell(p, 1);
        }
    }

    /** Pit {@code pit}'s frame now, at real time {@code real} (the shut loop's clock). */
    int frame(int pit, double real) {
        return frame(states[pit], tell[pit], tick - since[pit], real);
    }

    /**
     * The frame of a pit in {@code state}, its tell run {@code tell} (0 to 1), {@code steps} steps after the state began,
     * at real time {@code real}.
     */
    static int frame(Pits.State state, double tell, long steps, double real) {
        return switch (state) {
            case TELL -> TELL_FIRST + Math.clamp((int) Math.floor(tell * TELL_FRAMES), 0, TELL_FRAMES - 1);
            case HELD -> TELL_FIRST + TELL_FRAMES - 1;
            case OPEN ->
                OPEN_FIRST + Math.floorMod((long) Math.floor(steps * OPEN_FPS / SimStep.PER_SECOND), OPEN_FRAMES);
            case CLOSED -> {
                int closing = (int) Math.floor(steps * CLOSE_FRAMES / (CLOSE_SECONDS * SimStep.PER_SECOND));
                yield closing < CLOSE_FRAMES ? CLOSE_FIRST + closing : shut(real);
            }
            case SHUT -> shut(real);
        };
    }

    /** The shut pit's breathing loop at real time {@code real}. */
    static int shut(double real) {
        return Math.floorMod((long) Math.floor(real * SHUT_FPS), SHUT_FRAMES);
    }
}
