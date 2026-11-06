package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part D, step CD1 (design/enemies/space/husk, the crack; design/campaign Level 46, the quiet's end; m11d-gaps.md
 * §1 #2, §4): a Husk never fires whole; at half its HP it tells 0.5 s, then fires its fan of 5 aimed shards once; it
 * dies in its ring of 8; the EMP holds its tell; killed in its tell it fires no fan. The level's crack cue cracks every
 * armed Husk on the screen in turn, farthest first, 0.6 s apart, skipping one already cracked; the first fan cues {@code
 * first-crack}.
 */
class HuskCrackTest {
    private static final RadioCue FIRST_CRACK =
            new RadioCue(CueTrigger.FIRST_CRACK, 0, "", "Rook", "It cracked.", false, "neutral", "Rook");

    /** Husks drifting slowly left to right at these depths, entering at 0.5 s; a crack cue at {@code cueAt} (NaN: none). */
    private static Sortie sortie(double cueAt, double... depths) {
        List<WaveSpec.Line> lines = new ArrayList<>();
        for (double depth : depths) {
            lines.add(Act7CDSpecs.line(-40, depth, 520, depth));
        }
        VrellLevel vrell = Double.isNaN(cueAt)
                ? VrellLevel.NONE
                : VrellLevel.NONE.withCrackCue(Optional.of(new VrellLevel.CrackCue(cueAt, 0.6)));
        LevelScript level = Act7CDSpecs.level(
                        30,
                        90,
                        List.of(Act7CDSpecs.drift(0.5, Act7CDSpecs.husk(150), depths.length, true, lines)),
                        List.of(),
                        vrell)
                .toBuilder()
                .radio(List.of(FIRST_CRACK))
                .build();
        return Act7CDSpecs.sortie(level);
    }

    private static void fly(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(Command.NONE);
        }
    }

    private static int index(Sortie sortie, Enemy unit) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i) == unit) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void atHalfItsHpItTellsThenFiresItsFanOnceAtTheShip() {
        Sortie sortie = sortie(Double.NaN, 100);
        fly(sortie, 3);
        Enemy husk = Act7CDSpecs.find(sortie, "husk");
        assertNotNull(husk);
        assertEquals(0, sortie.bulletCount(), "never fires whole");
        husk.hp = 74;
        sortie.step(Command.NONE);
        assertEquals(1, sortie.events().count(SimEvents.Type.CRACK_TELL));
        assertEquals(Cracking.Phase.TELLING, husk.crack().phase());
        int steps = 1;
        while (sortie.events().count(SimEvents.Type.CRACK_FAN) == 0) {
            sortie.step(Command.NONE);
            steps++;
        }
        assertEquals(SimStep.ticks(0.5), steps, 1, "its 0.5 s tell");
        assertEquals(5, sortie.bulletCount(), "a fan of 5");
        int radio = sortie.events().count(SimEvents.Type.RADIO);
        double toShip =
                StrictMath.atan2(sortie.ship().y() - husk.y(), sortie.ship().x() - husk.x());
        sortie.step(Command.NONE);
        double sum = 0;
        for (int b = 0; b < sortie.bulletCount(); b++) {
            EnemyBullet bullet = sortie.bullet(b);
            sum += StrictMath.atan2(bullet.renderY(1) - bullet.renderY(0), bullet.renderX(1) - bullet.renderX(0));
        }
        assertEquals(toShip, sum / sortie.bulletCount(), 0.05, "aimed at the ship");
        radio += sortie.events().count(SimEvents.Type.RADIO);
        husk.hp = 20;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.CRACK_TELL), "once");
            radio += sortie.events().count(SimEvents.Type.RADIO);
        }
        assertEquals(1, radio, "Rook's first-crack line, once");
        assertEquals(Cracking.Phase.SPENT, husk.crack().phase());
    }

    @Test
    void itDiesInItsRingAndKilledInItsTellFiresNoFan() {
        Sortie sortie = sortie(Double.NaN, 100);
        fly(sortie, 3);
        Enemy husk = Act7CDSpecs.find(sortie, "husk");
        husk.hp = 74;
        sortie.step(Command.NONE);
        assertEquals(Cracking.Phase.TELLING, husk.crack().phase());
        sortie.destroyEnemy(index(sortie, husk));
        assertEquals(8, sortie.bulletCount(), "its ring of 8");
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.CRACK_FAN));
        }
    }

    @Test
    void theEmpHoldsItsTell() {
        Sortie sortie = sortie(Double.NaN, 100);
        fly(sortie, 3);
        Enemy husk = Act7CDSpecs.find(sortie, "husk");
        husk.hp = 74;
        sortie.step(Command.NONE);
        husk.empStun = SimStep.ticks(1);
        int steps = 1;
        while (sortie.events().count(SimEvents.Type.CRACK_FAN) == 0 && steps < SimStep.ticks(4)) {
            sortie.step(Command.NONE);
            steps++;
        }
        assertTrue(steps >= SimStep.ticks(1.5) - 2, "held over the stun: " + steps);
    }

    @Test
    void theCueCracksTheHusksOnTheScreenInTurnFarthestFirstSkippingACrackedOne() {
        Sortie sortie = sortie(4, 60, 200, 330, 120);
        fly(sortie, 3);
        // The second Husk (depth 200) cracks before the cue.
        Enemy cracked = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy unit = sortie.enemy(i);
            if (Math.abs(unit.y() - (PlayField.HEIGHT - 200)) < 1) {
                cracked = unit;
            }
        }
        assertNotNull(cracked);
        cracked.hp = 74;
        int cue = -1;
        int count = -1;
        List<Integer> tells = new ArrayList<>();
        List<Double> distances = new ArrayList<>();
        for (int tick = 0; tick < SimStep.ticks(4); tick++) {
            sortie.step(Command.NONE);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.CRACK_CUE) {
                    cue = tick;
                    count = events.value(e);
                } else if (events.type(e) == SimEvents.Type.CRACK_TELL && cue >= 0) {
                    tells.add(tick);
                    distances.add(Math.hypot(
                            events.x(e) - sortie.ship().x(),
                            events.y(e) - sortie.ship().y()));
                }
            }
        }
        assertEquals(3, count, "the three armed Husks on the screen");
        assertEquals(List.of(cue, cue + SimStep.ticks(0.6), cue + 2 * SimStep.ticks(0.6)), tells, "0.6 s apart");
        assertTrue(distances.get(0) > distances.get(1) && distances.get(1) > distances.get(2), "farthest first");
    }
}
