package vanguard.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import vanguard.game.render.PixelScreen;
import vanguard.game.settings.Scaling;

/**
 * The mouse through libGDX: the game's one {@link com.badlogic.gdx.InputProcessor} on {@code
 * Gdx.input} (the wheel and short clicks only arrive as events), and the pointer's position mapped
 * into the pixel screen by the inverse of {@link PixelScreen#end}'s letterbox. The window's size is
 * read in logical units ({@code Gdx.graphics.getWidth}, as the pointer's position is) and the
 * letterbox in back-buffer pixels, so a HiDPI window (macOS Retina, Windows scaling) maps right.
 * Being the input processor, it also collects the typed characters (the high scores' name entry).
 */
public final class GdxPointer extends InputAdapter implements PointerState {
    private Scaling scaling = Scaling.INTEGER;
    private int presses;
    private float wheel;
    private final StringBuilder typed = new StringBuilder();

    /** The Video tab's scaling, which places the letterbox; set every frame. */
    public void scaling(Scaling current) {
        scaling = current;
    }

    private PixelScreen.Viewport viewport() {
        return PixelScreen.Viewport.of(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), scaling);
    }

    @Override
    public float x() {
        return viewport().screenX(Gdx.input.getX(), Gdx.graphics.getWidth(), Gdx.graphics.getBackBufferWidth());
    }

    @Override
    public float y() {
        return viewport().screenY(Gdx.input.getY(), Gdx.graphics.getHeight(), Gdx.graphics.getBackBufferHeight());
    }

    @Override
    public boolean down(int button) {
        return Gdx.input.isButtonPressed(button);
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
        return typed.toString();
    }

    @Override
    public void endFrame() {
        presses = 0;
        wheel = 0;
        typed.setLength(0);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button >= Buttons.LEFT && button <= Buttons.FORWARD) {
            presses |= 1 << button;
        }
        return true;
    }

    /** The keyboard's text input, collected for the name entry (design/systems/scoring, High scores). */
    @Override
    public boolean keyTyped(char character) {
        typed.append(character);
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        wheel += amountY;
        return true;
    }
}
