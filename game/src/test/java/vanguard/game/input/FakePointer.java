package vanguard.game.input;

import com.badlogic.gdx.Input.Buttons;

/**
 * A mouse the test moves and clicks, with the {@link Pointer} that reads it: the window has had the
 * focus long enough for clicks to count.
 */
public final class FakePointer implements PointerState {
    public final Pointer pointer = new Pointer();
    /** Whether the window reports the focus in the next frames. */
    public boolean focused = true;

    private float x = -100;
    private float y = -100;
    private int presses;
    private int heldButtons;
    private float wheel;
    private String typed = "";

    public FakePointer() {
        pointer.update(this, true, 1);
    }

    /** Moves the pointer to the point; a frame passes. */
    public Pointer move(float toX, float toY) {
        x = toX;
        y = toY;
        return frame();
    }

    /** Moves the pointer to the centre of the box. */
    public Pointer move(vanguard.game.ui.Box box) {
        return move(box.x() + box.width() / 2, box.y() + box.height() / 2);
    }

    /** A left click where the pointer is, pressed and released within the frame. */
    public Pointer click() {
        return press(Buttons.LEFT);
    }

    /** Moves to the centre of the box and clicks there in the next frame. */
    public Pointer click(vanguard.game.ui.Box box) {
        move(box);
        return click();
    }

    public Pointer press(int button) {
        presses |= 1 << button;
        return frame();
    }

    /** Holds the left button down (a drag) or lets it go; a frame passes. */
    public Pointer hold(boolean down) {
        heldButtons = down ? 1 << Buttons.LEFT : 0;
        return frame();
    }

    public Pointer turnWheel(float notches) {
        wheel += notches;
        return frame();
    }

    /** Types the characters on the keyboard; a frame passes. */
    public Pointer type(String characters) {
        typed += characters;
        return frame();
    }

    /** A frame without anything new: the pointer rests. */
    public Pointer frame() {
        pointer.update(this, focused, 1 / 60f);
        return pointer;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public boolean down(int button) {
        return (heldButtons & 1 << button) != 0;
    }

    @Override
    public int presses() {
        return presses;
    }

    @Override
    public float wheel() {
        return wheel;
    }

    @Override
    public String typed() {
        return typed;
    }

    @Override
    public void endFrame() {
        presses = 0;
        wheel = 0;
        typed = "";
    }
}
