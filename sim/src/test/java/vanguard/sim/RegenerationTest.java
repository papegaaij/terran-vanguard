package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1a (design/enemies, Regeneration; design/enemies/air/chimera): after 1.0 s without any damage a
 * regenerating unit refills 20 % of its maximum a second, never past it; any damage restarts the delay, so a beam held
 * on it (a hit every step) denies it and short bursts with gaps longer than the delay let it heal; the EMP's stun pauses
 * the delay and the healing; a kill pays its bounty once whatever it healed.
 */
class RegenerationTest {
    private final SimEvents events = new SimEvents(64);
    private final Pool<Enemy> pool = new Pool<>(4, () -> new Enemy(events), Enemy[]::new);

    private Enemy chimera() {
        Enemy enemy = pool.obtain();
        enemy.root(Act6ASpecs.CHIMERA, 0, 240, 1, -1);
        enemy.move(Ship.START_X, Ship.START_Y, 300);
        return enemy;
    }

    private void steps(Enemy unit, int count) {
        for (int i = 0; i < count; i++) {
            unit.move(Ship.START_X, Ship.START_Y, 0);
        }
    }

    @Test
    void afterItsDelayItRefillsItsRateOfItsMaximumASecondUpToIt() {
        Enemy unit = chimera();
        unit.damage(20);
        assertEquals(20, unit.hp(), 1e-9);
        events.clear();
        steps(unit, SimStep.ticks(1.0));
        assertEquals(20, unit.hp(), 1e-9, "1.0 s: the delay");
        steps(unit, 1);
        assertEquals(20 + 40 * 0.2 / SimStep.PER_SECOND, unit.hp(), 1e-9, "8 HP a second");
        assertTrue(unit.regeneration().healing());
        assertEquals(1, events.count(SimEvents.Type.REGENERATING));
        steps(unit, SimStep.ticks(1.0));
        assertEquals(28 + 40 * 0.2 / SimStep.PER_SECOND, unit.hp(), 1e-6);
        steps(unit, SimStep.ticks(3.0));
        assertEquals(40, unit.hp(), 1e-9, "never past its maximum");
        assertFalse(unit.regeneration().healing(), "full: the pulse stops");
        assertEquals(2, events.count(SimEvents.Type.REGENERATING));
    }

    @Test
    void anyDamageRestartsTheDelaySoAHeldBeamDeniesIt() {
        Enemy unit = chimera();
        unit.damage(20);
        steps(unit, SimStep.ticks(1.5));
        double healed = unit.hp();
        assertTrue(healed > 20);
        events.clear();
        for (int i = 0; i < SimStep.ticks(3); i++) {
            // A beam's touch: a little damage every step.
            unit.damage(0.01);
            steps(unit, 1);
        }
        assertEquals(healed - 0.01 * SimStep.ticks(3), unit.hp(), 1e-9, "no healing under a held beam");
        assertEquals(1, events.count(SimEvents.Type.REGENERATING), "the healing stopped at the first touch");
        for (int burst = 0; burst < 3; burst++) {
            unit.damage(1);
            steps(unit, SimStep.ticks(1.5));
        }
        assertTrue(unit.hp() > healed - 0.01 * SimStep.ticks(3), "bursts with gaps longer than the delay let it heal");
    }

    @Test
    void theEmpsStunPausesTheDelayAndTheHealing() {
        Enemy unit = chimera();
        unit.damage(20);
        steps(unit, SimStep.ticks(0.5));
        unit.empStun(SimStep.ticks(3));
        steps(unit, SimStep.ticks(3));
        assertEquals(20, unit.hp(), 1e-9, "no healing in the stun");
        steps(unit, SimStep.ticks(0.5));
        assertEquals(20, unit.hp(), 1e-9, "its delay resumes after it: 0.5 + 0.5 s");
        steps(unit, 2);
        assertTrue(unit.hp() > 20);
    }

    @Test
    void aKillPaysItsBountyOnceWhateverItHealed() {
        Sortie sortie = Act6ASpecs.sortie(
                Act6ASpecs.level(30, 20, List.of(new GroundUnit(0, 240, Act6ASpecs.CHIMERA, -1)), List.of()));
        Enemy unit = Act6ASpecs.until(sortie, "chimera", 40);
        unit.damage(30);
        Act6ASpecs.run(sortie, SimStep.ticks(5), Command.NONE);
        assertEquals(40, unit.hp(), 1e-9);
        int credits = sortie.credits();
        sortie.destroyEnemy(0);
        assertEquals(credits + 14, sortie.credits(), "its 14, once");
        assertEquals(1, sortie.kills());
    }

    @Test
    void aRegenerationHasARateAndADelay() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Regeneration(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Regeneration(0.2, -1));
    }
}
