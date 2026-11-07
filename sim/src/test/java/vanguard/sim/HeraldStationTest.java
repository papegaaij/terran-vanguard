package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part E, step E1 (design/campaign Level 47, the Heralds over a pair; m11e §1 #5, §6 #1): a hovering single's wave
 * {@code at} is its station (straight down to it, no sine drift) and its {@code hold} its hover's seconds there (per
 * difficulty in the content), then it leaves up. Its aura (90 px, centre to centre) rings the pair's upper Seraph 80 px
 * under it only while the Seraph crosses under it (within ±41 px), a Seraph gone in a jump is in no aura, and easy's
 * 75 px never reaches it.
 */
class HeraldStationTest {
    private static Sortie sortie(WaveSpec... waves) {
        return Act7ASpecs.sortie(Act7ASpecs.level(40, List.of(waves), Optional.empty()));
    }

    /** The steps the Herald holds its station (x 240, 70 px below the top) for a wave hold of {@code hold} s. */
    private static int held(double hold) {
        Sortie sortie = sortie(Act7ESpecs.station(0, Act7ESpecs.herald(90), 240, 70, hold));
        Enemy herald = null;
        int held = 0;
        double lastY = 0;
        for (int i = 0; i < SimStep.ticks(hold + 8); i++) {
            sortie.step(Command.NONE);
            herald = herald == null ? Act7ASpecs.find(sortie, "choir-herald") : herald;
            if (Act7ASpecs.find(sortie, "choir-herald") == null) {
                break;
            }
            assertEquals(240, herald.x(), 1e-9, "no sine drift at a fixed station");
            if (herald.phase == Enemy.Phase.HOLD) {
                held++;
                assertEquals(PlayField.HEIGHT - 70, herald.y(), 1e-9, "at its station");
            } else if (held > 0) {
                assertTrue(herald.y() >= lastY, "it leaves up");
            }
            lastY = herald.y();
        }
        assertTrue(held > 0);
        assertTrue(lastY > PlayField.HEIGHT - 70, "it left through the top");
        return held;
    }

    @Test
    void itHoldsItsStationForItsWavesHoldThenLeavesUp() {
        assertEquals(SimStep.ticks(16), held(16), 1, "medium 16 s");
        assertEquals(SimStep.ticks(11), held(11), 1, "easy 11 s");
        assertEquals(SimStep.ticks(22), held(22), 1, "hard 22 s");
    }

    @Test
    void itsAuraRingsTheUpperSeraphOnlyWhileItCrossesUnderItAndNotWhileGone() {
        Sortie sortie = sortie(
                Act7ESpecs.station(0, Act7ESpecs.herald(90), 240, 70, 16),
                Act7ESpecs.pair(1.5, Act7ESpecs.SERAPH, Optional.of(new WaveSpec.Rows(150, 245))));
        Enemy herald = null;
        Enemy upper = null;
        int inside = 0;
        int outside = 0;
        int claimed = 0;
        int gone = 0;
        for (int i = 0; i < SimStep.ticks(17); i++) {
            // The ship sweeps across the field: the mirrored Seraph crosses under the Herald.
            sortie.step(i % 160 < 80 ? Command.LEFT.bit() : Command.RIGHT.bit());
            herald = herald == null ? Act7ASpecs.find(sortie, "choir-herald") : herald;
            Enemy[] pair = Act7ESpecs.all(sortie, "choir-seraph");
            if (upper == null && pair.length == 2) {
                upper = pair[0];
            }
            if (upper == null || herald == null || herald.phase != Enemy.Phase.HOLD || pair.length == 0) {
                continue;
            }
            boolean reach = upper.inAura(herald, 90);
            if (upper.teleport().gone()) {
                gone++;
                assertFalse(reach, "gone: in no aura");
                // M11 part G, step G1 (E1's open point): its ring ends as it jumps, without the keep.
                assertTrue(upper.ringFeeder() < 0, "gone: its ring ended at once");
                continue;
            }
            double dx = Math.abs(upper.x() - herald.x());
            if (upper.y() == PlayField.HEIGHT - 150) {
                assertEquals(dx <= Math.sqrt(90 * 90 - 80 * 80), reach, "rings only within ±41 px: " + dx);
                inside += reach ? 1 : 0;
                outside += reach ? 0 : 1;
            }
            claimed += upper.ringFeeder() == herald.serial() ? 1 : 0;
        }
        assertTrue(inside > 0 && outside > 0, inside + " in, " + outside + " out");
        assertTrue(claimed > 0, "the Herald rang it");
        assertTrue(gone > 0, "it jumped under the Herald");
    }

    @Test
    void easysSeventyFivePixelAuraNeverReachesTheUpperRow() {
        Sortie sortie = sortie(
                Act7ESpecs.station(0, Act7ESpecs.herald(75), 240, 70, 16),
                Act7ESpecs.pair(1.5, Act7ESpecs.SERAPH, Optional.of(new WaveSpec.Rows(150, 245))));
        for (int i = 0; i < SimStep.ticks(17); i++) {
            sortie.step(i % 160 < 80 ? Command.LEFT.bit() : Command.RIGHT.bit());
            for (Enemy seraph : Act7ESpecs.all(sortie, "choir-seraph")) {
                // Its leaving tell's 40 px rise takes it to 40 px under the Herald: rung there (the report's item).
                if (seraph.teleport().leavingNow()) {
                    continue;
                }
                assertTrue(seraph.ringFeeder() < 0, "never rung before its leaving tell");
            }
        }
    }
}
