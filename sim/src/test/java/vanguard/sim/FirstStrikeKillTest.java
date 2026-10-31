package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part B, step B1 (design/campaign Level 36, radio: the first enemy killed by a lightning strike; gap check m10b
 * §1 #9): the {@code first-strike-kill} cue ({@link CueTrigger#FIRST_STRIKE_KILL}) plays once, with the first strike
 * that kills a unit counted as an enemy; a strike that kills nothing calls nothing.
 */
class FirstStrikeKillTest {
    private static final RadioCue LINE = CloudDiversSpecs.cue(CueTrigger.FIRST_STRIKE_KILL, "");

    private static Sortie sortie(List<GroundUnit> units, HazardSpec.Strike... at) {
        HazardSpec hazards = HazardSpec.NONE.withWeather(Optional.of(Act6BSpecs.storm(Act6BSpecs.strikes(0, at))));
        return Act6BSpecs.sortie(
                CloudDiversSpecs.level(20, hazards, units, List.of(), 0, List.of(LINE)), TestSpecs.LOADOUT);
    }

    @Test
    void theFirstKillingStrikeCallsItsLineOnce() {
        Sortie sortie = sortie(
                List.of(
                        new GroundUnit(0, 120, Act6BSpecs.target("a", Layer.AIR, 50), -1),
                        new GroundUnit(0, 360, Act6BSpecs.target("b", Layer.AIR, 50), -1)),
                new HazardSpec.Strike(0.5, 240, false),
                new HazardSpec.Strike(1, 120, false),
                new HazardSpec.Strike(1.5, 360, false));
        int lines = 0;
        int kills = 0;
        int firstLine = -1;
        int firstKill = -1;
        for (int i = 0; i < SimStep.ticks(6); i++) {
            sortie.step(Command.NONE);
            if (sortie.kills() > kills && firstKill < 0) {
                firstKill = i;
            }
            kills = sortie.kills();
            if (sortie.events().count(SimEvents.Type.RADIO) > 0) {
                lines++;
                firstLine = firstLine < 0 ? i : firstLine;
            }
        }
        assertEquals(2, kills, "both struck down");
        assertEquals(1, lines, "once");
        assertTrue(firstKill > SimStep.ticks(1.6), "not the empty strike at 240: " + firstKill);
        assertEquals(firstKill, firstLine, "with the first strike kill");
    }

    @Test
    void aStrikeThatKillsNothingCallsNothing() {
        Sortie sortie = sortie(
                List.of(new GroundUnit(0, 120, Act6BSpecs.target("tough", Layer.AIR, 500), -1)),
                new HazardSpec.Strike(1, 120, false));
        int lines = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
            lines += sortie.events().count(SimEvents.Type.RADIO);
        }
        assertEquals(0, lines);
        assertEquals(0, sortie.kills());
    }
}
