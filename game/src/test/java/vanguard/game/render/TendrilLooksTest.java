package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * M7 part C: a Tendril Anchor faces its partner across the canyon (design/enemies/ground/tendril-anchor; the
 * art's 32 headings: 8 facing left, 24 facing right), and before it holds a tendril the side it stands on says
 * where its partner will be.
 */
class TendrilLooksTest {
    private static final double EPSILON = 1e-9;

    @Test
    void facingsFollowTheArtsHeadings() {
        assertEquals(0, EnemyLooks.heading(TendrilLooks.facing(0, -1), 32), "down");
        assertEquals(8, EnemyLooks.heading(TendrilLooks.facing(-1, 0), 32), "left");
        assertEquals(16, EnemyLooks.heading(TendrilLooks.facing(0, 1), 32), "up");
        assertEquals(24, EnemyLooks.heading(TendrilLooks.facing(1, 0), 32), "right");
    }

    @Test
    void anAnchorWithoutATendrilFacesAcrossTheCanyon() {
        assertEquals(TendrilLooks.facing(1, 0), TendrilLooks.partnerFacing(null, 60, 300), EPSILON);
        assertEquals(TendrilLooks.facing(-1, 0), TendrilLooks.partnerFacing(null, 420, 300), EPSILON);
    }

    @Test
    void theSwayIsNoneAtTheTipsAndItsMostAtTheMiddle() {
        TendrilLooks.Strip strip = new TendrilLooks.Strip(100, 200, 1, 0, 280, 5);
        assertEquals(100, strip.x(0), EPSILON);
        assertEquals(200, strip.y(0), EPSILON);
        assertEquals(200, strip.y(280), 1e-6);
        assertEquals(205, strip.y(140), EPSILON);
    }
}
