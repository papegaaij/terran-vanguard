package vanguard.content;

/**
 * What a radio cue requires: {@code special} (a special fitted), {@code homing} (a weapon with
 * homing delivery fitted) or (M5 part C) {@code escort} (an escort flying); a prompt knows only
 * {@code special}.
 */
final class Requirement {
    /** A special fitted. */
    static final String SPECIAL = "special";
    /** A weapon with homing delivery fitted. */
    static final String HOMING = "homing";
    /** M5 part C: an escort flying (hired, fitted and not ejected). */
    static final String ESCORT = "escort";

    private Requirement() {}

    static void check(String requires) {
        Check.that(
                requires.equals(SPECIAL) || requires.equals(HOMING) || requires.equals(ESCORT),
                "requires: 'special', 'homing' or 'escort' (on a prompt only 'special' is known), was '" + requires
                        + "'");
    }

    static void checkPrompt(String requires) {
        Check.that(requires.equals(SPECIAL), "requires: on a prompt only 'special' is known, was '" + requires + "'");
    }
}
