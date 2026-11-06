package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part B, step B1 (design/campaign Level 43, radio: Rook's first Warden; Level 44, radio: the landing kill and the
 * first chain's head): {@code escort-first-kill} with a kind as its subject starts on Rook's first kill of that kind;
 * {@code first-kind-kill} on the attempt's first kill of a kind by anyone; {@code first-landing-kill} on the first
 * teleporting unit killed at most 0.5 s after its landing.
 */
class Act7BTriggersTest {
    private static final EnemySpec SHARD = EnemySpec.of("shard", 4, new Hitbox(16, 16), Layer.GROUND, 6, false, 10, 0);
    private static final EnemySpec HUSK = EnemySpec.of("husk", 4, new Hitbox(16, 16), Layer.GROUND, 6, false, 10, 0);

    private static RadioCue cue(CueTrigger trigger, String subject) {
        return new RadioCue(trigger, 0, subject, "Rook", "Line.", false, "neutral", "Rook");
    }

    /** A shard, then two husks, in a column at {@code x}, half a second apart. */
    private static LevelScript column(double x, List<RadioCue> radio) {
        return LevelScript.builder(8, 2)
                .sections(List.of(new Section(30, 130)))
                .groundUnits(List.of(
                        new GroundUnit(0.5, x, SHARD, -1),
                        new GroundUnit(1.0, x, HUSK, -1),
                        new GroundUnit(1.5, x, HUSK, -1)))
                .radio(radio)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    /** Per cue, the step it started at (-1 for never) over {@code steps} steps of fire; asserts each started once. */
    private static int[] flown(Sortie sortie, int cues, int steps) {
        int[] at = new int[cues];
        java.util.Arrays.fill(at, -1);
        int kills = 0;
        for (int i = 0; i < steps; i++) {
            sortie.step(Command.FIRE.bit());
            kills += sortie.events().count(SimEvents.Type.ENEMY_DESTROYED);
            for (int c = 0; c < cues; c++) {
                int started = Act7BSpecs.started(sortie, c);
                assertTrue(started <= 1);
                if (started > 0) {
                    assertEquals(-1, at[c], "cue " + c + " starts once");
                    at[c] = i;
                }
            }
        }
        assertEquals(3, kills, "the column is killed");
        return at;
    }

    @Test
    void rooksFirstKillOfAKindStartsItsEscortLine() {
        // The column ahead of him (his slot is 64 px left of the ship), out of the player's line of fire.
        LevelScript level = column(
                Ship.START_X - 64,
                List.of(
                        cue(CueTrigger.ESCORT_FIRST_KILL, ""),
                        cue(CueTrigger.ESCORT_FIRST_KILL, "husk"),
                        cue(CueTrigger.ESCORT_FIRST_KILL, "gate-warden")));
        int[] at = flown(Act7BSpecs.withRook(level), 3, 300);
        assertTrue(at[0] >= 0, "his first kill (the shard) as ever");
        assertTrue(at[1] > at[0], "his first husk after it");
        assertEquals(-1, at[2], "no kill of that kind");
    }

    @Test
    void thePlayersKillsStartNoEscortKindLine() {
        LevelScript level = column(Ship.START_X, List.of(cue(CueTrigger.ESCORT_FIRST_KILL, "husk")));
        assertEquals(-1, flown(Act7BSpecs.withRook(level), 1, 300)[0]);
    }

    @Test
    void theFirstKillOfAKindByAnyoneStartsItsLine() {
        LevelScript level = column(
                Ship.START_X,
                List.of(
                        cue(CueTrigger.FIRST_KIND_KILL, "husk"),
                        cue(CueTrigger.FIRST_KIND_KILL, "shard"),
                        cue(CueTrigger.FIRST_KIND_KILL, "coilwyrm")));
        Sortie alone = new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int[] at = flown(alone, 3, 300);
        assertTrue(at[1] >= 0);
        assertTrue(at[0] > at[1], "the first husk after the shard");
        assertEquals(-1, at[2]);
    }

    /** A sortie with one Rift Skater from 0.5 s and the landing kill's line. */
    private static Sortie skater() {
        LevelScript level =
                Act7ASpecs.level(20, List.of(Act7ASpecs.single(0.5, Act7ASpecs.SKATER)), Optional.empty()).toBuilder()
                        .radio(List.of(cue(CueTrigger.FIRST_LANDING_KILL, "")))
                        .build();
        return Act7ASpecs.sortie(level);
    }

    /** Steps until the Skater lands, then {@code after} steps more; returns its index. */
    private static int landed(Sortie sortie, int after) {
        for (int i = 0; i < SimStep.ticks(10) && sortie.events().count(SimEvents.Type.ARRIVE) == 0; i++) {
            sortie.step(Command.NONE);
        }
        assertEquals(1, sortie.events().count(SimEvents.Type.ARRIVE), "it landed");
        Act6ASpecs.run(sortie, after, Command.NONE);
        return Act7BSpecs.index(sortie, "rift-skater");
    }

    @Test
    void aSkaterKilledWithinHalfASecondOfItsLandingStartsTheLandingKill() {
        for (int after : new int[] {0, SimStep.ticks(ObjectivePasses.LANDING_KILL_SECONDS)}) {
            Sortie sortie = skater();
            sortie.destroyEnemy(landed(sortie, after));
            assertEquals(1, Act7BSpecs.started(sortie, 0), after + " steps after its landing");
        }
    }

    @Test
    void laterOrBeforeItsFirstJumpItDoesNot() {
        Sortie late = skater();
        late.destroyEnemy(landed(late, SimStep.ticks(ObjectivePasses.LANDING_KILL_SECONDS) + 1));
        assertEquals(0, Act7BSpecs.started(late, 0), "just over half a second after");

        Sortie early = skater();
        while (Act7BSpecs.index(early, "rift-skater") < 0) {
            early.step(Command.NONE);
        }
        early.destroyEnemy(Act7BSpecs.index(early, "rift-skater"));
        assertEquals(0, Act7BSpecs.started(early, 0), "on its path, before any landing");
    }
}
