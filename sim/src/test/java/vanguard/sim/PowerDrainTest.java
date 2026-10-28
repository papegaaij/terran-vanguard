package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M9 part A, step A1 (design/player/generator, Enemy drain effects; the Act 5 hand plan's power check): each latched
 * drainer lowers the effective output E by 2 MW, at most 6; a load over E first stops the shield's regeneration, then,
 * once the shortfall exceeds the shield's draw, every weapon fires at f = E ÷ (load − shield draw), never below 0.5.
 * Below the load the spare-power bonus follows E. Without a drainer nothing changes and nothing more is hashed.
 */
class PowerDrainTest {
    private static final int FIRE = Command.FIRE.bit();

    private static PowerState state(double output, double load, double shieldDraw) {
        return new PowerState(Act5Specs.power(output, load, shieldDraw, 2), 1);
    }

    private static PowerState drained(double output, double load, double shieldDraw, int drainers) {
        PowerState state = state(output, load, shieldDraw);
        for (int i = 0; i < drainers; i++) {
            state.drain(1);
        }
        return state;
    }

    @Test
    void theHandPlansTable() {
        // L29: beam L4 7 + pods 4 + Tail Gun 2 + magnet 1 + Shield Mk III 4 = 18 of 20.
        PowerState l29two = drained(20, 18, 4, 2);
        assertEquals(16, l29two.effectiveOutput(), 1e-9);
        assertTrue(l29two.regenStopped(), "E 16 under 18: the regen stops");
        assertEquals(1, l29two.factor(), "the weapons full");
        PowerState l29three = drained(20, 18, 4, 3);
        assertTrue(l29three.regenStopped());
        assertEquals(1, l29three.factor(), "shortfall 4 = the shield's draw: weapons full");
        assertEquals(14 / 15.5, drained(20, 19.5, 4, 3).factor(), 1e-9, "L30–31: 0.90");
        assertEquals(0.875, drained(20, 20, 4, 3).factor(), 1e-9, "L32–34: 0.875");
        assertTrue(drained(20, 20, 4, 2).regenStopped());
        assertEquals(1, drained(20, 20, 4, 2).factor());
        assertEquals(16 / 17.0, drained(22, 21, 4, 3).factor(), 1e-9, "L35 on the Mk V: 0.94");
        assertEquals(10 / 12.0, drained(16, 15, 3, 3).factor(), 1e-9, "a small generator: 0.83");
        PowerState one = drained(20, 18, 4, 1);
        assertFalse(one.regenStopped(), "1 MW to spare under one leech: only the bonus shrinks");
        assertEquals(1, one.factor());
    }

    @Test
    void aFourthDrainerDrainsNothingMoreAndTheRateNeverFallsBelowHalf() {
        PowerState four = drained(20, 20, 4, 4);
        assertEquals(4, four.drainers());
        assertEquals(6, four.drainedMw(), 1e-9, "capped at 6 MW");
        assertEquals(0.875, four.factor(), 1e-9);
        assertEquals(0.5, drained(10, 20, 4, 3).factor(), 1e-9, "E 4 ÷ 16 = 0.25: the floor");
        PowerState released = drained(20, 20, 4, 3);
        released.drain(-1);
        released.drain(-1);
        assertEquals(1, released.factor(), "shaken off: its 2 MW back at once");
        released.drain(-5);
        assertEquals(0, released.drainers());
    }

    @Test
    void theSparePowerBonusFollowsTheEffectiveOutput() {
        // 20 MW against 15: 5 spare, +50 %; one leech: E 18, 3 spare, +30 %.
        PowerState spare = drained(20, 15, 4, 1);
        assertEquals(2 * 1.3, spare.regenPerSecond(), 1e-9);
        assertEquals(0, drained(20, 15, 4, 3).regenPerSecond(), "E 14 under 15: stopped");
    }

    private static final WeaponSpec BOLT = TestSpecs.bolt(
            "bolt", 4, 1, 600, new Hitbox(4, 8), Double.POSITIVE_INFINITY, 1, TestSpecs.muzzle(0, 10, 0));

    private static Sortie sortie(double output, double load, double shieldDraw) {
        Loadout loadout = Act5Specs.powered(
                TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, BOLT, BOLT)),
                Act5Specs.power(output, load, shieldDraw, 2));
        return new Sortie(1, loadout, TorpedoTest.units(false), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    @Test
    void underThreeLeechesAtTheCapEveryWeaponFiresAtSevenEighthsOfItsRate() {
        Sortie full = sortie(20, 20, 4);
        assertEquals(16, TorpedoTest.count(full, SimStep.ticks(4), FIRE, SimEvents.Type.SHOT_FIRED));
        Sortie drained = sortie(20, 20, 4);
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, drained.drain(1));
        }
        assertEquals(3, drained.events().count(SimEvents.Type.POWER_DRAIN), "one per latch (the HUD's gauge)");
        assertEquals(0.875, drained.power().orElseThrow().factor(), 1e-9);
        int shots = TorpedoTest.count(drained, SimStep.ticks(24), FIRE, SimEvents.Type.SHOT_FIRED);
        int fullShots = 16 + TorpedoTest.count(full, SimStep.ticks(20), FIRE, SimEvents.Type.SHOT_FIRED);
        assertEquals(96, fullShots);
        assertEquals(0.875, (shots - 1) / (double) (fullShots - 1), 0.01, "the intervals × 1 / 0.875");
        drained.drain(-3);
        assertEquals(4, TorpedoTest.count(drained, SimStep.ticks(1), FIRE, SimEvents.Type.SHOT_FIRED), "full again");
    }

    @Test
    void aLoadOverTheDrainedOutputStopsTheShieldsRegenerationButNotItsDelay() {
        Sortie sortie = sortie(20, 18, 4);
        Defences defences = sortie.ship().defences();
        sortie.step(Command.NONE);
        defences.takeShot(10, sortie.events(), 0, 0);
        double after = defences.shield();
        sortie.drain(1);
        sortie.drain(1);
        TorpedoTest.run(sortie, SimStep.ticks(5), Command.NONE);
        assertEquals(after, defences.shield(), 1e-9, "two leeches: no regrowth");
        sortie.drain(-1);
        sortie.step(Command.NONE);
        assertTrue(defences.shield() > after, "it regrows at once: the delay ran on");
    }

    @Test
    void withoutADrainerAPoweredSortieFliesAndHashesAsAnUnpoweredOne() {
        Loadout plain = TestSpecs.loadout(new Armament.Mount(Armament.Slot.FRONT, BOLT, BOLT));
        Loadout powered = plain.withPower(Act5Specs.power(20, 18, 4, 2));
        Sortie a = new Sortie(1, plain, TorpedoTest.units(false), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        Sortie b = new Sortie(1, powered, TorpedoTest.units(false), TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        for (int i = 0; i < SimStep.ticks(3); i++) {
            int command = i % 7 < 4 ? FIRE : 0;
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash());
        }
        assertEquals(0, a.drain(1), "no budget: nothing drains");
        assertTrue(a.power().isEmpty());
    }

    @Test
    void aDrainReplaysToTheSameHashAndAllocatesNothing() {
        Supplier<Sortie> make = () -> sortie(20, 20, 4);
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(6); i++) {
            if (i % 90 == 10) {
                a.drain(i < 200 ? 1 : -1);
                b.drain(i < 200 ? 1 : -1);
            }
            a.step(FIRE);
            b.step(FIRE);
            assertEquals(a.stateHash(), b.stateHash());
        }
        long allocated = Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(6); i++) {
                if (i % 90 == 10) {
                    sortie.drain(i < 200 ? 1 : -1);
                }
                sortie.step(FIRE);
            }
        });
        assertEquals(0, allocated);
    }
}
