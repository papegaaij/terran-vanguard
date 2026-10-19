package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Sortie;

/**
 * M7 part C's wiring in the level screen (Level 16 Valles Canyon Run): the debrief's row reads {@code BARRIERS
 * BROKEN n / 9} (easy 7, hard 10: the gates' tendrils), paid when met.
 */
class LevelScreenCanyonTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(16).orElseThrow();

    @Test
    void theDebriefsBarriersRowCountsTheTendrilsBroken() {
        for (Difficulty difficulty : Difficulty.values()) {
            Sortie sortie = new Sortie(
                    1,
                    SimSpecs.starterLoadout(CONTENT, difficulty),
                    SimSpecs.level(CONTENT, KEY, difficulty),
                    SimSpecs.rules(CONTENT, KEY, difficulty),
                    60);
            DebriefScreen.Hulls count = LevelScreen.debriefCount(sortie);
            assertEquals(DebriefScreen.BARRIERS_BROKEN, count.label());
            assertFalse(count.percent());
            assertEquals(0, count.afloat(), "none broken at the start");
            int gates =
                    switch (difficulty) {
                        case EASY -> 7;
                        case MEDIUM -> 9;
                        case HARD -> 10;
                    };
            assertEquals(gates, count.of(), difficulty.toString());
        }
        DebriefScreen.Row met = DebriefScreen.hullsRow(new DebriefScreen.Hulls("BARRIERS BROKEN", 9, 9), true, 205);
        assertEquals("BARRIERS BROKEN", met.label());
        assertEquals("9 / 9", met.middle());
        assertEquals("+ 205 CR", met.right(1));
    }
}
