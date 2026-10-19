package vanguard.game.input;

/**
 * What the mouse reports; the seam that lets the pointer logic run without libGDX. The position is
 * in the 960x540 pixel screen's coordinates (y down, as the UI kit's), already mapped from the
 * window; presses and the wheel are collected as events between two frames.
 */
public interface PointerState {
    /** The pointer's x on the pixel screen; outside 0..960 over a letterbox bar. */
    float x();

    /** The pointer's y on the pixel screen, y down; outside 0..540 over a letterbox bar. */
    float y();

    /** Whether a mouse button ({@code Input.Buttons}) is held now. */
    boolean down(int button);

    /** The buttons that went down since the last {@link #endFrame}, a bit per button: {@code 1 << button}. */
    int presses();

    /** The wheel's notches since the last {@link #endFrame}, positive towards the user (down a list). */
    float wheel();

    /**
     * The characters typed since the last {@link #endFrame} (the keyboard's text input, with its
     * repeat: the name entry of the high scores); none by default.
     */
    default String typed() {
        return "";
    }

    /** Forgets the presses, the wheel and the typed characters: the frame has read them. */
    void endFrame();
}
