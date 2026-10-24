package vanguard.game.settings;

/**
 * The Gameplay tab (design/ui/options).
 *
 * @param textSpeed characters per second the radio types; the briefings type at twice this speed
 * @param screenShake 0..1 of the full screen shake (M7 part H: the play field's sideways tremble, {@code ScreenShake})
 * @param flashReduction tone down the white hit and invulnerability flashes
 * @param creditNumbers show the small floating numbers for credits picked up (design/ui/hud)
 * @param darkAssist M8 part F, Dark levels assist: a dark level's ambient light at 20 % instead of its own (design/campaign
 *     Level 26, Darkness)
 */
public record GameplaySettings(
        int textSpeed, double screenShake, boolean flashReduction, boolean creditNumbers, boolean darkAssist) {
    /** The radio's typing speed since M2. */
    public static final int DEFAULT_TEXT_SPEED = 30;

    private static final int BRIEFING_SPEED_FACTOR = 2;

    public static GameplaySettings defaults() {
        return new GameplaySettings(DEFAULT_TEXT_SPEED, 1, false, true, false);
    }

    /** Characters per second a briefing types: twice the radio's, so 60 at the default (design/ui/briefing). */
    public int briefingTextSpeed() {
        return BRIEFING_SPEED_FACTOR * textSpeed;
    }

    public GameplaySettings withTextSpeed(int charsPerSecond) {
        return new GameplaySettings(charsPerSecond, screenShake, flashReduction, creditNumbers, darkAssist);
    }

    public GameplaySettings withScreenShake(double share) {
        return new GameplaySettings(textSpeed, share, flashReduction, creditNumbers, darkAssist);
    }

    public GameplaySettings withFlashReduction(boolean on) {
        return new GameplaySettings(textSpeed, screenShake, on, creditNumbers, darkAssist);
    }

    public GameplaySettings withCreditNumbers(boolean on) {
        return new GameplaySettings(textSpeed, screenShake, flashReduction, on, darkAssist);
    }

    public GameplaySettings withDarkAssist(boolean on) {
        return new GameplaySettings(textSpeed, screenShake, flashReduction, creditNumbers, on);
    }
}
