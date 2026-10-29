package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M9 part F, step F1 (design/enemies/space/void-leech, Drain and EMP; design/player/generator, Enemy drain effects):
 * a latched leech lowers the generator's effective output by 2 MW, stacking to −6 MW (a fourth latches but drains
 * nothing more); it never drains shield points; shaken off or killed its 2 MW come back in the same step; a loadout
 * without a power budget is never drained. The EMP Burst throws every latched leech off (stunned 1 s, then it flees)
 * and stuns the free ones 1.5 s; the Lamprey's latch is unchanged (no power drain, it stays latched under the EMP).
 */
class LeechDrainTest {
    private static final int SPECIAL = Command.SPECIAL.bit();

    private static Loadout powered(double output, double load) {
        return Act5Specs.powered(TestSpecs.LOADOUT, Act5Specs.power(output, load, 4, 2));
    }

    /** A rear stream of {@code count} leeches: from below, near the ship, each chases it at once and latches. */
    private static Sortie rear(Loadout loadout, int count) {
        return Act5FSpecs.sortie(
                loadout,
                TestSpecs.RULES,
                40,
                Act5FSpecs.stream(0, Act5FSpecs.LEECH, count, WaveSpec.Entry.REAR, WaveSpec.Edge.LEFT));
    }

    /** Steps without commands until {@code count} units are latched; returns the steps taken. */
    private static int untilLatched(Sortie sortie, int count) {
        for (int i = 1; i <= SimStep.ticks(20); i++) {
            sortie.step(Command.NONE);
            if (sortie.latched() >= count) {
                return i;
            }
        }
        throw new AssertionError("only " + sortie.latched() + " latched");
    }

    @Test
    void eachLatchedLeechTakesTwoMegawattsToSixAndNoShieldPoints() {
        Sortie sortie = rear(powered(20, 20), 4);
        PowerState power = sortie.power().orElseThrow();
        double shield = sortie.ship().defences().shield();
        int drains = 0;
        for (int i = 0; i < SimStep.ticks(20) && sortie.latched() < 4; i++) {
            sortie.step(Command.NONE);
            drains += sortie.events().count(SimEvents.Type.POWER_DRAIN);
            assertEquals(sortie.latched(), power.drainers(), "the drainers follow the latched leeches at once");
            if (sortie.latched() == 2) {
                assertTrue(power.regenStopped(), "two leeches: the regeneration stops first");
                assertEquals(1, power.factor(), "the weapons still full");
            }
            if (sortie.latched() == 3) {
                assertEquals(0.875, power.factor(), 1e-9, "three: the rate falls to 7/8 (20 MW at its cap)");
            }
        }
        assertEquals(3, sortie.latched(), "three slots: the fourth is a contact and dies");
        assertEquals(3, drains, "one POWER_DRAIN a latch (the HUD's gauge)");
        assertEquals(6, power.drainedMw(), 1e-9);
        assertEquals(14, power.effectiveOutput(), 1e-9);
        assertFalse(sortie.draining(), "no SHIELD DRAIN: a leech never drains shield points");
        assertTrue(sortie.ship().defences().shield() <= shield, "the fourth's contact only");
    }

    @Test
    void shakenOffOrKilledItsOutputComesBackInTheSameStep() {
        Sortie sortie = rear(powered(20, 18), 2);
        untilLatched(sortie, 2);
        PowerState power = sortie.power().orElseThrow();
        assertEquals(2, power.drainers());
        assertTrue(power.regenStopped());
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).latched()) {
                sortie.destroyEnemy(i);
                break;
            }
        }
        sortie.step(Command.NONE);
        assertEquals(1, power.drainers(), "killed: its 2 MW back");
        assertFalse(power.regenStopped(), "one leech on 2 MW to spare: the regeneration runs");
        int[] shake = {
            Command.LEFT.bit(), Command.RIGHT.bit(), Command.LEFT.bit(), Command.RIGHT.bit(), Command.LEFT.bit()
        };
        for (int command : shake) {
            sortie.step(command);
        }
        assertEquals(0, sortie.latched(), "shaken off");
        assertEquals(0, power.drainers(), "in the same step");
        assertEquals(20, power.effectiveOutput(), 1e-9);
    }

    @Test
    void aLoadoutWithoutAPowerBudgetIsNeverDrained() {
        Sortie sortie = rear(TestSpecs.LOADOUT, 3);
        untilLatched(sortie, 3);
        assertTrue(sortie.power().isEmpty());
        for (int i = 0; i < 30; i++) {
            sortie.step(Command.NONE);
            assertEquals(0, sortie.events().count(SimEvents.Type.POWER_DRAIN));
        }
        assertFalse(sortie.draining());
    }

    @Test
    void theLampreyStillDrainsTheShieldAndNotThePower() {
        Sortie sortie = Act5FSpecs.sortie(
                powered(20, 20),
                TestSpecs.RULES,
                30,
                TestSpecs.wave(
                        0,
                        WaveSpec.Formation.SINGLE,
                        LampreyTest.LAMPREY,
                        1,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE));
        untilLatched(sortie, 1);
        sortie.step(Command.NONE);
        assertTrue(sortie.draining(), "SHIELD DRAIN");
        assertEquals(0, sortie.power().orElseThrow().drainers());
    }

    @Test
    void theEmpThrowsLatchedLeechesOffAndStunsTheFreeOnes() {
        Sortie sortie = rear(powered(20, 20).withSpecial(Act3SpecialsTest.emp(1)), 2);
        untilLatched(sortie, 2);
        PowerState power = sortie.power().orElseThrow();
        assertEquals(2, power.drainers());
        sortie.step(SPECIAL);
        int thrown = 0;
        for (int i = 0; i < 40 && thrown == 0; i++) {
            sortie.step(Command.NONE);
            thrown += sortie.events().count(SimEvents.Type.LATCH_THROWN);
        }
        assertEquals(2, thrown, "every latched leech thrown at once");
        assertEquals(0, sortie.latched());
        assertEquals(0, power.drainers(), "its output back");
        for (int j = 0; j < sortie.enemyCount(); j++) {
            Enemy enemy = sortie.enemy(j);
            assertEquals(Enemy.LatchPhase.STUNNED, enemy.latchPhase());
            assertFalse(enemy.empStunned(), "the shake-off's own 1 s stun, not the EMP's");
        }
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        for (int j = 0; j < sortie.enemyCount(); j++) {
            assertEquals(Enemy.LatchPhase.FLEE, sortie.enemy(j).latchPhase(), "then it flees, never to latch again");
        }
    }

    @Test
    void theEmpStunsAFreeLeechForOneAndAHalfSeconds() {
        Sortie sortie = Act5FSpecs.sortie(
                powered(20, 20).withSpecial(Act3SpecialsTest.emp(1)),
                TestSpecs.RULES,
                40,
                Act5FSpecs.sideStreamAt(0, Act5FSpecs.LEECH, 1, WaveSpec.Edge.LEFT, 300));
        while (sortie.enemyCount() == 0
                || !PlayField.overlaps(
                        sortie.enemy(0).x(),
                        sortie.enemy(0).y(),
                        sortie.enemy(0).hitbox())) {
            sortie.step(Command.NONE);
        }
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        Enemy leech = sortie.enemy(0);
        sortie.step(SPECIAL);
        int stunned = 0;
        for (int i = 0; i < SimStep.ticks(4); i++) {
            sortie.step(Command.NONE);
            if (leech.empStunned()) {
                stunned++;
                assertTrue(leech.loose(), "still free: the EMP throws only latched ones");
            }
        }
        assertEquals(SimStep.ticks(1.5), stunned, 1, "the Vrell's 1.5 s");
    }

    @Test
    void theLeechesReplayAndDrainWithoutAllocating() {
        Supplier<Sortie> make = () -> rear(powered(20, 20), 5);
        Sortie a = make.get();
        Sortie b = make.get();
        int[] commands = {0, 0, Command.FIRE.bit(), Command.LEFT.bit(), Command.RIGHT.bit()};
        for (int i = 0; i < SimStep.ticks(12); i++) {
            int command = commands[(i / 7) % commands.length];
            a.step(command);
            b.step(command);
        }
        assertEquals(a.stateHash(), b.stateHash());
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(12); i++) {
                sortie.step(commands[(i / 7) % commands.length]);
            }
        }));
    }
}
