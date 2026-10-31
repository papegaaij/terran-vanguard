package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part C, step C1 (design/campaign Level 37, Radio chatter): Rook's two new cues. {@code first-bounce} plays once
 * an attempt, at the step a mirror first bounces a player shot ({@link SimEvents.Type#MIRROR_BOUNCED});
 * {@code first-prey-kill} once, at the step the hunters first kill a unit of their prey (a hunter dead on its prey is
 * no prey kill).
 */
class Act6CCuesTest {
    @Test
    void theFirstBounceIsCuedOnceAtTheFirstBounce() {
        LevelScript level =
                Act6ASpecs.level(30, List.of(new GroundUnit(0, Ship.START_X, Act6ASpecs.MIRRORED, -1)), List.of())
                        .toBuilder()
                        .radio(Act6CSpecs.radio())
                        .build();
        Sortie sortie = Act6ASpecs.sortie(level, Act6ASpecs.front());
        Act6ASpecs.until(sortie, "mirror-interceptor", 120);
        int bounces = 0;
        int cues = 0;
        for (int i = 0; i < SimStep.ticks(2); i++) {
            sortie.step(Command.FIRE.bit());
            int now = sortie.events().count(SimEvents.Type.MIRROR_BOUNCED);
            boolean cued = Act6CSpecs.cued(sortie, CueTrigger.FIRST_BOUNCE);
            if (cued) {
                assertTrue(bounces == 0 && now > 0, "at the first bounce");
                cues++;
            }
            bounces += now;
        }
        assertTrue(bounces > 1, "it bounced again: " + bounces);
        assertEquals(1, cues, "once");
    }

    /** A hovering prey of {@code hp}: its hold 260 px below the top for 8 s, no gun. */
    private static EnemySpec prey(double hp) {
        return EnemySpec.of("prey-gunship", hp, new Hitbox(26, 24), Layer.AIR, 10, false, 20, 120)
                .withHover(Optional.of(new EnemySpec.Hover(new Range(8, 8), new Range(260, 260))));
    }

    private static Sortie war(double preyHp) {
        WaveSpec prey = WaveSpec.builder(
                        0, WaveSpec.Formation.SINGLE, prey(preyHp), 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .tag("prey")
                .build();
        WaveSpec rammer = WaveSpec.builder(
                        2, WaveSpec.Formation.SINGLE, PreyWarTest.RAMMER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .hunts("prey")
                .build();
        return Act6CSpecs.sortie(Act6CSpecs.plain(30, List.of(prey, rammer)));
    }

    @Test
    void theFirstPreyKillIsCuedForAPreyKilledNotForAHunterDeadOnIt() {
        // A tough prey: the rammer dies on it (a loss to the war) and the prey lives: no cue.
        Sortie tough = war(100);
        int lost = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            tough.step(Command.NONE);
            lost += tough.events().count(SimEvents.Type.PREY_KILLED);
            assertTrue(
                    !Act6CSpecs.cued(tough, CueTrigger.FIRST_PREY_KILL), "a hunter dead on its prey is no prey kill");
        }
        assertEquals(1, lost, "the rammer");

        // A weak prey: rammed down with its rammer: one cue, at that step.
        Sortie weak = war(4);
        int cues = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            weak.step(Command.NONE);
            if (Act6CSpecs.cued(weak, CueTrigger.FIRST_PREY_KILL)) {
                assertEquals(2, weak.events().count(SimEvents.Type.PREY_KILLED), "the prey and its rammer");
                cues++;
            }
        }
        assertEquals(1, cues);
    }
}
