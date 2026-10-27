package vanguard.game.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The screen state machine of design/ui: a stack of {@link GameScreen}s of which only the top one
 * is updated and drawn. A screen opened over another (options over the main menu, the pause menu
 * over a level) returns to it when it closes, so the one below keeps its state, its music and its
 * resources; a replacement closes every screen. A closed screen is disposed at once.
 */
public final class ScreenFlow implements Disposable {
    private final Deque<GameScreen> screens = new ArrayDeque<>();
    private final Runnable quit;

    /** @param quit ends the game */
    public ScreenFlow(GameScreen first, Runnable quit) {
        this.quit = quit;
        screens.push(first);
    }

    /** Updates the top screen and follows its transition. */
    public void update(float seconds) {
        switch (screens.peek().update(seconds)) {
            case Transition.Stay stay -> {}
            case Transition.Open open -> screens.push(open.screen());
            case Transition.Back back -> {
                if (screens.size() == 1) {
                    throw new IllegalStateException("the last screen cannot go back");
                }
                screens.pop().dispose();
            }
            case Transition.Replace replace -> {
                closeAll();
                screens.push(replace.screen());
            }
            case Transition.Quit q -> quit.run();
        }
    }

    /** Whether the screen on top hides the mouse pointer (a level in flight). */
    public boolean pointerHidden() {
        return screens.peek().pointerHidden();
    }

    public void draw(SpriteBatch batch) {
        screens.peek().draw(batch);
    }

    /** RF6 ({@code --shots}): the level tick of the level on top, -1 when the top screen is not a level. */
    public long levelTick() {
        return screens.peek() instanceof LevelScreen level ? level.tick() : -1;
    }

    /** The screen on top, for tests. */
    GameScreen top() {
        return screens.peek();
    }

    /** How many screens are open, for tests. */
    int depth() {
        return screens.size();
    }

    private void closeAll() {
        while (!screens.isEmpty()) {
            screens.pop().dispose();
        }
    }

    @Override
    public void dispose() {
        closeAll();
    }
}
