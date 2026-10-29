package vanguard.content;

import java.util.Locale;
import java.util.Optional;

/**
 * A speaker's portrait expression (design/ui/briefing, design/story/characters): the main cast has
 * neutral, grim and fierce, the minor speakers only the neutral one. A briefing page or radio line names one where
 * its text calls for it; otherwise the portrait is neutral. M9 part H (Level 35, the Act 5 intro's page 3): Vorne's
 * own two, {@link #SMUG} and {@link #RAGE} (his portraits; voiced with the neutral and the grim rows), for him only.
 */
public enum Expression {
    NEUTRAL,
    GRIM,
    FIERCE,
    /** M9 part H: Vorne's smug portrait (voiced as neutral); his only. */
    SMUG,
    /** M9 part H: Vorne's bared-teeth portrait (voiced as grim, cold, never shouted); his only. */
    RAGE;

    /** M9 part H: the speaker the {@link #SMUG} and {@link #RAGE} expressions belong to. */
    public static final String VORNE = "Vorne";

    /** The name in the data and in the portraits' file names, {@code grim}. */
    public String slug() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** M9 part H: whether only {@link #VORNE} may wear it. */
    public boolean vorneOnly() {
        return this == SMUG || this == RAGE;
    }

    /** M9 part H: smug and rage are Vorne's portraits only. */
    static void checkSpeaker(String speaker, Optional<Expression> expression) {
        expression.ifPresent(e -> Check.that(
                !e.vorneOnly() || speaker.equals(VORNE),
                "expression: " + e.slug() + " is Vorne's only, not " + speaker + "'s"));
    }
}
