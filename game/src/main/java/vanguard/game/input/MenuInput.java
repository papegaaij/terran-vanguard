package vanguard.game.input;

/**
 * The menu actions of design/ui (arrows / D-pad navigate, Enter / A confirm, Esc / B back, Q / E
 * and the bumpers switch tabs), with key repeat on the four directions: a held direction repeats
 * after a short delay, so a slider or a long list can be run through by holding. The mouse's right
 * button and its back button go back as Esc does; the rest of the mouse is the screens' own, through
 * {@link #pointer}.
 */
public final class MenuInput {
    static final float REPEAT_DELAY = 0.35f;
    static final float REPEAT_INTERVAL = 0.08f;

    private static final Action[] DIRECTIONS = {Action.MENU_UP, Action.MENU_DOWN, Action.MENU_LEFT, Action.MENU_RIGHT};

    private final ActionInput input;
    private final Pointer pointer;
    private final float[] heldFor = new float[DIRECTIONS.length];
    private final boolean[] fired = new boolean[DIRECTIONS.length];

    /** Without a mouse: tests that only press keys. */
    public MenuInput(ActionInput input) {
        this(input, new Pointer());
    }

    public MenuInput(ActionInput input, Pointer pointer) {
        this.input = input;
        this.pointer = pointer;
    }

    /** The mouse in this frame. */
    public Pointer pointer() {
        return pointer;
    }

    /** Call once per frame after {@link ActionInput#update}. */
    public void update(float seconds) {
        for (int i = 0; i < DIRECTIONS.length; i++) {
            if (!input.held(DIRECTIONS[i])) {
                heldFor[i] = 0;
                fired[i] = false;
                continue;
            }
            float before = heldFor[i];
            heldFor[i] += seconds;
            fired[i] = input.pressed(DIRECTIONS[i]) || repeats(before, heldFor[i]);
        }
    }

    /** Whether a repeat falls between two hold times. */
    private static boolean repeats(float before, float after) {
        if (after < REPEAT_DELAY) {
            return false;
        }
        int ticksBefore = before < REPEAT_DELAY ? -1 : (int) ((before - REPEAT_DELAY) / REPEAT_INTERVAL);
        return (int) ((after - REPEAT_DELAY) / REPEAT_INTERVAL) > ticksBefore;
    }

    public boolean up() {
        return fired[0];
    }

    public boolean down() {
        return fired[1];
    }

    public boolean left() {
        return fired[2];
    }

    public boolean right() {
        return fired[3];
    }

    public boolean confirm() {
        return input.pressed(Action.MENU_CONFIRM);
    }

    /** Esc / B, or the mouse's right or back button. */
    public boolean back() {
        return input.pressed(Action.MENU_BACK) || pointer.back();
    }

    /** A left click anywhere: a screen that has nothing to choose goes on (debrief, game over). */
    public boolean clicked() {
        return pointer.clicked();
    }

    /** The characters typed in this frame: a name entry's text input. */
    public String typed() {
        return pointer.typed();
    }

    public boolean previousTab() {
        return input.pressed(Action.MENU_PREVIOUS_TAB);
    }

    public boolean nextTab() {
        return input.pressed(Action.MENU_NEXT_TAB);
    }
}
