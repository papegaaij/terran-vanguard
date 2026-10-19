package vanguard.game.input;

import vanguard.sim.Command;

/** Turns the held actions into the simulation's command set for one step. */
public final class FlightCommands {
    private FlightCommands() {}

    /** The commands of the held actions; with auto-fire on, the guns fire without holding fire. */
    public static int of(ActionInput input, ControlSettings settings) {
        int commands = Command.NONE;
        commands |= bit(input, Action.MOVE_UP, Command.UP);
        commands |= bit(input, Action.MOVE_DOWN, Command.DOWN);
        commands |= bit(input, Action.MOVE_LEFT, Command.LEFT);
        commands |= bit(input, Action.MOVE_RIGHT, Command.RIGHT);
        commands |= bit(input, Action.PRECISION, Command.PRECISION);
        commands |= bit(input, Action.SPECIAL, Command.SPECIAL);
        // M7 part A: the Dash button (V, the left bumper); the double-tap of a direction is detected in
        // the simulation from the direction bits, and either does something only with the Evasive
        // thrusters fitted (design/ui/controls).
        commands |= bit(input, Action.DASH, Command.DASH);
        // With auto-fire on, holding fire does nothing extra (design/ui/controls).
        if (settings.autoFire() || input.held(Action.FIRE)) {
            commands |= Command.FIRE.bit();
        }
        return commands;
    }

    private static int bit(ActionInput input, Action action, Command command) {
        return input.held(action) ? command.bit() : Command.NONE;
    }
}
