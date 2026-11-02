package vanguard.game.screen;

import vanguard.sim.InputRecording;

/**
 * Step m10-f6r, the debug option {@code --replay <file>}: a level start flown by a recording's commands, a step at a
 * time, instead of the player's input (tools/concept/replay records the content autopilot's), for a real-time capture
 * with the game's sound. The level start must be the recording's (the same level, difficulty and fit options): the
 * simulation is deterministic, so the same commands fly the same level. Once the recording runs out the ship gets no
 * commands.
 */
final class ReplayCommands {
    private final int[] commands;
    private int next;
    private boolean ranOut;

    ReplayCommands(InputRecording recording) {
        commands = recording.commands();
    }

    /** The commands of the next step: the recording's, none once it has run out. */
    int next() {
        return next < commands.length ? commands[next++] : 0;
    }

    /** True once, at the first call after the recording's last step was handed out. */
    boolean ranOut() {
        if (ranOut || next < commands.length) {
            return false;
        }
        ranOut = true;
        return true;
    }

    /** The recording's steps. */
    int steps() {
        return commands.length;
    }
}
