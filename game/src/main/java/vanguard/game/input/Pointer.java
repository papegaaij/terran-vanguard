package vanguard.game.input;

import com.badlogic.gdx.Input.Buttons;
import vanguard.game.ui.Box;

/**
 * The mouse as the out-of-game screens read it in a frame (design/ui, Mouse): where the pointer is
 * on the pixel screen, whether it moved, the left click (confirm), the right click and the mouse's
 * back button (back), the left button held (a slider's drag) and the wheel. Hover selects only
 * while the pointer moves, so the last device used owns the selection and a resting pointer does
 * not fight the keys. A click that only gives the window the focus confirms nothing, and while the
 * pointer is hidden (in flight) the mouse does nothing at all.
 */
public final class Pointer {
    /** A click this soon after the window got the focus is the click that focused it. */
    static final float FOCUS_SECONDS = 0.1f;

    private static final int BACK_BUTTONS = 1 << Buttons.RIGHT | 1 << Buttons.BACK;

    private boolean enabled = true;
    private boolean sampled;
    private float x = -1;
    private float y = -1;
    private boolean moved;
    private boolean clicked;
    private boolean back;
    private boolean held;
    private int wheel;
    private String typed = "";
    private float wheelRest;
    private boolean wasFocused = true;
    /** Seconds since the window last got the focus. */
    private float sinceFocused = FOCUS_SECONDS;

    /** Whether the mouse acts: false while the pointer is hidden in flight. */
    public void enabled(boolean acts) {
        enabled = acts;
    }

    /** Samples the mouse; call once per frame, before the screens read it. */
    public void update(PointerState state, boolean focused, float seconds) {
        float nowX = state.x();
        float nowY = state.y();
        // Only a focus that was gained counts: a window that never reports it (X11 without a window
        // manager) takes every click.
        sinceFocused = focused && !wasFocused ? 0 : sinceFocused + seconds;
        wasFocused = focused;
        boolean live = enabled && sinceFocused >= FOCUS_SECONDS;
        int presses = live ? state.presses() : 0;
        moved = enabled && sampled && (nowX != x || nowY != y);
        x = nowX;
        y = nowY;
        sampled = true;
        clicked = (presses & 1 << Buttons.LEFT) != 0;
        back = (presses & BACK_BUTTONS) != 0;
        held = live && state.down(Buttons.LEFT);
        // Whole notches; a touchpad's fractions add up.
        wheelRest = live ? wheelRest + state.wheel() : 0;
        wheel = (int) wheelRest;
        wheelRest -= wheel;
        typed = enabled ? state.typed() : "";
        state.endFrame();
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    /** Whether the pointer moved in this frame. */
    public boolean moved() {
        return moved;
    }

    /** Whether the pointer moved onto or within the box in this frame: hover selects it. */
    public boolean hovers(Box box) {
        return moved && box.contains(x, y);
    }

    /** Whether the pointer is over the box. */
    public boolean over(Box box) {
        return enabled && box.contains(x, y);
    }

    /** Whether the left button was clicked in this frame, anywhere. */
    public boolean clicked() {
        return clicked;
    }

    /** Whether the left button was clicked on the box in this frame. */
    public boolean clicks(Box box) {
        return clicked && box.contains(x, y);
    }

    /** Whether the right button or the mouse's back button was clicked: back, as Esc. */
    public boolean back() {
        return back;
    }

    /** Whether the left button is held: a slider follows the pointer. */
    public boolean held() {
        return held;
    }

    /**
     * The characters the keyboard typed in this frame (none in flight): the high scores' name entry
     * reads them. The mouse's processor collects them, as it is the game's one input processor.
     */
    public String typed() {
        return typed;
    }

    /** The wheel's whole notches in this frame, positive down a list. */
    public int wheel() {
        return wheel;
    }
}
