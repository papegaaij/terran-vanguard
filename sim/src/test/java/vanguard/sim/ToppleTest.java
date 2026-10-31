package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * M9 part E, step E1 (design/enemies/ground/strider, The topple; m9e-gaps.md §1 #6): either leg's loss starts the
 * stagger (0.6 s, the guns stop, the fall line marked on the lost leg's side), the fall (0.8 s), then the crush: the
 * Strider is destroyed (its whole 60 paid) and every ground unit under the line too (hardened ones included, paid and
 * counted as kills), never an air unit; it takes no damage while it falls and the EMP does not stop it. The pod carries
 * only the homing salvos. M10 part E, step E1 (design/campaign Level 39; m10e-gaps.md §1 #6, #7): a crush that takes a
 * destroy-targets unit clears its group and starts the {@code topple-target} cues once; the first target group cleared
 * starts the {@code first-target} cues once.
 */
class ToppleTest {
    private static final int FIRE = Command.FIRE.bit();
    /** The ground scroll: the Strider walking up the screen at 32 px/s stands still on it. */
    private static final double SCROLL = 32;
    /** The ship holds its fire until then, so the ground units can come down to the Strider's row. */
    private static final double FIRE_FROM = 13;

    /** The Strider walking up the screen from 300 px below the top edge (it stands still on the screen). */
    private static WaveSpec strider() {
        return SiegeSpecs.walker(0, Act5ESpecs.STRIDER, new WaveSpec.At(240, 300), new WaveSpec.At(240, -5_000));
    }

    private static LevelScript level(List<GroundUnit> units) {
        return level(units, List.of());
    }

    /** The cues of {@link #targetLevel}: the crush's, the topple's on a target, the first target's. */
    private static final List<RadioCue> TARGET_CUES = List.of(
            LevelClockSpecs.cue(CueTrigger.TOPPLE_CRUSH, 0, 0),
            LevelClockSpecs.cue(CueTrigger.TOPPLE_TARGET, 0, 0),
            LevelClockSpecs.cue(CueTrigger.FIRST_TARGET, 0, 0));

    private static LevelScript level(List<GroundUnit> units, List<String> targets) {
        return LevelScript.builder(32, 5)
                .sections(List.of(new Section(40, SCROLL)))
                .waves(List.of(strider()))
                .groundUnits(units)
                .radio(targets.isEmpty() ? List.of() : TARGET_CUES)
                .objectives(ObjectiveSpec.NONE.withTargets(targets))
                .build();
    }

    private static Sortie sortie(List<GroundUnit> units, WeaponSpec weapon) {
        return sortie(level(units), weapon);
    }

    private static Sortie sortie(LevelScript level, WeaponSpec weapon) {
        return new Sortie(
                1, Act5ESpecs.loadout(weapon), level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** What a flight saw: the steps of the tell, the fall and the crush, where it fell, the kills and the credits. */
    private record Flight(
            int tell,
            int fall,
            int crush,
            double x,
            double y,
            double dirX,
            int crushed,
            int kills,
            int creditsAtTell,
            Sortie sortie,
            int[] cued,
            int cleared) {}

    /** Under the right leg (facing up the screen, the left leg is at the screen's right), firing from {@link #FIRE_FROM}. */
    private static Flight fly(List<GroundUnit> units, double legX, WeaponSpec weapon, boolean emp) {
        return fly(level(units), legX, weapon, emp);
    }

    private static Flight fly(LevelScript level, double legX, WeaponSpec weapon, boolean emp) {
        Sortie sortie = sortie(level, weapon);
        int[] cued = new int[TARGET_CUES.size()];
        int cleared = 0;
        int tell = -1;
        int fall = -1;
        int crush = -1;
        double x = 0;
        double y = 0;
        double dirX = 0;
        int crushed = 0;
        int creditsAtTell = 0;
        for (int step = 1; step <= SimStep.ticks(22) && crush < 0; step++) {
            int move = sortie.ship().x() < legX - 2
                    ? Command.RIGHT.bit()
                    : sortie.ship().x() > legX + 2 ? Command.LEFT.bit() : 0;
            boolean firing = sortie.levelSeconds() >= FIRE_FROM && tell < 0;
            sortie.step(move | (firing ? FIRE : 0));
            Enemy strider = PartCSpecs.find(sortie, "strider");
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.RADIO) {
                    cued[events.value(e)]++;
                }
                if (events.type(e) == SimEvents.Type.GROUP_CLEARED) {
                    cleared++;
                }
                if (events.type(e) == SimEvents.Type.TOPPLE_TELL) {
                    tell = step;
                    creditsAtTell = sortie.credits();
                    assertNotNull(strider);
                    assertTrue(strider.staggering());
                    if (emp) {
                        strider.empStun(SimStep.ticks(3));
                    }
                }
                if (events.type(e) == SimEvents.Type.TOPPLE_FALL) {
                    fall = step;
                }
                if (events.type(e) == SimEvents.Type.TOPPLE_CRUSH) {
                    crush = step;
                    x = events.x(e);
                    y = events.y(e);
                    crushed = events.value(e);
                }
            }
            if (strider != null && strider.toppling()) {
                dirX = strider.fallDirX();
                assertTrue(strider.gunQuiet() && strider.spitQuiet(), "its guns stop");
            }
        }
        // Two steps on: the cues of the crush's kills.
        for (int step = 0; step < 2 && crush > 0; step++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.RADIO) {
                    cued[events.value(e)]++;
                }
                if (events.type(e) == SimEvents.Type.GROUP_CLEARED) {
                    cleared++;
                }
            }
        }
        return new Flight(tell, fall, crush, x, y, dirX, crushed, sortie.kills(), creditsAtTell, sortie, cued, cleared);
    }

    private static final WeaponSpec HEAVY = Act5ESpecs.bolt(20, false);

    @Test
    void eitherLegStartsTheStaggerAndTheCrushComes1point4sLater() {
        Flight left = fly(List.of(), 240 + 42, HEAVY, false);
        assertTrue(left.tell() > 0, "the leg fell");
        assertEquals(left.tell() + SimStep.ticks(0.6), left.fall());
        assertEquals(left.tell() + SimStep.ticks(0.6) + SimStep.ticks(0.8), left.crush());
        assertEquals(1, left.dirX(), 1e-9, "it falls toward the lost leg (the screen's right, facing up)");
        assertNull(PartCSpecs.find(left.sortie(), "strider"), "destroyed on landing");
        assertEquals(1, left.kills());
        // Its whole 60: the leg's 6 when it fell, the hull's 40 and the standing leg's and pod's 6 + 8 on landing.
        assertTrue(left.creditsAtTell() > 0);
        assertEquals(10, (double) left.sortie().credits() / left.creditsAtTell(), 0.2);
        Flight right = fly(List.of(), 240 - 42, HEAVY, false);
        assertEquals(-1, right.dirX(), 1e-9);
    }

    @Test
    void theCrushDestroysTheGroundUnitsUnderItsLine_hardenedIncluded_neverAnAirUnit() {
        Flight probe = fly(List.of(), 240 + 42, HEAVY, false);
        double crushSeconds = probe.crush() * SimStep.SECONDS;
        // Enter each unit so it stands on the crush's row then: it comes down from the top edge at the scroll.
        List<GroundUnit> units = new ArrayList<>();
        double row = probe.y();
        units.add(at(crushSeconds, row, 330, Act5ESpecs.NEST));
        units.add(at(crushSeconds, row, 310, Act5ESpecs.BUNKER));
        units.add(at(crushSeconds, row, 120, Act5ESpecs.NEST));
        EnemySpec flyer = PartASpecs.dummy(Layer.AIR, 32, new Hitbox(28, 24), false);
        units.add(at(crushSeconds, row, 340, flyer));
        Flight flight = fly(units, 240 + 42, HEAVY, false);
        assertEquals(probe.crush(), flight.crush(), "the same topple");
        assertEquals(2, flight.crushed(), "the turret and the hardened bunker on the line");
        assertEquals(3, flight.kills(), "they count as kills, with the Strider");
        assertNotNull(PartCSpecs.find(flight.sortie(), "sam-nest"), "the turret off the line stands");
        assertNotNull(PartCSpecs.find(flight.sortie(), "dummy"), "the air unit over the line stands");
        assertNull(PartCSpecs.find(flight.sortie(), "rail-bunker"));
    }

    /** A ground unit entering so it is at {@code row} (play-field y) at {@code seconds}. */
    private static GroundUnit at(double seconds, double row, double x, EnemySpec spec) {
        double half = spec.hitbox().height() / 2;
        double t = seconds - (PlayField.HEIGHT + half - row) / SCROLL;
        assertTrue(t > 0, "" + t);
        return new GroundUnit(t, x, spec, -1);
    }

    @Test
    void aCrushOnTargetsClearsTheirGroupsAndCuesToppleTargetAndFirstTargetOnce() {
        Flight probe = fly(List.of(), 240 + 42, HEAVY, false);
        double crushSeconds = probe.crush() * SimStep.SECONDS;
        double row = probe.y();
        // Two towers of their own groups under the line: both cleared by the one crush, each cue once.
        Flight both = fly(
                level(
                        List.of(
                                new GroundUnit(
                                        at(crushSeconds, row, 310, Act5ESpecs.NEST)
                                                .t(),
                                        310,
                                        Act5ESpecs.NEST,
                                        0),
                                new GroundUnit(
                                        at(crushSeconds, row, 340, Act5ESpecs.NEST)
                                                .t(),
                                        340,
                                        Act5ESpecs.NEST,
                                        1)),
                        List.of("Pele A", "Pele B")),
                240 + 42,
                HEAVY,
                false);
        assertEquals(probe.crush(), both.crush(), "the same topple");
        assertEquals(2, both.crushed());
        assertEquals(2, both.cleared(), "both groups cleared by the crush");
        assertEquals(1, both.cued()[0], "topple-crush");
        assertEquals(1, both.cued()[1], "topple-target, once");
        assertEquals(1, both.cued()[2], "first-target, once");
        // A crush of a ground unit outside the targets: no topple-target (the target off the line stands).
        Flight other = fly(
                level(
                        List.of(
                                at(crushSeconds, row, 330, Act5ESpecs.NEST),
                                new GroundUnit(
                                        at(crushSeconds, row, 120, Act5ESpecs.NEST)
                                                .t(),
                                        120,
                                        Act5ESpecs.NEST,
                                        0)),
                        List.of("Pele B")),
                240 + 42,
                HEAVY,
                false);
        assertEquals(1, other.crushed());
        assertEquals(0, other.cleared());
        assertEquals(1, other.cued()[0], "topple-crush");
        assertEquals(0, other.cued()[1], "no topple-target");
        assertEquals(0, other.cued()[2], "no target cleared");
    }

    @Test
    void itTakesNoDamageWhileItFalls_andTheEmpDoesNotStopIt() {
        Flight stunned = fly(List.of(), 240 + 42, HEAVY, true);
        Flight plain = fly(List.of(), 240 + 42, HEAVY, false);
        assertEquals(
                plain.crush() - plain.tell(), stunned.crush() - stunned.tell(), "a stagger under the EMP still falls");
        Enemy enemy = new Enemy();
        assertFalse(enemy.toppling());
    }

    @Test
    void thePodCarriesOnlyTheHomingSalvos() {
        Sortie sortie = sortie(List.of(), HEAVY);
        int missiles = 0;
        for (int step = 0; step < SimStep.ticks(12); step++) {
            sortie.step(0);
            missiles += sortie.events().count(SimEvents.Type.MISSILE_LAUNCHED);
        }
        assertTrue(missiles >= 2, "salvos of 2: " + missiles);
        Enemy strider = PartCSpecs.find(sortie, "strider");
        int pod = 2;
        assertTrue(strider.damagePart(pod, 30, false), "the pod destroyed");
        assertTrue(strider.spitQuiet(), "the salvos stop");
        assertFalse(strider.gunQuiet(), "the cannons fire on");
        assertFalse(strider.toppling(), "the pod does not topple it");
    }

    @Test
    void aToppleNeedsAWalkerWithALegThatTopplesIt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5ESpecs.NEST.withToppleParts(List.of(Act5ESpecs.LEFT_LEG), Optional.of(Act5ESpecs.TOPPLE)));
        assertThrows(
                IllegalArgumentException.class,
                () -> Act5ESpecs.STRIDER.withToppleParts(List.of(Act5ESpecs.LEFT_LEG), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> Act5ESpecs.NEST.withParts(List.of(Act5ESpecs.POD_PART)));
        assertTrue(Toppling.underLine(240, 300, 1, 0, 48, 140, 370, 300, 16, 16), "at the line's end");
        assertFalse(Toppling.underLine(240, 300, 1, 0, 48, 140, 400, 300, 16, 16), "past it");
        assertFalse(Toppling.underLine(240, 300, 1, 0, 48, 140, 300, 345, 16, 16), "beside it");
        assertTrue(Toppling.underLine(240, 300, 0, -1, 48, 140, 240, 170, 16, 16), "down the screen");
    }
}
