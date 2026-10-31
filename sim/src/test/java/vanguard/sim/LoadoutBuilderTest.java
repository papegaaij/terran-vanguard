package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1b (m10a-gaps.md §2, as RF9b's {@code WaveSpec} builder): a {@link Loadout} is built and copied
 * through its {@link Loadout.Builder}, its {@code withX} copies one-liners on it; the canonical constructor's checks run
 * once on the whole loadout. The IFF spoofer is a loadout component: fitted, every spoofable unit's volley comes late.
 */
class LoadoutBuilderTest {
    @Test
    void theBuilderAndTheCopiesAgree() {
        Loadout plain = TestSpecs.LOADOUT;
        assertEquals(plain, plain.toBuilder().build());
        assertEquals(
                plain,
                Loadout.builder(TestSpecs.SHIP, plain.armament(), TestSpecs.SHIELD, TestSpecs.PLATING)
                        .build());
        Loadout fitted = plain.withSalvage(0.2).withPressureHull().withSpoofer(0.5);
        assertEquals(0.2, fitted.salvageBonus());
        assertTrue(fitted.pressureHull());
        assertEquals(0.5, fitted.spoofDelay());
        assertEquals(plain.armament(), fitted.armament());
        assertEquals(0, plain.spoofDelay(), "no spoofer unless fitted");
        assertEquals(fitted, fitted.toBuilder().build());
    }

    @Test
    void theChecksRunOnTheWholeLoadout() {
        Loadout plain = TestSpecs.LOADOUT;
        assertThrows(IllegalArgumentException.class, () -> plain.withSpoofer(-0.1));
        assertThrows(IllegalArgumentException.class, () -> plain.withSalvage(-1));
        assertThrows(IllegalArgumentException.class, () -> plain.toBuilder()
                .power(Optional.empty())
                .salvageBonus(Double.NaN)
                .build());
    }

    @Test
    void theFittedSpooferDelaysTheSpoofableTurretsVolleys() {
        LevelScript level =
                Act6ASpecs.level(12, 60, List.of(new GroundUnit(0, Ship.START_X, Act6ASpecs.TURRET, -1)), List.of());
        Sortie plain =
                new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
        Sortie spoofed = new Sortie(
                1,
                TestSpecs.LOADOUT.withSpoofer(0.5),
                level,
                TestSpecs.RULES.withInvulnerableShip(),
                TestSpecs.FULL_ARMOUR);
        int plainHesitations = Act6BSpecs.run(plain, SimStep.ticks(6), 0, SimEvents.Type.SPOOF_HESITATE);
        int hesitations = Act6BSpecs.run(spoofed, SimStep.ticks(6), 0, SimEvents.Type.SPOOF_HESITATE);
        assertEquals(0, plainHesitations, "without the module no volley waits");
        assertTrue(hesitations > 0, "with it the turret's volleys are held");
    }
}
