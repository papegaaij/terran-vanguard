package vanguard.game.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Keys;
import org.junit.jupiter.api.Test;
import vanguard.sim.Command;

class FlightCommandsTest {
    private final FakeDevices devices = new FakeDevices();
    private final ActionInput input = new ActionInput(Bindings.defaults());

    @Test
    void holdToFireFiresOnlyWhileFireIsHeld() {
        var settings = ControlSettings.defaults();
        devices.keys.add(Keys.LEFT);
        devices.keys.add(Keys.SHIFT_LEFT);
        input.update(devices);
        assertEquals(Command.of(Command.LEFT, Command.PRECISION), FlightCommands.of(input, settings));

        devices.keys.add(Keys.SPACE);
        input.update(devices);

        assertEquals(Command.of(Command.LEFT, Command.PRECISION, Command.FIRE), FlightCommands.of(input, settings));
    }

    @Test
    void autoFireFiresWithoutHolding() {
        var settings = ControlSettings.defaults().withAutoFire(true);
        input.update(devices);
        assertEquals(Command.FIRE.bit(), FlightCommands.of(input, settings));

        devices.keys.add(Keys.SPACE);
        input.update(devices);

        assertEquals(Command.FIRE.bit(), FlightCommands.of(input, settings), "holding does nothing extra");
    }

    /** M7 part A: the Dash button (V, the left bumper) sends the dash command; the double-tap is the simulation's. */
    @Test
    void theDashButtonSendsTheDashCommand() {
        var settings = ControlSettings.defaults();
        devices.keys.add(Keys.V);
        input.update(devices);
        assertEquals(Command.DASH.bit(), FlightCommands.of(input, settings));
        assertEquals(
                Command.values().length - 1, Command.DASH.ordinal(), "appended last: old recordings keep their bits");

        devices.keys.clear();
        devices.gamepad.add(GamepadControl.LEFT_BUMPER);
        input.update(devices);
        assertEquals(Command.DASH.bit(), FlightCommands.of(input, settings));
    }

    /** M7 part A: Dash's primary slot is the fixed double-tap; its key and button stay remappable. */
    @Test
    void dashKeepsItsDoubleTapAsItsPrimaryInput() {
        assertFalse(Action.DASH.remappable(BindingSlot.PRIMARY));
        assertTrue(Action.DASH.remappable(BindingSlot.ALTERNATIVE));
        assertTrue(Action.DASH.remappable(BindingSlot.GAMEPAD));
        assertEquals(Binding.NO_KEY, Bindings.defaults().get(Action.DASH).primaryKey());
        assertEquals(Keys.V, Bindings.defaults().get(Action.DASH).alternativeKey());
    }
}
