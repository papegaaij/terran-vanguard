package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1a (design/enemies/ground/shield-pylon, The dome): a circle of 110 px centred 140 px from the pylon
 * along its crown's bearing covers every ground unit whose centre lies in it: no damage from any path (a shot spent with
 * a gold spark, a blast does nothing, a beam stops there); air units and the pylons are never covered. The EMP drops
 * every dome for the stun, a breaker's hit on the pylon drops its dome 2 s, a dropped dome comes back over 0.3 s; the
 * pylon's death drops it for good. The seekers skip covered units while an uncovered target is on the screen.
 */
class DomeTest {
    private static final int FIRE = Command.FIRE.bit();
    /** The pylon 60 px left of the ship's column; its dome's centre 140 px above it. */
    private static final double PYLON_X = Ship.START_X - 60;
    /** The deck's scroll, px/s: slow, so the pylon stays on the screen through a stun. */
    private static final double SCROLL = 40;
    /** When a unit entering at the top stands level with the dome's centre (140 px above the pylon). */
    private static final double CENTRE_T = 140 / SCROLL;

    private static Sortie sortie(Armament.Mount mount, GroundUnit... more) {
        List<GroundUnit> units = new java.util.ArrayList<>();
        units.add(new GroundUnit(0, PYLON_X, Act6ASpecs.PYLON, -1));
        units.addAll(List.of(more));
        units.sort(java.util.Comparator.comparingDouble(GroundUnit::t));
        return Act6ASpecs.sortie(Act6ASpecs.level(40, SCROLL, units, List.of()), mount);
    }

    /** A ground target of 40 HP in the ship's column, level with the dome's centre (60 px from it): covered. */
    private static GroundUnit covered() {
        return new GroundUnit(CENTRE_T, Ship.START_X, Act6ASpecs.target(40), -1);
    }

    private static Enemy unit(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    private static Enemy pylon(Sortie sortie) {
        return unit(sortie, "shield-pylon");
    }

    @Test
    void aCoveredUnitTakesNoDamageEveryShotSpentWithAGoldSpark() {
        Sortie sortie = sortie(Act6ASpecs.front(), covered());
        Enemy target = Act6ASpecs.until(sortie, "dummy-GROUND", 200);
        assertTrue(target.covered());
        assertTrue(target.invulnerable());
        assertFalse(pylon(sortie).covered(), "never the pylon");
        int sparks = 0;
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(FIRE);
            sparks += sortie.events().count(SimEvents.Type.DOME_HIT);
        }
        assertTrue(sparks > 5, "every shot spent on it: " + sparks);
        assertEquals(40, target.hp());
        assertFalse(target.damage(30, true, true), "a blast or a special inside does nothing");
        assertEquals(40, target.hp());
    }

    @Test
    void outsideTheDomeOrInTheAirNothingIsCovered() {
        Sortie sortie = sortie(
                Act6ASpecs.front(),
                new GroundUnit(CENTRE_T, PYLON_X + 120, Act6ASpecs.target(40), -1),
                new GroundUnit(CENTRE_T, Ship.START_X, Act6ASpecs.air(40), -1));
        Act6ASpecs.until(sortie, "dummy-GROUND", 200);
        Enemy outside = unit(sortie, "dummy-GROUND");
        Enemy air = unit(sortie, "dummy-AIR");
        assertFalse(outside.covered(), "120 px from the centre: outside its 110");
        assertFalse(air.covered(), "it flies above the dome");
        Act6ASpecs.run(sortie, SimStep.ticks(0.5), FIRE);
        assertTrue(air.hp() < 40, "the ship's shots reach it");
    }

    @Test
    void aBeamStopsOnACoveredUnit() {
        Sortie sortie = sortie(
                PartASpecs.beamMount(3),
                covered(),
                new GroundUnit(CENTRE_T + 1.3, Ship.START_X, Act6ASpecs.air(40), -1));
        Act6ASpecs.until(sortie, "dummy-AIR", 60);
        Enemy target = unit(sortie, "dummy-GROUND");
        Enemy behind = unit(sortie, "dummy-AIR");
        int sparks = 0;
        for (int i = 0; i < SimStep.ticks(0.5); i++) {
            sortie.step(FIRE);
            sparks += sortie.events().count(SimEvents.Type.DOME_HIT);
        }
        assertTrue(sparks > 0);
        assertEquals(Beam.Contact.BLOCKED, sortie.beam(0).contact());
        assertEquals(40, target.hp());
        assertEquals(40, behind.hp(), "the beam stopped there");
    }

    @Test
    void theEmpDropsTheDomeForTheStunAndItComesBackOverAFlicker() {
        Sortie sortie = sortie(Act6ASpecs.front(), covered());
        Enemy target = Act6ASpecs.until(sortie, "dummy-GROUND", 100);
        int stun = SimStep.ticks(3);
        pylon(sortie).empStun(stun);
        sortie.step(Command.NONE);
        assertEquals(1, sortie.events().count(SimEvents.Type.DOME_DOWN));
        assertFalse(target.covered());
        assertTrue(target.damage(1) || target.hp() == 39, "it takes damage now");
        int up = -1;
        for (int i = 0; i < stun + SimStep.ticks(0.5) && up < 0; i++) {
            sortie.step(Command.NONE);
            if (sortie.events().count(SimEvents.Type.DOME_UP) > 0) {
                up = i + 2;
            }
        }
        assertEquals(stun + SimStep.ticks(0.3), up, 1, "back 0.3 s after the stun");
        assertTrue(target.covered());
    }

    @Test
    void aBreakersHitDropsItsDomeTwoSecondsAndThePylonsDeathForGood() {
        Sortie sortie = sortie(Act6ASpecs.front(), covered());
        Enemy target = Act6ASpecs.until(sortie, "dummy-GROUND", 100);
        Domes domes = sortie.domes();
        assertNotNull(domes);
        domes.breakerHit(pylon(sortie));
        Act6ASpecs.run(sortie, SimStep.ticks(2.0), Command.NONE);
        assertFalse(target.covered(), "dropped 2 s");
        Act6ASpecs.run(sortie, SimStep.ticks(0.3) + 1, Command.NONE);
        assertTrue(target.covered(), "back after its flicker");
        sortie.destroyEnemy(sortie.enemy(0) == pylon(sortie) ? 0 : 1);
        sortie.step(Command.NONE);
        assertEquals(1, sortie.events().count(SimEvents.Type.DOME_COLLAPSED));
        assertNull(pylon(sortie));
        assertFalse(target.covered(), "for good");
        Act6ASpecs.run(sortie, SimStep.ticks(1), FIRE);
        assertTrue(target.hp() < 40);
    }

    @Test
    void theSeekersSkipACoveredUnitWhileAnUncoveredTargetIsOnTheScreen() {
        Sortie sortie = sortie(Act6ASpecs.front(), covered());
        Enemy target = Act6ASpecs.until(sortie, "dummy-GROUND", 100);
        assertTrue(target.covered());
        assertTrue(target.shunned, "the pylon is an uncovered target");
        sortie.destroyEnemy(sortie.enemy(0) == pylon(sortie) ? 0 : 1);
        sortie.step(Command.NONE);
        assertFalse(target.shunned);
    }

    @Test
    void aDomeHasARadiusAndIsAFixedGroundUnits() {
        assertThrows(IllegalArgumentException.class, () -> new EnemySpec.Dome(0, 140, 0, 2, 0.3));
        assertThrows(IllegalArgumentException.class, () -> Act6ASpecs.air(10)
                .withDome(java.util.Optional.of(Act6ASpecs.dome(0))));
        EnemySpec.Dome down = Act6ASpecs.dome(0);
        assertEquals(100, down.centreX(100), 1e-9);
        assertEquals(60, down.centreY(200), 1e-9, "bearing 0: straight down the screen");
    }
}
