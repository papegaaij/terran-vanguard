package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;

/**
 * M7 part D's militia flak (design/campaign Level 17, Layout; the level's {@code flak: {from: 160, every: 1.5}}): a
 * burst every 1.5 s from t=160 over the lower field, one per due time, replayed after a restart rewinds the clock.
 */
class MilitiaFlakTest {
    @Test
    void level17sGunsOpenUpAtT160() {
        String key = DomesFixture.CONTENT.levelKey(17).orElseThrow();
        LevelData.Flak flak = DomesFixture.CONTENT.level(key).flak().orElseThrow();
        assertEquals(160, flak.from(), 1e-9);
        assertEquals(1.5, flak.every(), 1e-9);
    }

    @Test
    void aBurstEveryIntervalFromItsStartOncePerDueTime() {
        MilitiaFlak flak = new MilitiaFlak(160, 1.5);
        int bursts = 0;
        int last = -1;
        for (int tick = 0; tick <= SimStep.ticks(172.5); tick++) {
            double t = tick * SimStep.SECONDS;
            int burst = flak.step(t);
            if (burst >= 0) {
                assertTrue(t >= 160 - 1e-9, "none before the guns open up: " + t);
                assertEquals(last + 1, burst, "in order, none skipped step by step");
                last = burst;
                bursts++;
            }
        }
        assertEquals(9, bursts, "160, 161.5 ... 172");
        // A restart rewinds the clock: the bursts come again from there.
        assertEquals(-1, flak.step(10));
        assertEquals(0, flak.step(160));
        // A long frame shows only the latest burst.
        assertEquals(4, flak.step(166.1));
    }

    @Test
    void theBurstsSpreadOverTheLowerFieldClearOfTheEdges() {
        Set<Long> places = new HashSet<>();
        for (int n = 0; n < 40; n++) {
            double x = MilitiaFlak.x(n);
            double y = MilitiaFlak.y(n);
            assertTrue(x >= MilitiaFlak.SIDE_MARGIN && x <= PlayField.WIDTH - MilitiaFlak.SIDE_MARGIN, "x " + x);
            assertTrue(y >= MilitiaFlak.LOW_Y && y <= MilitiaFlak.HIGH_Y, "y " + y);
            places.add(Math.round(x) * 1000 + Math.round(y));
        }
        assertTrue(places.size() > 30, "spread, not stacked: " + places.size());
        assertEquals(MilitiaFlak.x(7), MilitiaFlak.x(7), "the same place on a replay");
    }
}
