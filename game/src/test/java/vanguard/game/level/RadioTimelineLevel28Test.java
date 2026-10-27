package vanguard.game.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vanguard.game.level.RadioPlayer.arenaHaltTick;
import static vanguard.game.level.RadioPlayer.holdAfterKill;

import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.level.RadioPlayer.EndRun;
import vanguard.sim.CueTrigger;
import vanguard.sim.LevelScript;
import vanguard.sim.SimStep;

/**
 * {@link RadioTimeline}'s checks for Level 28 (M8 part H), with and without the escort (every run is played with each
 * fit; Rook is missing, the Bulwark has no radio): the approach's timed lines and the secret's line (step H3); step H6:
 * the arena's lines on its halted clock (the arrival, the phases and the dive queued as phase lines) and the finale's
 * closing lines counted from the kill. Its lines are voiced (step H3).
 */
class RadioTimelineLevel28Test extends RadioTimeline {
    RadioTimelineLevel28Test() {
        super(LEVEL_28);
    }

    /**
     * The README's timeline: the kill holds the clock for the Maw's 4 s death chain and one second more, so the clock
     * reads 105 at K+5 (the shaft), the breakout's 115 at K+15 and the cruisers rise at 118, K+18, where Varga's line
     * waits.
     */
    @Test
    void theClockResumesAtKPlusFiveAndTheCruisersRiseAtKPlusEighteen() {
        LevelData data = CONTENT.level(LEVEL_28);
        for (Difficulty difficulty : Difficulty.values()) {
            LevelScript script = SimSpecs.level(CONTENT, LEVEL_28, difficulty);
            assertEquals(5, holdAfterKill(script), 1e-9, "K+5 = t=105 on " + difficulty);
            double arenaEnd = (arenaHaltTick(script) + 1) * SimStep.SECONDS;
            assertEquals(105, arenaEnd, 1e-9);
            double rise = data.cruisers().orElseThrow().rise();
            assertEquals(18, holdAfterKill(script) + rise - arenaEnd, 1e-9, "the rise at K+18");
        }
    }

    /**
     * In every voiced run to the end with the kill: the closing lines open in order, each after the one before has
     * spoken; the Choir's fragment its 3 s after the kill (at most a second later), Varga's "Those aren't Vrell hulls"
     * at the rise (K+18), Okafor's "break for orbit" at K+34 and Varga's hull-code line at K+42, each at most a second
     * late; the debrief waits for the last.
     */
    @Test
    void theFinalesClosingLinesComeInOrderFromTheKill() {
        LevelData data = CONTENT.level(LEVEL_28);
        int runs = 0;
        for (EndRun run : endRuns()) {
            List<Played> closing = run.played().stream()
                    .filter(line -> line.cue().trigger() == CueTrigger.BOSS_DESTROYED
                            && line.opened().isPresent())
                    .toList();
            if (closing.isEmpty()) {
                continue;
            }
            runs++;
            assertEquals(
                    List.of("The Choir", "Okafor", "Varga", "Okafor", "Okafor", "Varga"),
                    closing.stream().map(line -> line.cue().speaker()).toList(),
                    run.toString());
            double kill = closing.getFirst().due();
            int cue = 0;
            for (int i = 0; i < data.radio().size(); i++) {
                if (data.radio().get(i).event().orElse(null) == LevelData.CueEvent.BOSS_DESTROYED) {
                    double delay = data.radioDelay(i);
                    double opened = closing.get(cue).opened().orElseThrow() - kill;
                    if (delay > 0) {
                        assertTrue(
                                opened >= delay - SimStep.SECONDS - 1e-6 && opened <= delay + MAX_LATE_SECONDS,
                                closing.get(cue).cue().speaker() + "'s line opens at K+" + opened + ", not K+" + delay
                                        + " (" + run + ")");
                    }
                    if (cue > 0) {
                        Played before = closing.get(cue - 1);
                        double spoken = before.opened().orElseThrow() + speaks(before.cue(), before.text());
                        assertTrue(
                                closing.get(cue).opened().orElseThrow() >= spoken,
                                closing.get(cue).cue().speaker() + " opens before "
                                        + before.cue().speaker() + " has spoken (" + run + ")");
                    }
                    cue++;
                }
            }
            Played last = closing.getLast();
            assertTrue(
                    last.opened().orElseThrow() + speaks(last.cue(), last.text()) <= run.handover(),
                    "the debrief waits for Varga's last line (" + run + ")");
        }
        assertTrue(runs > 0, "runs with the kill");
    }

    /**
     * On the halted clock the boss's lines play: in the runs at the autopilot's fight lengths the arrival, phase 2's,
     * the dive's and both of phase 3's lines open, each after its event and within the phase lines' 20 s.
     */
    @Test
    void theArenasLinesPlayOnTheHaltedClock() {
        List<CueTrigger> boss = List.of(CueTrigger.BOSS_ARRIVAL, CueTrigger.BOSS_PHASE, CueTrigger.BOSS_DIVE);
        int runs = 0;
        for (EndRun run : endRuns()) {
            if (run.events().size() < 5 || run.events().getLast().into() < 90) {
                continue;
            }
            runs++;
            List<Played> lines = run.played().stream()
                    .filter(line -> boss.contains(line.cue().trigger()))
                    .toList();
            assertEquals(5, lines.size(), run.toString());
            for (Played line : lines) {
                assertTrue(line.opened().isPresent(), line.cue().line() + " plays (" + run + ")");
                double waited = line.opened().get() - line.due();
                assertTrue(
                        waited >= 0 && waited <= RadioQueue.PHASE_STALE_SECONDS,
                        line.cue().line() + " waits " + waited + " s (" + run + ")");
            }
        }
        assertTrue(runs > 0, "runs through the fight");
    }
}
