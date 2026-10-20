package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.game.render.DomesFixture;
import vanguard.sim.Sortie;

/**
 * M7 part D's wiring in the level screen (Level 17 Dome Siege, on {@link DomesFixture}'s hand-built domes until the
 * level's mapping lands): the debrief's row reads {@code DOMES UNTOUCHED n / 5} (here of the fixture's three), the
 * domes never hit of all, paid when the untouched secondary was met.
 */
class LevelScreenSiegeTest {
    @Test
    void theDebriefsRowCountsTheDomesNeverHit() {
        Sortie untouched = DomesFixture.sortie(0);
        DebriefScreen.Hulls count = LevelScreen.debriefCount(untouched);
        assertEquals("DOMES " + DebriefScreen.UNTOUCHED, count.label());
        assertFalse(count.percent());
        assertEquals(3, count.afloat(), "none hit at the start");
        assertEquals(3, count.of());
        DomesFixture.runTo(untouched, DomesFixture.LENGTH);
        assertTrue(untouched.secondaryMet());
        assertEquals(3, LevelScreen.debriefCount(untouched).afloat(), "nothing went for them");

        Sortie bombed = DomesFixture.sortie(1);
        DomesFixture.runTo(bombed, DomesFixture.LENGTH);
        DebriefScreen.Hulls after = LevelScreen.debriefCount(bombed);
        assertEquals(2, after.afloat(), "the bombed dome is not untouched");
        assertFalse(bombed.secondaryMet());

        DebriefScreen.Row met = DebriefScreen.hullsRow(new DebriefScreen.Hulls("DOMES UNTOUCHED", 5, 5), true, 256);
        assertEquals("DOMES UNTOUCHED", met.label());
        assertEquals("5 / 5", met.middle());
        assertEquals("+ 256 CR", met.right(1));
        assertEquals("", DebriefScreen.hullsRow(after, false, 256).right(1), "unpaid when missed");
    }

    /** Round 41: Level 17's secondary is a standing one ("No dome lost"): its row reads {@code DOMES STANDING n / 5}. */
    @Test
    void level17sRowCountsTheDomesStanding() {
        Content content = ContentLoader.fromClasspath();
        String key = content.levelKey(17).orElseThrow();
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(content, Difficulty.MEDIUM),
                SimSpecs.level(content, key, Difficulty.MEDIUM),
                SimSpecs.rules(content, key, Difficulty.MEDIUM),
                60);
        assertTrue(sortie.script().objectives().secondary().standing());
        DebriefScreen.Hulls count = LevelScreen.debriefCount(sortie);
        assertEquals("DOMES " + DebriefScreen.STANDING, count.label());
        assertEquals(sortie.allyCount(), count.afloat(), "all standing at the start");
        assertEquals(sortie.allyCount(), count.of());
    }
}
