package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1b (design/enemies, The three-way war; A1a's leftovers): the hunting links are live from the level's
 * turn ({@link LevelScript#turn()}): before it the Vrell and the Ascendancy fly side by side and no unit hurts another;
 * from it the hunters already in flight go for their prey too. A Mote Swarm's pursuit (a rammer, "a Mote Swarm's
 * lunge") follows and lunges at its prey wave's nearest unit while one is on the play field.
 */
class PreyTurnTest {
    private static WaveSpec prey(double t) {
        return WaveSpec.builder(
                        t, WaveSpec.Formation.SINGLE, PreyWarTest.PREY, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .tag("prey")
                .build();
    }

    private static WaveSpec shooters(double t) {
        return WaveSpec.builder(
                        t, WaveSpec.Formation.SINGLE, PreyWarTest.SHOOTER, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .hunts("prey")
                .build();
    }

    @Test
    void theLinksAreLiveFromTheTurn() {
        LevelScript level = Act6ASpecs.level(30, List.of(), List.of(prey(0), shooters(1))).toBuilder()
                .turn(Optional.of(5.0))
                .build();
        Sortie sortie = Act6ASpecs.sortie(level);
        int before = Act6BSpecs.run(sortie, SimStep.ticks(5) - 1, 0, SimEvents.Type.PREY_HIT);
        assertEquals(0, before, "side by side before the turn");
        assertNotNull(Act6BSpecs.find(sortie, "prey-gunship"));
        assertTrue(sortie.bulletCount() > 0, "the hunter fires at the ship meanwhile");
        int after = Act6BSpecs.run(sortie, SimStep.ticks(3), 0, SimEvents.Type.PREY_HIT);
        assertTrue(after > 0, "from the turn its volleys go at its prey");
        assertThrows(
                IllegalArgumentException.class,
                () -> level.toBuilder().turn(Optional.of(-1.0)).build());
    }

    @Test
    void aHuntingPursuitLungesAtItsPrey() {
        // The motes come once the prey holds below their band (they would ram it on the way down).
        WaveSpec motes = PursuitTest.pursuit(3, 8, PursuitTest.block(3, 0, 1, 20)).toBuilder()
                .hunts("prey")
                .build();
        WaveSpec sturdy = prey(0).toBuilder()
                .enemy(EnemySpec.of("prey-gunship", 1000, new Hitbox(26, 24), Layer.AIR, 10, false, 20, 120)
                        .withHover(Optional.of(new EnemySpec.Hover(new Range(8, 8), new Range(260, 260)))))
                .build();
        Sortie sortie = PartDSpecs.sortie(PartCSpecs.level(20, 120, List.of(sturdy, motes), List.of()));
        Enemy quarry = null;
        for (int i = 0; i < SimStep.ticks(9); i++) {
            sortie.step(i < SimStep.ticks(1) ? Command.LEFT.bit() : 0);
            if (Act6BSpecs.count(sortie, SimEvents.Type.PURSUIT_TELL) > 0) {
                quarry = Act6BSpecs.find(sortie, "prey-gunship");
                break;
            }
        }
        assertNotNull(quarry, "the pursuit told while its prey was on the field");
        assertTrue(Math.abs(quarry.x() - sortie.ship.x()) > 50, "the ship is elsewhere");
        assertEquals(quarry.x(), sortie.pursuit().orElseThrow().laneX(), 1, "its lane is the prey's");
    }
}
