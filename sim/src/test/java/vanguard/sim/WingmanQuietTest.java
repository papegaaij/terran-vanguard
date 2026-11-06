package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M11 part A, step A1b (design/world/vrell-space, The quiet; design/player/wingmen, Rook in Act 7; Level 46): while
 * the quiet holds, Rook's pick takes no unit; the player's first hit or a unit's first shot breaks it for the attempt.
 */
class WingmanQuietTest {
    /** Harmless drifters (no gun) from 1 s, slow, over 30 s; the quiet from 0 to {@code to} s. */
    static LevelScript level(double to) {
        EnemySpec drifter = EnemySpec.of("husk", 400, new Hitbox(40, 40), Layer.AIR, 10, false, 10, 30);
        LevelScript level = Act6ASpecs.level(30, List.of(), List.of(Act7ASpecs.stream(1, drifter, 3, 1.5)));
        return level.toBuilder()
                .vrell(VrellLevel.NONE.withQuiet(Optional.of(new VrellLevel.Quiet(0, to))))
                .build();
    }

    static Sortie sortie(LevelScript level) {
        return new Sortie(
                1,
                TestSpecs.LOADOUT.withWingman(WingmanTest.rook(WingmanSpec.Side.LEFT)),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
    }

    @Test
    void rookHoldsHisPickThroughTheQuietUntilItEnds() {
        Sortie sortie = sortie(level(8));
        boolean picked = false;
        for (int i = 0; i < SimStep.ticks(7.5); i++) {
            sortie.step(0);
            picked |= sortie.wingman().orElseThrow().target() >= 0;
        }
        assertTrue(sortie.enemyCount() > 0, "the drifters are on the screen");
        assertFalse(picked, "no unit his target in the quiet");
        assertFalse(sortie.quiet().broken());
        for (int i = 0; i < SimStep.ticks(3) && !picked; i++) {
            sortie.step(0);
            picked = sortie.wingman().orElseThrow().target() >= 0;
        }
        assertTrue(picked, "after the quiet he picks again");
    }

    @Test
    void thePlayersFirstHitBreaksTheQuiet() {
        Sortie sortie = sortie(level(25));
        int broken = 0;
        boolean picked = false;
        for (int i = 0; i < SimStep.ticks(12); i++) {
            sortie.step(Command.FIRE.bit());
            broken += sortie.events().count(SimEvents.Type.QUIET_BROKEN);
            picked |= sortie.wingman().orElseThrow().target() >= 0;
        }
        assertEquals(1, broken, "broken once");
        assertTrue(sortie.quiet().broken());
        assertTrue(picked, "he picks once it is broken");
    }

    @Test
    void withoutAQuietHePicksAsEver() {
        LevelScript level = level(25).toBuilder().vrell(VrellLevel.NONE).build();
        Sortie sortie = sortie(level);
        boolean picked = false;
        for (int i = 0; i < SimStep.ticks(8) && !picked; i++) {
            sortie.step(0);
            picked = sortie.wingman().orElseThrow().target() >= 0;
        }
        assertTrue(picked);
        assertEquals(null, sortie.quiet());
    }
}
