package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part D, step D1 (design/allies/mining-tug; design/campaign Level 31, The tug in this level): the hull escort's
 * entry, station and sway, climb-out, section hits and losses, the fail when every section is lost, the pay per section
 * home and the CLEAN HULL integrity secondary, replayed.
 */
class HullEscortTest {
    private static Sortie sortie(LevelScript level, Rules rules) {
        return new Sortie(7, TestSpecs.LOADOUT, level, rules, TestSpecs.FULL_ARMOUR);
    }

    private static Sortie sortie(ObjectiveSpec.Escort escort, double seconds) {
        return sortie(Act5DSpecs.level(seconds, List.of(), escort), TestSpecs.RULES);
    }

    private static void fly(Sortie sortie, double seconds) {
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(0);
        }
    }

    @Test
    void itRisesIntoItsStationUntouchableAndSwaysThere() {
        Sortie sortie = sortie(Act5DSpecs.ESCORT, 30);
        assertTrue(sortie.hullEscort());
        assertEquals(3, sortie.allyCount());
        assertEquals("bow", sortie.allyName(0));
        assertEquals("engines", sortie.allyName(2));
        sortie.step(0);
        assertTrue(sortie.hullY(1) < 0, "below the bottom edge at the start: " + sortie.hullY(1));
        for (int k = 0; k < 3; k++) {
            assertTrue(sortie.ally(k).untouchable(), "rising: untouchable");
            assertFalse(sortie.ally(k).touchable());
        }
        fly(sortie, 3);
        assertTrue(sortie.ally(0).untouchable(), "still rising at 3 s");
        fly(sortie, 4);
        double seconds = sortie.levelSeconds();
        double x = 240 + 10 * Trig.sin(2 * Math.PI * seconds / 14);
        double y = 160 - 2.5 * Trig.sin(4 * Math.PI * seconds / 14);
        assertEquals(x, sortie.hullX(1), 1e-6);
        assertEquals(y, sortie.hullY(1), 1e-6);
        for (int k = 0; k < 3; k++) {
            assertTrue(sortie.ally(k).touchable(), "on station: touchable");
            ObjectiveSpec.HullSection section = sortie.hullSection(k);
            assertEquals(x + section.dx(), sortie.ally(k).x(), 1e-6);
            assertEquals(y + section.dy(), sortie.ally(k).y(), 1e-6);
            assertEquals(section.box(), sortie.allySpec(k).hitbox());
            assertEquals(section.hp(), sortie.allySpec(k).hp());
        }
        // The engines' box ends ≈ 13 px above the bottom edge at the sway's lowest (§6).
        double enginesBottom = y - 103 - 42;
        assertTrue(enginesBottom > 10 && enginesBottom < 20, "engines' bottom: " + enginesBottom);
    }

    @Test
    void itClimbsOutUntouchableAndIsHome() {
        ObjectiveSpec.Escort escort =
                Act5DSpecs.escort(Act5DSpecs.hull(Act5DSpecs.sections(240, 160, 160), 10, Act5DSpecs.HOOKED));
        Sortie sortie = sortie(escort, 20);
        fly(sortie, 10.5);
        assertTrue(sortie.ally(1).untouchable(), "climbing out");
        assertEquals(Ally.State.CLIMBING, sortie.ally(1).state());
        fly(sortie, 6);
        assertEquals(Ally.State.HOME, sortie.ally(1).state());
        assertTrue(sortie.hullY(1) > PlayField.HEIGHT + 149, "off the top edge: " + sortie.hullY(1));
    }

    @Test
    void sectionsTakeTheirHitsLoseInAnyOrderAndAllLostFailsTheEscort() {
        Sortie sortie = sortie(Act5DSpecs.ESCORT, 60);
        fly(sortie, 7);
        HullEscort hull = sortie.convoy.hull();
        hull.hit(2, 100);
        sortie.step(0);
        assertEquals(60.0 / 160, sortie.ally(2).hpShare(), 1e-9);
        assertEquals(1, sortie.ally(0).hpShare());
        assertEquals(2, sortie.firstAllyHit());
        hull.hit(2, 100);
        sortie.step(0);
        assertTrue(sortie.ally(2).lost(), "the engines lost first");
        assertEquals(2, sortie.firstAllyLost());
        assertEquals(2, sortie.saveableAlliesAlive());
        assertFalse(sortie.primaryFailed());
        fly(sortie, 1);
        // A lost section keeps its place on the hull, dark, and nothing hurts it any more.
        assertEquals(sortie.hullY(1) - 103, sortie.ally(2).y(), 1e-6);
        assertTrue(sortie.ally(2).untouchable());
        assertEquals(1, hull.nearest(240, 0), "the nearest living section from below is the cargo");
        hull.hit(0, 1000);
        sortie.step(0);
        assertEquals(1, sortie.saveableAlliesAlive());
        assertFalse(sortie.primaryFailed());
        hull.hit(1, 1000);
        sortie.step(0);
        assertEquals(0, sortie.saveableAlliesAlive());
        assertTrue(sortie.primaryFailed(), "every section lost fails the escort");
        assertEquals(-1, hull.nearest(240, 300));
    }

    @Test
    void eachSectionHomePaysItsCredits() {
        ObjectiveSpec.Escort escort = Act5DSpecs.escort(Act5DSpecs.hull(
                List.of(
                        new ObjectiveSpec.HullSection("bow", 0, 108, new Hitbox(130, 76), 240, 30),
                        new ObjectiveSpec.HullSection("cargo", 0, 4, new Hitbox(180, 110), 160, 50),
                        new ObjectiveSpec.HullSection("engines", 0, -103, new Hitbox(134, 84), 160, 70)),
                10,
                Act5DSpecs.HOOKED));
        Sortie sortie = sortie(escort, 18);
        fly(sortie, 8);
        sortie.convoy.hull().hit(1, 1000);
        while (!sortie.complete()) {
            sortie.step(0);
        }
        LevelResult.Escort home = sortie.result().escort();
        assertEquals("mining-tug", home.ally());
        assertEquals(2, home.home());
        assertEquals(3, home.units());
        double factor = TestSpecs.RULES.scoring().creditFactor();
        assertEquals((30 + 70) * factor, home.credits(), 2 * factor);
    }

    @Test
    void theIntegritySecondaryNeedsEverySectionAtHalfOrMore() {
        LevelScript level = Act5DSpecs.level(
                        18,
                        List.of(),
                        Act5DSpecs.escort(Act5DSpecs.hull(Act5DSpecs.sections(240, 160, 160), 10, Act5DSpecs.HOOKED)))
                .toBuilder()
                .objectives(ObjectiveSpec.NONE
                        .withEscort(Optional.of(Act5DSpecs.escort(
                                Act5DSpecs.hull(Act5DSpecs.sections(240, 160, 160), 10, Act5DSpecs.HOOKED))))
                        .withSecondary(Secondary.integrity(0.5, 100, "CLEAN HULL")))
                .build();
        Sortie clean = sortie(level, TestSpecs.RULES);
        fly(clean, 8);
        clean.convoy.hull().hit(0, 100);
        while (!clean.complete()) {
            clean.step(0);
        }
        assertTrue(clean.result().secondaryMet(), "every section home at half or more");
        Sortie dented = sortie(level, TestSpecs.RULES);
        fly(dented, 8);
        dented.convoy.hull().hit(1, 81);
        dented.step(0);
        assertTrue(dented.integrityFailed(), "the cargo below half fails it at once");
        while (!dented.complete()) {
            dented.step(0);
        }
        assertFalse(dented.result().secondaryMet());
    }

    @Test
    void aHullIsCheckedWhereItIsBuilt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5DSpecs.hull(
                        List.of(new ObjectiveSpec.HullSection("bow", 0, 120, new Hitbox(130, 76), 240, 40)),
                        178,
                        Act5DSpecs.HOOKED),
                "a section past the footprint");
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5DSpecs.hull(Act5DSpecs.sections(240, 160, 160), 178, Map.of("hornet", HookMode.NEAREST)),
                "a hull's hook is always or none");
        assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectiveSpec.Escort(
                        Act5DSpecs.TUG,
                        List.of(1.0, 2.0, 3.0),
                        0,
                        0,
                        1,
                        0,
                        List.of("hornet"),
                        Optional.empty(),
                        Optional.of(Act5DSpecs.hull(Act5DSpecs.sections(240, 160, 160), 178, Act5DSpecs.HOOKED))),
                "a hull's hook is its own");
    }

    @Test
    void itReplays() {
        Sortie one = sortie(Act5DSpecs.ESCORT, 40);
        Sortie two = sortie(Act5DSpecs.ESCORT, 40);
        for (Sortie sortie : List.of(one, two)) {
            fly(sortie, 9);
            sortie.convoy.hull().hit(1, 30);
            fly(sortie, 2);
        }
        assertEquals(one.stateHash(), two.stateHash());
    }
}
